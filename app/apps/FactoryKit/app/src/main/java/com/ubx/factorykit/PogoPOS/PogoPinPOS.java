package com.ubx.factorykit.PogoPOS;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.EthernetManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class PogoPinPOS extends Activity {

    String TAG = "PogoPin";
    final String VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";
    final String STATUS = "/sys/class/power_supply/battery/status";
    TextView mTextCharge, mTextDock;
    private ReadThread mReadThread;
    private EthernetManager mEthernetManager;

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                onDataReceived();
                getChargingSta();
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    protected void onDataReceived() {
        runOnUiThread(new Runnable() {
            public void run() {
                syscEthInfo();
                Log.d(TAG, "onDataReceived = " + syscEthInfo());
                if(!syscEthInfo().equals("")){
                    mTextDock.setText(getResources().getString(R.string.pogo_dock_result_txt) + syscEthInfo());
                    mTextDock.setTextColor(Color.GREEN);
                }else{
                    mTextDock.setText(getResources().getString(R.string.pogo_dock_result_txt) + "reading ...");
                    mTextDock.setTextColor(Color.RED);
                }
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.pogo_pos);
        mEthernetManager = (EthernetManager) getSystemService(Context.ETHERNET_SERVICE);
        mTextCharge = (TextView) findViewById(R.id.pogo_charge_result);
        Button mChargeBtn = (Button) findViewById(R.id.pogo_charge_btn);
        mChargeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getChargingSta();
            }
        });

        mTextDock = (TextView) findViewById(R.id.pogo_dock_hint);

        Button pass = (Button) findViewById(R.id.pass);
        pass.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                pass();
            }
        });
        Button fail = (Button) findViewById(R.id.fail);
        fail.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                fail(null);
            }
        });

    }

    /**
     * 获取充电状态
     */
    public void getChargingSta(){
        runOnUiThread(new Runnable() {
            public void run() {
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
                mTextCharge.setText(result);
                mTextCharge.setTextColor(ret ? Color.GREEN : Color.RED);
            }
        });

    }

    /**
     * 获取MAC地址获取MAC地址
     * @return
     */
    public String syscEthInfo() {

        String mDockMac = "";
        try {
            mDockMac = loadFileAsString("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
        } catch (Exception e) {
            Log.e("DockerSettings", "read eth0 mac addr error");
        }
        if(TextUtils.isDigitsOnly(mDockMac)) mDockMac = "";
        return mDockMac;
    }

    public static String loadFileAsString(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    /**
     * 充电状态
     *
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

    @Override
    protected void onResume() {
        mReadThread = new ReadThread();
        mReadThread.start();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mReadThread != null) mReadThread.interrupt();
        // 关闭以太网
        /*if (mEthernetManager != null && mEthernetManager.isAvailable()) {
            mEthernetManager.stop();
            mEthernetManager.setEnabled(false);
        }*/
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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


    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
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