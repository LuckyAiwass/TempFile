package com.ubx.scanwedge.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.os.Build;
import java.util.Map;
import android.os.SystemProperties;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import java.util.List;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rocky on 18-11-12.
 */

public class ScanWedgeService extends Service {
    private final static String TAG =ScanWedgeService.class
            .getSimpleName();
    private int mCurrentUserId = UserHandle.USER_OWNER;
    private BroadcastReceiver bootReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "action " + intent.getAction());
            if(Intent.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                final int changedUser = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, UserHandle.USER_NULL);
                Log.v(TAG, "userId " + mCurrentUserId + " changedUser "  + changedUser + " is in the house");
                /*if(changedUser != mCurrentUserId) {
                    userSwitched(changedUser);
                }*/
                mCurrentUserId = changedUser;
                ScanWedgeApplication mainApplication = (ScanWedgeApplication) getApplication();
                mainApplication.getServiceProxyWrapper();
            } else if (Intent.ACTION_USER_UNLOCKED.equals(intent.getAction())) {
                // Unlocking the system user may require a refresh
                int userId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, UserHandle.USER_NULL);
                Log.v(TAG, "mCurrentUserId " + mCurrentUserId + " userId "  + userId + " is in the house");
            } else if(Intent.ACTION_USER_BACKGROUND.equals(intent.getAction())) {
                int userId = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, UserHandle.USER_NULL);
                Log.v(TAG, "mCurrentUserId " + mCurrentUserId + " userId "  + userId + " is in the house");
                ScanWedgeApplication mainApplication = (ScanWedgeApplication) getApplication();
                mainApplication.disconnect();
            }
        }
    };
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int userId = ActivityManager.getCurrentUser();
        Log.d(TAG, "onStartCommand ScanWedgeService mCurrentUserId " + mCurrentUserId + " userId "  + userId);
        if(userId == UserHandle.USER_OWNER) {
            ScanWedgeApplication mainApplication = (ScanWedgeApplication) getApplication();
            mainApplication.getServiceProxyWrapper();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind ScanWedgeService tid " + Thread.currentThread().getId() +  " pid " + Binder.getCallingPid() + " Uid " + Binder.getCallingUid());
        ScanWedgeApplication mainApplication = (ScanWedgeApplication) getApplication();
        return mainApplication.getScanWedgeCoreImpl().onBind(intent);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Log.d(TAG, "onDestroy ScanWedgeService");
        unregisterReceiver(bootReceiver);
       /* MainApplication mainApplication = (MainApplication) getApplication();
        mainApplication.getScanManagerCore().destroyInstance();*/
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind ScanWedgeService tid " + Thread.currentThread().getId() +  " pid " + Binder.getCallingPid() + " Uid " + Binder.getCallingUid() );
        ScanWedgeApplication mainApplication = (ScanWedgeApplication) getApplication();
        mainApplication.getScanWedgeCoreImpl().onUnbind(intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate ScanWedgeService");
        ScanWedgeApplication mainApplication = (ScanWedgeApplication) getApplication();
        mainApplication.getScanWedgeCoreImpl();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_STARTED);
        filter.addAction(Intent.ACTION_USER_BACKGROUND);
        filter.addAction(Intent.ACTION_USER_FOREGROUND);
        filter.addAction(Intent.ACTION_USER_SWITCHED);
        filter.addAction(Intent.ACTION_USER_STARTING);
        filter.addAction(Intent.ACTION_LOCKED_BOOT_COMPLETED);
        //filter.addAction(Intent.ACTION_PRE_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_USER_UNLOCKED);
        filter.addAction(Intent.ACTION_USER_INITIALIZE);
        registerReceiverAsUser(bootReceiver, UserHandle.ALL, filter, null, null);
    }

}
