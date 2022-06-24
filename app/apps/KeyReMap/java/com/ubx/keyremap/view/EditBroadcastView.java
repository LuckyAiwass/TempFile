package com.ubx.keyremap.view;

import android.app.Activity;
import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ubx.keyremap.IContract.IEditBroadcastView;
import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.data.IntentExtListAdapter;
import com.ubx.keyremap.data.ListItem;
import com.ubx.keyremap.presenter.EditBroadcastPresenter;

/**
 * author: Created by Ho Dao on 2019/1/21 0021 22:07
 * email: 372022839@qq.com (github: sistonnay)
 */
public class EditBroadcastView extends BaseView<EditBroadcastPresenter> implements IEditBroadcastView,
        View.OnClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = Utils.TAG + "#" + EditBroadcastView.class.getSimpleName();
    private static final int MAX_SHOW_LIST_LINES = 5;

    private View mFragmentView;
    //view of send broadcast
    private RelativeLayout mBroadcastEditView;
    private TextView mBroadcastShowView;
    private RelativeLayout mIntentAddView;
    private ListView mIntentExtList;
    private IntentExtListAdapter mListAdapter;
    private RelativeLayout mConfirmEditLayout;

    public EditBroadcastView(Activity context) {
        mContext = context;
    }

    @Override
    public void setFragment(Fragment fragment) {
        if (fragment != null) {
            mContext.getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).commit();
        }
    }

    @Override
    public void setFragmentView(View view) {
        mFragmentView = view;
        mBroadcastShowView = (TextView) mFragmentView.findViewById(R.id.fg_broadcast_text);
        mBroadcastEditView = (RelativeLayout) mFragmentView.findViewById(R.id.fg_broadcast_ly);
        mBroadcastEditView.setOnClickListener(this);
        mIntentAddView = (RelativeLayout) mFragmentView.findViewById(R.id.fg_intent_ly);
        mIntentAddView.setOnClickListener(this);
        mIntentExtList = (ListView) mContext.findViewById(R.id.fg_intent_list);
        mListAdapter = new IntentExtListAdapter(mContext, mPresenter.getIntentExtras());
        mIntentExtList.setAdapter(mListAdapter);
        mIntentExtList.setOnItemLongClickListener(this);
        mConfirmEditLayout = (RelativeLayout) mFragmentView.findViewById(R.id.confirm_ly);
        mConfirmEditLayout.setOnClickListener(this);
    }

    @Override
    public void setBroadcastContent(String action) {
        if (mBroadcastShowView != null && action != null) {
            mBroadcastShowView.setText(action);
        }
    }

    @Override
    public void notifyIntentExtList() {
        if (mListAdapter != null) {
            reSetListViewHeight();
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void initViews() {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fg_intent_ly: {
                new ExtraAddDialog(mContext, R.string.intent_ext_broadcast) {
                    @Override
                    public void onDialogOK(String key, String value) {
                        ListItem.IntentExtra intentExt = new ListItem.IntentExtra();
                        intentExt.key = key;
                        intentExt.value = value;
                        intentExt.deliverHide = true;
                        mPresenter.addIntentExtra(intentExt);
                        mIntentExtList.smoothScrollToPosition(mListAdapter.getCount() - 1);
                    }
                }.show();
                break;
            }
            case R.id.fg_broadcast_ly: {
                new EditActionDialog(mContext) {
                    @Override
                    public void onDialogOK(String action) {
                        mPresenter.setBroadcastAction(action);
                        mPresenter.refreshUI();
                    }
                }.show();
                break;
            }
            case R.id.confirm_ly: {
                mPresenter.confirmEdit();
                break;
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        final int pos = position;
        new SimpleDialog(mContext, R.string.dialog_title_attention, R.string.dialog_message_intent_delete) {
            @Override
            public void onDialogOK() {
                mPresenter.removeIntentExtra(pos);
            }
        }.show();
        return true;
    }

    private void reSetListViewHeight() {
        if (mIntentExtList == null) {
            return;
        }

        ListAdapter listAdapter = mIntentExtList.getAdapter();
        if (listAdapter == null) {
            return;
        }

        LinearLayout.LayoutParams layoutParams = null; //进行布局参数的设置
        int layout_width = ViewGroup.LayoutParams.MATCH_PARENT;
        int layout_height = ViewGroup.LayoutParams.WRAP_CONTENT;

        int itemCount = listAdapter.getCount();
        if (itemCount >= MAX_SHOW_LIST_LINES) {
            View itemView = listAdapter.getView(0, null, mIntentExtList);
            itemView.measure(0, 0);
            int itemHeight = itemView.getMeasuredHeight(); //一项的高度
            layout_height = itemHeight * MAX_SHOW_LIST_LINES;
        }

        layoutParams = new LinearLayout.LayoutParams(layout_width, layout_height);
        mIntentExtList.setLayoutParams(layoutParams);
    }
}
