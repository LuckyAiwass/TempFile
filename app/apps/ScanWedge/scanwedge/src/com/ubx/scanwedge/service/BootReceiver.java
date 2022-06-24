package com.ubx.scanwedge.service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.os.SystemProperties;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.device.provider.Settings;
import android.util.Log;

import com.ubx.scanwedge.settings.utils.ULog;
import com.ubx.database.helper.UConstants;
import com.ubx.database.helper.USettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = ULog.TAG + BootReceiver.class.getSimpleName();

    private static final String BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Broadcast BootReceiver Received!");
        switch (intent.getAction()) {
            case BOOT:
                try {
                    Intent intentService = new Intent("com.ubx.scanwedge.SCANWEDGE_SERVICE");
                    Intent eintent = new Intent(getExplicitIntent(context, intentService));
                    int mCurrentUserId = ActivityManager.getCurrentUser();
                    Log.d(TAG, "bindServiceLocked mCurrentUserId= " + mCurrentUserId);
                    //context.startServiceAsUser(eintent, new UserHandle(mCurrentUserId));
                    context.startServiceAsUser(eintent, UserHandle.OWNER);
                } catch (Exception e) {
                    Log.e(TAG,
                            "Start BackendService failed:" + e.getMessage());
                }
                File configFile = new File("sdcard/autoimport_scanner_property.xml");
                if (configFile != null && configFile.exists()) {
                    try {
                        Intent intentService = new Intent(ImportExoprtService.INTENT_SERVICE_ACTION);
                        Intent eintent = new Intent(getExplicitIntent(context, intentService));
                        eintent.putExtra(ImportExoprtService.IES_CONFIG_ACTION, ImportExoprtService.IES_CONFIG_ACTION_IMPORT);
                        eintent.putExtra(ImportExoprtService.IES_CONFIG_PROFILE_PATH, configFile.getAbsolutePath());
                        context.startService(eintent);
                    } catch (Exception e) {
                        Log.e(TAG,
                                "Start ImportExoprt Service failed:" + e.getMessage());
                    }
                } else {
                    String custom = android.os.SystemProperties.get("pwv.custom.custom", "XX");
                    if (readBoolean(context, "autoimport") == false/* || "WALMART".equals(custom)*/) {
                        try {
                            Intent intentService = new Intent(ImportExoprtService.INTENT_SERVICE_ACTION);
                            Intent eintent = new Intent(getExplicitIntent(context, intentService));
                            eintent.putExtra(ImportExoprtService.IES_CONFIG_ACTION, ImportExoprtService.IES_CONFIG_ACTION_AUTO);
                            context.startService(eintent);
                            writeBoolean(context, "autoimport", true);
                        } catch (Exception e) {
                            Log.e(TAG,
                                    "Start ImportExoprt Service failed:" + e.getMessage());
                        }
                    } else {
                        if ("QCS".equals(custom)) {
                            InputStream inputStream = null;
                            try {
                                inputStream = context.getResources().getAssets().open("configs/" + custom + "_scanner_property.xml");
                                if(inputStream != null) {
                                    inputStream.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                try{
                                    USettings.DW.putString(context.getContentResolver(), UConstants.DW_ENABLED, "false");
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            } catch (Exception es) {
                                try{
                                    USettings.DW.putString(context.getContentResolver(), UConstants.DW_ENABLED,  "false");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                break;
        }
    }

    private boolean supportHandle() {
        File file = new File("/sys/devices/soc/qpnp-smbcharger-17/usbid_scankey");
        return file.exists();
    }

    void setScanHandleEnabled(boolean enabled) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("/sys/devices/soc/qpnp-smbcharger-17/usbid_scankey");
            outputStream.write(Integer.toString(enabled ? 1 : 0).getBytes());
            outputStream.flush();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.w("BootReceiver", "setScanHandleEnabled() set ScanHandle status failed!" + e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean getVirtualBtnStatus(Context mContext) {
        try {
            int value = USettings.System.getInt(mContext.getContentResolver(), USettings.Profile.DEFAULT_ID, Settings.System.SUSPENSION_BUTTON, 0);
            return value == 1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

    public static void writeBoolean(Context context, String key_val, boolean enable) {
        try {
            SharedPreferences settings = context.getSharedPreferences("autoimport_scanner_property",
                    0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(key_val, enable);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean readBoolean(Context context, String key) {
        try {
            SharedPreferences settings = context.getSharedPreferences("autoimport_scanner_property",
                    0);
            return settings.getBoolean(key, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
