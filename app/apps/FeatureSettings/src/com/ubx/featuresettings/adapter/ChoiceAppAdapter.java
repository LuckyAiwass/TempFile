package com.ubx.featuresettings.adapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.featuresettings.R;
import com.ubx.featuresettings.util.AppInfo;
import com.ubx.featuresettings.util.ChoiceAppDialog;
import com.ubx.featuresettings.util.ULog;

import java.util.ArrayList;
import java.util.List;

public class ChoiceAppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private ChoiceAppDialog.CallbackListener mCallbackListener;
    private DismissListener dismissListener;
    private Context context;
    private List<AppInfo> list;
    private  ArrayList<ComponentName> mAllHomeComponents = new ArrayList<>();
    private  IntentFilter mHomeFilter;
    public ChoiceAppAdapter(Context context,List<AppInfo> list, ArrayList<ComponentName> mAllHomeComponents,ChoiceAppDialog.CallbackListener callbackListener) {
    	this.context = context;
    	this.list = list;
    	this.mAllHomeComponents = mAllHomeComponents;
        this.mCallbackListener = callbackListener;
        mHomeFilter = new IntentFilter(Intent.ACTION_MAIN);
        mHomeFilter.addCategory(Intent.CATEGORY_HOME);
        mHomeFilter.addCategory(Intent.CATEGORY_DEFAULT);
    }

    public interface DismissListener {
        void dismissDialog();
    }

    public void setDismissListener(DismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app,null,false);
        final SelectHomeHolder selectHomeHolder = new SelectHomeHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            	 if (mCallbackListener!=null) {
            		 ULog.d("Click item ");
                     int position =selectHomeHolder.getAdapterPosition();
                     AppInfo appInfo = list.get(position);
                     String value = appInfo.getPackageName() + "/"+appInfo.getAppName();
                     ULog.d("value                ==    "  + value);
                	 ComponentName component = ComponentName.unflattenFromString(value);
//                     ComponentName component = new ComponentName(appInfo.getPackageName(), appInfo.getAppName());
                     context.getPackageManager().replacePreferredActivity(mHomeFilter,
                             IntentFilter.MATCH_CATEGORY_EMPTY,
                             mAllHomeComponents.toArray(new ComponentName[0]), component);
                     ULog.d("mAllHomeComponents" + mAllHomeComponents.size());
                     for(ComponentName name : mAllHomeComponents){
                    	 ULog.d("name = " + name.toString());
                     }
                     Toast.makeText(context, context.getString(R.string.set_success), Toast.LENGTH_SHORT).show();
                     mCallbackListener.getAppInfo(appInfo);
                     dismissListener.dismissDialog();
                 }
            }
        });
        return selectHomeHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    	SelectHomeHolder selectHomeHolder = (SelectHomeHolder) holder;
    	selectHomeHolder.name.setText(list.get(position).getAppName());
    	selectHomeHolder.icon.setImageDrawable(list.get(position).getIcon());
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    static class SelectHomeHolder extends RecyclerView.ViewHolder{

        private TextView name;
        private ImageView icon;
        public SelectHomeHolder(View itemView) {
            super(itemView);
           name = (TextView)itemView.findViewById(R.id.item_name);
           icon = (ImageView)itemView.findViewById(R.id.item_icon);
        }
    }

}
