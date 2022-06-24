package com.android.server.scanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.os.UserHandle;
import com.android.server.ScanServiceWrapper;
import android.device.scanner.configuration.Symbology;
/**
 * Created by gouwei on 17-9-23.
 */

public class IA100Scanner extends Scanner{
    private static String TAG = "IA100Scanner";
    public static final String ACTION_A100S_STOP_DECODE = "ACTION_A100S_STOP_DECODE";
    public static final String ACTION_A100S_START_DECODE = "ACTION_A100S_START_DECODE";
    public static final String ACTION_A100S_INIT = "ACTION_IMAGE_A100S_INIT";
    public static final String ACTION_A100S_RELEASE = "ACTION_IMAGE_A100S_RELEASE";

    private static final int MSG_START_DECODE = 101;
    private static final int MSG_STOP_DECODE = 102;
    private static final int MSG_OPEN_DECODE = 103;
    private static final int MSG_CLOSE_DECODE = 104;

    private static final String ACTION_SCAN_DECODE_RESULT = "ACTION_SCAN_DECODE_RESULT";
    private static final String DECODE_DATA = "DECODE_DATA";
    private static final String DECODE_RAWDATA = "barcode";
    public static final String BARCODE_NAME_TAG = "format";

    private MyHandler mHandler;
    private MyBroadcastReceiver myBroadcastReceiver;

    public IA100Scanner(ScanServiceWrapper scanService){
        mScannerType = ScannerFactory.TYPE_IA100;
        mScanService = scanService;
        mContext = scanService.mContext;
        mHandler = new MyHandler(mContext.getMainLooper());
        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SCAN_DECODE_RESULT);
        mContext.registerReceiver(myBroadcastReceiver,filter);
        for (int i = 0; i < INTERNAL_PROPERTY_INDEX.length; i++) {
            mPropIndexHashMap.put(INTERNAL_PROPERTY_INDEX[i], VALUE_PARAM_INDEX[i]);
        }
    }

    class MyBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //String result = intent.getStringExtra(DECODE_DATA);
            byte[] result = intent.getByteArrayExtra(DECODE_RAWDATA);
            if(result != null) {
                int length = intent.getIntExtra("length", result.length);
                String format = intent.getStringExtra("format");
                int synId = getSymbologyId(format);
                sendBroadcast(result, synId, length);
            }
        }
    }
    private int getSymbologyId(String symName) {
        if(symName == null || symName.equals("")) return Symbology.NONE.toInt();
        if(symName.equals("AZTEC")) {
            return Symbology.AZTEC.toInt();
        } else  if(symName.equals("UPC-A")) {
            return Symbology.UPCA.toInt();
        } else  if(symName.equals("C39")) {
            return Symbology.CODE93.toInt();
        } else  if(symName.equals("C128")) {
            return Symbology.CODE128.toInt();
        } else  if(symName.equals("I25")) {
            return Symbology.INTERLEAVED25.toInt();
        } else  if(symName.equals("C93")) {
            return Symbology.CODE93.toInt();
        } else  if(symName.equals("GS1 DATABAR")) {
            return Symbology.GS1_14.toInt();
        } else  if(symName.equals("MSI")) {
            return Symbology.MSI.toInt();
        } else  if(symName.equals("CODEBLOCK F")) {
            return Symbology.CODE32.toInt();
        } else  if(symName.equals("PDF417")) {
            return Symbology.PDF417.toInt();
        } else  if(symName.equals("MICROPDF")) {
            return Symbology.MICROPDF417.toInt();
        } else  if(symName.equals("MAXICODE")) {
            return Symbology.MAXICODE.toInt();
        } else  if(symName.equals("QR CODE")) {
            return Symbology.QRCODE.toInt();
        } else  if(symName.equals("DATA MATRIX")) {
            return Symbology.DATAMATRIX.toInt();
        } else  if(symName.equals("HAXIN")) {
            return Symbology.HANXIN.toInt();
        } else  if(symName.equals("MATRIX 25")) {
            return Symbology.MATRIX25.toInt();
        } else  if(symName.equals("TRIOPTIC")) {
            return Symbology.TRIOPTIC.toInt();
        } else  if(symName.equals("STRAIGHT 25")) {
            return Symbology.DISCRETE25.toInt();
        } else  if(symName.equals("TELEPEN")) {
            return Symbology.NONE.toInt();
        } else  if(symName.equals("C11")) {
            return Symbology.CODE11.toInt();
        } else  if(symName.equals("UPC-E")) {
            return Symbology.UPCE.toInt();
        } else  if(symName.equals("EAN-8")) {
            return Symbology.EAN8.toInt();
        } else  if(symName.equals("EAN-13")) {
            return Symbology.EAN13.toInt();
        } else {
            return Symbology.NONE.toInt();
        }
    }

    class MyHandler extends Handler{
        public MyHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_START_DECODE:
                    Log.d(TAG,"MSG_START_DECODE");
                    sendBroadcast(ACTION_A100S_START_DECODE);
                    break;
                case MSG_STOP_DECODE:
                    Log.d(TAG,"MSG_STOP_DECODE");
                    sendBroadcast(ACTION_A100S_STOP_DECODE);
                    break;
                case MSG_OPEN_DECODE:
                    Log.d(TAG,"MSG_OPEN_DECODE");
                    //sendBroadcast(ACTION_A100S_INIT);
                    break;
                case MSG_CLOSE_DECODE:
                    Log.d(TAG,"MSG_CLOSE_DECODE");
                    sendBroadcast(ACTION_A100S_RELEASE);
                    break;
            }
        }
    }

    private void sendBroadcast(String action){

        //mContext.sendBroadcast(new Intent(action));
		mContext.sendBroadcastAsUser(new Intent(action), UserHandle.ALL);

    }
    @Override
    public void setDefaults() {

    }

    @Override
    public boolean open() {
        Log.d(TAG,"open");
        mHandler.removeMessages(MSG_OPEN_DECODE);
        mHandler.sendEmptyMessage(MSG_OPEN_DECODE);
        return true;
    }

    @Override
    public void close() {
        Log.d(TAG,"close");
        mHandler.removeMessages(MSG_CLOSE_DECODE);
        mHandler.sendEmptyMessage(MSG_CLOSE_DECODE);
    }

    @Override
    public void startDecode(int timeout) {
        Log.d(TAG,"startDecode");
        mHandler.removeMessages(MSG_START_DECODE);
        mHandler.sendEmptyMessage(MSG_START_DECODE);
    }

    @Override
    public void stopDecode() {
        Log.d(TAG,"stopDecode");
        mHandler.removeMessages(MSG_STOP_DECODE);
        mHandler.sendEmptyMessage(MSG_STOP_DECODE);
    }

    @Override
    public void openPhoneMode() {

    }

    @Override
    public void closePhoneMode() {

    }

    @Override
    public int setProperties(SparseArray<Integer> property) {
        sendBroadcast("ACTION_SCNNER_SYNC_CONFIG");
        return 0;
    }

    @Override
    protected void release() {
        Log.d(TAG,"close");
        mHandler.removeMessages(MSG_CLOSE_DECODE);
        mHandler.sendEmptyMessage(MSG_CLOSE_DECODE);

    }

    @Override
    public boolean lockHwTriggler(boolean lock) {
        return false;
    }

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
            DEC_Multiple_Decode_INTERVAL,
            DEC_Multiple_Decode_MODE,
            DEC_OCR_MODE,
            DEC_OCR_TEMPLATE,
            TRANSMIT_CODE_ID,
            DOTCODE_ENABLE
    };
}
