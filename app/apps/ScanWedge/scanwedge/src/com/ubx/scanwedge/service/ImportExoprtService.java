package com.ubx.scanwedge.service;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rocky on 18-10-29.
 * 完成配置导入导出配置文件
 *
 */

public class ImportExoprtService extends IntentService {
    private static final String TAG = "Wedge" +"IEService";
    public static final String INTENT_SERVICE_ACTION = "com.ubx.scanwedge.EXPORT_IMPORT_SCANNER_SERVICE";
    //执行配置操作
    public static final String IES_CONFIG_ACTION = "config_action";
    public static final int IES_CONFIG_ACTION_UNKNOWN = 0;
    //导入配置文件
    public static final int IES_CONFIG_ACTION_IMPORT = 1;
    //导出配置文件
    public static final int IES_CONFIG_ACTION_EXPORT = 2;
    //开机自动检查导入指定配置文件
    public static final int IES_CONFIG_ACTION_AUTO = 3;
    //配置文件的Profile 名称
    public static final String IES_CONFIG_PROFILE_NAME = "profileName";
    //指定导入或导出配置文件路径
    public static final String IES_CONFIG_PROFILE_PATH = "configFilepath";
    public ImportExoprtService() {
        super(TAG);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        int actionKey = intent.getIntExtra("config_action", IES_CONFIG_ACTION_UNKNOWN);
        String profileName = intent.getStringExtra("profileName");
        Log.d(TAG, intent.getAction() + "actionKey " + actionKey + " profileName " + profileName );
        String fileName = intent.getStringExtra("configFilepath");
        int ret = -1;
        ImportExportAsyncTask task = new ImportExportAsyncTask(this);
        switch (actionKey) {
            case IES_CONFIG_ACTION_IMPORT:
            {
                ret = task.importScannerConfig(null, fileName, profileName);
            }
            break;
            case IES_CONFIG_ACTION_EXPORT:{
                ret = task.exportScannerConfig(profileName);
            }
            break;
            case IES_CONFIG_ACTION_AUTO:{
                String custom = android.os.SystemProperties.get("pwv.custom.custom", "XX");
                InputStream inputStream;
                try {
                    inputStream = getApplicationContext().getResources().getAssets().open("configs/" + custom + "_scanner_property.xml");
                    if(inputStream != null) {
                        ret = task.importScannerConfig(inputStream, null, profileName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception es) {
                }
                /*if("WALMART".equals(custom)) {
                    try {
                        inputStream = getApplicationContext().getResources().getAssets().open("configs/ims_scanner_property.xml");
                        if(inputStream != null) {
                            ret = task.importScannerConfig(inputStream, null, profileName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        BootReceiver.writeBoolean(getApplicationContext(), "autoimport", false);
                    } catch (Exception es) {
                    }
                    try {
                        inputStream = getApplicationContext().getResources().getAssets().open("configs/terminalemulator_scanner_property.xml");
                        if(inputStream != null) {
                            ret = task.importScannerConfig(inputStream, null, profileName);
                        }
                    } catch (IOException e) {
                        BootReceiver.writeBoolean(getApplicationContext(), "autoimport", false);
                        e.printStackTrace();
                    } catch (Exception es) {
                    }
                }*/
            }
            break;
        }
        Log.d(TAG,  "actionKey " + actionKey + " profileName " + profileName + " ret " + ret);
        if(ret == 0 && actionKey == IES_CONFIG_ACTION_IMPORT) {
            //默认配置实时同步
            if(!TextUtils.isEmpty(profileName) && "Default".equals(profileName))
                sendBroadcast(new Intent("action.SYNC_SCANWEDGE_IMPORT_CONFIG"));
        }
        try{
            Thread.sleep(1500);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
