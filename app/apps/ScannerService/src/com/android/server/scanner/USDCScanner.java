package com.android.server.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.LicenseHelper;
import android.device.scanner.configuration.PropertyID;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.os.UserHandle;
import com.android.server.ScanServiceWrapper;
import android.device.scanner.configuration.Symbology;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import com.android.server.scanner.camera2.CameraProviderManager;
import com.ubx.decoder.BarcodeReader;
import com.ubx.decoder.General;
import com.ubx.decoder.HSMDecodeResult;
import com.ubx.decoder.OCRActiveTemplate;
import com.ubx.decoder.SDSProperties;
import com.ubx.decoder.license.ActivationManager;

/**
 * Created by xjf on 19-07-04.
 */

public class USDCScanner extends Scanner{
    private static String TAG = "USDCScanner";
    private CaptureHandler mHandler;
    private CameraProviderManager mCameraManager;
    private enum State {
        PREVIEW,
        DECODING,
        DONE
    }
    private static final long InactivityTimer__DURATION_MS = 6 * 10 * 1000L;
    private static final long InactivityTimer__DURATION_CONTINUOUS_MODE_MS = 5 * 60 * 1000L;
    private long workPreviewTime = 0;
    private boolean workDecodeTimeOut = false;
    int CONTINUOUS_MODE = 8;
    private int saveMode = SaveMode.NOTSAVE.ordinal();
    private State state;
    private int openCameraId = 1;
    private long delayMillis = 0;
    //skip_first_frame
    private int delayDecode = 1;
    private int intervalTime = 0;
    boolean enableHSMDecoder = false;
    private BarcodeReader mBarcodeReader;
    private String decoderVersion = "";
    private long waitMultipleStartTime = 0;
    private static boolean bWaitMultiple = true;	// flag for single or multiple decode
    private int g_nMultiReadResultCount = 0;		// For tracking # of multiread results
    private int g_nMaxMultiReadCount = 1;		// Maximum multiread count
    private int waitMultipleMode = 0;
    private int waitMultipleTiemOut = 10*1000;
    private byte[] preDecodeData = null;
    private boolean activateDecoder = false;
    private int activateDecoderTimes = 2;
    private boolean hasLicense = true;
    private boolean syncLicenseStrore = false;
    public USDCScanner(ScanServiceWrapper scanService){
        mScannerType = ScannerFactory.TYPE_SE2030;
        mScanService = scanService;
        mContext = scanService.mContext;
        mHandler = new CaptureHandler(mContext.getMainLooper());
        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            mPropIndexHashMap.put(INTERNAL_PROPERTY_INDEX[i], 1);
        }
        syncLicenseStrore(false);
    }
    private void syncLicenseStrore(final boolean activate) {
        try{
            String buildDate = android.os.SystemProperties.get("ro.build.date.utc");
            long defaultBuildTime = Long.parseLong(buildDate) * 1000;
            long currentTime = System.currentTimeMillis();
            Log.d(TAG, " currentTime" + currentTime + " defaultBuildTime " + defaultBuildTime);
            if(currentTime < defaultBuildTime) {
                android.os.SystemClock.setCurrentTimeMillis(defaultBuildTime);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    ActivationManager.syncLicenseStrore(activate);
                }
            }).start();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    final class DecodeHandler extends Handler {

        private boolean running = true;

        DecodeHandler() {
        }

        @Override
        public void handleMessage(Message message) {
            if (!running) {
                return;
            }
            switch (message.what) {
                case CAMERA_MODE_DECODE:
                    decode((byte[]) message.obj, message.arg1, message.arg2);
                    break;
                case CAMERA_MODE_QUIT:
                    running = false;
                    Looper.myLooper().quit();
                    break;
            }
        }

        /**
         * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
         * reuse the same reader objects from one decode to the next.
         *
         * @param data   The YUV preview frame.
         * @param width  The width of the preview frame.
         * @param height The height of the preview frame.
         */
        private void decode(byte[] data, int width, int height) {
            //Log.d(TAG, "decode width=" + width + " height = " + height);
            long start = System.currentTimeMillis();
            int ret = mBarcodeReader.decodeImage(data, width, height);
            if (mHandler != null) {
                HSMDecodeResult firstResult = null;
                if(ret > 0) {
                    firstResult = mBarcodeReader.getDecodeData();
                }
                long end = System.currentTimeMillis();
                Log.i(TAG, " startDecode data dec="+ (end - start) + " ms");
                if (ret > 0 && firstResult != null) {
                    if(CONTINUOUS_MODE == 4) {
                        if(waitMultipleMode == 1) {
                            if (preDecodeData != null && System.currentTimeMillis() - waitMultipleStartTime < waitMultipleTiemOut && comparabytes(firstResult.getBarcodeDataBytes(), firstResult.getBarcodeDataLength())) {
                                Message message = Message.obtain(mHandler, BCRDR_MSG_FRAME_ERROR);
                                message.sendToTarget();
                                return;
                            }
                        }
                        g_nMultiReadResultCount++;
                        preDecodeData = firstResult.getBarcodeDataBytes();
                        waitMultipleStartTime = System.currentTimeMillis();
                        Message message = mHandler.obtainMessage(BCRDR_MSG_DECODE_COMPLETE);
                        message.obj = preDecodeData;
                        message.arg1 = firstResult.getBarcodeDataLength();
                        message.arg2 = firstResult.getSymbologyId();
                        message.sendToTarget();
                    } else {
                        Message message = mHandler.obtainMessage(BCRDR_MSG_DECODE_COMPLETE);
                        message.obj = firstResult.getBarcodeDataBytes();
                        message.arg1 = firstResult.getBarcodeDataLength();
                        message.arg2 = firstResult.getSymbologyId();
                        message.sendToTarget();
                    }
                    if (saveMode == SaveMode.SAVEDECODESUCCESSALLBMP.getVal()) {
                        Utils.saveYUVImage(mContext, data, width, height, end);
                    } else if (saveMode == SaveMode.SAVEDECODESUCCESSLASTBMP.getVal()) {
                        Utils.saveYUVImage(mContext, data, width, height, 0);
                    }
                } else {
                    Message message = Message.obtain(mHandler, BCRDR_MSG_FRAME_ERROR);
                    message.sendToTarget();
                }
            }
            if (saveMode == SaveMode.SAVEPREVIEWALLBMP.getVal()) {
                Utils.saveYUVImage(mContext, data, width, height, start);
            } else if (saveMode == SaveMode.SAVEPREVIEWLASTBMP.getVal()) {
                Utils.saveYUVImage(mContext, data, width, height, 0);
            }
        }
    }
    boolean comparabytes(byte[] data, int length) {
        int i =0;
        if(length != preDecodeData.length) return false;
        for(i = 0; i < length && i < preDecodeData.length; i++) {
            if(preDecodeData[i] != data[i]) {
                return false;
            }
        }
        return true;
    }
    private static final int BCRDR_MSG_DECODE_COMPLETE	= 1;
    private static final int BCRDR_MSG_DECODE_TIMEOUT	= 2;
    private static final int BCRDR_MSG_DECODE_CANCELED	= 3;
    private static final int BCRDR_MSG_DECODE		= 4;
    private static final int BCRDR_MSG_DECODE_PREVIEW		= 5;
    private static final int BCRDR_MSG_DECODE_REPREVIEW		= 6;
    private static final int BCRDR_MSG_FRAME_ERROR		= 7;
    private static final int BCRDR_MSG_INIT		= 8;
    private static final int BCRDR_MSG_RELEASE		= 9;
    private static final int BCRDR_MSG_INIT_DECODE		= 10;
    private static final int BCRDR_MSG_TIMER_PREVIEW		= 11;
    private final class DecodeThread extends Thread {
        private Handler handler;
        private final CountDownLatch handlerInitLatch;

        public DecodeThread() {
            handlerInitLatch = new CountDownLatch(1);
        }

        public Handler getHandler() {
            try {
                handlerInitLatch.await();
            } catch (InterruptedException ie) {
                // continue?
            }
            return handler;
        }

        @Override
        public void run() {
            Looper.prepare();
            handler = new DecodeHandler();
            handlerInitLatch.countDown();
            Looper.loop();
        }
    }

    class CaptureHandler extends Handler {
        private final DecodeThread decodeThread;

        public CaptureHandler(Looper looper) {
            super(looper);
            state = State.DONE;
            decodeThread = new DecodeThread();
            decodeThread.start();
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case BCRDR_MSG_DECODE_PREVIEW:
                    workPreviewTime = System.currentTimeMillis();
                    if (mCameraManager != null) {
                        //mCameraManager.setDecodeHandler(decodeThread.getHandler());
                        mCameraManager.restartDecoding();
                    }
                    break;
                case BCRDR_MSG_DECODE_COMPLETE:
                    workPreviewTime = System.currentTimeMillis();
                    if(CONTINUOUS_MODE == 4) {
                        if(intervalTime > 0) {
                            this.sendEmptyMessageDelayed(BCRDR_MSG_DECODE_PREVIEW, intervalTime);
                        } else {
                            if (mCameraManager != null) {
                                //mCameraManager.setDecodeHandler(decodeThread.getHandler());
                                mCameraManager.restartDecoding();
                            }
                        }
                        sendBroadcast((byte[])message.obj, message.arg2, message.arg1);
                    } else {
                        removeMessages(BCRDR_MSG_DECODE_TIMEOUT);
                        state = State.DONE;
                        sendBroadcast((byte[])message.obj, message.arg2, message.arg1);
                        if (mCameraManager != null) {
                            mCameraManager.stopDecoding();
                            //this.removeCallbacksAndMessages(null);
                        }
                        /*try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }*/
                        setAimerOrIllumMode(0);
                    }
                    //updateAimerOrIllum(ILLUM_PATH , LIGHT_ILLUM_OFF);
                    //updateAimerOrIllum(AIMER_FLICKER_PATH , LIGHT_AIMER_OFF);
                    /*Bundle messageResult = (Bundle) message.obj;
                    ArrayList<Result> scanResults = messageResult.getParcelableArrayList("scanResults");
                    handleDecode(scanResults);*/
                    break;
                case BCRDR_MSG_FRAME_ERROR:
                    // We're decoding as fast as possible, so when one decode fails, start another.
                    //state = State.DECODING;
                    //workPreviewTime = System.currentTimeMillis();
                    if(state == State.DECODING) {
                        //restartPreviewAndDecode();
                        mCameraManager.restartDecoding();
                    }
                    break;
                case BCRDR_MSG_DECODE_CANCELED:
                    workPreviewTime = System.currentTimeMillis();
                    state = State.DONE;
                    stopCameraDecode();
                    removeMessages(BCRDR_MSG_DECODE_TIMEOUT);
                    break;
                case BCRDR_MSG_DECODE:
                    workPreviewTime = System.currentTimeMillis();
                    if(state == State.DECODING) {
                        //restartPreviewAndDecode();
                        Log.d(TAG, "state == State.DECODING");
                        return;
                    }
                    g_nMultiReadResultCount = 0;
                    preDecodeData = null;
                    int timeOut = message.arg1;
                    startCameraDecode();
                    state = State.DECODING;
                    if(CONTINUOUS_MODE == 2) {
                        this.removeMessages(BCRDR_MSG_DECODE_TIMEOUT);
                        this.sendEmptyMessageDelayed(BCRDR_MSG_DECODE_TIMEOUT, timeOut > 0 ? timeOut: 5000);
                    }
                    restartPreviewAndDecode();
                    break;
                case BCRDR_MSG_DECODE_REPREVIEW:
                    workPreviewTime = System.currentTimeMillis();
                    state = State.DONE;
                    startCameraPreview();
                    restartPreviewAndDecode();
                    if (mCameraManager != null && isOpen()) {
                        state = State.DECODING;
                        mCameraManager.startDecoding(delayDecode);
                    }
                    //state = State.DECODING;
                    break;
                case BCRDR_MSG_RELEASE:
                    state = State.DONE;
                    closeDriver();
                    break;
                case BCRDR_MSG_INIT:
                    state = State.DONE;
                    connectDecoderLibrary();
                    startCameraPreview();
                    restartPreviewAndDecode();
                    break;
                case BCRDR_MSG_INIT_DECODE:
                    state = State.DONE;
                    openDriver(openCameraId);
                    startCameraPreview();
                    restartPreviewAndDecode();
                    startCameraDecode();
                    state = State.DECODING;
                    break;
                case BCRDR_MSG_DECODE_TIMEOUT:
                    state = State.DONE;
                    stopCameraDecode();
                    break;
                case BCRDR_MSG_TIMER_PREVIEW:
                    long currentTime = System.currentTimeMillis();
                    Log.d(TAG, "timer_preview " + (currentTime - workPreviewTime));
                    if (currentTime - workPreviewTime >= (CONTINUOUS_MODE == 4 ? InactivityTimer__DURATION_CONTINUOUS_MODE_MS : InactivityTimer__DURATION_MS)) {
                        closeDriver();
                        state = State.DONE;
                    } else {
                        workPreviewTime = System.currentTimeMillis();
                        sendEmptyMessageDelayed(BCRDR_MSG_TIMER_PREVIEW, CONTINUOUS_MODE == 4 ? InactivityTimer__DURATION_CONTINUOUS_MODE_MS : InactivityTimer__DURATION_MS);
                    }
                    break;
            }
        }

        public void quitSynchronously() {
            state = State.DONE;
            stopCameraPreview();
            stopCameraDecode();
            Message quit = Message.obtain(decodeThread.getHandler(), CAMERA_MODE_QUIT);
            quit.sendToTarget();
            try {
                // Wait at most half a second; should be enough time, and onPause() will timeout quickly
                decodeThread.join(500L);
            } catch (InterruptedException e) {
                // continue
            }
            // Be absolutely sure we don't send any queued up messages
            removeMessages(BCRDR_MSG_DECODE_COMPLETE);
            removeMessages(BCRDR_MSG_FRAME_ERROR);
        }

        private void restartPreviewAndDecode() {
            setDecodeHandler(decodeThread.getHandler());
        }
    }
    private static final String AIMER_PATH = "/sys/kernel/kobject_scanner_led/scanner_aimled";
    private static final String AIMER_FLICKER_PATH = "/sys/kernel/kobject_scanner_led/scanner_aimflickerled";
    private static final String ILLUM_PATH = "/sys/kernel/kobject_scanner_led/scanner_illled";
    private static byte[] LIGHT_ILLUM_OFF = {'0'};
    private static byte[] LIGHT_ILLUM_ON = {'7'};
    private static byte[] LIGHT_ILLUM_ON_1 = {'1'};
    private static byte[] LIGHT_ILLUM_ON_2 = {'2'};
    private static byte[] LIGHT_ILLUM_ON_3 = {'3'};
    private static byte[] LIGHT_ILLUM_ON_4 = {'4'};
    private static byte[] LIGHT_ILLUM_ON_5 = {'5'};
    private static byte[] LIGHT_ILLUM_ON_6 = {'6'};
    private static byte[] LIGHT_ILLUM_ON_7 = {'7'};
    private static final byte[] LIGHT_AIMER_OFF = {'0'};
    private static final byte[] LIGHT_AIMER_ON = {'1'};
    private static int AimerIllumMode = 3;
    private void updateLightGrade(int lightGrade) {
        if(lightGrade <1 || lightGrade > 7) {
            lightGrade = 1;
        }
        switch(lightGrade) {
            case 1:
                LIGHT_ILLUM_ON = LIGHT_ILLUM_ON_1;
                break;
            case 2:
                LIGHT_ILLUM_ON = LIGHT_ILLUM_ON_2;
                break;
            case 3:
                LIGHT_ILLUM_ON = LIGHT_ILLUM_ON_3;
                break;
            case 4:
                LIGHT_ILLUM_ON = LIGHT_ILLUM_ON_4;
                break;
            case 5:
                LIGHT_ILLUM_ON = LIGHT_ILLUM_ON_5;
                break;
            case 6:
                LIGHT_ILLUM_ON = LIGHT_ILLUM_ON_6;
                break;
            case 7:
                LIGHT_ILLUM_ON = LIGHT_ILLUM_ON_7;
                break;
        }
    }
    private void updateAimerOrIllum(String aimerOrIllum , byte[] value) {
        FileOutputStream fs = null;
        try {
            fs = new FileOutputStream(aimerOrIllum);
            fs.write(value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fs!=null) {
                try {
                    fs.close();
                } catch(IOException e) {
                    e.printStackTrace();
                }
                fs = null;
            }
        }
    }
    private synchronized void setAimerOrIllumMode(int mode) {
        switch (mode) {
            case 1:
                //updateAimerOrIllum(ILLUM_PATH , LIGHT_ILLUM_OFF);
                updateAimerOrIllum(AIMER_FLICKER_PATH , LIGHT_AIMER_ON);
                break;
            case 2:
                updateAimerOrIllum(ILLUM_PATH , LIGHT_ILLUM_ON);
                //updateAimerOrIllum(AIMER_FLICKER_PATH , LIGHT_AIMER_OFF);
                break;
            case 3:
                updateAimerOrIllum(ILLUM_PATH , LIGHT_ILLUM_ON);
                updateAimerOrIllum(AIMER_FLICKER_PATH , LIGHT_AIMER_ON);
                break;
            case 0:
                updateAimerOrIllum(ILLUM_PATH , LIGHT_ILLUM_OFF);
                updateAimerOrIllum(AIMER_FLICKER_PATH , LIGHT_AIMER_OFF);
                break;
        }
    }
    /**
     * Method used to connect to the camera engine and initialize the API.
     *激活绑定设备wifi mac地址
     */
    private boolean connectDecoderLibrary() {
        final boolean enable = LicenseHelper.setWifiWnable(mContext,true);
        if(enable == false) {
            File wifiAddress = new File("sys/class/net/wlan0/address");
            for(int i = 0; i < 8; i++) {
                try{
                    Thread.sleep(100);
                } catch (Exception e){}
                if(wifiAddress.exists()) {
                    Log.d(TAG," getwifiAddress sleep " + i);
                    break;
                }
            }
        }
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //接口有访问远程服务器动作，避免服务器或网络异常需超时才退出，造成ANR
                    //if(BarcodeReader.activateLicense(ActivationManager.Activation_DIR) != 0){
                    if(ActivationManager.activateAPIWithLocalFile() == false){
                        activateDecoder = false;
                    } else {
                        activateDecoder = true;
                        if(!syncLicenseStrore) {
                            syncLicenseStrore(true);
                            syncLicenseStrore = true;
                        }
                    }
                    if(enable == false)
                        LicenseHelper.setWifiWnable(mContext, enable);
                }
            }).start();
            for (int i = 0; i < 2; i++) {
                Thread.sleep(100);
                if (activateDecoder) {
                    Log.d(TAG, " activateDecoder sleep " + i);
                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        openCameraId = ScanUtil.scannerID();
        openDriver(openCameraId);
        mBarcodeReader = BarcodeReader.open(-1, mContext);
        if(mBarcodeReader != null) {
            byte[] version = new byte[256];
            mBarcodeReader.reportDecoderVersion(version);
            decoderVersion = (new String(version)).trim();
            Log.d(TAG, "version " + decoderVersion);
            state = State.PREVIEW;
            preDecodeData = null;
            g_nMultiReadResultCount = 0;
        } else {
            return false;
        }
        if (mHandler != null && workDecodeTimeOut) {
            mHandler.removeMessages(BCRDR_MSG_TIMER_PREVIEW);
            mHandler.sendEmptyMessageDelayed(BCRDR_MSG_TIMER_PREVIEW, CONTINUOUS_MODE == 4 ? InactivityTimer__DURATION_CONTINUOUS_MODE_MS:InactivityTimer__DURATION_MS);
            workPreviewTime = System.currentTimeMillis();
        }
        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            setDecodeParameter(INTERNAL_PROPERTY_INDEX[i]);
        }
        setDecodeParameter(PropertyID.DEC_EachImageAttempt_TIME);
        setDecodeParameter(PropertyID.DEC_DECODE_DELAY);
        setDecodeParameter(PropertyID.POSTAL_GROUP_TYPE_ENABLE);
        setDecodeParameter(PropertyID.DEC_DECODE_DEBUG_MODE);
        return true;
    }
    public synchronized void openDriver(int cameraId) {
        state = State.DONE;
        if(mCameraManager != null && mCameraManager.isOpen()) {
            return;
        }
        try {
            mCameraManager = CameraProviderManager.sharedObject(mContext);
            mCameraManager.setPreviewResolution(CameraProviderManager.Resolution.Resolution_1280x720);
            mCameraManager.initializeCamera(cameraId);
            Log.d(TAG, "openDriver end ===================");
        } catch (Exception e) {
            e.printStackTrace();
            mCameraManager = null;
        }
    }
    public synchronized boolean isOpen() {
        return (mCameraManager != null && mCameraManager.isOpen());
    }

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        try {
            setAimerOrIllumMode(0);
            if (mCameraManager != null) {
                mCameraManager.stopDecoding();
                mCameraManager.stopPreview();
                mCameraManager.closeDriver();
            }
            if(mBarcodeReader != null) {
                mBarcodeReader.release();
            }
        } catch (java.lang.RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCameraManager = null;
    }

    public synchronized void startCameraDecode() {
        if (mCameraManager != null && isOpen()) {
            if(AimerIllumMode > 0) {
                setAimerOrIllumMode(AimerIllumMode);
            }
            if(delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            mCameraManager.startDecoding(delayDecode);
        } else {
            Log.d(TAG, "reopen camera");
            if(workDecodeTimeOut) {
                closeDriver();
                connectDecoderLibrary();
                if(AimerIllumMode > 0) {
                    setAimerOrIllumMode(AimerIllumMode);
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(BCRDR_MSG_DECODE_REPREVIEW);
                }
            }
        }
    }

    public synchronized void stopCameraDecode() {
        //if(AimerIllumMode > 0) {
        setAimerOrIllumMode(0);
        //}
        if (mCameraManager != null) {
            mCameraManager.stopDecoding();
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startCameraPreview() {
        if (mCameraManager != null) {
            mCameraManager.startPreview();
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopCameraPreview() {
        if (mCameraManager != null) {
            mCameraManager.stopPreview();
        }
    }

    public synchronized void setDecodeHandler(Handler handler) {
        if (mCameraManager != null) {
            mCameraManager.setDecodeHandler(handler);
        }
    }
    @Override
    public void setDefaults() {
        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            setDecodeParameter(INTERNAL_PROPERTY_INDEX[i]);
        }
        setDecodeParameter(PropertyID.DEC_EachImageAttempt_TIME);
        setDecodeParameter(PropertyID.DEC_DECODE_DELAY);
        setDecodeParameter(PropertyID.POSTAL_GROUP_TYPE_ENABLE);
        setDecodeParameter(PropertyID.DEC_DECODE_DEBUG_MODE);
    }
    @Override
    public boolean open() {
        synchronized (mHandler) {
            mHandler.removeMessages(BCRDR_MSG_INIT);
            Message m = Message.obtain(mHandler, BCRDR_MSG_INIT);
            mHandler.sendMessage(m);
        }
        return true;
    }

    @Override
    public void close() {
        Log.d(TAG, "close " );
        synchronized (mHandler) {
            if(state == State.DECODING && mCameraManager != null) {
                mCameraManager.stopDecoding();
            }
            setAimerOrIllumMode(0);
            mHandler.removeMessages(BCRDR_MSG_DECODE_TIMEOUT);
            mHandler.removeMessages(BCRDR_MSG_FRAME_ERROR);
            mHandler.removeMessages(BCRDR_MSG_RELEASE);
            mHandler.removeMessages(BCRDR_MSG_TIMER_PREVIEW);
            Message m = Message.obtain(mHandler, BCRDR_MSG_RELEASE);
            mHandler.sendMessage(m);
            preDecodeData = null;
            g_nMultiReadResultCount = 0;
        }
    }
    /*
    @Override
    public boolean open() {
        synchronized (mHandler) {
            if(state != State.DONE) {
                Log.d(TAG, "open State=" + state);
                return false;
            }
            return connectDecoderLibrary();
        }
    }

    @Override
    public void close() {
        Log.d(TAG, "close ");
        synchronized (mHandler) {
            if(state == State.DECODING && mCameraManager != null) {
                mCameraManager.stopDecoding();
            }
            state = State.DONE;
            mHandler.removeMessages(BCRDR_MSG_DECODE_TIMEOUT);
            mHandler.removeMessages(BCRDR_MSG_FRAME_ERROR);
            mHandler.removeMessages(BCRDR_MSG_RELEASE);
            mHandler.removeMessages(BCRDR_MSG_TIMER_PREVIEW);
            closeDriver();
        }
        preDecodeData = null;
        g_nMultiReadResultCount = 0;
    }*/

    @Override
    public void startDecode(int timeout) {
        synchronized (mHandler) {
            CONTINUOUS_MODE = mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES);
            /*Message m = Message.obtain(mHandler, BCRDR_MSG_DECODE);
            m.arg1 = timeout;
            mHandler.sendMessage(m);*/
            workPreviewTime = System.currentTimeMillis();
            if(state == State.DECODING) {
                //restartPreviewAndDecode();
                Log.d(TAG, "state == State.DECODING");
                return;
            }
            g_nMultiReadResultCount = 0;
            preDecodeData = null;
            startCameraDecode();
            state = State.DECODING;
            if(CONTINUOUS_MODE != 4) {
                mHandler.removeMessages(BCRDR_MSG_DECODE_TIMEOUT);
                mHandler.sendEmptyMessageDelayed(BCRDR_MSG_DECODE_TIMEOUT, timeout > 0 ? timeout: 5000);
            }
        }
    }

    @Override
    public void stopDecode() {
        Log.d(TAG, "stopDecode state=" +state );
        workPreviewTime = System.currentTimeMillis();
        synchronized (mHandler) {
            mHandler.removeMessages(BCRDR_MSG_DECODE_TIMEOUT);
            mHandler.removeMessages(BCRDR_MSG_FRAME_ERROR);
            /*mHandler.removeMessages(BCRDR_MSG_DECODE_CANCELED);
            Message m = Message.obtain(mHandler, BCRDR_MSG_DECODE_CANCELED);
            mHandler.sendMessage(m);*/
            /*if(mBarcodeReader != null && state == State.DECODING) {
                mBarcodeReader.stopDecode();
            }*/
            workPreviewTime = System.currentTimeMillis();
            state = State.DONE;
            stopCameraDecode();
            g_nMultiReadResultCount = 0;
        }
    }

    @Override
    public void openPhoneMode() {

    }

    @Override
    public void closePhoneMode() {

    }

    @Override
    public int setProperties(SparseArray<Integer> property) {
        if (property != null) {
            int size = property.size();
            Log.d(TAG, "setProperties property size= " + size);
            for (int i = 0; i < size; i++) {
                int keyForIndex = property.keyAt(i);
                setDecodeParameter(keyForIndex);
            }
        }
        return 0;
    }

    @Override
    protected void release() {
        Log.d(TAG, "release");
        synchronized (mHandler) {
            mHandler.removeMessages(BCRDR_MSG_TIMER_PREVIEW);
            closeDriver();
            state = State.DONE;
        }
    }

    @Override
    public boolean lockHwTriggler(boolean lock) {
        return false;
    }
    private void setDecodeParameter(int property) {
        int SDSProperty = 0;
        int val = 0;
        switch (property) {
            case PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL:
                //delayDecode = property.get(keyForIndex);
                Log.d(TAG, "setProperties property delayDecode= " + delayDecode);
                break;
            case PropertyID.DEC_2D_LIGHTS_MODE:
                val = mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE);
                if(mBarcodeReader != null)
                    mBarcodeReader.setParameter(PropertyID.DEC_2D_LIGHTS_MODE, val);
                break;
            case PropertyID.DEC_ILLUM_POWER_LEVEL:
                int level = mScanService.getPropertyInt(PropertyID.DEC_ILLUM_POWER_LEVEL);
                Log.d(TAG, "setProperties property level= " + level);
                //updateLightGrade(level);
                if(mBarcodeReader != null)
                    mBarcodeReader.setParameter(PropertyID.DEC_ILLUM_POWER_LEVEL, level);
                break;
            case PropertyID.DEC_DECODE_DELAY:
                delayMillis = mScanService.getPropertyInt(PropertyID.DEC_DECODE_DELAY);
                Log.d(TAG, "setProperties property delay= " + delayMillis);
                if(mBarcodeReader != null)
                    mBarcodeReader.setParameter(PropertyID.DEC_DECODE_DELAY, (int)delayMillis);
                break;
            case PropertyID.DEC_DECODE_DEBUG_MODE:
                saveMode = mScanService.getPropertyInt(PropertyID.DEC_DECODE_DEBUG_MODE);
                Log.d(TAG, "setProperties property saveMode= " + saveMode);
                if(mBarcodeReader != null)
                    mBarcodeReader.setParameter(PropertyID.DEC_DECODE_DEBUG_MODE, saveMode);
                break;
            case PropertyID.DEC_Multiple_Decode_INTERVAL:
                intervalTime = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_INTERVAL);
                if(mBarcodeReader != null) {
                    mBarcodeReader.setParameter(PropertyID.DEC_Multiple_Decode_INTERVAL, intervalTime);
                }
                break;
            case PropertyID.DEC_MaxMultiRead_COUNT:
            case PropertyID.DEC_Multiple_Decode_MODE:
                g_nMaxMultiReadCount = mScanService.getPropertyInt(PropertyID.DEC_MaxMultiRead_COUNT);
                if(g_nMaxMultiReadCount <= 0) {
                    g_nMaxMultiReadCount = 1;
                }
                g_nMaxMultiReadCount = 1;//暂时
                waitMultipleMode = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_MODE);
                if(waitMultipleMode > 0) {
                    mBarcodeReader.setMultiReadCount(g_nMaxMultiReadCount);
                } else {
                    mBarcodeReader.setMultiReadCount(1);
                }
                break;
            case PropertyID.DEC_Multiple_Decode_TIMEOUT:
                try {
                    waitMultipleTiemOut = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_TIMEOUT);
                    if(waitMultipleTiemOut <= 50) {
                        waitMultipleTiemOut = 10 * 1000;
                    }
                    Log.d(TAG, "setProperties property g_waitMultipleTiemOut= " + waitMultipleTiemOut);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                }
                break;
            case PropertyID.DEC_EachImageAttempt_TIME:
                try {
                    int attemptLimitTime = mScanService.getPropertyInt(PropertyID.DEC_EachImageAttempt_TIME);
                    if(attemptLimitTime <= 30) {
                        attemptLimitTime = 125;
                    }
                    mBarcodeReader.setDecodeAttemptLimit(attemptLimitTime);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                }
                break;
            case PropertyID.AZTEC_ENABLE:
            case PropertyID.AZTEC_INVERSE:
                /*The property value is a bit field defined as follows:
                b0: Enable normal video Aztec decoding
                b1: Enable inverse video Aztec decoding
                b2: Enable Compact Aztec Code decoding
                b3: Enable Full-Size Aztec Code decoding*/
                SDSProperty = SDSProperties.SD_PROP_AZ_ENABLED;
                int aztec = mScanService.getPropertyInt(PropertyID.AZTEC_ENABLE);//1
                int inverse = mScanService.getPropertyInt(PropertyID.AZTEC_INVERSE);//2
                if(aztec == 1 && inverse == 1) {
                    val = 11;
                } else{
                    if(aztec == 1)
                        val += 1;
                    if(inverse == 1)
                        val += 2;
                }
                break;
            case PropertyID.UPCA_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_UPC_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.UPCA_ENABLE);
                break;
            case PropertyID.UPCE_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_UPC_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.UPCE_ENABLE);
                break;
            case PropertyID.UPCE1_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_UPC_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.UPCE1_ENABLE);
                break;
            case PropertyID.EAN13_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_UPC_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.EAN13_ENABLE);
                break;
            case PropertyID.EAN8_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_UPC_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.EAN8_ENABLE);
                break;
            case PropertyID.EAN_EXT_ENABLE_2_5_DIGIT:
                SDSProperty = SDSProperties.SD_PROP_UPC_SUPPLEMENTALS;
                val = mScanService.getPropertyInt(PropertyID.EAN_EXT_ENABLE_2_5_DIGIT);
                break;
            case PropertyID.CODE39_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_C39_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.CODE39_ENABLE);
                break;
            case PropertyID.CODE39_FULL_ASCII:
                SDSProperty = SDSProperties.SD_PROP_C39_FULL_ASCII;
                val = mScanService.getPropertyInt(PropertyID.CODE39_FULL_ASCII);
                break;
            case PropertyID.CODE39_ENABLE_CHECK:
                SDSProperty = SDSProperties.SD_PROP_C39_CHECKSUM;
                val = mScanService.getPropertyInt(PropertyID.CODE39_ENABLE_CHECK);
                break;
            case PropertyID.CODE128_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_C128_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.CODE128_ENABLE);
                break;
            case PropertyID.I25_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_I25_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.I25_ENABLE);
                break;
            case PropertyID.I25_ENABLE_CHECK:
                SDSProperty = SDSProperties.SD_PROP_I25_CHECKSUM;
                val = mScanService.getPropertyInt(PropertyID.I25_ENABLE_CHECK);
                break;
            case PropertyID.GS1_14_ENABLE:
            case PropertyID.GS1_EXP_ENABLE:
            case PropertyID.GS1_LIMIT_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_RSS_ENABLED;
                /*The property value is a bit field defined as follows:
                    b0: Enable GS1 Databar Expanded decoding.
                    b1: Enable GS1 Databar Expanded Stacked decoding.
                    b2: Enable GS1 Databar Limited decoding.
                    b3: Enable GS1 Databar Omnidirectional and GS1 Databar truncated decoding.
                    b4: Enable GS1 Databar Stacked Omnidirectional and GS1 Databar Stacked decoding.*/
                int gs1_trun = mScanService.getPropertyInt(PropertyID.GS1_14_ENABLE);//8//10
                int exp_val = mScanService.getPropertyInt(PropertyID.GS1_EXP_ENABLE);//1
                int limit_val = mScanService.getPropertyInt(PropertyID.GS1_LIMIT_ENABLE);//4 0010
                if(gs1_trun == 1 && exp_val == 1 && limit_val == 1) {
                    val = 0x1F;//31
                } else{
                    if(gs1_trun == 1)
                        val += 8;
                    if(exp_val == 1)
                        val += 1;
                    if(limit_val == 1)
                        val += 4;
                }
                break;
            case PropertyID.MSI_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_MSIP_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.MSI_ENABLE);
                break;
            case PropertyID.MSI_CHECK_2_MOD_11:
                SDSProperty = SDSProperties.SD_PROP_MSIP_CHECKSUM;
                val = mScanService.getPropertyInt(PropertyID.MSI_CHECK_2_MOD_11);
                break;
            case PropertyID.PDF417_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_PDF_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.PDF417_ENABLE);
                break;
            case PropertyID.MICROPDF417_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_MICROPDF_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.MICROPDF417_ENABLE);
                break;
            case PropertyID.MAXICODE_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_MC_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.MAXICODE_ENABLE);
                if(val == 1) {
                    val = 0x3f;
                }
                break;
            case PropertyID.QRCODE_ENABLE:
            case PropertyID.QRCODE_INVERSE:
            case PropertyID.MICROQRCODE_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_QR_ENABLED;
                int qr = mScanService.getPropertyInt(PropertyID.QRCODE_ENABLE);//1
                int qrInverse = mScanService.getPropertyInt(PropertyID.QRCODE_INVERSE);//2/3
                int micqr = mScanService.getPropertyInt(PropertyID.MICROQRCODE_ENABLE);//2/3
                if(qr == 1 && qrInverse == 0) {
                    val = 1;
                } else if(qr == 1 && qrInverse > 0) {
                    val = 2;
                } else {
                    val = 0;
                }
                /*if(qr == 1 && qrInverse == 0) {
                    if(micqr == 1) {
                        val = 6;
                    }else {
                        val = 1;
                    }
                } else if(qr == 1 && qrInverse > 0) {
                    if(micqr == 1) {
                        val = 8;
                    }else {
                        val = 2;
                    }
                } else {
                    if(micqr == 1) {
                        val = 6;
                    }else {
                        val = 0;
                    }
                }*/
                break;
            case PropertyID.DATAMATRIX_ENABLE:
            case PropertyID.DATAMATRIX_INVERSE:
                SDSProperty = SDSProperties.SD_PROP_DM_ENABLED;
                int dm = mScanService.getPropertyInt(PropertyID.DATAMATRIX_ENABLE);//1
                int dmInverse = mScanService.getPropertyInt(PropertyID.DATAMATRIX_INVERSE);//2/3
                if(dm == 1 && dmInverse == 0) {
                    val = 1;
                } else if(dm == 1 && dmInverse == 1) {
                    val = 2;
                } else if(dm == 1 && dmInverse == 2) {
                    val = 3;
                } else {
                    val = 0;
                }
                break;
            case PropertyID.HANXIN_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_HX_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.HANXIN_ENABLE);
                break;
            case PropertyID.M25_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_M25_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.HANXIN_ENABLE);
                break;
            case PropertyID.TRIOPTIC_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_TP_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.TRIOPTIC_ENABLE);
                break;
            case PropertyID.D25_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_S25_2SS_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.D25_ENABLE);
                break;
            case PropertyID.CODE11_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_C11_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.CODE11_ENABLE);
                break;
            case PropertyID.CODE11_ENABLE_CHECK:
            case PropertyID.CODE11_SEND_CHECK:
                SDSProperty = SDSProperties.SD_PROP_C11_CHECKSUM;
                //0 two check digits;1 one check; 2 two check and stripped form result data; 3 one check and stripped form result data
                boolean enableCK = mScanService.getPropertyInt(PropertyID.CODE11_ENABLE_CHECK) == 1;
                if(enableCK) {
                    val = mScanService.getPropertyInt(PropertyID.CODE11_SEND_CHECK);
                }
                break;
            case PropertyID.CODE93_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_C93_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.CODE93_ENABLE);
                break;
            case PropertyID.DOTCODE_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_DOTCODE_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.DOTCODE_ENABLE);
                break;
            case PropertyID.DEC_OCR_MODE:
            case PropertyID.DEC_OCR_TEMPLATE:
            case PropertyID.DEC_OCR_USER_TEMPLATE: {
                setOcrSettings();
                break;
            }
            case PropertyID.C25_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_HK25_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.C25_ENABLE);
                break;
            case PropertyID.US_POSTNET_ENABLE:
            case PropertyID.US_PLANET_ENABLE:
            case PropertyID.US_POSTAL_SEND_CHECK:
            case PropertyID.USPS_4STATE_ENABLE:
            case PropertyID.UPU_FICS_ENABLE:
            case PropertyID.ROYAL_MAIL_ENABLE:
            case PropertyID.ROYAL_MAIL_SEND_CHECK:
            case PropertyID.AUSTRALIAN_POST_ENABLE:
            case PropertyID.KIX_CODE_ENABLE:
            case PropertyID.JAPANESE_POST_ENABLE:
                SDSProperty = 0;
                disablePostalSymbologies();
                break;
            case PropertyID.KOREA_POST_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_KP_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.KOREA_POST_ENABLE);
                break;
            default:
                break;
        }
        if(SDSProperty != 0 && mBarcodeReader != null) {
            mBarcodeReader.setParameter(SDSProperty, val);
        }
    }
    private void disablePostalSymbologies(){
        if(mBarcodeReader != null) {
            int SDSProperty = SDSProperties.SD_PROP_POSTAL_ENABLED;
            int PLANET = mScanService.getPropertyInt(PropertyID.US_PLANET_ENABLE);
            int POSTNET = mScanService.getPropertyInt(PropertyID.US_POSTNET_ENABLE);
            int UPS4 = mScanService.getPropertyInt(PropertyID.USPS_4STATE_ENABLE);
            int UPU_FICS = mScanService.getPropertyInt(PropertyID.UPU_FICS_ENABLE);
            int ROYAL = mScanService.getPropertyInt(PropertyID.ROYAL_MAIL_ENABLE);
            int AUSTRALIAN = mScanService.getPropertyInt(PropertyID.AUSTRALIAN_POST_ENABLE);
            int KIX = mScanService.getPropertyInt(PropertyID.AUSTRALIAN_POST_ENABLE);
            int JAPANESE = mScanService.getPropertyInt(PropertyID.AUSTRALIAN_POST_ENABLE);
            if(PLANET == 0 && POSTNET == 0 &&UPS4 == 0 &&UPU_FICS == 0 &&AUSTRALIAN == 0 &&KIX == 0 &&ROYAL == 0 &&JAPANESE == 0) {
                mBarcodeReader.setParameter(SDSProperty, 0);
            } else {
                if(PLANET == 1 && POSTNET ==1) {
                    mBarcodeReader.setParameter(SDSProperty, 0x2081);
                } else if(PLANET == 1) {
                    mBarcodeReader.setParameter(SDSProperty, General.SD_CONST_PL);
                } else if(POSTNET == 1) {
                    mBarcodeReader.setParameter(SDSProperty, General.SD_CONST_PN);
                }
                if(UPS4 == 1) {
                    mBarcodeReader.setParameter(SDSProperty, General.SD_CONST_UPU);
                }
                if(UPU_FICS == 1) {
                    mBarcodeReader.setParameter(SDSProperty, General.SD_CONST_USPS4CB);
                }
                if(ROYAL == 1) {
                    mBarcodeReader.setParameter(SDSProperty, General.SD_CONST_RM);
                }
                if(AUSTRALIAN == 1) {
                    mBarcodeReader.setParameter(SDSProperty, General.SD_CONST_AP);
                }
                if(KIX == 1) {
                    mBarcodeReader.setParameter(SDSProperty, General.SD_CONST_RM + 1);
                }
                if(JAPANESE == 1) {
                    mBarcodeReader.setParameter(SDSProperty, General.SD_CONST_JP);
                }
                //暂时开启这几种postal
                mBarcodeReader.setParameter(SDSProperty, General.SD_CONST_PL + General.SD_CONST_PN +General.SD_CONST_USPS4CB + General.SD_CONST_UPU+1);
            }
        }
    }
    /**
     * Sets the OCR settings based on user preferences
     *The property value is a bit field defined as follows:
     * b0: User
     * b1: Passport
     * b2: ISBN
     * b3: Price Field
     * b4: MICR E-13B
     enable theUser template along with ISBN
     SD_Set(Handle, SD_PROP_OCR_ACTIVE_TEMPLATES, (void *)(1+4));
     *
     */
    private void setOcrSettings(){
        Log.d(TAG, "SetOcrSettings++");
        if(mBarcodeReader != null) {
            int ocr_mode = 0;
            int ocr_template = 0;
            byte[] ocr_user_defined_template;
            ocr_mode = mScanService.getPropertyInt(PropertyID.DEC_OCR_MODE);
            ocr_template = mScanService.getPropertyInt(PropertyID.DEC_OCR_TEMPLATE);
            String userDefinedTemplate = mScanService.getPropertyString(PropertyID.DEC_OCR_USER_TEMPLATE);
            Log.d(TAG, "SetOcrSettings++ ocr_mode " + ocr_mode);
            if(ocr_mode > 0) {
                mBarcodeReader.setOCRMode(ocr_mode);
                if(ocr_template == 0) ocr_template = OCRActiveTemplate.USER;
                else if(ocr_template == 1) ocr_template = OCRActiveTemplate.PASSPORT;
                else if(ocr_template == 2) ocr_template = OCRActiveTemplate.ISBN;
                else if(ocr_template == 3) ocr_template = OCRActiveTemplate.PRICE_FIELD;
                else if(ocr_template == 4) ocr_template = OCRActiveTemplate.MICR;//MICRE13B
                Log.d(TAG, "SetOcrSettings++ ocr_template " + ocr_template);
                mBarcodeReader.setOCRTemplates(ocr_template);
                //default Template "13777777770" to bytearrary  01 03 07 07 07 07 00
                //1,2,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,8,8,0
                if(ocr_template == OCRActiveTemplate.USER) {
                    try{
                        //userDefinedTemplate = "1.3777778E10";
                        if(userDefinedTemplate != null && !userDefinedTemplate.equals("")) {
                            ocr_user_defined_template = userDefinedTemplate.getBytes();
                            //char[] templateChar = userDefinedTemplate.toCharArray();
                            for( int i = 0; i < ocr_user_defined_template.length; i++) {
                                if(ocr_user_defined_template[i] >= 48)
                                    ocr_user_defined_template[i] = (byte)(ocr_user_defined_template[i] - 48);
                            }
                            mBarcodeReader.setOCRUserTemplate(ocr_user_defined_template);
                        }
                    } catch(Exception e) {
                    }
                }
            } else {
                mBarcodeReader.setParameter(SDSProperties.SD_PROP_OCR_ENABLED, 0);
            }
            Log.d(TAG, "SetOcrSettings--");
        }
    }

    /*void getOcrSettings() {
        int default_ocr_mode = -1;
        int default_template = -1;
        byte[] default_ocr_user_template = null;
        String default_ocr_user_template_string = null;
        try {
            default_ocr_mode = m_decDecoder.getOCRMode();
            default_template = m_decDecoder.getOCRTemplates();
            default_ocr_user_template = m_decDecoder.getOCRUserTemplate();

            for (int i = 0; i < default_ocr_user_template.length; i++)
                Log.d(TAG, "default_ocr_user_template[" + i + "] = " + default_ocr_user_template[i]);

            Log.d(TAG, "default_ocr_mode = " + default_ocr_mode);
            Log.d(TAG, "default_template = " + default_template);

            // Convert 'default_ocr_user_template_string' to printable string...
            StringBuilder sb = new StringBuilder();
            for (byte b : default_ocr_user_template) {
                sb.append(String.format("%x", b & 0xff));
            }
            Log.d(TAG, "sb = " + sb);
            default_ocr_user_template_string = sb.toString();

            Log.d(TAG, "default_ocr_user_template_string = " + default_ocr_user_template_string);
        } catch (DecoderException e) {
            HandleDecoderException(e);
        }
    }*/
    private final int[] VALUE_PARAM_INDEX = {
            RESERVED_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            SPECIAL_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.CODE39_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.TRIOPTIC_ENABLE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            N6603Scanner.N6603ParamIndex.C25_ENABLE,
            N6603Scanner.N6603ParamIndex.D25_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.M25_ENABLE,
            N6603Scanner.N6603ParamIndex.CODE11_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.I25_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.CODABAR_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.CODE93_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.CODE128_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.CODE_ISBT_128,
            N6603Scanner.N6603ParamIndex.CODE128_GS1_ENABLE,
            N6603Scanner.N6603ParamIndex.UPCA_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.UPCE_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            SPECIAL_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.EAN13_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.EAN8_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.MSI_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.GS1_14_ENABLE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.GS1_LIMIT_ENABLE,
            N6603Scanner.N6603ParamIndex.GS1_EXP_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            N6603Scanner.N6603ParamIndex.PDF417_ENABLE,
            N6603Scanner.N6603ParamIndex.MICROPDF417_ENABLE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            SPECIAL_VALUE,
            N6603Scanner.N6603ParamIndex.HANXIN_ENABLE,
            SPECIAL_VALUE,
            N6603Scanner.N6603ParamIndex.DATAMATRIX_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.MAXICODE_ENABLE,
            N6603Scanner.N6603ParamIndex.QRCODE_ENABLE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.MICROQRCODE_ENABLE,
            N6603Scanner.N6603ParamIndex.AZTEC_ENABLE,
            RESERVED_VALUE,
            N6603Scanner.N6603ParamIndex.DEC_2D_LIGHTS_MODE,
            N6603Scanner.N6603ParamIndex.DEC_2D_CENTERING_ENABLE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            RESERVED_VALUE,
            DEC_ILLUM_POWER_LEVEL,
            DEC_PICKLIST_AIM_MODE,
            DEC_PICKLIST_AIM_DELAY,
            DEC_MaxMultiRead_COUNT,
            DEC_Multiple_Decode_TIMEOUT,
            DEC_Multiple_Decode_MODE,
            DEC_OCR_MODE,
            DEC_OCR_TEMPLATE,
    };
}
