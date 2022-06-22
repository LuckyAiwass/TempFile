package com.ubx.appinstall.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import com.ubx.appinstall.util.SharedPrefsStrListUtil;
import com.ubx.appinstall.util.ULog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.PackageInfo;

public class StartActivityService extends Service {

    private List<String> pmList = new ArrayList<>();
    private int i = 0;
    private Handler mBootHandler = new BootHandler(StartActivityService.this);

    class BootHandler extends Handler {
        private WeakReference<StartActivityService> mReference = null;

        public BootHandler(StartActivityService context) {
            mReference = new WeakReference<StartActivityService>(context);
        }

        @Override
        public void handleMessage(Message msg) {
            StartActivityService context = mReference.get();
            if (context != null) {
                if (isInstalled((String) msg.obj)){
                    context.startApplication((String) msg.obj);
                }
                //context.doStartApplicationWithPackageName((String) msg.obj);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        ULog.d("  StartActivityService  onCreate ");
        pmList = SharedPrefsStrListUtil.getStrListValue(this, "POWERBOOT");
        startApp();
        return super.onStartCommand(intent, flags, startId);
    }

    public void startApp() {

        if (i < pmList.size()) {
            // new Handler().postDelayed(new Runnable(){
            // @Override
            // public void run() {
            // // TODO Auto-generated method stub
            ULog.d("pmList         +++++++++ " + pmList.get(i));
            Message msg = new Message();
            msg.obj = pmList.get(i);
            mBootHandler.sendMessageDelayed(msg, i*2000);
            i++;
            startApp();
            // }
            // }, 2000);
        }else{
            onDestroy();
            i = 0;
        }
    }


    private void startApplication(String packname){
        ULog.d("    startApplication  :   " + packname);
        PackageManager packageManager = getPackageManager();
        Intent intent = new Intent();
        intent = packageManager.getLaunchIntentForPackage(packname);
        startActivity(intent);
    }
    
    private boolean isInstalled(String pkgName) {
        List<PackageInfo> pinfo = getPackageManager().getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName.toLowerCase();
                if (pn.equals(pkgName.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
