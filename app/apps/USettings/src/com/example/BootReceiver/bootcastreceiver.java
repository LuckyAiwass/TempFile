package com.example.BootReceiver;

import java.util.prefs.Preferences;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import android.text.TextUtils;
import android.widget.Toast;
import android.content.DialogInterface;
import com.example.maintools.SettingsProperty;
import android.app.AlertDialog;
import com.urovo.bluetooth.scanner.R;

public class bootcastreceiver extends BroadcastReceiver{
    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("urovo","intent.getAction():"+intent.getAction());
        if(intent.getAction().equals("android.intent.action.nfc.properties")){
            android.os.SystemProperties.set("persist.sys.nfc.u-setting","false");
        }else   if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            // SharedPreferences preferences = context.getSharedPreferences("FirstDevice", Context.MODE_PRIVATE);
            // //判断是不是首次登录，
            // if (preferences.getBoolean("firststart", true)) {
            //     SharedPreferences.Editor  editor = preferences.edit();
            //     //将登录标志位设置为false，下次登录时不在显示首次登录界面
            //     editor.putBoolean("firststart", false);
            //     editor.commit();
                try {
                    Intent intentService = new Intent("android.intent.action.BOOT_SERVICE");
                    intentService.setPackage("com.urovo.bluetooth.scanner");
                    context.startService(intentService);
                } catch (Exception e) {
                    Log.d("urovo","Start BOOT_SERVICE failed:" + e.getMessage());
                }
            // }
        }else if(intent.getAction().equals("android.intent.action.web.update")){
            //Toast.makeText(context, "导入文件！", Toast.LENGTH_LONG).show();
            mContext=context;
            String filepatch = intent.getStringExtra("patch");
            if(!TextUtils.isEmpty(filepatch)){
                File file = new File(filepatch);
                if (!file.exists()) {
                    Toast.makeText(context,mContext.getString(R.string.no_file),Toast.LENGTH_LONG).show();
                }else{

                    SettingsProperty  mSettingsProperty=new SettingsProperty(context);
                    int ret=mSettingsProperty.SetSettingProp(filepatch);
                    if(ret<0){
                        Toast.makeText(context, mContext.getString(R.string.bt_improt_fail), Toast.LENGTH_LONG).show();
                    }else if(ret==1){
                        Toast.makeText(context, mContext.getString(R.string.text_improt_success), Toast.LENGTH_LONG).show();
                    }else if(ret==0){
                        // Toast.makeText(context, "导入成功", Toast.LENGTH_LONG).show();
                        try{
                            Thread.currentThread().sleep(500);
                        }catch (InterruptedException e) {
                            System.out.print("sleep  500 error");
                        }
                        new AlertDialog.Builder(context).setTitle(mContext.getString(R.string.bt_improt_reboot))
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setPositiveButton(mContext.getString(R.string.main_ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent mintent = new Intent(Intent.ACTION_REBOOT);
                                        mintent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
                                        mContext.startActivity(mintent);
                                    }
                                })
                                .setNegativeButton(mContext.getString(R.string.main_cancel), new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();

                    }

                }
            }

        }
    }
}
