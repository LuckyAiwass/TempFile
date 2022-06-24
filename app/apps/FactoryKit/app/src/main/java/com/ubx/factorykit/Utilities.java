/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.provider.Settings;
//import android.telephony.SubInfoRecord;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.Framework.MainApp;
import com.ubx.factorykit.Framework.PhoneProcessAgent;

import static com.ubx.factorykit.Framework.FactoryKitPro.PROJECT;

public class Utilities {

    public static final String TAG = "FactoryKit";
    public static final String RESULT_PASS = "Pass";
    public static final String RESULT_FAIL = "Failed";

    public static final String CURRENT_FILE_NAME = "CurrentMessage.txt";
    public static final String TESTLOG_FILE_NAME = "Testlog.txt";
    private static final int VOLUME_SDCARD_INDEX = 1;

    public static void writeCurMessage(Context context, String Tag, String result) {

        String msg = "[" + Tag + "] " + result;
        FileOutputStream mFileOutputStream = null;
        try {
            mFileOutputStream = new FileOutputStream(context.getFilesDir() + "/" + CURRENT_FILE_NAME, false);
            byte[] buffer = msg.getBytes();
            mFileOutputStream.write(buffer, 0, buffer.length);
            mFileOutputStream.flush();
        } catch (Exception e) {
            loge(e);
            e.printStackTrace();
        } finally {
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                loge(e);
                e.printStackTrace();
            }
        }
        logd("Writed result=" + result);

    }

    public static void writeCurMessage(String Tag, String result) {

        String msg = "[" + Tag + "] " + result;
        logd("WritedResult: " + Tag + "=" + result);
        FileOutputStream mFileOutputStream = null;
        String filePath = "/data/data/com.ubx.factorykit/files/" + CURRENT_FILE_NAME;
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                loge(e);
                e.printStackTrace();
            }
        }

        try {
            mFileOutputStream = new FileOutputStream(file, false);
            byte[] buffer = msg.getBytes();
            mFileOutputStream.write(buffer, 0, buffer.length);
            mFileOutputStream.flush();
        } catch (Exception e) {
            loge(e);
            e.printStackTrace();
        } finally {
            try {
                if (mFileOutputStream != null)
                    mFileOutputStream.close();
            } catch (IOException e) {
                loge(e);
                e.printStackTrace();
            }
        }

    }

    public static void writeTestLog(String Tag, String result) {

        String msg = null;
        if (result != null)
            msg = "[" + Tag + "] " + result + "\n";
        else
            msg = Tag + "\n";// only write a string
        FileOutputStream mFileOutputStream = null;
        String filePath = "/data/data/com.ubx.factorykit/files/" + TESTLOG_FILE_NAME;
        File file = new File(filePath);
        file.getParentFile().mkdirs();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                loge(e);
                e.printStackTrace();
            }
        }

        try {
            mFileOutputStream = new FileOutputStream(file, true);
            byte[] buffer = msg.getBytes();
            mFileOutputStream.write(buffer, 0, buffer.length);
            mFileOutputStream.flush();
        } catch (Exception e) {
            loge(e);
            e.printStackTrace();
        } finally {
            try {
                mFileOutputStream.close();
            } catch (IOException e) {
                loge(e);
                e.printStackTrace();
            }
        }
        logd("Added TestLog=" + result);

    }

    public static void CleanCurrentMessage(String fpath, String msg) {

        File file = new File(fpath + "/" + "CurrentMessage.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
                file.createNewFile();
            }
            // ==Modified==
            FileWriter fr = new FileWriter(file, true);
            fr.write(msg);

            fr.close();
        } catch (IOException e) {
            Log.e(TAG, "log():" + fpath + ", err=" + e);
        }
    }

    public String getHwPlatform() {
        String hwPlatform = SystemProperties.get("ro.hw_platform");
        if (hwPlatform != null && hwPlatform.length() > 4)
            hwPlatform = hwPlatform.substring(hwPlatform.length() - 4);
        return hwPlatform;
    }

    public static String getStringValueSaved(Context mContext, String key, String def) {

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        return mSharedPreferences.getString(key, def);
    }

    public static void saveStringValue(Context mContext, String key, String value) {

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static boolean getBooleanPreference(Context context, String key, boolean def) {

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return mSharedPreferences.getBoolean(key, def);

    }

    public static void saveBooleanPreference(Context context, String key, boolean value) {

        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean writeFile(String filePath, String content) {
        boolean res = true;
        File file = new File(filePath);
        File dir = new File(file.getParent());
        if (!dir.exists())
            dir.mkdirs();
        try {
            FileWriter mFileWriter = new FileWriter(file, false);
            mFileWriter.write(content);
            mFileWriter.close();
        } catch (IOException e) {
            res = false;
        }
        Log.d("zml", "writeFile   " +filePath  + "   value   " +content);
        return res;
    }

    public static String readFile(String filePath) {
        String res = "";
        File file = new File(filePath);
        if (!file.exists())
            return res;

        try {
            char[] buf = new char[1024];
            int count = 0;
            FileReader fileReader = new FileReader(file);
            while ((count = fileReader.read(buf)) > 0) {
                res += new String(buf, 0, count);
            }
            fileReader.close();

        } catch (IOException e) {
            res = "";
        }
        return res;
    }

    public static boolean setSystemProperties(String key, String val) {
        logd("set " + key + " value=" + val);
        if (val == null || key == null)
            return false;
//        SystemProperties.set(key, val);
        if (false && val.equals(SystemProperties.get(key)))
            return true;
        else {
            loge("setproper failed. Check if app has system permission.");
            return false;
        }
    }

    public static String getSystemProperties(String key, String defaultValue) {
        if (key == null)
            return null;
        String property = SystemProperties.get(key, defaultValue);
        logd(key + "=" + property);
        return property;
    }

    public static void createShortcut(Context context, Class<Framework> appClass) {
        logd("");
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));
        intent.putExtra("duplicate", false);
        Intent appIntent = new Intent();
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.setClass(context, appClass);
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, appIntent);
        ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context, R.drawable.icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        context.sendBroadcast(intent);
    }

    private static void log(String fpath, String msg) {

        File file = new File(fpath);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter mFileWriter = new FileWriter(file, true);
            mFileWriter.append(msg);
            mFileWriter.close();
        } catch (IOException e) {
        }
    }

    public static String getPlatform() {

        return PROJECT;//SystemProperties.get(Values.PROP_HW_PLATFORM);
    }

    public static void parseParameter(final String in, HashMap<String, String> out) {
        String key, value, src;
        if (in == null || out == null)
            return;
        src = in;
        while (true) {
            if (src == null || src.length() == 0)
                break;
            int index1 = src.indexOf('=');
            if (index1 > 0)
                key = src.substring(0, index1).trim();// [start,end)
            else
                break;
            int index2 = src.indexOf(';');
            if (index2 > 0) {
                value = src.substring(index1 + 1, index2).trim();
                src = src.substring(index2 + 1);
                out.put(key, value);
            } else {
                value = src.substring(index1 + 1).trim();
                out.put(key, value);
                break;
            }
        }
    }

    public static void enableBluetooth(boolean enable) {
        logd("enableBluetooth=" + enable);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (enable)
                mBluetoothAdapter.enable();
            else
                mBluetoothAdapter.disable();
        }
    }

    public static void enableWifi(Context context, boolean enable) {
        logd("enableWifi=" + enable);
        WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null)
            mWifiManager.setWifiEnabled(enable);
    }

    public static void enableGps(Context context, boolean enable) {
        logd("enableGps=" + enable);
        try {
            Settings.Secure.setLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER,
                    enable);
        } catch (Exception e) {
            loge(e);
        }
    }

    public static void configScreenTimeout(Context context, int value) {

        logd(System.getProperty("screenSet"));
        if (System.getProperty("screenSet") == null) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, value);
        }
    }

    public static int getScreenTimeout(Context context) {
         int time = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 60000);
         return time;
    }

    public static void configMultiSim(Context mContext) {
        logd(System.getProperty("cardEnabled"));
        if (System.getProperty("cardEnabled") == null) {
            System.setProperty("cardEnabled", "yes");
            if ("dsds".equals(Utilities.getSystemProperties(Values.PROP_MULTISIM, null)))
                mContext.startService(new Intent(mContext, PhoneProcessAgent.class));
        }
    }


    public static int getIntPara(int index, String key, int def) {
        int ret = def;
        if (index >= 0) {
            Map<String, ?> item = (Map<String, ?>) MainApp.getInstance().mItemList.get(index);
            HashMap<String, String> paraMap = (HashMap<String, String>) item.get("parameter");

            try {
                ret = Integer.valueOf(paraMap.get(key));
            } catch (NumberFormatException e) {
            }

        }
        return ret;
    }

    public static Boolean getBoolPara(int index, String key, boolean def) {
        boolean ret = def;
        if (index >= 0) {
            Map<String, ?> item = (Map<String, ?>) MainApp.getInstance().mItemList.get(index);
            HashMap<String, String> paraMap = (HashMap<String, String>) item.get("parameter");

            try {
                ret = Boolean.valueOf(paraMap.get(key));
            } catch (NumberFormatException e) {
            }

        }
        return ret;
    }

    public static void enableCharging(boolean enable) {
        logd("enableCharging=" + enable);
        String value = enable ? "0" : "1";
        Utilities.setSystemProperties(Values.PROP_CHARGE_DISABLE, value);
    }

    public static void exec(final String para, final Handler handler) {

        new Thread() {

            public void run() {
                try {
                    logd(para);

                    Process mProcess;
                    String paras[] = para.split(",");
                    for (int i = 0; i < paras.length; i++)
                        logd(i + ":" + paras[i]);
                    mProcess = Runtime.getRuntime().exec(paras);
                    mProcess.waitFor();

                    InputStream inStream = mProcess.getInputStream();
                    InputStreamReader inReader = new InputStreamReader(inStream);
                    BufferedReader inBuffer = new BufferedReader(inReader);
                    String s;
                    String data = "";
                    while ((s = inBuffer.readLine()) != null) {
                        data += s + "\n";
                    }
                    logd(data);
                    int result = mProcess.exitValue();
                    logd("ExitValue=" + result);
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString(Values.KEY_OUTPUT, data);
                    message.setData(bundle);
                    message.setTarget(handler);
                    message.sendToTarget();

                } catch (Exception e) {
                    logd(e);
                }

            }
        }.start();

    }

    public static boolean isSimSubscriptionStatusActive(int slotId) {
        /*SubInfoRecord subInfoRecord = null;
        List<SubInfoRecord> activeSubscriptions = SubscriptionManager.getActiveSubInfoList();
        if (activeSubscriptions != null) {
            for (SubInfoRecord record : activeSubscriptions) {
                if (record != null && slotId == record.slotId) {
                    subInfoRecord = record;
                    break;
                }
            }
        }
        if (subInfoRecord != null && subInfoRecord.slotId >= 0 &&
                subInfoRecord.mStatus != SubscriptionManager.SUB_CONFIGURATION_IN_PROGRESS) {
            return true;
        }
        */
        return false;
    }

    private static void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        // Log.d(TAG, s + "");
    }

    private static void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        // Log.e(TAG, e + "");
    }

    //judge device support MultiSim
    public static boolean isMultiSimEnabled() {
        String multiSimConfig =android.os.SystemProperties.get("persist.radio.multisim.config", "");
        if(multiSimConfig == null || multiSimConfig.length() <= 0)
            return false;
        return (multiSimConfig.equals("dsds") || multiSimConfig.equals("dsda") ||
                multiSimConfig.equals("tsts"));
    }

    public static String parserQcn(String strhex) {
        String qcn;
        if (strhex != null) {
            String product;
            String number;
            String fsgRf;
            String fsgversion;
            String Rfversion;
            Log.e(TAG, "qcn version >>> " + strhex);
            try {
                product = strhex.substring(0, 2);
                number = strhex.substring(2, 4);
                fsgRf = strhex.substring(4, 6);
                fsgversion = strhex.substring(6, 8);
                Rfversion = strhex.substring(8, 10);
                switch (strhex.substring(0, 2)) {
                    case "01":
                        product = "SQ29W";
                        break;
                    case "02":
                        product = "SQ29C";
                        break;
                    case "03":
                        product = PROJECT;//Utilities.getBuildProject() ro.product.model
                        break;
                    case "04":
                        product = "SQ52TG";
                        break;
                    case "05":
                        product = "SQ38";
                        break;
                    case "06":
                        product = "SQ45";
                        break;
                    case "07":
                        product = "SQ51FW";
                        break;
                    case "08":
                        product = "SQ53C";
                        break;
                    case "09":
                        product = "SQ52TGW";
                        break;
                    case "0a":
                        product = "SQ53";
                        break;
                    case "0b":
                        product = "SQ45W";
                        break;
                    case "0d":
                        product = "SQ51S";
                        break;
                    case "15":
                        product = "SQ83";
                        break;

                    default:
                        product = PROJECT;
                        break;
                }

                switch (strhex.substring(4, 6)) {
                    case "01":
                        fsgRf = "SS";
                        break;
                    case "02":
                        fsgRf = "5DS";
                        break;
                    case "03":
                        fsgRf = "7DS";
                        break;
                    case "04":
                        fsgRf = "Oversea";
                        break;

                    default:
                        fsgRf = strhex.substring(4, 6);
                        break;
                }
                qcn = product + "_" + number + "_" + fsgRf + "_V" + fsgversion + "_H" + Rfversion;
                return qcn;
            } catch (Exception e) {
                // TODO: handle exception
                return strhex;
            }
        }
        return "";
    }


    public static String getBuildProject() {
        String BuildProject =android.os.SystemProperties.get("pwv.project", "android");//Build.PROJECT

        return BuildProject;
    }

    public static String convertHexToString(String hex){
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for( int i=0; i<hex.length()-1; i+=2 ){
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char)decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }

    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) return null;

        StringBuilder ret = new StringBuilder(2*bytes.length);

        for (int i = 0 ; i < bytes.length ; i++) {
            int b;

            b = 0x0f & (bytes[i] >> 4);
            ret.append("0123456789abcdef".charAt(b));

            b = 0x0f & bytes[i];

            ret.append("0123456789abcdef".charAt(b));
        }

        return ret.toString();
    }

    public static boolean checkApk(String packageName ,Context mContext) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            ApplicationInfo info = mContext.getPackageManager().getApplicationInfo(
                    packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String getExternalSDPath(Context mcon) {
        String sd = null;
        StorageManager mStorageManager = (StorageManager) mcon
                .getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] volumes = mStorageManager.getVolumeList();
        //just read the first external SD
        StorageVolume mVolume = (volumes.length > VOLUME_SDCARD_INDEX) ?
                volumes[VOLUME_SDCARD_INDEX] : null;
        if(mVolume != null) {
            sd = volumes[VOLUME_SDCARD_INDEX].getPath();
            android.util.Log.d(TAG,"sd" + sd);
            return sd;
        }
        android.util.Log.d(TAG,"sd" + sd);
        return sd;
    }

    // urovo huangjiezhou add begin for multi locale to functionItems.name on 20220512
    public static String getLocaleString(Context mContext, int entriesId, int valuesId,
                                         MainApp.FunctionItem mFunctionItem) {
        String ans = mFunctionItem.getName();
        if (Locale.getDefault().getLanguage().endsWith("zh")) {
            return ans;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration configuration = new Configuration(mContext.getResources().getConfiguration());
            configuration.setLocale(Locale.SIMPLIFIED_CHINESE);
            Context transformContext = mContext.createConfigurationContext(configuration);
            String[] entries = mContext.getResources().getStringArray(entriesId);
            String[] configValues = transformContext.getResources().getStringArray(valuesId);
            String[] curLocaleValues = mContext.getResources().getStringArray(valuesId);
            String itemPackageName = mFunctionItem.getPackageName();
            String itemName = mFunctionItem.getName();

            for (int i = 0; i < entries.length; i++) {
                if (entries[i].equals(itemPackageName)) {
                    if (configValues[i].equals(itemName)) {
                        return curLocaleValues[i];
                    } else {
                        return ans;
                    }
                }
            }
        }

        return ans;
    }
    // urovo huangjiezhou add end
}
