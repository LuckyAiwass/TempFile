package com.ubx.scanwedge.settings.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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
 * @Date: 19-12-30下午9:41
 */
public class DataOutputFrament extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "WedgeDataOutput";
    private static final String KEY_KEYBOARD_GROUP = "scanner_keyboard_group";
    private static final String KEY_KEYBOARD_MODE = "scanner_keyboard_output";
    private static final String KEY_SCAN_SOUNDS = "scanner_beep";
    private static final String KEY_SCAN_SOUNDS_CHANNEL = "scanner_beep_channel";
    private static final String KEY_SCAN_CUSTOM_SOUNDS = "scanner_custom_beep";
    private static final String KEY_SCAN_VIBRATE = "scanner_vibrate";
    private static final String KEY_SCAN_ENTER = "action_key_character";
    private static final String KEY_RESET_GROUP = "reset_group";
    private static final String KEY_RESET_SCAN = "reset_def";
    private static final String KEY_IMPORT_CONFIG = "import_config";
    private static final String KEY_EXPORT_CONFIG = "export_config";
    private static final String KEY_ScanVirtualButton = "ScanVirtualButton";
    private static final String KEY_SCAN_HANDLE = "handle_scan_key";
    private static final String KEY_PHONEMODE_SCAN = "phonemode_scan_key";
    private static final String KEY_TRIGGER_MODE = "scanner_trigger_mode";
    private static final String KEY_KEYBOARD_TYPE = "scanner_keyboard_type";
    private static final String KEY_SYMBOLOGY_SET = "scanner_symbology_settings";
    private static final String KEY_FORMAT = "scanner_formatting";
    private static final String KEY_TRIGGER_READ = "scanner_triggering";
    private static final String KEY_CODING_FORMAT = "image_coding_format";
    private static final String KEY_INTENT_GROUP = "scanner_intent_group";
    private static final String KEY_INTENT_ACTION = "intent_action";
    private static final String KEY_INTENT_STRINGLABEL = "intent_stringlabel";
    private static final String KEY_SEND_SCAN_SOUNDS = "scanner_send_beep";
    private static final String KEY_SEND_SCAN_VIBRATE = "scanner_send_vibrate";
    private static final String KEY_WIRED_SCAN = "wired_scan";
    private static final String KEY_SCAN_APP = "scan_app_key";
    private static final String KEY_ASSOCIATED_APPS = "associated_apps";
    private static final String KEY_MULTIPLE_DECODE = "scan_multiple_decode";
    private static final String KEY_OUT_EDITORTEXT_MODE = "OUT_EDITORTEXT_MODE";

    private PreferenceScreen root;
    private SwitchPreference keyboardOutPref = null;
    private PreferenceCategory keyboardCategory = null;
    private ListPreference keyboardTypePref = null;
    private ListPreference keyboardBeepPref = null;
    private ListPreference feedbackBeepChannelPref = null;
    private Preference feedbackBeepCustomPref = null;
    private SwitchPreference keyboardVibratePref = null;
    private SwitchPreference keyboardEnterPref = null;
    private ListPreference keyboardActionKeyPref = null;
    private ListPreference outEditorTextMode = null;
    private SwitchPreference mClipboard;

    private PreferenceCategory intentCategory = null;
    private SwitchPreference intentOutput = null;
    private EditTextPreference intentActionPref = null;
    private EditTextPreference intentActionCategory = null;
    private EditTextPreference intentLabelPref = null;
    private ListPreference intentBeepPref = null;
    private SwitchPreference intentVibratePref = null;
    private SwitchPreference intentForegroundFlag = null;

    private int scannerType;
    private int mProfileId;
    private ContentResolver mContentResolver;
    private Activity mContext;
    private ScanWedgeApplication mApplication;

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
        mContext = getActivity();
        setHasOptionsMenu(true);
        root = this.getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        Bundle args = getArguments();
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
        root = this.getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        updateActionBar();
        getActivity().setTitle(R.string.scanner_wedge_mode);
        initKeyboardAndIntentCategory();
    }

    /***/
    private void initKeyboardAndIntentCategory() {
        if (root == null) return;
        keyboardCategory = new PreferenceCategory(mContext);
        keyboardCategory.setKey(KEY_KEYBOARD_GROUP);
        keyboardCategory.setTitle(R.string.scanner_keyboard_output);
        root.addPreference(keyboardCategory);

        keyboardOutPref = new SwitchPreference(mContext);
        keyboardOutPref.setKey(KEY_KEYBOARD_MODE);
        keyboardOutPref.setTitle(R.string.scanner_output_mode);
        keyboardOutPref.setSummary(R.string.scanner_keyboard_output_summary);
        //root.addPreference(keyboardOutPref);
        keyboardCategory.addPreference(keyboardOutPref);
        keyboardOutPref.setOnPreferenceChangeListener(this);

        keyboardTypePref = new ListPreference(mContext);
        keyboardTypePref.setKey(KEY_KEYBOARD_TYPE);
        keyboardTypePref.setTitle(R.string.scanner_keyboard_type_title);
        keyboardTypePref.setEntries(R.array.scanner_keyboard_type_entries);
        keyboardTypePref.setEntryValues(R.array.scanner_keyboard_type_values);
        keyboardTypePref.setOnPreferenceChangeListener(this);
        keyboardCategory.addPreference(keyboardTypePref);


        /*keyboardActionKeyPref= new ListPreference(mContext);
        keyboardActionKeyPref.setKey(KEY_SCAN_ENTER);
        keyboardActionKeyPref.setTitle(R.string.scanner_action_key_character);
        keyboardActionKeyPref.setEntries(R.array.action_key_character_entries);
        keyboardActionKeyPref.setEntryValues(R.array.action_key_character_values);
        keyboardActionKeyPref.setOnPreferenceChangeListener(this);
        keyboardCategory.addPreference(keyboardActionKeyPref);*/

        intentCategory = new PreferenceCategory(mContext);
        intentCategory.setKey(KEY_INTENT_GROUP);
        intentCategory.setTitle(R.string.scanner_wedge_intent_mode);
        root.addPreference(intentCategory);
        intentOutput = new SwitchPreference(mContext);
        intentOutput.setKey(Settings.System.WEDGE_KEYBOARD_ENABLE);
        intentOutput.setTitle(R.string.scanner_wedge_intent_title);
        intentOutput.setOnPreferenceChangeListener(this);
        intentCategory.addPreference(intentOutput);

        intentActionPref = new EditTextPreference(mContext);
        intentActionPref.setKey(Settings.System.WEDGE_INTENT_ACTION_NAME);
        intentActionPref.setTitle(R.string.scanner_intent_action);
        intentActionPref.setOnPreferenceChangeListener(this);
        intentCategory.addPreference(intentActionPref);

        intentLabelPref = new EditTextPreference(mContext);
        intentLabelPref.setKey(Settings.System.WEDGE_INTENT_DATA_STRING_TAG);
        intentLabelPref.setTitle(R.string.scanner_intent_stringlabel);
        intentLabelPref.setOnPreferenceChangeListener(this);
        intentCategory.addPreference(intentLabelPref);

        intentActionCategory = new EditTextPreference(mContext);
        intentActionCategory.setKey(Settings.System.WEDGE_INTENT_CATEGORY_NAME);
        intentActionCategory.setTitle(R.string.scanner_intent_categtoy);//intent categtoy
        intentActionCategory.setOnPreferenceChangeListener(this); //Specify the intent category name
        //intentCategory.addPreference(intentActionCategory);

        intentForegroundFlag = new SwitchPreference(mContext);
        intentForegroundFlag.setKey(Settings.System.WEDGE_INTENT_FOREGROUND_FLAG);
        intentForegroundFlag.setTitle(R.string.scanner_intent_recevier_foreground);
        intentForegroundFlag.setSummary(R.string.scanner_intent_recevier_summary);
        //intentCategory.addPreference(intentForegroundFlag);

        mClipboard = new SwitchPreference(mContext);
        mClipboard.setKey(Settings.System.OUT_CLIPBOARD_ENABLE);
        mClipboard.setTitle(R.string.scanner_keyboard_copy);
        root.addPreference(mClipboard);
        mClipboard.setOnPreferenceChangeListener(this);

        outEditorTextMode = new ListPreference(mContext);
        outEditorTextMode.setKey(Settings.System.OUT_EDITORTEXT_MODE);
        outEditorTextMode.setTitle(R.string.scanner_out_editortext_mode);
        outEditorTextMode.setEntries(R.array.out_editortext_mode_entries);
        outEditorTextMode.setEntryValues(R.array.out_editortext_mode_values);
        outEditorTextMode.setOnPreferenceChangeListener(this);
        root.addPreference(outEditorTextMode);

        int isKbOut = mApplication.getPropertyInt(mProfileId, Settings.System.WEDGE_KEYBOARD_ENABLE, 0);
        if (isKbOut == 1) {
            keyboardOutPref.setChecked(true);
            intentOutput.setChecked(false);
        } else if (isKbOut == 0) {
            keyboardOutPref.setChecked(false);
            intentOutput.setChecked(false);
        } else if (isKbOut == 2) {
            keyboardOutPref.setChecked(true);
            intentOutput.setChecked(true);
        }else if (isKbOut == 3) {
            keyboardOutPref.setChecked(false);
            intentOutput.setChecked(true);
        }

        int kbType = mApplication.getPropertyInt(mProfileId, Settings.System.WEDGE_KEYBOARD_TYPE, 0);
        keyboardTypePref.setValue(String.valueOf(kbType));
        keyboardTypePref.setSummary(keyboardTypePref.getEntries()[kbType]);

        String action = mApplication.getPropertyString(mProfileId, Settings.System.WEDGE_INTENT_ACTION_NAME, "");
        intentActionPref.setDefaultValue(action);
        intentActionPref.setSummary(action);
        String label = mApplication.getPropertyString(mProfileId, Settings.System.WEDGE_INTENT_DATA_STRING_TAG, "");
        intentLabelPref.setDefaultValue(label);
        intentLabelPref.setSummary(label);
        int mode = mApplication.getPropertyInt(mProfileId, Settings.System.OUT_CLIPBOARD_ENABLE, 0);
        mClipboard.setChecked(mode == 1);
        mode = mApplication.getPropertyInt(mProfileId, Settings.System.OUT_EDITORTEXT_MODE, 0);
        outEditorTextMode.setValue(String.valueOf(mode));
        outEditorTextMode.setSummary(outEditorTextMode.getEntries()[mode]);
        if (kbType == 3) {
            outEditorTextMode.setEnabled(true);
        } else {
            outEditorTextMode.setEnabled(false);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        try {
            if (preference == keyboardOutPref) {
                int value = 0;
                if (intentOutput.isChecked() && keyboardOutPref.isChecked()) {
                    value = 2;
                } else if (!intentOutput.isChecked() && keyboardOutPref.isChecked()) {
                    value = 1;
                } else if (intentOutput.isChecked() && !keyboardOutPref.isChecked()) {
                    value = 3;
                } else {
                    value = 0;
                }
                mApplication.setPropertyInt(mProfileId, PropertyID.WEDGE_KEYBOARD_ENABLE, Settings.System.WEDGE_KEYBOARD_ENABLE, value);
            } else if (preference == intentOutput) {
                int value = 0;
                if (intentOutput.isChecked() && keyboardOutPref.isChecked()) {
                    value = 2;
                } else if (!intentOutput.isChecked() && keyboardOutPref.isChecked()) {
                    value = 1;
                } else if (intentOutput.isChecked() && !keyboardOutPref.isChecked()) {
                    value = 3;
                } else {
                    value = 0;
                }
                mApplication.setPropertyInt(mProfileId, PropertyID.WEDGE_KEYBOARD_ENABLE, Settings.System.WEDGE_KEYBOARD_ENABLE, value);
            } else if (preference == mClipboard) {
                mApplication.setPropertyInt(mProfileId, PropertyID.OUT_CLIPBOARD_ENABLE, Settings.System.OUT_CLIPBOARD_ENABLE, mClipboard.isChecked() ? 1 : 0);
            } else if (preference == intentLabelPref) {
                intentLabelPref.getEditText().setText(intentLabelPref.getSummary());
            } else if (preference == intentActionPref) {
                intentActionPref.getEditText().setText(intentActionPref.getSummary());
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
        if (preference == outEditorTextMode) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.OUT_EDITORTEXT_MODE, Settings.System.OUT_EDITORTEXT_MODE, value);
            outEditorTextMode.setValue(String.valueOf(value));
            outEditorTextMode.setSummary(outEditorTextMode.getEntries()[value]);
        } else if (preference == keyboardTypePref) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.WEDGE_KEYBOARD_TYPE, Settings.System.WEDGE_KEYBOARD_TYPE, value);
            keyboardTypePref.setValue(String.valueOf(value));
            keyboardTypePref.setSummary(keyboardTypePref.getEntries()[value]);
            if (value == 3) {
                outEditorTextMode.setEnabled(true);
            } else {
                outEditorTextMode.setEnabled(false);
            }
        } else if (preference == intentActionPref) {
            String action = newValue.toString();
            mApplication.setPropertyString(mProfileId, PropertyID.WEDGE_INTENT_ACTION_NAME, Settings.System.WEDGE_INTENT_ACTION_NAME, action);
            intentActionPref.setSummary(action);
        } else if (preference == intentLabelPref) {
            String label = newValue.toString();
            mApplication.setPropertyString(mProfileId, PropertyID.WEDGE_INTENT_DATA_STRING_TAG, Settings.System.WEDGE_INTENT_DATA_STRING_TAG, label);
            intentLabelPref.setSummary(label);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
