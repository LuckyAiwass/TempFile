package com.ubx.database.helper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.device.provider.Constants;
import android.device.provider.Settings;
import android.net.Uri;
import android.provider.Settings.NameValueTable;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.ubx.propertyparser.Category;
import com.ubx.propertyparser.DataParser;
import com.ubx.propertyparser.Property;

import java.util.ArrayList;
import java.util.List;

import static com.ubx.database.helper.UConstants.PROFILE_ID;
import static com.ubx.database.helper.UConstants.PROFILE_NAME;
import static com.ubx.database.helper.UConstants.PROPERTY_GROUP;
import static com.ubx.database.helper.UConstants.PROPERTY_ID;
import static com.ubx.database.helper.UConstants.PROPERTY_NAME;
import static com.ubx.database.helper.UConstants.PROPERTY_SCANNER_TYPE;
import static com.ubx.database.helper.UConstants.PROPERTY_VALUE;
import static com.ubx.database.helper.UConstants.PROPERTY_VALUE_MAX;
import static com.ubx.database.helper.UConstants.PROPERTY_VALUE_MIN;
import static com.ubx.database.helper.UConstants.PROPERTY_VALUE_TYPE;

public class USettings {
    private static final boolean DEBUG = false;
    private static final String TAG = "Wedge-USettings";
    public static final boolean SYNC_TO_NEW_SETTINGS = false;
    /*private static final String[] sSettingsProjection = {
            UConstants.DW_NAME, UConstants.DW_VALUE
    };

    private static final String sSettingsSelection = UConstants.DW_NAME + "=?";*/
    private static final String[] pSettingsProjection = {
            UConstants.KEY_NAME, UConstants.KEY_VALUE
    };
    private static final String[] allProProjection = new String[]{
            UConstants.PROPERTY_ID,
            UConstants.PROPERTY_NAME,
            UConstants.PROPERTY_VALUE,
            PROPERTY_VALUE_TYPE,
            PROPERTY_VALUE_MIN,
            PROPERTY_VALUE_MAX,
            PROPERTY_SCANNER_TYPE,
            PROPERTY_GROUP
    };
    private static final String pSettingsSelection = UConstants.KEY_NAME + "=?";

    public static class DW extends NameValueTable {
        public static final Uri CONTENT_URI_DW = Uri.parse(UConstants.SCANWEDGE_CONTENT_URI_USETTINGS + UConstants.TABLE_DW_SETTINGS);

        /**
         * @return This CONTENT_URI appended with name
         */
        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_URI_DW, name);
        }

        /**
         * @param helper
         * @param name
         * @param def
         * @return
         */
        public static String getString(ContentResolver helper, String name, String def) {
            String result = def;
            Cursor cursor = null;
            try {
                cursor = helper.query(CONTENT_URI_DW, new String[]{UConstants.DW_VALUE},
                        UConstants.DW_NAME + "=?", new String[]{name}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(0);
                }
            } catch (Exception e) {
                Log.e(TAG, "DW-getString:", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return result;
        }

        /**
         * @param helper
         * @param name
         * @param value
         * @return
         */
        public static boolean putString(ContentResolver helper, String name, String value) {
            try {
                Cursor cursor = helper.query(CONTENT_URI_DW, new String[]{UConstants.ID}, UConstants.DW_NAME + "=?", new String[]{name}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.close();
                    ContentValues values = new ContentValues();
                    values.put(UConstants.DW_NAME, name);
                    values.put(UConstants.DW_VALUE, value);
                    int result = helper.update(CONTENT_URI_DW, values, UConstants.DW_NAME + "=?", new String[]{name});
                    return result > 0 ? true : false;
                } else {
                    if (cursor != null) cursor.close();
                    ContentValues values = new ContentValues();
                    values.put(UConstants.DW_NAME, name);
                    values.put(UConstants.DW_VALUE, value);
                    helper.insert(CONTENT_URI_DW, values);
                }
                return true;
            } catch (SQLException e) {
                Log.w(TAG, "Can't set key " + name + " in " + CONTENT_URI_DW, e);
                return false;
            }
        }
    }

    public static class Profile {
        public static final int DEFAULT_ID = 1;
        public static final String DEFAULT_ID_STR = "1";
        public static final String DEFAULT = "Default";
        public static final Uri CONTENT_URI_PROFILES = Uri.parse(UConstants.SCANWEDGE_CONTENT_URI_USETTINGS + UConstants.TABLE_PROFILES);

        public static String getProfileName(ContentResolver helper, int profileId) {
            Cursor cursor = null;
            try {
                cursor = helper.query(CONTENT_URI_PROFILES, new String[]{UConstants.PROFILE_NAME},
                        UConstants.ID + "=?", new String[]{String.valueOf(profileId)}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(0);
                }
            } catch (Exception e) {
                Log.e(TAG, "Profile-isProfileEnable:", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return DEFAULT;
        }

        /**
         * @param helper
         * @param name
         * @return
         */
        public static boolean isProfileEnable(ContentResolver helper, String name) {
            boolean result = false;
            Cursor cursor = null;
            try {
                cursor = helper.query(CONTENT_URI_PROFILES, new String[]{UConstants.PROFILE_ENABLE},
                        UConstants.PROFILE_NAME + "=?", new String[]{name}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(0).equals("true");
                }
            } catch (Exception e) {
                Log.e(TAG, "Profile-isProfileEnable:", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return result;
        }

        public static boolean isProfileEnable(ContentResolver helper, int profileId) {
            boolean result = false;
            Cursor cursor = null;
            try {
                cursor = helper.query(CONTENT_URI_PROFILES, new String[]{UConstants.PROFILE_ENABLE},
                        UConstants.ID + "=?", new String[]{String.valueOf(profileId)}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(0).equals("true");
                }
            } catch (Exception e) {
                Log.e(TAG, "Profile-isProfileEnable:", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return result;
        }

        /**
         * @param helper
         * @param name
         * @param def
         * @return
         */
        public static int getId(ContentResolver helper, String name, int def) {
            int id = def;
            try {
                Cursor cur = helper.query(CONTENT_URI_PROFILES, new String[]{UConstants.ID}, PROFILE_NAME + "=?", new String[]{name}, null);
                if (cur != null) {
                    if (cur.moveToFirst()) {
                        String result = cur.getString(0);
                        id = result != null ? Integer.parseInt(result) : def;
                    }
                    cur.close();
                    cur = null;
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Profile-getId:", e);
            } finally {
            }
            return id;
        }
        public static boolean existProfile(ContentResolver cr, String name) {
            try {
                Cursor cursor = cr.query(CONTENT_URI_PROFILES, new String[]{UConstants.ID}, PROFILE_NAME + "=?", new String[]{name}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.close();
                    return true;
                }
            } catch (Exception e){
                Log.e(TAG, "Profile-getId:", e);
            }
            return false;
        }
        /**
         * @param db
         * @param name
         * @return
         */
        public static long createProfile(ContentResolver db, String name) {
            return createProfile(db, name, false, false);
        }

        /**
         * @param cr
         * @param name
         * @param editable
         * @param deletable
         * @return
         */
        public static long createProfile(ContentResolver cr, String name, boolean editable, boolean deletable) {
            long profileId = -1;
            try {
                if (DEBUG) Log.d(TAG, "createProfile = " + name);
                Cursor cursor = cr.query(CONTENT_URI_PROFILES, new String[]{UConstants.ID}, PROFILE_NAME + "=?", new String[]{name}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.close();
                } else {
                    if (cursor != null) cursor.close();
                    ContentValues values = new ContentValues();
                    values.put(PROFILE_NAME, name);
                    values.put(UConstants.PROFILE_EDITABLE, editable ? "true" : "false");
                    values.put(UConstants.PROFILE_DELETABLE, DEFAULT.equals(name) ? "false" : "true");
                    values.put(UConstants.PROFILE_ENABLE, "true");
                    Uri uri = cr.insert(CONTENT_URI_PROFILES, values);
                    if (uri != null) {
                        if (DEBUG) Log.d(TAG, "insert uri = " + uri.toString());
                        cursor = cr.query(CONTENT_URI_PROFILES, new String[]{UConstants.ID}, PROFILE_NAME + "=?", new String[]{name}, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            profileId = cursor.getInt(0);
                            cursor.close();
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            return profileId;
        }

        /**
         * @param helper
         * @param newName
         * @param oldName
         * @return
         */
        public static long renameProfile(ContentResolver helper, String newName, String oldName) {
            long result = -1;
            try {
                Cursor cursor = helper.query(CONTENT_URI_PROFILES, new String[]{UConstants.ID}, PROFILE_NAME + "=?", new String[]{oldName}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.close();
                    ContentValues values = new ContentValues();
                    values.put(PROFILE_NAME, newName);
                    result = helper.update(CONTENT_URI_PROFILES, values, PROFILE_NAME + "=?", new String[]{oldName});
                } else {
                    if (cursor != null) cursor.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Profile-renameProfile:", e);
            } finally {
            }
            return result;
        }

        /**
         * @param helper
         * @param profileId
         * @return
         */
        public static int deleteProfile(ContentResolver helper, int profileId) {
            int result = 0;
            try {
                result = helper.delete(CONTENT_URI_PROFILES, UConstants.ID + "=?", new String[]{String.valueOf(profileId)});
            } catch (Exception e) {
                Log.e(TAG, "Profile-deleteProfile:", e);
            } finally {
            }
            return result;
        }

        /**
         * @param cr
         * @param name
         * @param enable
         * @return
         */
        public static long enableProfile(ContentResolver cr, String name, boolean enable) {
            long result = -1;
            try {
                Cursor cursor = cr.query(CONTENT_URI_PROFILES, new String[]{UConstants.ID}, PROFILE_NAME + "=?", new String[]{name}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.close();
                    ContentValues values = new ContentValues();
                    values.put(UConstants.PROFILE_NAME, name);
                    values.put(UConstants.PROFILE_ENABLE, enable ? "true" : "false");
                    result = cr.update(CONTENT_URI_PROFILES, values, PROFILE_NAME + "=?", new String[]{name});
                } else {
                    if (cursor != null) cursor.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Profile-enableProfile:", e);
            } finally {
            }
            return result;
        }

        public static long enableProfile(ContentResolver cr, int profileId, boolean enable) {
            long result = -1;
            try {
                Cursor cursor = cr.query(CONTENT_URI_PROFILES, new String[]{PROFILE_NAME}, UConstants.ID + "=?", new String[]{String.valueOf(profileId)}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    String name = cursor.getString(0);
                    cursor.close();
                    ContentValues values = new ContentValues();
                    values.put(UConstants.PROFILE_NAME, name);
                    values.put(UConstants.PROFILE_ENABLE, enable ? "true" : "false");
                    result = cr.update(CONTENT_URI_PROFILES, values, UConstants.ID + "=?", new String[]{String.valueOf(profileId)});
                } else {
                    if (cursor != null) cursor.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Profile-enableProfile:", e);
            } finally {
            }
            return result;
        }

        public static int resetProfileTable(ContentResolver helper, int profileId) {
            int result = 0;
            try {
                result = helper.delete(CONTENT_URI_PROFILES, null, null);
            } catch (Exception e) {
                Log.e(TAG, "Profile-deleteProfile:", e);
            } finally {
            }
            return result;
        }
    }

    /**
     * app list table
     */
    public static class AppList {
        public static final String DEFAULT = "default";
        public static final Uri CONTENT_URI_APP_LIST = Uri.parse(UConstants.SCANWEDGE_CONTENT_URI_USETTINGS + UConstants.TABLE_APP_LIST);

        /**
         * @param cr
         * @param profileId
         * @param pkg
         * @return
         */
        public static int deleteAssociatedApp(ContentResolver cr, int profileId, String pkg) {
            int result = 0;
            try {
                result = cr.delete(CONTENT_URI_APP_LIST, UConstants.PROFILE_ID + "=? AND " + UConstants.APP_PKG_NAME + "=?", new String[]{String.valueOf(profileId), pkg});
            } catch (Exception e) {
                Log.e(TAG, "AppList-deleteAssociatedApp:", e);
            } finally {
            }
            return result;
        }

        /**
         * @param cr
         * @param profileId
         * @return
         */
        public static int deleteAssociatedApps(ContentResolver cr, int profileId) {
            int result = 0;
            try {
                result = cr.delete(CONTENT_URI_APP_LIST, UConstants.PROFILE_ID + "=?", new String[]{String.valueOf(profileId)});
            } catch (Exception e) {
                Log.e(TAG, "Profile-deleteAssociatedApps:", e);
            } finally {
            }
            return result;
        }

        /**
         * @param cr
         * @param profileId
         * @param pkg
         * @return
         */
        public static long refreshList(ContentResolver cr, int profileId, String pkg) {
            long appListId = -1;
            try {
                Cursor cursor = cr.query(CONTENT_URI_APP_LIST, new String[]{UConstants.ID}, UConstants.APP_PKG_NAME + "=?", new String[]{pkg}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.close();
                } else {
                    if (cursor != null) cursor.close();

                    ContentValues values = new ContentValues();
                    values.put(UConstants.PROFILE_ID, profileId);
                    values.put(UConstants.APP_PKG_NAME, pkg);
                    Uri uri = cr.insert(CONTENT_URI_APP_LIST, values);
                    if (uri != null) {
                        //if(DEBUG)Log.d(TAG, "insert uri = " + uri.toString() + uri.getPath());
                        /*cursor = cr.query(CONTENT_URI_APP_LIST, new String[]{UConstants.ID}, UConstants.APP_PKG_NAME + "=?", new String[]{pkg}, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            appListId = cursor.getInt(0);
                            cursor.close();
                        }*/
                        appListId = 1;
                    }
                }
            } catch (Exception e) {

            }
            return appListId;
        }

        /**
         * @param cr
         * @param profileId
         * @return
         */
        public static List<String> getAddedPackages(ContentResolver cr, int profileId) {
            List<String> result = null;
            String selection = null;
            if (profileId != -1) {
                selection = UConstants.PROFILE_ID + "=" + profileId;
            }
            try {
                Cursor cursor = cr.query(CONTENT_URI_APP_LIST, new String[]{UConstants.APP_PKG_NAME},
                        selection, null, null);
                if (cursor != null) {
                    result = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        //Log.e(TAG, "Profile-getAddedPackages:"+ cursor.getString(0));
                        result.add(cursor.getString(0));
                    }
                    cursor.close();
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Profile-getAddedPackages:", e);
            } finally {
            }
            return result;
        }

        public static int resetAssociatedAppsTable(ContentResolver cr) {
            int result = 0;
            try {
                result = cr.delete(CONTENT_URI_APP_LIST, null, null);
            } catch (Exception e) {
                Log.e(TAG, "Profile-deleteAssociatedApps:", e);
            } finally {
            }
            return result;
        }
    }

    public static class System extends NameValueTable {

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI_PROPERTY_SETTINGS = Uri.parse(UConstants.SCANWEDGE_CONTENT_URI_USETTINGS + UConstants.TABLE_PROPERTY_SETTINGS);
        public static final Uri CONTENT_URI = Uri.parse(Constants.CONTENT_URI_SETTINGS + Constants.TABLE_SETTINGS);

        /**
         * @return This CONTENT_URI appended with name
         */
        public static Uri getUriFor(String name) {
            return getUriFor(CONTENT_URI_PROPERTY_SETTINGS, name);
        }

        /**
         *
         */
        public static String getAppProfileId(ContentResolver cr, String pkg) {
            String result = String.valueOf(Profile.DEFAULT_ID);
            Cursor c = null;
            if (pkg != null) {
                try {
                    c = cr.query(AppList.CONTENT_URI_APP_LIST, new String[]{UConstants.PROFILE_ID},
                            UConstants.APP_PKG_NAME + "=?", new String[]{pkg}, null);
                    if (c != null) {
                        if (c.moveToFirst()) {
                            result = c.getString(0);
                        }
                    }
                } catch (Exception e) {
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
            return result;
        }

        /**
         *
         */
        public static String getString(ContentResolver cr, String name) {
            return getString(cr, null, name);
        }

        /**
         *
         */
        public static String getString(ContentResolver cr, String pkg, String name) {
            String outVal = null;
            String profileId = getAppProfileId(cr, pkg);
            if (DEBUG) Log.w(TAG, "getString found profileId " + profileId);
            Cursor c = null;
            try {
                //扫描服务默认读取Settings表中的配置
                if (SYNC_TO_NEW_SETTINGS && Profile.DEFAULT_ID_STR.equals(profileId)) {
                    c = cr.query(CONTENT_URI,
                            pSettingsProjection, pSettingsSelection,
                            new String[]{name}, null);
                    int column = c.getColumnIndexOrThrow(UConstants.KEY_VALUE);

                    while (c.moveToNext()) {
                        String temp = c.getString(column);
                        if (temp != null) {
                            outVal = c.getString(column);
                        }
                    }
                } else {
                    c = cr.query(CONTENT_URI_PROPERTY_SETTINGS,
                            new String[]{UConstants.PROPERTY_NAME, UConstants.PROPERTY_VALUE},
                            UConstants.PROPERTY_NAME + "=? AND " + UConstants.PROFILE_ID + "=?",
                            new String[]{name, profileId}, null);

                    int column = c.getColumnIndexOrThrow(UConstants.PROPERTY_VALUE);
                    while (c.moveToNext()) {
                        String temp = c.getString(column);
                        if (temp != null) {
                            outVal = c.getString(column);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // Column not found
                Log.w(TAG, "Value column not found", e);
            } finally {
                if (c != null) c.close();
            }
            return outVal;
        }

        public static String getString(ContentResolver cr, int profileId, String name) {
            String outVal = null;
            Cursor c = null;
            try {
                if (SYNC_TO_NEW_SETTINGS && Profile.DEFAULT_ID == profileId) {
                    c = cr.query(CONTENT_URI,
                            pSettingsProjection, pSettingsSelection,
                            new String[]{name}, null);
                    int column = c.getColumnIndexOrThrow(UConstants.KEY_VALUE);
                    while (c.moveToNext()) {
                        String temp = c.getString(column);
                        if (temp != null) {
                            outVal = c.getString(column);
                        }
                    }
                } else {
                    c = cr.query(CONTENT_URI_PROPERTY_SETTINGS,
                            new String[]{UConstants.PROPERTY_NAME, UConstants.PROPERTY_VALUE},
                            UConstants.PROPERTY_NAME + "=? AND " + UConstants.PROFILE_ID + "=?",
                            new String[]{name, String.valueOf(profileId)}, null);
                    int column = c.getColumnIndexOrThrow(UConstants.PROPERTY_VALUE);
                    while (c.moveToNext()) {
                        String temp = c.getString(column);
                        if (temp != null) {
                            outVal = c.getString(column);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // Column not found
                Log.w(TAG, "Value column not found", e);
            } finally {
                if (c != null) c.close();
            }
            return outVal;
        }

        /**
         *
         */
        public static int getInt(ContentResolver cr, String name, int def) {
            return getInt(cr, null, name, def);
        }

        /**
         *
         */
        public static int getInt(ContentResolver cr, String pkg, String name, int def) {
            String v = getString(cr, pkg, name);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public static int getInt(ContentResolver cr, int profileId, String name, int def) {
            String v = getString(cr, profileId, name);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        /**
         *
         */
        public static int getInt(ContentResolver cr, String name)
                throws SettingNotFoundException {
            return getInt(cr, null, name);
        }

        /**
         *
         */
        public static int getInt(ContentResolver cr, String pkg, String name)
                throws SettingNotFoundException {
            String v = getString(cr, pkg, name);
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new SettingNotFoundException(name);
            }
        }

        /** */
        /*public static boolean putBulkStrings(ContentResolver cr, ContentValues[] values) {
            return putBulkStrings(cr, null, values);
        }
        */

        /**
         *
         */
        public static boolean putBulkStrings(ContentResolver cr, String pkg, ContentValues[] values) {
            String profileId = getAppProfileId(cr, pkg) != null ? pkg : "default";
            if (SYNC_TO_NEW_SETTINGS && Profile.DEFAULT_ID_STR.equals(profileId)) {
                ContentValues[] currValues = new ContentValues[values.length];
                for (int i = 0; i < values.length; i++) {
                    ContentValues valuesTemp = new ContentValues();
                    valuesTemp.put(UConstants.KEY_NAME, (String) values[i].get(UConstants.KEY_NAME));
                    valuesTemp.put(UConstants.KEY_VALUE, (String) values[i].get(UConstants.KEY_VALUE));
                    currValues[i] = valuesTemp;
                }
                try {
                    cr.bulkInsert(CONTENT_URI, currValues);
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            } else {
                for (ContentValues val : values) {
                    val.put(UConstants.PROFILE_ID, profileId);
                }
                try {
                    cr.bulkInsert(CONTENT_URI_PROPERTY_SETTINGS, values);
                    return true;
                } catch (SQLException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }

        public static boolean putBulkStrings(ContentResolver cr, ContentValues[] values) {
            String profileId = Profile.DEFAULT_ID_STR;
            try {
                profileId = (String) values[0].get(UConstants.PROFILE_ID);
            } catch (Exception e) {
            }
            if (SYNC_TO_NEW_SETTINGS && Profile.DEFAULT_ID_STR.equals(profileId)) {
                ContentValues[] currValues = new ContentValues[values.length];
                for (int i = 0; i < values.length; i++) {
                    ContentValues valuesTemp = new ContentValues();
                    valuesTemp.put(UConstants.KEY_NAME, (String) values[i].get(UConstants.KEY_NAME));
                    valuesTemp.put(UConstants.KEY_VALUE, (String) values[i].get(UConstants.KEY_VALUE));
                    currValues[i] = valuesTemp;
                }
                try {
                    cr.bulkInsert(CONTENT_URI, currValues);
                    return true;
                } catch (SQLException es) {
                    es.printStackTrace();
                    return false;
                }
            } else {
                try {
                    cr.bulkInsert(CONTENT_URI_PROPERTY_SETTINGS, values);
                    return true;
                } catch (SQLException eq) {
                    eq.printStackTrace();
                    return false;
                }
            }
        }

        /**
         *
         */
        public static boolean putString(ContentResolver cr, int propertyId, String name, String value) {
            return putString(cr, "default", propertyId, name, value);
        }

        /**
         *
         */
        public static boolean putString(ContentResolver cr, String pkg, int propertyId, String name, String value) {
            String profileId = getAppProfileId(cr, pkg);
            try {
                ContentValues values = new ContentValues();
                values.put(UConstants.PROPERTY_NAME, name);
                values.put(UConstants.PROPERTY_VALUE, value);
                if (SYNC_TO_NEW_SETTINGS && Profile.DEFAULT_ID_STR.equals(profileId)) {
                    cr.insert(CONTENT_URI, values);
                } else {
                    values.put(UConstants.PROPERTY_ID, propertyId);
                    values.put(UConstants.PROFILE_ID, profileId);
                    cr.insert(CONTENT_URI_PROPERTY_SETTINGS, values);
                }
                return true;
            } catch (SQLException e) {
                Log.w(TAG, "Can't set key " + name + " in " + CONTENT_URI_PROPERTY_SETTINGS, e);
                return false;
            }
        }

        public static boolean putString(ContentResolver cr, int profileId, int propertyId, String name, String value) {
            try {
                ContentValues values = new ContentValues();
                values.put(UConstants.PROPERTY_NAME, name);
                values.put(UConstants.PROPERTY_VALUE, value);
                if (SYNC_TO_NEW_SETTINGS && profileId == Profile.DEFAULT_ID) {
                    cr.insert(CONTENT_URI, values);
                } else {
                    values.put(UConstants.PROPERTY_ID, propertyId);
                    values.put(UConstants.PROFILE_ID, profileId);
                    cr.insert(CONTENT_URI_PROPERTY_SETTINGS, values);
                }
                return true;
            } catch (SQLException e) {
                Log.w(TAG, "Can't set key " + name + " in " + CONTENT_URI_PROPERTY_SETTINGS, e);
                return false;
            }
        }

        /**
         *
         */
        public static boolean putInt(ContentResolver cr, int propertyId, String name, int value) {
            return putInt(cr, null, propertyId, name, value);
        }

        /**
         *
         */
        public static boolean putInt(ContentResolver cr, String pkg, int propertyId, String name, int value) {
            return putString(cr, pkg, propertyId, name, Integer.toString(value));
        }

        public static boolean putInt(ContentResolver cr, int profileId, int propertyId, String name, int value) {
            return putString(cr, profileId, propertyId, name, Integer.toString(value));
        }

        /*public static boolean initSettings(ContentResolver cr, int profileId) {
            ContentValues values = new ContentValues();
            values.put(PROFILE_ID, profileId);
            values.put(PROPERTY_ID, 0);
            values.put(PROPERTY_NAME, Settings.System.SCANNER_ENABLE);
            values.put(PROPERTY_VALUE, Profile.DEFAULT_ID_STR);
            cr.insert(CONTENT_URI_PROPERTY_SETTINGS, values);
            List<Property> property = parsePropertyXML(DEFAULT_PROPERTY_PATH);
            int len = property.size();
            for (int i = 0; i < len; i++) {
                Property prop = property.get(i);
                if (prop != null) {
                    values.put(PROFILE_ID, profileId);
                    values.put(PROPERTY_ID, prop.getId());
                    values.put(PROPERTY_NAME, prop.getName());
                    values.put(PROPERTY_VALUE, prop.getValue());
                    cr.insert(CONTENT_URI_PROPERTY_SETTINGS, values);
                }
            }
            return true;
        }*/
        private static ContentValues createContentValues(Property property, int profileId) {
            ContentValues values = new ContentValues();
            values.put(UConstants.PROFILE_ID, profileId);
            values.put(UConstants.PROPERTY_ID, property.getId());
            values.put(UConstants.PROPERTY_NAME, property.getName());
            values.put(UConstants.PROPERTY_VALUE, property.getValue());
            values.put(PROPERTY_VALUE_TYPE, property.getValueType());
            values.put(PROPERTY_VALUE_MIN, property.getMin());
            values.put(PROPERTY_VALUE_MAX, property.getMax());
            values.put(PROPERTY_SCANNER_TYPE, property.getSupportType());
        /*values.put(UConstants.PROPERTY_DISPLAY_NAME, property.getDisplayName());
        values.put(UConstants.PROPERTY_PARAM_NUMBER, property.getParamNum());
        values.put(UConstants.PROPERTY_DEFAULT_VALUE, property.getDefaultValue());
        values.put(UConstants.PROPERTY_VALUE_DISCRETE_COUNT, property.getDiscreteCount());
        values.put(UConstants.PROPERTY_VALUE_DISCRETE, property.getDiscreteEntryValues());
        values.put(UConstants.PROPERTY_VALUE_DISCRETE_NAMES, property.getDiscreteEntries());*/
            values.put(PROPERTY_GROUP, property.getCategory());
            return values;
        }
        public static boolean initSettings(Context context, int profileId) {
            List<Property> parsePropertyList = DataParser.parseALLPropertyFromXML(context,"configs/scanner_default_property.xml");
            if(parsePropertyList != null && parsePropertyList.size() > 0) {
                ContentValues[] values = new ContentValues[parsePropertyList.size()];
                int i = 0;
                for(Property property: parsePropertyList) {
                    values[i++] = createContentValues(property, profileId);
                }
                long start = java.lang.System.currentTimeMillis();
                try {
                    context.getContentResolver().bulkInsert(UConstants.CONTENT_URI_PROPERTY_SETTINGS, values);
                    long end = java.lang.System.currentTimeMillis();
                    if(DEBUG) Log.d(TAG, "setDefaultProperty db time=" + (end - start) + "ms");
                    return true;
                } catch (SQLException eq) {
                    eq.printStackTrace();
                    return false;
                }
            }
            return false;
        }
        public static boolean cloneSettings(ContentResolver cr, int profileId, long newProfileId) {
            Cursor cursor = null;
            try {
                cursor = cr.query(UConstants.CONTENT_URI_PROPERTY_SETTINGS, allProProjection,
                        /*UConstants.PROPERTY_NAME + "=? AND " + */UConstants.PROFILE_ID + "=?", new String[]{/*UConstants.DEFAULT, */String.valueOf(UConstants.DEFAULT_ID)}, null);
                if (cursor != null) {
                    ContentValues values = new ContentValues();
                    while (cursor.moveToNext()) {
                        values.put(PROFILE_ID, (int) newProfileId);
                        values.put(PROPERTY_ID, cursor.getInt(0));
                        values.put(PROPERTY_NAME, cursor.getString(1));
                        values.put(PROPERTY_VALUE, cursor.getString(2));
                        values.put(PROPERTY_VALUE_TYPE, cursor.getString(3));
                        values.put(PROPERTY_VALUE_MIN, cursor.getString(4));
                        values.put(PROPERTY_VALUE_MAX, cursor.getString(5));
                        values.put(PROPERTY_SCANNER_TYPE, cursor.getString(6));
                        values.put(PROPERTY_GROUP, cursor.getString(7));

                        cr.insert(CONTENT_URI_PROPERTY_SETTINGS, values);
                    }
                }

                return true;
            } catch (IllegalArgumentException e) {
                // Column not found
                Log.w(TAG, "Value column not found", e);
            } finally {
                if (cursor != null) cursor.close();
            }
            return false;
        }
    }
}
