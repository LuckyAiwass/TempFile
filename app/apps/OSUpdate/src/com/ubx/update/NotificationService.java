
package com.ubx.update;

import com.ubx.update.misc.Constants;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Process;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.app.NotificationChannel;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.net.ConnectivityManager;
import android.provider.Settings;

public class NotificationService extends Service {
    private static final String TAG = "OSUpdate" + "NotificationService";

    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    private Context mContext;
    private static UpdateInfo info;
    private DownloadManager downloadManager;

    private boolean isSilentUpdate;

    private Handler mCheckHandler = new Handler() {
        public void handleMessage(Message msg) {
            /*info = (UpdateInfo) msg.obj;
            Log.e(TAG, "handleMessage(),getHasNewVersion(): " + info.hasNewVersion());

            if (info.hasNewVersion()) {
                Log.e(TAG, "handleMessage(), has new version");
                showNotification(mContext);
            }
            UpdateSettings.saveLastUpdateTime(mContext);
            UpdateSettings.scheduleNextCheck(mContext);*/
            if (!UpdateUtil.getSystemCurrentTime().getString("currentDate").equals(SystemProperties.get("persist.sys.update.notificationdate",""))) {
                Log.d(TAG,"show update notification !");
                showNotification(mContext);
                SystemProperties.set("persist.sys.update.notificationdate",UpdateUtil.getSystemCurrentTime().getString("currentDate"));
            }
        }
    };
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Receive new Message show or not");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i("owen", "Yes is clicked");
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i("owen", "No is clicked");
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }
    private void showConfirmAlert(final UpdateInfo updateInfo) {
        Log.e(TAG, "showConfirmAlert ): " + updateInfo.hasNewVersion());
        /*Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("是否重启服务");
        builder.setNegativeButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                   // to do
            }
        });
        builder.setPositiveButton("确定", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                   // to do
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//需要添加的语句
        dialog.show();*/
        final AlertDialog.Builder dialog =new AlertDialog.Builder(NotificationService.this);
        dialog.setTitle(R.string.title_getupdate);
        dialog.setMessage(R.string.msg_start_download);
        dialog.setNeutralButton(android.R.string.ok, new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if(updateInfo != null) {
                    if (!DownloadManager.getDefault(NotificationService.this).isDownloading()) {
                        DownloadManager.getDefault(NotificationService.this).download(updateInfo);
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.setPositiveButton(android.R.string.cancel, new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
            }
        });
        Dialog alertDialog = dialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//set background was transparent
        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//需要添加的语句
        alertDialog.setCanceledOnTouchOutside(false);//点击外面区域不会让dialog消失
        dialog.show();
    }
    private NotificationManager getNotificationManager(Context ctx) {
        return (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void showNotification(Context ctx) {
/*        Notification noti = new Notification();
        String title = ctx.getResources().getString(R.string.system_update_notification_title);
        String msg = ctx.getResources().getString(R.string.system_update_notification_message);
        Intent i = new Intent(ctx, RemoteActivity.class);
        i.putExtra("notify", true);
        noti.icon = android.R.drawable.stat_notify_sync;
        noti.when = 0;
        noti.flags = Notification.FLAG_AUTO_CANCEL;
        noti.tickerText = title;
        noti.defaults = 0;
        noti.sound = null;
        noti.vibrate = null;
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, i, 0);
        noti.setLatestEventInfo(ctx, title, msg, pi);
        getNotificationManager(ctx).notify(R.string.system_update_notification_title, noti);*/
	Intent i = new Intent(ctx, RemoteActivity.class);
        i.putExtra("notify", true);
	PendingIntent pi = PendingIntent.getActivity(ctx, 0, i, 0);

	String title = ctx.getResources().getString(R.string.system_update_notification_title);
        String msg = ctx.getResources().getString(R.string.system_update_notification_message);	

	NotificationChannel channel = new NotificationChannel("1","osupdate_channel", NotificationManager.IMPORTANCE_DEFAULT);
	channel.enableLights(true); //是否在桌面icon右上角展示小圆点
	//channel.setLightColor(Color.RED); //小红点颜色
	channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知

	NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	manager.createNotificationChannel(channel);
	Notification.Builder builder = new Notification.Builder(ctx);
	builder.setContentTitle(title);
	builder.setContentText(msg);
	builder.setSmallIcon(R.drawable.remote_update);
	builder.setChannelId("1");
	builder.setAutoCancel(true);//设置点击通知后通知取消
	builder.setContentIntent(pi);//设置点击通知后跳转界面
	Notification notification = builder.build();
	manager.notify(1, notification);
    }

    private String isWifiOrInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();
        boolean internet = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        if (wifi) {
            return "wifi";
        } else if (internet) {
            return "internet";
        } else {
            return null;
        }
    }

    private void doUpdate() {
        String address = downloadManager.getServerUrl();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (address != null) {
            List<UpdateInfo> results;
            if(address.startsWith("https")){
                results = UpdateUtil.getUpdateInfo(UpdateUtil.buildUrl(mContext, address),mContext);
            }else{
                Log.d(TAG,"http-------------------------");
                results = UpdateUtil.getUpdateInfo(UpdateUtil.buildUrl(mContext, address));
            }
            if (results != null && results.size() > 0) {
                UpdateInfo info = results.get(0);
                if (info != null && info.hasNewVersion()) {
                    if (!DownloadManager.getDefault(this).isDownloading()) {
                        DownloadManager.getDefault(this).download(info);
                    }
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    void processMessage(Message msg) {
        Bundle bundle = (Bundle) msg.obj;
        String action = bundle.getString("action");
        boolean autoupdate = bundle.getBoolean("autoupdate", false);
        boolean scheduled_update = bundle.getBoolean("scheduled_update", false);
        Log.d(TAG, action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String updateFrequency = isSilentUpdate ? "28800" : prefs.getString(UpdateSettings.KEY_UPDATE_TIMEOUT_PREFS, getResources().getString(R.string.config_scheduled_update_time));
	    if(SystemProperties.get("persist.sys.urv.scheduled.update.frequency", "0").equals("0")){
                updateFrequency = prefs.getString(UpdateSettings.KEY_UPDATE_TIMEOUT_PREFS, getResources().getString(R.string.config_scheduled_update_time));
                if(updateFrequency.compareTo("21600")<0){
                    updateFrequency = "86400";
                }
            }else{
                updateFrequency = prefs.getString(UpdateSettings.KEY_UPDATE_TIMEOUT_PREFS, SystemProperties.get("persist.sys.urv.scheduled.update.frequency", "0"));
            }
            if (autoupdate) {
                String address = downloadManager.getServerUrl();
                if (address != null) {
                    List<UpdateInfo> results;
                    if(address.startsWith("https")){
                        results = UpdateUtil.getUpdateInfo(UpdateUtil.buildUrl(
                            NotificationService.this, address),mContext);
                    }else{
                        results = UpdateUtil.getUpdateInfo(UpdateUtil.buildUrl(
                            NotificationService.this, address));
                    }
                    if(Build.PWV_CUSTOM_CUSTOM.equals("UTE") || Build.PWV_CUSTOM_CUSTOM.equals("UTEWO")) {
                        if(results == null || results.size() <= 0) {
                            results = UpdateUtil.getUpdateInfo(address);
                        }
                    }
                    if (results != null && results.size() > 0) {
                        UpdateInfo info = results.get(0);
                        String version = prefs.getString(UpdateUtil.KEY_UPDATE_FILE_VERSION, "");
                        Log.d(TAG, "SharedPreferences version " + version);
                        if (info != null && info.hasNewVersion()
                                && !version.equals(info.getVersion())) {
                            // Store the last update check time and ensure boot check completed is true
                            Date d = new Date();
                            PreferenceManager.getDefaultSharedPreferences(this).edit()
                                    .putLong(Constants.LAST_UPDATE_CHECK_PREF, d.getTime())
                                    .putBoolean(Constants.BOOT_CHECK_COMPLETED, true)
                                    .apply();
                            //设置有更新标志位，用于后面上传升级结果
                            Log.d(TAG, "hasNewVersion " + info.getVersion());
                            if(info.getForceUpdate()){
                                if(info.getSilentUpdate()){
                                    if (!DownloadManager.getDefault(this).isDownloading() && !SystemProperties.get("persist.sys.update.silence","").contains("silence")) {
                                        DownloadManager.getDefault(this).setHandleUpdateType(false);
                                        DownloadManager.getDefault(this).download(info);
                                    }
                                }else{
                                    if (!DownloadManager.getDefault(this).isDownloading() && !ForceUpdateManager.getInstance().getforceUpdate()) {
                                        Intent intent = new Intent("android.settings.ConfirmDownloadDialog");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("force", true);
                                        intent.putExtra(Intent.EXTRA_INTENT, (UpdateInfo) results.get(0));
                                        startActivity(intent);
                                    }
                                }
                            }else{
                                /*if (!DownloadManager.getDefault(this).isDownloading()) {
                                    Intent intent = new Intent("android.settings.ConfirmDownloadDialog");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("force", false);
                                    intent.putExtra(Intent.EXTRA_INTENT, (UpdateInfo) results.get(0));
                                    startActivity(intent);
                                }*/
                                mCheckHandler.sendEmptyMessage(0);
                            }
                        } else {
                            if (version.equals(info.getVersion())) {
                                String path = prefs.getString(UpdateUtil.KEY_UPDATE_FILE_PATH, "");
                                Log.d(TAG, "SharedPreferences version " + path);
                                //verifyPackage(path, version);
                                if(info.getForceUpdate()){
                                    if(info.getSilentUpdate()){
                                        if (!DownloadManager.getDefault(this).isDownloading() && !SystemProperties.get("persist.sys.update.silence","").contains("silence")) {
                                            DownloadManager.getDefault(this).download(info);
                                        }
                                    }else{
                                        if (!DownloadManager.getDefault(this).isDownloading() && !ForceUpdateManager.getInstance().getforceUpdate()) {
                                            Intent intent = new Intent("android.settings.ConfirmDownloadDialog");
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            intent.putExtra("force", true);
                                            intent.putExtra(Intent.EXTRA_INTENT, (UpdateInfo) results.get(0));
                                            startActivity(intent);
                                        }
                                    }
                                }else{
                                    /*if (!DownloadManager.getDefault(this).isDownloading()) {
                                        Intent intent = new Intent("android.settings.ConfirmDownloadDialog");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("force", false);
                                        intent.putExtra(Intent.EXTRA_INTENT, (UpdateInfo) results.get(0));
                                        startActivity(intent);
                                    }*/
                                    mCheckHandler.sendEmptyMessage(0);
                                }
                            } else {
                                Log.d(TAG, "no new version");
                            }
                        }
                    }
                    if (isSilentUpdate || scheduled_update) {
                        // 开启定时器更新
                        try {
                            int value =  Integer.parseInt(updateFrequency);
                            Log.d(TAG, "updateFrequency " + value);
                            UpdateUtil.scheduleUpdateService(mContext, value * 1000);
                        } catch(NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else if (isSilentUpdate || scheduled_update) {
                // 开启定时器更新
                try {
                    int value =  Integer.parseInt(updateFrequency);
                    Log.d(TAG, "updateFrequency " + value);
                    UpdateUtil.scheduleUpdateService(mContext, value * 1000);
                } catch(NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return;
        } else if (action.equals(UpdateSettings.TIMINGUPDATECHECK)) {
	    //定点更新
            Log.d(TAG,"Do update---------------TIMINGUPDATECHECK");
            if (isWifiOrInternet() != null) {
                if (isWifiOrInternet().equals("wifi")) {
                    //直接下载升级
                    doUpdate();
                } else {
                    //消息通知
                    showNotification(mContext);
                }
            }
	} else if (action.equals(UpdateSettings.BACKGROUND_CHECK)) {
            boolean mIsExpired = bundle.getBoolean("mIsExpired");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            scheduled_update = prefs.getBoolean(UpdateSettings.KEY_SCHEDULED_CHECK, mContext.getResources().getBoolean(R.bool.config_scheduled_update));
            if(isSilentUpdate || scheduled_update) {
                try {
                    String updateFrequency = isSilentUpdate ? "28800" : prefs.getString(UpdateSettings.KEY_UPDATE_TIMEOUT_PREFS, getResources().getString(R.string.config_scheduled_update_time));
		    if(SystemProperties.get("persist.sys.urv.scheduled.update.frequency", "0").equals("0")){
			updateFrequency = prefs.getString(UpdateSettings.KEY_UPDATE_TIMEOUT_PREFS, getResources().getString(R.string.config_scheduled_update_time));
			if(updateFrequency.compareTo("21600")<0){
                            updateFrequency = "86400";
                    	}
            	    }else{
			updateFrequency = prefs.getString(UpdateSettings.KEY_UPDATE_TIMEOUT_PREFS, SystemProperties.get("persist.sys.urv.scheduled.update.frequency", "0"));
	            }
                    int value =  Integer.parseInt(updateFrequency);
                    Log.d(TAG, "mIsExpired updateFrequency " + value);
                    UpdateUtil.scheduleUpdateService(mContext, value * 1000);
                } catch(NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            String address = downloadManager.getServerUrl();
            if (address != null) {
                List<UpdateInfo> results = UpdateUtil.getUpdateInfo(UpdateUtil.buildUrl(
                        NotificationService.this, address));
                if(Build.PWV_CUSTOM_CUSTOM.equals("UTE") || Build.PWV_CUSTOM_CUSTOM.equals("UTEWO")) {
                    if(results == null || results.size() <= 0) {
                        results = UpdateUtil.getUpdateInfo(address);
                    }
                } else if(address.startsWith("https")){
                    results = UpdateUtil.getUpdateInfo(UpdateUtil.buildUrl(
                        NotificationService.this, address),mContext);
                }else{
                    results = UpdateUtil.getUpdateInfo(UpdateUtil.buildUrl(
                        NotificationService.this, address));
                }
                if (results != null && results.size() > 0) {
                    UpdateInfo info = results.get(0);
                    String version = prefs.getString(UpdateUtil.KEY_UPDATE_FILE_VERSION, "");
                    Log.d(TAG, "SharedPreferences version " + version);
                    if (info != null && info.hasNewVersion() && !version.equals(info.getVersion())) {
                        /*
                         * Intent intent = new Intent(this,
                         * UpdateViewActivity.class);
                         * intent.putExtra(Intent.EXTRA_INTENT,
                         * (UpdateInfo)results.get(0));
                         * intent.putExtra("AUTO_DOWNLOAD", true);
                         * startActivity(intent);
                         */
                        // Store the last update check time and ensure boot check completed is true
                        Date d = new Date();
                        PreferenceManager.getDefaultSharedPreferences(this).edit()
                                .putLong(Constants.LAST_UPDATE_CHECK_PREF, d.getTime())
                                .putBoolean(Constants.BOOT_CHECK_COMPLETED, true)
                                .apply();
                        boolean download_update = prefs.getBoolean(
                                UpdateSettings.KEY_AUTO_DOWNLOAD_UPDATE,
                                NotificationService.this.getResources().getBoolean(
                                        R.bool.config_confirm_download_update));
                        Log.d(TAG,"save LAST_UPDATE_CHECK_PREF: " + d.getTime());
                        if(info.getForceUpdate()){
                            if(info.getSilentUpdate()){
                                if (!DownloadManager.getDefault(this).isDownloading() && !SystemProperties.get("persist.sys.update.silence","").contains("silence")) {
                                    DownloadManager.getDefault(this).setHandleUpdateType(false);
                                    DownloadManager.getDefault(this).download(info);
                                }
                            }else{
                                if (!DownloadManager.getDefault(this).isDownloading() && !ForceUpdateManager.getInstance().getforceUpdate()) {
                                    Intent intent = new Intent("android.settings.ConfirmDownloadDialog");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("force", true);
                                    intent.putExtra(Intent.EXTRA_INTENT, (UpdateInfo) results.get(0));
                                    startActivity(intent);
                                }
                            }
                        }else{
                            /*if (!DownloadManager.getDefault(this).isDownloading()) {
                                Intent intent = new Intent("android.settings.ConfirmDownloadDialog");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("force", false);
                                intent.putExtra(Intent.EXTRA_INTENT, (UpdateInfo) results.get(0));
                                startActivity(intent);
                            }*/
                            mCheckHandler.sendEmptyMessage(0);
                        }
                    } else {
                        if (version.equals(info.getVersion())) {
                            String path = prefs.getString(UpdateUtil.KEY_UPDATE_FILE_PATH, "");
                            Log.d(TAG, "SharedPreferences version " + path);
                            if(info.getForceUpdate()){
                                if(info.getSilentUpdate()){
                                    if (!DownloadManager.getDefault(this).isDownloading() && !SystemProperties.get("persist.sys.update.silence","").contains("silence")) {
                                        DownloadManager.getDefault(this).download(info);
                                    }
                                }else{
                                    if (!DownloadManager.getDefault(this).isDownloading() && !ForceUpdateManager.getInstance().getforceUpdate()) {
                                        Intent intent = new Intent("android.settings.ConfirmDownloadDialog");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.putExtra("force", true);
                                        intent.putExtra(Intent.EXTRA_INTENT, (UpdateInfo) results.get(0));
                                        startActivity(intent);
                                    }
                                }
                            }else{
                                /*if (!DownloadManager.getDefault(this).isDownloading()) {
                                    Intent intent = new Intent("android.settings.ConfirmDownloadDialog");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("force", false);
                                    intent.putExtra(Intent.EXTRA_INTENT, (UpdateInfo) results.get(0));
                                    startActivity(intent);
                                }*/
                                mCheckHandler.sendEmptyMessage(0);
                            }
                        } else {
                            Log.d(TAG, "no new version");
                        }
                    }
                }
            }
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            processMessage(msg);
            NotificationReceiver.finishStartingService(NotificationService.this, msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        mContext = this;

        // add for silent update
        //String enableSilentUpdate = new android.device.DeviceManager().getSettingProperty("System-enable_silent_update");
	String enableSilentUpdate = Settings.System.getString(mContext.getContentResolver() ,"enable_silent_update");
        isSilentUpdate = (enableSilentUpdate != null && enableSilentUpdate.equals("true")) ? true : false;

        HandlerThread thread = new HandlerThread("NotificationService",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        downloadManager = DownloadManager.getDefault(this);

        String address = downloadManager.getServerUrl();
        Log.d(TAG, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.obj = intent.getExtras();
            mServiceHandler.sendMessage(msg);
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mServiceLooper.quit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
