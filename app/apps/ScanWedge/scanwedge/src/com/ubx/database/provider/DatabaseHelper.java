package com.ubx.database.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.device.provider.Constants;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.net.Uri;
import android.util.Log;

import com.ubx.propertyparser.DataParser;
import com.ubx.propertyparser.Property;
import com.ubx.scanwedge.R;
import com.ubx.database.helper.UConstants;
import com.ubx.database.helper.USettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @Author: rocky
 * @Date: 18-3-27下午2:21
 *
 * primary key与unique的区别:
 * 定义了 UNIQUE 约束的字段中不能包含重复值，可以为一个或多个字段定义 UNIQUE 约束。因此，UNIQUE 即可以在字段级也可以在表级定义， 在 UNIQUED 约束的字段上可以包含空值。ORACLE自动会为具有 PRIMARY KEY 约束的字段(主码字段)建立一个唯一索引和一个NOT NULL约束,定义PRIMARY KEY约束时可以为它的索引；
 * UNIQUED 可空，可以在一个表里的一个或多个字段定义；PRIMARY KEY 不可空不可重复，在一个表里可以定义联合主键；
 * 简单的说，primary key = unique +  not null
 * unique 就是唯一，当你需要限定你的某个表字段每个值都唯一,没有重复值时使用。比如说,如果你有一个person 表，并且表中有个身份证的column，那么你就可以指定该字段为unique。 从技术的角度来看，Primary Key和Unique Key有很多相似之处。但还是有以下区别：
 * 一、作为Primary Key的域/域组不能为null，而Unique Key可以。
 * 二、在一个表中只能有一个Primary Key，而多个Unique Key可以同时存在。
 * 更大的区别在逻辑设计上。Primary Key一般在逻辑设计中用作记录标识，这也是设置Primary Key的本来用意，而Unique Key只是为了保证域/域组的唯一性。
 * oracle的constraint中有两种约束，都是对列的唯一性限制――unique与primary key，但其中是有区别的：
 * 1、unique key要求列唯一，但不包括null字段，也就是约束的列可以为空且仅要求列中的值除null之外不重复即可；
 * 2、primary key也要求列唯一，同时又限制字段的值不能为null，相当于Primary Key=unique + not null。
 * 创建一个primary key和unique key都会相应的创建一个unique index。
 * 0primary key的语法：alter table table name add constraint key name primary key( columns);
 * unique key的语法：alter table table name add constraint key name unique( columns);
 * <p>
 * 一个表只能有一个主键，但是可以有好多个UNIQUE，而且UNIQUE可以为NULL值，如员工的电话号码一般就用UNIQUE，因为电话号码肯定是唯一的，但是有的员工可能没有电话。
 * 主键肯定是唯一的，但唯一的不一定是主键；
 * 不要总把UNIQUE索引和UNIQUE约束混为一谈
 * 1、primary key = unique + not null
 * 2、唯一约束和主键一样都是约束的范畴，而且都可以作为外键的参考，不同的是，一张表只能有一个主键
 * 3、主键和唯一约束的创建需要依靠索引，如果在创建主键或唯一约束的时候没有已经建好的索引可以使用的话，Oracle会自动建立一个唯一的索引。
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "ScannerSettingsProvider";
    private static final String DATABASE_NAME = "dwsettings.db";

    private static final int DATABASE_VERSION = 3;
    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    private String sql_create_dw_settings_tb = "CREATE TABLE "
            + UConstants.TABLE_DW_SETTINGS + " ("
            + UConstants.ID + " INTEGER PRIMARY KEY,"
            + UConstants.DW_NAME + " VERCHAR(50) NOT NULL UNIQUE,"
            + UConstants.DW_VALUE + " VERCHAR(6) DEFAULT 'true' )";

    private String sql_create_profiles_tb = "CREATE TABLE "
            + UConstants.TABLE_PROFILES + " ("
            + UConstants.ID + " INTEGER PRIMARY KEY,"
            + UConstants.PROFILE_NAME + " TEXT NOT NULL UNIQUE,"
            + UConstants.PROFILE_DELETABLE + " VERCHAR(6) DEFAULT 'false',"
            + UConstants.PROFILE_EDITABLE + " VERCHAR(6) DEFAULT 'false',"
            + UConstants.PROFILE_ENABLE + " VERCHAR(6) DEFAULT 'true',"
            + UConstants.PROFILE_WORK_MODE + " INTEGER )";

    private String sql_create_applist_tb = "CREATE TABLE "
            + UConstants.TABLE_APP_LIST + " ("
            + UConstants.ID + " INTEGER PRIMARY KEY,"
            + UConstants.PROFILE_ID + " INTEGER NOT NULL,"
            + UConstants.APP_PKG_NAME + " TEXT DEFAULT 'default',"
            + UConstants.APP_ACTIVITY + " TEXT DEFAULT '*',"
            + UConstants.APP_ENABLED + " VERCHAR(6) DEFAULT 'true' )";
    private String sql_create_disabled_applist_tb = "CREATE TABLE "
            + UConstants.TABLE_DISABLED_APP_LIST + " ("
            + UConstants.ID + " INTEGER PRIMARY KEY,"
            + UConstants.PROFILE_ID + " INTEGER NOT NULL,"
            + UConstants.APP_PKG_NAME + " TEXT DEFAULT 'default',"
            + UConstants.APP_ACTIVITY + " TEXT DEFAULT '*',"
            + UConstants.APP_ENABLED + " VERCHAR(6) DEFAULT 'true' )";

    private String sql_create_parameter_tb = "CREATE TABLE "
            + UConstants.TABLE_PROPERTY_SETTINGS + " ("
            + UConstants.ID + " INTEGER PRIMARY KEY,"
            + UConstants.PROFILE_ID + " INTEGER NOT NULL,"
            + UConstants.PROPERTY_ID + " INTEGER NOT NULL,"
            + UConstants.PROPERTY_NAME + " TEXT NOT NULL,"
            + UConstants.PROPERTY_VALUE + " TEXT NOT NULL,"
            + UConstants.PROPERTY_VALUE_TYPE + " TEXT,"
            + UConstants.PROPERTY_VALUE_MIN + " INTEGER,"
            + UConstants.PROPERTY_VALUE_MAX + " INTEGER,"
            + UConstants.PROPERTY_SCANNER_TYPE + " TEXT,"
            + UConstants.PROPERTY_GROUP + " TEXT )";
    private String sql_create_rfid_parameter_tb = "CREATE TABLE "
            + UConstants.TABLE_RFID_PROPERTY_SETTINGS + " ("
            + UConstants.ID + " INTEGER PRIMARY KEY,"
            + UConstants.PROFILE_ID + " INTEGER NOT NULL,"
            + UConstants.PROPERTY_ID + " INTEGER NOT NULL,"
            + UConstants.PROPERTY_NAME + " TEXT NOT NULL,"
            + UConstants.PROPERTY_VALUE + " TEXT NOT NULL,"
            + UConstants.PROPERTY_VALUE_TYPE + " TEXT,"
            + UConstants.PROPERTY_VALUE_MIN + " INTEGER,"
            + UConstants.PROPERTY_VALUE_MAX + " INTEGER,"
            + UConstants.PROPERTY_GROUP + " TEXT )";
    private String sql_create_internal_parameter_tb = "CREATE TABLE "
            + UConstants.TABLE_INTERNAL_PROPERTY_SETTINGS + " ("
            + UConstants.ID + " INTEGER PRIMARY KEY,"
            + UConstants.PROFILE_ID + " INTEGER NOT NULL,"
            + UConstants.PROPERTY_ID + " INTEGER NOT NULL,"
            + UConstants.PROPERTY_NAME + " TEXT NOT NULL,"
            + UConstants.PROPERTY_VALUE + " TEXT NOT NULL,"
            + UConstants.PROPERTY_VALUE_TYPE + " TEXT,"
            + UConstants.PROPERTY_VALUE_MIN + " INTEGER,"
            + UConstants.PROPERTY_VALUE_MAX + " INTEGER,"
            + UConstants.PROPERTY_SCANNER_TYPE + " TEXT,"
            + UConstants.PROPERTY_DISPLAY_NAME + " TEXT,"
            + UConstants.PROPERTY_PARAM_NUMBER + " INTEGER,"
            + UConstants.PROPERTY_DEFAULT_VALUE + " TEXT,"
            + UConstants.PROPERTY_VALUE_DISCRETE_COUNT + " INTEGER,"
            + UConstants.PROPERTY_VALUE_DISCRETE + " TEXT,"
            + UConstants.PROPERTY_VALUE_DISCRETE_NAMES + " TEXT,"
            + UConstants.PROPERTY_GROUP + " TEXT )";

    private void createScanWedgeTable(SQLiteDatabase db) {
        db.execSQL(sql_create_profiles_tb);
        db.execSQL(sql_create_applist_tb);
        db.execSQL(sql_create_disabled_applist_tb);
        db.execSQL(sql_create_parameter_tb);
        db.execSQL(sql_create_dw_settings_tb);
        db.execSQL(sql_create_rfid_parameter_tb);
        db.execSQL(sql_create_internal_parameter_tb);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Constants.TABLE_SETTINGS + " ("
                + Constants.KEY_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Constants.KEY_NAME + " TEXT NOT NULL," // unique identifier
                + Constants.KEY_VALUE + " TEXT NOT NULL"
                + ");");
        db.execSQL("CREATE TABLE " + Constants.TABLE_PROPERTIES + " ("
                + Constants.KEY_ID + " INTEGER PRIMARY KEY," // unique identifier
                + Constants.KEY_VALUE + " TEXT NOT NULL"
                + ");");
        createScanWedgeTable(db);
        //loadScannerSettings(db);
        //scanWedge默认设置
        loadDWSettings(db);
        int profileId = loadDefaultProfileSettings(db);
        List<Property> property = DataParser.parseALLPropertyFromXML(mContext,"configs/scanner_default_property.xml");
        loadPropertySettings(db, profileId, property);
        property.clear();
        moveScannerProviderSettingsToNewTable(db, UConstants.TABLE_PROPERTY_SETTINGS, profileId);
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
                // Migrate now-global settings. Note that this happens before
                // new users can be created.
                /*createScanWedgeTable(db);
                loadDWSettings(db);
                List<Property> property = parsePropertyXML("/system/etc/default_property.xml");
                int profileId = loadDefaultProfileSettings(db);
                loadPropertySettings(db, profileId, property);
                HashMap<String, Integer> keyMap = propertyIDPopulate();
                moveSettingsToNewTable(db, Constants.TABLE_SETTINGS, UConstants.TABLE_PROPERTY_SETTINGS, keyMap, false);*/
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 2;
        }
        if (upgradeVersion == 2) {
            db.beginTransaction();
            SQLiteStatement stmt = null;
            try {
                db.execSQL(sql_create_rfid_parameter_tb);
                db.execSQL(sql_create_internal_parameter_tb);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                if (stmt != null) stmt.close();
            }
            upgradeVersion = 3;
        }
        // *** Remember to update DATABASE_VERSION above!
        if (upgradeVersion != currentVersion) {
            Log.w(TAG, "Destroying old data during upgrade.");
            db.execSQL("DROP TABLE IF EXISTS "
                    + Constants.TABLE_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS "
                    + Constants.TABLE_PROPERTIES);
            db.execSQL("DROP TABLE IF EXISTS " + UConstants.TABLE_PROFILES);
            db.execSQL("DROP TABLE IF EXISTS " + UConstants.TABLE_APP_LIST);
            db.execSQL("DROP TABLE IF EXISTS " + UConstants.TABLE_PROPERTY_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS " + UConstants.TABLE_DW_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS " + UConstants.TABLE_RFID_PROPERTY_SETTINGS);
            db.execSQL("DROP TABLE IF EXISTS " + UConstants.TABLE_INTERNAL_PROPERTY_SETTINGS);
            onCreate(db);
        }
    }

    private String[] setToStringArray(Set<String> set) {
        String[] array = new String[set.size()];
        return set.toArray(array);
    }

    private HashMap propertyIDPopulate() {
        Field[] keyFields = PropertyID.class.getDeclaredFields();
        HashMap keyMap = new HashMap<String, Integer>();

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
        //keyNames = new ArrayList<String>(keyMap.keySet());
        return keyMap;
    }

    private void moveSettingsToNewTable(SQLiteDatabase db,
                                        String sourceTable, String destTable,
                                        HashMap<String, Integer> property, boolean doIgnore) {
        // Copy settings values from the source table to the dest, and remove from the source
        SQLiteStatement insertStmt = null;

        db.beginTransaction();
        try {
            insertStmt = db.compileStatement("INSERT "
                    + (doIgnore ? " OR IGNORE " : " OR REPLACE ")
                    + " INTO " + destTable + " (" + UConstants.PROPERTY_NAME + "," + UConstants.PROPERTY_VALUE + ") SELECT name,value FROM "
                    + sourceTable + " WHERE name=?");
            try {
                insertStmt.bindString(1, Settings.System.SCANNER_ENABLE);
                insertStmt.execute();
            } catch (Exception e) {
                Log.e(TAG, "execute : " + Settings.System.SCANNER_ENABLE);
            }
            ArrayList<String> propertyNames = new ArrayList<String>(property.keySet());
            int len = propertyNames.size();
            for (int i = 0; i < len; i++) {
                String prop = propertyNames.get(i);
                if (prop != null) {
                    try {
                        insertStmt.bindString(1, prop);
                        insertStmt.execute();
                    } catch (Exception e) {
                        Log.e(TAG, "execute : " + prop);
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (insertStmt != null) {
                insertStmt.close();
            }
        }
    }

    /**
     * 同步系统扫描服务默认配置的scannerprovicer数据库值
     * @param db
     * @param destTable
     * @param profileId
     */
    private void moveScannerProviderSettingsToNewTable(SQLiteDatabase db, String destTable, int profileId) {
        // Copy settings values from the source table to the dest, and remove from the source
        Cursor cursor = null;
        SQLiteStatement insertStmt = null;
        //db.beginTransaction();
        try {
            String[] pSettingsProjection = {
                    Constants.KEY_NAME, Constants.KEY_VALUE
            };
            Uri CONTENT_URI = Uri.parse(Constants.CONTENT_URI_SETTINGS + Constants.TABLE_SETTINGS);
            cursor = mContext.getContentResolver().query(CONTENT_URI,
                    pSettingsProjection, null,
                    null, null);
            insertStmt = db.compileStatement("UPDATE "
                    + destTable
                    + " SET " + UConstants.PROPERTY_VALUE + "= ? "
                    + " WHERE " + UConstants.PROFILE_ID + "= ? AND " + UConstants.PROPERTY_NAME + "=?;");
            while (cursor != null && cursor.moveToNext()) {
                insertStmt.bindString(1, cursor.getString(1));
                insertStmt.bindString(2, String.valueOf(profileId));
                insertStmt.bindString(3, cursor.getString(0));
                insertStmt.execute();
            }
            //db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            //Log.e(TAG, "execute : ");
        } finally {
            //db.endTransaction();
            if (insertStmt != null) {
                insertStmt.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void moveSettingsToNewTable(SQLiteDatabase db,
                                        String sourceTable, String destTable,
                                        String[] settingsToMove, boolean doIgnore) {
        // Copy settings values from the source table to the dest, and remove from the source
        SQLiteStatement insertStmt = null;
        SQLiteStatement deleteStmt = null;

        db.beginTransaction();
        try {
            insertStmt = db.compileStatement("INSERT "
                    + (doIgnore ? " OR IGNORE " : "")
                    + " INTO " + destTable + " (name,value) SELECT name,value FROM "
                    + sourceTable + " WHERE name=?");
            deleteStmt = db.compileStatement("DELETE FROM " + sourceTable + " WHERE name=?");

            for (String setting : settingsToMove) {
                insertStmt.bindString(1, setting);
                insertStmt.execute();

                deleteStmt.bindString(1, setting);
                deleteStmt.execute();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            if (insertStmt != null) {
                insertStmt.close();
            }
            if (deleteStmt != null) {
                deleteStmt.close();
            }
        }
    }

    /**
     * 默认profile配置文件,不允许删除，允许编辑，默认启用配置文件,默认对所有app有效
     * @param db
     * @return
     *
     */
    public int loadDefaultProfileSettings(SQLiteDatabase db) {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR IGNORE INTO " + UConstants.TABLE_PROFILES
                    + "(" + UConstants.PROFILE_NAME + ","
                    + UConstants.PROFILE_DELETABLE + ","
                    + UConstants.PROFILE_EDITABLE + ","
                    + UConstants.PROFILE_ENABLE + ","
                    + UConstants.PROFILE_WORK_MODE + ") VALUES(?,?,?,?,?)");

            stmt.bindAllArgsAsStrings(new String[]{USettings.Profile.DEFAULT, "false", "true", "true",
                    String.valueOf(0)});
            stmt.execute();
            stmt = db.compileStatement("INSERT OR IGNORE INTO " + UConstants.TABLE_APP_LIST
                    + "(" + UConstants.PROFILE_ID + ","
                    + UConstants.APP_PKG_NAME + ","
                    + UConstants.APP_ACTIVITY + ") VALUES(?,?,?)");

            stmt.bindAllArgsAsStrings(new String[]{USettings.Profile.DEFAULT_ID_STR, "default", "*"});
            stmt.execute();
        } finally {
            if (stmt != null)
                stmt.close();
        }
        Log.w(TAG, "init ProfileSettings profileId=" + USettings.Profile.DEFAULT_ID);
        return USettings.Profile.DEFAULT_ID;
    }

    /**
     * scanWedge默认设置,是否开启wedge功能
     * @param db
     */
    private void loadDWSettings(SQLiteDatabase db) {
        SQLiteStatement stmt = null;
        try {
            stmt = db.compileStatement("INSERT OR IGNORE INTO " + UConstants.TABLE_DW_SETTINGS
                    + "(" + UConstants.DW_NAME + "," + UConstants.DW_VALUE + ") VALUES(?,?)");

            stmt.bindAllArgsAsStrings(new String[]{UConstants.DW_ENABLED,
                    mContext.getString(R.string.dw_enabled)});
            stmt.execute();

            stmt.bindAllArgsAsStrings(new String[]{UConstants.DW_LOGS_ENABLED,
                    mContext.getString(R.string.dw_log_enabled)});

            stmt.execute();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }

    /**
     * 初始化默认profile 参数
     * @param db
     * @param profileId
     * @param property
     */
    private void loadPropertySettings(SQLiteDatabase db, int profileId, List<Property> property) {
        if (property != null) {
            SQLiteStatement stmt = null;
            try {
                stmt = db.compileStatement("INSERT OR REPLACE INTO " + UConstants.TABLE_PROPERTY_SETTINGS
                        + "(" + UConstants.PROFILE_ID
                        + "," + UConstants.PROPERTY_ID
                        + "," + UConstants.PROPERTY_NAME
                        + "," + UConstants.PROPERTY_VALUE
                        + "," + UConstants.PROPERTY_VALUE_TYPE
                        + "," + UConstants.PROPERTY_VALUE_MIN
                        + "," + UConstants.PROPERTY_VALUE_MAX
                        + "," + UConstants.PROPERTY_SCANNER_TYPE
                        + "," + UConstants.PROPERTY_GROUP
                        + ") VALUES(?,?,?,?,?,?,?,?,?)");
                //default enable scanner
                //loadSetting(stmt, profileId, 0, Settings.System.SCANNER_ENABLE, 1);
                int len = property.size();
                for (int i = 0; i < len; i++) {
                    Property prop = property.get(i);
                    if (prop != null) {
                        loadSetting(stmt, profileId, prop.getId(), prop.getName(), prop.getValue(),
                        prop.getValueType(), prop.getMin(), prop.getMax(),prop.getSupportType(), prop.getCategory());
                    }
                }
            } finally {
                if (stmt != null) stmt.close();
            }
        }
    }
    private void loadSetting(SQLiteStatement stmt, Object profileId, Object propertyid, String key, Object value, Object ValueType, int min, int max, String SupportType, String Category) {
        try{
            stmt.bindString(1, profileId.toString());
            stmt.bindString(2, propertyid.toString());
            stmt.bindString(3, key);
            stmt.bindString(4, value.toString());
            stmt.bindString(5, ValueType.toString());
            stmt.bindString(6, String.valueOf(min));
            stmt.bindString(7, String.valueOf(max));
            stmt.bindString(8, SupportType);
            stmt.bindString(9, Category);
            stmt.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void loadSetting(SQLiteStatement stmt, Object profileId, Object propertyid, String key, Object value) {
        stmt.bindString(1, profileId.toString());
        stmt.bindString(2, propertyid.toString());
        stmt.bindString(3, key);
        stmt.bindString(4, value.toString());
        stmt.execute();
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
            List<Property> property = DataParser.parsePropertyXML(null,"/system/etc/default_property.xml");
            int len = property.size();
            for (int i = 0; i < len; i++) {
                Property prop = property.get(i);
                if (prop != null) {
                    loadSetting(stmt, prop.getName(), prop.getValue());
                }
            }
            property.clear();
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
            if (settings != null) {
                JSONArray scanAction = settings.getJSONArray("ScanAction");
                if (scanAction != null) {
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
}
