package com.android.server;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IScanService;
import android.os.IScanServiceWrapper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.os.Build;
import android.os.scanner.IScanCallBack;
import java.util.Map;
//juzhitao begin
import android.os.SystemProperties;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import java.util.List;
//juzhitao end

/**
 * Created by josin on 17-5-8.
 */

public class ScanServiceProxy extends IScanService.Stub {

    private static final String TAG = "ScanServiceProxy";
    private IScanServiceWrapper mScanServiceWrapper;
    private Context mContext;
    private Intent scanIntent;
    private BroadcastReceiver bootReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
                Log.i(TAG, "onReceive: boot complete");
                mContext.startService(scanIntent);
                Log.i(TAG, "init ...");
                init();
            }
        }
    };

    public ScanServiceProxy(Context context){
        Log.i(TAG, "ScanServiceProxy ...");
        mContext = context;
        scanIntent = new Intent();
        scanIntent.setClassName("com.android.usettings","com.android.server.ScanService");
        IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
        context.registerReceiver(bootReceiver, filter);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "==========onServiceConnected============");
            mScanServiceWrapper = IScanServiceWrapper.Stub.asInterface(iBinder);
	    // urovo add shenpidong begin 2019-03-14
	    if(android.os.Build.PROJECT.equals("SQ53")){
	        try {
	            int type = readConfig("SCANER_TYPE");
		    // urovo add shenpidong begin 2019-06-22
	            if (type < 1 || type > 15) {
                        writeConfig("SCANER_TYPE", 8);
	            }
		    // urovo add shenpidong end 2019-06-22
	        } catch (android.os.RemoteException e) {
                    Log.e(TAG, "==========onServiceConnected============ RemoteException");
                }
	    }
	    // urovo add shenpidong end 2019-03-14
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        	mScanServiceWrapper = null;
        	Log.i(TAG, "==========onServiceDisconnected============");
        }
    };

    @Override
    public void init() {
        if(mScanServiceWrapper != null) {
            return;
        } else {
            Log.i(TAG, "init bind scan service");
            mContext.bindServiceAsUser(scanIntent, mServiceConnection, Context.BIND_AUTO_CREATE, UserHandle.OWNER);
            long waitTime = System.currentTimeMillis();
            while(mScanServiceWrapper == null && System.currentTimeMillis()-waitTime < 2000){
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean open() throws RemoteException {
        if (mScanServiceWrapper == null)  return false;
        try {
            return mScanServiceWrapper.open();
        } catch (android.os.RemoteException e) {
        }
        return false;
    }

    @Override
    public void close() throws RemoteException {
        if (mScanServiceWrapper == null)  return;
        try {
            mScanServiceWrapper.close();
        } catch (android.os.RemoteException e) {
        }
    }
    @Override
    public void closeScannerByCamera() throws RemoteException {
        if (mScanServiceWrapper == null)  return;
        try {
            mScanServiceWrapper.closeScannerByCamera();
        } catch (android.os.RemoteException e) {
        }
    }

    @Override
    public void writeConfig(String s, int i) throws RemoteException {
        if (mScanServiceWrapper == null)  return;
        try {
            mScanServiceWrapper.writeConfig(s, i);
        } catch (android.os.RemoteException e) {
        }
    }

    @Override
    public int readConfig(String s) throws RemoteException {
        Log.i(TAG, "mScanServiceWrapper ==== " + mScanServiceWrapper);
        if (mScanServiceWrapper == null)  return 0;
        try {
            return mScanServiceWrapper.readConfig(s);
        } catch (android.os.RemoteException e) {
        }
        return 0;
    }

    @Override
    public String writeConfigs(String s, String s1) throws RemoteException {
        if (mScanServiceWrapper == null)
            return null;
        try{
            return mScanServiceWrapper.writeConfigs(s, s1);
        } catch (android.os.RemoteException e) {
        }
        return null;
    }

    @Override
    public String readConfigs(String s, String s1) throws RemoteException {
        if (mScanServiceWrapper == null)
            return null;
        try{
            return mScanServiceWrapper.readConfigs(s, s1);
        } catch (android.os.RemoteException e) {
        }
        return null;
    }

    @Override
    public Map getScanerList() throws RemoteException {
        if (mScanServiceWrapper == null)
            return null;
        try{
            return mScanServiceWrapper.getScanerList();
        } catch (android.os.RemoteException e) {
        }
        return null;
    }

    @Override
    public boolean setDefaults() throws RemoteException {
        if (mScanServiceWrapper == null)
            return false;
        try{
            return mScanServiceWrapper.setDefaults();
        } catch (android.os.RemoteException e) {
        }
        return false;
    }

    /* urovo add juzhitao:open camera app and press scan key at same time,camera app crash
	so detect if camera app is in foreground,in this case do not start decode in scanner*/
    private boolean isCameraTopActivity() {
        List<ActivityManager.RunningTaskInfo> taskList;

        try {
            taskList = ActivityManagerNative.getDefault().getTasks(1);
        } catch (RemoteException e) {
            Log.e(TAG, "Camera get the activity stack failed");
            return false;
        }
        if ((taskList != null)
                && (taskList.get(0) != null)
                && (taskList.get(0).topActivity != null)
                && (taskList.get(0).topActivity.getPackageName() != null)
                && (taskList.get(0).topActivity.getPackageName().equals("org.codeaurora.snapcam")
		|| taskList.get(0).topActivity.getPackageName().equals("com.myntai.volume.measure")
		|| taskList.get(0).topActivity.getPackageName().equals("com.mvcn.mipicamera.develop")
                )) {
            Log.e(TAG, "Camera get the activity stack" + taskList.get(0).topActivity.getPackageName());
            return true;
        }

        return false;
    }
    // urovo add juzhitao
    @Override
    public void softTrigger(int i) throws RemoteException {
        if (mScanServiceWrapper == null){
            Log.i(TAG, "mScanServiceWrapper == null");
            return;
        } 
        try{
		//juzhitao add persist.sys.urv.reset.scanner is set by snapcam or myntai app
		if(Build.PROJECT.equals("SQ53C")){
			String reset_scanner = SystemProperties.get("persist.sys.urv.reset.scanner", "0");
			Log.e(TAG, "softTrigger end reset_scanner============[" + reset_scanner + "]");
			if(reset_scanner.equals("1")){
				if(isCameraTopActivity()){
					Log.e(TAG, "Camera is opened cannot use scanner");
					return;
				}
				close();
				open();
				Log.e(TAG, "reset_scanner");
			}
		}
		//juzhitao end

            mScanServiceWrapper.softTrigger(i);
        } catch (android.os.RemoteException e) {
            e.printStackTrace();
            mContext.unbindService(mServiceConnection);
            mScanServiceWrapper = null;
            init();
            
        }
    }

    @Override
    public boolean lockHwTriggler(boolean b) throws RemoteException {
        if (mScanServiceWrapper == null)
            return false;
        try{
            return mScanServiceWrapper.lockHwTriggler(b);
        } catch (android.os.RemoteException e) {
        }
        return false;
    }

    @Override
    public int getPropertyInt(int i) throws RemoteException {
        if (mScanServiceWrapper == null)
            return 0;
        try{
            return mScanServiceWrapper.getPropertyInt(i);
        } catch (android.os.RemoteException e) {
        }
        return 0;
    }

    @Override
    public int setPropertyInts(int[] ints, int i, int[] ints1, int i1, int[] ints2) throws
            RemoteException {
        if (mScanServiceWrapper == null)
            return 0;
        try{
            return mScanServiceWrapper.setPropertyInts(ints, i, ints1, i1, ints2);
        } catch (android.os.RemoteException e) {
        }
        return 0;
    }

    @Override
    public int getPropertyInts(int[] ints, int i, int[] ints1, int i1, int[] ints2) throws
            RemoteException {
        if (mScanServiceWrapper == null)
            return 0;
        try{
            return mScanServiceWrapper.getPropertyInts(ints, i, ints1, i1, ints2);
        } catch (android.os.RemoteException e) {
        }
        return 0;
    }

    @Override
    public boolean setPropertyString(int i, String s) throws RemoteException {
        if (mScanServiceWrapper == null)
            return false;
        try{
            return mScanServiceWrapper.setPropertyString(i, s);
        } catch (android.os.RemoteException e) {
        }
        return false;
    }

    @Override
    public String getPropertyString(int i) throws RemoteException {
        if (mScanServiceWrapper == null)
            return null;
        try{
            return mScanServiceWrapper.getPropertyString(i);
        } catch (android.os.RemoteException e) {
        }
        return null;
    }

    @Override
    public void enableAllSymbologies(boolean b) throws RemoteException {
        if (mScanServiceWrapper == null)
            return;
        try{
            mScanServiceWrapper.enableAllSymbologies(b);
        } catch (android.os.RemoteException e) {
        }
        return;
    }

    @Override
    public boolean isSymbologyEnabled(int i) throws RemoteException {
        if (mScanServiceWrapper == null)
            return false;
        try{
            return mScanServiceWrapper.isSymbologyEnabled(i);
        } catch (android.os.RemoteException e) {
        }
        return false;
    }

    @Override
    public boolean isSymbologySupported(int i) throws RemoteException {
        if (mScanServiceWrapper == null)
            return false;
        try{
            return mScanServiceWrapper.isSymbologySupported(i);
        } catch (android.os.RemoteException e) {
        }
        return false;
    }

    @Override
    public void enableSymbology(int i, boolean b) throws RemoteException {
        if (mScanServiceWrapper == null)
            return;
        try{
            mScanServiceWrapper.enableSymbology(i, b);
        } catch (android.os.RemoteException e) {
        }
        return;
    }
    @Override
    public boolean getCameraStatus() throws RemoteException {
        if (mScanServiceWrapper == null)
            return false;
        try{
            return mScanServiceWrapper.getCameraStatus();
        } catch (android.os.RemoteException e) {
        }
        return false;
    }

    @Override
    public void setCameraStatus(boolean b) throws RemoteException {
        if (mScanServiceWrapper == null)
            return;
        try{
            mScanServiceWrapper.setCameraStatus(b);
        } catch (android.os.RemoteException e) {
        }
        return;
    }

    @Override
    public void addScanCallBack(IScanCallBack cb) throws RemoteException {
        if (mScanServiceWrapper == null)
            return;
        try{
            mScanServiceWrapper.addScanCallBack(cb);
        } catch (android.os.RemoteException e) {
        }
        return;
    }

    @Override
    public void removeScanCallBack(IScanCallBack cb) throws RemoteException {
        if (mScanServiceWrapper == null)
            return;
        try{
            mScanServiceWrapper.removeScanCallBack(cb);
        } catch (android.os.RemoteException e) {
        }
        return;
    }
	
	// urovo add shenpidong begin 2019-07-20
    @Override
    public boolean screenTurnedOn(boolean on) {
        Log.i(TAG, "screenTurnedOn ... on:" + on + ",Scan Service:" + (mScanServiceWrapper == null));
	boolean turnOn = false;
        if (mScanServiceWrapper == null)
            return turnOn;
        try{
            turnOn = mScanServiceWrapper.screenTurnedOn(on);
        } catch (android.os.RemoteException e) {
        }
        Log.i(TAG, "screenTurnedOn ... turnOn:" + turnOn);
        return turnOn;
    }
    // urovo add shenpidong end 2019-07-20

}
