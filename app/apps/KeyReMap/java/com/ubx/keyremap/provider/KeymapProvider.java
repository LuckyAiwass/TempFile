package com.ubx.keyremap.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.device.KeyMapManager;
import android.net.Uri;

import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.dao.DataBaseHelper;
import com.ubx.keyremap.dao.DataBaseUtils;

public class KeymapProvider extends ContentProvider {

    private static final String TAG = Utils.TAG + "#" + KeymapProvider.class.getSimpleName();

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // UriMatcher values
    private static final int MATCH_KEYMAP = 101;

    static {
        mUriMatcher.addURI(KeyMapManager.AUTHORITY, KeyMapManager.TABLE_KEYMAP, MATCH_KEYMAP);
    }

    private DataBaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DataBaseHelper(getContext());
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        ULog.d(TAG, "insert() with uri => " + uri + "\n values => " + values.toString());

        if (checkContentValues(uri, values)) {
            String table = getTableNameFromUri(uri);
            String selection = buildSelectionFromContentValues(uri, values);

            Notify notify = new Notify(uri) {
                @Override
                public void send(long changedID) {
                    //this.uri = Uri.withAppendedPath(uri, String.valueOf(changedID));
                    sendNotify(getContext(), uri, TAG);
                }
            };

            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            DataBaseUtils.getInstance().insertCheckForUpdate(db, table, values, selection, null, notify);

            return notify.uri;
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        ULog.d(TAG, "query() with uri => " + uri
                + "\n projection => " + projection
                + "\n selection => " + selection
                + "\n selectionArgs => " + selectionArgs
                + "\n sortOrder => " + sortOrder);

        Cursor outVal = DataBaseUtils.getInstance().query(mOpenHelper.getReadableDatabase(), getTableNameFromUri(uri), projection, selection, selectionArgs, null);

        ULog.d(TAG, "query() cursor outval => " + outVal);

        return outVal;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        /*
         * Prepare the table, rowID, and uniqueColumns by matching the uri.
         * table is one of TABLE_* constants. rowID is only set if one of *_ID
         * constants. null rowID means use the whole database. uniqueColumns are
         * the columns which specify a unique item.
         */
        int count;
        if (checkContentValues(uri, values)) {
            String table = getTableNameFromUri(uri);
            String rowID = null;

            Notify notify = new Notify(uri) {
                @Override
                public void send(long changedID) {
                    //this.uri = Uri.withAppendedPath(uri, String.valueOf(changedID));
                    sendNotify(getContext(), uri, TAG);
                }
            };

            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            count = DataBaseUtils.getInstance().updateCheckForNew(db, table, values, selection, selectionArgs, rowID, notify);

            // Values have been set with correct values to check for prior
            // existence, and update if exists.
            ULog.d(TAG, "update() updateCheckforNew with uri => " + uri
                    + "\n values => " + values
                    + "\n selection => " + selection
                    + "\n selectionArgs => " + selectionArgs
                    + "\n table => " + table
                    + "\n rowID => " + rowID);

        } else {
            throw new IllegalStateException();
        }

        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        ULog.d(TAG, "delete() with uri => " + uri
                + "\n selection => " + selection
                + "\n selectionArgs => " + selectionArgs);

        String table = getTableNameFromUri(uri);

        Notify notify = new Notify(uri) {
            @Override
            public void send(long deletedCount) {
                ULog.d(TAG, "deleted count = " + deletedCount);
                sendNotify(getContext(), uri, TAG);
            }
        };

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = DataBaseUtils.getInstance().delete(db, table, selection, selectionArgs, notify);

        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case MATCH_KEYMAP:
                return KeyMapManager.CONTENT_TYPE + KeyMapManager.AUTHORITY + "." + KeyMapManager.TABLE_KEYMAP;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * @param uri
     * @return
     */
    private String getTableNameFromUri(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case MATCH_KEYMAP:
                return KeyMapManager.TABLE_KEYMAP;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
    }

    /**
     * @param uri
     * @param values
     * @return
     */
    private String buildSelectionFromContentValues(Uri uri, ContentValues values) {
        switch (mUriMatcher.match(uri)) {
            case MATCH_KEYMAP:
                return KeyMapManager.KEY_SCANCODE + " = '" + values.getAsString(KeyMapManager.KEY_SCANCODE) + "' AND " +
                        KeyMapManager.KEY_KEYCODE + " = '" + values.getAsString(KeyMapManager.KEY_KEYCODE) + "' AND " +
                        KeyMapManager.KEY_TYPE + " = '" + values.getAsString(KeyMapManager.KEY_TYPE) + "'";
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
        switch (mUriMatcher.match(uri)) {
            case MATCH_KEYMAP:
                return values.getAsString(KeyMapManager.KEY_KEYCODE) != null;
            default:
                return false;
        }
    }

    /**
     * Notify
     */
    public abstract static class Notify {
        public Uri uri;

        Notify(Uri uri) {
            this.uri = uri;
        }

        public abstract void send(long changedID);
    }

    private void sendNotify(Context context, Uri uri, String tag) {
        context.getContentResolver().notifyChange(uri, null);
    }
}
