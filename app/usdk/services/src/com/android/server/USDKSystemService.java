/* * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server;

import com.android.server.SystemService;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.pm.IPackageManager;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.util.Log;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.device.UFSManager;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.FileInputStream;

// urovo weiyu add on 2020-09-22 [s]
import android.net.NetworkInfo;
import android.net.ConnectivityManager;
// urovo weiyu add on 2020-09-22 [e]

import static android.os.IServiceManager.DUMP_FLAG_PRIORITY_DEFAULT;

public class USDKSystemService extends SystemService {
    static final String TAG = "USDKSystemService";
    static final boolean DEBUG = true;

    private Context mContext;
    private final USDKHandler mHandler;

    private static final int BIND_FAILED_RETRY_TIMES = 10;//if bindservice failed, retry times
    private static final int TIMEOUT_BIND_MS = 3000; //Maximum msec to wait for a bind
    private static final int MESSAGE_BIND_SERVICE = 100;
    private static final int MESSAGE_UFS_SERVICE = 101;

    // urovo add luolin begin 2018-12-03
    private ScanServiceProxy mScanner = null;
    // urovo add luolin end 2018-12-03

    //urovo zhoubo add begin 2020.11.11
    private UFSManager mUFSManager;

    public String ufsnumber = "";
    public String ufsprop = "";
    //urovo zhoubo add end 2020.11.11

    private final Map<String/*service package name*/, UsdkServiceConnections> mUsdkServices = new HashMap<>();

    public USDKSystemService(Context context) {
        super(context);
        mContext = context;

        mHandler = new USDKHandler(IoThread.get().getLooper());

        Intent profileIntent = new Intent("com.ubx.usdk.profileservice");
        profileIntent.setClassName("com.ubx.usdk.profile", "com.ubx.usdk.profile.USDKProfileService");

        mUsdkServices.put("com.ubx.usdk.profile", new UsdkServiceConnections(profileIntent, "USDKProfileManager"));
    }

    @Override
    public void onStart() {
        LOGD("onStart:");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        //filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addDataScheme("package");
        mContext.registerReceiver(mPackageAddedReceiver, filter);//listen USDKService package change

        mContext.registerReceiver(mBootcompleteReceiver, new IntentFilter(Intent.ACTION_BOOT_COMPLETED));

        mContext.registerReceiver(mNetworkChangedReceiver,new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));

            // urovo add by shenpidong begin 2020-05-22
            try {
                LOGD("Scan Service");
                mScanner = new ScanServiceProxy(mContext);
                publishBinderService(Context.SCAN_SERVICE, mScanner, false, DUMP_FLAG_PRIORITY_DEFAULT);//ServiceManager.addService
            } catch (Throwable e) {
                LOGD("starting Scan Service" + e);
            }
	         // urovo add by shenpidong end 2020-05-22
    }

    @Override
    public void onBootPhase(int phase) {
        LOGD("onBootPhase:" + phase);
        if (phase == PHASE_THIRD_PARTY_APPS_CAN_START) {

        } else if (phase == PHASE_BOOT_COMPLETED) {
            for (UsdkServiceConnections connections : mUsdkServices.values()) {// bind all USDKService
                connections.bindService();
            }
        }
    }

    private class USDKHandler extends Handler {
        USDKHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_BIND_SERVICE: {
                    UsdkServiceConnections connections = (UsdkServiceConnections) msg.obj;
                    removeMessages(MESSAGE_BIND_SERVICE, msg.obj);
                    if (connections != null) {
                        connections.bindService();
                    }
                    break;
                }
                case MESSAGE_UFS_SERVICE:{
                    removeMessages(MESSAGE_UFS_SERVICE);
                    if(runUFS()){
                        mUFSManager = new UFSManager(mContext);
                        mUFSManager.setUFS();
                        SystemProperties.set("persist.sys.ufs.status", ufsnumber);
                    }
                    break;
                }
            }
        }

    }


    private final class UsdkServiceConnections
            implements ServiceConnection, IBinder.DeathRecipient {
        IBinder mService;
        ComponentName mClassName;
        Intent mIntent;
        String ServiceTAG;
        int mBindFailedCount = 0;

        /**
         * UsdkServiceConnections
         * @param intent USDKService's intent
         * @param tag USDKService's TAG for ServiceManager.getService(TAG)
         */
        UsdkServiceConnections(Intent intent, String tag) {
            mService = null;
            mClassName = null;
            ServiceTAG = tag;
            mIntent = intent;
        }

        private boolean bindService() {
            if (mIntent != null && mService == null) {
                boolean ret = doBind(mIntent, this);
                if (ret) {
                    mBindFailedCount = 0;
                } else {
                    mBindFailedCount++;
                    LOGD("Unable to bind with intent: " + mIntent);
                }
                if (mBindFailedCount <= BIND_FAILED_RETRY_TIMES) {
                    Message msg = mHandler.obtainMessage(MESSAGE_BIND_SERVICE);
                    msg.obj = this;
                    mHandler.sendMessageDelayed(msg, TIMEOUT_BIND_MS);
                }
                return ret;
            }

            return false;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // remove timeout message
            mHandler.removeMessages(MESSAGE_BIND_SERVICE, this);
            mService = service;
            mClassName = className;
            try {
                mService.linkToDeath(this, 0);// listen USDKService's status
            } catch (RemoteException e) {
                LOGD("Unable to linkToDeath" + e);
            }
            try {
                publishBinderService(ServiceTAG, mService, false, DUMP_FLAG_PRIORITY_DEFAULT);//ServiceManager.addService
            } catch (Exception e) {
                LOGD("Unable to publishBinderService" + e);
            }
            LOGD("onServiceConnected " + className);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (mService == null) return;
            try {
                mService.unlinkToDeath(this, 0);
            } catch (NoSuchElementException e) {
                LOGD("Unable to unlinkToDeath" + e);
            }

            mService = null;
            mClassName = null;
        }

        @Override
        public void binderDied() {
            LOGD("USDK service : " + mClassName + " died.");
            onServiceDisconnected(mClassName);
            // Trigger rebind
            Message msg = mHandler.obtainMessage(MESSAGE_BIND_SERVICE);
            msg.obj = this;
            mHandler.sendMessageDelayed(msg, TIMEOUT_BIND_MS);
        }
    }

    /*
       bind usdk service
     */
    private boolean doBind(Intent intent, ServiceConnection conn) {
        boolean ret = false;
        long id = Binder.clearCallingIdentity();
        try {
            ret = mContext.bindServiceAsUser(intent, conn, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT, UserHandle.CURRENT);
        } catch (Exception e) {
            LOGD("Fail to bindServiceAsUser: " + intent+"\n"+e);
        }
        Binder.restoreCallingIdentity(id);
        return ret;
    }

    private BroadcastReceiver mPackageAddedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                String packageName = intent.getData().getSchemeSpecificPart();
                UsdkServiceConnections connections = mUsdkServices.get(packageName);//if package is USDKService, bind package's service
                if (connections != null) {
                    connections.bindService();
                }
            }
        }
    };

    // urovo weiyu add on 2020-08-17 [s]
    private BroadcastReceiver mNetworkChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"the network is connected!");
            NetworkInfo ni = (NetworkInfo) intent
                        .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            Intent mIntent = new Intent("udroid.action.network.changed");
            if (ni != null && ni.isConnected()) {
                mIntent.putExtra("isNetworkConnected",true);
            } else {
                mIntent.putExtra("isNetworkConnected",false);
            }
            mContext.sendBroadcast(mIntent);
        }
    };
    // urovo weiyu add on 2020-08-17 [e]

    private BroadcastReceiver mBootcompleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                LOGD("mBootcompleteReceiver");
                Message msg = mHandler.obtainMessage(MESSAGE_UFS_SERVICE);
                mHandler.sendMessageDelayed(msg, 3000);
                /*if(runUFS()){
                    mUFSManager
                    mUFSManager.setUFS();
                    SystemProperties.set("persist.sys.ufs.status", ufsnumber);
                }*/
                startAutoRunningApp();

            }
        }
    };

    private void startAutoRunningApp() {
        try {
            String lockTaskPackage = Settings.System.getString(mContext.getContentResolver(), "lockTaskPackage");
            Intent lockTaskIntent = new Intent(Intent.ACTION_MAIN);
            lockTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            lockTaskIntent.setPackage(lockTaskPackage);
            List<ResolveInfo> ri = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).queryIntentActivities(
                                      lockTaskIntent,lockTaskIntent.resolveTypeIfNeeded(mContext.getContentResolver()),0,0).getList();;

            if (ri != null && ri.size() > 0) {
                Intent appIntent = new Intent();
                appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appIntent.setClassName(lockTaskPackage, ri.get(0).activityInfo.name);
                mContext.startActivityAsUser(appIntent, UserHandle.CURRENT);
                new android.device.DeviceManager().setLockTaskMode(lockTaskPackage, true);
            }
        } catch(Exception e) {
            Log.d(TAG,"startLockTaskMode " + e.toString());
        }

        try {
            //开机默认启动Uhome服务
            String packageName = "com.urovo.uhome";
            String className = "com.urovo.uhome.service.UHomeService";
            ComponentName component = new ComponentName(packageName, className);
            Intent appIntent = new Intent();
            appIntent.setComponent(component);
            mContext.startServiceAsUser(appIntent, UserHandle.CURRENT);
        } catch(Exception e) {
            e.printStackTrace();
        }

        //开机默认启动应用
        try {
            String packageNames =  Settings.System.getString(mContext.getContentResolver(), "AutoRunningApp");
            if (!android.text.TextUtils.isEmpty(packageNames)) {
            String[] split = packageNames.split(",");
                for (int i = 0; i < split.length; i++) {
                    String ApppackageName = split[i];//default launcher package name
                    ComponentName component = ComponentName.unflattenFromString(ApppackageName);
                    if((component != null)) {
                        //Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
                        //intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
                        //intentToResolve.setPackage(packageName);
                        Intent appIntent = new Intent();
                        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appIntent.setComponent(component);
                        try {
                            mContext.startActivityAsUser(appIntent, UserHandle.CURRENT);
                        } catch(Exception e) {
                        }
                    }
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void LOGD(String msg) {
        if (DEBUG)
            Log.d(TAG, msg);
    }

    //-------------------------------------------------------UFS------------------------------------------begin----------------------------------
    private boolean runUFS(){
        ufsnumber = SystemProperties.get("ro.ufs.build.date.utc",""); //prop from UFS 
        ufsprop = SystemProperties.get("persist.sys.ufs.status",""); //prop determine whether to parse UFS
        Log.d(TAG,"setufs ============= ufsnumber ===="+ufsnumber + "          ufsprop ===="+ufsprop);
        if(!ufsprop.equals(ufsnumber)){
            return true;
        } else {
            return false;
        }
    }
    //-------------------------------------------------------UFS------------------------------------------end----------------------------------
}
