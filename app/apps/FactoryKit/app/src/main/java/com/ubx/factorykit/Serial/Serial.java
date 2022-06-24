package com.ubx.factorykit.Serial;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.serialport.SerialPort;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;


public class Serial extends Activity implements OnClickListener {

    private String TAG = "Serial";
    private SerialPort mSerialPort;
    protected String pathName;
    protected int speed = -1;
    private String sendData = "123456";
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadDataThread mReadDataThread;
    private TextView mTextSeialPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.peripheral);

        mTextSeialPort = findViewById(R.id.tv_peripheral_serial_hint);
        mTextSeialPort.setText("Plug in serial wire and then click send button...");
        mTextSeialPort.setTextColor(Color.RED);

        Button mSendDataBtn = findViewById(R.id.btn_peripheral_serial);
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
        Button pass = findViewById(R.id.pass);
        pass.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pass();
            }
        });

        Button fail = findViewById(R.id.fail);
        fail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                fail();
            }
        });
        initSerial();
        mReadDataThread = new ReadDataThread();
        mReadDataThread.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReadDataThread != null) mReadDataThread.interrupt();
    }

    @Override
    protected void onDestroy() {
        try {
            if (mOutputStream != null) mOutputStream.close();
            if (mInputStream != null) mInputStream.close();
            if (mSerialPort != null) mSerialPort.close();
        } catch (IOException e) {
        }
        super.onDestroy();
    }

    private class ReadDataThread extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size = 0;
                try {
                    byte[] buffer = new byte[256];

                    if (mInputStream != null)
                        size = mInputStream.read(buffer);

                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException: ");
                    e.printStackTrace();
                    //return;
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
                    mTextSeialPort.setText("onDataReceived = " + recv+"\n");
                    mTextSeialPort.setTextColor(Color.GREEN);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

    }

    public void initSerial() {
        try {
            pathName = "/dev/ttyUSB0";
            for (int i = 0;i < 10;i++){
                File file = new File("/dev/ttyUSB"+String.valueOf(i));
                if (file.exists()){
                    pathName = "/dev/ttyUSB"+String.valueOf(i);
                    Log.d(TAG,pathName + " is exist");
                    break;
                }
            }
            Log.d(TAG,"open pathName = "+pathName);
            if (speed == -1) speed = 9600; //115200
            mSerialPort = new SerialPort(new File(pathName), speed, 0);
            mOutputStream = mSerialPort.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
        } catch (Exception e) {
           e.printStackTrace();
        }
    }

    public void fail() {
        //loge(msg);
        //toast(msg);
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG, "Failed");
        finish();
    }

    public void pass() {
        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG, "Pass");
        finish();
    }

}
