package com.ubx.appinstall.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.provider.Settings;
import android.content.ComponentName;
import android.app.Activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ubx.appinstall.service.AutoInstallService;
import com.ubx.appinstall.service.StartActivityService;
import com.ubx.appinstall.util.AutoInstallConfig;
import com.ubx.appinstall.util.CallbackBundle;
import com.ubx.appinstall.util.IoUtils;
import com.ubx.appinstall.util.LoadingView;
import com.ubx.appinstall.util.OpenFileDialog;
import com.ubx.appinstall.util.ServiceMenuKeySequence;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PackageUserState;

import com.ubx.appinstall.util.ULog;
import com.ubx.appinstall.R;
import com.ubx.appinstall.util.SharedPrefsStrListUtil;

import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageInstaller;
import android.widget.Toast;
import android.view.KeyEvent;
import com.android.internal.content.PackageHelper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

    private RelativeLayout selectAuto = null;
    private RelativeLayout checkPkg = null;
    private RelativeLayout addDir = null;
    private RelativeLayout powerBoot = null;
    private RelativeLayout portInstall = null;
    private RelativeLayout otgInstall = null;
    private RelativeLayout scanInstall = null;
    private CheckBox mCheckBox = null;
    private TextView installTitle = null;
    private TextView installContent = null;
    private TextView addTitle = null;
    private TextView addContent = null;
    private TextView powerBootTitle = null;
    private TextView powerBootContent = null;
    private TextView portInstallTitle = null;
    private TextView portInstallContent = null;
    private TextView otgInstallTitle = null;
    private TextView otgInstallContent = null;
    private RelativeLayout startCopy = null;
    private Switch startLog = null;
    private boolean logcating;
    private TextView scanInstallTitle = null;
    private TextView scanInstallContent = null;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    //private static boolean mPermissionReqProcessed = false;
    private static final String KEY_AUTO_INSTALL_APPS = "persist.auto.install.apps";
    private static final String KEY_LOGCATING = "persist.catching.log";
    private static final String UROVO_LOGGER_PERSSION = "smartpos.deviceservice.permission.DeviceInfo";
    private String systemLogFile;
    private String kernelLogFile;
    private String isCheck;
    private String islogcat;
    private boolean firstStart = true;
    private long mLastClickTime = 0;
    public static final long TIME_INTERVAL = 1000L;
    LoadingView customDialog;
    private ServiceMenuKeySequence mServiceInvoker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();

    }

    private void initView() {
        selectAuto = (RelativeLayout) findViewById(R.id.select_auto);
        mCheckBox = (CheckBox) findViewById(R.id.checkview);
        installTitle = (TextView) findViewById(R.id.install_title);
        installContent = (TextView) findViewById(R.id.install_content);
        checkPkg = (RelativeLayout) findViewById(R.id.install_update);
        addDir = (RelativeLayout) findViewById(R.id.add_path);
        addTitle = (TextView) findViewById(R.id.add_title);
        addContent = (TextView) findViewById(R.id.add_content);
        powerBoot = (RelativeLayout) findViewById(R.id.powerboot);
        powerBootTitle = (TextView) findViewById(R.id.powerboot_title);
        powerBootContent = (TextView) findViewById(R.id.powerboot_content);
        portInstall = (RelativeLayout) findViewById(R.id.portinstall);
        portInstallTitle = (TextView) findViewById(R.id.portinstall_title);
        portInstallContent = (TextView) findViewById(R.id.portinstall_content);
        otgInstall = (RelativeLayout) findViewById(R.id.otginstall);
        otgInstallTitle = (TextView) findViewById(R.id.otginstall_title);
        otgInstallContent = (TextView) findViewById(R.id.otginstall_content);
        scanInstall =  (RelativeLayout) findViewById(R.id.scan_install);
        scanInstallTitle = (TextView) findViewById(R.id.scan_install_title);
        scanInstallContent = (TextView) findViewById(R.id.scan_install_content);

        startLog = (Switch) findViewById(R.id.control_log);
        startCopy = (RelativeLayout) findViewById(R.id.copy_log);

        //portInstall.setVisibility(View.GONE);
        //selectAuto.setVisibility(View.GONE);
        //checkPkg.setVisibility(View.GONE);
        addDir.setVisibility(View.GONE);
        //powerBoot.setVisibility(View.GONE);
        //startLog.setVisibility(View.GONE);
        //startCopy.setVisibility(View.GONE);
    }

    private void initData() {
        customDialog = new LoadingView(this, "Copying...");
        isCheck = Settings.System.getString(getContentResolver(), KEY_AUTO_INSTALL_APPS);
        //islogcat = Settings.System.getString(getContentResolver(), KEY_LOGCATING);
        if (isCheck == null){
            isCheck = "true";
            Settings.System.putString(getContentResolver(), KEY_AUTO_INSTALL_APPS, "true");
        }
        ULog.d("isCheck   --------------------------->" + isCheck);
        SharedPrefsStrListUtil.putStringValue(this, "isCheck", isCheck);
        if (isCheck != null) {
            mCheckBox.setChecked("true".equals(isCheck) ? true : false);
        }

        String islogcat = SharedPrefsStrListUtil.getStringValue(this, "islogcat", "false");
        if ("true".equals(islogcat)){
            startLog.setChecked(true);
        }else {
            startLog.setChecked(false);
        }

        if (mServiceInvoker == null){
            mServiceInvoker = new ServiceMenuKeySequence();
        }
        mServiceInvoker.setOnInvokeServiceMenuListener(new ServiceMenuKeySequence.OnInvokeServiceMenuListener() {
            public void onServiceMenu() {
                startLog.setVisibility(View.VISIBLE);
                startCopy.setVisibility(View.VISIBLE);
            }
        });
        String installDir = AutoInstallConfig.AUTOINSTALLPATH;
        File dir = new File(installDir);
        if (!dir.exists()) {
            dir.mkdirs();
            ULog.d("mkdirs: " + installDir);
        }
    }

    private void initListener() {
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){ 
            @Override 
            public void onCheckedChanged(CompoundButton buttonView, 
                    boolean isChecked) {
                // TODO Auto-generated method stub 
                Settings.System.putString(getContentResolver(), KEY_AUTO_INSTALL_APPS, isChecked ? "true" : "false");
            }
        }); 

        if (checkPkg != null) {
            checkPkg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub
                    /*ULog.d("onCreate checkPkg onClick:" + mCheckBox.isChecked());
                    startAutoInstallService();*/
            Intent intent = new Intent(MainActivity.this, InstallActivity.class);
                    startActivity(intent);
                }
            });
        }

        addDir.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                ULog.d("----------------------");
                Dialog dialog = OpenFileDialog.createDialog(0, MainActivity.this, "Manage Directory");
                dialog.show();
            }
        });

        powerBoot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MainActivity.this, PowerBootActivity.class);
                startActivity(intent);
            }
        });
        portInstall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                //Intent intent = new Intent(MainActivity.this,PortInstallActivity.class);
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.udroid.portinstall", "com.udroid.portinstall.MainActivity"));
                startActivity(intent);
            }
        });
        otgInstall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MainActivity.this, OTGActivity.class);
                startActivity(intent);
            }
        });

    scanInstall.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View arg0) {
        // TODO Auto-generated method stub
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo network = cm.getActiveNetworkInfo();
        if(network != null){
            Intent intent = new Intent(MainActivity.this,com.zxing.activity.CaptureActivity.class);
            startActivity(intent);
        }else{
            Toast.makeText(MainActivity.this, "Please connect to the network", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent("android.settings.WIFI_SETTINGS");
            startActivity(intent);
        }
        }
    });
        startLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                systemLogFile = "SystemLog_" + getCurrentDate();
                SharedPrefsStrListUtil.putStringValue(MainActivity.this, "islogcat", b ? "true":"false");
                if (b) {
                    logcating = true;
                    Intent intent = new Intent("action.LOG_CONTROL_SERVICE");
                    intent.putExtra("option", 1);
                    intent.putExtra("android", true);
                    intent.putExtra("kernel", false);
                    intent.putExtra("androidFile", systemLogFile);
                    intent.putExtra("fileMaxSize", 5);
                    ULog.d("Start to cat log");
                    sendBroadcast(intent, UROVO_LOGGER_PERSSION);
                } else {
                    logcating = false;
                    Intent intent = new Intent("action.LOG_CONTROL_SERVICE");
                    intent.putExtra("option", 0);
                    intent.putExtra("android", true);
                    intent.putExtra("kernel", true);
                    ULog.d("end to cat log");
                    sendBroadcast(intent, UROVO_LOGGER_PERSSION);
                    if (!TextUtils.isEmpty(getUdistPath(MainActivity.this)))
                        showCopyDialog();
                }
            }
        });
        startCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long nowTime = System.currentTimeMillis();
                if (nowTime - mLastClickTime > TIME_INTERVAL) {
                    // do something
                    mLastClickTime = nowTime;
                } else {
                    return;
                }

                if (logcating) {
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.str_logcating), Toast.LENGTH_SHORT).show();
                } else {
                    if (!TextUtils.isEmpty(getUdistPath(MainActivity.this))) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                startCopytoSdcard("sdcard/Ulog", getUdistPath(MainActivity.this) + "/Ulog");
                            }
                        }).start();
                    } else
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.str_no_udist), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showCopyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.str_title));
        builder.setMessage(getResources().getString(R.string.str_content));
        builder.setNegativeButton(getResources().getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                arg0.dismiss();
            }
        });
        builder.setPositiveButton(getResources().getString(R.string.str_ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO Auto-generated method stub
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startCopytoSdcard("sdcard/Ulog", getUdistPath(MainActivity.this) + "/Ulog");
                    }
                }).start();
            }
        });
        builder.create().show();// 使用show()方法显示对话框

    }


    public void startCopytoSdcard(String src, String des) {
        showLoading(true);
        try {
            File file1 = new File(src);
            File[] fs = file1.listFiles();
            File file2 = new File(des);
            ULog.d("startCopytoSdcard" + file1.getAbsolutePath() + "         " + file2.getAbsolutePath());
            if (!file2.exists()) {
                file2.mkdirs();
            }
            if (fs.length == 0){
                showToast( getResources().getString(R.string.str_copy_nofile));
                showLoading(false);
                return;
            }
            showToast(getResources().getString(R.string.str_copy_start));
            for (File f : fs) {
                if (f.isFile()) {
                    fileCopy(f.getPath(), des + "/" + f.getName());
                } else if (f.isDirectory()) {
                    startCopytoSdcard(f.getPath(), des + "/" + f.getName());
                }
            }
            Intent intent = new Intent("action.LOG_CONTROL_SERVICE");
            intent.putExtra("option", 2);
            sendBroadcast(intent, UROVO_LOGGER_PERSSION);
            showToast( getResources().getString(R.string.str_copy_finish));
            showLoading(false);


        } catch (Exception e) {
            e.printStackTrace();
            showToast(getResources().getString(R.string.str_copy_error));
            showLoading(false);
        }
        //copy end and delete file

    }

    private void showToast(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IoUtils.showMessage(MainActivity.this,msg);
                //Toast.makeText(MainActivity.this,msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showLoading(final boolean status){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (status){
                    customDialog.show();
                    customDialog.start();
                }else {
                    customDialog.stop();
                    customDialog.dismiss();
                }
            }
        });
    }

    /**
     * 文件复制的具体方法
     */
    private void fileCopy(String src, String des) {
        ULog.d("fileCopy :" + src + "         " + des);

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src));
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(des));
            int i = -1;
            byte[] bt = new byte[2014];
            while ((i = bis.read(bt)) != -1) {
                bos.write(bt, 0, i);
            }
            bis.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getResources().getString(R.string.str_copy_error), Toast.LENGTH_SHORT).show();
            showLoading(false);
        }
    }

    public String getSDPath(Context mcon) {
        String sd = null;
        StorageManager mStorageManager = (StorageManager) mcon.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] volumes = mStorageManager.getVolumeList();
        for (StorageVolume volume : volumes) {
            ULog.d(volume.getPath());
        }
        StorageVolume mVolume = (volumes.length > 1) ? volumes[1] : null;
        if (mVolume != null) {
            sd = volumes[1].getPath();
            return sd;
        }
        return "";
    }


    public String getUdistPath(Context mcon) {
        String ud = null;
        StorageManager mStorageManager = (StorageManager) mcon.getSystemService(Context.STORAGE_SERVICE);
        List<VolumeInfo> volumes = mStorageManager.getVolumes();
        for (VolumeInfo volumeInfo : volumes) {
            //ULog.d(volume.getPath());

            //StorageVolume mVolume = (volumes.length > VOLUME_UDIST_INDEX) ? volumes[VOLUME_UDIST_INDEX] : null;
            //if (mVolume != null) {
            //  ud = volumes[VOLUME_UDIST_INDEX].getPath();
            //  return ud;
            //}
            if (volumeInfo.getType() == 0) {
                DiskInfo distInfo = volumeInfo.getDisk();
                if (distInfo != null && distInfo.isUsb() && volumeInfo.getPath() != null) {
                    return volumeInfo.getPath().getPath();
                }
            }
        }
        return "";
    }


    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }

    /*
        @SuppressLint("NewApi")
        private void checkPermissions() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // int i = ContextCompat.checkSelfPermission(this, permissions[0]);
                int i = this.getApplicationContext().checkSelfPermission(permissions[0]);
                if (i != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions();
                    mPermissionReqProcessed = false;
                } else {
                    mPermissionReqProcessed = true;
                }
            } else {
                mPermissionReqProcessed = true;
            }
        }

        private void requestPermissions() {
            this.requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_EXTERNAL_STORAGE);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            if (requestCode == REQUEST_EXTERNAL_STORAGE) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // Storage-related task you need to do.
                    mPermissionReqProcessed = true;
                    startAutoInstallService();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // finish();
                }
            }

        }
    */
    private void startAutoInstallService() {
        /*if (!mPermissionReqProcessed) {
            ULog.d("startAutoInstallService requestPermissions");
            //checkPermissions();
            return;
        }*/
        if (selectAuto != null && mCheckBox.isChecked()) {
            Intent intent = new Intent(this, AutoInstallService.class);
            this.startService(intent);
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(mServiceInvoker != null && mServiceInvoker.keyIn(keyCode)) {
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

}
