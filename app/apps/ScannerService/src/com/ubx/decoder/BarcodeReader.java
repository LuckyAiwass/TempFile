package com.ubx.decoder;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.ubx.decoder.license.ActivationResponseListener;
import com.ubx.decoder.license.ActivationResult;

import java.lang.ref.WeakReference;

/**
 * Created by rocky on 19-08-08.
 *
 * The BarCodeReader class is used to set bar code reader settings, start/stop preview,
 * snap pictures, and capture frames for encoding for video.
 * This class is a client for the Camera service, which manages the actual Camera hardware.
 *
 * <p>To decode bar codes with this class, use the following steps:</p>
 *
 * <ol>
 * <li>Obtain an instance of BarCodeReader with {@link #open(int, Context)}.
 *
 * <li>Get the current settings with {@link #getParameters()}.
 *
 * <li>If necessary, modify the returned {@link BarcodeReader.Parameters} object and call
 * {@link #setParameters(BarcodeReader.Parameters)}.
 *
 * <li>Call {@link #setDecodeCallback(BarcodeReader.DecodeCallback)} to register a
 * bar code decode event handler.
 *
 * <li>If a view finder is desired, pass a fully initialized to
 * {@link #startPreview()} .
 *
 * <li>To begin a decode session, call {@link #startDecode()} or {@link #startHandsFreeDecode(int)}.
 * Your registered DecodeCallback will be called when a successful decode occurs or if
 * the configured timeout expires.
 *
 * <li>Call {@link #stopDecode()} to end the decode session.
 *
 * <li><b>Important:</b> Call {@link #release()} to release the BarCodeReader for
 * use by other applications.  Applications should release the BarCodeReader
 * immediately in {@link Activity#onPause()} (and re-{@link #open(int, Context)}
 * it in {@link Activity#onResume()}).
 * </ol>
 *
 * <p>This class is not thread-safe, and is meant for use from one event thread.
 * Callbacks will be invoked on the event thread {@link #open(int, Context)} was called from.
 * This class's methods must never be called from multiple threads at once.</p>
 */
public class BarcodeReader {
    private static final String TAG = "DECODERBReader";
    static {
        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) {
            System.loadLibrary("UBXDecoderAPI_5.1");
        } else if(Build.VERSION.SDK_INT == 23/*Build.VERSION_CODES.M*/) {
            System.loadLibrary("UBXDecoderAPI_6.x");
        } else if(Build.VERSION.SDK_INT <= 25/*Build.VERSION_CODES.N_MR1*/) {
            System.loadLibrary("UBXDecoderAPI_7.1");
        } else if(Build.VERSION.SDK_INT <= 27/*Build.VERSION_CODES.O_MR1*/) {
            System.loadLibrary("UBXDecoderAPI_8.1");
        } else {
            System.loadLibrary("UBXDecoderAPI");
        }
    }
    private static final int BCRDR_MSG_ERROR			= 0x000001;
    private static final int BCRDR_MSG_SHUTTER			= 0x000002;
    private static final int BCRDR_MSG_FOCUS			= 0x000004;
    private static final int BCRDR_MSG_ZOOM				= 0x000008;
    private static final int BCRDR_MSG_PREVIEW_FRAME	= 0x000010;
    private static final int BCRDR_MSG_VIDEO_FRAME		= 0x000020;
    private static final int BCRDR_MSG_POSTVIEW_FRAME	= 0x000040;
    private static final int BCRDR_MSG_RAW_IMAGE		= 0x000080;
    private static final int BCRDR_MSG_COMPRESSED_IMAGE	= 0x000100;
    private static final int BCRDR_MSG_LAST_DEC_IMAGE	= 0x000200;
    private static final int BCRDR_MSG_DEC_COUNT		= 0x000400;
    // Add bar code reader specific values here
    private static final int BCRDR_MSG_DECODE_COMPLETE	= 0x010000;
    private static final int BCRDR_MSG_DECODE_TIMEOUT	= 0x020000;
    private static final int BCRDR_MSG_DECODE_CANCELED	= 0x040000;
    private static final int BCRDR_MSG_DECODE_ERROR		= 0x080000;
    private static final int BCRDR_MSG_DECODE_EVENT		= 0x100000;
    private static final int BCRDR_MSG_FRAME_ERROR		= 0x200000;
    private static final int BCRDR_MSG_LICENSE_EVENT	= 0x400000;
    private static final int BCRDR_MSG_ALL_MSGS			= 0x7F01FF;

    private static final int DECODE_MODE_NO_DISPLAY	= 0;
    private static final int DECODE_MODE_PREVIEW		= 1;
    private static final int DECODE_MODE_VIEWFINDER		= 2;
    private static final int DECODE_MODE_VIDEO			= 3;
    // Result codes for functions that return and integer status

    /**
     * Function completed successfully
     */
    public static final int BCR_SUCCESS						= 0;

    /**
     * Function failed
     */
    public static final int BCR_ERROR						= -1;


    // onDecodeComplete status codes passed as the length value

    /**
     * onDecodeComplete length value indicating that the decode timed out
     */
    public static final int DECODE_STATUS_TIMEOUT			= -4;

    /**
     * onDecodeComplete length value indicating that the decode was canceled
     */
    public static final int DECODE_STATUS_CANCELED			= -1;

    /**
     * onDecodeComplete length value indicating that an error occurred
     */
    public static final int DECODE_STATUS_ERROR				= -2;

    /**
     * onDecodeComplete length value indicating a multi-decode event
     */
    public static final int DECODE_STATUS_MULTI_DEC_COUNT	= -3;


    // Miscellaneous event ID's

    /**
     * Scan mode changed event ID
     */
    public static final int BCRDR_EVENT_SCAN_MODE_CHANGED	= 5;

    /**
     * Motion detected event ID
     */
    public static final int BCRDR_EVENT_MOTION_DETECTED		= 6;

    /**
     * Scanner reset event ID
     */
    public static final int BCRDR_EVENT_SCANNER_RESET		= 7;

    /**
     * Unspecified reader error.
     * @see BarcodeReader.ErrorCallback
     */
    public static final int BCRDR_ERROR_UNKNOWN = 1;

    /**
     * Media server died. In this case, the application must release the
     * BarCodeReader object and instantiate a new one.
     * @see BarcodeReader.ErrorCallback
     */
    public static final int BCRDR_ERROR_SERVER_DIED = 100;

    private long					mNativeContext;		// accessed by native methods
    private HSMDecodeResult result;
    private EventHandler			mEventHandler;
    private DecodeCallback			mDecodeCallback;
    private ErrorCallback			mErrorCallback;
    private DecoderListener         mDecoderListener;
    private static final int NO_ERROR = 0;
    private static final int EACCESS = -13;
    private static final int ENODEV = -19;
    private static final int EBUSY = -16;
    private static final int EINVAL = -22;
    private static final int ENOSYS = -38;
    private static final int EUSERS = -87;
    private static final int EOPNOTSUPP = -95;
    public static native int activate(String storagePath, int mode);
    public static native int activateLicense(String storagePath);
    public static native int generateLicenseRequest(String storagePath, String filename);
    public static native int consumeLicenseResponse(String storagePath, String filename);
    public static native int localServerLicense(String writePath, String localServer);
    public static native int remoteLicenseWithProxy(String writePath, String myProxyServer, String myProxyUserPwd);
    public static void activateAsyncAPIWithLocalServer(final String storagePath, final ActivationResponseListener listener) {
        try {
            new Thread(new Runnable() {
                public void run() {
                    int actRes = activateLicense(storagePath);

                    if (listener != null) {
                        listener.onActivationComplete(ActivationResult.fromInt(actRes));
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onActivationComplete(ActivationResult.FAILED_UNKNOWN);
            }
        }
    }
    public static native void getAPIRevision(byte[] version);
    public native int decodeImage(byte[] data, int width, int heigth);
    public native int reportDecoderVersion(byte[] version);
    /**
     * Sets the decode attempt limit (or the amount of time it will spend
     * decoding a particular image).
     *
     * @param limit - in milliseconds
     */
    public native void setDecodeAttemptLimit(int limit);
    public native void setMultiReadCount(int count);
    public native HSMDecodeResult getDecodeData();
    private native final String native_getParameters();
    private native final int native_release();
    private native final int setNumParameter(int paramNum, int paramVal);
    private native final int setStrParameter(int paramNum, String paramVal);
    private native final void native_setParameters(String params);
    private native final int native_setup(Object reader_this, int readerId, Object currentContext);
    private native final void native_startPreview(int mode);
    /**
     * Sets the OCR template configuration (pre-defined).
     *
     * @param template - OCRTemplates setting
     */
    public native void setOCRTemplates(int template);

    /**
     * Gets the OCR active template configuration.
     *
     */
    public native int getOCRTemplates();

    /**
     * Sets the OCR User defined template.
     *
     * @param template - byte array of the template
     */
    public native void setOCRUserTemplate(byte[] template);

    /**
     * Gets the OCR User defined template.
     *
     * @return template - byte array of the template
     */
    public native byte[] getOCRUserTemplate();

    /**
     * Sets the OCR Mode (used for enabling/disabling)
     *
     * @param mode - OCRMode to set
     */
    public native void setOCRMode(int mode);

    /**
     * Gets the OCR Mode
     *
     */
    public native int getOCRMode();
    /**
     * Get the last decoded image. When the image is available, BCRDR_MSG_LAST_DEC_IMAGE
     * is triggered
     */
    public native final byte[] getLastDecImage();
    /**
     * Returns the value of a specified bar code reader numeric property or BCR_ERROR
     * if the specified property number is invalid.
     */
    //public native final int getNumProperty(int propNum);

    /**
     * Returns the value of a specified bar code reader string property or null
     * if the specified property number is invalid.
     */
    //public native final String getStrProperty(int propNum);

    /**
     * Returns the value of a specified bar code reader numeric parameter or BCR_ERROR
     * if the specified parameter number is invalid.
     */
    public native final int getNumParameter(int paramNum);

    /**
     * Returns the value of a specified bar code reader string parameter or BCR_ERROR
     * if the specified parameter number is invalid.
     */
    //public native final String getStrParameter(int paramNum);

    /**
     * Sets the value of a specified bar code reader numeric parameter.
     * Returns BCR_SUCCESS if successful or BCR_ERROR if the specified parameter number
     * or value is invalid.
     */
    public final int setParameter(int paramNum, int paramVal)
    {
        return(setNumParameter(paramNum, paramVal));
    }

    /**
     * Sets the value of a specified bar code reader string parameter.
     *
     * @param paramNum	The parameter number to set
     * @param paramVal	The new value for the parameter
     *
     * @return BCR_SUCCESS if successful or BCR_ERROR if the specified parameter number
     * or value is invalid.
     */
    public final int setParameter(int paramNum, String paramVal)
    {
        return(setStrParameter(paramNum, paramVal));
    }

    /**
     * Sets all bar code reader parameters to their default values.
     */
    //public native final void setDefaultParameters();
    /////////////////////////////////////////////////////////////////
    // Public native functions
    /////////////////////////////////////////////////////////////////

    /**
     * Returns the number of physical readers available on this device.
     */
    public native static int getNumberOfReaders();


    /**
     * Creates a new BarCodeReader object to access a particular hardware reader.
     *
     * <p>You must call {@link #release()} when you are done using the reader,
     * otherwise it will remain locked and be unavailable to other applications.
     *
     * <p>Your application should only have one BarCodeReader object active at a time
     * for a particular hardware reader.
     *
     * <p>Callbacks from other methods are delivered to the event loop of the
     * thread which called open().  If this thread has no event loop, then
     * callbacks are delivered to the main application event loop.  If there
     * is no main application event loop, callbacks are not delivered.
     *
     * <p class="caution"><b>Caution:</b> On some devices, this method may
     * take a long time to complete.  It is best to call this method from a
     * worker thread (possibly using {@link android.os.AsyncTask}) to avoid
     * blocking the main application UI thread.
     *
     * @param readerId the hardware reader to access, between 0 and {@link #getNumberOfReaders()}-1.
     *
     * @return a new BarCodeReader object, connected, locked and ready for use.
     *
     * @throws RuntimeException if connection to the reader service fails (for
     *	 example, if the reader is in use by another process).
     */
    public static BarcodeReader open(int readerId, Context context)
    {
        return(new BarcodeReader(readerId, context));
    }

    BarcodeReader(int readerId, Context context)
    {
        Looper	aLooper;

        mEventHandler		= null;
        mDecodeCallback		= null;
        mErrorCallback		= null;
        mDecoderListener = null;
        aLooper = Looper.myLooper();
        if ( null == aLooper )
            aLooper = Looper.getMainLooper();
        if ( aLooper != null )
        {
            mEventHandler = new EventHandler(this, aLooper);
            result = new HSMDecodeResult();
        }

        int err = native_setup(new WeakReference<BarcodeReader>(this), readerId, context);
        if (err != NO_ERROR) {
            mEventHandler = null;
            result = null;
            throw new RuntimeException("Fail to connect to Scanner service");
            /*if (err == -EACCESS) {
                throw new RuntimeException("Fail to connect to camera service");
            } else if (err == -ENODEV) {
                throw new RuntimeException("Camera initialization failed");
            } else if (err == -ENOSYS) {
                throw new RuntimeException("Camera initialization failed because some methods"
                        + " are not implemented");
            } else if (err == -EOPNOTSUPP) {
                throw new RuntimeException("Camera initialization failed because the hal"
                        + " version is not supported by this device");
            } else if (err == -EINVAL) {
                throw new RuntimeException("Camera initialization failed because the input"
                        + " arugments are invalid");
            } else if (err == -EBUSY) {
                throw new RuntimeException("Camera initialization failed because the camera"
                        + " device was already opened");
            } else if (err == -EUSERS) {
                throw new RuntimeException("Camera initialization failed because the max"
                        + " number of camera devices were already opened");
            }
            // Should never hit this.
            throw new RuntimeException("Unknown camera error");*/
        }
    }
    /*1. finalize的作用
    finalize()是Object的protected方法，子类可以覆盖该方法以实现资源清理工作，GC在回收对象之前调用该方法。
    finalize()与C++中的析构函数不是对应的。C++中的析构函数调用的时机是确定的（对象离开作用域或delete掉），但Java中的finalize的调用具有不确定性
    不建议用finalize方法完成“非内存资源”的清理工作，但建议用于：① 清理本地对象(通过JNI创建的对象)；② 作为确保某些非内存资源(如Socket、文件等)释放的一个补充：在finalize方法中显式调用其他资源释放方法。其原因可见下文[finalize的问题]
    2. finalize的问题
    一些与finalize相关的方法，由于一些致命的缺陷，已经被废弃了，如System.runFinalizersOnExit()方法、Runtime.runFinalizersOnExit()方法
    System.gc()与System.runFinalization()方法增加了finalize方法执行的机会，但不可盲目依赖它们
    Java语言规范并不保证finalize方法会被及时地执行、而且根本不会保证它们会被执行
    finalize方法可能会带来性能问题。因为JVM通常在单独的低优先级线程中完成finalize的执行
    对象再生问题：finalize方法中，可将待回收对象赋值给GC Roots可达的对象引用，从而达到对象再生的目的
    finalize方法至多由GC执行一次(用户当然可以手动调用对象的finalize方法，但并不影响GC对finalize的行为)
    3. finalize的执行过程(生命周期)


    (1) 首先，大致描述一下finalize流程：当对象变成(GC Roots)不可达时，GC会判断该对象是否覆盖了finalize方法，若未覆盖，则直接将其回收。否则，若对象未执行过finalize方法，将其放入F-Queue队列，由一低优先级线程执行该队列中对象的finalize方法。执行finalize方法完毕后，GC会再次判断该对象是否可达，若不可达，则进行回收，否则，对象“复活”。
            (2) 具体的finalize流程：
    对象可由两种状态，涉及到两类状态空间，一是终结状态空间 F = {unfinalized, finalizable, finalized}；二是可达状态空间 R = {reachable, finalizer-reachable, unreachable}。各状态含义如下：
    unfinalized: 新建对象会先进入此状态，GC并未准备执行其finalize方法，因为该对象是可达的
    finalizable: 表示GC可对该对象执行finalize方法，GC已检测到该对象不可达。正如前面所述，GC通过F-Queue队列和一专用线程完成finalize的执行
    finalized: 表示GC已经对该对象执行过finalize方法
    reachable: 表示GC Roots引用可达
    finalizer-reachable(f-reachable)：表示不是reachable，但可通过某个finalizable对象可达
    unreachable：对象不可通过上面两种途径可达*/
    /*protected void finalize()
    {
        Log.v(TAG, "release finalize");
        try {
            release();
        } finally {
            super.finalize();
        }
    }*/

    /**
     * Disconnects and releases the BarCodeReader object resources.
     *
     * <p>You must call this as soon as you're done with the BarCodeReader object.</p>
     */
    public final void release()
    {
        if(mEventHandler != null) {
            mEventHandler.removeCallbacksAndMessages(null);
        }
        mEventHandler = null;
        native_release();
        mDecodeCallback = null;
        mDecoderListener = null;
        result = null;
    }

    /**
     * Starts capturing frames in preview mode.
     *
     */
    public final void startPreview()
    {
        native_startPreview(DECODE_MODE_NO_DISPLAY);
    }

    /**
     * Stops capturing and drawing preview frames to the surface, and
     * resets the reader for a future call to {@link #startPreview()}.
     */
    public native final void stopPreview();

    /**
     * Starts capturing frames and passes the captured frames to the decoder.
     * When a decode occurs or timeout
     * expires and {@link #setDecodeCallback(BarcodeReader.DecodeCallback)} was called,
     * {@link #BarcodeReader.DecodeCallback.onDecodeComplete(int, HSMDecodeResult, BarcodeReader)}
     * will be called with the decode results.
     */
    public native final void startDecode(int timeOut);

    /**
     * Starts capturing frames and passes the captured frames to the decoder.
     * If a decode occurs a decode event is generated. Decoding
     * continues until {@link #stopDecode()} is called.
     *
     * @param mode	Indicates the trigger mode to use.
     *
     * @return BCR_SUCCESS if hands-free mode is successfully started or BCR_ERROR
     * if an invalid mode is specified or if a decode session is already in progress.
     */
    public native final int startHandsFreeDecode(int mode);

    /**
     * Stops capturing and decoding frames.
     */
    public native final void stopDecode();

    private class EventHandler extends Handler
    {
        private final BarcodeReader mReader;

        public EventHandler(BarcodeReader rdr, Looper looper)
        {
            super(looper);
            mReader = rdr;
        }

        @Override
        public void handleMessage(Message msg)
        {
            //Log.v(TAG, String.format("Event message: %X, arg1=%d, arg2=%d", msg.what, msg.arg1, msg.arg2));
            switch ( msg.what )
            {
                case BCRDR_MSG_DECODE_COMPLETE:
                    if ( mDecodeCallback != null )
                    {
                        mDecodeCallback.onDecodeComplete(BCR_SUCCESS, (HSMDecodeResult) msg.obj, mReader);
                    }
                    return;
                case BCRDR_MSG_DECODE_TIMEOUT:
                    if ( mDecodeCallback != null )
                    {

                        mDecodeCallback.onDecodeComplete(DECODE_STATUS_TIMEOUT, null, mReader);
                    }
                    return;
                case BCRDR_MSG_DECODE_ERROR:
                    if ( mDecodeCallback != null )
                    {
                        mDecodeCallback.onEvent(DECODE_STATUS_MULTI_DEC_COUNT, 0, null, mReader);
                    }
                    return;
                case BCRDR_MSG_DEC_COUNT:
                    if ( mDecodeCallback != null )
                    {
                        mDecodeCallback.onDecodeComplete(DECODE_STATUS_MULTI_DEC_COUNT, null, mReader);
                    }
                    return;
                default:
                    Log.e(TAG, "Unknown message type " + msg.what);
                    return;
            }
        }
    }
    /**
     * Native API Callbacks
     *
     */
    private void postEventFromNative(Object reader_ref, int what, int length, Object obj, long decodeTime, Object bounds, Object aimModifier)
    {
        @SuppressWarnings("unchecked")
        BarcodeReader c = (BarcodeReader) ((WeakReference<BarcodeReader>) reader_ref).get();
        if ( (c != null) && (c.mEventHandler != null) )
        {
            if(BCRDR_MSG_DECODE_COMPLETE == what) {
                byte[] symIds = (byte[]) aimModifier;
                c.result.setCodeId(symIds[0]);
                c.result.setAIMCodeLetter(symIds[1]);
                c.result.setAIMModifier(symIds[2]);
                c.result.setBarcodeDataBytes((byte[]) obj);
                c.result.setBarcodeDataLength(length);
                c.result.setDecodeTime(decodeTime);
                c.result.setBarcodeBounds((int[])bounds);
                Message m = c.mEventHandler.obtainMessage(what, 0, length, c.result);
                c.mEventHandler.sendMessage(m);
            } else {
                Message m = c.mEventHandler.obtainMessage(what, 0, 0, null);
                c.mEventHandler.sendMessage(m);
            }
        }
    }

    /**
     * Callback used to stop decoding. It is used in conjunction with
     *  {@link #setMultiReadCount(int)}.
     *
     * @return true if the Decoder should continue looking for symbols,
     * otherwise false to stop the decode process.
     */
    public boolean callbackKeepGoing()
    {
        //Log.d(TAG, "callbackKeepGoing");

        if(mDecoderListener != null)
        {
            return(mDecoderListener.onKeepGoingCallback());
        }

        return false;

    }

    /**
     * Callback used for MultiRead (Shotgun). Upon successful decode when using
     * {@link #setMultiReadCount(int)}, the API calls this function when data is available
     * and can be retrieved.
     *
     * @return true if the Decoder is to continue to look for additional
     * symbols, otherwise return false to stop decode attempts.
     */
    public boolean callbackMultiRead()
    {
        //Log.d(TAG, "callbackMultiRead");

        if(mDecoderListener != null)
        {
            return(mDecoderListener.onMultiReadCallback());
        }

        return false;
    }

    /**
     * Used to register the listeners
     *
     * @param observer
     */
    public void setDecoderListeners(DecoderListener observer)
    {
        this.mDecoderListener = observer;
    }
    /**
     * Callback interface for reader error notification.
     *
     * @see #setErrorCallback(ErrorCallback)
     */
    public interface ErrorCallback
    {
        /**
         * Callback for reader errors.
         * @param error	error code:
         * <ul>
         * <li>{@link #BCRDR_ERROR_UNKNOWN}
         * <li>{@link #BCRDR_ERROR_SERVER_DIED}
         * </ul>
         * @param reader	the BarcodeReader service object
         */
        void onError(int error, BarcodeReader reader);
    }
    /**
     * Installs callbacks to be invoked when a decode request completes
     * or a decoder event occurs. This method can be called at any time,
     * even while a decode request is active.  Any other decode callbacks
     * are overridden.
     *
     * @param cb a callback object that receives a notification of a completed,
     *	decode request or null to stop receiving decode callbacks.
     */
    public final void setDecodeCallback(DecodeCallback cb)
    {
        mDecodeCallback = cb;
    }
    /**
     * Callback interface used to deliver decode results.
     *
     * @see #setDecodeCallback(BarcodeReader.DecodeCallback)
     * @see #startDecode(int)
     */
    public interface DecodeCallback
    {
        /**
         * Called when a decode operation has completed, either due to a timeout,
         * a successful decode or canceled by the user.  This callback is invoked
         * on the event thread {@link #open(int, Context)} was called from.
         * @param event the type of event that has occurred
         * @param result the contents of the decoded bar code
         * @param reader the BarCodeReader service object.
         */
        void onDecodeComplete(int event, HSMDecodeResult result, BarcodeReader reader);

        /**
         * Called to indicate that the decoder detected an event such as MOTION DECTECTED.
         * This callback is invoked on the event thread {@link #open(int, Context)} was called from.
         *
         * @param event the type of event that has occurred
         * @param info additional event information, if any, else zero
         * @param data data associated with the event, if any, else null
         * @param reader the BarCodeReader service object.
         */
        void onEvent(int event, int info, byte[] data, BarcodeReader reader);
    }
    /**
     * Listener interface for handling a multiple decode result from the Decoder API
     *
     */
    public interface DecoderListener {
        /**
         * Handler for listener when a keep going callback occurs
         *
         * @return true to continue looking for decoded results, otherwise false.
         */
        public boolean onKeepGoingCallback();
        /**
         * Handler for listener when a multiple decode result is available.
         *
         * @return true to continue looking for decoded results, otherwise false.
         */
        public boolean onMultiReadCallback();
    }
}
