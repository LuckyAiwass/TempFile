package com.ubx.keyremap.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;

import java.util.List;

public class IntentExtListAdapter extends BaseAdapter {

    private static final String TAG = Utils.TAG + "#" + IntentExtListAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mInflater;
    private List<ListItem.IntentExtra> mItemList;

    public IntentExtListAdapter(Context context, List items) {
        mItemList = items;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        if (mItemList == null)
            return -1;
        return mItemList.size();
    }

    @Override
    public Object getItem(int position) {
        if (mItemList == null)
            return null;
        return mItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IntentExtViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.intent_ext_list_item, null);
            holder = new IntentExtViewHolder();
            holder.key = (TextView) convertView.findViewById(R.id.intent_ext_key);
            holder.value = (TextView) convertView.findViewById(R.id.intent_ext_value);
            holder.deliver = (View) convertView.findViewById(R.id.deliver);
            convertView.setTag(holder);
        } else {
            holder = (IntentExtViewHolder) convertView.getTag();
        }

        ListItem.IntentExtra item = mItemList.get(position);
        if (!item.deliverHide) {
            holder.deliver.setVisibility(View.VISIBLE);
        } else {
            holder.deliver.setVisibility(View.GONE);
        }
        if (item.key != null) {
            holder.key.setText(item.key);
        }
        if (item.value != null) {
            holder.value.setText(item.value);
        }

        return convertView;
    }

    class IntentExtViewHolder {
        TextView key;
        TextView value;
        View deliver;
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                