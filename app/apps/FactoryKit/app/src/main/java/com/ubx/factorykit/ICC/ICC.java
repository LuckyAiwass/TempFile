
package com.ubx.factorykit.ICC;

import android.device.IccManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class ICC extends Activity {
    private EditText mNo;
    private Button mSend;
    private Button mDefApdu;
    private Button mReset;
    private Button mSetApduTimeout;
    private Button mGetRsp;
    private Button mSetETU;
    private Button mStopApduRspRecv;
    private Button mDetect;
    private Button mInitIC;
    private Button mInitSle4442;

    EditText mEmission;
    private IccManager mIccManager;
    IccReaderThread mIccReadeThreadr;
    private static String resultString = Utilities.RESULT_FAIL;
    private static String TAG = "ICC";
    byte[] apdu_utf = {
            0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53,
            0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.icc);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mNo = (EditText) findViewById(R.id.editText1);
        mEmission = (EditText) findViewById(R.id.emission);
        mSend = (Button) findViewById(R.id.button1);
        mSend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String reception = mEmission.getText().toString();
                android.util.Log.i("debug", "onEditorAction:" + reception);

                if (!reception.equals("")) {
                    if (Convert.isHexAnd16Byte(reception, ICC.this)) {
                        mNo.append("SEND: " + reception + "\n");
                        byte[] apdu = Convert.hexStringToByteArray(reception);
                        sendCmd(apdu, 2);
                    }
                } else {
                    Toast.makeText(ICC.this, "please input content", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mDefApdu = (Button) findViewById(R.id.button2);
        mDefApdu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCmd(apdu_utf, 3);
            }
        });

        mReset = (Button) findViewById(R.id.button3);
        mReset.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /*byte[] atr = new byte[64];
                int retLen = mIccManager.activate(atr);
                // 6.print the atr
                if (retLen == -1) {
                    mNo.append(getString(R.string.icc_reset_fail) + "\n");
                } else {
                    mNo.append("ATR: " + Convert.bytesToHexString(atr, 0, retLen) + "\n");
                }*/
                int ret = mIccManager.deactivate();
                int ret1 = mIccManager.close();
                if (ret == 0 && ret1 == 0) {
                    mNo.append(" Deactivation success" + "\n");
                }else {
                    mNo.append(" Deactivation failed" + "\n");
                }
            }
        });

        mDetect = (Button) findViewById(R.id.btn_detect);
        mDetect.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /*int status = mIccManager.detect();
                if (status != 0) {
                    mNo.append(getString(R.string.icc_hint) + "\n");
                }*/
                byte[] atr = new byte[64];
                int retLen = mIccManager.activate(atr);
                // 6.print the atr
                if (retLen == -1) {
                    mNo.append(" IC Card reset faile......." + "\n");
                } else {
                    mNo.append("ATR: " + Convert.bytesToHexString(atr, 0, retLen) + "\n");
                }
                
            }
        });

        mInitIC = (Button) findViewById(R.id.btn_init_ic);
        mInitIC.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int ret = mIccManager.open((byte) 0, (byte) 0x01, (byte) 0x01);
                if (ret != 0) {
                    mNo.append(" Activation success " + "\n");
                }else {
                    mNo.append(" Activation success " + "\n");
                }

            }
        });

    }

    public void onPass(View view) {
        setResult(RESULT_OK);
        resultString = Utilities.RESULT_PASS;
        finish();
    }

    public void onFail(View view) {
        setResult(RESULT_CANCELED);
        resultString = Utilities.RESULT_FAIL;
        finish();
    }

    private void sendCmd(byte[] cmd, int type) {

        int apdu_count = (type == 2) ? cmd.length : apdu_utf.length;
        byte[] rspBuf = new byte[256];
        byte[] rspStatus = new byte[2];
        int retLen = mIccManager.apduTransmit((type == 2) ? cmd : apdu_utf, (char) apdu_count, rspBuf, rspStatus);
        if (retLen == -1) {
            return;
        }
        mNo.append("APDU RSP REVC: " + Convert.bytesToHexString(rspBuf, 0, retLen) + "\n");
        mNo.append("APDU RSP REVC Status : " + Convert.bytesToHexString(rspStatus, 0, 2) + "\n");

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //if(mIccReadeThreadr != null)
        //    mIccReadeThreadr.threadrun = false;
        if (mIccManager != null) {
            int ret = mIccManager.deactivate();
            android.util.Log.i("mIccReader", "-----------Eject-----retr=" + ret);
            mIccManager.close();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mIccManager = new IccManager();
        //int status = mIccReader.SelectSlot((byte)1);
        //mIccReadeThreadr = new IccReaderThread("ReaderThread");
        //mIccReadeThreadr.start();
    }

    private boolean onDataReceivedF(final byte[] content, final String type) {
        Log.i("mIccManager", "-------onDataReceivedF---------------");
        runOnUiThread(new Runnable() {
            public void run() {
                if (content != null) {
                    //mNo.setText("");

                    String contenta = Convert.bytesToHexString(content);
                    if (contenta != null) {
                        int len = contenta.length();
                        mNo.append(type);
                        mNo.append(contenta);
                        mNo.append("\n");
                    }


                }
            }
        });
        return true;
    }

    private class IccReaderThread extends Thread {
        private boolean threadrun = true;

        public IccReaderThread(String name) {
            super(name);

        }

        public void run() {

            while (threadrun) {
                Log.i("mIccManager", "-------run---------------");
                try {
                    sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*boolean ret = mIccReader.IccOpen();
                if (!ret) {
                    continue;
                }

                byte[] retb = mIccManager.IccFound();
                if (retb == null && retb.length <= 0) {
                    continue;
                }
                for (int i = 0; i < 4; i++) {
                    android.util.Log.i("mIccManager", "---------------------------------retb=["
                            + retb[i] + "]");
                }
                // 3.solt one has card?
                if (retb[0] != 1) {
                    continue;
                }*/
                // 4.select solt one
                int status = mIccManager.IccSelect((char) 1);
                if (status != 0) {
                    continue;
                }
                status = mIccManager.detect();
                if (status != 0) {
                    continue;
                }
                byte[] atr = new byte[64];
                int ret = mIccManager.reset(atr);
                // 6.print the atr
                if (ret == -1) {
                    continue;
                }
                for (int i = 0; i < atr.length && atr.length < 33; i++) {
                    android.util.Log.i("mIccManager", "----------------atr=[" + atr[i] + "]");
                }
                onDataReceivedF(atr, "ATR: ");

                byte[] apdu_utf = {
                        0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x31, 0x50, 0x41, 0x59, 0x2E, 0x53,
                        0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00
                };
                int apdu_count = apdu_utf.length;
                byte[] rspBuf = new byte[256];
                byte[] rspStatus = new byte[1];
                int retr = mIccManager.apduTransmit(apdu_utf, (char) apdu_count, rspBuf, rspStatus);
                if (retr == -1) {
                    continue;
                }
                for (int i = 0; i < retr; i++) {
                    android.util.Log.i("mIccReader", "----------------retr=[" + rspBuf[i] + "]");
                }
                onDataReceivedF(rspBuf, "APDU Out: ");
                //mIccManager.IccClose();
            }

        }
    }

}
