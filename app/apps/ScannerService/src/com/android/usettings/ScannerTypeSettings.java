/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.usettings;
import java.util.Iterator;
import java.util.Map;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.content.Context;
import android.content.SharedPreferences;
import android.device.ScanManager;
import android.util.Log;
import android.view.KeyEvent;
import android.os.IScanService;
import android.os.ServiceManager;
import android.widget.Toast;

public class ScannerTypeSettings extends PreferenceActivity implements
        Preference.OnPreferenceChangeListener{
    private static final String TAG = "UrovoSettings";

    private static final String SCAN_TYPE = "scan_type";
    //private ScanManager mScanManager;
//    private NVAccess mNVAccess;
    ListPreference ScanTypePreference;
    SharedPreferences prefs;
    CharSequence entries[];
    CharSequence entriesvaule[];  
    int typeInit;
    boolean isTypeValid = false;
    IScanService mService;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.scanner_type_settings);
		ScanTypePreference = (ListPreference) findPreference(SCAN_TYPE);

		ScanTypePreference.setOnPreferenceChangeListener(this);
		//mScanManager = new ScanManager();
//		mNVAccess = new NVAccess();
		mService = IScanService.Stub.asInterface(ServiceManager
                .getService(Context.SCAN_SERVICE));
		try{
		typeInit = mService.readConfig("SCANER_TYPE");

		Log.i(TAG, "mTypeinit = " + typeInit);
    		Map m =mService.getScanerList();
    		if(m != null) {
    	        int length = m.size();
    	        entries = new String[length];
    	        entriesvaule = new String[length];
    	        entryList();
    	        
    	        if (typeInit == 0) {
    	            ScanTypePreference.setSummary("NULL");
    	        } else {
	// urovo add shenpidong begin 2019-03-14
//    	            if (isScanTypeWriten())
//    	                ScanTypePreference.setEnabled(false);
	// urovo add shenpidong end 2019-03-14
    	            if (!isTypeValid)
    	                return;
    	            updateTimeoutPreferenceDescription(typeInit);
    	        }
    	        int ii=systemtolocal(typeInit);
    	        ScanTypePreference.setValue(String.valueOf(ii+1));
	        } else {
	// urovo add shenpidong begin 2019-03-14
//	            ScanTypePreference.setEnabled(false);
	// urovo add shenpidong end 2019-03-14
	        }
    	} catch (android.os.RemoteException e) {
        }
	}
	
	private boolean isSnWriten(){
		/*String deviceSn = Settings.System.getString(getContentResolver(), 
				Settings.System.DEVICE_SN);*/
	    try{
    	    String deviceSn = mService.readConfigs("SCANER_SN", "0");
    		Log.i(TAG,"SN=====" + deviceSn);
    		if(deviceSn != null && (deviceSn.length() == 14 || deviceSn.length()==9))
    			return true;
    	} catch (android.os.RemoteException e) {
        }
		return false;
	}
	private boolean isScanTypeWriten(){
		int scanType = android.device.provider.Settings.System.getInt(getContentResolver(), android.device.provider.Settings.System.SCANNER_TYPE, 0);
		Log.i(TAG,"scanType=====" + scanType);
		if(scanType != 0)
			return true;
		else
			return false;
	}

	private int localtosystem(int typeInit) {
		Log.d(TAG, "localtosystem " + typeInit + "=" + entriesvaule[typeInit]);
		return Integer.parseInt(entriesvaule[typeInit].toString());
	}

	private int systemtolocal(int typeInit) {
	    try{
    		int i;
		Map m = mService.getScanerList();
		if(m!=null) {
    		    int length = m.size();
    		    for (i = 0; i < length; i++) {
    			Log.d(TAG, "systemtolocal " + String.valueOf(typeInit) + "="
    					+ entriesvaule[i]);
    			if (String.valueOf(typeInit) == entriesvaule[i]) {
    				return i;
    			}
    		    }
		} else {
		    Log.d(TAG, "systemtolocal m:" + m + ",type:" + typeInit);
		}
	    } catch (android.os.RemoteException e) {
        }
		return 0;
	}

	private void entryList() {
        try{
    		Map m = mService.getScanerList();
		if(m == null) {
		    Log.d(TAG, "entryList m:" + m);
		    return;
		}
    		Iterator mi = m.entrySet().iterator();
    		int i = 0;
    		while (mi.hasNext()) {
    			Map.Entry e = (Map.Entry) mi.next();
    			Log.d(TAG, "entryList " + e.getKey() + "=" + e.getValue());
    			entries[i] = e.getKey().toString();
    			entriesvaule[i] = e.getValue().toString();
    			if(entriesvaule[i].equals(String.valueOf(typeInit))) isTypeValid = true;
    			i++;
    		}
    		ScanTypePreference.setEntries(entries);
    	} catch (android.os.RemoteException e) {
        }
	}

	private String updateentryList(int v) {
	    try{
    		Map m = mService.getScanerList();
		if(m == null) {
		    Log.d(TAG, "updateentryList m:" + m + ",v:" + v);
		    return null;
		}
//		Log.d(TAG, "updateentryList m.size:" + m.size());
    		Iterator mi = m.entrySet().iterator();
    		int i = 0;
    		while (mi.hasNext()) {
    			Map.Entry e = (Map.Entry) mi.next();
    			Log.d(TAG,"updateentryList "+e.getKey() + "=" + e.getValue());
    			if (String.valueOf(v) == e.getValue().toString()) {
    				return e.getKey().toString();
    			}
    			i++;
    		}
    	} catch (android.os.RemoteException e) {
        }
		return null;
	}

	@Override
	protected void onResume() {
		super.onResume();
		setTitle(R.string.scan_type);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		Log.i(TAG, "onPreferenceTreeClick...");
		try{
    		final String key = preference.getKey();
    		if (SCAN_TYPE.equals(key)) {
    			int value = mService.readConfig("SCANER_TYPE");
    			Log.i(TAG, "onPreferenceTreeClick..." + value);
    			int value2 = systemtolocal(value);
    			ScanTypePreference.setValue(String.valueOf(value2+1));
    			updateTimeoutPreferenceDescription(value);
    		}
		} catch (android.os.RemoteException e) {
        }
		return true;
	}

	public boolean onPreferenceChange(Preference preference, Object objValue) {
		Log.i(TAG, "onPreferenceChange...");
		final String key = preference.getKey();
		if (SCAN_TYPE.equals(key)) {
			int value = Integer.parseInt((String) objValue);
			value = localtosystem(value-1);
			try {
				Log.i(TAG, "onPreferenceChange..." + value);
				mService.writeConfig("SCANER_TYPE", value);
                                int v = mService.readConfig("SCANER_TYPE");
				updateTimeoutPreferenceDescription(v);
                                if (v != value) {
                                    Toast.makeText(preference.getContext(), R.string.scanner_select_unmatch, Toast.LENGTH_LONG).show();
				    Log.i(TAG, "onPreferenceChange..." + value + ",v:" + v);
                                    return false;
                                }
			} catch (NumberFormatException e) {
				Log.e(TAG, "could not persist tv mode setting", e);
			} catch (android.os.RemoteException e) {
	        }
			// mScanManager.setScannerType(value);
		}
		return true;
	}
    private void updateTimeoutPreferenceDescription(int value) {
        ListPreference preference = ScanTypePreference;
        String summary;
		Log.i(TAG, "updateTimeoutPreferenceDescription..." + value);
        if (value <= -1) {
            summary = "NULL";
        }  else {
            summary = preference.getContext().getString(R.string.scan_type_summary,
            		updateentryList(value));
        }
        preference.setSummary(summary);
    }
}
