/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.factorykit.Pogo2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;
import com.serialport.SerialPort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

public class PogoPin2 extends Activity {

    final String POGO_NODE = "/sys/devices/platform/soc/soc:meig-gpios/meig-gpios/otg_enable";
    final String POGO_SCL_NODE = "/sys/devices/platform/soc/soc:meig-gpios/meig-gpios/pogo_scl";
    final String POGO_SDA_NODE = "/sys/devices/platform/soc/soc:meig-gpios/meig-gpios/pogo_sda";
    final String POGO_NODE_SQ83 = "/sys/devices/virtual/Usb_switch/usbswitch/function_otg_en";
    final String POGO_SCL_NODE_SQ83 = "/sys/devices/platform/soc/soc:misc-gpios/misc-gpios/pogo_scl";
    final String POGO_SDA_NODE_SQ83 = "/sys/devices/platform/soc/soc:misc-gpios/misc-gpios/pogo_sda";
    TextView mTextView, mTextSeialPort, mTextPogoNode, mTextPogoNode2;
    String TAG = "PogoPin2";
    private Context mContext;
    private boolean isOTGStorageAvi = false;
    private StorageManager mStorageManager;

    // 串口操作 2019.07.16 begin
    private SerialPort mSerialPort = null;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    protected String pathName;
    protected int speed = -1;
    private String sendData = "123456go";

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[256];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        //android.util.Log.i(TAG, "size: " + size);
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    android.util.Log.i(TAG, "IOException: ");
                    e.printStackTrace();
                    return;
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    protected void onDataReceived(final byte[] buffer, final int size) {
        runOnUiThread(new Runnable() {
            public void run() {
                String recv = new String(buffer, 0, size);
                Log.d(TAG, "onDataReceived = " + recv);
                if (recv.equals(sendData)) {
//                    mTextSeialPort.setText("onDataReceived = " + recv);
//                    mTextSeialPort.setTextColor(Color.GREEN);
                }
            }
        });
    }

    public void initSerial() {
        try {
            pathName = "/dev/ttyMSM3";
            if (speed == -1) speed = 9600; //115200
            mSerialPort = new SerialPort(new File(pathName), speed, 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        } catch (SecurityException e) {
            Log.i(TAG, "security e:" + e);
        } catch (IOException e) {
            Log.i(TAG, "IOException e:" + e);
        } catch (InvalidParameterException e) {
            Log.i(TAG, "InvalidParameterException e:" + e);
        }
    }

    private final StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            if (isInteresting(vol)) {
                isudiskExists();
                Log.i(TAG, "onVolumeStateChanged isOTGStorageAvi=" + isOTGStorageAvi);
                if (isOTGStorageAvi) {
                    refresh();
                    // quitActionTimer.start(); //三项测试，不做自动测试
                }
            }
        }

        @Override
        public void onDiskDestroyed(DiskInfo disk) {
            //refresh();
            //finish();
            //System.exit(0);
            //fail(null);
        }
    };

    private static boolean isInteresting(VolumeInfo vol) {
        switch (vol.getType()) {
            //case VolumeInfo.TYPE_PRIVATE:
            case VolumeInfo.TYPE_PUBLIC:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.pogo2);
        mTextView = (TextView) findViewById(R.id.pogo2_otg_hint);
        mTextView.setText(getString(R.string.otg_wait));
//        mTextSeialPort = findViewById(R.id.pogo2_serial_hint);
//        mTextSeialPort.setText(getString(R.string.pogo_serila_tip));
        mTextPogoNode = findViewById(R.id.pogo2_key_hint_01);
        Button mPogoNodeBtn = findViewById(R.id.pogo2_key_btn_01);

        mPogoNodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextPogoNode.setText("pogo_scl " + getPogoNode(POGO_SCL_NODE_SQ83) + " " + (getPogoNode(POGO_SCL_NODE_SQ83).equals("0") ? "pass" : "fail"));
                mTextPogoNode.setTextColor(getPogoNode(POGO_SCL_NODE_SQ83).equals("0") ? Color.GREEN : Color.RED);
                toast("SCL");
            }
        });

        mTextPogoNode2 = findViewById(R.id.pogo2_key_hint_02);
        Button mPogoNodeBtn2 = findViewById(R.id.pogo2_key_btn_02);

        mPogoNodeBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTextPogoNode2.setText("pogo_sda " + getPogoNode(POGO_SDA_NODE_SQ83) + " " + (getPogoNode(POGO_SDA_NODE_SQ83).equals("0") ? "pass" : "fail"));
                mTextPogoNode2.setTextColor(getPogoNode(POGO_SDA_NODE_SQ83).equals("0") ? Color.GREEN : Color.RED);
                toast("SDA");
            }
        });

//        Button mSendDataBtn = findViewById(R.id.pogo2_serial_btn);
        /*mSendDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 向串口发送数据
                    if (mOutputStream != null) mOutputStream.write(sendData.getBytes());
//                    mTextSeialPort.setText(getResources().getString(R.string.pogo_serila_send_txt_mind));
//                    mTextSeialPort.setTextColor(Color.RED);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });*/
        init();

        Button pass = findViewById(R.id.pass);
        pass.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                pass();
            }
        });
        Button fail = findViewById(R.id.fail);
        fail.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                fail(null);
            }
        });


    }

    /**
     * 充电状态
     * @param path
     * @return
     */
    private String getBatteryInfo(String path) {

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

    /**
     * 获取节点状态
     * @param node
     * @return
     */
    private String getPogoNode(String node) {
        FileInputStream inputStream = null;
        byte[] buffer = new byte[16];
        try {
            inputStream = new FileInputStream(node);
            inputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i(TAG, "getPogoNode =" + new String(buffer).trim());
        return new String(buffer).trim();
    }

    /**
     * POGO PIN key Test
     *
     * @param event The key event.
     * @return
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        TextView mKeyMind = findViewById(R.id.pogo2_key_hint_tv);
        mKeyMind.setText(getResources().getString(R.string.pogo_key_mind) + keyCode);
        TextView keyText = null;
        switch (keyCode) {
            case 190:
                keyText = findViewById(R.id.pogo2_key_01);
                break;
            case 191:
                keyText = findViewById(R.id.pogo2_key_02);
                break;
            default:
                break;
        }
        if (keyText != null) {
            keyText.setBackgroundResource(R.color.green);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private void init() {
        mContext = getApplicationContext();
        mStorageManager = mContext.getSystemService(StorageManager.class);
        mStorageManager.registerListener(mStorageListener);
    }

    private void refresh() {
        final List<VolumeInfo> volumes = mStorageManager.getVolumes();
        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
        Log.i(TAG, "refresh volumes.size=" + volumes.size());
        for (VolumeInfo volume : volumes) {
            DiskInfo diskInfo = volume.getDisk();
            if (diskInfo != null && diskInfo.isUsb()) {
                String sdcardState = volume.getEnvironmentForState(volume.getState());
                if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
                    updateDisplay(volume);
                }
            }
        }
    }

    private void updateDisplay(VolumeInfo volume) {
        if (volume.isMountedReadable()) {
            //mVolume = volume;
            final File path = volume.getPath();
            //if (totalBytes <= 0) {
            final long totalBytes = path.getTotalSpace();
            // }
            final long freeBytes = path.getFreeSpace();
            final long usedBytes = totalBytes - freeBytes;

            final String used = Formatter.formatFileSize(mContext, usedBytes);
            final String total = Formatter.formatFileSize(mContext, totalBytes);

            Log.i(TAG, "updateDisplay used=" + used + ";total=" + total);
            mTextView.setText("total= " + total + " used of " + used);
            mTextView.setTextColor(Color.GREEN);
        } else
            mTextView.setText(volume.getStateDescription());

    }

    public void isudiskExists() {
        int num = 0;
        List<VolumeInfo> volumes = mStorageManager.getVolumes();
        for (VolumeInfo volInfo : volumes) {
            DiskInfo diskInfo = volInfo.getDisk();
            if (diskInfo != null && diskInfo.isUsb()) {
                String sdcardState = volInfo.getEnvironmentForState(volInfo.getState());
                Log.i(TAG, "isudiskExists sdcardState=" + sdcardState);
                if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
                    num++;
                }
            }
        }
        Log.i(TAG, "isudiskExists num=" + num);
        if (num > 0)
            isOTGStorageAvi = true;
        else
            isOTGStorageAvi = false;
    }

    @Override
    protected void onResume() {
        //setPogoNode(POGO_NODE, "3");
        setPogoNode(POGO_NODE_SQ83,"4");
        mStorageManager.registerListener(mStorageListener);
        isudiskExists();
        Log.i(TAG, "onResume isOTGStorageAvi=" + isOTGStorageAvi);
        if (isOTGStorageAvi) {
            refresh();
            quitActionTimer.start();
        }
        // 串口操作 2019.07.16 begin
        /*initSerial();
        mReadThread = new ReadThread();
        mReadThread.start();*/
        // 串口操作 2019.07.16 end
        super.onResume();
    }

    @Override
    protected void onPause() {
        //setPogoNode(POGO_NODE, "0");
        setPogoNode(POGO_NODE_SQ83,"6");
        mStorageManager.unregisterListener(mStorageListener);
        //if (mReadThread != null) mReadThread.interrupt();// 串口操作
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //if (mSerialPort != null && mSerialPort.mFd != null) mSerialPort.close();
    }

    private final int QUIT_DELAY_TIME = 1500;

    CountDownTimer quitActionTimer = new CountDownTimer(QUIT_DELAY_TIME, QUIT_DELAY_TIME) {

        @Override
        public void onTick(long arg0) {
        }

        @Override
        public void onFinish() {
            pass();
        }
    };

    void fail(Object msg) {

        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG, "Failed");
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG, "Pass");
        finish();
    }

    private void setPogoNode(String patch, String value) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(patch);
            outputStream.write(value.getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
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

    @SuppressWarnings("unused")
    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }
}
