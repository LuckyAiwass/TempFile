package com.android.server.scanner;

import android.util.Log;
import android.util.SparseArray;
import android.device.scanner.configuration.PropertyID;
import android.os.Looper;
import android.os.Message;
import android.os.Handler;

import java.util.concurrent.CountDownLatch;

import com.android.server.ScanServiceWrapper;
import com.zebra.adc.decoder.BarCodeReader;
import com.zebra.adc.decoder.BarCodeReader.DecodeCallback;

public class Se4500Scanner extends Scanner implements DecodeCallback, BarCodeReader.ErrorCallback{
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
    private final CountDownLatch mInitializedLatch = new CountDownLatch(1);


	private static final String TAG = "Se4500Scanner";

	static {
		System.loadLibrary("IAL");
		System.loadLibrary("SDL");
		System.loadLibrary("barcodereader44");
	}
	public Se4500Scanner(ScanServiceWrapper scanService) {
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
	}

	public void setPropertyString(int index, String value) {
		// TODO Auto-generated method stub
	}

    public void setPropertyInt(int index, int value) {
        // TODO Auto-generated method stub
        if (mBarCodeReader == null)
            return;
        Integer realIndex = mPropIndexHashMap.get(index);
        if(realIndex != null && realIndex != SPECIAL_VALUE)
            mBarCodeReader.setParameter(realIndex, value);
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
                if(mBarCodeReader != null) {
                    int ret = mBarCodeReader.setParameter(BarCodeReader.ParamNum.PRIM_TRIG_MODE, BarCodeReader.ParamVal.LEVEL);
                    if (ret != BarCodeReader.BCR_ERROR) Log.i(TAG,"reset Level trigger mode faile!");
                    mBarCodeReader.stopDecode();
                }
            } else {
                mHandler.removeMessages(MESSAGE_CODE_ENABLE);
                Message m = Message.obtain(mHandler, MESSAGE_CODE_ENABLE);
                m.arg1 = 0;
                mHandler.sendMessage(m);
            }
            
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
	            }
	            Log.i(TAG,"stopDecode no exit");
	            }
	            state = STATE_IDLE;
	        }
		//}
		
	}

	public void startCoder() {
		Log.i(TAG,"startCoder");
		// TODO Auto-generated method stub
		if(mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == 4) {
		    if (mBarCodeReader != null) {
		        int ret = mBarCodeReader.startHandsFreeDecode(BarCodeReader.ParamVal.HANDSFREE);
		       // ret = mBarCodeReader.setParameter(BarCodeReader.ParamNum.PICKLIST_MODE, 2);
		        if (ret != BarCodeReader.BCR_SUCCESS); Log.i(TAG,"startHandsFreeDecode faile");
		    }
		} else {
		    if (state != STATE_IDLE) return;
	        if (mBarCodeReader!= null) {
	            state = STATE_DECODE;
	            try{
	            mBarCodeReader.startDecode(); // start decode (callback gets results)
	            }catch(Exception e){
	                e.printStackTrace();
	                Log.i(TAG,"startDecode no exit");
	            }
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
		Log.i(TAG, "openCoder");
		if (mEnabled)
			return;
		state = STATE_IDLE;
        try{
            mBarCodeReader = BarCodeReader.open(mScanService.mContext);
            }catch(Exception e){
            mBarCodeReader = null;
        }
		if (mBarCodeReader != null) {
			// mBarCodeReader.setParameter(BarCodeReader.ParamNum.PICKLIST_MODE,
			// 2);
			mBarCodeReader.setDecodeCallback(this);
			mBarCodeReader.setErrorCallback(this);
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
		state = STATE_IDLE;
	}
    public void onError(int error, BarCodeReader reader) {
		// TODO Auto-generated method stub
		Log.e(TAG, "error " + error);
	}
    @Override
    public void onDecodeComplete(int symbology, int length, byte[] data, BarCodeReader reader) {
        // TODO Auto-generated method stub
        /*if (state == STATE_DECODE) {
            state = STATE_IDLE;
        }*/
        if (mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) != 4)
            mBarCodeReader.stopDecode();
        switch (length) {
            case BarCodeReader.DECODE_STATUS_TIMEOUT:

                break;
            case BarCodeReader.DECODE_STATUS_ERROR:
                break;
            case BarCodeReader.DECODE_STATUS_CANCELED:
                break;

            default:
                if (length > 0) {
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
                    Log.d("se4500", "onDecodeComplete symbology = " + symbology);

                    barcodeType = symbology;
                    Log.d("se4500", "onDecodeComplete barcodeType = " + barcodeType);
                    sendBroadcast(temp, barcodeType, length);
                }
                break;
        }

            state = STATE_IDLE; //no need stop again
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
         Log.d("se4500", "setProperties property size= " + size);
         if(null != mBarCodeReader) {
             for(int i=0; i < size; i++) {
                 int keyForIndex = property.keyAt(i);
                 int internalIndex = mPropIndexHashMap.get(keyForIndex, SPECIAL_VALUE);
                 if(internalIndex != SPECIAL_VALUE) {
                     int value = property.get(keyForIndex);
                     switch(keyForIndex) {
                          case PropertyID.EAN_EXT_ENABLE_2_5_DIGIT:
                              if(value == 1 ) {
                                  value = 0x02;
                              }
                              break;
                          case PropertyID.COMPOSITE_CC_AB_ENABLE:
                              if(value == 1 ) {
                                  mBarCodeReader.setParameter(Se4500ParamIndex.COMPOSITE_CC_AB_ENABLE, value);
                                  if(mScanService.getPropertyInt(PropertyID.GS1_14_ENABLE) == 0) {
                                      mBarCodeReader.setParameter(Se4500ParamIndex.GS1_14_ENABLE, 1);
                                  }
                              }
                              break;
                          case PropertyID.COMPOSITE_TLC39_ENABLE:
                                  mBarCodeReader.setParameter(Se4500ParamIndex.COMPOSITE_TLC39_ENABLE, value);
                              break;
                          case PropertyID.COMPOSITE_CC_C_ENABLE:
                                  mBarCodeReader.setParameter(Se4500ParamIndex.COMPOSITE_CC_C_ENABLE, value);
                              break;
                          case PropertyID.DEC_PICKLIST_AIM_MODE: {
                              if(value == 1 ) {
                                  mBarCodeReader.setParameter(Se4500ParamIndex.DEC_PICKLIST_AIM_MODE, 2);
                              }
                            }
                          case PropertyID.DEC_2D_LIGHTS_MODE: {
                                if(value == 0) {
                                    mBarCodeReader.setParameter(Se4500ParamIndex.DEC_ILLUM_MODE, 0);
                                    mBarCodeReader.setParameter(Se4500ParamIndex.DEC_AIM_PATTERN, 0);
                                } else if(value == 1) {
                                    mBarCodeReader.setParameter(Se4500ParamIndex.DEC_ILLUM_MODE, 0);
                                    mBarCodeReader.setParameter(Se4500ParamIndex.DEC_AIM_PATTERN, 1);
                                } else if(value == 2) {
                                    mBarCodeReader.setParameter(Se4500ParamIndex.DEC_ILLUM_MODE, 1);
                                    mBarCodeReader.setParameter(Se4500ParamIndex.DEC_AIM_PATTERN, 0);
                                } else if(value == 3) {
                                    mBarCodeReader.setParameter(Se4500ParamIndex.DEC_ILLUM_MODE, 1);
                                    mBarCodeReader.setParameter(Se4500ParamIndex.DEC_AIM_PATTERN, 1);
                                } else if(value == 4) {
                                    mBarCodeReader.setParameter(Se4500ParamIndex.DEC_ILLUM_MODE, 1);
                                    mBarCodeReader.setParameter(Se4500ParamIndex.DEC_AIM_PATTERN, 1);
                                }
                               }
                            break;
                          default:
                            //Log.d("se4500", "setProperties property internalIndex = " + internalIndex  + "   value == " + value );
                            mBarCodeReader.setParameter(internalIndex, value);
                              break;
                     }
                 }
                 
             }
         }
         return 0;
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
        }
	}

    @Override
    public boolean lockHwTriggler(boolean lock) {
        // TODO Auto-generated method stub
        return false;
    }

    private final int[] defParamIndex = new int[]{
            Se4500ParamIndex.CODE39_LENGTH1,
            Se4500ParamIndex.CODE39_LENGTH2,
            Se4500ParamIndex.D25_LENGTH1,
            Se4500ParamIndex.D25_LENGTH2,
            Se4500ParamIndex.M25_LENGTH1,
            Se4500ParamIndex.M25_LENGTH2,
            Se4500ParamIndex.I25_LENGTH1,
            Se4500ParamIndex.I25_LENGTH2,
            Se4500ParamIndex.CODABAR_ENABLE,
            Se4500ParamIndex.CODABAR_LENGTH1,
            Se4500ParamIndex.CODABAR_LENGTH2,
            Se4500ParamIndex.CODE93_LENGTH1,
            Se4500ParamIndex.CODE93_LENGTH2,
//            Se4500ParamIndex.CODE128_LENGTH1,
//            Se4500ParamIndex.CODE128_LENGTH2,
            Se4500ParamIndex.UPCA_SEND_CHECK,
            Se4500ParamIndex.UPCE_SEND_CHECK,
            Se4500ParamIndex.UPCE_SEND_SYS,
            Se4500ParamIndex.MSI_LENGTH1,
            Se4500ParamIndex.MSI_LENGTH2,
            Se4500ParamIndex.GS1_LIMIT_ENABLE,
            Se4500ParamIndex.GS1_EXP_ENABLE,
            Se4500ParamIndex.USPS_4STATE_ENABLE,
            Se4500ParamIndex.AUSTRALIAN_POST_ENABLE,
            Se4500ParamIndex.ROYAL_MAIL_SEND_CHECK ,
            Se4500ParamIndex.KIX_CODE_ENABLE,
            Se4500ParamIndex.JAPANESE_POST_ENABLE,
            Se4500ParamIndex.PDF417_ENABLE,
	        Se4500ParamIndex.EAN_EXT_ENABLE_2_5_DIGIT,
    };
    
    private final int[] defParamVal = new int[] {
            1, //Se4500ParamIndex.CODE39_LENGTH1,
            20, //Se4500ParamIndex.CODE39_LENGTH2,
            2, //Se4500ParamIndex.D25_LENGTH1,
            50, //Se4500ParamIndex.D25_LENGTH2,
            2,
            50,
            2,
            50,
            1, 
            4, 
            20, 
            2, 
            20, 
//            2, 
//            40,
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
            0
    };
    static class Se4500ParamIndex {
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
    }
	private final int[] VALUE_PARAM_INDEX = {
	        Se4500ParamIndex.IMAGE_EXPOSURE_MODE,
	        Se4500ParamIndex.IMAGE_FIXED_EXPOSURE,
            Se4500ParamIndex.IMAGE_PICKLIST_MODE,
            Se4500ParamIndex.IMAGE_ONE_D_INVERSE,
            Se4500ParamIndex.LASER_ON_TIME,
            Se4500ParamIndex.TIMEOUT_BETWEEN_SAME_SYMBOL,
            Se4500ParamIndex.LINEAR_CODE_TYPE_SECURITY_LEVEL,
            Se4500ParamIndex.FUZZY_1D_PROCESSING,
            Se4500ParamIndex.MULTI_DECODE_MODE,
            Se4500ParamIndex.BAR_CODES_TO_READ,
            Se4500ParamIndex.FULL_READ_MODE,
            Se4500ParamIndex.CODE39_ENABLE,
            Se4500ParamIndex.CODE39_ENABLE_CHECK,
            Se4500ParamIndex.CODE39_SEND_CHECK,
            Se4500ParamIndex.CODE39_FULL_ASCII,
            Se4500ParamIndex.CODE39_LENGTH1,
            Se4500ParamIndex.CODE39_LENGTH2,
            Se4500ParamIndex.TRIOPTIC_ENABLE,
            Se4500ParamIndex.CODE32_ENABLE,
            Se4500ParamIndex.CODE32_SEND_START,
            Se4500ParamIndex.C25_ENABLE,
            Se4500ParamIndex.D25_ENABLE, 
            Se4500ParamIndex.D25_LENGTH1,
            Se4500ParamIndex.D25_LENGTH2,
            Se4500ParamIndex.M25_ENABLE,
            Se4500ParamIndex.CODE11_ENABLE,
            Se4500ParamIndex.CODE11_ENABLE_CHECK,
            Se4500ParamIndex.CODE11_SEND_CHECK,
            Se4500ParamIndex.CODE11_LENGTH1,
            Se4500ParamIndex.CODE11_LENGTH2,
            Se4500ParamIndex.I25_ENABLE,
            Se4500ParamIndex.I25_ENABLE_CHECK,
            Se4500ParamIndex.I25_SEND_CHECK,
            Se4500ParamIndex.I25_LENGTH1,
            Se4500ParamIndex.I25_LENGTH2,
            Se4500ParamIndex.I25_TO_EAN13,
            Se4500ParamIndex.CODABAR_ENABLE,
            Se4500ParamIndex.CODABAR_NOTIS,
            Se4500ParamIndex.CODABAR_CLSI,
            Se4500ParamIndex.CODABAR_LENGTH1,
            Se4500ParamIndex.CODABAR_LENGTH2,
            Se4500ParamIndex.CODE93_ENABLE,
            Se4500ParamIndex.CODE93_LENGTH1,
            Se4500ParamIndex.CODE93_LENGTH2,
            Se4500ParamIndex.CODE128_ENABLE,
            Se4500ParamIndex.CODE128_LENGTH1,
            Se4500ParamIndex.CODE128_LENGTH2,
            Se4500ParamIndex.CODE_ISBT_128,
            Se4500ParamIndex.CODE128_GS1_ENABLE,
            Se4500ParamIndex.UPCA_ENABLE, 
            Se4500ParamIndex.UPCA_SEND_CHECK,
            Se4500ParamIndex.UPCA_SEND_SYS,
            Se4500ParamIndex.UPCA_TO_EAN13,
            Se4500ParamIndex.UPCE_ENABLE,
            Se4500ParamIndex.UPCE_SEND_CHECK,
            Se4500ParamIndex.UPCE_SEND_SYS,
            Se4500ParamIndex.UPCE_TO_UPCA,
            Se4500ParamIndex.UPCE1_ENABLE,
            Se4500ParamIndex.UPCE1_SEND_CHECK,
            Se4500ParamIndex.UPCE1_SEND_SYS,
            Se4500ParamIndex.UPCE1_TO_UPCA,
            Se4500ParamIndex.EAN13_ENABLE,
            //Se4500ParamIndex.EAN13_SEND_CHECK,
            Se4500ParamIndex.EAN13_BOOKLANDEAN,
            Se4500ParamIndex.EAN13_BOOKLAND_FORMAT,
            Se4500ParamIndex.EAN8_ENABLE,
            //Se4500ParamIndex.EAN8_SEND_CHECK,
            Se4500ParamIndex.EAN8_TO_EAN13,
            Se4500ParamIndex.EAN_EXT_ENABLE_2_5_DIGIT,
            Se4500ParamIndex.UPC_EAN_SECURITY_LEVEL,
            Se4500ParamIndex.UCC_COUPON_EXT_CODE,
            Se4500ParamIndex.MSI_ENABLE,
            Se4500ParamIndex.MSI_REQUIRE_2_CHECK,
            Se4500ParamIndex.MSI_SEND_CHECK,
            Se4500ParamIndex.MSI_CHECK_2_MOD_11,
            Se4500ParamIndex.MSI_LENGTH1,
            Se4500ParamIndex.MSI_LENGTH2,
            Se4500ParamIndex.GS1_14_ENABLE,
            Se4500ParamIndex.GS1_14_TO_UPC_EAN,
            Se4500ParamIndex.GS1_LIMIT_ENABLE,
            Se4500ParamIndex.GS1_EXP_ENABLE,
            Se4500ParamIndex.GS1_EXP_LENGTH1,
            Se4500ParamIndex.GS1_EXP_LENGTH2,
            Se4500ParamIndex.US_POSTNET_ENABLE,
            Se4500ParamIndex.US_PLANET_ENABLE,
            Se4500ParamIndex.US_POSTAL_SEND_CHECK,
            Se4500ParamIndex.USPS_4STATE_ENABLE,
            Se4500ParamIndex.UPU_FICS_ENABLE,
            Se4500ParamIndex.ROYAL_MAIL_ENABLE,
            Se4500ParamIndex.ROYAL_MAIL_SEND_CHECK,
            Se4500ParamIndex.AUSTRALIAN_POST_ENABLE,
            Se4500ParamIndex.KIX_CODE_ENABLE,
            Se4500ParamIndex.JAPANESE_POST_ENABLE,
            Se4500ParamIndex.PDF417_ENABLE,
            Se4500ParamIndex.MICROPDF417_ENABLE,
            Se4500ParamIndex.COMPOSITE_CC_AB_ENABLE,
            Se4500ParamIndex.COMPOSITE_CC_C_ENABLE,
            Se4500ParamIndex.COMPOSITE_TLC39_ENABLE,
            Se4500ParamIndex.HANXIN_ENABLE,
            Se4500ParamIndex.HANXIN_INVERSE,
            Se4500ParamIndex.DATAMATRIX_ENABLE,
            Se4500ParamIndex.DATAMATRIX_LENGTH1,
            Se4500ParamIndex.DATAMATRIX_LENGTH2,
            Se4500ParamIndex.DATAMATRIX_INVERSE,
            Se4500ParamIndex.MAXICODE_ENABLE,
            Se4500ParamIndex.QRCODE_ENABLE,
            Se4500ParamIndex.QRCODE_INVERSE,
            Se4500ParamIndex.MICROQRCODE_ENABLE,
            Se4500ParamIndex.AZTEC_ENABLE,
            Se4500ParamIndex.AZTEC_INVERSE,
            Se4500ParamIndex.DEC_2D_LIGHTS_MODE,
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
            Se4500ParamIndex.DEC_ILLUM_POWER_LEVEL,
            Se4500ParamIndex.DEC_PICKLIST_AIM_MODE,
            DEC_PICKLIST_AIM_DELAY,
            DEC_MaxMultiRead_COUNT,
            DEC_Multiple_Decode_TIMEOUT,
            DEC_Multiple_Decode_INTERVAL,
            DEC_Multiple_Decode_MODE,
            DEC_OCR_MODE,
            DEC_OCR_TEMPLATE,
            Se4500ParamIndex.TRANSMIT_CODE_ID,
            DOTCODE_ENABLE
    };
}
