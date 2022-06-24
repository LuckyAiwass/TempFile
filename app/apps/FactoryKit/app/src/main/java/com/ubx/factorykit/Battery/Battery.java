/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.Battery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class Battery extends Activity {

    String TAG = "Battery";
    String resultString = Utilities.RESULT_FAIL;

    final String CAPACITY = "/sys/class/power_supply/battery/capacity";
    final String VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";
    final String STATUS = "/sys/class/power_supply/battery/status";

    // name="usbonline">/sys/class/power_supply/usb/online</string>//
    // name="aconline">/sys/class/power_supply/ac/online</string>


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void init(Context context)
    { 
        resultString = Utilities.RESULT_FAIL;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.battery);
        //init(this);

        boolean ret = false;
        String tmp = null;
        float voltage = 0;
        String result = "";

        tmp = getBatteryInfo(STATUS);
        if (tmp != null) {
            result += tmp + "\n";
            if ("Charging".equals(tmp) || "Full".equals(tmp))
                ret = true;
            else
                ret = false;
        }
        tmp = getBatteryInfo(VOLTAGE_NOW);
        if (tmp != null) {
                voltage = Float.valueOf(tmp);
            if (voltage > 1000000)
                voltage = voltage / 1000000;
            else if (voltage > 1000)
                voltage = voltage / 1000;
            result += (getString(R.string.battery_voltage)) + voltage + "V";

        }
        toast(result);
        if (ret)
            pass();
        else
            fail();
        Utilities.writeCurMessage(TAG, resultString);
        logd(resultString);
        finish();
        return;
    }

    private String getBatteryInfo(String path) {

        File mFile;
        FileReader mFileReader;
        mFile = new File(path);
        String status[] = null;
        try {
            mFileReader = new FileReader(mFile);
            char data[] = new char[128];
            int charCount;

            try {
                charCount = mFileReader.read(data);
                status = new String(data, 0, charCount).trim().split("\n");
                logd(status[0]);

            } catch (IOException e) {
                loge(e);
            }
        } catch (FileNotFoundException e) {
            loge(e);
        }
        return status[0];
    }

    void fail() {
        setResult(RESULT_CANCELED);
        resultString=Utilities.RESULT_FAIL;
        logd(resultString);

    }

    void pass() {
        setResult(RESULT_OK);
        resultString=Utilities.RESULT_PASS;
        logd(resultString);

    }

    public void toast(Object s) {

        if (s == null)
            return;
        //Toast.makeText(getApplicationContext(), s + "", Toast.LENGTH_SHORT).show();
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
