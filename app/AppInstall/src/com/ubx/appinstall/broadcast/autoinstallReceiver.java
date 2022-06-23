package com.ubx.appinstall.broadcast;

import com.ubx.appinstall.activity.MainActivity;
import com.ubx.appinstall.service.AutoInstallService;
import com.ubx.appinstall.service.StartActivityService;
import com.ubx.appinstall.util.ULog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import com.ubx.appinstall.util.AutoInstallConfig;
import com.ubx.appinstall.activity.OTGActivity;
import com.ubx.appinstall.util.SharedPrefsStrListUtil;
import android.content.pm.PackageManager;


public class autoinstallReceiver extends BroadcastReceiver {

    private static final String KEY_AUTO_INSTALL_APPS = "persist.auto.install.apps";
    private static final String ACTION_AUTO_INSTALL = "com.action.autoinstall";
    private static final String ACTION_COPY_FINISH = "android.intent.action.autoinstall";
    private static final String ACTION_MEDIA_MOUNT = "android.intent.action.MEDIA_MOUNTED";
    private static final String ACTION_DOWNLOAD_COMPLETE = "android.intent.action.DOWNLOAD_COMPLETE";
    private static final String ACTION_POWERBOOT_COMPLETE = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String action = intent.getAction();
        ULog.d("autoinstallReceiver ------------------------------------> is Receiver action:" + action);
        String isCheck = Settings.System.getString(context.getContentResolver(), KEY_AUTO_INSTALL_APPS);
        ULog.d("isCheck   --------------------------->" + isCheck);
        if (action.equals(ACTION_AUTO_INSTALL)) {
            SharedPrefsStrListUtil.putStringValue(context, "islogcat", "false");
            if ("true".equals(isCheck)) {
                Intent intent1 = new Intent(context, AutoInstallService.class);
                intent1.putExtra("bootStart", true);
                context.startService(intent1);
            }else{
                return;
            }
        /*  }else if (action.equals(ACTION_AUTO_INSTALL)){
            boolean status = intent.getBooleanExtra("status", false);
            Settings.System.putString(context.getContentResolver(), KEY_AUTO_INSTALL_APPS, "" + status);
            
            ULog.d("isCheck   --------------------------->" + Settings.System.getString(context.getContentResolver(), KEY_AUTO_INSTALL_APPS));
            */
        }else if(action.equals(ACTION_COPY_FINISH)){
            ULog.d("start to copy");
            InputStream is = null;
            OutputStream out = null;
            SharedPrefsStrListUtil.clear(context);
            try{
               File root = context.getFilesDir();
               String rootPath = root.toString();
               String newpath = rootPath.substring(0, rootPath.lastIndexOf("/"));
               File dest = new File(newpath + "/shared_prefs/AutoInstall.xml");
               File source = new File(AutoInstallConfig.AUTOINSTALLPATH + "/AutoInstall.xml");
               ULog.d("source.path() = " + newpath + "/shared_prefs/AutoInstall.xml");
               ULog.d("source.exists()" + source.exists());
               File file = new File(newpath, "shared_prefs");
               if (!file.exists()){
                    file.mkdir();
                }
               if(source.exists()){
                    ULog.d("copying!");
                    is = new FileInputStream(source);
                    out = new FileOutputStream(dest);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) > 0) {
                        out.write(buffer, 0, bytesRead);
                    }
                    dest.setWritable(true);
                    dest.setReadable(true);
                    is.close();
                    out.close();
                    ULog.d("success!");
               }
            }catch (IOException e) {
                e.printStackTrace();
            }
            String value = SharedPrefsStrListUtil.getStringValue(context,"isCheck","false");
            Settings.System.putString(context.getContentResolver(), KEY_AUTO_INSTALL_APPS, value);
        }else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)){
                      /*  PackageManager packageManager = context.getPackageManager();
                        if(!packageManager.isSafeMode()){
                Intent intent2 = new Intent(context, OTGActivity.class);
                intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent2);
                        }*/
        }/*else if(action.equals(ACTION_DOWNLOAD_COMPLETE)){
            Intent intent_download_complete = new Intent(context, AutoInstallService.class);
                        context.startService(intent_download_complete);
        }*/

        /* else if(action.equals(ACTION_POWERBOOT_COMPLETE)){
            ULog.d("ACTION_POWERBOOT_COMPLETE in autoinstallReceiver");
            Intent intent_powerboot = new Intent(context, AutoInstallService.class);
            context.startService(intent_powerboot);
        } */
        
    }

}
