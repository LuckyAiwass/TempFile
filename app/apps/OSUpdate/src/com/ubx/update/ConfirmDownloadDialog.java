package com.ubx.update;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class ConfirmDownloadDialog extends Activity implements  OnClickListener, OnCancelListener {
    private static final int DIALOG_FORCE = 0;
    private static final int DIALOG_CHOOSE = 1;
    UpdateInfo updateInfo;
    public static String TAG = "OSUpdate" + ConfirmDownloadDialog.class.getSimpleName();
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, " ConfirmDownloadDialog  " + getIntent().getAction());
        //this.dismissDialog(DIALOG_CHOOSE);
        boolean force = getIntent().getBooleanExtra("force",false);
        if(force){
            this.showDialog(DIALOG_FORCE);
        }else{
            this.showDialog(DIALOG_CHOOSE);
        }
        updateInfo = (UpdateInfo) getIntent().getSerializableExtra(Intent.EXTRA_INTENT);
        UpdateUtil.handleForceupgradingView(false); //false 禁用
    }
    @Override
    public void onCancel(DialogInterface dialog) {
        // TODO Auto-generated method stub
        this.finish();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // TODO Auto-generated method stub
        this.finish();
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CHOOSE:
                return new AlertDialog.Builder(this)
                .setTitle(R.string.title_getupdate).setOnCancelListener(this)
                .setMessage(R.string.msg_start_download)
                .setNegativeButton(android.R.string.cancel, this)
                //.setCanceledOnTouchOutside(false)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok, new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if(updateInfo != null) {
                            if (!DownloadManager.getDefault(ConfirmDownloadDialog.this).isDownloading()) {
                                DownloadManager.getDefault(ConfirmDownloadDialog.this).download(updateInfo);
                            }
                        }
                        finish();
                    }
                }).create();
            case DIALOG_FORCE:
                return new AlertDialog.Builder(this)
                //.setTitle(R.string.title_getupdate)
                .setMessage(R.string.msg_start_download)
                .setOnCancelListener(this)
                //.setCanceledOnTouchOutside(false)
                .setCancelable(false)
                .setPositiveButton(R.string.update_display, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //UpdateSettings.scheduleNextCheck(ConfirmDownloadDialog.this);
                        //finish();
                        //UpdateUtil.handleForceupgradingView(true);
                        Intent intent = new Intent(ConfirmDownloadDialog.this,RemoteActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("uroforce", true);
                        startActivity(intent);
                        finish();
                    }
                }).create();
        }
        return  super.onCreateDialog(id);
    }
}
                                                                                                                                                                                                                                