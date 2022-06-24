package com.ubx.keyremap.component;

import android.content.Intent;
import android.device.KeyMapManager;
import android.device.DeviceManager;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.View;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.widget.EditText;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.dao.DataUpdateTask;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.net.Uri;
import android.content.ContentResolver;
import android.content.DialogInterface;

public class KeyboardSettings extends PreferenceActivity {

    private static final String TAG = Utils.TAG + "#" + KeyboardSettings.class.getSimpleName();

    private static final int Scandcode_aA = 217;

    private KeyMapManager mKeyMap = null;
    private DeviceManager mDeviceManager;
    private ContentResolver mResolver;

    private SwitchPreference mEnableIntercept;
    private Preference mkey_remapping;
    private Preference mviewer_remapped_keys;
    private Preference pttAction;
    private Preference functionEnable;
    private Preference mWakeKey;

    public Intent intent = null;

	private final SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                String settingValue = Settings.System.getString(mResolver,KeyMapManager.SETTINGS_KEYMAP_ENABLE);
                mEnableIntercept.setChecked(mKeyMap.isInterception());
            }
        };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.keyboard_settings);
        mkey_remapping = (Preference) getPreferenceScreen().findPreference("key_remapping");
        mviewer_remapped_keys = (Preference) getPreferenceScreen().findPreference("viewer_remapped_keys");
        mEnableIntercept = (SwitchPreference) getPreferenceScreen().findPreference("disable_intercept");
        pttAction = (Preference) getPreferenceScreen().findPreference("key_ptt_broadcast");
        if(android.os.Build.PROJECT.equals("SQ53A") || android.os.Build.PROJECT.equals("SQ83")){
            getPreferenceScreen().removePreference(pttAction);
        }
        functionEnable = (Preference) findPreference("key_scan_customize");
        mWakeKey = (Preference) getPreferenceScreen().findPreference("key_wakeup");
        mKeyMap = new KeyMapManager(getApplicationContext());
        mDeviceManager = new DeviceManager();
        intent = new Intent("action.1aA.changed");
        mResolver = this.getContentResolver();
        mSettingsContentObserver.register(mResolver);
	getPreferenceScreen().removePreference(functionEnable);
    }
    abstract class SettingsContentObserver extends ContentObserver {

        public SettingsContentObserver(Handler handler) {
            super(handler);
        }

        void register(ContentResolver contentResolver) {
            contentResolver.registerContentObserver(Settings.System.getUriFor(KeyMapManager.SETTINGS_KEYMAP_ENABLE), false, this);
        }

        public void unregister(ContentResolver contentResolver) {
            contentResolver.unregisterContentObserver(this);
        }

        @Override
        public abstract void onChange(boolean selfChange, Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        mEnableIntercept.setChecked(mKeyMap.isInterception());
        /*String action = getIntent().getAction();
        if(action.equals("action.PROGRAMMABLE_KEY")) {
            int actionKey = getIntent().getIntExtra("programmable", 1);
            if(actionKey == 1) {
                UpdateAsyncTask task = new UpdateAsyncTask(this, 3);
                task.execute("import");
            } else if(actionKey == 2) {
                UpdateAsyncTask task = new UpdateAsyncTask(this, 4);
                task.execute("export");
            }
        }*/
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (mkey_remapping == preference) {
            Intent intent = new Intent("android.intent.action.KEY_REMAP");
            startActivity(intent);
        } else if (mviewer_remapped_keys == preference) {
            Intent intent = new Intent("android.intent.action.KEY_REMAP_VIEWER");
            startActivity(intent);
        } else if (mWakeKey == preference) {
            Intent intent = new Intent("android.intent.action.WAKE_KEY_SETTINGS");
            startActivity(intent);
        } else if (mEnableIntercept == preference) {
            mKeyMap.disableInterception(mEnableIntercept.isChecked());
        } else if (pttAction == preference) {
            setPttAction();
        } else if (functionEnable == preference){
            Intent intent = new Intent("android.intent.action.KEYFUNCTION");
            startActivity(intent);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //menu.add(0, 1 ,1, R.string.import_config_keys);
        //menu.add(0, 2 ,2, R.string.export_config_keys);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 1: {
                new DataUpdateTask(this, DataUpdateTask.TYPE_INTERNAL_IMPORT).execute("import");
                break;
            }
            case 2: {
                new DataUpdateTask(this, DataUpdateTask.TYPE_INTERNAL_EXPORT).execute("export");
                break;
            }
            default:
                break;
        }
        return true;
    }

    private void setPttAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_ptt));
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ptt_action, null, false);
        builder.setView(view);
        final EditText downaction = (EditText) view.findViewById(R.id.down_action);
        final EditText upaction = (EditText) view.findViewById(R.id.up_action);
        //android.os.Utils.Log.d(mDeviceManager.getPTTDownAction() + "           " + mDeviceManager.getPTTUpAction());
        downaction.setText(mDeviceManager.getPTTDownAction() == null ? "android.intent.action.PTT_KEYDOWN" : mDeviceManager.getPTTDownAction());
        upaction.setText(mDeviceManager.getPTTUpAction() == null ? "android.intent.action.PTT_KEYUP" : mDeviceManager.getPTTUpAction());
        builder.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String downStr = downaction.getText().toString().trim();
                String upStr = upaction.getText().toString().trim();
                mDeviceManager.setPTTDownAction(downStr);
                mDeviceManager.setPTTUpAction(upStr);
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }
}
