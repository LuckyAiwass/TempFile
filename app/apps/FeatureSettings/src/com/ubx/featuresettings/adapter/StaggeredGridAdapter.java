package com.ubx.featuresettings.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ubx.featuresettings.R;

import java.util.List;

public class StaggeredGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    List<String> data;
    OnItemClickListener mItemClickListener;
    String key1;
    String key2;

    public StaggeredGridAdapter(List<String> data, Context context,String key1,String key2) {
        this.data = data;
        this.context = context;
        this.key1 = key1;
        this.key2 = key2;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_text, null, false);
        final KeyViewHold viewHold = new KeyViewHold(view);
        viewHold.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHold.getAdapterPosition();
                mItemClickListener.onItemClick(v,position);
            }
        });
        return viewHold;
    }

    public void setOnViewItemClickListener(OnItemClickListener mItemClickListener){
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        KeyViewHold viewHold = (KeyViewHold) holder;
        viewHold.textView.setText(data.get(position));
        if(data.get(position).equals(key1)||data.get(position).equals(key2))
            viewHold.textView.setSelected(true);
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    class KeyViewHold extends RecyclerView.ViewHolder {

        TextView textView;

        public KeyViewHold(View itemView) {
            super(itemView);
            textView =(TextView) itemView.findViewById(R.id.item_name);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(View view,int position);
//        void onItemLongClick(View view);
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 