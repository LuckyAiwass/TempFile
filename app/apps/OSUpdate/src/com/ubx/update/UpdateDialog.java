/*
 * Copyright (c) 2011-2013 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.ubx.update;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RecoverySystem;
import android.os.storage.StorageManager;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import android.util.Log;
import android.widget.Toast;
import android.app.ActivityManager;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.AsyncTask;

public class UpdateDialog extends Activity implements RecoverySystem.ProgressListener,
        OnClickListener, OnCancelListener {

    private static final int DIALOG_CHOOSE = 1;

    private static final int INDEX_REMOTE = 0;

    private static final int INDEX_LOCAL = 1;

    private static final int REQUEST_CODE_FILE = 1;

    private static final String UPDATE_FILE = "update.zip";

    private static final String factory_update_file = "/cache/uro/data/content.txt";

    private ProgressDialog pDialog;

    private ProgressDialog verifyPackageDialog;

    private Handler handler;

    private static final String TAG = "UpdateDialog";
 
    public static final String SELECTOR_REQUEST_CODE_KEY = "selector_request_code_key";

    public static final String SELECTOR_BUNDLE_PATHS = "selector_bundle_paths";

    public static final Integer SELECTOR_MODE_FILE = 100;

    private InactivityTimer inactivityTimer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityManager.isUserAMonkey()) {
            finish();
        } else {
            this.showDialog(DIALOG_CHOOSE);
        }
	android.util.Log.d("lincong","111");
	android.os.SystemProperties.set("persist.sys.update.silence.asdfa","silence_immedate");
	android.util.Log.d("lincong","222");
        inactivityTimer = new InactivityTimer(this, 60 * 1000);
        verifyPackageDialog = new ProgressDialog(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        inactivityTimer.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void doUpdate() {
        pDialog = ProgressDialog.show(this, getText(R.string.dialog_update_title),
                getText(R.string.dialog_update_msg));
        handler = new Handler();

        new Thread(new Runnable() {

            private boolean isGetPkg = false;

            private void searchFile(File filepath) {
                Log.i(TAG, ">>>>>>>>>>>>>>" + filepath.getAbsolutePath());
                File[] files = filepath.listFiles();
                if (files.length > 0) {
                    for (File file : files) {
                        if (isGetPkg)
                            return;
                        if (file.isDirectory()) {
                            if (file.canRead()) {
                                searchFile(file);
                            }
                        } else {
                            if (file.getName().equals(UPDATE_FILE)) {
                                isGetPkg = true;
                                Intent intent = new Intent(InstallReceiver.ACTION_REBOOT);
                                Log.i(TAG, "pkg:" + file.getAbsolutePath());
                                intent.setData(Uri.fromFile(new File(file.getPath())));
                                UpdateDialog.this.startActivity(intent);
                                UpdateDialog.this.finish();
                            }
                        }
                    }
                }
            }

            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    File files = Environment.getExternalStorageDirectory();
                    searchFile(files);
                }

                StorageManager mStorageManager = (StorageManager) UpdateDialog.this.getSystemService(Context.STORAGE_SERVICE);
                File sdFile = Environment.getSecondaryStorageDirectory();
                if (mStorageManager.getVolumeState(sdFile.getPath()).equals(
                        android.os.Environment.MEDIA_MOUNTED)) {
                    searchFile(sdFile);
                }
                if (!isGetPkg)
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            pDialog.dismiss();
                            Toast.makeText(UpdateDialog.this,
                                    getText(R.string.dialog_update_not_found), Toast.LENGTH_LONG)
                                    .show();
                            UpdateDialog.this.finish();
                        }
                    });
            }
        }, "search update pkg").start();
    }

    private class ChooseFromListener implements OnClickListener {

        public void onClick(DialogInterface dialog, int which) {
            Intent intent = null;
            if (Build.PWV_CUSTOM_CUSTOM.equals("ORBITA")) {
                which = INDEX_LOCAL;
            }
            switch (which) {
                case INDEX_REMOTE:
	            if (UpdateManager.getDefault(UpdateDialog.this).isUpdating()) {
                        XToast.makeText(UpdateDialog.this, R.string.msg_updating_now,
                                XToast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isNetworkAvailable()) {
                        intent = new Intent(UpdateDialog.this, RemoteActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        XToast.makeText(UpdateDialog.this, R.string.msg_no_network,
                                XToast.LENGTH_SHORT).show();
                    }
                    break;
                case INDEX_LOCAL:
		    if (UpdateManager.getDefault(UpdateDialog.this).isUpdating()) {
                        XToast.makeText(UpdateDialog.this, R.string.msg_updating_now,
                                XToast.LENGTH_SHORT).show();
			return;
		    }
                    Log.i(TAG, "PROJECT>>>>>" + Build.PROJECT);
                    if (Build.PROJECT.equals("SQ26")) {
                        doUpdate();
                    } else {
                        try {
                            intent = new Intent("android.intent.action.GET_UPDATE_CONTENT");
                            startActivityForResult(intent, REQUEST_CODE_FILE);
                            Toast.makeText(UpdateDialog.this, R.string.msg_pick_update,
                                    Toast.LENGTH_SHORT).show();
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            try {
                                 intent = new Intent(Intent.ACTION_GET_CONTENT);
                                 // urovo suyuchuan modified on 20210903 start
                                 //intent.setType("*/*");
                                  intent.setType("application/*");
                                 // urovo suyuchuan modified on 20210903 end
                                 startActivityForResult(intent, REQUEST_CODE_FILE);
                                 Toast.makeText(UpdateDialog.this,R.string.msg_pick_update, Toast.LENGTH_SHORT).show();
                             } catch (ActivityNotFoundException ex) {
                                 XToast.makeText(UpdateDialog.this,R.string.msg_no_file_explore,XToast.LENGTH_SHORT).show();
                             }
                        }
                    }
                    break;
            }
        }

    }

    private void doVerifyPackage(String path) {
        verifyPackageDialog.setTitle(R.string.dialog_verify_title);
        verifyPackageDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //urovo weiyu add on 2020-01-14 start
        Log.d(TAG,"path:"+path);
	path = path.replace("/document/primary:","/storage/emulated/0/");
        path = path.replace(":","/");
        path = path.replace("document","storage");
        Log.d(TAG,"path2:"+path);
        //urovo weiyu add on 2020-01-14 end
        final File file = new File(path);
        verifyPackageDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    // TODO Auto-generated method stub
                    RecoverySystem.verifyPackage(file, UpdateDialog.this, null);
                    Intent intent = new Intent(InstallReceiver.ACTION_REBOOT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                        StrictMode.setVmPolicy(builder.build());
                    }
                    intent.setData(Uri.fromFile(file));
                    intent.putExtra("localupdate", true);
                    startActivity(intent);
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    if (file != null && file.exists()) {
                        file.delete(); // 校验失败删除文件
                    }
                    Toast.makeText(UpdateDialog.this, R.string.msg_not_right_file_type,
                            Toast.LENGTH_SHORT).show();
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    if (file != null && file.exists()) {
                        file.delete(); // 校验失败删除文件
                    }
                    Toast.makeText(UpdateDialog.this, R.string.msg_not_right_file_type,
                            Toast.LENGTH_SHORT).show();
                }
                verifyPackageDialog.dismiss();
                Looper.loop();
            }
        }).start();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
                    //doVerifyPackage(data.getData().getPath());
                    Log.d(TAG,"path----->"+data.getData().getPath());
                    String resultPath = data.getData().getPath();
                    if (resultPath.contains("usbotg")) {
                        String filePath = resultPath.replace("/storage/usbotg","/sdcard/QRDUpdate");
                        Log.d(TAG,"filePath--->"+filePath);
                        //doFileCopy(resultPath,filePath);
                        //doVerifyPackage(filePath);
                        new FileCopyTask().execute(resultPath, filePath);
                    } else {
                        Log.d(TAG,"sdcard update");
                        doVerifyPackage(data.getData().getPath());
                    }
                    break;
            }
        }
    }

    protected void doFileCopy (String orgPath, String desPath) {
        try {
            File file = new File(desPath);
            file.getParentFile().mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }
            InputStream is = new FileInputStream(new File(orgPath));
            OutputStream os = new FileOutputStream(file);
            byte[] bs = new byte[2048];
            while (true) {
                int len = is.read(bs, 0, bs.length);
                if (len == -1) {
                    break;
                } else {
                    os.write(bs, 0, len);
                }
            }
            is.close();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class FileCopyTask extends AsyncTask<String, Object, String> implements
            OnCancelListener {

        private ProgressDialog mProgressDialog;

        protected void onPostExecute(String result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
                doVerifyPackage(result);
            }
            return;
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(UpdateDialog.this);
            mProgressDialog.setTitle(R.string.title_copyotapackage);
            mProgressDialog.setMessage(getResources().getString(R.string.msg_copyotapackage));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setOnCancelListener(this);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            String orgPath = params[0];
            String desPath = params[1];
            doFileCopy(orgPath,desPath);
            return desPath;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();
        boolean internet = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        return activeNetworkInfo != null;
    }

    public void remoteUpdate() {
        Intent intent = new Intent(UpdateDialog.this, RemoteActivity.class);
        startActivity(intent);
        finish();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CHOOSE:
                if (android.os.SystemProperties.get("pwv.osupdate.local.menu", "true").equals("false")) {
                    Log.d(TAG,"hide the local update item!!!!!");
                    return new AlertDialog.Builder(this)
                        .setSingleChoiceItems(
                                new MenuAdapter(this, getResources().getStringArray(
                                        R.array.remote_entries_remote), R.drawable.remote_update), 0, new ChooseFromListener())
                        .setTitle(R.string.title_choose_update_from).setOnCancelListener(this)
                        .setNegativeButton(android.R.string.cancel, this).create();
                } else if (android.os.SystemProperties.get("pwv.osupdate.remote.menu", "true").equals("false")) {
                    Log.d(TAG,"hide the remote update item!!!!!");
                    return new AlertDialog.Builder(this)
                            .setSingleChoiceItems(
                                    new MenuAdapter(this, getResources().getStringArray(
                                            R.array.remote_entries_local), R.drawable.local_update),
                                    0, new ChooseFromListener())
                            .setTitle(R.string.title_choose_update_from).setOnCancelListener(this)
                            .setNegativeButton(android.R.string.cancel, this).create();
                } else {
                    return new AlertDialog.Builder(this)
                            .setSingleChoiceItems(
                                    new MenuAdapter(this, getResources().getStringArray(
                                            R.array.remote_entries), R.drawable.remote_update,
                                            R.drawable.local_update), 0, new ChooseFromListener())
                            .setTitle(R.string.title_choose_update_from).setOnCancelListener(this)
                            .setNegativeButton(android.R.string.cancel, this).create();
                }
        }
        return null;
    }

    public void onProgress(int progress) {
        verifyPackageDialog.setProgress(progress);
    }

    public void onClick(DialogInterface dialog, int which) {
        finish();
    }

    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
