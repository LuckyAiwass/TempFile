package com.android.server.scanner.camera2;

import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xjf on 19-07-04.
 * Base implementation of camera preview functions, common to all
 * camera application operating modes.
 */
public class PreviewCallback {
    private static final String TAG =
            PreviewCallback.class.getSimpleName();

    private Size mTargetPreviewSize;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest.Builder mSnapshotRequestBuilder;

    private final CameraDevice   mCameraDevice;
    private  SurfaceTexture mPreviewSurface;
    private  Surface        mOutputSurface = null;
    private  Surface        mImageReaderSurface = null;
    private  Surface        mPreviousImageReaderSurface = null;
    private CameraCaptureSession mActiveCaptureSession;
    private CaptureRequest.Builder mBuilder = null;
    private CameraHelper mCameraHelper = null ;

    public PreviewCallback(CameraDevice device,
                           SurfaceTexture surface,
                           Size targetPreviewSize,
                           Surface imageReaderSurface,
                           CameraHelper cameraHelper
    ) {
        mCameraDevice = device;
        mCameraHelper = cameraHelper;
        mPreviewSurface = surface;
        mTargetPreviewSize = targetPreviewSize;
        mImageReaderSurface = imageReaderSurface;

    }

    //Request for a basic preview
    protected CaptureRequest.Builder createPreviewRequestBuilder()
            throws CameraAccessException {
        if(getCameraDevice() != null)
            return(getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW));
        else
            return null;
    }

    //Request for snapshotPreview //
    protected CaptureRequest.Builder createSnapshotBuilder()
            throws CameraAccessException {
        if ( getCameraDevice() != null ){
            return (getCameraDevice().createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE));
        } else{
            return null;
        }
    }
    //Return all target surfaces for camera frames
    protected List<Surface> getCaptureTargets() {
        List<Surface> baseTargets = new ArrayList<Surface>();
        baseTargets.add(mOutputSurface);
        baseTargets.add(mImageReaderSurface);
        return baseTargets;
    }

    /*
     * The same builder is used for all repeated requests, and some
     * state is shared between them. The object is lazily created
     * the first time it is needed.
     */
    protected final CaptureRequest.Builder getPreviewRequestBuilder()
            throws CameraAccessException {
        if (mPreviewRequestBuilder == null) {
            mPreviewRequestBuilder = createPreviewRequestBuilder();
        }

        return mPreviewRequestBuilder;
    }
    protected final CaptureRequest.Builder getSnapshotRequestBuilder()
            throws CameraAccessException {
        if (mSnapshotRequestBuilder == null)
        {
            mSnapshotRequestBuilder = createSnapshotBuilder();
        }
        return mSnapshotRequestBuilder;
    }

    protected final CameraDevice getCameraDevice() {
        return mCameraDevice;
    }

    private void setActiveCaptureSession(CameraCaptureSession session) {
        mActiveCaptureSession = session;
    }

    protected final CameraCaptureSession getActiveCaptureSession() {
        return mActiveCaptureSession;
    }

    public void cancelActiveCaptureSession() {
        if (mActiveCaptureSession != null) {
            try {
                mActiveCaptureSession.stopRepeating();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelAndAbortCaptureSession() {
        if (mActiveCaptureSession != null) {
            try {
                mActiveCaptureSession.abortCaptures();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    //Restart preview with existing camera settings
    public void restartPreview(int effect) throws CameraAccessException {
        final CaptureRequest.Builder builder = getPreviewRequestBuilder();
        builder.set(CaptureRequest.CONTROL_EFFECT_MODE, effect);
        getActiveCaptureSession().setRepeatingRequest(builder.build(),
                null, null);
    }

    public void setmPreviewSurface(SurfaceTexture surface)
            throws CameraAccessException {

        mPreviewSurface = surface;
    }
    public void closePreviewSession()
    {
        if(mActiveCaptureSession!=null) {
            mActiveCaptureSession.close();
            mActiveCaptureSession = null;
        }
    }

    /*
     * Begin still picture data.
     */
    public void startStillCapture( int currentFocusMode, int currentFlashMode, CameraCaptureSession.StateCallback CameraSessionCallback)
            throws CameraAccessException
    {
        //Preview request contains state we need to reset
        // when we start a new preview session.
        mSnapshotRequestBuilder = null;

        // Configure the size of default buffer match the camera preview.
        if(mPreviewSurface != null) {
            mPreviewSurface.setDefaultBufferSize(mTargetPreviewSize.getWidth(),
                    mTargetPreviewSize.getHeight());
        }
        else
        {
            Log.i(TAG, "preview surface is null");
        }

        // This is the output Surface we need to start preview.
        mOutputSurface = new Surface(mPreviewSurface);

        // We set up a CaptureRequest.Builder with the output Surface.
        // CaptureRequest.Builder builder = getSnapshotRequestBuilder();
        //CaptureRequest.Builder builder = getPreviewRequestBuilder();
        CaptureRequest.Builder builder = getSnapshotRequestBuilder();
        builder.addTarget(mOutputSurface);

        // Add Image Reader
        builder.addTarget(mImageReaderSurface);

        // Setup the focus mode desired
        switch(currentFocusMode)
        {
            case CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                break;
            case CaptureRequest.CONTROL_AF_MODE_AUTO:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                break;
            case CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                break;
            case CaptureRequest.CONTROL_AF_MODE_MACRO:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_MACRO);
                break;
            case CaptureRequest.CONTROL_AF_MODE_OFF:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                break;
            case CaptureRequest.CONTROL_AF_MODE_EDOF:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_EDOF);
                break;
            default:
                break;
        }

        // Setup the flash mode desired
        switch(currentFlashMode)
        {
            case CaptureRequest.FLASH_MODE_OFF:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                break;
            case CaptureRequest.FLASH_MODE_SINGLE:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                break;
            case CaptureRequest.FLASH_MODE_TORCH:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                break;
            default:
                break;
        }

        mBuilder = builder;
        // Here, we create a CameraCaptureSession for camera preview.
        getCameraDevice().createCaptureSession(getCaptureTargets(),
                CameraSessionCallback, null);
    }

    /*
     * Begin streaming preview data.
     */
    public void startPreviewSession(int currentFocusMode, int currentFlashMode, CameraCaptureSession.StateCallback CameraSessionCallback)
            throws CameraAccessException {

        //Preview request contains state we need to reset
        // when we start a new preview session.
        mPreviewRequestBuilder = null;

        // Configure the size of default buffer match the camera preview.
        if(mPreviewSurface != null) {
            mPreviewSurface.setDefaultBufferSize(mTargetPreviewSize.getWidth(),
                    mTargetPreviewSize.getHeight());
        }
        else
        {
            Log.i(TAG, "preview surface is null");
        }

        // This is the output Surface we need to start preview.
        mOutputSurface = new Surface(mPreviewSurface);

        // We set up a CaptureRequest.Builder with the output Surface.
        CaptureRequest.Builder builder = getPreviewRequestBuilder();
        //builder.addTarget(mOutputSurface);

        // Add Image Reader
        builder.addTarget(mImageReaderSurface);

        // Setup the focus mode desired
        /*switch(currentFocusMode)
        {
            case CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                break;
            case CaptureRequest.CONTROL_AF_MODE_AUTO:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                break;
            case CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                break;
            case CaptureRequest.CONTROL_AF_MODE_MACRO:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_MACRO);
                break;
            case CaptureRequest.CONTROL_AF_MODE_OFF:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
                break;
            case CaptureRequest.CONTROL_AF_MODE_EDOF:
                builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_EDOF);
                break;

            default:
                break;

        }*/

        // Setup the flash mode desired
        /*switch(currentFlashMode)
        {
            case CaptureRequest.FLASH_MODE_OFF:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                break;
            case CaptureRequest.FLASH_MODE_SINGLE:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                break;
            case CaptureRequest.FLASH_MODE_TORCH:
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                break;

            default:
                break;

        }*/


        mBuilder = builder;

        // Here, we create a CameraCaptureSession for camera preview.
        getCameraDevice().createCaptureSession(getCaptureTargets(),
                CameraSessionCallback, null);
    }

    public void onConfiguredCaptureSession(CameraCaptureSession captureSession ){

        // The camera is already closed
        if (null == getCameraDevice()) {
            return;
        }
        // When the session is ready, we start displaying the preview.
        setActiveCaptureSession(captureSession);
        try {
            // Finally, we start displaying the camera preview.
            CaptureRequest previewRequest = mBuilder.build();
            getActiveCaptureSession().setRepeatingRequest(previewRequest,
                    null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void onConfiguredStillImage( CameraCaptureSession captureSession, CameraCaptureSession.CaptureCallback captureCallback )
    {
        // The camera is already closed
        if (null == getCameraDevice() ) {
            return;
        }

        // When the session is ready, we start displaying the preview.
        setActiveCaptureSession(captureSession);
        try {
            // Finally, we start displaying the camera preview.
            //CaptureRequest previewRequest = mBuilder.build();
            CaptureRequest stillCaptureRequest = mBuilder.build();
            getActiveCaptureSession().capture(stillCaptureRequest,
                    captureCallback , null);
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
    }
    //Callback to react to creation of the preview session
    private class PreviewSessionCallback
            extends CameraCaptureSession.StateCallback {
        private final CaptureRequest.Builder mBuilder;
        public PreviewSessionCallback(CaptureRequest.Builder builder) {
            mBuilder = builder;
        }

        @Override
        public void onConfigured(CameraCaptureSession captureSession) {
            Log.w(TAG, "onConfigured");

            // The camera is already closed
            if (null == getCameraDevice()) {
                return;
            }
            // When the session is ready, we start displaying the preview.
            setActiveCaptureSession(captureSession);
            try {
                // Finally, we start displaying the camera preview.
                CaptureRequest previewRequest = mBuilder.build();
                getActiveCaptureSession().setRepeatingRequest(previewRequest,
                        null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession captureSession) {
            Log.w(TAG, "Failed to Create Camera Preview");
        }

        @Override
        public void onReady(CameraCaptureSession captureSession) {
            Log.w(TAG, "CameraCaptureSession OnReady");
        }
    }
}

