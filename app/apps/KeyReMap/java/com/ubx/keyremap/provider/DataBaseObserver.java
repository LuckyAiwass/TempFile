package com.ubx.keyremap.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.device.KeyMapManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;

/**
 * DataBaseObserver observes the keymap provider. Results are posted to
 * ViewerActivity.
 */
public class DataBaseObserver extends ContentObserver {

    private static final String TAG = Utils.TAG + "#" + DataBaseObserver.class.getSimpleName();

    private static final Uri KEYMAP_URI = KeyMapManager.CONTENT_URI;
    public static final int MSG_KEY_MAP_TABLE_CHANGED = 0x0001;

    private String[] projection = {
            KeyMapManager.KEY_ID,
            KeyMapManager.KEY_SCANCODE,
            KeyMapManager.KEY_KEYCODE,
            KeyMapManager.KEY_CHARACTER,
            KeyMapManager.KEY_KEYCODE_META,
            KeyMapManager.KEY_ACTIVITY,
            KeyMapManager.KEY_BROADCAST,
            KeyMapManager.KEY_TYPE,
            KeyMapManager.KEY_WAKE};

    private Context mContext;

    private Handler mHandler;

    /**
     * @param context To register ContentObserver
     * @param handler To message updates in the database.
     */
    public DataBaseObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        this.mHandler = handler;
    }

    /**
     * Register this observer for the keymap database, and update the current
     * keymap.
     */
    public void observe() {
        ContentResolver resolver = mContext.getContentResolver();
        resolver.registerContentObserver(KEYMAP_URI, false, this);
        // resolver.registerContentObserver(GYLPHS_PURI, false, this);
        updateKeymap();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        updateKeymap();
    }

    /**
     * Get a cursor containing Keymap data, and send that to the handler.
     */
    public void updateKeymap() {
        Cursor cursor = mContext.getContentResolver().query(KEYMAP_URI, projection, null, null, null);
        if (cursor == null || cursor.isAfterLast()) {
            ULog.w(TAG, "No existed keymap record!");
            Message msg = mHandler.obtainMessage(MSG_KEY_MAP_TABLE_CHANGED, cursor);
            mHandler.sendMessage(msg);
            return;
        }
        ULog.w(TAG, "existed keymap record!");
        Message msg = mHandler.obtainMessage(MSG_KEY_MAP_TABLE_CHANGED, cursor);
        mHandler.sendMessage(msg);
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               