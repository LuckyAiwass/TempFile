/**
 * Copyright (c) 2012, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.update;

import com.ubx.update.https.Cer;
import com.ubx.update.https.HttpsManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.StrictMode;
import android.os.SystemProperties;
import android.os.RecoverySystem;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import android.os.ServiceManager;
import android.content.ContentResolver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.AlarmManager;
import android.provider.Settings;
import android.app.PendingIntent;
import java.util.Calendar;


public class DownloadManager {
    private static final boolean DEBUG = true;

    private static final String TAG = "OSUpdateDownloadManager";

    private static DownloadManager instance;

    private NotificationManager nManager;

    private SharedPreferences sp;

    private static Context mContext;
    private PowerManager mPm;
    private WakeLock mWakeLock ;
    private ContentResolver mSystemCR;
    private boolean enableNotification = false;
    private UpdateInfo mUpdateInfo;
    private boolean handleUpdateType = false;
    /**
     * action for state changed
     */
    static final String ACTION_STATE_CHANGED = "com.ubx.update.DOWNLOAD_STATE_CHANGED";

    /**
     * action for progress changed
     */
    static final String ACTION_PROGRESS_CHANGED = "com.ubx.update.DOWNLOAD_PROGRESS_CHANGED";

    /**
     * current task
     */
    private DownloadTask task;

    private static final int WHAT_TASK_PROGRESS = 1;

    private static final int WHAT_TASK_COMPLETE = 2;

    private static final int WHAT_TASK_FAILED = 3;

    private static final int WHAT_TASK_PAUSE = 4;

    private static final int WHAT_TASK_START = 5;
    
    private static final int WHAT_TASK_verify = 6;

    private static final int WHAT_TASK_DOWNLOAD_STATE = 7;
    //add by urovo weiyu on 2019-10-30 start
    private static final int WHAT_TASK_CANCEL = 8;

    public static final long SINGLE_OR_MULT_THREAD_DOWNLOAD = 1024*1024*30;//选择单线程下载还是多线程下载的文件大小临界点

    public static long mult_pro;

    public static boolean mult_download_flag = false;

    public static boolean mult_flag1 = false;  //线程1下载完成标志
    public static boolean mult_flag2 = false;  //线程2下载完成标志
    public static boolean mult_flag3 = false;  //线程3下载完成标志
    public static boolean merge_success = false;//文件合并完成标志
    public static boolean pause_or_cancel = false;

    public static boolean isMultDownloadCanced = false;

    //add by urovo weiyu on 2019-10-30 end

    private static final String KEY_REMOTE = "url_server";

    private static final String KEY_TAG_REMOTE_URL = "tag_remote_url";

    public static final String KEY_TAG_UPDATE_SIZE = "tag_update_size";

    public static final String KEY_TAG_LOCAL_PATH = "tag_local_path";

    private static final String KEY_TAG_LOCAL_SIZE = "tag_local_size";

    private static final String KEY_TAG_LOCAL_TIME = "tag_local_time";

    private static final String PATH_SAVE_DIR = DownloadManager.getSdCardPath() + "/QRDUpdate/";

    private static final String DEFAULT_SERVER_URL = "http://update.urovo.com:8000/Update/osUpdate";
    public static final String DEFAULT_RUPOS_SERVER = "http://87.251.82.85:80/Update/osUpdate";
    private boolean mForceUpdate = false;
    //WIFI OS: http://download1.tw.ute.com/cs/firmware/ea300/genericSaga2Wifi.xml
    ///LTE OS: http://download1.tw.ute.com/cs/firmware/ea300/genericSaga2.xml
    
    public static final String KEY_UTE_SERVER = "ute_url_server";
    public static final String UTE_DEFAULT_SERVER_PREFIX = "http://download1.tw.ute.com/cs/firmware";
    public static final String UTE_DEFAULT_SERVER_URL_FORMAT = "/%s%s/%s.xml";

    private static final String DEFAULT_SERVER_UROVO_HTTPS_URL = "https://yun.urovo.com:4443/Update/osUpdate";
    private static final String JANAM_SERVER_URL = "http://update.Janam.com:8000/Update/osUpdate";

    //add by urovo weiyu begin 2019-09-01 
    private static final String YDSD_DEFAULT_SERVER_URL = "http://ota.yundasys.com:8090";
    //add by urovo weiyu end 2019-09-01

    // yangkun add start 2021/07/15
    private static final boolean isRUYDE = SystemProperties.get("persist.sys.ru.yde", "false").equals("true");
    private static final String RU_YDE_DEFAULT_SERVER_URL = "https://mdm-urovo.com:443/Update/osUpdate";
    // urovo huangjiezhou add begin 2021/09/14
    private static final String CUSTOM_SERVER_URL = SystemProperties.get("persist.sys.setotaserver.url", "false");
    private static final String CUSTOM_TRUST_FILE =  SystemProperties.get("persist.sys.settrustcert.file", "none");
    // urovo huangjiezhou add end
    // add end
	
    // private static final String DEFAULT_SERVER_URL =
    // "http://192.168.8.111:8900/Update/osUpdate";
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_TASK_PROGRESS:
                    notifyProgress(msg.arg1);
                    break;
                case WHAT_TASK_COMPLETE:
                	if(mWakeLock.isHeld()){
                		mWakeLock.release();
                	}
                    File f = (File) msg.getData().getSerializable("file");
                    boolean delta = msg.getData().getBoolean("delta");
                    String version = msg.getData().getString("version");
                    notifyComplete(f, delta, version);
                    break;
                case WHAT_TASK_FAILED:
                	if(mWakeLock.isHeld()){
                		mWakeLock.release();
                	}
                    notifyFailed();
                    break;
                case WHAT_TASK_PAUSE:
                	if(mWakeLock.isHeld()){
                		mWakeLock.release();
                	}
                    notifyPause();
                    break;
                case WHAT_TASK_CANCEL:
                        if(mWakeLock.isHeld()){
                                mWakeLock.release();
                        }
                    notifyCancel();
                    break;
                case WHAT_TASK_START:
                	if(!mWakeLock.isHeld()){
                		mWakeLock.acquire();
                	}
                    notifyStart();
                    break;
                case WHAT_TASK_DOWNLOAD_STATE:
                    Log.d(TAG , "handleMessage , msg.what:" + msg.what + ",mult_flag1:" +
                                mult_flag1 + ",mult_flag2:" + mult_flag2 + ",mult_flag3:" + mult_flag3);
                    if(mult_flag1 && mult_flag2 && mult_flag3) {
                        String filePath_1 = PATH_SAVE_DIR + ".patch1.zip";
                        String filePath_2 = PATH_SAVE_DIR + ".patch2.zip";
                        String filePath_3 = PATH_SAVE_DIR + ".patch3.zip";
                        String filePath = PATH_SAVE_DIR + mUpdateInfo.getFileName();
                        mergeFile(filePath,filePath_1,filePath_2,filePath_3);
                        mult_flag1 = false;
                        mult_flag2 = false;
                        mult_flag3 = false;
                        merge_success = true;
                    }
                default:
                    break;
                case WHAT_TASK_verify:
                    if(mWakeLock.isHeld()){
                        mWakeLock.release();
                    }
                    if(enableNotification) {
                        nManager.cancel(0);
                    }
                    if(ForceUpdateManager.getInstance().getforceUpdate()){
                        Toast.makeText(mContext, R.string.download_package_damaged, Toast.LENGTH_SHORT).show();
                        //UpdateUtil.forceExit();
                    }else if(handleUpdateType){
                        Toast.makeText(mContext, R.string.download_package_damaged, Toast.LENGTH_SHORT).show();
                    }
		    if(SystemProperties.get("persist.sys.urv.support.oem", "false").equals("true")){
                        UpdateUtil.pushUpgradeResultToServer(mContext,UpdateUtil.VERIFICATION_FAILED);
		    }
                    break;
            }
        }

    };

    //add by urovo weiyu on 2019-10-30 start
    private void mergeFile(String filePath,String filePath_1,
                           String filePath_2,String filePath_3) {
        try {
            long index = 0;
            File file = new File(filePath);
            RandomAccessFile raf = null;
            if(file != null) {
                raf = new RandomAccessFile(file,"rwd");
            }
            index = getContent(filePath_1,raf,index) + index;
            Log.e(TAG,"file1："+index+"fileLength:"+raf.length());
            index = getContent(filePath_2,raf,index) + index;
            Log.e(TAG,"file2："+index+"fileLength:"+raf.length());
            getContent(filePath_3,raf,index);
            Log.d(TAG,"Download successfully! FileTotal:"+raf.length());
            raf.close();
        } catch (Exception e) {
            Log.e(TAG,"Download failed because of merge exception!");
            e.printStackTrace();
            handler.sendEmptyMessage(WHAT_TASK_FAILED);
            return;
        }
    }

    private long getContent(String filePath,RandomAccessFile raf,long index) {
        long downloadTotal = 0;
        try {
            File file = new File(filePath);
            InputStream input = null;
            if(file != null) {
                input = new FileInputStream(file);
            }
            raf.seek(index);
            byte [] Buffer = new byte[1024*1024*8];
            int length = 0;
            while((length = input.read(Buffer)) != -1) {
                raf.write(Buffer,0,length);
                downloadTotal = downloadTotal + length;
            }
            input.close();
            Log.e(TAG,"The "+ file.getName() +" finished merging,has been deleted!");
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return downloadTotal;
        }
    }


        public HttpURLConnection getHttpConnection(String urlAddress) throws IOException {

            if(urlAddress.startsWith("https")) {
                HttpsURLConnection conn = null;
                URL url = new URL(urlAddress);
                Log.e(TAG,"DownladManager doInBackground.........." + urlAddress+" isRUYDE = "+isRUYDE);
                SSLContext sslContext;
                if(Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
                    HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open("RootCert.cer"));
                    sslContext = HttpsManager.getInstance().getSSLContext(mContext);
                } else if(isRUYDE){// yangkun add 2020/10/14
					HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open("mdm_urovo_com_2021_06_27.crt"));
					sslContext = HttpsManager.getInstance().getSSLContext(mContext);
                // urovo huangjiezhou add begin 2021/09/14
                } else if(!CUSTOM_SERVER_URL.equals("false") && CUSTOM_SERVER_URL.startsWith("https")){
                    HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open(CUSTOM_TRUST_FILE));
                    sslContext = HttpsManager.getInstance().getSSLContext(mContext);
                // urovo huangjiezhou add end
				} else{
                    sslContext = Cer.getSSLContext(mContext);
                }
                conn = (HttpsURLConnection) url.openConnection();

                conn.setSSLSocketFactory(sslContext.getSocketFactory());

                conn.setHostnameVerifier(new HostnameVerifier() {

                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        // TODO Auto-generated method stub
                        return true;
                    }
                });
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(20000);
                conn.setRequestMethod("GET");

                return conn;

            } else {
                URL url = new URL(urlAddress);
                Log.e(TAG,"DownladManager doInBackground.........." + urlAddress);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(30000);

                conn.setReadTimeout(20000);

                conn.setRequestMethod("GET");

                return conn;
            }
        }

    public class DownloadFileThreadPool {
        //网络资源地址
        private String urlAddress;
        //文件存储路径
        //private String filePath;
        //线程池中有多少个线程
        private int poolLength;

        public DownloadFileThreadPool(String urlAddress,int poolLength) {
            super();
            this.urlAddress = urlAddress;
            //this.filePath = filePath;
            this.poolLength = poolLength;
        }

        public void downloadFile() {
            try {

                long fileLength = getHttpConnection(urlAddress).getContentLength();
                ExecutorService downloadPool = Executors.newFixedThreadPool(poolLength);
                long blockSize = fileLength/poolLength;
                for(int threadId = 1;threadId <= poolLength;threadId++) {
                    long startIndex = (threadId -1)*blockSize;
                    long endIndex = threadId*blockSize - 1;
                    if(threadId == poolLength) {
                        endIndex = fileLength - 1;
                    }
                    Log.e(TAG,"Thread "+threadId+":start download>>>>>>"+
                            startIndex+"<-->"+endIndex);
                    //创建下载线程类
                    DownloadFileThread downloadFileThread = new DownloadFileThread(startIndex,endIndex,
                                                                    urlAddress,threadId,blockSize,fileLength);
                    downloadPool.execute(downloadFileThread);
                }
                downloadPool.shutdown();//关闭线程池
            } catch(Exception e) {
                Log.e(TAG, "failed to download the file! " + e);
                handler.sendEmptyMessage(WHAT_TASK_FAILED);
                e.printStackTrace();
                return;
           }
        }
    }


    public class DownloadFileThread implements Runnable {
        private long startIndex;//文件下载的位置
        private long endIndex;//文件结束下载的位置
        private String urlAddress;
        private int threadId;//线程id
        private long blockSize;
        private long fileLength;

        public DownloadFileThread(long startIndex,long endIndex, String urlAddress,
                                  int threadId,long blockSize,long fileLength) {
            super();
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.urlAddress = urlAddress;
            this.threadId = threadId;
            this.blockSize = blockSize;
            this.fileLength = fileLength;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection connection = getHttpConnection(urlAddress);
                File file = null;
                if(threadId == 1) {
                    file = new File(PATH_SAVE_DIR+".patch1.zip");
                } else if (threadId == 2) {
                    file = new File(PATH_SAVE_DIR+".patch2.zip");
                } else {
                    file = new File(PATH_SAVE_DIR+".patch3.zip");
                }
                file.getParentFile().mkdirs();

                RandomAccessFile raf = null;
                if(file != null) {
                    raf = new RandomAccessFile(file,"rwd");
                } else {
                    Log.e(TAG,"Create raf failed!");
                }

                long total = 0;
                long start = startIndex;

                if(file.exists() && file.length() > 0) {//断点续传
                    total = file.length();
                    synchronized (DownloadManager.this) {
                        mult_pro += file.length();
                    }
                    startIndex = file.length() + start;//获取断点开始位置
                    Log.e(TAG,"Thread "+threadId+"startIndex:"+startIndex+"endIndex:"+endIndex);
                    if(startIndex > endIndex) {
                        Log.e(TAG,"Thread "+threadId+"has finished downloading!");
                    } else {
                        connection.setRequestProperty("Range","bytes="+startIndex+"-"+endIndex);
                    }

                    raf.seek(file.length());//将文件设置到断点位置
                    Log.e(TAG,"thread"+threadId+",startIndex:"+startIndex);
                } else {//之前没有下载，正常下载！
                    connection.setRequestProperty("Range","bytes="+startIndex+"-"+endIndex);
                    raf.seek(0);
                }

                if (connection.getResponseCode() == 404) {
                    Log.e(TAG,"Thread " + threadId +"getResponseCode == 404");
                    handler.sendEmptyMessage(WHAT_TASK_FAILED);
                    Log.w(TAG, "get http response 404!");
                    return;
                }
                InputStream is = null;
                is = connection.getInputStream();

                if(file.length() < blockSize) {
                    byte [] Buffer = new byte[1024*1024];
                    int len = 0;//记录每次循环读到Buffer中的长度
                    long downloadtotal = 0;//记录实时下载的大小
                    long fileLen = endIndex - startIndex + 1;//记录每次进来每个线程需要下载的长度

                    int process = 0;
                    int last = 0;
                    handler.obtainMessage(WHAT_TASK_PROGRESS, process, 0).sendToTarget();
                    while (true && !isMultDownloadCanced) {
                        if(fileLen - downloadtotal < Buffer.length) {
                            len = is.read(Buffer, 0, (int) (fileLen - downloadtotal));
                        } else {
                            len = is.read(Buffer);
                        }
                        raf.write(Buffer, 0, len);
                        total += len;
                        downloadtotal += len;
                        synchronized (DownloadManager.this) {
                            mult_pro += len;
                            process = (int) ((mult_pro*100)/fileLength);
                        }
                        if (process > last && !isMultDownloadCanced) {
                            last = process;
                            handler.obtainMessage(WHAT_TASK_PROGRESS, process, 0).sendToTarget();
                            Log.d("OSUpdate","process------------------->"+process);
                        }
                        if(downloadtotal == fileLen) {
                            break;
                        }
                    }
                    if (!isMultDownloadCanced) {
                        handler.obtainMessage(WHAT_TASK_PROGRESS, process, 0).sendToTarget();
                    }
                }
                if (total == (endIndex - start + 1)) {
                    if (threadId == 1) {
                        mult_flag1 = true;
                    } else if (threadId == 2) {
                        mult_flag2 = true;
                    } else {
                        mult_flag3 = true;
                    }
                    handler.removeMessages(WHAT_TASK_DOWNLOAD_STATE);
                    handler.obtainMessage(WHAT_TASK_DOWNLOAD_STATE);
                    handler.sendEmptyMessage(WHAT_TASK_DOWNLOAD_STATE);
                    Log.e(TAG,"Thread "+threadId+"finish downloading,has downloaded for "+file.length()+"!");
                } else {
                    Log.e(TAG,"Thread "+threadId+"download exception!");
                }

                raf.close();
                is.close();

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "failed to download the file! " + e);
                handler.sendEmptyMessage(WHAT_TASK_FAILED);
                return;
            }
        }
    }
    //add by urovo weiyu on 2019-10-30 end

    private DownloadManager(Context context) {
        nManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
	mSystemCR = mContext.getContentResolver();
        mPm = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "update");
        UpdateUtil.mkDeltaDir(mContext);
        if(Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
            enableNotification = false;
        }
	// urovo weiyu add on 2020-07-10 start
	IntentFilter filter = new IntentFilter();
	filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
	mContext.registerReceiver(mReceiver, filter);
	// urovo weiyu add on 2020-07-10 end
    }

    // urovo weiyu add on 2020-07-10 start
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
	    if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
	        Log.d(TAG,"connectivity changed !");
		if (!isNetworkAvailable()) {
	            handler.sendEmptyMessage(WHAT_TASK_FAILED);
		    pause(mUpdateInfo);
		}
	    }
	}
    };

    // urovo weiyu add on 2020-07-10 end
    
    public void setForceUpdate(boolean force){
    	mForceUpdate = force;
    }

    /**
     * notify the state changed
     */
    private void notifyStateChanged() {
        mContext.sendBroadcast(new Intent(ACTION_STATE_CHANGED));
    }

    /**
     * notify task start
     */
    protected void notifyStart() {
        if(enableNotification) {
            nManager.notify(0, new DownloadNotification(0, true));
        }
    }

    /**
     * notify the task pause
     */
    protected void notifyPause() {
        if(enableNotification) {
            nManager.cancel(0);
	    if (isNetworkAvailable())
		    if(handleUpdateType){
                Toast.makeText(mContext, R.string.toast_task_pause, Toast.LENGTH_SHORT).show();
            }
        }
    }

    //add by urovo weiyu on 2019-11-05 start
    /**
     * notify the task canceled
     */
    protected void notifyCancel() {
        if(enableNotification) {
            nManager.cancel(0);
            Toast.makeText(mContext, R.string.toast_task_cancel, Toast.LENGTH_SHORT).show();
        }
    }
    //add by urovo weiyu on 2019-11-05 end

    /**
     * notify the task failed
     */
    protected void notifyFailed() {
        if(enableNotification) {
            nManager.cancel(0);
        }
        if (!isNetworkAvailable()) {
	    notifyStateChanged();
            if (enableNotification)
	        Toast.makeText(mContext, R.string.msg_no_network, Toast.LENGTH_SHORT).show();
	} else if(ForceUpdateManager.getInstance().getforceUpdate()){
            Toast.makeText(mContext, R.string.toast_task_failed, Toast.LENGTH_SHORT).show();
            //UpdateUtil.forceExit();
        } else {
            if(handleUpdateType){
                Toast.makeText(mContext, R.string.toast_task_failed, Toast.LENGTH_SHORT).show();
            }
        }
        handleUpdateType = false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                .isConnectedOrConnecting();
        boolean internet = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                .isConnectedOrConnecting();
        return activeNetworkInfo != null;
    }

    /**
     * notify the task download success
     */
    protected void notifyComplete(File file, boolean delta, String version) {
        if(enableNotification) {
            nManager.cancel(0);
        }
        Intent explicitIntent = new Intent("action.OTA_DOWNLOAD_COMPLETE");
        explicitIntent.putExtra("filePathName", file.getAbsolutePath());
        explicitIntent.putExtra("osVersion", version);
        mContext.sendBroadcast(explicitIntent);
        if(Build.PWV_CUSTOM_CUSTOM.equals("RUPOS") && getServerUrl().contains("urovo")) {
            //NO TODO
            //TMS接收上面下载完成广播控制是否重启升级系统
        } else {
            Intent intent = new Intent(delta ? InstallReceiver.ACTION_REBOOT_DELTA
                : InstallReceiver.ACTION_REBOOT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
            }

            intent.putExtra("force",mForceUpdate);
            intent.setData(Uri.fromFile(file));
            intent.putExtra("version", version);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        }
    }

    /**
     * notify the task progress
     * 
     * @param progress
     */
    protected void notifyProgress(int progress) {
        if(enableNotification) {
            nManager.notify(0, new DownloadNotification(progress));
            Log.d(TAG,"notifyProgress enableNotification=true progress:"+progress);
        }
        Log.d("OSUpdate" + TAG,"notifyProgress------------------------------->"+progress);
        Intent intent = new Intent(ACTION_PROGRESS_CHANGED);
        intent.putExtra(Intent.EXTRA_INTENT, progress);
        mContext.sendBroadcast(intent);
    }

    public static DownloadManager getDefault(Context context) {
        mContext = context;
        if (instance == null) {
            instance = new DownloadManager(context);
        }
        return instance;
    }

    public static DownloadManager getDefault(Context context,boolean flag) {
        if (instance == null) {
            instance = new DownloadManager(context);
        }
        return instance;
    }

    /**
     * is there a task running
     * 
     * @return
     */
    boolean isDownloading() {
        return task != null;
    }

    public void setHandleUpdateType(boolean handleUpdate){
        handleUpdateType = handleUpdate;
    }

    /**
     * is the update file downloading now
     * 
     * @param updateInfo
     * @return
     */
    boolean isDownloading(UpdateInfo updateInfo) {
        return task != null
                && TextUtils.equals(task.update.getDownloadURL(), updateInfo.getDownloadURL());
    }

    /**
     * return the url to download the file
     * 
     * @param updateInfo
     * @return
     */
    private String getDownloadUrl(UpdateInfo updateInfo) {
        return updateInfo.getDownloadURL();
    }

    /**
     * return server url
     * 
     * @return
     */
    public String getServerUrl() {
        /*String url = sp
                .getString(
                        KEY_REMOTE,
                        Build.PWV_CUSTOM_CUSTOM.equals("Reliance") ? "http://35.154.33.194:8811/Update/osUpdate"
                                : "");
        return url.equals("") ? DEFAULT_SERVER_URL : url;
        */
        //return "https://yun.urovo.com:4443/Update/osUpdate";
        //return "https://13.127.115.168:443/Update/osUpdate";
        //地址不允许修改直接返回
        if(Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
            return "https://Otaprod.pos.jiophone.net:443/Update/osUpdate";//正式服务器
            //url = "https://Otasit.pos.jiophone.net:443/Update/osUpdate";//sit测试
            //url = "http://35.154.33.194:8811/Update/osUpdate";
        }
        //String url = sp.getString(KEY_REMOTE,"");
        String url = "";
        try {
	    url = Settings.Global.getString(mSystemCR, "FotaServerUrl");
	    Log.d(TAG,"url---------------->"+url);
        } catch(Exception e) {

        }
        if(url == null || url.equals("")) {
            if(Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
                url = "https://Otaprod.pos.jiophone.net:443/Update/osUpdate";//正式服务器
                //url = "https://Otasit.pos.jiophone.net:443/Update/osUpdate";//sit测试
                //url = "http://35.154.33.194:8811/Update/osUpdate";
            } else if(Build.PWV_CUSTOM_CUSTOM.equals("UTE") || Build.PWV_CUSTOM_CUSTOM.equals("UTEWO")) {
                url = UpdateUtil.getUTEServerUrl();//UTE_DEFAULT_SERVER_PREFIX;
            } else if(Build.PWV_CUSTOM_CUSTOM.equals("RUPOS")) {
                url = DEFAULT_RUPOS_SERVER;
            }else if(android.os.SystemProperties.get("persist.uvo.equipment","PDA").equals("POS")){
                url = DEFAULT_SERVER_UROVO_HTTPS_URL;
            }else  if (Build.PWV_CUSTOM_CUSTOM.equals("JANAM")) {   // yangkun add for JANAM 2018/09/05
                url = JANAM_SERVER_URL;
            }else if(Build.PWV_CUSTOM_CUSTOM.equals("YDSD")) {      // weiyu add for YDSD 2019-09-01
		      url = YDSD_DEFAULT_SERVER_URL;
            } else if (isRUYDE) { // yangkun add for RU-YDE 2021/07/15
			  url = RU_YDE_DEFAULT_SERVER_URL;
            // urovo add huangjiezhou begin 2021/09/14
            } else if (!CUSTOM_SERVER_URL.equals("false")){
                url =CUSTOM_SERVER_URL;
            // urovo add huangjiezhou end
	        }else {
                url = DEFAULT_SERVER_URL;
            }
        }
        return url;
    }

    /**
     * save server url
     * 
     * @param url
     * @return
     */
    public boolean saveServerUrl(String url) {
        //return sp.edit().putString(KEY_REMOTE, url).commit();
	    Log.d(TAG,"saveServerUrl url--------->"+url);
        if(url == null) {
            url = "";
        }
        try {
	        Settings.Global.putString(mSystemCR, "FotaServerUrl", url);
	        return true;
        } catch(Exception e) {

        }
        return false;
    }

    /**
     * return the url for all update files
     * 
     * @return
     */
    public String getUpdateListUrl() {
        String serverUrl = getServerUrl();
        if (serverUrl == null)
            return null;
        return serverUrl + "/updates.xml";
    }

    /**
     * download the file
     * 
     * @param updateInfo
     */
    void download(UpdateInfo updateInfo) {
	mUpdateInfo = updateInfo;
        if (isDownloading()) {
            //throw new IllegalArgumentException("there is a task downloading now!");
            Log.d(TAG,"there is a task downloading now!");
            return;
        }
        if(updateInfo.getForceUpdate() && updateInfo.getSilentUpdate()){
            enableNotification = false;
        }
        task = new DownloadTask(updateInfo);
        notifyStateChanged();
        task.execute();
    }

    /**
     * pause to download the file
     * 
     * @param updateInfo
     */
    void pause(UpdateInfo updateInfo) {
        if (!isDownloading(updateInfo)) {
            //throw new IllegalArgumentException("the task has not started!");
            Log.d(TAG,"pause:the task has not started!");
            return;
        }
        pause_or_cancel = false;
        isMultDownloadCanced = true;
        task.cancel(false);
    }

    //add by urovo weiyu on 2019-11-05

    /**
     * cancel to download the file
     *
     * @param updateInfo
     */
    void cancel(UpdateInfo updateInfo) {
        if(!isDownloading(updateInfo)) {
            //throw new IllegalArgumentException("the task has not started!");
            Log.d(TAG,"cancel:the task has not started!");
            return;
        }
        pause_or_cancel = true;
        isMultDownloadCanced = true;
        try {
            File file1 = new File(PATH_SAVE_DIR + ".patch1.zip");
            File file2 = new File(PATH_SAVE_DIR + ".patch2.zip");
            File file3 = new File(PATH_SAVE_DIR + ".patch3.zip");
            File file = new File(PATH_SAVE_DIR + mUpdateInfo.getFileName());
            if(file1.exists()) {
                file1.delete();
            }
            if(file2.exists()) {
                file2.delete();
            }
            if(file3.exists()) {
                file3.delete();
            }
            if(file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            task.cancel(false);
        }
    }
    //add by urovo weiyu on 2019-11-05

    /**
     * save tag to local used for continue download
     * 
     * @param path
     * @param info
     * @return
     */
    private boolean saveTag(String path, UpdateInfo info) {
        File f = new File(path);
        Editor editor = sp.edit();
        editor.putString(KEY_TAG_REMOTE_URL, getDownloadUrl(info));
        editor.putLong(KEY_TAG_UPDATE_SIZE, info.getSize());
        editor.putString(KEY_TAG_LOCAL_PATH, path);
        editor.putLong(KEY_TAG_LOCAL_SIZE, f.length());
        editor.putLong(KEY_TAG_LOCAL_TIME, f.lastModified());
        return editor.commit();
    }

    /**
     * clear tag
     * 
     * @return
     */
    private boolean clearTag() {
        Editor editor = sp.edit();
        editor.remove(KEY_TAG_REMOTE_URL);
        editor.remove(KEY_TAG_UPDATE_SIZE);
        editor.remove(KEY_TAG_LOCAL_PATH);
        editor.remove(KEY_TAG_LOCAL_SIZE);
        editor.remove(KEY_TAG_LOCAL_TIME);
        return editor.commit();
    }

    /**
     * get the tag last time download
     * 
     * @param path
     * @param info
     * @return
     */
    private long getTag(String path, UpdateInfo info) {
        File f = new File(path);
        if (f.exists()
                && TextUtils.equals(getDownloadUrl(info), sp.getString(KEY_TAG_REMOTE_URL, null))
                && sp.getLong(KEY_TAG_UPDATE_SIZE, 0) == info.getSize()
                && TextUtils.equals(path, sp.getString(KEY_TAG_LOCAL_PATH, null))) {
            // && f.length() == sp.getLong(KEY_TAG_LOCAL_SIZE, 0)
            // && f.lastModified() == sp.getLong(KEY_TAG_LOCAL_TIME, 0))
            return f.length();
        }
        return -1;
    }

    public void UpdatePeriod(File file,UpdateInfo update) {
        //固定时间段：  00000000_00:00:00-00000000_06:00:00
        //固定日期区间：20200807_01:00:00-20200808_23:59:59
        String period = update.getPeriod();
        //String period = "20200807_01:00:00-20200808_23:59:59";
        String startDate = period.substring(0,8);
        String endDate = period.substring(period.indexOf("-") + 1,period.indexOf("-") + 9);
        String startTime = period.substring(9,17);
        String endTime = period.substring(period.indexOf("-") + 10,period.indexOf("-") + 18);
        Log.d(TAG,"startDate-->"+startDate+" startTime-->"+startTime);
        Log.d(TAG,"endDate---->"+endDate+"   endTime---->"+endTime);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        Date mDate = new Date(System.currentTimeMillis());
        String currentDate = simpleDateFormat.format(mDate).substring(0,8);
        String currentTime = simpleDateFormat.format(mDate).substring(9,17);
        Log.d(TAG,"currentDate--->"+currentDate+" currentTime--->"+currentTime);


        if (startDate.equals("00000000")) {
            //每天固定这个时间段如果有升级包就开始更新
            if (isCurrentTimeOutOfRange(currentTime,startTime,endTime)) {
                //当前时间在指定区间，直接升级
                Log.d(TAG,"the current time is in the time-range !");
                rebootToInstall(file,update);
            } else {
                //当前时间不在指定区间，定时到区间的起始时间开始更新
                Log.d(TAG,"update in the time-range every day !");
                String[] str = new String[] {};
                str = startTime.split(":");
                scheduleUpdateService(mContext,Integer.parseInt(str[0]),Integer.parseInt(str[1]),Integer.parseInt(str[2]),file,update);
            }
        } else {
            //固定日期加时间段区间
            if (Long.parseLong(startDate+startTime.replace(":","")) <= Long.parseLong(currentDate+currentTime.replace(":","")) 
               && Long.parseLong(currentDate+currentTime.replace(":","")) <= Long.parseLong(endDate+endTime.replace(":",""))) {
                Log.d(TAG,"the current time is in the time-range");
                rebootToInstall(file,update);
            } else if (Long.parseLong(currentDate+currentTime.replace(":","")) < Long.parseLong(startDate+startTime.replace(":",""))) {
                int year = Integer.parseInt(startDate.substring(0,4));
                int month = Integer.parseInt(startDate.substring(4,2));
                int day = Integer.parseInt(startDate.substring(6,2));
                String[] str = new String[] {};
                str = startTime.split(":");
                int hour = Integer.parseInt(str[0]);
                int min = Integer.parseInt(str[2]);
                int sec = Integer.parseInt(str[3]);
                Log.d(TAG,"year-->"+year+"month-->"+month+"day-->"+day+"hour-->"+hour+"min-->"+min+"sec-->"+sec);
                scheduleUpdateService(mContext,year,month,day,hour,min,sec,file,update);
            } else if (Long.parseLong(currentDate+currentTime.replace(":","")) > Long.parseLong(endDate+endTime.replace(":",""))) {
                // 时间段已过，不升级
                Log.d(TAG,"the current time is newer than the time-range of endtime");
            }
        }
    }


    public void scheduleUpdateService(Context context, int hour, int min, int second,File file,UpdateInfo update) {

        Log.d(TAG,"scheduleUpdateService-------------------------------");
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setData(Uri.fromFile(file));
        intent.putExtra("version", update.getVersion());
        intent.putExtra("delta",update.getDelta() != null);
        intent.setAction(UpdateSettings.RANGETIMEUPDATE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Clear any old alarms and schedule the new alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        am.setRepeating(AlarmManager.RTC_WAKEUP, getTimeDiff(hour,min,second), AlarmManager.INTERVAL_DAY, pi);
    }

    public void scheduleUpdateService(Context context, int year, int month, int day, int hour, int min, int second,File file,UpdateInfo update) {

        Log.d(TAG,"scheduleUpdateService-------------------------------");
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setData(Uri.fromFile(file));
        intent.putExtra("version", update.getVersion());
        intent.putExtra("delta",update.getDelta() != null);
        intent.setAction(UpdateSettings.RANGETIMEUPDATE);
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Clear any old alarms and schedule the new alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        am.set(AlarmManager.RTC_WAKEUP, getTimeDiff(year,month,day,hour,min,second), pi);
    }

    public long getTimeDiff(int hour, int min, int second){
        Calendar ca=Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY,hour);
        ca.set(Calendar.MINUTE,min);
        ca.set(Calendar.SECOND,second);
        return ca.getTimeInMillis();
    }

    public long getTimeDiff(int year,int month,int day,int hour, int min, int second){
        Calendar ca=Calendar.getInstance();
        ca.setTimeInMillis(System.currentTimeMillis());
        ca.clear();
        ca.set(Calendar.YEAR, year);
        ca.set(Calendar.MONTH,month);
        ca.set(Calendar.DAY_OF_MONTH,day);
        ca.set(Calendar.HOUR_OF_DAY,hour);
        ca.set(Calendar.MINUTE,min);
        ca.set(Calendar.SECOND,second);
        return ca.getTimeInMillis();
    }

    public boolean isCurrentTimeOutOfRange(String currentTime,String rangeStartTime,String rangeEndTime) {
        int curTime = Integer.parseInt(currentTime.replace(":",""));
        int ranStartTime = Integer.parseInt(rangeStartTime.replace(":",""));
        int ranEndTime = Integer.parseInt(rangeEndTime.replace(":",""));
        if (ranStartTime > ranEndTime) {
            if (curTime >= ranStartTime || curTime <= ranEndTime) return true;
        } else {
            if (curTime >= ranStartTime && curTime <= ranEndTime) return true;
        }
        return false;
    }

    //add by urovo weiyu on 2019-10-31 start
    public void multExecute(final File result,final UpdateInfo update) {
        if (result != null) {
            	new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Looper.prepare();
                            // TODO Auto-generated method stub
                            RecoverySystem.verifyPackage(result, null, null);
                            Log.d(TAG, "verifyPackage name = " + result.getName() + ForceUpdateManager.getInstance().getforceUpdate() + handleUpdateType);
                            if(ForceUpdateManager.getInstance().getforceUpdate() || Build.PWV_CUSTOM_CUSTOM.startsWith("UTE") || handleUpdateType){
                                Log.d(TAG, "force update: File name = " + result.getName());
                                rebootToInstall(result,update);
                            } else if(update.getForceUpdate()){
                                if(update.getSilentUpdate()){
                                    if(update.getImmediateUpdate()){
                                        //强制静默立即升级
                                        SystemProperties.set("persist.sys.update.silence","silence_immedate");
                                        Log.e(TAG,"Force silence immediate upgrade");
                                    }else{
                                        //设置标志位
                                        if(enableNotification) {
                                            nManager.cancel(0);
                                        }
                                        Log.e(TAG,"Force silence upgrade");
                                        //强制静默非立即升级
                                        SystemProperties.set("persist.sys.update.silence","silence_noimmedate");
                                    }
                                    if (update.getPeriod() != null && !update.getPeriod().equals("") && !update.getPeriod().equals("00000000_00:00:00-00000000_00:00:00")) {
                                        UpdatePeriod(result,update);
                                    } else {
                                        rebootToInstall(result,update);
                                    }
                                }
                            } else{
                                Log.d(TAG,"period------>"+update.getPeriod());
                                if (update.getPeriod() != null && !update.getPeriod().equals("") && !update.getPeriod().equals("00000000_00:00:00-00000000_00:00:00")) {
                                    UpdatePeriod(result,update);
                                } else {
                                    rebootToInstall(result,update);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            clearTag();
                            if (result != null && result.exists()) {
                                result.delete();
                            }
                            handler.sendEmptyMessage(WHAT_TASK_verify);
                        } catch (GeneralSecurityException e) {
                            e.printStackTrace();
                            clearTag();
                            if (result != null && result.exists()) {
                                result.delete();
                            }
                            handler.sendEmptyMessage(WHAT_TASK_verify);
                        }
                        Looper.loop();
                    }
                }).start();
        }
        if (task != null) {
            task = null;
            notifyStateChanged();
        }

    }


    private void rebootToInstall(File file,UpdateInfo update) {
            Bundle data = new Bundle();
            data.putBoolean("delta", update.getDelta() != null);
            data.putString("version", update.getVersion());
            data.putSerializable("file", file);
            Message message = handler.obtainMessage(WHAT_TASK_COMPLETE);
            message.setData(data);
            message.sendToTarget();
    }
    //add by urovo weiyu on 2019-10-31 end
    class DownloadTask extends AsyncTask<Object, Object, File> {

        private UpdateInfo update;

        private float total;

        private float read;

        DownloadTask(UpdateInfo updateInfo) {
            update = updateInfo;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (task != null) {
                task = null;
                notifyStateChanged();
            }
        }

        @Override
        protected void onPostExecute(File result) {
            super.onPostExecute(result);
            if (result != null) {
                multExecute(result,update);
            } else {}
            if (task != null) {
                task = null;
                notifyStateChanged();
            }
        }
        /*
        private void rebootToInstall(File file) {
            Intent intent = new Intent("android.intent.action.MASTER_CLEAR");
            intent.putExtra("packagefile", file);
            //intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, false);
            intent.putExtra("qrdupdate", true);
            mContext.sendBroadcast(intent);
        }
        */
        @Override
        protected File doInBackground(Object... params) {
            String url = getDownloadUrl(update);
            long fileLength = 0;
	    String local = PATH_SAVE_DIR + update.getFileName();
	    File file = new File(local);
	    long tag = getTag(local, update);
	    // the file has download last time
	    if (tag == update.getSize()) {
                 return file;
            }
            try {
                fileLength = getHttpConnection(url).getContentLength();
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(WHAT_TASK_FAILED);
                return null;
            }
            if(fileLength > SINGLE_OR_MULT_THREAD_DOWNLOAD) {
                total = (float) fileLength;
                mult_download_flag = true;
                isMultDownloadCanced = false;
                handler.sendEmptyMessage(WHAT_TASK_START);
                Log.d(TAG,"The size of the OTA package is too large,use Mult-Download! ");
                mult_pro = 0;
                String filePath = PATH_SAVE_DIR + mUpdateInfo.getFileName();
                Log.d(TAG,"mUpdateInfo.getName---->"+mUpdateInfo.getFileName());
                DownloadFileThreadPool pool = new DownloadFileThreadPool(url,3);
                pool.downloadFile();
                while(true) {
                    if(merge_success) {
                        try {
                            file = new File(filePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(WHAT_TASK_FAILED);
                        }
                        multExecute(file,update);
                        merge_success = false;
                        break;
                    }
                    if (isCancelled()) {
                       if(pause_or_cancel) {
                           handler.sendEmptyMessage(WHAT_TASK_CANCEL);
                       } else {
                           handler.sendEmptyMessage(WHAT_TASK_PAUSE);
                       }
                       return null;
                    }
		    saveTag(local, update);
                }
		saveTag(local, update);
                return null;
            } else {
            handler.sendEmptyMessage(WHAT_TASK_START);

            if(url.startsWith("https")) {
                RandomAccessFile randomFile = null;
                HttpsURLConnection connection = null;
                InputStream is = null;
                FileOutputStream os = null;
                URL remoteUrl = null;
                byte buffer[] = new byte[4096];
                int readsize = 0;
                file.getParentFile().mkdirs();
                if (DEBUG) {
                    Log.d(TAG, "download file: " + url);
                    Log.d(TAG, "file path to save: " + local);
                    Log.d(TAG, "get tag: " + tag);
                }
                try {
                    //add
                    if(tag <= 0){
                        if (!file.exists()) {
                           file.createNewFile();
                        }
                    
                        remoteUrl = new URL(url);
                        Log.e(TAG,"DownladManager doInBackground.........." + url);
                        SSLContext sslContext;
                        if(Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
                            HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open("RootCert.cer"));
                            sslContext = HttpsManager.getInstance().getSSLContext(mContext);
                        } else if (isRUYDE) {
							HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open("mdm_urovo_com_2021_06_27.crt"));
							sslContext = HttpsManager.getInstance().getSSLContext(mContext);
                        // urovo huangjiezhou add begin 2021/09/14
                        } else if(!CUSTOM_SERVER_URL.equals("false") && CUSTOM_SERVER_URL.startsWith("https")){
                            HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open(CUSTOM_TRUST_FILE));
                            sslContext = HttpsManager.getInstance().getSSLContext(mContext);
                        // urovo huangjiezhou add end
						} else {
                            sslContext = Cer.getSSLContext(mContext);
                        }
                        connection = (HttpsURLConnection) remoteUrl.openConnection();
                        connection.setRequestProperty("User-Agent", "PacificHttpClient");
                        connection.setSSLSocketFactory(sslContext.getSocketFactory());
                        connection.setHostnameVerifier(new HostnameVerifier() {
                            
                              @Override
                              public boolean verify(String hostname, SSLSession session) {
                                  // TODO Auto-generated method stub
                                  return true;
                              }
                        });
                       if(randomFile == null){
                           randomFile = new RandomAccessFile(local, "rwd");
                       }
                       connection.setConnectTimeout(30000);
                       connection.setReadTimeout(20000);
                       if (connection.getResponseCode() == 404) {
                           Log.e(TAG,"getResponseCode == 404");
                           handler.sendEmptyMessage(WHAT_TASK_FAILED);
                           Log.w(TAG, "get http response 404!");
                           return null;
                       }
                       total = connection.getContentLength() + (tag != -1 ? tag : 0);
                       Log.w(TAG, "total --1-> "+total+", tag --1-> "+tag+",  update.getSize() --1-> "+update.getSize());
                       // not support for pause and restart
                       if (tag != -1 && total != update.getSize()) {
                           total = update.getSize();
                           tag = -1;
                           clearTag();
                       }
                       if (DEBUG) {
                           Log.d(TAG, "total length of the file: " + total);
                       }
                       is = connection.getInputStream();
                       os = new FileOutputStream(file, tag != -1);
                       read = tag != -1 ? tag : 0;
                       int last = 0;
                       int pro = (int) (read / total * 100);
                       handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                       while ((readsize = is.read(buffer)) > 0 && !isCancelled()) {
                           read += readsize;
                           pro = (int) (read / total * 100);
                           if (pro > last) {
                               handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                               last = pro;
                           }
                           saveTag(local, update);
                        randomFile.write(buffer, 0, readsize);
                        //os.flush();
                       }
                       handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                       // save the tag for the data has download
                       saveTag(local, update);
                       if (isCancelled()) {
                           //add by urovo weiyu on 2019-11-05 start
                           if(pause_or_cancel) {
                               handler.sendEmptyMessage(WHAT_TASK_CANCEL);
                           } else {
                               handler.sendEmptyMessage(WHAT_TASK_PAUSE);
                           }
                           //add by urovo weiyu on 2019-11-05 end
                           //handler.sendEmptyMessage(WHAT_TASK_PAUSE);
                           return null;
                       }    
                    }else{
                       long start = tag;
                       remoteUrl = new URL(url);
                       Log.e(TAG,"DownladManager doInBackground.........." + url);
                       SSLContext sslContext;
                        if(Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
                            HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open("RootCert.cer"));
                            sslContext = HttpsManager.getInstance().getSSLContext(mContext);
                        } else if (isRUYDE) {  // yangkun add 2021/07/15
							HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open("mdm_urovo_com_2021_06_27.crt"));
							sslContext = HttpsManager.getInstance().getSSLContext(mContext);
                        // urovo huangjiezhou add begin 2021/09/14
                        } else if(!CUSTOM_SERVER_URL.equals("false") && CUSTOM_SERVER_URL.startsWith("https")){
                            HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open(CUSTOM_TRUST_FILE));
                            sslContext = HttpsManager.getInstance().getSSLContext(mContext);
                        // urovo huangjiezhou add end
						} else {
                            sslContext = Cer.getSSLContext(mContext);
                        }
                       connection = (HttpsURLConnection) remoteUrl.openConnection();
                       //connection.setRequestProperty("User-Agent", "PacificHttpClient");
                        connection.setSSLSocketFactory(sslContext.getSocketFactory());
                        connection.setHostnameVerifier(new HostnameVerifier() {
                            
                              @Override
                              public boolean verify(String hostname, SSLSession session) {
                                   // TODO Auto-generated method stub
                                   return true;
                              }
                        });
                       if(randomFile == null){
                          randomFile = new RandomAccessFile(local, "rwd");
                       }
                       randomFile.seek(start);
                       connection.setRequestProperty("Range", "bytes=" + start + "-" + update.getSize());
                       //connection.setRequestProperty("User-Agent", "PacificHttpClient");
                       connection.setSSLSocketFactory(sslContext.getSocketFactory());
                       connection.setConnectTimeout(30000);
                       connection.setReadTimeout(20000);
                       if (connection.getResponseCode() == 404) {
                           handler.sendEmptyMessage(WHAT_TASK_FAILED);
                           Log.w(TAG, "get http response 404!");
                           return null;
                       }
                       total = connection.getContentLength() + (tag != -1 ? tag : 0);
                       Log.w(TAG, "total --2-> "+total+", tag --2-> "+tag+",  update.getSize() --2-> "+update.getSize());
                       // not support for pause and restart
                       /*if (tag != -1 && total != update.getSize()) {
                           total = update.getSize();
                           tag = -1;
                           clearTag();
                       } */
                       if (true) {
                           Log.d(TAG, "total length of the file: " + total);
                       }
                       is = connection.getInputStream();
                       os = new FileOutputStream(file, tag != -1);
                       read = tag != -1 ? tag : 0;
                       int last = 0;
                       int pro = (int) (read / total * 100);
                       handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                       while ((readsize = is.read(buffer)) > 0 && !isCancelled()) {
                           read += readsize;
                           pro = (int) (read / total * 100);
                           if (pro > last) {
                              handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                              last = pro;
                           }
                           saveTag(local, update);
                           randomFile.write(buffer, 0, readsize);
                           //os.flush();
                       }
                       handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                       // save the tag for the data has download
                       saveTag(local, update);
                       if (isCancelled()) {
                           //add by urovo weiyu on 2019-11-05 start
                           if(pause_or_cancel) {
                               handler.sendEmptyMessage(WHAT_TASK_CANCEL);
                           } else {
                               handler.sendEmptyMessage(WHAT_TASK_PAUSE);
                           }
                           //add by urovo weiyu on 2019-11-05 end
                           //handler.sendEmptyMessage(WHAT_TASK_PAUSE);
                           return null;
                        }
                     }
                } catch (javax.net.ssl.SSLHandshakeException e) {
                    Log.e(TAG, "failed to download the file! " + e);
                    //saveTag(local, update);
                    clearTag();
                    File f = new File(local);
                    if (f!= null && f.exists()) {
                        f.delete();
                    }
                    handler.sendEmptyMessage(WHAT_TASK_FAILED);
                    return null;
                } catch (Exception e) {
                    Log.e(TAG, "failed to download the file! " + e);
                    saveTag(local, update);
                    handler.sendEmptyMessage(WHAT_TASK_FAILED);
                    return null;
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                        }
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e) {
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return file;
            } else {
                RandomAccessFile randomFile = null;
                HttpURLConnection connection = null;
                InputStream is = null;
                FileOutputStream os = null;
                URL remoteUrl = null;
                byte buffer[] = new byte[4096];
                int readsize = 0;
                file.getParentFile().mkdirs();
                if (DEBUG) {
                    Log.d(TAG, "download file: " + url);
                    Log.d(TAG, "file path to save: " + local);
                    Log.d(TAG, "get tag: " + tag);
                }
                try {
                    if (tag <= 0) {
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        remoteUrl = new URL(url);
                        connection = (HttpURLConnection) remoteUrl.openConnection();
                        connection.setRequestProperty("User-Agent", "PacificHttpClient");
                        if (tag != -1) {
                            connection.setRequestProperty("RANGE", "bytes=" + tag + "-");
                        }
                        if (randomFile == null) {
                            randomFile = new RandomAccessFile(local, "rwd");
                        }
                        connection.setConnectTimeout(30000);
                        connection.setReadTimeout(20000);
                        if (connection.getResponseCode() == 404) {
                            handler.sendEmptyMessage(WHAT_TASK_FAILED);
                            Log.w(TAG, "get http response 404!");
                            return null;
                        }
                        total = connection.getContentLength() + (tag != -1 ? tag : 0);
                        Log.w(TAG, "total --1-> " + total + ", tag --1-> " + tag
                                + ",  update.getSize() --1-> " + update.getSize());
                        // not support for pause and restart
                        if (tag != -1 && total != update.getSize()) {
                            total = update.getSize();
                            tag = -1;
                            clearTag();
                        }
                        if (DEBUG) {
                            Log.d(TAG, "total length of the file: " + total);
                        }
                        is = connection.getInputStream();
                        os = new FileOutputStream(file, tag != -1);
                        read = tag != -1 ? tag : 0;
                        int last = 0;
                        int pro = (int) (read / total * 100);
                        handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                        while ((readsize = is.read(buffer)) > 0 && !isCancelled()) {
                            read += readsize;
                            pro = (int) (read / total * 100);
                            if (pro > last) {
                                handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                                last = pro;
                            }
                            saveTag(local, update);
                            randomFile.write(buffer, 0, readsize);
                            // os.flush();
                        }
                        handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                        // save the tag for the data has download
                        saveTag(local, update);
                        if (isCancelled()) {
                            //add by urovo weiyu on 2019-11-05 start
                           if(pause_or_cancel) {
                               handler.sendEmptyMessage(WHAT_TASK_CANCEL);
                           } else {
                               handler.sendEmptyMessage(WHAT_TASK_PAUSE);
                           }
                           //add by urovo weiyu on 2019-11-05 end
                            //handler.sendEmptyMessage(WHAT_TASK_PAUSE);
                            return null;
                        }
                    } else {
                        long start = tag;
                        remoteUrl = new URL(url);
                        Log.e(TAG, "DownladManager doInBackground.........." + url);
                        connection = (HttpURLConnection) remoteUrl.openConnection();
                        connection.setRequestProperty("Range",
                                "bytes=" + start + "-" + update.getSize());
                        if (randomFile == null) {
                            randomFile = new RandomAccessFile(local, "rwd");
                        }
                        randomFile.seek(start);
                        // connection.setRequestProperty("User-Agent",
                        // "PacificHttpClient");
                        connection.setConnectTimeout(30000);
                        connection.setReadTimeout(20000);
                        if (connection.getResponseCode() == 404) {
                            handler.sendEmptyMessage(WHAT_TASK_FAILED);
                            Log.w(TAG, "get http response 404!");
                            return null;
                        }
                        total = connection.getContentLength() + (tag != -1 ? tag : 0);
                        Log.w(TAG, "total --2-> " + total + ", tag --2-> " + tag
                                + ",  update.getSize() --2-> " + update.getSize());
                        // not support for pause and restart
                        /*
                         * if (tag != -1 && total != update.getSize()) { total =
                         * update.getSize(); tag = -1; clearTag(); }
                         */
                        if (true) {
                            Log.d(TAG, "total length of the file: " + total);
                        }
                        is = connection.getInputStream();
                        os = new FileOutputStream(file, tag != -1);
                        read = tag != -1 ? tag : 0;
                        int last = 0;
                        int pro = (int) (read / total * 100);
                        handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                        while ((readsize = is.read(buffer)) > 0 && !isCancelled()) {
                            read += readsize;
                            pro = (int) (read / total * 100);
                            if (pro > last) {
                                handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                                last = pro;
                            }
                            saveTag(local, update);
                            randomFile.write(buffer, 0, readsize);
                            // os.flush();
                        }
                        handler.obtainMessage(WHAT_TASK_PROGRESS, pro, 0).sendToTarget();
                        // save the tag for the data has download
                        saveTag(local, update);
                        if (isCancelled()) {
                            //add by urovo weiyu on 2019-11-05 start
                           if(pause_or_cancel) {
                               handler.sendEmptyMessage(WHAT_TASK_CANCEL);
                           } else {
                               handler.sendEmptyMessage(WHAT_TASK_PAUSE);
                           }
                           //add by urovo weiyu on 2019-11-05 end
                            //handler.sendEmptyMessage(WHAT_TASK_PAUSE);
                            return null;
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "failed to download the file! " + e);
                    saveTag(local, update);
                    handler.sendEmptyMessage(WHAT_TASK_FAILED);
                    return null;
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                        }
                    }
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                        }
                    }
                    if (randomFile != null) {
                        try {
                            randomFile.close();
                        } catch (IOException e) {
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
                return file;
            }
        }
      }
    }

    class DownloadNotification extends Notification {

        DownloadNotification(int progress) {
            this(progress, false);
        }

        DownloadNotification(int progress, boolean indeterminate) {
            Intent intent = new Intent(mContext, UpdateViewActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_INTENT, task.update);
            contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
            icon = android.R.drawable.stat_sys_download;
            tickerText = task.update.getFileName();
            contentView = new RemoteViews(mContext.getPackageName(), R.layout.download_notification);
            contentView.setProgressBar(R.id.download_prog, 100, progress, indeterminate);
            contentView.setTextViewText(R.id.file_name, task.update.getFileName());
            if (mult_download_flag) {
                contentView.setTextViewText(R.id.prog_text, UpdateUtil.formatSize(mult_pro) + "/"
                        + UpdateUtil.formatSize(task.total));
            } else {
                contentView.setTextViewText(R.id.prog_text, UpdateUtil.formatSize(task.read) + "/"
                        + UpdateUtil.formatSize(task.total));
            }
            flags |= Notification.FLAG_NO_CLEAR;
        }
    }

    private static String getSdCardPath() {
        File sdCardFile = null;

        ArrayList<String> devMountList = getExtSDCardPath();

        for (String devMount : devMountList) {

            File file = new File(devMount);
            return file.getAbsolutePath();

        }

        boolean sdCardExist = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdCardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            return sdCardFile.getAbsolutePath();
        }
        return "";
    }

    private static ArrayList<String> getExtSDCardPath() {
        ArrayList<String> lResult = new ArrayList<String>();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("sdcard")) {
                    String[] arr = line.split(" ");
                    String path = arr[1];
                    File file = new File(path);
                    if (file.isDirectory()) {
                        lResult.add(path);
                    }
                }
            }
            br.close();
            isr.close();
            is.close();
        } catch (Exception e) {
        }
        return lResult;
    }
}
