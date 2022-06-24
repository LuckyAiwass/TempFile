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

package com.android.settings.wallpaper;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.when;

import android.app.Application;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadows.ShadowApplication;

@RunWith(RobolectricTestRunner.class)
public class WallpaperSuggestionActivityTest {

    @Mock
    private Context mContext;
    @Mock
    private Resources mResources;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        final Application application = RuntimeEnvironment.application;
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(application);
        ShadowApplication shadowApplication = Shadows.shadowOf(application);
        shadowApplication.setSystemService(Context.WALLPAPER_SERVICE, wallpaperManager);
    }

    @After
    public void tearDown() {
        ShadowWallpaperManager.reset();
    }

    @Test
    public void wallpaperServiceEnabled_no_shouldReturnTrue() {
        when(mContext.getResources()).thenReturn(mResources);
        when(mResources.getBoolean(com.android.internal.R.bool.config_enableWallpaperService))
                .thenReturn(false);

        assertThat(WallpaperSuggestionActivity.isSuggestionComplete(mContext)).isTrue();
    }

    @Test
    @Config(shadows = ShadowWallpaperManager.class)
    public void hasWallpaperSet_no_shouldReturnFalse() {
        ShadowWallpaperManager.setWallpaperId(0);

        assertThat(WallpaperSuggestionActivity.isSuggestionComplete(RuntimeEnvironment.application))
                .isFalse();
    }

    @Test
    @Config(shadows = ShadowWallpaperManager.class)
    public void hasWallpaperSet_yes_shouldReturnTrue() {
        ShadowWallpaperManager.setWallpaperId(100);

        assertThat(WallpaperSuggestionActivity.isSuggestionComplete(RuntimeEnvironment.application))
                .isTrue();
    }

    @Implements(WallpaperManager.class)
    public static class ShadowWallpaperManager extends
        org.robolectric.shadows.ShadowWallpaperManager {

        private static int sWallpaperId;

        private static void setWallpaperId(int id) {
            sWallpaperId = id;
        }

        @Resetter
        public static void reset() {
            sWallpaperId = 0;
        }

        @Implementation
        protected boolean isWallpaperServiceEnabled() {
            return true;
        }

        @Implementation
        protected int getWallpaperId(int which) {
            return sWallpaperId;
        }
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                            