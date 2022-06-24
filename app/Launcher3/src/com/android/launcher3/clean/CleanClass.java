package com.android.launcher3.clean;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.provider.Settings;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.content.pm.ActivityInfo;
import android.content.ComponentName;
import com.android.launcher3.R;
import java.util.ArrayList;

public class CleanClass {
	private long ramSize1 = 0l;
	private long ramSize2 = 0l;
	//private PackageManager mPackageManager = null;
	private Handler mHandler = null;
	Context mContext;
	private static final int MSG_UPDATE_TOAST_TEXT = 1;
	private static final int MSG_CLEAN_RAM = 2;
	private static final int DISPLAY_TASKS = 20;
    private static final int MAX_TASKS = DISPLAY_TASKS + 1;
	public String TopPackage = "";
	public ArrayList<String> lockNames = new ArrayList<String>(); //zhangguangfen_20200813 add lockTasks for SQ53Q

	public CleanClass(Context context)
	{
		mContext = context;
	}
	private void startclean(Long ramsize) {
		ramSize1 = ramsize;
		killBackgroundProcess();
		System.gc();
		//Log.d("chen","ramSize2="+ramSize2);
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MSG_UPDATE_TOAST_TEXT:
					//Log.d("yanglin","startclean.MSG_UPDATE_TOAST_TEXT");
					ramSize2 = getRamFreeMemSize();
					//Log.i("chen","ramSize2 = "+ramSize2);
					String text = String.format(mContext.getString(R.string.cleanramsize)
						,convertStorage(ramSize2-ramSize1),convertStorage(ramSize2));
					Toast.makeText(mContext,text,Toast.LENGTH_LONG).show();
					ramSize1 = 0l;
					ramSize2 = 0l;
					//Log.d("chen",mText);
					break;
				case MSG_CLEAN_RAM:
					//Log.d("yanglin","startclean.MSG_CLEAN_RAM");
					killBackgroundProcess();
					System.gc();
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TOAST_TEXT, 400);
					break;
				}
			}
		};
		mHandler.sendEmptyMessageDelayed(MSG_UPDATE_TOAST_TEXT, 400);
		//mHandler.sendEmptyMessageDelayed(MSG_CLEAN_RAM, 200);
	}
	public void startclean() {
		ramSize1 = getRamFreeMemSize();
		//Log.d("yanglin","startclean.ramSize1 = "+ramSize1);
		//Log.i("chen","ramSize1 = "+ramSize1);
		startclean(ramSize1);
	}

	//zhangguangfen_20200813 add lockTasks for SQ53Q S
	public void startclean(ArrayList<String> lockPackages) {
		ramSize1 = getRamFreeMemSize();
		startclean(ramSize1);
		lockNames = lockPackages;
	}
	//zhangguangfen_20200813 add lockTasks for SQ53Q E
	private void killBackgroundProcess() {
		//Log.d("yanglin","killBackgroundProcess.");
		ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
		ArrayList<String> killPackages = new ArrayList<String>();
		for (ActivityManager.RunningAppProcessInfo info : processes) {
			if (info != null && info.processName != null&& info.processName.length() > 0) {
				try{
					String pkgName = info.pkgList[0];
					PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(pkgName,0);
					if((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
					{
						if(!killPackages.contains(pkgName)&&!isDefaultIME(pkgName)&&!lockNames.contains(pkgName)) //zhangguangfen_20200813 add lockTasks for SQ53Q &&!isBootEnable(pkgName)
						{
							//Log.d("yanglin","killBackgroundProcess.222.pkgName = "+pkgName);
							killPackages.add(pkgName);
						}
					}
				} catch (Exception e) {
		            e.printStackTrace();  
		        }
			}
		}
		for (String pkg : killPackages) 
		{
			Log.i("chen",pkg+" kill");
			//Log.d("yanglin","killBackgroundProcess.pkg = "+pkg);
			//am.killUid(info.uid,"clean_kill");
			am.forceStopPackage(pkg);
		}
	}
	/*public Boolean isBootEnable(String packageName) {  
		Boolean enable = false;
	    ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		enable = manager.isBootEnabledForPackage(packageName);
        return enable;  
    } */ 
	boolean isDefaultIME(String packageName)
	{
		String defaultInput = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.DEFAULT_INPUT_METHOD);
		if(defaultInput != null && defaultInput.contains(packageName))
			return true;
		return false;
	}
	/*private void clearCache() {
		try {
			if (mPackageManager == null) {
				mPackageManager = mContext.getPackageManager();
			}
			Long freeStorageSize = Long.valueOf(getDataDirectorySize());
			mPackageManager.freeStorageAndNotify(freeStorageSize,null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	private long getRamFreeMemSize() {
		MemInfoReader miReader = new MemInfoReader();
		miReader.readMemInfo();
		return (miReader.getFreeSize() + miReader.getCachedSize());
	}
	private long getDataDirectorySize() {
		File tmpFile = Environment.getDataDirectory();
		if (tmpFile == null) {
			return 0l;
		}
		String strDataDirectoryPath = tmpFile.getPath();
		StatFs localStatFs = new StatFs(strDataDirectoryPath);
		long size = localStatFs.getBlockSize() * localStatFs.getBlockCount();
		return size;
	}
	public static String convertStorage(long size) {
		long kb = 1024;
		long mb = kb * 1024;
		long gb = mb * 1024;
		if(size<0)
			return "0KB";
		if (size >= gb) {
			return String.format("%.1f G", (float) size / gb);
		} else if (size >= mb) {
			float f = (float) size / mb;
			return String.format(f > 100 ? "%.0f M" : "%.1f M", f);
		} else if (size >= kb) {
			float f = (float) size / kb;
			return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
		} else
			return String.format("%d B", size);
	}
}
