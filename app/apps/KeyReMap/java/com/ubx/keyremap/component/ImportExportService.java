package com.ubx.keyremap.component;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.device.KeyMapManager;

import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.dao.DataBaseUtils;

/**
 * Created by rocky on 18-10-29.
 * com.android.settings/com.android.settings.ProgrammableKeyExport
 * • com.android.settings/com.android.settings.ProgrammableKeyImport
 */

public class ImportExportService extends IntentService {

    private static final String TAG = Utils.TAG + "#" + ImportExportService.class.getSimpleName();

    public ImportExportService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int actionKey = intent.getIntExtra("programmable", 0);
        String filepath = intent.getStringExtra("filepath");
        ULog.d(TAG, "actionKey " + actionKey);

        Context context = getApplicationContext();
        KeyMapManager keyMapManager = new KeyMapManager(context);
        int ret = -1;
        if (actionKey == 1) {
            ret = DataBaseUtils.getInstance().importKeysConfig(context, filepath);
            keyMapManager.disableInterception(DataBaseUtils.getInstance().isKeysEnable());
        } else if (actionKey == 2) {
            DataBaseUtils.getInstance().setKeysEnable(keyMapManager.isInterception());
            ret = DataBaseUtils.getInstance().exportKeysConfig(context, filepath);
        }else if (actionKey == 3) {
            ret = DataBaseUtils.getInstance().resetKeysConfig(context);
        }

        ULog.d(TAG,  "ret " + ret);
        if (actionKey != 2 && ret == 0) {
            sendBroadcast(new Intent("action.IMPORT_REMAPKEY_CONFIG"));
        }
        /*if (actionKey == 3) {
            Intent resIntent = new Intent("action.IMPORT_EXPORT_RESULT");
            resIntent.putExtra("resultCode", ret);
            sendBroadcast(resIntent);
        }*/
        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 