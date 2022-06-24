package com.ubx.usdk.profile;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.IPackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Handler;
import android.database.ContentObserver;
import android.content.ContentResolver;

import com.android.internal.statusbar.IStatusBarService;

import android.os.UserHandle;
import android.app.StatusBarManager;
import android.app.IProcessObserver;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.text.TextUtils;
import android.util.Log;
import java.util.List;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.net.Uri;

import com.ubx.usdk.LogUtil;
import com.ubx.usdk.profile.aidl.IProfileManager;

import android.content.ComponentName;
import android.hardware.camera2.CameraManager;
import android.hardware.Camera;

public class USDKProfileService extends Service {
    private Context mContext;

    private ProfileManagerStub mProfileManagerStub;

    private ApplicationImpl mApplicationImpl;

    private DeviceControlImpl mDeviceControlImpl;

    private NetworkImpl mNetworkImpl;

    private RestrictionImpl mRestrictionImpl;

    private SecurityImpl mSecurityImpl;

    private Handler mWorkHander = new Handler();

    private SettingsObserver mSettingsObserver;

    private SuspensionBtSettingsObserver mSuspensionBtSettingsObserver;

    private IStatusBarService mStatusBarService;

    private IBinder mToken = new Binder();


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        mSettingsObserver = new SettingsObserver(mWorkHander);
        mSettingsObserver.observe();

        mSuspensionBtSettingsObserver = new SuspensionBtSettingsObserver(mWorkHander);
        mSuspensionBtSettingsObserver.observe();

        try {
            boolean scanbtn = android.device.provider.Settings.System.getInt(mContext.getContentResolver(),
                    android.device.provider.Settings.System.SUSPENSION_BUTTON, 0) == 1;
            Intent scanIntent = new Intent();
            scanIntent.setComponent(new ComponentName("com.android.usettings", "com.android.usettings.FxService"));
            if (scanbtn) {
                mContext.startService(scanIntent);
            } else {
                mContext.stopService(scanIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        int statusBarFlags = android.provider.Settings.System.getInt(mContext.getContentResolver(),
                "UROVO_STATUSBAR_ENABLE", StatusBarManager.DISABLE_NONE);
        try {
            mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService(Context.STATUS_BAR_SERVICE));
            if (mStatusBarService != null) {
                mStatusBarService.disable(statusBarFlags, mToken, mContext.getPackageName());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            ActivityManagerNative.getDefault().registerProcessObserver(mProcessObserver);
        } catch(RemoteException e){
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        LogUtil.d("USDKProfileService:onBind");
        if (mProfileManagerStub == null) {
            mProfileManagerStub = new ProfileManagerStub();
        }
        return mProfileManagerStub;
    }

    final public class ProfileManagerStub extends IProfileManager.Stub {
        @Override
        public String getVersion() throws RemoteException {
            PackageManager packageManager = getPackageManager();
            try {
                PackageInfo packageInfo = packageManager.getPackageInfo(
                        getPackageName(), 0);
                return packageInfo.versionName;
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        public IBinder getApplicationIBinder() throws RemoteException {
            if (mApplicationImpl == null) {
                mApplicationImpl = new ApplicationImpl(mContext);
            }
            return mApplicationImpl;
        }

        @Override
        public IBinder getDeviceControlIBinder() throws RemoteException {
            if (mDeviceControlImpl == null) {
                mDeviceControlImpl = new DeviceControlImpl(mContext);
            }
            return mDeviceControlImpl;
        }

        @Override
        public IBinder getNetworkPolicyIBinder() throws RemoteException {
            if (mNetworkImpl == null) {
                mNetworkImpl = new NetworkImpl(mContext);
            }
            return mNetworkImpl;
        }

        @Override
        public IBinder getRestrictionIBinder() throws RemoteException {
            if (mRestrictionImpl == null) {
                mRestrictionImpl = new RestrictionImpl(mContext);
            }
            return mRestrictionImpl;
        }

        @Override
        public IBinder getSecurityPolicyIBinder() throws RemoteException {
            if (mSecurityImpl == null) {
                mSecurityImpl = new SecurityImpl(mContext);
            }
            return mSecurityImpl;
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();

            resolver.registerContentObserver(android.provider.Settings.System.getUriFor("UROVO_STATUSBAR_ENABLE"), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            try {
                if (uri.toString().endsWith("UROVO_STATUSBAR_ENABLE")) {
                    int statusBarFlags = android.provider.Settings.System.getInt(mContext.getContentResolver(),
                            "UROVO_STATUSBAR_ENABLE", StatusBarManager.DISABLE_NONE);

                    mStatusBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService(Context.STATUS_BAR_SERVICE));
                    if (mStatusBarService != null) {
                        mStatusBarService.disable(statusBarFlags, mToken, mContext.getPackageName());
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class SuspensionBtSettingsObserver extends ContentObserver {
        SuspensionBtSettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(android.device.provider.Settings.System.getUriFor(android.device.provider.Settings.System.SUSPENSION_BUTTON), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            try {
                if (uri.toString().endsWith(android.device.provider.Settings.System.SUSPENSION_BUTTON)) {
                    boolean scanbtn = android.device.provider.Settings.System.getInt(mContext.getContentResolver(),
                            android.device.provider.Settings.System.SUSPENSION_BUTTON, 0) == 1;
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.android.usettings", "com.android.usettings.FxService"));
                    if (scanbtn) {
                        mContext.startService(intent);
                    } else {
                        mContext.stopService(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private IProcessObserver mProcessObserver = new IProcessObserver.Stub() {

        @Override
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {

            if(!foregroundActivities){
                return;
            }
            String lockTaskPackage = android.provider.Settings.System.getString(mContext.getContentResolver(), "lockTaskPackage");
            if (mApplicationImpl == null) {
                mApplicationImpl = new ApplicationImpl(mContext);
            }
            try{
            List<ActivityManager.RunningAppProcessInfo> processesList = mApplicationImpl.getRunningAppProcesses();
            if(!TextUtils.isEmpty(lockTaskPackage) && processesList != null && processesList.size() != 0) {
                //ActivityManager.RunningAppProcessInfo info = processesList.get(0);
                for(ActivityManager.RunningAppProcessInfo info:processesList) {
                    if(info.pid == pid && info.processName.equals(lockTaskPackage)) {
                        Log.i("TAG", "onForegroundActivitiesChanged::processName: " + info.processName);
                        mApplicationImpl.setLockTaskMode(lockTaskPackage, true);
                    }
                }
            }
            }catch(RemoteException e){}
        }

        @Override
        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) {}

        @Override
        public void onProcessDied(int pid, int uid) {
        }

    };
}
