package com.ubx.keyremap.view;

import android.app.Activity;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.data.SextupletAdapter;
import com.ubx.keyremap.IContract.IRemapResultView;
import com.ubx.keyremap.presenter.RemapResultPresenter;

public class RemapResultView extends BaseView<RemapResultPresenter> implements IRemapResultView,
        AdapterView.OnItemLongClickListener {

    private static final String TAG = Utils.TAG + "#" + RemapResultView.class.getSimpleName();

    private ListView mMappedKeyList;
    private SextupletAdapter mListAdapter;

    public RemapResultView(Activity context) {
        mContext = context;
        mListAdapter = new SextupletAdapter(mContext);
    }

    @Override
    public void initViews() {
        mMappedKeyList = (ListView) mContext.findViewById(R.id.map_result_list);
        mMappedKeyList.setAdapter(mListAdapter);
        mMappedKeyList.setOnItemLongClickListener(this);
        mListAdapter.setPresenter(mPresenter);
    }

    @Override
    public void notifyMappedKeyList(Cursor cursor) {
        if (mListAdapter != null) {
            mListAdapter.setCursor(cursor);
            mListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
        new SimpleDialog(mContext, R.string.totast_remove_current_remap_msg_title,
                R.string.totast_remove_current_remap_msg) {
            @Override
            public void onDialogOK() {
                if (id == -1)
                    return;
                mPresenter.deleteAll("_id = " + id);
                if (mListAdapter.getItemScanCode(position) == 217) {
                    mPresenter.notify1aAChanged();
                }
            }
        }.show();
        return true;
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         