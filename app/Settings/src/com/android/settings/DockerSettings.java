package com.android.settings;

import com.android.settings.ethernet.EthernetConfigDialog;
import com.android.settings.ethernet.EthernetEnabler;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.EthernetManager;
import android.os.Build;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.CheckBoxPreference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceCategory;
import android.provider.Settings;
import android.provider.*;
import android.text.TextUtils;
import android.util.Log;
import android.os.Handler;
import android.os.SystemClock;
import java.io.BufferedReader;
import java.io.FileReader;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import android.os.SystemProperties;

/*** yangkun add for Docking station 2020/09/03 ***/
public class DockerSettings extends SettingsPreferenceFragment {

	private static final String KEY_USBHOST_TOGGLE = "host_toggle";
	private static final String KEY_ETH_TOGGLE = "eth_toggle";
	private static final String KEY_ETH_CONFIG = "eth_config";
	private static final String KEY_ETH_PROXY = "eth_proxy";
	private static final String KEY_ETH_INFO_MAC = "eth_mac_address";
	private static final String KEY_SCANHANDLE_TOGGLE = "scanhandle_toggle";

	private IntentFilter intentFilter;

	private CheckBoxPreference hostToggle;	
	private CheckBoxPreference ethToggle;
	private CheckBoxPreference scanHandleToggle;
	
	Preference ethConfig;
	PreferenceScreen ethProxy;
	Preference ethInfoMac;
	Preference ethInfoIp;
    
	private EthernetManager mEthernetManager;
    private EthernetEnabler mEthEnabler;
    private EthernetConfigDialog mEthConfigDialog;
    private PreferenceCategory mPreferenceCategory;
	private DockManager mDockManager;	
	
	@Override
	public void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		
		addPreferencesFromResource(R.xml.docker_settings);
		hostToggle = (CheckBoxPreference) findPreference(KEY_USBHOST_TOGGLE);
		ethToggle = (CheckBoxPreference) findPreference(KEY_ETH_TOGGLE);
		scanHandleToggle = (CheckBoxPreference) findPreference(KEY_SCANHANDLE_TOGGLE);
		
		ethConfig = findPreference(KEY_ETH_CONFIG);
		ethProxy = (PreferenceScreen) findPreference(KEY_ETH_PROXY);
		ethInfoMac = findPreference(KEY_ETH_INFO_MAC);		
		
		mEthernetManager = (EthernetManager) getActivity().getSystemService(Context.ETHERNET_SERVICE);

		mPreferenceCategory = (PreferenceCategory) findPreference("docker_lan");
        if (Build.PWV_CUSTOM_CUSTOM.equals("Reliance") && mPreferenceCategory != null) {
            mPreferenceCategory.setEnabled(false);
            if (ethInfoMac != null) {
                ethInfoMac.setEnabled(false);
            }
        }
		intentFilter = new IntentFilter("docker.setting.change");
		final Activity activity = getActivity();
		mDockManager = new DockManager(getActivity());
		initToggles();
		removePreference(KEY_SCANHANDLE_TOGGLE);
	}
	
	private void initToggles() {
		final Context context = getActivity();		
		hostToggle.setChecked(Settings.System.getInt(getActivity().getContentResolver(), "sys.hostkey.switch", 0) != 0);
		ethToggle.setChecked(Settings.System.getInt(getActivity().getContentResolver(),"sys.ethernet.switch", 0) != 0);
		// scanHandleToggle.setChecked(mDockManager.isScanHandleEnable());
        mEthEnabler = new EthernetEnabler((CheckBoxPreference) findPreference (KEY_ETH_TOGGLE), context);
		ethConfig.setEnabled(ethToggle.isChecked());
		ethProxy.setEnabled(ethToggle.isChecked());
        mEthConfigDialog = new EthernetConfigDialog(context, mEthEnabler, ethConfig);
        mEthEnabler.setConfigDialog(mEthConfigDialog);
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mEthEnabler.resume();
		getActivity().registerReceiver(mReceiver, intentFilter);
		mEthernetManager.addListener(mEthernetListener);
		syscEthInfo(mEthernetManager.isAvailable("eth0"));
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mEthEnabler.pause();
		getActivity().unregisterReceiver(mReceiver);
		mEthernetManager.removeListener(mEthernetListener);
	}
	
	// tankaikun add for resolving device reboot when frequently click docker charger swich. start 2018-09-01 	
	Handler handler = new Handler();
	Runnable runnable=new Runnable(){
	   	@Override
	   	public void run() {
			hostToggle.setEnabled(true);
	 	}
	};
	// add end
	
	public boolean onPreferenceTreeClick(Preference preference) {
        super.onPreferenceTreeClick(preference);
        if (preference == ethConfig) {
            mEthConfigDialog.show();
        } else if (preference == ethToggle) {
            boolean ethToggleEnabled = ethToggle.isChecked();
			SystemProperties.set("persist.sys.ethernet.mode", ethToggleEnabled ? "true" : "false");
			mEthernetManager.updateIface("eth0",ethToggleEnabled);
			Settings.System.putInt(getActivity().getContentResolver(), "sys.ethernet.switch", ethToggleEnabled ? 1 : 0);
        	syscWedigetStat();
        } else if (preference == hostToggle) {
			handler.postDelayed(runnable, 1000);	
			boolean hostToggleEnabled = hostToggle.isChecked();
			Settings.System.putInt(getActivity().getContentResolver(), "sys.hostkey.switch", hostToggleEnabled ? 1 : 0);
			SystemClock.sleep(100);
			mDockManager.setEnabled(hostToggleEnabled);
		} else if (preference == scanHandleToggle) {
			boolean scanvalue = scanHandleToggle.isChecked();
           	Settings.System.putInt(getActivity().getContentResolver(), "sys.scankey.switch", scanvalue ? 1:0);
			// TODO According to the needs of the project
           	if (scanvalue) {
				// TODO
           	} else {
           	    // TODO
           	}			
		}
        return false;
	}

	public void syscWedigetStat() {
		ethConfig.setEnabled(ethToggle.isChecked());
		ethProxy.setEnabled(ethToggle.isChecked());
	}
	
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			syscWedigetStat();
		}
	};

	private final EthernetManager.Listener mEthernetListener = new EthernetManager.Listener() {
		@Override
		public void onAvailabilityChanged(String iface, boolean isAvailable) {
			syscEthInfo(isAvailable);
		}
	};


	public void syscEthInfo(boolean isAvailable) {
		if (isAvailable) {
			String mac = "";
			try {
				mac = loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
			} catch (Exception e) {
				Log.e("DockerSettings", "read eth0 mac addr error");
			}
			if (!TextUtils.isEmpty(mac)) {
				ethInfoMac.setSummary(mac);
			}
		} else {
			ethInfoMac.setSummary(R.string.device_info_not_available);
		}
	}

	public static String loadFileAsString(String filePath) throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead=0;
		while ((numRead=reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}
		reader.close();
		return fileData.toString();
	}
	
    @Override
    public int getMetricsCategory() {
        return MetricsEvent.SETTINGS_SYSTEM_CATEGORY;
    }
	
}
