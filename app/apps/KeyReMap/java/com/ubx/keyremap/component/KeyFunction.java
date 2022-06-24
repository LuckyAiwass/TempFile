package com.ubx.keyremap.component;

import android.os.Bundle;
import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.device.DeviceManager;
import com.ubx.keyremap.R;

public class KeyFunction extends Activity {
    String TAG="KeyFunction";
    TextView mTextView;
    CheckBox scanCheckBox;
    CheckBox rfidCheckBox;
    private DeviceManager deviceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function);
        mTextView = (TextView) findViewById(R.id.key_code);
        scanCheckBox = (CheckBox) findViewById(R.id.checkBox1);
        rfidCheckBox = (CheckBox) findViewById(R.id.checkBox2);
        deviceManager = new DeviceManager();
        scanCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked) {
            // TODO Auto-generated method stub
                String tmp= (String) mTextView.getText();
                if(isChecked){
                    rfidset(tmp,"persist-persist.sys.scan.key");
                }else{
                    rfidget(tmp,"persist-persist.sys.scan.key");
                }
            }
        });

        rfidCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked) {
            // TODO Auto-generated method stub
                String tmp= (String) mTextView.getText();
                if(isChecked){
                    rfidset(tmp,"persist-persist.sys.rfid.key");
                }else{
                    rfidget(tmp,"persist-persist.sys.rfid.key");
                }
			 }
        });
    }

        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            Log.v(TAG, "dispatchKeyEvent " + event);
            mTextView.setText(""+event.getKeyCode());
            boolean state =true;
            String mkeycode=""+event.getKeyCode();
            String rfidkey=deviceManager.getSettingProperty("persist-persist.sys.rfid.key");
            String[] tableProperty = rfidkey.split("-");
            if(!TextUtils.isEmpty(rfidkey)){
                for(int i=0;i<tableProperty.length;i++){
                    if(mkeycode.equals(tableProperty[i])){
                        Log.v(TAG, "rfid on  mkeycode:"+mkeycode);
                        rfidCheckBox.setChecked(true);
                        state =false;
                    }
                }
                Log.v(TAG, "rfid on  state:"+state);
                if(state){
                    rfidCheckBox.setChecked(false);
                }
            }
            state =true;
            String scankey=deviceManager.getSettingProperty("persist-persist.sys.scan.key");
            String[] mtableProperty = scankey.split("-");
            if(!TextUtils.isEmpty(scankey)){
                for(int i=0;i<mtableProperty.length;i++){
                    if(mkeycode.equals(mtableProperty[i])){
                        Log.v(TAG, "scan on mkeycode:"+mkeycode);
                        scanCheckBox.setChecked(true);
                        state =false;
                    }
                }   
                Log.v(TAG, "scan on  state:"+state);
                if(state){
                    scanCheckBox.setChecked(false);
                }
            }
            return super.dispatchKeyEvent(event);
        }

        public void rfidset(String tmp,String mpersist){
            String rfidkey=deviceManager.getSettingProperty(mpersist);
            boolean state=true;
            if(!TextUtils.isEmpty(tmp)){
                if(!TextUtils.isEmpty(rfidkey)){
                    String[] tableProperty = rfidkey.split("-");
                    Log.v(TAG, "tableProperty:"+ tableProperty.length);
                    if(tableProperty.length > 0) {
                        for(int i=0;i<tableProperty.length;i++){
                            if(tmp.equals(tableProperty[i]))
                                state=false;
                        }
                    }
                    if(state){
                        Log.v(TAG, "rfid正确state:"+ rfidkey);
                        String a=rfidkey.substring(rfidkey.length()-1,rfidkey.length());
                        if(a.equals("-"))
                            rfidkey=rfidkey.substring(0,rfidkey.length()-1);
                        rfidkey=rfidkey+"-"+tmp;
                        deviceManager.setSettingProperty(mpersist,rfidkey+"-");
                    }
                }else{
                    rfidkey=rfidkey+"-"+tmp;
                    deviceManager.setSettingProperty(mpersist,rfidkey+"-");
                }
                Log.v(TAG, "rfid正确:"+ rfidkey);
            }else{
                Log.v(TAG, "rfid失败" );
            }
        }

        public void rfidget(String tmp,String mpersist){
            String newtmp="";
            String tmprfidkey=deviceManager.getSettingProperty(mpersist);
            String[] tableProperty = tmprfidkey.split("-");
            Log.v(TAG, "tmprfidkey:"+ tmprfidkey);
            if(tableProperty.length > 0) {                
                for(int i=0;i<tableProperty.length;i++){
                    if(!tmp.equals(tableProperty[i])){
                        Log.v(TAG, "tableProperty[i]:"+i+" ::"+ tableProperty[i]);
                        if(!TextUtils.isEmpty(newtmp)){
                            newtmp=newtmp+"-"+tableProperty[i];
                            Log.v(TAG, "11111newtmp:"+ newtmp);
                         }else{   
                            newtmp=tableProperty[i];
                            Log.v(TAG, "222222newtmp:"+ newtmp);
                        }
                    }
                }                
            }
            Log.v(TAG, "newtmp:"+ newtmp);
            deviceManager.setSettingProperty(mpersist,newtmp+"-");
        }
	}
