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

package com.android.settings.homepage;

import static com.android.settings.search.actionbar.SearchMenuController.NEED_SEARCH_ICON_IN_ACTION_BAR;
import static com.android.settingslib.search.SearchIndexable.MOBILE;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.android.settings.R;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.support.SupportPreferenceController;
import com.android.settingslib.core.instrumentation.Instrumentable;
import com.android.settingslib.search.SearchIndexable;

import java.util.Arrays;
import java.util.List;
//urovo add wei.yuan 2021.01.04
import android.content.pm.PackageInfo;
import androidx.preference.PreferenceScreen;

@SearchIndexable(forTarget = MOBILE)
public class TopLevelSettings extends DashboardFragment implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TAG = "TopLevelSettings";

    public TopLevelSettings() {
        final Bundle args = new Bundle();
        // Disable the search icon because this page uses a full search view in actionbar.
        args.putBoolean(NEED_SEARCH_ICON_IN_ACTION_BAR, false);
        setArguments(args);
    }

    // urovo add wei.yuan 2021.01.04
    private boolean isAppInstall(String name) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = getActivity().getPackageManager().getPackageInfo(name, 0);
        } catch (Exception e) {
            packageInfo = null;
        }
        return packageInfo != null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceScreen mPreferenceScreen = (PreferenceScreen) findPreference("top_level_settings");
        if(!isAppInstall("com.android.usettings")) mPreferenceScreen.removePreference(mPreferenceScreen.findPreference("level_scan_setting"));
        if(!isAppInstall("com.ubx.scanwedge")) mPreferenceScreen.removePreference(mPreferenceScreen.findPreference("level_sacn_wedge"));
        if(!isAppInstall("com.ubx.featuresettings")) mPreferenceScreen.removePreference(mPreferenceScreen.findPreference("level_feature_setting"));
        if(!isAppInstall("com.ubx.keyremap")) mPreferenceScreen.removePreference(mPreferenceScreen.findPreference("level_key_remap"));
        if(isAppInstall("com.ubx.datawedge")) {//?????????
            if(mPreferenceScreen.findPreference("level_scan_setting") != null) mPreferenceScreen.removePreference(mPreferenceScreen.findPreference("level_scan_setting"));
            if(mPreferenceScreen.findPreference("level_sacn_wedge") != null) mPreferenceScreen.removePreference(mPreferenceScreen.findPreference("level_sacn_wedge"));
        }else if(!isAppInstall("com.ubx.datawedge")){
            mPreferenceScreen.removePreference(mPreferenceScreen.findPreference("level_scan_setting_new"));
            mPreferenceScreen.removePreference(mPreferenceScreen.findPreference("level_sacn_wedge_new"));
        }
        mPreferenceScreen.findPreference("level_ubx_dividers").setIconSpaceReserved(false);//????????????icon??????
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.top_level_settings;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.DASHBOARD_SUMMARY;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        use(SupportPreferenceController.class).setActivity(getActivity());
    }

    @Override
    public int getHelpResource() {
        // Disable the help icon because this page uses a full search view in actionbar.
        return 0;
    }

    @Override
    public Fragment getCallbackFragment() {
        return this;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        new SubSettingLauncher(getActivity())
                .setDestination(pref.getFragment())
                .setArguments(pref.getExtras())
                .setSourceMetricsCategory(caller instanceof Instrumentable
                        ? ((Instrumentable) caller).getMetricsCategory()
                        : Instrumentable.METRICS_CATEGORY_UNKNOWN)
                .setTitleRes(-1)
                .launch();
        return true;
    }

    @Override
    protected boolean shouldForceRoundedIcon() {
        return getContext().getResources()
                .getBoolean(R.bool.config_force_rounded_icon_TopLevelSettings);
    }

    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.top_level_settings;
                    return Arrays.asList(sir);
                }

                @Override
                protected boolean isPageSearchEnabled(Context context) {
                    // Never searchable, all entries in this page are already indexed elsewhere.
                    return false;
                }
            };
}
