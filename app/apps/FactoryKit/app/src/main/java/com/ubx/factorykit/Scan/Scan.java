/*
 * Copyright (c) 2011-2012 urovo Technologies, Inc.  All Rights Reserved.
 * urovo Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.Scan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.device.ScanManager;
import android.widget.TextView;

import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;
import com.ubx.factorykit.Framework.Framework;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.Iterator;
import java.util.Map;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import com.qti.factory.Framework.ShellUtils;
import com.qti.factory.Framework.ShellUtils.CommandResult;
import android.os.Handler;
import android.os.Message;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import android.view.Window;
import android.view.WindowManager;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.PixelFormat;
import android.widget.Toast;
import android.device.scanner.configuration.PropertyID;



public class Scan extends Activity {

    private static final String TAG = "Scan";
    private static String resultString = Utilities.RESULT_FAIL;
    private static Context mContext;
	TextView mTextView;
    private EditText focusView;
    private static String barcodeStr;
    private final static String SCAN_ACTION = "urovo.rcv.message";
    ScanManager mScanManager;
    private boolean soundstate = false;
    private boolean modestate = false;
    private boolean powerstate = false;
    private boolean lockstate = false;
	private static final String SCAN_DEV = SystemProperties.get("persist.sys.scanner.type","/sys/kernel/kobject_scanner_led/scanner_type");
	private static final String SCAN_PRO = SystemProperties.get("persist.sys.scanner.state","/sys/kernel/kobject_scanner_led/scanner_typestate");
	private Handler mHandler = new Handler();
    private int[] id_buffer = new int[] {
            PropertyID.DEC_OCR_MODE,
            PropertyID.DEC_OCR_TEMPLATE,
    };
    private int[] value_buffer = new int[2];
    private int[] update_value_buffer = new int[2];
    int ScannerType = 0;
	private String scan_enabled = ""; 
    private String scan_pro_result = ""; 	
	private String scan_type = "null";
	private String scan_result="null";
	private Camera mCamera = null;
    private Button passButton, failButton;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
	private boolean isCamera = false;
    private static final int REQUEST_CODE_SCAN = 0x0000;
    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";
    private static final String DECODED_STATE_KEY = "codedState";
    private int scanOption = -1;
    private int scanMode = -1;
    private int scanSound = -1;
    private int scanTriggler = -1;


    // private CharSequence entries[];
    // private CharSequence entriesvaule[];
    @Override
    public void finish() {
		if(isCamera){
    	  stopCamera();
		}
        Utilities.writeCurMessage(this, TAG, resultString);
		if(FactoryKitPro.IS_UROVO_VERSION)
        unregisterReceiver(mScanReceiver);
        super.finish();
    }

    @Override
    protected void onPause() {

        super.onPause();
        if(mScanManager != null && (ScannerType == 5 || ScannerType == 8 || ScannerType == 15)) {
            enableOCR(value_buffer);
        }
        setPrevScanParamaters();
        mScanManager = null;
    }
    @Override
    public void onResume() {
        super.onResume();
		if(FactoryKitPro.IS_UROVO_VERSION)
        initScan();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
		mContext =getApplicationContext();
		if(FactoryKitPro.IS_UROVO_VERSION)
		initUrovo();
		else if(Utilities.checkApk("com.ubx.scandemo",mContext))
        initScanDemo(mContext);
		else
		initOther();
    }
	
	private void init(Context context) {
        mContext = context;
        resultString = Utilities.RESULT_FAIL;
    }
	
	private void initUrovo(){
		init(getApplicationContext());
        setRequestedOrientation(Framework.orientation);

        if(Build.PROJECT.equals("SQ46"))
            setContentView(R.layout.scan_sq46);
        else {
            setContentView(R.layout.scan);
            mTextView = (TextView) findViewById(R.id.otg_hint);
            mTextView.setVisibility(View.GONE);
        }
        focusView = (EditText) findViewById(R.id.focus);
        focusView.setVisibility(View.VISIBLE);
        // urovo add 屈臣氏默认弹出键盘，避免测试无法显示扫描结果 2020-01-17 begin
        //if(android.os.Build.PWV_CUSTOM_CUSTOM.equals("QCS"))
        {
            focusView.setFocusable(true);
            focusView.setFocusableInTouchMode(true);
            focusView.requestFocus();
            getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
        // urovo add 屈臣氏默认弹出键盘，避免测试无法显示扫描结果 2020-01-17 end
        Button pass = (Button) findViewById(R.id.pass);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SCAN_ACTION);
        registerReceiver(mScanReceiver, filter);
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
        Button btscannertype = (Button) findViewById(R.id.scannertype);
		btscannertype.setVisibility(View.VISIBLE);
        btscannertype.setText("Scanner");
        btscannertype.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                showScannerType();
            }
        });
	}



	private void init6703(Context context) {

		setRequestedOrientation(Framework.orientation);
		mContext = context;
		resultString = Utilities.RESULT_FAIL;
		mTextView = (TextView) findViewById(R.id.otg_hint);
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

    private void initScanDemo(Context context) {

        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.scan);
        //if(SystemProperties.get("persist.vendor.sys.scan.name","null").equals("null"))
        //    SystemProperties.set("persist.vendor.sys.scan.name","n6700");
        mTextView = (TextView) findViewById(R.id.otg_hint);
        mTextView.setVisibility(View.GONE);
        mContext = context;
        resultString = Utilities.RESULT_FAIL;
        mTextView = (TextView) findViewById(R.id.otg_hint);
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
        try {
            Intent intent = new Intent();
            intent.setClassName("com.ubx.scandemo","com.ubx.scandemo.ScanDemoActivity");
            startActivityForResult(intent, REQUEST_CODE_SCAN);
            //startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this,R.string.toast_no_apk,Toast.LENGTH_SHORT).show();
            fail(null);
            e.printStackTrace();
        }
    }

    private void initOther(){
		readdata();
		readproresult();
		Log.d(TAG,"scan_type=="+scan_type+"---scan_result=="+scan_result);
		if(scan_enabled!=null&&scan_result!=null){
			//mTextView.setText(scan_enabled);
			if(scan_enabled.contains("3")&&scan_result.contains("1")){
				setContentView(R.layout.scan);
				init6703(getApplicationContext());
				mTextView.setText("6703 OK PASS");
			}else if(scan_enabled.contains("2")||scan_enabled.contains("8")){
				this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				setContentView(R.layout.scan_camera);
				mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
				mSurfaceHolder = mSurfaceView.getHolder();
				mSurfaceHolder.addCallback(callback);
				mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
				bindView();
				isCamera = true;
			}else{

				fail(null);
			}
		}else{
			fail(null);
		}

	}
	
	
	 private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

        // SurfaceHolder callbacks
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //Log.v(TAG, "surfaceChanged: width =" + width + ", height = " + height);
			logd("surfaceChanged");
		    startCamera();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.v(TAG, "surfaceCreated");
           int oritationAdjust = 270;
		try {
			mCamera = Camera.open(2);
			//mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_EXTERNAL);
			mCamera.setDisplayOrientation(oritationAdjust);
		} catch (Exception exception) {
			//toast(getString(R.string.cameraback_fail_open));
			//Toast.makeText(this,R.string.no_scan_camera, Toast.LENGTH_SHORT).show();
			mCamera = null;
		}

		if (mCamera == null) {
			fail(null);
		} else {
			try {
				mCamera.setPreviewDisplay(mSurfaceHolder);
			} catch (IOException exception) {
				mCamera.release();
				mCamera = null;
				finish();
			}
		}
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.v(TAG, "surfaceDestroyed");
            mSurfaceHolder = null;
            //previewUIDestroyed();
        }
    };
	
	void bindView() {

		//takeButton = (Button) findViewById(R.id.take_picture);
		passButton = (Button) findViewById(R.id.camera_pass);
		failButton = (Button) findViewById(R.id.camera_fail);		
		passButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {

				setResult(RESULT_OK);
				Utilities.writeCurMessage(mContext, TAG, "Pass");
				finish();
			}
		});
		failButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View arg0) {

				setResult(RESULT_CANCELED);
				Utilities.writeCurMessage(mContext, TAG, "Failed");
				finish();
			}
		});

	}
	
	
	private void readdata() { 
       try {
        FileInputStream fp_read = new FileInputStream(SCAN_DEV);
	    byte[] mbyte = new byte[13];
	    fp_read.read(mbyte);
	    scan_enabled = new String(mbyte); 
		Log.d(TAG,"scan_enabled==" + scan_enabled);
		fp_read.close();		
        } catch (Exception e) {
          e.printStackTrace();
		} 
		if(scan_enabled!=null){			
			//mTextView.setText(scan_enabled);
			if(scan_enabled.contains("3")){
				scan_type = "3";				
			}else if(scan_enabled.contains("1")){
				scan_type = "2";				
			}else{
				Toast.makeText(this,"6703 FAIL", Toast.LENGTH_SHORT).show();
				fail(null);
			}
		}else{
			Toast.makeText(this,"No Scan Found!!", Toast.LENGTH_SHORT).show();
			fail(null);
		}
   }
   
   private void readproresult() { 
       try {
        FileInputStream fp_read = new FileInputStream(SCAN_PRO);
	    byte[] mbyte = new byte[13];
	    fp_read.read(mbyte);
	    scan_pro_result = new String(mbyte); 
		Log.d(TAG,"scan_pro_result==" + scan_pro_result);
		fp_read.close();		
        } catch (Exception e) {
          e.printStackTrace();
		} 
		if(scan_pro_result!=null){			
			//mTextView.setText(scan_enabled);
			if(scan_pro_result.contains("1")){
				scan_result = "1";								
			}else{
				Toast.makeText(this,"6703 PRO FAIL", Toast.LENGTH_SHORT).show();
				fail(null);
			}
		}else{
			Toast.makeText(this,"No Scan Found!!", Toast.LENGTH_SHORT).show();
			fail(null);
		}
		
   }
   
  
	private void startCamera() {

		if (mCamera != null) {
			try {
				Camera.Parameters parameters = mCamera.getParameters();
				parameters.setPictureFormat(PixelFormat.JPEG);
				// parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
				// parameters.setRotation(CameraInfo.CAMERA_FACING_FRONT);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			} catch (Exception e) {
				loge(e);
			}
		}

	}

	private void stopCamera() {

		if (mCamera != null) {
			try {
				if (mCamera.previewEnabled())
					mCamera.stopPreview();
				mCamera.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder surfaceholder) {

		logd("surfaceDestroyed");
		stopCamera();
	}

    
    void fail(Object msg) {
        loge(msg);
        Utilities.writeCurMessage(mContext, TAG, "Failed");
        setResult(RESULT_CANCELED);
        resultString = Utilities.RESULT_FAIL;
        finish();
    }

    void pass() {
        Utilities.writeCurMessage(mContext, TAG, "Pass");
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
	
	    private BroadcastReceiver mScanReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
//            focusView.setText("");

            byte[] barocode = intent.getByteArrayExtra("barocode");
            int barocodelen = intent.getIntExtra("length", 0);
            byte temp = intent.getByteExtra("barcodeType", (byte) 0);
            loge(temp);
            barcodeStr = new String(barocode, 0, barocodelen);
            focusView.setText(barcodeStr);
        }

    };
    
    private void initScan() {
        // TODO Auto-generated method stub
        mScanManager = new ScanManager();
        getScanParamaters();
        if(mScanManager.getOutputParameter(4) != 1){
            //mScanManager.setOutputParameter(4, 1);
            mScanManager.openScanner();
            powerstate = true;
        } 
		if (mScanManager.getOutputParameter(5) != 1) {
			modestate = true;
			mScanManager.setOutputParameter(5, 1);
		}
        if (mScanManager.getOutputParameter(1) != 1) {
            mScanManager.setOutputParameter(1, 1);
            soundstate = true;
        }
        if(mScanManager.getOutputParameter(6) == 1){
            mScanManager.unlockTriggler();
            lockstate = true;
        }

        ScannerType = mScanManager.getScannerType();
        if(ScannerType <= 0 && Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
            ScannerType = 13;//4710
            mScanManager.setOutputParameter(7, ScannerType);            
        }
        initScannerList(ScannerType);
        /*int i;
        int length = mScanManager.getScanerList().size();
        for (i=0;i<length;i++){
            if(String.valueOf(type) == entriesvaule[i].toString())
                setTitle(entries[i]);
        }*/
        if(ScannerType <= 0 || currentType == -1) {
            showScannerType();
        }
        Log.d(TAG, currentType + " showScannerType " + ScannerType);
        //默认开启ocr功能
        if(ScannerType == 5 || ScannerType == 8 || ScannerType == 15) {
            mScanManager.getPropertyInts(id_buffer, value_buffer);
            update_value_buffer[0] = 1;
            update_value_buffer[1] = 1;
            enableOCR(update_value_buffer);
        }
    }
    String[] entries;
    int entriesvaule[];
    int currentType = -1;
    void initScannerList(int type) {
        Map<String, Integer> scanTypelist = mScanManager.getScanerList();
        if(scanTypelist != null) {
            int length = scanTypelist.size();
            entries = new String[length];
            entriesvaule = new int[length];
            try{
                Iterator mi = scanTypelist.entrySet().iterator();
                int i = 0;
                while (mi.hasNext()) {
                    Map.Entry e = (Map.Entry) mi.next();
                    Log.d(TAG, "initScannerList entryList " + e.getKey() + "=" + e.getValue());
                    entries[i] = e.getKey().toString();
                    entriesvaule[i] = (Integer)(e.getValue());
                    i++;
                }
                i = 0;
                if(entries != null) {
                    for (i = entriesvaule.length - 1; i > 0; i--)
                    {
                        //在 0-i 范围内，将该范围内最大的数字沉到i
                        for (int j = 0; j < i; j++)
                        {
                            if (entriesvaule[j] < entriesvaule[j+1])
                            {
                                //交换
                                int temp = entriesvaule[j];
                                entriesvaule[j] = entriesvaule[j+1];
                                entriesvaule[j+1] = temp;
                                
                                String name = entries[j];
                                entries[j] = entries[j+1];
                                entries[j+1] = name;
                            }
                        }
                    }
                    i = 0;
                    for (i = 0;i < entriesvaule.length ; i++)
                    {
                        if(type == entriesvaule[i]) {
                            currentType = i;
                            break;
                        }
                        Log.d(TAG, "sort entryList " + entries[i] + "=" + entriesvaule[i] );
                    }
                    if(currentType > 0 && currentType < entries.length)
                        setTitle(entries[currentType]);
                }
                
                if(currentType >=length) {
                    currentType = -1;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void enableOCR(int[] buffer) {
        try{
            if(mScanManager != null) {
                mScanManager.setPropertyInts(id_buffer, buffer);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void showScannerType() {
        if (entries == null) {
            Log.d(TAG, "entries null " );
            return;
        }
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Scanner Type")
                .setSingleChoiceItems(entries, currentType, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Toast.makeText(MainActivity.this, items[which],
                        // Toast.LENGTH_SHORT).show();
                        currentType = which;
                        ScannerType = entriesvaule[which];
                        setTitle(entries[currentType]);
                        Log.d(TAG, currentType + " showScannerType " + ScannerType);
                        mScanManager.setOutputParameter(7, ScannerType);
                        if(ScannerType == 5 || ScannerType == 8 || ScannerType == 15) {
                            update_value_buffer[0] = 1;
                            update_value_buffer[1] = 1;
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    enableOCR(update_value_buffer);
                                }
                            }, 2000);   
                        }
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                //String content = data.getStringExtra(DECODED_CONTENT_KEY);
                //byte[] bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);
                boolean state = data.getBooleanExtra(DECODED_STATE_KEY , false);
                if(state) {
                    pass();
                    return;
                }

            }
        }

        fail(null);

    }

    /*进入扫描测试界面获取扫描相关设置参数*/
    private void getScanParamaters(){
        scanOption = mScanManager.getOutputParameter(4);
        scanMode = mScanManager.getOutputParameter(5);
        scanSound = mScanManager.getOutputParameter(1);
        scanTriggler = mScanManager.getOutputParameter(6);
        Log.d("zml","getScanParamaters  scanOption=" + scanOption +" scanMode=" + scanMode +" scanSound=" + scanSound +" scanTriggler=" + scanTriggler);
    }
    /*退出扫描测试界面时,重新将扫描相关设置参数设置为进入界面之前的参数*/
    private void setPrevScanParamaters(){
        if(!FactoryKitPro.IS_UROVO_VERSION)
            return;
        if(powerstate && scanOption == 0)
            mScanManager.closeScanner();
        if(modestate && scanMode != -1)
            mScanManager.setOutputParameter(5,scanMode);
        if(soundstate && scanSound != -1)
            mScanManager.setOutputParameter(1,scanSound);
        if(lockstate && scanTriggler == 1)
            mScanManager.lockTrigger();
        Log.d("zml","setPrevScanParamaters   ");
        getScanParamaters();

    }
}
