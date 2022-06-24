/**
 * Copyright (c) 2011-2012, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.update;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.SystemProperties;

public class RemoteActivity extends Activity implements OnItemClickListener {

    private static final int DIALOG_SETTINGS = 1;

    private static final int DIALOG_INSTALL_LAST = 4;

    private EditText serverEdit;

    private static final String TAG = "QRDUpdateRemoteActivity";

    private static final boolean DEBUG = true;

    private ListView updateList;

    private TextView emptyText;

    private LinearLayout rootView;

    private String lastUpdatePath;
    private boolean isLastUpdateDelta;

    private DownloadManager downloadManager;

    private List<UpdateInfo> allList;
    private boolean mForceUpdate = false;
    boolean mIsUTE = false;
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.remote_updates);
        allList = new ArrayList<UpdateInfo>();
        updateList = (ListView) findViewById(R.id.update_list);
        updateList.setOnItemClickListener(this);
        emptyText = (TextView) findViewById(R.id.empty_text);
        rootView = (LinearLayout) findViewById(R.id.root);
        downloadManager = DownloadManager.getDefault(this);
        mForceUpdate = getIntent().getBooleanExtra("force", false) || getIntent().getBooleanExtra("uroforce", false);
        mIsUTE = Build.PWV_CUSTOM_CUSTOM.startsWith("UTE");

        /*lastUpdatePath = UpdateUtil.getLastUpdate(this);
        isLastUpdateDelta = UpdateUtil.getLastIsDelta(this);
        if (lastUpdatePath == null) {
            new ListUpdatesTask().execute(downloadManager.getUpdateListUrl());
        } else {
            showDialog(DIALOG_INSTALL_LAST);
        }*/
        String address = downloadManager.getServerUrl();
        Log.i(TAG, "ServerUrl: address = " + address);
        if (address != null) {
            if((Build.PWV_CUSTOM_CUSTOM.equals("UTE") || Build.PWV_CUSTOM_CUSTOM.equals("UTEWO")) && !address.contains("urovo")) {
                new ListUpdatesTask().execute(address, "UTE-OS");
            } else {
                new ListUpdatesTask().execute(UpdateUtil.buildUrl(RemoteActivity.this, address), "OS");
            }
        }
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent intent = new Intent(this, UpdateViewActivity.class);
        intent.putExtra(Intent.EXTRA_INTENT, (UpdateInfo) arg1.getTag());
        startActivity(intent);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu){
        if(Build.PWV_CUSTOM_CUSTOM.equals("Reliance")){
            menu.getItem(menu.size() -1).setVisible(false);
        }
        if ("false".equals(SystemProperties.get("pwv.osupdate.server.menu", "true"))) {
            menu.getItem(menu.size() - 2).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                // new
                // ListUpdatesTask().execute(downloadManager.getUpdateListUrl());
                String address = downloadManager.getServerUrl();
                if (address != null) {
                    allList.clear();
                    if((Build.PWV_CUSTOM_CUSTOM.equals("UTE") || Build.PWV_CUSTOM_CUSTOM.equals("UTEWO")) && !address.contains("urovo")) {
                        Log.i(TAG, "UTEServerUrl: address = " + address);
                        new ListUpdatesTask().execute(address, "UTE-OS");
                    } else {
                        new ListUpdatesTask().execute(UpdateUtil.buildUrl(RemoteActivity.this, address), "OS");
                    }
                }
                break;
            case R.id.settings:
                showDialog(DIALOG_SETTINGS);
                break;
            case R.id.auto_request_settings:
                Intent intent = new Intent(RemoteActivity.this, UpdateSettings.class);
                startActivity(intent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private class SetDialogListener implements OnClickListener {

        public void onClick(DialogInterface dialog, int which) {
            if (serverEdit.isEnabled()) {
		Log.d(TAG,"serverEdit.getText()----->"+serverEdit.getText().toString().replaceAll(" ",""));
                downloadManager.saveServerUrl(serverEdit.getText().toString().replaceAll(" ",""));
            }
        }
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case DIALOG_SETTINGS: {
                String url = downloadManager.getServerUrl();
                if (url != null && serverEdit != null) {
                    serverEdit.setText(url);
                }
                break;
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_SETTINGS: {
                View dialogView = LayoutInflater.from(this).inflate(R.layout.remote_settings, null);
                String url = downloadManager.getServerUrl();

                serverEdit = (EditText) dialogView.findViewById(R.id.server_url);
                if (url != null) {
                    serverEdit.setText(url);
                }
                if (Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
                    serverEdit.setEnabled(false);
                    EditText tmsserverEdit = (EditText) dialogView.findViewById(R.id.tms_server_url);
                    EditText tmsserverport = (EditText) dialogView.findViewById(R.id.tms_server_port);
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    tmsserverEdit.setText(prefs.getString("TMSServer", ""));
                    tmsserverport.setText( prefs.getString("TMSPort", ""));
                    tmsserverEdit.setEnabled(false);
                    tmsserverport.setEnabled(false);
                    return new AlertDialog.Builder(this).setView(dialogView)
                            .setTitle(getString(R.string.title_server_url)).create();
                } else {
                    LinearLayout tmsserverEdit = (LinearLayout) dialogView.findViewById(R.id.tms_server_info);
                    tmsserverEdit.setVisibility(View.GONE);
                    return new AlertDialog.Builder(this).setView(dialogView)
                            .setTitle(getString(R.string.title_server_url))
                            .setNeutralButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok, new SetDialogListener()).create();
                }
            }
            case DIALOG_INSTALL_LAST:
                final File update = new File(lastUpdatePath);
                return new AlertDialog.Builder(this).setTitle(update.getName())
                        .setMessage(R.string.msg_install_last)
                        .setNeutralButton(android.R.string.cancel, new OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                UpdateUtil.deteteUpdate(RemoteActivity.this);
                                new ListUpdatesTask().execute(downloadManager.getUpdateListUrl());
                            }
                        }).setPositiveButton(android.R.string.ok, new OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                UpdateUtil.deteteUpdate(RemoteActivity.this);
                                Intent intent = new Intent(
                                        isLastUpdateDelta ? InstallReceiver.ACTION_REBOOT_DELTA
                                                : InstallReceiver.ACTION_REBOOT);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                                    StrictMode.setVmPolicy(builder.build());
                                }
                                intent.setData(Uri.fromFile(update));
                                startActivity(intent);
                            }
                        }).create();
        }
        return null;
    }
    
    /**
     * 
     * @param lists
     * @param force auto download in UpdateViewActivity
     */
    private void forceUpdate(List<UpdateInfo> lists, boolean force){
    	UpdateInfo info = lists.get(0);
    	for(int i = 0; i < lists.size(); i++){
    		UpdateInfo tmp = lists.get(i);
    		if(tmp.getDelta() != null && tmp.getDelta().to > info.getDelta().to){
    			info = tmp;
    		}
    	}
    	Log.d(TAG, "force update: info name = " + info.getName());
        Intent intent = new Intent(this, UpdateViewActivity.class);
        intent.putExtra(Intent.EXTRA_INTENT,info);
        intent.putExtra("force", force);
        startActivity(intent);
        RemoteActivity.this.finish();
    }

    private class ListUpdatesTask extends AsyncTask<String, Object, List<UpdateInfo>> implements
            OnCancelListener {
        private ProgressDialog mProgressDialog;

        private String updateType = "";

        protected void onPostExecute(List<UpdateInfo> result) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (this.isCancelled()) {
                return;
            }
             Log.i("QRDUpdate", "getPkgVersion: updateType ret= " + updateType);
            if (result != null && result.size() > 0) {
                // updateList.setAdapter(new
                // UpdateListAdapter(RemoteActivity.this, result));
                UpdateInfo info = result.get(0);
                if (info.hasNewVersion()) {
                    for (int i = 0; i < result.size(); i++) {
                        result.get(i).setName(updateType);
                        allList.add(result.get(i));
                    }
                    if(mForceUpdate){
                    	forceUpdate(allList,true);
						return;
                    }
                    if(mIsUTE){
                    	forceUpdate(allList,false);
						return;
                    }
                }
                if (allList.size() > 0) {
                    rootView.setGravity(Gravity.TOP);
                    updateList.setVisibility(View.VISIBLE);
                    emptyText.setVisibility(View.GONE);
                    updateList.setAdapter(new UpdateListAdapter(RemoteActivity.this, allList));
                } else {
                    rootView.setGravity(Gravity.CENTER);
                    updateList.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                }
                Log.i("QRDUpdate", "to do next updateType");
                if ("OS".equals(updateType)) {
                    String address = downloadManager.getServerUrl();
                    String ufsUrl = UpdateUtil.buildUFSUpdatePKGUrl(RemoteActivity.this, address);
                    if (ufsUrl != null) {
                        new ListUpdatesTask().execute(ufsUrl, "UFS");
                    }
                    /*String address = downloadManager.getServerUrl();
                    String seUrl = UpdateUtil.buildSEUpdatePKGUrl(RemoteActivity.this, address);
                    if (seUrl != null) {
                        new ListUpdatesTask().execute(seUrl, "SE");
                    }*/
                } else if ("UFS".equals(updateType) && android.os.SystemProperties.get("persist.uvo.equipment","PDA").equals("POS")) {
                    String address = downloadManager.getServerUrl();
                    String seUrl = UpdateUtil.buildSEUpdatePKGUrl(RemoteActivity.this, address);
                    if (seUrl != null) {
                        new ListUpdatesTask().execute(seUrl, "SE");
                    }
                } else {
                    /*if ("UFS".equals(updateType) && UpdateUtil.isDivision()) {
                        String address = downloadManager.getServerUrl();
                        String seUrl = UpdateUtil.buildSEUpdatePKGUrl(RemoteActivity.this, address);
                        if (seUrl != null) {
                            new ListUpdatesTask().execute(seUrl, "SE");
                        }
                    } else if ("UFS".equals(updateType) && Build.PROJECT.equals("SQ75")) {
                        String address = downloadManager.getServerUrl();
                        String scUrl = UpdateUtil.buildSCUpdatePKGUrl(RemoteActivity.this, address);
                        if (scUrl != null) {
                            new ListUpdatesTask().execute(scUrl, "SC");
                        }
                    }*/
                }
            }
        }

        @Override
        protected void onPreExecute() {
            mProgressDialog = new ProgressDialog(RemoteActivity.this);
            mProgressDialog.setTitle(R.string.title_getupdate);
            mProgressDialog.setMessage(getResources().getString(R.string.msg_getupdate));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setOnCancelListener(this);
            mProgressDialog.show();
        }

        @Override
        protected List<UpdateInfo> doInBackground(String... params) {
            String url = params[0];
            if(url != null && url.startsWith("https")) {
                List<UpdateInfo> results = UpdateUtil.getUpdateInfo(url, RemoteActivity.this);
                updateType = params[1];
                //UpdateUtil.updateFileSize(RemoteActivity.this, results);
                return results;
            } else {
                List<UpdateInfo> results = UpdateUtil.getUpdateInfo(url);
                updateType = params[1];
                //UpdateUtil.updateFileSize(RemoteActivity.this, results);
                return results;
            }
            
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            this.cancel(true);
        }

    }

    private class UpdateListAdapter extends BaseAdapter {

        private Context mContext;

        private List<UpdateInfo> lists;

        public UpdateListAdapter(Context context, List<UpdateInfo> updates) {
            mContext = context;
            lists = updates;
        }

        public int getCount() {
            return lists.size();
        }

        public Object getItem(int arg0) {
            return lists.get(arg0);
        }

        public long getItemId(int arg0) {
            return arg0;
        }

        public View getView(int arg0, View arg1, ViewGroup arg2) {
            View updateInfoView = null;
            if (arg1 != null) {
                updateInfoView = arg1;
            } else {
                updateInfoView = LayoutInflater.from(mContext).inflate(R.layout.update_item, null);
            }
            UpdateInfo updateInfo = (UpdateInfo) getItem(arg0);

            TextView updateNameText = (TextView) updateInfoView.findViewById(R.id.update_file);
            TextView descriptionText = (TextView) updateInfoView.findViewById(R.id.description);
            TextView fileSizeText = (TextView) updateInfoView.findViewById(R.id.size);

            updateNameText.setText(updateInfo.getFileName());
            descriptionText.setText(updateInfo.getName() + "-" + updateInfo.getVersion());
            fileSizeText.setText(UpdateUtil.formatSize(updateInfo.getSize()));

            updateInfoView.setTag(updateInfo);
            return updateInfoView;
        }

    }

    private void log(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
