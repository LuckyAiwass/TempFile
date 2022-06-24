
package com.android.server.scanner;

import com.android.server.ScanServiceWrapper;
import com.hsm.barcode.*;
import com.hsm.barcode.DecoderException.ResultID;
import com.hsm.barcode.ExposureValues.ExposureMode;
import com.hsm.barcode.DecoderConfigValues.LightsMode;
import com.hsm.barcode.DecoderConfigValues.SymbologyFlags;
import com.hsm.barcode.DecoderConfigValues.SymbologyID;
import com.hsm.barcode.ExposureValues.ExposureSettings;
import com.hsm.barcode.DecodeWindowing.DecodeWindowLimits;
import com.hsm.barcode.DecodeWindowing.DecodeWindowMode;
import com.hsm.barcode.DecodeWindowing.DecodeWindow;
import com.hsm.barcode.DecodeWindowing.DecodeWindowShowWindow;
import com.ubx.decoder.ImageUtils;
import com.ubx.decoder.DCLProperties;
import com.ubx.propertyparser.DataParser;
import com.ubx.propertyparser.Property;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.scanner.configuration.PropertyID;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.UserHandle;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
// urovo add by shenpidong begin 2020-04-10
import android.provider.Settings;
// urovo add by shenpidong end 2020-04-10
import java.io.ByteArrayOutputStream;

public class N6603Scanner extends Scanner implements DecoderListener {
    private static final String TAG = "N6603Scanner";
    private boolean initEngine = false;
    private boolean isDecoding = false;
    private boolean isDisconnectScan6603 = true; //add by qiuzhoujun
    private boolean isWaitDecodeReturned = true;//add by qiuzhoujun
    private Decoder m_decDecoder;
    private boolean isHS7Engine = false;
    private static int g_nDecodeTimeout = 10000;
    // urovo add shenpidong begin 2019-04-11
    private static boolean isErrNoImage = false;
    // urovo add shenpidong end 2019-04-11
    // urovo add shenpidong begin 2020-03-11
    private static boolean isMultiDecodeMode = false;
    private static int isMultiDecodeCount = 0;
    // urovo add shenpidong end 2020-03-11
    // urovo add shenpidong begin 2020-03-23
    private static int isFullReadMode = 1; // value:0 1 , default 1
    // urovo add shenpidong end 2020-03-23
    DecodeResult m_decResult;
    private int g_nImageWidth;
    private int g_nImageHeight;
    //连续扫描模式
    boolean isContinueScanMode = false;
    private final Object mDecodeLock = new Object();

    // urovo add shenpidong begin 2019-09-12
    private byte[] lastImageData = null;
    private boolean enableLastDecImage = false;
    private boolean saveLastDecImage = false;
    private static int rotateImage = 0;
    private int compressJpegQuality = 50;
    //1.CLSI Editing 启用时，此参数将删除起始字符和结束字符，并在14个字符的Codabar符号的第一个、第五个和第十个字符后插入空格。符号长度不包括开始和结束字符。
    //2.NOTIS Editing 开启该功能后，解码器会忽略起始位和结束位
    private boolean CLSI_enable = false;
    //默认输出开始结束字母
    private boolean NOTIS_enable = true;
    public N6603Scanner(ScanServiceWrapper scanService, int type) {
        mScannerType = type;//ScannerFactory.TYPE_N6603;
        rotateImage = mScannerType == ScannerFactory.TYPE_N6603 ? 180:0;
        mScanService = scanService;
        m_decResult = new DecodeResult();
        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            mPropIndexHashMap.put(INTERNAL_PROPERTY_INDEX[i], VALUE_PARAM_INDEX[i]);
        }
        mPropIndexHashMap.put(PropertyID.SPECIFIC_CODE_GS, 0);
        mWorkHandlerThread = new WorkHandlerThread(type == ScannerFactory.TYPE_N6603 ? "N6603" : (type == ScannerFactory.TYPE_N6703 ? "N6703" : "EX30"));
        mWorkHandlerThread.startThread(mDecodeLock);
        IntentFilter filter = new IntentFilter();
        filter.addAction("action.scanner_capture_image");
        filter.addAction("com.ubx.barcode.action_config");
        mScanService.getContext().registerReceiver(mReceiver, filter);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("action.scanner_capture_image".equals(action)) {
                if (mWorkHandler != null) {
                    Message m = Message.obtain(mWorkHandler, WorkHandler.MESSAGE_CAPTURE_IMAGE);
                    Bundle bundle = intent.getExtras();
                    m.obj = bundle;
                    mWorkHandler.sendMessage(m);
                }
            } else {
                if (m_decDecoder != null) {
                    try {
                        int property = intent.getIntExtra("property", 0);
                        int value = intent.getIntExtra("value", 0);
                        Log.d(TAG, "property=" + property + " value " + value);
                        if (property > 0) {
                            m_decDecoder.setDecodeParameter(property, value);
                        }
                    } catch (DecoderException e) {
                        HandleDecoderException(e);
                    }
                }
            }
        }
    };

    private void showAbout() {
        Log.d(TAG, "ShowAbout++");
        if (m_decDecoder == null) {
            return;
        }
        try {
            // Decoder Rev's are very long, so we'll shorten them (to year.month.rev):
            String strDecoderRevFullString = m_decDecoder.getDecoderRevision();
            String strDclRevFullString = m_decDecoder.getControlLogicRevision();
            String strDecoderRevSubString = strDecoderRevFullString.substring(
                    strDecoderRevFullString.indexOf(":") + 1, strDecoderRevFullString.length() - 1);
            String strDclRevSubString = strDclRevFullString.substring(
                    strDclRevFullString.indexOf(":") + 1, strDclRevFullString.length() - 1);
            Log.d(TAG, "Get imager props...");
            // Get Imager properties:
            ImagerProperties imgProp = new ImagerProperties();
            m_decDecoder.getImagerProperties(imgProp);
            if(mScannerType != ScannerFactory.TYPE_N6603) {
                if(imgProp.Columns > 0 && imgProp.Rows > 0) {
                    //(HS7)5703 800X1280
                    if(imgProp.Columns < imgProp.Rows) {
                        rotateImage = 90;
                        isHS7Engine = true;
                    }
                }
            }
            Log.d(TAG, "...Return from imager props" + m_decDecoder.getScanDriverRevision());
            String version = "== Engine Information ==" + "\nEngineID: 0x"
                    + Integer.toHexString(imgProp.FirmwareEngineID) + " (" + m_decDecoder.getEngineID()
                    + ")" + "\nS/N: " + m_decDecoder.getEngineSerialNumber() + "\nPSoC Rev: "
                    + m_decDecoder.getPSOCMajorRev() + "." + m_decDecoder.getPSOCMinorRev()
                    + "\nCols: " + imgProp.Columns + " Rows: " + imgProp.Rows + "\nAimerType: "
                    + imgProp.AimerType + " Optics: " + imgProp.Optics
                    + "\n\n== Revision Information ==" + "\nAPI: " + m_decDecoder.getAPIRevision()
                    + "\nDecoder: " + strDecoderRevSubString
                    + "\nDCL: " + strDclRevSubString
                    + "\nScan Driver: " + m_decDecoder.getScanDriverRevision();

            Log.d(TAG, "ShowAbout--" + version);
        } catch (DecoderException e) {
            HandleDecoderException(e);
        }
    }

    @Override
    public void setDefaults() {
        // TODO Auto-generated method stub
        try {
            g_nMultiReadResultCount = 0;
            if (m_decDecoder != null) {
                close();
                m_decDecoder = new Decoder();
                m_decDecoder.connectDecoderLibrary();
                isDisconnectScan6603 = false;//add by qiuzhoujun
                // urovo add shenpidong begin 2019-07-21
                waitMultipleMode = 0;
                // urovo add shenpidong end 2019-07-21
                // urovo add shenpidong begin 2020-02-14
                waitMultipleInterval = 10;
                waitMultipleTiemOut = 5 * 1000;
                // urovo add shenpidong end 2020-02-14
                // urovo add by shenpidong begin 2020-03-17
                isMultiDecodeMode = false;
                isMultiDecodeCount = 0;
                // urovo add shenpidong begin 2020-03-23
                isFullReadMode = 1;
                mFullReadModeDecodeResult = null;
                // urovo add shenpidong end 2020-03-23
                currentDecodeDataArr = null;
                preDecodeDataArr = null;
                currentDecodeDataArrIndex = 0;
                // urovo add by shenpidong end 2020-03-17
                preDecodeData = "";
                delayDecodeTime = 0;
                delayPicklistAimMode = false;
                if (m_decDecoder != null) {
                    // urovo add shenpidong begin 2019-07-21
                    m_decDecoder.setDecoderListeners(this);
                    m_decDecoder.setDecodeWindowMode(DecodeWindowMode.DECODE_WINDOW_MODE_DISABLED);
                    m_decDecoder.setLightsMode(LightsMode.ILLUM_AIM_ON);
                    m_decDecoder.setExposureMode(ExposureMode.HHP);
                    // urovo add shenpidong begin 2019-04-11
                    // urovo add 0x1a002002 shenpidong begin 2019-03-18
                    // 0x1a002002 This property controls the reading tolerance of the decoder.
                    // 0:Very high reading tolerance :this is the most permissive mode. When enabled, the scanner reads codes of variable quality.
                    // 1:High reading tolerance
                    // 2:Medium reading tolerance: this mode allows medium permissiveness(recommended)
                    // 3:Low reading tolerance: this is the least permissive mode
                    m_decDecoder.setDecodeParameter(0x1a002002, 2); //
                    // urovo add 0x1a002002 shenpidong end 2019-03-18
                    m_decDecoder.setDecodeParameter(0x40005025, 1); // Enhance the broken bar code
                    m_decDecoder.setDecodeParameter(0x40010905, 1);
//		            m_decDecoder.setDecodeParameter(0x1a001007, 1); // Combine pieces of composite codes before issuing result. value is 0 or 1
                }
                setOcrSettings();
                if (m_decDecoder != null) {
//		            m_decDecoder.setDecodeParameter(0x40005025 , 1); // Enhance the broken bar code
                    isErrNoImage = false;
                    // 0x1a161001 decoding DotCode , 0---default value not decoding DotCode , 1---enable decoding DotCode
//				    m_decDecoder.setDecodeParameter(0x1a161001 , 1);
                    try {
                        DecodeOptions decOpt2 = new DecodeOptions();
                        m_decDecoder.getDecodeOptions(decOpt2);
                        decOpt2.VideoReverse = -1;
                        // urovo add shenpidong begin 2019-07-24
                        if (mScannerType == ScannerFactory.TYPE_N6703) {
                            decOpt2.DecAttemptLimit = 500;
                        } else {
                            decOpt2.DecAttemptLimit = -1;
                        }
                        m_decDecoder.setDecodeOptions(decOpt2);
                    } catch (DecoderException e) {
                        Log.d(TAG, "setDefaults DecodeOptions open decOpt2.VideoReverse");
                        HandleDecoderException(e);
                    }
                }
            }
        } catch (DecoderException e) {
            HandleDecoderException(e);
            return;
        }
        //setProperties(null);
        //close();
        //open();
        //mWorkHandler.sendEmptyMessageDelayed(WorkHandler.MESSAGE_DECODE_CONFIG, 1000);
    }

    @Override
    public boolean open() {
        // TODO Auto-generated method stub
        try {
            synchronized (mDecodeLock) {
                m_decDecoder = new Decoder();
                m_decDecoder.connectDecoderLibrary();
                isDisconnectScan6603 = false;//add by qiuzhoujun
                // urovo add shenpidong begin 2019-04-12
                isErrNoImage = false;
                // urovo add shenpidong end 2019-04-12
                // urovo add shenpidong begin 2019-02-18
                // The lack of a SYMBOLOGY_CHECK_TRANSMIT when open scan EAN13/EAN8
                SetSymbologySettings(SymbologyID.SYM_EAN13);
                SetSymbologySettings(SymbologyID.SYM_EAN8);
                // urovo add shenpidong end 2019-02-18
                //getDecodingWindow();
                //getExposureSettings();
                // urovo add by shenpidong begin 2020-03-17
                int delayTime = mScanService.getPropertyInt(PropertyID.DEC_PICKLIST_AIM_DELAY);
                if (delayTime >= 0 && delayTime <= 4000) {
                    delayDecodeTime = delayTime;
                }
                int aimMode = mScanService.getPropertyInt(PropertyID.DEC_PICKLIST_AIM_MODE);
                delayPicklistAimMode = aimMode == 1;
                int decode_mode = mScanService.getPropertyInt(PropertyID.MULTI_DECODE_MODE);
                isMultiDecodeMode = decode_mode == 1;
                isMultiDecodeCount = 0;
                g_nMultiReadResultCount = 0;
                waitMultipleMode = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_MODE);
                Log.d(TAG, "waitMultipleMode " + waitMultipleMode);
                if (isMultiDecodeMode || waitMultipleMode > 0) {
                    g_nMaxMultiReadCount = mScanService.getPropertyInt(PropertyID.DEC_MaxMultiRead_COUNT);
                    isMultiDecodeCount = mScanService.getPropertyInt(PropertyID.BAR_CODES_TO_READ);
                    // urovo add shenpidong begin 2020-03-23
                    isFullReadMode = mScanService.getPropertyInt(PropertyID.FULL_READ_MODE);
                    // urovo add shenpidong end 2020-03-23
                    if (g_nMaxMultiReadCount <= 0 || isMultiDecodeCount <= 0) {
                        g_nMaxMultiReadCount = 2;
                        isMultiDecodeCount = 2;
                    }
                    setDecodeOptions();
                } else {
                    m_decDecoder.setDecoderListeners(this);
                    waitMultipleMode = 0;
                    currentDecodeDataArr = null;
                    preDecodeDataArr = null;
                    currentDecodeDataArrIndex = 0;
                    // urovo add shenpidong begin 2020-03-23
                    isFullReadMode = 1;
                    mFullReadModeDecodeResult = null;
                    // urovo add shenpidong end 2020-03-23
                }
                // urovo add by shenpidong end 2020-03-17
                if (mScanService.getPropertyInt(PropertyID.DEC_2D_CENTERING_ENABLE) == 1) {
                    setDecodingSettings();
                }
                preDecodeData = "";
                // urovo add shenpidong begin 2019-09-12
                waitMultipleStartTime = -1;
                // urovo add shenpidong end 2019-09-12
                waitMultipleTiemOut = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_TIMEOUT);
                if (waitMultipleTiemOut < 50) {
                    waitMultipleTiemOut = 5 * 1000;
                }
                // urovo add shenpidong begin 2019-09-12
                // urovo tao.he add begin
                waitMultipleInterval = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_INTERVAL);
                if (waitMultipleInterval <= 10) {
                    waitMultipleInterval = 15;
                }
                // urovo tao.he add end
                //getOcrSettings();
                setOcrSettings();
                g_nImageWidth = m_decDecoder.getImageWidth();
                g_nImageHeight = m_decDecoder.getImageHeight();
                if (m_decDecoder != null) {
                    m_decDecoder.setLightsMode(mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE));
                    if (mScanService.getPropertyInt(PropertyID.IMAGE_EXPOSURE_MODE) == 1) {
                        m_decDecoder.setExposureMode(ExposureMode.FIXED);
                        int valueEx = mScanService.getPropertyInt(PropertyID.IMAGE_FIXED_EXPOSURE);
                        valueEx = (valueEx <= 0 ? 200 : valueEx);
                        if(isHS7Engine) {
                            //一帧图16ms. 曝光时间过大，不会点亮激光。比如exp time 120 120*127=15.240ms,限制最大值30
                            valueEx = (valueEx > 100 ? 100 : valueEx);
                        }
                        int g_nExposureSettings[] = {
                                ExposureSettings.DEC_ES_FIXED_EXP, valueEx,
                                //ExposureSettings.DEC_ES_MAX_EXP, 0,
                        };
                        m_decDecoder.setExposureSettings(g_nExposureSettings);
                        m_decDecoder.setExposureMode(ExposureMode.FIXED);
                    }
                    // urovo add shenpidong begin 2019-04-11
                    // urovo add 0x1a002002 shenpidong begin 2019-03-18
                    // 0x1a002002 This property controls the reading tolerance of the decoder.
                    // 0:Very high reading tolerance :this is the most permissive mode. When enabled, the scanner reads codes of variable quality.
                    // 1:High reading tolerance
                    // 2:Medium reading tolerance: this mode allows medium permissiveness(recommended)
                    // 3:Low reading tolerance: this is the least permissive mode
//		                    m_decDecoder.setDecodeParameter(0x1a002002 , 2);  //
                    // urovo add 0x1a002002 shenpidong end 2019-03-18
                    setDecodeParameter(0x40005025, 1); // Enhance the broken bar code
                    setDecodeParameter(0x40010905, 1);
                    // 0x1a161001 decoding DotCode , 0---default value not decoding DotCode , 1---enable decoding DotCode
//				    m_decDecoder.setDecodeParameter(0x1a161001 , 1);
                    DecodeOptions decOpt2 = new DecodeOptions();
                    try {
                        m_decDecoder.getDecodeOptions(decOpt2);
                        decOpt2.VideoReverse = -1;
                        // urovo add shenpidong begin 2019-07-24
                        if (mScannerType == ScannerFactory.TYPE_N6703) {
                            decOpt2.DecAttemptLimit = 500;
                        } else {
                            decOpt2.DecAttemptLimit = -1;
                        }
                        // urovo add shenpidong end 2019-07-24
                        //				    decOpt2.SearchLimit = 100;
                        m_decDecoder.setDecodeOptions(decOpt2);
                    } catch (DecoderException e) {
                        Log.d(TAG, "DecodeOptions open decOpt2.VideoReverse");
                        HandleDecoderException(e);
                    }
                    // urovo add shenpidong end 2019-04-11
                }
                //打开扫描头时加载无法通过界面设置解码库的参数,检查sdcard目录下是否有property.json文件
                boolean hasParse = (advPropertyLists != null && advPropertyLists.size() > 0) ? true:false;
                int exists = DataParser.checkPropertyFileExists(hasParse);
                if(exists > 0) {
                    advPropertyLists = DataParser.parseDecoderPropertyFromJSONFile(mScannerType, exists, false);
                }
                if(advPropertyLists != null && advPropertyLists.size() > 0) {
                    for(Property property: advPropertyLists) {
                        if(m_decDecoder != null) {
                            int val = -1;
                            try{
                                val = Integer.parseInt(property.getValue());
                            } catch(NumberFormatException w){}
                            if(val > -1) {
                                try {
                                    m_decDecoder.setDecodeParameter(property.getParamNum(), val);
                                } catch (DecoderException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
            if (!initEngine) {
                initEngine = true;
                showAbout();
            }
        } catch (DecoderException e) {
            HandleDecoderException(e);
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    // urovo modify shenpidong begin 2019-04-11
    @Override
    public void close() {
        // TODO Auto-generated method stub
        try {
            Log.d(TAG, "close ..... m_decDecoder:" + (m_decDecoder != null));
            g_nMultiReadResultCount = 0;
            if (m_decDecoder != null) {
                //m_decDecoder.stopScanning(); //for Failed to communicate with the engine device driver
                Message m = Message.obtain(mWorkHandler, WorkHandler.MESSAGE_DECODE_STOP);
                mWorkHandler.sendMessage(m);
                isDecoding = false;
                isErrNoImage = false;
//                stopDecode();
                Log.d(TAG, "close Decoder isWaitDecodeReturned:" + isWaitDecodeReturned);
                isDisconnectScan6603 = true; //add by qiuzhoujun
                if (!isWaitDecodeReturned) {
                    try {
                        Thread.currentThread().sleep(300);
                    } catch (InterruptedException e) {
                    }
                }
//                while(!isWaitDecodeReturned); //add by qiuzhoujun
                synchronized (mDecodeLock) {
                    Log.d(TAG, "close Decoder m_decDecoder:" + (m_decDecoder != null));
                    if (m_decDecoder != null) {
                        m_decDecoder.disconnectDecoderLibrary();
                    }
                    isWaitDecodeReturned = true;
                    Log.d(TAG, "close Decoder isDecoding:" + isDecoding);
                    isDecoding = false;

                }
//                isDisconnectScan6603 = true; //add by qiuzhoujun
                //Thread.currentThread().sleep(300);
                m_decDecoder = null;
            }
        } catch (DecoderException e) {
            HandleDecoderException(e);
        }
        isDecoding = false;
        enableLastDecImage = false;
    }
    // urovo modify shenpidong begin 2019-04-11

    @Override
    public void startDecode(int timeout) {
        // TODO Auto-generated method stub
        isContinueScanMode = mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == 4;
        g_nDecodeTimeout = timeout;
        Message m = Message.obtain(mWorkHandler, WorkHandler.MESSAGE_DECODE_START);
        mWorkHandler.sendMessage(m);
    }

    @Override
    public void stopDecode() {
        // TODO Auto-generated method stub
        Log.d(TAG, "stopDecode() " + isDecoding);
        if (isDecoding) {
            // urovo add stop scan. shenpidong begin 2019-03-27
            isDecoding = false;
            // urovo add stop scan. shenpidong end 2019-03-27
/*
            try {
                if (m_decDecoder != null){
                    synchronized(mDecodeLock){
                        m_decDecoder.stopScanning();
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
*/
        }
        Message m = Message.obtain(mWorkHandler, WorkHandler.MESSAGE_DECODE_STOP);
        mWorkHandler.sendMessage(m);
        isDecoding = false;
    }

    @Override
    public void openPhoneMode() {
        // TODO Auto-generated method stub
    }

    @Override
    public void closePhoneMode() {
        // TODO Auto-generated method stub
    }

    @Override
    public int setProperties(SparseArray<Integer> property) {
        // TODO Auto-generated method stub
        if (null != m_decDecoder) {
            if (property != null) {
                int size = property.size();
                Log.d(TAG, "setProperties property size= " + size);

                int symID = -1;
                int flags = 0;
                boolean bNotSupported = false;
                for (int i = 0; i < size; i++) {
                    bNotSupported = false;
                    int keyForIndex = property.keyAt(i);
                    int internalIndex = mPropIndexHashMap.get(keyForIndex, SPECIAL_VALUE);
                    //Log.d(TAG, "setProperties keyForIndex:" + keyForIndex + ",internalIndex:" + internalIndex);
                    if (internalIndex != SPECIAL_VALUE) {
                        //int value = property.get(keyForIndex);
                        switch (keyForIndex) {
                            case PropertyID.IMAGE_ONE_D_INVERSE:
                                int value = property.get(keyForIndex);
                                if (value >= 0 && value < 3) {
                                    try {
                                        m_decDecoder.setDecodeParameter(0x1a00102b, value);
                                    } catch (DecoderException e) {
                                        Log.d(TAG, "DecodeOptions EXCEPTION keyForIndex:" + keyForIndex);
                                        HandleDecoderException(e);
                                    }
                                } else {
                                    Log.d(TAG, "setProperties error!!! value:" + value);
                                }
                                bNotSupported = true;
                                break;
                            case PropertyID.DEC_PICKLIST_AIM_MODE: {
                                int aimMode = property.get(keyForIndex);
                                delayPicklistAimMode = aimMode == 1;
                                bNotSupported = true;
                            }
                            break;
                            case PropertyID.DEC_PICKLIST_AIM_DELAY: {
                                int time = property.get(keyForIndex);
                                if (time >= 0 && time <= 4000) {
                                    delayDecodeTime = time;
                                } else if (time < 0) {
                                    delayDecodeTime = 0;
                                } else if (time > 4000) {
                                    delayDecodeTime = 4000;
                                }
                                bNotSupported = true;
                            }
                            break;
                            case PropertyID.SPECIFIC_CODE_GS:
                                int value1 = property.get(keyForIndex);
                                Log.d(TAG, "setProperties SPECIFIC_CODE_GS!!! value:" + value1);
                                try {
                                    if (value1 == 1)
                                        m_decDecoder.setDecodeParameter(0x1a014006, 0xf1);
                                    else
                                        m_decDecoder.setDecodeParameter(0x1a014006, 0x1d);
                                } catch (DecoderException e) {
                                    Log.d(TAG, "DecodeOptions EXCEPTION keyForIndex:" + keyForIndex);
                                    HandleDecoderException(e);
                                }
                                break;
                            // add by tao.he begin, 20190505
                            case PropertyID.LINEAR_CODE_TYPE_SECURITY_LEVEL: {
                                try {
                                    int value2 = property.get(keyForIndex) - 1;
                                    m_decDecoder.setDecodeParameter(0x1a002002, value2);
                                    Log.d(TAG, "setProperties LINEAR_CODE_TYPE_SECURITY_LEVEL value:" + value2);
                                } catch (DecoderException e) {
                                    Log.d(TAG, "DecodeOptions EXCEPTION keyForIndex:" + keyForIndex);
                                    HandleDecoderException(e);
                                }
                                break;
                            }
                            // add by tao.he end, 20190505
                            case PropertyID.IMAGE_PICKLIST_MODE:
                            case PropertyID.LASER_ON_TIME:
                            case PropertyID.TIMEOUT_BETWEEN_SAME_SYMBOL:
                                bNotSupported = true;
                                break;
                            // urovo add by shenpidong begin 2020-03-17
                            case PropertyID.MULTI_DECODE_MODE:
                                int decode_mode = mScanService.getPropertyInt(PropertyID.MULTI_DECODE_MODE);
                                Log.d(TAG, "setProperties MULTI_DECODE_MODE:" + decode_mode);
                                isMultiDecodeMode = decode_mode == 1;
                                if (isMultiDecodeMode) {
                                    if (waitMultipleMode > 0) {
                                        g_nMaxMultiReadCount = mScanService.getPropertyInt(PropertyID.DEC_MaxMultiRead_COUNT);
                                    }
                                    isMultiDecodeCount = mScanService.getPropertyInt(PropertyID.BAR_CODES_TO_READ);
                                }
                                setDecodeOptions();
                                bNotSupported = true;
                                break;
                            case PropertyID.BAR_CODES_TO_READ:
                                int multi_decode = mScanService.getPropertyInt(PropertyID.BAR_CODES_TO_READ);
                                Log.d(TAG, "setProperties multi_decode:" + multi_decode);
                                if (multi_decode <= 0) {
                                    multi_decode = 2;
                                }
                                isMultiDecodeCount = multi_decode;
                                setDecodeOptions();
                                bNotSupported = true;
                                break;
                            // urovo add by shenpidong end 2020-03-17
                            // urovo add by shenpidong begin 2020-03-23
                            case PropertyID.FULL_READ_MODE:
                                int fullReadMode = mScanService.getPropertyInt(PropertyID.FULL_READ_MODE);
//                            int value3 = property.get(keyForIndex);
                                Log.d(TAG, "setProperties FULL_READ_MODE fullReadMode:" + fullReadMode);
                                // urovo add shenpidong begin 2020-03-23
                                isFullReadMode = fullReadMode;
                                // urovo add shenpidong end 2020-03-23
                                bNotSupported = true;
                                break;
                            // urovo add by shenpidong end 2020-03-23
                            // Flag & Range:
                            case PropertyID.AZTEC_ENABLE:       //aztec code
                            case PropertyID.AZTEC_INVERSE:
                                symID = SymbologyID.SYM_AZTEC;
                                break;
                            case PropertyID.CODABAR_ENABLE:           //codebar
                            case PropertyID.CODABAR_NOTIS:
                            case PropertyID.CODABAR_CLSI:
                            case PropertyID.CODABAR_LENGTH1:
                            case PropertyID.CODABAR_LENGTH2:
                                symID = SymbologyID.SYM_CODABAR;
                                break;
                            case PropertyID.CODE11_ENABLE:
                            case PropertyID.CODE11_ENABLE_CHECK:
                            case PropertyID.CODE11_SEND_CHECK:
                            case PropertyID.CODE11_LENGTH1:
                            case PropertyID.CODE11_LENGTH2:
                                symID = SymbologyID.SYM_CODE11;
                                break;
                            case PropertyID.CODE128_ENABLE:       //code128
                            case PropertyID.CODE128_LENGTH1:
                            case PropertyID.CODE128_LENGTH2:
                                symID = SymbologyID.SYM_CODE128;
                                break;
                            case PropertyID.CODE128_GS1_ENABLE:       //gs1-128
                                symID = SymbologyID.SYM_GS1_128;
                                break;
                            case PropertyID.CODE39_ENABLE:        //Code39 definitions
                            case PropertyID.CODE39_ENABLE_CHECK:
                            case PropertyID.CODE39_SEND_CHECK:
                            case PropertyID.CODE39_FULL_ASCII:
                            case PropertyID.CODE39_LENGTH1:
                            case PropertyID.CODE39_LENGTH2:
                                symID = SymbologyID.SYM_CODE39;
                                break;
                            case PropertyID.CODE93_ENABLE:        //code 93
                            case PropertyID.CODE93_LENGTH1:
                            case PropertyID.CODE93_LENGTH2:
                                symID = SymbologyID.SYM_CODE93;
                                break;
                            case PropertyID.COMPOSITE_CC_AB_ENABLE:     //composite-cc_ab
                            case PropertyID.COMPOSITE_CC_C_ENABLE:    //composite-cc_c
                                if (PropertyID.COMPOSITE_CC_AB_ENABLE == keyForIndex && mScanService.getPropertyInt(PropertyID.GS1_14_ENABLE) == 0) {
                                    SetSymbologySettings(SymbologyID.SYM_RSS);
                                }
                                symID = SymbologyID.SYM_COMPOSITE;
                                break;
                            case PropertyID.DATAMATRIX_ENABLE:        //datamatrix
                            case PropertyID.DATAMATRIX_LENGTH1:
                            case PropertyID.DATAMATRIX_LENGTH2:
                            case PropertyID.DATAMATRIX_INVERSE:
                                symID = SymbologyID.SYM_DATAMATRIX;
                                break;
                            case PropertyID.I25_ENABLE:           //interleaved 2/5
                            case PropertyID.I25_ENABLE_CHECK:
                            case PropertyID.I25_SEND_CHECK:
                            case PropertyID.I25_LENGTH1:
                            case PropertyID.I25_LENGTH2:
                            case PropertyID.I25_TO_EAN13:
                                symID = SymbologyID.SYM_INT25;
                                break;
                            case PropertyID.MAXICODE_ENABLE:          //maxicode
                                symID = SymbologyID.SYM_MAXICODE;
                                break;
                            case PropertyID.QRCODE_ENABLE:            //qrcode
                            case PropertyID.QRCODE_INVERSE:
                            case PropertyID.MICROQRCODE_ENABLE:
                                symID = SymbologyID.SYM_QR;
                                break;
                            case PropertyID.MICROPDF417_ENABLE:       //micro pdf417
                                symID = SymbologyID.SYM_MICROPDF;
                                break;
                            case PropertyID.PDF417_ENABLE:
                                symID = SymbologyID.SYM_PDF417;
                                break;
                            case PropertyID.GS1_14_ENABLE:            //rss
                            case PropertyID.GS1_14_TO_UPC_EAN:
                            case PropertyID.GS1_LIMIT_ENABLE:         //rss limit
                            case PropertyID.GS1_EXP_ENABLE:           //rss exp
                            case PropertyID.GS1_EXP_LENGTH1:
                            case PropertyID.GS1_EXP_LENGTH2:
                                symID = SymbologyID.SYM_RSS;
                                break;
                            case PropertyID.D25_ENABLE:           //discrete 2/5
                            case PropertyID.D25_LENGTH1:
                            case PropertyID.D25_LENGTH2:
                                symID = SymbologyID.SYM_STRT25;
                                break;
                            case PropertyID.MSI_ENABLE:               //msi
                            case PropertyID.MSI_REQUIRE_2_CHECK:
                            case PropertyID.MSI_SEND_CHECK:
                            case PropertyID.MSI_CHECK_2_MOD_11:
                            case PropertyID.MSI_LENGTH1:
                            case PropertyID.MSI_LENGTH2:
                                symID = SymbologyID.SYM_MSI;
                                break;
                            case PropertyID.C25_ENABLE:
                                symID = SymbologyID.SYM_CHINAPOST;
                                break;
                            case PropertyID.M25_LENGTH1:
                            case PropertyID.M25_LENGTH2:
                            case PropertyID.M25_ENABLE:           //matrix 2/5
                                symID = SymbologyID.SYM_MATRIX25;
                                break;
                            case PropertyID.HANXIN_ENABLE:
                            case PropertyID.HANXIN_INVERSE:
                                symID = SymbologyID.SYM_HANXIN;
                                break;
                            case PropertyID.EAN8_ENABLE:          //ean8
                            case PropertyID.EAN8_TO_EAN13:
                                symID = SymbologyID.SYM_EAN8;
                                break;
                            case PropertyID.EAN13_ENABLE:         //ean13
                            case PropertyID.EAN13_BOOKLANDEAN:
                            case PropertyID.EAN13_BOOKLAND_FORMAT:
                                symID = SymbologyID.SYM_EAN13;
                                break;
                            case PropertyID.US_POSTNET_ENABLE:        //postal code
                            case PropertyID.US_POSTAL_SEND_CHECK:
                                symID = SymbologyID.SYM_POSTNET;
                                break;
                            case PropertyID.UPCA_ENABLE:          //uspa
                            case PropertyID.UPCA_SEND_CHECK:
                            case PropertyID.UPCA_SEND_SYS:
                            case PropertyID.UPCA_TO_EAN13:
                                symID = SymbologyID.SYM_UPCA;
                                break;
                            case PropertyID.UPCE_ENABLE:      //uspe
                            case PropertyID.UPCE_SEND_CHECK:
                            case PropertyID.UPCE_SEND_SYS:
                            case PropertyID.UPCE_TO_UPCA:
                                symID = SymbologyID.SYM_UPCE0;
                                break;
                            case PropertyID.UPCE1_ENABLE:
                            case PropertyID.UPCE1_SEND_CHECK:
                            case PropertyID.UPCE1_SEND_SYS:
                            case PropertyID.UPCE1_TO_UPCA:
                                symID = SymbologyID.SYM_UPCE1;
                                break;
                            case PropertyID.CODE_ISBT_128:
                                symID = SymbologyID.SYM_ISBT;
                                break;
                            case PropertyID.COMPOSITE_TLC39_ENABLE:
                                symID = SymbologyID.SYM_TLCODE39;
                                break;
                            case PropertyID.AUSTRALIAN_POST_ENABLE:
                                symID = SymbologyID.SYM_AUSPOST;
                                break;
                            case PropertyID.US_PLANET_ENABLE:
                                symID = SymbologyID.SYM_PLANET;
                                break;
                            case PropertyID.TRIOPTIC_ENABLE:      //trioptic
                                symID = SymbologyID.SYM_TRIOPTIC;
                                break;
                            case PropertyID.CODE32_ENABLE:      //code 32 also see pharmacode 39
                            case PropertyID.CODE32_SEND_START:
                                symID = SymbologyID.SYM_CODE32;
                                break;
                            case PropertyID.USPS_4STATE_ENABLE:
                                symID = SymbologyID.SYM_USPS4CB;
                                break;
                            case PropertyID.UPU_FICS_ENABLE:
                                symID = SymbologyID.SYM_IDTAG;
                                break;
                            case PropertyID.ROYAL_MAIL_ENABLE:
                            case PropertyID.ROYAL_MAIL_SEND_CHECK:
                                symID = SymbologyID.SYM_BPO;
                                break;
                            case PropertyID.KIX_CODE_ENABLE:
                                symID = SymbologyID.SYM_DUTCHPOST;
                                break;
                            case PropertyID.JAPANESE_POST_ENABLE:
                                symID = SymbologyID.SYM_JAPOST;
                                break;
                            case PropertyID.DEC_2D_LIGHTS_MODE: {
                                try {
                                    int mode = mScanService.getPropertyInt(PropertyID.DEC_2D_LIGHTS_MODE);
                                    m_decDecoder.setLightsMode(mode < 0 ? 3 : mode);
                                    //m_decDecoder.setExposureMode(mode);
                                    //if (mScanService.getPropertyInt(PropertyID.IMAGE_EXPOSURE_MODE) == 1) {
                                    //    setExposureSettings(ExposureMode.FIXED);
                                    //}
                                } catch (DecoderException e) {
                                    // TODO Auto-generated catch block
                                    HandleDecoderException(e);
                                }
                                bNotSupported = true;
                            }
                            break;
                            case PropertyID.DEC_OCR_MODE:
                            case PropertyID.DEC_OCR_TEMPLATE:
                            case PropertyID.DEC_OCR_USER_TEMPLATE: {
                            /*try {
                                setOcrSettings();
                            } catch (DecoderException e) {
                                // TODO Auto-generated catch block
                                HandleDecoderException(e);
                            }*/
                                int ocr_mode = mScanService.getPropertyInt(PropertyID.DEC_OCR_MODE);
                                if (ocr_mode > 0) {
                                    close();
                                    open();
                                    mWorkHandler.sendEmptyMessageDelayed(WorkHandler.MESSAGE_DECODE_CONFIG, 1000);
                                } else {
                                    try {
                                        setOcrSettings();
                                    } catch (DecoderException e) {
                                        // TODO Auto-generated catch block
                                        HandleDecoderException(e);
                                    }
                                }
                            }
                            break;
                            case PropertyID.DEC_2D_CENTERING_ENABLE:
                            case PropertyID.DEC_2D_CENTERING_MODE:
                            case PropertyID.DEC_2D_WINDOW_UPPER_LX:
                            case PropertyID.DEC_2D_WINDOW_UPPER_LY:
                            case PropertyID.DEC_2D_WINDOW_LOWER_RX:
                            case PropertyID.DEC_2D_WINDOW_LOWER_RY:
                            case PropertyID.DEC_2D_DEBUG_WINDOW_ENABLE:
                                setDecodingSettings();
                                bNotSupported = true;
                                break;
                            /*case PropertyID.DEC_ES_EXPOSURE_METHOD:
                            case PropertyID.DEC_ES_TARGET_VALUE:
                            case PropertyID.DEC_ES_TARGET_PERCENTILE:
                            case PropertyID.DEC_ES_TARGET_ACCEPT_GAP:
                            case PropertyID.DEC_ES_MAX_EXP:
                            case PropertyID.DEC_ES_MAX_GAIN:
                            case PropertyID.DEC_ES_FRAME_RATE:
                            case PropertyID.DEC_ES_CONFORM_IMAGE:
                            case PropertyID.DEC_ES_CONFORM_TRIES:
                            case PropertyID.DEC_ES_SPECULAR_EXCLUSION:
                            case PropertyID.DEC_ES_SPECULAR_SAT:
                            case PropertyID.DEC_ES_SPECULAR_LIMIT:
                            case PropertyID.DEC_ES_FIXED_GAIN:
                            case PropertyID.DEC_ES_FIXED_FRAME_RATE:
                            case PropertyID.IMAGE_FIXED_EXPOSURE:
                                if (mScanService.getPropertyInt(PropertyID.IMAGE_EXPOSURE_MODE) == 1) {
                                    setExposureSettings(ExposureMode.FIXED);
                                }
                                bNotSupported = true;
                                break;*/
                            case PropertyID.IMAGE_FIXED_EXPOSURE:
                            case PropertyID.IMAGE_EXPOSURE_MODE:
                                try {
                                    int expMode = mScanService.getPropertyInt(PropertyID.IMAGE_EXPOSURE_MODE);
                                    //Log.d(TAG, "setProperties ExposureSettings property= " + property + " expMode=" + expMode + "getExposureMode= " + m_decDecoder.getExposureMode());
                                    if (expMode == 1) {
                                        //setExposureSettings(ExposureMode.FIXED);
                                        m_decDecoder.setExposureMode(ExposureMode.FIXED);
                                        value = mScanService.getPropertyInt(PropertyID.IMAGE_FIXED_EXPOSURE);
                                        value = (value <= 0 ? 200 : value);
                                        if(isHS7Engine) {
                                            //一帧图16ms. 曝光时间过大，不会点亮激光。比如exp time 120 120*127=15.240ms,限制最大值30
                                            value = (value > 100 ? 100 : value);
                                        }
                                        int g_nExposureSettings[] = {
                                                ExposureSettings.DEC_ES_FIXED_EXP, value,
                                                //ExposureSettings.DEC_ES_MAX_EXP, 0,
                                        };
                                        //Log.d(TAG, "setProperties ExposureSettings DEC_ES_FIXED_EXP value= " + value);
                                        m_decDecoder.setExposureSettings(g_nExposureSettings);
                                        m_decDecoder.setExposureMode(ExposureMode.FIXED);
                                    } else {
                                        if(keyForIndex == PropertyID.IMAGE_EXPOSURE_MODE) {
                                            //setExposureSettings(ExposureMode.HHP);
                                            //m_decDecoder.setExposureMode(ExposureMode.HHP);
                                            int g_nExposureSettings[] = {
                                                    ExposureSettings.DEC_ES_FIXED_EXP, 200,
                                                    ExposureSettings.DEC_ES_MAX_EXP, 130,
                                            };
                                            //m_decDecoder.setExposureSettings(g_nExposureSettings);
                                            m_decDecoder.setExposureMode(ExposureMode.HHP);
                                        }
                                    }
                                } catch (DecoderException e) {
                                    // TODO Auto-generated catch block
                                    HandleDecoderException(e);
                                }
                                bNotSupported = true;
                                break;
                            case PropertyID.EAN_EXT_ENABLE_2_5_DIGIT:   //UPC/EAN Extensions definitions
                                //case PropertyID.UPC_EAN_SECURITY_LEVEL:
                                //case PropertyID.UCC_COUPON_EXT_CODE:
                                boolean enable = mScanService.getPropertyInt(PropertyID.EAN_EXT_ENABLE_2_5_DIGIT) == 1;
                                SymbologyConfig symConfig = new SymbologyConfig(0);
                                symConfig.symID = SymbologyID.SYM_UPCE0;
                                try {
                                    m_decDecoder.getSymbologyConfig(symConfig); // gets the current symConfig
                                } catch (DecoderException e) {
                                    HandleDecoderException(e);
                                }
                                symConfig.Mask = SymbologyFlags.SYM_MASK_FLAGS;
                                // enable, check transmit, sys num transmit, addenda separator, 2 digit addenda, 5 digit addenda, addenda required
                            /*flags |= SymbologyFlags.SYMBOLOGY_CHECK_ENABLE;// sharedPrefs.getBoolean("sym_upca_check_transmit_enable", false) ? SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                            flags |= SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT;//sharedPrefs.getBoolean("sym_upca_sys_num_transmit_enable", false) ? SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT : 0;
                            flags |= SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT;*/
                                flags |= mScanService.getPropertyInt(PropertyID.UPCE_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                                flags |= mScanService.getPropertyInt(PropertyID.UPCE_SEND_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT | SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                                flags |= mScanService.getPropertyInt(PropertyID.UPCE_SEND_SYS) == 1 ? SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT : 0;
                                flags |= enable ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                                flags |= enable ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
                                symConfig.Flags = flags;
                                try {
                                    m_decDecoder.setSymbologyConfig(symConfig);
                                } catch (DecoderException e) {
                                    Log.d(TAG, "1 EXCEPTION SYMID = " + symConfig.symID);
                                    HandleDecoderException(e);
                                }

                                symConfig.symID = SymbologyID.SYM_UPCA;
                                try {
                                    m_decDecoder.getSymbologyConfig(symConfig); // gets the current symConfig
                                } catch (DecoderException e) {
                                    HandleDecoderException(e);
                                }
                                symConfig.Mask = SymbologyFlags.SYM_MASK_FLAGS;
                                // enable, check transmit, sys num transmit, addenda separator, 2 digit addenda, 5 digit addenda, addenda required
                                flags |= mScanService.getPropertyInt(PropertyID.UPCA_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                                flags |= mScanService.getPropertyInt(PropertyID.UPCA_SEND_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT | SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                                flags |= mScanService.getPropertyInt(PropertyID.UPCA_SEND_SYS) == 1 ? SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT : 0;
                                flags |= enable ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                                flags |= enable ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
                                symConfig.Flags = flags;
                                try {
                                    m_decDecoder.setSymbologyConfig(symConfig);
                                } catch (DecoderException e) {
                                    Log.d(TAG, "1 EXCEPTION SYMID = " + symConfig.symID);
                                    HandleDecoderException(e);
                                }
                                symConfig.symID = SymbologyID.SYM_EAN8;
                                try {
                                    m_decDecoder.getSymbologyConfig(symConfig); // gets the current symConfig
                                } catch (DecoderException e) {
                                    HandleDecoderException(e);
                                }
                                symConfig.Mask = SymbologyFlags.SYM_MASK_FLAGS;
                                // enable, check transmit, sys num transmit, addenda separator, 2 digit addenda, 5 digit addenda, addenda required
                                flags |= mScanService.getPropertyInt(PropertyID.EAN8_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                                flags |= SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT;
                                flags |= enable ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                                flags |= enable ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
                                symConfig.Flags = flags;
                                try {
                                    m_decDecoder.setSymbologyConfig(symConfig);
                                } catch (DecoderException e) {
                                    Log.d(TAG, "1 EXCEPTION SYMID = " + symConfig.symID);
                                    HandleDecoderException(e);
                                }
                                symConfig.symID = SymbologyID.SYM_EAN13;
                                try {
                                    m_decDecoder.getSymbologyConfig(symConfig); // gets the current symConfig
                                } catch (DecoderException e) {
                                    HandleDecoderException(e);
                                }
                                symConfig.Mask = SymbologyFlags.SYM_MASK_FLAGS;
                                // enable, check transmit, sys num transmit, addenda separator, 2 digit addenda, 5 digit addenda, addenda required
                                flags |= mScanService.getPropertyInt(PropertyID.EAN13_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                                flags |= SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT;
                                flags |= enable ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                                flags |= enable ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
                                symConfig.Flags = flags;
                                try {
                                    m_decDecoder.setSymbologyConfig(symConfig);
                                } catch (DecoderException e) {
                                    Log.d(TAG, "1 EXCEPTION SYMID = " + symConfig.symID);
                                    HandleDecoderException(e);
                                }
                                bNotSupported = true;
                                break;
                            case PropertyID.DEC_MaxMultiRead_COUNT:
                                try {
                                    g_nMaxMultiReadCount = mScanService.getPropertyInt(PropertyID.DEC_MaxMultiRead_COUNT);
                                    if (g_nMaxMultiReadCount <= 0) {
                                        g_nMaxMultiReadCount = 2;
                                    }
                                    Log.d(TAG, "setProperties property g_nMaxMultiReadCount= " + g_nMaxMultiReadCount);
                                    setDecodeOptions();
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                }
                                bNotSupported = true;
                                break;
                            case PropertyID.DEC_Multiple_Decode_TIMEOUT:
                                try {
                                    waitMultipleTiemOut = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_TIMEOUT);
                                    if (waitMultipleTiemOut < 50) {
                                        waitMultipleTiemOut = 5 * 1000;
                                    }
                                    Log.d(TAG, "setProperties property g_waitMultipleTiemOut= " + waitMultipleTiemOut);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                }
                                bNotSupported = true;
                                break;
                            // urovo tao.he add begin
                            case PropertyID.DEC_Multiple_Decode_INTERVAL:
                                try {
                                    waitMultipleInterval = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_INTERVAL);
                                    // urovo add shenpidong begin 2019-09-12
                                    waitMultipleInterval = waitMultipleInterval <= 10 ? 15 : waitMultipleInterval;
                                    // urovo add shenpidong end 2019-09-12
                                    Log.d(TAG, "setProperties property g_waitMultipleInterval= " + waitMultipleInterval);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                }
                                bNotSupported = true;
                                break;
                            // urovo tao.he add end
                            case PropertyID.DEC_Multiple_Decode_MODE:
                                Log.d(TAG, "setProperties property waitMultipleMode= " + waitMultipleMode);
                                try {
                                    waitMultipleMode = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_MODE);
                                    if (waitMultipleMode > 0) setDecodeOptions();
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                }
                                bNotSupported = true;
                                break;
                            case PropertyID.DOTCODE_ENABLE:
                                if (m_decDecoder != null) {
                                    int isenable = mScanService.getPropertyInt(PropertyID.DOTCODE_ENABLE);
                                    try {
                                        m_decDecoder.setDecodeParameter(0x1a161001, isenable); // Enhance the broken bar code
                                    } catch (DecoderException e) {
                                        HandleDecoderException(e);
                                    }
                                }
                                break;
                            default:
                                bNotSupported = true;
                                break;
                        }
                    } else {
                        if (PropertyID.DPM_DECODE_MODE == keyForIndex) {
                            if (m_decDecoder != null) {
                                try {
                                    int dpmMode = mScanService.getPropertyInt(PropertyID.DPM_DECODE_MODE);
                                    Log.d(TAG, "setProperties dpmMode:" + dpmMode);
                                    m_decDecoder.setDecodeParameter(0x40012903, dpmMode);
                                } catch (DecoderException e) {
                                    HandleDecoderException(e);
                                }
                            }
                            bNotSupported = true;
                        } else if (PropertyID.DATAMATRIX_SYMBOL_SIZE == keyForIndex) {
                            if (m_decDecoder != null) {
                                try {
                                    int dpmMode = mScanService.getPropertyInt(PropertyID.DATAMATRIX_SYMBOL_SIZE);
                                    m_decDecoder.setDecodeParameter(0x40010416, dpmMode);
                                } catch (DecoderException e) {
                                    HandleDecoderException(e);
                                }
                            }
                            bNotSupported = true;
                        } else if (PropertyID.MAXICODE_SYMBOL_SIZE == keyForIndex) {
                            if (m_decDecoder != null) {
                                try {
                                    int dpmMode = mScanService.getPropertyInt(PropertyID.MAXICODE_SYMBOL_SIZE);
                                    m_decDecoder.setDecodeParameter(0x40010602, dpmMode >= 1 ? 1 : 0);
                                } catch (DecoderException e) {
                                    HandleDecoderException(e);
                                }
                            }
                            bNotSupported = true;
                        } else if (PropertyID.QRCODE_SYMBOL_SIZE == keyForIndex) {
                            if (m_decDecoder != null) {
                                try {
                                    int dpmMode = mScanService.getPropertyInt(PropertyID.QRCODE_SYMBOL_SIZE);
                                    Log.d(TAG, "setProperties dpmMode:" + dpmMode);
                                    m_decDecoder.setDecodeParameter(0x40010904, dpmMode >= 1 ? 1 : 0);
                                } catch (DecoderException e) {
                                    HandleDecoderException(e);
                                }
                            }
                            bNotSupported = true;
                        } else if (PropertyID.AZTEC_SYMBOL_SIZE == keyForIndex) {
                            if (m_decDecoder != null) {
                                try {
                                    int dpmMode = mScanService.getPropertyInt(PropertyID.AZTEC_SYMBOL_SIZE);
                                    Log.d(TAG, "setProperties dpmMode:" + dpmMode);
                                    m_decDecoder.setDecodeParameter(0x40011202, dpmMode >= 1 ? 1 : 0);
                                } catch (DecoderException e) {
                                    HandleDecoderException(e);
                                }
                            }
                            bNotSupported = true;
                        } else if (PropertyID.POSTAL_GROUP_TYPE_ENABLE == keyForIndex) {
                            setPostalSymbologies();
                        } else if (PropertyID.KOREA_POST_ENABLE == keyForIndex) {
                            SymbologyConfig symConfig = new SymbologyConfig(0);
                            symConfig.symID = SymbologyID.SYM_KOREAPOST;
                            try {
                                m_decDecoder.getSymbologyConfig(symConfig); // gets the current symConfig
                            } catch (DecoderException e) {
                                HandleDecoderException(e);
                            }
                            flags |= mScanService.getPropertyInt(PropertyID.KOREA_POST_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                            symConfig.Flags = flags;
                            try {
                                m_decDecoder.setSymbologyConfig(symConfig);
                            } catch (DecoderException e) {
                                Log.d(TAG, "1 EXCEPTION SYMID = " + symConfig.symID);
                                HandleDecoderException(e);
                            }
                        } else if (PropertyID.Canadian_POSTAL_ENABLE == keyForIndex) {
                            SymbologyConfig symConfig = new SymbologyConfig(0);
                            symConfig.symID = SymbologyID.SYM_CANPOST;
                            try {
                                m_decDecoder.getSymbologyConfig(symConfig); // gets the current symConfig
                            } catch (DecoderException e) {
                                HandleDecoderException(e);
                            }
                            flags |= mScanService.getPropertyInt(PropertyID.Canadian_POSTAL_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                            symConfig.Flags = flags;
                            try {
                                m_decDecoder.setSymbologyConfig(symConfig);
                            } catch (DecoderException e) {
                                Log.d(TAG, "1 EXCEPTION SYMID = " + symConfig.symID);
                                HandleDecoderException(e);
                            }
                        } else if (PropertyID.CODE128_REDUCED_QUIET_ZONE == keyForIndex) {
                            int isenable = mScanService.getPropertyInt(PropertyID.CODE128_REDUCED_QUIET_ZONE);
                            try {
                                m_decDecoder.setDecodeParameter(0x40010202, isenable);
                            } catch (DecoderException e) {
                                HandleDecoderException(e);
                            }
                        } else if (PropertyID.CODE128_SECURITY_LEVEL == keyForIndex) {
                            int isenable = mScanService.getPropertyInt(PropertyID.CODE128_SECURITY_LEVEL);
                            try {
                                m_decDecoder.setDecodeParameter(0x40010209, isenable);
                            } catch (DecoderException e) {
                                HandleDecoderException(e);
                            }
                        } else if (PropertyID.FUZZY_1D_PROCESSING == keyForIndex) {
                            int isenable = mScanService.getPropertyInt(PropertyID.FUZZY_1D_PROCESSING);
                            try {
                                m_decDecoder.setDecodeParameter(0x40005025, isenable);
                            } catch (DecoderException e) {
                                HandleDecoderException(e);
                            }
                        } else if (PropertyID.EAN8_SEND_CHECK == keyForIndex) {
                            symID = SymbologyID.SYM_EAN8;
                        } else if (PropertyID.EAN13_SEND_CHECK == keyForIndex) {
                            symID = SymbologyID.SYM_EAN13;
                        }
                    }
                    if (bNotSupported) continue; // invalid / not supported
                    SetSymbologySettings(symID);
                }
            } else {
                DecodeOptions decOpt = new DecodeOptions();
                try {
                    if (mScanService != null) {
                        m_decDecoder.getDecodeOptions(decOpt);
                        decOpt.VideoReverse = mScanService.getPropertyInt(PropertyID.IMAGE_ONE_D_INVERSE);
                        // urovo add shenpidong begin 2019-07-24
                        if (mScannerType == ScannerFactory.TYPE_N6703) {
                            decOpt.DecAttemptLimit = 500;
                        } else {
                            decOpt.DecAttemptLimit = -1;
                        }
                        // urovo add shenpidong end 2019-07-24
                        m_decDecoder.setDecodeOptions(decOpt);
                    } else {
                        Log.d(TAG, "setProperties , Scan Service:" + mScanService);
                    }
                } catch (DecoderException e) {
                    Log.d(TAG, "DecodeOptions EXCEPTION decOpt videoReverse:" + decOpt.VideoReverse);
                    HandleDecoderException(e);
                }
                Log.d(TAG, "setProperties open");
                for (int i = 0; i < SymbologyID.SYM_ALL; i++) {
                    SetSymbologySettings(i);
                }
                waitMultipleTiemOut = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_TIMEOUT);
                if (waitMultipleTiemOut < 50) {
                    waitMultipleTiemOut = 10 * 1000;
                }
                // urovo tao.he add begin
                waitMultipleInterval = mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_INTERVAL);
                if (waitMultipleInterval <= 0) {
                    waitMultipleInterval = 0;
                }
                //if(mScanService.getPropertyInt(PropertyID.IMAGE_EXPOSURE_MODE) == 1) {
                //setExposureSettings();
                //}
                if (m_decDecoder != null) {
                    int isenable = mScanService.getPropertyInt(PropertyID.DOTCODE_ENABLE);
                    try {
                        m_decDecoder.setDecodeParameter(0x1a161001, isenable); // Enhance the broken bar code
                    } catch (DecoderException e) {
                        HandleDecoderException(e);
                    }
                }
                try {
                    setOcrSettings();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (m_decDecoder != null) {
                    try {
                        int dpmMode = mScanService.getPropertyInt(PropertyID.DPM_DECODE_MODE);
                        m_decDecoder.setDecodeParameter(0x40012903, dpmMode);
                    } catch (DecoderException e) {
                        HandleDecoderException(e);
                    }
                }
            }
            if (m_decDecoder != null) {
                try {
                    m_decDecoder.setDecodeParameter(0x40005025, 1);
                } catch (DecoderException e) {
                    HandleDecoderException(e);
                }
            }
        }
        return 0;
    }

    /*
    When enabling Postal Symbologies, only certain combinations are allowed. Postal symbology decoding is enabled according to
    the table below. Table entries may not be combined (i.e., summed) to enable multiple symbologies, unless the combination is
    explicitly shown below:
    */
    private void setPostalSymbologies() {
        int postalEnable = mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE);
        switch (postalEnable) {
            case 0:
                break;
            case 1:
                //Australian Post
                break;
            case 2:
                //Royal Mail(BPO 4-State)
                postalEnable = 8;
                break;
            case 3:
                //Japan Post
                break;
            case 4:
                //Dutch Post(KIX code)
                break;
            case 5:
                //Planet
                break;
            case 6:
                //Postnet
                postalEnable = 11;
                break;
            case 7:
                //UPU FICS Postal(US4State FICS/UPU ID-Tag)
                postalEnable = 9;
                break;
            case 8:
                //USPS 4CB(Intelligent Mail)
                postalEnable = 10;
                break;
            case 9:
                //Canadian Post
                postalEnable = 30;
                break;
            case 10:
                postalEnable = 12;
                break;
            case 11:
                postalEnable = 13;
                break;
            case 12:
                postalEnable = 14;
                break;
            case 13:
                postalEnable = 15;
                break;
            case 14:
                postalEnable = 16;
                break;
            case 15:
                postalEnable = 17;
                break;
            case 16:
                postalEnable = 21;
                break;
            case 17:
                postalEnable = 22;
                break;
            case 18:
                postalEnable = 23;
                break;
            case 19:
                postalEnable = 24;
                break;
            case 20:
                postalEnable = 28;
                break;
            default:
                if(postalEnable < 0 || postalEnable > 42) {
                    postalEnable = 0;
                }
                break;

        }
        try {
            m_decDecoder.setDecodeParameter(DCLProperties.DEC_POSTAL_ENABLED, postalEnable);
            boolean sendChk = mScanService.getPropertyInt(PropertyID.US_POSTAL_SEND_CHECK) == 1;
            if(postalEnable > 0) {
                m_decDecoder.setDecodeParameter(DCLProperties.DEC_PLANETCODE_CHECK_DIGIT_TRANSMIT, sendChk ? 1 : 0);
            }
            if(postalEnable == 1) {
                /**
                 * none
                 * Numeric N table
                 * Alphanumeric C table
                 * Combination N and C table
                 */
                m_decDecoder.setDecodeParameter(DCLProperties.DEC_AUS_POST_INTERPRET_MODE, 3);
            }
        } catch (DecoderException e) {
            HandleDecoderException(e);
        }
    }

    /**
     * Gets exposure settings
     * nMode = 0 upX 386 upY 290 lwX 446 lwY 350 nDebugWindow= 0
     * nMode = 0 upX 0 upY 830 lwX 638 lwY 0 upX 1 upY 831 lwX 1 lwY 639
     * GetExposureSettings++ lightMode 3
     * g_nExposureSettings:
     * tag = 0 value = 0
     * tag = 1 value = 0
     * tag = 2 value = 0
     * tag = 3 value = 0
     * tag = 4 value = 0
     * tag = 5 value = 0
     * tag = 8 value = 0
     * tag = 9 value = 0
     * tag = 10 value = 0
     * tag = 11 value = 0
     * tag = 12 value = 0
     * tag = 13 value = 0
     * tag = 14 value = 0
     * tag = 15 value = 0
     * tag = 16 value = 0
     */
    private void getExposureSettings() {
        if (m_decDecoder == null) {
            Log.d(TAG, "getExposureSettings, m_decDecoder:" + m_decDecoder);
            return;
        }
        try {
            int lightMode = m_decDecoder.getLightsMode();
            Log.d(TAG, "GetExposureSettings++ lightMode " + lightMode);
            m_decDecoder.getExposureSettings(g_nExposureSettings);
        } catch (DecoderException e) {
            HandleDecoderException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        String debug = "g_nExposureSettings:\n";

        int tag = 0;

        for (int i = 0; i < g_nExposureSettings.length; i++) {
            tag = g_nExposureSettings[i++];
            debug += "tag = " + tag + " value = " + g_nExposureSettings[i] + "\n";
        }

        Log.d(TAG, debug);

        Log.d(TAG, "GetExposureSettings--");
    }

    /**
     * Sets default Decoder preferences based on "HSMDecoderAPI" settings
     * nMode = 0 upX 386 upY 290 lwX 446 lwY 350 nDebugWindow= 0
     * nMode = 0 upX 0 upY 830 lwX 638 lwY 0 upX 1 upY 831 lwX 1 lwY 639
     * 1 EXCEPTION SYMID = 11
     * HandleDecoderException++9An invalid parameter was specified.
     * 1 EXCEPTION SYMID = 26
     * HandleDecoderException++9An invalid parameter was specified.
     * 1 EXCEPTION SYMID = 35
     * HandleDecoderException++9An invalid parameter was specified.
     * 1 EXCEPTION SYMID = 39
     * HandleDecoderException++9An invalid parameter was specified.
     */
    void getDecodingWindow() {

        if (m_decDecoder == null) {
            Log.d(TAG, "getDecodingWindow, m_decDecoder:" + m_decDecoder);
            return;
        }
        int nMode = DecodeWindowMode.DECODE_WINDOW_MODE_DISABLED;
        int nDebugWindow = 0;
        DecodeWindow myWindow = new DecodeWindow();
        try {
            Log.d(TAG, "getDecodeWindow");
            m_decDecoder.getDecodeWindow(myWindow);
            Log.d(TAG, "getDecodeWindowMode");
            nMode = m_decDecoder.getDecodeWindowMode();
            Log.d(TAG, "getShowDecodeWindow");
            nDebugWindow = m_decDecoder.getShowDecodeWindow();
            Log.d(TAG, "nMode = " + nMode + " upX " + myWindow.UpperLeftX + " upY " + myWindow.UpperLeftY + " lwX " + myWindow.LowerRightX + " lwY " + myWindow.LowerRightY + " nDebugWindow= " + nDebugWindow);
            DecodeWindowLimits windowlimits = new DecodeWindowLimits();
            m_decDecoder.getDecodeWindowLimits(windowlimits);
            //bOk = false;
            Log.d(TAG, "nMode = " + nMode + " upX " + windowlimits.UpperLeft_X_Min + " upY " + windowlimits.UpperLeft_X_Max
                    + " lwX " + windowlimits.UpperLeft_Y_Max + " lwY " + windowlimits.UpperLeft_Y_Min + " upX " + windowlimits.LowerRight_X_Min + " upY " + windowlimits.LowerRight_X_Max
                    + " lwX " + windowlimits.LowerRight_Y_Min + " lwY " + windowlimits.LowerRight_Y_Max);
        } catch (DecoderException e) {
            HandleDecoderException(e);
        }
    }

    /**
     * Sets the Decoder settings based on user preferences
     * (412, 316), (420,324),Field of View,（一定要选择这个）
     */
    private void setDecodingSettings() {

        // Windowing
        DecodeWindow myWindow = new DecodeWindow();
        int nMode = DecodeWindowMode.DECODE_WINDOW_MODE_DISABLED;
        // #define IT6000_DEFAULT_DECODE_WINDOW_ULX       386
        // #define IT6000_DEFAULT_DECODE_WINDOW_ULY       290
        // #define IT6000_DEFAULT_DECODE_WINDOW_LRX       446
        // #define IT6000_DEFAULT_DECODE_WINDOW_LRY       350
        boolean enable_windowing = mScanService.getPropertyInt(PropertyID.DEC_2D_CENTERING_ENABLE) == 1;
        try {
            if (enable_windowing && m_decDecoder != null) {
                nMode = mScanService.getPropertyInt(PropertyID.DEC_2D_CENTERING_MODE);
                myWindow.UpperLeftX = mScanService.getPropertyInt(PropertyID.DEC_2D_WINDOW_UPPER_LX);
                myWindow.UpperLeftY = mScanService.getPropertyInt(PropertyID.DEC_2D_WINDOW_UPPER_LY);
                myWindow.LowerRightX = mScanService.getPropertyInt(PropertyID.DEC_2D_WINDOW_LOWER_RX);
                myWindow.LowerRightY = mScanService.getPropertyInt(PropertyID.DEC_2D_WINDOW_LOWER_RY);
                Log.d(TAG, "Centering is enabled");
                if (nMode < 0) nMode = DecodeWindowMode.DECODE_WINDOW_MODE_FIELD_OF_VIEW;
                // urovo add by shenpidong begin 2020-04-10
                if (myWindow.UpperLeftX <= 0) myWindow.UpperLeftX = 412;
                if (myWindow.UpperLeftY <= 0) myWindow.UpperLeftY = 316;
                if (myWindow.LowerRightX <= 0) myWindow.LowerRightX = 420;
                if (myWindow.LowerRightY <= 0) myWindow.LowerRightY = 324;
                // urovo add by shenpidong end 2020-04-10
                Log.d(TAG, "nMode = " + nMode + " upX " + myWindow.UpperLeftX + " upY " + myWindow.UpperLeftY + " lwX " + myWindow.LowerRightX + " lwY " + myWindow.LowerRightY);
                // enable the mode
                m_decDecoder.setDecodeWindowMode(nMode);

                // set the window
                m_decDecoder.setDecodeWindow(myWindow);
                boolean bDebugWindowMode = mScanService.getPropertyInt(PropertyID.DEC_2D_DEBUG_WINDOW_ENABLE) == 1;
                Log.d(TAG, "set the debug window");
                // set the debug window
                if (bDebugWindowMode) {
                    nMode = DecodeWindowShowWindow.DECODE_WINDOW_SHOW_WINDOW_WHITE; // white
                    m_decDecoder.setShowDecodeWindow(nMode);
                } else {
                    nMode = DecodeWindowMode.DECODE_WINDOW_MODE_DISABLED;
                    m_decDecoder.setShowDecodeWindow(nMode);
                }
            } else {
                // disable windowing
                m_decDecoder.setDecodeWindowMode(nMode);
            }
        } catch (DecoderException e) {
            HandleDecoderException(e);
        }
        getDecodingWindow();
        if (mScanService.getPropertyInt(PropertyID.DEC_Multiple_Decode_MODE) > 0) {
            setDecodeOptions();
        }
    }

    void SetSymbologySettings(int symID) {
        //Log.d(TAG, "SetSymbologySettings++");
        // urovo add shenpidong begin 2019-05-17
        if (m_decDecoder == null || isErrNoImage || isDisconnectScan6603) {
            Log.d(TAG, "SetSymbologySettings, m_decDecoder:" + m_decDecoder + ",isErrNoImage:" + isErrNoImage + ", disconnect Scan:" + isDisconnectScan6603);
            return;
        }
        // urovo add shenpidong end 2019-05-17

        int flags = 0; // flags config
        int min = 0; // minimum length config
        int max = 0; // maximum length config
        int postal_config = 0; // postal config
        SymbologyConfig symConfig = new SymbologyConfig(0); // symbology config
        int min_default = 0, max_default = 0;
        boolean bNotSupported = false;

        symConfig.symID = symID; // symID
        // Set appropriate sym config mask...
        switch (symID) {
            // Flag & Range:
            case SymbologyID.SYM_AZTEC:
            case SymbologyID.SYM_CODABAR:
            case SymbologyID.SYM_CODE11:
            case SymbologyID.SYM_CODE128:
            case SymbologyID.SYM_GS1_128:
            case SymbologyID.SYM_CODE39:
                // case SymbologyID.SYM_CODE49: // not supported
            case SymbologyID.SYM_CODE93:
            case SymbologyID.SYM_COMPOSITE:
            case SymbologyID.SYM_DATAMATRIX:
            case SymbologyID.SYM_INT25:
            case SymbologyID.SYM_MAXICODE:
            case SymbologyID.SYM_MICROPDF:
            case SymbologyID.SYM_PDF417:
            case SymbologyID.SYM_QR:
            case SymbologyID.SYM_RSS:
            case SymbologyID.SYM_IATA25:
                //case SymbologyID.SYM_CODABLOCK:
            case SymbologyID.SYM_MSI:
            case SymbologyID.SYM_MATRIX25:
            case SymbologyID.SYM_KOREAPOST:
            case SymbologyID.SYM_STRT25:
                // case SymbologyID.SYM_PLESSEY: // not supported
            case SymbologyID.SYM_CHINAPOST:
                //case SymbologyID.SYM_TELEPEN:
                // case SymbologyID.SYM_CODE16K: // not supported
                // case SymbologyID.SYM_POSICODE: // not supported
            case SymbologyID.SYM_HANXIN:
                // case SymbologyID.SYM_GRIDMATRIX: // not supported
                try {
                    // urovo add shenpidong begin 2019-05-17
                    // gets the current symConfig
                    if (m_decDecoder != null) {
                        m_decDecoder.getSymbologyConfig(symConfig);
                    }

                    Log.d(TAG, "SetSymbologySettings, SYM_HANXIN m_decDecoder:" + m_decDecoder + ",symID:" + symID);
                    if (m_decDecoder != null) {
                        min_default = m_decDecoder.getSymbologyMinRange(symID);
                    }
                    if (m_decDecoder != null) {
                        max_default = m_decDecoder.getSymbologyMaxRange(symID);
                    }
                } catch (DecoderException e) {
                    Log.d(TAG, "SetSymbologySettings, SYM_HANXIN DecoderException m_decDecoder:" + m_decDecoder + ",symID:" + symID);
                    HandleDecoderException(e);
                }
                // urovo add shenpidong end 2019-05-17
                symConfig.Mask = SymbologyFlags.SYM_MASK_FLAGS | SymbologyFlags.SYM_MASK_MIN_LEN
                        | SymbologyFlags.SYM_MASK_MAX_LEN;
                break;
            // Flags Only:
            case SymbologyID.SYM_EAN8:
            case SymbologyID.SYM_EAN13:
            case SymbologyID.SYM_UPCA:
            case SymbologyID.SYM_UPCE0:
            case SymbologyID.SYM_UPCE1:
            case SymbologyID.SYM_ISBT:
            case SymbologyID.SYM_TLCODE39:
            case SymbologyID.SYM_TRIOPTIC:
            case SymbologyID.SYM_CODE32:
                //case SymbologyID.SYM_COUPONCODE:
            case SymbologyID.SYM_IDTAG:
                // case SymbologyID.SYM_LABEL: // not supported
                try {
                    // gets the current symConfig
                    if (m_decDecoder != null) {
                        m_decDecoder.getSymbologyConfig(symConfig);
                    }
                } catch (DecoderException e) {
                    HandleDecoderException(e);
                }
                symConfig.Mask = SymbologyFlags.SYM_MASK_FLAGS;
                break;
            case SymbologyID.SYM_POSTNET:
            case SymbologyID.SYM_BPO:
            case SymbologyID.SYM_CANPOST:
            case SymbologyID.SYM_AUSPOST:
            case SymbologyID.SYM_JAPOST:
            case SymbologyID.SYM_PLANET:
            case SymbologyID.SYM_DUTCHPOST:
            case SymbologyID.SYM_USPS4CB:
            case SymbologyID.SYM_US_POSTALS1:
                setPostalSymbologies();
                bNotSupported = true;
                break;
            default:
                // invalid / not supported
                bNotSupported = true;
                break;
        }

        // Set symbology config...
        switch (symID) {
            case SymbologyID.SYM_AZTEC:
                // enable
                flags |= mScanService.getPropertyInt(PropertyID.AZTEC_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = min_default;
                max = max_default;
                break;
            case SymbologyID.SYM_CODABAR:
                // enable, check char, start/stop transmit, codabar concatenate
                flags |= mScanService.getPropertyInt(PropertyID.CODABAR_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                CLSI_enable = mScanService.getPropertyInt(PropertyID.CODABAR_CLSI) == 1;
                //默认输出开始结束字母
                NOTIS_enable = mScanService.getPropertyInt(PropertyID.CODABAR_NOTIS) == 0;
                flags |= NOTIS_enable ? SymbologyFlags.SYMBOLOGY_START_STOP_XMIT : 0;
                //flags |= mScanService.getPropertyInt(PropertyID.CODABAR_CLSI) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                //flags |= mScanService.getPropertyInt(PropertyID.CODABAR_CLSI) == 1 ? SymbologyFlags.SYMBOLOGY_CODABAR_CONCATENATE : 0;
                min = mScanService.getPropertyInt(PropertyID.CODABAR_LENGTH1);
                min = min >= 2 ? min:2;
                max = mScanService.getPropertyInt(PropertyID.CODABAR_LENGTH2);
                min = min < min_default ? min_default : min;
                max = max > max_default ? max_default : max;
                break;
            case SymbologyID.SYM_CODE11:
                // enable, check char
                // flags |= sharedPrefs.getBoolean("sym_code11_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                // flags |= sharedPrefs.getBoolean("sym_code11_check_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.CODE11_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                // flags |=
                // mScanService.getPropertyInt(PropertyID.CODE11_SEND_CHECK) ==
                // 1 ? SymbologyFlags.SYMBOLOGY_START_STOP_XMIT : 0;
                flags |= mScanService.getPropertyInt(PropertyID.CODE11_ENABLE_CHECK) > 0 ? SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                min = mScanService.getPropertyInt(PropertyID.CODE11_LENGTH1);
                max = mScanService.getPropertyInt(PropertyID.CODE11_LENGTH2);
                min = min < min_default ? min_default : min;
                max = max > max_default ? max_default : max;
                break;
            case SymbologyID.SYM_CODE128:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_code128_enable", false)
                // ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.CODE128_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = mScanService.getPropertyInt(PropertyID.CODE128_LENGTH1);
                max = mScanService.getPropertyInt(PropertyID.CODE128_LENGTH2);
                min = min < min_default ? min_default : min;
                max = max > max_default ? max_default : max;
                break;
            case SymbologyID.SYM_GS1_128:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_gs1_128_enable", false)
                // ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.CODE128_GS1_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = min_default;
                max = max_default;
                break;
            case SymbologyID.SYM_CODE39:
                // enable, check char, start/stop transmit, append, full ascii
                /*
                 * flags |= sharedPrefs.getBoolean("sym_code39_enable", false) ?
                 * SymbologyFlags.SYMBOLOGY_ENABLE : 0; flags |=
                 * sharedPrefs.getBoolean("sym_code39_check_enable", false) ?
                 * SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0; flags |=
                 * sharedPrefs
                 * .getBoolean("sym_code39_start_stop_transmit_enable", false) ?
                 * SymbologyFlags.SYMBOLOGY_START_STOP_XMIT : 0; flags |=
                 * sharedPrefs.getBoolean("sym_code39_append_enable", false) ?
                 * SymbologyFlags.SYMBOLOGY_ENABLE_APPEND_MODE : 0; flags |=
                 * sharedPrefs.getBoolean("sym_code39_fullascii_enable", false)
                 * ? SymbologyFlags.SYMBOLOGY_ENABLE_FULLASCII : 0;
                 */
                flags |= mScanService.getPropertyInt(PropertyID.CODE39_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.CODE39_ENABLE_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.CODE39_SEND_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                flags |= mScanService.getPropertyInt(PropertyID.CODE39_FULL_ASCII) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE_FULLASCII : 0;
                min = mScanService.getPropertyInt(PropertyID.CODE39_LENGTH1);
                max = mScanService.getPropertyInt(PropertyID.CODE39_LENGTH2);
                min = min < min_default ? min_default : min;
                max = max > max_default ? max_default : max;
                break;
            case SymbologyID.SYM_CODE49:
            case SymbologyID.SYM_GRIDMATRIX:
            case SymbologyID.SYM_PLESSEY:
            case SymbologyID.SYM_CODE16K:
            case SymbologyID.SYM_POSICODE:
            case SymbologyID.SYM_LABEL:
                // not supported
                break;
            case SymbologyID.SYM_CODE93:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_code93_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.CODE93_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = mScanService.getPropertyInt(PropertyID.CODE93_LENGTH1);
                max = mScanService.getPropertyInt(PropertyID.CODE93_LENGTH2);
                min = min < min_default ? min_default : min;
                max = max > max_default ? max_default : max;
                break;
            case SymbologyID.SYM_COMPOSITE:
                // enable, composit upc
                // flags |= sharedPrefs.getBoolean("sym_composite_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                // flags |= sharedPrefs.getBoolean("sym_composite_upc_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_COMPOSITE_UPC : 0;
                if (mScanService.getPropertyInt(PropertyID.COMPOSITE_CC_AB_ENABLE) == 1 || mScanService.getPropertyInt(PropertyID.COMPOSITE_CC_C_ENABLE) == 1) {
                    Log.d(TAG, "SYMBOLOGY_ENABLE ");
                    flags |= SymbologyFlags.SYMBOLOGY_ENABLE;
                    flags |= SymbologyFlags.SYMBOLOGY_COMPOSITE_UPC;
                } else {
                    flags |= 0;
                }
                //flags |= mScanService.getPropertyInt(PropertyID.COMPOSITE_CC_AB_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE: 0;
                //flags |= mScanService.getPropertyInt(PropertyID.COMPOSITE_CC_C_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = min_default;
                max = max_default;
                break;
            case SymbologyID.SYM_DATAMATRIX:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_datamatrix_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                // min, max
                flags |= mScanService.getPropertyInt(PropertyID.DATAMATRIX_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = min_default;
                max = max_default;
                break;
            case SymbologyID.SYM_EAN8: {
                // enable, check char transmit, addenda separator, 2 digit
                // addena, 5 digit addena, addena required
                // flags |= sharedPrefs.getBoolean("sym_ean8_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                // flags |=
                // sharedPrefs.getBoolean("sym_ean8_check_transmit_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                // flags |=
                // sharedPrefs.getBoolean("sym_ean8_addenda_separator_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR : 0;
                // flags |=
                // sharedPrefs.getBoolean("sym_ean8_2_digit_addenda_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA: 0;
                // flags |=
                // sharedPrefs.getBoolean("sym_ean8_5_digit_addenda_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
                // flags |=
                // sharedPrefs.getBoolean("sym_ean8_addenda_required_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED : 0;
                flags |= mScanService.getPropertyInt(PropertyID.EAN8_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.EAN8_SEND_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                boolean enable = mScanService.getPropertyInt(PropertyID.EAN_EXT_ENABLE_2_5_DIGIT) == 1;
                flags |= enable ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                flags |= enable ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
            }
            break;
            case SymbologyID.SYM_EAN13: {
                // enable, check char transmit, addenda separator, 2 digit
                // addena, 5 digit addena, addena required
                /*
                 * flags |= sharedPrefs.getBoolean("sym_ean13_enable", false) ?
                 * SymbologyFlags.SYMBOLOGY_ENABLE : 0; flags |=
                 * sharedPrefs.getBoolean("sym_ean13_check_transmit_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0; flags
                 * |=
                 * sharedPrefs.getBoolean("sym_ean13_addenda_separator_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR : 0;
                 * flags |=
                 * sharedPrefs.getBoolean("sym_ean13_2_digit_addenda_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA: 0; flags
                 * |= sharedPrefs.getBoolean("sym_ean13_5_digit_addenda_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0; flags
                 * |=
                 * sharedPrefs.getBoolean("sym_ean13_addenda_required_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED : 0;
                 */
                flags |= mScanService.getPropertyInt(PropertyID.EAN13_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.EAN13_SEND_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                boolean enable = mScanService.getPropertyInt(PropertyID.EAN_EXT_ENABLE_2_5_DIGIT) == 1;
                flags |= enable ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                flags |= enable ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
                int bookland = mScanService.getPropertyInt(PropertyID.EAN13_BOOKLANDEAN);
                if (m_decDecoder != null) {
                    try {
                        m_decDecoder.setDecodeParameter(0x1a013007, bookland);
                    } catch (DecoderException e) {
                        HandleDecoderException(e);
                    }
                }
            }
            break;
            case SymbologyID.SYM_INT25:
                // enable, check enable, check transmit
                // flags |= sharedPrefs.getBoolean("sym_int25_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                // flags |= sharedPrefs.getBoolean("sym_int25_check_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                // flags |=
                // sharedPrefs.getBoolean("sym_int25_check_transmit_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                flags |= mScanService.getPropertyInt(PropertyID.I25_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.I25_ENABLE_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.I25_SEND_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                min = mScanService.getPropertyInt(PropertyID.I25_LENGTH1);
                max = mScanService.getPropertyInt(PropertyID.I25_LENGTH2);
                min = min < min_default ? min_default : min;
                max = max > max_default ? max_default : max;
                break;
            case SymbologyID.SYM_MAXICODE:
                boolean maxicode_enable = mScanService.getPropertyInt(PropertyID.MAXICODE_ENABLE) == 1;
                flags |= maxicode_enable ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = min_default;
                max = max_default;
                break;
            case SymbologyID.SYM_MICROPDF:
                boolean micropdf417_enable = mScanService.getPropertyInt(PropertyID.MICROPDF417_ENABLE) == 1;
                flags |= micropdf417_enable ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = min_default;
                max = max_default;
                break;
            case SymbologyID.SYM_PDF417:
                flags |= mScanService.getPropertyInt(PropertyID.PDF417_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = min_default;
                max = max_default;
                break;
            case SymbologyID.SYM_QR:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_qr_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.QRCODE_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = min_default;
                max = max_default;
                break;
            case SymbologyID.SYM_HANXIN:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_hanxin_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.HANXIN_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = min_default;
                max = max_default;
                break;
            case SymbologyID.SYM_RSS:
                // rss enable, rsl enable, rse enable
                /*
                 * flags |= sharedPrefs.getBoolean("sym_rss_rss_enable", false)
                 * ? SymbologyFlags.SYMBOLOGY_RSS_ENABLE : 0; flags |=
                 * sharedPrefs.getBoolean("sym_rss_rsl_enable", false) ?
                 * SymbologyFlags.SYMBOLOGY_RSL_ENABLE : 0; flags |=
                 * sharedPrefs.getBoolean("sym_rss_rse_enable", false) ?
                 * SymbologyFlags.SYMBOLOGY_RSE_ENABLE : 0;
                 */
                flags |= mScanService.getPropertyInt(PropertyID.GS1_14_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_RSS_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.GS1_LIMIT_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_RSL_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.GS1_EXP_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_RSE_ENABLE : 0;
                min = mScanService.getPropertyInt(PropertyID.GS1_EXP_LENGTH1);
                max = mScanService.getPropertyInt(PropertyID.GS1_EXP_LENGTH2);
                min = min < min_default ? min_default : min;
                max = max > max_default ? max_default : max;
                if (mScanService.getPropertyInt(PropertyID.COMPOSITE_CC_AB_ENABLE) == 1) {
                    flags |= SymbologyFlags.SYMBOLOGY_RSS_ENABLE;
                }
                break;
            case SymbologyID.SYM_UPCA: {
                // enable, check transmit, sys num transmit, addenda separator,
                // 2 digit addenda, 5 digit addenda, addenda required
                /*
                 * flags |= sharedPrefs.getBoolean("sym_upca_enable", false) ?
                 * SymbologyFlags.SYMBOLOGY_ENABLE : 0; flags |=
                 * sharedPrefs.getBoolean("sym_upca_check_transmit_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0; flags |=
                 * sharedPrefs.getBoolean("sym_upca_sys_num_transmit_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT : 0; flags
                 * |=
                 * sharedPrefs.getBoolean("sym_upca_addenda_separator_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR : 0;
                 * flags |=
                 * sharedPrefs.getBoolean("sym_upca_2_digit_addenda_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0; flags
                 * |= sharedPrefs.getBoolean("sym_upca_5_digit_addenda_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0; flags
                 * |= sharedPrefs.getBoolean("sym_upca_addenda_required_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED : 0;
                 */
                flags |= mScanService.getPropertyInt(PropertyID.UPCA_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                //flags |= mScanService.getPropertyInt(PropertyID.UPCA_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR: 0;
                flags |= mScanService.getPropertyInt(PropertyID.UPCA_SEND_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT | SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.UPCA_SEND_SYS) == 1 ? SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT : 0;
                flags |= mScanService.getPropertyInt(PropertyID.UPCA_TO_EAN13) == 1 ? SymbologyFlags.SYMBOLOGY_UPCA_TRANSLATE_TO_EAN13 : 0;
                boolean enable = mScanService.getPropertyInt(PropertyID.EAN_EXT_ENABLE_2_5_DIGIT) == 1;
                flags |= enable ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                flags |= enable ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
            }
            break;
            case SymbologyID.SYM_UPCE1: {
                // upce1 enable
                // flags |= sharedPrefs.getBoolean("sym_upce1_upce1_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_UPCE1_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.UPCE1_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_UPCE1_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.UPCE1_TO_UPCA) == 1 ? SymbologyFlags.SYMBOLOGY_EXPANDED_UPCE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.UPCE1_SEND_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT | SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.UPCE1_SEND_SYS) == 1 ? SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT : 0;
                boolean enable = mScanService.getPropertyInt(PropertyID.EAN_EXT_ENABLE_2_5_DIGIT) == 1;
                flags |= enable ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                flags |= enable ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
            }
            break;
            case SymbologyID.SYM_UPCE0: {
                // enable, upce expanded, char char transmit, num sys transmit,
                // addenda separator, 2 digit addenda, 5 digit addenda, addenda
                // required
                /*
                 * flags |= sharedPrefs.getBoolean("sym_upce0_enable", false) ?
                 * SymbologyFlags.SYMBOLOGY_ENABLE : 0; flags |=
                 * sharedPrefs.getBoolean("sym_upce0_upce_expanded_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_EXPANDED_UPCE : 0; flags |=
                 * sharedPrefs.getBoolean("sym_upce0_check_transmit_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0; flags |=
                 * sharedPrefs.getBoolean("sym_upce0_sys_num_transmit_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT : 0; flags
                 * |=
                 * sharedPrefs.getBoolean("sym_upce0_addenda_separator_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_ADDENDA_SEPARATOR : 0;
                 * flags |=
                 * sharedPrefs.getBoolean("sym_upce0_2_digit_addenda_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0; flags
                 * |= sharedPrefs.getBoolean("sym_upce0_5_digit_addenda_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0; flags
                 * |=
                 * sharedPrefs.getBoolean("sym_upce0_addenda_required_enable",
                 * false) ? SymbologyFlags.SYMBOLOGY_ADDENDA_REQUIRED : 0;
                 */
                flags |= mScanService.getPropertyInt(PropertyID.UPCE_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.UPCE_TO_UPCA) == 1 ? SymbologyFlags.SYMBOLOGY_EXPANDED_UPCE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.UPCE_SEND_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT | SymbologyFlags.SYMBOLOGY_CHECK_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.UPCE_SEND_SYS) == 1 ? SymbologyFlags.SYMBOLOGY_NUM_SYS_TRANSMIT : 0;
                boolean enable = mScanService.getPropertyInt(PropertyID.EAN_EXT_ENABLE_2_5_DIGIT) == 1;
                flags |= enable ? SymbologyFlags.SYMBOLOGY_2_DIGIT_ADDENDA : 0;
                flags |= enable ? SymbologyFlags.SYMBOLOGY_5_DIGIT_ADDENDA : 0;
            }
            break;
            case SymbologyID.SYM_ISBT:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_isbt_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.CODE_ISBT_128) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_IATA25:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_iata25_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                //flags |= SymbologyFlags.SYMBOLOGY_ENABLE;//default enable
                break;
            case SymbologyID.SYM_CODABLOCK:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_codablock_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                //flags |= SymbologyFlags.SYMBOLOGY_ENABLE;//default enable
                break;

            /* Post Symbology Config */
            case SymbologyID.SYM_POSTNET:
                // enable
                // flags |= (postal_config == POSTNET) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                // check transmit
                // flags |=
                // sharedPrefs.getBoolean("sym_postnet_check_transmit_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                //flags |= mScanService.getPropertyInt(PropertyID.US_POSTNET_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE  : 0;
                flags |= mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE) == 6 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.US_POSTAL_SEND_CHECK) == 1 ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;

                break;
            case SymbologyID.SYM_JAPOST:
                // enable
                // flags |= (postal_config == JAPAN_POST) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                //flags |= mScanService.getPropertyInt(PropertyID.JAPANESE_POST_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE  : 0;
                flags |= mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE) == 2 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_PLANET:
                // enable
                // flags |= (postal_config == PLANETCODE) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                //flags |= mScanService.getPropertyInt(PropertyID.US_PLANET_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE  : 0;
                flags |= mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE) == 5 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_DUTCHPOST:
                // enable
                // flags |= (postal_config == KIX) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                //flags |= mScanService.getPropertyInt(PropertyID.KIX_CODE_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE) == 3 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_US_POSTALS1:
                // enable
                // flags |= (postal_config == US_POSTALS) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                int POSTALSVal = mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE);
                flags |= POSTALSVal == 11 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;//default enable
                break;
            case SymbologyID.SYM_USPS4CB:
                // enable
                // flags |= (postal_config == USPS_4_STATE) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                //flags |= mScanService.getPropertyInt(PropertyID.USPS_4STATE_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE) == 7 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_IDTAG:
                // enable
                // flags |= (postal_config == UPU_4_STATE) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                //flags |= mScanService.getPropertyInt(PropertyID.UPU_FICS_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE  : 0;
                flags |= mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE) == 8 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_BPO:
                // enable
                // flags |= (postal_config == ROYAL_MAIL) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                //flags |= mScanService.getPropertyInt(PropertyID.ROYAL_MAIL_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE  : 0;
                flags |= mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE) == 4 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_CANPOST:
                // enable
                // flags |= (postal_config == CANADIAN) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE) == 9 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_AUSPOST:
                // enable
                // flags |= (postal_config == AUS_POST) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                // Bar output
                // sharedPrefs.getBoolean("sym_auspost_bar_output_enable",
                // false);
                // Interpret Mode
                /*
                 * temp = sharedPrefs.getString("sym_aus_interpret_mode","0");
                 * postal_config = Integer.parseInt(temp); switch(postal_config)
                 * { // Numeric N Table: case 1: flags |=
                 * SymbologyFlags.SYMBOLOGY_AUS_POST_NUMERIC_N_TABLE; break; //
                 * Alphanumeric C Table: case 2: flags |=
                 * SymbologyFlags.SYMBOLOGY_AUS_POST_ALPHANUMERIC_C_TABLE;
                 * break; // Combination N & C Tables: case 3: flags |=
                 * SymbologyFlags.SYMBOLOGY_AUS_POST_COMBINATION_N_AND_C_TABLES;
                 * break; default: break; }
                 */
                //flags |= mScanService.getPropertyInt(PropertyID.AUSTRALIAN_POST_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.POSTAL_GROUP_TYPE_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= SymbologyFlags.SYMBOLOGY_AUS_POST_COMBINATION_N_AND_C_TABLES;
                break;
            /* ===================== */

            case SymbologyID.SYM_MSI:
                // enable, check transmit
                // flags |= sharedPrefs.getBoolean("sym_msi_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                // flags |=
                // sharedPrefs.getBoolean("sym_msi_check_transmit_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                flags |= mScanService.getPropertyInt(PropertyID.MSI_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                boolean enableCk = mScanService.getPropertyInt(PropertyID.MSI_SEND_CHECK) == 1;
                flags |= enableCk ? SymbologyFlags.SYMBOLOGY_CHECK_TRANSMIT : 0;
                min = mScanService.getPropertyInt(PropertyID.MSI_LENGTH1);
                max = mScanService.getPropertyInt(PropertyID.MSI_LENGTH2);
                min = min < min_default ? min_default : min;
                max = max > max_default ? max_default : max;
                break;
            case SymbologyID.SYM_TLCODE39:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_tlcode39_enable", false)
                // ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.COMPOSITE_TLC39_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_MATRIX25:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_matrix25_enable", false)
                // ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.M25_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = mScanService.getPropertyInt(PropertyID.M25_LENGTH1);
                max = mScanService.getPropertyInt(PropertyID.M25_LENGTH2);
                min = min < min_default ? min_default : min;
                max = max > max_default ? max_default : max;
                break;
            case SymbologyID.SYM_KOREAPOST:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_koreapost_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.KOREA_POST_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE  : 0;
                break;
            case SymbologyID.SYM_TRIOPTIC:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_trioptic_enable", false)
                // ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.TRIOPTIC_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_CODE32:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_code32_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.CODE32_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                break;
            case SymbologyID.SYM_STRT25:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_strt25_enable", false) ?
                // SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.D25_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = mScanService.getPropertyInt(PropertyID.D25_LENGTH1);
                max = mScanService.getPropertyInt(PropertyID.D25_LENGTH2);
                min = min < min_default ? min_default : min;
                max = max > max_default ? max_default : max;
                break;
            case SymbologyID.SYM_CHINAPOST:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_chinapost_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                flags |= mScanService.getPropertyInt(PropertyID.C25_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                min = min_default;
                max = max_default;
                break;
            case SymbologyID.SYM_TELEPEN:
                //flags |= SymbologyFlags.SYMBOLOGY_ENABLE;//default enable
                //min = min_default;
                //max = max_default;
                break;
            case SymbologyID.SYM_COUPONCODE:
                // enable
                // flags |= sharedPrefs.getBoolean("sym_couponcode_enable",
                // false) ? SymbologyFlags.SYMBOLOGY_ENABLE : 0;
                //flags |= SymbologyFlags.SYMBOLOGY_ENABLE;//default enable
                break;
            default:
                symConfig.Mask = 0; // will not setSymbologyConfig
                break;
        }

        if (bNotSupported) {
            bNotSupported = false; // // do nothing, but reset flag
        }
        // urovo add shenpidong begin 2019-05-17
        // Flags & Range
        if (symConfig.Mask == (SymbologyFlags.SYM_MASK_FLAGS | SymbologyFlags.SYM_MASK_MIN_LEN | SymbologyFlags.SYM_MASK_MAX_LEN)) {
            symConfig.Flags = flags;
            symConfig.MinLength = min;
            symConfig.MaxLength = max;
            try {
                if (m_decDecoder != null) {
                    m_decDecoder.setSymbologyConfig(symConfig);
                    if(symID == SymbologyID.SYM_MSI && mScanService.getPropertyInt(PropertyID.MSI_ENABLE) == 1) {
                        int checkMode = 0;
                        int ckDigitAlg = mScanService.getPropertyInt(PropertyID.MSI_CHECK_2_MOD_11);
                        int ckDigitMode = mScanService.getPropertyInt(PropertyID.MSI_REQUIRE_2_CHECK);
                        int sendCK = mScanService.getPropertyInt(PropertyID.MSI_SEND_CHECK);
                        if(sendCK == 1) {
                            if(ckDigitMode == 0) {
                                //one check;
                                checkMode = 1;
                            } else {
                                //two check;
                                if(ckDigitAlg == 0) {
                                    //MOD 10/MOD 11
                                    checkMode = 2;
                                } else {
                                    //MOD 10/MOD 10
                                    checkMode = 3;
                                }
                            }
                        } else {
                            if(ckDigitMode == 0) {
                                //one check;
                                checkMode = 5;
                            } else {
                                //two check;
                                if(ckDigitAlg == 0) {
                                    //MOD 10/MOD 11
                                    checkMode = 6;
                                } else {
                                    //MOD 10/MOD 10
                                    checkMode = 7;
                                }
                            }
                        }
                        m_decDecoder.setDecodeParameter(0x40011602 , checkMode);
                    }
                }
            } catch (DecoderException e) {
                Log.d(TAG, "1 EXCEPTION SYMID = " + symID);
                HandleDecoderException(e);
            }
        } else if (m_decDecoder != null && symConfig.Mask == (SymbologyFlags.SYM_MASK_FLAGS)) //Flag Only
        {
            symConfig.Flags = flags;
            try {
                m_decDecoder.setSymbologyConfig(symConfig);
            } catch (DecoderException e) {
                Log.d(TAG, "2 EXCEPTION SYMID = " + symID);
                HandleDecoderException(e);
            }
            if(symID == SymbologyID.SYM_STRT25) {
                symConfig = new SymbologyConfig(0); // symbology config
                symConfig.symID = SymbologyID.SYM_IATA25; // symID
                flags = 0;
                flags |= mScanService.getPropertyInt(PropertyID.D25_ENABLE) == 1 ? SymbologyFlags.SYMBOLOGY_ENABLE  : 0;
                symConfig.Flags = flags;
                try {
                    m_decDecoder.setSymbologyConfig(symConfig);
                } catch (DecoderException e) {
                    Log.d(TAG, "1 EXCEPTION SYMID = " + symID);
                    HandleDecoderException(e);
                }
            }
        } else {
            // invalid
            //Log.d(TAG, "1 invalid SYMID = " + symID);
        }
        if (symID == SymbologyID.SYM_CODE11) {
            int enCKMode = mScanService.getPropertyInt(PropertyID.CODE11_ENABLE_CHECK);//0 no 1 one 2 two
            if(enCKMode == 0) {
                enCKMode = 4;
            } else {
                boolean sendCK = mScanService.getPropertyInt(PropertyID.CODE11_SEND_CHECK) == 1;
                if(enCKMode == 1) {
                    if(sendCK) {
                        enCKMode = 1;
                    } else {
                        enCKMode = 3;
                    }
                } else {
                    if(sendCK) {
                        enCKMode = 0;
                    } else {
                        enCKMode = 2;
                    }
                }
            }
            if (m_decDecoder != null) {
                //0 two check digits;1 one check; 2 two check and stripped form result data; 3 one check and stripped form result data
                try {
                    m_decDecoder.setDecodeParameter(0x40011802, enCKMode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (symID == SymbologyID.SYM_INT25) {
            int enable = mScanService.getPropertyInt(PropertyID.I25_ENABLE);
            if(enable == 1) {
                if (m_decDecoder != null) {
                    try {
                        /*
                        0: Disallow short quiet zone symbols.
                        1: Allow short quiet zone symbols (on one end only). default
                        2: Allow short quiet zone symbols (on both ends).
                        */
                        m_decDecoder.setDecodeParameter(0x40010504, 2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (symID == SymbologyID.SYM_DATAMATRIX) {
            int enable = mScanService.getPropertyInt(PropertyID.DATAMATRIX_ENABLE);
            if(enable == 1) {
                if (m_decDecoder != null) {
                    try {
                        //lower-left 忽略L标识不规范，影响解码
                        m_decDecoder.setDecodeParameter(0x40010418, 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else if (symID == SymbologyID.SYM_CODE128) {
            int enable = mScanService.getPropertyInt(PropertyID.CODE128_ENABLE);
            if(enable == 1) {
                if (m_decDecoder != null) {
                    int val = mScanService.getPropertyInt(PropertyID.C128_OUT_OF_SPEC);
                    if (val < 0 || val > 15) {
                        val = 0;
                    }
                    try {
                        m_decDecoder.setDecodeParameter(DCLProperties.DEC_C128_OUT_OF_SPEC_SYMBOL, val);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // urovo add shenpidong end 2019-05-17
    }

    private void setDecodeParameter(int property, int value) {
        if (m_decDecoder != null) {
            try {
                m_decDecoder.setDecodeParameter(property, value); //
            } catch (DecoderException e) {
                HandleDecoderException(e);
            }
        }
    }

    /**
     * Sets exposure settings
     */
    private void setExposureSettings(int mode) {
        if (m_decDecoder == null) {
            Log.d(TAG, "SetExposureSettings, m_decDecoder:" + m_decDecoder);
            return;
        }
        Log.d(TAG, "SetExposureSettings");
        Log.d(TAG, "setProperties property IMAGE_EXPOSURE_MODE= " + mode);
        /*try {
            m_decDecoder.setExposureMode(mode);
        } catch (DecoderException e) {
            // TODO Auto-generated catch block
            HandleDecoderException(e);
        }*/
        try {
            m_decDecoder.getExposureSettings(g_nExposureSettings);
        } catch (DecoderException e) {
            HandleDecoderException(e);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        //if(mode != ExposureMode.FIXED) return;
        int tag = 0;
        int esValue = 0;
        for (int i = 0; i < g_nExposureSettings.length; i++) {
            tag = g_nExposureSettings[i];
            esValue = g_nExposureSettings[++i];
            switch (tag) {
                case ExposureSettings.DEC_ES_FIXED_EXP:
                    if (mode == ExposureMode.FIXED) {
                        int value = mScanService.getPropertyInt(PropertyID.IMAGE_FIXED_EXPOSURE);
                        // urovo add shenpidong begin 2019-07-09
                        esValue = (value == -1 ? 200 : value);
                        // urovo add shenpidong end 2019-07-09
                    } else {
                        Log.d(TAG, "DEC_ES_FIXED_EXP = " + esValue);
                        esValue = 0;
                    }
                    break;
                default:
                    break;
            }
            Log.d(TAG, "ExposureSettings tag = " + tag + " value=" + esValue);
            g_nExposureSettings[i] = esValue;
        }
        try {
            // urovo add shenpidong begin 2019-05-17
            if (m_decDecoder != null) {
                m_decDecoder.setExposureSettings(g_nExposureSettings);
            }
            // urovo add shenpidong end 2019-05-17
        } catch (DecoderException e) {
            HandleDecoderException(e);
        }
    }
    //1.CLSI Editing 启用时，此参数将删除起始字符和结束字符，并在14个字符的Codabar符号的第一个、第五个和第十个字符后插入空格。符号长度不包括开始和结束字符。
    //2.NOTIS Editing 开启该功能后，解码器会忽略起始位和结束位
    private byte[] codabarCLSIEditing(byte[] dataBuffer, int length) {
        if(NOTIS_enable && (dataBuffer[0] == 'A' || dataBuffer[0] == 'B' || dataBuffer[0] == 'C' || dataBuffer[0] == 'D' ||
                dataBuffer[0] == 'a' || dataBuffer[0] == 'b' || dataBuffer[0] == 'c' || dataBuffer[0] == 'd')) {
            if(length == 16) {
                //A12345678901234A
                byte[] dstBuffer = new byte[length + 1];
                int index = 0;
                for (int i = 1; i < length - 1; ++i) {
                    if(index == 1 || index == 6 || index == 11) {
                        dstBuffer[index] = 0x20;
                        ++index;
                        dstBuffer[index] = dataBuffer[i];
                    } else {
                        dstBuffer[index] = dataBuffer[i];
                    }
                    index++;
                }
                return dstBuffer;
            }
        } else {
            if(length == 14) {
                //12345678901234 >> 1 2345 67890 1234
                byte[] dstBuffer = new byte[length + 3];
                int index = 0;
                for (int i = 0; i < length; ++i) {
                    if(index == 1 || index == 6 || index == 11) {
                        dstBuffer[index] = 0x20;
                        ++index;
                        dstBuffer[index] = dataBuffer[i];
                    } else {
                        dstBuffer[index] = dataBuffer[i];
                    }
                    index++;
                }
                return dstBuffer;
            }
        }
        return null;
    }
    /**
     * Sets the OCR settings based on user preferences
     *
     * @throws DecoderException
     */
    private void setOcrSettings() throws DecoderException {
        Log.d(TAG, "SetOcrSettings++");
        if (m_decDecoder != null) {
            int ocr_mode = 0;
            int ocr_template = 0;
            byte[] ocr_user_defined_template;
            ocr_mode = mScanService.getPropertyInt(PropertyID.DEC_OCR_MODE);
            ocr_template = mScanService.getPropertyInt(PropertyID.DEC_OCR_TEMPLATE);
            String userDefinedTemplate = mScanService.getPropertyString(PropertyID.DEC_OCR_USER_TEMPLATE);
            Log.d(TAG, "SetOcrSettings++ ocr_mode " + ocr_mode);
            if (ocr_mode > DecoderConfigValues.OCRMode.OCR_OFF) {
                m_decDecoder.setOCRMode(ocr_mode);
                if (ocr_template == 0) ocr_template = DecoderConfigValues.OCRTemplate.USER;
                else if (ocr_template == 1) ocr_template = DecoderConfigValues.OCRTemplate.PASSPORT;
                else if (ocr_template == 2) ocr_template = DecoderConfigValues.OCRTemplate.ISBN;
                else if (ocr_template == 3)
                    ocr_template = DecoderConfigValues.OCRTemplate.PRICE_FIELD;
                else if (ocr_template == 4) ocr_template = DecoderConfigValues.OCRTemplate.MICRE13B;
                Log.d(TAG, "SetOcrSettings++ ocr_template " + ocr_template);
                m_decDecoder.setOCRTemplates(ocr_template);
                //default Template "13777777770" to bytearrary  01 03 07 07 07 07 00
                //1,2,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,8,8,0
                if (ocr_template == DecoderConfigValues.OCRTemplate.USER) {
                    try {
                        //userDefinedTemplate = "1.3777778E10";
                        if (userDefinedTemplate != null && !userDefinedTemplate.equals("")) {
                            ocr_user_defined_template = userDefinedTemplate.getBytes();
                            //char[] templateChar = userDefinedTemplate.toCharArray();
                            for (int i = 0; i < ocr_user_defined_template.length; i++) {
                                if (ocr_user_defined_template[i] >= 48)
                                    ocr_user_defined_template[i] = (byte) (ocr_user_defined_template[i] - 48);
                            }
                            // urovo add shenpidong begin 2019-05-17
                            if (m_decDecoder != null) {
                                m_decDecoder.setOCRUserTemplate(ocr_user_defined_template);
                            }
                            // urovo add shenpidong end 2019-05-17
                        }
                    } catch (DecoderException e) {
                        HandleDecoderException(e);
                    }
                }
            } else {
                m_decDecoder.setOCRMode(DecoderConfigValues.OCRMode.OCR_OFF);
            }
            Log.d(TAG, "SetOcrSettings--");
        }
    }

    void getOcrSettings() {
        int default_ocr_mode = -1;
        int default_template = -1;
        byte[] default_ocr_user_template = null;
        String default_ocr_user_template_string = null;
        if (m_decDecoder == null) {
            Log.d(TAG, "getOcrSettings, m_decDecoder:" + m_decDecoder);
            return;
        }
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
    }

    @Override
    protected void release() {
        // TODO Auto-generated method stub
        try {
            if (m_decDecoder != null) {
//                m_decDecoder.stopScanning();
                Message m = Message.obtain(mWorkHandler, WorkHandler.MESSAGE_DECODE_STOP);
                mWorkHandler.sendMessage(m);
                isDecoding = false;
                m_decDecoder.disconnectDecoderLibrary();
            }
        } catch (DecoderException e) {
            HandleDecoderException(e);
        }
        m_decDecoder = null;
    }

    @Override
    public boolean lockHwTriggler(boolean lock) {
        // TODO Auto-generated method stub
        return false;
    }

    // urovo add by shenpidong begin 2020-03-23
    private static class FullReadModeDecodeResult {
        byte aimID;
        byte aimModifier;
        byte codeID;
        byte[] barcodeByteData;
        int decResultLength;
    }
    // urovo add by shenpidong end 2020-03-23

    private String preDecodeData = "";
    // urovo add by shenpidong begin 2020-03-17
    private String[] currentDecodeDataArr = null;
    private int currentDecodeDataArrIndex = 0;
    private String[] preDecodeDataArr = null;
    // urovo add by shenpidong end 2020-03-17
    // urovo add by shenpidong begin 2020-03-23
    private FullReadModeDecodeResult[] mFullReadModeDecodeResult = null;
    // urovo add by shenpidong end 2020-03-23
    // urovo add shenpidong begin 2019-09-12
    private long waitMultipleStartTime = 0;
    // urovo add shenpidong end 2019-09-12
    private static boolean bWaitMultiple = true;    // flag for single or multiple decode
    private int g_nMultiReadResultCount = 0;        // For tracking # of multiread results
    private int g_nMaxMultiReadCount = 2;        // Maximum multiread count
    private int waitMultipleMode = 0;
    private int waitMultipleTiemOut = 5 * 1000;
    // urovo add shenpidong begin 2019-09-12
    private int waitMultipleInterval = 10;          // urovo tao.he add, for scan interval

    // urovo add shenpidong end 2019-09-12
    private void setDecodeOptions() {
        try {
            if (m_decDecoder == null) {
                Log.d(TAG, "setDecodeOptions , m_decDecoder:" + m_decDecoder);
                return;
            }
            m_decDecoder.setDecoderListeners(this);
            DecodeOptions decOpt = new DecodeOptions();
            m_decDecoder.getDecodeOptions(decOpt);
            //decOpt.DecAttemptLimit = -1; // ignore
            //decOpt.VideoReverse = -1; // ignore
            Log.d(TAG, "setDecodeOptions , DecAttemptLimit:" + decOpt.DecAttemptLimit);
            // urovo add by shenpidong begin 2020-03-17
            if (waitMultipleMode > 0 && !isMultiDecodeMode) {
                decOpt.MultiReadCount = g_nMaxMultiReadCount;
            } else {
                decOpt.MultiReadCount = isMultiDecodeCount;
            }
            currentDecodeDataArr = null;
            preDecodeDataArr = null;
            currentDecodeDataArrIndex = 0;
            // urovo add by shenpidong begin 2020-03-23
            mFullReadModeDecodeResult = null;
            // urovo add by shenpidong end 2020-03-23
            if (decOpt.MultiReadCount > 0) {
                currentDecodeDataArrIndex = decOpt.MultiReadCount;
                currentDecodeDataArr = new String[decOpt.MultiReadCount];
                preDecodeDataArr = new String[decOpt.MultiReadCount];
                mFullReadModeDecodeResult = new FullReadModeDecodeResult[decOpt.MultiReadCount];
            }
            // urovo add by shenpidong end 2020-03-17
            m_decDecoder.setDecodeOptions(decOpt);
        } catch (DecoderException e) {
            HandleDecoderException(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Callback when multiple decode results are available
     */
    @Override
    public boolean onMultiReadCallback() {
        Log.d(TAG, "onMultipleDecodeResults , MultipleMode:" + waitMultipleMode + ",MultiDecodeMode:" + isMultiDecodeMode);
        //单次解码模式，延时解码输出
        if (delayPicklistAimMode && !isContinueScanMode) {
            //在delayDecodeTime时间内解码不出输出结果
            ignoreDecodeTime += System.currentTimeMillis() - decodeStartTime;
            if(ignoreDecodeTime >= delayDecodeTime) {
                return false;
            } else {
                return true;
            }
        }
        // urovo add shenpidong begin 2019-09-12
        // urovo add by shenpidong begin 2020-03-17
        // Do something with the results
        if (DisplayMultireadResults() && waitMultipleMode == 1 && !isMultiDecodeMode) {
            // Give the UI thread time
			/*
     		try {
			    Thread.sleep(50);
		    } catch (InterruptedException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
		    }*/
            return false;
        } else {
            // Give the UI thread time
			/*
     		try {
			    Thread.sleep(50);
		    } catch (InterruptedException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
		    }*/
            // Stop scanning if max count is acheived
            Log.d(TAG, "g_nMultiReadResultCount=" + g_nMultiReadResultCount + ",g_nMaxMultiReadCount=" + g_nMaxMultiReadCount + ",MultiDecodeMode:" + isMultiDecodeMode + ",MultiDecodeCount:" + isMultiDecodeCount);
            if ((waitMultipleMode == 2 && g_nMultiReadResultCount == g_nMaxMultiReadCount) || (isMultiDecodeMode && isMultiDecodeCount > 1 && g_nMultiReadResultCount == isMultiDecodeCount)) {
                if (preDecodeDataArr == null) {
                    preDecodeDataArr = new String[currentDecodeDataArr.length];
                }
//		    Log.d(TAG, "currentDecodeDataArr.length:" + currentDecodeDataArr.length + ",preDecodeDataArr.length:" + preDecodeDataArr.length);
                if (currentDecodeDataArr != null && preDecodeDataArr != null && currentDecodeDataArr.length == preDecodeDataArr.length) {
                    System.arraycopy(currentDecodeDataArr, 0, preDecodeDataArr, 0, preDecodeDataArr.length);
                    // urovo add by shenpidong begin 2020-03-23
                    if (isMultiDecodeMode && isFullReadMode == 1) {
                        if (mFullReadModeDecodeResult != null) {
                            for (int i = 0; i < mFullReadModeDecodeResult.length; i++) {
                                if (mFullReadModeDecodeResult[i] != null) {
                                    mayBeseparatorDecode(mFullReadModeDecodeResult[i].aimID, mFullReadModeDecodeResult[i].aimModifier,
                                            mFullReadModeDecodeResult[i].codeID, mFullReadModeDecodeResult[i].barcodeByteData, mFullReadModeDecodeResult[i].decResultLength);
                                } else {
                                    Log.d(TAG, "Full Read Mode DecodeResult:" + (mFullReadModeDecodeResult[i] != null));
                                }
                            }
                        }
                    }
                    // urovo add by shenpidong end 2020-03-23
/*
			for(int i=0;i<preDecodeDataArr.length;i++) {
			    Log.d(TAG, "preDecodeDataArr[" + i + "]:" + preDecodeDataArr[i]);
			}
			for(int i=0;i<currentDecodeDataArr.length;i++) {
			    currentDecodeDataArr[i] = "";
			}
*/
                }
                Log.d(TAG, "MAX MULTI!! Read Mode:" + isFullReadMode);
                return false;
            }
            // urovo add by shenpidong end 2020-03-17
            return true;
        }
        // urovo add shenpidong end 2019-09-12
    }

    /**
     * Displays results when reading mulitple barcodes
     */
    private boolean DisplayMultireadResults() {
        // urovo add by shenpidong begin 2020-03-17
        try {
            // pull the data manually:
            if (m_decDecoder != null && m_decDecoder.getBarcodeLength() > 0) {
                if (m_decDecoder.getBarcodeData() != null) {
                    // urovo add shenpidong begin 2019-09-12
                    boolean multipleTimeOut = System.currentTimeMillis() - waitMultipleStartTime < waitMultipleTiemOut;
                    boolean isMultiDecodeTwoBarcode = isMultiDecodeCount <= 0/* || g_nMaxMultiReadCount <= 2*/;
                    boolean repeatWaitMultipleMode = waitMultipleMode == 1 && isContinueScanMode;
//		    Log.d(TAG , "DisplayMultireadResults , m_decDecoder.getBarcodeData:" + m_decDecoder.getBarcodeData() + ",multipleTimeOut:" + multipleTimeOut + ",isMultiDecodeTwoBarcode:" + isMultiDecodeTwoBarcode + ",isMultiDecodeCount:" + isMultiDecodeCount + ",g_nMaxMultiReadCount:" + g_nMaxMultiReadCount + ",repeatWaitMultipleMode:" + repeatWaitMultipleMode + ",g_nMultiReadResultCount:" + g_nMultiReadResultCount);
                    if (!isMultiDecodeMode && repeatWaitMultipleMode/*isMultiDecodeTwoBarcode*/ && multipleTimeOut && m_decDecoder.getBarcodeData().equals(preDecodeData)) {
                        return false;
                    }
                    Log.d(TAG, "DisplayMultireadResults , repeatWaitMultipleMode:" + repeatWaitMultipleMode + ",preDecodeDataArr:" + preDecodeDataArr);
                    if (repeatWaitMultipleMode && multipleTimeOut && preDecodeDataArr != null && preDecodeDataArr.length > 0) {
                        for (int i = 0; i < preDecodeDataArr.length; i++) {
//			    Log.d(TAG , "DisplayMultireadResults , preDecodeDataArr[" + i + "]:" + preDecodeDataArr[i]);
                            if (m_decDecoder.getBarcodeData().equals(preDecodeDataArr[i])) {
                                preDecodeDataArr[i] = "";
                                Log.d(TAG, "DisplayMultireadResults , BarcodeData:" + m_decDecoder.getBarcodeData() + ",pre index:" + i);
                                return false;
                            }
                        }
                    }
                    if (!isMultiDecodeMode && repeatWaitMultipleMode/*isMultiDecodeTwoBarcode*/) {
                        preDecodeData = m_decDecoder.getBarcodeData();
                    } else {
                        if (currentDecodeDataArr != null && g_nMultiReadResultCount == 0) {
                            for (int i = 0; i < currentDecodeDataArr.length; i++) {
                                Log.d(TAG, "DisplayMultireadResults , reset currentDataArr[" + i + "]:" + currentDecodeDataArr[i]);
                                currentDecodeDataArr[i] = "";
                            }
                        }
                        // urovo add by shenpidong begin 2020-03-23
                        if (mFullReadModeDecodeResult != null && g_nMultiReadResultCount == 0) {
                            for (int i = 0; i < mFullReadModeDecodeResult.length; i++) {
                                if (mFullReadModeDecodeResult[i] != null) {
                                    mFullReadModeDecodeResult[i].aimID = 0;
                                    mFullReadModeDecodeResult[i].aimModifier = 0;
                                    mFullReadModeDecodeResult[i].codeID = 0;
                                    mFullReadModeDecodeResult[i].barcodeByteData = null;
                                    mFullReadModeDecodeResult[i].decResultLength = 0;
                                }
                            }
                        }
                        // urovo add by shenpidong end 2020-03-23
                        if (currentDecodeDataArr != null && g_nMultiReadResultCount < currentDecodeDataArr.length) {
                            for (int i = 0; i < g_nMultiReadResultCount; i++) {
//				Log.d(TAG , "DisplayMultireadResults , currentDataArr[" + i + "]:" + currentDecodeDataArr[i]);
                                if (m_decDecoder.getBarcodeData().equals(currentDecodeDataArr[i])) {
                                    Log.d(TAG, "DisplayMultireadResults , BarcodeData:" + m_decDecoder.getBarcodeData() + ",index:" + i + ",Arr[" + i + "]:" + currentDecodeDataArr[i]);
                                    currentDecodeDataArr[i] = "";
                                    return false;
                                }
                            }
                            currentDecodeDataArr[g_nMultiReadResultCount] = m_decDecoder.getBarcodeData();
                            preDecodeData = currentDecodeDataArr[g_nMultiReadResultCount];
                            Log.d(TAG, "DisplayMultireadResults , m_decDecoder.getBarcodeData:" + m_decDecoder.getBarcodeData() + ",currentDecodeDataArr[" + g_nMultiReadResultCount + "]:" + currentDecodeDataArr[g_nMultiReadResultCount]);
                        } else {
                            preDecodeData = m_decDecoder.getBarcodeData();
                        }
                    }
                    waitMultipleStartTime = System.currentTimeMillis();
                    // urovo add shenpidong end 2019-09-12
                    byte aimID = m_decDecoder.getBarcodeAimID();
                    byte aimModifier = m_decDecoder.getBarcodeAimModifier();
                    byte codeID = m_decDecoder.getBarcodeCodeID();
                    Log.d(TAG, "DisplayMultireadResults , currentDecodeDataArr:" + (currentDecodeDataArr != null) + ",MultiDecode:" + isMultiDecodeMode + ",full Read Mode:" + isFullReadMode);
                    if (!isMultiDecodeMode || isFullReadMode != 1 || (isMultiDecodeMode && isMultiDecodeCount == 1)) {
                        mayBeseparatorDecode(aimID, aimModifier, codeID, preDecodeData.getBytes(), m_decDecoder.getBarcodeLength());
                    } else {
                        // urovo add by shenpidong begin 2020-03-23
                        if (mFullReadModeDecodeResult == null && currentDecodeDataArr != null) {
                            mFullReadModeDecodeResult = new FullReadModeDecodeResult[currentDecodeDataArr.length];
                        }
                        if (mFullReadModeDecodeResult != null && g_nMultiReadResultCount < mFullReadModeDecodeResult.length) {
                            if (mFullReadModeDecodeResult[g_nMultiReadResultCount] == null) {
                                mFullReadModeDecodeResult[g_nMultiReadResultCount] = new FullReadModeDecodeResult();
                            }
                            mFullReadModeDecodeResult[g_nMultiReadResultCount].aimID = aimID;
                            mFullReadModeDecodeResult[g_nMultiReadResultCount].aimModifier = aimModifier;
                            mFullReadModeDecodeResult[g_nMultiReadResultCount].codeID = codeID;
                            if (mFullReadModeDecodeResult[g_nMultiReadResultCount].barcodeByteData == null) {
                                mFullReadModeDecodeResult[g_nMultiReadResultCount].barcodeByteData = new byte[m_decDecoder.getBarcodeLength()];
                            }
                            mFullReadModeDecodeResult[g_nMultiReadResultCount].barcodeByteData = preDecodeData.getBytes();
                            mFullReadModeDecodeResult[g_nMultiReadResultCount].decResultLength = m_decDecoder.getBarcodeLength();
                        } else {
                            Log.d(TAG, "DisplayMultireadResults , FullReadMode:" + (mFullReadModeDecodeResult != null) + ",DataArr:" + (currentDecodeDataArr != null));
                            mayBeseparatorDecode(aimID, aimModifier, codeID, preDecodeData.getBytes(), m_decDecoder.getBarcodeLength());
                        }
                        // urovo add by shenpidong end 2020-03-23
                    }
                    g_nMultiReadResultCount++;
                    //Log.d(TAG, "Additional Multiread Results " + preDecodeData);
                    //Log.d(TAG, "  AimID:" + m_decDecoder.getBarcodeAimID() );
                    //Log.d(TAG, "  AimModifier:" + m_decDecoder.getBarcodeAimModifier() );
                    //Log.d(TAG, "  CodeID:" + m_decDecoder.getBarcodeCodeID() );
                    //Log.d(TAG, "  Length:" + m_decDecoder.getBarcodeLength());
                } else if (null != m_decDecoder.getBarcodeByteData()) {
                    byte aimID = m_decDecoder.getBarcodeAimID();
                    byte aimModifier = m_decDecoder.getBarcodeAimModifier();
                    byte codeID = m_decDecoder.getBarcodeCodeID();
                    Log.d(TAG, "DisplayMultireadResults ,2  currentDecodeDataArr:" + (currentDecodeDataArr != null));
                    mayBeseparatorDecode(aimID, aimModifier, codeID, m_decDecoder.getBarcodeByteData(), m_decDecoder.getBarcodeLength());
                    //Log.d(TAG, "  Length:" + m_decDecoder.getBarcodeLength());
                }
            } else {
                Log.d(TAG, g_nMultiReadResultCount + ": " + "!! No Data !!" + "\n");
            }
            Log.d(TAG, "display g_nMultiReadResultCount = " + g_nMultiReadResultCount);
        } catch (DecoderException e) {
            HandleDecoderException(e);
        }
        // urovo add by shenpidong end 2020-03-17
        return true;
    }

    /**
     * Callback to keep scanning (i.e. trigger callback)
     */
    @Override
    public boolean onKeepGoingCallback() {
        //Log.d(TAG, "onKeepGoingCallback isDecoding = " + isDecoding);
        return isDecoding;
    }

    private synchronized void mayBeseparatorDecode(byte aimID, byte aimModifier, byte codeID, byte[] barcodeByteData, int decResultLength) {
        int modeCodeID = mScanService.getPropertyInt(PropertyID.TRANSMIT_CODE_ID);
        aimCodeId[0] = ']';
        aimCodeId[1] = aimID;
        aimCodeId[2] = aimModifier;
        if (mScanService.getPropertyInt(PropertyID.LABEL_SEPARATOR_ENABLE) == 1 && SeparatorDecodeUtil.isSeparatorDecode(aimID, aimModifier, codeID)) {
            String sepChar = mScanService.getPropertyString(PropertyID.LABEL_FORMAT_SEPARATOR_CHAR);
            if (!TextUtils.isEmpty(sepChar)) {
                SeparatorDecodeUtil.setSeparatorChar(sepChar.getBytes());
            }
            // urovo modify shenpidong begin 2019-11-28
//            byte[] resultSeparator = SeparatorDecodeUtil.separatorDecode(barcodeByteData);
            byte[] resultSeparator = null;
            int compositeIndex = SeparatorDecodeUtil.compositeIndexCode(codeID);
            if (compositeIndex > 0 && barcodeByteData.length > compositeIndex) {
                byte[] compositeByte = new byte[barcodeByteData.length - compositeIndex];
                System.arraycopy(barcodeByteData, compositeIndex, compositeByte, 0, compositeByte.length);
                byte[] compositeByteData = SeparatorDecodeUtil.separatorDecode(compositeByte);
                if (compositeByteData != null && compositeByte.length != compositeByteData.length) {
                    resultSeparator = new byte[compositeIndex + compositeByteData.length];
                    System.arraycopy(barcodeByteData, 0, resultSeparator, 0, compositeIndex);
                    System.arraycopy(compositeByteData, 0, resultSeparator, compositeIndex, compositeByteData.length);
                } else {
                    Log.d(TAG, "mayBeseparatorDecode , composite error!!! composite Byte len:" + compositeByte.length + " , Data len:" + (compositeByteData != null ? compositeByteData.length : 0) + ",composite:" + (compositeByteData != null));
                    resultSeparator = SeparatorDecodeUtil.separatorDecode(barcodeByteData);
                }
            } else {
                if (!SeparatorDecodeUtil.isSupperCompositeCode(aimID, aimModifier, codeID)) {
                    resultSeparator = barcodeByteData;
                } else {
                    resultSeparator = SeparatorDecodeUtil.separatorDecode(barcodeByteData);
                }
            }
//	    Log.d(TAG , "mayBeseparatorDecode , aimID:" + aimID + ",aimModifier:" + aimModifier + ",codeID:" + codeID + ",length:" + (resultSeparator!=null ? resultSeparator.length:-1));
            // urovo modify shenpidong end 2019-11-28
    /*
            Log.d(TAG , "mayBeseparatorDecode ========================================================= start m_decResult.barcodeData:" + m_decResult.barcodeData);
            for(int i=0;i<barcodeByteData.length;i++) {
            Log.d(TAG , "mayBeseparatorDecode , barcodeByteData[" + i + "]:" + barcodeByteData[i]);
            }
            Log.d(TAG , "mayBeseparatorDecode ========================================================= mid");
            for(int i=0;i<resultSeparator.length;i++) {
            Log.d(TAG , "mayBeseparatorDecode , resultSeparator[" + i + "]:" + resultSeparator[i]);
            }
            Log.d(TAG , "mayBeseparatorDecode ========================================================= end");
    */
            if (resultSeparator != null && resultSeparator.length > 0) {
                try {
                    if (modeCodeID > 0) {
                        byte[] byteArraryData;
                        if (modeCodeID == 1) {
                            String AimID = String.format("]%c%c", aimID, aimModifier);
                            int aimLen = AimID.length();
                            byteArraryData = new byte[aimLen + resultSeparator.length];
                            byte[] AimIDArray = AimID.getBytes();
                            System.arraycopy(AimIDArray, 0, byteArraryData, 0, aimLen);
                            System.arraycopy(resultSeparator, 0, byteArraryData, aimLen, resultSeparator.length);
                        } else {
                            String CodeID = String.format("%c", codeID);
                            byte[] CodeIDArray = CodeID.getBytes();
                            byteArraryData = new byte[1 + resultSeparator.length];
                            byteArraryData[0] = CodeIDArray[0];
                            System.arraycopy(resultSeparator, 0, byteArraryData, 1, resultSeparator.length);
                        }
                        sendBroadcast(byteArraryData, codeID, byteArraryData.length);
                    } else {
                        sendBroadcast(resultSeparator, codeID, resultSeparator.length);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sendBroadcast(resultSeparator, codeID, resultSeparator.length);
                }
                //return;
            } else {
                Log.d(TAG, "mayBeseparatorDecode , aimID:" + aimID + ",aimModifier:" + aimModifier + ",codeID:" + codeID + ",length:" + (resultSeparator != null ? resultSeparator.length : -1));
            }
        } else {
            try {
                if(CLSI_enable
                        && aimID == 'F'
                        && codeID == 'a'
                        && (barcodeByteData.length == 14 || (barcodeByteData.length == 16 && NOTIS_enable))) {
                        //长度16位可能是包含开始停止字符
                    byte[] dstBuffer = codabarCLSIEditing(barcodeByteData, barcodeByteData.length);
                    if(dstBuffer != null) {
                        barcodeByteData = dstBuffer;
                        decResultLength = barcodeByteData.length;
                    }
                }
                if (modeCodeID > 0) {
                    byte[] byteArraryData;
                    if (modeCodeID == 1) {
                        String AimID = String.format("]%c%c", aimID, aimModifier);
                        int aimLen = AimID.length();
                        byteArraryData = new byte[aimLen + barcodeByteData.length];
                        byte[] AimIDArray = AimID.getBytes();
                        System.arraycopy(AimIDArray, 0, byteArraryData, 0, aimLen);
                        System.arraycopy(barcodeByteData, 0, byteArraryData, aimLen, barcodeByteData.length);
                    } else {
                        String CodeID = String.format("%c", codeID);
                        byte[] CodeIDArray = CodeID.getBytes();
                        byteArraryData = new byte[1 + barcodeByteData.length];
                        byteArraryData[0] = CodeIDArray[0];
                        System.arraycopy(barcodeByteData, 0, byteArraryData, 1, barcodeByteData.length);
                    }
                    // urovo add shenpidong begin 2019-05-06
                    if (mScanService != null && mScanService.isRemoveNonPrintChar()) {
                        ScanUtil.searchLoopGSAndReplase(byteArraryData);
                    }
                    // urovo add shenpidong end 2019-05-06
                    sendBroadcast(byteArraryData, codeID, byteArraryData.length);
                } else {
                    // urovo add shenpidong begin 2019-05-06
                    if (mScanService != null && mScanService.isRemoveNonPrintChar()) {
                        ScanUtil.searchLoopGSAndReplase(barcodeByteData);
                    }
                    // urovo add shenpidong end 2019-05-06
                    sendBroadcast(barcodeByteData, codeID, decResultLength);
                }
            } catch (Exception e) {
                e.printStackTrace();
                // urovo add shenpidong begin 2019-05-06
                if (mScanService != null && mScanService.isRemoveNonPrintChar()) {
                    ScanUtil.searchLoopGSAndReplase(barcodeByteData);
                }
                // urovo add shenpidong end 2019-05-06
                sendBroadcast(barcodeByteData, codeID, decResultLength);
            }
        }
        try {
            if (enableLastDecImage && !isContinueScanMode) {
                ImageAttributes attr = new ImageAttributes();
                lastImageData = m_decDecoder.getLastImage(attr);
            }
        } catch (Exception e) {
        }
    }

    WorkHandler mWorkHandler;
    WorkHandlerThread mWorkHandlerThread;
    private long decodeEndTime, decodeStartTime, decodeTime;
    // urovo add by shenpidong begin 2020-04-10
    private boolean delayPicklistAimMode = false;
    private int delayDecodeTime = 0;
    private int ignoreDecodeTime = 0;
    // urovo add by shenpidong end 2020-04-10

    private class WorkHandler extends Handler {
        public static final int MESSAGE_DECODE_START = 1;
        public static final int MESSAGE_DECODE_STOP = 2;
        public static final int MESSAGE_DECODE_TIMEOUT = 3;
        public static final int MESSAGE_DECODE_CONFIG = 4;
        public static final int MESSAGE_CAPTURE_IMAGE = 5;
        private final Object decodeEvent = new Object();
        private boolean decodeNotified = false;
        private DecodeNotifyThread DecodeNotify = new DecodeNotifyThread();
        private Object mLock;

        public WorkHandler(Looper loop, Object o) {
            super(loop);
            mLock = o;
            DecodeNotify.start();
        }

        private class DecodeNotifyThread extends Thread {

            @Override
            public void run() {
                while (true) {
                    boolean multipeDecode = false;
                    synchronized (decodeEvent) {
                        while (!decodeNotified) {
                            try {
                                Log.d(TAG, "decodeEvent.wait() isDecoding " + isDecoding);
                                decodeEvent.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        //不是连扫模式,解码成功后进入线程堵塞等待
                        multipeDecode = (isContinueScanMode == false);//mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) != 4;
                        if (multipeDecode)
                            decodeNotified = false; // loop todo
                    }
                    // Log.d(TAG, "no wait"+decodeNotified);
                    try {
                        if (!isDecoding) {
                            decodeStartTime = System.currentTimeMillis();
                            if (m_decDecoder != null) {
                                isDecoding = true;
                                try {
                                    Log.d(TAG, "waitForDecodeTwo start " + decodeNotified + waitMultipleMode);
                                    // urovo add by shenpidong begin 2020-03-17
//                                    Log.d(TAG, "waitForDecodeTwo start waitMultipleTiemOut:" + waitMultipleTiemOut + ",isMultiDecodeMode:" + isMultiDecodeMode + ",isMultiDecodeCount:" + isMultiDecodeCount);
                                    if (!isDisconnectScan6603) {     //add by qiuzhoujun
                                        isWaitDecodeReturned = false; //add by qiuzhoujun
                                        g_nMultiReadResultCount = 0;
                                        //wait for multiple by decoder callback
                                        if (decodeNotified && waitMultipleMode > 0) {
//                                            g_nMultiReadResultCount = 0;
                                            m_decDecoder.waitMultipleDecode(waitMultipleTiemOut);
                                        } else if ((isMultiDecodeMode && isMultiDecodeCount > 1) || (waitMultipleMode == 2 && g_nMaxMultiReadCount > 1)) {
//                                          //多解码and去重码
                                            m_decDecoder.waitMultipleDecode(waitMultipleTiemOut);                    // wait for multiple
                                        } else {
                                            //单次解码模式，连续重复解码
                                            // urovo add by shenpidong begin 2020-04-10
                                            if (delayPicklistAimMode && !isContinueScanMode && delayDecodeTime > 0) {
                                                //在delayDecodeTime时间内解码不出输出结果
                                                if(ignoreDecodeTime < delayDecodeTime) {
                                                    m_decDecoder.waitMultipleDecode(ignoreDecodeTime == 0 ? delayDecodeTime : java.lang.Math.abs(delayDecodeTime - ignoreDecodeTime));
                                                    sleep(15); // TODO: sleep for 50 ms before doing again?
                                                    Log.d(TAG, "delay out");
                                                } else {
                                                    isDecoding = true;
                                                    Log.d(TAG, "decode.");
                                                    decodeNotified = false;
                                                }
                                            }
                                            // urovo add by shenpidong end 2020-04-10
                                            m_decDecoder.waitForDecodeTwo(decodeNotified ? 10 * 1000
                                                    : g_nDecodeTimeout, m_decResult);
                                        }
                                        // urovo add by shenpidong end 2020-03-17
                                        isDecoding = false;
                                        isWaitDecodeReturned = true; //add by qiuzhoujun
                                        decodeEndTime = System.currentTimeMillis();
                                        decodeTime = decodeEndTime - decodeStartTime;
                                        laserTriggerTime = decodeTime;
                                        Log.d(TAG, "waitForDecodeTwo returned " + decodeTime
                                                + decodeNotified);

                                        //Log.d(TAG, "m_decResult.length " + m_decResult.length);
                                        // urovo add shenpidong begin 2019-07-27
                                        // urovo add by shenpidong begin 2020-03-17
                                        if ((!decodeNotified || waitMultipleMode == 0) && m_decResult.length > 0 && g_nMultiReadResultCount == 0) {
                                            Log.d(TAG, "waitForDecodeTwo  decResult.len:" + m_decResult.length);
                                            // urovo add by shenpidong end 2020-03-17
                                            boolean isSeparator = false;
                                            byte aimID = m_decDecoder.getBarcodeAimID();
                                            byte aimModifier = m_decDecoder.getBarcodeAimModifier();
                                            byte codeID = m_decDecoder.getBarcodeCodeID();
                                            decodeSessionTime = m_decDecoder.getLastDecodeTime();
                                            boolean isBarcodeUPCE1 = ScanUtil.isBarCodeUPC_E1(aimID, aimModifier, codeID);
                                            byte checksum = -1;
                                            byte[] codeData = null;
                                            if (m_decResult.barcodeData.length() != 0) {
                                                if (isBarcodeUPCE1) {
                                                    checksum = ScanUtil.barCodeUPC_E1_Checksum(m_decResult.barcodeData.getBytes());
                                                }
                                                if (checksum > 0) {
                                                    codeData = new byte[m_decResult.length + 1];
                                                    System.arraycopy(m_decResult.barcodeData.getBytes(), 0, codeData, 0, m_decResult.length);
                                                    codeData[codeData.length - 1] = checksum;
                                                }
                                                isBarcodeUPCE1 = isBarcodeUPCE1 && checksum > 0;
                                                mayBeseparatorDecode(aimID, aimModifier, codeID,
                                                        isBarcodeUPCE1 ? codeData : m_decResult.barcodeData.getBytes(), isBarcodeUPCE1 ? codeData.length : m_decResult.length);
                                            } else {
                                                m_decResult.byteBarcodeData = m_decDecoder
                                                        .getBarcodeByteData();
                                                if (m_decResult.byteBarcodeData != null) {
                                                    if (isBarcodeUPCE1) {
                                                        checksum = ScanUtil.barCodeUPC_E1_Checksum(m_decResult.byteBarcodeData);
                                                    }
                                                    if (checksum > 0) {
                                                        codeData = new byte[m_decResult.byteBarcodeData.length + 1];
                                                        System.arraycopy(m_decResult.byteBarcodeData, 0, codeData, 0, m_decResult.byteBarcodeData.length);
                                                        codeData[codeData.length - 1] = checksum;
                                                    }
                                                    isBarcodeUPCE1 = isBarcodeUPCE1 && checksum > 0;
                                                    mayBeseparatorDecode(aimID, aimModifier, codeID,
                                                            isBarcodeUPCE1 ? codeData : m_decResult.byteBarcodeData, isBarcodeUPCE1 ? codeData.length : m_decResult.byteBarcodeData.length);
                                                }
                                            }
                                            // urovo add shenpidong end 2019-07-27
                                        }
                                    }
                                } catch (DecoderException e) {
                                    // urovo add by shenpidong begin 2020-04-10
                                    Log.d(TAG, "DecoderException waitForDecodeTwo , delayMode:" + delayPicklistAimMode);
                                    if (isDecoding && delayPicklistAimMode && !isContinueScanMode) {
                                        decodeNotified = true;
                                        isDecoding = false;
                                        ignoreDecodeTime += (int) (System.currentTimeMillis() - decodeStartTime);
                                        Log.d(TAG, "DecoderException waitForDecodeTwo -----ignoreDecodeTime:" + ignoreDecodeTime);
                                        if (ignoreDecodeTime < delayDecodeTime) {
                                            Log.d(TAG, "DecoderException waitForDecodeTwo ----- < 3000");
                                            continue;
                                        } else if(ignoreDecodeTime >= g_nDecodeTimeout){
                                            decodeNotified = false;
                                        }
                                    }
                                    // urovo add by shenpidong end 2020-04-10
                                    isDecoding = false;
                                    isWaitDecodeReturned = true; //add by qiuzhoujun
                                    // urovo add fix continue scan stop once in while. shenpidong begin 2019-03-27
                                    // urovo add shenpidong begin 2019-04-11
                                    if (e.getErrorCode() == ResultID.RESULT_ERR_NOIMAGE) {
                                        Log.d(TAG, "in decode while loop and failed. ignore this Error:" + e.getErrorCode() + " , restart service.");
                                        isErrNoImage = true;
                                    }
                                    // urovo add shenpidong end 2019-04-11
                                    if (e.getErrorCode() != ResultID.RESULT_ERR_NODECODE) {
                                        decodeNotified = false;
                                    }
                                    // urovo add fix continue scan stop once in while. shenpidong end 2019-03-27
                                    preDecodeData = "";
                                    // urovo add by shenpidong begin 2020-03-17
                                    if (currentDecodeDataArr != null) {
                                        for (int i = 0; i < currentDecodeDataArr.length; i++) {
                                            currentDecodeDataArr[i] = "";
                                        }
                                    }
                                    if (preDecodeDataArr != null) {
                                        for (int i = 0; i < preDecodeDataArr.length; i++) {
                                            preDecodeDataArr[i] = "";
                                        }
                                    }
                                    //				    currentDecodeDataArr = null;
                                    //				    preDecodeDataArr = null;
                                    currentDecodeDataArrIndex = 0;
                                    // urovo add by shenpidong end 2020-03-17
                                    // urovo add shenpidong begin 2019-09-12
                                    waitMultipleStartTime = -1;
                                    // urovo add shenpidong end 2019-09-12
                                    Log.d(TAG, "in decode while loop and failed @@@@@@");
                                    HandleDecoderException(e);
                                }
                            }
                        }
                        if (!multipeDecode && waitMultipleInterval > 0) {
                            // TODO: sleep for 50 ms before doing again?
                            sleep(waitMultipleInterval);
                        }
                    } catch (InterruptedException e) {
                        decodeNotified = false;
                        isDecoding = false;
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void handleMessage(Message msg) {
            android.util.Log.d(TAG, "handleMessage " + Thread.currentThread().getId() + "--msg-" + msg.what + " decodeNotified " + decodeNotified + " isDecoding " + isDecoding);
            switch (msg.what) {
                case MESSAGE_DECODE_START:
                    // urovo add shenpidong begin 2019-05-17
                    if (isDisconnectScan6603) {
                        android.util.Log.d(TAG, "handleMessage , disconnect Scan:" + isDisconnectScan6603);
                        return;
                    }
                    // urovo add shenpidong end 2019-05-17
                    if (!isDecoding)
                        decodeFeedback();
                    break;
                case MESSAGE_DECODE_TIMEOUT:
                    decodeNotified = false;
                    isDecoding = false;
                    break;
                case MESSAGE_DECODE_CONFIG:
                    setProperties(null);
                    break;
                case MESSAGE_DECODE_STOP:
                    mWorkHandler.removeMessages(MESSAGE_DECODE_TIMEOUT);
                    decodeNotified = false;
                    isDecoding = false;
                    break;
                case MESSAGE_CAPTURE_IMAGE:
                    enableLastDecImage = true;
                    Bundle bundle = (Bundle) msg.obj;
                    int w = 0;
                    int h = 0;
                    if (bundle != null) {
                        w = bundle.getInt("ImageWidth", 0);
                        h = bundle.getInt("ImageHeight", 0);
                        rotateImage = bundle.getInt("rotate", rotateImage);
                        saveLastDecImage = bundle.getBoolean("saveLastDecImage", false);
                        compressJpegQuality = bundle.getInt("jpegQuality", 50);
                        compressJpegQuality = compressJpegQuality >= 100 ? 90: compressJpegQuality;
                    }
                    captureImage(w, h, rotateImage);
                    break;
            }
        }

        private void decodeFeedback() {
            synchronized (decodeEvent) {
                // urovo add by shenpidong begin 2020-03-17
                waitMultipleStartTime = System.currentTimeMillis();
                // urovo add by shenpidong end 2020-03-17
                decodeNotified = true;
                ignoreDecodeTime = 0;
                decodeEvent.notify();
            }
        }
    }

    private class WorkHandlerThread extends HandlerThread {
        private Looper myLooper;

        public WorkHandlerThread(String name) {
            super(name);
            start();
            myLooper = this.getLooper();
        }

        public void startThread(Object lock) {

            mWorkHandler = new WorkHandler(myLooper, lock);
        }
    }

    /**
     * Function:ChineseHandle
     *
     * @param arraydata
     * @return
     */
    private static String ChineseHandle(int[] arraydata) {
        String str01 = "";
        if (!Isutf8orgb2312(arraydata)) {
            str01 = Utf8toString(arraydata);
        } else {
            str01 = DecodetoString(1, arraydata);
        }
        return str01;
    }

    private static boolean Isutf8orgb2312(int[] value) {
        boolean bool = true;// GB2312
        int len = value.length;
        boolean flag = false;

        for (int i = 0; i < len; i++) {
            if (value[i] >= 128) {
                if ((i + 2) < len) {
                    if ((value[i] >= 0xE0) && (value[i + 1] >= 0x80) && (value[i + 2] >= 0x80)) {// Judge
                        // TF-8
                        i = i + 2;
                    } else {
                        flag = true;
                        bool = true;
                        break;
                    }
                } else {
                    flag = true;
                    break;
                }
            }
        }
        if (!flag) {
            bool = false;
        }
        return bool;
    }

    /**
     * function:DecodetoString
     *
     * @param value[] id:=0,reservd,=1,Chinese ,=2,Japanese
     * @return
     */
    private static String DecodetoString(int id, int[] value) {
        int len = value.length;
        String str = null;
        byte[] bt = new byte[len];
        for (int i = 0; i < len; i++) {
            bt[i] = (byte) value[i];
        }
        try {
            switch (id) {
                case 1:
                    Log.d("GB2312", "GB2312 in Java dectected");
                    str = new String(bt, "gb2312");
                    break;
                case 2:
                    Log.d("SHIFT-JIS", "SHIFT-JIS in Java dectected");
                    str = new String(bt, "SHIFT-JIS");
                    break;
                default:
                    str = new String(bt, "gb2312");
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * function:UTF8 to String
     *
     * @param value
     * @return
     */
    private static String Utf8toString(int[] value) {
        int len = value.length;
        String str = null;
        byte[] bt = new byte[len];
        for (int i = 0; i < len; i++) {
            bt[i] = (byte) value[i];
        }
        try {
            str = new String(bt, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    public static String byte2hex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            String temp = Integer.toHexString(((int) data[i]) & 0xFF);
            for (int t = temp.length(); t < 2; t++) {
                sb.append("0");
            }
            sb.append(temp).append(", ");
        }
        return sb.toString();
    }

    /**
     * Handles the DecoderException by displaying error in log and printing the
     * stack trace
     */
    private void HandleDecoderException(final DecoderException e) {
        switch (e.getErrorCode()) {
            case ResultID.RESULT_ERR_NOTCONNECTED:
                break;

            case ResultID.RESULT_ERR_NOIMAGE:
                // urovo add shenpidong begin 2019-04-11
                Log.d(TAG, "HandleDecoderException++ ResultID.RESULT_ERR_NOIMAGE , no image:" + isErrNoImage);
                if (isErrNoImage && mScanService != null) {
                    close();
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException ee) {
                        ee.printStackTrace();
                    }
                    open();
                    isErrNoImage = false;
                    mScanService.updateProperties();
                    boolean isContinueScan = mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES) == 4;
                    if (isContinueScan && mWorkHandler != null) {
                        mWorkHandler.sendEmptyMessageDelayed(WorkHandler.MESSAGE_DECODE_START, 600);
                    }
                }
                // urovo add shenpidong begin 2019-04-11
/*
        try{
		    m_decDecoder.disconnectDecoderLibrary();
                    m_decDecoder.connectDecoderLibrary();
		} catch(DecoderException ex) {
		    HandleDecoderException(ex);
		}
*/
                break;
            default:
                break;
        }
        Log.d(TAG, "HandleDecoderException++" + e.getErrorCode() + e.getMessage());
        if (e.getErrorCode() != ResultID.RESULT_ERR_NOIMAGE) {
            e.printStackTrace();
        }
    }

    private void captureImage(int width, int height, int rotate) {
        Log.d(TAG, "captureImage++");
        Bitmap bmp = null;
        byte[] imagedata = null;
        if (m_decDecoder != null) {
            try {
                if (lastImageData == null) {
                    Log.d(TAG, "lastImageData null");
                    ImageAttributes attr = new ImageAttributes();
                    lastImageData = m_decDecoder.getLastImage(attr);
                }
                if (lastImageData == null || lastImageData.length == 0) {
                    Log.d(TAG, "getLastImage null");
                } else {
                    Log.d(TAG, "getLastImage lastImageData " + lastImageData.length);
                    g_nImageWidth = m_decDecoder.getImageWidth();
                    g_nImageHeight = m_decDecoder.getImageHeight();

                    int[] array = new int[g_nImageWidth * g_nImageHeight * 2];

                    for (int h = 0; h < g_nImageHeight; h++) {
                        for (int w = 0; w < g_nImageWidth; w++) {
                            array[g_nImageWidth * h + w] = lastImageData[g_nImageWidth * h
                                    + w] * 0x00010101;
                        }
                    }
                    //Bitmap lastbmp = BitmapFactory.decodeByteArray(lastImageData, 0, lastImageData.length);
                    lastImageData = null;
                    Bitmap lastbmp = Bitmap.createBitmap(g_nImageWidth, g_nImageHeight,
                            Bitmap.Config.RGB_565);
                    // Set the pixels
                    if (lastbmp != null) {
                        lastbmp.setPixels(array, 0, g_nImageWidth, 0, 0, g_nImageWidth,
                                g_nImageHeight);
                        Log.d(TAG, "saveLastDecImage " + saveLastDecImage);
                        if (saveLastDecImage) {
                            long fileSuffix = System.currentTimeMillis();
                            ImageUtils.saveImageAsPNG(String.format("sdcard/decode_%d.png", fileSuffix), lastbmp);
                        }
                        if (rotate > 0 && rotate <= 270) {
                            Matrix matrix = new Matrix();
                            matrix.setRotate(rotate);
                            lastbmp = Bitmap.createBitmap(lastbmp, 0, 0, lastbmp.getWidth(),
                                    lastbmp.getHeight(), matrix, false);
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        lastbmp.compress(Bitmap.CompressFormat.JPEG, compressJpegQuality, baos);
                        imagedata = baos.toByteArray();
                        lastbmp.recycle();
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "scanner_capture_image_result null");
            }
            Intent intent = new Intent("scanner_capture_image_result");
            Bundle b = new Bundle();
            if (imagedata == null) {
                try {
                    m_decDecoder.startScanning();
                    // Create bmp
                    if (g_nImageWidth == 0 || g_nImageHeight == 0) {
                        g_nImageWidth = m_decDecoder.getImageWidth();
                        g_nImageHeight = m_decDecoder.getImageHeight();
                    }
                    bmp = Bitmap.createBitmap(g_nImageWidth, g_nImageHeight, Bitmap.Config.RGB_565);
                    m_decDecoder.getSingleFrame(bmp);
                    //m_decDecoder.getPreviewFrame(bmp);// 1/4th size image capture
                    //m_decDecoder.stopScanning();
                    if (bmp != null) {
                        if (width > g_nImageWidth || width == 0) {
                            width = g_nImageWidth / 2;
                        }
                        if (height > g_nImageHeight || height == 0) {
                            height = g_nImageHeight / 2;
                        }
                        long start = System.currentTimeMillis();
                        Bitmap scaledBitmap = ScalingUtilities.createScaledBitmap(bmp,
                                width, height, ScalingUtilities.ScalingLogic.FIT);
                        long end = System.currentTimeMillis();
                        if (scaledBitmap != null) {
                            Log.d(TAG, "captureImage g_nImageWidth= " + g_nImageWidth + " g_nImageHeight= " + g_nImageHeight + " bmp W=" + scaledBitmap.getWidth() + " H= " + scaledBitmap.getHeight() + " time: " + (end - start));
                            b.putByteArray("bitmapBytes", bitmap2Bytes(scaledBitmap));
                            scaledBitmap.recycle();
                            scaledBitmap = null;
                            bmp.recycle();
                            bmp = null;
                        } else {
                            b.putByteArray("bitmapBytes", bitmap2Bytes(bmp));
                            bmp.recycle();
                            bmp = null;
                        }
                    }
                    intent.putExtras(b);
                    mScanService.getContext().sendBroadcastAsUser(intent, android.os.UserHandle.ALL);
                } catch (DecoderException e) {
                    HandleDecoderException(e);
                } catch (Exception e) {
                    Log.d(TAG, "scanner_capture_image_result null");
                }
            } else {
                try {
                    b.putByteArray("bitmapBytes", imagedata);
                    intent.putExtras(b);
                    mScanService.getContext().sendBroadcastAsUser(intent, android.os.UserHandle.ALL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private byte[] bitmap2Bytes(Bitmap bm) {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    // Declare/Initialize an array of ints
    int g_nExposureSettings[] =
            {
                    ExposureSettings.DEC_ES_EXPOSURE_METHOD, 0,
                    ExposureSettings.DEC_ES_TARGET_VALUE, 0,
                    ExposureSettings.DEC_ES_TARGET_PERCENTILE, 0,
                    ExposureSettings.DEC_ES_TARGET_ACCEPT_GAP, 0,
                    ExposureSettings.DEC_ES_MAX_EXP, 0,
                    ExposureSettings.DEC_ES_MAX_GAIN, 0,
                    ExposureSettings.DEC_ES_FRAME_RATE, 0,
                    ExposureSettings.DEC_ES_CONFORM_IMAGE, 0,
                    ExposureSettings.DEC_ES_CONFORM_TRIES, 0,
                    ExposureSettings.DEC_ES_SPECULAR_EXCLUSION, 0,
                    ExposureSettings.DEC_ES_SPECULAR_SAT, 0,
                    ExposureSettings.DEC_ES_SPECULAR_LIMIT, 0,
                    ExposureSettings.DEC_ES_FIXED_EXP, 0,
                    ExposureSettings.DEC_ES_FIXED_GAIN, 0,
                    ExposureSettings.DEC_ES_FIXED_FRAME_RATE, 0,
            };

    static class N6603ParamIndex {
        public static final int IMAGE_EXPOSURE_MODE = 7848;
        public static final int IMAGE_FIXED_EXPOSURE = 7849;
        public static final int IMAGE_PICKLIST_MODE = SPECIAL_VALUE;
        public static final int IMAGE_ONE_D_INVERSE = 0;//SPECIAL_VALUE;
        public final static int LASER_ON_TIME = SPECIAL_VALUE;//0x01-0x63 df 0x63 * 100 ms
        public final static int TIMEOUT_BETWEEN_SAME_SYMBOL = SPECIAL_VALUE;//0x01-0x63 df 6
        public final static int LINEAR_CODE_TYPE_SECURITY_LEVEL = 1001;//1 2 3 4
        public static final int FUZZY_1D_PROCESSING = SPECIAL_VALUE;
        // urovo add by shenpidong begin 2020-03-17
        public static final int MULTI_DECODE_MODE = 1001;//SPECIAL_VALUE;
        public static final int BAR_CODES_TO_READ = 1001;//SPECIAL_VALUE;
        // urovo add by shenpidong end 2020-03-17
        // urovo add by shenpidong begin 2020-03-23
        public static final int FULL_READ_MODE = 1001;//SPECIAL_VALUE;
        // urovo add by shenpidong end 2020-03-23
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
        public static final int CODABAR_ENABLE_CHECK = 0xf2 << 8 | 0x68; //TODO 2d
        public static final int CODABAR_SEND_CHECK = 0xf2 << 8 | 0x69; //TODO 2d
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
        public static final int UPCA_SEND_SYS = 34; // 0 2 1//TODO
        public static final int UPCA_TO_EAN13 = 34; //RESERVED_VALUE;//TODO 0 2 1
        public static final int UPCE_ENABLE = 2;//0 1 1
        public static final int UPCE_SEND_CHECK = 41;//0 1 1df 0
        public static final int UPCE_SEND_SYS = 35; // 0 2 1//TODOdf 0
        public static final int UPCE_TO_UPCA = 37;//0 1 0
        public final static int UPCE1_ENABLE = 0x0C;
        public final static int UPCE1_SEND_CHECK = 0x2A;
        public final static int UPCE1_SEND_SYS = 0x24;
        public final static int UPCE1_TO_UPCA = 0x26;
        public static final int EAN13_ENABLE = 3;//TODO 0 1 1
        public static final int EAN13_SEND_CHECK = 0; //TODO
        public static final int EAN13_SEND_SYS = SPECIAL_VALUE; //TODO
        public static final int EAN13_TO_ISBN = 83;//TODO 0 1 0 prefix 978
        public static final int EAN13_BOOKLANDEAN = 83;
        public static final int EAN13_BOOKLAND_FORMAT = 576;
        public static final int EAN13_TO_ISSN = 617;//TODO0 1 0 prefix 977
        public static final int EAN8_ENABLE = 4;//TODO 0 1 1
        public static final int EAN8_SEND_CHECK = 0; //TODO
        public static final int EAN8_TO_EAN13 = 39;//0 1 0
        public static final int EAN_EXT_ENABLE_2_5_DIGIT = 0x10;//TODO to see upc-a upc-e ena-13 ena-8  ??????????????
        public final static int UPC_EAN_SECURITY_LEVEL = 0x4d;//        0       3       1
        public final static int UCC_COUPON_EXT_CODE = 0x55;//        0       1       0
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
        public static final int GS1_EXP_LENGTH1 = 1001;  //TODO
        public static final int GS1_EXP_LENGTH2 = 1001;  //TODO
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
        public static final int HANXIN_ENABLE = 1167;
        public static final int HANXIN_INVERSE = 1168;
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
        public static final int DEC_2D_LIGHTS_MODE = 1001;
        public static final int DEC_2D_CENTERING_ENABLE = 1001;
        public static final int DEC_2D_CENTERING_MODE = 1001;
        public static final int DEC_2D_WINDOW_UPPER_LX = 1001;
        public static final int DEC_2D_WINDOW_UPPER_LY = 1001;
        public static final int DEC_2D_WINDOW_LOWER_RX = 1001;
        public static final int DEC_2D_WINDOW_LOWER_RY = 1001;
        public static final int DEC_2D_DEBUG_WINDOW_ENABLE = 1001;

        public static final int DEC_ES_EXPOSURE_METHOD = 1001;   // Auto Exposure Method
        public static final int DEC_ES_TARGET_VALUE = 1001;      // Target White Value
        public static final int DEC_ES_TARGET_PERCENTILE = 1001; // Target Percentile
        public static final int DEC_ES_TARGET_ACCEPT_GAP = 1001; // Target Acceptance Gap
        public static final int DEC_ES_MAX_EXP = 1001;           // Maximum Exposure
        public static final int DEC_ES_MAX_GAIN = 1001;          // Maximum Gain
        public static final int DEC_ES_FRAME_RATE = 1001;            // Frame Rate
        public static final int DEC_ES_CONFORM_IMAGE = 1001;     // Image Must Conform
        public static final int DEC_ES_CONFORM_TRIES = 1001;     // Tries for Conform
        public static final int DEC_ES_SPECULAR_EXCLUSION = 1001;    // Exclude Specular Regions
        public static final int DEC_ES_SPECULAR_SAT = 1001;      // Specular Saturation
        public static final int DEC_ES_SPECULAR_LIMIT = 1001;        // Specular Limit
        //public static final int  DEC_ES_FIXED_EXP =  0x0A21;         // Fixed Exposure
        public static final int DEC_ES_FIXED_GAIN = 1001;            // Fixed Gain
        public static final int DEC_ES_FIXED_FRAME_RATE = 1001;  // Fixed Frame Rate
        public static final int DEC_MaxMultiRead_COUNT = 1001;  // Fixed Frame Rate
        public static final int DEC_Multiple_Decode_TIMEOUT = 1001;  // Fixed Frame Rate
        public static final int DEC_Multiple_Decode_INTERVAL = 1001;  // Fixed Frame Rate, add by tao.he
        public static final int DEC_Multiple_Decode_MODE = 1001;  // Fixed Frame Rate
        public static final int DEC_OCR_MODE = 1001;  // Fixed Frame Rate
        public static final int DEC_OCR_TEMPLATE = 1001;  // Fixed Frame Rate
        public static final int TRANSMIT_CODE_ID = 1001;
        public static final int DOTCODE_ENABLE = 1001;
        public static final int COMPOSITE_UPC_MODE = SPECIAL_VALUE;
        public static final int KOREA_POST_ENABLE = SPECIAL_VALUE;
        public static final int Canadian_POSTAL_ENABLE = SPECIAL_VALUE;
        public static final int POSTAL_GROUP_TYPE_ENABLE = SPECIAL_VALUE;
        public static final int CODE128_REDUCED_QUIET_ZONE = SPECIAL_VALUE;
        public static final int CODE128_SECURITY_LEVEL = SPECIAL_VALUE;
        public static final int DEC_PICKLIST_AIM_MODE = 1001;
        public static final int DEC_PICKLIST_AIM_DELAY = 1001;
    }

    private final int[] VALUE_PARAM_INDEX = {
            N6603ParamIndex.IMAGE_EXPOSURE_MODE,
            N6603ParamIndex.IMAGE_FIXED_EXPOSURE,
            N6603ParamIndex.IMAGE_PICKLIST_MODE,
            N6603ParamIndex.IMAGE_ONE_D_INVERSE,
            N6603ParamIndex.LASER_ON_TIME,
            N6603ParamIndex.TIMEOUT_BETWEEN_SAME_SYMBOL,
            N6603ParamIndex.LINEAR_CODE_TYPE_SECURITY_LEVEL,
            N6603ParamIndex.FUZZY_1D_PROCESSING,
            N6603ParamIndex.MULTI_DECODE_MODE,
            N6603ParamIndex.BAR_CODES_TO_READ,
            N6603ParamIndex.FULL_READ_MODE,
            N6603ParamIndex.CODE39_ENABLE,
            N6603ParamIndex.CODE39_ENABLE_CHECK,
            N6603ParamIndex.CODE39_SEND_CHECK,
            N6603ParamIndex.CODE39_FULL_ASCII,
            N6603ParamIndex.CODE39_LENGTH1,
            N6603ParamIndex.CODE39_LENGTH2,
            N6603ParamIndex.TRIOPTIC_ENABLE,
            N6603ParamIndex.CODE32_ENABLE,
            N6603ParamIndex.CODE32_SEND_START,
            N6603ParamIndex.C25_ENABLE,
            N6603ParamIndex.D25_ENABLE,
            N6603ParamIndex.D25_LENGTH1,
            N6603ParamIndex.D25_LENGTH2,
            N6603ParamIndex.M25_ENABLE,
            N6603ParamIndex.CODE11_ENABLE,
            N6603ParamIndex.CODE11_ENABLE_CHECK,
            N6603ParamIndex.CODE11_SEND_CHECK,
            N6603ParamIndex.CODE11_LENGTH1,
            N6603ParamIndex.CODE11_LENGTH2,
            N6603ParamIndex.I25_ENABLE,
            N6603ParamIndex.I25_ENABLE_CHECK,
            N6603ParamIndex.I25_SEND_CHECK,
            N6603ParamIndex.I25_LENGTH1,
            N6603ParamIndex.I25_LENGTH2,
            N6603ParamIndex.I25_TO_EAN13,
            N6603ParamIndex.CODABAR_ENABLE,
            N6603ParamIndex.CODABAR_NOTIS,
            N6603ParamIndex.CODABAR_CLSI,
            N6603ParamIndex.CODABAR_LENGTH1,
            N6603ParamIndex.CODABAR_LENGTH2,
            N6603ParamIndex.CODE93_ENABLE,
            N6603ParamIndex.CODE93_LENGTH1,
            N6603ParamIndex.CODE93_LENGTH2,
            N6603ParamIndex.CODE128_ENABLE,
            N6603ParamIndex.CODE128_LENGTH1,
            N6603ParamIndex.CODE128_LENGTH2,
            N6603ParamIndex.CODE_ISBT_128,
            N6603ParamIndex.CODE128_GS1_ENABLE,
            N6603ParamIndex.UPCA_ENABLE,
            N6603ParamIndex.UPCA_SEND_CHECK,
            N6603ParamIndex.UPCA_SEND_SYS,
            N6603ParamIndex.UPCA_TO_EAN13,
            N6603ParamIndex.UPCE_ENABLE,
            N6603ParamIndex.UPCE_SEND_CHECK,
            N6603ParamIndex.UPCE_SEND_SYS,
            N6603ParamIndex.UPCE_TO_UPCA,
            N6603ParamIndex.UPCE1_ENABLE,
            N6603ParamIndex.UPCE1_SEND_CHECK,
            N6603ParamIndex.UPCE1_SEND_SYS,
            N6603ParamIndex.UPCE1_TO_UPCA,
            N6603ParamIndex.EAN13_ENABLE,
            N6603ParamIndex.EAN13_BOOKLANDEAN,
            N6603ParamIndex.EAN13_BOOKLAND_FORMAT,
            N6603ParamIndex.EAN8_ENABLE,
            N6603ParamIndex.EAN8_TO_EAN13,
            N6603ParamIndex.EAN_EXT_ENABLE_2_5_DIGIT,
            N6603ParamIndex.UPC_EAN_SECURITY_LEVEL,
            N6603ParamIndex.UCC_COUPON_EXT_CODE,
            N6603ParamIndex.MSI_ENABLE,
            N6603ParamIndex.MSI_REQUIRE_2_CHECK,
            N6603ParamIndex.MSI_SEND_CHECK,
            N6603ParamIndex.MSI_CHECK_2_MOD_11,
            N6603ParamIndex.MSI_LENGTH1,
            N6603ParamIndex.MSI_LENGTH2,
            N6603ParamIndex.GS1_14_ENABLE,
            N6603ParamIndex.GS1_14_TO_UPC_EAN,
            N6603ParamIndex.GS1_LIMIT_ENABLE,
            N6603ParamIndex.GS1_EXP_ENABLE,
            N6603ParamIndex.GS1_EXP_LENGTH1,
            N6603ParamIndex.GS1_EXP_LENGTH2,
            N6603ParamIndex.US_POSTNET_ENABLE,
            N6603ParamIndex.US_PLANET_ENABLE,
            N6603ParamIndex.US_POSTAL_SEND_CHECK,
            N6603ParamIndex.USPS_4STATE_ENABLE,
            N6603ParamIndex.UPU_FICS_ENABLE,
            N6603ParamIndex.ROYAL_MAIL_ENABLE,
            N6603ParamIndex.ROYAL_MAIL_SEND_CHECK,
            N6603ParamIndex.AUSTRALIAN_POST_ENABLE,
            N6603ParamIndex.KIX_CODE_ENABLE,
            N6603ParamIndex.JAPANESE_POST_ENABLE,
            N6603ParamIndex.PDF417_ENABLE,
            N6603ParamIndex.MICROPDF417_ENABLE,
            N6603ParamIndex.COMPOSITE_CC_AB_ENABLE,
            N6603ParamIndex.COMPOSITE_CC_C_ENABLE,
            N6603ParamIndex.COMPOSITE_TLC39_ENABLE,
            N6603ParamIndex.HANXIN_ENABLE,
            N6603ParamIndex.HANXIN_INVERSE,
            N6603ParamIndex.DATAMATRIX_ENABLE,
            N6603ParamIndex.DATAMATRIX_LENGTH1,
            N6603ParamIndex.DATAMATRIX_LENGTH2,
            N6603ParamIndex.DATAMATRIX_INVERSE,
            N6603ParamIndex.MAXICODE_ENABLE,
            N6603ParamIndex.QRCODE_ENABLE,
            N6603ParamIndex.QRCODE_INVERSE,
            N6603ParamIndex.MICROQRCODE_ENABLE,
            N6603ParamIndex.AZTEC_ENABLE,
            N6603ParamIndex.AZTEC_INVERSE,
            N6603ParamIndex.DEC_2D_LIGHTS_MODE,
            N6603ParamIndex.DEC_2D_CENTERING_ENABLE,
            N6603ParamIndex.DEC_2D_CENTERING_MODE,
            N6603ParamIndex.DEC_2D_WINDOW_UPPER_LX,
            N6603ParamIndex.DEC_2D_WINDOW_UPPER_LY,
            N6603ParamIndex.DEC_2D_WINDOW_LOWER_RX,
            N6603ParamIndex.DEC_2D_WINDOW_LOWER_RY,
            N6603ParamIndex.DEC_2D_DEBUG_WINDOW_ENABLE,
            N6603ParamIndex.DEC_ES_EXPOSURE_METHOD,
            N6603ParamIndex.DEC_ES_TARGET_VALUE,
            N6603ParamIndex.DEC_ES_TARGET_PERCENTILE,
            N6603ParamIndex.DEC_ES_TARGET_ACCEPT_GAP,
            N6603ParamIndex.DEC_ES_MAX_EXP,
            N6603ParamIndex.DEC_ES_MAX_GAIN,
            N6603ParamIndex.DEC_ES_FRAME_RATE,
            N6603ParamIndex.DEC_ES_CONFORM_IMAGE,
            N6603ParamIndex.DEC_ES_CONFORM_TRIES,
            N6603ParamIndex.DEC_ES_SPECULAR_EXCLUSION,
            N6603ParamIndex.DEC_ES_SPECULAR_SAT,
            N6603ParamIndex.DEC_ES_SPECULAR_LIMIT,
            N6603ParamIndex.DEC_ES_FIXED_GAIN,
            N6603ParamIndex.DEC_ES_FIXED_FRAME_RATE,
            DEC_ILLUM_POWER_LEVEL,
            N6603ParamIndex.DEC_PICKLIST_AIM_MODE,
            N6603ParamIndex.DEC_PICKLIST_AIM_DELAY,
            N6603ParamIndex.DEC_MaxMultiRead_COUNT,
            N6603ParamIndex.DEC_Multiple_Decode_TIMEOUT,
            N6603ParamIndex.DEC_Multiple_Decode_INTERVAL, /* urovo tao.he add, 20190314 */
            N6603ParamIndex.DEC_Multiple_Decode_MODE,
            N6603ParamIndex.DEC_OCR_MODE,
            N6603ParamIndex.DEC_OCR_TEMPLATE,
            N6603ParamIndex.TRANSMIT_CODE_ID,
            N6603ParamIndex.DOTCODE_ENABLE,
            LINEAR_1D_QUIET_ZONE_LEVEL,
            CODE39_Quiet_Zone,
            CODE39_START_STOP,
            CODE39_SECURITY_LEVEL,
            M25_SEND_CHECK,
            N6603ParamIndex.M25_LENGTH1,
            N6603ParamIndex.M25_LENGTH2,
            I25_QUIET_ZONE,
            I25_SECURITY_LEVEL,
            CODABAR_ENABLE_CHECK,
            CODABAR_SEND_CHECK,
            CODABAR_SEND_START,
            CODABAR_CONCATENATE,
            N6603ParamIndex.CODE128_REDUCED_QUIET_ZONE,
            CODE128_CHECK_ISBT_TABLE,
            CODE_ISBT_Concatenation_MODE,
            N6603ParamIndex.CODE128_SECURITY_LEVEL,
            CODE128_IGNORE_FNC4,
            UCC_REDUCED_QUIET_ZONE,
            UCC_COUPON_EXT_REPORT_MODE,
            UCC_EAN_ZERO_EXTEND,
            UCC_EAN_SUPPLEMENTAL_MODE,
            GS1_LIMIT_Security_Level,
            COMPOSITE_UPC_MODE,
            N6603ParamIndex.POSTAL_GROUP_TYPE_ENABLE,
            N6603ParamIndex.KOREA_POST_ENABLE,
            N6603ParamIndex.Canadian_POSTAL_ENABLE,
    };
}
