package com.ubx.provider.settings;

import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.os.Handler;
import  static android.device.provider.Constants.*;
import  android.device.provider.Settings;

import com.qualcomm.qcnvitems.QcNvItems;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static com.ubx.provider.settings.DataFormatTransform.bytesToHexString;
import static com.ubx.provider.settings.DataFormatTransform.hexStringtoBytes;

//import com.qualcomm.qcnvitems.QcNvItemIds;
//import com.qualcomm.qcrilhook.QcRilHook;
//import com.qualcomm.qcrilhook.QcRilHookCallback;
//import com.qualcomm.qcrilhook.OemHookCallback;
/**
 * Items in SettingsProvider may have only one instance. If an item already
 * exists during insert, it will be updated instead.
 * 
 * @author jeff
 * 
 */
public class SettingsProvider extends ContentProvider {

	private static final String TAG = "ScannerSettingsProvider";
	private static final boolean DEBUG = false;

//	public static final String KEY_ID = Constants.KEY_ID;
//	public static final String AUTHORITY = Constants.AUTHORITY_SETTINGS;
//
//	public static final String TABLE_SETTINGS = Constants.TABLE_SETTINGS;
//	public static final String KEY_NAME = Constants.KEY_NAME;
//
//	public static final String TABLE_PROPERTIES = Constants.TABLE_PROPERTIES;
//	public static final String KEY_VALUE_INT = Constants.KEY_VALUE_INT;
//	public static final String KEY_VALUE_STRING = Constants.KEY_VALUE_STRING;
//
//	public static final String CONTENT_URI = Constants.CONTENT_URI_SETTINGS;

	private static final int SN_READ_TIMES = 30;
	
	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	// Refactor : no longer using rowID
	private static final int MATCH_SETTINGS = 101;
//	private static final int SETTINGS_ID = 102;
	private static final int MATCH_PROPERTIES = 103;
//	private static final int PROPERTIES_ID = 104;

	static {
		sUriMatcher.addURI(AUTHORITY_SETTINGS, TABLE_SETTINGS, MATCH_SETTINGS);
//		sUriMatcher.addURI(AUTHORITY, TABLE_SETTINGS + "/#", SETTINGS_ID);
		sUriMatcher.addURI(AUTHORITY_SETTINGS, TABLE_PROPERTIES, MATCH_PROPERTIES);
//		sUriMatcher.addURI(AUTHORITY, TABLE_PROPERTIES + "/#", PROPERTIES_ID);
	}
	private DatabaseHelper mOpenHelper;
//	private QcRilHook mQcRilOemHook;
	public QcNvItems mQcNvItems;
	// urovo add shenpidong begin 2019-06-03
	private static boolean mUpdateNV = false;
	private static boolean mHasScanTypeNV = false;
	// urovo add shenpidong end 2019-06-03

    private String DEVICE_SN = "device_sn";


	@Override
	public boolean onCreate() {
		Log.d(TAG , "onCreate SettingsProvider");
		mOpenHelper = new DatabaseHelper(getContext());
    	mQcNvItems = new QcNvItems(getContext());
//		mQcRilOemHook = new QcRilHook(getContext(), mQcrilHookCb);
        if(android.os.Build.PROJECT.equals("SQ53")){
            return true;
        }
        IntentFilter inFilter = new IntentFilter();
	// urovo add shenpidong begin 2019-06-01
        inFilter.addAction("udroid.android.ACTION_UPDATE_NV");
	// urovo add shenpidong end 2019-06-01
        inFilter.addAction("android.intent.action_UPDATE_SN");
        getContext().registerReceiver(new BroadcastReceiver() {	
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {	
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
						updateScanType(null);
                        getUrovoSN();
                    }
                }).start();
             }
        }, inFilter);
        return true;
    }

/*
    private QcRilHookCallback mQcrilHookCb = new QcRilHookCallback() {
        public void onQcRilHookReady() {
            getUrovoSN();
	    }

        public void onQcRilHookDisconnected(){

        }
    };
*/
	private void getUrovoSN() {
		// 将读取放在线程里 qibo.li 2019.09.10
		new Thread(new Runnable() {
			@Override
			public void run() {
				String group = "";
				String qcn = "";
				int times = 0;
				if (mUpdateNV) {
					Log.i(TAG, "already get SN.");
					return;
				}
				mUpdateNV = true;
                
                if(android.os.Build.PROJECT.equals("SQ81")){
				    try {
                        if(mQcNvItems.getUbrovoSN() != null) {
                             group = new String(mQcNvItems.getUbrovoSN());   
                        }
				    } catch (Exception e) {
					    Log.d(TAG, "getsn faild.");
				    }
                } else{
                    try {
                        if(mQcNvItems.getUbrovoSN() != null) {
				if(android.os.Build.PROJECT.equals("SQ53A") || android.os.Build.PROJECT.equals("SQ52M")){
				group = new String(mQcNvItems.getUbrovoSN());
				}else{
				group = convertHexToString(bytesToHexString(mQcNvItems.getUbrovoSN()));
				}
			}
                    }catch (Exception e) {
					    Log.d(TAG, "getsn faild.");
				    }               
                }
				while ((group == null || group.equals("")) && times < SN_READ_TIMES) {
					Log.i(TAG, "Reading device NV with " + times + " times!");
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						// TODO: handle exception
					}
                    if(android.os.Build.PROJECT.equals("SQ81")){
					    try {
                            if(mQcNvItems.getUbrovoSN() != null) {
                                 group = new String(mQcNvItems.getUbrovoSN());   
                            }
					    } catch (Exception e) {
						    Log.d(TAG, "getsn faild.");
					    }
                    } else{
                        try {
                            if(mQcNvItems.getUbrovoSN() != null) {
				    if(android.os.Build.PROJECT.equals("SQ53A") || android.os.Build.PROJECT.equals("SQ52M")){
				    group = new String(mQcNvItems.getUbrovoSN());
					}else{	    
				    group = convertHexToString(bytesToHexString(mQcNvItems.getUbrovoSN()));
					}
			    }
                        }catch (Exception e) {
					        Log.d(TAG, "getsn faild.");
				        }       
                    }
					times++;
				}

				if (group != null && !group.equals("")) {
				    saveUrovoSN(group);
				}

				try {
					qcn = mQcNvItems.getQcnVersion();
					if (qcn != null && !qcn.equals("")) {
						if (android.os.Build.PROJECT.equals("SQ81")) {
							byte[] qcnVersion = qcn.getBytes();
							int index = 0;
							for (int i = 0; i < qcn.length(); i++) {
								if (qcnVersion[i] >= 0x20 && qcnVersion[i] <= 0x7e) {
									index++;
								} else {
									break;
								}
							}
							qcn = new String(qcnVersion, 0, index);
						}
						android.os.SystemProperties.set("persist.sys.qcn.version", qcn);
                        Log.d(TAG, qcn);
					}
				} catch (Exception e) {
					Log.d(TAG, "getQcnVersion faild.");
					e.printStackTrace();
				}

				//mQcNvItems.dispose();
//				if (mQcRilOemHook != null) mQcRilOemHook.dispose();
			}
		}).start();
	}

    

    private void saveUrovoSN(String group){
    	StringBuffer sn = new StringBuffer();
    	StringBuffer nv = new StringBuffer();
    	StringBuffer model = new StringBuffer();
        int offset = 4;
    	if(group != null && !group.equals("")){
            if(android.os.Build.PROJECT.equals("SQ81") || android.os.Build.PROJECT.equals("SQ53A") || android.os.Build.PROJECT.equals("SQ52M")){
                offset = 1;            
            }
    		for(int i = 0; i < group.length();i+=offset){
                    //if(number.contains(String.valueOf(group.charAt(i)).toLowerCase())){
    			if(sn.length() < 14){
    				sn.append(group.charAt(i));
    			} else if(nv.length() < 2){
    				nv.append(group.charAt(i));
    			} else{
    				model.append(group.charAt(i));
    			}
                    //}
    		}
    		String newDeviceSN = sn.toString().trim();
    		String nvType = nv.toString().trim();
    		String deviceModel = model.toString().trim();

    		if(newDeviceSN.length() > 0){
    			Log.d(TAG, "newDeviceSN:" + newDeviceSN);
    			String defSerialno = android.os.SystemProperties.get("persist.sys.product.serialno");
    			Log.d(TAG, "defSerialno:" + defSerialno);
    			if (!newDeviceSN.equals(defSerialno)) {
    				Log.d(TAG, "persist.sys.product.serialno:" + newDeviceSN);
    				android.os.SystemProperties.set("persist.sys.product.serialno", newDeviceSN);
    				Settings.System.putString(getContext().getContentResolver(), DEVICE_SN,newDeviceSN);
				if(android.os.Build.PROJECT.equals("SQ53A")){
				writeOemPartition("OEM_PSN",newDeviceSN);
				Log.d(TAG,"OEM_PSN: "+readOemPartition("OEM_PSN"));
				}
    			}
    		}
    		Log.d(TAG, "nvType:" + nvType);
    		if( nvType.length() > 0) {

    			int defType = 0;
    			try{
    				defType = Settings.System.getInt(getContext().getContentResolver(), android.device.provider.Settings.System.SCANNER_TYPE, 0);
    			} catch (Exception e) {
	                            // TODO: handle exception
    				e.printStackTrace();
    			}
    			try{
    				int newtype = Integer.parseInt(nvType.toString());
    				if (newtype != defType && newtype >= 1 && newtype <= 14) {
    					Settings.System.putInt(getContext().getContentResolver(), android.device.provider.Settings.System.SCANNER_TYPE, newtype);
    				}
    			} catch (Exception e) {
	                            // TODO: handle exception
    				e.printStackTrace();
    			}
    		} else {
		    int scanType = 0;
		    String scanname = android.os.SystemProperties.get("persist.vendor.sys.scan.name", "");
		    if(ScanTypeUtil.TYPE_N6703_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_N6703;
		    } else if(ScanTypeUtil.TYPE_N6603_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_N6603;
		    } else if(ScanTypeUtil.TYPE_N603_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_N603;
		    } else if(ScanTypeUtil.TYPE_SE4710_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_SE4710;
		    } else if(ScanTypeUtil.TYPE_SE4750_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_SE4750;
		    } else if(ScanTypeUtil.TYPE_SE2100_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_SE2100;
		    } else if(ScanTypeUtil.TYPE_SE4850_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_SE4850;
                    // urovo add by shenpidong begin 2020-09-17
		    } else if(ScanTypeUtil.TYPE_EX30_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_EX30;
                    // urovo add by shenpidong end 2020-09-17
		    }
		    Log.d(TAG, "scan type:" + scanType + ",scan name:" + scanname);
		    if(scanType >= ScanTypeUtil.TYPE_MIN && scanType <= ScanTypeUtil.TYPE_MAX) {
    			try{
			    Settings.System.putInt(getContext().getContentResolver(), android.device.provider.Settings.System.SCANNER_TYPE, scanType);
    			} catch (Exception e) {
			    e.printStackTrace();
    			}
		    }
		}

    		Log.d(TAG, "model:" + deviceModel);
    		if(deviceModel.length() > 0) {
                        String oldModel = android.os.SystemProperties.get("persist.sys.product.model");
                        if (!deviceModel.equals(oldModel)) {
    			    android.os.SystemProperties.set("persist.sys.product.model", deviceModel);
                            android.provider.Settings.Global.putString(getContext().getContentResolver(), android.provider.Settings.Global.DEVICE_NAME, deviceModel);
                        }
    			Log.d(TAG, "deviceModel:" + android.os.SystemProperties.get("persist.sys.product.model") + ",old:" + oldModel);
    		}
    	}
    }
	
		private void updateScanType(String nvType) {
    		Log.d(TAG, "nvType:" + nvType);
		int defType = 0;
		try{
		    defType = Settings.System.getInt(getContext().getContentResolver(), android.device.provider.Settings.System.SCANNER_TYPE, 0);
		} catch (Exception e) {
		    // TODO: handle exception
		    e.printStackTrace();
		}
    		if(nvType != null && !"".equals(nvType.trim()) && nvType.length() > 0) {
			mHasScanTypeNV = true;
    			try{
    				int newtype = Integer.parseInt(nvType.toString());
    				if (newtype != defType && newtype >= ScanTypeUtil.TYPE_MIN && newtype <= ScanTypeUtil.TYPE_MAX) {
    					Settings.System.putInt(getContext().getContentResolver(), android.device.provider.Settings.System.SCANNER_TYPE, newtype);
    				}
    			} catch (Exception e) {
	                            // TODO: handle exception
    				e.printStackTrace();
    			}
    		} else {
		    int scanType = 0;
		    String scanname = android.os.SystemProperties.get("persist.vendor.sys.scan.name", "");
		    if(ScanTypeUtil.TYPE_N6703_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_N6703;
		    } else if(ScanTypeUtil.TYPE_N6603_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_N6603;
		    } else if(ScanTypeUtil.TYPE_N603_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_N603;
		    } else if(ScanTypeUtil.TYPE_SE4710_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_SE4710;
		    } else if(ScanTypeUtil.TYPE_SE4750_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_SE4750;
		    } else if(ScanTypeUtil.TYPE_SE2100_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_SE2100;
                    // urovo add by shenpidong begin 2020-07-27
		    } else if(ScanTypeUtil.TYPE_SE4850_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_SE4850;
                    // urovo add by shenpidong end 2020-07-27
                    // urovo add by shenpidong begin 2020-09-17
		    } else if(ScanTypeUtil.TYPE_EX30_NAME.equals(scanname)) {
			scanType = ScanTypeUtil.TYPE_EX30;
                    // urovo add by shenpidong end 2020-09-17
		    }
		    Log.d(TAG, "scan type:" + scanType + ",scan name:" + scanname + ",NV type:" + mHasScanTypeNV);
		    if(!mHasScanTypeNV && scanType != defType && scanType >= ScanTypeUtil.TYPE_MIN && scanType <= ScanTypeUtil.TYPE_MAX) {
    			try{
			    Settings.System.putInt(getContext().getContentResolver(), android.device.provider.Settings.System.SCANNER_TYPE, scanType);
    			} catch (Exception e) {
			    e.printStackTrace();
    			}
		    }
		}
	
    }

    public static String convertHexToString(String hex){
        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();
        for( int i=0; i<hex.length()-1; i+=2 ){
            String output = hex.substring(i, (i + 2));
            int decimal = Integer.parseInt(output, 16);
            sb.append((char)decimal);
            temp.append(decimal);
        }
        return sb.toString();
    }

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		if(DEBUG) android.util.Log.d(TAG, "insert() with uri= " + uri);

		String table = null;
		String selection = null;
		String[] uniqueColumns = null;
		switch (sUriMatcher.match(uri)) {
		case MATCH_SETTINGS: {
		// Refactor : no longer using rowID
//		case SETTINGS_ID: 
			table = TABLE_SETTINGS;
			// Unique selection
			selection = KEY_NAME + " = '" + values.getAsString(KEY_NAME) + "'";
			uniqueColumns = new String[] {KEY_NAME};
			break;
		}
		case MATCH_PROPERTIES: {
		// Refactor : no longer using rowID
//		case PROPERTIES_ID: 
			table = TABLE_PROPERTIES;
			// Unique selection
			selection = KEY_ID + " = '" + values.getAsString(KEY_ID) + "'";
			uniqueColumns = new String[] {KEY_ID};
			break;
		}
		default:
			throw new UnsupportedOperationException();
		}

		Uri nodeUri = insertCheckForUpdate(uri, values, table, selection, uniqueColumns);
			
		return nodeUri;
	}

	/**
	 * @param uri
	 *            Same as inherited insert.
	 * @param values
	 *            Same as inherited insert.
	 * @param table
	 *            The name of the table to insert into.
	 * @param selection
	 *            Selection should be the names, keys, or values which determine
	 *            an entry would be the same item.
	 * @return The Uri to return in the inherited insert.
	 */
	private Uri insertCheckForUpdate(Uri uri, ContentValues values, String table,
			String selection, String[] uniqueColumns) {
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
				uri = Uri.withAppendedPath(uri, values.getAsString(KEY_NAME));
	            sendNotify(getContext(), uri, TAG);
				if ( cursor != null ) {
					cursor.close();
					cursor = null;
				}

				return uri;
			}
			// Update not successful, but also not inserting. Return nothing to notify nothing.
			else {
				if ( cursor != null ) {
					cursor.close();
					cursor = null;
				}
				return null;
			}
		}

		long rowId = db.insert(table, null, values);
		// Will notify if rowID > 0
		Uri nodeUri = getInsertedUri(rowId, table, uri);

		if ( cursor != null ) {
			cursor.close();
			cursor = null;
		}

		return nodeUri;
	}
	
	@Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
	    String table = null;
	    String selection = null;
        String[] uniqueColumns = null;
	    switch (sUriMatcher.match(uri)) {
	        case MATCH_SETTINGS: {
	            table = TABLE_SETTINGS;
	            uniqueColumns = new String[] {KEY_NAME};
	            break;
	        }
	        default:
	            throw new UnsupportedOperationException();
	        }
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                //if (db.insert(table, null, values[i]) < 0) return 0;
                selection = KEY_NAME + " = '" + values[i].getAsString(KEY_NAME) + "'";
                int numberUpdated = db.update(table, values[i], selection, null);
                
                if(numberUpdated <= 0) {
                    if (db.insert(table, null, values[i]) < 0) return 0;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        sendNotify(getContext(), uri, TAG);
        return values.length;
    }

	/**
	 * @param rowId
	 *            Greater than 0 means an item has been inserted to uri.
	 * @param table
	 *            The name of the table inserted into.
	 * @param uri
	 *            The Uri to notify, if rowId > 0.
	 * @return If rowId > 0, this returns the CONTENT_URI, plus table, appended
	 *         with rowId. Null if rowId <= 0.
	 */
	private Uri getInsertedUri(long rowId, String table, Uri uri) {
		Uri nodeUri = null;

		if (rowId > 0) {
			if (uri != null) {
	            sendNotify(getContext(), uri, TAG);
			}
			nodeUri = ContentUris.withAppendedId(
					Uri.parse(CONTENT_URI_SETTINGS + table), rowId);
		}

		return nodeUri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		// Replace these with valid SQL statements if necessary.
		String groupBy = null;
		String having = null;

		String limit = null;

		switch (sUriMatcher.match(uri)) {
		case MATCH_SETTINGS: {
			qb.setTables(TABLE_SETTINGS);
			// No rowID set
			break;
		}
		// Refactor : no longer using rowID
//		case SETTINGS_ID: {
//			String appWidgetId = uri.getPathSegments().get(1);
//			qb.setTables(TABLE_SETTINGS);
//			qb.appendWhere(KEY_ID + "=" + appWidgetId);
//			break;
//		}
		case MATCH_PROPERTIES: {
			qb.setTables(TABLE_PROPERTIES);
			// No rowID set
			break;
		}
		// Refactor : no longer using rowID
//		case PROPERTIES_ID: {
//			String currentId = uri.getPathSegments().get(1);
//			qb.setTables(TABLE_PROPERTIES);
//			qb.appendWhere(KEY_ID + "=" + currentId);
//			break;
//		}
		default:
			throw new IllegalArgumentException("Unknown URI" + uri);
		}

		Cursor outVal = qb.query(db, projection, selection, selectionArgs,
				groupBy, having, sortOrder, limit);

		return outVal;
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
		int count;
		String table = null;
		String rowID = null;
		String[] uniqueColumns = null;

		switch (sUriMatcher.match(uri)) {
		case MATCH_SETTINGS: {
			table = TABLE_SETTINGS;
			// rowID not set
			// A name specifies a unique setting.
			uniqueColumns = new String[] { KEY_NAME };
			selection = KEY_NAME + " = '" + values.getAsString(KEY_NAME) + "'";
			break;
		}
		// Refactor : no longer using rowID
//		case SETTINGS_ID: {
//			// Path segments is 0 base, so segment 1 is the # in SINGLE_ROW
//			table = TABLE_SETTINGS;
//			// rowID not set
//			rowID = uri.getPathSegments().get(1);
//			// A name specifies a unique setting.
//			uniqueColumns = new String[] { KEY_NAME };
//			break;
//		}
		case MATCH_PROPERTIES: {
			table = TABLE_PROPERTIES;
			// The ID is the unique identifier of each property.
			uniqueColumns = new String[] { KEY_ID };
			selection = KEY_ID + " = '" + values.getAsString(KEY_ID) + "'";
			break;
		}
		// Refactor : no longer using rowID
//		case PROPERTIES_ID: {
//			// Path segments is 0 base, so segment 1 is the # in SINGLE_ROW
//			table = TABLE_PROPERTIES;
//			// rowID not set
//			rowID = uri.getPathSegments().get(1);
//			// The ID is the unique identifier of each property.
//			uniqueColumns = new String[] { KEY_ID };
//			break;
//		}

		default:
			throw new UnsupportedOperationException();

		}
		// Values have been set with correct values to check for prior
		// existence, and update if exists.
		count = updateCheckForNew(uri, values, selection, selectionArgs, table,
				uniqueColumns, rowID);

		if (count > 0)
			uri = Uri.withAppendedPath(uri, values.getAsString(KEY_NAME));
            sendNotify(getContext(), uri, TAG);
		return count;
	}

	/**
	 * @param values
	 *            Inserted or updated to the `selection` in `table`
	 * @param selection
	 *            The items to insert or update.
	 * @param selectionArgs
	 *            selectionArgs same as SQLite selectionArgs
	 * @param table
	 *            The name of the table to insert/update into
	 * @param uniqueColumns
	 *            The columns to use in selection. Try to specify only those
	 *            columns which detail a unique item in the database.
	 * @param rowID
	 *            The item id, for specified item. Set to null to use the whole
	 *            database, as with the constants MATCH_*
	 * @return The number of rows updated. If an item is instead inserted, this
	 *         returns 0, and observers should be updated in insert.
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
			if ( cursor != null ) {
				cursor.close();
				cursor = null;
			}
			return 0;
		}

		if ( cursor != null ) {
			cursor.close();
			cursor = null;
		}

		return count;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		int count = 0;

		switch (sUriMatcher.match(uri)) {
		case MATCH_SETTINGS: {
			count = db.delete(TABLE_SETTINGS, selection, selectionArgs);
			break;
		}
		// Refactor : No longer using rowID.
//		case SETTINGS_ID: {
//			long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
//			count = db.delete(TABLE_SETTINGS, KEY_ID + "=" + appWidgetId, null);
//			break;
//		}
		case MATCH_PROPERTIES: {
			count = db.delete(TABLE_PROPERTIES, selection, selectionArgs);
			break;
		}
		// Refactor : No longer using rowID.
//		case PROPERTIES_ID: {
//			long appWidgetId = Long.parseLong(uri.getPathSegments().get(1));
//			count = db.delete(TABLE_PROPERTIES, KEY_ID + "=" + appWidgetId,
//					null);
//			break;
//		}

		default:
			throw new UnsupportedOperationException();
		}
		if (count > 0)
            sendNotify(getContext(), uri, TAG);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case MATCH_SETTINGS:
			return CONTENT_TYPE + CONTENT_UROVO + "." + TABLE_SETTINGS;
		// Refactor : No longer using rowID.
//		case SETTINGS_ID:
//			return Constants.CONTENT_ITEM_TYPE + Constants.CONTENT_DATALOGIC + "." + TABLE_SETTINGS;
		case MATCH_PROPERTIES:
			return CONTENT_TYPE + CONTENT_UROVO + "." + TABLE_PROPERTIES;
		// Refactor : No longer using rowID.
//		case PROPERTIES_ID:
//			return Constants.CONTENT_ITEM_TYPE + Constants.CONTENT_DATALOGIC + "."
//					+ TABLE_PROPERTIES;
		default:
			throw new IllegalStateException();
		}
	}

    private void writeNode(String node,String value) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(node);
            outputStream.write(value.getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private String getNode(String node) {
        FileInputStream inputStream = null;
        byte[] buffer = null;
        try {
            inputStream = new FileInputStream(node);
            buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i(TAG, "getNode =" + new String(buffer).trim());
        return new String(buffer).trim();
    }

    private void writeOemPartition(String where, String value) {
	writeNode("/sys/kernel/oem/block", "PRODUCTLINE");
        writeNode("/sys/kernel/oem/name", where);
        writeNode("/sys/kernel/oem/value_str",value);
    }

    private String readOemPartition(String where) {
	writeNode("/sys/kernel/oem/block", "PRODUCTLINE");
        writeNode("/sys/kernel/oem/name", where);
        return getNode("/sys/kernel/oem/value_str");
    }

}
