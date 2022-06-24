/*
 * Copyright (C) 2016 The Android Open Source Project
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
 */

package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import android.text.BidiFormatter;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.Toast;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.development.DevelopmentSettingsEnabler;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.InstrumentedPreferenceFragment;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileReader;
import android.util.Log;
import java.io.FileInputStream;
import java.io.File;

public class HardwareVersionPreferenceController extends BasePreferenceController implements
        LifecycleObserver, OnStart {

    private static final String KEY_HARDWARE_VERSION = "hardware_version_number";
	private String HARDWARE_DEV = "/sys/devices/soc/800f000.qcom,spmi/spmi-0/spmi0-00/800f000.qcom,spmi:qcom,pm660@0:vadc@3100/unc_board_id";
		//"/sys/devices/soc/soc:qcom,mdss_dsi@0/sq53_board_id";
    private Activity mActivity;
    private InstrumentedPreferenceFragment mFragment;
    private final UserManager mUm;


    public HardwareVersionPreferenceController(Context context) {
       super(context,KEY_HARDWARE_VERSION);
       mUm = (UserManager) context.getSystemService(Context.USER_SERVICE);
    }

	public void setHost(InstrumentedPreferenceFragment fragment) {
        mFragment = fragment;
        mActivity = fragment.getActivity();
    }


   /* @Override
    public String getPreferenceKey() {
        return KEY_HARDWARE_VERSION;
    }*/

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

   @Override
    public CharSequence getSummary() {
        return getVersionString(HARDWARE_DEV);
    }

    @Override
    public void onStart() {
       /* final Preference preference = screen.findPreference(KEY_HARDWARE_VERSION);
        if (preference != null) {
            try {
                preference.setSummary(getVersionString(HARDWARE_DEV));
            } catch (Exception e) {
                preference.setSummary(R.string.device_info_default);
            }
        }*/
    }

    /**
     * Handles password confirmation result.
     *
     * @return if activity result is handled.
     */
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return true;
    }
	private static String getVersionString(String path) {
        String prop = "";
		String versionProp = "";
		int mProp = 0;
        File mVersionFile = new File(path);
		FileReader fileReader;
		BufferedReader br;
        try {
			fileReader = new FileReader(mVersionFile);
			br = new BufferedReader(fileReader);
    	    prop = br.readLine();
			mProp = Integer.parseInt(prop.substring(0, prop.indexOf(" &&")));
			Log.d("HardwareVersion","HardwareVersion mProp = "+mProp);
			if(mProp < 6){
                versionProp = "Rev 1."+String.valueOf(mProp);
			}else{
                versionProp = "Rev 2."+String.valueOf(mProp - 6);
			}
			
			br.close();
        } catch (Exception e) {
            e.printStackTrace();
		}
		Log.d("HardwareVersion","HardwareVersion = "+prop);
        return versionProp;
    }
}
