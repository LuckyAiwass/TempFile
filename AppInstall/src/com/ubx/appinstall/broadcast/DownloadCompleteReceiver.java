package com.ubx.appinstall.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import com.ubx.appinstall.activity.MainActivity;
import com.ubx.appinstall.activity.InstallActivity;

import com.ubx.appinstall.service.AutoInstallService;
import com.ubx.appinstall.util.AutoInstallConfig;
import com.ubx.appinstall.util.SharedPrefsStrListUtil;
import com.ubx.appinstall.util.ULog;
import com.zxing.activity.CaptureActivity;
import com.zxing.activity.ResultActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DownloadCompleteReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent mintent = new Intent(context,InstallActivity.class);
		mintent.putExtra("isDownloadApp", true);
		context.startActivity(mintent);
	}
}
