package com.android.server;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class SecurityDialog extends Dialog {

    public SecurityDialog(Context context, int theme) {
        super(context, theme);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        if(event.getDeviceId() <= 0)  {
            Log.e("dispatch", "dispatchKeyEvent " + event.getDeviceId());
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        if(ev.getDeviceId() <= 0)  {
            Log.e("dispatch", "dispatchTouchEvent " + ev.getDeviceId());
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(event.getDeviceId() <= 0)  {
            Log.e("dispatch", "onKeyDown " + event.getDeviceId());
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if(event.getDeviceId() <= 0)  {
            Log.e("dispatch", "onKeyLongPress " + event.getDeviceId());
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 