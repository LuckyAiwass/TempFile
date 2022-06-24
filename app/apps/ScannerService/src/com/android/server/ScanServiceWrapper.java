package com.android.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.net.Uri;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.device.ScanManager;
import android.device.ScanNative;
import android.device.scanner.configuration.PropertyID;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Binder;
import android.os.IBinder;
import android.os.IScanService;
import android.os.IScanServiceWrapper;
import android.os.Vibrator;
import android.device.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.os.Bundle;
import android.os.RemoteCallbackList;
import android.view.InputDevice;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.scanner.IScanCallBack;
import com.android.server.scanner.Scanner;
import com.android.server.scanner.ScannerFactory;
import com.android.server.scanner.DecodeData;
import com.android.server.scanner.Utils;
import com.android.server.encode.ChineseHandle;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Build;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.media.AudioAttributes;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.ActivityManager;
import com.android.usettings.FxService;

import com.android.server.scanner.OEMSymbologyId;
import com.ubx.barcode.smartdataparser.InputData;
import com.ubx.barcode.smartdataparser.SmartDataParser;
import com.ubx.barcode.smartdataparser.SmartDataParserException;
import com.ubx.barcode.smartdataparser.TokenizedData;

public class ScanServiceWrapper extends IScanServiceWrapper.Stub {
    private static final String TAG = "ScanServiceWrapper";

    private int scanType;
    // urovo add shenpidong begin 2019-04-12
    private static int scanPower = -1;
    // urovo add shenpidong end 2019-04-12
    public int outputMode = 0;
    public Context mContext;

    private boolean bootCompleted = false;

    private SoundPool soundpool = null;
    private int heightBeepId;
    private int middleBeepId;
    private Vibrator mVibrator;
    private Scanner mScanner;
    private boolean mIsScannerOpend = false;
    private boolean mScreenStateOn = true;
    // urovo add shenpidong begin 2019-04-12
    private static boolean mTopActivityIsCameraAndCloseScanner = false;
    private static boolean mIsOpenCameraAndCloseScanner = false;
    private static boolean mIsOpenCameraAndUse = false;
    // urovo add shenpidong end 2019-04-12
    /*  private static final int MESSAGE_SET = 0;
        private static final int MESSAGE_CODE_CHANGED = 1;*/
    private static final int MESSAGE_DELAYED_RESET_SCANNER = 2;
    private static final int MESSAGE_CONFIG_CHANGED = 3;
    private static final int MESSAGE_SEND_KEYEVENT = 4;
    private static final int MESSAGE_SOFT_DECODE = 5;
    private static final int MESSAGE_RELEASE_WAKELOCK = 6;
    private static final int MESSAGE_DELAYED_CONFIG_SCANNER = 7;
    // urovo add shenpidong begin 2019-03-15
    private static final int MESSAGE_DELAYED_OPEN_SCANNER = 8;
    // urovo add shenpidong end 2019-03-15
    private ContentResolver mContentResolver;

    private IWindowManager mWinManager = null;
    private ClipboardManager clipboard;
    private static HashMap<String, Integer> mParaSettings;

    private RemoteCallbackList<IScanCallBack> mCallBacks = new RemoteCallbackList<IScanCallBack>();

    // zhouxin ST
    private PowerManager.WakeLock mWakeLock;
    //ID85 YTO
    private static final String ACTION_SCAN_SWITCH = "com.yto.action.SCAN_SWITCH";
    private static final String ACTION_SCAN_START = "com.yto.action.START_SCAN";
    private static final String ACTION_SCAN_CONFIG = "com.yto.action.SCANNER_CONFIG";
    private static final String ACTION_SCAN_CONTINUE = "com.yto.action.CONTINUE_SCAN";
    private static final String ACTION_SCAN_DATA = "com.yto.action.GET_SCANDATA";
    private static final String DEC_ILLUM_POWER_LEVEL = "DEC_ILLUM_POWER_LEVEL";
    // urovo add shenpidong begin 2019-07-08
    private static final String DEC_2D_LIGHTS_MODE = "DEC_2D_LIGHTS_MODE";
    // urovo add shenpidong end 2019-07-08
    
    //urovo add jinpu.lin 2019.04.30
    private boolean  isoutput=true;
    private static final int MESSAGE_DELAYED_1D_SCANNER = 10;
    private static final String focusIntent  = "action.view.focus_changed";
    private static final String outputIntent= "action.scan.data.output";
    // urovo add shenpidong begin 2019-07-11
    private static final String FACTORY_BACKCAMERA = "com.qualcomm.factory.CameraBack.CameraBack";
    private static final String FACTORY_FRONTCAMERA = "com.qualcomm.factory.CameraFront.CameraFront";
    private static final String QCOM_SNAPCAM_CAMERA = "org.codeaurora.snapcam";
    // urovo add shenpidong end 2019-07-11
    private int cacheStartTime = -1;
    private int cacheLimitEnable = 1;
    private int cacheLimitTime = 70;//s
    private int cacheEnable = 1;
    private long currtentDateTimes = 0;
    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    private SparseArray<String> cacheBarcode = new SparseArray<String>();
    //urovo add end 2019.04.30
    // urovo add by shenpidong begin 2020-05-04
    private static long mContinueTimeout = System.currentTimeMillis();
    private static boolean isContinueScanner = false;
    private static boolean isScreenDark = false;
    private static int mPrevBrightness = 0;;
    private static final int MESSAGE_DELAYED_CONTINUE_TIMEOUT_SCANNER = 10;
    private static final int MESSAGE_UPDATE_DELAYED_CONTINUE_TIMEOUT_SCANNER = 11;
    // urovo add by shenpidong end 2020-05-04
    
    private static final String ACTION_GEENK_SCAN_SWITCH = "com.geenk.action.HARDWARE_SCAN_SWITCH";
    private boolean mCustomYTO = false;
    private boolean mYTOSixLenEnabled = true;
    private boolean mCustomDEPPON = true;//Build.PWV_CUSTOM_CUSTOM.equals("DEPPON");
	//记录开机后开始扫描次数
    private int mDeviceDecodeNum = 0;
    private final class YTOReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_SCAN_SWITCH.equals(action)) {
                int enable = intent.getIntExtra("extra", 0);
                if (enable == 1) {
                    open();
                } else if (enable == 0) {
                    close();
                }
            } else if (ACTION_SCAN_START.equals(action)) {
                softTrigger(1);
            } else if (ACTION_SCAN_CONFIG.equals(action)) {
                byte[] config = intent.getByteArrayExtra("config");
                ScanNative.setProperties(config);
            } else if (ACTION_SCAN_CONTINUE.equals(action)) {
                int enable = intent.getIntExtra("extra", 8);
                int[] id_buffer = new int[1];
                int[] value_buffer = new int[1];
                id_buffer[0] = PropertyID.TRIGGERING_MODES;
                int[] id_bad_buffer = new int[1];
                value_buffer[0] = enable == 1 ? 4 : 8;
                setPropertyInts(id_buffer, id_buffer.length, value_buffer, value_buffer.length,
                        id_bad_buffer);

            }else if(Intent.ACTION_BOOT_COMPLETED.equals(action)){
                int[] id_buffer = new int[] {
                        PropertyID.CODE128_LENGTH1,
                        PropertyID.CODE39_LENGTH1,
                        PropertyID.CODE93_LENGTH1,
                        PropertyID.CODE11_LENGTH1
                };
                String len = android.device.provider.Settings.System.getString(mContext.getContentResolver(),
                        "YTO_LEN");
                if(TextUtils.isEmpty(len)){
                    len = "6";
                }
                int lenth = Integer.parseInt(len);
                boolean enabled = !"disabled".equals(android.device.provider.Settings.System.getString(mContext.getContentResolver(),
                        "SIX_LEN_ENABLED"));
                if(!enabled){
                    lenth = 2;
                }
                int[] value_buffer = new int[] {
                        lenth + 1,
                        lenth + 1,
                        lenth + 1,
                        lenth + 1
                };
                // urovo add by shenpidong begin 2020-12-08 , fix ArrayIndexOutOfBoundsException
                int[] id_bad_buffer = new int[id_buffer.length];
                // urovo add by shenpidong end 2020-12-08
                setPropertyInts(id_buffer, id_buffer.length, value_buffer, value_buffer.length,
                        id_bad_buffer);
            }
        }
    }

    private final class GEENKReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_GEENK_SCAN_SWITCH.equals(action)) {
                int extra = intent.getIntExtra("extra", 0);
                if (extra == 1) {
                    int[] id_buffer = new int[1];
                    int[] value_buffer = new int[1];
                    id_buffer[0] = PropertyID.WEDGE_KEYBOARD_ENABLE;
                    int[] id_bad_buffer = new int[1];
                    value_buffer[0] = 0;
                    setPropertyInts(id_buffer, id_buffer.length, value_buffer, value_buffer.length,
                            id_bad_buffer);
                } else if (extra == 2) {
                    int[] id_buffer = new int[2];
                    int[] value_buffer = new int[2];
                    id_buffer[0] = PropertyID.WEDGE_KEYBOARD_ENABLE;
                    id_buffer[1] = PropertyID.LABEL_APPEND_ENTER;
                    int[] id_bad_buffer = new int[2];
                    value_buffer[0] = 1;
                    value_buffer[1] = 1;
                    setPropertyInts(id_buffer, id_buffer.length, value_buffer, value_buffer.length,
                            id_bad_buffer);
                }
            } 
        }
    }

    private final class ConfigReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "intent:" + action);
            if("action.IMPORT_SCANNER_CONFIG_SYNC".equals(action)) {
                loadImportProperties();
            } else if("com.ubx.barcode.ACTION_ENABLE_SCANNER".equals(action)) {
                int enable = intent.getIntExtra("enable", 0);
                if (enable == 1) {
                    open();
                } else if (enable == 0) {
                    close();
                }
            } else if("com.ubx.barcode.ACTION_TRIGGER".equals(action)) {
                int enable = intent.getIntExtra("enable", 0);
                softTrigger(enable);
            } else if ("com.ubx.GET_DECODE_NUM".equals(action)) {
                Intent resintent = new Intent("com.ubx.GET_DECODE_NUM_RESULT");
                resintent.putExtra("DecodeNum", mDeviceDecodeNum);
                mContext.sendBroadcastAsUser(resintent, UserHandle.ALL);
            }
        }
    }
    //输出条码数据是否覆盖文本框内容，只在仅输入法模式下处理
    private int barcodeEditorMode = 0;
    private String wedgeIntentAction = ScanManager.ACTION_DECODE;
    private String wedgeIntentLable = ScanManager.BARCODE_STRING_TAG;
    private String wedgeIntentRaw = ScanManager.DECODE_DATA_TAG;
    private int timeOut = 5000;
    private int removeNonPrintChar = 0;
    private int webjump = 0;
    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();

            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(
                    android.device.provider.Settings.System.WEDGE_INTENT_ACTION_NAME), false, this);
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(
                    android.device.provider.Settings.System.WEDGE_INTENT_DATA_STRING_TAG),
                    false, this);
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(
                    android.device.provider.Settings.System.LASER_ON_TIME), false, this);
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(
                    android.device.provider.Settings.System.SCANNER_TYPE), false, this);
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(
                    android.device.provider.Settings.System.REMOVE_NONPRINT_CHAR), false, this);
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(
                    android.device.provider.Settings.System.LABEL_FORMAT_SEPARATOR_CHAR), false, this);
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(
                    android.device.provider.Settings.System.DEC_OCR_USER_TEMPLATE), false, this);
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(
                    android.device.provider.Settings.System.OUT_EDITORTEXT_MODE), false, this);
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(
                    android.device.provider.Settings.System.SUSPENSION_BUTTON), false, this);
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(
                    android.device.provider.Settings.System.WEBJUMP), false, this);
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
            wedgeIntentAction = android.device.provider.Settings.System.getString(resolver,
                    android.device.provider.Settings.System.WEDGE_INTENT_ACTION_NAME);
            wedgeIntentLable = android.device.provider.Settings.System.getString(resolver,
                    android.device.provider.Settings.System.WEDGE_INTENT_DATA_STRING_TAG);
            wedgeIntentRaw = android.device.provider.Settings.System.getString(resolver,
                    android.device.provider.Settings.System.WEDGE_INTENT_DECODE_DATA_TAG);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (wedgeIntentAction == null) {
            wedgeIntentAction = ScanManager.ACTION_DECODE;
        }
        if (wedgeIntentLable == null) {
            wedgeIntentLable = ScanManager.BARCODE_STRING_TAG;
        }
        if (TextUtils.isEmpty(wedgeIntentRaw)) {
            wedgeIntentRaw = ScanManager.DECODE_DATA_TAG;
        }
        try {
            removeNonPrintChar = android.device.provider.Settings.System.getInt(resolver,
                    android.device.provider.Settings.System.REMOVE_NONPRINT_CHAR, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            separatorChar = android.device.provider.Settings.System.getString(resolver,
                    android.device.provider.Settings.System.LABEL_FORMAT_SEPARATOR_CHAR);
        } catch (Exception e) {
            e.printStackTrace();
            separatorChar = "";
        }
        try {
            ocrUserTemplate = android.device.provider.Settings.System.getString(resolver,
                    android.device.provider.Settings.System.DEC_OCR_USER_TEMPLATE);
        } catch (Exception e) {
            e.printStackTrace();
            ocrUserTemplate = "";
        }
        try {
            barcodeEditorMode = android.device.provider.Settings.System.getInt(resolver,
                    android.device.provider.Settings.System.OUT_EDITORTEXT_MODE, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            webjump = android.device.provider.Settings.System.getInt(resolver,
                    android.device.provider.Settings.System.WEBJUMP, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        timeOut = android.device.provider.Settings.System.getInt(resolver,
                android.device.provider.Settings.System.LASER_ON_TIME, 50);
        timeOut = timeOut * 100;
        Log.i(TAG, "updateSettings......" + wedgeIntentAction + wedgeIntentLable);
        //int type = ScannerFactory.TYPE_N6603;

        int type = readConfig("SCANER_TYPE");
        //add by urovo luolin begin
        if(Build.PROJECT.equals("SQ53") && type == 0) {
            type = ScannerFactory.TYPE_N6603;
        }
       //add by urovo luolin end

        int isFirstOpenScanner = (scanType > 0 && scanType != ScannerFactory.TYPE_SE4710 && scanType != ScannerFactory.TYPE_SE4850 && scanType != ScannerFactory.TYPE_SE4770) 
            ? 1 : android.provider.Settings.System.getInt(resolver , "isFirstOpenScanner", 1);
        Log.i(TAG, "updateSettings......scanType:" + scanType + ", type:" + type + ",first time:" + isFirstOpenScanner);
        boolean isFirstOpenScan = false;

        if(type > 0) {
        // if(type > 0 && bootCompleted) {
            isFirstOpenScan = scanType != type;
            if (!bootCompleted && (type == ScannerFactory.TYPE_SE2030 || type == ScannerFactory.TYPE_SE955)) {
                if(scanType != type) {
                    if(mScanner != null) {
                        mScanner.close();
                        mScanner = null;
                    }
                    mScanner = ScannerFactory.createScanner(type, this);
                    mIsScannerOpend = false;
                } else {
                    if(mScanner == null) {
                        mScanner = ScannerFactory.createScanner(type, this);
                        mIsScannerOpend = false;
                    }
                }
                scanType = type;
                if(mScanner != null){
                    mScanner.setScannedCallback(mMyOnScannedListener);
                }
                return;
            }
            if (isFirstOpenScan) {
                if (mScanner != null) {
                    mScanner.close();
                    mScanner = null;
                    // urovo add shenpidong begin 2019-06-05
                    mIsScannerOpend = false;
                    // urovo add shenpidong end 2019-06-05
                }
                mScanner = ScannerFactory.createScanner(type, this);
                mIsScannerOpend = false;
                if (mScanner != null) {
                    scanType = type;
                    int power = readConfig("SCANER_POWER");
                    // urovo add shenpidong begin 2019-04-12
                    scanPower = power;
                    // urovo add shenpidong end 2019-04-12
                    if (power == 1) {
                        open();
                    }
                }
            }
        }
        if(isFirstOpenScanner == 1 && isFirstOpenScan && (scanType == ScannerFactory.TYPE_SE4710 || scanType == ScannerFactory.TYPE_SE4850 || scanType == ScannerFactory.TYPE_SE4770)) {
            android.provider.Settings.System.putInt(resolver , "isFirstOpenScanner", 0);
            updateDatabaseDefault();
        }
        //urovo add jinpu.lin 2019.04.30
        try{
            cacheLimitEnable = android.device.provider.Settings.System.getInt(resolver, ScanManager.CACHE_LIMIT_ENABLE, 1);
            cacheLimitTime = android.device.provider.Settings.System.getInt(resolver, ScanManager.CACHE_LIMIT_TIME, 70);
            cacheEnable = android.provider.Settings.System.getInt(resolver, ScanManager.CACHE_ENABLE, 1);
        } catch(Exception e) {
            e.printStackTrace();
            cacheEnable = 1;
            cacheLimitEnable = 1;
            cacheLimitTime = 70;
        }
        int buttonStatus = android.device.provider.Settings.System.getInt(mContext.getContentResolver(),
             android.device.provider.Settings.System.SUSPENSION_BUTTON, 0);
        updateSuspensionButtonStatus(buttonStatus);
        //urovo add end
    }

    public Context getContext() {
        return mContext;
    }

    // urovo add shenpidong begin 2019-04-11
    public void updateProperties() {
        if ((scanType == ScannerFactory.TYPE_N6603 || scanType == ScannerFactory.TYPE_N6703 || scanType == ScannerFactory.TYPE_SE4710
         || scanType == ScannerFactory.TYPE_SE4850 || scanType == ScannerFactory.TYPE_SE4770) && mHandler!=null) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_DELAYED_RESET_SCANNER, 200);
        }
    }
    // urovo add shenpidong end 2019-04-11

    // urovo zhoubo add begin 2020.8.13
    public SparseArray<Integer> getProperties() {
        return mCachePropertys;
    }

    public void updateSuspensionButtonStatus(int status) {
        try {
            if(bootCompleted) {
                Intent intent = new Intent(mContext, FxService.class);
                if (status == 1){
                    mContext.startService(intent);
                } else {
                    mContext.stopService(intent);
                }
            }
        } catch (Exception e) {
            Log.e(TAG,"Start fxService failed:" + e.getMessage());
        }
    }
    // urovo zhoubo add begin 2020.8.13

    // urovo add shenpidong begin 2019-05-06
    public boolean isRemoveNonPrintChar() {
        return removeNonPrintChar == 1;
    }
    // urovo add shenpidong begin 2019-05-06

    public ScanServiceWrapper(Context context) {
        super();
        Log.i(TAG, "ScanService......");
        mContext = context;
        mCustomYTO = Build.PWV_CUSTOM_CUSTOM.equals("YTO");
        mWinManager = IWindowManager.Stub.asInterface(ServiceManager.getService(Context
                .WINDOW_SERVICE));
        clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        mParaSettings = new HashMap<String, Integer>();
        initSoundpool();
        int size = ALL_PROPERTY_INDEX.length > ALL_PROPERTY_INDEX.length ? ALL_PROPERTY_INDEX.length : ALL_PROPERTY_INDEX.length;
        for (int i = 0; i < size; i++) {
            mHashMap.put(ALL_PROPERTY_INDEX[i], PROVIDERS_SETTINGS_STRING[i]);
        }

        for (int i = 0; i < NON_PRINTABLE_CHARS.length; i++) {
            nonPrintCharHashmap.put(NON_PRINTABLE_CHARS[i], NON_PRINTABLE_CHARS_VALUES[i]);
        }
        mContentResolver = mContext.getContentResolver();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(focusIntent);
        filter.addAction(outputIntent);
        mContext.registerReceiver(new PowerChangeReceiver(), filter);
        if (Build.PWV_CUSTOM_CUSTOM.equals("YTO")) {
            IntentFilter ytofilter = new IntentFilter();
            ytofilter.addAction(ACTION_SCAN_CONFIG);
            ytofilter.addAction(ACTION_SCAN_START);
            ytofilter.addAction(ACTION_SCAN_SWITCH);
            ytofilter.addAction(ACTION_SCAN_CONTINUE);
            ytofilter.addAction(Intent.ACTION_BOOT_COMPLETED);
            mContext.registerReceiver(new YTOReceiver(), ytofilter);
        }
        if (Build.PWV_CUSTOM_CUSTOM.equals("GEENK")) {
            IntentFilter geenkfilter = new IntentFilter();
            geenkfilter.addAction(ACTION_GEENK_SCAN_SWITCH);
            mContext.registerReceiver(new GEENKReceiver(), geenkfilter);
        }

        // zhouxin ST
        if (Build.PWV_CUSTOM_CUSTOM.equals("BHW") || Build.PWV_CUSTOM_CUSTOM.equals("STO")) {
            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        }

        IntentFilter configfilter = new IntentFilter();
        configfilter.addAction("action.IMPORT_SCANNER_CONFIG_SYNC");
        configfilter.addAction("com.ubx.barcode.ACTION_ENABLE_SCANNER");
        configfilter.addAction("com.ubx.barcode.ACTION_TRIGGER");
        configfilter.addAction("com.ubx.GET_DECODE_NUM");
        mContext.registerReceiver(new ConfigReceiver(), configfilter);
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
        LoadParas();
        ScannerFactory.scanerListInit();

        SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        settingsObserver.observe();
        mNotificationThread = new NotificationThread("LEDnotification");
        mNotificationThread.startThread();
        //urovo add jinpu.lin 2019.04.30
        if(Build.PWV_CUSTOM_CUSTOM.equals("WPH")) {

            cacheLimitEnable = android.device.provider.Settings.System.getInt(mContentResolver, ScanManager.CACHE_LIMIT_ENABLE, -1);
            cacheLimitTime = android.device.provider.Settings.System.getInt(mContentResolver, ScanManager.CACHE_LIMIT_TIME, -1);
            cacheEnable = android.provider.Settings.System.getInt(mContentResolver, ScanManager.CACHE_ENABLE, -1);
            if((cacheLimitEnable==-1) || (cacheLimitTime==-1) || (cacheEnable==-1)){
                writeConfig(ScanManager.CACHE_ENABLE, 1);
                writeConfig(ScanManager.CACHE_LIMIT_ENABLE, 1);
                writeConfig(ScanManager.CACHE_LIMIT_TIME, 70);
            }
            mCacheThread = new CacheThread("cache");
            mCacheThread.startThread();
        }
        Log.i(TAG, "ScanService end");
        //urovo add end 2019.04.30
    }

    // urovo add shenpidong begin 2019-04-12
    private void setBrightness(String ledDev, byte[] lightOn) {
        FileOutputStream fLed = null;
        try {
            fLed = new FileOutputStream(ledDev);
            fLed.write(lightOn);
//            fLed.close(); // maybe OOM : Too many open files
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fLed!=null) {
                try {
                    fLed.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                fLed = null;
            }
        }
    }
    // urovo add shenpidong end 2019-04-12

    NotificationHandler mNotificationHandler;
    NotificationThread mNotificationThread;

    final byte[] LIGHT_ON_26TB = {'2', '5', '5'};
    final byte[] LIGHT_ON = {'1'};
    final byte[] LIGHT_OFF = {'0'};
    final byte[] LIGHT_ON_SQ38 = {'7'};
    final byte[] LIGHT_OFF_SQ38 = {'8'};
    final byte[] LIGHT_ON_SQ45 = {'5'};
    final byte[] LIGHT_OFF_SQ45 = {'6'};
    final byte[] LIGHT_OFF_9 = {'0'};
    static String SCAN_LED_9 = "/sys/class/leds/left-red/brightness";
    static String SCAN_LED = "/sys/class/leds/scan-red/brightness";
    static String SQ45S_SCAN_LED = "/sys/class/leds/scan-blue/brightness";
    static String SQ53H_SCAN_LED = "sys/class/leds/scanner_led_red/brightness";
    final byte[] LIGHT_ON_SQ45S = {'4'};
    final byte[] LIGHT_OFF_SQ45S = {'5'};
    final byte[] LIGHT_ON_SQ51FW = {'3'};
    final byte[] LIGHT_OFF_SQ51FW = {'2'};
    static String SCAN_LED_SQ51FW = "/sys/bus/i2c/drivers/AW2013_LED/2-0045/led";
    private static String SQ81_led_left_red = "/sys/class/leds/scan_red/brightness";
    private class NotificationHandler extends Handler {
        public static final int MESSAGE_GOOD_READ = 1;
        public static final int MESSAGE_STOP_LED_NOTIFICATIONS = 2;

        private final Object goodReadEvent = new Object();
        private boolean goodReadNotified = false;
        private goodReadNotifyThread goodReadNotify = new goodReadNotifyThread();

        public NotificationHandler(Looper loop) {
            super(loop);
            goodReadNotify.start();
        }

        private class goodReadNotifyThread extends Thread {

            @Override
            public void run() {
                while (true) {
                    synchronized (goodReadEvent) {
                        while (!goodReadNotified) {
                            try {
                                goodReadEvent.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        goodReadNotified = false;
                    }

                    try {
                        /* Enable leds */
                        if (Build.PROJECT.equals("SQ26TB")) {
                            setBrightness(SCAN_LED, LIGHT_ON_26TB);
                        } else if ("SQ53".equals(Build.PROJECT)) {
                            setBrightness(SCAN_LED_9, LIGHT_ON_26TB);
                        } else if ("SQ38".equals(Build.PROJECT)) {
                            setBrightness(SCAN_LED, LIGHT_ON_SQ38);
                        } else if ("SQ45".equals(Build.PROJECT)) {
                            setBrightness(SCAN_LED, LIGHT_ON_SQ45);
                        }else if ("SQ45S".equals(Build.PROJECT)) {
                            setBrightness(SQ45S_SCAN_LED, LIGHT_ON_SQ45S);
                        }else if ("SQ51FW".equals(Build.PROJECT)) {
                            setBrightness(SCAN_LED_SQ51FW , LIGHT_ON_SQ51FW);
                        } else if ("SQ81".equals(Build.PROJECT)) {
                            setBrightness(SQ81_led_left_red , LIGHT_ON_26TB);
                        } else if ("SQ53H".equals(Build.PROJECT)) {
                            setBrightness(SQ53H_SCAN_LED , LIGHT_ON_26TB);
                        } else {
                            setBrightness(SCAN_LED, LIGHT_ON);
                        }
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    /* Disable leds */
                    if ("SQ53".equals(Build.PROJECT)) {
                        setBrightness(SCAN_LED_9, LIGHT_OFF_9);
                    } else if ("SQ38".equals(Build.PROJECT)) {
                        setBrightness(SCAN_LED, LIGHT_OFF_SQ38);
                    } else if ("SQ45".equals(Build.PROJECT)){
                        setBrightness(SCAN_LED, LIGHT_OFF_SQ45);
                    }else if ("SQ45S".equals(Build.PROJECT)){
                        setBrightness(SQ45S_SCAN_LED, LIGHT_OFF_SQ45S);
                    }else if ("SQ51FW".equals(Build.PROJECT)) {
                        setBrightness(SCAN_LED_SQ51FW, LIGHT_OFF_SQ51FW);
                    } else if ("SQ81".equals(Build.PROJECT)) {
                        setBrightness(SQ81_led_left_red , LIGHT_OFF_9);
                    } else if ("SQ53H".equals(Build.PROJECT)) {
                        setBrightness(SQ53H_SCAN_LED , LIGHT_OFF_9);
                    } else {
                        setBrightness(SCAN_LED, LIGHT_OFF);
                    }
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_GOOD_READ:
                    goodReadFeedback();
                    break;
                case MESSAGE_STOP_LED_NOTIFICATIONS:
                    goodReadNotify.interrupt();
                    break;
            }
        }

        private void goodReadFeedback() {
            synchronized (goodReadEvent) {
                goodReadNotified = true;
                goodReadEvent.notify();
            }
        }
    }

    private class NotificationThread extends HandlerThread {
        private Looper myLooper;

        public NotificationThread(String name) {
            super(name);
            start();
            myLooper = this.getLooper();
        }

        public void startThread() {
            mNotificationHandler = new NotificationHandler(myLooper);
        }
    }
    
    //urovo add jinpu.lin 2019.04.30
    CacheHandler mCacheHandler;
    CacheThread mCacheThread;
    private class CacheHandler extends Handler {
        public static final int MESSAGE_CACHE_READ = 1;
        public static final int MESSAGE_STOP_CACHE_SEND = 2;

        private final Object cacheReadEvent = new Object();
        private boolean cacheReadNotified = false;
        private CacheNotifyThread cacheReadNotify = new CacheNotifyThread();

        public CacheHandler(Looper loop){
            super(loop);
            cacheReadNotify.start();
        }

       private class CacheNotifyThread extends Thread {
            @Override
            public void run() {
                while (true) {
                    synchronized (cacheReadEvent) {
                        while (!cacheReadNotified) {
                            try {
                                cacheReadEvent.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                       /* try {
                            Thread.sleep(280);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        //int outputstate= android.device.provider.Settings.System.getInt(mContentResolver, "text_state", 1);
                        Log.i(TAG,"####isoutput:"+isoutput);
                        if(isoutput){
                            cacheReadNotified = false;
                            Log.i(TAG, "no Focus......");
                            isoutput=true;
                            continue;
                        }
                        isoutput=true;
                        /*try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/
                        synchronized (cacheBarcode) {
                            int len = cacheBarcode.size();
                            
                            if (len != 0) {
                                int index = 0;
                                int tmpkey=0;   
                                int key=0;
                                String barcode = "";
                                if (cacheLimitEnable == 1) {
                                    SparseArray<String> cacheTemp = cacheBarcode.clone();
                                    Log.i(TAG,"cacheBarcode.clear:");
                                    cacheBarcode.clear();
                                    long endTime = System.currentTimeMillis();
                                    int endTS = (int) (endTime - currtentDateTimes);
                                    Log.i(TAG, "cacheBarcode endTs = [" + endTS + "]......len"  +len);
                                    for (int i = 0; i < len; i++) {
                                        key = cacheTemp.keyAt(i);
                                        if (endTS - key > cacheLimitTime *1000) {
                                        } else {
                                            if (index == 0) {
                                                barcode = cacheTemp.get(key, "");
                                                Log.i(TAG, "cacheBarcode index = [" +
                                                 key + "]......" + barcode);
                                                tmpkey=key;
                                                index = 1;
                                            } else if (index == 1) {
                                                String codeVal = cacheTemp.get(key, "");
                                                 Log.i(TAG, "cacheTemp = [" + key +
                                                "]......" + codeVal);
                                                cacheBarcode.put(key, codeVal);
                                            }
                                        }
                                    }
                                    cacheTemp.clear();
                                    cacheTemp = null;
                                } else {
                                    key = cacheBarcode.keyAt(index);
                                    tmpkey=key;
                                    barcode = cacheBarcode.get(key, "");
                                    Log.i(TAG, "cacheBarcode = [" + key + "]......" +barcode);
                                    cacheBarcode.removeAt(index);
                                }
                                if (barcode.equals(""))
                                    continue;
                                Log.i(TAG,"urovobarcode:"+barcode+"   key:"+tmpkey);
                                WPHOutOfFocus(barcode,tmpkey);
                                try{
                                    sleep(60);
                                } catch(InterruptedException e) {
                                }
                                cacheReadNotified = false;
                                /*if (getPropertyInt(PropertyID.GOOD_READ_VIBRATE_ENABLE) == 1)
                                    VibratorControl(100);
                                int beep = getPropertyInt(PropertyID.GOOD_READ_BEEP_ENABLE);
                                if (beep != 0)
                                    playSound(beep);*/
                            } else {
                                cacheReadNotified = false;
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MESSAGE_CACHE_READ:
                isoutput=false;
                Log.i(TAG,"MESSAGE_CACHE_READ####isoutput:"+isoutput);
                cacheFeedback();
                break;
            case MESSAGE_STOP_CACHE_SEND:
                cacheReadNotify.interrupt();
                break;
            }
        }

        private void cacheFeedback() {
            synchronized (cacheReadEvent) {
                if(cacheReadNotified == true) {
                    Log.i(TAG, "cacheReadNotified......working");
                    return;
                }
                cacheReadNotified = true;
                cacheReadEvent.notify();
            }
        }
    }

    private class CacheThread extends HandlerThread {
        private Looper myLooper;
        public CacheThread(String name) {
            super(name);
            start();
            myLooper = this.getLooper();
        }

        public void startThread() {
            mCacheHandler = new CacheHandler(myLooper);
        }
    }
    //urovo add end 2019.04.30

    private void injectKeyEvent(KeyEvent event) {
        InputManager.getInstance().injectInputEvent(event,
                InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
    }

    private long mEventThrottle = 0;

    private void doEventThrottle() {
        try {
            Thread.sleep(mEventThrottle);
        } catch (InterruptedException e) {
        }
    }

    private void sendKeyEvent(int keyCode) {
        long now = SystemClock.uptimeMillis();
        injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
        injectKeyEvent(new KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0, InputDevice.SOURCE_KEYBOARD));
    }

    private String removeNonPrintable(String barcode) {
        if (barcode == null) return "";
        byte[] data = barcode.getBytes();
        int len = data.length;
        Log.d(TAG, "removeNonPrintable  len=" + len);
        byte[] temp = new byte[len];
        int index = 0;
        for (int i = 0; i < len; i++) {
            if (0x00 <= data[i] && data[i] < 0x20) {
                if (data[i] == 0x0a || data[i] == 0x08 || data[i] == 0x09 || data[i] == 0x0d) {
                    if (data[i] == 0x0d) {
                        temp[index++] = 0x0a;
                    } else {
                        temp[index++] = data[i];
                    }
                }
            } else {
                temp[index++] = data[i];
            }
        }
        Log.d(TAG, "removeNonPrintable=" + index);
        if (index != len) {
            data = null;
            data = new byte[index];
            System.arraycopy(temp, 0, data, 0, index);
            Log.d(TAG, "removeNonPrintable=" + index);
            return (new String(data));
        } else {
            return barcode;
        }
    }

    private void sendKeyEvent(String text) {

        String buff = removeNonPrintable(text);
        //Log.d(TAG, "sendKeyEvent = " + buff + "   length=" +buff.length());
        char[] chars = buff.toCharArray();

        KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
        KeyEvent[] events = kcm.getEvents(chars);
        if (events != null) {
            Log.d(TAG, "KeyEvent[] " + events.length);
            for (int i = 0; i < events.length; i++) {
                injectKeyEvent(events[i]);
                if(mEventThrottle > 0) {
                    doEventThrottle();
                }
            }
        }
    }

    /**
     * 模拟Ctrl+A全选
     * 模拟Ctrl+V执行粘贴
     */
    private void pasteClipboard(boolean isClip, int appendEnter) {
        //key Ctrl_Left down
        if(barcodeEditorMode == 1) {
            KeyEvent event = new KeyEvent(System.currentTimeMillis(), System.currentTimeMillis(),
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT, 0, KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 29, 0x8 | KeyEvent.FLAG_FROM_SYSTEM, 0x101);
            injectKeyEvent(event);
            //key A down
            event = new KeyEvent(System.currentTimeMillis(), System.currentTimeMillis(),
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A, 0, KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON,
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 47, 0x8 | KeyEvent.FLAG_FROM_SYSTEM, 0x101);
            injectKeyEvent(event); 
        }
        if(isClip) {
            KeyEvent event = new KeyEvent(System.currentTimeMillis(), System.currentTimeMillis(),
                KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_CTRL_LEFT, 0, KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 29, 0x8 | KeyEvent.FLAG_FROM_SYSTEM, 0x101);
            injectKeyEvent(event);
            //key V down
            event = new KeyEvent(System.currentTimeMillis(), System.currentTimeMillis(),
                    KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_V, 0, KeyEvent.META_CTRL_ON | KeyEvent.META_CTRL_LEFT_ON,
                    KeyCharacterMap.VIRTUAL_KEYBOARD, 47, 0x8 | KeyEvent.FLAG_FROM_SYSTEM, 0x101);
            injectKeyEvent(event);
            if (appendEnter == 1) {
                sendKeyEvent(KeyEvent.KEYCODE_ENTER);
            } else if(appendEnter == 3) {
                sendKeyEvent(KeyEvent.KEYCODE_TAB);
            }
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
                    Settings.System.putInt(mContentResolver, key, value);
                    break;
                case MESSAGE_SEND_KEYEVENT:
                    int keycode = msg.arg1;
                    sendKeyEvent(keycode);
                    break;
                case MESSAGE_SOFT_DECODE:
                    int state = msg.arg1;
                    // urovo add by shenpidong begin 2020-10-16
                    if (mScanner == null) {
                        Log.d(TAG , "handleMessage , Scanner:" + mScanner + ",state:" + state);
                        return;
                    }
                    // urovo add by shenpidong end 2020-10-16
                    if (state == 1)
                        mScanner.startDecode(1);
                    else
                        mScanner.stopDecode();
                    break;
                case MESSAGE_DELAYED_RESET_SCANNER:
                    if (mScanner != null) {
                        if (scanType == ScannerFactory.TYPE_SE4500 || scanType == ScannerFactory.TYPE_SE2100 || scanType == ScannerFactory.TYPE_SE4710 
                            || scanType == ScannerFactory.TYPE_SE4850 || scanType == ScannerFactory.TYPE_SE4770) {
                            //mScanner.setProperties(mCachePropertys);
                        } else if (scanType == ScannerFactory.TYPE_N3680 || scanType ==
                                ScannerFactory.TYPE_HONYWARE) {
                            boolean reset = SystemProperties.getBoolean("persist.sys.scanner" +
                                    ".reset", false);
                            Log.d(TAG, "persist.sys.scanner.reset 3680 " + reset);
                            if (false == reset) {
                                ScanNative.dohonywareset();
                                SystemProperties.set("persist.sys.scanner.reset", "true");
                            }
                        } else {
                            mScanner.setProperties(null);
                        }
                    }
                    break;
                case MESSAGE_DELAYED_CONFIG_SCANNER:
                    if (mScanner != null) {
                        SparseArray<Integer> mTempMap = (SparseArray<Integer>) msg.obj;
                        if (mTempMap != null && mTempMap.size() > 0)
                            mScanner.setProperties(mTempMap);
                    }
                    break;
                case MESSAGE_RELEASE_WAKELOCK:
                    int lock = msg.arg1;
                    if (lock == 1)
                        mWakeLock.acquire();
                    else
                        mWakeLock.release();
                    break;
                // urovo add shenpidong begin 2019-03-15
                case MESSAGE_DELAYED_OPEN_SCANNER:
                    Log.d(TAG , "MESSAGE_DELAYED_OPEN_SCANNER , startScanService" );
                    startScanService();
                    break;
                // urovo add shenpidong end 2019-03-15
            }
        }
    }

    // urovo add wujinquan begin 2020-02-25
    private void setBacklightEnabled(boolean enable){
        FileOutputStream f = null;
        byte[] LIGHT_ON = { '1', '0', '2' };
        try{
            f = new FileOutputStream("/sys/class/leds/lcd-backlight/brightness");
            f.write(enable?LIGHT_ON:"0".getBytes());
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(f != null){
                try {
                    f.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // urovo add wujinquan end 2019-02-25

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

    // sync with database
    private void LoadPara(String key) {
        String tmp = "Settings.System." + key;
        try {
            mParaSettings.put(key, Settings.System.getInt(mContentResolver, tmp));
        } catch (SettingNotFoundException e) {
            // TODO: handle exception
            Log.i(TAG, tmp + " is not exit");
        }

    }

    private void LoadPara(String key, int value) {
        mParaSettings.put(key, value);
    }

    private void LoadParas() {
//      LoadPara("SCANER_KEY");
        LoadPara("SCANER_VIB");
        LoadPara("SCANER_SOUND");
        LoadPara("SCANER_ENTER");
        LoadPara("SCANER_MODE");
        LoadPara("SCANER_POWER");
        LoadPara("SCANER_TYPE");
        LoadPara("SCANER_PHONE");
    }

    private int GetPara(String key) {
        int tmp = 0;
        try {
            tmp = mParaSettings.get(key);
        } catch (NullPointerException e) {
            // TODO: handle exception
//          Log.i(TAG,"NullPointerException");
        }
        return tmp;
    }

    // aidl interface
    @Override
    public void close() {
        Log.i(TAG, "close start......mIsScannerOpend:" + mIsScannerOpend + ",canner:" + (mScanner != null));
        android.provider.Settings.System.putInt(mContext.getContentResolver(), "urovo_scan_stat", 0);

        if (mScanner == null || mIsScannerOpend == false) {
            return;
        }
        long id = Binder.clearCallingIdentity();
        // urovo add shenpidong begin 2019-03-15
        mHandler.removeMessages(MESSAGE_DELAYED_OPEN_SCANNER);
        mHandler.removeMessages(MESSAGE_DELAYED_RESET_SCANNER);
        // urovo add shenpidong end 2019-03-15
        // zhouxin
        if (mWakeLock != null && mScanning) {
            Message m = Message.obtain(mHandler, MESSAGE_RELEASE_WAKELOCK);
            m.arg1 = 0;
            mHandler.sendMessage(m);
        }
        mScanning = false;
        // urovo add shenpidong begin 2019-04-12
        mIsOpenCameraAndCloseScanner = false;
        mTopActivityIsCameraAndCloseScanner = false;
        // urovo add shenpidong end 2019-04-12
        if (mIsScannerOpend == true) {
            mIsScannerOpend = false;
            mScanner.close();
        }

        writeConfig("SCANER_POWER", 0);
        SystemProperties.set("service.scan.enable", "0");
        Binder.restoreCallingIdentity(id);
/*
        if (mNotificationThread != null) {
            mNotificationThread.interrupt();
            mNotificationThread = null;
        }
*/

    }

    // aidl interface
    @Override
    public void closeScannerByCamera() {
        Log.i(TAG, "closeScannerByCamera start......mIsScannerOpend:" + mIsScannerOpend + ",canner:" + (mScanner != null));

        if (mScanner == null || mIsScannerOpend == false) {
            return;
        }
        if (mWakeLock != null && mScanning) {
            Message m = Message.obtain(mHandler, MESSAGE_RELEASE_WAKELOCK);
            m.arg1 = 0;
            mHandler.sendMessage(m);
        }
        // urovo add shenpidong begin 2019-03-15
        mHandler.removeMessages(MESSAGE_DELAYED_OPEN_SCANNER);
        mHandler.removeMessages(MESSAGE_DELAYED_RESET_SCANNER);
        // urovo add shenpidong end 2019-03-15
        mScanning = false;
        if (mIsScannerOpend == true) {
            mIsScannerOpend = false;
            mScanner.close();
        }

        writeConfig("SCANER_POWER", 0);
        SystemProperties.set("service.scan.enable", "0");
/*
        if (mNotificationThread != null) {
            mNotificationThread.interrupt();
            mNotificationThread = null;
        }
*/

    }

    @Override
    public boolean open() {
        Log.i(TAG, "open start......mIsScannerOpend:" + mIsScannerOpend + ",bootCompleted:" + bootCompleted);
        if(android.os.SystemProperties.get("persist.sys.scanner", "true").equals("false")){
            //UTE禁用扫描头
            Log.i(TAG, "UTE disable scanner");
            return false;
        }
        android.provider.Settings.System.putInt(mContext.getContentResolver(), "urovo_scan_stat", 1);

        if (mScanner == null) {
            Log.i(TAG, "Did not find the scanner");
            return false;
        }
        if (mIsScannerOpend) {
            Log.i(TAG, "scanner already open");
            return mIsScannerOpend;
        }
        // urovo add shenpidong begin 2019-03-15
        mHandler.removeMessages(MESSAGE_DELAYED_OPEN_SCANNER);
        mHandler.removeMessages(MESSAGE_DELAYED_RESET_SCANNER);
        // urovo add shenpidong end 2019-03-15
            long id = Binder.clearCallingIdentity();
            if(!mScanner.open()){
                Log.i(TAG, "scanner open failed!!!");
                Binder.restoreCallingIdentity(id);
                return false;
            }
        // urovo add shenpidong begin 2019-07-11
        if(!bootCompleted) {
            lockHwTriggler(false);
        }
        // urovo add shenpidong end 2019-07-11
           
        mIsScannerOpend = true;
        // urovo add shenpidong begin 2019-04-12
        mIsOpenCameraAndCloseScanner = false;
        mIsOpenCameraAndUse = false;
        mTopActivityIsCameraAndCloseScanner = false;
        // urovo add shenpidong end 2019-04-12
        // zhouxin
        if (mWakeLock != null && mScanning) {
            Message m = Message.obtain(mHandler, MESSAGE_RELEASE_WAKELOCK);
            m.arg1 = 0;
            mHandler.sendMessage(m);
        }
        mScanning = false;
        writeConfig("SCANER_POWER", 1);
        if (scanType == ScannerFactory.TYPE_SE4500 || scanType == ScannerFactory.TYPE_SE2100 || scanType == ScannerFactory.TYPE_SE4710
            || scanType == ScannerFactory.TYPE_SE4850 || scanType == ScannerFactory.TYPE_SE4770) {
            SystemProperties.set("service.scan.enable", "1");
        }
        // urovo add shenpidong begin 2019-09-12
        if (scanType == ScannerFactory.TYPE_SE4500 || scanType == ScannerFactory.TYPE_SE2100 || scanType == ScannerFactory.TYPE_N3680 ||
                scanType == ScannerFactory.TYPE_HONYWARE || scanType == ScannerFactory.TYPE_SE4710 || scanType == ScannerFactory.TYPE_SE4850 ||
                scanType == ScannerFactory.TYPE_SE4770) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_DELAYED_RESET_SCANNER, 300);
        } else if (scanType == ScannerFactory.TYPE_N6603 || scanType == ScannerFactory.TYPE_N6703) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_DELAYED_RESET_SCANNER, 300);
        }
        // urovo add shenpidong end 2019-09-12
        Binder.restoreCallingIdentity(id);
        Log.i(TAG, "open end......mIsScannerOpend:" + mIsScannerOpend);
        return mIsScannerOpend;
    }
    
    public boolean isOpen() {
        return mIsScannerOpend;
    }
    

    // urovo add shenpidong begin 2019-04-12
    @Override
    public synchronized int readConfig(String name) {
    // Exception : CursorWindow: Could not create CursorWindow from Parcel due to error -24, process fd count=0
    // add synchronized, for Cursor window could not be created from binder.
        Log.i(TAG, "readConfig: " + this + " :" + name + ",boot Completed:" + bootCompleted);
        if (name.equals("SCANER_TYPE")) {
            // urovo add shenpidong begin 2019-06-05
            if(bootCompleted && scanType > ScannerFactory.TYPE_MIN && scanType <= ScannerFactory.TYPE_MAX) {
            // urovo add shenpidong end 2019-06-05
                Log.i(TAG, "readConfig scan type = " + scanType);
                return scanType;
            } else {
                int type = android.device.provider.Settings.System.getInt(mContentResolver, android
                            .device.provider.Settings.System.SCANNER_TYPE, 0);
                Log.i(TAG, "readConfig type = " + type);
                return type;
            }
        } else if (name.equals("SCANER_POWER")) {
            // urovo add shenpidong begin 2019-06-05
            if(bootCompleted && (scanPower == 0 || scanPower == 1)) {
            // urovo add shenpidong end 2019-06-05
                Log.i(TAG, "readConfig scan ret = " + scanPower);
                return scanPower;
            } else {
                int ret = android.device.provider.Settings.System.getInt(mContentResolver, android
                        .device.provider.Settings.System.SCANNER_ENABLE, 0);
                Log.i(TAG, "readConfig ret = " + ret);
                return ret;
            }
        //urovo add jinpu.lin 2019.04.30
        }else if(name.equals(ScanManager.CACHE_LIMIT_ENABLE)) {
            return cacheLimitEnable;
        } else if(name.equals(ScanManager.CACHE_LIMIT_TIME)) {
            return cacheLimitTime;
        } else if(name.equals(ScanManager.CACHE_ENABLE)) {
            return cacheEnable;
        }
        //urovo add end 2019.04.30
        return GetPara(name);
    }

    @Override
    public void writeConfig(String name, int value) {
        if (name.equals("SCANER_TYPE")) {
            Log.i(TAG, "writeConfig scan type " + value);
            // urovo add by shenpidong begin 2020-10-16
            // power close and open deal at app
            if (scanType != value) {
                if (mScanner != null) {
                    mScanner.close();
                    mScanner = null;
                }
                mScanner = ScannerFactory.createScanner(value, this);
                Log.i(TAG, "writeConfig createScanner = " + mScanner);
                if (mScanner != null) {
                    android.device.provider.Settings.System.putInt(mContentResolver, android.device
                            .provider.Settings.System.SCANNER_TYPE, value);
                    scanType = value;
                    int power = readConfig("SCANER_POWER");
                    scanPower = power;
                    if (power == 1) {
                        //SE4500 and SE4750 are the same type of scan, and the current is SE4750
                        if (scanType == ScannerFactory.TYPE_SE4500 || scanType == ScannerFactory.TYPE_SE2100 || scanType == ScannerFactory.TYPE_SE4710 
                            || scanType == ScannerFactory.TYPE_SE4850 || scanType == ScannerFactory.TYPE_SE4770) {
                            SystemProperties.set("service.scan.enable", "1");
                        }
                        PowerSet(1);
                        mIsScannerOpend = true;
                        if (mNotificationThread == null) {
                            mNotificationThread = new NotificationThread("LEDnotification");
                            mNotificationThread.startThread();
                        }
                    }
                } else {
                    Log.i(TAG, "scan type " + value + " not exit");
                    if (scanType>=ScannerFactory.TYPE_MIN && scanType<=ScannerFactory.TYPE_MAX) {
                        SetScanType(scanType);
//                        android.device.provider.Settings.System.putInt(mContentResolver, android.device
//                            .provider.Settings.System.SCANNER_TYPE, scanType);
                    }
                }
            }
        // urovo add by shenpidong end 2020-10-16
        } else if (name.equals("SCANER_POWER")) {
            if(scanPower != value) {
                android.device.provider.Settings.System.putInt(mContentResolver, android.device
                            .provider.Settings.System.SCANNER_ENABLE, value);
                scanPower = value;
            }
        } else if (name.equals("softdecode")) {
            Message m = Message.obtain(mHandler, MESSAGE_SOFT_DECODE);
            m.arg1 = value;
            m.obj = name;
            mHandler.sendMessage(m);
        //urovo add jinpu.lin 2019.04.30
        }else if(name.equals(ScanManager.CACHE_LIMIT_ENABLE)) {
            android.device.provider.Settings.System.putInt(mContentResolver,  ScanManager.CACHE_LIMIT_ENABLE, value);
            cacheBarcode.clear();
            cacheLimitEnable = value;
        } else if(name.equals(ScanManager.CACHE_LIMIT_TIME)) {
            android.device.provider.Settings.System.putInt(mContentResolver, ScanManager.CACHE_LIMIT_TIME, value);
            cacheBarcode.clear();
            cacheLimitTime = value;
        } else if(name.equals(ScanManager.CACHE_ENABLE)) {
            android.provider.Settings.System.putInt(mContentResolver, ScanManager.CACHE_ENABLE, value);
            cacheBarcode.clear();
            cacheEnable = value;
            //urovo add end 2019.04.30
        } else {
            LoadPara(name, value);
            ConfigDispatch(name, value);
            Message m = Message.obtain(mHandler, MESSAGE_CONFIG_CHANGED);
            m.arg1 = value;
            m.obj = name;
            mHandler.sendMessage(m);
        }
    }
    // urovo add shenpidong end 2019-04-12

    private void ConfigDispatch(String name, int value) {
        Log.i(TAG, "ConfigDispatch......" + name);
        if (name.equals("SCANER_TYPE"))
            SetScanType(value);
    }

    @Override
    public String readConfigs(String name, String value) {
        if (name.equals("SCANER_PHONE")) {
            return String.valueOf(android.device.provider.Settings.System.getInt
                    (mContentResolver, android.device.provider.Settings.System
                            .IMAGE_PICKLIST_MODE, 0));
        } else if (name.equals("SCANER_SN")) {
            String SN = android.device.provider.Settings.System.getString(mContentResolver,
                    android.device.provider.Settings.System.SCANNER_SN);
            if (SN == null || SN.equals("")) {
                String lname = "Settings.System." + name;
                try {
                    return Settings.System.getString(mContentResolver, lname);
                } catch (Exception e) {
                    // TODO: handle exception
                    return "0";
                }
            } else {
                return SN;
            }
        } else {
            String lname = "Settings.System." + name;
            try {
                return Settings.System.getString(mContentResolver, lname);
            } catch (Exception e) {
                // TODO: handle exception
                return "0";
            }
        }
    }

    @Override
    public String writeConfigs(String name, String value) {
        String lname = "Settings.System." + name;
        Settings.System.putString(mContentResolver, lname, value);
/*      LoadPara(name, Integer.valueOf(value));*/
        ConfigDispatch(name, value);
        return null;
    }

    private void ConfigDispatch(String name, String value) {
        Log.i(TAG, "ConfigDispatch......" + name + "  " + value);
        if (name.equals("SCANER_PHONE")) {
            if (value.equals("1")) {
                mCachePropertys.put(PropertyID.IMAGE_PICKLIST_MODE, 1);
                mScanner.openPhoneMode();
                android.device.provider.Settings.System.putInt(mContentResolver, android.device
                        .provider.Settings.System.IMAGE_PICKLIST_MODE, 1);
            } else {
                mCachePropertys.put(PropertyID.IMAGE_PICKLIST_MODE, 0);
                mScanner.closePhoneMode();
                android.device.provider.Settings.System.putInt(mContentResolver, android.device
                        .provider.Settings.System.IMAGE_PICKLIST_MODE, 0);
            }
        } else if (name.equals("SCANER_SN")) {
            android.device.provider.Settings.System.putString(mContentResolver, android.device
                    .provider.Settings.System.SCANNER_SN, value);
        }
    }

    // scan reselect at runtime
    private void SetScanType(int type) {
        Log.i(TAG, "SetScanType......" + type);
        // power close and open deal at app
        mScanner = ScannerFactory.createScanner(type, this);
        if (mScanner != null) {
            int power = readConfig("SCANER_POWER");
            // urovo add shenpidong begin 2019-04-12
            scanPower = power;
            // urovo add shenpidong end 2019-04-12
            if (power == 1)
                PowerSet(1);
        } else {
            Log.i(TAG, "scan type " + type + " not exit");
        }
    }

    private int mSendTokensOption = 0;
    private int mSendTokensFormat = 0;
    //分隔条码数据与UDI格式换数据
    private int mSendTokensSeparator = 0;
    private int mFormatUDIDate = 0;
    private static int UDI_GS1 = 0x0002;
    private static int UDI_HIBCC = 0x0004;
    private static int UDI_ICCBBA = 0x0008;
    private static int UDI_MA = 0x0010;
    private static int UDI_AHM = 0x0020;
    private int mParserUDICodes = UDI_GS1 | UDI_HIBCC | UDI_ICCBBA | UDI_MA | UDI_AHM;
    private final String[] codingName = new String[]{"UTF-8","GBK", "BIG5", "Shift_JIS", "ISO-8859-15", "US-ASCII", "UTF-16", "UTF-16BE", "UTF-16LE"};
    private String codingFormatName = "";
    //UDI 数据解析输出
    private boolean smartDataParser(byte[] barcode, int barcodelen, byte barcodeType, byte[] aim) {
        if(mSendTokensOption > 0 && aim != null) {
            //Log.i(TAG,"smartDataParser   mSendTokensOption:"+mSendTokensOption +"  aim:"+(new String(aim)));
            InputData inputData = null;
            String DFI = "";
            if((mParserUDICodes&UDI_HIBCC) !=0 && barcode[0] == '+') {
                //code128 code39 aztec datamatrix qrcode
                if(((aim[1] == 'Q' && aim[2] == '1')
                        || (aim[1] == 'Q' && aim[2] == '2')
                        ||(aim[1] == 'd' && aim[2] == '1')
                        ||(aim[1] == 'C' && aim[2] == '0')
                        ||(aim[1] == 'A' && aim[2] == '4')
                        ||(aim[1] == 'z' && aim[2] == '3')
                        ||(aim[1] == 'z' && aim[2] == '0'))) {
                    DFI = "HIBCC";
                    inputData = new InputData("HIBCC");
                    ArrayList<byte[]> data = new ArrayList<>();
                    data.add(barcode);
                    inputData.setData(data);
                }
            } else if((mParserUDICodes&UDI_MA) !=0 && barcodelen >= 3 && (barcode[0] == 'M' && barcode[1] == 'A' && barcode[2] == '.')) {
                // datamatrix qrcode
                if(((aim[1] == 'Q' && aim[2] == '1')
                        || (aim[1] == 'Q' && aim[2] == '2')
                        ||(aim[1] == 'd' && aim[2] == '1'))) {
                    DFI = "MA";
                    inputData = new InputData("MA");
                    ArrayList<byte[]> data = new ArrayList<>();
                    data.add(barcode);
                    inputData.setData(data);
                }
            } else if((mParserUDICodes&UDI_ICCBBA) !=0 && (barcode[0] == '=' || barcode[0] == '&')) {
                if(((aim[1] == 'C' && aim[2] == '0')
                        || (aim[1] == 'C' && aim[2] == '1')
                        || (aim[1] == 'C' && aim[2] == '4')
                        ||(aim[1] == 'd' && aim[2] == '1'))) {
                    DFI = "ICCBBA";
                    inputData = new InputData("ICCBBA");
                    ArrayList<byte[]> data = new ArrayList<>();
                    data.add(barcode);
                    inputData.setData(data);
                }
            } else if((mParserUDICodes&UDI_GS1) !=0 &&((aim[1] == 'C' && aim[2] == '1')
                    || (aim[1] == 'e' && aim[2] == '0')
                    || (aim[1] == 'Q' && aim[2] == '3')
                    ||(aim[1] == 'd' && aim[2] == '2'))) {
                DFI = "GS1";
                inputData = new InputData("GS1");
                ArrayList<byte[]> data = new ArrayList<>();
                data.add(barcode);
                inputData.setData(data);
            } else if((mParserUDICodes&UDI_AHM) !=0 && barcodelen == 20 && barcode[0] == '8' && ((aim[1] == 'C' && aim[2] == '0'))) {
                DFI = "AHM";
                inputData = new InputData("AHM");
                ArrayList<byte[]> data = new ArrayList<>();
                data.add(barcode);
                inputData.setData(data);
            }
            if(inputData != null) {
                try{
                    SmartDataParser mSmartDataParser = new SmartDataParser();
                    TokenizedData parse = mSmartDataParser.parse(inputData);
                    String udiDataResult = "";
                    if(parse != null) {
                        if(mSendTokensFormat == 1) {
                            udiDataResult = mSmartDataParser.convertersToXML(DFI, null,(mFormatUDIDate ==1), parse);
                        } else if(mSendTokensFormat == 2) {
                            udiDataResult = mSmartDataParser.convertersToJSON(DFI, null, (mFormatUDIDate ==1),parse);
                        } else {
                            udiDataResult = mSmartDataParser.convertersToString(DFI, mSendTokensSeparator, (mFormatUDIDate ==1),parse);
                        }
                        //Log.i(TAG,"smartDataParser   udiDataResult:"+udiDataResult);
                        if(TextUtils.isEmpty(udiDataResult) == false) {
                            int coding = getPropertyInt(PropertyID.CODING_FORMAT);
                            StringBuilder textForWedge = new StringBuilder();
                            if(mSendTokensOption == 2) {
                                if(coding > 0 && coding <= 8) {
                                    try{
                                        textForWedge.append(new String(barcode, codingName[coding]));
                                    } catch(java.io.UnsupportedEncodingException e) {
                                        textForWedge.append(new String(barcode));
                                    }
                                } else {
                                    textForWedge.append(new String(barcode));
                                }
                                if (mSendTokensSeparator == 1|| mSendTokensSeparator == 2) {
                                    textForWedge.append("\n");
                                } else if(mSendTokensSeparator == 3) {
                                    textForWedge.append("\t");
                                }
                            }
                            textForWedge.append(udiDataResult);
                            int wedge = getPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE);
                            if (wedge == 1) {
                                int beep = getPropertyInt(PropertyID.GOOD_READ_BEEP_ENABLE);
                                if (beep != 0)
                                    playSound(beep);
                                if (getPropertyInt(PropertyID.GOOD_READ_VIBRATE_ENABLE) == 1)
                                    VibratorControl(100);

                                int appendEnter = getPropertyInt(PropertyID.LABEL_APPEND_ENTER);
                                //InputMethodService.java
                                int keyboard = getPropertyInt(PropertyID.WEDGE_KEYBOARD_TYPE);
                                if(keyboard == 2) {
                                    if (appendEnter == 1|| appendEnter == 2) {
                                        textForWedge.append("\n");
                                    } else if(appendEnter == 3) {
                                        textForWedge.append("\t");
                                    }
                                    sendKeyEvent(textForWedge.toString());//模拟按键必须移除\t \r以外的非打印字符
                                } else {
                                    Intent intent = new Intent("ACTION_BARCODE_INPUTMETHOD");
                                    intent.putExtra("suffixMode", appendEnter);
                                    intent.putExtra("editorMode", barcodeEditorMode);
                                    intent.putExtra("Barcode", textForWedge.toString());
                                    mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                                }
                            } else {
                                int beep = getPropertyInt(PropertyID.SEND_GOOD_READ_BEEP_ENABLE);
                                if (beep != 0)
                                    playSound(beep);
                                if (getPropertyInt(PropertyID.SEND_GOOD_READ_VIBRATE_ENABLE) == 1)
                                    VibratorControl(100);
                                Intent intent = new Intent(wedgeIntentAction);
                                intent.putExtra("UDIMode", true);
                                intent.putExtra("sendTokensFormat", mSendTokensFormat);
                                intent.putExtra(wedgeIntentLable, textForWedge.toString());
                                intent.putExtra(wedgeIntentRaw, barcode);
                                intent.putExtra(ScanManager.BARCODE_LENGTH_TAG, barcodelen);
                                intent.putExtra(ScanManager.BARCODE_TYPE_TAG, barcodeType);
                                intent.putExtra("DecodeNum", mDeviceDecodeNum);
                                mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                            }
                            return true;
                        }
                    }
                } catch (SmartDataParserException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void receivedMultipleDecodedData(final List<DecodeData> decodeDataArray) {
        if(decodeDataArray != null && decodeDataArray.size() > 0) {
            mDeviceDecodeNum = mDeviceDecodeNum + 1;
            synchronized (mNotificationHandler) {
                mNotificationHandler.removeMessages(NotificationHandler.MESSAGE_GOOD_READ);
                Message m = Message.obtain(mNotificationHandler, NotificationHandler.MESSAGE_GOOD_READ);
                mNotificationHandler.sendMessage(m);
            }
            int i = 0;
            int coding = getPropertyInt(PropertyID.CODING_FORMAT);
            int wedge = getPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE);
            if (wedge == 1) {
                int beep = getPropertyInt(PropertyID.GOOD_READ_BEEP_ENABLE);
                if (beep != 0)
                    playSound(beep);
                if (getPropertyInt(PropertyID.GOOD_READ_VIBRATE_ENABLE) == 1)
                    VibratorControl(100);
                StringBuilder textForWedge = new StringBuilder();
                for(DecodeData result: decodeDataArray) {
                    byte[] data = result.getBarcodeDataBytes();
                    if(coding > 0 && coding <= 8) {
                        try{
                            textForWedge.append(new String(data, codingName[coding]));
                        } catch(java.io.UnsupportedEncodingException e) {
                            textForWedge.append(new String(data));
                        }
                    } else {
                        textForWedge.append(new String(data));
                    }
                    if(++i < decodeDataArray.size()) {
                        textForWedge.append("\n");
                    }
                }
                int appendEnter = getPropertyInt(PropertyID.LABEL_APPEND_ENTER);
                 //InputMethodService.java
                int keyboard = getPropertyInt(PropertyID.WEDGE_KEYBOARD_TYPE);
                if(keyboard == 2) {
                    if (appendEnter == 1|| appendEnter == 2) {
                        textForWedge.append("\n");
                    } else if(appendEnter == 3) {
                        textForWedge.append("\t");
                    }
                    sendKeyEvent(textForWedge.toString());//模拟按键必须移除\t \r以外的非打印字符
                } else {
                    Intent intent = new Intent("ACTION_BARCODE_INPUTMETHOD");
                    intent.putExtra("suffixMode", appendEnter);
                    intent.putExtra("editorMode", barcodeEditorMode);
                    intent.putExtra("Barcode", textForWedge.toString());
                    mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
                }
            } else {
                int beep = getPropertyInt(PropertyID.SEND_GOOD_READ_BEEP_ENABLE);
                if (beep != 0)
                    playSound(beep);
                if (getPropertyInt(PropertyID.SEND_GOOD_READ_VIBRATE_ENABLE) == 1)
                    VibratorControl(100);
                String[] strResults = new String[decodeDataArray.size()];
                String[] hexResults = new String[decodeDataArray.size()];
                int[] strFormat = new int[decodeDataArray.size()];
                int[] codeId = new int[decodeDataArray.size()];
                int[] strResultslength = new int[decodeDataArray.size()];
                for(DecodeData result: decodeDataArray) {
                    //SendBroadcast(data, result.getSymbologyId(), result.getBarcodeDataLength());
                    strResultslength[i] = result.getBarcodeDataLength();
                    strFormat[i] = result.getSymbologyId();
                    codeId[i] = result.getCodeId();
                    byte[] data = result.getBarcodeDataBytes();
                    hexResults[i] = Utils.bytesToHexString(data);
                    if(coding > 0 && coding <= 8) {
                        try{
                            strResults[i] = new String(data, codingName[coding]);
                        } catch(java.io.UnsupportedEncodingException e) {
                            strResults[i] = new String(data);
                        }
                    } else {
                        strResults[i] = new String(data);
                    }
                    i++;
                }
                Intent intent = new Intent(wedgeIntentAction);
                intent.putExtra(wedgeIntentLable+"Lists", strResults);
                intent.putExtra(wedgeIntentLable+"HexLists", hexResults);
                intent.putExtra(ScanManager.BARCODE_LENGTH_TAG+"Lists", strResultslength);
                intent.putExtra(ScanManager.BARCODE_TYPE_TAG+"Lists", codeId);
                intent.putExtra("symbologyIdLists", strFormat);
                intent.putExtra("DecodeNum", mDeviceDecodeNum);
                mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
            }
        }
    }

    // barcode send
    public void SendBroadcast(byte[] barocode, byte barcodeType, int barcodelen, byte[] aim, long laserTriggerTime, long decodeSessionTime) {
        Log.i(TAG, "SendBoradcast......start");
        mDeviceDecodeNum = mDeviceDecodeNum + 1;

        Bundle results = new Bundle();
        results.putByteArray("BarcodeData", barocode);
        results.putInt("BarcodeType", (int)barcodeType);
        results.putInt("BarcodeLength", barcodelen);
        int N = mCallBacks.beginBroadcast();
        for (int i = 0; i < N; i++) {
            try {
                mCallBacks.getBroadcastItem(i).onScanResults(results);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mCallBacks.finishBroadcast();

        //a successful good decode
        String codeStr = "";
        int coding = getPropertyInt(PropertyID.CODING_FORMAT);
        //SE4500 and SE4750 are the same type of scan, and the current is SE4750
        if(coding > 0 && coding <= 8) {
            try{
                codeStr = new String(barocode, codingName[coding]);
            } catch(java.io.UnsupportedEncodingException e) {
                codeStr = new String(barocode);
            }
        } else if(coding == codingName.length) {
            try {
                int length = barocode.length;
                int[] temp_value = new int[length];
                for (int i = 0; i < length; i++) {
                    temp_value[i] = barocode[i] & 0xff;
                }
                //实现utf8orgb2312自动检查
                codeStr = ChineseHandle.chineseBarcode(temp_value);
                if(TextUtils.isEmpty(codeStr)) {
                    codeStr = new String(barocode);
                }
            } catch (Exception e) {
                e.printStackTrace();
                codeStr = new String(barocode);
            }
        } else if(coding == codingName.length + 1 && TextUtils.isEmpty(codingFormatName) == false) {
            try {
                //String[] codingLists = codingFormatName.split(",");
                codeStr = new String(barocode, codingFormatName);
            } catch (Exception e) {
                e.printStackTrace();
                codeStr = new String(barocode);
            }
        } else {
            codeStr = new String(barocode);
        }
        boolean udiMode = smartDataParser(barocode, barcodelen, barcodeType, aim);
        if(udiMode) {
            return;
        }
        if(android.os.Build.PWV_CUSTOM_CUSTOM.equals("JANAM")){
            int copystate= android.device.provider.Settings.System.getInt(mContext.getContentResolver(),"ClipData_enable", 0);
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            if(copystate==1){
                ClipData clipData = ClipData.newPlainText(null, codeStr); // 把数据集设置（复制）到剪贴板 
                clipboard.setPrimaryClip(clipData);
            }else{
                ClipData clipData = ClipData.newPlainText(null, ""); // 把数据集设置（复制）到剪贴板 
                clipboard.setPrimaryClip(clipData);
            }
        } else if(getPropertyInt(PropertyID.OUT_CLIPBOARD_ENABLE) == 1){
            ClipData clipData = ClipData.newPlainText(null, codeStr); // 把数据集设置（复制）到剪贴板
            clipboard.setPrimaryClip(clipData);
            int appendEnter = getPropertyInt(PropertyID.LABEL_APPEND_ENTER);
            pasteClipboard(true, appendEnter);
        }
        synchronized (mNotificationHandler) {
            mNotificationHandler.removeMessages(NotificationHandler.MESSAGE_GOOD_READ);
            Message m = Message.obtain(mNotificationHandler, NotificationHandler.MESSAGE_GOOD_READ);
            mNotificationHandler.sendMessage(m);
        }
        if(webjump == 1){
            if(codeStr.startsWith("http://") || codeStr.startsWith("https://")){
                Uri uri = Uri.parse(codeStr);
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setData(uri);
                try{
                    mContext.startActivity(intent);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (Build.PROJECT.equals("SQ26") && Build.PWV_CUSTOM_CUSTOM.equals("SF")) {
            Intent intent = new Intent("com.android.server.scannerservice.broadcast");
            intent.putExtra("scannerdata", codeStr);
            mContext.sendBroadcastAsUser(intent,UserHandle.ALL);

            if (getPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE) == 1) {
                int beep = getPropertyInt(PropertyID.GOOD_READ_BEEP_ENABLE);
                if (beep != 0)
                    playSound(beep);
                if (getPropertyInt(PropertyID.GOOD_READ_VIBRATE_ENABLE) == 1)
                    VibratorControl(100);
                int appendEnter = getPropertyInt(PropertyID.LABEL_APPEND_ENTER);
                sendScanDecodeDataToWindow(codeStr, appendEnter, 1);
            }
        } else {
            int wedgeMode = getPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE);
            if (wedgeMode == 1) {

                int beep = getPropertyInt(PropertyID.GOOD_READ_BEEP_ENABLE);
                if (beep != 0)
                    playSound(beep);
                if (getPropertyInt(PropertyID.GOOD_READ_VIBRATE_ENABLE) == 1)
                    VibratorControl(100);
                int type = getPropertyInt(PropertyID.SEND_LABEL_PREFIX_SUFFIX);
                if (type > 0) {
                    codeStr = buildLabelData(type, codeStr);
                }
                //urovo add jinpu.lin 2019.04.30
                //    OutOfFocus(codeStr);
                if (Build.PWV_CUSTOM_CUSTOM.equals("WPH")) {
                    Log.i(TAG, "SendBoradcast......");
                    if (getPropertyInt(PropertyID.GOOD_READ_VIBRATE_ENABLE) == 1)
                        VibratorControl(100);
         
                        String codeLabel = removeNonPrintable(codeStr);
                        if(cacheEnable == 1) {
                            synchronized(cacheBarcode) {
                                cacheStartTime = (int)((System.currentTimeMillis() - currtentDateTimes));
                                Log.d(TAG, "cacheStartTime " + cacheStartTime);
                                Log.d(TAG, "codeLabel :" + codeLabel);
                                cacheBarcode.put(cacheStartTime, codeLabel);
                                /*for(int index =0; index<cacheBarcode.size(); index++) {
                                    int key = cacheBarcode.keyAt(index);
                                    String barcode = cacheBarcode.get(key, "");
                                    Log.i(TAG, "cacheBarcode put= [" + key + "]......" + barcode);
                                }*/
                                Message m = Message.obtain(mCacheHandler, CacheHandler.MESSAGE_CACHE_READ);
                                mCacheHandler.sendMessage(m);
                            }
                        } else {
                                WPHOutOfFocus(codeLabel,0);  
                        }
                }else{
                    OutOfFocus(codeStr);
                }
                //urovo add end 2019.04.30
            } else {
                int beep = getPropertyInt(PropertyID.SEND_GOOD_READ_BEEP_ENABLE);
                if (beep != 0)
                    playSound(beep);
                if (getPropertyInt(PropertyID.SEND_GOOD_READ_VIBRATE_ENABLE) == 1)
                    VibratorControl(100);
                int type = getPropertyInt(PropertyID.SEND_LABEL_PREFIX_SUFFIX);
                if (type > 0) {
                    codeStr = buildLabelData(type, codeStr);
                }
                if (wedgeMode == 2) {
                    OutOfFocus(codeStr);
                }
                OutOfBroadcast(barocode, barcodeType, barcodelen);
                if (Build.PWV_CUSTOM_CUSTOM.equals("YTO")) {
                    Intent i = new Intent(ACTION_SCAN_DATA);
                    i.putExtra("data", barocode);
                    i.putExtra("length", barcodelen);
                    mContext.sendBroadcastAsUser(i,UserHandle.ALL);

                    Intent intent = new Intent(wedgeIntentAction);
                    if (type > 0) {
                        intent.putExtra(wedgeIntentRaw, codeStr.getBytes());
                        intent.putExtra(wedgeIntentLable, barocode);//yto外场程序　barcode_string，返回byte数组
                        intent.putExtra(ScanManager.BARCODE_LENGTH_TAG, codeStr.length());
                    } else {
                        intent.putExtra(wedgeIntentRaw, barocode);
                        intent.putExtra(wedgeIntentLable, barocode);
                        intent.putExtra(ScanManager.BARCODE_LENGTH_TAG, barcodelen);
                    }
                    intent.putExtra(ScanManager.BARCODE_TYPE_TAG, barcodeType);
                    mContext.sendBroadcastAsUser(intent,UserHandle.ALL);
                } else if (Build.PWV_CUSTOM_CUSTOM.equals("CP-42") || Build.PWV_CUSTOM_CUSTOM
                        .equals("CP-43")) {
                    Intent intent = new Intent("ACTION_BAR_SCAN");
                    intent.putExtra("EXTRA_SCAN_DATA", codeStr);
                    mContext.sendBroadcastAsUser(intent,UserHandle.ALL);
                } else {
                    Intent intent = new Intent(wedgeIntentAction);
                    if (type > 0) {
                        intent.putExtra(wedgeIntentRaw, codeStr.getBytes());
                        intent.putExtra(wedgeIntentLable, codeStr);
                        intent.putExtra(ScanManager.BARCODE_LENGTH_TAG, codeStr.length());
                    } else {
                        intent.putExtra(wedgeIntentRaw, barocode);
                        intent.putExtra(wedgeIntentLable, codeStr);
                        intent.putExtra(ScanManager.BARCODE_LENGTH_TAG, barcodelen);
                    }
                    intent.putExtra(ScanManager.BARCODE_TYPE_TAG, barcodeType);
                    int SymbologyId = 0;
                    if (scanType == ScannerFactory.TYPE_N6603 || scanType == ScannerFactory.TYPE_N6703 || scanType == ScannerFactory.TYPE_N4603) {
                        String symName = OEMSymbologyId.stringFromSymbologyType(OEMSymbologyId.HoneyWellEngine, barcodeType);
                        SymbologyId = OEMSymbologyId.getHSMSymbologyId(barcodeType);
                        intent.putExtra("codetype", symName);
                        intent.putExtra("symName",symName);
                    } else if (scanType == ScannerFactory.TYPE_SE2030){
                        String symName = OEMSymbologyId.stringFromSymbologyType(OEMSymbologyId.CommonEngine, barcodeType);
                        intent.putExtra("codetype", symName);
                        intent.putExtra("symName", symName);
                    } else {
                        String symName = OEMSymbologyId.stringFromSymbologyType(OEMSymbologyId.ZebraEngine, barcodeType);
                        SymbologyId = OEMSymbologyId.getZebraSymbologyId(barcodeType);
                        intent.putExtra("codetype", symName);
                        intent.putExtra("symName",symName);
                    }
                    intent.putExtra("symbologyId",SymbologyId);
                    intent.putExtra("laserTime", laserTriggerTime);
                    intent.putExtra("decodeTime", decodeSessionTime);
					intent.putExtra("DecodeNum", mDeviceDecodeNum);
                    mContext.sendBroadcastAsUser(intent,UserHandle.ALL);
                }
            }
        }
        Log.i(TAG, "SendBoradcast......end");
    }
    
    //urovo add jinpu.lin 2019.04.30
    private void WPHOutOfFocus(String codeStr,int key) {
        int appendEnter = getPropertyInt(PropertyID.LABEL_APPEND_ENTER);
        int keyboard = getPropertyInt(PropertyID.WEDGE_KEYBOARD_TYPE);
        if(keyboard == 2) {
            if (appendEnter == 1) {
                codeStr = codeStr + "\n";
            } else if(appendEnter == 3) {
                codeStr = codeStr + "\t";
            }
            sendKeyEvent(codeStr);//模拟按键必须移除\t \r以外的非打印字符
        } else if (keyboard == 3) {
            //InputMethodService.java
            Intent intent = new Intent("ACTION_BARCODE_INPUTMETHOD");
            intent.putExtra("suffixMode", appendEnter);
            intent.putExtra("Barcode", codeStr);
            intent.putExtra("editorMode", barcodeEditorMode);
            mContext.sendBroadcastAsUser(intent,UserHandle.ALL);
        } else {
            if(removeNonPrintChar == 1) {
                String codeLabel = removeNonPrintable(codeStr);
                WPHsendScanDecodeDataToWindow(codeLabel,key, appendEnter, keyboard);
            } else {
                WPHsendScanDecodeDataToWindow(codeStr,key,appendEnter, keyboard);
            }
        }
    }
    //urovo add end 2019.04.30
    // Deal with Scan Mode
    private void OutOfFocus(String codeStr) {
        int appendEnter = getPropertyInt(PropertyID.LABEL_APPEND_ENTER);
        int keyboard = getPropertyInt(PropertyID.WEDGE_KEYBOARD_TYPE);
        if(keyboard == 2) {
            if (appendEnter == 1) {
                codeStr = codeStr + "\n";
            }else if(appendEnter == 3) {
                codeStr = codeStr + "\t";
            }
            if(barcodeEditorMode == 1) {
                //全选文本框内容
                pasteClipboard(false, appendEnter);
            }
            sendKeyEvent(codeStr);//模拟按键必须移除\t \r以外的非打印字符
        } else if (keyboard == 3) {
            //InputMethodService.java
            Intent intent = new Intent("ACTION_BARCODE_INPUTMETHOD");
            intent.putExtra("editorMode", barcodeEditorMode);
            intent.putExtra("suffixMode", appendEnter);
            intent.putExtra("Barcode", codeStr);
            mContext.sendBroadcastAsUser(intent,UserHandle.ALL);
        } else {
            if(removeNonPrintChar == 1) {
                String codeLabel = removeNonPrintable(codeStr);
                sendScanDecodeDataToWindow(codeLabel, appendEnter, keyboard);
            } else {
                sendScanDecodeDataToWindow(codeStr, appendEnter, keyboard);
            }
        }
    }

    private void OutOfBroadcast(byte[] barocode, byte barcodeType,
                                int barcodelen) {
        Intent intent = new Intent("urovo.rcv.message");
        intent.putExtra("barocode", barocode);
        intent.putExtra("barcode", barocode);
        intent.putExtra("length", barcodelen);
        intent.putExtra("barcodeType", barcodeType);
        mContext.sendBroadcastAsUser(intent,UserHandle.ALL);
    }

    private void VibratorControl(int value) {
        if (mVibrator == null)
            mVibrator = (Vibrator) mContext
                    .getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(value);// ms
        Log.i(TAG, "VibratorControl......");
    }

    private void initSoundpool() {
        if (soundpool != null) {
            soundpool.release();
        }
    
        //  urovo add shenpidong begin 2019-04-30
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
    //          .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
    //          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
            soundpool = new SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build();
        } else {
            soundpool = new SoundPool(10, AudioManager.STREAM_NOTIFICATION, 0);
        }
        heightBeepId = soundpool.load("/etc/Scan_buzzer.ogg", 1);
        middleBeepId = soundpool.load("/etc/Scan_new.ogg", 1);
        soundpool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.i(TAG, "onLoadComplete ------ status:" + status + ",sampleId:" + sampleId + ",soundpool:" + (soundPool!=null));
                if(status == 0) {
                }
            }
        });
    //  urovo add shenpidong end 2019-04-30
    }

    private void playSound(int type) {
        Log.i(TAG, "PlaySound......");
        if (soundpool != null) {
        //  urovo add shenpidong begin 2019-04-30
            if (type == 1) {
                soundpool.stop(middleBeepId);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                soundpool.play(middleBeepId, 1, 1, 1, 0, 1);
            } else {
                soundpool.stop(heightBeepId);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                soundpool.play(heightBeepId, 1, 1, 1, 0, 1);
            }
        //  urovo add shenpidong end 2019-04-30
        }
    }

    public void sendScanDecodeDataToWindow(String data, int appendEnter, int keyborad) {
        try {
            //urovo modify jinpu.lin 2019.05.08
            //mWinManager.sendScanDataToFocus(data, appendEnter, keyborad);
            if(Build.PWV_CUSTOM_CUSTOM.equals("QCS")){
                    //int enterAddDelay = 0;
                    int[] get_id_buffer = { PropertyID.APPEND_ENTER_DELAY, PropertyID.CHARACTER_DATA_DELAY};
                    int[] get_value_buffer = new int[2];
                    int[] id_bad_buffer = new int[get_id_buffer.length];
                   // try {
                        getPropertyInts(get_id_buffer, get_id_buffer.length, get_value_buffer, get_value_buffer.length,
                                id_bad_buffer);
                        if(get_value_buffer[1] < 0)
                            get_value_buffer[1] = 0;
                        if(get_value_buffer[0] < 0)
                            get_value_buffer[0] = 0;
                        //扫描延迟
                       // if(get_value_buffer[1] > 0)
                       //     enterAddDelay = get_value_buffer[1]*(data.length() - 1);                     
                   // } catch (RemoteException e) {
                      //  Log.e(TAG,"Decoder error: in getPropertyInts");
                  //  }
                      if(get_value_buffer[0]==0&&get_value_buffer[1]==0){
                          mWinManager.sendScanDataToFocus(data, appendEnter, keyborad);
                      }else{
                          if(get_value_buffer[1] < 10 ) get_value_buffer[1] = 10;
                          sendText(data,get_value_buffer[1]);
                          if (appendEnter == 1) {
                              sendText("\n",get_value_buffer[0]);
                          }
                      }
            }else{
                mWinManager.sendScanDataToFocus(data, appendEnter, keyborad);
            }
            //urovo modify end 2019.05.08
        } catch (RemoteException e) {
            android.util.Log.e("ScanService", "sendScanDataToFocus");
        }
    }
    //urovo add jinpu.lin 2019.04.30
    public void WPHsendScanDecodeDataToWindow(String data,int key, int appendEnter, int keyborad) {
/*
        try {
            mWinManager.sendWPHScanDataToFocus(data,key,appendEnter, keyborad);
        } catch (RemoteException e) {
            android.util.Log.e("ScanService", "WPHsendScanDataToFocus");
        }
*/
    }
    //urovo add end 2019.04.30
    
     //urovo add jinpu.lin 2019.05.08
     private void sendText(String text,final int delay) {
         //Log.d(TAG , "sendText --------------------- start");
         if(text.equals("\n")){
             try{
                 Log.d(TAG,"1 Sleep " + delay);
                 Thread.sleep(delay);
             }catch(InterruptedException e) {
                 e.printStackTrace();
             }
             keyRemappingSendFakeKeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER);
             keyRemappingSendFakeKeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER);
             return;
         }

        StringBuffer buff = new StringBuffer(text);

        char[] chars = buff.toString().toCharArray();
        /*if(chars!=null) {
            Log.d(TAG , "chars.length:" + chars.length);
            for (int i = 0; i < chars.length; i++) {
                Log.d(TAG, "chars[" + i + "]:" + chars[i]);
            }
        }*/

        KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
        KeyEvent[] events = kcm.getEvents(chars);
        if (events != null) {
            Log.d(TAG , "events.length:" + events.length);
            for (int i = 0; i < events.length; i++) {
               /* Handler postHandler = new Handler();
                final int j = i;
                Runnable r = new Runnable() {
                            @Override
                            public void run() {
                               // seteventTime(delay*j);
                                injectKeyEventASYNC(events[j]);
                                Thread.sleep(delay);
                            }
                        };
                if(text.equals("/n"))
                    postHandler.postDelayed(r, delay);
                else
                    postHandler.postDelayed(r, delay*i);*/
                /*if(text.equals("\n")){
                    try{
                        Log.d(TAG,"1 Sleep " + delay + ",i:" + i + ",events[i]:" + events[i]);
                        Thread.sleep(delay);
                    }catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                    injectKeyEventASYNC(events[i]);
                }else{*/
                    injectKeyEventASYNC(events[i]);
                    if(i < events.length){
                        try{
                            Log.d(TAG,"2 Sleep " + delay /*+ ",i:" + i + ",events[i]:" + events[i]*/);
                            Thread.sleep(delay);
                       }catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                //doEventThrottle();
                }
            //}
        }
         //Log.d(TAG , "sendText --------------------- end");
    }


    private void keyRemappingSendFakeKeyEvent(int action, int keyCode) {
        long eventTime = SystemClock.uptimeMillis();
        KeyEvent keyEvent = new KeyEvent(eventTime, eventTime, action, keyCode, 0);
        InputManager inputManager = (InputManager) mContext.getSystemService(Context.INPUT_SERVICE);
        inputManager.injectInputEvent(keyEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }


    private void injectKeyEventASYNC(KeyEvent event) {
        //android.util.Log.i(TAG, "InjectKeyEvent: " + event);
        InputManager.getInstance().injectInputEvent(event,
                InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }
    //urovo add end 2019.05.08

    
    // urovo add shenpidong begin 2019-04-12
    // power save when screen off
    private void PowerSet(int onoff) {
        if(android.os.SystemProperties.get("persist.sys.scanner", "true").equals("false")){
            //UTE禁用扫描头
            return;
        }
        Log.i(TAG, "PowerSet onff......" + onoff + ",mScanner:" + mScanner + ",mIsScannerOpend:" + mIsScannerOpend);
        if (mScanner == null) {
            Log.i(TAG, "Do not find the scanner");
            return;
        }
        long id = Binder.clearCallingIdentity();
        mScanning = false;
        if (onoff == 1) {
            mIsOpenCameraAndUse = false;
            mIsScannerOpend = true;
            mScanner.open();
            //SE4500 and SE4750 are the same type of scan, and the current is SE4750
        // urovo add shenpidong begin 2019-09-12
            if (scanType == ScannerFactory.TYPE_SE4500 || scanType == ScannerFactory.TYPE_SE2100 || scanType == ScannerFactory.TYPE_N3680
                    || scanType == ScannerFactory.TYPE_HONYWARE || scanType == ScannerFactory.TYPE_SE4710 || scanType == ScannerFactory.TYPE_SE4850
                    || scanType == ScannerFactory.TYPE_SE4770) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_DELAYED_RESET_SCANNER, 300);
            } else if (scanType == ScannerFactory.TYPE_N6603 || scanType == ScannerFactory.TYPE_N6703) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_DELAYED_RESET_SCANNER, 300);
            }
        // urovo add shenpidong end 2019-09-12
        } else {
            mScanner.close();
            mIsScannerOpend = false;
            mIsOpenCameraAndCloseScanner = false;
        }
        mTopActivityIsCameraAndCloseScanner = false;
        Binder.restoreCallingIdentity(id);
        Log.i(TAG, "PowerSet onff......" + onoff + ",mIsScannerOpend:" + mIsScannerOpend);
    }
    // urovo add shenpidong end 2019-04-12
    
    // urovo add shenpidong begin 2019-07-11
    private String getTopActivity(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            Log.i(TAG , "getTopActivity , topActivity:" + (runningTaskInfos.get(0).topActivity).toString());
            return (runningTaskInfos.get(0).topActivity).toString();
        }
        return null;
    }


    private boolean topActivityIsCameraApp(Context context) {
        boolean isOpenCameraApp = false;
        String topActivityStr = getTopActivity(context);
        Log.i(TAG , "topActivityIsCameraApp , str:" + topActivityStr);
        if(topActivityStr!=null && (topActivityStr.contains(FACTORY_BACKCAMERA) || topActivityStr.contains(FACTORY_FRONTCAMERA) || topActivityStr.contains(QCOM_SNAPCAM_CAMERA))) {
            isOpenCameraApp = true;
        }
        return isOpenCameraApp;
    }
    // urovo add shenpidong end 2019-07-11
    
    // urovo add shenpidong begin 2019-07-21
    @Override
    public synchronized boolean screenTurnedOn(boolean on) {
        android.util.Log.d(TAG, "screenTurnedOn " + bootCompleted + ",mScanning:" + mScanning + ",on:" + on + ",Scan Open:" + mIsScannerOpend);
        if(mIsScannerOpend || (bootCompleted== false && scanType == ScannerFactory.TYPE_SE2030)) {
            return mIsScannerOpend;
        }
        int power = readConfig("SCANER_POWER");
        boolean turnOn = power == 1;
        if (mContext != null && (mWakeLock == null || !mScanning)) {
            mIsOpenCameraAndCloseScanner = topActivityIsCameraApp(mContext);
                android.util.Log.i(TAG, "screenTurnedOn , mIsOpenCameraAndCloseScanner:" + mIsOpenCameraAndCloseScanner + ",mTopActivityIsCameraAndCloseScanner:" + mTopActivityIsCameraAndCloseScanner);
            if (on  && turnOn && !mIsOpenCameraAndCloseScanner) {
                PowerSet(1);
                turnOn = true;
            } else {
                turnOn = false;
                mTopActivityIsCameraAndCloseScanner = mIsOpenCameraAndCloseScanner;
                android.util.Log.d(TAG, "onReceive , ACTION_SCREEN_ON power:" + power + ",Open Camera is:" + mIsOpenCameraAndCloseScanner);
            }
        }
        return turnOn && mIsScannerOpend;
    }
    // urovo add shenpidong end 2019-07-21

    private final class PowerChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int power = readConfig("SCANER_POWER");
            // urovo add shenpidong begin 2019-04-12
            scanPower = power;
            android.util.Log.d(TAG, "onReceive --- " + intent.getAction() + power + ",bootCompleted:" + bootCompleted + ",mScanning:" + mScanning);
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                // zhouxin
                if (mWakeLock == null || !mScanning) {
                    if (power == 1 && !mIsOpenCameraAndCloseScanner) {
                        PowerSet(1);
                    } else {
                        mTopActivityIsCameraAndCloseScanner = mIsOpenCameraAndCloseScanner;
                        android.util.Log.d(TAG, "onReceive , ACTION_SCREEN_ON power:" + power + ",Open Camera is:" + mIsOpenCameraAndCloseScanner);
                    }
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                bootCompleted = true;
                mTopActivityIsCameraAndCloseScanner = false;
                if (mWakeLock == null || !mScanning) {
                    if (power == 1)
                        PowerSet(0);
                }
            }
            else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                // urovo add shenpidong begin 2019-06-05
                bootCompleted = true;
                // urovo add shenpidong end 2019-06-05
                Date date = new Date();
                String daTime = df.format(date);
                try{
                    currtentDateTimes = df.parse(daTime).getTime();
                } catch(ParseException e) {
                    e.printStackTrace();
                }
                if (!mIsScannerOpend && (scanType == ScannerFactory.TYPE_SE2030 || scanType == ScannerFactory.TYPE_SE955)) {
                    if(mScanner == null) {
		        mScanner = ScannerFactory.createScanner(scanType, ScanServiceWrapper.this);
		    }
                    if (power == 1) {
                        open();
                    }
                }
                int suspensionButtonStatus = android.device.provider.Settings.System.getInt(mContext.getContentResolver(),
                             android.device.provider.Settings.System.SUSPENSION_BUTTON, 0); 
                updateSuspensionButtonStatus(suspensionButtonStatus);
            }else if(focusIntent.equals(intent.getAction())) {
                if(cacheEnable == 1 ) {
                    synchronized (cacheBarcode) {
                        int len = cacheBarcode.size();
                        if (len != 0) {
                            Message m = Message.obtain(mCacheHandler, CacheHandler.MESSAGE_CACHE_READ);
                            mCacheHandler.sendMessage(m);
                            }
                        }
                }
            }else if(outputIntent.equals(intent.getAction())) {
                String data=intent.getStringExtra("data");
                int time=intent.getIntExtra("time",0);
                Log.i(TAG,"outputIntent  data:"+data+"  time:"+time);
                if(!data.equals("Scan-data")){
                    synchronized (cacheBarcode) {
                        int len = cacheBarcode.size();
                        Log.i(TAG,"outputIntent start len:"+len);
                        cacheBarcode.put(time-1, data);
                        len = cacheBarcode.size();
                        Log.i(TAG,"outputIntent end len:"+len);
                    }
                }
                Log.i(TAG,"outputIntent isoutput:"+isoutput);
                isoutput=false;
            }
        //urovo add end 2019.04.30
        }
    }

    @Override
    public Map<String, Integer> getScanerList() {
        return ScannerFactory.getScanerList();
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
            //android.util.Log.i(TAG, "parse property key: " + key + ",value:" + value + ",get:" + mHashMap.get(key) + ",WEDGE_KEYBOARD_ENABLE:" + PropertyID.WEDGE_KEYBOARD_ENABLE);
            if (value == null) {
                android.util.Log.i("debug", "parse property: " + key + ",v:" + mHashMap.get(key));
                continue;
            }
            ContentValues values = new ContentValues();
            values.put(android.device.provider.Constants.KEY_NAME, mHashMap.get(key));
            values.put(android.device.provider.Constants.KEY_VALUE, value);
            currValues[currIndex++] = values;
            if (!isStringType(key)) {
                try {
                    if(!TextUtils.isEmpty(value)) {
                        mCachePropertys.put(key, Integer.valueOf(value));
                    }
                } catch (NumberFormatException e) {
            //android.util.Log.i(TAG, "parse property key: " + key + ",value:" + value + ",get:" + mHashMap.get(key));
                    if (value!=null && value.length()>0) {
                        try {
                            int val = value.charAt(0);
                            mCachePropertys.put(key, val);//StringIndexOutOfBoundsException
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                    } else {
                        --currIndex;
                        android.util.Log.i(TAG, "Exception parse property key: " + key + ",value:" + value);
                    }
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
                } else if (key == PropertyID.WEDGE_INTENT_DECODE_DATA_TAG){
                    wedgeIntentRaw = value;
                } else if (key == PropertyID.LABEL_MATCHER_PATTERN) {
                    mPattern = value;
                } else if(key == PropertyID.LABEL_MATCHER_TARGETREGEX) {
                    replaceRegex = value;
                } else if(key == PropertyID.LABEL_MATCHER_REPLACEMENT) {
                    replaceDst = value;
                } else if(key == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR) {
                    separatorChar = value;
                } else if(key == PropertyID.DEC_OCR_USER_TEMPLATE) {
                    ocrUserTemplate  = value;
                }
            }
        }
        // urovo add by shenpidong end 2020-10-15
        if (length != currIndex) {
            ContentValues[] Values = new ContentValues[currIndex];
            System.arraycopy(currValues, 0, Values, 0, currIndex);
            android.device.provider.Settings.System.putBulkStrings(mContentResolver, Values);
        } else {
            android.device.provider.Settings.System.putBulkStrings(mContentResolver, currValues);
        }
        currValues = null;
    }

    private void updateCustomDefault(SparseArray<String> property){
        int size = property.size();
        ContentValues[] currValues = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            int key = property.keyAt(i);
            String value = property.get(key);
            if (value == null) {
                android.util.Log.i("debug", "parse property: " + key);
                continue;
            }
            ContentValues values = new ContentValues();
            values.put(android.device.provider.Constants.KEY_NAME, mHashMap.get(key));
            values.put(android.device.provider.Constants.KEY_VALUE, value);
            currValues[i] = values;
            if (!isStringType(key)) {
                try {
                    if(!TextUtils.isEmpty(value)) {
                        mCachePropertys.put(key, Integer.valueOf(value));
                    }
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
                } else if(key == PropertyID.LABEL_MATCHER_TARGETREGEX) {
                    replaceRegex = value;
                } else if(key == PropertyID.LABEL_MATCHER_REPLACEMENT) {
                    replaceDst = value;
                } else if(key == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR) {
                    separatorChar = value;
                } else if(key == PropertyID.DEC_OCR_USER_TEMPLATE) {
                    ocrUserTemplate  = value;
                }
            }
        }
        android.device.provider.Settings.System.putBulkStrings(mContentResolver, currValues);
    }
    ///system/etc/default_property.xml　/system/etc/scanner_default_property.xml 配置有所有选项
    //scanner_custom_property.xml可以仅配置客户需要默认的选项
    private void updateDatabaseDefault() {
        long startTime = System.currentTimeMillis();
        SparseArray<String> property = parsePropertyXML(true);
        long stopTime = System.currentTimeMillis();
        android.util.Log.i("debug", "parse time: " + (stopTime - startTime));
        updateDefault(property);
        // urovo zhoubo add begin 2021.3.4
        if (new File("/system/etc/scanner_custom_property.xml").exists()){
            SparseArray<String> customprop = parsePropertyXML(false);
            updateCustomDefault(customprop);
        }
        // urovo zhoubo add end 2021.3.4
        stopTime = System.currentTimeMillis();
        android.util.Log.i(TAG, "update database: " + (stopTime - startTime));
        property.clear();
        property = null;
        android.util.Log.i(TAG, "update default end: ");
    }

    private SparseArray<String> parsePropertyXML(boolean defaultXML) {
        //优先级：/customize/etc/default_property.xml　/system/etc/default_property.xml　/system/etc/scanner_default_property.xml
        InputStream inputStream = null;
        SparseArray<String> arraryProperty = new SparseArray<String>();
        try {
            if (defaultXML) {
                if (new File("/customize/etc/default_property.xml").exists()){
                    inputStream = new FileInputStream("/customize/etc/default_property.xml");
                }
                if (inputStream == null){
                    if (new File("/system/etc/default_property.xml").exists()){
                        inputStream = new FileInputStream("/system/etc/default_property.xml");
                    } else {
                        inputStream = new FileInputStream("/system/etc/scanner_default_property.xml");
                    }
                }
            } else {
                inputStream = new FileInputStream("/system/etc/scanner_custom_property.xml");
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
                            if(scanType == 13 && DEC_ILLUM_POWER_LEVEL.equals(name) && Integer.parseInt(value) > 7) { // 4710 max DEC_ILLUM_POWER_LEVEL=7
                                value = "3";
                            }
                            if(scanType == 9 && DEC_2D_LIGHTS_MODE.equals(name) && Integer.parseInt(value) > 2) { // 2100 default LIGHTS_MODE value is 1
                                value = "1";
                            }
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

    public boolean setDefaults() {
        if (mScanner != null && mIsScannerOpend) {
            updateDatabaseDefault();
            mScanner.setDefaults();
            // urovo add shenpidong begin 2019-09-12
            if (scanType == ScannerFactory.TYPE_N6603 || scanType == ScannerFactory.TYPE_N6703 || scanType == ScannerFactory.TYPE_SE2030 || scanType == ScannerFactory.TYPE_N4603) {
            // urovo add shenpidong begin 2019-09-12
                mScanner.setProperties(null);
            }
            // urovo add shenpidong end 2019-09-12
            SystemProperties.set("persist.sys.scanner.reset", "true");
            mSendTokensOption = getPropertyInt(PropertyID.SEND_TOKENS_OPTION);
            mSendTokensFormat = getPropertyInt(PropertyID.SEND_TOKENS_FORMAT);
            mSendTokensSeparator = getPropertyInt(PropertyID.SEND_TOKENS_SEPARATOR);
            mFormatUDIDate = getPropertyInt(PropertyID.ENABLE_FORMAT_UDI_DATE);
            mParserUDICodes = getPropertyInt(PropertyID.ENABLE_PARSER_UDICODE);
            return true;
        }
        return false;
    }

    public boolean lockHwTriggler(boolean lock) {
        if(mCustomDEPPON && lock) {
            //释放德邦定制后台摄像头扫描服务
            mContext.sendBroadcast(new Intent("CAMERA_IMAGE_SCAN_RELEASE"));
            //return false;
        }
        if (lock == (getPropertyInt(PropertyID.TRIGGERING_LOCK) == 1)) {
            android.util.Log.i(TAG, "=======================lockHwTriggler: true ");
            return true;
        }
        synchronized (mCachePropertys) {
            if (mScanner != null) {
                /*boolean isLock = mScanner.lockHwTriggler(lock);
                if (lock)
                    mCachePropertys.put(PropertyID.TRIGGERING_LOCK, isLock ? 1 : 0);
                else
                    mCachePropertys.put(PropertyID.TRIGGERING_LOCK, isLock ? 0 : 1);
                return isLock;
                */
                android.util.Log.i(TAG, "lockHwTriggler:  " + lock);
                mCachePropertys.put(PropertyID.TRIGGERING_LOCK, lock ? 1:0);
                android.device.provider.Settings.System.putInt(mContentResolver, android.device.provider.Settings.System.TRIGGERING_LOCK,  lock ? 1:0);
                return true;
            }
            return false;
        }
    }

    private boolean mScanning = false;

    // urovo add shenpidong begin 2019-02-14
    private synchronized void startScanService() {
        Log.d(TAG , "startScanService , ,camera status:" + mIsOpenCameraAndUse +" mIsScannerOpend:" + mIsScannerOpend + ",Open Camera And Close Scanner:" + mIsOpenCameraAndCloseScanner + ",TopActivity:" + mTopActivityIsCameraAndCloseScanner);
        if(mIsOpenCameraAndCloseScanner && (!mIsOpenCameraAndUse) && (mIsScannerOpend || mTopActivityIsCameraAndCloseScanner)) {
            mIsOpenCameraAndCloseScanner = false;
            mTopActivityIsCameraAndCloseScanner = false;
            PowerSet(1);
        }
        mIsOpenCameraAndUse = false;
        mIsOpenCameraAndCloseScanner = false;
    }
    // urovo add shenpidong end 2019-02-14

    public void softTrigger(int on) {
        if(android.os.SystemProperties.get("persist.sys.scanercontrol", "on").equals("off")){
            return;
        }
        if(getPropertyInt(PropertyID.TRIGGERING_LOCK) == 1) {
            return;
        }
        // urovo add shenpidong begin 2019-04-22
        if(mIsOpenCameraAndUse && (scanType == ScannerFactory.TYPE_SE4710 || scanType == ScannerFactory.TYPE_SE4850 ||  scanType == ScannerFactory.TYPE_SE2100 
            || scanType == ScannerFactory.TYPE_N3601 || scanType == ScannerFactory.TYPE_N6603 || scanType == ScannerFactory.TYPE_SE4500
            || scanType == ScannerFactory.TYPE_N6703 || scanType == ScannerFactory.TYPE_SE4770 || scanType == ScannerFactory.TYPE_SE2030)) {
            Log.d(TAG , "soft Trigger , Camera open and ignore Scanner.");
            return;
        }
        // urovo add shenpidong end 2019-04-22
        startScanService();
        /*
        int power = android.provider.Settings.System.getInt(mContext.getContentResolver(), "urovo_scan_stat", 1);
        boolean cameraStatus = getCameraStatus();
        if(power == 1 && !cameraStatus){
            open();
        }
        */
        if (!mIsScannerOpend) {
            Log.e(TAG, "scanner no open");
            return;
        }
        if (getPropertyInt(PropertyID.TRIGGERING_MODES) == 4) {
            if (1 == on && !mScanning) {
                mScanning = true;
                // zhouxin
                if (mWakeLock != null) {
                    Message m = Message.obtain(mHandler, MESSAGE_RELEASE_WAKELOCK);
                    m.arg1 = 1;
                    mHandler.sendMessage(m);
                }
                mScanner.startDecode(timeOut);
            } else {
                if (mWakeLock != null && mScanning) {
                    Message m = Message.obtain(mHandler, MESSAGE_RELEASE_WAKELOCK);
                    m.arg1 = 0;
                    mHandler.sendMessage(m);
                }
                mScanning = false;
                mScanner.stopDecode();
            }
        } else {
            mScanning = false;
            if (mScanner != null) {
                if (0 == on) {
                    mScanner.stopDecode();
                    /*if(scanType == 4 && PROJECT_SQ26) {
                        Message m = Message.obtain(mNotificationHandler, NotificationHandler
                        .MESSAGE_STOP_LED_NOTIFICATIONS);
                        mNotificationHandler.sendMessage(m);
                    }*/
                } else if (1 == on) {
                    // It shouldn't be needeed but we add it to be sure that led notifications
                    // are stopped
                    /*if(scanType == 4 && PROJECT_SQ26) {
                        Message m = Message.obtain(mNotificationHandler, NotificationHandler
                        .MESSAGE_STOP_LED_NOTIFICATIONS);
                        mNotificationHandler.sendMessage(m);
                    }*/
                    mScanner.startDecode(timeOut);
                }
            }
        }
    }

    private void loadProperties() {
        int valueInt = -1;
        for (int i = 0; i < ALL_PROPERTY_INDEX.length; i++) {
            int id = ALL_PROPERTY_INDEX[i];
            String key = mHashMap.get(id);
            if (isStringType(id)) {
                String val = android.device.provider.Settings.System.getString(mContentResolver,
                        key);
                if (id == PropertyID.LABEL_PREFIX && val != null) {
                    mLabelPrefix = val;
                } else if (id == PropertyID.LABEL_SUFFIX && val != null) {
                    mLabelSuffix = val;
                } else if (id == PropertyID.WEDGE_INTENT_ACTION_NAME && val != null) {
                    wedgeIntentAction = val;
                } else if (id == PropertyID.WEDGE_INTENT_DATA_STRING_TAG && val != null) {
                    wedgeIntentLable = val;
                } else if (id == PropertyID.WEDGE_INTENT_DECODE_DATA_TAG && val != null){
                    wedgeIntentRaw = val;
                } else if (id == PropertyID.LABEL_MATCHER_PATTERN && val != null) {
                    mPattern = val;
                } else if(id == PropertyID.LABEL_MATCHER_TARGETREGEX && val != null) {
                    replaceRegex = val;
                } else if(id == PropertyID.LABEL_MATCHER_REPLACEMENT && val != null) {
                    replaceDst = val;
                } else if(id == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR && val != null) {
                    separatorChar = val;
                } else if(id == PropertyID.DEC_OCR_USER_TEMPLATE && val != null) {
                    ocrUserTemplate = val;
                }
            } else {
                valueInt = android.device.provider.Settings.System.getInt(mContentResolver, key,
                        valueInt);
                if (valueInt != -1) {
                    mCachePropertys.put(id, valueInt);
                    valueInt = -1;
                }
            }
        }
        mSendTokensOption = getPropertyInt(PropertyID.SEND_TOKENS_OPTION);
        mSendTokensFormat = getPropertyInt(PropertyID.SEND_TOKENS_FORMAT);
        mSendTokensSeparator = getPropertyInt(PropertyID.SEND_TOKENS_SEPARATOR);
        mFormatUDIDate = getPropertyInt(PropertyID.ENABLE_FORMAT_UDI_DATE);
        mParserUDICodes = getPropertyInt(PropertyID.ENABLE_PARSER_UDICODE);
    }

    private void loadImportProperties() {
        SparseArray<Integer> mTempMap = new SparseArray<Integer>();
        synchronized (mCachePropertys) {
            int valueInt = -1;
            for (int i = 0; i < ALL_PROPERTY_INDEX.length; i++) {
                int id = ALL_PROPERTY_INDEX[i];
                String key = mHashMap.get(id);
                if(isStringType(id)) {
                    String val = android.device.provider.Settings.System.getString(mContentResolver, key);
                    if(id == PropertyID.LABEL_PREFIX && val != null) {
                        mLabelPrefix = val;
                    } else if(id == PropertyID.LABEL_SUFFIX && val != null) {
                        mLabelSuffix = val;
                    } else if(id == PropertyID.WEDGE_INTENT_ACTION_NAME && val != null) {
                        wedgeIntentAction = val;
                    } else if(id == PropertyID.WEDGE_INTENT_DATA_STRING_TAG && val != null) {
                        wedgeIntentLable = val;
                    } else if (id == PropertyID.WEDGE_INTENT_DECODE_DATA_TAG && val != null){
                        wedgeIntentRaw = val;
                    } else if(id == PropertyID.LABEL_MATCHER_PATTERN && val != null) {
                        mPattern = val;
                    } else if(id == PropertyID.LABEL_MATCHER_TARGETREGEX && val != null) {
                        replaceRegex = val;
                    } else if(id == PropertyID.LABEL_MATCHER_REPLACEMENT && val != null) {
                        replaceDst = val;
                    } else if(id == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR && val != null) {
                        separatorChar = val;
                    } else if(id == PropertyID.DEC_OCR_USER_TEMPLATE && val != null) {
                        ocrUserTemplate = val;
                    } else if(id == PropertyID.CODING_FORMAT_NAME && val != null) {
                        codingFormatName = val;
                    }
                } else {
                    valueInt = android.device.provider.Settings.System.getInt(mContentResolver, key,
                            valueInt);
                    try {
                        if(valueInt != -1) {
                            Object v = null;
                            v = mCachePropertys.get(id);
                            if (v instanceof Integer) {
                                int value = ((Integer) v).intValue();
                                if(valueInt == value) {
                                    continue;
                                }
                                mTempMap.put(id, value);
                            }
                            mCachePropertys.put(id, valueInt);
                            valueInt = -1;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if(mTempMap.size() > 0 && mScanner != null) {
                mScanner.setProperties(mTempMap);
            }
        }
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
                values.put(android.device.provider.Constants.KEY_NAME, key);
                values.put(android.device.provider.Constants.KEY_VALUE, value);
                currValues[i] = values;
            }
            android.device.provider.Settings.System.putBulkStrings(mContentResolver, currValues);
        }
    }

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

    private String getAppendString(String appendStr) {
        if (appendStr != null && !appendStr.isEmpty()) {
            for (int i = 0; i < NON_PRINTABLE_CHARS.length; i++) {
                String noPrint = nonPrintCharHashmap.get(NON_PRINTABLE_CHARS[i]);
                if (noPrint != null) {
                    appendStr = appendStr.replace(NON_PRINTABLE_CHARS[i], noPrint);
                }
            }
        } else {
            appendStr = "";
        }
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
                        + Character.digit(s.charAt(i+1), 16));
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
        } else if (type == 6) {
            String dstBarcode = barcode;
            if (replaceRegex != null && !replaceRegex.equals("")) {
                if (replaceDst != null) {
                    byte[] regex = hexStringToByteArray(replaceRegex);
                    if (regex != null) {
                        try {
                            dstBarcode = barcode.replace((new String(regex)), replaceDst);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            textForWedge.append(getAppendString(mLabelPrefix));
            textForWedge.append(getAppendString(dstBarcode));
            textForWedge.append(getAppendString(mLabelSuffix));
            return textForWedge.toString();
        }
        return barcode;
    }

    public boolean setPropertyString(int index, String value) {
        // TODO Auto-generated method stub
        if (value == null || !isStringType(index)) {
            return false;
        }
        if (index == PropertyID.LABEL_PREFIX) {
            mLabelPrefix = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.LABEL_PREFIX, value);
        } else if (index == PropertyID.LABEL_SUFFIX) {
            mLabelSuffix = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.LABEL_SUFFIX, value);
        } else if (index == PropertyID.WEDGE_INTENT_ACTION_NAME) {
            wedgeIntentAction = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.WEDGE_INTENT_ACTION_NAME, value);
        } else if (index == PropertyID.WEDGE_INTENT_DATA_STRING_TAG) {
            wedgeIntentLable = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.WEDGE_INTENT_DATA_STRING_TAG, value);
        } else if (index == PropertyID.WEDGE_INTENT_DECODE_DATA_TAG) {
            wedgeIntentRaw = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.WEDGE_INTENT_DECODE_DATA_TAG, value);
        } else if (index == PropertyID.LABEL_MATCHER_PATTERN) {
            mPattern = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.LABEL_MATCHER_PATTERN, value);
        } else if (index == PropertyID.LABEL_MATCHER_TARGETREGEX) {
            replaceRegex = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.LABEL_MATCHER_TARGETREGEX, value);
        } else if (index == PropertyID.LABEL_MATCHER_REPLACEMENT) {
            replaceDst = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.LABEL_MATCHER_REPLACEMENT, value);
        } else if (index == PropertyID.DEC_OCR_USER_TEMPLATE) {
            ocrUserTemplate = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.DEC_OCR_USER_TEMPLATE, value);
            SparseArray<Integer> mTempMap = new SparseArray<Integer>();
            mTempMap.put(PropertyID.DEC_OCR_TEMPLATE, 1);
            if(mScanner != null)
                mScanner.setProperties(mTempMap);
        } else if(index == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR) {
            separatorChar = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.LABEL_FORMAT_SEPARATOR_CHAR, value);
        } else if(index == PropertyID.CODING_FORMAT_NAME) {
            codingFormatName = value;
            android.device.provider.Settings.System.putString(mContentResolver,
                    android.device.provider.Settings.System.CODING_FORMAT_NAME, value);
        }
        return true;
    }

    public String getPropertyString(int index) {
        // TODO Auto-generated method stub
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
                } else if (index == PropertyID.WEDGE_INTENT_DECODE_DATA_TAG) {
                    value = wedgeIntentRaw;
                } else if (index == PropertyID.LABEL_MATCHER_PATTERN) {
                    value = mPattern;
                } else if (index == PropertyID.LABEL_MATCHER_TARGETREGEX) {
                    value = replaceRegex;
                } else if (index == PropertyID.LABEL_MATCHER_REPLACEMENT) {
                    value = replaceDst;
                } else if (index == PropertyID.DEC_OCR_USER_TEMPLATE) {
                    value = ocrUserTemplate;
                } else if(index == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR) {
                    value = separatorChar;
                } else if(index == PropertyID.CODING_FORMAT_NAME) {
                    value = codingFormatName;
                }
            } else if (mCachePropertys.indexOfKey(index) != -1) {
                Object v = null;
                v = mCachePropertys.get(index);
                value = (String) v;
            }
        }
        return value;
    }

    public boolean isPropertySupported(int prop) {
        // TODO Auto-generated method stub
        boolean isLocal = false;
        isLocal = isLocalPropertyIndex(prop);
        if (isLocal) {
            return true;
        }
        if (mScanner == null)
            return false;
        return mScanner.isPropertySupported(prop);
    }

    public int setPropertyInts(int[] id_buffer, int id_buffer_length, int[] value_buffer, int
            value_buffer_length, int[] id_bad_buffer) {
        int[] temp_bad_buff = new int[id_buffer_length];
        int bad_id = 0;
        boolean checkValue = true;
        if(id_bad_buffer != null && id_bad_buffer[0] == -1) {
            checkValue = false;////Scan wedge配置参数全部设置到扫描头中
        }
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
                    if (isLocal == false) {
                        int V = getPropertyInt(index);
                        if (V == value && checkValue) {
                            continue;
                        }
                        mTempMap.put(index, value);
                    // urovo add shenpidong begin 2019-04-18
                    } else if((scanType == ScannerFactory.TYPE_SE4710 || scanType == ScannerFactory.TYPE_SE4850 
                        || scanType == ScannerFactory.TYPE_SE2100 || scanType == ScannerFactory.TYPE_SE4770) && PropertyID.LABEL_SEPARATOR_ENABLE == index) {
                        int V = getPropertyInt(index);
                        if (V == value) {
                            continue;
                        }
                        mTempMap.put(PropertyID.TRANSMIT_CODE_ID, value);
                    }
                    // urovo add shenpidong end 2019-04-18
                    mMap.put(index, value);
                    if (index == PropertyID.TRIGGERING_MODES && mIsScannerOpend && mScanner !=
                            null) {
                        //android.util.Log.i(TAG, " setParameterInts: index: { " + index + " }
                        // value: { " + value + "}");
                        // zhouxin
                        if (mWakeLock != null && mScanning) {
                            Message m = Message.obtain(mHandler, MESSAGE_RELEASE_WAKELOCK);
                            m.arg1 = 0;
                            mHandler.sendMessage(m);
                        }
                        if (value == 8 || value == 2) {
                            mScanning = false;
                            mScanner.stopDecode();
                            if (scanType == ScannerFactory.TYPE_N3680)
                                ScanNative.unlockTriggle();
                            if(scanType == ScannerFactory.TYPE_IA100)
                                mContext.sendBroadcastAsUser(new Intent(com.android.server.scanner.IA100Scanner.ACTION_A100S_RELEASE),UserHandle.ALL);
                        } else {
                            if (scanType == ScannerFactory.TYPE_N3680)
                                ScanNative.lockTriggle();
                            if(scanType == ScannerFactory.TYPE_IA100)
                                mContext.sendBroadcastAsUser(new Intent(com.android.server.scanner.IA100Scanner.ACTION_A100S_RELEASE),UserHandle.ALL);
                        }
                    } else if (index == PropertyID.IMAGE_PICKLIST_MODE && mIsScannerOpend &&
                            mScanner != null) {
                        mScanning = false;
                        mScanner.stopDecode();
                    // urovo zhoubo add for SUSPENSION_BUTTON begin 2020.8.13
                    } else if (index == PropertyID.SUSPENSION_BUTTON){
                        updateSuspensionButtonStatus(value);
                    } else if(index == PropertyID.SEND_TOKENS_OPTION) {
                        mSendTokensOption = value;
                    } else if(index == PropertyID.SEND_TOKENS_FORMAT) {
                        mSendTokensFormat = value;
                    } else if(index == PropertyID.SEND_TOKENS_SEPARATOR) {
                        mSendTokensSeparator = value;
                    } else if(index == PropertyID.ENABLE_FORMAT_UDI_DATE) {
                        mFormatUDIDate = value;
                    } else if(index == PropertyID.ENABLE_PARSER_UDICODE) {
                        mParserUDICodes = value;
                    }
                    // urovo zhoubo add for SUSPENSION_BUTTON end 2020.8.13
                    mCachePropertys.put(index, value);// maybe need to storage
                }
            } else {
                temp_bad_buff[bad_id++] = index;
            }
        }
        //Log.i(TAG, "setParameterInts  id_buffer_length  " + id_buffer_length);
        //config to scanner engine
        if (mTempMap.size() > 0 && mScanner != null)
            mScanner.setProperties(mTempMap);
        updateToDB(mMap);
        if (bad_id > 0)
            System.arraycopy(temp_bad_buff, 0, id_bad_buffer, 0, bad_id);
        return bad_id;
    }

    public int getPropertyInts(int[] id_buffer, int id_buffer_length, int[] value_buffer, int
            value_buffer_length, int[] id_bad_buffer) {
        int value_number = 0;
        int[] temp_bad_buff = new int[id_buffer_length];
        //int[] temp_value_buff = new int[value_buffer_length];
        int bad_id = 0;
        int value = 0;
        for (int i = 0; i < id_buffer_length && i < value_buffer_length; i++) {
            int index = id_buffer[i];
            if(index == 10000) {
                value_buffer[value_number++] = mDeviceDecodeNum;
            } else {
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
        }
        /*if(value_number > 0) {
            System.arraycopy(temp_value_buff, 0, value_buffer, 0, value_number);
        } */
        if (bad_id > 0)
            System.arraycopy(temp_bad_buff, 0, id_bad_buffer, 0, bad_id);
        return bad_id;
    }

    public void enableAllSymbologies(boolean enable) {
        boolean isSupported = false;
        int value = (enable ? 1 : 0);
        int index = -1;
        SparseArray<Integer> mTempMap = new SparseArray<Integer>();
        try {
            int size = Scanner.KEY_BARCODE_ENABLE_INDEX.length;
            for (int i = 0; i < size; i++) {
                index = Scanner.KEY_BARCODE_ENABLE_INDEX[i];
                if(index != -1) {
                    int V = getPropertyInt(index);
                    if (V != -1 && value != V) {
                        mCachePropertys.put(index, value);
                        mTempMap.put(index, value);
                    }
                }
            }
            Log.d(TAG, "mTempMap size= " + mTempMap.size());
            if (mTempMap.size() > 0 && null != mScanner) {
                mScanner.setProperties(mTempMap);
            }
            updateToDB(mTempMap);
        } catch (Exception e) {
        }
    }

    public boolean isSymbologyEnabled(int type) {
        if (mScanner != null) {
            int index = mScanner.getSymbologyEnableIndex(type);
            if (index < 0)
                return false;
            int value = getPropertyInt(index);
            if (value == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean isSymbologySupported(int type) {
        if (mScanner != null) {
            int index = mScanner.getSymbologyEnableIndex(type);
            if (index < 0)
                return false;
            return mScanner.isPropertySupported(index);
        }
        return false;
    }

    public void enableSymbology(int type, boolean enable) {
        boolean isSupported = false;
        int value = (enable ? 1 : 0);
        int index = -1;
        if (mScanner != null) {
            SparseArray<Integer> mTempMap = new SparseArray<Integer>();
            index = mScanner.getSymbologyEnableIndex(type);
            Log.d(TAG, "enableSymbology index = " + index);
            // Integer V = (Integer)mCacheHashMap.get(index);
            if(index != -1) {
                int V = getPropertyInt(index);
                if (V != -1 && value != V) {
                    mCachePropertys.put(index, value);
                    mTempMap.put(index, value);
                    mScanner.setProperties(mTempMap);
                    updateToDB(mTempMap);
                } 
            }
        }
    }

    public int releaseScannerEngineForCamera(int mode) {
        mHandler.removeMessages(MESSAGE_DELAYED_OPEN_SCANNER);
        mHandler.removeMessages(MESSAGE_DELAYED_RESET_SCANNER);
        if(mIsScannerOpend && ScannerFactory.isCameraEngine()) {
            if(mode == 1 && mScanner != null) {
                mIsOpenCameraAndUse = true;
                mIsOpenCameraAndCloseScanner = true;
                mScanner.close();
                return 1;
            } else {
                mIsOpenCameraAndUse = false;
                if (mode == 0 && mHandler != null) {
                    mHandler.sendEmptyMessageDelayed(MESSAGE_DELAYED_OPEN_SCANNER, 1500);
                }
            }
        }
        return 0;
    }

    // urovo add shenpidong begin 2019-02-14
    @Override
    public synchronized boolean getCameraStatus() {
        return mIsOpenCameraAndUse;
    }

    @Override
    public synchronized void setCameraStatus(boolean b) {
        Log.d(TAG, "setCameraStatus , b:" + b);
        // urovo add shenpidong begin 2019-03-15
        mHandler.removeMessages(MESSAGE_DELAYED_OPEN_SCANNER);
        mHandler.removeMessages(MESSAGE_DELAYED_RESET_SCANNER);
        // urovo add shenpidong end 2019-03-15
        if (b && mScanner != null) {
            mIsOpenCameraAndUse = true;
            mIsOpenCameraAndCloseScanner = true;
            mScanner.close();
        } else {
            mIsOpenCameraAndUse = false;
        // mIsOpenCameraAndCloseScanner = false;
        }
        // urovo add shenpidong begin 2019-07-11
        // android.provider.Settings.System.putInt(mContext.getContentResolver(), "urovo_scan_stat", b?0:1);
        // urovo add shenpidong end 2019-07-11
        // android.device.provider.Settings.System.putInt(mContentResolver, "scan_camera_used", b?1:0);
        // urovo add shenpidong begin 2019-03-15
        if (!b && mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MESSAGE_DELAYED_OPEN_SCANNER, scanType == ScannerFactory.TYPE_SE2030 ? 600:1500);
        }
        // urovo add shenpidong end 2019-03-15
    }
    // urovo add shenpidong end 2019-02-14

    private SparseArray<Integer> mCachePropertys = new SparseArray<Integer>();
    private SparseArray<String> mHashMap = new SparseArray<String>();

    private HashMap<String, String> nonPrintCharHashmap = new HashMap<String, String>();

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
                if (val >= 4 && val <= 55)
                    return true;
            case PropertyID.GS1_EXP_LENGTH1:// 1 74 1
            case PropertyID.GS1_EXP_LENGTH2:// 1 74 74
                if (val >= 1 && val <= 74)
                    return true;
            case PropertyID.I25_ENABLE_CHECK:
            case PropertyID.UPCA_SEND_SYS:
            case PropertyID.UPCE_SEND_SYS:
            case PropertyID.UPCE1_SEND_SYS:
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
            case PropertyID.HANXIN_INVERSE:
                if (val >= 0 && val <= 2)
                    return true;
            case PropertyID.BAR_CODES_TO_READ:
                if (val >= 1 || val <= 10) {
                    return true;
                }
                break;
            case PropertyID.LABEL_APPEND_ENTER:
            case PropertyID.WEDGE_KEYBOARD_ENABLE:
                if (val >= 0 || val <= 3) {
                    return true;
                }
                break;
            case PropertyID.CODING_FORMAT:
                if (val >= 0 || val <= 10) {
                    return true;
                }
                break;
            case PropertyID.IMAGE_FIXED_EXPOSURE:
                if (val >= 1 || val <= 7874) {
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
                if (val >= 0 || val <= 4000) {
                    return true;
                }
                break;
            case PropertyID.DEC_Multiple_Decode_TIMEOUT:
                if(val >=50 || val <= 60000) {
                    return true;
                }
                break;
            /* urovo tao.he add begin, 20190314*/
            case PropertyID.DEC_Multiple_Decode_INTERVAL:
                if(val >=0 || val <= 5000) {
                    return true;
                }
                break;
            /* urovo tao.he add end, 20190314 */
            case PropertyID.DEC_MaxMultiRead_COUNT:
                if(val >=1 || val <= 100) {
                    return true;
                }
                break;
            case PropertyID.DEC_Multiple_Decode_MODE:
                if(val >=0 || val <= 2) {
                    return true;
                }
                break;
            case PropertyID.WEDGE_KEYBOARD_TYPE:
                if(val >=0 || val <= 3) {
                    return true;
                }
                break;
            case PropertyID.SEND_TOKENS_SEPARATOR:
            case PropertyID.DEC_OCR_MODE:
            case PropertyID.CODE11_SEND_CHECK:
                if(val >=0 || val <= 3) {
                    return true;
                }
                break;
            case PropertyID.DEC_OCR_TEMPLATE:
                if(val >=1 || val <= 16) {
                    return true;
                }
                break;
            case PropertyID.SEND_TOKENS_FORMAT:
            case PropertyID.SEND_TOKENS_OPTION:
            case PropertyID.TRANSMIT_CODE_ID:
            case PropertyID.DPM_DECODE_MODE:
            case PropertyID.DATAMATRIX_SYMBOL_SIZE:
            case PropertyID.MAXICODE_SYMBOL_SIZE:
            case PropertyID.QRCODE_SYMBOL_SIZE:
            case PropertyID.AZTEC_SYMBOL_SIZE:
                if(val >=0 || val <= 2) {
                    return true;
                }
                break;
            //urovo add jinpu.lin 2019.05.08
            case PropertyID.CHARACTER_DATA_DELAY:
            case PropertyID.APPEND_ENTER_DELAY:
                if(val >=0 || val <= 1000) {
                    return true;
                }
                break;
             case PropertyID.LOW_CONTRAST_IMPROVED:
                if(val >=0 || val <= 5) {
                    return true;
                }
                break;
            case PropertyID.DEC_EachImageAttempt_TIME:
                if(val >=30 || val <= 1500) {
                    return true;
                }
                break;
            case PropertyID.DEC_DECODE_DELAY:
                if(val >=0 || val <= 10000) {
                    return true;
                }
                break;
            case PropertyID.POSTAL_GROUP_TYPE_ENABLE:
            case PropertyID.DEC_DECODE_DEBUG_MODE:
                if(val >=0 || val <= 10) {
                    return true;
                }
                break;
            case PropertyID.MSI_REQUIRE_2_CHECK:
                if(val >=0 || val <= 6) {
                    return true;
                }
                break;
            case PropertyID.LOW_POWER_SLEEP_TIMEOUT:
                if(val >=15000 || val <= 60000) {
                    return true;
                }
                break;
            case PropertyID.CODE128_REDUCED_QUIET_ZONE:
                if(val >=0 || val <= 2) {
                    return true;
                }
                break;
            case PropertyID.CODE128_SECURITY_LEVEL:
                if(val >=0 || val <= 3) {
                    return true;
                }
                break;
            case PropertyID.CACHE_DATA_LIMIT_TIME:
                if(val >1000 || val <= 60*1000) {
                    return true;
                }
                break;
            case PropertyID.ENABLE_PARSER_UDICODE:
                if(val >=0 && val <= 62) {
                    return true;
                }
                break;
            case PropertyID.DEC_ES_MAX_GAIN:
                if(val >=1 && val <= 255) {
                    return true;
                }
                break;
            case PropertyID.DEC_ES_MAX_EXP:
                if(val >=1 && val <= 7874) {
                    return true;
                }
                break;
            case PropertyID.DEC_ES_TARGET_VALUE:
                if(val >=1 && val <= 8000) {
                    return true;
                }
                break;
            case PropertyID.C128_OUT_OF_SPEC:
                if(val >=0 && val <= 15) {
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
                || id == PropertyID.WEDGE_INTENT_DECODE_DATA_TAG
                || id == PropertyID.LABEL_MATCHER_PATTERN
                || id == PropertyID.LABEL_MATCHER_REPLACEMENT
                || id == PropertyID.LABEL_MATCHER_TARGETREGEX
                || id == PropertyID.DEC_OCR_USER_TEMPLATE
                || id == PropertyID.LABEL_FORMAT_SEPARATOR_CHAR
                || id == PropertyID.CODING_FORMAT_NAME);
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
            //PropertyID.LABEL_SEPARATOR_ENABLE,
            PropertyID.LABEL_FORMAT_SEPARATOR_CHAR,
            PropertyID.DEC_OCR_USER_TEMPLATE,
            PropertyID.CHARACTER_DATA_DELAY,
            PropertyID.APPEND_ENTER_DELAY,
            PropertyID.OUT_EDITORTEXT_MODE,
            PropertyID.SUSPENSION_BUTTON,
            PropertyID.CACHE_DATA_ENABLE,
            PropertyID.CACHE_DATA_LIMIT_ENABLE,
            PropertyID.CACHE_DATA_LIMIT_TIME,
            PropertyID.SEND_TOKENS_FORMAT,
            PropertyID.SEND_TOKENS_SEPARATOR,
            PropertyID.ENABLE_PARSER_UDICODE,
            PropertyID.ENABLE_FORMAT_UDI_DATE,
            PropertyID.WEBJUMP,
            PropertyID.OUTPUT_HEX_STRING_DATA,
            PropertyID.CODING_FORMAT_NAME,
    };
    private final int[] ALL_PROPERTY_INDEX = {
            PropertyID.IMAGE_EXPOSURE_MODE,
            PropertyID.IMAGE_FIXED_EXPOSURE,
            PropertyID.TRIGGERING_MODES,
            PropertyID.TRIGGERING_SLEEP_WORK,
            PropertyID.GOOD_READ_BEEP_ENABLE,
            PropertyID.GOOD_READ_VIBRATE_ENABLE,
            PropertyID.SEND_GOOD_READ_BEEP_ENABLE,
            PropertyID.SEND_GOOD_READ_VIBRATE_ENABLE,
            PropertyID.LABEL_APPEND_ENTER,
            PropertyID.IMAGE_PICKLIST_MODE,
            PropertyID.IMAGE_ONE_D_INVERSE,
            PropertyID.WEDGE_KEYBOARD_ENABLE,
            PropertyID.WEDGE_KEYBOARD_TYPE,
            PropertyID.WEDGE_INTENT_ACTION_NAME,
            PropertyID.WEDGE_INTENT_DATA_STRING_TAG,
            PropertyID.TRIGGERING_LOCK,
            PropertyID.SEND_LABEL_PREFIX_SUFFIX,
            PropertyID.LABEL_PREFIX,
            PropertyID.LABEL_SUFFIX,
            PropertyID.LABEL_MATCHER_PATTERN,
            PropertyID.LABEL_MATCHER_TARGETREGEX,
            PropertyID.LABEL_MATCHER_REPLACEMENT,
            PropertyID.REMOVE_NONPRINT_CHAR,
            PropertyID.LABEL_SEPARATOR_ENABLE,
            PropertyID.LABEL_FORMAT_SEPARATOR_CHAR,
            PropertyID.LASER_ON_TIME,
            PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL,
            PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL,
            PropertyID.FUZZY_1D_PROCESSING,
            PropertyID.MULTI_DECODE_MODE,
            PropertyID.BAR_CODES_TO_READ,
            PropertyID.FULL_READ_MODE,
            PropertyID.CODING_FORMAT,
            PropertyID.CODE39_ENABLE,        //Code39 definitions
            PropertyID.CODE39_ENABLE_CHECK,
            PropertyID.CODE39_SEND_CHECK,
            PropertyID.CODE39_FULL_ASCII,
            PropertyID.CODE39_LENGTH1,
            PropertyID.CODE39_LENGTH2,
            PropertyID.TRIOPTIC_ENABLE,      //trioptic
            PropertyID.CODE32_ENABLE,      //code 32 also see pharmacode 39
            PropertyID.CODE32_SEND_START,
            PropertyID.C25_ENABLE,//chinese 25
            PropertyID.D25_ENABLE,           //discrete 2/5
            PropertyID.D25_LENGTH1,
            PropertyID.D25_LENGTH2,
            PropertyID.M25_ENABLE,           //matrix 2/5
            PropertyID.CODE11_ENABLE,
            PropertyID.CODE11_ENABLE_CHECK,
            PropertyID.CODE11_SEND_CHECK,
            PropertyID.CODE11_LENGTH1,
            PropertyID.CODE11_LENGTH2,
            PropertyID.I25_ENABLE,           //interleaved 2/5
            PropertyID.I25_ENABLE_CHECK,
            PropertyID.I25_SEND_CHECK,
            PropertyID.I25_LENGTH1,
            PropertyID.I25_LENGTH2,
            PropertyID.I25_TO_EAN13,
            PropertyID.CODABAR_ENABLE,           //codebar
            PropertyID.CODABAR_NOTIS,
            PropertyID.CODABAR_CLSI,
            PropertyID.CODABAR_LENGTH1,
            PropertyID.CODABAR_LENGTH2,
            PropertyID.CODE93_ENABLE,        //code 93
            PropertyID.CODE93_LENGTH1,
            PropertyID.CODE93_LENGTH2,
            PropertyID.CODE128_ENABLE,       //code128
            PropertyID.CODE128_LENGTH1,
            PropertyID.CODE128_LENGTH2,
            PropertyID.CODE_ISBT_128,
            PropertyID.CODE128_GS1_ENABLE,       //gs1-128
            PropertyID.UPCA_ENABLE,          //uspa
            PropertyID.UPCA_SEND_CHECK,
            PropertyID.UPCA_SEND_SYS,
            PropertyID.UPCA_TO_EAN13,
            PropertyID.UPCE_ENABLE,      //uspe
            PropertyID.UPCE_SEND_CHECK,
            PropertyID.UPCE_SEND_SYS,
            PropertyID.UPCE_TO_UPCA,
            PropertyID.UPCE1_ENABLE,
            PropertyID.UPCE1_SEND_CHECK,
            PropertyID.UPCE1_SEND_SYS,
            PropertyID.UPCE1_TO_UPCA,
            PropertyID.EAN13_ENABLE,         //ean13
            PropertyID.EAN13_SEND_CHECK,
            PropertyID.EAN13_BOOKLANDEAN,
            PropertyID.EAN13_BOOKLAND_FORMAT,
            PropertyID.EAN8_ENABLE,          //ean8
            PropertyID.EAN8_SEND_CHECK,
            PropertyID.EAN8_TO_EAN13,
            PropertyID.EAN_EXT_ENABLE_2_5_DIGIT,   //UPC/EAN Extensions definitions
            PropertyID.UPC_EAN_SECURITY_LEVEL,
            PropertyID.UCC_COUPON_EXT_CODE,
            PropertyID.MSI_ENABLE,               //msi
            PropertyID.MSI_REQUIRE_2_CHECK,
            PropertyID.MSI_SEND_CHECK,
            PropertyID.MSI_CHECK_2_MOD_11,
            PropertyID.MSI_LENGTH1,
            PropertyID.MSI_LENGTH2,
            PropertyID.GS1_14_ENABLE,            //rss
            PropertyID.GS1_14_TO_UPC_EAN,
            PropertyID.GS1_LIMIT_ENABLE,         //rss limit
            PropertyID.GS1_EXP_ENABLE,           //rss exp
            PropertyID.GS1_EXP_LENGTH1,
            PropertyID.GS1_EXP_LENGTH2,
            PropertyID.US_POSTNET_ENABLE,        //postal code
            PropertyID.US_PLANET_ENABLE,
            PropertyID.US_POSTAL_SEND_CHECK,
            PropertyID.USPS_4STATE_ENABLE,
            PropertyID.UPU_FICS_ENABLE,
            PropertyID.ROYAL_MAIL_ENABLE,
            PropertyID.ROYAL_MAIL_SEND_CHECK,
            PropertyID.AUSTRALIAN_POST_ENABLE,
            PropertyID.KIX_CODE_ENABLE,
            PropertyID.JAPANESE_POST_ENABLE,
            PropertyID.PDF417_ENABLE,        //pdf417
            PropertyID.MICROPDF417_ENABLE,       //micro pdf417
            PropertyID.COMPOSITE_CC_AB_ENABLE,     //composite-cc_ab
            PropertyID.COMPOSITE_CC_C_ENABLE,    //composite-cc_c
            PropertyID.COMPOSITE_TLC39_ENABLE,
            PropertyID.HANXIN_ENABLE,
            PropertyID.HANXIN_INVERSE,
            PropertyID.DATAMATRIX_ENABLE,        //datamatrix
            PropertyID.DATAMATRIX_LENGTH1,
            PropertyID.DATAMATRIX_LENGTH2,
            PropertyID.DATAMATRIX_INVERSE,
            PropertyID.MAXICODE_ENABLE,          //maxicode
            PropertyID.QRCODE_ENABLE,            //qrcode
            PropertyID.QRCODE_INVERSE,
            PropertyID.MICROQRCODE_ENABLE,
            PropertyID.AZTEC_ENABLE,          //aztec code
            PropertyID.AZTEC_INVERSE,
            PropertyID.DEC_2D_LIGHTS_MODE,
            PropertyID.DEC_2D_CENTERING_ENABLE,
            PropertyID.DEC_2D_CENTERING_MODE,
            PropertyID.DEC_2D_WINDOW_UPPER_LX,
            PropertyID.DEC_2D_WINDOW_UPPER_LY,
            PropertyID.DEC_2D_WINDOW_LOWER_RX,
            PropertyID.DEC_2D_WINDOW_LOWER_RY,
            PropertyID.DEC_2D_DEBUG_WINDOW_ENABLE,
            PropertyID.DEC_ES_EXPOSURE_METHOD,
            PropertyID.DEC_ES_TARGET_VALUE,
            PropertyID.DEC_ES_TARGET_PERCENTILE,
            PropertyID.DEC_ES_TARGET_ACCEPT_GAP,
            PropertyID.DEC_ES_MAX_EXP,
            PropertyID.DEC_ES_MAX_GAIN,
            PropertyID.DEC_ES_FRAME_RATE,
            PropertyID.DEC_ES_CONFORM_IMAGE,
            PropertyID.DEC_ES_CONFORM_TRIES,
            PropertyID.DEC_ES_SPECULAR_EXCLUSION,
            PropertyID.DEC_ES_SPECULAR_SAT,
            PropertyID.DEC_ES_SPECULAR_LIMIT,
            PropertyID.DEC_ES_FIXED_GAIN,
            PropertyID.DEC_ES_FIXED_FRAME_RATE,
            PropertyID.DEC_ILLUM_POWER_LEVEL,
            PropertyID.DEC_PICKLIST_AIM_MODE,
            PropertyID.DEC_PICKLIST_AIM_DELAY,
            PropertyID.DEC_MaxMultiRead_COUNT,
            PropertyID.DEC_Multiple_Decode_TIMEOUT,
            PropertyID.DEC_Multiple_Decode_INTERVAL, /* urovo tao.he add, 20190314 */
            PropertyID.DEC_Multiple_Decode_MODE,
            PropertyID.DEC_OCR_MODE,
            PropertyID.DEC_OCR_TEMPLATE,
            PropertyID.DEC_OCR_USER_TEMPLATE,
            PropertyID.SPECIFIC_CODE_GS,
            PropertyID.TRANSMIT_CODE_ID,
            PropertyID.CHARACTER_DATA_DELAY,
            PropertyID.APPEND_ENTER_DELAY,
            PropertyID.DOTCODE_ENABLE,
            PropertyID.KOREA_POST_ENABLE,
            PropertyID.POSTAL_GROUP_TYPE_ENABLE,
            PropertyID.DEC_DECODE_DELAY,
            PropertyID.DEC_DECODE_DEBUG_MODE,
            PropertyID.OUT_CLIPBOARD_ENABLE,
            PropertyID.DEC_EachImageAttempt_TIME,
            PropertyID.DPM_DECODE_MODE,
            PropertyID.LINEAR_1D_QUIET_ZONE_LEVEL,
            PropertyID.CODE39_Quiet_Zone,
            PropertyID.CODE39_START_STOP,
            PropertyID.CODE39_SECURITY_LEVEL,
            PropertyID.M25_SEND_CHECK,
            PropertyID.M25_LENGTH1,
            PropertyID.M25_LENGTH2,
            PropertyID.I25_QUIET_ZONE,
            PropertyID.I25_SECURITY_LEVEL,
            PropertyID.CODABAR_ENABLE_CHECK,
            PropertyID.CODABAR_SEND_CHECK,
            PropertyID.CODABAR_SEND_START,
            PropertyID.CODABAR_CONCATENATE,
            PropertyID.CODE128_REDUCED_QUIET_ZONE,
            PropertyID.CODE128_CHECK_ISBT_TABLE,
            PropertyID.CODE_ISBT_Concatenation_MODE,
            PropertyID.CODE128_SECURITY_LEVEL,
            PropertyID.CODE128_IGNORE_FNC4,
            PropertyID.UCC_REDUCED_QUIET_ZONE,
            PropertyID.UCC_COUPON_EXT_REPORT_MODE,
            PropertyID.UCC_EAN_ZERO_EXTEND,
            PropertyID.UCC_EAN_SUPPLEMENTAL_MODE,
            PropertyID.GS1_LIMIT_Security_Level,
            PropertyID.COMPOSITE_UPC_MODE,
            PropertyID.POSTAL_GROUP_TYPE_ENABLE,
            PropertyID.KOREA_POST_ENABLE,
            PropertyID.Canadian_POSTAL_ENABLE,
            PropertyID.OUT_EDITORTEXT_MODE,
            PropertyID.SUSPENSION_BUTTON,
            PropertyID.CACHE_DATA_ENABLE,
            PropertyID.CACHE_DATA_LIMIT_ENABLE,
            PropertyID.CACHE_DATA_LIMIT_TIME,
            PropertyID.DATAMATRIX_SYMBOL_SIZE,
            PropertyID.MAXICODE_SYMBOL_SIZE,
            PropertyID.QRCODE_SYMBOL_SIZE,
            PropertyID.AZTEC_SYMBOL_SIZE,
            PropertyID.LOW_POWER_SLEEP_MODE,
            PropertyID.LOW_POWER_SLEEP_TIMEOUT,
            PropertyID.LOW_CONTRAST_IMPROVED,
            PropertyID.LOW_CONTRAST_IMPROVED_ALGORITHM,
            PropertyID.SEND_TOKENS_OPTION,
            PropertyID.SEND_TOKENS_FORMAT,
            PropertyID.SEND_TOKENS_SEPARATOR,
            PropertyID.ENABLE_PARSER_UDICODE,
            PropertyID.ENABLE_FORMAT_UDI_DATE,
            PropertyID.WEBJUMP,
            PropertyID.OUTPUT_HEX_STRING_DATA,
            PropertyID.GRIDMATRIX_ENABLED,
            PropertyID.QR_WITHOUT_QZ,
            PropertyID.QR_NON_SQUARE_MODULES,
            PropertyID.C128_OUT_OF_SPEC,
            PropertyID.CODING_FORMAT_NAME,
    };
    //NOTE this arrary length eq ALL_PROPERTY_INDEX length
    private String[] PROVIDERS_SETTINGS_STRING = {
            android.device.provider.Settings.System.IMAGE_EXPOSURE_MODE,
            android.device.provider.Settings.System.IMAGE_FIXED_EXPOSURE,
            android.device.provider.Settings.System.TRIGGERING_MODES,
            android.device.provider.Settings.System.TRIGGERING_SLEEP_WORK,
            android.device.provider.Settings.System.GOOD_READ_BEEP_ENABLE,
            android.device.provider.Settings.System.GOOD_READ_VIBRATE_ENABLE,
            android.device.provider.Settings.System.SEND_GOOD_READ_BEEP_ENABLE,
            android.device.provider.Settings.System.SEND_GOOD_READ_VIBRATE_ENABLE,
            android.device.provider.Settings.System.LABEL_APPEND_ENTER,
            android.device.provider.Settings.System.IMAGE_PICKLIST_MODE,
            android.device.provider.Settings.System.IMAGE_ONE_D_INVERSE,
            android.device.provider.Settings.System.WEDGE_KEYBOARD_ENABLE,
            android.device.provider.Settings.System.WEDGE_KEYBOARD_TYPE,
            android.device.provider.Settings.System.WEDGE_INTENT_ACTION_NAME,
            android.device.provider.Settings.System.WEDGE_INTENT_DATA_STRING_TAG,
            android.device.provider.Settings.System.TRIGGERING_LOCK,
            android.device.provider.Settings.System.SEND_LABEL_PREFIX_SUFFIX,
            android.device.provider.Settings.System.LABEL_PREFIX,
            android.device.provider.Settings.System.LABEL_SUFFIX,
            android.device.provider.Settings.System.LABEL_MATCHER_PATTERN,
            android.device.provider.Settings.System.LABEL_MATCHER_TARGETREGEX,
            android.device.provider.Settings.System.LABEL_MATCHER_REPLACEMENT,
            android.device.provider.Settings.System.REMOVE_NONPRINT_CHAR,
            android.device.provider.Settings.System.LABEL_SEPARATOR_ENABLE,
            android.device.provider.Settings.System.LABEL_FORMAT_SEPARATOR_CHAR,
            android.device.provider.Settings.System.LASER_ON_TIME,
            android.device.provider.Settings.System.TIMEOUT_BETWEEN_SAME_SYMBOL,
            android.device.provider.Settings.System.LINEAR_CODE_TYPE_SECURITY_LEVEL,
            android.device.provider.Settings.System.FUZZY_1D_PROCESSING,
            android.device.provider.Settings.System.MULTI_DECODE_MODE,
            android.device.provider.Settings.System.BAR_CODES_TO_READ,
            android.device.provider.Settings.System.FULL_READ_MODE,
            android.device.provider.Settings.System.CODING_FORMAT,
            android.device.provider.Settings.System.CODE39_ENABLE,        //Code39 definitions
            android.device.provider.Settings.System.CODE39_ENABLE_CHECK,
            android.device.provider.Settings.System.CODE39_SEND_CHECK,
            android.device.provider.Settings.System.CODE39_FULL_ASCII,
            android.device.provider.Settings.System.CODE39_LENGTH1,
            android.device.provider.Settings.System.CODE39_LENGTH2,
            android.device.provider.Settings.System.TRIOPTIC_ENABLE,      //trioptic
            android.device.provider.Settings.System.CODE32_ENABLE,      //code 32 also see
            // pharmacode 39
            android.device.provider.Settings.System.CODE32_SEND_START,
            android.device.provider.Settings.System.C25_ENABLE,
            android.device.provider.Settings.System.D25_ENABLE,           //DISCRETE 2/5
            android.device.provider.Settings.System.D25_LENGTH1,
            android.device.provider.Settings.System.D25_LENGTH2,
            android.device.provider.Settings.System.M25_ENABLE,           //matrix 2/5
            android.device.provider.Settings.System.CODE11_ENABLE,
            android.device.provider.Settings.System.CODE11_ENABLE_CHECK,
            android.device.provider.Settings.System.CODE11_SEND_CHECK,
            android.device.provider.Settings.System.CODE11_LENGTH1,
            android.device.provider.Settings.System.CODE11_LENGTH2,
            android.device.provider.Settings.System.I25_ENABLE,           //interleaved 2/5
            android.device.provider.Settings.System.I25_ENABLE_CHECK,
            android.device.provider.Settings.System.I25_SEND_CHECK,
            android.device.provider.Settings.System.I25_LENGTH1,
            android.device.provider.Settings.System.I25_LENGTH2,
            android.device.provider.Settings.System.I25_TO_EAN13,
            android.device.provider.Settings.System.CODABAR_ENABLE,           //codebar
            android.device.provider.Settings.System.CODABAR_NOTIS,
            android.device.provider.Settings.System.CODABAR_CLSI,
            android.device.provider.Settings.System.CODABAR_LENGTH1,
            android.device.provider.Settings.System.CODABAR_LENGTH2,
            android.device.provider.Settings.System.CODE93_ENABLE,        //code 93
            android.device.provider.Settings.System.CODE93_LENGTH1,
            android.device.provider.Settings.System.CODE93_LENGTH2,
            android.device.provider.Settings.System.CODE128_ENABLE,       //code128
            android.device.provider.Settings.System.CODE128_LENGTH1,
            android.device.provider.Settings.System.CODE128_LENGTH2,
            android.device.provider.Settings.System.CODE_ISBT_128,
            android.device.provider.Settings.System.CODE128_GS1_ENABLE,     //GS1-128
            android.device.provider.Settings.System.UPCA_ENABLE,          //uspa
            android.device.provider.Settings.System.UPCA_SEND_CHECK,
            android.device.provider.Settings.System.UPCA_SEND_SYS,
            android.device.provider.Settings.System.UPCA_TO_EAN13,
            android.device.provider.Settings.System.UPCE_ENABLE,      //uspe
            android.device.provider.Settings.System.UPCE_SEND_CHECK,
            android.device.provider.Settings.System.UPCE_SEND_SYS,
            android.device.provider.Settings.System.UPCE_TO_UPCA,
            android.device.provider.Settings.System.UPCE1_ENABLE,
            android.device.provider.Settings.System.UPCE1_SEND_CHECK,
            android.device.provider.Settings.System.UPCE1_SEND_SYS,
            android.device.provider.Settings.System.UPCE1_TO_UPCA,
            android.device.provider.Settings.System.EAN13_ENABLE,         //ean13
            android.device.provider.Settings.System.EAN13_SEND_CHECK,
            android.device.provider.Settings.System.EAN13_BOOKLANDEAN,
            android.device.provider.Settings.System.EAN13_BOOKLAND_FORMAT,
            android.device.provider.Settings.System.EAN8_ENABLE,          //ean8
            android.device.provider.Settings.System.EAN8_SEND_CHECK,
            android.device.provider.Settings.System.EAN8_TO_EAN13,
            android.device.provider.Settings.System.EAN_EXT_ENABLE_2_5_DIGIT,   //UPC/EAN
            // Extensions definitions
            android.device.provider.Settings.System.UPC_EAN_SECURITY_LEVEL,
            android.device.provider.Settings.System.UCC_COUPON_EXT_CODE,
            android.device.provider.Settings.System.MSI_ENABLE,               //msi
            android.device.provider.Settings.System.MSI_REQUIRE_2_CHECK,
            android.device.provider.Settings.System.MSI_SEND_CHECK,
            android.device.provider.Settings.System.MSI_CHECK_2_MOD_11,
            android.device.provider.Settings.System.MSI_LENGTH1,
            android.device.provider.Settings.System.MSI_LENGTH2,
            android.device.provider.Settings.System.GS1_14_ENABLE,            //rss
            android.device.provider.Settings.System.GS1_14_TO_UPC_EAN,
            android.device.provider.Settings.System.GS1_LIMIT_ENABLE,         //rss limit
            android.device.provider.Settings.System.GS1_EXP_ENABLE,           //rss exp
            android.device.provider.Settings.System.GS1_EXP_LENGTH1,
            android.device.provider.Settings.System.GS1_EXP_LENGTH2,
            android.device.provider.Settings.System.US_POSTNET_ENABLE,        //postal code
            android.device.provider.Settings.System.US_PLANET_ENABLE,
            android.device.provider.Settings.System.US_POSTAL_SEND_CHECK,
            android.device.provider.Settings.System.USPS_4STATE_ENABLE,
            android.device.provider.Settings.System.UPU_FICS_ENABLE,
            android.device.provider.Settings.System.ROYAL_MAIL_ENABLE,
            android.device.provider.Settings.System.ROYAL_MAIL_SEND_CHECK,
            android.device.provider.Settings.System.AUSTRALIAN_POST_ENABLE,
            android.device.provider.Settings.System.KIX_CODE_ENABLE,
            android.device.provider.Settings.System.JAPANESE_POST_ENABLE,
            android.device.provider.Settings.System.PDF417_ENABLE,        //pdf417
            android.device.provider.Settings.System.MICROPDF417_ENABLE,       //micro pdf417
            android.device.provider.Settings.System.COMPOSITE_CC_AB_ENABLE,     //composite_cc_ab
            android.device.provider.Settings.System.COMPOSITE_CC_C_ENABLE,      //composite_cc_c
            android.device.provider.Settings.System.COMPOSITE_TLC39_ENABLE,
            android.device.provider.Settings.System.HANXIN_ENABLE,
            android.device.provider.Settings.System.HANXIN_INVERSE,
            android.device.provider.Settings.System.DATAMATRIX_ENABLE,        //datamatrix
            android.device.provider.Settings.System.DATAMATRIX_LENGTH1,
            android.device.provider.Settings.System.DATAMATRIX_LENGTH2,
            android.device.provider.Settings.System.DATAMATRIX_INVERSE,
            android.device.provider.Settings.System.MAXICODE_ENABLE,          //maxicode
            android.device.provider.Settings.System.QRCODE_ENABLE,            //qrcode
            android.device.provider.Settings.System.QRCODE_INVERSE,
            android.device.provider.Settings.System.MICROQRCODE_ENABLE,
            android.device.provider.Settings.System.AZTEC_ENABLE,             //aztec code
            android.device.provider.Settings.System.AZTEC_INVERSE,
            android.device.provider.Settings.System.DEC_2D_LIGHTS_MODE,
            android.device.provider.Settings.System.DEC_2D_CENTERING_ENABLE,
            android.device.provider.Settings.System.DEC_2D_CENTERING_MODE,
            android.device.provider.Settings.System.DEC_2D_WINDOW_UPPER_LX,
            android.device.provider.Settings.System.DEC_2D_WINDOW_UPPER_LY,
            android.device.provider.Settings.System.DEC_2D_WINDOW_LOWER_RX,
            android.device.provider.Settings.System.DEC_2D_WINDOW_LOWER_RY,
            android.device.provider.Settings.System.DEC_2D_DEBUG_WINDOW_ENABLE,
            android.device.provider.Settings.System.DEC_ES_EXPOSURE_METHOD,        // Auto
            // Exposure Method
            android.device.provider.Settings.System.DEC_ES_TARGET_VALUE,// Target White Value
            android.device.provider.Settings.System.DEC_ES_TARGET_PERCENTILE,// Target Percentile
            android.device.provider.Settings.System.DEC_ES_TARGET_ACCEPT_GAP,// Target Acceptance
            // Gap
            android.device.provider.Settings.System.DEC_ES_MAX_EXP,// Maximum Exposure
            android.device.provider.Settings.System.DEC_ES_MAX_GAIN,// Maximum Gain
            android.device.provider.Settings.System.DEC_ES_FRAME_RATE,// Frame Rate
            android.device.provider.Settings.System.DEC_ES_CONFORM_IMAGE,// Image Must Conform
            android.device.provider.Settings.System.DEC_ES_CONFORM_TRIES,// Tries for Conform
            android.device.provider.Settings.System.DEC_ES_SPECULAR_EXCLUSION,// Exclude Specular
            // Regions
            android.device.provider.Settings.System.DEC_ES_SPECULAR_SAT,// Specular Saturation
            android.device.provider.Settings.System.DEC_ES_SPECULAR_LIMIT,// Specular Limit
            //android.device.provider.Settings.System.DEC_ES_FIXED_EXP = "DEC_ES_FIXED_EXP";
            // Fixed Exposure
            android.device.provider.Settings.System.DEC_ES_FIXED_GAIN,// Fixed Gain
            android.device.provider.Settings.System.DEC_ES_FIXED_FRAME_RATE,// Fixed Frame Rate
            android.device.provider.Settings.System.DEC_ILLUM_POWER_LEVEL,
            android.device.provider.Settings.System.DEC_PICKLIST_AIM_MODE,
            android.device.provider.Settings.System.DEC_PICKLIST_AIM_DELAY,
            android.device.provider.Settings.System.DEC_MaxMultiRead_COUNT,
            android.device.provider.Settings.System.DEC_Multiple_Decode_TIMEOUT,
            android.device.provider.Settings.System.DEC_Multiple_Decode_INTERVAL, /* urovo tao.he add, 20190314 */
            android.device.provider.Settings.System.DEC_Multiple_Decode_MODE,
            android.device.provider.Settings.System.DEC_OCR_MODE,
            android.device.provider.Settings.System.DEC_OCR_TEMPLATE,
            android.device.provider.Settings.System.DEC_OCR_USER_TEMPLATE,
            android.device.provider.Settings.System.SPECIFIC_CODE_GS,
            android.device.provider.Settings.System.TRANSMIT_CODE_ID,
            android.device.provider.Settings.System.CHARACTER_DATA_DELAY,
            android.device.provider.Settings.System.APPEND_ENTER_DELAY,
            android.device.provider.Settings.System.DOTCODE_ENABLE,
            android.device.provider.Settings.System.KOREA_POST_ENABLE,
            android.device.provider.Settings.System.POSTAL_GROUP_TYPE_ENABLE,
            android.device.provider.Settings.System.DEC_DECODE_DELAY,
            android.device.provider.Settings.System.DEC_DECODE_DEBUG_MODE,
            android.device.provider.Settings.System.OUT_CLIPBOARD_ENABLE,
            android.device.provider.Settings.System.DEC_EachImageAttempt_TIME,
            android.device.provider.Settings.System.DPM_DECODE_MODE,
            android.device.provider.Settings.System.LINEAR_1D_QUIET_ZONE_LEVEL,
            android.device.provider.Settings.System.CODE39_Quiet_Zone,
            android.device.provider.Settings.System.CODE39_START_STOP,
            android.device.provider.Settings.System.CODE39_SECURITY_LEVEL,
            android.device.provider.Settings.System.M25_SEND_CHECK,
            android.device.provider.Settings.System.M25_LENGTH1,
            android.device.provider.Settings.System.M25_LENGTH2,
            android.device.provider.Settings.System.I25_QUIET_ZONE,
            android.device.provider.Settings.System.I25_SECURITY_LEVEL,
            android.device.provider.Settings.System.CODABAR_ENABLE_CHECK,
            android.device.provider.Settings.System.CODABAR_SEND_CHECK,
            android.device.provider.Settings.System.CODABAR_SEND_START,
            android.device.provider.Settings.System.CODABAR_CONCATENATE,
            android.device.provider.Settings.System.CODE128_REDUCED_QUIET_ZONE,
            android.device.provider.Settings.System.CODE128_CHECK_ISBT_TABLE,
            android.device.provider.Settings.System.CODE_ISBT_Concatenation_MODE,
            android.device.provider.Settings.System.CODE128_SECURITY_LEVEL,
            android.device.provider.Settings.System.CODE128_IGNORE_FNC4,
            android.device.provider.Settings.System.UCC_REDUCED_QUIET_ZONE,
            android.device.provider.Settings.System.UCC_COUPON_EXT_REPORT_MODE,
            android.device.provider.Settings.System.UCC_EAN_ZERO_EXTEND,
            android.device.provider.Settings.System.UCC_EAN_SUPPLEMENTAL_MODE,
            android.device.provider.Settings.System.GS1_LIMIT_Security_Level,
            android.device.provider.Settings.System.COMPOSITE_UPC_MODE,
            android.device.provider.Settings.System.POSTAL_GROUP_TYPE_ENABLE,
            android.device.provider.Settings.System.KOREA_POST_ENABLE,
            android.device.provider.Settings.System.Canadian_POSTAL_ENABLE,
            android.device.provider.Settings.System.OUT_EDITORTEXT_MODE,
            android.device.provider.Settings.System.SUSPENSION_BUTTON,
            android.device.provider.Settings.System.CACHE_DATA_ENABLE,
            android.device.provider.Settings.System.CACHE_DATA_LIMIT_ENABLE,
            android.device.provider.Settings.System.CACHE_DATA_LIMIT_TIME,
            android.device.provider.Settings.System.DATAMATRIX_SYMBOL_SIZE,
            android.device.provider.Settings.System.MAXICODE_SYMBOL_SIZE,
            android.device.provider.Settings.System.QRCODE_SYMBOL_SIZE,
            android.device.provider.Settings.System.AZTEC_SYMBOL_SIZE,
            android.device.provider.Settings.System.LOW_POWER_SLEEP_MODE,
            android.device.provider.Settings.System.LOW_POWER_SLEEP_TIMEOUT,
            android.device.provider.Settings.System.LOW_CONTRAST_IMPROVED,
            android.device.provider.Settings.System.LOW_CONTRAST_IMPROVED_ALGORITHM,
            android.device.provider.Settings.System.SEND_TOKENS_OPTION,
            android.device.provider.Settings.System.SEND_TOKENS_FORMAT,
            android.device.provider.Settings.System.SEND_TOKENS_SEPARATOR,
            android.device.provider.Settings.System.ENABLE_PARSER_UDICODE,
            android.device.provider.Settings.System.ENABLE_FORMAT_UDI_DATE,
            android.device.provider.Settings.System.WEBJUMP,
            android.device.provider.Settings.System.OUTPUT_HEX_STRING_DATA,
            android.device.provider.Settings.System.GRIDMATRIX_ENABLED,
            android.device.provider.Settings.System.QR_WITHOUT_QZ,
            android.device.provider.Settings.System.QR_NON_SQUARE_MODULES,
            android.device.provider.Settings.System.C128_OUT_OF_SPEC,
            android.device.provider.Settings.System.CODING_FORMAT_NAME
    };

    @Override
    public void addScanCallBack(IScanCallBack cb) throws RemoteException {
        if(cb != null)
            mCallBacks.register(cb);
    }

    @Override
    public void removeScanCallBack(IScanCallBack cb) throws RemoteException {
        if(cb != null)
            mCallBacks.unregister(cb);
    }
}
