package com.android.server.scanner;

import android.util.Log;
import android.util.SparseArray;
import android.device.ScanNative;
import android.device.scanner.configuration.PropertyID;

import com.android.server.ScanServiceWrapper;
import java.util.Arrays;

public class ME5600Scanner extends SerialScanner {
    private static final String TAG = "ME5600Scanner";
    
    public ME5600Scanner(ScanServiceWrapper scanService) {
        super(scanService);
        // TODO Auto-generated constructor stub
        mScannerType = ScannerFactory.TYPE_Opticon;
        mBaudrate = 9600;

        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            mPropIndexHashMap.put(INTERNAL_PROPERTY_INDEX[i], 1);
        }
    }
    
    private int BytesIndexOf(byte[] arr, int offset, int count, byte b) {
        for (int i = offset; i < offset + count; ++i) {
            if (arr[i] == b) {
                return i;
            }
        }
        return -1;
    }
    private int BytesIndexOfETX(byte[] arr, int offset, int count, byte b, byte etx) {
        for (int i = offset; i < offset + count; ++i) {
            if (arr[i] == b) {
                if(i < mBufOffset && mBuffer[i + 1] == etx) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    protected boolean onDataReceived() {
        // TODO Auto-generated method stub
        if (mBufOffset < 4)
        return false;

        // for honyware 5180
        // int start = BytesIndexOf(mBuffer, 0, mBufOffset, (byte) 0x02);
        // Log.i(TAG, "----------------------------start=[" + start + "]");
        // if (start != -1) {
        //int end = BytesIndexOfETX(mBuffer, start, mBufOffset - start, (byte) 0x13, (byte)0x10);
        int end = BytesIndexOfETX(mBuffer, 0, mBufOffset, (byte) 0x0d, (byte)0x0a);
        Log.i(TAG, "----------------------------end=[" + end + "]"  + "mBufOffset " + mBufOffset);
        if (end != -1) {
            //if(end + 1 < mBufOffset && mBuffer[end + 1] == 0x04) {
            //int barcodelen = end - start - 2;
            int barcodelen = end;
            if(barcodelen < 0) return false;
            byte[] tmp = new byte[barcodelen];
            for (int i = 0; i < barcodelen; ++i) {
                tmp[i] = mBuffer[i];
                Log.i(TAG, "----------------------------tmp[i]=" + tmp[i] + "");
            }
            sendBroadcast(tmp, 0, barcodelen);

            // 把剩余内容移动到缓冲头部
            // int len = mBufOffset - (end + 2); // 剩余长度
            // Log.i(TAG, "-----------next len-------len=[" + len + "]");
            // if(len < 0) len = 0;
            // for (int i = 0; i < len; ++i) {
            //     mBuffer[i] = mBuffer[end + 1 + i];
            // }
            // mBufOffset = len;
            Arrays.fill(mBuffer,0,mBufOffset + 1,(byte)0);
            mBufOffset = 0;
            return true;
            //}
            }// end of end != -1
        //} // end of start !- 01
        return false;
    }

    @Override
    protected void onGetParamTimeout() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onSetParamTimeout() {
        // TODO Auto-generated method stub

    }

    @Override
    public int setProperties(SparseArray<Integer> property) {
        // TODO Auto-generated method stub
        int size = property.size();
        //ArrayList<Byte> list = new ArrayList<Byte>();
        byte[] params = null;
        try {
            for(int i=0; i < size; i++) {
                int keyForIndex = property.keyAt(i);
                int internalIndex = mPropIndexHashMap.get(keyForIndex, SPECIAL_VALUE);
                if(internalIndex != SPECIAL_VALUE) {
                    int len = 0;
                    int value = property.get(keyForIndex);
                    switch(keyForIndex) {
                        case PropertyID.IMAGE_PICKLIST_MODE: 
                            len = DEC_PICKLIST_LED.length;
                            DEC_PICKLIST_LED[len - 1] = (byte)value;
                            native_param_send(DEC_PICKLIST_LED);
                            break;
                        case PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL: 
                            len = DEC_SAME_1D_Delayed.length;
                            DEC_SAME_1D_Delayed[len - 1] = (byte)value;
                            native_param_send(DEC_SAME_1D_Delayed);
                            
                            len = DEC_SAME_2D_Delayed.length;
                            DEC_SAME_2D_Delayed[len - 1] = (byte)value;
                            native_param_send(DEC_SAME_2D_Delayed);
                            break;
                        case PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL:
                            len = DEC_SEC_LEVEL.length;
                            DEC_SEC_LEVEL[len - 1] = (byte)value;
                            native_param_send(DEC_SEC_LEVEL);
                             break;
                        //case PropertyID.INVERSE_1D: break;
                        case PropertyID.CODE39_ENABLE: 
                            len = DEC_code39_ENABLE.length;
                            DEC_code39_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_code39_ENABLE);
                            break;        //Code39 definitions
                        case PropertyID.CODE39_ENABLE_CHECK: 
                            len = DEC_code39_check.length;
                            DEC_code39_check[len - 1] = (byte)value;
                            native_param_send(DEC_code39_check);
                            break;  
                        case PropertyID.CODE39_SEND_CHECK: 
                            len = DEC_code39_sendcheck.length;
                            DEC_code39_sendcheck[len - 1] = (byte)value;
                            native_param_send(DEC_code39_sendcheck);
                            break;
                        case PropertyID.CODE39_FULL_ASCII: 
                            len = DEC_code39_ASCLL.length;
                            DEC_code39_ASCLL[len - 1] = (byte)value;
                            native_param_send(DEC_code39_ASCLL);
                            break; 
                        case PropertyID.CODE39_LENGTH1: 
                            len = DEC_code39_minlen.length;
                            DEC_code39_minlen[len - 1] = (byte)value;
                            native_param_send(DEC_code39_minlen);
                            break; 
                        case PropertyID.CODE39_LENGTH2: 
                            len = DEC_code39_maxlen.length;
                            DEC_code39_maxlen[len - 1] = (byte)value;
                            native_param_send(DEC_code39_maxlen);
                            break;
                        case PropertyID.TRIOPTIC_ENABLE: 
                            len = DEC_Trioptic_enable.length;
                            DEC_Trioptic_enable[len - 1] = (byte)value;
                            native_param_send(DEC_Trioptic_enable);
                            break;      //trioptic
                        case PropertyID.CODE32_ENABLE: 
                            len = DEC_code32_enable.length;
                            DEC_code32_enable[len - 1] = (byte)value;
                            native_param_send(DEC_code32_enable);
                            break;      //code 32 also see pharmacode 39
                        case PropertyID.CODE32_SEND_START:
                            len = DEC_code32_prefix.length;
                            DEC_code32_prefix[len - 1] = (byte)value;
                            native_param_send(DEC_code32_prefix);
                            break;
                        case PropertyID.CODE11_ENABLE: 
                            len = DEC_CODE11_ENABLE.length;
                            DEC_CODE11_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_CODE11_ENABLE);
                            break;
                        case PropertyID.CODE11_ENABLE_CHECK: 
                            len = DEC_CODE11_check.length;
                            DEC_CODE11_check[len - 1] = (byte)value;
                        native_param_send(DEC_CODE11_check);
                        break;
                        case PropertyID.CODE11_SEND_CHECK: 
                            len = DEC_CODE11_sendcheck.length;
                            DEC_CODE11_sendcheck[len - 1] = (byte)value;
                            native_param_send(DEC_CODE11_sendcheck);
                            break;
                        case PropertyID.CODE11_LENGTH1:
                            len = DEC_CODE11_minlen.length;
                            DEC_CODE11_minlen[len - 1] = (byte)value;
                            native_param_send(DEC_CODE11_minlen);
                            break;
                        case PropertyID.CODE11_LENGTH2: 
                            len = DEC_CODE11_maxlen.length;
                            DEC_CODE11_maxlen[len - 1] = (byte)value;
                            native_param_send(DEC_CODE11_maxlen);
                            break;//max55
                        case PropertyID.C25_ENABLE: 
                            len = DEC_ChinaPost_ENABLE.length;
                            DEC_ChinaPost_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_ChinaPost_ENABLE);
                            break;
                        case PropertyID.D25_ENABLE: 
                            len = DEC_D25_ENABLE.length;
                            DEC_D25_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_D25_ENABLE);
                            break;           //discrete 2/5
                        case PropertyID.D25_LENGTH1: 
                            len = DEC_D25_minlen.length;
                            DEC_D25_minlen[len - 1] = (byte)value;
                            native_param_send(DEC_D25_minlen);
                            break;
                        case PropertyID.D25_LENGTH2: 
                            len = DEC_D25_maxlen.length;
                            DEC_D25_maxlen[len - 1] = (byte)value;
                            native_param_send(DEC_D25_maxlen);
                            break; 
                        case PropertyID.M25_ENABLE: 
                            len = DEC_M25_ENABLE.length;
                            DEC_M25_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_M25_ENABLE);
                            break;           //matrix 2/5
                        case PropertyID.M25_LENGTH1: 
                            len = DEC_M25_minlen.length;
                            DEC_M25_minlen[len - 1] = (byte)value;
                            native_param_send(DEC_M25_minlen);
                            break; 
                        case PropertyID.M25_LENGTH2: 
                            len = DEC_M25_maxlen.length;
                            DEC_M25_maxlen[len - 1] = (byte)value;
                            native_param_send(DEC_M25_maxlen);
                            break;
                        case PropertyID.I25_ENABLE: 
                            len = DEC_I25_ENABLE.length;
                            DEC_I25_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_I25_ENABLE);
                            break;           //interleaved 2/5
                        case PropertyID.I25_ENABLE_CHECK: 
                            len = DEC_I25_check.length;
                            DEC_I25_check[len - 1] = (byte)value;
                            native_param_send(DEC_I25_check);
                            break;
                        case PropertyID.I25_SEND_CHECK: 
                            len = DEC_I25_sendcheck.length;
                            DEC_I25_sendcheck[len - 1] = (byte)value;
                            native_param_send(DEC_I25_sendcheck);
                            break;
                        case PropertyID.I25_TO_EAN13: 
                            break;
                        case PropertyID.I25_LENGTH1: 
                            len = DEC_I25_minlen.length;
                            DEC_I25_minlen[len - 1] = (byte)value;
                            native_param_send(DEC_I25_minlen);
                            break; 
                        case PropertyID.I25_LENGTH2: 
                            len = DEC_I25_maxlen.length;
                            DEC_I25_maxlen[len - 1] = (byte)value;
                            native_param_send(DEC_I25_maxlen);
                            break;
                        case PropertyID.CODABAR_ENABLE: 
                            len = DEC_CODABAR_ENABLE.length;
                            DEC_CODABAR_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_CODABAR_ENABLE);
                            break;           //codebar
                        case PropertyID.CODABAR_CLSI: 
                            len = DEC_CODABAR_Startend.length;
                            DEC_CODABAR_Startend[len - 1] = (byte)value;
                            native_param_send(DEC_CODABAR_Startend);
                            break;
                        case PropertyID.CODABAR_NOTIS: 
                            len = DEC_CODABAR_Startend_checkSame.length;
                            DEC_CODABAR_Startend_checkSame[len - 1] = (byte)value;
                            native_param_send(DEC_CODABAR_Startend_checkSame);
                            break;
                        case PropertyID.CODABAR_LENGTH1: 
                            len = DEC_CODABAR_minlen.length;
                            DEC_CODABAR_minlen[len - 1] = (byte)value;
                            native_param_send(DEC_CODABAR_minlen);
                            break; 
                        case PropertyID.CODABAR_LENGTH2:
                            len = DEC_CODABAR_maxlen.length;
                            DEC_CODABAR_maxlen[len - 1] = (byte)value;
                            native_param_send(DEC_CODABAR_maxlen);
                            break;
                        case PropertyID.CODE93_ENABLE: 
                            len = DEC_CODE93_ENABLE.length;
                            DEC_CODE93_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_CODE93_ENABLE);
                            break;        //code 93
                        case PropertyID.CODE93_LENGTH1: 
                            len = DEC_CODE93_minlen.length;
                            DEC_CODE93_minlen[len - 1] = (byte)value;
                            native_param_send(DEC_CODE93_minlen);
                            break;
                        case PropertyID.CODE93_LENGTH2: 
                            len = DEC_CODE93_maxlen.length;
                            DEC_CODE93_maxlen[len - 1] = (byte)value;
                            native_param_send(DEC_CODE93_maxlen);
                            break;
                        case PropertyID.CODE128_ENABLE: 
                            len = DEC_code128_ENABLE.length;
                            DEC_code128_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_code128_ENABLE);
                            break;       //code128
                        case PropertyID.CODE_ISBT_128: 
                            len = DEC_ISBT128_ENABLE.length;
                            DEC_ISBT128_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_ISBT128_ENABLE);
                            break;
                        case PropertyID.CODE128_LENGTH1: 
                            len = DEC_code128_minlen.length;
                            DEC_code128_minlen[len - 1] = (byte)value;
                            native_param_send(DEC_code128_minlen);
                            break;
                        case PropertyID.CODE128_LENGTH2: 
                            len = DEC_code128_maxlen.length;
                            DEC_code128_maxlen[len - 1] = (byte)value;
                            native_param_send(DEC_code128_maxlen);
                            break;
                        case PropertyID.CODE128_GS1_ENABLE: 
                            len = DEC_GS128_ENABLE.length;
                            DEC_GS128_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_GS128_ENABLE);
                            break;       //gs1-128
                        case PropertyID.UPCA_ENABLE: 
                            len = DEC_UPCA_ENABLE.length;
                            DEC_UPCA_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_UPCA_ENABLE);
                            break;          //uspa
                        case PropertyID.UPCA_SEND_CHECK: 
                            len = DEC_UPCA_sendcheck.length;
                            DEC_UPCA_sendcheck[len - 1] = (byte)value;
                            native_param_send(DEC_UPCA_sendcheck);
                            break; 
                        case PropertyID.UPCA_SEND_SYS: 
                            len = DEC_UPCA_Preamble.length;
                            if(value == 1) 
                                value = 3;
                            DEC_UPCA_Preamble[len - 1] = (byte)value;
                            native_param_send(DEC_UPCA_Preamble);
                            break;
                        case PropertyID.UPCE_ENABLE:
                            len = DEC_UPCE_ENABLE.length;
                            DEC_UPCE_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_UPCE_ENABLE);
                            break;      //uspe
                        case PropertyID.UPCE_SEND_CHECK: 
                            len = DEC_UPCE_Preamble.length;
                            if(value == 1) 
                                value = 0;
                            else
                                value = 4;
                            DEC_UPCE_Preamble[len - 1] = (byte)value;
                            native_param_send(DEC_UPCE_Preamble);
                            break;
                        case PropertyID.UPCE_SEND_SYS: 
                            len = DEC_UPCE_Preamble.length;
                            DEC_UPCE_Preamble[len - 1] = (byte)value;
                            native_param_send(DEC_UPCE_Preamble);
                            break;
                        case PropertyID.UPCE_TO_UPCA:
                            len = DEC_UPCE_Preamble.length;
                            if(value == 1) 
                                value = 3;
                            DEC_UPCE_Preamble[len - 1] = (byte)value;
                            native_param_send(DEC_UPCE_Preamble);
                            break;
                        case PropertyID.UPCE1_ENABLE: 
                            len = DEC_UPCE1_ENABLE.length;
                            DEC_UPCE1_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_UPCE1_ENABLE);
                            break;      //uspe
                        case PropertyID.UPCE1_SEND_CHECK:
                            len = DEC_UPCE1_sendcheck.length;
                            DEC_UPCE1_sendcheck[len - 1] = (byte)value;
                            native_param_send(DEC_UPCE1_sendcheck);
                            break;
                        case PropertyID.UPCE1_SEND_SYS: 
                            len = DEC_UPCE1_Preamble.length;
                            if(value == 1) 
                                value = 0;
                            else
                                value = 4;
                            DEC_UPCE1_Preamble[len - 1] = (byte)value;
                            native_param_send(DEC_UPCE1_Preamble);
                            break;
                        case PropertyID.UPCE1_TO_UPCA: 
                            len = DEC_UPCE1_Preamble.length;
                            if(value == 1) 
                                value = 3;
                            DEC_UPCE1_Preamble[len - 1] = (byte)value;
                            native_param_send(DEC_UPCE1_Preamble);
                            break;
                        case PropertyID.EAN13_ENABLE: 
                            len = DEC_EAN13_ENABLE.length;
                            DEC_EAN13_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_EAN13_ENABLE);
                            break;         //ean13
                        case PropertyID.EAN13_BOOKLANDEAN: 
                            len = DEC_EAN13_ISBNISSN.length;
                            DEC_EAN13_ISBNISSN[len - 1] = (byte)value;
                            native_param_send(DEC_EAN13_ISBNISSN);
                            break;
                        case PropertyID.EAN13_BOOKLAND_FORMAT: 
                            len = DEC_EAN13_ISBNISSN.length;
                            DEC_EAN13_ISBNISSN[len - 1] = (byte)value;
                            native_param_send(DEC_EAN13_ISBNISSN);
                            break;
                        case PropertyID.EAN8_ENABLE: 
                            len = DEC_EAN8_ENABLE.length;
                            DEC_EAN8_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_EAN8_ENABLE);
                            break;          //ean8
                        case PropertyID.EAN8_TO_EAN13: 
                            len = DEC_EAN8_Preamble.length;
                            if(value == 1) value = 2;
                            DEC_EAN8_Preamble[len - 1] = (byte)value;
                            native_param_send(DEC_EAN8_Preamble);
                            break;
                        case PropertyID.EAN_EXT_ENABLE_2_5_DIGIT: 
                            break;   //UPC/EAN Extensions definitions
                        case PropertyID.UPC_EAN_SECURITY_LEVEL: break; 
                        case PropertyID.UCC_COUPON_EXT_CODE: break;
                        case PropertyID.MSI_ENABLE: 
                            len = DEC_MSI_ENABLE.length;
                            DEC_MSI_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_MSI_ENABLE);
                            break;               //msi
                        case PropertyID.MSI_REQUIRE_2_CHECK: 
                            len = DEC_MSI_check.length;
                            DEC_MSI_check[len - 1] = (byte)value;
                            native_param_send(DEC_MSI_check);
                            break;
                        case PropertyID.MSI_SEND_CHECK: 
                            len = DEC_MSI_sendcheck.length;
                            DEC_MSI_sendcheck[len - 1] = (byte)value;
                            native_param_send(DEC_MSI_sendcheck);
                            break;
                        case PropertyID.MSI_CHECK_2_MOD_11: 
                            /*len = DEC_MSI_ENABLE.length;
                            DEC_MSI_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_MSI_ENABLE);*/
                            break; 
                        case PropertyID.MSI_LENGTH1:
                            len = DEC_MSI_minlen.length;
                            DEC_MSI_minlen[len - 1] = (byte)value;
                            native_param_send(DEC_MSI_minlen);
                            break;
                        case PropertyID.MSI_LENGTH2: 
                            len = DEC_MSI_maxlen.length;
                            DEC_MSI_maxlen[len - 1] = (byte)value;
                            native_param_send(DEC_MSI_maxlen);
                            break;
                        case PropertyID.GS1_14_ENABLE: 
                            len = DEC_GS1DataBar_ENABLE.length;
                            DEC_GS1DataBar_ENABLE[len - 1] = (byte)value;
                            native_param_send(DEC_GS1DataBar_ENABLE);
                            break;            //rss
                        case PropertyID.GS1_14_TO_UPC_EAN: 
                            len = DEC_GS1DataBar_Convert.length;
                            DEC_GS1DataBar_Convert[len - 1] = (byte)value;
                            native_param_send(DEC_GS1DataBar_Convert);
                            break;
                        case PropertyID.GS1_LIMIT_ENABLE: 
                            len = DEC_GS1Limited.length;
                            DEC_GS1Limited[len - 1] = (byte)value;
                            native_param_send(DEC_GS1Limited);
                            break;         //rss limit
                        case PropertyID.GS1_EXP_ENABLE:
                            len = DEC_GS1Expanded.length;
                            DEC_GS1Expanded[len - 1] = (byte)value;
                            native_param_send(DEC_GS1Expanded);
                            break;           //rss exp
                        case PropertyID.GS1_EXP_LENGTH1: 
                            len = DEC_GS1Expanded_minlen.length;
                            DEC_GS1Expanded_minlen[len - 1] = (byte)value;
                            native_param_send(DEC_GS1Expanded_minlen);
                            break;
                        case PropertyID.GS1_EXP_LENGTH2: 
                            len = DEC_GS1Expanded_maxlen.length;
                            DEC_GS1Expanded_maxlen[len - 1] = (byte)value;
                            native_param_send(DEC_GS1Expanded_maxlen);
                            break;
                        case PropertyID.US_POSTNET_ENABLE: break;        //postal code
                        case PropertyID.US_PLANET_ENABLE: break; 
                        case PropertyID.US_POSTAL_SEND_CHECK: break;
                        case PropertyID.USPS_4STATE_ENABLE: break; 
                        case PropertyID.UPU_FICS_ENABLE: break; 
                        case PropertyID.ROYAL_MAIL_ENABLE: break;
                        case PropertyID.ROYAL_MAIL_SEND_CHECK: break;
                        case PropertyID.AUSTRALIAN_POST_ENABLE: break;
                        case PropertyID.KIX_CODE_ENABLE: break; 
                        case PropertyID.JAPANESE_POST_ENABLE: break;
                        case PropertyID.PDF417_ENABLE: 
                            len = DEC_PDF417_enable.length;
                            DEC_PDF417_enable[len - 1] = (byte)value;
                            native_param_send(DEC_PDF417_enable);
                            break;        //pdf417
                        case PropertyID.MICROPDF417_ENABLE: 
                            len = DEC_MicroPDF417_enable.length;
                            DEC_MicroPDF417_enable[len - 1] = (byte)value;
                            native_param_send(DEC_MicroPDF417_enable);
                            break;       //micro pdf417
                        case PropertyID.COMPOSITE_CC_AB_ENABLE:
                        len = DEC_Composite.length;
                        if(value == 0)
                        DEC_Composite[len - 1] = (byte)value;
                        else
                            DEC_Composite[len - 1] = 2;
                        native_param_send(DEC_Composite);
                        break;     //composite-cc_ab  GS1-128 复合码、DataBar 复合码、UPC/EAN 复合码
                        case PropertyID.COMPOSITE_CC_C_ENABLE:
                        len = DEC_Composite.length;
                        DEC_Composite[len - 1] = (byte)value;
                        native_param_send(DEC_Composite);
                        break;    //composite-cc_c GS1-128 复合码、DataBar 复合码
                        //case PropertyID.COMPOSITE_TLC_39_ENABLE: break;
                        case PropertyID.DATAMATRIX_ENABLE: 
                            len = DEC_DATAMatrix_enable.length;
                            DEC_DATAMatrix_enable[len - 1] = (byte)value;
                            native_param_send(DEC_DATAMatrix_enable);
                            break;        //datamatrix
                        case PropertyID.DATAMATRIX_INVERSE: break;
                        case PropertyID.DATAMATRIX_LENGTH1: break;
                        case PropertyID.DATAMATRIX_LENGTH2: break;
                        case PropertyID.MAXICODE_ENABLE: 
                            break;          //maxicode
                        case PropertyID.QRCODE_ENABLE: 
                            len = DEC_QRCode_enable.length;
                            DEC_QRCode_enable[len - 1] = (byte)value;
                            native_param_send(DEC_QRCode_enable);
                            break;            //qrcode
                        case PropertyID.QRCODE_INVERSE: break;
                        case PropertyID.MICROQRCODE_ENABLE: 
                            len = DEC_MicroQR_enable.length;
                            DEC_MicroQR_enable[len - 1] = (byte)value;
                            native_param_send(DEC_MicroQR_enable);break;
                        case PropertyID.AZTEC_ENABLE: 
                            len = DEC_Aztec_enable.length;
                            DEC_Aztec_enable[len - 1] = (byte)value;
                            native_param_send(DEC_Aztec_enable);
                            break;             //aztec code
                        case PropertyID.AZTEC_INVERSE: break;
                        case PropertyID.HANXIN_ENABLE: 
                            len = DEC_HANXIN_enable.length;
                            DEC_HANXIN_enable[len - 1] = (byte)value;
                            native_param_send(DEC_HANXIN_enable);
                            break;             //han xin code
                        case PropertyID.HANXIN_INVERSE: break;
                        /*case PropertyID.DEC_ILLUM_MODE: 
                            len = DEC_ILLUM_MODE.length;
                            DEC_ILLUM_MODE[len - 1] = (byte)value;
                            native_param_send(DEC_ILLUM_MODE);
                            break;
                        case PropertyID.DEC_AIM_LEDMODE: 
                            len = DEC_AIM_LEDMODE.length;
                            DEC_AIM_LEDMODE[len - 1] = (byte)value;
                            native_param_send(DEC_AIM_LEDMODE);
                            break;
                        
                        case PropertyID.DEC_ILLUM_LEVEL: 
                            len = DEC_ILLUM_LEVEL.length;
                            DEC_ILLUM_LEVEL[len - 1] = (byte)value;
                            native_param_send(DEC_ILLUM_LEVEL);
                            break;
                        case PropertyID.IMAGER_PICKLIST_ENABLE:
                        case PropertyID.DEC_DEC_CENTERING: 
                            len = DEC_DEC_CENTERING.length;
                            if(value == 0) {
                                DEC_DEC_CENTERING[len - 1] = 0x00;
                            } else {
                                DEC_DEC_CENTERING[len - 1] = 0x01;
                            }
                            native_param_send(DEC_DEC_CENTERING);
                            break;
                        case PropertyID.DEC_2D_Symbology_ENABLE: 
                            len = DEC_2D_ALLDISABLE.length;
                            DEC_2D_ALLDISABLE[len - 1] = (byte)value;
                            native_param_send(DEC_2D_ALLDISABLE);
                            break;
                        case PropertyID.DEC_1D_Symbology_ENABLE: 
                            len = DEC_1D_ALLDISABLE.length;
                            DEC_1D_ALLDISABLE[len - 1] = (byte)value;
                            native_param_send(DEC_1D_ALLDISABLE);
                            break;*/
                        default:
                            break;
                    }
                }
            }
            native_me5600_writeFlash();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    private void native_param_send(byte[] param) {
    
    }
    private void native_me5600_writeFlash() {
    
    }
    @Override
    public void setDefaults() {
        // TODO Auto-generated method stub
        ScanNative.doopticonset();
         
    }

    //单次按键触发 00
    //按键保持 01*
    //单次按键保持 04
    //主机 05
    static byte[] DEC_MODE = new byte[]{0x01,(byte)0x91, 0x01,0x01};
    /**
    4sec 0x00 8sec 0x01 16 0x02 24 0x03 30 0x04 1min 1.5min
    2/5/7/10/15/20/20/45/60min
    */
    static byte[] DEC_MODE_TIMEOUT = new byte[]{0x01,(byte)0x92, 0x01,0x00};
    static byte[] DEC_SAME_1D_Delayed = new byte[]{0x01,(byte)0x93, 0x01,0x08}; //0-50sec
    static byte[] DEC_SAME_2D_Delayed = new byte[]{0x01,(byte)0x9F, 0x01,0x08};
    //多次解码结果相同,数据才被确认为有效 00-09
    static byte[] DEC_SEC_LEVEL = new byte[]{0x01,(byte)0x94, 0x01,0x00};
    //纠错优化解码功能:如使能,引擎会使用纠错算法优化解码。本功能并不是对所有的解码种类都有效
    static byte[] DEC_SEC_ENABLE = new byte[]{0x01,(byte)0x9A, 0x01,0x01};
    //连续扫描数据输出延时:如使能,在连续扫描模式,解码成功后,会暂存数据,继续解码。在设定时间内
    //没有解得新的条码,才输出之前保存的一个或多个条码数据 00-99 (100ms)
     static byte[] DEC_Continuous_timeout = new byte[]{0x01,(byte)0x9B, 0x01,0x00};
     // 00 close 01 open 02 blink 03 识读时长开
     static byte[] DEC_ILLUM_MODE = new byte[]{0x23,(byte)0x29, 0x01,0x02};
     // 00 close 01 open 02 识读前开启 03 仅识读时开启
     static byte[] DEC_AIM_LEDMODE = new byte[]{0x23,(byte)0x2A, 0x01,0x03};
     //00 close 01 low 02 dim 03 hight
     static byte[] DEC_ILLUM_LEVEL = new byte[]{0x23,(byte)0x2B, 0x01,0x02};
     //00 auto 01 disable 02 enable  04 only qr 03 only pdf417
     static byte[] DEC_1D_ALLDISABLE = new byte[]{0x03,(byte)0xED, 0x01,0x00};
     static byte[] DEC_2D_ALLDISABLE = new byte[]{0x03,(byte)0xE9, 0x01,0x00};
     static byte[] DEC_DEC_MultipleMode = new byte[]{0x03,(byte)0xEB, 0x01,0x00};
     static byte[] DEC_DEC_CENTERING = new byte[]{0x03,(byte)0xEC, 0x01,0x00};
     //DPM
     static byte[] DEC_PICKLIST_DPM = new byte[]{0x03,(byte)0xEA, 0x01,0x00};
     static byte[] DEC_PICKLIST_LED = new byte[]{0x03,(byte)0xEF, 0x01,0x01};
     //upca
     static byte[] DEC_UPCA_ENABLE = new byte[]{0x04,(byte)0x4D,0x01,0x01};
     static byte[] DEC_UPCA_check = new byte[]{0x04,(byte)0x4E,0x01,0x01};
     static byte[] DEC_UPCA_sendcheck = new byte[]{0x04,(byte)0x4F,0x01,0x01};
     //00 2 01 5 02 2/5 03
     static byte[] DEC_UPCA_ADDON = new byte[]{0x04,0x52,0x01,0x00};
     /*无 00*截去前导“0” 01扩展成 EAN-13 02截去系统字符 03增加国家代码 04*/ //TODO 0 2 4
     static byte[] DEC_UPCA_Preamble = new byte[]{0x04,0x53,0x01,0x00};
     //upce
     static byte[] DEC_UPCE_ENABLE = new byte[]{0x04,(byte)0xB1,0x01,0x01};
     static byte[] DEC_UPCE_check = new byte[]{0x04,(byte)0xB2,0x01,0x01};
     static byte[] DEC_UPCE_sendcheck = new byte[]{0x04,(byte)0xB3,0x01,0x01};
     //00 2 01 5 02 2/5 03
     static byte[] DEC_UPCE_ADDON = new byte[]{0x04,(byte)0xB6,0x01,0x00};
     //无 00*截去前导“0” 01扩展成 EAN-13 02扩展成 UPC-A 03截去系统字符 04增加国家代码 05
     static byte[] DEC_UPCE_Preamble = new byte[]{0x04,(byte)0xB7,0x01,0x00};
     //upce1
     static byte[] DEC_UPCE1_ENABLE = new byte[]{(byte)0x0D,0x49,0x01,0x00};
     static byte[] DEC_UPCE1_check = new byte[]{(byte)0x0D,(byte)0x4A,0x01,0x01};
     static byte[] DEC_UPCE1_sendcheck = new byte[]{(byte)0x0D,(byte)0x4B,0x01,0x01};
     //00 2 01 5 02 2/5 03
     static byte[] DEC_UPCE1_ADDON = new byte[]{(byte)0x0D,(byte)0x4E,0x01,0x00};
     //无 00*截去前导“0” 01扩展成 EAN-13 02扩展成 UPC-A 03截去系统字符 04增加国家代码 05
     static byte[] DEC_UPCE1_Preamble = new byte[]{(byte)0x0D,(byte)0x4F,0x01,0x00};
     //EAN13 ISBN 是对前导码为“978”的 EAN-13 码进行转换得到 10 位字符数据;ISSN 是对前导码为“977”的EAN-13 码进行转换得到的 8 位字符数据。
    //例如:条码“9780194315104”,输出:“019431510X”。
    //例如:条码“9771005180004”,输出:“10051805”
     static byte[] DEC_EAN13_ENABLE = new byte[]{(byte)0x05,0x15,0x01,0x01};
     static byte[] DEC_EAN13_check = new byte[]{(byte)0x05,(byte)0x16,0x01,0x01};
     static byte[] DEC_EAN13_sendcheck = new byte[]{(byte)0x05,(byte)0x17,0x01,0x01};
     //00 2 01 5 02 2/5 03
     static byte[] DEC_EAN13_ADDON = new byte[]{(byte)0x05,(byte)0x1A,0x01,0x00};
     static byte[] DEC_EAN13_ISBNISSN = new byte[]{(byte)0x05,(byte)0x1C,0x01,0x00};
     static byte[] DEC_EAN8_ENABLE = new byte[]{(byte)0x05,0x79,0x01,0x01};
     static byte[] DEC_EAN8_check = new byte[]{(byte)0x05,(byte)0x7A,0x01,0x01};
     static byte[] DEC_EAN8_sendcheck = new byte[]{(byte)0x05,(byte)0x7B,0x01,0x01};
     //00 2 01 5 02 2/5 03
     static byte[] DEC_EAN8_ADDON = new byte[]{(byte)0x05,(byte)0x7E,0x01,0x00};
     // 02 扩展成EAN-13
     static byte[] DEC_EAN8_Preamble = new byte[]{(byte)0x05,(byte)0x7F,0x01,0x00};
     //code39
     static byte[] DEC_code39_ENABLE = new byte[]{(byte)0x05,(byte)0xDD,0x01,0x01};
     static byte[] DEC_code39_check = new byte[]{(byte)0x05,(byte)0xDE,0x01,0x01};
     static byte[] DEC_code39_sendcheck = new byte[]{(byte)0x05,(byte)0xDF,0x01,0x01};
     static byte[] DEC_code39_minlen = new byte[]{(byte)0x05,(byte)0xE1,0x01,0x01};
     static byte[] DEC_code39_maxlen = new byte[]{(byte)0x05,(byte)0xE0,0x01,0x00};
     static byte[] DEC_code39_ASCLL = new byte[]{(byte)0x05,(byte)0xE4,0x01,0x00};
     static byte[] DEC_code39_START_END = new byte[]{(byte)0x05,(byte)0xE5,0x01,0x00};
     static byte[] DEC_code32_enable = new byte[]{(byte)0x05,(byte)0xE7,0x01,0x00};
     static byte[] DEC_code32_prefix = new byte[]{(byte)0x05,(byte)0xE8,0x01,0x00};
     static byte[] DEC_Trioptic_enable = new byte[]{(byte)0x05,(byte)0xE9,0x01,0x00};
     static byte[] DEC_Trioptic_START_END = new byte[]{(byte)0x05,(byte)0xEA,0x01,0x00};
     
     static byte[] DEC_I25_ENABLE = new byte[]{(byte)0x06,0x41,0x01,0x01};
     static byte[] DEC_I25_check = new byte[]{(byte)0x06,(byte)0x42,0x01,0x00};
     static byte[] DEC_I25_sendcheck = new byte[]{(byte)0x06,(byte)0x43,0x01,0x00};
     static byte[] DEC_I25_minlen = new byte[]{(byte)0x06,(byte)0x46,0x01,0x06};
     static byte[] DEC_I25_maxlen = new byte[]{(byte)0x06,(byte)0x45,0x01,0x00};
     
     static byte[] DEC_M25_ENABLE = new byte[]{(byte)0x07,0x09,0x01,0x01};
     static byte[] DEC_M25_check = new byte[]{(byte)0x07,(byte)0x0A,0x01,0x00};
     static byte[] DEC_M25_sendcheck = new byte[]{(byte)0x07,(byte)0x0B,0x01,0x00};
     static byte[] DEC_M25_maxlen = new byte[]{(byte)0x07,(byte)0x0C,0x01,0x00};
     static byte[] DEC_M25_minlen = new byte[]{(byte)0x07,(byte)0x0D,0x01,0x06};
     
     static byte[] DEC_D25_ENABLE = new byte[]{(byte)0x06,(byte)0xA5,0x01,0x00};
     static byte[] DEC_D25_maxlen = new byte[]{(byte)0x06,(byte)0xA6,0x01,0x00};
     static byte[] DEC_D25_minlen = new byte[]{(byte)0x06,(byte)0xA7,0x01,0x01};
     static byte[] DEC_CODABAR_ENABLE = new byte[]{(byte)0x07,0x6D,0x01,0x01};
     static byte[] DEC_CODABAR_check = new byte[]{(byte)0x07,(byte)0x6E,0x01,0x00};
     static byte[] DEC_CODABAR_sendcheck = new byte[]{(byte)0x07,(byte)0x6F,0x01,0x00};
     static byte[] DEC_CODABAR_maxlen = new byte[]{(byte)0x07,(byte)0x70,0x01,0x00};
     static byte[] DEC_CODABAR_minlen = new byte[]{(byte)0x07,(byte)0x71,0x01,0x01};
     //ABCD/ABCD 00*abcd/abcd 01ABCD/TN*E 02abcd/tn*E 03
     static byte[] DEC_CODABAR_Startend_type = new byte[]{(byte)0x07,(byte)0x74,0x01,0x00};
     //CLSI Editing
     static byte[] DEC_CODABAR_Startend = new byte[]{(byte)0x07,(byte)0x75,0x01,0x01,0x00};
     //NOTIS Editing
     //如使能,条码的起始符与终止符必须相同才是有效条码
     static byte[] DEC_CODABAR_Startend_checkSame = new byte[]{(byte)0x07,(byte)0x76,0x01,0x01};
     static byte[] DEC_code128_ENABLE = new byte[]{(byte)0x07,(byte)0xD1,0x01,0x01};
     static byte[] DEC_code128_check = new byte[]{(byte)0x07,(byte)0xD2,0x01,0x00};
     static byte[] DEC_code128_sendcheck = new byte[]{(byte)0x07,(byte)0xD3,0x01,0x00};
     static byte[] DEC_code128_maxlen = new byte[]{(byte)0x07,(byte)0xD4,0x01,0x00};
     static byte[] DEC_code128_minlen = new byte[]{(byte)0x07,(byte)0xD5,0x01,0x01};
     static byte[] DEC_GS128_ENABLE = new byte[]{(byte)0x09,(byte)0xC5,0x01,0x01};
     static byte[] DEC_ISBT128_ENABLE = new byte[]{(byte)0x0C,(byte)0xE5,0x01,0x01};
    
    static byte[] DEC_CODE93_ENABLE = new byte[]{(byte)0x08,(byte)0x35,0x01,0x01};
    static byte[] DEC_CODE93_check = new byte[]{(byte)0x08,(byte)0x36,0x01,0x01};
    static byte[] DEC_CODE93_sendcheck = new byte[]{(byte)0x08,(byte)0x37,0x01,0x00};
    static byte[] DEC_CODE93_maxlen = new byte[]{(byte)0x08,(byte)0x38,0x01,0x00};
    static byte[] DEC_CODE93_minlen = new byte[]{(byte)0x08,(byte)0x39,0x01,0x01};
    
    static byte[] DEC_CODE11_ENABLE = new byte[]{(byte)0x08,(byte)0x99,0x01,0x00};
    static byte[] DEC_CODE11_check = new byte[]{(byte)0x08,(byte)0x9A,0x01,0x01};
    static byte[] DEC_CODE11_sendcheck = new byte[]{(byte)0x08,(byte)0x9B,0x01,0x00};
    static byte[] DEC_CODE11_maxlen = new byte[]{(byte)0x08,(byte)0x9C,0x01,0x00};
    static byte[] DEC_CODE11_minlen = new byte[]{(byte)0x08,(byte)0x9D,0x01,0x00};
    
    static byte[] DEC_MSI_ENABLE = new byte[]{(byte)0x08,(byte)0xFD,0x01,0x00};
    //有 1 位或者 2 位校验符选项。有三种校验模式:Mod10、Mod10/10 和 Mod10/11。1 位(模 10) 01
    static byte[] DEC_MSI_check = new byte[]{(byte)0x08,(byte)0xFE,0x01,0x00};
    static byte[] DEC_MSI_sendcheck = new byte[]{(byte)0x08,(byte)0xFF,0x01,0x00};
    static byte[] DEC_MSI_maxlen = new byte[]{(byte)0x09,(byte)0x00,0x01,0x00};
    static byte[] DEC_MSI_minlen = new byte[]{(byte)0x09,(byte)0x01,0x01,0x00};
    
    static byte[] DEC_UK_ENABLE = new byte[]{(byte)0x09,(byte)0x61,0x01,0x00};
    static byte[] DEC_UK_check = new byte[]{(byte)0x09,(byte)0x62,0x01,0x01};
    static byte[] DEC_UK_sendcheck = new byte[]{(byte)0x09,(byte)0x63,0x01,0x00};
    static byte[] DEC_UK_maxlen = new byte[]{(byte)0x09,(byte)0x64,0x01,0x00};
    static byte[] DEC_UK_minlen = new byte[]{(byte)0x09,(byte)0x65,0x01,0x01};
    //中国邮政码是 11定长
    static byte[] DEC_ChinaPost_ENABLE = new byte[]{(byte)0x0A,(byte)0x29,0x01,0x01};
    //static byte[] DEC_ChinaPost_maxlen = new byte[]{(byte)0x0A,(byte)0x2C,0x01,0x0B};
    //static byte[] DEC_ChinaPost_minlen = new byte[]{(byte)0x0A,(byte)0x2D,0x01,0x0B};
    static byte[] DEC_GS1DataBar_ENABLE = new byte[]{(byte)0x0A,(byte)0x8D,0x01,0x01};
    //UCC/EAN 128 01//UPC-A 或 EAN-13 02
    static byte[] DEC_GS1DataBar_Convert = new byte[]{(byte)0x0A,(byte)0x90,0x01,0x00};
    static byte[] DEC_GS1Limited = new byte[]{(byte)0x0A,(byte)0xF1,0x01,0x01};
    //UCC/EAN 128 01//UPC-A 或 EAN-13 02
    static byte[] DEC_GS1Limited_Convert = new byte[]{(byte)0x0A,(byte)0xF4,0x01,0x00};
    static byte[] DEC_GS1Expanded = new byte[]{(byte)0x0B,(byte)0x55,0x01,0x01};
    static byte[] DEC_GS1Expanded_maxlen = new byte[]{(byte)0x0B,(byte)0x56,0x01,0x00};
    static byte[] DEC_GS1Expanded_minlen = new byte[]{(byte)0x0B,(byte)0x57,0x01,0x01};
    //UCC/EAN 128 01//UPC-A 
    static byte[] DEC_GS1Expanded_Convert = new byte[]{(byte)0x0b,(byte)0x5A,0x01,0x00};
    /*GS1 复合码是一种特殊的条码类别,由一个线性一维条码和一个二维条码按上下排列的方式组合而成。线
    性一维条码是主码,包含主要信息,条码类型可以是:GS1-128 码、UPC/EAN 码或 DataBar 系列;二维条
    码是从码,包含次要信息(如日期、批号等),条码类型可以是:CC-A(最多可编码 56 个数字),CC-B(最
    多 338 个数字),CC-C(最多 2361 个数字)*/
    static byte[] DEC_Composite = new byte[]{(byte)0x0D,(byte)0xAD,0x01,0x00};
    static byte[] DEC_PDF417_enable = new byte[]{(byte)0x0b,(byte)0xb9,0x01,0x01};
    static byte[] DEC_MicroPDF417_enable = new byte[]{(byte)0x0C,(byte)0x1D,0x01,0x00};
    static byte[] DEC_QRCode_enable = new byte[]{(byte)0x0A,(byte)0xF1,0x01,0x01};
    static byte[] DEC_MicroQR_enable = new byte[]{(byte)0x11,(byte)0x95,0x01,0x00};
    static byte[] DEC_DATAMatrix_enable = new byte[]{(byte)0x10,(byte)0x05,0x01,0x01};
    static byte[] DEC_HANXIN_enable = new byte[]{(byte)0x10,(byte)0x69,0x01,0x00};
    static byte[] DEC_Aztec_enable = new byte[]{(byte)0x10,(byte)0xCD,0x01,0x00};
}
