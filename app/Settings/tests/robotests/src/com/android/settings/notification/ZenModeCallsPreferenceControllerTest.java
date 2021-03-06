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

package com.android.settings.notification;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.NotificationManager;
import android.content.Context;

import com.android.settingslib.core.lifecycle.Lifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ReflectionHelpers;

import androidx.preference.Preference;

@RunWith(RobolectricTestRunner.class)
public final class ZenModeCallsPreferenceControllerTest {

    private ZenModeCallsPreferenceController mController;
    @Mock
    private NotificationManager mNotificationManager;
    @Mock
    private NotificationManager.Policy mPolicy;

    private Context mContext;
    @Mock
    private ZenModeBackend mBackend;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ShadowApplication shadowApplication = ShadowApplication.getInstance();
        shadowApplication.setSystemService(Context.NOTIFICATION_SERVICE, mNotificationManager);

        mContext = RuntimeEnvironment.application;
        when(mNotificationManager.getNotificationPolicy()).thenReturn(mPolicy);

        mController = new ZenModeCallsPreferenceController(
                mContext, mock(Lifecycle.class), "zen_mode_calls_settings");
        ReflectionHelpers.setField(mController, "mBackend", mBackend);
    }

    @Test
    public void testIsAvailable() {
        assertTrue(mController.isAvailable());
    }

    @Test
    public void testHasSummary() {
        Preference pref = mock(Preference.class);
        mController.updateState(pref);
        verify(pref).setSummary(any());
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               