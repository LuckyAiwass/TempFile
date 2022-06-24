package com.ubx.database.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.device.provider.Constants;
import android.net.Uri;
import android.util.Log;

import com.ubx.database.helper.SettingsDao;
import com.ubx.database.helper.UConstants;

import java.util.Map;

import static android.device.provider.Constants.KEY_ID;
import static android.device.provider.Constants.KEY_NAME;
import static android.device.provider.Constants.KEY_VALUE;
import static android.device.provider.Constants.TABLE_PROPERTIES;
import static com.ubx.database.helper.UConstants.APP_PKG_NAME;
import static com.ubx.database.helper.UConstants.DW_NAME;
import static com.ubx.database.helper.UConstants.Notify;
import static com.ubx.database.helper.UConstants.PROFILE_ID;
import static com.ubx.database.helper.UConstants.PROFILE_NAME;
import static com.ubx.database.helper.UConstants.PROPERTY_NAME;
import static com.ubx.database.helper.UConstants.TABLE_APP_LIST;
import static com.ubx.database.helper.UConstants.TABLE_DW_SETTINGS;
import static com.ubx.database.helper.UConstants.TABLE_PROFILES;
import static com.ubx.database.helper.UConstants.TABLE_PROPERTY_SETTINGS;
import static com.ubx.database.helper.UConstants.SCANWEDGE_CONTENT_URI_USETTINGS;
import static com.ubx.database.helper.UConstants.SCANWEDGE_AUTHORITY_USETTINGS;

/**
 * Items in ScannerProvider may have only one instance. If an item already
 * exists during insert, it will be updated instead.
 *
 * @author rocky
 */
public class ScannerProvider extends ContentProvider {

    private static final String TAG = "ScannerSettingsProvider";
    private static final boolean DEBUG = false;

    private static final UriMatcher sUriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    // Refactor : no longer using rowID
    private static final int MATCH_SETTINGS = 101;
    //	private static final int SETTINGS_ID = 102;
    private static final int MATCH_PROPERTIES = 103;
//	private static final int PROPERTIES_ID = 104;

    private static final int MATCH_DW_SETTINGS = 0x1001;
    private static final int MATCH_APP_LIST = 0x1002;
    private static final int MATCH_PROFILES = 0x1004;
    private static final int MATCH_PROPERTY_SETTINGS = 0x1008;

    //content://com.urovo.provider.settings/settings
    //content://com.urovo.provider.settings/profiles
    static {
        sUriMatcher.addURI(SCANWEDGE_AUTHORITY_USETTINGS, Constants.TABLE_SETTINGS, MATCH_SETTINGS);
//		sUriMatcher.addURI(AUTHORITY, TABLE_SETTINGS + "/#", SETTINGS_ID);
        sUriMatcher.addURI(SCANWEDGE_AUTHORITY_USETTINGS, TABLE_PROPERTIES, MATCH_PROPERTIES);
//		sUriMatcher.addURI(AUTHORITY, TABLE_PROPERTIES + "/#", PROPERTIES_ID);
        sUriMatcher.addURI(SCANWEDGE_AUTHORITY_USETTINGS, TABLE_DW_SETTINGS, MATCH_DW_SETTINGS);
        sUriMatcher.addURI(SCANWEDGE_AUTHORITY_USETTINGS, TABLE_APP_LIST, MATCH_APP_LIST);
        sUriMatcher.addURI(SCANWEDGE_AUTHORITY_USETTINGS, TABLE_PROFILES, MATCH_PROFILES);
        sUriMatcher.addURI(SCANWEDGE_AUTHORITY_USETTINGS, TABLE_PROPERTY_SETTINGS, MATCH_PROPERTY_SETTINGS);
    }

    private DatabaseHelper mOpenHelper;
    private String DEVICE_SN = "device_sn";

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (DEBUG) Log.d(TAG, "insert() with uri= " + uri);

        if (checkContentValues(uri, values)) {
            String table = getTableNameFromUri(uri);
            String selection = buildSelectionFromContentValues(uri, values);
            if (UConstants.TABLE_SETTINGS.equals(table)) {
                String[] uniqueColumns = null;
                selection = KEY_NAME + " = '" + values.getAsString(KEY_NAME) + "'";
                uniqueColumns = new String[]{KEY_NAME};
                Uri nodeUri = insertCheckForUpdate(uri, values, table, selection, uniqueColumns, KEY_NAME);
                return nodeUri;
            } else if (UConstants.TABLE_DW_SETTINGS.equals(table)) {
                String[] uniqueColumns = null;
                uniqueColumns = new String[]{DW_NAME};
                Uri nodeUri = insertCheckForUpdate(uri, values, table, selection, uniqueColumns, DW_NAME);
                return nodeUri;
            } else {
                Notify notify = new Notify(uri) {
                    @Override
                    public void send(long changedID) {
                        // If the insert succeeded, the row ID exists.
                        this.uri = Uri.withAppendedPath(uri, String.valueOf(changedID));
                        UConstants.sendNotify(ScannerProvider.this.getContext(), uri, TAG);
                    }
                };
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                SettingsDao.insertCheckForUpdate(db, table, values, selection, null, notify);
                return notify.uri;
            }
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * @param uri       Same as inherited insert.
     * @param values    Same as inherited insert.
     * @param table     The name of the table to insert into.
     * @param selection Selection should be the names, keys, or values which determine
     *                  an entry would be the same item.
     * @return The Uri to return in the inherited insert.
     */
    private Uri insertCheckForUpdate(Uri uri, ContentValues values, String table,
                                     String selection, String[] uniqueColumns, String columnName) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] selectionArgs = null;
        // No grouping nor sort order
        String groupBy = null;
        String having = null;
        String sortOrder = null;

        // If there is an element in the db already, update instead of insert.
        // Form selection to query.
        qb.setTables(table);

        // Query to find existence.
        Cursor cursor = qb.query(db, uniqueColumns, selection, selectionArgs, groupBy,
                having, sortOrder);

        // There is currently an item, so update instead
        if (cursor != null && cursor.moveToFirst()) {

            // uri is currently the same as CONTENT_URI. The first element
            // should be the only element in the given query.
            // Refactor : no longer using rowID, so uri will stay the same.
//			uri = ContentUris.withAppendedId(Uri.parse(CONTENT_URI),
//					cursor.getInt(cursor.getColumnIndex(KEY_ID)));

            // Update, and get the number of rows successfully updated (should
            // return 1 for this provider)
            int numberUpdated = db.update(table, values, selection, selectionArgs);

            // If successful, updated item is at uri, including /KEY_ID
            if (numberUpdated > 0) {
                uri = Uri.withAppendedPath(uri, values.getAsString(columnName));
                UConstants.sendNotify(getContext(), uri, TAG);
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }

                return uri;
            }
            // Update not successful, but also not inserting. Return nothing to notify nothing.
            else {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
                return null;
            }
        }

        long rowId = db.insert(table, null, values);
        // Will notify if rowID > 0
        Uri nodeUri = getInsertedUri(rowId, table, uri);

        if (cursor != null) {
            cursor.close();
            cursor = null;
        }

        return nodeUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        String selection;
        String table = getTableNameFromUri(uri);
        if (DEBUG) Log.d(TAG, "bulkInsert() with uri= " + uri + " table=" + table);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();

        int changedTotal = 0;
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (checkContentValues(uri, values[i])) {
                    selection = selectionFromContentValues(values[i]);
                    int changedCount = db.update(table, values[i], selection, null);
                    if (changedCount <= 0) {
                        long id = db.insert(table, null, values[i]);
                        if (id >= 0) changedCount = 1;
                    }
                    changedTotal += changedCount;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        UConstants.sendNotify(getContext(), uri, TAG);
        return changedTotal;
    }

    /**
     * @param rowId Greater than 0 means an item has been inserted to uri.
     * @param table The name of the table inserted into.
     * @param uri   The Uri to notify, if rowId > 0.
     * @return If rowId > 0, this returns the CONTENT_URI, plus table, appended
     * with rowId. Null if rowId <= 0.
     */
    private Uri getInsertedUri(long rowId, String table, Uri uri) {
        Uri nodeUri = null;

        if (rowId > 0) {
            if (uri != null) {
                UConstants.sendNotify(getContext(), uri, TAG);
            }
            nodeUri = ContentUris.withAppendedId(
                    Uri.parse(SCANWEDGE_CONTENT_URI_USETTINGS + table), rowId);
        }

        return nodeUri;
    }

    /**
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (DEBUG) Log.d(TAG, "query() with uri= " + uri);
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        // Replace these with valid SQL statements if necessary.
		/*String groupBy = null;
		String having = null;

		String limit = null;*/
        qb.setTables(getTableNameFromUri(uri));
        return qb.query(db, projection, selection, selectionArgs,
                null, null, sortOrder, null);
        //return SettingsDao.query(mOpenHelper.getReadableDatabase(), getTableNameFromUri(uri), projection, selection, selectionArgs, null);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        /*
         * Prepare the table, rowID, and uniqueColumns by matching the uri.
         * table is one of TABLE_* constants. rowID is only set if one of *_ID
         * constants. null rowID means use the whole database. uniqueColumns are
         * the columns which specify a unique item.
         */
        if (DEBUG) Log.d(TAG, "update() with uri= " + uri + " selection " + selection);
        int count;
        if (checkContentValues(uri, values)) {
            String table = getTableNameFromUri(uri);
            if (UConstants.TABLE_SETTINGS.equals(table)) {
                // Values have been set with correct values to check for prior
                // existence, and update if exists.
                String[] uniqueColumns = new String[]{KEY_NAME};
                count = updateCheckForNew(uri, values, selection, selectionArgs, table,
                        uniqueColumns, null);

                if (count > 0)
                    uri = Uri.withAppendedPath(uri, values.getAsString(KEY_NAME));
                UConstants.sendNotify(getContext(), uri, TAG);
            } else if (TABLE_DW_SETTINGS.equals(table)) {
                // Values have been set with correct values to check for prior
                // existence, and update if exists.
                String[] uniqueColumns = new String[]{DW_NAME};
                count = updateCheckForNew(uri, values, selection, selectionArgs, table,
                        uniqueColumns, null);

                if (count > 0)
                    uri = Uri.withAppendedPath(uri, values.getAsString(DW_NAME));
                UConstants.sendNotify(getContext(), uri, TAG);
            } else {
                Notify notify = new Notify(uri) {
                    @Override
                    public void send(long changedID) {
                        this.uri = Uri.withAppendedPath(uri, String.valueOf(changedID));
                        UConstants.sendNotify(getContext(), uri, TAG);
                    }
                };
                count = SettingsDao.updateCheckForNew(mOpenHelper.getWritableDatabase(), table, values, selection, selectionArgs, notify);
            }
        } else {
            throw new IllegalStateException();
        }
        return count;
    }

    /**
     * @param values        Inserted or updated to the `selection` in `table`
     * @param selection     The items to insert or update.
     * @param selectionArgs selectionArgs same as SQLite selectionArgs
     * @param table         The name of the table to insert/update into
     * @param uniqueColumns The columns to use in selection. Try to specify only those
     *                      columns which detail a unique item in the database.
     * @param rowID         The item id, for specified item. Set to null to use the whole
     *                      database, as with the constants MATCH_*
     * @return The number of rows updated. If an item is instead inserted, this
     * returns 0, and observers should be updated in insert.
     */
    private int updateCheckForNew(Uri uri, ContentValues values, String selection,
                                  String[] selectionArgs, String table, String[] uniqueColumns,
                                  String rowID) {
        int count = 0;
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // No grouping or sorting needed.
        String groupBy = null;
        String having = null;
        String sortOrder = null;

        // The table is needed. Append rowID only if a rowID is specified.
        qb.setTables(table);
        if (rowID != null) {
            qb.appendWhere(KEY_ID + "=" + rowID);
        }

        Cursor cursor = qb.query(db, uniqueColumns, selection, selectionArgs,
                groupBy, having, sortOrder);

        // Already an item, update and receive count.
        if (cursor != null && cursor.moveToFirst()) {
            count = db.update(table, values, selection, selectionArgs);
        }
        // No existing item; insert as new item.
        else {
            // Base Uri to insert this item
            // Refactor : No longer using rowID, so uri will stay the same.
//			Uri uri = Uri.parse(CONTENT_URI + table);
            getContext().getContentResolver().insert(uri, values);
            /*
             * Case 1 : successful insertion observers are updated when
             * inserted. Return 0 to not notify again. Case 2 : unsuccessful
             * insertion Nothing is changed, so do not notify. Return 0 to not
             * notify.
             */
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return 0;
        }

        if (cursor != null) {
            cursor.close();
            cursor = null;
        }

        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String table = getTableNameFromUri(uri);
        int count = 0;
        if (UConstants.TABLE_SETTINGS.equals(table)) {
            try {
                count = db.delete(table, selection, selectionArgs);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (count > 0)
                UConstants.sendNotify(getContext(), uri, TAG);
        } else {
            Notify notify = new Notify(uri) {
                @Override
                public void send(long deletedID) {
                    this.uri = Uri.withAppendedPath(uri, String.valueOf(deletedID));
                    UConstants.sendNotify(getContext(), uri, TAG);
                }
            };
            count = SettingsDao.delete(db, table, selection, selectionArgs, notify);
        }
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case MATCH_DW_SETTINGS:
                return Constants.CONTENT_TYPE + Constants.CONTENT_UROVO + "." + TABLE_DW_SETTINGS;
            case MATCH_PROFILES:
                return Constants.CONTENT_TYPE + Constants.CONTENT_UROVO + "." + TABLE_PROFILES;
            case MATCH_APP_LIST:
                return Constants.CONTENT_TYPE + Constants.CONTENT_UROVO + "." + TABLE_APP_LIST;
            case MATCH_PROPERTY_SETTINGS:
                return Constants.CONTENT_TYPE + Constants.CONTENT_UROVO + "." + TABLE_PROPERTY_SETTINGS;
            case MATCH_SETTINGS:
                return Constants.CONTENT_TYPE + Constants.CONTENT_UROVO + "." + Constants.TABLE_SETTINGS;
            case MATCH_PROPERTIES:
                return Constants.CONTENT_TYPE + Constants.CONTENT_UROVO + "." + TABLE_PROPERTIES;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * @param values
     * @return
     */
    @Deprecated
    private String selectionFromContentValues(ContentValues values) {
        String result = null;
        for (Map.Entry<String, Object> item : values.valueSet()) {
            if (result == null) result = "";
            result += item.getKey() + " = '" + item.getValue().toString() + "' AND ";
        }
        result = result.substring(0, result.length() - 4);
        return result;
    }

    /**
     * @param uri
     * @param values
     * @return
     */
    private String buildSelectionFromContentValues(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case MATCH_DW_SETTINGS:
                return DW_NAME + " = '" + values.getAsString(DW_NAME) + "'";
            case MATCH_PROFILES:
                return PROFILE_NAME + " = '" + values.getAsString(PROFILE_NAME) + "'";
            case MATCH_APP_LIST:
                return APP_PKG_NAME + " = '" + values.getAsString(APP_PKG_NAME) + "'";
            case MATCH_PROPERTY_SETTINGS:
                //不加单引号会查询异常
                return PROFILE_ID + "='" + values.getAsString(PROFILE_ID) + "' AND "
                        + PROPERTY_NAME + "='" + values.getAsString(PROPERTY_NAME) + "'";
            case MATCH_SETTINGS:
                return KEY_NAME + " = '" + values.getAsString(KEY_NAME) + "'";
            case MATCH_PROPERTIES:
                return KEY_ID + " = '" + values.getAsString(KEY_ID) + "'";
            default:
                return null;
        }
    }

    /**
     * @param uri
     * @param values
     * @return
     */
    private boolean checkContentValues(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case MATCH_DW_SETTINGS:
                return values.getAsString(DW_NAME) != null;
            case MATCH_PROFILES:
                return values.getAsString(PROFILE_NAME) != null;
            case MATCH_APP_LIST:
                return values.getAsString(APP_PKG_NAME) != null;
            case MATCH_PROPERTY_SETTINGS:
                return (values.getAsString(PROFILE_ID) != null) && (values.getAsString(PROPERTY_NAME) != null);
            case MATCH_SETTINGS:
                return (values.getAsString(KEY_NAME) != null);
            case MATCH_PROPERTIES:
                return (values.getAsString(KEY_VALUE) != null);
            default:
                return false;
        }
    }

    /**
     * @param uri
     * @return
     */
    private String getTableNameFromUri(Uri uri) {
        if (DEBUG) Log.d(TAG, "UriMatcher= " + sUriMatcher.match(uri));
        switch (sUriMatcher.match(uri)) {
            case MATCH_DW_SETTINGS:
                return TABLE_DW_SETTINGS;
            case MATCH_PROPERTY_SETTINGS:
                return TABLE_PROPERTY_SETTINGS;
            case MATCH_APP_LIST:
                return TABLE_APP_LIST;
            case MATCH_PROFILES:
                return TABLE_PROFILES;
            case MATCH_SETTINGS:
                return Constants.TABLE_SETTINGS;
            case MATCH_PROPERTIES:
                return TABLE_PROPERTIES;
            default:
                throw new IllegalArgumentException("Unknown URI:" + uri);
        }
    }
}
