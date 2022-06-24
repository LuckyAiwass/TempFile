package com.ubx.scanwedge.settings.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.service.ImportExoprtService;
import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.scanwedge.settings.utils.ProfileEnable;
import com.ubx.scanwedge.settings.utils.ScannerAdapter;
import com.ubx.database.helper.UConstants;
import com.ubx.database.helper.USettings;

public class ScannerSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "Wedge" +ScannerSettings.class.getSimpleName();

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
    private static final String KEY_INTENT_GROUP = "scanner_intent_group";
    private static final String KEY_INTENT_ACTION = "intent_action";
    private static final String KEY_INTENT_STRINGLABEL = "intent_stringlabel";
    private static final String KEY_SEND_SCAN_SOUNDS = "scanner_send_beep";
    private static final String KEY_SEND_SCAN_VIBRATE = "scanner_send_vibrate";
    private static final String KEY_WIRED_SCAN = "wired_scan";
    private static final String KEY_SCAN_APP = "scan_app_key";
    private static final String KEY_ASSOCIATED_APPS_Category = "associated_apps_Category";
    private static final String KEY_ASSOCIATED_APPS = "associated_apps";
    private static final String KEY_MULTIPLE_DECODE = "scan_multiple_decode";
    private static final String KEY_OUT_EDITORTEXT_MODE = "OUT_EDITORTEXT_MODE";
    private static final String KEY_SPECIAL_SETTINGS = "KEY_SPECIAL_SETTINGS";

    private PreferenceScreen root;
    private Activity mContext;
    private int mProfileId;
    private String mProfileName;

    private int mScanType;
    //private ScannerEnable mScannerEnable;
    private ProfileEnable mProfileEnable;
    private ScanWedgeApplication mApplication;
    @Override
    public void initPresenter() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mApplication = (ScanWedgeApplication) getActivity().getApplication();
        mScanType = mApplication.getScannerType();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.v(TAG, "onViewCreated ");
        /*if (root != null) {
            root.setEnabled(MySettings.Profile.isProfileEnable(mHelper, mProfileName));
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v(TAG, "onDetach ");
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        Log.v(TAG, "onResume ");
        if(getArguments() != null) {
            mProfileId = getArguments().getInt(UConstants.PROFILE_ID, USettings.Profile.DEFAULT_ID);
            mProfileName = getArguments().getString(UConstants.PROFILE_NAME, USettings.Profile.DEFAULT);
            Log.v(TAG, "getArguments is-main-page " + getArguments().getBoolean("is-main-page", false));
        } else {
            Log.v(TAG, "getArguments null ");
            mProfileName = USettings.Profile.DEFAULT;
            mProfileId = USettings.Profile.DEFAULT_ID;
        }
        mProfileName = USettings.Profile.getProfileName(getActivity().getContentResolver(), mProfileId);
        Log.v(TAG, "getProfileName  " + mProfileName);
        addPreferencesFromResource(R.xml.scanner_settings);
        root = getPreferenceScreen();
        initActionBar();
        super.onResume();
        updatePreferences();
    }
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
    private void initActionBar() {
        //getActivity().getActionBar().setCustomView(null);
        if (mContext instanceof PreferenceActivity) {
            getActivity().setTitle(mProfileName);
            Switch barSwitch = new Switch(mContext);
            //Log.v(TAG, "initActionBar ==PreferenceActivity==== ");
            PreferenceActivity preferenceActivity = (PreferenceActivity) mContext;
            if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
                int padding = mContext.getResources().getDimensionPixelSize(R.dimen.action_bar_switch_padding);
                barSwitch.setPadding(0, 0, padding, 0);
                mContext.getActionBar().setDisplayShowCustomEnabled(true);
                mContext.getActionBar().setCustomView(barSwitch, new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.RIGHT));
            }
            /*mScannerEnable = new ScannerEnable(mContext, mApplication, barSwitch, mProfileId);
            mScannerEnable.setSwitch(barSwitch);
            mScannerEnable.setOnEnableListener(new ScannerEnable.OnEnableListener() {
                @Override
                public void onEnable(boolean enable) {
                    if (root != null) {
                        root.setEnabled(enable);
                        //MySettings.Profile.enableProfile(mHelper, mProfileName, enable);
                    }
                }
            });*/
            mProfileEnable = new ProfileEnable(mContext, mApplication, barSwitch, mProfileId);
            mProfileEnable.setSwitch(barSwitch);
            mProfileEnable.setOnEnableListener(new ProfileEnable.OnEnableListener() {
                @Override
                public void onEnable(boolean enable) {
                    /*if (root != null) {
                        root.setEnabled(enable);
                        //MySettings.Profile.enableProfile(mHelper, mProfileName, enable);
                    }*/
                }
            });
        } else {
            //Log.v(TAG, "initActionBar ====== ");
        }
    }

    private void updatePreferences() {
        if (root != null) {
            root.removeAll();
        }
        updateAssociatedAppPreference();
        updateVirtualButtonPreference();
        /*mDecodeFeedback = new Preference(mContext);
        mDecodeFeedback.setKey(Settings.System.GOOD_READ_BEEP_ENABLE);
        mDecodeFeedback.setTitle(R.string.scanner_decode_feedback);
        root.addPreference(mDecodeFeedback);*/
        updateDecodeFeedbackCategory();
        updateDecodePreferenceCategory();
        updateScanTriggerModePreference(mScanType);

        mOutputMode = new Preference(mContext);
        mOutputMode.setKey(Settings.System.WEDGE_INTENT_ENABLE);
        mOutputMode.setTitle(R.string.scanner_wedge_mode);
        root.addPreference(mOutputMode);
        /*initKeyboardAndIntentCategory();
        updateKeyboardAndIntentCategory();*/

        updateScanReaderPreference(mScanType);
        updateScannerFormatPreference();
        updateDecoderPropertiesPreference(mScanType);
        if(ScannerAdapter.isSupportPreference(mScanType, Settings.System.DEC_OCR_MODE)) {
            mOCRScanSettings = new Preference(mContext);
            mOCRScanSettings.setKey(Settings.System.DEC_OCR_MODE);
            mOCRScanSettings.setTitle(R.string.lable_ocr_symbology);
            root.addPreference(mOCRScanSettings);
        }
        updateSpecialPreference();
        //不添加重置配置文件功能
        //updateResetScannerCategory();
        setPreferenceScreen(root);
    }
    private PreferenceCategory scannerInputCategory = null;
    private final static String KEY_SCANNER_INPUT_Category = "scanner_input_Category";
    private SwitchPreference mScannerInputEnable;
    private final static String KEY_SCANNER_INPUT_ENABLE = "scanner_input_enable";

    private PreferenceCategory applicationCategory = null;
    private Preference associatedAppsPref = null;
    private SwitchPreference mScanVirtualButton;
    private Preference mOutputMode = null;
    private PreferenceCategory mDecodeFeedbackCategory = null;
    private Preference mDecodeFeedback;
    private Preference mOCRScanSettings = null;
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

    private PreferenceCategory intentCategory = null;
    private EditTextPreference intentActionPref = null;
    private EditTextPreference intentLabelPref = null;
    private ListPreference intentBeepPref = null;
    private SwitchPreference intentVibratePref = null;
    /**
     * 配置绑定应用
     */
    private void updateAssociatedAppPreference() {
        if (root == null) return;
        if (mProfileId == USettings.Profile.DEFAULT_ID) {
            if (root.findPreference(KEY_ASSOCIATED_APPS_Category) != null) {
                root.removePreference(applicationCategory);
            }
            return;
        }
        if (root.findPreference(KEY_ASSOCIATED_APPS_Category) == null) {
            applicationCategory = new PreferenceCategory(mContext);
            applicationCategory.setKey(KEY_ASSOCIATED_APPS_Category);
            applicationCategory.setTitle(R.string.scanner_associated_apps_category_title);
            root.addPreference(applicationCategory);
            associatedAppsPref = new Preference(mContext);
            associatedAppsPref.setKey(KEY_ASSOCIATED_APPS);
            associatedAppsPref.setTitle(R.string.scanner_associated_apps_title);
            associatedAppsPref.setSummary(R.string.scanner_associated_apps_summary);
            applicationCategory.addPreference(associatedAppsPref);

        }
    }
    /**
     * 悬浮按钮
     */
    private void updateVirtualButtonPreference() {
        if (root == null) return;
        if (root.findPreference(KEY_ScanVirtualButton) == null) {
            mScanVirtualButton = new SwitchPreference(mContext);
            mScanVirtualButton.setKey(KEY_ScanVirtualButton);
            mScanVirtualButton.setTitle(R.string.scanner_virtual_button_summary);
            root.addPreference(mScanVirtualButton);
            boolean isHandleMode = mApplication.getPropertyInt(mProfileId, Settings.System.SUSPENSION_BUTTON, 0) == 1;
            mScanVirtualButton.setChecked(isHandleMode);
            if(mProfileId == USettings.Profile.DEFAULT_ID){
                /*if(mScanVirtualButton.isChecked()) {
                    try{
                        Intent intentService = new Intent("com.ubx.barcode.action.SUSPENSION_BUTTON");
                        Intent eintent = new Intent(BootReceiver.getExplicitIntent(mContext, intentService));
                        if(eintent != null) {
                            mContext.startService(eintent);
                        }
                    } catch (Exception e) {
                    }
                } else {
                    try{
                        Intent intentService = new Intent("com.ubx.barcode.action.SUSPENSION_BUTTON");
                        Intent eintent = new Intent(BootReceiver.getExplicitIntent(mContext, intentService));
                        if(eintent != null) {
                            mContext.stopService(eintent);
                        }
                    } catch (Exception e) {
                    }
                }*/
            }
        }
    }
    private void updateDecodePreferenceCategory() {
        if (root == null) return;
        if (root.findPreference(KEY_SCANNER_INPUT_Category) == null) {
            scannerInputCategory = new PreferenceCategory(mContext);
            scannerInputCategory.setKey(KEY_SCANNER_INPUT_Category);
            scannerInputCategory.setTitle(R.string.scanner_input_category_title);
            root.addPreference(scannerInputCategory);
            mScannerInputEnable = new SwitchPreference(mContext);
            mScannerInputEnable.setKey(KEY_SCANNER_INPUT_ENABLE);
            mScannerInputEnable.setTitle(R.string.scanner_input_title);
            mScannerInputEnable.setSummary(R.string.scanner_input_summary);
            root.addPreference(mScannerInputEnable);
            boolean enable = mApplication.getPropertyInt(mProfileId, Settings.System.SCANNER_ENABLE, 0) == 1;
            mScannerInputEnable.setChecked(enable);
        }
    }
    /**
     * 配置扫描声音
     */
    public void updateDecodeFeedbackCategory(){
        if (root == null) return;
        if (root.findPreference(Settings.System.GOOD_READ_BEEP_ENABLE) == null) {
            mDecodeFeedbackCategory = new PreferenceCategory(mContext);
            mDecodeFeedbackCategory.setKey(Settings.System.GOOD_READ_BEEP_ENABLE);
            mDecodeFeedbackCategory.setTitle(R.string.scanner_decode_feedback);
            root.addPreference(mDecodeFeedbackCategory);
            keyboardBeepPref = new ListPreference(mContext);
            keyboardBeepPref.setKey(KEY_SCAN_SOUNDS);
            keyboardBeepPref.setTitle(R.string.scanner_beep);
            keyboardBeepPref.setEntries(R.array.scanner_beep_entries);
            keyboardBeepPref.setEntryValues(R.array.image_inverse_decoder_values);
            keyboardBeepPref.setOnPreferenceChangeListener(this);
            mDecodeFeedbackCategory.addPreference(keyboardBeepPref);
            int beepIndex = mApplication.getPropertyInt(mProfileId, Settings.System.GOOD_READ_BEEP_ENABLE, 2);
            keyboardBeepPref.setValue(String.valueOf(beepIndex));
            keyboardBeepPref.setSummary(keyboardBeepPref.getEntries()[beepIndex]);
            /*feedbackBeepCustomPref = new Preference(mContext);
            feedbackBeepCustomPref.setKey(KEY_SCAN_CUSTOM_SOUNDS);
            mDecodeFeedbackCategory.addPreference(feedbackBeepCustomPref);
            feedbackBeepChannelPref = new ListPreference(mContext);
            feedbackBeepChannelPref.setKey(KEY_SCAN_SOUNDS_CHANNEL);
            feedbackBeepChannelPref.setEntries(R.array.scanner_beep_channel_entries);
            feedbackBeepChannelPref.setEntryValues(R.array.scanner_beep_channel_values);
            feedbackBeepChannelPref.setOnPreferenceChangeListener(this);
            mDecodeFeedbackCategory.addPreference(feedbackBeepChannelPref);*/
            keyboardVibratePref = new SwitchPreference(mContext);
            keyboardVibratePref.setKey(KEY_SCAN_VIBRATE);
            keyboardVibratePref.setTitle(R.string.scanner_vibrate);
            keyboardVibratePref.setSummary(R.string.scanner_vibrate_summary);
            mDecodeFeedbackCategory.addPreference(keyboardVibratePref);
            boolean isVibrate = mApplication.getPropertyInt(mProfileId, Settings.System.GOOD_READ_VIBRATE_ENABLE, 0) == 1;
            keyboardVibratePref.setChecked(isVibrate);
        }
    }
    /**
     * 扫描触发模式
     */
    private ListPreference scanTriggerModePref = null;
    private Preference scanMultipledecode;
    private void updateScanTriggerModePreference(int scanType) {
        if (root == null) return;

        if (root.findPreference(KEY_TRIGGER_MODE) == null) {
            scanTriggerModePref = new ListPreference(mContext);
            scanTriggerModePref.setKey(KEY_TRIGGER_MODE);
            scanTriggerModePref.setTitle(R.string.scanner_trigger_mode);
            scanTriggerModePref.setEntries(R.array.scanner_triggermode_entries);
            scanTriggerModePref.setEntryValues(R.array.scanner_triggermode_values);
            scanTriggerModePref.setOnPreferenceChangeListener(this);
            root.addPreference(scanTriggerModePref);
        }
        if (ScannerAdapter.isSupportPreference(scanType, ScannerAdapter.KEY_MULTIPLE_DECODE)) {
            scanMultipledecode = new Preference(mContext);
            scanMultipledecode.setKey(ScannerAdapter.KEY_MULTIPLE_DECODE);
            scanMultipledecode.setTitle(R.string.n6603_multiple_decode_config);
            root.addPreference(scanMultipledecode);
        }
        updateScanTriggerModeState();
    }

    /**
     * 扫描模式
     */
    private void updateScanTriggerModeState() {
        int mode = mApplication.getPropertyInt(mProfileId, Settings.System.TRIGGERING_MODES, 8);
        String value = "2";
        switch (mode) {
            case 4:
                value = "1";
                break;
            case 8:
                value = "2";
                break;
            case 2:
                value = "0";
                break;
        }
        scanTriggerModePref.setValue(value);
        scanTriggerModePref.setSummary(scanTriggerModePref.getEntry());
        if (scanMultipledecode != null) {
            if (mode == Triggering.CONTINUOUS.toInt()) {
                scanMultipledecode.setEnabled(true);
            } else {
                scanMultipledecode.setEnabled(false);
            }
        }
    }

    /**
     * 手机模式
     */
    private SwitchPreference scanPhoneModePref = null;
    private void updatePhoneModePreference(int scanType) {
        if (root == null) return;
        if (scanType == 4 || scanType == 5 || scanType == 6 || scanType == 9 || scanType == 8 || scanType == 13) {
            if (root.findPreference(KEY_PHONEMODE_SCAN) == null) {
                scanPhoneModePref = new SwitchPreference(mContext);
                scanPhoneModePref.setKey(KEY_PHONEMODE_SCAN);
                scanPhoneModePref.setTitle(R.string.phonemode_title);
                scanPhoneModePref.setSummary(R.string.phonemode_summary);
                root.addPreference(scanPhoneModePref);
                boolean isPhoneMode = mApplication.getPropertyInt(mProfileId, Settings.System.IMAGE_PICKLIST_MODE, 0) == 1;
                scanPhoneModePref.setChecked(isPhoneMode);
            }
        } else {
            if (root.findPreference(KEY_PHONEMODE_SCAN) != null) {
                root.removePreference(scanPhoneModePref);
            }
        }
    }

    /**
     * 扫描操作配置
     */
    private Preference scanTriggerReadPref = null;
    private void updateScanReaderPreference(int scanType) {
        if (root == null) return;
        if (root.findPreference(KEY_TRIGGER_READ) == null) {
            scanTriggerReadPref = new Preference(mContext);
            scanTriggerReadPref.setKey(KEY_TRIGGER_READ);
            scanTriggerReadPref.setTitle(R.string.scanner_trigger_reading);
            root.addPreference(scanTriggerReadPref);
        }
    }

    /**
     * 扫描数据格式化
     */
    private Preference scannerFormatPref = null;
    private void updateScannerFormatPreference() {
        if (root == null) return;

        if (root.findPreference(KEY_FORMAT) == null) {
            scannerFormatPref = new Preference(mContext);
            scannerFormatPref.setKey(KEY_FORMAT);
            scannerFormatPref.setTitle(R.string.scanner_formatting);
            root.addPreference(scannerFormatPref);
        }
    }

    /***/
    private Preference decoderPropertiesPref = null;
    private void updateDecoderPropertiesPreference(int scanType) {
        if (root == null) return;

        if (scanType != 10) {
            if (root.findPreference(KEY_SYMBOLOGY_SET) == null) {
                decoderPropertiesPref = new Preference(mContext);
                decoderPropertiesPref.setKey(KEY_SYMBOLOGY_SET);
                decoderPropertiesPref.setTitle(R.string.scanner_symbology);
                root.addPreference(decoderPropertiesPref);
            }
        } else {
            if (root.findPreference(KEY_SYMBOLOGY_SET) != null) {
                root.removePreference(decoderPropertiesPref);
            }
        }
    }

    /***/
    private PreferenceCategory resetScannerCategory = null;
    private Preference resetScannerPref = null;
    private Preference importScannerPref = null;
    private Preference exportScannerPref = null;
    private Preference specialPropertiesPref = null;
    private void updateResetScannerCategory() {
        if (root == null) return;

        if (root.findPreference(KEY_RESET_GROUP) == null) {
            resetScannerCategory = new PreferenceCategory(mContext);
            resetScannerCategory.setKey(KEY_RESET_GROUP);
            resetScannerCategory.setTitle(R.string.scanner_reset_def);
            root.addPreference(resetScannerCategory);
        }

        if (resetScannerCategory.findPreference(KEY_RESET_SCAN) == null) {
            /*importScannerPref = new Preference(mContext);
            importScannerPref.setKey(KEY_IMPORT_CONFIG);
            importScannerPref.setTitle(R.string.import_config_keys);
            resetScannerCategory.addPreference(importScannerPref);
            exportScannerPref = new Preference(mContext);
            exportScannerPref.setKey(KEY_EXPORT_CONFIG);
            exportScannerPref.setTitle(R.string.export_config_keys);
            resetScannerCategory.addPreference(exportScannerPref);*/

            resetScannerPref = new Preference(mContext);
            resetScannerPref.setKey(KEY_RESET_SCAN);
            resetScannerPref.setTitle(R.string.scanner_reset_profile_def);
            resetScannerPref.setSummary(R.string.scanner_reset_profile_def_summary);
            resetScannerCategory.addPreference(resetScannerPref);
        }
    }
    private void updateSpecialPreference() {
        if (root == null) return;
        if (root.findPreference(KEY_SPECIAL_SETTINGS) == null) {
            specialPropertiesPref = new Preference(mContext);
            specialPropertiesPref.setKey(KEY_SPECIAL_SETTINGS);
            specialPropertiesPref.setTitle(R.string.scanner_special_settings);
            root.addPreference(specialPropertiesPref);
        }
    }
    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        if (preference == keyboardBeepPref) {
            int value = Integer.parseInt((String) o);
            mApplication.setPropertyInt(mProfileId, PropertyID.GOOD_READ_BEEP_ENABLE, Settings.System.GOOD_READ_BEEP_ENABLE, value);
            mApplication.setPropertyInt(mProfileId, PropertyID.SEND_GOOD_READ_BEEP_ENABLE, Settings.System.SEND_GOOD_READ_BEEP_ENABLE, value);
            keyboardBeepPref.setValue(String.valueOf(value));
            keyboardBeepPref.setSummary(keyboardBeepPref.getEntries()[value]);
        } else if (preference == keyboardActionKeyPref) {
            int value = Integer.parseInt((String) o);
            mApplication.setPropertyInt(mProfileId, PropertyID.LABEL_APPEND_ENTER, Settings.System.LABEL_APPEND_ENTER,value);
            keyboardActionKeyPref.setValue(String.valueOf(value));
            keyboardActionKeyPref.setSummary(keyboardActionKeyPref.getEntries()[value]);
        } else if (preference == outEditorTextMode) {
            int value = Integer.parseInt((String) o);
            mApplication.setPropertyInt(mProfileId, PropertyID.OUT_EDITORTEXT_MODE, Settings.System.OUT_EDITORTEXT_MODE, value);
            outEditorTextMode.setValue(String.valueOf(value));
            outEditorTextMode.setSummary(outEditorTextMode.getEntries()[value]);
        } else if (preference == keyboardTypePref) {
            int value = Integer.parseInt((String) o);
            mApplication.setPropertyInt(mProfileId, PropertyID.WEDGE_KEYBOARD_TYPE, Settings.System.WEDGE_KEYBOARD_TYPE,value);
            keyboardTypePref.setValue(String.valueOf(value));
            keyboardTypePref.setSummary(keyboardTypePref.getEntries()[value]);
            if(value == 3) {
                outEditorTextMode.setEnabled(true);
            } else {
                outEditorTextMode.setEnabled(false);
            }
        } else if (preference == intentActionPref) {
            String action = o.toString();
            mApplication.setPropertyString(mProfileId, PropertyID.WEDGE_INTENT_ACTION_NAME, Settings.System.WEDGE_INTENT_ACTION_NAME, action);
            intentActionPref.setSummary(action);
        } else if (preference == intentLabelPref) {
            String label = o.toString();
            mApplication.setPropertyString(mProfileId, PropertyID.WEDGE_INTENT_DATA_STRING_TAG, Settings.System.WEDGE_INTENT_DATA_STRING_TAG, label);
            intentLabelPref.setSummary(label);
        } else if (preference == intentBeepPref) {
            int value = Integer.parseInt((String) o);
            mApplication.setPropertyInt(mProfileId, PropertyID.SEND_GOOD_READ_BEEP_ENABLE, Settings.System.SEND_GOOD_READ_BEEP_ENABLE, value);
            intentBeepPref.setValue(String.valueOf(value));
            intentBeepPref.setSummary(intentBeepPref.getEntries()[value]);
        } else if (preference == scanTriggerModePref) {
            int value = Integer.parseInt((String) o);
            int mode = 8;
            switch (value) {
                case 1:
                    mode = Triggering.CONTINUOUS.toInt();
                    break;
                case 2:
                    mode = Triggering.HOST.toInt();
                    break;
                case 0:
                    mode = Triggering.PULSE.toInt();
                    ;
                    break;
            }
            if (scanMultipledecode != null) {
                if (mode == Triggering.CONTINUOUS.toInt()) {
                    scanMultipledecode.setEnabled(true);
                } else {
                    scanMultipledecode.setEnabled(false);
                }
            }
            mApplication.setPropertyInt(mProfileId, PropertyID.TRIGGERING_MODES, Settings.System.TRIGGERING_MODES, mode);
            scanTriggerModePref.setSummary(scanTriggerModePref.getEntries()[value]);
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mScannerInputEnable) {
            mApplication.setPropertyInt(mProfileId, 0, Settings.System.SCANNER_ENABLE, mScannerInputEnable.isChecked() ? 1 : 0);
            if (mProfileId == USettings.Profile.DEFAULT_ID) {
                boolean isProfileEnable = mApplication.isProfileEnable(mProfileId);
                if(mApplication.isDataWedgeEnable() && isProfileEnable) {
                    try{
                        if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
                            if (mScannerInputEnable.isChecked()) {
                                mApplication.getService().open();
                            } else {
                                mApplication.getService().close();
                            }
                        } else {
                            if (mScannerInputEnable.isChecked()) {
                                mApplication.getIService().open();
                            } else {
                                mApplication.getIService().close();
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        } else if (preference == keyboardVibratePref) {
            mApplication.setPropertyInt(mProfileId, PropertyID.GOOD_READ_VIBRATE_ENABLE, Settings.System.GOOD_READ_VIBRATE_ENABLE, keyboardVibratePref.isChecked() ? 1 : 0);
        } else if (preference == keyboardEnterPref) {
            mApplication.setPropertyInt(mProfileId, PropertyID.LABEL_APPEND_ENTER, Settings.System.LABEL_APPEND_ENTER, keyboardEnterPref.isChecked() ? 1 : 0);
        } else if (preference == intentVibratePref) {
            mApplication.setPropertyInt(mProfileId, PropertyID.SEND_GOOD_READ_VIBRATE_ENABLE, Settings.System.SEND_GOOD_READ_VIBRATE_ENABLE, intentVibratePref.isChecked() ? 1 : 0);
        } else if (preference == scanPhoneModePref) {
            mApplication.setPropertyInt(mProfileId, PropertyID.IMAGE_PICKLIST_MODE, Settings.System.IMAGE_PICKLIST_MODE, scanPhoneModePref.isChecked() ? 1 : 0);
        } else if (preference == mScanVirtualButton) {
            if(mProfileId == USettings.Profile.DEFAULT_ID){
                /*if (mScanVirtualButton.isChecked()) {
                    try {
                        Intent mIntent = new Intent();
                        mIntent.setComponent(new ComponentName("com.android.usettings", "com.android.usettings.FxService"));
                        mContext.startService(mIntent);
                    } catch (Exception e) {
                    }
                } else {
                    try {
                        Intent mIntent = new Intent();
                        mIntent.setComponent(new ComponentName("com.android.usettings", "com.android.usettings.FxService"));
                        mContext.stopService(mIntent);
                    } catch (Exception e) {
                    }
                }*/
            }
            mApplication.setPropertyInt(mProfileId, PropertyID.SUSPENSION_BUTTON, Settings.System.SUSPENSION_BUTTON, mScanVirtualButton.isChecked() ? 1:0);
        } else if (preference == intentLabelPref) {
            intentLabelPref.getEditText().setText(intentLabelPref.getSummary());
        } else if (preference == intentActionPref) {
            intentActionPref.getEditText().setText(intentActionPref.getSummary());
        } else if (preference == scanTriggerReadPref) {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 2);
            bundle.putInt("scannertype", mScanType);
            bundle.putInt("profileId", mProfileId);
            bundle.putBoolean("is-main-page", false);
            if (mScanType == 10) {
                mContext.startActivity(new Intent("android.CAMERA_SCAN_DECODE_SETTING"));
            } else {
                startFragment(this, ScannerReaderFragment.class.getCanonicalName(), -1, bundle);
            }
        } else if (preference == scannerFormatPref) {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 3);
            bundle.putInt("scannertype", mScanType);
            bundle.putInt("profileId", mProfileId);
            bundle.putBoolean("is-main-page", false);
            startFragment(this, DataFormattingSettings.class.getCanonicalName(), -1, bundle);
        } else if (preference == decoderPropertiesPref) {
            Bundle bundle = new Bundle();
            bundle.putInt("type", mScanType);
            bundle.putInt("profileId", mProfileId);
            bundle.putBoolean("is-main-page", false);
            startFragment(this, SymbologySettings.class.getCanonicalName(), -1, bundle);
        } else if (preference == resetScannerPref) {
            showResetDialog(1);
        } else if (preference == associatedAppsPref) {
            Bundle bundle = new Bundle();
            bundle.putInt("profileId", mProfileId);
            bundle.putInt("scannertype", mScanType);
            bundle.putBoolean("is-main-page", false);
            startFragment(this, AssociatedApps.class.getCanonicalName(), -1, bundle);
        } else if (scanMultipledecode == preference) {
            Bundle bundle = new Bundle();
            bundle.putInt("profileId", mProfileId);
            bundle.putInt("scannertype", mScanType);
            bundle.putBoolean("is-main-page", false);
            startFragment(this, ContinuousDecodeConfing.class.getCanonicalName(), -1, bundle);
        } else if (preference == exportScannerPref) {
            ImportExportAsyncTask task = new ImportExportAsyncTask(getActivity(), 2);
            task.execute("export");
        } else if (preference == importScannerPref) {
            ImportExportAsyncTask task = new ImportExportAsyncTask(getActivity(), 1);
            task.execute("import");
        } else if(preference == mOutputMode) {
            Bundle bundle = new Bundle();
            bundle.putInt("profileId", mProfileId);
            bundle.putInt("scannertype", mScanType);
            bundle.putBoolean("is-main-page", false);
            startFragment(this, DataOutputFrament.class.getCanonicalName(), -1, bundle);
        } else if(preference == mDecodeFeedback) {
            Bundle bundle = new Bundle();
            bundle.putInt("profileId", mProfileId);
            bundle.putInt("scannertype", mScanType);
            bundle.putBoolean("is-main-page", false);
            startFragment(this, ScanFeedbackFrament.class.getCanonicalName(), -1, bundle);
        } else if(specialPropertiesPref == preference) {
            Bundle bundle = new Bundle();
            bundle.putInt("profileId", mProfileId);
            bundle.putInt("scannertype", mScanType);
            bundle.putBoolean("is-main-page", false);
            startFragment(this, SpecialSettingsFrament.class.getCanonicalName(), -1, bundle);
        } else if(mOCRScanSettings == preference) {
            Bundle bundle = new Bundle();
            bundle.putInt("profileId", mProfileId);
            bundle.putInt("scannertype", mScanType);
            bundle.putBoolean("is-main-page", false);
            startFragment(this, OCRSettingsFragment.class.getCanonicalName(), -1, bundle);
        }
        return true;
    }

    private void showResetDialog(final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.scanner_reset_profile_def_alert);
        builder.setTitle(R.string.scanner_reset_profile_def);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ResetAsyncTask task = new ResetAsyncTask(getActivity());
                task.execute("reset");
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    class ResetAsyncTask extends AsyncTask<String, String, Integer> {
        private Context mContext;
        private ProgressDialog pDialog;

        public ResetAsyncTask(Context c) {
            mContext = c;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage(mContext.getResources().getString(R.string.scanner_reset_progress));
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                mApplication.resetScannerParameters(mProfileId);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (pDialog != null) pDialog.dismiss();
            Toast.makeText(mContext, R.string.scanner_profile_toast, Toast.LENGTH_LONG).show();
            updatePreferences();
        }
    }
    class ImportExportAsyncTask extends AsyncTask<String, String, Integer> {
        private Context mContext;
        private ProgressDialog pdialog;
        private int mAction;

        public ImportExportAsyncTask(Context c, int action) {
            mContext = c;
            mAction = action;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pdialog = new ProgressDialog(mContext);
            if(mAction == 1) {
                pdialog.setMessage(getActivity().getResources().getString(R.string.importing_config));
            } else {
                pdialog.setMessage(getActivity().getResources().getString(R.string.exporting_config));
            }
            pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pdialog.setCancelable(false);
            pdialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {

            // TODO Auto-generated method stub
            //Intent intentService = new Intent("action.EXPORT_IMPORT_SCANNER_SERVICE");
            Intent eintent = new Intent(getActivity(), ImportExoprtService.class);//new Intent(ImportExoprtReceiver.getExplicitIntent(mContext, intentService));
            eintent.putExtra("config_action", mAction);
            eintent.putExtra("profileName", mProfileName);
            try {
                mContext.startService(eintent);
            } catch (Exception e) {
                Log.e(TAG,
                        "Start ImportExoprt Service failed:" + e.getMessage());
            }
            try{
                Thread.sleep(3000);
            } catch(Exception e) {
                e.printStackTrace();
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(mAction == 1) {
                updatePreferences();
            }
            if(pdialog != null) pdialog.dismiss();
        }

    }
}
