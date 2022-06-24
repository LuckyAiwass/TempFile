package com.android.server.scanner.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import com.android.server.scanner.Scanner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 */
public final class CameraManager {

    private static final String TAG = "USDC" + CameraManager.class.getSimpleName();

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

    private final Context context;
    private Camera mCamera;
    //private AutoFocusManager autoFocusManager;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private int requestedCameraId = 0;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;
    Resolution mResolution = Resolution.Resolution_1280x800;
    private static CameraManager cameraManager = null;
    public static enum Resolution {
        Resolution_1208x800, Resolution_1280x720, Resolution_1280x800, Resolution_1280x960, Resolution_1920x1080, Resolution_352x288, Resolution_4160x3120, Resolution_640x480;
    }
    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private final PreviewCallback previewCallback;
    private Handler previewHandler;
    private int previewMessage;
    private int delayDecode = 0;
    class PreviewCallback implements Camera.PreviewCallback {

        PreviewCallback() {
        }

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (previewHandler != null && previewMessage == Scanner.CAMERA_MODE_DECODE) {
                //图片补光不够
                if(delayDecode <= 0) {
                    Handler thePreviewHandler = previewHandler;
                    Message message = thePreviewHandler.obtainMessage(previewMessage, PreviewSize.width, PreviewSize.height, data);
                    message.sendToTarget();
                    previewHandler = null;
                    thePreviewHandler = null;
                } else {
                    delayDecode = delayDecode - 1;
                    Log.d(TAG, "Got preview callback,  delayDecode=" + delayDecode);
                }
            } else {
                Log.d(TAG, "Got preview callback, but no handler or resolution available previewMessage=" + previewMessage);
            }
        }

    }
    public static CameraManager sharedObject(Context paramContext) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(paramContext);
        }
        return cameraManager;
    }

    private CameraManager(Context context) {
        this.context = context;
        previewCallback = new PreviewCallback();
        surfaceTexture = new SurfaceTexture(10);

    }

    public boolean initializeCamera(int cameraId) throws IOException {
        if (mCamera == null) {
            mCamera = Camera.open(cameraId);
            if (mCamera == null) {
                throw new IOException("Camera.open() failed to return object from driver");
            }
        }

        if (!initialized) {
            initialized = true;
            setDefaultSettings();
        }

        Camera cameraObject = mCamera;
        //cameraObject.setOneShotPreviewCallback(previewCallback);
        cameraObject.setPreviewCallback(previewCallback);
        cameraObject.setPreviewTexture(surfaceTexture);
        int angle = getOrientation(cameraId);
        cameraObject.setDisplayOrientation(angle);
        return true;
    }
    public int getOrientation(int cameraId) {
        WindowManager localWindowManager = (WindowManager) context.getSystemService("window");
        Display localDisplay = localWindowManager.getDefaultDisplay();
        int displayRotation = localDisplay.getRotation();

        int rotationFromNaturalToDisplay = 0;
        int angle = 0;
        Camera.CameraInfo info = new Camera.CameraInfo();

        Camera.getCameraInfo(cameraId, info);
        Log.d(TAG, "cameraId " + cameraId);
        switch (displayRotation) {
            case Surface.ROTATION_0:
                rotationFromNaturalToDisplay = 0;
                break;
            case Surface.ROTATION_90:
                rotationFromNaturalToDisplay = 90;
                break;
            case Surface.ROTATION_180:
                rotationFromNaturalToDisplay = 180;
                break;
            case Surface.ROTATION_270:
                rotationFromNaturalToDisplay = 270;
                break;
            default:
                // Have seen this return incorrect values like -90
                if (displayRotation % 90 == 0) {
                    rotationFromNaturalToDisplay = (360 + displayRotation) % 360;
                } else {
                    throw new IllegalArgumentException("Bad rotation: " + displayRotation);
                }
        }
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            angle = (info.orientation + rotationFromNaturalToDisplay) % 360;
            angle = (360 - angle) % 360;
        } else {
            angle = (info.orientation - rotationFromNaturalToDisplay + 360) % 360;
        }
        Log.d(TAG, "rotationFromNaturalToDisplay " + angle);
        return angle;
    }
    public void setDefaultSettings() {
        try {
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> rawSupportedSizes = params.getSupportedPreviewSizes();
            PreviewSize bestPreviewSize = setResolution(mResolution);
            /*Camera.Size  previewSize =getCloselyPreSize(bestPreviewSize.x, bestPreviewSize.y, rawSupportedSizes);
            LogHelper.d("rawSupportedSizes Preview Size - w: " + previewSize.width + ", h: " + previewSize.height);
            previewSize = getClosestSize(params, bestPreviewSize.x, bestPreviewSize.y);
            LogHelper.d("getClosestSize Preview Size - w: " + previewSize.width + ", h: " + previewSize.height);*/

            params.setPreviewSize(PreviewSize.width, PreviewSize.height);
            //params.setPreviewSize(previewSize.width, previewSize.height);
            Log.d(TAG,"setPreviewSize Preview Size - w: " + PreviewSize.width + ", h: " + PreviewSize.height);
            //setHighestFrameRate(params);

            /*String isoVals = params.get("iso-values");
            Log.d(TAG,"iso-values isoVals: " + isoVals);
            if (isoVals != null) {
                String[] vals = isoVals.split(",");
                if ((vals != null) && (vals.length > 0)) {
                    for (String iso : vals) {
                        if ((iso.equals("800")) || (iso.toUpperCase().equals("ISO800"))) {
                            params.set("iso", iso);
                            break;
                        }
                    }
                }
            }
            setFocusArea(params);

            List focusModes = params.getSupportedFocusModes();
            if (focusModes != null) {
                if (focusModes.contains("macro")) {
                    params.setFocusMode("macro");
                    this.isAutoFocusSupported = true;
                } else if (focusModes.contains("auto")) {
                    params.setFocusMode("auto");
                    this.isAutoFocusSupported = true;
                }
                params.setFocusMode("continuous-video");
            }*/

            List formats = params.getSupportedPreviewFormats();
            if ((formats != null) &&
                    (formats.contains(Integer.valueOf(17)))) {
                params.setPreviewFormat(17);
            }

            if (params.isZoomSupported()) {
                params.setZoom(0);
            }

            List supportedFullImageSizes = params.getSupportedPictureSizes();
            if ((supportedFullImageSizes != null) && (supportedFullImageSizes.size() > 0)) {
                Camera.Size largestSize = (Camera.Size) params.getSupportedPictureSizes().get(0);
                //params.setPictureSize(largestSize.width, largestSize.height);
                Log.d(TAG, "setPictureSize Preview Size - w: " + largestSize.width + ", h: " + largestSize.height);
            }
            mCamera.setParameters(params);
            try
            {
                params.setPictureSize(PreviewSize.width, PreviewSize.height);
                mCamera.setParameters(params);
            }
            catch (Exception localException)
            {
                localException.printStackTrace();
            }
        } catch (Exception e) {
        }
    }
    private void setHighestFrameRate(Camera.Parameters params) {
        try {
            List<int[]> frameRanges = params.getSupportedPreviewFpsRange();
            if (frameRanges != null) {
                int highestFrameRate = 0;

                for (int[] range : frameRanges) {
                    if (range.length == 2) {
                        int max = Math.max(range[0], range[1]);
                        if (max > highestFrameRate)
                            highestFrameRate = max;
                    }
                }
                params.setPreviewFpsRange(highestFrameRate, highestFrameRate);
            }
        } catch (Exception e) {
        }
    }

    private void setFocusArea(Camera.Parameters params) {
        try {
            int maxFocusAreas = params.getMaxNumFocusAreas();
            if (maxFocusAreas > 0) {
                List<Camera.Area> areas = new ArrayList();
                Rect bounds = new Rect(-333, -333, 333, 333);
                areas.add(new Camera.Area(bounds, 1000));
                params.setFocusAreas(areas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Allows third party apps to specify the camera ID, rather than determine
     * it automatically based on available cameras and their orientation.
     *
     * @param cameraId camera ID of the camera to use. A negative value means "no preference".
     */
    public synchronized void setManualCameraId(int cameraId) {
        requestedCameraId = cameraId;
    }
    private void isStartCameraPreview() {
        if (cameraPreviewOn && !previewing) {
            startPreview();
        }
    }

    /*public synchronized View getCameraPreview() {
        if (cameraPreviewLayer == null) {
            Log.d(TAG, "init cameraPreviewLayer ");
            cameraPreviewLayer = new CameraPreviewLayer(context);
        }
        return cameraPreviewLayer;
    }*/

    public void setPreviewResolution(Resolution resolution) {
        if (mCamera != null) {
            PreviewSize localSize = setResolution(resolution);
            Camera.Parameters localParameters = mCamera.getParameters();
            localParameters.setPreviewSize(PreviewSize.width, PreviewSize.height);
            mCamera.setParameters(localParameters);
            try {
                localParameters.setPictureSize(PreviewSize.width, PreviewSize.height);
                mCamera.setParameters(localParameters);
            } catch (Exception localException2) {
                Log.e(TAG, "Could not set picture size to match preview size: " + localException2);
            }
        }
        mResolution = resolution;
    }

    public synchronized boolean isOpen() {
        return mCamera != null;
    }
  /*在停止预览时，调用自定义stopPreview()，在执行到_mCamera.release()后，出现Method called after release()异常。

          1. 具体原因

是因为在之前调用_mCamera.startPreview()方法之前，调用了_mCamera.setPreviewCallback(xxActivity.this)，导致在手动调用上面stopPreview()的时候，xxActivity.this 实现的PreviewCallback接口onPreviewFrame方法还在不停调用，具体调用频率就是当前预览的FrameRate，当stopPreview()执行完_mCamera.release()时，onPreviewFrame再次被调用时就出现了该异常。

          2. 解决办法

  在自定义的stopPreview()里面调用_mCamera.release()之前，先调用一次_mCamera.setPreviewCallback(null); ，这样在执行完_mCamera.release()，因为指定的PreviewCallback为null，因而就不会再调用onPreviewFrame，进而也不会再引用到camera和调用到其任何方法。注：onPreviewFrame第二个参数引用了camera。
*/

    /**
     * Closes the camera driver if still in use.
     */
    public synchronized void closeDriver() {
        Log.d(TAG, "close　camera　Driver release" );
        try {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                //SystemProperties.set("persist.camera.af.code.set", "0");
                mCamera = null;
                // Make sure to clear these each time we close the camera, so that any scanning rect
                // requested by intent is forgotten.
                framingRect = null;
                framingRectInPreview = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mCamera = null;
        initialized = false;
        previewHandler = null;
        //cameraPreviewLayer = null;
        previewing = false;
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview() {
        Camera theCamera = mCamera;
        try {
            Log.d(TAG, "startPreview hasSurface " + hasSurface + previewing);
            cameraPreviewOn = true;
            if (theCamera != null && !previewing) {
                theCamera.startPreview();
                previewing = true;
                //autoFocusManager = new AutoFocusManager(context, theCamera.getCamera());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            mCamera = null;
        }
    }
    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        /*if (autoFocusManager != null) {
            autoFocusManager.stop();
            autoFocusManager = null;
        }*/
        cameraPreviewOn = false;
        if (mCamera != null && previewing) {
            mCamera.stopPreview();
            previewing = false;
        }
    }

    public synchronized void stopDecoding() {
        previewMessage = Scanner.CAMERA_MODE_PREVIEW;
    }

    public void startDecoding(int delay) {
        delayDecode = delay;
        previewMessage = Scanner.CAMERA_MODE_DECODE;
    }
    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[]
     * in the message.obj field, with width and height encoded as message.arg1 and message.arg2,
     * respectively.
     *
     */
    public synchronized void setDecodeHandler(Handler handler) {
        Log.d(TAG, "requestPreviewFrame cameraPreviewOn " + cameraPreviewOn + "previewing "  +previewing);
        previewHandler = handler;
    }


    private static int findDesiredDimensionInRange(int resolution, int hardMin, int hardMax) {
        int dim = 7 * resolution / 8; // Target 5/8 of each dimension
        if (dim < hardMin) {
            return hardMin;
        }
        if (dim > hardMax) {
            return hardMax;
        }
        return dim;
    }

    public byte[] renderThumbnail(byte[] yuvData, int dataWidth, int dataHeight, int[] renderRect) {
        //Rect rect = getFramingRect();
        int width = dataWidth;
        int height = dataHeight;
        if(dataHeight < dataWidth) {
            height = width =  3 * dataHeight / 4;
        } else {
            height = width =  7 * dataWidth / 8;
        }
        int top = (dataHeight - height)/2;
        int left = (dataWidth - width)/2;
        Log.d(TAG, "renderThumbnail left = " + left  +  " top = " + top +  " width = " + width+  " height = " + height+  " dataWidth = " + dataWidth+  " dataHeight = " + dataHeight);
        /*if (left + width > dataHeight || top + height > dataHeight) {
            left = 0;
            top = 0;
            width = dataWidth;
            height = dataHeight;
        }*/
        //leftOffset = 45 topOffset = 325 width = 630 height = 630

      /*  left = 325;


        top = 45;
        width = 630;
        height = 630;*/
        byte[] pixels = new byte[width * height];
        int inputOffset = top * dataWidth + left;
        for (int y = 0; y < height; y++) {
            int outputOffset = y * width;
            System.arraycopy(yuvData, inputOffset, pixels, outputOffset, width);
            inputOffset += dataWidth;
        }
        renderRect[0] = width;
        renderRect[1] = height;
        return pixels;
    }
    static class PreviewSize {
        public static int width;
        public static int height;
        public PreviewSize(int w, int h) {
            width = w;
            height = h;
        }
    }
    private PreviewSize setResolution(Resolution paramResolution) {
        Log.d(TAG, "setResolution " + paramResolution);
        //Resolution_1208x800, Resolution_1280x720, Resolution_1280x800, Resolution_1280x960, Resolution_1920x1080, Resolution_352x288, Resolution_4160x3120, Resolution_640x480;
        if(Resolution.Resolution_352x288 == paramResolution) {
            return new PreviewSize(352, 288);
        } else if(Resolution.Resolution_640x480 == paramResolution) {
            return new PreviewSize(640, 480);
        }else if(Resolution.Resolution_1280x720 == paramResolution) {
            return new PreviewSize(1280, 720);
        }else if(Resolution.Resolution_1280x800 == paramResolution) {
            return new PreviewSize(1280, 800);
        }else if(Resolution.Resolution_1920x1080 == paramResolution) {
            return new PreviewSize(1920, 1080);
        } else {
            return new PreviewSize(640, 480);
        }
    }

    private boolean cameraPreviewOn = false;
    private boolean cameraTorchOn = false;
    //CameraPreviewLayer cameraPreviewLayer;
    private SurfaceHolder surfaceHolder;
    private SurfaceTexture surfaceTexture;
    private boolean hasSurface = false;
    private boolean hidePreview = true;
    //surfaceHolder.setFormat(PixelFormat.TRANSPARENT);//translucent半透明 transparent透明
            /*SURFACE_TYPE_NORMAL：用RAM缓存原生数据的普通Surface
            SURFACE_TYPE_HARDWARE：适用于DMA(Direct memory access )引擎和硬件加速的Surface
            SURFACE_TYPE_GPU：适用于GPU加速的Surface
            SURFACE_TYPE_PUSH_BUFFERS：表明该Surface不包含原生数据，Surface用到的数据由其他对象提供，
            在Camera图像预览中就使用该类型的Surface，有Camera负责提供给预览Surface数据，
            这样图像预览会比较流畅。如果设置这种类型则就不能调用lockCanvas来获取Canvas对象了*/
    /*public class CameraPreviewLayer extends SurfaceView implements SurfaceHolder.Callback {
        public CameraPreviewLayer(Context context) {
            super(context);
            surfaceHolder = getHolder();
            surfaceHolder.addCallback(this);

            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int measuredWidth = getMeasuredWidth();
            int measuredHeigh = getMeasuredHeight();
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);   //获取宽的模式
            int heightMode = MeasureSpec.getMode(heightMeasureSpec); //获取高的模式
            LogHelper.d("onMeasure Preview Size - w: " + measuredWidth + ", h: " + measuredHeigh);
            if (camera != null) {
                int degress = configManager.getOrientation(requestedCameraId);
                Point point = setResolution(mResolution.ordinal());//configManager.getBestPreviewSize();
                Camera.Parameters parameters = camera.getCamera().getParameters();
                Camera.Size localSize = parameters.getPreviewSize();
                LogHelper.d("parameters Preview Size - w: " + localSize.width + ", h: " + localSize.height);
                int dstWidth = localSize.width;
                int dstHeight = localSize.height;
                int w;
                if ((degress == 90) || (degress == 270)) {
                    w = dstWidth;
                    dstWidth = dstHeight;
                    dstHeight = w;
                }
                int h;
                if (measuredWidth * dstHeight < measuredHeigh * dstWidth) {
                    w = dstWidth * measuredHeigh / dstHeight;
                    h = measuredHeigh;
                } else {
                    h = dstHeight * measuredWidth / dstWidth;
                    w = measuredWidth;
                }

                setMeasuredDimension(w, h);
                float f1 = (measuredWidth - w) / 2.0F;
                float f2 = (measuredHeigh - h) / 2.0F;

                setTranslationX(f1);
                setTranslationY(f2);
            } else {
                LogHelper.d("camera null ,onMeasure Preview Size - w: " + measuredWidth + ", h: " + measuredHeigh);
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            if (surfaceHolder == null) {
                LogHelper.d("*** WARNING *** surfaceCreated() gave us a null surface!");
            }
            LogHelper.d("surfaceCreated hasSurface " + hasSurface);
            if (!hasSurface) {
                hasSurface = true;
                try {
                    initializeCamera(surfaceHolder);
                    LogHelper.d("surfaceCreated");
                    isStartCameraPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                    LogHelper.e(e);
                    hasSurface = false;
                    throw new RuntimeException("Fail to connect to camera service");
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
            LogHelper.d("surfaceChanged Preview Size - w: " + width + ", h: " + height);
            try {
                if (camera != null) {
                    Camera.Parameters localParameters = camera.getCamera().getParameters();
                    WindowManager localWindowManager = (WindowManager) context.getSystemService("window");
                    Display localDisplay = localWindowManager.getDefaultDisplay();

                    Camera.CameraInfo localCameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(requestedCameraId, localCameraInfo);
                    int i;
                    if (localCameraInfo.facing == 1) {
                        i = (localCameraInfo.orientation + localDisplay.getRotation()) % 360;
                        i = (360 - i) % 360;
                    } else {
                        i = (localCameraInfo.orientation - localDisplay.getRotation() + 360) % 360;
                    }
                    localParameters.setPreviewSize(height, width);
                    LogHelper.d("surfaceChanged Preview Size - w: " + width + ", h: " + height + " setDisplayOrientation " + i);
                    camera.getCamera().setDisplayOrientation(i);
                }
            } catch (Exception localException) {
                LogHelper.d("SurfaceChangedException:", localException.toString());
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            Log.v(TAG, "surfaceDestroyed Preview");
            hasSurface = false;
        }
    }*/
}
