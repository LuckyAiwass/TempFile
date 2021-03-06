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

public class ConfirmRebootDialog extends Activity implements  OnClickListener{
    private static final int DIALOG_CHOOSE = 1;
    public static String TAG = "OSUpdate" + ConfirmRebootDialog.class.getSimpleName();
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, " ConfirmRebootDialog  " + getIntent().getAction());
        this.showDialog(DIALOG_CHOOSE);
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
		.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.title_dialog_alert))
                .setMessage(getString(R.string.msg_update_successfully))
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Log.i(TAG, "Rebooting Now.");
                        Intent intent = new Intent("android.ota.reboot.update.action");
                        intent.setPackage("com.android.settings");
                        sendBroadcast(intent);
	                UpdateState.getInstance().setIsUpdating(false);//升级完成，系统重启前清除正在升级标志位
                        finish();
                    }
                }).create();
        }
        return  super.onCreateDialog(id);
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      