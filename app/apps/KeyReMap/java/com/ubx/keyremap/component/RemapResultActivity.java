package com.ubx.keyremap.component;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.presenter.RemapResultPresenter;
import com.ubx.keyremap.view.RemapResultView;

public class RemapResultActivity extends BaseActivity<RemapResultPresenter, RemapResultView> {

    private static final String TAG = Utils.TAG + "#" + RemapResultActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter.setTopBarTitle(getString(R.string.title_activity_viewer));
        mPresenter.setShowBackArrow(true);
    }

    @Override
    public void initViewPresenter() {
        setContentView(R.layout.activity_result);
        mView = new RemapResultView(this);
        mPresenter = new RemapResultPresenter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.initListData();
        mPresenter.refreshUI();
        mPresenter.updateFilesData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mapped_key_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                this.onBackPressed();
                return true;
            }
            case R.id.action_reset: {
                mPresenter.deleteAll(null);
                mPresenter.notify1aAChanged();
                return true;
            }
            case R.id.action_import: {
                mPresenter.importConfig();
                return true;
            }
            case R.id.action_export: {
                mPresenter.exportConfig();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              