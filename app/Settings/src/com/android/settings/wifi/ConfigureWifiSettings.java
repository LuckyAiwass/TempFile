/*
 * Copyright (C) 2015 The Android Open Source Project
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
package com.android.settings.wifi;

import static android.content.Context.WIFI_SERVICE;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.SearchIndexableResource;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.wifi.p2p.WifiP2pPreferenceController;
import com.android.settingslib.core.AbstractPreferenceController;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.os.Bundle;
import android.util.Log;
import android.provider.Settings;
import android.os.SystemProperties;
import java.util.ArrayList;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.net.wifi.ScanResult;
import android.os.Handler;
import java.io.IOException;

import java.io.BufferedReader;  
import java.io.InputStreamReader;  
@SearchIndexable
public class ConfigureWifiSettings extends DashboardFragment implements
                                                         Preference.OnPreferenceChangeListener{

    private static final String TAG = "ConfigureWifiSettings";

    public static final String KEY_IP_ADDRESS = "current_ip_address";
	public static final int WIFI_WAKEUP_REQUEST_CODE = 600;
	//songtingting_200813 add for wifi band&channel S
	public static final String KEY_WLAN_BAND = "wlan_band"; 
	public static final String KEY_WLAN_CHANNEL = "wlan_channel";
	private WifiWakeupPreferenceController mWifiWakeupPreferenceController;
    private UseOpenWifiPreferenceController mUseOpenWifiPreferenceController;	
	private ListPreference mWlanBand;
	private Preference mWlanChannel;
	private int wBand = 7;
	private WifiManager mWifiManager;
	
	public static final String SETTINGS_WLAN_CHANNEL_24 = "wifi_24.config";
	public static final String SETTINGS_WLAN_CHANNEL_5 = "wifi_5.config";
    private static final String Ap_Frequency_Band = "ap_frequency_band";

	//settingsWlanChannel = band == 0 ? SETTINGS_WLAN_CHANNEL_24 : SETTINGS_WLAN_CHANNEL_5; 

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mWlanBand = (ListPreference) getPreferenceScreen().findPreference(KEY_WLAN_BAND);
		mWlanChannel = (Preference) getPreferenceScreen().findPreference(KEY_WLAN_CHANNEL);
		mWlanBand.setOnPreferenceChangeListener(this);
        final Context context = getContext();
		wBand = Settings.Global.getInt(context.getContentResolver(), Ap_Frequency_Band, 7);
        mWlanBand.setValue(Integer.toString(wBand));
		mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        getPreferenceScreen().removePreference(mWlanChannel);
        
        /*if(!android.os.SystemProperties.get("ro.unc.model_name").equals("SQ53Q")){
               getPreferenceScreen().removePreference(mWlanBand);
		}*/
		//wangyinghua
		//WifiManager mWifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        //Log.d(TAG, "wangyinghua 111mWifiManager.getCountryCode():"+mWifiManager.getCountryCode());
		
				
	}
	
	
	
	@Override
     public boolean onPreferenceChange(Preference preference, Object objValue) {
         final String key = preference.getKey();
           String value = (String)objValue;
 	       Log.i(TAG,"onPreferenceChange(),value =  "+value);
		   if(value.equals("1")){
			   SystemProperties.set("persist.sys.is_24g","1");
			   SystemProperties.set("persist.sys.is_5g","0");
			   Settings.System.putString(getContentResolver(),SETTINGS_WLAN_CHANNEL_5,"0");
			   Settings.System.putString(getContentResolver(),SETTINGS_WLAN_CHANNEL_24,"66");
			   SystemProperties.set("persist.vendor.sys.channel_24g_wifi","1");
			   SystemProperties.set("persist.vendor.sys.channel_5g_wifi","0");
               SystemProperties.set("persist.vendor.sys.channel_all_wifi","0");              
			   resetWifi();
		   }else if(value.equals("6")){
			   SystemProperties.set("persist.sys.is_5g","1");
			   SystemProperties.set("persist.sys.is_24g","0");
			   Settings.System.putString(getContentResolver(),SETTINGS_WLAN_CHANNEL_24,"0");
			   Settings.System.putString(getContentResolver(),SETTINGS_WLAN_CHANNEL_5,"66");
			   SystemProperties.set("persist.vendor.sys.channel_24g_wifi","0");
			   SystemProperties.set("persist.vendor.sys.channel_5g_wifi","1");
               SystemProperties.set("persist.vendor.sys.channel_all_wifi","0");		
			   resetWifi();
		   }else{
			   SystemProperties.set("persist.sys.is_5g","0");
			   SystemProperties.set("persist.sys.is_24g","0");
			   SystemProperties.set("persist.vendor.sys.channel_24g_wifi","0");
			   SystemProperties.set("persist.vendor.sys.channel_5g_wifi","0");
               SystemProperties.set("persist.vendor.sys.channel_all_wifi","1");		
			   resetWifi();
		   }
           final Context context = getContext();
           Settings.Global.putInt(context.getContentResolver(),Ap_Frequency_Band, Integer.parseInt(value));		   
		   return true;
     }
	 
	 public void resetWifi(){
		   mWifiManager.setWifiEnabled(false);
		   Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
				  mWifiManager.setWifiEnabled(true);				 
				}
			}, 1000);
			
	}
    //songtingting_200813 add for wifi band&channel E
    @Override
    public int getMetricsCategory() {
        return SettingsEnums.CONFIGURE_WIFI;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getInitialExpandedChildCount() {
        int tileLimit = 3;//2
        if (mUseOpenWifiPreferenceController.isAvailable()) {
            tileLimit++;
        }
        return tileLimit;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.wifi_configure_settings;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        mWifiWakeupPreferenceController = new WifiWakeupPreferenceController(context, this,
                getSettingsLifecycle());
        mUseOpenWifiPreferenceController = new UseOpenWifiPreferenceController(context, this,
                getSettingsLifecycle());
        final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(mWifiWakeupPreferenceController);
        controllers.add(new NotifyOpenNetworksPreferenceController(context,
                getSettingsLifecycle()));
        controllers.add(mUseOpenWifiPreferenceController);
        controllers.add(new WifiInfoPreferenceController(context, getSettingsLifecycle(),
                wifiManager));
        controllers.add(new WifiP2pPreferenceController(context, getSettingsLifecycle(),
                wifiManager));
        return controllers;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WIFI_WAKEUP_REQUEST_CODE && mWifiWakeupPreferenceController != null) {
            mWifiWakeupPreferenceController.onActivityResult(requestCode, resultCode);
            return;
        }
        if (requestCode == UseOpenWifiPreferenceController.REQUEST_CODE_OPEN_WIFI_AUTOMATICALLY
                && mUseOpenWifiPreferenceController != null) {
            mUseOpenWifiPreferenceController.onActivityResult(requestCode, resultCode);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.wifi_configure_settings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);

                    // If connected to WiFi, this IP address will be the same as the Status IP.
                    // Or, if there is no connection they will say unavailable.
                    ConnectivityManager cm = (ConnectivityManager)
                            context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = cm.getActiveNetworkInfo();
                    if (info == null
                            || info.getType() == ConnectivityManager.TYPE_WIFI) {
                        keys.add(KEY_IP_ADDRESS);
                    }

                    return keys;
                }

                protected boolean isPageSearchEnabled(Context context) {
                    return context.getResources()
                            .getBoolean(R.bool.config_show_wifi_settings);
                }
            };
}
