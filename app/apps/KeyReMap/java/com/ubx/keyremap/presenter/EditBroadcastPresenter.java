package com.ubx.keyremap.presenter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.data.EditBroadcastModel;
import com.ubx.keyremap.data.ListItem;
import com.ubx.keyremap.fragments.BaseFragment;
import com.ubx.keyremap.fragments.EditBroadcastFragment;
import com.ubx.keyremap.view.EditBroadcastView;
import com.ubx.keyremap.IContract.IEditBroadcastPresenter;
import com.ubx.keyremap.view.SimpleDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * author: Created by Ho Dao on 2019/1/21 0021 22:06
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EditBroadcastPresenter extends BasePresenter<EditBroadcastModel, EditBroadcastView> implements IEditBroadcastPresenter {

    private static final String TAG = Utils.TAG + "#" + EditBroadcastPresenter.class.getSimpleName();

    private Activity mContext;
    private BaseFragment mFragment;
    private String mTopBarTitle;
    private boolean showBackArrow = false;

    public EditBroadcastPresenter(Activity context) {
        mContext = context;
        mModel = new EditBroadcastModel();
    }

    @Override
    public void initViews() {
        mView.get().initViews();
        mFragment = new EditBroadcastFragment();
        if (mFragment != null) {
            mFragment.setPresenter(this);
            mView.get().setFragment(mFragment);
        }
    }

    @Override
    public void refreshUI() {
        mView.get().showHomeButton(showBackArrow);
        mView.get().updateTitle(mTopBarTitle);
        mView.get().setBroadcastContent(mModel.getBroadcastAction());
    }

    public void setShowBackArrow(boolean show) {
        this.showBackArrow = show;
    }

    public void setTopBarTitle(String title) {
        this.mTopBarTitle = title;
    }

    @Override
    public List getIntentExtras() {
        return mModel.getIntentExtras();
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
    public void setFragmentView(View view) {
        Intent intent = mContext.getIntent();
        String action = intent.getStringExtra("action");
        ArrayList extras = intent.getParcelableArrayListExtra("extras");
        mModel.setBroadcastAction(action);
        mModel.setIntentExtras(extras);
        mView.get().setFragmentView(view);
    }

    @Override
    public void setBroadcastAction(String action) {
        mModel.setBroadcastAction(action);
    }

    @Override
    public void confirmEdit() {
        if (mModel.getBroadcastAction() != null) {
            Intent data = new Intent();
            data.putExtra("action", mModel.getBroadcastAction());
            data.putParcelableArrayListExtra("extras", (ArrayList) mModel.getIntentExtras());
            mContext.setResult(Activity.RESULT_OK, data);
            mContext.finish();
        } else {
            showConfirmDialog(R.string.dialog_title_attention, R.string.dialog_message_edit_broadcast);
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
}
                                                                                             