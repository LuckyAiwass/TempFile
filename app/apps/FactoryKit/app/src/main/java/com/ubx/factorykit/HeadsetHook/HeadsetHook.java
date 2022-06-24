/*
 * Copyright (c) 2011-2012, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.factorykit.HeadsetHook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class HeadsetHook extends Activity {

    private static final String TAG = "HeadsetHook";
    private static String resultString = Utilities.RESULT_FAIL;
    private static Context mContext;
    AudioManager mAudioManager;
    private final int[] KEYMODE0 = { KeyEvent.KEYCODE_HEADSETHOOK};
    private final int[] KEYMODE1 = { KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.KEYCODE_MEDIA_PREVIOUS };
    private final int[] KEYMODE2 = { KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE};
    private final int[][] KEYMODE = { KEYMODE0, KEYMODE1, KEYMODE2};
    int[] keyMode;
    HashMap<Integer, Boolean> keyStatusHashMap = new HashMap<Integer, Boolean>();

    @Override
    public void finish() {
        Utilities.writeCurMessage(this, TAG, resultString);
        super.finish();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    private void init(Context context) {
        mContext = context;
        resultString = Utilities.RESULT_FAIL;
    }

    void showWarningDialog(String title) {

        new AlertDialog.Builder(mContext).setTitle(title).setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(Framework.orientation);
        init(this);
        setContentView(R.layout.headsethook);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (!mAudioManager.isWiredHeadsetOn())
            showWarningDialog(getString(R.string.insert_headset));
	
	    if(FactoryKitPro.PRODUCT_SQ52M || FactoryKitPro.PRODUCT_SQ53X || FactoryKitPro.PRODUCT_SQ45C)
	        keyMode = KEYMODE[2];
	    else
            keyMode = KEYMODE[0];
        for (int i = 0; i < keyMode.length; i++) {
            keyStatusHashMap.put(keyMode[i], false);
        }

        TextView headsethookView = (TextView) findViewById(R.id.headsethook);

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

    void fail(Object msg) {

        loge(msg);
        setResult(RESULT_CANCELED);
        resultString = Utilities.RESULT_FAIL;
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        resultString = Utilities.RESULT_PASS;
        finish();
    }

    void logd(Object d) {

        Log.d(TAG, "" + d);
    }

    void loge(Object e) {

        Log.e(TAG, "" + e);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        TextView keyText = null;
        keyStatusHashMap.put(keyCode, true);
        switch (keyCode) {
            case KeyEvent.KEYCODE_HEADSETHOOK:
                keyText = (TextView) findViewById(R.id.headsethook);
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                keyText = (TextView) findViewById(R.id.headsethook);
                break;
        }

        if (null != keyText) {
            keyText.setBackgroundResource(R.color.green);
        }
        
	    if (allKeyPassed())
            pass();

        return true;
    }

    private boolean allKeyPassed() {
        for (int i = 0; i < keyMode.length; i++) {
            if (!keyStatusHashMap.get(keyMode[i]))
                return false;
        }
        return true;
    }

}
