package com.ubx.featuresettings.util;


import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.ubx.featuresettings.R;
import com.ubx.featuresettings.adapter.ChoiceAppAdapter;
import com.ubx.featuresettings.util.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class ChoiceAppDialog extends Dialog {

    private TextView dialogTitle;
    private RecyclerView recyclerview;
    private  ArrayList<ComponentName> mAllHomeComponents = new ArrayList<>();


    private Context context;
    private ChoiceAppAdapter adapter;
    private CallbackListener callbackListener;
    private List<AppInfo> list = new ArrayList<>();
    private List<PackageInfo> packageList;
    private PackageManager packageManager;

    public ChoiceAppDialog(@NonNull Context context) {
        super(context);
    }


    public static ChoiceAppDialog newInstance(Context context, CallbackListener listener) {
        ChoiceAppDialog dialog = new ChoiceAppDialog(context);
        dialog.setContext(context);
        View v = LayoutInflater.from(context).inflate(R.layout.dialog_choice_app, null, false);
        dialog.setCallbackListener(listener);
        dialog.setContentView(v);
        dialog.initView(v);
        dialog.setTitle(context.getString(R.string.dialog_title));
        dialog.initData();
        dialog.initEvent();
        
        return dialog;
    }

    private void setCallbackListener(CallbackListener listener) {
        this.callbackListener = listener;
    }

    private void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void create() {
        super.create();
    }

    private void initView(View v) {
        recyclerview = (RecyclerView) v.findViewById(R.id.recyclerview);
        packageManager = context.getPackageManager();
        recyclerview.setLayoutManager(new LinearLayoutManager(context));
    }



    private void initEvent() {
        adapter.setDismissListener(new ChoiceAppAdapter.DismissListener() {
            @Override
            public void dismissDialog() {
                dismiss();
            }
        });

    }


    private void initData() {
        adapter = new ChoiceAppAdapter(context, list,mAllHomeComponents, callbackListener);
        recyclerview.setAdapter(adapter);

        new Thread() {
            @Override
            public void run() {
//                packageList = packageManager.getInstalledPackages(0);
                ArrayList<ResolveInfo> homeActivities = new ArrayList<>();
                ComponentName currentDefaultHome = packageManager.getHomeActivities(homeActivities);
                mAllHomeComponents.clear();
                for (ResolveInfo candidate : homeActivities) {
                	
                	 AppInfo appInfo = new AppInfo();
                	 final ActivityInfo info = candidate.activityInfo;

                	 ULog.i("info.name  == " + info.name + "   PackageName  ==  " + info.packageName);
                	 appInfo.setAppName(info.name);
                	 appInfo.setPackageName(info.packageName);
                	 appInfo.setIcon(info.loadIcon(packageManager));
                	 ComponentName activityName = new ComponentName(info.packageName, info.name);
                     mAllHomeComponents.add(activityName);
                	 if (info.packageName.equals("com.android.settings")) {
		                   continue;
		               }
                	 list.add(appInfo);
                }
                adapter.notifyDataSetChanged();
            }
        }.run();
    }



    public interface CallbackListener {
        void getAppInfo(AppInfo info);
    }

}
