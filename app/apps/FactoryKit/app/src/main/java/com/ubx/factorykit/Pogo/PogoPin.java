/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.factorykit.Pogo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.SystemProperties;
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

import com.ubx.factorykit.DeviceInfo.DeviceInfo;
import com.ubx.factorykit.Framework.FactoryKitPro;
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

import static com.ubx.factorykit.Framework.FactoryKitPro.POGO_KEY_SERIAL;
import static com.ubx.factorykit.Framework.FactoryKitPro.PRODUCT_SQ47;
import static com.ubx.factorykit.Framework.FactoryKitPro.PRODUCT_SQ83;
import static com.ubx.factorykit.Framework.FactoryKitPro.PRODUCT_SQ53H;

public class PogoPin extends Activity {
    //pogo 电源开关
    final String POGO_NODE_YOUKAI =SystemProperties.get("persist.sys.pogopin.otgdata.switch",
            PRODUCT_SQ53H ? "/sys/devices/platform/ext_power_otg/otg_mode" : "/sys/class/Usb_switch/usbswitch/function_otg_en");
    //pogo otg模式开关
    final String POGO_NODE = SystemProperties.get("persist.sys.pogopin.otg5v.en",
            PRODUCT_SQ53H ? "/sys/devices/platform/ext_power_otg/otg_vbus"  :
            "/sys/devices/soc/78db000.usb/otg_enable");
    //pogo id脚节点
    final String POGO_ID_TEST_NODE =SystemProperties.get("persist.sys.pogopin.id",
            PRODUCT_SQ53H ? "/sys/devices/platform/ext_power_otg/pogo_id" : "/sys/devices/soc/78db000.usb/pogo_id");
    //pogo 按键节点
    final String POGO_KEY_READY_YOUKAI = SystemProperties.get("persist.sys.pogopin.key",
            PRODUCT_SQ53H ? "/sys/devices/platform/ext_power_otg/pogo_key" :  "/sys/devices/soc/soc:gpio_keys/pogo_key");

    final String POGO_NODE_SQ47 ="/sys/devices/virtual/Usb_switch/usbswitch/function_otg_en";
    final String VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";
    final String STATUS = "/sys/class/power_supply/battery/status";
    // 测试先打开节点，退出关闭 2019.09.18
    final String POGO_UART_READY_YOUKAI = "/sys/devices/soc/c170000.serial/pogo_uart";
    final String POGO_ID_READY_YOUKAI = "/sys/devices/virtual/Usb_switch/usbswitch/pogo_id";
    final String POGO_ID_TEST_NODE_YOUKAI = "/sys/devices/virtual/Usb_switch/usbswitch/gpio_power_sw";
    final String POGO_ID_TEST_NODE_SQ45S = "/sys/devices/platform/soc/7000000.ssusb/pogo_id";
    final String POGO_NODE_SQ45S = "/sys/devices/platform/soc/7000000.ssusb/otg_enable";
    
    //SQ51S节点 2019.11.01
    final String POGO_NODE_SQ51S = "/sys/devices/soc/soc:usb_switch/function_otg_en";

    //SQ53X OTG测试节点 2021.11.30
    final String POGO_NODE_SQ53X = "/sys/kernel/kobject_pogo_otg_status/pogo_otg_status";

    TextView mTextView, mTextSeialPort, mTextCharge, mTextPogoNode, mKeyMind;
    String TAG = "PogoPin";
    private Context mContext;
    private boolean isOTGStorageAvi = false;
    private StorageManager mStorageManager;

    // 串口操作 2019.07.16 begin
    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    protected String pathName;
    protected int speed = -1;
    private String sendData = "123456go";
    private int test_SQ45S = 3;
    private AutoTest autoTest;
    private boolean test1,test2,test3,test4 = false;

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
                        // android.util.Log.i(TAG, "size: " + size);
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
                if (recv.contains(sendData)) {
                    mTextSeialPort.setText("onDataReceived = " + sendData);
                    mTextSeialPort.setTextColor(Color.GREEN);
                    test2 = true;
                }
            }
        });
    }

    public void initSerial() {
        try {
            // urovo yuanwei adjust youkai begin 2019.07.24
            if (FactoryKitPro.PRODUCT_SQ53 || FactoryKitPro.PRODUCT_SQ53Z) {
                pathName = "/dev/ttyHSL0";
            } else if (FactoryKitPro.PRODUCT_SQ51S || PRODUCT_SQ47 || PRODUCT_SQ83 || FactoryKitPro.PRODUCT_SQ45S) {
                pathName = "/dev/ttyMSM0";
            } else if (PRODUCT_SQ53H) {
                pathName = "/dev/ttyS1";
            }else {
                pathName = POGO_KEY_SERIAL;
            }

            Log.i(TAG, "pathName : " + pathName);
            // urovo yuanwei adjust youkai end 2019.07.24
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
    // 串口操作 2019.07.16 end

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

    private void batteryCharing() {
        boolean ret = false;
        String tmp = null;
        float voltage = 0;
        String result = "";

        tmp = getBatteryInfo(STATUS);
        if (tmp != null) {
            result += tmp + "\n";
            if ("Charging".equals(tmp) || "Full".equals(tmp)) {
                ret = true;
                test3 = true;
            }
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
        mTextCharge.setText(result);
        mTextCharge.setTextColor(ret ? Color.GREEN : Color.RED);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.pogo);
        mTextView = (TextView) findViewById(R.id.pogo_otg_hint);
        mTextView.setText(getString(R.string.otg_wait));
        mTextSeialPort = findViewById(R.id.pogo_serial_hint);
        mTextSeialPort.setText(getString(R.string.pogo_serila_tip));
        mTextPogoNode = findViewById(R.id.pogo_key_hint);
        mKeyMind = findViewById(R.id.pogo_key_hint_tv);
        Button mPogoNodeBtn = findViewById(R.id.pogo_key_btn);
        mPogoNodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FactoryKitPro.PRODUCT_SQ53 || FactoryKitPro.PRODUCT_SQ53Z) {
                    mTextPogoNode.setText(getPogoNode(POGO_ID_TEST_NODE_YOUKAI) + " " + (getPogoNode(POGO_ID_TEST_NODE_YOUKAI).equals("0") ? "pass" : "fail"));
                    mTextPogoNode.setTextColor(getPogoNode(POGO_ID_TEST_NODE_YOUKAI).equals("0") ? Color.GREEN : Color.RED);
                } else if (FactoryKitPro.PRODUCT_SQ45S) {
                    mTextPogoNode.setText(getPogoNode(POGO_ID_TEST_NODE_SQ45S) + " " + (getPogoNode(POGO_ID_TEST_NODE_SQ45S).equals("0") ? "pass" : "fail"));
                    mTextPogoNode.setTextColor(getPogoNode(POGO_ID_TEST_NODE_SQ45S).equals("0") ? Color.GREEN : Color.RED);
                } else {
                    mTextPogoNode.setText(getPogoNode(POGO_ID_TEST_NODE) + " " + (getPogoNode(POGO_ID_TEST_NODE).equals("0") ? "pass" : "fail"));
                    mTextPogoNode.setTextColor(getPogoNode(POGO_ID_TEST_NODE).equals("0") ? Color.GREEN : Color.RED);
                }
            }
        });
        mTextCharge = findViewById(R.id.pogo_charge_result);
        Button mChargeBtn = findViewById(R.id.pogo_charge_btn);
        mChargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                batteryCharing();
            }
        });
        Button mSendDataBtn = findViewById(R.id.pogo_serial_btn);
        mSendDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // 向串口发送数据
                    if (mOutputStream != null) mOutputStream.write(sendData.getBytes());
                    mTextSeialPort.setText(getResources().getString(R.string.pogo_serila_send_txt_mind));
                    mTextSeialPort.setTextColor(Color.RED);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
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

        // SQ53 无pogo key 2
        if (FactoryKitPro.PRODUCT_SQ53 || FactoryKitPro.PRODUCT_SQ53Z) {
            // findViewById(R.id.pogo_key_02).setVisibility(View.GONE);
            String hardHarPogoKey = "Rev 1.4";
            String hardHarPogoKeyGet = DeviceInfo.getVersionString(DeviceInfo.HARDWARE_DEV).trim();
            if (!hardHarPogoKeyGet.equals(hardHarPogoKey)) {
                Log.d(TAG, "SQ53 hard version is not 'Rev 1.4' ");
                findViewById(R.id.pogo_key_btn_divide_view).setVisibility(View.GONE);
                findViewById(R.id.pogo_key_btn_view).setVisibility(View.GONE);
            }

        }
        //urovo luoqi SQ51S 去除测试5 2019.11.01
        if (FactoryKitPro.PRODUCT_SQ51S || FactoryKitPro.PRODUCT_SQ53Q || PRODUCT_SQ83
                || FactoryKitPro.PRODUCT_SQ53X || FactoryKitPro.PRODUCT_SQ53S) {
            findViewById(R.id.pogo_key_btn_divide_view).setVisibility(View.GONE);
            findViewById(R.id.pogo_key_btn_view).setVisibility(View.GONE);
        }
        if (PRODUCT_SQ47){
            findViewById(R.id.pogo_key).setVisibility(View.GONE);
            findViewById(R.id.pogo_key_hint_tv).setVisibility(View.GONE);
            findViewById(R.id.pogo_key_01).setVisibility(View.GONE);
            findViewById(R.id.pogo_key_line).setVisibility(View.GONE);
            findViewById(R.id.pogo_pin_t2).setVisibility(View.GONE);
            findViewById(R.id.pogo_key_btn_divide_view).setVisibility(View.GONE);
            findViewById(R.id.pogo_key_btn_view).setVisibility(View.GONE);
        }
        if (FactoryKitPro.PRODUCT_SQ45S) {
            findViewById(R.id.pogo_pin_t2).setVisibility(View.GONE);
            findViewById(R.id.pogo_key).setVisibility(View.GONE);
            findViewById(R.id.pogo_key_line).setVisibility(View.GONE);
            mPogoNodeBtn.setVisibility(View.GONE);
            mChargeBtn.setVisibility(View.GONE);
            mSendDataBtn.setVisibility(View.GONE);
            pass.setVisibility(View.GONE);
            fail.setVisibility(View.GONE);
        }
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
        mKeyMind.setText(getResources().getString(R.string.pogo_key_mind) + keyCode);
        TextView keyText = null;
        switch (keyCode) {
            case 523: //KEYCODE_SCAN_4
                keyText = findViewById(R.id.pogo_key_01);
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
        // mTextView = (TextView) findViewById(R.id.otg_hint);
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
            test1 = true;
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
        // urovo yuanwei adjust youkai begin 2019.07.24
        if (FactoryKitPro.PRODUCT_SQ53 || PRODUCT_SQ47 || FactoryKitPro.PRODUCT_SQ53Z) {
            if(PRODUCT_SQ47){
            setPogoNode(POGO_NODE_SQ47, "2");
            setPogoNode(POGO_UART_READY_YOUKAI, "1");

            }
            else{
            setPogoNode(POGO_NODE_YOUKAI, "2");
            setPogoNode(POGO_KEY_READY_YOUKAI, "1");
            setPogoNode(POGO_UART_READY_YOUKAI, "1");
            setPogoNode(POGO_ID_READY_YOUKAI, "1");}
        } else if (FactoryKitPro.PRODUCT_SQ51S) {
            setPogoNode(POGO_NODE_SQ51S, "2");
        } else if (PRODUCT_SQ83) {
            setPogoNode(POGO_NODE_SQ47, "2");
        } else if (FactoryKitPro.PRODUCT_SQ45S) {
            setPogoNode(POGO_NODE_SQ45S, "1");
        // urovo huangjiezhou add begin 2021-12-01
        } else if (FactoryKitPro.PRODUCT_SQ53X) {
            setPogoNode(POGO_NODE, "1");
            setPogoNode(POGO_KEY_READY_YOUKAI, "1");
            setPogoNode(POGO_NODE_SQ53X, "1");
        // urovo huangjiezhou add end
        } else {
            setPogoNode(POGO_NODE, "1");
	    //urovo weiyu add on 2020-05-11 start
            setPogoNode(POGO_KEY_READY_YOUKAI, "1");
	    //urovo weiyu add on 2020-05-11 end
            setPogoNode(POGO_NODE_YOUKAI, "2");
        }
        // urovo yuanwei adjust youkai end 2019.07.24
        mStorageManager.registerListener(mStorageListener);
        isudiskExists();
        Log.i(TAG, "onResume isOTGStorageAvi=" + isOTGStorageAvi);
        if (isOTGStorageAvi) {
            refresh();
            quitActionTimer.start();
        }
        if(FactoryKitPro.PRODUCT_SQ45S) {
            autoTest = new SQ45sAutoTest();
            quitActionTimer.start();
        }
        // 串口操作 2019.07.16 begin
        initSerial();
        mReadThread = new ReadThread();
        mReadThread.start();
        // 串口操作 2019.07.16 end
        super.onResume();
    }

    @Override
    protected void onPause() {
        // urovo yuanwei adjust youkai begin 2019.07.24
        if (FactoryKitPro.PRODUCT_SQ53  || PRODUCT_SQ47 || FactoryKitPro.PRODUCT_SQ53Z) {
            if (PRODUCT_SQ47) {
              setPogoNode(POGO_NODE_SQ47, "0");
              setPogoNode(POGO_UART_READY_YOUKAI, "0");

            }
            else{
            setPogoNode(POGO_NODE_YOUKAI, "0");
            setPogoNode(POGO_KEY_READY_YOUKAI, "0");
            setPogoNode(POGO_UART_READY_YOUKAI, "0");
            setPogoNode(POGO_ID_READY_YOUKAI, "0");}
        } else if (FactoryKitPro.PRODUCT_SQ51S) {
            setPogoNode(POGO_NODE_SQ51S, "0");
        } else if (PRODUCT_SQ83) {
            setPogoNode(POGO_NODE_SQ47, "0");
        }  else if (FactoryKitPro.PRODUCT_SQ45S) {
            setPogoNode(POGO_NODE_SQ45S, "0");
        // urovo huangjiezhou add begin 2021-12-01
        } else if (FactoryKitPro.PRODUCT_SQ53X) {
        setPogoNode(POGO_NODE, "0");
        setPogoNode(POGO_KEY_READY_YOUKAI, "0");
        setPogoNode(POGO_NODE_SQ53X, "0");
        // urovo huangjiezhou add end
        } else {
            setPogoNode(POGO_NODE, "0");
	    //urovo weiyu add on 2020-05-11 start
            setPogoNode(POGO_KEY_READY_YOUKAI, "0");
	    //urovo weiyu add on 2020-05-11 end
            setPogoNode(POGO_NODE_YOUKAI, "0");
        }
        // urovo yuanwei adjust youkai end 2019.07.24
        mStorageManager.unregisterListener(mStorageListener);
        if (mReadThread != null) mReadThread.interrupt();// 串口操作
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try{
            if (mSerialPort.mFd != null) mSerialPort.close();
        } catch (NullPointerException e) {
            Log.i(TAG, "NullPointerException e:" + e);
        }

        if (quitActionTimer != null) {
            quitActionTimer.cancel();
            quitActionTimer = null;
        }
    }

    private final int QUIT_DELAY_TIME = 1000;

    CountDownTimer quitActionTimer = new CountDownTimer(4 * QUIT_DELAY_TIME, QUIT_DELAY_TIME) {

        @Override
        public void onTick(long arg0) {
            if(autoTest != null)
                autoTest.start(test_SQ45S--);
        }

        @Override
        public void onFinish() {
            if(test1 && test2 && test3 && test4)
                pass();
            else
                fail(null);
        }
    };
    private interface AutoTest {
        public void start(int test);
    }
    private class SQ45sAutoTest implements AutoTest{

        @Override
        public void start(int test) {
            logd("test:" + test);
            if(test == 3){
                refresh();
            }else if(test == 2){
                try {
                    // 向串口发送数据
                    if (mOutputStream != null) mOutputStream.write(sendData.getBytes());
                    if(mTextSeialPort != null) {
                        mTextSeialPort.setText(getResources().getString(R.string.pogo_serila_send_txt_mind));
                        mTextSeialPort.setTextColor(Color.RED);
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }else if(test == 1){
                batteryCharing();
            }else if(test == 0){
                if(mTextPogoNode != null) {
                    String id = getPogoNode(POGO_ID_TEST_NODE_SQ45S);
                    mTextPogoNode.setText(id + " " + (id.equals("0") ? "pass" : "fail"));
                    mTextPogoNode.setTextColor(id.equals("0") ? Color.GREEN : Color.RED);
                    if(id.equals("0")) test4 = true;
                }
            } else {

            }
        }
    }

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
