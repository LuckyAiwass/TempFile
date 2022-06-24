package com.android.settings;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.text.TextUtils;

import java.util.List;
import com.android.settings.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ProgrammableKeyExport extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.import_export_activity);
        int resultCode = 0;
        String ErrorMessage ="";
        try {
            String exportPath = getIntent().getStringExtra("filepath");
            Log.d("ProgrammableKeyExport", "exportPath " + exportPath);
            if(TextUtils.isEmpty(exportPath)) {
                exportPath = "sdcard/";
            }
            File inportFile = new File(exportPath);
            if(!inportFile.exists()){
                 inportFile.mkdirs();
            }
            if(!inportFile.exists()){
                Log.e("ProgrammableKeyExport", "exportPath error: is not exists");
                ErrorMessage ="Export dir is not exists";
                resultCode = 1;
            } else if(inportFile.isDirectory()){
                Intent intentService = new Intent("action.PROGRAMMABLE_KEY_SERVICE");
                Intent eintent = new Intent(getExplicitIntent(this,intentService));
                eintent.putExtra("programmable", 2);
                eintent.putExtra("filepath", exportPath);
                startService(eintent);
                ErrorMessage ="Export successed";
                resultCode = 0;
            } else {
                ErrorMessage ="Export path is not dir";
                resultCode = 1;
            }
            //urovo weiyu add on 2019-12-30 start
            //add for UTE to make sure all of the Keys have been exported before finish
            Thread.sleep(1000);
            //urovo weiyu add on 2019-12-30 end
        } catch (Exception e) {
            Log.e("ProgrammableKeyExport",
                    "BackendService failed:" + e.getMessage());
            resultCode = 1;
            ErrorMessage ="Export failed:Exception ";
        }
        Intent result = new Intent();
        result.putExtra("ResultCode", resultCode);
        result.putExtra("ErrorMessage", ErrorMessage);
        ProgrammableKeyExport.this.setResult(resultCode, result);
        finish();
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            int actionKey = intent.getIntExtra("resultCode", 0);
            Intent result = new Intent();
            result.putExtra("ResultCode", actionKey == 0 ? 0 : 1);
            result.putExtra("ErrorMessage", actionKey == 0 ? "Export successed":"Export failed");
            ProgrammableKeyExport.this.setResult(actionKey == 0 ? 0 : 1, result);
            ProgrammableKeyExport.this.finish();
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        /*IntentFilter mFilter = new IntentFilter();
        mFilter.addAction("action.IMPORT_EXPORT_RESULT");
        registerReceiver(mReceiver, mFilter);
        int resultCode = 0;
        try {
            String exportPath = getIntent().getStringExtra("filepath");
            Log.d("ProgrammableKeyExport", "exportPath " + exportPath);
            if(TextUtils.isEmpty(exportPath)) {
                exportPath = "sdcard/";
            }

            Intent intentService = new Intent("action.PROGRAMMABLE_KEY_SERVICE");
            Intent eintent = new Intent(getExplicitIntent(this,intentService));
            eintent.putExtra("programmable", 2);
            eintent.putExtra("filepath", exportPath);
            startService(eintent);
        } catch (Exception e) {
            Log.e("ProgrammableKeyExport",
                    "BackendService failed:" + e.getMessage());
                    resultCode = 1;
        }
        if(resultCode == 1) {
            Intent result = new Intent();
            result.putExtra("ResultCode", resultCode);
            result.putExtra("ErrorMessage", "Export failed");
            ProgrammableKeyExport.this.setResult(1, result);
            finish();
        }*/
    }
    @Override
    protected void onPause() {
        super.onPause();
        //unregisterReceiver(mReceiver);
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
