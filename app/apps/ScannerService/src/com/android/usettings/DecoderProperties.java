package com.android.usettings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.device.ScanManager;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
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
import android.util.Log;

public class DecoderProperties extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    
    private ScanManager mScanManager;
    
    private int setType = 0;
    private int scannerType = 0;
    private int[] id_buffer;
    private int[] value_buffer;

    private PreferenceScreen root;
    
    private PreferenceCategory aztec;
    private PreferenceCategory codabar;
    private PreferenceCategory code_128;
    private PreferenceCategory gs1_128;
    private PreferenceCategory code_39;
    private PreferenceCategory code_93;
    private PreferenceCategory composite_cc_ab;
    private PreferenceCategory composite_cc_c;
    private PreferenceCategory datamatrix;
    private PreferenceCategory ean13;
    private PreferenceCategory ean8;
    private PreferenceCategory gs1_databar14;
    private PreferenceCategory gs1_expanded;
    private PreferenceCategory gs1_limited;
    private PreferenceCategory Interleaved_2_of_5;
    private PreferenceCategory Matrix_2_of_5;
    private PreferenceCategory maxicode;
    private PreferenceCategory micropdf417;
    private PreferenceCategory MSI;
    private PreferenceCategory code32;
    private PreferenceCategory pdf417;
    private PreferenceCategory postal_australian;
    private PreferenceCategory postal_japan;
    private PreferenceCategory postal_kix;
    private PreferenceCategory postal_royal_mail;
    private PreferenceCategory postal_planet;
    private PreferenceCategory postal_postnet;
    private PreferenceCategory postal_usps4_state;
    private PreferenceCategory qrcode;
    private PreferenceCategory microQrcode;
    private PreferenceCategory Discrete_2_of_5;
    private PreferenceCategory trioptic;
    private PreferenceCategory upca;
    private PreferenceCategory upce;
    private PreferenceCategory upc_ena_extensions;
    private PreferenceCategory code11;
    private PreferenceCategory upce1;
    private PreferenceCategory chinese25;
    private PreferenceCategory composite_39;
    private PreferenceCategory hanxin;
    private PreferenceCategory postal;
    private PreferenceCategory mOCR;
    private PreferenceCategory mDotCode;
    
    private CheckBoxPreference decoder_type_aztec;
    private ListPreference decoder_type_aztec_inverse;
    private EditTextPreference decoder_type_aztec_userid;
    private EditTextPreference decoder_type_aztec_l1;
    private EditTextPreference decoder_type_aztec_l2;
    private ListPreference decoder_type_aztec_size;


    private CheckBoxPreference decoder_type_Codabar;
    private CheckBoxPreference decoder_type_Codabar_enable_check;
    private CheckBoxPreference decoder_type_Codabar_send_check;
    private CheckBoxPreference decoder_type_Codabar_send_start;
    private CheckBoxPreference decoder_type_Codabar_clsi;
    private EditTextPreference decoder_L1_of_codabar;
    private EditTextPreference decoder_L2_of_codabar;
    private EditTextPreference decoder_codabar_userid;
    
    private CheckBoxPreference decoder_type_code128;
    private EditTextPreference decoder_L1_of_code128;
    private EditTextPreference decoder_L2_of_code128;
    private CheckBoxPreference decoder_code_isbt_128;
    private EditTextPreference decoder_c128_OutOfSpec;
    
    private CheckBoxPreference decoder_gs1128;
    private EditTextPreference decoder_L1_gs1128;
    private EditTextPreference decoder_L2_gs1128;
    private EditTextPreference decoder_gs1128_userid;
    
    private CheckBoxPreference decoder_type_code39;
    private CheckBoxPreference decoder_type_code39_enable_check;
    private CheckBoxPreference decoder_type_code39_send_check;
    private CheckBoxPreference decoder_type_code39_full_ascii;
    private EditTextPreference decoder_L1_of_code39;
    private EditTextPreference decoder_L2_of_code39;
    private EditTextPreference decoder_code39_userid;
    
    private CheckBoxPreference decoder_type_code93;
    private EditTextPreference decoder_L1_of_code93;
    private EditTextPreference decoder_L2_of_code93;
    private EditTextPreference decoder_code93_userid;
    
    private CheckBoxPreference decoder_type_code11;
    private EditTextPreference decoder_L1_of_code11;
    private EditTextPreference decoder_L2_of_code11;
    private CheckBoxPreference decoder_type_code11_enable_check;
    private CheckBoxPreference decoder_type_code11_send_check;
    private ListPreference decoder_type_code11_check_mode;
    
    private CheckBoxPreference decoder_type_composite_cc_ab;
    private EditTextPreference decoder_L1_composite_cc_ab;
    private EditTextPreference decoder_L2_composite_cc_ab;
    private EditTextPreference decoder_composite_cc_ab_userid;
    
    private CheckBoxPreference decoder_type_composite_cc_c;
    private EditTextPreference decoder_L1_composite_cc_c;
    private EditTextPreference decoder_L2_composite_cc_c;
    private EditTextPreference decoder_composite_cc_c_userid;
    
    private CheckBoxPreference decoder_type_datamatrix;
    private EditTextPreference decoder_type_datamatrix_userid;
    private ListPreference decoder_type_datamatrix_inverse;
    private EditTextPreference decoder_l1_datamatrix;
    private EditTextPreference decoder_l2_datamatrix;
    private ListPreference decoder_type_dm_size;
    
    private CheckBoxPreference decoder_type_EAN13;
    private CheckBoxPreference decoder_type_EAN13_send_check;
    private CheckBoxPreference decoder_type_EAN13_to_isbn;
    private CheckBoxPreference decoder_type_EAN13_to_issn;
    private EditTextPreference decoder_ean13_userid;
    
    private CheckBoxPreference decoder_type_EAN8;
    private CheckBoxPreference decoder_type_EAN8_send_check;
    private CheckBoxPreference decoder_type_EAN8_to_ean13;
    private EditTextPreference decoder_ean8_userid;
    
    private CheckBoxPreference decoder_type_gs1_databar14;
    private CheckBoxPreference  decoder_type_convert_to_upc_ean;

    private CheckBoxPreference decoder_type_RSS_Expanded;
    private EditTextPreference decoder_rss_exp_userid;
    private EditTextPreference decoder_l1_expanded;
    private EditTextPreference decoder_l2_expanded;
    
    private CheckBoxPreference decoder_type_gs1_Limited;
    private EditTextPreference decoder_rss_limit_userid;
    
    private CheckBoxPreference decoder_type_Interleaved_2_of_5;
    private CheckBoxPreference decoder_type_Interleaved_2_of_5_en_check;
    private CheckBoxPreference decoder_type_Interleaved_2_of_5_send_check;
    private EditTextPreference decoder_L1_Interleaved_2_of_5;
    private EditTextPreference decoder_L2_Interleaved_2_of_5;
    private CheckBoxPreference decoder_Interleaved_2_of_5_to_ean13;
    
    private CheckBoxPreference decoder_type_Matrix_2_of_5;
    private EditTextPreference decoder_L1_Matrix_2_of_5;
    private EditTextPreference decoder_L2_Matrix_2_of_5;
    private EditTextPreference decoder_Matrix_2_of_5_userid;
    
    private CheckBoxPreference decoder_type_maxicode;
    private EditTextPreference decoder_type_maxicode_userid;
    private EditTextPreference decoder_L1_maxicode;
    private EditTextPreference decoder_L2_maxicode;
    private ListPreference decoder_type_maxicode_size;
    
    private CheckBoxPreference decoder_type_micropdf417;
    private EditTextPreference decoder_type_micropdf417_userid;
    private EditTextPreference decoder_L1_micropdf417;
    private EditTextPreference decoder_L2_micropdf417;
    
    private CheckBoxPreference decoder_type_MSI;
    private ListPreference decoder_type_MSI_2_check;
    private CheckBoxPreference decoder_type_MSI_send_check;
    private ListPreference decoder_type_MSI_2_mod_11;
    private EditTextPreference decoder_L1_of_MSI;
    private EditTextPreference decoder_L2_of_MSI;
    private EditTextPreference decoder_MSI_userid;
    
    private CheckBoxPreference decoder_type_code32;
    private CheckBoxPreference decoder_type_code32_send_start;
    private EditTextPreference decoder_code32_userid;
    
    private CheckBoxPreference decoder_type_pdf417;
    private EditTextPreference decoder_type_pdf417_userid;
    private EditTextPreference decoder_L1_pdf417;
    private EditTextPreference decoder_L2_pdf417;
    
    private CheckBoxPreference decoder_type_ups_fics;
    private CheckBoxPreference decoder_type_australian_post;
    private EditTextPreference decoder_type_australian_post_userid;
    
    private CheckBoxPreference decoder_type_japan_code;
    private EditTextPreference decoder_type_japan_code_userid;
    
    private CheckBoxPreference decoder_type_kix_code;
    private EditTextPreference decoder_type_kix_code_userid;
    
    private CheckBoxPreference decoder_type_royal_mail;
    private EditTextPreference decoder_type_royal_mail_userid;
    private CheckBoxPreference decoder_type_royal_mail_send_chk;
    
    private CheckBoxPreference decoder_type_planet;
    private CheckBoxPreference  decoder_type_postal_planet_send_check;
    private EditTextPreference decoder_planet_userid;
    
    private CheckBoxPreference decoder_type_postnet;
    private EditTextPreference decoder_postnet_userid;
    
    private CheckBoxPreference decoder_type_usps_4state;
    private EditTextPreference decoder_usps_4state_userid;
    private ListPreference scanner_postal_symbologies;
    
    private CheckBoxPreference decoder_type_qrcode;
    private EditTextPreference decoder_type_qrcode_userid;
    private ListPreference decoder_type_qrcode_inverse;
    private EditTextPreference decoder_L1_qrcode;
    private EditTextPreference decoder_L2_qrcode;
    private CheckBoxPreference decoder_type_microqrcode;
    private ListPreference decoder_type_qrcode_size;
    
    private CheckBoxPreference decoder_type_Discrete_2_of_5;
    private EditTextPreference decoder_L1_Discrete_2_of_5;
    private EditTextPreference decoder_L2_Discrete_2_of_5;
    private EditTextPreference decoder_Discrete_2_of_5_userid;
    
    private CheckBoxPreference decoder_type_trioptic;
    private EditTextPreference decoder_trioptic_userid;
    
    private CheckBoxPreference decoder_type_UPC_A;
    private CheckBoxPreference decoder_type_UPC_A_send_check;
    private CheckBoxPreference decoder_type_UPC_A_send_sys;
    private CheckBoxPreference decoder_type_UPC_A_to_ean13;
    private EditTextPreference decoder_upca_userid;
    
    private CheckBoxPreference decoder_type_UPC_E;
    private CheckBoxPreference decoder_type_UPC_E_send_check;
    private CheckBoxPreference decoder_type_UPC_E_send_sys;
    private CheckBoxPreference decoder_type_UPC_E_to_upca;
    private EditTextPreference decoder_upce_userid;
    
    private CheckBoxPreference decoder_type_UPC_E1;
    private CheckBoxPreference decoder_type_UPC_E1_send_check;
    private CheckBoxPreference decoder_type_UPC_E1_send_sys;
    private CheckBoxPreference decoder_type_UPC_E1_to_upca;
    
    private CheckBoxPreference decoder_type_upc_ean_25;
    private ListPreference decoder_upc_ean_security_level;
    private CheckBoxPreference decoder_ucc_coupon_ext;
    
    private CheckBoxPreference decoder_type_chinese_2_of_5;
    private CheckBoxPreference decoder_type_composite_39;
    private CheckBoxPreference decoder_type_hanxin;
    private ListPreference decoder_type_hanxin_inverse;
    //OCR

    private ListPreference sym_ocr_mode_config;
    private ListPreference sym_ocr_template_config;
    private Preference sym_ocr_template_font;
    private EditTextPreference sym_ocr_user_template;
    private CheckBoxPreference decoder_type_dotcode;
    
    
    private void initEditText(EditTextPreference keyEditText, boolean number){
        
        keyEditText.getEditText().setInputType( number ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
        keyEditText.getEditText().setFilters(new InputFilter[]{new InputFilter.LengthFilter(number ? 4 : 1)});
        keyEditText.setOnPreferenceChangeListener(this);

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
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.scanner_barcode_param);
        Bundle args = getArguments();
        setType = args != null ? args.getInt("type") : 0;
        scannerType = args != null ? args.getInt("scanType") : 0;
        mScanManager = new ScanManager();
        
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
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mScanManager = null;
        dismissDialog();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.urovo_decoder_setting);
        root = this.getPreferenceScreen();
        aztec = (PreferenceCategory) getPreferenceScreen().findPreference("aztec");
        codabar = (PreferenceCategory) getPreferenceScreen().findPreference("codabar");
        code_128 = (PreferenceCategory) getPreferenceScreen().findPreference("code_128");
        gs1_128 = (PreferenceCategory) getPreferenceScreen().findPreference("gs1_128");
        code_39 = (PreferenceCategory) getPreferenceScreen().findPreference("code_39");
        code_93 = (PreferenceCategory) getPreferenceScreen().findPreference("code_93");
        composite_cc_ab = (PreferenceCategory) getPreferenceScreen().findPreference("composite_cc_ab");
        composite_cc_c = (PreferenceCategory) getPreferenceScreen().findPreference("composite_cc_c");
        datamatrix = (PreferenceCategory) getPreferenceScreen().findPreference("datamatrix");
        ean13 = (PreferenceCategory) getPreferenceScreen().findPreference("ean13");
        ean8 = (PreferenceCategory) getPreferenceScreen().findPreference("ean8");
        gs1_databar14 = (PreferenceCategory) getPreferenceScreen().findPreference("gs1_databar14");
        gs1_expanded = (PreferenceCategory) getPreferenceScreen().findPreference("gs1_expanded");
        gs1_limited = (PreferenceCategory) getPreferenceScreen().findPreference("gs1_limited");
        Interleaved_2_of_5 = (PreferenceCategory) getPreferenceScreen().findPreference("Interleaved_2_of_5");
        Matrix_2_of_5 = (PreferenceCategory) getPreferenceScreen().findPreference("Matrix_2_of_5");
        maxicode = (PreferenceCategory) getPreferenceScreen().findPreference("maxicode");
        micropdf417 = (PreferenceCategory) getPreferenceScreen().findPreference("micropdf417");
        MSI = (PreferenceCategory) getPreferenceScreen().findPreference("MSI");
        code32 = (PreferenceCategory) getPreferenceScreen().findPreference("code32");
        pdf417 = (PreferenceCategory) getPreferenceScreen().findPreference("pdf417");
        postal_australian = (PreferenceCategory) getPreferenceScreen().findPreference("postal_australian");
        postal_japan = (PreferenceCategory) getPreferenceScreen().findPreference("postal_japan");
        postal_kix = (PreferenceCategory) getPreferenceScreen().findPreference("postal_kix");
        postal_royal_mail = (PreferenceCategory) getPreferenceScreen().findPreference("postal_royal_mail");
        postal_planet = (PreferenceCategory) getPreferenceScreen().findPreference("postal_planet");
        postal_postnet = (PreferenceCategory) getPreferenceScreen().findPreference("postal_postnet");
        postal_usps4_state = (PreferenceCategory) getPreferenceScreen().findPreference("postal_usps4_state");
        qrcode = (PreferenceCategory) getPreferenceScreen().findPreference("qrcode");
        microQrcode = (PreferenceCategory) getPreferenceScreen().findPreference("micro_qrcode");
        Discrete_2_of_5 = (PreferenceCategory) getPreferenceScreen().findPreference("Discrete_2_of_5");
        trioptic = (PreferenceCategory) getPreferenceScreen().findPreference("trioptic");
        upca = (PreferenceCategory) getPreferenceScreen().findPreference("upca");
        upce = (PreferenceCategory) getPreferenceScreen().findPreference("upce");
        upc_ena_extensions = (PreferenceCategory) getPreferenceScreen().findPreference("upc_ena_extensions");
        
        chinese25 = (PreferenceCategory) getPreferenceScreen().findPreference("chinese_2_of_5");
        upce1 = (PreferenceCategory) getPreferenceScreen().findPreference("upce1");
        code11 = (PreferenceCategory) getPreferenceScreen().findPreference("code_11");
        
        hanxin = (PreferenceCategory) getPreferenceScreen().findPreference("hanxin");
        postal = (PreferenceCategory) getPreferenceScreen().findPreference("postal_codes");
        composite_39 = (PreferenceCategory) getPreferenceScreen().findPreference("composite_39");
        mOCR = (PreferenceCategory) getPreferenceScreen().findPreference("lable_ocr_symbology");
        mDotCode = (PreferenceCategory) getPreferenceScreen().findPreference("lable_dotcode_symbology");
        int i = 0;
        if (setType == SymbologySettings.SET_Aztec) {
            if (aztec != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.AZTEC_ENABLE, // aztec code
                            PropertyID.AZTEC_INVERSE,
                            PropertyID.AZTEC_SYMBOL_SIZE
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_aztec = (CheckBoxPreference) getPreferenceScreen().findPreference(
                            "decoder_type_aztec");
                    decoder_type_aztec_inverse = (ListPreference)getPreferenceScreen().findPreference(
                            "scanner_aztec_inverse");
                    decoder_type_aztec_inverse.setOnPreferenceChangeListener(this);
                    decoder_type_aztec.setChecked(value_buffer[i++] == 1);
                    decoder_type_aztec_inverse.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_aztec_inverse.setSummary( decoder_type_aztec_inverse.getEntry());
                    if(scannerType == 6 || scannerType == 8 || scannerType == 5 || scannerType == 11) {
                        if(decoder_type_aztec_inverse != null)
                            aztec.removePreference(decoder_type_aztec_inverse);
                    }
                    if(scannerType == 5 || scannerType == 8|| scannerType == 15) {
                        decoder_type_aztec_size = new ListPreference(getActivity());
                        decoder_type_aztec_size.setKey(Settings.System.AZTEC_SYMBOL_SIZE);
                        decoder_type_aztec_size.setTitle(R.string.scanner_symbols_size);
                        decoder_type_aztec_size.setEntries(R.array.scanner_symbols_size_entries);
                        decoder_type_aztec_size.setEntryValues(R.array.code_id_type_values);
                        decoder_type_aztec_size.setOnPreferenceChangeListener(this);
                        aztec.addPreference(decoder_type_aztec_size);
                        int val = value_buffer[i++];
                        val = val>=0?val:0;
                        decoder_type_aztec_size.setValue(String.valueOf(val));
                        decoder_type_aztec_size.setSummary(decoder_type_aztec_size.getEntry());
                    }
                } catch (Exception e) {

                }
            }
        } else {
            if (aztec != null) {
                root.removePreference(aztec);
            }
        }
        if (setType == SymbologySettings.SET_Codabar) {
            if (codabar != null) {
                try {

                    id_buffer = new int[] {
                            PropertyID.CODABAR_ENABLE,
                            PropertyID.CODABAR_LENGTH1,
                            PropertyID.CODABAR_LENGTH2,
                            PropertyID.CODABAR_NOTIS,
                            PropertyID.CODABAR_CLSI,
                            //PropertyID.CODABAR_ENABLE_CHECK, PropertyID.CODABAR_SEND_CHECK,
                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_Codabar = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_Codabar");
                    decoder_L1_of_codabar = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L1_of_codabar");
                    initEditText(decoder_L1_of_codabar, true);
                    decoder_L2_of_codabar = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L2_of_codabar");
                    initEditText(decoder_L2_of_codabar, true);
                   /* decoder_type_Codabar_enable_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("codabar_enable_checksum");
                    decoder_type_Codabar_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("codabar_send_checksum");*/
                    decoder_type_Codabar_send_start = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_Codabar_send_start");
                    decoder_type_Codabar_clsi = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_Codabar_clsi");

                    decoder_type_Codabar.setChecked(value_buffer[i++] == 1);
                    decoder_L1_of_codabar.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_L2_of_codabar.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_type_Codabar_send_start.setChecked(value_buffer[i++] == 1);
                    decoder_type_Codabar_clsi.setChecked(value_buffer[i++] == 1);
                    //decoder_type_Codabar_enable_check.setChecked(value_buffer[i++] == 1);
                    //decoder_type_Codabar_send_check.setChecked(value_buffer[i++] == 1);

                } catch (Exception e) {

                }
            }
        } else {
            if (codabar != null) {
                root.removePreference(codabar);
            }
        }
        if (setType == SymbologySettings.SET_Code128) {
            if (code_128 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.CODE128_ENABLE,
                            PropertyID.CODE128_LENGTH1,
                            PropertyID.CODE128_LENGTH2,
                            PropertyID.CODE_ISBT_128,
                            PropertyID.C128_OUT_OF_SPEC
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_code128 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code128");
                    decoder_code_isbt_128 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code128_isbt");
                    decoder_L1_of_code128 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L1_of_code128");
                    initEditText(decoder_L1_of_code128, true);
                    decoder_L2_of_code128 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L2_of_code128");
                    initEditText(decoder_L2_of_code128, true);

                    decoder_type_code128.setChecked(value_buffer[i++] == 1);
                    decoder_L1_of_code128.setSummary(String.valueOf( value_buffer[i++]));
                    decoder_L2_of_code128.setSummary(String.valueOf( value_buffer[i++]));
                    decoder_code_isbt_128.setChecked(value_buffer[i++] == 1);
                    if(scannerType == 5 || scannerType == 8|| scannerType == 15) {
                        decoder_c128_OutOfSpec = new EditTextPreference(getActivity());
                        decoder_c128_OutOfSpec.setKey(Settings.System.C128_OUT_OF_SPEC);
                        decoder_c128_OutOfSpec.setTitle(R.string.c128_outofspec);
                        initEditText(decoder_c128_OutOfSpec, true);
                        code_128.addPreference(decoder_c128_OutOfSpec);
                        int level = value_buffer[i++];
                        level = level >= 0 ? level : 0;
                        decoder_c128_OutOfSpec.setSummary(String.valueOf(level));
                    }
                } catch (Exception e) {
                }
            }
        } else {
            if (code_128 != null) {
                root.removePreference(code_128);
            }
        }
        if (setType == SymbologySettings.SET_Gs1_128) {
            if (gs1_128 != null) {
                try {

                    id_buffer = new int[] {
                            PropertyID.CODE128_GS1_ENABLE
                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_gs1128 = (CheckBoxPreference) getPreferenceScreen().findPreference(
                            "enable_gs1_128");
                   /* decoder_L1_gs1128 = (EditTextPreference) getPreferenceScreen().findPreference(
                            "gs1_128_L1");
                    initEditText(decoder_L1_gs1128, true);
                    decoder_L2_gs1128 = (EditTextPreference) getPreferenceScreen().findPreference(
                            "gs1_128_L2");
                    initEditText(decoder_L2_gs1128, true);*/

                    decoder_gs1128.setChecked(value_buffer[i++] == 1);
                    //decoder_L1_gs1128.setSummary(String.valueOf(value_buffer[i++]));
                    //decoder_L2_gs1128.setSummary(String.valueOf(value_buffer[i++]));
                } catch (Exception e) {

                }
            }
        } else {
            if (gs1_128 != null) {
                root.removePreference(gs1_128);
            }
        }
        if (setType == SymbologySettings.SET_Code39) {

            if (code_39 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.CODE39_ENABLE,
                            PropertyID.CODE39_LENGTH1,
                            PropertyID.CODE39_LENGTH2,
                            PropertyID.CODE39_ENABLE_CHECK,
                            PropertyID.CODE39_SEND_CHECK,
                            PropertyID.CODE39_FULL_ASCII,
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_code39 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code39");
                    decoder_L1_of_code39 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L1_of_code39");
                    initEditText(decoder_L1_of_code39, true);
                    decoder_L2_of_code39 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L2_of_code39");
                    initEditText(decoder_L2_of_code39, true);
                    decoder_type_code39_enable_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code39_enable_check");
                    decoder_type_code39_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code39_send_check");
                    decoder_type_code39_full_ascii = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code39_full_ascii");

                    decoder_type_code39.setChecked(value_buffer[i++] == 1);
                    decoder_L1_of_code39.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_L2_of_code39.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_type_code39_enable_check.setChecked(value_buffer[i++] == 1);
                    decoder_type_code39_send_check.setChecked(value_buffer[i++] == 1);
                    decoder_type_code39_full_ascii.setChecked(value_buffer[i++] == 1);
                } catch (Exception e) {
                }
            }
        } else {
            if (code_39 != null) {
                root.removePreference(code_39);
            }
        }
        if (setType == SymbologySettings.SET_Code93) {
            if (code_93 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.CODE93_ENABLE,
                            PropertyID.CODE93_LENGTH1,
                            PropertyID.CODE93_LENGTH2,
                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_code93 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code93");
                    decoder_L1_of_code93 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L1_of_code93");
                    initEditText(decoder_L1_of_code93, true);
                    decoder_L2_of_code93 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L2_of_code93");
                    initEditText(decoder_L2_of_code93, true);

                    decoder_type_code93.setChecked(value_buffer[i++] == 1);
                    decoder_L1_of_code93.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_L2_of_code93.setSummary(String.valueOf(value_buffer[i++]));
                } catch (Exception e) {
                }
            }
        } else {
            if (code_93 != null) {
                root.removePreference(code_93);
            }
        }
        if (setType == SymbologySettings.SET_Composite_cc_ab) {
            if (composite_cc_ab != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.COMPOSITE_CC_AB_ENABLE 
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_composite_cc_ab = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_composite_cc_ab");
                    decoder_type_composite_cc_ab.setChecked(value_buffer[i++] == 1);
                } catch (Exception e) {

                }
            }
        } else {
            if (composite_cc_ab != null) {
                root.removePreference(composite_cc_ab);
            }
        }
        if (setType == SymbologySettings.SET_Composite_cc_cc) {
            if (composite_cc_c != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.COMPOSITE_CC_C_ENABLE
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_composite_cc_c = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_composite_cc_c");
                    decoder_type_composite_cc_c.setChecked(value_buffer[i++] == 1);
                } catch (Exception e) {

                }
            }
        } else {
            if (composite_cc_c != null) {
                root.removePreference(composite_cc_c);
            }
        }
        if (setType == SymbologySettings.SET_Datamatrix) {
            if (datamatrix != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.DATAMATRIX_ENABLE,
                            PropertyID.DATAMATRIX_INVERSE,
                            PropertyID.DATAMATRIX_SYMBOL_SIZE,
                            //PropertyID.DATAMATRIX_LENGTH1,
                            //PropertyID.DATAMATRIX_LENGTH2,
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_datamatrix = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_datamatrix");
                    /*decoder_l1_datamatrix = (EditTextPreference) getPreferenceScreen()
                            .findPreference("datamatrix_L1");
                    initEditText(decoder_l1_datamatrix, true);
                    decoder_l2_datamatrix = (EditTextPreference) getPreferenceScreen()
                            .findPreference("datamatrix_L2");
                    initEditText(decoder_l2_datamatrix, true);*/
                    decoder_type_datamatrix_inverse = (ListPreference) getPreferenceScreen()
                            .findPreference("scanner_datamatrix_inverse");
                    decoder_type_datamatrix_inverse.setOnPreferenceChangeListener(this);
                    decoder_type_datamatrix.setChecked(value_buffer[i++] == 1);
                    //decoder_l1_datamatrix.setSummary(String.valueOf(value_buffer[i++]));
                    //decoder_l2_datamatrix.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_type_datamatrix_inverse.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_datamatrix_inverse.setSummary( decoder_type_datamatrix_inverse.getEntry());
                    if(scannerType == 6 || scannerType == 8 || scannerType == 5 || scannerType == 11) {
                        if(decoder_type_datamatrix_inverse != null)
                            datamatrix.removePreference(decoder_type_datamatrix_inverse);
                    }
                    if(scannerType == 5 || scannerType == 8|| scannerType == 15) {
                        decoder_type_dm_size = new ListPreference(getActivity());
                        decoder_type_dm_size.setKey(Settings.System.DATAMATRIX_SYMBOL_SIZE);
                        decoder_type_dm_size.setTitle(R.string.scanner_symbols_size);
                        decoder_type_dm_size.setEntries(R.array.scanner_symbols_size_entries);
                        decoder_type_dm_size.setEntryValues(R.array.code_id_type_values);
                        decoder_type_dm_size.setOnPreferenceChangeListener(this);
                        datamatrix.addPreference(decoder_type_dm_size);
                        int val = value_buffer[i++];
                        val = val>=0?val:0;
                        decoder_type_dm_size.setValue(String.valueOf(val));
                        decoder_type_dm_size.setSummary(decoder_type_dm_size.getEntry());
                    }
                } catch (Exception e) {
                }
            }
        } else {
            if (datamatrix != null) {
                root.removePreference(datamatrix);
            }
        }
        if (setType == SymbologySettings.SET_Ean13) {
            if (ean13 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.EAN13_ENABLE,
                            PropertyID.EAN13_BOOKLANDEAN,
                            PropertyID.EAN13_BOOKLAND_FORMAT,
                            PropertyID.EAN13_SEND_CHECK,
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_EAN13 = (CheckBoxPreference) getPreferenceScreen().findPreference(
                            "decoder_type_EAN13");
                    decoder_type_EAN13_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_ean13_send_check");
                    decoder_type_EAN13_to_isbn = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_EAN13_to_isbn");
                    decoder_type_EAN13_to_issn = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_EAN13_to_issn");
                    decoder_type_EAN13.setChecked(value_buffer[i++] == 1);
                    decoder_type_EAN13_to_isbn.setChecked(value_buffer[i++] == 1);
                    decoder_type_EAN13_to_issn.setChecked(value_buffer[i++] == 1);
                    decoder_type_EAN13_send_check.setChecked(value_buffer[i++] == 1);
                    if(scannerType == 5 || scannerType == 8) {
                        if(decoder_type_EAN13_to_issn != null)
                            ean13.removePreference(decoder_type_EAN13_to_issn);
                    }
                } catch (Exception e) {

                }
            }
        } else {
            if (ean13 != null) {
                root.removePreference(ean13);
            }
        }
        if (setType == SymbologySettings.SET_Ean8) {
            if (ean8 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.EAN8_ENABLE,
                            PropertyID.EAN8_TO_EAN13,
                            PropertyID.EAN8_SEND_CHECK,
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_EAN8 = (CheckBoxPreference) getPreferenceScreen().findPreference(
                            "decoder_type_EAN8");
                    decoder_type_EAN8_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_ean8_send_check");
                    decoder_type_EAN8_to_ean13 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_EAN8_to_ean13");

                    decoder_type_EAN8.setChecked(value_buffer[i++] == 1);
                    decoder_type_EAN8_to_ean13.setChecked(value_buffer[i++] == 1);
                    decoder_type_EAN8_send_check.setChecked(value_buffer[i++] == 1);
                    if(scannerType == 5 || scannerType == 8) {
                        if(decoder_type_EAN8_to_ean13 != null)
                            ean8.removePreference(decoder_type_EAN8_to_ean13);
                    }
                } catch (Exception e) {

                }
            }
        } else {
            if (ean8 != null) {
                root.removePreference(ean8);
            }
        }
        if (setType == SymbologySettings.SET_Gs1_databar14) {
            if (gs1_databar14 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.GS1_14_ENABLE,
                            PropertyID.GS1_14_TO_UPC_EAN
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_gs1_databar14 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_RSS_14");
                    decoder_type_convert_to_upc_ean = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_convert_to_upc_ean");
                    
                    decoder_type_gs1_databar14.setChecked(value_buffer[i++] == 1);
                    decoder_type_convert_to_upc_ean.setChecked(value_buffer[i++] == 1);
                    if(scannerType == 5 || scannerType == 8) {
                        if(decoder_type_convert_to_upc_ean != null)
                            gs1_databar14.removePreference(decoder_type_convert_to_upc_ean);
                    }
                } catch (Exception e) {

                }
            }
        } else {
            if (gs1_databar14 != null) {
                root.removePreference(gs1_databar14);
            }
        }
        if (setType == SymbologySettings.SET_Gs1_databar_expanded) {
            if (gs1_expanded != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.GS1_EXP_ENABLE,
                            PropertyID.GS1_EXP_LENGTH1,
                            PropertyID.GS1_EXP_LENGTH2,
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_RSS_Expanded = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_RSS_Expanded");
                    decoder_l1_expanded = (EditTextPreference) getPreferenceScreen()
                            .findPreference("gs1_expanded_L1");
                    initEditText(decoder_l1_expanded, true);
                    decoder_l2_expanded = (EditTextPreference) getPreferenceScreen()
                            .findPreference("gs1_expanded_L2");
                    initEditText(decoder_l2_expanded, true);
                    decoder_type_RSS_Expanded.setChecked(value_buffer[i++] == 1);
                    decoder_l1_expanded.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_l2_expanded.setSummary(String.valueOf(value_buffer[i++]));
                    //urovo weiyu add on 2020-01-04 start
                    if ((android.os.Build.PWV_CUSTOM_CUSTOM.equals("UTE")||android.os.Build.PWV_CUSTOM_CUSTOM.equals("UTEWO")) && scannerType != 15) {
                        PreferenceCategory displayOptions = (PreferenceCategory) findPreference("gs1_expanded");
                        displayOptions.removePreference(findPreference("gs1_expanded_L1"));
                        displayOptions.removePreference(findPreference("gs1_expanded_L2"));
                    }
                    //urovo weiyu add on 2020-01-04 end
                } catch (Exception e) {

                }
            }
        } else {
            if (gs1_expanded != null) {
                root.removePreference(gs1_expanded);
            }
        }
        if (setType == SymbologySettings.SET_Gs1_databar_limited) {
            if (gs1_limited != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.GS1_LIMIT_ENABLE
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_gs1_Limited = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_RSS_Limited");
                    decoder_type_gs1_Limited.setChecked(value_buffer[i++] == 1);
                } catch (Exception e) {
                }
            }
        } else {
            if (gs1_limited != null) {
                root.removePreference(gs1_limited);
            }
        }
        if (setType == SymbologySettings.SET_Interleaved25) {
            if (Interleaved_2_of_5 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.I25_ENABLE,
                            PropertyID.I25_LENGTH1,
                            PropertyID.I25_LENGTH2,
                            PropertyID.I25_ENABLE_CHECK,
                            PropertyID.I25_SEND_CHECK,
                            PropertyID.I25_TO_EAN13
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_Interleaved_2_of_5 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_Interleaved_2_of_5");
                    decoder_L1_Interleaved_2_of_5 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L1_Interleaved_2_of_5");
                    initEditText(decoder_L1_Interleaved_2_of_5, true);
                    decoder_L2_Interleaved_2_of_5 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L2_Interleaved_2_of_5");
                    initEditText(decoder_L2_Interleaved_2_of_5, true);
                    decoder_type_Interleaved_2_of_5_en_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_Interleaved_2_of_5_en_check");
                    decoder_type_Interleaved_2_of_5_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_Interleaved_2_of_5_send_check");
                    decoder_Interleaved_2_of_5_to_ean13 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_Interleaved_2_of_5_to_ean13");

                    decoder_type_Interleaved_2_of_5.setChecked(value_buffer[i++] == 1);
                    decoder_L1_Interleaved_2_of_5.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_L2_Interleaved_2_of_5.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_type_Interleaved_2_of_5_en_check.setChecked(value_buffer[i++] == 1);
                    decoder_type_Interleaved_2_of_5_send_check.setChecked(value_buffer[i++] == 1);
                    decoder_Interleaved_2_of_5_to_ean13.setChecked(value_buffer[i++] == 1);
                    if(scannerType == 5 || scannerType == 8) {
                        if(decoder_Interleaved_2_of_5_to_ean13 != null)
                            Interleaved_2_of_5.removePreference(decoder_Interleaved_2_of_5_to_ean13);
                    }
                } catch (Exception e) {

                }
            }
        } else {
            if (Interleaved_2_of_5 != null) {
                root.removePreference(Interleaved_2_of_5);
            }
        }
        if (setType == SymbologySettings.SET_Matrix_25) {
            if (Matrix_2_of_5 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.M25_ENABLE
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_Matrix_2_of_5 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_Matrix_2_of_5");
                    /*decoder_L1_Matrix_2_of_5 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L1_Matrix_2_of_5");
                    initEditText(decoder_L1_Matrix_2_of_5, true);
                    decoder_L2_Matrix_2_of_5 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L2_Matrix_2_of_5");
                    initEditText(decoder_L2_Matrix_2_of_5, true);*/

                    decoder_type_Matrix_2_of_5.setChecked(value_buffer[i++] == 1);
                    //decoder_L1_Matrix_2_of_5.setSummary(String.valueOf(value_buffer[i++]));
                    //decoder_L2_Matrix_2_of_5.setSummary(String.valueOf(value_buffer[i++]));

                } catch (Exception e) {

                }
            }
        } else {
            if (Matrix_2_of_5 != null) {
                root.removePreference(Matrix_2_of_5);
            }
        }
        if (setType == SymbologySettings.SET_Maxicode) {
            if (maxicode != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.MAXICODE_ENABLE,
                            PropertyID.MAXICODE_SYMBOL_SIZE
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_maxicode = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_maxicode");
                    decoder_type_maxicode.setChecked(value_buffer[i++] == 1);
                    if(scannerType == 5 || scannerType == 8|| scannerType == 15) {
                        decoder_type_maxicode_size = new ListPreference(getActivity());
                        decoder_type_maxicode_size.setKey(Settings.System.MAXICODE_SYMBOL_SIZE);
                        decoder_type_maxicode_size.setTitle(R.string.scanner_symbols_size);
                        decoder_type_maxicode_size.setEntries(R.array.scanner_symbols_size_entries);
                        decoder_type_maxicode_size.setEntryValues(R.array.code_id_type_values);
                        decoder_type_maxicode_size.setOnPreferenceChangeListener(this);
                        maxicode.addPreference(decoder_type_maxicode_size);
                        int val = value_buffer[i++];
                        val = val>=0?val:0;
                        decoder_type_maxicode_size.setValue(String.valueOf(val));
                        decoder_type_maxicode_size.setSummary(decoder_type_maxicode_size.getEntry());
                    }
                } catch (Exception e) {
                }
            }
        } else {
            if (maxicode != null) {
                root.removePreference(maxicode);
            }
        }
        if (setType == SymbologySettings.SET_Micropdf417) {
            if (micropdf417 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.MICROPDF417_ENABLE
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_micropdf417 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_micropdf417");
                    decoder_type_micropdf417.setChecked(value_buffer[i++] == 1);

                } catch (Exception e) {

                }
            }
        } else {
            if (micropdf417 != null) {
                root.removePreference(micropdf417);
            }
        }
        if (setType == SymbologySettings.SET_MSI) {
            if (MSI != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.MSI_ENABLE,
                            PropertyID.MSI_LENGTH1,
                            PropertyID.MSI_LENGTH2, 
                            PropertyID.MSI_REQUIRE_2_CHECK,
                            PropertyID.MSI_SEND_CHECK,
                            PropertyID.MSI_CHECK_2_MOD_11,
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_MSI = (CheckBoxPreference) getPreferenceScreen().findPreference(
                            "decoder_type_MSI");
                    decoder_L1_of_MSI = (EditTextPreference) getPreferenceScreen().findPreference(
                            "decoder_L1_of_MSI");
                    initEditText(decoder_L1_of_MSI, true);
                    decoder_L2_of_MSI = (EditTextPreference) getPreferenceScreen().findPreference(
                            "decoder_L2_of_MSI");
                    initEditText(decoder_L2_of_MSI, true);
                    decoder_type_MSI_2_check = (ListPreference) getPreferenceScreen()
                            .findPreference("decoder_type_MSI_2_check");
                    decoder_type_MSI_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_MSI_send_check");
                    decoder_type_MSI_2_mod_11 = (ListPreference) getPreferenceScreen()
                            .findPreference("decoder_type_MSI_2_mod_11");

                    decoder_type_MSI.setChecked(value_buffer[i++] == 1);
                    decoder_L1_of_MSI.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_L2_of_MSI.setSummary(String.valueOf(value_buffer[i++]));
                    
                    decoder_type_MSI_2_check.setOnPreferenceChangeListener(this);
                    decoder_type_MSI_2_check.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_MSI_2_check.setSummary(decoder_type_MSI_2_check.getEntry());
                    
                    decoder_type_MSI_send_check.setChecked(value_buffer[i++] == 1);
                    
                    decoder_type_MSI_2_mod_11.setOnPreferenceChangeListener(this);
                    decoder_type_MSI_2_mod_11.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_MSI_2_mod_11.setSummary( decoder_type_MSI_2_mod_11.getEntry());
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (MSI != null) {
                root.removePreference(MSI);
            }
        }
        if (setType == SymbologySettings.SET_Code32) {
            if (code32 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.CODE32_ENABLE,
                            PropertyID.CODE32_SEND_START
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_code32 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code32");
                    decoder_type_code32_send_start = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code32_prefix");
                    decoder_type_code32.setChecked(value_buffer[i++] == 1);
                    decoder_type_code32_send_start.setChecked(value_buffer[i++] == 1);
                    if(scannerType == 5 || scannerType == 8) {
                        if(decoder_type_code32_send_start != null)
                            code32.removePreference(decoder_type_code32_send_start);
                    }
                } catch (Exception e) {

                }
            }
        } else {
            if (code32 != null) {
                root.removePreference(code32);
            }
        }
        if (setType == SymbologySettings.SET_Pdf47) {
            if (pdf417 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.PDF417_ENABLE
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_pdf417 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_pdf417");

                    decoder_type_pdf417.setChecked(value_buffer[i++] == 1);
                } catch (Exception e) {

                }
            }

        } else {
            if (pdf417 != null) {
                root.removePreference(pdf417);
            }
        }
        if (setType == SymbologySettings.SET_Postal) {
            if (postal != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.US_POSTNET_ENABLE,        //postal code
                            PropertyID.US_PLANET_ENABLE, 
                            PropertyID.US_POSTAL_SEND_CHECK,
                            PropertyID.USPS_4STATE_ENABLE, 
                            PropertyID.UPU_FICS_ENABLE, 
                            PropertyID.ROYAL_MAIL_ENABLE,
                            PropertyID.ROYAL_MAIL_SEND_CHECK,
                            PropertyID.AUSTRALIAN_POST_ENABLE,
                            PropertyID.KIX_CODE_ENABLE, 
                            PropertyID.JAPANESE_POST_ENABLE,
                    };
                    value_buffer = new int[id_buffer.length];
                    
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_postnet = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_postal_postnet");
                    decoder_type_planet = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_postal_planet");
                    decoder_type_postal_planet_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_postal_planet_send_check");
                    decoder_type_usps_4state = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_postal_usps4_state");
                    decoder_type_ups_fics = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_postal_upu_fics");
                    decoder_type_royal_mail = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_postal_royal_mail");
                    decoder_type_royal_mail_send_chk = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_postal_royal_mail_send_check");
                    decoder_type_australian_post = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_postal_australian");
                    decoder_type_kix_code = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_postal_kix");
                    decoder_type_japan_code = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_postal_japan");
                    decoder_type_postnet.setChecked(value_buffer[i++] == 1);
                    decoder_type_planet.setChecked(value_buffer[i++] == 1);
                    decoder_type_postal_planet_send_check.setChecked(value_buffer[i++] == 1);
                    decoder_type_usps_4state.setChecked(value_buffer[i++] == 1);
                    decoder_type_ups_fics.setChecked(value_buffer[i++] == 1);
                    decoder_type_royal_mail.setChecked(value_buffer[i++] == 1);
                    decoder_type_royal_mail_send_chk.setChecked(value_buffer[i++] == 1);
                    decoder_type_australian_post.setChecked(value_buffer[i++] == 1);
                    decoder_type_kix_code.setChecked(value_buffer[i++] == 1);
                    decoder_type_japan_code.setChecked(value_buffer[i++] == 1);
                } catch (Exception e) {

                }
            }
        } else {
            if (postal != null) {
                root.removePreference(postal);
            }
        }

        if (setType == SymbologySettings.SET_Qrcode) {
            if (qrcode != null) {

                try {
                    id_buffer = new int[] {
                            PropertyID.QRCODE_ENABLE,
                            PropertyID.QRCODE_INVERSE,
                            PropertyID.QRCODE_SYMBOL_SIZE
                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_qrcode = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_qrcode");
                    decoder_type_qrcode_inverse = (ListPreference) getPreferenceScreen()
                            .findPreference("scanner_qrcode_inverse");
                    decoder_type_qrcode_inverse.setOnPreferenceChangeListener(this);
                    decoder_type_qrcode.setChecked(value_buffer[i++] == 1);
                    decoder_type_qrcode_inverse.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_qrcode_inverse.setSummary( decoder_type_qrcode_inverse.getEntry());
                    if(scannerType != 15) { // rm se2100/se4710
                        if(decoder_type_qrcode_inverse != null)
                            qrcode.removePreference(decoder_type_qrcode_inverse);
                    }
                    if(scannerType == 5 || scannerType == 8|| scannerType == 15) {
                        decoder_type_qrcode_size = new ListPreference(getActivity());
                        decoder_type_qrcode_size.setKey(Settings.System.QRCODE_SYMBOL_SIZE);
                        decoder_type_qrcode_size.setTitle(R.string.scanner_symbols_size);
                        decoder_type_qrcode_size.setEntries(R.array.scanner_symbols_size_entries);
                        decoder_type_qrcode_size.setEntryValues(R.array.code_id_type_values);
                        decoder_type_qrcode_size.setOnPreferenceChangeListener(this);
                        qrcode.addPreference(decoder_type_qrcode_size);
                        int val = value_buffer[i++];
                        val = val>=0?val:0;
                        decoder_type_qrcode_size.setValue(String.valueOf(val));
                        decoder_type_qrcode_size.setSummary(decoder_type_qrcode_size.getEntry());
                    }
                } catch (Exception e) {
                }
            }
        } else {
            if (qrcode != null) {
                root.removePreference(qrcode);
            }
        }
        if (setType == SymbologySettings.SET_MicroQR) {
            if (microQrcode != null) {

                try {
                    id_buffer = new int[] {
                            PropertyID.MICROQRCODE_ENABLE,
                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_microqrcode = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_microqrcode");
                    decoder_type_microqrcode.setChecked(value_buffer[i++] == 1);
                } catch (Exception e) {
                }
            }
        } else {
            if (microQrcode != null) {
                root.removePreference(microQrcode);
            }
        }
        if (setType == SymbologySettings.SET_Discrete_25) {
            if (Discrete_2_of_5 != null) {

                try {
                    id_buffer = new int[] {
                            PropertyID.D25_ENABLE,
                            PropertyID.D25_LENGTH1,
                            PropertyID.D25_LENGTH2,

                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_Discrete_2_of_5 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_Discrete_2_of_5");
                    decoder_L1_Discrete_2_of_5 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L1_Discrete_2_of_5");
                    initEditText(decoder_L1_Discrete_2_of_5, true);
                    decoder_L2_Discrete_2_of_5 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L2_Discrete_2_of_5");
                    initEditText(decoder_L2_Discrete_2_of_5, true);

                    decoder_type_Discrete_2_of_5.setChecked(value_buffer[i++] == 1);
                    decoder_L1_Discrete_2_of_5.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_L2_Discrete_2_of_5.setSummary(String.valueOf(value_buffer[i++]));
                    if(scannerType == 5 || scannerType == 8) {
                        if(decoder_L1_Discrete_2_of_5 != null)
                            Discrete_2_of_5.removePreference(decoder_L1_Discrete_2_of_5);
                        if(decoder_L2_Discrete_2_of_5 != null)
                            Discrete_2_of_5.removePreference(decoder_L2_Discrete_2_of_5);
                    }
                } catch (Exception e) {
                }
            }
        } else {
            if (Discrete_2_of_5 != null) {
                root.removePreference(Discrete_2_of_5);
            }
        }
        if (setType == SymbologySettings.SET_Trioptic) {
            if (trioptic != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.TRIOPTIC_ENABLE
                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_trioptic = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_Trioptic_code39");
                    decoder_type_trioptic.setChecked(value_buffer[i++] == 1);

                } catch (Exception e) {
                }
            }
        } else {
            if (trioptic != null) {
                root.removePreference(trioptic);
            }
        }
        if (setType == SymbologySettings.SET_Upc_a) {
            if (upca != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.UPCA_ENABLE, PropertyID.UPCA_SEND_CHECK, PropertyID.UPCA_SEND_SYS,
                            PropertyID.UPCA_TO_EAN13,

                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_UPC_A = (CheckBoxPreference) getPreferenceScreen().findPreference(
                            "decoder_type_UPC_A");
                    decoder_type_UPC_A_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_UPC_A_send_check");
                    decoder_type_UPC_A_send_sys = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_UPC_A_send_sys");
                    decoder_type_UPC_A_to_ean13 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_UPC_A_to_ean13");

                    decoder_type_UPC_A.setChecked(value_buffer[i++] == 1);
                    decoder_type_UPC_A_send_check.setChecked(value_buffer[i++] == 1);
                    decoder_type_UPC_A_send_sys.setChecked(value_buffer[i++] == 1);
                    decoder_type_UPC_A_to_ean13.setChecked(value_buffer[i++] == 1);

                } catch (Exception e) {

                }
            }
        } else {
            if (upca != null) {
                root.removePreference(upca);
            }
        }
        if (setType == SymbologySettings.SET_Upc_e) {
            if (upce != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.UPCE_ENABLE,
                            PropertyID.UPCE_SEND_CHECK,
                            PropertyID.UPCE_SEND_SYS,
                            PropertyID.UPCE_TO_UPCA,

                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);

                    decoder_type_UPC_E = (CheckBoxPreference) getPreferenceScreen().findPreference(
                            "decoder_type_UPC_E");
                    decoder_type_UPC_E_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_UPC_E_send_check");
                    decoder_type_UPC_E_send_sys = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_UPC_E_send_sys");
                    decoder_type_UPC_E_to_upca = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_UPC_E_to_upca");

                    decoder_type_UPC_E.setChecked(value_buffer[i++] == 1);
                    decoder_type_UPC_E_send_check.setChecked(value_buffer[i++] == 1);
                    decoder_type_UPC_E_send_sys.setChecked(value_buffer[i++] == 1);
                    decoder_type_UPC_E_to_upca.setChecked(value_buffer[i++] == 1);
                } catch (Exception e) {
                }
            }

        } else {
            if (upce != null) {
                root.removePreference(upce);
            }
        }

        if (setType == SymbologySettings.SET_Upc_ena_extensions) {
            if (upc_ena_extensions != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.EAN_EXT_ENABLE_2_5_DIGIT, PropertyID.UCC_COUPON_EXT_CODE,
                            PropertyID.UPC_EAN_SECURITY_LEVEL,

                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_upc_ean_25 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("enable_25digit_extensions");
                    decoder_ucc_coupon_ext = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("ucc_coupon_extended");
                    decoder_upc_ean_security_level = (ListPreference)getPreferenceScreen()
                            .findPreference("symbology_upc_ean_security_level");
                    decoder_upc_ean_security_level.setOnPreferenceChangeListener(this);
                    decoder_type_upc_ean_25.setChecked(value_buffer[0] == 1);
                    decoder_ucc_coupon_ext.setChecked(value_buffer[1] == 1);
                    decoder_upc_ean_security_level.setValue(String.valueOf(value_buffer[2]));
                    decoder_upc_ean_security_level.setSummary( decoder_upc_ean_security_level.getEntry());
                    if(scannerType == 3 || scannerType == 6|| scannerType == 8 || scannerType == 5 || scannerType == 11) {
                        //decoder_ucc_coupon_ext.setEnabled(false);
                        //decoder_upc_ean_security_level.setEnabled(false);
                        if(decoder_ucc_coupon_ext != null)
                            upc_ena_extensions.removePreference(decoder_ucc_coupon_ext);
                        if(decoder_upc_ean_security_level != null)
                            upc_ena_extensions.removePreference(decoder_upc_ean_security_level);
                    }
                } catch (Exception e) {

                }

            }
        } else {
            if (upc_ena_extensions != null) {
                root.removePreference(upc_ena_extensions);
            }
        }
        
        if (setType == SymbologySettings.SET_Upc_e1) {
            if (upce1 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.UPCE1_ENABLE,
                            PropertyID.UPCE1_SEND_CHECK,
                            PropertyID.UPCE1_SEND_SYS,
                            PropertyID.UPCE1_TO_UPCA,

                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_UPC_E1= (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_UPC_E1");
                    decoder_type_UPC_E1_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_UPC_E1_send_check");
                    decoder_type_UPC_E1_send_sys = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_UPC_E1_send_sys");
                    decoder_type_UPC_E1_to_upca = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_UPC_E1_to_upca");
                     
                    decoder_type_UPC_E1.setChecked(value_buffer[0] == 1);
                    decoder_type_UPC_E1_send_check.setChecked(value_buffer[1] == 1);
                    decoder_type_UPC_E1_send_sys.setChecked(value_buffer[2] == 1);
                    decoder_type_UPC_E1_to_upca.setChecked(value_buffer[3] == 1);
                    if(scannerType == 6 || scannerType == 8 || scannerType == 5 || scannerType == 11) {
                        if(decoder_type_UPC_E1_to_upca != null)
                            upce1.removePreference(decoder_type_UPC_E1_to_upca);
                        if(decoder_type_UPC_E1_send_sys != null)
                            upce1.removePreference(decoder_type_UPC_E1_send_sys);
                        if(decoder_type_UPC_E1_send_check != null)
                            upce1.removePreference(decoder_type_UPC_E1_send_check);
                    }
                } catch (Exception e) {

                }

            }
        } else {
            if (upce1 != null) {
                root.removePreference(upce1);
            }
        }

        if (setType == SymbologySettings.SET_Chinese25) {
            if (chinese25 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.C25_ENABLE,

                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_chinese_2_of_5= (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_chinese_2_of_5");
                     
                    decoder_type_chinese_2_of_5.setChecked(value_buffer[0] == 1);
                } catch (Exception e) {

                }

            }
        } else {
            if (chinese25 != null) {
                root.removePreference(chinese25);
            }
        }

        if (setType == SymbologySettings.SET_Code11) {

            if (code11 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.CODE11_ENABLE,
                            PropertyID.CODE11_LENGTH1,
                            PropertyID.CODE11_LENGTH2,
                            PropertyID.CODE11_ENABLE_CHECK,
                            PropertyID.CODE11_SEND_CHECK,
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_code11 = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code11");
                    decoder_L1_of_code11 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L1_of_code11");
                    initEditText(decoder_L1_of_code11, true);
                    decoder_L2_of_code11 = (EditTextPreference) getPreferenceScreen()
                            .findPreference("decoder_L2_of_code11");
                    initEditText(decoder_L2_of_code11, true);
                    decoder_type_code11_enable_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code11_enable_check");
                    decoder_type_code11_send_check = (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_code11_send_check");
                    decoder_type_code11_check_mode = (ListPreference)getPreferenceScreen().findPreference(
                            "decoder_type_code11_check_mode");
                    decoder_type_code11.setChecked(value_buffer[i++] == 1);
                    decoder_L1_of_code11.setSummary(String.valueOf(value_buffer[i++]));
                    decoder_L2_of_code11.setSummary(String.valueOf(value_buffer[i++]));
                    if(decoder_type_code11_enable_check != null) {
                        code11.removePreference(decoder_type_code11_enable_check);
                    }
                    decoder_type_code11_check_mode.setOnPreferenceChangeListener(this);
                    decoder_type_code11_check_mode.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_code11_check_mode.setSummary( decoder_type_code11_check_mode.getEntry());
                    decoder_type_code11_send_check.setChecked(value_buffer[i++] == 1);
                } catch (Exception e) {
                }
            }
        } else {
            if (code11 != null) {
                root.removePreference(code11);
            }
        }
        if (setType == SymbologySettings.SET_Composite39) {
            if (composite_39 != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.COMPOSITE_TLC39_ENABLE,
                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_composite_39= (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_composite_39");
                    decoder_type_composite_39.setChecked(value_buffer[0] == 1);
                } catch (Exception e) {

                }
            }
        } else {
            if (composite_39 != null) {
                root.removePreference(composite_39);
            }
        }
        if (setType == SymbologySettings.SET_HANXIN) {
            if (hanxin != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.HANXIN_ENABLE,
                            PropertyID.HANXIN_INVERSE,
                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_hanxin= (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_hanxin");
                    decoder_type_hanxin_inverse = (ListPreference) getPreferenceScreen()
                            .findPreference("scanner_hanxin_inverse");
                    decoder_type_hanxin_inverse.setOnPreferenceChangeListener(this);
                    decoder_type_hanxin.setChecked(value_buffer[i++] == 1);
                    decoder_type_hanxin_inverse.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_hanxin_inverse.setSummary( decoder_type_hanxin_inverse.getEntry());
                    if(scannerType == 6  || scannerType == 8 || scannerType == 5 || scannerType == 11) {
                        if(decoder_type_hanxin_inverse != null)
                            hanxin.removePreference(decoder_type_hanxin_inverse);
                    }
                } catch (Exception e) {

                }
            }
        } else {
            if (hanxin != null) {
                root.removePreference(hanxin);
            }
        }
        if(setType == SymbologySettings.SET_OCR) {
            if (mOCR != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.DEC_OCR_MODE,
                            PropertyID.DEC_OCR_TEMPLATE,
                    };
                    value_buffer = new int[id_buffer.length];

                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    sym_ocr_mode_config = (ListPreference)getPreferenceScreen()
                            .findPreference("sym_ocr_mode_config");
                    sym_ocr_template_config = (ListPreference)getPreferenceScreen()
                            .findPreference("sym_ocr_template_config");
                    sym_ocr_user_template = (EditTextPreference)getPreferenceScreen()
                            .findPreference("sym_ocr_user_template");
                    sym_ocr_template_font = (Preference)getPreferenceScreen()
                            .findPreference("edit_ocr_user_template");
                    if(sym_ocr_mode_config != null){
                        sym_ocr_mode_config.setOnPreferenceChangeListener(this);
                        sym_ocr_mode_config.setValue(String.valueOf(value_buffer[0]));
                        sym_ocr_mode_config.setSummary(sym_ocr_mode_config.getEntry());
                    }
                    if(sym_ocr_template_config != null){
                        sym_ocr_template_config.setOnPreferenceChangeListener(this);
                        sym_ocr_template_config.setValue(String.valueOf(value_buffer[1]));
                        sym_ocr_template_config.setSummary(sym_ocr_template_config.getEntry());
                    }
                    if(sym_ocr_user_template != null) {
                        /*String userTemp = mScanManager.getPropertyString(PropertyID.DEC_OCR_USER_TEMPLATE);
                        sym_ocr_user_template.setOnPreferenceChangeListener(this);
                        sym_ocr_user_template.setSummary(userTemp);*/
                        mOCR.removePreference(sym_ocr_user_template);
                    }
                } catch (Exception e) {

                }
            }
        } else {
            if (mOCR != null) {
                root.removePreference(mOCR);
            }
        }
        if(setType == SymbologySettings.SET_DotCode) {
            if (mDotCode != null) {
                try {
                    id_buffer = new int[] {
                            PropertyID.DOTCODE_ENABLE
                    };
                    value_buffer = new int[id_buffer.length];
                    mScanManager.getPropertyInts(id_buffer, value_buffer);
                    decoder_type_dotcode= (CheckBoxPreference) getPreferenceScreen()
                            .findPreference("decoder_type_dotcode");
                    decoder_type_dotcode.setChecked(value_buffer[0] == 1);
                } catch (Exception e) {
                }
            }
        } else {
            if (mDotCode != null) {
                root.removePreference(mDotCode);
            }
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        try {
            if(setType == SymbologySettings.SET_OCR || setType == SymbologySettings.SET_Postal) {

            } else {
                if(value_buffer != null) {
                    mScanManager.setPropertyInts(id_buffer, value_buffer);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void enableSymbology(int id, boolean enable) {
        int[] index = new int[1];
        int[] value = new int[1];
        index[0] = id;
        value[0] = enable ? 1 : 0;
        mScanManager.setPropertyInts(index, value);
    }
    private void enableSymbology(int id, int enable) {
        int[] index = new int[1];
        int[] value = new int[1];
        index[0] = id;
        value[0] = enable;
        mScanManager.setPropertyInts(index, value);
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof EditTextPreference) {
            ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            return false;
        }
        try{
            if (preference == decoder_type_code39) {
                //mScanManager.enableSymbology(Symbology.CODE39, decoder_type_code39.isChecked());
                enableSymbology(PropertyID.CODE39_ENABLE, decoder_type_code39.isChecked());
                value_buffer[0] = decoder_type_code39.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_code39_enable_check) {
                value_buffer[3] = decoder_type_code39_enable_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_code39_send_check) {
                value_buffer[4] = decoder_type_code39_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_code39_full_ascii) {
                value_buffer[5] = decoder_type_code39_full_ascii.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_trioptic) {
                enableSymbology(PropertyID.TRIOPTIC_ENABLE, decoder_type_trioptic.isChecked());
                //mScanManager.enableSymbology(Symbology.TRIOPTIC, decoder_type_trioptic.isChecked());
                value_buffer[0] = decoder_type_trioptic.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_code32) {
                enableSymbology(PropertyID.CODE32_ENABLE, decoder_type_code32.isChecked());
                //mScanManager.enableSymbology(Symbology.CODE32, decoder_type_code32.isChecked());
                value_buffer[0] = decoder_type_code32.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_code32_send_start) {
                id_buffer = new int[] {
		    PropertyID.CODE32_SEND_START
                };
                value_buffer[0] = decoder_type_code32_send_start.isChecked() ? 1 : 0;
		mScanManager.setPropertyInts(id_buffer , value_buffer);
            } else if (preference == decoder_type_Discrete_2_of_5) {
                //mScanManager.enableSymbology(Symbology.DISCRETE25, decoder_type_Discrete_2_of_5.isChecked());
                enableSymbology(PropertyID.D25_ENABLE, decoder_type_Discrete_2_of_5.isChecked());

                value_buffer[0] = decoder_type_Discrete_2_of_5.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_Matrix_2_of_5) {
                //mScanManager.enableSymbology(Symbology.MATRIX25, decoder_type_Matrix_2_of_5.isChecked());
                enableSymbology(PropertyID.M25_ENABLE, decoder_type_Matrix_2_of_5.isChecked());
                value_buffer[0] = decoder_type_Matrix_2_of_5.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_Interleaved_2_of_5) {
                //mScanManager.enableSymbology(Symbology.INTERLEAVED25, decoder_type_Interleaved_2_of_5.isChecked());
                enableSymbology(PropertyID.I25_ENABLE, decoder_type_Interleaved_2_of_5.isChecked());
                value_buffer[0] = decoder_type_Interleaved_2_of_5.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_Interleaved_2_of_5_en_check) {
                value_buffer[3] = decoder_type_Interleaved_2_of_5_en_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_Interleaved_2_of_5_send_check) {
                value_buffer[4] = decoder_type_Interleaved_2_of_5_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_Interleaved_2_of_5_to_ean13) {
                value_buffer[5] = decoder_Interleaved_2_of_5_to_ean13.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_Codabar) {
                enableSymbology(PropertyID.CODABAR_ENABLE, decoder_type_Codabar.isChecked());
                //mScanManager.enableSymbology(Symbology.CODABAR, decoder_type_Codabar.isChecked());
                value_buffer[0] = decoder_type_Codabar.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_Codabar_enable_check) {
                value_buffer[5] = decoder_type_Codabar_enable_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_Codabar_send_check) {
                value_buffer[6] = decoder_type_Codabar_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_Codabar_send_start) {
                value_buffer[3] = decoder_type_Codabar_send_start.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_Codabar_clsi) {
                value_buffer[4] = decoder_type_Codabar_clsi.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_code93) {
                enableSymbology(PropertyID.CODE93_ENABLE, decoder_type_code93.isChecked());
                //mScanManager.enableSymbology(Symbology.CODE93, decoder_type_code93.isChecked());
                value_buffer[0] = decoder_type_code93.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_code128) {
                enableSymbology(PropertyID.CODE128_ENABLE, decoder_type_code128.isChecked());
                //mScanManager.enableSymbology(Symbology.CODE128, decoder_type_code128.isChecked());
                value_buffer[0] = decoder_type_code128.isChecked() ? 1 : 0;
            } else if (preference == decoder_code_isbt_128) {
                value_buffer[3] = decoder_code_isbt_128.isChecked() ? 1 : 0;
            } else if (preference == decoder_gs1128) {
                enableSymbology(PropertyID.CODE128_GS1_ENABLE, decoder_gs1128.isChecked());
                //mScanManager.enableSymbology(Symbology.GS1_128, decoder_gs1128.isChecked());
                value_buffer[0] = decoder_gs1128.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_A) {
                enableSymbology(PropertyID.UPCA_ENABLE, decoder_type_UPC_A.isChecked());
                //mScanManager.enableSymbology(Symbology.UPCA, decoder_type_UPC_A.isChecked());
                value_buffer[0] = decoder_type_UPC_A.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_A_send_check) {
                value_buffer[1] = decoder_type_UPC_A_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_A_send_sys) {
                value_buffer[2] = decoder_type_UPC_A_send_sys.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_A_to_ean13) {
                value_buffer[3] = decoder_type_UPC_A_to_ean13.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_E) {
                enableSymbology(PropertyID.UPCE_ENABLE, decoder_type_UPC_E.isChecked());
                //mScanManager.enableSymbology(Symbology.UPCE, decoder_type_UPC_E.isChecked());
                value_buffer[0] = decoder_type_UPC_E.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_E_send_check) {
                value_buffer[1] = decoder_type_UPC_E_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_E_send_sys) {
                value_buffer[2] = decoder_type_UPC_E_send_sys.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_E_to_upca) {
                value_buffer[3] = decoder_type_UPC_E_to_upca.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_E1) {
                enableSymbology(PropertyID.UPCE1_ENABLE, decoder_type_UPC_E1.isChecked());
                value_buffer[0] = decoder_type_UPC_E1.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_E1_send_check) {
                value_buffer[1] = decoder_type_UPC_E1_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_E1_send_sys) {
                value_buffer[2] = decoder_type_UPC_E1_send_sys.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_UPC_E1_to_upca) {
                value_buffer[3] = decoder_type_UPC_E1_to_upca.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_EAN13) {
                enableSymbology(PropertyID.EAN13_ENABLE, decoder_type_EAN13.isChecked());
                //mScanManager.enableSymbology(Symbology.EAN13, decoder_type_EAN13.isChecked());
                value_buffer[0] = decoder_type_EAN13.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_EAN13_send_check) {
                value_buffer[3] = decoder_type_EAN13_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_EAN13_to_isbn) {
                value_buffer[1] = decoder_type_EAN13_to_isbn.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_EAN13_to_issn) {
                value_buffer[2] = decoder_type_EAN13_to_issn.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_EAN8) {
                enableSymbology(PropertyID.EAN8_ENABLE, decoder_type_EAN8.isChecked());
                //mScanManager.enableSymbology(Symbology.EAN8, decoder_type_EAN8.isChecked());
                value_buffer[0] = decoder_type_EAN8.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_EAN8_send_check) {
                value_buffer[2] = decoder_type_EAN8_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_EAN8_to_ean13) {
                value_buffer[1] = decoder_type_EAN8_to_ean13.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_upc_ean_25) {
                value_buffer[0] = decoder_type_upc_ean_25.isChecked() ? 1 : 0;
            } else if (preference == decoder_ucc_coupon_ext) {
                value_buffer[1] = decoder_ucc_coupon_ext.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_MSI) {
                enableSymbology(PropertyID.MSI_ENABLE, decoder_type_MSI.isChecked());
                //mScanManager.enableSymbology(Symbology.MSI, decoder_type_MSI.isChecked());
                value_buffer[0] = decoder_type_MSI.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_MSI_send_check) {
                value_buffer[4] = decoder_type_MSI_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_gs1_databar14) {
                enableSymbology(PropertyID.GS1_14_ENABLE, decoder_type_gs1_databar14.isChecked());
                //mScanManager.enableSymbology(Symbology.GS1_14, decoder_type_gs1_databar14.isChecked());
                value_buffer[0] = decoder_type_gs1_databar14.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_convert_to_upc_ean) {
                value_buffer[1] = decoder_type_convert_to_upc_ean.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_gs1_Limited) {
                enableSymbology(PropertyID.GS1_LIMIT_ENABLE, decoder_type_gs1_Limited.isChecked());
                //mScanManager.enableSymbology(Symbology.GS1_LIMIT, decoder_type_gs1_Limited.isChecked());
                value_buffer[0] = decoder_type_gs1_Limited.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_RSS_Expanded) {
                enableSymbology(PropertyID.GS1_EXP_ENABLE, decoder_type_RSS_Expanded.isChecked());
                //mScanManager.enableSymbology(Symbology.GS1_EXP, decoder_type_RSS_Expanded.isChecked());
                value_buffer[0] = decoder_type_RSS_Expanded.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_pdf417) {
                enableSymbology(PropertyID.PDF417_ENABLE, decoder_type_pdf417.isChecked());
                //mScanManager.enableSymbology(Symbology.PDF417, decoder_type_pdf417.isChecked());
                value_buffer[0] = decoder_type_pdf417.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_micropdf417) {
                enableSymbology(PropertyID.MICROPDF417_ENABLE, decoder_type_micropdf417.isChecked());
                //mScanManager.enableSymbology(Symbology.MICROPDF417, decoder_type_micropdf417.isChecked());
                value_buffer[0] = decoder_type_micropdf417.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_composite_cc_c) {
                enableSymbology(PropertyID.COMPOSITE_CC_C_ENABLE, decoder_type_composite_cc_c.isChecked());
                //mScanManager.enableSymbology(Symbology.COMPOSITE_CC_C, decoder_type_composite_cc_c.isChecked());
                value_buffer[0] = decoder_type_composite_cc_c.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_composite_cc_ab) {
                enableSymbology(PropertyID.COMPOSITE_CC_AB_ENABLE, decoder_type_composite_cc_ab.isChecked());
                //mScanManager.enableSymbology(Symbology.COMPOSITE_CC_AB, decoder_type_composite_cc_ab.isChecked());
                value_buffer[0] = decoder_type_composite_cc_ab.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_datamatrix) {
                enableSymbology(PropertyID.DATAMATRIX_ENABLE, decoder_type_datamatrix.isChecked());
                //mScanManager.enableSymbology(Symbology.DATAMATRIX, decoder_type_datamatrix.isChecked());
                value_buffer[0] = decoder_type_datamatrix.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_maxicode) {
                enableSymbology(PropertyID.MAXICODE_ENABLE, decoder_type_maxicode.isChecked());
                //mScanManager.enableSymbology(Symbology.MAXICODE, decoder_type_maxicode.isChecked());
                value_buffer[0] = decoder_type_maxicode.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_qrcode) {
                enableSymbology(PropertyID.QRCODE_ENABLE, decoder_type_qrcode.isChecked());
                //mScanManager.enableSymbology(Symbology.QRCODE, decoder_type_qrcode.isChecked());
                value_buffer[0] = decoder_type_qrcode.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_microqrcode) {
                enableSymbology(PropertyID.MICROQRCODE_ENABLE, decoder_type_microqrcode.isChecked());
                value_buffer[0] = decoder_type_microqrcode.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_aztec) {
                enableSymbology(PropertyID.AZTEC_ENABLE, decoder_type_aztec.isChecked());
                //mScanManager.enableSymbology(Symbology.AZTEC, decoder_type_aztec.isChecked());
                value_buffer[0] = decoder_type_aztec.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_code11) {
                enableSymbology(PropertyID.CODE11_ENABLE, decoder_type_code11.isChecked());
                value_buffer[0] = decoder_type_code11.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_code11_enable_check) {
                value_buffer[3] = decoder_type_code11_enable_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_code11_send_check) {
                value_buffer[4] = decoder_type_code11_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_chinese_2_of_5) {
                enableSymbology(PropertyID.C25_ENABLE, decoder_type_chinese_2_of_5.isChecked());
                value_buffer[0] = decoder_type_chinese_2_of_5.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_composite_39) {
                enableSymbology(PropertyID.COMPOSITE_TLC39_ENABLE, decoder_type_composite_39.isChecked());
                value_buffer[0] = decoder_type_composite_39.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_hanxin) {
                enableSymbology(PropertyID.HANXIN_ENABLE, decoder_type_hanxin.isChecked());
                value_buffer[0] = decoder_type_hanxin.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_postnet) {
                enableSymbology(PropertyID.US_POSTNET_ENABLE, decoder_type_postnet.isChecked());
                value_buffer[0] = decoder_type_postnet.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_planet) {
                enableSymbology(PropertyID.US_PLANET_ENABLE, decoder_type_planet.isChecked());
                value_buffer[1] = decoder_type_planet.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_postal_planet_send_check) {
                enableSymbology(PropertyID.US_POSTAL_SEND_CHECK, decoder_type_postal_planet_send_check.isChecked());
                value_buffer[2] = decoder_type_postal_planet_send_check.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_usps_4state) {
                enableSymbology(PropertyID.USPS_4STATE_ENABLE, decoder_type_usps_4state.isChecked());
                value_buffer[3] = decoder_type_usps_4state.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_ups_fics) {
                enableSymbology(PropertyID.UPU_FICS_ENABLE, decoder_type_ups_fics.isChecked());
                value_buffer[4] = decoder_type_ups_fics.isChecked() ? 1 : 0;
            }else if (preference == decoder_type_royal_mail) {
                enableSymbology(PropertyID.ROYAL_MAIL_ENABLE, decoder_type_royal_mail.isChecked());
                value_buffer[5] = decoder_type_royal_mail.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_royal_mail_send_chk) {
                enableSymbology(PropertyID.ROYAL_MAIL_SEND_CHECK, decoder_type_royal_mail_send_chk.isChecked());
                value_buffer[6] =  decoder_type_royal_mail_send_chk.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_australian_post) {
                enableSymbology(PropertyID.AUSTRALIAN_POST_ENABLE, decoder_type_australian_post.isChecked());
                value_buffer[7] =  decoder_type_australian_post.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_kix_code) {
                enableSymbology(PropertyID.KIX_CODE_ENABLE, decoder_type_kix_code.isChecked());
                value_buffer[8] =  decoder_type_kix_code.isChecked() ? 1 : 0;
            } else if (preference == decoder_type_japan_code) {
                enableSymbology(PropertyID.JAPANESE_POST_ENABLE, decoder_type_japan_code.isChecked());
                value_buffer[9] =  decoder_type_japan_code.isChecked() ? 1 : 0;
            } else if(preference == sym_ocr_template_font) {
                showEditDialog(R.string.lable_ocr_user_template, "");
            } else if (preference == decoder_type_dotcode) {
                enableSymbology(PropertyID.DOTCODE_ENABLE, decoder_type_dotcode.isChecked());
                value_buffer[0] = decoder_type_dotcode.isChecked() ? 1 : 0;
            }

        } catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        if (preference == decoder_upc_ean_security_level) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[2] = value;
            decoder_upc_ean_security_level.setSummary( decoder_upc_ean_security_level.getEntries()[value]);
        } else if(preference == decoder_type_MSI_2_mod_11) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[5] = value;
            decoder_type_MSI_2_mod_11.setSummary( decoder_type_MSI_2_mod_11.getEntries()[value]);
        } else if(preference == decoder_type_MSI_2_check) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[3] = value;
            decoder_type_MSI_2_check.setSummary( decoder_type_MSI_2_check.getEntries()[value]);
        } else if(preference == decoder_type_aztec_inverse) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[1] = value;
            decoder_type_aztec_inverse.setSummary( decoder_type_aztec_inverse.getEntries()[value]);
        } else if (decoder_type_code11_check_mode == preference) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[3] = value;
            decoder_type_code11_check_mode.setSummary( decoder_type_code11_check_mode.getEntries()[value]);
        } else if(preference == decoder_type_datamatrix_inverse) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[1] = value;
            decoder_type_datamatrix_inverse.setSummary( decoder_type_datamatrix_inverse.getEntries()[value]);
        } else if(preference == decoder_type_qrcode_inverse) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[1] = value;
            decoder_type_qrcode_inverse.setSummary( decoder_type_qrcode_inverse.getEntries()[value]);
        } else if(preference == decoder_type_qrcode_size) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[2] = value;
            decoder_type_qrcode_size.setSummary( decoder_type_qrcode_size.getEntries()[value]);
        } else if(preference == decoder_type_dm_size) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[2] = value;
            decoder_type_dm_size.setSummary( decoder_type_dm_size.getEntries()[value]);
        } else if(preference == decoder_type_aztec_size) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[2] = value;
            decoder_type_aztec_size.setSummary( decoder_type_aztec_size.getEntries()[value]);
        } else if(preference == decoder_type_maxicode_size) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[1] = value;
            decoder_type_maxicode_size.setSummary( decoder_type_maxicode_size.getEntries()[value]);
        } else if(preference == decoder_type_hanxin_inverse) {
            int value = Integer.parseInt((String) newValue);
            value_buffer[1] = value;
            decoder_type_hanxin_inverse.setSummary( decoder_type_hanxin_inverse.getEntries()[value]);
        } else if (preference == sym_ocr_mode_config) {
            int[] update_id_buffer = new int[]{PropertyID.DEC_OCR_MODE};
            int value = Integer.parseInt((String) newValue);
            android.util.Log.i("debug", "sym_ocr_mode_config==================" + value);
            int[] update_value_buffer = new int[]{value} ;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            sym_ocr_mode_config.setSummary(sym_ocr_mode_config.getEntries()[value]);
        } else if (preference == sym_ocr_template_config) {
            int[] update_id_buffer = new int[]{PropertyID.DEC_OCR_TEMPLATE};
            int value = Integer.parseInt((String) newValue);
            android.util.Log.i("debug", "sym_ocr_template_config==================" + value);
            int[] update_value_buffer = new int[]{value} ;
            mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
            sym_ocr_template_config.setSummary(sym_ocr_template_config.getEntries()[value]);
        } else {
            if (preference instanceof EditTextPreference) {
                ((EditTextPreference) preference).setSummary(newValue.toString());
            }
            try{
                String value = newValue.toString();
                if (preference == decoder_L1_of_codabar) {//1 50
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 50) {
                        value_buffer[1] = Integer.parseInt(value);
                    } else {
                        showAlertToast(1, 50);
                        return false;
                    }
                } else if (preference == decoder_L2_of_codabar) {
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 50) {
                        value_buffer[2] = Integer.parseInt(value);
                    } else {
                        showAlertToast(1, 50);
                        return false;
                    }
                } else if(preference ==decoder_L1_of_code128) {//1 80
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 55) {
                        value_buffer[1] = Integer.parseInt(value);
                    } else {
                    decoder_L1_of_code128.setSummary(String.valueOf(value_buffer[1]));
                        showAlertToast(1, 55);
                        return false;
                    }
                } else if(preference ==decoder_L2_of_code128) { 
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 55) {
                        value_buffer[2] = Integer.parseInt(value);
                    } else {
                        decoder_L2_of_code128.setSummary(String.valueOf(value_buffer[2]));
                        showAlertToast(1, 55);
                        return false;
                    }
                } else if(preference ==decoder_c128_OutOfSpec) {
                    int len = Integer.parseInt(value);
                    if(len >= 0 && len <= 15) {
                        enableSymbology(PropertyID.C128_OUT_OF_SPEC,len);
                    } else {
                        showAlertToast(0, 15);
                        return false;
                    }
                } else if(preference ==decoder_L1_gs1128) {
                    value_buffer[1] = Integer.parseInt(value);
                } else if(preference ==decoder_L2_gs1128) { 
                    value_buffer[2] = Integer.parseInt(value);
                } else if (preference == decoder_L1_of_code39) {//1 50
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 50) {
                        value_buffer[1] = Integer.parseInt(value);
                    } else {
                        showAlertToast(1, 50);
                        return false;
                    }
                } else if (preference == decoder_L2_of_code39) {
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 50) {
                        value_buffer[2] = Integer.parseInt(value);
                    } else {
                        showAlertToast(1, 50);
                        return false;
                    }
                } else if (preference == decoder_L1_of_code93) {//1 50
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 50) {
                        value_buffer[1] = Integer.parseInt(value);
                    } else {
                        showAlertToast(1, 50);
                        return false;
                    }
                } else if (preference == decoder_L2_of_code93) {
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 50) {
                        value_buffer[2] = Integer.parseInt(value);
                    } else {
                        showAlertToast(1, 50);
                        return false;
                    }
                } else if (preference == decoder_l1_datamatrix) {
                    value_buffer[1] = Integer.parseInt(value);
                } else if (preference == decoder_l2_datamatrix) {
                    value_buffer[2] = Integer.parseInt(value);
                } else if (preference == decoder_l1_expanded) {//1 74
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 74) {
                        value_buffer[1] = Integer.parseInt(value);
                    } else {
                        showAlertToast(1, 74);
                        return false;
                    }
                } else if (preference == decoder_l2_expanded) {
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 74) {
                        value_buffer[2] = Integer.parseInt(value);
                    } else {
                        showAlertToast(1, 74);
                        return false;
                    }
                } else if (preference == decoder_L1_Interleaved_2_of_5) {//2 50
                    int len = Integer.parseInt(value);
                    if(len >= 2 && len <= 50) {
                        value_buffer[1] = Integer.parseInt(value);
                    } else {
                        showAlertToast(2, 50);
                        return false;
                    }
                } else if (preference == decoder_L2_Interleaved_2_of_5) {
                    int len = Integer.parseInt(value);
                    if(len >= 2 && len <= 50) {
                        value_buffer[2] = Integer.parseInt(value);
                    } else {
                        showAlertToast(2, 50);
                        return false;
                    }
                } else if (preference == decoder_L1_Matrix_2_of_5) {
                    value_buffer[1] = Integer.parseInt(value);
                } else if (preference == decoder_L2_Matrix_2_of_5) {
                    value_buffer[2] = Integer.parseInt(value);
                } else if (preference == decoder_L1_of_MSI) {//1 15
                    int len = Integer.parseInt(value);
                    if(len >= 4 && len <= 48) {
                        value_buffer[1] = Integer.parseInt(value);
                    } else {
                        showAlertToast(4, 48);
                        return false;
                    }
                } else if (preference == decoder_L2_of_MSI) {
                    int len = Integer.parseInt(value);
                    if(len >= 4 && len <= 48) {
                        value_buffer[2] = Integer.parseInt(value);
                    } else {
                        showAlertToast(4, 48);
                        return false;
                    }
                } else if (preference == decoder_L1_Discrete_2_of_5) {//1 50
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 50) {
                        value_buffer[1] = Integer.parseInt(value);
                    } else {
                        showAlertToast(1, 50);
                        return false;
                    }
                } else if (preference == decoder_L2_Discrete_2_of_5) {
                    int len = Integer.parseInt(value);
                    if(len >= 1 && len <= 50) {
                        value_buffer[2] = Integer.parseInt(value);
                    } else {
                        showAlertToast(1, 50);
                        return false;
                    }
                } else if (preference == decoder_L1_of_code11) {//1 50
                    int len = Integer.parseInt(value);
                    if(len >= 4 && len <= 55) {
                        value_buffer[1] = Integer.parseInt(value);
                    } else {
                        showAlertToast(4, 55);
                        return false;
                    }
                } else if (preference == decoder_L2_of_code11) {
                    int len = Integer.parseInt(value);
                    if(len >= 4 && len <= 55) {
                        value_buffer[2] = Integer.parseInt(value);
                    } else {
                        showAlertToast(4, 55);
                        return false;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return true;
    }
    private void showAlertToast(int min, int max) {
        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_symbology_length_range), min, max), Toast.LENGTH_LONG).show();
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
        ArrayAdapter adapter1= ArrayAdapter.createFromResource(
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
                android.util.Log.i("Scan-debug", "position, " +position  +" templateFont id " + templateFont);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putInt("user_template_font", position);
                editor.commit();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });
        ArrayAdapter adapter2= ArrayAdapter.createFromResource(
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
                android.util.Log.i("debug", "position, " +position  +" templateFont id " + templateCharacter);
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
                            if(TextUtils.isEmpty(lablePrefix) == false) {
                                int length = Integer.parseInt(lablePrefix); if(length < 1 || length > 50) return;
                                SharedPreferences.Editor editor = sharedPrefs.edit();
                                editor.putString("user_template_font_length", ""+length);
                                editor.commit();
                                StringBuffer sb = new StringBuffer();
                                sb.append(1).append(templateFont);
                                for(int i = 0; i < length; i++) {
                                    sb.append(templateCharacter);
                                }
                                sb.append(0);
                                android.util.Log.i("n6603", " new template:" + sb.toString());
                                //sym_ocr_user_template.setSummary(sb.toString());
                                mScanManager.setPropertyString(PropertyID.DEC_OCR_USER_TEMPLATE, sb.toString());
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

    private void dismissDialog() {
        if (mOkDialog == null) return;
        mOkDialog.dismiss();
        mOkDialog = null;
    }
}
