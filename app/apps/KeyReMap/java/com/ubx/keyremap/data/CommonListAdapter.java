package com.ubx.keyremap.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;

import java.util.List;

public class CommonListAdapter extends BaseAdapter {

    private static final String TAG = Utils.TAG + "#" + CommonListAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mInflater;
    private List<ListItem.Common> mItemList;

    public CommonListAdapter(Context context, List items) {
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
        ReMapTypeViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.icon_text_list_item, null);
            holder = new ReMapTypeViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.title = (TextView) convertView.findViewById(R.id.title);
            holder.arrow = (ImageView) convertView.findViewById(R.id.arrow);
            holder.deliver = (View) convertView.findViewById(R.id.deliver);
            convertView.setTag(holder);
        } else {
            holder = (ReMapTypeViewHolder) convertView.getTag();
        }

        ListItem.Common item = mItemList.get(position);
        if (!item.deliverHide) {
            holder.deliver.setVisibility(View.VISIBLE);
        } else {
            holder.deliver.setVisibility(View.GONE);
        }
        if (item.iconResId != -1) {
            holder.icon.setImageResource(item.iconResId);
            holder.icon.setVisibility(View.VISIBLE);
        } else {
            holder.icon.setVisibility(View.GONE);
        }
        if (item.titleText != null) {
            holder.title.setText(item.titleText);
        }

        return convertView;
    }

    class ReMapTypeViewHolder {
        ImageView icon;
        ImageView arrow;
        TextView title;
        View deliver;
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         