package com.ubx.usdk.profile;

import android.content.ComponentName;
import android.content.pm.IPackageInstallObserver;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.ubx.usdk.LogUtil;
import com.ubx.usdk.profile.aidl.IApplicationPolicy;

import java.util.List;

/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class ApplicationManager {

    private ProfileManager mProfileManager;
    private IApplicationPolicy mIApplicationPolicy;

    protected ApplicationManager(ProfileManager profileManager) {
        this.mProfileManager = profileManager;
    }

    /**
     * @hide
     */
    protected void init() {
        if (mIApplicationPolicy == null && mProfileManager != null && mProfileManager.getIProfileManager() != null) {
            try {
                IBinder binder = mProfileManager.getIProfileManager().getApplicationIBinder();
                if (binder != null) {
                    mIApplicationPolicy = IApplicationPolicy.Stub.asInterface(binder);
                }
            } catch (RemoteException e) {
                LogUtil.e("NetworkManager::init", e);
            }
        }
    }

    /**
     * @hide
     */
    protected void release() {
        mIApplicationPolicy = null;
    }


    public boolean setDefaultLauncher(ComponentName componentName) {
        if (mIApplicationPolicy != null) {
            try {
                return mIApplicationPolicy.setDefaultLauncher(componentName);
            } catch (RemoteException e) {
                LogUtil.e("setDefaultLauncher", e);
            }
        }
        return false;
    }

    public boolean removeDefaultLauncher(String packageName) {
        if (mIApplicationPolicy != null) {
            try {
                return mIApplicationPolicy.removeDefaultLauncher(packageName);
            } catch (RemoteException e) {
                LogUtil.e("removeDefaultLauncher", e);
            }
        }
        return false;
    }

    public boolean grantRuntimePermission(String packageName, String permName) {
        if (mIApplicationPolicy != null) {
            try {
                return mIApplicationPolicy.grantRuntimePermission(packageName, permName);
            } catch (RemoteException e) {
                LogUtil.e("grantRuntimePermission", e);
            }
        }
        return false;
    }

    public boolean revokeRuntimePermission(String packageName, String permName) {
        if (mIApplicationPolicy != null) {
            try {
                return mIApplicationPolicy.revokeRuntimePermission(packageName, permName);
            } catch (RemoteException e) {
                LogUtil.e("revokeRuntimePermission", e);
            }
        }
        return false;
    }

    public void setOpsUidMode(int code, int uid, int mode) {
        if (mIApplicationPolicy != null) {
            try {
                mIApplicationPolicy.setOpsUidMode(code, uid, mode);
            } catch (RemoteException e) {
                LogUtil.e("setOpsUidMode", e);
            }
        }
    }

    public double getAppPowerUsage(String packagename) {
        if (mIApplicationPolicy != null) {
            try {

                return mIApplicationPolicy.getAppPowerUsage(packagename);
            } catch (RemoteException e) {
                LogUtil.e("getAppPowerUsage", e);
            }
        }
        return -1;
    }

    public double getAllAppsPowerUsage() {
        if (mIApplicationPolicy != null) {
            try {
                return mIApplicationPolicy.getAllAppsPowerUsage();
            } catch (RemoteException e) {
                LogUtil.e("getAllAppsPowerUsage", e);
            }
        }
        return -1;
    }

    public Bundle getPowerUsage() {
        if (mIApplicationPolicy != null) {
            try {
                return mIApplicationPolicy.getPowerUsage();
            } catch (RemoteException e) {
                LogUtil.e("getPowerUsage", e);
            }
        }
        return null;
    }

    public boolean getApplicationEnabledSetting(String packageName) {
        if (mIApplicationPolicy != null) {
            try {
                return mIApplicationPolicy.getApplicationEnabledSetting(packageName);
            } catch (RemoteException e) {
                LogUtil.e("getApplicationEnabledSetting", e);
            }
        }
        return false;
    }

    public void setApplicationEnabledSetting(String packageName, boolean enable) {
        if (mIApplicationPolicy != null) {
            try {
                mIApplicationPolicy.setApplicationEnabledSetting(packageName, enable);
            } catch (RemoteException e) {
                LogUtil.e("setApplicationEnabledSetting", e);
            }
        }
    }


    public boolean forceStopPackage(String packageName) {
        if (mIApplicationPolicy != null) {
            try {
                return mIApplicationPolicy.forceStopPackage(packageName);
            } catch (RemoteException e) {
                LogUtil.e("setApplicationEnabledSetting", e);
            }
        }
        return false;
    }

    public boolean removeTask(int taskid) {
        if (mIApplicationPolicy != null) {
            try {
                return mIApplicationPolicy.removeTask(taskid);
            } catch (RemoteException e) {
                LogUtil.e("setApplicationEnabledSetting", e);
            }
        }
        return false;
    }

    public void setLockTaskMode(String packageName, boolean enable) {
        if (mIApplicationPolicy != null) {
            try {
                mIApplicationPolicy.setLockTaskMode(packageName, enable);
            } catch (RemoteException e) {
                LogUtil.e("setApplicationEnabledSetting", e);
            }
        }
    }

    public int installPackage(String apkFilePath, int installFlags, IPackageInstallObserver observer) {
        if (mIApplicationPolicy != null) {
            try {
                mIApplicationPolicy.installPackage(apkFilePath, installFlags, observer);
            } catch (RemoteException e) {
                LogUtil.e("setApplicationEnabledSetting", e);
            }
        }
        return 0;
    }
}
