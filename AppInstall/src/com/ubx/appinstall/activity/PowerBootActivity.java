package com.ubx.appinstall.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.view.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.ubx.appinstall.R;
import com.ubx.appinstall.util.ULog;
import com.ubx.appinstall.adapter.PowerBootAdapter;
import com.ubx.appinstall.bean.PowerBootBean;
import com.ubx.appinstall.util.AutoInstallConfig;
import com.ubx.appinstall.util.SharedPrefsStrListUtil;

public class PowerBootActivity extends Activity {


    private RecyclerView powerbootRecyc;
    private PowerBootAdapter adapter;
    public PackageManager pm;

    private List<PowerBootBean> list = new ArrayList<>();
    private List<String> pmList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_boot);
        pm = getPackageManager();
        powerbootRecyc = (RecyclerView)findViewById(R.id.powerboot_recyc);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        powerbootRecyc.setLayoutManager(linearLayoutManager);
        adapter = new PowerBootAdapter(this,list);
        powerbootRecyc.setAdapter(adapter);
        pmList = SharedPrefsStrListUtil.getStrListValue(this, "POWERBOOT");
        ULog.d(" pmList.size():" + pmList.size());
        initData();

    }

    private void initData() {
        File file = new File(AutoInstallConfig.AUTOINSTALLPATH);
        if (file != null && file.exists()&&file.isDirectory()) {
            checkAppsAndSize(file);
        }

        adapter.notifyDataSetChanged();

    }
    
    @Override
       public boolean onKeyDown(int keyCode, KeyEvent event) {
           if(keyCode==KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN) {
               InputStream is = null;
               OutputStream out = null;
               try{
                   File root = this.getFilesDir();
                   String rootPath = root.toString();
                   String newpath = rootPath.substring(0, rootPath.lastIndexOf("/"));
                   File source = new File(newpath + "/shared_prefs/AutoInstall.xml");
                   File dest = new File(AutoInstallConfig.AUTOINSTALLPATH + "/AutoInstall.xml");
                   ULog.d("source.path() = " + newpath + "/shared_prefs/AutoInstall.xml");
                   ULog.d("source.exists()" + source.exists());
                   if(source.exists()){
                        ULog.d("copying!");
                        is = new FileInputStream(source);
                        out = new FileOutputStream(dest);
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) > 0) {
                            out.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        out.close();
                        ULog.d("success!"); 
                   }
                }catch (IOException e) {
                    e.printStackTrace();
                }
           }
           return super.onKeyDown(keyCode, event);
       }
    private void checkAppsAndSize(File file) {
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (File f : files) {
                if (f != null && f.exists()) {
                    if (f.isDirectory()) {
                        checkAppsAndSize(f);
                    } else if (f.isFile() && f.getAbsolutePath().toLowerCase().endsWith(".apk")
                            && isInstalled(pm.getPackageArchiveInfo(f.getPath(),0).packageName)) {
                        PowerBootBean bean = new PowerBootBean();
                        PackageInfo pi = pm.getPackageArchiveInfo(f.getPath(),0);
                        pi.applicationInfo.sourceDir = f.getPath();
                        pi.applicationInfo.publicSourceDir = f.getPath();
                        for(int i = 0;i < pmList.size();i++){
                            if(pi.packageName.equals(pmList.get(i))){
                                bean.setNum(i+1);
                                ULog.d(pi.packageName + "   num: "+i);
                            }
                        }
                        bean.setPackageInfo(pi);
                        list.add(bean);
                    } else {

                    }
                } else {

                }
            }
        }
    }

    private boolean isInstalled(String pkgName) {
        List<PackageInfo> pinfo = getPackageManager().getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName.toLowerCase();
                if (pn.equals(pkgName.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}
