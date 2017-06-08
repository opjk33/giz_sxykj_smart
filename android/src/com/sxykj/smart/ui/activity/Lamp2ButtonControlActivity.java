package com.sxykj.smart.ui.activity;

import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.opensource.devicecontrol.ui.activity.GosDeviceMoreActivity;
import com.xxx.giz_sxykj_smart.R;

public class Lamp2ButtonControlActivity extends ComDeviceControlModuleBaseActivity implements OnClickListener{


	/*
	 * ======================================================================
	 * 以下定义的是云端数据点对应的存储值
	 * ======================================================================
	 */	
	/** 数据点"siwtch0"对应的值**/
	protected static Boolean [] isPowerOns={false,false};
	/** 数据点"Brightness0"对应的值**/
	protected static int [] bringhtness_nums={0,0};	
	final int Num=2;

	/*
	 * ======================================================================
	 * 以下对应UI
	 * ======================================================================
	 */	
	/** 返回按钮 */
	private ImageView ivBack;

	/** 标题TextView */
	private TextView tvTitle;

	/** 设置按钮 */
	private ImageView ivSetting;

	/** 开关按钮 */
	private Button [] btnPowers=new Button[Num];
	/** 亮度进度条 */
	private SeekBar [] sbBrightenesss=new SeekBar[Num];
	/** 开关TextView */
	private TextView [] tvPowers=new TextView[Num];
	/** 开关名称*/
	private TextView [] tvBtnNames=new TextView[Num];

	
	private Runnable mRunnable = new Runnable() {
		public void run() {
			if (isDeviceCanBeControlled()) {
				progressDialog.cancel();
			} else {
				handler.sendEmptyMessage(handler_key.DEV_NOREADY.ordinal());
			}
		}

	};
	private enum handler_key {
		/** 获取设备状态*/
		GET_DEV_STUAT,

		/** 接收到设备的数据 */
		RECEIVED,

		/** 设备未就绪 */
		DEV_NOREADY,
	}

	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			handler_key key = handler_key.values()[msg.what];
			switch (key) {
			case GET_DEV_STUAT:
				device.getDeviceStatus();
				break;
			case DEV_NOREADY:
				toastDeviceNoReadyAndExit();
				break;
			case RECEIVED:
				Log.e("debug", "handler reciever data");
				progressDialog.cancel();
				getDataFromDateMap();
				upDateUI();
				break;
			default:
				break;
			}
		}

	};

	//	protected void upDateUI() {
	//		updateTitle();
	////		updatePowerUI(isPowerOn0);
	//		updatePowerUI(0,isPowerOn0);
	//		updateBrighteness(bringhtness_num0);
	//	}

	protected void upDateUI() {
		Log.e("debug", "update ui ");
		updateTitle();
		for(int i=0;i<Num;i++){
			updatePowerUI(i,isPowerOns[i]);
		}
		for(int i=0;i<Num;i++){
			updateBrighteness(i,bringhtness_nums[i]);
		}
	}

	private void updateTitle() {
		tvTitle.setText(getDeviceName());		
	}

	/**
	 * 更新电源开关切换.
	 */

	private void updatePowerUI(int id,boolean isPower){
		if(!isPower){
			btnPowers[id].setSelected(true);
			tvPowers[id].setText(getString(R.string.openlight));
		}else{
			btnPowers[id].setSelected(false);
			tvPowers[id].setText(getString(R.string.closelight));
		}
	}

	//	private void updatePowerUI(boolean isPower) {
	//		if (!isPower) {
	//			btnPower.setSelected(true);
	//			tvPower.setText(getString(R.string.openlight));
	//			ivmain.setVisibility(View.VISIBLE);
	//			rl_top.setVisibility(View.VISIBLE);	
	//		} else {
	//			btnPower.setSelected(false);
	//			tvPower.setText(getString(R.string.closelight));			
	//			ivPowerOff.setVisibility(View.INVISIBLE);
	//			rl_middle.setVisibility(View.VISIBLE);
	//			ivmain.setVisibility(View.INVISIBLE);
	//		}
	//	}



	/*
	 * 更新亮度值
	 */
	//	private void updateBrighteness(int bringhtness_num) {
	//		
	//		sbBrighteness.setProgress(bringhtness_num);
	//	}

	private void updateBrighteness(int id,int bringhtness_num) {
		sbBrightenesss[id].setProgress(bringhtness_num);
		//		sbBrighteness.setProgress(bringhtness_num);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("debug", "lamp 3 button oncreate start");
		setContentView(R.layout.activity_lamp_two_btn_device_control);		
		initView();
		initEvent();
		initDevice();		
		upDateUI();
		Log.e("debug", "lamp 3 button oncreate");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e("debug", "lamp 3 button onresume");
		// 每次界面可视时候将获取重新获取设备装备
		getStatusOfSocket();
		upDateUI();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		device.setSubscribe(false);
	}

	private void toastDeviceNoReadyAndExit() {
		myToast(R.string.device_no_ready);
		finish();
	}

	private boolean isDeviceCanBeControlled() {
		return device.getNetStatus() == GizWifiDeviceNetStatus.GizDeviceControlled;
	}

	/**
	 * Description:判断当前设备是否可控.
	 */
	private void getStatusOfSocket() {

		// 设备是否可控
		if (isDeviceCanBeControlled()) {
			// 可控则查询当前设备状态
			device.getDeviceStatus();
		} else {
			// 显示等待栏
			setProgressDialog(getResources().getString(R.string.wait_for_connet), true, false);
			progressDialog.show();
			if (device.isLAN()) {
				// 小循环10s未连接上设备自动退出
				handler.postDelayed(mRunnable, 10000);
			} else {
				// 大循环20s未连接上设备自动退出
				handler.postDelayed(mRunnable, 20000);
			}
		}
	}


	LinearLayout [] lampCtrViews=new LinearLayout[Num] ;
	

	private void initView() {
		ivBack = (ImageView) findViewById(R.id.iv_back);
		tvTitle = (TextView) findViewById(R.id.tvTitle);
		ivSetting = (ImageView) findViewById(R.id.ivSetting);

		lampCtrViews[0] = (LinearLayout) findViewById(R.id.lampcontrol1);
		Log.e("debug", "lampCtrViews[0] is null ="+(lampCtrViews[0]==null));
		lampCtrViews[1] = (LinearLayout) findViewById(R.id.lampcontrol2);
		
		for( int i=0;i<Num;i++){
			btnPowers[i]=(Button)lampCtrViews[i].findViewById(R.id.btnPower);
			tvPowers[i]=(TextView)lampCtrViews[i].findViewById(R.id.tvPower);
			tvBtnNames[i]=(TextView)lampCtrViews[i].findViewById(R.id.tv_title_name);
			tvBtnNames[i].setText("开关键"+(i+1));
			final int id=i;
			//亮度
			sbBrightenesss[i] = (SeekBar) lampCtrViews[i].findViewById(R.id.sbBrighteness);
			sbBrightenesss[i].setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					bringhtness_nums[id] = seekBar.getProgress();
					cBrightness( device, seekBar.getProgress(),id);				
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {

				}
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

				}
			});
		}

	}

	
	private void initEvent() {
		ivBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		ivSetting.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startDeviceSettingActivity();
			}
		});
		
		for(int i=0;i<Num;i++){
			final int id=i;
			btnPowers[id].setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					btnPowerAction(id);
				}
			});
		}
		

	}

	private void initDevice() {
		Intent intent = getIntent();
		GizWifiDevice dev = (GizWifiDevice) intent.getParcelableExtra("GizWifiDevice");
		if (dev != null) {
			device = dev;
			device.setListener(gizWifiDeviceListener);
		} else {
			toastDeviceNoReadyAndExit();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
//		switch (v.getId()) {
//		case R.id.iv_back:
//			finish();	
//			break;
//		case R.id.ivSetting:
//			startDeviceSettingActivity();
//			break;
//		case R.id.btnPower:
//			btnPowerAction();
//			break;
//		default:
//			break;
//		}
	}

	private void startDeviceSettingActivity() {
		startActivity(new Intent(this, ComDeviceMoreActivity.class));
	}

	
	private void btnPowerAction(int id){
		Log.e("debug", "btn power action id="+id);
		isPowerOns[id]=!isPowerOns[id];
		Log.e("debug","powerons id="+id+" ison="+isPowerOns[id]);
		Log.e("debug", "key=switch"+id+" isPower="+isPowerOns[id]);
		sendCommand("switch"+id, isPowerOns[id]);
		
		for(int i=0;i<Num;i++){
			sendCommand("switch"+i, isPowerOns[i]);
		}
	}
	
	
//	/* 开关*/
//	private void btnPowerAction() {
//		sendCommand("Power_Switch", !isPowerOn0);		
//		isPowerOn0 =!isPowerOn0;		
//		if (!isPowerOn0 == true) {
//			btnPowerOff();
//		} else {
//			ivPowerOff.setVisibility(View.INVISIBLE);
//			rl_middle.setVisibility(View.VISIBLE);
//			//			ivmain.setVisibility(View.INVISIBLE);
//		}
//	}



	/* 亮度*/
	public void cBrightness( GizWifiDevice device, int bringhtness_num,int id) {
		sendCommand("brightness"+id, bringhtness_num);	
	}	

	public void sendMoreCommand(String key, Object value,String key1, Object value1,String key2, Object value2) {
		//		int sn = 5; // 如果App不使用sn，此处可写成 int sn = 0;
		int sn = 0;
		ConcurrentHashMap<String, Object> command = new ConcurrentHashMap<String, Object>();
		command.put(key, value);
		command.put(key1, value1);
		command.put(key2, value2);		
		device.write(command, sn);
	}

//	private void btnPowerOff() {
//		ivmain.setVisibility(View.VISIBLE);
//		rl_top.setVisibility(View.VISIBLE);
//		powerOff();
//	}
//
//	private void powerOff() {		
//		Bitmap mBitmap = Bitmap.createBitmap(rl_middle.getWidth(), rl_middle.getHeight(), Config.ARGB_8888);
//		Canvas canvas = new Canvas(mBitmap);
//		rl_middle.draw(canvas);
//		ivPowerOff.setVisibility(View.VISIBLE);
//		ivPowerOff.setImageBitmap(mBitmap);
//		rl_middle.setVisibility(View.INVISIBLE);
//
//	}

	@Override
	protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
			ConcurrentHashMap<String, Object> dataMap, int sn) {
		super.didReceiveData(result, device, dataMap, sn);
		if (result == GizWifiErrorCode.GIZ_SDK_SUCCESS) {
			deviceDataMap = dataMap;
			handler.sendEmptyMessage(handler_key.RECEIVED.ordinal());
		}
	}

	@Override
	protected void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
		super.didUpdateNetStatus(device, netStatus);
		if (device.isSubscribed()) {
			if (netStatus == GizWifiDeviceNetStatus.GizDeviceControlled) {
				handler.removeCallbacks(mRunnable);
				progressDialog.cancel();
				// 后面操作是等待sdk下发查询设备状态命令，收到设备状态后再更新界面
			} else {
				myToast(R.string.device_dropped);
				finish();
			}
		}
	}

	@Override
	protected void getDataFromDateMap() {
		// TODO Auto-generated method stub
		super.getDataFromDateMap();
		// 已定义的设备数据点，有布尔、数值和枚举型数据
		if (deviceDataMap.get("data") != null) {
			ConcurrentHashMap<String, Object> map = (ConcurrentHashMap<String, Object>) deviceDataMap.get("data");

			for (String key : map.keySet()) {
				// 开关0
				Log.e("debug", "key="+key);
				if (key.equals("switch0")) {
					isPowerOns[0] = (Boolean) map.get(key);
				}
				// 开关1
				if (key.equals("switch1")) {
					isPowerOns[1]  = (Boolean) map.get(key);
				}
				
				//亮度0
				if (key.equals("brightness0")) {
					bringhtness_nums[0]= (Integer) map.get(key);
				}
				//亮度1
				if (key.equals("brightness1")) {
					bringhtness_nums[1] = (Integer) map.get(key);
				}
			
			}
		}
	}

}