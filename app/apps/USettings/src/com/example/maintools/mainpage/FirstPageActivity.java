package com.example.maintools.mainpage;

import java.util.ArrayList;
import java.util.List;

import com.urovo.bluetooth.scanner.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.support.v4.app.FragmentTransaction;

import android.content.Intent;
import android.util.Log;
import android.net.Uri;
import android.content.Context;
import com.example.maintools.SettingsProperty;
import android.widget.Toast;

import android.view.Menu;
import android.view.MenuItem;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.app.AlertDialog;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import android.os.Environment;
import android.view.Window;
import android.os.Build;

public class FirstPageActivity extends FragmentActivity implements OnClickListener{
    private RelativeLayout rl_input;
    private RelativeLayout rl_export;
    List<Fragment> mFragments = new ArrayList<Fragment>();
    private int currentView = -1;
    private View line1;
    private View line2;
    private Fragment currentFragment;
    String TAG="wujinquan";
    Context mContext;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_first_page);
        mContext=this;
        initView();
    }

    private FragmentTransaction switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Log.i("wujinquan","targetFragment.isAdded():"+targetFragment.isAdded());
        Log.i("wujinquan","currentFragment:"+currentFragment);
        if (!targetFragment.isAdded()) {
            //第一次使用switchFragment()时currentFragment为null，所以要判断一下
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
            transaction.add(R.id.fl_container, targetFragment, targetFragment.getClass().getName());
        } else {
            transaction.hide(currentFragment).show(targetFragment);
        }
        currentFragment = targetFragment;
        return transaction;
    }

    private void initView() {
        rl_input = (RelativeLayout) findViewById(R.id.rl_input);
        rl_export = (RelativeLayout) findViewById(R.id.rl_export);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        /**
         * 加入子页面
         * */
        mFragments.add(new InputFragment());
        mFragments.add(new ExportFragment());
        rl_input.setOnClickListener(this);
        rl_export.setOnClickListener(this);
        rl_input.performClick();//初始化默认显示导入
        // switchFragment(mFragments.get(0)).commit();
    }

    /**
     * 替换Fragment
     * */
    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_container, fragment).commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_input:
                switchFragment(mFragments.get(0)).commit();
                //点击切换如果点击的和之前的不一样才做切换
            /*
			if(currentView!=-1 &&currentView!=v.getId()){
				//replaceFragment(mFragments.get(0));
				showLine(0);
			}else if(currentView == -1){
				//replaceFragment(mFragments.get(0));
				showLine(0);
			}*/
                showLine(0);
                currentView = v.getId();
                break;
            case R.id.rl_export:
                switchFragment(mFragments.get(1)).commit();
            /*
			if(currentView!=-1 &&currentView!=v.getId()){
				//replaceFragment(mFragments.get(1));
				showLine(1);
			}else if(currentView == -1){
				//replaceFragment(mFragments.get(1));
				showLine(1);
			}*/
                showLine(1);
                currentView = v.getId();
                break;

            default:
                break;
        }
    }
    /**
     * 切换TAB页面下面的横线
     * */
    private void showLine(int index){
        if(index==0){
            line1.setVisibility(View.VISIBLE);
            line2.setVisibility(View.INVISIBLE);
        }else {
            line1.setVisibility(View.INVISIBLE);
            line2.setVisibility(View.VISIBLE);
        }
    }

    private static final int FILE_SELECT_CODE = 0;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult() , resultCode: " + resultCode);
        if (resultCode != RESULT_OK) {
            Log.e(TAG, "onActivityResult() error, resultCode: " + resultCode);
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if (requestCode == FILE_SELECT_CODE) {
            Uri uri = data.getData();
            Log.i(TAG, "------->" + uri.getPath());
            SettingsProperty  mSettingsProperty=new SettingsProperty(mContext);
            int ret=mSettingsProperty.SetSettingProp(uri.getPath());
            if(ret==-1)
                Toast.makeText(this, mContext.getString(R.string.open_fail), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, mContext.getString(R.string.local_impore_success), Toast.LENGTH_SHORT).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem;
        menuItem = menu.add(Menu.NONE, R.id.menu_show_reset, 3, R.string.menu_macqrcode);
        menuItem.setIcon(R.mipmap.ic_popup_sync);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItem = menu.add(Menu.NONE, R.id.menu_show_big, 6, R.string.menu_macqrcode);
        menuItem.setIcon(R.mipmap.ic_more_default);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        mHandler = new UIHandler();
        return super.onCreateOptionsMenu(menu);
    }
    UIHandler mHandler=null;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_show_reset) {
            new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.main_reset))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setNegativeButton(mContext.getString(R.string.main_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new Thread(){
                                public void run(){
                                    try {
                                        File sdCardDir = Environment.getExternalStorageDirectory();
                                        String Patch =sdCardDir.getCanonicalPath() +"/Custom_default/default_Settings_property.xml";
                                        SettingsProperty  mSettingsProperty=new SettingsProperty(mContext);
                                        int ret=mSettingsProperty.SetSettingProp(Patch);
                                        if(ret!=0){
                                            Message msg = mHandler.obtainMessage(UIHandler.SHOW_FAIL);
                                            mHandler.sendMessage(msg);
                                        }else{
                                            Message msg = mHandler.obtainMessage(UIHandler.SHOW_SUCCESS);
                                            mHandler.sendMessage(msg);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(mContext.getString(R.string.main_cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();


            return true;
        } else if (item.getItemId() == R.id.menu_show_big) {
            new AlertDialog.Builder(mContext).setTitle(mContext.getString(R.string.main_apk))
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setNegativeButton(mContext.getString(R.string.improt_apk), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent =new Intent(FirstPageActivity.this,com.example.bignfcfile.ImprotNfcActivity.class);
                            mContext.startActivity(intent);
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(mContext.getString(R.string.exprot_apk), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent =new Intent(FirstPageActivity.this,com.example.bignfcfile.ExprotNfcActivity.class);
                            mContext.startActivity(intent);
                            dialog.dismiss();
                        }
                    }).show();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    private class UIHandler extends Handler {
        public static final int SHOW_SUCCESS   = 0;
        public static final int SHOW_FAIL = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_SUCCESS:
                    Toast.makeText(mContext, mContext.getString(R.string.reset_success), Toast.LENGTH_SHORT).show();
                    break;
                case SHOW_FAIL:
                    Toast.makeText(mContext, mContext.getString(R.string.reset_fail), Toast.LENGTH_LONG).show();
                    break;
            }

        }
    }

}
