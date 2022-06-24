package com.ubx.scanwedge.settings.fragments;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;

import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.os.Bundle;
import android.preference.SwitchPreference;
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
import android.widget.Toast;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.scanwedge.settings.utils.ScannerAdapter;
import com.ubx.database.helper.USettings;

public class DecoderProperties extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private int profileId;
    private String symbologyType = "";
    private int scannerType = 0;
    private int[] id_buffer;
    private String[] propertyName_buffer;
    private int[] value_buffer;
    private int[] length_value_buffer;
    private int[] length_id_buffer;
    private PreferenceScreen root;

    private PreferenceCategory aztec;
    private PreferenceCategory codabar;
    private PreferenceCategory code_128;
    private PreferenceCategory gs1_128;
    private PreferenceCategory code_39;
    private PreferenceCategory code_93;
    private PreferenceCategory composite;
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
    private PreferenceCategory hanxin;
    private PreferenceCategory postal;
    private PreferenceCategory mDotCode;

    private SwitchPreference decoder_type_aztec;
    private ListPreference decoder_type_aztec_inverse;
    private EditTextPreference decoder_type_aztec_userid;
    private EditTextPreference decoder_type_aztec_l1;
    private EditTextPreference decoder_type_aztec_l2;
    private ListPreference decoder_type_aztec_size;

    private SwitchPreference decoder_type_Codabar;
    private SwitchPreference decoder_type_Codabar_enable_check;
    private SwitchPreference decoder_type_Codabar_send_check;
    private SwitchPreference decoder_type_Codabar_send_start;
    private SwitchPreference decoder_type_Codabar_concatenate;
    private SwitchPreference decoder_type_Codabar_notis_editing;
    private SwitchPreference decoder_type_Codabar_clsi_editing;
    private EditTextPreference decoder_L1_of_codabar;
    private EditTextPreference decoder_L2_of_codabar;
    private EditTextPreference decoder_codabar_userid;

    private SwitchPreference decoder_type_code128;
    private EditTextPreference decoder_L1_of_code128;
    private EditTextPreference decoder_L2_of_code128;
    private SwitchPreference decoder_code_isbt_128;
    private SwitchPreference decoder_check_isbt_table;
    private ListPreference decoder_isbt_concatenation_mode;
    private ListPreference decoder_type_code128_security_level;
    private ListPreference decoder_type_code128_quiet_zone;
    private SwitchPreference decoder_gs1128;
    private EditTextPreference decoder_L1_gs1128;
    private EditTextPreference decoder_L2_gs1128;
    private EditTextPreference decoder_gs1128_userid;

    private SwitchPreference decoder_type_code39;
    private SwitchPreference decoder_type_code39_enable_check;
    private SwitchPreference decoder_type_code39_send_check;
    private SwitchPreference decoder_type_code39_full_ascii;
    private SwitchPreference decoder_type_code39_start_stop;
    private ListPreference decoder_type_code39_security_level;
    private EditTextPreference decoder_L1_of_code39;
    private EditTextPreference decoder_L2_of_code39;
    private SwitchPreference decoder_type_code39_quiet_zone;
    private EditTextPreference decoder_code39_userid;

    private SwitchPreference decoder_type_code93;
    private EditTextPreference decoder_L1_of_code93;
    private EditTextPreference decoder_L2_of_code93;
    private EditTextPreference decoder_code93_userid;

    private SwitchPreference decoder_type_code11;
    private EditTextPreference decoder_L1_of_code11;
    private EditTextPreference decoder_L2_of_code11;
    private SwitchPreference decoder_type_code11_enable_check;
    private SwitchPreference decoder_type_code11_send_check;
    private ListPreference decoder_type_code11_check_mode;

    private SwitchPreference decoder_type_composite_cc_ab;
    private ListPreference decoder_type_composite_upc_mode;
    private EditTextPreference decoder_L1_composite_cc_ab;
    private EditTextPreference decoder_L2_composite_cc_ab;
    private EditTextPreference decoder_composite_cc_ab_userid;

    private SwitchPreference decoder_type_composite_cc_c;
    private EditTextPreference decoder_L1_composite_cc_c;
    private EditTextPreference decoder_L2_composite_cc_c;
    private EditTextPreference decoder_composite_cc_c_userid;

    private SwitchPreference decoder_type_datamatrix;
    private EditTextPreference decoder_type_datamatrix_userid;
    private ListPreference decoder_type_datamatrix_inverse;
    private EditTextPreference decoder_l1_datamatrix;
    private EditTextPreference decoder_l2_datamatrix;
    private ListPreference decoder_type_dm_size;

    private SwitchPreference decoder_type_EAN13;
    private SwitchPreference decoder_type_EAN13_send_check;
    private SwitchPreference decoder_type_EAN13_to_isbn;
    private SwitchPreference decoder_type_EAN13_to_issn;
    private EditTextPreference decoder_ean13_userid;

    private SwitchPreference decoder_type_EAN8;
    private SwitchPreference decoder_type_EAN8_send_check;
    private SwitchPreference decoder_type_EAN8_to_ean13;
    private EditTextPreference decoder_ean8_userid;

    private SwitchPreference decoder_type_gs1_databar14;
    private SwitchPreference decoder_type_convert_to_upc_ean;

    private SwitchPreference decoder_type_RSS_Expanded;
    private EditTextPreference decoder_rss_exp_userid;
    private EditTextPreference decoder_l1_expanded;
    private EditTextPreference decoder_l2_expanded;

    private SwitchPreference decoder_type_gs1_Limited;
    private ListPreference decoder_type_gs1_limit_security_level;
    private EditTextPreference decoder_rss_limit_userid;

    private SwitchPreference decoder_type_Interleaved_2_of_5;
    private SwitchPreference decoder_type_Interleaved_2_of_5_en_check;
    private SwitchPreference decoder_type_Interleaved_2_of_5_send_check;
    private EditTextPreference decoder_L1_Interleaved_2_of_5;
    private EditTextPreference decoder_L2_Interleaved_2_of_5;
    private SwitchPreference decoder_Interleaved_2_of_5_to_ean13;
    private ListPreference decoder_Int25_reduced_quiet_zone;
    private ListPreference decoder_Int25_security_level;

    private SwitchPreference decoder_type_Matrix_2_of_5;
    private EditTextPreference decoder_L1_Matrix_2_of_5;
    private EditTextPreference decoder_L2_Matrix_2_of_5;
    private SwitchPreference decoder_type_Matrix25_enable_check;
    private SwitchPreference decoder_type_Matrix25_send_check;
    private EditTextPreference decoder_Matrix_2_of_5_userid;

    private SwitchPreference decoder_type_maxicode;
    private EditTextPreference decoder_type_maxicode_userid;
    private EditTextPreference decoder_L1_maxicode;
    private EditTextPreference decoder_L2_maxicode;
    private ListPreference decoder_type_maxicode_size;

    private SwitchPreference decoder_type_micropdf417;
    private EditTextPreference decoder_type_micropdf417_userid;
    private EditTextPreference decoder_L1_micropdf417;
    private EditTextPreference decoder_L2_micropdf417;

    private SwitchPreference decoder_type_MSI;
    private ListPreference decoder_type_MSI_2_check;
    private SwitchPreference decoder_type_MSI_send_check;
    private ListPreference decoder_type_MSI_2_mod_11;
    private EditTextPreference decoder_L1_of_MSI;
    private EditTextPreference decoder_L2_of_MSI;
    private EditTextPreference decoder_MSI_userid;

    private SwitchPreference decoder_type_code32;
    private SwitchPreference decoder_type_code32_send_start;
    private EditTextPreference decoder_code32_userid;

    private SwitchPreference decoder_type_pdf417;
    private EditTextPreference decoder_type_pdf417_userid;
    private EditTextPreference decoder_L1_pdf417;
    private EditTextPreference decoder_L2_pdf417;

    private SwitchPreference decoder_type_ups_fics;
    private SwitchPreference decoder_type_australian_post;
    private EditTextPreference decoder_type_australian_post_userid;
    private SwitchPreference decoder_type_Canadian_post;

    private SwitchPreference decoder_type_japan_code;
    private SwitchPreference decoder_type_korean_code;
    private EditTextPreference decoder_type_japan_code_userid;

    private SwitchPreference decoder_type_kix_code;
    private EditTextPreference decoder_type_kix_code_userid;

    private SwitchPreference decoder_type_royal_mail;
    private EditTextPreference decoder_type_royal_mail_userid;
    private SwitchPreference decoder_type_royal_mail_send_chk;

    private SwitchPreference decoder_type_planet;
    private SwitchPreference decoder_type_postal_planet_send_check;
    private EditTextPreference decoder_planet_userid;

    private SwitchPreference decoder_type_postnet;
    private EditTextPreference decoder_postnet_userid;

    private SwitchPreference decoder_type_usps_4state;
    private EditTextPreference decoder_usps_4state_userid;
    private ListPreference scanner_postal_symbologies;

    private SwitchPreference decoder_type_qrcode;
    private EditTextPreference decoder_type_qrcode_userid;
    private ListPreference decoder_type_qrcode_inverse;
    private EditTextPreference decoder_L1_qrcode;
    private EditTextPreference decoder_L2_qrcode;
    private SwitchPreference decoder_type_microqrcode;
    private ListPreference decoder_type_qrcode_size;

    private SwitchPreference decoder_type_Discrete_2_of_5;
    private EditTextPreference decoder_L1_Discrete_2_of_5;
    private EditTextPreference decoder_L2_Discrete_2_of_5;
    private EditTextPreference decoder_Discrete_2_of_5_userid;

    private SwitchPreference decoder_type_trioptic;
    private EditTextPreference decoder_trioptic_userid;

    private SwitchPreference decoder_type_UPC_A;
    private SwitchPreference decoder_type_UPC_A_send_check;
    private SwitchPreference decoder_type_UPC_A_send_sys;
    private ListPreference decoder_type_UPC_A_preamble_sys;
    private SwitchPreference decoder_type_UPC_A_to_ean13;
    private EditTextPreference decoder_upca_userid;

    private SwitchPreference decoder_type_UPC_E;
    private SwitchPreference decoder_type_UPC_E_send_check;
    private SwitchPreference decoder_type_UPC_E_send_sys;
    private ListPreference decoder_type_UPC_E_preamble_sys;
    private SwitchPreference decoder_type_UPC_E_to_upca;
    private EditTextPreference decoder_upce_userid;

    private SwitchPreference decoder_type_UPC_E1;
    private SwitchPreference decoder_type_UPC_E1_send_check;
    private SwitchPreference decoder_type_UPC_E1_send_sys;
    private ListPreference decoder_type_UPC_E1_preamble_sys;
    private SwitchPreference decoder_type_UPC_E1_to_upca;

    private SwitchPreference decoder_type_upc_ean_25;
    private SwitchPreference decoder_convert_databar_upc_ean;
    private SwitchPreference decoder_upc_reduced_quiet_zone;
    private ListPreference decoder_coupon_report_mode;
    private SwitchPreference decoder_bookland;
    private ListPreference decoder_bookland_format;
    private SwitchPreference decoder_ean_zero_extend;
    private ListPreference decoder_upc_ean_security_level;
    private SwitchPreference decoder_ucc_coupon_ext;//UCC Coupon Extended Code
    private ListPreference decoder_upc_ean_supplemental_mode;

    private SwitchPreference decoder_type_chinese_2_of_5;
    private SwitchPreference decoder_type_composite_39;
    private SwitchPreference decoder_type_hanxin;
    private ListPreference decoder_type_hanxin_inverse;
    //OCR
    PreferenceCategory mOCR;
    private ListPreference sym_ocr_mode_config;
    private ListPreference sym_ocr_template_config;
    private Preference sym_ocr_template_font;
    private EditTextPreference sym_ocr_user_template;
    private SwitchPreference decoder_type_dotcode;

    private ScanWedgeApplication mApplication;
    private int id;

    private void initEditText(EditTextPreference keyEditText, boolean number) {

        keyEditText.getEditText().setInputType(number ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
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
        getActivity().setTitle(R.string.scanner_barcode_param);
        //getActivity().getActionBar().setDisplayShowCustomEnabled(false);

        Bundle args = getArguments();
        symbologyType = args != null ? args.getString("symbologyType"): "";
        scannerType = args != null ? args.getInt("scanType") : 0;
        profileId = args != null ? args.getInt("profileId") : USettings.Profile.DEFAULT_ID;
        addPreferencesFromResource(R.xml.scanner_settings);
        mApplication = (ScanWedgeApplication) getActivity().getApplication();

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
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        updateActionBar();
        root = this.getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        root = this.getPreferenceScreen();
        int i = 0;
        if (Settings.System.AZTEC_ENABLE.equals(symbologyType)) {
            aztec = new PreferenceCategory(getActivity());
            aztec.setTitle(R.string.scanner_symbology_aztec);
            root.addPreference(aztec);
            decoder_type_aztec = new SwitchPreference(getActivity());
            decoder_type_aztec.setKey(Settings.System.AZTEC_ENABLE);
            decoder_type_aztec.setTitle(R.string.scanner_symbology_enable);
            /*decoder_type_aztec.setSummaryOn(R.string.scanner_symbology_enable);
            decoder_type_aztec.setSummaryOff(R.string.scanner_symbology_disable);*/
            aztec.addPreference(decoder_type_aztec);
            decoder_type_aztec_inverse = new ListPreference(getActivity());
            decoder_type_aztec_inverse.setKey(Settings.System.AZTEC_INVERSE);
            decoder_type_aztec_inverse.setTitle(R.string.scanner_inverse);
            decoder_type_aztec_inverse.setEntries(R.array.image_2d_inverse_decoder_entries);
            decoder_type_aztec_inverse.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_type_aztec_inverse.setOnPreferenceChangeListener(this);

            decoder_type_aztec_size = new ListPreference(getActivity());
            decoder_type_aztec_size.setKey(Settings.System.AZTEC_SYMBOL_SIZE);
            decoder_type_aztec_size.setTitle(R.string.scanner_symbols_size);
            decoder_type_aztec_size.setEntryValues(R.array.code_id_type_values);
            decoder_type_aztec_size.setEntries(R.array.scanner_symbols_size_entries);
            decoder_type_aztec_size.setOnPreferenceChangeListener(this);

            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                aztec.addPreference(decoder_type_aztec_inverse);
            }
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                aztec.addPreference(decoder_type_aztec_size);
            }
            int value = mApplication.getPropertyInt(profileId, Settings.System.AZTEC_ENABLE, 0);
            decoder_type_aztec.setEnabled(value == 1);
            if (decoder_type_aztec_inverse != null) {
                value = mApplication.getPropertyInt(profileId, Settings.System.AZTEC_INVERSE, 0);
                decoder_type_aztec_inverse.setValue(String.valueOf(value));
                decoder_type_aztec_inverse.setSummary(decoder_type_aztec_inverse.getEntry());
            }
            value = mApplication.getPropertyInt(profileId, Settings.System.AZTEC_INVERSE, 0);
            decoder_type_aztec_size.setValue(String.valueOf(value));
            decoder_type_aztec_size.setSummary(decoder_type_aztec_size.getEntry());
        } else if (Settings.System.C25_ENABLE.equals(symbologyType)) {
            PreferenceCategory chinese25 = new PreferenceCategory(getActivity());
            chinese25.setTitle(R.string.scanner_symbology_chinese_25);
            root.addPreference(chinese25);
            decoder_type_chinese_2_of_5 = new SwitchPreference(getActivity());
            decoder_type_chinese_2_of_5.setKey(Settings.System.C25_ENABLE);
            decoder_type_chinese_2_of_5.setTitle(R.string.scanner_symbology_enable);
            chinese25.addPreference(decoder_type_chinese_2_of_5);

            int value = mApplication.getPropertyInt(profileId, Settings.System.C25_ENABLE, 0);
            decoder_type_chinese_2_of_5.setEnabled(value == 1);
        } else if (Settings.System.CODABAR_ENABLE.equals(symbologyType)) {
            codabar = new PreferenceCategory(getActivity());
            codabar.setTitle(R.string.scanner_symbology_codabar);
            root.addPreference(codabar);
            decoder_type_Codabar = new SwitchPreference(getActivity());
            decoder_type_Codabar.setKey(Settings.System.CODABAR_ENABLE);
            decoder_type_Codabar.setTitle(R.string.scanner_symbology_enable);
            decoder_L1_of_codabar = new EditTextPreference(getActivity());
            initEditText(decoder_L1_of_codabar, true);
            decoder_L1_of_codabar.setKey(Settings.System.CODABAR_LENGTH1);
            decoder_L1_of_codabar.setTitle(R.string.scanner_symbology_user_l1);
            decoder_L2_of_codabar = new EditTextPreference(getActivity());
            decoder_L2_of_codabar.setKey(Settings.System.CODABAR_LENGTH2);
            decoder_L2_of_codabar.setTitle(R.string.scanner_symbology_user_l2);
            initEditText(decoder_L2_of_codabar, true);
            codabar.addPreference(decoder_type_Codabar);
            codabar.addPreference(decoder_L1_of_codabar);
            codabar.addPreference(decoder_L2_of_codabar);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                /*CLSI Editing 如果条码长度14(不包含前后字母abcd)，使能后会在条码第1 ，5 ，10位置后面加入空格。同时不输出条码的前后a b c d字符
                Enable this parameter to strip the start and stop characters and insert a space after the first, fifth, and tenth
                characters of a 14-character Codabar symbol. Enable this if the host system requires this data format.
                NOTE
                Symbol length does not include start and stop characters.*/
                decoder_type_Codabar_clsi_editing = new SwitchPreference(getActivity());
                decoder_type_Codabar_clsi_editing.setKey(Settings.System.CODABAR_CLSI);
                decoder_type_Codabar_clsi_editing.setTitle(R.string.scanner_symbology_codabar_clsi_editing);
                decoder_type_Codabar_notis_editing = new SwitchPreference(getActivity());
                decoder_type_Codabar_notis_editing.setKey(Settings.System.CODABAR_NOTIS);
                decoder_type_Codabar_notis_editing.setTitle(R.string.scanner_symbology_codabar_notis_editing);
                codabar.addPreference(decoder_type_Codabar_clsi_editing);
                codabar.addPreference(decoder_type_Codabar_notis_editing);
            } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                decoder_type_Codabar_enable_check = new SwitchPreference(getActivity());
                decoder_type_Codabar_enable_check.setKey(Settings.System.CODABAR_ENABLE_CHECK);
                decoder_type_Codabar_enable_check.setTitle(R.string.scanner_symbology_enable_checksum);
                decoder_type_Codabar_send_check = new SwitchPreference(getActivity());
                decoder_type_Codabar_send_check.setKey(Settings.System.CODABAR_SEND_CHECK);
                decoder_type_Codabar_send_check.setTitle(R.string.scanner_symbology_send_checksum);
                decoder_type_Codabar_send_start = new SwitchPreference(getActivity());
                decoder_type_Codabar_send_start.setKey(Settings.System.CODABAR_SEND_START);
                decoder_type_Codabar_send_start.setTitle(R.string.scanner_symbology_send_start_stop);
                codabar.addPreference(decoder_type_Codabar_enable_check);
                codabar.addPreference(decoder_type_Codabar_send_check);
                codabar.addPreference(decoder_type_Codabar_send_start);

                /*decoder_type_Codabar_concatenate = new SwitchPreference(getActivity());
                decoder_type_Codabar_concatenate.setKey(Settings.System.CODABAR_CONCATENATE);
                decoder_type_Codabar_concatenate.setTitle(R.string.scanner_symbology_codabar_concatenate);
                codabar.addPreference(decoder_type_Codabar_concatenate);*/
            }

            id_buffer = new int[]{
                    PropertyID.CODABAR_ENABLE,
                    PropertyID.CODABAR_LENGTH1,
                    PropertyID.CODABAR_LENGTH2,
                    PropertyID.CODABAR_ENABLE_CHECK,
                    PropertyID.CODABAR_SEND_CHECK,
                    PropertyID.CODABAR_SEND_START,
                    //PropertyID.CODABAR_CONCATENATE,
                    PropertyID.CODABAR_NOTIS,
                    PropertyID.CODABAR_CLSI,
            };
            propertyName_buffer = new String[]{
                    Settings.System.CODABAR_ENABLE,
                    Settings.System.CODABAR_LENGTH1,
                    Settings.System.CODABAR_LENGTH2,
                    Settings.System.CODABAR_ENABLE_CHECK,
                    Settings.System.CODABAR_SEND_CHECK,
                    Settings.System.CODABAR_SEND_START,
                    //Settings.System.CODABAR_CONCATENATE,
                    Settings.System.CODABAR_NOTIS,
                    Settings.System.CODABAR_CLSI,
            };
            value_buffer = new int[id_buffer.length];
            length_id_buffer = new int[]{
                    PropertyID.CODABAR_LENGTH1,
                    PropertyID.CODABAR_LENGTH2,
            };
            length_value_buffer = new int[length_id_buffer.length];
            mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
            if (decoder_type_Codabar != null) {
                decoder_type_Codabar.setChecked(value_buffer[i++] == 1);
            } else {
                i++;
            }
            if (decoder_L1_of_codabar != null) {
                decoder_L1_of_codabar.setSummary(String.valueOf(value_buffer[i++]));
            } else {
                i++;
            }
            if (decoder_L2_of_codabar != null) {
                decoder_L2_of_codabar.setSummary(String.valueOf(value_buffer[i++]));
            } else {
                i++;
            }
            if (decoder_type_Codabar_enable_check != null) {
                decoder_type_Codabar_enable_check.setChecked(value_buffer[i++] == 1);
            } else {
                i++;
            }
            if (decoder_type_Codabar_send_check != null) {
                decoder_type_Codabar_send_check.setChecked(value_buffer[i++] == 1);
            } else {
                i++;
            }
            if (decoder_type_Codabar_send_start != null) {
                decoder_type_Codabar_send_start.setChecked(value_buffer[i++] == 1);
            } else {
                i++;
            }
            if (decoder_type_Codabar_notis_editing != null) {
                decoder_type_Codabar_notis_editing.setChecked(value_buffer[i++] == 1);
            } else {
                i++;
            }
            if (decoder_type_Codabar_clsi_editing != null) {
                decoder_type_Codabar_clsi_editing.setChecked(value_buffer[i++] == 1);
            } else {
                i++;
            }
        } else if (Settings.System.CODE11_ENABLE.equals(symbologyType)) {
            code11 = new PreferenceCategory(getActivity());
            code11.setKey(getString(R.string.scanner_symbology_code11));
            code11.setTitle(R.string.scanner_symbology_code11);
            root.addPreference(code11);
            decoder_type_code11 = new SwitchPreference(getActivity());
            decoder_type_code11.setKey(Settings.System.CODE11_ENABLE);
            decoder_type_code11.setTitle(R.string.scanner_symbology_enable);
            decoder_L1_of_code11 = new EditTextPreference(getActivity());
            initEditText(decoder_L1_of_code11, true);
            decoder_L1_of_code11.setKey(Settings.System.CODE11_LENGTH1);
            decoder_L1_of_code11.setTitle(R.string.scanner_symbology_user_l1);
            decoder_L2_of_code11 = new EditTextPreference(getActivity());
            decoder_L2_of_code11.setKey(Settings.System.CODE11_LENGTH2);
            decoder_L2_of_code11.setTitle(R.string.scanner_symbology_user_l2);
            initEditText(decoder_L2_of_code11, true);
            code11.addPreference(decoder_type_code11);
            code11.addPreference(decoder_L1_of_code11);
            code11.addPreference(decoder_L2_of_code11);

            if (ScannerAdapter.isSupportPreference(scannerType, Settings.System.CODE11_ENABLE_CHECK)) {
                decoder_type_code11_enable_check = new SwitchPreference(getActivity());
                decoder_type_code11_enable_check.setKey(Settings.System.CODE11_ENABLE_CHECK);
                decoder_type_code11_enable_check.setTitle(R.string.scanner_symbology_enable_checksum);
                code11.addPreference(decoder_type_code11_enable_check);
                decoder_type_code11_check_mode = new ListPreference(getActivity());
                decoder_type_code11_check_mode.setTitle(R.string.scanner_symbology_checksum_mode);
                decoder_type_code11_check_mode.setKey(Settings.System.CODE11_SEND_CHECK);
                decoder_type_code11_check_mode.setEntryValues(R.array.image_inverse_decoder_values);
                decoder_type_code11_check_mode.setEntries(R.array.code11_verify_check_mode_entries);
                code11.addPreference(decoder_type_code11_check_mode);
            } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                decoder_type_code11_check_mode = new ListPreference(getActivity());
                decoder_type_code11_check_mode.setTitle(R.string.scanner_symbology_checksum_mode);
                decoder_type_code11_check_mode.setKey(Settings.System.CODE11_SEND_CHECK);
                decoder_type_code11_check_mode.setEntryValues(R.array.code11_check_mode_value);
                decoder_type_code11_check_mode.setEntries(R.array.code11_check_mode_entries);
                code11.addPreference(decoder_type_code11_check_mode);
            }

            try {
                id_buffer = new int[]{
                        PropertyID.CODE11_ENABLE,
                        PropertyID.CODE11_LENGTH1,
                        PropertyID.CODE11_LENGTH2,
                        PropertyID.CODE11_ENABLE_CHECK,
                        PropertyID.CODE11_SEND_CHECK,
                };
                propertyName_buffer = new String[]{
                        Settings.System.CODE11_ENABLE,
                        Settings.System.CODE11_LENGTH1,
                        Settings.System.CODE11_LENGTH2,
                        Settings.System.CODE11_ENABLE_CHECK,
                        Settings.System.CODE11_SEND_CHECK,
                };
                value_buffer = new int[id_buffer.length];
                length_id_buffer = new int[]{
                        PropertyID.CODE11_LENGTH1,
                        PropertyID.CODE11_LENGTH2,
                };
                length_value_buffer = new int[length_id_buffer.length];
                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                if (decoder_type_code11 != null) {
                    decoder_type_code11.setChecked(value_buffer[i++] == 1);
                } else {
                    i++;
                }
                if (decoder_L1_of_code11 != null) {
                    decoder_L1_of_code11.setSummary(String.valueOf(value_buffer[i++]));
                } else {
                    i++;
                }
                if (decoder_L2_of_code11 != null) {
                    decoder_L2_of_code11.setSummary(String.valueOf(value_buffer[i++]));
                } else {
                    i++;
                }
                if (decoder_type_code11_enable_check != null) {
                    decoder_type_code11_enable_check.setChecked(value_buffer[i++] == 1);
                } else {
                    i++;
                }
                if (decoder_type_code11_check_mode != null) {
                    decoder_type_code11_check_mode.setOnPreferenceChangeListener(this);
                    decoder_type_code11_check_mode.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_code11_check_mode.setSummary(decoder_type_code11_check_mode.getEntry());
                }
            } catch (Exception e) {

            }
        } else if (Settings.System.CODE128_ENABLE.equals(symbologyType)) {
            code_128 = new PreferenceCategory(getActivity());
            code_128.setKey(getString(R.string.scanner_symbology_code128));
            code_128.setTitle(R.string.scanner_symbology_code128);
            root.addPreference(code_128);
            decoder_type_code128 = new SwitchPreference(getActivity());
            decoder_type_code128.setKey(Settings.System.CODE128_ENABLE);
            decoder_type_code128.setTitle(R.string.scanner_symbology_enable);
            code_128.addPreference(decoder_type_code128);
            decoder_L1_of_code128 = new EditTextPreference(getActivity());
            initEditText(decoder_L1_of_code128, true);
            decoder_L1_of_code128.setKey(Settings.System.CODE128_LENGTH1);
            decoder_L1_of_code128.setTitle(R.string.scanner_symbology_user_l1);
            decoder_L2_of_code128 = new EditTextPreference(getActivity());
            decoder_L2_of_code128.setKey(Settings.System.CODE128_LENGTH2);
            decoder_L2_of_code128.setTitle(R.string.scanner_symbology_user_l2);
            initEditText(decoder_L2_of_code128, true);
            code_128.addPreference(decoder_L1_of_code128);
            code_128.addPreference(decoder_L2_of_code128);
            decoder_gs1128 = new SwitchPreference(getActivity());
            decoder_gs1128.setKey(Settings.System.CODE128_GS1_ENABLE);
            decoder_gs1128.setTitle(R.string.scanner_symbology_gs1_128);
            code_128.addPreference(decoder_gs1128);
            decoder_code_isbt_128 = new SwitchPreference(getActivity());
            decoder_code_isbt_128.setKey(Settings.System.CODE_ISBT_128);
            decoder_code_isbt_128.setTitle(R.string.scanner_symbology_isbt_128);
            code_128.addPreference(decoder_code_isbt_128);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                //Enable or disable decoding Code 128 bar codes with reduced quiet zones. If you select Enable, select a 1D Quiet Zone Level
                decoder_check_isbt_table = new SwitchPreference(getActivity());
                decoder_check_isbt_table.setKey(Settings.System.CODE128_CHECK_ISBT_TABLE);
                decoder_check_isbt_table.setTitle(R.string.scanner_symbology_isbt_check_table);
                code_128.addPreference(decoder_check_isbt_table);
                decoder_isbt_concatenation_mode = new ListPreference(getActivity());
                decoder_isbt_concatenation_mode.setKey(Settings.System.CODE_ISBT_Concatenation_MODE);
                decoder_isbt_concatenation_mode.setTitle(R.string.scanner_symbology_isbt_concatenation_mode);
                decoder_isbt_concatenation_mode.setEntries(R.array.scanner_code128_concatenation_mode_entries);
                decoder_isbt_concatenation_mode.setEntryValues(R.array.image_inverse_decoder_values);
                decoder_isbt_concatenation_mode.setOnPreferenceChangeListener(this);
                //code_128.addPreference(decoder_isbt_concatenation_mode);
            }
            decoder_type_code128_quiet_zone = new ListPreference(getActivity());
            decoder_type_code128_quiet_zone.setKey(Settings.System.CODE128_REDUCED_QUIET_ZONE);
            decoder_type_code128_quiet_zone.setTitle(R.string.scanner_symbology_reduced_quiet_zones);
            decoder_type_code128_quiet_zone.setEntries(R.array.scanner_code128_quiet_zone_entries);
            decoder_type_code128_quiet_zone.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_type_code128_quiet_zone.setOnPreferenceChangeListener(this);
            code_128.addPreference(decoder_type_code128_quiet_zone);
            decoder_type_code128_security_level = new ListPreference(getActivity());
            decoder_type_code128_security_level.setKey(Settings.System.CODE128_SECURITY_LEVEL);
            decoder_type_code128_security_level.setTitle(R.string.scanner_symbology_security_level);
            decoder_type_code128_security_level.setEntries(R.array.linear_1d_quiet_zone_level);
            decoder_type_code128_security_level.setEntryValues(R.array.linear_security_level_values);
            decoder_type_code128_security_level.setOnPreferenceChangeListener(this);
            code_128.addPreference(decoder_type_code128_security_level);

            id_buffer = new int[]{
                    PropertyID.CODE128_ENABLE,
                    PropertyID.CODE128_LENGTH1,
                    PropertyID.CODE128_LENGTH2,
                    PropertyID.CODE128_GS1_ENABLE,
                    PropertyID.CODE_ISBT_128,
                    PropertyID.CODE128_CHECK_ISBT_TABLE,
                    PropertyID.CODE_ISBT_Concatenation_MODE,
                    PropertyID.CODE128_REDUCED_QUIET_ZONE,
                    PropertyID.CODE128_SECURITY_LEVEL

            };
            propertyName_buffer = new String[]{
                    Settings.System.CODE128_ENABLE,
                    Settings.System.CODE128_LENGTH1,
                    Settings.System.CODE128_LENGTH2,
                    Settings.System.CODE128_GS1_ENABLE,
                    Settings.System.CODE_ISBT_128,
                    Settings.System.CODE128_CHECK_ISBT_TABLE,
                    Settings.System.CODE_ISBT_Concatenation_MODE,
                    Settings.System.CODE128_REDUCED_QUIET_ZONE,
                    Settings.System.CODE128_SECURITY_LEVEL
            };
            value_buffer = new int[id_buffer.length];
            length_id_buffer = new int[]{
                    PropertyID.CODE128_LENGTH1,
                    PropertyID.CODE128_LENGTH2,
            };
            length_value_buffer = new int[length_id_buffer.length];
            mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);

            decoder_type_code128.setChecked(value_buffer[i++] == 1);
            decoder_L1_of_code128.setSummary(String.valueOf(value_buffer[i++]));
            decoder_L2_of_code128.setSummary(String.valueOf(value_buffer[i++]));
            if (decoder_gs1128 != null) {
                decoder_gs1128.setChecked(value_buffer[i++] == 1);
            } else {
                i++;
            }
            if (decoder_code_isbt_128 != null) {
                decoder_code_isbt_128.setChecked(value_buffer[i++] == 1);
            } else {
                i++;
            }
            if (decoder_check_isbt_table != null) {
                decoder_check_isbt_table.setChecked(value_buffer[i++] == 1);
            } else {
                i++;
            }
            if (decoder_isbt_concatenation_mode != null) {
                decoder_isbt_concatenation_mode.setValue(String.valueOf(value_buffer[i++]));
                decoder_isbt_concatenation_mode.setSummary(decoder_isbt_concatenation_mode.getEntry());
            } else {
                i++;
            }
            if (decoder_type_code128_quiet_zone != null) {
                decoder_type_code128_quiet_zone.setValue(String.valueOf(value_buffer[i++]));
                decoder_type_code128_quiet_zone.setSummary(decoder_type_code128_quiet_zone.getEntry());
            } else {
                i++;
            }
            if (decoder_type_code128_security_level != null) {
                decoder_type_code128_security_level.setValue(String.valueOf(value_buffer[i++]));
                decoder_type_code128_security_level.setSummary(decoder_type_code128_security_level.getEntry());
            }
        } else if (Settings.System.CODE39_ENABLE.equals(symbologyType)) {
            code_39 = new PreferenceCategory(getActivity());
            code_39.setKey(getString(R.string.scanner_symbology_code39));
            code_39.setTitle(R.string.scanner_symbology_code39);
            root.addPreference(code_39);
            decoder_type_code39 = new SwitchPreference(getActivity());
            decoder_type_code39.setKey(Settings.System.CODE39_ENABLE);
            decoder_type_code39.setTitle(R.string.scanner_symbology_enable);
            code_39.addPreference(decoder_type_code39);
            decoder_L1_of_code39 = new EditTextPreference(getActivity());
            initEditText(decoder_L1_of_code39, true);
            decoder_L1_of_code39.setKey(Settings.System.CODE39_LENGTH1);
            decoder_L1_of_code39.setTitle(R.string.scanner_symbology_user_l1);
            decoder_L2_of_code39 = new EditTextPreference(getActivity());
            decoder_L2_of_code39.setKey(Settings.System.CODE39_LENGTH2);
            decoder_L2_of_code39.setTitle(R.string.scanner_symbology_user_l2);
            initEditText(decoder_L2_of_code39, true);
            code_39.addPreference(decoder_L1_of_code39);
            code_39.addPreference(decoder_L2_of_code39);
            decoder_type_code39_enable_check = new SwitchPreference(getActivity());
            decoder_type_code39_enable_check.setKey(Settings.System.CODE39_ENABLE_CHECK);
            decoder_type_code39_enable_check.setTitle(R.string.scanner_symbology_enable_checksum);
            code_39.addPreference(decoder_type_code39_enable_check);
            decoder_type_code39_send_check = new SwitchPreference(getActivity());
            decoder_type_code39_send_check.setKey(Settings.System.CODE39_SEND_CHECK);
            decoder_type_code39_send_check.setTitle(R.string.scanner_symbology_send_checksum);
            code_39.addPreference(decoder_type_code39_send_check);
            decoder_type_code39_full_ascii = new SwitchPreference(getActivity());
            decoder_type_code39_full_ascii.setKey(Settings.System.CODE39_FULL_ASCII);
            decoder_type_code39_full_ascii.setTitle(R.string.scanner_symbology_full_ascii_conversion);
            code_39.addPreference(decoder_type_code39_full_ascii);

            decoder_type_code32 = new SwitchPreference(getActivity());
            decoder_type_code32.setKey(Settings.System.CODE32_ENABLE);
            decoder_type_code32.setTitle(R.string.scanner_symbology_code32);
            code_39.addPreference(decoder_type_code32);
            if (ScannerAdapter.isSupportPreference(scannerType, Settings.System.CODE32_SEND_START)) {
                //Convert Code 39 to Code 32 must be enabled for this parameter to function.
                decoder_type_code32_send_start = new SwitchPreference(getActivity());
                decoder_type_code32_send_start.setKey(Settings.System.CODE32_SEND_START);
                decoder_type_code32_send_start.setTitle(R.string.scanner_symbology_code32_prefix);
                code_39.addPreference(decoder_type_code32_send_start);
            }
            decoder_type_code39_quiet_zone = new SwitchPreference(getActivity());
            decoder_type_code39_quiet_zone.setKey(Settings.System.CODE39_Quiet_Zone);
            decoder_type_code39_quiet_zone.setTitle(R.string.scanner_symbology_reduced_quiet_zones);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                decoder_type_code39_start_stop = new SwitchPreference(getActivity());
                decoder_type_code39_start_stop.setKey(Settings.System.CODE32_SEND_START);
                decoder_type_code39_start_stop.setTitle(R.string.scanner_symbology_send_start_stop);
                code_39.addPreference(decoder_type_code39_start_stop);
            }
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                code_39.addPreference(decoder_type_code39_quiet_zone);
                decoder_type_code39_security_level = new ListPreference(getActivity());
                decoder_type_code39_security_level.setKey(Settings.System.CODE39_SECURITY_LEVEL);
                decoder_type_code39_security_level.setTitle(R.string.scanner_symbology_security_level);
                decoder_type_code39_security_level.setEntries(R.array.linear_1d_quiet_zone_level);
                decoder_type_code39_security_level.setEntryValues(R.array.linear_security_level_values);
                decoder_type_code39_security_level.setOnPreferenceChangeListener(this);
                code_39.addPreference(decoder_type_code39_security_level);
            }
            try {
                id_buffer = new int[]{
                        PropertyID.CODE39_ENABLE,
                        PropertyID.CODE39_LENGTH1,
                        PropertyID.CODE39_LENGTH2,
                        PropertyID.CODE39_ENABLE_CHECK,
                        PropertyID.CODE39_SEND_CHECK,
                        PropertyID.CODE39_FULL_ASCII,
                        PropertyID.CODE32_ENABLE,
                        PropertyID.CODE32_SEND_START,
                        PropertyID.CODE39_Quiet_Zone,
                        PropertyID.CODE39_START_STOP,
                        PropertyID.CODE39_SECURITY_LEVEL
                };
                propertyName_buffer = new String[]{
                        Settings.System.CODE39_ENABLE,
                        Settings.System.CODE39_LENGTH1,
                        Settings.System.CODE39_LENGTH2,
                        Settings.System.CODE39_ENABLE_CHECK,
                        Settings.System.CODE39_SEND_CHECK,
                        Settings.System.CODE39_FULL_ASCII,
                        Settings.System.CODE32_ENABLE,
                        Settings.System.CODE32_SEND_START,
                        Settings.System.CODE39_Quiet_Zone,
                        Settings.System.CODE39_START_STOP,
                        Settings.System.CODE39_SECURITY_LEVEL
                };
                value_buffer = new int[id_buffer.length];
                length_id_buffer = new int[]{
                        PropertyID.CODE39_LENGTH1,
                        PropertyID.CODE39_LENGTH2,
                };
                length_value_buffer = new int[length_id_buffer.length];
                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                decoder_type_code39.setChecked(value_buffer[i++] == 1);
                decoder_L1_of_code39.setSummary(String.valueOf(value_buffer[i++]));
                decoder_L2_of_code39.setSummary(String.valueOf(value_buffer[i++]));
                decoder_type_code39_enable_check.setChecked(value_buffer[i++] == 1);
                decoder_type_code39_send_check.setChecked(value_buffer[i++] == 1);
                decoder_type_code39_full_ascii.setChecked(value_buffer[i++] == 1);
                decoder_type_code32.setChecked(value_buffer[i++] == 1);
                if (decoder_type_code32_send_start != null) {
                    decoder_type_code32_send_start.setChecked(value_buffer[i++] == 1);
                } else {
                    i++;
                }
                if (decoder_type_code39_quiet_zone != null) {
                    decoder_type_code39_quiet_zone.setChecked(value_buffer[i++] == 1);
                } else {
                    i++;
                }
                if (decoder_type_code39_start_stop != null) {
                    decoder_type_code39_start_stop.setChecked(value_buffer[i++] == 1);
                } else {
                    i++;
                }

                if (decoder_type_code39_security_level != null) {
                    decoder_type_code39_security_level.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_code39_security_level.setSummary(decoder_type_code39_security_level.getEntry());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.CODE93_ENABLE.equals(symbologyType)) {
            code_93 = new PreferenceCategory(getActivity());
            code_93.setKey(getString(R.string.scanner_symbology_code93));
            code_93.setTitle(R.string.scanner_symbology_code93);
            root.addPreference(code_93);
            decoder_type_code93 = new SwitchPreference(getActivity());
            decoder_type_code93.setKey(Settings.System.CODE93_ENABLE);
            decoder_type_code93.setTitle(R.string.scanner_symbology_enable);
            code_93.addPreference(decoder_type_code93);
            decoder_L1_of_code93 = new EditTextPreference(getActivity());
            initEditText(decoder_L1_of_code93, true);
            decoder_L1_of_code93.setKey(Settings.System.CODE93_LENGTH1);
            decoder_L1_of_code93.setTitle(R.string.scanner_symbology_user_l1);
            decoder_L2_of_code93 = new EditTextPreference(getActivity());
            decoder_L2_of_code93.setKey(Settings.System.CODE93_LENGTH2);
            decoder_L2_of_code93.setTitle(R.string.scanner_symbology_user_l2);
            initEditText(decoder_L2_of_code93, true);
            code_93.addPreference(decoder_L1_of_code93);
            code_93.addPreference(decoder_L2_of_code93);
            try {
                id_buffer = new int[]{
                        PropertyID.CODE93_ENABLE,
                        PropertyID.CODE93_LENGTH1,
                        PropertyID.CODE93_LENGTH2,
                };
                propertyName_buffer = new String[]{
                        Settings.System.CODE93_ENABLE,
                        Settings.System.CODE93_LENGTH1,
                        Settings.System.CODE93_LENGTH2,
                };
                value_buffer = new int[id_buffer.length];
                length_id_buffer = new int[]{
                        PropertyID.CODE93_LENGTH1,
                        PropertyID.CODE93_LENGTH2,
                };
                length_value_buffer = new int[length_id_buffer.length];
                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                decoder_type_code93.setChecked(value_buffer[i++] == 1);
                decoder_L1_of_code93.setSummary(String.valueOf(value_buffer[i++]));
                decoder_L2_of_code93.setSummary(String.valueOf(value_buffer[i++]));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.COMPOSITE_CC_AB_ENABLE.equals(symbologyType) || Settings.System.COMPOSITE_CC_C_ENABLE.equals(symbologyType)
                || Settings.System.COMPOSITE_TLC39_ENABLE.equals(symbologyType)) {
            composite = new PreferenceCategory(getActivity());
            composite.setKey(getString(R.string.scanner_symbology_composite));
            composite.setTitle(R.string.scanner_symbology_composite);
            root.addPreference(composite);
            decoder_type_composite_cc_ab = new SwitchPreference(getActivity());
            decoder_type_composite_cc_ab.setKey(Settings.System.COMPOSITE_CC_AB_ENABLE);
            decoder_type_composite_cc_ab.setTitle(R.string.scanner_symbology_composite_cc_ab);
            composite.addPreference(decoder_type_composite_cc_ab);
            decoder_type_composite_cc_c = new SwitchPreference(getActivity());
            decoder_type_composite_cc_c.setKey(Settings.System.COMPOSITE_CC_C_ENABLE);
            decoder_type_composite_cc_c.setTitle(R.string.scanner_symbology_composite_cc_c);
            composite.addPreference(decoder_type_composite_cc_c);
            decoder_type_composite_39 = new SwitchPreference(getActivity());
            decoder_type_composite_39.setKey(Settings.System.COMPOSITE_TLC39_ENABLE);
            decoder_type_composite_39.setTitle(R.string.scanner_symbology_composite_39);
            composite.addPreference(decoder_type_composite_39);
            decoder_type_composite_upc_mode = new ListPreference(getActivity());
            decoder_type_composite_upc_mode.setKey(Settings.System.COMPOSITE_UPC_MODE);
            decoder_type_composite_upc_mode.setTitle(R.string.scanner_symbology_composite_upc_mode);
            decoder_type_composite_upc_mode.setEntries(R.array.ucc_link_mode_entries);
            decoder_type_composite_upc_mode.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_type_composite_upc_mode.setOnPreferenceChangeListener(this);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                composite.addPreference(decoder_type_composite_upc_mode);
            }
            if (decoder_type_composite_cc_ab != null) {
                int value = mApplication.getPropertyInt(profileId, Settings.System.COMPOSITE_CC_AB_ENABLE, 0);
                decoder_type_composite_cc_ab.setChecked(value == 1);
            }
            if (decoder_type_composite_cc_c != null) {
                int value = mApplication.getPropertyInt(profileId, Settings.System.COMPOSITE_CC_C_ENABLE, 0);
                decoder_type_composite_cc_c.setChecked(value == 1);
            }
            if (decoder_type_composite_39 != null) {
                int value = mApplication.getPropertyInt(profileId, Settings.System.COMPOSITE_TLC39_ENABLE, 0);
                decoder_type_composite_39.setChecked(value == 1);
            }
            if (decoder_type_composite_upc_mode != null) {
                int value = mApplication.getPropertyInt(profileId, Settings.System.COMPOSITE_UPC_MODE, 0);
                decoder_type_composite_upc_mode.setValue(String.valueOf(value));
                decoder_type_composite_upc_mode.setSummary(decoder_type_composite_upc_mode.getEntry());
            }
        } else if (Settings.System.D25_ENABLE.equals(symbologyType)) {
            Discrete_2_of_5 = new PreferenceCategory(getActivity());
            Discrete_2_of_5.setKey(getString(R.string.scanner_symbology_discrete_25));
            Discrete_2_of_5.setTitle(R.string.scanner_symbology_discrete_25);
            root.addPreference(Discrete_2_of_5);
            decoder_type_Discrete_2_of_5 = new SwitchPreference(getActivity());
            decoder_type_Discrete_2_of_5.setKey(Settings.System.D25_ENABLE);
            decoder_type_Discrete_2_of_5.setTitle(R.string.scanner_symbology_enable);
            Discrete_2_of_5.addPreference(decoder_type_Discrete_2_of_5);
            decoder_L1_Discrete_2_of_5 = new EditTextPreference(getActivity());
            initEditText(decoder_L1_Discrete_2_of_5, true);
            decoder_L1_Discrete_2_of_5.setKey(Settings.System.D25_LENGTH1);
            decoder_L1_Discrete_2_of_5.setTitle(R.string.scanner_symbology_user_l1);
            decoder_L2_Discrete_2_of_5 = new EditTextPreference(getActivity());
            decoder_L2_Discrete_2_of_5.setKey(Settings.System.D25_LENGTH2);
            decoder_L2_Discrete_2_of_5.setTitle(R.string.scanner_symbology_user_l2);
            initEditText(decoder_L2_Discrete_2_of_5, true);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                Discrete_2_of_5.addPreference(decoder_L1_Discrete_2_of_5);
                Discrete_2_of_5.addPreference(decoder_L2_Discrete_2_of_5);
            }
            try {
                id_buffer = new int[]{
                        PropertyID.D25_ENABLE,
                        PropertyID.D25_LENGTH1,
                        PropertyID.D25_LENGTH2,
                };
                propertyName_buffer = new String[]{
                        Settings.System.D25_ENABLE,
                        Settings.System.D25_LENGTH1,
                        Settings.System.D25_LENGTH2,
                };
                value_buffer = new int[id_buffer.length];
                length_id_buffer = new int[]{
                        PropertyID.D25_LENGTH1,
                        PropertyID.D25_LENGTH2,
                };
                length_value_buffer = new int[length_id_buffer.length];
                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                decoder_type_Discrete_2_of_5.setChecked(value_buffer[i++] == 1);
                if (decoder_L1_Discrete_2_of_5 != null) {
                    decoder_L1_Discrete_2_of_5.setSummary(String.valueOf(value_buffer[i++]));
                }
                if (decoder_L2_Discrete_2_of_5 != null) {
                    decoder_L2_Discrete_2_of_5.setSummary(String.valueOf(value_buffer[i++]));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.DATAMATRIX_ENABLE.equals(symbologyType)) {
            datamatrix = new PreferenceCategory(getActivity());
            datamatrix.setKey(getString(R.string.scanner_symbology_Datamatrix));
            datamatrix.setTitle(R.string.scanner_symbology_Datamatrix);
            root.addPreference(datamatrix);
            decoder_type_datamatrix = new SwitchPreference(getActivity());
            decoder_type_datamatrix.setKey(Settings.System.DATAMATRIX_ENABLE);
            decoder_type_datamatrix.setTitle(R.string.scanner_symbology_enable);
            datamatrix.addPreference(decoder_type_datamatrix);
            decoder_type_datamatrix_inverse = new ListPreference(getActivity());
            decoder_type_datamatrix_inverse.setKey(Settings.System.DATAMATRIX_INVERSE);
            decoder_type_datamatrix_inverse.setTitle(R.string.scanner_inverse);
            decoder_type_datamatrix_inverse.setEntries(R.array.image_2d_inverse_decoder_entries);
            decoder_type_datamatrix_inverse.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_type_datamatrix_inverse.setOnPreferenceChangeListener(this);
            decoder_type_dm_size = new ListPreference(getActivity());
            decoder_type_dm_size.setKey(Settings.System.DATAMATRIX_SYMBOL_SIZE);
            decoder_type_dm_size.setTitle(R.string.scanner_symbols_size);
            decoder_type_dm_size.setEntryValues(R.array.code_id_type_values);
            decoder_type_dm_size.setEntries(R.array.scanner_symbols_size_entries);
            decoder_type_dm_size.setOnPreferenceChangeListener(this);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                datamatrix.addPreference(decoder_type_datamatrix_inverse);
            }
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                datamatrix.addPreference(decoder_type_dm_size);
            }
            try {
                id_buffer = new int[]{
                        PropertyID.DATAMATRIX_ENABLE,
                        PropertyID.DATAMATRIX_INVERSE,
                        PropertyID.DATAMATRIX_SYMBOL_SIZE,
                };
                propertyName_buffer = new String[]{
                        Settings.System.DATAMATRIX_ENABLE,
                        Settings.System.DATAMATRIX_INVERSE,
                        Settings.System.DATAMATRIX_SYMBOL_SIZE,
                };
                value_buffer = new int[id_buffer.length];
                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                decoder_type_datamatrix.setChecked(value_buffer[i++] == 1);
                decoder_type_datamatrix_inverse.setValue(String.valueOf(value_buffer[i++]));
                decoder_type_datamatrix_inverse.setSummary(decoder_type_datamatrix_inverse.getEntry());
                int val = value_buffer[i++];
                val = val >= 0 ? val : 0;
                decoder_type_dm_size.setValue(String.valueOf(val));
                decoder_type_dm_size.setSummary(decoder_type_dm_size.getEntry());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.DOTCODE_ENABLE.equals(symbologyType)) {
            mDotCode = new PreferenceCategory(getActivity());
            mDotCode.setKey(getString(R.string.scanner_symbology_dotcode));
            mDotCode.setTitle(R.string.scanner_symbology_dotcode);
            root.addPreference(mDotCode);
            decoder_type_dotcode = new SwitchPreference(getActivity());
            decoder_type_dotcode.setKey(Settings.System.DOTCODE_ENABLE);
            decoder_type_dotcode.setTitle(R.string.scanner_symbology_enable);
            mDotCode.addPreference(decoder_type_dotcode);
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.DOTCODE_ENABLE, 0);
                decoder_type_dotcode.setChecked(value == 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.EAN13_ENABLE.equals(symbologyType)) {
            ean13 = new PreferenceCategory(getActivity());
            ean13.setKey(getString(R.string.scanner_symbology_ean13));
            ean13.setTitle(R.string.scanner_symbology_ean13);
            root.addPreference(ean13);
            decoder_type_EAN13 = new SwitchPreference(getActivity());
            decoder_type_EAN13.setKey(Settings.System.EAN13_ENABLE);
            decoder_type_EAN13.setTitle(R.string.scanner_symbology_enable);
            ean13.addPreference(decoder_type_EAN13);
            decoder_type_EAN13_send_check = new SwitchPreference(getActivity());
            decoder_type_EAN13_send_check.setKey(Settings.System.EAN13_SEND_CHECK);
            decoder_type_EAN13_send_check.setTitle(R.string.scanner_symbology_send_checksum);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                ean13.addPreference(decoder_type_EAN13_send_check);
            }
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.EAN13_ENABLE, 0);
                decoder_type_EAN13.setChecked(value == 1);
                value = mApplication.getPropertyInt(profileId, Settings.System.EAN13_SEND_CHECK, 0);
                decoder_type_EAN13_send_check.setChecked(value == 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.EAN8_ENABLE.equals(symbologyType)) {
            ean8 = new PreferenceCategory(getActivity());
            ean8.setKey(getString(R.string.scanner_symbology_ean8));
            ean8.setTitle(R.string.scanner_symbology_ean8);
            root.addPreference(ean8);
            decoder_type_EAN8 = new SwitchPreference(getActivity());
            decoder_type_EAN8.setKey(Settings.System.EAN8_ENABLE);
            decoder_type_EAN8.setTitle(R.string.scanner_symbology_enable);
            ean8.addPreference(decoder_type_EAN8);
            decoder_type_EAN8_send_check = new SwitchPreference(getActivity());
            decoder_type_EAN8_send_check.setKey(Settings.System.EAN8_SEND_CHECK);
            decoder_type_EAN8_send_check.setTitle(R.string.scanner_symbology_send_checksum);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                ean8.addPreference(decoder_type_EAN8_send_check);
            }
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.EAN8_ENABLE, 0);
                decoder_type_EAN8.setChecked(value == 1);
                value = mApplication.getPropertyInt(profileId, Settings.System.EAN8_SEND_CHECK, 0);
                decoder_type_EAN8_send_check.setChecked(value == 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.GS1_14_ENABLE.equals(symbologyType)) {
            gs1_databar14 = new PreferenceCategory(getActivity());
            gs1_databar14.setKey(getString(R.string.scanner_symbology_gs1_databar14));
            gs1_databar14.setTitle(R.string.scanner_symbology_gs1_databar14);
            root.addPreference(gs1_databar14);
            decoder_type_gs1_databar14 = new SwitchPreference(getActivity());
            decoder_type_gs1_databar14.setKey(Settings.System.GS1_14_ENABLE);
            decoder_type_gs1_databar14.setTitle(R.string.scanner_symbology_enable);
            gs1_databar14.addPreference(decoder_type_gs1_databar14);
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.GS1_14_ENABLE, 0);
                decoder_type_gs1_databar14.setChecked(value == 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.GS1_EXP_ENABLE.equals(symbologyType)) {
            gs1_expanded = new PreferenceCategory(getActivity());
            gs1_expanded.setKey(getString(R.string.scanner_symbology_gs1_databar_expanded));
            gs1_expanded.setTitle(R.string.scanner_symbology_gs1_databar_expanded);
            root.addPreference(gs1_expanded);
            decoder_type_RSS_Expanded = new SwitchPreference(getActivity());
            decoder_type_RSS_Expanded.setKey(Settings.System.GS1_EXP_ENABLE);
            decoder_type_RSS_Expanded.setTitle(R.string.scanner_symbology_enable);
            gs1_expanded.addPreference(decoder_type_RSS_Expanded);
            decoder_l1_expanded = new EditTextPreference(getActivity());
            initEditText(decoder_l1_expanded, true);
            decoder_l1_expanded.setKey(Settings.System.GS1_EXP_LENGTH1);
            decoder_l1_expanded.setTitle(R.string.scanner_symbology_user_l1);
            decoder_l2_expanded = new EditTextPreference(getActivity());
            decoder_l2_expanded.setKey(Settings.System.GS1_EXP_LENGTH2);
            decoder_l2_expanded.setTitle(R.string.scanner_symbology_user_l2);
            initEditText(decoder_l2_expanded, true);
            gs1_expanded.addPreference(decoder_l1_expanded);
            gs1_expanded.addPreference(decoder_l2_expanded);
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.GS1_EXP_ENABLE, 0);
                decoder_type_RSS_Expanded.setChecked(value == 1);
                value = mApplication.getPropertyInt(profileId, Settings.System.GS1_EXP_LENGTH1, 1);
                decoder_l1_expanded.setSummary(String.valueOf(value));
                value = mApplication.getPropertyInt(profileId, Settings.System.GS1_EXP_LENGTH2, 80);
                decoder_l2_expanded.setSummary(String.valueOf(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.GS1_LIMIT_ENABLE.equals(symbologyType)) {
            gs1_limited = new PreferenceCategory(getActivity());
            gs1_limited.setKey(getString(R.string.scanner_symbology_gs1_databar_limited));
            gs1_limited.setTitle(R.string.scanner_symbology_gs1_databar_limited);
            root.addPreference(gs1_limited);
            decoder_type_gs1_Limited = new SwitchPreference(getActivity());
            decoder_type_gs1_Limited.setKey(Settings.System.GS1_LIMIT_ENABLE);
            decoder_type_gs1_Limited.setTitle(R.string.scanner_symbology_enable);
            gs1_limited.addPreference(decoder_type_gs1_Limited);
            decoder_type_gs1_limit_security_level = new ListPreference(getActivity());
            decoder_type_gs1_limit_security_level.setKey(Settings.System.GS1_LIMIT_Security_Level);
            decoder_type_gs1_limit_security_level.setTitle(R.string.scanner_symbology_security_level);
            decoder_type_gs1_limit_security_level.setEntries(R.array.linear_1d_quiet_zone_level);
            decoder_type_gs1_limit_security_level.setEntryValues(R.array.linear_security_level_values);
            decoder_type_gs1_limit_security_level.setOnPreferenceChangeListener(this);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                gs1_limited.addPreference(decoder_type_gs1_limit_security_level);
            }
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.GS1_EXP_ENABLE, 0);
                decoder_type_gs1_Limited.setChecked(value == 1);
                value = mApplication.getPropertyInt(profileId, Settings.System.GS1_LIMIT_Security_Level, 2);
                decoder_type_gs1_limit_security_level.setValue(String.valueOf(value));
                decoder_type_gs1_limit_security_level.setSummary(decoder_type_gs1_limit_security_level.getEntry());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.HANXIN_ENABLE.equals(symbologyType)) {
            hanxin = new PreferenceCategory(getActivity());
            hanxin.setKey(getString(R.string.scanner_symbology_hanxin));
            hanxin.setTitle(R.string.scanner_symbology_hanxin);
            root.addPreference(hanxin);
            decoder_type_hanxin = new SwitchPreference(getActivity());
            decoder_type_hanxin.setKey(Settings.System.HANXIN_ENABLE);
            decoder_type_hanxin.setTitle(R.string.scanner_symbology_enable);
            hanxin.addPreference(decoder_type_hanxin);
            decoder_type_hanxin_inverse = new ListPreference(getActivity());
            decoder_type_hanxin_inverse.setKey(Settings.System.HANXIN_INVERSE);
            decoder_type_hanxin_inverse.setTitle(R.string.scanner_inverse);
            decoder_type_hanxin_inverse.setEntries(R.array.image_2d_inverse_decoder_entries);
            decoder_type_hanxin_inverse.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_type_hanxin_inverse.setOnPreferenceChangeListener(this);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                hanxin.addPreference(decoder_type_hanxin_inverse);
            }
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.HANXIN_ENABLE, 0);
                decoder_type_hanxin.setChecked(value == 1);
                value = mApplication.getPropertyInt(profileId, Settings.System.HANXIN_INVERSE, 0);
                decoder_type_hanxin_inverse.setValue(String.valueOf(value));
                decoder_type_hanxin_inverse.setSummary(decoder_type_hanxin_inverse.getEntry());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.I25_ENABLE.equals(symbologyType)) {
            Interleaved_2_of_5 = new PreferenceCategory(getActivity());
            Interleaved_2_of_5.setKey(getString(R.string.scanner_symbology_interleaved_25));
            Interleaved_2_of_5.setTitle(R.string.scanner_symbology_interleaved_25);
            root.addPreference(Interleaved_2_of_5);
            decoder_type_Interleaved_2_of_5 = new SwitchPreference(getActivity());
            decoder_type_Interleaved_2_of_5.setKey(Settings.System.I25_ENABLE);
            decoder_type_Interleaved_2_of_5.setTitle(R.string.scanner_symbology_enable);
            Interleaved_2_of_5.addPreference(decoder_type_Interleaved_2_of_5);
            decoder_L1_Interleaved_2_of_5 = new EditTextPreference(getActivity());
            initEditText(decoder_L1_Interleaved_2_of_5, true);
            decoder_L1_Interleaved_2_of_5.setKey(Settings.System.I25_LENGTH1);
            decoder_L1_Interleaved_2_of_5.setTitle(R.string.scanner_symbology_user_l1);
            decoder_L2_Interleaved_2_of_5 = new EditTextPreference(getActivity());
            decoder_L2_Interleaved_2_of_5.setKey(Settings.System.I25_LENGTH2);
            decoder_L2_Interleaved_2_of_5.setTitle(R.string.scanner_symbology_user_l2);
            initEditText(decoder_L2_Interleaved_2_of_5, true);
            Interleaved_2_of_5.addPreference(decoder_L1_Interleaved_2_of_5);
            Interleaved_2_of_5.addPreference(decoder_L2_Interleaved_2_of_5);

            decoder_type_Interleaved_2_of_5_en_check = new SwitchPreference(getActivity());
            decoder_type_Interleaved_2_of_5_en_check.setKey(Settings.System.I25_ENABLE_CHECK);
            decoder_type_Interleaved_2_of_5_en_check.setTitle(R.string.scanner_symbology_enable_checksum);
            Interleaved_2_of_5.addPreference(decoder_type_Interleaved_2_of_5_en_check);

            decoder_type_Interleaved_2_of_5_send_check = new SwitchPreference(getActivity());
            decoder_type_Interleaved_2_of_5_send_check.setKey(Settings.System.I25_SEND_CHECK);
            decoder_type_Interleaved_2_of_5_send_check.setTitle(R.string.scanner_symbology_send_checksum);
            Interleaved_2_of_5.addPreference(decoder_type_Interleaved_2_of_5_send_check);

            decoder_Interleaved_2_of_5_to_ean13 = new SwitchPreference(getActivity());
            decoder_Interleaved_2_of_5_to_ean13.setKey(Settings.System.I25_SEND_CHECK);
            decoder_Interleaved_2_of_5_to_ean13.setTitle(R.string.scanner_symbology_convert_to_ean13);

            decoder_Int25_reduced_quiet_zone = new ListPreference(getActivity());
            decoder_Int25_reduced_quiet_zone.setKey(Settings.System.I25_QUIET_ZONE);
            decoder_Int25_reduced_quiet_zone.setTitle(R.string.scanner_symbology_reduced_quiet_zones);
            decoder_Int25_reduced_quiet_zone.setEntries(R.array.scanner_code128_quiet_zone_entries);
            decoder_Int25_reduced_quiet_zone.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_Int25_reduced_quiet_zone.setOnPreferenceChangeListener(this);

            decoder_Int25_security_level = new ListPreference(getActivity());
            decoder_Int25_security_level.setKey(Settings.System.I25_SECURITY_LEVEL);
            decoder_Int25_security_level.setTitle(R.string.scanner_symbology_security_level);
            decoder_Int25_security_level.setEntries(R.array.linear_1d_quiet_zone_level);
            decoder_Int25_security_level.setEntryValues(R.array.linear_security_level_values);
            decoder_Int25_security_level.setOnPreferenceChangeListener(this);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                Interleaved_2_of_5.addPreference(decoder_Interleaved_2_of_5_to_ean13);
                Interleaved_2_of_5.addPreference(decoder_Int25_reduced_quiet_zone);
                Interleaved_2_of_5.addPreference(decoder_Int25_security_level);
            }
            try {
                id_buffer = new int[]{
                        PropertyID.I25_ENABLE,
                        PropertyID.I25_LENGTH1,
                        PropertyID.I25_LENGTH2,
                        PropertyID.I25_ENABLE_CHECK,
                        PropertyID.I25_SEND_CHECK,
                        PropertyID.I25_TO_EAN13,
                        PropertyID.I25_QUIET_ZONE,
                        PropertyID.I25_SECURITY_LEVEL,
                };
                propertyName_buffer = new String[]{
                        Settings.System.I25_ENABLE,
                        Settings.System.I25_LENGTH1,
                        Settings.System.I25_LENGTH2,
                        Settings.System.I25_ENABLE_CHECK,
                        Settings.System.I25_SEND_CHECK,
                        Settings.System.I25_TO_EAN13,
                        Settings.System.I25_QUIET_ZONE,
                        Settings.System.I25_SECURITY_LEVEL,
                };
                value_buffer = new int[id_buffer.length];
                length_id_buffer = new int[]{
                        PropertyID.I25_LENGTH1,
                        PropertyID.I25_LENGTH2,
                };
                length_value_buffer = new int[length_id_buffer.length];
                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);

                decoder_type_Interleaved_2_of_5.setChecked(value_buffer[i++] == 1);
                decoder_L1_Interleaved_2_of_5.setSummary(String.valueOf(value_buffer[i++]));
                decoder_L2_Interleaved_2_of_5.setSummary(String.valueOf(value_buffer[i++]));
                decoder_type_Interleaved_2_of_5_en_check.setChecked(value_buffer[i++] == 1);
                decoder_type_Interleaved_2_of_5_send_check.setChecked(value_buffer[i++] == 1);
                decoder_Interleaved_2_of_5_to_ean13.setChecked(value_buffer[i++] == 1);
                decoder_Int25_reduced_quiet_zone.setValue(String.valueOf(value_buffer[i++]));
                decoder_Int25_reduced_quiet_zone.setSummary(decoder_Int25_reduced_quiet_zone.getEntry());
                decoder_Int25_security_level.setValue(String.valueOf(value_buffer[i++]));
                decoder_Int25_security_level.setSummary(decoder_Int25_security_level.getEntry());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.M25_ENABLE.equals(symbologyType)) {
            Matrix_2_of_5 = new PreferenceCategory(getActivity());
            Matrix_2_of_5.setKey(getString(R.string.scanner_symbology_matrix_25));
            Matrix_2_of_5.setTitle(R.string.scanner_symbology_matrix_25);
            root.addPreference(Matrix_2_of_5);
            decoder_type_Matrix_2_of_5 = new SwitchPreference(getActivity());
            decoder_type_Matrix_2_of_5.setKey(Settings.System.M25_ENABLE);
            decoder_type_Matrix_2_of_5.setTitle(R.string.scanner_symbology_enable);
            Matrix_2_of_5.addPreference(decoder_type_Matrix_2_of_5);
            decoder_L1_Matrix_2_of_5 = new EditTextPreference(getActivity());
            initEditText(decoder_L1_Matrix_2_of_5, true);
            decoder_L1_Matrix_2_of_5.setKey(Settings.System.M25_LENGTH1);
            decoder_L1_Matrix_2_of_5.setTitle(R.string.scanner_symbology_user_l1);
            decoder_L2_Matrix_2_of_5 = new EditTextPreference(getActivity());
            decoder_L2_Matrix_2_of_5.setKey(Settings.System.M25_LENGTH2);
            decoder_L2_Matrix_2_of_5.setTitle(R.string.scanner_symbology_user_l2);
            initEditText(decoder_L2_Matrix_2_of_5, true);
            Matrix_2_of_5.addPreference(decoder_L1_Matrix_2_of_5);
            Matrix_2_of_5.addPreference(decoder_L2_Matrix_2_of_5);

            decoder_type_Matrix25_enable_check = new SwitchPreference(getActivity());
            decoder_type_Matrix25_enable_check.setKey(Settings.System.M25_ENABLE_CHECK);
            decoder_type_Matrix25_enable_check.setTitle(R.string.scanner_symbology_enable_checksum);

            decoder_type_Matrix25_send_check = new SwitchPreference(getActivity());
            decoder_type_Matrix25_send_check.setKey(Settings.System.M25_SEND_CHECK);
            decoder_type_Matrix25_send_check.setTitle(R.string.scanner_symbology_send_checksum);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                Matrix_2_of_5.addPreference(decoder_type_Matrix25_enable_check);
                Matrix_2_of_5.addPreference(decoder_type_Matrix25_send_check);
            }
            try {
                id_buffer = new int[]{
                        PropertyID.M25_ENABLE,
                        PropertyID.M25_LENGTH1,
                        PropertyID.M25_LENGTH2,
                        PropertyID.M25_ENABLE_CHECK,
                        PropertyID.M25_SEND_CHECK,
                };
                propertyName_buffer = new String[]{
                        Settings.System.M25_ENABLE,
                        Settings.System.M25_LENGTH1,
                        Settings.System.M25_LENGTH2,
                        Settings.System.M25_ENABLE_CHECK,
                        Settings.System.M25_SEND_CHECK,
                };
                length_id_buffer = new int[]{
                        PropertyID.M25_LENGTH1,
                        PropertyID.M25_LENGTH2,
                };
                length_value_buffer = new int[length_id_buffer.length];
                value_buffer = new int[id_buffer.length];
                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                decoder_type_Matrix_2_of_5.setChecked(value_buffer[i++] == 1);
                int length = value_buffer[i++];
                decoder_L1_Matrix_2_of_5.setSummary(String.valueOf(length < 0 ? 2 : length));
                length = value_buffer[i++];
                decoder_L2_Matrix_2_of_5.setSummary(String.valueOf(length < 0 ? 88 : length));
                decoder_type_Matrix25_enable_check.setChecked(value_buffer[i++] == 1);
                decoder_type_Matrix25_send_check.setChecked(value_buffer[i++] == 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.MAXICODE_ENABLE.equals(symbologyType)) {
            maxicode = new PreferenceCategory(getActivity());
            maxicode.setKey(getString(R.string.scanner_symbology_maxicode));
            maxicode.setTitle(R.string.scanner_symbology_maxicode);
            root.addPreference(maxicode);
            decoder_type_maxicode = new SwitchPreference(getActivity());
            decoder_type_maxicode.setKey(Settings.System.MAXICODE_ENABLE);
            decoder_type_maxicode.setTitle(R.string.scanner_symbology_enable);
            maxicode.addPreference(decoder_type_maxicode);
            decoder_type_maxicode_size = new ListPreference(getActivity());
            decoder_type_maxicode_size.setKey(Settings.System.MAXICODE_SYMBOL_SIZE);
            decoder_type_maxicode_size.setTitle(R.string.scanner_symbols_size);
            decoder_type_maxicode_size.setEntryValues(R.array.code_id_type_values);
            decoder_type_maxicode_size.setEntries(R.array.scanner_symbols_size_entries);
            decoder_type_maxicode_size.setOnPreferenceChangeListener(this);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                maxicode.addPreference(decoder_type_maxicode_size);
            }
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.MAXICODE_ENABLE, 0);
                decoder_type_maxicode.setChecked(value == 1);
                value = mApplication.getPropertyInt(profileId, Settings.System.MAXICODE_SYMBOL_SIZE, 0);
                decoder_type_maxicode_size.setValue(String.valueOf(value));
                decoder_type_maxicode_size.setSummary(decoder_type_maxicode_size.getEntry());
            } catch (Exception e) {
            }
        } else if (Settings.System.MICROPDF417_ENABLE.equals(symbologyType)) {
            micropdf417 = new PreferenceCategory(getActivity());
            micropdf417.setKey(getString(R.string.scanner_symbology_micropdf417));
            micropdf417.setTitle(R.string.scanner_symbology_micropdf417);
            root.addPreference(micropdf417);
            decoder_type_micropdf417 = new SwitchPreference(getActivity());
            decoder_type_micropdf417.setKey(Settings.System.MICROPDF417_ENABLE);
            decoder_type_micropdf417.setTitle(R.string.scanner_symbology_enable);
            micropdf417.addPreference(decoder_type_micropdf417);
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.MICROPDF417_ENABLE, 0);
                decoder_type_micropdf417.setChecked(value == 1);
            } catch (Exception e) {
            }
        } else if (Settings.System.MICROQRCODE_ENABLE.equals(symbologyType)) {
            microQrcode = new PreferenceCategory(getActivity());
            microQrcode.setKey(getString(R.string.scanner_symbology_micro_qrcode));
            microQrcode.setTitle(R.string.scanner_symbology_micro_qrcode);
            root.addPreference(microQrcode);
            decoder_type_microqrcode = new SwitchPreference(getActivity());
            decoder_type_microqrcode.setKey(Settings.System.MICROQRCODE_ENABLE);
            decoder_type_microqrcode.setTitle(R.string.scanner_symbology_enable);
            microQrcode.addPreference(decoder_type_microqrcode);
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.MICROQRCODE_ENABLE, 0);
                decoder_type_microqrcode.setChecked(value == 1);
            } catch (Exception e) {
            }
        } else if (Settings.System.MSI_ENABLE.equals(symbologyType)) {
            MSI = new PreferenceCategory(getActivity());
            MSI.setKey(getString(R.string.scanner_symbology_MSI));
            MSI.setTitle(R.string.scanner_symbology_MSI);
            root.addPreference(MSI);
            decoder_type_MSI = new SwitchPreference(getActivity());
            decoder_type_MSI.setKey(Settings.System.MSI_ENABLE);
            decoder_type_MSI.setTitle(R.string.scanner_symbology_enable);
            MSI.addPreference(decoder_type_MSI);
            decoder_L1_of_MSI = new EditTextPreference(getActivity());
            initEditText(decoder_L1_of_MSI, true);
            decoder_L1_of_MSI.setKey(Settings.System.MSI_LENGTH1);
            decoder_L1_of_MSI.setTitle(R.string.scanner_symbology_user_l1);
            decoder_L2_of_MSI = new EditTextPreference(getActivity());
            decoder_L2_of_MSI.setKey(Settings.System.MSI_LENGTH2);
            decoder_L2_of_MSI.setTitle(R.string.scanner_symbology_user_l2);
            initEditText(decoder_L2_of_MSI, true);
            MSI.addPreference(decoder_L1_of_MSI);
            MSI.addPreference(decoder_L2_of_MSI);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                decoder_type_MSI_2_check = new ListPreference(getActivity());
                decoder_type_MSI_2_check.setKey(Settings.System.MSI_REQUIRE_2_CHECK);
                decoder_type_MSI_2_check.setTitle(R.string.scanner_symbology_enable_checksum);
                decoder_type_MSI_2_check.setEntries(R.array.scanner_msi_verify_check_mode_entries);
                decoder_type_MSI_2_check.setEntryValues(R.array.scanner_msi_check_mod_value);
                decoder_type_MSI_2_check.setOnPreferenceChangeListener(this);
            } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                decoder_type_MSI_2_check = new ListPreference(getActivity());
                decoder_type_MSI_2_check.setKey(Settings.System.MSI_REQUIRE_2_CHECK);
                decoder_type_MSI_2_check.setTitle(R.string.scanner_symbology_checksum_mode);
                decoder_type_MSI_2_check.setEntries(R.array.scanner_honeywell_msi_check_entries);
                decoder_type_MSI_2_check.setEntryValues(R.array.scanner_honeywell_msi_check_value);
                decoder_type_MSI_2_check.setOnPreferenceChangeListener(this);
            }
            decoder_type_MSI_send_check = new SwitchPreference(getActivity());
            decoder_type_MSI_send_check.setKey(Settings.System.MSI_SEND_CHECK);
            decoder_type_MSI_send_check.setTitle(R.string.scanner_symbology_send_checksum);
            decoder_type_MSI_2_mod_11 = new ListPreference(getActivity());
            decoder_type_MSI_2_mod_11.setKey(Settings.System.MSI_CHECK_2_MOD_11);
            decoder_type_MSI_2_mod_11.setTitle(R.string.scanner_symbology_checksum_mode);
            decoder_type_MSI_2_mod_11.setEntries(R.array.scanner_msi_check_entries);
            decoder_type_MSI_2_mod_11.setEntryValues(R.array.scanner_msi_check_mod_value);
            decoder_type_MSI_2_mod_11.setOnPreferenceChangeListener(this);
            MSI.addPreference(decoder_type_MSI_2_check);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                MSI.addPreference(decoder_type_MSI_2_mod_11);
                MSI.addPreference(decoder_type_MSI_send_check);
            }

            try {
                id_buffer = new int[]{
                        PropertyID.MSI_ENABLE,
                        PropertyID.MSI_LENGTH1,
                        PropertyID.MSI_LENGTH2,
                        PropertyID.MSI_REQUIRE_2_CHECK,
                        PropertyID.MSI_SEND_CHECK,
                        PropertyID.MSI_CHECK_2_MOD_11,
                };
                propertyName_buffer = new String[]{
                        Settings.System.MSI_ENABLE,
                        Settings.System.MSI_LENGTH1,
                        Settings.System.MSI_LENGTH2,
                        Settings.System.MSI_REQUIRE_2_CHECK,
                        Settings.System.MSI_SEND_CHECK,
                        Settings.System.MSI_CHECK_2_MOD_11,
                };
                value_buffer = new int[id_buffer.length];
                length_id_buffer = new int[]{
                        PropertyID.MSI_LENGTH1,
                        PropertyID.MSI_LENGTH2,
                };
                length_value_buffer = new int[length_id_buffer.length];
                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);

                decoder_type_MSI.setChecked(value_buffer[i++] == 1);
                int length = value_buffer[i++];
                decoder_L1_of_MSI.setSummary(String.valueOf(length < 0 ? 4 : length));
                length = value_buffer[i++];
                decoder_L2_of_MSI.setSummary(String.valueOf(length < 0 ? 55 : length));

                decoder_type_MSI_2_check.setValue(String.valueOf(value_buffer[i++]));
                decoder_type_MSI_2_check.setSummary(decoder_type_MSI_2_check.getEntry());
                decoder_type_MSI_2_mod_11.setValue(String.valueOf(value_buffer[i++]));
                decoder_type_MSI_2_mod_11.setSummary(decoder_type_MSI_2_mod_11.getEntry());
            } catch (Exception e) {
            }
        } else if (Settings.System.PDF417_ENABLE.equals(symbologyType)) {
            pdf417 = new PreferenceCategory(getActivity());
            pdf417.setKey(getString(R.string.scanner_symbology_pdf417));
            pdf417.setTitle(R.string.scanner_symbology_pdf417);
            root.addPreference(pdf417);
            decoder_type_pdf417 = new SwitchPreference(getActivity());
            decoder_type_pdf417.setKey(Settings.System.PDF417_ENABLE);
            decoder_type_pdf417.setTitle(R.string.scanner_symbology_enable);
            pdf417.addPreference(decoder_type_pdf417);
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.PDF417_ENABLE, 0);
                decoder_type_pdf417.setChecked(value == 1);
            } catch (Exception e) {
            }
        } else if (Settings.System.QRCODE_ENABLE.equals(symbologyType)) {
            qrcode = new PreferenceCategory(getActivity());
            qrcode.setKey(getString(R.string.scanner_symbology_qrcode));
            qrcode.setTitle(R.string.scanner_symbology_qrcode);
            root.addPreference(qrcode);
            decoder_type_qrcode = new SwitchPreference(getActivity());
            decoder_type_qrcode.setKey(Settings.System.QRCODE_ENABLE);
            decoder_type_qrcode.setTitle(R.string.scanner_symbology_enable);
            qrcode.addPreference(decoder_type_qrcode);
            decoder_type_qrcode_inverse = new ListPreference(getActivity());
            decoder_type_qrcode_inverse.setKey(Settings.System.QRCODE_INVERSE);
            decoder_type_qrcode_inverse.setTitle(R.string.scanner_inverse);
            decoder_type_qrcode_inverse.setEntries(R.array.image_2d_inverse_decoder_entries);
            decoder_type_qrcode_inverse.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_type_qrcode_inverse.setOnPreferenceChangeListener(this);
            decoder_type_qrcode_size = new ListPreference(getActivity());
            decoder_type_qrcode_size.setKey(Settings.System.QRCODE_SYMBOL_SIZE);
            decoder_type_qrcode_size.setTitle(R.string.scanner_symbols_size);
            decoder_type_qrcode_size.setEntryValues(R.array.code_id_type_values);
            decoder_type_qrcode_size.setEntries(R.array.scanner_symbols_size_entries);
            decoder_type_qrcode_size.setOnPreferenceChangeListener(this);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                qrcode.addPreference(decoder_type_qrcode_inverse);
            }
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                qrcode.addPreference(decoder_type_qrcode_size);
            }
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.QRCODE_ENABLE, 0);
                decoder_type_qrcode.setChecked(value == 1);
                value = mApplication.getPropertyInt(profileId, Settings.System.QRCODE_INVERSE, 0);
                decoder_type_qrcode_inverse.setValue(String.valueOf(value));
                decoder_type_qrcode_inverse.setSummary(decoder_type_qrcode_inverse.getEntry());
                value = mApplication.getPropertyInt(profileId, Settings.System.QRCODE_SYMBOL_SIZE, 0);
                decoder_type_qrcode_size.setValue(String.valueOf(value));
                decoder_type_qrcode_size.setSummary(decoder_type_qrcode_size.getEntry());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.TRIOPTIC_ENABLE.equals(symbologyType)) {
            trioptic = new PreferenceCategory(getActivity());
            trioptic.setKey(getString(R.string.scanner_symbology_trioptic));
            trioptic.setTitle(R.string.scanner_symbology_trioptic);
            root.addPreference(trioptic);
            decoder_type_trioptic = new SwitchPreference(getActivity());
            decoder_type_trioptic.setKey(Settings.System.TRIOPTIC_ENABLE);
            decoder_type_trioptic.setTitle(R.string.scanner_symbology_enable);
            trioptic.addPreference(decoder_type_trioptic);
            try {
                int value = mApplication.getPropertyInt(profileId, Settings.System.TRIOPTIC_ENABLE, 0);
                decoder_type_trioptic.setChecked(value == 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.UPCA_ENABLE.equals(symbologyType)) {
            upca = new PreferenceCategory(getActivity());
            upca.setKey(getString(R.string.scanner_symbology_upc_a));
            upca.setTitle(R.string.scanner_symbology_upc_a);
            root.addPreference(upca);
            decoder_type_UPC_A = new SwitchPreference(getActivity());
            decoder_type_UPC_A.setKey(Settings.System.UPCA_ENABLE);
            decoder_type_UPC_A.setTitle(R.string.scanner_symbology_enable);
            upca.addPreference(decoder_type_UPC_A);
            decoder_type_UPC_A_to_ean13 = new SwitchPreference(getActivity());
            decoder_type_UPC_A_to_ean13.setKey(Settings.System.UPCA_TO_EAN13);
            decoder_type_UPC_A_to_ean13.setTitle(R.string.scanner_symbology_convert_to_ean13);
            decoder_type_UPC_A_send_check = new SwitchPreference(getActivity());
            decoder_type_UPC_A_send_check.setKey(Settings.System.UPCA_SEND_CHECK);
            decoder_type_UPC_A_send_check.setTitle(R.string.scanner_symbology_send_checksum);
            decoder_type_UPC_A_send_sys = new SwitchPreference(getActivity());
            decoder_type_UPC_A_send_sys.setKey(Settings.System.UPCA_SEND_SYS);
            decoder_type_UPC_A_send_sys.setTitle(R.string.scanner_symbology_send_system_digit);

            decoder_type_UPC_A_preamble_sys = new ListPreference(getActivity());
            decoder_type_UPC_A_preamble_sys.setKey(Settings.System.UPCA_SEND_SYS);
            decoder_type_UPC_A_preamble_sys.setTitle(R.string.scanner_symbology_send_system_digit);
            decoder_type_UPC_A_preamble_sys.setEntries(R.array.scanner_zebra_Preamble_characters_entries);
            decoder_type_UPC_A_preamble_sys.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_type_UPC_A_preamble_sys.setOnPreferenceChangeListener(this);

            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                upca.addPreference(decoder_type_UPC_A_send_sys);
                upca.addPreference(decoder_type_UPC_A_send_check);
                upca.addPreference(decoder_type_UPC_A_to_ean13);

            } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                upca.addPreference(decoder_type_UPC_A_preamble_sys);
                upca.addPreference(decoder_type_UPC_A_send_check);
            }

            try {
                id_buffer = new int[]{
                        PropertyID.UPCA_ENABLE,
                        PropertyID.UPCA_SEND_CHECK,
                        PropertyID.UPCA_SEND_SYS,
                        PropertyID.UPCA_TO_EAN13,
                };
                propertyName_buffer = new String[]{
                        Settings.System.UPCA_ENABLE,
                        Settings.System.UPCA_SEND_CHECK,
                        Settings.System.UPCA_SEND_SYS,
                        Settings.System.UPCA_TO_EAN13,
                };
                value_buffer = new int[id_buffer.length];

                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                decoder_type_UPC_A.setChecked(value_buffer[i++] == 1);
                decoder_type_UPC_A_send_check.setChecked(value_buffer[i++] == 1);
                if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                    decoder_type_UPC_A_send_sys.setChecked(value_buffer[i++] == 1);
                    decoder_type_UPC_A_to_ean13.setChecked(value_buffer[i++] == 1);

                } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                    decoder_type_UPC_A_preamble_sys.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_UPC_A_preamble_sys.setSummary(decoder_type_UPC_A_preamble_sys.getEntry());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.UPCE_ENABLE.equals(symbologyType)) {
            upce = new PreferenceCategory(getActivity());
            upce.setKey(getString(R.string.scanner_symbology_upc_e));
            upce.setTitle(R.string.scanner_symbology_upc_e);
            root.addPreference(upce);
            decoder_type_UPC_E = new SwitchPreference(getActivity());
            decoder_type_UPC_E.setKey(Settings.System.UPCE_ENABLE);
            decoder_type_UPC_E.setTitle(R.string.scanner_symbology_enable);
            upce.addPreference(decoder_type_UPC_E);
            decoder_type_UPC_E_to_upca = new SwitchPreference(getActivity());
            decoder_type_UPC_E_to_upca.setKey(Settings.System.UPCE_TO_UPCA);
            decoder_type_UPC_E_to_upca.setTitle(R.string.scanner_symbology_convert_to_upc_a);
            decoder_type_UPC_E_send_check = new SwitchPreference(getActivity());
            decoder_type_UPC_E_send_check.setKey(Settings.System.UPCE_SEND_CHECK);
            decoder_type_UPC_E_send_check.setTitle(R.string.scanner_symbology_send_checksum);
            decoder_type_UPC_E_send_sys = new SwitchPreference(getActivity());
            decoder_type_UPC_E_send_sys.setKey(Settings.System.UPCE_SEND_SYS);
            decoder_type_UPC_E_send_sys.setTitle(R.string.scanner_symbology_send_system_digit);

            decoder_type_UPC_E_preamble_sys = new ListPreference(getActivity());
            decoder_type_UPC_E_preamble_sys.setKey(Settings.System.UPCE_SEND_SYS);
            decoder_type_UPC_E_preamble_sys.setTitle(R.string.scanner_symbology_send_system_digit);
            decoder_type_UPC_E_preamble_sys.setEntries(R.array.scanner_zebra_Preamble_characters_entries);
            decoder_type_UPC_E_preamble_sys.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_type_UPC_E_preamble_sys.setOnPreferenceChangeListener(this);

            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                upce.addPreference(decoder_type_UPC_E_send_sys);
            } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                upce.addPreference(decoder_type_UPC_E_preamble_sys);
            }
            upce.addPreference(decoder_type_UPC_E_send_check);
            upce.addPreference(decoder_type_UPC_E_to_upca);

            try {
                id_buffer = new int[]{
                        PropertyID.UPCE_ENABLE,
                        PropertyID.UPCE_SEND_CHECK,
                        PropertyID.UPCE_SEND_SYS,
                        PropertyID.UPCE_TO_UPCA,

                };
                propertyName_buffer = new String[]{
                        Settings.System.UPCE_ENABLE,
                        Settings.System.UPCE_SEND_CHECK,
                        Settings.System.UPCE_SEND_SYS,
                        Settings.System.UPCE_TO_UPCA,
                };
                value_buffer = new int[id_buffer.length];

                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                decoder_type_UPC_E.setChecked(value_buffer[i++] == 1);
                decoder_type_UPC_E_send_check.setChecked(value_buffer[i++] == 1);
                if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                    decoder_type_UPC_E_send_sys.setChecked(value_buffer[i++] == 1);
                    decoder_type_UPC_E_to_upca.setChecked(value_buffer[i++] == 1);

                } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                    decoder_type_UPC_E_preamble_sys.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_UPC_E_preamble_sys.setSummary(decoder_type_UPC_E_preamble_sys.getEntry());
                    decoder_type_UPC_E_to_upca.setChecked(value_buffer[i++] == 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.UPCE1_ENABLE.equals(symbologyType)) {
            upce1 = new PreferenceCategory(getActivity());
            upce1.setKey(getString(R.string.scanner_symbology_upc_e1));
            upce1.setTitle(R.string.scanner_symbology_upc_e1);
            root.addPreference(upce1);
            decoder_type_UPC_E1 = new SwitchPreference(getActivity());
            decoder_type_UPC_E1.setKey(Settings.System.UPCE1_ENABLE);
            decoder_type_UPC_E1.setTitle(R.string.scanner_symbology_enable);
            upce1.addPreference(decoder_type_UPC_E1);
            decoder_type_UPC_E1_to_upca = new SwitchPreference(getActivity());
            decoder_type_UPC_E1_to_upca.setKey(Settings.System.UPCE1_TO_UPCA);
            decoder_type_UPC_E1_to_upca.setTitle(R.string.scanner_symbology_convert_to_upc_a);
            decoder_type_UPC_E1_send_check = new SwitchPreference(getActivity());
            decoder_type_UPC_E1_send_check.setKey(Settings.System.UPCE1_SEND_CHECK);
            decoder_type_UPC_E1_send_check.setTitle(R.string.scanner_symbology_send_checksum);
            decoder_type_UPC_E1_send_sys = new SwitchPreference(getActivity());
            decoder_type_UPC_E1_send_sys.setKey(Settings.System.UPCE1_SEND_SYS);
            decoder_type_UPC_E1_send_sys.setTitle(R.string.scanner_symbology_send_system_digit);

            decoder_type_UPC_E1_preamble_sys = new ListPreference(getActivity());
            decoder_type_UPC_E1_preamble_sys.setKey(Settings.System.UPCE1_SEND_SYS);
            decoder_type_UPC_E1_preamble_sys.setTitle(R.string.scanner_symbology_send_system_digit);
            decoder_type_UPC_E1_preamble_sys.setEntries(R.array.scanner_zebra_Preamble_characters_entries);
            decoder_type_UPC_E1_preamble_sys.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_type_UPC_E1_preamble_sys.setOnPreferenceChangeListener(this);

            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                upce1.addPreference(decoder_type_UPC_E1_send_sys);
            } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                upce1.addPreference(decoder_type_UPC_E1_preamble_sys);
            }
            upce1.addPreference(decoder_type_UPC_E1_send_check);
            upce1.addPreference(decoder_type_UPC_E1_to_upca);

            try {
                id_buffer = new int[]{
                        PropertyID.UPCE1_ENABLE,
                        PropertyID.UPCE1_SEND_CHECK,
                        PropertyID.UPCE1_SEND_SYS,
                        PropertyID.UPCE1_TO_UPCA,
                };
                propertyName_buffer = new String[]{
                        Settings.System.UPCE1_ENABLE,
                        Settings.System.UPCE1_SEND_CHECK,
                        Settings.System.UPCE1_SEND_SYS,
                        Settings.System.UPCE1_TO_UPCA,
                };
                value_buffer = new int[id_buffer.length];

                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                decoder_type_UPC_E1.setChecked(value_buffer[i++] == 1);
                decoder_type_UPC_E1_send_check.setChecked(value_buffer[i++] == 1);
                if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                    decoder_type_UPC_E1_send_sys.setChecked(value_buffer[i++] == 1);
                    decoder_type_UPC_E1_to_upca.setChecked(value_buffer[i++] == 1);

                } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                    decoder_type_UPC_E1_preamble_sys.setValue(String.valueOf(value_buffer[i++]));
                    decoder_type_UPC_E1_preamble_sys.setSummary(decoder_type_UPC_E1_preamble_sys.getEntry());
                    decoder_type_UPC_E1_to_upca.setChecked(value_buffer[i++] == 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.POSTAL_GROUP_TYPE_ENABLE.equals(symbologyType)) {
            scanner_postal_symbologies = new ListPreference(getActivity());
            scanner_postal_symbologies.setKey(Settings.System.POSTAL_GROUP_TYPE_ENABLE);
            scanner_postal_symbologies.setTitle(R.string.scanner_symbology_postal);
            scanner_postal_symbologies.setEntries(R.array.postal_sym_entries);
            scanner_postal_symbologies.setEntryValues(R.array.postal_sym_values);
            scanner_postal_symbologies.setOnPreferenceChangeListener(this);

            decoder_type_postal_planet_send_check = new SwitchPreference(getActivity());
            decoder_type_postal_planet_send_check.setKey(Settings.System.US_POSTAL_SEND_CHECK);
            decoder_type_postal_planet_send_check.setTitle(R.string.scanner_symbology_send_checksum);

            decoder_type_australian_post = new SwitchPreference(getActivity());
            decoder_type_australian_post.setKey(Settings.System.AUSTRALIAN_POST_ENABLE);
            decoder_type_australian_post.setTitle(R.string.scanner_symbology_postal_australian);

            decoder_type_Canadian_post = new SwitchPreference(getActivity());
            decoder_type_Canadian_post.setKey(Settings.System.Canadian_POSTAL_ENABLE);
            decoder_type_Canadian_post.setTitle(R.string.scanner_symbology_postal_Canadian);

            decoder_type_kix_code = new SwitchPreference(getActivity());
            decoder_type_kix_code.setKey(Settings.System.KIX_CODE_ENABLE);
            decoder_type_kix_code.setTitle(R.string.scanner_symbology_postal_kix);

            decoder_type_japan_code = new SwitchPreference(getActivity());
            decoder_type_japan_code.setKey(Settings.System.JAPANESE_POST_ENABLE);
            decoder_type_japan_code.setTitle(R.string.scanner_symbology_postal_japan);

            decoder_type_korean_code = new SwitchPreference(getActivity());
            decoder_type_korean_code.setKey(Settings.System.KOREA_POST_ENABLE);
            decoder_type_korean_code.setTitle(R.string.scanner_symbology_postal_Korean);

            decoder_type_royal_mail = new SwitchPreference(getActivity());
            decoder_type_royal_mail.setKey(Settings.System.AUSTRALIAN_POST_ENABLE);
            decoder_type_royal_mail.setTitle(R.string.scanner_symbology_postal_royal_mail);

            decoder_type_usps_4state = new SwitchPreference(getActivity());
            decoder_type_usps_4state.setKey(Settings.System.USPS_4STATE_ENABLE);
            decoder_type_usps_4state.setTitle(R.string.scanner_symbology_postal_usps4_state);

            decoder_type_ups_fics = new SwitchPreference(getActivity());
            decoder_type_ups_fics.setKey(Settings.System.UPU_FICS_ENABLE);
            decoder_type_ups_fics.setTitle(R.string.scanner_symbology_upu_fics);

            decoder_type_planet = new SwitchPreference(getActivity());
            decoder_type_planet.setKey(Settings.System.US_PLANET_ENABLE);
            decoder_type_planet.setTitle(R.string.scanner_symbology_postal_planet);

            decoder_type_postnet = new SwitchPreference(getActivity());
            decoder_type_postnet.setKey(Settings.System.US_POSTNET_ENABLE);
            decoder_type_postnet.setTitle(R.string.scanner_symbology_postal_postnet);

            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                root.addPreference(scanner_postal_symbologies);
                root.addPreference(decoder_type_postal_planet_send_check);
                root.addPreference(decoder_type_korean_code);
            } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                root.addPreference(decoder_type_australian_post);
                //root.addPreference(decoder_type_Canadian_post);
                root.addPreference(decoder_type_kix_code);
                root.addPreference(decoder_type_japan_code);
                root.addPreference(decoder_type_korean_code);
                root.addPreference(decoder_type_royal_mail);
                root.addPreference(decoder_type_usps_4state);
                root.addPreference(decoder_type_ups_fics);
                root.addPreference(decoder_type_planet);//enable UK Postal US Postal
                /*//Transmit US Postal Check Digit
                //Parameter # 95
                //Select whether to transmit US Postal data, which includes both US Postnet and US Planet, with or without the
                //check digit:
                //• *1 - Transmit US Postal Check Digit
                //• 0 - Do Not Transmit US Postal Check Digit
                Transmit UK Postal Check Digit
                Parameter # 96
                Select whether to transmit UK Postal data with or without the check digit:
                • *1 - Transmit UK Postal Check Digit
                • 0 - Do Not Transmit UK Postal Check Digit*/
                root.addPreference(decoder_type_postal_planet_send_check);
                root.addPreference(decoder_type_postnet);
            }

            try {
                id_buffer = new int[]{
                        PropertyID.POSTAL_GROUP_TYPE_ENABLE,
                        PropertyID.US_POSTNET_ENABLE,        //postal code
                        PropertyID.US_PLANET_ENABLE,
                        PropertyID.US_POSTAL_SEND_CHECK,
                        PropertyID.USPS_4STATE_ENABLE,
                        PropertyID.UPU_FICS_ENABLE,
                        PropertyID.ROYAL_MAIL_ENABLE,
                        PropertyID.KOREA_POST_ENABLE,
                        PropertyID.AUSTRALIAN_POST_ENABLE,
                        PropertyID.Canadian_POSTAL_ENABLE,
                        PropertyID.KIX_CODE_ENABLE,
                        PropertyID.JAPANESE_POST_ENABLE,
                };
                propertyName_buffer = new String[]{
                        Settings.System.POSTAL_GROUP_TYPE_ENABLE,
                        Settings.System.US_POSTNET_ENABLE,        //postal code
                        Settings.System.US_PLANET_ENABLE,
                        Settings.System.US_POSTAL_SEND_CHECK,
                        Settings.System.USPS_4STATE_ENABLE,
                        Settings.System.UPU_FICS_ENABLE,
                        Settings.System.ROYAL_MAIL_ENABLE,
                        Settings.System.KOREA_POST_ENABLE,
                        Settings.System.AUSTRALIAN_POST_ENABLE,
                        Settings.System.Canadian_POSTAL_ENABLE,
                        Settings.System.KIX_CODE_ENABLE,
                        Settings.System.JAPANESE_POST_ENABLE,
                };
                value_buffer = new int[id_buffer.length];
                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                int val = value_buffer[i++];
                scanner_postal_symbologies.setValue(String.valueOf(val < 0 ? 0 : val));
                scanner_postal_symbologies.setSummary(scanner_postal_symbologies.getEntry());
                decoder_type_postnet.setChecked(value_buffer[i++] == 1);
                decoder_type_planet.setChecked(value_buffer[i++] == 1);
                decoder_type_postal_planet_send_check.setChecked(value_buffer[i++] == 1);
                decoder_type_usps_4state.setChecked(value_buffer[i++] == 1);
                decoder_type_ups_fics.setChecked(value_buffer[i++] == 1);
                decoder_type_royal_mail.setChecked(value_buffer[i++] == 1);
                decoder_type_korean_code.setChecked(value_buffer[i++] == 1);
                decoder_type_australian_post.setChecked(value_buffer[i++] == 1);
                decoder_type_Canadian_post.setChecked(value_buffer[i++] == 1);
                decoder_type_kix_code.setChecked(value_buffer[i++] == 1);
                decoder_type_japan_code.setChecked(value_buffer[i++] == 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Settings.System.EAN_EXT_ENABLE_2_5_DIGIT.equals(symbologyType)) {
            upc_ena_extensions = new PreferenceCategory(getActivity());
            upc_ena_extensions.setKey(getString(R.string.scanner_symbology_upc_ena_extensions));
            upc_ena_extensions.setTitle(R.string.scanner_symbology_upc_ena_extensions);
            root.addPreference(upc_ena_extensions);
            decoder_convert_databar_upc_ean = new SwitchPreference(getActivity());
            decoder_convert_databar_upc_ean.setKey(Settings.System.GS1_14_TO_UPC_EAN);
            decoder_convert_databar_upc_ean.setTitle(R.string.scanner_convert_databar_to_upc_ean);
            decoder_upc_reduced_quiet_zone = new SwitchPreference(getActivity());
            decoder_upc_reduced_quiet_zone.setKey(Settings.System.UCC_REDUCED_QUIET_ZONE);
            decoder_upc_reduced_quiet_zone.setTitle(R.string.scanner_symbology_reduced_quiet_zones);
            decoder_bookland = new SwitchPreference(getActivity());
            decoder_bookland.setKey(Settings.System.EAN13_BOOKLANDEAN);
            decoder_bookland.setTitle(R.string.scanner_symbology_booklandean);

            decoder_bookland_format = new ListPreference(getActivity());
            decoder_bookland_format.setKey(Settings.System.EAN13_BOOKLAND_FORMAT);
            decoder_bookland_format.setTitle(R.string.scanner_symbology_bookland_format);
            decoder_bookland_format.setEntries(R.array.scanner_zebra_bookland_format_entries);
            decoder_bookland_format.setEntryValues(R.array.scanner_msi_check_mod_value);
            decoder_bookland_format.setOnPreferenceChangeListener(this);

            decoder_ucc_coupon_ext = new SwitchPreference(getActivity());
            decoder_ucc_coupon_ext.setKey(Settings.System.UCC_COUPON_EXT_CODE);
            decoder_ucc_coupon_ext.setTitle(R.string.scanner_symbology_ucc_coupon_extended);

            decoder_coupon_report_mode = new ListPreference(getActivity());
            decoder_coupon_report_mode.setKey(Settings.System.UCC_COUPON_EXT_REPORT_MODE);
            decoder_coupon_report_mode.setTitle(R.string.scanner_symbology_ucc_coupon_mode);
            decoder_coupon_report_mode.setEntries(R.array.scanner_zebra_ucc_coupon_mode_entries);
            decoder_coupon_report_mode.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_coupon_report_mode.setOnPreferenceChangeListener(this);

            decoder_ean_zero_extend = new SwitchPreference(getActivity());
            decoder_ean_zero_extend.setKey(Settings.System.UCC_EAN_ZERO_EXTEND);
            decoder_ean_zero_extend.setTitle(R.string.scanner_symbology_ean_zero_extend);

            decoder_upc_ean_security_level = new ListPreference(getActivity());
            decoder_upc_ean_security_level.setKey(Settings.System.UPC_EAN_SECURITY_LEVEL);
            decoder_upc_ean_security_level.setTitle(R.string.scanner_symbology_upc_ean_security_level);
            decoder_upc_ean_security_level.setEntries(R.array.upc_ean_security_level_entries);
            decoder_upc_ean_security_level.setEntryValues(R.array.linear_security_level_values);
            decoder_upc_ean_security_level.setOnPreferenceChangeListener(this);

            decoder_upc_ean_supplemental_mode = new ListPreference(getActivity());
            decoder_upc_ean_supplemental_mode.setKey(Settings.System.UCC_EAN_SUPPLEMENTAL_MODE);
            decoder_upc_ean_supplemental_mode.setTitle(R.string.scanner_symbology_ucc_coupon_mode);
            decoder_upc_ean_supplemental_mode.setEntries(R.array.scanner_zebra_ucc_coupon_mode_entries);
            decoder_upc_ean_supplemental_mode.setEntryValues(R.array.image_inverse_decoder_values);
            decoder_upc_ean_supplemental_mode.setOnPreferenceChangeListener(this);

            decoder_type_upc_ean_25 = new SwitchPreference(getActivity());
            decoder_type_upc_ean_25.setKey(Settings.System.EAN_EXT_ENABLE_2_5_DIGIT);
            decoder_type_upc_ean_25.setTitle(R.string.scanner_symbology_enable_25digit_extensions);
            if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE)) {
                upc_ena_extensions.addPreference(decoder_convert_databar_upc_ean);
                upc_ena_extensions.addPreference(decoder_upc_reduced_quiet_zone);
                upc_ena_extensions.addPreference(decoder_bookland);
                upc_ena_extensions.addPreference(decoder_bookland_format);
                upc_ena_extensions.addPreference(decoder_ucc_coupon_ext);
                upc_ena_extensions.addPreference(decoder_coupon_report_mode);
                upc_ena_extensions.addPreference(decoder_ean_zero_extend);
                upc_ena_extensions.addPreference(decoder_upc_ean_security_level);
                upc_ena_extensions.addPreference(decoder_type_upc_ean_25);
                //upc_ena_extensions.addPreference(decoder_upc_ean_supplemental_mode);
            } else if (ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_HONEYWELL_ENGINE_PREFERENCE)) {
                upc_ena_extensions.addPreference(decoder_bookland);
                upc_ena_extensions.addPreference(decoder_type_upc_ean_25);
            }
            try {
                id_buffer = new int[]{
                        PropertyID.EAN_EXT_ENABLE_2_5_DIGIT,
                        PropertyID.UCC_COUPON_EXT_CODE,
                        PropertyID.UCC_COUPON_EXT_REPORT_MODE,
                        PropertyID.UPC_EAN_SECURITY_LEVEL,
                        PropertyID.GS1_14_TO_UPC_EAN,
                        PropertyID.UCC_REDUCED_QUIET_ZONE,
                        PropertyID.EAN13_BOOKLANDEAN,
                        PropertyID.EAN13_BOOKLAND_FORMAT,
                        PropertyID.UCC_EAN_ZERO_EXTEND,
                        PropertyID.UCC_EAN_SUPPLEMENTAL_MODE,
                };
                propertyName_buffer = new String[]{
                        Settings.System.EAN_EXT_ENABLE_2_5_DIGIT,
                        Settings.System.UCC_COUPON_EXT_CODE,
                        Settings.System.UCC_COUPON_EXT_REPORT_MODE,
                        Settings.System.UPC_EAN_SECURITY_LEVEL,
                        Settings.System.GS1_14_TO_UPC_EAN,
                        Settings.System.UCC_REDUCED_QUIET_ZONE,
                        Settings.System.EAN13_BOOKLANDEAN,
                        Settings.System.EAN13_BOOKLAND_FORMAT,
                        Settings.System.UCC_EAN_ZERO_EXTEND,
                        Settings.System.UCC_EAN_SUPPLEMENTAL_MODE,
                };
                value_buffer = new int[id_buffer.length];

                mApplication.getPropertyInts(profileId, propertyName_buffer, value_buffer);
                decoder_type_upc_ean_25.setChecked(value_buffer[i++] == 1);
                decoder_ucc_coupon_ext.setChecked(value_buffer[i++] == 1);
                decoder_coupon_report_mode.setValue(String.valueOf(value_buffer[i++]));
                decoder_coupon_report_mode.setSummary(decoder_coupon_report_mode.getEntry());
                decoder_upc_ean_security_level.setValue(String.valueOf(value_buffer[i++]));
                decoder_upc_ean_security_level.setSummary(decoder_upc_ean_security_level.getEntry());
                decoder_convert_databar_upc_ean.setChecked(value_buffer[i++] == 1);
                decoder_upc_reduced_quiet_zone.setChecked(value_buffer[i++] == 1);
                decoder_bookland.setChecked(value_buffer[i++] == 1);
                decoder_bookland_format.setValue(String.valueOf(value_buffer[i++]));
                decoder_bookland_format.setSummary(decoder_bookland_format.getEntry());
                decoder_ean_zero_extend.setChecked(value_buffer[i++] == 1);
                    /*decoder_upc_ean_supplemental_mode.setValue(String.valueOf(value_buffer[i++]));
                    decoder_upc_ean_supplemental_mode.setSummary(decoder_upc_ean_supplemental_mode.getEntry());*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        /*try {
            if (length_value_buffer != null) {
                mApplication.setPropertyInts(profileId, id_buffer, propertyName_buffer, length_value_buffer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private void enableSymbology(int id, String propertyName, boolean enable) {
        //默认配置则同步到系统扫描服务中
        mApplication.setPropertyInt(profileId, id, propertyName, enable ? 1 : 0);
    }

    private void enableSymbology(int id, String propertyName, int enable) {
        //默认配置则同步到系统扫描服务中
        mApplication.setPropertyInt(profileId, id, propertyName, enable);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference instanceof EditTextPreference) {
            ((EditTextPreference) preference).getEditText().setText(((EditTextPreference) preference).getSummary());
            return false;
        }
        try {
            if (preference == decoder_type_aztec) {
                enableSymbology(PropertyID.AZTEC_ENABLE, Settings.System.AZTEC_ENABLE, decoder_type_aztec.isChecked());
            } else if (preference == decoder_type_chinese_2_of_5) {
                enableSymbology(PropertyID.C25_ENABLE, Settings.System.C25_ENABLE, decoder_type_chinese_2_of_5.isChecked());
            } else if (preference == decoder_type_Codabar) {
                enableSymbology(PropertyID.CODABAR_ENABLE, Settings.System.CODABAR_ENABLE, decoder_type_Codabar.isChecked());
            } else if (preference == decoder_type_Codabar_enable_check) {
                enableSymbology(PropertyID.CODABAR_ENABLE_CHECK, Settings.System.CODABAR_ENABLE_CHECK, decoder_type_Codabar_enable_check.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_Codabar_send_check) {
                enableSymbology(PropertyID.CODABAR_SEND_CHECK, Settings.System.CODABAR_SEND_CHECK, decoder_type_Codabar_send_check.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_Codabar_send_start) {
                enableSymbology(PropertyID.CODABAR_SEND_START, Settings.System.CODABAR_SEND_START, decoder_type_Codabar_send_start.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_Codabar_clsi_editing) {
                enableSymbology(PropertyID.CODABAR_CLSI, Settings.System.CODABAR_CLSI, decoder_type_Codabar_clsi_editing.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_Codabar_notis_editing) {
                enableSymbology(PropertyID.CODABAR_NOTIS, Settings.System.CODABAR_NOTIS, decoder_type_Codabar_notis_editing.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_code11) {
                enableSymbology(PropertyID.CODE11_ENABLE, Settings.System.CODE11_ENABLE, decoder_type_code11.isChecked());
            } else if (preference == decoder_type_code11_enable_check) {
                enableSymbology(PropertyID.CODE11_ENABLE_CHECK, Settings.System.CODE11_ENABLE_CHECK, decoder_type_code11_enable_check.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_code11_send_check) {
                enableSymbology(PropertyID.CODE11_SEND_CHECK, Settings.System.CODE11_SEND_CHECK, decoder_type_code11_send_check.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_code128) {
                enableSymbology(PropertyID.CODE128_ENABLE, Settings.System.CODE128_ENABLE, decoder_type_code128.isChecked());
            } else if (preference == decoder_code_isbt_128) {
                enableSymbology(PropertyID.CODE_ISBT_128, Settings.System.CODE_ISBT_128, decoder_type_code128.isChecked());
            } else if (preference == decoder_check_isbt_table) {
                enableSymbology(PropertyID.CODE128_CHECK_ISBT_TABLE, Settings.System.CODE128_CHECK_ISBT_TABLE, decoder_check_isbt_table.isChecked());
            } else if (preference == decoder_gs1128) {
                enableSymbology(PropertyID.CODE128_GS1_ENABLE, Settings.System.CODE128_GS1_ENABLE, decoder_gs1128.isChecked());
            } else if (preference == decoder_type_code39) {
                enableSymbology(PropertyID.CODE39_ENABLE, Settings.System.CODE39_ENABLE, decoder_type_code39.isChecked());
            } else if (preference == decoder_type_code39_enable_check) {
                enableSymbology(PropertyID.CODE39_ENABLE_CHECK, Settings.System.CODE39_ENABLE_CHECK, decoder_type_code39_enable_check.isChecked());
            } else if (preference == decoder_type_code39_send_check) {
                enableSymbology(PropertyID.CODE39_SEND_CHECK, Settings.System.CODE39_SEND_CHECK, decoder_type_code39_send_check.isChecked());
            } else if (preference == decoder_type_code39_full_ascii) {
                enableSymbology(PropertyID.CODE39_FULL_ASCII, Settings.System.CODE39_FULL_ASCII, decoder_type_code39_full_ascii.isChecked());
            } else if (preference == decoder_type_code39_quiet_zone) {
                enableSymbology(PropertyID.CODE39_Quiet_Zone, Settings.System.CODE39_Quiet_Zone, decoder_type_code39_quiet_zone.isChecked());
            } else if (preference == decoder_type_code39_start_stop) {
                enableSymbology(PropertyID.CODE39_START_STOP, Settings.System.CODE39_START_STOP, decoder_type_code39_start_stop.isChecked());
            } else if (preference == decoder_type_code32) {
                enableSymbology(PropertyID.CODE32_ENABLE, Settings.System.CODE32_ENABLE, decoder_type_code32.isChecked());
            } else if (preference == decoder_type_code32_send_start) {
                enableSymbology(PropertyID.CODE32_SEND_START, Settings.System.CODE32_SEND_START, decoder_type_code32_send_start.isChecked());
            } else if (preference == decoder_type_code93) {
                enableSymbology(PropertyID.CODE93_ENABLE, Settings.System.CODE93_ENABLE, decoder_type_code93.isChecked());
            } else if (preference == decoder_type_composite_cc_c) {
                enableSymbology(PropertyID.COMPOSITE_CC_C_ENABLE, Settings.System.COMPOSITE_CC_C_ENABLE, decoder_type_composite_cc_c.isChecked());
            } else if (preference == decoder_type_composite_cc_ab) {
                enableSymbology(PropertyID.COMPOSITE_CC_AB_ENABLE, Settings.System.COMPOSITE_CC_AB_ENABLE, decoder_type_composite_cc_ab.isChecked());
            } else if (preference == decoder_type_composite_39) {
                enableSymbology(PropertyID.COMPOSITE_TLC39_ENABLE, Settings.System.COMPOSITE_TLC39_ENABLE, decoder_type_composite_39.isChecked());
            } else if (preference == decoder_type_Discrete_2_of_5) {
                enableSymbology(PropertyID.D25_ENABLE, Settings.System.D25_ENABLE, decoder_type_Discrete_2_of_5.isChecked());
            } else if (preference == decoder_type_datamatrix) {
                enableSymbology(PropertyID.DATAMATRIX_ENABLE, Settings.System.DATAMATRIX_ENABLE, decoder_type_datamatrix.isChecked());
            } else if (preference == decoder_type_dotcode) {
                enableSymbology(PropertyID.DOTCODE_ENABLE, Settings.System.DOTCODE_ENABLE, decoder_type_dotcode.isChecked());
            } else if (preference == decoder_type_EAN13) {
                enableSymbology(PropertyID.EAN13_ENABLE, Settings.System.EAN13_ENABLE, decoder_type_EAN13.isChecked());
            } else if (preference == decoder_type_EAN13_send_check) {
                enableSymbology(PropertyID.EAN13_SEND_CHECK, Settings.System.EAN13_SEND_CHECK, decoder_type_EAN13_send_check.isChecked());
            } else if (preference == decoder_type_EAN8) {
                enableSymbology(PropertyID.EAN8_ENABLE, Settings.System.EAN8_ENABLE, decoder_type_EAN8.isChecked());
            } else if (preference == decoder_type_EAN8_send_check) {
                enableSymbology(PropertyID.EAN8_SEND_CHECK, Settings.System.EAN8_SEND_CHECK, decoder_type_EAN8_send_check.isChecked());
            } else if (preference == decoder_type_gs1_databar14) {
                enableSymbology(PropertyID.GS1_14_ENABLE, Settings.System.GS1_14_ENABLE, decoder_type_gs1_databar14.isChecked());
            } else if (preference == decoder_type_gs1_Limited) {
                enableSymbology(PropertyID.GS1_LIMIT_ENABLE, Settings.System.GS1_LIMIT_ENABLE, decoder_type_gs1_Limited.isChecked());
            } else if (preference == decoder_type_RSS_Expanded) {
                enableSymbology(PropertyID.GS1_EXP_ENABLE, Settings.System.GS1_EXP_ENABLE, decoder_type_RSS_Expanded.isChecked());
            } else if (preference == decoder_type_hanxin) {
                enableSymbology(PropertyID.HANXIN_ENABLE, Settings.System.HANXIN_ENABLE, decoder_type_hanxin.isChecked());
            } else if (preference == decoder_type_Interleaved_2_of_5) {
                enableSymbology(PropertyID.I25_ENABLE, Settings.System.I25_ENABLE, decoder_type_Interleaved_2_of_5.isChecked());
            } else if (preference == decoder_type_Interleaved_2_of_5_en_check) {
                enableSymbology(PropertyID.I25_ENABLE_CHECK, Settings.System.I25_ENABLE_CHECK, decoder_type_Interleaved_2_of_5_en_check.isChecked());
            } else if (preference == decoder_type_Interleaved_2_of_5_send_check) {
                enableSymbology(PropertyID.I25_SEND_CHECK, Settings.System.I25_SEND_CHECK, decoder_type_Interleaved_2_of_5_send_check.isChecked());
            } else if (preference == decoder_Interleaved_2_of_5_to_ean13) {
                enableSymbology(PropertyID.I25_TO_EAN13, Settings.System.I25_TO_EAN13, decoder_Interleaved_2_of_5_to_ean13.isChecked());
            } else if (preference == decoder_type_Matrix_2_of_5) {
                enableSymbology(PropertyID.M25_ENABLE, Settings.System.M25_ENABLE, decoder_type_Matrix_2_of_5.isChecked());
            } else if (preference == decoder_type_Matrix25_send_check) {
                enableSymbology(PropertyID.M25_SEND_CHECK, Settings.System.M25_SEND_CHECK, decoder_type_Matrix25_send_check.isChecked());
            } else if (preference == decoder_type_Matrix25_enable_check) {
                enableSymbology(PropertyID.M25_ENABLE_CHECK, Settings.System.M25_ENABLE_CHECK, decoder_type_Matrix25_enable_check.isChecked());
            } else if (preference == decoder_type_maxicode) {
                enableSymbology(PropertyID.MAXICODE_ENABLE, Settings.System.MAXICODE_ENABLE, decoder_type_maxicode.isChecked());
            } else if (preference == decoder_type_micropdf417) {
                enableSymbology(PropertyID.MICROPDF417_ENABLE, Settings.System.MICROPDF417_ENABLE, decoder_type_micropdf417.isChecked());
            } else if (preference == decoder_type_microqrcode) {
                enableSymbology(PropertyID.MICROQRCODE_ENABLE, Settings.System.MICROQRCODE_ENABLE, decoder_type_microqrcode.isChecked());
            } else if (preference == decoder_type_MSI) {
                enableSymbology(PropertyID.MSI_ENABLE, Settings.System.MSI_ENABLE, decoder_type_MSI.isChecked());
            } else if (preference == decoder_type_MSI_send_check) {
                enableSymbology(PropertyID.MSI_SEND_CHECK, Settings.System.MSI_SEND_CHECK, decoder_type_MSI_send_check.isChecked());
            } else if (preference == decoder_type_pdf417) {
                enableSymbology(PropertyID.PDF417_ENABLE, Settings.System.PDF417_ENABLE, decoder_type_pdf417.isChecked());
            } else if (preference == decoder_type_qrcode) {
                enableSymbology(PropertyID.QRCODE_ENABLE, Settings.System.QRCODE_ENABLE, decoder_type_qrcode.isChecked());
            } else if (preference == decoder_type_trioptic) {
                enableSymbology(PropertyID.TRIOPTIC_ENABLE, Settings.System.TRIOPTIC_ENABLE, decoder_type_trioptic.isChecked());
            } else if (preference == decoder_type_UPC_A) {
                enableSymbology(PropertyID.UPCA_ENABLE, Settings.System.UPCA_ENABLE, decoder_type_UPC_A.isChecked());
            } else if (preference == decoder_type_UPC_A_send_check) {
                enableSymbology(PropertyID.UPCA_SEND_CHECK, Settings.System.UPCA_SEND_CHECK, decoder_type_UPC_A_send_check.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_UPC_A_send_sys) {
                enableSymbology(PropertyID.UPCA_SEND_SYS, Settings.System.UPCA_SEND_SYS, decoder_type_UPC_A_send_sys.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_UPC_A_to_ean13) {
                enableSymbology(PropertyID.UPCA_TO_EAN13, Settings.System.UPCA_TO_EAN13, decoder_type_UPC_A_to_ean13.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_UPC_E) {
                enableSymbology(PropertyID.UPCE_ENABLE, Settings.System.UPCE_ENABLE, decoder_type_UPC_E.isChecked());
            } else if (preference == decoder_type_UPC_E_send_check) {
                enableSymbology(PropertyID.UPCE_SEND_CHECK, Settings.System.UPCE_SEND_CHECK, decoder_type_UPC_E_send_check.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_UPC_E_send_sys) {
                enableSymbology(PropertyID.UPCE_SEND_SYS, Settings.System.UPCE_SEND_SYS, decoder_type_UPC_E_send_sys.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_UPC_E_to_upca) {
                enableSymbology(PropertyID.UPCE_TO_UPCA, Settings.System.UPCE_TO_UPCA, decoder_type_UPC_E_to_upca.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_UPC_E1) {
                enableSymbology(PropertyID.UPCE1_ENABLE, Settings.System.UPCE1_ENABLE, decoder_type_UPC_E1.isChecked());
            } else if (preference == decoder_type_UPC_E1_send_check) {
                enableSymbology(PropertyID.UPCE1_SEND_CHECK, Settings.System.UPCE1_SEND_CHECK, decoder_type_UPC_E1_send_check.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_UPC_E1_send_sys) {
                enableSymbology(PropertyID.UPCE1_SEND_SYS, Settings.System.UPCE1_SEND_SYS, decoder_type_UPC_E1_send_sys.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_UPC_E1_to_upca) {
                enableSymbology(PropertyID.UPCE1_TO_UPCA, Settings.System.UPCE1_TO_UPCA, decoder_type_UPC_E1_to_upca.isChecked() ? 1 : 0);
            } else if (preference == decoder_type_postal_planet_send_check) {
                enableSymbology(PropertyID.US_POSTAL_SEND_CHECK, Settings.System.US_POSTAL_SEND_CHECK, decoder_type_postal_planet_send_check.isChecked());
            } else if (preference == decoder_type_korean_code) {
                enableSymbology(PropertyID.KOREA_POST_ENABLE, Settings.System.KOREA_POST_ENABLE, decoder_type_korean_code.isChecked());
            } else if (preference == decoder_type_postnet) {
                enableSymbology(PropertyID.US_POSTNET_ENABLE, Settings.System.US_POSTNET_ENABLE, decoder_type_postnet.isChecked());
            } else if (preference == decoder_type_planet) {
                enableSymbology(PropertyID.US_PLANET_ENABLE, Settings.System.US_PLANET_ENABLE, decoder_type_planet.isChecked());
            } else if (preference == decoder_type_usps_4state) {
                enableSymbology(PropertyID.USPS_4STATE_ENABLE, Settings.System.USPS_4STATE_ENABLE, decoder_type_usps_4state.isChecked());
            } else if (preference == decoder_type_ups_fics) {
                enableSymbology(PropertyID.UPU_FICS_ENABLE, Settings.System.UPU_FICS_ENABLE, decoder_type_ups_fics.isChecked());
            } else if (preference == decoder_type_royal_mail) {
                enableSymbology(PropertyID.ROYAL_MAIL_ENABLE, Settings.System.ROYAL_MAIL_ENABLE, decoder_type_royal_mail.isChecked());
            } else if (preference == decoder_type_australian_post) {
                enableSymbology(PropertyID.AUSTRALIAN_POST_ENABLE, Settings.System.AUSTRALIAN_POST_ENABLE, decoder_type_australian_post.isChecked());
            } else if (preference == decoder_type_Canadian_post) {
                enableSymbology(PropertyID.Canadian_POSTAL_ENABLE, Settings.System.Canadian_POSTAL_ENABLE, decoder_type_Canadian_post.isChecked());
            } else if (preference == decoder_type_kix_code) {
                enableSymbology(PropertyID.KIX_CODE_ENABLE, Settings.System.KIX_CODE_ENABLE, decoder_type_kix_code.isChecked());
            } else if (preference == decoder_type_japan_code) {
                enableSymbology(PropertyID.JAPANESE_POST_ENABLE, Settings.System.JAPANESE_POST_ENABLE, decoder_type_japan_code.isChecked());
            } else if (preference == decoder_type_upc_ean_25) {
                enableSymbology(PropertyID.EAN_EXT_ENABLE_2_5_DIGIT, Settings.System.EAN_EXT_ENABLE_2_5_DIGIT, decoder_type_upc_ean_25.isChecked());
            } else if (preference == decoder_ucc_coupon_ext) {
                enableSymbology(PropertyID.UCC_COUPON_EXT_CODE, Settings.System.UCC_COUPON_EXT_CODE, decoder_ucc_coupon_ext.isChecked());
            } else if (preference == decoder_convert_databar_upc_ean) {
                enableSymbology(PropertyID.GS1_14_TO_UPC_EAN, Settings.System.GS1_14_TO_UPC_EAN, decoder_convert_databar_upc_ean.isChecked());
            } else if (preference == decoder_upc_reduced_quiet_zone) {
                enableSymbology(PropertyID.UCC_REDUCED_QUIET_ZONE, Settings.System.UCC_REDUCED_QUIET_ZONE, decoder_upc_reduced_quiet_zone.isChecked());
            } else if (preference == decoder_bookland) {
                enableSymbology(PropertyID.EAN13_BOOKLANDEAN, Settings.System.EAN13_BOOKLANDEAN, decoder_bookland.isChecked());
            } else if (preference == decoder_ean_zero_extend) {
                enableSymbology(PropertyID.UCC_EAN_ZERO_EXTEND, Settings.System.UCC_EAN_ZERO_EXTEND, decoder_ean_zero_extend.isChecked());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        if (preference == decoder_type_aztec_inverse) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_aztec_inverse.setSummary(decoder_type_aztec_inverse.getEntries()[value]);
            enableSymbology(PropertyID.AZTEC_INVERSE, Settings.System.AZTEC_INVERSE, value);
        } else if (decoder_type_code11_check_mode == preference) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_code11_check_mode.setSummary(decoder_type_code11_check_mode.getEntries()[value]);
            enableSymbology(PropertyID.CODE11_SEND_CHECK, Settings.System.CODE11_SEND_CHECK, value);
        } else if (preference == decoder_type_code39_security_level) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_code39_security_level.setSummary(decoder_type_code39_security_level.getEntries()[value]);
            enableSymbology(PropertyID.CODE39_SECURITY_LEVEL, Settings.System.CODE39_SECURITY_LEVEL, value);
        } else if (preference == decoder_type_composite_upc_mode) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_composite_upc_mode.setSummary(decoder_type_composite_upc_mode.getEntries()[value]);
            enableSymbology(PropertyID.COMPOSITE_UPC_MODE, Settings.System.COMPOSITE_UPC_MODE, value);
        } else if (preference == decoder_type_datamatrix_inverse) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_datamatrix_inverse.setSummary(decoder_type_datamatrix_inverse.getEntries()[value]);
            enableSymbology(PropertyID.DATAMATRIX_INVERSE, Settings.System.DATAMATRIX_INVERSE, value);
        } else if (preference == decoder_type_gs1_limit_security_level) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_gs1_limit_security_level.setSummary(decoder_type_gs1_limit_security_level.getEntries()[value]);
            enableSymbology(PropertyID.GS1_LIMIT_Security_Level, Settings.System.GS1_LIMIT_Security_Level, value);
        } else if (preference == decoder_type_hanxin_inverse) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_hanxin_inverse.setSummary(decoder_type_hanxin_inverse.getEntries()[value]);
            enableSymbology(PropertyID.HANXIN_INVERSE, Settings.System.HANXIN_INVERSE, value);
        } else if (preference == decoder_Int25_reduced_quiet_zone) {
            int value = Integer.parseInt((String) newValue);
            decoder_Int25_reduced_quiet_zone.setSummary(decoder_Int25_reduced_quiet_zone.getEntries()[value]);
            enableSymbology(PropertyID.I25_QUIET_ZONE, Settings.System.I25_QUIET_ZONE, value);
        } else if (preference == decoder_Int25_security_level) {
            int value = Integer.parseInt((String) newValue);
            decoder_Int25_security_level.setSummary(decoder_Int25_security_level.getEntries()[value]);
            enableSymbology(PropertyID.I25_SECURITY_LEVEL, Settings.System.I25_SECURITY_LEVEL, value);
        } else if (preference == decoder_type_MSI_2_check) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_MSI_2_check.setSummary(decoder_type_MSI_2_check.getEntries()[value]);
            enableSymbology(PropertyID.MSI_REQUIRE_2_CHECK, Settings.System.MSI_REQUIRE_2_CHECK, value);
        } else if (preference == decoder_type_MSI_2_mod_11) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_MSI_2_mod_11.setSummary(decoder_type_MSI_2_mod_11.getEntries()[value]);
            enableSymbology(PropertyID.MSI_CHECK_2_MOD_11, Settings.System.MSI_CHECK_2_MOD_11, value);
        } else if (preference == decoder_type_qrcode_inverse) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_qrcode_inverse.setSummary(decoder_type_qrcode_inverse.getEntries()[value]);
            enableSymbology(PropertyID.QRCODE_INVERSE, Settings.System.QRCODE_INVERSE, value);
        } else if (preference == decoder_type_qrcode_size) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_qrcode_size.setSummary(decoder_type_qrcode_size.getEntries()[value]);
            enableSymbology(PropertyID.QRCODE_SYMBOL_SIZE, Settings.System.QRCODE_SYMBOL_SIZE, value);
        } else if (preference == decoder_type_dm_size) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_dm_size.setSummary(decoder_type_dm_size.getEntries()[value]);
            enableSymbology(PropertyID.DATAMATRIX_SYMBOL_SIZE, Settings.System.DATAMATRIX_SYMBOL_SIZE, value);
        } else if (preference == decoder_type_aztec_size) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_aztec_size.setSummary(decoder_type_aztec_size.getEntries()[value]);
            enableSymbology(PropertyID.AZTEC_SYMBOL_SIZE, Settings.System.AZTEC_SYMBOL_SIZE, value);
        } else if (preference == decoder_type_maxicode_size) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_maxicode_size.setSummary(decoder_type_maxicode_size.getEntries()[value]);
            enableSymbology(PropertyID.MAXICODE_SYMBOL_SIZE, Settings.System.MAXICODE_SYMBOL_SIZE, value);
        } else if (preference == decoder_type_UPC_A_preamble_sys) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_UPC_A_preamble_sys.setSummary(decoder_type_UPC_A_preamble_sys.getEntries()[value]);
            enableSymbology(PropertyID.UPCA_SEND_SYS, Settings.System.UPCA_SEND_SYS, value);
        } else if (preference == decoder_type_UPC_E_preamble_sys) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_UPC_E_preamble_sys.setSummary(decoder_type_UPC_E_preamble_sys.getEntries()[value]);
            enableSymbology(PropertyID.UPCE_SEND_SYS, Settings.System.UPCE_SEND_SYS, value);
        } else if (preference == decoder_type_UPC_E1_preamble_sys) {
            int value = Integer.parseInt((String) newValue);
            decoder_type_UPC_E1_preamble_sys.setSummary(decoder_type_UPC_E1_preamble_sys.getEntries()[value]);
            enableSymbology(PropertyID.UPCE1_SEND_SYS, Settings.System.UPCE1_SEND_SYS, value);
        } else if (preference == scanner_postal_symbologies) {
            int value = Integer.parseInt((String) newValue);
            scanner_postal_symbologies.setSummary(scanner_postal_symbologies.getEntries()[value]);
            enableSymbology(PropertyID.POSTAL_GROUP_TYPE_ENABLE, Settings.System.POSTAL_GROUP_TYPE_ENABLE, value);
        } else if (preference == decoder_upc_ean_security_level) {
            int value = Integer.parseInt((String) newValue);
            decoder_upc_ean_security_level.setSummary(decoder_upc_ean_security_level.getEntries()[value]);
            enableSymbology(PropertyID.UPC_EAN_SECURITY_LEVEL, Settings.System.UPC_EAN_SECURITY_LEVEL, value);
        } else if (preference == decoder_coupon_report_mode) {
            int value = Integer.parseInt((String) newValue);
            decoder_coupon_report_mode.setSummary(decoder_coupon_report_mode.getEntries()[value]);
            enableSymbology(PropertyID.UCC_COUPON_EXT_REPORT_MODE, Settings.System.UCC_COUPON_EXT_REPORT_MODE, value);
        } else if (preference == decoder_bookland_format) {
            int value = Integer.parseInt((String) newValue);
            decoder_bookland_format.setSummary(decoder_bookland_format.getEntries()[value]);
            enableSymbology(PropertyID.EAN13_BOOKLAND_FORMAT, Settings.System.EAN13_BOOKLAND_FORMAT, value);
        } else if (preference == decoder_upc_ean_supplemental_mode) {
            int value = Integer.parseInt((String) newValue);
            decoder_upc_ean_supplemental_mode.setSummary(decoder_upc_ean_supplemental_mode.getEntries()[value]);
            enableSymbology(PropertyID.UCC_EAN_SUPPLEMENTAL_MODE, Settings.System.UCC_EAN_SUPPLEMENTAL_MODE, value);
        } else {
            if (preference instanceof EditTextPreference) {
                ((EditTextPreference) preference).setSummary(newValue.toString());
            }
            try {
                String value = newValue.toString();
                if (preference == decoder_L1_of_codabar) {//1 50
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 60) {
                        enableSymbology(PropertyID.CODABAR_LENGTH1, Settings.System.CODABAR_LENGTH1, len);
                    } else {
                        showAlertToast(1, 60);
                        return false;
                    }
                } else if (preference == decoder_L2_of_codabar) {
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 60) {
                        enableSymbology(PropertyID.CODABAR_LENGTH2, Settings.System.CODABAR_LENGTH2, len);
                    } else {
                        showAlertToast(1, 60);
                        return false;
                    }
                } else if (preference == decoder_L1_of_code128) {//1 80
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 80) {
                        enableSymbology(PropertyID.CODE128_LENGTH1, Settings.System.CODE128_LENGTH1, len);
                    } else {
                        showAlertToast(1, 80);
                        return false;
                    }
                } else if (preference == decoder_L2_of_code128) {
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 80) {
                        enableSymbology(PropertyID.CODE128_LENGTH2, Settings.System.CODE128_LENGTH2, len);
                    } else {
                        showAlertToast(1, 80);
                        return false;
                    }
                } else if (preference == decoder_L1_gs1128) {
                    //length_value_buffer[0] = Integer.parseInt(value);
                } else if (preference == decoder_L2_gs1128) {
                    //length_value_buffer[1] = Integer.parseInt(value);
                } else if (preference == decoder_L1_of_code39) {//1 50
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 55) {
                        enableSymbology(PropertyID.CODE39_LENGTH1, Settings.System.CODE39_LENGTH1, len);
                    } else {
                        showAlertToast(1, 55);
                        return false;
                    }
                } else if (preference == decoder_L2_of_code39) {
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 55) {
                        enableSymbology(PropertyID.CODE39_LENGTH2, Settings.System.CODE39_LENGTH2, len);
                    } else {
                        showAlertToast(1, 55);
                        return false;
                    }
                } else if (preference == decoder_L1_of_code93) {//1 50
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 80) {
                        enableSymbology(PropertyID.CODE93_LENGTH1, Settings.System.CODE93_LENGTH1, len);
                    } else {
                        showAlertToast(1, 80);
                        return false;
                    }
                } else if (preference == decoder_L2_of_code93) {
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 80) {
                        enableSymbology(PropertyID.CODE93_LENGTH2, Settings.System.CODE93_LENGTH2, len);
                    } else {
                        showAlertToast(1, 80);
                        return false;
                    }
                } else if (preference == decoder_l1_datamatrix) {
                    length_value_buffer[0] = Integer.parseInt(value);
                } else if (preference == decoder_l2_datamatrix) {
                    length_value_buffer[1] = Integer.parseInt(value);
                } else if (preference == decoder_l1_expanded) {//1 74
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 74) {
                        enableSymbology(PropertyID.GS1_EXP_LENGTH1, Settings.System.GS1_EXP_LENGTH1, len);
                    } else {
                        showAlertToast(1, 74);
                        return false;
                    }
                } else if (preference == decoder_l2_expanded) {
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 74) {
                        enableSymbology(PropertyID.GS1_EXP_LENGTH2, Settings.System.GS1_EXP_LENGTH2, len);
                    } else {
                        showAlertToast(1, 74);
                        return false;
                    }
                } else if (preference == decoder_L1_Interleaved_2_of_5) {//2 50
                    int len = Integer.parseInt(value);
                    if (len >= 2 && len <= 55) {
                        enableSymbology(PropertyID.I25_LENGTH1, Settings.System.I25_LENGTH1, len);
                    } else {
                        showAlertToast(2, 55);
                        return false;
                    }
                } else if (preference == decoder_L2_Interleaved_2_of_5) {
                    int len = Integer.parseInt(value);
                    if (len >= 2 && len <= 55) {
                        enableSymbology(PropertyID.I25_LENGTH2, Settings.System.I25_LENGTH2, len);
                    } else {
                        showAlertToast(2, 55);
                        return false;
                    }
                } else if (preference == decoder_L1_Matrix_2_of_5) {
                    length_value_buffer[0] = Integer.parseInt(value);
                } else if (preference == decoder_L2_Matrix_2_of_5) {
                    length_value_buffer[1] = Integer.parseInt(value);
                } else if (preference == decoder_L1_of_MSI) {//1 15
                    int len = Integer.parseInt(value);
                    if (len >= 4 && len <= 55) {
                        enableSymbology(PropertyID.MSI_LENGTH1, Settings.System.MSI_LENGTH1, len);
                    } else {
                        showAlertToast(4, 55);
                        return false;
                    }
                } else if (preference == decoder_L2_of_MSI) {
                    int len = Integer.parseInt(value);
                    if (len >= 4 && len <= 55) {
                        enableSymbology(PropertyID.MSI_LENGTH2, Settings.System.MSI_LENGTH2, len);
                    } else {
                        showAlertToast(4, 55);
                        return false;
                    }
                } else if (preference == decoder_L1_Discrete_2_of_5) {//1 50
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 55) {
                        enableSymbology(PropertyID.D25_LENGTH1, Settings.System.D25_LENGTH1, len);
                    } else {
                        showAlertToast(1, 55);
                        return false;
                    }
                } else if (preference == decoder_L2_Discrete_2_of_5) {
                    int len = Integer.parseInt(value);
                    if (len >= 1 && len <= 55) {
                        enableSymbology(PropertyID.D25_LENGTH2, Settings.System.D25_LENGTH2, len);
                    } else {
                        showAlertToast(1, 55);
                        return false;
                    }
                } else if (preference == decoder_L1_of_code11) {//1 50
                    int len = Integer.parseInt(value);
                    if (len >= 4 && len <= 55) {
                        enableSymbology(PropertyID.CODE11_LENGTH1, Settings.System.CODE11_LENGTH1, len);
                    } else {
                        showAlertToast(4, 55);
                        return false;
                    }
                } else if (preference == decoder_L2_of_code11) {
                    int len = Integer.parseInt(value);
                    if (len >= 4 && len <= 55) {
                        enableSymbology(PropertyID.CODE11_LENGTH2, Settings.System.CODE11_LENGTH2, len);
                    } else {
                        showAlertToast(4, 55);
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @SuppressLint("StringFormatMatches")
    private void showAlertToast(int min, int max) {
        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_symbology_length_range), min, max), Toast.LENGTH_LONG).show();
    }
}