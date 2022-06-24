/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.factorykit.TCard;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
//import android.os.storage.IMountService;
import android.os.ServiceManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class TCard extends Activity {

    TextView mTextView;
    String TAG = "TCard";
    private static final int VOLUME_SDCARD_INDEX = 1;

    @Override
    public void finish() {

        super.finish();
    }

    @Override
    protected void onPause() {

        unregisterReceiver(mReceiver);
        super.onPause();

    }
	public String getSDPath(Context mcon) {
		String sd = null;
		StorageManager mStorageManager = (StorageManager) mcon
				.getSystemService(Context.STORAGE_SERVICE);
		StorageVolume[] volumes = mStorageManager.getVolumeList();
		StorageVolume mVolume = (volumes.length > VOLUME_SDCARD_INDEX) ?
		volumes[VOLUME_SDCARD_INDEX] : null;
		if(mVolume != null) {
                        
			sd = volumes[VOLUME_SDCARD_INDEX].getPath();
                        android.util.Log.d(TAG,"sd" + sd);
			return sd;
		}
		/*
		for (int i = 0; i < volumes.length; i++) {
			Const.vLog("volumes-- >"+volumes[i].isRemovable());
			Const.vLog("volumes-- >"+volumes[i].allowMassStorage());
			Const.vLog("volumes-- getPath >"+volumes[i].getPath());
			if (volumes[i].isRemovable() && volumes[i].allowMassStorage()) {
				sd = volumes[i].getPath();
				Const.vLog("sd-- >"+sd);
				return sd;
			}
		}*/
                android.util.Log.d(TAG,"sd" + sd);
		return sd;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.sdcard);

        mTextView = (TextView) findViewById(R.id.sdcard_hint);
        mTextView.setText(getString(R.string.tcard_wait));
/*
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            exec("mount");
        } else
            showConfirmDialog(this);
*/	
//	IMountService mountService = IMountService.Stub.asInterface(ServiceManager.getService("mount"));
	StorageManager mStorageManager = (StorageManager)getSystemService(Context.STORAGE_SERVICE);
        android.util.Log.d(TAG,"getVolumeList start");
	try{
	  int iskid = mStorageManager.getVolumeList().length;
          android.util.Log.d(TAG,"getVolumeList start");
	  Log.e("iskid","---------------------------------" + iskid + "-------------" + mStorageManager.getVolumeState(getSDPath(this)));
	  if(mStorageManager.getVolumeState(getSDPath(this)).equals(Environment.MEDIA_MOUNTED))
		exec("mount");
	  else
		showConfirmDialog(this);	
	}catch(Exception e){
                e.printStackTrace();
		showConfirmDialog(this);
	}
    }

    Handler mHandler = new Handler() {
        public void dispatchMessage(android.os.Message msg) {
            boolean res = (Boolean) msg.obj;
            if (res)
                pass();
            else
                fail(null);
        };
    };

    void exec(final String para) {

        new Thread() {

            public void run() {
                try {
                    logd(para);

                    Process mProcess;
                    String paras[] = para.split(",");
                    for (int i = 0; i < paras.length; i++)
                        logd(i + ":" + paras[i]);
                    mProcess = Runtime.getRuntime().exec(paras);
                    mProcess.waitFor();

                    InputStream inStream = mProcess.getInputStream();
                    InputStreamReader inReader = new InputStreamReader(inStream);
                    BufferedReader inBuffer = new BufferedReader(inReader);
                    String s;
                    String data = "";
                    while ((s = inBuffer.readLine()) != null) {
                        data += s + "\n";
                    }
                    logd(data);
                    int result = mProcess.exitValue();
                    logd("ExitValue=" + result);
                    Message message = new Message();
                    if (data.contains(getSDPath(TCard.this)))
                        message.obj = true;
                    else
                        message.obj = false;
                    message.setTarget(mHandler);
                    message.sendToTarget();

                } catch (Exception e) {
                    logd(e);
                }

            }
        }.start();

    }

    @Override
    protected void onResume() {

        IntentFilter mfilter = new IntentFilter();
        mfilter.addAction("android.intent.action.MEDIA_MOUNTED");
        mfilter.addDataScheme("file");
        registerReceiver(mReceiver, mfilter);
        super.onResume();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            logd(intent.getAction());
            if (intent.getAction().equals("android.intent.action.MEDIA_MOUNTED"))
                exec("mount");

        }

    };

    void showConfirmDialog(final Context context) {

        new AlertDialog.Builder(context).setTitle(getString(R.string.tcard_confirm))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        fail(null);
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        toast(getString(R.string.tcard_to_insert));
                    }
                }).show();
    }

    void fail(Object msg) {

        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG,"Failed");
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG,"Pass");
        finish();
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
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

    @SuppressWarnings("unused")
    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }
}
