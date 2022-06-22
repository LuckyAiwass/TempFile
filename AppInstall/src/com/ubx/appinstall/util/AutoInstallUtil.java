package com.ubx.appinstall.util;

import java.io.File;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.IPackageInstallObserver;

public class AutoInstallUtil {
	
	public static void checkAndCreateAutoInstallPath() {
	    File file = new File(AutoInstallConfig.AUTOINSTALLPATH);
	    if(file!=null) {
		if(!file.exists()) {
		    boolean createAutoInstallPath = file.mkdirs();
		    ULog.i("checkAutoInstallPath , createAutoInstallPath:" + createAutoInstallPath);
		}
	    } else {
		ULog.i("checkAutoInstallPath , file:" + file);
	    }
	}
	
	public static boolean isExistsAutoInstallPath() {
	    return new File(AutoInstallConfig.AUTOINSTALLPATH).exists();
	}
	
	public static boolean autoInstallApps(Context context) {
	    if(context == null) {
		ULog.i("autoInstallApps , error! context:" + context);
		return false;
	    }
	    if(isExistsAutoInstallPath()) {
		File file = new File(AutoInstallConfig.AUTOINSTALLPATH);
		if(file!=null && file.exists()) {
			
		} else {
		    ULog.i("autoInstallApps , error!auto install path " + AutoInstallConfig.AUTOINSTALLPATH + " is not exists!");
		}
	    } else {
		ULog.i("autoInstallApps , error!!! auto install path " + AutoInstallConfig.AUTOINSTALLPATH + " is not exists!");
	    }
	    return false;
	}
	
	public static boolean installApps(Context context , File file) {
	    if(file == null) {
		ULog.d("installApps error!!! file:" + file);
		return false;
	    }
	    File[] files = file.listFiles();
	    if(files!=null && files.length>0) {
		for(File f:files) {
		    if(f!=null && f.exists()) {
			if(f.isDirectory()) {
			    installApps(context , f);
			} else {
			    installAPK(context , f);
			}
		    } else {
			ULog.i("installApps , file not exists! file:" + f);
		    }
		}
	    } else {
		ULog.i("autoInstallApps " + AutoInstallConfig.AUTOINSTALLPATH + " is Empty! files:" + (files!=null?files.length:-1));
	    }
	    return false;
	}
	
	private static boolean installAPK(Context context , File apkFile) {
	    if(context == null || apkFile == null) {
		ULog.i("installAPK error!!! context:" + context + " ,apk:" + apkFile);
		return false;
	    }
	    if(!apkFile.exists()) {
		ULog.i("installAPK error!!! apk is not exists! apk:" + apkFile);
		return false;
	    }
	    PackageInstallObserver installObserver = new PackageInstallObserver();
	    Uri packageURI = Uri.parse("file://"+apkFile.getPath());
	    int installFlags = 0;
	    PackageManager pm = context.getPackageManager();
	    PackageParser.Package pkgInfo = getPackageInfo(packageURI);
	    if(pkgInfo == null) {
		ULog.i("installAPK error!!! parse AndroidManifest.xml error for " + packageURI + " !");
		return false;
	    }
	    installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
	    pm.installPackage(packageURI, installObserver, installFlags, null);
	    return true;
	}

	private static  PackageParser.Package getPackageInfo(Uri packageURI) {
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
	
	static class PackageInstallObserver extends IPackageInstallObserver.Stub {
	    public void packageInstalled(String packageName, int returnCode) {
//		
//			 Message msg = mHandler.obtainMessage(INSTALL_COMPLETE);
//             msg.arg1 = returnCode;
//             mHandler.sendMessage(msg);
		}
	};

}
