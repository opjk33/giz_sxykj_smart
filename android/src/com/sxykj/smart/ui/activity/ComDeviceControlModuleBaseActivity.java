package com.sxykj.smart.ui.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.gizwits.opensource.appkit.CommonModule.GosBaseActivity;
import com.gizwits.opensource.devicecontrol.date.GosScheduleData;
import com.xxx.giz_sxykj_smart.R;

public class ComDeviceControlModuleBaseActivity  extends GosBaseActivity {

	
	
	/** 全局变量device */
	protected static GizWifiDevice device;
	
	protected static List<GosScheduleData> scheduleDates = new ArrayList<GosScheduleData>();

	private Toast mToast;

	/** 接收到数据后存储的变量 */
	protected ConcurrentHashMap<String, Object> deviceDataMap;
	
	

	private GizWifiSDKListener gizWifiSDKListener = new GizWifiSDKListener() {

		/** 用于设备解绑 */
		public void didUnbindDevice(GizWifiErrorCode result, java.lang.String did) {
			ComDeviceControlModuleBaseActivity.this.didUnbindDevice(result, did);
		}

	};

	/**
	 * 设备监听
	 */
	protected GizWifiDeviceListener gizWifiDeviceListener = new GizWifiDeviceListener() {
		// 用于设备订阅
		@Override
		public void didSetSubscribe(GizWifiErrorCode arg0, GizWifiDevice arg1, boolean arg2) {
			ComDeviceControlModuleBaseActivity.this.didSetSubscribe(arg0, arg1, arg2);
		}

		// 用于接收数据
		public void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
				java.util.concurrent.ConcurrentHashMap<String, Object> dataMap, int sn) {
			ComDeviceControlModuleBaseActivity.this.didReceiveData(result, device, dataMap, sn);
		};

		// 用于获取网络状态
		public void didUpdateNetStatus(GizWifiDevice device,
				com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus netStatus) {
			ComDeviceControlModuleBaseActivity.this.didUpdateNetStatus(device, netStatus);
		};

		// 用于修改设备别名
		public void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
			ComDeviceControlModuleBaseActivity.this.didSetCustomInfo(result, device);
		};
	};

	protected void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device2) {

	}

	protected void didReceiveData(GizWifiErrorCode result, GizWifiDevice device,
			ConcurrentHashMap<String, Object> dataMap, int sn) {
	}

	protected void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {

	}

	protected void didSetSubscribe(GizWifiErrorCode arg0, GizWifiDevice arg1, boolean arg2) {

	}

	protected void didUnbindDevice(GizWifiErrorCode result, String did) {

	}

	protected void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 隐藏ActionBar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 每次返回activity都要注册一次sdk监听器，保证sdk状态能正确回调
		GizWifiSDK.sharedInstance().setListener(gizWifiSDKListener);
		// 每次打开页面都注册一次device监听器，保证能收到设备数据
		if (device != null) {
			device.setListener(gizWifiDeviceListener);
		}
	}

	/*
	 * 返回键处理
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * <p>
	 * Description: 下发控制指令
	 * </p>
	 * 
	 * @param key
	 *            对应的数据点
	 * @param value
	 *            对应数据点的值
	 * @param isShowProgressDialog
	 *            是否弹出提示框，true为弹出
	 */
	protected void sendCommand(String key, Object value) {

		if (device.getNetStatus() != GizWifiDeviceNetStatus.GizDeviceControlled) {
			myToast(R.string.device_no_ready);
			return;
		}
		
//		int sn = 5; // 如果App不使用sn，此处可写成 int sn = 0;
		int sn = 0;
		ConcurrentHashMap<String, Object> command = new ConcurrentHashMap<String, Object>();
		command.put(key, value);
		device.write(command, sn);
	}

	/**
	 * <p>
	 * Description:从设备返回的dataMap中获取数据
	 * </p>
	 * 
	 * @return true 获取成功，false 获取失败，表示接收到数据为空
	 */
	@SuppressWarnings("unchecked")
	protected void getDataFromDateMap() {
					
						
	}

	public void myToast(int rid) {
		// 防止toast重复弹出
		if (mToast != null) {
			mToast.setText(rid);
		} else {
			mToast = Toast.makeText(getApplicationContext(), rid, Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	public void myToast(String string) {
		if (mToast != null) {
			mToast.setText(string);
		} else {
			mToast = Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT);
		}
		mToast.show();
	}

	/**
	 * <p>
	 * Description: 获取设备名称
	 * </p>
	 * 
	 * @return 如果有别名，则返回别名，如果没有别名则返回产品名
	 */
	public String getDeviceName() {
		if (device != null) {
			if (device.getAlias() == null || device.getAlias().length() <= 0) {
				return device.getProductName();
			} else {
				return device.getAlias();
			}
		}
		return null;
	}
	
	public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER = new NumberPicker.Formatter() {
		final StringBuilder mBuilder = new StringBuilder();

		final java.util.Formatter mFmt = new java.util.Formatter(mBuilder, java.util.Locale.US);

		final Object[] mArgs = new Object[1];

		public String format(int value) {
			mArgs[0] = value;
			mBuilder.delete(0, mBuilder.length());
			mFmt.format("%02d", mArgs);
			return mFmt.toString();
		}
	};
	
	protected String wentBackOneDay(String userPick) {
		// 保存的重复周数往前推一天，周二变周一
		StringBuilder result = new StringBuilder();
		if (userPick.contains("mon")) {
			result.append("sun,");
		}
		if (userPick.contains("tue")) {
			result.append("mon,");
		}
		if (userPick.contains("wed")) {
			result.append("tue,");
		}
		if (userPick.contains("thu")) {
			result.append("wed,");
		}
		if (userPick.contains("fri")) {
			result.append("thu,");
		}
		if (userPick.contains("sat")) {
			result.append("fri,");
		}
		if (userPick.contains("sun")) {
			result.append("sat,");
		}
		return result.substring(0, result.length() - 1);
	}
	
	protected String wentFowardOneDay(String userPick) {
		// 保存的重复周数往后推一天，周二变周三
		StringBuilder result = new StringBuilder();
		if (userPick.contains("mon")) {
			result.append("tue,");
		}
		if (userPick.contains("tue")) {
			result.append("wed,");
		}
		if (userPick.contains("wed")) {
			result.append("thu,");
		}
		if (userPick.contains("thu")) {
			result.append("fri,");
		}
		if (userPick.contains("fri")) {
			result.append("sat,");
		}
		if (userPick.contains("sat")) {
			result.append("sun,");
		}
		if (userPick.contains("sun")) {
			result.append("mon,");
		}
		return result.substring(0, result.length() - 1);
	}

}