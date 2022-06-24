/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.annotation.Nullable;
import android.view.LayoutInflater;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.icu.text.Collator;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.VisibleForTesting;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.Indexable;
import android.provider.Settings;
import androidx.preference.Preference.OnPreferenceClickListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.widget.ListView;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.widget.SimpleAdapter;
import java.util.Map;
import java.util.HashMap;
import android.widget.CheckBox;
import android.widget.AdapterView;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.CompoundButton;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.provider.Settings;
import java.util.Arrays;
import android.os.Handler;
import android.os.SystemProperties;
import android.net.wifi.ScanResult;


/**
 * UI to manage saved networks/access points.
 * TODO(b/64806699): convert to {@link DashboardFragment} with {@link PreferenceController}s
 */
public class WifiChannelSettings extends DashboardFragment{
    private static final String TAG = "WifiChannelSettings";

	public static final String KEY_WLAN_CHANNEL_24 = "wlan_channel_24";
	public static final String KEY_WLAN_CHANNEL_5 = "wlan_channel_5";

	public static final String PERSIST_WLAN_CHANNEL_24 = "persist.sys.wifi_24.config";
	public static final String PERSIST_WLAN_CHANNEL_5 = "persist.sys.wifi_5.config";
	public static final String SETTINGS_WLAN_CHANNEL_24 = "wifi_24.config";
	public static final String SETTINGS_WLAN_CHANNEL_5 = "wifi_5.config";

	private String persistWlanChannel;
	private String settingsWlanChannel;
	
	private Preference mWlanChannel24;
	private Preference mWlanChannel5;
	
	private String mWifiChannel24;
	private String mWifiChannel5;
	private String nWifiChannel;
    private List<ScanResult> wifiScanResult;
	View getlistview;
    String[] mlistText24;
	String[] mlistText5;
	String[] mlistText;
    AlertDialog.Builder builder;

	private List<String> listText;
	private BaseAdapter adapter;
	
	//wangyinghua
	private WifiManager mWifiManager;

	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mlistText24 = getContext().getResources().getStringArray(R.array.wlan_channel_24_choices);
		mlistText5 = getContext().getResources().getStringArray(R.array.wlan_channel_5_choices);
		mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);//wangyinghua
        mWlanChannel24 = /*(ListPreference)*/ getPreferenceScreen().findPreference(KEY_WLAN_CHANNEL_24);
		mWlanChannel24.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                showChannel24Dialog(0);
                return true;
            }
        });

		mWlanChannel5 = /*(ListPreference)*/ getPreferenceScreen().findPreference(KEY_WLAN_CHANNEL_5);
		mWlanChannel5.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                    		
                showChannel24Dialog(1);
                return true;
            }
        });
	}

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.CONFIGURE_WIFI;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.wifi_channel_settings;
    }

	 private void showChannel24Dialog(int band) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        getlistview = inflater.inflate(R.xml.wifi_channel_list, null);
		mWifiChannel24 = SystemProperties.get(PERSIST_WLAN_CHANNEL_24, "0");
		mWifiChannel5 = SystemProperties.get(PERSIST_WLAN_CHANNEL_5, "0");
		
        listText=new ArrayList<String>();
		Log.d(TAG,"band = "+band);
		nWifiChannel = band == 0 ? mWifiChannel24 : mWifiChannel5;
		persistWlanChannel = band == 0 ? PERSIST_WLAN_CHANNEL_24 : PERSIST_WLAN_CHANNEL_5; 
		settingsWlanChannel = band == 0 ? SETTINGS_WLAN_CHANNEL_24 : SETTINGS_WLAN_CHANNEL_5; 
		
		mlistText = band == 0 ? mlistText24 : mlistText5;
		
		if(band == 0){
           for (int i=0;i < mlistText24.length;i++){
              listText.add(mlistText24[i]);
           }
		}else{
           for (int i=0;i < mlistText5.length;i++){
              listText.add(mlistText5[i]);
           }
		}
		
        ListView listview = (ListView) getlistview.findViewById(R.id.wifi_channel_list_view);
        adapter = new MyAdapter(listText,getActivity());
        listview.setAdapter(adapter);


        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(band == 0 ? R.string.wlan_channel_24_title : R.string.wlan_channel_5_title);

        builder.setView(getlistview);
        builder.setPositiveButton("ok", new DialogOnClick());
        builder.create().show();
    }
	class DialogOnClick implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
            case Dialog.BUTTON_POSITIVE:
			    resetWifi();//wangyinghua
				//SystemProperties.set("persist.sys.is_channel_choose","1");
                break;
            case Dialog.BUTTON_NEGATIVE:
                break;
            default:
                break;
            }
        }
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
			/*wifiScanResult = mWifiManager.getScanResults();
		    if (wifiScanResult != null && wifiScanResult.size() > 0) {				
				wifiScanResult.clear();
			}*/
	}
	
	
	
 private static class ViewHolder {        
 	private TextView tv;       
	private CheckBox checkBox;       
	private View contentView;        
	public ViewHolder(View view) {           
		this.contentView = view;           
		//tv = contentView.findViewById(R.id.channel_item_text);            
		checkBox = contentView.findViewById(R.id.channel_checkbox);            
		contentView.setTag(this);        
	}    
 }
 
 private class MyAdapter extends BaseAdapter {        
 	private List<String> listText;
    private Context context;
    private Map<Integer,Boolean> map=new HashMap<>();
    public MyAdapter(List<String> listText,Context context){
        this.listText=listText;
        this.context=context;
    }       
	@Override        
		public int getCount() {        
		return listText.size();       
		}        
	@Override        
		public Object getItem(int i) {           
		return null;        
		}        
	@Override        
		public long getItemId(int i) {            
		return 0;       
		}       
	@Override        
		public View getView(final int position, View contentView, ViewGroup viewGroup) { 
		View view;
		ViewHolder holder = null;   
        if (contentView == null){
            view = View.inflate(context,R.xml.wifi_channel_list_item,null);
			holder = new ViewHolder(view);   
        }else {
            view = contentView;
			holder = (ViewHolder) contentView.getTag();
        }
        holder.checkBox.setText(listText.get(position));
		final CheckBox checkBox=(CheckBox)view.findViewById(R.id.channel_checkbox);
		Log.d(TAG,"getView position = "+position);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
			nWifiChannel = SystemProperties.get(persistWlanChannel, "0");
                if (checkBox.isChecked()){
					if(position == 0 || (nWifiChannel.split(",").length == mlistText.length-2)){
                         SystemProperties.set(persistWlanChannel, "0");
						 Settings.System.putString(context.getContentResolver(),settingsWlanChannel,"0");
					}else{
                         SystemProperties.set(persistWlanChannel, addFunction(nWifiChannel,String.valueOf(position)));
						 Settings.System.putString(context.getContentResolver(),settingsWlanChannel,addFunction(nWifiChannel,String.valueOf(position)));

					}
                }else {
                    if(position == 0){
                         SystemProperties.set(persistWlanChannel, "1");
						 Settings.System.putString(context.getContentResolver(),settingsWlanChannel,"1");
					}else{
					     SystemProperties.set(persistWlanChannel, removeFunction(nWifiChannel,String.valueOf(position)));
						 Settings.System.putString(context.getContentResolver(),settingsWlanChannel,removeFunction(nWifiChannel,String.valueOf(position)));
					}
                }
               adapter.notifyDataSetChanged();
            }
        });
    		String[] channelCheck = SystemProperties.get(persistWlanChannel, "0").split(",");
    		int [] channel = Arrays.asList(channelCheck).stream().mapToInt(Integer::parseInt).toArray();
				Log.d(TAG,"; position = "+position+"; text = "+holder.checkBox.getText());
				if(channelCheck[0].equals("0") && channelCheck.length == 1){
                    holder.checkBox.setChecked(true);
				}else{
             		if(useList(channelCheck,String.valueOf(position))){
                         holder.checkBox.setChecked(true);
             		}else{
                         holder.checkBox.setChecked(false);
     				}
				}
		
        return view;			
		}    
	} 
    public static boolean useList(String[] arr, String targetValue) {
        return Arrays.asList(arr).contains(targetValue);
    }

       private static String addFunction(String functions, String function) {

            if(functions == null){
                  functions = "0";
			}
                 if (functions.length() > 0) {
                     functions += ",";
                 }
                 functions += function;
              Log.d(TAG,"functions = "+functions+"; function = "+function);
             return functions;
        }
	   private static String removeFunction(String functions, String function) {
	   	    if(function.equals("0")){
                  return "";
			}
            String[] split = functions.split(",");
            for (int i = 0; i < split.length; i++) {
                if (function.equals(split[i])) {
                    split[i] = null;
                }
            }
            /*if (split.length == 1 && split[0] == null) {
                return UsbManager.USB_FUNCTION_NONE;
            }*/
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                if (s != null) {
                    if (builder.length() > 0) {
                        builder.append(",");
                    }
                    builder.append(s);
               }
            }
            return builder.toString();
        }

}
