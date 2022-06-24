package com.ubx.update;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.os.Handler;

import android.os.Message;
import com.ubx.update.service.BroadcastRegisterService;

import java.io.File;
import java.util.List;
import android.os.SystemProperties;
import android.provider.Settings;
import android.os.SystemProperties;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "OSUpdate" + "NotificationReceiver";

    static final Object mStartingServiceSync = new Object();

    static PowerManager.WakeLock mStartingService;

    public static boolean mIsExpired = false;

    public static boolean mIsPushed = false; // use to push upgrade result to server

    private static boolean isbootcomplete = false;

    private static final int MESSAGE_DETECTION_NETWORK = 1;

    private Context context;

    private Intent in;

    @Override
    public void onReceive(Context ctx, Intent intent) {
	context = ctx;
	in = intent;
        Log.e(TAG, "onReceive() intent: " + intent.getAction());
	if(SystemProperties.get("persist.sys.urv.silent.update","false").equals("true")){
            Settings.System.putString(ctx.getContentResolver(), "enable_silent_update" , "true");
        }
        // add for silent update
        //String enableSilentUpdate = new android.device.DeviceManager().getSettingProperty("System-enable_silent_update");
	String enableSilentUpdate = Settings.System.getString(ctx.getContentResolver(), "enable_silent_update");
        boolean isSilentUpdate = (enableSilentUpdate != null && enableSilentUpdate.equals("true")) ? true : false;
	Log.d(TAG,"isSilentUpdate: "+isSilentUpdate);
        String action = intent.getAction();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        //String defaultConfig = ctx.getResources().getString(R.string.config_autoupdate_mode);
        //Log.e(TAG, "onReceive() autoupdate defaultConfig " + defaultConfig);
        String autoupdate = prefs.getString(UpdateSettings.KEY_UPDATE_PREFS, "0");
        boolean bootCompletedUpdate =  prefs.getBoolean(UpdateSettings.KEY_BOOT_CHECK, SystemProperties.get("persist.sys.bootcompleted.update", "false").equals("true") || ctx.getResources().getBoolean(R.bool.config_bootcomplete_update));
        //兼容旧的OS
        Log.e(TAG, "onReceive() autoupdate config_bootcomplete_update " + bootCompletedUpdate);
        if(autoupdate.equals("1")) {
            bootCompletedUpdate = true;
            Editor mEditor =  prefs.edit();
            mEditor.putString(UpdateSettings.KEY_UPDATE_PREFS, "0");
            mEditor.putBoolean(UpdateSettings.KEY_BOOT_CHECK, bootCompletedUpdate);
            mEditor.commit();
        }
        boolean scheduled_update = prefs.getBoolean(UpdateSettings.KEY_SCHEDULED_CHECK, !SystemProperties.get("persist.sys.urv.scheduled.update.frequency", "0").equals("0") || ctx.getResources().getBoolean(R.bool.config_scheduled_update));
        Log.e(TAG, "onReceive() autoupdate " + autoupdate + " bootCompletedUpdate " + bootCompletedUpdate + " scheduled_update " + scheduled_update);
        if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
        String forceUpgradingView = Settings.System.getString(ctx.getContentResolver(), "force_upgrading_view");
        Log.d(TAG,"forceUpgradingView: "+forceUpgradingView);
        if(forceUpgradingView != null && forceUpgradingView.equals("false")){
            UpdateUtil.handleForceupgradingView(true);
        }
	    Intent broadcastRegisterIntent = getExplicitIntent(ctx, new Intent(
                        "com.urovo.systemupgrade.BROADCAST_REGISTER"));
	    ctx.startService(broadcastRegisterIntent);

            SystemProperties.set("persist.sys.update.silence"," "); //复位静默升级标志
	    String otaFilePath = Settings.System.getString(ctx.getContentResolver(), "OtaFilePath");
            //String otaFilePath = new android.device.DeviceManager().getSettingProperty("System-OtaFilePath");
            if (otaFilePath != null && !otaFilePath.equals("")) {
		Settings.System.putString(ctx.getContentResolver(), "OtaFilePath","");
                //new android.device.DeviceManager().setSettingProperty("System-OtaFilePath","");
                File otaFile = new File(otaFilePath);
                try {
                    if (otaFile.exists()) otaFile.delete(); //升级完成以后删除升级包,兼容recovery升级
                } catch (Exception e) {}
            }

	    if (isSilentUpdate || bootCompletedUpdate) {
                    ConnectivityManager cm = (ConnectivityManager) ctx
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo ni = cm.getActiveNetworkInfo();
                    if (ni == null || !ni.isConnected()) {
                        Log.e(TAG, "set mIsExpired true");
                        mIsExpired = true;
                        isbootcomplete = true;
                        return;
                    }
                Intent i = getExplicitIntent(ctx, new Intent(
                        "com.urovo.systemupgrade.NOTIFICATION_SERVICE"));
                // i.setClass(ctx, NotificationService.class);
                i.putExtras(intent);
                i.putExtra("action", intent.getAction());
                i.putExtra("autoupdate", isSilentUpdate || bootCompletedUpdate);
                i.putExtra("scheduled_update", isSilentUpdate || scheduled_update);
                Uri uri = intent.getData();
                if (uri != null) {
                    i.putExtra("uri", uri.toString());
                    Log.e(TAG, "onReceive(), uri: " + uri.toString());
                }
                beginStartingService(ctx, i);
            } else if(isSilentUpdate || scheduled_update) {
                Intent i = getExplicitIntent(ctx, new Intent(
                        "com.urovo.systemupgrade.NOTIFICATION_SERVICE"));
                // i.setClass(ctx, NotificationService.class);
                i.putExtras(intent);
                i.putExtra("action", intent.getAction());
                i.putExtra("scheduled_update", isSilentUpdate || scheduled_update);
                Uri uri = intent.getData();
                if (uri != null) {
                    i.putExtra("uri", uri.toString());
                    Log.e(TAG, "onReceive(), uri: " + uri.toString());
                }
                beginStartingService(ctx, i);
 	    }          

            // 上传升级结果
	    if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
            String baseOsVersion = UpdateUtil.readOemPartition(UpdateUtil.TAG_BASE_OS_VERSION);
            if (baseOsVersion == null || baseOsVersion.equals("")) {
                UpdateUtil.setBaseOsVersion();
            }
            ConnectivityManager connManager = (ConnectivityManager) ctx
                            .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo == null || !networkInfo.isConnected()) {
                mIsPushed = true;
            } else {
                if (UpdateUtil.isUpgradeSuccess()) {
                    UpdateUtil.pushUpgradeResultToServer(ctx, UpdateUtil.NULL_REASON);
                } else {
                    if ("1".equals(UpdateUtil.readOemPartition(UpdateUtil.TAG_IS_UPGRADE))) {
                        UpdateUtil.pushUpgradeResultToServer(ctx, UpdateUtil.CHECK_FAILED_IN_RECOVERY);
                    }
                }
            }
	    }
        } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
            Log.d(TAG,"ConnectivityManager.CONNECTIVITY_ACTION-----------------------------");
            Log.e(TAG, "mIsExpired >>>>>> "+mIsExpired);
	    if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
            Log.d(TAG, "mIsPushed  >>>>>> " + mIsPushed);
            if (mIsPushed && intent.getBooleanExtra("isNetworkConnected",false)) {
                if (UpdateUtil.isUpgradeSuccess()) {
                    UpdateUtil.pushUpgradeResultToServer(ctx, UpdateUtil.NULL_REASON);
                } else {
                    if ("1".equals(UpdateUtil.readOemPartition(UpdateUtil.TAG_IS_UPGRADE))) {
                        UpdateUtil.pushUpgradeResultToServer(ctx, UpdateUtil.CHECK_FAILED_IN_RECOVERY);
                    }
                }
                mIsPushed = false;
            }
	    }
            if (mIsExpired) {
		NetworkInfo ni = (NetworkInfo) intent
                        .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (ni != null && ni.isConnected()) {
                    Intent i = getExplicitIntent(ctx, new Intent(
                            "com.urovo.systemupgrade.NOTIFICATION_SERVICE"));//
                    // i.setClass(ctx, NotificationService.class);
                    i.putExtra("action", UpdateSettings.BACKGROUND_CHECK);
                    i.putExtra("autoupdate", isSilentUpdate || bootCompletedUpdate);
                    i.putExtra("scheduled_update", isSilentUpdate || scheduled_update);
                    i.putExtra("isbootcomplete", isbootcomplete);
                    i.putExtra("mIsExpired", mIsExpired);
                    beginStartingService(ctx, i);
                    Log.e(TAG, "set mIsExpired false");
                    mIsExpired = false;
                    isbootcomplete = false;
                }
            }
        } else if (action != null && (action.equals(UpdateSettings.NOTIFICATION))) {
            Log.d(TAG,"NOTIFICATION------------------------------------------>");
            //DO nothing
            ConnectivityManager cm = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni == null || !ni.isConnected() ) {
                Log.e(TAG, "set mIsExpired true");
                mIsExpired = true;
            } else {
                Intent i = getExplicitIntent(ctx, new Intent(
                        "com.urovo.systemupgrade.NOTIFICATION_SERVICE"));
                // i.setClass(ctx, NotificationService.class);
                i.putExtra("action", UpdateSettings.BACKGROUND_CHECK);
                beginStartingService(ctx, i);
                //UpdateSettings.scheduleNextCheck(ctx);
            }
        } else if (action != null && action.equals("ACTION_UPDATE_CHECK_TESTING")) {
            Log.d(TAG,"ACTION_UPDATE_CHECK_TESTING----------------------");
            int updateFrequency = intent.getIntExtra("testFrequency", 40*1000);
            Log.e(TAG, "updateFrequency " + updateFrequency);
             UpdateUtil.scheduleUpdateService(ctx, updateFrequency);
        } else if (action != null && action.equals("ACTION_UPDATE_OTA_SERVER_URL")) {
            String otaServer = intent.getStringExtra("OTA_SERVER_URL");
            Log.e(TAG, "otaServer " + otaServer);
            Editor mEditor =  prefs.edit();
            mEditor.putString("url_server", otaServer);
            mEditor.commit();
        } else if("com.urovo.tms_Host".equals(action)) {
            String TMSServer = intent.getStringExtra("base_host");
            String TMSPort = intent.getStringExtra("base_port");
            Log.e(TAG, "otaServer " + TMSServer + "TMSPort " + TMSPort);
            Editor mEditor =  prefs.edit();
            mEditor.putString("TMSServer", TMSServer);
            mEditor.putString("TMSPort", TMSPort);
            mEditor.commit();
        } else if("action.OTA_UPDATE_DELAYED".equals(action)) {
            //延时更新
            String path = prefs.getString(UpdateUtil.KEY_UPDATE_FILE_PATH, "");
            File downfile =  new File(path);
            if(!downfile.exists()){
               UpdateUtil.deteteUpdate(ctx);
               return;
            }
            String version = prefs.getString(UpdateUtil.KEY_UPDATE_FILE_VERSION, "");
            Intent mIntent = new Intent(InstallReceiver.ACTION_REBOOT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
            }
            mIntent.setData(Uri.fromFile(new File(path)));
            mIntent.putExtra("version", version);
            Log.e(TAG, "path " + path + "version " + version);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(mIntent);
        } else if (UpdateSettings.TIMINGUPDATECHECK.equals(action)) {
	    Intent i = getExplicitIntent(ctx, new Intent(
                        "com.urovo.systemupgrade.NOTIFICATION_SERVICE"));
            i.putExtra("action", UpdateSettings.TIMINGUPDATECHECK);
            beginStartingService(ctx, i);
	} else if (UpdateSettings.RANGETIMEUPDATE.equals(action)) {
            Log.d(TAG,"start update ----------------------------------");
            Intent mIntent = new Intent(intent.getBooleanExtra("delta",false) ? InstallReceiver.ACTION_REBOOT_DELTA
                : InstallReceiver.ACTION_REBOOT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
            }
            mIntent.setData(intent.getData());
            mIntent.putExtra("version", intent.getStringExtra("version"));
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(mIntent);
        } else if (UpdateSettings.PUSH_RESULT_TO_SERVER.equals(action)) {
	    if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
		Log.d(TAG, "push upgrade result to server again !!!!");
                UpdateUtil.pushUpgradeResultToServer(ctx, UpdateUtil.KEEP_FAILED_REASON);
	    }
        } else if("com.ubx.update.MODIFY_SCHEDULED_TIME_INTERVAL".equals(action)){
	    int scheduled_time_interval = intent.getIntExtra("scheduled_time_interval", 28800);
	    if(scheduled_time_interval>=600){
	    	Log.d(TAG,"scheduled_time_interval: "+scheduled_time_interval);
	    	//SystemProperties.set("persist.sys.urv.scheduled.update.frequency",String.valueOf(scheduled_time_interval));
		Editor mEditor =  prefs.edit();
                mEditor.putString(UpdateSettings.KEY_UPDATE_TIMEOUT_PREFS, String.valueOf(scheduled_time_interval));
                mEditor.commit();
	    }else{
		Log.d(TAG,"The set interval is too short!!");
	    }
	}
    }

    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    public static void beginStartingService(Context ctx, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
			"UpdateNotificationService");
		mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            Log.e(TAG, "beginStartingService(), ctx: " + ctx + ", intent: " + intent);
            ctx.startService(intent);
        }
    }

    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (service.stopSelfResult(startId))
                    mStartingService.release();
            }
        }
    }

    public static void deleteUpdateFile(File root) {
        File files[] = root.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) { // 判断是否为文件夹
                } else {
                    if (f.exists()) { // 判断是否存在
                        //deleteAllFiles(f);
                        try {
                            f.delete();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }
}
