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
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.DeviceInfoUtils;
import com.android.settingslib.core.AbstractPreferenceController;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

/**
 * urovo zhoubo add file for UFS begin 2020.11.3 
 */
public class UfsPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String TAG = "UfsPreferenceController";
    private static final String KEY_UFS_VERSION = "ufs_number";


    public UfsPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        String ufscustom=android.os.SystemProperties.get("ro.ufs.custom", "unknown");
        if(ufscustom.equals("unknown")) {
            return false;
        }
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        String ufscustom=android.os.SystemProperties.get("ro.ufs.custom", "unknown");
        String ufsattach=android.os.SystemProperties.get("ro.ufs.custom.attach","unknown");
        String ufsversion=android.os.SystemProperties.get("ro.ufs.build.version", "unknown");
        String ufstime=android.os.SystemProperties.get("ro.ufs.build.date.utc","1405299600");
        long millisecond = Long.parseLong(ufstime);
        Date date = new Date(millisecond);
        SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if(ufsattach.equals("unknown")) {
            ufscustom=ufscustom+"_"+ufsversion+"_"+mformat.format(date);
        } else {
            ufscustom=ufscustom+"_"+ufsattach+"_"+ufsversion+"_"+mformat.format(date);
        }
        preference.setSummary(ufscustom);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_UFS_VERSION;
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              