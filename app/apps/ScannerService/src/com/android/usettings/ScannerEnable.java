
package com.android.usettings;

import android.content.Context;
import android.content.Intent;
import android.device.ScanManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class ScannerEnable implements CompoundButton.OnCheckedChangeListener {
    OnEnableListener onEnableListener;

    /**
     * Represents a listener that will be notified of headline selections.
     */
    public interface OnEnableListener {
        /**
         * Called when a given headline is selected.
         * 
         * @param index the index of the selected headline.
         */
        public void onEnable(boolean enable);
    }

    private static final String TAG = "ScannerEnable";

    private ScanManager mScanManager;

    private final Context mContext;

    private Switch mSwitch;

    private int syncScantype() {
        int typeInit = mScanManager.getOutputParameter(7);
        if (typeInit == 0) {
            mSwitch.setEnabled(false);
        }
        mSwitch.setEnabled(true);
        return typeInit;
    }

    private void SetPowerSwitch() {
        int isenabled = android.provider.Settings.System.getInt(mContext.getContentResolver(), "urovo_scan_stat", 1);
        if (isenabled == 1) {
            mSwitch.setChecked(true);
        } else {
            mSwitch.setChecked(false);
        }
        // syncScantype();
    }

    public ScannerEnable(Context context, Switch switch_) {
        mContext = context;
        mSwitch = switch_;
        mScanManager = new ScanManager();
        mSwitch.setOnCheckedChangeListener(this);
        // syncScantype();
        if(android.os.SystemProperties.get("persist.sys.scanner", "true").equals("false")){
            //UTE禁用扫描头
            mSwitch.setEnabled(false);
        } else {
            mSwitch.setEnabled(true);
        }
    }

    public void setOnEnableListener(OnEnableListener onEnableListener) {
        this.onEnableListener = onEnableListener;
    }

    public void setSwitch(Switch switch_) {
        if (mSwitch == switch_) {
            SetPowerSwitch();
            return;
        }
        mSwitch.setOnCheckedChangeListener(null);
        mSwitch = switch_;
        mSwitch.setOnCheckedChangeListener(this);
        SetPowerSwitch();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onCheckedChanged= " + isChecked);

        if (isChecked) {
            mScanManager.openScanner();
        } else {
            mScanManager.closeScanner();
        }
        android.provider.Settings.System.putInt(mContext.getContentResolver(), "urovo_scan_stat", isChecked?1:0);
        if (onEnableListener != null) {
            onEnableListener.onEnable(isChecked);
        }
    }

    public void resume() {
        //SetPowerSwitch();
        mSwitch.setOnCheckedChangeListener(this);
    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      