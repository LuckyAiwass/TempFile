package com.android.server.scanner.camera2;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;

import com.android.server.scanner.Scanner;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

import static android.graphics.ImageFormat.YUV_420_888;

/**
 * Created by xjf on 19-07-04.
 * This object wraps the Camera service object and expects to be the only one talking to it. The
 * implementation encapsulates the steps needed to take preview-sized images, which are used for
 * both preview and decoding.
 */
public final class CameraProviderManager {

    private static final String TAG = "USDC" + CameraProviderManager.class.getSimpleName();

    private static final int MIN_FRAME_WIDTH = 240;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MAX_FRAME_WIDTH = 1200; // = 5/8 * 1920
    private static final int MAX_FRAME_HEIGHT = 675; // = 5/8 * 1080

    private final Context context;
    //private AutoFocusManager autoFocusManager;
    private Rect framingRect;
    private Rect framingRectInPreview;
    private boolean initialized;
    private boolean previewing;
    private int requestedCameraId = 0;
    private int requestedFramingRectWidth;
    private int requestedFramingRectHeight;
    Resolution mResolution = Resolution.Resolution_1280x720;
    private int     mBarCodeReaderImageWidth = 0;
    private int     mBarCodeReaderImageHeight = 0;
    private CameraHelper mCameraHelper;
    private PreviewCallback mCameraCallback = null;
    private SurfaceTexture mSurfaceTexture = null;
    private String m_androidDeviceId = null;
    private DecodeCameraSessionCallback decodeSessionCallback;
    private int     mCurrentFocusMode = CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
    private int     mCurrentFlashMode = CameraMetadata.FLASH_MODE_OFF;
    private static CameraProviderManager cameraManager = null;
    public static enum Resolution {
        Resolution_1208x800, Resolution_1280x720, Resolution_1280x800, Resolution_1280x960, Resolution_1920x1080, Resolution_352x288, Resolution_4160x3120, Resolution_640x480;
    }
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;
    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    /**
     * The {@link android.util.Size} of camera preview.
     */
    private Size mPreviewSize;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.i(TAG, "StateCallback.onOpened");
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            /*try {
                StreamConfigurationMap map = mCameraHelper.getConfiguration(mCameraDevice.getId());
                Size correctVideoSize = new Size(1280, 720);mCameraHelper.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

                //("Media Record Size", "Supported Size. Width: " + correctVideoSize.getWidth() + "height : " + correctVideoSize.getHeight());
                String previewsSize = String.format(""+correctVideoSize.getWidth()+"x"+correctVideoSize.getHeight());

                mBarCodeReaderImageWidth = correctVideoSize.getWidth();
                mBarCodeReaderImageHeight= correctVideoSize.getHeight();
                previewSizeArray = map.getOutputSizes(ImageFormat.YUV_420_888);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            PreviewSize bestPreviewSize = setResolution(mResolution);
            mBarCodeReaderImageWidth = PreviewSize.width;
            mBarCodeReaderImageHeight= PreviewSize.height;
            startPreview();
        }

        @Override
        public void onDisconnected( CameraDevice cameraDevice) {
            Log.i(TAG, "StateCallback.onDisconnected");
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError( CameraDevice cameraDevice, int error) {
            Log.i(TAG, "StateCallback.onError " + error);
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

    };
    private class DecodeCameraSessionCallback
            extends CameraCaptureSession.StateCallback {

        public DecodeCameraSessionCallback() {
            Log.w(TAG, "DecodeCameraSessionCallback");
        }
        @Override
        public void onConfigured(CameraCaptureSession captureSession) {
            Log.w(TAG, "DecodeCameraSessionCallback onConfigured");
            if (null == mCameraDevice) {
                return;
            }
            mCameraCallback.onConfiguredCaptureSession(captureSession);
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession captureSession) {
            Log.w(TAG, "Failed to Create Camera Preview");
        }

        @Override
        public void onReady(CameraCaptureSession captureSession) {
            Log.w(TAG, "DecodeCameraSessionCallback onReady");
        }
    }
    // Start a new camera capture session
    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void startCameraPreviewSession() {

        if(mImageReader == null)
            setImagerReader();
        if(decodeSessionCallback == null) {
            decodeSessionCallback = new DecodeCameraSessionCallback();
        }
        if(mCameraCallback == null) {
            Size targetPreviewSize = new Size(mBarCodeReaderImageWidth, mBarCodeReaderImageHeight);
            mCameraCallback = new PreviewCallback(mCameraDevice,
                    /*mPreviewTexture.getSurfaceTexture()*/mSurfaceTexture,
                    targetPreviewSize, mImageReader.getSurface(), mCameraHelper );

            try {
                mCameraCallback.setmPreviewSurface(/*mPreviewTexture.getSurfaceTexture()*/mSurfaceTexture);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        try{
            mCameraCallback.startPreviewSession(mCurrentFocusMode, mCurrentFlashMode, decodeSessionCallback);
        } catch (CameraAccessException e) {
            Log.w(TAG, "start Preview Session", e);
        }
    }

    /**
     * Y im.getPlanes()[0]
     * U im.getPlanes()[1]
     * V im.getPlanes()[2]
     * YUV_420_888(4个Y对应一组UV,平均１个像素占1.5个byte,12位) y.length/4 = u.length == v.length
     * YUV_422 2个y对应一组UV 平均１个像素占2个byte,16位 (u.length == v.length) && (y.length/2> u.length) &&(y.length/2~~ u.length)
     *
     */
    void setImagerReader()
    {
        m_frameData = new byte[mBarCodeReaderImageWidth * mBarCodeReaderImageHeight * (3/2)];

        mImageReader = ImageReader.newInstance(
                mBarCodeReaderImageWidth,
                mBarCodeReaderImageHeight,
                YUV_420_888, /* ImageFormat */
                2 /* MaxImages */ );

        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, null);
    }
    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to
     * clear the handler so it will only receive one message.
     */
    private Handler previewHandler;
    private int previewMessage;
    private int delayDecode = 0;
    private static ByteBuffer m_yPlaneData ;
    private static byte[] m_frameData;
    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener
            = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            //将指定帧数据转成字节数组，类似camera1 previewcallback回调预览帧数据
            if (previewHandler != null && previewMessage == Scanner.CAMERA_MODE_DECODE) {
                if(delayDecode <= 0) {
                    Image im = reader.acquireNextImage();//ir.acquireLatestImage();
                    if(im != null) {
                        //Handler thePreviewHandler = previewHandler;
                        //Log.i(TAG, "Width: " + im.getWidth() + "height : " + im.getHeight() + "format: " + im.getFormat() + " decodes=" + delayDecode);
                        m_yPlaneData = im.getPlanes()[0].getBuffer();
                        m_yPlaneData.get(m_frameData);
                        final int width = im.getWidth();
                        final int height = im.getHeight();
                        Message message = previewHandler.obtainMessage(previewMessage, width, height, m_frameData);
                        message.sendToTarget();
                        //previewHandler = null;
                        previewMessage = Scanner.CAMERA_MODE_PREVIEW;
                        //thePreviewHandler = null;
                        // Image should be closed after we are done with it
                        im.close();
                    } else {
                        Log.i(TAG, "acquireLatestImage null: ");
                    }
                } else {
                    Image im = reader.acquireLatestImage();
                    if(im != null) {
                        im.close();
                    }
                    delayDecode = delayDecode - 1;
                    Log.d(TAG, "Got preview callback,  delayDecode=" + delayDecode);
                }
            }else{
                //long start = System.currentTimeMillis();
                Image img = reader.acquireNextImage();
                if(img != null) {
                    img.close();
                    //long startdec = System.currentTimeMillis();
                    //Log.i(TAG, "onImageAvailable data: " + (startdec - start) + " ms");
                } else {
                    //long startdec = System.currentTimeMillis();
                    //Log.i(TAG, "acquireNextImage null: " + (startdec - start) + " ms");
                }
                //Log.d(TAG, "Got preview callback, but no handler or resolution available previewMessage=" + previewMessage);
            }
        }

    };

    public static CameraProviderManager sharedObject(Context paramContext) {
        if (cameraManager == null) {
            cameraManager = new CameraProviderManager(paramContext);
        }
        return cameraManager;
    }

    private CameraProviderManager(Context context) {
        this.context = context;
        mCameraHelper = new CameraHelper(context);
        mSurfaceTexture = new SurfaceTexture(10);

    }
    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public boolean initializeCamera(int cameraId) throws IOException {
        try {
            /*if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new IOException("Time out waiting to lock camera opening.");
            }*/
            mCameraHelper.openCamera(String.valueOf(cameraId), mStateCallback);
            initialized = true;
        } catch (CameraAccessException e) {
            Log.w(TAG, "Unable to access camera: "+cameraId, e);
        }/*catch (InterruptedException e) {
            throw new IOException("Interrupted while trying to lock camera opening.", e);
        }*/
        return true;
    }
    /*
     * Terminate the active camera preview session
     */
    private void closeCameraPreviewSessionAndAll()
    {
        if (mCameraCallback != null) {
            mCameraCallback.closePreviewSession();
        }
    }

    /*
     * Terminate the active camera preview session
     */
    private void closeCameraPreviewSession() {     //change
        if (mCameraCallback != null) {
            mCameraCallback.cancelActiveCaptureSession();
        }
    }

    /*
     * Terminate the active camera session
     */
    private void closeCamera() {
        if (mCameraCallback != null) {
            mCameraCallback.cancelActiveCaptureSession();
            mCameraCallback.cancelAndAbortCaptureSession();
            mCameraCallback = null;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if(mImageReader != null)
        {
            mImageReader.close();
            mImageReader = null;
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
        mResolution = resolution;
    }

    public synchronized boolean isOpen() {
        return mCameraDevice != null;
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
        closeCamera();
        initialized = false;
        previewHandler = null;
        previewing = false;
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public synchronized void startPreview() {
        try {
            Log.d(TAG, "startPreview hasSurface " + hasSurface + previewing);
            if (mCameraDevice != null && !previewing) {
                startCameraPreviewSession();
                previewing = true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }
    /**
     * Tells the camera to stop drawing preview frames.
     */
    public synchronized void stopPreview() {
        cameraPreviewOn = false;
        if (mCameraDevice != null && previewing) {
            closeCameraPreviewSession();
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
    public void restartDecoding() {
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
