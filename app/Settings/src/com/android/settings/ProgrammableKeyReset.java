package com.android.settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class ProgrammableKeyReset extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int resultCode = 0;
        try {
            Intent intentService = new Intent("action.PROGRAMMABLE_KEY_SERVICE");
            Intent eintent = new Intent(getExplicitIntent(this,intentService));
            eintent.putExtra("programmable", 3);
            startService(eintent);
            resultCode = 0;
            //urovo weiyu add on 2019-12-30 start
            //add for UTE to make sure all of the Keys have been reseted before finish
            Thread.sleep(1000);
            //urovo weiyu add on 2019-12-30 end
        } catch (Exception e) {
            Log.e("ProgrammableKeyReset",
                    "BackendService failed:" + e.getMessage());
            resultCode = 1;
            
        }
        Intent result = new Intent();
        result.putExtra("ResultCode", resultCode);
        result.putExtra("ErrorMessage", "Reset successed");
        ProgrammableKeyReset.this.setResult(resultCode, result);
        ProgrammableKeyReset.this.finish();
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
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               