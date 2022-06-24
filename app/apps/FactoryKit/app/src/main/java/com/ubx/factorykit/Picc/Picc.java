package com.ubx.factorykit.Picc;

import java.util.Timer;
import java.util.TimerTask;

import android.device.PiccManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class Picc extends Activity {

    private Button bt_scan;
    private EditText tv_result;
    Context mContext = null;
    PiccManager picc = new PiccManager();
    private static final int RESULT_SCAN = 1;
    private static final String TAG = "Picc";
    private static final String DATA = "can't find any card!";
    private int speed = 57600;
    private byte addr = (byte) 0x00; 
	
    private int soundid;
    private SoundPool soundpool = null;
    private static String resultString = Utilities.RESULT_FAIL;
	
    private boolean readingDate = false;
    private Timer timer;
    private TimerTask task;
	
    Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
            case RESULT_SCAN:
                tv_result.append((String)msg.obj + "\n");
                if(!DATA.equals((String)msg.obj)){
                    if(Picc.this != null){
                        SoundTool.getMySound(Picc.this).playMusic("scan");
                    }
                }
                break;

            default:
                tv_result.setText("00 00 00 00");
                break;
            }
        }
    };
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picc);
        mContext = this;
        //notice THIS the OS4.0 DEFAULT LOG OPEN
        //	uhf = new UhfLib(speed, (byte)addr, "/dev/ttySAC3",1, mContext);
        //notice THIS the OS2.3 DEFAULT LOG OPEN
        //	uhf = new UhfLib(speed, (byte)addr, "/dev/s3c2410_serial3",1, mContext);
    }	

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //uhf.open_reader();
        //open picc 
        picc.open();
        //init picc
        //picc.initpicc();
        //uhf.SetReader_Power((byte)0x1e);
		init();
	}
	
    private void init(){
        bt_scan = (Button) findViewById(R.id.bt_read);
        tv_result = (EditText) findViewById(R.id.scan_result);
		
        bt_scan.setOnClickListener(new View.OnClickListener() {
			
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onClick()");
                if (bt_scan.getText().toString().equals(mContext.getString(R.string.read))) {
                    bt_scan.setText(R.string.stop_read);
                    tv_result.setText("");
                    timer = new Timer();
                    timer.schedule(new TimerTask() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            String result = readData();
                            Message msg = mHandler.obtainMessage(RESULT_SCAN,result);
                            mHandler.sendMessage(msg);
                            Log.d(TAG, "scan complete.");
                        }
                    }, 0, 1 * 1000);
                } else {
                    bt_scan.setText(R.string.read);
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                }
            }
        });
		
        //soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        //soundid = soundpool.load("/etc/Scan_new.ogg", 1);
        
        Button pass = (Button) findViewById(R.id.pass);
        pass.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                pass();
            }
        });
        Button fail = (Button) findViewById(R.id.fail);
        fail.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                fail(null);
            }
        });
    }
    
    void fail(Object msg) {

		setResult(RESULT_CANCELED);
		resultString = Utilities.RESULT_FAIL;
		finish();
	}

	void pass() {

		setResult(RESULT_OK);
		resultString = Utilities.RESULT_PASS;
		finish();
	}
	@Override
	public void finish() {
	    Utilities.writeCurMessage(this, TAG, resultString);
	    super.finish();
	}

	private String readData(){

		byte info[] = new byte[256];
		byte info1[] = new byte[100];
		int lenth[]=new int [1];
		byte read_buf[]=new byte[512];
		//test apdu
	    byte EMV_APDU[] = { 0x00, (byte) 0xA4, 0x04, 0x00, 0x0E, 0x32, 0x50, 0x41, 0x59,
				0x2E, 0x53, 0x59, 0x53, 0x2E, 0x44, 0x44, 0x46, 0x30, 0x31, 0x00 };
		StringBuffer result = new StringBuffer();	
		int scan_card =-1;
		byte keyBuf[]={(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff};
		int ret = 0;
	
		
		//test A\B can apdu
		byte CardType[] = new byte[2];
		byte Atq[] = new byte[16];
		char SAK = 1;
		byte[] sak = new byte[1];
		sak[0] =  (byte)SAK;
		byte SN[] = new byte [10];
		int SNLen=-1;
		scan_card = picc.request_type((byte)0x7f,CardType,Atq);
		if(CardType[0] == 'V' || CardType[0] == 'F'){
			// do nothing
		}else if(CardType[0] == 'S'){

		}else{
		   SNLen=picc.antisel(SN, sak);
		}
		StringBuffer sb = new StringBuffer();
		Log.d(TAG, "CardType[0] = "+CardType[0] + "CardType[1] ="+CardType[1] );
	//	if(SNLen>0)
	//	{
	//		scan_card=picc.ApduTransmit(EMV_APDU, 20, read_buf, lenth);
	//	}
		
//	if(SNLen >= 0)
//			scan_card=picc.M1_KeyAuth('a', 5, 6, keyBuf, SNLen, SN);
/*	
		//test request M1 not apdu
		byte CardType[] = new byte[1];
		byte Atq[] = new byte[2];
		byte SAK[] = new byte[1];
		byte SN[] = new byte [10];
		byte pReadBuf[]=new byte [100];
		int SNLen=-1;
		byte keyBuf[]={(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff,(byte) 0xff};
		scan_card=picc.RequestM1(CardType,Atq);
		SNLen=picc.AntiselM1(SN, SAK);
		if(SNLen >= 0)
			scan_card=picc.M1_KeyAuth('a', 5, 6, keyBuf, SNLen, SN);
		//if(scan_card == 0)
		//	scan_card=picc.M1_KeyAuth('a', 4, 6, keyBuf, SNLen, SN);
		//if(scan_card == 0)
			//scan_card=picc.M1_ReadBlock(4, pReadBuf);
		//if(scan_card == 0)
		//	scan_card=picc.M1_Decrement(4, 1);
		//if(scan_card == 0)
		//	scan_card=picc.M1_Increment(4, 1);
	*/		
		Log.d(TAG, "scan_card = "+scan_card);
				
		if(scan_card <= 0){
			Log.d(TAG, "can find any card!");
			return DATA;
		}
		// play sound
		//soundpool.play(soundid, 1, 1, 0, 0, 1);
		result.append("Type " + Character.toString((char) CardType[0]) + ": ");
/*		
			//test request M1 not apdu
	for (int i = 0; i <SNLen; i++){
			Log.d(TAG, "SN ["+i+"] = "+SN[i]);
			String hex = Integer.toHexString(SN[i] & 0xFF);
			if (hex.length() == 1){
				hex = '0' + hex;
			}
			
*/		
		
	//test A\B can apdu
/*		if(CardType[1]==0x0f&&CardType[0]=='A')
			result.append("A CPU Card: ");
		else if(CardType[1]==0x03&&CardType[0]=='A')
			result.append("A Mifare Card: ");
		else if(CardType[0]=='B')
			result.append("B Card: ");
		else */
//			result.append(" Card: ");
		
		if(CardType[0] == 'V' || CardType[0] == 'F'){
			for (int i = 0; i < scan_card; i++){
				Log.d(TAG, "SN ["+i+"] = "+Atq[i]);
				String hex = Integer.toHexString(Atq[i] & 0xFF);
				if (hex.length() == 1){
					hex = '0' + hex;
				}
				result.append(hex);
				System.out.print(hex.toUpperCase() + " ");
			}
		}else if(CardType[0] == 'S'){ // SRT512
			byte[] aChipID = new byte[16];
			byte[] aUID = new byte[8];
			// get out chipid
			if(scan_card > 0){
				ret = picc.srt512ChipIDGet(aChipID);
				if(ret > 0){
					for( int i = 0; i < scan_card; i++){
						ret = picc.srt512UIDGet(aChipID[i], aUID);
						if(ret == 8){
							String sUid = bytesToHexString(aUID);
							result.append("\n");
							result.append(sUid);
							result.append("\n");
							System.out.print(sUid.toUpperCase() + " " + sUid);
						}
					}
				}
			}
		}else{
			for (int i = 0; i < SNLen; i++){
				Log.d(TAG, "SN ["+i+"] = "+SN[i]);
				String hex = Integer.toHexString(SN[i] & 0xFF);
				if (hex.length() == 1){
					hex = '0' + hex;
				}
				result.append(hex);
				System.out.print(hex.toUpperCase() + " ");
			}
		}
		
		System.out.println("");
		//uhf.close_reader();
		return result.toString();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if(timer != null){
            timer.cancel();
            timer = null;
        }
//		picc.Remove();
        if(Picc.this != null){
            SoundTool.getMySound(Picc.this).release();
        }
        picc.close();
    }

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		//uhf.close_reader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.picc_menu, menu);
		return true;
	}

	public static String bytesToHexString(byte[] src, int offset, int length) {
         StringBuilder stringBuilder = new StringBuilder("");
         if (src == null || src.length <= 0) {
             return null;
         }
         for (int i = offset; i < length; i++) {
             int v = src[i] & 0xFF;
             String hv = Integer.toHexString(v);
             if (hv.length() < 2) {
                 stringBuilder.append(0);
             }
             stringBuilder.append(hv);
         }
         return stringBuilder.toString();
     }

	public static String bytesToHexString(byte[] src) {
         StringBuilder stringBuilder = new StringBuilder("");
         if (src == null || src.length <= 0) {
             return null;
         }
         for (int i = 0; i < src.length; i++) {
             int v = src[i] & 0xFF;
             String hv = Integer.toHexString(v);
             if (hv.length() < 2) {
                 stringBuilder.append(0);
             }
             stringBuilder.append(hv);
         }
         return stringBuilder.toString();
     }

}
