package com.ubx.keyremap.component;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.presenter.RemapDetailPresenter;
import com.ubx.keyremap.view.RemapDetailView;

public class RemapDetailActivity extends BaseActivity<RemapDetailPresenter, RemapDetailView> {

    private static final String TAG = Utils.TAG + "#" + RemapDetailActivity.class.getSimpleName();

    private String mType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String name = getIntent().getStringExtra("key-name");
        KeyEvent event = getIntent().getParcelableExtra("key-event");
        mPresenter.setKeyEvent(name, event);
        mPresenter.setShowBackArrow(true);
        mPresenter.setType(mType);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.refreshUI();
    }

    @Override
    public void initViewPresenter() {
        setContentView(R.layout.activity_remap);
        mType = getIntent().getStringExtra("remap-type");
        mView = new RemapDetailView.Builder(this).create(mType);
        mPresenter = new RemapDetailPresenter(this);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RemapDetailPresenter.REMAP_ACTIVITY_PICKER: {
                if (resultCode == RESULT_OK) {
                    mPresenter.setAppSelectedResult(data);
                }
                break;
            }
            case RemapDetailPresenter.EDIT_KEY_DOWN_BROADCAST:
            case RemapDetailPresenter.EDIT_KEY_UP_BROADCAST: {
                if (resultCode == RESULT_OK) {
                    mPresenter.setBroadcastEditResult(data, requestCode);
                }
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           