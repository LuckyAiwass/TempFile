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
package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.SwitchPreference;
import androidx.preference.Preference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import java.io.FileOutputStream;//hanzengqin add
import java.io.FileInputStream;//hanzengqin add
import android.util.Log;

//add by songtingting 20180808
public class GlovePreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_GLOVE = "Glove_key";
	
	//s merge Caribou 	
	final byte[] GLOVE_MODE_ON = {'1'};
	final byte[] GLOVE_MODE_OFF = {'0'};
	
	private String Glove_DEV = "/sys/devices/soc/c178000.i2c/i2c-4/4-0014/gtp_glove_mode";	

		
    public GlovePreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_GLOVE;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
       
			boolean value = (Boolean) newValue;
			Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.SETTINGS_GLOVE_MODE_STATE, value? 1 : 0);
			try {
				FileOutputStream fGloveMode = new FileOutputStream(Glove_DEV);
				fGloveMode.write(value ? GLOVE_MODE_ON : GLOVE_MODE_OFF);
				fGloveMode.close();
				Log.i("stt","value="+value);
			} catch (Exception e) {
				Log.e("stt", "" + e);
			}
		 return true;
    }

    @Override
    public void updateState(Preference preference) {
			int mGloveMode=Settings.Global.getInt(mContext.getContentResolver(),Settings.Global.SETTINGS_GLOVE_MODE_STATE, 0);
			
			Log.i("stt","mGloveMode="+mGloveMode);
			((SwitchPreference) preference).setChecked(mGloveMode==1? true:false);
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          