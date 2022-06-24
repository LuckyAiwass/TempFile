package com.ubx.usdk.psam;

import android.util.Log;
import android.os.Message;
import android.content.Context;
import android.os.IBinder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.RemoteException;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.ComponentName;

import com.ubx.usdk.LogUtil;
import com.ubx.usdk.USDKBaseManager;
import com.ubx.usdk.USDKManager;
import com.ubx.usdk.USDKManager.STATUS;
import com.ubx.usdk.USDKManager.FEATURE_TYPE;


import com.qualcomm.qti.sam.interfaces.CardState;
import com.qualcomm.qti.sam.interfaces.CardATR;
import com.qualcomm.qti.sam.interfaces.SAMErrors;
import com.qualcomm.qti.sam.interfaces.ISAM;
import com.qualcomm.qti.sam.interfaces.SlotMap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


/**
 * This class provides SAM Interface
 * for the clients.
 *
 */

public class PsamManager extends USDKBaseManager implements USDKManager.StatusListener {
    public final String LOG_TAG = "PsamManager";

    private Context context;
    private boolean reset_timeout_flag = true;
    private boolean apdu_resp_timeout_flag = true;
    private boolean power_down_timeout_flag = true;


    public static final int MSG_CONNECT_SERVICE = 1;
    public static final int MSG_SERVICE_CONNECT_STATUS = 2;
    public static final int MSG_REGISTER_UNSOL_CALLBACK = 3;
    public static final int MSG_RESET_CARD = 4;
    public static final int MSG_POWER_DOWN = 5;
    public static final int MSG_POWER_DOWN_COMPLETE = 6;
    public static final int MSG_POWER_UP = 7;
    public static final int MSG_POWER_UP_COMPLETE = 8;
    public static final int MSG_SEND_APDU = 9;
    public static final int MSG_SEND_APDU_COMPLETE = 10;

    public static int slot_id = -1;
    public static int class_type;
    public static int instruction;
    public static int param1;
    public static int param2;
    public static int param3;
    public String dataCmd = "";
    public static ApduResponseData mApduData;

    private static CardState mCardState;
    private static CardATR mCardATR;
    private static PsamHandler mSender;
    private static SAMUnsolicitedCallback mUnsolCallback;
    public static SAMResponseCallback mResponseCallback;

    HandlerThread mSenderThread;

    private ISAM mIPsamManager;
    public PsamManager(Context context) {
        super(context, FEATURE_TYPE.PSAM);
        Intent intent = new Intent();
        intent.setPackage("com.qualcomm.qti.sam.service");
        intent.setClassName("com.qualcomm.qti.sam.service", "com.qualcomm.qti.sam.service.SAMService");
        setIntent(intent);
        addStatusListener(this);
    }

    @Override
    public void onStatus(FEATURE_TYPE featureType, STATUS status) {
        if(status == STATUS.SUCCESS) {
            mIPsamManager = ISAM.Stub.asInterface(getIBinder());
            LogUtil.d("onStatus mIPsamManager:"+mIPsamManager);
	    	    mCardATR = new CardATR();
            mCardState = new CardState();
            mSenderThread = new HandlerThread("PsamHandlerThread");
            mSenderThread.start();
            Looper looper = mSenderThread.getLooper();
            mSender = new PsamHandler(looper);
            mUnsolCallback = new SAMUnsolicitedCallback();
            mResponseCallback = new SAMResponseCallback(mSender);
            try {
              int ret = mIPsamManager.registerUnsolicitedResponseCallback(mUnsolCallback);
              LogUtil.d("registerUnsolicitedResponseCallback result = " + ret);
            } catch(RemoteException e) {
              LogUtil.e("registerUnsolicitedResponseCallback", e);
            }
        } else {
            mIPsamManager = null;
        }
    }
    @Override
    public void release() {
        if(mIPsamManager != null) {
          try {
            mIPsamManager.deregisterUnsolicitedResponseCallback(mUnsolCallback);
          } catch(RemoteException e) {
            LogUtil.e("deregisterUnsolicitedResponseCallback", e);
          }
        }
        mIPsamManager = null;
        super.release();
    }
    
    private void send(int msg_id, MessageRequest request) {
        Message msg = mSender.obtainMessage();
        msg.what = msg_id;
        if (request != null) {
          msg.obj = request;
        }
        msg.sendToTarget();
    }

    public static final class ApduResponseData {
        public int sw1;
        public int sw2;
        public String apdu_data_resp;

        public ApduResponseData() {
          //do noting
        }
    }

    /**
     * A request object for use with {@link MessageHandler}. Requesters should
     * wait() on the request after sending. The main thread will notify the
     * request when it is complete.
     */
    private static final class MessageRequest {
        /** The argument to use for the request */
        public Object argument;
        /** The result of the request that is run on the main thread */
        public int msg_id;
        public int status;

        public MessageRequest(int msg_id) {
            this.msg_id = msg_id;
            this.status = -1;//need initiate the status
        }
    }

    class PsamHandler extends Handler implements Runnable {
    private boolean needResetCard;
    MessageRequest connect_request;
    MessageRequest reset_request;
    MessageRequest power_down_request;
    MessageRequest apdu_request;

    public PsamHandler(Looper looper) {
      super(looper);
    }

    //***** Runnable implementation
    @Override
    public void
    run() {
    //setup if needed
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case MSG_RESET_CARD: {
                reset_request = (MessageRequest)msg.obj;
                needResetCard = true;
                Message msg_power_down = mSender.obtainMessage();
                msg_power_down.what = MSG_POWER_DOWN;
                msg_power_down.sendToTarget();
            }
            break;

            case MSG_POWER_DOWN: {
                power_down_request = (MessageRequest)msg.obj;
                int power_down_result = cardPowerDown(slot_id, mResponseCallback);
                Log.d(LOG_TAG, "cardPowerDown intermediate status = " + power_down_result);
            }
            break;

            case MSG_POWER_DOWN_COMPLETE: {
                int power_down_complete_result = msg.arg1;
                Log.d(LOG_TAG, "cardPowerDown actionstatus = " + power_down_complete_result);
                if (needResetCard) {
                  send(MSG_POWER_UP, null);
                } else {
                  if(power_down_request != null) {
                      synchronized (power_down_request) {
                        power_down_request.argument = new Object();
                        power_down_request.status = power_down_complete_result;
                        power_down_timeout_flag = false;
                        power_down_request.notifyAll();
                      }
                  }
                }
            }
            break;

            case MSG_POWER_UP: {
                int power_up_result = cardPowerUp(slot_id, mResponseCallback);
                LogUtil.d("cardPowerUP intermediate status = " + power_up_result);
            }
            break;

            case MSG_POWER_UP_COMPLETE: {
                int power_up_complete_result = msg.arg1;
                Log.d(LOG_TAG, "cardPowerUp actionstatus = " + power_up_complete_result);
                if (needResetCard) {
                  synchronized (reset_request) {
                      reset_request.argument = new Object();
                      reset_request.status = power_up_complete_result;
                      reset_timeout_flag = false;
                      reset_request.notifyAll();
                  }
                }
                needResetCard = false;
            }
            break;

            case MSG_SEND_APDU: {
                apdu_request = (MessageRequest)msg.obj;
                int send_apdu_result = streamApdu(slot_id, class_type, instruction, param1, param2, param3, dataCmd, mResponseCallback);
                LogUtil.d("send apdu intermediate status = " + send_apdu_result);
            }
            break;

            case MSG_SEND_APDU_COMPLETE: {
                int apdu_data_send_complete_result = msg.arg1;
                Log.d(LOG_TAG, "send apdu actionstatus = " + apdu_data_send_complete_result);
                mApduData = (ApduResponseData)msg.obj;
                synchronized (apdu_request) {
                    apdu_request.argument = new Object();
                    apdu_request.status = apdu_data_send_complete_result;
                    apdu_resp_timeout_flag = false;
                    apdu_request.notifyAll();
                }
            }
            break;

            default: {
                Log.d(LOG_TAG, "Unknow message id msg.what=" + msg.what);
            }
            break;
        }
    }
}

    //2018.07.02 uner hehuan add interface for urovo psam start
    /**
     * Adapative urovo open interface, this interface will compelete connect service and other initiate operation.
     * @param slot---define the global slot id:
     * 0----IC slot
     * 1----PSAM SLOT 1
     * 2----PSAM SLOT 2
     * @param cardType----not used by psam.
     * @param volt----not used by psam.
     * @return
     * 0----open successfully.
     * -1----open failed.
     */
    public int open(byte slot, byte cardType, byte volt) {
        writeFile(PSAM_POWER_FILE,"1");//urovo zhangmeilin add for SQ47    20200611
        int result = 0;
        slot_id = slot & 0xff;
        Log.d(LOG_TAG, "current slot id is " + slot_id);
        return result;
    }

    /**
     * urovo close interface.
     * @param none
     * @return
     * 0----close successfully
     * -1----close failed
     */
    public int close() {
         writeFile(PSAM_POWER_FILE,"0");//urovo zhangmeilin add for SQ47    20200611
        //reset the slot id to -1
        slot_id = -1;
        return 0;
    }

    /**
     * urovo detect interface.
     * @param none.
     * @return
     * 0----card is present.
     * -1----card is not present.
     */
    public int activate() {
        if (slot_id == -1) {
          Log.e(LOG_TAG, "slot id is wrong, need initiate slot id firstly!");
          return -1;
        }

        if ((getCardState(slot_id, mCardState) == 0) && (mCardState.getCardState() == 1)) {
          Log.d(LOG_TAG, "detect card is present now!");
          return 0;
        }
        Log.e(LOG_TAG, "detect card state is not present, and card state = " + mCardState.getCardState());
        return -1;
    }

    /**
     * urovo activate interface.
     * @param
     * psam_data-----the object of psam data, include atr data.
     * @return
     * > 0-----activate successfully return atr data length.
     * -1----failed.
     *  note: for urovo requirment, we need to reset card firstly(include power down and power up)!!! and then get ATR.
     */
    public int reset(byte[] pAtr) {
        int get_atr_retry_times = 8;
        int wait_time = 500;
        if (slot_id == -1) {
          Log.e(LOG_TAG, "slot id is wrong, need initiate slot id firstly!");
          return -1;
        }

        MessageRequest reset_request = new MessageRequest(MSG_RESET_CARD);
        send(MSG_RESET_CARD, reset_request);
        synchronized (reset_request) {
            while (reset_request.argument == null) {
                try {
                    reset_request.wait(3000);//set timeout to 3000 ms, in case of main thread ANR.
                } catch (InterruptedException e) {
                    // Do nothing, go back and wait until the request is complete
                }
            }
        }

        if(reset_timeout_flag) {
          Log.e(LOG_TAG, "reset psam card timeout!!");
          return -2;
        }

        if(reset_request.status != 0) {
          Log.e(LOG_TAG, "reset card error!");
          return -1;
        }

        for(int j = 0; j < get_atr_retry_times; j++) {
          if(getCardATR(slot_id, mCardATR) == 0) {
              byte[] pAtr_tmp = mCardATR.getCardATR().getBytes();
              System.arraycopy(pAtr_tmp, 0, pAtr, 0, pAtr_tmp.length);
              return pAtr_tmp.length;
          } else {
           //wait for 100ms per time for modem get ATR
              try {
                Log.d(LOG_TAG, "sleep for get ATR times = " + j);
                Thread.sleep(wait_time);
              } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "sleep for get card atr error!");
              }
          }
        }
        return -1;
    }

    /**
     * urovo deactivate interface.
     * @param none.
     * @return
     * 0----deactivate successfully.
     * -1----deactivate failed.
     */
    public int deactivate() {
        if (slot_id == -1) {
          Log.e(LOG_TAG, "slot id is wrong, need initiate slot id firstly!");
          return -1;
        }

        MessageRequest power_down_request = new MessageRequest(MSG_POWER_DOWN);
        send(MSG_POWER_DOWN, power_down_request);
        synchronized (power_down_request) {
            while (power_down_request.argument == null) {
                try {
                    power_down_request.wait(1000);//set power down psam card timeout to 1000 ms in case of main thread ANR.
                } catch (InterruptedException e) {
                    // Do nothing, go back and wait until the request is complete
                }
            }
        }

        if(power_down_timeout_flag) {
          Log.e(LOG_TAG, "power down psam card timeout!!");
          return -2;
        }

        return power_down_request.status;
    }
    /*urovo zhangmeilin add for SQ47    20200611 st*/
    private String PSAM_POWER_FILE    =    "/sys/class/pwv-gpio-intf/vcc_power/enable";	
    private void writeFile(String fileName, String writestr) {
        File file = new File(fileName);
	     Log.d("zml", "writeFile  == "  + fileName + "  value==" + writestr);
        try {
            FileWriter mFileWriter = new FileWriter(file, false);
            mFileWriter.write(writestr);
            mFileWriter.close();
            } catch (IOException e) {
             }
       }
    /*urovo zhangmeilin add for SQ47    20200611 ed*/



    /**
    * Get Card State.
    * @param - slotId - SAM card slot id
    *        - state - (output variabe) SAM card state
    * @return status of query on SAM card state
    */
    public int getCardState(int slotId, CardState cardState) {
        int ret;

        if(mIPsamManager == null){
            Log.e(LOG_TAG, "service not connected!");
            return SAMErrors.SERVICE_NOT_CONNECTED;
        }

        Log.i(LOG_TAG, "getCardState");

        try {
            ret = mIPsamManager.getCardState(slotId, cardState);

            if (ret == 0) {
                Log.i(LOG_TAG, "getCardState return " + ret + cardState.getCardState());
            }

        } catch(RemoteException e){

            Log.e(LOG_TAG, "getCardState, remote exception");
            e.printStackTrace();
            ret = SAMErrors.INTERNAL_FAILURE;
        }

        return ret;
    }

    /**
    * Get Card ATR.
    * @param - slotId - SAM card slot id
    *        - cardATR - (output variabe) SAM card ATR
    * @return status of query on SAM card ATR
    */
    public int getCardATR(int slotId, CardATR cardATR) {
        int ret;

        if(mIPsamManager == null){
            Log.e(LOG_TAG, "service not connected!");
            return SAMErrors.SERVICE_NOT_CONNECTED;
        }

        Log.i(LOG_TAG, "getCardATR");

        try {
            ret = mIPsamManager.getCardATR(slotId, cardATR);

            if (ret == 0)
            {
                Log.i(LOG_TAG, "getCardATR return " + cardATR.getSlotId() + cardATR.getCardATR());
            }
        } catch(RemoteException e){

            Log.e(LOG_TAG, "getCardATR, remote exception");
            e.printStackTrace();
            ret = SAMErrors.INTERNAL_FAILURE;
        }

        return ret;
    }

    /**
    * Send APDU command.
    * @param - slotId - SAM card slot id
    *       Following fields are used to derive the APDU ("command" and "length"
    *          values in TS 27.007 +CSIM and +CGLA commands).
    *        - class_type - class type
    *        - instruction - instruction
    *        - param1 - parameter 1
    *        - param2 - parameter 2
    *        - param3 - parameter 3
    *        - dataCmd - command
    *        - callback - callback to be called asynchronously.
    * @return intermediate status
    */
    public int streamApdu(int slotId, int class_type, int instruction, int param1, int param2, int param3, String dataCmd, SAMResponseCallback callback) {
        int ret;

        if(mIPsamManager == null){
            Log.e(LOG_TAG, "service not connected!");
            return SAMErrors.SERVICE_NOT_CONNECTED;
        }

        Log.i(LOG_TAG, "streamApdu");

        try {
            ret = mIPsamManager.streamApdu(slotId, class_type, instruction, param1, param2, param3, dataCmd, callback);
        } catch(RemoteException e){
            Log.e(LOG_TAG, "streamApdu, remote exception");
            e.printStackTrace();
            ret = SAMErrors.INTERNAL_FAILURE;
        }

        return ret;
    }

    /**
    * Card power up request.
    * @param - slotId - SAM card slot id
    *        - callback - callback to be called asynchronously.
    *
    * @return intermediate status
    */
    public int cardPowerUp(int slotId, SAMResponseCallback callback) {
        int ret;

        if(mIPsamManager == null){
            Log.e(LOG_TAG, "service not connected!");
            return SAMErrors.SERVICE_NOT_CONNECTED;
        }

        Log.i(LOG_TAG, "cardPowerUp");

        try {
            ret = mIPsamManager.cardPowerUp(slotId, callback);
        } catch(RemoteException e){
            Log.e(LOG_TAG, "cardPowerUp, remote exception");
            e.printStackTrace();
            ret = SAMErrors.INTERNAL_FAILURE;
        }

        return ret;
    }

    /**
    * Card power down request.
    * @param - slotId - SAM card slot id
    *        - callback - callback to be called asynchronously.
    *
    * @return intermediate status
    */
    public int cardPowerDown(int slotId, SAMResponseCallback callback) {
        int ret;

        if(mIPsamManager == null){
            Log.e(LOG_TAG, "service not connected!");
            return SAMErrors.SERVICE_NOT_CONNECTED;
        }

        Log.i(LOG_TAG, "cardPowerDown");

        try {
            ret = mIPsamManager.cardPowerDown(slotId, callback);
        } catch(RemoteException e){
            Log.e(LOG_TAG, "cardPowerDown, remote exception");
            e.printStackTrace();
            ret = SAMErrors.INTERNAL_FAILURE;
        }

        return ret;
    }
}
