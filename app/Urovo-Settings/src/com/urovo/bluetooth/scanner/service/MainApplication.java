package com.urovo.bluetooth.scanner.service;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;


public class MainApplication extends Application {
    public static String TAG = "BTSCANNER" + MainApplication.class.getSimpleName();
    private String mVersion = "1.0.1";    //default value

    @Override
    public void onCreate() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pinfo = pm.getPackageInfo(getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            mVersion = pinfo.versionName;
        } catch (Exception e) {
            Log.e(TAG, "getVersionName failed:" + e.getMessage());
        }
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        Log.e(TAG, "DeviceServerApk process will be terminated");
        super.onTerminate();
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            