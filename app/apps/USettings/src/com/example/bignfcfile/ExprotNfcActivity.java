package com.example.bignfcfile;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.widget.TextView;
import android.os.Environment;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.provider.Settings;
import java.io.FileWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import com.urovo.bluetooth.scanner.R;

public class ExprotNfcActivity extends Activity implements NfcAdapter.CreateBeamUrisCallback {
    private NfcAdapter mNfcAdapter;
    // private  String targetFilename = "/sdcard/Custom_Local/default_Settings_property.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        TextView textView = (TextView)findViewById(R.id.nfctext);
        textView.setText(getString(R.string.apk_exprot_nfc));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNfcAdapter = mNfcAdapter.getDefaultAdapter(this);


        Log.i("urovo","mNfcAdapter:"+mNfcAdapter);
        Log.i("urovo","mNfcAdapter.isEnabled():"+mNfcAdapter.isEnabled());
        writepatch("file.txt",false);
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {

        }else{
            IsToSet(this);
            // Toast.makeText(this, "NFC 未打开，请打开NFC!!", Toast.LENGTH_SHORT).show();
        }
        mNfcAdapter.setBeamPushUrisCallback(this, this);
    }

    @Override
    public Uri[] createBeamUris(NfcEvent event) {
        Log.e("urovo","createBeamUris:");
        //File root = this.getFilesDir();
        // String rootPath = root.toString();
        //String newpath = rootPath.substring(0, rootPath.lastIndexOf("/"));
        String AUTOPATH="";

        try {
            AUTOPATH = Environment.getExternalStorageDirectory().getCanonicalPath() + "/installapps/";;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        File file=new File(AUTOPATH);
        File[] files=file.listFiles();
        boolean flag=false;
        if (files == null){Log.e("urovo","空目录");return  null;}

        Uri[] uris = new Uri[files.length];
        for(int i =0;i<files.length;i++){
            Log.e("urovo","目录:"+files[i].getAbsolutePath());
            String patch =files[i].getAbsolutePath();
            while(patch.indexOf("/")!=-1){
                patch=patch.substring(patch.indexOf("/")+1);
                Log.e("urovo","目录patch:"+patch);
            }
            writepatch(patch,true);
            Log.e("urovo","AUTOPATH+patch:"+AUTOPATH+patch);
            String targetFilename= AUTOPATH+patch;
            File file1 = new File(targetFilename);

            if (file1.exists()){
                android.os.SystemProperties.set("persist.sys.nfc.u-setting","true");
                Log.e("urovo","存在文件");
                Uri uri = Uri.parse("file://" + targetFilename);
                uris[i] = uri;
            }else {
                android.os.SystemProperties.set("persist.sys.nfc.u-setting","false");
                Toast.makeText(this, getString(R.string.text_nfc_exprot_fail), Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        return uris;

    }


    public boolean  writepatch(String content,Boolean state) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式
            String AUTOPATH = Environment.getExternalStorageDirectory().getCanonicalPath() + "/installapps/";;

            File dir = new File(AUTOPATH);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            FileWriter writer = new FileWriter( AUTOPATH +"file.txt", state);
            writer.write(content+"\r\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return  true;
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

}
