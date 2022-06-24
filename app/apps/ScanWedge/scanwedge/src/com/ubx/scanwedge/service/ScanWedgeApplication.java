package com.ubx.scanwedge.service;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.ubx.database.helper.UConstants;
import com.ubx.database.helper.USettings;
import com.ubx.propertyparser.SettingProviderHelper;
import com.ubx.scanwedge.service.ServiceConnectionProxy.OnServiceConnectedListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import android.os.IScanServiceWrapper;
import android.os.IScanService;

/**
 * Created by rocky on 18-11-12.
 */

public class ScanWedgeApplication extends Application {
    private static String TAG = ScanWedgeApplication.class.getSimpleName();
    public static boolean DEVDEBUG = false;
    public static boolean ENABLE_BINDER_SERVICE = false;
    private ScanWedgeCoreImpl mScanWedgeCoreImpl;
    private ServiceConnectionProxy mServiceConnectionProxy;
    private SettingProviderHelper mSettingProviderHelper;
    //private ScanManager mScanManager;
    private String mVersion = "1.0.1";    //default value
    private String pckName = "";
    private ContentResolver mContentResolver;
    public static Context statContext = null;
    /**
     * Android 7.1 and above version device
     */
    private IScanServiceWrapper mService = null;
    /**
     * Android 5.1 devices
     */
    private IScanService mScanService;
    private boolean isConnect = false;
    private Intent scanIntent = new Intent();

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate  myPid: " + android.os.Process.myPid() + " Binder.getCallingPid: " + Binder.getCallingPid());
        //AppCrashHandler.getInstance().init(this, pckName, mVersion);
        mContentResolver = getContentResolver();
        super.onCreate();
        scanIntent.setClassName("com.android.usettings", "com.android.server.ScanService");
        mServiceConnectionProxy = ServiceConnectionProxy.createInstance(this);
        //mServiceConnectionProxy.connect(scanIntent, myOnServiceConnectedListener);
        mSettingProviderHelper = SettingProviderHelper.getSingleton(this);
    }

    public IScanServiceWrapper getService() {
        if (mService == null) {
            mServiceConnectionProxy.connect(scanIntent, myOnServiceConnectedListener);
        }
        return mService;
    }

    public void getServiceProxyWrapper() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && ENABLE_BINDER_SERVICE) {
            if (mService == null) {
                mServiceConnectionProxy.connect(scanIntent, myOnServiceConnectedListener);
            }
        } else {
            if (mScanService == null) {
                mScanService = IScanService.Stub.asInterface(ServiceManager
                        .getService("scan"));
            }
        }
    }

    public IScanService getIService() {
        if (mScanService == null) {
            mScanService = IScanService.Stub.asInterface(ServiceManager
                    .getService("scan"));
        }
        return mScanService;
    }

    public void disconnect() {
        if (mServiceConnectionProxy != null) {
            if (mService != null) {
                mServiceConnectionProxy.disconnect();
            } else {
                Log.e(TAG, "disconnect mService is null");
            }
        }
        mService = null;
    }

    /*public ScanManager getScanManager() {
        if (mScanManager == null) {
            mScanManager = new ScanManager();
        }
        return mScanManager;
    }*/
    public ScanWedgeCoreImpl getScanWedgeCoreImpl() {
        if (mScanWedgeCoreImpl == null) {
            mScanWedgeCoreImpl = ScanWedgeCoreImpl.getInstance(this, mVersion);
        }
        return mScanWedgeCoreImpl;
    }

    public String getAPPVersion() {
        try {
            PackageManager pm = getPackageManager();
            PackageInfo pinfo = pm.getPackageInfo(getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            mVersion = pinfo.versionName;
            pckName = pinfo.packageName;
        } catch (Exception e) {
            Log.d(TAG, "ERROR" + TAG + "getVersionName failed:" + e.getMessage());
        }
        return mVersion;
    }

    public String getScannerFrameworkVersion() {
        String packageName = "com.android.usettings";
        try {
            PackageManager pm = this.getPackageManager();
            ApplicationInfo app = pm.getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if (app != null) {
                PackageInfo packInfo = pm.getPackageInfo(packageName, 0);
                String version = packInfo.versionName;
                return version;
            }
        } catch (PackageManager.NameNotFoundException e) {
            // Will never happen.
        }
        return null;
    }

    public void enableDataWedgeLogging(boolean enable) {
        DEVDEBUG = enable;
    }

    public void resetScannerParameters(final int profileId) {
        try {
            if (profileId == USettings.Profile.DEFAULT_ID) {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && ENABLE_BINDER_SERVICE) {
                    if (getService() != null) {
                        getService().setDefaults();
                    }
                } else {
                    if (getIService() != null) {
                        getIService().setDefaults();
                    }
                }
            } else {
                USettings.AppList.deleteAssociatedApps(mContentResolver, profileId);
            }
        } catch (RemoteException e) {
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                USettings.System.initSettings(getApplicationContext(), profileId);
                long end = System.currentTimeMillis();
                Log.d(TAG, "setDefaultProperty db time=" + (end - start) + "ms");
            }
        }).start();
    }

    public int getScannerType() {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && ENABLE_BINDER_SERVICE) {
                if (getService() != null) {
                    return getService().readConfig("SCANER_TYPE");
                }
            } else {
                if (getIService() != null) {
                    return getIService().readConfig("SCANER_TYPE");
                }
            }
        } catch (RemoteException e) {
        }
        return 0;
    }

    /**
     * @param profileId
     * @param propertyName
     * @param defValue
     * @return
     */
    public int getPropertyInt(int profileId, String propertyName, int defValue) {
        if (mContentResolver == null) {
            mContentResolver = getContentResolver();
        }
        return USettings.System.getInt(mContentResolver, profileId, propertyName, defValue);
    }

    public void setPropertyInt(int profileId, int property, String propertyName, int value) {
        if (mContentResolver == null) {
            mContentResolver = getContentResolver();
        }
        USettings.System.putInt(mContentResolver, profileId, property, propertyName, value);
        if (profileId == USettings.Profile.DEFAULT_ID && property != 0) {
            int[] index = new int[1];
            int[] valuebuf = new int[1];
            index[0] = property;
            valuebuf[0] = value;
            int[] id_bad_buffer = new int[1];
            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && ENABLE_BINDER_SERVICE) {
                    if (getService() != null) {
                        if (getService() != null) {
                            int error = getService().setPropertyInts(index, 1, valuebuf, 1,
                                    id_bad_buffer);
                            if (error > 0) {
                                Log.e(TAG, "Decoder error: in setParameterInts");
                            }
                        }
                    }
                } else {
                    if (getIService() != null) {
                        if (getIService() != null) {
                            int error = getIService().setPropertyInts(index, 1, valuebuf, 1,
                                    id_bad_buffer);
                            if (error > 0) {
                                Log.e(TAG, "Decoder error: in setParameterInts");
                            }
                        }
                    }
                }
            } catch (RemoteException ex) {
                Log.e(TAG, "Decoder error: in setParameterInts");
            }
            mSettingProviderHelper.setIntProperty(property, value);
        }
    }

    public String getPropertyString(int profileId, String propertyName, String defValue) {
        if (mContentResolver == null) {
            mContentResolver = getContentResolver();
        }
        return USettings.System.getString(mContentResolver, profileId, propertyName);
    }

    public void setPropertyString(int profileId, int property, String propertyName, String value) {
        if (mContentResolver == null) {
            mContentResolver = getContentResolver();
        }
        USettings.System.putString(mContentResolver, profileId, property, propertyName, value);
        if (profileId == USettings.Profile.DEFAULT_ID) {
            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && ENABLE_BINDER_SERVICE) {
                    if (getService() != null) {
                        getService().setPropertyString(property, value);
                    }
                } else {
                    if (getIService() != null) {
                        getIService().setPropertyString(property, value);
                    }
                }
            } catch (RemoteException e) {
            }
            mSettingProviderHelper.setStringProperty(property, value);
        }
    }

    public void getPropertyInts(int profileId, String[] propertyName, int[] values) {
        if (mContentResolver == null) {
            mContentResolver = getContentResolver();
        }
        for (int i = 0; i < propertyName.length; ++i) {
            values[i] = USettings.System.getInt(mContentResolver, profileId, propertyName[i], -1);
        }
    }

    public void setPropertyInts(int profileId, int[] ids, String[] propertyName, int[] values) {
        if (mContentResolver == null) {
            mContentResolver = getContentResolver();
        }
        if (profileId == USettings.Profile.DEFAULT_ID) {
            int idBuffLen = ids.length;
            int valueBuffLen = values.length;
            int[] id_bad_buffer = new int[idBuffLen];

            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && ENABLE_BINDER_SERVICE) {
                    if (getService() != null) {
                        int error = getService().setPropertyInts(ids, idBuffLen, values, valueBuffLen,
                                id_bad_buffer);
                        if (error > 0) {
                            Log.e(TAG, "Decoder error: in setParameterInts");
                        }
                    }
                } else {
                    if (getIService() != null) {
                        int error = getIService().setPropertyInts(ids, idBuffLen, values, valueBuffLen,
                                id_bad_buffer);
                        if (error > 0) {
                            Log.e(TAG, "Decoder error: in setParameterInts");
                        }
                    }
                }
            } catch (RemoteException ex) {
                Log.e(TAG, "Decoder error: in setParameterInts");
            }
        }
        for (int i = 0; i < values.length; ++i) {
            USettings.System.putInt(mContentResolver, profileId, ids[i], propertyName[i], values[i]);
            if (profileId == USettings.Profile.DEFAULT_ID) {
                mSettingProviderHelper.setIntProperty(ids[i], values[i]);
            }
        }
    }

    public void setPropertyStrings(int profileId, int[] ids, String[] propertyName, String[] values) {
        if (mContentResolver == null) {
            mContentResolver = getContentResolver();
        }
        int minLen = ids.length > values.length ? values.length : ids.length;
        if (profileId == USettings.Profile.DEFAULT_ID) {
            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && ENABLE_BINDER_SERVICE) {
                    if (getService() != null) {
                        for (int i = 0; i < minLen; i++) {
                            getService().setPropertyString(ids[i], values[i]);
                        }
                    }
                } else {
                    if (getIService() != null) {
                        for (int i = 0; i < minLen; i++) {
                            getIService().setPropertyString(ids[i], values[i]);
                        }
                    }
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Decoder error: in setPropertyString");
            }
        }
        for (int i = 0; i < minLen; ++i) {
            USettings.System.putString(mContentResolver, profileId, ids[i], propertyName[i], values[i]);
            if (profileId == USettings.Profile.DEFAULT_ID) {
                mSettingProviderHelper.setStringProperty(ids[i], values[i]);
            }
        }
    }

    public boolean isProfileEnable(int currentProfileId) {
        return USettings.Profile.isProfileEnable(mContentResolver, currentProfileId);
    }

    public boolean isDataWedgeEnable() {
        boolean isDataWedgeEnable = USettings.DW.getString(mContentResolver, UConstants.DW_ENABLED, "false").equals("true");
        return isDataWedgeEnable;
    }

    public void setScanHandleEnabled(boolean enabled) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("/sys/devices/soc/qpnp-smbcharger-17/usbid_scankey");
            outputStream.write(Integer.toString(enabled ? 1 : 0).getBytes());
            outputStream.flush();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.w(TAG, "setScanHandleEnabled() set ScanHandle status failed!" + e);
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

    public boolean supportHandle() {
        File file = new File("/sys/devices/soc/qpnp-smbcharger-17/usbid_scankey");
        return file.exists();
    }

    public boolean isScanHandleEnable() {
        FileInputStream inputStream = null;
        boolean result = false;
        try {
            byte[] buffer = new byte[Integer.toString(0).getBytes().length];
            inputStream = new FileInputStream("/sys/devices/soc/qpnp-smbcharger-17/usbid_scankey");
            inputStream.read(buffer);
            if ("1".equals(new String(buffer)))
                result = true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.w(TAG, "isScanHandleEnable() get ScanHandle status failed!" + e);
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

    @Override
    public void onTerminate() {
        Log.d(TAG, "DeviceServerApk process will be terminated");
        if (mScanWedgeCoreImpl != null) {
            mScanWedgeCoreImpl.destroyInstance();
            mScanWedgeCoreImpl = null;
        }
        mServiceConnectionProxy.destroy();
        mService = null;
        isConnect = false;
        mServiceConnectionProxy = null;
        super.onTerminate();
    }

    public boolean isServiceConnetcted() {
        return isConnect;
    }

    OnServiceConnectedListener myOnServiceConnectedListener = new OnServiceConnectedListener() {
        @Override
        public int serviceEventNotify(
                int event, IScanServiceWrapper service) {
            if (event == EVENT_SERVICE_CONNECTED) {
                Log.e(TAG,
                        "Service is connect successed...");
                isConnect = true;
                mService = service;
                try {
                    Map<String, Integer> list = mService.getScanerList();
                    if (list != null) {
                        for (String key : list.keySet()) {
                            Log.i(TAG, "key:" + key + ",value:" + list.get(key));
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (event == EVENT_SERVICE_VERSION_NOT_COMPATABLE) {

            } else if (event == EVENT_SERVICE_DISCONNECTED) {
                Log.e(TAG,
                        "Service is disconnected...");
                isConnect = false;
                mService = null;
            }
            return 0;
        }
    };
}
