package com.ubx.appinstall.util;

import java.io.File;
import java.io.IOException;

import android.os.Environment;

public class AutoInstallConfig {
    
    public static final String TAG = "AppInstall";
    public static final boolean DEBUG = true;
    public static final boolean INFO = true;
    public static final String AUTOINSTALLAPPS = "com.autoinstallapps.service";
    public static final String AUTOPATH = "/installapps";
    public static final String AUTOINSTALLPATH = getAutoInstallPath();
    public static final String DOWNLOADPATH = "/downloadapps";
    public static final String APPDOWNLOADPATH = getAppDownloadPath();
    public static final String SDCARD = "/sdcard" + AUTOPATH;
    
    public static String getAutoInstallPath() {
        File file = Environment.getExternalStorageDirectory();
        if(file!=null) {
            try {
                return file.getCanonicalPath() + AUTOPATH;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            ULog.d("getAutoInstallPath , file:" + file);
        }
        return SDCARD;
    }

    public static String getAppDownloadPath() {
        File file = Environment.getExternalStorageDirectory();
        if(file!=null) {
            try {
                return file.getCanonicalPath() + DOWNLOADPATH;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            ULog.d("getAppDownloadPath , file:" + file);
        }
        return SDCARD;
    }
    
    public static String getRootPath(){
        File file = Environment.getExternalStorageDirectory();
        if(file!=null) {
            try {
                return file.getCanonicalPath();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return  "/sdcard" ;
    }

}
