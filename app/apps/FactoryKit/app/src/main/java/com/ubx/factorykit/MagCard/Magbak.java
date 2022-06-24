
package com.ubx.factorykit.MagCard;

import com.ubx.factorykit.Utilities;

import android.device.MagManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.ubx.factorykit.R;

public class Magbak extends Activity {
    private EditText mNo;
    private Button mCheck;
    private Button mAllStrip;
    private Button mFirstStrip;
    private Button mSecondStrip;
    private Button mThirdStrip;
    private Button mOpen;
    private MagManager mMagManager;
    private ToneGenerator tg = null;
    
    private static String resultString = Utilities.RESULT_FAIL;
    private static String TAG = "MagCard";
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            /*switch (msg.what) {
                case MagReadService.MESSAGE_READ_MAG:
                    beep();
                    String number = msg.getData().getString(MagReadService.CARD_NUMBER);
                    String track1 = msg.getData().getString(MagReadService.CARD_TRACK1);
                    String track2 = msg.getData().getString(MagReadService.CARD_TRACK2);
                    String track3 = msg.getData().getString(MagReadService.CARD_TRACK3);
                    String validtime = msg.getData().getString(MagReadService.CARD_VALIDTIME);
                    mNo.setText("");
                    mNo.setText(" track1: " + track1 + "\n track2: " +track2  + "\n track3: " +track3 + "\n number: " +number +"\n validtime: " +validtime);
                break;
            }*/
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.magcard);
        
        mNo = (EditText) findViewById(R.id.editText1);
        tg = new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
        //mReadService = new MagReadService(this, mHandler);
        mCheck = (Button) findViewById(R.id.button1);
        mCheck.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                readerMagCard(1, 0);
            }
        });
        
        mAllStrip = (Button) findViewById(R.id.button2);
        mAllStrip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                readerMagCard(2, 0);
            }
        });
        mFirstStrip = (Button) findViewById(R.id.button3);
        mFirstStrip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                readerMagCard(3, 1);
            }
        });
        mSecondStrip = (Button) findViewById(R.id.button4);
        mSecondStrip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                readerMagCard(3, 2);
            }
        });
        mThirdStrip = (Button) findViewById(R.id.button5);
        mThirdStrip.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                readerMagCard(3, 3);
            }
        });
        mOpen = (Button) findViewById(R.id.button6);
        mOpen.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mMagManager != null) {
                    int ret = mMagManager.open();
                    if(ret != 0) {
                        android.util.Log.i("MagReader", "Open Mag Card device faile.");
                        //mNo.append("Open Mag Card faile......." + "\n");
                        //return;
                        
                    }
                }
                    
            }
        });
        
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
    
    private void readerMagCard(int cmd, int strip) {
        /*int ret = mReader.Open();
        if(ret != 0) {
            android.util.Log.i("MagReader", "Open Mag Card device faile.");
            //mNo.append("Open Mag Card faile......." + "\n");
            //return;
            
        }*/
        int ret = mMagManager.checkCard();
        if(ret != 0) {
            android.util.Log.i("MagReader", "Now not Pay by card.");
            //mNo.append("Pay by card......." + "\n");
            //return;
        }
        if(cmd == 1) {
            mNo.append("Pay by card OK" + "\n");
            return;
        }
        String result = "";
        if(cmd == 2) {
            byte[] stripInfo = new byte[1024];
            int allLen = mMagManager.getAllStripInfo(stripInfo);
            //result = new String(stripInfo, 0, allLen);//Convert.bytesToHexString(stripInfo, 0, allLen);
            if(allLen > 0) {
                int len = stripInfo[1];
                if(len != 0)
                    result = " track1: " + new String(stripInfo, 2, len);//" track1: " + track1 + "\n track2: " +track2  + "\n track3: "
                int len2 = stripInfo[3 + len];
                if(len2 != 0)
                    result += " \ntrack2: " +new String(stripInfo, 4 + len, len2);
                int len3 = stripInfo[5 + len];
                if(len3 != 0)
                    result += " \ntrack3: " +new String(stripInfo, 6 + len+ len2, len3);
            }
            
        }else if(cmd == 3) {
            byte[] stripInfo = new byte[512];
            int len = mMagManager.getSingleStripInfo(strip, stripInfo);
             if(len==-1) {
                 // do nothing
             } else {
                 result = new String(stripInfo, 0, len);//Convert.bytesToHexString(stripInfo, 0, len);
             }
        }
        mNo.append(result + "\n");
        //mReader.Close();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        //mReadService.stop();
        if(mMagManager != null) {
        	mMagManager.close();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //mReadService.start();
        mMagManager = new MagManager();
    }
 
    private void beep() {
        if (tg != null)
            tg.startTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING);
    }
}
