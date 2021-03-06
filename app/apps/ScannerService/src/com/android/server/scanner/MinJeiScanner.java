
package com.android.server.scanner;

import android.util.Log;
import android.util.SparseArray;

import android.device.ScanNative;

import com.android.server.ScanServiceWrapper;

public class MinJeiScanner extends SerialScanner {

    private static final String TAG = "MeiJeiScanner";

    public MinJeiScanner(ScanServiceWrapper scanService) {
        super(scanService);
        mScannerType = 0;//ScannerFactory.TYPE_MJ;
        mBaudrate = 115200;
    }

    @Override
    protected boolean onDataReceived() {
		if (mBufOffset < 4) {
			return false;
		}
		// meijie
		Log.i(TAG, "onDataReceived enter ......");
/*		int startIdx = 1;
//		int msgLen = mBuffer[1] << 8 | mBuffer[2];
        int msgLen = mBufOffset;
//		if ((msgLen + 3) >= mBufOffset) {
		if ( msgLen > 0) {
			byte[] tmp = new byte[msgLen];
			for (int i = 0; i < msgLen; ++i) {
				tmp[i] = mBuffer[i + 3];
				if (tmp[i] < 0){
					mBufOffset = 0;
					return false;
				}
			}
//			sendBroadcast(tmp, (byte) 0, msgLen);
			sendBroadcast(mBuffer, (byte) 0, msgLen);
			mBufOffset = 0;
		}*/

		  //MinJie 2D   startindx(0x03)+ length(2 bytes) + prefix(0x02) + barcode + suffix(0x04)	
		int startIdx = -1;
		for (int i = 0; i < mBufOffset - 4; ++i) {
			if (mBuffer[i] == 3 && mBuffer[i + 3] == 2) {
				startIdx = i;
				break;
			}
		}

		if (startIdx < 0) {
			return false;
		}

		int msgLen = (mBuffer[startIdx + 1] & 0xFF) << 8 | (mBuffer[startIdx + 2] & 0xFF);

		Log.i(TAG, "------------mBufOffset, startidx, msgLen----------------["
				+ mBufOffset + "][" + startIdx + "][" + msgLen + "]");
		
		if (msgLen < 2) {
			return false;
		}

		if ((msgLen + startIdx + 3) <= mBufOffset) // length enough
		{

			if (mBuffer[startIdx + 3 + msgLen - 1] == 0x04) {

				byte[] tmp = new byte[msgLen - 2];
				for (int i = 0; i < msgLen - 2; ++i) {
					tmp[i] = mBuffer[startIdx + i + 4];
				}
				sendBroadcast(tmp, (byte) 0, msgLen - 2);

				int len = mBufOffset - msgLen - startIdx - 3;
				for (int i = 0; i < len; ++i) {
					mBuffer[i] = mBuffer[startIdx + msgLen + 3 + i];
				}
				mBufOffset = len;

				return true;

			}

			else { // data error

				if (mBufOffset == mBufferSize) {
					Log.i(TAG, "-------error, so find next----------");
					mBufOffset = 0;
					return false;
				} else {
					Log.i(TAG, "-------error, so find next----------");
					mBuffer[startIdx] = 0;
					return true;
				}
			}
		}
		Log.i(TAG, "onDataReceived exit ......");

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
    public void setDefaults() {
        // TODO Auto-generated method stub
        ScanNative.dominjiedefaultset();
    }

    @Override
    public int setProperties(SparseArray<Integer> property) {
        // TODO Auto-generated method stub
        return 0;
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               