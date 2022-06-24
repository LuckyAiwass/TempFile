package com.android.server.scanner;

import android.util.Log;
import android.util.SparseArray;
import android.device.scanner.configuration.PropertyID;
import android.os.Looper;
import android.os.Message;
import android.os.Handler;
import android.content.Context;
import android.content.Intent;
//import android.content.IntentFilter;
import android.os.Build;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

//import com.android.server.ScanService;
import com.android.server.ScanServiceWrapper;
import com.zebra.adc.decoder.BarCodeReader;
import com.zebra.adc.decoder.BarCodeReader.DecodeCallback;
import com.zebra.adc.decoder.BarCodeReader.ErrorCallback;
// urovo add shenpidong begin 2019-04-18
import android.text.TextUtils;
// urovo add shenpidong end 2019-04-18
//import android.content.BroadcastReceiver;

public class Se2100Scanner extends Scanner implements DecodeCallback, BarCodeReader.ErrorCallback{
	private BarCodeReader mBarCodeReader;
	    // states
	static final int STATE_IDLE = 0;
	static final int STATE_DECODE = 1;
	static final int STATE_HANDSFREE = 2;
	static final int STATE_PREVIEW = 3; // snapshot preview mode
	static final int STATE_SNAPSHOT = 4;
	static final int STATE_VIDEO = 5;
	private int state = STATE_IDLE;
	private volatile boolean mEnabled;
    private final Thread mThread;
    // Handler for processing events in mThread.
    private ProviderHandler mHandler;
//    private static final String UPDATE_PARAM = "com.urovo.se2100";
    private final CountDownLatch mInitializedLatch = new CountDownLatch(1);

    /*private final class UpdateParamReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
	    String action = intent.getAction();
	    Log.d(TAG , "onReceive ----- action:" + action);
            if(Intent.ACTION_BOOT_COMPLETED.equals(action) || UPDATE_PARAM.equals(action)) {
//		setPreview();
            mHandler.removeMessages(MESSAGE_SET_PARAM);
//            Message m = Message.obtain(mHandler, MESSAGE_SET_PARAM);
            mHandler.sendEmptyMessageDelayed(MESSAGE_SET_PARAM , 500);
            
        }
    }*/

	private static final String TAG = "Se2100Scanner";

	static {
		System.loadLibrary("IAL_2100");
		System.loadLibrary("SDL_2100");
//		System.loadLibrary("barcodereader44");
//		System.loadLibrary("IAL_2100");
//		System.loadLibrary("SDL_2100");
		System.loadLibrary("barcodereader44_2100");
	}
	public Se2100Scanner(ScanServiceWrapper scanService) {
		mScannerType = ScannerFactory.TYPE_SE2100;
		mScanService = scanService;
	    for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
                mPropIndexHashMap.put(INTERNAL_PROPERTY_INDEX[i], VALUE_PARAM_INDEX[i]);
            }
        mThread = new ProviderThread();
        mThread.start();
        while (true) {
            try {
                mInitializedLatch.await();
                break;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
	}

	public void setPropertyString(int index, String value) {
		// TODO Auto-generated method stub
	}

    public void setPropertyInt(int index, int value) {
        // TODO Auto-generated method stub
        if (mBarCodeReader == null)
            return;
        Integer realIndex = mPropIndexHashMap.get(index);
        if(realIndex != null && realIndex != SPECIAL_VALUE) {
            mBarCodeReader.setParameter(realIndex, value);
		}

    }

    public int getPropertyInt(int index) {
        // TODO Auto-generated method stub
        int value = -1;
        if (mBarCodeReader == null)
            return value;
        Integer realIndex = mPropIndexHashMap.get(index);
        if(realIndex != null && realIndex != SPECIAL_VALUE)
            value = mBarCodeReader.getNumParameter(realIndex);
        return value;
    }

	public String getPropertyString(int index) {
		// TODO Auto-generated method stub
	    return null;
	}

    public boolean open() {
        synchronized (mHandler) {
            mHandler.removeMessages(MESSAGE_ENABLE);
            Message m = Message.obtain(mHandler, MESSAGE_ENABLE);
            m.arg1 = 1;
            mHandler.sendMessage(m);
        }
	return true;
    }

    public void close() {
        synchronized (mHandler) {
            mHandler.removeMessages(MESSAGE_ENABLE);
            Message m = Message.obtain(mHandler, MESSAGE_ENABLE);
            m.arg1 = 0;
            mHandler.sendMessage(m);
        }
    }
    
    public void startDecode(int timeout) {
        synchronized (mHandler) {
            //mHandler.removeMessages(MESSAGE_DECODE_TIMEOUT);
            mHandler.removeMessages(MESSAGE_CODE_ENABLE);
            Message m = Message.obtain(mHandler, MESSAGE_CODE_ENABLE);
            m.arg1 = 1;
            m.arg2 = timeout;
            mHandler.sendMessage(m);
        }
    }
    
    
    public void stopDecode() {
        synchronized (mHandler) {
            if(mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == 4) {
//                if(mBarCodeReader != null) {
//		    Log.d(TAG , "stopDecode ------- isContinueScan:" + isContinueScan);
		    isContinueScan = false;
//                    int ret = mBarCodeReader.setParameter(BarCodeReader.ParamNum.PRIM_TRIG_MODE, BarCodeReader.ParamVal.LEVEL);
//                    if (ret != BarCodeReader.BCR_ERROR) Log.i(TAG,"reset Level trigger mode faile!");
//                    mBarCodeReader.stopDecode();
                }
//            } else {
                mHandler.removeMessages(MESSAGE_CODE_ENABLE);
                Message m = Message.obtain(mHandler, MESSAGE_CODE_ENABLE);
                m.arg1 = 0;
                mHandler.sendMessage(m);
//            }
            
        }
    }
    
    private static final int MESSAGE_ENABLE = 0;
    private static final int MESSAGE_CODE_ENABLE =1;
    private static final int MESSAGE_SET_PROPERTY = 2;
    private static final int MESSAGE_DECODE_TIMEOUT = 3;
    private static final int MESSAGE_TARGET_TIMEOUT = 4;
    private static final int MESSAGE_SET_DEFAULT =5;
    private final class ProviderHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int message = msg.what;
            switch (message) {
                case MESSAGE_ENABLE:
                    if (msg.arg1 == 1) {
                        openDecoder();
                    } else {
                        closeDecoder();
                    }
                    break;
                case MESSAGE_CODE_ENABLE:
                    if (msg.arg1 == 1) {
//                        int timeout = msg.arg2;
                        startCoder();
//                        sendEmptyMessageDelayed(MESSAGE_DECODE_TIMEOUT,timeout);
                    } else {
                        stopCoder();
                    }
                    break;
                case MESSAGE_SET_DEFAULT:
                	setDefaults();
                    break;
/*                case MESSAGE_SET_PROPERTY:
                    if (msg.arg1 == 1) {
                        HashMap value = (HashMap) msg.obj;
                    	resetProperty(value);
					} else {
                    }
                    break;
                case MESSAGE_DECODE_TIMEOUT:
                    if(state != STATE_IDLE) {
                        state = STATE_IDLE;
                        if(mBarCodeReader != null)
                            mBarCodeReader.stopDecode();
                    }                      
                    break;*/
            }

        }
    };
    
	public void stopCoder() {
		// TODO Auto-generated method stub
		Log.i(TAG,"stopCoder");
		/*if(mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == 4) {
		    if(mBarCodeReader != null) {
		        int ret = mBarCodeReader.setParameter(BarCodeReader.ParamNum.PRIM_TRIG_MODE, BarCodeReader.ParamVal.LEVEL);
	            if (ret != BarCodeReader.BCR_ERROR) Log.i(TAG,"reset Level trigger mode faile!");
	            mBarCodeReader.stopDecode();
		    }
		} else {*/
		    if (mBarCodeReader != null) {
	            if(state == STATE_DECODE) {
	            try{
	                mBarCodeReader.stopDecode();
	            }catch(Exception e){
	                e.printStackTrace();
	            Log.i(TAG,"stopDecode no exit");
	            }
	            }
		    updateAIM_OR_FLASH_LED(false);
	            state = STATE_IDLE;
//		Log.i(TAG,"stopCoder , isContinueScan:" + isContinueScan);
	        }
		//}
		
	}

	private long startTime = -1;
	public void startCoder() {
	    isContinueScan = mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == 4;
//		Log.i(TAG,"startCoder , isContinueScan:" + isContinueScan);
		// TODO Auto-generated method stub
		if(isContinueScan) {
		    if (mBarCodeReader != null) {
//		Log.i(TAG,"startCoder , isContinueScan:" + isContinueScan + ",decodeNotified:" + decodeNotified);
			if(decodeNotified) {
			    decodeNotified = false;
			    isContinueScan = false;
			    mBarCodeReader.stopDecode();
			} else {
			    isContinueScan = true;
			    decodeNotified = true;
			    new DecodeNotifyThread().start();
			}
//		        int ret = mBarCodeReader.startHandsFreeDecode(BarCodeReader.ParamVal.HANDSFREE);
		       // ret = mBarCodeReader.setParameter(BarCodeReader.ParamNum.PICKLIST_MODE, 2);
//		        if (ret != BarCodeReader.BCR_SUCCESS); Log.i(TAG,"startHandsFreeDecode faile");
		    }
		} else {
		    if (state != STATE_IDLE) return;
	        if (mBarCodeReader!= null) {
	            state = STATE_DECODE;
	            try{
				//mBarCodeReader.setParameter(-1,255); //add by qiuzhoujun, debug logging
				//mBarCodeReader.setParameter(-2,1); //add by qiuzhoujun
			updateAIM_OR_FLASH_LED(true);
			startTime = System.currentTimeMillis();
				Log.e(TAG,"will call decode lib startCoder ***** startTime:" + startTime);
	            int ret_start =  mBarCodeReader.startDecode(); // start decode (callback gets results)
	            Log.e(TAG,"retrun val startCoder ret_start:" + ret_start);
				
			    while(ret_start == -1)
			   	{
					//mBarCodeReader.stopDecode();
					try
					{
						Thread.sleep(50);
						
					}catch(InterruptedException e1)
	            	{Log.e(TAG,"in startCoder, and delay exception!!!!!");}
			startTime = System.currentTimeMillis();
	            Log.e(TAG,"retrun val startCoder --- startTime:" + startTime);
					ret_start = mBarCodeReader.startDecode();
			    }
	            }catch(Exception e){
	                e.printStackTrace();
	                Log.i(TAG,"startDecode no exit");
	            }
	          }
		}
	}

    private boolean updateAIM_OR_FLASH_LED(boolean on) {
	// urovo add shenpidong begin 2019-04-03
	if(true) return false;
	// urovo add shenpidong end 2019-04-03
	int aim_led = mScanService.getPropertyInt(PropertyID.DEC_PICKLIST_AIM_MODE);
	int aim_parrern = mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE);
	Log.d(TAG , "updateAIM or FLASH_LED , aim parrern:" + aim_parrern + ",aim led:" + aim_led + ",on:" + on);
	boolean state = false;
	if(aim_led >= 1) {
	    aim_led = 1;
	    aim_parrern = ScanUtil.PICKLIST;
	    if(false && on) {
//		state = ScanUtil.update_LED_state(mScannerType , ScanUtil.FLASHLED_SE2100_STATE , 0);
	    }
	} else {
	    aim_led = 0;
	    if(on && (aim_parrern == 0 || aim_parrern == 2)) {
		on = false;
	    } else if(on && (aim_parrern > 0)) {
		// AIM LED is on
	    }
	    aim_parrern = ScanUtil.getMode(aim_parrern);
	}
	state = ScanUtil.update_LED_state(mScannerType , ScanUtil.AIMLED_SE2100_STATE , on ? 1 : 0 , aim_parrern);
	Log.d(TAG , "updateAIM or FLASH_LED , state:" + state + ",aim led:" + aim_led);
	return state;
    }

    private boolean isContinueScan = false;
    private boolean decodeNotified = false;
    private final Object decodeEvent = new Object();
    private class DecodeNotifyThread extends Thread {
	@Override
	public void run() {
	    while (isContinueScan) {
		synchronized (decodeEvent) {
//		    Log.d(TAG , "run ------ decodeNotified:" + decodeNotified);
		    while (!decodeNotified) {
			try {
			    Log.d(TAG , "run ------- pre decodeEvent.wait");
			    decodeEvent.wait();
//			    Log.d(TAG , "run ------- next decodeEvent.wait decodeNotified:" + decodeNotified);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
			decodeNotified = true;
		    }
		}
		try {
		    Thread.sleep(50);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		state = STATE_DECODE;
		Log.d(TAG , "run -------- startDecode decodeNotified:" + decodeNotified);
		updateAIM_OR_FLASH_LED(true);
		startTime = System.currentTimeMillis();
		int ret_start =  mBarCodeReader.startDecode();
		int retryCount = 0;
		while(ret_start == -1 && decodeNotified) {
		    try {
			mBarCodeReader.stopDecode();
			Thread.sleep(50);
		    } catch(InterruptedException e1) {
			Log.e(TAG,"run in startCoder, and delay exception!!!!!");
		    }
		    startTime = System.currentTimeMillis();
		    ret_start =  mBarCodeReader.startDecode();
		    Log.d(TAG , "run while -------- startDecode ret_start:" + ret_start);
		}
		Log.d(TAG , "run , startDecode ret_start:" + ret_start);
		decodeNotified = false;
	    }
	}
    }

    private void decodeFeedback() {
	synchronized (decodeEvent) {
	    Log.d(TAG , "decodeFeedback , is continueScan:" + isContinueScan);
	    if(isContinueScan) {
		decodeEvent.notify();
	    }
	}
    }
	
    public void openPhoneMode() {
    	if(mBarCodeReader != null) {
    		mBarCodeReader.setParameter(716, 1);
    	}
    }
    
    public void closePhoneMode() {
    	if(mBarCodeReader != null) {
    		mBarCodeReader.setParameter(716, 0);
    	}
    }

	public void openDecoder() {
		Log.i(TAG, "openCoder , mEnabled:" + mEnabled);
		if (mEnabled)
			return;
		state = STATE_IDLE;
        try{
	// urovo add shenpidong begin 2019-04-17
	    int scan_index = ScanUtil.scannerID();
//            mBarCodeReader = BarCodeReader.open(mScanService.mContext);
	    //mBarCodeReader = BarCodeReader.open((scan_index>=0 && scan_index <= 2) ?scan_index:2, mScanService.mContext);
	    mBarCodeReader = BarCodeReader.open(scan_index,mScanService.mContext);//juzhitao modify
	// urovo add shenpidong end 2019-04-17
	// urovo add shenpidong begin 2019-04-23
	    if(mBarCodeReader!=null) {
		String sMod = mBarCodeReader.getStrProperty(BarCodeReader.PropertyNum.MODEL_NUMBER);
		Log.i(TAG, "openCoder , sMod:" + sMod);
		if(!"BOCV3703-WDZ9068".equals(sMod!=null?sMod.trim():null)) {
		    Log.d(TAG , "openCoder , " + sMod + " isn't 6602.");
		    mBarCodeReader.release();
		    mBarCodeReader = null;
		    return;
		}
	    }
	// urovo add shenpidong end 2019-04-23
            }catch(Exception e){
            mBarCodeReader = null;
	    e.printStackTrace();
        }
		if (mBarCodeReader != null) {
			// mBarCodeReader.setParameter(BarCodeReader.ParamNum.PICKLIST_MODE,
			// 2);
			updateAIM_OR_FLASH_LED(false);
			mBarCodeReader.setDecodeCallback(this);
			mBarCodeReader.setErrorCallback(this);
			mBarCodeReader.setParameter(765,0);
                mBarCodeReader.setParameter(Se2100ParamIndex.M25_LENGTH1, 2);
                mBarCodeReader.setParameter(Se2100ParamIndex.M25_LENGTH2, 50);
		int m25_enable = mScanService.getPropertyInt(PropertyID.M25_ENABLE);
		if(m25_enable >=0 && m25_enable <2) {
			mBarCodeReader.setParameter(Se2100ParamIndex.M25_ENABLE , m25_enable);
		}
                mBarCodeReader.setParameter(Se2100ParamIndex.I25_LENGTH1, 2);
                mBarCodeReader.setParameter(Se2100ParamIndex.I25_LENGTH2, 50);
		int i25_enable = mScanService.getPropertyInt(PropertyID.I25_ENABLE);
                if(i25_enable >=0 && i25_enable <2) {
                        mBarCodeReader.setParameter(Se2100ParamIndex.I25_ENABLE , i25_enable);
                }
			mBarCodeReader.setParameter(136,50);
			// urovo add shenpidong begin 2019-04-03
			int ret = mBarCodeReader.setParameter(8610,1);
			// urovo add shenpidong end 2019-04-03
//			ret = mBarCodeReader.setParameter(8611,1);
			Log.d(TAG , "openDecoder , ret 2 8610:" + ret);		
			ret = mBarCodeReader.setParameter(1881,1);
			Log.d(TAG , "openDecoder , ret 1881:" + ret);		
			ret = mBarCodeReader.setParameter(1882,1);
			// urovo add shenpidong begin 2019-04-18
			if(mScanService.getPropertyInt(PropertyID.LABEL_SEPARATOR_ENABLE) == 1) {
			    ret = mBarCodeReader.setParameter(Se2100ParamIndex.TRANSMIT_CODE_ID,1);
			}
			// urovo add shenpidong end 2019-04-18
			Log.d(TAG , "openDecoder , ret:" + ret);		
//			mBarCodeReader.setParameter(764,1);
			mEnabled = true;
		} else {
			mEnabled = false;
		}
		
	}

	public void closeDecoder() {
		Log.i(TAG, "closeCoder");
		if (!mEnabled)
			return;
		mEnabled = false;
		if (mBarCodeReader != null) {
                try{
			if (state == STATE_DECODE) {
				mBarCodeReader.stopDecode();
			}
			mBarCodeReader.release();
                }catch(Exception e){
		  mBarCodeReader = null;
                }
			mBarCodeReader = null;
		}
		updateAIM_OR_FLASH_LED(false);
		state = STATE_IDLE;
	}
    public void onError(int error, BarCodeReader reader) {
		// TODO Auto-generated method stub
		Log.e(TAG, "error " + error);
	}
    @Override
    public void onDecodeComplete(int symbology, int length, byte[] data, BarCodeReader reader) {
        // TODO Auto-generated method stub
        if (state == STATE_DECODE) {
            state = STATE_IDLE;
        }
/*
	int status = -1;
	String decodeStatString = null;
	if(mBarCodeReader != null) {
	    status = mBarCodeReader.getNumProperty(BarCodeReader.PropertyNum.ENGINE_STATUS);
	    decodeStatString = ("[Decoding] Engine Status 0x" + Integer.toHexString(status));
	}
        Log.d(TAG,"onDecodeComplete=====1111111111=======length:" + length + ",data:" + data + ",symbology:" + symbology + ",status:" + status + ",decodeStatString:" + decodeStatString);
*/
	if(length > 0 && data != null) {
	    long endTime = System.currentTimeMillis();
	    Log.d(TAG , "onDecodeComplete time:" + (endTime - startTime));
	}
       
        switch (length) {
            case BarCodeReader.DECODE_STATUS_TIMEOUT:

				state = STATE_IDLE; //no need stop again
		decodeFeedback();
		updateAIM_OR_FLASH_LED(false);
                break;
            case BarCodeReader.DECODE_STATUS_ERROR:
				state = STATE_IDLE; //no need stop again
		decodeFeedback();
		updateAIM_OR_FLASH_LED(false);
                break;
            case BarCodeReader.DECODE_STATUS_CANCELED:
		updateAIM_OR_FLASH_LED(false);
                break;

            default:
                if (length > 0) {
				if (mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) != 4){
					if (mBarCodeReader != null) {
//						Log.d(TAG,"onDecodeComplete stop============");
            			mBarCodeReader.stopDecode(); //add by jinquan
					}
				}
                    // type 99?
                    if (symbology == 0x99) {
                        symbology = data[0];
                        int n = data[1];
                        int s = 2;
                        int d = 0;
                        int len = 0;
                        byte d99[] = new byte[data.length];
                        for (int i = 0; i < n; ++i) {
                            s += 2;
                            len = data[s++];
                            System.arraycopy(data, s, d99, d, len);
                            s += len;
                            d += len;
                        }
                        d99[d] = 0;
                        data = d99;
                    }
                    byte[] temp = new byte[length];
                    System.arraycopy(data, 0, temp, 0, length);
                    int barcodeType = 0;
                    Log.d(TAG, "onDecodeComplete symbology = " + symbology);

                    barcodeType = symbology;
                    Log.d(TAG, "onDecodeComplete barcodeType = " + barcodeType);
					// urovo add shenpidong begin 2019-04-18
					mayBeseparatorDecode(barcodeType , temp , length);
//                    sendBroadcast(temp, barcodeType, length);
					// urovo add shenpidong end 2019-04-18
					state = STATE_IDLE; //no need stop again
		    decodeFeedback();
		    updateAIM_OR_FLASH_LED(false);
                }
                break;
        }

           // state = STATE_IDLE; //no need stop again
    }

    // urovo add shenpidong begin 2019-04-18
    private void mayBeseparatorDecode(int codeID, byte[] barcodeByteData, int decResultLength) {
        boolean SEPARATOR_ENABLE = mScanService.getPropertyInt(PropertyID.LABEL_SEPARATOR_ENABLE) == 1;
        if (SEPARATOR_ENABLE && barcodeByteData != null && barcodeByteData.length > 3) {
            byte[] aimCodeID = new byte[3]; // aimCodeID values: ]C1、]e0、]d2、]Q3 ...
            System.arraycopy(barcodeByteData, 0, aimCodeID, 0, aimCodeID.length);
	    // urovo modify shenpidong begin 2019-11-28
	    String aimCodeIDStr = new String(aimCodeID);
            boolean isSeparatorDecode = SeparatorDecodeUtil.isSeparatorDecode(aimCodeIDStr , codeID);
//	    Log.d(TAG , "mayBeseparatorDecode ------- isSeparatorDecode:" + isSeparatorDecode + ",CodeID:" + codeID + ",aimCodeID:" + new String(aimCodeID));
            // realBarcodeByteData is real barcode byte data
            byte[] realBarcodeByteData = new byte[barcodeByteData.length - aimCodeID.length];
            System.arraycopy(barcodeByteData, aimCodeID.length, realBarcodeByteData, 0, realBarcodeByteData.length);
            if (isSeparatorDecode) { // if aimCodeID contains ]C1、]e0、]d2、]Q3 ]E0 ]E4 isSeparatorDecode=true , other isSeparatorDecode=false
                String sepChar = mScanService.getPropertyString(PropertyID.LABEL_FORMAT_SEPARATOR_CHAR);
                if (!TextUtils.isEmpty(sepChar)) {
                    SeparatorDecodeUtil.setSeparatorChar(sepChar.getBytes());
                }
//                byte[] resultSeparator = SeparatorDecodeUtil.separatorDecode(realBarcodeByteData);
		int compositeIndex = SeparatorDecodeUtil.compositeIndexCode(aimCodeIDStr , codeID) + 3;
//		Log.d(TAG , "mayBeseparatorDecode ------- compositeIndex:" + compositeIndex);
		byte[] resultSeparator = null;
		if(compositeIndex > 3 && realBarcodeByteData.length > compositeIndex) { // ]e0 length is 3 , ignore it
		    int realCompositeIndex = SeparatorDecodeUtil.isSupperCompositeCode(realBarcodeByteData) + 3;
		    byte[] compositeByte = null;
//		    Log.d(TAG , "mayBeseparatorDecode ------- realCompositeIndex:" + realCompositeIndex);
		        if(realCompositeIndex>3 && realBarcodeByteData.length > realCompositeIndex) {
			    compositeByte = new byte[realBarcodeByteData.length - realCompositeIndex];
			    System.arraycopy(realBarcodeByteData , realCompositeIndex , compositeByte , 0 , compositeByte.length); // realCompositeIndex + 3 ignore ]e0
			    compositeIndex = realCompositeIndex;
		    } else {
			    compositeByte = new byte[realBarcodeByteData.length - compositeIndex];
			    System.arraycopy(realBarcodeByteData , compositeIndex , compositeByte , 0 , compositeByte.length); // compositeIndex + 3 ignore ]e0
		    }
		    byte[] compositeByteData = SeparatorDecodeUtil.separatorDecode(compositeByte);
//		    Log.d(TAG , "mayBeseparatorDecode ------- compositeByteData:" + (compositeByteData!=null?compositeByteData.length:-1) + ",compositeByte:" + (compositeByte!=null?compositeByte.length:-1));
		    if(compositeByteData != null && compositeByte.length != compositeByteData.length) {
			    resultSeparator = new byte[compositeIndex + compositeByteData.length];
			    System.arraycopy(realBarcodeByteData , 0 , resultSeparator , 0 , compositeIndex - 3);
			    System.arraycopy(compositeByteData , 0 , resultSeparator , compositeIndex - 3 , compositeByteData.length);
		    } else {
			    Log.d(TAG , "mayBeseparatorDecode , composite error!!! composite Byte len:" + compositeByte.length + " , Data len:" + (compositeByteData != null?compositeByteData.length:0) + ",composite:" + (compositeByteData != null));
			    resultSeparator = SeparatorDecodeUtil.separatorDecode(realBarcodeByteData);
		    }
		} else {
		    resultSeparator = SeparatorDecodeUtil.separatorDecode(realBarcodeByteData);
		}
/*
		for(int i=0;i<realBarcodeByteData.length;i++) {
		    Log.d(TAG , "mayBeseparatorDecode ------- realBarcodeByteData[" + i + "]:" + realBarcodeByteData[i]);
		}
		Log.d(TAG , "mayBeseparatorDecode +++++++++++++++++++++++++++++++++++++++++++++++++ ");
		for(int i=0;i<resultSeparator.length;i++) {
		    Log.d(TAG , "mayBeseparatorDecode ------- resultSeparator[" + i + "]:" + resultSeparator[i]);
		}
*/
	    // urovo modify shenpidong end 2019-11-28
                sendBroadcast(resultSeparator, codeID, resultSeparator.length);
            } else {
                int modeCodeID = mScanService.getPropertyInt(PropertyID.TRANSMIT_CODE_ID);
                if(modeCodeID > 0) {
                    sendBroadcast(barcodeByteData, codeID, barcodeByteData.length);
                } else {
                    sendBroadcast(realBarcodeByteData, codeID, realBarcodeByteData.length);
                }
            }
        } else {
//	    Log.d(TAG , "mayBeseparatorDecode ------- codeID:" + codeID + ",decResultLength:" + decResultLength + ",SEPARATOR_ENABLE:" + mScanService.getPropertyInt(PropertyID.LABEL_SEPARATOR_ENABLE));
            // urovo add shenpidong begin 2019-05-06
            if (mScanService != null && mScanService.isRemoveNonPrintChar()) {
                ScanUtil.searchLoopGSAndReplase(barcodeByteData);
            }
            // urovo add shenpidong end 2019-05-06
            sendBroadcast(barcodeByteData, codeID, barcodeByteData.length);
        }
    }

   /**
     * Return the current connection state. */
     private final class ProviderThread extends Thread {
        public ProviderThread() {
            super("DecoderProvider");
        }

        public void run() {
            Looper.prepare();
            mHandler = new ProviderHandler();
            // signal when we are initialized and ready to go
            mInitializedLatch.countDown();
            Looper.loop();
        }
    }

    @Override
    protected void release() {
        if (mBarCodeReader != null) {
            try {
                if (state == STATE_DECODE) {
                    mBarCodeReader.stopDecode();
                }
                mBarCodeReader.release();
            } catch (Exception e) {

            }
            mBarCodeReader = null;
        }
    }
     @Override
     public int setProperties(SparseArray<Integer> property) {
         // TODO Auto-generated method stub
         int size = property.size();
         Log.d(TAG, "setProperties property size= " + size);
         if(null != mBarCodeReader) {
             for(int i=0; i < size; i++) {
                 int keyForIndex = property.keyAt(i);
                 int internalIndex = mPropIndexHashMap.get(keyForIndex, SPECIAL_VALUE);
//         Log.d(TAG, "setProperties property ----- keyForIndex= " + keyForIndex + ",internalIndex:" + internalIndex);
                 if(internalIndex != SPECIAL_VALUE) {
                     int value = property.get(keyForIndex);
//		     Log.d(TAG, "setProperties property ------ value:" + value);
                     switch(keyForIndex) {
                          case PropertyID.EAN_EXT_ENABLE_2_5_DIGIT:
                              if(value == 1 ) {
                                  value = 0x02;
                              }
							  mBarCodeReader.setParameter(internalIndex, value);
                              break;
                          case PropertyID.COMPOSITE_CC_AB_ENABLE:
                              if(value == 1 ) {
                                  mBarCodeReader.setParameter(Se2100ParamIndex.COMPOSITE_CC_AB_ENABLE, value);
                                  if(mScanService.getPropertyInt(PropertyID.GS1_14_ENABLE) == 0) {
                                      mBarCodeReader.setParameter(Se2100ParamIndex.GS1_14_ENABLE, 1);
                                  }
                              }
                              break;
                          case PropertyID.COMPOSITE_TLC39_ENABLE:
                                  mBarCodeReader.setParameter(Se2100ParamIndex.COMPOSITE_TLC39_ENABLE, value);
                              break;
                          case PropertyID.COMPOSITE_CC_C_ENABLE:
                                  mBarCodeReader.setParameter(Se2100ParamIndex.COMPOSITE_CC_C_ENABLE, value);
                              break;
                          case PropertyID.DEC_PICKLIST_AIM_MODE: {
//				Log.d(TAG, "setProperties property ------ 1 PropertyID.DEC_2D_LIGHTS_MODE value:" + value + ",size:" + size + ",size:" + size);
                              if(value == 1 || value == 0) {
//                                  mBarCodeReader.setParameter(Se2100ParamIndex.DEC_PICKLIST_AIM_MODE, value);
				  if(value == 0 ) {
					boolean state = false;
				// size > 1 SCREEN_ON/SCREEN_OFF ignore
					if(size < 2) {
					    state = ScanUtil.update_LED_state(mScannerType , ScanUtil.AIMLED_SE2100_STATE , value ,
                                         ScanUtil.getMode(mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE)));
					}
//				      boolean state = ScanUtil.update_LED_state(mScannerType , ScanUtil.AIMLED_SE2100_STATE , value ,
//					 ScanUtil.getMode(mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE)));
				      int aim_parrern = mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE);
//				Log.d(TAG, "setProperties property ------ 11 PropertyID.DEC_2D_LIGHTS_MODE aim_parrern:" + aim_parrern + ",size:" + size + ",state:" + state);
					update_2D_Lights_Mode(aim_parrern);
				  } else {
				    update_2D_Lights_Mode(1);
//					mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 0);
//                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 0);
				  }
                              }
                            }
                              break;
                          case PropertyID.DEC_2D_LIGHTS_MODE: {
				int aim_led = mScanService.getPropertyInt(PropertyID.DEC_PICKLIST_AIM_MODE);
//				Log.d(TAG, "setProperties property ------ 2 PropertyID.DEC_2D_LIGHTS_MODE value:" + value + ",aim_led:" + aim_led + ",size:" + size);
				if(aim_led < 1) {
				    update_2D_Lights_Mode(value);
				}
/*
                                if(value == 0) {
                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 0);
                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 0);
                                } else if(value == 1) {
                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 0);
                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 1);
                                } else if(value == 2) {
                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 1);
                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 0);
                                } else if(value == 3) {
                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 1);
                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 1);
                                } else if(value == 4) {
                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 1);
                                    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 1);
                                }
*/
                               }
                            break;
			  case PropertyID.UPCA_SEND_SYS:
			  case PropertyID.UPCA_TO_EAN13:
				int upca_ean13 = mScanService.getPropertyInt(PropertyID.UPCA_TO_EAN13);
				int upca_sys = mScanService.getPropertyInt(PropertyID.UPCA_SEND_SYS);
//                            Log.d(TAG , "setProperties property PropertyID.UPCA_SEND_SYS internalIndex = " + internalIndex  + "   value == " + value + ",upca:" + upca_ean13 + ",upca_sys:" + upca_sys);
				if(upca_ean13 == 1) {
				    value = 2;
				} else if(upca_ean13 == 0 && upca_sys != value) {
				    value = upca_sys;
				}
//                            Log.d(TAG , "setProperties 22222222 property PropertyID.UPCA_SEND_SYS internalIndex = " + internalIndex  + "   value == " + value + ",upca:" + upca_ean13 + ",upca_sys:" + upca_sys);
				mBarCodeReader.setParameter(internalIndex, value);
			    break;
				// urovo add shenpidong begin 2019-04-18
			  case PropertyID.TRANSMIT_CODE_ID:
				updateTransmitCodeID(value);
			    break;
			// urovo add shenpidong end 2019-04-18
                          default:
				// urovo add shenpidong begin 2019-04-03
				Log.d(TAG , "setProperties property internalIndex = " + internalIndex  + "   value == " + value );
				if(internalIndex>=0) {
				    mBarCodeReader.setParameter(internalIndex, value);
				} else {
				    Log.d(TAG , "setProperties property ignore internalIndex = " + internalIndex  + "   value == " + value );
				}
				// urovo add shenpidong end 2019-04-03
                              break;
                     }
                 }
                 
             }
         }
         return 0;
     }

	// urovo add shenpidong begin 2019-04-18
    private void updateTransmitCodeID(int value) {
	if(value ==0 || value == 1) {
	    int ret = mBarCodeReader.setParameter(Se2100ParamIndex.TRANSMIT_CODE_ID, value);
	    Log.d(TAG , "updateTransmitCodeID , ret:" + ret);
	}
    }
    // urovo add shenpidong end 2019-04-18
	
    private void update_2D_Lights_Mode(int value) {
	// urovo add shenpidong begin 2019-05-29
	if(value == 0) {
	    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 0);
	    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 0);
	} else if(value == 1) {
	    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 1);
	    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 1);
	} else if(value == 2) {
	    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 1);
	    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 1);
	}/* else if(value == 3) {
	    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 1);
	    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 1);
	} else if(value == 4) {
	    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_ILLUM_MODE, 1);
	    mBarCodeReader.setParameter(Se2100ParamIndex.DEC_AIM_PATTERN, 1);
	}
*/
	// urovo add shenpidong end 2019-05-29
    }

	@Override
	public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDefaults() {
		// TODO Auto-generated method stub
	    synchronized (mHandler) {
            if (state != STATE_IDLE) return;
            if(mBarCodeReader != null) {
                mBarCodeReader.setDefaultParameters();
            }
            int size = defParamIndex.length;
            for(int i=0; i < size; i++) {
                mBarCodeReader.setParameter(defParamIndex[i], defParamVal[i]);
            }
            // Set parameter - Uncomment for QC/MTK platforms
			mBarCodeReader.setParameter(765, 0); // For QC/MTK platforms
			mBarCodeReader.setParameter(136,50);
			// urovo add shenpidong begin 2019-04-03
			int ret = mBarCodeReader.setParameter(8610,1);
			// urovo add shenpidong end 2019-04-03
//			ret = mBarCodeReader.setParameter(8611,1);
			ret = mBarCodeReader.setParameter(1881,1);
			ret = mBarCodeReader.setParameter(1882,1);
			// urovo add shenpidong begin 2019-04-18
			if(mScanService.getPropertyInt(PropertyID.LABEL_SEPARATOR_ENABLE) == 1) {
			    ret = mBarCodeReader.setParameter(Se2100ParamIndex.TRANSMIT_CODE_ID, 1);
			}
			// urovo add shenpidong end 2019-04-18
			updateAIM_OR_FLASH_LED(false);
//			mBarCodeReader.setParameter(764,1);
			Log.d(TAG , "setDefaults , ret:" + ret);
        }
	}

    @Override
    public boolean lockHwTriggler(boolean lock) {
        // TODO Auto-generated method stub
        return false;
    }

    private final int[] defParamIndex = new int[]{
            Se2100ParamIndex.CODE39_LENGTH1,
            Se2100ParamIndex.CODE39_LENGTH2,
            Se2100ParamIndex.D25_LENGTH1,
            Se2100ParamIndex.D25_LENGTH2,
            Se2100ParamIndex.M25_LENGTH1,
            Se2100ParamIndex.M25_LENGTH2,
            Se2100ParamIndex.I25_LENGTH1,
            Se2100ParamIndex.I25_LENGTH2,
            Se2100ParamIndex.CODABAR_ENABLE,
            Se2100ParamIndex.CODABAR_LENGTH1,
            Se2100ParamIndex.CODABAR_LENGTH2,
            Se2100ParamIndex.CODE93_LENGTH1,
            Se2100ParamIndex.CODE93_LENGTH2,
            Se2100ParamIndex.CODE128_LENGTH1,
            Se2100ParamIndex.CODE128_LENGTH2,
            Se2100ParamIndex.UPCA_SEND_CHECK,
            Se2100ParamIndex.UPCE_SEND_CHECK,
            Se2100ParamIndex.UPCE_SEND_SYS,
            Se2100ParamIndex.MSI_LENGTH1,
            Se2100ParamIndex.MSI_LENGTH2,
            Se2100ParamIndex.GS1_LIMIT_ENABLE,
            Se2100ParamIndex.GS1_EXP_ENABLE,
            Se2100ParamIndex.USPS_4STATE_ENABLE,
            Se2100ParamIndex.AUSTRALIAN_POST_ENABLE,
            Se2100ParamIndex.ROYAL_MAIL_SEND_CHECK ,
            Se2100ParamIndex.KIX_CODE_ENABLE,
            Se2100ParamIndex.JAPANESE_POST_ENABLE,
            Se2100ParamIndex.PDF417_ENABLE,
	    Se2100ParamIndex.EAN_EXT_ENABLE_2_5_DIGIT
//	    Se2100ParamIndex.EAN13_SEND_CHECK,
//	    Se2100ParamIndex.EAN8_SEND_CHECK
//	    Se2100ParamIndex.DEC_ILLUM_POWER_LEVEL,
    };
    
    private final int[] defParamVal = new int[] {
            1, //Se2100ParamIndex.CODE39_LENGTH1,
            20, //Se2100ParamIndex.CODE39_LENGTH2,
            2, //Se2100ParamIndex.D25_LENGTH1,
            50, //Se2100ParamIndex.D25_LENGTH2,
            2,
            50,
            2,
            50,
            1, 
            4, 
            20, 
            2, 
            20, 
            2, 
            40,
            1,
            1, 
            1,
            4,
            10, 
            1, 
            1, 
            1, 
            0, 
            0, 
            0,
            0,
            1,
            0
//	    1,
//	    1
    };
    static class Se2100ParamIndex {
        public static final int IMAGE_EXPOSURE_MODE = RESERVED_VALUE;
        public static final int IMAGE_FIXED_EXPOSURE = RESERVED_VALUE;
        public static final int IMAGE_PICKLIST_MODE = 716;
        public static final int IMAGE_ONE_D_INVERSE = 586;
        public final static int LASER_ON_TIME = BarCodeReader.ParamNum.LASER_ON_PRIM;//0x01-0x63 df 0x63 * 100 ms
        public final static int TIMEOUT_BETWEEN_SAME_SYMBOL = BarCodeReader.ParamNum.LASER_OFF_PRIM;//0x01-0x63 df 6
        public final static int LINEAR_CODE_TYPE_SECURITY_LEVEL = BarCodeReader.ParamNum.LIN_SEC_LEV;//1 2 3 4
        public static final int FUZZY_1D_PROCESSING = 514;
        public static final int MULTI_DECODE_MODE = 900;
        public static final int BAR_CODES_TO_READ = 902;
        public static final int FULL_READ_MODE = 901;
        public static final int CODE39_ENABLE = 0;//0 1 1
        public static final int CODE39_ENABLE_CHECK = 48;//0 1 0
        public static final int CODE39_SEND_CHECK = 43;//0 1 0 
        public static final int CODE39_FULL_ASCII = 17;//0 1 0 
        public static final int CODE39_LENGTH1 = 18;//0 55 2//df 1
        public static final int CODE39_LENGTH2 = 19;//0 55 55//df 20
        public static final int TRIOPTIC_ENABLE = 13;//0 1 0
        public static final int CODE32_ENABLE = 86;//0 1 0//TODO 2d 1d code32
        public static final int CODE32_SEND_CHECK = SPECIAL_VALUE; //RESERVED_VALUE;//TODO 2d 1d
        public static final int CODE32_SEND_START = 0xe7;//231;//TODO 2d 1d 0 1 0 adding the prefix character "A" to all Code 32 bar
        public static final int C25_ENABLE = 408;   //      0       1       0
        public static final int D25_ENABLE = 5;//0 1 0//TODO 2d 1d
        public static final int D25_ENABLE_CHECK = SPECIAL_VALUE; //RESERVED_VALUE;//TODO 2d 1d
        public static final int D25_SEND_CHECK = SPECIAL_VALUE; //RESERVED_VALUE;//TODO 2d 1d
        public static final int D25_2_BAR_START = SPECIAL_VALUE; //RESERVED_VALUE;
        public static final int D25_LENGTH1 = 20;//TODO 2d 1d 0 55 12//df6
        public static final int D25_LENGTH2 = 21;//TODO 2d 1d 0 55 0//df 10
        public static final int M25_ENABLE = 618;//TODO 2d 0 1 0
        public static final int M25_ENABLE_CHECK = SPECIAL_VALUE;//622;//TODO 2d 0 1 0
        public static final int M25_SEND_CHECK = SPECIAL_VALUE;//623;//TODO 2d 0 1 0
        public static final int M25_LENGTH1 = 619;//TODO 2d 0 55 14//df 6
        public static final int M25_LENGTH2 = 620;//TODO 2d 0 55 0//df 10
        public final static int CODE11_ENABLE = 0x0a;
        public final static int CODE11_ENABLE_CHECK = 0x34;
        public final static int CODE11_SEND_CHECK = 0x2f;
        public final static int CODE11_LENGTH1 = 0x1c;//min 2
        public final static int CODE11_LENGTH2 = 0x01d;//max 14
        public static final int I25_ENABLE = 6;//TODO 2d 1d 0 1 1
        public static final int I25_ENABLE_CHECK = 49;//TODO 2d 1d 0 2 0
        public static final int I25_SEND_CHECK = 44;//TODO 2d 1d 0 1 0
        public static final int I25_CASE_CODE = SPECIAL_VALUE;  
        public static final int I25_LENGTH1 = 22; //0 55 14//df6
        public static final int I25_LENGTH2 = 23; //0 55 0//df10
        public final static int I25_TO_EAN13 = 0x52;
        public static final int CODABAR_ENABLE = 7;//0 1 0//TODO df 1
        public static final int CODABAR_ENABLE_CHECK = 0xf2<<8|0x68; //TODO 2d  
        public static final int CODABAR_SEND_CHECK = 0xf2<<8|0x69; //TODO 2d  
        public static final int CODABAR_SEND_START = 55;//0 1 0 0xf2<<8|0x57
        public static final int CODABAR_NOTIS = 55;
        public static final int CODABAR_CLSI = 54;//TODO 2d 1d 0 1 0
        public static final int CODABAR_WIDE_GAPS = SPECIAL_VALUE; //RESERVED_VALUE;
        public static final int CODABAR_LENGTH1 = 24;//TODO 2d 1d 0 55 5 //df4
        public static final int CODABAR_LENGTH2 = 25;//TODO 2d 1d 0 55 55 //df 20
        public static final int CODE93_ENABLE = 9;//0 1 0
        public static final int CODE93_LENGTH1 = 26;//0 55 4//df 2
        public static final int CODE93_LENGTH2 = 27;//0 55 55//df20
        public static final int CODE128_ENABLE = 8;//0 1 1
        public static final int CODE128_EXT_ASCII = SPECIAL_VALUE; 
        public static final int CODE128_LENGTH1 = 209;// 2d 1d 0 55 0//df 2
        public static final int CODE128_LENGTH2 = 210;// 2d 1d 0 55 0//df 40
        public static final int CODE_ISBT_128 = 0x54;
        public static final int CODE128_GS1_ENABLE = 0x0e;      //gs1-128
        public static final int CODE128_GS1_LENGTH1 = 209;
        public static final int CODE128_GS1_LENGTH2 = 210;
        public static final int UPCA_ENABLE = 1;// 0 1 1
        public static final int UPCA_SEND_CHECK = 40;//TODO 0 1 1 df0
        public static final int UPCA_SEND_SYS =  34; // 0 2 1//TODO
        public static final int UPCA_TO_EAN13 = 34; //RESERVED_VALUE;//TODO 0 2 1
        public static final int UPCE_ENABLE = 2;//0 1 1
        public static final int UPCE_SEND_CHECK = 41;//0 1 1df 0
        public static final int UPCE_SEND_SYS =  35; // 0 2 1//TODOdf 0
        public static final int UPCE_TO_UPCA = 37;//0 1 0
        public final static int UPCE1_ENABLE = 0x0C;
        public final static int UPCE1_SEND_CHECK = 0x2A;
        public final static int UPCE1_SEND_SYS = 0x24;
        public final static int UPCE1_TO_UPCA = 0x26;
        public static final int EAN13_ENABLE = 3;//TODO 0 1 1
        public static final int EAN13_SEND_CHECK = SPECIAL_VALUE; //TODO
        public static final int EAN13_SEND_SYS = SPECIAL_VALUE; //TODO
        public static final int EAN13_TO_ISBN = 83;//TODO 0 1 0 prefix 978
        public static final int EAN13_BOOKLANDEAN = 83;
        public static final int EAN13_BOOKLAND_FORMAT = 576;
        public static final int EAN13_TO_ISSN = 617;//TODO0 1 0 prefix 977
        public static final int EAN8_ENABLE = 4;//TODO 0 1 1
        public static final int EAN8_SEND_CHECK = SPECIAL_VALUE; //TODO
        public static final int EAN8_TO_EAN13 = 39;//0 1 0
        public static final int EAN_EXT_ENABLE_2_5_DIGIT = 0x10;//TODO to see upc-a upc-e ena-13 ena-8  ??????????????
        public final static int UPC_EAN_SECURITY_LEVEL = 0x4d;//        0       3       1
        public final static int UCC_COUPON_EXT_CODE= 0x55;//        0       1       0
        public static final int MSI_ENABLE = 11;// 0 1 0
        public static final int MSI_REQUIRE_2_CHECK = 50;//TODO 0 1 0
        public static final int MSI_SEND_CHECK = 46;//TODO 0 1 0
        public static final int MSI_CHECK_2_MOD_11 = 51;//TODO 0 1 1
        public static final int MSI_LENGTH1 = 30;//TODO 0 55 4 df 4
        public static final int MSI_LENGTH2 = 31;//TODO 0 55 55 df 10
        public static final int GS1_14_ENABLE = 338; //0 1 1
        public static final int GS1_14_TO_UPC_EAN = 427; 
        public static final int GS1_14_TO_GS1_128 = SPECIAL_VALUE; 
        public static final int GS1_14_REQUIRE_2D = SPECIAL_VALUE;  
        public static final int GS1_LIMIT_ENABLE = 339;// 0 1 0 df 1
        public static final int GS1_LIMIT_TO_GS1_128 = SPECIAL_VALUE;  
        public static final int GS1_LIMIT_REQUIRE_2D = SPECIAL_VALUE;  
        public static final int GS1_EXP_ENABLE = 340;//0 1 0 df 1
        public static final int GS1_EXP_TO_GS1_128 = SPECIAL_VALUE; 
        public static final int GS1_EXP_REQUIRE_2D = RESERVED_VALUE;
        public static final int GS1_EXP_LENGTH1 = SPECIAL_VALUE;  //TODO
        public static final int GS1_EXP_LENGTH2 = SPECIAL_VALUE;  //TODO
        public static final int US_POSTNET_ENABLE = 0x59;//89TODO 0 1 1
        public static final int US_PLANET_ENABLE = 0x5a;//90TODO 0 1 1
        public static final int US_POSTAL_SEND_CHECK = 0x5f;//95TODO 0 1 1
        public static final int UK_POSTAL_ENABLE = 0x5b;
        public static final int UK_POSTAL_SEND_CHECK = 96;
        public static final int USPS_4STATE_ENABLE = 592;//TODO 0 1 0 df 1
        public static final int UPU_FICS_ENABLE = 611;//TODO 0 1 0
        public static final int ROYAL_MAIL_ENABLE = 91;//TODO 0 1 1
        public static final int ROYAL_MAIL_SEND_CHECK = 96;//TODO 0 1 1df0
        /*• *0 - Autodiscriminate (or Smart mode) - Attempt to decode the Customer Information Field using the N
        and C Encoding Tables.
        • 1 - Raw Format - Output raw bar patterns as a series of numbers 0 through 3.
        • 2 - Alphanumeric Encoding - Decode the Customer Information Field using the C Encoding Table.
        • 3 - Numeric Encoding - Decode the Customer Information Field using the N Encoding Table.*/
        public static final int AUSTRALIAN_POST_FORMAT = 718;
        public static final int AUSTRALIAN_POST_ENABLE = 291;//TODO 0 1 1df 0
        public static final int KIX_CODE_ENABLE = 326;//TODO 0 1 1df0
        public static final int JAPANESE_POST_ENABLE = 290;//TODO 0 1 1df0
        public static final int PDF417_ENABLE = 15;//0 1 1
        public static final int PDF417_LENGTH1 = SPECIAL_VALUE; //TODO
        public static final int PDF417_LENGTH2 = SPECIAL_VALUE; //TODO
        public static final int MICROPDF417_ENABLE = 0xe3;// 0 1 0
        public static final int MICROPDF417_LENGTH1 = SPECIAL_VALUE; //TODO;
        public static final int MICROPDF417_LENGTH2 = SPECIAL_VALUE; //TODO;
        public static final int COMPOSITE_CC_AB_ENABLE = 342;     //composite-cc_ab
        public static final int COMPOSITE_CC_AB_LENGTH1 = SPECIAL_VALUE; //TODO
        public static final int COMPOSITE_CC_AB_LENGTH2 = SPECIAL_VALUE; //TODO
        public static final int COMPOSITE_CC_C_ENABLE = 341;     //composite-cc_c
        public static final int COMPOSITE_CC_C_LENGTH1 = SPECIAL_VALUE; //TODO
        public static final int COMPOSITE_CC_C_LENGTH2 = SPECIAL_VALUE; //TODO
        public final static int COMPOSITE_TLC39_ENABLE = 371;
        public static final int HANXIN_ENABLE =  1167;
        public static final int HANXIN_INVERSE =  1168;
        public static final int DATAMATRIX_ENABLE = 292;//0 1 1
        public static final int DATAMATRIX_LENGTH1 = 619; 
        public static final int DATAMATRIX_LENGTH2 = 620;
        public static final int DATAMATRIX_INVERSE = 588;
        public static final int MAXICODE_ENABLE = 294;// 0 1 1
        public static final int MAXICODE_LENGTH1 = SPECIAL_VALUE; //TODO
        public static final int MAXICODE_LENGTH2 = SPECIAL_VALUE; //TODO
        public static final int QRCODE_ENABLE = 293;//0 1 1 df 0
        public static final int MICROQRCODE_ENABLE = 573;//TODO 0 1 1
        public static final int QRCODE_LENGTH1 = SPECIAL_VALUE; //TODO
        public static final int QRCODE_LENGTH2 = SPECIAL_VALUE; //TODO
        public static final int QRCODE_INVERSE = 587;
        public static final int AZTEC_ENABLE = 574;//0x24d 0 1 1
        public static final int AZTEC_LENGTH1 = SPECIAL_VALUE; //TODO
        public static final int AZTEC_LENGTH2 = SPECIAL_VALUE; //TODO
        public static final int AZTEC_INVERSE = 589;
        public static final int DEC_ILLUM_POWER_LEVEL = 764;
        public static final int DEC_PICKLIST_AIM_MODE = 402;
        public static final int DEC_ILLUM_MODE = 298;
        public static final int DEC_AIM_PATTERN = 306;
        public static final int DEC_HANDS_FREE_AIM_PATTERN = 590;
        public static final int DEC_2D_LIGHTS_MODE = 1000;
        public static final int TRANSMIT_CODE_ID = 45;
		public static final int LINEAR_1D_QUIET_ZONE_LEVEL= 1000;
		public static final int CODE39_Quiet_Zone= 1000;
		public static final int CODE39_SECURITY_LEVEL= 1000;
		public static final int I25_QUIET_ZONE= 1000;
		public static final int I25_SECURITY_LEVEL= 1000;
		public static final int CODE128_REDUCED_QUIET_ZONE= 1000;
		public static final int CODE128_CHECK_ISBT_TABLE= 1000;
		public static final int CODE_ISBT_Concatenation_MODE= 1000;
		public static final int CODE128_SECURITY_LEVEL= 1000;
		public static final int CODE128_IGNORE_FNC4= 1000;
		public static final int UCC_REDUCED_QUIET_ZONE= 1000;
		public static final int UCC_COUPON_EXT_REPORT_MODE= 1000;
		public static final int UCC_EAN_ZERO_EXTEND= 1000;
		public static final int UCC_EAN_SUPPLEMENTAL_MODE= 1000;
		public static final int GS1_LIMIT_Security_Level= 1000;
		public static final int COMPOSITE_UPC_MODE= 1000;
		public static final int KOREA_POST_ENABLE= 1000;
		public static final int Canadian_POSTAL_ENABLE= 1000;
    }
	private final int[] VALUE_PARAM_INDEX = {
	        Se2100ParamIndex.IMAGE_EXPOSURE_MODE,
	        Se2100ParamIndex.IMAGE_FIXED_EXPOSURE,
            Se2100ParamIndex.IMAGE_PICKLIST_MODE,
            Se2100ParamIndex.IMAGE_ONE_D_INVERSE,
            Se2100ParamIndex.LASER_ON_TIME,
            Se2100ParamIndex.TIMEOUT_BETWEEN_SAME_SYMBOL,
            Se2100ParamIndex.LINEAR_CODE_TYPE_SECURITY_LEVEL,
            Se2100ParamIndex.FUZZY_1D_PROCESSING,
            Se2100ParamIndex.MULTI_DECODE_MODE,
            Se2100ParamIndex.BAR_CODES_TO_READ,
            Se2100ParamIndex.FULL_READ_MODE,
            Se2100ParamIndex.CODE39_ENABLE,
            Se2100ParamIndex.CODE39_ENABLE_CHECK,
            Se2100ParamIndex.CODE39_SEND_CHECK,
            Se2100ParamIndex.CODE39_FULL_ASCII,
            Se2100ParamIndex.CODE39_LENGTH1,
            Se2100ParamIndex.CODE39_LENGTH2,
            Se2100ParamIndex.TRIOPTIC_ENABLE,
            Se2100ParamIndex.CODE32_ENABLE,
            Se2100ParamIndex.CODE32_SEND_START,
            Se2100ParamIndex.C25_ENABLE,
            Se2100ParamIndex.D25_ENABLE, 
            Se2100ParamIndex.D25_LENGTH1,
            Se2100ParamIndex.D25_LENGTH2,
            Se2100ParamIndex.M25_ENABLE,
            Se2100ParamIndex.CODE11_ENABLE,
            Se2100ParamIndex.CODE11_ENABLE_CHECK,
            Se2100ParamIndex.CODE11_SEND_CHECK,
            Se2100ParamIndex.CODE11_LENGTH1,
            Se2100ParamIndex.CODE11_LENGTH2,
            Se2100ParamIndex.I25_ENABLE,
            Se2100ParamIndex.I25_ENABLE_CHECK,
            Se2100ParamIndex.I25_SEND_CHECK,
            Se2100ParamIndex.I25_LENGTH1,
            Se2100ParamIndex.I25_LENGTH2,
            Se2100ParamIndex.I25_TO_EAN13,
            Se2100ParamIndex.CODABAR_ENABLE,
            Se2100ParamIndex.CODABAR_NOTIS,
            Se2100ParamIndex.CODABAR_CLSI,
            Se2100ParamIndex.CODABAR_LENGTH1,
            Se2100ParamIndex.CODABAR_LENGTH2,
            Se2100ParamIndex.CODE93_ENABLE,
            Se2100ParamIndex.CODE93_LENGTH1,
            Se2100ParamIndex.CODE93_LENGTH2,
            Se2100ParamIndex.CODE128_ENABLE,
            Se2100ParamIndex.CODE128_LENGTH1,
            Se2100ParamIndex.CODE128_LENGTH2,
            Se2100ParamIndex.CODE_ISBT_128,
            Se2100ParamIndex.CODE128_GS1_ENABLE,
            Se2100ParamIndex.UPCA_ENABLE, 
            Se2100ParamIndex.UPCA_SEND_CHECK,
            Se2100ParamIndex.UPCA_SEND_SYS,
            Se2100ParamIndex.UPCA_TO_EAN13,
            Se2100ParamIndex.UPCE_ENABLE,
            Se2100ParamIndex.UPCE_SEND_CHECK,
            Se2100ParamIndex.UPCE_SEND_SYS,
            Se2100ParamIndex.UPCE_TO_UPCA,
            Se2100ParamIndex.UPCE1_ENABLE,
            Se2100ParamIndex.UPCE1_SEND_CHECK,
            Se2100ParamIndex.UPCE1_SEND_SYS,
            Se2100ParamIndex.UPCE1_TO_UPCA,
            Se2100ParamIndex.EAN13_ENABLE,
            Se2100ParamIndex.EAN13_BOOKLANDEAN,
            Se2100ParamIndex.EAN13_BOOKLAND_FORMAT,
            Se2100ParamIndex.EAN8_ENABLE,
            Se2100ParamIndex.EAN8_TO_EAN13,
            Se2100ParamIndex.EAN_EXT_ENABLE_2_5_DIGIT,
            Se2100ParamIndex.UPC_EAN_SECURITY_LEVEL,
            Se2100ParamIndex.UCC_COUPON_EXT_CODE,
            Se2100ParamIndex.MSI_ENABLE,
            Se2100ParamIndex.MSI_REQUIRE_2_CHECK,
            Se2100ParamIndex.MSI_SEND_CHECK,
            Se2100ParamIndex.MSI_CHECK_2_MOD_11,
            Se2100ParamIndex.MSI_LENGTH1,
            Se2100ParamIndex.MSI_LENGTH2,
            Se2100ParamIndex.GS1_14_ENABLE,
            Se2100ParamIndex.GS1_14_TO_UPC_EAN,
            Se2100ParamIndex.GS1_LIMIT_ENABLE,
            Se2100ParamIndex.GS1_EXP_ENABLE,
            Se2100ParamIndex.GS1_EXP_LENGTH1,
            Se2100ParamIndex.GS1_EXP_LENGTH2,
            Se2100ParamIndex.US_POSTNET_ENABLE,
            Se2100ParamIndex.US_PLANET_ENABLE,
            Se2100ParamIndex.US_POSTAL_SEND_CHECK,
            Se2100ParamIndex.USPS_4STATE_ENABLE,
            Se2100ParamIndex.UPU_FICS_ENABLE,
            Se2100ParamIndex.ROYAL_MAIL_ENABLE,
            Se2100ParamIndex.ROYAL_MAIL_SEND_CHECK,
            Se2100ParamIndex.AUSTRALIAN_POST_ENABLE,
            Se2100ParamIndex.KIX_CODE_ENABLE,
            Se2100ParamIndex.JAPANESE_POST_ENABLE,
            Se2100ParamIndex.PDF417_ENABLE,
            Se2100ParamIndex.MICROPDF417_ENABLE,
            Se2100ParamIndex.COMPOSITE_CC_AB_ENABLE,
            Se2100ParamIndex.COMPOSITE_CC_C_ENABLE,
            Se2100ParamIndex.COMPOSITE_TLC39_ENABLE,
            Se2100ParamIndex.HANXIN_ENABLE,
            Se2100ParamIndex.HANXIN_INVERSE,
            Se2100ParamIndex.DATAMATRIX_ENABLE,
            Se2100ParamIndex.DATAMATRIX_LENGTH1,
            Se2100ParamIndex.DATAMATRIX_LENGTH2,
            Se2100ParamIndex.DATAMATRIX_INVERSE,
            Se2100ParamIndex.MAXICODE_ENABLE,
            Se2100ParamIndex.QRCODE_ENABLE,
            Se2100ParamIndex.QRCODE_INVERSE,
            Se2100ParamIndex.MICROQRCODE_ENABLE,
            Se2100ParamIndex.AZTEC_ENABLE,
            Se2100ParamIndex.AZTEC_INVERSE,
            Se2100ParamIndex.DEC_2D_LIGHTS_MODE,
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
            Se2100ParamIndex.DEC_ILLUM_POWER_LEVEL,
            Se2100ParamIndex.DEC_PICKLIST_AIM_MODE,
            DEC_PICKLIST_AIM_DELAY,
            DEC_MaxMultiRead_COUNT,
            DEC_Multiple_Decode_TIMEOUT,
            DEC_Multiple_Decode_INTERVAL,
            DEC_Multiple_Decode_MODE,
            DEC_OCR_MODE,
            DEC_OCR_TEMPLATE,
            Se2100ParamIndex.TRANSMIT_CODE_ID,
            DOTCODE_ENABLE,
			Se2100ParamIndex.LINEAR_1D_QUIET_ZONE_LEVEL,
			Se2100ParamIndex.CODE39_Quiet_Zone,
			CODE39_START_STOP,
			Se2100ParamIndex.CODE39_SECURITY_LEVEL,
			Se2100ParamIndex.M25_SEND_CHECK,
			Se2100ParamIndex.M25_LENGTH1,
			Se2100ParamIndex.M25_LENGTH2,
			Se2100ParamIndex.I25_QUIET_ZONE,
			Se2100ParamIndex.I25_SECURITY_LEVEL,
			CODABAR_ENABLE_CHECK,
			CODABAR_SEND_CHECK,
			CODABAR_SEND_START,
			CODABAR_CONCATENATE,
			Se2100ParamIndex.CODE128_REDUCED_QUIET_ZONE,
			Se2100ParamIndex.CODE128_CHECK_ISBT_TABLE,
			Se2100ParamIndex.CODE_ISBT_Concatenation_MODE,
			Se2100ParamIndex.CODE128_SECURITY_LEVEL,
			Se2100ParamIndex.CODE128_IGNORE_FNC4,
			Se2100ParamIndex.UCC_REDUCED_QUIET_ZONE,
			Se2100ParamIndex.UCC_COUPON_EXT_REPORT_MODE,
			Se2100ParamIndex.UCC_EAN_ZERO_EXTEND,
			Se2100ParamIndex.UCC_EAN_SUPPLEMENTAL_MODE,
			Se2100ParamIndex.GS1_LIMIT_Security_Level,
			Se2100ParamIndex.COMPOSITE_UPC_MODE,
			POSTAL_GROUP_TYPE_ENABLE,
			Se2100ParamIndex.KOREA_POST_ENABLE,
			Se2100ParamIndex.Canadian_POSTAL_ENABLE,
    };
}
