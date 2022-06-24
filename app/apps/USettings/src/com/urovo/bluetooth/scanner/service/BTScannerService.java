package com.urovo.bluetooth.scanner.service;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;
import com.urovo.bluetooth.scanner.R;
import android.content.SharedPreferences;
import android.os.Environment;
import java.util.Set;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.app.AlertDialog;
import android.graphics.Color;
import android.view.WindowManager;
import android.view.Window;
import java.lang.reflect.*;
/**
 * Created by rocky on 17-12-25.
 */
/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BTScannerService extends Service {
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    // Member object for the chat services
    private BluetoothConnectHelper mChatService = null;
    private String mConnectedDeviceName = "";
    private static final String ACTION_SCAN_DECODE_RESULT_BT = "ACTION_SCAN_DECODE_RESULT_BT";
    private static final String DECODE_DATA = "DECODE_DATA";
    String tmpaddress;
    private int trystate=0;
    private boolean sendf=true;  
    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothConnectHelper.STATE_CONNECTED:
                            Intent intent = new Intent("android.bluetooth.device.urovo.connected");
                            intent.putExtra("CONNECTED", "connected");
                            Log.i(MainApplication.TAG,"CONNECTED:");
                            sendBroadcast(intent);
                            SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                            Log.i(MainApplication.TAG,"BTexport CONNECTED:"+(preferences.getBoolean("BTexport", false)));
                            Log.i(MainApplication.TAG,"BTWrite CONNECTED:"+(preferences.getBoolean("BTWrite", false)));
                            if(preferences.getBoolean("BTexport", false) && preferences.getBoolean("BTWrite", false)){
                                SharedPreferences.Editor  editor = preferences.edit();
                                editor.commit();
                                WriteFileToBT();
                            }
                            showNotification(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothConnectHelper.STATE_CONNECTING:
                            showNotification(getString(R.string.title_connecting));
                            break;
                        case BluetoothConnectHelper.STATE_LISTEN:
                        case BluetoothConnectHelper.STATE_NONE:
                            Intent mintent = new Intent("android.bluetooth.device.urovo.connected");
                            mintent.putExtra("CONNECTED", "disconnected");
                            sendBroadcast(mintent);
                            showNotification(getString(R.string.title_not_connected));
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ://把扫码结果发送到系统ScanService，后面流程跟正常扫描头扫码流程一样处理
                    byte[] byteBarcodeData = (byte[]) msg.obj;
                    int length = msg.arg1;
                    // construct a string from the valid bytes in the buffer
                    //String result = new String(byteBarcodeData, 0,length);
                   /* 
                    SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                    Log.i(MainApplication.TAG,"RBTexport CONNECTED:"+(preferences.getBoolean("BTexport", false)));
                    Log.i(MainApplication.TAG,"BTRead CONNECTED:"+(preferences.getBoolean("BTRead", false)));
                    if(!preferences.getBoolean("BTexport", false) && preferences.getBoolean("BTRead", false)){     
                     if(byteBarcodeData != null) {
                        int[] temp_value = new int[length];
                        for (int i = 0; i < length; i++) {
                            temp_value[i] = byteBarcodeData[i] & 0xff;
                        }
                        String result = ChineseHandle.chineseBarcode(temp_value);                           
                        if(result.indexOf("Success") != -1){
                            SharedPreferences.Editor  editor = preferences.edit();   
                            editor.putBoolean("BTRead", false); 
        	                editor.commit();
                            Intent intent = new Intent("ACTION_XML_BT_IMPORT");
                            sendBroadcast(intent);
                         } 
                        result=  result.replace("Success", "");
                        result="\r\n"+result;                            
                        ReadFileToBT(result,true);
                   
                   
                    }
                     

                            
                 }*/


                    
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    String address = msg.getData().getString(Constants.DEVICE_ADDRESS);
                    showNotification("Connected to "
                            + mConnectedDeviceName);
                    tmpaddress="";
                    break;
                case Constants.MESSAGE_TOAST:
                    showNotification("####"+msg.getData().getString(Constants.TOAST));
                    stopTimer();
                    startTimer();
                    break;
            }
        }
    };


    
	public void WriteFileToBT() {
     
     new Thread(){
        public void run(){
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor  editor = preferences.edit();
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        String  mag="蓝牙数据发送成功 !";
        BufferedReader br = null;
        String str = null;
        try {
            File sdCardDir = Environment.getExternalStorageDirectory();
            String path =sdCardDir+ "/Custom_Local/default_Settings_property.xml";

            editor.putBoolean("BTWrite", false);
            mChatService.write(true,path);
            try {
                   sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                   e.printStackTrace();
               }
            mChatService.write(false,"");
         
        }catch (Exception e) {
            mag="蓝牙数据发送失败 !";
            e.printStackTrace();
        }
        Intent intent = new Intent("com.example.bluetooth.toast");
        intent.putExtra("state",mag);
        getApplicationContext().sendBroadcast(intent);        
          
        editor.putBoolean("BTWrite", true);  
        editor.commit();
        }
     }.start();
   }
    
  /*   public boolean  ReadFileToBT(String content ,boolean flag) {  
        try {  
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            File sdCardDir = Environment.getExternalStorageDirectory();
            File dir = new File(sdCardDir+"/Custom_BT/");  
            if (!dir.exists()) {  
                 dir.mkdirs();  
             }
            FileWriter writer = new FileWriter(sdCardDir.getCanonicalPath() +"/Custom_BT/default_Settings_property.xml", flag);  
            writer.write(content);  
            writer.close();  
        } catch (IOException e) {  
            e.printStackTrace();
            return false;  
        }  
            return  true;
    }*/ 
     public boolean fileIsExists(String strFile)
        {
        try {
            File f=new File(strFile);
            if(!f.exists()){
                return false;
            }
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    Timer mtimer;
    TimerTask task; 
    private void startTimer(){
        if (mtimer == null) {
             mtimer = new Timer();
        }
        showNotification("************start");
        if(task==null){            
            task = new TimerTask() {
            @Override
            public void run() {
                showNotification("************run");
              if(tmpaddress != null && !tmpaddress.equals("")) {
                     if(mChatService != null){
                      mChatService.stop();
                      mChatService.start();
                    }
                    
                    if(mChatService != null && mChatService.getState() <= BluetoothConnectHelper.STATE_LISTEN) {//当前设备未处于连接状态
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(tmpaddress);
                        int deviceClass = device.getBluetoothClass().getMajorDeviceClass();
                        String name = device.getName();
                        Log.d(MainApplication.TAG, "MESSAGE_TOAST deviceClass = " + deviceClass + " name = " + name);
                  
                        connectDevice(tmpaddress, true);
                        if(trystate>2)
                              tmpaddress="";
                        
                        trystate++;
                        } 
                    }                   

                }
            };
        }
        if(task != null && mtimer != null)
             mtimer.schedule(task, 1500);
    }

    private void stopTimer(){
        if (mtimer != null) {
            mtimer.cancel();
            mtimer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }

    }



    // The BroadcastReceiver that listens for discovered devices
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                String stateStr = "???";
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothDevice.ERROR)) {
                    case BluetoothAdapter.STATE_OFF:
                        stateStr = "off";
                        if (mChatService != null) {
                            mChatService.stop();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        stateStr = "turning on";
                        break;
                    case BluetoothAdapter.STATE_ON:
                        stateStr = "on";
                        if (mChatService != null) {
                            mChatService.start();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        stateStr = "turning off";
                        break;
                }
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();
                String address = device.getAddress();
                String stateStr = "???";
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                Log.e(MainApplication.TAG, "state:"+state);
                int deviceClass = device.getBluetoothClass().getMajorDeviceClass();
               /* if (state != BluetoothDevice.BOND_BONDED) {
                    try{
                        Method creMethod = BluetoothDevice.class.getMethod("createBond");
                        creMethod.invoke(device);
                        Log.e("urovo", "66666666");
                    }catch(Exception ex) {
                        Log.e("urovo", "Remote Exception "+ex);
                    }
                }*/
                switch (state) {
                    case BluetoothDevice.BOND_BONDED:
                        stateStr = "配对成功";
                        Log.d(MainApplication.TAG, "deviceClass =  " + deviceClass + "name: " + name + " state = " + mChatService.getState());
                        if(mChatService != null && mChatService.getState() <= BluetoothConnectHelper.STATE_LISTEN) {//当前设备未处于连接状态
                                connectDevice(address, true);
                            
                        }

                        break;
                    case BluetoothDevice.BOND_BONDING:
                        stateStr = "正在配对";
                        break;
                    case BluetoothDevice.BOND_NONE:
                        stateStr = "删除配对";
                       
                            if (mChatService != null) {
                                mChatService.stop();
                            }
                        
                        break;
                }
                Log.d(MainApplication.TAG, stateStr);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
        
                Log.d(MainApplication.TAG, "已连接");
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
                Log.d(MainApplication.TAG, "ACTION_ACL_DISCONNECT_REQUESTED");
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(MainApplication.TAG, "连接已断开");
            } else if(action.equals("ACTION_FORCE_CONNECTED_DEVICE")) {
                String address = intent.getStringExtra(Constants.DEVICE_ADDRESS);
                if(address != null && !address.equals("")) {
                    if(mChatService != null && mChatService.getState() <= BluetoothConnectHelper.STATE_LISTEN) {//当前设备未处于连接状态
                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                        int deviceClass = device.getBluetoothClass().getMajorDeviceClass();
                        String name = device.getName();
                    
                            connectDevice(address, true);
                        
                    }
                }
            }
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction("ACTION_FORCE_CONNECTED_DEVICE");//用户强制连接某个设备
        this.registerReceiver(mReceiver, filter);
        initialize();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Log.d(MainApplication.TAG, "onStartCommand");
        // If BT is not on, request that it be enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            /*Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);*/
            mBluetoothAdapter.enable();
        }
        if(mChatService == null) {
            // Initialize the BluetoothChatService to perform bluetooth connections
            mChatService = new BluetoothConnectHelper(getApplicationContext(), mHandler);
        }
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothConnectHelper.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
        sendf=true;
        if(intent != null) {
            String address = intent.getStringExtra(Constants.DEVICE_ADDRESS);
            tmpaddress=address;
            Log.d(MainApplication.TAG,"onStartCommand  address:"+address);
            if(mChatService != null){
                  mChatService.stop();
                  mChatService.start();
            }
            if(address != null && !address.equals("")) {
                if(mChatService != null && mChatService.getState() <= BluetoothConnectHelper.STATE_LISTEN) {//当前设备未处于连接状态
                    try{
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    int deviceClass = device.getBluetoothClass().getMajorDeviceClass();
                    String name = device.getName();
                    Log.d(MainApplication.TAG, "onStartCommand deviceClass = " + deviceClass + " name = " + name);
                    
                        trystate=0;                        
                        connectDevice(address, true);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                         e.printStackTrace();
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
    /**
     * Establish connection with other divice
     *
     * @param address   // Get the device MAC address
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(String address, boolean secure) {
        if(mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothConnectHelper.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
            // Get the BluetoothDevice object
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            // Attempt to connect to the device
            mChatService.connect(device, secure);
        }
    }

    private void showNotification(String message) {
        Log.d(MainApplication.TAG, message);
    }
    private void reConnectedDevice() {
        // Get currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                //mPairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                int deviceClass = device.getBluetoothClass().getMajorDeviceClass();
                String name = device.getName();
              
                    connectDevice(device.getAddress(), true);
                    break;
                
            }
        }
    }
    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(MainApplication.TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(MainApplication.TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }
    public class LocalBinder extends Binder {
        public BTScannerService getService() {
            return BTScannerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
}
