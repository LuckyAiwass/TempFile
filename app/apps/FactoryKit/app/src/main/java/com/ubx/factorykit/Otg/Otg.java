/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.factorykit.Otg;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class Otg extends Activity {

    TextView mTextView;
    String TAG = "Otg";
    private Context mContext;
    private boolean isOTGStorageAvi = false;
    private StorageManager mStorageManager;

    private final StorageEventListener mStorageListener = new StorageEventListener() {
        @Override
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            if (isInteresting(vol)) {
                isudiskExists();
                Log.i(TAG, "onVolumeStateChanged isOTGStorageAvi=" + isOTGStorageAvi);
                if (isOTGStorageAvi) {
                    refresh();
                    quitActionTimer.start();
                }
            }
        }

        @Override
        public void onDiskDestroyed(DiskInfo disk) {
            //refresh();
            //finish();
            //System.exit(0);
            //fail(null);
        }
    };

    private static boolean isInteresting(VolumeInfo vol) {
        switch (vol.getType()) {
            //case VolumeInfo.TYPE_PRIVATE:
            case VolumeInfo.TYPE_PUBLIC:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.otg);
        mTextView = (TextView) findViewById(R.id.otg_hint);
        mTextView.setText(getString(R.string.otg_wait));
        init();
    }

    private void init() {
        mContext = getApplicationContext();
        mTextView = (TextView) findViewById(R.id.otg_hint);
        mStorageManager = mContext.getSystemService(StorageManager.class);
        mStorageManager.registerListener(mStorageListener);
    }

    private void refresh() {
        final List<VolumeInfo> volumes = mStorageManager.getVolumes();
        Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
        Log.i(TAG, "refresh volumes.size=" + volumes.size());
        for (VolumeInfo volume : volumes) {
            DiskInfo diskInfo = volume.getDisk();
            if (diskInfo != null && diskInfo.isUsb()) {
                String sdcardState = volume.getEnvironmentForState(volume.getState());
                if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
                    updateDisplay(volume);
                }
            }

        }
    }

    private void updateDisplay(VolumeInfo volume) {
        if (volume.isMountedReadable()) {
            //mVolume = volume;
            final File path = volume.getPath();
            //if (totalBytes <= 0) {
            final long totalBytes = path.getTotalSpace();
            // }
            final long freeBytes = path.getFreeSpace();
            final long usedBytes = totalBytes - freeBytes;

            final String used = Formatter.formatFileSize(mContext, usedBytes);
            final String total = Formatter.formatFileSize(mContext, totalBytes);

            Log.i(TAG, "updateDisplay used=" + used + ";total=" + total);
            mTextView.setText("total= " + total + " used of " + used);
        } else
            mTextView.setText(volume.getStateDescription());

    }

    public void isudiskExists() {
        int num = 0;
        List<VolumeInfo> volumes = mStorageManager.getVolumes();
        for (VolumeInfo volInfo : volumes) {
            DiskInfo diskInfo = volInfo.getDisk();
            if (diskInfo != null && diskInfo.isUsb()) {
                String sdcardState = volInfo.getEnvironmentForState(volInfo.getState());
                Log.i(TAG, "isudiskExists sdcardState=" + sdcardState);
                if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
                    num++;
                }
            }
        }
        Log.i(TAG, "isudiskExists num=" + num);
        if (num > 0)
            isOTGStorageAvi = true;
        else
            isOTGStorageAvi = false;
    }

    @Override
    protected void onResume() {
        mStorageManager.registerListener(mStorageListener);
        isudiskExists();
        Log.i(TAG, "onResume isOTGStorageAvi=" + isOTGStorageAvi);
        if (isOTGStorageAvi) {
            refresh();
            quitActionTimer.start();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        logd("");
        mStorageManager.unregisterListener(mStorageListener);
        super.onPause();
    }

    private final int QUIT_DELAY_TIME = 1500;

    CountDownTimer quitActionTimer = new CountDownTimer(QUIT_DELAY_TIME, QUIT_DELAY_TIME) {

        @Override
        public void onTick(long arg0) {
        }

        @Override
        public void onFinish() {
            pass();
        }
    };

    void fail(Object msg) {

        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG, "Failed");
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG, "Pass");
        finish();
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(getApplicationContext(), s + "", Toast.LENGTH_SHORT).show();
    }

    private void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(TAG, e + "");
    }

    @SuppressWarnings("unused")
    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }
}
