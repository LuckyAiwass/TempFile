/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.content.Context;
import androidx.preference.Preference;
import android.os.Build;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import android.text.TextUtils;

/**
 * urovo xjf add file 20190225
 */
public class InternalBuildNumberPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String TAG = "InternalBuildNumberPreferenceController";
    private static final String KEY_BUILD_ID_VERSION = "internal_build_number";


    public InternalBuildNumberPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        /*if(TextUtils.isEmpty(android.os.SystemProperties.get("ro.vendor.build.id", ""))) {
            return !TextUtils.isEmpty(android.os.SystemProperties.get("ro.build.version.id", ""));
        } else {
            return true;
        }*/
	return false;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
		String buildId = android.os.SystemProperties.get("ro.vendor.build.id", Build.ID);
        preference.setSummary(buildId);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BUILD_ID_VERSION;
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       