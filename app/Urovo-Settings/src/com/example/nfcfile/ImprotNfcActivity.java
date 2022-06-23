package com.example.nfcfile;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.os.Environment;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import com.example.maintools.SettingsProperty;
import android.content.Context;
import com.urovo.bluetooth.scanner.R;
import android.provider.Settings;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;

public class ImprotNfcActivity extends Activity  {
    private NfcAdapter mNfcAdapter;
    private String filepatch;
    private Context mContext;
    private NfcBroadCastReceiver mReceiver;
    private SoundPool mSoundPool;
    private int mSuccessSound;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        TextView textView = (TextView)findViewById(R.id.nfctext);
        textView.setText(getString(R.string.text_import_nfc_contact));

    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSoundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        mSuccessSound = mSoundPool.load(this, R.raw.end, 1);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.urovo.settings.nfc");
        mReceiver = new NfcBroadCastReceiver();
        this.registerReceiver(mReceiver, filter);
        android.os.SystemProperties.set("persist.sys.nfc.u-setting","false");
        mNfcAdapter = mNfcAdapter.getDefaultAdapter(this);
        Log.i("urovo","mNfcAdapter:"+mNfcAdapter);
        Log.i("urovo","mNfcAdapter.isEnabled():"+mNfcAdapter.isEnabled());
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {

        }else{
            IsToSet(this);
            //Toast.makeText(this, "NFC 未打开，请打开NFC!!", Toast.LENGTH_SHORT).show();
        }
        File sdCardDir = Environment.getExternalStorageDirectory();
        getFilesAllName(sdCardDir+"/beam/");
    }

    private  void IsToSet(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(getString(R.string.text_nfc_no_open));
        // builder.setTitle("提示");

        builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent("android.settings.NFC_SETTINGS");
                activity.startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.main_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }); builder.create().show();
    }


    public void getFilesAllName(String path) {
        File file=new File(path);
        File[] files=file.listFiles();
        boolean flag=false;
        if (files == null){Log.e("urovo","空目录");return ;}

        for(int i =0;i<files.length;i++){
            Log.e("urovo","目录:"+files[i].getAbsolutePath());
            String patch =files[i].getAbsolutePath();
            while(patch.indexOf("/")!=-1){
                patch=patch.substring(patch.indexOf("/")+1);
                Log.e("urovo","目录patch:"+patch);
            }
            if(patch.length()>=25){
                patch=patch.substring(0,25);
                Log.e("urovo","hellopatch:"+patch);
                if(patch.equals("default_Settings_property"))
                    flag=delete(files[i].getAbsolutePath());
            }
        }

    }

    public static  boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("删除文件失败:" + fileName + "不存在！");
            return false;
        } else {
            if (file.isFile()){
                if (file.delete()) {
                    System.out.println("删除单个文件" + fileName + "成功！");
                    return true;
                } else {
                    System.out.println("删除单个文件" + fileName + "失败！");
                    return false;
                }
            }
        }
        return true;
    }


    public void getFilepatch(String path) {
        File file=new File(path);
        File[] files=file.listFiles();
        boolean flag=false;
        if (files == null){Log.e("urovo","空目录");return ;}

        for(int i =0;i<files.length;i++){
            Log.e("urovo","目录:"+files[i].getAbsolutePath());
            String patch =files[i].getAbsolutePath();
            while(patch.indexOf("/")!=-1){
                patch=patch.substring(patch.indexOf("/")+1);
                Log.e("urovo","目录patch:"+patch);
            }
            if(patch.length()>=25){
                patch=patch.substring(0,25);
                Log.e("urovo","hellopatch:"+patch);
                if(patch.equals("default_Settings_property"))
                    filepatch=files[i].getAbsolutePath();
            }
        }

    }


    public class NfcBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            mContext=context;
            //Toast.makeText(context,"成功接收NFC数据",Toast.LENGTH_LONG).show();

            try{
                Thread.currentThread().sleep(1000);
            }catch (InterruptedException e) {
                System.out.print("sleep  500 error");
            }
            //mSoundPool.stop(mSuccessSound);
            File sdCardDir = Environment.getExternalStorageDirectory();
            getFilepatch(sdCardDir+"/beam/");
            File file = new File(filepatch);
            Log.i("urovo","filepatch:"+filepatch);
            if (!file.exists()) {
                Toast.makeText(ImprotNfcActivity.this,getString(R.string.text_get_nfc_fail),Toast.LENGTH_LONG).show();
            }else{

                Toast.makeText(context, getString(R.string.bt_receive), Toast.LENGTH_LONG).show();
                SettingsProperty  mSettingsProperty=new SettingsProperty(context);
                int ret=mSettingsProperty.SetSettingProp(filepatch);
                if(ret<0){
                    Toast.makeText(context, getString(R.string.bt_improt_fail), Toast.LENGTH_LONG).show();
                }else if(ret==1){

                    Toast.makeText(context, getString(R.string.text_improt_success), Toast.LENGTH_LONG).show();
                    mSoundPool.play(mSuccessSound, 1.0f, 1.0f, 0, -1, 1.0f);
                }else if(ret==0){
                    Toast.makeText(context, getString(R.string.bt_receive), Toast.LENGTH_LONG).show();
                    mSoundPool.play(mSuccessSound, 1.0f, 1.0f, 0, -1, 1.0f);
                    try{
                        Thread.currentThread().sleep(500);
                    }catch (InterruptedException e) {
                        System.out.print("sleep  500 error");
                    }
                    new AlertDialog.Builder(mContext).setTitle(getString(R.string.bt_improt_reboot))
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent mintent = new Intent(Intent.ACTION_REBOOT);
                                    mintent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
                                    mContext.startActivity(mintent);
                                }
                            })
                            .setNegativeButton(getString(R.string.main_cancel), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();


                }
                try{
                    Thread.currentThread().sleep(2222);
                }catch (InterruptedException e) {
                    System.out.print("sleep  500 error");
                }
                mSoundPool.stop(mSuccessSound);


            }
        }

    }




}
