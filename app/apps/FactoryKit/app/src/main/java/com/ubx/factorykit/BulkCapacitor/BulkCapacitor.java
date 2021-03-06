/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.BulkCapacitor;

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
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import java.util.Timer;
import java.util.TimerTask;

public class BulkCapacitor extends Activity {
    
    static String TAG = "BulkCapacitor";
    Button passButton, failButton;

    private String mbTempValue = "";



    Context mContext = null;


    @Override
    public void finish() {
        super.finish();
    }

    void bindView() {
        passButton = (Button) findViewById(R.id.pass);
        failButton = (Button) findViewById(R.id.fail);
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
        setContentView(R.layout.bulkcapacitor);
        bindView();
    }


    void setButtonClickable(boolean cmd) {
        passButton.setClickable(cmd);
        passButton.setFocusable(cmd);
        failButton.setClickable(cmd);
        failButton.setFocusable(cmd);
    }
    
    @Override
    protected void onDestroy() {
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
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    