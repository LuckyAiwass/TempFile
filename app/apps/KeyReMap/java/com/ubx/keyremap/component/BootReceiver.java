package com.ubx.keyremap.component;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.device.KeyMapManager;

import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.dao.DataBaseHelper;


public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = Utils.TAG + "#" + BootReceiver.class.getSimpleName();
    private KeyMapManager mKeyMap = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        mKeyMap = new KeyMapManager(context);
        //开机同步
        context.sendBroadcast(new Intent("action.IMPORT_REMAPKEY_CONFIG"));
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            /*SQLiteDatabase db = (new DataBaseHelper(context)).getWritableDatabase();
            if (db != null) {
                db.close();
            }*/
            if (mKeyMap.hasKeyEntry(217) && mKeyMap.isInterception()) {
                try {
                    Utils.toWriteCap_map("1");
                    ULog.i(TAG, "1aA key has been mapped,to write 1 ");
                } catch (Exception e) {
                    ULog.e(TAG, "write value failed:", e);
                }
            }
        }
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      