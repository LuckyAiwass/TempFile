package com.android.usettings;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;
import android.device.provider.Constants;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;

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
import java.util.Map;

import com.android.usettings.R;
import android.content.ContentResolver;

/**
 * Created by rocky on 18-10-29.
 */

public class ImportExoprtService extends IntentService {
    private static final String TAG = "ImportExoprtService";
    public ImportExoprtService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        int actionKey = intent.getIntExtra("config_action", 0);
        Log.d(TAG, intent.getAction() + "actionKey " + actionKey );
        int ret = -1;
        if(actionKey == 1) {
            String fileName = intent.getStringExtra("configFilepath");
            ret = importScannerConfig(fileName);
        } else if(actionKey == 2) {
            ret = exportScannerConfig();
        }
        /*Log.d(TAG, intent.getAction().toString() + "ret " + ret );*/
        if(ret == 0 && actionKey == 1) {
		    Intent intentx = new Intent("action.IMPORT_SCANNER_CONFIG_SYNC");
		    intentx.addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
            sendBroadcast(intentx);
        }
        try{
            Thread.sleep(1500);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void toast(int msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ImportExoprtService.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    public int importScannerConfig(String propertyFile) {
        if (TextUtils.isEmpty(propertyFile)) {
            propertyFile = "sdcard/scanner_property.xml";
        }
        List<Property> listProperty = null;
        Property property = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(propertyFile);
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
                            if (parser.getAttributeCount() == 1) {
                                String name = parser.getAttributeValue(0);
                                property.setPropertyName(name);
                                //android.util.Log.i("debug","name: " +name);
                                String value = parser.nextText();
                                property.setPropertyValue(value);
                                if (name != null && name.equals("SUSPENSION_BUTTON")) {
                                    android.util.Log.d(TAG,"SUSPENSION_BUTTON:value-->"+value);
                                    ContentResolver mContentResolver = ImportExoprtService.this.getContentResolver();
                                    android.device.provider.Settings.System.putInt(mContentResolver, android.device.provider.Settings.System.SUSPENSION_BUTTON, Integer.parseInt(value));
                                }
                            } else {
                                String id = parser.getAttributeValue(0);
                                String name = parser.getAttributeValue(1);
                                property.setPropertyName(name);
                                //android.util.Log.i("debug","name: " +name);
                                String value = parser.nextText();
                                property.setPropertyValue(value);
                                if (name != null && name.equals("SUSPENSION_BUTTON")) {
                                    android.util.Log.d(TAG,"SUSPENSION_BUTTON:value-->"+value);
                                    ContentResolver mContentResolver = ImportExoprtService.this.getContentResolver();
                                    android.device.provider.Settings.System.putInt(mContentResolver, android.device.provider.Settings.System.SUSPENSION_BUTTON, Integer.parseInt(value));
                                }
                            }
                            listProperty.add(property);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        property = null;
                        break;

                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            toast(R.string.import_scannersettings_fail);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {

            }
        }
        try {
            int len = listProperty.size();
            ContentValues[] currValues = new ContentValues[len];
            int currIndex = 0;
            for (int i = 0; i < len; i++) {
                Property prop = listProperty.get(i);
                if (prop != null) {
                    ContentValues values = new ContentValues();
                    values.put(android.device.provider.Constants.KEY_NAME, prop.getPropertyName());
                    values.put(android.device.provider.Constants.KEY_VALUE, prop.getPropertyValue());
                    currValues[currIndex++] = values;
                }
            }
            android.device.provider.Settings.System.putBulkStrings(getApplicationContext().getContentResolver(), currValues);
            toast(R.string.import_scannersettings_success);
        } catch (Exception e) {
            e.printStackTrace();
            toast(R.string.import_scannersettings_fail);
        }
        return 0;
    }
    private static final String[] sSettingsProjection = {Constants.KEY_NAME, Constants.KEY_VALUE};
    public int exportScannerConfig() {
        int result = -1;
        Cursor cursor = getApplicationContext().getContentResolver().query(Settings.System.CONTENT_URI,
                sSettingsProjection, null,null, null);
        try {
            if (cursor != null) {
                cursor.moveToFirst();
                String enter = System.getProperty("line.separator");//换行
                File newxmlfile = new File("/sdcard/scanner_property.xml");
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
                /*<property name="device_sn">20181212114209</property>
                <property name="SCANNER_TYPE">8</property>
                <property name="settings_keymap_enable">false</property>*/
                String name = "";
                String propertyId = "-1";
                HashMap<String, Integer> keyMap = propertyPopulate();
                for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
                    name = cursor.getString(cursor
                            .getColumnIndex(Constants.KEY_NAME));
                    if(name == null || name.equals("device_sn") || "SCANNER_TYPE".equals(name) || "settings_keymap_enable".equals(name)) {
                        continue;
                    }
                    if(name.equals("SCANNER_ENABLE")) {
                        propertyId = "0";
                    } else if(name.equals("INTENT_DATA_STRING_TAG")) {
                        propertyId = "200002";
                    } else {
                        Integer id = keyMap.get(name);
                        if (id == null) {
                            Log.e(TAG, "propertyID no defined " + name);
                            continue;
                        }
                        propertyId = String.valueOf(id);
                    }
                    if (!"-1".equals(propertyId)) {
                        addTab(serializer, 1);
                        serializer.startTag(null, "property");
                        serializer.attribute(null, "id", propertyId);
                        serializer.attribute(null, "name", name);
                        serializer.text(cursor.getString(cursor
                                .getColumnIndex(Constants.KEY_VALUE)));

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
            toast(R.string.export_scannersettings_success);
        } catch (Exception e) {
            Log.e(TAG, "error occurred while creating xml file");
            result = -1;
            toast(R.string.export_scannersettings_fail);
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

    class Property {
        private String name;
        private String value;

        public void setPropertyName(String name) {
            this.name = name;
        }

        public String getPropertyName() {
            return name;
        }

        public void setPropertyValue(String value) {
            this.value = value;
        }

        public String getPropertyValue() {
            return value;
        }
    }
}
