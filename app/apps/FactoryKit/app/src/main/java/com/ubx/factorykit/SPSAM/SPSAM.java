package com.ubx.factorykit.SPSAM;

import android.content.Context;
import android.os.Handler;
import android.os.Bundle;

import com.meigsmart.meigrs32.util.SerialPort;
import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import android.app.Activity;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;

import android.widget.LinearLayout;

public class SPSAM extends Activity {
    String TAG = "SPSAM";
    String resultString = Utilities.RESULT_FAIL;
    final String MEIGE_PSAM_NODE = "/sys/devices/platform/soc/soc:meig-psam/psam_enable";
    private LinearLayout mContainer;
    private boolean mStatus = false;
    private SerialPort mPsamSerialPort;
    private static Context mContext;
    static final int TEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.battery);
        mContext = getApplicationContext();
        init(mContext);
        mContainer = findViewById(R.id.battery_layout);
        addTextView();
        setNode(MEIGE_PSAM_NODE, "1");
    }

    public void addTextView() {
        TextView child = new TextView(mContext);
        child.setTextSize(20);
        child.setText(getString(R.string.testing));
        // 调用一个参数的addView方法
        mContainer.addView(child);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mHandler != null){
            mHandler.removeMessages(TEST);
            mHandler.sendEmptyMessageDelayed(TEST, 500);
        }
    }

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TEST:
                    test();
                    break;
            }
        }
    };

    private void test() {
        int[] input = {98, 0, 0, 0, 0, 0, 2, 0, 0, 0, 96};
        int[] output = {0};
        try {
            mPsamSerialPort = new SerialPort();
            mPsamSerialPort.test("/dev/ttyMSM1", 38400, input, output);
            mStatus = mPsamSerialPort.isStatus();
            logd("PSAM mStatus = " + mStatus);
            toast(mPsamSerialPort.getPsam_atr() + " " + (mStatus ? getString(R.string.pass) : getString(R.string.fail)));
        } catch (Exception e) {
            loge(e);
            e.printStackTrace();
        } finally {
            if (mStatus) {
                pass();
            }
            else {
                fail();
            }
            setNode(MEIGE_PSAM_NODE, "0");
            finish();
        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }


    @Override
    public void finish() {
        //Utilities.writeCurMessage(this, TAG, resultString);

        super.finish();
    }

    private void init(Context context)
    {
        setResult(RESULT_CANCELED);
        resultString = Utilities.RESULT_FAIL;
    }

    /**
     * 获取节点状态
     * @param node
     * @return
     */
    private String getNode(String node) {
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
        Log.i(TAG, "getNode =" + new String(buffer).trim());
        return new String(buffer).trim();
    }


    private void setNode(String patch, String value) {
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


    void fail() {
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(TAG, "Failed");
    }

    void pass() {
        setResult(RESULT_OK);
        Utilities.writeCurMessage(TAG, "Pass");
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
