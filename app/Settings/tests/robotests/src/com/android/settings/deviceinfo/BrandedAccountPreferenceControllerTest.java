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

package com.android.settings.deviceinfo;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import android.accounts.Account;
import android.content.Context;

import com.android.settings.testutils.FakeFeatureFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class BrandedAccountPreferenceControllerTest {

    private Context mContext;
    private FakeFeatureFactory fakeFeatureFactory;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mContext = RuntimeEnvironment.application;
        fakeFeatureFactory = FakeFeatureFactory.setupForTest();

    }

    @Test
    public void isAvailable_configOn_noAccount_off() {
        final BrandedAccountPreferenceController controller =
                new BrandedAccountPreferenceController(mContext, "test_key");
        assertThat(controller.isAvailable()).isFalse();
    }

    @Test
    public void isAvailable_accountIsAvailable_on() {
        when(fakeFeatureFactory.mAccountFeatureProvider.getAccounts(any(Context.class)))
                .thenReturn(new Account[]{new Account("fake@account.foo", "fake.reallyfake")});

        final BrandedAccountPreferenceController controller =
                new BrandedAccountPreferenceController(mContext, "test_key");

        assertThat(controller.isAvailable()).isTrue();
    }

    @Test
    @Config(qualifiers = "mcc999")
    public void isAvailable_configOff_hasAccount_off() {
        when(fakeFeatureFactory.mAccountFeatureProvider.getAccounts(any(Context.class)))
                .thenReturn(new Account[]{new Account("fake@account.foo", "fake.reallyfake")});

        final BrandedAccountPreferenceController controller =
                new BrandedAccountPreferenceController(mContext, "test_key");

        assertThat(controller.isAvailable()).isFalse();
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    