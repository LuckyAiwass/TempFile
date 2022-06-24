package com.ubx.propertyparser;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
 * @Date: 20-5-27下午3:26
 */

public class DataParser {
    public static final String SCANNER_DEFAULT_PROP = "etc/scanner_default_property.xml";
    public static final String SCANNER_DEFAULT_PROP_ASSETS = "configs/scanner_default_property.xml";
    //public static final String SCANNER_DEFAULT_PROP_CUSTOM = "etc/scanner_default_property_%s.xml";
    public static final String SCANNER_DEFAULT_PROP_CUSTOM = "etc/scanner_custom_property.xml";
    private static boolean DEBUG = false;
    private static final String TAG = "DataParser";
    private final static String propertygroup_TAG = "propertygroup";
    private final static String property_TAG = "property";
    private final static String id_TAG = "id";
    private final static String name_TAG = "name";
    private final static String scannertype_TAG = "scannertype";
    private final static String category_TAG = "category";
    private final static String value_type_TAG = "value_type";
    private final static String displayName_TAG = "displayName";
    private final static String discreteCount_TAG = "discreteCount";
    private final static String discreteEntryValues_TAG = "discreteEntryValues";
    private final static String discreteEntries_TAG = "discreteEntries";
    private final static String value_min_TAG = "value_min";
    private final static String value_max_TAG = "value_max";
    private final static String paramNum_TAG = "paramNum";
    private final static String defaultValue_TAG = "defaultValue";

    /**
     * 只解析包含以下信息的XML文件
     * <property id="6" name="SEND_GOOD_READ_BEEP_ENABLE" >0</property>
     * @param path /system/etc/default_property.xml
     * @return List<Property>
     *
     */
    public static SparseArray<Property> parsePropertyArrayXML(Context context, String path) {
        Property property;
        SparseArray<Property> listProperty = new SparseArray<>();
        InputStream inputStream = null;
        try {
            if (context != null) {
                inputStream = context.getResources().getAssets().open(path);
            } else {
                inputStream = new FileInputStream(path);
            }
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, "utf-8");

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (property_TAG.equals(parser.getName())) {
                            property = new Property();
                            //String name = parser.getAttributeValue(null, "name");
                            String id = parser.getAttributeValue(0);
                            Integer key = Integer.parseInt(id);
                            property.setId(key);
                            String name = parser.getAttributeValue(1);
                            property.setName(name);
                            //ULog.i("debug","name: " +name);
                            String value = parser.nextText();
                            property.setValue(value);
                            listProperty.put(key, property);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        property = null;
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return listProperty;
    }
    public static List<Property> parsePropertyXML(Context context, String path) {
        Property property;
        List<Property> listProperty = null;
        InputStream inputStream = null;
        try {
            if (context != null) {
                inputStream = context.getResources().getAssets().open(path);
            } else {
                inputStream = new FileInputStream(path);
            }
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, "utf-8");

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        listProperty = new ArrayList<>();
                        break;
                    case XmlPullParser.START_TAG:
                        if (property_TAG.equals(parser.getName())) {
                            property = new Property();
                            //String name = parser.getAttributeValue(null, "name");
                            String id = parser.getAttributeValue(0);
                            Integer key = Integer.parseInt(id);
                            property.setId(key);
                            String name = parser.getAttributeValue(1);
                            property.setName(name);
                            //ULog.i("debug","name: " +name);
                            String value = parser.nextText();
                            property.setValue(value);

                            listProperty.add(property);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        property = null;
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return listProperty;
    }

    //<property id="6" name="SEND_GOOD_READ_BEEP_ENABLE" scannertype="*" category="READER" value_type="DISCRETE" value_min="0" value_max="2">0</property>
    public static List<Property> parseALLPropertyFromXML(Context context, String path) {
        try{
            InputStream inputStream = null;
            if (context != null) {
                inputStream = context.getResources().getAssets().open(path);
            } else {
                inputStream = new FileInputStream(path);
            }
            return parseALLPropertyFromXML(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static List<Property> parseALLPropertyFromXML(InputStream inputStream) {
        List<Property> listProperty = null;
        if(inputStream != null) {
            Property property;
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(inputStream, "utf-8");

                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            listProperty = new ArrayList<>();
                            break;
                        case XmlPullParser.START_TAG:
                            if (property_TAG.equals(parser.getName())) {
                                property = new Property();
                                //String name = parser.getAttributeValue(null, "name");
                                String id = parser.getAttributeValue(0);
                                if (DEBUG) Log.d(TAG, "id = " + id);
                                Integer key = Integer.parseInt(id);
                                property.setId(key);
                                String name = parser.getAttributeValue(1);
                                if (DEBUG) Log.d(TAG, "name = " + name);
                                property.setName(name);
                                String scannertype = parser.getAttributeValue(2);
                                if (DEBUG) Log.d(TAG, "scannertype = " + scannertype);
                                property.setSupportType(scannertype);
                                String category = parser.getAttributeValue(3);
                                if (DEBUG) Log.d(TAG, "category = " + category);
                                property.setCategory(category);
                                String value_type = parser.getAttributeValue(4);
                                if (DEBUG) Log.d(TAG, "value_type = " + value_type);
                                property.setValueType(value_type);
                                String value_min = parser.getAttributeValue(5);
                                if (DEBUG) Log.d(TAG, "value_min = " + value_min);
                                try {
                                    property.setMin(Integer.parseInt(value_min));
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                String value_max = parser.getAttributeValue(6);
                                if (DEBUG) Log.d(TAG, "value_max = " + value_max);
                                try {
                                    property.setMax(Integer.parseInt(value_max));
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                String value = parser.nextText();
                                if (DEBUG) Log.d(TAG, "value = " + value);
                                property.setValue(value);
                                property.setDefaultValue(value);
                                listProperty.add(property);
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            property = null;
                            break;
                    }
                    eventType = parser.next();
                }
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return listProperty;
    }
    public static SparseArray<Property> parsePropertyFromXML(Context context, String path) {
        Property property;
        SparseArray<Property> listProperty = new SparseArray<>();
        InputStream inputStream = null;
        try {
            if (context != null) {
                inputStream = context.getResources().getAssets().open(path);
            } else {
                inputStream = new FileInputStream(path);
            }
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, "utf-8");

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (property_TAG.equals(parser.getName())) {
                            property = new Property();
                            //String name = parser.getAttributeValue(null, "name");
                            String id = parser.getAttributeValue(0);
                            if (DEBUG) Log.d(TAG, "id = " + id);
                            Integer key = Integer.parseInt(id);
                            property.setId(key);
                            String name = parser.getAttributeValue(1);
                            if (DEBUG) Log.d(TAG, "name = " + name);
                            property.setName(name);
                            String scannertype = parser.getAttributeValue(2);
                            if (DEBUG) Log.d(TAG, "scannertype = " + scannertype);
                            property.setSupportType(scannertype);
                            String category = parser.getAttributeValue(3);
                            if (DEBUG) Log.d(TAG, "category = " + category);
                            property.setCategory(category);
                            String value_type = parser.getAttributeValue(4);
                            if (DEBUG) Log.d(TAG, "value_type = " + value_type);
                            property.setValueType(value_type);
                            String value_min = parser.getAttributeValue(5);
                            if (DEBUG) Log.d(TAG, "value_min = " + value_min);
                            try {
                                property.setMin(Integer.parseInt(value_min));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            String value_max = parser.getAttributeValue(6);
                            if (DEBUG) Log.d(TAG, "value_max = " + value_max);
                            try {
                                property.setMax(Integer.parseInt(value_max));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            String value = parser.nextText();
                            if (DEBUG) Log.d(TAG, "value = " + value);
                            property.setValue(value);

                            listProperty.put(property.getId(), property);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        property = null;
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return listProperty;
    }
    /**
     * input 流转换为字符串
     *
     * @param is
     * @return
     */
    private static String convertStreamToString(InputStream is) {
        String s = null;
        try {
            //格式转换
            Scanner scanner = new Scanner(is, "UTF-8").useDelimiter("\\A");
            if (scanner.hasNext()) {
                s = scanner.next();
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }
    //开机后只加载一次
    private static final String ONEWAY_PARSE = "sdcard/oneway_load_property.json";
    //每次打开扫描头都加载
    private static final String ALWAYS_PARSE = "sdcard/always_load_property.json";
    public static final int ZEBRA_ENGINE = 1;
    public static final int HONEYWELL_ENGINE = 2;
    public static final int ALL_ENGINE = 0;
    public static int checkPropertyFileExists(boolean hasParse) {
        try {
            File file = new File(ALWAYS_PARSE);
            boolean exists = file.exists();
            if(exists) {
                Log.d(TAG, "exists = " + ALWAYS_PARSE);
                return 2;
            }
            //如果设备在开机或打开扫描头时已经加载过一次，就不再加载，除非更改文件名为always
            if(hasParse == false) {
                file = new File(ONEWAY_PARSE);
                boolean onewayExists = file.exists();
                if(onewayExists) {
                    Log.d(TAG, "onewayExists = " + ONEWAY_PARSE);
                    return 1;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }
    public static List<Property> parseALLPropertyFromJSONFile(Context context, String path) {
        InputStream inputStream = null;
        try {
            if (context != null) {
                inputStream = context.getResources().getAssets().open(path);
            } else {
                inputStream = new FileInputStream(path);
            }
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
            return parseALLPropertyFromJSON(0, stringBuilder.toString(), true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 打开扫描头时加载无法通过界面设置解码库的参数
     * @param target 加载文件名称编号
     * @param allMode　是否需求全部配置数据
     * @return
     */
    public static List<Property> parseDecoderPropertyFromJSONFile(int engineType, int target, boolean allMode) {
        InputStream inputStream = null;
        try {
            if(target == 2) {
                inputStream = new FileInputStream(ALWAYS_PARSE);
            } else {
                inputStream = new FileInputStream(ONEWAY_PARSE);
            }
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
            return parseALLPropertyFromJSON(engineType, stringBuilder.toString(), allMode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    /*"id": 300,
    "name": "SD_PROP_EDGE_DETECTOR",
    "scannertype": "8,15",
    "category": "DECODER",
    "value_type": "INTEGER",
    "displayName": "Increase depth for linear codes",
    "discreteCount": 0,
    "discreteEntryValues": "0",
    "discreteEntries": "0",
    "value_min": 0,
    "value_max": 1,
    "paramNum": 1074790403,
    "defaultValue": "0"*/
    public static List<Property> parseALLPropertyFromJSON(int engineType, String jsonString, boolean allMode) {
        Property property;
        List<Property> listProperty = new ArrayList<>();
        try {
            //if (DEBUG) Log.d(TAG, "jsonString = " + jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray propertyArray = jsonObject.getJSONArray(propertygroup_TAG);
            for (int i = 0; i < propertyArray.length(); i++) {
                JSONObject objectArray = propertyArray.getJSONObject(i);
                if(objectArray != null) {
                    String scannertype = objectArray.getString(scannertype_TAG);
                    if (DEBUG) Log.d(TAG, "scannertype = " + scannertype);
                    if(engineType > 0 && isSupport(scannertype, String.valueOf(engineType)) == false) {
                        continue;
                    }
                    property = new Property();
                    if(allMode) {
                        property.setSupportType(scannertype);
                        int id = objectArray.getInt(id_TAG);
                        if (DEBUG) Log.d(TAG, "id = " + id);
                        property.setId(id);
                        String name = objectArray.getString(name_TAG);
                        if (DEBUG) Log.d(TAG, "name = " + name);
                        property.setName(name);
                        String category = objectArray.getString(category_TAG);
                        if (DEBUG) Log.d(TAG, "category = " + category);
                        property.setCategory(category);
                    }

                    if(allMode) {
                        String value_type = objectArray.getString(value_type_TAG);
                        if (DEBUG) Log.d(TAG, "value_type = " + value_type);
                        property.setValueType(value_type);
                        String displayName = objectArray.getString(displayName_TAG);
                        if (DEBUG) Log.d(TAG, "displayName = " + displayName);
                        property.setDisplayName(displayName);
                        int discreteCount = objectArray.getInt(discreteCount_TAG);
                        if (DEBUG) Log.d(TAG, "discreteCount = " + discreteCount);
                        property.setDiscreteCount(discreteCount);
                        String discreteEntryValues = objectArray.getString(discreteEntryValues_TAG);
                        if (DEBUG) Log.d(TAG, "discreteEntryValues = " + discreteEntryValues);
                        property.setDiscreteEntryValues(discreteEntryValues);
                        String discreteEntries = objectArray.getString(discreteEntries_TAG);
                        if (DEBUG) Log.d(TAG, "discreteEntries = " + discreteEntries);
                        property.setDiscreteEntries(discreteEntries);
                        int value_min = objectArray.getInt(value_min_TAG);
                        if (DEBUG) Log.d(TAG, "value_min = " + value_min);
                        property.setMin(value_min);
                        int value_max = objectArray.getInt(value_max_TAG);
                        if (DEBUG) Log.d(TAG, "value_max = " + value_max);
                        property.setMax(value_max);
                    }
                    int paramNum = objectArray.getInt(paramNum_TAG);
                    if (DEBUG) Log.d(TAG, "paramNum = " + paramNum);
                    property.setParamNum(paramNum);
                    String defaultValue = objectArray.getString(defaultValue_TAG);
                    if (DEBUG) Log.d(TAG, "defaultValue = " + defaultValue);
                    property.setDefaultValue(defaultValue);
                    property.setValue(defaultValue);
                    listProperty.add(property);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listProperty;
    }
    public static boolean isSupport(String supportType, String type) {
        if (!TextUtils.isEmpty(supportType)) {
            if (supportType.equals("*")) {
                return true;
            } else {
                String[] list = supportType.split(",");
                for (String id : list) {
                    if (type.equals(id)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
