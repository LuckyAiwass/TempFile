package com.ubx.usdk.profile;

import android.os.IBinder;
import android.os.RemoteException;

import com.ubx.usdk.LogUtil;
import com.ubx.usdk.profile.aidl.IRestrictionPolicy;

/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class RestrictionManager {

    private ProfileManager mProfileManager;
    private IRestrictionPolicy mRestrictionPolicy;

    protected RestrictionManager(ProfileManager profileManager) {
        this.mProfileManager = profileManager;
    }

    /**
     * @hide
     */
    protected void init() {
        if (mRestrictionPolicy == null && mProfileManager != null && mProfileManager.getIProfileManager() != null) {
            try {
                IBinder binder = mProfileManager.getIProfileManager().getRestrictionIBinder();
                if (binder != null) {
                    mRestrictionPolicy = IRestrictionPolicy.Stub.asInterface(binder);
                }
            } catch (RemoteException e) {
                LogUtil.e("RestrictionManager::init", e);
            }
        }
    }

    /**
     * @hide
     */
    protected void release() {
        mRestrictionPolicy = null;
    }

    public boolean setSettingProperty(String name, String value) {
        if (mRestrictionPolicy != null) {
            try {
                return mRestrictionPolicy.setSettingProperty(name, value);
            } catch (RemoteException e) {
                LogUtil.e("setSettingProperty", e);
            }
        }
        return false;
    }

    public String getSettingProperty(String name) {
        if (mRestrictionPolicy != null) {
            try {
                return mRestrictionPolicy.getSettingProperty(name);
            } catch (RemoteException e) {
                LogUtil.e("getSettingProperty", e);
            }
        }
        return null;
    }

    public int getRestrictionPolicy(int action) {
        if (mRestrictionPolicy != null) {
            try {
                return mRestrictionPolicy.getRestrictionPolicy(action);
            } catch (RemoteException e) {
                LogUtil.e("getRestrictionPolicy", e);
            }
        }
        return -1;
    }

    public int setRestrictionPolicy(int faction, int status) {
        if (mRestrictionPolicy != null) {
            try {
                return mRestrictionPolicy.setRestrictionPolicy(faction, status);
            } catch (RemoteException e) {
                LogUtil.e("setRestrictionPolicy", e);
            }
        }
        return -1;
    }


    public void setUserRestriction(String key, boolean value) {
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setUserRestriction(key, value);
            } catch (RemoteException e) {
                LogUtil.e("setUserRestriction", e);
            }
        }
    }

    public boolean hasUserRestriction(String restrictionKey) {
        if (mRestrictionPolicy != null) {
            try {
                return mRestrictionPolicy.hasUserRestriction(restrictionKey);
            } catch (RemoteException e) {
                LogUtil.e("hasUserRestriction", e);
            }
        }
        return false;
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     