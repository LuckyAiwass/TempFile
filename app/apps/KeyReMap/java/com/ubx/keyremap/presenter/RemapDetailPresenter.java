package com.ubx.keyremap.presenter;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.device.KeyMapManager;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;

import com.ubx.keyremap.R;
import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.data.ListItem;
import com.ubx.keyremap.data.RemapDetailModel;
import com.ubx.keyremap.data.RemapTypeModel;
import com.ubx.keyremap.fragments.BaseFragment;
import com.ubx.keyremap.fragments.MapActivityFragment;
import com.ubx.keyremap.fragments.MapCodeFragment;
import com.ubx.keyremap.view.RemapDetailView;
import com.ubx.keyremap.IContract.IRemapDetailPresenter;
import com.ubx.keyremap.view.SimpleDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RemapDetailPresenter extends BasePresenter<RemapDetailModel, RemapDetailView> implements IRemapDetailPresenter {

    private static final String TAG = Utils.TAG + "#" + RemapDetailPresenter.class.getSimpleName();

    public static final int REMAP_ACTIVITY_PICKER   = 0x0001;
    public static final int EDIT_KEY_DOWN_BROADCAST = 0x0002;
    public static final int EDIT_KEY_UP_BROADCAST   = 0x0003;

    private Activity mContext;
    private String mTopBarTitle;
    private boolean showBackArrow = false;
    private String mType;
    private BaseFragment mFragment;
    private int mKeyCode;
    private String mKeyName;
    private KeyEvent mKeyEvent;

    private int mMapCode = -1;
    private String mMapName;

    private Drawable mAppIcon;
    private String mAppLabel;
    private String mAppClass;
    private String mAppPkgName;

    private boolean isSuccessfulMapped;

    public RemapDetailPresenter(Activity context) {
        mContext = context;
        mModel = new RemapDetailModel(context);
        mAppIcon = mContext.getResources().getDrawable(R.mipmap.ic_search);
        mAppLabel = mContext.getString(R.string.input_remap_activity_select);
    }

    @Override
    public void initViews() {
        mView.get().initViews();
    }

    @Override
    public void refreshUI() {
        mView.get().showHomeButton(showBackArrow);
        mView.get().updateTitle(mTopBarTitle);
        mView.get().setMapCodeName(mMapCode, mMapName);
        mView.get().setAppContent(mAppIcon, mAppLabel, mAppPkgName);
        mView.get().setBroadcastSummary(mModel.getKeyDownAction(), mModel.getKeyUpAction());

        if(mKeyCode == KeyEvent.KEYCODE_KEYBOARD_PTT ||
            mKeyCode == KeyEvent.KEYCODE_SCAN_1 ||
            mKeyCode == KeyEvent.KEYCODE_SCAN_2 ||
            mKeyCode == KeyEvent.KEYCODE_SCAN_3 ||
            mKeyCode == KeyEvent.KEYCODE_SCAN_4) {
            mView.get().setWakeUpVisibility(true);
        } else {
            mView.get().setWakeUpVisibility(false);
        }
    }

    public void setType(String type) {
        this.mType = type;
        mTopBarTitle = "NULL";
        if (mType != null && !mType.isEmpty()) {
            switch (mType) {
                case "code": {
                    mFragment = new MapCodeFragment();
                    mTopBarTitle = mContext.getString(RemapTypeModel.REMAP_TYPE_NAME.get("code"));
                    break;
                }
                case "activity": {
                    mFragment = new MapActivityFragment();
                    mTopBarTitle = mContext.getString(RemapTypeModel.REMAP_TYPE_NAME.get("activity"));
                    break;
                }
                case "broadcast": {
                    mFragment = null;
                    mTopBarTitle = mContext.getString(RemapTypeModel.REMAP_TYPE_NAME.get("broadcast"));
                    break;
                }
                default: {
                    mTopBarTitle = "ERROR TYPE" + mType;
                }
            }
            if (mFragment != null) {
                mFragment.setPresenter(this);
                mView.get().setFragment(mFragment);
            }
        }
        setTopBarTitle(mTopBarTitle + " (Code:" + mKeyCode + ")");
    }

    public String getType() {
        return mType;
    }

    public void setKeyEvent(String name, KeyEvent event) {
        this.mKeyName = name;
        this.mKeyEvent = event;
        this.mKeyCode = event.getKeyCode();
    }

    public void setShowBackArrow(boolean show) {
        this.showBackArrow = show;
    }

    public void setTopBarTitle(String title) {
        this.mTopBarTitle = title;
    }

    @Override
    public void setFragmentView(View view) {
        mView.get().setFragmentView(view);
    }

    @Override
    public void startPickActivity() {
        Intent pickerIntent = new Intent("android.intent.action.PICK_ACTIVITY");
        Intent filterIntent = new Intent("android.intent.action.MAIN", null);
        filterIntent.addCategory("android.intent.category.LAUNCHER");
        pickerIntent.putExtra("android.intent.extra.INTENT", filterIntent);
        mContext.startActivityForResult(pickerIntent, REMAP_ACTIVITY_PICKER);
    }

    @Override
    public void startEditBroadcastActivity(int requestCode) {
        if (requestCode != EDIT_KEY_DOWN_BROADCAST
                && requestCode != EDIT_KEY_UP_BROADCAST) {
            ULog.w(TAG, "Undefined request code.");
            return;
        }
        Intent editExtrasIntent = new Intent(Utils.ACTION_EDIT_INTENT_ACTIVITY);
        switch (requestCode) {
            case EDIT_KEY_DOWN_BROADCAST: {
                editExtrasIntent.putExtra("action", mModel.getKeyDownAction());
                editExtrasIntent.putParcelableArrayListExtra("extras", mModel.getKeyDownActionExtras());
                break;
            }
            case EDIT_KEY_UP_BROADCAST: {
                editExtrasIntent.putExtra("action", mModel.getKeyUpAction());
                editExtrasIntent.putParcelableArrayListExtra("extras", mModel.getKeyUpActionExtras());
                break;
            }
        }
        mContext.startActivityForResult(editExtrasIntent, requestCode);
    }

    @Override
    public void setMapCodeName(int code, String name) {
        this.mMapCode = code;
        this.mMapName = name;
    }

    @Override
    public void setAppSelectedResult(Intent data) {
        if (data != null) {
            PackageManager pm = mContext.getPackageManager();
            mAppPkgName = data.getComponent().getPackageName();
            mAppClass = data.getComponent().getClassName();
            ApplicationInfo appInfo = null;
            try {
                appInfo = pm.getApplicationInfo(mAppPkgName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (appInfo != null) {
                mAppIcon = pm.getApplicationIcon(appInfo);
                mAppLabel = pm.getApplicationLabel(appInfo).toString();
            }
        }
    }

    @Override
    public void setBroadcastEditResult(Intent data, int requestCode) {
        if (data != null) {
            String action = data.getStringExtra("action");
            ArrayList extras = data.getParcelableArrayListExtra("extras");
            switch (requestCode) {
                case RemapDetailPresenter.EDIT_KEY_DOWN_BROADCAST:{
                    mModel.setKeyDownAction(action);
                    mModel.setKeyDownActionExtras(extras);
                    break;
                }
                case RemapDetailPresenter.EDIT_KEY_UP_BROADCAST:{
                    mModel.setKeyUpAction(action);
                    mModel.setKeyUpActionExtras(extras);
                    break;
                }
            }
        }
    }

    @Override
    public List getIntentExtras() {
        return mModel.getIntentExtras();
    }

    @Override
    public List getAllKeyNames() {
        return mModel.getKeyList();
    }

    @Override
    public int findKeyCode(String name) {
        return mModel.getKeyCode(name);
    }

    @Override
    public String findKeyName(int position) {
        return mModel.getKeyName(position);
    }

    @Override
    public String getKeyDownAction() {
        return mModel.getKeyDownAction();
    }

    @Override
    public String getKeyUpAction() {
        return mModel.getKeyUpAction();
    }

    @Override
    public void addIntentExtra(ListItem.IntentExtra extra) {
        int count = mModel.getIntentExtras().size();
        if (count > 0) {
            ((ListItem.IntentExtra) mModel.getIntentExtras().get(count - 1)).deliverHide = false;
        }
        mModel.getIntentExtras().add(extra);
        mView.get().notifyIntentExtList();
    }

    @Override
    public void removeIntentExtra(int position) {
        int count = mModel.getIntentExtras().size();
        if (position == count - 1 && position - 1 >= 0) {
            ((ListItem.IntentExtra) mModel.getIntentExtras().get(position - 1)).deliverHide = true;
        }
        mModel.getIntentExtras().remove(position);
        mView.get().notifyIntentExtList();
    }

    @Override
    public void doRemap() {
        isSuccessfulMapped = false;
        if (mType != null && !mType.isEmpty()) {
            if (mType.equals("code")) {
                if (mMapCode != -1) {
                    mModel.getKeyMapManager().mapKeyEntry(mKeyEvent, KeyMapManager.KEY_TYPE_KEYCODE,
                            String.valueOf(mMapCode), generateBroadcastJSONString(), mView.get().wakeupEnable());
                    isSuccessfulMapped = true;
                } else {
                    showConfirmDialog(R.string.dialog_title_attention, R.string.dialog_message_remap_code);
                }
            } else if (mType.equals("activity")) {
                if (mAppPkgName != null) {
                    mModel.getKeyMapManager().mapKeyEntry(mKeyEvent, KeyMapManager.KEY_TYPE_STARTAC,
                            generateActivityJSONString(), generateBroadcastJSONString(), mView.get().wakeupEnable());
                    isSuccessfulMapped = true;
                } else {
                    showConfirmDialog(R.string.dialog_title_attention, R.string.dialog_message_remap_activity);
                }
            } else if (mType.equals("broadcast")) {
                String broadcast =  generateBroadcastJSONString();
                if (broadcast != null && !broadcast.isEmpty()) {
                    mModel.getKeyMapManager().mapKeyEntry(mKeyEvent, KeyMapManager.KEY_TYPE_STARTBC,
                            "", broadcast, mView.get().wakeupEnable());
                    isSuccessfulMapped = true;
                } else {
                    showConfirmDialog(R.string.dialog_title_attention, R.string.dialog_message_remap_broadcast);
                }
            }
        }
        startMappedResultActivity();
    }

    private String generateActivityJSONString() {
        String result = "";
        JSONObject jsonObject = generateIntentJSON(mAppClass + " " + mAppPkgName, mModel.getActivityExtrasJSON());
        if (jsonObject != null) {
            result = jsonObject.toString();
            result = (result == null || result.length() <= 0) ? "" : result;
        }
        return result;
    }

    private String generateBroadcastJSONString() {
        String result = "";
        JSONObject jsonObject = null;
        try {
            if (mView.get().isKeyDownBroadcastEnable()) {
                JSONObject downBroadcast = generateIntentJSON(mModel.getKeyDownAction(), mModel.getKeyDownBroadcastExtrasJSON());
                if (downBroadcast != null) {
                    jsonObject = new JSONObject();
                    jsonObject.put(KeyMapManager.KEY_DOWN_BROADCAST, downBroadcast);
                }
            }
            if (mView.get().isKeyUpBroadcastEnable()) {
                JSONObject upBroadcast = generateIntentJSON(mModel.getKeyUpAction(), mModel.getKeyUpBroadcastExtrasJSON());
                if (upBroadcast != null) {
                    if (jsonObject == null) {
                        jsonObject = new JSONObject();
                    }
                    jsonObject.put(KeyMapManager.KEY_UP_BROADCAST, upBroadcast);
                }
            }
            if (jsonObject != null) {
                result = jsonObject.toString();
                result = (result == null || result.length() <= 0) ? "" : result;
            }
        } catch (JSONException e) {
            ULog.e(TAG, "generateIntentJSONString:", e);
            result = "";
        }
        return result;
    }

    private JSONObject generateIntentJSON(String action, JSONObject extras) {
        if (action == null || action.isEmpty()) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(KeyMapManager.KEY_INTENT_ACTION, action);
            if (extras!=null) {
                jsonObject.put(KeyMapManager.KEY_INTENT_EXTRAS, extras);
            }
            return jsonObject;
        } catch (JSONException e) {
            ULog.e(TAG, "generateIntentJSON:", e);
            return null;
        }
    }

    private void showConfirmDialog(int title, int message) {
        SimpleDialog dialog = new SimpleDialog(mContext, title, message) {
            @Override
            public void onDialogOK() {

            }
        };
        dialog.disableCancel(true);
        dialog.show();
    }

    private void startMappedResultActivity() {
        if (isSuccessfulMapped) {
            Intent intent = new Intent(Utils.ACTION_RESULT_ACTIVITY);
            mContext.startActivity(intent);
            mContext.finish();
        }
    }
}
