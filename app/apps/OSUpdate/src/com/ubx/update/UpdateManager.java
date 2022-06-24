/**
 * Copyright (c) 2020, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.ubx.update;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.device.DeviceManager;
import android.os.SystemProperties;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager.WakeLock;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.text.format.Formatter;
import java.io.File;
import java.io.IOException;
import com.android.internal.util.Preconditions;

public class UpdateManager {
    private static final String TAG = "UpdateManager";
    private static UpdateManager updateManager;
    private UpdateProgressCallback mUpdateProgressCallback;
    private UpdateTask updateTask;
    private PowerManager.WakeLock mWakeLock ;
    private Context mContext;
    private PowerManager mPowerManager;
    private File mUpdateFile;
    private DeviceManager mDeviceManager;
    private boolean isUpdating = false;
    private final CarUpdateEngineCallback mCarUpdateEngineCallback = new CarUpdateEngineCallback();
    private final UpdateEngine mUpdateEngine = new UpdateEngine();

    public static final class UpdateStatusCode{
        public static final int UPDATE_SUCCESS = 0;
        public static final int UPDATE_FAILED = 1;
    }

    private int mPackageStatus = 0;
    private int mUFSPercent = 0;
    private int mUFSUpdateCount = 0;
    private final int UFS_UPDATE_CHECK_MAX = 30;
    Handler mHandler = new Handler();
    private final Runnable mUFSUpdateProgress = new Runnable() {
        @Override
        public void run() {
            int ufsstatus = SystemProperties.getInt("persist.sys.ufsupdate_status",9);
            if(ufsstatus != 9 || mUFSUpdateCount == UFS_UPDATE_CHECK_MAX) {
                if(ufsstatus == 1 || mUFSUpdateCount == UFS_UPDATE_CHECK_MAX) {
                    updateSuccess();
                    mHandler.postDelayed(mReboot,1000);
                }
                mUFSPercent = 0;
                mUFSUpdateCount = 0;
                return;
            }
            if(mUFSPercent < 100) {
                mUFSPercent = mUFSPercent + 10;
            } else {
                mUFSUpdateCount++;
            }
            Log.d(TAG,"mUFSPercent---->"+mUFSPercent);
            if (mUpdateProgressCallback != null) {
                mUpdateProgressCallback.onProgressUpdate(mUFSPercent);
            }
            mHandler.postDelayed(mUFSUpdateProgress,500);
        }
    };

    private final Runnable mReboot = new Runnable() {
        @Override
        public void run() {
            rebootNow();
        }
    };

    private UpdateManager(Context context){
        mContext = context;
        mPowerManager = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"update");
        mDeviceManager = new DeviceManager();
    }

    public static UpdateManager getDefault(Context context) {
        if (updateManager == null) {
            updateManager = new UpdateManager(context);
        }
        return updateManager;
    }

    void doUpdate(String filePath,UpdateProgressCallback updateProgressCallback) {
        if (isUpdating()) {
            Log.d(TAG,"the device is updating now,do not do it again!");
            return;
        }
        mUpdateProgressCallback = updateProgressCallback;
        if (filePath != null && !filePath.equals("")) {
            SystemProperties.set("persist.sys.package.path",filePath);
            mUpdateFile = new File(filePath);
        } else {
            Log.d(TAG,"the filePath-->"+filePath+" is not correct!");
	    if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
            if ("1".equals(UpdateUtil.readOemPartition(UpdateUtil.TAG_IS_UPGRADE))) {
                UpdateUtil.pushUpgradeResultToServer(mContext, UpdateUtil.OTHER);
            }
	    }
            return;
        }
        updateTask = new UpdateTask();
        updateTask.execute(mUpdateFile);
        updateStart();
    }

    boolean isUpdating() {
        return updateTask != null && isUpdating;
    }

    void updateStart() {
        if(!mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
        isUpdating = true;
    }

    void updateFailed() {
        Log.d(TAG,"update failed !");
        if(mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (ForceUpdateManager.getInstance().getforceUpdate()) {
            UpdateUtil.handleForceupgradingView(true); //unlock the statusbar and back button,home button,appswitch button
            ForceUpdateManager.getInstance().setforceUpdate(false);
        }
        if (mUpdateProgressCallback != null) {
            mUpdateProgressCallback.onUpdateComplete(UpdateStatusCode.UPDATE_FAILED);
        }
        if (mUpdateFile.exists()) {
           mUpdateFile.delete();
        }
        isUpdating = false;
        updateTask.cancel(false);
	if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
        if ("1".equals(UpdateUtil.readOemPartition(UpdateUtil.TAG_IS_UPGRADE))) {
            UpdateUtil.pushUpgradeResultToServer(mContext, UpdateUtil.OTHER);
        }
	}
    }

    void updateSuccess() {
        Log.d(TAG,"update success !");
        if(mWakeLock.isHeld()) {
            mWakeLock.release();
        }
        if (ForceUpdateManager.getInstance().getforceUpdate()) {
            UpdateUtil.handleForceupgradingView(true); //unlock the statusbar and back button,home button,appswitch button
            ForceUpdateManager.getInstance().setforceUpdate(false);
        }
        if (mUpdateProgressCallback != null) {
            mUpdateProgressCallback.onUpdateComplete(UpdateStatusCode.UPDATE_SUCCESS);
        }
        if (mUpdateFile.exists()) {
           mUpdateFile.delete();
        }
        isUpdating = false;
        updateTask.cancel(false);
        if ("1".equals(UpdateUtil.readOemPartition(UpdateUtil.TAG_IS_UPGRADE))) {
            UpdateUtil.pushUpgradeResultToServer(mContext, UpdateUtil.NULL_REASON);
        }
    }

    private void rebootNow() {
        if (!SystemProperties.get("persist.sys.update.silence","").contains("noimmedate")) {
            Log.i(TAG, "Rebooting Now.");
            //Intent intent = new Intent("android.ota.reboot.update.action");
            //intent.setPackage("com.android.settings");
            //mContext.sendBroadcast(intent);
            try {
         Intent intent = new Intent(Intent.ACTION_REBOOT);
         intent.putExtra("nowait", 1);
         intent.putExtra("interval", 1);
         intent.putExtra("window", 0);
         mContext.sendBroadcast(intent);
        } catch (Exception e) {}
        }
    }

    /** Attempt to install the update that is copied to the device. */
    private void installUpdate(UpdateParser.ParsedUpdate parsedUpdate) {
        if(mPackageStatus == UpdateParser.ONLY_UFS_PACKAGE)
        {
            mHandler.postDelayed(mUFSUpdateProgress,500);
        } else {
            mUpdateEngine.bind(mCarUpdateEngineCallback, new Handler(mContext.getMainLooper()));
        }

        // urovo weiyu add on 2020-07-31 start
        if(mPackageStatus == UpdateParser.AB_UFS_PACKAGE || mPackageStatus == UpdateParser.ONLY_UFS_PACKAGE)
            SystemProperties.set("persist.sys.ufsupdate","1");
        if(mPackageStatus == UpdateParser.AB_UFS_PACKAGE || mPackageStatus == UpdateParser.ONLY_AB_PACKAGE)
            mUpdateEngine.applyPayload(
                    parsedUpdate.mUrl, parsedUpdate.mOffset, parsedUpdate.mSize, parsedUpdate.mProps);
        // urovo weiyu add on 2020-07-31 end
    }

    class UpdateTask extends AsyncTask<File,Void,UpdateParser.ParsedUpdate> {

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (updateTask != null) {
                updateTask = null;
            }
        }

        @Override
        protected void onPostExecute(UpdateParser.ParsedUpdate result) {
            if (result == null && mPackageStatus != UpdateParser.ONLY_UFS_PACKAGE) {
                Log.e(TAG,"AnomalyConfigReceiver result = null");
                updateFailed();
                return;
            }
            if (!result.isValid() && mPackageStatus != UpdateParser.ONLY_UFS_PACKAGE) {
                Log.e(TAG,"AnomalyConfigReceiver result.isValid() >>> "+result.isValid());
                Log.e(TAG, String.format("Failed verification %s", result));
                updateFailed();
                return;
            }
            Log.i(TAG, result.toString());
            installUpdate(result);
        }

        @Override
        protected UpdateParser.ParsedUpdate doInBackground(File... files) {
            Preconditions.checkArgument(files.length > 0, "No file specified");
            UpdateParser.ParsedUpdate mParsedUpdate;
            try {
                mPackageStatus = UpdateParser.getPackageStatus(files[0]);
            } catch (IOException e) {
                mPackageStatus = UpdateParser.ONLY_AB_PACKAGE;
                Log.e(TAG, "OTA package parser error : ", e);
            }
            Log.d("SystemUpdate","mPackageStatus ="+mPackageStatus);
            try {
                mParsedUpdate = UpdateParser.parse(files[0]);
            } catch (IOException e) {
                updateFailed();
                Log.e(TAG, String.format("For file %s", files[0]), e);
                return null;
            }
            return mParsedUpdate;
        }
    }

    /** Handles events from the UpdateEngine. */
    public class CarUpdateEngineCallback extends UpdateEngineCallback {

        @Override
        public void onStatusUpdate(int status, float percent) {
            Log.d(TAG, String.format("onStatusUpdate %d, Percent %.2f", status, percent));
            switch (status) {
                case UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT:
                    rebootNow();
                    break;
                case UpdateEngine.UpdateStatusConstants.DOWNLOADING:
                    Log.d(TAG,"percent >>>>>>>> "+(int)(percent * 100));
                    if (mUpdateProgressCallback != null) {
                        mUpdateProgressCallback.onProgressUpdate((int) (percent * 100));
                    }
                    break;
                default:
                    // noop
            }
        }

        @Override
        public void onPayloadApplicationComplete(int errorCode) {
            Log.d(TAG, String.format("onPayloadApplicationComplete %d", errorCode));
            if (errorCode == UpdateEngine.ErrorCodeConstants.SUCCESS) {
                updateSuccess();
            }
            if (errorCode != UpdateEngine.ErrorCodeConstants.SUCCESS){
                updateFailed();
            }
        }
    }

}
