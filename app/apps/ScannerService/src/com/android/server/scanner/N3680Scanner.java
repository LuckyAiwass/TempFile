package com.android.server.scanner;

import android.device.ScanNative;
import android.device.scanner.configuration.PropertyID;
import android.util.Log;
import android.util.SparseArray;

import com.android.server.ScanServiceWrapper;

public class N3680Scanner extends SerialScanner {
    private static final String TAG = "N3680Scanner";

    public N3680Scanner(ScanServiceWrapper scanService) {
        super(scanService);
        // TODO Auto-generated constructor stub
        mScanService = scanService;
        mScannerType = ScannerFactory.TYPE_N3680;
        mBaudrate = 115200;
        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            mPropIndexHashMap.put(INTERNAL_PROPERTY_INDEX[i], VALUE_PARAM_INDEX[i]);
        }
	}
    @Override
    protected boolean onDataReceived() {
       
    	if (mBufOffset < 4)
    		return false;
  
        // for honyware 5180
            int start = BytesIndexOf(mBuffer, 0, mBufOffset, (byte) 0x02);
            Log.i(TAG, "----------------------------start=[" + start + "]");
           if (start != -1) {
                int end = BytesIndexOfETX(mBuffer, start, mBufOffset - start, (byte) 0x03, (byte)0x04);
                Log.i(TAG, "----------------------------end=[" + end + "]"  + "mBufOffset " + mBufOffset);
               if (end != -1) {
                   //if(end + 1 < mBufOffset && mBuffer[end + 1] == 0x04) {
                       int barcodelen = end - start - 2;
                       if(barcodelen < 0) return false;
                       byte[] tmp = new byte[barcodelen];
                       for (int i = 0; i < barcodelen; ++i) {
                           tmp[i] = mBuffer[start + 2 + i];
                           //Log.i(TAG, "----------------------------tmp[i]=" + tmp[i] + "");
                       }
                       sendBroadcast(tmp, mBuffer[start + 1], barcodelen);

                       // 把剩余内容移动到缓冲头部
                       int len = mBufOffset - (end + 2); // 剩余长度
                       Log.i(TAG, "-----------next len-------len=[" + len + "]");
                       if(len < 0) len = 0;
                       for (int i = 0; i < len; ++i) {
                           mBuffer[i] = mBuffer[end + 1 + i];
                       }
                       mBufOffset = len;

                       return true;
                   //}
                }// end of end != -1
            } // end of start !- 01
        return false;
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
                if(i + 1 < mBufOffset && mBuffer[i + 1] == etx) {
                    return i;
                }
            }
        }
        return -1;
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
        if(android.os.Build.PROJECT.equals("SQ46")) {
            ScanNative.resetHoneywellRS232();
        } else {
            ScanNative.dohonywareset();
        }
        AztecCode[6] = '1';
        ScanNative.setHWProperties(AztecCode);
        MaxiCode[6] = '1';
        ScanNative.setHWProperties(MaxiCode);
        ScanNative.setHWProperties(CodabookA);
        ScanNative.setHWProperties(CodabookF);
        sleep(400);
        ScanNative.setHWProperties(US_POSTNET_ENABLE);
        ScanNative.setHWProperties(US_PLANET_ENABLE);
        ScanNative.setHWProperties(USPS_4STATE_ENABLE);
        sleep(400);
        ScanNative.setHWProperties(UPU_FICS_ENABLE);
        ScanNative.setHWProperties(ROYAL_MAIL_ENABLE);
        ScanNative.setHWProperties(KIX_CODE_ENABLE);
        sleep(400);
        ScanNative.setHWProperties(AUSTRALIAN_POST_ENABLE);
        ScanNative.setHWProperties(JAPANESE_POST_ENABLE);
        ScanNative.unlockTriggle();
    }
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public int setProperties(SparseArray<Integer> property) {
        // TODO Auto-generated method stub
        synchronized (mHandler) {
            int size = property.size();
            for (int i = 0; i < size; i++) {
                int keyForIndex = property.keyAt(i);
                int internalIndex = mPropIndexHashMap.get(keyForIndex, SPECIAL_VALUE);
                if (internalIndex != SPECIAL_VALUE) {
                    int value = property.get(keyForIndex);
                    int len = 0;
                    Log.i(TAG, "setProperties  size  " + size + " keyForIndex = " + keyForIndex);
                    switch (keyForIndex) {
                        case PropertyID.LASER_ON_TIME: {

                        }
                            break;
                        case PropertyID.DEC_2D_LIGHTS_MODE: {
                        Log.i(TAG, "setProperties  value  " + value + " keyForIndex = " + keyForIndex);
                            if(value == 0) {
                                ScanNative.setHWProperties(SCAN_LED_OFF);
                                sleep(400);
                                ScanNative.setHWProperties(SCAN_AIM_OFF);
                                sleep(400);
                                ScanNative.setHWProperties(SCAN_LED_OFF);
                            } else if(value == 1) {
                                ScanNative.setHWProperties(SCAN_LED_OFF);
                                sleep(400);
                                ScanNative.setHWProperties(SCAN_AIM_ON);
                                sleep(400);
                                ScanNative.setHWProperties(SCAN_LED_OFF);
                            } else if(value == 2) {
                                ScanNative.setHWProperties(SCAN_LED_ON);
                                sleep(400);;
                                ScanNative.setHWProperties(SCAN_AIM_OFF);
                                sleep(400);
                                ScanNative.setHWProperties(SCAN_LED_ON);
                            } else if(value == 3 || value == 4) {
                                ScanNative.setHWProperties(SCAN_LED_ON);
                                sleep(400);
                                ScanNative.setHWProperties(SCAN_AIM_ON);
                                sleep(400);
                                ScanNative.setHWProperties(SCAN_LED_ON);
                            }
                            if(value == 1 || 3 == value || 4 == value) {
                                int delay = mScanService.getPropertyInt(PropertyID.DEC_PICKLIST_AIM_DELAY);
                                if (delay < 100) {
                                    delay = 10;
                                } else {
                                    delay = delay/100;
                                }
                                if (delay < 10) {
                                    byte[] aimDelay = new byte[]{'S','C','N','D','L','Y',(byte) (delay + 48), '0', '0'};
                                    ScanNative.setHWProperties(aimDelay);
                                } else {
                                    int min = delay / 10 + 48;
                                    int mins = delay % 10 + 48;
                                    byte[] aimDelay = new byte[]{'S','C','N','D','L','Y', (byte) (min), (byte) (mins), '0', '0'};
                                    ScanNative.setHWProperties(aimDelay);
                                }
                            } else {
                                byte[] aimDelay = new byte[]{'S','C','N','D','L','Y','0'};
                                ScanNative.setHWProperties(aimDelay);
                            }
                        }
                            break;
                        case PropertyID.DEC_PICKLIST_AIM_MODE:
                        case PropertyID.DEC_2D_CENTERING_MODE:{
                            if(keyForIndex == PropertyID.DEC_PICKLIST_AIM_MODE) {
                                int aim_mode = mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE);
                                if ( aim_mode == 1 || 3 == aim_mode || 4 == aim_mode) {
                                    int mode = mScanService.getPropertyInt(PropertyID.DEC_2D_CENTERING_MODE);
                                    Log.i(TAG, "setProperties  CENTERING_MODE  " + mode);
                                    if(value == 1/* && mode != 0*/) {
                                        len = Centering_DECWIN.length;
                                        Centering_DECWIN[len -1] = '1';
                                        ScanNative.setHWProperties(Centering_DECWIN);
                                        sleep(400);
                                        ScanNative.setHWProperties(Centering_DECWIN);
                                        sleep(400);
                                        ScanNative.setHWProperties(Centering_DECTOP);
                                        if(mode == 1) {
                                            ScanNative.setHWProperties(Centering_DECTOP);
                                        } else if(mode == 2) {
                                            ScanNative.setHWProperties(Centering_DECBOT);
                                        }
                                    } else {
                                        ScanNative.setHWProperties(Centering_DECWIN);
                                    }
                                }
                            } else {
                                if(value == 1) {
                                    ScanNative.setHWProperties(Centering_DECTOP);
                                } else if(value == 2) {
                                    ScanNative.setHWProperties(Centering_DECBOT);
                                }
                            }
                        }
                            break;
                        case PropertyID.DEC_PICKLIST_AIM_DELAY: {
                            int aim_mode = mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE);
                            if ( aim_mode == 1 || 3 == aim_mode || 4 == aim_mode) {
                                if (value < 100) {
                                    value = 10;
                                } else {
                                    value = value/100;
                                }
                                if (value < 10) {
                                    byte[] aimDelay = new byte[]{'S','C','N','D','L','Y',(byte) (value + 48), '0', '0'};
                                    ScanNative.setHWProperties(aimDelay);
                                } else {
                                    int min = value / 10 + 48;
                                    int mins = value % 10 + 48;
                                    byte[] aimDelay = new byte[]{'S','C','N','D','L','Y', (byte) (min), (byte) (mins), '0', '0'};
                                    ScanNative.setHWProperties(aimDelay);
                                }
                            }
                        }
                            break;
                        case PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL: {
                            if (value < 10) {
                                byte[] rereadDelay = new byte[]{'D','L','Y','R','R','D', (byte) (value + 48), '0', '0'};
                                ScanNative.setHWProperties(rereadDelay);
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                byte[] rereadDelay = new byte[]{'D','L','Y','R','R','D', (byte) (min), (byte) (mins), '0', '0'};
                                ScanNative.setHWProperties(rereadDelay);
                            }
                        }
                            break;
                        case PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL: {
                            ScanNative.setHWProperties(SecurityLevel[value - 1]);
                        }
                            break;
                        case PropertyID.IMAGE_PICKLIST_MODE:{
                            if (value == 0) {
                                ScanNative.setHWProperties(NorScanMode);
                            } else {
                                ScanNative.setHWProperties(IMAGE_PICKLIST_MODE);
                            }
                        }
                        break;
                        case PropertyID.FUZZY_1D_PROCESSING:{
                            len = POOR_1D_PROCESSING.length;
                            if (value == 0) {
                                POOR_1D_PROCESSING[len - 1] = '0';
                            } else {
                                POOR_1D_PROCESSING[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(POOR_1D_PROCESSING);
                        }
                        break;
                        case PropertyID.CODE39_ENABLE: {
                            len = Code39.length;
                            if (value == 0) {
                                Code39[len - 1] = '0';
                            } else {
                                Code39[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(Code39);
                        }
                            break;
                        case PropertyID.CODE39_ENABLE_CHECK: {
                            len = Code39CHK.length;
                            if (value == 0) {
                                Code39CHK[len - 1] = '0';
                            } else {
                                if (mScanService.getPropertyInt(PropertyID.CODE39_SEND_CHECK) == 1) {
                                    Code39CHK[len - 1] = '2';
                                } else {
                                    Code39CHK[len - 1] = '1';
                                }
                            }
                            ScanNative.setHWProperties(Code39CHK);
                        }
                            break;
                        case PropertyID.CODE39_SEND_CHECK: {
                            len = Code39CHK.length;
                            if (value == 0) {
                                if (mScanService.getPropertyInt(PropertyID.CODE39_ENABLE_CHECK) == 1) {
                                    Code39CHK[len - 1] = '1';
                                } else {
                                    Code39CHK[len - 1] = '0';
                                }
                            } else {
                                Code39CHK[len - 1] = '2';
                            }
                            ScanNative.setHWProperties(Code39CHK);
                        }
                            break;
                        case PropertyID.CODE39_FULL_ASCII: {
                            len = Code39FullASCII.length;
                            if (value == 0) {
                                Code39FullASCII[len - 1] = '0';
                            } else {
                                Code39FullASCII[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(Code39FullASCII);
                        }
                            break;
                        case PropertyID.CODE39_LENGTH1: {
                            byte[] code39Min;
                            if (value < 10) {
                                code39Min = new byte[] {
                                        'C', '3', '9', 'M', 'I', 'N', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                code39Min = new byte[] {
                                        'C', '3', '9', 'M', 'I', 'N', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(code39Min);
                        }
                            break;
                        case PropertyID.CODE39_LENGTH2: {
                            byte[] code39Max;
                            if (value < 10) {
                                code39Max = new byte[] {
                                        'C', '3', '9', 'M', 'A', 'X', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                code39Max = new byte[] {
                                        'C', '3', '9', 'M', 'A', 'X', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(code39Max);
                        }
                            break;
                        case PropertyID.TRIOPTIC_ENABLE: {
                            len = TripticCode.length;
                            if (value == 0) {
                                TripticCode[len - 1] = '0';
                            } else {
                                TripticCode[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(TripticCode);
                        }
                            break;
                        case PropertyID.CODE32_ENABLE: {
                            len = Code32.length;
                            if (value == 0) {
                                Code32[len - 1] = '0';
                            } else {
                                Code32[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(Code32);
                        }
                            break;
                        case PropertyID.CODE32_SEND_START: {
                            len = Code39StartStop.length;
                            if (value == 0) {
                                Code39StartStop[len - 1] = '0';
                            } else {
                                Code39StartStop[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(Code39StartStop);
                        }
                            break;
                        case PropertyID.C25_ENABLE: {
                            len = Chinese25.length;
                            if (value == 0) {
                                Chinese25[len - 1] = '0';
                            } else {
                                Chinese25[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(Chinese25);
                        }
                            break;
                        case PropertyID.D25_ENABLE: {
                            len = D25.length;
                            if (value == 0) {
                                D25[len - 1] = '0';
                            } else {
                                D25[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(D25);
                        }
                            break;
                        case PropertyID.D25_LENGTH1: {
                            byte[] D25Min;
                            if (value < 10) {
                                D25Min = new byte[] {
                                        'R', '2', '5', 'M', 'I', 'X', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                D25Min = new byte[] {
                                        'R', '2', '5', 'M', 'I', 'X', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(D25Min);
                        }
                            break;
                        case PropertyID.D25_LENGTH2: {
                            byte[] D25Max;
                            if (value < 10) {
                                D25Max = new byte[] {
                                        'R', '2', '5', 'M', 'A', 'X', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                D25Max = new byte[] {
                                        'R', '2', '5', 'M', 'A', 'X', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(D25Max);
                        }
                            break;
                        case PropertyID.M25_ENABLE: {
                            len = M25.length;
                            if (value == 0) {
                                M25[len - 1] = '0';
                            } else {
                                M25[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(M25);
                        }
                            break;
                        case PropertyID.CODE11_ENABLE: {
                            len = Code11.length;
                            if (value == 0) {
                                Code11[len - 1] = '0';
                            } else {
                                Code11[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(Code11);
                        }
                            break;
                        case PropertyID.CODE11_ENABLE_CHECK:{
                            len = Code11CK.length;
                            if (value == 0) {
                                Code11CK[len - 1] = '2';
                            } else {
                                Code11CK[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(Code11CK);
                        }
                            break;
                        case PropertyID.CODE11_SEND_CHECK:{
                            len = Code11SEND.length;
                            if (value == 0) {
                                Code11SEND[len - 1] = '5';
                            } else {
                                Code11SEND[len - 1] = '4';
                            }
                            ScanNative.setHWProperties(Code11SEND);
                        }
                            break;
                        case PropertyID.CODE11_LENGTH1: {
                            byte[] C11Min;
                            if (value < 10) {
                                C11Min = new byte[] {
                                        'C', '1', '1', 'M', 'I', 'N', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                C11Min = new byte[] {
                                        'C', '1', '1', 'M', 'I', 'N', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(C11Min);
                        }
                            break;
                        case PropertyID.CODE11_LENGTH2: {
                            byte[] C11Max;
                            if (value < 10) {
                                C11Max = new byte[] {
                                        'C', '1', '1', 'M', 'A', 'X', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                C11Max = new byte[] {
                                        'C', '1', '1', 'M', 'A', 'X', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(C11Max);
                        }
                            break;
                        case PropertyID.I25_ENABLE: {
                            len = I25.length;
                            if (value == 0) {
                                I25[len - 1] = '0';
                            } else {
                                I25[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(I25);
                        }
                            break;
                        case PropertyID.I25_ENABLE_CHECK: {
                            len = I25CHK.length;
                            if (value == 0) {
                                I25CHK[len - 1] = '0';
                            } else {
                                if (mScanService.getPropertyInt(PropertyID.I25_SEND_CHECK) == 1) {
                                    I25CHK[len - 1] = '2';
                                } else {
                                    I25CHK[len - 1] = '1';
                                }
                            }
                            ScanNative.setHWProperties(I25CHK);
                        }
                            break;
                        case PropertyID.I25_SEND_CHECK: {
                            len = I25CHK.length;
                            if (value == 0) {
                                if (mScanService.getPropertyInt(PropertyID.I25_ENABLE_CHECK) == 1) {
                                    I25CHK[len - 1] = '1';
                                } else {
                                    I25CHK[len - 1] = '2';
                                }
                            } else {
                                I25CHK[len - 1] = '2';
                            }
                            ScanNative.setHWProperties(I25CHK);
                        }
                            break;
                        case PropertyID.I25_LENGTH1: {
                            byte[] I25Min;
                            if (value < 10) {
                                I25Min = new byte[] {
                                        'I', '2', '5', 'M', 'I', 'N', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                I25Min = new byte[] {
                                        'I', '2', '5', 'M', 'I', 'N', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(I25Min);
                        }
                            break;
                        case PropertyID.I25_LENGTH2: {
                            byte[] I25Max;
                            if (value < 10) {
                                I25Max = new byte[] {
                                        'I', '2', '5', 'M', 'A', 'X', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                I25Max = new byte[] {
                                        'I', '2', '5', 'M', 'A', 'X', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(I25Max);
                        }
                            break;
                        case PropertyID.I25_TO_EAN13: {
                        }
                            break;
                        case PropertyID.CODABAR_ENABLE: {
                            len = Codabar.length;
                            if (value == 0) {
                                Codabar[len - 1] = '0';
                            } else {
                                Codabar[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(Codabar);
                        }
                            break;
                        case PropertyID.CODABAR_NOTIS: {
                            len = CodabarST.length;
                            if (value == 0) {
                                CodabarST[len - 1] = '0';
                            } else {
                                CodabarST[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(CodabarST);
                        }

                            break;
                        /*
                         * case PropertyID.CODABAR_SEND_CHECK: break; case
                         * PropertyID.CODABAR_SEND_START: break;
                         */
                        case PropertyID.CODABAR_CLSI:{
                            len = CodabarCHK.length;
                            if (value == 0) {
                                CodabarCHK[len - 1] = '0';
                            } else {
                                CodabarCHK[len - 1] = '6';
                            }
                            ScanNative.setHWProperties(CodabarCHK);
                        }
                            break;
                        case PropertyID.CODABAR_LENGTH1: {
                            byte[] CBRMin;
                            if (value < 10) {
                                CBRMin = new byte[] {
                                        'C', 'B', 'R', 'M', 'I', 'N', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                CBRMin = new byte[] {
                                        'C', 'B', 'R', 'M', 'I', 'N', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(CBRMin);
                        }
                            break;
                        case PropertyID.CODABAR_LENGTH2: {
                            byte[] CBRMax;
                            if (value < 10) {
                                CBRMax = new byte[] {
                                        'C', 'B', 'R', 'M', 'A', 'X', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                CBRMax = new byte[] {
                                        'C', 'B', 'R', 'M', 'A', 'X', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(CBRMax);
                        }
                            break;
                        case PropertyID.CODE93_ENABLE: {
                            len = Code93.length;
                            if (value == 0) {
                                Code93[len - 1] = '0';
                            } else {
                                Code93[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(Code93);
                        }
                            break;
                        case PropertyID.CODE93_LENGTH1: {
                            byte[] C93Min;
                            if (value < 10) {
                                C93Min = new byte[] {
                                        'C', '9', '3', 'M', 'I', 'N', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                C93Min = new byte[] {
                                        'C', '9', '3', 'M', 'I', 'N', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(C93Min);
                        }
                            break;
                        case PropertyID.CODE93_LENGTH2: {
                            byte[] C93Max;
                            if (value < 10) {
                                C93Max = new byte[] {
                                        'C', '9', '3', 'M', 'A', 'X', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                C93Max = new byte[] {
                                        'C', '9', '3', 'M', 'A', 'X', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(C93Max);
                        }
                            break;
                        case PropertyID.CODE128_ENABLE: {
                            len = Code128.length;
                            if (value == 0) {
                                Code128[len - 1] = '0';
                            } else {
                                Code128[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(Code128);
                        }
                            break;
                        case PropertyID.CODE128_LENGTH1: {
                            byte[] C128Min;
                            if (value < 10) {
                                C128Min = new byte[] {
                                        '1', '2', '8', 'M', 'I', 'N', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                C128Min = new byte[] {
                                        '1', '2', '8', 'M', 'I', 'N', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(C128Min);
                        }
                            break;
                        case PropertyID.CODE128_LENGTH2: {
                            byte[] C128Max;
                            if (value < 10) {
                                C128Max = new byte[] {
                                        '1', '2', '8', 'M', 'A', 'X', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                C128Max = new byte[] {
                                        '1', '2', '8', 'M', 'A', 'X', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(C128Max);
                        }
                            break;
                        case PropertyID.CODE_ISBT_128: {
                            len = ISBT128.length;
                            if (value == 0) {
                                ISBT128[len - 1] = '0';
                            } else {
                                ISBT128[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(ISBT128);
                        }
                            break;
                        case PropertyID.CODE128_GS1_ENABLE: {
                            len = GS1_128.length;
                            if (value == 0) {
                                GS1_128[len - 1] = '0';
                            } else {
                                GS1_128[len - 1] = '6';
                            }
                            ScanNative.setHWProperties(GS1_128);
                        }
                            break;
                        case PropertyID.UPCA_ENABLE: {
                            len = UPC_A.length;
                            if (value == 0) {
                                UPC_A[len - 1] = '0';
                            } else {
                                UPC_A[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPC_A);
                        }
                            break;
                        case PropertyID.UPCA_SEND_CHECK: {
                            len = UPC_A_CHK.length;
                            if (value == 0) {
                                UPC_A_CHK[len - 1] = '0';
                            } else {
                                UPC_A_CHK[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPC_A_CHK);
                        }
                            break;
                        case PropertyID.UPCA_SEND_SYS: {
                            len = UPC_A_SYS.length;
                            if (value == 0) {
                                UPC_A_SYS[len - 1] = '0';
                            } else {
                                UPC_A_SYS[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPC_A_SYS);
                        }
                            break;
                        case PropertyID.UPCA_TO_EAN13: {
                            len = UPCA_TO_EAN13.length;
                            if (value == 1) {
                                UPCA_TO_EAN13[len - 1] = '0';
                            } else {
                                UPCA_TO_EAN13[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPCA_TO_EAN13);
                        }
                            break;
                        case PropertyID.UPCE_ENABLE: {
                            len = UPC_E.length;
                            if (value == 0) {
                                UPC_E[len - 1] = '0';
                            } else {
                                UPC_E[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPC_E);
                        }
                            break;
                        case PropertyID.UPCE_SEND_CHECK: {
                            len = UPC_E_CK.length;
                            if (value == 0) {
                                UPC_E_CK[len - 1] = '0';
                            } else {
                                UPC_E_CK[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPC_E_CK);
                        }
                            break;
                        case PropertyID.UPCE_SEND_SYS: {
                            len = UPC_E_Lead_Zero.length;
                            if (value == 0) {
                                UPC_E_Lead_Zero[len - 1] = '0';
                            } else {
                                UPC_E_Lead_Zero[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPC_E_Lead_Zero);
                        }
                            break;
                        case PropertyID.UPCE_TO_UPCA: {
                            len = UPC_E_to_UPCA.length;
                            if (value == 0) {
                                UPC_E_to_UPCA[len - 1] = '0';
                            } else {
                                UPC_E_to_UPCA[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPC_E_to_UPCA);
                        }
                            break;
                        case PropertyID.UPCE1_ENABLE:
                        {
                            len = UPCE_E1.length;
                            if (value == 0) {
                                UPCE_E1[len - 1] = '0';
                            } else {
                                UPCE_E1[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPCE_E1);
                        }
                            break;
                        case PropertyID.UPCE1_SEND_CHECK:
                         {
                            len = UPC_E_CK.length;
                            if (value == 0) {
                                UPC_E_CK[len - 1] = '0';
                            } else {
                                UPC_E_CK[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPC_E_CK);
                        }
                            break;
                        case PropertyID.UPCE1_SEND_SYS:
                        {
                            len = UPC_E_Lead_Zero.length;
                            if (value == 0) {
                                UPC_E_Lead_Zero[len - 1] = '0';
                            } else {
                                UPC_E_Lead_Zero[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPC_E_Lead_Zero);
                        }
                            break;
                        case PropertyID.UPCE1_TO_UPCA:
                        {
                            len = UPC_E_to_UPCA.length;
                            if (value == 0) {
                                UPC_E_to_UPCA[len - 1] = '0';
                            } else {
                                UPC_E_to_UPCA[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(UPC_E_to_UPCA);
                        }
                            break;
                        case PropertyID.EAN13_ENABLE: {
                            len = EAN13.length;
                            if (value == 0) {
                                EAN13[len - 1] = '0';
                            } else {
                                EAN13[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(EAN13);
                        }
                            break;

                        case PropertyID.EAN13_BOOKLANDEAN: {
                            len = BooklandEAN_ISBN.length;
                            if (value == 0) {
                                BooklandEAN_ISBN[len - 1] = '0';
                            } else {
                                BooklandEAN_ISBN[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(EAN13);
                            len = BooklandEAN_ISSN.length;
                            if (value == 0) {
                                BooklandEAN_ISSN[len - 1] = '0';
                            } else {
                                BooklandEAN_ISSN[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(BooklandEAN_ISSN);
                        }
                            break;
                        case PropertyID.EAN13_BOOKLAND_FORMAT: {
                            len = BooklandFormat.length;
                            if (value == 0) {
                                BooklandFormat[len - 1] = '0';
                            } else {
                                BooklandFormat[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(BooklandFormat);
                        }
                            break;
                        case PropertyID.EAN8_ENABLE: {
                            len = EAN8.length;
                            if (value == 0) {
                                EAN8[len - 1] = '0';
                            } else {
                                EAN8[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(EAN8);
                        }
                            break;
                        case PropertyID.EAN8_TO_EAN13: {
                            len = EAN8_to_EAN13.length;
                            if (value == 0) {
                                EAN8_to_EAN13[len - 1] = '0';
                            } else {
                                EAN8_to_EAN13[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(EAN8_to_EAN13);
                        }
                            break;
                        case PropertyID.EAN_EXT_ENABLE_2_5_DIGIT: {
                            if (value == 0) {
                                EAN13_ADD2[6] = '0';
                                ScanNative.setHWProperties(EAN13_ADD2);
                                EAN13_ADD5[6] = '0';
                                ScanNative.setHWProperties(EAN13_ADD5);
                                sleep(400);
                                EAN8_ADD2[6] = '0';
                                ScanNative.setHWProperties(EAN8_ADD2);
                                EAN8_ADD5[6] = '0';
                                ScanNative.setHWProperties(EAN8_ADD5);
                                sleep(400);
                                UPC_A_ADD2[6] = '0';
                                ScanNative.setHWProperties(UPC_A_ADD2);
                                UPC_A_ADD5[6] = '0';
                                ScanNative.setHWProperties(UPC_A_ADD5);
                                sleep(400);
                                UPC_E_ADD2[6] = '0';
                                ScanNative.setHWProperties(UPC_E_ADD2);
                                UPC_E_ADD5[6] = '0';
                                ScanNative.setHWProperties(UPC_E_ADD5);
                            } else {
                                EAN13_ADD2[6] = '1';
                                ScanNative.setHWProperties(EAN13_ADD2);
                                EAN13_ADD5[6] = '1';
                                ScanNative.setHWProperties(EAN13_ADD5);
                                sleep(400);
                                EAN8_ADD2[6] = '1';
                                ScanNative.setHWProperties(EAN8_ADD2);
                                EAN8_ADD5[6] = '1';
                                ScanNative.setHWProperties(EAN8_ADD5);
                                sleep(400);
                                UPC_A_ADD2[6] = '1';
                                ScanNative.setHWProperties(UPC_A_ADD2);
                                UPC_A_ADD5[6] = '1';
                                ScanNative.setHWProperties(UPC_A_ADD5);
                                sleep(400);
                                UPC_E_ADD2[6] = '1';
                                ScanNative.setHWProperties(UPC_E_ADD2);
                                UPC_E_ADD5[6] = '1';
                                ScanNative.setHWProperties(UPC_E_ADD5);
                            }
                        }
                            break;
                        case PropertyID.UPC_EAN_SECURITY_LEVEL:
                            break;
                        case PropertyID.UCC_COUPON_EXT_CODE:
                            break;
                        case PropertyID.MSI_ENABLE: {
                            len = MSI.length;
                            if (value == 0) {
                                MSI[len - 1] = '0';
                            } else {
                                MSI[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(MSI);
                        }
                            break;
                        case PropertyID.MSI_REQUIRE_2_CHECK: {
                            len = MSI_CHK10_NO_SEND.length;
                            if (value == 0) {
                                MSI_CHK10_NO_SEND[len - 1] = '0';
                            } else {
                                MSI_CHK10_NO_SEND[len - 1] = '2';
                            }
                            ScanNative.setHWProperties(MSI_CHK10_NO_SEND);
                        }
                            break;
                        case PropertyID.MSI_SEND_CHECK: {
                            len = MSI_CHK10_NO_SEND.length;
                            if (value == 0) {
                                MSI_CHK10_NO_SEND[len - 1] = '0';
                            } else {
                                if (mScanService.getPropertyInt(PropertyID.MSI_REQUIRE_2_CHECK) == 1) {
                                    MSI_CHK10_NO_SEND[len - 1] = '3';
                                } else {
                                    MSI_CHK10_NO_SEND[len - 1] = '1';
                                }
                            }
                            ScanNative.setHWProperties(MSI_CHK10_NO_SEND);
                        }
                            break;
                        case PropertyID.MSI_CHECK_2_MOD_11: {
                            len = MSI_CHK10_NO_SEND.length;
                            if (value == 0) {
                                MSI_CHK10_NO_SEND[len - 1] = '0';
                            } else {
                                MSI_CHK10_NO_SEND[len - 1] = '2';
                            }
                            ScanNative.setHWProperties(MSI_CHK10_NO_SEND);
                        }
                            break;
                        case PropertyID.MSI_LENGTH1: {
                            byte[] MSIMin;
                            if (value < 10) {
                                MSIMin = new byte[] {
                                        'M', 'S', 'I', 'M', 'I', 'N', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                MSIMin = new byte[] {
                                        'M', 'S', 'I', 'M', 'I', 'N', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(MSIMin);
                        }
                            break;
                        case PropertyID.MSI_LENGTH2: {
                            byte[] MSIMax;
                            if (value < 10) {
                                MSIMax = new byte[] {
                                        'M', 'S', 'I', 'M', 'A', 'X', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                MSIMax = new byte[] {
                                        'M', 'S', 'I', 'M', 'A', 'X', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(MSIMax);
                        }
                            break;
                        case PropertyID.GS1_14_ENABLE: {
                            len = GS1_DataBar.length;
                            if (value == 0) {
                                GS1_DataBar[len - 1] = '0';
                            } else {
                                GS1_DataBar[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(GS1_DataBar);
                        }
                            break;
                        case PropertyID.GS1_14_TO_UPC_EAN: {
                            len = GS1_DataBar_UPCEAN.length;
                            if (value == 0) {
                                GS1_DataBar_UPCEAN[len - 1] = '0';
                            } else {
                                GS1_DataBar_UPCEAN[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(GS1_DataBar_UPCEAN);
                        }
                            break;
                        case PropertyID.GS1_LIMIT_ENABLE: {
                            len = GS1_DataBar_Limited.length;
                            if (value == 0) {
                                GS1_DataBar_Limited[len - 1] = '0';
                            } else {
                                GS1_DataBar_Limited[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(GS1_DataBar_Limited);
                        }
                            break;
                        case PropertyID.GS1_EXP_ENABLE: {
                            len = GS1_DataBar_EXP.length;
                            if (value == 0) {
                                GS1_DataBar_EXP[len - 1] = '0';
                            } else {
                                GS1_DataBar_EXP[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(GS1_DataBar_EXP);
                        }
                            break;
                        case PropertyID.GS1_EXP_LENGTH1: {
                            byte[] RSEMin;
                            if (value < 10) {
                                RSEMin = new byte[] {
                                        'R', 'S', 'E', 'M', 'I', 'N', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                RSEMin = new byte[] {
                                        'R', 'S', 'E', 'M', 'I', 'N', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(RSEMin);
                        }
                            break;
                        case PropertyID.GS1_EXP_LENGTH2: {
                            byte[] RSEMax;
                            if (value < 10) {
                                RSEMax = new byte[] {
                                        'R', 'S', 'E', 'M', 'A', 'X', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                RSEMax = new byte[] {
                                        'R', 'S', 'E', 'M', 'A', 'X', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setHWProperties(RSEMax);
                        }
                            break;
                        case PropertyID.IMAGE_ONE_D_INVERSE:{
                            len = IMAGE_ONE_D_INVERSE.length;
                            if (value == 0) {
                                IMAGE_ONE_D_INVERSE[len - 1] = '0';
                            } else if(value == 1){
                                IMAGE_ONE_D_INVERSE[len - 1] = '1';
                            }  else if(value == 2){
                                IMAGE_ONE_D_INVERSE[len - 1] = '2';
                            }
                            ScanNative.setHWProperties(IMAGE_ONE_D_INVERSE);
                        }
                            break;
                        case PropertyID.US_POSTNET_ENABLE:
                        case PropertyID.US_PLANET_ENABLE:
                        case PropertyID.USPS_4STATE_ENABLE:
                        case PropertyID.UPU_FICS_ENABLE:
                        case PropertyID.ROYAL_MAIL_ENABLE:
                        case PropertyID.KIX_CODE_ENABLE:
                        case PropertyID.AUSTRALIAN_POST_ENABLE:
                        case PropertyID.JAPANESE_POST_ENABLE:
                            if (value == 0) {
                                ScanNative.setHWProperties(DIS_ALL_Postal_CODE);
                                if(mScanService.getPropertyInt(PropertyID.US_POSTNET_ENABLE) == 1) {
                                    ScanNative.setHWProperties(US_POSTNET_ENABLE);
                                }
                                if(mScanService.getPropertyInt(PropertyID.US_PLANET_ENABLE) == 1) {
                                    ScanNative.setHWProperties(US_PLANET_ENABLE);
                                }
                                if(mScanService.getPropertyInt(PropertyID.USPS_4STATE_ENABLE) == 1) {
                                    ScanNative.setHWProperties(USPS_4STATE_ENABLE);
                                }
                                sleep(400);
                                if(mScanService.getPropertyInt(PropertyID.UPU_FICS_ENABLE) == 1) {
                                    ScanNative.setHWProperties(UPU_FICS_ENABLE);
                                }
                                if(mScanService.getPropertyInt(PropertyID.ROYAL_MAIL_ENABLE) == 1) {
                                    ScanNative.setHWProperties(ROYAL_MAIL_ENABLE);
                                }
                                if(mScanService.getPropertyInt(PropertyID.KIX_CODE_ENABLE) == 1) {
                                    ScanNative.setHWProperties(KIX_CODE_ENABLE);
                                }
                                if(mScanService.getPropertyInt(PropertyID.AUSTRALIAN_POST_ENABLE) == 1) {
                                    ScanNative.setHWProperties(AUSTRALIAN_POST_ENABLE);
                                }
                                if(mScanService.getPropertyInt(PropertyID.JAPANESE_POST_ENABLE) == 1) {
                                    ScanNative.setHWProperties(JAPANESE_POST_ENABLE);
                                }
                            } else if(value == 1){
                                if(keyForIndex ==  PropertyID.US_POSTNET_ENABLE)
                                ScanNative.setHWProperties(US_POSTNET_ENABLE);
                                if(keyForIndex ==  PropertyID.US_PLANET_ENABLE)
                                    ScanNative.setHWProperties(US_PLANET_ENABLE);
                                if(keyForIndex ==  PropertyID.USPS_4STATE_ENABLE)
                                    ScanNative.setHWProperties(USPS_4STATE_ENABLE);
                                if(keyForIndex ==  PropertyID.UPU_FICS_ENABLE)
                                    ScanNative.setHWProperties(UPU_FICS_ENABLE);
                                if(keyForIndex ==  PropertyID.ROYAL_MAIL_ENABLE)
                                    ScanNative.setHWProperties(ROYAL_MAIL_ENABLE);
                                if(keyForIndex ==  PropertyID.KIX_CODE_ENABLE)
                                    ScanNative.setHWProperties(KIX_CODE_ENABLE);
                                if(keyForIndex ==  PropertyID.AUSTRALIAN_POST_ENABLE)
                                    ScanNative.setHWProperties(AUSTRALIAN_POST_ENABLE);
                                if(keyForIndex ==  PropertyID.JAPANESE_POST_ENABLE)
                                    ScanNative.setHWProperties(JAPANESE_POST_ENABLE);
                            }
                            break;
                        case  PropertyID.PDF417_ENABLE: {
                            len = PDF417.length;
                            if (value == 0) {
                                PDF417[len - 1] = '0';
                            } else {
                                PDF417[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(PDF417);
                        }
                        break;
                        case  PropertyID.MICROPDF417_ENABLE: {
                            len = MicroPDF417.length;
                            if (value == 0) {
                                MicroPDF417[len - 1] = '0';
                            } else {
                                MicroPDF417[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(MicroPDF417);
                        }
                        break;
                        case  PropertyID.COMPOSITE_CC_C_ENABLE: {
                            len = COMPOSITE_CC_C_ENABLE.length;
                            if (value == 0) {
                                COMPOSITE_CC_C_ENABLE[len - 1] = '0';
                            } else {
                                COMPOSITE_CC_C_ENABLE[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(COMPOSITE_CC_C_ENABLE);
                        }
                        break;
                        case  PropertyID.COMPOSITE_CC_AB_ENABLE: {
                            len = COMPOSITE_CC_AB_ENABLE.length;
                            if (value == 0) {
                                COMPOSITE_CC_AB_ENABLE[len - 1] = '0';
                            } else {
                                COMPOSITE_CC_AB_ENABLE[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(COMPOSITE_CC_AB_ENABLE);
                        }
                        break;
                        case  PropertyID.COMPOSITE_TLC39_ENABLE: {
                            len = COMPOSITE_TLC39_ENABLE.length;
                            if (value == 0) {
                                COMPOSITE_TLC39_ENABLE[len - 1] = '0';
                            } else {
                                COMPOSITE_TLC39_ENABLE[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(COMPOSITE_TLC39_ENABLE);
                        }
                        break;
                        case  PropertyID.HANXIN_ENABLE: {
                            len = HANXIN.length;
                            if (value == 0) {
                                HANXIN[len - 1] = '0';
                            } else {
                                HANXIN[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(HANXIN);
                        }
                        break;
                        case  PropertyID.DATAMATRIX_ENABLE: {
                            len = DATAMatrix.length;
                            if (value == 0) {
                                DATAMatrix[len - 1] = '0';
                            } else {
                                DATAMatrix[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(DATAMatrix);
                        }
                        break;
                        case  PropertyID.MAXICODE_ENABLE: {
                            len = MaxiCode.length;
                            if (value == 0) {
                                MaxiCode[len - 1] = '0';
                            } else {
                                MaxiCode[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(MaxiCode);
                        }
                        break;
                        case  PropertyID.QRCODE_ENABLE: {
                            len = QRCode.length;
                            if (value == 0) {
                                QRCode[len - 1] = '0';
                            } else {
                                QRCode[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(QRCode);
                        }
                        break;
                        case  PropertyID.MICROQRCODE_ENABLE: {
                        }
                        break;
                        case  PropertyID.AZTEC_ENABLE: {
                            len = AztecCode.length;
                            if (value == 0) {
                                AztecCode[len - 1] = '0';
                            } else {
                                AztecCode[len - 1] = '1';
                            }
                            ScanNative.setHWProperties(AztecCode);
                        }
                        break;
                        default:
                            break;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 955 Engine Internally defined param index id value is -1,engine no
     * support the param setting NOTE: some symbology type no find Param id from
     * the 955 user doc, so set -1
     */
    // NOTE temp set some values = SPECIAL_VALUE
    private static final int OPTICON_RESERVED_VALUE = 0x00;

    static class HoneywellParamIndex {
        public static final int IMAGE_EXPOSURE_MODE = RESERVED_VALUE;
        public static final int IMAGE_FIXED_EXPOSURE = RESERVED_VALUE;
        public static final int IMAGE_PICKLIST_MODE = OPTICON_RESERVED_VALUE;
        public static final int IMAGE_ONE_D_INVERSE = OPTICON_RESERVED_VALUE;
        public final static int LASER_ON_TIME = OPTICON_RESERVED_VALUE;// 0x01-0x63 df 0x1e * 100 ms
        public final static int TIMEOUT_BETWEEN_SAME_SYMBOL = OPTICON_RESERVED_VALUE;// 0x01-0x63 // df 0x30
        public final static int LINEAR_CODE_TYPE_SECURITY_LEVEL = OPTICON_RESERVED_VALUE;// 1 2 3 4
        public static final int FUZZY_1D_PROCESSING = OPTICON_RESERVED_VALUE;
        public static final int MULTI_DECODE_MODE = RESERVED_VALUE;
        public static final int BAR_CODES_TO_READ = RESERVED_VALUE;
        public static final int FULL_READ_MODE = RESERVED_VALUE;

        public static final int CODE39_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int CODE39_ENABLE_CHECK = OPTICON_RESERVED_VALUE;
        public static final int CODE39_SEND_CHECK = OPTICON_RESERVED_VALUE;// TODO
        public static final int CODE39_FULL_ASCII = OPTICON_RESERVED_VALUE;
        public static final int CODE39_LENGTH1 = OPTICON_RESERVED_VALUE;
        public static final int CODE39_LENGTH2 = OPTICON_RESERVED_VALUE;

        public static final int TRIOPTIC_ENABLE = OPTICON_RESERVED_VALUE;

        public static final int CODE32_ENABLE = OPTICON_RESERVED_VALUE;// code32
        public static final int CODE32_SEND_CHECK = SPECIAL_VALUE; // TODO 2d 1d
        public static final int CODE32_SEND_START = OPTICON_RESERVED_VALUE;// TODO
        
        public static final int C25_ENABLE = OPTICON_RESERVED_VALUE;
        
        public final static int CODE11_ENABLE = OPTICON_RESERVED_VALUE;
        public final static int CODE11_ENABLE_CHECK = OPTICON_RESERVED_VALUE;
        public final static int CODE11_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public final static int CODE11_LENGTH1 = OPTICON_RESERVED_VALUE;// min 2
        public final static int CODE11_LENGTH2 = OPTICON_RESERVED_VALUE;// max // 14
        public static final int D25_ENABLE = OPTICON_RESERVED_VALUE;// TODO 2d
        public static final int D25_ENABLE_CHECK = SPECIAL_VALUE; // TODO 2d 1d
        public static final int D25_SEND_CHECK = SPECIAL_VALUE; // TODO 2d 1d
        public static final int D25_2_BAR_START = SPECIAL_VALUE;
        public static final int D25_LENGTH1 = OPTICON_RESERVED_VALUE;// TODO 2d
        public static final int D25_LENGTH2 = OPTICON_RESERVED_VALUE;// TODO 2d
                                                                     // 1d
        public static final int M25_ENABLE = OPTICON_RESERVED_VALUE;// TODO 2d
        public static final int M25_ENABLE_CHECK = SPECIAL_VALUE;// TODO 2d
        public static final int M25_SEND_CHECK = SPECIAL_VALUE;// TODO 2d
        public static final int M25_LENGTH1 = OPTICON_RESERVED_VALUE;// TODO 2d
        public static final int M25_LENGTH2 = OPTICON_RESERVED_VALUE;// TODO 2d
        
        public static final int I25_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int I25_ENABLE_CHECK = OPTICON_RESERVED_VALUE;
        public static final int I25_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public static final int I25_CASE_CODE = SPECIAL_VALUE;
        public final static int I25_TO_EAN13 = OPTICON_RESERVED_VALUE;
        public static final int I25_LENGTH1 = OPTICON_RESERVED_VALUE;
        public static final int I25_LENGTH2 = OPTICON_RESERVED_VALUE;
        
        public static final int CODABAR_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int CODABAR_ENABLE_CHECK = OPTICON_RESERVED_VALUE;
        public static final int CODABAR_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public static final int CODABAR_SEND_START = OPTICON_RESERVED_VALUE;
        public static final int CODABAR_CLSI = OPTICON_RESERVED_VALUE;
        public static final int CODABAR_NOTIS = OPTICON_RESERVED_VALUE;
        public static final int CODABAR_WIDE_GAPS = SPECIAL_VALUE;
        public static final int CODABAR_LENGTH1 = OPTICON_RESERVED_VALUE;
        public static final int CODABAR_LENGTH2 = OPTICON_RESERVED_VALUE;
        
        public static final int CODE93_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int CODE93_LENGTH1 = OPTICON_RESERVED_VALUE;
        public static final int CODE93_LENGTH2 = OPTICON_RESERVED_VALUE;
        
        public static final int CODE128_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int CODE128_ENABLE_GS1_128 = OPTICON_RESERVED_VALUE;
        public static final int CODE128_EXT_ASCII = SPECIAL_VALUE;
        public static final int CODE128_LENGTH1 = OPTICON_RESERVED_VALUE;
        public static final int CODE128_LENGTH2 = OPTICON_RESERVED_VALUE;
        public static final int CODE_ISBT_128 = OPTICON_RESERVED_VALUE;
        
        public static final int CODE128_GS1_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int CODE128_GS1_LENGTH1 = OPTICON_RESERVED_VALUE;
        public static final int CODE128_GS1_LENGTH2 = OPTICON_RESERVED_VALUE;
        
        public static final int UPCA_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int UPCA_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public static final int UPCA_SEND_SYS = OPTICON_RESERVED_VALUE;
        public static final int UPCA_TO_EAN13 = OPTICON_RESERVED_VALUE;
        
        public static final int UPCE_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int UPCE_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public static final int UPCE_SEND_SYS = OPTICON_RESERVED_VALUE;
        public static final int UPCE_TO_UPCA = OPTICON_RESERVED_VALUE;
        
        public final static int UPCE1_ENABLE = OPTICON_RESERVED_VALUE;
        public final static int UPCE1_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public final static int UPCE1_SEND_SYS = OPTICON_RESERVED_VALUE;
        public final static int UPCE1_TO_UPCA = OPTICON_RESERVED_VALUE;
        
        public static final int EAN13_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int EAN13_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public static final int EAN13_SEND_SYS = SPECIAL_VALUE;
        public static final int EAN13_TO_ISBN = OPTICON_RESERVED_VALUE;
        public static final int EAN13_TO_ISSN = OPTICON_RESERVED_VALUE;
        public final static int EAN13_BOOKLANDEAN = OPTICON_RESERVED_VALUE;// BOOKLANDEAN
        public final static int EAN13_BOOKLAND_FORMAT = OPTICON_RESERVED_VALUE;// df
                                                                               // 0x00
                                                                               // ISBN-10;0x01
                                                                               // ISBN-13
        public static final int EAN8_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int EAN8_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public static final int EAN8_TO_EAN13 = OPTICON_RESERVED_VALUE;
        
        public static final int EAN_EXT_ENABLE_2_5_DIGIT = OPTICON_RESERVED_VALUE;// TODO
        public final static int UPC_EAN_SECURITY_LEVEL = OPTICON_RESERVED_VALUE;// 0
        public final static int UCC_COUPON_EXT_CODE = OPTICON_RESERVED_VALUE;
        
        public static final int MSI_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int MSI_REQUIRE_2_CHECK = OPTICON_RESERVED_VALUE;
        public static final int MSI_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public static final int MSI_CHECK_2_MOD_11 = OPTICON_RESERVED_VALUE;
        public static final int MSI_LENGTH1 = OPTICON_RESERVED_VALUE;
        public static final int MSI_LENGTH2 = OPTICON_RESERVED_VALUE;
        
        public static final int GS1_14_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int GS1_14_TO_UPC_EAN = OPTICON_RESERVED_VALUE; // RESERVED_VALUE;//0xf0<<8|0x8d;//TODO-
        public static final int GS1_14_TO_GS1_128 = SPECIAL_VALUE;
        public static final int GS1_LIMIT_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int GS1_LIMIT_LEVEL = RESERVED_VALUE;
        public static final int GS1_LIMIT_TO_GS1_128 = SPECIAL_VALUE;
        public static final int GS1_LIMIT_REQUIRE_2D = SPECIAL_VALUE;
        public static final int GS1_EXP_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int GS1_EXP_TO_GS1_128 = SPECIAL_VALUE;
        public static final int GS1_EXP_REQUIRE_2D = RESERVED_VALUE;
        public static final int GS1_EXP_LENGTH1 = OPTICON_RESERVED_VALUE;
        public static final int GS1_EXP_LENGTH2 = OPTICON_RESERVED_VALUE;
        
        public static final int US_POSTNET_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int US_PLANET_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int US_POSTAL_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public static final int USPS_4STATE_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int UPU_FICS_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int ROYAL_MAIL_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int ROYAL_MAIL_SEND_CHECK = OPTICON_RESERVED_VALUE;
        public static final int AUSTRALIAN_POST_ENABLE = OPTICON_RESERVED_VALUE;
        /*• *0 - Autodiscriminate (or Smart mode) - Attempt to decode the Customer Information Field using the N
        and C Encoding Tables.
        • 1 - Raw Format - Output raw bar patterns as a series of numbers 0 through 3.
        • 2 - Alphanumeric Encoding - Decode the Customer Information Field using the C Encoding Table.
        • 3 - Numeric Encoding - Decode the Customer Information Field using the N Encoding Table.*/

        public static final int AUSTRALIAN_POST_FORMAT = OPTICON_RESERVED_VALUE;
        public static final int KIX_CODE_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int JAPANESE_POST_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int PDF417_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int PDF417_LENGTH1 = RESERVED_VALUE;
        public static final int PDF417_LENGTH2 = RESERVED_VALUE;
        public static final int MICROPDF417_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int MICROPDF417_LENGTH1 = RESERVED_VALUE;
        public static final int MICROPDF417_LENGTH2 = RESERVED_VALUE;
        public static final int COMPOSITE_CC_AB_ENABLE = OPTICON_RESERVED_VALUE; // composite-cc_ab
        public static final int COMPOSITE_CC_AB_LENGTH1 = RESERVED_VALUE;
        public static final int COMPOSITE_CC_AB_LENGTH2 = RESERVED_VALUE;
        public static final int COMPOSITE_CC_C_ENABLE = OPTICON_RESERVED_VALUE; // composite-cc_c
        public static final int COMPOSITE_CC_C_LENGTH1 = RESERVED_VALUE;
        public static final int COMPOSITE_CC_C_LENGTH2 = RESERVED_VALUE;
        public final static int COMPOSITE_TLC39_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int HANXIN_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int HANXIN_INVERSE = OPTICON_RESERVED_VALUE;
        public static final int DATAMATRIX_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int DATAMATRIX_LENGTH1 = RESERVED_VALUE;
        public static final int DATAMATRIX_LENGTH2 = RESERVED_VALUE;
        public static final int DATAMATRIX_INVERSE = OPTICON_RESERVED_VALUE;
        public static final int MAXICODE_ENABLE = OPTICON_RESERVED_VALUE;
        public static final int MAXICODE_LENGTH1 = RESERVED_VALUE;
        public static final int MAXICODE_LENGTH2 = RESERVED_VALUE;
        public static final int QRCODE_ENABLE = OPTICON_RESERVED_VALUE; // 2d
        public static final int QRCODE_INVERSE = OPTICON_RESERVED_VALUE;
        public static final int MICROQRCODE_ENABLE = OPTICON_RESERVED_VALUE; // 2d
        public static final int AZTEC_ENABLE = OPTICON_RESERVED_VALUE;            //2d
        public static final int AZTEC_INVERSE = OPTICON_RESERVED_VALUE;
        public static final int DEC_PICKLIST_AIM_DELAY = OPTICON_RESERVED_VALUE;
        public static final int DEC_2D_LIGHTS_MODE = OPTICON_RESERVED_VALUE;
        public static final int DEC_PICKLIST_AIM_MODE = OPTICON_RESERVED_VALUE;
        public static final int DEC_2D_CENTERING_MODE = OPTICON_RESERVED_VALUE;
    }
   
    private final int[] VALUE_PARAM_INDEX = {
            HoneywellParamIndex.IMAGE_EXPOSURE_MODE ,
            HoneywellParamIndex.IMAGE_FIXED_EXPOSURE,
            HoneywellParamIndex.IMAGE_PICKLIST_MODE,
            HoneywellParamIndex.IMAGE_ONE_D_INVERSE,
            HoneywellParamIndex.LASER_ON_TIME,
            HoneywellParamIndex.TIMEOUT_BETWEEN_SAME_SYMBOL,
            HoneywellParamIndex.LINEAR_CODE_TYPE_SECURITY_LEVEL,
            HoneywellParamIndex.FUZZY_1D_PROCESSING,
            HoneywellParamIndex.MULTI_DECODE_MODE,
            HoneywellParamIndex.BAR_CODES_TO_READ,
            HoneywellParamIndex.FULL_READ_MODE,
            HoneywellParamIndex.CODE39_ENABLE,
            HoneywellParamIndex.CODE39_ENABLE_CHECK,
            HoneywellParamIndex.CODE39_SEND_CHECK,
            HoneywellParamIndex.CODE39_FULL_ASCII,
            HoneywellParamIndex.CODE39_LENGTH1,
            HoneywellParamIndex.CODE39_LENGTH2,
            HoneywellParamIndex.TRIOPTIC_ENABLE,
            HoneywellParamIndex.CODE32_ENABLE,
            HoneywellParamIndex.CODE32_SEND_START,
            HoneywellParamIndex.C25_ENABLE,
            HoneywellParamIndex.D25_ENABLE, 
            HoneywellParamIndex.D25_LENGTH1,
            HoneywellParamIndex.D25_LENGTH2,
            HoneywellParamIndex.M25_ENABLE,
            HoneywellParamIndex.CODE11_ENABLE,
            HoneywellParamIndex.CODE11_ENABLE_CHECK,
            HoneywellParamIndex.CODE11_SEND_CHECK,
            HoneywellParamIndex.CODE11_LENGTH1,
            HoneywellParamIndex.CODE11_LENGTH2,
            HoneywellParamIndex.I25_ENABLE,
            HoneywellParamIndex.I25_ENABLE_CHECK,
            HoneywellParamIndex.I25_SEND_CHECK,
            HoneywellParamIndex.I25_LENGTH1,
            HoneywellParamIndex.I25_LENGTH2,
            HoneywellParamIndex.I25_TO_EAN13,
            HoneywellParamIndex.CODABAR_ENABLE,
            HoneywellParamIndex.CODABAR_NOTIS,
            HoneywellParamIndex.CODABAR_CLSI,
            HoneywellParamIndex.CODABAR_LENGTH1,
            HoneywellParamIndex.CODABAR_LENGTH2,
            HoneywellParamIndex.CODE93_ENABLE,
            HoneywellParamIndex.CODE93_LENGTH1,
            HoneywellParamIndex.CODE93_LENGTH2,
            HoneywellParamIndex.CODE128_ENABLE,
            HoneywellParamIndex.CODE128_LENGTH1,
            HoneywellParamIndex.CODE128_LENGTH2,
            HoneywellParamIndex.CODE_ISBT_128,
            HoneywellParamIndex.CODE128_GS1_ENABLE,
            HoneywellParamIndex.UPCA_ENABLE, 
            HoneywellParamIndex.UPCA_SEND_CHECK,
            HoneywellParamIndex.UPCA_SEND_SYS,
            HoneywellParamIndex.UPCA_TO_EAN13,
            HoneywellParamIndex.UPCE_ENABLE,
            HoneywellParamIndex.UPCE_SEND_CHECK,
            HoneywellParamIndex.UPCE_SEND_SYS,
            HoneywellParamIndex.UPCE_TO_UPCA,
            HoneywellParamIndex.UPCE1_ENABLE,
            HoneywellParamIndex.UPCE1_SEND_CHECK,
            HoneywellParamIndex.UPCE1_SEND_SYS,
            HoneywellParamIndex.UPCE1_TO_UPCA,
            HoneywellParamIndex.EAN13_ENABLE,
            //HoneywellParamIndex.EAN13_SEND_CHECK,
            HoneywellParamIndex.EAN13_BOOKLANDEAN,
            HoneywellParamIndex.EAN13_BOOKLAND_FORMAT,
            HoneywellParamIndex.EAN8_ENABLE,
            //HoneywellParamIndex.EAN8_SEND_CHECK,
            HoneywellParamIndex.EAN8_TO_EAN13,
            HoneywellParamIndex.EAN_EXT_ENABLE_2_5_DIGIT,
            HoneywellParamIndex.UPC_EAN_SECURITY_LEVEL,
            HoneywellParamIndex.UCC_COUPON_EXT_CODE,
            HoneywellParamIndex.MSI_ENABLE,
            HoneywellParamIndex.MSI_REQUIRE_2_CHECK,
            HoneywellParamIndex.MSI_SEND_CHECK,
            HoneywellParamIndex.MSI_CHECK_2_MOD_11,
            HoneywellParamIndex.MSI_LENGTH1,
            HoneywellParamIndex.MSI_LENGTH2,
            HoneywellParamIndex.GS1_14_ENABLE,
            HoneywellParamIndex.GS1_14_TO_UPC_EAN,
            HoneywellParamIndex.GS1_LIMIT_ENABLE,
            HoneywellParamIndex.GS1_EXP_ENABLE,
            HoneywellParamIndex.GS1_EXP_LENGTH1,
            HoneywellParamIndex.GS1_EXP_LENGTH2,
            HoneywellParamIndex.US_POSTNET_ENABLE,
            HoneywellParamIndex.US_PLANET_ENABLE,
            HoneywellParamIndex.US_POSTAL_SEND_CHECK,
            HoneywellParamIndex.USPS_4STATE_ENABLE,
            HoneywellParamIndex.UPU_FICS_ENABLE,
            HoneywellParamIndex.ROYAL_MAIL_ENABLE,
            HoneywellParamIndex.ROYAL_MAIL_SEND_CHECK,
            HoneywellParamIndex.AUSTRALIAN_POST_ENABLE,
            HoneywellParamIndex.KIX_CODE_ENABLE,
            HoneywellParamIndex.JAPANESE_POST_ENABLE,
            HoneywellParamIndex.PDF417_ENABLE,
            HoneywellParamIndex.MICROPDF417_ENABLE,
            HoneywellParamIndex.COMPOSITE_CC_AB_ENABLE,
            HoneywellParamIndex.COMPOSITE_CC_C_ENABLE,
            HoneywellParamIndex.COMPOSITE_TLC39_ENABLE,
            HoneywellParamIndex.HANXIN_ENABLE,
            HoneywellParamIndex.HANXIN_INVERSE,
            HoneywellParamIndex.DATAMATRIX_ENABLE,
            HoneywellParamIndex.DATAMATRIX_LENGTH1,
            HoneywellParamIndex.DATAMATRIX_LENGTH2,
            HoneywellParamIndex.DATAMATRIX_INVERSE,
            HoneywellParamIndex.MAXICODE_ENABLE,
            HoneywellParamIndex.QRCODE_ENABLE,
            HoneywellParamIndex.QRCODE_INVERSE,
            HoneywellParamIndex.MICROQRCODE_ENABLE,
            HoneywellParamIndex.AZTEC_ENABLE,
            HoneywellParamIndex.AZTEC_INVERSE,
            HoneywellParamIndex.DEC_2D_LIGHTS_MODE,
            DEC_2D_CENTERING_ENABLE,
            HoneywellParamIndex.DEC_2D_CENTERING_MODE,
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
            HoneywellParamIndex.DEC_PICKLIST_AIM_MODE,
            HoneywellParamIndex.DEC_PICKLIST_AIM_DELAY,
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
    public static final byte[] Idle_Illumination_low = new byte[]{'P','W','R','I','D','L', '7'};//连续扫描时，自动关闭补光
    public static final byte[] Idle_Illumination_med = new byte[]{'P','W','R','I','D','L', '1','5'};//连续扫描时，自动减弱补光
    public static final byte[] Idle_Illumination_h = new byte[]{'P','W','R','I','D','L', '5','0'};
    public static final byte[] SCAN_LED_ON = new byte[]{'S','C','N','L','E','D', '1'};
    public static final byte[] SCAN_LED_OFF = new byte[]{'S','C','N','L','E','D', '0'};
    public static final byte[] SCAN_AIM_ON = new byte[]{'S','C','N','A','I','M','2'};
    public static final byte[] SCAN_AIM_OFF = new byte[]{'S','C','N','A','I','M', '0'};
    public static final byte[] Aimer_Delay = new byte[]{'S','C','N','D','L','Y', '0'};//scndly200 200ms//先显示瞄准红点延时后出补光解码 0-4000ms
    public static final byte[] NOREAD = new byte[]{'S','H','W','N','R','D','0'};//0 or 1
    public static byte[] Centering_DECWIN = new byte[]{'D','E','C','W','I','N','0'};//0 or 1
    public static final byte[] Centering_DECTOP = new byte[]{'D','E','C','T','O','P'};//0 or 1
    public static final byte[] Centering_DECBOT = new byte[]{'D','E','C','B','O','T'};//0 or 1
    public static final byte[] Hands_Free_TimeOut = new byte[]{'T','R','G','P','T','O'};//0 or 1
    //0 - 300,000
    //*30,000 ms
    //private static final byte[] rereadDelay = new byte[]{'D','L','Y','R','R','D'};
    private static final byte[][] SecurityLevel = new byte[][]{
        {'P', 'A', 'P', 'L', 'S', '1'},//Low
        {'P', 'A', 'P', 'L', 'S', '2'},//Low/Medium
        {'P', 'A', 'P', 'L', 'S', '3'}, //Medium/High
        {'P', 'A', 'P', 'L', 'S', '4'},//High
    };//Redundancy
    static final byte[] EndableAllSymb = new byte[]{'A','L','L','E','N','A','0'};
    static final byte[] DisableAllSymb = new byte[]{'A','L','L','E','N','A','1'};
    public static final byte[] ConScanMode = new byte[]{'P','A','P','P','S','T'};//Presentation Mode
    public static final byte[] NorScanMode = new byte[]{'P','A','P','H','H','F'};//Manual Trigger Mode 无超时
    //Serial Trigger Mode TRGSTO### trgsto10000.
    //Once the scan engine is in serial trigger mode, the trigger is activated and deactivated by sending the following
    //commands:
    //Activate: SYN T CR
    //Deactivate: SYN U CR
    public static final byte[] SerialScanMode = new byte[]{'T','R','G','S','T','O'};
    //FUZZY_1D_PROCESSING
    //Poor Quality 1D Reading On DECLDI1 
    //*Poor Quality 1D Reading Off DECLDI0 
    //Poor Quality PDF Reading On PDFXPR1 
    //*Poor Quality PDF Reading Off PDFXPR0 

    static byte[] POOR_1D_PROCESSING = new byte[]{'D','E','C','L','D','I', '1'};
    static byte[] POOR_PDF_PROCESSING = new byte[]{'P','D','F','X','P','R', '1'};
    //IMAGE_PICKLIST_MODE
    //Mobile Phone Read Mode
    static byte[] IMAGE_PICKLIST_MODE = new byte[]{'P','A','P','H','H', 'C'};
    
    //IMAGE_ONE_D_INVERSE
    //Video Reverse Only VIDREV1 
    //Video Reverse and Standard Bar Codes VIDREV2 
   //*Video Reverse Off  VIDREV0 
    static byte[] IMAGE_ONE_D_INVERSE = new byte[]{'V', 'I', 'D', 'R', 'E', 'V', '1'};
    static byte[] Codabar = new byte[]{'C', 'B', 'R', 'E', 'N', 'A', '1'};
    //CBRMIN CBRMAX
    //codabar NOTIS
    static byte[] CodabarST = new byte[]{'C', 'B', 'R', 'S', 'S', 'X', '1'};//start/stop characters
    /*
    *No Check Char. CBRCK20
    Validate Modulo 16, But Don’t Transmit  CBRCK21 
    Validate Modulo 16, and Transmit  CBRCK22 
    Validate Modulo 7 CD, But Don’t Transmit  CBRCK23
    Validate Modulo 7 CD, and Transmit  CBRCK24
    Validate CLSI, But Don’t Transmit CBRCK25 
    Validate CLSI, and Transmit CBRCK26 
     */
    static byte[] CodabarCHK = new byte[]{'C', 'B', 'R', 'C', 'K', '2', '0'};//DF 0 Codabar Check Character
    //Codabar Concatenation
   /* *Off CBRCCT0  
    On CBRCCT1  
    Require CBRCCT2
    Concatenation Timeout DLYCCT */
    static byte[] CodabarCCT = new byte[]{'C', 'B', 'R', 'C', 'C', 'T', '0'};//DF 0 Codabar Concatenation
    static byte[] Code39 = new byte[]{'C', '3', '9', 'E', 'N', 'A', '1'};
  //C39MIN C39MAX
    static byte[] Code39FullASCII = new byte[] {'C', '3', '9', 'A', 'S', 'C', '1'};//DF 0
    //C39SSX0 
    static byte[] Code39StartStop = new byte[] {'C', '3', '9', 'S', 'S', 'X', '0'};//DF
    /*
    *No Check Char. C39CK20 
    Validate, But Don’t Transmit C39CK21 
    Validate,and Transmit C39CK22 
    */
    static byte[] Code39CHK = new byte[]{'C', '3', '9', 'C', 'K', '2', '0'};//DF
    //code32 pharmaceutical
    static byte[] Code32 = new byte[]{'C', '3', '9', 'B', '3', '2', '1'};//df 0
    static byte[] I25 = new byte[]{'I', '2', '5', 'E', 'N', 'A', '1'};//df 1
    //I25MIN I25MAX
    /*
     *No Check Char. I25CK20 
     Validate, But Don’t Transmit I25CK21 
     Validate,and Transmit I25CK22 
     */
    static byte[] I25CHK = new byte[]{'I', '2', '5', 'C', 'K', '2', '0'};
    /*NEC 25 code
    *Off N25ENA0 
    On N25ENA1 
  //N25MIN N25MAX
    
     *No Check Char. N25CK20 
     Validate, But Don’t Transmit N25CK21 
     Validate,and Transmit N25CK22 
     */
    static byte[] Code93 = new byte[]{'C', '9', '3', 'E', 'N', 'A', '1'};//DF 1
    //C93MIN C93MAX
    //Straight25/Discrete25
    static byte[] D25 = new byte[]{'R', '2', '5', 'E', 'N', 'A', '0'};//df 0
    //D25MIN D25MAX
    //Straight25 IATA A25ENA0 A25MIN## 
    static byte[] M25 = new byte[]{'X', '2', '5', 'E', 'N', 'A', '0'};//df 0
    //Minimum (1 - 80) *3 X25MIN## 
    //Maximum (1 - 80) X25MAX##  *80 
    /*
    *No Check Char. X25CK20 
    Validate, But Don’t Transmit X25CK21 
    Validate,and Transmit X25CK22 
    */
    static byte[] M25CK = new byte[]{'X', '2', '5', 'C', 'K', '2', '0'};//df 0
    static byte[] Code11 = new byte[]{'C', '1', '1', 'E', 'N', 'A', '0'};//df 0
   //C11 min 3 C11MAX 80
   /* 1 Check Digit C11CK20 6-20
    Required 
    *2 Check Digits C11CK21 6-20
    Required 
    Auto Select Check C11CK22 
    Digits Required */
    static byte[] Code11CK = new byte[]{'C', '1', '1', 'C', 'K', '2', '0'};//df 1
   
     /*Validate and Transmit One Check Digit C11CK23  
    Validate and Transmit Two Check Digits C11CK24  
    Validate and Transmit Auto Select Check Digits  C11CK25 
    */
    
    static byte[] Code11SEND = new byte[]{'C', '1', '1', 'C', 'K', '2', '0'};//df 4
    
    static byte[] Code128 = new byte[]{'1', '2', '8', 'E', 'N', 'A', '1'};//df 1
    //128MIN 3 128MAX 80
    static byte[] ISBT128 = new byte[]{'I', 'S', 'B', 'E', 'N', 'A', '1'};//df 0
    static byte[] GS1_128 = new byte[]{'G', 'S', '1', 'E', 'N', 'A', '1'};//df 1
    //Telepen
    static byte[] Telepen = new byte[]{'T', 'E', 'L', 'E', 'N', 'A', '1'};//df 0
    //upc-a
    static byte[] UPC_A = new byte[]{'U', 'P', 'B', 'E', 'N', 'A', '1'};//df 1
    /*Transmit UPC-A as UPAENA0 
    EAN-13
    *Transmit UPC-A 
    asn UPC-A UPAENA1*/
    static byte[] UPCA_TO_EAN13= new byte[]{'U', 'P', 'A', 'E', 'N', 'A', '1'};//df 1
   /* 
    *Off UPAAD20 
    On UPAAD21 
    UPC-A 5 Digit *Off UPAAD50 
    Addenda 
    On UPAAD51 */
    static byte[] UPC_A_ADD2 = new byte[]{'U', 'P', 'A', 'A', 'D', '2', '1'};
    static byte[] UPC_A_ADD5 = new byte[]{'U', 'P', 'A', 'A', 'D', '5', '1'};
    static byte[] UPC_A_SYS= new byte[]{'U', 'P', 'A', 'N', 'S', 'X', '1'};//df 1
    static byte[] UPC_A_CHK= new byte[]{'U', 'P', 'A', 'C', 'K', 'X', '0'};//df 1
    static byte[] UPC_E = new byte[]{'U', 'P', 'E', 'E', 'N', '0', '1'};//df 1
    /*2 Digit Addenda On UPEAD21 6-42
    *2 Digit Addenda Off UPEAD20 6-42
    5 Digit Addenda On UPEAD51 6-42
    *5 Digit Addenda Off UPEAD50 */
    static byte[] UPC_E_ADD2 = new byte[]{'U', 'P', 'E', 'A', 'D', '2', '1'};
    static byte[] UPC_E_ADD5 = new byte[]{'U', 'P', 'E', 'A', 'D', '5', '1'};
    static byte[] UPC_E_CK = new byte[]{'U', 'P', 'E', 'C', 'K', 'X', '0'};//df 1
    static byte[] UPC_E_SYS= new byte[]{'U', 'P', 'E', 'E', 'X', 'N', '1'};//df 1
    static byte[] UPC_E_Lead_Zero= new byte[]{'U', 'P', 'E', 'N', 'S', 'X', '1'};//df 1
    static byte[] UPC_E_to_UPCA= new byte[]{'U', 'P', 'E', 'E', 'X', 'P', '1'};//df 0
    static byte[] UPCE_E1 = new byte[]{'U','P','E','E','N','1','0'};
    static byte[] EAN13 = new byte[]{'E', '1', '3', 'E', 'N', 'A', '1'};//df 1
    static byte[] EAN13_SEND_CK = new byte[]{'E', '1', '3', 'C', 'K', 'X', '1'};//df 1
    /*2 Digit Addenda On E13AD21 
    *2 Digit Addenda Off E13AD20 
    5 Digit Addenda On E13AD51 
    *5 Digit Addenda Off E13AD50 */
    static byte[] EAN13_ADD2 = new byte[]{'E', '1', '3', 'A', 'D', '2', '1'};
    static byte[] EAN13_ADD5 = new byte[]{'E', '1', '3', 'A', 'D', '5', '1'};
    /*
     ISBN Translate

     *Off E13ISB0  
    On E13ISB1  
    Convert to 13-Digit On E13I131  
    *Convert to 13-Digit off E13I130  
    Reformat On E13IBR1  
    *Reformat Off E13IBR0 
    *
    *ISSN Translate

    * Off E13ISS0 
        On E13ISS1 
       Reformat OnE13ISR1 
     *Reformat Off E13ISR0 
    **/
    static byte[] BooklandEAN_ISBN = new byte[]{'E', '1', '3', 'I', 'S', 'B', '0'};//df 0
    static byte[] BooklandEAN_ISSN = new byte[]{'E', '1', '3', 'I', 'S', 'S', '0'};//df 0
    static byte[] BooklandFormat =new byte[]{'E', '1', '3', 'I', '1', '3', '0'};//df 0
    static byte[] EAN8 = new byte[]{'E', 'A', '8', 'E', 'N', 'A', '1'};//df 1
    /**2 Digit Addenda Off EA8AD20 
    2 Digit Addenda On EA8AD21 
    *5 Digit Addenda Off EA8AD50 
    5 Digit Addenda On EA8AD51 */
    static byte[] EAN8_ADD2 = new byte[]{'E', 'A', '8', 'A', 'D', '2', '1'};//df 0
    static byte[] EAN8_ADD5 = new byte[]{'E', 'A', '8', 'A', 'D', '5', '1'};//df 0
    static byte[] EAN8_CHK = new byte[]{'E', 'A', '8', 'C', 'K', 'X', '1'};//df 1
    static byte[] EAN8_to_EAN13 = new byte[]{'E', 'A', 'N', 'E', 'M', 'U', '4'};//df 1

    static byte[] MSI = new byte[]{'M', 'S', 'I', 'E', 'N', 'A', '0'};//df 0
    //MSIMIN 3 MSIMAX 80
    /*
     *Validate Type 10,but Don’t Transmit   MSICHK0
    Validate Type 10 and  Transmit MSICHK1
    Validate 2 Type 10  Chars, but Don’t Transmit  MSICHK2 
    Validate 2 Type 10 Chars and Transmit  MSICHK3 
    Disable MSI Check Characters MSICHK6
     */
    static byte[] MSI_CHK10_NO_SEND = new byte[]{'M', 'S', 'I', 'C', 'H', 'K', '0'};//df 0
    static byte[] PlesseyCode = new byte[]{'P', 'L', 'S', 'E', 'N', 'A', '1'};
    static byte[] GS1_DataBar = new byte[]{'R', 'S', 'S', 'E', 'N', 'A', '0'};//df 1
    /*GS1-128 Emulation EANEMU1  
    GS1 DataBar EANEMU2  
    Emulation 
    GS1 Code EANEMU3  
    Expansion Off 
    EAN8 to EAN13 EANEMU4  
    Conversion 
    *GS1 Emulation Off EANEMU0*/ 
    static byte[] GS1_DataBar_UPCEAN = new byte[]{'E', 'A', 'N', 'E', 'M', 'U', '0'};//df 1
    static byte[] GS1_DataBar_Limited = new byte[]{'R', 'S', 'L', 'E', 'N', 'A', '0'};//df 1
    static byte[] GS1_DataBar_EXP = new byte[]{'R', 'S', 'E', 'E', 'N', 'A', '0'};//df 1
    //RSEMIN 3 RESMAX 80
    
    static byte[] TripticCode = new byte[]{'T', 'R', 'I', 'E', 'N', 'A', '1'};//df 0
    static byte[] Chinese25 = new byte[]{'C', 'P', 'C', 'E', 'N', 'A', '1'};//df0
    //CPCMIN 3 CPCMAX 80
    static byte[] CodabookA = new byte[]{'C','B','A','E','N','A','0'};
    static byte[] CodabookF= new byte[]{'C','B','F','E','N','A','0'};
    static byte[] PDF417 = new byte[]{'P','D','F','E','N','A','1'};
    static byte[] MicroPDF417 = new byte[]{'P','D','F','M','A','C','1'};
    static byte[] QRCode = new byte[]{'Q','R','C','E','N','A','1'};//and enable microQRcode
    static byte[] DATAMatrix = new byte[]{'I','D','M','E','N','A','1'};//IDMENA0 
    static byte[] MaxiCode = new byte[]{'M','A','X','E','N','A','1'};
    static byte[] AztecCode = new byte[]{'A','Z','T','E','N','A','1'};
    static byte[] HANXIN = new byte[]{'H','X','_','E','N','A','1'};
    static byte[] KoreaPost = new byte[]{'K','P', 'C', 'E', 'N', 'A', '1'};
    //Transmit Check Digit KPCCHK1 7-46
    //*Don’t Transmit Check Digit KPCCHK0 
    static byte[] KoreaPostCK = new byte[]{'K','P', 'C', 'C', 'H', 'K', '1'};
    //Single 2D Postal Codes 
    /*Australian Post On POSTAL1 
    British Post On POSTAL7 
    Canadian Post On POSTAL30 
    Intelligent Mail Bar Code On POSTAL10 
    Japanese Post On POSTAL3 
    KIX Post On POSTAL4 
    Planet Code On POSTAL5 
    Postal-4i On POSTAL9 
    Postnet On POSTAL6
    Postnet with B and B’ Fields On POSTAL11
    infoMail On POSTAL2 
     */
    /*Planet Code Check Digit
    Transmit PLNCKX1 
    *Don’t Transmit PLNCKX0*/
    
    /*Postnet Check Digit
    Transmit NETCKX1 
    *Don’t Transmit NETCKX0 */
    
    //AUSTRALIAN_POST_FORMAT
    /*Bar Output AUSINT0 
    Numeric N Table AUSINT1 
    Alphanumeric C Table AUSINT2
    Combination N and C Tables AUSINT3 
     */
    //2D Postal Codes Off
    static byte[] DIS_ALL_Postal_CODE = new byte[]{'P','O','S','T','A','L','0'};
    static byte[] US_POSTNET_ENABLE = new byte[]{'P','O','S','T','A','L','6'};
    static byte[] US_POSTNET_SEND_CHECK= new byte[]{'N','E','T','C','K','X','1'};//PLNCKX1 
    static byte[] US_PLANET_ENABLE= new byte[]{'P','O','S','T','A','L','5'};
    static byte[] US_PLANET_SEND_CHECK= new byte[]{'P','L','N','C','K','X','1'};//PLNCKX1 
    static byte[] USPS_4STATE_ENABLE= new byte[]{'P','O','S','T','A','L','1','0'};
    static byte[] UPU_FICS_ENABLE= new byte[]{'P','O','S','T','A','L','3','0'};
    static byte[] ROYAL_MAIL_ENABLE= new byte[]{'P','O','S','T','A','L','2'};
    static byte[] ROYAL_MAIL_SEND_CHECK = new byte[]{'P','O','S','T','A','L','4'};
    static byte[] AUSTRALIAN_POST_ENABLE = new byte[]{'P','O','S','T','A','L','1'};
    static byte[] KIX_CODE_ENABLE= new byte[]{'P','O','S','T','A','L','4'};
    static byte[] JAPANESE_POST_ENABLE= new byte[]{'P','O','S','T','A','L','3'};
    /*UPC/EAN Version
    Scan the UPC/EAN Version On bar code to decode GS1 Composite symbols that have a U.P.C. or an EAN linear compo-
    nent. (This does not affect GS1 Composite symbols with a GS1-128 or GS1 linear component.) Default = UPC/EAN Ver-
    sion Off.*/

    //COMPOSITE_CC_AB_ENABLE
    static byte[] COMPOSITE_CC_AB_ENABLE = new byte[]{'C','O','M','U','P','C','1'};
    //COMPOSITE_CC_C_ENABLE
    //GS1 Composite Codes
    static byte[] COMPOSITE_CC_C_ENABLE = new byte[]{'C','O','M','E','N','A','1'};
    //TCIF Linked Code 39 (TLC39)

    static byte[] COMPOSITE_TLC39_ENABLE = new byte[]{'T','3','9','E','N','A','1'};

}
