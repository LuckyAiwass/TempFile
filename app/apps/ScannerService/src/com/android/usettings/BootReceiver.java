package com.android.usettings;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.ContentResolver;
import com.android.usettings.ScannerSettings;
import android.util.Log;
import android.os.Build;
import java.io.File;

public class BootReceiver extends BroadcastReceiver{
    private static final String BOOT = "android.intent.action.BOOT_COMPLETED";
    private static final String TAG = "BootReceiver";
    SharedPreferences sp;
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals(BOOT)) {
            if (getHandleStatus(context)) {
                ScannerSettings.setScanHandleEnabled(context, true);
            }
            if(new File("/sdcard/import_scanner_property.xml").exists()){
                Intent mIntent = new Intent("action.IMPORT_SCANNER_CONFIG");
                mIntent.setPackage("com.android.usettings");
                mIntent.putExtra("configFilepath", "/sdcard/import_scanner_property.xml");
                context.sendBroadcast(mIntent);
                Log.d(TAG,"imoort /sdcard/import_scanner_property.xml");
            }
        }
    }

    // urovo add shenpidong begin 2019-09-29
    public boolean getHandleStatus(Context context) {
	if(context == null) {
	    Log.d(TAG, "getHandleStatus Context:" + context);
	    return false;
	}
	ContentResolver mContentResolver = context.getContentResolver();
	// yangkun modify start 2020/04/16
	// int value = android.device.provider.Settings.System.getInt(mContentResolver, android.device.provider.Settings.System.SCAN_HANDLE, 0);
	int value;
	if (Build.PWV_CUSTOM_CUSTOM.equals("INDIA") || Build.PWV_CUSTOM_CUSTOM.equals("INDIAWO") || Build.PWV_CUSTOM_CUSTOM.equals("WALMART")) {//add INDIAWO by huangruohui on 20200903
		value = android.device.provider.Settings.System.getInt(mContentResolver, android.device.provider.Settings.System.SCAN_HANDLE, 1);
	} else {
		value = android.device.provider.Settings.System.getInt(mContentResolver, android.device.provider.Settings.System.SCAN_HANDLE, 0);
	}
	// modify end
	return value == 1;
    }
    // urovo add shenpidong end 2019-09-29
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              