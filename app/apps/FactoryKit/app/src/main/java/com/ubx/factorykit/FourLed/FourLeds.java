package com.ubx.factorykit.FourLed;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.device.MaxNative;
import android.os.Bundle;
import android.os.CountDownTimer;

import java.io.FileOutputStream;

import android.util.Log;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;


public class FourLeds extends Activity {

    private String TAG = "FourLeds";
    private int colorNum = 7;
    private int color = 0;
    private final int BLUE = 0;
    private final int YELLOW = 1;
    private final int GREEN = 2;
    private final int RED = 3;
    private final int SYSRED = 4;
    private final int SYSGREEN = 5;
    private final byte[] LIGHT_ON = {'2', '5', '5'};
    private final byte[] LIGHT_OFF = {'0'};
    private final String RED_LED_DEV = "/sys/class/leds/red/brightness";
    private final String GREEN_LED_DEV = "/sys/class/leds/green/brightness";

    CountDownTimer mCountDownTimer = new CountDownTimer(colorNum * 1200 + 200, 1200) {

        public void onTick(long arg0) {
            setColor(color++);
        }

        public void onFinish() {
            showDialog();
        }
    };

    public void setColor(int number) {
        boolean blue = false, yellow = false, green = false, red = false, sysred = false, sysgreen = false;
        setIndicatorLED(BLUE, 0);
        setIndicatorLED(YELLOW, 0);
        setIndicatorLED(GREEN, 0);
        setIndicatorLED(RED, 0);
        switch (number) {
            case BLUE:
                blue = true;
                break;
            case YELLOW:
                yellow = true;
                break;
            case GREEN:
                green = true;
                break;
            case RED:
                red = true;
                break;
            case SYSRED:
                sysred = true;
                break;
            case SYSGREEN:
                sysgreen = true;
                break;
            default:
                break;
        }
        try {
            if (blue) setIndicatorLED(BLUE, 1);
            if (yellow) setIndicatorLED(YELLOW, 1);
            if (green) setIndicatorLED(GREEN, 1);
            if (red) setIndicatorLED(RED, 1);
            //Log.e("liqianbo","sn ==========>>>"+(new android.device.DeviceManager()).getDeviceId());
            //(new android.device.DeviceManager()).setBatteryLed(RED_LED_DEV,sysred ? 1 : 0);
            //(new android.device.DeviceManager()).setBatteryLed(GREEN_LED_DEV,sysgreen ? 1 : 0);
            FileOutputStream fRed = new FileOutputStream(RED_LED_DEV);
            fRed.write(sysred ? LIGHT_ON : LIGHT_OFF);
            fRed.close();
            FileOutputStream fGreen = new FileOutputStream(GREEN_LED_DEV);
            fGreen.write(sysgreen ? LIGHT_ON : LIGHT_OFF);
            fGreen.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fourcolor_led);
        init();
    }

    private void init() {
        // TODO Auto-generated method stub
        //Toast.makeText(Fengmq.this, "Not implemented", Toast.LENGTH_SHORT).show();
        //fail(null);
        mCountDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        mCountDownTimer.cancel();
        super.onDestroy();
    }

    private void showDialog() {

        new AlertDialog.Builder(FourLeds.this).setMessage(R.string.fourled_confirm)
                .setPositiveButton(R.string.yes, passListener).setNegativeButton(R.string.no, failListener).show();
    }

    OnClickListener passListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            setResult(RESULT_OK);
            Utilities.writeCurMessage(FourLeds.this, TAG, "Pass");
            finish();
        }
    };

    OnClickListener failListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            setResult(RESULT_CANCELED);
            Utilities.writeCurMessage(FourLeds.this, TAG, "Failed");
            finish();
        }
    };

    void fail(Object msg) {
        //loge(msg);
        //toast(msg);
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG, "Failed");
        finish();
    }

    void pass() {
        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG, "Pass");
        finish();
    }

    private int setIndicatorLED(int id, int onoff) {
        byte[] ResponseData = new byte[32];
        byte[] ResLen = new byte[1];
        return MaxNative.setLed(id, onoff, ResponseData, ResLen);
    }
}
