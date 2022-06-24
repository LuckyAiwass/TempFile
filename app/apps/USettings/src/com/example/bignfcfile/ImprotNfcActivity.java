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
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.OutputStream;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

public class ImprotNfcActivity extends Activity  {
    private NfcAdapter mNfcAdapter;
    private String filepatch;
    private Context mContext;
    private NfcBroadCastReceiver mReceiver;
	private Context mcontext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        TextView textView = (TextView)findViewById(R.id.nfctext);
        mcontext=this;
        textView.setText(mcontext.getString(R.string.apk_import_nfc));
        mHandler = new UIHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(mReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();

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


    UIHandler mHandler=null;
    private class UIHandler extends Handler {
        public static final int SHOW_SUCCESS   = 0;
        public static final int SHOW_FAIL = 1;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_SUCCESS:
                    Toast.makeText(mcontext, mContext.getString(R.string.apk_write_success), Toast.LENGTH_SHORT).show();
                    break;
                case SHOW_FAIL:
                    Toast.makeText(mcontext, mContext.getString(R.string.reset_fail), Toast.LENGTH_LONG).show();
                    break;
            }

        }
    }



    public class NfcBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            mContext=context;
            Toast.makeText(context,mcontext.getString(R.string.nfc_writing),Toast.LENGTH_LONG).show();
            try{
                Thread.currentThread().sleep(1000);
            }catch (InterruptedException e) {
                System.out.print("sleep  500 error");
            }



            new Thread(new Runnable() {
                @Override
                public void run() {
                    File sdCardDir = Environment.getExternalStorageDirectory();
                    File   fileold = new File(sdCardDir+"/beam/");
                    txtstate=false;
                    search(fileold);
                    Log.i("urovo","search txtstate:"+txtstate);
                    txtstate=false;
                    File   filetxt = new File(tmppatch+"file.txt");
                    txtstate=cheakfile(filetxt);
                    Log.i("urovo","txtstate:"+txtstate);
                    if(txtstate){
                        Log.i("urovo","111tmppatch:"+tmppatch);
                        txt2String(filetxt);
                        Intent autointent = new Intent("android.intent.action.autoinstall");
                        mContext.sendBroadcast(autointent);
                        Message msg = mHandler.obtainMessage(UIHandler.SHOW_SUCCESS);
                        mHandler.sendMessage(msg);
                    }

                }
            }).start();




    		    /*if (!file.exists()) {
                    Toast.makeText(ImprotNfcActivity.this,"接收NFC数据失败，请重试！",Toast.LENGTH_LONG).show();
                }*/
        }

    }

    String tmppatch="";
    boolean txtstate=false;
    private void search(File fileold ){

        try{
            tmppatch="";
            File[] files=fileold.listFiles();
            if(files.length>0){
                for(int j=0;j<files.length;j++){
                    if(!files[j].isDirectory()){
                        if(files[j].getName().indexOf("file.txt")> -1){
                            String path =   files[j].getPath();
                            Log.i("urovo","patch:"+path);

                            path=path.substring(0,path.length()-8);
                            tmppatch=path;
                            Log.i("urovo","666patch:"+path);
                            txtstate=true;
                            Log.i("urovo","11txtstate:"+txtstate);
                        }
                    }else{
                        this.search(files[j]);
                    }
                }
            }
        } catch(Exception e){
            Log.i("urovo","Exception:"+e);
            txtstate= false;
        }

    }

    public  boolean cheakfile(File file){
        //StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                Log.i("urovo","txt2String:"+s);
                File f=new File(tmppatch+s);
                if(!f.exists())
                {
                    return false;
                }

            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;

    }



    public  boolean txt2String(File file){
        //StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                Log.i("urovo","txt2String:"+s);
                copyfile(s);
            }
            File delfile=new File(tmppatch);
            deleteFile(delfile);

            br.close();
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

    }


    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                Log.i("urovo","deleteFile:"+f);
                deleteFile(f);
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }


    public void copyfile(String filepatch) {

        InputStream is = null;
        OutputStream out = null;
        try{
            String AUTOPATH = Environment.getExternalStorageDirectory().getCanonicalPath() + "/installapps/";
            Log.i("urovo","AUTOPATH:"+AUTOPATH);
            File dir = new File(AUTOPATH);
            if (!dir.exists()) {

                dir.mkdirs();
            }

            File source = new File(tmppatch + filepatch);
            File dest = new File(AUTOPATH + filepatch);

            if(source.exists()){
                is = new FileInputStream(source);
                out = new FileOutputStream(dest);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) > 0) {
                    out.write(buffer, 0, bytesRead);
                }
                is.close();
                out.close();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }


    }

}
