package com.android.settings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

/*** yangkun add for Docking station 2020/09/03 ***/

public class DockManager {
	private Context mContext;
	private SharedPreferences preferences;
	private ContentResolver mContentResolver;

	public DockManager(Context context){
		mContext = context;
		preferences = mContext.getSharedPreferences("dock", Context.MODE_PRIVATE);
		mContentResolver = mContext.getContentResolver();
	}

	public void setEnabled(boolean enabled) {
		FileOutputStream outputStream = null;
		try {
			if(enabled && Build.PROJECT.equals("SQ51") && isScanHandleEnable()){
				FileOutputStream os =new FileOutputStream("/sys/devices/soc.0/gpio_keys.63/pogo_scankey_enable");
				os.write(Integer.toString(0).getBytes());
				os.close();
				preferences.edit().putBoolean("scanhandle_enabled", false).commit();
			}
			outputStream = new FileOutputStream("/sys/devices/virtual/Usb_switch/usbswitch/function_otg_en");
			outputStream.write(Integer.toString(enabled ? 2 : 0).getBytes());
			preferences.edit().putBoolean("enabled", enabled).commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isEnable() {
		FileInputStream inputStream = null;
		boolean result = false;
		try {
			byte[] buffer = new byte[Integer.toString(0).getBytes().length];
			inputStream = new FileInputStream("/sys/devices/soc/78db000.usb/otg_enable");
			inputStream.read(buffer);
			if("1".equals(new String(buffer)))
					result = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public void setScanHandleEnabled(boolean enabled) {
		FileOutputStream outputStream = null;
		try {
			if(enabled && Build.PROJECT.equals("SQ51") && isEnable()){
				FileOutputStream os =new FileOutputStream("/sys/devices/soc.0/78d9000.usb/otg_enable");
				os.write(Integer.toString(0).getBytes());
				os.close();
				preferences.edit().putBoolean("enabled", false).commit();
			}

			outputStream = new FileOutputStream("/sys/devices/soc.0/gpio_keys.63/pogo_scankey_enable");
			outputStream.write(Integer.toString(enabled ? 1 : 0).getBytes());
			preferences.edit().putBoolean("scanhandle_enabled", enabled).commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public boolean isScanHandleEnable() {
		FileInputStream inputStream = null;
		boolean result = false;
		try {
			byte[] buffer = new byte[Integer.toString(0).getBytes().length];
			inputStream = new FileInputStream("/sys/devices/soc.0/gpio_keys.63/pogo_scankey_enable");
			inputStream.read(buffer);
			if("1".equals(new String(buffer)))
				result = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
                                                                                                                                                                                               