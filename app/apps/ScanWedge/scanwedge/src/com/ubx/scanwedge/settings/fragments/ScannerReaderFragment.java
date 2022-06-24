package com.ubx.scanwedge.settings.fragments;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.scanwedge.settings.utils.ScannerAdapter;
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
 * @Date: 20-1-14下午5:05
 */
public class ScannerReaderFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "ScannerReaderParams";
    //Formating None
    private static final String NONE = "None";
    //trigger
    private static final String KEY_CODING_FORMAT = "image_coding_format";
    private static final String KEY_LASER_ON_TIME = "laser_on_time";
    private static final String KEY_TIMEOUT_SAME_SYMBOL = "timeout_same_symbol";
    private static final String KEY_LINER_SECURITY_LEVEL = "linear_code_type_security_level";
    private static final String KEY_1D_QUIET_ZONE_LEVEL = "key_1d_quiet_zone_level";

    PreferenceCategory mScannerFormat;
    PreferenceCategory mTriggerReading;
    PreferenceCategory m1DSpecial;

    private SwitchPreference mFuzzy_1d_processing;
    private ListPreference mImage_inverse_decoder;
    PreferenceCategory mMultiDecode;
    private ListPreference mImage_bar_cades_to_read;
    private SwitchPreference mFull_read_mode;
    private SwitchPreference mMulti_decode_mode;

    private ListPreference mLinearSecurityLevel;
    private ListPreference m1DQuietZoneLevel;
    private Preference mLinerTimeoutSym;
    private Preference mLaserOnTime;
    private Preference decIllumPowerLevel;
    private SwitchPreference decPicklistAimMode;
    private Preference dec_aim_mode_delay;

    private ListPreference mSendCodeId;
    private Preference mLablePrefix;
    private Preference mLableSuffix;
    private Preference mLablePattern;
    private Preference mLableReplace;
    String replaceRegex = "";
    String replaceMent = "";
    private CheckBoxPreference mRemoveNonprintchar;
    private CheckBoxPreference mScanSeparator;
    private Preference edit_lable_separator;

    private PreferenceCategory mExposure;
    private ListPreference exposureMode;
    private EditTextPreference fixedExposureLevel;
    private PreferenceCategory n6603_decoce_windowing;
    ListPreference n6603_lightsConfig;
    SwitchPreference n6603_dec_centering_enable;
    ListPreference decode_centering_mode;
    EditTextPreference decode_window_upper_left_x;
    EditTextPreference decode_window_upper_left_y;
    EditTextPreference decode_window_lower_right_x;
    EditTextPreference decode_window_lower_right_y;
    CheckBoxPreference decode_debug_window_enable;

    private PreferenceScreen root;
    private int setType;
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

        root = this.getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        Bundle args = getArguments();
        setType = args != null ? args.getInt("type") : 0;
        scannerType = args != null ? args.getInt("scannertype") : 0;
        mProfileId = args != null ? args.getInt("profileId") : USettings.Profile.DEFAULT_ID;
        addPreferencesFromResource(R.xml.scanner_settings);
        root = this.getPreferenceScreen();
        mContentResolver = getContentResolver();
        mApplication = (ScanWedgeApplication) getActivity().getApplication();
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
        getActivity().setTitle(R.string.scanner_trigger_reading);
        if (root != null) {
            root.removeAll();
        }
        updateActionBar();
        updateState();
    }
    private void initEditText(EditTextPreference keyEditText, boolean number, String hintText){

        keyEditText.getEditText().setInputType( number ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
        keyEditText.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(number ? 4 : 1)});
        keyEditText.getEditText().setHint(hintText);
        keyEditText.setOnPreferenceChangeListener(this);

    }
    private void initEditText(EditTextPreference keyEditText, boolean number) {
        keyEditText.getEditText().setInputType(number ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
        keyEditText.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(number ? 4 : 1)});
        keyEditText.getEditText().setHint("1~7848");
        keyEditText.setOnPreferenceChangeListener(this);

    }
    private void showAlertToast(int min, int max) {
        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_symbology_length_range), String.valueOf(min), String.valueOf(max)), Toast.LENGTH_LONG).show();
    }
    @SuppressLint("StringFormatMatches")
    private void updateSummay(Preference pref, int value) {
        String timeout = String.format(getActivity().getString(R.string.scanner_laser_timeout_summary), value * 100);
        pref.setSummary(timeout);
    }

    private void updateState() {
        updateImgCodingFormatPreference();
        if(ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
            updateZebarScannerPrefrence();
        }
        if(ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
            updateHoneywellScannerPrefrence();
        }
    }
    /**
     * 扫描编码类型
     */
    private ListPreference imgCodingFormatPref = null;
    private void updateImgCodingFormatPreference() {
        if (root == null) return;
        if (root.findPreference(KEY_CODING_FORMAT) == null) {
            imgCodingFormatPref = new ListPreference(getActivity());
            imgCodingFormatPref.setKey(KEY_CODING_FORMAT);
            imgCodingFormatPref.setTitle(R.string.scanner_coding_format);
            imgCodingFormatPref.setEntries(R.array.image_coding_format_entries);
            imgCodingFormatPref.setEntryValues(R.array.image_coding_format_values);
            imgCodingFormatPref.setOnPreferenceChangeListener(this);
            root.addPreference(imgCodingFormatPref);
        }
        updateImgCodingFormatState();
    }

    private void updateImgCodingFormatState() {
        int codeFormat = mApplication.getPropertyInt(mProfileId, Settings.System.CODING_FORMAT, 0);
        imgCodingFormatPref.setValue(String.valueOf(codeFormat));
        imgCodingFormatPref.setSummary(imgCodingFormatPref.getEntry());
    }
    /**
     * 手机模式
     */
    private SwitchPreference scanPhoneModePref = null;
    private void updatePhoneModePreference() {
        if (root == null) return;
        if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_LCDMODE_SCAN)) {
            if (root.findPreference(ScannerAdapter.KEY_LCDMODE_SCAN) == null) {
                scanPhoneModePref = new SwitchPreference(getActivity());
                scanPhoneModePref.setKey(ScannerAdapter.KEY_LCDMODE_SCAN);
                scanPhoneModePref.setTitle(R.string.phonemode_title);
                scanPhoneModePref.setSummary(R.string.phonemode_summary);
                root.addPreference(scanPhoneModePref);
            }
            boolean isPhoneMode = mApplication.getPropertyInt(mProfileId, Settings.System.IMAGE_PICKLIST_MODE, 0) == 1;
            scanPhoneModePref.setChecked(isPhoneMode);
        } else {
            if (root.findPreference(ScannerAdapter.KEY_LCDMODE_SCAN) != null) {
                root.removePreference(scanPhoneModePref);
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * ZEBAR Scanner Engine
     */
    private void updateZebarScannerPrefrence() {
        if (root.findPreference(KEY_LASER_ON_TIME) == null) {
            mLaserOnTime = new Preference(getActivity());
            mLaserOnTime.setKey(KEY_LASER_ON_TIME);
            mLaserOnTime.setTitle(R.string.scanner_laser_on_time);
            root.addPreference(mLaserOnTime);
        }
        int value = 0;
        if(mLaserOnTime != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.LASER_ON_TIME, 50);
            updateSummay(mLaserOnTime, value);
        }
        if (root.findPreference(KEY_TIMEOUT_SAME_SYMBOL) == null) {
            mLinerTimeoutSym = new Preference(getActivity());
            mLinerTimeoutSym.setKey(KEY_TIMEOUT_SAME_SYMBOL);
            mLinerTimeoutSym.setTitle(R.string.scanner_timeout_same_symbol);
            root.addPreference(mLinerTimeoutSym);
        }
        if(mLinerTimeoutSym != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.TIMEOUT_BETWEEN_SAME_SYMBOL, 10);
            updateSummay(mLinerTimeoutSym, value);
        }
        if (root.findPreference(KEY_1D_QUIET_ZONE_LEVEL) == null) {
            m1DQuietZoneLevel = new ListPreference(getActivity());
            m1DQuietZoneLevel.setKey(KEY_1D_QUIET_ZONE_LEVEL);
            m1DQuietZoneLevel.setTitle(R.string.scanner_1d_quiet_zone_level);
            m1DQuietZoneLevel.setEntries(R.array.linear_1d_quiet_zone_level);
            m1DQuietZoneLevel.setEntryValues(R.array.linear_security_level_values);
            m1DQuietZoneLevel.setOnPreferenceChangeListener(this);
            root.addPreference(m1DQuietZoneLevel);
        }
        if(m1DQuietZoneLevel != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.LINEAR_1D_QUIET_ZONE_LEVEL, 1);
            m1DQuietZoneLevel.setValue(String.valueOf(value));
            m1DQuietZoneLevel.setSummary(m1DQuietZoneLevel.getEntry());
        }
        if (root.findPreference(KEY_LINER_SECURITY_LEVEL) == null) {
            mLinearSecurityLevel = new ListPreference(getActivity());
            mLinearSecurityLevel.setKey(KEY_LINER_SECURITY_LEVEL);
            mLinearSecurityLevel.setTitle(R.string.scanner_type_security_level);
            mLinearSecurityLevel.setEntries(R.array.linear_security_level_entries);
            mLinearSecurityLevel.setEntryValues(R.array.linear_security_level_values);
            mLinearSecurityLevel.setOnPreferenceChangeListener(this);
            root.addPreference(mLinearSecurityLevel);
        }
        if(mLinearSecurityLevel != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.LINEAR_CODE_TYPE_SECURITY_LEVEL, 2);
            mLinearSecurityLevel.setValue(String.valueOf(value - 1));
            mLinearSecurityLevel.setSummary(mLinearSecurityLevel.getEntry());
        }
        if(root.findPreference(ScannerAdapter.KEY_1D_SPECIAL_PREFERENCE) == null) {
            m1DSpecial = new PreferenceCategory(getActivity());
            m1DSpecial.setKey(ScannerAdapter.KEY_1D_SPECIAL_PREFERENCE);
            m1DSpecial.setTitle(R.string.scanner_1d_special);
            root.addPreference(m1DSpecial);

            mFuzzy_1d_processing = new SwitchPreference(getActivity());
            mFuzzy_1d_processing.setKey(Settings.System.FUZZY_1D_PROCESSING);
            mFuzzy_1d_processing.setTitle(R.string.scanner_fuzzy_1d_processing);
            m1DSpecial.addPreference(mFuzzy_1d_processing);

            mImage_inverse_decoder = new ListPreference(getActivity());
            mImage_inverse_decoder.setKey(Settings.System.IMAGE_ONE_D_INVERSE);
            mImage_inverse_decoder.setTitle(R.string.scanner_1d_inverse);
            mImage_inverse_decoder.setEntries(R.array.image_inverse_decoder_entries);
            mImage_inverse_decoder.setEntryValues(R.array.image_inverse_decoder_values);
            mImage_inverse_decoder.setOnPreferenceChangeListener(this);
            m1DSpecial.addPreference(mImage_inverse_decoder);
        }
        if(mFuzzy_1d_processing != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.FUZZY_1D_PROCESSING, 1);
            mFuzzy_1d_processing.setChecked(value == 1);
        }
        if(mFuzzy_1d_processing != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.IMAGE_ONE_D_INVERSE, 0);
            mImage_inverse_decoder.setValue(String.valueOf(value));
            mImage_inverse_decoder.setSummary(mImage_inverse_decoder.getEntry());
        }
        updatePhoneModePreference();
        if(root.findPreference(ScannerAdapter.KEY_EXPOSURE_MODE_PREFERENCE) == null) {
            mExposure = new PreferenceCategory(getActivity());
            mExposure.setKey(ScannerAdapter.KEY_EXPOSURE_MODE_PREFERENCE);
            mExposure.setTitle(R.string.scanner_exposure_setting);
            root.addPreference(mExposure);

            n6603_lightsConfig = new ListPreference(getActivity());
            n6603_lightsConfig.setKey(Settings.System.DEC_2D_LIGHTS_MODE);
            n6603_lightsConfig.setTitle(R.string.n6603_title_lights_config);
            n6603_lightsConfig.setEntries(R.array.n6603_lights_config_values_titles);
            n6603_lightsConfig.setEntryValues(R.array.n6603_lights_config_values);
            n6603_lightsConfig.setOnPreferenceChangeListener(this);
            mExposure.addPreference(n6603_lightsConfig);

            decIllumPowerLevel = new Preference(getActivity());
            decIllumPowerLevel.setKey(Settings.System.DEC_ILLUM_POWER_LEVEL);
            decIllumPowerLevel.setTitle(R.string.se4500_illum_power_level);
            mExposure.addPreference(decIllumPowerLevel);
        }
        if(root.findPreference(Settings.System.DEC_PICKLIST_AIM_MODE) == null) {
            decPicklistAimMode = new SwitchPreference(getActivity());
            decPicklistAimMode.setKey(Settings.System.DEC_PICKLIST_AIM_MODE);
            decPicklistAimMode.setTitle(R.string.se4500_dec_picklist_aim_mode);
            root.addPreference(decPicklistAimMode);
        }
        int lightsConfig = 3;
        if(n6603_lightsConfig != null) {
            lightsConfig = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_2D_LIGHTS_MODE, 3);
            if (lightsConfig < 0) lightsConfig = 3;
            if (n6603_lightsConfig != null) {
                n6603_lightsConfig.setValue(String.valueOf(lightsConfig));
                n6603_lightsConfig.setSummary(n6603_lightsConfig.getEntry());
            }
        }
        if(decIllumPowerLevel != null) {
            int level = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_ILLUM_POWER_LEVEL, 10);
            decIllumPowerLevel.setSummary("" + level);
            if (lightsConfig >= 2) {
                decIllumPowerLevel.setEnabled(true);
            } else {
                decIllumPowerLevel.setEnabled(false);
            }
        }
        if(decPicklistAimMode != null) {
            boolean isChecked = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_PICKLIST_AIM_MODE, 0) == 1;
            decPicklistAimMode.setChecked(isChecked);
            if (lightsConfig == 1 || 3 == lightsConfig || 4 == lightsConfig) {
                if (decPicklistAimMode != null) {
                    decPicklistAimMode.setEnabled(true);
                }
            } else {
                if (decPicklistAimMode != null) {
                    decPicklistAimMode.setEnabled(false);
                }
            }
        }
    }
    private void updateMultiDecodePreference() {
        if(root.findPreference(ScannerAdapter.KEY_MULTI_DECODE_MODE_PREFERENCE) == null) {
            mMultiDecode = new PreferenceCategory(getActivity());
            mMultiDecode.setKey(ScannerAdapter.KEY_MULTI_DECODE_MODE_PREFERENCE);
            mMultiDecode.setTitle(R.string.scanner_multi_decode);
            root.addPreference(mMultiDecode);

            mMulti_decode_mode = new SwitchPreference(getActivity());
            mMulti_decode_mode.setKey(Settings.System.MULTI_DECODE_MODE);
            mMulti_decode_mode.setTitle(R.string.scanner_multi_decode_mode);
            mMultiDecode.addPreference(mMulti_decode_mode);
            mImage_bar_cades_to_read = new ListPreference(getActivity());
            mImage_bar_cades_to_read.setKey(Settings.System.BAR_CODES_TO_READ);
            mImage_bar_cades_to_read.setTitle(R.string.scanner_bar_codes_to_read);
            mImage_bar_cades_to_read.setEntries(R.array.image_bar_cades_to_read_entries);
            mImage_bar_cades_to_read.setEntryValues(R.array.image_bar_cades_to_read_values);
            mImage_bar_cades_to_read.setOnPreferenceChangeListener(this);
            mMultiDecode.addPreference(mImage_bar_cades_to_read);

            mFull_read_mode = new SwitchPreference(getActivity());
            mFull_read_mode.setKey(Settings.System.FULL_READ_MODE);
            mFull_read_mode.setTitle(R.string.scanner_full_read_mode);
            mMultiDecode.addPreference(mFull_read_mode);
        }
        int value = mApplication.getPropertyInt(mProfileId, Settings.System.MULTI_DECODE_MODE, 0);
        mMulti_decode_mode.setChecked(value == 1);
        value = mApplication.getPropertyInt(mProfileId, Settings.System.BAR_CODES_TO_READ, 1);
        mImage_bar_cades_to_read.setValue(String.valueOf(value - 1));
        mImage_bar_cades_to_read.setSummary(mImage_bar_cades_to_read.getEntry());
        value = mApplication.getPropertyInt(mProfileId, Settings.System.FULL_READ_MODE, 1);
        mFull_read_mode.setChecked(value == 1);
        mImage_bar_cades_to_read.setEnabled(mMulti_decode_mode.isChecked());
        mFull_read_mode.setEnabled(mMulti_decode_mode.isChecked());
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Honeywell scanner engine
     */
    private void updateHoneywellScannerPrefrence() {
        if (root.findPreference(KEY_LASER_ON_TIME) == null) {
            mLaserOnTime = new Preference(getActivity());
            mLaserOnTime.setKey(KEY_LASER_ON_TIME);
            mLaserOnTime.setTitle(R.string.scanner_laser_on_time);
            root.addPreference(mLaserOnTime);
        }
        int value = 50;
        if(mLaserOnTime != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.LASER_ON_TIME, 50);
            updateSummay(mLaserOnTime, value);
        }
        if (root.findPreference(KEY_LINER_SECURITY_LEVEL) == null) {
            mLinearSecurityLevel = new ListPreference(getActivity());
            mLinearSecurityLevel.setKey(KEY_LINER_SECURITY_LEVEL);
            mLinearSecurityLevel.setTitle(R.string.scanner_type_security_level);
            mLinearSecurityLevel.setEntries(R.array.linear_security_level_entries);
            mLinearSecurityLevel.setEntryValues(R.array.linear_security_level_values);
            mLinearSecurityLevel.setOnPreferenceChangeListener(this);
            root.addPreference(mLinearSecurityLevel);
        }
        if(mLinearSecurityLevel != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.LINEAR_CODE_TYPE_SECURITY_LEVEL, 2);
            mLinearSecurityLevel.setValue(String.valueOf(value - 1));
            mLinearSecurityLevel.setSummary(mLinearSecurityLevel.getEntry());
        }

        if(ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_1D_SPECIAL_PREFERENCE) && root.findPreference(ScannerAdapter.KEY_1D_SPECIAL_PREFERENCE) == null) {
            m1DSpecial = new PreferenceCategory(getActivity());
            m1DSpecial.setKey(ScannerAdapter.KEY_1D_SPECIAL_PREFERENCE);
            m1DSpecial.setTitle(R.string.scanner_1d_special);
            root.addPreference(m1DSpecial);

            mFuzzy_1d_processing = new SwitchPreference(getActivity());
            mFuzzy_1d_processing.setKey(Settings.System.FUZZY_1D_PROCESSING);
            mFuzzy_1d_processing.setTitle(R.string.scanner_fuzzy_1d_processing);
            m1DSpecial.addPreference(mFuzzy_1d_processing);

            mImage_inverse_decoder = new ListPreference(getActivity());
            mImage_inverse_decoder.setKey(Settings.System.IMAGE_ONE_D_INVERSE);
            mImage_inverse_decoder.setTitle(R.string.scanner_1d_inverse);
            mImage_inverse_decoder.setEntries(R.array.image_inverse_decoder_entries);
            mImage_inverse_decoder.setEntryValues(R.array.image_inverse_decoder_values);
            mImage_inverse_decoder.setOnPreferenceChangeListener(this);
            //m1DSpecial.addPreference(mImage_inverse_decoder);
        }
        if(mFuzzy_1d_processing != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.FUZZY_1D_PROCESSING, 1);
            mFuzzy_1d_processing.setChecked(value == 1);
        }
        if(mImage_inverse_decoder != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.IMAGE_ONE_D_INVERSE, 0);
            mImage_inverse_decoder.setValue(String.valueOf(value));
            mImage_inverse_decoder.setSummary(mImage_inverse_decoder.getEntry());
        }
        //updatePhoneModePreference();
        if(root.findPreference(ScannerAdapter.KEY_EXPOSURE_MODE_PREFERENCE) == null) {
            mExposure = new PreferenceCategory(getActivity());
            mExposure.setKey(ScannerAdapter.KEY_EXPOSURE_MODE_PREFERENCE);
            mExposure.setTitle(R.string.scanner_exposure_setting);
            root.addPreference(mExposure);

            n6603_lightsConfig = new ListPreference(getActivity());
            n6603_lightsConfig.setKey(Settings.System.DEC_2D_LIGHTS_MODE);
            n6603_lightsConfig.setTitle(R.string.n6603_title_lights_config);
            n6603_lightsConfig.setEntries(R.array.n6603_lights_config_values_titles);
            n6603_lightsConfig.setEntryValues(R.array.n6603_lights_config_values);
            n6603_lightsConfig.setOnPreferenceChangeListener(this);
            mExposure.addPreference(n6603_lightsConfig);
            if(ScannerAdapter.isSupportPreference(scannerType, Settings.System.DEC_ILLUM_POWER_LEVEL) && root.findPreference(Settings.System.DEC_ILLUM_POWER_LEVEL) == null) {
                decIllumPowerLevel = new Preference(getActivity());
                decIllumPowerLevel.setKey(Settings.System.DEC_ILLUM_POWER_LEVEL);
                decIllumPowerLevel.setTitle(R.string.se4500_illum_power_level);
                mExposure.addPreference(decIllumPowerLevel);
            }
            if(ScannerAdapter.isSupportPreference(scannerType, Settings.System.IMAGE_EXPOSURE_MODE) && root.findPreference(Settings.System.IMAGE_EXPOSURE_MODE) == null) {
                exposureMode = new ListPreference(getActivity());
                exposureMode.setKey(Settings.System.IMAGE_EXPOSURE_MODE);
                exposureMode.setTitle(R.string.scanner_exposure_mode);
                exposureMode.setEntries(R.array.scanner_exposure_mode_entries);
                exposureMode.setEntryValues(R.array.scanner_exposure_mode_values);
                exposureMode.setOnPreferenceChangeListener(this);
                mExposure.addPreference(exposureMode);
                fixedExposureLevel = new EditTextPreference(getActivity());
                fixedExposureLevel.setKey(Settings.System.IMAGE_FIXED_EXPOSURE);
                fixedExposureLevel.setTitle(R.string.scanner_exposure_fixed_title);
                mExposure.addPreference(fixedExposureLevel);
            }
        }
        if(ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_DEC_DecodeWindowLimits) && root.findPreference(ScannerAdapter.KEY_DEC_DecodeWindowLimits) == null) {
            n6603_decoce_windowing = new PreferenceCategory(getActivity());
            n6603_decoce_windowing.setKey(ScannerAdapter.KEY_DEC_DecodeWindowLimits);
            n6603_decoce_windowing.setTitle(R.string.n6603_decoce_windowing);
            root.addPreference(n6603_decoce_windowing);
            n6603_dec_centering_enable = new SwitchPreference(getActivity());
            n6603_dec_centering_enable.setKey(Settings.System.DEC_2D_CENTERING_ENABLE);
            n6603_dec_centering_enable.setTitle(R.string.n6603_configure_windowing);
            n6603_decoce_windowing.addPreference(n6603_dec_centering_enable);
            decode_centering_mode = new ListPreference(getActivity());
            decode_centering_mode.setKey(Settings.System.DEC_2D_CENTERING_MODE);
            decode_centering_mode.setTitle(R.string.n6603_centering_mode_title);
            decode_centering_mode.setEntries(R.array.n6603_centering_mode_entries);
            decode_centering_mode.setEntryValues(R.array.n6603_centering_mode_values);
            decode_centering_mode.setOnPreferenceChangeListener(this);
            n6603_decoce_windowing.addPreference(decode_centering_mode);

            /*decode_window_upper_left_x = new EditTextPreference(getActivity());
            decode_window_upper_left_x.setKey(Settings.System.DEC_2D_WINDOW_UPPER_LX);
            decode_window_upper_left_x.setTitle(R.string.n6603_UpperLeftWindowX);
            n6603_decoce_windowing.addPreference(decode_window_upper_left_x);
            decode_window_upper_left_y = new EditTextPreference(getActivity());
            decode_window_upper_left_y.setKey(Settings.System.DEC_2D_WINDOW_UPPER_LY);
            decode_window_upper_left_y.setTitle(R.string.n6603_UpperLeftWindowY);
            n6603_decoce_windowing.addPreference(decode_window_upper_left_y);
            decode_window_lower_right_x = new EditTextPreference(getActivity());
            decode_window_lower_right_x.setKey(Settings.System.DEC_2D_WINDOW_LOWER_RX);
            decode_window_lower_right_x.setTitle(R.string.n6603_LowerRightWindowX);
            n6603_decoce_windowing.addPreference(decode_window_lower_right_x);
            decode_window_lower_right_y = new EditTextPreference(getActivity());
            decode_window_lower_right_y.setKey(Settings.System.DEC_2D_WINDOW_LOWER_RY);
            decode_window_lower_right_y.setTitle(R.string.n6603_LowerRightWindowY);
            n6603_decoce_windowing.addPreference(decode_window_lower_right_y);*/

        }

        if(ScannerAdapter.isSupportPreference(scannerType, Settings.System.DEC_PICKLIST_AIM_MODE) && root.findPreference(Settings.System.DEC_PICKLIST_AIM_MODE) == null) {
            decPicklistAimMode = new SwitchPreference(getActivity());
            decPicklistAimMode.setKey(Settings.System.DEC_PICKLIST_AIM_MODE);
            decPicklistAimMode.setTitle(R.string.se4500_dec_picklist_aim_mode);
            root.addPreference(decPicklistAimMode);
        }
        //3680准确扫描
        if(ScannerAdapter.isSupportPreference(scannerType, Settings.System.DEC_PICKLIST_AIM_DELAY) && root.findPreference(Settings.System.DEC_PICKLIST_AIM_DELAY) == null) {
            dec_aim_mode_delay = new Preference(getActivity());
            dec_aim_mode_delay.setKey(Settings.System.DEC_PICKLIST_AIM_DELAY);
            dec_aim_mode_delay.setTitle(R.string.n3680_aimer_delay);
            root.addPreference(dec_aim_mode_delay);
        }
        int lightsConfig = 3;
        if(n6603_lightsConfig != null) {
            lightsConfig = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_2D_LIGHTS_MODE, 3);
            if (lightsConfig < 0) lightsConfig = 3;
            if (n6603_lightsConfig != null) {
                n6603_lightsConfig.setValue(String.valueOf(lightsConfig));
                n6603_lightsConfig.setSummary(n6603_lightsConfig.getEntry());
            }
        }
        if(exposureMode != null){
            int mode = mApplication.getPropertyInt(mProfileId, Settings.System.IMAGE_EXPOSURE_MODE, 0);
            exposureMode.setOnPreferenceChangeListener(this);
            exposureMode.setValue(String.valueOf(mode));
            exposureMode.setSummary(exposureMode.getEntry());
            if(fixedExposureLevel != null) {
                int level = mApplication.getPropertyInt(mProfileId, Settings.System.IMAGE_FIXED_EXPOSURE, 1);
                initEditText(fixedExposureLevel, true, "1~7874");
                fixedExposureLevel.setSummary(String.valueOf(level));
                if(mode == 0)
                    fixedExposureLevel.setEnabled(false);
            }
        }
        if(decIllumPowerLevel != null) {
            int level = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_ILLUM_POWER_LEVEL, 10);
            decIllumPowerLevel.setSummary("" + level);
            if (lightsConfig >= 2) {
                decIllumPowerLevel.setEnabled(true);
            } else {
                decIllumPowerLevel.setEnabled(false);
            }
        }
        if(decPicklistAimMode != null) {
            boolean isChecked = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_PICKLIST_AIM_MODE, 0) == 1;
            decPicklistAimMode.setChecked(isChecked);
            if (lightsConfig == 1 || 3 == lightsConfig || 4 == lightsConfig) {
                if (decPicklistAimMode != null) {
                    decPicklistAimMode.setEnabled(true);
                }
            } else {
                if (decPicklistAimMode != null) {
                    decPicklistAimMode.setEnabled(false);
                }
            }
        }
        if(dec_aim_mode_delay != null) {
            int delay = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_PICKLIST_AIM_DELAY, 0);
            updateSummay(dec_aim_mode_delay, delay < 0 ? 0 : delay);
            if(lightsConfig == 1 || 3 == lightsConfig || 4 == lightsConfig) {
                if(dec_aim_mode_delay != null){
                    dec_aim_mode_delay.setEnabled(true);
                }
                if(decPicklistAimMode != null) {
                    decPicklistAimMode.setEnabled(true);
                }
            }else {
                if(dec_aim_mode_delay != null){
                    dec_aim_mode_delay.setEnabled(false);
                }
                if(decPicklistAimMode != null) {
                    decPicklistAimMode.setEnabled(false);
                }
            }
        }
        if(n6603_dec_centering_enable != null) {
            value = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_2D_CENTERING_ENABLE, 0);
            n6603_dec_centering_enable.setChecked(value == 1);
            value = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_2D_CENTERING_MODE, 0);
            if(value < 0) value = 0;
            decode_centering_mode.setOnPreferenceChangeListener(this);
            decode_centering_mode.setValue(String.valueOf(value));
            decode_centering_mode.setSummary(decode_centering_mode.getEntry());
            decode_centering_mode.setEnabled(n6603_dec_centering_enable.isChecked());
           /* WindowTopY       : default  290, min    0, max   319
            WindowBotY       : default  350, min  320, max   639
            WindowTopX       : default  386, min    0, max   415
            WindowBotX       : default  446, min  416, max   831*/
            //upX 386 upY 290 lwX 446 lwY 350
            int window = 0;
            if(decode_window_upper_left_x != null) {
                mApplication.getPropertyInt(mProfileId, Settings.System.DEC_2D_WINDOW_UPPER_LX, 386);
                if(window < 0) window = 386;
                decode_window_upper_left_x.setSummary(String.valueOf(window));
                decode_window_upper_left_x.setEnabled(n6603_dec_centering_enable.isChecked());
            }
            if(decode_window_upper_left_y != null) {
                window = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_2D_WINDOW_UPPER_LY, 290);
                if(window < 0) window = 290;
                decode_window_upper_left_y.setSummary(String.valueOf(window));
                decode_window_upper_left_y.setEnabled(n6603_dec_centering_enable.isChecked());
            }
            if(decode_window_lower_right_x != null) {
                window = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_2D_WINDOW_LOWER_RX, 446);
                if(window < 0) window = 446;
                decode_window_lower_right_x.setSummary(String.valueOf(window));
                decode_window_lower_right_x.setEnabled(n6603_dec_centering_enable.isChecked());
            }
            if(decode_window_lower_right_y != null) {
                window = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_2D_WINDOW_LOWER_RY, 350);
                if(window < 0) window = 350;
                decode_window_lower_right_y.setSummary(String.valueOf(window));
                decode_window_lower_right_y.setEnabled(n6603_dec_centering_enable.isChecked());
            }
        }
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        try {
            String key = preference.getKey();
            if (key == null) return false;
            if (key.equals(KEY_LASER_ON_TIME)) {
                showSeekBarDialog(R.string.scanner_laser_on_time, Settings.System.LASER_ON_TIME, 5);
            } else if (key.equals(KEY_TIMEOUT_SAME_SYMBOL)) {
                showSeekBarDialog(R.string.scanner_timeout_same_symbol, Settings.System.TIMEOUT_BETWEEN_SAME_SYMBOL, 1);
            } else if (key.equals(Settings.System.DEC_ILLUM_POWER_LEVEL)) {
                showSeekBarDialog(R.string.se4500_illum_power_level, Settings.System.DEC_ILLUM_POWER_LEVEL, 0);
            } else if (key.equals(Settings.System.DEC_PICKLIST_AIM_DELAY)) {
                showSeekBarDialog(R.string.n3680_aimer_delay, Settings.System.DEC_PICKLIST_AIM_DELAY, 0);
            } else if (key.equals(Settings.System.FUZZY_1D_PROCESSING)) {
                mApplication.setPropertyInt(mProfileId, PropertyID.FUZZY_1D_PROCESSING, Settings.System.FUZZY_1D_PROCESSING, mFuzzy_1d_processing.isChecked() ? 1 : 0);
            } else if (key.equals(Settings.System.MULTI_DECODE_MODE)) {
                mApplication.setPropertyInt(mProfileId, PropertyID.MULTI_DECODE_MODE, Settings.System.MULTI_DECODE_MODE, mMulti_decode_mode.isChecked() ? 1 : 0);
            } else if (key.equals(Settings.System.FULL_READ_MODE)) {
                mApplication.setPropertyInt(mProfileId, PropertyID.FULL_READ_MODE, Settings.System.FULL_READ_MODE, mFull_read_mode.isChecked() ? 1 : 0);
            } else if (key.equals(Settings.System.IMAGE_FIXED_EXPOSURE)) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals(Settings.System.DEC_2D_WINDOW_UPPER_LX)) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals(Settings.System.DEC_2D_WINDOW_UPPER_LY)) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals(Settings.System.DEC_2D_WINDOW_LOWER_RX)) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals(Settings.System.DEC_2D_WINDOW_LOWER_RY)) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals(Settings.System.DEC_2D_CENTERING_ENABLE)) {
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_CENTERING_ENABLE, Settings.System.DEC_2D_CENTERING_ENABLE, n6603_dec_centering_enable.isChecked() ? 1 : 0);
                decode_centering_mode.setEnabled(n6603_dec_centering_enable.isChecked());
                if(decode_window_upper_left_x != null) {
                    decode_window_upper_left_x.setEnabled(n6603_dec_centering_enable.isChecked());
                }
                if(decode_window_upper_left_y != null) {
                    decode_window_upper_left_y.setEnabled(n6603_dec_centering_enable.isChecked());
                }
                if(decode_window_lower_right_x != null) {
                    decode_window_lower_right_x.setEnabled(n6603_dec_centering_enable.isChecked());
                }
                if(decode_window_lower_right_y != null) {
                    decode_window_lower_right_y.setEnabled(n6603_dec_centering_enable.isChecked());
                }
            } /*else if (key.equals("decode_debug_window_enable")) {
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_DEBUG_WINDOW_ENABLE, Settings.System.DEC_2D_DEBUG_WINDOW_ENABLE, decode_debug_window_enable.isChecked() ? 1 : 0);
            } */else if (key.equals(Settings.System.DEC_PICKLIST_AIM_MODE)) {
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_PICKLIST_AIM_MODE, Settings.System.DEC_PICKLIST_AIM_MODE, decPicklistAimMode.isChecked() ? 1 : 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (key == null) return false;
        if (preference == imgCodingFormatPref) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.CODING_FORMAT, Settings.System.CODING_FORMAT, value);
            imgCodingFormatPref.setSummary(imgCodingFormatPref.getEntries()[value]);
        } else if (key.equals(KEY_LINER_SECURITY_LEVEL)) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL, Settings.System.LINEAR_CODE_TYPE_SECURITY_LEVEL, value + 1);
            mLinearSecurityLevel.setSummary(mLinearSecurityLevel.getEntries()[value]);
        } else if (key.equals(KEY_1D_QUIET_ZONE_LEVEL)) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.LINEAR_1D_QUIET_ZONE_LEVEL, Settings.System.LINEAR_1D_QUIET_ZONE_LEVEL, value);
            m1DQuietZoneLevel.setSummary(m1DQuietZoneLevel.getEntries()[value]);
        } else if (key.equals(Settings.System.DEC_2D_LIGHTS_MODE)) {
            int value = Integer.parseInt((String) newValue);
            if (decIllumPowerLevel != null) {
                decIllumPowerLevel.setEnabled(value >= 2 ? true : false);
            }
            mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_LIGHTS_MODE, Settings.System.DEC_2D_LIGHTS_MODE, value);
            n6603_lightsConfig.setSummary(n6603_lightsConfig.getEntries()[value]);
            if (ScannerAdapter.isSupportPreference(scannerType,  ScannerAdapter.KEY_DEC_AIM_MODE_DELAY)) {
                if (value == 1 || 3 == value || 4 == value) {
                    if (dec_aim_mode_delay != null) {
                        dec_aim_mode_delay.setEnabled(true);
                    }
                    if (decPicklistAimMode != null) {
                        decPicklistAimMode.setEnabled(true);
                    }
                } else {
                    if (dec_aim_mode_delay != null) {
                        dec_aim_mode_delay.setEnabled(false);
                    }
                    if (decPicklistAimMode != null) {
                        decPicklistAimMode.setEnabled(false);
                    }
                }
            }
        } else if (key.equals(Settings.System.BAR_CODES_TO_READ)) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.BAR_CODES_TO_READ, Settings.System.BAR_CODES_TO_READ, value);
            mImage_bar_cades_to_read.setSummary( mImage_bar_cades_to_read.getEntries()[value]);
        } else if (key.equals(Settings.System.IMAGE_ONE_D_INVERSE)) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.IMAGE_ONE_D_INVERSE, Settings.System.IMAGE_ONE_D_INVERSE, value);
            mImage_inverse_decoder.setSummary(mImage_inverse_decoder.getEntries()[value]);
        } else if (key.equals(Settings.System.IMAGE_FIXED_EXPOSURE)) {
            if(newValue == null || newValue.equals("")) return false;
            int len = Integer.parseInt((String) newValue);
            if(len >= 1 && len <= 7874) {
            } else if(len < 1) {
                len = 1;
            } else if(len > 7874) {
                len = 7874;
            }
            fixedExposureLevel.setSummary(len);
            mApplication.setPropertyInt(mProfileId, PropertyID.IMAGE_EXPOSURE_MODE, Settings.System.IMAGE_EXPOSURE_MODE, len);
        } else if (key.equals(Settings.System.IMAGE_EXPOSURE_MODE)) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.IMAGE_EXPOSURE_MODE, Settings.System.IMAGE_EXPOSURE_MODE, value);
            exposureMode.setSummary(exposureMode.getEntries()[value]);
            fixedExposureLevel.setEnabled(value == 0 ? false : true);
        } else if (key.equals(Settings.System.DEC_2D_WINDOW_UPPER_LX)) {
            if(newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            if(len >= 0 && len <= 415) {
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_WINDOW_UPPER_LX, Settings.System.DEC_2D_WINDOW_UPPER_LX, len);
                decode_window_upper_left_x.setSummary(value);
            } else {
                showAlertToast(0, 415);
            }
        } else if (key.equals(Settings.System.DEC_2D_WINDOW_UPPER_LY)) {
            if(newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            if(len >= 0 && len <= 319) {
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_WINDOW_UPPER_LY, Settings.System.DEC_2D_WINDOW_UPPER_LY, len);
                decode_window_upper_left_y.setSummary(value);
            } else {
                showAlertToast(0, 319);
            }
        } else if (key.equals(Settings.System.DEC_2D_WINDOW_LOWER_RX)) {
            if(newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            if(len >= 416 && len <= 831) {
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_WINDOW_LOWER_RX, Settings.System.DEC_2D_WINDOW_LOWER_RX, len);
                decode_window_lower_right_x.setSummary(value);
            } else {
                showAlertToast(416, 831);
            }
        } else if (key.equals(Settings.System.DEC_2D_WINDOW_LOWER_RY)) {
            if(newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            if(len >= 320 && len <= 639) {
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_WINDOW_LOWER_RY, Settings.System.DEC_2D_WINDOW_LOWER_RY, len);
                decode_window_lower_right_y.setSummary(value);
            } else {
                showAlertToast(320, 639);
            }
        } else if (key.equals(Settings.System.DEC_2D_CENTERING_MODE)) {
            int value = Integer.parseInt((String) newValue);
            if(value == 2) {
                //(412, 316), (420,324)
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_WINDOW_UPPER_LX, Settings.System.DEC_2D_WINDOW_UPPER_LX, 412);
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_WINDOW_UPPER_LY, Settings.System.DEC_2D_WINDOW_UPPER_LY, 316);
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_WINDOW_LOWER_RX, Settings.System.DEC_2D_WINDOW_LOWER_RX, 420);
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_WINDOW_LOWER_RY, Settings.System.DEC_2D_WINDOW_LOWER_RY, 324);
            }
            mApplication.setPropertyInt(mProfileId, PropertyID.DEC_2D_CENTERING_MODE, Settings.System.DEC_2D_CENTERING_MODE, value);
            decode_centering_mode.setSummary(decode_centering_mode.getEntries()[value]);
        }
        return true;
    }
    private Dialog mMaxMinDialog;
    private int mProgress;
    private TextView mCurrentPro;
    private int max;

    private void showSeekBarDialog(final int title, final String id, final int minVal) {
        if (mMaxMinDialog != null) mMaxMinDialog.dismiss();
        mProgress = USettings.System.getInt(mContentResolver, mProfileId, id, -1);
        if (mProgress < 0) mProgress = 0;
        View view = View.inflate(getActivity(), R.layout.scanner_maxmin_timeout_dialog, null);
        mCurrentPro = (TextView) view.findViewById(R.id.progress);
        SeekBar mSeekbar = (SeekBar) view.findViewById(R.id.maxmin_seekbar);
        if (minVal == 1) {
            max = 98;
            mCurrentPro.setText(mProgress + "/99");
        } else if (minVal == 0) {
            if (title == R.string.n3680_aimer_delay) {
                max = 40;
                mCurrentPro.setText(mProgress + "/40");
            } else {
                if (scannerType == 13) { // SE4710
                    if (mProgress > 7) {
                        mProgress = 7;
                    }
                    max = 7;
                    mCurrentPro.setText(mProgress + "/7");
                } else { // SE4500
                    max = 10;
                    mCurrentPro.setText(mProgress + "/10");
                }
            }
        } else {
            max = 94;
            mCurrentPro.setText(mProgress + "/99");
        }
        mSeekbar.setMax(max);
        mSeekbar.setProgress(mProgress - minVal);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                mProgress = progress + minVal;
                //android.util.ULog.i("debug", "onProgressChanged==================" + mProgress);
                mCurrentPro.setText(mProgress + "/" + max);
            }
        });

        mMaxMinDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(title)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (title == R.string.scanner_laser_on_time) {
                            mApplication.setPropertyInt(mProfileId, PropertyID.LASER_ON_TIME, Settings.System.LASER_ON_TIME, mProgress);
                            updateSummay(mLaserOnTime, mProgress);
                        } else if (title == R.string.scanner_timeout_same_symbol) {
                            mApplication.setPropertyInt(mProfileId, PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL, Settings.System.TIMEOUT_BETWEEN_SAME_SYMBOL, mProgress);
                            updateSummay(mLinerTimeoutSym, mProgress);
                        } else if (title == R.string.n3680_aimer_delay) {
                            mApplication.setPropertyInt(mProfileId, PropertyID.DEC_PICKLIST_AIM_DELAY, Settings.System.DEC_PICKLIST_AIM_DELAY, mProgress);
                            updateSummay(dec_aim_mode_delay, mProgress);
                        } else {
                            mApplication.setPropertyInt(mProfileId, PropertyID.DEC_ILLUM_POWER_LEVEL, Settings.System.DEC_ILLUM_POWER_LEVEL, mProgress);
                            decIllumPowerLevel.setSummary("" + mProgress);
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .show();
    }
}
