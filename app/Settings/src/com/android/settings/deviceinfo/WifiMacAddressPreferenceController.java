/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.deviceinfo;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.deviceinfo.AbstractWifiMacAddressPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.text.TextUtils;

import android.util.Log;
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.qualcomm.qcrilhook.QcRilHook;
import com.qualcomm.qcrilhook.OemHookCallback;
import java.io.UnsupportedEncodingException;
import android.os.Handler;
import android.os.Message;
import android.os.AsyncResult;
import android.app.Activity;
import android.app.Fragment;
import android.provider.Settings;
/**
 * Concrete subclass of WIFI MAC address preference controller
 */
public class WifiMacAddressPreferenceController  extends AbstractPreferenceController implements
        PreferenceControllerMixin, LifecycleObserver{

    @VisibleForTesting
    static final String KEY_WIFI_MAC_ADDRESS = "wifi_mac_address";

    private static final String[] CONNECTIVITY_INTENTS = {
            ConnectivityManager.CONNECTIVITY_ACTION,
            WifiManager.LINK_CONFIGURATION_CHANGED_ACTION,
            WifiManager.NETWORK_STATE_CHANGED_ACTION,
    };

    private Preference mWifiMacAddress;
    private final WifiManager mWifiManager;
	//songtingting
	private QcRilHook mQcRILHook;
	private static int mToken = 0;
	private final static int INT_SIZE = 4;
	private static final int EVENT_SHOW_STRING = 123;
	private final int MSG_GET_MODEM_INFO_RESP = 1;
	private final int MSG_GET_MODEM_INFO_STRING_RESP = 2;
	private final int SERVICE_MAX_TOKEN = 65535; 
	private String wifiAddress;
	private String oldAddress;
	private Context mContext;
	Handler mHandler = null;
	private class MyOemHookCallback extends OemHookCallback {
      public Message mMsg;
      public MyOemHookCallback(Message msg) {
            super(msg);
            mMsg = msg;
     }
	   @Override
     public void onOemHookResponse(byte[] response, int phoneId) {
         int token = mMsg.arg1;
         switch (mMsg.what){
             case MSG_GET_MODEM_INFO_STRING_RESP:
					try {						
						wifiAddress = new String(response,"ISO-8859-1");
						Log.d("wifibluetooth","wifiAddress = "+wifiAddress.equals(oldAddress));
						if(!wifiAddress.equals(oldAddress)){
						    Settings.System.putString(mContext.getContentResolver(),"wifi_mac_address", wifiAddress);
							updatePreference();
						}

					} catch (UnsupportedEncodingException  e) {
						Log.e("wifi","unsupport ISO-8859-1");
					}
					break;
             default:
                break;
         }
     }
 };

    public WifiMacAddressPreferenceController(Context context,Lifecycle lifecycle) {
        super(context);
		mContext = context;
        mWifiManager = context.getSystemService(WifiManager.class);
		//songtingting add
		mQcRILHook = new QcRilHook(context);
		oldAddress = Settings.System.getString(mContext.getContentResolver(),"wifi_mac_address");

	    mHandler = new Handler() {
		public void handleMessage(Message msg) {
			final AsyncResult ar;
			switch (msg.what) {
                case 121:
				    if(mQcRILHook.getBindQcrilMsgTunnelServiceState()){
						getModemInfoString(4678);
				    }else {
						Message msg2 = mHandler.obtainMessage(121);
						msg2.obj = new AsyncResult(null, 0, null);
						mHandler.sendMessageDelayed(msg2, 800);
					}
					break;
				}
			}
	 	};
		if(mQcRILHook.getBindQcrilMsgTunnelServiceState()){
           getModemInfoString(4678);
		}else {
			Message msg2 = mHandler.obtainMessage(121);
			msg2.obj = new AsyncResult(null, 0, null);
			mHandler.sendMessageDelayed(msg2, 800);
		}

    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_WIFI_MAC_ADDRESS;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (Utils.isSupportCTPA(mContext)) {
            Preference macAddressPreference = screen.findPreference(getPreferenceKey());
            CharSequence oldValue = macAddressPreference.getSummary();
            String macAddress = Utils.getString(mContext, Utils.KEY_WIFI_MAC_ADDRESS);
            String unAvailable = mContext.getString(
                    com.android.settingslib.R.string.status_unavailable);
            Log.d(TAG, "displayPreference: macAddress = " + macAddress
                    + " oldValue = " + oldValue + " unAvailable = " + unAvailable);
            if (null == macAddress || macAddress.isEmpty()) {
                macAddress = unAvailable;
            }
            if (null != oldValue && (WifiInfo.DEFAULT_MAC_ADDRESS.equals(oldValue) ||
                    unAvailable.equals(oldValue))) {
                macAddressPreference.setSummary(macAddress);
            }
        }
		  mWifiMacAddress = screen.findPreference(KEY_WIFI_MAC_ADDRESS);
       // updatePreference();
       mWifiMacAddress.setSummary(oldAddress);
    }
   private void updatePreference() {
       /* WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        final int macRandomizationMode = Settings.Global.getInt(mContext.getContentResolver(),
                Settings.Global.WIFI_CONNECTED_MAC_RANDOMIZATION_ENABLED, 0);
        final String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();*/

        if(mQcRILHook.getBindQcrilMsgTunnelServiceState()){
            mWifiMacAddress.setSummary(wifiAddress);
        }
    }
	private void getModemInfoString(int type){
			int token;
			int requestId = mQcRILHook.QCRIL_EVT_HOOK_GET_MODEM_INFO_STRING;
			byte[] request = new byte[INT_SIZE];
			ByteBuffer reqBuffer = mQcRILHook.createBufferWithNativeByteOrder(request);		
			reqBuffer.putInt(type);		
			token = getToken();
			Message msg = Message.obtain(null, MSG_GET_MODEM_INFO_STRING_RESP, token, 0);
			OemHookCallback oemHookCb = new MyOemHookCallback(msg);		
			mQcRILHook.sendQcRilHookMsgAsync(requestId, request, oemHookCb, 0);
					
		}
	 private synchronized int getToken() {
        mToken++;
        if(mToken > SERVICE_MAX_TOKEN) {
            mToken = 0;
        }
        return mToken;
    }
    // This space intentionally left blank
}
