package com.ubx.factorykit.CameraBack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.camera.CameraPreview;
import com.ubx.factorykit.BeeperSingle;
import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class CameraBack extends AppCompatActivity {

    CameraPreview cameraView;
    private String resultString = Utilities.RESULT_FAIL;
    final static String TAG = "CameraBack";
    private Button takeButton, passButton, failButton;
    private boolean hasFront = false;
    private boolean hasBack = false;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera camera;
    private int backCameraId = 0;
    private boolean singleCamera = false;
    private static Context mContext = null;
    private BeeperSingle beeper;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    cameraView.takePicture();
                    mHandler.sendEmptyMessageDelayed(2, 1000);
                    break;
                case 1:
                    cameraView.takePicture();
                    mHandler.sendEmptyMessageDelayed(3, 1000);
                    break;
                case 2:
                    if (hasFront) {
                        cameraView.setCamera(true);
                        cameraView.switchCamera();
                        mHandler.sendEmptyMessageDelayed(1, 2000);
                    } else {
                        mHandler.sendEmptyMessageDelayed(3, 1000);
                    }
                    break;
                case 3:
                    mHandler.removeCallbacksAndMessages(null);
                    setResult(10);
                    finish();
                    break;
                case 4:
                    mHandler.removeMessages(7);
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] bytes, Camera camera) {
                            if (singleCamera) {
                                setResult(10);
                                finish();
                            } else {
                                if (hasBack) {
                                    //mHandler.sendEmptyMessageDelayed(5, 2000);
                            } else {
                                setResult(10);
                                finish();
                            }
                            }
                        }
                    });
                    break;
                case 5:
                    if (camera != null) {
                        camera.setPreviewCallback(null);
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                    if (hasFront) {
//                        mHandler.sendEmptyMessageDelayed(7, 10000);
                        initCamera(false);
//                        camera.startPreview();
                    } else {
                        mHandler.sendEmptyMessageDelayed(3, 1000);
                    }
                    break;
                case 6:
                    mHandler.removeMessages(7);
                    camera.takePicture(null, null, new Camera.PictureCallback() {
                        @Override
                        public void onPictureTaken(byte[] bytes, Camera camera) {
                            //mHandler.sendEmptyMessageDelayed(3, 1000);
                        }
                    });
                    break;
                case 7:
                    mHandler.removeCallbacksAndMessages(null);
                    setResult(10);
                    finish();
                    break;
                default:
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("zgy", "onCreate");
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.camera_back);
        mContext = this;
        takeButton = (Button) findViewById(R.id.take_picture);
        passButton = (Button) findViewById(R.id.camera_pass);
        failButton = (Button) findViewById(R.id.camera_fail);
        mSurfaceView = (SurfaceView) findViewById(R.id.mSurfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        beeper = new BeeperSingle(this,R.raw.beeper);
//        singleCamera = ModelUtil.singleCamera();
        Log.v("zgy", "singleCamera:" + singleCamera);
//        if (!singleCamera) {
            int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
            Log.v("zgy","numberOfCameras:" +numberOfCameras);
            for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraId, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK /*&& !hasBack*/) {
                    // 后置摄像头信息
                    // urovo huangjiezhou add begin on 20211028
                    if (hasBack && cameraId == numberOfCameras - 1)
                        continue;
                    // urovo huangjiezhuo add end
                    backCameraId = cameraId;
                    Log.v("zgy","back id:" + backCameraId);
                    hasBack = true;
                } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    // 前置摄像头信息
                    hasFront = true;
                }
            }
//        }
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Log.v("zgy", "hasFront:" + hasFront);
        Log.v("zgy", "hasBack:" + hasBack);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (singleCamera) {
                            initCamera(true);
                        } else {
                            if (hasBack) {
                                initCamera(true);
                            } else {
                                initCamera(false);
                            }
                        }
//                        mHandler.sendEmptyMessageDelayed(4, 4000);
                    }
                }, 500);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                if (camera != null) {
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }
            }
        });
        takeButton.setOnClickListener(new Button.OnClickListener() {

            public void onClick(View arg0) {

                takeButton.setVisibility(View.GONE);
                try {
                    if (camera != null) {
                            //urovo huangjiezhou modify 20210831
                            //beeper.play();
                            takePicture();
                    } else {
                        finish();
                    }
                } catch (Exception e) {
                    fail(getString(R.string.autofocus_fail));
                }
            }
        });

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

    private void takePicture() {

        if (camera != null) {
            try {
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        takeButton.setVisibility(View.GONE);
                        passButton.setVisibility(View.VISIBLE);
                        failButton.setVisibility(View.VISIBLE);
                    }
                });
            } catch (Exception e) {
                finish();
            }
        } else {
            finish();
        }
    }

    private void initCamera(boolean isBack) {
        try {
            int cameraId = isBack ? backCameraId : backCameraId + 1;
            Log.v("zgy", "cameraId:" + cameraId);
            camera = Camera.open(cameraId);
            if (camera != null) {
                camera.setDisplayOrientation(getDisplayOrientation(getDisplayRottation(this),cameraId));
                Camera.Parameters parameters = camera.getParameters();
                if (!FactoryKitPro.PRODUCT_SQ28)
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                if (isBack) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
//                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                //通过SurfaceView显示预览
                camera.setParameters(parameters);
                camera.setPreviewDisplay(mSurfaceHolder);
                camera.startPreview();
                //开始预览
            }
        } catch (Exception e) {
            e.printStackTrace();
            mHandler.sendEmptyMessage(3);
        }
    }

    @Override
    protected void onResume() {
        Log.v("zgy", "camera resume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacksAndMessages(null);
        if (camera != null) {
            try {
                camera.setPreviewCallback(null) ;
                camera.stopPreview();
                camera.release();
                camera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void takePic(View view) {
        cameraView.takePicture();
    }

    @Override
    public void onBackPressed() {
        setResult(20);
        finish();
    }

    public static int getDisplayOrientation(int degree, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int iRet;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            iRet = (info.orientation + degree) % 360;
            iRet = (360 - iRet) % 360;
        } else {
            iRet = (info.orientation - degree + 360) % 360;
        }
        return iRet;
    }

    public static int getDisplayRottation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
            default:
                return 270;
        }
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
        if(beeper != null )
            beeper.release();

        Utilities.writeCurMessage(TAG, resultString);
        super.finish();
    }
}
