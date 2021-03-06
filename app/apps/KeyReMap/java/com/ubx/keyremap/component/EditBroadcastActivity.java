package com.ubx.keyremap.component;

import android.os.Bundle;
import android.view.MenuItem;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.presenter.EditBroadcastPresenter;
import com.ubx.keyremap.view.EditBroadcastView;
import com.ubx.keyremap.view.SimpleDialog;

/**
 * author: Created by Ho Dao on 2019/1/21 0021 23:12
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EditBroadcastActivity extends BaseActivity<EditBroadcastPresenter, EditBroadcastView> {

    private static final String TAG = Utils.TAG + "#" + EditBroadcastActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.setTopBarTitle(getString(R.string.edit_broadcast));
        mPresenter.setShowBackArrow(true);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.refreshUI();
    }

    @Override
    public void initViewPresenter() {
        setContentView(R.layout.activity_edit_bc);
        mView = new EditBroadcastView(this);
        mPresenter = new EditBroadcastPresenter(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new SimpleDialog(this, R.string.dialog_title_attention, R.string.dialog_message_edit_broadcast_abort) {
            @Override
            public void onDialogOK() {
                EditBroadcastActivity.super.onBackPressed();
            }
        }.show();
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      