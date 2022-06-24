package com.ubx.database.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.Settings;
import android.util.Log;

import static com.ubx.database.helper.UConstants.*;
import static com.ubx.database.helper.UConstants.APP_PKG_NAME;
import static com.ubx.database.helper.UConstants.DW_NAME;
import static com.ubx.database.helper.UConstants.ID;
import static com.ubx.database.helper.UConstants.PROFILE_ID;
import static com.ubx.database.helper.UConstants.PROFILE_NAME;
import static com.ubx.database.helper.UConstants.PROPERTY_NAME;
import static com.ubx.database.helper.UConstants.TABLE_APP_LIST;
import static com.ubx.database.helper.UConstants.TABLE_DW_SETTINGS;
import static com.ubx.database.helper.UConstants.TABLE_PROFILES;
import static com.ubx.database.helper.UConstants.TABLE_PROPERTY_SETTINGS;

/**
 *
 */
public abstract class SettingsDao {

    private static final String TAG = SettingsDao.class.getSimpleName();

    /**
     * <init>
     */
    private SettingsDao() {
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
    public static Cursor query(SQLiteDatabase db, String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder, null);
        //cursor.setNotificationUri(getContext().getContentResolver(), uri);
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
    public static long insertCheckForUpdate(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs) {
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
    public static long insertCheckForUpdate(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs, Notify notify) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);
        if (DEBUG)
            Log.d(TAG, "insertCheckForUpdate - table = " + table + " selection " + selection);
        // Query to find existence.
        long changedID = -1;
        int changedCount = 0;
        Cursor cursor = null;
        try {
            cursor = qb.query(db, new String[]{ID}, selection, selectionArgs, null, null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    changedID = cursor.getInt(0);
                    changedCount = db.update(table, values, selection, selectionArgs);
                    if (DEBUG)
                        Log.d(TAG, "insertCheckForUpdate - do update changedID = " + changedID);
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e(TAG, "query " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        if (changedCount == 0) {
            changedID = db.insert(table, null, values);
            if (changedID != -1) {
                changedCount = 1;
            }
            if (DEBUG) Log.d(TAG, "insertCheckForUpdate - do insert changedID = " + changedID);
//            cursor = db.rawQuery("select last_insert_rowid() from " + table, null);
//            if (cursor != null ) {
//                if (cursor.moveToFirst()) {
//                    changedID = cursor.getInt(0);
//                    changedCount = 1;
//                }
//                cursor.close();
//                cursor = null;
//            }
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
    public static int updateCheckForNew(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs) {
        return updateCheckForNew(db, table, values, selection, selectionArgs, null);
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
    public static int updateCheckForNew(SQLiteDatabase db, String table, ContentValues values, String selection, String[] selectionArgs, Notify notify) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);

        long changedID = -1;
        int changedCount = 0;
        Cursor cursor = qb.query(db, new String[]{ID}, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                changedID = cursor.getInt(0);
                if (notify != null) {
                    notify.send(changedID);
                }
            }
            changedCount = db.update(table, values, selection, selectionArgs);
            if (DEBUG) Log.d(TAG, "updateCheckForNew - do update changedCount = " + changedCount);
            cursor.close();
            cursor = null;
        }

        if (changedCount == 0) {
            changedID = db.insert(table, null, values);
            if (changedID != -1) {
                changedCount = 1;
                if (notify != null) {
                    notify.send(changedID);
                }
            }
            if (DEBUG) Log.d(TAG, "updateCheckForNew - do insert changedCount = " + changedCount);
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
    public static int delete(SQLiteDatabase db, String table, String selection, String[] selectionArgs) {
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
    public static int delete(SQLiteDatabase db, String table, String selection, String[] selectionArgs, Notify notify) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);

        int count = 0;
        int deletedID = -1;
        Cursor cursor = qb.query(db, new String[]{ID}, selection, selectionArgs, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                deletedID = cursor.getInt(0);
                if (notify != null) {
                    notify.send(deletedID);
                }
            }
            count = db.delete(table, selection, selectionArgs);
            Log.d(TAG, "delete - count = " + count);
            cursor.close();
            cursor = null;
        }

        return count;
    }

    /**
     * @param db
     * @param table
     * @param projection
     * @param selection
     * @param def
     * @return
     */
    public static String getStringFromTable(SQLiteDatabase db, String table, String[] projection, String selection, String[] selectionArgs, String def) {
        String result = def;
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(table);
        Cursor cur = qb.query(db, projection, selection, selectionArgs, null, null, null);
        if (cur != null) {
            if (cur.moveToFirst()) {
                result = cur.getString(0);
            }
            cur.close();
            cur = null;
        }
        return result;
    }

    public static String buildSelectionFromTable(String table) {
        switch (table) {
            case TABLE_DW_SETTINGS:
                return DW_NAME + "=?";
            case TABLE_PROFILES:
                return PROFILE_NAME + "=?";
            case TABLE_APP_LIST:
                return APP_PKG_NAME + "=?";
            case TABLE_PROPERTY_SETTINGS:
                return PROFILE_ID + "=? AND " + PROPERTY_NAME + "=?";
            case TABLE_SETTINGS:
                return Settings.NameValueTable.NAME + "=?";
            default:
                return null;
        }
    }

}
