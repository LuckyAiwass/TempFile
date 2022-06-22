package com.ubx.appinstall.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.content.BroadcastReceiver;
import android.os.storage.VolumeInfo;
import android.os.storage.DiskInfo;
import java.io.File;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.app.PendingIntent;
import android.os.Handler;
import android.os.Message;
import android.widget.CheckBox;
import java.util.ArrayList;
import java.io.FileInputStream;
import java.io.IOException;
import android.content.IntentFilter;

import android.widget.Toast;
import android.net.Uri;

import java.io.InputStream;
import java.io.OutputStream;
import android.hardware.usb.UsbManager;
import android.content.Intent;

import com.ubx.appinstall.util.IoUtils;
import com.ubx.appinstall.util.AutoInstallConfig;

import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageInstaller;

import com.android.internal.content.PackageHelper;

import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PackageUserState;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.content.pm.IPackageInstallObserver2;
import com.ubx.appinstall.util.SharedPrefsStrListUtil;
import java.util.List;

import com.ubx.appinstall.util.ULog;
import com.ubx.appinstall.util.PackageUtil;
import android.content.Context;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.text.TextUtils;
import com.ubx.appinstall.R;
import android.widget.CompoundButton;
import android.widget.Button;
import android.text.method.ScrollingMovementMethod;

import java.util.concurrent.SynchronousQueue;
import android.content.IIntentSender;
import android.content.IntentSender;
import android.os.IBinder;
import android.content.IIntentReceiver;
import java.util.concurrent.TimeUnit;

public class InstallActivity extends Activity {
    private static final int VOLUME_SDCARD_INDEX = 1;
    private static final String BROADCAST_ACTION = "com.android.packageinstaller.ACTION_INSTALL_COMMIT";
    private static final int VOLUME_UDIST_INDEX = 2;
    private static final int INSTALL_COMPLETE = 10000;
    private static final int INSTALL_START = 10001;
    private static final int INSTALL_DISMISS_PROGRESS = 10002;
    private List<File> installApkList = new ArrayList<>();
    private List<String> checkedApk = new ArrayList<>();
    private int[] choiceApk ;
    private TextView reScan;
    private TextView mTextVeiwInstall;
    private Button mSuccessBtn;
    private RecyclerView recyclerView;
    private FileAdapter adapter;
    private ProgressBar progress;
    private static int mProgress = 0;
    private LinearLayout progressLayout;
    private LinearLayout mainLayout;
    private PackageManager mPackageManager;
    private boolean portinstall;
    private int installed = 0;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case INSTALL_COMPLETE:
                    int status = msg.arg1;
                    String packageName = (String)msg.obj;
                    ULog.d("handleMessage , status:" + status);
                    if(status == 0) {
                        mTextVeiwInstall.setText(packageName+"  Install Success"+"\n"+ mTextVeiwInstall.getText());
                        File downloadfile = new File("/storage/emulated/0/downloadapps/"+packageName+".apk");
                        ULog.i("downloadfile path: "+"/storage/emulated/0/downloadapps/"+packageName+".apk");
                        if (downloadfile.exists()) {
                            downloadfile.delete();
                            ULog.i("delete the apk in downloadapps");
                        }
                    } else {
                        mTextVeiwInstall.setText(packageName+"  Install Failed    " + PackageUtil.installStatusToString(status)+"\n"+ mTextVeiwInstall.getText());

                        File apkfile = new File("/storage/emulated/0/installapps/"+packageName+".apk");
                        ULog.i("apkfile path: "+"/storage/emulated/0/installapps/"+packageName+".apk");
                        if (apkfile.exists()) {
                            apkfile.delete();
                            ULog.i("delete the apk in installapp");
                        }
                        File downloadfile = new File("/storage/emulated/0/downloadapps/"+packageName+".apk");
                        ULog.i("downloadfile path: "+"/storage/emulated/0/downloadapps/"+packageName+".apk");
                        if (downloadfile.exists()) {
                            downloadfile.delete();
                            ULog.i("delete the apk in downloadapps");
                        } 
                    }
                    //Toast.makeText(InstallActivity.this, packageName+" "+ PackageUtil.installStatusToString(status), Toast.LENGTH_SHORT).show();

                    if(!portinstall){
                        //installed++;
                        if(checkedApk == null || checkedApk.size() == 0){
                            mHandler.sendEmptyMessageDelayed(INSTALL_DISMISS_PROGRESS, 5000);
                            mTextVeiwInstall.setText("-----Install Complete-----\n"+ mTextVeiwInstall.getText());
                            adapter.notifyDataSetChanged();
                            mSuccessBtn.setEnabled(true);
                        }
                    }else{ 
                        mTextVeiwInstall.setText("-----Install Complete-----\n"+ mTextVeiwInstall.getText());
                        mHandler.sendEmptyMessageDelayed(INSTALL_DISMISS_PROGRESS, 5000);
                        mSuccessBtn.setEnabled(true);
                    }

                   /* switch (status) {
                        case PackageInstaller.STATUS_SUCCESS:
                            //Toast.makeText(InstallActivity.this, getString(R.string.installcomp), Toast.LENGTH_SHORT).show();
                            if(!portinstall){
                                progressLayout.setVisibility(View.GONE);
                                mainLayout.setVisibility(View.VISIBLE);
                            }else{
                                
                                finish();
                            }
                            break;
                        default:
                            //Toast.makeText(InstallActivity.this, getString(R.string.installerror), Toast.LENGTH_SHORT).show();
                            if(!portinstall){
                                progressLayout.setVisibility(View.GONE);
                                mainLayout.setVisibility(View.VISIBLE);
                            }else{
                               
                                finish();
                            }
                            break;
                    }*/
                    break;
                case INSTALL_START:
                    String pkgpath =  (String) msg.obj;
                    File pkgfile = new File(pkgpath);
                    if(msg.arg2 > 0) {
                        mTextVeiwInstall.setText("-----Start Install APP-----\t"+"  sum:"+msg.arg2+"\n"+ mTextVeiwInstall.getText());
                        mTextVeiwInstall.setText("Installing: "+pkgfile.getName()+"  rest:"+msg.arg1+"\n"+ mTextVeiwInstall.getText());
                    }else
                        mTextVeiwInstall.setText("Installing: "+pkgfile.getName()+"  rest:"+msg.arg1+"\n"+ mTextVeiwInstall.getText());
                    mSuccessBtn.setEnabled(false);
                    installAPK(pkgpath);
                    break;
                case INSTALL_DISMISS_PROGRESS:
                    if(!portinstall){
                        //installed++;
                        if(checkedApk == null || checkedApk.size() == 0){
                            /*progressLayout.setVisibility(View.GONE);
                            mainLayout.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                            mTextVeiwInstall.setText("");*/
                            finish();
                        }
                    }else{
                        finish();
                    }
                break;

                default:
                    break;
            }
        }

    };
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otg);

        reScan = (TextView) findViewById(R.id.start);
        mTextVeiwInstall = (TextView) findViewById(R.id.installing);
        mSuccessBtn = (Button) findViewById(R.id.success);
        recyclerView = (RecyclerView) findViewById(R.id.recyc_apk);
        progressLayout = (LinearLayout) findViewById(R.id.layout_progress);
        mainLayout = (LinearLayout) findViewById(R.id.layout_main);
        //progress = (ProgressBar) findViewById(R.id.progress);
        ImageView loadingImg = (ImageView) this.findViewById(R.id.loadingImg);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.loading_animation);
        loadingImg.startAnimation(animation);

        adapter = new FileAdapter(this, installApkList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        checkedApk = SharedPrefsStrListUtil.getStrListValue(this, "ADD_APK");
        mTextVeiwInstall.setMovementMethod(ScrollingMovementMethod.getInstance());
        mPackageManager = getPackageManager();

        mSuccessBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                mHandler.removeMessages(INSTALL_DISMISS_PROGRESS);
                if(!portinstall){
                        //installed++;
                        if(checkedApk == null || checkedApk.size() == 0){
                            progressLayout.setVisibility(View.GONE);
                            mainLayout.setVisibility(View.VISIBLE);
                        }
                    }else{   
                        finish();
                    }
            }
        });
        mSuccessBtn.setVisibility(View.GONE);

        reScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                installed = 0;
                checkedApk = SharedPrefsStrListUtil.getStrListValue(InstallActivity.this, "ADD_APK");
                if(checkedApk.size()==0){
                     Toast.makeText(InstallActivity.this, "Please select the file", Toast.LENGTH_SHORT).show();
                }else{
                     progressLayout.setVisibility(View.VISIBLE);
                     mainLayout.setVisibility(View.GONE);
                     // for(String path:checkedApk){
                     //     File file = new File(path);
                     //     installAPK(file);
                     // }
                     if(checkedApk != null && checkedApk.size() > 0) {
                        Message msg = mHandler.obtainMessage(INSTALL_START);
                        msg.arg1 =  checkedApk.size();
                        msg.arg2 =  checkedApk.size();
                        msg.obj = checkedApk.get(0);
                        mHandler.sendMessage(msg);
                     }

                }
            }
        });
        //串口安装
        Intent intent = getIntent();
        portinstall = intent.getBooleanExtra("portinstall",false);
        if (portinstall){
            String path = intent.getStringExtra("filepath");
            if (path != null && !path.equals("")){
                progressLayout.setVisibility(View.VISIBLE);
                mainLayout.setVisibility(View.GONE);
                //File file = new File(path);
                //installAPK(file);
                Message msg = mHandler.obtainMessage(INSTALL_START);
                msg.arg1 =  1;
                msg.arg2 =  1;
                msg.obj = path;
                mHandler.sendMessage(msg);
            }else {
                Message msg = Message.obtain();
                msg.arg1 = -1;
                msg.what = INSTALL_COMPLETE;
                mHandler.sendMessage(msg);
            }
        } else {
            boolean isDownloadApp = intent.getBooleanExtra("isDownloadApp", false);
            updateData(isDownloadApp);
        }

        installed = 0;
        checkedApk = SharedPrefsStrListUtil.getStrListValue(InstallActivity.this, "ADD_APK");
        progressLayout.setVisibility(View.VISIBLE);
        mainLayout.setVisibility(View.GONE);
        if(checkedApk != null && checkedApk.size() > 0) {
            Message msg = mHandler.obtainMessage(INSTALL_START);
            msg.arg1 =  checkedApk.size();
            msg.arg2 =  checkedApk.size();
            msg.obj = checkedApk.get(0);
            mHandler.sendMessage(msg);
        }
    }

    public void updateData(boolean isDownloadapp) {
        installApkList.clear();
        /*//没插入SD卡，外接OTG，mStorageManager.getVolumeList()的第一个才是OTG路径
        String udistpath = getUdistPath(InstallActivity.this);
        if (udistpath.equals("")){
            udistpath = getSDPath(InstallActivity.this);
        }
        //如果还是空finish()
        
        if (TextUtils.isEmpty("udistpath")){
            finish();
            Toast.makeText(this, "Not found Application!", Toast.LENGTH_SHORT).show();
        }*/
        String apkpath = "/storage/emulated/0/" + (isDownloadapp ? "downloadapps/" : "installapps/");
        ULog.d("apkpath :" + apkpath);
        File file = new File(apkpath);
        if (file != null && file.exists()) {
            checkAppsAndSize(file);
            adapter.notifyDataSetChanged();
        }

    }


    public String getSDPath(Context mcon) {
        String sd = null;
        StorageManager mStorageManager = (StorageManager) mcon.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] volumes = mStorageManager.getVolumeList();
        for (StorageVolume volume : volumes) {
            ULog.d(volume.getPath());
        }
        StorageVolume mVolume = (volumes.length > VOLUME_SDCARD_INDEX) ? volumes[VOLUME_SDCARD_INDEX] : null;
        if (mVolume != null) {
            sd = volumes[VOLUME_SDCARD_INDEX].getPath();
            return sd;
        }
        return "";
    }




    public String getUdistPath(Context mcon) {
        String ud = null;
        StorageManager mStorageManager = (StorageManager) mcon.getSystemService(Context.STORAGE_SERVICE);
        List<VolumeInfo> volumes = mStorageManager.getVolumes();
        for(VolumeInfo volumeInfo:volumes){
            //ULog.d(volume.getPath());

            //StorageVolume mVolume = (volumes.length > VOLUME_UDIST_INDEX) ? volumes[VOLUME_UDIST_INDEX] : null;
            //if (mVolume != null) {
            //  ud = volumes[VOLUME_UDIST_INDEX].getPath();
            //  return ud;
            //}
            if(volumeInfo.getType() == 0){
                DiskInfo distInfo = volumeInfo.getDisk();
                if(distInfo!=null&&distInfo.isUsb()&&volumeInfo.getPath()!=null){
                    return volumeInfo.getPath().getPath();
                }
            }
        }
        return "";
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
                        ULog.d("installApkList.add apk " + f.getName());
                        PackageManager pm = getPackageManager();
                        PackageInfo info = pm.getPackageArchiveInfo( f.getPath(),0);
                        if(info!=null){
                            installApkList.add(f);
                            checkedApk.add(f.getPath());
                            SharedPrefsStrListUtil.addStrListValue(InstallActivity.this, "ADD_APK",f.getPath());
                            ULog.d("addStrListValue , f.getPath(): "+f.getPath());
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

    private boolean installAPK(String apkFilePath) {
        File apkFile = new File(apkFilePath);
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
        //PackageParser.Package pkgInfo = getPackageInfo(packageURI);
        // if (pkgInfo == null) {
        //   ULog.i("installAPK error!!! parse AndroidManifest.xml error for " + packageURI + " !");
        //   return false;
        //}

        PackageInfo info = pm.getPackageArchiveInfo(apkFile.getPath(), PackageManager.GET_ACTIVITIES);
        ULog.i("packageURI :"+packageURI);
        String pkgName = apkFilePath.split("/")[apkFilePath.split("/").length - 1];
        try{
            File dir = new File("/storage/emulated/0/installapps/");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!apkFile.getPath().contains("/storage/emulated/0/installapps/")){
                copyFile(apkFile, new File("/storage/emulated/0/installapps/"+pkgName));
                ULog.i("copy success: "+pkgName);
            }
        } catch(IOException e) {
            ULog.i("copy failed: "+pkgName);
            e.printStackTrace();
        }
        installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
        //ULog.i("installFlags :"+installFlags);
        installPackage(apkFilePath,installFlags, installObserver);
        //pm.installPackage(packageURI, installObserver, installFlags, null);
        //doPackageStage(pm, apkFile);
        return true;
    }

    public int installPackage(String apkfile, int installFlags, final IPackageInstallObserver2 observer) {
        if (TextUtils.isEmpty(apkfile)) {
            return -1;
        }
    PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        params.installFlags |= installFlags;

        InstallThread installThread = new InstallThread(apkfile, params, observer);
        installThread.start();

        return 0;
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
        private IPackageInstallObserver2 mObserver;

        private PackageInstaller.Session session = null;
        private OutputStream outputStream = null;
        private FileInputStream inputStream = null;

        private PackageInstaller mPackageInstaller;
        public InstallThread(String apkfile, PackageInstaller.SessionParams params, final IPackageInstallObserver2 observer) {
            mApkFile = apkfile;
            mParams = params;
            mObserver = observer;
            mPackageInstaller = mPackageManager.getPackageInstaller();
        }

        @Override
        public void run() {
            super.run();
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
                        mObserver.onPackageInstalled(apkName, status,msg,null);
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
            }

        }
    }


    class PackageInstallObserver extends IPackageInstallObserver2.Stub {
    public void onUserActionRequired(Intent intent){}
    @Override
    public void onPackageInstalled(String packageName, int returnCode, String msgStr, Bundle extras) {

            //final int status = PackageManager.installStatusToPublicStatus(returnCode);
            //ULog.d("packageInstalled , returnCode:" + status);

            //mHandler.removeMessages(INSTALL_COMPLETE);
            if(portinstall){
                Message msg = Message.obtain();
                msg.arg1 = returnCode;
                msg.obj = packageName;
                msg.what = INSTALL_COMPLETE;
                mHandler.sendMessage(msg);
            } else if(checkedApk != null && checkedApk.size() > 0) {
                ULog.i("checkedApk.size(): "+checkedApk.size());
                SharedPrefsStrListUtil.removeStrListItem(InstallActivity.this, "ADD_APK",new File(checkedApk.get(0)).getAbsolutePath());
                checkedApk.remove(0);

                Message msg = Message.obtain();
                msg.arg1 = returnCode;
                msg.obj = packageName;
                msg.what = INSTALL_COMPLETE;
                mHandler.sendMessage(msg);

                if(checkedApk.size() > 0) {
                    msg = mHandler.obtainMessage(INSTALL_START);
                    msg.arg1 =  checkedApk.size();//剩余
                    msg.obj = checkedApk.get(0);
                    mHandler.sendMessage(msg);
                }
            }
        }
    }

    ;

    private void doPackageStage(File file /* , PackageInstaller.SessionParams params */) {
        ULog.d("doPackageStage , file:" + file);
        mProgress = 0;
        PackageManager pm = getPackageManager();
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
                        percent += c;
                        mProgress = (int) ((percent / (float) sizeBytes) * 100);
                        ULog.d("doPackageStage , mProgress:" + mProgress);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //progress.setProgress(mProgress);
                                if (mProgress == 100) {
                                    //Toast.makeText(InstallActivity.this, getString(R.string.installcomp), Toast.LENGTH_SHORT).show();
                                    progressLayout.setVisibility(View.GONE);
                                    mainLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        });

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
        } catch (IOException e) {
            onPackageInstalled(PackageInstaller.STATUS_FAILURE);
            ULog.d("doPackageStage , IOException");
        } finally {
            IoUtils.closeQuietly(session);
        }
    }

    void onPackageInstalled(int statusCode) {

        Toast.makeText(InstallActivity.this, "INSTALL FAILURE", Toast.LENGTH_SHORT).show();
    }

    class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Context context;
        private List<File> list;

        public FileAdapter(Context context, List<File> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.filedialog_item, null, false);
            final PowerBootViewHolder holder = new PowerBootViewHolder(view);
        
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getAdapterPosition();
                    holder.enabled.setChecked(!holder.enabled.isChecked());
                }
            });
    
        holder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int position = holder.getAdapterPosition();
                if(holder.enabled.isChecked()){
                    if(!checkedApk.contains(list.get(position).getAbsolutePath())){
                        checkedApk.add(list.get(position).getAbsolutePath());
            ULog.d("addStrListValue , list.get(position).getAbsolutePath(): "+list.get(position).getAbsolutePath());
                        SharedPrefsStrListUtil.addStrListValue(context, "ADD_APK",list.get(position).getAbsolutePath());
                    }
                }else{
                    if(checkedApk.contains(list.get(position).getAbsolutePath())){
                        ULog.d("removeStrListItem , list.get(position).getAbsolutePath(): "+list.get(position).getAbsolutePath());
                        checkedApk.remove(list.get(position).getAbsolutePath());
                        SharedPrefsStrListUtil.removeStrListItem(context, "ADD_APK",list.get(position).getAbsolutePath());
                    }
                }
            }
        });
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            PowerBootViewHolder powerBootViewHolder = (PowerBootViewHolder) holder;
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageArchiveInfo((String) list.get(position).getPath(),
                    0);
            if(info!=null&&info.applicationInfo!=null){
                info.applicationInfo.sourceDir = (String) list.get(position).getPath();
                info.applicationInfo.publicSourceDir = (String) list.get(position).getPath();
                ApplicationInfo appInfo = info.applicationInfo;
                powerBootViewHolder.icon.setImageDrawable(appInfo.loadIcon(pm));
                //powerBootViewHolder.name.setText(appInfo.loadLabel(pm).toString());
                powerBootViewHolder.name.setText(list.get(position).getAbsolutePath());
            }else{
                powerBootViewHolder.name.setText("Something Error");
            }
            if(checkedApk.contains(list.get(position).getAbsolutePath())){
 ULog.d("enabled ,"+list.get(position).getAbsolutePath()+" true");
                powerBootViewHolder.enabled.setChecked(true);
            }else{
 ULog.d("enabled ,"+list.get(position).getAbsolutePath()+" false");
                powerBootViewHolder.enabled.setChecked(false);
            }

        }

        @Override
        public int getItemCount() {
            return list == null ? 0 : list.size();
        }


        class PowerBootViewHolder extends RecyclerView.ViewHolder {

            ImageView icon;
            TextView name;
            CheckBox enabled;

            public PowerBootViewHolder(View itemView) {
                super(itemView);
                icon = (ImageView) itemView.findViewById(R.id.item_img);
                name = (TextView) itemView.findViewById(R.id.item_name);
                enabled = (CheckBox) itemView.findViewById(R.id.item_check);
            }
        }
    }

    // 复制文件
    private void copyFile(File sourceFile, File targetFile)
            throws IOException {
        // 新建文件输入流并对它进行缓冲
        FileInputStream input = new FileInputStream(sourceFile);
        BufferedInputStream inBuff = new BufferedInputStream(input);

        // 新建文件输出流并对它进行缓冲
        FileOutputStream output = new FileOutputStream(targetFile);
        BufferedOutputStream outBuff = new BufferedOutputStream(output);

        // 缓冲数组
        byte[] b = new byte[1024 * 4];
        int len;
        while ((len = inBuff.read(b)) != -1) {
            outBuff.write(b, 0, len);
        }
        // 刷新此缓冲的输出流
        outBuff.flush();

        // 关闭流
        inBuff.close();
        outBuff.close();
        output.close();
        input.close();
    }

    private static final String[] ABC_PACKAGES = {"com.abc.zacloud.abclauncher",
            "com.abc.zacloud.appmarket", "com.abc.zacloud.message.center",
            "com.abc.zacloud.parammanager", "com.abc.zacloud.settings",
            "com.abc.zacloud.zactms", "com.zacloud.deviceservice", "com.zacloud.systemservice"};

    private static final String DATA_PACKAGE_WHITELIST_FILE_PATH="/customize/app/backupslist.txt";
    public List<String> getcustomizeInstallApps() {
          ArrayList<String> list = new ArrayList();

          File    f = new File(DATA_PACKAGE_WHITELIST_FILE_PATH);
          BufferedReader reader = null;
          String line = "";
          try {
              if (f.exists()) {
                  reader = new BufferedReader(new FileReader(f));
                  while ((line = reader.readLine()) != null) {
                      line=line.trim();
                      if(!TextUtils.isEmpty(line))
                          list.add(line);
                  }
              }
             
          } catch (IOException e) {
              e.printStackTrace();
          } finally {
              if (reader != null) {
                  try {
                      reader.close();
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              }
          }
          return list;
      }

}
