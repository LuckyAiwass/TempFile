
package com.android.server.scanner;

import android.device.ScanNative;
import android.device.scanner.configuration.PropertyID;
import android.util.Log;
import android.util.SparseArray;

import com.android.server.ScanServiceWrapper;

import java.util.ArrayList;

public class Se955Scanner extends SerialScanner {

    private static final byte DECODE_DATA = (byte) 0xF3;
    private static final byte CMD_ACK = (byte) 0xD0;
    private static final byte CMD_NAK = (byte) 0xD1;
    private static final byte PARAM_SEND = (byte) 0xC6;
    private static final byte REPLY_REVISION = (byte) 0xA4;
    private static final byte EVENT = (byte) 0xF6;
    private static final String TAG = "Se955Scanner";

    public Se955Scanner(ScanServiceWrapper scanService) {
        super(scanService);
        mScannerType = ScannerFactory.TYPE_SE955;
        mBaudrate = 9600;
        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            mPropIndexHashMap.put(INTERNAL_PROPERTY_INDEX[i], VALUE_PARAM_INDEX[i]);
        }
    }

    @Override
    protected boolean onDataReceived() {
        if (mBufOffset < 4) {
            return false;
        }
        // 955

        // search the header of the data packet.
        int startIdx = 0;
        for (int i = 0; i < mBufOffset - 2; ++i) {
            if (mBuffer[i] >= 4 && mBuffer[i + 2] == 0 && (mBuffer[i+1] ==DECODE_DATA
                    ||mBuffer[i+1] ==CMD_ACK||mBuffer[i+1] ==CMD_NAK
                    ||mBuffer[i+1] ==PARAM_SEND
                    ||mBuffer[i+1] ==REPLY_REVISION
                    ||mBuffer[i+1] ==EVENT)) {
                //startIdx = i;
                break;
            }
            startIdx++;
        }
        int msgLen = mBuffer[startIdx] + 2;
        Log.i(TAG, "mBufOffset, startidx, msgLen......[" + mBufOffset
                + "][" + startIdx + "][" + msgLen + "]");
        
        /*if (startIdx > 16){
            Log.i(TAG, "----955---startIdx > 16----------");
            mBufOffset = 0;
            return false;        	
        }*/
        
        if (startIdx + msgLen <= mBufOffset) {
            if (CheckCheckSum(mBuffer, startIdx, msgLen)) {
                switch (mBuffer[startIdx + 1]) {
                    case DECODE_DATA: {
                        waittingACK = false;
                        SendACK();
                        int barcodeLen = msgLen - 7;
                        if (barcodeLen < 0)
                        	break;
                        byte[] tmp = new byte[barcodeLen];
                        for (int i = 0; i < barcodeLen; ++i) {
                            tmp[i] = mBuffer[startIdx + 5 + i];
                        }
                        sendBroadcast(tmp, mBuffer[startIdx + 4], barcodeLen);
                    }
                        break;
                    case CMD_ACK:
                        if(waittingACK) {
                            Log.i(TAG, "----unlock-------CMD_ACK---: ");
                            mHandler.sendEmptyMessage(MSG_RESPONSE_ACK);
                        }
                        break;
                    case CMD_NAK:
                        if(waittingACK) {
                            Log.i(TAG, "----unlock-------CMD_NCK---: ");
                            mHandler.sendEmptyMessage(MSG_RESPONSE_NCK);
                        }
                        break;
                    case PARAM_SEND:
                    case REPLY_REVISION:
                        break;
                    case EVENT:
                        SendACK();
                        break;
                    default:
                        SendNAK(2);
                        break;
                }

                // 把剩余内容移动到缓冲头部
                int len = mBufOffset - msgLen - startIdx;
                for (int i = 0; i < len; ++i) {
                    mBuffer[i] = mBuffer[startIdx + msgLen + i];
                }
                mBufOffset = len;
//                mBufOffset = 0;

                return true;
            } else { // checksum error
                Log.i(TAG, "----955---error, so clear----------");
                mBufOffset = 0;
                SendNAK(1);
                return false;
            }
        }
        Log.i(TAG, "prepare recv next...");

        return false;
    }

    private boolean CheckCheckSum(byte[] arr, int offset, int length) {

        int checksum = 0;
        int data = 0;
        int i = 0;
        for (i = 0; i < length - 2; ++i) {
            data = arr[offset + i] & 0xFF;
            checksum = (checksum + data) & 0xFFFF;
        }
        checksum = (~checksum + 0x0001) & 0xFFFF;

        if (((checksum & 0xFFFF) >> 8) == (arr[offset + i++] & 0xFF)
                && (checksum & 0xFF) == (arr[offset + i] & 0xFF)) {
            return true;
        } else {
            return false;
        }
    }

    private void SendACK() {
        synchronized(mHandler) {
            ScanNative.doAck();
        }
    }

    private void SendNAK(int a) {
        synchronized(mHandler) {
            ScanNative.doNack(a);
        }
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
    public void setDefaults() {
        // TODO Auto-generated method stub
        ScanNative.doDefaultSet();
    }

    @Override
    public int setProperties(SparseArray<Integer> property) {
        // TODO Auto-generated method stub
        int size = property.size();
        ArrayList<Byte> list = new ArrayList<Byte>();
        byte[] params = null;
        for(int i=0; i < size; i++) {
            int keyForIndex = property.keyAt(i);
            int internalIndex = mPropIndexHashMap.get(keyForIndex, SPECIAL_VALUE);
            if(internalIndex != SPECIAL_VALUE) {
                int value = property.get(keyForIndex);
                switch(keyForIndex) {
                   /* case PropertyID.CODABAR_NOTIS: {
                        value = (value== 1) ? 0:1;
                        }
                        break;*/
                    case PropertyID.MSI_CHECK_2_MOD_11: {
                        value = (value== 1) ? 0:1;//Options are: 0 (enable check Mod10/10, 1 (enable check Mod11/10)
                        }
                        break;
                    case PropertyID.CODABAR_CLSI: {
                        }
                       break;
                    case PropertyID.CODE32_ENABLE: {
                            if(mScanService.getPropertyInt(PropertyID.CODE39_ENABLE) == 0) {
                                list.add((byte) Se955ParamIndex.CODE39_ENABLE);
                                list.add((byte) value);
                            }
                    }
                        break;
                    case PropertyID.CODE39_ENABLE: {
                       /* if(value == 1) {
                            if(mScanService.getPropertyInt(PropertyID.CODE32_ENABLE) == 1) {
                                list.add((byte) Se955ParamIndex.CODE32_ENABLE);
                                list.add((byte) 1);
                            }
                        }*/
                    }
                        break;
                   /* case PropertyID.TRIOPTIC_ENABLE: {
                           if(mScanService.getPropertyInt(PropertyID.CODE39_FULL_ASCII) == 1) {
                                int newV = (value == 1 ? 0:1);
                                list.add((byte) Se955ParamIndex.CODE39_FULL_ASCII);
                                list.add((byte) newV);
                            }
                    }
                        break;*/
                    case PropertyID.EAN_EXT_ENABLE_2_5_DIGIT:
                        if(value == 1 ) {
                            value = 0x02;
                        }
                        break;
                    default:
                        break;
                }
                if (internalIndex != -1 && (internalIndex >> 8) == 0) {
                    list.add((byte) internalIndex);
                    list.add((byte) value);
                } else if (internalIndex != -1) {
                    list.add((byte) (internalIndex >> 8));
                    list.add((byte) (internalIndex & 0xff));
                    list.add((byte) value);
                }
            }
        }
        int length = list.size();
        if(length > 0) {
            params = new byte[length];
            for (int i = 0; i < length; i++) {
                params[i] = list.get(i);
            }
            ScanNative.setProperties(params);
        }
        return 0;
    }
    
    static class Se955ParamIndex {
        public static final int IMAGE_EXPOSURE_MODE = RESERVED_VALUE;
        public static final int IMAGE_FIXED_EXPOSURE = RESERVED_VALUE;
        public static final int IMAGE_PICKLIST_MODE = RESERVED_VALUE;
        public static final int IMAGE_ONE_D_INVERSE = RESERVED_VALUE;
        public final static int LASER_ON_TIME = 0x88;//0x01-0x63 df 0x1e * 100 ms
        public final static int TIMEOUT_BETWEEN_SAME_SYMBOL = 0x89;//0x01-0x63 df 0x30
        public final static int LINEAR_CODE_TYPE_SECURITY_LEVEL = 0x4e;//1 2 3 4
        public static final int FUZZY_1D_PROCESSING = RESERVED_VALUE;
        public static final int MULTI_DECODE_MODE = RESERVED_VALUE;
        public static final int BAR_CODES_TO_READ = RESERVED_VALUE;
        public static final int FULL_READ_MODE = RESERVED_VALUE;
        public static final int CODE39_ENABLE = 0x00;
        public static final int CODE39_ENABLE_CHECK = 0x30;
        public static final int CODE39_SEND_CHECK = 0x2b;//TODO 2d 1d
        public static final int CODE39_FULL_ASCII = 0x11;
        public static final int CODE39_LENGTH1 = 0x12;
        public static final int CODE39_LENGTH2 = 0x13;
        public static final int TRIOPTIC_ENABLE = 0x0d;
        public static final int CODE32_ENABLE = 0x56;//code32 //TODO 2d 1d need enable code39
        public static final int CODE32_SEND_CHECK = SPECIAL_VALUE;  // 2d 1d
        public static final int CODE32_SEND_START = SPECIAL_VALUE;//0xe7;//2d 1d adding the prefix character "A" to all Code 32 bar codes
        public static final int C25_ENABLE = 0xf0 << 8 | 0x98;
        public static final int D25_ENABLE = 0x05;//TODO 2d 1d discrete 2 of 5
        public static final int D25_ENABLE_CHECK = SPECIAL_VALUE;  //TODO 2d 1d
        public static final int D25_SEND_CHECK = SPECIAL_VALUE;  //TODO 2d 1d
        public static final int D25_2_BAR_START = SPECIAL_VALUE;  
        public static final int D25_LENGTH1 = 0x14;//TODO 2d 1d
        public static final int D25_LENGTH2 = 0x15;//TODO 2d 1d
        public static final int M25_ENABLE = 0xf2<<8|0x60;//TODO 2d matrix2of5
        public static final int M25_ENABLE_CHECK = 0xf2<<8|0x61;//TODO 2d
        public static final int M25_SEND_CHECK = 0xf2<<8|0x62;//TODO 2d
        public static final int M25_LENGTH1 = 0xf2<<8|0x63;//TODO 2d
        public static final int M25_LENGTH2 = 0xf2<<8|0x64;//TODO 2d
        public final static int CODE11_ENABLE = 0x0a;
        public final static int CODE11_ENABLE_CHECK = 0x34;
        public final static int CODE11_SEND_CHECK = 0x2f;
        public final static int CODE11_LENGTH1 = 0x1c;//min 2
        public final static int CODE11_LENGTH2 = 0x01d;//max 14
        public static final int I25_ENABLE = 0x06;
        public static final int I25_ENABLE_CHECK = 0x31;
        public static final int I25_SEND_CHECK = 0x2c;//TODO 2d 1d
        public static final int I25_CASE_CODE = SPECIAL_VALUE; //RESERVED_VALUE;//TODO 2d 1d
        public final static int I25_TO_EAN13 = 0x52;
        public static final int I25_LENGTH1 = 0x16;
        public static final int I25_LENGTH2 = 0x17;
        public static final int CODABAR_ENABLE = 0x07;
        public static final int CODABAR_ENABLE_CHECK = 0xf2<<8 | 0x68;  //TODO 2d 1d
        public static final int CODABAR_SEND_CHECK = 0xf2<<8 | 0x69;//SPECIAL_VALUE;  //TODO 2d 1d
        public static final int CODABAR_SEND_START = 0x37;
        public static final int CODABAR_NOTIS = 0x37;
        public static final int CODABAR_CLSI = 0x36;
        public static final int CODABAR_WIDE_GAPS = SPECIAL_VALUE; //RESERVED_VALUE;
        public static final int CODABAR_LENGTH1 = 0x18;
        public static final int CODABAR_LENGTH2 = 0x19;
        public static final int CODE93_ENABLE = 0x09;
        public static final int CODE93_LENGTH1 = 0x1a;
        public static final int CODE93_LENGTH2 = 0x1b;
        public static final int CODE128_ENABLE = 0x08;
        public static final int CODE128_EXT_ASCII = SPECIAL_VALUE; 
        public static final int CODE128_LENGTH1 = 0xf2<<8 | 0x72;  //TODO 2d (1d No length setting is required for Code 128.)
        public static final int CODE128_LENGTH2 = 0xf2<<8 | 0x73;  //TODO 2d 1d
        public static final int CODE_ISBT_128 = 0x54;
        public static final int CODE128_GS1_ENABLE = 0x0e;      //gs1-128
        public static final int CODE128_GS1_LENGTH1 = 0xf2<<8 | 0x9a;
        public static final int CODE128_GS1_LENGTH2 = 0xf2<<8 | 0x9b;
        public static final int UPCA_ENABLE = 0x01;
        public static final int UPCA_SEND_CHECK = 0x28;
        public static final int UPCA_SEND_SYS = 0x22;//TODO 2d 1d//0/1/2 df 0  (<SYSTEM CHARACTER> <DATA>)(0x01)  (< COUNTRY CODE> <SYSTEM CHARACTER> <DATA>)(0x02)
        public static final int UPCA_TO_EAN13 = 0x22;//SPECIAL_VALUE; //TODO 2d 1d  (< COUNTRY CODE> <SYSTEM CHARACTER> <DATA>)(0x02)
        public static final int UPCE_ENABLE = 0x02;
        public static final int UPCE_SEND_CHECK = 0x29;
        public static final int UPCE_SEND_SYS = 0x23;//TODO 2d 1d//0/1/2 df 0
        public static final int UPCE_TO_UPCA = 0x25;
        public final static int UPCE1_ENABLE = 0x0C;
        public final static int UPCE1_SEND_CHECK = 0x2A;
        public final static int UPCE1_SEND_SYS = 0x24;
        public final static int UPCE1_TO_UPCA = 0x26;
        public static final int EAN13_ENABLE = 0x03;
        public static final int EAN13_SEND_CHECK = 0xf2<<8 | 0x3a;//TODO 2d 1d
        public static final int EAN13_SEND_SYS = SPECIAL_VALUE; //RESERVED_VALUE;;//TODO 2d 1d
        public final static int EAN13_BOOKLANDEAN = 0x53;//BOOKLANDEAN
        public final static int EAN13_BOOKLAND_FORMAT = 0xf1 << 8 | 0x40;//df 0x00 ISBN-10;0x01 ISBN-13
        public static final int EAN8_ENABLE = 0x04;
        public static final int EAN8_SEND_CHECK = 0xf2<<8 | 0x41;//SPECIAL_VALUE; enable chk  0xf2<<8 | 0x40
        public static final int EAN8_TO_EAN13 = 0x27;//0x27  EAN Zero Extend //Convert EAN-8 to EAN-13 Type parm 0xe0 val 0* as ean13 val1 as ean8 When EAN Zero Extend is disabled, this parameter has no effect on bar code data.
        public static final int EAN_EXT_ENABLE_2_5_DIGIT = 0x10;//TODO to see upc-a upc-e ena-13 ena-8  ??????????????
        public final static int UPC_EAN_SECURITY_LEVEL = 0x4d;//0 1 2 3
        public final static int UCC_COUPON_EXT_CODE= 0x55;
        public static final int MSI_ENABLE = 0x0b;
        public static final int MSI_REQUIRE_2_CHECK = 0x32;
        public static final int MSI_SEND_CHECK = 0x2e;
        public static final int MSI_CHECK_2_MOD_11 = 0x33;//0 is mod10/mod11 1 is mod10/mod10 df 1
        public static final int MSI_LENGTH1 = 0x1e;
        public static final int MSI_LENGTH2 = 0x1f;
        public static final int GS1_14_ENABLE = 0xf0<<8|0x52;//rss code
        public static final int GS1_14_TO_UPC_EAN = 0xf0<<8 | 0x8d; //RESERVED_VALUE;//0xf0<<8|0x8d;//TODO-
        public static final int GS1_LIMIT_ENABLE = 0xf0<<8|0x53;
        public static final int GS1_EXP_ENABLE = 0xf0<<8|0x54;
        public static final int GS1_EXP_LENGTH1 = 0xf2<<8|0xb0; 
        public static final int GS1_EXP_LENGTH2 = 0xf2<<8|0xb1; 
        public static final int US_POSTNET_ENABLE = RESERVED_VALUE;
        public static final int US_PLANET_ENABLE = RESERVED_VALUE;
        public static final int US_POSTAL_SEND_CHECK = RESERVED_VALUE;
        public static final int USPS_4STATE_ENABLE = RESERVED_VALUE;
        public static final int UPU_FICS_ENABLE = RESERVED_VALUE;
        public static final int ROYAL_MAIL_ENABLE = RESERVED_VALUE;
        public static final int ROYAL_MAIL_SEND_CHECK = RESERVED_VALUE;
        public static final int AUSTRALIAN_POST_ENABLE = RESERVED_VALUE;
        public static final int KIX_CODE_ENABLE = RESERVED_VALUE;
        public static final int JAPANESE_POST_ENABLE = RESERVED_VALUE;
        public static final int PDF417_ENABLE = RESERVED_VALUE;
        public static final int PDF417_LENGTH1 = RESERVED_VALUE;
        public static final int PDF417_LENGTH2 = RESERVED_VALUE;
        public static final int MICROPDF417_ENABLE = RESERVED_VALUE;
        public static final int MICROPDF417_LENGTH1 = RESERVED_VALUE;
        public static final int MICROPDF417_LENGTH2 = RESERVED_VALUE;
        public static final int COMPOSITE_CC_AB_ENABLE = RESERVED_VALUE;     //composite-cc_ab
        public static final int COMPOSITE_CC_AB_LENGTH1 = RESERVED_VALUE; 
        public static final int COMPOSITE_CC_AB_LENGTH2 = RESERVED_VALUE; 
        public static final int COMPOSITE_CC_C_ENABLE = RESERVED_VALUE;     //composite-cc_c
        public static final int COMPOSITE_CC_C_LENGTH1 = RESERVED_VALUE; 
        public static final int COMPOSITE_CC_C_LENGTH2 = RESERVED_VALUE;
        public final static int COMPOSITE_TLC39_ENABLE = RESERVED_VALUE;
        public static final int HANXIN_ENABLE =  RESERVED_VALUE;
        public static final int HANXIN_INVERSE =  RESERVED_VALUE;
        public static final int DATAMATRIX_ENABLE = RESERVED_VALUE;
        public static final int DATAMATRIX_LENGTH1 = RESERVED_VALUE;
        public static final int DATAMATRIX_LENGTH2 = RESERVED_VALUE;
        public static final int DATAMATRIX_INVERSE = RESERVED_VALUE;
        public static final int MAXICODE_ENABLE = RESERVED_VALUE;
        public static final int MAXICODE_LENGTH1 = RESERVED_VALUE;
        public static final int MAXICODE_LENGTH2 = RESERVED_VALUE;
        public static final int QRCODE_ENABLE = RESERVED_VALUE;           //2d
        public static final int MICROQRCODE_ENABLE = RESERVED_VALUE;      //2d
        public static final int QRCODE_LENGTH1 = RESERVED_VALUE;
        public static final int QRCODE_LENGTH2 = RESERVED_VALUE;
        public static final int QRCODE_INVERSE = RESERVED_VALUE;
        public static final int AZTEC_ENABLE = RESERVED_VALUE;            //2d
        public static final int AZTEC_LENGTH1 = RESERVED_VALUE;
        public static final int AZTEC_LENGTH2 = RESERVED_VALUE;
        public static final int AZTEC_INVERSE = RESERVED_VALUE;
    }
   
    private final int[] VALUE_PARAM_INDEX = {
            Se955ParamIndex.IMAGE_EXPOSURE_MODE,
            Se955ParamIndex.IMAGE_FIXED_EXPOSURE,
            Se955ParamIndex.IMAGE_PICKLIST_MODE,
            Se955ParamIndex.IMAGE_ONE_D_INVERSE,
            Se955ParamIndex.LASER_ON_TIME,
            Se955ParamIndex.TIMEOUT_BETWEEN_SAME_SYMBOL,
            Se955ParamIndex.LINEAR_CODE_TYPE_SECURITY_LEVEL,
            Se955ParamIndex.FUZZY_1D_PROCESSING,
            Se955ParamIndex.MULTI_DECODE_MODE,
            Se955ParamIndex.BAR_CODES_TO_READ,
            Se955ParamIndex.FULL_READ_MODE,
            Se955ParamIndex.CODE39_ENABLE,
            Se955ParamIndex.CODE39_ENABLE_CHECK,
            Se955ParamIndex.CODE39_SEND_CHECK,
            Se955ParamIndex.CODE39_FULL_ASCII,
            Se955ParamIndex.CODE39_LENGTH1,
            Se955ParamIndex.CODE39_LENGTH2,
            Se955ParamIndex.TRIOPTIC_ENABLE,
            Se955ParamIndex.CODE32_ENABLE,
            Se955ParamIndex.CODE32_SEND_START,
            Se955ParamIndex.C25_ENABLE,
            Se955ParamIndex.D25_ENABLE, 
            Se955ParamIndex.D25_LENGTH1,
            Se955ParamIndex.D25_LENGTH2,
            Se955ParamIndex.M25_ENABLE,
            Se955ParamIndex.CODE11_ENABLE,
            Se955ParamIndex.CODE11_ENABLE_CHECK,
            Se955ParamIndex.CODE11_SEND_CHECK,
            Se955ParamIndex.CODE11_LENGTH1,
            Se955ParamIndex.CODE11_LENGTH2,
            Se955ParamIndex.I25_ENABLE,
            Se955ParamIndex.I25_ENABLE_CHECK,
            Se955ParamIndex.I25_SEND_CHECK,
            Se955ParamIndex.I25_LENGTH1,
            Se955ParamIndex.I25_LENGTH2,
            Se955ParamIndex.I25_TO_EAN13,
            Se955ParamIndex.CODABAR_ENABLE,
            Se955ParamIndex.CODABAR_NOTIS,
            Se955ParamIndex.CODABAR_CLSI,
            Se955ParamIndex.CODABAR_LENGTH1,
            Se955ParamIndex.CODABAR_LENGTH2,
            Se955ParamIndex.CODE93_ENABLE,
            Se955ParamIndex.CODE93_LENGTH1,
            Se955ParamIndex.CODE93_LENGTH2,
            Se955ParamIndex.CODE128_ENABLE,
            Se955ParamIndex.CODE128_LENGTH1,
            Se955ParamIndex.CODE128_LENGTH2,
            Se955ParamIndex.CODE_ISBT_128,
            Se955ParamIndex.CODE128_GS1_ENABLE,
            Se955ParamIndex.UPCA_ENABLE, 
            Se955ParamIndex.UPCA_SEND_CHECK,
            Se955ParamIndex.UPCA_SEND_SYS,
            Se955ParamIndex.UPCA_TO_EAN13,
            Se955ParamIndex.UPCE_ENABLE,
            Se955ParamIndex.UPCE_SEND_CHECK,
            Se955ParamIndex.UPCE_SEND_SYS,
            Se955ParamIndex.UPCE_TO_UPCA,
            Se955ParamIndex.UPCE1_ENABLE,
            Se955ParamIndex.UPCE1_SEND_CHECK,
            Se955ParamIndex.UPCE1_SEND_SYS,
            Se955ParamIndex.UPCE1_TO_UPCA,
            Se955ParamIndex.EAN13_ENABLE,
            //Se955ParamIndex.EAN13_SEND_CHECK,
            Se955ParamIndex.EAN13_BOOKLANDEAN,
            Se955ParamIndex.EAN13_BOOKLAND_FORMAT,
            Se955ParamIndex.EAN8_ENABLE,
            //Se955ParamIndex.EAN8_SEND_CHECK,
            Se955ParamIndex.EAN8_TO_EAN13,
            Se955ParamIndex.EAN_EXT_ENABLE_2_5_DIGIT,
            Se955ParamIndex.UPC_EAN_SECURITY_LEVEL,
            Se955ParamIndex.UCC_COUPON_EXT_CODE,
            Se955ParamIndex.MSI_ENABLE,
            Se955ParamIndex.MSI_REQUIRE_2_CHECK,
            Se955ParamIndex.MSI_SEND_CHECK,
            Se955ParamIndex.MSI_CHECK_2_MOD_11,
            Se955ParamIndex.MSI_LENGTH1,
            Se955ParamIndex.MSI_LENGTH2,
            Se955ParamIndex.GS1_14_ENABLE,
            Se955ParamIndex.GS1_14_TO_UPC_EAN,
            Se955ParamIndex.GS1_LIMIT_ENABLE,
            Se955ParamIndex.GS1_EXP_ENABLE,
            Se955ParamIndex.GS1_EXP_LENGTH1,
            Se955ParamIndex.GS1_EXP_LENGTH2,
            Se955ParamIndex.US_POSTNET_ENABLE,
            Se955ParamIndex.US_PLANET_ENABLE,
            Se955ParamIndex.US_POSTAL_SEND_CHECK,
            Se955ParamIndex.USPS_4STATE_ENABLE,
            Se955ParamIndex.UPU_FICS_ENABLE,
            Se955ParamIndex.ROYAL_MAIL_ENABLE,
            Se955ParamIndex.ROYAL_MAIL_SEND_CHECK,
            Se955ParamIndex.AUSTRALIAN_POST_ENABLE,
            Se955ParamIndex.KIX_CODE_ENABLE,
            Se955ParamIndex.JAPANESE_POST_ENABLE,
            Se955ParamIndex.PDF417_ENABLE,
            Se955ParamIndex.MICROPDF417_ENABLE,
            Se955ParamIndex.COMPOSITE_CC_AB_ENABLE,
            Se955ParamIndex.COMPOSITE_CC_C_ENABLE,
            Se955ParamIndex.COMPOSITE_TLC39_ENABLE,
            Se955ParamIndex.HANXIN_ENABLE,
            Se955ParamIndex.HANXIN_INVERSE,
            Se955ParamIndex.DATAMATRIX_ENABLE,
            Se955ParamIndex.DATAMATRIX_LENGTH1,
            Se955ParamIndex.DATAMATRIX_LENGTH2,
            Se955ParamIndex.DATAMATRIX_INVERSE,
            Se955ParamIndex.MAXICODE_ENABLE,
            Se955ParamIndex.QRCODE_ENABLE,
            Se955ParamIndex.QRCODE_INVERSE,
            Se955ParamIndex.MICROQRCODE_ENABLE,
            Se955ParamIndex.AZTEC_ENABLE,
            Se955ParamIndex.AZTEC_INVERSE,
            DEC_2D_LIGHTS_MODE,
            DEC_2D_CENTERING_ENABLE,
            DEC_2D_CENTERING_MODE,
            DEC_2D_WINDOW_UPPER_LX,
            DEC_2D_WINDOW_UPPER_LY,
            DEC_2D_WINDOW_LOWER_RX,
            DEC_2D_WINDOW_LOWER_RY,
            DEC_2D_DEBUG_WINDOW_ENABLE,
            DEC_ES_EXPOSURE_METHOD,
            DEC_ES_TARGET_VALUE,
            DEC_ES_TARGET_PERCENTILE,
            DEC_ES_TARGET_ACCEPT_GAP,
            DEC_ES_MAX_EXP,
            DEC_ES_MAX_GAIN,
            DEC_ES_FRAME_RATE,
            DEC_ES_CONFORM_IMAGE,
            DEC_ES_CONFORM_TRIES,
            DEC_ES_SPECULAR_EXCLUSION,
            DEC_ES_SPECULAR_SAT,
            DEC_ES_SPECULAR_LIMIT,
            DEC_ES_FIXED_GAIN,
            DEC_ES_FIXED_FRAME_RATE,
            DEC_ILLUM_POWER_LEVEL,
            DEC_PICKLIST_AIM_MODE,
            DEC_PICKLIST_AIM_DELAY,
            DEC_MaxMultiRead_COUNT,
            DEC_Multiple_Decode_TIMEOUT,
            DEC_Multiple_Decode_INTERVAL,
            DEC_Multiple_Decode_MODE,
            DEC_OCR_MODE,
            DEC_OCR_TEMPLATE,
            TRANSMIT_CODE_ID,
            DOTCODE_ENABLE,
            LINEAR_1D_QUIET_ZONE_LEVEL,
            CODE39_Quiet_Zone,
            CODE39_START_STOP,
            CODE39_SECURITY_LEVEL,
            M25_SEND_CHECK,
            M25_LENGTH1,
            M25_LENGTH2,
            I25_QUIET_ZONE,
            I25_SECURITY_LEVEL,
            CODABAR_ENABLE_CHECK,
            CODABAR_SEND_CHECK,
            CODABAR_SEND_START,
            CODABAR_CONCATENATE,
            CODE128_REDUCED_QUIET_ZONE,
            CODE128_CHECK_ISBT_TABLE,
            CODE_ISBT_Concatenation_MODE,
            CODE128_SECURITY_LEVEL,
            CODE128_IGNORE_FNC4,
            UCC_REDUCED_QUIET_ZONE,
            UCC_COUPON_EXT_REPORT_MODE,
            UCC_EAN_ZERO_EXTEND,
            UCC_EAN_SUPPLEMENTAL_MODE,
            GS1_LIMIT_Security_Level,
            COMPOSITE_UPC_MODE,
            POSTAL_GROUP_TYPE_ENABLE,
            KOREA_POST_ENABLE,
            Canadian_POSTAL_ENABLE,
    };
}
