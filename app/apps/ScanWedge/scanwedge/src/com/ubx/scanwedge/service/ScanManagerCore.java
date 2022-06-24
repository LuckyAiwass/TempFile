package com.ubx.scanwedge.service;

/**
 * Created by rocky on 18-11-13.
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

import com.ubx.scanwedge.aidl.IScanWedgeEngine;
import com.ubx.database.helper.UConstants;
import com.ubx.database.helper.USettings;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScanManagerCore {
    public static String TAG = "Wedge" + ScanManagerCore.class.getSimpleName();
    ScanWedgeEngineImpl mScanWedgeEngineImpl;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int scanType;

    private int[] ALL_SUPPORT_PROPERTY_INDEX;
    private boolean bootCompleted = false;
    private SoundPool soundpool = null;
    private int heightBeepId;
    private int middleBeepId;
    private Vibrator mVibrator;
    private boolean mIsScannerOpend = false;
    private boolean mScanning = false;
    /*	private static final int MESSAGE_SET = 0;
        private static final int MESSAGE_CODE_CHANGED = 1;*/
    private static final int MESSAGE_DELAYED_RESET_SCANNER = 2;
    private static final int MESSAGE_CONFIG_CHANGED = 3;
    private static final int MESSAGE_SEND_KEYEVENT = 4;
    private static final int MESSAGE_SOFT_DECODE = 5;
    private static final int MESSAGE_RELEASE_WAKELOCK = 6;
    private static final int MESSAGE_DELAYED_CONFIG_SCANNER = 7;
    private SymbolIntentReceiver symbolIntentReceiver;

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

    private final class startProcessChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("action.NEW_STARTPROCESS_APP".equals(intent.getAction())) {
                String packageName = intent.getStringExtra("packageName");
                String className = intent.getStringExtra("getClassName");
                int userId = intent.getIntExtra("userId", 1000);
                Log.d(TAG, "onReceive --packageName- " + packageName + " userId:" + userId + " className " + className);
                if (!TextUtils.isEmpty(packageName)/* && checkToptaskAPPFromPackageName(packageName)*/) {
                    loadPropertiesForAPP(packageName);
                }
            } else if ("action.SYNC_SCANWEDGE_IMPORT_CONFIG".equals(intent.getAction())) {
                Log.d(TAG, "onReceive --SYNC_SCANWEDGE_IMPORT_CONFIG- ");
                loadImportProperties();
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

    private ScanManager mScanManager = null;
    private static HashMap<String, Integer> mParaSettings;

    private PowerManager.WakeLock mWakeLock;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();

            resolver.registerContentObserver(Settings.System.getUriFor(Settings.System.WEDGE_INTENT_ACTION_NAME), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(Settings.System.WEDGE_INTENT_DATA_STRING_TAG),
                    false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(Settings.System.LASER_ON_TIME), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.SCANNER_TYPE), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.REMOVE_NONPRINT_CHAR), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.LABEL_FORMAT_SEPARATOR_CHAR), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.DEC_OCR_USER_TEMPLATE), false, this);
            updateSettings();
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    private synchronized void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();
        try {
            wedgeIntentAction = Settings.System.getString(resolver, Settings.System.WEDGE_INTENT_ACTION_NAME);
            wedgeIntentLable = Settings.System.getString(resolver, Settings.System.WEDGE_INTENT_DATA_STRING_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (wedgeIntentAction == null) {
            wedgeIntentAction = ScanManager.ACTION_DECODE;
        }
        if (wedgeIntentLable == null) {
            wedgeIntentLable = ScanManager.BARCODE_STRING_TAG;
        }
        try {
            removeNonPrintChar = Settings.System.getInt(resolver, Settings.System.REMOVE_NONPRINT_CHAR, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            separatorChar = Settings.System.getString(resolver, Settings.System.LABEL_FORMAT_SEPARATOR_CHAR);
        } catch (Exception e) {
            e.printStackTrace();
            separatorChar = "";
        }
        try {
            ocrUserTemplate = Settings.System.getString(resolver, Settings.System.DEC_OCR_USER_TEMPLATE);
        } catch (Exception e) {
            e.printStackTrace();
            ocrUserTemplate = "";
        }
        timeOut = Settings.System.getInt(resolver, Settings.System.LASER_ON_TIME, 50);
        timeOut = timeOut * 100;

        //Log.i(TAG, "updateSettings......" + wedgeIntentAction + wedgeIntentLable);
        //int type = ScannerFactory.TYPE_N6603;
        int type = Settings.System.getInt(mContentResolver, Settings.System.SCANNER_TYPE, 0);
        //Log.i(TAG, "updateSettings......scanType:" + scanType + ", type:" + type);

        if (type > 0) {
            Log.i(TAG, "updateSettings......scanType:" + scanType + ", type:" + type);
            if (mScanWedgeEngineImpl == null)
                mScanWedgeEngineImpl = new ScanWedgeEngineImpl(this);
        }
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

    private ScanManagerCore(Context context) {

        Log.i(TAG, "ScanService......");
        mContext = context;
        mScanManager = new ScanManager();
        mParaSettings = new HashMap<String, Integer>();
        /*for (int i = 0; i < ALL_PROPERTY_INDEX.length; i++) {
            mHashMap.put(ALL_PROPERTY_INDEX[i], PROVIDERS_SETTINGS_STRING[i]);
        }*/
        propertyPopulate();
        for (int i = 0; i < NON_PRINTABLE_CHARS.length; i++) {
            nonPrintCharHashmap.put(NON_PRINTABLE_CHARS[i], NON_PRINTABLE_CHARS_VALUES[i]);
        }
        mContentResolver = mContext.getContentResolver();
        symbolIntentReceiver = new SymbolIntentReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        mContext.registerReceiver(new PowerChangeReceiver(), filter);
        IntentFilter procfilter = new IntentFilter();
        procfilter.addAction("action.NEW_STARTPROCESS_APP");
        //mContext.registerReceiver(new startProcessChangeReceiver(), procfilter, "permission.RECEIVER_APP_STARTPROCESS", null);
        procfilter.addAction("action.SYNC_SCANWEDGE_IMPORT_CONFIG");
        mContext.registerReceiver(new startProcessChangeReceiver(), procfilter);

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
        loadProperties();

        /*SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        settingsObserver.observe();*/
        if (mScanWedgeEngineImpl == null)
            mScanWedgeEngineImpl = new ScanWedgeEngineImpl(this);

    }

    public IBinder onBind(Intent intent) {
        if (intent != null && intent.getComponent() != null)
            Log.e(TAG, " onBind  packagename: " + intent.getComponent().getPackageName());
        if (mScanWedgeEngineImpl == null)
            mScanWedgeEngineImpl = new ScanWedgeEngineImpl(this);
        return mScanWedgeEngineImpl;
    }

    public boolean onUnbind(Intent intent) {
        Log.d(TAG, " onUnbind ");
        return true;
    }

    private static ScanManagerCore mScanManagerCore = null;

    public static ScanManagerCore getInstance(Context context, String version) {
        Log.d(TAG, " getInstance ");
        if (mScanManagerCore == null) {
            mScanManagerCore = new ScanManagerCore(context);
        }
        return mScanManagerCore;
    }

    public Context getContext() {
        return mContext;
    }

    public void destroyInstance() {
        Log.d(TAG, " destroyInstance ");
    }

    public class ScanWedgeEngineImpl extends IScanWedgeEngine.Stub {
        WeakReference<ScanManagerCore> mService;

        public ScanWedgeEngineImpl(ScanManagerCore service) {
            mService = new WeakReference<ScanManagerCore>(service);
        }

        @Override
        public int open() throws RemoteException {
            return 0;
        }

        @Override
        public int close() throws RemoteException {
            return 0;
        }

        @Override
        public Map getScanerList() throws RemoteException {
            //return mScanManager.getScanerList();
            return null;
        }

        @Override
        public boolean setDefaults() throws RemoteException {
            if (mScanManager != null) {
                mScanManager.resetScannerParameters();
                updateDatabaseDefault();
                return true;
            }
            return false;
        }

        @Override
        public boolean softTrigger(int on) throws RemoteException {
            return false;
        }

        @Override
        public boolean hardwareTrigger(int on) throws RemoteException {
            return false;
        }

        @Override
        public boolean lockHwTriggler(boolean lock) throws RemoteException {
            return false;
        }

        @Override
        public int getPropertyInt(int index) {
            int value = -1;
            synchronized (mCachePropertys) {
                if (mCachePropertys.indexOfKey(index) != -1) {
                    Object v = null;
                    v = mCachePropertys.get(index);
                    if (v instanceof Integer) {
                        value = ((Integer) v).intValue();
                        return value;
                    }
                }
            }
            return value;
        }

        @Override
        public int setPropertyInts(int[] id_buffer, int id_buffer_length, int[] value_buffer, int value_buffer_length, int[] id_bad_buffer) throws RemoteException {
            int[] temp_bad_buff = new int[id_buffer_length];
            int bad_id = 0;

            SparseArray<Integer> mTempMap = new SparseArray<Integer>();
            SparseArray<Integer> mMap = new SparseArray<Integer>();
            for (int i = 0; i < id_buffer_length && i < value_buffer_length; i++) {
                int index = id_buffer[i];
                int value = value_buffer[i];
                if (value < 0) {
                    temp_bad_buff[bad_id++] = index;
                    continue;
                }
                boolean isSupprt = isPropertySupported(index);
                if (isSupprt) {
                    boolean isValid = invalidCheck(index, value);
                    if (!isValid) {
                        android.util.Log.i(TAG, "set parameter inalid int index: { " + index + " } " +
                                "value: { " + value + "}");
                        return -1;
                    }
                    synchronized (mCachePropertys) {
                        boolean isLocal = false;
                        isLocal = isLocalPropertyIndex(index);
                        //android.util.Log.i(TAG, " setParameterInts: index: { " + index + " } value:
                        // { " + value + "}");
                        if (isLocal == false) {
                            int V = getPropertyInt(index);
                            if (V == value) {
                                continue;
                            }
                            mTempMap.put(index, value);
                        }
                        mMap.put(index, value);
                        mCachePropertys.put(index, value);// maybe need to storage
                    }
                } else {
                    temp_bad_buff[bad_id++] = index;
                }
            }
            //Log.i(TAG, "setParameterInts  id_buffer_length  " + id_buffer_length);
            updateToDB(mMap);
            if (bad_id > 0)
                System.arraycopy(temp_bad_buff, 0, id_bad_buffer, 0, bad_id);
            return bad_id;
        }

        @Override
        public int getPropertyInts(int[] id_buffer, int id_buffer_length, int[] value_buffer, int value_buffer_length, int[] id_bad_buffer) throws RemoteException {
            int value_number = 0;
            int[] temp_bad_buff = new int[id_buffer_length];
            //int[] temp_value_buff = new int[value_buffer_length];
            int bad_id = 0;
            int value = 0;
            for (int i = 0; i < id_buffer_length && i < value_buffer_length; i++) {
                int index = id_buffer[i];
                boolean isSupprt = isPropertySupported(index);
                //Log.i(TAG, "isPropertySupported  index  " + index + " isSupprt " + isSupprt);
                if (isSupprt) {
                    synchronized (mCachePropertys) {
                        if (mCachePropertys.indexOfKey(index) != -1) {
                            Object v = null;
                            v = mCachePropertys.get(index);
                            if (v instanceof Integer) {
                                value = ((Integer) v).intValue();
                                value_buffer[value_number++] = value;
                            } else {
                                value_buffer[value_number++] = -1;
                                temp_bad_buff[bad_id++] = index;
                            }
                        }
                    }
                } else {
                    value_buffer[value_number++] = -1;
                    temp_bad_buff[bad_id++] = index;
                }
            }
            /*if(value_number > 0) {
                System.arraycopy(temp_value_buff, 0, value_buffer, 0, value_number);
            } */
            if (bad_id > 0)
                System.arraycopy(temp_bad_buff, 0, id_bad_buffer, 0, bad_id);
            return bad_id;
        }

        @Override
        public boolean setPropertyString(int index, String value) throws RemoteException {
            if (value == null || !isStringType(index)) {
                return false;
            }
            if (index == PropertyID.LABEL_PREFIX) {
                mLabelPrefix = value;
                USettings.System.putString(mContentResolver, USettings.Profile.DEFAULT_ID,
                        Settings.System.LABEL_PREFIX, value);
            } else if (index == PropertyID.LABEL_SUFFIX) {
                mLabelSuffix = value;
                USettings.System.putString(mContentResolver, USettings.Profile.DEFAULT_ID,
                        Settings.System.LABEL_SUFFIX, value);
            } else if (index == PropertyID.WEDGE_INTENT_ACTION_NAME) {
                wedgeIntentAction = value;
                USettings.System.putString(mContentResolver, USettings.Profile.DEFAULT_ID,
                        Settings.System.WEDGE_INTENT_ACTION_NAME, value);
            } else if (index == PropertyID.WEDGE_INTENT_DATA_STRING_TAG) {
                wedgeIntentLable = value;
                USettings.System.putString(mContentResolver, USettings.Profile.DEFAULT_ID,
                        Settings.System.WEDGE_INTENT_DATA_STRING_TAG, value);
            } else if (index == PropertyID.LABEL_MATCHER_PATTERN) {
                mPattern = value;
                USettings.System.putString(mContentResolver, USettings.Profile.DEFAULT_ID,
                        Settings.System.LABEL_MATCHER_PATTERN, value);
            } else if (index == PropertyID.LABEL_MATCHER_TARGETREGEX) {
                replaceRegex = value;
                USettings.System.putString(mContentResolver, USettings.Profile.DEFAULT_ID,
                        Settings.System.LABEL_MATCHER_TARGETREGEX, value);
            } else if (index == PropertyID.LABEL_MATCHER_REPLACEMENT) {
                replaceDst = value;
                USettings.System.putString(mContentResolver, USettings.Profile.DEFAULT_ID,
                        Settings.System.LABEL_MATCHER_REPLACEMENT, value);
            } else if (index == PropertyID.DEC_OCR_USER_TEMPLATE) {
                ocrUserTemplate = value;
                USettings.System.putString(mContentResolver, USettings.Profile.DEFAULT_ID,
                        Settings.System.DEC_OCR_USER_TEMPLATE, value);
            } else if (index == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR) {
                separatorChar = value;
                USettings.System.putString(mContentResolver, USettings.Profile.DEFAULT_ID,
                        Settings.System.LABEL_FORMAT_SEPARATOR_CHAR, value);
            }
            return true;
        }

        @Override
        public String getPropertyString(int index)/* throws RemoteException */ {
            String value = null;
            synchronized (mCachePropertys) {
                if (isStringType(index)) {
                    if (index == PropertyID.LABEL_PREFIX) {
                        value = mLabelPrefix;
                    } else if (index == PropertyID.LABEL_SUFFIX) {
                        value = mLabelSuffix;
                    } else if (index == PropertyID.WEDGE_INTENT_ACTION_NAME) {
                        value = wedgeIntentAction;
                    } else if (index == PropertyID.WEDGE_INTENT_DATA_STRING_TAG) {
                        value = wedgeIntentLable;
                    } else if (index == PropertyID.LABEL_MATCHER_PATTERN) {
                        value = mPattern;
                    } else if (index == PropertyID.LABEL_MATCHER_TARGETREGEX) {
                        value = replaceRegex;
                    } else if (index == PropertyID.LABEL_MATCHER_REPLACEMENT) {
                        value = replaceDst;
                    } else if (index == PropertyID.DEC_OCR_USER_TEMPLATE) {
                        value = ocrUserTemplate;
                    } else if (index == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR) {
                        value = separatorChar;
                    }
                } else if (mCachePropertys.indexOfKey(index) != -1) {
                    Object v = null;
                    v = mCachePropertys.get(index);
                    value = (String) v;
                }
            }
            return value;
        }

        @Override
        public void enableAllSymbologies(boolean enable) throws RemoteException {

        }

        @Override
        public boolean isSymbologyEnabled(int barcodeType) throws RemoteException {

            return false;
        }

        @Override
        public boolean isSymbologySupported(int barcodeType) throws RemoteException {

            return false;
        }

        @Override
        public void enableSymbology(int barcodeType, boolean enable) throws RemoteException {

        }

        @Override
        public boolean isPropertySupported(int idProperty) throws RemoteException {
            // TODO Auto-generated method stub
            return false;
        }
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

    /**
     * 加载系统扫描头服务配置参数
     */
    private void loadProperties() {
        int valueInt = -1;
        for (int i = 0; i < mHashMap.size(); i++) {
            int id = mHashMap.keyAt(i);
            String key = mHashMap.get(id);
            try {
                if (isStringType(id)) {
                    String val = Settings.System.getString(mContentResolver,
                            key);

                    if (id == PropertyID.LABEL_PREFIX && val != null) {
                        mLabelPrefix = val;
                    } else if (id == PropertyID.LABEL_SUFFIX && val != null) {
                        mLabelSuffix = val;
                    } else if (id == PropertyID.WEDGE_INTENT_ACTION_NAME && val != null) {
                        wedgeIntentAction = val;
                    } else if (id == PropertyID.WEDGE_INTENT_DATA_STRING_TAG && val != null) {
                        wedgeIntentLable = val;
                    } else if (id == PropertyID.LABEL_MATCHER_PATTERN && val != null) {
                        mPattern = val;
                    } else if (id == PropertyID.LABEL_MATCHER_TARGETREGEX && val != null) {
                        replaceRegex = val;
                    } else if (id == PropertyID.LABEL_MATCHER_REPLACEMENT && val != null) {
                        replaceDst = val;
                    } else if (id == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR && val != null) {
                        separatorChar = val;
                    } else if (id == PropertyID.DEC_OCR_USER_TEMPLATE && val != null) {
                        ocrUserTemplate = val;
                    }
                } else {
                    valueInt = Settings.System.getInt(mContentResolver, key,
                            valueInt);
                    if (valueInt != -1) {
                        //if (DEBUG) android.util.Log.i(TAG, valueInt + " database valueInt index:" +
                        // id);
                        mCachePropertys.put(id, valueInt);
                        valueInt = -1;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    int currentProfileId = USettings.Profile.DEFAULT_ID;
    private void loadPropertiesForAPP(String packageName) {
        long startTime = System.currentTimeMillis();
        int valueInt = -1;
        String profileId = USettings.System.getAppProfileId(mContentResolver, packageName);
        Log.i(TAG, currentProfileId + " currentProfileId valueInt packageName:" + profileId);
        try {
            valueInt = Integer.parseInt(profileId);
        } catch (NumberFormatException e) {
        }
        if (currentProfileId == valueInt) {
            return;
        }
        currentProfileId = valueInt;
        int currentProfileIdEnable = USettings.System.getInt(mContentResolver, currentProfileId, Settings.System.SCANNER_ENABLE, 0);
        if (currentProfileIdEnable == 1) {
            mScanManager.openScanner();
        } else {
            mScanManager.closeScanner();
            Log.i(TAG, " currentProfileId:" +currentProfileId + " packageName:" + packageName + " close scanner");
            return;
        }
        SparseArray<Integer> mTempMap = new SparseArray<Integer>();
        synchronized (mCachePropertys) {
            valueInt = -1;
            for (int i = 0; i < mHashMap.size(); i++) {
                int id = mHashMap.keyAt(i);
                String key = mHashMap.get(id);
                if (isStringType(id)) {
                    String val = USettings.System.getString(mContentResolver, currentProfileId, key);
                    if (id == PropertyID.LABEL_PREFIX && val != null) {
                        mLabelPrefix = val;
                    } else if (id == PropertyID.LABEL_SUFFIX && val != null) {
                        mLabelSuffix = val;
                    } else if (id == PropertyID.WEDGE_INTENT_ACTION_NAME && val != null) {
                        wedgeIntentAction = val;
                    } else if (id == PropertyID.WEDGE_INTENT_DATA_STRING_TAG && val != null) {
                        wedgeIntentLable = val;
                    } else if (id == PropertyID.LABEL_MATCHER_PATTERN && val != null) {
                        mPattern = val;
                    } else if (id == PropertyID.LABEL_MATCHER_TARGETREGEX && val != null) {
                        replaceRegex = val;
                    } else if (id == PropertyID.LABEL_MATCHER_REPLACEMENT && val != null) {
                        replaceDst = val;
                    } else if (id == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR && val != null) {
                        separatorChar = val;
                    } else if (id == PropertyID.DEC_OCR_USER_TEMPLATE && val != null) {
                        ocrUserTemplate = val;
                    }
                    if (ScanWedgeApplication.DEVDEBUG)
                        android.util.Log.i(TAG, val + " database valueString index:" + id);
                    mCacheStringPropertys.put(id, val);
                } else {
                    valueInt = USettings.System.getInt(mContentResolver, currentProfileId, key, valueInt);
                    if (ScanWedgeApplication.DEVDEBUG)
                        android.util.Log.i(TAG, valueInt + " database valueInt index:" + id);
                    /*boolean isLocal = false;
                    isLocal = isLocalPropertyIndex(id);*/
                    if (mScanWedgeEngineImpl != null) {
                        int V = mScanWedgeEngineImpl.getPropertyInt(id);
                        //Log.i(TAG, valueInt + " database valueInt index:" + V);
                        if (V == valueInt) {
                            continue;
                        }
                    }
                    mTempMap.put(id, valueInt);
                    mCachePropertys.put(id, valueInt);
                    valueInt = -1;
                }
            }
        }
        long midTime = System.currentTimeMillis();
        Log.i(TAG, " database read time: " + (midTime - startTime));
        boolean enableDockButton = USettings.System.getInt(mContentResolver, currentProfileId, Settings.System.SUSPENSION_BUTTON, 0) == 1;
        if (enableDockButton) {
            try {
                Intent mIntent = new Intent();
                mIntent.setComponent(new ComponentName("com.android.usettings", "com.android.usettings.FxService"));
                mContext.stopService(mIntent);
            } catch (Exception e) {
            }
            try {
                Intent intentService = new Intent("com.ubx.scanwedge.DOCK_BUTTOM");
                Intent eintent = new Intent(getExplicitIntent(mContext, intentService));
                mContext.startService(eintent);
            } catch (Exception e) {
                Log.e(TAG,
                        "Start DeviceBackendService failed:" + e.getMessage());
            }
        } else {
            try {
                Intent mIntent = new Intent();
                mIntent.setComponent(new ComponentName("com.android.usettings", "com.android.usettings.FxService"));
                mContext.stopService(mIntent);
            } catch (Exception e) {
            }
            try {
                Intent intentService = new Intent("com.ubx.scanwedge.DOCK_BUTTOM");
                Intent eintent = new Intent(getExplicitIntent(mContext, intentService));
                mContext.stopService(eintent);
            } catch (Exception e) {
                Log.e(TAG,
                        "Start DeviceBackendService failed:" + e.getMessage());
            }
        }
        Log.i(TAG, " reload profile end currentProfileId:" + currentProfileId + " currentProfileIdEnable: " + currentProfileIdEnable + " mTempMap size=" + mTempMap.size() + " enableDockButton:" + enableDockButton);
        updateScanPropertys(mTempMap);
        long endTime = System.currentTimeMillis();
        Log.i(TAG, " updateScan service time: " + (endTime - startTime));
    }

    private void loadImportProperties() {
        SparseArray<Integer> mTempMap = new SparseArray<Integer>();
        synchronized (mCachePropertys) {
            int valueInt = -1;
            int profileId = USettings.Profile.DEFAULT_ID;
            for (int i = 0; i < mHashMap.size(); i++) {
                int id = mHashMap.keyAt(i);
                String key = mHashMap.get(id);
                if (isStringType(id)) {
                    String val = USettings.System.getString(mContentResolver, profileId, key);
                    if (id == PropertyID.LABEL_PREFIX && val != null) {
                        mLabelPrefix = val;
                    } else if (id == PropertyID.LABEL_SUFFIX && val != null) {
                        mLabelSuffix = val;
                    } else if (id == PropertyID.WEDGE_INTENT_ACTION_NAME && val != null) {
                        wedgeIntentAction = val;
                    } else if (id == PropertyID.WEDGE_INTENT_DATA_STRING_TAG && val != null) {
                        wedgeIntentLable = val;
                    } else if (id == PropertyID.LABEL_MATCHER_PATTERN && val != null) {
                        mPattern = val;
                    } else if (id == PropertyID.LABEL_MATCHER_TARGETREGEX && val != null) {
                        replaceRegex = val;
                    } else if (id == PropertyID.LABEL_MATCHER_REPLACEMENT && val != null) {
                        replaceDst = val;
                    } else if (id == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR && val != null) {
                        separatorChar = val;
                    } else if (id == PropertyID.DEC_OCR_USER_TEMPLATE && val != null) {
                        ocrUserTemplate = val;
                    }
                    mCacheStringPropertys.put(id, val);
                } else {
                    valueInt = USettings.System.getInt(mContentResolver, profileId, key, valueInt);
                    try {
                        if (valueInt != -1) {
                            Object v = null;
                            v = mCachePropertys.get(id);
                            if (v instanceof Integer) {
                                int value = ((Integer) v).intValue();
                                if (valueInt == value) {
                                    continue;
                                }
                                mTempMap.put(id, value);
                            }
                            //if (DEBUG) android.util.Log.i(TAG, valueInt + " database valueInt index:" + id);
                            mCachePropertys.put(id, valueInt);
                            valueInt = -1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            updateScanPropertys(mTempMap);
        }
    }

    //更新参数到扫描服务
    private void updateScanPropertys(SparseArray<Integer> tempMap) {
        int[] id = new int[1];
        int[] value = new int[1];
        String[] valueStr = new String[1];
        int key = 0;
        for (int i = 0; i < tempMap.size(); i++) {
            key = tempMap.keyAt(i);
            id[0] = key;
            try {
                value[0] = tempMap.get(key);
            } catch (NumberFormatException e) {
                value[0] = 0;
            }
            mScanManager.setParameterInts(id, value);
        }
        for (int i = 0; i < mCacheStringPropertys.size(); i++) {
            key = mCacheStringPropertys.keyAt(i);
            id[0] = key;
            try {
                valueStr[0] = mCacheStringPropertys.get(key);
            } catch (NumberFormatException e) {
                valueStr[0] = "";
            }
            mScanManager.setParameterString(id, valueStr);
        }
    }

    private void updateDefault(SparseArray<String> property) {
        int cachLength = mHashMap.size();
        int propertyLength = property.size();
        int length = cachLength > propertyLength ? propertyLength : cachLength;
        ContentValues[] currValues = new ContentValues[length];
        int currIndex = 0;
        for (int i = 0; i < length; i++) {
            int key = mHashMap.keyAt(i);
            String value = property.get(key);
            if (value == null) {
                android.util.Log.i("debug", "parse property: " + key);
                continue;
            }
            ContentValues values = new ContentValues();
            values.put(UConstants.PROFILE_ID, USettings.Profile.DEFAULT_ID);
            values.put(UConstants.PROPERTY_ID, key);
            values.put(UConstants.PROPERTY_NAME, mHashMap.get(key));
            values.put(UConstants.PROPERTY_VALUE, value);
            currValues[currIndex++] = values;
            if (!isStringType(key)) {
                try {
                    mCachePropertys.put(key, Integer.valueOf(value));
                } catch (NumberFormatException e) {
                    int val = value.charAt(0);
                    mCachePropertys.put(key, val);//StringIndexOutOfBoundsException
                }
            } else {
                if (key == PropertyID.LABEL_PREFIX) {
                    mLabelPrefix = value;
                } else if (key == PropertyID.LABEL_SUFFIX) {
                    mLabelSuffix = value;
                } else if (key == PropertyID.WEDGE_INTENT_ACTION_NAME) {
                    wedgeIntentAction = value;
                } else if (key == PropertyID.WEDGE_INTENT_DATA_STRING_TAG) {
                    wedgeIntentLable = value;
                } else if (key == PropertyID.LABEL_MATCHER_PATTERN) {
                    mPattern = value;
                } else if (key == PropertyID.LABEL_MATCHER_TARGETREGEX) {
                    replaceRegex = value;
                } else if (key == PropertyID.LABEL_MATCHER_REPLACEMENT) {
                    replaceDst = value;
                } else if (key == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR) {
                    separatorChar = value;
                } else if (key == PropertyID.DEC_OCR_USER_TEMPLATE) {
                    ocrUserTemplate = value;
                }
            }
        }
        if (length != currIndex) {
            ContentValues[] Values = new ContentValues[currIndex];
            System.arraycopy(currValues, 0, Values, 0, currIndex);
            USettings.System.putBulkStrings(mContentResolver, Values);
        } else {
            USettings.System.putBulkStrings(mContentResolver, currValues);
        }
        currValues = null;
    }

    private SparseArray<String> parsePropertyXML(boolean defaultXML) {
        InputStream inputStream = null;
        SparseArray<String> arraryProperty = new SparseArray<String>();
        try {
            if (defaultXML) {
                inputStream = new FileInputStream("/system/etc/default_property.xml");
            }
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, "utf-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("property".equals(parser.getName())) {

                            //String name = parser.getAttributeValue(null, "name");
                            String id = parser.getAttributeValue(0);
                            Integer key = Integer.parseInt(id);
                            String name = parser.getAttributeValue(1);
                            String value = parser.nextText();
                            if (key != null) {
                                arraryProperty.put(key, value);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {

            }
        }
        return arraryProperty;
    }

    private void updateDatabaseDefault() {
        long startTime = System.currentTimeMillis();
//        SparseArray<String> UFSproperty = parsePropertyXML(false);
        SparseArray<String> property = parsePropertyXML(true);
        long stopTime = System.currentTimeMillis();
        android.util.Log.i("debug", "parse time: " + (stopTime - startTime));
        updateDefault(property);
        stopTime = System.currentTimeMillis();
        android.util.Log.i(TAG, "update database: " + (stopTime - startTime));
        property.clear();
        property = null;
        android.util.Log.i(TAG, "update default end: ");
    }

    private String getAppendString(String appendStr) {
        if (appendStr != null && !appendStr.isEmpty()) {
            for (int i = 0; i < NON_PRINTABLE_CHARS.length; i++) {

                String noPrint = nonPrintCharHashmap.get(NON_PRINTABLE_CHARS[i]);
                if (noPrint != null) {
                    // System.out.println("charest = " + noPrint);
                    appendStr = appendStr.replace(NON_PRINTABLE_CHARS[i], noPrint);
                }
            }
        } else {
            appendStr = "";
        }
        // if(DEBUG) Log.i(TAG, " append prefix/suffix String length:  " +
        // appendStr.length());
        return appendStr;
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

    private String buildLabelData(int type, String barcode) {
        StringBuilder textForWedge = new StringBuilder();
        if (type == 1) {
            textForWedge.append(getAppendString(mLabelPrefix));
            textForWedge.append(getAppendString(barcode));
            return textForWedge.toString();
        } else if (type == 2) {
            textForWedge.append(getAppendString(barcode));
            textForWedge.append(getAppendString(mLabelSuffix));
            return textForWedge.toString();
        } else if (type == 3) {
            textForWedge.append(getAppendString(mLabelPrefix));
            textForWedge.append(getAppendString(barcode));
            textForWedge.append(getAppendString(mLabelSuffix));
            return textForWedge.toString();
        } else if (type == 4) {
            if (mPattern != null && !mPattern.equals("")) {
                Pattern p = Pattern.compile(mPattern);
                Matcher matcher = p.matcher(barcode);
                while (matcher.find()) {
                    textForWedge.append(matcher.group());
                }
                if (textForWedge.length() > 0) {
                    return textForWedge.toString();
                }
            }
        } else if (type == 5) {
            if (replaceRegex != null && !replaceRegex.equals("")) {
                if (replaceDst != null) {
                    byte[] regex = hexStringToByteArray(replaceRegex);
                    if (regex != null) {
                        try {
                            String dstBarcode = barcode.replace((new String(regex)), replaceDst);
                            return dstBarcode;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return barcode;
    }

    /**
     * 检查是否为扫描头内部参数
     *
     * @param index
     * @return
     */
    private boolean isLocalPropertyIndex(int index) {
        boolean isLocal = false;
        for (int i = 0; i < LOCAL_PROPERTY_INDEX.length; i++) {
            if (index == LOCAL_PROPERTY_INDEX[i]) {
                isLocal = true;
                break;
            }
        }
        return isLocal;
    }

    private void updateToDB(SparseArray<Integer> tempMap) {
        int size = tempMap.size();
        if (size > 0) {
            ContentValues[] currValues = new ContentValues[size];
            String key = null;
            int value;
            for (int i = 0; i < size; i++) {
                ContentValues values = new ContentValues();
                int keyForIndex = tempMap.keyAt(i);
                key = mHashMap.get(keyForIndex);
                value = (Integer) tempMap.valueAt(i);

                values.put(UConstants.PROFILE_ID, USettings.Profile.DEFAULT_ID);
                values.put(UConstants.PROPERTY_ID, keyForIndex);
                values.put(UConstants.PROPERTY_NAME, key);
                values.put(UConstants.PROPERTY_VALUE, value);
                currValues[i] = values;
            }
            USettings.System.putBulkStrings(mContentResolver, currValues);
        }
    }

    private SparseArray<Integer> mCachePropertys = new SparseArray<Integer>();
    private SparseArray<String> mCacheStringPropertys = new SparseArray<String>();
    private SparseArray<String> mHashMap = new SparseArray<String>();

    private HashMap<String, String> nonPrintCharHashmap = new HashMap<String, String>();
    private String wedgeIntentAction = ScanManager.ACTION_DECODE;
    private String wedgeIntentLable = ScanManager.BARCODE_STRING_TAG;
    private int timeOut = 5000;
    private int removeNonPrintChar = 0;
    private String mLabelPrefix = "";
    private String mLabelSuffix = "";
    private String mPattern = "";
    private String replaceRegex = "";
    private String replaceDst = "";
    private String separatorChar = "";
    private String ocrUserTemplate = "";
    private final String[] NON_PRINTABLE_CHARS = new String[]{
            "[BS]", "[LF]", "[CR]", "[HT]"// \b \n \r \t
    };

    private final String[] NON_PRINTABLE_CHARS_VALUES = new String[]{
            "\b", "\n", "\n", "\t"// \b \n \r \t
    };

    private boolean invalidCheck(int index, int val) {
        switch (index) {
            case PropertyID.TRIGGERING_MODES:
                if (val == 4 || val == 8 || val == 2)
                    return true;
            case PropertyID.SEND_GOOD_READ_BEEP_ENABLE:
            case PropertyID.GOOD_READ_BEEP_ENABLE:
                if (val >= 0 && val <= 2)
                    return true;
            case PropertyID.SEND_LABEL_PREFIX_SUFFIX:
                if (val >= 0 && val <= 6)
                    return true;
            case PropertyID.I25_LENGTH1:// 2 50 6
            case PropertyID.I25_LENGTH2:// 2 50 10
                if (val >= 2 && val <= 50)
                    return true;
            case PropertyID.D25_LENGTH1:// 1 50 6
            case PropertyID.D25_LENGTH2:// 1 50 10
            case PropertyID.CODE39_LENGTH1: // 1 50 1
            case PropertyID.CODE39_LENGTH2:// 1 50 20
            case PropertyID.CODABAR_LENGTH1: // 1 50 4
            case PropertyID.CODABAR_LENGTH2:// 1 50 20
            case PropertyID.CODE93_LENGTH1:// 1 50 2
            case PropertyID.CODE93_LENGTH2:// 1 50 20
                if (val >= 1 && val <= 50)
                    return true;
            case PropertyID.CODE128_LENGTH1:// 1 80 2 NOTE to 2D 55
            case PropertyID.CODE128_LENGTH2:// 1 80 40
                if (val >= 1 && val <= 80)
                    return true;
            case PropertyID.MSI_LENGTH1:// 1 15 4
            case PropertyID.MSI_LENGTH2:// 1 15 10
                if (val >= 1 && val <= 15)
                    return true;
            case PropertyID.GS1_EXP_LENGTH1:// 1 74 1
            case PropertyID.GS1_EXP_LENGTH2:// 1 74 74
                if (val >= 1 && val <= 74)
                    return true;
            case PropertyID.I25_ENABLE_CHECK:
            case PropertyID.UPCA_SEND_SYS:
            case PropertyID.UPCE_SEND_SYS:
                if (val >= 0 && val <= 2)
                    return true;
            case PropertyID.DATAMATRIX_LENGTH1://1
            case PropertyID.DATAMATRIX_LENGTH2://1500
                if (val >= 1 && val <= 1500)
                    return true;
            case PropertyID.LASER_ON_TIME:
                if (val >= 5 && val <= 99)
                    return true;
            case PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL://100ms
                if (val > 0 && val <= 99)
                    return true;
            case PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL:
                if (val >= 1 && val <= 4)
                    return true;
            case PropertyID.CODE11_LENGTH1://1
            case PropertyID.CODE11_LENGTH2://1500
                if (val >= 4 && val <= 55)
                    return true;
            case PropertyID.CODE11_ENABLE_CHECK://1500
                if (val >= 0 && val <= 2)
                    return true;
            case PropertyID.UPC_EAN_SECURITY_LEVEL:
                if (val >= 0 && val <= 3)
                    return true;
            case PropertyID.IMAGE_ONE_D_INVERSE:
            case PropertyID.DATAMATRIX_INVERSE:
            case PropertyID.QRCODE_INVERSE:
            case PropertyID.AZTEC_INVERSE:
                if (val >= 0 && val <= 2)
                    return true;
            case PropertyID.BAR_CODES_TO_READ:
                if (val >= 1 || val <= 10) {
                    return true;
                }
                break;
            case PropertyID.CODING_FORMAT:
                if (val >= 0 || val <= 3) {
                    return true;
                }
                break;
            case PropertyID.IMAGE_FIXED_EXPOSURE:
                if (val >= 1 || val <= 7848) {
                    return true;
                }
                break;
            case PropertyID.DEC_2D_LIGHTS_MODE:
                if (val >= 0 || val <= 4) {
                    return true;
                }
                break;
            case PropertyID.DEC_2D_CENTERING_ENABLE:
            case PropertyID.DEC_2D_DEBUG_WINDOW_ENABLE:
                if (val == 1 || val == 0) {
                    return true;
                }
                break;
            case PropertyID.DEC_2D_CENTERING_MODE:
                if (val >= 0 || val <= 3) {
                    return true;
                }
                break;
            case PropertyID.DEC_2D_WINDOW_UPPER_LX:
                if (val >= 0 || val <= 830) {
                    return true;
                }
                break;
            case PropertyID.DEC_2D_WINDOW_UPPER_LY:
                if (val >= 0 || val <= 638) {
                    return true;
                }
                break;
            case PropertyID.DEC_2D_WINDOW_LOWER_RX:
                if (val >= 1 || val <= 831) {
                    return true;
                }
                break;
            case PropertyID.DEC_2D_WINDOW_LOWER_RY:
                if (val >= 1 || val <= 639) {
                    return true;
                }
                break;
            case PropertyID.DEC_ILLUM_POWER_LEVEL:
                if (val >= 0 || val <= 10) {
                    return true;
                }
                break;
            case PropertyID.DEC_PICKLIST_AIM_DELAY://1-4000ms
                if (val >= 0 || val <= 40) {
                    return true;
                }
                break;
            case PropertyID.DEC_Multiple_Decode_TIMEOUT:
                if (val >= 50 || val <= 60000) {
                    return true;
                }
                break;
            case PropertyID.DEC_MaxMultiRead_COUNT:
                if (val >= 1 || val <= 100) {
                    return true;
                }
                break;
            case PropertyID.DEC_Multiple_Decode_MODE:
                if (val >= 0 || val <= 2) {
                    return true;
                }
                break;
            case PropertyID.LABEL_APPEND_ENTER:
            case PropertyID.WEDGE_KEYBOARD_TYPE:
                if (val >= 0 || val <= 3) {
                    return true;
                }
                break;
            case PropertyID.DEC_OCR_MODE:
                if (val >= 0 || val <= 3) {
                    return true;
                }
                break;
            case PropertyID.DEC_OCR_TEMPLATE:
                if (val >= 1 || val <= 16) {
                    return true;
                }
                break;
            default:
                if (val == 0 || val == 1) {
                    return true;
                }
        }
        return false;
    }

    private boolean isStringType(int id) {
        return (id == PropertyID.LABEL_PREFIX
                || id == PropertyID.LABEL_SUFFIX
                || id == PropertyID.WEDGE_INTENT_ACTION_NAME
                || id == PropertyID.WEDGE_INTENT_DATA_STRING_TAG
                || id == PropertyID.LABEL_MATCHER_PATTERN
                || id == PropertyID.LABEL_MATCHER_REPLACEMENT
                || id == PropertyID.LABEL_MATCHER_TARGETREGEX
                || id == PropertyID.DEC_OCR_USER_TEMPLATE
                || id == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR);
    }

    private final int[] LOCAL_PROPERTY_INDEX = {
            PropertyID.TRIGGERING_MODES,
            PropertyID.TRIGGERING_SLEEP_WORK,
            PropertyID.GOOD_READ_BEEP_ENABLE,
            PropertyID.GOOD_READ_VIBRATE_ENABLE,
            PropertyID.SEND_GOOD_READ_BEEP_ENABLE,
            PropertyID.SEND_GOOD_READ_VIBRATE_ENABLE,
            PropertyID.LABEL_APPEND_ENTER,
            //PropertyID.IMAGE_PICKLIST_MODE,
            PropertyID.WEDGE_KEYBOARD_ENABLE,
            PropertyID.WEDGE_KEYBOARD_TYPE,
            PropertyID.TRIGGERING_LOCK,
            PropertyID.SEND_LABEL_PREFIX_SUFFIX,
            PropertyID.LABEL_PREFIX,
            PropertyID.LABEL_SUFFIX,
            PropertyID.LABEL_MATCHER_PATTERN,
            PropertyID.LABEL_MATCHER_TARGETREGEX,
            PropertyID.LABEL_MATCHER_REPLACEMENT,
            PropertyID.REMOVE_NONPRINT_CHAR,
            PropertyID.CODING_FORMAT,
            PropertyID.WEDGE_INTENT_ACTION_NAME,
            PropertyID.WEDGE_INTENT_DATA_STRING_TAG,
            PropertyID.LABEL_SEPARATOR_ENABLE,
            PropertyID.LABEL_FORMAT_SEPARATOR_CHAR,
            PropertyID.DEC_OCR_USER_TEMPLATE,
            PropertyID.OUT_EDITORTEXT_MODE
    };
}
