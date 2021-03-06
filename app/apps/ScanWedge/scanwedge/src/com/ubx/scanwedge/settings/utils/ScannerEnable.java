package com.ubx.scanwedge.settings.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.device.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.database.helper.USettings;

public class ScannerEnable implements CompoundButton.OnCheckedChangeListener {
    OnEnableListener onEnableListener;

    /**
     * Represents a listener that will be notified of headline selections.
     */
    public interface OnEnableListener {
        /**
         * Called when a given headline is selected.
         *
         * @param enable
         */
        void onEnable(boolean enable);
    }

    private static final String TAG = "ScannerEnable";

    private final Context mContext;

    private Switch mSwitch;

    private int mProfileID;
    private ContentResolver mContentResolver;
    private ScanWedgeApplication mApplication;

    private void updateSwitch() {
        int enable = USettings.System.getInt(mContentResolver, mProfileID, Settings.System.SCANNER_ENABLE, 1);
        mSwitch.setChecked(enable == 1);
    }

    public ScannerEnable(Context context, ScanWedgeApplication application, Switch switchPref, int profileID) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mSwitch = switchPref;
        mProfileID = profileID;
        mApplication = application;
        //mSwitch.setOnCheckedChangeListener(this);
        if (android.os.SystemProperties.get("persist.sys.scanner", "true").equals("false")) {
            //UTE禁用扫描头
            mSwitch.setEnabled(false);
        } else {
            mSwitch.setEnabled(true);
        }
    }

    public void setOnEnableListener(OnEnableListener onEnableListener) {
        this.onEnableListener = onEnableListener;
        if (onEnableListener != null) onEnableListener.onEnable(mSwitch.isChecked());
    }

    public void setSwitch(Switch switchPref) {
        if (mSwitch == switchPref) {
            updateSwitch();
            return;
        }
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switchPref;
        updateSwitch();
        mSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onCheckedChanged = " + isChecked);
        USettings.System.putInt(mContentResolver, mProfileID, 0, Settings.System.SCANNER_ENABLE, isChecked ? 1 : 0);
        if (mProfileID == USettings.Profile.DEFAULT_ID) {
            try{
                if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
                    if (isChecked) {
                        mApplication.getService().open();
                    } else {
                        mApplication.getService().close();
                    }
                } else {
                    if (isChecked) {
                        mApplication.getIService().open();
                    } else {
                        mApplication.getIService().close();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        if (onEnableListener != null) {
            onEnableListener.onEnable(isChecked);
        }
    }

    public void resume() {
        mSwitch.setOnCheckedChangeListener(this);
    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
    }
}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          