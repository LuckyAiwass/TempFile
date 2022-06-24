/******************************************************************************
  @file    SAMResponseCallback.java
  @brief   SAM asyncronous response callback definition. Client code can
           override the same and pass it to SAM Manager.

  ---------------------------------------------------------------------------
  Copyright (c) 2016 Qualcomm Technologies, Inc.
  All Rights Reserved.
  Confidential and Proprietary - Qualcomm Technologies, Inc.
  ---------------------------------------------------------------------------
******************************************************************************/

package com.ubx.usdk.psam;

import android.util.Log;//2018.06.19 uner hehuan add for print log
//2018.07.02 uner hehuan add interface for urovo psam start
import android.os.Message;
import android.os.Handler;
import com.ubx.usdk.psam.PsamManager.*;
//2018.07.02 uner hehuan add interface for urovo psam end
import com.qualcomm.qti.sam.interfaces.ISAMResponseCallback;

public class SAMResponseCallback extends ISAMResponseCallback.Stub {
    private final String LOG_TAG = "SAMResponseCallback";//2018.06.19 uner hehuan add for print log
    //2018.07.02 uner hehuan add interface for urovo psam start
    public static Handler mHandler;
    //2018.07.02 uner hehuan add interface for urovo psam end
    /**
    * APDU response callback.
    * @param - slotId - SAM card slot id
    *        - actionStatus - status of the request
    *        - statusWord1- status word
    *        - statusWord1- status word
    *        - dataRsp - data response
    * @return none
    */
    public void streamApduResponse(int slotId, int actionStatus, int statusWord1, int statusWord2, String dataRsp) {
        Log.i(LOG_TAG, "streamApduResponse  actionStatus = " + actionStatus);//2018.06.19 uner hehuan add for print log
        //2018.07.02 uner hehuan add interface for urovo psam start
        ApduResponseData apdu_data = new PsamManager.ApduResponseData();
        apdu_data.sw1 = statusWord1;
        apdu_data.sw2 = statusWord2;
        apdu_data.apdu_data_resp = dataRsp;

        Message msg2 = mHandler.obtainMessage();
        msg2.what = PsamManager.MSG_SEND_APDU_COMPLETE;
        msg2.arg1 = actionStatus;
        msg2.obj = apdu_data;
        msg2.sendToTarget();
        //2018.07.02 uner hehuan add interface for urovo psam end
    }

    /**
    * switch slot response callback.
    * @param - slotId - SAM card slot id
    *        - actionStatus - status of the request
    * @return none
    */
    public void switchSlotResponse(int actionStatus) {
    }

    /**
    * card power up response callback.
    * @param - slotId - SAM card slot id
    *        - actionStatus - status of the request
    * @return none
    */
    public void cardPowerUpResponse(int slotId, int actionStatus) {
        Log.i(LOG_TAG, "cardPowerUpResponse  actionStatus = " + actionStatus);//2018.06.19 uner hehuan add for print log
        //2018.07.02 uner hehuan add interface for urovo psam start
        Message msg1 = mHandler.obtainMessage();
        msg1.what = PsamManager.MSG_POWER_UP_COMPLETE;
        msg1.arg1 = actionStatus;
        msg1.sendToTarget();
        //2018.07.02 uner hehuan add interface for urovo psam end
    }

    /**
    * card power down callback.
    * @param - slotId - SAM card slot id
    *        - actionStatus - status of the request
    * @return none
    */
    public void cardPowerDownResponse(int slotId, int actionStatus) {
        Log.i(LOG_TAG, "cardPowerDownResponse  actionStatus = " + actionStatus);//2018.06.19 uner hehuan add for print log
        //2018.07.02 uner hehuan add interface for urovo start
        Message msg = mHandler.obtainMessage();
        msg.what = PsamManager.MSG_POWER_DOWN_COMPLETE;
        msg.arg1 = actionStatus;
        msg.sendToTarget();
        //2018.07.02 uner hehuan add interface for urovo end
    }

//2018.07.02 uner hehuan add interface for urovo start
    public SAMResponseCallback(Handler handler) {
        this.mHandler = handler;
    }
//2018.07.02 uner hehuan add interface for urovo end
}
