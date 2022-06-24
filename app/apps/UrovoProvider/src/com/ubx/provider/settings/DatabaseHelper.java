package com.ubx.provider.settings;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.device.provider.Constants;
import android.device.provider.Settings;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.ubx.provider.settings.R;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "ScannerSettingsProvider";
	private static final String DATABASE_NAME = "settings.db";

	private static final int DATABASE_VERSION = 2;
	private Context mContext;
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Constants.TABLE_SETTINGS + " ("
				+ Constants.KEY_ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ Constants.KEY_NAME + " TEXT NOT NULL," // unique
																// identifier
				+ Constants.KEY_VALUE + " TEXT NOT NULL"
				+ ");");
		db.execSQL("CREATE TABLE " + Constants.TABLE_PROPERTIES + " ("
				+ Constants.KEY_ID + " INTEGER PRIMARY KEY," // unique identifier
				+ Constants.KEY_VALUE + " TEXT NOT NULL"
				+ ");");
		
		loadScannerSettings(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
		Log.w(TAG, "Upgrading settings database from version " + oldVersion
				+ " to " + currentVersion);
        int upgradeVersion = oldVersion;
        if (upgradeVersion == 1) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR IGNORE INTO settings(name,value)"
                    + " VALUES(?,?);");
                loadSetting(stmt, Settings.System.EAN13_SEND_CHECK, 1);
                loadSetting(stmt, Settings.System.EAN8_SEND_CHECK, 1);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 2;
        }
        if (upgradeVersion != currentVersion) {
            Log.w(TAG, "Destroying old data during upgrade.");
            db.execSQL("DROP TABLE IF EXISTS "
                + Constants.TABLE_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS "
                + Constants.TABLE_PROPERTIES);
            onCreate(db);
        }
	}
	
	private void loadSetting(SQLiteStatement stmt, String key, Object value) {
        stmt.bindString(1, key);
        stmt.bindString(2, value.toString());
        stmt.execute();
    }
	
	private void loadIntegerSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key,
                Integer.toString(mContext.getResources().getInteger(resid)));
    }
	
	private void loadBooleanSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key,
                mContext.getResources().getBoolean(resid) ? "1" : "0");
    }
	
	private void loadStringSetting(SQLiteStatement stmt, String key, int resid) {
        loadSetting(stmt, key, mContext.getResources().getString(resid));
    }
	
	private void loadScannerSettings(SQLiteDatabase db) {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR IGNORE INTO settings(name,value)"
                    + " VALUES(?,?);");
            
            /*loadIntegerSetting(stmt, Settings.System.WEDGE_KEYBOARD_ENABLE,
                    R.integer.config_wedge_keyboard_enable);
                    
            loadIntegerSetting(stmt, Settings.System.WEDGE_INTENT_ENABLE,
                    R.integer.config_wedge_intent_enable);
            
            loadIntegerSetting(stmt, Settings.System.WEDGE_INTENT_DELIVERY_MODE,
                    R.integer.config_wedge_intent_delivery_mode);
            
            loadIntegerSetting(stmt, Settings.System.GOOD_READ_VIBRATE_ENABLE,
                    R.integer.config_good_read_vidrate_enable);
                    
            loadIntegerSetting(stmt, Settings.System.GOOD_READ_ENABLE,
                    R.integer.config_good_read_beep_enable);
            
            loadIntegerSetting(stmt, Settings.System.GOOD_READ_LED_ENABLE,
                    R.integer.config_good_read_led_enable);
            
            loadIntegerSetting(stmt, Settings.System.WEDGE_KEYBOARD_SUFFIX_MODE,
                    R.integer.config_wedge_keyboard_suffix_mode);
            
            loadStringSetting(stmt, Settings.System.WEDGE_INTENT_ACTION_NAME,
                    R.string.config_wedge_intent_action_name);
            loadStringSetting(stmt, Settings.System.WEDGE_INTENT_CATEGORY_NAME,
                    R.string.config_wedge_intent_category_name);*/
            
            //loadSetting(stmt, Settings.System.SCANNER_SN, "");
            if(android.os.Build.PWV_CUSTOM_CUSTOM.equals("SF")) {
                loadSetting(stmt, Settings.System.SCANNER_ENABLE, 1);
            } else if(android.os.Build.PWV_CUSTOM_CUSTOM.equals("YTO")) {
                loadSetting(stmt, Settings.System.SCANNER_ENABLE, 1);
                loadSetting(stmt, Settings.System.WEDGE_KEYBOARD_ENABLE, 0);
            }else {
                loadIntegerSetting(stmt, Settings.System.SCANNER_ENABLE,
                        R.integer.config_scanner_providers_allowed);
            }
            
            /*loadIntegerSetting(stmt, Settings.System.SCANNER_TYPE,
                    R.integer.config_scanner_type);
            
            loadBooleanSetting(stmt, Settings.Secure.SCANNER_TYPE,
                    R.bool.def_mount_ums_autostart);*/
            
            List<Property> property = parsePropertyXML(true);
            if(property != null && property.size()>0){
                int len = property.size();
                for(int i =0; i< len; i++) {
                    Property prop = property.get(i);
                    if(prop != null) {
                        loadSetting(stmt, prop.getPropertyName(), prop.getPropertyValue());
                    }
                }
            }
            if (new File("/system/etc/scanner_custom_property.xml").exists()){
                List<Property> propertycustom = parsePropertyXML(false);
                if(propertycustom != null && propertycustom.size()>0){
                    int len1 = propertycustom.size();
                    for(int i =0; i< len1; i++) {
                        Property propcus = propertycustom.get(i);
                        if(propcus != null) {
                            loadSetting(stmt, propcus.getPropertyName(), propcus.getPropertyValue());
                        }
                    }
                }
            }
        } finally {
            if (stmt != null) stmt.close();
        }
    }
	private void parseJSONCondfigs(String JSONString, SQLiteStatement stmt) {
	    String ACTION_DECODE = "android.intent.ACTION_DECODE_DATA";
	    String BARCODE_STRING_TAG = "barcode_string";
        try {
            JSONObject config = new JSONObject(JSONString);
            JSONObject settings = config.getJSONObject("RevActions");
            if(settings != null) {
                JSONArray scanAction = settings.getJSONArray("ScanAction");
                if(scanAction !=null){
                    ACTION_DECODE = scanAction.getString(0);
                    BARCODE_STRING_TAG = scanAction.getString(1);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        loadSetting(stmt, Settings.System.WEDGE_INTENT_ACTION_NAME, ACTION_DECODE);
        loadSetting(stmt, Settings.System.WEDGE_INTENT_DATA_STRING_TAG, BARCODE_STRING_TAG);
    }
//	private byte[] getPropertyFromUFS() {
//        UfsManager manager = new UfsManager();
//        if(manager.init() == 0) {
//            int ret = UfsNative.getEtcCount();
//            int index = 0;
//            if(ret > 0) {
//                for(index = 0; index < ret;) {
//                    byte[] name = new byte[100];
//                    int len = UfsNative.getEtcName(index, name);
//                    String nameEtc = new String(name, 0, len);
//                    if(nameEtc.trim().equals("/default_property.etc")) {
//                        break;
//                    } else {
//                        index++;
//                    }
//                }
//                if(index < ret) {
//                    ret = UfsNative.getEtcLen(index);
//                    if(ret > 0) {
//                        byte[] propertyArray = new byte[ret];
//                        ret = UfsNative.getEtc(index, propertyArray);
//                        //parsePropertyXML(propertyArray);
//                        if(ret > 0) {
//                            manager.release();
//                            return propertyArray;
//                        }
//                    }
//                }
//            }
//        }
//        manager.release();
//        return null;
//    }
	private List<Property> parsePropertyXML(boolean defaultXml) {
        List<Property> listProperty = null;
        Property property = null;
        InputStream inputStream = null;
        try {
            if(defaultXml){
                if (new File("/customize/etc/default_property.xml").exists()){
                    inputStream = new FileInputStream("/customize/etc/default_property.xml");
                }
                if (inputStream == null){
                    if (new File("/system/etc/default_property.xml").exists()){
                        inputStream = new FileInputStream("/system/etc/default_property.xml");
                    }else{
                        inputStream = new FileInputStream("/system/etc/scanner_default_property.xml");
                    }
                }
            }else{
                inputStream = new FileInputStream("/system/etc/scanner_custom_property.xml");
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

                            //String name = parser.getAttributeValue(null, "name");
                            /*String id = parser.getAttributeValue(0);
                            Integer key = Integer.parseInt(id);*/
                            String name = parser.getAttributeValue(1);
                            property.setPropertyName( name);
                            //android.util.Log.i("debug","name: " +name);
                            String value = parser.nextText();
                            property.setPropertyValue(value);
                            
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally{
            try{
                if (inputStream != null) {
                    inputStream.close();
                }
            }catch (IOException e) {
                
            }
        }
        return listProperty;
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
