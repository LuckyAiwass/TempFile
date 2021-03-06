package com.ubx.keyremap.dao;

import android.annotation.Nullable;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.device.KeyMapManager;

import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String TAG = Utils.TAG + "#" + DataBaseHelper.class.getSimpleName();
    private static final String DB_NAME = Utils.DB_NAME;

    private static final int DB_VERSION = 1;

    private Context mContext;

    private final String sql_create_db = "CREATE TABLE "
            + KeyMapManager.TABLE_KEYMAP + " ("
            + KeyMapManager.KEY_ID +" INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KeyMapManager.KEY_SCANCODE + " INTEGER DEFAULT 0,"
            + KeyMapManager.KEY_KEYCODE  + " INTEGER DEFAULT 0,"
            + KeyMapManager.KEY_KEYCODE_META + " INTEGER DEFAULT 0,"
            + KeyMapManager.KEY_CHARACTER + " TEXT NOT NULL,"
            + KeyMapManager.KEY_ACTIVITY + " TEXT  NOT NULL,"
            + KeyMapManager.KEY_BROADCAST + " TEXT  NOT NULL,"
            + KeyMapManager.KEY_TYPE + " INTEGER DEFAULT 0,"
            + KeyMapManager.KEY_WAKE + " INTEGER DEFAULT 0)";

    public DataBaseHelper(@Nullable Context context) {
        this(context, DB_VERSION);
    }

    public DataBaseHelper(@Nullable Context context, int version) {
        super(context, DB_NAME, null, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ULog.v(TAG, "onCreate: " + sql_create_db);
        try {
            db.execSQL(sql_create_db);
        } catch (Exception e) {
            ULog.e(TAG, e.getMessage());
        }
        DataBaseUtils.getInstance().loadKeySettings(db, mContext);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ULog.w(TAG, "Upgrading settings database from version " + oldVersion + " to " + newVersion);
        int version = oldVersion;

        if (version != DB_VERSION) {
            ULog.w(TAG, "Destroying old data during upgrade.");
            db.execSQL("DROP TABLE IF EXISTS " + KeyMapManager.TABLE_KEYMAP);
            onCreate(db);
        }
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             