package com.ubx.scanwedge.service;

/**
 * Created by rocky(xiejifu) on 18-11-13.
 */

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.device.ScanManager;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.media.SoundPool;
import android.os.IBinder;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;

import com.ubx.database.helper.UConstants;
import com.ubx.database.helper.USettings;
import com.ubx.propertyparser.Category;
import com.ubx.propertyparser.Property;
import com.ubx.propertyparser.SettingProviderHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanWedgeCoreImpl {
    public static String TAG = "Wedge" + ScanManagerCore.class.getSimpleName();
    private ContentResolver mContentResolver;
    private Context mContext;
    private static final int MESSAGE_DELAYED_RESET_SCANNER = 2;
    private static final int MESSAGE_CONFIG_CHANGED = 3;
    private static final int MESSAGE_SEND_KEYEVENT = 4;
    private static final int MESSAGE_SOFT_DECODE = 5;
    private static final int MESSAGE_RELEASE_WAKELOCK = 6;
    private static final int MESSAGE_DELAYED_CONFIG_SCANNER = 7;
    private Object syncObject = new Object();
    private SparseArray<String> mHashMap = new SparseArray<String>();
    private int currentProfileId = USettings.Profile.DEFAULT_ID;
    private PowerManager.WakeLock mWakeLock;
    private boolean isDataWedgeEnable = false;
    private boolean isDataWedgeLoggingEnable = false;
    private SettingProviderHelper mSettingProviderHelper;
    private SymbolIntentReceiver symbolIntentReceiver;
    private ForegroundActivityReceiver mForegroundActivityReceiver;
    private ScanWedgeApplication mApplication;
    private ArrayList<ExcludedApp> mExcludedAppList = new ArrayList<ExcludedApp>();
    private boolean ignoreActivity(String pkg, String act) {
        Iterator<ExcludedApp> iterator = this.mExcludedAppList.iterator();
        while (iterator.hasNext()) {
            ExcludedApp excludedApp = iterator.next();
            if (excludedApp.packageName.equals(pkg) && excludedApp.activityName.equals(act)) {
                return true;
            }
        }
        Log.d(TAG, "Ignoring the activity change.");
        return false;
    }
    private void createExcludedAppList() {
        mExcludedAppList.clear();
        mExcludedAppList.add(new ExcludedApp("org.codeaurora.snapcam", "com.android.camera.CameraActivity"));
        mExcludedAppList.add(new ExcludedApp("com.android.packageinstaller", "com.android.packageinstaller.permission.ui.GrantPermissionsActivity"));
        mExcludedAppList.add(new ExcludedApp("com.android.systemui", "com.android.systemui.recents.RecentsActivity"));
        mExcludedAppList.add(new ExcludedApp("com.android.settings", "com.android.settings.bluetooth.RequestPermissionActivity"));
        mExcludedAppList.add(new ExcludedApp("com.android.settings", "com.android.settings.bluetooth.RequestPermissionHelperActivity"));
        mExcludedAppList.add(new ExcludedApp("com.android.providers.media", "com.android.providers.media.RingtonePickerActivity"));
    }
    private class ExcludedApp {
        String packageName = null;
        String activityName = null;
        public ExcludedApp(String pkg, String act) {
            packageName = pkg;
            activityName = act;
        }
    }
    private final class SymbolIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (IntentConstants.API_ACTION.equals(action)) {
            }
        }
    }

    private void registerSymbolIntentReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(IntentConstants.API_ACTION);
        mContext.registerReceiver(symbolIntentReceiver, filter);
    }

    private final class PowerChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*int power = USettings.System.getInt(mContentResolver, Settings.System.SCANNER_ENABLE, 0);
            android.util.Log.d(TAG, "onReceive --- " + intent.getAction() + power + ",bootCompleted:" + bootCompleted + ",mScanning:" + mScanning);
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                if (mWakeLock == null || !mScanning) {
                    if (power == 1)
                        rePowerSet(1);
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                if (mWakeLock == null || !mScanning) {
                    if (power == 1)
                        rePowerSet(0);
                }
            }*/
        }
    }

    private final class ForegroundActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.ubx.FOREGROUND_ACTIVITY_CHANGED".equals(action) || "action.NEW_STARTPROCESS_APP".equals(action)) {
                String packageName = intent.getStringExtra("packageName");
                String className = intent.getStringExtra("getClassName");
                int userId = intent.getIntExtra("userId", 1000);
                Log.d(TAG, "onReceive --packageName- " + packageName + " userId:" + userId + " className " + className);
                if (!TextUtils.isEmpty(packageName)/* && checkToptaskAPPFromPackageName(packageName)*/) {
                    if(!ignoreActivity(packageName, className)){
                        loadPropertiesToForegroundAPP(packageName, className);
                    }
                }
            } else if ("action.SYNC_SCANWEDGE_IMPORT_CONFIG".equals(action)) {
                Log.d(TAG, "onReceive --SYNC_SCANWEDGE_IMPORT_CONFIG- ");
                //loadImportProperties();
            }
        }
    }

    private boolean checkToptaskAPPFromPackageName(String topPackageName) {
        PackageManager packageManager = mContext.getPackageManager();
        final ActivityManager manager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = manager.getRunningTasks(1);
        if ((taskList != null)
                && (taskList.get(0) != null)
                && (taskList.get(0).topActivity != null)) {
            final ComponentName cn = taskList.get(0).topActivity;
            String packageName = cn.getPackageName();
            Log.e(TAG, "getRunningTasks packageName " + packageName);
            if (packageName != null)
                return (packageName.equals(topPackageName) || packageName.startsWith(topPackageName));
        }
        return false;
    }

    class DWSettingsObserver extends ContentObserver {
        DWSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(USettings.DW.getUriFor(UConstants.DW_ENABLED), false, this);
            resolver.registerContentObserver(USettings.DW.getUriFor(UConstants.DW_LOGS_ENABLED), false, this);
            updateDWSettings();
        }

        @Override
        public void onChange(boolean selfChange) {
            updateDWSettings();
        }
    }
    private void updateDWSettings() {
        isDataWedgeEnable = USettings.DW.getString(mContext.getContentResolver(), UConstants.DW_ENABLED, "false").equals("true");
        if(!isDataWedgeEnable) {
            //updateDockButtonStatus(false);
        } else {
            /*boolean profileEnable = USettings.Profile.isProfileEnable(mContext.getContentResolver(), USettings.Profile.DEFAULT_ID);
            int currentProfileIdEnable = USettings.System.getInt(mContext.getContentResolver(), USettings.Profile.DEFAULT_ID, Settings.System.SCANNER_ENABLE, 0);
            if(profileEnable && currentProfileIdEnable == 1 ){
                updateDockButtonStatus(true);
            }*/
        }
        isDataWedgeLoggingEnable = USettings.DW.getString(mContext.getContentResolver(), UConstants.DW_LOGS_ENABLED, "false").equals("true");
        mApplication.enableDataWedgeLogging(isDataWedgeLoggingEnable);
        Log.e(TAG, "updateDWSettings isDataWedgeEnable " + isDataWedgeEnable + " isDataWedgeLoggingEnable:" + isDataWedgeLoggingEnable);
    }

    private WorkerHandler mHandler;
    private WorkerThread mThread;
    private final CountDownLatch mInitializedLatch = new CountDownLatch(1);

    private class WorkerHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_CONFIG_CHANGED:
                    int value = msg.arg1;
                    String name = (String) msg.obj;
                    String key = "Settings.System." + name;
                    //USettings.System.putInt(mContentResolver, key, value);
                    break;
                case MESSAGE_SEND_KEYEVENT:
                    int keycode = msg.arg1;
                    break;
                case MESSAGE_SOFT_DECODE:
                    int state = msg.arg1;
                    break;
                case MESSAGE_DELAYED_RESET_SCANNER:

                    break;
                case MESSAGE_DELAYED_CONFIG_SCANNER:
                    break;
                case MESSAGE_RELEASE_WAKELOCK:
                    int lock = msg.arg1;
                    if (lock == 1)
                        mWakeLock.acquire();
                    else
                        mWakeLock.release();
                    break;
            }
        }
    }

    private final class WorkerThread extends Thread {
        public WorkerThread() {
            super("WorkerThread");
        }

        public void run() {
            Looper.prepare();
            mHandler = new WorkerHandler();
            // signal when we are initialized and ready to go
            mInitializedLatch.countDown();
            Looper.loop();
        }
    }

    private ScanWedgeCoreImpl(ScanWedgeApplication application) {
        Log.i(TAG, "ScanWedgeCoreImpl......");
        mApplication = application;
        mContext = mApplication.getApplicationContext();
        propertyPopulate();
        mContentResolver = mContext.getContentResolver();
        mForegroundActivityReceiver = new ForegroundActivityReceiver();
        symbolIntentReceiver = new SymbolIntentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        mContext.registerReceiver(new PowerChangeReceiver(), filter);
        IntentFilter procfilter = new IntentFilter();
        procfilter.addAction("action.NEW_STARTPROCESS_APP");
        procfilter.addAction("com.ubx.FOREGROUND_ACTIVITY_CHANGED");//ForegroundActivityChanged
        procfilter.addAction("action.SYNC_SCANWEDGE_IMPORT_CONFIG");
        mContext.registerReceiver(mForegroundActivityReceiver, procfilter);
        createExcludedAppList();
        mThread = new WorkerThread();
        mThread.start();
        while (true) {
            try {
                mInitializedLatch.await();
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        DWSettingsObserver dwSettingsObserver = new DWSettingsObserver(mHandler);
        dwSettingsObserver.observe();

        mSettingProviderHelper = SettingProviderHelper.getSingleton(mContext);
        mSettingProviderHelper.initScannerProviderProperty();

    }

    public IBinder onBind(Intent intent) {
        if (intent != null && intent.getComponent() != null)
            Log.e(TAG, " onBind  packagename: " + intent.getComponent().getPackageName());
        return null;
    }

    public boolean onUnbind(Intent intent) {
        Log.d(TAG, " onUnbind ");
        return true;
    }

    private static ScanWedgeCoreImpl mScanWedgeCore = null;

    public static ScanWedgeCoreImpl getInstance(ScanWedgeApplication context, String version) {
        Log.d(TAG, " getInstance ");
        if (mScanWedgeCore == null) {
            mScanWedgeCore = new ScanWedgeCoreImpl(context);
        }
        return mScanWedgeCore;
    }

    public Context getContext() {
        return mContext;
    }

    public void destroyInstance() {
        Log.d(TAG, " destroyInstance ");
    }
    private void propertyPopulate() {
        Field[] keyFields = PropertyID.class.getDeclaredFields();
        //HashMap<Integer, String> keyMap = new HashMap<Integer, String >();
        String tmpName = null;
        int keyCount = keyFields.length;
        try {
            for (int i = 0; i < keyCount; i++) {
                tmpName = keyFields[i].getName();
                int modi = keyFields[i].getModifiers();
                if (Modifier.isStatic(modi) && Modifier.isPublic(modi)) {
                    mHashMap.put((Integer) keyFields[i].get(null), tmpName);
                }
            }
        } catch (NullPointerException e) {
            Log.w(TAG, "Non-static field : " + tmpName);
        } catch (IllegalArgumentException e1) {
            Log.w(TAG, "Type mismatch : " + tmpName);
        } catch (IllegalAccessException e2) {
            Log.w(TAG, "Non-public field : " + tmpName);
        }
    }

    private static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }


    private void loadPropertiesToForegroundAPP(String packageName, String calssName) {
        if(!isDataWedgeEnable) {
            Log.e(TAG, " isDataWedgeEnable:" + isDataWedgeEnable);
            return;
        }
        long startTime = System.currentTimeMillis();
        int valueInt = -1;
        String profileId = USettings.System.getAppProfileId(mContentResolver, packageName);
        Log.i(TAG, currentProfileId + " currentProfileId valueInt packageName:" + profileId);
        try {
            valueInt = Integer.parseInt(profileId);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        if (currentProfileId == valueInt) {
            return;
        }
        currentProfileId = valueInt;
        //确认ScanWedge已经连接到服务
        if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
            if(mApplication.getService() == null) {
                Log.e(TAG, " scanner service no connected");
                return;
            }
        } else {
            if(mApplication.getIService() == null) {
                Log.e(TAG, " scanner service porxy no connected");
                return;
            }
        }
        //确认当前profiled是使能状态
        boolean profileEnable = USettings.Profile.isProfileEnable(mContentResolver, currentProfileId);
        try {
            if(!profileEnable){
                //updateDockButtonStatus(false);
                if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
                    mApplication.getService().close();
                } else {
                    mApplication.getIService().close();
                }
                Log.e(TAG, " profileEnable:" + profileEnable);
                return;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //确认当前profiled是扫描头使能状态
        int currentProfileIdEnable = USettings.System.getInt(mContentResolver, currentProfileId, Settings.System.SCANNER_ENABLE, 0);
        try {
            if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
                if (currentProfileIdEnable == 1) {
                    mApplication.getService().open();
                } else {
                    //updateDockButtonStatus(false);
                    mApplication.getService().close();
                    Log.i(TAG, " currentProfileId:" + currentProfileId + " packageName:" + packageName + " close scanner");
                    return;
                }
            } else {
                if (currentProfileIdEnable == 1) {
                    mApplication.getIService().open();
                } else {
                    //updateDockButtonStatus(false);
                    mApplication.getIService().close();
                    Log.i(TAG, " currentProfileId:" + currentProfileId + " packageName:" + packageName + " close scanner");
                    return;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        synchronized (syncObject) {
            int[] id = new int[1];
            int[] value = new int[1];
            int[] id_bad_buffer = new int[1];
            int count = 0;
            try {
                Cursor cursor = mSettingProviderHelper.getPropertyForProfile(currentProfileId);
                long midTime = System.currentTimeMillis();
                Log.i(TAG, " database read time: " + (midTime - startTime));
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        int propId =cursor.getInt(0);
                        String val = cursor.getString(2);
                        String valueType = cursor.getString(3);
                        String category = cursor.getString(4);
                        if(TextUtils.isEmpty(valueType) || TextUtils.isEmpty(category)) {
                            //旧版本os未初始化这两个属性
                            if(mSettingProviderHelper.isStringProperty(propId)) {
                                try {
                                    if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
                                        mApplication.getService().setPropertyString(propId, val);
                                    } else {
                                        mApplication.getIService().setPropertyString(propId, val);
                                    }
                                } catch (Exception e) {
                                }
                                mSettingProviderHelper.setStringProperty(propId, val);
                                continue;
                            }
                        }
                        if (Category.VAL_STRING.equals(valueType)) {
                            try {
                                if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
                                    mApplication.getService().setPropertyString(propId, val);
                                } else {
                                    mApplication.getIService().setPropertyString(propId, val);
                                }
                            } catch (Exception e) {
                            }
                            mSettingProviderHelper.setStringProperty(propId, val);
                        } else {
                            id[0] = propId;
                            try {
                                value[0] = Integer.parseInt(val);
                            } catch (NumberFormatException e) {
                                value[0] = 0;
                            }
                            if(value[0] == mSettingProviderHelper.getIntProperty(propId)) {
                                continue;
                            }
                            Log.i(TAG, " updateScan service id" + (id[0] +" = " +value[0]));
                            try {
                                if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
                                    mApplication.getService().setPropertyInts(id, 1, value, 1,
                                            id_bad_buffer);
                                } else {
                                    id_bad_buffer[0] = -1;//Scan wedge配置参数全部设置到扫描头中
                                    mApplication.getIService().setPropertyInts(id, 1, value, 1,
                                            id_bad_buffer);
                                }
                            } catch (Exception e) {
                            }
                            mSettingProviderHelper.setIntProperty(propId, value[0]);
                            count++;
                        }
                    }
                    cursor.close();
                    cursor = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean enableDockButton = USettings.System.getInt(mContentResolver, currentProfileId, Settings.System.SUSPENSION_BUTTON, 0) == 1;
            //updateDockButtonStatus(enableDockButton);
            Log.i(TAG, " reload profile end currentProfileId:" + currentProfileId + " currentProfileIdEnable: " + currentProfileIdEnable + " enableDockButton:" + enableDockButton + " count " + count);
            long endTime = System.currentTimeMillis();
            Log.i(TAG, " updateScan service time: " + (endTime - startTime));
        }
    }

    /**
     * 更新浮动按钮状态
     * @param enable
     */
    private void updateDockButtonStatus(boolean enable) {
        if (enable) {
            //关闭旧版本上控制的浮动按钮
            /*try {
                Intent mIntent = new Intent();
                mIntent.setComponent(new ComponentName("com.android.usettings", "com.android.usettings.FxService"));
                mContext.stopService(mIntent);
            } catch (Exception e) {
            }*/
            try {
                Intent intentService = new Intent("com.ubx.barcode.action.SUSPENSION_BUTTON");
                Intent eintent = new Intent(getExplicitIntent(mContext, intentService));
                mContext.startService(eintent);
            } catch (Exception e) {
                Log.e(TAG,
                        "Start DeviceBackendService failed:" + e.getMessage());
            }
        } else {
            //关闭旧版本上控制的浮动按钮
            /*try {
                Intent mIntent = new Intent();
                mIntent.setComponent(new ComponentName("com.android.usettings", "com.android.usettings.FxService"));
                mContext.stopService(mIntent);
            } catch (Exception e) {
            }*/
            try {
                Intent intentService = new Intent("com.ubx.barcode.action.SUSPENSION_BUTTON");
                Intent eintent = new Intent(getExplicitIntent(mContext, intentService));
                mContext.stopService(eintent);
            } catch (Exception e) {
                Log.e(TAG,
                        "Start DeviceBackendService failed:" + e.getMessage());
            }
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        if (s.length() < 2)
            return null;
        int len = s.length();
        byte[] data = new byte[len / 2];
        try {
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
            }
        } catch (Exception e) {
            Log.d("debug", "Argument(s) for hexStringToByteArray(String s)"
                    + "was not a hex string");
        }
        return data;
    }

    private byte[] StrToHexByte(String str) {
        if (str == null)
            return null;
        else if (str.length() < 2)
            return null;
        else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }
}
