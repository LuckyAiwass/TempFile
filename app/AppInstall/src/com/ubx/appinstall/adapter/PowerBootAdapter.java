package com.ubx.appinstall.adapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import com.ubx.appinstall.R;

import com.ubx.appinstall.bean.PowerBootBean;
import com.ubx.appinstall.util.SharedPrefsStrListUtil;

public class PowerBootAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private Context context;
	private List<PowerBootBean> list;
	private PackageManager pm;
	private List<String> pmList = new ArrayList<>();
	// private PowerBootAdapter.OnItemClickListener onItemClickListener;

	public PowerBootAdapter(Context context, List<PowerBootBean> list) {
		this.context = context;
		this.list = list;
		pm = context.getPackageManager();
		pmList = SharedPrefsStrListUtil.getStrListValue(context, "POWERBOOT");
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(context).inflate(R.layout.item_app, null, false);
		final PowerBootViewHolder holder = new PowerBootViewHolder(view);
		view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int po = holder.getAdapterPosition();

				if (holder.num.getVisibility() == View.INVISIBLE) {
					if (pmList.size() >= 5) {
						Toast.makeText(context, context.getString(R.string.overmax), Toast.LENGTH_SHORT).show();
						return;
					}
					holder.num.setVisibility(View.VISIBLE);
					pmList.add(list.get(po).getPackageInfo().packageName);
					SharedPrefsStrListUtil.addStrListValue(context, "POWERBOOT",
							list.get(po).getPackageInfo().packageName);
					list.get(po).setNum(pmList.size());
					holder.num.setText(String.valueOf(pmList.size()));
				} else {
					holder.num.setVisibility(View.INVISIBLE);
					pmList.remove(list.get(po).getPackageInfo().packageName);
					SharedPrefsStrListUtil.removeStrListItem(context, "POWERBOOT",
							list.get(po).getPackageInfo().packageName);
					// 更新页面
					if (!holder.num.getText().equals(pmList.size() + 1)) {
						for (int i = list.get(po).getNum() - 1; i < pmList.size(); i++) {
							for (int j = 0; j < list.size(); j++) {
								if (pmList.get(i).equals(list.get(j).getPackageInfo().packageName)) {
									list.get(j).setNum(i + 1);
									break;
								}
							}
						}
					}

					list.get(po).setNum(0);
					notifyDataSetChanged();
				}

			}
		});
		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		PowerBootViewHolder powerBootViewHolder = (PowerBootViewHolder) holder;
		powerBootViewHolder.icon.setImageDrawable(list.get(position).getPackageInfo().applicationInfo.loadIcon(pm));
		powerBootViewHolder.name.setText(list.get(position).getPackageInfo().applicationInfo.loadLabel(pm));
		if (list.get(position).getNum() != 0) {
			powerBootViewHolder.num.setVisibility(View.VISIBLE);
			powerBootViewHolder.num.setText(String.valueOf(list.get(position).getNum()));
		} else {
			powerBootViewHolder.num.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public int getItemCount() {
		return list == null ? 0 : list.size();
	}

	// public interface OnItemClickListener {
	// void onItemClick(View view, int position);
	// void onItemLongClick(View view, int position);
	// }
	//
	//
	// public void setOnItemClickListener(PowerBootAdapter.OnItemClickListener
	// listener) {
	// this.onItemClickListener = listener;
	// }

	class PowerBootViewHolder extends RecyclerView.ViewHolder {

		ImageView icon;
		TextView name;
		TextView num;

		public PowerBootViewHolder(View itemView) {
			super(itemView);
			icon = (ImageView) itemView.findViewById(R.id.item_icon);
			name = (TextView) itemView.findViewById(R.id.item_name);
			num = (TextView) itemView.findViewById(R.id.item_num);
		}
	}
}
