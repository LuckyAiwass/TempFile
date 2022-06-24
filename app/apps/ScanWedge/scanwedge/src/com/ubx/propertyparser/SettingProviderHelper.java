package com.ubx.propertyparser;

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
 * @Date: 20-9-23下午9:04
 */

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.device.scanner.configuration.PropertyID;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;


import com.ubx.database.helper.UConstants;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

public class SettingProviderHelper {
    private static boolean DEBUG = false;
    private static final String TAG = "SettingProviderHelper";
    private final Object mLock = new Object();
    private SparseArray<Property> mCacheProperties = new SparseArray<Property>();
    //存储参数值为字符串类型
    private SparseArray<Property> mCacheStringProperties = new SparseArray<Property>();
    private SparseArray<Property> mCacheOutputProperties = new SparseArray<Property>();
    private SparseArray<Property> mCacheFormatProperties = new SparseArray<Property>();
    private SparseArray<Property> mCacheReaderProperties = new SparseArray<Property>();
    private static final String[] allProProjection = new String[]{
            UConstants.PROPERTY_ID,
            UConstants.PROPERTY_NAME,
            UConstants.PROPERTY_VALUE,
            UConstants.PROPERTY_VALUE_TYPE,
            UConstants.PROPERTY_VALUE_MIN,
            UConstants.PROPERTY_VALUE_MAX,
            UConstants.PROPERTY_SCANNER_TYPE,
            UConstants.PROPERTY_GROUP
    };
    private ContentValues[] values;
    private static SettingProviderHelper mPropertyHelper = null;
    private Context mContext = null;
    private ContentResolver mContentResolver;
    private int profileId = UConstants.DEFAULT_ID;
    private static HashMap<String, Integer> keyPropertyMap = null;
    public SettingProviderHelper(Context context) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        keyPropertyMap = propertyPopulate();
    }

    public static SettingProviderHelper getSingleton(Context context) {
        if (mPropertyHelper == null) {
            mPropertyHelper = new SettingProviderHelper(context);
        }
        return mPropertyHelper;
    }
    private ContentValues createContentValues(Property property) {
        ContentValues values = new ContentValues();
        values.put(UConstants.PROFILE_ID, UConstants.DEFAULT_ID);
        values.put(UConstants.PROPERTY_ID, property.getId());
        values.put(UConstants.PROPERTY_NAME, property.getName());
        values.put(UConstants.PROPERTY_VALUE, property.getValue());
        values.put(UConstants.PROPERTY_VALUE_TYPE, property.getValueType());
        values.put(UConstants.PROPERTY_VALUE_MIN, property.getMin());
        values.put(UConstants.PROPERTY_VALUE_MAX, property.getMax());
        values.put(UConstants.PROPERTY_SCANNER_TYPE, property.getSupportType());
        /*values.put(UConstants.PROPERTY_DISPLAY_NAME, property.getDisplayName());
        values.put(UConstants.PROPERTY_PARAM_NUMBER, property.getParamNum());
        values.put(UConstants.PROPERTY_DEFAULT_VALUE, property.getDefaultValue());
        values.put(UConstants.PROPERTY_VALUE_DISCRETE_COUNT, property.getDiscreteCount());
        values.put(UConstants.PROPERTY_VALUE_DISCRETE, property.getDiscreteEntryValues());
        values.put(UConstants.PROPERTY_VALUE_DISCRETE_NAMES, property.getDiscreteEntries());*/
        values.put(UConstants.PROPERTY_GROUP, property.getCategory());
        return values;
    }
    public void setDefaultProperty() {
        synchronized (mLock) {
            try {
                mCacheStringProperties.clear();
                mCacheOutputProperties.clear();
                mCacheFormatProperties.clear();
                mCacheReaderProperties.clear();
                mCacheProperties.clear();
                List<Property> parsePropertyList = DataParser.parseALLPropertyFromXML(mContext,"configs/scanner_default_property.xml"/*"etc/scanner_default_property.xml"*/);
                if(parsePropertyList != null && parsePropertyList.size() > 0) {
                    values = new ContentValues[parsePropertyList.size()];
                    int i = 0;
                    for(Property property: parsePropertyList) {
                        if (Category.VAL_STRING.equals(property.getValueType())) {
                            mCacheStringProperties.put(property.getId(), property);
                        } else if (Category.CAT_OUTPUT.equals(property.getCategory())) {
                            mCacheOutputProperties.put(property.getId(), property);
                        } else if (Category.CAT_FORAMT.equals(property.getCategory())) {
                            mCacheFormatProperties.put(property.getId(), property);
                        } else if (Category.CAT_READER.equals(property.getCategory())) {
                            mCacheReaderProperties.put(property.getId(), property);
                        } else {
                            mCacheProperties.put(property.getId(), property);
                        }
                        values[i++] = createContentValues(property);
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            long start = System.currentTimeMillis();
                            /*for (int i = 0; i < values.length; i++) {
                                //Log.d(TAG, "setDefaultProperty db i=" + i + values[i].get(UConstants.PROPERTY_NAME));
                                putStrings(values[i]);
                            }*/
                            putBulkStrings(values);
                            long end = System.currentTimeMillis();
                            if(DEBUG) Log.d(TAG, "setDefaultProperty db time=" + (end - start) + "ms");
                            values = null;
                        }
                    }).start();
                }
                if(DEBUG) {
                    Log.d(TAG, "mCacheStringProperties size=" + mCacheStringProperties.size());
                    Log.d(TAG, "mCacheOutputProperties size=" + mCacheOutputProperties.size());
                    Log.d(TAG, "mCacheFormatProperties size=" + mCacheFormatProperties.size());
                    Log.d(TAG, "mCacheReaderProperties size=" + mCacheReaderProperties.size());
                    Log.d(TAG, "mCacheProperties size=" + mCacheProperties.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //加载系统扫描服务读取的通用配置参数
    public void initScannerProviderProperty() {
        if(mCacheProperties.size() > 0) {
            return;
        }
        try {
            List<Property> parsePropertyList = DataParser.parseALLPropertyFromXML(mContext,"configs/scanner_default_property.xml");
            if(parsePropertyList != null) {
                for(Property property: parsePropertyList) {
                    if (Category.VAL_STRING.equals(property.getValueType())) {
                        mCacheStringProperties.put(property.getId(), property);
                    } else if (Category.CAT_OUTPUT.equals(property.getCategory())) {
                        mCacheOutputProperties.put(property.getId(), property);
                    } else if (Category.CAT_FORAMT.equals(property.getCategory())) {
                        mCacheFormatProperties.put(property.getId(), property);
                    } else if (Category.CAT_READER.equals(property.getCategory())) {
                        mCacheReaderProperties.put(property.getId(), property);
                    } else {
                        mCacheProperties.put(property.getId(), property);
                    }
                }
            }
            String[] pSettingsProjection = {
                    UConstants.KEY_NAME, UConstants.KEY_VALUE
            };
            Uri CONTENT_URI = Uri.parse(UConstants.CONTENT_URI_SETTINGS + UConstants.TABLE_SETTINGS);
            Cursor cursor = mContext.getContentResolver().query(CONTENT_URI,
                    pSettingsProjection, null,
                    null, null);
            if (cursor != null) {
                Property property = null;
                while (cursor.moveToNext()) {
                    String name = cursor.getString(0);
                    if(name == null) {
                        Log.e(TAG, "keyPropertyMap name is null=" + name);
                        continue;
                    }
                    Integer propId = -1;
                    if(name == "SCANNER_ENABLE") {
                        propId = 0;
                    } else {
                        propId = keyPropertyMap.get(name);
                        if(propId == null) {
                            Log.e(TAG, "keyPropertyMap name no difine=" + name);
                            continue;
                        }
                    }
                    String value = cursor.getString(1);
                    //旧版本os未初始化这两个属性 property.getValueType property.getCategory
                    if ((property = mCacheStringProperties.get(propId)) != null) {
                    } else if ((property = mCacheProperties.get(propId)) != null) {
                    } else if ((property = mCacheOutputProperties.get(propId)) != null) {
                    } else if ((property = mCacheFormatProperties.get(propId)) != null) {
                    } else if ((property = mCacheReaderProperties.get(propId)) != null) {
                    }
                    //Log.d(TAG, "keyPropertyMap name difine=" + name);
                    if(property != null) {
                        property.setValue(value);
                        if (Category.VAL_STRING.equals(property.getValueType())) {
                            mCacheStringProperties.put(property.getId(), property);
                        } else if (Category.CAT_OUTPUT.equals(property.getCategory())) {
                            mCacheOutputProperties.put(property.getId(), property);
                        } else if (Category.CAT_FORAMT.equals(property.getCategory())) {
                            mCacheFormatProperties.put(property.getId(), property);
                        } else if (Category.CAT_READER.equals(property.getCategory())) {
                            mCacheReaderProperties.put(property.getId(), property);
                        } else {
                            mCacheProperties.put(property.getId(), property);
                        }
                    }
                }
                cursor.close();
                cursor = null;
            } else {
                Log.e(TAG, "cursor is null=" + CONTENT_URI);
            }
            if(DEBUG) {
                Log.d(TAG, "mCacheStringProperties size=" + mCacheStringProperties.size());
                Log.d(TAG, "mCacheOutputProperties size=" + mCacheOutputProperties.size());
                Log.d(TAG, "mCacheFormatProperties size=" + mCacheFormatProperties.size());
                Log.d(TAG, "mCacheReaderProperties size=" + mCacheReaderProperties.size());
                Log.d(TAG, "mCacheProperties size=" + mCacheProperties.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //加载默认通用的配置
    public void initProperty() {
        if(mCacheProperties.size() > 0) {
            return;
        }
        try {
            List<Property> parsePropertyList = DataParser.parseALLPropertyFromXML(mContext,"configs/scanner_default_property.xml");
            if(parsePropertyList != null) {
                for(Property property: parsePropertyList) {
                    if (Category.VAL_STRING.equals(property.getValueType())) {
                        mCacheStringProperties.put(property.getId(), property);
                    } else if (Category.CAT_OUTPUT.equals(property.getCategory())) {
                        mCacheOutputProperties.put(property.getId(), property);
                    } else if (Category.CAT_FORAMT.equals(property.getCategory())) {
                        mCacheFormatProperties.put(property.getId(), property);
                    } else if (Category.CAT_READER.equals(property.getCategory())) {
                        mCacheReaderProperties.put(property.getId(), property);
                    } else {
                        mCacheProperties.put(property.getId(), property);
                    }
                }
            }
            Cursor cursor = mContentResolver.query(UConstants.CONTENT_URI_PROPERTY_SETTINGS, allProProjection,
                    /*UConstants.PROPERTY_NAME + "=? AND " + */UConstants.PROFILE_ID + "=?", new String[]{/*UConstants.DEFAULT, */String.valueOf(UConstants.DEFAULT_ID)}, null);
            if (cursor != null) {
                Property property = null;
                while (cursor.moveToNext()) {
                    int propId = cursor.getInt(0);
                    String value = cursor.getString(2);
                    //旧版本os未初始化这两个属性 property.getValueType property.getCategory
                    /*if (mCacheStringProperties.indexOfKey(propId) != -1) {
                        property = mCacheStringProperties.get(propId);
                    } else if (mCacheProperties.indexOfKey(propId) != -1) {
                        property = mCacheProperties.get(propId);
                    } else if (mCacheOutputProperties.indexOfKey(propId) != -1) {
                        property = mCacheOutputProperties.get(propId);
                    } else if (mCacheFormatProperties.indexOfKey(propId) != -1) {
                        property = mCacheFormatProperties.get(propId);
                    } else if (mCacheReaderProperties.indexOfKey(propId) != -1) {
                        property = mCacheReaderProperties.get(propId);
                    }*/
                    if ((property = mCacheStringProperties.get(propId)) != null) {
                    } else if ((property = mCacheProperties.get(propId)) != null) {
                    } else if ((property = mCacheOutputProperties.get(propId)) != null) {
                    } else if ((property = mCacheFormatProperties.get(propId)) != null) {
                    } else if ((property = mCacheReaderProperties.get(propId)) != null) {
                    }
                    if(property != null) {
                        property.setValue(value);
                        if (Category.VAL_STRING.equals(property.getValueType())) {
                            mCacheStringProperties.put(property.getId(), property);
                        } else if (Category.CAT_OUTPUT.equals(property.getCategory())) {
                            mCacheOutputProperties.put(property.getId(), property);
                        } else if (Category.CAT_FORAMT.equals(property.getCategory())) {
                            mCacheFormatProperties.put(property.getId(), property);
                        } else if (Category.CAT_READER.equals(property.getCategory())) {
                            mCacheReaderProperties.put(property.getId(), property);
                        } else {
                            mCacheProperties.put(property.getId(), property);
                        }
                    }
                }
                cursor.close();
                cursor = null;
            }
            if(DEBUG) {
                Log.d(TAG, "mCacheStringProperties size=" + mCacheStringProperties.size());
                Log.d(TAG, "mCacheOutputProperties size=" + mCacheOutputProperties.size());
                Log.d(TAG, "mCacheFormatProperties size=" + mCacheFormatProperties.size());
                Log.d(TAG, "mCacheReaderProperties size=" + mCacheReaderProperties.size());
                Log.d(TAG, "mCacheProperties size=" + mCacheProperties.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initPropertyForProfile(int profileId) {
        try {
            Cursor cursor = mContentResolver.query(UConstants.CONTENT_URI_PROPERTY_SETTINGS, allProProjection,
                    /*UConstants.PROPERTY_NAME + "=? AND " + */UConstants.PROFILE_ID + "=?", new String[]{/*UConstants.DEFAULT, */String.valueOf(profileId)}, null);
            if (cursor != null) {
                Property property;
                while (cursor.moveToNext()) {
                    property = new Property();
                    property.setId(cursor.getInt(0));
                    property.setName(cursor.getString(1));
                    property.setValue(cursor.getString(2));
                    property.setValueType(cursor.getString(3));
                    property.setMin(cursor.getInt(4));
                    property.setMax(cursor.getInt(5));
                    property.setSupportType(cursor.getString(6));
                    property.setCategory(cursor.getString(7));
                    if (Category.VAL_STRING.equals(property.getValueType())) {
                        mCacheStringProperties.put(property.getId(), property);
                    } else if (Category.CAT_OUTPUT.equals(property.getCategory())) {
                        mCacheOutputProperties.put(property.getId(), property);
                    } else if (Category.CAT_FORAMT.equals(property.getCategory())) {
                        mCacheFormatProperties.put(property.getId(), property);
                    } else if (Category.CAT_READER.equals(property.getCategory())) {
                        mCacheReaderProperties.put(property.getId(), property);
                    } else {
                        mCacheProperties.put(property.getId(), property);
                    }
                }
                cursor.close();
                cursor = null;
            }
            if(DEBUG) {
                Log.d(TAG, "mCacheStringProperties size=" + mCacheStringProperties.size());
                Log.d(TAG, "mCacheOutputProperties size=" + mCacheOutputProperties.size());
                Log.d(TAG, "mCacheFormatProperties size=" + mCacheFormatProperties.size());
                Log.d(TAG, "mCacheReaderProperties size=" + mCacheReaderProperties.size());
                Log.d(TAG, "mCacheProperties size=" + mCacheProperties.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public Cursor getPropertyForProfile(int profileId) {
        try {
            Cursor cursor = mContentResolver.query(UConstants.CONTENT_URI_PROPERTY_SETTINGS, new String[]{
                            UConstants.PROPERTY_ID,
                            UConstants.PROPERTY_NAME,
                            UConstants.PROPERTY_VALUE,
                            UConstants.PROPERTY_VALUE_TYPE,
                            UConstants.PROPERTY_GROUP
                    },
                    /*UConstants.PROPERTY_NAME + "=? AND " + */UConstants.PROFILE_ID + "=?", new String[]{/*UConstants.DEFAULT, */String.valueOf(profileId)}, null);
            return cursor;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public SparseArray<Property> getALLStringProperty(){
        return mCacheStringProperties;
    }
    public SparseArray<Property> getALLReaderProperty(){
        return mCacheReaderProperties;
    }
    public SparseArray<Property> getALLOutputProperty(){
        return mCacheOutputProperties;
    }
    public SparseArray<Property> getALLFormatProperty(){
        return mCacheFormatProperties;
    }
    /**
     * 获取全部与扫描头解码内部相关参数
     * @return
     */
    public SparseArray<Property> getALLDecoderProperty(){
        return mCacheProperties;
    }

    /**
     * 获取指定属性的字符串值
     * @param propId
     * @return
     */
    public String getStringProperty(int propId) {
        synchronized (mCacheStringProperties) {
            Property property = null;
            if ((property = mCacheStringProperties.get(propId)) != null) {
                //Property property = mCacheStringProperties.get(propId);
                if (property != null) {
                    return property.getValue();
                }
            }
        }
        return "";
    }

    public boolean isStringProperty(int propId) {
        synchronized (mCacheStringProperties) {
            Property property = null;
            if ((property = mCacheStringProperties.get(propId)) != null) {
                //Property property = mCacheStringProperties.get(propId);
                if (property != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 更新指定属性的字符串类型值
     *
     * @param propId
     * @param value
     * @return
     */
    public int setStringProperty(int propId, String value) {
        synchronized (mCacheStringProperties) {
            Property property = null;
            if ((property = mCacheStringProperties.get(propId)) != null) {
                if (DEBUG)
                    Log.d(TAG, "mCacheStringProperties size=" + mCacheStringProperties.size());
                //Property property = mCacheStringProperties.get(propId);
                if (property != null) {
                    property.setValue(value);
                    mCacheStringProperties.put(propId, property);
                    if (DEBUG)
                        Log.d(TAG, "mCacheStringProperties end size=" + mCacheStringProperties.size());
                }
            }
        }
        return -1;
    }
    /**
     * 获取指定属性的int值
     * @param propId
     * @return
     */
    public int getIntProperty(int propId) {
        synchronized (mLock) {
            int value = -1;
            Property property = null;
            if ((property = mCacheProperties.get(propId)) != null) {
                try {
                    //Property property = mCacheProperties.get(propId);
                    if (property != null) {
                        value = Integer.parseInt(property.getValue());
                    }
                } catch (NumberFormatException w) {
                    w.printStackTrace();
                }
            } else if ((property = mCacheOutputProperties.get(propId)) != null) {
                try {
                    //Property property = mCacheOutputProperties.get(propId);
                    if (property != null) {
                        value = Integer.parseInt(property.getValue());
                    }
                } catch (NumberFormatException w) {
                    w.printStackTrace();
                }
            } else if ((property = mCacheFormatProperties.get(propId)) != null) {
                try {
                    // Property property = mCacheFormatProperties.get(propId);
                    if (property != null) {
                        value = Integer.parseInt(property.getValue());
                    }
                } catch (NumberFormatException w) {
                    w.printStackTrace();
                }
            } else if ((property = mCacheReaderProperties.get(propId)) != null) {
                try {
                    //Property property = mCacheReaderProperties.get(propId);
                    if (property != null) {
                        value = Integer.parseInt(property.getValue());
                    }
                } catch (NumberFormatException w) {
                    w.printStackTrace();
                }
            }
            return value;
        }
    }

    public int getDecoderProperty(int propId) {
        synchronized (mLock) {
            int value = -1;
            Property property = null;
            if ((property = mCacheProperties.get(propId)) != null) {
                try {
                    //Property property = mCacheProperties.get(propId);
                    if (property != null) {
                        value = Integer.parseInt(property.getValue());
                    }
                } catch (NumberFormatException w) {
                    w.printStackTrace();
                }
            }
            return value;
        }
    }

    public int getFormatProperty(int propId) {
        synchronized (mLock) {
            int value = -1;
            Property property = null;
            if ((property = mCacheFormatProperties.get(propId)) != null) {
                try {
                    //Property property = mCacheFormatProperties.get(propId);
                    if (property != null) {
                        value = Integer.parseInt(property.getValue());
                    }
                } catch (NumberFormatException w) {
                    w.printStackTrace();
                }
            }
            return value;
        }
    }
    /**
     * 更新指定属性的int类型值
     * @param propId
     * @param value
     * @return
     */
    public int setIntProperty(int propId, int value) {
        synchronized (mLock) {
            Property property = null;
            if ((property = mCacheProperties.get(propId)) != null) {
                try {
                    //Property property = mCacheProperties.get(propId);
                    if (property != null) {
                        if(DEBUG)
                            Log.d(TAG, "mCacheStringProperties "
                                    + " propId " + property.getId()
                                    + " Name " + property.getName()
                                    + " Min " + property.getMin()
                                    + " Max " + property.getMax()
                                    + " value " + property.getValue()

                            );
                        if(value >= property.getMin() && value <= property.getMax()) {
                            property.setValue(String.valueOf(value));
                            mCacheProperties.put(propId, property);
                            return 0;
                        }
                    }
                } catch (Exception w) {
                    w.printStackTrace();
                }
            } else if ((property = mCacheOutputProperties.get(propId)) != null) {
                try {
                    //Property property = mCacheOutputProperties.get(propId);
                    if (property != null) {
                        if(DEBUG)
                            Log.d(TAG, "mCacheOutputProperties "
                                    + " propId " + property.getId()
                                    + " Name " + property.getName()
                                    + " Min " + property.getMin()
                                    + " Max " + property.getMax()
                                    + " value " + property.getValue()

                            );
                        if(value >= property.getMin() && value <= property.getMax()) {
                            property.setValue(String.valueOf(value));
                            mCacheOutputProperties.put(propId, property);
                            return 0;
                        }
                    }
                } catch (Exception w) {
                    w.printStackTrace();
                }
            } else if ((property = mCacheFormatProperties.get(propId)) != null) {
                try {
                    //Property property = mCacheFormatProperties.get(propId);
                    if (property != null) {
                        if(DEBUG)
                            Log.d(TAG, "mCacheFormatProperties "
                                    + " propId " + property.getId()
                                    + " Name " + property.getName()
                                    + " Min " + property.getMin()
                                    + " Max " + property.getMax()
                                    + " value " + property.getValue()

                            );
                        if(value >= property.getMin() && value <= property.getMax()) {
                            property.setValue(String.valueOf(value));
                            mCacheFormatProperties.put(propId, property);
                            return 0;
                        }
                    }
                } catch (Exception w) {
                    w.printStackTrace();
                }
            } else if ((property = mCacheReaderProperties.get(propId)) != null) {
                try {
                    //Property property = mCacheReaderProperties.get(propId);
                    if (property != null) {
                        if(DEBUG)
                            Log.d(TAG, "mCacheReaderProperties "
                                    + " propId " + property.getId()
                                    + " Name " + property.getName()
                                    + " Max " + property.getMin()
                                    + " Min " + property.getMax()
                                    + " value " + property.getValue()

                            );
                        if(value >= property.getMin() && value <= property.getMax()) {
                            property.setValue(String.valueOf(value));
                            mCacheReaderProperties.put(propId, property);
                            return 0;
                        }
                    }
                } catch (Exception w) {
                    w.printStackTrace();
                }
            }
            return -1;
        }
    }

    /**
     * 默认配置profile
     * 批量更新参数到数据库
     * @param values
     * @return
     */
    public boolean putBulkStrings(ContentValues[] values) {
        try {
            mContentResolver.bulkInsert(UConstants.CONTENT_URI_PROPERTY_SETTINGS, values);
            return true;
        } catch (SQLException eq) {
            eq.printStackTrace();
            return false;
        }
    }
    public boolean putStrings(ContentValues values) {
        try {
            mContentResolver.insert(UConstants.CONTENT_URI_PROPERTY_SETTINGS, values);
            return true;
        } catch (SQLException eq) {
            eq.printStackTrace();
            return false;
        }
    }
    public boolean putString(int propertyId, String name, String value) {
        try {
            ContentValues values = new ContentValues();
            values.put(UConstants.PROPERTY_NAME, name);
            values.put(UConstants.PROPERTY_VALUE, value);
            values.put(UConstants.PROPERTY_ID, propertyId);
            values.put(UConstants.PROFILE_ID, UConstants.DEFAULT_ID);
            mContentResolver.insert(UConstants.CONTENT_URI_PROPERTY_SETTINGS, values);
            return true;
        } catch (SQLException e) {
            Log.w(TAG, "Can't set key " + name + " in " + UConstants.CONTENT_URI_PROPERTY_SETTINGS, e);
            return false;
        }
    }
    public String getString(String name) {
        String outVal = null;
        Cursor c = null;
        try {
            c = mContentResolver.query(UConstants.CONTENT_URI_PROPERTY_SETTINGS,
                    new String[]{UConstants.PROPERTY_NAME, UConstants.PROPERTY_VALUE},
                    UConstants.PROPERTY_NAME + "=? AND " + UConstants.PROFILE_ID + "=?",
                    new String[]{name, String.valueOf(UConstants.DEFAULT_ID)}, null);
            int column = c.getColumnIndexOrThrow(UConstants.PROPERTY_VALUE);
            while (c.moveToNext()) {
                String temp = c.getString(column);
                if (temp != null) {
                    outVal = c.getString(column);
                }
            }
        } catch (IllegalArgumentException e) {
            // Column not found
            Log.w(TAG, "Value column not found", e);
        } finally {
            if (c != null) c.close();
        }
        return outVal;
    }
    private static HashMap<String, Integer> propertyPopulate() {
        Field[] keyFields = PropertyID.class.getDeclaredFields();
        HashMap<String, Integer> keyMap = new HashMap<String, Integer>();

        String tmpName = null;
        try {
            for (int i = 0; i < keyFields.length; i++) {
                tmpName = keyFields[i].getName();
                int modi = keyFields[i].getModifiers();
                if (Modifier.isStatic(modi) && Modifier.isPublic(modi)) {
                    int keycode = (Integer) keyFields[i].get(null);
                    keyMap.put(tmpName, keycode);
                }
            }
        } catch (NullPointerException e) {
            Log.w(TAG, "Non-static field : " + tmpName);
        } catch (IllegalArgumentException e1) {
            Log.w(TAG, "Type mismatch : " + tmpName);
        } catch (IllegalAccessException e2) {
            Log.w(TAG, "Non-public field : " + tmpName);
        }

        /*keyNames = new ArrayList<String>(keyMap.keySet());
        Collections.sort(keyNames);*/
        return keyMap;
    }
}
