package android.device.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.Settings.NameValueTable;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

/**
 * Javadoc comments given to access these values from the SDK
 */
public class Settings {

    private static final String TAG = "UrovoSettings";

    private static final String[] sSettingsProjection = {
            Constants.KEY_NAME, Constants.KEY_VALUE
    };

    private static final String sSettingsSelection = Constants.KEY_NAME + "=?";

    /**
     *
     */
    public static class System extends NameValueTable {
        public static final String SCANNER_SN = "SCANNER_SN";
        public static final String SCANNER_ENABLE = "SCANNER_ENABLE";
        public static final String SCANNER_TYPE = "SCANNER_TYPE";
        public static final String IMAGE_PICKLIST_MODE = "IMAGE_PICKLIST_MODE";
        public static final String IMAGE_ONE_D_INVERSE = "IMAGE_ONE_D_INVERSE";
        public static final String TRIGGERING_MODES = "TRIGGERING_MODES";
        public static final String SEND_GOOD_READ_BEEP_ENABLE = "SEND_GOOD_READ_BEEP_ENABLE";
        public static final String SEND_GOOD_READ_VIBRATE_ENABLE = "SEND_GOOD_READ_VIBRATE_ENABLE";
        public static final String GOOD_READ_BEEP_ENABLE = "GOOD_READ_BEEP_ENABLE";
        public static final String GOOD_READ_BEEP_AUDIO_FILE = "GOOD_READ_BEEP_AUDIO_FILE";
        public static final String GOOD_READ_VIBRATE_ENABLE = "GOOD_READ_VIBRATE_ENABLE";
        public static final String LABEL_APPEND_ENTER = "LABEL_APPEND_ENTER";
        public static final String TRIGGERING_LOCK = "TRIGGERING_LOCK";
        public static final String SEND_LABEL_PREFIX_SUFFIX = "SEND_LABEL_PREFIX_SUFFIX";
        public static final String LABEL_PREFIX = "LABEL_PREFIX";
        public static final String LABEL_SUFFIX = "LABEL_SUFFIX";
        public static final String LABEL_MATCHER_PATTERN = "LABEL_MATCHER_PATTERN";
        public static final String LASER_ON_TIME = "LASER_ON_TIME";
        public static final String TIMEOUT_BETWEEN_SAME_SYMBOL = "TIMEOUT_BETWEEN_SAME_SYMBOL";
        public static final String LINEAR_CODE_TYPE_SECURITY_LEVEL = "LINEAR_CODE_TYPE_SECURITY_LEVEL";
        public final static String LINEAR_1D_QUIET_ZONE_LEVEL = "LINEAR_1D_QUIET_ZONE_LEVEL";
        public static final String FUZZY_1D_PROCESSING = "FUZZY_1D_PROCESSING";
        public static final String MULTI_DECODE_MODE = "MULTI_DECODE_MODE";
        public static final String BAR_CODES_TO_READ = "BAR_CODES_TO_READ";
        public static final String FULL_READ_MODE = "FULL_READ_MODE";
        public static final String CODING_FORMAT = "CODING_FORMAT";
        public final static String IMAGE_EXPOSURE_MODE = "IMAGE_EXPOSURE_MODE";
        public final static String IMAGE_FIXED_EXPOSURE = "IMAGE_FIXED_EXPOSURE";
        public final static String LABEL_MATCHER_TARGETREGEX = "LABEL_MATCHER_TARGETREGEX";
        public final static String LABEL_MATCHER_REPLACEMENT = "LABEL_MATCHER_REPLACEMENT";
        public final static String REMOVE_NONPRINT_CHAR = "REMOVE_NONPRINT_CHAR";
        public final static String LABEL_SEPARATOR_ENABLE = "LABEL_SEPARATOR_ENABLE";
        public final static String LABEL_FORMAT_SEPARATOR_CHAR = "LABEL_FORMAT_SEPARATOR_CHAR";
        public final static String TRIGGERING_SLEEP_WORK = "TRIGGERING_SLEEP_WORK";
        public final static String TRANSMIT_CODE_ID = "TRANSMIT_CODE_ID";
        public final static String DPM_DECODE_MODE = "DPM_DECODE_MODE";

        //urovo add jinpu.lin 2019.05.08
        public final static String CHARACTER_DATA_DELAY = "CHARACTER_DATA_DELAY";
        public final static String APPEND_ENTER_DELAY = "APPEND_ENTER_DELAY";
        //urovo add end 2019.05.08

        public static final String CODE39_ENABLE = "CODE39_ENABLE";
        public static final String CODE39_ENABLE_CHECK = "CODE39_ENABLE_CHECK";
        public static final String CODE39_SEND_CHECK = "CODE39_SEND_CHECK";
        public static final String CODE39_FULL_ASCII = "CODE39_FULL_ASCII";
        public static final String CODE39_Quiet_Zone = "CODE39_Quiet_Zone";
        public static final String CODE39_START_STOP = "CODE39_START_STOP";
        public final static String CODE39_SECURITY_LEVEL = "CODE39_SECURITY_LEVEL";
        public static final String CODE39_LENGTH1 = "CODE39_LENGTH1";
        public static final String CODE39_LENGTH2 = "CODE39_LENGTH2";
        public static final String TRIOPTIC_ENABLE = "TRIOPTIC_ENABLE";
        public static final String CODE32_ENABLE = "CODE32_ENABLE";
        public static final String CODE32_SEND_START = "CODE32_SEND_START";
        public static final String C25_ENABLE = "C25_ENABLE";
        public static final String D25_ENABLE = "D25_ENABLE";
        public static final String D25_ENABLE_CHECK = "D25_ENABLE_CHECK";
        public static final String D25_LENGTH1 = "D25_LENGTH1";
        public static final String D25_LENGTH2 = "D25_LENGTH2";
        public static final String M25_ENABLE = "M25_ENABLE";
        public static final String M25_ENABLE_CHECK = "M25_ENABLE_CHECK";
        public static final String M25_SEND_CHECK = "M25_SEND_CHECK";
        public static final String M25_LENGTH1 = "M25_LENGTH1";
        public static final String M25_LENGTH2 = "M25_LENGTH2";
        public static final String CODE11_ENABLE = "CODE11_ENABLE";
        public static final String CODE11_ENABLE_CHECK = "CODE11_ENABLE_CHECK";
        public static final String CODE11_SEND_CHECK = "CODE11_SEND_CHECK";
        public static final String CODE11_LENGTH1 = "CODE11_LENGTH1";
        public static final String CODE11_LENGTH2 = "CODE11_LENGTH2";
        public static final String I25_ENABLE = "I25_ENABLE";
        public static final String I25_ENABLE_CHECK = "I25_ENABLE_CHECK";
        public static final String I25_SEND_CHECK = "I25_SEND_CHECK";
        public static final String I25_QUIET_ZONE = "I25_QUIET_ZONE";
        public static final String I25_SECURITY_LEVEL = "I25_SECURITY_LEVEL";
        public static final String I25_LENGTH1 = "I25_LENGTH1";
        public static final String I25_LENGTH2 = "I25_LENGTH2";
        public static final String I25_TO_EAN13 = "I25_TO_EAN13";
        public static final String CODABAR_ENABLE = "CODABAR_ENABLE";
        public static final String CODABAR_ENABLE_CHECK = "CODABAR_ENABLE_CHECK";
        public static final String CODABAR_SEND_CHECK = "CODABAR_SEND_CHECK";
        public static final String CODABAR_SEND_START = "CODABAR_SEND_START";
        public static final String CODABAR_CONCATENATE = "CODABAR_CONCATENATE";
        public static final String CODABAR_NOTIS = "CODABAR_NOTIS";
        public static final String CODABAR_CLSI = "CODABAR_CLSI";
        public static final String CODABAR_WIDE_GAPS = "CODABAR_WIDE_GAPS";
        public static final String CODABAR_LENGTH1 = "CODABAR_LENGTH1";
        public static final String CODABAR_LENGTH2 = "CODABAR_LENGTH2";
        public static final String CODE93_ENABLE = "CODE93_ENABLE";
        public static final String CODE93_LENGTH1 = "CODE93_LENGTH1";
        public static final String CODE93_LENGTH2 = "CODE93_LENGTH2";
        public static final String CODE128_ENABLE = "CODE128_ENABLE";
        public static final String CODE128_LENGTH1 = "CODE128_LENGTH1";
        public static final String CODE128_LENGTH2 = "CODE128_LENGTH2";
        public static final String CODE_ISBT_128 = "CODE_ISBT_128";
        public final static String CODE128_REDUCED_QUIET_ZONE = "CODE128_REDUCED_QUIET_ZONE";
        public final static String CODE128_CHECK_ISBT_TABLE = "CODE128_CHECK_ISBT_TABLE";
        public final static String CODE_ISBT_Concatenation_MODE = "CODE_ISBT_Concatenation_MODE";
        public final static String CODE128_SECURITY_LEVEL = "CODE128_SECURITY_LEVEL";
        public final static String CODE128_IGNORE_FNC4 = "CODE128_IGNORE_FNC4";
        public static final String CODE128_GS1_ENABLE = "CODE128_GS1_ENABLE";
        public final static String GS1128__UCCEAN_Composite = "GS1128__UCCEAN_Composite";
        public static final String UPCA_ENABLE = "UPCA_ENABLE";
        public static final String UPCA_SEND_CHECK = "UPCA_SEND_CHECK";
        public static final String UPCA_SEND_SYS = "UPCA_SEND_SYS";
        public static final String UPCA_TO_EAN13 = "UPCA_TO_EAN13";
        public static final String UPCE_ENABLE = "UPCE_ENABLE";
        public static final String UPCE_SEND_CHECK = "UPCE_SEND_CHECK";
        public static final String UPCE_SEND_SYS = "UPCE_SEND_SYS";
        public static final String UPCE_TO_UPCA = "UPCE_TO_UPCA";
        public static final String UPCE1_ENABLE = "UPCE1_ENABLE";
        public static final String UPCE1_SEND_CHECK = "UPCE1_SEND_CHECK";
        public static final String UPCE1_SEND_SYS = "UPCE1_SEND_SYS";
        public static final String UPCE1_TO_UPCA = "UPCE1_TO_UPCA";
        public static final String EAN13_ENABLE = "EAN13_ENABLE";
        public static final String EAN13_SEND_CHECK = "EAN13_SEND_CHECK";
        public static final String EAN13_BOOKLANDEAN = "EAN13_BOOKLANDEAN";
        public static final String EAN13_BOOKLAND_FORMAT = "EAN13_BOOKLAND_FORMAT";
        public static final String EAN8_ENABLE = "EAN8_ENABLE";
        public static final String EAN8_SEND_CHECK = "EAN8_SEND_CHECK";
        public static final String EAN8_TO_EAN13 = "EAN8_TO_EAN13";
        public static final String EAN_EXT_ENABLE_2_5_DIGIT = "EAN_EXT_ENABLE_2_5_DIGIT";
        public static final String UPC_EAN_SECURITY_LEVEL = "UPC_EAN_SECURITY_LEVEL";
        public static final String UCC_COUPON_EXT_CODE = "UCC_COUPON_EXT_CODE";
        public static final String UCC_COUPON_EXT_REPORT_MODE = "UCC_COUPON_EXT_REPORT_MODE";
        public static final String UCC_REDUCED_QUIET_ZONE = "UCC_REDUCED_QUIET_ZONE";
        public static final String UCC_EAN_ZERO_EXTEND = "UCC_EAN_ZERO_EXTEND";
        public static final String UCC_EAN_SUPPLEMENTAL_MODE = "UCC_EAN_SUPPLEMENTAL_MODE";
        public static final String MSI_ENABLE = "MSI_ENABLE";
        public static final String MSI_REQUIRE_2_CHECK = "MSI_REQUIRE_2_CHECK";
        public static final String MSI_SEND_CHECK = "MSI_SEND_CHECK";
        public static final String MSI_CHECK_2_MOD_11 = "MSI_CHECK_2_MOD_11";
        public static final String MSI_LENGTH1 = "MSI_LENGTH1";
        public static final String MSI_LENGTH2 = "MSI_LENGTH2";
        public static final String GS1_14_ENABLE = "GS1_14_ENABLE";
        public static final String GS1_14_TO_UPC_EAN = "GS1_14_TO_UPC_EAN";
        public static final String GS1_LIMIT_ENABLE = "GS1_LIMIT_ENABLE";
        public final static String GS1_LIMIT_Security_Level = "GS1_LIMIT_Security_Level";
        public static final String GS1_EXP_ENABLE = "GS1_EXP_ENABLE";
        public static final String GS1_EXP_LENGTH1 = "GS1_EXP_LENGTH1";
        public static final String GS1_EXP_LENGTH2 = "GS1_EXP_LENGTH2";
        public static final String US_POSTNET_ENABLE = "US_POSTNET_ENABLE";
        public static final String US_PLANET_ENABLE = "US_PLANET_ENABLE";
        public static final String US_POSTAL_SEND_CHECK = "US_POSTAL_SEND_CHECK";
        public static final String USPS_4STATE_ENABLE = "USPS_4STATE_ENABLE";
        public static final String UPU_FICS_ENABLE = "UPU_FICS_ENABLE";
        public static final String ROYAL_MAIL_ENABLE = "ROYAL_MAIL_ENABLE";
        public static final String ROYAL_MAIL_SEND_CHECK = "ROYAL_MAIL_SEND_CHECK";
        public static final String AUSTRALIAN_POST_ENABLE = "AUSTRALIAN_POST_ENABLE";
        public static final String KIX_CODE_ENABLE = "KIX_CODE_ENABLE";
        public static final String JAPANESE_POST_ENABLE = "JAPANESE_POST_ENABLE";
        public static final String KOREA_POST_ENABLE = "KOREA_POST_ENABLE";
        public final static String Canadian_POSTAL_ENABLE= "Canadian_POSTAL_ENABLE";
        public static final String POSTAL_GROUP_TYPE_ENABLE = "POSTAL_GROUP_TYPE_ENABLE";
        public static final String PDF417_ENABLE = "PDF417_ENABLE";
        public static final String MICROPDF417_ENABLE = "MICROPDF417_ENABLE";
        public static final String COMPOSITE_CC_AB_ENABLE = "COMPOSITE_CC_AB_ENABLE";
        public static final String COMPOSITE_CC_C_ENABLE = "COMPOSITE_CC_C_ENABLE";
        public static final String COMPOSITE_TLC39_ENABLE = "COMPOSITE_TLC39_ENABLE";
        public final static String COMPOSITE_UPC_MODE  = "COMPOSITE_UPC_MODE";
        public static final String HANXIN_ENABLE = "HANXIN_ENABLE";
        public static final String HANXIN_INVERSE = "HANXIN_INVERSE";
        public static final String DATAMATRIX_ENABLE = "DATAMATRIX_ENABLE";
        public static final String DATAMATRIX_LENGTH1 = "DATAMATRIX_LENGTH1";
        public static final String DATAMATRIX_LENGTH2 = "DATAMATRIX_LENGTH2";
        public static final String DATAMATRIX_INVERSE = "DATAMATRIX_INVERSE";
        //public static final String DATAMATRIX_MATRIX_MODE = "DATAMATRIX_MATRIX_MODE";
        public static final String MAXICODE_ENABLE = "MAXICODE_ENABLE";
        public static final String QRCODE_ENABLE = "QRCODE_ENABLE";
        public static final String QRCODE_INVERSE = "QRCODE_INVERSE";
        public static final String MICROQRCODE_ENABLE = "MICROQRCODE_ENABLE";
        public static final String AZTEC_ENABLE = "AZTEC_ENABLE";
        public static final String AZTEC_INVERSE = "AZTEC_INVERSE";
        public static final String DOTCODE_ENABLE = "DOTCODE_ENABLE";
        public static final String WEDGE_KEYBOARD_ENABLE = "WEDGE_KEYBOARD_ENABLE";
        public static final String WEDGE_KEYBOARD_TYPE = "WEDGE_KEYBOARD_TYPE";
        public static final String WEDGE_INTENT_ENABLE = "WEDGE_INTENT_ENABLE";
        public static final String WEDGE_INTENT_ACTION_NAME = "WEDGE_INTENT_ACTION_NAME";
        public static final String WEDGE_INTENT_CATEGORY_NAME = "WEDGE_INTENT_CATEGORY_NAME";
        public static final String WEDGE_INTENT_DELIVERY_MODE = "WEDGE_INTENT_DELIVERY_MODE";
        public static final String WEDGE_INTENT_LABEL_TYPE_TAG = "INTENT_LABEL_TYPE_TAG";//label_type
        public static final String WEDGE_INTENT_DATA_STRING_TAG = "INTENT_DATA_STRING_TAG";//data_string
        public static final String WEDGE_INTENT_DECODE_DATA_TAG = "INTENT_DECODE_DATA_TAG";//decode_data
        public final static String WEDGE_INTENT_FOREGROUND_FLAG = "WEDGE_INTENT_FOREGROUND_FLAG";

        public static final String DEC_2D_LIGHTS_MODE = "DEC_2D_LIGHTS_MODE";
        public static final String DEC_2D_CENTERING_ENABLE= "DEC_2D_CENTERING_ENABLE";
        public static final String DEC_2D_CENTERING_MODE= "DEC_2D_CENTERING_MODE";
        public static final String DEC_2D_WINDOW_UPPER_LX= "DEC_2D_WINDOW_UPPER_LX";
        public static final String DEC_2D_WINDOW_UPPER_LY= "DEC_2D_WINDOW_UPPER_LY";
        public static final String DEC_2D_WINDOW_LOWER_RX= "DEC_2D_WINDOW_LOWER_RX";
        public static final String DEC_2D_WINDOW_LOWER_RY= "DEC_2D_WINDOW_LOWER_RY";
        public static final String DEC_2D_DEBUG_WINDOW_ENABLE= "DEC_2D_DEBUG_WINDOW_ENABLE";
        public static final String DEC_ES_EXPOSURE_METHOD = "DEC_ES_EXPOSURE_METHOD";		// Auto Exposure Method
        public static final String DEC_ES_TARGET_VALUE = "DEC_ES_TARGET_VALUE";		// Target White Value
        public static final String DEC_ES_TARGET_PERCENTILE = "DEC_ES_TARGET_PERCENTILE";	// Target Percentile
        public static final String DEC_ES_TARGET_ACCEPT_GAP = "DEC_ES_TARGET_ACCEPT_GAP";	// Target Acceptance Gap
        public static final String DEC_ES_MAX_EXP = "DEC_ES_MAX_EXP";				// Maximum Exposure
        public static final String DEC_ES_MAX_GAIN = "DEC_ES_MAX_GAIN";			// Maximum Gain
        public static final String DEC_ES_FRAME_RATE = "DEC_ES_FRAME_RATE";			// Frame Rate
        public static final String DEC_ES_CONFORM_IMAGE = "DEC_ES_CONFORM_IMAGE";		// Image Must Conform
        public static final String DEC_ES_CONFORM_TRIES = "DEC_ES_CONFORM_TRIES";		// Tries for Conform
        public static final String DEC_ES_SPECULAR_EXCLUSION = "DEC_ES_SPECULAR_EXCLUSION";	// Exclude Specular Regions
        public static final String DEC_ES_SPECULAR_SAT = "DEC_ES_SPECULAR_SAT";		// Specular Saturation
        public static final String DEC_ES_SPECULAR_LIMIT = "DEC_ES_SPECULAR_LIMIT";		// Specular Limit
        //public static final String DEC_ES_FIXED_EXP = "DEC_ES_FIXED_EXP";			// Fixed Exposure
        public static final String DEC_ES_FIXED_GAIN = "DEC_ES_FIXED_GAIN";			// Fixed Gain
        public static final String DEC_ES_FIXED_FRAME_RATE = "DEC_ES_FIXED_FRAME_RATE";	// Fixed Frame Rate
        public static final String DEC_ILLUM_POWER_LEVEL = "DEC_ILLUM_POWER_LEVEL";
        public static final String DEC_PICKLIST_AIM_MODE = "DEC_PICKLIST_AIM_MODE";
        public static final String DEC_PICKLIST_AIM_DELAY = "DEC_PICKLIST_AIM_DELAY";
        public static final String DEC_MaxMultiRead_COUNT= "DEC_MaxMultiRead_COUNT";
        public static final String DEC_Multiple_Decode_TIMEOUT= "DEC_Multiple_Decode_TIMEOUT";
        public static final String DEC_Multiple_Decode_INTERVAL= "DEC_Multiple_Decode_INTERVAL"; // urovo tao.he add, 20190314
        public static final String DEC_Multiple_Decode_MODE= "DEC_Multiple_Decode_MODE";
        public static final String DEC_EachImageAttempt_TIME= "DEC_EachImageAttempt_TIME";
        public static final String DEC_DECODE_DELAY= "DEC_DECODE_DELAY";
        public static final String DEC_DECODE_DEBUG_MODE= "DEC_DECODE_DEBUG_MODE";
        public static final String OUT_EDITORTEXT_MODE= "OUT_EDITORTEXT_MODE";
        public static final String OUT_CLIPBOARD_ENABLE= "OUT_CLIPBOARD_ENABLE";
        public static final String OUT_CLIPBOARD_PASTE_ENABLE= "OUT_CLIPBOARD_PASTE_ENABLE";

        public static final String DEC_OCR_MODE= "DEC_OCR_MODE";
        public static final String DEC_OCR_TEMPLATE= "DEC_OCR_TEMPLATE";
        public static final String DEC_OCR_USER_TEMPLATE= "DEC_OCR_USER_TEMPLATE";
        public static final String SUSPENSION_BUTTON= "SUSPENSION_BUTTON";
        public static final String SCAN_HANDLE= "SCAN_HANDLE";
        public static final String SPECIFIC_CODE_GS= "SPECIFIC_CODE_GS";
        public static final String CACHE_DATA_ENABLE= "CACHE_DATA_ENABLE";
        public static final String CACHE_DATA_LIMIT_ENABLE= "CACHE_DATA_LIMIT_ENABLE";
        public static final String CACHE_DATA_LIMIT_TIME= "CACHE_DATA_LIMIT_TIME";
        public static final String DATAMATRIX_SYMBOL_SIZE= "DATAMATRIX_SYMBOL_SIZE";
        public static final String MAXICODE_SYMBOL_SIZE= "MAXICODE_SYMBOL_SIZE";
        public static final String QRCODE_SYMBOL_SIZE= "QRCODE_SYMBOL_SIZE";
        public static final String AZTEC_SYMBOL_SIZE= "AZTEC_SYMBOL_SIZE";
        public static final String LOW_POWER_SLEEP_MODE=  "LOW_POWER_SLEEP_MODE";
        public static final String LOW_POWER_SLEEP_TIMEOUT=  "LOW_POWER_SLEEP_TIMEOUT";
        public static final String LOW_CONTRAST_IMPROVED=  "LOW_CONTRAST_IMPROVED";
        public static final String LOW_CONTRAST_IMPROVED_ALGORITHM=  "LOW_CONTRAST_IMPROVED_ALGORITHM";
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(Constants.CONTENT_URI_SETTINGS + Constants.TABLE_SETTINGS);

        /**
         * @return This CONTENT_URI appended with name
         */
        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_URI, name);
        }

        /** */
        public static String getString(ContentResolver cr, String name)
        {
            String outVal = null;
            int column;

            Cursor c = cr.query(CONTENT_URI,
                    sSettingsProjection, sSettingsSelection,
                    new String[] { name }, null);
            try {
                column = c.getColumnIndexOrThrow(Constants.KEY_VALUE);

                while (c.moveToNext()) {
                    String temp = c.getString(column);
                    if ( temp != null ) {
                        outVal = c.getString(column);
                    }
                }
            } catch (IllegalArgumentException e) {
                // Column not found
                Log.w(TAG, "Value column not found", e);
            } finally {
                if (c != null) c.close();
            }

            return outVal;
        }

        /**
         * Get setting as int. Internally settings are strings. 
         * The default value will be returned if the setting is 
         * not defined or not an integer.
         *
         * @param name The name of the setting.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid integer.
         */
        public static int getInt(ContentResolver cr, String name, int def) {
            String v = getString(cr, name);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        /**
         * Get setting as int. Internally settings are strings. 
         * The default value will be returned if the setting is 
         * not defined or not an integer.
         * <p>
         * If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link SettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting.
         *
         * @throws SettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not an integer.
         *
         * @return The setting's current value.
         */
        public static int getInt(ContentResolver cr, String name)
                throws SettingNotFoundException {
            String v = getString(cr, name);
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        public static boolean putBulkStrings (ContentResolver cr, ContentValues[] values){
            try {
                cr.bulkInsert(CONTENT_URI, values);
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        /** */
        public static boolean putString ( ContentResolver cr, String name, String value ) {
            return putString ( cr, CONTENT_URI, name, value ) ;
        }

        /** */
        public static boolean putInt ( ContentResolver cr, String name, int value ) {
            return putString ( cr, name, Integer.toString(value) );
        }
        /** */
        // defined in NamveValueTable
//         public static boolean putString ( ContentResolver cr, String name, String value ) {
//             ContentValues values = new ContentValues();
// 	    values.put(Constants.KEY_NAME, name);
// 	    values.put(Constants.KEY_VALUE, value);
// 	    
// 	    return cr.insert(CONTENT_URI, values);
//         }
    }

    /** */
    public static class Properites extends NameValueTable {
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(Constants.CONTENT_URI_SETTINGS + Constants.TABLE_PROPERTIES);

        /**
         * @return This CONTENT_URI appended with name
         */
        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_URI, name);
        }

        /** */
        public static String getString(ContentResolver cr, String name)
        {
            String outVal = null;
            int column;

            Cursor c = cr.query(CONTENT_URI,
                    sSettingsProjection, sSettingsSelection,
                    new String[] { name }, null);
            try {
                column = c.getColumnIndexOrThrow(Constants.KEY_VALUE);

                while (c.moveToNext()) {
                    String temp = c.getString(column);
                    if ( temp != null ) {
                        outVal = c.getString(column);
                    }
                }
            } catch (IllegalArgumentException e) {
                // Column not found
                Log.w(TAG, "Value column not found", e);
            } finally {
                if (c != null) c.close();
            }

            return outVal;
        }

        /**
         * Get setting as int. Internally settings are strings. 
         * The default value will be returned if the setting is 
         * not defined or not an integer.
         *
         * @param name The name of the setting.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid integer.
         */
        public static int getInt(ContentResolver cr, String name, int def) {
            String v = getString(cr, name);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        /**
         * Get setting as int. Internally settings are strings. 
         * The default value will be returned if the setting is 
         * not defined or not an integer.
         * <p>
         * If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link SettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting.
         *
         * @throws SettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not an integer.
         *
         * @return The setting's current value.
         */
        public static int getInt(ContentResolver cr, String name)
                throws SettingNotFoundException {
            String v = getString(cr, name);
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        /** */
        public static boolean putString ( ContentResolver cr, String name, String value ) {
            return putString ( cr, CONTENT_URI, name, value ) ;
        }

        /** */
        public static boolean putInt ( ContentResolver cr, String name, int value ) {
            return putString ( cr, name, Integer.toString(value) );
        }
        /** */
        // defined in NamveValueTable
//         public static boolean putString ( ContentResolver cr, String name, String value ) {
//             ContentValues values = new ContentValues();
// 	    values.put(Constants.KEY_NAME, name);
// 	    values.put(Constants.KEY_VALUE, value);
// 	    
// 	    return cr.insert(CONTENT_URI, values);
//         }
    }
}

