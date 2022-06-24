/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.Heating;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import java.util.Timer;
import java.util.TimerTask;

public class Heating extends Activity {
    
    static String TAG = "Heating";
    Button passButton, failButton,lcdButton, scanButton,mbButton;
    TextView mLCDTempTextView,mSCANTempTextView,mMBTempTextView,
            mLCDNotifyView,mSCANNotifyView,mMBNotifyView;
    private static final String LCD_TEMP_PATH = "/sys/kernel/heat/lcd_temp";
    private static final String SCAN_TEMP_PATH = "/sys/kernel/heat/scan_temp";
    private static final String MB_TEMP_PATH = "/sys/kernel/heat/mb_temp";
    private static final String LCD_HEATING_PATH = SystemProperties.get("persist.sys.heat.lcd","/sys/kernel/heat/lcd");
    private static final String SCAN_HEATING_PATH = SystemProperties.get("persist.sys.heat.scanner","/sys/kernel/heat/scan");
    private static final String MB_HEATING_PATH = SystemProperties.get("persist.sys.heat.mb","/sys/kernel/heat/mb");
    private static final String HEATING_LED_PATH = SystemProperties.get("persist.sys.heat.led","/sys/class/pwv-gpio-intf/pogo_pin_power/enable");
    private static final String HEATING_CLOSE_PATH = "/sys/kernel/heat/temp_exit";
    private static final String VALUE_ON = "60000";//1  60000 mean heating 60*1000  60S
    private static final String VALUE_OFF = "0";
    private static final int MSG_OPEN = 0;
    private static final int MSG_OPEN_FAILED = 1;
    private static final int MSG_OPEN_SUCESS = 2;
    private static final int MSG_CLOSE = 3;
    private static final int MSG_QUIT = 4;
    private static final int MSG_UPDATE_SYNC = 5;
    private static final int LCD_HEATING = 0;
    private static final int SCAN_HEATING = 1;
    private static final int MB_HEATING = 2;
    private String lcdTempValue = "";
    private String scanTempValue = "";
    private String mbTempValue = "";
    private static boolean MSG_NO_NEED_CANCELL = false;
    private static boolean QUIT_FLAG = false;


    Context mContext = null;
    Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int device;
            switch (msg.what){
                case MSG_OPEN:
                    break;
                case MSG_OPEN_FAILED:
                    device = (int) msg.obj;
                    if(device==LCD_HEATING){
                        mLCDNotifyView.setText(getString(R.string.lcd_heating_status) + getString(R.string.heating_open_failed));
                        mLCDNotifyView.setTextColor(Color.RED);
                    }else if(device==SCAN_HEATING){
                        mSCANNotifyView.setText(getString(R.string.scan_heating_status) + getString(R.string.heating_open_failed));
                        mLCDNotifyView.setTextColor(Color.RED);
                    }else if(device==MB_HEATING){
                        mMBNotifyView.setText(getString(R.string.mb_heating_status) + getString(R.string.heating_open_failed));
                        mLCDNotifyView.setTextColor(Color.RED);
                    }
                    break;
                case MSG_OPEN_SUCESS:
                    device = (int) msg.obj;
                    if(device==LCD_HEATING){
                        mLCDNotifyView.setText(getString(R.string.lcd_heating_status) + getString(R.string.heating_open_success));
                    }else if(device==SCAN_HEATING){
                        mSCANNotifyView.setText(getString(R.string.scan_heating_status) + getString(R.string.heating_open_success));
                    }else if(device==MB_HEATING){
                        mMBNotifyView.setText(getString(R.string.mb_heating_status) + getString(R.string.heating_open_success));
                    }
                    break;
                case MSG_CLOSE:
                    device = (int) msg.obj;
                    if(device==LCD_HEATING){
                        mLCDNotifyView.setText(getString(R.string.lcd_heating_status) + getString(R.string.heating_close));
                    }else if(device==SCAN_HEATING){
                        mSCANNotifyView.setText(getString(R.string.scan_heating_status) + getString(R.string.heating_close));
                    }else if(device==MB_HEATING){
                        mMBNotifyView.setText(getString(R.string.mb_heating_status) + getString(R.string.heating_close));
                    }
                    break;
                case MSG_QUIT:
                        MSG_NO_NEED_CANCELL = true;
                        closeHeating();
                        mLCDNotifyView.setText(getString(R.string.lcd_heating_status) + getString(R.string.heating_close));
                        mSCANNotifyView.setText(getString(R.string.scan_heating_status) + getString(R.string.heating_close));
                        mMBNotifyView.setText(getString(R.string.mb_heating_status) + getString(R.string.heating_close));
                    break;
                case MSG_UPDATE_SYNC:
                    lcdTempValue = Utilities.readFile(LCD_TEMP_PATH);
                    scanTempValue = Utilities.readFile(SCAN_TEMP_PATH);
                    mbTempValue = Utilities.readFile(MB_TEMP_PATH);
                    mLCDTempTextView.setText(lcdTempValue);
                    mSCANTempTextView.setText(scanTempValue);
                    mMBTempTextView.setText(mbTempValue);
                    break;
                default:
                    break;
            }
        };
    };

    Timer mTimer = new Timer();

    TimerTask mTask = new TimerTask(){
        public void run() {
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = MSG_UPDATE_SYNC;
                    mHandler.sendMessage(message);
                }});
        }
    };



    private void quitAction(){
        Log.d("zml","quitAction   " + QUIT_FLAG);
        //if(QUIT_FLAG)
        //    return;
        //QUIT_FLAG = true;
        closeHeating();
        mTask.cancel();
        //if(mHandler != null && !MSG_NO_NEED_CANCELL)
        //    mHandler.removeMessages(MSG_QUIT);
    }
    @Override
    public void finish() {
        quitAction();
        super.finish();
    }

    void bindView() {
        passButton = (Button) findViewById(R.id.pass);
        failButton = (Button) findViewById(R.id.fail);
        lcdButton = (Button) findViewById(R.id.lcd_heating);
        scanButton = (Button) findViewById(R.id.scan_heating);
        mbButton = (Button) findViewById(R.id.mb_heating);
        mLCDTempTextView = (TextView) findViewById(R.id.lcd_temp_value);
        mSCANTempTextView = (TextView) findViewById(R.id.scan_temp_value);
        mMBTempTextView = (TextView) findViewById(R.id.mb_temp_value);
        mLCDNotifyView = (TextView) findViewById(R.id.lcd_notify);
        mSCANNotifyView = (TextView) findViewById(R.id.scan_notify);
        mMBNotifyView = (TextView) findViewById(R.id.mb_notify);
        passButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                pass();
            }
        });

        failButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                fail(null);
            }
        });
    }



    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.heating);
        bindView();
        setHeatingEable(true);//open heating
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //mHandler.sendEmptyMessageDelayed(MSG_QUIT,2*60*1000);//close heating
        mTimer.schedule(mTask,1000,2000);
    }

    void setHeatingEable(boolean value) {
        if(value){
            //Utilities.writeFile(HEATING_LED_PATH,VALUE_ON);
            if(Utilities.writeFile(LCD_HEATING_PATH,VALUE_ON)){
                sendMsg(LCD_HEATING,MSG_OPEN_SUCESS);
            }
            else{
                sendMsg(LCD_HEATING,MSG_OPEN_FAILED);
            }

            if(Utilities.writeFile(SCAN_HEATING_PATH,VALUE_ON)){
                sendMsg(SCAN_HEATING,MSG_OPEN_SUCESS);
            }
            else{
                sendMsg(SCAN_HEATING,MSG_OPEN_FAILED);
            }

            if(Utilities.writeFile(MB_HEATING_PATH,VALUE_ON)){
                sendMsg(MB_HEATING,MSG_OPEN_SUCESS);
            }
            else{
                sendMsg(MB_HEATING,MSG_OPEN_FAILED);
            }

        }else{
            Utilities.writeFile(LCD_HEATING_PATH,VALUE_OFF);
            sendMsg(LCD_HEATING,MSG_CLOSE);
            Utilities.writeFile(SCAN_HEATING_PATH,VALUE_OFF);
            sendMsg(SCAN_HEATING,MSG_CLOSE);
            Utilities.writeFile(MB_HEATING_PATH,VALUE_OFF);
            sendMsg(MB_HEATING,MSG_CLOSE);
            //Utilities.writeFile(HEATING_CLOSE_PATH,VALUE_ON);
        }
    }
    private  void closeHeating(){
        Log.d("zml","closeHeating");
        //Utilities.writeFile(HEATING_LED_PATH,VALUE_OFF);
        Utilities.writeFile(LCD_HEATING_PATH,VALUE_OFF);
        Utilities.writeFile(SCAN_HEATING_PATH,VALUE_OFF);
        Utilities.writeFile(MB_HEATING_PATH,VALUE_OFF);
        //Utilities.writeFile(HEATING_CLOSE_PATH,VALUE_OFF);

    }


    private synchronized void sendMsg(int device,int status) {
        Message message = new Message();
        message.what = status;
        message.obj = device;
        mHandler.sendMessage(message);
    }
    

    
    void setButtonClickable(boolean cmd) {
        passButton.setClickable(cmd);
        passButton.setFocusable(cmd);
        failButton.setClickable(cmd);
        failButton.setFocusable(cmd);
    }

    @Override
    protected void onResume() {
        QUIT_FLAG =false;
        super.onResume();
    }
    
    @Override
    protected void onDestroy() {
        //quitAction();
        super.onDestroy();
    }
    
    void fail(Object msg) {
        toast(msg);
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(mContext, TAG, "Failed");
        finish();
    }
    
    void pass() {
        setResult(RESULT_OK);
        Utilities.writeCurMessage(mContext, TAG, "Pass");
        finish();
    }
    
    public void toast(Object s) {
        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }

}
