package com.android.usettings;

import com.android.usettings.ScannerEnable.OnEnableListener;

import java.util.Iterator;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.SharedPreferences.Editor;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Build;
import android.os.IScanService;
import android.os.ServiceManager;
import android.preference.PreferenceCategory;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Switch;
import android.widget.Toast;
import android.content.ContentResolver;

//urovo add jinpu.lin 2019.05.08
import android.app.Dialog;
import android.view.View;
import android.widget.EditText;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
//urovo add end 2019.05.08

public class ScannerSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, OnEnableListener {

    private static final String TAG = "ScannerSettings";
    private static final int TYPE_SOUNDS = 1;
    private static final int TYPE_VIBRATE = 2;
    private static final int TYPE_ENTER = 3;
    private static final int TYPE_POWER = 4;
    private static final int TYPE_MODE = 5;
    private static final int TYPE_KEY = 6;

    private static final String KEY_OPRN_SCAN = "open_scanner";
    private static final String KEY_SCAN_LOCK = "lock_scan_key";
    private static final String KEY_KEYBOARD_MODE = "scanner_keyboard_output";
    private static final String KEY_SCAN_SOUNDS = "scanner_beep";
    private static final String KEY_SCAN_VIBRATE = "scanner_vibrate";
    private static final String KEY_SCAN_ENTER = "scanner_enter";
    private static final String KEY_RESET_SCAN = "reset_def";
    //  private static final String KEY_BARCODE_PARAM = "barcode_param";
    private static final String KEY_PHONEMODE_SCAN = "phonemode_scan_key";
    private static final String KEY_TRIGGER_MODE = "scanner_trigger_mode";
    private static final String KEY_KEYBOARD_TYPE = "scanner_keyboard_type";
    private static final String KEY_SYMBOLOGY_SET = "scanner_symbology_settings";
    private static final String KEY_FORMAT = "scanner_formatting";
    private static final String KEY_TRIGGER_READ = "scanner_triggering";
    private static final String KEY_CODING_FORMAT = "image_coding_format";
    private static final String KEY_INTENT_ACTION = "intent_action";
    private static final String KEY_INTENT_STRINGLABEL = "intent_stringlabel";
    private static final String KEY_SEND_SCAN_SOUNDS = "scanner_send_beep";
    private static final String KEY_SEND_SCAN_VIBRATE = "scanner_send_vibrate";
    private static final String KEY_SCAN_VIRTUAN_BUTTON = "scanner_virtual";
    private static final String KEY_WIRED_SCAN = "wired_scan";
    private static final String KEY_SCAN_APP = "scan_app_key";
    private static final String KEY_SCANHANDLE = "scanhandle_toggle";
    private static final String KEY_WEBJUMPSWITCH = "webjump_switch";
    private static final String KEY_YTO_SIXLEN = "yto_sixlen_key";
    private static final String KEY_YTO_LIMIT_LEN = "yto_limit_len";
    // urovo add shenpidong begin 2019-09-29
    private static final String KEY_HANDLE_SQ53 = "/sys/devices/soc/soc:gpio_keys/pogo_key";
    private static final String KEY_HANDLE_DEFAULT = "/sys/devices/soc/qpnp-smbcharger-17/usbid_scankey";
    private static final String KEY_HANDLE_SQ53H = "sys/devices/platform/ext_power_otg/pogo_key";
    // urovo add shenpidong end 2019-09-29
    // urovo add by shenpidong begin 2020-04-10
    private static final String KEY_SCAN_DELAYTRIGGER_KEY = "persist.scan.delaytrigger";
    private static final String KEY_SCAN_DELAYTAIM_KEY = "persist.scan.delayaim";
    // urovo add by shenpidong end 2020-04-10
    private CheckBoxPreference mScanVirtual;
    private CheckBoxPreference mScanHandle;
    private CheckBoxPreference mScanner;
    private CheckBoxPreference mScanKey;
    private CheckBoxPreference mScanOutput;
    private CheckBoxPreference mClipboard;
    private ListPreference mScanSounds;
    private CheckBoxPreference mScanVibrate;
    private CheckBoxPreference mScanEnter;
    private ListPreference mScanActionKey;
    private CheckBoxPreference mScanPhoneMode;
    private CheckBoxPreference mYTOSixLen;
    private EditTextPreference mYTOLen;
    private ListPreference mScanTriggerMode;
    private ListPreference mCodingFormat;
    private PreferenceScreen root;
    private Preference mResetScan;
    private Preference mImportScan;
    private Preference mExportScan;
    //private Preference mScanType;
    //private Preference mBarcode_param;
    private ScanManager mScanManager;
    private Preference mDecoderProperies;
    private Preference mUDIFormatting;
    private Preference mScannerFormatting;
    private Preference mScannerTriggerRead;
    private ListPreference mScanKeyboardType;
    private ListPreference outEditorTextMode;
    private EditTextPreference mIntentAction;
    private EditTextPreference mIntentLabel;
    private ListPreference mScanSendSounds;
    private CheckBoxPreference mScanSendVibrate;
    private CheckBoxPreference mScanApp;
    private ListPreference mWiredScan;
    private Preference n6603multipledecode;//n6603_multiple_decode

    private CheckBoxPreference mWebJumpSwitch;

    private Preference scannerMultipledecode;
    private boolean isReset;
    private int type;
    private ScannerEnable mScannerEnable;
    //private int scanPhoneModeValue;
    //private int scannerType;
    //private SettingsObserver mSettingsObserver;
    public static final String SCAN_SETTING_CHANGE =
            "scan.setting.change";
    private IntentFilter mIntentFilter;
    private SharedPreferences sp;
    Context mContext = getActivity();
    private static int mScanType;
    private IScanService mService;
 
    //urovo add jinpu.lin 2019.05.08   
    private Preference output_data_delay;
    private Preference keyevent_enter_delay;
    //urovo add end 2019.05.08
    private String intentAction = null;
    private String serverUrl = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        root = this.getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.scanner_settings);
        root = this.getPreferenceScreen();
        mService = IScanService.Stub.asInterface(ServiceManager.getService(Context.SCAN_SERVICE));

        mScanHandle = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SCANHANDLE);
        mScanVirtual = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SCAN_VIRTUAN_BUTTON);
        mWiredScan = (ListPreference) getPreferenceScreen().findPreference(KEY_WIRED_SCAN);
        mScanner = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_OPRN_SCAN);
        mScanKey = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SCAN_LOCK);
        mScanOutput = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_KEYBOARD_MODE);
        mClipboard = (CheckBoxPreference) getPreferenceScreen().findPreference("scanner_keyboard_copy");
        //mScanSounds = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SCAN_SOUNDS);
        mScanVibrate = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SCAN_VIBRATE);
        //mScanSendSounds = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SEND_SCAN_SOUNDS);
        mScanSendVibrate = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SEND_SCAN_VIBRATE);
        //mScanEnter = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SCAN_ENTER);
        mScanActionKey = (ListPreference) getPreferenceScreen().findPreference("scanner_keyboard_action_character");
        mScanPhoneMode = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_PHONEMODE_SCAN);
        mScanTriggerMode = (ListPreference) getPreferenceScreen().findPreference(KEY_TRIGGER_MODE);
        mResetScan = (Preference) getPreferenceScreen().findPreference(KEY_RESET_SCAN);
        mImportScan = (Preference) getPreferenceScreen().findPreference("import_config");
        mExportScan = (Preference) getPreferenceScreen().findPreference("export_config");
//      mScanType = (Preference) getPreferenceScreen().findPreference("scantype");
//      mBarcode_param = (Preference) getPreferenceScreen().findPreference(KEY_BARCODE_PARAM);
        mIntentAction = (EditTextPreference) getPreferenceScreen().findPreference(KEY_INTENT_ACTION);
        mIntentLabel = (EditTextPreference) getPreferenceScreen().findPreference(KEY_INTENT_STRINGLABEL);
        mDecoderProperies = (Preference) getPreferenceScreen().findPreference(KEY_SYMBOLOGY_SET);
        mUDIFormatting = (Preference) getPreferenceScreen().findPreference("scanner_udi_format");
        mScannerFormatting = (Preference) getPreferenceScreen().findPreference(KEY_FORMAT);
        mScannerTriggerRead = (Preference) getPreferenceScreen().findPreference(KEY_TRIGGER_READ);
        n6603multipledecode = (Preference) getPreferenceScreen().findPreference("n6603_multiple_decode");
        mCodingFormat = (ListPreference) getPreferenceScreen().findPreference(KEY_CODING_FORMAT);
        mWebJumpSwitch = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_WEBJUMPSWITCH);
        
        //urovo add jinpu.lin 2019.05.08
        PreferenceCategory keyboard_output = (PreferenceCategory)getPreferenceScreen().findPreference("preferencecategory_keyboard_output");
        output_data_delay= (Preference) getPreferenceScreen().findPreference("output_data_delay");
        keyevent_enter_delay= (Preference) getPreferenceScreen().findPreference("keyevent_enter_delay");
        scannerMultipledecode = (Preference) getPreferenceScreen().findPreference("scanner_multiple_decode_mode");
        if(!Build.PWV_CUSTOM_CUSTOM.equals("QCS")) {
            keyboard_output.removePreference(output_data_delay);
            keyboard_output.removePreference(keyevent_enter_delay);
        }
        //urovo add end  2019.05.08
        
        mCodingFormat.setOnPreferenceChangeListener(this);
        mIntentAction.setOnPreferenceChangeListener(this);
        mIntentLabel.setOnPreferenceChangeListener(this);
        mScanKeyboardType = (ListPreference) getPreferenceScreen().findPreference(KEY_KEYBOARD_TYPE);
        mScanKeyboardType.setOnPreferenceChangeListener(this);
        outEditorTextMode= (ListPreference) getPreferenceScreen().findPreference("scanner_out_editortext_mode");
        outEditorTextMode.setOnPreferenceChangeListener(this);

        mScanApp = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_SCAN_APP);
        mScanApp.setOnPreferenceChangeListener(this);
        // urovo add shenpidong begin 2019-05-11
        if(mScanApp != null) {
            getPreferenceScreen().removePreference(mScanApp);
        }
        // urovo add shenpidong end 2019-05-11
        getPreferenceScreen().removePreference(mScanKey);
        getPreferenceScreen().removePreference(mScanner);
        if (!Build.PROJECT.equals("SQ46")) {
            getPreferenceScreen().removePreference(mWiredScan);
        }
        mYTOSixLen = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_YTO_SIXLEN);
        mYTOLen = (EditTextPreference) getPreferenceScreen().findPreference(KEY_YTO_LIMIT_LEN);
        if (!Build.PWV_CUSTOM_CUSTOM.equals("YTO")) {
            getPreferenceScreen().removePreference(mYTOSixLen);
            getPreferenceScreen().removePreference(mYTOLen);
        }else{
            mYTOSixLen.setOnPreferenceChangeListener(this);
            mYTOLen.setOnPreferenceChangeListener(this);
        }
        mScanSounds = (ListPreference) getPreferenceScreen().findPreference(KEY_SCAN_SOUNDS);
        mScanSendSounds = (ListPreference) getPreferenceScreen().findPreference(KEY_SEND_SCAN_SOUNDS);
        mScanSounds.setOnPreferenceChangeListener(this);
        mScanSendSounds.setOnPreferenceChangeListener(this);
        mScanTriggerMode.setOnPreferenceChangeListener(this);
        mWiredScan.setOnPreferenceChangeListener(this);
        mScanActionKey.setOnPreferenceChangeListener(this);
        // urovo modified by shenpidong begin 2020-09-17
        if (Build.PROJECT.equals("SQ45") || Build.PROJECT.equals("SQ53") || Build.PROJECT.equals("SQ47") || Build.PROJECT.equals("SQ45S") || Build.PROJECT.equals("SQ81")) {
            getPreferenceScreen().removePreference(mScanHandle);
        }else{
            mScanHandle.setOnPreferenceChangeListener(this);
        }
        // urovo add shenpidong end 2019-07-11
        refleshBarSwitch();
        mContext = getActivity();
        mScanManager = new ScanManager();
        updateUI();
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            intentAction = intent.getAction();
            serverUrl = intent.getStringExtra("ServerUrl");
            //mIntentFlags = intent.getFlags();//0x8000
            //Log.v(TAG, "onCreate Action " + intent.toString());
        }
    }

    // urovo add shenpidong begin 2019-07-11
    private void refleshBarSwitch() {
        final Activity activity = getActivity();
        Switch actionBarSwitch = new Switch(activity);
        if (activity instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity) activity;
            if (preferenceActivity.onIsHidingHeaders() || !preferenceActivity.onIsMultiPane()) {
                final int padding = activity.getResources().getDimensionPixelSize(
                        R.dimen.action_bar_switch_padding);
                actionBarSwitch.setPadding(0, 0, padding, 0);
                activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                        ActionBar.DISPLAY_SHOW_CUSTOM);
                activity.getActionBar().setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        ActionBar.LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER_VERTICAL | Gravity.RIGHT));
            }
        }

        mScannerEnable = new ScannerEnable(activity, actionBarSwitch);
        mScannerEnable.setSwitch(actionBarSwitch);
        mScannerEnable.setOnEnableListener(this);

        mIntentFilter = new IntentFilter(SCAN_SETTING_CHANGE);
    }
    // urovo add shenpidong end 2019-07-11

    private void updateCheckBox(CheckBoxPreference checkBox, boolean value) {
        checkBox.setChecked(value);
    }

    private void updateUI() {
        mScanType = mScanManager.getScannerType();
        if(mScanType == 11){
            mScanType = 13;
        }
        // urovo add shenpidong begin 2019-09-12
        if (mScanType != 4 && mScanType != 6 && mScanType != 9 && mScanType != 13 && mScanType != 7) {
            getPreferenceScreen().removePreference(mScanPhoneMode);
        }
        if (mScanType != 4 && mScanType != 9 && mScanType != 6 && mScanType != 8 && mScanType != 5 && mScanType != 11 && mScanType != 10 && mScanType != 13 && mScanType != 7 && mScanType != 15) {
            getPreferenceScreen().removePreference(mCodingFormat);
        }
        if (mScanType != 4 && mScanType != 5 && mScanType != 11 && mScanType != 9 && mScanType != 2 && mScanType != 3 && mScanType != 6 && mScanType != 8 && mScanType != 13 && mScanType != 7 && mScanType != 15) {
            if (mScanType != 10/* && mScanType != 5*/) {
                getPreferenceScreen().removePreference(mScanTriggerMode);
            }
            if (mScanType != 10) {
                getPreferenceScreen().removePreference(mScannerTriggerRead);
            }
        }
        if (mScanType != 5 && mScanType != 8 && mScanType != 11 && mScanType != 12 && mScanType != 15 && n6603multipledecode != null) {
            getPreferenceScreen().removePreference(n6603multipledecode);
        } else {
            n6603multipledecode.setEnabled(false);
        }
        if(mScanType != 15 && scannerMultipledecode != null) {
            getPreferenceScreen().removePreference(scannerMultipledecode);
        }
        // urovo add shenpidong end 2019-09-12
    }

    private void updateState() {
        if (mScanManager == null)
            return;
        int power = android.provider.Settings.System.getInt(mContext.getContentResolver(), "urovo_scan_stat", 1);
        //updateCheckBox(mScanSounds,
        //      mScanManager.getOutputParameter(TYPE_SOUNDS) != 0);
        updateCheckBox(mScanVibrate,
                mScanManager.getOutputParameter(TYPE_VIBRATE) != 0);
        //updateCheckBox(mScanEnter,
        //      mScanManager.getOutputParameter(TYPE_ENTER) != 0);
        
        mScanActionKey.setValue(String.valueOf(mScanManager.getOutputParameter(TYPE_ENTER)));
        mScanActionKey.setSummary( mScanActionKey.getEntry());
        // updateCheckBox(mScanner, mScanManager.getOutputParameter(TYPE_POWER)
        // != 0);
        updateCheckBox(
                mScanOutput,
                mScanManager.getOutputParameter(TYPE_MODE) != 0);
        if(android.os.Build.PWV_CUSTOM_CUSTOM.equals("JANAM") && mClipboard!=null){
            int copystate= android.device.provider.Settings.System.getInt(mContext.getContentResolver(),"ClipData_enable", 0);
            updateCheckBox(mClipboard, copystate!= 0);
            mClipboard.setEnabled(power != 0);
        }
        mResetScan.setEnabled(power != 0);
        mExportScan.setEnabled(mResetScan.isEnabled());
        mImportScan.setEnabled(mResetScan.isEnabled());
        mScanOutput.setEnabled(power != 0);
        
        boolean state = power != 0;//mScanManager.getScannerState();
        // updateCheckBox(mScanKey, mScanManager.getOutputParameter(TYPE_KEY) !=
        // 0);
        Log.d(TAG, "power" +state);
        if (mScanType == 4 || mScanType == 9 || mScanType == 2 || mScanType == 3 || mScanType == 6 || mScanType == 8 || mScanType == 5 || mScanType == 11 || mScanType == 10 || mScanType == 13 || mScanType == 7 || mScanType == 15) {
            /*updateCheckBox(mScanTriggerMode,
                    mScanManager.getTriggerMode() == Triggering.CONTINUOUS);*/
            Triggering mode = mScanManager.getTriggerMode();
            if (false && mScanType == 8) {
                if (mode == Triggering.CONTINUOUS) {
                    //mScanTriggerMode.setValue("1");
                    //mScanTriggerMode.setSummary( mScanTriggerMode.getEntry());
                } else if (mode == Triggering.HOST) {
                    mScanTriggerMode.setValue("1");
                    mScanTriggerMode.setSummary(mScanTriggerMode.getEntry());
                } else if (mode == Triggering.PULSE) {
                    mScanTriggerMode.setValue("0");
                    mScanTriggerMode.setSummary(mScanTriggerMode.getEntry());
                }
            } else {
                if (mode == Triggering.CONTINUOUS) {
                    mScanTriggerMode.setValue("1");
                    mScanTriggerMode.setSummary(mScanTriggerMode.getEntry());
            // urovo add shenpidong begin 2019-09-12
                    if (state && (mScanType == 5 || mScanType == 8 || mScanType == 11 || mScanType == 12 || mScanType == 15))
                        n6603multipledecode.setEnabled(true);
            // urovo add shenpidong end 2019-09-12
                } else if (mode == Triggering.HOST) {
                    mScanTriggerMode.setValue("2");
                    mScanTriggerMode.setSummary(mScanTriggerMode.getEntry());
            // urovo add shenpidong start 2019-10-15
            if(n6603multipledecode!=null) {
            n6603multipledecode.setEnabled(false);
            }
            // urovo add shenpidong end 2019-10-15
                } else if (mode == Triggering.PULSE) {
                    mScanTriggerMode.setValue("0");
                    mScanTriggerMode.setSummary(mScanTriggerMode.getEntry());
            // urovo add shenpidong start 2019-10-15
            if(n6603multipledecode!=null) {
            n6603multipledecode.setEnabled(false);
            }
            // urovo add shenpidong end 2019-10-15
                }
            }

        }
        if (mScanType == 4 || mScanType == 9 || mScanType == 6 || mScanType == 8 || mScanType == 5 || mScanType == 11 || mScanType == 10 || mScanType == 13 || mScanType == 7 || mScanType == 15) {
            int[] id = new int[]{PropertyID.IMAGE_PICKLIST_MODE, PropertyID.CODING_FORMAT};
            int[] value = new int[2];
            mScanManager.getPropertyInts(id, value);
            updateCheckBox(mScanPhoneMode, value[0] == 1);
            mCodingFormat.setValue(String.valueOf(value[1]));
            mCodingFormat.setSummary(mCodingFormat.getEntry());
        }
        int[] id = new int[]{PropertyID.WEDGE_KEYBOARD_TYPE,
            PropertyID.SEND_GOOD_READ_BEEP_ENABLE,
            PropertyID.SEND_GOOD_READ_VIBRATE_ENABLE,
            PropertyID.GOOD_READ_BEEP_ENABLE,
            PropertyID.OUT_EDITORTEXT_MODE
            };
        int[] value = new int[5];
        mScanManager.getPropertyInts(id, value);
        if (value != null) {
            int keyboardType = value[0];
            mScanKeyboardType.setValue(String.valueOf(keyboardType));
            mScanKeyboardType.setSummary(mScanKeyboardType.getEntry());
            int soundType = value[1];
            mScanSendSounds.setValue(String.valueOf(soundType));
            mScanSendSounds.setSummary(mScanSendSounds.getEntry());
            //updateCheckBox(mScanSendSounds, value[1] != 0);
            updateCheckBox(mScanSendVibrate, value[2] != 0);
            int soundid = value[3];
            mScanSounds.setValue(String.valueOf(soundid));
            mScanSounds.setSummary(mScanSounds.getEntry());
            if(outEditorTextMode != null) {
                outEditorTextMode.setValue(String.valueOf(value[4]));
                outEditorTextMode.setSummary( outEditorTextMode.getEntry());
                if(keyboardType != 3) {
                    outEditorTextMode.setEnabled(false);
                } else {
                    outEditorTextMode.setEnabled(true&&state);
                }
            }
        }
        int[] idbuf = new int[]{PropertyID.WEDGE_INTENT_ACTION_NAME, PropertyID.WEDGE_INTENT_DATA_STRING_TAG};
        String[] value_buf = mScanManager.getParameterString(idbuf);
        if (value_buf != null) {
            mIntentAction.setSummary(value_buf[0]);
            mIntentLabel.setSummary(value_buf[1]);
        }
        boolean mkeyout = mScanOutput.isChecked();
        mIntentAction.setEnabled(!mkeyout && state);
        mIntentLabel.setEnabled(!mkeyout && state);
        mScanSendSounds.setEnabled(!mkeyout && state);
        mScanSendVibrate.setEnabled(!mkeyout && state);
        mScannerFormatting.setEnabled(state);
        mScanTriggerMode.setEnabled(state);
        mScannerFormatting.setEnabled(state);
        mCodingFormat.setEnabled(state);
        mScanVirtual.setEnabled(state);
        mScanHandle.setEnabled(state);
        mWiredScan.setEnabled(state);
        mScannerTriggerRead.setEnabled(state);
        mScanPhoneMode.setEnabled(state);
        mDecoderProperies.setEnabled(state);
        mUDIFormatting.setEnabled(state);
        mYTOSixLen.setChecked(!"disabled".equals(android.device.provider.Settings.System.getString(mContext.getContentResolver(),
                "SIX_LEN_ENABLED")));
        String len = android.device.provider.Settings.System.getString(mContext.getContentResolver(),
                "YTO_LEN");
        if(TextUtils.isEmpty(len)){
            len = "6";
        }
        mYTOLen.setText(len);
        mYTOLen.setSummary(len);
        mYTOSixLen.setEnabled(state);
        mYTOLen.setEnabled(state && mYTOSixLen.isChecked());
        int scanApp = android.provider.Settings.System.getInt(mContext.getContentResolver(), "urovo_scan_app", 0);
        if (mScanApp != null) {
            if (scanApp == 0) {
                mScanApp.setChecked(false);
            } else {
                mScanApp.setChecked(true);
            }
        }
        //urovo add jinpu.lin 2019.05.08
        int[] get_id_buffer = {PropertyID.APPEND_ENTER_DELAY,PropertyID.CHARACTER_DATA_DELAY, PropertyID.OUT_CLIPBOARD_ENABLE};
        int[] get_value_buffer = new int[3];
        mScanManager.getPropertyInts(get_id_buffer,get_value_buffer);
        if(get_value_buffer[1] < 0)
            get_value_buffer[1] = 0;
        if(get_value_buffer[0] < 0)
            get_value_buffer[0] = 0; 
        updateSummay(output_data_delay,get_value_buffer[1]);
        updateSummay(keyevent_enter_delay,get_value_buffer[0]);
        //urovo add end 2019.05.08
        updateCheckBox(mClipboard, get_value_buffer[2] == 1);
        mClipboard.setEnabled(state);
        mWebJumpSwitch.setEnabled(state);
        if(mScanType == 15 && scannerMultipledecode != null) {
            scannerMultipledecode.setEnabled(state);
        }
        int mWenJumpEnable = android.device.provider.Settings.System.getInt(mContext.getContentResolver(), android.device.provider.Settings.System.WEBJUMP, 0);
        updateCheckBox(mWebJumpSwitch,mWenJumpEnable == 1);
    }

    private void recvHander(boolean state) {
        Log.d(TAG, "recvHander" +state);
        mResetScan.setEnabled(state);
        mExportScan.setEnabled(mResetScan.isEnabled());
        mImportScan.setEnabled(mResetScan.isEnabled());
        mScanOutput.setEnabled(state);
        mDecoderProperies.setEnabled(state);
        mUDIFormatting.setEnabled(state);
        if(mScanType == 15 && scannerMultipledecode != null) {
            scannerMultipledecode.setEnabled(state);
        }
        if ((mScanType == 4 || mScanType == 9 || mScanType == 6 || mScanType == 13) && mScanPhoneMode != null) {
            mScanPhoneMode.setEnabled(state);
        }
    // urovo add shenpidong end 2019-09-12
        if (state && !mScanOutput.isChecked()) {
            mIntentAction.setEnabled(true);
            mIntentLabel.setEnabled(true);
            mScanSendSounds.setEnabled(true);
            mScanSendVibrate.setEnabled(true);
        } else {
            mIntentAction.setEnabled(false);
            mIntentLabel.setEnabled(false);
            mScanSendSounds.setEnabled(false);
            mScanSendVibrate.setEnabled(false);
        }
        mScannerFormatting.setEnabled(state);
        mScanTriggerMode.setEnabled(state);
        mScannerFormatting.setEnabled(state);
        mCodingFormat.setEnabled(state);
        mScanVirtual.setEnabled(state);
        mScanHandle.setEnabled(state);
        mWiredScan.setEnabled(state);
        mScannerTriggerRead.setEnabled(state);
        mScanPhoneMode.setEnabled(state);
        // urovo add shenpidong begin 2019-09-12
        if ((mScanType == 5 || mScanType == 8 || mScanType == 11 || mScanType == 12 || mScanType == 15)) {
            String val = mScanTriggerMode.getValue();
            if (val.equals("1")) {
                n6603multipledecode.setEnabled(state);
            }
        }
        mClipboard.setEnabled(state);
    }

    private final static int PHONE_MODE = 1;
    private final static int CONTINUOUS_MODE = 2;

    private void updateToggles(int function) {
        if (function == PHONE_MODE) {
            mScanPhoneMode.setChecked(true);
            /*if(mScanTriggerMode != null && mScanTriggerMode.isChecked()) {
                mScanTriggerMode.setChecked(false);
                mScanManager.setTriggerMode(Triggering.HOST);
            }*/
        } else if (function == CONTINUOUS_MODE) {
            //mScanTriggerMode.setChecked(true);
            /*if(mScanPhoneMode != null && mScanPhoneMode.isChecked()) {
                int[] id = new int[]{PropertyID.IMAGE_PICKLIST_MODE};
                int[] value = new int[] {0};
                mScanManager.setPropertyInts(id, value);
                mScanPhoneMode.setChecked(false);
            }*/
        }
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        intentAction = null;
        if (mScannerEnable != null) {
            mScannerEnable.pause();
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // add by tao.he for actibar sync issue
        refleshBarSwitch();
        updateActionBar();
        // end add
        if (mScannerEnable != null) {
            mScannerEnable.resume();
        }
        // urovo add shenpidong begin 2019-04-12
        updateScanHandle(false);
        // urovo add shenpidong end 2019-04-12
        updateState();
        updateWiredScanListPreference();
        updateVituanButton();
        if (intentAction != null && "com.ubx.scanner.LICENSE_ACTIVATION".equals(intentAction)) {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 2);
            bundle.putInt("scannertype", mScanType);
            bundle.putString("ServerUrl", serverUrl);
            startFragment(this, ScannerReaderParams.class.getCanonicalName(), -1, bundle);
        }
    }

    // add by tao.he for actibar sync issue
    private void updateActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
        }
        getActivity().setTitle(R.string.scanner_settings);
    }
    //end add

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        if(preference==mClipboard){
            /*if (mScanOutput.isChecked()) {
                android.device.provider.Settings.System.putInt(mContext.getContentResolver(),"ClipData_enable", 1);
            } else {
                android.device.provider.Settings.System.putInt(mContext.getContentResolver(),"ClipData_enable", 0);
            }*/
            int[] id = new int[]{PropertyID.OUT_CLIPBOARD_ENABLE};
            int[] valueBuf = new int[1];
            valueBuf[0] = mClipboard.isChecked() ? 1 : 0;
            mScanManager.setPropertyInts(id, valueBuf);
        }else if (preference == mScanOutput) {
            if (mScanOutput.isChecked()) {
                mIntentAction.setEnabled(false);
                mIntentLabel.setEnabled(false);
                mScanSendSounds.setEnabled(false);
                mScanSendVibrate.setEnabled(false);
                mScanManager.setOutputParameter(TYPE_MODE, 1);
            } else {
                mIntentAction.setEnabled(true);
                mIntentLabel.setEnabled(true);
                mScanSendSounds.setEnabled(true);
                mScanSendVibrate.setEnabled(true);
                mScanManager.setOutputParameter(TYPE_MODE, 0);
            }

        } else if (preference == mScanVibrate) {
            if (mScanVibrate.isChecked()) {
                mScanManager.setOutputParameter(TYPE_VIBRATE, 1);
            } else {
                mScanManager.setOutputParameter(TYPE_VIBRATE, 0);
            }
        } else if (preference == mScanEnter) {
            if (mScanEnter.isChecked()) {
                mScanManager.setOutputParameter(TYPE_ENTER, 1);
            } else {
                mScanManager.setOutputParameter(TYPE_ENTER, 0);
            }
        } else if (preference == mResetScan) {
            showResetDialog(1);
        } else if (preference == mExportScan) {
            ImportExportAsyncTask task = new ImportExportAsyncTask(getActivity(), 2);
            task.execute("export");
        } else if (preference == mImportScan) {
            ImportExportAsyncTask task = new ImportExportAsyncTask(getActivity(), 1);
            task.execute("import");
        } else if (preference == mScanVirtual) {
            if (mScanVirtual.isChecked()) {
                android.device.provider.Settings.System.putInt(mContext.getContentResolver(), android.device.provider.Settings.System.SUSPENSION_BUTTON, 1);
            } else {
                android.device.provider.Settings.System.putInt(mContext.getContentResolver(), android.device.provider.Settings.System.SUSPENSION_BUTTON, 0);
            }
        } else if (preference == mScanPhoneMode) {
            int[] id = new int[]{PropertyID.IMAGE_PICKLIST_MODE};
            int[] value = new int[1];
            value[0] = mScanPhoneMode.isChecked() ? 1 : 0;
            if (mScanPhoneMode.isChecked()) {
                updateToggles(PHONE_MODE);
            }
            mScanManager.setPropertyInts(id, value);
        } else if (preference == mIntentAction) {
            mIntentAction.getEditText().setText(mIntentAction.getSummary());

        } else if (preference == mIntentLabel) {
            mIntentLabel.getEditText().setText(mIntentLabel.getSummary());

        }/* else if (preference == mScanSendSounds) {
            int[] id = new int[]{PropertyID.SEND_GOOD_READ_BEEP_ENABLE};
            int[] value = new int[1];
            value[0] = mScanSendSounds.isChecked() ? 1 : 0;
            mScanManager.setPropertyInts(id, value);
        }*/ else if (preference == mScanSendVibrate) {
            int[] id = new int[]{PropertyID.SEND_GOOD_READ_VIBRATE_ENABLE};
            int[] value = new int[1];
            value[0] = mScanSendVibrate.isChecked() ? 1 : 0;
            mScanManager.setPropertyInts(id, value);
        }/* else if(preference == mScanTriggerMode) {
            if (mScanTriggerMode.isChecked()) {
                updateToggles(CONTINUOUS_MODE);
                mScanManager.setTriggerMode(Triggering.CONTINUOUS);
            } else {
                mScanManager.setTriggerMode(Triggering.HOST);
            }
        }*/ else if (preference == mDecoderProperies) {
            Bundle bundle = new Bundle();
            bundle.putInt("type", mScanType);
            startFragment(this, SymbologySettings.class.getCanonicalName(), -1, bundle);
        } else if (preference == mScannerFormatting) {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 3);
            bundle.putInt("scannertype", mScanType);
            startFragment(this, ScannerReaderParams.class.getCanonicalName(), -1, bundle);
        } else if (preference == mScannerTriggerRead) {
            Bundle bundle = new Bundle();
            bundle.putInt("type", 2);
            bundle.putInt("scannertype", mScanType);
            if (mScanType == 10) {
                getActivity().startActivity(new Intent("android.CAMERA_SCAN_DECODE_SETTING"));
            } else {
                startFragment(this, ScannerReaderParams.class.getCanonicalName(), -1, bundle);
            }
        } else if (n6603multipledecode == preference) {
            //showMutipleDecodeDialog();
            Bundle bundle = new Bundle();
            bundle.putInt("type", 2);
            bundle.putInt("scannertype", mScanType);
            startFragment(this, ContinuousDecodeConfing.class.getCanonicalName(), -1, bundle);
        } else if (output_data_delay == preference) {
            showEditDialog(R.string.output_data_delay, PropertyID.CHARACTER_DATA_DELAY, 0);
        } else if (keyevent_enter_delay == preference) {
            showEditDialog(R.string.keyevent_enter_delay, PropertyID.APPEND_ENTER_DELAY, 0);
        } else if(scannerMultipledecode == preference) {
            //showMutipleDecodeDialog();
            Bundle bundle = new Bundle();
            bundle.putInt("type", 3);
            bundle.putInt("scannertype", mScanType);
            startFragment(this, ContinuousDecodeConfing.class.getCanonicalName(), -1, bundle);
        } else if(mUDIFormatting == preference) {
            //showMutipleDecodeDialog();
            Bundle bundle = new Bundle();
            bundle.putInt("type", 4);
            bundle.putInt("scannertype", mScanType);
            startFragment(this, ContinuousDecodeConfing.class.getCanonicalName(), -1, bundle);
        } else if(mWebJumpSwitch == preference) {
            int[] id = new int[]{PropertyID.WEBJUMP};
            int[] valueBuf = new int[1];
            valueBuf[0] = mWebJumpSwitch.isChecked() ? 1 : 0;
            mScanManager.setPropertyInts(id, valueBuf);
        }
        return true;
    }

    private void updateScanHandle(boolean reset) {
        if (mScanHandle != null) {
        int value = 0;
        if(reset) {
            value = 1;
            setScanHandleEnabled(mContext , true);
        } else {
            ContentResolver mContentResolver = mContext.getContentResolver();
            value =android.device.provider.Settings.System.getInt(mContentResolver, android.device.provider.Settings.System.SCAN_HANDLE, 1);
            if (value == 1) {
                if (isScanHandleEnable() == false) {
                    setScanHandleEnabled(mContext , true);
                }
            } else {
                setScanHandleEnabled(mContext , false);
            }
        }
            mScanHandle.setChecked(value==1);
        }
    }

    // urovo add shenpidong begin 2020-02-14
    private void resetGSCode() {
        int[] key = new int[]{PropertyID.SPECIFIC_CODE_GS};
        int[] value = new int[1];
        value[0] = 0;
        mScanManager.setPropertyInts(key , value);
    }
    // urovo add by shenpidong end 2020-10-16
    
    // urovo add shenpidong begin 2019-07-12
    private void resetClipboard() {
        if (android.os.Build.PWV_CUSTOM_CUSTOM.equals("JANAM") && mClipboard != null) {
            int copystate = android.device.provider.Settings.System.getInt(mContext.getContentResolver(), "ClipData_enable", 0);
            if (copystate == 1) {
                android.device.provider.Settings.System.putInt(mContext.getContentResolver(), "ClipData_enable", 0);
            }
            updateCheckBox(mClipboard, false);
        }
    }
    // urovo add shenpidong end 2019-07-12

    public void updateVituanButton() {
        int value =android.device.provider.Settings.System.getInt(mContext.getContentResolver(), android.device.provider.Settings.System.SUSPENSION_BUTTON, 0);
        if (value==1) {
            mScanVirtual.setChecked(true);
        } else {
            mScanVirtual.setChecked(false);
        }
    }
    private void showResetDialog(final int type) {
        AlertDialog.Builder builder = new Builder(getActivity());
        builder.setMessage(R.string.scanner_reset_def_alert);
        builder.setTitle(R.string.scanner_reset_def);
        builder.setPositiveButton(android.R.string.yes, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ResetAsyncTask task = new ResetAsyncTask(getActivity());
                task.execute("reset");
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    class ResetAsyncTask extends AsyncTask<String, String, Integer> {
        private Context mContext;
        private ProgressDialog pdialog;

        public ResetAsyncTask(Context c) {
            mContext = c;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pdialog = new ProgressDialog(mContext);
            pdialog.setMessage(mContext.getResources().getString(R.string.scanner_reset_progress));
            pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pdialog.setCancelable(false);
            pdialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                mScanManager.setOutputParameter(8, 1);
                // urovo add shenpidong begin 2019-04-12 , reset scan_app_key 
                android.provider.Settings.System.putInt(mContext.getContentResolver(), "urovo_scan_app", 0);
                // urovo add shenpidong end 2019-04-12
                // urovo add by shenpidong begin 2020-04-10
                if(mScanType == 8) {
                    android.provider.Settings.System.putInt(mContext.getContentResolver() , KEY_SCAN_DELAYTRIGGER_KEY , 0);
                    android.provider.Settings.System.putInt(mContext.getContentResolver() , KEY_SCAN_DELAYTAIM_KEY , 0);
                }
                // urovo add by shenpidong end 2020-04-10
                if (Build.PWV_CUSTOM_CUSTOM.equals("YTO")) {
                    android.device.provider.Settings.System.putString(mContext.getContentResolver(), "SIX_LEN_ENABLED", "enabled");
                    android.device.provider.Settings.System.putString(mContext.getContentResolver(), "YTO_LEN", "6");
                    mScanManager.setOutputParameter(TYPE_MODE, 0);
                    int[] id = new int[]{PropertyID.GOOD_READ_BEEP_ENABLE};
                    int[] sendid = new int[]{PropertyID.SEND_GOOD_READ_BEEP_ENABLE};
                    int[] valueBuf = new int[1];
                    valueBuf[0] = 0;
                    int[] id_buffer = new int[] {
                            PropertyID.CODE128_LENGTH1,
                            PropertyID.CODE39_LENGTH1,
                            PropertyID.CODE93_LENGTH1,
                            PropertyID.CODE11_LENGTH1
                    };
                    int[] value_buffer = new int[] {
                            7,
                            7,
                            7,
                            7
                    };
                    mScanManager.setPropertyInts(id, valueBuf);
                    mScanManager.setPropertyInts(sendid, valueBuf);
                    mScanManager.setPropertyInts(id_buffer, value_buffer);
                }
                // urovo add by shenpidong begin 2020-10-16
                // urovo add by shenpidong begin 2020-10-19
                // urovo add by shenpidong end 2020-10-19
                resetGSCode();
                // urovo add by shenpidong end 2020-10-16
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (pdialog != null) pdialog.dismiss();
            Toast.makeText(mContext,
                    R.string.scanner_toast, Toast.LENGTH_LONG).show();
            // urovo add shenpidong begin 2019-04-12 , reset virtual button and ScanHandle
            updateVituanButton();
            // urovo modify shenpidong begin 2019-07-12
            resetClipboard();
            // urovo modify shenpidong end 2019-07-12
            updateScanHandle(true);
            // urovo add shenpidong end 2019-04-12
            updateState();
        }
    }

    class UpdateAsyncTask extends AsyncTask<String, String, Integer> {
        private Context mContext;
        private ProgressDialog pdialog;

        public UpdateAsyncTask(Context c) {
            mContext = c;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            pdialog = new ProgressDialog(mContext);
            pdialog.setMessage(mContext.getResources().getString(R.string.scanner_update_progress));
            pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pdialog.setCancelable(false);
            pdialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            try {
                mService.open();
            } catch (android.os.RemoteException e) {
                // TODO: handle exception
            }
            if (pdialog != null) pdialog.dismiss();
            getPreferenceScreen().addPreference(mScanPhoneMode);
            getPreferenceScreen().addPreference(mCodingFormat);
            updateUI();
            updateState();
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        if (preference == mCodingFormat) {
            int value = Integer.parseInt((String) newValue);
            //android.util.Log.i("debug", "LINEAR_CODE_TYPE_SECURITY_LEVEL==================" + value);
            int[] id = new int[]{PropertyID.CODING_FORMAT};
            int[] value_buf = new int[1];
            value_buf[0] = value;
            mScanManager.setPropertyInts(id, value_buf);
            mCodingFormat.setSummary(mCodingFormat.getEntries()[value]);
        } else if (preference == mScanKeyboardType) {
            int value = Integer.parseInt((String) newValue);
            int[] id = new int[]{PropertyID.WEDGE_KEYBOARD_TYPE};
            int[] valueBuf = new int[1];
            valueBuf[0] = value;
            mScanManager.setPropertyInts(id, valueBuf);
            mScanKeyboardType.setSummary( mScanKeyboardType.getEntries()[value]);
            if(value == 3) {
                outEditorTextMode.setEnabled(true);
            }
        } else if (preference == outEditorTextMode) {
            int value = Integer.parseInt((String) newValue);
            int[] id = new int[]{PropertyID.OUT_EDITORTEXT_MODE};
            int[] valueBuf = new int[1];
            valueBuf[0] =  value;
            mScanManager.setPropertyInts(id, valueBuf);
            outEditorTextMode.setSummary( outEditorTextMode.getEntries()[value]);
        } else if(preference == mWiredScan){
            //liqb add
            ListPreference mWiredScan = (ListPreference) preference;
            int index = mWiredScan.findIndexOfValue((String) newValue);
            int value = Integer.parseInt((String) newValue);
            try {
                mService.close();
                mService.writeConfig("SCANER_TYPE", value);
                mWiredScan.setSummary(mWiredScan.getEntries()[index]);
                UpdateAsyncTask task = new UpdateAsyncTask(getActivity());
                task.execute("update");
            } catch (android.os.RemoteException e) {
                // TODO: handle exception
            }
        } else if (preference == mIntentAction) {
            String action = newValue.toString();
            if (!"".equals(action)) {
                mIntentAction.setSummary(action);
                mScanManager.setPropertyString(PropertyID.WEDGE_INTENT_ACTION_NAME, action);
            }
        } else if (preference == mIntentLabel) {
            String action = newValue.toString();
            if (!"".equals(action)) {
                mIntentLabel.setSummary(action);
                mScanManager.setPropertyString(PropertyID.WEDGE_INTENT_DATA_STRING_TAG, action);
            }
        } else if (preference == mScanSounds) {
            int value = Integer.parseInt((String) newValue);
            int[] id = new int[]{PropertyID.GOOD_READ_BEEP_ENABLE};
            int[] valueBuf = new int[1];
            valueBuf[0] = value;
            mScanManager.setPropertyInts(id, valueBuf);
            mScanSounds.setSummary(mScanSounds.getEntries()[value]);
        } else if (preference == mScanSendSounds) {
            int value = Integer.parseInt((String) newValue);
            int[] id = new int[]{PropertyID.SEND_GOOD_READ_BEEP_ENABLE};
            int[] valueBuf = new int[1];
            valueBuf[0] = value;
            mScanManager.setPropertyInts(id, valueBuf);
            mScanSendSounds.setSummary(mScanSendSounds.getEntries()[value]);
        } else if (preference == mScanTriggerMode) {
            int value = Integer.parseInt((String) newValue);
            //android.util.Log.i("debug", "Triggering==================" + value);
            if (value == 1) {
                //updateToggles(CONTINUOUS_MODE);
                if (false && mScanType == 8)
                    mScanManager.setTriggerMode(Triggering.HOST);
                else
                    mScanManager.setTriggerMode(Triggering.CONTINUOUS);
        // urovo add shenpidong begin 2019-09-12
                if (mScanType == 5 || mScanType == 8 || mScanType == 11 || mScanType == 12 || mScanType == 15)
                    n6603multipledecode.setEnabled(true);
        // urovo add shenpidong begin 2019-09-12
            } else if (value == 2) {
                if (n6603multipledecode != null)
                    n6603multipledecode.setEnabled(false);
                mScanManager.setTriggerMode(Triggering.HOST);
            } else if (value == 0) {
                if (n6603multipledecode != null)
                    n6603multipledecode.setEnabled(false);
                mScanManager.setTriggerMode(Triggering.PULSE);
            }
            mScanTriggerMode.setSummary( mScanTriggerMode.getEntries()[value]);
        } else if (preference == mScanActionKey) {
            int value = Integer.parseInt((String) newValue);
            int[] id = new int[]{PropertyID.LABEL_APPEND_ENTER};
            int[] valueBuf = new int[1];
            valueBuf[0] =  value;
            mScanManager.setPropertyInts(id, valueBuf);
            mScanActionKey.setSummary( mScanActionKey.getEntries()[value]);
        }else if(preference == mScanApp){
                android.provider.Settings.System.putInt(mContext.getContentResolver(), "urovo_scan_app", (Boolean)newValue?1:0);
        }else if(preference == mScanHandle){
            setScanHandleEnabled(mContext , (boolean) newValue);
        }else if(preference == mYTOSixLen){
            int[] id_buffer = new int[] {
                    PropertyID.CODE128_LENGTH1,
                    PropertyID.CODE39_LENGTH1,
                    PropertyID.CODE93_LENGTH1,
                    PropertyID.CODE11_LENGTH1
            };
            String len = android.device.provider.Settings.System.getString(mContext.getContentResolver(),
                    "YTO_LEN");
            if(TextUtils.isEmpty(len)){
                len = "6";
            }
            int lenth = Integer.parseInt(len);
            android.device.provider.Settings.System.putString(mContext.getContentResolver(), "SIX_LEN_ENABLED", (boolean)newValue?"enabled":"disabled");
            if(!(boolean)newValue){
                lenth = 2;
            }
            int[] value_buffer = new int[] {
                    lenth + 1,
                    lenth + 1,
                    lenth + 1,
                    lenth + 1
            };
            mScanManager.setPropertyInts(id_buffer, value_buffer);
            mYTOLen.setEnabled( (boolean) newValue);
        }else if(preference == mYTOLen){
            int lenth = Integer.parseInt((String)newValue);
            android.device.provider.Settings.System.putString(mContext.getContentResolver(), "YTO_LEN", (String)newValue);
            int[] id_buffer = new int[] {
                    PropertyID.CODE128_LENGTH1,
                    PropertyID.CODE39_LENGTH1,
                    PropertyID.CODE93_LENGTH1,
                    PropertyID.CODE11_LENGTH1
            };
            int[] value_buffer = new int[] {
                    lenth + 1,
                    lenth + 1,
                    lenth + 1,
                    lenth + 1
            };
            mYTOLen.setSummary((String)newValue);
            mScanManager.setPropertyInts(id_buffer, value_buffer);
        }
        return true;
    }

    public void updateWiredScanListPreference() {
        String summary;
        try {
            int typeInit = mService.readConfig("SCANER_TYPE");
            if (typeInit == 3 || typeInit == 6) {
                mWiredScan.setValue(String.valueOf(typeInit));
                summary = getWiredScanSummary(typeInit);
            } else {
                summary = "other";
            }
            mWiredScan.setSummary(summary);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public String getWiredScanSummary(int value) {
        String summary = "";
        for (int i = 0; i < mWiredScan.getEntryValues().length; i++) {
            if (mWiredScan.getEntryValues()[i].toString().equals(String.valueOf(value))) {
                summary = mWiredScan.getEntries()[i].toString();
            }
        }
        return summary;
    }

    @Override
    public void onEnable(boolean enable) {
        // TODO Auto-generated method stub
        recvHander(enable);
    }

    // urovo add shenpidong begin 2019-09-29
    private static final byte[] SCAN_HANDLE_ON = { '1' };
    private static final byte[] SCAN_HANDLE_OFF = { '0' };

    public static void setScanHandleEnabled(Context context , boolean enabled) {
    //  Log.d(TAG, "setScanHandleEnabled() set ScanHandle enabled:" + enabled);
        if(context == null) {
            Log.d(TAG, "setScanHandleEnabled() , enabled:" + enabled + ",Context:" + context);
            return;
        }
        FileOutputStream outputStream = null;
        String handlePath = KEY_HANDLE_DEFAULT;
        ContentResolver mContentResolver = context.getContentResolver();
        android.device.provider.Settings.System.putInt(mContentResolver, android.device.provider.Settings.System.SCAN_HANDLE, enabled ? 1 : 0);
        try {
            if(android.os.Build.PROJECT.equals("SQ53")) {
                handlePath = KEY_HANDLE_SQ53;
            } else if(android.os.Build.PROJECT.equals("SQ53H")) {
                handlePath = KEY_HANDLE_SQ53H;
            }
            outputStream = new FileOutputStream(handlePath);
            outputStream.write(enabled ? SCAN_HANDLE_ON : SCAN_HANDLE_OFF);
//            outputStream.write(Integer.toString(enabled ? 1 : 0).getBytes());
            outputStream.flush();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.w(TAG, "setScanHandleEnabled() set ScanHandle status failed!" + e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    // urovo add shenpidong end 2019-09-29
    
    //urovo add jinpu.lin 2019.05.08
    private void updateSummay(Preference pref, int value) {
        String timeout = String.format(getActivity().getString(R.string.scanner_laser_timeout_summary), value);
        pref.setSummary(timeout);
    }
    //urovo add end 2019.05.08

    // urovo add shenpidong begin 2019-09-29
    public boolean isScanHandleEnable() {
        FileInputStream inputStream = null;
        boolean result = false;
        String handlePath = KEY_HANDLE_DEFAULT;
        try {
            byte[] buffer = new byte[Integer.toString(0).getBytes().length];
            if(android.os.Build.PROJECT.equals("SQ53") || Build.PROJECT.equals("SQ53H")) {
                handlePath = KEY_HANDLE_SQ53;
            }else if(android.os.Build.PROJECT.equals("SQ53H")) {
                handlePath = KEY_HANDLE_SQ53H;
            }
            inputStream = new FileInputStream(handlePath);
            inputStream.read(buffer);
            if ("1".equals(new String(buffer)))
                result = true;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.w(TAG, "isScanHandleEnable() get ScanHandle status failed!" + e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    // urovo add shenpidong end 2019-09-29

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
            Intent intent = new Intent();
            if(mAction == 1) {
                intent.setAction("action.IMPORT_SCANNER_CONFIG");
            } else {
                intent.setAction("action.EXPORT_SCANNER_CONFIG");
            }
            getActivity().sendBroadcast(intent);
            try{
                Thread.sleep(1500);
            } catch(Exception e) {
                e.printStackTrace();
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if(mAction  == 1) {
                updateState();
            }
            if(pdialog != null) pdialog.dismiss();
            Intent intent = new Intent("com.android.intent.action.SCANNER_SETTINGS");
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            getActivity().startActivity(intent);
        }
    }
    
    //urovo add jinpu.lin 2019.05.08
    private Dialog mMaxMinDialog;
    private int mProgress;
    private TextView mCurrentPro;
    private int max;
    int[] update_id_buffer = new int[1];
    int[] update_value_buffer = new int[1];
    private void showSeekBarDialog(final int title, final int id, final int minVal) {
        if(mMaxMinDialog != null) mMaxMinDialog.dismiss();
        int[]  id_buffer = new int[1];
        id_buffer[0] = id;
        int[] value_buffer = new int[1];
        mScanManager.getPropertyInts(id_buffer, value_buffer);
        mProgress =  value_buffer[0];
        if(mProgress < 0) mProgress= 0;
        View view = View.inflate(getActivity(), R.layout.scanner_maxmin_timeout_dialog, null);
        mCurrentPro = (TextView) view.findViewById(R.id.progress);
        SeekBar mSeekbar = (SeekBar) view.findViewById(R.id.maxmin_seekbar);
        max = 1000;
        mCurrentPro.setText(mProgress + "/" + max);
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
                //android.util.Log.i("debug", "onProgressChanged==================" + mProgress);
                mCurrentPro.setText(mProgress + "/" +max);
            }
        });

        mMaxMinDialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(title)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        android.util.Log.i("debug", "setNegativeButton==================" + mProgress);
                        if(title == R.string.keyevent_enter_delay) {
                            update_id_buffer[0] = PropertyID.APPEND_ENTER_DELAY;
                            update_value_buffer[0] = mProgress;
                            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                            updateSummay(keyevent_enter_delay, mProgress);
                        } else if(title ==  R.string.output_data_delay){
                            update_id_buffer[0] = PropertyID.CHARACTER_DATA_DELAY;
                            update_value_buffer[0] = mProgress;
                            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                            updateSummay(output_data_delay, mProgress);
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
    private void showEditDialog(final int title, final int id, final int minVal) {
        if(mMaxMinDialog != null) mMaxMinDialog.dismiss();
        int[]  id_buffer = new int[1];
        id_buffer[0] = id;
        int[] value_buffer = new int[1];
        mScanManager.getPropertyInts(id_buffer, value_buffer);
        mProgress =  value_buffer[0];
        if(mProgress < 0) mProgress= 0;
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        //LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        //lp.setMargins(16,10,16,0);
        //editText.setLayoutParams(lp);
        mMaxMinDialog =new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setView(editText)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String num = editText.getText().toString().trim();
                            //  urovo add shenpidong begin 2019-05-29
                            try {
                                mProgress = Integer.valueOf("".equals(num)?"0":num);
                                android.util.Log.i("debug", "setNegativeButton==================" + mProgress);
                            } catch(NumberFormatException e) {
                                android.util.Log.i("debug", "setNegativeButton , Progress:" + mProgress + ",editText:" + num);
                                Toast.makeText(getActivity(),
                                    R.string.unknown_input, Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                return;
                            }
                            if(title == R.string.keyevent_enter_delay) {
                                mProgress = mProgress > 1000 ? 1000 : mProgress;
                                update_id_buffer[0] = PropertyID.APPEND_ENTER_DELAY;
                                update_value_buffer[0] = mProgress ;
                                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                                updateSummay(keyevent_enter_delay, mProgress);
                            } else if(title ==  R.string.output_data_delay){
                                if(Build.PWV_CUSTOM_CUSTOM.equals("QCS"))
                                    mProgress = mProgress > 100 ? 100 : mProgress;
                                else
                                    mProgress = mProgress > 1000 ? 1000 : mProgress;
                                update_id_buffer[0] = PropertyID.CHARACTER_DATA_DELAY;
                                update_value_buffer[0] = mProgress ;
                                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                                updateSummay(output_data_delay, mProgress);
                            }
                            //  urovo add shenpidong end 2019-05-29
                        }
                   }).show();
    }    
    
}


