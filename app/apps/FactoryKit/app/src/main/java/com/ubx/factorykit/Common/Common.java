/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.Common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Exception;
import java.util.HashMap;
import java.util.List;

import android.widget.Toast;
import android.content.Intent;

import com.ubx.factorykit.Framework.MainApp;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;
import com.ubx.factorykit.Values;

public class Common extends Activity {

    private static  String TAG = "Common";
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
            //解析获得title
            Bundle bundle = getIntent().getExtras();   //得到传过来的bundle
            int postion = bundle.getInt(Values.KEY_SERVICE_INDEX,-1);
            String title = (String) MainApp.getInstance().mItemList.get(postion).get("title");
            TAG = title;
            setTitle(title);
            //解析获取包名
            PackageManager packageManager = getPackageManager();
            HashMap<String, String> params = (HashMap<String, String>) MainApp.getInstance().mItemList.get(postion).get("parameter");
            Intent intent = new Intent();
            String pkg = params.get("pkg");
            String className = params.get("class");
            if(TextUtils.isEmpty(className)){
                Intent tIntent = new Intent(Intent.ACTION_MAIN, null);
                tIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                tIntent.setPackage(pkg);
                /** Retrieve all activities that can be performed for the given intent */
                List<ResolveInfo> thirdList = packageManager.queryIntentActivities(tIntent, 0);
                ResolveInfo ri = thirdList.iterator().next();

                if (ri != null) {
                    intent.setClassName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name);
                }
            }else {
                if(!className.contains(pkg)){
                    className = pkg+className;
                }
                intent.setClassName(pkg, className);
            }
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
