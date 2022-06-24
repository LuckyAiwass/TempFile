package com.ubx.usdk.profile;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.IWindowManager;

import com.android.internal.widget.LockPatternUtils;
import com.ubx.usdk.profile.aidl.ISecurityPolicy;

/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class SecurityImpl extends ISecurityPolicy.Stub {
	
	private final static String TAG = SecurityImpl.class.getSimpleName();
	
	private Context mContext;
	private ContentResolver mSystemCR;
	private LockPatternUtils mLockPatternUtils;
	private DevicePolicyManager mDevicePolicyManager;
	
	public SecurityImpl(Context context) {
		mContext = context;
		mSystemCR = mContext.getContentResolver();
		mLockPatternUtils = new LockPatternUtils(mContext);
		mDevicePolicyManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
	}
	
    @Override
    public void saveLockPattern(String pattern) throws RemoteException {
        String credential = Settings.Secure.getString(mSystemCR, "history_credential");
        android.util.Log.d("luoqi", "saveLockPassword ---->USER_SYSTEM  ------> credential:"+credential + "    pattern: "+pattern);
        byte[] patternBytes = pattern != null ? pattern.getBytes() : null;
        byte[] credentialBytes = credential != null ? credential.getBytes() : null;
        mLockPatternUtils.saveLockPattern(LockPatternUtils.byteArrayToPattern(patternBytes), credentialBytes, UserHandle.USER_SYSTEM);
        mLockPatternUtils.setVisiblePatternEnabled(true, UserHandle.USER_SYSTEM);
    }

    @Override
    public void saveLockPassword(String password, int quality) throws RemoteException {
        String credential = Settings.Secure.getString(mSystemCR, "history_credential");
        android.util.Log.d("luoqi", "saveLockPassword -> credential:"+credential + "    password: "+password);
        byte[] passwordBytes = password != null ? password.getBytes() : null;
        byte[] credentialBytes = credential != null ? credential.getBytes() : null;
        mLockPatternUtils.saveLockPassword(passwordBytes, credentialBytes, quality, UserHandle.USER_SYSTEM);
    }

    @Override
    public void clearLock() throws RemoteException {
        String credential = Settings.Secure.getString(mSystemCR, "history_credential");
        byte[] credentialBytes = credential != null ? credential.getBytes() : null;
        mLockPatternUtils.clearLock(credentialBytes, UserHandle.USER_SYSTEM);
    }

    @Override
    public void setForceLockScreen(boolean lock) throws RemoteException {
        if (lock)
            SystemProperties.set("persist.sys.forcelock", "true");
        else
            SystemProperties.set("persist.sys.forcelock", "false");

        if (lock) {
            try {
                ((PowerManager) mContext.getSystemService(Context.POWER_SERVICE)).goToSleep(SystemClock.uptimeMillis(),
                        PowerManager.GO_TO_SLEEP_REASON_DEVICE_ADMIN, 0);
                // Ensure the device is locked
                mLockPatternUtils.requireCredentialEntry(UserHandle.USER_ALL);
                IBinder b = ServiceManager.getService(Context.WINDOW_SERVICE);
                IWindowManager.Stub.asInterface(b).lockNow(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setLockScreenDisabled(boolean disable) throws RemoteException {
        mLockPatternUtils.setLockScreenDisabled(disable, UserHandle.USER_SYSTEM);
    }

    @Override
    public boolean isLockScreenDisabled() throws RemoteException {
        boolean disable = mLockPatternUtils.isLockScreenDisabled(UserHandle.USER_SYSTEM);
        return disable;
    }

    @Override
    public void setDeviceOwner(ComponentName name) throws RemoteException {
        try {
            if (!mDevicePolicyManager.isAdminActive(name)) {
                mDevicePolicyManager.setActiveAdmin(name, true);
            }
            if (!mDevicePolicyManager.isDeviceOwnerApp(name.getPackageName())) {
                mDevicePolicyManager.setDeviceOwner(name);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            Log.e(TAG, "setDeviceOwner  " + name + e);
        }
    }

    @Override
    public boolean isDeviceOwner(String packageName) throws RemoteException {
        boolean is = false;
        try {
            is = mDevicePolicyManager.isDeviceOwnerApp(packageName);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return is;
    }

    @Override
    public void cleanDeviceOwner(String packageName) throws RemoteException {
        try {
            mDevicePolicyManager.clearDeviceOwnerApp(packageName);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    @Override
    public String getDeviceOwner() throws RemoteException {
        String packageName = "";
        try {
            packageName = mDevicePolicyManager.getDeviceOwner();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return packageName;
    }
}
