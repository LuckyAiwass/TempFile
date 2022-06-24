/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.PogoDC;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static com.ubx.factorykit.Framework.FactoryKitPro.PRODUCT_SQ45S;
import static com.ubx.factorykit.Framework.FactoryKitPro.PRODUCT_SQ53H;

public class PogoDcCharging extends Activity {

    String TAG = "PogoDcCharging";
    private static String resultString = Utilities.RESULT_FAIL;
    String result = "";
    //final String PGOG_DC_NODE = "/sys/devices/soc/soc:usb_switch/function_charger_det";
    private final String POGO_DC_NODE_SQ45S = "/sys/devices/platform/soc/7000000.ssusb/dc_state";
     private static final String PGOG_DC_NODE = SystemProperties.get("persist.sys.dc.charging",
            PRODUCT_SQ53H ? "/sys/devices/platform/ext_power_otg/pogo_dcin" :  "/sys/devices/soc/soc:usb_switch/function_charger_det");

    final String STATUS_DC_HIGH = PRODUCT_SQ53H ? "dcout" :"typec_dcin_det_gpio high";
    final String STATUS_DC_LOW = PRODUCT_SQ45S ? "typec_dcin_det_gpio low" : "dcin";

    @Override
    public void finish() {
        Utilities.writeCurMessage(this, TAG, resultString);
        super.finish();
    }

    private void init(Context context) {
        resultString = Utilities.RESULT_FAIL;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery);
        init(this);
        if(PRODUCT_SQ53H){
            try{
                result = getPogoBatteryInfo(PGOG_DC_NODE);
                if(result.equals(STATUS_DC_LOW)){
                    pass();
                }else if(result.equals(STATUS_DC_HIGH)){
                    fail(result + getResources().getString(R.string.pogo_dc_charge_mind));
                }
            } catch (NullPointerException e) {
                Log.i(TAG, "NullPointerException e:" + e);
            }
        }else {
            if(FactoryKitPro.PRODUCT_SQ45S) {
                result = getPogoBatteryInfo(POGO_DC_NODE_SQ45S);
            } else {
                result = getPogoBatteryInfo(PGOG_DC_NODE);
            }
            if (TextUtils.isEmpty(result)) {
                fail(result + getResources().getString(R.string.pogo_dc_charge_mind));
            } else {
                if (result.equals(STATUS_DC_LOW)) {
                    pass();
                } else {
                    fail(result + getResources().getString(R.string.pogo_dc_charge_mind));
                }
            }
        }
    }

    private String getPogoBatteryInfo(String path) {

        File mFile;
        FileReader mFileReader;
        mFile = new File(path);

        try {
            mFileReader = new FileReader(mFile);
            char data[] = new char[128];
            int charCount;
            String status[] = null;
            try {
                charCount = mFileReader.read(data);
                status = new String(data, 0, charCount).trim().split("\n");
                logd(status[0]);
                return status[0];
            } catch (IOException e) {
                loge(e);
            }
        } catch (FileNotFoundException e) {
            loge(e);
        }
        return null;
    }

    void fail(Object msg) {

        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        resultString = Utilities.RESULT_FAIL;
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        resultString = Utilities.RESULT_PASS;
        finish();
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(getApplicationContext(), s + "", Toast.LENGTH_SHORT).show();
    }

    private void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(TAG, e + "");
    }

    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }

}
