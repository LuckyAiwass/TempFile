/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.system;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.app.Activity;
import android.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.ActionBar;
import android.app.Fragment;
import android.util.Log;
import com.android.settings.R;
import android.content.Intent;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.content.Context;
import android.provider.Settings;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.os.SystemProperties;//add by chenchuanliang for UFS update 20200711
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.os.UpdateEngine;
import android.os.UpdateEngineCallback;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import android.widget.LinearLayout;
import com.android.internal.util.Preconditions;

import java.io.File;
import java.io.IOException;

/**
 * Apply a system update using an ota package on internal or external storage.
 */
public class SystemUpdaterActivity extends Activity
        /*implements DeviceListFragment.SystemUpdater */{

    private static final String FRAGMENT_TAG = "FRAGMENT_TAG";
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0;
    private static final String[] REQUIRED_STORAGE_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
	public static final String EXTRA_RESUME_UPDATE = "resume_update";

    private static final String TAG = "SystemUpdaterActivity";
    private static final String EXTRA_UPDATE_FILE = "extra_update_file";
    private static final int PERCENT_MAX = 100;
    private static final String REBOOT_REASON = "reboot-ab-update";
    private static final String NOTIFICATION_CHANNEL_ID = "update";
    private static final int NOTIFICATION_ID = 1;
	private ProgressBar mProgressBar;
	private LinearLayout mPrgressLayout;
	private TextView mProgressPercent;
    private TextView mContentTitle;
    private TextView mContentInfo;
    private TextView mContentDetails;
    private File mUpdateFile;
    private Button mSystemUpdateToolbarAction;
    private PowerManager mPowerManager;
    private NotificationManager mNotificationManager;
    private final UpdateVerifier mPackageVerifier = new UpdateVerifier();
    private final UpdateEngine mUpdateEngine = new UpdateEngine();
    private boolean mInstallationInProgress = false;
	private int updateState = 1;
	private int rebootState = 0;
	private boolean installOn = false;
	private final CarUpdateEngineCallback mCarUpdateEngineCallback = new CarUpdateEngineCallback();
	//add by chenchuanliang for UFS update 20200711
	private int mPackageStatus = 0;
	private int mUFSPercent = 0;
	private int mUFSUpdateCount = 0;
	private final int UFS_UPDATE_CHECK_MAX = 30;
	Handler mHandler = new Handler();
	private final Runnable mUFSUpdateProgress = new Runnable() {
			@Override
			public void run() {
				int ufsstatus = SystemProperties.getInt("persist.sys.ufsupdate_status",9);
				if(ufsstatus != 9 || mUFSUpdateCount == UFS_UPDATE_CHECK_MAX)
				{
					showUFSStatus((ufsstatus == 1 || mUFSUpdateCount == UFS_UPDATE_CHECK_MAX) ? R.string.install_success : R.string.install_failed) ;
					
					if(ufsstatus == 1 || mUFSUpdateCount == UFS_UPDATE_CHECK_MAX)
						mHandler.postDelayed(mReboot,1000);
					mUFSPercent = 0;
					mUFSUpdateCount = 0;
					return;
				}
				if(mUFSPercent < 100)
					mUFSPercent = mUFSPercent + 10;
				else
					mUFSUpdateCount++;
				mProgressBar.setProgress((int) (mUFSPercent));
				mProgressPercent.setText((int) (mUFSPercent) + "%");
				mHandler.postDelayed(mUFSUpdateProgress,500);
			}
		};
	private final Runnable mReboot = new Runnable() {
			@Override
			public void run() {
				Settings.Global.putInt(getContentResolver(), "local_update_state",1);
                rebootNow();
			}
		};
	//add end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, REQUIRED_STORAGE_PERMISSIONS,
                    STORAGE_PERMISSIONS_REQUEST_CODE);
        }

        setContentView(R.layout.activity_main);
        
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mNotificationManager =
                (NotificationManager) getSystemService(NotificationManager.class);
        mNotificationManager.createNotificationChannel(
                new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        getString(R.id.system_update_auto_content_title),
                        NotificationManager.IMPORTANCE_DEFAULT));

        if (savedInstanceState == null) {
			String sSdDirectory="";
             int VOLUME_SDCARD_INDEX = 1;
             try {
               StorageManager mStorageManager =
                                (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
                    final StorageVolume[] volumes = mStorageManager.getVolumeList();
                         if (volumes.length > VOLUME_SDCARD_INDEX) {
                             StorageVolume volume = volumes[VOLUME_SDCARD_INDEX];
                             if (volume.isRemovable()) {
                                 sSdDirectory = volume.getPath();
                             }
                         }
                     } catch (Exception e) {
                         Log.e("SystemUpdater", "couldn't talk to MountService", e);
                    }                             
            String UPDATE_FILE = sSdDirectory + "/update.zip";
            File updatefile = new File(UPDATE_FILE);
			
            Bundle intentExtras = getIntent().getExtras();
			updateState = Settings.Global.getInt(getContentResolver(),"local_update_state", 1);
            Log.d("SystemUpdate","updateState ="+updateState);
			initView();
			if (updatefile.exists()) {
				mUpdateFile = new File(updatefile.getAbsolutePath());
            }
			//add by chenchuanliang for UFS update 20200711
			try {
                mPackageStatus = UpdateParser.getPackageStatus(mUpdateFile);
            } catch (IOException e) {
            	mPackageStatus = UpdateParser.ONLY_AB_PACKAGE;
                Log.e(TAG, "OTA package parser error : ", e);
            }
			Log.d("SystemUpdate","mPackageStatus ="+mPackageStatus);
			/*if(mPackageStatus == UpdateParser.ONLY_UFS_PACKAGE)
			{
				SystemProperties.set("persist.sys.ufsupdate","1");
				mInstallationInProgress = false;
				Settings.Global.putInt(getContentResolver(), "local_update_state",1);
				showStatus(R.string.install_success);
            	mPrgressLayout.setVisibility(View.GONE);
            	mSystemUpdateToolbarAction.setVisibility(View.GONE);
				return;
			}*/
			//add end
			if (updateState == 0) {
                // Rejoin the update already in progress.
                showInstallationInProgress();
            } else {
            // Extract the necessary information and begin the update.
               mPackageVerifier.execute(mUpdateFile);
           }		
        }
    }
	 private void initView() {
        mContentTitle = (TextView)findViewById(R.id.system_update_auto_content_title);
        mContentInfo = (TextView)findViewById(R.id.system_update_auto_content_info);
        mContentDetails = (TextView)findViewById(R.id.system_update_auto_content_details);
        mPrgressLayout = (LinearLayout)findViewById(R.id.progress_bar_layout);
		mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
		mProgressPercent = (TextView)findViewById(R.id.progress_percent);
		mProgressPercent.setText("0%");

        mSystemUpdateToolbarAction = (Button)findViewById(R.id.action_button1);
        mProgressBar.setIndeterminate(true);
    }
   @Override
    public void onResume() {
           super.onResume();

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
            int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (STORAGE_PERMISSIONS_REQUEST_CODE == requestCode) {
            if (grantResults.length == 0) {
                finish();
            }
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
            }
        }
    }
	@Override
    public void onDestroy() {//songtingting modify automatic upgrade 20200620
        super.onStop();
        if (mPackageVerifier != null) {
            mPackageVerifier.cancel(true);
        }
    }
	/** Set the layout to show installation progress. */
    private void showInstallationInProgress() {
        mProgressBar.setIndeterminate(false);
        mPrgressLayout.setVisibility(View.VISIBLE);
        mProgressBar.setMax(PERCENT_MAX);
        mSystemUpdateToolbarAction.setVisibility(View.GONE);
        Log.i(TAG, "showInstallationInProgress");
		if(updateState == 0 && !mInstallationInProgress){//songtingting modify update info 20200611
			mContentTitle.setText(R.string.install_in_progress);
			mContentInfo.append(getString(R.string.update_file_name, mUpdateFile.getName()));
            mContentInfo.append(System.getProperty("line.separator"));
            mContentInfo.append(getString(R.string.update_file_size));
            mContentInfo.append(Formatter.formatFileSize(this, mUpdateFile.length()));
			}
		  mInstallationInProgress = true;
		if(mPackageStatus == UpdateParser.ONLY_UFS_PACKAGE)
		{
			mHandler.postDelayed(mUFSUpdateProgress,500);
		}
		else
        	mUpdateEngine.bind(mCarUpdateEngineCallback, new Handler(getMainLooper()));
    }
	/** Update the status information. */
    private void showStatus(@StringRes int status) {
        mContentTitle.setText(status);
		Log.d(TAG,"mInstallationInProgress = "+mInstallationInProgress);
		
        if (mInstallationInProgress) {
            mNotificationManager.notify(NOTIFICATION_ID, createNotification(this, status));
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }
	//add by chenchuanliang for UFS update 20200711
	private void showUFSStatus(@StringRes int status) {
		mInstallationInProgress = false;
		Settings.Global.putInt(getContentResolver(), "local_update_state",1);
		showStatus(status);
    	mPrgressLayout.setVisibility(View.GONE);
    	mSystemUpdateToolbarAction.setVisibility(View.GONE);
    }
	//add end
	/** Show the install now button. */
    private void showInstallNow(UpdateParser.ParsedUpdate update) {
        mContentTitle.setText(R.string.install_ready);
        mContentInfo.append(getString(R.string.update_file_name, mUpdateFile.getName()));
        mContentInfo.append(System.getProperty("line.separator"));
        mContentInfo.append(getString(R.string.update_file_size));
        mContentInfo.append(Formatter.formatFileSize(this, mUpdateFile.length()));
        mContentDetails.setText(null);
        mSystemUpdateToolbarAction.setOnClickListener(v -> showInstallDialog(update));
        mSystemUpdateToolbarAction.setText(R.string.install_now);
        mSystemUpdateToolbarAction.setVisibility(View.VISIBLE);
    }
	private void showInstallDialog(UpdateParser.ParsedUpdate update) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                         .setTitle(R.string.recovery_entry_title)
                         .setIconAttribute(android.R.attr.alertDialogIcon)
                         .setMessage(R.string.install_update_warning)
                         .setCancelable(true)
                         .setPositiveButton(android.R.string.ok,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
										  installUpdate(update);
										  dialog.cancel();
                          }
                          })
                          .setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                          dialog.cancel();
                          }
                          })
                          .create(); 
                          dialog.show(); 
    }

    /** Reboot the system. */
    private void rebootNow() {
       // if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "Rebooting Now.");
       // }
        installOn = false;
        mPowerManager.reboot(REBOOT_REASON);
    }

    /** Attempt to install the update that is copied to the device. */
    private void installUpdate(UpdateParser.ParsedUpdate parsedUpdate) {
     // Log.i(TAG, "installUpdate");
	    installOn = true;
	    Settings.Global.putInt(getContentResolver(),"local_update_state",0);
        showInstallationInProgress();
		showStatus(R.string.install_in_progress);
		//add by chenchuanliang for UFS update 20200711
		if(mPackageStatus == UpdateParser.AB_UFS_PACKAGE || mPackageStatus == UpdateParser.ONLY_UFS_PACKAGE)
			SystemProperties.set("persist.sys.ufsupdate","1");
		if(mPackageStatus == UpdateParser.AB_UFS_PACKAGE || mPackageStatus == UpdateParser.ONLY_AB_PACKAGE)
		//add end
        	mUpdateEngine.applyPayload(
                	parsedUpdate.mUrl, parsedUpdate.mOffset, parsedUpdate.mSize, parsedUpdate.mProps);
    }
	/** Attempt to verify the update and extract information needed for installation. */
    private class UpdateVerifier extends AsyncTask<File, Void, UpdateParser.ParsedUpdate> {

        @Override
        protected UpdateParser.ParsedUpdate doInBackground(File... files) {
            Preconditions.checkArgument(files.length > 0, "No file specified");
            File file = files[0];
            try {
                return UpdateParser.parse(file);
            } catch (IOException e) {
                Log.e(TAG, String.format("For file %s", file), e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(UpdateParser.ParsedUpdate result) {
            mPrgressLayout.setVisibility(View.GONE);
			Log.e(TAG, "onPostExecute  rebootState = "+rebootState);
			//add UFS check by chenchuanliang 20200717 
            if (result == null && mPackageStatus != UpdateParser.ONLY_UFS_PACKAGE) {
                showStatus(R.string.verify_failure);
                return;
            }
			//add UFS check by chenchuanliang 20200717 
            if (!result.isValid() && mPackageStatus != UpdateParser.ONLY_UFS_PACKAGE) {
                showStatus(R.string.verify_failure);
                Log.e(TAG, String.format("Failed verification %s", result));
                return;
            }
            if (Log.isLoggable(TAG, Log.INFO)) {
                Log.i(TAG, result.toString());
            }
            if(rebootState == 0 && updateState ==0){
                installUpdate(result);
			}else{   
				showInstallNow(result);
			}
        }
    }

    /** Handles events from the UpdateEngine. */
    public class CarUpdateEngineCallback extends UpdateEngineCallback {

        @Override
        public void onStatusUpdate(int status, float percent) {
            Log.d(TAG,"rebootNow status = "+status);
			rebootState = status;
            switch (status) {
				case UpdateEngine.UpdateStatusConstants.IDLE:
                    updateState = Settings.Global.getInt(getContentResolver(),"local_update_state", 1);
					if(updateState == 0 && !installOn){
                        mPackageVerifier.execute(mUpdateFile);
					}
                    break;
                case UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT:
                    Settings.Global.putInt(getContentResolver(), "local_update_state",1);
                    rebootNow();
                    break;
                case UpdateEngine.UpdateStatusConstants.DOWNLOADING:
                    mProgressBar.setProgress((int) (percent * 100));
					mProgressPercent.setText((int) (percent * 100) + "%");
                    break;
                default:
                    // noop
            }
        }

        @Override
        public void onPayloadApplicationComplete(int errorCode) {
            Log.w(TAG, String.format("onPayloadApplicationComplete %d", errorCode));
            mInstallationInProgress = false;
			Settings.Global.putInt(getContentResolver(), "local_update_state",1);
            showStatus(errorCode == UpdateEngine.ErrorCodeConstants.SUCCESS
                    ? R.string.install_success
                    : R.string.install_failed);
            mPrgressLayout.setVisibility(View.GONE);
            mSystemUpdateToolbarAction.setVisibility(View.GONE);
        }
    }
	/** Build a notification to show the installation status. */
    private static Notification createNotification(Context context, @StringRes int contents) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context, SystemUpdaterActivity.class));
        intent.putExtra(EXTRA_RESUME_UPDATE, true);
		Log.d(TAG,"createNotification");
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        /* requestCode= */ 0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentTitle(context.getString(contents))
                .setSmallIcon(R.drawable.ic_system_update_alt_black_48dp)
                .setContentIntent(pendingIntent)
                .setShowWhen(false)
                .setOngoing(true)
                .setAutoCancel(false)
                .build();
    }
}
