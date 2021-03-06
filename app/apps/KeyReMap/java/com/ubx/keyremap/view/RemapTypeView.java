package com.ubx.keyremap.view;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.data.CommonListAdapter;
import com.ubx.keyremap.IContract.IMainView;
import com.ubx.keyremap.presenter.RemapTypePresenter;

public class RemapTypeView extends BaseView<RemapTypePresenter> implements IMainView, AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = Utils.TAG + "#" + RemapTypeView.class.getSimpleName();

    private ListView mReMapTypeList;
    private TextView mKeyCode;
    private TextView mKeyName;
    private TextView mRemapType;
    private RelativeLayout mResetMapLayout;
    private RelativeLayout mShowMappedLayout;

    public RemapTypeView(Activity activity) {
        mContext = activity;
    }

    @Override
    public void initViews() {
        mReMapTypeList = (ListView) mContext.findViewById(R.id.key_type_list);
        mReMapTypeList.setAdapter(new CommonListAdapter(mContext, mPresenter.getRemapTypes()));
        mReMapTypeList.setOnItemClickListener(this);

        mKeyCode = (TextView) mContext.findViewById(R.id.key_code);
        mKeyName = (TextView) mContext.findViewById(R.id.key_name);
        mRemapType = (TextView) mContext.findViewById(R.id.key_type_ly);
        mResetMapLayout = (RelativeLayout) mContext.findViewById(R.id.key_map_reset_ly);
        mResetMapLayout.setVisibility(View.GONE);
        mResetMapLayout.setOnClickListener(this);
        mShowMappedLayout = (RelativeLayout) mContext.findViewById(R.id.key_mapped_ly);
        mShowMappedLayout.setOnClickListener(this);
    }

    @Override
    public void showListView(boolean show) {
        mReMapTypeList.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showResetMapView(boolean show) {
        mResetMapLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setKeyCodeName(String code, String name) {
        mKeyCode.setText(code);
        mKeyName.setText(name);
    }

    @Override
    public void setRemapTypeText(String text) {
        mRemapType.setText(text);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPresenter.doListViewClick(position);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.key_map_reset_ly: {
                new SimpleDialog(mContext, R.string.totast_remove_current_remap_msg_title,
                        R.string.totast_remove_current_remap_msg) {
                    @Override
                    public void onDialogOK() {
                        mPresenter.resetCurrentKeyMap();
                    }
                }.show();
                break;
            }
            case R.id.key_mapped_ly: {
                Intent intent = new Intent(Utils.ACTION_RESULT_ACTIVITY);
                mContext.startActivity(intent);
                break;
            }
        }
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           