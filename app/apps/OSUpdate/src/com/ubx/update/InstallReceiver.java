/**
 * Copyright (c) 2011-2012 Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.update;

import java.io.File;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Message;
import android.os.Looper;
import android.os.RecoverySystem;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.provider.Settings;
import android.os.SystemProperties;

public class InstallReceiver extends Activity implements RecoverySystem.ProgressListener,
        OnCancelListener, OnClickListener {

    public static final String ACTION_REBOOT = "com.ubx.update.REBOOT";

    public static final String ACTION_REBOOT_DELTA = "com.ubx.update.REBOOT_DELTA";

    private static final int DIALOG_CONFIRM_REBOOT = 1;
    
    private static final int DIALOG_CONFIRM_BatteryLevel = 2;

    private int mBatteryWarningLevel = 30;

    private static final String TAG = "OSUpdateInstallUpdateReceiver";

    private static final boolean DEBUG = true;

    private ProgressDialog verifyPackageDialog;
    private SharedPreferences prefs;
    private boolean enableDelayedUpdate = false; 
    private String enableDelayedTime ="10800"; 
    private boolean isSilence = false;
    private boolean isForceUpdate = false;
    private static final int MESSAGE_UPDATE_FAILED = 1;
    private static final int MESSAGE_REBOOT_TO_INSTALL = 2;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
	    switch (msg.what) {
                case MESSAGE_UPDATE_FAILED:
		    String path = (String) msg.obj;
	            deleteUpdateZip(path);
        	    updateFaildNotification(path);
		    if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
            	        UpdateUtil.pushUpgradeResultToServer(InstallReceiver.this, UpdateUtil.VERIFICATION_FAILED);
		    }
                    break;
		case MESSAGE_REBOOT_TO_INSTALL:
		    rebootToInstall();
		    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            String level = Settings.System.getString(this.getContentResolver(), "FotaBatteryWarningLevel");
            if(!TextUtils.isEmpty(level)) {
                mBatteryWarningLevel = Integer.parseInt(level);
            }
            Log.d(TAG, "level= " + level + " mBatteryWarningLevel= " + mBatteryWarningLevel);
        } catch(Exception e) {

        }
        if(mBatteryWarningLevel < 15 || mBatteryWarningLevel > 99) {
            mBatteryWarningLevel = 30;
        }

	//urovo yansitian add begin 20220221
	if(!android.text.TextUtils.isEmpty(SystemProperties.get("persist.sys.urv.set.fota.battery.warning.level", ""))){
	    mBatteryWarningLevel = SystemProperties.getInt("persist.sys.urv.set.fota.battery.warning.level", 30);
	}
	Log.d(TAG, "mBatteryWarningLevel= " + mBatteryWarningLevel);
	//add end

	isSilence = getIntent().getBooleanExtra("isSilence",false);
        isForceUpdate = getIntent().getBooleanExtra("force",false);
        if (ACTION_REBOOT.equals(getIntent().getAction())
                || ACTION_REBOOT_DELTA.equals(getIntent().getAction())) {
            prefs = PreferenceManager.getDefaultSharedPreferences(InstallReceiver.this);
            enableDelayedUpdate = prefs.getBoolean(UpdateSettings.KEY_DELAYED_UPDATE_ENABLE, getResources().getBoolean(R.bool.config_update_delayed_enable));
            enableDelayedTime = prefs.getString(UpdateSettings.KEY_DELAYED_UPDATE_TIME, getResources().getString(R.string.config_update_delayed_time));
            if(Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
                //下载完升级包就保存
                writeRecoveryCommand();
                saveUpdate();
                finish();
                //showFullDialog();
            } else if (isSilence || SystemProperties.get("persist.sys.update.silence","").contains("silence") || isForceUpdate) {
		        Log.d(TAG,"update silence............................................");
                if(checkBatteryLevelWarning(mBatteryWarningLevel)) {
                    //showDialog(DIALOG_CONFIRM_BatteryLevel);
                    //UpdateSettings.scheduleNextCheck(InstallReceiver.this);
		    if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
                        UpdateUtil.pushUpgradeResultToServer(InstallReceiver.this, UpdateUtil.LOW_BATTERY_LEVEL);
		    }
                    Log.d(TAG,"Low battery level,please do it again!");
                    finish();
                } else {
		    if(SystemProperties.get("persist.sys.urv.delay.update", "false").equals("true")){
    		        showUpgradeTip();
		        mHandler.sendEmptyMessageDelayed(MESSAGE_REBOOT_TO_INSTALL,300000);
		    }else{
	                rebootToInstall();
		    }
                }
	    } else {
                //if(Build.PWV_CUSTOM_CUSTOM.startsWith("UTE")) {
                    if(checkBatteryLevelWarning(mBatteryWarningLevel)) {
                        Log.d(TAG, "checkBatteryLevelWarning");
                        if (!getIntent().getBooleanExtra("localupdate", false)) {
			    if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
                                UpdateUtil.pushUpgradeResultToServer(InstallReceiver.this, UpdateUtil.LOW_BATTERY_LEVEL);
			    }
                        }
                        showDialog(DIALOG_CONFIRM_BatteryLevel);
                    } else {
                        if (Build.PWV_CUSTOM_CUSTOM.equals("UTE") && Build.PROJECT.equals("SQ53") || SystemProperties.getBoolean("persist.sys.urv.fota.without.confirm", false)) {
                            rebootToInstall();
                            //53 AB系统升级流程
                        }
                        showDialog(DIALOG_CONFIRM_REBOOT);
                    }
	     }
	 } else {
            finish();
        }
        } 

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
	if (verifyPackageDialog != null) {
            verifyPackageDialog.dismiss();
        }
        if(updateDialog != null) {
            updateDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        // TODO Auto-generated method stub
        if(updateDialog != null && updateDialog.isShowing()) {
            return;
        }
        super.onBackPressed();
    }
    private boolean isDelta() {
        return ACTION_REBOOT_DELTA.equals(getIntent().getAction());
    }

    private void rebootToInstall() {
        final String path = getIntent().getData().getSchemeSpecificPart();
        verifyPackageDialog = new ProgressDialog(this);
        final boolean localUpdate = getIntent().getBooleanExtra("localupdate", false);
        verifyPackageDialog.setTitle(R.string.dialog_verify_title);
        verifyPackageDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        if (!localUpdate && !isSilence && !SystemProperties.get("persist.sys.update.silence","").contains("silence")) {
            verifyPackageDialog.show();
        }
        log("install file:" + path);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Looper.prepare();
                    if (!localUpdate) {
			if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
                            UpdateUtil.writeOemPartition(UpdateUtil.TAG_IS_UPGRADE,"1"); // 标志是升级进行重启
			}
                        RecoverySystem.verifyPackage(new File(path), InstallReceiver.this, null);
                    }
                    if("true".equals(SystemProperties.get("ro.build.ab_update", "false")) || Build.PROJECT.equals("SQ53") || Build.PROJECT.equals("SQ47") || Build.PROJECT.equals("SQ53Q")){
                        Intent intent = new Intent(InstallReceiver.this,UpdateProgressDialog.class);
                        intent.putExtra("extra_update_file",path);
                        intent.putExtra("force",isForceUpdate);
			intent.putExtra("isSilence",isSilence || SystemProperties.get("persist.sys.update.silence","").contains("silence"));
                        startActivity(intent);
                    }else{
			Settings.System.putString(getContentResolver(), "OtaFilePath" , path);
                        //new android.device.DeviceManager().setSettingProperty("System-OtaFilePath",path);
                        if (isForceUpdate) {
                            UpdateUtil.handleForceupgradingView(true); //unlock the statusbar and back button,home button,appswitch button
                            ForceUpdateManager.getInstance().setforceUpdate(false);
                        }
                        Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
                        intent.setPackage("android");
                        intent.putExtra("packagefile", new File(path));
                        intent.putExtra("qrdupdate", true);
                        sendBroadcast(intent);
                    }
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.obj = path;
		    msg.what = MESSAGE_UPDATE_FAILED;
                    mHandler.sendMessage(msg);
                    verifyPackageDialog.dismiss();
                    return;
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.obj = path;
		    msg.what = MESSAGE_UPDATE_FAILED;
                    mHandler.sendMessage(msg);
                    verifyPackageDialog.dismiss();
                    return;
                }
                verifyPackageDialog.dismiss();
                Looper.loop();
            }
        }).start();
    }

    public void updateFaildNotification(final String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_dialog_alert));
        builder.setMessage(getString(R.string.download_package_damaged));
        builder.setOnCancelListener(this);
        /*builder.setNeutralButton(android.R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });*/
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showUpgradeTip() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_dialog_alert));
        builder.setMessage(getString(R.string.upgrade_tip_msg));
	builder.setOnCancelListener(this);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void verifyFaildPCIAQNotification() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_dialog_alert));
        builder.setMessage(getString(R.string.download_ota_check_fail));
        builder.setOnCancelListener(this);
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deleteUpdateZip(final String path){
        File file = new File(path);
        if (file != null) {
              file.delete();
        }
    }

    private void installToInstallDelta() {
        final String path = getIntent().getData().getSchemeSpecificPart();
        log("delta install file:" + path);
        new AsyncTask<String, Object, Boolean>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                Toast.makeText(InstallReceiver.this, R.string.toast_upgrade_reboot,
                        Toast.LENGTH_LONG).show();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (!result) {
                    Toast.makeText(InstallReceiver.this, R.string.toast_upgrade_failed,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected Boolean doInBackground(String... params) {
                return UpdateUtil.copyToDeltaFile(new File(path)) && UpdateUtil.writeDeltaCommand()
                        && UpdateUtil.rebootInstallDelta(InstallReceiver.this);
            }

        }.execute();
    }

    private void saveUpdate() {
        String path = getIntent().getData().getSchemeSpecificPart();
        String version = getIntent().getStringExtra("version");
        log("save file:" + path + " version " + version);
        UpdateUtil.saveUpdate(this, path, isDelta(), version, true);
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CONFIRM_REBOOT: {
                return new AlertDialog.Builder(this).setOnCancelListener(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.title_dialog_alert))
                        .setMessage(getString(R.string.msg_reboot))
                        .setNeutralButton(android.R.string.cancel, this)
                        .setPositiveButton(android.R.string.ok, this).create();
            }
            case DIALOG_CONFIRM_BatteryLevel: {
                return new AlertDialog.Builder(this).setOnCancelListener(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(getString(R.string.title_dialog_alert))
                        .setMessage(getString(R.string.battery_low_notcharging))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                          // TODO Auto-generated method stub
                            finish();
                         }
                        }).create();
            }
        }
        return super.onCreateDialog(id);
    }

    public void onCancel(DialogInterface dialog) {
        Log.d(TAG, "onCancel .setOnCancelListener");
        finish();
    }

    public void onClick(DialogInterface dialog, int which) {
        if (ACTION_REBOOT.equals(getIntent().getAction())
                && which == DialogInterface.BUTTON_POSITIVE) {
            UpdateUtil.deteteUpdate(InstallReceiver.this);
            rebootToInstall();
        } else if (ACTION_REBOOT_DELTA.equals(getIntent().getAction())
                && which == DialogInterface.BUTTON_POSITIVE) {
            installToInstallDelta();
            finish();
        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
            boolean localUpdate = getIntent().getBooleanExtra("localupdate", false);
            if(enableDelayedUpdate && !localUpdate) {
                updateLater();
            }
            saveUpdate();
            finish();
        }
        // finish();
    }

    public void updateLater() {
        log("updateLater " + enableDelayedTime);
        Intent intent = new Intent(InstallReceiver.this, NotificationReceiver.class);
        intent.setAction("action.OTA_UPDATE_DELAYED");
        PendingIntent sender = PendingIntent.getBroadcast(InstallReceiver.this, 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, Integer.valueOf(enableDelayedTime) * 1000);
        long now = System.currentTimeMillis();
        long intervalMillis = Integer.valueOf(enableDelayedTime) * 1000;
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        //alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
        alarm.set(AlarmManager.RTC_WAKEUP, now + intervalMillis, sender);
    }
    private void log(String msg) {
        if (DEBUG) {
            Log.e(TAG, msg);
        }
    }

    public void onProgress(int progress) {
        verifyPackageDialog.setProgress(progress);
    }
    private void writeRecoveryCommand() {
        boolean localUpdate = getIntent().getBooleanExtra("localupdate", false);
        log("updateLaterenableDelayedUpdate" + enableDelayedUpdate);
        if(!localUpdate) {
            String path = getIntent().getData().getSchemeSpecificPart();
            File otaFile = new File(path);
            try {
               path = otaFile.getCanonicalPath();
           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
            Intent intent = new Intent( "action.WRITE_RECOVERY_UPDATE_COMMAND");
            intent.putExtra("action", 1);
            intent.putExtra("otaPath", path);
            sendBroadcast(intent);
        }
    }
    private void delayedUpdate() {
        boolean localUpdate = getIntent().getBooleanExtra("localupdate", false);
        log("updateLaterenableDelayedUpdate" + enableDelayedUpdate);
        if(enableDelayedUpdate && !localUpdate) {
            updateLater();
            String path = getIntent().getData().getSchemeSpecificPart();
            File otaFile = new File(path);
            try {
               path = otaFile.getCanonicalPath();
           } catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
           }
            Intent intent = new Intent( "action.WRITE_RECOVERY_UPDATE_COMMAND");
            intent.putExtra("action", 1);
            intent.putExtra("otaPath", path);
            sendBroadcast(intent);
        }
        saveUpdate();
        //finish();
    }
    public boolean checkBatteryLevelWarning(int updateLevel) {
        Intent intentBt = this.registerReceiver(null, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        int level = intentBt.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = intentBt.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
        int status = intentBt.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean batteryCharging  =  (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
        //level_percentage = getBatteryLevel(level, scale);

        // AC status
        int external_power_source = intentBt.getIntExtra(
                BatteryManager.EXTRA_PLUGGED, 0);
        boolean acPower = (external_power_source == BatteryManager.BATTERY_PLUGGED_AC);
        //低于30%不做任何操作
        if(Build.PWV_CUSTOM_CUSTOM.startsWith("UTE")) {
            if(batteryCharging){
                return false;
        	} else{
        		if(level >= updateLevel){
        			return false;
        		}else{
        			return true;
        		}
        	}
        } else {
            Log.d(TAG, "updateDialog.EXTRA_LEVEL " + level);
            if (level >= updateLevel || (batteryCharging && level > 15)) {
                //允许重启升级
            } else {
                return true;
            }
        }
        return false;
    }
    Dialog updateDialog;
    Button sure, cancel,warningOk;
    LinearLayout mupdate_dialog_layout;
    void showFullDialog() {
         View dialogView = View.inflate(this,
                 R.layout.update_reboot_dialog, null);
         mupdate_dialog_layout = (LinearLayout) dialogView.findViewById(R.id.update_dialog_layout);
         sure = (Button) dialogView.findViewById(R.id.update_ok);
         sure.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 if( checkBatteryLevelWarning(mBatteryWarningLevel)) {
                     mupdate_dialog_layout.setBackgroundResource(R.drawable.batterylevelwarning);
                     sure.setVisibility(View.GONE);
                     cancel.setVisibility(View.GONE);
                     warningOk.setVisibility(View.VISIBLE);
                     delayedUpdate();
                 } else {
                     updateDialog.dismiss();
                     UpdateUtil.deteteUpdate(InstallReceiver.this);
                     Intent intent = new Intent( "action.WRITE_RECOVERY_UPDATE_COMMAND");
                     intent.putExtra("action", 0);
                     sendBroadcast(intent);
                     rebootToInstall();
                 }
             }
         });
         cancel = (Button) dialogView.findViewById(R.id.update_cancel);
         cancel.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 delayedUpdate();
                 finish();
             }
         }); 
         warningOk = (Button) dialogView.findViewById(R.id.update_level_ok);
         warningOk.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 // TODO Auto-generated method stub
                 finish();
             }
         }); 
         updateDialog = new Dialog(this,R.style.updateDialog);
         updateDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                 WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
         updateDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                 WindowManager.LayoutParams.FLAG_FULLSCREEN);
         Window win = updateDialog.getWindow();
         win.setGravity(Gravity.BOTTOM);
         win.getDecorView().setPadding(0, 0, 0, 0);
         updateDialog.setCancelable(false);
         updateDialog.setCanceledOnTouchOutside(false);
         updateDialog.setContentView(dialogView, new ViewGroup.LayoutParams(
                 LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
         updateDialog.show();
         WindowManager.LayoutParams lp = win.getAttributes();
         lp.width = WindowManager.LayoutParams.MATCH_PARENT;
         lp.height = WindowManager.LayoutParams.MATCH_PARENT;
         
         WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
         //int width = wm.getDefaultDisplay().getWidth();
         int height = wm.getDefaultDisplay().getHeight();
         lp.x = 0;
         lp.height = height;
         lp.format = PixelFormat.RGBA_8888;
         lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
         lp.gravity = Gravity.BOTTOM;
         win.setAttributes(lp);
     }
}
