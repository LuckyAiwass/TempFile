package com.ubx.scanwedge.settings.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.service.ImportExportAsyncTask;
import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.database.helper.UConstants;
import com.ubx.database.helper.USettings;

import java.io.File;
import java.util.ArrayList;

import static com.ubx.database.helper.UConstants.PROFILE_NAME;

public class ScanWedgeSettings extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final String TAG = "ScanwedgeSettings";
    private static final String EXPORT_PROFILE_PREF_KEY = "export_scanwedge_config";
    private static final String IMPORT_PROFILE_PREF_KEY = "import_scanwedge_config";
    private static final String KEY_RESET_SCANWEDGE_PROFILE = "reset_scanwedge_profile";

    private PreferenceScreen root;
    private Preference resetScannerPref = null;
    private SwitchPreference mEnableDataWedge;
    private SwitchPreference mEnableDataWedgeLogging;
    private ScanWedgeApplication mApplication;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mApplication = (ScanWedgeApplication)getApplication();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.scanwedge_settings);
        root = this.getPreferenceScreen();
        //setTitle(R.string.scanwedge_settings);
        if (root.findPreference(KEY_RESET_SCANWEDGE_PROFILE) == null) {
            resetScannerPref = new Preference(this);
            resetScannerPref.setKey(KEY_RESET_SCANWEDGE_PROFILE);
            resetScannerPref.setTitle(R.string.scanner_reset_def);
            resetScannerPref.setSummary(R.string.scanner_reset_def_summary);
            root.addPreference(resetScannerPref);
        }
        mEnableDataWedge = (SwitchPreference) root.findPreference("datawedge_enable");
        String enable = USettings.DW.getString(getContentResolver(), UConstants.DW_ENABLED, "true");
        mEnableDataWedge.setChecked(enable.equals("true"));
        mEnableDataWedgeLogging = (SwitchPreference) root.findPreference("datawedge_logging_enable");
        enable = USettings.DW.getString(getContentResolver(), UConstants.DW_LOGS_ENABLED, "false");
        mEnableDataWedgeLogging.setChecked(enable.equals("true"));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        try {
            String key = preference.getKey();
            //android.util.Log.i("debug", "onPreferenceTreeClick==================" + key);
            if (key == null) return false;
            if (key.equals(IMPORT_PROFILE_PREF_KEY)) {
                configFilepath = null;
                File configFile = new File(ImportExportAsyncTask.DEFAULT_IES_CONFIG_PROFILE_NAME);
                if (configFile.exists()) {
                    configFilepath = ImportExportAsyncTask.DEFAULT_IES_CONFIG_PROFILE_NAME;
                    Toast.makeText(this, "文件路径：" + configFilepath, Toast.LENGTH_SHORT).show();
                    ImportExportAsyncTask task = new ImportExportAsyncTask(this, ImportExportAsyncTask.IES_CONFIG_ACTION_IMPORT,true);
                    task.execute(profileName, configFilepath);
                } else {
                    openFileSelector();
                }
            } else if (key.equals(EXPORT_PROFILE_PREF_KEY)) {
                profileItems.clear();
                try {
                    Cursor cursor = getContentResolver().query(USettings.Profile.CONTENT_URI_PROFILES, null, null, null, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            String title = cursor.getString(cursor.getColumnIndexOrThrow(PROFILE_NAME));
                            Log.v(TAG, "PROFILE_NAME = " + title);
                            profileItems.add(title);
                        }
                        cursor.close();
                        cursor = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (profileItems.size() > 0) {
                    showProfileList();
                }
            } else if (preference == resetScannerPref) {
                showResetDialog(1);
            } else if (preference == mEnableDataWedge) {
                USettings.DW.putString(getContentResolver(), UConstants.DW_ENABLED, mEnableDataWedge.isChecked() ? "true": "false");
            } else if (preference == mEnableDataWedgeLogging) {
                USettings.DW.putString(getContentResolver(), UConstants.DW_LOGS_ENABLED, mEnableDataWedgeLogging.isChecked() ? "true": "false");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    ArrayList<String> profileItems = new ArrayList<String>();
    String profileName = null;
    String configFilepath = null;
    int FILE_SELECTOR_CODE = 1000;

    private void openFileSelector() {
        profileName = null;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.setType("*/*.txt;*/*.xml");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, FILE_SELECTOR_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_SELECTOR_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            Toast.makeText(this, "文件路径：" + uri.getPath().toString(), Toast.LENGTH_SHORT).show();
            configFilepath = uri.getPath().toString();
            ImportExportAsyncTask task = new ImportExportAsyncTask(this, ImportExportAsyncTask.IES_CONFIG_ACTION_IMPORT,true);
            task.execute(profileName, configFilepath);

        }
    }

    private void showProfileList() {
        profileName = null;
        final String[] items = (String[]) profileItems.toArray(new String[profileItems.size()]);
        if(items.length > 0) {
            profileName = items[0];
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this, 0);
        builder.setTitle(R.string.scanner_settings);
        builder.setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        profileName = items[which];
                        Log.v(TAG, "setSingleChoiceItems PROFILE_NAME = " + profileName + " item: "+ which);
                    }
                });
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Log.v(TAG, "setPositiveButton PROFILE_NAME = " + which);
                if (profileName != null) {
                    ImportExportAsyncTask task = new ImportExportAsyncTask(ScanWedgeSettings.this, ImportExportAsyncTask.IES_CONFIG_ACTION_EXPORT, true);
                    task.execute(profileName, configFilepath);
                }
            }
        });
        builder.setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        String key = preference.getKey();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showResetDialog(final int type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ScanWedgeSettings.this);
        builder.setMessage(R.string.scanner_reset_def_alert);
        builder.setTitle(R.string.scanner_reset_def);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ResetAsyncTask task = new ResetAsyncTask(ScanWedgeSettings.this);
                task.execute("reset");
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    class ResetAsyncTask extends AsyncTask<String, String, Integer> {
        private Context mContext;
        private ProgressDialog pDialog;

        public ResetAsyncTask(Context c) {
            mContext = c;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage(mContext.getResources().getString(R.string.scanner_reset_progress));
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                try{
                    if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
                        mApplication.getService().setDefaults();
                    } else {
                        mApplication.getIService().setDefaults();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
                USettings.AppList.resetAssociatedAppsTable(getContentResolver());
                USettings.Profile.resetProfileTable(getContentResolver(), USettings.Profile.DEFAULT_ID);
                loadDefaultProfileSettings();
                USettings.System.initSettings(mContext, USettings.Profile.DEFAULT_ID);
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (pDialog != null) pDialog.dismiss();
            Toast.makeText(mContext, R.string.scanner_toast, Toast.LENGTH_LONG).show();
        }
    }
    public int loadDefaultProfileSettings() {
        int profileId = (int) USettings.Profile.createProfile(getContentResolver(), USettings.Profile.DEFAULT);
        if (profileId == -1) {
            Log.w(TAG, "init TABLE_PROFILES error");
        }
        int listId = (int) USettings.AppList.refreshList(getContentResolver(), profileId, USettings.AppList.DEFAULT);
        if (listId == -1) {
            Log.w(TAG, "init TABLE_APP_LIST error");
        }
        Log.w(TAG, "init ProfileSettings profileId=" + profileId);
        return profileId;
    }
}
