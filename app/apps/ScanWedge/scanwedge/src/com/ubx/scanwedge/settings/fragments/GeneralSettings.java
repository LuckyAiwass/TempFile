package com.ubx.scanwedge.settings.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.service.FxService;
import com.ubx.scanwedge.settings.utils.ULog;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class GeneralSettings extends SettingsPreferenceFragment {
    private static final String TAG = ULog.TAG + GeneralSettings.class.getSimpleName();

    private static final String USB_SCAN_ID_KEY = "/sys/devices/soc/qpnp-smbcharger-17/usbid_scankey";

    private static final String KEY_SCAN_VIRTUAL_BUTTON = "scanner_virtual";
    private static final String KEY_SCAN_HANDLE = "scan_handle_toggle";

    private PreferenceScreen root;
    private Activity mContext;

    @Override
    public void initPresenter() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();

        updatePreferences();
    }

    public void updatePreferences() {
        root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.scanner_settings);
        root = getPreferenceScreen();

        updateShowFloatBarPreference();

        updateHandleTogglePreference();

        setPreferenceScreen(root);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == showFloatBarPref) {
            updateShowFloatBarPreference();
        } else if (preference == handleTogglePref) {
            updateHandleTogglePreference();
        }
        return true;
    }

    /***/
    private SwitchPreference showFloatBarPref = null;

    private void updateShowFloatBarPreference() {
        if (root == null) return;

        if (root.findPreference(KEY_SCAN_VIRTUAL_BUTTON) == null) {
            showFloatBarPref = new SwitchPreference(mContext);
            showFloatBarPref.setKey(KEY_SCAN_VIRTUAL_BUTTON);
            showFloatBarPref.setTitle(R.string.scanner_virtual_button_title);
            showFloatBarPref.setSummary(R.string.scanner_virtual_button_summary);
            root.addPreference(showFloatBarPref);
        }

        updateShowFloatBarState();
    }

    private void updateShowFloatBarState() {
        if (showFloatBarPref.isChecked()) {
            Intent intent = new Intent(mContext, FxService.class);
            mContext.startService(intent);
        } else {
            Intent intent = new Intent(mContext, FxService.class);
            mContext.stopService(intent);
        }
        storyVirtualValue();
    }

    private void storyVirtualValue() {
        SharedPreferences sp = mContext.getSharedPreferences("virtual_button", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(KEY_SCAN_VIRTUAL_BUTTON, showFloatBarPref.isChecked());
        editor.commit();
    }

    /***/
    private SwitchPreference handleTogglePref = null;

    private void updateHandleTogglePreference() {
        if (root == null) return;

        if (root.findPreference(KEY_SCAN_HANDLE) == null) {
            handleTogglePref = new SwitchPreference(mContext);
            handleTogglePref.setKey(KEY_SCAN_HANDLE);
            handleTogglePref.setTitle(R.string.scanhandle_toggle_title);
            handleTogglePref.setSummaryOn(R.string.scanhandle_toggle_summary_enable);
            handleTogglePref.setSummaryOff(R.string.scanhandle_toggle_summary_disable);
            root.addPreference(handleTogglePref);
            handleTogglePref.setChecked(isScanHandleEnable());
        }

        setScanHandleEnabled(handleTogglePref.isChecked());
    }

    private void setScanHandleEnabled(boolean enabled) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(USB_SCAN_ID_KEY);
            outputStream.write(Integer.toString(enabled ? 1 : 0).getBytes());
            outputStream.flush();
        } catch (Exception e) {
            ULog.e(TAG, "setScanHandleEnabled() set ScanHandle status failed!" + e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isScanHandleEnable() {
        FileInputStream inputStream = null;
        boolean result = false;
        try {
            byte[] buffer = new byte[Integer.toString(0).getBytes().length];
            inputStream = new FileInputStream(USB_SCAN_ID_KEY);
            inputStream.read(buffer);
            if ("1".equals(new String(buffer)))
                result = true;
        } catch (Exception e) {
            ULog.e(TAG, "isScanHandleEnable() get ScanHandle status failed!" + e);
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}
