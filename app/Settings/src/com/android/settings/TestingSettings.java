/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import android.app.settings.SettingsEnums;
import android.os.Bundle;
import android.os.UserManager;

import androidx.preference.PreferenceScreen;
import android.telephony.TelephonyManager;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

//urovo weiyu add on 2019-12-06 start
import androidx.preference.Preference;
import android.text.TextUtils;
import android.os.SystemProperties;
import android.device.DeviceManager;
import android.os.ServiceManager;
//urovo weiyu add on 2019-12-06 end

import android.widget.EditText;
import android.widget.Toast;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.provider.Settings;
import com.android.internal.telephony.PhoneConstants;
public class TestingSettings extends SettingsPreferenceFragment {
    int sNumPhones = TelephonyManager.getDefault().getPhoneCount();

    private AlertDialog mSecurtyAlertDialog;
    private AlertDialog mInstallAlertDialog;
    private String packageNames = "";
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.testing_settings);
        //urovo add by lq 2020.07.11 begin
        mContext = getContext();
        PreferenceScreen mappInstallPreferenceScreen = (PreferenceScreen) findPreference("app_installed_white_list_key");
        if(!android.os.Build.PWV_CUSTOM_CUSTOM.equals("JST")) {
            getPreferenceScreen().removePreference(mappInstallPreferenceScreen);
        }
        mappInstallPreferenceScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                securtyInput();
                return true;
            }
        });
        //urovo add end 2020.07.11
        //urovo weiyu add on 2019-12-06 start
        PreferenceScreen mPreferenceScreen = (PreferenceScreen) 
                    findPreference("usb_config_settings");
        mPreferenceScreen.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                /*IDeviceManagerService device = IDeviceManagerService.Stub
                        .asInterface(ServiceManager.getService("DeviceManager"));*/
                DeviceManager device = new DeviceManager();
                try{
                    String vendorconfig = device.getSettingProperty("persist-persist.vendor.usb.config");
                    if(TextUtils.isEmpty(vendorconfig)) {
                        if(android.os.Build.PROJECT.equals("SQ53") || android.os.Build.PROJECT.equals("SQ51S")) {
                            vendorconfig = "diag,serial_cdev,rmnet,adb";
                        } else {
                            vendorconfig = "diag,serial_smd,rmnet_qti_bam,adb";
                        }
                    }
                    device.setSettingProperty("persist-persist.sys.usb.config", vendorconfig);
                    device.setSettingProperty("persist-sys.usb.config", vendorconfig);
                    device.setSettingProperty("persist-sys.usb.state", vendorconfig);
                }catch(Exception e){
                    return false;
                };
                return true;
            }

        });
        //urovo weiyu add on 2019-12-06 end

        final UserManager um = UserManager.get(getContext());
        if (!um.isAdminUser()) {
            PreferenceScreen preferenceScreen = (PreferenceScreen)
                    findPreference("radio_info_settings");
            getPreferenceScreen().removePreference(preferenceScreen);
        }

        if (PhoneConstants.MAX_PHONE_COUNT_DUAL_SIM == sNumPhones) {
            PreferenceScreen preferenceScreen = (PreferenceScreen)
                    findPreference("radio_info_settings");
            getPreferenceScreen().removePreference(preferenceScreen);
        } else if (PhoneConstants.MAX_PHONE_COUNT_SINGLE_SIM == sNumPhones) {
            PreferenceScreen preferenceScreen1 = (PreferenceScreen)
                    findPreference("radio_info1_settings");
            getPreferenceScreen().removePreference(preferenceScreen1);

            PreferenceScreen preferenceScreen2 = (PreferenceScreen)
                    findPreference("radio_info2_settings");
            getPreferenceScreen().removePreference(preferenceScreen2);
        }
    }

    //urovo add by lq begin 2020.07.20
    private void securtyInput() {
        final EditText etPw = new EditText(mContext);
        etPw.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
        mSecurtyAlertDialog = new AlertDialog.Builder(mContext)
        .setTitle(mContext.getString(R.string.allow_app_installed_password)).setView(etPw)
        .setNegativeButton(mContext.getString(com.android.internal.R.string.cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                etPw.setText("");
            }
        })
        .setPositiveButton(mContext.getString(com.android.internal.R.string.ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if(etPw.getText().toString().equals("83918215")){
                    etPw.setText("");
                    dialog.cancel();
                    appInstalledSettings();
                } else {
                    Toast toast = Toast.makeText(mContext, mContext.getString(R.string.allow_app_installed_password_error), Toast.LENGTH_SHORT);
                    toast.show();
                    etPw.setText("");
                }
            }
        }).create();
        //mSecurtyAlertDialog.setCanceledOnTouchOutside(false);
        mSecurtyAlertDialog.setCancelable(false);
        mSecurtyAlertDialog.show();
    }
    //urovo add end 2020.07.20

    //urovo add by lq 2020.07.11 begin
    private void appInstalledSettings() {
        final EditText etPw = new EditText(mContext);
        etPw.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        packageNames = Settings.System.getString(mContext.getContentResolver(), "InstallerPckNames");
        etPw.setText(packageNames);
        mInstallAlertDialog = new AlertDialog.Builder(mContext)
        .setTitle(mContext.getString(R.string.app_installed_white_list)).setView(etPw)
        .setNegativeButton(mContext.getString(com.android.internal.R.string.cancel), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        })
        .setPositiveButton(mContext.getString(com.android.internal.R.string.ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                packageNames = etPw.getText().toString();
                Settings.System.putString(mContext.getContentResolver(), "InstallerPckNames", packageNames == null ? "" : packageNames);
            }
        }).create();
        //mInstallAlertDialog.setCanceledOnTouchOutside(false);
        mInstallAlertDialog.setCancelable(false);
        mInstallAlertDialog.show();
    }
    //urovo add end 2020.07.11

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.TESTING;
    }
}
