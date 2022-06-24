package com.ubx.factorykit.Fingerprint;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
//import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.UserHandle;
import android.os.Vibrator;
import android.os.Bundle;
import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;//Utilities;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.os.CancellationSignal;

import java.io.File;
import android.view.KeyEvent;
import android.content.pm.ActivityInfo;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.content.Context;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.hardware.fingerprint.Fingerprint;


import android.hardware.fingerprint.FingerprintManager;
import android.hardware.fingerprint.FingerprintManager.AuthenticationCallback;
import android.hardware.fingerprint.FingerprintManager.AuthenticationResult;
import android.hardware.fingerprint.FingerprintManager.RemovalCallback;
import android.os.CancellationSignal;
import java.util.List;
import android.os.Vibrator;
import android.content.Context;
import android.util.Log;
import android.os.UserHandle;

import androidx.annotation.RequiresApi;


public class FingerPrintActivity extends Activity {
    private static final String TAG = "FingerPrintActivity";
    private boolean mEnrolling;
    private int ID = -1;
    private String mResult = null;
    private int pass = 0;
    private PowerManager.WakeLock mWakeLock;
    private boolean mbStarted = false;
    private CancellationSignal mCancellationSignal;
    private byte[] mToken = new byte[69];//
    private Vibrator mVibrator;
    private FingerprintManager mFingerprintManager;
    private CancellationSignal mEnrollmentCancel;
	private Context mContext;
    private AlertDialog dialog;
	
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            ID = intent.getIntExtra("ID", 0);
        }
		mContext = FingerPrintActivity.this;
        //setContentView(R.layout.printer);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        //mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mFingerprintManager = (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);
       // enrollRoundPrg = (RoundProgressBar) findViewById(R.id.enrollPrg_roundProgressBar);
	    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_fingerprint, null);
        initView(view);
        builder.setView(view);
        builder.setCancelable(false);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //setResultAndClean();
                dialog.cancel();
                FingerPrintActivity.this.finish();
            }
        });
        dialog = builder.create();
        dialog.show();

        if (FactoryKitPro.PRODUCT_SQ53S) {
            for (int i = 0; i < mToken.length; i++) {
                mToken[i] = (byte)(i % 10);
                Log.d(TAG, "mToken[" + i + "] = " + (mToken[i] & 0xFF));
            }
        } else {
            long challenge = mFingerprintManager.preEnroll();
            Log.i(TAG, "mToken challenge="+Long.toHexString(challenge));

            for (int i = 0; i < mToken.length; i++) {
                mToken[i] = (byte)0xFF;
            }
            mToken[33] = 2;
            mToken[34] = 0;
            mToken[35] = 0;
            mToken[36] = 0;
            int challenge_h = (int) (challenge >> 32);
            int challenge_l = (int) (challenge & 0xFFFFFFFF);
            Log.i(TAG, "mToken challenge_h="+ Integer.toHexString(challenge_h));
            Log.i(TAG, "mToken challenge_l="+ Integer.toHexString(challenge_l));
            mToken[8] = (byte) (challenge_h >> 24);
            mToken[7] = (byte) ((challenge_h & 0xFF0000) >> 16);
            mToken[6] = (byte) ((challenge_h & 0xFF00) >> 8);
            mToken[5] = (byte) (challenge_h & 0xFF);
            mToken[4] = (byte) (challenge_l >> 24);
            mToken[3] = (byte) ((challenge_l & 0xFF0000) >> 16);
            mToken[2] = (byte) ((challenge_l & 0xFF00) >> 8);
            mToken[1] = (byte) (challenge_l & 0xFF);
            mToken[0] = (byte) 0;
        }
    }
	
	
	 // 游标动画
    private Handler indexHandler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0){
                int i = postion % 5;
                if (i == 0){
                    tv[4].setBackground(null);
                    tv[i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                }
                else{
                    tv[i].setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    tv[i-1].setBackground(null);
                }
                postion++;
                indexHandler.sendEmptyMessageDelayed(0,100);
            }
        }
    };
    TextView[] tv = new TextView[5];
    private int postion = 0;
    private void initView(View view) {
        postion = 0;
        tv[0] = (TextView) view.findViewById(R.id.tv_1);
        tv[1] = (TextView) view.findViewById(R.id.tv_2);
        tv[2] = (TextView) view.findViewById(R.id.tv_3);
        tv[3] = (TextView) view.findViewById(R.id.tv_4);
        tv[4] = (TextView) view.findViewById(R.id.tv_5);
        indexHandler.sendEmptyMessageDelayed(0,100);
    }

    private void startFingerPrintTest() {
		
		 Log.d(TAG, "startFingerPrintTest");
        //clearEnrolledFingerPrint();
        enrollStart();
    }

    private void enrollStart() {//check device functional well or not
		Log.d(TAG, "startFingerPrintTest");
        if (mFingerprintManager.isHardwareDetected()) {
			showToast(getString(R.string.fingerprint_yes));
           
        } else {
			showToast(getString(R.string.fingerprint_no));        
			fail();
        }
        mEnrollmentCancel = new CancellationSignal();
        mFingerprintManager.enroll(mToken, mEnrollmentCancel, 0, UserHandle.USER_OWNER, mEnrollmentCallback);
        mEnrolling = true;
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void doVerified(int flag) {
        mFingerprintManager.authenticate(null, null, flag, mAuthenticationCallback, null);
        long mid = mFingerprintManager.getAuthenticatorId();
        Log.d(TAG, "mid = " + mid);
    }

    private void clearEnrolledFingerPrint() {//clear all fingerprints when in & out our test
        final List<Fingerprint> items = mFingerprintManager.getEnrolledFingerprints();
		Log.d(TAG, "startFingerPrintTest items.size()=" + items.size());
        if (items.size() > 0) {
            Log.d(TAG, "Fingerprint = " + items.size());
            for (Fingerprint i : items) {
                mFingerprintManager.remove(i, UserHandle.USER_OWNER ,mRemovalCallback);
            }
        }
    }

	
    @Override
    protected void onStart() {
        super.onStart();
        startFingerPrintTest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mWakeLock.acquire();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mWakeLock.release();
        //clearEnrolledFingerPrint();
    }


    private void setDialogProgress(int progress) {//update progress bar when enrolling
        if (progress >= 0 && progress <= 100) {
          //  enrollRoundPrg.setProgress(progress);
            if (progress == 100) {
            //    enrollRoundPrg.doEnrollSuccessAnimation();
            }
        }
    }

    private FingerprintManager.EnrollmentCallback mEnrollmentCallback
            = new FingerprintManager.EnrollmentCallback() {

        @Override
        public void onEnrollmentProgress(int remaining) {//must enroll 14 times & i don't know why
            Log.d(TAG,"--yongcan--onEnrollmentProgress  remaining = " + remaining);
			
			passwithToast(getString(R.string.fingerprint_get));
            if (0 == remaining) {
                setDialogProgress(100);
              //guoyongcan block   doVerified(0);//enroll completed and start match
            } else {
                setDialogProgress((14 - remaining) * 7);//update progressbar while enrolling
            }
        }

        @Override
        public void onEnrollmentHelp(int helpMsgId, CharSequence helpString) {
            Log.d(TAG,"onEnrollmentHelp  helpMsgId = " + helpMsgId + "	helpString = " + helpString);
        }

        @Override
        public void onEnrollmentError(int errMsgId, CharSequence errString) {
            Log.d(TAG,"onEnrollmentError  errMsgId = " + errMsgId + "	errString = " + errString);
            if (errMsgId == 7) {//match fail,it will lockout for some time maybe 30s
             //   enrollRoundPrg.doIdentifyFailAnimation();
             //   buttonShow(false,true,true);
                //mFingerprintManager.resetTimeout(mToken); //Reset the lockout timer when asked to do so
            }
			fail();
        }
    };

    private FingerprintManager.AuthenticationCallback mAuthenticationCallback
            = new FingerprintManager.AuthenticationCallback() {
				
				

        public void onAuthenticationError(int errorCode, CharSequence errString) { 
            Log.d(TAG,"onAuthenticationError");
        }

        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
		    Log.d(TAG,"onAuthenticationHelp helpCode = " + helpCode + "	helpString = " + helpString);}

        public void onAuthenticationSucceeded(AuthenticationResult result) {
            Log.d(TAG,"onAuthenticationSucceeded ");
           // enrollRoundPrg.doIdentifySuccessAnimation();//match success
           // buttonShow(true,true,true);
        }

        public void onAuthenticationFailed() {
            Log.d(TAG,"onAuthenticationFailed");
        }

        public void onAuthenticationAcquired(int acquireInfo) {}
    };
	
	public void makeText(){
		Toast.makeText(this,"onAuthenticationSucceeded!!",Toast.LENGTH_SHORT).show();
		pass();
	}

    private FingerprintManager.RemovalCallback mRemovalCallback
            = new FingerprintManager.RemovalCallback() {
        public void onRemovalError(Fingerprint fp, int errMsgId, CharSequence errString) { }

        public void onRemovalSucceeded(Fingerprint fingerprint) {
            Log.d(TAG,"onRemovalSucceeded");
        }
    };
	
	void fail() {
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG, "Failed");
        dialog.dismiss();
        finish();
    }
	
	   private void showToast(String message) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT)
                .show();
        }
	void passwithToast(String msg) {
		showToast(msg);
        pass();
    }

    void pass() {
		
        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG, "Pass");
        dialog.dismiss();
        finish();
    }
}
