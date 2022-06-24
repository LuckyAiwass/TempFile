package com.ubx.usdk.profile;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.IActivityTaskManager;
import android.app.admin.DevicePolicyManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageInstallObserver;
import android.os.BatteryStats;
import android.os.Binder;
import android.os.IBinder;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.content.pm.ParceledListSlice;
import com.android.internal.os.BatterySipper;
import com.android.internal.os.BatteryStatsHelper;
import com.ubx.usdk.LogUtil;
import com.ubx.usdk.profile.aidl.IApplicationPolicy;
import java.io.File;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import java.util.concurrent.SynchronousQueue;
import libcore.io.IoUtils;
import android.content.IIntentSender;
import android.content.IntentSender;
import android.content.IIntentReceiver;
import android.content.pm.PackageInstaller;
import android.content.pm.IPackageInstaller;
import android.device.SettingsUtils;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import android.app.StatusBarManager;

/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class ApplicationImpl extends IApplicationPolicy.Stub {

    private Context mContext;

    private PackageManager mPackageManager;
    private IPackageManager mIPackageManager;
    private PackageInstaller mPackageInstaller;
    private BatteryStatsHelper mBatteryStatsHelper;
    private UserManager mUserManager;
    private ActivityManager mActivityManager;
    private IActivityManager mIActivityManager;
    private IActivityTaskManager mIActivityTaskManager;
    private UsageStatsManager mUsageStatsManager;

    private boolean dialogFlag = true;
    private AlertDialog mLockTaskExitAlertDialog;
    private WorkerThread mWorkerThread;
    private WorkerHandler mWorkerHandler;

    private static final int MESSAGE_SHOW_DIALOG_PASSWD = 9;

    public ApplicationImpl(Context context) {
        mContext = context;
        mPackageManager = mContext.getPackageManager();
        
        mIPackageManager = AppGlobals.getPackageManager();

        mPackageInstaller = mPackageManager.getPackageInstaller();

        mBatteryStatsHelper = new BatteryStatsHelper(mContext);
        mBatteryStatsHelper.create((Bundle) null);

        mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);

        mUsageStatsManager = (UsageStatsManager) mContext.getSystemService(Context.USAGE_STATS_SERVICE);

        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        if(mIActivityTaskManager == null)
            mIActivityTaskManager = IActivityTaskManager.Stub.asInterface(ServiceManager.getService(Context.ACTIVITY_TASK_SERVICE));

        mWorkerThread = new WorkerThread();
        mWorkerThread.start();
    }

    @Override
    public boolean setDefaultLauncher(ComponentName componentName) throws RemoteException {
        LogUtil.d("setDefaultLauncher------------------------------:");
        if(componentName == null)
            return false;

        boolean result = false;
        try{
            ArrayList<ResolveInfo> homeActivities = new ArrayList<>();
            ComponentName currentDefaultHome = mPackageManager.getHomeActivities(homeActivities);
            if(currentDefaultHome != null && currentDefaultHome.getPackageName().equals(componentName.getPackageName())) {
                LogUtil.d("currentDefaultHome PackageName  == " + currentDefaultHome.getPackageName());
                result = true;
            } else {
                IntentFilter mHomeFilter = new IntentFilter(Intent.ACTION_MAIN);
                mHomeFilter.addCategory(Intent.CATEGORY_HOME);
                mHomeFilter.addCategory(Intent.CATEGORY_DEFAULT);
                ComponentName[] mHomeComponentSet = new ComponentName[homeActivities.size()];
                int i = 0;
                for (ResolveInfo candidate : homeActivities) {
                    LogUtil.i("info.name  == " + candidate.activityInfo.name + "   PackageName  ==  " + candidate.activityInfo.packageName);
                    ComponentName activityName = new ComponentName(candidate.activityInfo.packageName, candidate.activityInfo.name);
                    mHomeComponentSet[i++] = activityName;
                }
                mPackageManager.replacePreferredActivity(mHomeFilter,
                        IntentFilter.MATCH_CATEGORY_EMPTY, mHomeComponentSet, componentName);
            }
        } catch(Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    @Override
    public boolean removeDefaultLauncher(String packageName) throws RemoteException {
        if (TextUtils.isEmpty(packageName))
            return false;

        boolean result = false;
        try {
            //clear default launcher preference
            ArrayList<IntentFilter> intentList = new ArrayList<IntentFilter>();
            ArrayList<ComponentName> cnList = new ArrayList<ComponentName>();
            mPackageManager.getPreferredActivities(intentList, cnList, null);
            IntentFilter dhIF;
            for (int i = 0; i < cnList.size(); i++) {
                dhIF = intentList.get(i);
                if (dhIF.hasAction(Intent.ACTION_MAIN) && dhIF.hasCategory(Intent.CATEGORY_HOME)) {
                    if (packageName.equals(cnList.get(i).getPackageName())) {
                        mPackageManager.clearPackagePreferredActivities(
                                cnList.get(i).getPackageName());
                        result = true;
                    } else {
                        setDefaultLauncher(cnList.get(i));
                        result = true;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        return result;
    }

    @Override
    public boolean grantRuntimePermission(String packageName, String permName) throws RemoteException {
        LogUtil.d("grantRuntimePermission:"+packageName +"  "+permName);
        boolean result = false;

        try {
            mIPackageManager.grantRuntimePermission(packageName, permName, UserHandle.USER_SYSTEM);
            result = true;
        } catch (Exception e) {
            LogUtil.e("grantRuntimePermission", e);
            result = false;
        } 
        return result;
    }

    @Override
    public boolean revokeRuntimePermission(String packageName, String permName) throws RemoteException {
        boolean result = false;
        try {
            mIPackageManager.revokeRuntimePermission(packageName, permName, UserHandle.USER_SYSTEM);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    @Override
    public void setOpsUidMode(int code, int uid, int mode) throws RemoteException {
        //long callingId = Binder.clearCallingIdentity();
        LogUtil.d("setOpsUidMode:"+code +"  "+uid+" "+mode);
        AppOpsManager mAppOps = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
        mAppOps.setUidMode(code, uid, mode);
        //Binder.restoreCallingIdentity(callingId);
    }

    @Override
    public double getAppPowerUsage(String packagename) throws RemoteException {
        double apppower = 0.0;
        PackageInfo packageInfo;
        try {
            packageInfo = mPackageManager.getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtil.d("getAppPowerUsage not found package:" + packagename);
            return apppower;
        }

        mBatteryStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, mUserManager.getUserProfiles());

        List<BatterySipper> usageList = mBatteryStatsHelper.getUsageList();
        for (BatterySipper sipper : usageList) {
            if (sipper.getUid() == packageInfo.applicationInfo.uid) {
                int dischargeAmount = mBatteryStatsHelper.getStats().getDischargeAmount(
                        BatteryStats.STATS_SINCE_CHARGED);
                final int percentOfMax = (int) ((sipper.totalPowerMah)
                        / mBatteryStatsHelper.getTotalPower() * dischargeAmount + .5f);
                apppower = sipper.totalPowerMah;
            }
        }

        return apppower;
    }

    @Override
    public double getAllAppsPowerUsage() throws RemoteException {
        double totalPower = 0.0;

        mBatteryStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, mUserManager.getUserProfiles());

        List<BatterySipper> usageList = mBatteryStatsHelper.getUsageList();
        for (BatterySipper sipper : usageList) {
            totalPower += sipper.totalPowerMah;
        }

        return totalPower;
    }

    @Override
    public Bundle getPowerUsage() throws RemoteException {
        mBatteryStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, mUserManager.getUserProfiles());

        List<BatterySipper> usageList = mBatteryStatsHelper.getUsageList();
        Bundle b = new Bundle();
        for (BatterySipper sipper : usageList) {
            b.putDouble(sipper.drainType.name(), sipper.totalPowerMah);
        }

        return b;
    }

    @Override
    public boolean getApplicationEnabledSetting(String packageName) throws RemoteException {
        try {
            if (mIPackageManager.getApplicationEnabledSetting(packageName, UserHandle.myUserId())
                    == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void setApplicationEnabledSetting(String packageName, boolean enable) throws RemoteException {
        try {
            mIPackageManager.setApplicationEnabledSetting(packageName,
                    enable?PackageManager.COMPONENT_ENABLED_STATE_ENABLED:PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0, UserHandle.myUserId(), null);
        } catch (Exception e) {
            // Will never happen.
            LogUtil.e("setApplicationEnabledSetting", e);
        }
    }

    @Override
    public boolean forceStopPackage(String packageName) throws RemoteException {
        if(mIActivityManager == null) {
            mIActivityManager = IActivityManager.Stub.asInterface(ServiceManager.getService(Context.ACTIVITY_SERVICE));
        }
        mIActivityManager.forceStopPackage(packageName, UserHandle.myUserId());
        return true;
    }

    @Override
    public boolean removeTask(int taskid) throws RemoteException {
        boolean result = false;

        if(mIActivityManager == null) {
            mIActivityManager = IActivityManager.Stub.asInterface(ServiceManager.getService(Context.ACTIVITY_SERVICE));
        }

        try{
            mIActivityManager.removeTask(taskid);
            result = true;
        } catch(Exception e) {
            result = false;
            LogUtil.e("removeTask", e);
        }
        return result;
    }

    private String mPassword;//全局变量,避免不能及时更新
    private void showDialog(final String password, final int uid) {
        mPassword = password;
        if(!dialogFlag)
            mLockTaskExitAlertDialog = null;
        if(mLockTaskExitAlertDialog == null){//避免多次创建start
            dialogFlag = true;
            final EditText etPw = new EditText(mContext);
            etPw.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD|android.text.InputType.TYPE_CLASS_TEXT);
            mLockTaskExitAlertDialog = new AlertDialog.Builder(mContext)
            .setTitle(mContext.getString(com.android.internal.R.string.locktask_title)).setView(etPw)
            .setNegativeButton(mContext.getString(com.android.internal.R.string.cancel), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    etPw.setText("");
                }
            })
            .setPositiveButton(mContext.getString(com.android.internal.R.string.ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    // Log.d(TAG, "etPw.getText().toString() = " + etPw.getText().toString() + ", mPassword = " + mPassword);
                    if(etPw.getText().toString().equals(mPassword)){
                        try {
                            if (mIActivityTaskManager.isInLockTaskMode()) {
                                Log.d("TAG", "Screen is in isInLockTaskMode, stop it!");
                                mIActivityTaskManager.stopSystemLockTaskMode();
                            } else {
                                Log.d("TAG", "Screen is not in isInLockTaskMode");
                            }
                            int what = 0;
                            what &= ~StatusBarManager.DISABLE_EXPAND;
                            Settings.System.putString(mContext.getContentResolver(),"UROVO_STATUSBAR_ENABLE",String.valueOf(what));
                        } catch (RemoteException e) {
                            //Log.d(TAG, "exception --" + e);
                        }
                        etPw.setHint("");//清空内容
                        etPw.setText("");//清空内容
                        dialogFlag = false;
                        dialog.cancel();
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
		                handler.post(new Runnable() {
			             public void run() {
				                Toast toast = Toast.makeText(mContext, mContext.getString(com.android.internal.R.string.locktask_error_password), Toast.LENGTH_SHORT);
                                toast.show();
			                }
		                });
                        etPw.setHint(mContext.getString(com.android.internal.R.string.locktask_error_password));
                        etPw.setText("");
                        Log.d("TAG", "error: password");
                    }
                }
            }).create();

        }//避免多次创建end
        if(!mLockTaskExitAlertDialog.isShowing()) {
            mLockTaskExitAlertDialog.setCancelable(false);
            mLockTaskExitAlertDialog.setCanceledOnTouchOutside(false);
            mLockTaskExitAlertDialog.getWindow().setType(android.view.WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            mLockTaskExitAlertDialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);//无法弹出软键盘
            mLockTaskExitAlertDialog.show();
        }
    }

    @Override
    public void setLockTaskMode(String packageName, boolean enable) throws RemoteException {
        if(TextUtils.isEmpty(packageName)) return;
        long callUid = android.os.Binder.getCallingUid();
        try{
            if(enable) {
                String whitePackages = Settings.System.getString(mContext.getContentResolver(), "lockTaskWhitePackages");
                whitePackages = SettingsUtils.addPackageName(whitePackages, packageName);
                mIActivityTaskManager.updateLockTaskPackages(UserHandle.myUserId(), whitePackages.split(","));
                mIActivityTaskManager.updateLockTaskFeatures(UserHandle.myUserId(), DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD | DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS);
                List<ActivityManager.RunningTaskInfo> taskList;
                try {
                    taskList = mIActivityTaskManager.getTasks(1);
                    if ((taskList != null)
                            && (taskList.get(0) != null)
                            && (taskList.get(0).topActivity != null)){
                        int stackId =  taskList.get(0).id;
                        LogUtil.d("setLockTaskMode packageName:" + taskList.get(0).topActivity.getPackageName() + " stackId:" +stackId);
                        if(stackId >= 0) {
                            mIActivityTaskManager.startSystemLockTaskMode(stackId);
                        } else {
                            mIActivityTaskManager.startSystemLockTaskMode(1);
                        }
                    } else {
                        mIActivityTaskManager.startSystemLockTaskMode(1);
                    }
                    Settings.System.putString(mContext.getContentResolver(), "lockTaskPackage", packageName);
                } catch (Exception e) {
                    LogUtil.e("setLockTaskMode get the activity stack failed");
                }
            } else {
                String password =  Settings.System.getString(mContext.getContentResolver(), "LockTaskModePassword");
                if (!TextUtils.isEmpty(password) && packageName.equals("android")){
                    mWorkerHandler.removeMessages(MESSAGE_SHOW_DIALOG_PASSWD);
                    Message m = Message.obtain(mWorkerHandler, MESSAGE_SHOW_DIALOG_PASSWD);
                    m.arg1 = (int)callUid;
                    m.obj = password;
                    mWorkerHandler.sendMessage(m);
                } else {
                    Settings.System.putString(mContext.getContentResolver(), "lockTaskPackage","");
                    if (mIActivityTaskManager.isInLockTaskMode()) {
                        LogUtil.d("Screen is in isInLockTaskMode, stop it!");
                        mIActivityTaskManager.stopSystemLockTaskMode();
                    }
                } 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class WorkerHandler extends Handler {
        private static final int MESSAGE_SET = 0;

        public WorkerHandler() {

        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_DIALOG_PASSWD:
                    showDialog((String) msg.obj,msg.arg1);
                    break;
            }
        }
    }

    private final class WorkerThread extends Thread {
        public WorkerThread() {
            super("WorkerThread");
        }

        public void run() {
            Looper.prepare();
            mWorkerHandler = new WorkerHandler();

            Looper.loop();
        }
    }

    @Override
    public ParceledListSlice<UsageStats> queryUsageStats(int intervalType, long beginTime, long endTime) {
        long id = Binder.clearCallingIdentity();
        try {
            final List<UsageStats> results = mUsageStatsManager.queryUsageStats(intervalType, beginTime, endTime);
            if (results != null) {
                return new ParceledListSlice<>(results);
            }
        } finally {
            Binder.restoreCallingIdentity(id);
        }
        return null;
    }

    private static class LocalIntentReceiver {
        private final SynchronousQueue<Intent> mResult = new SynchronousQueue<>();

        private IIntentSender.Stub mLocalSender = new IIntentSender.Stub() {
            @Override
            public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken,
                             IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
                try {
                    mResult.offer(intent, 5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        public IntentSender getIntentSender() {
            return new IntentSender((IIntentSender) mLocalSender);
        }

        public Intent getResult() {
            try {
                return mResult.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class InstallThread extends Thread {
        private String mApkFile;
        private PackageInstaller.SessionParams mParams;
        private IPackageInstallObserver mObserver;

        private PackageInstaller.Session session = null;
        private OutputStream outputStream = null;
        private FileInputStream inputStream = null;

        public InstallThread(String apkfile, PackageInstaller.SessionParams params, final IPackageInstallObserver observer) {
            mApkFile = apkfile;
            mParams = params;
            mObserver = observer;
        }

        @Override
        public void run() {
            super.run();
            long id = Binder.clearCallingIdentity();
            try {
                int sessionId = mPackageInstaller.createSession(mParams);
                session = mPackageInstaller.openSession(sessionId);
                String apkName = mApkFile.substring(mApkFile.lastIndexOf(File.separator) + 1, mApkFile.lastIndexOf(".apk"));
                outputStream = session.openWrite(apkName, 0, -1);
                inputStream = new FileInputStream(mApkFile);
                byte[] buffer = new byte[4096];
                int n;
                while ((n = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, n);
                }
                outputStream.flush();
                session.fsync(outputStream);
                IoUtils.closeQuietly(inputStream);
                IoUtils.closeQuietly(outputStream);

                LocalIntentReceiver receiver = new LocalIntentReceiver();
                session.commit(receiver.getIntentSender());

                final Intent result = receiver.getResult();
                String msg = result.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
                int status = result.getIntExtra(PackageInstaller.EXTRA_STATUS, Integer.MIN_VALUE);
                if (mObserver != null) {
                    try {
                        mObserver.packageInstalled(apkName+" "+msg, installStatusToPublicStatus(status));
                    } catch (Exception e) {
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
                if (session != null) {
                    session.abandon();
                }
            } finally {
                IoUtils.closeQuietly(session);
                Binder.restoreCallingIdentity(id);
            }

        }
    }

    @Override
    public int installPackage(String apkfile, int installFlags, final IPackageInstallObserver observer) {
        if (TextUtils.isEmpty(apkfile)) {
            return -1;
        }

        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.installFlags |= installFlags;

        InstallThread installThread = new InstallThread(apkfile, params, observer);
        installThread.start();

        return 0;
    }

    @Override
    public int deletePackage(String packageName, int deleteFlags, IPackageDeleteObserver observer) {
        int res = -1;
        if (TextUtils.isEmpty(packageName)) {
            res = -2;
        } else if (mIPackageManager != null) {
            long id = Binder.clearCallingIdentity();
            try {
                mPackageManager.deletePackage(packageName, observer, deleteFlags);
                res = 0;
            } catch (Exception e) {
                res = -3;
            } finally {
                Binder.restoreCallingIdentity(id);
            }
        }
        return res;
    }

    public static int installStatusToPublicStatus(int status) {
        switch (status) {
            case PackageInstaller.STATUS_SUCCESS: return PackageManager.INSTALL_SUCCEEDED;
            case PackageInstaller.STATUS_FAILURE_CONFLICT: return PackageManager.INSTALL_FAILED_ALREADY_EXISTS;
            case PackageInstaller.STATUS_FAILURE_INVALID: return PackageManager.INSTALL_FAILED_INVALID_APK;
            case PackageInstaller.STATUS_FAILURE_STORAGE: return PackageManager.INSTALL_FAILED_INSUFFICIENT_STORAGE;
            case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE: return PackageManager.INSTALL_FAILED_OLDER_SDK;
            case PackageInstaller.STATUS_FAILURE_ABORTED: return PackageManager.INSTALL_FAILED_VERIFICATION_TIMEOUT;
            case PackageInstaller.STATUS_FAILURE: return PackageManager.INSTALL_FAILED_INTERNAL_ERROR;
            default: return PackageManager.INSTALL_FAILED_ABORTED;
        }
    }

    @Override
    public boolean clearApplicationUserData(String packageName, IPackageDataObserver observer) {
        boolean res = false;
        long id = Binder.clearCallingIdentity();
        try {
            mIPackageManager.clearApplicationUserData(packageName, observer, UserHandle.USER_SYSTEM);
            res = true;
        } catch (Exception e) {
            res = false;
        } finally {
            Binder.restoreCallingIdentity(id);
        }
        return res;
    }

    @Override
    public boolean deleteApplicationCacheFiles(String packageName, IPackageDataObserver observer) {
        boolean res = false;
        long id = Binder.clearCallingIdentity();
        try {
            mIPackageManager.deleteApplicationCacheFiles(packageName, observer);
            res = true;
        } catch (Exception e) {
            res = false;
        } finally {
            Binder.restoreCallingIdentity(id);
        }
        return res;
    }

    @Override
    public List<ActivityManager.RunningTaskInfo> getRunningTasks(int maxNum) {
        long id = Binder.clearCallingIdentity();
        List<ActivityManager.RunningTaskInfo> taskInfoList;
        taskInfoList = mActivityManager.getRunningTasks(maxNum);
        Binder.restoreCallingIdentity(id);
        return taskInfoList;
    }

    @Override
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        long id = Binder.clearCallingIdentity();
        List<ActivityManager.RunningAppProcessInfo> processInfoList;
        processInfoList = mActivityManager.getRunningAppProcesses();
        Binder.restoreCallingIdentity(id);
        return processInfoList;
    }

    @Override
    public Debug.MemoryInfo[] getProcessMemoryInfo(int[] pids) {
        long id = Binder.clearCallingIdentity();
        Debug.MemoryInfo[] memInfo;
        memInfo = mActivityManager.getProcessMemoryInfo(pids);
        Binder.restoreCallingIdentity(id);
        return memInfo;
    }

    @Override
    public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) {
        long id = Binder.clearCallingIdentity();
        mActivityManager.getMemoryInfo(outInfo);
        Binder.restoreCallingIdentity(id);
        return;
    }
}
