package com.ubx.usdk.profile;

import android.content.ComponentName;
import android.os.IBinder;
import android.os.RemoteException;

import com.ubx.usdk.LogUtil;
import com.ubx.usdk.profile.aidl.ISecurityPolicy;

/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class SecurityManager {

    private ProfileManager mProfileManager;
    private ISecurityPolicy mSecurityPolicy;

    protected SecurityManager(ProfileManager profileManager) {
        this.mProfileManager = profileManager;
    }

    /**
     * @hide
     */
    protected void init() {
        if (mSecurityPolicy == null && mProfileManager != null && mProfileManager.getIProfileManager() != null) {
            try {
                IBinder binder = mProfileManager.getIProfileManager().getSecurityPolicyIBinder();
                if (binder != null) {
                    mSecurityPolicy = ISecurityPolicy.Stub.asInterface(binder);
                }
            } catch (RemoteException e) {
                LogUtil.e("SecurityManager::init", e);
            }
        }
    }

    /**
     * @hide
     */
    protected void release() {
        mSecurityPolicy = null;
    }

    public void saveLockPattern(String pattern) {
        if (mSecurityPolicy != null) {
            try {
                mSecurityPolicy.saveLockPattern(pattern);
            } catch (RemoteException e) {
                LogUtil.e("saveLockPattern", e);
            }
        }
    }

    public void saveLockPassword(String password, int quality) {
        if (mSecurityPolicy != null) {
            try {
                mSecurityPolicy.saveLockPassword(password, quality);
            } catch (RemoteException e) {
                LogUtil.e("saveLockPassword", e);
            }
        }
    }

    public void clearLock() {
        if (mSecurityPolicy != null) {
            try {
                mSecurityPolicy.clearLock();
            } catch (RemoteException e) {
                LogUtil.e("clearLock", e);
            }
        }
    }

    public void setForceLockScreen(boolean lock) {
        if (mSecurityPolicy != null) {
            try {
                mSecurityPolicy.setForceLockScreen(lock);
            } catch (RemoteException e) {
                LogUtil.e("setForceLockScreen", e);
            }
        }
    }

    public void setLockScreenDisabled(boolean disable) {
        if (mSecurityPolicy != null) {
            try {
                mSecurityPolicy.setLockScreenDisabled(disable);
            } catch (RemoteException e) {
                LogUtil.e("setLockScreenDisabled", e);
            }
        }
    }

    public boolean isLockScreenDisabled() {
        if (mSecurityPolicy != null) {
            try {
                return mSecurityPolicy.isLockScreenDisabled();
            } catch (RemoteException e) {
                LogUtil.e("isLockScreenDisabled", e);
            }
        }
        return false;
    }

    public void setDeviceOwner(ComponentName name) {
        if (mSecurityPolicy != null) {
            try {
                mSecurityPolicy.setDeviceOwner(name);
            } catch (RemoteException e) {
                LogUtil.e("setDeviceOwner", e);
            }
        }
    }

    public boolean isDeviceOwner(String packageName) {
        if (mSecurityPolicy != null) {
            try {
                return mSecurityPolicy.isDeviceOwner(packageName);
            } catch (RemoteException e) {
                LogUtil.e("isDeviceOwner", e);
            }
        }
        return false;
    }

    public void cleanDeviceOwner(String packageName) {
        if (mSecurityPolicy != null) {
            try {
                mSecurityPolicy.cleanDeviceOwner(packageName);
            } catch (RemoteException e) {
                LogUtil.e("cleanDeviceOwner", e);
            }
        }
    }

    public String getDeviceOwner() {
        if (mSecurityPolicy != null) {
            try {
                return mSecurityPolicy.getDeviceOwner();
            } catch (RemoteException e) {
                LogUtil.e("getDeviceOwner", e);
            }
        }
        return null;
    }

}
