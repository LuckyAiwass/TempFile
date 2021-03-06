package com.ubx.scanwedge.settings.utils;

/*
 * Copyright (C) 2019, Urovo Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @Author: rocky
 * @Date: 20-2-24下午1:48
 */

import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.database.helper.USettings;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

public class ProfileEnable implements CompoundButton.OnCheckedChangeListener {
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

    private static final String TAG = "WedgeProfileEnable";

    private final Context mContext;

    private Switch mSwitch;

    private int mProfileID;
    private ContentResolver mContentResolver;
    private ScanWedgeApplication mApplication;

    private void updateSwitch() {
        boolean enable = mApplication.isProfileEnable(mProfileID);
        mSwitch.setChecked(enable);
    }

    public ProfileEnable(Context context, ScanWedgeApplication application, Switch switchPref, int profileID) {
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
            mSwitch.setOnCheckedChangeListener(this);
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
        USettings.Profile.enableProfile(mContentResolver, mProfileID, isChecked);
        if (mProfileID == USettings.Profile.DEFAULT_ID) {
            try{
                //boolean isProfileEnable = mApplication.isProfileEnable(mProfileID);
                if (mApplication.isDataWedgeEnable() /*&& isProfileEnable*/ && !isChecked) {
                    if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
                        mApplication.getService().close();
                    } else {
                        mApplication.getIService().close();
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void resume() {
        mSwitch.setOnCheckedChangeListener(this);
    }

    public void pause() {
        mSwitch.setOnCheckedChangeListener(null);
    }
}
                                                                                                                          