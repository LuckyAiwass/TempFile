package com.android.usettings;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.ubx.decoder.license.ActivationManager;

import java.io.File;

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
 * @Author: rocky(xiejifu)
 * @Date: 21-3-10下午4:13
 */
public class LicenseActivationAsyncTask extends AsyncTask<String, String, Boolean> {
    private static final String TAG = "DECODE" + "ActTask";
    private boolean activateAPI = android.os.Build.PWV_CUSTOM_CUSTOM.equals("TKWAY");
    /**
     * 定义回调接口
     */
    public interface OnTaskListener{
        void onResult(boolean ret, String version);
    }
    private OnTaskListener mListener;
    private Context mContext;
    public LicenseActivationAsyncTask(Context context) {
        mContext = context;
    }
    public void setTaskListener(OnTaskListener listener) {
        mListener = listener;
    }
    @Override
    protected Boolean doInBackground(String... params) {
        String serverUrl = params[0];
        //恢复出厂时间不正确
        boolean hasLicense = false;
        try {
            String buildDate = android.os.SystemProperties.get("ro.build.date.utc");
            long defaultBuildTime = Long.parseLong(buildDate) * 1000;
            long currentTime = System.currentTimeMillis();
            Log.d(TAG, " currentTime" + currentTime + " defaultBuildTime " + defaultBuildTime);
            if (currentTime < defaultBuildTime) {
                android.os.SystemClock.setCurrentTimeMillis(defaultBuildTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            boolean enableWifi = true;
            File wifiAddress = new File("sys/class/net/wlan0/address");
            if(!wifiAddress.exists()) {
                enableWifi = setWifiWnable(mContext,true);
                if(enableWifi == false) {
                    for(int i = 0; i < 8; i++) {
                        try{
                            Thread.sleep(100);
                        } catch (Exception e){}
                        if(wifiAddress.exists()) {
                            Log.d(TAG," getwifiAddress sleep " + i);
                            break;
                        }
                    }
                }
            } else {
                enableWifi = true;
            }
            //铁科定制单独调用解码库
            if(activateAPI) {
                if(TextUtils.isEmpty(serverUrl)) {
                    hasLicense = ActivationManager.activateDecoderAPI(mContext);
                } else {
                    Log.d(TAG, "activate device from local server:"+serverUrl);
                    hasLicense = ActivationManager.activateDecoderAPILocalServer(mContext, serverUrl);
                }
            } else {
                //不是所有设备支持该方式
                if("online".equals(serverUrl)) {
                    //强制使用远程服务器激活
                    hasLicense = ActivationManager.activateDecoderAPI(mContext);
                } else if(TextUtils.isEmpty(serverUrl) == false && serverUrl.startsWith("http://")) {
                    //强制使用工厂本地服务器激活
                    Log.d(TAG, "activate device from local server:"+serverUrl);
                    hasLicense = ActivationManager.activateDecoderAPILocalServer(mContext, serverUrl);
                } else {
                    hasLicense = ActivationManager.activateCommonLicense(mContext, true);
                }
            }
            Log.d(TAG, " device License " + hasLicense);
            if (enableWifi == false) {
                setWifiWnable(mContext, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasLicense;
    }
    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(mListener != null) {
            String apiVersion = ActivationManager.getAPIRevision();
            Log.d(TAG, " device apiVersion " + apiVersion);
            mListener.onResult(result, apiVersion);
        }

    }
    public static boolean setWifiWnable(Context mContext, boolean enable) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
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
}

