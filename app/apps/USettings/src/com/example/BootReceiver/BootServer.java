package com.example.BootReceiver;
import android.app.Notification;
import android.app.Service;
import android.app.IActivityManager;
import android.app.ActivityManagerNative;
import android.app.StatusBarManager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.BroadcastReceiver;
import android.database.Cursor;
import android.database.ContentObserver;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import android.os.Binder;
import android.os.Environment;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.os.FileUtils;
import android.os.IDeviceManagerService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.BatteryManager;
import android.os.LocaleList;
import android.os.UserHandle;

import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings;
import android.text.TextUtils;

import com.example.saxparsexml.Packagelist;
import com.example.saxparsexml.PropertySax;
import com.example.saxparsexml.Propertylist;
import com.example.saxparsexml.Tablelist;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.media.AudioManager;

import android.device.UFSManager;
import android.device.DeviceManager;

import com.ubx.usdk.profile.aidl.IProfileManager;
import com.ubx.usdk.profile.aidl.IApplicationPolicy;
import com.ubx.usdk.profile.aidl.IDeviceControlPolicy;

import static android.content.Intent.ACTION_SCREEN_OFF;
import static android.content.Intent.ACTION_SCREEN_ON;

public class BootServer  extends Service{

    private static final String TAG = "UrovoSettings";
    private AudioManager mAudioManager;
    private HandlerThread mHandlerThread;
    private Handler mWorkHander;
    private IProfileManager mIProfileManager;
    private final IBinder mBinder = new LocalBinder();
    private IBinder mIProfileBinder;
    private Context mContext;
    private WifiManager mWifiManager;
    private SettingsObserver mSettingsObserver;
    private TimerTask mDockControlTask = null;
    private PackageManager mPackageManager;
    private UFSManager mUFSManager;
    private DeviceManager mDeviceManager;
    private IApplicationPolicy mIApplicationPolicy;
    private IDeviceControlPolicy mIDeviceControlPolicy;

    private static final int MESSAGE_UFS_INIT = 1;
    private static final int MESSAGE_CUSTOM_INIT = 2;
    private static final int MESSAGE_SET_UFS = 10;

    private static final String ACTION_CONTROL_TORCH = "com.udroid.control.torch";
    private static final String ACTION_MANUAL_POP_SOFTINPUT = "android.intent.action.ACTION_MANUAL_POP_SOFTINPUT";

    private boolean isFirstBoot(){
        SharedPreferences preferences = mContext.getSharedPreferences("FirstDevice", Context.MODE_PRIVATE);
        boolean isfirstboot = preferences.getBoolean("firststart", true);
        if (isfirstboot) {
            SharedPreferences.Editor  editor = preferences.edit();
            editor.putBoolean("firststart", false);
            editor.commit();
            Log.d(TAG,"firststart");
        }
        return isfirstboot;
    }

    private void checkServiceAlive() {
        if(mIProfileBinder == null || !mIProfileBinder.isBinderAlive()) {
            mIProfileBinder = ServiceManager.getService("USDKProfileManager");
            if(mIProfileBinder != null) {
                mIProfileManager = IProfileManager.Stub.asInterface(mIProfileBinder);
            } else {
                Log.e(TAG, "DeviceManager USDKProfileService is null");
                return;
            }
            if (mIProfileManager == null) {
                Log.e(TAG, "DeviceManager IProfileManager is null");
                return;
            }

            try {
                IBinder binderApp = mIProfileManager.getApplicationIBinder();
                if (binderApp != null) {
                    mIApplicationPolicy = IApplicationPolicy.Stub.asInterface(binderApp);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "IApplicationPolicy::init", e);
            }

            try {
                IBinder binderDevice = mIProfileManager.getDeviceControlIBinder();
                if (binderDevice != null) {
                    mIDeviceControlPolicy = IDeviceControlPolicy.Stub.asInterface(binderDevice);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "IDeviceControlPolicy::init", e);
            }

            // try {
            //     IBinder binderNet = mIProfileManager.getNetworkPolicyIBinder();
            //     if (binderNet != null) {
            //         mINetworkPolicy = INetworkPolicy.Stub.asInterface(binderNet);
            //     }
            // } catch (RemoteException e) {
            //     Log.e(TAG, "INetworkPolicy::init", e);
            // }

            // try {
            //     IBinder binderRes = mIProfileManager.getRestrictionIBinder();
            //     if (binderRes != null) {
            //         mRestrictionPolicy = IRestrictionPolicy.Stub.asInterface(binderRes);
            //     }
            // } catch (RemoteException e) {
            //     Log.e(TAG, "RestrictionPolicy::init", e);
            // }

            // try {
            //     IBinder binderSec = mIProfileManager.getSecurityPolicyIBinder();
            //     if (binderSec != null) {
            //         mSecurityPolicy = ISecurityPolicy.Stub.asInterface(binderSec);
            //     }
            // } catch (RemoteException e) {
            //     Log.e(TAG, "ISecurityPolicy::init", e);
            // }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.e("urovo","onStartCommand");
        mContext=getApplicationContext();
        mAudioManager = mContext.getSystemService(AudioManager.class);
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mPackageManager = mContext.getPackageManager();
        mUFSManager = new UFSManager(mContext);
        mDeviceManager = new DeviceManager();
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();

        mWorkHander = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MESSAGE_UFS_INIT:
                        ufsInit();
                        break;
                    case MESSAGE_CUSTOM_INIT:
                        customInit();
                        break;
                    case MESSAGE_SET_UFS:
                        setUfs();
                        SystemProperties.set("persist.sys.ufs.state", "1");
                        break;
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SCREEN_ON);
        filter.addAction(ACTION_SCREEN_OFF);
        filter.addAction(ACTION_CONTROL_TORCH);
        filter.addAction(ACTION_MANUAL_POP_SOFTINPUT);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver, filter);

        mWorkHander.sendEmptyMessage(MESSAGE_UFS_INIT);
        mWorkHander.sendEmptyMessage(MESSAGE_CUSTOM_INIT);

        mSettingsObserver = new SettingsObserver(mWorkHander);
        mSettingsObserver.observe();
        try {
            boolean scanbtn = android.device.provider.Settings.System.getInt(mContext.getContentResolver(),
                    android.device.provider.Settings.System.SUSPENSION_BUTTON, 0) == 1;
            Intent scanIntent = new Intent();
            scanIntent.setComponent(new ComponentName("com.android.usettings", "com.android.usettings.FxService"));
            if (scanbtn) {
                mContext.startService(scanIntent);
            } else {
                mContext.stopService(scanIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(isFirstBoot()){
            setdefault();
            WritetoXml();
            Log.d(TAG, "SetUfs = ===========");
        }

        Log.d(TAG, "Setdefault = ===========");

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public class LocalBinder extends Binder {
        public BootServer getBootServer() {
            return BootServer.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        //close();
        return super.onUnbind(intent);
    }
    private void getUfsStatus(){
        String ufsState = SystemProperties.get("persist.sys.ufs.state", "0");
        Log.d(TAG, "ufsState = " + ufsState);
        if (ufsState.equals("0")) {
            SetSettingProp();
            mWorkHander.removeMessages(MESSAGE_SET_UFS);
            Message m = Message.obtain(mWorkHander, MESSAGE_SET_UFS);
            mWorkHander.sendMessage(m);
        }
    }

    private void setUfs() {
        FileUtils.deleteContents(new File("/mnt/vendor/persist/media/bootanimation.zip"));
        // try {
            int ret = mUFSManager.setSplash("/customize/media/splash.img","/dev/block/mmcblk0p42");
            Log.d(TAG,"setSplash--return:"+ret);
            mUFSManager.setBootanimationState(2);
            mUFSManager.setWallpaper(null, 1);//WallpaperManager.FLAG_SYSTEM  1
            mUFSManager.setWallpaper(null, 2);//WallpaperManager.FLAG_LOCK    2
        // }catch(RemoteException e){
        //     e.printStackTrace();
        // };
        setinstallCustomApplist("/data/system/");
        setinstallCustomApplist("/customize/app/");
        installCustomizeApp("/customize/app/");
        installCustomizeApp("/data/system/");
        // setKeyRemap();
        Log.d(TAG, "setUfs = ==========" );
    }

    private void ufsInit() {
        String ufsState = SystemProperties.get("persist.sys.ufs.state", "true");
        if (ufsState.equals("1")) {
            SetSettingProp();
            android.os.SystemProperties.set("persist.sys.ufs.state", "false");
        }
        if (!ufsState.equals("true")) {
            setinstallCustomApplist("/data/system");
            installCustomizeApp("/data/system");
        }
    }

    private void customInit() {

        Settings.System.putString(mContext.getContentResolver(), "lockTaskWhitePackages", "com.urovo.uhome,com.urovo.remotecontrol");

        int wifistate = mWifiManager.getWifiState();
        if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
            connectDefaultWifi();
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "mReceiver action  = " + action);
            try {
                // if (action.equals("action_change_status_bar")) {
                //     boolean mBarStatus = false;
                //     mBarStatus = intent.getBooleanExtra("shieldPulldown", false);
                //     int what = 0;
                //     if (mBarStatus) {
                //         what &= ~StatusBarManager.DISABLE_EXPAND;
                //     } else {
                //         what |= StatusBarManager.DISABLE_EXPAND;
                //     }
                //     getService().disableStatusBar(what);
                // } else if (action.equals(ACTION_CONTROL_TORCH)) {
                if (action.equals(ACTION_CONTROL_TORCH)) {
                    final boolean status = intent.getBooleanExtra("torch_enable", false);
                    final int brightness = intent.getIntExtra("brightness", 200);
                    try {
                        FileUtils.stringToFile("/sys/class/leds/led:torch_0/brightness", Integer.toString(status ? brightness : 0));
                        FileUtils.stringToFile("/sys/class/leds/led:switch/brightness", Integer.toString(status ? 1 : 0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (action.equals(ACTION_SCREEN_ON) || action.equals(ACTION_SCREEN_OFF)) {
                    checkServiceAlive();
                    Bundle b = mIDeviceControlPolicy.getBatteryInfo();
                    int plugType = b.getInt("plugged");
                    if ((plugType & BatteryManager.BATTERY_PLUGGED_ANY) == 0) {
                        Timer timer = new Timer();
                        if (action.equals(ACTION_SCREEN_ON)) {
                            if (mDockControlTask != null) {
                                mDockControlTask.cancel();
                                mDockControlTask = null;
                            }
                            if (timer != null) {
                                timer.cancel();
                                timer = null;
                            }
                            boolean dock_enable = SystemProperties.getBoolean("persist.sys.dock_enable", false);
                            if (dock_enable && !isDockEnable()) {
                                setDockEnabled(true);
                            }

                        } else if (action.equals(ACTION_SCREEN_OFF)) {
                            SystemProperties.set("persist.sys.dock_enable", String.valueOf(isDockEnable()));
                            mDockControlTask = new TimerTask() {
                                @Override
                                public void run() {
                                    setDockEnabled(false);
                                }

                            };
                            timer.schedule(mDockControlTask, 30 * 1000);
                        }
                    }
                } else if (ACTION_MANUAL_POP_SOFTINPUT.equals(intent.getAction())) {
                    Settings.System.putInt(mContext.getContentResolver(), "keycode_input_flag", 0);
                } else if ((action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION))) {
                    int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                    if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                        connectDefaultWifi();
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            // Observe all users' changes
            ContentResolver resolver = mContext.getContentResolver();

            //urovo add by xjf for PPT KEY action 20190513 
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(android.device.provider.Settings.System.SUSPENSION_BUTTON), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            try {
                if (uri.toString().endsWith(android.device.provider.Settings.System.SUSPENSION_BUTTON)) {
                    boolean scanbtn = android.device.provider.Settings.System.getInt(mContext.getContentResolver(),
                            android.device.provider.Settings.System.SUSPENSION_BUTTON, 0) == 1;

                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.usettings", "com.android.usettings.FxService"));
                    if (scanbtn) {
                        mContext.startService(intent);
                    } else {
                        mContext.stopService(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setdefault(){
        Settings.System.putString(mContext.getContentResolver(), "volume_music_speaker", "15");
        Settings.System.putString(mContext.getContentResolver(), "volume_alarm_speaker", "15");
        Settings.System.putString(mContext.getContentResolver(), "volume_ring_speaker", "15");
        Settings.System.putString(mContext.getContentResolver(), "font_scale", "1.0");
        Settings.System.putString(mContext.getContentResolver(), "time_12_24", "12");
        Settings.System.putString(mContext.getContentResolver(), "device_nfc", "1");
        Settings.Secure.putString(mContext.getContentResolver(), "accessibility_display_inversion_enabled", "0");
        Settings.Global.putString(mContext.getContentResolver(), "auto_pop_softinput", "1");
        Settings.System.putString(mContext.getContentResolver(), "Glove_mode", "0");
        Settings.Global.putString(mContext.getContentResolver(), "charging_sounds_enabled","1");
        Settings.System.putString(mContext.getContentResolver(), "volume_music_speaker","12");
        Settings.System.putString(mContext.getContentResolver(), "multi_finger_screen_shot_enabled","0");
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 12,AudioManager.FLAG_ACTIVE_MEDIA_ONLY);
    }

    private void SetSettingProp() {
        String mPackage, mname, mkey, mvalue;
        try {
            ArrayList<Packagelist> aPackagelist = parseXMLFile();
            if (aPackagelist == null)
                return;
            Iterator<Packagelist> iPackagelist = aPackagelist.iterator();
            while (iPackagelist.hasNext()) {
                Packagelist mPackagelist = iPackagelist.next();
                mPackage = mPackagelist.getpackageName();
                ArrayList<Tablelist> aTablelist = mPackagelist.getTablelists();
                Iterator<Tablelist> iTablelist = aTablelist.iterator();
                while (iTablelist.hasNext()) {
                    Tablelist mTablelist = iTablelist.next();
                    mname = mTablelist.getname();
                    ArrayList<Propertylist> aPropertylist = mTablelist.getpropertys();
                    Iterator<Propertylist> iPropertylist = aPropertylist.iterator();
                    Propertylist mPropertylist = null;
                    while (iPropertylist.hasNext()) {
                        mPropertylist = iPropertylist.next();
                        mkey = mPropertylist.getkey();
                        mvalue = mPropertylist.getvalue();
                        Log.i(TAG, "mPackage :" + mPackage + "  mname:" + mname + "   mkey:" + mkey + "   mvalue:" + mvalue);
                        if (!TextUtils.isEmpty(mPackage) && !TextUtils.isEmpty(mname) && !TextUtils.isEmpty(mkey) && !TextUtils.isEmpty(mvalue))
                            if (mPackage.equals("com.android.providers.settings")) {
                                if (mname.equals("Secure")) {
                                    //String Secureprop = Settings.Secure.getString(mContext.getContentResolver(), mkey);
                                    // if (!TextUtils.isEmpty(Secureprop))
                                    Settings.Secure.putString(mContext.getContentResolver(), mkey, mvalue);//Integer.parseInt(mvalue));
                                    //else
                                    //    Log.i(TAG, "SaxParseXml, No label Secure exists:" + mkey);
                                } else if (mname.equals("System")) {
                                    //String  Systemprop=Settings.System.getString(mContext.getContentResolver(), mkey);
                                    //if(!TextUtils.isEmpty(Systemprop))
                                    Settings.System.putString(mContext.getContentResolver(), mkey, mvalue);
                                    //else
                                    //  Log.i(TAG,"SaxParseXml, No label System exists:"+mkey);
                                } else if (mname.equals("Global")) {

                                    Settings.Global.putString(mContext.getContentResolver(), mkey, mvalue);
                                } else {
                                    Log.i(TAG, "SaxParseXml error :XML file writing is not standard");
                                }
                            } else if (mPackage.equals("SystemProperties")) {
                                String propvalue = SystemProperties.get(mkey, "");
                                if (!TextUtils.isEmpty(propvalue))
                                    SystemProperties.set(mkey, mvalue);
                                else
                                    Log.i(TAG, "SaxParseXml,No label  exists:" + mkey);

                                if (mkey.equals("persist.sys.locale"))
                                    setlanguage(mvalue);
                            } else if (mPackage.equals("com.ubx.provider.settings")) {
                                String Settingsprop = android.device.provider.Settings.System.getString(mContext.getContentResolver(), mkey);
                                // if (!TextUtils.isEmpty(Settingsprop))
                                android.device.provider.Settings.System.putString(mContext.getContentResolver(), mkey, mvalue);
                                // else
                                //    Log.i(TAG, "SaxParseXml,No label  exists:" + mkey);
                            } else {
                                Log.i(TAG, "SaxParseXml  error: XML file writing is not standard");
                            }
                    }
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setlanguage(String mlanguage) {
        try {
            String mfont_scale = Settings.System.getString(mContext.getContentResolver(), "font_scale").trim();
            Log.i(TAG, "setlanguage font_scale:" + mfont_scale);
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            float mfscale = Float.parseFloat(mfont_scale);
            Log.i(TAG, "setlanguage mfscale:" + mfont_scale);
            config.fontScale = mfscale;
            Log.i(TAG, "setlanguage mlanguage:" + mlanguage);
            //String mlanguage=android.os.SystemProperties.get("persist.sys.locale","zh-CN");
            //String mcountry=android.os.SystemProperties.get("persist.sys.country","CN");
            String mcountry = mlanguage.substring(mlanguage.lastIndexOf("-") + 1);
            Log.i(TAG, "setlanguage mcountry:" + mcountry);
            //android.os.SystemProperties.set("persist.sys.country", mcountry);
            mlanguage = mlanguage.substring(0, mlanguage.indexOf("-"));
            Log.i(TAG, "setlanguage mlanguage:" + mlanguage);
            //android.os.SystemProperties.set("persist.sys.language", mlanguage);
            Locale mlocale = new Locale(mlanguage, mcountry);
            LocaleList locales = new LocaleList(mlocale);
            config.userSetLocale = true;
            config.setLocales(locales);
            am.updatePersistentConfiguration(config);
            //BackupManager.dataChanged("com.android.providers.settings");
        } catch (RemoteException e) {
            Log.i(TAG, "RemoteException e:" + e);
        }
    }

    public void setinstallCustomApplist(String patch) {
        android.util.Log.d(TAG, "patch.startsWith:" + patch.startsWith("/customize/"));
        File file = new File(patch);
        if (!file.exists())
            return;

        File[] subFile = file.listFiles();
        if(subFile == null) {
            return;
        }
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                String apkFilePath = patch + "/" + filename;
                if (!TextUtils.isEmpty(apkFilePath) && apkFilePath.endsWith(".apk")) {
                    PackageInfo info = mPackageManager.getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES);
                    android.util.Log.d(TAG, "packageName:" + info.packageName);
                    // try {
                        mDeviceManager.setAllowInstallApps(info.packageName, 5, 1);
                    // } catch (RemoteException e) {
                    //     e.printStackTrace();
                    // }
                }
            }
        }
    }

    public void installCustomizeApp(String patch) {
        File file = new File(patch);
        if (!file.exists())
            return;
        File[] subFile = file.listFiles();
        if(subFile == null) {
            return;
        }
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                //int ret=setCustomizeApp(patch,filename);
                PackageInfo info = mPackageManager.getPackageArchiveInfo(patch + "/" + filename, PackageManager.GET_ACTIVITIES);
                if (filename.endsWith(".apk")) {
                    checkServiceAlive();
                    try {
                        mIApplicationPolicy.installPackage(patch + "/" + filename, 0, null);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // public void setKeyRemap() {
    //     try{
    //         String importPath = "/sdcard/.keys_config.txt";
    //         String s = getService().getSettingProperty("File-/customize/etc/keys_config.txt");
    //         FileUtils.stringToFile(importPath, s);
    //         Intent eintent = new Intent("action.PROGRAMMABLE_KEY_SERVICE");
    //         ComponentName component = new ComponentName("com.urovo.keyremap", "com.urovo.keyremap.component.ImportExportService");
    //         eintent.setComponent(component);
    //         eintent.putExtra("programmable", 1);
    //         eintent.putExtra("filepath", importPath);
    //         startService(eintent);
    //     } catch(Exception e) {
    //         e.printStackTrace();
    //     }     
    // }

    public void setDockEnabled(boolean enabled) {
        try {
            FileUtils.stringToFile("/sys/devices/soc/78db000.usb/otg_enable", Integer.toString(enabled ? 1 : 0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isDockEnable() {
        boolean result = false;
        try {
            String str = FileUtils.readTextFile(new File("/sys/devices/soc/78db000.usb/otg_enable"), 0, "");
            result = Boolean.parseBoolean(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private void connectDefaultWifi() {
        String mwificonfig = Settings.System.getString(mContext.getContentResolver(), "wifi_ssid_password");
        if (TextUtils.isEmpty(mwificonfig))
            return;

        String mssid[] = mwificonfig.split(",");
        int len = mssid.length;
        int i = 0;
        while (i < len && (i + 1) < len) {
            Log.i(TAG, "DefaultWifi ssid:" + mssid[i]);
            WifiConfiguration tempConfig1 = isExsits(mssid[i]);
            if (tempConfig1 == null) {
                int type = 3;
                if(len > (i + 2)) {
                    try{
                        type = Integer.parseInt(mssid[i + 2]);
                    } catch(Exception e){
                        type = 3;
                    }
                }
                WifiConfiguration wifiConfig = createWifiInfo(mssid[i], mssid[i + 1], type);
                if (wifiConfig != null) {
                    Log.i(TAG, "DefaultWifi Config:" + wifiConfig);
                    int netID = mWifiManager.addNetwork(wifiConfig);
                    mWifiManager.enableNetwork(netID, true);
                }
            }
            i = i + 3;
        }
    }

    public void WritetoXml( ) {
        int i=0;
        // Intent intent = new Intent("com.udroid.action.bootufs");
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                // 获取SD卡的目录
                File sdCardDir = Environment.getExternalStorageDirectory();
                String path = "/Custom_default/";
                File dir = new File(sdCardDir+path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // FileWriter fwtmp = new  FileWriter( sdCardDir.getCanonicalPath() +path+"default_Settings_property.xml",  false );
                //  fwtmp.write("NULL"+"\r\n");
                //  fwtmp.close();



                Log.i("urovo","WritetoXml");
                FileWriter fw = new  FileWriter( sdCardDir.getCanonicalPath() +path+"default_Settings_property.xml",  false );
                fw.write("<propertygroup>"+"\r\n");
                fw.write("  <package>"+"\r\n");
                fw.write("          <packageName>com.android.providers.settings</packageName>"+"\r\n");
                Uri systemuri = android.provider.Settings.System.CONTENT_URI;
                Cursor systemcursor = mContext.getContentResolver().query(systemuri, null, null, null, null);
                while (systemcursor.moveToNext()) {
                    String  key=  systemcursor.getString(systemcursor.getColumnIndex("name"));
                    String  value=systemcursor.getString(systemcursor.getColumnIndex("value"));i++;
                    fw.write("          <table >"+"\r\n");
                    fw.write("              <name>System</name>"+"\r\n");
                    fw.write("              <property>"+"\r\n");
                    fw.write("                  <key>"+key+"</key>"+"\r\n");
                    fw.write("                  <value>"+value+"</value>"+"\r\n");
                    fw.write("              </property>"+"\r\n");
                    fw.write("          </table >"+"\r\n");
                    Log.i(TAG,"old System key:"+key+"              value: "+value);
                }
                systemcursor.close();

                Uri Globauri = android.provider.Settings.Global.CONTENT_URI;
                Cursor globacursor = mContext.getContentResolver().query(Globauri, null, null, null, null);
                while (globacursor.moveToNext()) {
                    String  key=  globacursor.getString(globacursor.getColumnIndex("name"));
                    String  value=globacursor.getString(globacursor.getColumnIndex("value"));  i++;
                    fw.write("          <table >"+"\r\n");
                    fw.write("              <name>Global</name>"+"\r\n");
                    fw.write("              <property>"+"\r\n");
                    fw.write("                  <key>"+key+"</key>"+"\r\n");
                    fw.write("                  <value>"+value+"</value>"+"\r\n");
                    fw.write("              </property>"+"\r\n");
                    fw.write("          </table >"+"\r\n");
                    Log.i(TAG,"old Globauri key:"+key+"              value: "+value);
                }
                globacursor.close();

                Uri Secureuri = android.provider.Settings.Secure.CONTENT_URI;
                Cursor Securecursor = mContext.getContentResolver().query(Secureuri, null, null, null, null);
                while (Securecursor.moveToNext()) {
                    String  key=  Securecursor.getString(Securecursor.getColumnIndex("name"));
                    String  value=Securecursor.getString(Securecursor.getColumnIndex("value"));i++;
                    fw.write("          <table >"+"\r\n");
                    fw.write("              <name>Secure</name>"+"\r\n");
                    fw.write("              <property>"+"\r\n");
                    fw.write("                  <key>"+key+"</key>"+"\r\n");
                    fw.write("                  <value>"+value+"</value>"+"\r\n");
                    fw.write("              </property>"+"\r\n");
                    fw.write("          </table >"+"\r\n");
                    Log.i(TAG,"old Secureuri key:"+key+"              value: "+value);
                }
                Securecursor.close();
                fw.write("  </package>"+"\r\n");
                fw.write("  <package>"+"\r\n");
                fw.write("          <packageName>com.ubx.provider.settings</packageName>"+"\r\n");
                Uri Scanuri= Uri.parse("content://com.urovo.provider.settings/settings");
                Cursor Scancursor = mContext.getContentResolver().query(Scanuri, null, null, null, null);
                while (Scancursor.moveToNext()) {
                    String  key=  Scancursor.getString(Scancursor.getColumnIndex("name"));
                    String  value=   Scancursor.getString(Scancursor.getColumnIndex("value"));  i++;
                    fw.write("          <table >"+"\r\n");
                    fw.write("              <name>settings</name>"+"\r\n");
                    fw.write("              <property>"+"\r\n");
                    fw.write("                  <key>"+key+"</key>"+"\r\n");
                    fw.write("                  <value>"+value+"</value>"+"\r\n");
                    fw.write("              </property>"+"\r\n");
                    fw.write("          </table >"+"\r\n");
                    Log.i(TAG,"old Scanuri key:"+key+"              value: "+value);
                }
                Scancursor.close();
                fw.write("  </package>"+"\r\n");
                fw.write("</propertygroup>"+"\r\n");
                fw.close();
            }
            // mContext.sendBroadcast(intent);
            getUfsStatus();
        } catch (Exception e) {
            // mContext.sendBroadcast(intent);
            getUfsStatus();
            e.printStackTrace();
        }
    }

    ArrayList<Packagelist> parseXMLFile() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        InputStream inputStream = new FileInputStream(new File("/customize/etc/default_Settings_property.xml"));
        PropertySax handle = new PropertySax();
        saxParser.parse(inputStream, handle);
        inputStream.close();
        return handle.getLessons();
    }

    private WifiConfiguration isExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if (existingConfigs == null) return null;
        for (WifiConfiguration existingConfig : existingConfigs) {
            if (existingConfig != null && existingConfig.SSID.equals("\"" + SSID + "\"")) {
                return existingConfig;
            }
        }
        return null;
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (Type == 1) {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 2) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (Type == 3) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            // 此处需要修改否则不能自动重联
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }
}
