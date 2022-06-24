package com.android.settings;

import android.os.Build;
import java.util.EventListener;
import android.view.KeyEvent;
import android.util.Log;

public class ServiceMenuKeySequence{
    private static final String TAG = "ServiceMenuKeySequence.java";

    public interface OnInvokeServiceMenuListener extends EventListener{
        public void onServiceMenu();
    }
    OnInvokeServiceMenuListener mrListener;

    long mlLastTime;
    int mnCurPos=0;
    final int[] mrnPreset= {
            KeyEvent.KEYCODE_VOLUME_UP, 
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP, 
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP, 
            KeyEvent.KEYCODE_VOLUME_UP, 
            KeyEvent.KEYCODE_VOLUME_DOWN,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            };

	final int[] mrnPreset_SQ27TD= {
            KeyEvent.KEYCODE_KEYBOARD_FN,
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_KEYBOARD_FN,
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_KEYBOARD_FN,
            KeyEvent.KEYCODE_KEYBOARD_FN,
            KeyEvent.KEYCODE_DEL,
            KeyEvent.KEYCODE_DEL,
            };

    final int[] mrnPresetNumber= {
            KeyEvent.KEYCODE_1, 
            KeyEvent.KEYCODE_2,
            KeyEvent.KEYCODE_3, 
            KeyEvent.KEYCODE_4,
            KeyEvent.KEYCODE_4,
            KeyEvent.KEYCODE_3,
            KeyEvent.KEYCODE_2,
            KeyEvent.KEYCODE_1,
            };

    final int[] mrnPreset_scan= {
            KeyEvent.KEYCODE_SCAN_1, 
            KeyEvent.KEYCODE_SCAN_2,
            KeyEvent.KEYCODE_SCAN_1, 
            KeyEvent.KEYCODE_SCAN_2,
            KeyEvent.KEYCODE_SCAN_1, 
            KeyEvent.KEYCODE_SCAN_1, 
            KeyEvent.KEYCODE_SCAN_2,
            KeyEvent.KEYCODE_SCAN_2,    
    };

    public ServiceMenuKeySequence(){
        setOnInvokeServiceMenuListener(null);
    }

    public boolean keyIn(int inKeyCode){
        //Log.i(TAG,"inKeyCode = "+ inKeyCode);
		if(Build.PROJECT.equals("SQ27TD")){
			if(mnCurPos < mrnPreset_SQ27TD.length && (inKeyCode==mrnPreset_SQ27TD[mnCurPos] || inKeyCode==mrnPresetNumber[mnCurPos])){
		        if(mnCurPos==0){
		            mlLastTime=System.currentTimeMillis();
		            ++mnCurPos;
		        } else {
		            if(System.currentTimeMillis()-mlLastTime<10000){
		                ++mnCurPos;
		                if(mnCurPos>=mrnPreset_SQ27TD.length){
		                    mnCurPos=0;
		                    mrListener.onServiceMenu();
		                    return true;
		                }
		                mlLastTime=System.currentTimeMillis();
		            } else {
		                mnCurPos=0;
		            }
		        }
		    } else {
		        mnCurPos=0;
		    }
		}else{
//		    if(mnCurPos < mrnPreset.length && (inKeyCode==mrnPreset[mnCurPos] || inKeyCode==mrnPresetNumber[mnCurPos])){
            if(mnCurPos < mrnPreset.length && (inKeyCode==mrnPreset[mnCurPos] || inKeyCode==mrnPresetNumber[mnCurPos] || (mnCurPos < mrnPreset.length && (inKeyCode==mrnPreset_scan[mnCurPos])))){
		        if(mnCurPos==0){
		            mlLastTime=System.currentTimeMillis();
		            ++mnCurPos;
		        } else {
					android.util.Log.d(TAG, "keyIn: ");
		            if(System.currentTimeMillis()-mlLastTime<10000){
		                ++mnCurPos;
		                if(mnCurPos>=mrnPreset.length){
		                    mnCurPos=0;
		                    mrListener.onServiceMenu();
		                    return true;
		                }
		                mlLastTime=System.currentTimeMillis();
		            } else {
		                mnCurPos=0;
		            }
		        }
		    } else {
		        mnCurPos=0;
		    }
		}
        return false;
    }

    public void setOnInvokeServiceMenuListener(OnInvokeServiceMenuListener irListener){
		android.util.Log.d(TAG, "setOnInvokeServiceMenuListener: ");
        if(irListener==null){
            mrListener = new OnInvokeServiceMenuListener(){
                public void onServiceMenu() {}
            };
        } else {
            mrListener = irListener;
        }
    }
}
