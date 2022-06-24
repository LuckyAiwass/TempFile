package com.android.server.scanner;

import android.util.Log;
import android.util.SparseArray;
import android.device.ScanNative;
import android.device.scanner.configuration.PropertyID;

import com.android.server.ScanServiceWrapper;

public class OpticonScanner extends SerialScanner {
    private static final String TAG = "OpticonScanner";
    
    public OpticonScanner(ScanServiceWrapper scanService) {
        super(scanService);
        // TODO Auto-generated constructor stub
        mScannerType = ScannerFactory.TYPE_Opticon;
        mBaudrate = 9600;

        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            mPropIndexHashMap.put(INTERNAL_PROPERTY_INDEX[i], VALUE_PARAM_INDEX[i]);
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

    @Override
    protected boolean onDataReceived() {
        // TODO Auto-generated method stub
     // mdl2000
        int start = BytesIndexOf(mBuffer, 0, mBufOffset, (byte) 0x02);
        Log.i(TAG, "-start=[" + start + "]... ...");
        if (start != -1) {
            int end = BytesIndexOf(mBuffer, start, mBufOffset - start, (byte) 0x03);
            Log.i(TAG, "end=[" + end + "]... ...");
            if (end != -1) {
                int barcodelen = (mBuffer[start + 2] - 0x30) * 10 + (mBuffer[start + 3] - 0x30);
                Log.i(TAG, "barcodelen=[" + barcodelen + "]... ...");
                if (barcodelen + 4 == end - start) { // 校验报告长度和真实长度
                    byte[] tmp = new byte[barcodelen];
                    for (int i = 0; i < barcodelen; ++i) {
                        tmp[i] = mBuffer[start + 4 + i];
                    }
                    sendBroadcast(tmp, mBuffer[start + 1], barcodelen);

                    // 把剩余内容移动到缓冲头部
                    int len = mBufOffset - (end + 1); // 剩余长度
                    for (int i = 0; i < len; ++i) {
                        mBuffer[i] = mBuffer[end + 1 + i];
                    }
                    mBufOffset = len;

                    return true;
                } else {
                    Log.i(TAG, "---mdc-100----error, so clear---------------");
                    mBufOffset = 0;
                    return false;
                }

            }// end of end != -1
        } // end of start !- 01
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
        synchronized (mHandler) {
            byte[] cmd = new byte[256];
            int indexPos  = 0;
            Log.i(TAG, "-setProperties--command---------------" + indexPos); 
            StringBuffer cmdL1 = new StringBuffer(OpticonCommand.FIXED_LEN_MIN);
            StringBuffer cmdL2 = new StringBuffer(OpticonCommand.FIXED_LEN_MAX);
            boolean disable = false;
            int size = property.size();
            for (int i = 0; i < size; i++) {
                int keyForIndex = property.keyAt(i);
                int internalIndex = mPropIndexHashMap.get(keyForIndex, SPECIAL_VALUE);
                if (internalIndex != SPECIAL_VALUE) {
                    int value = property.get(keyForIndex);
                    int len = 0;
                    switch (keyForIndex) {
                        case PropertyID.CODE39_ENABLE: {
                            if (value == 0) {
                                if (mScanService.getPropertyInt(PropertyID.CODE32_ENABLE) == 1) {
                                    len = OpticonCommand.CODE39_ONLY_CODE32.length;
                                    System.arraycopy(OpticonCommand.CODE39_ONLY_CODE32, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    disable = true;
                                }
                            } else {
                                if (mScanService.getPropertyInt(PropertyID.CODE32_ENABLE) == 1) {
                                    len = OpticonCommand.CODE39_IF_CODE32.length;
                                    System.arraycopy(OpticonCommand.CODE39_IF_CODE32, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    len = OpticonCommand.CODE39_NORMAL.length;
                                    System.arraycopy(OpticonCommand.CODE39_NORMAL, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                len = OpticonCommand.CODE39.length;
                                System.arraycopy(OpticonCommand.CODE39, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                            }
                        }
                            break;
                        case PropertyID.CODE39_ENABLE_CHECK: {
                            if (value == 0) {
                                len = OpticonCommand.CODE39_NO_CHECK_CD.length;
                                System.arraycopy(OpticonCommand.CODE39_NO_CHECK_CD, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                            } else {
                                len = OpticonCommand.CODE39_CHECK_CD.length;
                                System.arraycopy(OpticonCommand.CODE39_CHECK_CD, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                            }
                        }
                            break;
                        case PropertyID.CODE39_SEND_CHECK: {
                            if (value == 0) {
                                len = OpticonCommand.CODE39_NO_CD.length;
                                System.arraycopy(OpticonCommand.CODE39_NO_CD, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                            } else {
                                len = OpticonCommand.CODE39_CD.length;
                                System.arraycopy(OpticonCommand.CODE39_CD, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                            }
                        }
                            break;
                        case PropertyID.CODE39_FULL_ASCII: {
                            if (value == 0) {
                                if (mScanService.getPropertyInt(PropertyID.CODE32_ENABLE) == 1) {
                                    len = OpticonCommand.CODE39_NORMAL.length;
                                    System.arraycopy(OpticonCommand.CODE39_NORMAL, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    len = OpticonCommand.CODE39_IF_CODE32.length;
                                    System.arraycopy(OpticonCommand.CODE39_IF_CODE32, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    len = OpticonCommand.CODE39_NORMAL.length;
                                    System.arraycopy(OpticonCommand.CODE39_NORMAL, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                            } else {
                                if (mScanService.getPropertyInt(PropertyID.CODE32_ENABLE) == 1){
                                    len = OpticonCommand.CODE39_IF_CODE32.length;
                                    System.arraycopy(OpticonCommand.CODE39_IF_CODE32, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    len = OpticonCommand.CODE39_FULL_ASCII.length;
                                    System.arraycopy(OpticonCommand.CODE39_FULL_ASCII, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                            }
                        }
                            break;

                        case PropertyID.CODE39_LENGTH1: {
                            if (value < 10)
                                cmdL1.append(" V0").append(value);
                            else
                                cmdL1.append(" V").append(value);
                        }
                            break;
                        case PropertyID.CODE39_LENGTH2: {
                            if (value < 10)
                                cmdL2.append(" V0").append(value);
                            else
                                cmdL2.append(" V").append(value);
                        }
                            break;
                        case PropertyID.TRIOPTIC_ENABLE: {
                            if (value == 0) {
                                disable = true;
                            } else {
                                len = OpticonCommand.TRIOPTIC.length;
                                System.arraycopy(OpticonCommand.TRIOPTIC, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                                //command.append(OpticonCommand.TRIOPTIC);
                            }
                        }
                            break;
                        case PropertyID.CODE32_ENABLE: {
                            if (value == 0) {
                                if (mScanService.getPropertyInt(PropertyID.CODE39_ENABLE) == 1) {
                                    len = OpticonCommand.CODE39_NORMAL.length;
                                    System.arraycopy(OpticonCommand.CODE39_NORMAL, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    //command.append(OpticonCommand.CODE39_NORMAL);
                                    if (mScanService.getPropertyInt(PropertyID.CODE39_FULL_ASCII) == 1) {
                                        len = OpticonCommand.CODE39_IF_FULL_ASCII.length;
                                        System.arraycopy(OpticonCommand.CODE39_IF_FULL_ASCII, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                        //command.append(OpticonCommand.CODE39_IF_FULL_ASCII);
                                } else {
                                    disable = true;
                                }
                            } else {
                                if (mScanService.getPropertyInt(PropertyID.CODE39_ENABLE) == 1){
                                    len = OpticonCommand.CODE39_NORMAL.length;
                                    System.arraycopy(OpticonCommand.CODE39_NORMAL, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    //command.append(OpticonCommand.CODE39_IF_CODE32);
                                } else {
                                    len = OpticonCommand.CODE39.length;
                                    System.arraycopy(OpticonCommand.CODE39, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    len = OpticonCommand.CODE39_ONLY_CODE32.length;
                                    System.arraycopy(OpticonCommand.CODE39_ONLY_CODE32, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    //command.append(OpticonCommand.CODE39);
                                    //command.append(OpticonCommand.CODE39_ONLY_CODE32);
                                }
                            }
                        }
                            break;
                        case PropertyID.CODE32_SEND_START:
                            if(value == 0) {
                                len = OpticonCommand.CODE39_NO_ID_A_CODE32.length;
                                System.arraycopy(OpticonCommand.CODE39_NO_ID_A_CODE32, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                            } else {
                                len = OpticonCommand.CODE39_ID_A_CODE32.length;
                                System.arraycopy(OpticonCommand.CODE39_ID_A_CODE32, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                            }
                            break;
                        case PropertyID.D25_ENABLE: {
                            if (value == 0) {
                                disable = true;
                            } else {
                                len = OpticonCommand.DISCRETE25.length;
                                System.arraycopy(OpticonCommand.DISCRETE25, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                                //command.append(OpticonCommand.DISCRETE25);
                            }
                        }
                            break;
                        case PropertyID.D25_LENGTH1: {
                            if (value < 10)
                                cmdL1.append(" O0").append(value);
                            else
                                cmdL1.append(" O").append(value);
                        }
                            break;
                        case PropertyID.D25_LENGTH2: {
                            if (value < 10)
                                cmdL2.append(" O0").append(value);
                            else
                                cmdL2.append(" O").append(value);
                        }
                            break;
                        case PropertyID.M25_ENABLE: {
                            if (value == 0) {
                                disable = true;
                            } else {
                                len = OpticonCommand.MATRIX25.length;
                                System.arraycopy(OpticonCommand.MATRIX25, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                                //command.append(OpticonCommand.MATRIX25);
                            }
                        }
                            break;
                        case PropertyID.I25_ENABLE: {
                            if (value == 0) {
                                disable = true;
                            } else {
                                len = OpticonCommand.INTERLEAVED25.length;
                                System.arraycopy(OpticonCommand.INTERLEAVED25, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                                //command.append(OpticonCommand.INTERLEAVED25);
                            }
                        }
                            break;
                        case PropertyID.I25_ENABLE_CHECK: {
                            if (value == 0) {
                                len = OpticonCommand.I25_NO_CHECK_CD.length;
                                System.arraycopy(OpticonCommand.I25_NO_CHECK_CD, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                                //command.append(OpticonCommand.I25_NO_CHECK_CD);
                            } else {
                                len = OpticonCommand.I25_CHECK_CD.length;
                                System.arraycopy(OpticonCommand.I25_CHECK_CD, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                                //command.append(OpticonCommand.I25_CHECK_CD);
                            }
                        }
                            break;
                        case PropertyID.I25_SEND_CHECK: {
                            if (value == 0) {
                                len = OpticonCommand.I25_NO_CD.length;
                                System.arraycopy(OpticonCommand.I25_NO_CD, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                               // command.append(OpticonCommand.I25_NO_CD);
                            } else {
                                len = OpticonCommand.I25_CD.length;
                                System.arraycopy(OpticonCommand.I25_CD, 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                                //command.append(OpticonCommand.I25_CD);
                            }
                        }
                            break;
                        case PropertyID.I25_LENGTH1: {
                            if (value < 10)
                                cmdL1.append(" N0").append(value);
                            else
                                cmdL1.append(" N").append(value);
                        }
                            break;
                        case PropertyID.I25_LENGTH2: {
                                if(value < 10)
                                    cmdL2.append(" N0").append(value);
                                else
                                    cmdL2.append(" N").append(value);
                        }
                        case PropertyID.I25_TO_EAN13: {
                        }
                                break;
                            case PropertyID.CODABAR_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.CODABAR.length;
                                    System.arraycopy(OpticonCommand.CODABAR, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    //command.append(OpticonCommand.CODABAR);
                                }
                                break;
                            case PropertyID.CODABAR_NOTIS:
                                if(value == 0) {
                                    len = OpticonCommand.CODABAR_DIS_ST_SP.length;
                                    System.arraycopy(OpticonCommand.CODABAR_DIS_ST_SP, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    //command.append(OpticonCommand.CODABAR_DIS_ST_SP);
                                } else {
                                    len = OpticonCommand.CODABAR_ABCD_STSP.length;
                                    System.arraycopy(OpticonCommand.CODABAR_ABCD_STSP, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    //command.append(OpticonCommand.CODABAR_ABCD_STSP);
                                }
                                break;
                                /*case PropertyID.CODABAR_SEND_CHECK:
                                if(value == 0) {
                                    command.append(OpticonCommand.CODABAR_NO_TRAN_CD);
                                } else {
                                    command.append(OpticonCommand.CODABAR_TRAN_CD);
                                }
                                break;
                                case PropertyID.CODABAR_SEND_START:
                                if(value == 0) {
                                    command.append(OpticonCommand.CODABAR_DIS_ST_SP);
                                } else {
                                    command.append(OpticonCommand.CODABAR_abcd_STSP);
                                }
                                break;*/
                            case PropertyID.CODABAR_CLSI:
                                if(value == 0) {
                                    len = OpticonCommand.CODABAR_DIS_CLSI.length;
                                    System.arraycopy(OpticonCommand.CODABAR_DIS_CLSI, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    //command.append(OpticonCommand.CODABAR_DIS_CLSI);
                                } else {
                                    len = OpticonCommand.CODABAR_EN_CLSI.length;
                                    System.arraycopy(OpticonCommand.CODABAR_EN_CLSI, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    //command.append(OpticonCommand.CODABAR_EN_CLSI);
                                }
                                break;
                            case PropertyID.CODABAR_LENGTH1:
                                if(value < 10)
                                    cmdL1.append(" R0").append(value);
                                else
                                    cmdL1.append(" R").append(value);
                            case PropertyID.CODABAR_LENGTH2:
                                if(value < 10)
                                    cmdL2.append(" R0").append(value);
                                else
                                    cmdL2.append(" R").append(value);
                                break;
                            case PropertyID.CODE11_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.CODE11.length;
                                    System.arraycopy(OpticonCommand.CODE11, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    //command.append(OpticonCommand.CODE93);
                                }
                                break;
                            case PropertyID.CODE11_ENABLE_CHECK:
                                if(value == 0) {
                                    len = OpticonCommand.CODE11_NO_CHK.length;
                                    System.arraycopy(OpticonCommand.CODE11_NO_CHK, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    len = OpticonCommand.CODE11_CHK.length;
                                    System.arraycopy(OpticonCommand.CODE11_CHK, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.CODE11_SEND_CHECK:
                                if(value == 0) {
                                    len = OpticonCommand.CODE11_NOSEND_CHK.length;
                                    System.arraycopy(OpticonCommand.CODE11_NOSEND_CHK, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    len = OpticonCommand.CODE11_SEND_CHK.length;
                                    System.arraycopy(OpticonCommand.CODE11_SEND_CHK, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.CODE11_LENGTH1:
                                if(value < 10)
                                    cmdL1.append(" b0").append(value);
                                else
                                    cmdL1.append(" b").append(value);
                            case PropertyID.CODE11_LENGTH2:
                                if(value < 10)
                                    cmdL2.append(" b0").append(value);
                                else
                                    cmdL2.append(" b").append(value);
                                break;
                            case PropertyID.C25_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.CHINESE25_POST.length;
                                    System.arraycopy(OpticonCommand.CHINESE25_POST, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.CODE93_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.CODE93.length;
                                    System.arraycopy(OpticonCommand.CODE93, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.CODE93_LENGTH1:
                                if(value < 10)
                                    cmdL1.append(" U0").append(value);
                                else
                                    cmdL1.append(" U").append(value);
                                break;
                            case PropertyID.CODE93_LENGTH2:
                                if(value < 10)
                                    cmdL2.append(" U0").append(value);
                                else
                                    cmdL2.append(" U").append(value);
                                break;
                            case PropertyID.CODE128_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    if (mScanService.getPropertyInt(PropertyID.CODE128_GS1_ENABLE) == 1) {
                                        len = OpticonCommand.CODE128.length;
                                        System.arraycopy(OpticonCommand.CODE128, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                        len = OpticonCommand.GS1_128_IF.length;
                                        System.arraycopy(OpticonCommand.GS1_128_IF, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    } else {
                                        len = OpticonCommand.CODE128.length;
                                        System.arraycopy(OpticonCommand.CODE128, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                        len = OpticonCommand.GS1_128_DIS.length;
                                        System.arraycopy(OpticonCommand.GS1_128_DIS, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                }
                                break;
                            case PropertyID.CODE128_LENGTH1:
                                if(value < 10)
                                    cmdL1.append(" T0").append(value);
                                else
                                    cmdL1.append(" T").append(value);
                                break;
                            case PropertyID.CODE128_LENGTH2:
                                if(value < 10)
                                    cmdL2.append(" T0").append(value);
                                else
                                    cmdL2.append(" T").append(value);
                                break;
                            case PropertyID.CODE128_GS1_ENABLE:
                                if(value == 0) {
                                    len = OpticonCommand.GS1_128_DIS.length;
                                    System.arraycopy(OpticonCommand.GS1_128_DIS, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    if (mScanService.getPropertyInt(PropertyID.CODE128_ENABLE) == 1){
                                        len = OpticonCommand.GS1_128_IF.length;
                                        System.arraycopy(OpticonCommand.GS1_128_IF, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    } else {
                                        len = OpticonCommand.CODE128.length;
                                        System.arraycopy(OpticonCommand.CODE128, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                        len = OpticonCommand.GS1_128_ONLY.length;
                                        System.arraycopy(OpticonCommand.GS1_128_ONLY, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                }
                                break;
                            case PropertyID.CODE_ISBT_128:
                                break;
                            case PropertyID.UPCA_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.UPC.length;
                                    System.arraycopy(OpticonCommand.UPC, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.UPCA_SEND_CHECK:
                                if(value == 0) {
                                    if (mScanService.getPropertyInt(PropertyID.UPCA_SEND_SYS) == 1){
                                        len = OpticonCommand.UPCA_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCA_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCA_NO_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCA_NO_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                } else {
                                    if (mScanService.getPropertyInt(PropertyID.UPCA_SEND_SYS) == 1) {
                                        len = OpticonCommand.UPCA_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCA_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCA_NO_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCA_NO_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                }
                                break;
                            case PropertyID.UPCA_SEND_SYS:
                                if(value == 0) {
                                    if (mScanService.getPropertyInt(PropertyID.UPCA_SEND_SYS) == 1) {
                                        len = OpticonCommand.UPCA_NO_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCA_NO_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCA_NO_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCA_NO_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                } else {
                                    if (mScanService.getPropertyInt(PropertyID.UPCA_SEND_SYS) == 1){
                                        len = OpticonCommand.UPCA_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCA_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCA_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCA_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                }
                                break;
                            case PropertyID.UPCE_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.UPCE.length;
                                    System.arraycopy(OpticonCommand.UPCE, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.UPCE_SEND_CHECK:
                                if(value == 0) {
                                    if (mScanService.getPropertyInt(PropertyID.UPCE_SEND_SYS) == 1){
                                        len = OpticonCommand.UPCE_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCE_NO_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_NO_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                } else {
                                    if (mScanService.getPropertyInt(PropertyID.UPCE_SEND_SYS) == 1){
                                        len = OpticonCommand.UPCE_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCE_NO_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_NO_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                }
                                break;
                            case PropertyID.UPCE_SEND_SYS:
                                if(value == 0) {
                                    if (mScanService.getPropertyInt(PropertyID.UPCE_SEND_CHECK) == 1){
                                        len = OpticonCommand.UPCE_NO_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_NO_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCE_NO_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_NO_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                } else {
                                    if (mScanService.getPropertyInt(PropertyID.UPCE_SEND_CHECK) == 1) {
                                        len = OpticonCommand.UPCE_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCE_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                }
                                break;
                            case PropertyID.UPCE_TO_UPCA:
                                if(value == 0) {
                                    len = OpticonCommand.UPCE_AS.length;
                                    System.arraycopy(OpticonCommand.UPCE_AS, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    len = OpticonCommand.UPCE_AS_UPCA.length;
                                    System.arraycopy(OpticonCommand.UPCE_AS_UPCA, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            /*case PropertyID.UPCE1_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.UPCE.length;
                                    System.arraycopy(OpticonCommand.UPCE, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.UPCE1_SEND_CHECK:
                                if(value == 0) {
                                    if (mScanService.getPropertyInt(PropertyID.UPCE_SEND_SYS) == 1){
                                        len = OpticonCommand.UPCE_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCE_NO_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_NO_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                } else {
                                    if (mScanService.getPropertyInt(PropertyID.UPCE_SEND_SYS) == 1){
                                        len = OpticonCommand.UPCE_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCE_NO_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_NO_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                }
                                break;
                            case PropertyID.UPCE1_SEND_SYS:
                                if(value == 0) {
                                    if (mScanService.getPropertyInt(PropertyID.UPCE_SEND_CHECK) == 1){
                                        len = OpticonCommand.UPCE_NO_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_NO_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCE_NO_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_NO_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                } else {
                                    if (mScanService.getPropertyInt(PropertyID.UPCE_SEND_CHECK) == 1) {
                                        len = OpticonCommand.UPCE_ZERO_SEND_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_ZERO_SEND_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.UPCE_ZERO_NO_CD.length;
                                        System.arraycopy(OpticonCommand.UPCE_ZERO_NO_CD, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                }
                                break;
                            case PropertyID.UPCE1_TO_UPCA:
                                if(value == 0) {
                                    len = OpticonCommand.UPCE1_AS.length;
                                    System.arraycopy(OpticonCommand.UPCE_AS, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    len = OpticonCommand.UPCE_AS_UPCA.length;
                                    System.arraycopy(OpticonCommand.UPCE_AS_UPCA, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;*/
                            case PropertyID.EAN13_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.EAN.length;
                                    System.arraycopy(OpticonCommand.EAN, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                             
                            case PropertyID.EAN13_BOOKLANDEAN:
                                if(value == 0) {
                                    len = OpticonCommand.ENA13_DIS_ISBN.length;
                                    System.arraycopy(OpticonCommand.ENA13_DIS_ISBN, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    len = OpticonCommand.ENA13_DIS_ISSN.length;
                                    System.arraycopy(OpticonCommand.ENA13_DIS_ISSN, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    len = OpticonCommand.ENA13_EN_ISSN.length;
                                    System.arraycopy(OpticonCommand.ENA13_EN_ISSN, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                    len = OpticonCommand.ENA13_EN_ISBN.length;
                                    System.arraycopy(OpticonCommand.ENA13_EN_ISBN, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.EAN13_BOOKLAND_FORMAT:
                                /*if(value == 0) {
                                    len = OpticonCommand.ENA13_EN_ISSN.length;
                                    System.arraycopy(OpticonCommand.ENA13_EN_ISSN, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    len = OpticonCommand.ENA13_EN_ISBN.length;
                                    System.arraycopy(OpticonCommand.ENA13_EN_ISBN, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }*/
                                break;
                            case PropertyID.EAN8_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.EAN.length;
                                    System.arraycopy(OpticonCommand.EAN, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                             
                                break;
                            case PropertyID.EAN8_TO_EAN13:
                               /* if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.EAN.length;
                                    System.arraycopy(OpticonCommand.EAN, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }*/
                             
                                break;
                            case PropertyID.EAN_EXT_ENABLE_2_5_DIGIT:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.ALL_ADD_25.length;
                                    System.arraycopy(OpticonCommand.ALL_ADD_25, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.UPC_EAN_SECURITY_LEVEL:
                                break;
                            case PropertyID.UCC_COUPON_EXT_CODE:
                                break;
                            case PropertyID.MSI_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.MSI.length;
                                    System.arraycopy(OpticonCommand.MSI, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.MSI_REQUIRE_2_CHECK:
                                if(value == 0) {
                                    len = OpticonCommand.MSI_CHECK_1CD_MOD10.length;
                                    System.arraycopy(OpticonCommand.MSI_CHECK_1CD_MOD10, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    len = OpticonCommand.MSI_CHECK_2CD_MOD10_10.length;
                                    System.arraycopy(OpticonCommand.MSI_CHECK_2CD_MOD10_10, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.MSI_SEND_CHECK:
                                if(value == 0) {
                                    len = OpticonCommand.MSI_NO_TRAN_CD.length;
                                    System.arraycopy(OpticonCommand.MSI_NO_TRAN_CD, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                } else {
                                    if (mScanService.getPropertyInt(PropertyID.MSI_REQUIRE_2_CHECK) == 1) {
                                        len = OpticonCommand.MSI_TRAN_CD12.length;
                                        System.arraycopy(OpticonCommand.MSI_TRAN_CD12, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                    else {
                                        len = OpticonCommand.MSI_TRAN_CD1.length;
                                        System.arraycopy(OpticonCommand.MSI_TRAN_CD1, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                }
                                break;
                            case PropertyID.MSI_CHECK_2_MOD_11:
                                if(value == 0) {
                                    if (mScanService.getPropertyInt(PropertyID.MSI_REQUIRE_2_CHECK) == 1){
                                        len = OpticonCommand.MSI_CHECK_2CD_MOD10_10.length;
                                        System.arraycopy(OpticonCommand.MSI_CHECK_2CD_MOD10_10, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }else {
                                        len = OpticonCommand.MSI_CHECK_1CD_MOD10.length;
                                        System.arraycopy(OpticonCommand.MSI_CHECK_1CD_MOD10, 0, cmd, indexPos, len);
                                        indexPos = indexPos + len;
                                    }
                                } else {
                                    len = OpticonCommand.MSI_CHECK_2CD_MOD10_11.length;
                                    System.arraycopy(OpticonCommand.MSI_CHECK_2CD_MOD10_11, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.MSI_LENGTH1:
                                if(value < 10)
                                    cmdL1.append(" Z0").append(value);
                                else
                                    cmdL1.append(" Z").append(value);
                                break;
                            case PropertyID.MSI_LENGTH2:
                                if(value < 10)
                                    cmdL2.append(" Z0").append(value);
                                else
                                    cmdL2.append(" Z").append(value);
                                break;
                            case PropertyID.GS1_14_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.GS1_14.length;
                                    System.arraycopy(OpticonCommand.GS1_14, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.GS1_14_TO_UPC_EAN:
                                break;
                            case PropertyID.GS1_LIMIT_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.GS1_LIMIT.length;
                                    System.arraycopy(OpticonCommand.GS1_LIMIT, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.GS1_EXP_ENABLE:
                                if(value == 0) {
                                    disable = true;
                                } else {
                                    len = OpticonCommand.GS1_EXP.length;
                                    System.arraycopy(OpticonCommand.GS1_EXP, 0, cmd, indexPos, len);
                                    indexPos = indexPos + len;
                                }
                                break;
                            case PropertyID.GS1_EXP_LENGTH1:
                                if(value < 10)
                                    cmdL1.append(" y0").append(value);
                                else
                                    cmdL1.append(" y").append(value);
                                break;
                            case PropertyID.GS1_EXP_LENGTH2:
                                if(value < 10)
                                    cmdL2.append(" y0").append(value);
                                else
                                    cmdL2.append(" y").append(value);
                            break;
                            case PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL:
                                len = SecurityLevel[value-1].length;
                                System.arraycopy(SecurityLevel[value-1], 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                                break;
                            case PropertyID.IMAGE_ONE_D_INVERSE:
                                len = IMAGE_ONE_D_INVERSE[value].length;
                                System.arraycopy(SecurityLevel[value], 0, cmd, indexPos, len);
                                indexPos = indexPos + len;
                                break;
                            case PropertyID.LASER_ON_TIME:{
                                int ms = value/10;
                                byte[] onTime = new byte[]{'Y',(byte) (ms+48)};
                                System.arraycopy(onTime, 0, cmd, indexPos, 2);
                                indexPos = indexPos + 2;
                                Log.i(TAG, indexPos + "-LASER_ON_TIME---------------" + (new String(onTime)));
                            }
                                break;
                        default:
                            break;
                    }
                }
            }
            if(disable) {
                byte[] resetComd = getCurrentEnable();
                //if(!resetComd.equals(OpticonCommand.DISABLE_ALL_CODE))
                ScanNative.setOptionParams(resetComd);
                Log.i(TAG, "-setOptionParams--resetComd---------------" + (new String(resetComd)));
             }
             
             String minLength = cmdL1.toString();
             if(!minLength.equals(OpticonCommand.FIXED_LEN_MIN)) {
                 ScanNative.setOptionParams(minLength.getBytes());
                 Log.i(TAG, "-setOptionParams--minLength---------------" + minLength);
             }
                 
             String maxLength = cmdL2.toString();
             if(!maxLength.equals(OpticonCommand.FIXED_LEN_MAX)) {
                 ScanNative.setOptionParams(maxLength.getBytes());
                 Log.i(TAG, "-setOptionParams--maxLength---------------" + maxLength);
             }
             if(indexPos !=0) {
                 byte[] curCmd = new byte[indexPos];
                 System.arraycopy(cmd, 0, curCmd, 0, indexPos);
                 ScanNative.setOptionParams(curCmd);
                 Log.i(TAG, "-setOptionParams--command---------------" + indexPos); 
             }
            return 0;
        }
    }
    private void enableSymbologys() {
        ScanNative.setOptionParams(OpticonCommand.DISABLE_ALL_CODE);
        int enableLen = OPTICON_ENABLE_INDEX.length;
        for(int i = 0; i < enableLen; i++) {
            if (mScanService.getPropertyInt(OPTICON_ENABLE_INDEX[i]) == 1) {
                ScanNative.setOptionParams(OPTICON_ENABLE_COMMD[i]);
            }
        }
        
        if (mScanService.getPropertyInt(PropertyID.CODE128_ENABLE) == 0) {
            if(mScanService.getPropertyInt(PropertyID.CODE128_GS1_ENABLE) == 1) {
                ScanNative.setOptionParams(OpticonCommand.CODE128);
                ScanNative.setOptionParams(OpticonCommand.GS1_128_ONLY);
            }
        }
        if (mScanService.getPropertyInt(PropertyID.CODE39_ENABLE) == 0) {
            if (mScanService.getPropertyInt(PropertyID.CODE32_ENABLE) == 1) {
                ScanNative.setOptionParams(OpticonCommand.CODE39);
                ScanNative.setOptionParams(OpticonCommand.CODE39_ONLY_CODE32);
            }
        }
        //def IATA Telepen UK/Plessey korean postal authority code
        
        ScanNative.setOptionParams(OpticonCommand.IATA);
        ScanNative.setOptionParams(OpticonCommand.TELEPEN);
        ScanNative.setOptionParams(OpticonCommand.UK_PLESSEY);
        ScanNative.setOptionParams(OpticonCommand.CHINESE_SENSIBLE);
    }
    private byte[] getCurrentEnable() {
        byte[] curEnable = new byte[256];
        int indexPos = 0;
        int len = OpticonCommand.DISABLE_ALL_CODE.length;
        System.arraycopy(OpticonCommand.DISABLE_ALL_CODE, 0, curEnable, indexPos, len);
        indexPos = indexPos + len;
        int enableLen = OPTICON_ENABLE_INDEX.length;
        for(int i = 0; i < enableLen; i++) {
            if (mScanService.getPropertyInt(OPTICON_ENABLE_INDEX[i]) == 1) {
                len = OPTICON_ENABLE_COMMD[i].length;
                System.arraycopy(OPTICON_ENABLE_COMMD[i], 0, curEnable, indexPos, len);
                indexPos = indexPos + len;
            }
                
        }
        if (mScanService.getPropertyInt(PropertyID.CODE128_ENABLE) == 0) {
            len = OpticonCommand.CODE128.length;
            System.arraycopy(OpticonCommand.CODE128, 0, curEnable, indexPos, len);
            indexPos = indexPos + len;
            len = OpticonCommand.GS1_128_ONLY.length;
            System.arraycopy(OpticonCommand.GS1_128_ONLY, 0, curEnable, indexPos, len);
            indexPos = indexPos + len;
        }
        if (mScanService.getPropertyInt(PropertyID.CODE39_ENABLE) == 0) {
            if (mScanService.getPropertyInt(PropertyID.CODE32_ENABLE) == 1) {
                len = OpticonCommand.CODE39.length;
                System.arraycopy(OpticonCommand.CODE39, 0, curEnable, indexPos, len);
                indexPos = indexPos + len;
                len = OpticonCommand.CODE39_ONLY_CODE32.length;
                System.arraycopy(OpticonCommand.CODE39_ONLY_CODE32, 0, curEnable, indexPos, len);
                indexPos = indexPos + len;
            }
        }
        //def IATA Telepen UK/Plessey korean postal authority code
        len = OpticonCommand.IATA.length;
        System.arraycopy(OpticonCommand.IATA, 0, curEnable, indexPos, len);
        indexPos = indexPos + len;
        len = OpticonCommand.TELEPEN.length;
        System.arraycopy(OpticonCommand.TELEPEN, 0, curEnable, indexPos, len);
        indexPos = indexPos + len;
        len = OpticonCommand.UK_PLESSEY.length;
        System.arraycopy(OpticonCommand.UK_PLESSEY, 0, curEnable, indexPos, len);
        indexPos = indexPos + len;
        byte[] cur = new byte[indexPos];
        System.arraycopy(curEnable, 0, cur, 0, indexPos);
        return cur;
    }
    @Override
    public void setDefaults() {
        // TODO Auto-generated method stub
        ScanNative.doopticonset();
         // ScanNative.doOpTiConSet();
         //ScanNative.setOptionParams(OpticonCommand.DEF_ENABLE_COMD);
         //ScanNative.setOptionParams(OpticonCommand.DEF_MINLEN_COMD);
         //ScanNative.setOptionParams(OpticonCommand.DEF_MAXLEN_COMD);
         
    }

    /**
     * 955 Engine Internally defined param index id value is -1,engine no
     * support the param setting NOTE: some symbology type no find Param id from
     * the 955 user doc, so set -1
     */
    // NOTE temp set some values = SPECIAL_VALUE
    private static final int OPTICON_RESERVED_VALUE = 0x00;

    static class OpticonParamIndex {
		public static final int IMAGE_EXPOSURE_MODE = RESERVED_VALUE;
        public static final int IMAGE_FIXED_EXPOSURE = RESERVED_VALUE;
        public static final int IMAGE_PICKLIST_MODE = RESERVED_VALUE;
        public static final int IMAGE_ONE_D_INVERSE = OPTICON_RESERVED_VALUE;
        public final static int LASER_ON_TIME = OPTICON_RESERVED_VALUE;// 0x01-0x63 df 0x1e * 100 ms
        public final static int TIMEOUT_BETWEEN_SAME_SYMBOL = OPTICON_RESERVED_VALUE;// 0x01-0x63 // df 0x30
        public final static int LINEAR_CODE_TYPE_SECURITY_LEVEL = OPTICON_RESERVED_VALUE;// 1 2 3 4
        public static final int FUZZY_1D_PROCESSING = RESERVED_VALUE;
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
        public final static int CODE11_LENGTH2 = OPTICON_RESERVED_VALUE;// max
                                                                        // 14
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
        public static final int COMPOSITE_ENABLE = RESERVED_VALUE;
        public static final int COMPOSITE_TO_GS1_128 = RESERVED_VALUE;
        public static final int COMPOSITE_LENGTH1 = RESERVED_VALUE;
        public static final int COMPOSITE_LENGTH2 = RESERVED_VALUE;
        public static final int COMPOSITE_CC_AB_ENABLE = RESERVED_VALUE; // composite-cc_ab
        public static final int COMPOSITE_CC_AB_LENGTH1 = RESERVED_VALUE;
        public static final int COMPOSITE_CC_AB_LENGTH2 = RESERVED_VALUE;
        public static final int COMPOSITE_CC_C_ENABLE = RESERVED_VALUE; // composite-cc_c
        public static final int COMPOSITE_CC_C_LENGTH1 = RESERVED_VALUE;
        public static final int COMPOSITE_CC_C_LENGTH2 = RESERVED_VALUE;
        public final static int COMPOSITE_TLC39_ENABLE = RESERVED_VALUE;
        public static final int HANXIN_ENABLE = RESERVED_VALUE;
        public static final int HANXIN_INVERSE = RESERVED_VALUE;
        public static final int DATAMATRIX_ENABLE = RESERVED_VALUE;
        public static final int DATAMATRIX_LENGTH1 = RESERVED_VALUE;
        public static final int DATAMATRIX_LENGTH2 = RESERVED_VALUE;
        public static final int DATAMATRIX_INVERSE = RESERVED_VALUE;
        public static final int MAXICODE_ENABLE = RESERVED_VALUE;
        public static final int MAXICODE_LENGTH1 = RESERVED_VALUE;
        public static final int MAXICODE_LENGTH2 = RESERVED_VALUE;
        public static final int QRCODE_ENABLE = RESERVED_VALUE; // 2d
        public static final int QRCODE_INVERSE = RESERVED_VALUE;
        public static final int MICROQRCODE_ENABLE = RESERVED_VALUE; // 2d
        public static final int AZTEC_ENABLE = RESERVED_VALUE;            //2d
        public static final int AZTEC_INVERSE = RESERVED_VALUE;
    }
   
    private final int[] VALUE_PARAM_INDEX = {
	OpticonParamIndex.IMAGE_EXPOSURE_MODE,
	OpticonParamIndex.IMAGE_FIXED_EXPOSURE,
            OpticonParamIndex.IMAGE_PICKLIST_MODE,
            OpticonParamIndex.IMAGE_ONE_D_INVERSE,
            OpticonParamIndex.LASER_ON_TIME,
            OpticonParamIndex.TIMEOUT_BETWEEN_SAME_SYMBOL,
            OpticonParamIndex.LINEAR_CODE_TYPE_SECURITY_LEVEL,
            OpticonParamIndex.FUZZY_1D_PROCESSING,
            OpticonParamIndex.MULTI_DECODE_MODE,
            OpticonParamIndex.BAR_CODES_TO_READ,
            OpticonParamIndex.FULL_READ_MODE,
            OpticonParamIndex.CODE39_ENABLE,
            OpticonParamIndex.CODE39_ENABLE_CHECK,
            OpticonParamIndex.CODE39_SEND_CHECK,
            OpticonParamIndex.CODE39_FULL_ASCII,
            OpticonParamIndex.CODE39_LENGTH1,
            OpticonParamIndex.CODE39_LENGTH2,
            OpticonParamIndex.TRIOPTIC_ENABLE,
            OpticonParamIndex.CODE32_ENABLE,
            OpticonParamIndex.CODE32_SEND_START,
            OpticonParamIndex.C25_ENABLE,
            OpticonParamIndex.D25_ENABLE, 
            OpticonParamIndex.D25_LENGTH1,
            OpticonParamIndex.D25_LENGTH2,
            OpticonParamIndex.M25_ENABLE,
            OpticonParamIndex.CODE11_ENABLE,
            OpticonParamIndex.CODE11_ENABLE_CHECK,
            OpticonParamIndex.CODE11_SEND_CHECK,
            OpticonParamIndex.CODE11_LENGTH1,
            OpticonParamIndex.CODE11_LENGTH2,
            OpticonParamIndex.I25_ENABLE,
            OpticonParamIndex.I25_ENABLE_CHECK,
            OpticonParamIndex.I25_SEND_CHECK,
            OpticonParamIndex.I25_LENGTH1,
            OpticonParamIndex.I25_LENGTH2,
            OpticonParamIndex.I25_TO_EAN13,
            OpticonParamIndex.CODABAR_ENABLE,
            OpticonParamIndex.CODABAR_NOTIS,
            OpticonParamIndex.CODABAR_CLSI,
            OpticonParamIndex.CODABAR_LENGTH1,
            OpticonParamIndex.CODABAR_LENGTH2,
            OpticonParamIndex.CODE93_ENABLE,
            OpticonParamIndex.CODE93_LENGTH1,
            OpticonParamIndex.CODE93_LENGTH2,
            OpticonParamIndex.CODE128_ENABLE,
            OpticonParamIndex.CODE128_LENGTH1,
            OpticonParamIndex.CODE128_LENGTH2,
            OpticonParamIndex.CODE_ISBT_128,
            OpticonParamIndex.CODE128_GS1_ENABLE,
            OpticonParamIndex.UPCA_ENABLE, 
            OpticonParamIndex.UPCA_SEND_CHECK,
            OpticonParamIndex.UPCA_SEND_SYS,
            OpticonParamIndex.UPCA_TO_EAN13,
            OpticonParamIndex.UPCE_ENABLE,
            OpticonParamIndex.UPCE_SEND_CHECK,
            OpticonParamIndex.UPCE_SEND_SYS,
            OpticonParamIndex.UPCE_TO_UPCA,
            OpticonParamIndex.UPCE1_ENABLE,
            OpticonParamIndex.UPCE1_SEND_CHECK,
            OpticonParamIndex.UPCE1_SEND_SYS,
            OpticonParamIndex.UPCE1_TO_UPCA,
            OpticonParamIndex.EAN13_ENABLE,
            OpticonParamIndex.EAN13_BOOKLANDEAN,
            OpticonParamIndex.EAN13_BOOKLAND_FORMAT,
            OpticonParamIndex.EAN8_ENABLE,
            //OpticonParamIndex.EAN8_SEND_CHECK,
            OpticonParamIndex.EAN8_TO_EAN13,
            OpticonParamIndex.EAN_EXT_ENABLE_2_5_DIGIT,
            OpticonParamIndex.UPC_EAN_SECURITY_LEVEL,
            OpticonParamIndex.UCC_COUPON_EXT_CODE,
            OpticonParamIndex.MSI_ENABLE,
            OpticonParamIndex.MSI_REQUIRE_2_CHECK,
            OpticonParamIndex.MSI_SEND_CHECK,
            OpticonParamIndex.MSI_CHECK_2_MOD_11,
            OpticonParamIndex.MSI_LENGTH1,
            OpticonParamIndex.MSI_LENGTH2,
            OpticonParamIndex.GS1_14_ENABLE,
            OpticonParamIndex.GS1_14_TO_UPC_EAN,
            OpticonParamIndex.GS1_LIMIT_ENABLE,
            OpticonParamIndex.GS1_EXP_ENABLE,
            OpticonParamIndex.GS1_EXP_LENGTH1,
            OpticonParamIndex.GS1_EXP_LENGTH2,
            OpticonParamIndex.US_POSTNET_ENABLE,
            OpticonParamIndex.US_PLANET_ENABLE,
            OpticonParamIndex.US_POSTAL_SEND_CHECK,
            OpticonParamIndex.USPS_4STATE_ENABLE,
            OpticonParamIndex.UPU_FICS_ENABLE,
            OpticonParamIndex.ROYAL_MAIL_ENABLE,
            OpticonParamIndex.ROYAL_MAIL_SEND_CHECK,
            OpticonParamIndex.AUSTRALIAN_POST_ENABLE,
            OpticonParamIndex.KIX_CODE_ENABLE,
            OpticonParamIndex.JAPANESE_POST_ENABLE,
            OpticonParamIndex.PDF417_ENABLE,
            OpticonParamIndex.MICROPDF417_ENABLE,
            OpticonParamIndex.COMPOSITE_CC_AB_ENABLE,
            OpticonParamIndex.COMPOSITE_CC_C_ENABLE,
            OpticonParamIndex.COMPOSITE_TLC39_ENABLE,
            OpticonParamIndex.HANXIN_ENABLE,
            OpticonParamIndex.HANXIN_INVERSE,
            OpticonParamIndex.DATAMATRIX_ENABLE,
            OpticonParamIndex.DATAMATRIX_LENGTH1,
            OpticonParamIndex.DATAMATRIX_LENGTH2,
            OpticonParamIndex.DATAMATRIX_INVERSE,
            OpticonParamIndex.MAXICODE_ENABLE,
            OpticonParamIndex.QRCODE_ENABLE,
            OpticonParamIndex.QRCODE_INVERSE,
            OpticonParamIndex.MICROQRCODE_ENABLE,
            OpticonParamIndex.AZTEC_ENABLE,
            OpticonParamIndex.AZTEC_INVERSE,
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

    // default enable symbology: B2R8R9B3B6R1R4JXOG
    // R1 R4 B2 B3 R8 R9 B6 OG JX
    // code identifiers: V O Q N R U T C D B A Z y y y Y T

    public static String CURRENT_COMD = "BO";
    private static final byte[][] SecurityLevel = new byte[][]{
        {'X', '0'},
        {'X', '1'},
        {'X', '2'},
        {'X', '3'},
    };//Redundancy
    private static final byte[][] IMAGE_ONE_D_INVERSE = new byte[][]{
        {'V', '2'},//Positive bar codes
        {'V', '3'},//Negative bar codes
        {'V', '3'},//Positive and negative bar codes
    };

    static class OpticonCommand {
        /**
         * Single read:When a bar code has been decoded, the reader will be turned OFF.
         */
        public static final byte[] SingleRead = new byte[]{'S', '0'};//default
        /**
         * When a bar code has been decoded, the
            reader will stay ON for a time as set by 'Read
        time options' or indefinitely if the trigger switch
        has been disabled.
         */
        public static final byte[] MultipleRead = new byte[]{'S', '1'};//default
        //Read time options 0ms -10ms Y0-YL default 
        public static final byte[] Indefinitely = new byte[]{'Y', 'M'}; 

        public static final byte[] STOPDECODE_CMD = new byte[]{'Y'};//"Y";// de-trigger the reader

        public static final byte[] DEF_ENABLE_COMD = new byte[]{'B','0','S','1','Y','3','B','3','F','0','H','E','B','6','O','G','B','2','D','5','C','0','D','9','R','4','H','N',
            'R','8','G','0','E','1','[','D','4','L','J','S','4','A','4','G','R','1','E','4','E','8','6','Q','R','2','R','3','R','4','R','6'};
        //"B0C1D8B2D5HEF0R8B3E1E8E4G0B6OGR46HOGR46JHNIBE9E9R16QE4E2B54B4B4GDRJXJYY3";

        public static final byte[] DEF_MINLEN_COMD = new byte[]{'H','L',' ','Q','0','6',' ','O','0','6',' ','N','0','6',' ','R','0','4',' ','V','0','1',' ','Z','0','4',' ','T','0','2',' ','U','0','2',' ','b','0','4',' ','T','0','2',' ','y','0','1'};
        public static final byte[] DEF_MAXLEN_COMD = new byte[]{'H','M',' ','Q','1','0',' ','O','1','0',' ','N','1','0',' ','R','2','0',' ','V','2','0',' ','Z','1','0',' ','T','4','0',' ','U','2','0',' ','b','5','5',' ','T','4','0',' ','y','7','4'};
        public static final byte[] ENABLE_ALL_COMD = "B2R7BBJSWHR8R9B3B5B6R1R4B7JXJYDRJZD7OG".getBytes();
        public static final byte[] ENABLE_ALL_CODE = new byte[]{'[', 'B', 'C', 'M'};// "[BCM";
        public static final byte[] DISABLE_ALL_CODE = new byte[]{'B', '0'};//"B0";
        // enable of readable codes
        public static final byte[] CODE39_32= new byte[]{'B', '2', 'D', '7'};//"B2";
        public static final byte[] CODE39 = new byte[]{'B', '2'};//"B2";
        public static final byte[] DISCRETE25 = new byte[]{'R', '7'};//"R7";
        public static final byte[] MATRIX25 = new byte[]{'B', 'B'};//"BB";
        public static final byte[] CHINESE_POST_MATRIX25 = new byte[]{'J', 'S'};//"JS";
        public static final byte[] KOREAN_POSTAL_AUTHORITY = new byte[]{'W', 'H'};//"WH";
        public static final byte[] INTERLEAVED25 = new byte[]{'R', '8'};//"R8";
        public static final byte[] CODABAR = new byte[]{'B', '3'};// "B3";
        public static final byte[] CODE93 = new byte[]{'B', '5'};// "B5";
        public static final byte[] CODE128_gs1 = new byte[]{'B', '6', 'O', 'G'};//"B6";
        public static final byte[] CODE128 = new byte[]{'B', '6'};//"B6";
        public static final byte[] CODABLOCK_F = new byte[]{'[', 'D', '4', 'P'};//"[D4P";// add [ code128
        public static final byte[] ALL_ADD_25 = new byte[]{'R', '2', 'R', '3','R', '5','R', '6'};//"A0";
        public static final byte[] ALL_ADD_ON = new byte[]{'A', '0'};//"A0";
        public static final byte[] UPCA = new byte[]{'R', '1'};//"R1";
        public static final byte[] UPCE = new byte[]{'R', '1'};//"R1";
        public static final byte[] UPC = new byte[]{'R', '1'};//"R1";
        public static final byte[] UPC_2 = new byte[]{'R', '2'};//"R2";
        public static final byte[] UPC_5 = new byte[]{'R', '3'};//"R3";
        public static final byte[] EAN13 = new byte[]{'R', '4'};// "R4";
        public static final byte[] EAN8 = new byte[]{'R', '4'};//"R4";
        public static final byte[] EAN = new byte[]{'R', '4'};//"R4";
        public static final byte[] EAN_2 = new byte[]{'R', '5'};//"R5"
        public static final byte[] EAN_5 = new byte[]{'R', '6'};//"R6";
        public static final byte[] IATA = new byte[]{'B', '4'};//"B4";
        public static final byte[] MSI = new byte[]{'B', '7'};//"B7";
        public static final byte[] GS1_14 = new byte[]{'J', 'X'};//"JX";
        public static final byte[] GS1_LIMIT = new byte[]{'J', 'Y'};//"JY";
        public static final byte[] GS1_EXP = new byte[]{'D', 'R'};//"DR";
        public static final byte[] TRIOPTIC = new byte[]{'J', 'Z'};//"JZ";
        public static final byte[] CODE32 = new byte[]{'D', '7'};//"D7";// It. Pharmaceutical if
                                                 // possible //D6 It.
                                                 // Pharmaceutical only
        public static final byte[] GS1_128 = new byte[]{'O', 'G'};//"OG";
        public static final byte[] TELEPEN = new byte[]{'B', '9'};//"B9";
        public static final byte[] UK_PLESSEY = new byte[]{'B', '1'};//"B1";
        public static final byte[] S_CODE = new byte[]{'R', '9'};//"R9";
        public static final byte[] CODE11 = new byte[]{'[', 'B', 'L', 'C'};//"[BLC";
        public static final byte[] CODE11_NO_CHK = new byte[]{'[', 'B', 'L', 'F'};//"[BLF";
        public static final byte[] CODE11_CHK = new byte[]{'[', 'B', 'L', 'I'};//"[BLI";
        public static final byte[] CODE11_SEND_CHK = new byte[]{'[', 'B', 'L', 'K'};//"[BLK";
        public static final byte[] CODE11_NOSEND_CHK  = new byte[]{'[', 'B', 'L', 'J'};//"[BLJ";
        public static final byte[] CHINESE25_POST= new byte[]{'J', 'S'};//"[D4L";
        public static final byte[] CHINESE_SENSIBLE = new byte[]{'[', 'D', '4', 'L'};//"[D4L";

        // CODE length
        public static final byte[] FIXED_OFF_ALL = new byte[]{'H', '0'};//"H0";// fixed length OFF all // codes
        public static final byte[] FIXED_ON_ALL = new byte[]{'H', '1'};//"H1";
        public static final byte[] FIXED_ON_SELECTED = new byte[]{'H', 'K'};//"HK";// FIXED length ON
                                                            // for selected
                                                            // codes
        public static final String FIXED_LEN_MIN = "HL";// minmum length for
        // selected codes
        public static final String FIXED_LEN_MAX = "HM";
        public static final byte[] FIXED_SEL_MIN = new byte[]{'H', 'L'};//"HL";// minmum length for // selected codes
        public static final byte[] FIXED_SEL_MAX = new byte[]{'H', 'M'};//"HM";
        // setting code specific options
        public static final byte[] UPCA_NO_ZERO_SEND_CD = new byte[]{'E', '3'};//"E3";// no leading digit, transmit CD
        public static final byte[] UPCA_ZERO_SEND_CD = new byte[]{'E', '2'};//"E2";
        public static final byte[] UPCA_NO_ZERO_NO_CD = new byte[]{'E', '5'};//"E5";
        public static final byte[] UPCA_ZERO_NO_CD = new byte[]{'E', '4'};//"E4";
        public static final byte[] UPCE_NO_ZERO_SEND_CD = new byte[]{'E', '7'};//"E7";// no leading // digit,// transmit CD
        public static final byte[] UPCE_ZERO_SEND_CD = new byte[]{'E', '6'};//"E6";// send system digit and cd
        public static final byte[] UPCE_NO_ZERO_NO_CD = new byte[]{'E', '9'};//"E9";
        public static final byte[] UPCE_ZERO_NO_CD = new byte[]{'E', '8'};//"E8";
        public static final byte[] UPCE_AS = new byte[]{'6', 'Q'};//"6Q";
        public static final byte[] UPCE_AS_UPCA = new byte[]{'6', 'P'};//"6P";
        public static final byte[] ENA13_NO_CD = new byte[]{'6', 'J'};//"6J";// NO SEND CD
        public static final byte[] ENA13_CD = new byte[]{'6', 'K'};//"6K";// df
        public static final byte[] ENA13_DIS_ISBN = new byte[]{'I', 'B'};//"IB";// df
        public static final byte[] ENA13_EN_ISBN = new byte[]{'I', 'A'};//"IA";
        public static final byte[] ENA13_IF_ISBN = new byte[]{'I', 'K'};//"IK";// enable isbn if// possible
        public static final byte[] ENA13_DIS_ISSN = new byte[]{'H', 'N'};//"HN";// df
        public static final byte[] ENA13_EN_ISSN = new byte[]{'H', 'O'};//"HO";
        public static final byte[] ENA13_IF_ISSN = new byte[]{'4', 'V'};//"4V";// enable issn if// possible
        public static final byte[] ENA13_DIS_ISMN = new byte[]{'I', 'O'};//"IO";// df
        public static final byte[] ENA13_EN_ISMN = new byte[]{'I', 'P'};//"IP";
        public static final byte[] ENA13_IF_ISMN = new byte[]{'I', 'Q'};//"IQ";// enable issn if possible
        public static final byte[] ENA8_NO_CD = new byte[]{'6', 'H'};//"6H";
        public static final byte[] ENA8_CD = new byte[]{'6', 'I'};//"6I";// df
        public static final byte[] CODE39_NORMAL = new byte[]{'D', '5'};//"D5";// df
        public static final byte[] CODE39_FULL_ASCII = new byte[]{'D', '4'};//"D4";
        public static final byte[] CODE39_IF_FULL_ASCII = new byte[]{'+', 'K'};//"+K";
        public static final byte[] CODE39_NO_CHECK_CD = new byte[]{'C', '1'};//"C1";
        public static final byte[] CODE39_CHECK_CD = new byte[]{'C', '0'};//"C0";
        public static final byte[] CODE39_ONLY_CODE32 = new byte[]{'D', '6'};//"D6";
        public static final byte[] CODE39_IF_CODE32 = new byte[]{'D', '7'};//"D7";
        public static final byte[] CODE39_NO_CD = new byte[]{'D', '8'};//"D8";
        public static final byte[] CODE39_CD = new byte[]{'D', '9'};//"D9";// DF
        public static final byte[] CODE39_ST_SP = new byte[]{'D', '0'};//"D0";// df
        public static final byte[] CODE39_NO_ST_SP = new byte[]{'D', '1'};//"D1";
        public static final byte[] CODE39_NO_ID_A_CODE32 = new byte[]{'D', 'A'};//"DA";
        public static final byte[] CODE39_ID_A_CODE32 = new byte[]{'D', 'B'};//"DB";
        public static final byte[] CODABAR_NORMAL = new byte[]{'H', 'A'};//"HA";// df
        public static final byte[] CODABAR_ONLY_ABC = new byte[]{'H', '4'};//"H4";
        public static final byte[] CODABAR_ONLY_CX = new byte[]{'H', '5'};//"H5";
        public static final byte[] CODABAR_ABC_CX = new byte[]{'H', '3'};//"H3";
        public static final byte[] CODABAR_NO_CHECK_CD = new byte[]{'H', '7'};//"H7";// df
        public static final byte[] CODABAR_CHECK_CD = new byte[]{'H', '6'};//"H6";
        public static final byte[] CODABAR_NO_TRAN_CD = new byte[]{'H', '9'};//"H9";
        public static final byte[] CODABAR_TRAN_CD = new byte[]{'H', '8'};//"H8";
        public static final byte[] CODABAR_DIS_CLSI = new byte[]{'H', 'E'};//"HE";// df disable space insertion
        public static final byte[] CODABAR_EN_CLSI = new byte[]{'H', 'D'};//"HD";
        public static final byte[] CODABAR_DIS_ST_SP = new byte[]{'F', '0'};//"F0";// df
        public static final byte[] CODABAR_ABCD_STSP = new byte[]{'F', '3'};//"F3";// ABCD/ABCD
        public static final byte[] CODABAR_abcd_STSP = new byte[]{'F', '4'};//"F4";// abcd/abcd
        public static final byte[] CODABAR_ABCD_STSP_TN = new byte[]{'F', '1'};//"F1";// ABCD/TN*E
        public static final byte[] CODABAR_abcd_STSP_tn = new byte[]{'F', '2'};//"F2";// abcd/tn*e
        public static final byte[] CODABAR_DC_STSP = new byte[]{'H', 'J'};//"HJ";// <DC1><DC2><DC3><DC4>/<DC1><DC2><DC3><DC4>
        public static final byte[] I25_NO_CD = new byte[]{'E', '1'};//"E1";
        public static final byte[] I25_CD = new byte[]{'E', '0'};//"E0";
        public static final byte[] I25_NO_CHECK_CD = new byte[]{'G', '0'};//"G0";// DF
        public static final byte[] I25_CHECK_CD = new byte[]{'G', '1'};//"G1";
        public static final byte[] MSI_NO_CHECK_CD = new byte[]{'4', 'A'};//"4A";
        public static final byte[] MSI_CHECK_1CD_MOD10 = new byte[]{'4', 'B'};//"4B";// DF
        public static final byte[] MSI_CHECK_2CD_MOD10_10 = new byte[]{'4', 'C'};//"4C";
        public static final byte[] MSI_CHECK_2CD_MOD10_11 = new byte[]{'4', 'D'};//"4D";
        public static final byte[] MSI_CHECK_2CD_MOD11_10 = new byte[]{'4', 'R'};//"4R";
        public static final byte[] MSI_NO_TRAN_CD = new byte[]{'4', 'G'};//"4G";
        public static final byte[] MSI_TRAN_CD1 = new byte[]{'4', 'E'};//"4E";// DF
        public static final byte[] MSI_TRAN_CD12 = new byte[]{'4', 'F'};//"4F";
        public static final byte[] GS1_128_DIS = new byte[]{'O', 'F'};//"OF";// DF
        public static final byte[] GS1_128_ONLY = new byte[]{'J', 'F'};//"JF";// If the data does not
                                                       // comply with the
                                                       // GS1-128 format, then
                                                       // the label is rejected
        public static final byte[] GS1_128_IF = new byte[]{'O', 'G'};//"OG";// enable gs1-128 if possible
        public static final byte[] CODE93_NO_CHECK_CD =new byte[]{'9', 'Q'};//"9Q";
        public static final byte[] CODE93_CHECK_CD = new byte[]{'A', 'C'};//"AC";// df
        public static final byte[] CODE93_NO_TRAN_CD = new byte[]{'D', 'Z'};//"DZ";// df
        public static final byte[] CODE93_TRAN_CD = new byte[]{'D', 'Y'};//"DY";
        public static final byte[] GS1_DATABAR_NO_TRAN_CD = new byte[]{'D', 'M'};//"DM";
        public static final byte[] GS1_DATABAR_TRAN_CD = new byte[]{'D', 'L'};// "DL";// df
        public static final byte[] GS1_DATABAR_NO_TRAN_INDEN = new byte[]{'D', 'T'};//"DT";
        public static final byte[] GS1_DATABAR_TRAN_INDEN = new byte[]{'D', 'S'};//"DS";// dfTransmit Application Identifier
    }

    private byte[][] OPTICON_ENABLE_COMMD = new byte[][] {
            OpticonCommand.CODE39, 
            OpticonCommand.DISCRETE25, 
            OpticonCommand.MATRIX25,
            OpticonCommand.INTERLEAVED25, 
            OpticonCommand.CODABAR, 
            OpticonCommand.CODE93,
            OpticonCommand.CODE128, 
            OpticonCommand.UPCA, 
            OpticonCommand.UPCE, 
            OpticonCommand.EAN13,
            OpticonCommand.EAN8, 
            OpticonCommand.MSI, 
            OpticonCommand.GS1_14,
            OpticonCommand.GS1_LIMIT, 
            OpticonCommand.GS1_EXP,
            OpticonCommand.ALL_ADD_25,// MAYBE todo
            OpticonCommand.TRIOPTIC, 
            OpticonCommand.CODE39_32,
            OpticonCommand.CODE128_gs1 , 
            OpticonCommand.CHINESE25_POST,
            OpticonCommand.CODE11,
    };

    private int[] OPTICON_ENABLE_INDEX = new int[] {
            PropertyID.CODE39_ENABLE, // Code39 definitions
            PropertyID.D25_ENABLE, // discrete 2/5
            PropertyID.M25_ENABLE, // matrix 2/5
            PropertyID.I25_ENABLE, // interleaved 2/5
            PropertyID.CODABAR_ENABLE, // codebar
            PropertyID.CODE93_ENABLE, // code 93
            PropertyID.CODE128_ENABLE, // code128
            PropertyID.UPCA_ENABLE, // uspa
            PropertyID.UPCE_ENABLE, // uspe
            PropertyID.EAN13_ENABLE, // ean13
            PropertyID.EAN8_ENABLE, // ean8
            PropertyID.MSI_ENABLE, // msi
            PropertyID.GS1_14_ENABLE, // rss
            PropertyID.GS1_LIMIT_ENABLE, // rss limit
            PropertyID.GS1_EXP_ENABLE, // rss exp
            PropertyID.EAN_EXT_ENABLE_2_5_DIGIT, // UPC/EAN Extensions// definitions
            PropertyID.TRIOPTIC_ENABLE, // trioptic
            PropertyID.CODE32_ENABLE, // code 32 also see pharmacode 39
            PropertyID.CODE128_GS1_ENABLE, // gs1-128
            PropertyID.C25_ENABLE,
            PropertyID.CODE11_ENABLE
    };
}
