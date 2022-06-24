package com.android.usettings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.device.LicenseHelper;
import android.device.ScanManager;
import android.device.scanner.configuration.PropertyID;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
// urovo add shenpidong begin 2019-04-18
import com.ubx.decoder.license.ActivationManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
// urovo add shenpidong end 2019-04-18

public class ScannerReaderParams extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    
    private static final String TAG = "ScannerReaderParams";
    //Formating None
    private static final String NONE = "None";
    //format
    private static final String KEY_SEND_CODE_ID ="send_lable_fix";
    private static final String KEY_LABEL_PREFIX ="edit_lable_prefix";
    private static final String KEY_LABEL_SUFFIX ="edit_lable_suffix";
    private static final String KEY_LABEL_PATTERN ="edit_lable_pattern";
    private static final String KEY_LABEL_REPLACE ="edit_lable_replace";
    private static final String KEY_LABEL_SEPARATOR ="edit_lable_separator";
    private static final String KEY_REMOVE_NON_PRINTABLE_CHARS ="remove_non_printable_chars";
    private static final String KEY_SCAN_SEPARATOR_BUTTON = "screen_scanner_separator";
    private static final String KEY_SCAN_SEPARATOR_KEY = "persist.scan.separator";
    // urovo add by shenpidong begin 2020-04-10
    private static final String KEY_SCAN_DELAYTRIGGER_KEY = "persist.scan.delaytrigger";
    private static final String KEY_SCAN_DELAYTAIM_KEY = "persist.scan.delayaim";
    // urovo add by shenpidong end 2020-04-10

    //trigger
    private static final String KEY_LASER_ON_TIME ="laser_on_time";
    private static final String KEY_TIMEOUT_SAME_SYMBOL ="timeout_same_symbol";
    private static final String KEY_LINER_SECURITY_LEVEL ="linear_code_type_security_level";
    // urovo add shenpidong begin 2019-07-08
    private static final int SE2100_DEFAULT_2D_LIGHTS_MODE = 1;
    private static final int N6603_FIXED_EXPOSURE_MIN = 1;
    private static final int N6603_FIXED_EXPOSURE_MAX = 600; // Android9.0 max is 600 , other 7848
    // urovo add shenpidong end 2019-07-08
    
    PreferenceCategory mScannerFormat;
    PreferenceCategory mTriggerReading;
    PreferenceCategory m1DSpecial;
    
    private CheckBoxPreference mFuzzy_1d_processing;
    private ListPreference mImage_inverse_decoder;
    PreferenceCategory mMultiDecode;
    private ListPreference mImage_bar_cades_to_read;
    private CheckBoxPreference mFull_read_mode;
    private CheckBoxPreference mMulti_decode_mode;
    
    private ListPreference mLinearSecurityLevel;
    private Preference mLinerTimeoutSym;
    private Preference mLaserOnTime;
    private Preference decIllumPowerLevel;
    private CheckBoxPreference decPicklistAimMode;
    private EditTextPreference dec_aim_mode_delay;

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
    ListPreference sdcn603_illum_power_level;
    CheckBoxPreference n6603_dec_centering_enable;
	CheckBoxPreference n6603_dec_gs_enable;
    ListPreference decode_centering_mode;
    EditTextPreference decode_window_upper_left_x;
    EditTextPreference decode_window_upper_left_y;
    EditTextPreference decode_window_lower_right_x;
    EditTextPreference decode_window_lower_right_y;
    CheckBoxPreference decode_debug_window_enable;
    //OCR
    CheckBoxPreference sym_ocr_enable;
    ListPreference sym_ocr_mode_config;
    ListPreference sym_ocr_template_config;
    EditTextPreference sym_ocr_user_template;
    
    private CheckBoxPreference scanner_cache_enable;
    private CheckBoxPreference scanner_cache_mode;
    private EditTextPreference scanner_cache_limit_time;

    private ListPreference scanner_Transmit_Code_ID_type;
    private PreferenceCategory scanner_config_debug_mode;
    private EditTextPreference dec_attempt_limit_time;
    private ListPreference low_contrast_image_mode;
    private CheckBoxPreference low_power_sleep_mode;
    private CheckBoxPreference low_contrast_improved_alg;
    private ListPreference dec_debug_pic_mode;
    private Preference dec_switch_decoder;
    private EditTextPreference maxmunExposure;
    private EditTextPreference maxnumGian;
    private EditTextPreference targetWhite;

    private PreferenceScreen root;
    private int setType;
    private int scannerType;
    
    private ScanManager mScanManager;
    int[] update_id_buffer = new int[1];
    int[] update_value_buffer = new int[1];
    String[] string_update_value_buffer = new String[1];
    private String activateServerUrl = null;
    private LicenseActivationAsyncTask activationAsyncTask;
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
        activateServerUrl = args != null ? args.getString("ServerUrl") : null;
        scannerType = args != null ? args.getInt("scannertype") : 0;
        addPreferencesFromResource(R.xml.scanner_reader_params);
        root = this.getPreferenceScreen();
    }
    
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mScanManager = null;
        activateServerUrl = null;
        try {
            if(activationAsyncTask != null) {
                activationAsyncTask.setTaskListener(null);
                activationAsyncTask.cancel(false);
                activationAsyncTask = null;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mScanManager = new ScanManager();
        // add by tao.he for actibar sync issue
        updateActionBar();
        // end add
        updateState();
        if(scannerType == 15 && setType == 2) {
            licenseActivation(activateServerUrl);
        }
    }
    private void licenseActivation(String serverUrl) {
        try {
            if(activationAsyncTask == null) {
                activationAsyncTask = new LicenseActivationAsyncTask(getActivity().getApplicationContext());
                activationAsyncTask.setTaskListener(new LicenseActivationAsyncTask.OnTaskListener() {
                    @Override
                    public void onResult(boolean hasLicense, String version) {
                        if(root != null) {
                            if(dec_switch_decoder != null) {
                                if(hasLicense) {
                                    dec_switch_decoder.setTitle(R.string.msg_license_status);
                                } else {
                                    dec_switch_decoder.setTitle(R.string.msg_default_status);
                                }
                                dec_switch_decoder.setSummary(""+version);
                            }
                        }
                    }
                });
                activationAsyncTask.execute(serverUrl, null);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void copyHeadFileFile(final Context context) {
        final File file = new File("sdcard/bh_1280_720.bin");
        if (file != null && file.exists()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream;
                try {
                    inputStream = context.getResources().openRawResource(R.raw.bh_1280_720);
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int count = 0;
                    while ((count = inputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, count);
                    }
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    inputStream.close();
                    System.out.println("success");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    // add by tao.he for actibar sync issue
    private void updateActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowCustomEnabled(false);
        }
    }
    // end add
    private void updateState() {
        if(setType == 3) {
            
            getActivity().setTitle(R.string.scanner_formatting);
            mTriggerReading = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_read_trigger");
            if(mTriggerReading != null)
                getPreferenceScreen().removePreference(mTriggerReading);
            m1DSpecial = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_1d_special");
            decPicklistAimMode = (CheckBoxPreference) getPreferenceScreen().findPreference("dec_picklist_aim_mode");//se4500准确扫描
            if(decPicklistAimMode != null)
                getPreferenceScreen().removePreference(decPicklistAimMode);
            dec_aim_mode_delay = (EditTextPreference) getPreferenceScreen().findPreference("dec_aim_mode_delay");//3680准确扫描
            if(dec_aim_mode_delay != null)
                getPreferenceScreen().removePreference(dec_aim_mode_delay);
            mMultiDecode = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_multi_decode");
            if(m1DSpecial != null)
                getPreferenceScreen().removePreference(m1DSpecial);
            //  urovo add by shenpidong begin 2020-03-17
            boolean isSupporMulDecode = Build.PROJECT.equals("SQ53") && Build.PWV_CUSTOM_CUSTOM.equals("XX") && (scannerType == 8 || scannerType == 5);
            if (!isSupporMulDecode && mMultiDecode != null)
                getPreferenceScreen().removePreference(mMultiDecode);
            scanner_config_debug_mode = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_debug_mode");
            if (scanner_config_debug_mode != null) {
                getPreferenceScreen().removePreference(scanner_config_debug_mode);
            }
            mExposure = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_exposure");
            if(mExposure != null)
            getPreferenceScreen().removePreference(mExposure);
			n6603_decoce_windowing = (PreferenceCategory) getPreferenceScreen().findPreference("n6603_decoce_windowing");
            if(n6603_decoce_windowing != null)
                getPreferenceScreen().removePreference(n6603_decoce_windowing);
            PreferenceCategory mScannerCacheConfig = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_cache_config");
            if (mScannerCacheConfig != null) {
               //urovo add by jinpu.lin 2019.04.25
               // getPreferenceScreen().removePreference(mScannerCacheConfig);
                if (!Build.PWV_CUSTOM_CUSTOM.equals("WPH"))
                    getPreferenceScreen().removePreference(mScannerCacheConfig);
               //urovo add end 2019.04.25
            }
            mSendCodeId = (ListPreference) getPreferenceScreen().findPreference(KEY_SEND_CODE_ID);
            mLablePrefix = (Preference) getPreferenceScreen().findPreference(KEY_LABEL_PREFIX);
            mLableSuffix = (Preference) getPreferenceScreen().findPreference(KEY_LABEL_SUFFIX);
            mLablePattern = (Preference) getPreferenceScreen().findPreference(KEY_LABEL_PATTERN);
            mLableReplace = (Preference) getPreferenceScreen().findPreference(KEY_LABEL_REPLACE);
            mSendCodeId.setOnPreferenceChangeListener(this);
            mScannerFormat = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_format");

            edit_lable_separator = (Preference) getPreferenceScreen().findPreference("edit_lable_separator");
            scanner_Transmit_Code_ID_type = (ListPreference) getPreferenceScreen().findPreference("scanner_reader_codeid_type");
            mRemoveNonprintchar = (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_REMOVE_NON_PRINTABLE_CHARS);
            mRemoveNonprintchar.setOnPreferenceChangeListener(this);
            int[]  id_buffer = new int[]{
                    PropertyID.SEND_LABEL_PREFIX_SUFFIX,PropertyID.REMOVE_NONPRINT_CHAR, PropertyID.LABEL_SEPARATOR_ENABLE, PropertyID.TRANSMIT_CODE_ID
            };
            int[] value_buffer = new int[id_buffer.length];
            mScanManager.getPropertyInts(id_buffer, value_buffer);
            
            mSendCodeId.setValue(String.valueOf(value_buffer[0]));
            mSendCodeId.setSummary( mSendCodeId.getEntry());
            mRemoveNonprintchar.setChecked(value_buffer[1] == 1);
            if(mScannerFormat!=null) {
                mScanSeparator = (CheckBoxPreference) mScannerFormat.findPreference(KEY_SCAN_SEPARATOR_BUTTON);
                mScanSeparator.setOnPreferenceChangeListener(this);
                mScanSeparator.setChecked(value_buffer[2] == 1);
            }

            if(scanner_Transmit_Code_ID_type != null) {
                scanner_Transmit_Code_ID_type.setOnPreferenceChangeListener(this);
                scanner_Transmit_Code_ID_type.setValue(String.valueOf(value_buffer[3]));
                scanner_Transmit_Code_ID_type.setSummary( scanner_Transmit_Code_ID_type.getEntry());
		// urovo add shenpidong begin 2019-09-12
                if(scannerType != 4 && scannerType != 5 && scannerType != 8 && scannerType != 11 && scannerType != 9 && scannerType != 13 && scannerType != 15 && scannerType != 7) {
                    // 4-SE4500 , 5-6703 , 8-6603 , 9-210 , 13-4710
                    mScannerFormat.removePreference(scanner_Transmit_Code_ID_type);
                }
		// urovo add shenpidong end 2019-09-12
            }

            String prefix = mScanManager.getPropertyString(PropertyID.LABEL_PREFIX);
            String suffix = mScanManager.getPropertyString(PropertyID.LABEL_SUFFIX);
            String pattern = mScanManager.getPropertyString(PropertyID.LABEL_MATCHER_PATTERN);
            String separatorChar = mScanManager.getPropertyString(PropertyID.LABEL_FORMAT_SEPARATOR_CHAR);
            if(TextUtils.isEmpty(separatorChar)) separatorChar = "()";//默认
            edit_lable_separator.setSummary(separatorChar);
            mLablePrefix.setSummary(prefix);
            mLableSuffix.setSummary(suffix);
            mLablePattern.setSummary(pattern);
            replaceRegex = mScanManager.getPropertyString(PropertyID.LABEL_MATCHER_TARGETREGEX);
            replaceMent = mScanManager.getPropertyString(PropertyID.LABEL_MATCHER_REPLACEMENT);
            if(replaceRegex != null && !replaceRegex.equals(""))
                mLableReplace.setSummary(replaceRegex + " > " + replaceMent);
        } else if (setType == 2) {
            getActivity().setTitle(R.string.scanner_trigger_reading);
            mScannerFormat = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_format");
            if(mScannerFormat != null)
                getPreferenceScreen().removePreference(mScannerFormat);
            PreferenceCategory mScannerCacheConfig = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_cache_config");
            if(mScannerCacheConfig != null)
                getPreferenceScreen().removePreference(mScannerCacheConfig);
            m1DSpecial = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_1d_special");
            mMultiDecode = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_multi_decode");
            decPicklistAimMode = (CheckBoxPreference) getPreferenceScreen().findPreference("dec_picklist_aim_mode");
            if(scannerType==9)
                if(decPicklistAimMode != null)
                    getPreferenceScreen().removePreference(decPicklistAimMode);
            dec_aim_mode_delay = (EditTextPreference) getPreferenceScreen().findPreference("dec_aim_mode_delay");//3680准确扫描
            if (scannerType == 8 || scannerType == 5 || scannerType == 15){
                dec_aim_mode_delay.setOnPreferenceChangeListener(this);
            }
            decIllumPowerLevel = (Preference) getPreferenceScreen().findPreference("dec_illum_power_level");
            
            mLinearSecurityLevel = (ListPreference) getPreferenceScreen().findPreference(KEY_LINER_SECURITY_LEVEL);
            mLaserOnTime = (Preference) getPreferenceScreen().findPreference(KEY_LASER_ON_TIME);
            mLinerTimeoutSym = (Preference) getPreferenceScreen().findPreference(KEY_TIMEOUT_SAME_SYMBOL);
            if(mLinearSecurityLevel != null)
            mLinearSecurityLevel.setOnPreferenceChangeListener(this);
            
            mFuzzy_1d_processing = (CheckBoxPreference) getPreferenceScreen().findPreference("scanner_fuzzy_1d_processing");
            mImage_inverse_decoder = (ListPreference) getPreferenceScreen().findPreference("scanner_1d_inverse");
            if(mImage_inverse_decoder != null)
            mImage_inverse_decoder.setOnPreferenceChangeListener(this);
            mMulti_decode_mode = (CheckBoxPreference) getPreferenceScreen().findPreference("multi_decode_mode");
            mFull_read_mode  = (CheckBoxPreference) getPreferenceScreen().findPreference("full_read_mode");
            mImage_bar_cades_to_read = (ListPreference) getPreferenceScreen().findPreference("bar_codes_to_read");
            if(mImage_bar_cades_to_read != null)
                mImage_bar_cades_to_read.setOnPreferenceChangeListener(this);
            
            int[]  id_buffer = new int[]{
                    PropertyID.LASER_ON_TIME,
                    PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL,
                    PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL,
                    PropertyID.FUZZY_1D_PROCESSING,
                    PropertyID.IMAGE_ONE_D_INVERSE,
                    PropertyID.MULTI_DECODE_MODE,
                    PropertyID.BAR_CODES_TO_READ,
                    PropertyID.FULL_READ_MODE,
                    PropertyID.IMAGE_EXPOSURE_MODE,
                    PropertyID.IMAGE_FIXED_EXPOSURE,
                    PropertyID.DEC_2D_LIGHTS_MODE,
                    PropertyID.DEC_2D_CENTERING_ENABLE,
                    PropertyID.DEC_2D_CENTERING_MODE,
                    PropertyID.DEC_2D_WINDOW_UPPER_LX,
                    PropertyID.DEC_2D_WINDOW_UPPER_LY,
                    PropertyID.DEC_2D_WINDOW_LOWER_RX,
                    PropertyID.DEC_2D_WINDOW_LOWER_RY,
                    PropertyID.DEC_2D_DEBUG_WINDOW_ENABLE,
                    PropertyID.DEC_ILLUM_POWER_LEVEL,
                    PropertyID.DEC_PICKLIST_AIM_MODE,
                    PropertyID.DEC_PICKLIST_AIM_DELAY,
                    PropertyID.DEC_OCR_MODE,
                    PropertyID.DEC_OCR_TEMPLATE,
                    PropertyID.SPECIFIC_CODE_GS,
                    PropertyID.DEC_DECODE_DEBUG_MODE,
                    PropertyID.DEC_EachImageAttempt_TIME
            };
            int[] value_buffer = new int[id_buffer.length];
            mScanManager.getPropertyInts(id_buffer, value_buffer);
           /* for(int i = 0; i < id_buffer.length; i++) {
                android.util.Log.i("debug", "id_buffer========"+ id_buffer[i]+ "==========" + value_buffer[i]);
            }*/
            if(mLaserOnTime != null)
            updateSummay(mLaserOnTime, value_buffer[0]);
            if(mLinerTimeoutSym != null)
            updateSummay(mLinerTimeoutSym, value_buffer[1]);
            if(mLinearSecurityLevel != null){
                mLinearSecurityLevel.setValue(String.valueOf(value_buffer[2] - 1));
                mLinearSecurityLevel.setSummary( mLinearSecurityLevel.getEntry());
            }
            if(mFuzzy_1d_processing !=null)
            mFuzzy_1d_processing.setChecked(value_buffer[3] == 1);
            if(mImage_inverse_decoder !=null)
            mImage_inverse_decoder.setValue(String.valueOf(value_buffer[4]));
            if(mImage_inverse_decoder !=null)
            mImage_inverse_decoder.setSummary( mImage_inverse_decoder.getEntry());
            
            if(mMulti_decode_mode !=null)
            mMulti_decode_mode.setChecked(value_buffer[5] == 1);
            if(mImage_bar_cades_to_read !=null) {
                mImage_bar_cades_to_read.setValue(String.valueOf(value_buffer[6] - 1));
                mImage_bar_cades_to_read.setSummary( mImage_bar_cades_to_read.getEntry());
            }
            if(mFull_read_mode!=null)
            mFull_read_mode.setChecked(value_buffer[7] == 1);
            if(scannerType == 3 || scannerType == 6) {//HONYWARE_1D and 3680
                //mLaserOnTime.setEnabled(false);
                //mLinerTimeoutSym.setEnabled(false);
                mTriggerReading = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_read_trigger");
                if(mTriggerReading != null){
                    if(mLaserOnTime != null)
                    mTriggerReading.removePreference(mLaserOnTime);
                    if(scannerType != 6) {
                        if(mLinerTimeoutSym != null)
                            mTriggerReading.removePreference(mLinerTimeoutSym);
                    }
                }
            }
            if(scannerType != 4 && scannerType != 6 && scannerType != 9 && scannerType != 13 && scannerType != 7 && scannerType != 15) {//4---SE4500 , 9---SE2100 , 13---SE4710
                if (m1DSpecial != null && mFuzzy_1d_processing != null)
                    m1DSpecial.removePreference(mFuzzy_1d_processing);
                /*if(mMultiDecode != null)
                    getPreferenceScreen().removePreference(mMultiDecode);*/
            }
            if (scannerType != 15) {
                scanner_config_debug_mode = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_debug_mode");
                if (scanner_config_debug_mode != null) {
                    getPreferenceScreen().removePreference(scanner_config_debug_mode);
                }
            } else {
                scanner_config_debug_mode = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_debug_mode");
                dec_debug_pic_mode = (ListPreference) getPreferenceScreen().findPreference("dec_debug_pic_mode");
                int savemode = value_buffer[24];
                savemode = (savemode >= 0 && savemode <= 4) ? savemode : 0;
                dec_debug_pic_mode.setValue(String.valueOf(savemode));
                dec_debug_pic_mode.setSummary(dec_debug_pic_mode.getEntry());
                dec_debug_pic_mode.setOnPreferenceChangeListener(this);
                dec_attempt_limit_time = (EditTextPreference) getPreferenceScreen()
                        .findPreference("dec_attempt_limit_time");
                if(dec_attempt_limit_time != null) {
                    initEditText(dec_attempt_limit_time, true, "125~1500ms");
                    int limittime = value_buffer[25];
                    limittime = (limittime >= 125 && limittime <= 1500) ? limittime:125;
                    dec_attempt_limit_time.setSummary(String.valueOf(limittime));
                }
                dec_switch_decoder = (Preference) getPreferenceScreen().findPreference("dec_switch_decoder");
                int[] idlowContrast = new int[]{
                        PropertyID.LOW_CONTRAST_IMPROVED,
                        PropertyID.LOW_CONTRAST_IMPROVED_ALGORITHM,
                        PropertyID.LOW_POWER_SLEEP_MODE,
                };
                int[] valuelowContrast = new int[idlowContrast.length];
                mScanManager.getPropertyInts(idlowContrast, valuelowContrast);
                low_contrast_image_mode = (ListPreference) getPreferenceScreen().findPreference("low_contrast_image_mode");
                int lowMode = valuelowContrast[0];
                lowMode = (lowMode >= 0 && lowMode <= 5) ? lowMode:0;
                low_contrast_image_mode.setValue(String.valueOf(lowMode));
                low_contrast_image_mode.setSummary(low_contrast_image_mode.getEntry());
                low_contrast_image_mode.setOnPreferenceChangeListener(this);
                low_contrast_improved_alg = (CheckBoxPreference) getPreferenceScreen().findPreference("low_contrast_improved_alg");
                low_contrast_improved_alg.setChecked(valuelowContrast[1] == 1);
                low_contrast_improved_alg.setOnPreferenceChangeListener(this);
                low_power_sleep_mode = (CheckBoxPreference) getPreferenceScreen().findPreference("low_power_sleep_mode");
                low_power_sleep_mode.setChecked(valuelowContrast[2] == 1);
                low_power_sleep_mode.setOnPreferenceChangeListener(this);
            }

            //  urovo add by shenpidong begin 2020-03-17
            boolean isSupporMulDecode = Build.PROJECT.equals("SQ53") && Build.PWV_CUSTOM_CUSTOM.equals("XX") && (scannerType == 8 || scannerType == 5);
            if (!isSupporMulDecode && mMultiDecode != null)
                getPreferenceScreen().removePreference(mMultiDecode);
            if(scannerType == 1 || scannerType == 13 || scannerType == 7) { // 4710---BarCodeReader.startDecoder rm mLinerTimeoutSym, BarCodeReader.startHandsFreeDecode don't rm mLinerTimeoutSym
                mTriggerReading = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_read_trigger");
                if(mTriggerReading != null){
		    // urovo add shenpidong begin 2019-06-10
                    if(mLinerTimeoutSym != null && scannerType != 13 && scannerType != 7)
                    mTriggerReading.removePreference(mLinerTimeoutSym);
		    // urovo add shenpidong end 2019-06-10
                }
            }
            if(scannerType == 5 || scannerType == 8 || scannerType == 11 || scannerType == 12 || scannerType == 15) { //N6603  3601
                mTriggerReading = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_read_trigger");
                if(mTriggerReading != null){
                    if(mLinerTimeoutSym != null)
                    mTriggerReading.removePreference(mLinerTimeoutSym);
                    if(scannerType == 12 && mLinearSecurityLevel != null) //N6603 开启安全级别校验功能
                        mTriggerReading.removePreference(mLinearSecurityLevel);
                }
                mExposure = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_exposure");
                if(mExposure != null && decIllumPowerLevel != null) {
                    mExposure.removePreference(decIllumPowerLevel);
                }
                int[] idDelay = new int[]{
                        PropertyID.DEC_PICKLIST_AIM_MODE,
                        PropertyID.DEC_PICKLIST_AIM_DELAY,
                };
                int[] valueDelay = new int[idDelay.length];
                mScanManager.getPropertyInts(idDelay, valueDelay);
                //6603/6703出光后解码延时输出
                if ((scannerType == 8 || scannerType == 5 || scannerType == 15) && decPicklistAimMode != null) {
                    decPicklistAimMode.setChecked(valueDelay[0] == 1);
                    decPicklistAimMode.setOnPreferenceChangeListener(this);
                } else if (decPicklistAimMode != null) {
                    getPreferenceScreen().removePreference(decPicklistAimMode);
                }
                if ((scannerType == 8 || scannerType == 5 || scannerType == 15) && dec_aim_mode_delay != null) {
                    int val = valueDelay[1];
                    if (val < 0) val = 0;
                    Log.d(TAG, "updateState , val:" + val);
                    //updateSummay(dec_aim_mode_delay, val);
                    String timeout = String.format(getActivity().getString(R.string.scanner_laser_timeout_summary), val);
                    dec_aim_mode_delay.setSummary(timeout);
                    dec_aim_mode_delay.setEnabled(valueDelay[0] == 1);
                } else if (dec_aim_mode_delay != null) {
                    getPreferenceScreen().removePreference(dec_aim_mode_delay);
                }
                exposureMode = (ListPreference) getPreferenceScreen().findPreference("scanner_exposure_mode");
                int expMode = value_buffer[8];
                if (exposureMode != null) {
                    exposureMode.setOnPreferenceChangeListener(this);
                    exposureMode.setValue(String.valueOf(expMode));
                    exposureMode.setSummary(exposureMode.getEntry());
                }
                fixedExposureLevel = (EditTextPreference) getPreferenceScreen()
                            .findPreference("scanner_fixed_exposure_level");
                if (fixedExposureLevel != null) {
                    initEditText(fixedExposureLevel, true, N6603_FIXED_EXPOSURE_MIN + "~" + N6603_FIXED_EXPOSURE_MAX);
                    fixedExposureLevel.setSummary(String.valueOf(value_buffer[9]));
                    if(value_buffer[8] == 0)
                        fixedExposureLevel.setEnabled(false);
                }
                if (mExposure != null && scannerType == 15) {
                    if (fixedExposureLevel != null) {
                        mExposure.removePreference(fixedExposureLevel);
                    }
                }
		// urovo add shenpidong begin 2019-05-29
		if(scannerType == 9) {
                    n6603_lightsConfig = (ListPreference) getPreferenceScreen().findPreference("se2100_lightsConfig");
                    if (mExposure != null && getPreferenceScreen().findPreference("n6603_lightsConfig") != null)
		    mExposure.removePreference(getPreferenceScreen().findPreference("n6603_lightsConfig"));
		} else {
                    n6603_lightsConfig = (ListPreference) getPreferenceScreen().findPreference("n6603_lightsConfig");
                    if(mExposure != null && getPreferenceScreen().findPreference("se2100_lightsConfig")!=null)
		    mExposure.removePreference(getPreferenceScreen().findPreference("se2100_lightsConfig"));
		}
                int lightsConfig = value_buffer[10];
                if (lightsConfig < 0) {
                    if (scannerType == 9) {
                        lightsConfig = SE2100_DEFAULT_2D_LIGHTS_MODE;
                    } else {
                        lightsConfig = 3;
                    }
                } else if (scannerType == 9 && (lightsConfig < 0 || lightsConfig > 2)) {
                    lightsConfig = SE2100_DEFAULT_2D_LIGHTS_MODE;
                    updateSe2100DefaultParameter(lightsConfig);
                }
		// urovo add shenpidong end 2019-05-29
                if(n6603_lightsConfig != null){
                    n6603_lightsConfig.setOnPreferenceChangeListener(this);
                    n6603_lightsConfig.setValue(String.valueOf(lightsConfig));
                    n6603_lightsConfig.setSummary(n6603_lightsConfig.getEntry());
                }
                int illum_power_level = value_buffer[18] - 1;
                sdcn603_illum_power_level = (ListPreference) getPreferenceScreen().findPreference("sdcn603_illum_power_level");
                if (sdcn603_illum_power_level != null) {
                    sdcn603_illum_power_level.setOnPreferenceChangeListener(this);
                    if (illum_power_level < 0 || illum_power_level > 6) {
                        illum_power_level = 6;
                    }
                    sdcn603_illum_power_level.setValue(String.valueOf(illum_power_level));
                    sdcn603_illum_power_level.setSummary(sdcn603_illum_power_level.getEntry());
                }
                if (scannerType != 15 && mExposure != null && sdcn603_illum_power_level != null) {
                    mExposure.removePreference(sdcn603_illum_power_level);
                }
                n6603_dec_centering_enable= (CheckBoxPreference) getPreferenceScreen()
                    .findPreference("n6603_decode_centering_enable");
                if(n6603_dec_centering_enable != null) {
                 n6603_dec_centering_enable.setChecked(value_buffer[11] == 1);
                 n6603_dec_centering_enable.setOnPreferenceChangeListener(this);
                }
                n6603_dec_gs_enable= (CheckBoxPreference) getPreferenceScreen()
                    .findPreference("n6603_decode_GS_enable");
                if (n6603_dec_gs_enable != null) {
					n6603_dec_gs_enable.setChecked(value_buffer[23] == 1);
					n6603_dec_gs_enable.setOnPreferenceChangeListener(this);
                }
				
                decode_centering_mode = (ListPreference) getPreferenceScreen().findPreference("decode_centering_mode");
                int mode = value_buffer[12];
                if(mode < 0) mode = 0;
                if(decode_centering_mode != null){
                 decode_centering_mode.setOnPreferenceChangeListener(this);
                 decode_centering_mode.setValue(String.valueOf(value_buffer[12]));
                 decode_centering_mode.setSummary(decode_centering_mode.getEntry());
                }
                decode_window_upper_left_x= (EditTextPreference) getPreferenceScreen()
                    .findPreference("decode_window_upper_left_x");
                int window = value_buffer[13];//upX 386 upY 290 lwX 446 lwY 350
                if(window < 0) window = 386;
                if(decode_window_upper_left_x != null) {
                 decode_window_upper_left_x.setOnPreferenceChangeListener(this);
                 decode_window_upper_left_x.setSummary(String.valueOf(window));
                }
                decode_window_upper_left_y= (EditTextPreference) getPreferenceScreen()
                    .findPreference("decode_window_upper_left_y");
                window = value_buffer[14];//upX 386 upY 290 lwX 446 lwY 350
                if(window < 0) window = 290;
                if(decode_window_upper_left_y != null) {
                 decode_window_upper_left_y.setOnPreferenceChangeListener(this);
                 decode_window_upper_left_y.setSummary(String.valueOf(window));
                }
                decode_window_lower_right_x= (EditTextPreference) getPreferenceScreen()
                    .findPreference("decode_window_lower_right_x");
                window = value_buffer[15];//upX 386 upY 290 lwX 446 lwY 350
                if(window < 0) window = 446;
                if(decode_window_lower_right_x != null) {
                 decode_window_lower_right_x.setOnPreferenceChangeListener(this);
                 decode_window_lower_right_x.setSummary(String.valueOf(window));
                }
                decode_window_lower_right_y= (EditTextPreference) getPreferenceScreen()
                    .findPreference("decode_window_lower_right_y");
                window = value_buffer[16];//upX 386 upY 290 lwX 446 lwY 350
                if(window < 0) window = 350;
                if(decode_window_lower_right_y != null) {
                 decode_window_lower_right_y.setOnPreferenceChangeListener(this);
                 decode_window_lower_right_y.setSummary(String.valueOf(window));
                }
                decode_debug_window_enable= (CheckBoxPreference) getPreferenceScreen()
                    .findPreference("decode_debug_window_enable");
                if(decode_debug_window_enable != null) {
                 decode_debug_window_enable.setChecked(value_buffer[17] == 1);
                 decode_debug_window_enable.setOnPreferenceChangeListener(this);
                }
                //OCR
                /*CheckBoxPreference sym_ocr_enable;
                ListPreference sym_ocr_mode_config;
                ListPreference sym_ocr_template_config;
                EditTextPreference sym_ocr_user_template;*/

                /*sym_ocr_mode_config = (ListPreference)getPreferenceScreen()
                        .findPreference("sym_ocr_mode_config");
                sym_ocr_template_config = (ListPreference)getPreferenceScreen()
                        .findPreference("sym_ocr_template_config");
                sym_ocr_user_template = (EditTextPreference)getPreferenceScreen()
                        .findPreference("sym_ocr_user_template");
                if(sym_ocr_mode_config != null){
                    sym_ocr_mode_config.setOnPreferenceChangeListener(this);
                    sym_ocr_mode_config.setValue(String.valueOf(value_buffer[21]));
                    sym_ocr_mode_config.setSummary(sym_ocr_mode_config.getEntry());
                }
                if(sym_ocr_template_config != null){
                    sym_ocr_template_config.setOnPreferenceChangeListener(this);
                    sym_ocr_template_config.setValue(String.valueOf(value_buffer[22]));
                    sym_ocr_template_config.setSummary(sym_ocr_template_config.getEntry());
                }
                if(sym_ocr_user_template != null) {
                    String userTemp = mScanManager.getPropertyString(PropertyID.DEC_OCR_USER_TEMPLATE);
                    sym_ocr_user_template.setOnPreferenceChangeListener(this);
                    sym_ocr_user_template.setSummary(userTemp);
                }*/
                if (scannerType == 15) {
                    n6603_decoce_windowing = (PreferenceCategory) getPreferenceScreen().findPreference("n6603_decoce_windowing");
                    if (n6603_decoce_windowing != null && decode_debug_window_enable != null)
                        n6603_decoce_windowing.removePreference(decode_debug_window_enable);
                    if (n6603_decoce_windowing != null && n6603_dec_gs_enable != null) {
                        n6603_decoce_windowing.removePreference(n6603_dec_gs_enable);
                    }
                }
                //添加2035扫描控制曝光参数
                maxmunExposure = (EditTextPreference) getPreferenceScreen()
                    .findPreference("preferences_exp_time");
                maxmunExposure.setOnPreferenceChangeListener(this);
                maxnumGian = (EditTextPreference) getPreferenceScreen()
                    .findPreference("preferences_gain");
                maxnumGian.setOnPreferenceChangeListener(this);
                targetWhite = (EditTextPreference) getPreferenceScreen()
                    .findPreference("preferences_target_white");
                targetWhite.setOnPreferenceChangeListener(this);
                String driverName = android.os.SystemProperties.get("persist.vendor.sys.scan.name");
                if(!TextUtils.isEmpty(driverName)) {
                    if(("ov9281".equals(driverName) || "se2030".equals(driverName))
                        || ("m114".equals(driverName) || "se2020".equals(driverName))
                        || ("ar0144".equals(driverName) || "se2035".equals(driverName))) {
                        int[] idexp = new int[]{
                            PropertyID.DEC_ES_MAX_GAIN,
                            PropertyID.DEC_ES_MAX_EXP,
                            PropertyID.DEC_ES_TARGET_VALUE,
                        };
                        int[] valueExp = new int[idexp.length];
                        mScanManager.getPropertyInts(idexp, valueExp);
                        int maxVal = valueExp[0];
                        if(maxVal <1 || maxVal > 255) maxVal = 64;
                        maxnumGian.setSummary(String.valueOf(maxVal));
                        maxVal = valueExp[1];
                        if(maxVal <1 || maxVal > 900) maxVal = 100;
                        maxmunExposure.setSummary(String.valueOf(maxVal));
                        maxVal = valueExp[2];
                        if(maxVal <1 || maxVal > 8000) maxVal = 8000;
                        targetWhite.setSummary(String.valueOf(maxVal));
                        updateExposurePreference(expMode == 1);
                        if(("m114".equals(driverName) || "se2020".equals(driverName))) {
                            if (mExposure != null) {
                                if (targetWhite != null) {
                                    mExposure.removePreference(targetWhite);
                                }
                            }
                        } else {
                            if(("ov9281".equals(driverName) || "se2030".equals(driverName))) {
                                targetWhite.setTitle(R.string.sensor_image_contrast);
                            } else {
                                targetWhite.setTitle(R.string.sensor_target_white);
                            }
                        }
                    }
                }
            } else {
                if(scannerType == 4 || scannerType == 9 || scannerType == 13 || scannerType == 7) {//4500
                    scanner_config_debug_mode = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_debug_mode");
                    if (scanner_config_debug_mode != null) {
                        getPreferenceScreen().removePreference(scanner_config_debug_mode);
                    }
                    if(dec_aim_mode_delay != null)
                        getPreferenceScreen().removePreference(dec_aim_mode_delay);
                    mExposure = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_exposure");
                    if(mExposure != null) {
                        exposureMode = (ListPreference) getPreferenceScreen().findPreference("scanner_exposure_mode");
                        if(exposureMode != null){
                            mExposure.removePreference(exposureMode);
                        }
                        fixedExposureLevel = (EditTextPreference) getPreferenceScreen()
                            .findPreference("scanner_fixed_exposure_level");
                        if(fixedExposureLevel != null) {
                            mExposure.removePreference(fixedExposureLevel);
                        }
                        maxmunExposure = (EditTextPreference) getPreferenceScreen()
                            .findPreference("preferences_exp_time");
                        maxnumGian = (EditTextPreference) getPreferenceScreen()
                            .findPreference("preferences_gain");
                        targetWhite = (EditTextPreference) getPreferenceScreen()
                            .findPreference("preferences_target_white");
                        if (maxmunExposure != null) {
                            mExposure.removePreference(maxmunExposure);
                        }
                        if (maxnumGian != null) {
                            mExposure.removePreference(maxnumGian);
                        }
                        if (targetWhite != null) {
                            mExposure.removePreference(targetWhite);
                        }
                    }
		    // urovo add shenpidong begin 2019-05-29
		    if(scannerType == 9) {
			n6603_lightsConfig = (ListPreference) getPreferenceScreen().findPreference("se2100_lightsConfig");
                        if (mExposure != null && getPreferenceScreen().findPreference("n6603_lightsConfig") != null)
                            mExposure.removePreference(getPreferenceScreen().findPreference("n6603_lightsConfig"));
		    } else {
			n6603_lightsConfig = (ListPreference) getPreferenceScreen().findPreference("n6603_lightsConfig");
                        if(mExposure != null && getPreferenceScreen().findPreference("se2100_lightsConfig")!=null)
			mExposure.removePreference(getPreferenceScreen().findPreference("se2100_lightsConfig"));
		    }
                    int lightsConfig = value_buffer[10];
                    if (lightsConfig < 0) {
                        if (scannerType == 9) {
                            lightsConfig = SE2100_DEFAULT_2D_LIGHTS_MODE;
                        } else {
                            lightsConfig = 3;
                        }
                    } else if (scannerType == 9 && (lightsConfig < 0 || lightsConfig > 2)) {
                        lightsConfig = SE2100_DEFAULT_2D_LIGHTS_MODE;
                        updateSe2100DefaultParameter(lightsConfig);
                    }
		    // urovo add shenpidong end 2019-05-29
                    if(n6603_lightsConfig != null){
                        n6603_lightsConfig.setValue(String.valueOf(lightsConfig));
                        n6603_lightsConfig.setSummary(n6603_lightsConfig.getEntry());
                        n6603_lightsConfig.setOnPreferenceChangeListener(this);
                    }
                    decIllumPowerLevel = (Preference) getPreferenceScreen().findPreference("dec_illum_power_level");
                    if(decIllumPowerLevel != null) {
                        decIllumPowerLevel.setSummary("" + value_buffer[18]);
                        if(lightsConfig >= 2) {
                            decIllumPowerLevel.setEnabled(true);
                        } else {
                            decIllumPowerLevel.setEnabled(false);
                        }
                    }
                    decPicklistAimMode = (CheckBoxPreference) getPreferenceScreen().findPreference("dec_picklist_aim_mode");
                    if(scannerType==9){
                        if(decPicklistAimMode != null)
                            getPreferenceScreen().removePreference(decPicklistAimMode);
			// urovo add shenpidong begin 2019-05-29
			if(decIllumPowerLevel != null && mExposure!=null) {
			    mExposure.removePreference(decIllumPowerLevel);
			}
			// urovo add shenpidong end 2019-05-29
                    }else if(decPicklistAimMode != null) {
			        boolean isChecked = value_buffer[19] == 1;
                         decPicklistAimMode.setChecked(isChecked);
                         decPicklistAimMode.setOnPreferenceChangeListener(this);
			if((scannerType == 13 || scannerType == 7 || scannerType == 9) && n6603_lightsConfig != null) {
			    n6603_lightsConfig.setEnabled(!isChecked);
			}
			if((scannerType == 13 || scannerType == 7 || scannerType == 9) && decIllumPowerLevel != null && lightsConfig >= 2) {
			    decIllumPowerLevel.setEnabled(!isChecked);
			}
                    }
                    sdcn603_illum_power_level = (ListPreference) getPreferenceScreen().findPreference("sdcn603_illum_power_level");
                    if (scannerType != 15 && mExposure != null && sdcn603_illum_power_level != null) {
                        mExposure.removePreference(sdcn603_illum_power_level);
                    }
                } else if(scannerType == 6) {//3680
                    scanner_config_debug_mode = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_debug_mode");
                    if (scanner_config_debug_mode != null) {
                        getPreferenceScreen().removePreference(scanner_config_debug_mode);
                    }
                    mExposure = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_exposure");
                    if(mExposure != null) {
                        exposureMode = (ListPreference) getPreferenceScreen().findPreference("scanner_exposure_mode");
                        if(exposureMode != null){
                            mExposure.removePreference(exposureMode);
                        }
                        fixedExposureLevel = (EditTextPreference) getPreferenceScreen()
                            .findPreference("scanner_fixed_exposure_level");
                        if(fixedExposureLevel != null) {
                            mExposure.removePreference(fixedExposureLevel);
                        }
                        decIllumPowerLevel = (Preference) getPreferenceScreen().findPreference("dec_illum_power_level");
                        if(decIllumPowerLevel != null) {
                            mExposure.removePreference(decIllumPowerLevel);
                        }
                        maxmunExposure = (EditTextPreference) getPreferenceScreen()
                            .findPreference("preferences_exp_time");
                        maxnumGian = (EditTextPreference) getPreferenceScreen()
                            .findPreference("preferences_gain");
                        targetWhite = (EditTextPreference) getPreferenceScreen()
                            .findPreference("preferences_target_white");
                        if (maxmunExposure != null) {
                            mExposure.removePreference(maxmunExposure);
                        }
                        if (maxnumGian != null) {
                            mExposure.removePreference(maxnumGian);
                        }
                        if (targetWhite != null) {
                            mExposure.removePreference(targetWhite);
                        }
                    }
                    decPicklistAimMode = (CheckBoxPreference) getPreferenceScreen().findPreference("dec_picklist_aim_mode");
                    if(scannerType==9){
                        if(decPicklistAimMode != null)
                            getPreferenceScreen().removePreference(decPicklistAimMode);
                    }else if(decPicklistAimMode != null) {
                        decPicklistAimMode.setChecked(value_buffer[19] == 1);
                        decPicklistAimMode.setOnPreferenceChangeListener(this);
                    }
		    // urovo add shenpidong begin 2019-05-29
		    if(scannerType == 9) {
			n6603_lightsConfig = (ListPreference) getPreferenceScreen().findPreference("se2100_lightsConfig");
                        if (mExposure != null && getPreferenceScreen().findPreference("n6603_lightsConfig") != null)
                            mExposure.removePreference(getPreferenceScreen().findPreference("n6603_lightsConfig"));
		    } else {
			n6603_lightsConfig = (ListPreference) getPreferenceScreen().findPreference("n6603_lightsConfig");
                        if(mExposure != null && getPreferenceScreen().findPreference("se2100_lightsConfig")!=null)
			mExposure.removePreference(getPreferenceScreen().findPreference("se2100_lightsConfig"));
		    }
                    int lightsConfig = value_buffer[10];
                    if (lightsConfig < 0) {
                        if (scannerType == 9) {
                            lightsConfig = SE2100_DEFAULT_2D_LIGHTS_MODE;
                        } else {
                            lightsConfig = 3;
                        }
                    } else if (scannerType == 9 && (lightsConfig < 0 || lightsConfig > 2)) {
                        lightsConfig = SE2100_DEFAULT_2D_LIGHTS_MODE;
                        updateSe2100DefaultParameter(lightsConfig);
                    }
		    // urovo add shenpidong end 2019-05-29
                    if(n6603_lightsConfig != null){
                        n6603_lightsConfig.setValue(String.valueOf(lightsConfig));
                        n6603_lightsConfig.setSummary(n6603_lightsConfig.getEntry());
                        n6603_lightsConfig.setOnPreferenceChangeListener(this);
                    }
                    dec_aim_mode_delay = (EditTextPreference) getPreferenceScreen().findPreference("dec_aim_mode_delay");//3680准确扫描
                    dec_aim_mode_delay.setOnPreferenceChangeListener(this);
                    if(dec_aim_mode_delay != null){
                        int val =value_buffer[20];
                        if(val < 0) val = 0;
                        //updateSummay(dec_aim_mode_delay, val);
                         String timeoutval = String.format(getActivity().getString(R.string.scanner_laser_timeout_summary), val);
                        dec_aim_mode_delay.setSummary(timeoutval);
                    }
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
                } else {
                    scanner_config_debug_mode = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_debug_mode");
                    if (scanner_config_debug_mode != null) {
                        getPreferenceScreen().removePreference(scanner_config_debug_mode);
                    }
                    if(decPicklistAimMode != null) {
                        getPreferenceScreen().removePreference(decPicklistAimMode);
                    }
                    if(dec_aim_mode_delay != null)
                        getPreferenceScreen().removePreference(dec_aim_mode_delay);
                    mExposure = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_exposure");
                    if(mExposure != null)
                    getPreferenceScreen().removePreference(mExposure);
                }
                n6603_decoce_windowing = (PreferenceCategory) getPreferenceScreen().findPreference("n6603_decoce_windowing");
                if(n6603_decoce_windowing != null)
                    getPreferenceScreen().removePreference(n6603_decoce_windowing);
            }
        } else if(setType == 4) {
            /*getActivity().setTitle(R.string.scanner_cache_config);
            mScannerFormat = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_format");
            if(mScannerFormat != null)
                getPreferenceScreen().removePreference(mScannerFormat);
            mTriggerReading = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_read_trigger");
            if(mTriggerReading != null)
                getPreferenceScreen().removePreference(mTriggerReading);
            m1DSpecial = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_1d_special");
            mMultiDecode = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_multi_decode");
            decPicklistAimMode = (CheckBoxPreference) getPreferenceScreen().findPreference("dec_picklist_aim_mode");
            if(decPicklistAimMode != null) {
                getPreferenceScreen().removePreference(decPicklistAimMode);
            }
            dec_aim_mode_delay = (Preference) getPreferenceScreen().findPreference("dec_aim_mode_delay");
            if(dec_aim_mode_delay != null)
                getPreferenceScreen().removePreference(dec_aim_mode_delay);
            if(m1DSpecial != null)
                getPreferenceScreen().removePreference(m1DSpecial);
            if(mMultiDecode != null)
                getPreferenceScreen().removePreference(mMultiDecode);
            mExposure = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_config_exposure");
            if(mExposure != null)
                getPreferenceScreen().removePreference(mExposure);
            n6603_decoce_windowing = (PreferenceCategory) getPreferenceScreen().findPreference("n6603_decoce_windowing");
                if(n6603_decoce_windowing != null)
                    getPreferenceScreen().removePreference(n6603_decoce_windowing);
            scanner_cache_enable = (CheckBoxPreference) getPreferenceScreen().findPreference("scanner_cache_enable");
            if(scanner_cache_enable != null) {
                int mode = mScanManager.readConfig(mScanManager.CACHE_ENABLE);
                scanner_cache_enable.setChecked(mode == 1);
            }
            scanner_cache_mode = (CheckBoxPreference) getPreferenceScreen().findPreference("scanner_cache_mode");
            scanner_cache_limit_time = (EditTextPreference) getPreferenceScreen().findPreference("scanner_cache_limit_time");
            if(scanner_cache_mode != null) {
                int mode = mScanManager.readConfig(mScanManager.CACHE_LIMIT_ENABLE);
                scanner_cache_mode.setChecked(mode == 1);
            }
            if(scanner_cache_limit_time != null) {
                scanner_cache_limit_time.getEditText().setInputType( InputType.TYPE_CLASS_NUMBER);
                scanner_cache_limit_time.getEditText().setHint("3 S");
                scanner_cache_limit_time.setOnPreferenceChangeListener(this);
                int time = mScanManager.readConfig(mScanManager.CACHE_LIMIT_TIME);
                scanner_cache_limit_time.setSummary("" + time);
            }*/
        }
        //urovo add by jinpu.lin 2019.04.25
        if (Build.PWV_CUSTOM_CUSTOM.equals("WPH")) {
            scanner_cache_enable = (CheckBoxPreference) getPreferenceScreen().findPreference("scanner_cache_enable");
            if (scanner_cache_enable != null) {
                int mode = mScanManager.readConfig(mScanManager.CACHE_ENABLE);
                scanner_cache_enable.setChecked(mode == 1);
            }
            scanner_cache_mode = (CheckBoxPreference) getPreferenceScreen().findPreference("scanner_cache_mode");
            scanner_cache_limit_time = (EditTextPreference) getPreferenceScreen().findPreference("scanner_cache_limit_time");
            if (scanner_cache_mode != null) {
                int mode = mScanManager.readConfig(mScanManager.CACHE_LIMIT_ENABLE);
                scanner_cache_mode.setChecked(mode == 1);
            }
            if (scanner_cache_limit_time != null) {
                scanner_cache_limit_time.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
                scanner_cache_limit_time.getEditText().setHint("3 S");
                scanner_cache_limit_time.setOnPreferenceChangeListener(this);
                int time = mScanManager.readConfig(mScanManager.CACHE_LIMIT_TIME);
                scanner_cache_limit_time.setSummary("" + time);
            }
        }
        //urovo add end 2019.04.25
    }

    // urovo add shenpidong begin 2019-07-09
    private void updateSe2100DefaultParameter(int light) {
        if (mScanManager != null) {
            update_id_buffer[0] = PropertyID.DEC_2D_LIGHTS_MODE;
            update_value_buffer[0] = light;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
        }
    }
    private void updateExposurePreference(boolean enable){
        if (fixedExposureLevel != null) {
                fixedExposureLevel.setEnabled(enable);
        }
        if(maxmunExposure != null) {
            maxmunExposure.setEnabled(enable);
        }
        if(maxnumGian != null) {
            maxnumGian.setEnabled(enable);
        }
        if(targetWhite != null) {
            targetWhite.setEnabled(enable);
        }
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        try{
            String key = preference.getKey();
            //android.util.Log.i("debug", "onPreferenceTreeClick==================" + key);
            if(key == null) return false;
            if(key.equals(KEY_LABEL_PREFIX)) {
                showEditDialog(R.string.edit_lable_prefix, mLablePrefix.getSummary().toString());
            } else if(key.equals(KEY_LABEL_SUFFIX)) {
                showEditDialog(R.string.edit_lable_suffix, mLableSuffix.getSummary().toString());
            } else if(key.equals(KEY_LABEL_PATTERN)) {
                showEditDialog(R.string.edit_lable_pattern, mLablePattern.getSummary().toString());
            } else if(key.equals(KEY_LABEL_REPLACE)) {
                showEditDialog(R.string.scanner_lable_replace, "");
            } else if (key.equals(KEY_REMOVE_NON_PRINTABLE_CHARS)) {
                update_id_buffer[0] = PropertyID.REMOVE_NONPRINT_CHAR;
                update_value_buffer[0] = mRemoveNonprintchar.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            }else if(key.equals(KEY_LASER_ON_TIME)) {
                showSeekBarDialog(R.string.scanner_laser_on_time, PropertyID.LASER_ON_TIME, 5);
            } else if(key.equals(KEY_TIMEOUT_SAME_SYMBOL)) {
                showSeekBarDialog(R.string.scanner_timeout_same_symbol, PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL, 1);
            } else if(key.equals("dec_illum_power_level")) {
                showSeekBarDialog(R.string.se4500_illum_power_level, PropertyID.DEC_ILLUM_POWER_LEVEL, 0);
            } else if(key.equals("dec_aim_mode_delay")) {
                //showSeekBarDialog(R.string.n3680_aimer_delay, PropertyID.DEC_PICKLIST_AIM_DELAY, 0);
                int[] id_buffer = new int[]{
                        PropertyID.DEC_PICKLIST_AIM_DELAY
                };
                int[] value_buffer = new int[id_buffer.length];
                mScanManager.getPropertyInts(id_buffer, value_buffer);
                 ((EditTextPreference) preference).getEditText().setText("" + value_buffer[0]);
            } else if (key.equals("scanner_fuzzy_1d_processing")) {
                update_id_buffer[0] = PropertyID.FUZZY_1D_PROCESSING;
                update_value_buffer[0] = mFuzzy_1d_processing.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            } else if (key.equals("multi_decode_mode")) {
                update_id_buffer[0] = PropertyID.MULTI_DECODE_MODE;
                update_value_buffer[0] = mMulti_decode_mode.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            } else if (key.equals("full_read_mode")) {
                update_id_buffer[0] = PropertyID.FULL_READ_MODE;
                update_value_buffer[0] = mFull_read_mode.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            } else if (key.equals("scanner_fixed_exposure_level")) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals("preferences_exp_time")) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals("preferences_gain")) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals("preferences_target_white")) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals("decode_window_upper_left_x")) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals("decode_window_upper_left_y")) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals("decode_window_lower_right_x")) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals("decode_window_lower_right_y")) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            } else if (key.equals("n6603_decode_centering_enable")) {
                update_id_buffer[0] = PropertyID.DEC_2D_CENTERING_ENABLE;
                update_value_buffer[0] = n6603_dec_centering_enable.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
			} else if (key.equals("n6603_decode_GS_enable")) {
                update_id_buffer[0] = PropertyID.SPECIFIC_CODE_GS;
                update_value_buffer[0] = n6603_dec_gs_enable.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            } else if (key.equals("decode_debug_window_enable")) {
                update_id_buffer[0] = PropertyID.DEC_2D_DEBUG_WINDOW_ENABLE;
                update_value_buffer[0] = decode_debug_window_enable.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            } else if(key.equals("dec_picklist_aim_mode")) {
                update_id_buffer[0] = PropertyID.DEC_PICKLIST_AIM_MODE;
                update_value_buffer[0] = decPicklistAimMode.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
//            android.util.Log.i("Scan-debug", "onPreferenceTreeClick==================" + key + ",decIllumPowerLevel:" + decIllumPowerLevel);
                if(dec_aim_mode_delay != null) {
                    dec_aim_mode_delay.setEnabled(decPicklistAimMode.isChecked());
                }
		if(scannerType == 9 || scannerType == 13 || scannerType == 7) {
		    if(decPicklistAimMode != null) {
			n6603_lightsConfig.setEnabled(!decPicklistAimMode.isChecked());
		    }
		    if(decIllumPowerLevel != null && decPicklistAimMode != null) {
            int[]  id_buffer = new int[]{
//                    PropertyID.DEC_ILLUM_POWER_LEVEL,
		    PropertyID.DEC_2D_LIGHTS_MODE
            };
            int[] value_buffer = new int[id_buffer.length];
            mScanManager.getPropertyInts(id_buffer, value_buffer);
//		android.util.Log.i("Scan-debug", "onPreferenceTreeClick================== value_buffer:" + value_buffer[0]);
			decIllumPowerLevel.setEnabled(value_buffer[0] > 1 && !decPicklistAimMode.isChecked());
		    }
		}
            } else if (key.equals("scanner_cache_enable")) {
                mScanManager.writeConfig(mScanManager.CACHE_ENABLE, scanner_cache_enable.isChecked() ? 1 : 0);
            } else if (key.equals("scanner_cache_mode")) {
                mScanManager.writeConfig(mScanManager.CACHE_LIMIT_ENABLE, scanner_cache_mode.isChecked() ? 1 : 0);
            } else if(key.equals("scanner_cache_limit_time")) {
                scanner_cache_limit_time.getEditText().setText(scanner_cache_limit_time.getSummary());
            } else if ("screen_scanner_separator".equals(key) || preference == mScanSeparator) {
                /*String check = Settings.System.getString(getActivity().getContentResolver(), KEY_SCAN_SEPARATOR_KEY);
                if (getActivity() != null && mScanSeparator != null) {
                    Settings.System.putString(getActivity().getContentResolver(), KEY_SCAN_SEPARATOR_KEY, mScanSeparator.isChecked() + "");
                } else {
                    Log.d(TAG, "Activity:" + getActivity() + ",Scan Control:" + mScanSeparator);
                }*/
                update_id_buffer[0] = PropertyID.LABEL_SEPARATOR_ENABLE;
                update_value_buffer[0] = mScanSeparator.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            } else if(key.equals("edit_lable_separator")) {
                String separator = (String)edit_lable_separator.getSummary();
                showEditDialog(R.string.lable_application_identifier_edit, separator);
            } else if (key.equals("lable_ocr_user_template")) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            }  else if (key.equals("low_contrast_improved_alg")) {
                update_id_buffer[0] = PropertyID.LOW_CONTRAST_IMPROVED_ALGORITHM;
                update_value_buffer[0] =  low_contrast_improved_alg.isChecked() ? 1 : 0;;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            } else if (key.equals("low_power_sleep_mode")) {
                update_id_buffer[0] = PropertyID.LOW_POWER_SLEEP_MODE;
                update_value_buffer[0] =  low_power_sleep_mode.isChecked() ? 1 : 0;;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            } else if (key.equals("dec_attempt_limit_time")) {
                ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        String key = preference.getKey();
        //android.util.Log.i("debug", "onPreferenceChange==================" + key);
        if(key == null) return false;
        if (key.equals(KEY_SEND_CODE_ID)) {
            update_id_buffer[0] = PropertyID.SEND_LABEL_PREFIX_SUFFIX;
            int value = Integer.parseInt((String) newValue);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            mSendCodeId.setSummary( mSendCodeId.getEntries()[value]);
        } else if (key.equals("scanner_reader_codeid_type")) {
            update_id_buffer[0] = PropertyID.TRANSMIT_CODE_ID;
            int value = Integer.parseInt((String) newValue);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            scanner_Transmit_Code_ID_type.setSummary( scanner_Transmit_Code_ID_type.getEntries()[value]);
        } else if (key.equals(KEY_LINER_SECURITY_LEVEL)) {
            update_id_buffer[0] = PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL;
            int value = Integer.parseInt((String) newValue);
            //android.util.Log.i("debug", "LINEAR_CODE_TYPE_SECURITY_LEVEL==================" + value);
            update_value_buffer[0] = value + 1;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            mLinearSecurityLevel.setSummary( mLinearSecurityLevel.getEntries()[value]);
        } else if (key.equals("scanner_1d_inverse")) {
            update_id_buffer[0] = PropertyID.IMAGE_ONE_D_INVERSE;
            int value = Integer.parseInt((String) newValue);
            //android.util.Log.i("debug", "LINEAR_CODE_TYPE_SECURITY_LEVEL==================" + value);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            mImage_inverse_decoder.setSummary( mImage_inverse_decoder.getEntries()[value]);
        } else if (key.equals("bar_codes_to_read")) {
            update_id_buffer[0] = PropertyID.BAR_CODES_TO_READ;
            int value = Integer.parseInt((String) newValue);
            //android.util.Log.i("debug", "LINEAR_CODE_TYPE_SECURITY_LEVEL==================" + value);
            update_value_buffer[0] = value + 1;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            mImage_bar_cades_to_read.setSummary( mImage_bar_cades_to_read.getEntries()[value]);
        } else if (key.equals("scanner_fixed_exposure_level")) {
            if(newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.IMAGE_FIXED_EXPOSURE;
             String value = newValue.toString();
             int len = Integer.parseInt(value);
			 // urovo modify shenpidong begin 2019-07-09
             if(len >= N6603_FIXED_EXPOSURE_MIN && len <= N6603_FIXED_EXPOSURE_MAX) {
                update_value_buffer[0] = len;
             } else if(len < 1) {
                update_value_buffer[0] = N6603_FIXED_EXPOSURE_MIN;
                // urovo add shenpidong begin 2019-04-08
                value = "" + N6603_FIXED_EXPOSURE_MIN;
                        showAlertToast(key , N6603_FIXED_EXPOSURE_MIN, N6603_FIXED_EXPOSURE_MAX);
                // urovo add shenpidong end 2019-04-08
                     } else if(len > N6603_FIXED_EXPOSURE_MAX) {
                        update_value_buffer[0] = N6603_FIXED_EXPOSURE_MAX;
                // urovo add shenpidong begin 2019-04-08
                value = "" + N6603_FIXED_EXPOSURE_MAX;
                        showAlertToast(key , N6603_FIXED_EXPOSURE_MIN, N6603_FIXED_EXPOSURE_MAX);
                // urovo add shenpidong end 2019-04-08
             }
			 // urovo modify shenpidong end 2019-07-09
            //android.util.Log.i("debug", "IMAGE_FIXED_EXPOSURE==================" + value);
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            fixedExposureLevel.setSummary(value);
        } else if (key.equals("preferences_target_white")) {
            if (newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_ES_TARGET_VALUE;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            if (len >= 1 && len <= 8000) {
            } else {
                if (len < 1) {
                    len = 1;
                } else if (len > 8000) {
                    len = 8000;
                }
                showAlertToast(1, 8000);
            }
            update_value_buffer[0] = len;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            targetWhite.setSummary(value);
        } else if (key.equals("preferences_gain")) {
            if (newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_ES_MAX_GAIN;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            if (len >= 1 && len <= 255) {
            } else {
                if (len < 1) {
                    len = 1;
                } else if (len > 255) {
                    len = 64;
                }
                showAlertToast(1, 255);
            }
            update_value_buffer[0] = len;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            maxnumGian.setSummary(value);
        } else if (key.equals("preferences_exp_time")) {
            if (newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_ES_MAX_EXP;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            if (len >= 1 && len <= 900) {
            } else {
                if (len < 1) {
                    len = 1;
                } else if (len > 900) {
                    len = 100;
                }
                showAlertToast(1, 900);
            }
            update_value_buffer[0] = len;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            maxmunExposure.setSummary(value);
        } else if (key.equals("scanner_exposure_mode")) {
            update_id_buffer[0] = PropertyID.IMAGE_EXPOSURE_MODE;
            int value = Integer.parseInt((String) newValue);
            //android.util.Log.i("debug", "IMAGE_EXPOSURE_MODE==================" + value);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            exposureMode.setSummary(exposureMode.getEntries()[value]);
            updateExposurePreference(value == 1);
        } else if (key.equals("decode_window_upper_left_x")) {
	// urovo add shenpidong begin 2019-05-07
            if(newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_2D_WINDOW_UPPER_LX;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            int min = scannerType == 15 ? 0 : 0;
            int max = scannerType == 15 ? 1260 : scannerType == 5 ? 1280:830;
            if (len >= min && len <= max) {
               update_value_buffer[0] = len;
           mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
           decode_window_upper_left_x.setSummary(value);
            } else {
                showAlertToast(min, max);
            }
           //android.util.Log.i("debug", "IMAGE_FIXED_EXPOSURE==================" + value);
        }  else if (key.equals("decode_window_upper_left_y")) {
            if(newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_2D_WINDOW_UPPER_LY;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            int min = scannerType == 15 ? 0 : 0;
            int max = scannerType == 15 ? 700 : scannerType == 5 ? 800:638;
            if (len >= min && len <= max) {
               update_value_buffer[0] = len;
           mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
           decode_window_upper_left_y.setSummary(value);
            } else {
                showAlertToast(min, max);
            }
           //android.util.Log.i("debug", "IMAGE_FIXED_EXPOSURE==================" + value);
        }  else if (key.equals("decode_window_lower_right_x")) {
            if(newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_2D_WINDOW_LOWER_RX;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            int min = scannerType == 15 ? 20 : 1;
            int max = scannerType == 15 ? 1280 : scannerType == 5 ? 1280:831;
            if (len >= min && len <= max) {
                update_value_buffer[0] = len;
           mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
           decode_window_lower_right_x.setSummary(value);
            } else {
                showAlertToast(min, max);
            }
           //android.util.Log.i("debug", "IMAGE_FIXED_EXPOSURE==================" + value);
        } else if (key.equals("decode_window_lower_right_y")) {
            if(newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_2D_WINDOW_LOWER_RY;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            int min = scannerType == 15 ? 20 : 1;
            int max = scannerType == 15 ? 720 : scannerType == 5 ? 800:639;
            if (len >= min && len <= max) {
               update_value_buffer[0] = len;
           mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
           decode_window_lower_right_y.setSummary(value);
            } else {
                showAlertToast(min, max);
            }
           //android.util.Log.i("debug", "IMAGE_FIXED_EXPOSURE==================" + value);
	// urovo add shenpidong end 2019-05-07
	} else if (key.equals("sdcn603_illum_power_level")) {
            update_id_buffer[0] = PropertyID.DEC_ILLUM_POWER_LEVEL;
            int value = Integer.parseInt((String) newValue);
            update_value_buffer[0] = value + 1;
            android.util.Log.i("debug", "DEC_ILLUM_POWER_LEVEL==================" + value);
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            sdcn603_illum_power_level.setSummary(sdcn603_illum_power_level.getEntries()[value]);
	// urovo add shenpidong begin 2019-05-29
        } else if (key.equals("n6603_lightsConfig") || key.equals("se2100_lightsConfig")) {
            update_id_buffer[0] = PropertyID.DEC_2D_LIGHTS_MODE;
            int value = Integer.parseInt((String) newValue);
//            android.util.Log.i("debug", "DEC_2D_LIGHTS_MODE==================" + value);
            update_value_buffer[0] = value;
            if(decIllumPowerLevel != null) {
                decIllumPowerLevel.setEnabled( value >= 2 ? true : false);
            }
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            n6603_lightsConfig.setSummary(n6603_lightsConfig.getEntries()[value]);
            if(scannerType == 6) {
                if(value == 1 || 3 == value || 4 == value) {
                    if(dec_aim_mode_delay != null){
                        dec_aim_mode_delay.setEnabled(true);
                    }
                    if(decPicklistAimMode != null) {
                        decPicklistAimMode.setEnabled(true);
                    }
                    /*int[]  id_buffer = new int[1];
                    id_buffer[0] = PropertyID.DEC_PICKLIST_AIM_DELAY;
                    int[] value_buffer = new int[1];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    mProgress =  value_buffer[0];
                    android.util.Log.i("debug", "DEC_PICKLIST_AIM_DELAY=============mProgress=====" + mProgress);
                    if(mProgress > 0) {
                        mScanManager.setPropertyInts(id_buffer, value_buffer);
                    }*/
                }else {
                    if(dec_aim_mode_delay != null){
                        dec_aim_mode_delay.setEnabled(false);
                    }
                    if(decPicklistAimMode != null) {
                        decPicklistAimMode.setEnabled(false);
                    }
                }
            }
	// urovo add shenpidong end 2019-05-29
        } else if (key.equals("decode_centering_mode")) {
            update_id_buffer[0] = PropertyID.DEC_2D_CENTERING_MODE;
            int value = Integer.parseInt((String) newValue);
            //android.util.Log.i("debug", "DEC_2D_CENTERING_MODE==================" + value);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            decode_centering_mode.setSummary(decode_centering_mode.getEntries()[value]);
        } else if(key.equals("scanner_cache_limit_time")) {
            if(newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            int time = Integer.parseInt(value);
            if(time <= 0) {
                Toast.makeText(getActivity(), "请输入大于0的数值!", Toast.LENGTH_LONG).show();
            } else {
                scanner_cache_limit_time.setSummary("" + value);
                mScanManager.writeConfig(mScanManager.CACHE_LIMIT_TIME, time);
            }
        } else if (key.equals("sym_ocr_user_template")) {
            if(newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            mScanManager.setPropertyString(PropertyID.DEC_OCR_USER_TEMPLATE, value);
            sym_ocr_user_template.setSummary(value);
        } else if (key.equals("sym_ocr_mode_config")) {
            update_id_buffer[0] = PropertyID.DEC_OCR_MODE;
            int value = Integer.parseInt((String) newValue);
            android.util.Log.i("debug", "DEC_2D_CENTERING_MODE==================" + value);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            sym_ocr_mode_config.setSummary(sym_ocr_mode_config.getEntries()[value]);
        } else if (key.equals("sym_ocr_template_config")) {
            update_id_buffer[0] = PropertyID.DEC_OCR_TEMPLATE;
            int value = Integer.parseInt((String) newValue);
            android.util.Log.i("debug", "DEC_2D_CENTERING_MODE==================" + value);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            sym_ocr_template_config.setSummary(sym_ocr_template_config.getEntries()[value]);
        } else if (key.equals("dec_attempt_limit_time")) {
            if(newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_EachImageAttempt_TIME;
            String value = newValue.toString();
            int len = Integer.parseInt(value);
            if(len >= 125 && len <= 1500) {
                update_value_buffer[0] = len;
                //android.util.Log.i("debug", "IMAGE_FIXED_EXPOSURE==================" + value);
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                dec_attempt_limit_time.setSummary(value);
            } else {
                showAlertToast(125, 1500);
            }
        } else if (key.equals("low_contrast_image_mode")) {
            update_id_buffer[0] = PropertyID.LOW_CONTRAST_IMPROVED;
            int value = Integer.parseInt((String) newValue);
            //android.util.Log.i("debug", "LINEAR_CODE_TYPE_SECURITY_LEVEL==================" + value);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            low_contrast_image_mode.setSummary( low_contrast_image_mode.getEntries()[value]);
        } else if (key.equals("dec_debug_pic_mode")) {
            update_id_buffer[0] = PropertyID.DEC_DECODE_DEBUG_MODE;
            int value = Integer.parseInt((String) newValue);
            //android.util.Log.i("debug", "LINEAR_CODE_TYPE_SECURITY_LEVEL==================" + value);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            dec_debug_pic_mode.setSummary(dec_debug_pic_mode.getEntries()[value]);
            if (value > 0) {
                copyHeadFileFile(getActivity());
            }
        } else if (key.equals("dec_aim_mode_delay")) {
            if (newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            int time = Integer.parseInt(value);
            int min = 0;
            int max =4000;
            if (time >= min && time <= max) {
                update_id_buffer[0] = PropertyID.DEC_PICKLIST_AIM_DELAY;
                update_value_buffer[0] = time;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                String timeout = String.format(getActivity().getString(R.string.scanner_laser_timeout_summary), time);
                dec_aim_mode_delay.setSummary(timeout);
            } else {
                showAlertToast(min, max);
            }
        }
        return true;
    }

    private void initEditText(EditTextPreference keyEditText, boolean number, String hintText) {
        
        keyEditText.getEditText().setInputType( number ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
        keyEditText.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(number ? 5 : 1)});
		keyEditText.getEditText().setHint(hintText);
		// urovo modify shenpidong begin 2019-07-09
        //keyEditText.getEditText().setHint(N6603_FIXED_EXPOSURE_MIN + "~" + N6603_FIXED_EXPOSURE_MAX);
		// urovo modify shenpidong end 2019-07-09
        keyEditText.setOnPreferenceChangeListener(this);

    }
    private void updateSummay(Preference pref, int value) {
        String timeout = String.format(getActivity().getString(R.string.scanner_laser_timeout_summary), value * 100);
        pref.setSummary(timeout);
    }
    
    private Dialog mMaxMinDialog;
    private int mProgress;
    private TextView mCurrentPro;
    private int max;
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
	// urovo add shenpidong begin 2019-04-19
        if(minVal == 1) {
            max = 99;
            mCurrentPro.setText(mProgress + "/99");
        } else if(minVal == 0) {
            if(title ==  R.string.n3680_aimer_delay) {
                max = 40;
                mCurrentPro.setText(mProgress + "/40");
            } else {
				if(scannerType == 13 || scannerType == 7) { // SE4710
				    if(mProgress > 7) {
					mProgress = 7;
				    }
				    max = 7;
				    mCurrentPro.setText(mProgress + "/7");
				} else { // SE4500
		                    max = 10;
		                    mCurrentPro.setText(mProgress + "/10");
				}
            }
        } else if(minVal == 5) {
            max = 99;
            mCurrentPro.setText(mProgress + "/99");
        } else {
            max = 99;
            mCurrentPro.setText(mProgress + "/99");
        }
        mSeekbar.setMax(max);
        mSeekbar.setProgress(mProgress - minVal);
        mSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
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
				// urovo add shenpidong begin 2019-04-19
				if(mProgress>max) {
		    		mProgress = max;
				}
				// urovo add shenpidong end 2019-04-19
                mCurrentPro.setText(mProgress + "/" +max);
            }
        });
	// urovo add shenpidong end 2019-04-19
        
        mMaxMinDialog = new AlertDialog.Builder(getActivity())
        .setView(view)
        .setTitle(title)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                android.util.Log.i("debug", "setNegativeButton==================" + mProgress);
                    if(title == R.string.scanner_laser_on_time) {
                        update_id_buffer[0] = PropertyID.LASER_ON_TIME;
                        update_value_buffer[0] = mProgress;
                        mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                        updateSummay(mLaserOnTime, mProgress);
                    } else if(title ==  R.string.scanner_timeout_same_symbol){
                        update_id_buffer[0] = PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL;
                        update_value_buffer[0] = mProgress;
                        mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                        updateSummay(mLinerTimeoutSym, mProgress);
                    } else if(title ==  R.string.n3680_aimer_delay){
                        update_id_buffer[0] = PropertyID.DEC_PICKLIST_AIM_DELAY;
                        update_value_buffer[0] = mProgress;
                        mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                        updateSummay(dec_aim_mode_delay, mProgress);
                    } else {
                        update_id_buffer[0] = PropertyID.DEC_ILLUM_POWER_LEVEL;
                        update_value_buffer[0] = mProgress;
                        mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
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
        if(!currentFormat.equals(NONE)) {
            mCurrentFormat.setText(currentFormat);
        }
        LinearLayout linearlayout =  (LinearLayout) view.findViewById(R.id.format_linearlayout);
        LinearLayout linearlayoutReplaceSrc =  (LinearLayout) view.findViewById(R.id.format_linearlayout_src);
        LinearLayout linearlayoutReplaceDst =  (LinearLayout) view.findViewById(R.id.format_linearlayout_dst);
        Button sure = (Button) view.findViewById(R.id.ok);
        Button cancel = (Button) view.findViewById(R.id.cancel);
         
        Spinner mCharSpinner = (Spinner) view.findViewById(R.id.special_char);
        if(R.string.edit_lable_pattern == title) {
            linearlayout.setVisibility(View.GONE);
            linearlayoutReplaceSrc.setVisibility(View.GONE);
            linearlayoutReplaceDst.setVisibility(View.GONE);
            mCurrentFormat.setHint("[0-9]{5,}");
        } else if(R.string.scanner_lable_replace == title) {
            linearlayout.setVisibility(View.GONE);
            mCurrentFormat.setVisibility(View.GONE);
            mReplaceRegex.setText("" + replaceRegex );
            mReplaceMent.setText("" + replaceMent );
            mReplaceRegex.setHint("hex(0-9,A-F,a-f),eg.1D");
        } else if(R.string.lable_application_identifier_edit == title) {
            linearlayout.setVisibility(View.GONE);
            linearlayoutReplaceSrc.setVisibility(View.GONE);
            linearlayoutReplaceDst.setVisibility(View.GONE);
            mCurrentFormat.setHint("()");
        } else {
            linearlayoutReplaceSrc.setVisibility(View.GONE);
            linearlayoutReplaceDst.setVisibility(View.GONE);
        }
        ArrayAdapter adapter1= ArrayAdapter.createFromResource(
                getActivity(), R.array.gs_substitution_entries, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        mCharSpinner.setAdapter(adapter1);
        mCharSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> adapter, View view,
                    int position, long id) {
                // TODO Auto-generated method stub
                //specialChar = (String) mCharSpinner.getAdapter().getItem(position);
                //android.util.Log.i("debug", "position, " +position  +" long id " + id);
                if(position != 0) {
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
                            if(title == R.string.edit_lable_prefix) {
                                String lablePrefix = mCurrentFormat.getText().toString();
                                if("".equals(lablePrefix)) lablePrefix = "";
                                mLablePrefix.setSummary(lablePrefix);
                                mScanManager.setPropertyString(PropertyID.LABEL_PREFIX, lablePrefix);
                            } else if(title == R.string.edit_lable_suffix) {
                                String lableSuffix = mCurrentFormat.getText().toString();
                                if("".equals(lableSuffix)) lableSuffix = "";
                                mLableSuffix.setSummary(lableSuffix);
                                mScanManager.setPropertyString(PropertyID.LABEL_SUFFIX, lableSuffix);
                            } else if(title == R.string.edit_lable_pattern) {
                                String lablePattern = mCurrentFormat.getText().toString();
				// urovo add shenpidong begin 2019-04-18
                                if("".equals(lablePattern)) {
				    lablePattern = "";
				} else {
				    if(!bracketPairs(lablePattern)) {
					Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_lable_format_error), lablePattern ) , Toast.LENGTH_LONG).show();
					return;
				    }
				}
				// urovo add shenpidong end 2019-04-18
                                mLablePattern.setSummary(lablePattern);
                                mScanManager.setPropertyString(PropertyID.LABEL_MATCHER_PATTERN, lablePattern);
                            } else if(title == R.string.scanner_lable_replace) {
                                String regex = mReplaceRegex.getText().toString();
                                String ment = mReplaceMent.getText().toString();
                                if("".equals(regex) || isHexAnd16Byte(regex) == false) {
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.scanner_lable_replaceerror), Toast.LENGTH_LONG).show();
                                } else {
                                    mLableReplace.setSummary(regex + " > " + ment);
                                    replaceRegex = regex;
                                    replaceMent = ment;
                                    mScanManager.setPropertyString(PropertyID.LABEL_MATCHER_TARGETREGEX, regex);
                                    mScanManager.setPropertyString(PropertyID.LABEL_MATCHER_REPLACEMENT, ment);
                                }
                            } else if(title == R.string.lable_application_identifier_edit){
                                String regex = mCurrentFormat.getText().toString();
                                if("".equals(regex) || regex.length() != 2) {
                                    Toast.makeText(getActivity(), getActivity().getString(R.string.lable_ai_edit_length), Toast.LENGTH_LONG).show();
                                } else {
				    // urovo add shenpidong begin 2019-04-17
				    char reg0 = regex.charAt(0);
				    char reg1 = regex.charAt(1);
//				android.util.Log.i(TAG , "showEditDialog --------- Scan reg0:" + reg0 + ",reg1:" + reg1 + ",regex:" + regex + ",reg00:" + (reg0 >= 0x20 && reg0 <= 0x7F));
				    // Within the reasonable scope of the ASCII
				    if(reg0 >= 0x20 && reg0 <= 0x7F && reg1 >= 0x20 && reg1 <= 0x7F) {
					edit_lable_separator.setSummary(regex);
					mScanManager.setPropertyString(PropertyID.LABEL_FORMAT_SEPARATOR_CHAR, regex);
				    } else {
				    // Not within the reasonable scope of the ASCII
					Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.lable_ai_edit_ascii), regex) , Toast.LENGTH_LONG).show();
				    }
				    // urovo add shenpidong end 2019-04-17
                                }
                            }
                        } catch(Exception e) {
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

    // urovo add shenpidong begin 2019-04-18
    public boolean bracketPairs(String str) {
	if(str == null || "".equals(str.trim())) {
	    return false;
	}
	Stack<Character> sc = new Stack<Character>();
	char[] c = str.toCharArray();
	for (int i = 0; i < c.length; i++) {
	    if (c[i]=='('||c[i]=='['||c[i]=='{') {
		sc.push(c[i]);
	    } else if (c[i]==')') {
		if (sc.peek()=='(') {
		    sc.pop();
		}
	    } else if (c[i]==']') {
		if (sc.peek()=='[') {
		    sc.pop();
		}
	    } else if (c[i]=='}') {
		if (sc.peek()=='{') {
		    sc.pop();
		}
	    }
	}
	boolean formatResult = sc.empty();
	if (formatResult) {
	    android.util.Log.i(TAG , "bracketPairs " + str + " format is OK.");
	} else {
	    android.util.Log.i(TAG , "bracketPairs " + str + " NO format.");
	}
	sc.clear();
	sc = null;
	return formatResult;
    }
    // urovo add shenpidong end 2019-04-18

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

    public static boolean isHexAnd16Byte(String hexString) {
        if (hexString.matches("[0-9A-Fa-f]+") == false) {
            // Error, not hex.
            return false;
        }
        return true;
    }

    private void showAlertToast(int min, int max) {
        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_symbology_length_range), min, max), Toast.LENGTH_LONG).show();
    }

    // urovo add shenpidong begin 2019-04-08
    private void showAlertToast(String key , int min, int max) {
	if(key.equals("scanner_fixed_exposure_level")) {
            Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_exposure_length_range), min, max), Toast.LENGTH_LONG).show();
	}
    }
    // urovo add shenpidong end 2019-04-08
}
