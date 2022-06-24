package com.ubx.scanwedge.settings.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.scanwedge.settings.utils.ScannerAdapter;
import com.ubx.database.helper.USettings;

import java.util.Stack;

public class DataFormattingSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private PreferenceScreen root;
    PreferenceCategory mScannerFormat;
    private ListPreference mSendLablePrefixSuffix;
    private Preference mLablePrefix;
    private Preference mLableSuffix;
    private Preference mLablePattern;
    private Preference mLableReplace;
    String replaceRegex = "";
    String replaceMent = "";
    private CheckBoxPreference mRemoveNonprintchar;
    private CheckBoxPreference mScanSeparator;
    private Preference edit_lable_separator;

    private ListPreference scanner_Transmit_Code_ID_type;
    private ListPreference keyboardActionKeyPref = null;
    private CheckBoxPreference mDecode_GS_Character_enable;
    private int scannerType;
    private int profileId;

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
        setHasOptionsMenu(true);
        Bundle args = getArguments();
        scannerType = args != null ? args.getInt("scannertype") : 0;
        profileId = args != null ? args.getInt("profileId") : USettings.Profile.DEFAULT_ID;
        addPreferencesFromResource(R.xml.scanner_data_formatting);
        root = this.getPreferenceScreen();
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
        updateActionBar();
        updateState();
    }

    private void updateState() {
        if(root != null) {
            root.removeAll();
        }
        root = this.getPreferenceScreen();
        getActivity().setTitle(R.string.scanner_formatting);
        mScannerFormat = new PreferenceCategory(getActivity());
        mScannerFormat.setTitle(R.string.scanner_formatting);
        root.addPreference(mScannerFormat);

        mSendLablePrefixSuffix = new ListPreference(getActivity());
        mSendLablePrefixSuffix.setTitle(R.string.scanner_send_lable_fix);
        mSendLablePrefixSuffix.setKey(Settings.System.SEND_LABEL_PREFIX_SUFFIX);
        mSendLablePrefixSuffix.setEntryValues(R.array.send_lable_fix_values);
        mSendLablePrefixSuffix.setEntries(R.array.send_lable_fix_entries);
        mSendLablePrefixSuffix.setOnPreferenceChangeListener(this);
        mScannerFormat.addPreference(mSendLablePrefixSuffix);

        mLablePrefix = new Preference(getActivity());
        mLablePrefix.setTitle(R.string.scanner_lable_prefix);
        mLablePrefix.setKey(Settings.System.LABEL_PREFIX);
        mScannerFormat.addPreference(mLablePrefix);

        mLableSuffix = new Preference(getActivity());
        mLableSuffix.setTitle(R.string.scanner_lable_suffix);
        mLableSuffix.setKey(Settings.System.LABEL_SUFFIX);
        mScannerFormat.addPreference(mLableSuffix);

        mLablePattern = new Preference(getActivity());
        mLablePattern.setTitle(R.string.scanner_lable_pattern);
        mLablePattern.setKey(Settings.System.LABEL_MATCHER_PATTERN);
        mScannerFormat.addPreference(mLablePattern);

        mLableReplace = new Preference(getActivity());
        mLableReplace.setTitle(R.string.scanner_lable_replace);
        mLableReplace.setKey(Settings.System.LABEL_MATCHER_TARGETREGEX);
        mScannerFormat.addPreference(mLableReplace);

        mRemoveNonprintchar = new CheckBoxPreference(getActivity());
        mRemoveNonprintchar.setTitle(R.string.remove_non_printable_chars);
        mRemoveNonprintchar.setKey(Settings.System.REMOVE_NONPRINT_CHAR);
        mRemoveNonprintchar.setOnPreferenceChangeListener(this);
        mScannerFormat.addPreference(mRemoveNonprintchar);

        mScanSeparator = new CheckBoxPreference(getActivity());
        mScanSeparator.setTitle(R.string.lable_application_identifier);
        mScanSeparator.setSummary(R.string.lable_application_identifier_summary);
        mScanSeparator.setKey(Settings.System.LABEL_SEPARATOR_ENABLE);
        mScanSeparator.setOnPreferenceChangeListener(this);
        mScannerFormat.addPreference(mScanSeparator);

        edit_lable_separator = new Preference(getActivity());
        edit_lable_separator.setTitle(R.string.lable_application_identifier_edit);
        edit_lable_separator.setKey(Settings.System.LABEL_FORMAT_SEPARATOR_CHAR);
        mScannerFormat.addPreference(edit_lable_separator);

        int mode = mApplication.getPropertyInt(profileId, Settings.System.SEND_LABEL_PREFIX_SUFFIX, 0);
        mSendLablePrefixSuffix.setValue(String.valueOf(mode));
        mSendLablePrefixSuffix.setSummary(mSendLablePrefixSuffix.getEntry());

        mode = mApplication.getPropertyInt(profileId, Settings.System.REMOVE_NONPRINT_CHAR, 0);
        mRemoveNonprintchar.setChecked(mode == 1);
        mode = mApplication.getPropertyInt(profileId, Settings.System.LABEL_SEPARATOR_ENABLE, 0);
        mScanSeparator.setOnPreferenceChangeListener(this);
        mScanSeparator.setChecked(mode == 1);
        edit_lable_separator.setEnabled(mScanSeparator.isEnabled());

        if (ScannerAdapter.isSupportPreference(scannerType, Settings.System.TRANSMIT_CODE_ID)) {
            scanner_Transmit_Code_ID_type = new ListPreference(getActivity());
            scanner_Transmit_Code_ID_type.setTitle(R.string.scanner_reader_codeid_type);
            scanner_Transmit_Code_ID_type.setKey(Settings.System.TRANSMIT_CODE_ID);
            scanner_Transmit_Code_ID_type.setEntryValues(R.array.code_id_type_values);
            scanner_Transmit_Code_ID_type.setEntries(R.array.code_id_type_entries);
            scanner_Transmit_Code_ID_type.setOnPreferenceChangeListener(this);
            mode = mApplication.getPropertyInt(profileId, Settings.System.TRANSMIT_CODE_ID, 0);
            scanner_Transmit_Code_ID_type.setOnPreferenceChangeListener(this);
            scanner_Transmit_Code_ID_type.setValue(String.valueOf(mode));
            scanner_Transmit_Code_ID_type.setSummary(scanner_Transmit_Code_ID_type.getEntry());
            mScannerFormat.addPreference(scanner_Transmit_Code_ID_type);
        }
        String prefix = mApplication.getPropertyString(profileId, Settings.System.LABEL_PREFIX, "");
        String suffix = mApplication.getPropertyString(profileId, Settings.System.LABEL_SUFFIX, "");
        String pattern = mApplication.getPropertyString(profileId, Settings.System.LABEL_MATCHER_PATTERN, "");
        String separatorChar = mApplication.getPropertyString(profileId, Settings.System.LABEL_FORMAT_SEPARATOR_CHAR, "()");
        if (TextUtils.isEmpty(separatorChar)) separatorChar = "()";//默认
        edit_lable_separator.setSummary(separatorChar);
        mLablePrefix.setSummary(prefix);
        mLableSuffix.setSummary(suffix);
        mLablePattern.setSummary(pattern);
        replaceRegex = mApplication.getPropertyString(profileId, Settings.System.LABEL_MATCHER_TARGETREGEX, "");
        replaceMent = mApplication.getPropertyString(profileId, Settings.System.LABEL_MATCHER_REPLACEMENT, "");
        if (replaceRegex != null && !replaceRegex.equals(""))
            mLableReplace.setSummary(replaceRegex + " > " + replaceMent);
        if (ScannerAdapter.isSupportPreference(scannerType, Settings.System.SPECIFIC_CODE_GS)) {
            mDecode_GS_Character_enable = new CheckBoxPreference(getActivity());
            mDecode_GS_Character_enable.setKey(Settings.System.SPECIFIC_CODE_GS);
            mDecode_GS_Character_enable.setTitle(R.string.n6603_Enable_GS_Window);
            //mDecode_GS_Character_enable.setOnPreferenceChangeListener(this);
            mode = mApplication.getPropertyInt(profileId, Settings.System.SPECIFIC_CODE_GS, 0);
            mDecode_GS_Character_enable.setChecked(mode == 1);
            mScannerFormat.addPreference(mDecode_GS_Character_enable);
        }

        keyboardActionKeyPref= new ListPreference(getActivity());
        keyboardActionKeyPref.setKey(Settings.System.LABEL_APPEND_ENTER);
        keyboardActionKeyPref.setTitle(R.string.scanner_action_key_character);
        keyboardActionKeyPref.setEntries(R.array.action_key_character_entries);
        keyboardActionKeyPref.setEntryValues(R.array.action_key_character_values);
        keyboardActionKeyPref.setOnPreferenceChangeListener(this);
        root.addPreference(keyboardActionKeyPref);
        int isEnter = mApplication.getPropertyInt(profileId, Settings.System.LABEL_APPEND_ENTER, 0);
        keyboardActionKeyPref.setValue(String.valueOf(isEnter));
        keyboardActionKeyPref.setSummary(keyboardActionKeyPref.getEntries()[isEnter]);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        try {
            String key = preference.getKey();
            if (key == null) return false;
            if (key.equals(Settings.System.LABEL_PREFIX)) {
                showEditDialog(R.string.edit_lable_prefix, mLablePrefix.getSummary().toString());
            } else if (key.equals(Settings.System.LABEL_SUFFIX)) {
                showEditDialog(R.string.edit_lable_suffix, mLableSuffix.getSummary().toString());
            } else if (key.equals(Settings.System.LABEL_MATCHER_PATTERN)) {
                showEditDialog(R.string.edit_lable_pattern, mLablePattern.getSummary().toString());
            } else if (key.equals(Settings.System.LABEL_MATCHER_TARGETREGEX)) {
                showEditDialog(R.string.scanner_lable_replace, "");
            } else if (key.equals(Settings.System.REMOVE_NONPRINT_CHAR)) {
                mApplication.setPropertyInt(profileId, PropertyID.REMOVE_NONPRINT_CHAR, Settings.System.REMOVE_NONPRINT_CHAR, mRemoveNonprintchar.isChecked() ? 1 : 0);
            } else if (preference == mScanSeparator) {
                mApplication.setPropertyInt(profileId, PropertyID.LABEL_SEPARATOR_ENABLE, Settings.System.LABEL_SEPARATOR_ENABLE, mScanSeparator.isChecked() ? 1 : 0);
            } else if (key.equals(Settings.System.LABEL_FORMAT_SEPARATOR_CHAR)) {
                String separator = (String) edit_lable_separator.getSummary();
                showEditDialog(R.string.lable_application_identifier_edit, separator);
            } else if(preference == mDecode_GS_Character_enable) {
                mApplication.setPropertyInt(profileId, PropertyID.SPECIFIC_CODE_GS, Settings.System.SPECIFIC_CODE_GS, mDecode_GS_Character_enable.isChecked() ? 1 : 0);
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
        if (key.equals(Settings.System.SEND_LABEL_PREFIX_SUFFIX)) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(profileId, PropertyID.SEND_LABEL_PREFIX_SUFFIX, Settings.System.SEND_LABEL_PREFIX_SUFFIX, value);
            mSendLablePrefixSuffix.setSummary(mSendLablePrefixSuffix.getEntries()[value]);
        } else if (key.equals(Settings.System.TRANSMIT_CODE_ID)) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(profileId, PropertyID.TRANSMIT_CODE_ID, Settings.System.TRANSMIT_CODE_ID, value);
            scanner_Transmit_Code_ID_type.setSummary(scanner_Transmit_Code_ID_type.getEntries()[value]);
        } else if (preference == keyboardActionKeyPref) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(profileId, PropertyID.LABEL_APPEND_ENTER, Settings.System.LABEL_APPEND_ENTER,value);
            keyboardActionKeyPref.setValue(String.valueOf(value));
            keyboardActionKeyPref.setSummary(keyboardActionKeyPref.getEntries()[value]);
        }
        return true;
    }

    private void initEditText(EditTextPreference keyEditText, boolean number) {

        keyEditText.getEditText().setInputType(number ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
        keyEditText.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(number ? 4 : 1)});
        keyEditText.getEditText().setHint("1~7848");
        keyEditText.setOnPreferenceChangeListener(this);

    }

    private void updateSummay(Preference pref, int value) {
        String timeout = String.format(getActivity().getString(R.string.scanner_laser_timeout_summary), String.valueOf(value * 100));
        pref.setSummary(timeout);
    }

    //Formating None
    private static final String NONE = "None";
    private Dialog mOkDialog;
    String specialChar = "";
    EditText mCurrentFormat;
    EditText mReplaceRegex;
    EditText mReplaceMent;

    private void showEditDialog(final int title, final String currentFormat) {
        if (mOkDialog != null) dismissDialog();
        View view = View.inflate(getActivity(),
                R.layout.scanner_edit_formatting_dialog, null);
        mCurrentFormat = (EditText) view.findViewById(R.id.edit_formatting);
        mReplaceRegex = (EditText) view.findViewById(R.id.lable_replaceregex);
        mReplaceMent = (EditText) view.findViewById(R.id.lable_replacement);
        if (!currentFormat.equals(NONE)) {
            mCurrentFormat.setText(currentFormat);
        }
        LinearLayout linearlayout = (LinearLayout) view.findViewById(R.id.format_linearlayout);
        LinearLayout linearlayoutReplaceSrc = (LinearLayout) view.findViewById(R.id.format_linearlayout_src);
        LinearLayout linearlayoutReplaceDst = (LinearLayout) view.findViewById(R.id.format_linearlayout_dst);
        Button sure = (Button) view.findViewById(R.id.ok);
        Button cancel = (Button) view.findViewById(R.id.cancel);

        Spinner mCharSpinner = (Spinner) view.findViewById(R.id.special_char);
        if (R.string.edit_lable_pattern == title) {
            linearlayout.setVisibility(View.GONE);
            linearlayoutReplaceSrc.setVisibility(View.GONE);
            linearlayoutReplaceDst.setVisibility(View.GONE);
            mCurrentFormat.setHint("[0-9]{5,}");
        } else if (R.string.scanner_lable_replace == title) {
            linearlayout.setVisibility(View.GONE);
            mCurrentFormat.setVisibility(View.GONE);
            mReplaceRegex.setText("" + replaceRegex);
            mReplaceMent.setText("" + replaceMent);
            mReplaceRegex.setHint("hex(0-9,A-F,a-f),eg.1D");
        } else if (R.string.lable_application_identifier_edit == title) {
            linearlayout.setVisibility(View.GONE);
            linearlayoutReplaceSrc.setVisibility(View.GONE);
            linearlayoutReplaceDst.setVisibility(View.GONE);
            mCurrentFormat.setHint("()");
        } else {
            linearlayoutReplaceSrc.setVisibility(View.GONE);
            linearlayoutReplaceDst.setVisibility(View.GONE);
        }
        ArrayAdapter adapter1 = ArrayAdapter.createFromResource(
                getActivity(), R.array.gs_substitution_entries, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mCharSpinner.setAdapter(adapter1);
        mCharSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                //specialChar = (String) mCharSpinner.getAdapter().getItem(position);
                //android.util.Log.i("debug", "position, " +position  +" long id " + id);
                if (position != 0) {
                    specialChar = getActivity().getResources().getStringArray(R.array.gs_substitution_entries)[position];
                    mCurrentFormat.append(specialChar);
                }
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
                        android.util.Log.i("debug", "setPositiveButton, ");
                        try {
                            if (title == R.string.edit_lable_prefix) {
                                String lablePrefix = mCurrentFormat.getText().toString();
                                if ("".equals(lablePrefix)) lablePrefix = "";
                                mLablePrefix.setSummary(lablePrefix);
                                mApplication.setPropertyString(profileId, PropertyID.LABEL_PREFIX, Settings.System.LABEL_PREFIX, lablePrefix);
                            } else if (title == R.string.edit_lable_suffix) {
                                String lableSuffix = mCurrentFormat.getText().toString();
                                if ("".equals(lableSuffix)) lableSuffix = "";
                                mLableSuffix.setSummary(lableSuffix);
                                mApplication.setPropertyString(profileId, PropertyID.LABEL_SUFFIX, Settings.System.LABEL_SUFFIX, lableSuffix);
                            } else if (title == R.string.edit_lable_pattern) {
                                String lablePattern = mCurrentFormat.getText().toString();
                                if ("".equals(lablePattern)) {
                                    lablePattern = "";
                                } else {
                                    if (!bracketPairs(lablePattern)) {
                                        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_lable_format_error), lablePattern), Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }
                                mLablePattern.setSummary(lablePattern);
                                mApplication.setPropertyString(profileId, PropertyID.LABEL_MATCHER_PATTERN, Settings.System.LABEL_MATCHER_PATTERN, lablePattern);
                            } else if (title == R.string.scanner_lable_replace) {
                                String regex = mReplaceRegex.getText().toString();
                                String ment = mReplaceMent.getText().toString();
                                if ("".equals(regex) || isHexAnd16Byte(regex) == false) {
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.scanner_lable_replaceerror), Toast.LENGTH_LONG).show();
                                } else {
                                    mLableReplace.setSummary(regex + " > " + ment);
                                    replaceRegex = regex;
                                    replaceMent = ment;
                                    mApplication.setPropertyString(profileId, PropertyID.LABEL_MATCHER_TARGETREGEX, Settings.System.LABEL_MATCHER_TARGETREGEX, regex);
                                    mApplication.setPropertyString(profileId, PropertyID.LABEL_MATCHER_REPLACEMENT, Settings.System.LABEL_MATCHER_REPLACEMENT, ment);
                                }
                            } else if (title == R.string.lable_application_identifier_edit) {
                                String regex = mCurrentFormat.getText().toString();
                                char reg0 = regex.charAt(0);
                                char reg1 = regex.charAt(1);
                                // Within the reasonable scope of the ASCII
                                if (reg0 >= 0x20 && reg0 <= 0x7F && reg1 >= 0x20 && reg1 <= 0x7F) {
                                    edit_lable_separator.setSummary(regex);
                                    mApplication.setPropertyString(profileId, PropertyID.LABEL_FORMAT_SEPARATOR_CHAR, Settings.System.LABEL_FORMAT_SEPARATOR_CHAR, regex);
                                } else {
                                    // Not within the reasonable scope of the ASCII
                                    Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.lable_ai_edit_ascii), regex), Toast.LENGTH_LONG).show();
                                }
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

    @Override
    public void onDestroy() {
        dismissDialog();
        super.onDestroy();
    }
    public boolean bracketPairs(String str) {
        if (str == null || "".equals(str.trim())) {
            return false;
        }
        Stack<Character> sc = new Stack<Character>();
        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == '(' || c[i] == '[' || c[i] == '{') {
                sc.push(c[i]);
            } else if (c[i] == ')') {
                if (sc.peek() == '(') {
                    sc.pop();
                }
            } else if (c[i] == ']') {
                if (sc.peek() == '[') {
                    sc.pop();
                }
            } else if (c[i] == '}') {
                if (sc.peek() == '{') {
                    sc.pop();
                }
            }
        }
        boolean formatResult = sc.empty();
        if (formatResult) {
            android.util.Log.i("dataformat", "bracketPairs " + str + " format is OK.");
        } else {
            android.util.Log.i("dataformat", "bracketPairs " + str + " NO format.");
        }
        sc.clear();
        sc = null;
        return formatResult;
    }
    public static boolean isHexAnd16Byte(String hexString) {
        if (hexString.matches("[0-9A-Fa-f]+") == false) {
            // Error, not hex.
            return false;
        }
        return true;
    }

    private void showAlertToast(int min, int max) {
        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_symbology_length_range), String.valueOf(min), String.valueOf(max)), Toast.LENGTH_LONG).show();
    }
}
