package android.device;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.ContentObserver;
import android.provider.Settings;
import android.hardware.input.InputManager;
import android.media.session.MediaSessionLegacyHelper;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IScanService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.IWindowManager;
import android.text.TextUtils;
import android.view.ViewConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import android.device.admin.*;

/**
 * Created by luol on 16-5-20.
 */
public class KeyMapManager {

    private static final String TAG = "KeyMapManager";

    private static boolean DEBUG = true;

    public static final Uri CONTENT_URI = Uri.parse("content://com.android.provider.keymap/keymap");

    public static final String AUTHORITY = "com.android.provider.keymap";

    public static final String TABLE_KEYMAP = "keymap";

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/";

    public static final String SETTINGS_KEYMAP_ENABLE = "settings_keymap_enable";

    public static final String KEY_ID = "_id";

    public static final String KEY_SCANCODE = "scancode";

    public static final String KEY_KEYCODE = "keycode";

    public static final String KEY_KEYCODE_META = "metaState";

    public static final String KEY_CHARACTER = "character";

    public static final String KEY_ACTIVITY = "activity";

    public static final String KEY_BROADCAST = "broadcast";

    public static final String KEY_DOWN_BROADCAST = "down";

    public static final String KEY_UP_BROADCAST = "up";

    public static final String KEY_TYPE = "type";

    public static final String KEY_WAKE = "wake";

    public static final String KEY_INTENT_ACTION = "action";

    public static final String KEY_INTENT_EXTRAS = "extras";

    public static final int KEY_TYPE_UNKNOWN = 0;

    public static final int KEY_TYPE_KEYCODE = 1;

    public static final int KEY_TYPE_UNICODE = 2;

    public static final int KEY_TYPE_STARTAC = 3;

    public static final int KEY_TYPE_STARTBC = 4;

    static final int MODE_KB_NUM = 0;
    static final int MODE_KB_LOWER = 1;
    static final int MODE_KB_UPPER = 2;
    static final int MODE_KB_SYMBOL = 3;
    int setMetaState = 0; //urovo zhoubo add  parameter for input type
    int getDeviceId = 0; //urovo zhoubo add parameter for keyremap

    private Context mContext;
    private IWindowManager mWindowManager;

    private SparseArray<String> mKeycodeArray;

    private ArrayList<KeyEntry> mKeyList = new ArrayList<>();

    private ContentResolver mResolver;
    private KeyMapObserver mObserver;
    private IScanService mScanService = null;
    private static boolean isInterception = false;
    private InjectKeyEventHandler mInjectHandler = null;
    private SettingsObserver mSettingsObserver;
    public static boolean mFnStatus = false;
    //urovo add for SQ47/SQ47C begin
    public static String project = SystemProperties.get("ro.boot.hardware.revision", ""); //V03:SQ47   V03C:SQ47C
    public static boolean mOrange = false; //status for OrangeButton(SQ47/SQ47C)
    public static boolean mBlue =false; //status for BlueButton(SQ47C)
    private int mLastNumberKeyCode = KeyEvent.KEYCODE_UNKNOWN;
    private int mCurrentNumberKeyIndex = 0;//SQ47C a->b->c->a
    private long mLastNumberKeyEventTimeStamp = -1;
    //urovo add for SQ47/SQ47C end
    public static boolean mFnLongPressed = false;
    public static int isDoubleClick = 0;
    public static boolean mFnKeepStatus = false;
    Handler mCheckForDoublePressHandler = new Handler();
    CheckForDoublePress mPendingCheckForDoublePress = new CheckForDoublePress();

    //add by xjf for PTT key action
    private String mPTTKeyEventDownAction = "android.intent.action.PTT_KEYDOWN";
    private String mPTTKeyEventUpAction = "android.intent.action.PTT_KEYUP";
    // urovo add luolin end 2018-12-03

    private boolean mLockScreenMode = false;
    private int mLockScreenFirstKey = KeyEvent.KEYCODE_POWER;
    private int mLockScreenSecondKey = KeyEvent.KEYCODE_VOLUME_UP;
    private boolean mFirstKeyPressed = false;
    private boolean mSecondKeyPressed = false;
    private boolean mIsLockScreenMode = false;

    private boolean mScreenOn = true;

    private List<Integer> mDisallowedKeyList = new ArrayList<>();

    private boolean scanpass = true; 
    private boolean mScanKeyPass = true;
    private int mNumberInputType;
    Map SQ47_KeyMap = new HashMap(); //urovo zhoubo add for SQ47 Key-Key matching
    private void keymapinit() {
        SQ47_KeyMap.put(KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_F11);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_F12);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_A);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_B);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_C);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_D);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_TAB, KeyEvent.KEYCODE_E);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_ALT_LEFT, KeyEvent.KEYCODE_F);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_M);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_N);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_O);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_J);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_K);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_L);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_G);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_H);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_I);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_COMMA, KeyEvent.KEYCODE_P);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_Q);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_PERIOD, KeyEvent.KEYCODE_R);        
        SQ47_KeyMap.put(KeyEvent.KEYCODE_F1, KeyEvent.KEYCODE_S);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_F2, KeyEvent.KEYCODE_T);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_F3, KeyEvent.KEYCODE_U);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_F4, KeyEvent.KEYCODE_V);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_F5, KeyEvent.KEYCODE_W);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_F6, KeyEvent.KEYCODE_X);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_F7, KeyEvent.KEYCODE_Y);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_F8, KeyEvent.KEYCODE_Z);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_F9, KeyEvent.KEYCODE_VOLUME_DOWN);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_F10, KeyEvent.KEYCODE_VOLUME_UP);
        SQ47_KeyMap.put(KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_SPACE);
    }

    private List<Integer>SQ47C_NUMBER = Arrays.asList(KeyEvent.KEYCODE_1,KeyEvent.KEYCODE_2,KeyEvent.KEYCODE_3,KeyEvent.KEYCODE_4,KeyEvent.KEYCODE_5,KeyEvent.KEYCODE_6,
                                            KeyEvent.KEYCODE_7,KeyEvent.KEYCODE_8,KeyEvent.KEYCODE_9,KeyEvent.KEYCODE_0,KeyEvent.KEYCODE_DEL,KeyEvent.KEYCODE_SPACE);
    private final int[][] SQ47C_NUMBER_CHAR = {
          //key, length, key1, key2, ...
          {KeyEvent.KEYCODE_1, 0, KeyEvent.KEYCODE_F1},
          {KeyEvent.KEYCODE_2, 3, KeyEvent.KEYCODE_F2,KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_C},
          {KeyEvent.KEYCODE_3, 3, KeyEvent.KEYCODE_F3,KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_F},
          {KeyEvent.KEYCODE_4, 3, KeyEvent.KEYCODE_F4,KeyEvent.KEYCODE_G, KeyEvent.KEYCODE_H, KeyEvent.KEYCODE_I},
          {KeyEvent.KEYCODE_5, 3, KeyEvent.KEYCODE_F5,KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_L},
          {KeyEvent.KEYCODE_6, 3, KeyEvent.KEYCODE_F6,KeyEvent.KEYCODE_M, KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_O},
          {KeyEvent.KEYCODE_7, 4, KeyEvent.KEYCODE_F7,KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_S},
          {KeyEvent.KEYCODE_8, 3, KeyEvent.KEYCODE_F8,KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_U, KeyEvent.KEYCODE_V},
          {KeyEvent.KEYCODE_9, 4, KeyEvent.KEYCODE_F9,KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_X, KeyEvent.KEYCODE_Y, KeyEvent.KEYCODE_Z},
          {KeyEvent.KEYCODE_0, 0, KeyEvent.KEYCODE_F10},
          {KeyEvent.KEYCODE_DEL, 0, KeyEvent.KEYCODE_VOLUME_DOWN},
          {KeyEvent.KEYCODE_SPACE, 0, KeyEvent.KEYCODE_VOLUME_UP},
    };

    public KeyMapManager(Context context) {
        mContext = context;
        mKeycodeArray = getKeyFieldNames();
        mResolver = mContext.getContentResolver();

        mInjectHandler = new InjectKeyEventHandler(mContext.getMainLooper());

        mObserver = new KeyMapObserver(this, mInjectHandler);
        mResolver.registerContentObserver(CONTENT_URI, true, mObserver);

        mSettingsObserver = new SettingsObserver(mInjectHandler);
        mSettingsObserver.observe();

        keymapinit();

    }

    public KeyMapManager(Context context, IWindowManager windowManager) {
        mWindowManager = windowManager;
        mContext = context;
        mKeycodeArray = getKeyFieldNames();
        mResolver = mContext.getContentResolver();

        mInjectHandler = new InjectKeyEventHandler(mContext.getMainLooper());

        mObserver = new KeyMapObserver(this, mInjectHandler);
        mResolver.registerContentObserver(CONTENT_URI, true, mObserver);

        mSettingsObserver = new SettingsObserver(mInjectHandler);
        mSettingsObserver.observe();

        keymapinit();

        IntentFilter filter = new IntentFilter();
        filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(mScreenReceiver, filter);

        mIsLockScreenMode = SystemProperties.getBoolean("persist.sys.lockscreen", false);
        if (mIsLockScreenMode) {
            SystemProperties.set("persist.sys.allow.touch", mIsLockScreenMode ? "1" : "0");
        }

        scanpass = SystemProperties.getBoolean("persist.sys.initscankeypass", true);
        if(scanpass){
            Settings.System.putString(mResolver, "UROVO_SCAN_PASS","true");
            SystemProperties.set("persist.sys.initscankeypass", "false");
        }

        mScanKeyPass = Boolean.parseBoolean(Settings.System.getString(mResolver, "UROVO_SCAN_PASS"));

        try {
            mPTTKeyEventUpAction = Settings.System.getString(mResolver, "UROVO_PTT_Up_ACTION");
            mPTTKeyEventDownAction = Settings.System.getString(mResolver, "UROVO_PTT_Down_ACTION");

            String enable = Settings.System.getString(mResolver, KeyMapManager.SETTINGS_KEYMAP_ENABLE);
            if (TextUtils.isEmpty(enable)) {
                isInterception = false;
            } else {
                isInterception = Boolean.parseBoolean(enable);
            }
            String action = Settings.System.getString(mResolver, "UROVO_LOCK_SCREEN");
            if (TextUtils.isEmpty(action)) {
                mLockScreenMode = false;
            } else {
                mLockScreenMode = Boolean.parseBoolean(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(mPTTKeyEventUpAction)) {
            mPTTKeyEventUpAction = "android.intent.action.PTT_KEYUP";
        }
        if (TextUtils.isEmpty(mPTTKeyEventDownAction)) {
            mPTTKeyEventDownAction = "android.intent.action.PTT_KEYDOWN";
        }

        mDisallowedKeyList.clear();
        String disallowedkeys = Settings.System.getString(mResolver, "UROVO_DISALLOWED_KEYCODES");
        if(!TextUtils.isEmpty(disallowedkeys)) {
            String[] arr = disallowedkeys.split(",");
            for(String s:arr) {
                mDisallowedKeyList.add(Integer.parseInt(s));
            }
        }
    }

    public void LogV(String msg) {
        if (DEBUG) Log.v(TAG, msg);
    }

    /**
     * @hide
     */
    public int dispatchUdroidKeyEvent(KeyEvent event, boolean isFactoryKeypadTop) {
        final int sourceKeycode = event.getKeyCode();
        final int sourceKeyAction = event.getAction();
        final boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        final int repeatCount = event.getRepeatCount();
        //zhangmeilin@urovo add for factory keypad test	
        boolean inKeypadTest = SystemProperties.getBoolean("persist.sys.factorykit.keytest", false);
        	

        //if (!ExDevicePolicyManager.get(mContext).getRestrictionPolicy().isKeyEventAllowed(sourceKeycode)) {
        if (mDisallowedKeyList.contains(sourceKeycode)) {
            return -1;
        }

        if (mIsLockScreenMode) {
            if (sourceKeycode < KeyEvent.KEYCODE_SCAN_1 || sourceKeycode > KeyEvent.KEYCODE_SCAN_4) {
                return -1;
            }
        }

        if (sourceKeycode == KeyEvent.KEYCODE_SCAN_1 || sourceKeycode == KeyEvent.KEYCODE_SCAN_2
                || sourceKeycode == KeyEvent.KEYCODE_SCAN_3 || sourceKeycode == KeyEvent.KEYCODE_SCAN_4) {
            
            if(isFactoryKeypadTop) {
                return 0;
            }

            //add by wujinquan
            String scankeylist = "520-521-522-523-";
            String rfidkeylist = SystemProperties.get("persist.sys.rfid.key", "");
            if(!TextUtils.isEmpty(rfidkeylist)) {
                scankeylist = SystemProperties.get("persist.sys.scan.key", "520-521-522-523-");
            }
            int ret = 0;
            String[] rfidtableProperty = rfidkeylist.split("-");
            if(!TextUtils.isEmpty(rfidkeylist)){
                for(int i=0;i<rfidtableProperty.length;i++){
                    if(String.valueOf(sourceKeycode).equals(rfidtableProperty[i])){
                        Log.v(TAG, "---rfid  keyCode:"+sourceKeycode);
                        Intent Uhfintent = new Intent("ACTION_SCANLABLE_START_DECODE");
                        if(down){
                            if(repeatCount == 0){
                                Uhfintent.putExtra("StartUhf", true);
                                mContext.sendBroadcast(Uhfintent);
                            }
                        }else{
                            Uhfintent.putExtra("StartUhf", false);
                            mContext.sendBroadcast(Uhfintent);
                        } 
                        ret = -1;
                    }
                }
            }
     
            String[] scantableProperty = scankeylist.split("-");
            if(!TextUtils.isEmpty(scankeylist)){
                for(int i=0;i<scantableProperty.length;i++){
                    if(String.valueOf(sourceKeycode).equals(scantableProperty[i])){
                        Log.v(TAG, "---scan  keyCode:"+sourceKeycode);
                        ret = triggerScan(down, repeatCount);                  
                    }
                }
            }
            //urovo add jinpu.lin begin 2019.06.19
            if (mScanKeyPass) {
                return 0;
            }
            return ret;
            //add end
        } else if (sourceKeycode == KeyEvent.KEYCODE_KEYBOARD_PTT) {
            //urovo modify jinpu.lin begin 2019.06.19
            if (down) {
                if (repeatCount == 0) {
                    mContext.sendBroadcast(new Intent(mPTTKeyEventDownAction));
                }
            } else {
                mContext.sendBroadcast(new Intent(mPTTKeyEventUpAction));
            }
            //urovo modify jinpu.lin end 2019.06.19
            return 0;
        } else if (sourceKeycode == KeyEvent.KEYCODE_KEYBOARD_TALK && sourceKeyAction == KeyEvent.ACTION_DOWN){
            if(project.equals("V03C") && !inKeypadTest){
                changeMode(2);
                return -1;
            }
        } else if (sourceKeycode == KeyEvent.KEYCODE_SHIFT_LEFT && sourceKeyAction == KeyEvent.ACTION_DOWN){
            if(project.equals("V03") && !inKeypadTest){
                checkForDoubleClick(event);
                return -1;
            } else if(project.equals("V03C") && !inKeypadTest){
                changeMode(3);
                return -1;
            } else {
                mFnKeepStatus = false;
                if (mFnStatus) {
                    mFnStatus = false;
                    sendFnStatus();
                    return -1;
                } else {
                    return 0;
                }
            }
        } else if (sourceKeycode == KeyEvent.KEYCODE_KEYBOARD_FN && sourceKeyAction == KeyEvent.ACTION_DOWN) {//KeyEvent.KEYCODE_FUNCTION
            if(project.equals("V03") && !inKeypadTest ){
                int state = Settings.Global.getInt(mContext.getContentResolver(),"ufans.keyboard.state", MODE_KB_NUM);
                Log.d(TAG," state="+state);
                mNumberInputType = MODE_KB_NUM;
                if(state==MODE_KB_NUM){
                    mNumberInputType = MODE_KB_LOWER;
                    mOrange = true;
                } else if (state==MODE_KB_LOWER){
                    mNumberInputType = MODE_KB_NUM;
                    mOrange = false;
                } else if (state==MODE_KB_UPPER){
                    mNumberInputType = MODE_KB_SYMBOL;
                    mOrange = false;
                } else if (state==MODE_KB_SYMBOL){
                    mNumberInputType = MODE_KB_UPPER;
                    mOrange = true;
                }
                Settings.Global.putInt(mContext.getContentResolver(),"ufans.keyboard.state", mNumberInputType);
                Log.d(TAG,"mNumberInputType =====" + mNumberInputType);
                InputIconChange();
                return -1;
            // } else {
            //     checkForDoubleClick(event);
            //     return -1;
            }
            if(project.equals("V03C") && !inKeypadTest){
                changeMode(1);
                return -1;
            }

        }

        int remapKeycode = getKeyCode(sourceKeycode); //  check if it is mapped
        if (remapKeycode != -1 && sourceKeycode != remapKeycode){
            Log.d(TAG,"do remap + remapKeycode======="+ remapKeycode + "      sourceKeycode======="+ sourceKeycode);
            getDeviceId = KeyCharacterMap.VIRTUAL_KEYBOARD;
            if (sourceKeyAction == KeyEvent.ACTION_DOWN) {
                    mInjectHandler.processDown(sourceKeyAction, remapKeycode);
                } else if (sourceKeyAction == KeyEvent.ACTION_UP) {
                    mInjectHandler.processUp(sourceKeyAction, remapKeycode);
                    Log.d(TAG,"mFnStatus=========when up =======" + mFnStatus);
                    if ( !mFnKeepStatus && mFnStatus) {
                        onShiftClick();
                        InputIconChange();
                        Log.d(TAG,"cancel the shift");
                    }
                }
            // if (sourceKeyAction == KeyEvent.ACTION_UP && !mFnKeepStatus && mFnStatus) {
            //     mFnStatus = false;
            //     sendFnStatus();
            // }
            return -1;
        } else {
            return 0;
        }
    }

    final private class CheckForDoublePress implements Runnable {

        public void run() {
            if (isDoubleClick == 2 && !mFnKeepStatus) {
                if(!mFnStatus){
                    onShiftClick();
                }
                
                mFnKeepStatus = true;
                // sendFnStatus();
                InputIconChange();
            } else{
                //mFnStatus = !mFnStatus;
                onShiftClick();
                mFnKeepStatus = false;
                // sendFnStatus();
                InputIconChange();
            }
            isDoubleClick = 0;
            Log.d(TAG,"mFnKeepStatus ======="+ mFnKeepStatus);
        }

    }

    private void checkForDoubleClick(KeyEvent event) {
        if (isDoubleClick == 0) {
            if (mPendingCheckForDoublePress != null) {
                mCheckForDoublePressHandler.postDelayed(mPendingCheckForDoublePress, ViewConfiguration.getLongPressTimeout());
            }
            isDoubleClick++;
        } else {
            if (mPendingCheckForDoublePress != null) {
                mCheckForDoublePressHandler.removeCallbacks(mPendingCheckForDoublePress);
                mCheckForDoublePressHandler.post(mPendingCheckForDoublePress);
            }
            isDoubleClick++;
        }
    }

    private void sendFnStatus() {
        Intent intent = new Intent("action.fn.changed");
        intent.putExtra("Fn_status", mFnStatus);
        mContext.sendBroadcast(intent);
    }

    // urovo add for SQ47 update input statusbar 
    private void InputIconChange() {
        Intent intent = new Intent("android.intent.action.ACTION_INPUT_STATE");
        intent.putExtra("Shift_status",mFnStatus);
        intent.putExtra("Keep_shift",mFnKeepStatus);
        intent.putExtra("Orange_status",mOrange);
        intent.putExtra("Blue_status",mBlue);
        mContext.sendBroadcast(intent);
    }

    private void sendbrightnessStatus() {
        Intent intent = new Intent("com.android.intent.action.SHOW_BRIGHTNESS_DIALOG");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    //urovo  end


    public int dispatchKeyMapEvent(KeyEvent event, boolean keyguardOn) {
        int keycode = event.getKeyCode();
        Log.d(TAG,"keycode======" + keycode);
        final int keyAction = event.getAction();
        // urovo zhoubo add when it is not mapped
        if (project.equals("V03") && SQ47_KeyMap.containsKey(keycode) && !hasKeyEntry(keycode)){
            getDeviceId = 0;
            if (keycode == KeyEvent.KEYCODE_BACK){//返回键单独处理，执行返回键功能时在PhoneWindowManager进行处理
                if (mOrange){
                    keycode = (int)SQ47_KeyMap.get(keycode);
                } else {
                    return 0;
                }
            } else {
                if (mNumberInputType == MODE_KB_LOWER){
                    setMetaState = 0;
                    keycode = (int)SQ47_KeyMap.get(keycode);
                } else if (mNumberInputType == MODE_KB_UPPER){
                    setMetaState = KeyEvent.META_CAPS_LOCK_ON;
                    keycode = (int)SQ47_KeyMap.get(keycode);
                } else if (mNumberInputType == MODE_KB_NUM){
                    setMetaState = KeyEvent.META_NUM_LOCK_ON;
                } else if (mNumberInputType == MODE_KB_SYMBOL){
                    setMetaState = KeyEvent.META_SHIFT_ON;
                }
            }
            if (keyAction == KeyEvent.ACTION_DOWN) {
                mInjectHandler.processDown(keyAction, keycode);
            } else if (keyAction == KeyEvent.ACTION_UP) {
                mInjectHandler.processUp(keyAction, keycode);
                Log.d(TAG,"mFnStatus=========when up =======" + mFnStatus);
                if ( !mFnKeepStatus && mFnStatus) {
                    onShiftClick();
                    InputIconChange();
                    Log.d(TAG,"cancel the shift");
                }
            }
            return -1;
        }
        if (project.equals("V03C") && SQ47C_NUMBER.contains(keycode) && !hasKeyEntry(keycode)){
            getDeviceId = 0;
            if(mBlue){//fn模式
                for(int i=0; i < SQ47C_NUMBER_CHAR.length; i++){
                    if(keycode == SQ47C_NUMBER_CHAR[i][0]){
                        keycode = SQ47C_NUMBER_CHAR[i][2];
                        break;
                    }
                }
                if (keyAction == KeyEvent.ACTION_DOWN) {
                    mInjectHandler.processDown(keyAction, keycode);
                } else if (keyAction == KeyEvent.ACTION_UP) {
                    mInjectHandler.processUp(keyAction, keycode);
                }
                return -1;
            }
            if(mOrange && keycode >= KeyEvent.KEYCODE_2 && keycode <=KeyEvent.KEYCODE_9){//橙色键按下状态，只处理2-9键
                if(keyAction == KeyEvent.ACTION_DOWN){
                    for(int i=0; i < SQ47C_NUMBER_CHAR.length; i++){//遍历列表，确定所按下的键
                        if(keycode != SQ47C_NUMBER_CHAR[i][0]){
                            continue;
                        }
                        if(mFnStatus){
                            setMetaState = KeyEvent.META_CAPS_LOCK_ON;//输出大写 讯飞输入法不生效
                        } else {
                            setMetaState = 0;//输出小写
                        }
                        if(mLastNumberKeyCode != SQ47C_NUMBER_CHAR[i][0]){
                            initState(SQ47C_NUMBER_CHAR[i][0]);
                            keycode = SQ47C_NUMBER_CHAR[i][3];
                        } else if (mLastNumberKeyCode == SQ47C_NUMBER_CHAR[i][0] && inTimeArea(mLastNumberKeyEventTimeStamp, 500)){
                            mInjectHandler.processDown(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);//输出前先删除前一个字母
                            mCurrentNumberKeyIndex = (mCurrentNumberKeyIndex + 1) % SQ47C_NUMBER_CHAR[i][1];
                            mLastNumberKeyEventTimeStamp = SystemClock.uptimeMillis();
                            keycode = SQ47C_NUMBER_CHAR[i][mCurrentNumberKeyIndex + 3];
                        } else if(mLastNumberKeyCode == SQ47C_NUMBER_CHAR[i][0]){
                            initState(SQ47C_NUMBER_CHAR[i][0]);
                            keycode = SQ47C_NUMBER_CHAR[i][3];
                        }
                        mInjectHandler.processDown(KeyEvent.ACTION_DOWN, keycode);
                        mInjectHandler.processUp(KeyEvent.ACTION_UP, keycode);
                        return -1;
                    }
                }
                return -1;
            }
            return 0;
        }

        if (mLockScreenMode) {
            if (keycode == mLockScreenFirstKey) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    mFirstKeyPressed = true;
                } else {
                    mFirstKeyPressed = false;
                }
            } else if (keycode == mLockScreenSecondKey) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    mSecondKeyPressed = true;
                } else {
                    mSecondKeyPressed = false;
                }
            } else {
                mFirstKeyPressed = false;
                mSecondKeyPressed = false;
            }

            if (mFirstKeyPressed && mSecondKeyPressed) {
                mIsLockScreenMode = !mIsLockScreenMode;
                SystemProperties.set("persist.sys.lockscreen", mIsLockScreenMode ? "true" : "false");
                SystemProperties.set("persist.sys.allow.touch", mIsLockScreenMode ? "1" : "0");
            }
        }

        if (!isInterception())
            return 0;

        KeyEntry entry = getKeyEntry(keycode);

        if (entry != null) {
            LogV("dispatchKeyMapEvent  " + keycode);
            final int repeatCount = event.getRepeatCount();
            // LogV("repeatCount " + repeatCount);
            // send broadcast
            if (repeatCount == 0) {
                switch (event.getAction()) {
                    case KeyEvent.ACTION_DOWN: {
                        Intent intent = getBroadcast(entry.broadcast, KEY_DOWN_BROADCAST);
                        if (intent != null) {
                            mContext.sendBroadcast(intent);
                        }
                        break;
                    }
                    case KeyEvent.ACTION_UP: {
                        Intent intent = getBroadcast(entry.broadcast, KEY_UP_BROADCAST);
                        if (intent != null) {
                            mContext.sendBroadcast(intent);
                        }
                        break;
                    }
                }
            }
            //do remapped event
            switch (entry.type) {
                case KEY_TYPE_KEYCODE: {
                    if (keycode == entry.keycode_meta) {
                        return 0;
                    }
                    // performClick
 //                   final int keyAction = event.getAction();
                    if (entry.keycode_meta == KeyEvent.KEYCODE_BACK) {
                        if (keyAction == KeyEvent.ACTION_UP) {
                            sendDownAndUpKeyEvents(KeyEvent.KEYCODE_BACK);//返回键特别处理，否则会失效
                        }
                    } else {
                        if (keyAction == KeyEvent.ACTION_DOWN) {
                            mInjectHandler.processDown(keyAction, entry.keycode_meta);
                        } else if (keyAction == KeyEvent.ACTION_UP) {
                            mInjectHandler.processUp(keyAction, entry.keycode_meta);
                        }
                    }
                    LogV("type = " + entry.type + "  keycode_meta=" + entry.keycode_meta);
                    return -1;
                }
                case KEY_TYPE_STARTAC: {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && !keyguardOn && repeatCount == 0) {
                        Intent intent = getLauncherActivity(entry.activity);
                        if (intent == null) {
                            return 0;
                        } else {
                            mContext.startActivity(intent);
                        }
                    }
                    LogV("type = " + entry.type + "  activity=" + entry.activity);
                    return -1;
                }
                case KEY_TYPE_STARTBC: {
                    LogV("type = " + entry.type + "  broadcast");
                    return 0;
                }
                case KEY_TYPE_UNKNOWN: {
                    LogV("type = " + entry.type + "  unknown");
                    return 0;
                }
                default:
                    return 0;
            }
        }
        return 0;
    }

    final class InjectKeyEventHandler extends Handler {
        private static final int MSG_INJECT_DOWN = 1;
        private static final int MSG_INJECT_REPEAT = 2;
        private static final int MSG_INJECT_UP = 3;

        private KeyEvent mKeyEvent = null;

        public InjectKeyEventHandler() {
            super();
        }

        public InjectKeyEventHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            KeyEvent keyEvent = (KeyEvent) msg.obj;
            if (keyEvent == null) {
                return;
            }
            switch (msg.what) {
                case MSG_INJECT_DOWN:
                    repeatKeyEvent(keyEvent, ViewConfiguration.getLongPressTimeout());
                    break;
                case MSG_INJECT_REPEAT:
                    repeatKeyEvent(keyEvent, ViewConfiguration.getKeyRepeatDelay());
                    break;
                case MSG_INJECT_UP:
                    repeatKeyEvent(keyEvent, -1);
                    break;
            }
        }

        public void processDown(int keyAction, int keyCode) {
            Message message = obtainMessage(MSG_INJECT_DOWN, injectKeyEvent(keyAction, keyCode));
            message.setAsynchronous(true);
            sendMessage(message);
            LogV("Inject processDown");
        }

        public void processUp(int keyAction, int keyCode) {
            //removeMessages(MSG_INJECT_REPEAT);
            Message message = obtainMessage(MSG_INJECT_UP, injectKeyEvent(keyAction, keyCode));
            message.setAsynchronous(true);
            sendMessage(message);
            LogV("Inject processUp");
        }

        private KeyEvent injectKeyEvent(int keyAction, int keyCode) {
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis();
            // final KeyEvent keyEvent = new KeyEvent(downTime, eventTime, keyAction, keyCode, 0, 0,
            //         KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_KEYBOARD);
            final KeyEvent keyEvent = new KeyEvent(downTime, eventTime, keyAction, keyCode, 0, setMetaState,
                        getDeviceId, 0, KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_KEYBOARD);
            return keyEvent;
        }

        private void repeatKeyEvent(KeyEvent keyEvent, int delay) {
            InputManager.getInstance().injectInputEvent(keyEvent, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
            if (delay != -1) {
                Message message = obtainMessage(MSG_INJECT_REPEAT, KeyEvent.changeTimeRepeat(keyEvent,
                        SystemClock.uptimeMillis(), keyEvent.getRepeatCount() + 1));
                message.setAsynchronous(true);
                sendMessageDelayed(message, delay);
            } else {
                LogV("removeMessages(MSG_INJECT_REPEAT) & removeMessages(MSG_INJECT_DOWN)");
                removeMessages(MSG_INJECT_REPEAT);
                removeMessages(MSG_INJECT_DOWN);
                keyEvent.recycle();
            }
        }
    }

    private void sendDownAndUpKeyEvents(int keyCode) {
        // Inject down.
        final long downTime = SystemClock.uptimeMillis();
        KeyEvent down = KeyEvent.obtain(downTime, downTime, KeyEvent.ACTION_DOWN, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_FROM_SYSTEM,
                InputDevice.SOURCE_KEYBOARD, null);
        InputManager.getInstance().injectInputEvent(down,
                InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
        down.recycle();

        // Inject up.
        final long upTime = SystemClock.uptimeMillis();
        KeyEvent up = KeyEvent.obtain(downTime, upTime, KeyEvent.ACTION_UP, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0, KeyEvent.FLAG_FROM_SYSTEM,
                InputDevice.SOURCE_KEYBOARD, null);
        InputManager.getInstance().injectInputEvent(up,
                InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
        up.recycle();
    }

    public synchronized void updateKeyMap() {
        try {
            String enable = Settings.System.getString(mResolver, KeyMapManager.SETTINGS_KEYMAP_ENABLE);
            if (TextUtils.isEmpty(enable)) {
                isInterception = false;
            } else {
                isInterception = Boolean.parseBoolean(enable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mKeyList != null) {
            mKeyList.clear();
            getKeyList();
        }
    }

    public boolean hasKeyEntry(int keycode) {
        KeyEntry entry = getKeyEntry(keycode);
        return entry != null;
    }

    public void delKeyEntry(int keycode) {
        mResolver.delete(KeyMapManager.CONTENT_URI,
                "keycode=?", new String[]{String.valueOf(keycode)});
        updateKeyMap();
    }

    public void mapKeyEntry(KeyEvent event, int type, String meta) {
        mapKeyEntry(event, type, meta, "");
    }

    public void mapKeyEntry(KeyEvent event, int type, String meta, String broadcast) {
        mapKeyEntry(event, type, meta, broadcast, 0);
    }

    public void mapKeyEntry(KeyEvent event, int type, String meta, String broadcast, int wake) {
        KeyEntry entry = new KeyEntry();
        entry.scancode = event.getScanCode();
        entry.keycode = event.getKeyCode();
        entry.type = type;
        entry.broadcast = broadcast;
        entry.wake = wake;

        switch (entry.type) {
            case KEY_TYPE_KEYCODE:
                entry.keycode_meta = Integer.parseInt(meta);
                break;
            case KEY_TYPE_UNICODE:
                entry.character = meta;
                break;
            case KEY_TYPE_STARTAC:
                entry.activity = meta;
                break;
            default:
                break;
        }

        mapKeyMap(entry);
        if (wake == 1) {
            setWakeKey(event.getKeyCode(), true);
        }
    }

    public boolean isInterception() {
        String enable = Settings.System.getString(mResolver, KeyMapManager.SETTINGS_KEYMAP_ENABLE);
        if (enable == null) {
            return false;
        }
        return Boolean.parseBoolean(enable);
    }

    public void disableInterception(boolean interception) {
        isInterception = interception;
        Settings.System.putString(mResolver, KeyMapManager.SETTINGS_KEYMAP_ENABLE, Boolean.toString(interception));
    }


    public void setWakeKey(int keycode, boolean wake) {
        String b = Boolean.toString(wake);
        String keyName = "KEYCODE_" + mKeycodeArray.get(keycode, "UNKNOW");
        Settings.System.putString(mResolver, keyName, b);
        Log.w(TAG, "setWakeKey : " + keyName + "   " + b);
    }

    public boolean isWakeKey(int keycode) {
        String enable = "false";
        String keyName = "KEYCODE_" + mKeycodeArray.get(keycode, "UNKNOW");
        enable = Settings.System.getString(mResolver, keyName);
        Log.w(TAG, "isWakeKey : " + keyName + "   " + enable);
        return Boolean.parseBoolean(enable);
    }

    public int getKeyCode(int keycode) {
        KeyEntry tempKey = getKeyEntry(keycode);
        return tempKey == null ? -1 : tempKey.keycode;
    }

    public int getKeyMeta(int keycode) {
        KeyEntry tempKey = getKeyEntry(keycode);
        return tempKey == null ? -1 : tempKey.keycode_meta;
    }

    public String getKeyActivity(int keycode) {
        KeyEntry tempKey = getKeyEntry(keycode);
        return tempKey == null ? null : tempKey.activity;
    }

    public String getKeyBroadcast(int keycode) {
        KeyEntry tempKey = getKeyEntry(keycode);
        return tempKey == null ? null : tempKey.broadcast;
    }

    public String getKeyUnicode(int keycode) {
        KeyEntry tempKey = getKeyEntry(keycode);
        return tempKey == null ? null : tempKey.character;
    }

    public int getKeyType(int keycode) {
        KeyEntry tempKey = getKeyEntry(keycode);
        return tempKey == null ? -1 : tempKey.type;
    }

    public int getKeyWake(int keycode) {
        KeyEntry tempKey = getKeyEntry(keycode);
        return tempKey == null ? 0 : tempKey.wake;
    }

    private KeyEntry getKeyEntry(int keycode) {
        if (mKeyList == null || mKeyList.size() <= 0) {
            getKeyList();
        }

        if (mKeyList != null && mKeyList.size() > 0) {
            for (KeyEntry tempKey : mKeyList) {
                if (tempKey.keycode == keycode) {
                    return tempKey;
                }
            }
        }
        return null;
    }

    public List<KeyEntry> getKeyList() {
        mKeyList.clear();
        Cursor cursor = mContext.getContentResolver().query(CONTENT_URI, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                KeyEntry entry;
                do {
                    entry = new KeyEntry();
                    entry.scancode = cursor.getInt(cursor.getColumnIndex(KEY_SCANCODE));
                    entry.keycode = cursor.getInt(cursor.getColumnIndex(KEY_KEYCODE));
                    entry.keycode_meta = cursor.getInt(cursor.getColumnIndex(KEY_KEYCODE_META));
                    entry.character = cursor.getString(cursor.getColumnIndex(KEY_CHARACTER));
                    entry.activity = cursor.getString(cursor.getColumnIndex(KEY_ACTIVITY));
                    entry.broadcast = cursor.getString(cursor.getColumnIndex(KEY_BROADCAST));
                    entry.type = cursor.getInt(cursor.getColumnIndex(KEY_TYPE));
                    entry.wake = isWakeKey(entry.keycode) ? 1 : 0;

                    if (KEY_TYPE_UNKNOWN != entry.type) {
                        mKeyList.add(entry);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return mKeyList;
    }

    @Deprecated
    public SparseArray<String> getKeyFieldNames() {
        Field[] keyFields = KeyEvent.class.getDeclaredFields();
        SparseArray<String> keycodeArray = new SparseArray<>();
        String tmpName = null;
        try {
            for (int i = 0; i < keyFields.length; i++) {
                tmpName = keyFields[i].getName();
                int modifiers = keyFields[i].getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                    if (tmpName.startsWith("KEYCODE_")) {
                        tmpName = tmpName.replace("KEYCODE_", "");
                        keycodeArray.put((Integer) keyFields[i].get(null), tmpName);
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.w(TAG, "Non-static field : " + tmpName);
        } catch (IllegalArgumentException e1) {
            Log.w(TAG, "Type mismatch : " + tmpName);
        } catch (IllegalAccessException e2) {
            Log.w(TAG, "Non-public field : " + tmpName);
        }
        return keycodeArray;
    }

    private void mapKeyMap(KeyEntry entry) {
        if (entry.keycode == 0) return;

        ContentValues values = new ContentValues();
        values.put(KEY_SCANCODE, entry.scancode);
        values.put(KEY_KEYCODE, entry.keycode);
        values.put(KEY_KEYCODE_META, entry.keycode_meta);
        values.put(KEY_CHARACTER, entry.character);
        values.put(KEY_ACTIVITY, entry.activity);
        values.put(KEY_BROADCAST, entry.broadcast);
        values.put(KEY_TYPE, entry.type);
        values.put(KEY_WAKE, entry.wake);

        mContext.getContentResolver().insert(CONTENT_URI, values);
        updateKeyMap();
    }

    private Intent getBroadcast(String jsonString, String type) {
        if (!(KEY_DOWN_BROADCAST.equals(type) || KEY_UP_BROADCAST.equals(type))) {
            LogV("undefined broadcast type:" + type);
            return null;
        }
        JSONObject jsonObject = getJSONObjectFromString(jsonString);
        if (jsonObject == null) {
            return null;
        }
        try {
            JSONObject broadcast = jsonObject.getJSONObject(type);
            String action = broadcast.getString(KEY_INTENT_ACTION);
            LogV("broadcast type:" + type + ",broadcast action:" + action);
            if (action == null || action.isEmpty()) {
                LogV("broadcast action:" + action + " 's=null");
                return null;
            } else {
                Intent intent = new Intent(action);
                try {
                    Bundle extras = getExtrasFromJSON(broadcast.getJSONObject(KEY_INTENT_EXTRAS));
                    printBundle(extras, "broadcast-" + type);
                    if (extras != null) {
                        intent.putExtras(extras);
                    }
                } catch (JSONException e) {
                    // e.printStackTrace();
                    Log.w(TAG, "JSONException!!!");
                }
                return intent;
            }
        } catch (JSONException e) {
            // e.printStackTrace();
            Log.w(TAG, "JSONException!!!");
            return null;
        }
    }

    private Intent getLauncherActivity(String jsonString) {
        JSONObject jsonObject = getJSONObjectFromString(jsonString);
        if (jsonObject == null) {
            return null;
        }
        try {
            String action = jsonObject.getString(KEY_INTENT_ACTION);
            ComponentName componentName = getComponentNameFromString(action);
            Intent intent = null;
            if (componentName != null) {
                intent = new Intent();
                intent.setComponent(componentName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            } else {
                PackageManager packageManager = mContext.getPackageManager();
                intent = packageManager.getLaunchIntentForPackage(action);
            }
            if (intent == null) {
                LogV("activity action:" + action + " 's=null");
                return null;
            } else {
                try {
                    Bundle extras = getExtrasFromJSON(jsonObject.getJSONObject(KEY_INTENT_EXTRAS));
                    printBundle(extras, "activity");
                    if (extras != null) {
                        intent.putExtras(extras);
                    }
                } catch (JSONException e) {
                    // e.printStackTrace();
                    Log.w(TAG, "JSONException!!!");
                }
                return intent;
            }
        } catch (JSONException e) {
            // e.printStackTrace();
            Log.w(TAG, "JSONException!!!");
            return null;
        }
    }

    public JSONObject getJSONObjectFromString(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject;
        } catch (JSONException e) {
            // e.printStackTrace();
            Log.w(TAG, "JSONException!!!");
            return null;
        }
    }

    public void printBundle(Bundle bundle, String flag) {
        if (DEBUG) {
            for (String key : bundle.keySet()) {
                Log.v(TAG, flag + "-Extra Key=" + key + ", value=" + bundle.getString(key));
            }
        }
    }

    public Bundle getExtrasFromJSON(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                Bundle result = new Bundle();
                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String value = (String) jsonObject.get(key);
                    result.putString(key, value);
                }
                return result;
            } catch (JSONException e) {
                // e.printStackTrace();
                Log.w(TAG, "JSONException!!!");
                return null;
            }
        } else {
            return null;
        }
    }

    public ComponentName getComponentNameFromString(String compStr) {
        if (compStr == null)
            return null;
        int sep = compStr.indexOf(' ');
        if (sep < 0 || (sep + 1) >= compStr.length()) {
            return null;
        }
        String cls = compStr.substring(0, sep);
        String pkg = compStr.substring(sep + 1);
        if (cls.length() > 0 && cls.charAt(0) == '.') {
            cls = pkg + cls;
        }
        return new ComponentName(pkg, cls);
    }

    public static class KeyEntry {
        public int type = 0;
        public int scancode = 0;
        public int keycode = 0;
        public int keycode_meta = 0;
        public String character = "";
        public String activity = "";
        public String broadcast = "";
        public int wake = 0;

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            else {
                if (obj instanceof KeyEntry) {
                    boolean res = this.keycode == ((KeyEntry) obj).keycode;
                    return res;
                }
            }
            return false;
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            // Observe all users' changes
            ContentResolver resolver = mContext.getContentResolver();

            //urovo add by xjf for PPT KEY action 20190513 
            resolver.registerContentObserver(Settings.System.getUriFor("UROVO_PTT_Up_ACTION"), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor("UROVO_PTT_Down_ACTION"), false, this,
                    UserHandle.USER_ALL);
            //urovo add by xjf for PPT KEY action 20190513
            resolver.registerContentObserver(Settings.System.getUriFor(KeyMapManager.SETTINGS_KEYMAP_ENABLE), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor("UROVO_LOCK_SCREEN"), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor("UROVO_DISALLOWED_KEYCODES"), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(Settings.System.getUriFor("UROVO_SCAN_PASS"), false, this,
                    UserHandle.USER_ALL);
					//zhangmeilin@urovo add for update input icon 
            resolver.registerContentObserver(Settings.Global.getUriFor("ufans.keyboard.state"), false, this,
                    UserHandle.USER_ALL);		
        }

        @Override
        public void onChange(boolean selfChange) {
            try {
                mPTTKeyEventUpAction = Settings.System.getString(mResolver, "UROVO_PTT_Up_ACTION");
                mPTTKeyEventDownAction = Settings.System.getString(mResolver, "UROVO_PTT_Down_ACTION");

                String enable = Settings.System.getString(mResolver, KeyMapManager.SETTINGS_KEYMAP_ENABLE);
                if (TextUtils.isEmpty(enable)) {
                    isInterception = false;
                } else {
                    isInterception = Boolean.parseBoolean(enable);
                }
                String action = Settings.System.getString(mResolver, "UROVO_LOCK_SCREEN");
                if (TextUtils.isEmpty(action)) {
                    mLockScreenMode = false;
                } else {
                    mLockScreenMode = Boolean.parseBoolean(action);
                }

                mLockScreenFirstKey = Settings.System.getInt(mResolver, "key_lock_first", KeyEvent.KEYCODE_POWER);
                mLockScreenSecondKey = Settings.System.getInt(mResolver, "key_lock_second", KeyEvent.KEYCODE_VOLUME_UP);

                String disallowedkeys = Settings.System.getString(mResolver, "UROVO_DISALLOWED_KEYCODES");
                mDisallowedKeyList.clear();
                if(!TextUtils.isEmpty(disallowedkeys)) {
                    String[] arr = disallowedkeys.split(",");
                    for(String s:arr) {
                        mDisallowedKeyList.add(Integer.parseInt(s));
                    }
                }

                mScanKeyPass = Boolean.parseBoolean(Settings.System.getString(mResolver, "UROVO_SCAN_PASS"));
                mNumberInputType = Settings.Global.getInt(mContext.getContentResolver(),"ufans.keyboard.state", MODE_KB_NUM);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (TextUtils.isEmpty(mPTTKeyEventUpAction)) {
                    mPTTKeyEventUpAction = "android.intent.action.PTT_KEYUP";
                }
                if (TextUtils.isEmpty(mPTTKeyEventDownAction)) {
                    mPTTKeyEventDownAction = "android.intent.action.PTT_KEYDOWN";
                }
            }
        }
    }

    // urovo add luolin begin 2018-12-03
    private int triggerScan(boolean down, int repeatCount) {
        int stat = android.provider.Settings.System.getInt(mContext.getContentResolver(), "urovo_scan_stat", 1);
        int scanApp = android.provider.Settings.System.getInt(mContext.getContentResolver(), "urovo_scan_app", 0);
        LogV("mScreenOn = " + mScreenOn);
        if (Build.PROJECT.equals("SQ53C")) {
            if (!mScreenOn) return 0;//juzhitao modify for bug:kill camera app cannot use scankey
        } else {
            if (stat == 0 || !mScreenOn) return 0;
        }
        if (scanApp == 0) {
            try {
                mScanService = android.os.IScanService.Stub.asInterface(ServiceManager.getService(Context.SCAN_SERVICE));
                if (mScanService != null) {
                    if (repeatCount == 0)
                        mScanService.init();
                    int triggerMode = mScanService.getPropertyInt(8);
                    if (triggerMode == 4) {
                        if (down && repeatCount == 0) {
                            mScanService.softTrigger(1);
                        }
                    } else {
                        if (down) {
                            if (repeatCount == 0) {
                                LogV("KEYCODE_SCAN pressed");
                                mScanService.softTrigger(1);
                            }
                        } else {
                            if (triggerMode == 8) {
                                mScanService.softTrigger(0);
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            if (down) {
                if (repeatCount == 0) {
                    Intent wedgeIntent = new Intent("ACTION_SCAN_START_DECODE");
                    mContext.sendBroadcast(wedgeIntent);
                }
            } else {
                Intent wedgeIntent = new Intent("ACTION_SCAN_STOP_DECODE");
                mContext.sendBroadcast(wedgeIntent);
            }
        }
        
        return -1;
    }

    //urovo zhoubo add change the shift status when press the shitf button
    private void onShiftClick(){
        int state = Settings.Global.getInt(mContext.getContentResolver(),"ufans.keyboard.state", MODE_KB_NUM);
        mFnStatus = !mFnStatus;
        Log.d(TAG,"state="+state);
        if(state==MODE_KB_NUM){
            mNumberInputType = MODE_KB_SYMBOL;
        } else if (state==MODE_KB_LOWER){
            mNumberInputType = MODE_KB_UPPER;
        } else if (state==MODE_KB_UPPER){
            mNumberInputType = MODE_KB_LOWER;
        } else if (state==MODE_KB_SYMBOL){
            mNumberInputType = MODE_KB_NUM;
        }
        Settings.Global.putInt(mContext.getContentResolver(),"ufans.keyboard.state", mNumberInputType);
        Log.d(TAG,"onShiftClick =====" + mNumberInputType);
    }

    BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                mScreenOn = true;
            } else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                mScreenOn = false;
            }
        }
    };

    //---------------add for SQ47C begin 2020.12.30-------------------
    private void changeMode(int keyname){//1:OrangeButton  2:BlueButton 3:ShiftButton
        if(keyname == 1){
            if(mBlue)
                mBlue = false;
            mOrange = !mOrange;
        } else if (keyname == 2){
            if(mOrange)
                mOrange = false;
            mBlue = !mBlue;
        } else if (keyname == 3){
            mFnStatus = !mFnStatus;
        } else {
            Log.d(TAG,"do nothing");
        }
        InputIconChange();
    }

    private boolean inTimeArea(long timeStamp, int offset){
        long time = SystemClock.uptimeMillis();
        if(time - timeStamp < offset && timeStamp - time > -offset){
            return true;
        }
        return false;
    }

    private void initState(int intChar){
        mLastNumberKeyCode = intChar;
        mCurrentNumberKeyIndex = 0;
        mLastNumberKeyEventTimeStamp = SystemClock.uptimeMillis();
    }
    //---------------add for SQ47C end 2020.12.30-------------------
}
