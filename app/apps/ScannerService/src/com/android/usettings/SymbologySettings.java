package com.android.usettings;

import android.app.ActionBar;
import android.device.provider.Settings;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
// urovo add shenpidong begin 2019-04-08
import android.preference.ListPreference;
import android.device.ScanManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.device.scanner.configuration.PropertyID;
// urovo add shenpidong end 2019-04-08
// urovo add by shenpidong begin 2020-04-14
import android.os.Build;
// urovo add by shenpidong end 2020-04-14

public class SymbologySettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    public static final int SET_Aztec = 1;
    public static final int SET_Codabar = 2;
    
    public static final int SET_Code128 = 3;
    public static final int SET_Gs1_128 = 4;
    public static final int SET_Code39 = 5;
    public static final int SET_Code93 = 6;
    public static final int SET_Composite_cc_ab = 7;
    public static final int SET_Composite_cc_cc = 8;
    public static final int SET_Datamatrix = 9;
    
    public static final int SET_Ean13 = 10;
    public static final int SET_Gs1_databar14 = 11;
    public static final int SET_Gs1_databar_expanded = 12;
    public static final int SET_Gs1_databar_limited = 13;
    public static final int SET_Interleaved25 = 14;
    public static final int SET_Matrix_25 = 15;
    public static final int SET_Maxicode = 16;
    public static final int SET_Micropdf417 = 17;
    public static final int SET_MSI = 18;
    public static final int SET_Code32 = 19;
    public static final int SET_Pdf47 = 20;
    public static final int SET_Postal_australian = 21;
    public static final int SET_Postal_japan = 22;
    public static final int SET_Postal_kix = 23;
    public static final int SET_Postal_royal_mail = 24;
    public static final int SET_Postal_planet = 25;
    public static final int SET_Postal_postnet = 26;
    public static final int SET_Postal_usps4_state = 27;
    public static final int SET_Qrcode = 28;
    public static final int SET_Discrete_25 = 29;
    public static final int SET_Trioptic = 30;
    public static final int SET_Upc_a = 31;
    public static final int SET_Upc_e = 32;
    public static final int SET_Upc_ena_extensions = 33;
    public static final int SET_Ean8 = 34;
    public static final int SET_Upc_e1 = 35;
    public static final int SET_Code11 = 36;
    public static final int SET_Chinese25 = 37;
    public static final int SET_HANXIN = 38;
    public static final int SET_Composite39 = 39;
    public static final int SET_Postal = 40;
    public static final int SET_MicroQR = 41;
    public static final int SET_OCR = 42;
    public static final int SET_DotCode = 43;
    
    private PreferenceScreen root;
    private Preference mAztec;
    private Preference mCodabar;
    
    private Preference mCode128;
    private Preference mGs1_128;
    private Preference mCode39;
    private Preference mCode93;
    private Preference mComposite_cc_ab;
    private Preference mComposite_cc_cc;
    private Preference mDatamatrix;
    
    private Preference mEan13;
    private Preference mEan8;
    private Preference mGs1_databar14;
    private Preference mGs1_databar_expanded;
    private Preference mGs1_databar_limited;
    private Preference mInterleaved25;
    private Preference mMatrix_25;
    private Preference mMaxicode;
    private Preference mMicropdf417;
    private Preference mMSI;
    private Preference mCode32;
    private Preference mPdf47;
    //private Preference mPostal_australian;
    //private Preference mPostal_japan;
    //private Preference mPostal_kix;
    //private Preference mPostal_royal_mail;
    //private Preference mPostal_planet;
    //private Preference mPostal_postnet;
    //private Preference mPostal_usps4_state;
    private Preference mQrcode;
    private Preference mMicroQrcode;
    private Preference mDiscrete_25;
    private Preference mTrioptic;
    private Preference mUpc_a;
    private Preference mUpc_e;
    private Preference mUpc_ena_extensions;
    private Preference mUpc_e1;
    private Preference mChinese25;
    private Preference mCode11;
    private Preference mHanXin;
    private Preference mComposite39;
    private Preference mPostal;
    private Preference mOCR;
    private Preference mDotCode;
	// urovo add shenpidong begin 2019-04-08
	private ListPreference mPostalList;
	private ListPreference DPM_DECODE_MODE;
    private ScanManager mScanManager = null;
    private String[] postal_sym_entries = null;
    private String[] postal_sym_values = null;
	// urovo add shenpidong end 2019-04-08
    private int scannerType;
    // add by tao.he for actibar sync issue
    private void updateActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowCustomEnabled(false);
        }
        getActivity().setTitle(R.string.scanner_symbology);
    }
    // end add
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        root = this.getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        Bundle args = getArguments();
        scannerType = args != null ? args.getInt("type") : 0;
        addPreferencesFromResource(R.xml.symbology_settings);
        root = this.getPreferenceScreen();
		// urovo add shenpidong begin 2019-04-08
        mScanManager = new ScanManager();
	    postal_sym_entries = getActivity().getResources().getStringArray(R.array.postal_sym_entries);
	    postal_sym_values = getActivity().getResources().getStringArray(R.array.postal_sym_values);
		// urovo add shenpidong end 2019-04-08
        
        mAztec = (Preference) getPreferenceScreen().findPreference("scanner_symbology_aztec");
        mCodabar = (Preference) getPreferenceScreen().findPreference("scanner_symbology_codabar");
        mCode128 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_code128");
        mGs1_128 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_gs1_128");
        mCode39 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_code39");
        mCode93 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_code93");
        mComposite_cc_ab = (Preference) getPreferenceScreen().findPreference("scanner_symbology_composite_cc_ab");
        mComposite_cc_cc = (Preference) getPreferenceScreen().findPreference("scanner_symbology_composite_cc_c");
        mPostal = (Preference) getPreferenceScreen().findPreference("scanner_symbology_postal");
		// urovo add shenpidong begin 2019-04-08
        mPostalList = (ListPreference) getPreferenceScreen().findPreference("sym_post_config");
		// urovo add shenpidong end 2019-04-08
        mComposite39 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_composite_39");
        mHanXin = (Preference) getPreferenceScreen().findPreference("scanner_symbology_hanxin");
        mDatamatrix = (Preference) getPreferenceScreen().findPreference("scanner_symbology_Datamatrix");
        mEan13 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_ean13");
        mEan8 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_ean8");
        mGs1_databar14 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_gs1_databar14");
        mGs1_databar_expanded = (Preference) getPreferenceScreen().findPreference("scanner_symbology_gs1_databar_expanded");
        mGs1_databar_limited = (Preference) getPreferenceScreen().findPreference("scanner_symbology_gs1_databar_limited");
        mInterleaved25 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_interleaved_25");
        mMatrix_25 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_matrix_25");
        mMaxicode = (Preference) getPreferenceScreen().findPreference("scanner_symbology_maxicode");
        mMicropdf417 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_micropdf417");
        mMSI = (Preference) getPreferenceScreen().findPreference("scanner_symbology_MSI");
        mCode32 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_code32");
        mPdf47 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_pdf47");
        /*
        mPostal_australian = (Preference) getPreferenceScreen().findPreference("scanner_symbology_postal_australian");
        mPostal_japan = (Preference) getPreferenceScreen().findPreference("scanner_symbology_postal_japan");
        mPostal_kix = (Preference) getPreferenceScreen().findPreference("scanner_symbology_postal_kix");
        mPostal_royal_mail = (Preference) getPreferenceScreen().findPreference("scanner_symbology_postal_royal_mail");
        mPostal_planet = (Preference) getPreferenceScreen().findPreference("scanner_symbology_postal_planet");
        mPostal_postnet = (Preference) getPreferenceScreen().findPreference("scanner_symbology_postal_postnet");
        mPostal_usps4_state = (Preference) getPreferenceScreen().findPreference("scanner_symbology_postal_usps4_state");  */
        mQrcode = (Preference) getPreferenceScreen().findPreference("scanner_symbology_qrcode");
        mMicroQrcode = (Preference) getPreferenceScreen().findPreference("scanner_symbology_microqrcode");
        mDiscrete_25 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_discrete_25");
        mTrioptic = (Preference) getPreferenceScreen().findPreference("scanner_symbology_trioptic");
        mUpc_a = (Preference) getPreferenceScreen().findPreference("scanner_symbology_upc_a");
        mUpc_e = (Preference) getPreferenceScreen().findPreference("scanner_symbology_upc_e");
        mUpc_ena_extensions = (Preference) getPreferenceScreen().findPreference("scanner_symbology_upc_ena_extensions");
        mUpc_e1 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_upc_e1");
        mChinese25 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_chinese_25");
        mCode11 = (Preference) getPreferenceScreen().findPreference("scanner_symbology_code11");
        mOCR = (Preference) getPreferenceScreen().findPreference("ocr_symbology");
        mDotCode = (Preference) getPreferenceScreen().findPreference("dotcode_symbology");
        DPM_DECODE_MODE = (ListPreference) getPreferenceScreen().findPreference(Settings.System.DPM_DECODE_MODE);
		// urovo add shenpidong begin 2019-04-08
        if(mPostalList != null) {
            mPostalList.setOnPreferenceChangeListener(this);
        }
		// urovo add shenpidong end 2019-04-08
        if(scannerType == 0) {
            root.removeAll();
        }
        if(scannerType == 3 || scannerType == 1) {
            root.removePreference(mUpc_e1);
        }
        if(scannerType == 2 || scannerType == 3 || scannerType == 1) {
            root.removePreference(mAztec);
            root.removePreference(mDatamatrix);
            root.removePreference(mMaxicode);
            root.removePreference(mMicropdf417);
            root.removePreference(mPdf47);
            /*
            root.removePreference(mPostal_australian);
            root.removePreference(mPostal_japan);
            root.removePreference(mPostal_kix);
            root.removePreference(mPostal_royal_mail);
            root.removePreference(mPostal_planet);
            root.removePreference(mPostal_postnet);
            root.removePreference(mPostal_usps4_state);  */
            root.removePreference(mQrcode);
            root.removePreference(mMicroQrcode);
            root.removePreference(mPostal);
            root.removePreference(mComposite39);
            root.removePreference(mComposite_cc_ab);
            root.removePreference(mComposite_cc_cc);
            root.removePreference(mHanXin);
        } else if(scannerType == 6) {
            if(mMicroQrcode != null)
            root.removePreference(mMicroQrcode);
	    // urovo add shenpidong begin 2019-09-27
        } else if(false && scannerType == 5) { // scannerType=5 is 6703
            if(mMicroQrcode != null)
            root.removePreference(mMicroQrcode);
            if(mCode11 != null)
            root.removePreference(mCode11);
            root.removePreference(mComposite39);
            root.removePreference(mComposite_cc_ab);
            root.removePreference(mComposite_cc_cc);
            root.removePreference(mGs1_databar14 );
            root.removePreference(mGs1_databar_expanded);
            root.removePreference(mGs1_databar_limited);
            root.removePreference(mCode32);
            root.removePreference(mTrioptic);
            root.removePreference(mPostal);
        }
	    // urovo add shenpidong end 2019-09-27
	// urovo add shenpidong begin 2019-04-08
        if(scannerType == 5 || scannerType == 8 || scannerType == 11) {
            root.removePreference(mPostal);
	    }else{
            root.removePreference(mPostalList);
        }
	// urovo add shenpidong end 2019-04-08
	// urovo add shenpidong begin 2019-09-02
	// urovo modified by shenpidong begin 2020-04-14
        if(scannerType != 5 && scannerType != 8 && scannerType != 11 && scannerType != 12 && scannerType != 15&& scannerType != 13) {
	    boolean ocr = Build.PROJECT.equals("SQ45") && Build.PWV_CUSTOM_CUSTOM.equals("XX");
            if(mOCR != null && !ocr){
                root.removePreference(mOCR);
            }
        }
	// urovo modified by shenpidong end 2020-04-14
		if(scannerType != 5 && scannerType != 8 && scannerType != 11 && scannerType != 13 && scannerType != 9 && scannerType != 15) {
            if(mDotCode != null) {
                root.removePreference(mDotCode);
            }
        }
        if(scannerType != 5 && scannerType != 8&& scannerType != 15&& scannerType != 4) {
            if(DPM_DECODE_MODE != null) {
                root.removePreference(DPM_DECODE_MODE);
            }
        } else {
            if(DPM_DECODE_MODE != null) {
                if(scannerType == 4) {//4750
                    DPM_DECODE_MODE.setEntries(R.array.scanner_dpm_entries);
                }
                DPM_DECODE_MODE.setOnPreferenceChangeListener(this);
                int[] index = new int[1];
                int[] value = new int[1];
                index[0] = PropertyID.DPM_DECODE_MODE;
                mScanManager.getPropertyInts(index, value);
                DPM_DECODE_MODE.setValue(String.valueOf(value[0]));
                DPM_DECODE_MODE.setSummary(DPM_DECODE_MODE.getEntry());
            }
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
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        // add by tao.he for actibar sync issue
        updateActionBar();
        // end add
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        
        Bundle bundle = new Bundle();
        if (preference == mAztec) {
            bundle.putInt("type", SET_Aztec);
        } else if (preference == mCodabar) {
            bundle.putInt("type", SET_Codabar);
        } else if (preference == mCode128) {
            bundle.putInt("type", SET_Code128);
        } else if (preference == mGs1_128) {
            bundle.putInt("type", SET_Gs1_128);
        } else if (preference == mCode39) {
            bundle.putInt("type", SET_Code39);
        } else if (preference == mCode93) {
            bundle.putInt("type", SET_Code93);
        }  else if (preference == mComposite_cc_ab) {
            bundle.putInt("type", SET_Composite_cc_ab);
        }  else if (preference == mComposite_cc_cc) {
            bundle.putInt("type", SET_Composite_cc_cc);
        }  else if (preference == mComposite39) {
            bundle.putInt("type", SET_Composite39);
        }  else if (preference == mDatamatrix) {
            bundle.putInt("type", SET_Datamatrix);
        } else if (preference == mEan13) {
            bundle.putInt("type", SET_Ean13);
        } else if (preference == mGs1_databar14) {
            bundle.putInt("type", SET_Gs1_databar14);
        } else if (preference == mGs1_databar_expanded) {
            bundle.putInt("type", SET_Gs1_databar_expanded);
        } else if (preference == mGs1_databar_limited) {
            bundle.putInt("type", SET_Gs1_databar_limited);
        } else if (preference == mInterleaved25) {
            bundle.putInt("type", SET_Interleaved25);
        } else if (preference == mMatrix_25) {
            bundle.putInt("type", SET_Matrix_25);
        } else if (preference == mMaxicode) {
            bundle.putInt("type", SET_Maxicode);
        } else if (preference == mMicropdf417) {
            bundle.putInt("type", SET_Micropdf417);
        } else if (preference == mMSI) {
            bundle.putInt("type", SET_MSI);
        } else if (preference == mCode32) {
            bundle.putInt("type", SET_Code32);
        } else if (preference == mPdf47) {
            bundle.putInt("type", SET_Pdf47);
        } 
        else if (preference == mPostal) {
            bundle.putInt("type", SET_Postal);
        } /* else if (preference == mPostal_australian) {
            bundle.putInt("type", SET_Postal_australian);
        } else if (preference == mPostal_japan) {
            bundle.putInt("type", SET_Postal_japan);
        } else if (preference == mPostal_kix) {
            bundle.putInt("type", SET_Postal_kix);
        } else if (preference == mPostal_royal_mail) {
            bundle.putInt("type", SET_Postal_royal_mail);
        } else if (preference == mPostal_planet) {
            bundle.putInt("type", SET_Postal_planet);
        } else if (preference == mPostal_postnet) {
            bundle.putInt("type", SET_Postal_postnet);
        } else if (preference == mPostal_usps4_state) {
            bundle.putInt("type", SET_Postal_usps4_state);
        } */ else if (preference == mQrcode) {
            bundle.putInt("type", SET_Qrcode);
        } else if (preference == mMicroQrcode) {
            bundle.putInt("type", SET_MicroQR);
        } else if (preference == mDiscrete_25) {
            bundle.putInt("type", SET_Discrete_25);
        } else if (preference == mTrioptic) {
            bundle.putInt("type", SET_Trioptic);
        } else if (preference == mUpc_a) {
            bundle.putInt("type", SET_Upc_a);
        } else if (preference == mUpc_e) {
            bundle.putInt("type", SET_Upc_e);
        } else if (preference == mUpc_ena_extensions) {
            bundle.putInt("type", SET_Upc_ena_extensions);
        } else if (preference == mEan8) {
            bundle.putInt("type", SET_Ean8);
        } else if (preference == mChinese25) {
            bundle.putInt("type", SET_Chinese25);
        } else if (preference == mCode11) {
            bundle.putInt("type", SET_Code11);
        } else if (preference == mUpc_e1) {
            bundle.putInt("type", SET_Upc_e1);
        } else if (preference == mHanXin) {
            bundle.putInt("type", SET_HANXIN);
		// urovo add shenpidong begin 2019-04-08
        } else if (preference == mPostalList) {
            bundle.putInt("type", SET_Postal);
	    	return false;
		// urovo add shenpidong end 2019-04-08
        } else if (preference == mOCR) {
            bundle.putInt("type", SET_OCR);
        } else if (preference == mDotCode) {
            bundle.putInt("type", SET_DotCode);
        } else if(preference == DPM_DECODE_MODE) {
            return false;
        }
        bundle.putInt("scanType", scannerType);
        startFragment(this, DecoderProperties.class.getCanonicalName(), -1, bundle); 
        return false;
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference == DPM_DECODE_MODE) {
            int idvalue = Integer.valueOf(newValue.toString());
            int[] index = new int[1];
            int[] value = new int[1];
            index[0] = PropertyID.DPM_DECODE_MODE;
            value[0] = idvalue;
            mScanManager.setPropertyInts(index, value);
			DPM_DECODE_MODE.setSummary(DPM_DECODE_MODE.getEntries()[idvalue]);
        } else if(preference == mPostalList){
            int idvalue = Integer.parseInt((String) newValue);
            mPostalList.setSummary(mPostalList.getEntries()[idvalue]);
            int[] index = new int[1];
            int[] value = new int[1];
            index[0] = PropertyID.POSTAL_GROUP_TYPE_ENABLE;
            value[0] = idvalue;
            if(mScanManager != null) {
                mScanManager.setPropertyInts(index, value);
		    } 
        }
	    return true;
    }
}
