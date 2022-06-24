/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.device;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.Log;
import android.os.Build;
import android.os.IBinder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.os.LocaleList;

import com.ubx.usdk.profile.aidl.IDeviceControlPolicy;
import com.ubx.usdk.profile.aidl.IProfileManager;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import com.example.saxparsexml.*;


import android.text.TextUtils;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings;
import android.app.IActivityManager;
import android.app.ActivityManagerNative;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;


public class UFSManager {
    private static final String TAG = "UFSManager";
    private Context mContext;
    private IBinder mIProfileBinder;
    private IProfileManager mIProfileManager;
    private IDeviceControlPolicy mIDeviceControlPolicy;
    private WifiManager mWifiManager;

    boolean isMTK = android.os.Build.PROJECT.equals("SQ53H");

    public UFSManager(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        checkServiceAlive();
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
                IBinder binderDevice = mIProfileManager.getDeviceControlIBinder();
                if (binderDevice != null) {
                    mIDeviceControlPolicy = IDeviceControlPolicy.Stub.asInterface(binderDevice);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "IDeviceControlPolicy::init", e);
            }
        }
    }

    public void setUFS(){
        Log.i(TAG,"setufs---------------------------");
        
        //set SQL prop
        SetSettingProp();

        //set splash
        if(isMTK){
            setSplash("/customize/media/splash.bin",getSplashPath());
        } else {
            setSplash("/customize/media/splash.img",getSplashPath());
        }

        //set Wallpaper
        setWallpaper(null, 1);//WallpaperManager.FLAG_SYSTEM  1
        setWallpaper(null, 2);//WallpaperManager.FLAG_LOCK  2


        //set bootanimation
        FileUtils.deleteContents(new File("/mnt/vendor/persist/media/bootanimation.zip"));
        setBootanimation("/customize/media/bootanimation.zip");
        setBootanimationState(2);

        //copy libs
        // int ret = copyLib();
        // Log.d("zzzzz","ret ====" + ret);

        //set scansettings
        setScanSettings();

        //connect defaultWifi
        connectDefaultWifi();
    }

    /**
     * UFS 接口
     * 设置默认配置文件
     * default_Settings_property.xml
     *
     * @param Path 默认配置文件
     * @return
     */
    // public int setSettingsXml(final String path) {
    //     final File sdfile = new File(path);
    //     if (!sdfile.exists()) {
    //         return -1;
    //     }
    //     if (mIDeviceControlPolicy != null) {
    //         try {
    //             mIDeviceControlPolicy.copyFile(path,"/customize/etc/default_Settings_property.xml");
    //             return 0;
    //         } catch (final RemoteException e) {
    //             e.printStackTrace();
    //         }
    //     }
    //     return -1;
    // }

    /**
     * UFS 接口
     * 设置开机logo
     *
     * @param Path 开机logo图片路径
     * @return
     */
    public int setSplash(final String Path, String Path1) {
        final File sdfile = new File(Path);
        if (!sdfile.exists()) {
            return -2;
        }
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.copyFile(Path,Path1);
                return 0;
            } catch (final RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    // 获取splash分区的具体位置
    private String getSplashPath() {
        String splashpath;
        if("SQ53H".equals(android.os.Build.PROJECT)){
            splashpath = "/dev/block/mmcblk0p35";
        } else if ("SQ47".equals(android.os.Build.PROJECT)){
            splashpath = "/dev/block/mmcblk0p39";
        } else if ("SQ53A".equals(android.os.Build.PROJECT)){
            splashpath = "/dev/block/sde50";
        } else if ("SQ83".equals(android.os.Build.PROJECT)){
            splashpath = "/dev/block/mmcblk0p40";
        } else {
            splashpath = "/dev/block/mmcblk0p42";
        }
        return splashpath;
    }

    /**
     * UFS 接口
     * 设置custom应用
     *
     * @param Path     应用路径
     * @param filename 应用名称
     * @return
     */
    public int setCustomizeApp(final String path, final String filename) {
        final File sdfile = new File(path + "/" + filename);
        if (!sdfile.exists()) {
            return -1;
        }
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.copyFile(path,"/data/system/" + "filename");
                return 0;
            } catch (final RemoteException e) {
                e.printStackTrace();
            }
        }
        return  -1;
    }

    /**
     * UFS 接口
     * 设置开机动画
     *
     * @param Path 开机动画路径
     * @return
     */
    public int setBootanimation(final String path) {
        final File sdfile = new File(path);
        Log.e(TAG, "setBootanimation sdfile = " + sdfile);
        if (!sdfile.exists()) {
            return -2;//path not exit
        }

        final File animationfile = new File("/mnt/vendor/persist/media/bootanimation.zip");
        if(animationfile.exists()) {
            animationfile.delete();
        }
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.copyFile(path,"/mnt/vendor/persist/media/bootanimation.zip");
                return 0;
            } catch (final RemoteException e) {
                e.printStackTrace();
            }
        }
        return  -1;
    }

    /**
     * UFS 接口
     * 设置有效开机动画
     *
     * @param state 0为默认开机动画　//1为接口　//２为UFS
     * @return
     */
    public int setBootanimationState(final int state) {//0为默认开机动画　//1为接口　//２为UFS
        if (mIDeviceControlPolicy != null) {
            try {
                final List<String> list =  Arrays.asList(Integer.toString(state));
                Log.d(TAG,"list =========" + list);
                mIDeviceControlPolicy.writeListStringToFile(list,"/mnt/vendor/persist/media/bootanimationstate.txt");
                return 0;
            } catch (final RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * UFS 接口
     * 拷贝库资源
     *
     * @return
     */
    // public int copyLib() {
    //     if (mIDeviceControlPolicy != null) {
    //         try {
    //             mIDeviceControlPolicy.copyFile("customize/lib64/*","/system/lib64/");
    //             mIDeviceControlPolicy.copyFile("customize/lib/*","/system/lib/");
    //             return 0;
    //         } catch (final RemoteException e) {
    //             e.printStackTrace();
    //         }
    //     }
    //     return  -1;
    // }

    /** 设置壁纸
     * @param bitmap if(bitmap == null), clearWallpaper
     * @param which  WallpaperManager.FLAG_SYSTEM  1
     *               WallpaperManager.FLAG_LOCK    2
     */
    public void setWallpaper(final Bitmap bitmap, final int which) {
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.setWallpaper(bitmap, which);
            } catch (final RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * UFS 接口
     * 设置默认配置文件
     *
     * @return
     */
    //----------------------------------设置默认配置文件-------------begin--------------------
    public void SetSettingProp() {
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
                                    Settings.Secure.putString(mContext.getContentResolver(), mkey, mvalue);//Integer.parseInt(mvalue));
                                } else if (mname.equals("System")) {
                                    Settings.System.putString(mContext.getContentResolver(), mkey, mvalue);
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
                                android.device.provider.Settings.System.putString(mContext.getContentResolver(), mkey, mvalue);
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

    ArrayList<Packagelist> parseXMLFile() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        InputStream inputStream = new FileInputStream(new File("/customize/etc/default_Settings_property.xml"));
        PropertySax handle = new PropertySax();
        saxParser.parse(inputStream, handle);
        inputStream.close();
        return handle.getLessons();
    }

    public void setlanguage(String mlanguage) {
        try {
            String mfont_scale = Settings.System.getString(mContext.getContentResolver(), "font_scale").trim();
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            float mfscale = Float.parseFloat(mfont_scale);
            config.fontScale = mfscale;
            String mcountry = mlanguage.substring(mlanguage.lastIndexOf("-") + 1);
            mlanguage = mlanguage.substring(0, mlanguage.indexOf("-"));
            Locale mlocale = new Locale(mlanguage, mcountry);
            LocaleList locales = new LocaleList(mlocale);
            config.userSetLocale = true;
            config.setLocales(locales);
            am.updatePersistentConfiguration(config);
        } catch (RemoteException e) {
            Log.i(TAG, "RemoteException e:" + e);
        }
    }

    public void connectDefaultWifi() {
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
    //----------------------------------设置默认配置文件-------------end--------------------

    // public void setKeyRemap() {
    //     try{
    //         String importPath = "/sdcard/.keys_config.txt";
    //         String s = getService().getSettingProperty("File-/customize/etc/keys_config.txt");
    //         FileUtils.stringToFile(importPath, s);
    //         Intent eintent = new Intent("action.PROGRAMMABLE_KEY_SERVICE");
    //         ComponentName component = new ComponentName("com.ubx.keyremap", "com.ubx.keyremap.component.ImportExportService");
    //         eintent.setComponent(component);
    //         eintent.putExtra("programmable", 1);
    //         eintent.putExtra("filepath", importPath);
    //         startService(eintent);
    //     } catch(Exception e) {
    //         e.printStackTrace();
    //     }     
    // }

    /**
     * UFS 接口
     * 设置扫描头配置文件
     *
     * @return　1:存在配置文件　0:不存在配置文件
     */
    public int setScanSettings() {
        if(new File("customize/etc/scanner_default_property.xml").exists()){
            Intent intent = new Intent("action.IMPORT_SCANNER_CONFIG");
            intent.putExtra("configFilepath", "customize/etc/scanner_default_property.xml");
            intent.setPackage("com.ubx.datawedge");
            mContext.sendBroadcast(intent);
            Log.d(TAG,"true ===== scan.setting.reset");
            return 1;
        }
        return 0;
    }
}
