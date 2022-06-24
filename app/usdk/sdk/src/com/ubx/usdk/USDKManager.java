package com.ubx.usdk;

import android.content.Context;

import com.ubx.usdk.profile.ProfileManager;
import com.ubx.usdk.scanner.UScanManager;
import com.ubx.usdk.psam.PsamManager;

public class USDKManager {

    private volatile static USDKManager mInstance;
    private static Context mContext;
    private com.ubx.usdk.profile.ProfileManager mProfileMananger;
    private com.ubx.usdk.scanner.UScanManager mUScanMananger;
	private PsamManager mPsamManager;


    public static enum FEATURE_TYPE {
        PROFILE,
        BARCODE,
        RFID,
        PSAM,
        SCANNER
    }

    public static enum STATUS {
        NOT_SUPPORTED,
        NO_SERVICE,
        NOT_READY,
        SUCCESS,
        RELEASE,
        NOT_ALIVE,
        UNKNOWN
    }

    public interface StatusListener {
        void onStatus(USDKManager.FEATURE_TYPE featureType, STATUS status);
    }

    private USDKManager(Context context){
        mContext = context.getApplicationContext();
    }

    public static USDKManager getInstance(Context context) {
        if(mInstance == null) {
            synchronized (USDKManager.class) {
                mInstance = new USDKManager(context);
            }
        }
        return mInstance;
    }

    public static USDKBaseManager getFeatureManager(Context context, FEATURE_TYPE featureType) {
        if(mInstance == null) {
            synchronized (USDKManager.class) {
                mInstance = new USDKManager(context);
            }
        }

        return mInstance.getFeatureManager(featureType);
    }

    public USDKBaseManager getFeatureManagerAsync(USDKManager.FEATURE_TYPE featureType, USDKManager.StatusListener statusListener) {
        switch (featureType) {
            case PROFILE:
                if (mProfileMananger == null || mProfileMananger.getStatus() == STATUS.RELEASE) {
                    mProfileMananger = new ProfileManager(mContext);
                    mProfileMananger.addStatusListener(statusListener);
                    mProfileMananger.initialize();
                }else {
                    if(statusListener != null) {
                        mProfileMananger.addStatusListener(statusListener);
                        mProfileMananger.initialize();
                    }
                }
                return mProfileMananger;
            case BARCODE:

                ;
            case RFID:

                ;
            case PSAM:
                if (mPsamManager == null || mPsamManager.getStatus() == STATUS.RELEASE) {
                    mPsamManager = new PsamManager(mContext);
                    mPsamManager.addStatusListener(statusListener);
                    mPsamManager.initialize();
                }else {
                    if(statusListener != null) {
                        mPsamManager.addStatusListener(statusListener);
                        mPsamManager.initialize();
                    }
                }
                return mPsamManager;
            case SCANNER:
                if (mUScanMananger == null || mUScanMananger.getStatus() == STATUS.RELEASE) {
                    mUScanMananger = new UScanManager(mContext);
                    mUScanMananger.addStatusListener(statusListener);
                    mUScanMananger.initialize();
                } else {
                    if (statusListener != null) {
                        mUScanMananger.addStatusListener(statusListener);
                        mUScanMananger.initialize();
                    }
                }
                return mUScanMananger;
            default:
                return new USDKBaseManager();
        }
    }

    public USDKBaseManager getFeatureManager(FEATURE_TYPE featureType) {
        switch (featureType) {
            case PROFILE:
                if (mProfileMananger == null) {
                    mProfileMananger = new ProfileManager(mContext);
                    mProfileMananger.initialize();
                }
                return mProfileMananger;
            case BARCODE:

                ;
            case RFID:

                ;
            case PSAM:
				if (mPsamManager == null) {
                    mPsamManager = new PsamManager(mContext);
                    mPsamManager.initialize();
                }
                return mPsamManager;
            case SCANNER:
                if (mUScanMananger == null) {
                    mUScanMananger = new UScanManager(mContext);
                    mUScanMananger.initialize();
                }
                return mUScanMananger;
            default:
                return new USDKBaseManager();
        }
    }

    public void release() {
        if (mProfileMananger != null) {
            mProfileMananger.release();
            mProfileMananger = null;
        }
        if (mUScanMananger != null) {
            mUScanMananger.release();
        }
		
		if (mPsamManager != null) {
            mPsamManager.release();
            mPsamManager = null;
        }

    }

    public void release(FEATURE_TYPE featureType){
        switch (featureType) {
            case PROFILE:
                if (mProfileMananger != null) {
                    mProfileMananger.release();
                    mProfileMananger = null;
                }
            case BARCODE:

                ;
            case RFID:

                ;
            case PSAM:
                if (mPsamManager != null) {
                    mPsamManager.release();
                    mPsamManager = null;
                }
                ;
            case SCANNER:
        LogUtil.d("USDK release2 :" + mUScanMananger);
                if (mUScanMananger != null) {
                    mUScanMananger.release();
                    mUScanMananger = null;
                }
            default:

        }
    }
}
