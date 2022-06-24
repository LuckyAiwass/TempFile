package com.android.server.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import android.content.Context;
import android.util.SparseArray;

import com.android.server.ScanService;

import android.device.scanner.configuration.Constants.Symbology;
import android.device.scanner.configuration.PropertyID;

import com.android.server.ScanServiceWrapper;

import android.util.Log;//juzhitao
import android.os.Build;
import android.os.SystemProperties;//juzhitao
import com.ubx.propertyparser.Property;
public abstract class Scanner {

    private static final String TAG = "Scanner";
    protected int mScannerType;
    protected int mTimeout;
    protected ScanServiceWrapper mScanService;
    protected Context mContext;
    protected static boolean DEBUG = true;
    protected final static int RESERVED_VALUE = -1;
    protected final static int SPECIAL_VALUE = 10000;
    public static final int CAMERA_MODE_PREVIEW = 1;
    public static final int CAMERA_MODE_QUIT = 2;
    public static final int CAMERA_MODE_DECODE = 3;
    protected SparseArray<Integer> mPropIndexHashMap = new SparseArray<Integer>();   //scanner Engine internal
    protected List<Property> advPropertyLists= new  ArrayList<Property>();
    protected byte[] aimCodeId = new byte[3];
    protected long laserTriggerTime;//按下按键触发出光持续时间
    protected long decodeSessionTime;//解码库解码时间
    public int getScannerType() {
        return mScannerType;
    }

    public abstract void setDefaults();

    public abstract boolean open();

    public abstract void close();

    public abstract void startDecode(int timeout);

    public abstract void stopDecode();

    public abstract void openPhoneMode();

    public abstract void closePhoneMode();

    public abstract int setProperties(SparseArray<Integer> property);

    protected abstract void release();

    public abstract boolean lockHwTriggler(boolean lock);

    protected void sendBroadcast(byte[] barocode, int barcodeType, int barcodelen) {
        if (mScanService != null) {
            mScanService.SendBroadcast(barocode, (byte) barcodeType, barcodelen, aimCodeId, laserTriggerTime, decodeSessionTime);
            if (Build.PROJECT.equals("SQ53C")) {
                SystemProperties.set("persist.sys.urv.reset.scanner", "0");//juzhitao		
            }
        }
    }
    protected void receivedMultipleDecodedData(final List<DecodeData> decodeDataArray) {
        if (mScanService != null) {
            mScanService.receivedMultipleDecodedData(decodeDataArray);
        }
    }
    public boolean softTrigger(int on, int timeout) {
        // TODO Auto-generated method stub
        if (0 == on) {
            stopDecode();

        } else if (1 == on) {
            startDecode(timeout);
        }
        return true;
    }

    public boolean isPropertySupported(int prop) {
        // TODO Auto-generated method stub
        if(PropertyID.DEC_EachImageAttempt_TIME == prop ||PropertyID.DEC_DECODE_DELAY == prop || PropertyID.POSTAL_GROUP_TYPE_ENABLE == prop || PropertyID.DEC_DECODE_DEBUG_MODE == prop) {
            return true;
        }
        if(PropertyID.LOW_CONTRAST_IMPROVED == prop || PropertyID.LOW_CONTRAST_IMPROVED_ALGORITHM == prop || PropertyID.CODE_ISBT_Concatenation_MODE == prop 
         || PropertyID.SEND_TOKENS_OPTION == prop || PropertyID.FUZZY_1D_PROCESSING == prop) {
            return true;
        }
         if(PropertyID.EAN13_SEND_CHECK == prop ||PropertyID.EAN8_SEND_CHECK == prop || PropertyID.LOW_POWER_SLEEP_MODE == prop || PropertyID.C128_OUT_OF_SPEC== prop
            || PropertyID.GRIDMATRIX_ENABLED== prop || PropertyID.QR_WITHOUT_QZ== prop || PropertyID.QR_NON_SQUARE_MODULES== prop) {
            return true;
        }
        if(PropertyID.DEC_Multiple_Decode_INTERVAL == prop || PropertyID.DPM_DECODE_MODE == prop ||PropertyID.DATAMATRIX_SYMBOL_SIZE == prop || PropertyID.MAXICODE_SYMBOL_SIZE == prop || PropertyID.QRCODE_SYMBOL_SIZE == prop|| PropertyID.AZTEC_SYMBOL_SIZE == prop || PropertyID.LABEL_SEPARATOR_ENABLE == prop) {
            return true;
        }
        int realMappedIndex = mPropIndexHashMap.get(prop, SPECIAL_VALUE);
        if (realMappedIndex == RESERVED_VALUE) {
            return false;
        }
        return true;
    }

    public int getSymbologyEnableIndex(int type) {

        if (type - 1 < 0 || (type - 1) >= KEY_BARCODE_ENABLE_INDEX.length) {
            return -1;
        }
        return KEY_BARCODE_ENABLE_INDEX[type - 1];
    }

    /**
     * 44 type == VALUE_SYMBOLOGY length
     */
    public static final int[] KEY_BARCODE_ENABLE_INDEX = {
            PropertyID.CODE39_ENABLE,
            PropertyID.D25_ENABLE,
            PropertyID.M25_ENABLE,
            PropertyID.I25_ENABLE,
            PropertyID.CODABAR_ENABLE,
            -1,
            PropertyID.CODE93_ENABLE,
            PropertyID.CODE128_ENABLE,
            PropertyID.UPCA_ENABLE,
            PropertyID.UPCE_ENABLE,
            PropertyID.EAN13_ENABLE,
            PropertyID.EAN8_ENABLE,
            -1,
            PropertyID.MSI_ENABLE,
            -1,
            -1,
            PropertyID.GS1_14_ENABLE,
            PropertyID.GS1_LIMIT_ENABLE,
            PropertyID.GS1_EXP_ENABLE,
            -1,
            -1,
            PropertyID.PDF417_ENABLE,
            PropertyID.DATAMATRIX_ENABLE,
            PropertyID.MAXICODE_ENABLE,
            PropertyID.TRIOPTIC_ENABLE,
            PropertyID.CODE32_ENABLE,
            -1,
            -1,
            PropertyID.MICROPDF417_ENABLE,
            -1,
            PropertyID.QRCODE_ENABLE,
            PropertyID.AZTEC_ENABLE,
            -1,
            PropertyID.US_PLANET_ENABLE,
            PropertyID.US_POSTNET_ENABLE,
            PropertyID.USPS_4STATE_ENABLE,
            PropertyID.UPU_FICS_ENABLE,
            PropertyID.ROYAL_MAIL_ENABLE,
            PropertyID.AUSTRALIAN_POST_ENABLE,
            PropertyID.KIX_CODE_ENABLE,
            PropertyID.JAPANESE_POST_ENABLE,
            PropertyID.CODE128_GS1_ENABLE,
            PropertyID.COMPOSITE_CC_C_ENABLE,
            PropertyID.COMPOSITE_CC_AB_ENABLE,
            PropertyID.C25_ENABLE,
            PropertyID.CODE11_ENABLE,
            PropertyID.UPCE1_ENABLE,
            PropertyID.COMPOSITE_TLC39_ENABLE,
            PropertyID.HANXIN_ENABLE
    };

    /**
     * 44 == se45 se955scanner index_symbology array length
     * Correspondence with Symbology class
     */
    public static final int[] VALUE_SYMBOLOGY = {
            Symbology.CODE39,
            Symbology.DISCRETE25,
            Symbology.MATRIX25,
            Symbology.INTERLEAVED25,
            Symbology.CODABAR,
            Symbology.RESERVED_6,
            Symbology.CODE93,
            Symbology.CODE128,
            Symbology.UPCA,
            Symbology.UPCE,
            Symbology.EAN13,
            Symbology.EAN8,
            Symbology.RESERVED_13,
            Symbology.MSI,
            Symbology.RESERVED_15,
            Symbology.RESERVED_16,
            Symbology.GS1_14,
            Symbology.GS1_LIMIT,
            Symbology.GS1_EXP,
            Symbology.RESERVED_20,
            Symbology.RESERVED_21,
            Symbology.PDF417,
            Symbology.DATAMATRIX,
            Symbology.MAXICODE,
            Symbology.TRIOPTIC,
            Symbology.CODE32,
            Symbology.RESERVED_27,
            Symbology.RESERVED_28,
            Symbology.MICROPDF417,
            Symbology.RESERVED_30,
            Symbology.QRCODE,
            Symbology.AZTEC,
            Symbology.RESERVED_33,
            Symbology.POSTAL_PLANET,
            Symbology.POSTAL_POSTNET,
            Symbology.POSTAL_4STATE,
            Symbology.POSTAL_UPUFICS,
            Symbology.POSTAL_ROYALMAIL,
            Symbology.POSTAL_AUSTRALIAN,
            Symbology.POSTAL_KIX,
            Symbology.POSTAL_JAPAN,
            Symbology.GS1_128,
            Symbology.COMPOSITE_CC_C,
            Symbology.COMPOSITE_CC_AB,
            Symbology.CHINESE25,
            Symbology.CODE11,
            Symbology.UPCE1,
            Symbology.COMPOSITE_TLC39,
            Symbology.HANXIN,
            Symbology.DOTCODE

    };
    public final int[] INTERNAL_PROPERTY_INDEX = {
            PropertyID.IMAGE_EXPOSURE_MODE,
            PropertyID.IMAGE_FIXED_EXPOSURE,
            PropertyID.IMAGE_PICKLIST_MODE,
            PropertyID.IMAGE_ONE_D_INVERSE,
            PropertyID.LASER_ON_TIME,
            PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL,
            PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL,
            PropertyID.FUZZY_1D_PROCESSING,
            PropertyID.MULTI_DECODE_MODE,
            PropertyID.BAR_CODES_TO_READ,
            PropertyID.FULL_READ_MODE,
            PropertyID.CODE39_ENABLE,        //Code39 definitions
            PropertyID.CODE39_ENABLE_CHECK,
            PropertyID.CODE39_SEND_CHECK,
            PropertyID.CODE39_FULL_ASCII,
            PropertyID.CODE39_LENGTH1,
            PropertyID.CODE39_LENGTH2,
            PropertyID.TRIOPTIC_ENABLE,      //trioptic
            PropertyID.CODE32_ENABLE,      //code 32 also see pharmacode 39
            PropertyID.CODE32_SEND_START,
            PropertyID.C25_ENABLE,
            PropertyID.D25_ENABLE,           //discrete 2/5
            PropertyID.D25_LENGTH1,
            PropertyID.D25_LENGTH2,
            PropertyID.M25_ENABLE,           //matrix 2/5
            PropertyID.CODE11_ENABLE,
            PropertyID.CODE11_ENABLE_CHECK,
            PropertyID.CODE11_SEND_CHECK,
            PropertyID.CODE11_LENGTH1,
            PropertyID.CODE11_LENGTH2,
            PropertyID.I25_ENABLE,           //interleaved 2/5
            PropertyID.I25_ENABLE_CHECK,
            PropertyID.I25_SEND_CHECK,
            PropertyID.I25_LENGTH1,
            PropertyID.I25_LENGTH2,
            PropertyID.I25_TO_EAN13,
            PropertyID.CODABAR_ENABLE,           //codebar
            PropertyID.CODABAR_NOTIS,
            PropertyID.CODABAR_CLSI,
            PropertyID.CODABAR_LENGTH1,
            PropertyID.CODABAR_LENGTH2,
            PropertyID.CODE93_ENABLE,        //code 93
            PropertyID.CODE93_LENGTH1,
            PropertyID.CODE93_LENGTH2,
            PropertyID.CODE128_ENABLE,       //code128
            PropertyID.CODE128_LENGTH1,
            PropertyID.CODE128_LENGTH2,
            PropertyID.CODE_ISBT_128,
            PropertyID.CODE128_GS1_ENABLE,       //gs1-128
            PropertyID.UPCA_ENABLE,          //uspa
            PropertyID.UPCA_SEND_CHECK,
            PropertyID.UPCA_SEND_SYS,
            PropertyID.UPCA_TO_EAN13,
            PropertyID.UPCE_ENABLE,      //uspe
            PropertyID.UPCE_SEND_CHECK,
            PropertyID.UPCE_SEND_SYS,
            PropertyID.UPCE_TO_UPCA,
            PropertyID.UPCE1_ENABLE,
            PropertyID.UPCE1_SEND_CHECK,
            PropertyID.UPCE1_SEND_SYS,
            PropertyID.UPCE1_TO_UPCA,
            PropertyID.EAN13_ENABLE,         //ean13
            //PropertyID.EAN13_SEND_CHECK,
            PropertyID.EAN13_BOOKLANDEAN,
            PropertyID.EAN13_BOOKLAND_FORMAT,
            PropertyID.EAN8_ENABLE,          //ean8
            //PropertyID.EAN8_SEND_CHECK,
            PropertyID.EAN8_TO_EAN13,
            PropertyID.EAN_EXT_ENABLE_2_5_DIGIT,   //UPC/EAN Extensions definitions
            PropertyID.UPC_EAN_SECURITY_LEVEL,
            PropertyID.UCC_COUPON_EXT_CODE,
            PropertyID.MSI_ENABLE,               //msi
            PropertyID.MSI_REQUIRE_2_CHECK,
            PropertyID.MSI_SEND_CHECK,
            PropertyID.MSI_CHECK_2_MOD_11,
            PropertyID.MSI_LENGTH1,
            PropertyID.MSI_LENGTH2,
            PropertyID.GS1_14_ENABLE,            //rss
            PropertyID.GS1_14_TO_UPC_EAN,
            PropertyID.GS1_LIMIT_ENABLE,         //rss limit
            PropertyID.GS1_EXP_ENABLE,           //rss exp
            PropertyID.GS1_EXP_LENGTH1,
            PropertyID.GS1_EXP_LENGTH2,
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
            PropertyID.PDF417_ENABLE,        //pdf417
            PropertyID.MICROPDF417_ENABLE,       //micro pdf417
            PropertyID.COMPOSITE_CC_AB_ENABLE,     //composite-cc_ab
            PropertyID.COMPOSITE_CC_C_ENABLE,    //composite-cc_c
            PropertyID.COMPOSITE_TLC39_ENABLE,
            PropertyID.HANXIN_ENABLE,
            PropertyID.HANXIN_INVERSE,
            PropertyID.DATAMATRIX_ENABLE,        //datamatrix
            PropertyID.DATAMATRIX_LENGTH1,
            PropertyID.DATAMATRIX_LENGTH2,
            PropertyID.DATAMATRIX_INVERSE,
            PropertyID.MAXICODE_ENABLE,          //maxicode
            PropertyID.QRCODE_ENABLE,            //qrcode
            PropertyID.QRCODE_INVERSE,
            PropertyID.MICROQRCODE_ENABLE,
            PropertyID.AZTEC_ENABLE,       //aztec code
            PropertyID.AZTEC_INVERSE,
            PropertyID.DEC_2D_LIGHTS_MODE,
            PropertyID.DEC_2D_CENTERING_ENABLE,
            PropertyID.DEC_2D_CENTERING_MODE,
            PropertyID.DEC_2D_WINDOW_UPPER_LX,
            PropertyID.DEC_2D_WINDOW_UPPER_LY,
            PropertyID.DEC_2D_WINDOW_LOWER_RX,
            PropertyID.DEC_2D_WINDOW_LOWER_RY,
            PropertyID.DEC_2D_DEBUG_WINDOW_ENABLE,
            PropertyID.DEC_ES_EXPOSURE_METHOD,
            PropertyID.DEC_ES_TARGET_VALUE,
            PropertyID.DEC_ES_TARGET_PERCENTILE,
            PropertyID.DEC_ES_TARGET_ACCEPT_GAP,
            PropertyID.DEC_ES_MAX_EXP,
            PropertyID.DEC_ES_MAX_GAIN,
            PropertyID.DEC_ES_FRAME_RATE,
            PropertyID.DEC_ES_CONFORM_IMAGE,
            PropertyID.DEC_ES_CONFORM_TRIES,
            PropertyID.DEC_ES_SPECULAR_EXCLUSION,
            PropertyID.DEC_ES_SPECULAR_SAT,
            PropertyID.DEC_ES_SPECULAR_LIMIT,
            PropertyID.DEC_ES_FIXED_GAIN,
            PropertyID.DEC_ES_FIXED_FRAME_RATE,
            PropertyID.DEC_ILLUM_POWER_LEVEL,
            PropertyID.DEC_PICKLIST_AIM_MODE,
            PropertyID.DEC_PICKLIST_AIM_DELAY,
            PropertyID.DEC_MaxMultiRead_COUNT,
            PropertyID.DEC_Multiple_Decode_TIMEOUT,
            PropertyID.DEC_Multiple_Decode_INTERVAL, /* urovo tao.he add, 20190430*/
            PropertyID.DEC_Multiple_Decode_MODE,
            PropertyID.DEC_OCR_MODE,
            PropertyID.DEC_OCR_TEMPLATE,
            PropertyID.TRANSMIT_CODE_ID,
            PropertyID.DOTCODE_ENABLE,
            PropertyID.LINEAR_1D_QUIET_ZONE_LEVEL,
            PropertyID.CODE39_Quiet_Zone,
            PropertyID.CODE39_START_STOP,
            PropertyID.CODE39_SECURITY_LEVEL,
            PropertyID.M25_SEND_CHECK,
            PropertyID.M25_LENGTH1,
            PropertyID.M25_LENGTH2,
            PropertyID.I25_QUIET_ZONE,
            PropertyID.I25_SECURITY_LEVEL,
            PropertyID.CODABAR_ENABLE_CHECK,
            PropertyID.CODABAR_SEND_CHECK,
            PropertyID.CODABAR_SEND_START,
            PropertyID.CODABAR_CONCATENATE,
            PropertyID.CODE128_REDUCED_QUIET_ZONE,
            PropertyID.CODE128_CHECK_ISBT_TABLE,
            PropertyID.CODE_ISBT_Concatenation_MODE,
            PropertyID.CODE128_SECURITY_LEVEL,
            PropertyID.CODE128_IGNORE_FNC4,
            PropertyID.UCC_REDUCED_QUIET_ZONE,
            PropertyID.UCC_COUPON_EXT_REPORT_MODE,
            PropertyID.UCC_EAN_ZERO_EXTEND,
            PropertyID.UCC_EAN_SUPPLEMENTAL_MODE,
            PropertyID.GS1_LIMIT_Security_Level,
            PropertyID.COMPOSITE_UPC_MODE,
            PropertyID.POSTAL_GROUP_TYPE_ENABLE,
            PropertyID.KOREA_POST_ENABLE,
            PropertyID.Canadian_POSTAL_ENABLE,

    };

    //保存预览图片
    public enum SaveMode {
        NOTSAVE(0), SAVEDECODESUCCESSALLBMP(1),
        SAVEDECODESUCCESSLASTBMP(2), SAVEPREVIEWALLBMP(3),
        SAVEPREVIEWLASTBMP(4);
        private int mVal;

        private SaveMode(int paramInt) {
            this.mVal = paramInt;
        }

        public int getVal() {
            return this.mVal;
        }
    }

    //N6603 SPECIAL CONFIG
    protected int DEC_2D_LIGHTS_MODE = RESERVED_VALUE;
    protected int DEC_2D_CENTERING_ENABLE = RESERVED_VALUE;
    protected int DEC_2D_CENTERING_MODE = RESERVED_VALUE;
    protected int DEC_2D_WINDOW_UPPER_LX = RESERVED_VALUE;
    protected int DEC_2D_WINDOW_UPPER_LY = RESERVED_VALUE;
    protected int DEC_2D_WINDOW_LOWER_RX = RESERVED_VALUE;
    protected int DEC_2D_WINDOW_LOWER_RY = RESERVED_VALUE;
    protected int DEC_2D_DEBUG_WINDOW_ENABLE = RESERVED_VALUE;
    //OCR　config
    protected int DEC_OCR_MODE = RESERVED_VALUE;
    protected int DEC_OCR_TEMPLATE = RESERVED_VALUE;
    protected int DEC_OCR_USER_TEMPLATE = RESERVED_VALUE;


    protected int DEC_ES_EXPOSURE_METHOD = RESERVED_VALUE;   // Auto Exposure Method
    protected int DEC_ES_TARGET_VALUE = RESERVED_VALUE;      // Target White Value
    protected int DEC_ES_TARGET_PERCENTILE = RESERVED_VALUE; // Target Percentile
    protected int DEC_ES_TARGET_ACCEPT_GAP = RESERVED_VALUE; // Target Acceptance Gap
    protected int DEC_ES_MAX_EXP = RESERVED_VALUE;           // Maximum Exposure
    protected int DEC_ES_MAX_GAIN = RESERVED_VALUE;          // Maximum Gain
    protected int DEC_ES_FRAME_RATE = RESERVED_VALUE;            // Frame Rate
    protected int DEC_ES_CONFORM_IMAGE = RESERVED_VALUE;     // Image Must Conform
    protected int DEC_ES_CONFORM_TRIES = RESERVED_VALUE;     // Tries for Conform
    protected int DEC_ES_SPECULAR_EXCLUSION = RESERVED_VALUE;    // Exclude Specular Regions
    protected int DEC_ES_SPECULAR_SAT = RESERVED_VALUE;      // Specular Saturation
    protected int DEC_ES_SPECULAR_LIMIT = RESERVED_VALUE;        // Specular Limit
    //protected int DEC_ES_FIXED_EXP =  0x0A21;         // Fixed Exposure
    protected int DEC_ES_FIXED_GAIN = RESERVED_VALUE;            // Fixed Gain
    protected int DEC_ES_FIXED_FRAME_RATE = RESERVED_VALUE;  // Fixed Frame Rate
    protected int DEC_ILLUM_POWER_LEVEL = RESERVED_VALUE;
    protected int DEC_MaxMultiRead_COUNT = RESERVED_VALUE;
    protected int DEC_PICKLIST_AIM_MODE = RESERVED_VALUE;
    protected int DEC_PICKLIST_AIM_DELAY = RESERVED_VALUE; //3680The aimer delay allows a delay time for the operator to aim the scan engine before the picture is taken. Use these codes to set
    //the time between when the trigger is pulled and when the picture is taken. During the delay time, the aiming light will appear,
    //but the LEDs won’t turn on until the delay time is over.
    protected int DEC_Multiple_Decode_MODE = RESERVED_VALUE;  // Multiple_Decode
    protected int DEC_Multiple_Decode_TIMEOUT = RESERVED_VALUE;  // Multiple_Decode
    protected int DEC_Multiple_Decode_INTERVAL = RESERVED_VALUE;  // Multiple_Decode, add by tao.he, 0190314
    protected int TRANSMIT_CODE_ID = RESERVED_VALUE;
    protected int DOTCODE_ENABLE = RESERVED_VALUE;
    //20200217
    protected int LINEAR_1D_QUIET_ZONE_LEVEL = RESERVED_VALUE;
    protected int CODE39_Quiet_Zone = RESERVED_VALUE;
    protected int CODE39_START_STOP = RESERVED_VALUE;
    protected int CODE39_SECURITY_LEVEL = RESERVED_VALUE;
    protected int M25_ENABLE_CHECK = RESERVED_VALUE;
    protected int M25_SEND_CHECK = RESERVED_VALUE;
    protected int M25_LENGTH1 = RESERVED_VALUE;
    protected int M25_LENGTH2 = RESERVED_VALUE;
    protected int I25_QUIET_ZONE = RESERVED_VALUE;
    protected int I25_SECURITY_LEVEL = RESERVED_VALUE;
    protected int CODABAR_ENABLE_CHECK = RESERVED_VALUE;
    protected int CODABAR_SEND_CHECK = RESERVED_VALUE;
    protected int CODABAR_SEND_START = RESERVED_VALUE;
    protected int CODABAR_CONCATENATE = RESERVED_VALUE;
    protected int CODE128_REDUCED_QUIET_ZONE = RESERVED_VALUE;
    protected int CODE128_CHECK_ISBT_TABLE = RESERVED_VALUE;
    protected int CODE_ISBT_Concatenation_MODE = RESERVED_VALUE;
    protected int CODE128_SECURITY_LEVEL = RESERVED_VALUE;
    protected int CODE128_IGNORE_FNC4 = RESERVED_VALUE;
    protected int UCC_REDUCED_QUIET_ZONE = RESERVED_VALUE;
    protected int UCC_COUPON_EXT_REPORT_MODE = RESERVED_VALUE;
    protected int UCC_EAN_ZERO_EXTEND = RESERVED_VALUE;
    protected int UCC_EAN_SUPPLEMENTAL_MODE = RESERVED_VALUE;
    protected int GS1_LIMIT_Security_Level = RESERVED_VALUE;
    protected int COMPOSITE_UPC_MODE = RESERVED_VALUE;
    protected int POSTAL_GROUP_TYPE_ENABLE = RESERVED_VALUE;
    protected int KOREA_POST_ENABLE = RESERVED_VALUE;
    protected int Canadian_POSTAL_ENABLE = RESERVED_VALUE;
}
