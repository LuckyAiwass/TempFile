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

package com.android.settings.notification;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.when;

import android.content.Context;
import android.media.RingtoneManager;
import android.telephony.TelephonyManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

@RunWith(RobolectricTestRunner.class)
public class PhoneRingtonePreferenceControllerTest {

    @Mock
    private TelephonyManager mTelephonyManager;

    private Context mContext;
    private PhoneRingtonePreferenceController mController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ShadowApplication shadowContext = ShadowApplication.getInstance();
        shadowContext.setSystemService(Context.TELEPHONY_SERVICE, mTelephonyManager);
        mContext = RuntimeEnvironment.application;
        mController = new PhoneRingtonePreferenceController(mContext);
    }

    @Test
    public void isAvailable_notVoiceCapable_shouldReturnFalse() {
        when(mTelephonyManager.isVoiceCapable()).thenReturn(false);

        assertThat(mController.isAvailable()).isFalse();
    }

    @Test
    public void isAvailable_VoiceCapable_shouldReturnTrue() {
        when(mTelephonyManager.isVoiceCapable()).thenReturn(true);

        assertThat(mController.isAvailable()).isTrue();
    }

    @Test
    public void getRingtoneType_shouldReturnRingtone() {
        assertThat(mController.getRingtoneType()).isEqualTo(RingtoneManager.TYPE_RINGTONE);
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              