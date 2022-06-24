package com.ubx.featuresettings.service;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.Vibrator;
import android.os.Build;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.ubx.featuresettings.R;
import android.provider.Settings;
import android.view.IWindowManager;
import android.os.ServiceManager;
import android.os.Handler;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.InputDevice;
import android.hardware.input.InputManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.view.KeyCharacterMap;
import android.os.SystemClock;
import com.android.internal.view.IInputMethodManager;

public class FloatingService extends Service {

    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    WindowManager mWindowManager;

    View mFloatView;

    private static final String TAG = "FloatingService";
    boolean moving = false;
    private Vibrator mVibrator;
    private static final int TYPE_VIBRATE = 2;
    private IWindowManager mIWindowManager;


    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        createFloatView();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    private void createFloatView() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = mWindowManager.getDefaultDisplay().getWidth();
        wmParams.y = mWindowManager.getDefaultDisplay().getHeight()/2;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(this);
        mFloatView = inflater.inflate(R.layout.image_display, null);
        ImageView imageView = mFloatView.findViewById(R.id.image_display_imageview);
        imageView.setImageResource(R.drawable.soft_keyboard_floating_window);
        mWindowManager.addView(mFloatView, wmParams);
        mFloatView.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        mFloatView.setOnTouchListener(new OnTouchListener() {
            private int x;
            private int y;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = (int) event.getRawX();
                        y = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int nowX = (int) event.getRawX();
                        int nowY = (int) event.getRawY();
                        int movedX = nowX - x;
                        int movedY = nowY - y;
                        x = nowX;
                        y = nowY;
                        wmParams.x = wmParams.x + movedX;
                        wmParams.y = wmParams.y + movedY;
                        mWindowManager.updateViewLayout(mFloatView, wmParams);
                        break;
                    default:
                        break;
                }

                return false;
            }
        });

        mFloatView.setOnClickListener(new OnClickListener() {
            private IInputMethodManager myService;

            @Override
            public void onClick(View v) {
                myService = IInputMethodManager.Stub.asInterface(ServiceManager.getService("input_method"));
                mIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
                if (Settings.Global.getInt(FloatingService.this.getContentResolver(), "disable_pop_softinput", 0) == 0) {
                    Settings.Global.putInt(FloatingService.this.getContentResolver(), "disable_pop_softinput", 1);
                    try {
                        mIWindowManager.pttKeyShowSoftinput();
                    } catch (Exception e) {
                        android.util.Log.d(TAG,"Exception:"+e);
                        Settings.System.putInt(FloatingService.this.getContentResolver(), "keycode_input_flag", 0);
                    }
                    //Toast.makeText(FloatingService.this, getString(R.string.str_soft_keyboard_off), Toast.LENGTH_SHORT).show();
                } else {
                    Settings.Global.putInt(FloatingService.this.getContentResolver(), "disable_pop_softinput", 0);
                    try {
                        mIWindowManager.pttKeyShowSoftinput();
                    } catch (Exception e) {
                        android.util.Log.d(TAG,"Exception:"+e);
                        Settings.System.putInt(FloatingService.this.getContentResolver(), "keycode_input_flag", 0);
                    }
                    //Toast.makeText(FloatingService.this, getString(R.string.str_soft_keyboard_on), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendEvent(int action, int flags,long downtime, long when, int keyCode) {
        final int repeatCount = (flags & KeyEvent.FLAG_LONG_PRESS) != 0 ? 1 : 0;
        final KeyEvent ev = new KeyEvent(downtime, when, action, keyCode, repeatCount,
                0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                flags | KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_VIRTUAL_HARD_KEY,
                InputDevice.SOURCE_KEYBOARD);
        InputManager.getInstance().injectInputEvent(ev,
                InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mFloatView != null) {
            mWindowManager.removeView(mFloatView);
        }
    }

}
