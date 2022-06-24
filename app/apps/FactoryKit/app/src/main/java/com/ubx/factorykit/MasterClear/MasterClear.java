/*
 * Copyright (c) 2011, QUALCOMM Incorporated.
 * All Rights Reserved.
 * QUALCOMM Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */
package com.ubx.factorykit.MasterClear;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.Framework.MainApp;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import android.os.AsyncResult;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
//import com.qualcomm.qcnvitems.QcNvItemIds;
//import com.qualcomm.qcrilhook.QcRilHook;
//import com.qualcomm.qcrilhook.QmiOemHook;
//import com.qualcomm.qcrilhook.IQcRilHook;

public class MasterClear extends Activity {

    String TAG = "MasterClear";
    String resultString = Utilities.RESULT_PASS;
//    private static QcRilHook mQcRilOemHook;
    private int listSize = 0;
    private int length = 0;

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void finish() {

        Utilities.writeCurMessage(this, TAG, resultString);
        super.finish();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.master);

//        mQcRilOemHook = new QcRilHook(MasterClear.this);
        listSize = MainApp.getInstance().mItemList.size();

        Button confirmMaster = (Button)findViewById(R.id.confirmMaster);
        confirmMaster.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_OK);
                Utilities.writeCurMessage(MasterClear.this, TAG, resultString);
                
                for(length = 0; length < listSize; length++) {
                    if (!(((String)MainApp.getInstance().mItemList.get(length).get("result")).equals("Pass"))) break;
                }
                if (length >= listSize - 1) {
				    write_NV_2499_Succ();
                } else {
                    write_NV_2499_Fail();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // erase SDCard
                /*Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
                intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                startService(intent);
                */
                // go to master clear
                //sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));

                Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
                intent.setPackage("android");
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                sendBroadcast(intent);
                Log.d(TAG, "confirmMaster, Intent.ACTION_FACTORY_RESET");
                finish();
            }
        });
        Button confirmShutdownMaster = (Button)findViewById(R.id.confirmShutdownMaster);
        confirmShutdownMaster.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                setResult(RESULT_OK);
                Utilities.writeCurMessage(MasterClear.this, TAG, resultString);

                for(length = 0; length < listSize; length++) {
                    if (!(((String)MainApp.getInstance().mItemList.get(length).get("result")).equals("Pass"))) break;
                }
                if (length >= listSize - 1) {
				    write_NV_2499_Succ();
                } else {
                    write_NV_2499_Fail();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // erase SDCard
                /*Intent intent = new Intent(ExternalStorageFormatter.FORMAT_AND_FACTORY_RESET);
                intent.setComponent(ExternalStorageFormatter.COMPONENT_NAME);
                startService(intent);
                */
                // go to master clear
                //Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);

                Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
                intent.setPackage("android");
                intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                intent.putExtra("shutdown", true);
                sendBroadcast(intent);
                Log.d(TAG, "confirmShutdownMaster, Intent.ACTION_FACTORY_RESET");
                finish();
            }
        });
    }

    private void write_NV_2499_Succ(){
/*
        try{
	 		ByteBuffer buf = ByteBuffer.allocate(26);
         	buf.order(ByteOrder.LITTLE_ENDIAN);
         	buf.putInt(2499);
         	buf.putInt(1);
         	buf.putShort((short)(26));
	 		AsyncResult result = mQcRilOemHook.sendQcRilHookMsg(IQcRilHook.QCRILHOOK_NV_WRITE, buf.array());
	        if (result.exception != null) {
                result.exception.printStackTrace();
                throw new IOException();
            }
		} catch(IOException e) {
			e.printStackTrace();	
		}
*/
    }
    private void write_NV_2499_Fail(){
/*
        try{
            ByteBuffer buf1 = ByteBuffer.allocate(26);
            buf1.order(ByteOrder.LITTLE_ENDIAN);
            buf1.putInt(2499);
            buf1.putInt(0);
            buf1.putShort((short)(26));
            AsyncResult result1 = mQcRilOemHook.sendQcRilHookMsg(IQcRilHook.QCRILHOOK_NV_WRITE, buf1.array());
            if (result1.exception != null) {
                result1.exception.printStackTrace();
                throw new IOException();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
*/
    }

}
