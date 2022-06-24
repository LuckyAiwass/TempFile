package com.android.server.scanner;

import android.device.ScanNative;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;
import android.util.Log;
import android.util.SparseArray;

import com.android.server.ScanServiceWrapper;

public class DM30Scanner extends SerialScanner {
    private static final String TAG = "DM30Scanner";
    public DM30Scanner(ScanServiceWrapper scanService) {
        super(scanService);
        // TODO Auto-generated constructor stub
        mScanService = scanService;
        mScannerType = 0;//ScannerFactory.TYPE_DM30;
        mBaudrate = 115200;
        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            mPropIndexHashMap.put(INTERNAL_PROPERTY_INDEX[i], VALUE_PARAM_INDEX[i]);
        }
    }
    @Override
    protected boolean onDataReceived() {

        if (mBufOffset < 4)
            return false;

        // for DM30 //data formating startindx(0x02)+ codeid(1 byte) + datalength(xxx bytes) + prefix(0x03) + data + suffix(0x04)
        int startHeader = BytesIndexOf(mBuffer, 0, mBufOffset, (byte) 0x02);
        Log.i(TAG, "----------------------------startHeader=[" + startHeader + "]"
                + " mBufOffset= " + mBufOffset);
        if (startHeader != -1) {
            int endHeader = BytesIndexOf(mBuffer, startHeader, mBufOffset - startHeader,
                    (byte) 0x03);
            Log.i(TAG, "----------------------------endHeader=[" + endHeader + "]");
            if (endHeader != -1) {
                int symboType = mBuffer[startHeader + 1];
                int lenOffset = endHeader - startHeader - 2;
                Log.i(TAG, "----------------------------lenOffset=[" + lenOffset + "]" + " symboType = " + symboType);
                int barcodelen = 0;
                if (lenOffset > 0) {
                    String barcodelenStr = new String(mBuffer, startHeader + 2, lenOffset);
                    Log.i(TAG, "----------------------------barcodelenStr=[" + barcodelenStr + "]");
                    if (barcodelenStr != null) {
                        try {
                            barcodelen = Integer.parseInt(barcodelenStr);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            barcodelen = 0;
                        }
                    }
                } /*else {
                    barcodelen = aasc_to_bcd(mBuffer[startHeader + 2]);
                }*/
                Log.i(TAG, "----------------------------barcodelen=[" + barcodelen + "]");
                /*if (barcodelen < 2) {
                    return false;
                }*/
                // length enough
                if ((barcodelen + startHeader + 3 + lenOffset) < mBufOffset) {
                    if (mBuffer[startHeader + 3 + barcodelen + lenOffset] == 0x04) {

                        byte[] tmp = new byte[barcodelen];
                        for (int i = 0; i < barcodelen; ++i) {
                            tmp[i] = mBuffer[endHeader + 1 + i];
                        }
                        sendBroadcast(tmp, symboType, barcodelen);

                        // 把剩余内容移动到缓冲头部
                        // 剩余长度
                       /* int len = mBufOffset - (barcodelen + lenOffset + 4);
                        Log.i(TAG, "-----------next len-------len=[" + len + "]");
                        for (int i = 0; i < len; ++i) {
                            mBuffer[i] = mBuffer[barcodelen + lenOffset + 3 + i];
                        }
                        mBufOffset = len;*/
                        mBufOffset = 0;
                        return true;
                    }
                }

                /*int endETX = BytesIndexOfETX(mBuffer, endHeader, mBufOffset - endHeader, (byte) 0x04, barcodelen);
                Log.i(TAG, "----------------------------endETX=[" + endETX + "]");
                if(endETX != -1) {
                    barcodelen = endETX - endHeader -1;
                    Log.i(TAG, "----------------------------barcodelen=[" + barcodelen + "]" );
                    if (barcodelen < 0)
                        return false;
                    byte[] tmp = new byte[barcodelen];
                    for (int i = 0; i < barcodelen; ++i) {
                        tmp[i] = mBuffer[endHeader + 1 + i];
                        //Log.i(TAG, "----------------------------tmp=[" + tmp[i] + "]");
                    }
                    sendBroadcast(tmp, symboType, barcodelen);

                    // 把剩余内容移动到缓冲头部
                    int len = mBufOffset - (endETX + 1); // 剩余长度
                    Log.i(TAG, "-----------next len-------len=[" + len + "]");
                    for (int i = 0; i < len; ++i) {
                        mBuffer[i] = mBuffer[endETX + 1 + i];
                    }
                    mBufOffset = len;

                    return true;
                }*/
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
    public static byte aasc_to_bcd(byte ucAsc) {
        byte ucBcd;

        if ((ucAsc >= '0') && (ucAsc <= '9'))
            ucBcd = (byte) (ucAsc - '0');
        else if ((ucAsc >= 'A') && (ucAsc <= 'F'))
            ucBcd = (byte) (ucAsc - 'A' + 10);
        else if ((ucAsc >= 'a') && (ucAsc <= 'f'))
            ucBcd = (byte) (ucAsc - 'a' + 10);
        else if ((ucAsc > 0x39) && (ucAsc <= 0x3f))
            ucBcd = (byte) (ucAsc - '0');
        else
            ucBcd = 0x0f;

        return ucBcd;
    }
    private int BytesIndexOfETX(byte[] arr, int offset, int count, byte b, int length) {
        for (int i = offset; i < offset + count; ++i) {
            if (arr[i] == b) {
                int len = i - offset -1;
                Log.i(TAG, "-----------BytesIndexOf len-------len=[" + len + "]");
                if(length > 0 && length == len) {
                    return i;
                }
                return -1;
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
        ScanNative.resetDM30Scanner();
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
                           /*int delay = mScanService.getPropertyInt(PropertyID.DEC_PICKLIST_AIM_DELAY);
                           if (delay < 10) {
                               byte[] aimDelay = new byte[]{'S','C','N','D','L','Y',(byte) (delay + 48), '0', '0'};
                               ScanNative.setDM30Properties(aimDelay);
                           } else {
                               int min = delay / 10 + 48;
                               int mins = delay % 10 + 48;
                               byte[] aimDelay = new byte[]{'S','C','N','D','L','Y', (byte) (min), (byte) (mins), '0', '0'};
                               ScanNative.setDM30Properties(aimDelay);
                           }*/
                        }
                            break;
                            /*case PropertyID.DEC_2D_LIGHTS_MODE: {
                        Log.i(TAG, "setProperties  value  " + value + " keyForIndex = " + keyForIndex);
                            if(value == 0) {
                                ScanNative.setDM30Properties(SCAN_LED_OFF);
                                sleep(400);
                                ScanNative.setDM30Properties(SCAN_AIM_OFF);
                                sleep(400);
                                ScanNative.setDM30Properties(SCAN_LED_OFF);
                            } else if(value == 1) {
                                ScanNative.setDM30Properties(SCAN_LED_OFF);
                                sleep(400);
                                ScanNative.setDM30Properties(SCAN_AIM_ON);
                                sleep(400);
                                ScanNative.setDM30Properties(SCAN_LED_OFF);
                            } else if(value == 2) {
                                ScanNative.setDM30Properties(SCAN_LED_ON);
                                sleep(400);;
                                ScanNative.setDM30Properties(SCAN_AIM_OFF);
                                sleep(400);
                                ScanNative.setDM30Properties(SCAN_LED_ON);
                            } else if(value == 3 || value == 4) {
                                ScanNative.setDM30Properties(SCAN_LED_ON);
                                sleep(400);
                                ScanNative.setDM30Properties(SCAN_AIM_ON);
                                sleep(400);
                                ScanNative.setDM30Properties(SCAN_LED_ON);
                            }
                            if(value == 1 || 3 == value || 4 == value) {
                                int delay = mScanService.getPropertyInt(PropertyID.DEC_PICKLIST_AIM_DELAY);
                                if (delay < 10) {
                                    byte[] aimDelay = new byte[]{'S','C','N','D','L','Y',(byte) (delay + 48), '0', '0'};
                                    ScanNative.setDM30Properties(aimDelay);
                                } else {
                                    int min = delay / 10 + 48;
                                    int mins = delay % 10 + 48;
                                    byte[] aimDelay = new byte[]{'S','C','N','D','L','Y', (byte) (min), (byte) (mins), '0', '0'};
                                    ScanNative.setDM30Properties(aimDelay);
                                }
                            } else {
                                byte[] aimDelay = new byte[]{'S','C','N','D','L','Y','0'};
                                ScanNative.setDM30Properties(aimDelay);
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
                                    if(value == 1 && mode != 0) {
                                        len = Centering_DECWIN.length;
                                        Centering_DECWIN[len -1] = '1';
                                        ScanNative.setDM30Properties(Centering_DECWIN);
                                        sleep(400);
                                        ScanNative.setDM30Properties(Centering_DECWIN);
                                        sleep(400);
                                        ScanNative.setDM30Properties(Centering_DECTOP);
                                        if(mode == 1) {
                                            ScanNative.setDM30Properties(Centering_DECTOP);
                                        } else if(mode == 2) {
                                            ScanNative.setDM30Properties(Centering_DECBOT);
                                        }
                                    } else {
                                        ScanNative.setDM30Properties(Centering_DECWIN);
                                    }
                                }
                            } else {
                                if(value == 1) {
                                    ScanNative.setDM30Properties(Centering_DECTOP);
                                } else if(value == 2) {
                                    ScanNative.setDM30Properties(Centering_DECBOT);
                                }
                            }
                        }
                            break;
                        case PropertyID.DEC_PICKLIST_AIM_DELAY: {
                            int aim_mode = mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE);
                            if ( aim_mode == 1 || 3 == aim_mode || 4 == aim_mode) {
                                if (value < 10) {
                                    byte[] aimDelay = new byte[]{'S','C','N','D','L','Y',(byte) (value + 48), '0', '0'};
                                    ScanNative.setDM30Properties(aimDelay);
                                } else {
                                    int min = value / 10 + 48;
                                    int mins = value % 10 + 48;
                                    byte[] aimDelay = new byte[]{'S','C','N','D','L','Y', (byte) (min), (byte) (mins), '0', '0'};
                                    ScanNative.setDM30Properties(aimDelay);
                                }
                            }
                        }
                            break;
                        case PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL: {
                            if (value < 10) {
                                byte[] rereadDelay = new byte[]{'D','L','Y','R','R','D', (byte) (value + 48), '0', '0'};
                                ScanNative.setDM30Properties(rereadDelay);
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                byte[] rereadDelay = new byte[]{'D','L','Y','R','R','D', (byte) (min), (byte) (mins), '0', '0'};
                                ScanNative.setDM30Properties(rereadDelay);
                            }
                        }
                            break;
                        case PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL: {
                            ScanNative.setDM30Properties(SecurityLevel[value - 1]);
                        }
                            break;*/
                        case PropertyID.IMAGE_PICKLIST_MODE:{
                            if (mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == Triggering.CONTINUOUS.toInt()) {
                                /*if (value == 0) {
                                    ScanNative.setDM30Properties(PresentationMode);
                                } else {
                                    ScanNative.setDM30Properties(PresentationModePhone);
                                }*/
                            } else {
                                if (value == 0) {
                                    ScanNative.setDM30Properties(TriggerMode);
                                } else {
                                    ScanNative.setDM30Properties(TriggerModePhone);
                                }
                            }
                        }
                        break;
                        /*case PropertyID.FUZZY_1D_PROCESSING:{
                            len = POOR_1D_PROCESSING.length;
                            if (value == 0) {
                                POOR_1D_PROCESSING[len - 1] = '0';
                            } else {
                                POOR_1D_PROCESSING[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(POOR_1D_PROCESSING);
                        }
                        break;*/
                        case PropertyID.CODE39_ENABLE: {
                            len = SYM_CODE39.length;
                            if (value == 0) {
                                SYM_CODE39[len - 1] = '0';
                            } else {
                                SYM_CODE39[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_CODE39);
                        }
                            break;
                        case PropertyID.CODE39_ENABLE_CHECK: {
                            len = SYM_CODE39_SEND_CK.length;
                            if (value == 0) {
                                SYM_CODE39_SEND_CK[len - 1] = '0';
                            } else {
                                if (mScanService.getPropertyInt(PropertyID.CODE39_SEND_CHECK) == 1) {
                                    SYM_CODE39_SEND_CK[len - 1] = '2';
                                } else {
                                    SYM_CODE39_SEND_CK[len - 1] = '1';
                                }
                            }
                            ScanNative.setDM30Properties(SYM_CODE39_SEND_CK);
                        }
                            break;
                        case PropertyID.CODE39_SEND_CHECK: {
                            len = SYM_CODE39_SEND_CK.length;
                            if (value == 0) {
                                if (mScanService.getPropertyInt(PropertyID.CODE39_ENABLE_CHECK) == 1) {
                                    SYM_CODE39_SEND_CK[len - 1] = '1';
                                } else {
                                    SYM_CODE39_SEND_CK[len - 1] = '0';
                                }
                            } else {
                                SYM_CODE39_SEND_CK[len - 1] = '2';
                            }
                            ScanNative.setDM30Properties(SYM_CODE39_SEND_CK);
                        }
                            break;
                        case PropertyID.CODE39_FULL_ASCII: {
                            len = SYM_CODE39_ASCLL.length;
                            if (value == 0) {
                                SYM_CODE39_ASCLL[len - 1] = '0';
                            } else {
                                SYM_CODE39_ASCLL[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_CODE39_ASCLL);
                        }
                            break;
                        case PropertyID.CODE39_LENGTH1: {
                            byte[] code39Min;
                            if (value < 10) {
                                code39Min = new byte[] {
                                        '0', '2', '0', '3', '0', '7', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                code39Min = new byte[] {
                                        '0', '2', '0', '3', '0', '7', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(code39Min);
                        }
                            break;
                        case PropertyID.CODE39_LENGTH2: {
                            byte[] code39Max;
                            if (value < 10) {
                                code39Max = new byte[] {
                                        '0', '2', '0', '3', '0', '8', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                code39Max = new byte[] {
                                        '0', '2', '0', '3', '0', '8', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(code39Max);
                        }
                            break;
                        case PropertyID.TRIOPTIC_ENABLE: {
                            /*len = TripticCode.length;
                            if (value == 0) {
                                TripticCode[len - 1] = '0';
                            } else {
                                TripticCode[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(TripticCode);*/
                        }
                            break;
                        case PropertyID.CODE32_ENABLE: {
                            /*len = Code32.length;
                            if (value == 0) {
                                Code32[len - 1] = '0';
                            } else {
                                Code32[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(Code32);*/
                        }
                            break;
                        case PropertyID.CODE32_SEND_START: {
                            len = SYM_CODE39_SEND_SS.length;
                            if (value == 0) {
                                SYM_CODE39_SEND_SS[len - 1] = '0';
                            } else {
                                SYM_CODE39_SEND_SS[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_CODE39_SEND_SS);
                        }
                            break;
                        case PropertyID.C25_ENABLE: {
                            len = SYM_CHINA_POSTAL.length;
                            if (value == 0) {
                                SYM_CHINA_POSTAL[len - 1] = '0';
                            } else {
                                SYM_CHINA_POSTAL[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_CHINA_POSTAL);
                        }
                            break;
                        case PropertyID.D25_ENABLE: {
                            len = SYM_Industrial25.length;
                            if (value == 0) {
                                SYM_Industrial25[len - 1] = '0';
                            } else {
                                SYM_Industrial25[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_Industrial25);
                        }
                            break;
                        case PropertyID.D25_LENGTH1: {
                            byte[] D25Min;
                            if (value < 10) {
                                D25Min = new byte[] {
                                        '0', '2', '0', '6', '0', '2', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                D25Min = new byte[] {
                                        '0', '2', '0', '6', '0', '2', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(D25Min);
                        }
                            break;
                        case PropertyID.D25_LENGTH2: {
                            byte[] D25Max;
                            if (value < 10) {
                                D25Max = new byte[] {
                                        '0', '2', '0', '6', '0', '3',  (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                D25Max = new byte[] {
                                        '0', '2', '0', '6', '0', '3',  (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(D25Max);
                        }
                            break;
                        case PropertyID.M25_ENABLE: {
                            len = SYM_Matrix25.length;
                            if (value == 0) {
                                SYM_Matrix25[len - 1] = '0';
                            } else {
                                SYM_Matrix25[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_Matrix25);
                        }
                            break;
                        case PropertyID.CODE11_ENABLE: {
                            /*len = Code11.length;
                            if (value == 0) {
                                Code11[len - 1] = '0';
                            } else {
                                Code11[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(Code11);*/
                        }
                            break;
                        case PropertyID.CODE11_ENABLE_CHECK:{
                            /*len = Code11CK.length;
                            if (value == 0) {
                                Code11CK[len - 1] = '2';
                            } else {
                                Code11CK[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(Code11CK);*/
                        }
                            break;
                        case PropertyID.CODE11_SEND_CHECK:{
                            /*len = Code11SEND.length;
                            if (value == 0) {
                                Code11SEND[len - 1] = '5';
                            } else {
                                Code11SEND[len - 1] = '4';
                            }
                            ScanNative.setDM30Properties(Code11SEND);*/
                        }
                            break;
                        case PropertyID.CODE11_LENGTH1: {
                            /*byte[] C11Min;
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
                            ScanNative.setDM30Properties(C11Min);*/
                        }
                            break;
                        case PropertyID.CODE11_LENGTH2: {
                           /* byte[] C11Max;
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
                            ScanNative.setDM30Properties(C11Max);*/
                        }
                            break;
                        case PropertyID.I25_ENABLE: {
                            len = SYM_I25.length;
                            if (value == 0) {
                                SYM_I25[len - 1] = '0';
                            } else {
                                SYM_I25[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_I25);
                        }
                            break;
                        case PropertyID.I25_ENABLE_CHECK: {
                            len = SYM_I25_SEND_CK.length;
                            if (value == 0) {
                                SYM_I25_SEND_CK[len - 1] = '0';
                            } else {
                                if (mScanService.getPropertyInt(PropertyID.I25_SEND_CHECK) == 1) {
                                    SYM_I25_SEND_CK[len - 1] = '2';
                                } else {
                                    SYM_I25_SEND_CK[len - 1] = '1';
                                }
                            }
                            ScanNative.setDM30Properties(SYM_I25_SEND_CK);
                        }
                            break;
                        case PropertyID.I25_SEND_CHECK: {
                            len = SYM_I25_SEND_CK.length;
                            if (value == 0) {
                                if (mScanService.getPropertyInt(PropertyID.I25_ENABLE_CHECK) == 1) {
                                    SYM_I25_SEND_CK[len - 1] = '1';
                                } else {
                                    SYM_I25_SEND_CK[len - 1] = '2';
                                }
                            } else {
                                SYM_I25_SEND_CK[len - 1] = '2';
                            }
                            ScanNative.setDM30Properties(SYM_I25_SEND_CK);
                        }
                            break;
                        case PropertyID.I25_LENGTH1: {
                            byte[] I25Min;
                            if (value < 10) {
                                I25Min = new byte[] {
                                        '0', '2', '0', '4', '0', '3', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                I25Min = new byte[] {
                                        '0', '2', '0', '4', '0', '3',(byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(I25Min);
                        }
                            break;
                        case PropertyID.I25_LENGTH2: {
                            byte[] I25Max;
                            if (value < 10) {
                                I25Max = new byte[] {
                                        '0', '2', '0', '4', '0', '4',(byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                I25Max = new byte[] {
                                        '0', '2', '0', '4', '0', '4',(byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(I25Max);
                        }
                            break;
                        case PropertyID.I25_TO_EAN13: {
                        }
                            break;
                        case PropertyID.CODABAR_ENABLE: {
                            len = SYM_CODABAR.length;
                            if (value == 0) {
                                SYM_CODABAR[len - 1] = '0';
                            } else {
                                SYM_CODABAR[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_CODABAR);
                        }
                            break;
                        case PropertyID.CODABAR_NOTIS: {
                            len = SYM_CODABAR_SEND_SS.length;
                            if (value == 0) {
                                SYM_CODABAR_SEND_SS[len - 1] = '0';
                            } else {
                                SYM_CODABAR_SEND_SS[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_CODABAR_SEND_SS);
                        }
                            break;
                        case PropertyID.CODABAR_CLSI:{
                            len = SYM_CODABAR_SEND_CK.length;
                            if (value == 0) {
                                SYM_CODABAR_SEND_CK[len - 1] = '0';
                            } else {
                                SYM_CODABAR_SEND_CK[len - 1] = '2';
                            }
                            ScanNative.setDM30Properties(SYM_CODABAR_SEND_CK);
                        }
                            break;
                        case PropertyID.CODABAR_LENGTH1: {
                            byte[] CBRMin;
                            if (value < 10) {
                                CBRMin = new byte[] {
                                        '0', '2', '0', '2', '0', '5', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                CBRMin = new byte[] {
                                        '0', '2', '0', '2', '0', '5', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(CBRMin);
                        }
                            break;
                        case PropertyID.CODABAR_LENGTH2: {
                            byte[] CBRMax;
                            if (value < 10) {
                                CBRMax = new byte[] {
                                        '0', '2', '0', '2', '0', '6', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                CBRMax = new byte[] {
                                        '0', '2', '0', '2', '0', '6', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(CBRMax);
                        }
                            break;
                        case PropertyID.CODE93_ENABLE: {
                            len = SYM_CODE93.length;
                            if (value == 0) {
                                SYM_CODE93[len - 1] = '0';
                            } else {
                                SYM_CODE93[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_CODE93);
                        }
                            break;
                        case PropertyID.CODE93_LENGTH1: {
                            byte[] C93Min;
                            if (value < 10) {
                                C93Min = new byte[] {
                                        '0', '2', '0', 'D', '0', '2', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                C93Min = new byte[] {
                                        '0', '2', '0', 'D', '0', '2', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(C93Min);
                        }
                            break;
                        case PropertyID.CODE93_LENGTH2: {
                            byte[] C93Max;
                            if (value < 10) {
                                C93Max = new byte[] {
                                        '0', '2', '0', 'D', '0', '3', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                C93Max = new byte[] {
                                        '0', '2', '0', 'D', '0', '3',(byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(C93Max);
                        }
                            break;
                        case PropertyID.CODE128_ENABLE: {
                            len = SYM_CODE128.length;
                            if (value == 0) {
                                SYM_CODE128[len - 1] = '0';
                            } else {
                                SYM_CODE128[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_CODE128);
                        }
                            break;
                        case PropertyID.CODE128_LENGTH1: {
                            byte[] C128Min;
                            if (value < 10) {
                                C128Min = new byte[] {
                                        '0', '2', '0', 'A', '0', '2', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                C128Min = new byte[] {
                                        '0', '2', '0', 'A', '0', '2',  (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(C128Min);
                        }
                            break;
                        case PropertyID.CODE128_LENGTH2: {
                            byte[] C128Max;
                            if (value < 10) {
                                C128Max = new byte[] {
                                        '0', '2', '0', 'A', '0', '3', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                C128Max = new byte[] {
                                        '0', '2', '0', 'A', '0', '3', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(C128Max);
                        }
                            break;
                        case PropertyID.CODE_ISBT_128: {
                           /* len = ISBT128.length;
                            if (value == 0) {
                                ISBT128[len - 1] = '0';
                            } else {
                                ISBT128[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(ISBT128);*/
                        }
                            break;
                        case PropertyID.CODE128_GS1_ENABLE: {
                            len = SYM_GS1_128.length;
                            if (value == 0) {
                                SYM_GS1_128[len - 1] = '0';
                            } else {
                                SYM_GS1_128[len - 1] = '6';
                            }
                            ScanNative.setDM30Properties(SYM_GS1_128);
                        }
                            break;
                        case PropertyID.UPCA_ENABLE: {
                            len = SYM_UPCA.length;
                            if (value == 0) {
                                SYM_UPCA[len - 1] = '0';
                            } else {
                                SYM_UPCA[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_UPCA);
                        }
                            break;
                        case PropertyID.UPCA_SEND_CHECK: {
                            len = SYM_UPCA_SEND_CK.length;
                            if (value == 0) {
                                SYM_UPCA_SEND_CK[len - 1] = '0';
                            } else {
                                SYM_UPCA_SEND_CK[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_UPCA_SEND_CK);
                        }
                            break;
                        case PropertyID.UPCA_SEND_SYS: {
                            len = SYM_UPCA_Send_SYS.length;
                            if (value == 0) {
                                SYM_UPCA_Send_SYS[len - 1] = '0';
                            } else {
                                SYM_UPCA_Send_SYS[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_UPCA_Send_SYS);
                        }
                            break;
                        case PropertyID.UPCE_ENABLE: {
                            len = SYM_UPCE0.length;
                            if (value == 0) {
                                SYM_UPCE0[len - 1] = '0';
                            } else {
                                SYM_UPCE0[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_UPCE0);
                        }
                            break;
                        case PropertyID.UPCE_SEND_CHECK: {
                            len = SYM_UPCE0_SEND_CK.length;
                            if (value == 0) {
                                SYM_UPCE0_SEND_CK[len - 1] = '0';
                            } else {
                                SYM_UPCE0_SEND_CK[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_UPCE0_SEND_CK);
                        }
                            break;
                        case PropertyID.UPCE_SEND_SYS: {
                            len = SYM_UPCE0_Send_SYS.length;
                            if (value == 0) {
                                SYM_UPCE0_Send_SYS[len - 1] = '0';
                            } else {
                                SYM_UPCE0_Send_SYS[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_UPCE0_Send_SYS);
                        }
                            break;
                        case PropertyID.UPCE_TO_UPCA: {
                            len = SYM_UPCE0_EXPAND.length;
                            if (value == 0) {
                                SYM_UPCE0_EXPAND[len - 1] = '0';
                            } else {
                                SYM_UPCE0_EXPAND[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_UPCE0_EXPAND);
                        }
                            break;
                        case PropertyID.UPCE1_ENABLE:
                        {
                            len = SYM_UPCE1.length;
                            if (value == 0) {
                                SYM_UPCE1[len - 1] = '0';
                            } else {
                                SYM_UPCE1[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_UPCE1);
                        }
                            break;
                        case PropertyID.UPCE1_SEND_CHECK:
                            break;
                        case PropertyID.UPCE1_SEND_SYS:
                            break;
                        case PropertyID.UPCE1_TO_UPCA:
                            break;
                        case PropertyID.EAN13_ENABLE: {
                            len = SYM_EAN13.length;
                            if (value == 0) {
                                SYM_EAN13[len - 1] = '0';
                            } else {
                                SYM_EAN13[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_EAN13);
                        }
                            break;

                        case PropertyID.EAN13_BOOKLANDEAN: {
                            len = SYM_EAN13_ISBN.length;
                            if (value == 0) {
                                SYM_EAN13_ISBN[len - 1] = '0';
                            } else {
                                SYM_EAN13_ISBN[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_EAN13_ISBN);
                        }
                            break;
                        case PropertyID.EAN13_BOOKLAND_FORMAT: {
                            len = SYM_EAN13_ADD_SEP.length;
                            if (value == 0) {
                                SYM_EAN13_ADD_SEP[len - 1] = '0';
                            } else {
                                SYM_EAN13_ADD_SEP[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_EAN13_ADD_SEP);
                        }
                            break;
                        case PropertyID.EAN8_ENABLE: {
                            len = SYM_EAN8.length;
                            if (value == 0) {
                                SYM_EAN8[len - 1] = '0';
                            } else {
                                SYM_EAN8[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_EAN8);
                        }
                            break;
                        case PropertyID.EAN8_TO_EAN13: {
                            len = SYM_EAN8_ADD_REQ.length;
                            if (value == 0) {
                                SYM_EAN8_ADD_REQ[len - 1] = '0';
                            } else {
                                SYM_EAN8_ADD_REQ[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_EAN8_ADD_REQ);
                        }
                            break;
                        case PropertyID.EAN_EXT_ENABLE_2_5_DIGIT: {
                            if (value == 0) {
                                SYM_EAN13_ADD_ON2[6] = '0';
                                ScanNative.setDM30Properties(SYM_EAN13_ADD_ON2);
                                SYM_EAN13_ADD_ON5[6] = '0';
                                ScanNative.setDM30Properties(SYM_EAN13_ADD_ON5);
                                sleep(400);
                                SYM_EAN8_ADD_ON2[6] = '0';
                                ScanNative.setDM30Properties(SYM_EAN8_ADD_ON2);
                                SYM_EAN8_ADD_ON5[6] = '0';
                                ScanNative.setDM30Properties(SYM_EAN8_ADD_ON5);
                                sleep(400);
                                SYM_UPCA_ADD_ON2[6] = '0';
                                ScanNative.setDM30Properties(SYM_UPCA_ADD_ON2);
                                SYM_UPCA_ADD_ON5[6] = '0';
                                ScanNative.setDM30Properties(SYM_UPCA_ADD_ON5);
                                sleep(400);
                                SYM_UPCE0_ADD_ON2[6] = '0';
                                ScanNative.setDM30Properties(SYM_UPCE0_ADD_ON2);
                                SYM_UPCE0_ADD_ON5[6] = '0';
                                ScanNative.setDM30Properties(SYM_UPCE0_ADD_ON5);
                            } else {
                                SYM_EAN13_ADD_ON2[6] = '1';
                                ScanNative.setDM30Properties(SYM_EAN13_ADD_ON2);
                                SYM_EAN13_ADD_ON5[6] = '1';
                                ScanNative.setDM30Properties(SYM_EAN13_ADD_ON5);
                                sleep(400);
                                SYM_EAN8_ADD_ON2[6] = '1';
                                ScanNative.setDM30Properties(SYM_EAN8_ADD_ON2);
                                SYM_EAN8_ADD_ON5[6] = '1';
                                ScanNative.setDM30Properties(SYM_EAN8_ADD_ON5);
                                sleep(400);
                                SYM_UPCA_ADD_ON2[6] = '1';
                                ScanNative.setDM30Properties(SYM_UPCA_ADD_ON2);
                                SYM_UPCA_ADD_ON5[6] = '1';
                                ScanNative.setDM30Properties(SYM_UPCA_ADD_ON5);
                                sleep(400);
                                SYM_UPCE0_ADD_ON2[6] = '1';
                                ScanNative.setDM30Properties(SYM_UPCE0_ADD_ON2);
                                SYM_UPCE0_ADD_ON5[6] = '1';
                                ScanNative.setDM30Properties(SYM_UPCE0_ADD_ON5);
                            }
                        }
                            break;
                        case PropertyID.UPC_EAN_SECURITY_LEVEL:
                            break;
                        case PropertyID.UCC_COUPON_EXT_CODE:
                            break;
                        case PropertyID.MSI_ENABLE: {
                            len = SYM_MSI.length;
                            if (value == 0) {
                                SYM_MSI[len - 1] = '0';
                            } else {
                                SYM_MSI[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_MSI);
                        }
                            break;
                        case PropertyID.MSI_REQUIRE_2_CHECK: {
                            len = SYM_MSI_V1010_NOSEND.length;
                            if (value == 0) {
                                SYM_MSI_V1010_NOSEND[len - 1] = '6';
                            } else {
                                SYM_MSI_V1010_NOSEND[len - 1] = '2';
                            }
                            ScanNative.setDM30Properties(SYM_MSI_V1010_NOSEND);
                        }
                            break;
                        case PropertyID.MSI_SEND_CHECK: {
                            len = SYM_MSI_V1010_NOSEND.length;
                            if (value == 0) {
                                SYM_MSI_V1010_NOSEND[len - 1] = '0';
                            } else {
                                if (mScanService.getPropertyInt(PropertyID.MSI_REQUIRE_2_CHECK) == 1) {
                                    SYM_MSI_V1010_NOSEND[len - 1] = '3';
                                } else {
                                    SYM_MSI_V1010_NOSEND[len - 1] = '2';
                                }
                            }
                            ScanNative.setDM30Properties(SYM_MSI_V1010_NOSEND);
                        }
                            break;
                        case PropertyID.MSI_CHECK_2_MOD_11: {
                            len = SYM_MSI_V1110_NOSEND.length;
                            if (value == 0) {
                                SYM_MSI_V1110_NOSEND[len - 1] = '4';
                            } else {
                                SYM_MSI_V1110_NOSEND[len - 1] = '5';
                            }
                            ScanNative.setDM30Properties(SYM_MSI_V1110_NOSEND);
                        }
                            break;
                        case PropertyID.MSI_LENGTH1: {
                            byte[] MSIMin;
                            if (value < 10) {
                                MSIMin = new byte[] {
                                        '0', '2', '0', 'E', '0', '3', (byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                MSIMin = new byte[] {
                                        '0', '2', '0', 'E', '0', '3', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(MSIMin);
                        }
                            break;
                        case PropertyID.MSI_LENGTH2: {
                            byte[] MSIMax;
                            if (value < 10) {
                                MSIMax = new byte[] {
                                        '0', '2', '0', 'E', '0', '4',(byte) (value + 48)
                                };
                            } else {
                                int min = value / 10 + 48;
                                int mins = value % 10 + 48;
                                MSIMax = new byte[] {
                                        '0', '2', '0', 'E', '0', '4', (byte) (min), (byte) (mins)
                                };
                            }
                            ScanNative.setDM30Properties(MSIMax);
                        }
                            break;
                        case PropertyID.GS1_14_ENABLE: {
                            /*len = GS1_DataBar.length;
                            if (value == 0) {
                                GS1_DataBar[len - 1] = '0';
                            } else {
                                GS1_DataBar[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(GS1_DataBar);*/
                        }
                            break;
                        case PropertyID.GS1_14_TO_UPC_EAN: {
                           /* len = GS1_DataBar_UPCEAN.length;
                            if (value == 0) {
                                GS1_DataBar_UPCEAN[len - 1] = '0';
                            } else {
                                GS1_DataBar_UPCEAN[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(GS1_DataBar_UPCEAN);*/
                        }
                            break;
                        case PropertyID.GS1_LIMIT_ENABLE: {
                            /*len = GS1_DataBar_Limited.length;
                            if (value == 0) {
                                GS1_DataBar_Limited[len - 1] = '0';
                            } else {
                                GS1_DataBar_Limited[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(GS1_DataBar_Limited);*/
                        }
                            break;
                        case PropertyID.GS1_EXP_ENABLE: {
                            /*len = GS1_DataBar_EXP.length;
                            if (value == 0) {
                                GS1_DataBar_EXP[len - 1] = '0';
                            } else {
                                GS1_DataBar_EXP[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(GS1_DataBar_EXP);*/
                        }
                            break;
                        case PropertyID.GS1_EXP_LENGTH1: {
                            /*byte[] RSEMin;
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
                            ScanNative.setDM30Properties(RSEMin);*/
                        }
                            break;
                        case PropertyID.GS1_EXP_LENGTH2: {
                            /*byte[] RSEMax;
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
                            ScanNative.setDM30Properties(RSEMax);*/
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
                            ScanNative.setDM30Properties(IMAGE_ONE_D_INVERSE);
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
                            } else if(value == 1){
                            }
                            break;
                        case  PropertyID.PDF417_ENABLE: {
                            len = SYM_PDF417.length;
                            if (value == 0) {
                                SYM_PDF417[len - 1] = '0';
                            } else {
                                SYM_PDF417[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_PDF417);
                        }
                        break;
                        case  PropertyID.MICROPDF417_ENABLE: {
                            len = SYM_MICRO_PDF417.length;
                            if (value == 0) {
                                SYM_MICRO_PDF417[len - 1] = '0';
                            } else {
                                SYM_MICRO_PDF417[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_MICRO_PDF417);
                        }
                        break;
                        case  PropertyID.COMPOSITE_CC_C_ENABLE: {
                        }
                        break;
                        case  PropertyID.COMPOSITE_CC_AB_ENABLE: {
                        }
                        break;
                        case  PropertyID.COMPOSITE_TLC39_ENABLE: {
                        }
                        break;
                        case  PropertyID.HANXIN_ENABLE: {
                            len = SYM_HANXINCODE.length;
                            if (value == 0) {
                                SYM_HANXINCODE[len - 1] = '0';
                            } else {
                                SYM_HANXINCODE[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_HANXINCODE);
                        }
                        break;
                        case  PropertyID.DATAMATRIX_ENABLE: {
                            len = SYM_DATA_MATRIX.length;
                            if (value == 0) {
                                SYM_DATA_MATRIX[len - 1] = '0';
                            } else {
                                SYM_DATA_MATRIX[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_DATA_MATRIX);
                        }
                        break;
                        case  PropertyID.MAXICODE_ENABLE: {
                            len = SYM_MAXICODE.length;
                            if (value == 0) {
                                SYM_MAXICODE[len - 1] = '0';
                            } else {
                                SYM_MAXICODE[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_MAXICODE);
                        }
                        break;
                        case  PropertyID.QRCODE_ENABLE: {
                            len = SYM_QRCODE.length;
                            if (value == 0) {
                                SYM_QRCODE[len - 1] = '0';
                            } else {
                                SYM_QRCODE[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_QRCODE);
                        }
                        break;
                        case  PropertyID.MICROQRCODE_ENABLE: {
                        }
                        break;
                        case  PropertyID.AZTEC_ENABLE: {
                            len = SYM_AztecCODE.length;
                            if (value == 0) {
                                SYM_AztecCODE[len - 1] = '0';
                            } else {
                                SYM_AztecCODE[len - 1] = '1';
                            }
                            ScanNative.setDM30Properties(SYM_AztecCODE);
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
    public static final byte[] triggerModeTimeout = new byte[]{'0','E','0','1','0','5'};//1-65536/设置值0 表示永不超时，默认1500ms.
    //Barcode Scanning Delay//
    public static final byte[] scanningDelay = new byte[]{'0','8','0','B','0','8','0'};//设置两次成功读取条码时间间隔
    public static final byte[] scanningSameDelay = new byte[]{'0','8','0','B','0','6','0'};//设置同一条码时间间隔
    
    public static final byte[] TriggerMode = new byte[]{'0','9','1','A','0', '0'};
    public static final byte[] PresentationMode = new byte[]{'0','9','0','9','0', '1'};
    public static final byte[] TriggerModePhone = new byte[]{'0','9','1','B','0', '0'};
    public static final byte[] PresentationModePhone = new byte[]{'0','9','0','9','0', '2'};
  //IMAGE_ONE_D_INVERSE
    //Video Reverse Only 024B001 
    //Video Reverse and Standard Bar Codes 024B002 
   //*Video Reverse Off  024B000 
    private static byte[] IMAGE_ONE_D_INVERSE = new byte[]{'0', '2', '4', 'B', '0', '0', '0'};
    //Illumination setting for Trigger Mode
    private static byte[] TM_Illum_OFF = new byte[]{'0', '4', '0', '1', '0', '2', '4'};
    private static byte[] TM_Illum_LOW = new byte[]{'0', '4', '0', '1', '0', '2', '3'};
    private static byte[] TM_Illum_MID = new byte[]{'0', '4', '0', '1', '0', '2', '2'};
    private static byte[] TM_Illum_NOR = new byte[]{'0', '4', '0', '1', '0', '2', '1'};//default
    private static byte[] TM_Illum_HIGH = new byte[]{'0', '4', '0', '1', '0', '2', '0'};
    //Illumination for Presentation Mode
    //Illumination setting when in Scanning
    private static byte[] PM_Illum_OFF = new byte[]{'0', '4', '0', '1', '0', '0', '4'};
    private static byte[] PM_Illum_LOW = new byte[]{'0', '4', '0', '1', '0', '0', '3'};
    private static byte[] PM_Illum_MID = new byte[]{'0', '4', '0', '1', '0', '0', '2'};
    private static byte[] PM_Illum_NOR = new byte[]{'0', '4', '0', '1', '0', '0', '1'};//default
    private static byte[] PM_Illum_HIGH = new byte[]{'0', '4', '0', '1', '0', '0', '0'};
    //Illumination setting when in Idle
    private static byte[] PM_Illum_IDLE_OFF = new byte[]{'0', '4', '0', '1', '0', '1', '4'};
    private static byte[] PM_Illum_IDLE_LOW = new byte[]{'0', '4', '0', '1', '0', '1', '3'};//default
    private static byte[] PM_Illum_IDLE_MID = new byte[]{'0', '4', '0', '1', '0', '1', '2'};
    private static byte[] PM_Illum_IDLE_NOR = new byte[]{'0', '4', '0', '1', '0', '1', '1'};
    private static byte[] PM_Illum_IDLE_HIGH = new byte[]{'0', '4', '0', '1', '0', '1', '0'};
    //Barcode Scanning Delay
    private static byte[] SCAN_DELAY_2000 = new byte[]{'0', '8', '0', 'B', '0', '8', '2','0','0','0'};
    private static byte[] SCAN_DELAY_500 = new byte[]{'0', '8', '0', 'B', '0', '8', '5', '0'};
    private static byte[] SCAN_DELAY_NO = new byte[]{'0', '8', '0', 'B', '0', '8', '0'};
    //Enable/Disable All Symbologies
    private static byte[] ENABLE_ALL_SYM = new byte[]{'0', '2', '0', '1', '0', '0', '1'};
    private static byte[] DISABLE_ALL_SYM = new byte[]{'0', '2', '0', '1', '0', '0', '0'};
    //code128 Max: 020A03XX. es. 020A0325. max 25
    //Min: 020A0210.
    private static byte[] SYM_CODE128 = new byte[]{'0', '2', '0', 'A', '0', '1', '1'};//disable 020a010
    
    private static byte[] SYM_EAN8 = new byte[]{'0', '2', '1', '4', '0', '1', '1'};//disable 0214010
    private static byte[] SYM_EAN8_SEND_CK = new byte[]{'0', '2', '1', '4', '0', '2', '1'};//disable 0214020
    private static byte[] SYM_EAN8_ADD_ON2 = new byte[]{'0', '2', '1', '4', '0', '3', '0'};//enable 0214031
    private static byte[] SYM_EAN8_ADD_ON5 = new byte[]{'0', '2', '1', '4', '0', '4', '0'};//enable 0214041
    //Add-On Code Required
    private static byte[] SYM_EAN8_ADD_REQ = new byte[]{'0', '2', '1', '4', '0', '5', '0'};//enable 0214051
    //ENA/JAN-8 Addenda Separator
    private static byte[] SYM_EAN8_ADD_SEP = new byte[]{'0', '2', '1', '4', '0', '6', '1'};//enable 0214060
    
    private static byte[] SYM_EAN13 = new byte[]{'0', '2', '1', '3', '0', '1', '1'};//disable 0213010
    private static byte[] SYM_EAN13_SEND_CK = new byte[]{'0', '2', '1', '3', '0', '2', '1'};//disable 0213020
    private static byte[] SYM_EAN13_ADD_ON2 = new byte[]{'0', '2', '1', '3', '0', '3', '0'};//enable 0214031
    private static byte[] SYM_EAN13_ADD_ON5 = new byte[]{'0', '2', '1', '3', '0', '4', '0'};//enable 0214041
    //Add-On Code Required
    private static byte[] SYM_EAN13_ADD_REQ = new byte[]{'0', '2', '1', '3', '0', '5', '0'};//enable 0214051
    //ENA/JAN-8 Addenda Separator
    private static byte[] SYM_EAN13_ADD_SEP = new byte[]{'0', '2', '1', '3', '0', '6', '1'};//enable 0214060
    //ISBN Translate
    private static byte[] SYM_EAN13_ISBN = new byte[]{'0', '2', '1', '3', '0', '7', '0'};//enable 0214071
    //Enable/Disable UPC-E0/E1
    private static byte[] SYM_UPCE0 = new byte[]{'0', '2', '1', '2', '0', '1', '1'};//disable 0212010
    private static byte[] SYM_UPCE1 = new byte[]{'0', '2', '1', '2', '0', '2', '1'};//disable 0212010
    private static byte[] SYM_UPCE0_SEND_CK = new byte[]{'0', '2', '1', '2', '0', '4', '1'};//disable 0212040
    //UPCE0 expand expands the UPCE code to the 12 digits, UPC-A format
    private static byte[] SYM_UPCE0_EXPAND = new byte[]{'0', '2', '1', '2', '0', '3', '0'};//disable 0212031
    private static byte[] SYM_UPCE0_ADD_ON2 = new byte[]{'0', '2', '1', '2', '0', '6', '0'};//enable 0214041
    private static byte[] SYM_UPCE0_ADD_ON5 = new byte[]{'0', '2', '1', '2', '0', '7', '0'};//enable 0214041
    private static byte[] SYM_UPCE0_ADD_REQ = new byte[]{'0', '2', '1', '2', '0', '8', '0'};//enable 0214051
    private static byte[] SYM_UPCE0_ADD_SEP = new byte[]{'0', '2', '1', '2', '0', '9', '1'};//enable 0214060
    private static byte[] SYM_UPCE0_Send_SYS = new byte[]{'0', '2', '1', '2', '0', '5', '1'};//enable 0214060
    
    //Enable/Disable UPC-A
    private static byte[] SYM_UPCA = new byte[]{'0', '2', '1', '1', '0', '1', '1'};//disable 0212010
    private static byte[] SYM_UPCA_SEND_CK = new byte[]{'0', '2', '1', '1', '0', '2', '1'};//disable 0212040
    private static byte[] SYM_UPCA_ADD_ON2 = new byte[]{'0', '2', '1', '1', '0', '4', '0'};//enable 0214041
    private static byte[] SYM_UPCA_ADD_ON5 = new byte[]{'0', '2', '1', '1', '0', '4', '0'};//enable 0214041
    private static byte[] SYM_UPCA_ADD_REQ = new byte[]{'0', '2', '1', '1', '0', '6', '0'};//enable 0214051
    private static byte[] SYM_UPCA_ADD_SEP = new byte[]{'0', '2', '1', '1', '0', '7', '1'};//enable 0214060
    private static byte[] SYM_UPCA_Send_SYS = new byte[]{'0', '2', '1', '1', '0', '3', '1'};//enable 0214060
    
   //Enable/Disable Interleaved 2 of 5
    // 0-80 Max: 02040425.
    //Min: 02040310.
    private static byte[] SYM_I25 = new byte[]{'0', '2', '0', '4', '0', '1', '1'};//disable 0212010
   //No check Char (default) 0204020
    ////Validate and Transmit 0204022
    //Validate not Transmit 0204021
    private static byte[] SYM_I25_SEND_CK = new byte[]{'0', '2', '0', '4', '0', '2', '0'};//disable 0212040
    //Matrix 2 of 5  0-80 Max: 02080325.
    //Min: 02080210.
    private static byte[] SYM_Matrix25 = new byte[]{'0', '2', '0', '8', '0', '1', '0'};//enable 0214041
    
  //Industrial 2 of 5  0-48 Max: 02080325.
    //Min: 02080210.
    private static byte[] SYM_Industrial25 = new byte[]{'0', '2', '0', '6', '0', '1', '0'};//enable 0214041
    // Code 39  0-48 Max: 02030825.
    //Min: 02030710.
    private static byte[] SYM_CODE39 = new byte[]{'0', '2', '0', '3', '0', '1', '1'};//enable 0214041
    //Transmit Start/Stop Character
    private static byte[] SYM_CODE39_SEND_SS = new byte[]{'0', '2', '0', '3', '0', '5', '0'};//enable 0214060
    //No check Char (default) 0203040
    ////Validate and Transmit 0203042
    //Validate not Transmit 0203041
    private static byte[] SYM_CODE39_SEND_CK = new byte[]{'0', '2', '0', '3', '0', '4', '0'};//enable 0214060
    private static byte[] SYM_CODE39_ASCLL = new byte[]{'0', '2', '0', '3', '0', '2', '0'};
    
 // codabar 0-60 Max: 02020625.
    //Min: 02020610.
    private static byte[] SYM_CODABAR = new byte[]{'0', '2', '0', '2', '0', '1', '1'};//enable 0214041
    //Transmit Start/Stop Character
    private static byte[] SYM_CODABAR_SEND_SS = new byte[]{'0', '2', '0', '2', '0', '2', '0'};//enable 0214060
    //No check Char (default) 0202030
    ////Validate and Transmit 0202032
    //Validate not Transmit 0202031
    private static byte[] SYM_CODABAR_SEND_CK = new byte[]{'0', '2', '0', '2', '0', '3', '0'};//enable 0214060
 // code93 0-80 Max: 020D0325.
    //Min: 020D0310.
    private static byte[] SYM_CODE93 = new byte[]{'0', '2', '0', 'D', '0', '1', '0'};
    
 // gs128 0-80 Max: 020B0325.
    //Min: 020B0310.
    private static byte[] SYM_GS1_128 = new byte[]{'0', '2', '0', 'B', '0', '0', '1'};
    
    // mSI 0-48 Max: 020E0425.
    //Min: 020E0410.
    private static byte[] SYM_MSI = new byte[]{'0', '2', '0', 'E', '0', '1', '0'};
    //MSI Check Character
    //Validate Type 10, No Transmit (Default)
    private static byte[] SYM_MSI_V10_NOSEND = new byte[]{'0', '2', '0', 'E', '0', '2', '0'};
    private static byte[] SYM_MSI_V10_SEND = new byte[]{'0', '2', '0', 'E', '0', '2', '1'};
    private static byte[] SYM_MSI_V1010_NOSEND = new byte[]{'0', '2', '0', 'E', '0', '2', '2'};
    private static byte[] SYM_MSI_V1010_SEND = new byte[]{'0', '2', '0', 'E', '0', '2', '3'};
    private static byte[] SYM_MSI_V1110_NOSEND = new byte[]{'0', '2', '0', 'E', '0', '2', '4'};
    private static byte[] SYM_MSI_V1110_SEND = new byte[]{'0', '2', '0', 'E', '0', '2', '5'};
    private static byte[] SYM_MSI_DISABLE_CHECK = new byte[]{'0', '2', '0', 'E', '0', '2', '6'};
    //PDF417 0-2750 max length command: 021F06 min 021F05.
    private static byte[] SYM_PDF417 = new byte[]{'0','2','1','F','0','1','1'};
  //Micro PDF417 0-2750 max length command: 022203 min 022202.
    private static byte[] SYM_MICRO_PDF417 = new byte[]{'0','2','2','0','0','1','0'};
    //QR Code 0-7089 max023703. min 023702.
    private static byte[] SYM_QRCODE = new byte[]{'0','2','3','7','0','1','1'};
  //data matrix 0-3116 max023603. min 023602.
    private static byte[] SYM_DATA_MATRIX = new byte[]{'0','2','3','6','0','1','1'};
  //maxi code matrix 0-150 max023403. min 023402.
    private static byte[] SYM_MAXICODE = new byte[]{'0','2','3','4','0','1','0'};
  //Aztec code matrix 0-3832. max 023306.. min 023305.
    private static byte[] SYM_AztecCODE = new byte[]{'0','2','3','3','0','1','0'};
  //HANXIN code matrix 0-7833. max 023803.. min 023802.
    private static byte[] SYM_HANXINCODE = new byte[]{'0','2','3','8','0','1','0'};
    //China Postal Code
    private static byte[] SYM_CHINA_POSTAL = new byte[]{'0','2','1','8','0','1','0'};
    private static byte[] SYM_TELEPEN = new byte[]{'0','2','1','0','0','1','0'};

}
