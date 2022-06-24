/**
 * Copyright (c) 2012, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.update;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.ServiceManager;

//urovo add for SQ53Q
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.ConnectivityManager;
import android.widget.Toast;
//urovo add for SQ53Q end
import android.provider.Settings;

//urovo yansitian add begin 20220221
import android.os.SystemProperties;
//add end

public class UpdateViewActivity extends Activity implements OnClickListener ,OnCancelListener{
    private String TAG = "OSUpdate" + UpdateViewActivity.class.getSimpleName();
    private UpdateInfo updateInfo;
    private TextView nameView;
    private TextView detailView;
    private Button okBtn;
    private Button cancelBtn;
    private ProgressBar progressBar;
    private LinearLayout progressLayout;
    private TextView progressText;
    boolean AUTO_DOWNLOAD = false;
    private boolean mForceUpdate = false;
    private int mBatteryLevel = 0;
    private boolean mBatteryCharging = false;
    private TextView mBatteryWarning;
    private boolean mIsUTE = false;
    private int mBatteryWarningLevel = 30;
    private AlertDialog.Builder mBuilder;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (DownloadManager.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                updateOkText();
            } else if (DownloadManager.ACTION_PROGRESS_CHANGED.equals(intent.getAction())) {
                int progress = intent.getIntExtra(Intent.EXTRA_INTENT, 0);
                progressBar.setIndeterminate(false);
                progressBar.setProgress(progress);
                progressText.setText(progress + "%");
            }else if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
            	int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            	mBatteryCharging  =  (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
            	mBatteryLevel = intent.getIntExtra("level", 0);
            	if(mBatteryCharging){
            		
                }else{
                    if(mBatteryLevel >= mBatteryWarningLevel){
                        mBatteryCharging = true;
                        mBatteryWarning.setText(R.string.battery_notcharging);
                    }else{
                        mBatteryWarning.setText(R.string.battery_low_notcharging);
                    }
                }
                updateOkText();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_info);
        mIsUTE = Build.PWV_CUSTOM_CUSTOM.startsWith("UTE");
        updateInfo = (UpdateInfo) getIntent().getSerializableExtra(Intent.EXTRA_INTENT);
        AUTO_DOWNLOAD = getIntent().getBooleanExtra("AUTO_DOWNLOAD", false);
        progressBar = (ProgressBar) findViewById(R.id.download_prog);
        progressLayout = (LinearLayout) findViewById(R.id.download_container);
        progressText = (TextView) findViewById(R.id.download_text);
        nameView = (TextView) findViewById(R.id.file_name);
        detailView = (TextView) findViewById(R.id.update_detail);
        okBtn = (Button) findViewById(R.id.ok_button);
        cancelBtn = (Button) findViewById(R.id.cancel_button);
        mBatteryWarning = (TextView) findViewById(R.id.battery_info);
        okBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        mBuilder = new AlertDialog.Builder(this);
	mBuilder.setOnCancelListener(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.title_dialog_alert))
                .setMessage(getString(R.string.wifi_or_mobiledata_notification))
                .setNeutralButton(android.R.string.cancel, new android.content.DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                finish();
                            }
                        })
                .setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                DownloadManager.getDefault(UpdateViewActivity.this).download(updateInfo);
                            }
                        });

        mForceUpdate = getIntent().getBooleanExtra("force", false);
        if (mForceUpdate) {
	    UpdateUtil.handleForceupgradingView(false); //false 禁用
	    ForceUpdateManager.getInstance().setforceUpdate(true);
	    progressLayout.setVisibility(View.INVISIBLE);
	    cancelBtn.setText(getResources().getString(R.string.btn_later));
	}
        //if(mIsUTE){
        	mBatteryWarning.setVisibility(View.VISIBLE);
        //}
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadManager.ACTION_PROGRESS_CHANGED);
        filter.addAction(DownloadManager.ACTION_STATE_CHANGED);
        //if(mIsUTE){
        	filter.addAction(Intent.ACTION_BATTERY_CHANGED); 	
        //}
        registerReceiver(receiver, filter);

        try {
            String level = Settings.System.getString(UpdateViewActivity.this.getContentResolver(), "FotaBatteryWarningLevel");
            mBatteryWarningLevel = Integer.parseInt(level);
            Log.d(TAG, "level= " + level + " mBatteryWarningLevel= " + mBatteryWarningLevel);
            
        } catch(Exception e) {

        }
        if(mBatteryWarningLevel < 15 || mBatteryWarningLevel > 99) {
            mBatteryWarningLevel = 30;
        }
	//urovo yansitian add begin 20220221
	if(!android.text.TextUtils.isEmpty(SystemProperties.get("persist.sys.urv.set.fota.battery.warning.level", ""))){
	    mBatteryWarningLevel = SystemProperties.getInt("persist.sys.urv.set.fota.battery.warning.level", 30);
	}
	Log.d(TAG, "mBatteryWarningLevel= " + mBatteryWarningLevel);
	//add end

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateInfo = (UpdateInfo) intent.getSerializableExtra(Intent.EXTRA_INTENT);
        AUTO_DOWNLOAD = getIntent().getBooleanExtra("AUTO_DOWNLOAD", false);
        mForceUpdate = getIntent().getBooleanExtra("force", false);
        updateDetail();
        updateOkText();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDetail();
        updateOkText();
        Log.d(TAG, "onResume " + AUTO_DOWNLOAD);
        if(mForceUpdate){
            if (!DownloadManager.getDefault(this).isDownloading()) {
            	    Log.d(TAG, "force update: start download ");
                    DownloadManager.getDefault(this).download(updateInfo);
                }
        	DownloadManager.getDefault(this).setForceUpdate(true);
	        progressLayout.setVisibility(View.VISIBLE);
                okBtn.setEnabled(false);
                cancelBtn.setEnabled(false);
            return;
        }else{
        	DownloadManager.getDefault(this).setForceUpdate(false);
        }
        if(AUTO_DOWNLOAD) {
            if (!DownloadManager.getDefault(this).isDownloading()) {
                DownloadManager.getDefault(this).download(updateInfo);
            } else if (DownloadManager.getDefault(this).isDownloading(updateInfo)) {
                DownloadManager.getDefault(this).pause(updateInfo);
            }
        }
    }
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	//if(mForceUpdate){
    	//	return;
    	//}
    		
    	super.onBackPressed();
    }

    private void updateOkText() {
        if(mForceUpdate){
            return;
        }
        if (!DownloadManager.getDefault(this).isDownloading()) {
            okBtn.setText(R.string.btn_download);
            //if(mIsUTE && !mBatteryCharging){
            if(!mBatteryCharging){
            	okBtn.setEnabled(false);
            }else{
            	okBtn.setEnabled(true);
            }
            progressLayout.setVisibility(View.INVISIBLE);
        } else if (DownloadManager.getDefault(this).isDownloading(updateInfo)) {
            okBtn.setText(R.string.btn_pause);
            okBtn.setEnabled(true);
            progressLayout.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
        } else {
            okBtn.setText(R.string.btn_download);
            okBtn.setEnabled(false);
            progressLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void updateDetail() {
        if(getUpdateInfoObjectInfo()){
           Log.d(TAG, "updateInfo is null ");
           Intent intent = new Intent(this,RemoteActivity.class);
           startActivity(intent);
           finish();
           return;
        }
        nameView.setText(updateInfo.getFileName());
        detailView.setText(getString(R.string.info_size,
                UpdateUtil.formatSize(updateInfo.getSize()))
                + "\n"
                + getString(R.string.info_version, updateInfo.getVersion())
                + "\n\n"
                + (updateInfo.getDelta() != null ? (getString(R.string.info_delta,
                        String.valueOf(updateInfo.getDelta().from),
                        String.valueOf(updateInfo.getDelta().to)) + "\n") : ""));
                //+ updateInfo.getDescription());
    }
    private String isWifiOrInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();
        boolean internet = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        if (wifi) {
	    return "wifi";
	} else if (internet) {
	    return "internet";
	} else {
	    return null;
	}
    }

    public void onCancel(DialogInterface dialog) {
        Log.d(TAG, "onCancel .setOnCancelListener");
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == cancelBtn) {
	    if (mForceUpdate) {
	        //UpdateSettings.scheduleNextCheck(UpdateViewActivity.this);
                UpdateUtil.handleForceupgradingView(true);
                ForceUpdateManager.getInstance().setforceUpdate(false);
                finish();
	    } else {
                DownloadManager.getDefault(this).cancel(updateInfo);//add by urovo weiyu on 2019-11-05 
                finish();
	    }
        } else if (v == okBtn) {
	    if (isWifiOrInternet() == null) {
		Toast.makeText(this, R.string.msg_no_network,
                                Toast.LENGTH_SHORT).show();
	    } else if (mForceUpdate) {
                cancelBtn.setEnabled(false);
                progressLayout.setVisibility(View.VISIBLE);
                DownloadManager.getDefault(this).setForceUpdate(true);
                if (!DownloadManager.getDefault(this).isDownloading()) {
                    Log.d(TAG, "force update: start download ");
                    DownloadManager.getDefault(this).download(updateInfo);
                } else if (DownloadManager.getDefault(this).isDownloading(updateInfo)) {
                    DownloadManager.getDefault(this).pause(updateInfo);
                }
                return;
            } else if (!DownloadManager.getDefault(this).isDownloading()) {
		if (isWifiOrInternet().equals("internet")) {
                    mBuilder.show();
		} else {
		    DownloadManager.getDefault(this).download(updateInfo);
		}
            } else if (DownloadManager.getDefault(this).isDownloading(updateInfo)) {
                DownloadManager.getDefault(this).pause(updateInfo);
            }
        }
    }

    private boolean getUpdateInfoObjectInfo(){
        if(updateInfo == null){
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

}
