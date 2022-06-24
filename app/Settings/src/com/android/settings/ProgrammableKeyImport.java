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

public class ProgrammableKeyImport extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.import_export_activity);
        int resultCode = 0;
        String ErrorMessage ="";
        try {
            String importPath = getIntent().getStringExtra("filepath");
            Log.d("ProgrammableKeyImport", "importPath " + importPath);
            if(TextUtils.isEmpty(importPath)) {
                importPath = "/sdcard/keys_config.txt";
            }
            File inportFile = new File(importPath);
            if(!inportFile.exists()){
                Log.e("ProgrammableKeyImport", "importPath error: is not exists");
                ErrorMessage ="Import config file not exists";
                resultCode = 1;
            } else if(inportFile.isDirectory()){
                inportFile = new File(importPath);
                if(inportFile.exists()){
                    Intent intentService = new Intent("action.PROGRAMMABLE_KEY_SERVICE");
                    Intent eintent = new Intent(getExplicitIntent(this,intentService));
                    eintent.putExtra("programmable", 1);
                    eintent.putExtra("filepath", importPath);
                    startService(eintent);
                    ErrorMessage ="Import successed";
                    resultCode = 0;
                } else {
                    Log.e("ProgrammableKeyImport", "importPath error: is not exists");
                    ErrorMessage ="Import config file not exists";
                    resultCode = 1;
                }
            } else {
                Intent intentService = new Intent("action.PROGRAMMABLE_KEY_SERVICE");
                Intent eintent = new Intent(getExplicitIntent(this,intentService));
                eintent.putExtra("programmable", 1);
                eintent.putExtra("filepath", importPath);
                startService(eintent);
                ErrorMessage ="Import successed";
                resultCode = 0;
            }
            //urovo weiyu add on 2019-12-30 start
            //add for UTE to make sure all of the Keys have been imported before finish
            Thread.sleep(1000);
            //urovo weiyu add on 2019-12-30 end
        } catch (Exception e) {
            Log.e("ProgrammableKeyImport",
                    "BackendService failed:" + e.getMessage());
            resultCode = 1;
            ErrorMessage ="Import failed:Exception ";
        }
        Intent result = new Intent();
        result.putExtra("ResultCode", resultCode);
        result.putExtra("ErrorMessage", ErrorMessage);
        ProgrammableKeyImport.this.setResult(resultCode, result);
        finish();
    }
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            int actionKey = intent.getIntExtra("resultCode", 0);
            Intent result = new Intent();
            result.putExtra("ResultCode", actionKey == 0 ? 0 : 1);
            result.putExtra("ErrorMessage", actionKey == 0 ? "Import successed":"Import failed");
            ProgrammableKeyImport.this.setResult(actionKey == 0 ? 0 : 1, result);
            ProgrammableKeyImport.this.finish();
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        /*IntentFilter mFilter = new IntentFilter();
        mFilter.addAction("action.IMPORT_EXPORT_RESULT");
        registerReceiver(mReceiver, mFilter);
        int resultCode = 0;
        String ErrorMessage ="";
        try {
            String importPath = getIntent().getStringExtra("filepath");
            Log.d("ProgrammableKeyImport", "importPath " + importPath);
            if(TextUtils.isEmpty(importPath)) {
                importPath = "/sdcard/keys_config.txt";
            } else {
                File inportFile = new File(importPath);
                if(!inportFile.exists()){
                    Log.e("ProgrammableKeyImport", "importPath error: is not exists");
                    ErrorMessage ="Import config file not exists";
                    resultCode = 1;
                } else {
                    
                    Log.e("ProgrammableKeyImport", "importPath error: is not exists");
                    ErrorMessage ="Import config file not exists";
                    resultCode = 1;
                }
            }

            Intent intentService = new Intent("action.PROGRAMMABLE_KEY_SERVICE");
            Intent eintent = new Intent(getExplicitIntent(this,intentService));
            eintent.putExtra("programmable", 1);
            eintent.putExtra("filepath", importPath);
            startService(eintent);
        } catch (Exception e) {
            Log.e("ProgrammableKeyImport",
                    "BackendService failed:" + e.getMessage());
            resultCode = 1;
        }
        if(resultCode == 1) {
            Intent result = new Intent();
            result.putExtra("ResultCode", resultCode);
            result.putExtra("ErrorMessage", "Import failed");
            ProgrammableKeyImport.this.setResult(1, result);
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
