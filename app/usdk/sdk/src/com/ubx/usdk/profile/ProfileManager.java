package com.ubx.usdk.profile;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

import com.ubx.usdk.LogUtil;
import com.ubx.usdk.USDKBaseManager;
import com.ubx.usdk.USDKManager;
import com.ubx.usdk.USDKManager.STATUS;
import com.ubx.usdk.USDKManager.FEATURE_TYPE;
import com.ubx.usdk.profile.aidl.IProfileManager;

public class ProfileManager extends USDKBaseManager implements USDKManager.StatusListener {

    private IProfileManager mIProfileManager;

    private ApplicationManager mApplicationManager;
    private DeviceControlManager mDeviceControlManager;
    private NetworkManager mNetworkManager;
    private RestrictionManager mRestrictionManager;
    private SecurityManager mSecurityManager;

    public ProfileManager(Context context) {
        super(context, USDKManager.FEATURE_TYPE.PROFILE);
        Intent intent = new Intent();
        intent.setPackage("com.ubx.usdk.profile");
        intent.setAction("com.ubx.usdk.profileservice");
        setIntent(intent);
        addStatusListener(this);
    }

    @Override
    public void onStatus(FEATURE_TYPE featureType, STATUS status) {
        if (status == STATUS.SUCCESS) {
            mIProfileManager = IProfileManager.Stub.asInterface(getIBinder());
            LogUtil.d("onStatus mIProfileManager:" + mIProfileManager);

            initManagers();
        } else {
            mIProfileManager = null;

            releaseManagers();
        }
    }

    public IProfileManager getIProfileManager() {
        return mIProfileManager;
    }

    @Override
    public void release() {
        super.release();

        releaseManagers();

        mIProfileManager = null;

        mApplicationManager = null;

        mDeviceControlManager = null;

        mNetworkManager = null;

        mRestrictionManager = null;

        mSecurityManager = null;
    }

    public String getVersion() {
        if (mIProfileManager != null) {
            try {
                return mIProfileManager.getVersion();
            } catch (RemoteException | NullPointerException e) {
                LogUtil.e("getVersion", e);
            }
        }
        return "";
    }

    public ApplicationManager getApplicationManager() {
        if (mApplicationManager == null) {
            mApplicationManager = new ApplicationManager(this);
        }
        return mApplicationManager;
    }

    public DeviceControlManager getDeviceControlManager() {
        if (mDeviceControlManager == null) {
            mDeviceControlManager = new DeviceControlManager(this);
        }
        return mDeviceControlManager;
    }

    public NetworkManager getNetworkManager() {
        if (mNetworkManager == null) {
            mNetworkManager = new NetworkManager(this);
        }
        return mNetworkManager;
    }

    public RestrictionManager getRestrictionManager() {
        if (mRestrictionManager == null) {
            mRestrictionManager = new RestrictionManager(this);
        }
        return mRestrictionManager;
    }

    public SecurityManager getSecurityManager() {
        if (mSecurityManager == null) {
            mSecurityManager = new SecurityManager(this);
        }
        return mSecurityManager;
    }

    private void initManagers() {
        getApplicationManager().init();

        getDeviceControlManager().init();

        getNetworkManager().init();

        getRestrictionManager().init();

        getSecurityManager().init();
    }

    private void releaseManagers() {
        if(mApplicationManager != null) {
            mApplicationManager.release();
        }

        if(mDeviceControlManager != null) {
            mDeviceControlManager.release();
        }


        if(mRestrictionManager != null) {
            mRestrictionManager.release();
        }

        if(mNetworkManager != null) {
            mNetworkManager.release();
        }

        if(mSecurityManager != null) {
            mSecurityManager.release();
        }
    }
}
                                                                                                       