package com.ubx.scanwedge.settings.fragments;


import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.database.helper.USettings;

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
 * @Date: 20-1-7上午11:27
 */
public class SpecialSettingsFrament extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String KEY_SCAN_HANDLE = "handle_scan_key";
    private static final String KEY_WIRED_SCAN = "wired_scan";
    private PreferenceScreen root;
    private SwitchPreference scanHandleModePref = null;
    PreferenceCategory outputDelayPreferenceCategory;
    private Preference output_data_delay;
    private Preference keyevent_enter_delay;
    PreferenceCategory cachePreferenceCategory;
    private SwitchPreference scanner_cache_enable;
    private SwitchPreference scanner_cache_mode;
    private EditTextPreference scanner_cache_limit_time;
    private int scannerType;
    private int profileId;

    private ScanWedgeApplication mApplication;

    @Override
    public void initPresenter() {

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        scannerType = args != null ? args.getInt("scannertype") : 0;
        profileId = args != null ? args.getInt("profileId") : USettings.Profile.DEFAULT_ID;
        addPreferencesFromResource(R.xml.scanner_settings);
        root = this.getPreferenceScreen();
        mApplication = (ScanWedgeApplication) getActivity().getApplication();
    }

    private void updateActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            //actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowCustomEnabled(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.scanner_special_settings);
        if (root != null) {
            root.removeAll();
        }
        updateActionBar();
        updateHandlePreference();
        updateOutputDataDelayPreference();
        updateCacheOutputDataPreference();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == scanHandleModePref) {
            mApplication.setScanHandleEnabled(scanHandleModePref.isChecked());
            mApplication.setPropertyInt(profileId, PropertyID.SCAN_HANDLE, Settings.System.SCAN_HANDLE, scanHandleModePref.isChecked() ? 1 : 0);
        } else if (preference == wiredScanPref) {
            //int value = Integer.parseInt((String) o);
            //setPropertyInt(PropertyID.SCANNER_TYPE, value);
            /*mScanManager.setOutputParameter(7, value);
            wiredScanPref.setSummary(wiredScanPref.getEntries()[value / 3 - 1]);
            mScanManager.closeScanner();
            new UpdateAsyncTask(mContext).execute("update");*/
        } else if (output_data_delay == preference) {
            showEditDialog(R.string.output_data_character_delay, PropertyID.CHARACTER_DATA_DELAY, Settings.System.CHARACTER_DATA_DELAY, 0);
        } else if (keyevent_enter_delay == preference) {
            showEditDialog(R.string.keyevent_enter_delay, PropertyID.APPEND_ENTER_DELAY, Settings.System.APPEND_ENTER_DELAY, 0);
        } else if(preference == scanner_cache_limit_time) {
            scanner_cache_limit_time.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            scanner_cache_limit_time.getEditText().setText(scanner_cache_limit_time.getSummary());
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference == scanner_cache_limit_time) {
            if(newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            int time = Integer.parseInt(value);
            if(time <= 0) {
                Toast.makeText(getActivity(), "请输入大于0的数值!", Toast.LENGTH_LONG).show();
            } else {
                scanner_cache_limit_time.setSummary("" + value);
                mApplication.setPropertyInt(profileId, PropertyID.CACHE_DATA_LIMIT_TIME, Settings.System.CACHE_DATA_LIMIT_TIME, time);
            }
        }
        return false;
    }

    /**
     * 手柄
     */
    private void updateHandlePreference() {
        if (root == null) return;

        if (mApplication.supportHandle() && false) {
            if (root.findPreference(KEY_SCAN_HANDLE) == null) {
                scanHandleModePref = new SwitchPreference(getActivity());
                scanHandleModePref.setKey(KEY_SCAN_HANDLE);
                scanHandleModePref.setTitle(R.string.scanhandle_toggle_title);
                scanHandleModePref.setSummaryOn(R.string.scanhandle_toggle_summary_enable);
                scanHandleModePref.setSummaryOff(R.string.scanhandle_toggle_summary_disable);
                root.addPreference(scanHandleModePref);
                boolean isHandleMode = mApplication.getPropertyInt(profileId, Settings.System.SCAN_HANDLE, 0) == 1;
                if (isHandleMode) {
                    if (mApplication.isScanHandleEnable() == false) {
                        mApplication.setScanHandleEnabled(true);
                        mApplication.setPropertyInt(profileId, PropertyID.SCAN_HANDLE, Settings.System.SCAN_HANDLE, 1);
                    }
                } else {
                    mApplication.setScanHandleEnabled(false);
                    mApplication.setPropertyInt(profileId, PropertyID.SCAN_HANDLE, Settings.System.SCAN_HANDLE, 0);
                }
                scanHandleModePref.setChecked(isHandleMode);
            }
        } else {
            if (root.findPreference(KEY_SCAN_HANDLE) != null) {
                root.removePreference(scanHandleModePref);
            }
        }
    }

    /**
     * 指环选项
     */
    private ListPreference wiredScanPref = null;
    private void updateWiredScanPreference() {
        if (root == null) return;

        if (root.findPreference(KEY_WIRED_SCAN) == null) {
            wiredScanPref = new ListPreference(getActivity());
            wiredScanPref.setKey(KEY_WIRED_SCAN);
            wiredScanPref.setTitle(R.string.wired_scan);
            wiredScanPref.setEntries(R.array.wired_scan_entries);
            wiredScanPref.setEntryValues(R.array.wired_scan_values);
            wiredScanPref.setOnPreferenceChangeListener(this);
            root.addPreference(wiredScanPref);
        }

        updateWiredScanState();
    }

    private void updateWiredScanState() {
        if (scannerType == 3 || scannerType == 6) {
            int index = scannerType / 3 - 1;
            wiredScanPref.setValue(wiredScanPref.getEntryValues()[index].toString());
            wiredScanPref.setSummary(wiredScanPref.getEntries()[index].toString());
        } else {
            wiredScanPref.setSummary("other");
        }
    }

    private void updateOutputDataDelayPreference() {
        if (root.findPreference("outputDelayPreferenceCategory") == null) {
            outputDelayPreferenceCategory = new PreferenceCategory(getActivity());
            outputDelayPreferenceCategory.setKey("outputDelayPreferenceCategory");
            outputDelayPreferenceCategory.setTitle(R.string.output_data_delay);
            root.addPreference(outputDelayPreferenceCategory);
            output_data_delay = new Preference(getActivity());
            output_data_delay.setKey("output_data_character_delay");
            output_data_delay.setTitle(R.string.output_data_character_delay);
            outputDelayPreferenceCategory.addPreference(output_data_delay);
            int delayTime = mApplication.getPropertyInt(profileId, Settings.System.CHARACTER_DATA_DELAY, 0);
            updateSummay(output_data_delay, delayTime);
            keyevent_enter_delay = new Preference(getActivity());
            keyevent_enter_delay.setKey("keyevent_enter_delay");
            keyevent_enter_delay.setTitle(R.string.keyevent_enter_delay);
            outputDelayPreferenceCategory.addPreference(keyevent_enter_delay);
            delayTime = mApplication.getPropertyInt(profileId, Settings.System.APPEND_ENTER_DELAY, 0);
            updateSummay(keyevent_enter_delay, delayTime);
        }
    }
    private void updateCacheOutputDataPreference() {
        if (root.findPreference("updateCacheOutputDataPreference") == null) {
            cachePreferenceCategory = new PreferenceCategory(getActivity());
            cachePreferenceCategory.setKey("updateCacheOutputDataPreference");
            cachePreferenceCategory.setTitle(R.string.scanner_cache_config);
            root.addPreference(cachePreferenceCategory);

            scanner_cache_enable = new SwitchPreference(getActivity());
            scanner_cache_enable.setKey("scanner_cache_enable");
            scanner_cache_enable.setTitle(R.string.scanner_cache_enable);
            cachePreferenceCategory.addPreference(scanner_cache_enable);
            int enable = mApplication.getPropertyInt(profileId, Settings.System.CACHE_DATA_ENABLE, 0);
            scanner_cache_enable.setChecked(enable == 1);

            scanner_cache_mode = new SwitchPreference(getActivity());
            scanner_cache_mode.setKey("scanner_cache_mode");
            scanner_cache_mode.setTitle(R.string.scanner_cache_mode);
            cachePreferenceCategory.addPreference(scanner_cache_mode);
            enable = mApplication.getPropertyInt(profileId, Settings.System.CACHE_DATA_LIMIT_ENABLE, 0);
            scanner_cache_mode.setChecked(enable == 1);

            scanner_cache_limit_time = new EditTextPreference(getActivity());
            scanner_cache_limit_time.setKey("scanner_cache_limit_time");
            scanner_cache_limit_time.setTitle(R.string.scanner_cache_limit_time);
            scanner_cache_limit_time.setOnPreferenceChangeListener(this);
            cachePreferenceCategory.addPreference(scanner_cache_limit_time);
            int delayTime = mApplication.getPropertyInt(profileId, Settings.System.CACHE_DATA_LIMIT_TIME, 3000);
            updateSummay(scanner_cache_limit_time, delayTime);
        }
    }
    private void updateSummay(Preference pref, int value) {
        String timeout = String.format(getActivity().getString(R.string.scanner_laser_timeout_summary), value);
        pref.setSummary(timeout);
    }

    private Dialog mMaxMinDialog;
    private int mProgress;
    private void showEditDialog(final int title, final int id, final String propertyName, final int minVal) {
        if (mMaxMinDialog != null) mMaxMinDialog.dismiss();
        mProgress = mApplication.getPropertyInt(profileId, propertyName, 0);
        if (mProgress < 0) mProgress = 0;
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mMaxMinDialog = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setView(editText)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String num = editText.getText().toString().trim();
                        try {
                            mProgress = Integer.valueOf("".equals(num) ? "0" : num);
                        } catch (NumberFormatException e) {
                            /*Toast.makeText(getActivity(),
                                    R.string.unknown_input, Toast.LENGTH_LONG).show();*/
                            e.printStackTrace();
                            return;
                        }
                        if (title == R.string.keyevent_enter_delay) {
                            mProgress = mProgress > 1000 ? 1000 : mProgress;
                            mApplication.setPropertyInt(profileId, id, propertyName, mProgress);
                            updateSummay(keyevent_enter_delay, mProgress);
                        } else if (title == R.string.output_data_character_delay) {
                            if (Build.PWV_CUSTOM_CUSTOM.equals("QCS"))
                                mProgress = mProgress > 100 ? 100 : mProgress;
                            else
                                mProgress = mProgress > 1000 ? 1000 : mProgress;
                            mApplication.setPropertyInt(profileId, id, propertyName, mProgress);
                            updateSummay(output_data_delay, mProgress);
                        }
                    }
                }).show();
    }
}
