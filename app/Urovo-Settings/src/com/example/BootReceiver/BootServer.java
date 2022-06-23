package com.example.BootReceiver;
import android.app.Notification;
import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.util.Timer;
import java.util.TimerTask;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings;
import android.text.TextUtils;
import android.device.DeviceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.media.AudioManager;

public class BootServer  extends Service{

    private static final String TAG = "fristserver";
    private AudioManager mAudioManager;
    Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.e("urovo","onStartCommand");
        mContext=getApplicationContext();
        mAudioManager = mContext.getSystemService(AudioManager.class);
        setdefault();
	DeviceManager deviceManager = new DeviceManager();
	Intent ufsIntent = new Intent("com.udroid.action.bootufs");
        mContext.sendBroadcast(ufsIntent);
	if(!"true".equals(deviceManager.getSettingProperty("System-usettings_file_exists"))){
        //WritetoXml();
	try{
	Log.d(TAG,"WritetoXml");
	exec("mkdir sdcard/Custom_default");
	exec("cp system/etc/default_Settings_property.xml sdcard/Custom_default/default_Settings_property.xml");
 }catch(Exception e){
                e.printStackTrace();
        }
	deviceManager.setSettingProperty("System-usettings_file_exists","true");
	}
        return super.onStartCommand(intent, flags, startId);
    }
    private void setdefault(){
//        Settings.System.putString(mContext.getContentResolver(), "volume_music_speaker", "15");
        Settings.System.putString(mContext.getContentResolver(), "volume_alarm_speaker", "15");
        Settings.System.putString(mContext.getContentResolver(), "volume_ring_speaker", "12");
        Settings.System.putString(mContext.getContentResolver(), "font_scale", "1.0");
        if(android.os.Build.PWV_CUSTOM_CUSTOM.equals("MYNT")) {
            Settings.System.putString(mContext.getContentResolver(), "time_12_24", "24");
        }else {
            Settings.System.putString(mContext.getContentResolver(), "time_12_24", "12");
        }

        Settings.System.putString(mContext.getContentResolver(), "device_nfc", "1");
        Settings.Secure.putString(mContext.getContentResolver(), "accessibility_display_inversion_enabled", "0");
        Settings.Global.putString(mContext.getContentResolver(), "auto_pop_softinput", "1");
        Settings.System.putString(mContext.getContentResolver(), "Glove_mode", "0");
        Settings.Global.putString(mContext.getContentResolver(), "charging_sounds_enabled","1");
        Settings.System.putString(mContext.getContentResolver(), "volume_music_speaker","12");
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 12,AudioManager.FLAG_ACTIVE_MEDIA_ONLY);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    public class LocalBinder extends Binder {
        public BootServer getService() {
            return BootServer.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public  void WritetoXml( ) {
        int i=0;
        Intent intent = new Intent("com.udroid.action.bootufs");
        try {
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                // 获取SD卡的目录
                File sdCardDir = Environment.getExternalStorageDirectory();
                String path = "/Custom_default/";
                File dir = new File(sdCardDir+path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                // FileWriter fwtmp = new  FileWriter( sdCardDir.getCanonicalPath() +path+"default_Settings_property.xml",  false );
                //  fwtmp.write("NULL"+"\r\n");
                //  fwtmp.close();



                Log.i("urovo","WritetoXml");
                FileWriter fw = new  FileWriter( sdCardDir.getCanonicalPath() +path+"default_Settings_property.xml",  false );
                fw.write("<propertygroup>"+"\r\n");
                fw.write("	<package>"+"\r\n");
                fw.write("			<packageName>com.android.providers.settings</packageName>"+"\r\n");
                Uri systemuri = android.provider.Settings.System.CONTENT_URI;
                Cursor systemcursor = mContext.getContentResolver().query(systemuri, null, null, null, null);
                while (systemcursor.moveToNext()) {
                    String  key=  systemcursor.getString(systemcursor.getColumnIndex("name"));
                    String  value=systemcursor.getString(systemcursor.getColumnIndex("value"));i++;
                    fw.write("			<table >"+"\r\n");
                    fw.write("				<name>System</name>"+"\r\n");
                    fw.write("				<property>"+"\r\n");
                    fw.write("					<key>"+key+"</key>"+"\r\n");
                    fw.write("					<value>"+value+"</value>"+"\r\n");
                    fw.write("				</property>"+"\r\n");
                    fw.write("			</table >"+"\r\n");
                    Log.i(TAG,"old System key:"+key+"              value: "+value);
                }
                systemcursor.close();

                Uri Globauri = android.provider.Settings.Global.CONTENT_URI;
                Cursor globacursor = mContext.getContentResolver().query(Globauri, null, null, null, null);
                while (globacursor.moveToNext()) {
                    String  key=  globacursor.getString(globacursor.getColumnIndex("name"));
                    String  value=globacursor.getString(globacursor.getColumnIndex("value"));  i++;
                    fw.write("			<table >"+"\r\n");
                    fw.write("				<name>Global</name>"+"\r\n");
                    fw.write("				<property>"+"\r\n");
                    fw.write("					<key>"+key+"</key>"+"\r\n");
                    fw.write("					<value>"+value+"</value>"+"\r\n");
                    fw.write("				</property>"+"\r\n");
                    fw.write("			</table >"+"\r\n");
                    Log.i(TAG,"old Globauri key:"+key+"              value: "+value);
                }
                globacursor.close();

                Uri Secureuri = android.provider.Settings.Secure.CONTENT_URI;
                Cursor Securecursor = mContext.getContentResolver().query(Secureuri, null, null, null, null);
                while (Securecursor.moveToNext()) {
                    String  key=  Securecursor.getString(Securecursor.getColumnIndex("name"));
                    String  value=Securecursor.getString(Securecursor.getColumnIndex("value"));i++;
                    fw.write("			<table >"+"\r\n");
                    fw.write("				<name>Secure</name>"+"\r\n");
                    fw.write("				<property>"+"\r\n");
                    fw.write("					<key>"+key+"</key>"+"\r\n");
                    fw.write("					<value>"+value+"</value>"+"\r\n");
                    fw.write("				</property>"+"\r\n");
                    fw.write("			</table >"+"\r\n");
                    Log.i(TAG,"old Secureuri key:"+key+"              value: "+value);
                }
                Securecursor.close();
                fw.write("	</package>"+"\r\n");
                fw.write("	<package>"+"\r\n");
                fw.write("			<packageName>com.urovo.provider.settings</packageName>"+"\r\n");
                Uri Scanuri= Uri.parse("content://com.urovo.provider.settings/settings");
                Cursor Scancursor = mContext.getContentResolver().query(Scanuri, null, null, null, null);
                while (Scancursor.moveToNext()) {
                    String  key=  Scancursor.getString(Scancursor.getColumnIndex("name"));
                    String  value=   Scancursor.getString(Scancursor.getColumnIndex("value"));  i++;
                    fw.write("			<table >"+"\r\n");
                    fw.write("				<name>settings</name>"+"\r\n");
                    fw.write("				<property>"+"\r\n");
                    fw.write("					<key>"+key+"</key>"+"\r\n");
                    fw.write("					<value>"+value+"</value>"+"\r\n");
                    fw.write("				</property>"+"\r\n");
                    fw.write("			</table >"+"\r\n");
                    Log.i(TAG,"old Scanuri key:"+key+"              value: "+value);
                }
                Scancursor.close();
                fw.write("	</package>"+"\r\n");
                fw.write("</propertygroup>"+"\r\n");
                fw.close();
            }
            mContext.sendBroadcast(intent);
        } catch (Exception e) {
            mContext.sendBroadcast(intent);
            e.printStackTrace();
        }
    }
    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        //close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    private String exec(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
