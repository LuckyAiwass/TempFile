package com.ubx.usdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import com.ubx.usdk.USDKManager.StatusListener;
import com.ubx.usdk.USDKManager.FEATURE_TYPE;
import com.ubx.usdk.USDKManager.STATUS;

import java.util.ArrayList;
import java.util.List;


public class USDKBaseManager implements ServiceConnection {
    private Context mContext;
    private FEATURE_TYPE mFeatureType;
    private Intent mServiceIntent;
    private volatile IBinder mIBinder;
    private volatile STATUS mStatus = STATUS.UNKNOWN;

    private List<StatusListener> mStatusListeners;

    protected USDKBaseManager() {
    }

    protected USDKBaseManager(Context context, FEATURE_TYPE featureType) {
        mContext = context;
        mFeatureType = featureType;
        mStatusListeners = new ArrayList<StatusListener>();
    }

    protected void setIntent(Intent intent) {
        mServiceIntent = intent;
    }

    protected IBinder getIBinder() {
        if(mStatus == STATUS.NOT_READY && Looper.getMainLooper().getThread() == Thread.currentThread()){
            final long waitTime = System.currentTimeMillis();
            while (mStatus == STATUS.NOT_READY && System.currentTimeMillis() - waitTime < 2000) {
                try {
                    LogUtil.d("getIBinder wait " + (System.currentTimeMillis() - waitTime));
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return mIBinder;
    }

    public FEATURE_TYPE getFeatureType() {
        return mFeatureType;
    }

    public STATUS getStatus() {
            return mStatus;
    }

    public void addStatusListener(StatusListener statusListener) {
        if(mStatusListeners != null && !mStatusListeners.contains(statusListener))
            mStatusListeners.add(statusListener);
    }

    private void callOnStatus() {
        if (mStatusListeners != null) {
            for (StatusListener listener : mStatusListeners) {
                listener.onStatus(mFeatureType, mStatus);
            }
        }
    }

    public synchronized void initialize() {
        if(mIBinder != null && mIBinder.isBinderAlive()) {
            mStatus = STATUS.SUCCESS;
            callOnStatus();
            return;
        }
        if (mStatus != STATUS.SUCCESS || mStatus != STATUS.NOT_READY) {
            LogUtil.d("bindService " + mStatus);
            boolean status = mContext.bindService(mServiceIntent, USDKBaseManager.this, Context.BIND_AUTO_CREATE);
            if (status) {
                mStatus = STATUS.NOT_READY;
            } else {
                mStatus = STATUS.NO_SERVICE;
            }
        }
        if(mIBinder != null && !mIBinder.isBinderAlive()) {
            mIBinder = null;
        }
        callOnStatus();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        LogUtil.d("onServiceConnected: " + name.toShortString());
        mIBinder = service;
        mStatus = STATUS.SUCCESS;
        try {
            service.linkToDeath(mDeathHandle, 0);
        } catch (RemoteException e) {
            LogUtil.e("linkToDeath RemoteException");
        }
        callOnStatus();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        LogUtil.d("onServiceDisconnected: " + name.toShortString());
    }

    IBinder.DeathRecipient mDeathHandle = new IBinder.DeathRecipient() {

        @Override
        public void binderDied() {
            // TODO Auto-generated method stub
            if (mIBinder != null) {
                mIBinder.unlinkToDeath(mDeathHandle, 0);
            }
            mStatus = STATUS.NOT_ALIVE;
            LogUtil.e("DeathRecipient binderDied");
            initialize();
            callOnStatus();
        }
    };


    public void release() {
//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
                if (mIBinder != null) {
                    mIBinder.unlinkToDeath(mDeathHandle, 0);
                }
                mContext.unbindService(USDKBaseManager.this);
                mIBinder = null;
                mStatus = STATUS.RELEASE;
                //callOnStatus();
                if(mStatusListeners != null) {
                    mStatusListeners.clear();
                }
//            }
//        });

    }
}
