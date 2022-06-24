package com.ubx.scanwedge.service;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.os.IScanServiceWrapper;

public class ServiceConnectionProxy {
    private static final String TAG = "wedge"
            + ServiceConnectionProxy.class
            .getSimpleName();
    private final static int MSG_SERVICE_STATUS = 0;
    private final static int MSG_DEVICE_STATUS = 1;
    private final static int MSG_QUIT = 2;
    private Context mContext;
    private Intent serviceIntent;
    private boolean mbWorkLonely = false;
    private volatile IScanServiceWrapper mService = null;
    private int mServiceStatus = -1;
    private OnServiceConnectedListener mListener = null;
    private HandlerThread mHandlerThread = null;
    private ServiceHandler mServiceHandler;
    private ServiceConnection mConnection = null;
    private static ServiceConnectionProxy mServiceConnectionProxy;

    public static ServiceConnectionProxy createInstance(Context context) {
        if (mServiceConnectionProxy != null)
            return mServiceConnectionProxy;
        try {
            mServiceConnectionProxy = new ServiceConnectionProxy(context);
            return mServiceConnectionProxy;
        } catch (Exception e) {
            Log.e(TAG, "createInstant failed:" + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private ServiceConnectionProxy(Context context) {
        mContext = context;
    }

    public static void destroy() {
        if (mServiceConnectionProxy != null) {
            mServiceConnectionProxy.disconnect();
        }
        mServiceConnectionProxy = null;
    }

    private boolean bindServiceLocked() {
        try {
            if (mConnection == null) {
                mConnection = new MyServiceConnection();
            }
            int mCurrentUserId = ActivityManager.getCurrentUser();
            Log.d(TAG, "bindServiceLocked mCurrentUserId= " + mCurrentUserId);
            //mContext.startServiceAsUser(serviceIntent, new UserHandle(mCurrentUserId));
            //mContext.bindServiceAsUser(serviceIntent, mConnection, Context.BIND_AUTO_CREATE, new UserHandle(mCurrentUserId));
            mContext.bindServiceAsUser(serviceIntent, mConnection, Context.BIND_AUTO_CREATE, UserHandle.OWNER);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "bindServicLocked " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean bindSevice() {
        synchronized (ServiceConnectionProxy.this) {
            return bindServiceLocked();
        }
    }

    private void unbindService() {
        synchronized (ServiceConnectionProxy.this) {
            unbindServiceLocked();
        }
    }

    private void createHandlerThreadLocked() {
        if (mServiceHandler == null) {
            mHandlerThread = new HandlerThread("proxy-handler");
            mHandlerThread.start();
            mServiceHandler = new ServiceHandler(mHandlerThread.getLooper());
        }
    }

    private void quitHandlerThreadLocked() {
        try {
            if(mServiceHandler != null) {
                mServiceHandler.sendMessageAtFrontOfQueue(mServiceHandler
                    .obtainMessage(MSG_QUIT));
            }
            if(mHandlerThread != null) {
                //synchronized (mHandlerThread) {
                mHandlerThread.join();
                //}
            }
        } catch (Exception e) {
            Log.e(TAG, "quitHandlerThread " + e.getMessage());
            e.printStackTrace();
        }

        mHandlerThread = null;
        mServiceHandler = null;
    }

    private void unbindServiceLocked() {
        try {
            //mContext.stopService(serviceIntent);
            if(mConnection != null) {
                mContext.unbindService(mConnection);
            }
        } catch (Exception e) {
            Log.e(TAG, "unbindServiceLocked " + e.getMessage());
            e.printStackTrace();
        }
        mConnection = null;
        mService = null;

    }

    private boolean isSDKCompatable(String serverVersion, String sdkVersion) {
        try {
            String[] serverVersions = serverVersion.split(".");
            String[] sdkVersions = sdkVersion.split(".");
            for (int i = 0; i < serverVersions.length; i++) {
                if (Integer.parseInt(serverVersions[i]) > Integer
                        .parseInt(sdkVersions[i]))
                    return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "isSDKCompatable failed:" + e.getMessage());
            return false;
        }
        return true;
    }

    private void notifyServiceStatus(int status) {
        Message serviceStatusMsg = mServiceHandler.obtainMessage(
                MSG_SERVICE_STATUS, status, 0);
        mServiceHandler.sendMessage(serviceStatusMsg);
    }

    public void disconnect() {
        synchronized (this) {
            //unregisterMySelfLocked();
            unbindServiceLocked();
            quitHandlerThreadLocked();
            mListener = null;
        }
    }

    private void checkServerPkgExist() {
        String targetPkg = "com.android.usettings";
        try {
            PackageManager pkm = mContext.getPackageManager();
            pkm.getPackageInfo(targetPkg, PackageManager.GET_SERVICES);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Unnable to find scannerServerApk, work lonely!!");
            mbWorkLonely = true;
        }
    }

    public int connect(Intent intent, OnServiceConnectedListener listener) {
        if (listener == null) {
            Log.e(TAG, "listener cannot be null");
            return -1;
        }
        if (intent == null) {
            Log.e(TAG, "intent cannot be null");
            return -2;
        }
        synchronized (this) {
            if (mService != null) {
                return 0;
            } else {
                //checkServerPkgExist();
                mListener = listener;
                serviceIntent = intent;
                createHandlerThreadLocked();
                return bindServiceLocked() ? 0 : -1;
            }
        }
    }


    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if (what == MSG_SERVICE_STATUS) {
                int status = msg.arg1;
                if (status == OnServiceConnectedListener.EVENT_SERVICE_VERSION_NOT_COMPATABLE) {
                    unbindService();
                }
                /*if (status == OnServiceConnectedListener.EVENT_SERVICE_CONNECTED) {
                    if (registerMySelf() == false) {
                        //Register failed,
                        status = OnServiceConnectedListener.EVENT_SERVICE_DISCONNECTED;
                        unbindService();
                    }
                }*/
                mListener.serviceEventNotify(status, mService);
                /*if (status == OnServiceConnectedListener.EVENT_SERVICE_DISCONNECTED) {
                    Log.e(TAG,
                            "Service is disconnected, try to reconnect it...");
                    bindSevice();
                }*/
            } else if (what == MSG_DEVICE_STATUS) {
            } else if (what == MSG_QUIT) {
                if (mHandlerThread != null) {
                    mHandlerThread.getLooper().quit();
                }
            }
        }
    }

    private class MyServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className, IBinder service) {
            synchronized (ServiceConnectionProxy.this) {
                try {
                    //设置死亡代理
                    service.linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                mService = IScanServiceWrapper.Stub.asInterface(service);
                notifyServiceStatus(OnServiceConnectedListener.EVENT_SERVICE_CONNECTED);
                /*try {
                    String version = mService.getServerVersion();
                    String sdkVersion = ServiceConnectionProxy.this.getSDKVersion();
                    boolean compatable = isSDKCompatable(version, sdkVersion);
                    Log.e(TAG, "service version:" + version + " sdk version:"
                            + sdkVersion + " compatable?" + compatable);
                    notifyServiceStatus(compatable ? OnServiceConnectedListener.EVENT_SERVICE_CONNECTED
                            : OnServiceConnectedListener.EVENT_SERVICE_VERSION_NOT_COMPATABLE);
                } catch (RemoteException e) {
                    Log.e(TAG, "onServiceConnected " + e.getMessage());
                    e.printStackTrace();
                    notifyServiceStatus(OnServiceConnectedListener.EVENT_SERVICE_VERSION_NOT_COMPATABLE);
                }*/
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            synchronized (ServiceConnectionProxy.this) {
                mService = null;
                notifyServiceStatus(OnServiceConnectedListener.EVENT_SERVICE_DISCONNECTED);
            }
        }
    }
    private IBinder.DeathRecipient deathRecipient=new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            //解绑
            if(mService!=null){
                mService.asBinder().unlinkToDeath(deathRecipient,0);
                mService=null;
            }
            Log.e(TAG,
                    "Service is binderDied...");
            //断开重新绑定
            bindServiceLocked();
        }
    };
    public void binderIsAlive() {
        if (mService != null) {
            mService.asBinder().isBinderAlive();
        }
    }
    public static interface OnServiceConnectedListener {
        /**
         * 注意： OnServiceConnectedListener回调函数将在非主线程中完成
         * 请客户端APP注意线程同步
         */

        static final public int EVENT_SERVICE_CONNECTED = 0;
        static final public int EVENT_SERVICE_DISCONNECTED = 1;
        static final public int EVENT_SERVICE_VERSION_NOT_COMPATABLE = 2;

        /**
         * serviceStatus:用于通知后台服务的情况
         * event:
         * SERVICE_CONNECTED：表示后台服务连接成功
         * SERVICE_DISCONNECTED：表示后台服务断开
         * SERVICE_VERSION_NOT_COMPATABLE：表示SDK版本不匹配
         **/
        public int serviceEventNotify(int event, IScanServiceWrapper serviceWrapper);
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
}
