package com.ubx.decoder;

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
 * @Date: 20-4-15下午3:27
 */
public enum DecodeWindowMode {
    /** Disables Decode Window */
    DECODE_WINDOW_MODE_OFF(0),
    /** Window around aimer (center of image must be covered, since the
     * aimer coordinates offset the window from center) */
    DECODE_WINDOW_MODE_AIMER(1),
    /** Window as defined in field of view */
    DECODE_WINDOW_MODE_FIELD_OF_VIEW(2),
    /** Sub-image or window as defined in field of view (barcode must be
     * decodable within entire window) */
    DECODE_WINDOW_MODE_SUB_IMAGE(3);
    /**
     * @hide
     */
    private final int value;

    /**
     * @hide
     */
    private static DecodeWindowMode[] allValues = values();

    /**
     * @hide
     */
    private DecodeWindowMode(int type_number) {
        value = type_number;
    }

    /**
     * From the ordered enum to DecodeWindowMode.
     *
     * @return DecodeWindowMode the corresponding one.
     */
    public static DecodeWindowMode fromOrdinal(int n) {
        return allValues[n];
    }

    /**
     * From an integer value, retrieves the corresponding DecodeWindowMode.
     *
     * @param n
     *            <code>int</code>
     * @return DecodeWindowMode the corresponding Symbology.
     */
    public static DecodeWindowMode fromInt(int n) {
        for(int i = 0; i < allValues.length; i++) {
            if (allValues[i].value == n)
                return allValues[i];
        }
        return DECODE_WINDOW_MODE_OFF;
    }

    /**
     * Converts the DecodeWindowMode to its corresponding integer value.
     *
     * @return int representing a DecodeWindowMode value.
     */
    public int toInt() {
        return value;
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       