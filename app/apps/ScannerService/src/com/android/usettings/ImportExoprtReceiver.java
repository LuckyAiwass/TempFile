package com.android.usettings;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;


public class ImportExoprtReceiver extends BroadcastReceiver {
    private static final String TAG = "ImportExoprtReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, action);
        Intent intentService = new Intent("action.EXPORT_IMPORT_SCANNER_SERVICE");
        Intent eintent = new Intent(getExplicitIntent(context, intentService));
        if (action.equals("action.EXPORT_SCANNER_CONFIG")) {
            eintent.putExtra("config_action", 2);
        } else if (action.equals("action.IMPORT_SCANNER_CONFIG")) {
            String filePath = intent.getStringExtra("configFilepath");
            eintent.putExtra("config_action", 1);
            eintent.putExtra("configFilepath", filePath);
        }
        try {
            context.startService(eintent);
        } catch (Exception e) {
            Log.e(TAG,
                    "Start ImportExoprt Service failed:" + e.getMessage());
        }
    }

    public static Intent getExplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }
        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);
        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);
        // Set the component to be explicit
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                