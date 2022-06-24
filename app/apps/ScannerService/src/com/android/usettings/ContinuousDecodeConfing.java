package com.android.usettings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.device.ScanManager;
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

public class ContinuousDecodeConfing extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    
    //Formating None
    private static final String NONE = "None";
    //format
    private ListPreference mn6603_multiple_decode_mode;
    private EditTextPreference n6603_multiple_decode_timeout;
    private EditTextPreference n6603_multiple_decode_count;
    private EditTextPreference n6603_multiple_decode_interval;
    private PreferenceScreen root;
    private PreferenceCategory mContinuousDecodeMode;
    private PreferenceCategory mMultipleDecodeMode;
    private CheckBoxPreference scanner_multiple_decode_mode;
    private EditTextPreference scanner_multiple_decode_count;
    private CheckBoxPreference scanner_multiple_full_read_mode;
    private ListPreference scanner_multiple_decode_separator;
    private int setType;
    private int scannerType;
    
    private PreferenceCategory scanner_decode_udi_lable_list;
    private CheckBoxPreference scanner_decode_all_udi_lable;
    private CheckBoxPreference scanner_format_udi_date;
    private ListPreference scanner_udi_tokensoption;
    private ListPreference scanner_udi_tokensformat;
    private ListPreference scanner_udi_tokenseparator;
    private ListPreference scanner_decode_concatenation;
    private static int UDI_GS1 = 0x0002;
    private static int UDI_HIBCC = 0x0004;
    private static int UDI_ICCBBA = 0x0008;
    private static int UDI_MA = 0x0010;
    private static int UDI_AHM = 0x0020;
    private int mParserUDICodes = UDI_GS1 | UDI_HIBCC | UDI_ICCBBA | UDI_MA | UDI_AHM;
    private int[] parserUDI = new int[]{UDI_GS1 , UDI_HIBCC , UDI_ICCBBA , UDI_MA , UDI_AHM};
    private CheckBoxPreference[] udiCodeKeys;
    
    private ScanManager mScanManager;
    int[] update_id_buffer = new int[1];
    int[] update_value_buffer = new int[1];
    String[] string_update_value_buffer = new String[1];
    
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
        scannerType = args != null ? args.getInt("scannertype") : 0;
    }
    
    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mScanManager = null;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
		if(root != null) {
		    root.removeAll();
		}
		addPreferencesFromResource(R.xml.scanner_mupile_decode_config);
		root = this.getPreferenceScreen();
        mScanManager = new ScanManager();
		updateActionBar();
        updateState();
    }
    // urovo tao.he add begin, 20190314
    private void updateActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowCustomEnabled(false);
        }
        getActivity().setTitle(R.string.n6603_multiple_decode_config);
    }
    // urovo tao.he add end, 20190314 
    private void updateState() {
        if(setType == 2 || setType == 3) {
            if(setType == 2) {
                getActivity().setTitle(R.string.n6603_multiple_decode_config);
            } else if(setType == 3) {
                getActivity().setTitle(R.string.scanner_multiple_decode);
            }
            //mContinuousDecodeMode = (PreferenceCategory) getPreferenceScreen().findPreference("continuous_multiple_decode");
            //mMultipleDecodeMode = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_multiple_decode");
            scanner_multiple_decode_mode= (CheckBoxPreference) getPreferenceScreen().findPreference("scanner_multiple_decode_mode");
            scanner_multiple_decode_count= (EditTextPreference) getPreferenceScreen().findPreference("scanner_multiple_decode_count");
            scanner_multiple_decode_count.setOnPreferenceChangeListener(this);
            scanner_multiple_full_read_mode= (CheckBoxPreference) getPreferenceScreen().findPreference("scanner_multiple_full_read_mode");
            scanner_multiple_decode_separator= (ListPreference) getPreferenceScreen().findPreference("scanner_multiple_decode_separator");
            scanner_multiple_decode_separator.setOnPreferenceChangeListener(this);

            mn6603_multiple_decode_mode = (ListPreference) getPreferenceScreen().findPreference("n6603_multiple_decode_mode");
            if(mn6603_multiple_decode_mode != null) {
                mn6603_multiple_decode_mode.setOnPreferenceChangeListener(this);
            }
            n6603_multiple_decode_count= (EditTextPreference) getPreferenceScreen()
                    .findPreference("n6603_multiple_decode_count");
            n6603_multiple_decode_count.setOnPreferenceChangeListener(this);
            if(n6603_multiple_decode_count != null) {
                getPreferenceScreen().removePreference(n6603_multiple_decode_count);
                //n6603_multiple_decode_count.setOnPreferenceChangeListener(this);
            }
            n6603_multiple_decode_timeout= (EditTextPreference) getPreferenceScreen()
                    .findPreference("n6603_multiple_decode_timeout");
            n6603_multiple_decode_timeout.setOnPreferenceChangeListener(this);
            // urovo tao.he add begin, 20190430
            n6603_multiple_decode_interval = (EditTextPreference) getPreferenceScreen()
                    .findPreference("n6603_multiple_decode_interval");
            n6603_multiple_decode_interval.setOnPreferenceChangeListener(this);
            // urovo tao.he add end, 20190430
            int[] id_buffer = new int[]{
                    PropertyID.DEC_Multiple_Decode_MODE,
                    PropertyID.DEC_Multiple_Decode_TIMEOUT,
                    PropertyID.DEC_Multiple_Decode_INTERVAL,
                    PropertyID.MULTI_DECODE_MODE,
                    PropertyID.BAR_CODES_TO_READ,
                    PropertyID.FULL_READ_MODE,
            };
            int[] value_buffer = new int[id_buffer.length];
            mScanManager.getPropertyInts(id_buffer, value_buffer);
            mn6603_multiple_decode_mode.setValue(String.valueOf(value_buffer[0]));
            mn6603_multiple_decode_mode.setSummary( mn6603_multiple_decode_mode.getEntry());
            int timeout = value_buffer[1];
            n6603_multiple_decode_timeout.setSummary("" + String.valueOf(timeout == -1 ? 5000 : timeout));
            int interval = value_buffer[2];
            n6603_multiple_decode_interval.setSummary("" + String.valueOf(interval < 0? 50:interval));
            scanner_multiple_decode_mode.setChecked(value_buffer[3] == 1);
            int count = value_buffer[4];
             count = count <= 1 ? 1 : count;
            scanner_multiple_decode_count.setSummary("" + String.valueOf(count));
            scanner_multiple_full_read_mode.setChecked(value_buffer[5] == 1);
            if(setType == 2) {
                if(n6603_multiple_decode_interval != null && scannerType != 15) {
                    //getPreferenceScreen().removePreference(n6603_multiple_decode_interval);
                }
                if(scanner_multiple_decode_mode != null) {
                    getPreferenceScreen().removePreference(scanner_multiple_decode_mode);
                }
                if(scanner_multiple_decode_count != null) {
                    getPreferenceScreen().removePreference(scanner_multiple_decode_count);
                }
                if(scanner_multiple_full_read_mode != null) {
                    getPreferenceScreen().removePreference(scanner_multiple_full_read_mode);
                }
                if(scanner_multiple_decode_separator != null) {
                    getPreferenceScreen().removePreference(scanner_multiple_decode_separator);
                }
            } else if(setType == 3) {
                if(mn6603_multiple_decode_mode != null) {
                    getPreferenceScreen().removePreference(mn6603_multiple_decode_mode);
                }
                if(n6603_multiple_decode_timeout != null) {
                    getPreferenceScreen().removePreference(n6603_multiple_decode_timeout);
                }
                if(n6603_multiple_decode_interval != null) {
                    getPreferenceScreen().removePreference(n6603_multiple_decode_interval);
                }
                if(scanner_multiple_decode_separator != null) {
                    getPreferenceScreen().removePreference(scanner_multiple_decode_separator);
                }
            }
        } else if(setType == 4) {
            if(root != null) {
		        root.removeAll();
		    }
		    addPreferencesFromResource(R.xml.scanner_udi_format);
		    root = this.getPreferenceScreen();
            getActivity().setTitle(R.string.scanner_udi_format);
            scanner_decode_udi_lable_list = (PreferenceCategory) getPreferenceScreen().findPreference("scanner_decode_udi_lable_list");
            scanner_decode_all_udi_lable = (CheckBoxPreference) getPreferenceScreen().findPreference("scanner_decode_all_udi_lable");
            scanner_format_udi_date = (CheckBoxPreference) getPreferenceScreen().findPreference("scanner_format_udi_date");
            scanner_udi_tokensoption= (ListPreference) getPreferenceScreen().findPreference("scanner_udi_tokensoption");
            scanner_udi_tokensoption.setOnPreferenceChangeListener(this);
            scanner_udi_tokensformat= (ListPreference) getPreferenceScreen().findPreference("scanner_udi_tokensformat");
            scanner_udi_tokensformat.setOnPreferenceChangeListener(this);
            scanner_udi_tokenseparator= (ListPreference) getPreferenceScreen().findPreference("scanner_udi_tokenseparator");
            scanner_udi_tokenseparator.setOnPreferenceChangeListener(this);
            scanner_decode_concatenation= (ListPreference) getPreferenceScreen().findPreference("scanner_decode_concatenation");
            scanner_decode_concatenation.setOnPreferenceChangeListener(this);
            int[] id_buffer = new int[]{
                    PropertyID.SEND_TOKENS_OPTION,
                    PropertyID.SEND_TOKENS_FORMAT,
                    PropertyID.SEND_TOKENS_SEPARATOR,
                    PropertyID.CODE_ISBT_Concatenation_MODE,
                    PropertyID.ENABLE_FORMAT_UDI_DATE,
                    PropertyID.ENABLE_PARSER_UDICODE,
            };
            int[] value_buffer = new int[id_buffer.length];
            mScanManager.getPropertyInts(id_buffer, value_buffer);
            scanner_udi_tokensoption.setValue(String.valueOf(value_buffer[0]));
            scanner_udi_tokensoption.setSummary( scanner_udi_tokensoption.getEntry());
            scanner_udi_tokensformat.setValue(String.valueOf(value_buffer[1]));
            scanner_udi_tokensformat.setSummary( scanner_udi_tokensformat.getEntry());
            scanner_udi_tokenseparator.setValue(String.valueOf(value_buffer[2]));
            scanner_udi_tokenseparator.setSummary( scanner_udi_tokenseparator.getEntry());
            scanner_decode_concatenation.setValue(String.valueOf(value_buffer[3]));
            scanner_decode_concatenation.setSummary( scanner_decode_concatenation.getEntry());
            if(scanner_decode_concatenation != null) {
                root.removePreference(scanner_decode_concatenation);
            }
            //if(scanner_decode_all_udi_lable != null) {
            //    root.removePreference(scanner_decode_all_udi_lable);
            //}
            scanner_format_udi_date.setChecked(value_buffer[4] == 1);
            if(scanner_decode_udi_lable_list != null) {
                scanner_decode_udi_lable_list.removeAll();
            }
            int udiDodes = value_buffer[5];
            mParserUDICodes = UDI_GS1 | UDI_HIBCC | UDI_ICCBBA | UDI_MA | UDI_AHM;
            if(udiDodes < 0){
                udiDodes = mParserUDICodes;
            }
            scanner_decode_all_udi_lable.setChecked(udiDodes == mParserUDICodes);
            mParserUDICodes = udiDodes;
            String[] udiCodes = getActivity().getResources().getStringArray(R.array.scanner_udi_code_entries);
            if(udiCodes != null && udiCodes.length > 0 && scanner_decode_udi_lable_list != null) {
                int i = 0;
                udiCodeKeys = new CheckBoxPreference[udiCodes.length];
                for(String udiLable: udiCodes) {
                    CheckBoxPreference udiLableKey = new CheckBoxPreference(getActivity());
                    udiLableKey.setKey("UDI_"+udiLable);
                    udiLableKey.setTitle(udiLable);
                    udiLableKey.setPersistent(false);
                    if((parserUDI[i]&udiDodes) != 0) {
                        udiLableKey.setChecked(true);
                    }
                    udiCodeKeys[i]= udiLableKey;
                    scanner_decode_udi_lable_list.addPreference(udiLableKey);
                    i = i + 1;
                }
                /*if(scanner_decode_all_udi_lable.isChecked()) {
                    scanner_decode_udi_lable_list.setEnabled(false);
                } else {
                    scanner_decode_udi_lable_list.setEnabled(true);
                }*/
            }
        }
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        try{
            String key = preference.getKey();
            //android.util.Log.i("debug", "onPreferenceTreeClick==================" + key);
            if(key == null) return false;
            if (key.equals("n6603_multiple_decode_count")) {
                /*int[]  id_buffer = new int[]{
                        PropertyID.DEC_MaxMultiRead_COUNT,
                };
                int[] value_buffer = new int[id_buffer.length];
                mScanManager.getPropertyInts(id_buffer, value_buffer);*/
                ((EditTextPreference) preference).getEditText().setText("" + n6603_multiple_decode_timeout.getSummary());
            } else if (key.equals("n6603_multiple_decode_timeout")) {
                int[]  id_buffer = new int[]{
                        PropertyID.DEC_Multiple_Decode_TIMEOUT,
                };
                int[] value_buffer = new int[id_buffer.length];
                mScanManager.getPropertyInts(id_buffer, value_buffer);
                int timeout = value_buffer[0];
                timeout = timeout == -1 ? 5000 : timeout;
                ((EditTextPreference) preference).getEditText().setText("" + timeout);       
            } /*urovo tao.he add begin, 20190314*/
            else if (key.equals("n6603_multiple_decode_interval")) {
                int[] id_buffer = new int[]{
                        PropertyID.DEC_Multiple_Decode_INTERVAL,
                };
                int[] value_buffer = new int[id_buffer.length];
                mScanManager.getPropertyInts(id_buffer, value_buffer);
                int timeout = value_buffer[0];
                timeout = timeout == -1 ? 50 : timeout;
                ((EditTextPreference) preference).getEditText().setText("" + timeout);
            } else if (key.equals("scanner_multiple_decode_count")) {
                int[] id_buffer = new int[]{
                        PropertyID.BAR_CODES_TO_READ,
                };
                int[] value_buffer = new int[id_buffer.length];
                mScanManager.getPropertyInts(id_buffer, value_buffer);
                int count = value_buffer[0];
                count = count <= 1 ? 1:count;
                ((EditTextPreference) preference).getEditText().setText("" + count);
            } else if (key.equals("scanner_multiple_decode_mode")) {
                int[] id_buffer = new int[]{
                        PropertyID.MULTI_DECODE_MODE,
                };
                int[] value_buffer = new int[id_buffer.length];
                value_buffer[0] = scanner_multiple_decode_mode.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(id_buffer, value_buffer);
            } else if (key.equals("scanner_multiple_full_read_mode")) {
                int[] id_buffer = new int[]{
                        PropertyID.FULL_READ_MODE,
                };
                int[] value_buffer = new int[id_buffer.length];
                value_buffer[0] = scanner_multiple_full_read_mode.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(id_buffer, value_buffer);
            } else if (key.startsWith("UDI_")) {
                 for(int i=0; i < udiCodeKeys.length;i++) {
                    if(udiCodeKeys[i] == preference) {
                        if(((CheckBoxPreference)preference).isChecked()) {
                            mParserUDICodes |= parserUDI[i];
                        } else {
                            mParserUDICodes &= ~parserUDI[i];
                        }
                        break;
                    }
                }
                int[] id_buffer = new int[]{
                        PropertyID.ENABLE_PARSER_UDICODE,
                };
                int[] value_buffer = new int[id_buffer.length];
                value_buffer[0] = mParserUDICodes;
                mScanManager.setPropertyInts(id_buffer, value_buffer);
                if(mParserUDICodes == (UDI_GS1 | UDI_HIBCC | UDI_ICCBBA | UDI_MA | UDI_AHM)) {
                    scanner_decode_all_udi_lable.setChecked(true);
                } else {
                    scanner_decode_all_udi_lable.setChecked(false);
                }
            } else if (key.equals("scanner_format_udi_date")) {
                int[] id_buffer = new int[]{
                        PropertyID.ENABLE_FORMAT_UDI_DATE,
                };
                int[] value_buffer = new int[id_buffer.length];
                value_buffer[0] = scanner_format_udi_date.isChecked() ? 1 : 0;
                mScanManager.setPropertyInts(id_buffer, value_buffer);
            } else if (key.equals("scanner_decode_all_udi_lable")) {
                if(scanner_decode_all_udi_lable.isChecked()) {
                    int[] id_buffer = new int[]{
                            PropertyID.ENABLE_PARSER_UDICODE,
                    };
                    int[] value_buffer = new int[id_buffer.length];
                    mParserUDICodes = UDI_GS1 | UDI_HIBCC | UDI_ICCBBA | UDI_MA | UDI_AHM;
                    value_buffer[0] = mParserUDICodes;
                    mScanManager.setPropertyInts(id_buffer, value_buffer);
                    for(int i=0; i < udiCodeKeys.length;i++) {
                        udiCodeKeys[i].setChecked(true);
                    }
                    //scanner_decode_udi_lable_list.setEnabled(false);
                } else {
                    //scanner_decode_udi_lable_list.setEnabled(true);
                }
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
        if(key == null) return false;
        if (key.equals("scanner_udi_tokensoption")) {
            update_id_buffer[0] = PropertyID.SEND_TOKENS_OPTION;
            int value = Integer.parseInt((String) newValue);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            scanner_udi_tokensoption.setSummary( scanner_udi_tokensoption.getEntries()[value]);
        } else if (key.equals("scanner_udi_tokensformat")) {
            update_id_buffer[0] = PropertyID.SEND_TOKENS_FORMAT;
            int value = Integer.parseInt((String) newValue);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            scanner_udi_tokensformat.setSummary( scanner_udi_tokensformat.getEntries()[value]);
        } else if (key.equals("scanner_udi_tokenseparator")) {
            update_id_buffer[0] = PropertyID.SEND_TOKENS_SEPARATOR;
            int value = Integer.parseInt((String) newValue);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            scanner_udi_tokenseparator.setSummary( scanner_udi_tokenseparator.getEntries()[value]);
        } else if (key.equals("scanner_decode_concatenation")) {
            update_id_buffer[0] = PropertyID.CODE_ISBT_Concatenation_MODE;
            int value = Integer.parseInt((String) newValue);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            scanner_decode_concatenation.setSummary( scanner_decode_concatenation.getEntries()[value]);
        } else if (key.equals("n6603_multiple_decode_mode")) {
            update_id_buffer[0] = PropertyID.DEC_Multiple_Decode_MODE;
            int value = Integer.parseInt((String) newValue);
            update_value_buffer[0] = value;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            mn6603_multiple_decode_mode.setSummary(mn6603_multiple_decode_mode.getEntries()[value]);
        } else if (key.equals("n6603_multiple_decode_count")) {
            if (newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_MaxMultiRead_COUNT;
            String value = newValue.toString();
            //  urovo add shenpidong begin 2019-05-29
            int len = 1;
            try {
                len = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                len = 0;
                value = "" + len;
                e.printStackTrace();
            }
            if (len >= 1 && len <= 100) {
                update_value_buffer[0] = len;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            } else {
                showAlertToast(1, 100);
            }
            //  urovo add shenpidong end 2019-05-29
            //android.util.Log.i("debug", "IMAGE_FIXED_EXPOSURE==================" + value);
        } else if (key.equals("n6603_multiple_decode_timeout")) {
            //  urovo add by shenpidong begin 2020-03-17
            if (newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_Multiple_Decode_TIMEOUT;
            String value = newValue.toString();
            int len = 50;
            try {
                len = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                len = 50;
                value = "" + len;
                e.printStackTrace();
            }
//            android.util.Log.i("debug", "Scan DEC_Multiple_Decode_TIMEOUT==================" + value);
            //  urovo add shenpidong begin 2019-09-12
            //  urovo add shenpidong begin 2019-05-29
            if (len >= 50 && len <= 60000) {
                update_value_buffer[0] = len;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                n6603_multiple_decode_timeout.setSummary("" + value);
            } else {
                showAlertToast(50, 60000);
            }
            //  urovo add shenpidong end 2019-05-29
            //  urovo add shenpidong end 2019-09-12
            //  urovo add by shenpidong end 2020-03-17
            //android.util.Log.i("debug", "IMAGE_FIXED_EXPOSURE==================" + value);
        }/*urovo tao.he add begin, 20190314*/ else if (key.equals("n6603_multiple_decode_interval")) {
            if (newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.DEC_Multiple_Decode_INTERVAL;
            String value = newValue.toString();
            int len = 50;
            try {
                len = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                len = 0;
                value = "" + len;
                e.printStackTrace();
            }
            //  urovo add shenpidong begin 2019-09-12
            //  urovo add shenpidong begin 2019-05-29
            if (len >= 0 && len <= 5000) {
                update_value_buffer[0] = len;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                n6603_multiple_decode_interval.setSummary("" + value);
            } else {
                showAlertToast(0, 5000);
            }
            //  urovo add shenpidong end 2019-05-29
            //  urovo add shenpidong end 2019-09-12
        } else if (key.equals("scanner_multiple_decode_count")) {
            if(newValue == null || newValue.equals("")) return false;
            update_id_buffer[0] = PropertyID.BAR_CODES_TO_READ;
            String value = newValue.toString();
            int len = 1;
	        try {
                len = Integer.parseInt(value);
	        } catch (NumberFormatException e) {
                len = 1;
                value = "" + len;
                e.printStackTrace();
	        }
            if(len >= 1 && len <= 10) {
                update_value_buffer[0] = len;
                mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                scanner_multiple_decode_count.setSummary( "" + value);
            } else {
                showAlertToast(1, 10);
            }
        }
        return true;
    }
     private void initEditText(EditTextPreference keyEditText, boolean number){
        
        keyEditText.getEditText().setInputType( number ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
        keyEditText.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(number ? 4 : 1)});
        keyEditText.getEditText().setHint("1~7848");
        keyEditText.setOnPreferenceChangeListener(this);

    }
    private void updateSummay(Preference pref, int value) {
        String timeout = String.format(getActivity().getString(R.string.scanner_laser_timeout_summary), value * 100);
        pref.setSummary(timeout);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    private void showAlertToast(int min, int max) {
        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_symbology_timeout_range), min, max), Toast.LENGTH_LONG).show();
    }
}
