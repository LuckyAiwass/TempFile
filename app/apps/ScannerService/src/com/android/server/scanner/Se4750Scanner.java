package com.android.server.scanner;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
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

import com.android.server.ScanServiceWrapper;
import com.zebra.adc.decoder.BarCodeReader;
import com.zebra.adc.decoder.BarCodeReader.DecodeCallback;
import com.zebra.adc.decoder.BarCodeReader.ErrorCallback;
import android.hardware.Camera.CameraInfo;
// urovo add shenpidong begin 2019-04-18
import android.text.TextUtils;
// urovo add shenpidong end 2019-04-18
//import android.content.BroadcastReceiver;

public class Se4750Scanner extends Scanner implements DecodeCallback, BarCodeReader.ErrorCallback, BarCodeReader.PictureCallback{
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
	private static boolean mScanState = false;
	private static boolean mScanStateError = false;
    private final Thread mThread;
    // Handler for processing events in mThread.
    private ProviderHandler mHandler;
//    private static final String UPDATE_PARAM = "com.urovo.se2100";
    private final CountDownLatch mInitializedLatch = new CountDownLatch(1);
    private byte[] lastImageData = null;
    private boolean enableLastDecImage = false;
    private int imgRotate = 180;
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
        }
    }*/

	private static final String TAG = "Se4750Scanner";

	static {
		System.loadLibrary("IAL_4710");
		System.loadLibrary("SDL_4710");
//		System.loadLibrary("barcodereader44");
//		System.loadLibrary("IAL_2100");
//		System.loadLibrary("SDL_2100");
		System.loadLibrary("barcodereader44_4710");
	}
	public Se4750Scanner(ScanServiceWrapper scanService) {
		mScannerType = ScannerFactory.TYPE_SE4500;
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
        IntentFilter filter = new IntentFilter("action.scanner_capture_image");
        filter.addAction("com.ubx.barcode.action_config");
        mScanService.getContext().registerReceiver(mReceiver, filter);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("action.scanner_capture_image".equals(action)) {
                if (mHandler != null) {
                    Message m = Message.obtain(mHandler, MESSAGE_CAPTURE_IMAGE);
                    imgRotate = intent.getIntExtra("rotate", 180);
                    mHandler.sendMessage(m);
                }
            } else {
                if (mBarCodeReader != null) {
                    try {
                        int property = intent.getIntExtra("property", 0);
                        int value = intent.getIntExtra("value", 0);
                        if (property > 0) {
                            int ret = mBarCodeReader.setParameter(property, value);
                            Log.d(TAG, "property=" + property + " value " + value + " ret ="+ret);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
    };

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
//        synchronized (mHandler) {
	    openDecoder();
/*
            mHandler.removeMessages(MESSAGE_ENABLE);
            Message m = Message.obtain(mHandler, MESSAGE_ENABLE);
            m.arg1 = 1;
            mHandler.sendMessage(m);
*/
//        }
	return true;
    }

    public void close() {
        synchronized (mHandler) {
	    if(mBarCodeReader!=null) {
	    // urovo add shenpidong begin 2019-02-25 , close ISP
	    int ret = mBarCodeReader.setParameter(8610,0);
		Log.i(TAG, "close start.....8610.ret:" + ret);
	    // urovo add shenpidong end 2019-02-25 , close ISP
	    } else {
	    Log.i(TAG, "close start......BarCodeReader:" + (mBarCodeReader!=null));
	    }
	    Log.i(TAG, "close start 2......BarCodeReader:" + (mBarCodeReader!=null));
	    closeDecoder();
	    Log.i(TAG, "close start 3.....BarCodeReader:" + (mBarCodeReader!=null));
/*
            mHandler.removeMessages(MESSAGE_ENABLE);
            Message m = Message.obtain(mHandler, MESSAGE_ENABLE);
            m.arg1 = 0;
            mHandler.sendMessage(m);
*/
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
//		Log.d(TAG , "stopDecode 44 ----- isContinueScan:" + isContinueScan);
		    isContinueScan = false;
//                if(mBarCodeReader != null) {
		synchronized (decodeEvent) {
//            Log.d(TAG , "stopDecode , is continueScan:" + isContinueScan);
                    decodeEvent.notify();
		}
//                    int ret = mBarCodeReader.setParameter(BarCodeReader.ParamNum.PRIM_TRIG_MODE, BarCodeReader.ParamVal.LEVEL);
//                    if (ret != BarCodeReader.BCR_ERROR) Log.i(TAG,"reset Level trigger mode faile!");
//                    mBarCodeReader.stopDecode();
                }
		mScanState = false;
//            } else {
                mHandler.removeMessages(MESSAGE_CODE_ENABLE);
                Message m = Message.obtain(mHandler, MESSAGE_CODE_ENABLE);
                m.arg1 = 0;
                mHandler.sendMessage(m);
//		Log.d(TAG , "stopDecode ----- isContinueScan:" + isContinueScan);
//            }
            
        }
    }
    
    private static final int MESSAGE_ENABLE = 0;
    private static final int MESSAGE_CODE_ENABLE =1;
    private static final int MESSAGE_SET_PROPERTY = 2;
    private static final int MESSAGE_DECODE_TIMEOUT = 3;
    private static final int MESSAGE_TARGET_TIMEOUT = 4;
    private static final int MESSAGE_SET_DEFAULT =5;
    private static final int MESSAGE_CAPTURE_IMAGE = 6;
    private final class ProviderHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            int message = msg.what;
            switch (message) {
                case MESSAGE_ENABLE:
			Log.i(TAG, "handleMessage ...... msg.arg1:" + msg.arg1);
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
				case MESSAGE_CAPTURE_IMAGE:
                    setLastDecImage();
                    if(lastImageData != null) {
                        captureImage(lastImageData);
                        lastImageData = null;
                    } else {
                    if(mBarCodeReader != null)
                        mBarCodeReader.takePicture(Se4750Scanner.this);
                    }
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
    }
    private void setLastDecImage() {
        try {
            if(mBarCodeReader != null) {
                if(mBarCodeReader.getNumParameter(905) != 1) {
                    //get decode image
                    mBarCodeReader.setParameter(905, 1);
                    /*Parameter # 304 . Select an image format appropriate for the system. The decoder stores captured images in the 
                    selected format:
                    1 - JPEG File Format --- default
                    3 - BMP File Format
                    4 - TIFF File Format*/
                    mBarCodeReader.setParameter(304, 3);
                }
                enableLastDecImage = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void captureImage(byte[] imagedata) {
        try {
            Intent intent = new Intent("scanner_capture_image_result");
            Bundle b = new Bundle();
            if (imagedata != null && imagedata.length > 0) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(imagedata, 0, imagedata.length);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() - 80, bitmap.getHeight());
                if (imgRotate > 0 && imgRotate <= 270) {
                    Matrix matrix = new Matrix();
                    matrix.setRotate(imgRotate);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
                }
                java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                b.putByteArray("bitmapBytes", baos.toByteArray());
                bitmap.recycle();
                bitmap = null;
                lastImageData = null;
            } else {
                b.putByteArray("bitmapBytes", new byte[1]);
            }
            intent.putExtras(b);
            mScanService.getContext().sendBroadcastAsUser(intent, android.os.UserHandle.ALL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stopCoder() {
        // TODO Auto-generated method stub
        Log.i(TAG, "stopCoder start mBarCodeReader:" + (mBarCodeReader != null));
//		if(mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == 4) {
//		    if(mBarCodeReader != null) {
/*		        int ret = mBarCodeReader.setParameter(BarCodeReader.ParamNum.PRIM_TRIG_MODE, BarCodeReader.ParamVal.LEVEL);
	            if (ret != BarCodeReader.BCR_ERROR) Log.i(TAG,"reset Level trigger mode faile!");
	            mBarCodeReader.stopDecode();
*/
//		    }
//		} else {
		    if (mBarCodeReader != null) {
	            if(state == STATE_DECODE) {
	            try{
	                mBarCodeReader.stopDecode();
	            }catch(Exception e){
	                e.printStackTrace();
	            Log.i(TAG,"stopDecode no exit");
	            }
	            }
	            state = STATE_IDLE;
//	        }
		//}
	    }
		Log.i(TAG,"stopCoder end");
	}

	private long startTime = -1;
	public void startCoder() {
	    mScanStateError = false;
//	    mScanState = false;
	    isContinueScan = mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == 4;
		Log.i(TAG,"startCoder , isContinueScan:" + isContinueScan + ",decodeNotified:" + decodeNotified);
		// TODO Auto-generated method stub
		if(isContinueScan) {
		    if (mBarCodeReader != null) {
		Log.i(TAG,"startCoder , decodeNotified:" + decodeNotified);

			if(mScanState) {
				decodeNotified = false;
			    isContinueScan = false;
		synchronized (decodeEvent) {
				decodeEvent.notify();
		}
				try {
						Thread.sleep(50);	
					} catch (InterruptedException e1) {
					    Log.e(TAG,"in startCoder, restart and delay exception!!!!!");
					}
//			    mDecodeNotifyThread.stop();
			}

			if(decodeNotified && !mScanState) {
			    decodeNotified = false;
			    isContinueScan = false;
			    mScanState = false;
			    mBarCodeReader.stopDecode();
			} else {
			    isContinueScan = true;
			    decodeNotified = true;
			    mScanState = true;
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
			// urovo add shenpidong begin 2019-06-10
			int value = mBarCodeReader.getNumParameter(137);
			int ret_start = -1;
			startTime = System.currentTimeMillis();
				Log.e(TAG,"will call decode lib startCoder ***** startTime:" + startTime);
				//mBarCodeReader.setParameter(-1,255); //add by qiuzhoujun, debug logging
				//mBarCodeReader.setParameter(-2,1); //add by qiuzhoujun
			if(value > 10) {
		        ret_start = mBarCodeReader.startHandsFreeDecode(BarCodeReader.ParamVal.HANDSFREE);
			} else {
			ret_start =  mBarCodeReader.startDecode(); // start decode (callback gets results)
			}
			// urovo add shenpidong end 2019-06-10
	            Log.e(TAG,"retrun val startCoder ret_start:" + ret_start + ",value:" + value);
			    int retryCount = 0;
			    while(ret_start == -1 && retryCount < 5)
			   	{
				retryCount++;
					//mBarCodeReader.stopDecode();
					try {
						Thread.sleep(50);	
					} catch (InterruptedException e1)
	            	{Log.e(TAG,"in startCoder, and delay exception!!!!!");}
			// urovo add shenpidong begin 2019-06-10
				value = mBarCodeReader.getNumParameter(137);
			startTime = System.currentTimeMillis();
					if(value > 10) {
					ret_start = mBarCodeReader.startHandsFreeDecode(BarCodeReader.ParamVal.HANDSFREE);
					} else {
					ret_start = mBarCodeReader.startDecode();
					}
			// urovo add shenpidong end 2019-06-10
			    }
	            }catch(Exception e){
	                e.printStackTrace();
	                Log.i(TAG,"startDecode no exit");
	            }
	          }
		}
	}

    private static boolean isContinueScan = false;
    private boolean decodeNotified = false;
    private final Object decodeEvent = new Object();
    private class DecodeNotifyThread extends Thread {
	@Override
	public void run() {
	    while (mEnabled && isContinueScan && !mScanStateError) {
		synchronized (decodeEvent) {
		    Log.d(TAG , "run , decodeNotified:" + decodeNotified + ",isContinueScan:" + isContinueScan);
		    while (mEnabled && !decodeNotified && !mScanStateError) {
			try {
			    Log.d(TAG , "run , pre decodeEvent.wait");
			    if(mEnabled && !mScanStateError)
			    decodeEvent.wait();
			    Log.d(TAG , "run , next decodeEvent.wait decodeNotified:" + decodeNotified);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
			if(!mScanStateError)
			decodeNotified = true;
		    }
		}
		Log.d(TAG , "run , decodeNotified:" + decodeNotified + ",isContinueScan:" + isContinueScan);
		if(!isContinueScan) {
		    decodeNotified = false;
		    state = STATE_DECODE;
		    stopCoder();
		    Log.d(TAG , "run , stopDecode");
		    return;
		}
		try {
		    Thread.sleep(50);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		state = STATE_DECODE;
		Log.d(TAG , "run , startDecode decodeNotified:" + decodeNotified + ",mScanStateError:" + mScanStateError);
		startTime = System.currentTimeMillis();
		// urovo add shenpidong begin 2019-06-10
		int ret_start =  -1;
		int value = -1;
		if(mEnabled && !mScanStateError) {
		value = mBarCodeReader.getNumParameter(137);
		if(value > 10) {
		ret_start = mBarCodeReader.startHandsFreeDecode(BarCodeReader.ParamVal.HANDSFREE);
		} else {
		ret_start =  mBarCodeReader.startDecode();
		}
		// urovo add shenpidong begin 2019-06-10
		}
		int retryCount = 0;
		while(mEnabled && ret_start == -1 && decodeNotified && (retryCount < 5)) {
		    try {
			if(mEnabled) {
			    mBarCodeReader.stopDecode();
			}
			Thread.sleep(50);
		    } catch(InterruptedException e1) {
			Log.e(TAG,"run in startCoder, and delay exception!!!!!");
		    }
		    startTime = System.currentTimeMillis();
		    if(mEnabled) {
			// urovo add shenpidong begin 2019-06-10
			value = mBarCodeReader.getNumParameter(137);
			if(value > 10) {
			    ret_start = mBarCodeReader.startHandsFreeDecode(BarCodeReader.ParamVal.HANDSFREE);
			} else {
			    ret_start =  mBarCodeReader.startDecode();
			}
			// urovo add shenpidong begin 2019-06-10
		    }
		    retryCount++;
		    Log.d(TAG , "run while , startDecode ret_start:" + ret_start + ",mScanStateError:" + mScanStateError + ",retryCount:" + retryCount);
		}
		Log.d(TAG , "run , startDecode ret_start:" + ret_start + ",mScanStateError:" + mScanStateError);
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

    private int scannerID() {
	int num = android.hardware.Camera.getNumberOfCameras();
	Log.i(TAG, "scannerID , num:" + num);
	int scanID = 0;	
	/*
	CameraInfo info = new CameraInfo();
	for(int i=0 ; i < num ; i++ ) {
	    android.hardware.Camera.getCameraInfo(i, info);
	    if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
		Log.i(TAG, "scannerID , back camera found:" + i);
		scanID++;
	    } else if(info.facing == CameraInfo.CAMERA_FACING_FRONT) {
		Log.i(TAG, "scannerID , front camera found:" + i);
		scanID++;
	    } else {
		Log.i(TAG, "scannerID , other camera found:" + i);
//		scanID = i;
	    }
	}
	*/
	scanID=num-1; //juzhitao add
	Log.e(TAG, "scannerID , num:" + num + ",scanID:" + scanID);
	return scanID;
    }

	public synchronized void openDecoder() {
		Log.i(TAG, "openCoder start , mEnabled:" + mEnabled);
		if (mEnabled)
			return;
		state = STATE_IDLE;
        try{
	// urovo add shenpidong begin 2019-04-17
//            mBarCodeReader = BarCodeReader.open(mScanService.mContext);
	    // urovo add shenpidong begin 2019-03-04
         // gcz modify for SQ38
        int scan_index = 1;
        if(!"SQ38".equals(android.os.Build.PROJECT))
            scan_index = ScanUtil.scannerID();//scannerID();
        // urovo add shenpidong end 2019-03-04
	    //mBarCodeReader = BarCodeReader.open((scan_index>=0 && scan_index<=2)?scan_index:2, mScanService.mContext);
	    mBarCodeReader = BarCodeReader.open(scan_index, mScanService.mContext); //juzhitao modify
	// urovo add shenpidong end 2019-04-17
	// urovo add shenpidong begin 2019-04-23
	    if(mBarCodeReader!=null) {
		String sMod = mBarCodeReader.getStrProperty(BarCodeReader.PropertyNum.MODEL_NUMBER);
		Log.d(TAG , "openCoder , BarCodeReader:" + (mBarCodeReader!=null) + ",sMod:" + sMod);
		// urovo add shenpidong begin 2019-08-14
//		if(!"SE4710-LM000R".equals(sMod!=null?sMod.trim():null)) {
		if(!"SE4750MR-IM100R".equals(sMod!=null?sMod.trim():null) && !"SE4750MR-IM000R".equals(sMod!=null?sMod.trim():null)) {
		    Log.d(TAG , "openCoder , " + sMod + " isn't 4750.");
		    mBarCodeReader.release();
		    mBarCodeReader = null;
		    return;
		}
		// urovo add shenpidong end 2019-08-14
	    }
	// urovo add shenpidong end 2019-04-23
            }catch(Exception e){
            mBarCodeReader = null;
	    e.printStackTrace();
        }
		if (mBarCodeReader != null) {
			// mBarCodeReader.setParameter(BarCodeReader.ParamNum.PICKLIST_MODE,
			// 2);
			mBarCodeReader.setDecodeCallback(this);
			mBarCodeReader.setErrorCallback(this);
//			mBarCodeReader.setParameter(765,0); 
		// urovo add shenpidong begin 2019-04-12
		// Martrix 2 of 5 begin
                mBarCodeReader.setParameter(Se4750ParamIndex.M25_LENGTH1, 2);
                mBarCodeReader.setParameter(Se4750ParamIndex.M25_LENGTH2, 50);
		int m25_enable = mScanService.getPropertyInt(PropertyID.M25_ENABLE);
		if(m25_enable >=0 && m25_enable <2) {
			mBarCodeReader.setParameter(Se4750ParamIndex.M25_ENABLE , m25_enable);
		}
		// Martrix 2 of 5 end
		// urovo add shenpidong end 2019-04-12
			mBarCodeReader.setParameter(136,50);
//			mBarCodeReader.setParameter(900,0); // multi-decode
		// urovo add shenpidong begin 2019-02-25 , default open ISP
			int ret = mBarCodeReader.setParameter(8610,1);
		// urovo add shenpidong end 2019-02-25
			Log.d(TAG , "openDecoder , ret 8610:" + ret);
			ret = mBarCodeReader.setParameter(8611,1);
			Log.d(TAG , "openDecoder , ret:" + ret);		
			ret = mBarCodeReader.setParameter(1881,1);
			ret = mBarCodeReader.setParameter(1882,1);
			// urovo add shenpidong begin 2019-08-14
//			ret = mBarCodeReader.setParameter(1438, 1); // support 4750 dpm , other not support (4750SR/4750MR)
			// urovo add shenpidong end 2019-08-14
			// urovo add shenpidong begin 2019-04-12
			// urovo add shenpidong begin 2019-04-18
            /*if (mScanService.getPropertyInt(PropertyID.LABEL_SEPARATOR_ENABLE) == 1) {
                ret = mBarCodeReader.setParameter(Se4750ParamIndex.TRANSMIT_CODE_ID, 1);
            }*/
            updateTransmitCodeID();
			// urovo add shenpidong end 2019-04-18
			// ITF code begin
			int i25_enable = mScanService.getPropertyInt(PropertyID.I25_ENABLE);
			updateInterleaved2Of5(i25_enable);
			// ITF code end
			// Code93 begin
/*
			Log.d(TAG , "openDecoder , 23 ret:" + ret);		
			ret = mBarCodeReader.setParameter(26,4);
			Log.d(TAG , "openDecoder , 26 ret:" + ret);		
			ret = mBarCodeReader.setParameter(27,55);
			Log.d(TAG , "openDecoder , 27 ret:" + ret);		
*/
			// Code93 end
			// urovo add shenpidong begin 2019-04-12
			ret = mBarCodeReader.setParameter(Se4750ParamIndex.D25_LENGTH1,2);
			ret = mBarCodeReader.setParameter(Se4750ParamIndex.D25_LENGTH2,50);
			int d25_enable = mScanService.getPropertyInt(PropertyID.D25_ENABLE);
			if(d25_enable >= 0 && d25_enable < 2) {
			    mBarCodeReader.setParameter(Se4750ParamIndex.D25_ENABLE , d25_enable);
			}
			int upce1_enable = mScanService.getPropertyInt(PropertyID.UPCE1_ENABLE);
			if(upce1_enable >= 0 && upce1_enable < 2) {
			    mBarCodeReader.setParameter(Se4750ParamIndex.UPCE1_ENABLE , upce1_enable);
			}
			mBarCodeReader.setParameter(Se4750ParamIndex.DEC_ILLUM_POWER_LEVEL,3);
			// urovo add shenpidong begin 2019-05-30
			int code39_enable = mScanService.getPropertyInt(PropertyID.CODE39_ENABLE);
			if(code39_enable > 0) {
//			    ret = mBarCodeReader.getNumParameter(750);
			    ret = mScanService.getPropertyInt(PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL);
			    Log.i(TAG, "openDecoder , 750 ret:" + ret);
			    if(ret >0 && ret < 5) {
				ret = ret - 1;
			    } else {
				ret = 0;
			    }
			    ret = mBarCodeReader.setParameter(750, ret);
			    Log.i(TAG, "openDecoder ,end 750 ret:" + ret);
			}
			// urovo add shenpidong end 2019-05-30
			// urovo add shenpidong begin 2019-06-13
			int datamatrix_enable = mScanService.getPropertyInt(PropertyID.DATAMATRIX_ENABLE);
			Log.i(TAG, "openDecoder , datamatrix_enable:" + datamatrix_enable);
			if(datamatrix_enable > 0) {
			    // 0 - Never - do not decode Data Matrix bar codes that are mirror images
			    // 1 - Always - decode only Data Matrix bar codes that are mirror images
			    // 2 - Auto - decode both mirrored and unmirrored Data Matrix bar codes.
			    ret = mBarCodeReader.getNumParameter(537);
			    if(ret != 2) {
				ret = 2;
			    }
			    ret = mBarCodeReader.setParameter(537, ret);
			}
            if(enableLastDecImage) {
                setLastDecImage();
            }
			// urovo add shenpidong end 2019-06-13
			mEnabled = true;
		} else {
			mEnabled = false;
		}
		Log.i(TAG, "openCoder end , mEnabled:" + mEnabled);
	}

	public synchronized void closeDecoder() {
		Log.i(TAG, "closeCoder , enabled:" + mEnabled + ",BarCodeReader:" + (mBarCodeReader!=null) + ",state:" + state);
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
	    Log.i(TAG, "closeDecoder ...... Exception");
		  mBarCodeReader = null;
                }
			mBarCodeReader = null;
		}
		state = STATE_IDLE;
	}

    private void restartScanService() {
//		mScanState = isContinueScan;
//		isContinueScan = false;
//		decodeNotified = false;
		try {
		    Thread.sleep(500);
		}catch(InterruptedException e1) {
		    e1.printStackTrace();
		} 
		close();
		try {
		    Thread.sleep(200);				
		}catch(InterruptedException e1) {
		    e1.printStackTrace();
		} 
		open();
		// urovo add shenpidong begin 2019-04-12
		mScanService.updateProperties();
		// urovo add shenpidong end 2019-04-12
		isContinueScan = mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == 4;
		Log.e(TAG, "restartScanService , restart service:" + mScanState + ",isContinueScan:" + isContinueScan);
		if(isContinueScan && mScanState) {
		    startCoder();
		} else {
		    isContinueScan = false;
		}
//		if(mScanState) {
//		    startCoder();
//		}
    }

    public void onError(int error, BarCodeReader reader) {
		// TODO Auto-generated method stub
		Log.e(TAG, "error " + error);
	    if(error == 100) {
		mScanStateError = true;
		decodeFeedback();
		restartScanService();
	    }
	}
    @Override
    public void onDecodeComplete(int symbology, int length, byte[] data, BarCodeReader reader) {
        // TODO Auto-generated method stub
        if (state == STATE_DECODE) {
            state = STATE_IDLE;
        }
/*	int status = -1;
	String decodeStatString = null;
	if(mBarCodeReader != null) {
	    status = mBarCodeReader.getNumProperty(BarCodeReader.PropertyNum.ENGINE_STATUS);
	    decodeStatString = ("[Decoding] Engine Status 0x" + Integer.toHexString(status));
	}
        Log.d(TAG,"on decode complete , len:" + length + ",data:" + data + ",symbology:" + symbology + ",status:" + status + ",decodeStatString:" + decodeStatString);
*/
	if(length > 0 && data != null) {
	    long endTime = System.currentTimeMillis();
	    Log.d(TAG , "onDecodeComplete time:" + (endTime - startTime));
	}
       
        switch (length) {
            case BarCodeReader.DECODE_STATUS_TIMEOUT:

				state = STATE_IDLE; //no need stop again
		decodeFeedback();
                break;
            case BarCodeReader.DECODE_STATUS_ERROR:
				state = STATE_IDLE; //no need stop again
		decodeFeedback();
                break;
            case BarCodeReader.DECODE_STATUS_CANCELED:
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
                    Log.d(TAG , "onDecodeComplete symbology = " + symbology);

                    barcodeType = symbology;
                    Log.d(TAG , "onDecodeComplete barcodeType = " + barcodeType);
		    // urovo add shenpidong begin 2019-04-18
		    mayBeseparatorDecode(barcodeType , temp , length);
//                    sendBroadcast(temp, barcodeType, length);
					state = STATE_IDLE; //no need stop again
		    // urovo add shenpidong end 2019-04-18
		    decodeFeedback();
                }
                break;
        }

        // state = STATE_IDLE; //no need stop again
		if (enableLastDecImage && !isContinueScan && length != -3 && mBarCodeReader != null) {
            lastImageData = mBarCodeReader.getLastDecImage();
        }
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
    // urovo add shenpidong end 2019-04-18

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
		 if (null != mBarCodeReader) {
			 for (int i = 0; i < size; i++) {
				 int keyForIndex = property.keyAt(i);
				 int internalIndex = mPropIndexHashMap.get(keyForIndex, SPECIAL_VALUE);
//		Log.d(TAG, "setProperties keyForIndex:" + keyForIndex + ",internalIndex:" + internalIndex + ",SPECIAL_VALUE:" + SPECIAL_VALUE);
				 if (internalIndex != SPECIAL_VALUE) {
					 int value = property.get(keyForIndex);
//			Log.d(TAG, "setProperties ======== value:" + value);
                    switch (keyForIndex) {
                        case PropertyID.LINEAR_1D_QUIET_ZONE_LEVEL:
                            if(value < 0 || value > 3) {
                                value = 1;
                            }
                            mBarCodeReader.setParameter(1288, value);
                            break;
                        case PropertyID.CODE39_Quiet_Zone:
                            if(value < 0 || value > 3) {
                                value = 1;
                            }
                            mBarCodeReader.setParameter(750, value);
                            break;
                        case PropertyID.CODE39_SECURITY_LEVEL:
                            mBarCodeReader.setParameter(1209, value);
                            break;
                        case PropertyID.M25_SEND_CHECK:
                            mBarCodeReader.setParameter(622, value != 1 ? 0 : 1);
                            break;
                        case PropertyID.M25_LENGTH1:
                            mBarCodeReader.setParameter(619, value > 0 ? value : 1);
                            break;
                        case PropertyID.M25_LENGTH2:
                            mBarCodeReader.setParameter(620, value > 0 ? value : 55);
                            break;
                        case PropertyID.CODE128_CHECK_ISBT_TABLE:
                            mBarCodeReader.setParameter(578, value != 0 ? 1 : 0);
                            break;
                        case PropertyID.CODE_ISBT_Concatenation_MODE:
                            if(value < 0 || value > 2) {
                                value = 0;
                            }
                            mBarCodeReader.setParameter(577, value);
                            break;
                        case PropertyID.CODE128_SECURITY_LEVEL:
                            if(value < 0 || value > 3) {
                                value = 1;
                            }
                            mBarCodeReader.setParameter(77, value);//include the Code 128 family,UPC/EAN, and Code 93
                            break;
                        case PropertyID.CODE128_IGNORE_FNC4:
                            mBarCodeReader.setParameter(1254, value != 1 ? 0 : 1);
                            break;
                        case PropertyID.UCC_REDUCED_QUIET_ZONE:
                            mBarCodeReader.setParameter(1289, value != 1 ? 0 : 1);
                            break;
                        case PropertyID.UCC_COUPON_EXT_CODE:
                            mBarCodeReader.setParameter(85, value != 1 ? 0 : 1);
                            break;
                        case PropertyID.UCC_COUPON_EXT_REPORT_MODE:
                            if(value < 0 || value > 2) {
                                value = 0;
                            }
                            mBarCodeReader.setParameter(730, value);
                            break;
                        case PropertyID.UCC_EAN_ZERO_EXTEND:
                            //EAN-8/JAN-8 Extend
                            mBarCodeReader.setParameter(39, value != 1 ? 0 : 1);
                            break;
                        case PropertyID.UCC_EAN_SUPPLEMENTAL_MODE:
                            //mBarCodeReader.setParameter(16, value);
                            break;
                        case PropertyID.GS1_LIMIT_Security_Level:
                            if(value < 0 || value > 3) {
                                value = 3;
                            }
                            mBarCodeReader.setParameter(728, value);
                            break;
                        case PropertyID.COMPOSITE_UPC_MODE:
                            if(value < 0 || value > 2) {
                                value = 0;
                            }
                            mBarCodeReader.setParameter(344, value);
                            break;
                        case PropertyID.EAN13_BOOKLANDEAN:
                            mBarCodeReader.setParameter(83, value != 1 ? 0 : 1);
                            break;
                        case PropertyID.EAN13_BOOKLAND_FORMAT:
                            if(value < 0 || value > 1) {
                                value = 0;
                            }
                            mBarCodeReader.setParameter(576, value);
                            break;
                        case PropertyID.US_POSTAL_SEND_CHECK:
                            mBarCodeReader.setParameter(95, value != 1 ? 0 : 1);
                            mBarCodeReader.setParameter(96, value != 1 ? 0 : 1);
                            break;
                        case PropertyID.Canadian_POSTAL_ENABLE:
                            break;
                        case PropertyID.KOREA_POST_ENABLE:
                            mBarCodeReader.setParameter(581, value != 1 ? 0 : 1);
                            break;
                        case PropertyID.MSI_CHECK_2_MOD_11:
                        case PropertyID.MSI_REQUIRE_2_CHECK:
                        case PropertyID.MSI_SEND_CHECK: {
                                /*4710 0=//MOD 10/MOD 11  1=//MOD 10/MOD 10  (0,1) = one check digit 2= two check digit
                                default
                                Transmit MSI Check Digit:0(0,1)
                                MSI Check Digits:2 (0,1,2)
                                MSI Check Digit Algorithm:1(0,1)
                                //1 0 0            1 0 0
                                //1 1 1            1 2 1
                                //1 1 0            1 2 0
                                //1 0 1            1 0 1
                                //0 0 0            0 1 0
                                //0 1 0            0 2 0
                                //0 0 1            0 1 1
                                //0 1 1            0 2 1
                                */
                                if(mBarCodeReader != null) {
                                    int ckDigitAlg = mScanService.getPropertyInt(PropertyID.MSI_CHECK_2_MOD_11);
                                    int ckDigitMode = mScanService.getPropertyInt(PropertyID.MSI_REQUIRE_2_CHECK);
                                    int sendCK = mScanService.getPropertyInt(PropertyID.MSI_SEND_CHECK);
                                    mBarCodeReader.setParameter(Se4750ParamIndex.MSI_SEND_CHECK, sendCK);
                                    //two check;//MOD 10/MOD 11//MOD 10/MOD 10
                                    mBarCodeReader.setParameter(Se4750ParamIndex.MSI_CHECK_2_MOD_11, ckDigitAlg);
                                    mBarCodeReader.setParameter(Se4750ParamIndex.MSI_REQUIRE_2_CHECK, ckDigitMode);
                                    if(ckDigitMode == 1) {
                                        //two check;
                                        ckDigitMode = 2;
                                    } else {
                                        if(sendCK == 0) {
                                            ckDigitMode = 1;
                                        }
                                    }
                                    mBarCodeReader.setParameter(Se4750ParamIndex.MSI_REQUIRE_2_CHECK, ckDigitMode);
                                }
                            }
                            break;
                        case PropertyID.EAN_EXT_ENABLE_2_5_DIGIT:
                            if (value == 1) {
                                value = 0x02;
                            }
                            mBarCodeReader.setParameter(internalIndex, value);
                            break;
                        case PropertyID.CODE32_ENABLE:
                        case PropertyID.CODE32_SEND_START:
                            Log.d(TAG, "setProperties ======== value:" + value);
                            mBarCodeReader.setParameter(BarCodeReader.ParamNum.CODE32, mScanService.getPropertyInt(PropertyID.CODE32_ENABLE));
                            if(PropertyID.CODE32_SEND_START ==keyForIndex && value == 1) {
                                mBarCodeReader.setParameter(BarCodeReader.ParamNum.CODE32, 1);
                            }
                            mBarCodeReader.setParameter(BarCodeReader.ParamNum.C32_PREFIX, value);
                            break;
                        case PropertyID.COMPOSITE_CC_AB_ENABLE:
                            mBarCodeReader.setParameter(Se4750ParamIndex.COMPOSITE_CC_AB_ENABLE, value);
                            if (value == 1) {
                                if (mScanService.getPropertyInt(PropertyID.GS1_14_ENABLE) == 0) {
                                    mBarCodeReader.setParameter(Se4750ParamIndex.GS1_14_ENABLE, 1);
                                }
                                mBarCodeReader.setParameter(BarCodeReader.ParamNum.UPC_COMPOSITE, 2);
                            }
                            break;
                        case PropertyID.COMPOSITE_TLC39_ENABLE:
                            mBarCodeReader.setParameter(Se4750ParamIndex.COMPOSITE_TLC39_ENABLE, value);
                            break;
                        case PropertyID.COMPOSITE_CC_C_ENABLE:
                            mBarCodeReader.setParameter(Se4750ParamIndex.COMPOSITE_CC_C_ENABLE, value);
                            if (value == 1) {
                                mBarCodeReader.setParameter(BarCodeReader.ParamNum.UPC_COMPOSITE, 2);
                            }
                            break;
                        case PropertyID.DEC_PICKLIST_AIM_MODE: {
                            // urovo modify shenpidong begin 2019-08-13
                            if (value == 1) {
                                value = 2;
                            }
                            mBarCodeReader.setParameter(Se4750ParamIndex.DEC_PICKLIST_AIM_MODE, value);
                            // urovo modify shenpidong end 2019-08-13
                        }
                        break;
                        case PropertyID.DEC_2D_LIGHTS_MODE: {
                            value = mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE);
                            if (value == 0) {
                                mBarCodeReader.setParameter(Se4750ParamIndex.DEC_ILLUM_MODE, 0);
                                mBarCodeReader.setParameter(Se4750ParamIndex.DEC_AIM_PATTERN, 0);
                            } else if (value == 1) {
                                mBarCodeReader.setParameter(Se4750ParamIndex.DEC_ILLUM_MODE, 0);
                                mBarCodeReader.setParameter(Se4750ParamIndex.DEC_AIM_PATTERN, 1);
                            } else if (value == 2) {
                                mBarCodeReader.setParameter(Se4750ParamIndex.DEC_ILLUM_MODE, 1);
                                mBarCodeReader.setParameter(Se4750ParamIndex.DEC_AIM_PATTERN, 0);
                            } else if (value == 3) {
                                mBarCodeReader.setParameter(Se4750ParamIndex.DEC_ILLUM_MODE, 1);
                                mBarCodeReader.setParameter(Se4750ParamIndex.DEC_AIM_PATTERN, 1);
                            } else if (value == 4) {
                                mBarCodeReader.setParameter(Se4750ParamIndex.DEC_ILLUM_MODE, 1);
                                mBarCodeReader.setParameter(Se4750ParamIndex.DEC_AIM_PATTERN, 1);
                            }
                        }
                        break;
                        // urovo add shenpidong begin 2019-04-12
                        case PropertyID.I25_ENABLE:
                            updateInterleaved2Of5(value);
                            break;
                        case PropertyID.I25_QUIET_ZONE:
                            mBarCodeReader.setParameter(1120, value != 1 ? 0 : 1);
                            break;
                        case PropertyID.I25_SECURITY_LEVEL:
                            if(value < 0 || value > 3) {
                                value = 1;
                            }
                            mBarCodeReader.setParameter(1121, value);
                            break;
                        case PropertyID.CODE93_ENABLE:
                            updateCode93(value);
                            break;
                        case PropertyID.DOTCODE_ENABLE:
                            mBarCodeReader.setParameter(Se4750ParamIndex.DOTCODE_ENABLE, value);
                            if(value == 1) {
                                mBarCodeReader.setParameter(1907, 2);//DotCode Inverse
                            }
                            break;
                        // urovo add shenpidong end 2019-04-12
                        // urovo add shenpidong begin 2019-04-18
                        case PropertyID.LABEL_SEPARATOR_ENABLE:
                        case PropertyID.TRANSMIT_CODE_ID:
                            updateTransmitCodeID();
                            break;
                        // urovo add shenpidong end 2019-04-18
                        // urovo add shenpidong begin 2019-05-22
                        case PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL:
                            if (internalIndex > 0 && value > 0 && value < 5) {
                                mBarCodeReader.setParameter(internalIndex, value);
                                // urovo add shenpidong begin 2019-05-22 , CODE39 Security Level value is 0 1 2 3 , default 0.
                                int ret = mBarCodeReader.setParameter(750, value > 0 ? (value - 1) : value);
                                // urovo add shenpidong end 2019-05-22 , CODE39 Security Level value is 0 1 2 3 , default 0.
                                Log.d(TAG, "setProperties internalIndex:" + internalIndex + "   value == " + value + ",ret:" + ret);
                            } else {
                                Log.d(TAG, "setProperties internalIndex:" + internalIndex + "   value == " + value);
                            }
                            // urovo add shenpidong end 2019-05-22
                            break;
                        // urovo add shenpidong begin 2019-09-02
                        case PropertyID.DEC_OCR_MODE:
                        case PropertyID.DEC_OCR_TEMPLATE:
                        case PropertyID.DEC_OCR_USER_TEMPLATE: {
                            int ocr_mode = mScanService.getPropertyInt(PropertyID.DEC_OCR_MODE);
                            Log.d(TAG, "setProperties OCR_MODE internalIndex = " + internalIndex + "   value == " + value + ",ocr_mode:" + ocr_mode);
                            if (ocr_mode >= 0 && ocr_mode < 2) {
                                // ignore
                            } else {
                                ocr_mode = 0;
                            }
                            int ret = mBarCodeReader.setParameter(Se4750ParamIndex.DEC_OCR_MODE, ocr_mode);
                            String OCRTemplate = "999999999999999993";
//				String OCRTemplate = "91R";
                            if (ocr_mode != 1) {
                                OCRTemplate = "54R";
                            }
                            ret = mBarCodeReader.setParameter(547, OCRTemplate);
                            Log.d(TAG, "setProperties property ret:" + ret);
//				mBarCodeReader.setParameter(685, 2);
                            String OCRSubSetString = "0123456789X";
                            mBarCodeReader.setParameter(686, OCRSubSetString);
                            ret = mBarCodeReader.setParameter(691, 1);
                            ret = mBarCodeReader.setParameter(695, 20);
                            ret = mBarCodeReader.setParameter(687, 4);
                            ret = mBarCodeReader.setParameter(690, 18);
                            Log.d(TAG, "setProperties2 property ret:" + ret);
                        }
                        break;
                        // urovo add shenpidong end 2019-09-02
                        case PropertyID.UPCA_SEND_SYS:
                        case PropertyID.UPCA_TO_EAN13:
                            int upcasys = mScanService.getPropertyInt(PropertyID.UPCA_SEND_SYS);
                            int upcato13 = mScanService.getPropertyInt(PropertyID.UPCA_TO_EAN13);
                            if(upcasys == 1 && upcato13 == 1) {
                                mBarCodeReader.setParameter(Se4750ParamIndex.UPCA_SEND_SYS, 2);
                            } else if(upcasys == 1 && upcato13 != 1){
                                mBarCodeReader.setParameter(Se4750ParamIndex.UPCA_SEND_SYS, upcasys);
                            } else if(upcasys == 0 && upcato13 == 1){
                                mBarCodeReader.setParameter(Se4750ParamIndex.UPCA_SEND_SYS, 2);
                            } else {
                                mBarCodeReader.setParameter(Se4750ParamIndex.UPCA_SEND_SYS, upcasys);
                            }
                            break;
                        case PropertyID.DPM_DECODE_MODE:
                            if(value < 0 || value > 3) {
                                value = 0;
                            }
                            mBarCodeReader.setParameter(1438, value);
                        break;
                        case PropertyID.QRCODE_ENABLE:
                            mBarCodeReader.setParameter(internalIndex, value);
                            if(value == 1) {
                                /*• *0 - Linked QR Only: The scanner does not decode individual QR symbols from a set of Linked QR codes.
                                • 1 - Individual QR With Headers: The scanner decodes individual QR symbols from a set of Linked QR
                                codes and retains the header information and data.
                                • 2 - Individual QR No Headers: The scanner decodes individual QR symbols from a set of Linked QR
                                codes and transmits the data without header information*/
                                mBarCodeReader.setParameter(1847, 2);
                            }
                        break;
						 default:
							 //Log.d(TAG, "setProperties property internalIndex = " + internalIndex + "   value == " + value);
							 if (internalIndex >= 0) {
								 mBarCodeReader.setParameter(internalIndex, value);
							 } else {
								 Log.d(TAG, "setProperties property ignore internalIndex = " + internalIndex + "   value == " + value);
							 }
							 break;
					 }
                } else {
                    switch (keyForIndex) {
                        case PropertyID.LABEL_SEPARATOR_ENABLE:
                        case PropertyID.TRANSMIT_CODE_ID:
                            updateTransmitCodeID();
                        case PropertyID.EAN8_SEND_CHECK:
                            if(mBarCodeReader != null) {
                                int ean8xxtr = property.get(keyForIndex);
                                mBarCodeReader.setParameter(1881, ean8xxtr);
                            }
                        break;
                        case PropertyID.EAN13_SEND_CHECK:
                            if(mBarCodeReader != null) {
                                int ean13xxtr = property.get(keyForIndex);
                                mBarCodeReader.setParameter(1882, ean13xxtr);
                            }
                        break;
                    }
                }
            }
        }
		 return 0;
	 }

    // urovo add shenpidong begin 2019-04-18
    private void updateTransmitCodeID() {
        boolean SEPARATOR_ENABLE = mScanService.getPropertyInt(PropertyID.LABEL_SEPARATOR_ENABLE) == 1;
        int TransmitCodeID = mScanService.getPropertyInt(PropertyID.TRANSMIT_CODE_ID);
        if(SEPARATOR_ENABLE) {
            int ret = mBarCodeReader.setParameter(Se4750ParamIndex.TRANSMIT_CODE_ID, TransmitCodeID == 2 ? 2: 1);
        } else {
            int ret = mBarCodeReader.setParameter(Se4750ParamIndex.TRANSMIT_CODE_ID, TransmitCodeID);
        }
    }
    // urovo add shenpidong end 2019-04-18

    // urovo add shenpidong begin 2019-04-12
    private void updateCode93(int value) {
	if(value ==0 || value == 1) {
	    int ret = mBarCodeReader.setParameter(Se4750ParamIndex.CODE93_ENABLE ,value);
	    Log.d(TAG , "setProperties , 26 ret:" + ret + ",value:" + value);
	    if(value == 1) {
                int min = mScanService.getPropertyInt(PropertyID.CODE93_LENGTH1);
                int max = mScanService.getPropertyInt(PropertyID.CODE93_LENGTH2);
		ret = mBarCodeReader.setParameter(Se4750ParamIndex.CODE93_LENGTH1, (min >= 0 && min <=55)?min:2);
		ret = mBarCodeReader.setParameter(Se4750ParamIndex.CODE93_LENGTH2,(max >= 0 && max <=55)?max:20);
	    }
	}
    }

    private void updateInterleaved2Of5(int value) {
	if(value ==0 || value == 1) {
	    int ret = mBarCodeReader.setParameter(Se4750ParamIndex.I25_ENABLE ,value);
	    Log.d(TAG , "setProperties , 22 ret:" + ret + ",value:" + value);
	    if(value == 1) {
		int min = mScanService.getPropertyInt(PropertyID.I25_LENGTH1);
		int max = mScanService.getPropertyInt(PropertyID.I25_LENGTH2);
		ret = mBarCodeReader.setParameter(Se4750ParamIndex.I25_LENGTH1,(min >= 0 && min <=55)?min:2);
		ret = mBarCodeReader.setParameter(Se4750ParamIndex.I25_LENGTH2,(max >= 0 && max <=55)?max:50);
	    }
	}
    }
    // urovo add shenpidong end 2019-04-12

	@Override
	public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {
		// TODO Auto-generated method stub
		
	}
    public void onPictureTaken(int format, int width, int height, byte[] abData, BarCodeReader reader)
    {
        // Render it on the snapshot image
        Log.i(TAG, "onPictureTaken of res" + width + " w" + height + " h");
        // display snapshot
        if(abData != null) {
            captureImage(abData);
        }
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
//			mBarCodeReader.setParameter(765, 0); // For QC/MTK platforms
			mBarCodeReader.setParameter(136,50);
		// urovo add shenpidong begin 2019-02-25 , default open ISP
			int ret = mBarCodeReader.setParameter(8610,1);
		// urovo add shenpidong end 2019-02-25
			Log.d(TAG , "setDefaults , ret 8610:" + ret);
			ret = mBarCodeReader.setParameter(8611,1);
			ret = mBarCodeReader.setParameter(1881,1);
			ret = mBarCodeReader.setParameter(1882,1);
			// urovo add shenpidong begin 2019-04-18
            updateTransmitCodeID();
            // urovo add shenpidong end 2019-04-18
            int upce1_enable = mScanService.getPropertyInt(PropertyID.UPCE1_ENABLE);
            if (upce1_enable >= 0 && upce1_enable < 2) {
                // UPCE1 default value is enable=1
            } else {
                upce1_enable = 1;
            }
            mBarCodeReader.setParameter(Se4750ParamIndex.UPCE1_ENABLE, upce1_enable);
            mBarCodeReader.setParameter(Se4750ParamIndex.DEC_ILLUM_POWER_LEVEL, 3);
            // urovo add shenpidong begin 2019-05-22 , CODE39 Security Level value is 0 1 2 3 , default 0.
            ret = mBarCodeReader.setParameter(750, 0);
            // urovo add shenpidong end 2019-05-22 , CODE39 Security Level value is 0 1 2 3 , default 0.
            // urovo add shenpidong begin 2019-06-13
            int datamatrix_enable = mScanService.getPropertyInt(PropertyID.DATAMATRIX_ENABLE);
            Log.i(TAG, "setDefaults , datamatrix_enable:" + datamatrix_enable);
            if (datamatrix_enable > 0) {
                // 0 - Never - do not decode Data Matrix bar codes that are mirror images
                // 1 - Always - decode only Data Matrix bar codes that are mirror images
                // 2 - Auto - decode both mirrored and unmirrored Data Matrix bar codes.
                ret = mBarCodeReader.setParameter(537, 2);
            }
            // urovo add shenpidong end 2019-06-13
            Log.d(TAG, "setDefaults , ret:" + ret);
            // urovo add shenpidong begin 2019-09-03
            int upcasys = mScanService.getPropertyInt(PropertyID.UPCA_SEND_SYS);
			mBarCodeReader.setParameter(Se4750ParamIndex.UPCA_SEND_SYS, upcasys);
            mScanService.updateProperties();
            //4750 DPM mode
            mBarCodeReader.setParameter(1438, 0);
/*
	    // urovo modify shenpidong begin 2019-11-28
	    ///////////////////////////////// OCR start test //////////////////////////////////
	    // Enable OCR-B 
	    ret = mBarCodeReader.setParameter(681, 1);

	    // Set OCR templates
	    String OCRSubSetString = "0123456789X";//""01234567890"; // Only numeric characters
	    // Parameter # 686 - OCR Subset
	    ret = mBarCodeReader.setParameter(686, OCRSubSetString);

	    //String OCRTemplate = "54R"; // The D ignores all characters after the template
	    String OCRTemplate = "S999999999999999993D"; // The D ignores all characters after the template
	    // Parameter # 547 - OCR Template
	    ret = mBarCodeReader.setParameter(547, OCRTemplate);
	    // Parameter # 689 - OCR Minimum characters
	    ret = mBarCodeReader.setParameter(689, 15);
	    // Parameter # 690 - OCR Maximum characters
	    ret = mBarCodeReader.setParameter(690, 20);

	    // Set Orientation
	    ret = mBarCodeReader.setParameter(687, 0);
	    //ret = mBarCodeReader.setParameter(687, 4);
	    ret = mBarCodeReader.setParameter(136, 99);
	    ret = mBarCodeReader.setParameter(764, 3);
	    ret = mBarCodeReader.setParameter(-2, 1);
*/
	    ///////////////////////////////// OCR end test //////////////////////////////////
	    // urovo modify shenpidong end 2019-11-28
            // urovo add shenpidong end 2019-09-03
        }
	}

    @Override
    public boolean lockHwTriggler(boolean lock) {
        // TODO Auto-generated method stub
        return false;
    }

    private final int[] defParamIndex = new int[]{
            Se4750ParamIndex.CODE39_LENGTH1,
            Se4750ParamIndex.CODE39_LENGTH2,
            Se4750ParamIndex.D25_LENGTH1,
            Se4750ParamIndex.D25_LENGTH2,
            Se4750ParamIndex.M25_LENGTH1,
            Se4750ParamIndex.M25_LENGTH2,
            Se4750ParamIndex.I25_LENGTH1,
            Se4750ParamIndex.I25_LENGTH2,
            Se4750ParamIndex.CODABAR_ENABLE,
            Se4750ParamIndex.CODABAR_LENGTH1,
            Se4750ParamIndex.CODABAR_LENGTH2,
            Se4750ParamIndex.CODE93_LENGTH1,
            Se4750ParamIndex.CODE93_LENGTH2,
            Se4750ParamIndex.CODE128_LENGTH1,
            Se4750ParamIndex.CODE128_LENGTH2,
            Se4750ParamIndex.UPCA_SEND_CHECK,
            Se4750ParamIndex.UPCE_SEND_CHECK,
            Se4750ParamIndex.UPCE_SEND_SYS,
            Se4750ParamIndex.MSI_LENGTH1,
            Se4750ParamIndex.MSI_LENGTH2,
            Se4750ParamIndex.GS1_LIMIT_ENABLE,
            Se4750ParamIndex.GS1_EXP_ENABLE,
            Se4750ParamIndex.USPS_4STATE_ENABLE,
            Se4750ParamIndex.AUSTRALIAN_POST_ENABLE,
            Se4750ParamIndex.ROYAL_MAIL_SEND_CHECK ,
            Se4750ParamIndex.KIX_CODE_ENABLE,
            Se4750ParamIndex.JAPANESE_POST_ENABLE,
            Se4750ParamIndex.PDF417_ENABLE,
	        Se4750ParamIndex.EAN_EXT_ENABLE_2_5_DIGIT,
	        Se4750ParamIndex.DEC_ILLUM_POWER_LEVEL
    };
    
    private final int[] defParamVal = new int[] {
            1, //Se4750ParamIndex.CODE39_LENGTH1,
            20, //Se4750ParamIndex.CODE39_LENGTH2,
            2, //Se4750ParamIndex.D25_LENGTH1,
            50, //Se4750ParamIndex.D25_LENGTH2,
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
            0,
            0,
	    3
    };
    static class Se4750ParamIndex {
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
        public static final int CODE32_SEND_START = 231;//231;//TODO 2d 1d 0 1 0 adding the prefix character "A" to all Code 32 bar
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
        public static final int GS1_14_TO_UPC_EAN = 397;
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
        public static final int DOTCODE_ENABLE = 1906;
        public static final int DEC_OCR_MODE = 681;  // Fixed Frame Rate
        public static final int DEC_OCR_TEMPLATE = 547;  // Fixed Frame Rate
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
	        Se4750ParamIndex.IMAGE_EXPOSURE_MODE,
	        Se4750ParamIndex.IMAGE_FIXED_EXPOSURE,
            Se4750ParamIndex.IMAGE_PICKLIST_MODE,
            Se4750ParamIndex.IMAGE_ONE_D_INVERSE,
            Se4750ParamIndex.LASER_ON_TIME,
            Se4750ParamIndex.TIMEOUT_BETWEEN_SAME_SYMBOL,
            Se4750ParamIndex.LINEAR_CODE_TYPE_SECURITY_LEVEL,
            Se4750ParamIndex.FUZZY_1D_PROCESSING,
            Se4750ParamIndex.MULTI_DECODE_MODE,
            Se4750ParamIndex.BAR_CODES_TO_READ,
            Se4750ParamIndex.FULL_READ_MODE,
            Se4750ParamIndex.CODE39_ENABLE,
            Se4750ParamIndex.CODE39_ENABLE_CHECK,
            Se4750ParamIndex.CODE39_SEND_CHECK,
            Se4750ParamIndex.CODE39_FULL_ASCII,
            Se4750ParamIndex.CODE39_LENGTH1,
            Se4750ParamIndex.CODE39_LENGTH2,
            Se4750ParamIndex.TRIOPTIC_ENABLE,
            Se4750ParamIndex.CODE32_ENABLE,
            Se4750ParamIndex.CODE32_SEND_START,
            Se4750ParamIndex.C25_ENABLE,
            Se4750ParamIndex.D25_ENABLE, 
            Se4750ParamIndex.D25_LENGTH1,
            Se4750ParamIndex.D25_LENGTH2,
            Se4750ParamIndex.M25_ENABLE,
            Se4750ParamIndex.CODE11_ENABLE,
            Se4750ParamIndex.CODE11_ENABLE_CHECK,
            Se4750ParamIndex.CODE11_SEND_CHECK,
            Se4750ParamIndex.CODE11_LENGTH1,
            Se4750ParamIndex.CODE11_LENGTH2,
            Se4750ParamIndex.I25_ENABLE,
            Se4750ParamIndex.I25_ENABLE_CHECK,
            Se4750ParamIndex.I25_SEND_CHECK,
            Se4750ParamIndex.I25_LENGTH1,
            Se4750ParamIndex.I25_LENGTH2,
            Se4750ParamIndex.I25_TO_EAN13,
            Se4750ParamIndex.CODABAR_ENABLE,
            Se4750ParamIndex.CODABAR_NOTIS,
            Se4750ParamIndex.CODABAR_CLSI,
            Se4750ParamIndex.CODABAR_LENGTH1,
            Se4750ParamIndex.CODABAR_LENGTH2,
            Se4750ParamIndex.CODE93_ENABLE,
            Se4750ParamIndex.CODE93_LENGTH1,
            Se4750ParamIndex.CODE93_LENGTH2,
            Se4750ParamIndex.CODE128_ENABLE,
            Se4750ParamIndex.CODE128_LENGTH1,
            Se4750ParamIndex.CODE128_LENGTH2,
            Se4750ParamIndex.CODE_ISBT_128,
            Se4750ParamIndex.CODE128_GS1_ENABLE,
            Se4750ParamIndex.UPCA_ENABLE, 
            Se4750ParamIndex.UPCA_SEND_CHECK,
            Se4750ParamIndex.UPCA_SEND_SYS,
            Se4750ParamIndex.UPCA_TO_EAN13,
            Se4750ParamIndex.UPCE_ENABLE,
            Se4750ParamIndex.UPCE_SEND_CHECK,
            Se4750ParamIndex.UPCE_SEND_SYS,
            Se4750ParamIndex.UPCE_TO_UPCA,
            Se4750ParamIndex.UPCE1_ENABLE,
            Se4750ParamIndex.UPCE1_SEND_CHECK,
            Se4750ParamIndex.UPCE1_SEND_SYS,
            Se4750ParamIndex.UPCE1_TO_UPCA,
            Se4750ParamIndex.EAN13_ENABLE,
            Se4750ParamIndex.EAN13_BOOKLANDEAN,
            Se4750ParamIndex.EAN13_BOOKLAND_FORMAT,
            Se4750ParamIndex.EAN8_ENABLE,
            Se4750ParamIndex.EAN8_TO_EAN13,
            Se4750ParamIndex.EAN_EXT_ENABLE_2_5_DIGIT,
            Se4750ParamIndex.UPC_EAN_SECURITY_LEVEL,
            Se4750ParamIndex.UCC_COUPON_EXT_CODE,
            Se4750ParamIndex.MSI_ENABLE,
            Se4750ParamIndex.MSI_REQUIRE_2_CHECK,
            Se4750ParamIndex.MSI_SEND_CHECK,
            Se4750ParamIndex.MSI_CHECK_2_MOD_11,
            Se4750ParamIndex.MSI_LENGTH1,
            Se4750ParamIndex.MSI_LENGTH2,
            Se4750ParamIndex.GS1_14_ENABLE,
            Se4750ParamIndex.GS1_14_TO_UPC_EAN,
            Se4750ParamIndex.GS1_LIMIT_ENABLE,
            Se4750ParamIndex.GS1_EXP_ENABLE,
            Se4750ParamIndex.GS1_EXP_LENGTH1,
            Se4750ParamIndex.GS1_EXP_LENGTH2,
            Se4750ParamIndex.US_POSTNET_ENABLE,
            Se4750ParamIndex.US_PLANET_ENABLE,
            Se4750ParamIndex.US_POSTAL_SEND_CHECK,
            Se4750ParamIndex.USPS_4STATE_ENABLE,
            Se4750ParamIndex.UPU_FICS_ENABLE,
            Se4750ParamIndex.ROYAL_MAIL_ENABLE,
            Se4750ParamIndex.ROYAL_MAIL_SEND_CHECK,
            Se4750ParamIndex.AUSTRALIAN_POST_ENABLE,
            Se4750ParamIndex.KIX_CODE_ENABLE,
            Se4750ParamIndex.JAPANESE_POST_ENABLE,
            Se4750ParamIndex.PDF417_ENABLE,
            Se4750ParamIndex.MICROPDF417_ENABLE,
            Se4750ParamIndex.COMPOSITE_CC_AB_ENABLE,
            Se4750ParamIndex.COMPOSITE_CC_C_ENABLE,
            Se4750ParamIndex.COMPOSITE_TLC39_ENABLE,
            Se4750ParamIndex.HANXIN_ENABLE,
            Se4750ParamIndex.HANXIN_INVERSE,
            Se4750ParamIndex.DATAMATRIX_ENABLE,
            Se4750ParamIndex.DATAMATRIX_LENGTH1,
            Se4750ParamIndex.DATAMATRIX_LENGTH2,
            Se4750ParamIndex.DATAMATRIX_INVERSE,
            Se4750ParamIndex.MAXICODE_ENABLE,
            Se4750ParamIndex.QRCODE_ENABLE,
            Se4750ParamIndex.QRCODE_INVERSE,
            Se4750ParamIndex.MICROQRCODE_ENABLE,
            Se4750ParamIndex.AZTEC_ENABLE,
            Se4750ParamIndex.AZTEC_INVERSE,
            Se4750ParamIndex.DEC_2D_LIGHTS_MODE,
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
            Se4750ParamIndex.DEC_ILLUM_POWER_LEVEL,
            Se4750ParamIndex.DEC_PICKLIST_AIM_MODE,
            DEC_PICKLIST_AIM_DELAY,
            DEC_MaxMultiRead_COUNT,
            DEC_Multiple_Decode_TIMEOUT,
            DEC_Multiple_Decode_INTERVAL,
            DEC_Multiple_Decode_MODE,
            DEC_OCR_MODE,
            DEC_OCR_TEMPLATE,
            Se4750ParamIndex.TRANSMIT_CODE_ID,
            Se4750ParamIndex.DOTCODE_ENABLE,
			Se4750ParamIndex.LINEAR_1D_QUIET_ZONE_LEVEL,
			Se4750ParamIndex.CODE39_Quiet_Zone,
			CODE39_START_STOP,
			Se4750ParamIndex.CODE39_SECURITY_LEVEL,
			Se4750ParamIndex.M25_SEND_CHECK,
			Se4750ParamIndex.M25_LENGTH1,
			Se4750ParamIndex.M25_LENGTH2,
			Se4750ParamIndex.I25_QUIET_ZONE,
			Se4750ParamIndex.I25_SECURITY_LEVEL,
			CODABAR_ENABLE_CHECK,
			CODABAR_SEND_CHECK,
			CODABAR_SEND_START,
			CODABAR_CONCATENATE,
			Se4750ParamIndex.CODE128_REDUCED_QUIET_ZONE,
			Se4750ParamIndex.CODE128_CHECK_ISBT_TABLE,
			Se4750ParamIndex.CODE_ISBT_Concatenation_MODE,
			Se4750ParamIndex.CODE128_SECURITY_LEVEL,
			Se4750ParamIndex.CODE128_IGNORE_FNC4,
			Se4750ParamIndex.UCC_REDUCED_QUIET_ZONE,
			Se4750ParamIndex.UCC_COUPON_EXT_REPORT_MODE,
			Se4750ParamIndex.UCC_EAN_ZERO_EXTEND,
			Se4750ParamIndex.UCC_EAN_SUPPLEMENTAL_MODE,
			Se4750ParamIndex.GS1_LIMIT_Security_Level,
			Se4750ParamIndex.COMPOSITE_UPC_MODE,
			POSTAL_GROUP_TYPE_ENABLE,
			Se4750ParamIndex.KOREA_POST_ENABLE,
			Se4750ParamIndex.Canadian_POSTAL_ENABLE,
    };
}
