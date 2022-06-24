package com.android.server.scanner;

import android.device.ScanNative;
import android.util.Log;
import android.util.SparseArray;

import com.android.server.ScanServiceWrapper;

public class ZhDWScanner extends SerialScanner {
    private static final String TAG = "ZhDWScanner";
    
    private final byte DECODE_DATA = (byte) 0xF3;
	private final byte CMD_ACK = (byte) 0xD0;
	private final byte CMD_NAK = (byte) 0xD1;
	private final byte PARAM_SEND = (byte) 0xC6;
	private final byte REPLY_REVISION = (byte) 0xA4;
	private final byte EVENT = (byte) 0xF6;


    public ZhDWScanner(ScanServiceWrapper scanService) {
        super(scanService);
        // TODO Auto-generated constructor stub
        mScanService = scanService;
        mScannerType = ScannerFactory.TYPE_ZhDW;
        mBaudrate = 115200;
    }

    @Override
    protected boolean onDataReceived() {
        
//        int Enter = mScanService.readConfig("SCANER_ENTER");
    	if (mBufOffset < 4)
    		return false;
  
        int Enter = 0;
        int startIdx = 0;
        int msgLen = 0;
        if (Enter != 1){
            for (int i = 0; i < mBufOffset - 4; ++i) {
                msgLen = ((mBuffer[i] & 0xff) << 8) | (mBuffer[i + 1] & 0xff);
                if (msgLen >= 4 && mBuffer[i + 3] == 0) {
                    startIdx = i;
                    break;
                }
            }
    
            msgLen = msgLen + 2;
            Log.i(TAG, "mBufOffset, startidx, msgLen......[" + mBufOffset
                    + "][" + startIdx + "][" + msgLen + "]");
            
/*            if (msgLen > 1000){  // zdw setup not stable
                mBufOffset = 0;
                return false ;
            }*/
    
            if (startIdx + msgLen <= mBufOffset) {
                if (CheckCheckSum(mBuffer, startIdx, msgLen)) {
                    switch (mBuffer[startIdx + 2]) {
                        case DECODE_DATA: {
                            SendACK_ZDW();
                            int barcodeLen = msgLen - 6 - 2;
                            if (barcodeLen < 0) // shoud be care
                                break;
                            byte[] tmp = new byte[barcodeLen];
                            for (int i = 0; i < barcodeLen; ++i) {
                                tmp[i] = mBuffer[startIdx + 6 + i];
                            }
                            sendBroadcast(tmp, mBuffer[startIdx + 6], barcodeLen);
                        }
                            break;
                        case CMD_ACK:
                        case CMD_NAK:
                        case PARAM_SEND:
                        case REPLY_REVISION:
                            break;
                        case EVENT:
                            SendACK_ZDW();
                            break;
                        default:
                            SendNAK(2);
                            break;
                    }
    
                    // 把剩余内容移动到缓冲头部
/*                    int len = mBufOffset - msgLen - startIdx;
                    for (int i = 0; i < len; ++i) {
                        mBuffer[i] = mBuffer[startIdx + msgLen + i];
                    }
                    mBufOffset = len;*/
                    mBufOffset = 0;   
                    return true;
                } else { // checksum error
                    Log.i(TAG, "zdw2---error, so clear......");
                    mBufOffset = 0;
                    return false;
                }
            }
        }else{
            if (mBufOffset < 3)
                return false;
            if(mBuffer[mBufOffset - 1] == 0x0a && mBuffer[mBufOffset - 2] == 0x0d) {
                
                int barcodelen = mBufOffset - 2;
                byte[] tmp = new byte[barcodelen];
                for (int i = 0; i < barcodelen; ++i) {
                    tmp[i] = mBuffer[i];
                    Log.i(TAG, "----------------------------tmp[i]=" + tmp[i] + "");
                }
                sendBroadcast(tmp, 0, barcodelen);
                mBufOffset = 0;

                return true;
            }
        }

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
		ScanNative.doAck();
	}

	private void SendACK_ZDW() {
        ScanNative.doAckzdw();
    }
	private void SendNAK(int a) {
		ScanNative.doNack(a);
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

    }

    @Override
    public int setProperties(SparseArray<Integer> property) {
        // TODO Auto-generated method stub
        synchronized (mHandler) {
            
        }
        return 0;
    }

}
