/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.Keypad;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.integer;
import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Util;
import com.ubx.factorykit.Utilities;
import com.ubx.factorykit.Values;
import com.ubx.factorykit.Framework.MainApp;
import com.view.AutoLineFeedLinearLayout;

import android.provider.Settings;
import android.view.WindowManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import static com.ubx.factorykit.Framework.FactoryKitPro.IS_SQ47C;

public class Keypad extends Activity {

    private static final String TAG = "Keypad";
    private static String resultString = Utilities.RESULT_FAIL;
    private static Context mContext;
    private int itemIndex = -1;
    static final int MODE_KB_NUM = 0;
    static final int MODE_KB_LOWER = 1;
    static final int MODE_KB_UPPER = 2;
    static final int MODE_KB_SYMBOL = 3;
    
    private final int[] KEYMODE0 = { KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN };
    private final int[] KEYMODE1 = { KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_CAMERA };
    private final int[] KEYMODE2 = { KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_CAMERA,
            KeyEvent.KEYCODE_FOCUS };
    private final int[][] KEYMODE = { KEYMODE0, KEYMODE1, KEYMODE2 };

    int[] keyMode;
    HashMap<Integer, Boolean> keyStatusHashMap = new HashMap<Integer, Boolean>();
    private List<KeyInfo> mKeyInfo = new ArrayList();
    private boolean mIsSQ27 = false;
    private boolean mIsSQ51 = false;
    private boolean mIsSQ42 = false;
    private boolean mSQ26TB=false;
    private boolean mSQ46=false;
    private boolean mIsSQ27TD= false;
    private boolean mIsSQ52 = false;
    private TextView tvKeyCode;
    private boolean mIsSQ45 = false;
    private boolean mIsSQ38 = false;
    private boolean mIsSQ53 = false;
    private boolean mIsSQ51FW = false;
    private boolean mIsSQ51S = false;
	private boolean mIsSQ53Q = false;
    private boolean mIsSQ47 = false;
    private boolean mIsSQ29Z = false;
    private boolean mIsSQ28 = false;
	private MenuKeyChangeReceiver menukeyChangeReceiver;
    StatusBarManager mStatusBarManager;
    //private boolean isPass = false;
    static  int prevIme;
    private int preMODE_KB;
    @Override
    public void finish() {
        Utilities.writeCurMessage(this, TAG, resultString);
        super.finish();
    }

    @Override
    protected void onPause() {
        //SystemProperties.set("persist.sys.factorykit.keytest","0");//wangyinghua
        //SystemProperties.set("persist.sys.allow.ime",String.valueOf(prevIme));
        if(FactoryKitPro.PRODUCT_SQ47) {
            //mStatusBarManager.showNavigationBar();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if(FactoryKitPro.PRODUCT_SQ47){
        //mStatusBarManager.removeNavigationBar();
         }
        super.onResume();
    }

    private void init(Context context) {

        resultString = Utilities.RESULT_FAIL;

		Log.d(TAG,"wangyinghua Utilities.getBuildProject().SQ53Q"+Utilities.getBuildProject().equals("DT50"));
        if(Utilities.getBuildProject().equals("SQ27T") || Utilities.getBuildProject().equals("SQ27TC")||Utilities.getBuildProject().equals("SQ27TE")
                || Utilities.getBuildProject().equals("SQ75")){
        	mIsSQ27 = true;
            setContentView(R.layout.keypad_sq27);
        }else if(FactoryKitPro.PRODUCT_SQ45 || FactoryKitPro.PRODUCT_SQ45S){
            mIsSQ45 = true;
            setContentView(R.layout.keypad_sq45);
        }else if(FactoryKitPro.PRODUCT_SQ53Q || FactoryKitPro.PRODUCT_SQ53H){
			Log.d(TAG,"wangyinghua 111");
            mIsSQ53Q = true;
            setContentView(R.layout.keypad_sq53q);
			//TextView tv = (TextView) findViewById(R.id.menu);            
            //tv.setVisibility(View.INVISIBLE);
        }else if(FactoryKitPro.PRODUCT_SQ38){
            mIsSQ38 = true;
            setContentView(R.layout.keypad_sq38);
        }else if(Utilities.getBuildProject().equals("SQ51")){
        	mIsSQ51 = true;
            setContentView(R.layout.keypad_sq51);
        }else if(Utilities.getBuildProject().equals("SQ52") || Utilities.getBuildProject().equals("SQ52T") || Utilities.getBuildProject().equals("SQ52W")){
        	mIsSQ52 = true;
            setContentView(R.layout.keypad_sq52);
        }else if(Utilities.getBuildProject().equals("SQ42T") || Utilities.getBuildProject().equals("SQ43T") ){
        	mIsSQ42 = true;
            setContentView(R.layout.keypad_sq42);
        } else if(Utilities.getBuildProject().equals("SQ26TB")){
            mSQ26TB=true;
            setContentView(R.layout.keypad_sq26tb);
        }else if(Utilities.getBuildProject().equals("SQ46")){
            mSQ46=true;
            setContentView(R.layout.keypad_sq46);
        }else if(Utilities.getBuildProject().equals("SQ27TD")){
            mIsSQ27TD=true;
            setContentView(R.layout.keypad_sq27td);
        }else if(Utilities.getBuildProject().equals("SQ53") || FactoryKitPro.PRODUCT_SQ53C){
		    mIsSQ53=true;
            setContentView(R.layout.keypad_sq53);
            //add by urovo luolin
            TextView tv = (TextView) findViewById(R.id.menu);
            final int defaultValue = 0; //mContext.getResources()
                //.getBoolean(com.android.internal.R.bool.config_swipe_up_gesture_default) ? 1 : 0;
            final int swipeUpEnabled = Settings.Secure.getInt(mContext.getContentResolver(),
                    "swipe_up_to_switch_apps_enabled"/*Settings.Secure.SWIPE_UP_TO_SWITCH_APPS_ENABLED*/, defaultValue);
            if(tv != null && swipeUpEnabled == 1){
                tv.setVisibility(View.INVISIBLE);
            }
        }else if(FactoryKitPro.PRODUCT_SQ51FW){   
            mIsSQ51FW=true;
            setContentView(R.layout.keypad_sq51fw);
        }else if(FactoryKitPro.PRODUCT_SQ51S){
            mIsSQ51S=true;
            setContentView(R.layout.keypad_sq51s);
        }else if(FactoryKitPro.PRODUCT_SQ29Z){
            mIsSQ29Z =true;
            setContentView(R.layout.keypad_sq29z);
        } else if (FactoryKitPro.PRODUCT_SQ28) {
            mIsSQ28 = true;
            setContentView(R.layout.keypad_sq28);
        } else if (FactoryKitPro.PRODUCT_SQ47) {
            mIsSQ47 = true;
            if(IS_SQ47C || fileExist("sys/bus/i2c/drivers/aw9523-47c-key/4-005b/aw9523_gpio"))
            setContentView(R.layout.keypad_sq47c);
            else
            setContentView(R.layout.keypad_sq47);
        }else {
            // default layout
            setContentView(R.layout.keypad);
            getXmlConfig();
        }

    	if(Utilities.getBuildProject().equals("SQ27TC")||Utilities.getBuildProject().equals("SQ27TE")||Utilities.getBuildProject().equals("SQ27TD")){
    		((TextView)findViewById(R.id.led)).setVisibility(View.GONE);
    		((TextView)findViewById(R.id.scan)).setVisibility(View.GONE);
    	}
        if(Utilities.getBuildProject().equals("SQ75")){
            ((TextView)findViewById(R.id.led)).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.scan)).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.fn)).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.delete)).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.keyback)).setVisibility(View.GONE);
            ((TextView)findViewById(R.id.keyenter)).setVisibility(View.GONE);
        }
        tvKeyCode = (TextView)findViewById(R.id.key_code_value);
        

        // get keymode
        itemIndex = getIntent().getIntExtra(Values.KEY_SERVICE_INDEX, -1);
        int keymodeIndex = Utilities.getIntPara(itemIndex, "KeyMode", 0);
        
        keyMode = KEYMODE[keymodeIndex];
        for (int i = 0; i < keyMode.length; i++) {
            keyStatusHashMap.put(keyMode[i], false);
        }
        //getXmlConfig();
        
        // hide some keys according to keymode on board
        /*TextView focusView = (TextView) findViewById(R.id.focus);
        TextView camView = (TextView) findViewById(R.id.camera);
        if(!mIsSQ27 && !mIsSQ51&& !mIsSQ52&& !mIsSQ42 && !mSQ26TB && !mSQ46 && !mIsSQ27TD && !mIsSQ45 && !mIsSQ53 &&!mIsSQ38){
            if (keymodeIndex == 0) {
                focusView.setVisibility(View.GONE);
                camView.setVisibility(View.GONE);
            } else if (keymodeIndex == 1)
                focusView.setVisibility(View.GONE);
        }*/
    }

    private boolean fileExist(String filePath) {
        File file = new File(filePath);
        if(file.exists())
            return true;
        else
            return false;
    }
    
    private boolean allKeyPassed() {
        for (int i = 0; i < keyMode.length; i++) {
            if (!keyStatusHashMap.get(keyMode[i]))
                return false;
        }
        return true;
    }

    /**
     * urovo yuanwei hide Recent button
     */
    private void hideRecentBtn() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.STATUS_BAR_DISABLE_RECENT;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mContext = this;
        mStatusBarManager = (StatusBarManager) this.getSystemService(Context.STATUS_BAR_SERVICE);
        prevIme = SystemProperties.getInt("persist.sys.allow.ime", 0);
        if(prevIme ==0)
            SystemProperties.set("persist.sys.allow.ime", "1");
		SystemProperties.set("persist.sys.factorykit.keytest","1");//wangyinghua
        // urovo huangjiezhou add on 20211207
        preMODE_KB = Settings.Global.getInt(this.getContentResolver(),"ufans.keyboard.state", MODE_KB_NUM);
        Settings.Global.putInt(this.getContentResolver(),"ufans.keyboard.state", MODE_KB_NUM);//reset keybord state
        InputIconChange();
        //if (FactoryKitPro.mHaveNavbar)
        //hideRecentBtn();
        init(getApplicationContext());
        
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
		
		IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("persist.sys.factorykit.keypad.recent");
        menukeyChangeReceiver = new MenuKeyChangeReceiver();
        registerReceiver(menukeyChangeReceiver, intentFilter);

    }

    @Override
    public void onDestroy(){
        unregisterReceiver(menukeyChangeReceiver);
        SystemProperties.set("persist.sys.factorykit.keytest","0");//wangyinghua
        // urovo huangjiezhou add on 20211207
        Settings.Global.putInt(this.getContentResolver(),"ufans.keyboard.state", preMODE_KB);
        SystemProperties.set("persist.sys.allow.ime",String.valueOf(prevIme));
        super.onDestroy();
    }


    private class MenuKeyChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
                       if(action.equals("persist.sys.factorykit.keypad.recent")){
                               //isPass = true;
                                if(!mIsSQ47){
                                    TextView tv = (TextView) findViewById(R.id.menu);
                                    if(tv != null)
                                    tv.setBackgroundResource(R.color.green);
                                }
                              
                       }
        }
    }
    

    @Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
    	int keyCode = event.getKeyCode();
        TextView keyText = null;
        logd("dispatchKeyEvent : " + keyCode+"--keyevent=="+event);
        if(tvKeyCode != null){
        	tvKeyCode.setText(String.valueOf(keyCode));
        }
		switch (keyCode) {
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			keyText = (TextView) findViewById(R.id.keyenter);
			break;
		case KeyEvent.KEYCODE_KEYBOARD_TALK:
            keyText = (TextView) findViewById(R.id.keytalk);
			break;
        case KeyEvent.KEYCODE_POWER:
            keyText =  (TextView) findViewById(R.id.power);
            break;
        case KeyEvent.KEYCODE_SPACE:
            keyText =  (TextView) findViewById(R.id.space);
            break;
		case KeyEvent.KEYCODE_KEYBOARD_PTT:
			keyText =  (TextView) findViewById(R.id.ptt);
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			if (mSQ26TB|| mIsSQ47)
				keyText = (TextView) findViewById(R.id.up);
        	if(mIsSQ38) keyText = (TextView)findViewById(R.id.volume_up);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			if ((mSQ26TB)|| mIsSQ47)
				keyText = (TextView) findViewById(R.id.down);
        	if(mIsSQ38) keyText = (TextView)findViewById(R.id.volume_down);
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (mSQ26TB|| mIsSQ47)
				keyText = (TextView) findViewById(R.id.left);
			else
                keyText = (TextView) findViewById(R.id.light_down);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (mSQ26TB|| mIsSQ47)
				keyText = (TextView) findViewById(R.id.right);
			else
                keyText = (TextView) findViewById(R.id.light_up);
			break;
		case KeyEvent.KEYCODE_KEYBOARD_SWITCH:
		case KeyEvent.KEYCODE_SHIFT_LEFT:
			 keyText = (TextView)findViewById(R.id.keyAa);
			break;
		case KeyEvent.KEYCODE_KEYBOARD_FN:
			keyText = (TextView) findViewById(R.id.fn);
			break;
		case KeyEvent.KEYCODE_SCAN_1:
			keyText = (TextView) findViewById(R.id.scan1);
			break;
		case KeyEvent.KEYCODE_SCAN_2:
            if (FactoryKitPro.PRODUCT_SQ53C || mIsSQ51S||mIsSQ53Q) { // SQ53C keycode 521
                keyText = (TextView) findViewById(R.id.scan3);
            } else {
                keyText = (TextView) findViewById(R.id.scan2);
            }
			Log.d(TAG,"wangyinghua keyText=="+keyText);
			break;
		case KeyEvent.KEYCODE_SCAN_3:
			keyText = (TextView) findViewById(R.id.scan3);
			break;
		case KeyEvent.KEYCODE_SCAN_4:
			keyText = (TextView) findViewById(R.id.scan4);
			break;
		case KeyEvent.KEYCODE_TAB:
			if(mIsSQ38 ||mIsSQ47){
			    keyText = (TextView) findViewById(R.id.tab);
			}else{
			    keyText = (TextView) findViewById(R.id.keypound);
			}
			break;
		case KeyEvent.KEYCODE_NUMPAD_SUBTRACT:
        	if(mIsSQ38) keyText = (TextView) findViewById(R.id.keystar);
			break;
        case KeyEvent.KEYCODE_COMMA:
            keyText = (TextView) findViewById(R.id.comma);
            break;
		default:
			break;
		}
        if (null != getViewIndexByKeycode(keyCode)) {
            keyText = getViewIndexByKeycode(keyCode);
        }
        if(keyText != null){
        	keyText.setBackgroundResource(R.color.green);
        	return true;
        }
		return super.dispatchKeyEvent(event);
	}
	

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG,"wangyinghua onKeyDown keyCode=="+keyCode+"--event=="+event);
        TextView keyText = null;
        logd(keyCode);
        if(tvKeyCode != null){
        	tvKeyCode.setText(String.valueOf(keyCode));
        }
        keyStatusHashMap.put(keyCode, true);
        switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_UP:
            if(mSQ46)
                keyText = (TextView) findViewById(R.id.F1);
			else if(mIsSQ29Z||mIsSQ28)
			    keyText = (TextView) findViewById(R.id.up);
            else
                keyText = (TextView) findViewById(R.id.volume_up);
            break;
        case KeyEvent.KEYCODE_VOLUME_DOWN:
            if(mSQ46)
                keyText = (TextView) findViewById(R.id.F2);
			else if(mIsSQ29Z||mIsSQ28)
			    keyText = (TextView) findViewById(R.id.download);
            else
                keyText = (TextView) findViewById(R.id.volume_down);

            break;

        case KeyEvent.KEYCODE_FOCUS:
            keyText = (TextView) findViewById(R.id.focus);

            break;
        case KeyEvent.KEYCODE_CAMERA:
            //keyText = (TextView) findViewById(R.id.camera);
            break;
        case KeyEvent.KEYCODE_UNKNOWN:
        	keyText =  (TextView) findViewById(R.id.led);
        	break;
        case KeyEvent.KEYCODE_TV:
        	if(mIsSQ51 || mIsSQ27){
        		keyText =  (TextView) findViewById(R.id.led);
        	}
        	break;
        case KeyEvent.KEYCODE_F1:
                if(mSQ26TB||mSQ46 || mIsSQ45 || mIsSQ38 || mIsSQ47){
                    keyText =  (TextView) findViewById(R.id.F1);
                }
                break;
        case KeyEvent.KEYCODE_F2:
                if(mSQ26TB ||mSQ46  || mIsSQ45 || mIsSQ38 || mIsSQ47){
                    keyText =  (TextView) findViewById(R.id.F2);
                }
                break;
		case KeyEvent.KEYCODE_POWER:
            //if(mSQ46)
                TextView powerkeyText = (TextView) findViewById(R.id.power);
			    //powerkeyText.setBackgroundResource(R.color.green);
            //else
                //keyText = (TextView) findViewById(R.id.volume_down);

            break;
        case KeyEvent.KEYCODE_F3:
                if(mSQ26TB  || mIsSQ45 || mIsSQ38 || mIsSQ47){
                    keyText =  (TextView) findViewById(R.id.F3);
                }
                break;
        case KeyEvent.KEYCODE_F4:
                if(mSQ26TB  || mIsSQ45 || mIsSQ38 || mIsSQ47){
                    keyText =  (TextView) findViewById(R.id.F4);
                }
                break;
        case KeyEvent.KEYCODE_F5:
            if(mIsSQ47){
                keyText =  (TextView) findViewById(R.id.F5);
            }
            break;
        case KeyEvent.KEYCODE_F6:
            if(mIsSQ47){
                keyText =  (TextView) findViewById(R.id.F6);
            }
            break;
        case KeyEvent.KEYCODE_F7:
            if(mIsSQ47){
                keyText =  (TextView) findViewById(R.id.F7);
            }
            break;
        case KeyEvent.KEYCODE_F9:
            if(mIsSQ47){
                keyText =  (TextView) findViewById(R.id.F9);
            }
            break;
        case KeyEvent.KEYCODE_F10:
            if(mIsSQ47){
                keyText =  (TextView) findViewById(R.id.F0);
            }
            break;
        case KeyEvent.KEYCODE_SYSRQ:
        	keyText =  (TextView) findViewById(R.id.scan);
        	break;
        case KeyEvent.KEYCODE_F8:
        	if(mIsSQ27 || mIsSQ51){
        		keyText =  (TextView) findViewById(R.id.scan);
        	}else if(mIsSQ47){
                keyText =  (TextView) findViewById(R.id.F8);
            }
        	break;
        case KeyEvent.KEYCODE_ESCAPE:
                if(mIsSQ27 || mIsSQ27TD){
                    keyText = (TextView) findViewById(R.id.delete);
                }else{
                  keyText = (TextView)findViewById(R.id.back);
                }
                if(mIsSQ38 || mIsSQ47) keyText = (TextView)findViewById(R.id.esc);
        	break;
        case KeyEvent.KEYCODE_DEL:
            if(mIsSQ42 || mIsSQ27 || mIsSQ27TD || mIsSQ45 || mIsSQ38)
                keyText = (TextView) findViewById(R.id.keyback);
            else if(mIsSQ47){
                keyText = (TextView) findViewById(R.id.keydel);
            }
            else
                keyText = (TextView) findViewById(R.id.back_space);
        	break;
        case KeyEvent.KEYCODE_ENTER:
        case KeyEvent.KEYCODE_DPAD_CENTER:
        	keyText =  (TextView) findViewById(R.id.keyenter);
        	break;
        case KeyEvent.KEYCODE_MENU:
        case KeyEvent.KEYCODE_APP_SWITCH:
        	keyText =  (TextView) findViewById(R.id.menu);
            //add by urovo luolin
            if(keyText != null) {
                keyText.setVisibility(View.VISIBLE);
            }
        	break;
			
        case KeyEvent.KEYCODE_HOME:
        	keyText =  (TextView) findViewById(R.id.home);
        	break;
        case KeyEvent.KEYCODE_BACK:
        	keyText =  (TextView) findViewById(R.id.back);
        	if(mIsSQ38) keyText = (TextView)findViewById(R.id.esc);
        	break;
        case KeyEvent.KEYCODE_0:
              keyText = (TextView) findViewById(R.id.key0);
              break;
        case KeyEvent.KEYCODE_1:
            keyText = (TextView) findViewById(R.id.key1);
            break;
        
        case KeyEvent.KEYCODE_2:
        case KeyEvent.KEYCODE_A:
        case KeyEvent.KEYCODE_B:
        case KeyEvent.KEYCODE_C:
            keyText = (TextView) findViewById(R.id.key2);
            break; 
      
         case KeyEvent.KEYCODE_3:
         case KeyEvent.KEYCODE_D:
         case KeyEvent.KEYCODE_E:
         case KeyEvent.KEYCODE_F:
            keyText = (TextView) findViewById(R.id.key3);
            break;


         case KeyEvent.KEYCODE_4:
         case KeyEvent.KEYCODE_G:
         case KeyEvent.KEYCODE_H:
         case KeyEvent.KEYCODE_I:
            keyText = (TextView) findViewById(R.id.key4);
            break;

         case KeyEvent.KEYCODE_5:
         case KeyEvent.KEYCODE_J:
         case KeyEvent.KEYCODE_K:
         case KeyEvent.KEYCODE_L:
            keyText = (TextView) findViewById(R.id.key5);
            break;

         case KeyEvent.KEYCODE_6:
         case KeyEvent.KEYCODE_M:
         case KeyEvent.KEYCODE_N:
         case KeyEvent.KEYCODE_O:
            keyText = (TextView) findViewById(R.id.key6);
            break;

         case KeyEvent.KEYCODE_7:
         case KeyEvent.KEYCODE_P:
         case KeyEvent.KEYCODE_Q:
         case KeyEvent.KEYCODE_R:
         case KeyEvent.KEYCODE_S:
            keyText = (TextView) findViewById(R.id.key7);
            break;

         case KeyEvent.KEYCODE_8:
         case KeyEvent.KEYCODE_T:
         case KeyEvent.KEYCODE_U:
         case KeyEvent.KEYCODE_V:
                 keyText = (TextView) findViewById(R.id.key8);
            break;
         case KeyEvent.KEYCODE_9:
         case KeyEvent.KEYCODE_W:
         case KeyEvent.KEYCODE_X:
         case KeyEvent.KEYCODE_Y:
         case KeyEvent.KEYCODE_Z:
            keyText = (TextView) findViewById(R.id.key9);
            break;
         case KeyEvent.KEYCODE_STAR:
             keyText = (TextView) findViewById(R.id.keystar);
             break;
         case KeyEvent.KEYCODE_ALT_LEFT:
             keyText = (TextView) findViewById(R.id.alt_left);
             break;
         case KeyEvent.KEYCODE_CTRL_LEFT:
             keyText = (TextView) findViewById(R.id.ctrl_left);
             break;
         case KeyEvent.KEYCODE_POUND:
         case KeyEvent.KEYCODE_TAB:
             keyText = (TextView) findViewById(R.id.keypound);
             break;
         case KeyEvent.KEYCODE_NUMPAD_1:
         case 534://KEYCODE_KEYBOARD_P1
             keyText = (TextView) findViewById(R.id.p1);
             break;
         case KeyEvent.KEYCODE_NUMPAD_2:
         case 535://KEYCODE_KEYBOARD_P2
             keyText = (TextView) findViewById(R.id.p2);
             break;

  	    case KeyEvent.KEYCODE_PERIOD:
            if(mIsSQ42 || mIsSQ45)
                 keyText = (TextView) findViewById(R.id.keystar);
            else if (mSQ26TB)
                keyText = (TextView) findViewById(R.id.dot);
            else if (mIsSQ38 || mIsSQ47)
                keyText = (TextView) findViewById(R.id.keypound);
        break;
        }
        if (null != getViewIndexByKeycode(keyCode)) {
            keyText = getViewIndexByKeycode(keyCode);
        }
        if (null != keyText) {
            keyText.setBackgroundResource(R.color.green);
        }



        /*
        if (allKeyPassed())
            pass();
            */
        return true;
    }

    void fail(Object msg) {

        loge(msg);
        setResult(RESULT_CANCELED);
        resultString = Utilities.RESULT_FAIL;
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        resultString = Utilities.RESULT_PASS;
        finish();
    }

    void logd(Object d) {

        Log.d(TAG, "" + d);
    }

    void loge(Object e) {

        Log.e(TAG, "" + e);
    }

    View[] keyItemView;

    void getXmlConfig(){
        try {
            XmlPullParser xmlPullParser = null;
            String configFile = null;
            for (String tmpConfigFile : Values.CONFIG_KEYPAD_FILE_SEARCH_LIST) {
                File tmp = new File(tmpConfigFile);
                if (tmp.exists() && tmp.canRead()) {
                    configFile = tmpConfigFile;
                    logd("Found config file: " + tmpConfigFile);
                    break;
                }
            }
            if (configFile != null) {
                XmlPullParserFactory xmlPullParserFactory;
                try {
                    xmlPullParserFactory = XmlPullParserFactory.newInstance();
                    xmlPullParserFactory.setNamespaceAware(true);
                    xmlPullParser = xmlPullParserFactory.newPullParser();
                    FileInputStream fileInputStream = new FileInputStream(configFile);
                    xmlPullParser.setInput(fileInputStream, "utf-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else{
            xmlPullParser = getResources().getXml(R.xml.cit_keytest_config);}
            int mEventType = xmlPullParser.getEventType();
            FlexboxLayout linParent = (FlexboxLayout)findViewById(R.id.key_pad);
            FlexboxLayout lin = new FlexboxLayout(this);
            int lineNum = 0;
            /** Parse the xml */
            while (mEventType != XmlPullParser.END_DOCUMENT) {
                if (mEventType == XmlPullParser.START_TAG) {
                    String name = xmlPullParser.getName();
                    if (name.equals("test_line")) {
                        lin = new FlexboxLayout(this);
                        lin.setJustifyContent(JustifyContent.CENTER);
                        lin.setAlignItems(AlignItems.CENTER);
                        lin.setFlexWrap(FlexWrap.WRAP);
                        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT );
                        lin.setLayoutParams(layoutParams);
                        lineNum =0;
                        Log.d("zml","test_line ");

                    }else if(name.equals("key_test")){
                        lineNum++;
                        KeyInfo key=new KeyInfo();
                        key.keyCode = Integer.parseInt(xmlPullParser.getAttributeValue(null, "keyCode"));
                        key.keyName = xmlPullParser.getAttributeValue(null, "keyName");
                        Log.d("zml","name "+key.keyCode + "keycode " + key.keyName);
                        TextView keyView = new TextView(this);
                        keyView.setText(key.keyName);
                        keyView.setTag(key.keyName);
                        int padding = Util.dpToPixel(this, 1);
                        int paddingLeftAndRight = Util.dpToPixel(this, 1);
                        ViewCompat.setPaddingRelative(keyView, paddingLeftAndRight, padding, paddingLeftAndRight, padding);
                        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        int margin = Util.dpToPixel(this, 2);
                        int marginTop = Util.dpToPixel(this, 3);
                        layoutParams.setMargins(margin, marginTop, margin, 0);
                        keyView.setLayoutParams(layoutParams);
                        key.textView = keyView;
                        if(lin != null){
                            lin.addView(keyView);
                            mKeyInfo.add(key);
                        }
                    }
                } else if (mEventType == XmlPullParser.END_TAG) {
                    String tagName = xmlPullParser.getName();

                    if (lin != null && tagName.equals("test_line")) {
                        linParent.addView(lin);
                    }
                }
                mEventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            Log.e("zml","test_line " + e);
        }
    }

    public static class KeyInfo {

        int keyCode;
        String keyName;// the key for get test name
        TextView textView;

        public String getName() {
            return keyName;
        }

        public void setName(String nm) {
            keyName = nm;
        }

        public int getKeyCode() {
            return keyCode;
        }

        public void setKeyCode(int keycode) {
            keyCode = keycode;
        }

        public TextView getTextView() {
            return textView;
        }

        public void settextView(TextView tv) {
            textView = tv;
        }

    }

    private void InputIconChange() {
        Intent intent = new Intent("android.intent.action.ACTION_INPUT_STATE");
        intent.putExtra("Shift_status",false);
        intent.putExtra("Keep_shift",false);
        intent.putExtra("Orange_status",false);
        mContext.sendBroadcast(intent);
    }

    public TextView getViewIndexByKeycode(int keycode) {
        if (keycode == 0) {
            return null;
        }
        for (KeyInfo tv : mKeyInfo) {
            if(tv.keyCode == keycode)
                return tv.textView;
        }
        return null;
    }

}
