package com.android.server.scanner;

import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.device.ScanNative;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;
import android.util.Log;

import com.android.server.ScanServiceWrapper;

abstract public class SerialScanner extends Scanner {

	protected byte[] mBuffer;
	protected static int mBufferSize = 4096;
	protected int mBufOffset = 0;
	protected boolean isOpened = false;
	protected String path = "/dev/ttyMSM3";
	protected ScanReaderThread mReader;
	protected ScanWorkerThread mWorker;
	protected ScanWorkerHandler mHandler;
	protected FileInputStream mFileInputStream;
	private String TAG = "SerialScanner";
	protected int mBaudrate = 9600;
	protected boolean mIsDecodeTimeout = true;
	protected boolean mIsSetParamTimeout = false;
	protected boolean mIsGetParamTimeout = false;

	public final int GET_PARAM_TIMEOUT = 3000;
	public final int SET_PARAM_TIMEOUT = 3000;

	public final int MSG_ENABLE_DECODE = 0;
	public final int MSG_START_DECODE = 1;
	public final int MSG_STOP_DECODE = 2;
	public final int MSG_COMMIT_PROPERTY = 3;
	public final int MSG_SET_PARAM_TIMEOUT = 4;
	public final int MSG_GET_PARAM_TIMEOUT = 5;
	public final int MSG_TEST_DECODE_TIMEOUT = 6;
	public final int MSG_DECODE_TIMEOUT = 7;
	public final int MSG_DEFAULT_PARAME = 8;
	public final int MSG_RESPONSE_ACK = 9;
	public final int MSG_RESPONSE_NCK = 10;

	private int mState;
	private final int SCANNER_STATE_START = 0x01;
	private final int SCANNER_STATE_STOP = 0x02;
	private final int SCANNER_STATE_OPEN = 0x04;
	private final int SCANNER_STATE_CLOSE = 0x08;

	private final CountDownLatch mInitializedLatch = new CountDownLatch(1);
	protected final Object mLock = new Object();
	protected boolean waittingACK = false;
	private int mFd;

	public SerialScanner(ScanServiceWrapper scanService) {
		mState = SCANNER_STATE_CLOSE;
		mScanService = scanService;
		mBuffer = new byte[mBufferSize];
		mWorker = new ScanWorkerThread("ScanServiceWorker");
		mWorker.start();
		while (true) {
			try {
				mInitializedLatch.await();
				break;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

	}

	private void startCoder(int timeout) {
		if (timeout < 0 || timeout > 9000) {
			timeout = 3000;
		}
		Log.i(TAG, "startCoder......" + mState);
	    if (mState == SCANNER_STATE_START) {
            // if already start decode ,must stop it first.don't call
            // stopdecode.
	        if (mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == Triggering.PULSE.toInt()) {
	            if(mScannerType == ScannerFactory.TYPE_N3680|| mScannerType == ScannerFactory.TYPE_HONYWARE) {
	                ScanNative.triggerCmd(1,ScannerFactory.TYPE_N3680);
		    // urovo add shenpidong begin 2019-09-12
	            } /*else if(mScannerType == ScannerFactory.TYPE_DM30) {
	                ScanNative.triggerCmd(1, ScannerFactory.TYPE_DM30);
	            } */else {
		    // urovo add shenpidong end 2019-09-12
                    if(android.os.Build.PROJECT.equals("SQ46")) {
                        ScanNative.triggerCmd(1,ScannerFactory.TYPE_HONYWARE);
                    } else {
                        ScanNative.scanUp();
                    }
                }
	        } else {
	            if(android.os.Build.PROJECT.equals("SQ46")) {
                    ScanNative.triggerCmd(1,ScannerFactory.TYPE_HONYWARE);
                } else {
                    ScanNative.scanUp();
                }
	        }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mState = SCANNER_STATE_START;
        if (mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == Triggering.PULSE.toInt()) {
            if(mScannerType == ScannerFactory.TYPE_N3680 || mScannerType == ScannerFactory.TYPE_HONYWARE) {
                ScanNative.triggerCmd(0,ScannerFactory.TYPE_N3680);
	    // urovo add shenpidong begin 2019-09-12
            } /*else if(mScannerType == ScannerFactory.TYPE_DM30) {
                ScanNative.triggerCmd(0, ScannerFactory.TYPE_DM30);
            } */else {
	    // urovo add shenpidong end 2019-09-12
                if(android.os.Build.PROJECT.equals("SQ46")) {
                    ScanNative.triggerCmd(0,ScannerFactory.TYPE_HONYWARE);
                } else {
                    ScanNative.scanDown();
                }
            }
        } else {
            if(android.os.Build.PROJECT.equals("SQ46")) {
                ScanNative.triggerCmd(0,ScannerFactory.TYPE_HONYWARE);
            } else {
                ScanNative.scanDown();
            }
        }
	}

	private void stopCoder() {
	    Log.i(TAG, "stopCoder......");

        if (mState == SCANNER_STATE_START) {
            mState = SCANNER_STATE_STOP;
            if (mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == Triggering.PULSE.toInt()) {
                if(mScannerType == ScannerFactory.TYPE_N3680 || mScannerType == ScannerFactory.TYPE_HONYWARE) {
                    ScanNative.triggerCmd(1,ScannerFactory.TYPE_N3680);
		// urovo add shenpidong begin 2019-09-12
                } /*else if(mScannerType == ScannerFactory.TYPE_DM30) {
                    ScanNative.triggerCmd(1, ScannerFactory.TYPE_DM30);
                } */else {
		// urovo add shenpidong end 2019-09-12
                    if(android.os.Build.PROJECT.equals("SQ46")) {
                        ScanNative.triggerCmd(1,ScannerFactory.TYPE_HONYWARE);
                    } else {
                        ScanNative.scanUp();
                    }
                }
            } else {
                if(android.os.Build.PROJECT.equals("SQ46")) {
                    ScanNative.triggerCmd(1,ScannerFactory.TYPE_HONYWARE);
                } else {
                    ScanNative.scanUp();
                }
            }
        }
	}

	private void enable() {
		Log.i(TAG, "open......");

		if (isOpened) {
			Log.i(TAG, "-already-open......");
			return;
		}

		mBufOffset = 0;
		isOpened = true;
		waittingACK = false;
		mState = SCANNER_STATE_OPEN;
		ScanNative.openScan();
		mFd = ScanNative.openSerialFd(path, mBaudrate, mScannerType);
		
		if(mFd <= 0) {
		    isOpened = false;
		    mState = SCANNER_STATE_CLOSE;
		    ScanNative.closeScan();
		    return;
		}
		mReader = new ScanReaderThread("ScanServiceReader");
		if (mReader == null) {
		    isOpened = false;
		    mState = SCANNER_STATE_CLOSE;
		    ScanNative.closeSerialFd(mFd);
		    ScanNative.closeScan();
		    return;
		}
		mReader.start();
	}

	private void disable() {
		Log.i(TAG, "close......");
		mState = SCANNER_STATE_CLOSE;
		if (!isOpened) {
			Log.i(TAG, "-already-close......");
			return;
		}

		isOpened = false;
		if (mReader != null) {
            mReader.interrupt();
            mReader = null;
        }
		ScanNative.closeSerialFd(mFd);
		ScanNative.closeScan();
	}

	public void startDecode(int timeout) {
	    if (mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == Triggering.CONTINUOUS.toInt()) {
	        if(mScannerType == ScannerFactory.TYPE_SE955) {
	            ScanNative.scanUp();
	            //ScanNative.setTriggerMode(Triggering.CONTINUOUS.toInt());
	            sendCommand(3);
	        } else if(mScannerType == ScannerFactory.TYPE_HONYWARE){
	            ScanNative.setHWProperties(HonyWareScanner.ConScanMode);
	        }else if(mScannerType == ScannerFactory.TYPE_N3680) {
                ScanNative.setHWProperties(N3680Scanner.ConScanMode);
	    // urovo add shenpidong begin 2019-09-12
            } /*else if(mScannerType == ScannerFactory.TYPE_DM30) {
                if(mScanService.getPropertyInt(PropertyID.IMAGE_PICKLIST_MODE) == 1) {
                    ScanNative.setDM30Properties(DM30Scanner.PresentationModePhone);
                } else {
                    ScanNative.setDM30Properties(DM30Scanner.PresentationMode);
                }
            }*/
	    // urovo add shenpidong end 2019-09-12
        } else {
            synchronized (mHandler) {
                mIsDecodeTimeout = true;
                sendMessage(MSG_START_DECODE, timeout, null);
            }
		}

	}

	public void stopDecode() {
	    if (mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == Triggering.CONTINUOUS.toInt()) {
            if(mScannerType == ScannerFactory.TYPE_SE955) {
              //ScanNative.setTriggerMode(Triggering.HOST.toInt());
                sendCommand(2);
                ScanNative.scanUp();
            } else if(mScannerType == ScannerFactory.TYPE_HONYWARE) {
                ScanNative.setHWProperties(HonyWareScanner.NorScanMode);
            } else if(mScannerType == ScannerFactory.TYPE_N3680) {
                ScanNative.setHWProperties(N3680Scanner.NorScanMode);
	    // urovo add shenpidong begin 2019-09-12
            }/* else if(mScannerType == ScannerFactory.TYPE_DM30) {
                ScanNative.setDM30Properties(DM30Scanner.TriggerMode);
            }*/
	    // urovo add shenpidong end 2019-09-12
        } else {
            synchronized (mHandler) {
                sendMessage(MSG_STOP_DECODE, 0, null);
            }
		}

	}

	public boolean open() {
		// hold a wake lock while messages are pending
		synchronized (mHandler) {
			sendMessage(MSG_ENABLE_DECODE, 1, null);
		}
                return true;

	}

	@Override
	public void close() {
		synchronized (mHandler) {
		    if (mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == Triggering.CONTINUOUS.toInt()) {
		        if(mScannerType == ScannerFactory.TYPE_SE955) {
		            sendCommand(2);
	                ScanNative.scanUp();
	            } else if(mScannerType == ScannerFactory.TYPE_HONYWARE){
	                ScanNative.setHWProperties(HonyWareScanner.NorScanMode);
	            } else if(mScannerType == ScannerFactory.TYPE_N3680) {
	                ScanNative.setHWProperties(N3680Scanner.NorScanMode);
		    // urovo add shenpidong begin 2019-09-12
	            }/* else if(mScannerType == ScannerFactory.TYPE_DM30) {
                    ScanNative.setDM30Properties(DM30Scanner.TriggerMode);
                }*/
		    // urovo add shenpidong end 2019-09-12
//		    Log.i(TAG, "close ------- MSG_ENABLE_DECODE");
		        mHandler.sendEmptyMessageDelayed(MSG_ENABLE_DECODE, 500);
            } else {
                sendMessage(MSG_ENABLE_DECODE, 0, null);
            }
		}

	}

	public void openPhoneMode() {
        if(mScannerType == ScannerFactory.TYPE_N3680)
            ScanNative.setHWProperties(N3680Scanner.IMAGE_PICKLIST_MODE);
	}

	public void closePhoneMode() {
        if(mScannerType == ScannerFactory.TYPE_N3680)
            ScanNative.setHWProperties(N3680Scanner.NorScanMode);
	}

	private void sendMessage(int message, int arg, Object obj) {
		// hold a wake lock while messages are pending
		mHandler.removeMessages(message);
		Message m = Message.obtain(mHandler, message);
		m.arg1 = arg;
		m.obj = obj;
		mHandler.sendMessage(m);
	}

	class ScanWorkerHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ENABLE_DECODE:
				if (msg.arg1 == 1) {
					enable();
				} else {
					disable();
				}
				break;
			case MSG_START_DECODE:
				int timeout = mScanService.getPropertyInt(PropertyID.LASER_ON_TIME);
				timeout = timeout*100;
				Log.i(TAG, "startCoder.............."+ timeout);
				startCoder(timeout);
				/*if(mScannerType == ScannerFactory.TYPE_N4313) {
    				mHandler.removeMessages(MSG_TEST_DECODE_TIMEOUT);
    				sendEmptyMessageDelayed(MSG_TEST_DECODE_TIMEOUT, timeout);
				}*/
				break;
			case MSG_STOP_DECODE:
			    mHandler.removeMessages(MSG_TEST_DECODE_TIMEOUT);
				stopCoder();
				break;
			case MSG_DECODE_TIMEOUT:
				break;
			case MSG_RESPONSE_NCK:
			    Log.i(TAG, "----unlock-------MSG_RESPONSE_NCK---:");
			    //waittingACK = true;
			    synchronized (mLock) {
			        mLock.notify();
			    }
				break;
			case MSG_RESPONSE_ACK:
			    Log.i(TAG, "----unlock-------MSG_RESPONSE_ACK---:");
			    synchronized (mLock) {
			        waittingACK = false;
			        mLock.notify();
			    }
			    break;
			case MSG_GET_PARAM_TIMEOUT:
				onGetParamTimeout();
				break;
			case MSG_TEST_DECODE_TIMEOUT:
				if (true == mIsDecodeTimeout) {
					if (mState == SCANNER_STATE_START) {
						mState = SCANNER_STATE_STOP;
						ScanNative.scanUp();
					}
					mIsDecodeTimeout = false;
				}
				break;
			case MSG_DEFAULT_PARAME:
				// onSetDefaults();
				break;

			default:
				break;
			}

		}
	}

	class ScanWorkerThread extends Thread {

		public ScanWorkerThread(String name) {
			super(name);
		}

		public void run() {
			Looper.prepare();
			mHandler = new ScanWorkerHandler();
			// signal when we are initialized and ready to go
			mInitializedLatch.countDown();
			Looper.loop();
		}
	}

	class ScanReaderThread extends Thread {

//		 private boolean readrun = true;

		public ScanReaderThread(String name) {
			super(name);
		}

		public void run() {
			Log.i(TAG, "runing......");
			byte[] temp = new byte[16];
			int size = 0;
			while (!isInterrupted()) {
				/*if (mFileInputStream == null){
					Log.i(TAG, "no io......");
					mReader = null;
					return;
				}*/
					
				/*try {
					size = mFileInputStream.read(temp);
				} catch (IOException e) {
					// TODO: handle exception
//					e.printStackTrace();
					Log.i(TAG, "exit......");
					mReader = null;
					return;
				}*/
				Log.i(TAG, "serialRead......mFd:" + mFd);
			    size = ScanNative.serialRead(mFd, temp, 16);
				if (isInterrupted()) {
					Log.i(TAG, "stop......");
					mReader = null;
					return;
				}

				Log.i(TAG, "size......[" + size + "]");
				
				if (size > 0) {
					Log.i(TAG, "mBufOffset......[" + mBufOffset + "]");
					if (mBufOffset > mBufferSize-16 || mBufOffset < 0)
						mBufOffset = 0;
					System.arraycopy(temp, 0, mBuffer, mBufOffset, size);
                    String ttmp = "recv:\n";
                    for (int i = mBufOffset; i < mBufOffset + size; i++){
						ttmp += "buf[" + i + "]......"+"["+mBuffer[i]+"]"+"\n" ;
					}
					Log.i(TAG,ttmp);
					mBufOffset += size;
					Log.i(TAG, "mBufOffset new......[" + mBufOffset + "]");
					while (onDataReceived()) {
						if (mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) !=4 && mState == SCANNER_STATE_START) {
							mState = SCANNER_STATE_STOP;
							ScanNative.scanUp();
						}
					}
					if (size < 16)
						mBufOffset = 0;
				}
			}

		}
	}

	protected boolean onDataReceived() {
		Log.i(TAG, "onDataReceived()... from parent class ");
		return true;
	}

	protected void release() {
		sendMessage(MSG_ENABLE_DECODE, 0, null);
		Log.i(TAG, "releaseres()... from parent class ");
		if (mWorker != null) {
			mWorker.interrupt();
			mWorker = null;
		}
		if (mReader != null) {
			mReader.interrupt();
			mReader = null;
		}
/*		if (mFileInputStream != null)
			mFileInputStream = null;*/
	}

	public boolean lockHwTriggler(boolean lock) {
        if(lock) {
            return ScanNative.lockTriggle();
        } else {
            return sendCommand(1);
        }
	}

    private boolean sendCommand(int type) {
        waittingACK = true;
        for (int i = 0; i <= 3; i++) {
            Log.i(TAG, "-------do---------sendCommand:" + i);
            if (1 == type) {
                ScanNative.unlockTriggle();
            } else if (2 == type) {
                ScanNative.setTriggerMode(Triggering.HOST.toInt());
            } else if (3 == type) {
                ScanNative.setTriggerMode(Triggering.CONTINUOUS.toInt());
            }
            synchronized (mLock) {
                try {
                    mLock.wait(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!waittingACK)
                    break;
            }
        }

        Log.i(TAG, "----------------sendCommand:" + type + "  =  " + waittingACK);
        return !waittingACK;
    }

	abstract protected void onGetParamTimeout();

	abstract protected void onSetParamTimeout();
	// abstract protected void onSetDefaults();
	// abstract protected void onParamSend(byte[] params);
}
