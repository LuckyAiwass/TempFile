package com.ubx.database.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.device.provider.Constants;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

public class UConstants {
    public static final boolean DEBUG = false;
    public static final boolean SCANWEDGE_PROVIDER_ENABLE = true;
    public static final String AUTHORITY_USETTINGS = "com.ubx.scanwedge.provider";
    public static final String CONTENT_URI_SCANWEDGE = ContentResolver.SCHEME_CONTENT
            + "://" + AUTHORITY_USETTINGS + "/";
    //public static final String CONTENT_URI_USETTINGS = "content://com.ubx.scanwedge.provider/";
    public static final String SCANWEDGE_CONTENT_URI_USETTINGS = SCANWEDGE_PROVIDER_ENABLE ? CONTENT_URI_SCANWEDGE : Constants.CONTENT_URI_SETTINGS;
    public static final String SCANWEDGE_AUTHORITY_USETTINGS = SCANWEDGE_PROVIDER_ENABLE ? AUTHORITY_USETTINGS : Constants.AUTHORITY_SETTINGS;
    public static final Uri CONTENT_URI_PROPERTY_SETTINGS = Uri.parse(UConstants.SCANWEDGE_CONTENT_URI_USETTINGS + UConstants.TABLE_PROPERTY_SETTINGS);
    public static final String AUTHORITY_SETTINGS = "com.urovo.provider.settings";
    public static final String CONTENT_URI_SETTINGS = ContentResolver.SCHEME_CONTENT
            + "://" + AUTHORITY_SETTINGS + "/";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/";
    public static final String CONTENT_UROVO = "vnd.urovo";

    public static final int DEFAULT_ID = 1;
    public static final String DEFAULT_ID_STR = "1";
    public static final String DEFAULT = "Default";

    public static final String TABLE_DW_SETTINGS = "dw_settings";
    public static final String TABLE_APP_LIST = "app_list";
    public static final String TABLE_DISABLED_APP_LIST = "disabled_app_list";
    public static final String TABLE_PROFILES = "profiles";
    public static final String TABLE_SETTINGS = "settings";
    public static final String TABLE_PROPERTY_SETTINGS = "property_settings";
    public static final String TABLE_RFID_PROPERTY_SETTINGS = "rfid_property_settings";
    public static final String KEY_ID = "_id";
    public static final String KEY_NAME = Settings.NameValueTable.NAME;
    public static final String KEY_VALUE = Settings.NameValueTable.VALUE;
    public static final String ID = "_id";
    public static final String DW_NAME = "_dw_name";
    public static final String DW_VALUE = "_dw_value";
    public static final String DW_ENABLED = "dw_enabled";
    public static final String DW_LOGS_ENABLED = "dw_logs_enabled";

    public static final String PROFILE_NAME = "_name";
    public static final String PROFILE_DELETABLE = "_deletable";
    public static final String PROFILE_EDITABLE = "_editable";
    public static final String PROFILE_ENABLE = "_enabled";
    public static final String PROFILE_WORK_MODE = "workmode";

    public static final String PROFILE_ID = "_profile_id";
    //关联应用数据表字段
    public static final String APP_PKG_NAME = "_pkg_name";
    public static final String APP_ACTIVITY = "_activity";
    public static final String APP_ENABLED = "_enabled";
    //扫描配置参数数据库表字段
    public static final String PROPERTY_ID = "propertyid";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_VALUE = "value";
    public static final String PROPERTY_GROUP = "category";
    public static final String PROPERTY_SCANNER_TYPE = "scannertype";
    public static final String PROPERTY_VALUE_TYPE = "value_type";
    public static final String PROPERTY_VALUE_MIN = "value_min";
    public static final String PROPERTY_VALUE_MAX = "value_max";
    public static final String PROPERTY_DISPLAY_NAME = "display_name";
    public static final String PROPERTY_PARAM_NUMBER = "paramNum";
    public static final String PROPERTY_DEFAULT_VALUE = "default_value";
    public static final String PROPERTY_VALUE_DISCRETE_COUNT = "values_discrete_count";
    public static final String PROPERTY_VALUE_DISCRETE = "values_discrete";
    public static final String PROPERTY_VALUE_DISCRETE_NAMES = "values_discrete_names";
    //存储动态添加的参数
    public static final String TABLE_INTERNAL_PROPERTY_SETTINGS = "internal_property_settings";

    /*CREATE TABLE scanner_params (_id integer primary key autoincrement,
    scanner_type text not null,
    param_id text not null,
    param_category text not null,
    display_name text not null,
    default_value text not null,
    value_name text not null,
    value_type  text not null,
    value_min integer,
    value_max integer,
    values_discrete_count integer,
    values_discrete text,
    values_discrete_names text);

    675|
    INTERNAL_CAMERA|
    decoder_i2of5_security_level|
    DECODER_PARAMS|
    Interleaved 2of5 : I2of5 Security Level|
    1|
    I2of5 Security Level1|
    DISCRETE|
    0|
    0|
    4|
    0,1,2,3|
    I2of5 Security Level0,I2of5 Security Level1,I2of5 Security Level2,I2of5 Security Level3

    */
    public static void sendNotify(Context context, Uri uri, String TAG) {
        String notify = uri.getQueryParameter("notify");
        if (notify != null && !"true".equals(notify)) {
            Log.v(TAG, "notification suppressed: " + uri);
        } else {
            context.getContentResolver().notifyChange(uri, (ContentObserver) null);
        }
    }

    public abstract static class Notify {
        public Uri uri = null;

        public Notify(Uri uri) {
            this.uri = uri;
        }

        public abstract void send(long changedID);
    }
}
