package com.ubx.appinstall.util;

import android.util.Log;

public class ULog {
	
	public static final void d(String msg) {
		if(AutoInstallConfig.DEBUG) {
			Log.d(AutoInstallConfig.TAG, msg);
		}
	}
	
	public static final void i(String msg) {
		if(AutoInstallConfig.DEBUG) {
			Log.i(AutoInstallConfig.TAG, msg);
		}
	}

}
