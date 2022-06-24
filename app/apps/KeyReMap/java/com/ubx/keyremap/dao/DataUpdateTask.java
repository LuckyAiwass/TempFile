package com.ubx.keyremap.dao;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.device.KeyMapManager;
import android.os.AsyncTask;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.provider.DataBaseObserver;
import com.ubx.keyremap.view.SimpleDialog;

/**
 * import export xml task
 */
public class DataUpdateTask extends AsyncTask<String, String, Integer> {

    private static final String TAG = Utils.TAG + "#" + DataUpdateTask.class.getSimpleName();

    public static final int TYPE_INTERNAL_IMPORT = 1;
    public static final int TYPE_INTERNAL_EXPORT = 2;
    public static final int TYPE_EXTERNAL_IMPORT = 3;
    public static final int TYPE_EXTERNAL_EXPORT = 4;

    private Context mContext;
    private KeyMapManager mKeyMapManager;
    private ProgressDialog mProgressDialog;

    private int mType;
    private DataBaseObserver mObserver;
    public DataUpdateTask(Context context, int type) {
        this(context, null, type);
    }

    public DataUpdateTask(Context context, DataBaseObserver observer, int type) {
        mContext = context;
        mObserver = observer;
        mType = type;
        mKeyMapManager = new KeyMapManager(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mContext);
        switch (mType) {
            case TYPE_INTERNAL_IMPORT:
            case TYPE_EXTERNAL_IMPORT: {
                mProgressDialog.setMessage(mContext.getResources().getString(R.string.importing_config));
                break;
            }
            case TYPE_INTERNAL_EXPORT:
            case TYPE_EXTERNAL_EXPORT: {
                mProgressDialog.setMessage(mContext.getResources().getString(R.string.exporting_config));
                DataBaseUtils.getInstance().setKeysEnable(mKeyMapManager.isInterception());
                break;
            }
            default:
                break;
        }
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    protected Integer doInBackground(String... params) {
        int result = -1;
        switch (mType) {
            case TYPE_INTERNAL_IMPORT:
            case TYPE_EXTERNAL_IMPORT: {
                result = DataBaseUtils.getInstance().importKeysConfig(mContext);
                mKeyMapManager.disableInterception(DataBaseUtils.getInstance().isKeysEnable());
                break;
            }
            case TYPE_INTERNAL_EXPORT:
            case TYPE_EXTERNAL_EXPORT: {
                result = DataBaseUtils.getInstance().exportKeysConfig(mContext);
                break;
            }
            default:
                break;
        }
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }

        //导入成功更新界面
        if (mType == TYPE_INTERNAL_IMPORT && mObserver != null) {
            mObserver.observe();
        }

        if (mType == TYPE_INTERNAL_IMPORT || mType == TYPE_EXTERNAL_IMPORT) {
            //通知PhoneWidowManager
            mContext.sendBroadcast(new Intent("action.IMPORT_REMAPKEY_CONFIG"));
        }

        int msgId1 = result == 0 ? R.string.successed_config : R.string.failed_config;
        msgId1 = result == -2 ? R.string.importing_config_failed : msgId1;
        int msgId2 = (mType == TYPE_INTERNAL_IMPORT || mType == TYPE_EXTERNAL_IMPORT)
                ? R.string.import_config_keys : R.string.export_config_keys;

        SimpleDialog alertDialog = new SimpleDialog(mContext,
                mContext.getString(R.string.dialog_title_attention),
                String.format(mContext.getResources().getString(msgId1), mContext.getResources().getString(msgId2))) {
            @Override
            public void onDialogOK() {
                if (mType == TYPE_EXTERNAL_IMPORT || mType == TYPE_EXTERNAL_EXPORT) {
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).finish();
                    }
                }
            }
        };

        alertDialog.disableCancel(true);
        alertDialog.show();
    }
}
