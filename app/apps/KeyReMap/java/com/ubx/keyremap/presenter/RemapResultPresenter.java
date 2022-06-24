package com.ubx.keyremap.presenter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.dao.DataUpdateTask;
import com.ubx.keyremap.data.RemapResultModel;
import com.ubx.keyremap.IContract.IRemapResultPresenter;
import com.ubx.keyremap.provider.DataBaseObserver;
import com.ubx.keyremap.view.RemapResultView;

public class RemapResultPresenter extends BasePresenter<RemapResultModel, RemapResultView> implements IRemapResultPresenter {

    private static final String TAG = Utils.TAG + "#" + RemapResultPresenter.class.getSimpleName();

    private Activity mContext;

    private Handler mHandler;
    private DataBaseObserver mObserver;

    private String mTopBarTitle;
    private boolean showBackArrow = false;

    public RemapResultPresenter(Activity context) {
        mContext = context;
        mModel = new RemapResultModel(context);
        mHandler = new Handler(mContext.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case DataBaseObserver.MSG_KEY_MAP_TABLE_CHANGED: {
                        if (message.obj == null) {
                            break;
                        }
                        if (mModel.getMappedKeyCursor() != null) {
                            mModel.getMappedKeyCursor().close();
                        }
                        mModel.setMappedKeyCursor((Cursor) message.obj);
                        if (mView.get() != null) {
                            mView.get().notifyMappedKeyList(mModel.getMappedKeyCursor());
                        }
                        break;
                    }
                    default:
                        break;
                }
                return true;
            }
        });
        mObserver = new DataBaseObserver(mContext, mHandler);
    }

    @Override
    public void initViews() {
        mView.get().initViews();
    }

    @Override
    public void refreshUI() {
        mView.get().updateTitle(mTopBarTitle);
        mView.get().showHomeButton(showBackArrow);
    }

    @Override
    public void initListData() {
        mObserver.observe();
    }

    @Override
    public void updateFilesData() {
        Intent intentAc = mContext.getIntent();
        if (intentAc != null) {
            String action = intentAc.getAction();
            if (action != null) {
                if (action.equals("action.PROGRAMMABLE_IMPORT_KEY")) {
                    new DataUpdateTask(mContext, mObserver, DataUpdateTask.TYPE_EXTERNAL_IMPORT).execute("import");
                } else if (action.equals("action.PROGRAMMABLE_EXPORT_KEY")) {
                    new DataUpdateTask(mContext, DataUpdateTask.TYPE_EXTERNAL_EXPORT).execute("export");
                } else if (action.equals("action.PROGRAMMABLE_KEY")) {
                    int actionKey = intentAc.getIntExtra("programmable", 1);
                    if (actionKey == 1) {
                        new DataUpdateTask(mContext, mObserver, DataUpdateTask.TYPE_EXTERNAL_IMPORT).execute("import");
                    } else if (actionKey == 2) {
                        new DataUpdateTask(mContext, DataUpdateTask.TYPE_EXTERNAL_EXPORT).execute("export");
                    }
                }
            }
        }
    }

    public void setShowBackArrow(boolean show) {
        this.showBackArrow = show;
    }

    public void setTopBarTitle(String title) {
        this.mTopBarTitle = title;
    }

    /**
     * Delete all entries from KeyboardMapProvider
     */
    @Override
    public void deleteAll(String selection) {
        ContentResolver cr = mContext.getContentResolver();
        cr.delete(mModel.getKeyMapManager().CONTENT_URI, selection, null);
    }

    @Override
    public void importConfig() {
        DataUpdateTask task = new DataUpdateTask(mContext, mObserver, DataUpdateTask.TYPE_INTERNAL_IMPORT);
        task.execute("import");
    }

    @Override
    public void exportConfig() {
        DataUpdateTask task = new DataUpdateTask(mContext, DataUpdateTask.TYPE_INTERNAL_EXPORT);
        task.execute("export");
    }

    public void notify1aAChanged() {
        try {
            Utils.toWriteCap_map("0");
            String value = Utils.toReadAa1();
            Intent intent = null;
            if (value == null || " ".equals(value)) {
                intent = new Intent("action.1aA.changed");
                intent.putExtra("1aA_status", 0);
                mContext.sendBroadcast(intent);
            } else {
                intent = new Intent("action.1aA.changed");
                intent.putExtra("1aA_status", Integer.parseInt(value));
                mContext.sendBroadcast(intent);
            }
        } catch (Exception e) {
            ULog.e(TAG, "action_reset error:", e);
        }
    }

}
