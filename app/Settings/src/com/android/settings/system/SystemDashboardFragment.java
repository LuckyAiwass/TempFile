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
package com.android.settings.system;

import android.app.settings.SettingsEnums;
import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.Arrays;
import java.util.List;
//songtingting merge from SQ53 system updater 20200924
import android.util.Log;
import android.content.ComponentName;
import android.content.Intent;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.SystemProperties;
import java.io.File;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;

@SearchIndexable
public class SystemDashboardFragment extends DashboardFragment {

    private static final String TAG = "SystemDashboardFrag";

    private static final String KEY_RESET = "reset_dashboard";

    public static final String EXTRA_SHOW_AWARE_DISABLED = "show_aware_dialog_disabled";
	private static final String KEY_LOCAL_SYSTEM_UPDATE_SETTINGS = "local_system_updater";//songtingting merge from SQ53

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final PreferenceScreen screen = getPreferenceScreen();
        // We do not want to display an advanced button if only one setting is hidden
        if (getVisiblePreferenceCount(screen) == screen.getInitialExpandedChildrenCount() + 1) {
            screen.setInitialExpandedChildrenCount(Integer.MAX_VALUE);
        }
	
	Preference localSystemUpdatePref = screen.findPreference(KEY_LOCAL_SYSTEM_UPDATE_SETTINGS);
	if(localSystemUpdatePref != null) screen.removePreference(localSystemUpdatePref);

        showRestrictionDialog();
    }

    @VisibleForTesting
    public void showRestrictionDialog() {
        final Bundle args = getArguments();
        if (args != null && args.getBoolean(EXTRA_SHOW_AWARE_DISABLED, false)) {
            FeatureFactory.getFactory(getContext()).getAwareFeatureProvider()
                    .showRestrictionDialog(this);
        }
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_SYSTEM_CATEGORY;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.system_dashboard_fragment;
    }

    @Override
    public int getHelpResource() {
        return R.string.help_url_system_dashboard;
    }

    private int getVisiblePreferenceCount(PreferenceGroup group) {
        int visibleCount = 0;
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            final Preference preference = group.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                visibleCount += getVisiblePreferenceCount((PreferenceGroup) preference);
            } else if (preference.isVisible()) {
                visibleCount++;
            }
        }
        return visibleCount;
    }
	@Override
     public boolean onPreferenceTreeClick(Preference preference) {//songtingting merge from SQ53
        if (preference == findPreference(KEY_LOCAL_SYSTEM_UPDATE_SETTINGS)) {
             String sSdDirectory="";
             int VOLUME_SDCARD_INDEX = 1;
             try {
               StorageManager mStorageManager =
                                (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
                    final StorageVolume[] volumes = mStorageManager.getVolumeList();
                         if (volumes.length > VOLUME_SDCARD_INDEX) {
                             StorageVolume volume = volumes[VOLUME_SDCARD_INDEX];
                             if (volume.isRemovable()) {
                                 sSdDirectory = volume.getPath();
                             }
                         }
                     } catch (Exception e) {
                         Log.e("SystemUpdater", "couldn't talk to MountService", e);
                    }                             
                           String UPDATE_FILE = sSdDirectory + "/update.zip";
             File updatefile = new File(UPDATE_FILE/*"/storage/36DD-0FEF/update.zip"*/);
            if(updatefile.exists()){
					SystemProperties.set("persist.sys.package.path",UPDATE_FILE);//add by chenchuanliang for UFS update 20200711
                     Intent intent = new Intent(getActivity(), SystemUpdaterActivity.class);
                     startActivity(intent);
               } else{
                   AlertDialog dialog = new AlertDialog.Builder(getActivity())
                         .setTitle(R.string.recovery_entry_title)
                         .setIconAttribute(android.R.attr.alertDialogIcon)
                         .setMessage(R.string.updatepkgnotfound)
                         .setCancelable(true)
                         .setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                          dialog.cancel();
                          }
                          })
                          .create(); 
                          dialog.show();                 
                    } 
             return true;
        }
 
         return super.onPreferenceTreeClick(preference);
     }

    /**
     * For Search.
     */
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.system_dashboard_fragment;
                    return Arrays.asList(sir);
                }
            };
}
