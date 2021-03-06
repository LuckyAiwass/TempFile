/**
 * Copyright (c) 2013 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.ubx.update;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.os.Build;
import android.os.StrictMode;

import java.io.File;

public class StartUpdate extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        if ("com.osupdate.upgraderom".equals(action) || "android.intent.KQ_OSUPDATE".equals(action)) {
            Log.d("XOLO Care", "XOLO Start update received !!!");
            String filePath = intent.getStringExtra("fullfilename");
            File upgradeFile = new File(filePath);
            Log.d("XOLO Care", "XOLO install file:" + upgradeFile);
            Intent upIntent = new Intent("android.intent.action.MASTER_CLEAR");
            upIntent.setPackage("android");
            upIntent.putExtra("packagefile", upgradeFile);
            upIntent.putExtra("qrdupdate", true);
            context.sendBroadcast(upIntent);
        } else {
            Log.d("XOLO Care", "XOLO Start update received !!!");
            String filePath = intent.getStringExtra("filePath");
	    boolean isSilence = intent.getBooleanExtra("isSilence",false);
           // recheck　for　URI　operations
            filePath = filePath.replace("/mnt/sdcard", "/storage/sdcard0");
            File upgradeFile = new File(filePath);
            Log.d("XOLO Care", "XOLO install file:" + upgradeFile);
            intent = new Intent(InstallReceiver.ACTION_REBOOT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
            }
            intent.setData(Uri.fromFile(upgradeFile));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.putExtra("isSilence",isSilence);
            context.startActivity(intent);
        }

    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              