/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.settings.accessibility;

import android.app.settings.SettingsEnums;
import android.media.AudioAttributes;
import android.os.Vibrator;
import android.provider.Settings;

import com.android.settings.R;

/**
 * Fragment for picking accessibility shortcut service
 */
public class RingVibrationPreferenceFragment extends VibrationPreferenceFragment {
    @Override
    public int getMetricsCategory() {
        return SettingsEnums.ACCESSIBILITY_VIBRATION_RING;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.accessibility_ring_vibration_settings;
    }

    /**
     * Get the setting string of the vibration intensity setting this preference is dealing with.
     */
    @Override
    protected String getVibrationIntensitySetting() {
        return Settings.System.RING_VIBRATION_INTENSITY;
    }

    @Override
    protected String getVibrationEnabledSetting() {
        if (AccessibilitySettings.isRampingRingerEnabled(getContext())) {
            return Settings.Global.APPLY_RAMPING_RINGER;
        } else {
            return Settings.System.VIBRATE_WHEN_RINGING;
        }
    }

    @Override
    protected int getPreviewVibrationAudioAttributesUsage() {
        return AudioAttributes.USAGE_NOTIFICATION_RINGTONE;
    }

    @Override
    protected int getDefaultVibrationIntensity() {
        Vibrator vibrator = getContext().getSystemService(Vibrator.class);
        return vibrator.getDefaultRingVibrationIntensity();
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          