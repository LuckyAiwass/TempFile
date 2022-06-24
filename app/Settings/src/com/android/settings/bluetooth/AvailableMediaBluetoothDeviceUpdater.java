/*
 * Copyright 2018 The Android Open Source Project
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
package com.android.settings.bluetooth;

import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import androidx.preference.Preference;

import com.android.settings.connecteddevice.DevicePreferenceCallback;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;

/**
 * Controller to maintain available media Bluetooth devices
 */
public class AvailableMediaBluetoothDeviceUpdater extends BluetoothDeviceUpdater
        implements Preference.OnPreferenceClickListener {

    private static final String TAG = "AvailableMediaBluetoothDeviceUpdater";
    private static final boolean DBG = false;

    private final AudioManager mAudioManager;

    public AvailableMediaBluetoothDeviceUpdater(Context context, DashboardFragment fragment,
            DevicePreferenceCallback devicePreferenceCallback) {
        super(context, fragment, devicePreferenceCallback);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public void onAudioModeChanged() {
        forceUpdate();
    }

    @Override
    public boolean isFilterMatched(CachedBluetoothDevice cachedDevice) {
        final int audioMode = mAudioManager.getMode();
        final int currentAudioProfile;

        if (audioMode == AudioManager.MODE_RINGTONE
                || audioMode == AudioManager.MODE_IN_CALL
                || audioMode == AudioManager.MODE_IN_COMMUNICATION) {
            // in phone call
            currentAudioProfile = BluetoothProfile.HEADSET;
        } else {
            // without phone call
            currentAudioProfile = BluetoothProfile.A2DP;
        }

        boolean isFilterMatched = false;
        if (isDeviceConnected(cachedDevice)) {
            if (DBG) {
                Log.d(TAG, "isFilterMatched() current audio profile : " + currentAudioProfile);
            }
            // If device is Hearing Aid, it is compatible with HFP and A2DP.
            // It would show in Available Devices group.
            if (cachedDevice.isConnectedHearingAidDevice()) {
                return true;
            }
            // According to the current audio profile type,
            // this page will show the bluetooth device that have corresponding profile.
            // For example:
            // If current audio profile is a2dp, show the bluetooth device that have a2dp profile.
            // If current audio profile is headset,
            // show the bluetooth device that have headset profile.
            switch (currentAudioProfile) {
                case BluetoothProfile.A2DP:
                    isFilterMatched = cachedDevice.isConnectedA2dpDevice();
                    break;
                case BluetoothProfile.HEADSET:
                    isFilterMatched = cachedDevice.isConnectedHfpDevice();
                    break;
            }
            if (DBG) {
                Log.d(TAG, "isFilterMatched() device : " +
                        cachedDevice.getName() + ", isFilterMatched : " + isFilterMatched);
            }
        }
        return isFilterMatched;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        final CachedBluetoothDevice device = ((BluetoothDevicePreference) preference)
                .getBluetoothDevice();
        return device.setActive();
    }
}

                                  