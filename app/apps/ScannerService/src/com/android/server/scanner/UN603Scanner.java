package com.android.server.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.LicenseHelper;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.text.TextUtils;
import android.os.UserHandle;
import com.android.server.ScanServiceWrapper;
import android.device.scanner.configuration.Symbology;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import com.ubx.decoder.DecodeWindowMode;
import com.ubx.decoder.General;
import com.ubx.decoder.CodeId;
import com.ubx.decoder.license.ActivationManager;
import com.ubx.decoder.BarcodeReader;
import com.ubx.decoder.GS1DataParser;
import com.ubx.decoder.HSMDecodeResult;
import com.ubx.decoder.OCRActiveTemplate;
import com.ubx.decoder.SDSProperties;
import com.ubx.propertyparser.DataParser;
import com.ubx.propertyparser.Property;
import com.android.usettings.R;
/**
 * Created by xjf on 19-07-04.
 */

public class UN603Scanner extends Scanner implements BarcodeReader.DecodeCallback,BarcodeReader.DecoderListener{
    private static String TAG = "UN603Scanner";
    private CaptureHandler mHandler;
    //进入休眠模式时延Sleep mode delay
    private static long InactivityTimer_DURATION_MS = 15 * 1000L;
    private static final long InactivityTimer__DURATION_CONTINUOUS_MODE_MS = 60 * 1000L;
    private long workPreviewTime = 0;
    private boolean workDecodeTimeOutSleep = true;
    int CONTINUOUS_MODE = 8;
    private int saveMode = SaveMode.NOTSAVE.ordinal();
    private State state;
    private int openCameraId = 2;
    private int delayMillis = 0;
    //skip_first_frame
    private int delayDecode = 1;
    boolean enableHSMDecoder = false;
    private BarcodeReader mBarcodeReader;
    private String decoderVersion = "";
    /**
    *连扫模式下，固定时间内过滤相同条码输出
    */
    private long waitRepeatStartTime = 0;
    private int ignoreRepeatMode = 0;
    private int waitRepeatTimeOut = 10*1000;
     //连扫模式读取多个条码后输出
    private int cotinuousMultiReadCount = 2;
    //连扫模式去重缓存条码数据
    private byte[] preDecodeData = null;
    //一次解多码模式
    private int multiDecodeMode = 0;
    //一次解多码模式下，解出固定个数后才输出
    private int fullReadMode = 1;
    private static boolean bWaitMultiple = true;	// flag for single or multiple decode
    private int g_nMultiReadResultCount = 0;		// For tracking # of multiread results
    private int g_nMaxMultiReadCount = 1;		// Maximum multiread count
    private List<DecodeData> decodeDataArray = new ArrayList<DecodeData>();
    private int transmitCodeID = 0;
    private int enableGroupSeperator = 0;
    
    private boolean activateDecoder = false;
    private int activateDecoderTimes = 0;
    private boolean hasLicense = true;
    private boolean syncLicenseStrore = false;
    private byte[] headOfByte = null;
    private byte[] bitmapByte = null;
    private int[] bounds=new int[10];
    private static final int MAX_FRAME_WIDTH = 1280;
    private static final int MAX_FRAME_HEIGHT = 720;
    //应用控制动态获取图片
    private static final String CAPTURE_IMAGE_ACTION = "action.scanner_capture_image";
    //应用控制扫描成功后直接发送图片
    private static final String ENABLE_BROADCAST_IMAGE_ACTION = "com.ubx.barcode.broadcast_image";
    private static String BROADCAST_IMAGE_ACTION = "scanner_capture_image_result";
    private static String BROADCAST_IMAGE_ACTION_EXTRAS = "bitmapBytes";
    private static String BROADCAST_IMAGE_ACTION_PCK = "";
    private static String BROADCAST_SCANNER_ACTION_CONFIG = "com.ubx.barcode.action_config";
    private static boolean enableSendBarcodeAndBitmap = false;
    private static boolean enableSendBitmap = true;
    private static boolean enableSendFAILEDBitmap = false;
    private static int rotateImage = 0;
    private int compressJpegQuality = 50;
    private long decodeStartTime;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(CAPTURE_IMAGE_ACTION.equals(intent.getAction())) {
                if(mHandler != null) {
                    Message m = Message.obtain(mHandler, MESSAGE_CAPTURE_IMAGE);
                    Bundle bundle = intent.getExtras();
                    m.obj = bundle;
                    mHandler.sendMessage(m);
                }
            } else if(ENABLE_BROADCAST_IMAGE_ACTION.equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                BROADCAST_IMAGE_ACTION = bundle.getString("imageAction", BROADCAST_IMAGE_ACTION);
                BROADCAST_IMAGE_ACTION_EXTRAS = bundle.getString("imageExtras", BROADCAST_IMAGE_ACTION_EXTRAS);
                rotateImage = bundle.getInt("rotate", 0);
                enableSendBarcodeAndBitmap = bundle.getBoolean("enableSendImage", false);
                compressJpegQuality = bundle.getInt("jpegQuality", 50);
                compressJpegQuality = compressJpegQuality >= 100 ? 90: compressJpegQuality;
            } else if(BROADCAST_SCANNER_ACTION_CONFIG.equals(intent.getAction())) {
                if (mBarcodeReader != null) {
                    try {
                        int property = intent.getIntExtra("property", 0);
                        int value = intent.getIntExtra("value", 0);
                        Log.d(TAG, "property=" + property + " value " + value);
                        if (property > 0) {
                            mBarcodeReader.setParameter(property, value);
                        }
                    } catch (Exception e) {
                    }
                }
            } else if("com.cainiao.scanner.saveimage".equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                rotateImage = bundle.getInt("rotate", 0);
                enableSendFAILEDBitmap = bundle.getBoolean("enable", true);
                enableSendBitmap = bundle.getBoolean("status", true);
            } else if("com.cainiao.scanner.lightness".equals(intent.getAction())) {
                Bundle bundle = intent.getExtras();
                int level = bundle.getInt("value", 7);
                if(mBarcodeReader != null && level > 0)
                    mBarcodeReader.setParameter(PropertyID.DEC_ILLUM_POWER_LEVEL, level);
            }
        }
    };
    //**ACTLIC|http://10.10.10.120:7070 online: **ACTLIC|**
    private boolean doLicenseActivation(byte[] data, int dataLength ) {
        try {
            if(data[0] == '*' && data[1] == '*' && data[2] == 'A' && data[3] == 'C'
                 && data[4] == 'T' && data[5] == 'L' && data[6] == 'I' && data[7] == 'C' && data[8] == '|') {
                    String serverUrl = new String(data, 0, dataLength);
                    String[] server = serverUrl.split("\\|");
                    if(server != null && server.length == 2) {
                        Intent intent = new Intent("com.ubx.scanner.LICENSE_ACTIVATION");
                        if("**".equals(server[1])) {
                            intent.putExtra("ServerUrl", "online");
                        } else {
                            intent.putExtra("ServerUrl", server[1]);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mScanService.getContext().startActivity(intent);
                        return true;
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    private void receivedMultipleData() {
        if(transmitCodeID > 0 || enableGroupSeperator == 1) {
            List<DecodeData> composeDecodeDataArray = new ArrayList<DecodeData>();
            for(DecodeData firstResult: decodeDataArray) {
                byte[] byteArraryData = firstResult.getBarcodeDataBytes();
                byte[] barcodeDataBytes = composeAISeparator(firstResult.getAIMCodeLetter(), firstResult.getAIMModifier(), firstResult.getCodeId(), byteArraryData , firstResult.getBarcodeDataLength());
                if(barcodeDataBytes != null) {
                    firstResult.setBarcodeDataBytes(barcodeDataBytes);
                    firstResult.setBarcodeDataLength(barcodeDataBytes.length);
                }
                composeDecodeDataArray.add(firstResult);
            }
            decodeDataArray.clear();
            receivedMultipleDecodedData(composeDecodeDataArray);
        } else {
            receivedMultipleDecodedData(decodeDataArray);
        }
    }
    //add AIM code identifiers and Group seperator
    private byte[] composeAISeparator(byte aimID, byte aimModifier, byte codeID, byte[] barcodeByteData , int dataLength) {
        try{
            byte[] aiSeparatorData = null;
            if(enableGroupSeperator == 1) {
                String sepChar = mScanService.getPropertyString(PropertyID.LABEL_FORMAT_SEPARATOR_CHAR);
                byte[] sepBytes = null;
                if (!TextUtils.isEmpty(sepChar)) {
                    sepBytes = sepChar.getBytes();
                }
                if((aimID == 'E' && (aimModifier == '0' || aimModifier == '4'))) {
                    //only EAN-13/EAN-8/UPC-A/UPC-E Composite
                    int prefixLen = GS1DataParser.ignoreUPCCompositeCode(codeID, dataLength);
                    if(prefixLen > 0) {
                        byte[] compositeByte = new byte[dataLength - prefixLen];
                        System.arraycopy(barcodeByteData, prefixLen, compositeByte, 0, compositeByte.length);
                        byte[] compositeByteData = GS1DataParser.addAISeparator(aimID, aimModifier, compositeByte, sepBytes);
                        if (compositeByteData != null && compositeByte.length != compositeByteData.length) {
                            aiSeparatorData = new byte[prefixLen + compositeByteData.length];
                            System.arraycopy(barcodeByteData, 0, aiSeparatorData, 0, prefixLen);
                            System.arraycopy(compositeByteData, 0, aiSeparatorData, prefixLen, compositeByteData.length);
                        }
                    }
                } else {
                    aiSeparatorData = GS1DataParser.addAISeparator(aimID, aimModifier, barcodeByteData, sepBytes);
                }
            }
            if (transmitCodeID > 0) {
                try {
                    byte[] byteArraryData;
                    if (transmitCodeID == 1) {
                        if (aiSeparatorData != null && aiSeparatorData.length > 0) {
                            byteArraryData = new byte[3 + aiSeparatorData.length];
                            System.arraycopy(aiSeparatorData, 0, byteArraryData, 3, aiSeparatorData.length);
                        } else {
                            byteArraryData = new byte[3 + barcodeByteData.length];
                            System.arraycopy(barcodeByteData, 0, byteArraryData, 3, barcodeByteData.length);
                        }
                        byteArraryData[0] = ']';
                        byteArraryData[1] = aimID;
                        byteArraryData[2] = aimModifier;
                    } else {
                        if (aiSeparatorData != null && aiSeparatorData.length > 0) {
                            byteArraryData = new byte[1 + aiSeparatorData.length];
                            System.arraycopy(aiSeparatorData, 0, byteArraryData, 1, aiSeparatorData.length);
                        } else {
                            byteArraryData = new byte[1 + barcodeByteData.length];
                            System.arraycopy(barcodeByteData, 0, byteArraryData, 1, barcodeByteData.length);
                        }
                        byteArraryData[0] = codeID;
                    }
                    return byteArraryData;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                return aiSeparatorData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    @Override
    public void onDecodeComplete(int event, HSMDecodeResult result, BarcodeReader reader) {
        workPreviewTime = System.currentTimeMillis();
        if(event == BarcodeReader.BCR_SUCCESS) {
            laserTriggerTime = workPreviewTime - decodeStartTime;
            decodeSessionTime = laserTriggerTime;
            if(CONTINUOUS_MODE != Triggering.CONTINUOUS.toInt()) {
                state = State.PREVIEW;
                if(result != null) {
                    if(result.getBarcodeBounds()!= null) {
                        System.arraycopy(result.getBarcodeBounds(), 0, bounds, 0, result.getBarcodeBounds().length >= 10 ? 10 : result.getBarcodeBounds().length);
                    }
                    if(saveMode == 5) {
                        final byte[] m_frameData = mBarcodeReader.getLastDecImage();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap modBm = createBitmap(m_frameData, 0);
                                Utils.saveImageAsPNG(modBm, "sdcard/temp/", "temp");
                                modBm.recycle();
                                modBm = null;
                            }
                        }).start();
                    }
                    byte codeId = result.getCodeId();
                    //int SymbologyId = OEMSymbologyId.getHSMSymbologyId(codeId);
                    aimCodeId[0] = ']';
                    aimCodeId[1] = result.getAIMCodeLetter();
                    aimCodeId[2] = result.getAIMModifier();
                    byte[] byteArraryData = result.getBarcodeDataBytes();
                    int dataLength = result.getBarcodeDataLength();
                    //decodeSessionTime = result.getDecodeTime();
                    //如果扫描到指定QR码/code128并且未激活过
                    if(/*activateDecoder == false && */(codeId == CodeId.CODE_ID_QRCODE || codeId == CodeId.CODE_ID_CODE128) && dataLength >= 9) {
                        //**ACTLIC|http://10.10.10.120:7070
                        boolean actLic = doLicenseActivation(byteArraryData, dataLength);
                        //Log.d(TAG, "doLicenseActivation " + actLic);
                        if(actLic) {
                            return;
                        }
                    }
                    if(transmitCodeID > 0 || enableGroupSeperator == 1) {
                        byte[] barcodeDataBytes = composeAISeparator(result.getAIMCodeLetter(), result.getAIMModifier(), codeId, byteArraryData , dataLength);
                        if(barcodeDataBytes != null) {
                            sendBroadcast(barcodeDataBytes, codeId, barcodeDataBytes.length);
                        } else {
                           sendBroadcast(byteArraryData, codeId, dataLength); 
                        }
                    } else {
                        sendBroadcast(byteArraryData, codeId, dataLength);
                    }
                    if(enableSendBarcodeAndBitmap) {
                        String barcode = new String(byteArraryData, 0, dataLength);
                        byte[] m_frameData = mBarcodeReader.getLastDecImage();
                        int[] bounds = result.getBarcodeBounds();
                        sendBarcodeAndBitmapBroadcast(barcode, m_frameData, bounds);
                    }
                }
            } else {
                if(ignoreRepeatMode == 2) {
                    if(g_nMultiReadResultCount > 0 && decodeDataArray.size() > 0) {
                        for(DecodeData firstResult: decodeDataArray) {
                            if(result.getBarcodeDataBytes() != null
                                && comparabytes(firstResult.getBarcodeDataBytes(), result.getBarcodeDataBytes(), result.getBarcodeDataLength())) {
                                //decodeDataArray.clear();
                                return;
                            }
                        }
                    }
                    g_nMultiReadResultCount++;
                    DecodeData data = new DecodeData();
                    data.setBarcodeDataBytes(result.getBarcodeDataBytes());
                    data.setBarcodeDataLength(result.getBarcodeDataLength());
                    data.setDecodeTime(result.getDecodeTime());
                    byte codeId = result.getCodeId();
                    int SymbologyId = OEMSymbologyId.getHSMSymbologyId(codeId);
                    data.setSymbologyId(SymbologyId);
                    data.setCodeId(codeId);
                    data.setAIMCodeLetter(result.getAIMCodeLetter());
                    data.setAIMModifier(result.getAIMModifier());
                    decodeDataArray.add(data);
                    if (g_nMultiReadResultCount >= g_nMaxMultiReadCount) {
                        Log.d(TAG, "MAX onMultiReadCallback " + g_nMultiReadResultCount);
                        //receivedMultipleDecodedData(decodeDataArray);
                        if(mHandler != null) {
                            mHandler.sendEmptyMessage(MESSAGE_CONTINUOUS_MULTI_DECODE_COMPLETE);
                        }
                    }
                } else {
                   if(ignoreRepeatMode == 1) {
                        if (preDecodeData != null && System.currentTimeMillis() - waitRepeatStartTime < waitRepeatTimeOut 
                            && comparabytes(result.getBarcodeDataBytes(), result.getBarcodeDataLength())) {
                            decodeStartTime = System.currentTimeMillis();
                            return;
                        }
                    }
                    //g_nMultiReadResultCount++;
                    preDecodeData = result.getBarcodeDataBytes();
                    byte codeId = result.getCodeId();
                    //int SymbologyId = OEMSymbologyId.getHSMSymbologyId(codeId);
                    aimCodeId[0] = ']';
                    aimCodeId[1] = result.getAIMCodeLetter();
                    aimCodeId[2] = result.getAIMModifier();
                    //decodeSessionTime = result.getDecodeTime();
                    if(transmitCodeID > 0 || enableGroupSeperator == 1) {
                        byte[] barcodeDataBytes = composeAISeparator(result.getAIMCodeLetter(), result.getAIMModifier(), codeId, preDecodeData , result.getBarcodeDataLength());
                        if(barcodeDataBytes != null) {
                            sendBroadcast(barcodeDataBytes, codeId, barcodeDataBytes.length);
                        } else {
                            sendBroadcast(preDecodeData, codeId, result.getBarcodeDataLength());
                        }
                    } else {
                        sendBroadcast(preDecodeData, codeId, result.getBarcodeDataLength());
                    }
                    decodeStartTime = waitRepeatStartTime = System.currentTimeMillis();
                }
            }
        } else if(BarcodeReader.DECODE_STATUS_MULTI_DEC_COUNT == event) {
            //解码超时，未读取固定个数，输出已解码数据
            Log.i(TAG, "onDecodeComplete multiDecodeMode=" +multiDecodeMode + " fullReadMode=" +  fullReadMode+ " decodeDataArray=" +  decodeDataArray.size());
            if(multiDecodeMode == 1 && fullReadMode == 0 && decodeDataArray.size() > 0) {
                //receivedMultipleDecodedData(decodeDataArray);
                if(mHandler != null) {
                    mHandler.sendEmptyMessage(MESSAGE_MULTI_DECODE_COMPLETE);
                }
            }
            state = State.PREVIEW;
        } else {
            state = State.PREVIEW;
            if(mHandler != null) {
                if(enableSendFAILEDBitmap && CONTINUOUS_MODE != Triggering.CONTINUOUS.toInt()) {
                    mHandler.sendEmptyMessage(MESSAGE_LAST_DEC_FAILED_IMAGE);
                }
            }
        }
    }

    @Override
    public void onEvent(int event, int info, byte[] data, BarcodeReader reader) {
        if(BarcodeReader.DECODE_STATUS_MULTI_DEC_COUNT == event) {
            //单张图片解码超时,未清空decodeDataArray
            if(g_nMultiReadResultCount > 0 && decodeDataArray.size() > 0) {
                g_nMultiReadResultCount = 0;
                decodeDataArray.clear();
            }
        }
    }
    /**
     * Handler for listener when a keep going callback occurs
     *
     * @return true to continue looking for decoded results, otherwise false.
     */
    @Override
    public boolean onKeepGoingCallback() {
        //Log.i(TAG, "onKeepGoingCallback");
        return state == State.DECODING;
    }

    /**
     * Handler for listener when a multiple decode result is available.
     *
     * @return true to continue looking for decoded results, otherwise false.
     */
    @Override
    public boolean onMultiReadCallback() {
        //Log.i(TAG, "onMultiReadCallback");
        //final long startdec = System.currentTimeMillis();
        final HSMDecodeResult firstResult = mBarcodeReader.getDecodeData();
        if (firstResult != null) {
            workPreviewTime = System.currentTimeMillis();
            if(multiDecodeMode > 0) {
                //单张图片解码超时,未清空decodeDataArray
                /*if(g_nMultiReadResultCount > 0 && decodeDataArray.size() > 0) {
                    for(DecodeData result: decodeDataArray) {
                        if(result.getBarcodeDataBytes() != null && comparabytes(firstResult.getBarcodeDataBytes(), result.getBarcodeDataBytes(), result.getBarcodeDataLength())) {
                            //decodeDataArray.clear();
                            return true;
                        }
                    }
                }*/
                g_nMultiReadResultCount++;
                DecodeData data = new DecodeData();
                data.setBarcodeDataBytes(firstResult.getBarcodeDataBytes());
                //if(firstResult.getBarcodeDataBytes() != null)
                //Log.d(TAG, "onMultiReadCallback " + (new String(firstResult.getBarcodeDataBytes())));
                data.setBarcodeDataLength(firstResult.getBarcodeDataLength());
                data.setDecodeTime(firstResult.getDecodeTime());
                byte codeId = firstResult.getCodeId();
                int SymbologyId = OEMSymbologyId.getHSMSymbologyId(codeId);
                data.setSymbologyId(SymbologyId);
                data.setCodeId(firstResult.getCodeId());
                data.setAIMCodeLetter(firstResult.getAIMCodeLetter());
                data.setAIMModifier(firstResult.getAIMModifier());
                decodeDataArray.add(data);
                if (g_nMultiReadResultCount >= g_nMaxMultiReadCount) {
                    Log.d(TAG, "MAX onMultiReadCallback " + g_nMultiReadResultCount);
                    //receivedMultipleDecodedData(decodeDataArray);
                    if(mHandler != null) {
                        mHandler.sendEmptyMessage(MESSAGE_MULTI_DECODE_COMPLETE);
                    }
                    state = State.PREVIEW;
                    return false;
                }
            } else {
                preDecodeData = firstResult.getBarcodeDataBytes();
                waitRepeatStartTime = System.currentTimeMillis();
                byte codeId = firstResult.getCodeId();
                //int symId = OEMSymbologyId.getHSMSymbologyId(codeId);
                aimCodeId[0] = ']';
                aimCodeId[1] = firstResult.getAIMCodeLetter();
                aimCodeId[2] = firstResult.getAIMModifier();
                sendBroadcast(preDecodeData, codeId, firstResult.getBarcodeDataLength());
                state = State.PREVIEW;
                return false;
            }
        }
        return true;
    }
    boolean comparabytes(byte[] preData, byte[] data, int length) {
        int i =0;
        if(length != preData.length) return false;
        for(i = 0; i < length && i < preData.length; i++) {
            if(preData[i] != data[i]) {
                return false;
            }
        }
        return true;
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
    private enum State {
        PREVIEW,
        DECODING,
        DONE
    }

    public UN603Scanner(ScanServiceWrapper scanService){
        mScannerType = ScannerFactory.TYPE_SE2030;
        mScanService = scanService;
        mContext = scanService.mContext;
        mHandler = new CaptureHandler(mContext.getMainLooper());
        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            mPropIndexHashMap.put(INTERNAL_PROPERTY_INDEX[i], 1);
        }
        syncLicenseStrore(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(CAPTURE_IMAGE_ACTION);
        filter.addAction(ENABLE_BROADCAST_IMAGE_ACTION);
        filter.addAction(BROADCAST_SCANNER_ACTION_CONFIG);
        if(Build.PWV_CUSTOM_CUSTOM.equals("XNYZ")) {
            BROADCAST_IMAGE_ACTION = "com.cainiao.scanner.image";
            BROADCAST_IMAGE_ACTION_EXTRAS = "jpegData";
            BROADCAST_IMAGE_ACTION_PCK = "com.xiniao.android.xnapp";
            rotateImage = 0;
            enableSendBarcodeAndBitmap = true;
            enableSendBitmap = true;
            enableSendFAILEDBitmap = true;
            filter.addAction("com.cainiao.scanner.saveimage");
            filter.addAction("com.cainiao.scanner.lightness");
        }
        mContext.registerReceiver(mReceiver, filter);
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
            if(activate) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ActivationManager.syncLicenseStrore(activate);
                    }
                }).start();
            } else {
                /*if(Build.PROJECT.equals("SQ53C")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
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
                            if(enable == false)
                            LicenseHelper.setWifiWnable(mContext, enable);
                        }
                    }).start();
                }*/
            }
        }catch(Exception e){
            e.printStackTrace();
        }
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
    public static final int MESSAGE_CAPTURE_IMAGE = 12;
    public static final int MESSAGE_MULTI_DECODE_COMPLETE = 13;
    public static final int MESSAGE_CONTINUOUS_MULTI_DECODE_COMPLETE = 14;
    public static final int MESSAGE_LAST_DEC_FAILED_IMAGE = 15;
    class CaptureHandler extends Handler {

        public CaptureHandler(Looper looper) {
            super(looper);
            state = State.DONE;
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case BCRDR_MSG_TIMER_PREVIEW:
                    long currentTime = System.currentTimeMillis();
                    Log.d(TAG, "timer_preview " + (currentTime - workPreviewTime));
                    if (currentTime - workPreviewTime >= (CONTINUOUS_MODE == 4 ? InactivityTimer__DURATION_CONTINUOUS_MODE_MS : InactivityTimer_DURATION_MS)) {
                        if(mBarcodeReader != null) {
                            mBarcodeReader.stopDecode();
                            mBarcodeReader.stopPreview();
                        }
                        workPreviewTime = 0;
                        state = State.PREVIEW;
                    } else {
                        workPreviewTime = System.currentTimeMillis();
                        sendEmptyMessageDelayed(BCRDR_MSG_TIMER_PREVIEW, CONTINUOUS_MODE == 4 ? InactivityTimer__DURATION_CONTINUOUS_MODE_MS : InactivityTimer_DURATION_MS);
                    }
                    break;
                case BCRDR_MSG_INIT:
                    connectDecoderLibrary();
                    break;
                case BCRDR_MSG_RELEASE:
                    disconnectDecoderLibrary();
                    break;
                case MESSAGE_CAPTURE_IMAGE:
                    Bundle bundle=(Bundle)message.obj;
                    int w = 0;
                    int h = 0;
                    int rotate = 0;
                    if(bundle != null) {
                        w = bundle.getInt("ImageWidth", 0);
                        h = bundle.getInt("ImageHeight", 0);
                        rotate = bundle.getInt("rotate", 0);
                        compressJpegQuality = bundle.getInt("jpegQuality", 50);
                        compressJpegQuality = compressJpegQuality >= 100 ? 90: compressJpegQuality;
                    }
                    captureImage(w, h, rotate);
                    break;
                case MESSAGE_MULTI_DECODE_COMPLETE:
                    receivedMultipleData();
                    break;
                case MESSAGE_CONTINUOUS_MULTI_DECODE_COMPLETE:
                    if(mBarcodeReader != null && state == State.DECODING) {
                        mBarcodeReader.stopDecode();
                    }
                    state = State.PREVIEW;
                    receivedMultipleData();
                    break;
                case MESSAGE_LAST_DEC_FAILED_IMAGE:
                    if(mBarcodeReader != null && enableSendBarcodeAndBitmap && enableSendBitmap) {
                        byte[] m_frameData = mBarcodeReader.getLastDecImage();
                        sendBarcodeAndBitmapBroadcast(null, m_frameData, null);
                    }
                    break;
            }
        }

    }
    private Bitmap createBitmap(byte[] m_frameData, int rotate) {
        try{
            if(headOfByte == null) {
                //InputStream localInputStream = new FileInputStream(new File("etc/bh_1280_720.bin"));;
                InputStream localInputStream =mContext.getResources().openRawResource(R.raw.bh_1280_720);
                headOfByte = Utils.readFileHead(localInputStream);
                if(headOfByte !=null) {
                    bitmapByte = new byte[headOfByte.length + MAX_FRAME_WIDTH * MAX_FRAME_HEIGHT];
                }
            }
            if(headOfByte != null) {
                if(m_frameData != null && m_frameData.length > 0 && bitmapByte != null) {
                    System.arraycopy(headOfByte, 0, bitmapByte, 0, headOfByte.length);
                    System.arraycopy(m_frameData, 0, bitmapByte, headOfByte.length, MAX_FRAME_WIDTH*MAX_FRAME_HEIGHT);
                    Bitmap yuvbitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(bitmapByte)).copy(Bitmap.Config.RGB_565, true);
                    if(yuvbitmap != null) {
                        Log.d(TAG, "createBitmap ");
                        Bitmap modBm = Bitmap.createBitmap(yuvbitmap.getWidth(),yuvbitmap.getHeight(),yuvbitmap.getConfig());
                        Matrix matrix = new Matrix();
                        Canvas canvas = new Canvas(modBm);
                        Paint paint = new Paint();
                        //绘制矩阵  Matrix主要用于对平面进行平移(Translate)，缩放(Scale)，旋转(Rotate)以及斜切(Skew)操作。
                        //matrix.setRotate(90,modBm.getWidth()/2,modBm.getHeight()/2);
                        //        matrix.setTranslate(20,20);
                        //镜子效果：
                        matrix.setScale(-1,1);//翻转
                        matrix.postTranslate(yuvbitmap.getWidth(),0);
                        canvas.drawBitmap(yuvbitmap, matrix,paint);
                        yuvbitmap.recycle();
                        yuvbitmap = null;
                         /*旋转-90*/
                        if(rotate > 0 && rotate <= 270) {
                            matrix.reset();
                            matrix.setRotate(rotate);
                            // 重新绘制Bitmap
                            Bitmap rotateBm = Bitmap.createBitmap(modBm, 0, 0, modBm.getWidth(), modBm.getHeight(), matrix, true);
                            modBm.recycle();
                            modBm = null;
                            return rotateBm;
                        } else {
                            return modBm;
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    private void captureImage(int width, int height, int rotate) {
        if(mBarcodeReader != null) {
            try {
                Intent intent = new Intent(BROADCAST_IMAGE_ACTION);
                byte[] m_frameData = mBarcodeReader.getLastDecImage();
                if(m_frameData != null && m_frameData.length > 0/* && bitmapByte != null*/) {
                    Bitmap modBm = createBitmap(m_frameData, rotate);
                    if(modBm != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        modBm.compress(Bitmap.CompressFormat.JPEG, compressJpegQuality, baos);
                        modBm.recycle();
                        modBm = null;
                        intent.putExtra(BROADCAST_IMAGE_ACTION_EXTRAS, baos.toByteArray());
                    } else {
                        intent.putExtra(BROADCAST_IMAGE_ACTION_EXTRAS, new byte[1]);
                    }
                    if(bounds != null) {
                        for(int i = 0; i < bounds.length; i++) {
                            if(i%2 == 0) {
                                bounds[i] = MAX_FRAME_WIDTH - bounds[i];
                            } else {
                                bounds[i] = MAX_FRAME_HEIGHT - bounds[i];
                            }
                        }
                        intent.putExtra("bounds", bounds);
                    }
                } else {
                    intent.putExtra(BROADCAST_IMAGE_ACTION_EXTRAS, new byte[1]);
                }
                mScanService.getContext().sendBroadcastAsUser(intent, android.os.UserHandle.ALL);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void sendBarcodeAndBitmapBroadcast(String barcode, byte[] m_frameData, int[] bounds) {
    try{
        Intent intent = new Intent(BROADCAST_IMAGE_ACTION);
        if(barcode != null) {
            //Log.d(TAG , "onDecodeComplete , --------- barcode:" + barcode);
            intent.putExtra("barcode", barcode);
        }
        if(bounds != null) {
            //镜像坐标转换
            for(int i = 0; i < bounds.length; i++) {
                if(i%2 == 0) {
                    bounds[i] = MAX_FRAME_WIDTH - bounds[i];
                } else {
                    bounds[i] = MAX_FRAME_HEIGHT - bounds[i];
                }
            }
            if(rotateImage == 90) {
                /*旋转-90*/
                for(int i = 0; i < bounds.length;) {
                     int temp = bounds[i];
                     bounds[i] = MAX_FRAME_HEIGHT - bounds[i+1];
                     bounds[i+1] = temp;
                     i = i+2;
                }
            }
            intent.putExtra("bounds", bounds);
            intent.putExtra("x1", bounds[0]);
            intent.putExtra("y1", bounds[1]);
            intent.putExtra("x2", bounds[2]);
            intent.putExtra("y2", bounds[3]);
            intent.putExtra("x3", bounds[4]);
            intent.putExtra("y3", bounds[5]);
            intent.putExtra("x4", bounds[6]);
            intent.putExtra("y4", bounds[7]);
            intent.putExtra("cx", bounds[8]);
            intent.putExtra("cy", bounds[9]);
        }
        intent.putExtra("time", (int)decodeSessionTime);
        if (enableSendBitmap && m_frameData != null) {
            if(m_frameData != null && m_frameData.length > 0) {
                Bitmap modBm = createBitmap(m_frameData, rotateImage);
                if(modBm != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    modBm.compress(Bitmap.CompressFormat.JPEG, compressJpegQuality, baos);
                    modBm.recycle();
                    modBm = null;
                    intent.putExtra(BROADCAST_IMAGE_ACTION_EXTRAS, baos.toByteArray());
                } else {
                    intent.putExtra(BROADCAST_IMAGE_ACTION_EXTRAS, new byte[1]);
                }
            } else {
                intent.putExtra(BROADCAST_IMAGE_ACTION_EXTRAS, new byte[1]);
            }
            if(TextUtils.isEmpty(BROADCAST_IMAGE_ACTION_PCK) == false) {
                intent.setPackage(BROADCAST_IMAGE_ACTION_PCK);
            }
        }
        mScanService.getContext().sendBroadcastAsUser(intent, android.os.UserHandle.ALL);
    } catch(Exception e) {
        e.printStackTrace();
    }
    }
    @Override
    public void setDefaults() {
        stopDecode();
        if(mBarcodeReader != null) {
            for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
                setDecodeParameter(INTERNAL_PROPERTY_INDEX[i]);
            }
            setDecodeParameter(PropertyID.DEC_EachImageAttempt_TIME);
            setDecodeParameter(PropertyID.DEC_DECODE_DELAY);
            setDecodeParameter(PropertyID.POSTAL_GROUP_TYPE_ENABLE);
            setDecodeParameter(PropertyID.DEC_DECODE_DEBUG_MODE);
            setDecodeParameter(PropertyID.DPM_DECODE_MODE);
            mBarcodeReader.setParameter(PropertyID.DEC_2D_LIGHTS_MODE, 3);
            mBarcodeReader.setParameter(0x0c02, 0);
            mBarcodeReader.setParameter(SDSProperties.UDI_DECODE_MODE, 0);
            setDecodeParameter(PropertyID.LABEL_SEPARATOR_ENABLE);
        }
    }

    @Override
    public boolean open() {
        synchronized (mHandler) {
            if(mBarcodeReader != null && state != State.DONE) {
                Log.d(TAG, "open State=" + state);
                return false;
            }
            /*mHandler.removeMessages(BCRDR_MSG_INIT);
            Message m = Message.obtain(mHandler, BCRDR_MSG_INIT);
            mHandler.sendMessage(m);*/
            return connectDecoderLibrary();
        }
    }

    @Override
    public void close() {
        Log.d(TAG, "close State=" + state);
        synchronized (mHandler) {
            //mHandler.removeMessages(BCRDR_MSG_RELEASE);
            mHandler.removeMessages(BCRDR_MSG_TIMER_PREVIEW);
            //Message m = Message.obtain(mHandler, BCRDR_MSG_RELEASE);
            //mHandler.sendMessage(m);
            disconnectDecoderLibrary();
        }
        g_nMultiReadResultCount = 0;
        decodeDataArray.clear();
    }

    @Override
    public void startDecode(int timeout) {
        synchronized (mHandler) {
            if(mBarcodeReader != null) {
                if(state != State.DECODING) {
                    //开机后存在无法激活
                    if(activateDecoder == false && activateDecoderTimes > 0) {
                        /*if(activateDecoderTimes == 2) {
                            syncLicenseStrore(false);
                        }*/
                        activateDecoderTimes = activateDecoderTimes - 1;
                        if(ActivationManager.checkActivateStroreFile()) {
                            try{
                                disconnectDecoderLibrary();
                                Thread.sleep(50);
                                connectDecoderLibrary();
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                    if(mBarcodeReader != null) {
                        decodeDataArray.clear();
                        g_nMultiReadResultCount = 0;
                        preDecodeData = null;
                        decodeStartTime = System.currentTimeMillis();
                        CONTINUOUS_MODE = mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES);
                        if(CONTINUOUS_MODE == Triggering.CONTINUOUS.toInt()) {
                            mBarcodeReader.startHandsFreeDecode(1);
                        } else {
                            mBarcodeReader.startDecode(timeout);
                        }
                        state = State.DECODING;
                        if(mHandler != null && workDecodeTimeOutSleep && workPreviewTime == 0) {
                            mHandler.removeMessages(BCRDR_MSG_TIMER_PREVIEW);
                            mHandler.sendEmptyMessageDelayed(BCRDR_MSG_TIMER_PREVIEW, CONTINUOUS_MODE == 4 ? InactivityTimer__DURATION_CONTINUOUS_MODE_MS:InactivityTimer_DURATION_MS);
                        }
                        workPreviewTime = System.currentTimeMillis();
                    }
                }
            } else {
                state = State.DONE;
                Log.d(TAG, "startDecode mBarcodeReader" );
            }
        }
    }

    @Override
    public void stopDecode() {
        synchronized (mHandler) {
            Log.d(TAG, "stopDecode state=" +state );
            if(mBarcodeReader != null) {
                if(state == State.DECODING) {
                    mBarcodeReader.stopDecode();
                    if(enableSendFAILEDBitmap && CONTINUOUS_MODE != Triggering.CONTINUOUS.toInt()) {
                        mHandler.sendEmptyMessage(MESSAGE_LAST_DEC_FAILED_IMAGE);
                    }
                }
                //mBarcodeReader.stopPreview();
                state = State.PREVIEW;
            } else {
                state = State.DONE;
            }
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
        if(mBarcodeReader != null) {
            if (property != null) {
                int size = property.size();
                Log.d(TAG, "setProperties property size= " + size);
                for (int i = 0; i < size; i++) {
                    int keyForIndex = property.keyAt(i);
                    setDecodeParameter(keyForIndex);
                }
            } else {
                for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
                    setDecodeParameter(INTERNAL_PROPERTY_INDEX[i]);
                }
                setDecodeParameter(PropertyID.DEC_EachImageAttempt_TIME);
                setDecodeParameter(PropertyID.LOW_CONTRAST_IMPROVED);
                setDecodeParameter(PropertyID.LOW_CONTRAST_IMPROVED_ALGORITHM);
                setDecodeParameter(PropertyID.LOW_POWER_SLEEP_MODE);
                setDecodeParameter(PropertyID.POSTAL_GROUP_TYPE_ENABLE);
                setDecodeParameter(PropertyID.DEC_DECODE_DEBUG_MODE);
                setDecodeParameter(PropertyID.DPM_DECODE_MODE);
                setDecodeParameter(PropertyID.SEND_TOKENS_OPTION);
                setDecodeParameter(PropertyID.LABEL_SEPARATOR_ENABLE);
            }
        }
        return 0;
    }

    @Override
    protected void release() {
        Log.d(TAG, "release");
        synchronized (mHandler) {
            mHandler.removeMessages(BCRDR_MSG_TIMER_PREVIEW);
            if(mBarcodeReader != null) {
                mBarcodeReader.stopDecode();
                mBarcodeReader.release();
                mBarcodeReader = null;
            }
            state = State.DONE;
        }
    }

    @Override
    public boolean lockHwTriggler(boolean lock) {
        return false;
    }
    /**
     * Method used to connect to the camera engine and initialize the API.
     *激活绑定设备wifi mac地址
     */
    private boolean enableWifi = true;
    private boolean connectDecoderLibrary() {
        File wifiAddress = new File("sys/class/net/wlan0/address");
        if(!wifiAddress.exists()) {
            enableWifi = LicenseHelper.setWifiWnable(mContext,true);
            if(enableWifi == false) {
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
        } else {
            enableWifi = true;
        }
        activateDecoder = false;
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //接口有访问远程服务器动作，避免服务器或网络异常需超时才退出，造成ANR
                    //if(BarcodeReader.activateLicense(ActivationManager.Activation_DIR) != 0){
                    //if(ActivationManager.activateAPIWithLocalFile() == false){
                    if(ActivationManager.activateCommonLicense(mContext, false) == false){//不是所有设备支持该方式
                        activateDecoder = false;
                    } else {
                        activateDecoder = true;
                        if(!syncLicenseStrore) {
                            syncLicenseStrore(true);
                            syncLicenseStrore = true;
                        }
                    }
                    if(enableWifi == false && activateDecoder)
                        LicenseHelper.setWifiWnable(mContext, enableWifi);
                }
            }).start();
            for (int i = 0; i < 2; i++) {
                Thread.sleep(70);
                if (activateDecoder) {
                    Log.d(TAG, " activateDecoder sleep " + i);
                    break;
                }
                Thread.sleep(30);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT <= 27/*Build.VERSION_CODES.N_MR1*/) {
            int ids = BarcodeReader.getNumberOfReaders();
            Log.d(TAG," cameraIds.length " + ids);
            openCameraId = ids-1;
        } else {
            int ids = BarcodeReader.getNumberOfReaders();//如果设备装了3D体积测量,检测到只有3个
            Log.d(TAG," cameraIds.length " + ids);
            // urovo add shenpidong begin 2020-05-19
            if(Build.PROJECT.equals("SQ53C") || "SQ45S".equals(Build.PROJECT)) {
                int num = android.hardware.Camera.getNumberOfCameras();//
                if(num > ids) {
                    Log.d(TAG," getNumberOfCameras " + num);
                    ids = num;
                }
                Log.d(TAG," getNumberOfCameras id:" + ids + ",num:" + num);
                if ("SQ45S".equals(Build.PROJECT)) {
                    openCameraId = num;
                } else {
                    openCameraId = ids >= 2 ? ids - 1 : ids;
                }
            } else {
                openCameraId = ids - 1;
            }
            // urovo add shenpidong end 2020-05-19
        }
        try{
            mBarcodeReader = BarcodeReader.open(openCameraId, mContext);
        } catch (Exception e){
            mBarcodeReader = null;
            e.printStackTrace();
        }
        if(mBarcodeReader != null) {
            byte[] version = new byte[64];
            mBarcodeReader.reportDecoderVersion(version);
            decoderVersion = (new String(version)).trim();
            Log.d(TAG, "version " + decoderVersion);
            mBarcodeReader.setDecodeCallback(UN603Scanner.this);
            mBarcodeReader.setDecoderListeners(UN603Scanner.this);
            //mBarcodeReader.startPreview();
            state = State.PREVIEW;
            preDecodeData = null;
            g_nMultiReadResultCount = 0;
        } else {
            return false;
        }
        if (mHandler != null && workDecodeTimeOutSleep) {
            mHandler.removeMessages(BCRDR_MSG_TIMER_PREVIEW);
            mHandler.sendEmptyMessageDelayed(BCRDR_MSG_TIMER_PREVIEW, CONTINUOUS_MODE == 4 ? InactivityTimer__DURATION_CONTINUOUS_MODE_MS:InactivityTimer_DURATION_MS);
            workPreviewTime = System.currentTimeMillis();
        }
        if(mBarcodeReader != null) {
            for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
                setDecodeParameter(INTERNAL_PROPERTY_INDEX[i]);
            }
            setDecodeParameter(PropertyID.DEC_EachImageAttempt_TIME);
            setDecodeParameter(PropertyID.LOW_CONTRAST_IMPROVED);
            setDecodeParameter(PropertyID.LOW_CONTRAST_IMPROVED_ALGORITHM);
            setDecodeParameter(PropertyID.LOW_POWER_SLEEP_MODE);
            setDecodeParameter(PropertyID.POSTAL_GROUP_TYPE_ENABLE);
            setDecodeParameter(PropertyID.DEC_DECODE_DEBUG_MODE);
            setDecodeParameter(PropertyID.DPM_DECODE_MODE);
            setDecodeParameter(PropertyID.SEND_TOKENS_OPTION);
            setDecodeParameter(PropertyID.LABEL_SEPARATOR_ENABLE);
            //打开扫描头时加载无法通过界面设置解码库的参数,检查sdcard目录下是否有property.json文件
            boolean hasParse = (advPropertyLists != null && advPropertyLists.size() > 0) ? true:false;
            int exists = DataParser.checkPropertyFileExists(hasParse);
            if(exists > 0) {
                advPropertyLists = DataParser.parseDecoderPropertyFromJSONFile(mScannerType, exists, false);
            }
            if(advPropertyLists != null && advPropertyLists.size() > 0) {
                for(Property property: advPropertyLists) {
                    if(mBarcodeReader != null) {
                        int val = -1;
                        try{
                            val = Integer.parseInt(property.getValue());
                        } catch(NumberFormatException w){}
                        if(val > -1) {
                            try {
                                mBarcodeReader.setParameter(property.getParamNum(), val);
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Method used to disconnect from the camera engine and deinitialize the API.
     *
     */
    private void disconnectDecoderLibrary() {
        if(/*state != State.DONE && */mBarcodeReader != null) {
            mBarcodeReader.stopDecode();
            mBarcodeReader.setDecodeCallback(null);
            mBarcodeReader.setDecoderListeners(null);
            mBarcodeReader.release();
        }
        activateDecoder = false;
        state = State.DONE;
        preDecodeData = null;
        g_nMultiReadResultCount = 0;
        mBarcodeReader = null;
        workPreviewTime = 0;
    }
    private void setDecodeParameter(int property) {
        int SDSProperty = -1;
        int val = 0;
        switch (property) {
            case PropertyID.LOW_POWER_SLEEP_MODE:{
                     int mode = mScanService.getPropertyInt(PropertyID.LOW_POWER_SLEEP_MODE);
                     workDecodeTimeOutSleep = mode == 0 ? false : true;
                }
                break;
            case PropertyID.LOW_POWER_SLEEP_TIMEOUT:
                InactivityTimer_DURATION_MS = mScanService.getPropertyInt(PropertyID.LOW_POWER_SLEEP_TIMEOUT);
                if (InactivityTimer_DURATION_MS < 15*1000 || InactivityTimer_DURATION_MS > 60*1000) {
                    InactivityTimer_DURATION_MS = 15*1000;
                }
                break;
            case PropertyID.TRANSMIT_CODE_ID:
                transmitCodeID = mScanService.getPropertyInt(PropertyID.TRANSMIT_CODE_ID);
                break;
            case PropertyID.LABEL_SEPARATOR_ENABLE:
                enableGroupSeperator = mScanService.getPropertyInt(PropertyID.LABEL_SEPARATOR_ENABLE);
                break;
            case PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL:
                //delayDecode = property.get(keyForIndex);
                int seclevel = mScanService.getPropertyInt(PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL);
                Log.d(TAG, "SECURITY_LEVEL= " + seclevel);
                if(mBarcodeReader != null) {
                    if(seclevel > 1) {
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_C128_SECURITY, 1);
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_UPC_SECURITY, 1);
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_USE_MLD, 0x34);//b2+b4+b5=52
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_USE_DISTANCE_MAP, 0x34);
                    } else {
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_C128_SECURITY, 0);
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_UPC_SECURITY, 0);
                        //打印异常间隔过大破损条码,误码率可能提升(upc/code128/code39)
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_USE_MLD, 0);
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_USE_DISTANCE_MAP, 0);
                    }
                }
                break;
            case PropertyID.MULTI_DECODE_MODE:
            case PropertyID.BAR_CODES_TO_READ:
                val = mScanService.getPropertyInt(PropertyID.MULTI_DECODE_MODE);
                Log.d(TAG, "setProperties property multiDecodeMode= " + val);
                multiDecodeMode = val;
                if(mBarcodeReader != null) {
                    mBarcodeReader.setParameter(PropertyID.MULTI_DECODE_MODE, val);
                    g_nMaxMultiReadCount = mScanService.getPropertyInt(PropertyID.BAR_CODES_TO_READ);
                    if(g_nMaxMultiReadCount <= 0) {
                        g_nMaxMultiReadCount = 1;
                    }
                    if(multiDecodeMode > 0) {
                        if(g_nMaxMultiReadCount > 5) {
                            //单张图片解码数量，增加搜索条码时间
                            mBarcodeReader.setParameter(PropertyID.LASER_ON_TIME, 1500);
                        }
                        mBarcodeReader.setMultiReadCount(g_nMaxMultiReadCount);
                    } else {
                        mBarcodeReader.setMultiReadCount(1);
                    }
                }
                break;
            case PropertyID.DEC_PICKLIST_AIM_MODE:
                val = mScanService.getPropertyInt(PropertyID.DEC_PICKLIST_AIM_MODE);
                if(mBarcodeReader != null) {
                    mBarcodeReader.setParameter(PropertyID.DEC_PICKLIST_AIM_MODE, val);
                }
                break;
            case PropertyID.DEC_PICKLIST_AIM_DELAY:
                val = mScanService.getPropertyInt(PropertyID.DEC_PICKLIST_AIM_DELAY);
                if(mBarcodeReader != null) {
                    if (val < 0 || val > 4000) {
                        val = 1000;
                    }
                    mBarcodeReader.setParameter(PropertyID.DEC_PICKLIST_AIM_DELAY, val);
                }
                break;
            case PropertyID.FULL_READ_MODE:
                val = mScanService.getPropertyInt(PropertyID.FULL_READ_MODE);
                Log.d(TAG, "setProperties property fullReadMode= " + val);
                fullReadMode = val;
                if(mBarcodeReader != null)
                    mBarcodeReader.setParameter(PropertyID.FULL_READ_MODE, val);
                break;
            case PropertyID.DEC_2D_LIGHTS_MODE:
                val = mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE);
                if(mBarcodeReader != null) {
                    if (val < 0 || val > 4) {
                        val = 3;
                    }
                    mBarcodeReader.setParameter(PropertyID.DEC_2D_LIGHTS_MODE, val);
                }
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
                    mBarcodeReader.setParameter(PropertyID.DEC_DECODE_DELAY, delayMillis);
                break;
            case PropertyID.DEC_DECODE_DEBUG_MODE:
                saveMode = mScanService.getPropertyInt(PropertyID.DEC_DECODE_DEBUG_MODE);
                Log.d(TAG, "setProperties property saveMode= " + saveMode);
                if(mBarcodeReader != null) {
                    if(saveMode >= 0 && saveMode <= 4) {
                        mBarcodeReader.setParameter(PropertyID.DEC_DECODE_DEBUG_MODE, saveMode);
                    } else {
                        mBarcodeReader.setParameter(PropertyID.DEC_DECODE_DEBUG_MODE, 0);
                    }
                }
                break;
            case PropertyID.DEC_Multiple_Decode_INTERVAL:
                val = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_INTERVAL);
                if(mBarcodeReader != null) {
                    mBarcodeReader.setParameter(PropertyID.DEC_Multiple_Decode_INTERVAL, val);
                }
                break;
            case PropertyID.DEC_MaxMultiRead_COUNT:
                cotinuousMultiReadCount = mScanService.getPropertyInt(PropertyID.DEC_MaxMultiRead_COUNT);
                if(cotinuousMultiReadCount <= 2 || cotinuousMultiReadCount > 10) {
                    cotinuousMultiReadCount = 2;
                }
                Log.d(TAG, "setProperties property cotinuousMultiReadCount= " + cotinuousMultiReadCount);
                break;
            case PropertyID.DEC_Multiple_Decode_MODE:
                ignoreRepeatMode = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_MODE);
                break;
            case PropertyID.DEC_Multiple_Decode_TIMEOUT:
                try {
                    waitRepeatTimeOut = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_TIMEOUT);
                    if(waitRepeatTimeOut < 50) {
                        waitRepeatTimeOut = 10 * 1000;
                    }
                    Log.d(TAG, "setProperties property g_waitRepeatTimeOut= " + waitRepeatTimeOut);
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
            case PropertyID.IMAGE_ONE_D_INVERSE: {
                    SDSProperty = SDSProperties.SD_PROP_MISC_LINEAR_DECODE_POLARITY;
                    val = mScanService.getPropertyInt(PropertyID.IMAGE_ONE_D_INVERSE);
                    if (val < 0 || val > 2) {
                        val = 0;
                    }
                }
                break;
            case PropertyID.FUZZY_1D_PROCESSING: {//提升破损条码能力
                    SDSProperty = SDSProperties.SD_PROP_MISC_LINEAR_DAMAGE_IMPROVEMENTS;
                    val = mScanService.getPropertyInt(PropertyID.FUZZY_1D_PROCESSING);
                }
                break;
            case PropertyID.SEND_TOKENS_OPTION: {
                    SDSProperty = SDSProperties.UDI_DECODE_MODE;
                    val = mScanService.getPropertyInt(PropertyID.SEND_TOKENS_OPTION);
                    if (val > 0) {
                        val = 1;
                    }
                }
                break;
            case PropertyID.LOW_CONTRAST_IMPROVED_ALGORITHM:
                try {
                    int ALGMode = mScanService.getPropertyInt(PropertyID.LOW_CONTRAST_IMPROVED_ALGORITHM);
                    ALGMode = ALGMode != 1 ? 0:ALGMode;
                    mBarcodeReader.setParameter(PropertyID.LOW_CONTRAST_IMPROVED_ALGORITHM, ALGMode);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                }
                break;
             case PropertyID.LOW_CONTRAST_IMPROVED:
                SDSProperty = SDSProperties.SD_PROP_MISC_LOW_CONTRAST_IMPROVEMENTS;
                val = mScanService.getPropertyInt(PropertyID.LOW_CONTRAST_IMPROVED);
                Log.d(TAG, "lowCONTRASTMode= " + val);
                if(val < 0 || val > 5) {
                    val = 0;
                }
                if(val == 3) {
                    val = 4;
                } else if(val == 4) {
                    val = 3;
                } else if(val == 5) {
                    val = 7;
                }
                break;
            case PropertyID.AZTEC_ENABLE:
            case PropertyID.AZTEC_INVERSE:
                /*The property value is a bit field defined as follows:
                b0: Enable normal video Aztec decoding //1
                b1: Enable inverse video Aztec decoding //2
                b2: Enable Compact Aztec Code decoding ////4 0010
                b3: Enable Full-Size Aztec Code decoding*///8
                SDSProperty = SDSProperties.SD_PROP_AZ_ENABLED;
                int aztec = mScanService.getPropertyInt(PropertyID.AZTEC_ENABLE);//1
                int inverse = mScanService.getPropertyInt(PropertyID.AZTEC_INVERSE);//2
                if(aztec == 1)
                    val += 9;
                if(aztec == 1 && inverse == 1) {
                    val = 2;
                } else if(aztec == 1 && inverse == 2) {
                    val += 2;
                }
                if(mBarcodeReader != null) {
                    mBarcodeReader.setParameter(SDSProperties.SD_PROP_AZ_ENABLED, val);
                    if(val != General.CONST_DISABLE) {
                        val = mScanService.getPropertyInt(PropertyID.AZTEC_SYMBOL_SIZE);
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_AZ_SYMBOL_SIZE, val);
                    }
                    SDSProperty = -1;
                }
                break;
            case PropertyID.AZTEC_SYMBOL_SIZE:
                SDSProperty = SDSProperties.SD_PROP_AZ_SYMBOL_SIZE;
                val = mScanService.getPropertyInt(PropertyID.AZTEC_SYMBOL_SIZE);
                break;
            case PropertyID.UPCA_ENABLE:
                SDSProperty = SDSProperties.UPCA_ENABLE;
                val = mScanService.getPropertyInt(PropertyID.UPCA_ENABLE);
                break;
            case PropertyID.UPCA_SEND_CHECK:
                SDSProperty = SDSProperties.UPCA_SEND_CHECK;
                val = mScanService.getPropertyInt(PropertyID.UPCA_SEND_CHECK);
                break;
            case PropertyID.UPCA_SEND_SYS:
                SDSProperty = SDSProperties.UPCA_SEND_SYS;
                val = mScanService.getPropertyInt(PropertyID.UPCA_SEND_SYS);
                break;
            case PropertyID.UPCA_TO_EAN13:
                SDSProperty = SDSProperties.UPCA_TO_EAN13;
                val = mScanService.getPropertyInt(PropertyID.UPCA_TO_EAN13);
                break;
            case PropertyID.UPCE_ENABLE:
                SDSProperty = SDSProperties.UPCE_ENABLE;
                val = mScanService.getPropertyInt(PropertyID.UPCE_ENABLE);
                break;
            case PropertyID.UPCE_TO_UPCA:
                SDSProperty = SDSProperties.UPCE_TO_UPCA;
                val = mScanService.getPropertyInt(PropertyID.UPCE_TO_UPCA);
                break;
            case PropertyID.UPCE_SEND_CHECK:
                SDSProperty = SDSProperties.UPCE_SEND_CHECK;
                val = mScanService.getPropertyInt(PropertyID.UPCE_SEND_CHECK);
                break;
            case PropertyID.UPCE_SEND_SYS:
                SDSProperty = SDSProperties.UPCE_SEND_SYS;
                val = mScanService.getPropertyInt(PropertyID.UPCE_SEND_SYS);
                break;
            case PropertyID.UPCE1_TO_UPCA:
                SDSProperty = SDSProperties.UPCE1_TO_UPCA;
                val = mScanService.getPropertyInt(PropertyID.UPCE1_TO_UPCA);
                break;
            case PropertyID.UPCE1_SEND_CHECK:
                SDSProperty = SDSProperties.UPCE1_SEND_CHECK;
                val = mScanService.getPropertyInt(PropertyID.UPCE1_SEND_CHECK);
                break;
            case PropertyID.UPCE1_SEND_SYS:
                SDSProperty = SDSProperties.UPCE1_SEND_SYS;
                val = mScanService.getPropertyInt(PropertyID.UPCE1_SEND_SYS);
                break;
            case PropertyID.UPCE1_ENABLE:
                SDSProperty = SDSProperties.UPCE1_ENABLE;
                val = mScanService.getPropertyInt(PropertyID.UPCE1_ENABLE);
                break;
            case PropertyID.EAN13_BOOKLANDEAN:
                SDSProperty = SDSProperties.EAN13_BOOKLANDEAN;
                val = mScanService.getPropertyInt(PropertyID.EAN13_BOOKLANDEAN);
                break;
            case PropertyID.EAN13_BOOKLAND_FORMAT:
                SDSProperty = SDSProperties.EAN13_BOOKLAND_FORMAT;
                val = mScanService.getPropertyInt(PropertyID.EAN13_BOOKLAND_FORMAT);
                break;
            case PropertyID.EAN13_SEND_CHECK:
                SDSProperty = SDSProperties.EAN13_SEND_CHECK;
                val = mScanService.getPropertyInt(PropertyID.EAN13_SEND_CHECK);
                break;
            case PropertyID.EAN13_ENABLE:
                SDSProperty = SDSProperties.EAN13_ENABLE;
                val = mScanService.getPropertyInt(PropertyID.EAN13_ENABLE);
                break;
            case PropertyID.EAN8_ENABLE:
                SDSProperty = SDSProperties.EAN8_ENABLE;
                val = mScanService.getPropertyInt(PropertyID.EAN8_ENABLE);
                break;
            case PropertyID.EAN8_SEND_CHECK:
                SDSProperty = SDSProperties.EAN8_SEND_CHECK;
                val = mScanService.getPropertyInt(PropertyID.EAN8_SEND_CHECK);
                break;
            case PropertyID.EAN8_TO_EAN13:
                SDSProperty = SDSProperties.EAN8_TO_EAN13;
                val = mScanService.getPropertyInt(PropertyID.EAN8_TO_EAN13);
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
                //会默认发送check char
                SDSProperty = SDSProperties.SD_PROP_C39_FULL_ASCII;
                val = mScanService.getPropertyInt(PropertyID.CODE39_FULL_ASCII);
                break;
            case PropertyID.CODE39_ENABLE_CHECK:
            case PropertyID.CODE39_SEND_CHECK:{
                    SDSProperty = SDSProperties.SD_PROP_C39_CHECKSUM;
                    int enableCk = mScanService.getPropertyInt(PropertyID.CODE39_ENABLE_CHECK);
                    int sendCk = mScanService.getPropertyInt(PropertyID.CODE39_SEND_CHECK);
                    if(enableCk == 1 && sendCk == 1) {
                        val = 1;//文档描述有误
                    } else if(enableCk == 1) {
                        val = 2;
                    } else {
                        val = 0;
                    }
                }
                break;
            case PropertyID.TRIOPTIC_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_TRIOPTIC_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.TRIOPTIC_ENABLE);
                break;
            case PropertyID.CODE128_ENABLE:
                try {
                    //SDSProperty = SDSProperties.SD_PROP_C128_ENABLED;
                    val = mScanService.getPropertyInt(PropertyID.CODE128_ENABLE);
                    mBarcodeReader.setParameter(SDSProperties.SD_PROP_C128_ENABLED, val);
                    if(val == 1) {
                        val = mScanService.getPropertyInt(PropertyID.C128_OUT_OF_SPEC);
                        if (val < 0 || val > 15) {
                            val = 0;
                        }
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_C128_OUT_OF_SPEC_SYMBOL, val);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                }
                break;
            case PropertyID.CODE_ISBT_128:
                SDSProperty = SDSProperties.ISBT128_ENABLE;
                val = mScanService.getPropertyInt(PropertyID.CODE_ISBT_128);
                break;
            case PropertyID.CODE128_GS1_ENABLE:       //gs1-128
                SDSProperty = SDSProperties.GS1_128_ENABLE;
                val = mScanService.getPropertyInt(PropertyID.CODE128_GS1_ENABLE);
                break;
            case PropertyID.I25_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_I25_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.I25_ENABLE);
                break;
            case PropertyID.I25_ENABLE_CHECK:
            case PropertyID.I25_SEND_CHECK: {
                    //0: Disable checksum checking. 1
                    //1: Enable checksum checking. 2
                    //2: Enable checksum checking and strip the checksum from the result string. 4
                    SDSProperty = SDSProperties.SD_PROP_I25_CHECKSUM;
                    int enableCk = mScanService.getPropertyInt(PropertyID.I25_ENABLE_CHECK);
                    int sendCk = mScanService.getPropertyInt(PropertyID.I25_SEND_CHECK);
                    if(enableCk == 1 && sendCk == 1) {
                        val = 1;//文档描述有误
                    } else if(enableCk == 1) {
                        val = 2;
                    } else {
                        val = 0;
                    }
                }
                break;
            case PropertyID.I25_TO_EAN13:
                SDSProperty = SDSProperties.I25_TO_EAN13;
                val = mScanService.getPropertyInt(PropertyID.I25_TO_EAN13);
                break;
            case PropertyID.CODABAR_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_CB_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.CODABAR_ENABLE);
                break;
            case PropertyID.CODABAR_NOTIS:
                SDSProperty = SDSProperties.CODABAR_NOTIS_Editing;
                val = mScanService.getPropertyInt(PropertyID.CODABAR_NOTIS);
                break;
            case PropertyID.CODABAR_CLSI:
                SDSProperty = SDSProperties.CODABAR_CLSI_Editing;
                val = mScanService.getPropertyInt(PropertyID.CODABAR_CLSI);
                break;
            /*case PropertyID.CODABAR_ENABLE_CHECK:
            case PropertyID.CODABAR_SEND_CHECK:
                SDSProperty = SDSProperties.SD_PROP_CB_CHECKSUM;
                int enableCk = mScanService.getPropertyInt(PropertyID.CODABAR_ENABLE_CHECK);
                int sendCk = mScanService.getPropertyInt(PropertyID.CODABAR_SEND_CHECK);
                if(sendCk == 1) {
                    val = 2;
                } else if(enableCk == 1) {
                    val = 1;
                } else {
                    val = 0;
                }
                break;*/
            case PropertyID.GS1_14_ENABLE:
            case PropertyID.GS1_EXP_ENABLE:
            case PropertyID.GS1_LIMIT_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_RSS_ENABLED;
                /*The property value is a bit field defined as follows:
                    b0: Enable GS1 Databar Expanded decoding. 1
                    b1: Enable GS1 Databar Expanded Stacked decoding. 2
                    b2: Enable GS1 Databar Limited decoding. 4
                    b3: Enable GS1 Databar Omnidirectional and GS1 Databar truncated decoding. 8
                    b4: Enable GS1 Databar Stacked Omnidirectional and GS1 Databar Stacked decoding. 16*/
                int gs1_trun = mScanService.getPropertyInt(PropertyID.GS1_14_ENABLE);//8//10
                int exp_val = mScanService.getPropertyInt(PropertyID.GS1_EXP_ENABLE);//1
                int limit_val = mScanService.getPropertyInt(PropertyID.GS1_LIMIT_ENABLE);//4 0010
                if(gs1_trun == 1 && exp_val == 1 && limit_val == 1) {
                    val = 0x1F;//31
                } else{
                    if(gs1_trun == 1)
                        val += 24;
                    if(exp_val == 1)
                        val += 3;
                    if(limit_val == 1)
                        val += 4;
                }
                break;
            case PropertyID.GS1_14_TO_UPC_EAN:
                SDSProperty = SDSProperties.GS1_DATABAR14_TO_UPC;
                val = mScanService.getPropertyInt(PropertyID.GS1_14_TO_UPC_EAN);
                break;
            case PropertyID.MSI_ENABLE:
            case PropertyID.MSI_CHECK_2_MOD_11:
            case PropertyID.MSI_REQUIRE_2_CHECK:
            case PropertyID.MSI_SEND_CHECK: {
                    /* default 0 文档有无
                    0: Disable checksum checking.
                    1: Enable a single mod 10 checksum check.
                    2: Enable a mod 11 and a mod 10 checksum check.
                    3: Enable two mod 10 checksum checks.
                    5: Enable a single mod 10 checksum check and strip the checksum
                    6: Enable a mod 11 and a mod 10 checksum check and strip the checksums
                    7: Enable two mod 10 checksum checks and strip the checksums
                    */
                    if(mBarcodeReader != null) {
                        int msiEnable = mScanService.getPropertyInt(PropertyID.MSI_ENABLE);
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_MSIP_ENABLED, msiEnable);
                        if(msiEnable == 1) {
                            SDSProperty = SDSProperties.SD_PROP_MSIP_CHECKSUM;
                            int ckDigitAlg = mScanService.getPropertyInt(PropertyID.MSI_CHECK_2_MOD_11);
                            int ckDigitMode = mScanService.getPropertyInt(PropertyID.MSI_REQUIRE_2_CHECK);
                            int sendCK = mScanService.getPropertyInt(PropertyID.MSI_SEND_CHECK);
                            if(sendCK == 1) {
                                if(ckDigitMode == 0) {
                                    //one check;
                                    val = 1;
                                } else {
                                    //two check;
                                    if(ckDigitAlg == 0) {
                                        //MOD 10/MOD 11
                                        val = 2;
                                    } else {
                                        //MOD 10/MOD 10
                                        val = 3;
                                    }
                                }
                            } else {
                                if(ckDigitMode == 0) {
                                    //one check;
                                    val = 5;
                                } else {
                                    //two check;
                                    if(ckDigitAlg == 0) {
                                        //MOD 10/MOD 11
                                        val = 6;
                                    } else {
                                        //MOD 10/MOD 10
                                        val = 7;
                                    }
                                }
                            }
                            //value is ignored if MSI Plessey is disable
                            mBarcodeReader.setParameter(SDSProperties.SD_PROP_MSIP_CHECKSUM, val);
                        }
                        SDSProperty = -1;
                    }
                }
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
                if(mBarcodeReader != null) {
                    mBarcodeReader.setParameter(SDSProperties.SD_PROP_MC_ENABLED, val);
                    if(val != General.CONST_DISABLE) {
                        val = mScanService.getPropertyInt(PropertyID.MAXICODE_SYMBOL_SIZE);
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_MC_SYMBOL_SIZE, val);
                    }
                    SDSProperty = -1;
                }
                break;
            case PropertyID.MAXICODE_SYMBOL_SIZE:
                SDSProperty = SDSProperties.SD_PROP_MC_SYMBOL_SIZE;
                val = mScanService.getPropertyInt(PropertyID.MAXICODE_SYMBOL_SIZE);
                break;
            case PropertyID.QRCODE_ENABLE:
            case PropertyID.QRCODE_INVERSE:
            case PropertyID.MICROQRCODE_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_QR_ENABLED;
                int qr = mScanService.getPropertyInt(PropertyID.QRCODE_ENABLE);//1
                int qrInverse = mScanService.getPropertyInt(PropertyID.QRCODE_INVERSE);//2/3
                int micqr = mScanService.getPropertyInt(PropertyID.MICROQRCODE_ENABLE);//2/3
                val = General.CONST_DISABLE;
                /*b0: Enable normal video QR Code decoding. qr + 1
                b1: Enable inverse video QR Code decoding. qr +2
                b2: Enable normal video Micro QR Code decoding. qr  + 4
                b3: Enable inverse video Micro QR Code decoding. qr  + 8*/
                if(qr == 1) {
                    val = General.CONST_ENABLE;
                }
                if(qrInverse == 1 && qr == 1) {
                    val = 2;
                } else if(qrInverse == 2 && qr == 1) {
                    val += 2;
                }
                if(micqr == 1) {
                    val += 4;
                    if(qrInverse > 0) {
                        val += 8;
                    }
                }
                if(mBarcodeReader != null) {
                    mBarcodeReader.setParameter(SDSProperties.SD_PROP_QR_ENABLED, val);
                    if(val != General.CONST_DISABLE) {
                        val = mScanService.getPropertyInt(PropertyID.QRCODE_SYMBOL_SIZE);
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_QR_SYMBOL_SIZE, val);
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_QR_WITHOUT_QZ, 1);
                    }
                    SDSProperty = -1;
                }
                break;
            case PropertyID.QRCODE_SYMBOL_SIZE:
                SDSProperty = SDSProperties.SD_PROP_QR_SYMBOL_SIZE;
                val = mScanService.getPropertyInt(PropertyID.QRCODE_SYMBOL_SIZE);
                break;
            case PropertyID.DATAMATRIX_SYMBOL_SIZE:
                SDSProperty = SDSProperties.SD_PROP_DM_SYMBOL_SIZE;
                val = mScanService.getPropertyInt(PropertyID.DATAMATRIX_SYMBOL_SIZE);
                break;
            case PropertyID.DATAMATRIX_ENABLE:
            case PropertyID.DATAMATRIX_INVERSE:
                SDSProperty = SDSProperties.SD_PROP_DM_ENABLED;
                int dm = mScanService.getPropertyInt(PropertyID.DATAMATRIX_ENABLE);//1
                int dmInverse = mScanService.getPropertyInt(PropertyID.DATAMATRIX_INVERSE);//2/3
                //b0: Enable normal video Data Matrix decoding
                //b1: Enable inverse video Data Matrix decoding
                if(dm == 1) {
                    val = 1;
                }
                if(dm == 1 && dmInverse == 1) {
                    val = 2;
                } else if(dm == 1 && dmInverse == 2) {
                    val += 2;
                }
                if(mBarcodeReader != null) {
                    mBarcodeReader.setParameter(SDSProperties.SD_PROP_DM_ENABLED, val);
                    if(val != General.CONST_DISABLE) {
                        val = mScanService.getPropertyInt(PropertyID.DATAMATRIX_SYMBOL_SIZE);
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_DM_SYMBOL_SIZE, val);
                        //lower-left 忽略L标识不规范，影响解码
                        mBarcodeReader.setParameter(SDSProperties.SD_PROP_DM_ENHANCED_DAMAGE_MODE, 1);
                    }
                    SDSProperty = -1;
                }
                break;
            case PropertyID.DPM_DECODE_MODE:
                SDSProperty = SDSProperties.SD_PROP_DPM_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.DPM_DECODE_MODE);
                break;
            case PropertyID.HANXIN_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_HX_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.HANXIN_ENABLE);
                break;
            case PropertyID.M25_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_M25_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.M25_ENABLE);
                break;
            case PropertyID.D25_ENABLE:
                //Straight 2 of 5 (with 2 bar start/stop codes) is also known as: Standard 2 of 5, IATA 2 of 5, and Airline 2 of 5
                SDSProperty = SDSProperties.SD_PROP_S25_2SS_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.D25_ENABLE);
                try {
                    if(mBarcodeReader != null){
                        mBarcodeReader.setParameter(SDSProperty, val);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                }
                //Straight 2 of 5 (with 3 bar start/stop codes) is also known as: Industrial 2 of 5, Code 2 of 5, and Discrete 2 of 5
                SDSProperty = SDSProperties.SD_PROP_S25_3SS_ENABLED;
                break;
            case PropertyID.CODE11_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_C11_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.CODE11_ENABLE);
                break;
            case PropertyID.CODE11_ENABLE_CHECK:
            case PropertyID.CODE11_SEND_CHECK:
                SDSProperty = SDSProperties.SD_PROP_C11_CHECKSUM;
                //0 two check digits;1 one check; 2 two check and stripped form result data; 3 one check and stripped form result data; 4 no check
                int enCKMode = mScanService.getPropertyInt(PropertyID.CODE11_ENABLE_CHECK);//0 no 1 one 2 two
                if(enCKMode == 0) {
                    val = 4;
                } else {
                    boolean sendCK = mScanService.getPropertyInt(PropertyID.CODE11_SEND_CHECK) == 1;
                    if(enCKMode == 1) {
                        if(sendCK) {
                            val = 1;
                        } else {
                            val = 3;
                        }
                    } else {
                        if(sendCK) {
                            val = 0;
                        } else {
                            val = 2;
                        }
                    }
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
            case PropertyID.COMPOSITE_CC_AB_ENABLE:
                val = mScanService.getPropertyInt(PropertyID.COMPOSITE_CC_AB_ENABLE);
                mBarcodeReader.setParameter(SDSProperties.Composite_CC_A, val);
                mBarcodeReader.setParameter(SDSProperties.Composite_CC_B, val);
                break;
            case PropertyID.COMPOSITE_CC_C_ENABLE:
                SDSProperty = SDSProperties.Composite_CC_C;
                val = mScanService.getPropertyInt(PropertyID.COMPOSITE_CC_C_ENABLE);
                break;
            case PropertyID.COMPOSITE_TLC39_ENABLE:
                SDSProperty = SDSProperties.Composite_Code39;
                val = mScanService.getPropertyInt(PropertyID.COMPOSITE_TLC39_ENABLE);
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
                SDSProperty = -1;
                disablePostalSymbologies();
                break;
            case PropertyID.KOREA_POST_ENABLE:
                SDSProperty = SDSProperties.SD_PROP_KP_ENABLED;
                val = mScanService.getPropertyInt(PropertyID.KOREA_POST_ENABLE);
                break;
            case PropertyID.CODABAR_LENGTH1:
                SDSProperty = SDSProperties.CODABAR_LENGTH1;
                val = mScanService.getPropertyInt(PropertyID.CODABAR_LENGTH1);
                break;
            case PropertyID.CODABAR_LENGTH2:
                SDSProperty = SDSProperties.CODABAR_LENGTH2;
                val = mScanService.getPropertyInt(PropertyID.CODABAR_LENGTH2);
                break;
            case PropertyID.CODE11_LENGTH1:
                SDSProperty = SDSProperties.CODE11_LENGTH1;
                val = mScanService.getPropertyInt(PropertyID.CODE11_LENGTH1);
                break;
            case PropertyID.CODE11_LENGTH2:
                SDSProperty = SDSProperties.CODE11_LENGTH2;
                val = mScanService.getPropertyInt(PropertyID.CODE11_LENGTH2);
                break;
            case PropertyID.CODE39_LENGTH1:
                SDSProperty = SDSProperties.CODE39_LENGTH1;
                val = mScanService.getPropertyInt(PropertyID.CODE39_LENGTH1);
                break;
            case PropertyID.CODE39_LENGTH2:
                SDSProperty = SDSProperties.CODE39_LENGTH2;
                val = mScanService.getPropertyInt(PropertyID.CODE39_LENGTH2);
                break;
            case PropertyID.CODE93_LENGTH1:
                SDSProperty = SDSProperties.CODE93_LENGTH1;
                val = mScanService.getPropertyInt(PropertyID.CODE93_LENGTH1);
                break;
            case PropertyID.CODE93_LENGTH2:
                SDSProperty = SDSProperties.CODE93_LENGTH2;
                val = mScanService.getPropertyInt(PropertyID.CODE93_LENGTH2);
                break;
            case PropertyID.I25_LENGTH1:
                SDSProperty = SDSProperties.I25_LENGTH1;
                val = mScanService.getPropertyInt(PropertyID.I25_LENGTH1);
                break;
            case PropertyID.I25_LENGTH2:
                SDSProperty = SDSProperties.I25_LENGTH2;
                val = mScanService.getPropertyInt(PropertyID.I25_LENGTH2);
                break;
            case PropertyID.CODE128_LENGTH1:
                SDSProperty = SDSProperties.CODE128_LENGTH1;
                val = mScanService.getPropertyInt(PropertyID.CODE128_LENGTH1);
                break;
            case PropertyID.CODE128_LENGTH2:
                SDSProperty = SDSProperties.CODE128_LENGTH2;
                val = mScanService.getPropertyInt(PropertyID.CODE128_LENGTH2);
                break;
            case PropertyID.D25_LENGTH1:
                SDSProperty = SDSProperties.D25_LENGTH1;
                val = mScanService.getPropertyInt(PropertyID.D25_LENGTH1);
                break;
            case PropertyID.D25_LENGTH2:
                SDSProperty = SDSProperties.D25_LENGTH2;
                val = mScanService.getPropertyInt(PropertyID.D25_LENGTH2);
                break;
            case PropertyID.MSI_LENGTH1:
                SDSProperty = SDSProperties.MSI_LENGTH1;
                val = mScanService.getPropertyInt(PropertyID.MSI_LENGTH1);
                break;
            case PropertyID.MSI_LENGTH2:
                SDSProperty = SDSProperties.MSI_LENGTH2;
                val = mScanService.getPropertyInt(PropertyID.MSI_LENGTH2);
                break;
            case PropertyID.GS1_EXP_LENGTH1:
                SDSProperty = SDSProperties.GS1_EXP_LENGTH1;
                val = mScanService.getPropertyInt(PropertyID.GS1_EXP_LENGTH1);
                break;
            case PropertyID.GS1_EXP_LENGTH2:
                SDSProperty = SDSProperties.GS1_EXP_LENGTH2;
                val = mScanService.getPropertyInt(PropertyID.GS1_EXP_LENGTH2);
                break;
            case PropertyID.DEC_2D_CENTERING_ENABLE:
            case PropertyID.DEC_2D_CENTERING_MODE:
            case PropertyID.DEC_2D_WINDOW_UPPER_LX:
            case PropertyID.DEC_2D_WINDOW_UPPER_LY:
            case PropertyID.DEC_2D_WINDOW_LOWER_RX:
            case PropertyID.DEC_2D_WINDOW_LOWER_RY:
                setDecodeWindowSettings();
                break;
            case PropertyID.IMAGE_EXPOSURE_MODE:
            case PropertyID.DEC_ES_MAX_GAIN:
            case PropertyID.DEC_ES_MAX_EXP:
            case PropertyID.DEC_ES_TARGET_VALUE:
                setSensorExposure();
                break;
            default:
                SDSProperty = -1;
                break;
        }
        if(SDSProperty != -1 && mBarcodeReader != null) {
            mBarcodeReader.setParameter(SDSProperty, val);
            SDSProperty = -1;
        }
    }
    private void setSensorExposure() {
        int mode = mScanService.getPropertyInt(PropertyID.IMAGE_EXPOSURE_MODE);
        if (mBarcodeReader != null) {
            mBarcodeReader.setParameter(PropertyID.DEC_ES_EXPOSURE_METHOD, mode);
            if(mode == 1) {
                int maxVal = mScanService.getPropertyInt(PropertyID.DEC_ES_MAX_EXP);
                try{
                    if(maxVal <1 || maxVal > 900) maxVal = 100;
                    //String exp = Integer.toHexString(maxVal);
                    //int expInt = Integer.parseInt(exp);
                    //mBarcodeReader.setParameter(PropertyID.DEC_ES_MAX_EXP, expInt);
                    mBarcodeReader.setParameter(PropertyID.DEC_ES_MAX_EXP, maxVal);
                } catch (Exception e){
                    e.printStackTrace();
                }
                maxVal = mScanService.getPropertyInt(PropertyID.DEC_ES_MAX_GAIN);
                try{
                    if(maxVal <1 || maxVal > 255) maxVal = 4;
                    mBarcodeReader.setParameter(PropertyID.DEC_ES_MAX_GAIN, maxVal);
                } catch (Exception e){
                    e.printStackTrace();
                }
                maxVal = mScanService.getPropertyInt(PropertyID.DEC_ES_TARGET_VALUE);
                try{
                    if(maxVal <1 || maxVal > 8000) maxVal = 8000;
                    mBarcodeReader.setParameter(PropertyID.DEC_ES_TARGET_VALUE, maxVal);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * Sets the Decoder settings based on user preferences
     * (412, 316), (420,324),Field of View,（一定要选择这个）
     */
    private void setDecodeWindowSettings() {
        if (mBarcodeReader != null) {
            // Windowing
            boolean enable_windowing = mScanService.getPropertyInt(PropertyID.DEC_2D_CENTERING_ENABLE) == 1;
            try {
                if (enable_windowing) {
                    int mode = mScanService.getPropertyInt(PropertyID.DEC_2D_CENTERING_MODE);
                    mode = mode >= 0 && mode <= 2 ? mode : 0;
                    mBarcodeReader.setParameter(PropertyID.DEC_2D_CENTERING_MODE, mode);
                    if (mode == DecodeWindowMode.DECODE_WINDOW_MODE_FIELD_OF_VIEW.toInt()) {
                        int UpperLeftX = mScanService.getPropertyInt(PropertyID.DEC_2D_WINDOW_UPPER_LX);
                        int UpperLeftY = mScanService.getPropertyInt(PropertyID.DEC_2D_WINDOW_UPPER_LY);
                        int LowerRightX = mScanService.getPropertyInt(PropertyID.DEC_2D_WINDOW_LOWER_RX);
                        int LowerRightY = mScanService.getPropertyInt(PropertyID.DEC_2D_WINDOW_LOWER_RY);
                        if (UpperLeftX < 0) UpperLeftX = 0;
                        if (UpperLeftY < 0) UpperLeftY = 0;
                        if (LowerRightX < 20) LowerRightX = 1280;
                        if (LowerRightY < 20) LowerRightY = 720;
                        Log.d(TAG, "nMode = " + mode + " upX " + UpperLeftX + " upY " + UpperLeftY + " lwX " + LowerRightX + " lwY " + LowerRightY);
                        // enable the mode
                        //镜像处理坐标转换
                        int X = MAX_FRAME_WIDTH - UpperLeftX;
                        int Y = MAX_FRAME_HEIGHT - UpperLeftY;
                        UpperLeftX = MAX_FRAME_WIDTH - LowerRightX;
                        UpperLeftY = MAX_FRAME_HEIGHT - LowerRightY;
                        LowerRightX = X;
                        LowerRightY = Y;
                        mBarcodeReader.setParameter(SDSProperties.UpperLeftX, UpperLeftX);
                        mBarcodeReader.setParameter(SDSProperties.UpperLeftY, UpperLeftY);
                        mBarcodeReader.setParameter(SDSProperties.LowerRightX, LowerRightX);
                        mBarcodeReader.setParameter(SDSProperties.LowerRightY, LowerRightY);
                    }
                } else {
                    // disable windowing
                    mBarcodeReader.setParameter(PropertyID.DEC_2D_CENTERING_MODE, DecodeWindowMode.DECODE_WINDOW_MODE_OFF.toInt());
                }
            } catch (Exception e) {
            }
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
