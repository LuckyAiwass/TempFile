package com.android.server.scanner.camera2;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraCharacteristics.Key;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.util.Log;
import android.util.Range;
import android.util.Size;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by xjf on 19-07-04.
 * Helper to manage available cameras and their parameters
 */
public class CameraHelper {
    private static final String TAG = CameraHelper.class.getSimpleName();

    private CameraManager mCameraManager;
    // TODO: Consider making this work across any metadata object, not just camera characteristics
    private CameraCharacteristics mCharacteristics;
    public CameraHelper(Context context) {
        mCameraManager = (CameraManager) context
                .getSystemService(Context.CAMERA_SERVICE);
    }

    /**
     * Open the selected camera device on the main thread
     */
    public void openCamera(String cameraId,
                           CameraDevice.StateCallback stateCallback)
            throws CameraAccessException {
        mCameraManager.openCamera(cameraId, stateCallback, null);
        //mCharacteristics =
        //        mCameraManager.getCameraCharacteristics(cameraId);
    }

    /**
     * Select the first-detected camera device matching the
     * requested lens orientation.
     */
    public String getPreferredCameraId(int cameraType) {
        if (cameraType != CameraCharacteristics.LENS_FACING_FRONT
                && cameraType != CameraCharacteristics.LENS_FACING_BACK) {
            throw new IllegalArgumentException("Invalid camera type");
        }

        try {
            for (String cameraId : mCameraManager.getCameraIdList()) {
                CameraCharacteristics characteristics =
                        mCameraManager.getCameraCharacteristics(cameraId);

                if (characteristics.get(CameraCharacteristics.LENS_FACING)
                        == cameraType) {
                    Log.d(TAG, "Found camera: " + cameraId);
                    return cameraId;
                }
            }

            //No matching camera found
            return null;
        } catch (CameraAccessException e) {
            Log.w(TAG, "Unable to access camera devices.", e);
            return null;
        } catch (NullPointerException e) {
            //This will happen if the device does not support Camera2
            Log.w(TAG, "No Support for Camera2 APIs.", e);
            return null;
        }
    }

    /**
     * Camera Parameters Wrapper Methods
     */
    public StreamConfigurationMap getConfiguration(String cameraId)
            throws CameraAccessException {
        CameraCharacteristics characteristics =
                mCameraManager.getCameraCharacteristics(cameraId);

        return characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
    }


    public Size getTargetPreviewSize(String cameraId,
                                     int width, int height)
            throws CameraAccessException {
        StreamConfigurationMap map = getConfiguration(cameraId);

        //Pick the minimum size preview to match the view size
        return chooseOptimalSize(
                map.getOutputSizes(SurfaceTexture.class),
                width, height);
    }

    public Size chooseVideoSize(Size[] choices)
            throws CameraAccessException {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        Log.e(TAG, "Couldn't find any suitable video size");
        return choices[choices.length - 1];
    }

    public int getSensorOrientation(String cameraId)
            throws CameraAccessException {
        CameraCharacteristics characteristics =
                mCameraManager.getCameraCharacteristics(cameraId);

        //Get the orientation of the camera sensor
        return characteristics.get(
                CameraCharacteristics.SENSOR_ORIENTATION);
    }

    public int[] getSupportedEffects(String cameraId)
            throws CameraAccessException {
        CameraCharacteristics characteristics =
                mCameraManager.getCameraCharacteristics(cameraId);

        return characteristics.get(
                CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
    }

    public boolean isFlashSupported(String cameraId)
            throws CameraAccessException {

        CameraCharacteristics characteristics =
                mCameraManager.getCameraCharacteristics(cameraId);

        boolean flashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);

        return flashAvailable;
    }

    /*  CONTROL_AF_MODE_AUTO = 1;
        CONTROL_AF_MODE_CONTINUOUS_PICTURE = 4;
        CONTROL_AF_MODE_CONTINUOUS_VIDEO = 3;
        CONTROL_AF_MODE_EDOF = 5;
        CONTROL_AF_MODE_MACRO = 2;
        CONTROL_AF_MODE_OFF = 0;*/
    public String[] getSupportedFocusModes(String cameraId)
            throws CameraAccessException {

        CameraCharacteristics characteristics =
                mCameraManager.getCameraCharacteristics(cameraId);

        int[] modes = characteristics.get(
                CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);

        String[] focusModes = new String[modes.length];
        int j = 0;

        for (int i : modes) {
            switch (i) {
                case CameraMetadata.CONTROL_AF_MODE_OFF:
                    focusModes[j++] = "off";
                    break;

                case CameraMetadata.CONTROL_AF_MODE_AUTO:
                    focusModes[j++] = "auto";
                    break;

                case CameraMetadata.CONTROL_AF_MODE_MACRO:
                    focusModes[j++] = "macro";
                    break;

                case CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_VIDEO:
                    focusModes[j++] = "continuous-video";
                    break;

                case CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE:
                    focusModes[j++] = "continuous-picture";
                    break;

                case CameraMetadata.CONTROL_AF_MODE_EDOF:
                    focusModes[j++] = "edof";
                    break;

                default:
                    focusModes[j++] = "unknown mode";
                    break;
            }
        }
        return focusModes;
    }

    /*  FLASH_MODE_OFF;
        FLASH_MODE_SINGLE;
        FLASH_MODE_TORCH;
        */
    public String[] getSupportedFlashModes(String cameraId)
            throws CameraAccessException {

        CameraCharacteristics characteristics =
                mCameraManager.getCameraCharacteristics(cameraId);

        int[] modes = new int[3];
        modes[0] = CameraMetadata.FLASH_MODE_OFF;
        modes[1] = CameraMetadata.FLASH_MODE_SINGLE;
        modes[2] = CameraMetadata.FLASH_MODE_TORCH;

        String[] flashModes = new String[modes.length];

        for (int i : modes) {
            switch (modes[i]) {
                case CameraMetadata.FLASH_MODE_OFF:
                    flashModes[i] = "off";
                    break;

                case CameraMetadata.FLASH_MODE_SINGLE:
                    flashModes[i] = "decode only";
                    break;

                case CameraMetadata.FLASH_MODE_TORCH:
                    flashModes[i] = "torch";
                    break;

                default:
                    break;
            }
        }
        return flashModes;
    }

    public String[] getSupportedPreviewSizes(String cameraId)
            throws CameraAccessException {

        StreamConfigurationMap map = getConfiguration(cameraId);

        Size[] previewSizes = map.getOutputSizes(ImageFormat.YUV_420_888);

        String[] previewSizesModes = new String[previewSizes.length];

        for (int i = 0; i < previewSizes.length; i++) {
            previewSizesModes[i] = String.format("" + previewSizes[i].getWidth() + "x" + previewSizes[i].getHeight());
        }
        return previewSizesModes;
    }


    /**
     * Comparator to organize supported resolutions by overall size.
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * Choose the smallest size the will satisfy the minimum
     * requested dimensions.
     */
    public static Size chooseOptimalSize(Size[] choices,
                                         int width,
                                         int height) {

        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * Validate if a size is less than 1080p. Some devices
     * can't handle recording above that resolution.
     */
    public static boolean verifyVideoSize(Size option) {
        return (option.getWidth() <= 1080);
    }
    /**
     * Used to determine the stream direction for various helpers that look up
     * format or size information.
     */
    public enum StreamDirection {
        /** Stream is used with {@link android.hardware.camera2.CameraDevice#configureOutputs} */
        Output,
        /** Stream is used with {@code CameraDevice#configureInputs} -- NOT YET PUBLIC */
        Input
    }

    /**
     * Get available sizes for given format and direction.
     *
     * @param format The format for the requested size array.
     * @param direction The stream direction, input or output.
     * @return The sizes of the given format, empty array if no available size is found.
     */
    public Size[] getAvailableSizesForFormatChecked(int format, StreamDirection direction) {
        return getAvailableSizesForFormatChecked(format, direction,
                /*fastSizes*/true, /*slowSizes*/true);
    }

    /**
     * Get available sizes for given format and direction, and whether to limit to slow or fast
     * resolutions.
     *
     * @param format The format for the requested size array.
     * @param direction The stream direction, input or output.
     * @param fastSizes whether to include getOutputSizes() sizes (generally faster)
     * @param slowSizes whether to include getHighResolutionOutputSizes() sizes (generally slower)
     * @return The sizes of the given format, empty array if no available size is found.
     */

    public Size[] getAvailableSizesForFormatChecked(int format, StreamDirection direction,
                                                    boolean fastSizes, boolean slowSizes) {
        Key<StreamConfigurationMap> key =
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
        StreamConfigurationMap config = getValueFromKeyNonNull(key);

        if (config == null) {
            return new Size[0];
        }

        Size[] sizes = null;

        switch (direction) {
            case Output:
                Size[] fastSizeList = null;
                Size[] slowSizeList = null;
                if (fastSizes) {
                    fastSizeList = config.getOutputSizes(format);
                }
                if (slowSizes) {
                    slowSizeList = config.getHighResolutionOutputSizes(format);
                }
                if (fastSizeList != null && slowSizeList != null) {
                    sizes = new Size[slowSizeList.length + fastSizeList.length];
                    System.arraycopy(fastSizeList, 0, sizes, 0, fastSizeList.length);
                    System.arraycopy(slowSizeList, 0, sizes, fastSizeList.length, slowSizeList.length);
                } else if (fastSizeList != null) {
                    sizes = fastSizeList;
                } else if (slowSizeList != null) {
                    sizes = slowSizeList;
                }
                break;
            case Input:
                sizes = config.getInputSizes(format);
                break;
            default:
                throw new IllegalArgumentException("direction must be output or input");
        }

        if (sizes == null) {
            sizes = new Size[0];
        }

        return sizes;
    }
    /**
     *  min 15 fps must be no larger than max 15 fps
     * 10-08 20:12:52.600 27265 27265 D CameraHelper:  the frame duration 66666666 for min fps 15 must smaller than maxFrameDuration 66229800
     * 10-08 20:12:52.603 27265 27265 W CameraHelper: The static info key 'android.control.aeAvailableTargetFpsRanges'  the frame duration 66666666 for min fps 15 must smaller than maxFrameDuration 66229800
     * 10-08 20:12:52.604 27265 27265 D CameraHelper:  min 20 fps must be no larger than max 20 fps
     * 10-08 20:12:52.605 27265 27265 D CameraHelper:  the frame duration 50000000 for min fps 20 must smaller than maxFrameDuration 66229800
     * 10-08 20:12:52.608 27265 27265 D CameraHelper:  min 24 fps must be no larger than max 24 fps
     * 10-08 20:12:52.609 27265 27265 D CameraHelper:  the frame duration 41666666 for min fps 24 must smaller than maxFrameDuration 66229800
     * 10-08 20:12:52.612 27265 27265 D CameraHelper:  min 7 fps must be no larger than max 30 fps
     * 10-08 20:12:52.614 27265 27265 D CameraHelper:  the frame duration 142857142 for min fps 7 must smaller than maxFrameDuration 66229800
     * 10-08 20:12:52.616 27265 27265 W CameraHelper: The static info key 'android.control.aeAvailableTargetFpsRanges'  the frame duration 142857142 for min fps 7 must smaller than maxFrameDuration 66229800
     * 10-08 20:12:52.617 27265 27265 D CameraHelper:  min 30 fps must be no larger than max 30 fps
     * 10-08 20:12:52.619 27265 27265 D CameraHelper:  the frame duration 33333333 for min fps 30 must smaller than maxFrameDuration 66229800
     * Get available AE target fps ranges.
     *
     * @return Empty int array if aeAvailableTargetFpsRanges is invalid.
     */
    @SuppressWarnings("raw")
    public Range<Integer>[] getAeAvailableTargetFpsRangesChecked() {
        Key<Range<Integer>[]> key =
                CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES;
        Range<Integer>[] fpsRanges = getValueFromKeyNonNull(key);

        if (fpsRanges == null) {
            return new Range[0];
        }

        // Round down to 2 boundary if it is not integer times of 2, to avoid array out of bound
        // in case the above check fails.
        int fpsRangeLength = fpsRanges.length;
        int minFps, maxFps;
        long maxFrameDuration = getMaxFrameDurationChecked();
        for (int i = 0; i < fpsRangeLength; i += 1) {
            minFps = fpsRanges[i].getLower();
            maxFps = fpsRanges[i].getUpper();
            Log.d(TAG, String.format(
                    " min %d fps must be no larger than max %d fps",minFps, maxFps));
            checkTrueForKey(key, " min fps must be no larger than max fps!",
                    minFps > 0 && maxFps >= minFps);
            long maxDuration = (long) (1e9 / minFps);
            Log.d(TAG, String.format(
                    " the frame duration %d for min fps %d must smaller than maxFrameDuration %d",
                    maxDuration, minFps, maxFrameDuration));
            checkTrueForKey(key, String.format(
                    " the frame duration %d for min fps %d must smaller than maxFrameDuration %d",
                    maxDuration, minFps, maxFrameDuration), maxDuration <= maxFrameDuration);
        }
        return fpsRanges;
    }

    /**
     * Get the highest supported target FPS range.
     * Prioritizes maximizing the min FPS, then the max FPS without lowering min FPS.
     */
    public Range<Integer> getAeMaxTargetFpsRange() {
        Range<Integer>[] fpsRanges = getAeAvailableTargetFpsRangesChecked();

        Range<Integer> targetRange = fpsRanges[0];
        // Assume unsorted list of target FPS ranges, so use two passes, first maximize min FPS
        for (Range<Integer> candidateRange : fpsRanges) {
            if (candidateRange.getLower() > targetRange.getLower()) {
                targetRange = candidateRange;
            }
        }
        // Then maximize max FPS while not lowering min FPS
        for (Range<Integer> candidateRange : fpsRanges) {
            if (candidateRange.getLower() >= targetRange.getLower() &&
                    candidateRange.getUpper() > targetRange.getUpper()) {
                targetRange = candidateRange;
            }
        }
        return targetRange;
    }

    /**
     * Get max frame duration.
     *
     * @return 0 if maxFrameDuration is null
     */
    public long getMaxFrameDurationChecked() {
        Key<Long> key =
                CameraCharacteristics.SENSOR_INFO_MAX_FRAME_DURATION;
        Long maxDuration = getValueFromKeyNonNull(key);

        if (maxDuration == null) {
            return 0;
        }

        return maxDuration;
    }

    /**
     * Get available minimal frame durations for a given format.
     *
     * @param format One of the format from {@link ImageFormat}.
     * @return HashMap of minimal frame durations for different sizes, empty HashMap
     *         if availableMinFrameDurations is null.
     */
    public HashMap<Size, Long> getAvailableMinFrameDurationsForFormatChecked(int format) {

        HashMap<Size, Long> minDurationMap = new HashMap<Size, Long>();

        Key<StreamConfigurationMap> key =
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;
        StreamConfigurationMap config = getValueFromKeyNonNull(key);

        if (config == null) {
            return minDurationMap;
        }

        for (Size size : getAvailableSizesForFormatChecked(format,
                StreamDirection.Output)) {
            long minFrameDuration = config.getOutputMinFrameDuration(format, size);

            if (minFrameDuration != 0) {
                minDurationMap.put(new Size(size.getWidth(), size.getHeight()), minFrameDuration);
            }
        }

        return minDurationMap;
    }
    /**
     * Gets the key, logging warnings for null values.
     */
    public <T> T getValueFromKeyNonNull(CameraCharacteristics.Key<T> key) {
        if (key == null) {
            throw new IllegalArgumentException("key was null");
        }

        T value = mCharacteristics.get(key);

        /*if (value == null) {
            failKeyCheck(key, "was null");
        }*/

        return value;
    }
    private <T> void checkTrueForKey(Key<T> key, String message, boolean condition) {
        if (!condition) {
            failKeyCheck(key, message);
        }
    }
    private <T> void failKeyCheck(Key<T> key, String message) {
        // TODO: Consider only warning once per key/message combination if it's too spammy.
        // TODO: Consider offering other options such as throwing an assertion exception
        String failureCause = String.format("The static info key '%s' %s", key.getName(), message);
        Log.w(TAG, failureCause);
        /*switch (mLevel) {
            case WARN:
                Log.w(TAG, failureCause);
                break;
            case COLLECT:
                mCollector.addMessage(failureCause);
                break;
            case ASSERT:
                Assert.fail(failureCause);
            default:
                throw new UnsupportedOperationException("Unhandled level " + mLevel);
        }*/
    }
}
