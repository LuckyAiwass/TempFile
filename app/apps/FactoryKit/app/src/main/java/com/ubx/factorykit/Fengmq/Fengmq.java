package com.ubx.factorykit.Fengmq;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.device.MaxNative;
import android.os.Bundle;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;


public class Fengmq extends Activity {

    private String TAG = "Fengmq";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        // TODO Auto-generated method stub
        //Toast.makeText(Fengmq.this, "Not implemented", Toast.LENGTH_SHORT).show();
        //fail(null);
        setBeeper(3, 0, 500);
        showDialog();
    }

    private void showDialog() {

        new AlertDialog.Builder(Fengmq.this).setMessage(R.string.fengmq_confirm)
                .setPositiveButton(R.string.yes, passListener).setNegativeButton(R.string.no, failListener).show();
    }

    OnClickListener passListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            pass();
        }
    };

    OnClickListener failListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            fail(null);
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

    public int setBeeper(int cnts, int freq, int time) {
        byte[] ResponseData = new byte[32];
        byte[] ResLen = new byte[1];
        return MaxNative.setBeeper(cnts, freq, time, ResponseData, ResLen);
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   