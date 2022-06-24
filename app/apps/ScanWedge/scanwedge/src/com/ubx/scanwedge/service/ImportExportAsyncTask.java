package com.ubx.scanwedge.service;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.device.scanner.configuration.PropertyID;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;

import com.ubx.propertyparser.DataParser;
import com.ubx.propertyparser.Property;
import com.ubx.scanwedge.R;
import com.ubx.database.helper.UConstants;
import com.ubx.database.helper.USettings;
import com.ubx.scanwedge.settings.utils.ULog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
 * @Author: rocky(xiejifu)
 * @Date: 19-04-29上午11:41
 */
/**
 * 完成配置导入导出配置文件
 * <propertygroup>
 *     <property name="SCANNER_ENABLE">1</property>
 *     <property name="IMAGE_EXPOSURE_MODE">0</property>
 *     <property name="IMAGE_FIXED_EXPOSURE">240</property>
 *     <property name="SEND_GOOD_READ_BEEP_ENABLE">0</property>
 *     <property name="SEND_GOOD_READ_VIBRATE_ENABLE">0</property>
 *     <property name="TRIGGERING_MODES">8</property>
 *     <property name="GOOD_READ_BEEP_ENABLE">1</property>
 *     <property name="GOOD_READ_VIBRATE_ENABLE">0</property>
 *     <property name="LABEL_APPEND_ENTER">0</property>
 *     <property name="IMAGE_PICKLIST_MODE">0</property>
 *     <property name="IMAGE_ONE_D_INVERSE">0</property>
 */
public class ImportExportAsyncTask extends AsyncTask<String, String, Integer> {
    private static final String TAG = "Wedge" +"IETask";
    public static final String DEFAULT_IES_CONFIG_PROFILE_NAME = "sdcard/scanner_property.xml";
    //执行配置操作
    public static final String IES_CONFIG_ACTION = "config_action";
    public static final int IES_CONFIG_ACTION_UNKNOWN = 0;
    //导入配置文件
    public static final int IES_CONFIG_ACTION_IMPORT = 1;
    //导出配置文件
    public static final int IES_CONFIG_ACTION_EXPORT = 2;
    //开机自动检查导入指定配置文件
    public static final int IES_CONFIG_ACTION_AUTO = 3;
    //配置文件的Profile 名称
    public static final String IES_CONFIG_PROFILE_NAME = "profileName";
    //指定导入或导出配置文件路径
    public static final String IES_CONFIG_PROFILE_PATH = "configFilepath";
    private Context mContext;
    private int mActionKey;
    private boolean mForegroundActivity = false;
    private ProgressDialog pdialog;
    public ImportExportAsyncTask(Context context) {
        super();
        mContext = context;
    }
    public ImportExportAsyncTask(Context context, int action, boolean foregroundActivity) {
        super();
        mContext = context;
        mActionKey = action;
        mForegroundActivity = foregroundActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(mForegroundActivity) {
            pdialog = new ProgressDialog(mContext);
            if (mActionKey == IES_CONFIG_ACTION_IMPORT) {
                pdialog.setMessage(mContext.getResources().getString(R.string.importing_config));
            } else {
                pdialog.setMessage(mContext.getResources().getString(R.string.exporting_config));
            }
            pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pdialog.setCancelable(false);
            pdialog.show();
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if(mForegroundActivity) {
            if (pdialog != null) pdialog.dismiss();
        }

    }

    @Override
    protected void onProgressUpdate(String[] values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(Integer o) {
        super.onCancelled(o);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected Integer doInBackground(String... params) {
        int ret = -1;
        String profileName = params[0];
        switch (mActionKey) {
            case IES_CONFIG_ACTION_IMPORT:
            {
                String fileName = params[1];
                ret = importScannerConfig(null, fileName, profileName);
            }
                break;
            case IES_CONFIG_ACTION_EXPORT:{
                ret = exportScannerConfig(profileName);
            }
            break;
            case IES_CONFIG_ACTION_AUTO:{
                String custom = android.os.SystemProperties.get("pwv.custom.custom", "XX");
                InputStream inputStream;
                try {
                    inputStream = mContext.getResources().getAssets().open("configs/" + custom + "_scanner_property.xml");
                    if(inputStream != null) {
                        ret = importScannerConfig(inputStream, null, profileName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception es) {
                }
                if("WALMART".equals(custom)) {
                    try {
                        inputStream = mContext.getResources().getAssets().open("configs/ims_scanner_property.xml");
                        if(inputStream != null) {
                            ret = importScannerConfig(inputStream, null, profileName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        BootReceiver.writeBoolean(mContext, "autoimport", false);
                    } catch (Exception es) {
                    }
                    try {
                        inputStream = mContext.getResources().getAssets().open("configs/terminalemulator_scanner_property.xml");
                        if(inputStream != null) {
                            ret = importScannerConfig(inputStream, null, profileName);
                        }
                    } catch (IOException e) {
                        BootReceiver.writeBoolean(mContext, "autoimport", false);
                        e.printStackTrace();
                    } catch (Exception es) {
                    }
                }
            }
                break;
        }
        return ret;
    }
    /**
     * 添加新的Profile 名称
     * @param contentResolver
     * @param mProfileName
     * @param profilePackages
     * @return
     */
    private int addNewSettings(ContentResolver contentResolver, String mProfileName, String[] profilePackages) {
        int profileId = (int) USettings.Profile.createProfile(contentResolver, mProfileName, true, true);
        if (profileId == -1) {
            ULog.e(TAG, "add TABLE_PROFILES error, profileName no exist");
            profileId = USettings.Profile.getId(contentResolver, mProfileName, USettings.Profile.DEFAULT_ID);
        }
        ULog.e(TAG, "addNewSettings profileId=" + profileId);
        if(profilePackages != null) {
            for(String mPackageName:profilePackages) {
                int listId = (int) USettings.AppList.refreshList(contentResolver, profileId, mPackageName);
                if (listId == -1) {
                    ULog.e(TAG, "add TABLE_APP_LIST error, packageName already added");
                }
            }
        }
        //USettings.System.initSettings(contentResolver, profileId);
        return profileId;
    }

    /**
     * 导入配置文件
     * @param inStream
     * @param propertyFile
     * @param profileName
     * @return
     */
    public int importScannerConfig(InputStream inStream, String propertyFile, String profileName) {
        int profileId = USettings.Profile.DEFAULT_ID;
        boolean existProfile = false;
        String scanwedgeEnable = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        if(!TextUtils.isEmpty(profileName)) {
            profileId = USettings.Profile.getId(contentResolver, profileName, USettings.Profile.DEFAULT_ID);
        }
        if (TextUtils.isEmpty(propertyFile)) {
            propertyFile = "sdcard/scanner_property.xml";
        }
        List<Property> listProperty = null;
        Property property = null;
        InputStream inputStream = null;
        String profilePackages= null;
        try {
            if(inStream != null) {
                inputStream = inStream;
            } else {
                inputStream = new FileInputStream(propertyFile);
            }
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, "utf-8");

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        listProperty = new ArrayList<Property>();
                        break;
                    case XmlPullParser.START_TAG:
                        if ("property".equals(parser.getName())) {
                            property = new Property();
                            String id = parser.getAttributeValue(0);
                            Integer key = Integer.parseInt(id);
                            property.setId(key);
                            String name = parser.getAttributeValue(1);
                            property.setName(name);
                            //android.util.Log.i("debug","name: " +name);
                            String value = parser.nextText();
                            property.setValue(value);

                            listProperty.add(property);
                        } else if("profileName".equals(parser.getName())) {
                            profileName = parser.nextText();
                            if(!TextUtils.isEmpty(profileName)) {
                                existProfile = USettings.Profile.existProfile(contentResolver, profileName);
                                if(existProfile) {
                                    boolean prefProfileName = BootReceiver.readBoolean(mContext, profileName);
                                    Log.d(TAG,"inProfileName = " + profileName + " profileName already exist prefProfileName=" + prefProfileName);
                                    if(prefProfileName) {
                                        break;
                                    } else {
                                        existProfile = false;
                                    }
                                }
                                profileId = USettings.Profile.getId(contentResolver, profileName, profileId);
                            }
                            Log.d(TAG,"inProfileName = " + profileName + " profileId = " + profileId);
                        } else if("profilePackages".equals(parser.getName())){
                            profilePackages = parser.nextText();
                            Log.d(TAG,"profilePackages = " + profilePackages);
                        } else if("scanwedgeEnable".equals(parser.getName())) {
                            scanwedgeEnable = parser.nextText();
                            Log.d(TAG,"scanwedgeEnable = " + scanwedgeEnable);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        Log.d(TAG,"END_TAG = " + parser.getName());
                        property = null;
                        break;

                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {

            }
        }
        if(existProfile) {
            Log.d(TAG,"inProfileName = " + profileName + " profileName already exist ");
            return 0;
        }
        //标记已经存在，开机不在重复导入
        BootReceiver.writeBoolean(mContext, profileName, true);
        if(TextUtils.isEmpty(scanwedgeEnable) == false) {
            try{
                USettings.DW.putString(mContext.getContentResolver(), UConstants.DW_ENABLED, scanwedgeEnable);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try{
            String [] profilePackageName = null;
            if(TextUtils.isEmpty(profilePackages) == false) {
                profilePackageName = profilePackages.split(",");
            }
            profileId = addNewSettings(contentResolver, profileName, profilePackageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            int len = listProperty.size();
            if(len > 0) {
                SparseArray<Property> parsePropertyList = DataParser.parsePropertyFromXML(mContext,"configs/scanner_default_property.xml");
                ContentValues[] currValues = new ContentValues[len];
                int currIndex = 0;
                for (int i = 0; i < len; i++) {
                    Property prop = listProperty.get(i);
                    if (prop != null) {
                        if(parsePropertyList.indexOfKey(prop.getId()) != -1) {
                            Property newproperty = parsePropertyList.get(prop.getId());
                            newproperty.setValue(prop.getValue());
                            currValues[currIndex++] = createContentValues(newproperty, profileId);
                        }
                        //USettings.System.putString(contentResolver, profileId, prop.getId(), prop.getName(), prop.getValue());
                    }
                }
                if(currIndex > 0) {
                    USettings.System.putBulkStrings(contentResolver, currValues);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            if(!TextUtils.isEmpty(propertyFile) && propertyFile.endsWith("autoimport_scanner_property.xml")) {
                File configFile = new File(propertyFile);
                configFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    private ContentValues createContentValues(Property property, int profileId) {
        ContentValues values = new ContentValues();
        values.put(UConstants.PROFILE_ID, profileId);
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
    private static final String[] sSettingsProjection = {
            UConstants.PROPERTY_ID, UConstants.PROPERTY_NAME, UConstants.PROPERTY_VALUE
    };
    //导出配置文件
    public int exportScannerConfig(String profileName) {
        int profileId = USettings.Profile.DEFAULT_ID;
        StringBuffer profilePackages = null;
        ContentResolver contentResolver = mContext.getContentResolver();
        if(!TextUtils.isEmpty(profileName)) {
            profileId = USettings.Profile.getId(contentResolver, profileName, USettings.Profile.DEFAULT_ID);
            List<String> profileListPackages = USettings.AppList.getAddedPackages(contentResolver, profileId);
            if(profileListPackages != null && profileListPackages.size() > 0) {
                profilePackages = new StringBuffer();
                for(int i=0 ; i < profileListPackages.size(); i++) {
                    Log.e(TAG, "profileListPackages: " + profileListPackages.get(i));
                    if(i + 1 < profileListPackages.size())
                        profilePackages.append(profileListPackages.get(i)).append(",");
                    else
                        profilePackages.append(profileListPackages.get(i));
                }
            }
        }
        int result = -1;
        Cursor cursor = null;
        if(USettings.SYNC_TO_NEW_SETTINGS && profileId == USettings.Profile.DEFAULT_ID) {
            cursor = mContext.getContentResolver().query(USettings.System.CONTENT_URI,
                    null, null, null, null);
        } else {
            cursor = mContext.getContentResolver().query(USettings.System.CONTENT_URI_PROPERTY_SETTINGS,
                    sSettingsProjection, UConstants.PROFILE_ID + "=?", new String[]{String.valueOf(profileId)}, null);
        }
        try {
            HashMap<String, Integer> keyMap = propertyPopulate();
            if (cursor != null) {
                cursor.moveToFirst();
                String enter = System.getProperty("line.separator");//换行
                File newxmlfile = new File("/sdcard/"+ profileName +"_scanner_property.xml");
                try {
                    if (!newxmlfile.exists())
                        newxmlfile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("IOException", "exception in createNewFile() method");
                    result = -1;
                }
                FileOutputStream fileos = null;
                try {
                    fileos = new FileOutputStream(newxmlfile);
                } catch (FileNotFoundException e) {
                    Log.e("FileNotFoundException", "can't create FileOutputStream");
                    result = -1;
                }
                XmlSerializer serializer = Xml.newSerializer();
                serializer.setOutput(fileos, "UTF-8");
                serializer.startDocument("UTF-8", true);
                changeLine(serializer, enter);
                serializer.startTag(null, "propertygroup");
                changeLine(serializer, enter);
                addTab(serializer, 1);
                serializer.startTag(null, "profileName");
                serializer.text(profileName);
                serializer.endTag(null, "profileName");
                changeLine(serializer, enter);
                if(profilePackages != null) {
                    Log.e(TAG, "profilePackages " + profilePackages.toString());
                    addTab(serializer, 1);
                    serializer.startTag(null, "profilePackages");
                    serializer.text(profilePackages.toString());
                    serializer.endTag(null, "profilePackages");
                    changeLine(serializer, enter);
                }
                /*<property name="device_sn">20181212114209</property>
                <property name="SCANNER_TYPE">8</property>
                <property name="settings_keymap_enable">false</property>*/
                String name = "";
                String propertyId = "0";
                for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
                    name = cursor.getString(cursor
                            .getColumnIndex(UConstants.PROPERTY_NAME));
                    if(name == null || name.equals("device_sn") || "SCANNER_TYPE".equals(name) || "settings_keymap_enable".equals(name)) {
                        continue;
                    }
                    if(profileId != USettings.Profile.DEFAULT_ID) {
                        propertyId = cursor.getString(cursor
                                .getColumnIndex(UConstants.PROPERTY_ID));
                    } else {
                        Integer id = keyMap.get(name);
                        if(id == null) {
                            Log.e(TAG, "propertyID no defined " + name);
                            continue;
                        }
                        propertyId = String.valueOf(id);
                    }
                    if(!"-1".equals(propertyId)) {
                        addTab(serializer, 1);
                        serializer.startTag(null, "property");
                        serializer.attribute(null, "id", propertyId);
                        serializer.attribute(null, "name", name);
                        serializer.text(cursor.getString(cursor
                                .getColumnIndex(UConstants.PROPERTY_VALUE)));

                        serializer.endTag(null, "property");
                        changeLine(serializer, enter);
                    }
                }
                cursor.close();
                serializer.endTag(null, "propertygroup");
                changeLine(serializer, enter);
                serializer.endDocument();
                serializer.flush();
                fileos.close();
                result = 0;
            } else {
                return 1;
            }
        } catch (Exception e) {
            Log.e(TAG, "error occurred while creating xml file");
            e.printStackTrace();
            result = -1;
        }
        return result;
    }
    private HashMap<String, Integer> propertyPopulate() {
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
    private static void changeLine(XmlSerializer serializer, String enter) {
        try {
            serializer.text(enter);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    //格式化添加TAB
    private static void addTab(XmlSerializer serializer, int w) {
        try {
            if (w == 1)
                serializer.text("    ");
            else
                serializer.text("        ");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
