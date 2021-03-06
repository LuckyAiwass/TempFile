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
package com.android.settings.accounts;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.accounts.Account;
import android.content.Context;
import android.os.UserHandle;

import androidx.fragment.app.FragmentActivity;

import com.android.settings.testutils.shadow.ShadowContentResolver;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowContentResolver.class})
public class AccountSyncSettingsTest {

    @After
    public void tearDown() {
        ShadowContentResolver.reset();
    }

    @Test
    public void onPreferenceTreeClick_nullAuthority_shouldNotCrash() {
        final Context context = RuntimeEnvironment.application;
        final AccountSyncSettings settings = spy(new AccountSyncSettings());
        when(settings.getActivity()).thenReturn(mock(FragmentActivity.class));
        final SyncStateSwitchPreference preference = new SyncStateSwitchPreference(context,
                new Account("acct1", "type1"), "" /* authority */, "testPackage", 1 /* uid */);
        preference.setOneTimeSyncMode(false);
        ReflectionHelpers.setField(settings, "mUserHandle", UserHandle.CURRENT);

        settings.onPreferenceTreeClick(preference);
        // no crash
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              