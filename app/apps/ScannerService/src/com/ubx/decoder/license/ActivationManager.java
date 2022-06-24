package com.ubx.decoder.license;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.device.LicenseHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.ubx.decoder.BarcodeReader;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

//import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Copyright (C) 2019, Urovo Ltd
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
 *
 * @Author: rocky
 * @Date: 19-12-14下午4:25
 */
public class ActivationManager {
    private static final String TAG = "ActivationManager";
    //工厂服务器地址
    public static final String URL_SERVER = "http://183.47.49.246:7088/ScannerActive/activating";
    public static final String URL_SERVER_FEEDBACK = "http://183.47.49.246:7088/ScannerActive/actFeedBack";
    public static final String Activation_DIR = "sdcard/Decode/";
    public static final String deviceRequest = "deviceRequest.bin";
    public static final String deviceResponse = "deviceResponse.bin";
    private static JSONObject jsonObj;
    private static final boolean ENABLE_FORCE_OFFLINE_ACTIVATE = false;
    private static final boolean ENABLE_FORCE_LOCAL_SERVER_ACTIVATE = true;
    private static final boolean ENABLE_FORCE_ONLINE_ACTIVATE = true;

    public static String getAPIRevision() {
        byte[] version = new byte[32];
        try {
            BarcodeReader.getAPIRevision(version);
            return (new String(version)).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "00.0.000";
    }
    public static byte[] readStream(String fileName) {
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            InputStream inStream = new FileInputStream(fileName);
            if(inStream != null) {
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                while ((len = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, len);
                }
                byte[] data = outStream.toByteArray();
                outStream.close();
                inStream.close();
                return data;
            }
            System.out.println("inStream null");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeStream(String path, InputStream inputStream) {
        File file = new File(path);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, count);
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            inputStream.close();
            System.out.println("success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean writeStream(String path, byte[] inputStream) {
        File file = new File(path);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(inputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            System.out.println("success");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static File getFileFromBytes(byte[] b, String outputFile) {
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return file;
    }

    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     *
     * @return
     */
    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private static String getMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean enable = setWifiWnable(context, true);
        String macAddress = "02:00:00:00:00:00";
        if (enable == false) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
            for (int i = 0; i < 8; i++) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
                if (!TextUtils.isEmpty(macAddress) && !macAddress.equals("02:00:00:00:00:00")) {
                    Log.d("ActivationManager", " wifiAddress sleep " + i);
                    break;
                }
                wifiInfo = wifiManager.getConnectionInfo();
                if (null != wifiInfo) {
                    macAddress = wifiInfo.getMacAddress();
                }
            }
        } else {
            /*if (enable == false)
                setWifiWnable(enable);*/
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
        }
        return macAddress;
    }

    private static boolean setWifiWnable(Context context, boolean enable) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (enable) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(enable);
                return false;
            }
        } else {
            wifiManager.setWifiEnabled(enable);
        }
        return true;
    }

    private static byte[] StrToHexByte(String str) {
        if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(
                        str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    private static String getAPPVersion(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pinfo = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            return pinfo.versionName;
        } catch (Exception e) {
            Log.d(TAG, "ERROR"+TAG+ "getVersionName failed:" + e.getMessage());
        }
        return "1.0.1";
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean wifi=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        boolean internet=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        return activeNetworkInfo != null;
    }
    public static ActivationResult activateAPIFeedback(Context context, String relVersion, int actStatus, byte[] identitiyClient)
    {
        String dvcMac = getMacFromHardware();
        if("02:00:00:00:00:00".equals(dvcMac)) {
            Log.d(TAG, "ERROR"+TAG+ "getMacFromHardware failed:" );
            return ActivationResult.FAILED_UNKNOWN;
        }
        String dvcId = (new android.device.DeviceManager()).getDeviceId();
        String dvcType = Build.MODEL;
        //String scanVersion = Build.VERSION.RELEASE;//getAPPVersion(context);
        String osVersion = Build.ID;
        String ip = "192.168.8.1";
        String computerName = "Android";
        String fileData = bytesToHexString(identitiyClient);
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        String dateString = "YY" + formatter.format(currentTime);
        String plainText = dvcId + dvcType + dvcMac + relVersion + osVersion + ip + computerName + fileData + dateString;
        //Log.d(TAG, "MD5 plainText=" + plainText);
        //String validateParams = DigestUtils.md5Hex(plainText);
        //Log.d(TAG, "MD5 chiperText=" + validateParams);
        //String protocol = "?protocol=http";
        //String url = URL_SERVER + protocol + sb.toString();
        try {
            jsonObj = new JSONObject();
            jsonObj.put("dvcId", dvcId);
            jsonObj.put("dvcType", dvcType);
            jsonObj.put("dvcMac", dvcMac.replace(":","-"));
            jsonObj.put("scanVersion", relVersion);
            jsonObj.put("osVersion", osVersion);
            jsonObj.put("ip", ip);
            jsonObj.put("computerName", computerName);
            jsonObj.put("feedBackActStatus", actStatus);
            jsonObj.put("dvcRespBin", fileData);
            jsonObj.put("validateParams", dateString);
            Log.d(TAG, "jsonObj=" + jsonObj.toString());
            HttpURLConnection connection = null;
            InputStream in = null;
            try {
                URL url = new URL(URL_SERVER_FEEDBACK);
                connection = (HttpURLConnection) url.openConnection();
                //connection.setInstanceFollowRedirects(true);
                //connection.setRequestProperty("User-Agent", "PacificHttpClient");
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
                out.append(jsonObj.toString());
                out.flush();
                out.close();
                if (connection.getResponseCode() == 404) {
                    Log.d(TAG, "get updates from 404 error! ");
                    throw new Exception("404 error!");
                }
                in = connection.getInputStream();
                // 读取响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line;
                String res = "";
                while ((line = reader.readLine()) != null) {
                    res += line;
                }
                Log.d(TAG, "readLine " + res);
                reader.close();
                if (!TextUtils.isEmpty(res)) {
                    try {
                        JSONObject jsonObject = new JSONObject(res);
                        int activeFlag = jsonObject.getInt("feedBackFlag");
                        if (activeFlag == 1) {
                            return ActivationResult.SUCCESS;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "get updates error:" + e);
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ActivationResult.FAILED_UNKNOWN;
    }
    public static ActivationResult activateAPIWithLocalServer(Context context, byte[] identitiyClient)
    {
        String dvcMac = getMacFromHardware();
        if("02:00:00:00:00:00".equals(dvcMac)) {
            Log.d(TAG, "ERROR"+TAG+ "getMacFromHardware failed:" );
            return ActivationResult.FAILED_UNKNOWN;
        }
        String dvcId = (new android.device.DeviceManager()).getDeviceId();
        String dvcType = Build.MODEL;
        String scanVersion = Build.VERSION.RELEASE;//getAPPVersion(context);
        String osVersion = Build.ID;
        String ip = "192.168.8.1";
        String computerName = "Android";
        String fileData = bytesToHexString(identitiyClient);
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        String dateString = "YY" + formatter.format(currentTime);
        String plainText = dvcId + dvcType + dvcMac + scanVersion + osVersion + ip + computerName + fileData + dateString;
        //Log.d(TAG, "MD5 plainText=" + plainText);
        //String validateParams = DigestUtils.md5Hex(plainText);
        //Log.d(TAG, "MD5 chiperText=" + validateParams);
        //String protocol = "?protocol=http";
        //String url = URL_SERVER + protocol + sb.toString();
        try {
            jsonObj = new JSONObject();
            jsonObj.put("dvcId", dvcId);
            jsonObj.put("dvcType", dvcType);
            jsonObj.put("dvcMac", dvcMac.replace(":","-"));
            jsonObj.put("scanVersion", scanVersion);
            jsonObj.put("osVersion", osVersion);
            jsonObj.put("ip", ip);
            jsonObj.put("computerName", computerName);
            jsonObj.put("fileData", fileData);
            jsonObj.put("validateParams", dateString);
            Log.d(TAG, "jsonObj=" + jsonObj.toString());
            HttpURLConnection connection = null;
            InputStream in = null;
            try {
                URL url = new URL(URL_SERVER);
                connection = (HttpURLConnection) url.openConnection();
                //connection.setInstanceFollowRedirects(true);
                //connection.setRequestProperty("User-Agent", "PacificHttpClient");
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
                out.append(jsonObj.toString());
                out.flush();
                out.close();
                if (connection.getResponseCode() == 404) {
                    Log.d(TAG, "get updates from 404 error! ");
                    throw new Exception("404 error!");
                }
                in = connection.getInputStream();
                // 读取响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                String line;
                String res = "";
                while ((line = reader.readLine()) != null) {
                    res += line;
                }
                Log.d(TAG, "WithLocalServer readLine " + res);
                reader.close();
                if (!TextUtils.isEmpty(res)) {
                    try {
                        JSONObject jsonObject = new JSONObject(res);
                        int activeFlag = jsonObject.getInt("activeFlag");
                        String responseLicense = jsonObject.getString("license");
                        if (activeFlag == 1 && !TextUtils.isEmpty(responseLicense)) {
                            byte[] License = StrToHexByte(responseLicense);
                            boolean success = updateLicensFromLocalServer(License);//writeStream(Activation_DIR + deviceResponse, License);
                            if (success) {
                                return ActivationResult.SUCCESS;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "get updates error:" + e);
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ActivationResult.FAILED_UNKNOWN;
    }
    public static void activateAsync(Context context, byte[] identitiyClient, final ActivationResponseListener listener) {
        boolean enable = setWifiWnable(context, true);
        if(enable == false) {
            File wifiAddress = new File("sys/class/net/wlan0/address");
            for(int i = 0; i < 8; i++) {
                try{
                    Thread.sleep(100);
                } catch (Exception e){}
                if(wifiAddress.exists()) {
                    Log.d(TAG," wifiAddress sleep " + i);
                    break;
                }
            }
        }
        String dvcMac = getMacFromHardware();//getMacAddress(context);
        if("02:00:00:00:00:00".equals(dvcMac)) {
            Log.d(TAG, "ERROR"+TAG+ "getMacFromHardware failed:" );
            if (listener != null) {
                listener.onActivationComplete(ActivationResult.FAILED_UNKNOWN);
            }
            return;
        }
        String dvcId = (new android.device.DeviceManager()).getDeviceId();
        String dvcType = Build.MODEL;
        String scanVersion = Build.VERSION.RELEASE;
        String osVersion = Build.ID;
        String ip = "192.168.8.1";
        String computerName = "Android";
        String fileData = bytesToHexString(identitiyClient);
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        String dateString = "YY" + formatter.format(currentTime);
        String plainText = dvcId + dvcType + dvcMac + scanVersion + osVersion + ip + computerName + fileData + dateString;
        Log.d(TAG, "MD5 plainText=" + plainText);
        //String validateParams = DigestUtils.md5Hex(plainText);
        //Log.d(TAG, "MD5 chiperText=" + validateParams);
        //String protocol = "?protocol=http";
        //String url = URL_SERVER + protocol + sb.toString();
        try {
            jsonObj = new JSONObject();
            jsonObj.put("dvcId", dvcId);
            jsonObj.put("dvcType", dvcType);
            jsonObj.put("dvcMac", dvcMac);
            jsonObj.put("scanVersion", scanVersion);
            jsonObj.put("osVersion", osVersion);
            jsonObj.put("ip", ip);
            jsonObj.put("computerName", computerName);
            jsonObj.put("fileData", fileData);
            jsonObj.put("validateParams", dateString);
            Log.d(TAG, "jsonObj=" + jsonObj.toString());
            new Thread(new Runnable() {
                public void run() {
                    HttpURLConnection connection = null;
                    InputStream in = null;
                    try {
                        URL url = new URL(URL_SERVER);
                        connection = (HttpURLConnection) url.openConnection();
                        //connection.setInstanceFollowRedirects(true);
                        //connection.setRequestProperty("User-Agent", "PacificHttpClient");
                        connection.setRequestMethod("POST");
                        connection.setRequestProperty("Accept", "application/json");
                        connection.setRequestProperty("Content-Type", "application/json");
                        connection.setConnectTimeout(10000);
                        connection.setReadTimeout(10000);
                        connection.connect();
                        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
                        out.append(jsonObj.toString());
                        out.flush();
                        out.close();
                        if (connection.getResponseCode() == 404) {
                            Log.d(TAG, "get updates from 404 error! ");
                            throw new Exception("404 error!");
                        }
                        in = connection.getInputStream();
                        // 读取响应
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                        String line;
                        String res = "";
                        while ((line = reader.readLine()) != null) {
                            res += line;
                        }
                        Log.d(TAG, "readLine " + res);
                        reader.close();
                        if (!TextUtils.isEmpty(res)) {
                            try {
                                JSONObject jsonObject = new JSONObject(res);
                                int activeFlag = jsonObject.getInt("activeFlag");
                                String responseLicense = jsonObject.getString("license");
                                if (activeFlag == 1 && !TextUtils.isEmpty(responseLicense)) {
                                    byte[] License = StrToHexByte(responseLicense);
                                    boolean success = writeStream(Activation_DIR + deviceResponse, License);
                                    if (success) {
                                        if (listener != null) {
                                            listener.onActivationComplete(ActivationResult.SUCCESS);
                                        }
                                    } else {
                                        if (listener != null) {
                                            listener.onActivationComplete(ActivationResult.FAILED_DEVICEID_ERROR);
                                        }
                                    }
                                } else {
                                    if (listener != null) {
                                        listener.onActivationComplete(ActivationResult.FAILED_UNKNOWN);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "get updates error:" + e);
                        e.printStackTrace();
                        if (listener != null) {
                            listener.onActivationComplete(ActivationResult.FAILED_UNKNOWN);
                        }
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onActivationComplete(ActivationResult.FAILED_UNKNOWN);
            }
        }
    }
    public static void syncLicenseStrore(boolean activate) {
        if(!activate) {
            if(ENABLE_FORCE_OFFLINE_ACTIVATE) {
                if(!LicenseHelper.checkActivationResponseFileExists()) {
                    if(LicenseHelper.updateResponseBinFromCAStrore() == false) {
                        if(!LicenseHelper.checkFileExists()) {
                            LicenseHelper.updateLicensFromCAStrore();
                        }
                    }
                }
            } else {
                if(!LicenseHelper.checkFileExists()) {
                    LicenseHelper.updateLicensFromCAStrore();
                }
            }
        } else {
            if(ENABLE_FORCE_OFFLINE_ACTIVATE) {
                if(LicenseHelper.checkActivationResponseFileExists()) {
                    LicenseHelper.syncResponseBinStrore();
                } else if(LicenseHelper.checkFileExists()) {
                    LicenseHelper.syncLicenseStrore();
                }
            } else {
                if(LicenseHelper.checkFileExists()) {
                    LicenseHelper.syncLicenseStrore();
                }
            }
        }
    }
    public static void clearDir() {
        try {
            File file = new File(Activation_DIR);
            boolean exists = file.exists();
            if(exists) {
                file.delete();
                file = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean checkActivateStroreFile() {
        Log.d(TAG, "checkActivateStroreFile start");
        if(LicenseHelper.checkFileExists()) {
            return true;
        } else {
            //文件大小不对
            LicenseHelper.clearExistsFiles();
        }
        if(ENABLE_FORCE_OFFLINE_ACTIVATE) {
            if(LicenseHelper.checkActivationResponseFileExists()) {
                return true;
            } else {
                 LicenseHelper.clearResponseExistsFiles();
            }
            if(LicenseHelper.updateResponseBinFromCAStrore()) {
                if(LicenseHelper.checkActivationResponseFileExists()) {
                    return true;
                } else {
                     LicenseHelper.clearResponseExistsFiles();
                }
            }
        }
        if(LicenseHelper.updateLicensFromCAStrore()) {
            if(LicenseHelper.checkFileExists()) {
                return true;
            } else {
                //文件大小不对
                LicenseHelper.clearExistsFiles();
            }
        }
        Log.d(TAG, "checkActivateStroreFile end");
        return false;
    }
    /*将assets文件夹下的数据文件写入SD卡中*/
    public static void copyLicFile(Context context, String path, String srcPath) {
        InputStream inputStream;
        try {
            File fileDir = new File(Activation_DIR);
            fileDir.mkdirs();
            File file = new File(path);
            inputStream = context.getResources().getAssets().open(srcPath);
            if(inputStream != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();
                inputStream = null;
                Log.d(TAG, "copy success end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void copyLicFile(String path, String srcPath) {
        FileInputStream inputStream;
        try {
            File file = new File(path);
            inputStream = new FileInputStream(srcPath);
            if(inputStream != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int count = 0;
                while ((count = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                inputStream.close();
                inputStream = null;
                Log.d(TAG, "copy success end");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //d0:4e:50:38:fc:fd
    static String d04e5038fcfdAnchoringBase64 = "L5DOZwEqtMYZAAAAAIMqZWAgAAAAAwAAABwAAAAYAJNbIpPRbzq/+dSbGwQe+3aNezEYZ2xfb8ii6wXziUnBb4mw4BnH+cgI3DUHXJKN0U/99k9zAlHvozKNRd5Cn8ubXyjhAz5p4Gin/9T27oAj05toCVaP/1zLGeD7Scon8iwSUUcuAHUzFWH5KPjZ8h7gxitj0CpfIg==";
    static String isActivateed = "sdcard/activate";
    static String fixedReleaseVersion = "V20210602";
    /**
     * 不是所有设备支持该方式
     * 扫描头开启时，调用此接口激活 兼容activateAPIWithLocalFile()
     * @return
     */
    public static boolean activateCommonLicense(Context context, boolean feedback) {
        try {
            if(LicenseHelper.checkFileExists()) {//检查是否有激活文件并且文件大小正确
                File file = new File(isActivateed);
                boolean exists = file.exists();
                if(exists) {
                    //旧版本激活过,激活失败可能设备信息mac修改过
                    if(BarcodeReader.activateLicense(Activation_DIR) == 0) {
                        return true;
                    } else {
                        LicenseHelper.clearExistsFiles();
                    }
                } else {
                    byte[] anchoring = ActivationManager.readStream(anchoring_filePath);
                    if(anchoring != null) {
                        //检查文件是不是固定anchoring
                        String anchoringBase64 = Base64.encodeToString(anchoring, Base64.NO_WRAP);
                        if(d04e5038fcfdAnchoringBase64.equals(anchoringBase64)) {
                            if(BarcodeReader.activate(Activation_DIR, 1) == 0) {
                                return true;
                            } else {
                                //文件大小不对
                                LicenseHelper.clearExistsFiles();
                            }
                        } else {
                            Log.e(TAG, "no fixed");
                            if(BarcodeReader.activateLicense(Activation_DIR) == 0) {
                                writeStream(isActivateed,Build.MODEL.getBytes());
                                return true;
                            } else {
                                LicenseHelper.clearExistsFiles();
                            }
                        }
                    } else {
                        Log.e(TAG, "read failed");
                    }
                }
            }
            //文件大小不对
            LicenseHelper.deleteDir();
            //android9.x 及以前设备有备份文件.是否有备份文件
            if(LicenseHelper.updateLicensFromCAStrore()) {
                byte[] anchoring = ActivationManager.readStream(anchoring_filePath);
                if(anchoring != null) {
                    //检查文件是不是固定anchoring
                    String anchoringBase64 = Base64.encodeToString(anchoring, Base64.NO_WRAP);
                    if(d04e5038fcfdAnchoringBase64.equals(anchoringBase64)) {
                        if(BarcodeReader.activate(Activation_DIR, 1) == 0) {
                            return true;
                        } else {
                            //文件大小不对
                            LicenseHelper.clearExistsFiles();
                        }
                    } else {
                        Log.e(TAG, "no fixed");
                        if(BarcodeReader.activateLicense(Activation_DIR) == 0) {
                            //记录旧方式激活过的设备
                            writeStream(isActivateed,Build.MODEL.getBytes());
                            return true;
                        } else {
                            LicenseHelper.clearExistsFiles();
                        }
                    }
                } else {
                    Log.e(TAG, "read failed");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            File file = new File(isActivateed);
            boolean exists = file.exists();
            if(exists) {
                file.delete();
                file = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //文件大小不对
        LicenseHelper.deleteDir();
        //force to activate
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
            copyLicFile(anchoring_filePath, "etc/anchoring-0");
            copyLicFile(storage_filePath, "etc/storage-0");
        } else {
            copyLicFile(context, anchoring_filePath, "license/anchoring-0");
            copyLicFile(context, storage_filePath, "license/storage-0");
        }
        if(BarcodeReader.activate(Activation_DIR, 1) == 0) {
            if(feedback && isNetworkAvailable(context)) {
                ActivationResult result = activateAPIFeedback(context, fixedReleaseVersion, 1, readStream());
                Log.e(TAG, " successed activateAPIFeedback = " + result);
            }
            return true;
        } else {
            LicenseHelper.clearExistsFiles();
        }
        return false;
    }
    //扫描头开启时，调用此接口激活
    public static boolean activateAPIWithLocalFile() {
        if(LicenseHelper.checkFileExists()) {
            if(BarcodeReader.activateLicense(Activation_DIR) == 0) {
                //LicenseHelper.syncLicenseStrore();
                return true;
            } else {
                LicenseHelper.clearExistsFiles();
            }
        } else {
            //文件大小不对
            LicenseHelper.deleteDir();
        }
        if(ENABLE_FORCE_OFFLINE_ACTIVATE) {
            if(LicenseHelper.checkActivationResponseFileExists()) {
                if(BarcodeReader.consumeLicenseResponse(ActivationManager.Activation_DIR, ActivationManager.deviceResponse) == 0) {
                    if(LicenseHelper.checkFileExists()) {
                        return true;
                    } else {
                        //文件大小不对
                        LicenseHelper.clearExistsFiles();
                    }
                }
            }
            if(LicenseHelper.updateResponseBinFromCAStrore()) {
                if(BarcodeReader.consumeLicenseResponse(ActivationManager.Activation_DIR, ActivationManager.deviceResponse) == 0) {
                    if(LicenseHelper.checkFileExists()) {
                        return true;
                    } else {
                        //文件大小不对
                        LicenseHelper.clearExistsFiles();
                    }
                }
            }
        }
        if(LicenseHelper.updateLicensFromCAStrore()) {
            if(BarcodeReader.activateLicense(Activation_DIR)== 0) {
                return true;
            } else {
                //文件大小不对
                LicenseHelper.clearExistsFiles();
            }
        }
        return false;
    }

    /**
     * 通过工厂本地服务器激活
     * 第一次激活：指定目录下需要存放IdentityClient.bin文件
     * 激活成功后都会有这个两个文件:anchoring-0  storage-0
     *
     *
     * @param context
     * @param serverUrl 服务器地址
     * @return
     */
    public static boolean activateDecoderAPILocalServer(Context context, String serverUrl) {

        if(LicenseHelper.checkFileExists()) {
            if(BarcodeReader.activateLicense(Activation_DIR) == 0) {
                LicenseHelper.syncLicenseStrore();
                return true;
            } else {
                LicenseHelper.deleteDir();
            }
        } else {
            LicenseHelper.deleteDir();
        }

        //检查是否备份有通过远程服务器激活的文件
        if(LicenseHelper.updateLicensFromCAStrore()) {
            if(BarcodeReader.activateLicense(Activation_DIR)== 0) {
                LicenseHelper.syncLicenseStrore();
                return true;
            } else {
                LicenseHelper.deleteDir();
            }
        }
        //连接离线服务器激活
        if(isNetworkAvailable(context)) {
            //检查激活目录下是否有IdentityClient.bin文件
            LicenseHelper.copyIdentityClientFiles(context);
            int ret = BarcodeReader.localServerLicense(Activation_DIR, serverUrl);
            Log.e(TAG, " localServerLicense = " + ret);
            if(LicenseHelper.checkFileExists()) {
                //如果激活成功会生成两个有效激活文件，通过检查文件大小是否正确判断激活是否成功。成功则仅备份anchoring-0  storage-0文件
                if(BarcodeReader.activateLicense(Activation_DIR) == 0) {
                    LicenseHelper.syncLicenseStrore();
                    ActivationResult result = activateAPIFeedback(context, Build.VERSION.RELEASE, 1, readStream());
                    Log.e(TAG, " successed activateAPIFeedback = " + result);
                    return true;
                } else {
                    LicenseHelper.deleteDir();
                }
            } else {
                LicenseHelper.deleteDir();
            }
        }
        return false;
    }
    /**
     * 需在线程中调用
     * @param context
     * @return
     */
    public static boolean activateDecoderAPI(Context context) {
        //激活成功后都会有这个两个文件:anchoring-0  storage-0
        if(LicenseHelper.checkFileExists()) {
            if(BarcodeReader.activateLicense(Activation_DIR) == 0) {
                LicenseHelper.syncLicenseStrore();
                return true;
            } else {
                LicenseHelper.deleteDir();
            }
        } else {
            LicenseHelper.deleteDir();
        }
        if(ENABLE_FORCE_OFFLINE_ACTIVATE) {
            //使用离线文件.bin进行激活
            if(LicenseHelper.checkActivationResponseFileExists()) {
                if(BarcodeReader.consumeLicenseResponse(ActivationManager.Activation_DIR, ActivationManager.deviceResponse) == 0) {
                    if(LicenseHelper.checkFileExists()) {
                        return true;
                    } else {
                        //文件大小不对
                        LicenseHelper.clearExistsFiles();
                    }
                }
            }
            //检测是否有备份bin激活文件
            if(LicenseHelper.updateResponseBinFromCAStrore()) {
                if(BarcodeReader.consumeLicenseResponse(ActivationManager.Activation_DIR, ActivationManager.deviceResponse) == 0) {
                    if(LicenseHelper.checkFileExists()) {
                        return true;
                    } else {
                        //文件大小不对
                        LicenseHelper.clearExistsFiles();
                    }
                }
            }
        }
        //检查是否备份有通过远程服务器激活的文件
        if(LicenseHelper.updateLicensFromCAStrore()) {
            if(BarcodeReader.activateLicense(Activation_DIR)== 0) {
                LicenseHelper.syncLicenseStrore();
                return true;
            } else {
                LicenseHelper.deleteDir();
            }
        }
        //连接离线服务器激活
        if(isNetworkAvailable(context)) {
            if(ENABLE_FORCE_OFFLINE_ACTIVATE) {
                int activateResult = BarcodeReader.generateLicenseRequest(Activation_DIR, deviceRequest);
                if(activateResult == 0) {
                    ActivationResult result = activateAPIWithLocalServer(context, readStream(Activation_DIR + deviceRequest));
                    if(result == ActivationResult.SUCCESS) {
                        if(LicenseHelper.checkActivationResponseFileExists()) {
                            if(BarcodeReader.consumeLicenseResponse(ActivationManager.Activation_DIR, ActivationManager.deviceResponse) == 0) {
                                //如果激活成功会生成两个有效激活文件，通过检查文件大小是否正确判断激活是否成功。成功则仅备份bin文件
                                if(LicenseHelper.checkFileExists()) {
                                    LicenseHelper.syncResponseBinStrore();
                                    result = activateAPIFeedback(context,  Build.VERSION.RELEASE, 1, readStream(ActivationManager.Activation_DIR + ActivationManager.deviceResponse));
                                    Log.e(TAG, " successed activateAPIFeedback = " + result);
                                    return true;
                                } else {
                                    result = activateAPIFeedback(context,  Build.VERSION.RELEASE, 0, readStream(ActivationManager.Activation_DIR + ActivationManager.deviceResponse));
                                    Log.e(TAG, " activateAPIFeedback = " + result);
                                }
                            }
                        }
                    }
                }
            }
            if(ENABLE_FORCE_LOCAL_SERVER_ACTIVATE) {
                try{
                    //访问服务器是否有存储该MAC地址对应的激活文件
                    ActivationResult result = activateAPIWithLocalServer(context, getMacFromHardware().getBytes());
                    if(result == ActivationResult.SUCCESS) {
                        if(LicenseHelper.checkFileExists()) {
                            //如果激活成功会生成两个有效激活文件，通过检查文件大小是否正确判断激活是否成功。成功则仅备份bin文件
                            if(BarcodeReader.activateLicense(Activation_DIR) == 0) {
                                LicenseHelper.syncLicenseStrore();
                                result = activateAPIFeedback(context,  Build.VERSION.RELEASE, 1, readStream());
                                Log.e(TAG, " successed activateAPIFeedback = " + result);
                                return true;
                            } else {
                                result = activateAPIFeedback(context,  Build.VERSION.RELEASE, 0, getMacFromHardware().getBytes());
                                Log.e(TAG, " activateAPIFeedback = " + result);
                                LicenseHelper.deleteDir();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //远程服务器激活方式
            if(ENABLE_FORCE_ONLINE_ACTIVATE) {
                if(BarcodeReader.activateLicense(Activation_DIR) == 0) {
                    LicenseHelper.syncLicenseStrore();
                    ActivationResult result = activateAPIFeedback(context,  Build.VERSION.RELEASE,1, readStream());
                    Log.e(TAG, " ONLINE successed activateAPIFeedback = " + result);
                    return true;
                }
            }
        }
        return false;
    }
    static String anchoring_filePath = String.format("%s/anchoring-0", Activation_DIR/*Environment.getExternalStorageDirectory()*/);
    static String storage_filePath = String.format("%s/storage-0", Activation_DIR/*Environment.getExternalStorageDirectory()*/);
    static final int RW_BUFFER_MAX_LENGTH = 20 * 1024;
    static final int RW_BUFFER_MIN_LENGTH = 1024;
    private static byte[] readStream() {
        try {
            byte[] anchoringBuffer = new byte[RW_BUFFER_MIN_LENGTH];
            byte[] storageBuffer = new byte[RW_BUFFER_MAX_LENGTH];
            FileInputStream fileStream = new FileInputStream(anchoring_filePath);
            int readAnchoringBytes = fileStream.read(anchoringBuffer, 0, RW_BUFFER_MIN_LENGTH);
            Log.d(TAG, "sync anchoring License length=" + readAnchoringBytes);
            fileStream.close();
            fileStream = new FileInputStream(storage_filePath);
            int readStorageBytes = fileStream.read(storageBuffer, 0, RW_BUFFER_MAX_LENGTH);
            fileStream.close();
            Log.d(TAG, "sync License length=" + readStorageBytes);
            if (readAnchoringBytes > 0 && readStorageBytes > 0) {
                Log.d(TAG, "sync new Licens ");
                byte[] caKEY = new byte[readAnchoringBytes + readStorageBytes + 6];
                caKEY[0] = 0x02;
                caKEY[1] = (byte)((readAnchoringBytes) >> 8);
                caKEY[2] =(byte)((readAnchoringBytes) & 0xff) ;
                System.arraycopy(anchoringBuffer, 0, caKEY, 3, readAnchoringBytes);
                caKEY[readAnchoringBytes + 3] = (byte)((readStorageBytes) >> 8);
                caKEY[readAnchoringBytes + 4] =(byte)((readStorageBytes) & 0xff) ;
                System.arraycopy(storageBuffer, 0, caKEY, 3 + readAnchoringBytes + 2, readStorageBytes);
                caKEY[readAnchoringBytes + readStorageBytes + 3 + 2] = 0x03;
                //Log.d(TAG, caKEY.length + " sync new Licens " + bytesToHexString(caKEY, 10900, readAnchoringBytes + readStorageBytes + 6));
                return caKEY;
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
        }
        return null;
    }
    public static boolean updateLicensFromLocalServer(byte[] bufferKey) {
        int readAnchoringBytes = ((int)(bufferKey[1] & 0xff)) *256 + ((int)(bufferKey[2] & 0xff));
        Log.d(TAG, "LocalServer update From Licens readAnchoringBytes=" + readAnchoringBytes);
        int readStorageBytes = ((int)(bufferKey[readAnchoringBytes + 3] & 0xff)) *256 + ((int)(bufferKey[readAnchoringBytes + 4] & 0xff));
        Log.d(TAG, "update From Licens readStorageBytes=" + readStorageBytes);

        if(readStorageBytes > 0 && readAnchoringBytes > 0) {
            Log.d(TAG, "update  From Licens to file");
            int STX = bufferKey[0];
            int ETX = bufferKey[readAnchoringBytes + readStorageBytes + 3 + 2];
            if( STX == 0x02 && ETX == 0x03) {
                byte[] anchoringBuffer = new byte[readAnchoringBytes];
                byte[] storageBuffer = new byte[readStorageBytes];

                System.arraycopy(bufferKey, 3, anchoringBuffer, 0, readAnchoringBytes);
                System.arraycopy(bufferKey, 3 + readAnchoringBytes + 2, storageBuffer, 0, readStorageBytes);
                boolean result = writeFile(anchoring_filePath, anchoringBuffer);
                result = writeFile(storage_filePath, storageBuffer);
                Log.d(TAG, "LocalServer update From Licens OK STX=" + STX + " ETX = " + ETX);
                return result;

            }
            Log.d(TAG, "LocalServer update From Licens STX=" + STX + " ETX = " + ETX);
        }
        return false;
    }
    private static boolean writeFile(String fileName, byte[] data) {
        boolean ret = false;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File fileDir = new File(Activation_DIR);
            fileDir.mkdirs();
            file = new File(fileName);
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(data);
            bos.flush();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return ret;
    }
}
