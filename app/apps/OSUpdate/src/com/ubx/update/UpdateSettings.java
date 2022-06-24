
package com.ubx.update;

import com.ubx.update.misc.Constants;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.os.SystemProperties;

public class UpdateSettings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    public static String TAG = "OSUpdate" + UpdateSettings.class.getSimpleName();

    public static final String NOTIFICATION = "com.ubx.update.NOTIFICATION";
    public static final String BACKGROUND_CHECK = "com.ubx.update.BACKGROUND_CHECK";
    public static final String TIMINGUPDATECHECK = "com.ubx.update.TIMINGUPDATECHECK";
    public static final String RANGETIMEUPDATE = "com.ubx.update.RANGETIMEUPDATE";
    public static final String PUSH_RESULT_TO_SERVER = "com.ubx.update.PUSH_RESULT_TO_SERVER";
    
    public static final String SHARED_PREFS_NAME = "com.ubx.update.UpdateSettings_preferences";
    public static final String KEY_UPDATE_PREFS = "auto_update_prefs";

    public static final String KEY_UPDATE_TIMEOUT_PREFS = "update_timeout_prefs";

    public static final String KEY_LAST_UPDATE_TIME = "last_update_time";
    public static final String KEY_BOOT_CHECK = "boot_completed_check";
    public static final String KEY_SCHEDULED_CHECK = "auto_scheduled_check";
    public static final String KEY_SCHEDULED_TIME = "auto_scheduled_time";
    
    public static final String KEY_DELAYED_UPDATE_ENABLE = "enable_update_delayed";
    public static final String KEY_DELAYED_UPDATE_TIME = "update_delayed_time";
    
    public static final String KEY_AUTO_DOWNLOAD_UPDATE = "auto_download_update";
    private ListPreference mUpdatetimeout = null;

    private ListPreference mUpdateStyle = null;
    private CheckBoxPreference boot_completed_check;
    private CheckBoxPreference auto_scheduled_check;
    private CheckBoxPreference auto_download_update;
    SharedPreferences mPreferenceManager;
    int[]  auto_scheduled_time;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.os_update_prefs);

        Log.e(TAG, "Oncreate findpref.");

        //mUpdateStyle = (ListPreference) findPreference(KEY_UPDATE_PREFS);
        mUpdatetimeout = (ListPreference) findPreference(KEY_UPDATE_TIMEOUT_PREFS);
        boot_completed_check =  (CheckBoxPreference) getPreferenceScreen().findPreference("boot_completed_check");
        auto_scheduled_check =  (CheckBoxPreference) getPreferenceScreen().findPreference("auto_scheduled_check");
        auto_download_update =  (CheckBoxPreference) getPreferenceScreen().findPreference(KEY_AUTO_DOWNLOAD_UPDATE);
        mPreferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        auto_scheduled_time = getResources().getIntArray(R.array.update_auto_scheduled_time_values);
    }

    public void onResume() {
        super.onResume();

        //mUpdateStyle.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        mUpdatetimeout.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        //mUpdateStyle.setSummary(mUpdateStyle.getEntry());

        //mUpdatetimeout.setSummary(mUpdatetimeout.getEntry());

        //String defaultConfig = getResources().getString(R.string.config_autoupdate_mode);
        boolean defaultConfig = false;
        //兼容旧的OS版本
        String autoupdateold = mPreferenceManager.getString(UpdateSettings.KEY_UPDATE_PREFS,  "");
        if(autoupdateold.equals("1")) {
            defaultConfig = true;
        }
        boolean defaultscheduled = getResources().getBoolean(R.bool.config_scheduled_update);
        Log.e(TAG, " autoupdate defaultConfig " + defaultConfig + " scheduled_update "  + defaultscheduled);
        boolean autoupdate = mPreferenceManager.getBoolean(KEY_BOOT_CHECK,SystemProperties.get("persist.sys.bootcompleted.update", "false").equals("true") || getResources().getBoolean(R.bool.config_bootcomplete_update));
        Log.e(TAG, " KEY_BOOT_CHECK  " + autoupdate);
        if(defaultConfig == true) {
            autoupdate = defaultConfig;
        }
        boot_completed_check.setChecked(autoupdate);
        autoupdate = mPreferenceManager.getBoolean(KEY_SCHEDULED_CHECK,!SystemProperties.get("persist.sys.urv.scheduled.update.frequency", "0").equals("0") || defaultscheduled);
        Log.e(TAG, " KEY_SCHEDULED_CHECK  " + autoupdate);
        auto_scheduled_check.setChecked(autoupdate);
        String  defaultscheduledTime = getResources().getString(R.string.config_scheduled_update_time);
	if(!SystemProperties.get("persist.sys.urv.scheduled.update.frequency", "0").equals("0")){
	    defaultscheduledTime = mPreferenceManager.getString(KEY_UPDATE_TIMEOUT_PREFS, SystemProperties.get("persist.sys.urv.scheduled.update.frequency", "0"));
        }else{
	    defaultscheduledTime = mPreferenceManager.getString(KEY_UPDATE_TIMEOUT_PREFS, getResources().getString(R.string.config_scheduled_update_time));
	}
	mUpdatetimeout.setSummary(getResources().getString(R.string.update_time_interval_summary,UpdateUtil.secondToTime(Integer.parseInt(defaultscheduledTime),this)));
	Log.e(TAG, " defaultscheduledTime  " + defaultscheduledTime);
        autoupdate = mPreferenceManager.getBoolean(UpdateSettings.KEY_AUTO_DOWNLOAD_UPDATE, getResources().getBoolean(R.bool.config_confirm_download_update));
        Log.e(TAG, " KEY_AUTO_DOWNLOAD_UPDATE  " + autoupdate);
        auto_download_update.setChecked(autoupdate);
        //mUpdatetimeout.setValue(String.valueOf());
       /* if (mUpdateStyle.getSharedPreferences().getString(KEY_UPDATE_PREFS, defaultConfig)
                .equals("2")) {
            mUpdatetimeout.setEnabled(true);
        } else {
            mUpdatetimeout.setEnabled(false);
        }*/

        //String enableSilentUpdate = new android.device.DeviceManager().getSettingProperty("System-enable_silent_update");
	String enableSilentUpdate = Settings.System.getString(this.getContentResolver() ,"enable_silent_update");
        if (enableSilentUpdate != null && enableSilentUpdate.equals("true")) {
            boot_completed_check.setEnabled(false);
            auto_scheduled_check.setEnabled(false);
            auto_download_update.setEnabled(false);
            mUpdatetimeout.setEnabled(false);
        }
        
    }

    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        //mUpdateStyle.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        mUpdatetimeout.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    @Override
    @Deprecated
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        String key = preference.getKey();
        Editor mEditor = mPreferenceManager.edit();
        if(key.equals(KEY_BOOT_CHECK)) {
            mEditor.putBoolean(KEY_BOOT_CHECK, boot_completed_check.isChecked());
            mEditor.putString(KEY_UPDATE_PREFS, "0");
            mEditor.commit();
        } else if(key.equals(KEY_SCHEDULED_CHECK)) {
            if(auto_scheduled_check.isChecked()) {
                String time = mPreferenceManager.getString(KEY_UPDATE_TIMEOUT_PREFS, getResources().getString(R.string.config_scheduled_update_time));
                Log.v(TAG, "KEY_UPDATE_TIMEOUT_PREFS" + key + "::" + time);
                //scheduleNextCheck(UpdateSettings.this, Integer.valueOf(time));
                int value = Integer.valueOf(time);
                if(value > 0) {
                    UpdateUtil.scheduleUpdateService(this, value * 1000);
                }
            } else {
                //cancelScheduleCheck(this);
                UpdateUtil.scheduleUpdateService(this, Constants.UPDATE_FREQ_NONE);
            }
            mEditor.putBoolean(KEY_SCHEDULED_CHECK, auto_scheduled_check.isChecked());
            mEditor.commit();
        } else  if(key.equals(KEY_AUTO_DOWNLOAD_UPDATE)) {
            mEditor.putBoolean(KEY_AUTO_DOWNLOAD_UPDATE, auto_download_update.isChecked());
            mEditor.commit();
        }
        return true;
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        // TODO Auto-generated method stub
        if (key.equals(KEY_UPDATE_TIMEOUT_PREFS)) {
            String time = preferences.getString(KEY_UPDATE_TIMEOUT_PREFS, getResources().getString(R.string.config_scheduled_update_time));
	    mUpdatetimeout.setSummary(getResources().getString(R.string.update_time_interval_summary,UpdateUtil.secondToTime(Integer.parseInt(time),this)));
            //cancelScheduleCheck(this);
            //scheduleNextCheck(UpdateSettings.this, Integer.valueOf(time));
            int value = Integer.valueOf(time);
            if(value > 0) {
                UpdateUtil.scheduleUpdateService(this, value * 1000);
            }
            Log.v(TAG, "KEY_UPDATE_TIMEOUT_PREFS" + key + "::" + time);
        }
    }

    public static String getPeriodic(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        if (sp.contains(KEY_UPDATE_TIMEOUT_PREFS))
            return sp.getString(KEY_UPDATE_TIMEOUT_PREFS, ctx.getResources().getString(R.string.config_scheduled_update_time));
        return  ctx.getResources().getString(R.string.config_scheduled_update_time);
    }

    public static void scheduleNextCheck(Context ctx, int periodic) {
        Log.e(TAG, "scheduleNextCheck(), periodic: " + periodic);
        if (periodic > 0)
            return;

        long nextUpdateTime = System.currentTimeMillis() + getLastUpdatTime(ctx) + 3 * 60 * 1000;
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, NotificationReceiver.class);
        intent.setAction(NOTIFICATION);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, nextUpdateTime, pi);
    }

    public static void cancelScheduleCheck(Context ctx) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, NotificationReceiver.class);
        intent.setAction(NOTIFICATION);
        PendingIntent senderPi = PendingIntent.getBroadcast(ctx, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if (senderPi != null) {
            Log.i(TAG, "cancel alarm");
            am.cancel(senderPi);
        } else {
            Log.i(TAG, "sender == null");
        }
    }

    public static void scheduleNextCheck(Context ctx) {
        Log.e(TAG, "scheduleNextCheck()");
        String periodic = getPeriodic(ctx);
        Log.e(TAG, "scheduleNextCheck() periodic = " + periodic);
        if ("0".equals(periodic))
            return;
        long nextUpdateTime = System.currentTimeMillis() + getLastUpdatTime(ctx)
                + Integer.valueOf(periodic) * 60 * 60 * 1000;
        // long nextUpdateTime = SystemUpgradeActivity.getLastUpdatTime(ctx)
        // + 2 * 60 * 1000;
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, NotificationReceiver.class);
        intent.setAction(NOTIFICATION);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.set(AlarmManager.RTC_WAKEUP, nextUpdateTime, pi);
    }

    public static void saveLastUpdateTime(Context ctx) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.putLong(KEY_LAST_UPDATE_TIME, System.currentTimeMillis());
        editor.apply();
    }

    public static long getLastUpdatTime(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        if (sp.contains(KEY_LAST_UPDATE_TIME))
            return sp.getLong(KEY_LAST_UPDATE_TIME, 0);
        return 0;
    }


}
