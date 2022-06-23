package com.ubx.appinstall.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.RemoteViews;

import com.ubx.appinstall.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.net.Uri;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.Display;
import android.view.KeyEvent;
import android.util.DisplayMetrics;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.widget.ProgressBar;
import android.os.PowerManager;
import android.view.LayoutInflater;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.WindowManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageInstaller;
import com.android.internal.content.PackageHelper;
import com.ubx.appinstall.activity.MainActivity;
import com.ubx.appinstall.util.AutoInstallConfig;
import com.ubx.appinstall.util.AutoInstallUtil;
import com.ubx.appinstall.util.IoUtils;
import com.ubx.appinstall.util.SharedPrefsStrListUtil;
import com.ubx.appinstall.util.ULog;

import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PackageUserState;
import android.content.pm.ResolveInfo;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.IPackageInstallObserver;

public class AutoInstallService extends IntentService {

	private static final String BROADCAST_ACTION = "com.android.packageinstaller.ACTION_INSTALL_COMMIT";
	private static final String BROADCAST_SENDER_PERMISSION = "android.permission.INSTALL_PACKAGES";
	private PowerManager mPowerManager = null;
	private PowerManager.WakeLock mWakeLock = null;
	private NotificationManager mNotificationManager;
	private RemoteViews mRemoteViews;
	private Intent mIntent;
	private PendingIntent mPendingIntent;
	private Notification mNotification = null;
	private View installView = null;
	private Dialog installDialog = null;
	private static int mProgress = 0;
	private static final int INSTALLPROGRESS = 1;

	private Uri mPackageURI = null;
	private static final String SCHEME_FILE = "file";
	private PackageInfo mPkgInfo;
	private Map<File, Long> map = new HashMap<File, Long>();
	private List<File> installApkList = new ArrayList<>();
	private int count = -1;
	private boolean complete;
	private boolean StartWithPath = false;

	private boolean runback = false;
	private List<String> pmList = new ArrayList<>();
	private Handler mInstallHandler = new MyHandler(AutoInstallService.this);
	private Handler mBootHandler = new BootHandler(AutoInstallService.this);

	private boolean powerboot = false;
	private int i = 0;

	private static class MyHandler extends Handler {
		private WeakReference<AutoInstallService> mReference = null;

		public MyHandler(AutoInstallService context) {
			mReference = new WeakReference<AutoInstallService>(context);
		}

		@Override
		public void handleMessage(Message msg) {
			AutoInstallService context = mReference.get();
			ULog.d("handleMessage , msg.what:" + msg.what);
			if (context != null) {
				if (mProgress != msg.what) {
					mProgress = msg.what;
				}
				int current = (int) ((context.count / (float) context.installApkList.size()) * 100);
				if (context.runback && context.mRemoteViews != null) {
					context.mRemoteViews.setTextViewText(R.id.install_notification_progress_text, "" + current + "%");
					context.mRemoteViews.setProgressBar(R.id.install_notification_progress_bar, 100, current, false);
					context.notifyInstallNotification();
				}
				if (!context.runback && context.installView != null) {
					// ((ProgressBar)
					// context.installView.findViewById(R.id.one_copy_percent)).setProgress(mProgress);
					((TextView) context.installView.findViewById(R.id.target_Text))
							.setText((String) msg.obj == null ? "" : (String) msg.obj);
					((ProgressBar) context.installView.findViewById(R.id.one_copy_percent)).setProgress(current);
					((ProgressBar) context.installView.findViewById(R.id.all_copy_percent)).setProgress(mProgress);
					((TextView) context.installView.findViewById(R.id.source_Text)).setText(current + " %");
				}
				if (current >= 100 && !context.complete) {
					context.complete = true;
					// context.cancelInstallNotification();
					if (context.runback) {
						context.cancelInstallNotification();
					} else {
						context.installAlerDialogDismiss();
					}
					context.showToast("Install complete");
					
					if (context.powerboot) {
						ULog.d("powerboot --------------------->" + context.powerboot);
						context.startApp();
						context.powerboot = false;
					}
				}
			}
		}
	}

	public static class BootHandler extends Handler {
		private WeakReference<AutoInstallService> mReference = null;

		public BootHandler(AutoInstallService context) {
			mReference = new WeakReference<AutoInstallService>(context);
		}

		@Override
		public void handleMessage(Message msg) {
			AutoInstallService context = mReference.get();
			if (context != null) {
				ULog.d("      msg.ob   ----------------->  " +(String) msg.obj );
				context.startApplication((String) msg.obj);
				//context.doStartApplicationWithPackageName((String) msg.obj);
			}
		}
	}

	public AutoInstallService() {
		super("AutoInstallService");
		// TODO Auto-generated constructor stub
		ULog.d("AutoInstallService");
		AutoInstallUtil.checkAndCreateAutoInstallPath();
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		pmList = SharedPrefsStrListUtil.getStrListValue(this, "POWERBOOT");
		ULog.d("onCreate , isExistsAutoInstallPath:" + AutoInstallUtil.isExistsAutoInstallPath());
		if (AutoInstallUtil.isExistsAutoInstallPath()) {
			AutoInstallUtil.checkAndCreateAutoInstallPath();
		}
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
		try {
			mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, AutoInstallConfig.TAG);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void lockInstall() {
		if (mWakeLock != null) {
			try {
				if (!mWakeLock.isHeld()) {
					mWakeLock.acquire();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void unLockInstall() {
		if (mWakeLock != null) {
			try {
				if (mWakeLock.isHeld()) {
					mWakeLock.release();
					mWakeLock.setReferenceCounted(false);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void notifyInstallNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.notify(0, mNotification);
		}
	}

	private Notification createInstallProgressNotification(String title) {
		// mRemoteViews = new RemoteViews(getPackageName(),
		// R.layout.notification);
		mRemoteViews = new RemoteViews(getPackageName(), R.layout.install_notification);
		mRemoteViews.setTextViewText(R.id.install_notification_title, title);
		mRemoteViews.setTextViewText(R.id.install_notification_progress_text, "");
		mRemoteViews.setProgressBar(R.id.install_notification_progress_bar, 100, 0, true);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), 0);
		final Notification notification = new Notification();
		notification.icon = R.drawable.icon;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.contentView = mRemoteViews;
		notification.contentIntent = contentIntent;
		Intent statusintent = new Intent(this, MainActivity.class);
		notification.contentIntent = PendingIntent.getActivity(this, 0, statusintent, 0);
		return notification;
	}

	private void installAlertDialog() {
		// AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// installView =
		// LayoutInflater.from(this).inflate(R.layout.install_dialog, null,
		// false);
		//
		// builder.setView(installView);
		//
		// installDialog = builder.create();
		// AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// installDialog = builder.create();
		installDialog = new Dialog(this, R.style.dialog);
		LayoutInflater inflater = LayoutInflater.from(this);
		installView = inflater.inflate(R.layout.install_dialog, null);
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		// 设置dialog的宽高为屏幕的宽高
		ULog.d("width:   " + width + "     height:   " + height);
		ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, height);
		// installDialog.setContentView(installView, layoutParams);
		installDialog.addContentView(installView, layoutParams);
		// installDialog.getWindow().

		installDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		installDialog.setCanceledOnTouchOutside(false);
		installDialog.setOnDismissListener(mOnDismissListener);
		installDialog.setOnKeyListener(mOnKeyListener);
		((Button) installView.findViewById(R.id.but_stop_install)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				ULog.d("installDialog");
				unLockInstall();
				installAlerDialogDismiss();
			}
		});
		((Button) installView.findViewById(R.id.but_run_back)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				ULog.d("installDialog");
				runback = true;
				mNotification = createInstallProgressNotification("Installing");
				notifyInstallNotification();
				installAlerDialogDismiss();
			}
		});

		try {

			installDialog.show();

		} catch (Exception e) {
			e.printStackTrace();
			unLockInstall();
		}
	}

	private void installAlerDialogDismiss() {
		if (installDialog != null && installDialog.isShowing()) {
			installDialog.dismiss();
		}
	}

	OnKeyListener mOnKeyListener = new OnKeyListener() {
		@Override
		public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			ULog.d("mOnKeyListener onKey keyCode:" + keyCode);
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				dialog.dismiss();
				break;
			}
			return false;
		}
	};

	OnDismissListener mOnDismissListener = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface dialog) {
			ULog.d("mOnDismissListener");
			unLockInstall();
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		ULog.d("onStartCommand");
		// mNotification = createInstallProgressNotification("Installing");
		// notifyInstallNotification();
		powerboot = intent.getBooleanExtra("bootStart", false);
		installAlertDialog();
		return super.onStartCommand(intent, flags, startId);
	}

	public boolean autoInstallApps() {
		if (AutoInstallUtil.isExistsAutoInstallPath()) {
			File file = new File(AutoInstallConfig.AUTOINSTALLPATH);
			ULog.i("(AutoInstallConfig.AUTOINSTALLPATH)            " + (AutoInstallConfig.AUTOINSTALLPATH));
			if (file != null && file.exists()) {
				installApps(file);
			} else {
				ULog.i("autoInstallApps , error!auto install path " + AutoInstallConfig.AUTOINSTALLPATH
						+ " is not exists!");
			}
		} else {
			ULog.i("autoInstallApps , error!!! auto install path " + AutoInstallConfig.AUTOINSTALLPATH
					+ " is not exists!");
		}
		return false;
	}

	private PackageInfo getApkInfo(String apkPath) {
		return getPackageManager().getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);

	}

	private boolean isInstalled(String pkgName, int version) {
		boolean hasInstalled = false;
		List<PackageInfo> pinfo = this.getPackageManager().getInstalledPackages(0);
		if (pinfo != null) {
			for (int i = 0; i < pinfo.size(); i++) {
				String pn = pinfo.get(i).packageName.toLowerCase();
				int ver = pinfo.get(i).versionCode;
				
				if (pn.equals(pkgName.toLowerCase()) && ver >= version) {
					ULog.d("pkgName:  " + pkgName + "     version : " + version + " 	pn : " + pn
							+ "			ver:		" + ver);
					hasInstalled = true;
					break;
				}else{
					ULog.d("____>   pkgName:  " + pkgName + "	  version : " + version + " 	pn : " + pn
												+ " 		ver:		" + ver);

				}
			}
		}
		for (File f : installApkList) {
			if (getApkInfo(f.getAbsolutePath()).packageName.equals(pkgName)) {
				hasInstalled = true;
			}
		}
		return hasInstalled;
	}

	private void checkAppsAndSize(File file) {
		if (file == null) {
			ULog.d("checkAppsAndSize error!!! file:" + file);
		}
		File[] files = file.listFiles();
		if (files != null && files.length > 0) {
			for (File f : files) {
				if (f != null && f.exists()) {
					if (f.isDirectory()) {
						checkAppsAndSize(f);
					} else if (f.isFile() && f.getAbsolutePath().toLowerCase().endsWith(".apk")) {
						String apkName = getApkInfo(f.getAbsolutePath()).packageName;
						int apkVersion = getApkInfo(f.getAbsolutePath()).versionCode;
						if (!isInstalled(apkName, apkVersion)) {
							installApkList.add(f);
							// map.put(f,f.length());
						}
					} else {
						ULog.i("checkAppsAndSize continue other , file is " + (f != null ? f : null));
					}
				} else {
					ULog.i("checkAppsAndSize continue file is exists?" + (f != null ? f : null));
				}
			}
		}
	}

	public boolean installApps(File file) {
		if (file == null) {
			ULog.d("installApps error!!! file:" + file);
			return false;
		}
		complete = false;
		installApkList.clear();
		if (!StartWithPath) {
			List<String> addFile = SharedPrefsStrListUtil.getStrListValue(getApplicationContext(), "ADD_DIR");
			addFile.add(AutoInstallConfig.AUTOINSTALLPATH);
			for (String path : addFile) {
				File addfile = new File(path);
				if (addfile != null && addfile.exists()) {
					checkAppsAndSize(addfile);
				}
			}
		} else {
			// anotherapp start
			checkAppsAndSize(file);
		}
		ULog.d("installApkList.size()      " + "" + installApkList.size());

		if (installApkList.size() == 0) {
			ULog.i("installApps , no apk to install");
			showToast("No package to install!");
			complete = true;
			
			if (powerboot) {
				ULog.d("powerboot --------------------->" + powerboot);
				startApp();
				powerboot = false;
			}else
				installAlerDialogDismiss();
		} else {
			updateUI();
			for (File f : installApkList) {
				if (f != null && f.exists()) {
					installAPK(f);
				} else {
					ULog.i("installApps , file not exists! file:" + f);
				}
			}
		}
		return false;
	}

	private boolean installAPK(File apkFile) {
		if (apkFile == null) {
			ULog.i("installAPK error!!! apk:" + apkFile);
			return false;
		}
		if (!apkFile.exists()) {
			ULog.i("installAPK error!!! " + apkFile + " is not exists!");
			return false;
		}
		PackageInstallObserver installObserver = new PackageInstallObserver();
		Uri packageURI = Uri.parse("file://" + apkFile.getPath());
		int installFlags = 0;
		PackageManager pm = this.getPackageManager();
		PackageParser.Package pkgInfo = getPackageInfo(packageURI);
		if (pkgInfo == null) {
			ULog.i("installAPK error!!! parse AndroidManifest.xml error for " + packageURI + " !");
			return false;
		}
		// installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
		// pm.installPackage(packageURI, installObserver, installFlags, null);

		doPackageStage(pm, apkFile);
		return true;
	}

	private PackageParser.Package getPackageInfo(Uri packageURI) {
		final String archiveFilePath = packageURI.getPath();
		PackageParser packageParser = new PackageParser();
		File sourceFile = new File(archiveFilePath);
		DisplayMetrics metrics = new DisplayMetrics();
		metrics.setToDefaults();
		try {
			return packageParser.parsePackage(sourceFile, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	class PackageInstallObserver extends IPackageInstallObserver.Stub {
		public void packageInstalled(String packageName, int returnCode) {
			//
			// Message msg = mHandler.obtainMessage(INSTALL_COMPLETE);
			// msg.arg1 = returnCode;
			// mHandler.sendMessage(msg);
		}
	};

	private void doPackageStage(PackageManager pm,
			File file /* , PackageInstaller.SessionParams params */) {
		ULog.d("doPackageStage , file:" + file);
		final PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
				PackageInstaller.SessionParams.MODE_FULL_INSTALL);
		try {
			PackageLite pkg = PackageParser.parsePackageLite(file, 0);
			ULog.d("doPackageStage , pkg:" + pkg + ",pkg.packageName:" + pkg.packageName + ",params.abiOverride:"
					+ params.abiOverride + ",pkg.installLocation:" + pkg.installLocation);
			params.setAppPackageName(pkg.packageName);
			params.setInstallLocation(pkg.installLocation);
			params.setSize(PackageHelper.calculateInstalledSize(pkg, false, params.abiOverride));
		} catch (PackageParser.PackageParserException e) {
			params.setSize(file.length());
		} catch (IOException e) {
			params.setSize(file.length());
		}
		final PackageInstaller packageInstaller = pm.getPackageInstaller();
		PackageInstaller.Session session = null;
		try {
			final int sessionId = packageInstaller.createSession(params);
			final byte[] buffer = new byte[65536];

			ULog.d("doPackageStage , sessionId:" + sessionId);
			session = packageInstaller.openSession(sessionId);

			final InputStream in = new FileInputStream(file);
			final long sizeBytes = file.length();
			ULog.d("doPackageStage , sizeBytes:" + sizeBytes);
			final OutputStream out = session.openWrite("PackageInstaller", 0, sizeBytes);
			try {
				int c;
				float percent = 0f;
				while ((c = in.read(buffer)) != -1) {

					// ULog.d("doPackageStage , c:" + c);
					out.write(buffer, 0, c);
					if (sizeBytes > 0) {
						final float fraction = ((float) c / (float) sizeBytes);
						session.addProgress(fraction);

						Message msg = mInstallHandler.obtainMessage(INSTALLPROGRESS);
						percent += c;
						mProgress = (int) ((percent / (float) sizeBytes) * 100);
						if (mProgress > 100) {
							mProgress = 100;
						}
						msg.what = mProgress;
						msg.obj = installApkList.get(count).getName();
						ULog.d("onHandleIntent mProgress:" + mProgress + ",fraction:" + fraction + ",sizeBytes:"
								+ sizeBytes);
						mInstallHandler.sendMessage(msg);
					}
				}
				session.fsync(out);
			} finally {
				IoUtils.closeQuietly(in);
				IoUtils.closeQuietly(out);
			}
			// Create a PendingIntent and use it to generate the IntentSender
			Intent broadcastIntent = new Intent(BROADCAST_ACTION);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(this /* context */, sessionId, broadcastIntent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			session.commit(pendingIntent.getIntentSender());
			// cancelInstallNotification();
			// installAlerDialogDismiss();
			updateUI();
		} catch (IOException e) {
			onPackageInstalled(PackageInstaller.STATUS_FAILURE);
			ULog.d("doPackageStage , IOException");
		} finally {
			IoUtils.closeQuietly(session);
		}
	}

	private void updateUI() {
		count++;
		Message msg = mInstallHandler.obtainMessage(INSTALLPROGRESS);
		// mProgress = (int) ((count / (float) installApkList.size()) * 100);
		// if (mProgress > 100) {
		// mProgress = 100;
		// }
		mProgress = 0;
		msg.what = mProgress;
		if (count < installApkList.size()) {
			msg.obj = installApkList.get(count).getName();
			ULog.d("Next  doPackageStage ,_____________________ file:" + installApkList.get(count).getName());
		}
		mInstallHandler.sendMessage(msg);
	}

	void onPackageInstalled(int statusCode) {
	}

	private boolean processPackageUri(File file) {
		if (file == null) {
			ULog.d("processPackageUri , file:" + file);
			return false;
		}
		final Uri packageUri = Uri.fromFile(file);
		mPackageURI = packageUri;
		// final String scheme = packageUri.getScheme();
		// final PackageUtil.AppSnippet as;
		// if(SCHEME_FILE.equals(scheme)) {
		// File sourceFile = new File(packageUri.getPath());
		PackageParser.Package parsed = getPackageInfo(packageUri);
		if (parsed == null) {
			ULog.i("Parse error when parsing manifest.");
			return false;
		}
		mPkgInfo = PackageParser.generatePackageInfo(parsed, null, PackageManager.GET_PERMISSIONS, 0, 0, null,
				new PackageUserState());
		// as = PackageUtil.getAppSnippet(this, mPkgInfo.applicationInfo,
		// sourceFile);
		// PackageUtil.initSnippetForNewApp(this, as, R.id.app_snippet);
		return true;
		// }
		// return false;
	}

	@Override
	protected void onHandleIntent(Intent arg0) {
		// TODO Auto-generated method stub
		ULog.d("onHandleIntent");
		/*
		 * for(int i=0;i<=100;i+=10) { try { Thread.sleep(1000); }
		 * catch(Exception e) { } Message msg =
		 * mInstallHandler.obtainMessage(INSTALLPROGRESS); msg.what = i;
		 * mProgress = i; ULog.d("onHandleIntent i:" + i);
		 * mInstallHandler.sendMessage(msg); }
		 */
		String path = arg0.getStringExtra("path");
		if (null != path) {
			if (path.equals("")) {
				path = AutoInstallConfig.AUTOINSTALLPATH;
			}
			ULog.d("path --------->" + path);
			StartWithPath = true;
			File file = new File(path);
			installApps(file);
		} else {
			StartWithPath = false;
			autoInstallApps();
		}
	}

	private void cancelInstallNotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(0);
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		cancelInstallNotification();
		if (mInstallHandler != null) {
			mInstallHandler.removeCallbacksAndMessages(null);
		}
		ULog.d("onDestroy");
	}

	public void showToast(final String msg) {
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void startApp() {

		/*if (i < pmList.size()) {
			// new Handler().postDelayed(new Runnable(){
			// @Override
			// public void run() {
			// // TODO Auto-generated method stub
			ULog.d("pmList         +++++++++ " + pmList.get(i));
			Message msg = new Message();
			msg.obj = pmList.get(i);
			mBootHandler.sendMessageDelayed(msg, i*2000);
			i++;
			startApp();
			// }
			// }, 2000);
		}else
			installAlerDialogDismiss();*/
			Intent intent = new Intent(this, StartActivityService.class);
			startService(intent);
			installAlerDialogDismiss();

		
	}



	private void startApplication(String packname){
		PackageManager packageManager = getPackageManager();
		Intent intent = new Intent();
		intent = packageManager.getLaunchIntentForPackage(packname); 
		startActivity(intent);
	}

	private void doStartApplicationWithPackageName(String packagename) {
		
		// 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
		PackageInfo packageinfo = null;
		try {
			packageinfo = getPackageManager().getPackageInfo(packagename, 0);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		ULog.d("              packageinfo.packageName ----------------------------->"   +  packageinfo.packageName);
		if (packageinfo == null) {
			return;
		} // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName); // 通过getPackageManager()的queryIntentActivities方法遍历
		List<ResolveInfo> resolveinfoList = getPackageManager().queryIntentActivities(resolveIntent, 0);
		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
		if (resolveinfo != null) { // packagename = 参数packname
			String packageName = resolveinfo.activityInfo.packageName; // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
			String className = resolveinfo.activityInfo.name; // LAUNCHER Intent
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER); // 设置ComponentName参数1:packagename参数2:MainActivity路径
			ComponentName cn = new ComponentName(packageName, className);
			intent.setComponent(cn);
			ULog.d("              start ----------------------------->"   +  packagename);
			startActivity(intent);
		}
	}

}
