/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.Pen;

import java.util.HashMap;
import java.util.Map;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.content.IntentFilter;
import android.content.Intent;
import android.content.BroadcastReceiver;

import java.util.HashMap;

import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;
import com.ubx.factorykit.Values;
import com.ubx.factorykit.Framework.MainApp;

public class Pen extends Activity {

    private static final String TAG = "Pen";
    private static String resultString = Utilities.RESULT_FAIL;
    private TextView pen_in;
    private TextView pen_out;
    private boolean out = false;
    private boolean in = false;
    private BroadcastReceiver receiver =new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
		int state = intent.getIntExtra("state", 1);
		if (state == 32){
			out = true;
			pen_out.setBackgroundResource(R.color.green);
		}else if(state == 0){
			in = true;
			pen_in.setBackgroundResource(R.color.green);
		}
		if(in && out){
			pass();
		}
	}
    };


    @Override
    public void finish() {
        Utilities.writeCurMessage(this, TAG, resultString);
        super.finish();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
	setContentView(R.layout.pen);
        setRequestedOrientation(Framework.orientation);
        IntentFilter  filter = new IntentFilter();  
        filter.addAction("android.intent.action.STYLUE_PLUG");  
        registerReceiver(receiver, filter);  
	pen_in = (TextView) findViewById(R.id.pen_in);
	pen_out = (TextView) findViewById(R.id.pen_out);

        Button pass = (Button) findViewById(R.id.pass);
        pass.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                pass();
            }
        });

        Button fail = (Button) findViewById(R.id.fail);
        fail.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                fail(null);
            }
        });
    }

    @Override
    public void onDestroy() {
	unregisterReceiver(receiver);
	super.onDestroy();
    }

    private void unregisterReceiver(){
	this.unregisterReceiver(receiver);
    }

    void fail(Object msg) {

        loge(msg);
        setResult(RESULT_CANCELED);
        resultString = Utilities.RESULT_FAIL;
	in = false;
	out = false;
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        resultString = Utilities.RESULT_PASS;
	in = false;
	out = false;
        finish();
    }

    void logd(Object d) {

        Log.d(TAG, "" + d);
    }

    void loge(Object e) {

        Log.e(TAG, "" + e);
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   