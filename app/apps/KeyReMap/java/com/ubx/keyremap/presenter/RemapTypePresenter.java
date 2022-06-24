package com.ubx.keyremap.presenter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.device.KeyMapManager;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.widget.Toast;

import com.ubx.keyremap.R;
import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.data.ListItem;
import com.ubx.keyremap.data.RemapTypeModel;
import com.ubx.keyremap.IContract.IMainPresenter;
import com.ubx.keyremap.provider.DataBaseObserver;
import com.ubx.keyremap.view.RemapTypeView;
import com.ubx.keyremap.view.SimpleDialog;

import java.util.List;

public class RemapTypePresenter extends BasePresenter<RemapTypeModel, RemapTypeView> implements IMainPresenter {

    private static final String TAG = Utils.TAG + "#" + RemapTypePresenter.class.getSimpleName();

    private Activity mContext;

    private boolean showBackArrow = false;
    private boolean showListView = false;
    private boolean showResetMapView = false;
    private String mTopBarTitle = "";
    private String mRemapTypeText = "";
    private String mKeyCode = "";
    private String mKeyName = "";

    private KeyEvent mCurrentKeyEvent;

    private boolean isInterception;
    private Handler mHandler;
    private DataBaseObserver mObserver;

    private long lastPressTime = -1;
    private long currentPressTime = -1;

    public RemapTypePresenter(Activity context) {
        mContext = context;
        mModel = new RemapTypeModel(context.getApplicationContext());
        isInterception  = mModel.getKeyMapManager().isInterception();
        mHandler = new Handler(mContext.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                switch (message.what) {
                    case DataBaseObserver.MSG_KEY_MAP_TABLE_CHANGED: {
                        if (mView.get() != null) {
                            updateListViewState();
                            refreshUI();
                        }
                        break;
                    }
                    default:
                        break;
                }
                return false;
            }
        });
        mObserver = new DataBaseObserver(mContext, mHandler);
        mContext.getContentResolver().registerContentObserver(
                KeyMapManager.CONTENT_URI, false, mObserver);
    }

    @Override
    public void initViews() {
        mView.get().initViews();
    }

    @Override
    public void refreshUI() {
        mView.get().updateTitle(mTopBarTitle);
        mView.get().showHomeButton(showBackArrow);
        mView.get().showListView(showListView);
        mView.get().showResetMapView(showResetMapView);
        mView.get().setRemapTypeText(mRemapTypeText);
        mView.get().setKeyCodeName(mKeyCode, mKeyName);
    }

    public void setShowBackArrow(boolean show) {
        this.showBackArrow = show;
    }

    public void setTopBarTitle(String title) {
        this.mTopBarTitle = title;
    }

    public void setShowListView(boolean show) {
        this.showListView = show;
    }

    public void setShowResetMapView(boolean show) {
        this.showResetMapView = show;
    }

    public void setRemapTypeText(String text) {
        this.mRemapTypeText = text;
    }

    public void setKeyCode(String code) {
        this.mKeyCode = code;
    }

    public void setKeyName(String name) {
        this.mKeyName = name;
    }

    public void disableInterception(boolean interception) {
        isInterception = interception;
        mModel.getKeyMapManager().disableInterception(interception);
    }

    @Override
    public List getRemapTypes() {
        return mModel.getRemapTypes();
    }

    @Override
    public void doListViewClick(int position) {
        if (mCurrentKeyEvent == null) {
            Toast.makeText(mContext, R.string.txt_remap_scans_input, Toast.LENGTH_SHORT).show();
            return;
        }
        ListItem.Common item = mModel.getRemapType(position);
        if (item != null && item.clickKey != null) {
            Intent intent = new Intent(Utils.ACTION_REMAP_ACTIVITY);
            intent.putExtra("remap-type", item.clickKey);
            intent.putExtra("key-name", mKeyName);
            intent.putExtra("key-event", mCurrentKeyEvent);
            mContext.startActivity(intent);
        }
    }

    @Override
    public boolean interceptionKeyEvent(KeyEvent event) {
        mCurrentKeyEvent = event;
        mKeyCode = String.valueOf(event.getKeyCode());
        mKeyName = mModel.getKeyMapManager().getKeyFieldNames().get(event.getKeyCode(), "UNKONW");
        if (event.getKeyCode() == 4 && event.getAction() == KeyEvent.ACTION_UP) {
            currentPressTime = System.currentTimeMillis();
            if ((currentPressTime - lastPressTime) < 1000) {
                ULog.d(TAG, "currentPressTime=" + currentPressTime + ",lastPressTime=" + lastPressTime);
                new SimpleDialog(mContext, R.string.dialog_title_attention, R.string.dialog_message_exit) {
                    @Override
                    public void onDialogOK() {
                        mContext.finish();
                    }
                }.show();
            }
            lastPressTime = currentPressTime;
        }
        updateListViewState();
        return true;
    }

    @Override
    public void updateListViewState() {
        int keyCode = -1;
        try {
            keyCode = Integer.parseInt(mKeyCode);
        } catch (NumberFormatException e) {
            ULog.w(TAG, "parse keycode filed:", e);
            return;
        }
	ULog.d("zzzzz","mModel.getKeyMapManager().hasKeyEntry(keyCode)===="+mModel.getKeyMapManager().hasKeyEntry(keyCode));
	ULog.d("zzzzz","mCurrentKeyEvent.getDeviceId()"+mCurrentKeyEvent.getDeviceId());
        if (mModel.getKeyMapManager().hasKeyEntry(keyCode) || mCurrentKeyEvent.getDeviceId() == -1) {//KeyCharacterMap.VIRTUAL_KEYBOARD = -1
            mRemapTypeText = mContext.getString(R.string.txt_remap_scans_input_used);
            showListView = false;
            showResetMapView = true;
        } else {
            mRemapTypeText = mContext.getString(R.string.title_activity_remap);
            showListView = true;
            showResetMapView = false;
        }
    }

    public void resetCurrentKeyMap() {
        ContentResolver cr = mContext.getContentResolver();
        cr.delete(mModel.getKeyMapManager().CONTENT_URI, "keycode=" + mKeyCode, null);
        if (mCurrentKeyEvent.getScanCode() == 217) {
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

}
