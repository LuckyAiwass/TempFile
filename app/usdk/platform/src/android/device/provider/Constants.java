package android.device.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.provider.Settings.NameValueTable;

/**
* Javadoc comments given to access these values from the SDK
*/
public final class Constants {

    // Shared provider constants
    /** */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/";
    /** */
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/";
    /** */
    public static final String CONTENT_UROVO = "vnd.urovo";

    /** */
    public static final String KEY_ID = "_id";


    // SettingsProvider
    /** */
    public static final String AUTHORITY_SETTINGS = "com.ubx.provider.settings";

    /** */
    public static final String TABLE_SETTINGS = "settings";
    // All values are now stored as Strings. NAME and VALUE are taken
    // from android.provider.Settings.NameValueTable
    /** */
    public static final String KEY_NAME = NameValueTable.NAME;
    /** */
    public static final String KEY_VALUE = NameValueTable.VALUE;
//     /** */
//     public static final String KEY_VALUE_INT = "int";
//     /** */
//     public static final String KEY_VALUE_STRING = "string";

    /** */
    public static final String TABLE_PROPERTIES = "properties";

    /** */
    public static final String CONTENT_URI_SETTINGS = ContentResolver.SCHEME_CONTENT
	    + "://" + AUTHORITY_SETTINGS + "/";


    // KeymapProvider
    /** */
    public static final String AUTHORITY_KEYMAP = "com.urovo.provider.keymap";

    /** */
    public static final String TABLE_KEYMAP = "keymap";
    /** */
    public static final String KEY_SCANCODE = "scancode";
    /** */
    public static final String KEY_KEYCODE = "keycode";
    /** */
    public static final String KEY_CHARACTER = "character";
    /** */
    public static final String KEY_ACTION = "action";
    /** */
    public static final String KEY_METASTATE = "metaState";
    /** */
    public static final String KEY_TYPE = "type";

    /** */
    public static final String TABLE_GLYPHS = "glyphs";
    // Uses scancode, character, and metastate

    /** */
    public static final String CONTENT_URI_KEYMAP = ContentResolver.SCHEME_CONTENT
	    + "://" + AUTHORITY_KEYMAP + "/";

    /**
    * Send a notification when a particular content URI changes.
    * @param uri to send notifications for
    */
    public static void sendNotify(Context context, Uri uri, String TAG) {

	// Now send the notification through the content framework.

	String notify = uri.getQueryParameter("notify");
	if (notify == null || "true".equals(notify)) {
	    context.getContentResolver().notifyChange(uri, null);
	    if(false) Log.v(TAG, "notifying: " + uri);
	} else {
	    Log.v(TAG, "notification suppressed: " + uri);
	}
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       