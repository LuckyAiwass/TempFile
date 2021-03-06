/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.Safe;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Exception;
import android.widget.Toast;
import android.content.Intent;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class Safe extends Activity {

    private static final String TAG = "Safe";
    private static String resultString = Utilities.RESULT_FAIL;
    private static Context mContext;

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

        setContentView(R.layout.safe);
    }

    public static boolean writeFile(String filePath, String content) {
        boolean res = true;
        File file = new File(filePath);
        File dir = new File(file.getParent());
        if (!dir.exists())
            dir.mkdirs();
        try {
            FileWriter mFileWriter = new FileWriter(file, false);
            mFileWriter.write(content);
            mFileWriter.close();
        } catch (IOException e) {
            res = false;
        }
        Log.d("zml", "writeFile   " +filePath  + "   value   " +content);
        return res;
    }

    /**
     * urovo yuanwei hide Recent button
     */
    private void hideRecentBtn() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.STATUS_BAR_DISABLE_RECENT;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        init(getApplicationContext());
        
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
        try {

            /*String[] sourceArray = functionItem.packageName.split("/");
            String pkg = sourceArray[0];
            String className = sourceArray[1];
            if(!className.contains(pkg)){
                className = pkg+className;
            }*/

            Intent intent = new Intent();
            intent.setClassName("com.dfxh.wang.serialport_test","com.dfxh.wang.serialport_test.MainActivity");
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this,R.string.toast_no_apk,Toast.LENGTH_SHORT).show();
            fail(null);
            e.printStackTrace();
        }
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

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                    