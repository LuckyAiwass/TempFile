package com.ubx.scanwedge.settings.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
 * @Date: 20-2-25上午11:07
 */
public class ScanFeedbackFrament extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String KEY_SCAN_SOUNDS = "scanner_beep";
    private static final String KEY_SCAN_VIBRATE = "scanner_vibrate";
    private ListPreference keyboardBeepPref = null;
    private ListPreference feedbackBeepChannelPref = null;
    private Preference feedbackBeepCustomPref = null;
    private SwitchPreference keyboardVibratePref = null;
    private PreferenceScreen root;
    private int scannerType;
    private int mProfileId;
    private ScanWedgeApplication mApplication;
    private ContentResolver mContentResolver;

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
    public void initPresenter() {

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
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        scannerType = args != null ? args.getInt("type") : 0;
        mProfileId = args != null ? args.getInt("profileId") : USettings.Profile.DEFAULT_ID;
        addPreferencesFromResource(R.xml.scanner_settings);
        root = this.getPreferenceScreen();
        mApplication = (ScanWedgeApplication) getActivity().getApplication();
        mContentResolver = getContentResolver();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        getActivity().setTitle(R.string.scanner_decode_feedback);
        updateActionBar();
        if (root != null) {
            root.removeAll();
        }
        updateDecodeFeedback();
        decodeVibrateFeedback();
    }
    /**
     * 配置扫描声音
     */
    public void updateDecodeFeedback(){
        if (root == null) return;
        if (root.findPreference(KEY_SCAN_SOUNDS) == null) {
            keyboardBeepPref = new ListPreference(getActivity());
            keyboardBeepPref.setKey(KEY_SCAN_SOUNDS);
            keyboardBeepPref.setTitle(R.string.scanner_beep);
            keyboardBeepPref.setEntries(R.array.scanner_beep_entries);
            keyboardBeepPref.setEntryValues(R.array.image_inverse_decoder_values);
            keyboardBeepPref.setOnPreferenceChangeListener(this);
            root.addPreference(keyboardBeepPref);
            int beepIndex = mApplication.getPropertyInt(mProfileId, Settings.System.GOOD_READ_BEEP_ENABLE, 2);
            keyboardBeepPref.setValue(String.valueOf(beepIndex));
            keyboardBeepPref.setSummary(keyboardBeepPref.getEntries()[beepIndex]);
        }
        /*if(null == root.findPreference(KEY_SCAN_CUSTOM_SOUNDS)) {
            feedbackBeepCustomPref = new Preference(mContext);
            feedbackBeepCustomPref.setKey(KEY_SCAN_CUSTOM_SOUNDS);
            root.addPreference(feedbackBeepCustomPref);
        }
        if(null == root.findPreference(KEY_SCAN_SOUNDS_CHANNEL)) {
            feedbackBeepChannelPref = new ListPreference(mContext);
            feedbackBeepChannelPref.setKey(KEY_SCAN_SOUNDS_CHANNEL);
            feedbackBeepChannelPref.setEntries(R.array.scanner_beep_channel_entries);
            feedbackBeepChannelPref.setEntryValues(R.array.scanner_beep_channel_values);
            feedbackBeepChannelPref.setOnPreferenceChangeListener(this);
            root.addPreference(feedbackBeepChannelPref);
        }*/
    }
    public void decodeVibrateFeedback(){
        if (root == null) return;
        if (root.findPreference(KEY_SCAN_VIBRATE) == null) {
            keyboardVibratePref = new SwitchPreference(getActivity());
            keyboardVibratePref.setKey(KEY_SCAN_VIBRATE);
            keyboardVibratePref.setTitle(R.string.scanner_vibrate);
            keyboardVibratePref.setSummary(R.string.scanner_vibrate_summary);
            root.addPreference(keyboardVibratePref);
            boolean isVibrate = mApplication.getPropertyInt(mProfileId, Settings.System.GOOD_READ_VIBRATE_ENABLE, 0) == 1;
            keyboardVibratePref.setChecked(isVibrate);
        }

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        try {
            if (preference == keyboardVibratePref) {
                mApplication.setPropertyInt(mProfileId, PropertyID.GOOD_READ_VIBRATE_ENABLE, Settings.System.GOOD_READ_VIBRATE_ENABLE, keyboardVibratePref.isChecked() ? 1 : 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        String key = preference.getKey();
        //android.util.Log.i("debug", "onPreferenceChange==================" + key);
        if (key == null) return false;
        if (preference == keyboardBeepPref) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.GOOD_READ_BEEP_ENABLE, Settings.System.GOOD_READ_BEEP_ENABLE, value);
            mApplication.setPropertyInt(mProfileId, PropertyID.SEND_GOOD_READ_BEEP_ENABLE, Settings.System.SEND_GOOD_READ_BEEP_ENABLE, value);
            keyboardBeepPref.setValue(String.valueOf(value));
            keyboardBeepPref.setSummary(keyboardBeepPref.getEntries()[value]);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showAlertToast(int min, int max) {
        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_symbology_length_range), min, max), Toast.LENGTH_LONG).show();
    }
}
