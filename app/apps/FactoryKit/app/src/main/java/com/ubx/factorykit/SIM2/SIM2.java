/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.SIM2;

import android.app.Activity;
import android.app.Service;
import android.os.Bundle;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;
import com.ubx.factorykit.Values;

public class SIM2 extends Activity {

    String TAG = "SIM2";
    String resultString = "Failed";
    String toastString = "";
    int simState = TelephonyManager.SIM_STATE_UNKNOWN;
    boolean result = false;
    int SUB_ID = 1;

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logd("");

        super.onCreate(savedInstanceState);
        String IMSI = null;
        int simState = TelephonyManager.SIM_STATE_ABSENT;
        boolean isSubActive = false;
        TelephonyManager mTelephonyManager = (TelephonyManager)getSystemService(
                Service.TELEPHONY_SERVICE);
        int subid = 1;
        int[] subids = SubscriptionManager.getSubId(SUB_ID);
        if(subids != null){
            subid = subids[0];
            logd("subid=" + subid);
        }
        if (mTelephonyManager == null) {
            finish();
            return;
        }
        if(subids != null) {
            if (TelephonyManager.getDefault().isMultiSimEnabled() && subids != null) {
                simState = mTelephonyManager.getSimState(SUB_ID);
                logd("SIM state=" + simState);
                IMSI = mTelephonyManager.getSubscriberId(subid);
                isSubActive = Utilities.isSimSubscriptionStatusActive(SUB_ID);
            } else {
                simState = mTelephonyManager.getSimState();
                logd("SIM state=" + simState);
                IMSI = mTelephonyManager.getSubscriberId();
            }
        }
        if (IMSI != null && !IMSI.equals("")) {
            result = true;
            toastString = "IMSI: " + IMSI;
        } else if (simState != TelephonyManager.SIM_STATE_ABSENT) {
            result = true;
            toastString = "State: Ready";
        } else if (isSubActive){
            result = true;
            toastString = "SIM: Enabled";
        }

        if (result) {
            setResult(RESULT_OK);
            resultString = Utilities.RESULT_PASS;
            toast(toastString);

        } else {
            setResult(RESULT_CANCELED);
            resultString = Utilities.RESULT_FAIL;
        }
        Utilities.writeCurMessage(TAG, resultString);
        logd(resultString);
        finish();
        return;
    }

    public void toast(Object s) {

        //Toast.makeText(getApplicationContext(), s + "", Toast.LENGTH_SHORT).show();
    }

    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            