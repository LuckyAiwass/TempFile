package com.ubx.keyremap.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.device.KeyMapManager;
import android.util.Xml;
import android.view.KeyEvent;
import android.content.ContentResolver;
import android.provider.Settings;

import com.ubx.keyremap.R;
import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.provider.KeymapProvider;

import org.json.JSONException;
import org.json.JSONObject;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.net.Uri;
public class DataBaseUtils {

    public static final Uri CONTENT_URI = Uri.parse("content://com.android.provider.keymap/keymap");
    private static final String TAG = Utils.TAG + "#" + DataBaseUtils.class.getSimpleName();

    private DataBaseUtils() {
    }

    private static volatile DataBaseUtils mDBUtils;

    public static DataBaseUtils getInstance() {
        if (mDBUtils == null) {
            synchronized (DataBaseUtils.class) {
                if (mDBUtils == null) {
                    mDBUtils = new DataBaseUtils();
                }
            }
        }
        return mDBUtils;
    }

    private boolean mKeysEnable;

    public boolean isKeysEnable() {
        return mKeysEnable;
    }

    public void setKeysEnable(boolean enable) {
        this.mKeysEnable = enable;
    }

    public void setKeysEnable(String enable) {
        this.mKeysEnable = (enable != null && (enable.equals("1") || enable.equals("true")));
    }

    public void loadKeySettings(SQLiteDatabase db, Context context) {
        KeyMapManager keyMgr = new KeyMapManager(context);

        KeyProperty property;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(Utils.DEFAULT_KEYMAP_PATH);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, "utf-8");

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("property".equals(parser.getName())) {
                            String scanCode = parser.getAttributeValue(1);
                            String keyCode = parser.getAttributeValue(2);
                            String keyCodeMeta = parser.nextText();
                            if ("ENABLE_KEYS".equals(scanCode)) {
                                setKeysEnable(keyCodeMeta);
                            } else {
                                property = new KeyProperty();
                                property.setScanCode(scanCode);
                                property.setKeyCode(keyCode);
                                property.setKeyCodeMeta(keyCodeMeta);
                                property.insert2Database(/*db*/context);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = parser.next();
            }
            keyMgr.disableInterception(mKeysEnable);
        } catch (XmlPullParserException e) {
            ULog.e(TAG, "", e);
        } catch (IOException e) {
            ULog.e(TAG, "", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                ULog.e(TAG, "", e);
            }
        }
    }

    public int resetKeysConfig(Context context) {
        SQLiteOpenHelper helper = new DataBaseHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        try {
            db.beginTransaction();
            delete(db, KeyMapManager.TABLE_KEYMAP, "_id ", null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            ULog.e(TAG, "error occurred while do db Transaction:", e);
            return 1;
        } finally {
            db.endTransaction();
            db.close();
        }
        return 0;
    }

    public int importKeysConfig(Context context) {
        return importKeysConfig(context, null);
    }

    public int importKeysConfig(Context context, String filepath) {
        ContentResolver mResolver = context.getContentResolver();
        int result = -1;

        if (filepath == null || filepath.isEmpty()) {
            filepath = "/sdcard/keys_config.txt";
        }

        File keysConfigFile = new File(filepath);
        if (!keysConfigFile.exists()) {
            return -2;
        }
        if (keysConfigFile.isDirectory()) {
            ULog.w(TAG, "is Directory");
            keysConfigFile = new File(filepath + "/keys_config.txt");
            if (!keysConfigFile.exists()) {
                return -2;
            }
        }

        SQLiteOpenHelper helper = new DataBaseHelper(context);
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(keysConfigFile);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(inputStream, "UTF-8");

            String key = null;
            String value = null;
            String keyCodeMeta = null;
            JSONObject intentInfo = null;
            JSONObject intentExtras = null;
            KeyExtendProperty keyProperty = null;
            int keyType = KeyMapManager.KEY_TYPE_UNKNOWN;
            int keyCode = KeyEvent.KEYCODE_UNKNOWN;
            boolean isRootKey = true;

            String xmlName;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    xmlName = parser.getName();
                    switch (xmlName) {
                        case "KeyRemapEnabled":
                            mKeysEnable = "true".equals(parser.nextText()) ? true : false;
                            break;
                        case "KeyWakeUp":
                            break;
                        case "KeySource":
                            String keySource = parser.nextText();
                            String keyName = "KEYCODE_" + keySource;
                            String b = Boolean.toString(true);
                            Settings.System.putString(mResolver, keyName, b);
                            break;
                        case "Key": {
                            if (isRootKey) {
                                keyProperty = new KeyExtendProperty();
                                keyType = KeyMapManager.KEY_TYPE_UNKNOWN;
                            } else {
                                key = parser.nextText();
                            }
                            break;
                        }
                        case "KeyName": {
                            keyProperty.setKeyName(parser.nextText());
                            keyCode = KeyEvent.keyCodeFromString("KEYCODE_" + keyProperty.getKeyName());
                            keyProperty.setKeyCode(keyCode);
                            SQLiteDatabase db = helper.getWritableDatabase();
                            try {
                                db.beginTransaction();
                                db.delete(KeyMapManager.TABLE_KEYMAP, "keycode=?", new String[]{String.valueOf(keyProperty.getKeyCode())});
                                db.setTransactionSuccessful();
                            } catch (Exception e) {
                                ULog.e(TAG, "error occurred while do db Transaction:", e);
                            } finally {
                                db.endTransaction();
                                db.close();
                            }
                            break;
                        }
                        case "Wakeup":
                            keyProperty.setWakeUp(parser.nextText());
                            break;
                        case "KeyCode": {
                            keyCodeMeta = parser.nextText();
                            try {
                                int meta = Integer.parseInt(keyCodeMeta);
                                keyProperty.setKeyCodeMeta(meta);
                                if (keyCode == meta) {
                                    keyType = KeyMapManager.KEY_TYPE_UNKNOWN;
                                } else {
                                    keyType = KeyMapManager.KEY_TYPE_KEYCODE;
                                }
                            } catch (Exception e) {
                                keyProperty.setActivityAction(keyCodeMeta);
                                keyType = KeyMapManager.KEY_TYPE_STARTAC;
                            }
                            break;
                        }
                        case "StartActivityParams": {
                            intentExtras = new JSONObject();
                            keyType = KeyMapManager.KEY_TYPE_STARTAC;
                            break;
                        }
                        case "ActionParams": {
                            intentExtras = new JSONObject();
                            break;
                        }
                        case "BroadcastKeyDown":
                        case "BroadcastKeyUp": {
                            intentInfo = new JSONObject();
                            if (keyType == KeyMapManager.KEY_TYPE_UNKNOWN) {
                                keyType = KeyMapManager.KEY_TYPE_STARTBC;
                            }
                            break;
                        }
                        case "Action": {
                            intentInfo.put(KeyMapManager.KEY_INTENT_ACTION, parser.nextText());
                            break;
                        }
                        case "Param": {
                            isRootKey = false;
                            break;
                        }
                        case "Value": {
                            value = parser.nextText();
                            break;
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    xmlName = parser.getName();
                    switch (xmlName) {
                        case "Key": {
                            if (isRootKey) {
                                keyProperty.setRemapType(keyType);
                                keyProperty.insert2Database(context/*,helper.getWritableDatabase()*/);
                                key = null;
                                value = null;
                                intentInfo = null;
                                intentExtras = null;
                                keyProperty = null;
                            }
                            break;
                        }
                        case "KeyRemapEnabled":
                        case "KeyName":
                        case "Wakeup":
                        case "KeyCode":
                        case "Action":
                        case "Value":
                        case "KeyWakeUp":
                        case "KeySource":
                            break;
                        case "Param": {
                            intentExtras.put(key, value);
                            isRootKey = true;
                            break;
                        }
                        case "ActionParams":
                            intentInfo.put(KeyMapManager.KEY_INTENT_EXTRAS, intentExtras);
                            break;
                        case "StartActivityParams": {
                            keyProperty.setActivityAction(keyCodeMeta);
                            keyProperty.setActivityExtras(intentExtras);
                            break;
                        }
                        case "BroadcastKeyDown":
                            keyProperty.setBroadcastDownInfo(intentInfo);
                            break;
                        case "BroadcastKeyUp":
                            keyProperty.setBroadcastUpInfo(intentInfo);
                            break;
                    }
                }
                eventType = parser.next();
            }
            result = 0;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            result = -1;
        } catch (Exception e) {
            e.printStackTrace();
            result = -1;
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                result = -1;
            }
        }
        return result;
    }

    /**
     * Initializes keyNames, metaNames, keyAdapter, and
     * metaAdapter. Keys are taken from KeyEvent fields starting with KEYCODE,
     * and metas are taken from KeyEvent fields starting with META.
     * --Note--keyPopulate is assumed to be called only once, so this method is
     * not optimized for multiple calls.--
     */
    private HashMap<Integer, String> keyPopulate() {
        Field[] keyFields = KeyEvent.class.getDeclaredFields();
        HashMap<Integer, String> keyMap = new HashMap<>();
        String tmpName = null;
        try {
            for (int i = 0; i < keyFields.length; i++) {
                tmpName = keyFields[i].getName();
                int modifiers = keyFields[i].getModifiers();
                if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
                    if (tmpName.startsWith("KEYCODE_")) {
                        int keycode = (Integer) keyFields[i].get(null);
                        if (keycode > 0 && keycode <= KeyEvent.getMaxKeyCode()) {
                            tmpName = tmpName.replace("KEYCODE_", "");
                            keyMap.put((Integer) keyFields[i].get(null), tmpName);
                        }
                    }
                }
            }
        } catch (NullPointerException e) {
            ULog.w(TAG, "Non-static field : " + tmpName);
        } catch (IllegalArgumentException e1) {
            ULog.w(TAG, "Type mismatch : " + tmpName);
        } catch (IllegalAccessException e2) {
            ULog.w(TAG, "Non-public field : " + tmpName);
        }
        return keyMap;
    }

    public int exportKeysConfig(Context context) {
        return exportKeysConfig(context, null);
    }

    public int exportKeysConfig(Context context, String dir) {
        if (dir == null || dir.isEmpty()) {
            dir = "/sdcard/";
        } else {
            File out = new File(dir);
            out.mkdirs();
            if (!out.exists()) {
                ULog.w(TAG, "mkdirs error: is not exists");
                dir = "/sdcard/";
            }

            if (!out.isDirectory()) {
                ULog.w(TAG, "mkdirs error: is not Directory");
                dir = "/sdcard/";
            }
        }

        try {
            XmlSerializer serializer = Xml.newSerializer();
            HashMap<Integer, String> keyMap = keyPopulate();
            int result = exportKeysConfig(serializer, dir + "keys_config.txt", keyMap, context);
            result = exportDefaultKeyCodes(serializer, dir + "default_keycodes.txt", keyMap);
            return result;
        } catch (Exception e) {
            ULog.e(TAG, "error occurred while exportKeysConfig:", e);
            return -1;
        }
    }

    private List getExportKeyEntries(Context context) {
        List<KeyMapManager.KeyEntry> keyEntries = new ArrayList<>();
        KeyMapManager.KeyEntry keyEntry;

        SQLiteOpenHelper helper = new DataBaseHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        try {
            db.beginTransaction();
            Cursor cursor = db.query(KeyMapManager.TABLE_KEYMAP, null, null, null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
                    keyEntry = new KeyMapManager.KeyEntry();
                    keyEntry.scancode = cursor.getInt(cursor.getColumnIndex(KeyMapManager.KEY_SCANCODE));
                    keyEntry.keycode = cursor.getInt(cursor.getColumnIndex(KeyMapManager.KEY_KEYCODE));
                    keyEntry.keycode_meta = cursor.getInt(cursor.getColumnIndex(KeyMapManager.KEY_KEYCODE_META));
                    keyEntry.character = cursor.getString(cursor.getColumnIndex(KeyMapManager.KEY_CHARACTER));
                    keyEntry.activity = cursor.getString(cursor.getColumnIndex(KeyMapManager.KEY_ACTIVITY));
                    keyEntry.broadcast = cursor.getString(cursor.getColumnIndex(KeyMapManager.KEY_BROADCAST));
                    keyEntry.type = cursor.getInt(cursor.getColumnIndex(KeyMapManager.KEY_TYPE));
                    keyEntry.wake = cursor.getInt(cursor.getColumnIndex(KeyMapManager.KEY_WAKE));
                    keyEntries.add(keyEntry);
                }
                cursor.close();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            ULog.e(TAG, "exception in database transaction:", e);
            return null;
        } finally {
            db.endTransaction();
            db.close();
        }

        int[] physicalKeys = context.getResources().getIntArray(R.array.physical_keys);
        if (physicalKeys != null) {
            for (int i = 0; i < physicalKeys.length; i++) {
                keyEntry = new KeyMapManager.KeyEntry();
                keyEntry.scancode = 0;
                keyEntry.keycode = physicalKeys[i];
                keyEntry.keycode_meta = physicalKeys[i];
                keyEntry.character = "";
                keyEntry.activity = "";
                keyEntry.broadcast = "";
                keyEntry.type = KeyMapManager.KEY_TYPE_UNKNOWN;
                keyEntry.wake = 0;
                if (!keyEntries.contains(keyEntry)) {
                    keyEntries.add(keyEntry);
                }
            }
        }

        return keyEntries;
    }

    private int exportKeysConfig(XmlSerializer serializer, String filename, HashMap<Integer, String> keyMap, Context context)
            throws IOException, JSONException, FileNotFoundException {
        ContentResolver mResolver = context.getContentResolver();
        File newXmlFile = new File(filename);
        if (!newXmlFile.exists()) {
            newXmlFile.createNewFile();
        }

        FileOutputStream o = new FileOutputStream(newXmlFile);

        List<KeyMapManager.KeyEntry> keyEntries = getExportKeyEntries(context);

        // if (keyEntries == null || keyEntries.size() == 0) {
        //     return 1;
        // }

        serializer.setOutput(o, "UTF-8");
        serializer.startDocument("UTF-8", true);
        writeLine(serializer);
        serializer.startTag(null, "KeysConfig");
        writeLine(serializer);

        serializer.startTag(null, "KeyRemapEnabled");
        serializer.text(mKeysEnable ? "true" : "false");
        serializer.endTag(null, "KeyRemapEnabled");
        writeLine(serializer);
        
        serializer.startTag(null, "KeyWakeUp");
        writeLine(serializer);

        if(isWakeKey(mResolver,keyMap.get(KeyEvent.KEYCODE_KEYBOARD_PTT))) {
            serializer.startTag(null, "KeySource");
            serializer.text("KEYBOARD_PTT");
            serializer.endTag(null, "KeySource");
            writeLine(serializer);
        }

        if(isWakeKey(mResolver,keyMap.get(KeyEvent.KEYCODE_SCAN_1))) {
            serializer.startTag(null, "KeySource");
            serializer.text("SCAN_1");
            serializer.endTag(null, "KeySource");
            writeLine(serializer);
        }

        if(isWakeKey(mResolver,keyMap.get(KeyEvent.KEYCODE_SCAN_2))) {
            serializer.startTag(null, "KeySource");
            serializer.text("SCAN_2");
            serializer.endTag(null, "KeySource");
            writeLine(serializer);
        }

        if(isWakeKey(mResolver,keyMap.get(KeyEvent.KEYCODE_SCAN_3))) {
            serializer.startTag(null, "KeySource");
            serializer.text("SCAN_3");
            serializer.endTag(null, "KeySource");
            writeLine(serializer);
        }

        serializer.endTag(null, "KeyWakeUp");
        writeLine(serializer);

        KeyExtendProperty keyProperty = null;

        for (KeyMapManager.KeyEntry keyEntry : keyEntries) {
            keyProperty = new KeyExtendProperty(keyEntry);

            serializer.startTag(null, "Key");
            writeLine(serializer);

            // export key name
            writeTabs(serializer, 1);
            serializer.startTag(null, "KeyName");
            serializer.text(keyProperty.getKeyName());
            serializer.endTag(null, "KeyName");
            writeLine(serializer);

            // export key action
            switch (keyEntry.type) {
                // export key code
                case KeyMapManager.KEY_TYPE_UNKNOWN:
                case KeyMapManager.KEY_TYPE_KEYCODE: {
                    writeTabs(serializer, 1);
                    serializer.startTag(null, "KeyCode");
                    serializer.text(String.valueOf(keyProperty.getKeyCodeMeta()));
                    serializer.endTag(null, "KeyCode");
                    writeLine(serializer);
                    break;
                }
                // export key activity
                case KeyMapManager.KEY_TYPE_STARTAC: {
                    writeTabs(serializer, 1);
                    serializer.startTag(null, "KeyCode");
                    serializer.text(keyProperty.getActivityAction());
                    serializer.endTag(null, "KeyCode");
                    writeLine(serializer);

                    serializerExtras(serializer, keyProperty.getActivityExtras(), "StartActivityParams", 1);
                    break;
                }
            }

            // export key wakeup
            writeTabs(serializer, 1);
            serializer.startTag(null, "Wakeup");
            serializer.text(String.valueOf(keyEntry.wake));
            serializer.endTag(null, "Wakeup");
            writeLine(serializer);

            //export broadcast
            serializerBroadcast(serializer, keyProperty.getBroadcastDownInfo(), "BroadcastKeyDown");
            serializerBroadcast(serializer, keyProperty.getBroadcastUpInfo(), "BroadcastKeyUp");

            serializer.endTag(null, "Key");
            writeLine(serializer);
        }
        serializer.endTag(null, "KeysConfig");
        writeLine(serializer);
        serializer.endDocument();
        serializer.flush();
        o.close();
        return 0;
    }

    private int exportDefaultKeyCodes(XmlSerializer serializer, String filename, HashMap<Integer, String> keyMap)
            throws IOException, FileNotFoundException {
        File defaultXmlfile = new File(filename);
        if (!defaultXmlfile.exists())
            defaultXmlfile.createNewFile();

        FileOutputStream o = new FileOutputStream(defaultXmlfile);

        serializer.setOutput(o, "UTF-8");
        serializer.startDocument("UTF-8", true);
        writeLine(serializer);
        serializer.startTag(null, "DefaultKeyCodes");
        writeLine(serializer);

        List<Map.Entry<Integer, String>> list = new ArrayList<>(keyMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Integer, String>>() {
            //升序排序
            public int compare(Map.Entry<Integer, String> o1, Map.Entry<Integer, String> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        for (Map.Entry<Integer, String> mapping : list) {
            System.out.println(mapping.getKey() + ":" + mapping.getValue());
            //Log.d(TAG,"key= "+mapping.getKey()+" and value= "+mapping.getValue());
            writeTabs(serializer, 1);
            serializer.startTag(null, "Key");
            writeLine(serializer);

            writeTabs(serializer, 2);
            serializer.startTag(null, "KeyName");
            serializer.text(mapping.getValue());
            serializer.endTag(null, "KeyName");
            writeLine(serializer);

            writeTabs(serializer, 2);
            serializer.startTag(null, "KeyCode");
            serializer.text(mapping.getKey() + "");
            serializer.endTag(null, "KeyCode");
            writeLine(serializer);

            writeTabs(serializer, 1);
            serializer.endTag(null, "Key");
            writeLine(serializer);
        }

        serializer.endTag(null, "DefaultKeyCodes");
        writeLine(serializer);
        serializer.endDocument();
        serializer.flush();
        o.close();
        return 0;
    }

    public void writeLine(XmlSerializer serializer) {
        try {
            serializer.text(System.getProperty("line.separator"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void writeTabs(XmlSerializer serializer, int count) {
        try {
            for (int i = 0; i < count; ++i) {
                serializer.text("    ");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private JSONObject getBroadcastJson(String broadcastInfo, String type) {
        if (broadcastInfo == null ||
                (!KeyMapManager.KEY_DOWN_BROADCAST.equals(type) &&
                        !KeyMapManager.KEY_UP_BROADCAST.equals(type))) {
            return null;
        }
        JSONObject jsonObject = getJSONObjectFromString(broadcastInfo);
        if (jsonObject != null) {
            try {
                JSONObject bcObject = jsonObject.getJSONObject(type);
                return bcObject;
            } catch (JSONException e) {
                return null;
            }
        }
        return null;
    }

    private void serializerBroadcast(XmlSerializer serializer, JSONObject broadcastInfo, String rootTag) throws JSONException, IOException {
        if (serializer == null || broadcastInfo == null || rootTag == null) {
            return;
        }
        String action = broadcastInfo.getString(KeyMapManager.KEY_INTENT_ACTION);
        if (action != null && !action.isEmpty()) {
            writeTabs(serializer, 1);
            serializer.startTag(null, rootTag);
            writeLine(serializer);
            // export action
            writeTabs(serializer, 2);
            serializer.startTag(null, "Action");
            serializer.text(action);
            serializer.endTag(null, "Action");
            writeLine(serializer);
            JSONObject extras = null;
            try {
                extras = broadcastInfo.getJSONObject(KeyMapManager.KEY_INTENT_EXTRAS);
            } catch (JSONException e) {
                extras = null;
            }
            // export extras
            serializerExtras(serializer, extras, "ActionParams", 2);
            writeTabs(serializer, 1);
            serializer.endTag(null, rootTag);
            writeLine(serializer);
        }
    }

    private void serializerExtras(XmlSerializer serializer, JSONObject extras, String rootTag, int startTab) throws IOException, JSONException {
        if (serializer == null || extras == null || rootTag == null) {
            return;
        }
        writeTabs(serializer, startTab);
        serializer.startTag(null, rootTag);
        writeLine(serializer);
        Iterator iterator = extras.keys();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            String value = (String) extras.get(key);
            // export extra
            writeTabs(serializer, startTab + 1);
            serializer.startTag(null, "Param");
            writeLine(serializer);
            // export extra name
            writeTabs(serializer, startTab + 2);
            serializer.startTag(null, "Key");
            serializer.text(key);
            serializer.endTag(null, "Key");
            writeLine(serializer);
            // export extra value
            writeTabs(serializer, startTab + 2);
            serializer.startTag(null, "Value");
            serializer.text(value);
            serializer.endTag(null, "Value");
            writeLine(serializer);
            writeTabs(serializer, startTab + 1);
            serializer.endTag(null, "Param");
            writeLine(serializer);
        }
        writeTabs(serializer, startTab);
        serializer.endTag(null, rootTag);
        writeLine(serializer);
    }

    public JSONObject getJSONObjectFromString(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    class KeyProperty {
        protected int scanCode;
        protected int keyCode;
        protected int keyCodeMeta;

        KeyProperty() {
            this.scanCode = 0;
            this.keyCode = 0;
            this.keyCodeMeta = 0;
        }

        KeyProperty(int scanCode, int keyCode, int keyCodeMeta) {
            this.scanCode = scanCode;
            this.keyCode = keyCode;
            this.keyCodeMeta = keyCodeMeta;
        }

        public int getScanCode() {
            return scanCode;
        }

        public void setScanCode(int scanCode) {
            this.scanCode = scanCode;
        }

        public void setScanCode(String scanCode) {
            try {
                this.scanCode = Integer.parseInt(scanCode);
            } catch (Exception e) {
                ULog.e(TAG, "#KeyProperty ", e);
            }
        }

        public int getKeyCode() {
            return keyCode;
        }

        public void setKeyCode(int keyCode) {
            this.keyCode = keyCode;
        }

        public void setKeyCode(String keyCode) {
            try {
                this.keyCode = Integer.parseInt(keyCode);
            } catch (Exception e) {
                ULog.e(TAG, "#KeyProperty ", e);
            }
        }

        public int getKeyCodeMeta() {
            return keyCodeMeta;
        }

        public void setKeyCodeMeta(int keyCodeMeta) {
            this.keyCodeMeta = keyCodeMeta;
        }

        public void setKeyCodeMeta(String keyCodeMeta) {
            try {
                this.keyCodeMeta = Integer.parseInt(keyCodeMeta);
            } catch (Exception e) {
                ULog.e(TAG, "#KeyProperty ", e);
            }
        }

        protected void insert2Database(Context mContext/*,SQLiteDatabase db*/) {
            ContentValues values = new ContentValues();
            values.put(KeyMapManager.KEY_SCANCODE, scanCode);
            values.put(KeyMapManager.KEY_KEYCODE, keyCode);
            values.put(KeyMapManager.KEY_KEYCODE_META, keyCodeMeta);
            values.put(KeyMapManager.KEY_TYPE, KeyMapManager.KEY_TYPE_KEYCODE);
            values.put(KeyMapManager.KEY_CHARACTER, "");
            values.put(KeyMapManager.KEY_ACTIVITY, "");
            values.put(KeyMapManager.KEY_BROADCAST, "");
            values.put(KeyMapManager.KEY_WAKE, 0);
            ULog.v(TAG, "ContentValues: " + values);

	    mContext.getContentResolver().insert(CONTENT_URI, values);
            //long rowId = db.insert(KeyMapManager.TABLE_KEYMAP, null, values);
            //ULog.v(TAG, "RowID: " + rowId);
        }
    }

    class KeyExtendProperty extends KeyProperty {
        private String keyName;

        private int wakeUp;
        private int remapType;

        private JSONObject activityInfo;
        private String activityAction;
        private JSONObject activityExtras;

        private JSONObject broadcastUpInfo;
        private String broadcastUpAction;
        private JSONObject broadcastUpExtras;

        private JSONObject broadcastDownInfo;
        private String broadcastDownAction;
        private JSONObject broadcastDownExtras;

        KeyExtendProperty() {
            super();
            wakeUp = 0;
            keyName = "";
            remapType = KeyMapManager.KEY_TYPE_UNKNOWN;
            activityInfo = null;
            activityAction = "";
            activityExtras = null;
            broadcastUpInfo = null;
            broadcastUpAction = "";
            broadcastUpExtras = null;
            broadcastDownInfo = null;
            broadcastDownAction = "";
            broadcastDownExtras = null;
        }

        KeyExtendProperty(KeyMapManager.KeyEntry keyEntry) {
            super(keyEntry.scancode, keyEntry.keycode, keyEntry.keycode_meta);

            HashMap<Integer, String> keyMap = keyPopulate();
            keyName = KeyEvent.keyCodeToString(keyCode).replace("KEYCODE_", "");
            if (keyName == null || keyName.equals(String.valueOf(keyCode))) {
                keyName = keyMap.get(keyCode);
            }
            wakeUp = keyEntry.wake;

            remapType = keyEntry.type;
            switch (remapType) {
                case KeyMapManager.KEY_TYPE_STARTAC: {
                    activityInfo = getJSONObjectFromString(keyEntry.activity);
                    if (activityInfo != null) {
                        try {
                            activityAction = activityInfo.getString(KeyMapManager.KEY_INTENT_ACTION);
                        } catch (JSONException e) {
                            ULog.e(TAG, "#KeyExtendProperty ", e);
                        }

                        try {
                            activityExtras = activityInfo.getJSONObject(KeyMapManager.KEY_INTENT_EXTRAS);
                        } catch (JSONException e) {
                            ULog.e(TAG, "#KeyExtendProperty ", e);
                        }
                    }
                }
                break;
                default:
                    break;
            }

            broadcastUpInfo = getBroadcastJson(keyEntry.broadcast, KeyMapManager.KEY_UP_BROADCAST);
            try {
                broadcastUpAction = broadcastUpInfo.getString(KeyMapManager.KEY_INTENT_ACTION);
            } catch (Exception e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }

            try {
                broadcastUpExtras = broadcastUpInfo.getJSONObject(KeyMapManager.KEY_INTENT_EXTRAS);
            } catch (Exception e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }

            broadcastDownInfo = getBroadcastJson(keyEntry.broadcast, KeyMapManager.KEY_DOWN_BROADCAST);
            try {
                broadcastDownAction = broadcastDownInfo.getString(KeyMapManager.KEY_INTENT_ACTION);
            } catch (Exception e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }

            try {
                broadcastDownExtras = broadcastDownInfo.getJSONObject(KeyMapManager.KEY_INTENT_EXTRAS);
            } catch (Exception e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }
        }

        public String getKeyName() {
            return keyName;
        }

        public void setKeyName(String keyName) {
            this.keyName = keyName;
        }

        public int getWakeUp() {
            return wakeUp;
        }

        public void setWakeUp(int wakeUp) {
            this.wakeUp = wakeUp;
        }

        public void setWakeUp(String wakeUp) {
            try {
                this.wakeUp = Integer.parseInt(wakeUp);
            } catch (Exception e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }
        }

        public int getRemapType() {
            return remapType;
        }

        public void setRemapType(int remapType) {
            this.remapType = remapType;
        }

        public JSONObject getActivityInfo() {
            return activityInfo;
        }

        public void setActivityInfo(JSONObject activityInfo) {
            if (activityInfo == null)
                return;
            this.activityInfo = activityInfo;
            try {
                this.activityAction = activityInfo.getString(KeyMapManager.KEY_INTENT_ACTION);
                this.activityExtras = activityInfo.getJSONObject(KeyMapManager.KEY_INTENT_EXTRAS);
            } catch (JSONException e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }
        }

        public String getActivityAction() {
            return activityAction;
        }

        public void setActivityAction(String activityAction) {
            if (activityAction == null)
                return;
            this.activityAction = activityAction;
            try {
                this.activityInfo = new JSONObject();
                this.activityInfo.put(KeyMapManager.KEY_INTENT_ACTION, activityAction);
            } catch (JSONException e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }
        }

        public JSONObject getActivityExtras() {
            return activityExtras;
        }

        public void setActivityExtras(JSONObject activityExtras) {
            if (activityExtras == null)
                return;
            this.activityExtras = activityExtras;
            if (this.activityInfo != null) {
                try {
                    this.activityInfo.put(KeyMapManager.KEY_INTENT_EXTRAS, activityExtras);
                } catch (JSONException e) {
                    ULog.e(TAG, "#KeyExtendProperty ", e);
                }
            }
        }

        public JSONObject getBroadcastUpInfo() {
            return broadcastUpInfo;
        }

        public void setBroadcastUpInfo(JSONObject broadcastUpInfo) {
            if (broadcastUpInfo == null)
                return;
            this.broadcastUpInfo = broadcastUpInfo;
            try {
                this.broadcastUpAction = broadcastUpInfo.getString(KeyMapManager.KEY_INTENT_ACTION);
                this.broadcastUpExtras = broadcastUpInfo.getJSONObject(KeyMapManager.KEY_INTENT_EXTRAS);
            } catch (JSONException e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }
        }

        public String getBroadcastUpAction() {
            return broadcastUpAction;
        }

        public void setBroadcastUpAction(String broadcastUpAction) {
            if (broadcastUpAction == null)
                return;
            this.broadcastUpAction = broadcastUpAction;
            try {
                this.broadcastUpInfo = new JSONObject();
                this.broadcastUpInfo.put(KeyMapManager.KEY_INTENT_ACTION, broadcastUpAction);
            } catch (JSONException e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }
        }

        public JSONObject getBroadcastUpExtras() {
            return broadcastUpExtras;
        }

        public void setBroadcastUpExtras(JSONObject broadcastUpExtras) {
            if (broadcastUpExtras == null)
                return;
            this.broadcastUpExtras = broadcastUpExtras;
            if (this.broadcastUpInfo != null) {
                try {
                    this.broadcastUpInfo.put(KeyMapManager.KEY_INTENT_EXTRAS, broadcastUpExtras);
                } catch (JSONException e) {
                    ULog.e(TAG, "#KeyExtendProperty ", e);
                }
            }
        }

        public JSONObject getBroadcastDownInfo() {
            return broadcastDownInfo;
        }

        public void setBroadcastDownInfo(JSONObject broadcastDownInfo) {
            if (broadcastDownInfo == null)
                return;
            this.broadcastDownInfo = broadcastDownInfo;
            try {
                this.broadcastDownAction = broadcastDownInfo.getString(KeyMapManager.KEY_INTENT_ACTION);
                this.broadcastDownExtras = broadcastDownInfo.getJSONObject(KeyMapManager.KEY_INTENT_EXTRAS);
            } catch (JSONException e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }
        }

        public String getBroadcastDownAction() {
            return broadcastDownAction;
        }

        public void setBroadcastDownAction(String broadcastDownAction) {
            if (broadcastDownAction == null)
                return;
            this.broadcastDownAction = broadcastDownAction;
            try {
                this.broadcastDownInfo = new JSONObject();
                this.broadcastDownInfo.put(KeyMapManager.KEY_INTENT_ACTION, broadcastDownAction);
            } catch (JSONException e) {
                ULog.e(TAG, "#KeyExtendProperty ", e);
            }
        }

        public JSONObject getBroadcastDownExtras() {
            return broadcastDownExtras;
        }

        public void setBroadcastDownExtras(JSONObject broadcastDownExtras) {
            if (broadcastDownExtras == null)
                return;
            this.broadcastDownExtras = broadcastDownExtras;
            if (this.broadcastDownInfo != null) {
                try {
                    this.broadcastDownInfo.put(KeyMapManager.KEY_INTENT_EXTRAS, broadcastDownExtras);
                } catch (JSONException e) {
                    ULog.e(TAG, "#KeyExtendProperty ", e);
                }
            }
        }

        @Override
        protected void insert2Database(Context mContext/*,SQLiteDatabase db*/) {
            if (remapType == KeyMapManager.KEY_TYPE_UNKNOWN) {
                return;
            }

            String activity = "";
            if (activityInfo != null) {
                activity = activityInfo.toString();
                activity = activity == null ? "" : activity;
            }

            String broadcast = "";
            JSONObject broadcastInfo = null;
            if (broadcastUpInfo != null) {
                broadcastInfo = new JSONObject();
                try {
                    broadcastInfo.put(KeyMapManager.KEY_UP_BROADCAST, broadcastUpInfo);
                } catch (JSONException e) {
                    ULog.e(TAG, "#KeyExtendProperty", e);
                }
            }
            if (broadcastDownInfo != null) {
                if (broadcastInfo == null) {
                    broadcastInfo = new JSONObject();
                }
                try {
                    broadcastInfo.put(KeyMapManager.KEY_DOWN_BROADCAST, broadcastDownInfo);
                } catch (JSONException e) {
                    ULog.e(TAG, "#KeyExtendProperty", e);
                }
            }
            if (broadcastInfo != null) {
                broadcast = broadcastInfo.toString();
                broadcast = broadcast == null ? "" : broadcast;
            }

            ContentValues values = new ContentValues();
            values.put(KeyMapManager.KEY_SCANCODE, scanCode);
            values.put(KeyMapManager.KEY_KEYCODE, keyCode);
            values.put(KeyMapManager.KEY_KEYCODE_META, keyCodeMeta);
            values.put(KeyMapManager.KEY_TYPE, remapType);
            values.put(KeyMapManager.KEY_CHARACTER, "");
            values.put(KeyMapManager.KEY_ACTIVITY, activity);
            values.put(KeyMapManager.KEY_BROADCAST, broadcast);
            values.put(KeyMapManager.KEY_WAKE, wakeUp);
            ULog.v(TAG, "ContentValues: " + values);

            //ubx weiyu add on 2020-03-04 start
	    mContext.getContentResolver().insert(CONTENT_URI, values);
            /*
            try {
                db.beginTransaction();
                db.insert(KeyMapManager.TABLE_KEYMAP, null, values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                ULog.e(TAG, "error occurred while do db Transaction:", e);
            } finally {
                db.endTransaction();
                db.close();
            }*/
            //ubx weiyu add on 2020-03-04 end
        }
    }

    /**
     * @param db
     * @param table
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    public Cursor query(SQLiteDatabase db, String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, null);

        return cursor;
    }

    /**
     * @param db
     * @param table
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    public long insertCheckForUpdate(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs) {
        return insertCheckForUpdate(db, table, values, selection, selectionArgs, null);
    }

    /**
     * @param db
     * @param table
     * @param values
     * @param selection
     * @param selectionArgs
     * @param notify
     * @return
     */
    public long insertCheckForUpdate(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs, KeymapProvider.Notify notify) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);

        // Query to find existence.
        long changedID = -1;
        int changedCount = 0;
        Cursor cursor = qb.query(db, new String[]{KeyMapManager.KEY_ID}, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                changedID = cursor.getInt(0);
                changedCount = db.update(table, values, selection, selectionArgs);
                ULog.d(TAG, "insertCheckForUpdate - do update changedID = " + changedID);
            }
            cursor.close();
        }

        if (changedCount == 0) {
            changedID = db.insert(table, null, values);
            if (changedID != -1) {
                changedCount = 1;
            }
            ULog.d(TAG, "insertCheckForUpdate - do insert changedID = " + changedID);
        }

        if (changedCount > 0) {
            if (notify != null) {
                notify.send(changedID);
            }
        }

        return changedID;
    }

    /**
     * @param db
     * @param table
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    public int updateCheckForNew(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs, String rowID) {
        return updateCheckForNew(db, table, values, selection, selectionArgs, rowID, null);
    }

    /**
     * @param db
     * @param table
     * @param values
     * @param selection
     * @param selectionArgs
     * @param notify
     * @return
     */
    public int updateCheckForNew(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs, String rowID, KeymapProvider.Notify notify) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);
        qb.setTables(table);
        if (rowID != null) {
            qb.appendWhere(KeyMapManager.KEY_ID + "=" + rowID);
        }

        long changedID = -1;
        int changedCount = 0;
        Cursor cursor = qb.query(db, new String[]{KeyMapManager.KEY_ID}, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                changedCount = db.update(table, values, selection, selectionArgs);
                ULog.d(TAG, "updateCheckForNew - do update changedCount = " + changedCount);
            }
            cursor.close();
        }

        if (changedCount == 0) {
            changedID = db.insert(table, null, values);
            if (changedID != -1) {
                changedCount = 1;
            }
            ULog.d(TAG, "updateCheckForNew - do insert changedCount = " + changedCount);
        }

        if (changedCount > 0) {
            if (notify != null) {
                notify.send(changedID);
            }
        }

        return changedCount;
    }

    /**
     * @param db
     * @param table
     * @param selection
     * @param selectionArgs
     * @return
     */
    public int delete(SQLiteDatabase db, String table, String selection, String[] selectionArgs) {
        return delete(db, table, selection, selectionArgs, null);
    }

    /**
     * @param db
     * @param table
     * @param selection
     * @param selectionArgs
     * @param notify
     * @return
     */
    public int delete(SQLiteDatabase db, String table, String selection, String[] selectionArgs, KeymapProvider.Notify notify) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);

        int count = 0;
        Cursor cursor = qb.query(db, new String[]{KeyMapManager.KEY_ID}, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = db.delete(table, selection, selectionArgs);
                ULog.d(TAG, "delete - count = " + count);
            }
            cursor.close();
        }

        if (count > 0) {
            if (notify != null) {
                notify.send(count);
            }
        }

        return count;
    }

    public boolean isWakeKey(ContentResolver mResolver, String keyname) {
        String enable = "false";
        String keyName = "KEYCODE_" + keyname;
        try {
            enable = Settings.System.getString(mResolver, keyName);
            return Boolean.parseBoolean(enable);
        } catch (Exception e){
            ULog.e(TAG,"error occurred while do db Transaction:" + e);
        }
        return false;
    }
}
