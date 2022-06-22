package com.ubx.appinstall.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;
import android.util.Log;
import com.ubx.appinstall.activity.MainActivity;

import com.ubx.appinstall.service.StartActivityService;
import com.ubx.appinstall.util.AutoInstallConfig;
import com.ubx.appinstall.util.SharedPrefsStrListUtil;
import com.ubx.appinstall.util.ULog;
import com.zxing.activity.CaptureActivity;
import com.zxing.activity.ResultActivity;
import com.ubx.appinstall.service.AutoInstallService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String KEY_AUTO_INSTALL_APPS = "persist.auto.install.apps";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ltq", "onReceive in BootCompletedReceiver:" + intent.getAction());
        Intent mintent = new Intent(context,StartActivityService.class);
        context.startService(mintent);
        String isCheck = Settings.System.getString(context.getContentResolver(), KEY_AUTO_INSTALL_APPS);
        ULog.d("isCheck   --------------------------->" + isCheck);
        if (isCheck != null && isCheck.equals("true")){
            Intent intent_powerboot = new Intent(context, AutoInstallService.class);
            context.startService(intent_powerboot);
        }
    }
}
