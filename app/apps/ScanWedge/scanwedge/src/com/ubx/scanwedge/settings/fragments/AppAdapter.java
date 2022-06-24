package com.ubx.scanwedge.settings.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.settings.utils.ULog;
import com.ubx.database.helper.USettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppAdapter extends SimpleAdapter {
    private static final String TAG = ULog.TAG + AppAdapter.class.getSimpleName();

    public static final String ICON = "icon";
    public static final String NAME = "name";
    private static String mPackageName;
    public AppAdapter(Context context, List<Map<String, Object>> appMapList) {
        super(context,
                appMapList,
                R.layout.applist_item,
                new String[]{ICON, NAME},
                new int[] {R.id.icon, R.id.name});

        this.setViewBinder(new ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String s) {
                if (view instanceof ImageView) {
                    if (data != null && data instanceof Drawable) {
                        ((ImageView) view).setImageDrawable((Drawable) data);
                    } else  {
                        ((ImageView) view).setImageResource(android.R.drawable.sym_def_app_icon);
                    }
                    return true;
                } else if (view instanceof TextView) {
                    if (data != null && data instanceof String) {
                        ((TextView) view).setText((String)data);
                        return true;
                    }
                }
                return false;
            }
        });
        mPackageName = context.getPackageName();
    }

    public static List<Map<String, Object>> getAppMapList(PackageManager pm, ContentResolver cr) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<String> addedPkgs =  USettings.AppList.getAddedPackages(cr, -1);
            //List<PackageInfo> pkgInfoList = pm.getInstalledPackages(0);
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> launcherApps = pm.queryIntentActivities(mainIntent, 0);
            for (int i = 0; i < launcherApps.size(); i++) {
                ResolveInfo pkgInfo = launcherApps.get(i);
                /*//过滤掉系统app
                if ((ApplicationInfo.FLAG_SYSTEM & pkgInfo.applicationInfo.flags) != 0) {
                    continue;
                }*/
                if (mPackageName != null && mPackageName.equals(pkgInfo.activityInfo.packageName)) {
                    continue;
                }
                if (addedPkgs != null && addedPkgs.contains(pkgInfo.activityInfo.packageName)) {
                    continue;
                }
                Map<String, Object> map = new HashMap<>();
                map.put(AppAdapter.ICON, pkgInfo.loadIcon(pm));
                map.put(AppAdapter.NAME, pkgInfo.activityInfo.packageName);
                result.add(map);
            }
        } catch (Exception e) {
            ULog.e(TAG, "===============获取应用包信息失败");
            return null;
        }
        return result;
    }

    public static List<Map<String, Object>> getAssociatedApps(PackageManager pm, ContentResolver cr, int profileId) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<String> addedPkgs = USettings.AppList.getAddedPackages(cr, profileId);
            //List<PackageInfo> pkgInfoList = pm.getInstalledPackages(0);
            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> launcherApps = pm.queryIntentActivities(mainIntent, 0);
            for (int i = 0; i < launcherApps.size(); i++) {
                ResolveInfo pkgInfo = launcherApps.get(i);
                /*//过滤掉系统app
                if ((ApplicationInfo.FLAG_SYSTEM & pkgInfo.applicationInfo.flags) != 0) {
                    continue;
                }*/
                if (mPackageName != null && mPackageName.equals(pkgInfo.activityInfo.packageName)) {
                    continue;
                }
                if (addedPkgs != null && addedPkgs.contains(pkgInfo.activityInfo.packageName)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put(ICON, pkgInfo.loadIcon(pm));
                    map.put(NAME, pkgInfo.activityInfo.packageName);
                    result.add(map);
                }
            }
        } catch (Exception e) {
            ULog.e(TAG, "===============获取应用包信息失败");
            return null;
        }
        return result;
    }
}
