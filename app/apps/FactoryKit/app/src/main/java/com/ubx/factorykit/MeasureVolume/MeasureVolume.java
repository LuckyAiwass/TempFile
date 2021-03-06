/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.ubx.factorykit.MeasureVolume;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.Framework.MainApp;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import android.os.AsyncResult;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.qualcomm.qcrilhook.QcRilHook;
import com.qualcomm.qcrilhook.IQcRilHook;

public class MeasureVolume extends Activity {

    String TAG = "MeasureVolume";
    String resultString = Utilities.RESULT_PASS;

    private final static float L = 56.5f;
    private final static float W = 47.0f;
    private final static float H = 26.0f;

    private final static float L1 = 21.0f;
    private final static float W1 = 10.3f;
    private final static float H1 = 10.3f;

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void finish() {

        Utilities.writeCurMessage(this, TAG, resultString);
        super.finish();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setRequestedOrientation(Framework.orientation);
        try {
            Intent intent = new Intent();
            intent.setClassName("com.urovo.cd.measurevolume", "com.urovo.cd.measurevolume.MainActivity");
            startActivityForResult(intent, 1);
        }catch (Exception e){
            fail();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RESULT_OK，判断另外一个activity已经结束数据输入功能，Standard activity result:
        // operation succeeded. 默认值是-1
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                double length = data.getDoubleExtra("LENGTH", 0);
                double width= data.getDoubleExtra("WIDTH",0);
                double height = data.getDoubleExtra("HEIGHT",0);
                Log.d(TAG, "LENGTH:"+length+ "  WIDTH:"+width+ "    HEIGTH:"+height);
                double volume = length*width*height;
                if(volume > L*W*H*0.95 && volume < L*W*H*1.05) {
                    pass();
                }else if(volume > L1*W1*H1*0.8 && volume < L1*W1*H1*1.2){
                    pass();
                } 
            }
        }

        fail();
    }

    void fail() {
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG, "Failed");
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG, "Pass");
        finish();
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               