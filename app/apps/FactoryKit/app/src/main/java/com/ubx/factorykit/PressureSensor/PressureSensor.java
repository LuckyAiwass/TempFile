/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.PressureSensor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class PressureSensor extends Activity {

    private SensorManager mSensorManager = null;
    private Sensor mPressureSensor = null;
    private PressureSensorListener mPressureSensorListener;
    TextView mTextView;
    Button cancelButton;
    private final static String INIT_VALUE = "";
    private static String value = INIT_VALUE;
    private static String pre_value = INIT_VALUE;
    private final int MIN_COUNT = 10;
    String TAG = "PressureSensor";
    private final static int SENSOR_TYPE = Sensor.TYPE_PRESSURE;
    private final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST;

    @Override
    public void finish() {

        try {

            mSensorManager.unregisterListener(mPressureSensorListener, mPressureSensor);
        } catch (Exception e) {
        }
        super.finish();
    }

    void bindView() {

        mTextView = (TextView) findViewById(R.id.msensor_result);
        cancelButton = (Button) findViewById(R.id.msensor_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                fail(null);
            }
        });
    }

    void getService() {

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (mSensorManager == null) {
            fail(getString(R.string.service_get_fail));
        }

        mPressureSensor = mSensorManager.getDefaultSensor(SENSOR_TYPE);
        if (mPressureSensor == null) {
            fail(getString(R.string.sensor_get_fail));
        }

        mPressureSensorListener = new PressureSensorListener(this);
        if (!mSensorManager.registerListener(mPressureSensorListener, mPressureSensor, SENSOR_DELAY)) {
            fail(getString(R.string.sensor_register_fail));
        }
    }

    void updateView(Object s) {

        mTextView.setText(TAG + " : " + s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.msensor);

        bindView();
        getService();

        updateView(value);

    }

    void fail(Object msg) {

        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG,"Failed");
        finish();
    }

    void pass() {

        // toast(getString(R.string.test_pass));
        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG,"Pass");
        finish();

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (mSensorManager == null || mPressureSensorListener == null || mPressureSensor == null)
            return;
        mSensorManager.unregisterListener(mPressureSensorListener, mPressureSensor);
    }

    public class PressureSensorListener implements SensorEventListener {

        private int count = 0;

        public PressureSensorListener(Context context) {

            super();
        }

        public void onSensorChanged(SensorEvent event) {

            // MSensor event.value has 3 equal value.
            synchronized (this) {
                if (event.sensor.getType() == SENSOR_TYPE) {
                    logd(event.values.length + ":" + event.values[0] + " " + event.values[0] + " "
                            + event.values[0] + " ");
		    if(event.values.length < 1 /** uroro yuanwei 2018-12-19 default 2 */) {
			logd("error!!! , event.values.length is :" + event.values.length);
			return;
		    }
                    /*String value = "(" + event.values[0] + ", " + event.values[1] + ", "
                            + event.values[2] + ")";*/
                    // uroro yuanwei change analyzing conditions 2018-12-19
                    String value = "(" + event.values[0] + ")";
                    updateView(value);
                    if (value != pre_value)
                        count++;
                    if (count >= MIN_COUNT)
                        pass();
                    pre_value = value;
                }
            }
        }

        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }

    private void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(TAG, e + "");
    }

    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }

}
