package com.ubx.scanwedge.settings.fragments;

import android.app.ActionBar;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.scanwedge.settings.utils.ScannerAdapter;
import com.ubx.database.helper.USettings;

public class SymbologySettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private ScanWedgeApplication mApplication;
    private PreferenceScreen root;
    private ListPreference DPM_DECODE_MODE;
    private int scannerType;
    private int profileId;
    //Australian postal /canadian postal /dutch postal/ japanese postal/korean 3of5/mail mark/uk postal/us4state/us4state fics/us planet/us postnet
    //zebar  Decoder Signature
    //honeywell iata2of5 hk25/ telepen/ codablock F/ grid matrix/Standard 2of5
    //Discrete 2 of 5
    private static String[] symbologyPreferences = new String[] {
            Settings.System.AZTEC_ENABLE,
            Settings.System.C25_ENABLE,
            Settings.System.CODABAR_ENABLE,
            Settings.System.CODE11_ENABLE,
            Settings.System.CODE128_ENABLE,
            Settings.System.CODE39_ENABLE,
            Settings.System.CODE93_ENABLE,
            Settings.System.COMPOSITE_CC_AB_ENABLE,
            /*Settings.System.COMPOSITE_CC_C_ENABLE,
            Settings.System.COMPOSITE_TLC39_ENABLE,*/
            Settings.System.D25_ENABLE,
            Settings.System.DATAMATRIX_ENABLE,
            Settings.System.DOTCODE_ENABLE,
            Settings.System.EAN13_ENABLE,
            Settings.System.EAN8_ENABLE,
            Settings.System.GS1_14_ENABLE,
            Settings.System.GS1_EXP_ENABLE,
            Settings.System.GS1_LIMIT_ENABLE,
            Settings.System.HANXIN_ENABLE,
            Settings.System.I25_ENABLE,
            Settings.System.M25_ENABLE,
            Settings.System.MAXICODE_ENABLE,
            Settings.System.MICROPDF417_ENABLE,
            Settings.System.MICROQRCODE_ENABLE,
            Settings.System.MSI_ENABLE,
            Settings.System.PDF417_ENABLE,
            Settings.System.QRCODE_ENABLE,
            Settings.System.TRIOPTIC_ENABLE,
            Settings.System.UPCA_ENABLE,
            Settings.System.UPCE_ENABLE,
            Settings.System.UPCE1_ENABLE,
            Settings.System.POSTAL_GROUP_TYPE_ENABLE,
            Settings.System.EAN_EXT_ENABLE_2_5_DIGIT,
    };
    private static String[] symbology2DPreferences = new String[] {
            Settings.System.AZTEC_ENABLE,
            Settings.System.COMPOSITE_CC_AB_ENABLE,
            /*Settings.System.COMPOSITE_CC_C_ENABLE,
            Settings.System.COMPOSITE_TLC39_ENABLE,*/
            Settings.System.DATAMATRIX_ENABLE,
            Settings.System.DOTCODE_ENABLE,
            Settings.System.GS1_14_ENABLE,
            Settings.System.GS1_EXP_ENABLE,
            Settings.System.GS1_LIMIT_ENABLE,
            Settings.System.HANXIN_ENABLE,
            Settings.System.MAXICODE_ENABLE,
            Settings.System.MICROPDF417_ENABLE,
            Settings.System.MICROQRCODE_ENABLE,
            Settings.System.PDF417_ENABLE,
            Settings.System.QRCODE_ENABLE,
    };
    private static int[] symbologyNameid = new int[] {
            R.string.decoder_type_aztec,
            R.string.scanner_symbology_chinese_25,
            R.string.scanner_symbology_codabar,
            R.string.scanner_symbology_code11,
            R.string.scanner_symbology_code128,
            R.string.scanner_symbology_code39,
            R.string.scanner_symbology_code93,
            R.string.scanner_symbology_composite,
            /*R.string.scanner_symbology_composite_cc_ab,
            R.string.scanner_symbology_composite_cc_c,
            R.string.scanner_symbology_composite_39,*/
            R.string.scanner_symbology_discrete_25,
            R.string.scanner_symbology_Datamatrix,
            R.string.scanner_symbology_dotcode,
            R.string.scanner_symbology_ean13,
            R.string.scanner_symbology_ean8,
            R.string.scanner_symbology_gs1_databar14,
            R.string.scanner_symbology_gs1_databar_expanded,
            R.string.scanner_symbology_gs1_databar_limited,
            R.string.scanner_symbology_hanxin,
            R.string.scanner_symbology_interleaved_25,
            R.string.scanner_symbology_matrix_25,
            R.string.scanner_symbology_maxicode,
            R.string.scanner_symbology_micropdf417,
            R.string.scanner_symbology_micro_qrcode,
            R.string.scanner_symbology_MSI,
            R.string.scanner_symbology_pdf417,
            R.string.scanner_symbology_qrcode,
            R.string.scanner_symbology_trioptic,
            R.string.scanner_symbology_upc_a,
            R.string.scanner_symbology_upc_e,
            R.string.scanner_symbology_upc_e1,
            R.string.scanner_symbology_postal,
            R.string.scanner_symbology_upc_ena_extensions,
    };
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
        getActivity().setTitle(R.string.scanner_symbology);
        //getActivity().getActionBar().setDisplayShowCustomEnabled(false);
        Bundle args = getArguments();
        scannerType = args != null ? args.getInt("type") : 0;
        profileId = args != null ? args.getInt("profileId") : USettings.Profile.DEFAULT_ID;
        addPreferencesFromResource(R.xml.scanner_settings);
        root = this.getPreferenceScreen();
        mApplication = (ScanWedgeApplication) getActivity().getApplication();
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
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        updateActionBar();
        getActivity().setTitle(R.string.scanner_symbology);
        root = this.getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        for(int i = 0; i < symbologyPreferences.length; i++) {
            Preference symPreference = new Preference(getActivity());
            symPreference.setKey(symbologyPreferences[i]);
            symPreference.setTitle(symbologyNameid[i]);
            if(symbologyPreferences[i].equals(Settings.System.DOTCODE_ENABLE)) {
                symPreference.setEnabled(ScannerAdapter.isSupportPreference(scannerType, Settings.System.DOTCODE_ENABLE));
            } else if(symbologyPreferences[i].equals(Settings.System.MICROQRCODE_ENABLE)) {
                symPreference.setEnabled(ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_ZEBAR_ENGINE_PREFERENCE));
            } else if(ScannerAdapter.isSupportPreference(scannerType, ScannerAdapter.KEY_1D_LINEAR_SCANNER_ENGINE)) {
                for(int j = 0; j < symbology2DPreferences.length; j++) {
                    symPreference.setEnabled(!symbology2DPreferences[j].equals(symbologyPreferences[i]));
                }
            }
            root.addPreference(symPreference);
        }
        if(ScannerAdapter.isSupportPreference(scannerType, Settings.System.DPM_DECODE_MODE)) {
            DPM_DECODE_MODE = new ListPreference(getActivity());
            DPM_DECODE_MODE.setKey(Settings.System.DPM_DECODE_MODE);
            DPM_DECODE_MODE.setTitle(R.string.scanner_dpm_decoding);
            DPM_DECODE_MODE.setEntries(R.array.dpm_decoding_entries);
            DPM_DECODE_MODE.setEntryValues(R.array.code_id_type_values);
            DPM_DECODE_MODE.setOnPreferenceChangeListener(this);
            root.addPreference(DPM_DECODE_MODE);
            int mode = mApplication.getPropertyInt(profileId, Settings.System.DPM_DECODE_MODE, 0);
            DPM_DECODE_MODE.setValue(String.valueOf(mode));
            DPM_DECODE_MODE.setSummary(DPM_DECODE_MODE.getEntry());
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        if(preference == DPM_DECODE_MODE) {
            return false;
        }
        Bundle bundle = new Bundle();
        String key = preference.getKey();
        for(int i = 0; i < symbologyPreferences.length; i++) {
            if(symbologyPreferences[i].equals(key)) {
                bundle.putString("symbologyType", symbologyPreferences[i]);
            }
        }
        bundle.putInt("scanType", scannerType);
        bundle.putInt("profileId", profileId);
        bundle.putBoolean("is-main-page", false);
        startFragment(this, DecoderProperties.class.getCanonicalName(), -1, bundle);
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference == DPM_DECODE_MODE) {
            int idvalue = Integer.valueOf(newValue.toString());
            DPM_DECODE_MODE.setSummary(DPM_DECODE_MODE.getEntries()[idvalue]);
            mApplication.setPropertyInt(profileId, PropertyID.DPM_DECODE_MODE, Settings.System.DPM_DECODE_MODE, idvalue);
        }
        return true;
    }
}
