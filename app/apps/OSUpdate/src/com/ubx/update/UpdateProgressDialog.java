/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ubx.update;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.util.Preconditions;

import java.io.File;
import java.io.IOException;
import android.os.SystemProperties;
import android.device.DeviceManager;

/** Display update state and progress. */
public class UpdateProgressDialog extends Activity {
    public static final String EXTRA_RESUME_UPDATE = "resume_update";

    private static final String TAG = "UpdateProgressDialog";
    private static final String EXTRA_UPDATE_FILE = "extra_update_file";

    private ProgressDialog progressDialog;
    private boolean isSilence = false;
    private boolean isForceUpdate = false;
    private DeviceManager mDeviceManager;
    private final CarUpdateProgressCallback mCarUpdateProgressCallback = new CarUpdateProgressCallback();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String file = getIntent().getStringExtra(EXTRA_UPDATE_FILE);
	isSilence = getIntent().getBooleanExtra("isSilence",false);
        isForceUpdate = getIntent().getBooleanExtra("force",false);
        mDeviceManager = new DeviceManager();

        Log.e(TAG,"file >>> "+file);
        if(file == null || file.equals("")){
            finishUpdate();
            return;
        }
        if (isSilence) {
            UpdateManager.getDefault(this).doUpdate(file,null);
            UpdateProgressDialog.this.finish();
            return;
        } else {
            progressDialog = new ProgressDialog(this);
            showDialog();
            UpdateManager.getDefault(this).doUpdate(file,mCarUpdateProgressCallback);
        }
    }

    private void finishUpdate() {
	if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
        if ("1".equals(UpdateUtil.readOemPartition(UpdateUtil.TAG_IS_UPGRADE))) {
            UpdateUtil.pushUpgradeResultToServer(UpdateProgressDialog.this, UpdateUtil.OTHER);
        }
	}
	if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        SystemProperties.set("persist.sys.update.silence"," "); //clear the flag of silence update
        if (isForceUpdate) {
            UpdateUtil.handleForceupgradingView(true); //unlock the statusbar and back button,home button,appswitch button
            ForceUpdateManager.getInstance().setforceUpdate(false);
        }
        UpdateProgressDialog.this.finish();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /** Update the status information. */
    private void showStatus(int status) {
        Toast.makeText(this,status,Toast.LENGTH_LONG).show();
    }

    private void showDialog(){
        progressDialog.setMessage(getResources().getString(R.string.updating));
        progressDialog.setTitle(getResources().getString(R.string.update_progress));
        //progressDialog.setIcon(R.drawable.ic_launcher_background);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        //设置进度条样式
        //ProgressDialog.STYLE_SPINNER 环形精度条
        //ProgressDialog.STYLE_HORIZONTAL 水平样式的进度条
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
    }

    public class CarUpdateProgressCallback extends UpdateProgressCallback {
        @Override
        public void onProgressUpdate(int progress) {
            Log.d(TAG,"CarUpdateProgressCallback::onProgressUpdate---->"+progress);
            progressDialog.setProgress(progress);
        }

        @Override
        public void onUpdateComplete(int errorCode) {
            Log.d(TAG,"CarUpdateProgressCallback::onUpdateComplete---->"+errorCode);
            if (errorCode == UpdateManager.UpdateStatusCode.UPDATE_SUCCESS) {
                showStatus(R.string.install_success);
                finishUpdate();
                Log.d(TAG,"CarUpdateProgressCallback::onUpdateComplete--->update success!");
            } else {
                showStatus(R.string.install_failed);
                finishUpdate();
                Log.d(TAG,"CarUpdateProgressCallback::onUpdateComplete--->update failed!");
            }
        }
    }
}
