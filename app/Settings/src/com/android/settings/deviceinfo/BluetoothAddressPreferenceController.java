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

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.deviceinfo.AbstractBluetoothAddressPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.R;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import androidx.annotation.VisibleForTesting;
import android.annotation.SuppressLint;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.qualcomm.qcrilhook.QcRilHook;
import com.qualcomm.qcrilhook.OemHookCallback;
import java.io.UnsupportedEncodingException;
import android.os.Handler;
import android.os.Message;
import java.util.ArrayList;
import android.os.AsyncResult;
import android.bluetooth.BluetoothAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.provider.Settings;

/**
 * Concrete subclass of bluetooth address preference controller
 */
public class BluetoothAddressPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, LifecycleObserver{

    @VisibleForTesting
    static final String KEY_BT_ADDRESS = "bt_address";

    private static final String[] CONNECTIVITY_INTENTS = {
            BluetoothAdapter.ACTION_STATE_CHANGED
    };

    private Preference mBtAddress;
	//songtingting
	private QcRilHook mQcRILHook;
	private static int mToken = 0;
	private final static int INT_SIZE = 4;
	private static final int EVENT_SHOW_STRING = 123;
	private final int MSG_GET_MODEM_INFO_RESP = 1;
	private final int MSG_GET_MODEM_INFO_STRING_RESP = 2;
	private final int SERVICE_MAX_TOKEN = 65535; 
	private String bluetoothAddress;
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
						bluetoothAddress = new String(response,"ISO-8859-1");
						Log.d("wifibluetooth","bluetoothAddress = "+bluetoothAddress.equals(oldAddress));	
                       if(!bluetoothAddress.equals(oldAddress)){
						    Settings.System.putString(mContext.getContentResolver(),"bluetooth_mac_address", bluetoothAddress);
							updatePreference();	
						}
					} catch (UnsupportedEncodingException  e) {
						Log.e("bluetooth","unsupport ISO-8859-1");
					}
					break;
             default:
                break;
         }
     }
 };

 public BluetoothAddressPreferenceController(Context context,Lifecycle lifecycle) {
        super(context);
        mContext = context;
		//songtingting add
		mQcRILHook = new QcRilHook(context);
		oldAddress = Settings.System.getString(mContext.getContentResolver(),"bluetooth_mac_address");
		mHandler = new Handler() {
		public void handleMessage(Message msg) {
			final AsyncResult ar;
			switch (msg.what) {
                case 121:
				    if(mQcRILHook.getBindQcrilMsgTunnelServiceState()){
						getModemInfoString(447);
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
           getModemInfoString(447);
		}else {
			Message msg2 = mHandler.obtainMessage(121);
			msg2.obj = new AsyncResult(null, 0, null);
			mHandler.sendMessageDelayed(msg2, 800);
		}
    }

    @Override
    public boolean isAvailable() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BT_ADDRESS;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        mBtAddress = screen.findPreference(KEY_BT_ADDRESS);
        //updatePreference();
        mBtAddress.setSummary(oldAddress);
    }
    private void updatePreference() {
         if(mQcRILHook.getBindQcrilMsgTunnelServiceState()){
            mBtAddress.setSummary(bluetoothAddress);
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
	 /*private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			final AsyncResult ar;
			switch (msg.what) {
                case 121:
				    if(mQcRILHook.getBindQcrilMsgTunnelServiceState()){
						getModemInfoString(447);
				    }else {
						Message msg2 = mHandler.obtainMessage(121);
						msg2.obj = new AsyncResult(null, 0, null);
						mHandler.sendMessageDelayed(msg2, 800);
					}
					break;
				}
			}
	 	};*/
    // This space intentionally left blank
}
