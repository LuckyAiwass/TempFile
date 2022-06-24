package com.ubx.scanwedge.service;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.os.Vibrator;
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

import com.ubx.scanwedge.R;

public class FxService extends Service {

    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    WindowManager mWindowManager;

    ImageView mFloatView;

    private static final String TAG = "FxService";
    boolean putScan = false;
    boolean moving = false;
    private Vibrator mVibrator;
    private ScanWedgeApplication mApplication;
    private static final int TYPE_VIBRATE = 2;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        createFloatView();
        initScan();
    }

    private void initScan() {
        // TODO Auto-generated method stub
        mApplication = (ScanWedgeApplication) getApplication();
        mVibrator = (Vibrator) getApplication().getSystemService(Context.VIBRATOR_SERVICE);
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
        wmParams = new WindowManager.LayoutParams();
        mWindowManager = (WindowManager) getApplication().getSystemService(
                getApplication().WINDOW_SERVICE);
        wmParams.type = LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags =
                // LayoutParams.FLAG_NOT_TOUCH_MODAL |
                LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = mWindowManager.getDefaultDisplay().getWidth();
        wmParams.y = mWindowManager.getDefaultDisplay().getHeight()/2;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.floatbutton, null);
        mWindowManager.addView(mFloatLayout, wmParams);

        Log.i(TAG, "mFloatLayout-->left" + mFloatLayout.getLeft());
        Log.i(TAG, "mFloatLayout-->right" + mFloatLayout.getRight());
        Log.i(TAG, "mFloatLayout-->top" + mFloatLayout.getTop());
        Log.i(TAG, "mFloatLayout-->bottom" + mFloatLayout.getBottom());

        mFloatView = (ImageView) mFloatLayout.findViewById(R.id.float_id);

        mFloatLayout.measure(View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED), View.MeasureSpec
                .makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        mFloatView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub

                int newx = (int) event.getRawX()
                        - mFloatView.getMeasuredWidth() / 2;
                int newy = (int) event.getRawY()
                        - mFloatView.getMeasuredHeight() / 2 - 30;
                int movlev = 100;
                if (newx > wmParams.x + movlev || newx < wmParams.x - movlev
                        || newy > wmParams.y + movlev
                        || newy < wmParams.y - movlev || moving == true) {
                    moving = true;
                    wmParams.x = newx;
                    Log.i(TAG, "RawX" + event.getRawX());
                    Log.i(TAG, "X" + event.getX());
                    wmParams.y = newy;
                    Log.i(TAG, "RawY" + event.getRawY());
                    Log.i(TAG, "Y" + event.getY());// TODO Auto-generated catch block
                    // 刷新
                    mWindowManager.updateViewLayout(mFloatLayout, wmParams);
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    mFloatView.setBackgroundResource(R.drawable.virtual_press);
                    if (putScan == false) {
                        putScan = true;
                        try{
                            if(ScanWedgeApplication.ENABLE_BINDER_SERVICE) {
                                mApplication.getService().softTrigger(1);
                            } else {
                                mApplication.getIService().softTrigger(1);
                            }
                        } catch (Exception e) {
                        }
                        mVibrator.vibrate(100);
                    }
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    try{
                        mApplication.getService().softTrigger(0);
                    } catch (Exception e) {
                    }
                    mFloatView.setBackgroundResource(R.drawable.virtual);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    putScan = false;
                    moving = false;
                }
                return false;
            }
        });

        mFloatView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (mFloatLayout != null) {
            mWindowManager.removeView(mFloatLayout);
        }
    }

}

