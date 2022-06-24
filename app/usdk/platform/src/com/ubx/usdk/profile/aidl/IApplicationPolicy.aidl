// IApplicationPolicy.aidl
package com.ubx.usdk.profile.aidl;

// Declare any non-default types here with import statements
import android.app.ActivityManager;

import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageInstallObserver;
import android.os.Bundle;
import android.os.Debug;
import android.content.ComponentName;
import android.content.pm.ParceledListSlice;
import java.util.List;

interface IApplicationPolicy {

       int installPackage(String apkFilePath, int installFlags, in IPackageInstallObserver observer);
       int deletePackage(String packageName, int deleteFlags, in IPackageDeleteObserver observer);

       boolean setDefaultLauncher(in ComponentName componentName);
       boolean removeDefaultLauncher(String packageName);

       boolean clearApplicationUserData(String packageName, in IPackageDataObserver observer);
       boolean deleteApplicationCacheFiles(String packageName, in IPackageDataObserver observer);

       boolean grantRuntimePermission(String packageName, String permName);
       boolean revokeRuntimePermission(String packageName, String permName);
       void setOpsUidMode(int code, int uid, int mode);

       List<ActivityManager.RunningTaskInfo> getRunningTasks(int maxNum);
       List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses();
       void getMemoryInfo(out ActivityManager.MemoryInfo outInfo);
       Debug.MemoryInfo[] getProcessMemoryInfo(in int[] pids);
       ParceledListSlice queryUsageStats(int bucketType, long beginTime, long endTime);

       double getAppPowerUsage(String packagename);
       double getAllAppsPowerUsage();
       Bundle getPowerUsage();

       boolean getApplicationEnabledSetting(String packageName);
       void setApplicationEnabledSetting(String packageName, boolean enable);

       boolean forceStopPackage(in String packageName);
       boolean removeTask(int taskid);

       void setLockTaskMode(String packageName, boolean enable);
}
