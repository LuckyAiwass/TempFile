package com.ubx.scanwedge.settings.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

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
 * @Date: 20-1-15下午5:21
 */
public class OCRSettingsFragment extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private PreferenceScreen root;
    //OCR
    PreferenceCategory mOCR;
    private ListPreference sym_ocr_mode_config;
    private ListPreference sym_ocr_template_config;
    private Preference sym_ocr_template_font;
    private EditTextPreference sym_ocr_user_template;
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
        root = this.getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        getActivity().setTitle(R.string.lable_ocr_symbology);
        updateActionBar();
        updateOCRScanPreference();
    }
    private void updateOCRScanPreference() {
        if(root == null) return;
        if(root.findPreference(Settings.System.DEC_OCR_MODE) == null) {
            sym_ocr_mode_config = new ListPreference(getActivity());
            sym_ocr_mode_config.setKey(Settings.System.DEC_OCR_MODE);
            sym_ocr_mode_config.setTitle(R.string.lable_ocr_symbology_mode);
            sym_ocr_mode_config.setEntries(R.array.ocr_mode_entries);
            sym_ocr_mode_config.setEntryValues(R.array.ocr_mode_values);
            sym_ocr_mode_config.setOnPreferenceChangeListener(this);
            root.addPreference(sym_ocr_mode_config);
        }
        if(root.findPreference(Settings.System.DEC_OCR_TEMPLATE) == null) {
            sym_ocr_template_config = new ListPreference(getActivity());
            sym_ocr_template_config.setKey(Settings.System.DEC_OCR_TEMPLATE);
            sym_ocr_template_config.setTitle(R.string.lable_ocr_symbology_template);
            sym_ocr_template_config.setEntries(R.array.ocr_template_entries);
            sym_ocr_template_config.setEntryValues(R.array.ocr_template_values);
            sym_ocr_template_config.setOnPreferenceChangeListener(this);
            root.addPreference(sym_ocr_template_config);
        }
        if(root.findPreference("edit_ocr_user_template") == null) {
            sym_ocr_template_font = new Preference(getActivity());
            sym_ocr_template_font.setKey("edit_ocr_user_template");
            sym_ocr_template_font.setTitle(R.string.lable_ocr_user_template);
            root.addPreference(sym_ocr_template_font);
        }

        if(root.findPreference(Settings.System.DEC_OCR_USER_TEMPLATE) == null) {
            sym_ocr_user_template = new EditTextPreference(getActivity());
            sym_ocr_user_template.setKey(Settings.System.DEC_OCR_USER_TEMPLATE);
            sym_ocr_user_template.setTitle(R.string.lable_defined_ocr_user_template);
            sym_ocr_user_template.setDialogTitle(R.string.lable_defined_ocr_user_template);
            sym_ocr_user_template.setDialogMessage(R.string.feature_ocr_dialogmessage_summary);
            sym_ocr_user_template.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
            root.addPreference(sym_ocr_user_template);
        }
        if (sym_ocr_mode_config != null) {
            int value = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_OCR_MODE, 0);
            sym_ocr_mode_config.setOnPreferenceChangeListener(this);
            sym_ocr_mode_config.setValue(String.valueOf(value));
            sym_ocr_mode_config.setSummary(sym_ocr_mode_config.getEntry());
        }
        if (sym_ocr_template_config != null) {
            int value = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_OCR_MODE, 1);
            sym_ocr_template_config.setOnPreferenceChangeListener(this);
            sym_ocr_template_config.setValue(String.valueOf(value));
            sym_ocr_template_config.setSummary(sym_ocr_template_config.getEntry());
        }
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        try {
            String key = preference.getKey();
            if (key == null) return false;
            if (preference == sym_ocr_template_font) {
                showEditDialog(R.string.lable_ocr_user_template, "");
            } else if(preference == sym_ocr_user_template) {
                String template = mApplication.getPropertyString(mProfileId, Settings.System.DEC_OCR_USER_TEMPLATE, "13777777770");
                ((EditTextPreference) preference).getEditText().setText(template);
            }
        } catch (Exception e) {

        }
        return true;
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == sym_ocr_mode_config) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.DEC_OCR_MODE, Settings.System.DEC_OCR_MODE, value);
            sym_ocr_mode_config.setSummary(sym_ocr_mode_config.getEntries()[value]);
        } else if (preference == sym_ocr_template_config) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.DEC_OCR_TEMPLATE, Settings.System.DEC_OCR_TEMPLATE, value);
            sym_ocr_template_config.setSummary(sym_ocr_template_config.getEntries()[value]);
        } else if (preference == sym_ocr_user_template) {
            String value = newValue.toString();
            if(TextUtils.isEmpty(value) == false) {
                mApplication.setPropertyString(mProfileId, PropertyID.DEC_OCR_USER_TEMPLATE, Settings.System.DEC_OCR_USER_TEMPLATE, value);
            }
        }
        return false;
    }
    private Dialog mOkDialog;
    int templateFont = 2;
    int templateCharacter = 7;
    EditText templateFontLength;
    int[] userTemplateFont = null;
    int[] userTemplateCharacter = null;
    SharedPreferences sharedPrefs;

    private void showEditDialog(final int title, final String currentFormat) {
        if (mOkDialog != null) dismissDialog();
        View view = View.inflate(getActivity(),
                R.layout.scanner_edit_ocr_template_dialog, null);
        templateFontLength = (EditText) view.findViewById(R.id.user_template_font_length);
        Button sure = (Button) view.findViewById(R.id.ok);
        Button cancel = (Button) view.findViewById(R.id.cancel);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String orcLength = sharedPrefs.getString("user_template_font_length", "8");
        int orcFont = sharedPrefs.getInt("user_template_font", 2);
        int orcCharacter = sharedPrefs.getInt("user_template_character", 2);
        templateFontLength.setText("" + orcLength);
        Spinner mFontSpinner = (Spinner) view.findViewById(R.id.user_template_font);
        Spinner mCharacterSpinner = (Spinner) view.findViewById(R.id.user_template_character);
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(
                getActivity(), R.array.ocr_template_font_entries, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTemplateFont = getActivity().getResources().getIntArray(R.array.ocr_template_font_values);
        mFontSpinner.setAdapter(adapter1);
        mFontSpinner.setSelection(orcFont);
        mFontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                templateFont = userTemplateFont[position];
                android.util.Log.i("debug", "position, " + position + " templateFont id " + templateFont);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt("user_template_font", position);
                editor.commit();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        ArrayAdapter adapter2 = ArrayAdapter.createFromResource(
                getActivity(), R.array.ocr_template_character_entries, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTemplateCharacter = getActivity().getResources().getIntArray(R.array.ocr_template_character_values);
        mCharacterSpinner.setAdapter(adapter2);
        mCharacterSpinner.setSelection(orcCharacter);
        mCharacterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                templateCharacter = userTemplateCharacter[position];
                android.util.Log.i("debug", "position, " + position + " templateFont id " + templateCharacter);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt("user_template_character", position);
                editor.commit();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        mOkDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(title)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String lablePrefix = templateFontLength.getText().toString();
                            if (TextUtils.isEmpty(lablePrefix) == false) {
                                int length = Integer.parseInt(lablePrefix);
                                if (length < 1 || length > 50) return;
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                editor.putString("user_template_font_length", "" + length);
                                editor.commit();
                                StringBuffer sb = new StringBuffer();
                                sb.append(1).append(templateFont);
                                for (int i = 0; i < length; i++) {
                                    sb.append(templateCharacter);
                                }
                                sb.append(0);
                                android.util.Log.i("n6603", " new template:" + sb.toString());
                                mApplication.setPropertyString(mProfileId, PropertyID.DEC_OCR_USER_TEMPLATE, Settings.System.DEC_OCR_USER_TEMPLATE, sb.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.util.Log.i("debug", "setNegativeButton, )");
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void dismissDialog() {
        if (mOkDialog == null) return;
        mOkDialog.dismiss();
        mOkDialog = null;
    }
}
