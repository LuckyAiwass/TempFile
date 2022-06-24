package com.ubx.scanwedge.settings.utils;

import android.device.provider.Settings;

/*
 * Copyright (C) 2019, Urovo Ltd
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
 *
 * @Author: rocky
 * @Date: 19-12-24下午4:22
 */
public class ScannerAdapter {
    public static final int TYPE_Opticon = 1;

    public static final int TYPE_SE955 = 2;

    public static final int TYPE_HONYWARE = 3;

    public static final int TYPE_SE4500 = 4;

    public static final int TYPE_N6703 = 5;

    public static final int TYPE_N3680 = 6;

    public static final int TYPE_MJ = 7;

    public static final int TYPE_N6603 = 8;

    public static final int TYPE_SE2100 = 9;

    public static final int TYPE_IA100 = 10;

    public static final int TYPE_N7130 = 11; // SQ73 7.1  7130

    public static final int TYPE_N3601 = 12;

    public static final int TYPE_SE4710 = 13;

    public static final int TYPE_SE2707 = 14;

    public static final int TYPE_N603 = 15;//N603
    public static final String KEY_1D_LINEAR_SCANNER_ENGINE = "1d_linear_scanner";
    public static final String KEY_ZEBAR_ENGINE_PREFERENCE = "zebra_scanner_engine";
    public static final String KEY_HONEYWELL_ENGINE_PREFERENCE = "honeywell_scanner_engine";
    public static final String KEY_MULTIPLE_DECODE = "scan_multiple_decode";
    public static final String KEY_LCDMODE_SCAN = "lcd_mode_scan_key";
    public static final String KEY_1D_SPECIAL_PREFERENCE = "scanner_1d_special";
    public static final String KEY_MULTI_DECODE_MODE_PREFERENCE = "scanner_multi_decode";
    public static final String KEY_EXPOSURE_MODE_PREFERENCE = "scanner_config_exposure";
    public static final String KEY_DEC_AIM_MODE_DELAY = "dec_aim_mode_delay";
    public static final String KEY_DEC_DecodeWindowLimits = "DecodeWindowLimits";
    public static boolean isSupportPreference(int engineType, String preferenceKey) {
        if(KEY_ZEBAR_ENGINE_PREFERENCE.equals(preferenceKey)) {
            if(engineType == TYPE_SE4500 || engineType == TYPE_SE4710 || engineType == TYPE_SE2100/*|| engineType == TYPE_N6603 || engineType == TYPE_N603 || engineType == TYPE_N6703*/) {
                return true;
            }
        } else if (KEY_1D_LINEAR_SCANNER_ENGINE.equals(preferenceKey)) {
            if(engineType == TYPE_Opticon || engineType == TYPE_SE955 || engineType == TYPE_HONYWARE) {
                return true;
            }
        } else if(KEY_HONEYWELL_ENGINE_PREFERENCE.equals(preferenceKey)) {
            if(engineType == TYPE_N3601 || engineType == TYPE_N6603 || engineType == TYPE_N603 || engineType == TYPE_N6703 || engineType == TYPE_N3680) {
                return true;
            }
        } else if(KEY_MULTIPLE_DECODE.equals(preferenceKey)) {
            if(engineType == TYPE_N3601 || engineType == TYPE_N6603 || engineType == TYPE_N603 || engineType == TYPE_N6703) {
                return true;
            }
        } else if(KEY_LCDMODE_SCAN.equals(preferenceKey)) {
            if(engineType == TYPE_SE4500 || engineType == TYPE_SE4710 /*|| engineType == TYPE_N6603 || engineType == TYPE_N603 || engineType == TYPE_N6703*/) {
                return true;
            }
        } else if(KEY_DEC_AIM_MODE_DELAY.equals(preferenceKey)) {
            if(engineType == TYPE_N3680) {
                return true;
            }
        } else if(KEY_1D_SPECIAL_PREFERENCE.equals(preferenceKey)) {
            if(engineType == TYPE_N3680|| engineType == TYPE_N6603 || engineType == TYPE_N6703 || engineType == TYPE_SE4500 || engineType == TYPE_SE4710 || engineType == TYPE_SE2100) {
                return true;
            }
        } else if(Settings.System.DEC_PICKLIST_AIM_MODE.equals(preferenceKey)) {
            if(engineType == TYPE_N3680 || engineType == TYPE_N6603 || engineType == TYPE_N6703 || engineType == TYPE_SE4500 || engineType == TYPE_SE4710 || engineType == TYPE_SE2100) {
                return true;
            }
        } else if(Settings.System.DEC_ILLUM_POWER_LEVEL.equals(preferenceKey)) {
            if(engineType == TYPE_N603) {
                return true;
            }
        } else if(Settings.System.IMAGE_EXPOSURE_MODE.equals(preferenceKey)) {
            if(engineType == TYPE_N6603 || engineType == TYPE_N6703) {
                return true;
            }
        } else if(KEY_DEC_DecodeWindowLimits.equals(preferenceKey)) {
            if(engineType == TYPE_N6603 || engineType == TYPE_N6703) {
                return true;
            }
        } else if(Settings.System.DEC_OCR_MODE.equals(preferenceKey)) {
            if(engineType == TYPE_N603 ||engineType == TYPE_N6603 || engineType == TYPE_N6703) {
                return true;
            }
        } else if(Settings.System.DOTCODE_ENABLE.equals(preferenceKey)) {
            if(engineType == TYPE_N603 ||engineType == TYPE_N6603 || engineType == TYPE_N6703 || engineType == TYPE_SE4500 || engineType == TYPE_SE4710 || engineType == TYPE_SE2100) {
                return true;
            }
        } else if(Settings.System.CODE11_ENABLE_CHECK.equals(preferenceKey)) {
            if(engineType == TYPE_SE4500 || engineType == TYPE_SE4710 || engineType == TYPE_SE2100|| engineType == TYPE_SE955 /*|| engineType == TYPE_N603 || engineType == TYPE_N6703*/) {
                return true;
            }
        } else if(Settings.System.CODE32_SEND_START.equals(preferenceKey)) {
            if(engineType == TYPE_SE4500 || engineType == TYPE_SE4710 || engineType == TYPE_SE2100|| engineType == TYPE_SE955 /*|| engineType == TYPE_N603 || engineType == TYPE_N6703*/) {
                return true;
            }
        } else if(Settings.System.DPM_DECODE_MODE.equals(preferenceKey)) {
            if(engineType == TYPE_N603 ||engineType == TYPE_N6603 || engineType == TYPE_N6703 || engineType == TYPE_SE4500) {
                return true;
            }
        } else if(Settings.System.TRANSMIT_CODE_ID.equals(preferenceKey)) {
            if(/*engineType == TYPE_N603 ||*/engineType == TYPE_N6603 || engineType == TYPE_N6703 || engineType == TYPE_SE4500 || engineType == TYPE_SE4710 || engineType == TYPE_SE2100) {
                return true;
            }
        } else if(Settings.System.SPECIFIC_CODE_GS.equals(preferenceKey)) {
            if(/*engineType == TYPE_N603 ||*/engineType == TYPE_N6603 || engineType == TYPE_N6703 /*|| engineType == TYPE_SE4500 || engineType == TYPE_SE4710 || engineType == TYPE_SE2100*/) {
                return true;
            }
        }

        return false;
    }
}
