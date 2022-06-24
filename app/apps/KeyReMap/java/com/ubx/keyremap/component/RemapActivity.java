package com.ubx.keyremap.component;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Trace;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.ubx.keyremap.R;
import com.ubx.keyremap.ULog;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.presenter.RemapTypePresenter;
import com.ubx.keyremap.view.RemapTypeView;

import java.util.ArrayList;
import java.util.List;

public class RemapActivity extends BaseActivity<RemapTypePresenter, RemapTypeView> {

    private static final String TAG = Utils.TAG + "#" + RemapActivity.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST = 0x0001;

    private static final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private List<String> mUncheckedPermissions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermissions();
        mPresenter.setTopBarTitle(getString(R.string.app_name));
        mPresenter.setShowBackArrow(true);
        mPresenter.setShowListView(false);
        mPresenter.setShowResetMapView(false);
        mPresenter.setRemapTypeText(getString(R.string.txt_remap_scans_input));
    }

    @Override
    protected void onResume(){
        super.onResume();
        mPresenter.updateListViewState();
        mPresenter.refreshUI();
        mPresenter.disableInterception(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.disableInterception(true);
    }

    @Override
    public void initViewPresenter() {
        setContentView(R.layout.activity_main);
        mView = new RemapTypeView(this);
        mPresenter = new RemapTypePresenter(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mPresenter.disableInterception(true);
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        ULog.v(TAG, "dispatchKeyEvent " + event);
        mPresenter.interceptionKeyEvent(event);
        mPresenter.refreshUI();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        ULog.v(TAG, "onKeyDown keycode " + keyCode);
        mPresenter.interceptionKeyEvent(event);
        mPresenter.refreshUI();
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        ULog.v(TAG, "onKeyUp keycode " + keyCode);
        return super.onKeyUp(keyCode, event);
    }

    private boolean initPermissions() {
        Trace.beginSection("initPermissions");
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mUncheckedPermissions.clear();
                for (String permission : REQUIRED_PERMISSIONS) {
                    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        mUncheckedPermissions.add(permission);
                        if (shouldShowRequestPermissionRationale(permission)) {
                            ULog.v(TAG, "Required permissions are accessed for this application.");
                        }
                    }
                }
                if (mUncheckedPermissions.size() > 0) {
                    requestPermissions(mUncheckedPermissions
                            .toArray(new String[mUncheckedPermissions.size()]), PERMISSIONS_REQUEST);
                    return false;
                }
            }
        } finally {
            Trace.endSection();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            final int requestCode, final String[] permissions, final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST) {
            boolean isAllGranted = true;
            for (int grant : grantResults) {
                if (grant == PackageManager.PERMISSION_DENIED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                overridePendingTransition(0, 0);
            } else {
                Toast.makeText(this, "Some permissions denied！", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
