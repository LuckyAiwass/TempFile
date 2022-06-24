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

package com.android.settings.datetime;

import com.android.settings.R;
import com.android.settings.DateTimeSettings;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.TimePickerDialog;
import android.content.Context;
import androidx.preference.Preference;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.util.Log;
import android.widget.EditText;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.content.res.Resources;
import android.provider.Settings;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.Calendar;

public class NtpServerPreferenceController extends AbstractPreferenceController
        implements PreferenceControllerMixin {

    public interface NtpServerPreferenceHost extends UpdateTimeAndDateCallback {
        void showNtpServerDialog();
    }

    public static final int DIALOG_NTPSERVER = 10;

    private static final String KEY_NTPSERVER = "ntp_server";

    private EditText serverEdit;
    private ContentResolver mResolver;

    private final AutoTimePreferenceController mAutoTimePreferenceController;
    private final NtpServerPreferenceHost mHost;

    public NtpServerPreferenceController(Context context,
            NtpServerPreferenceHost host,
            AutoTimePreferenceController autoTimePreferenceController) {
        super(context);
        mHost = host;
        mAutoTimePreferenceController = autoTimePreferenceController;
        mResolver = context.getContentResolver();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        if (!(preference instanceof RestrictedPreference)) {
            return;
        }

        if(mResolver != null) {
            final String secureServer = Settings.Global.getString(
                       mResolver, Settings.Global.NTP_SERVER);
            preference.setSummary(secureServer);
        }
        
        if (!((RestrictedPreference) preference).isDisabledByAdmin()) {
            preference.setEnabled(mAutoTimePreferenceController.isEnabled());
        }
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(KEY_NTPSERVER, preference.getKey())) {
            return false;
        }
        mHost.showNtpServerDialog();
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_NTPSERVER;
    }

    public AlertDialog buildNtpServerDialog(Activity activity) {
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.ntpserver_settings, null);

        final Resources res = activity.getResources();
        mResolver = activity.getContentResolver();

        final String defaultServer = res.getString(
         com.android.internal.R.string.config_ntpServer);

        final String secureServer = Settings.Global.getString(
         mResolver, Settings.Global.NTP_SERVER);

        final String server = secureServer != null ? secureServer : defaultServer;

        serverEdit = (EditText) dialogView.findViewById(R.id.server_url);

        if (server != null) {
            serverEdit.setText(server);
        }
        return new AlertDialog.Builder(activity).setView(dialogView)
        .setTitle(res.getString(R.string.title_server_url))
        .setNeutralButton(android.R.string.cancel, null)
        .setPositiveButton(android.R.string.ok,                         
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String str = serverEdit.getText().toString().trim();
                    Settings.Global.putString(activity.getContentResolver(), Settings.Global.NTP_SERVER,str);
                    Settings.Global.putInt(activity.getContentResolver(), Settings.Global.AUTO_TIME, 0);
                    Settings.Global.putInt(activity.getContentResolver(), Settings.Global.AUTO_TIME, 1);
                    mHost.updateTimeAndDateDisplay(mContext);
                }
            }).create();
    }

}
