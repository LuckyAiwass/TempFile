package com.ubx.usdk.scanner;

import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import com.ubx.usdk.LogUtil;
import com.ubx.usdk.USDKBaseManager;
import com.ubx.usdk.USDKManager;
import com.ubx.usdk.USDKManager.STATUS;
import com.ubx.usdk.USDKManager.FEATURE_TYPE;
import android.os.IScanServiceWrapper;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Symbology;
import android.device.scanner.configuration.Triggering;
import android.os.scanner.IScanCallBack;

public class UScanManager extends USDKBaseManager implements USDKManager.StatusListener {

    private static final String TAG = "UScanManager";
    private static final boolean DEBUG = true;
    private IScanServiceWrapper mIUScanManager = null;
    public static final String ACTION_DECODE = "android.intent.ACTION_DECODE_DATA";
    public static final String BARCODE_STRING_TAG = "barcode_string";

    /**
     * String contains the label type of the bar code
     */
    public static final String BARCODE_TYPE_TAG = "barcodeType";

    /**
     * String contains the label length of the bar code
     */
    public static final String BARCODE_LENGTH_TAG = "length";

    /**
     * String contains the output data as a byte array.
     * In the case of concatenated bar codes, the decode data is
     * concatenated and sent out as a single array
     *
     */
    public static final String DECODE_DATA_TAG = "barcode";

    public static final String CACHE_LIMIT_ENABLE = "CACHE_LIMIT_ENABLE";
    public static final String CACHE_LIMIT_TIME = "CACHE_LIMIT_TIME";
    public static final String CACHE_ENABLE = "CACHE_ENABLE";

    public UScanManager(Context context) {
        super(context , USDKManager.FEATURE_TYPE.SCANNER);
        Intent intent = new Intent();
        intent.setPackage("com.android.usettings");
        intent.setClassName("com.android.usettings","com.android.server.ScanService");
        setIntent(intent);
        addStatusListener(this);
        LogUtil.d(TAG);
    }

    @Override
    public void onStatus(FEATURE_TYPE featureType, STATUS status) {
        if (status == STATUS.SUCCESS) {
            mIUScanManager = IScanServiceWrapper.Stub.asInterface(getIBinder());
            LogUtil.d("onStatus mIUScanManager:" + mIUScanManager);
        } else {
            mIUScanManager = null;
            LogUtil.d("onStatus mIUScanManager:" + mIUScanManager + ",status:" + status);
        }
    }

    @Override
    public void release() {
        super.release();
        LogUtil.d("release mIUScanManager:" + mIUScanManager);
        mIUScanManager = null;
    }

    /**
     * <p>Use this function to set the output mode of the barcode reader (either send output to text box or as Android intent).</p>
     * <p>TextBox Mode allows the captured data to be sent to the text box in focus.</p>
     * 
     * <p>Intent mode allows the captured data to be sent as an implicit Intent.
     * Application interested in the scan data should register an action as
     * <b>urovo.rcv.message</b> broadcast listerner.</p>
     * 
     * In the onReceive(Context context, Intent arg1) method, get the information as follow:
     *  <br>
     *    <code>byte[] barcode=arg1.getByteArrayExtra("barcode");</code>
     *  <br>
     *    <code>int barcodelen=arg1.getIntExtra("length",0);</code>
     *  <br>
     *     <code>byte type=arg1.getByteExtra("barcodeType",(byte)0);</code>
     *  <br>
     *  <p>The information are bar code data, length of bar code data, and bar code type (symbology).</p>
     *     @param mode Set to 0 if barcode output is to be sent as intent, barcode output is to be sent to the text box in focus
     *     @return Returns true if successful.  Returns false if failed.
     */
    public boolean switchOutputMode(int mode) {
        LogUtil.i("switch output mode:" + mode);
        return setPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE, mode);
    }

    private boolean setPropertyInt(int index, int value) {
        int[] id_buffer = new int[1];
        int[] value_buffer = new int[1];
        id_buffer[0] = index;
        int[] id_bad_buffer = new int[1];
        value_buffer[0] = value;
        boolean result = false;
        try {
            if(mIUScanManager != null) {
                int error = mIUScanManager.setPropertyInts(id_buffer, 1, value_buffer, 1, id_bad_buffer);
                if (error > 0) {
                    LogUtil.e(TAG + " error: in setPropertyInt , index:" + index + ",value:" + value);
                }
                result = error == 0;
            } else {
                LogUtil.e(TAG + "error: in setPropertyInt , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + "Decoder error: in setPropertyInt , index:" + index + ",value:" + value);
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * get current the scan result output mode
     * 
     * @return Returns zero if the barcode is sent as intent.  Returns 1 if barcode is sent to the text box in focus
     */
    public int getOutputMode() {
        int outputMode = 0;
        try {
            if (mIUScanManager != null) {
                outputMode = mIUScanManager.getPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE);
            } else {
                LogUtil.e(TAG + " error: getOutputMode , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getOutputMode Exception");
            ce.printStackTrace();
        }
        LogUtil.d(TAG + " getOutputMode value:" + outputMode);
        return outputMode;
    }

    /**
     *Turn on the power for the bar code reader.
     *
     *@return false if failed. true if success, 
     */
    public boolean openScanner() {
        LogUtil.i(TAG + " openScanner remote:" + (mIUScanManager != null));
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                result = mIUScanManager.open();
                unlockTrigger();
            } else {
                LogUtil.e(TAG + " error: openScanner , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " openScanner Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     *Turn off the power for the bar code reader.
     *
     *@return false if failed. true if success, 
     */
    public boolean closeScanner() {
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                mIUScanManager.close();
                result = true;
            } else {
                LogUtil.e(TAG + " error: closeScanner , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " closeScanner Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * Set the scan trigger active (enable the scan button)
     * 
     * @return Returns true if successful.  Returns false if failed.  
     */
    public boolean unlockTrigger() {
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                result = mIUScanManager.lockHwTriggler(false);
            } else {
                LogUtil.e(TAG + " error: unlockTrigger , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " unlockTrigger Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * Set the scan trigger inactive (disable the scan button)
     * 
     * @return Returns true if successful.  Returns false if failed.  
     */
    public boolean lockTrigger() {
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                result = mIUScanManager.lockHwTriggler(true);
            } else {
                LogUtil.e(TAG + " error: lockTrigger , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " lockTrigger Exception");
            ce.printStackTrace();
        }
        return result;
    }

    public boolean closeScannerByCamera() {
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                mIUScanManager.closeScannerByCamera();
                result = true;
            } else {
                LogUtil.e(TAG + " error: closeScannerByCamera , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " closeScannerByCamera Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * get the scanner power states
     *
     * @return true if the scanner power on.
     */
    public boolean getScannerState() {
        boolean result = false;
        try { 
            if (mIUScanManager != null) {
                int state = mIUScanManager.readConfig("SCANER_POWER");
                LogUtil.d(TAG + " getScannerState , state:" + state);
                result = state != 0;
            } else {
                LogUtil.e(TAG + " error: getScannerState , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getScannerState Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * This stops any data acquisition currently in progress.
     * 
     * @return true if stop successed.
     */
    public boolean stopDecode() {
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                mIUScanManager.softTrigger(0);
                result = true;
            } else {
                LogUtil.e(TAG + " error: stopDecode , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " stopDecode Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * Call this method to start decoding. <br>
     * 
     * @return true if the sanner and the trigger is already active
     */
    public boolean startDecode() {
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                mIUScanManager.softTrigger(1);
                result = true;
            } else {
                LogUtil.e(TAG + " error: startDecode , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " startDecode Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * get the scan trigger status
     * 
     * @return true if the scan trigger is already active
     */
    public boolean getTriggerLockState() {
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                int state = mIUScanManager.getPropertyInt(PropertyID.TRIGGERING_LOCK);
                LogUtil.d(TAG + " getTriggerLockState , state:" + state);
                result = state == 1;
            } else {
                LogUtil.e(TAG + " error: getTriggerLockState , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getTriggerLockState Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * Set factory defaults for all barcode symbology types.
     *
     * @return true if succeed
     * 
     */
    public boolean resetScannerParameters() {
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                mIUScanManager.setDefaults();
                result = true;
            } else {
                LogUtil.e(TAG + " error: startDecode , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " startDecode Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * 
     * This method set specifies which mode to control decode.
     * @param Specifies which mode. 
     * a trigger will activate the scan engine and start decoding.  It will be deactivated when a valid code is found, or when time out is reached.
     * Pulse - a trigger will activate the scan engine, and start decoding.  It will be deactivated when a valid code is found, or when the trigger is released, or when the time out is reached.  
     * Continuous - Scan engine is always on and always decoding.  
     * {@link android.device.scanner.configuration.Triggering}
     */
    public void setTriggerMode(Triggering mode) {
        LogUtil.i("setTriggerMode mode:" + mode);
        setPropertyInt(PropertyID.TRIGGERING_MODES, mode.toInt());
    }

    /**
     * 
     * Returns current configure triggering decode mode.
     * @return mode info {@link android.device.scanner.configuration.Triggering}
     */
    public Triggering getTriggerMode() {
        try {
            if (mIUScanManager != null) {
                int mode = mIUScanManager.getPropertyInt(PropertyID.TRIGGERING_MODES);
                switch (mode) {
                    case 4:
                        return Triggering.CONTINUOUS;
                    case 8:
                        return Triggering.HOST;
                    case 2:
                        return Triggering.PULSE;
                }
            } else {
                LogUtil.e(TAG + " error: getTriggerMode , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getTriggerMode Exception");
            ce.printStackTrace();
        }
        return Triggering.HOST;
    }

    /**
     *Sets one or more label programming parameters of type Int
     * @param id_buffer The indexes to the parameters to be set.   {@link android.device.scanner.configuration.PropertyID}
     * @param value_buffer the values to be used.   
     * @return
     */
    public int setParameterInts(int[] id_buffer, int[] value_buffer) {
        int idBuffLen = id_buffer.length;
        int valueBuffLen = value_buffer.length;
        int[] id_bad_buffer = new int[idBuffLen];
        int state = 0;
        try {
            if (mIUScanManager != null) {
                state = mIUScanManager.setPropertyInts(id_buffer, idBuffLen, value_buffer, valueBuffLen, id_bad_buffer);
                if (state > 0) {
                    LogUtil.e(TAG + " error: setParameterInts , state:" + state);
                }
            } else {
                state = -1;
                LogUtil.e(TAG + " error: setParameterInts , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            state = -1;
            LogUtil.e(TAG + " setParameterInts Exception");
            ce.printStackTrace();
        }
        return state;
    }

    /**
     * Gets one or more programming parameters of type Integer from the scan engine 
     * @param id_buffer  The indexes to the programming parameteres.    {@link android.device.scanner.configuration.PropertyID}
     * @return int arrary of the parameters. 
     */
    public int[] getParameterInts(int[] id_buffer) {
        if (id_buffer == null) {
            LogUtil.e(TAG + " error: getParameterInts , id arrary:" + id_buffer);
            return null;
        }
        int idBuffLen = id_buffer.length;
        int[] value_buffer = new int[idBuffLen];
        int[] id_bad_buffer = new int[idBuffLen];
        try {
            if (mIUScanManager != null) {
                int error = mIUScanManager.getPropertyInts(id_buffer , idBuffLen, value_buffer, idBuffLen, id_bad_buffer);
                if (error > 0) {
                    LogUtil.e(TAG + " error: setParameterInts , error:" + error);
                }
            } else {
                value_buffer = null;
                LogUtil.e(TAG + " error: setParameterInts , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            value_buffer = null;
            LogUtil.e(TAG + " getParameterInts Exception");
            ce.printStackTrace();
        }
        return value_buffer;
    }

    /**
     * Set the scanner parameter of type String at the specified indexes. 
     * @param id_buffer to the parameter that is to be set {@link android.device.scanner.configuration.PropertyID}
     * @param value_buffer  the string used to set the parameter 
     * @return False is returned if the index or value is error, and true is returned otherwise. 
     */
    public boolean setParameterString(int[] id_buffer, String[] value_buffer) {
        if (id_buffer == null || value_buffer == null) {
            LogUtil.e(TAG + " error: setParameterString , id arrary:" + id_buffer + ",value arrary:" + value_buffer);
            return false;
        }
        boolean state = false;
        int minLen = id_buffer.length > value_buffer.length ? value_buffer.length : id_buffer.length;
        try {
            if (mIUScanManager != null) {
                for(int i = 0; i < minLen; i++) {
                    boolean result = mIUScanManager.setPropertyString(id_buffer[i], value_buffer[i]);
                    if (!result) {
                        LogUtil.e(TAG + " error: setParameterString , id:" + id_buffer[i] + " , value:" + value_buffer[i]);
                    }
                    if (i > 0) {
                        state = result && state;
                    } else {
                        state = result;
                    }
                }
            } else {
                LogUtil.e(TAG + " error: setParameterInts , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            state = false;
            LogUtil.e(TAG + " setParameterString Exception");
            ce.printStackTrace();
        }
        LogUtil.d(TAG + " setParameterString state:" + state);
        return state;
    }

    /**
     * Gets label programming parameters of type String at the specified indexes.  
     * @param id_buffer The indexes to the programming parameteres.  {@link android.device.scanner.configuration.PropertyID}
     * @return string arrary of the parameters. 
     */
    public String[] getParameterString(int[] id_buffer) {
        if (id_buffer == null) {
            LogUtil.e(TAG + " error: getParameterString , id arrary:" + id_buffer);
            return null;
        }
        String[] value_buf = new String[id_buffer.length];
        try {
            if (mIUScanManager != null) {
                for(int i = 0; i < id_buffer.length; i++) {
                    value_buf[i] = mIUScanManager.getPropertyString(id_buffer[i]);
                }
            } else {
                LogUtil.e(TAG + " error: getParameterString , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getParameterString Exception");
            ce.printStackTrace();
        }
        return value_buf;
    }

    /**
     * Return true if the device's decoder is able to read a particular barcode
     * symbology.
     * 
     * @param barcodeType
     *            Barcode type is one of the <code>Symbology</code>.
     * @return False is returned if the decoder is not able to read the
     *         particular barcode type, and true is returned otherwise.
     * <br>
     *             Example:<br>
     *             <code>public boolean isQRSupported(ScanManager decoder) {</code>
     * <br>
     *             <code> &nbsp&nbspreturn decoder.isSymbologySupported(Symbology.QRCODE);</code>
     * <br>
     *             <code>}</code><br>
     */
    public boolean isSymbologySupported(Symbology barcodeType) {
        boolean isSupport = false;
        try {
            if (mIUScanManager != null) {
                isSupport = mIUScanManager.isSymbologySupported(barcodeType.toInt());
            } else {
                LogUtil.e(TAG + " error: isSymbologySupported , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " isSymbologySupported Exception");
            ce.printStackTrace();
        }
        return isSupport;
    }

    /**
     * Returns current enable setting for a particular barcode symbology.
     * 
     * @param barcodeType
     *            This gets the current enable setting for a particular data
     *            type. (one of the barcode typein the <code>Symbology</code>
     *            class).
     * @return False is returned if the particular data type is disabled, and
     *         true is returned otherwise.
     * <br>
     *             Example:<br>
     *             <code>public boolean isCode128Enabled(ScanManager decoder) {</code>
     * <br>
     *             <code> &nbsp&nbspreturn decoder.isSymbologyEnabled(Symbology.CODE128);</code>
     * <br>
     *             <code>}</code><br>
     */
    public boolean isSymbologyEnabled(Symbology barcodeType) {
        boolean enable = false;
        try {
            if (mIUScanManager != null) {
                enable = mIUScanManager.isSymbologyEnabled(barcodeType.toInt());
            } else {
                LogUtil.e(TAG + " error: isSymbologyEnabled , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " isSymbologyEnabled Exception");
            ce.printStackTrace();
        }
        return enable;
    }

    /**
     * Enables or disables all supported symbologies.
     * 
     * @param enable
     *            Specifies whether or not the symbologies will be enabled. If
     *            false, the symbologies are disabled, otherwise they are
     *            enabled.
     * <br>
     *             Note: <br>
     *             when the decoding configuration changes due a call to this
     *             method, the Scanner engine must be in the power on state. <br>
     * <br>
     *             Example:<br>
     *             <code>public void enableAll(ScanManager decoder) {</code><br>
     *             <code> &nbsp&nbspdecoder.enableAllSymbologies(true);</code><br>
     *             <code>}</code><br>
     */
    public void enableAllSymbologies(boolean enable) {
        try {
            if (mIUScanManager != null) {
                mIUScanManager.enableAllSymbologies(enable);
            } else {
                LogUtil.e(TAG + " error: enableAllSymbologies , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " enableAllSymbologies Exception");
            ce.printStackTrace();
        }
    }

    /**
     * Enables or disables a barcode symbology type.
     * 
     * @param barcodeType
     *            Indicates the type of data whose enable setting is to be
     *            altered. (one of the barcode type in the
     *            <code>Symbology</code> class).
     * @param enable
     *            Specifies whether or not the data type will be enabled. If
     *            false, the data type is disabled, otherwise it is enabled.
     * <br>
     *             Note: <br>
     *             when the decoding configuration changes due a call to this
     *             method,the Scanner engine must be in the power on state.  <br>
     * <br>
     *             Example:<br>
     *             <code>public void enableCode128(ScanManager decoder) {</code>
     * <br>
     *             <code> &nbsp&nbspdecoder.enableSymbology(Symbology.CODE128, true);</code>
     * <br>
     *             <code>}</code><br>
     */
    public void enableSymbology(Symbology barcodeType, boolean enable) {
        try {
            if (mIUScanManager != null) {
                mIUScanManager.enableSymbology(barcodeType.toInt() , enable);
            } else {
                LogUtil.e(TAG + " error: enableSymbology , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " enableSymbology Exception");
            ce.printStackTrace();
        }
    }

    //===========================hide========================//
    /**
     * @hide
     */
    public  int setPropertyInts(int[] id_buffer, int[] value_buffer) {
        return setParameterInts(id_buffer, value_buffer);
    }

    /**
     * @hide
     */
    public void getPropertyInts(int[] id_buffer, int[] value_buffer) {
        if (id_buffer == null || value_buffer == null) {
            LogUtil.e(TAG + " error: getPropertyInts , id arrary:" + id_buffer + ",value arrary:" + value_buffer);
            return;
        }
        int idBuffLen = id_buffer.length;
        int valueBuffLen = value_buffer.length;
        int[] id_bad_buffer = new int[idBuffLen];
        try {
            if (mIUScanManager != null) {
                int error = mIUScanManager.getPropertyInts(id_buffer, idBuffLen, value_buffer, valueBuffLen, id_bad_buffer);
                if (error > 0) {
                    LogUtil.e(TAG + " error: getPropertyInts , error:" + error);
                }
            } else {
                LogUtil.e(TAG + " error: getPropertyInts , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getPropertyInts Exception");
            ce.printStackTrace();
        }
    }

    /**
     * @hide
     */
    public boolean setPropertyString(int index, String value) {
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                result = mIUScanManager.setPropertyString(index, value);
            } else {
                LogUtil.e(TAG + " error: setPropertyString , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " setPropertyString Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * @hide
     */
    public String getPropertyString(int index) {
        try {
            if (mIUScanManager != null) {
                return mIUScanManager.getPropertyString(index);
            } else {
                LogUtil.e(TAG + " error: getPropertyString , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getPropertyString Exception");
            ce.printStackTrace();
        }
        return null;
    }

    public boolean getCameraStatus() {
        try {
            if (mIUScanManager != null) {
                return mIUScanManager.getCameraStatus();
            } else {
                LogUtil.e(TAG + " error: getCameraStatus , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getCameraStatus Exception");
            ce.printStackTrace();
        }
        return false;
    }

    public void setCameraStatus(boolean status) {
        try {
            if (mIUScanManager != null) {
                mIUScanManager.setCameraStatus(status);
            } else {
                LogUtil.e(TAG + " error: setCameraStatus , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " setCameraStatus Exception");
            ce.printStackTrace();
        }
    }

    public int getScannerType() {
        try {
            if (mIUScanManager != null) {
                return mIUScanManager.readConfig("SCANER_TYPE");
            } else {
                LogUtil.e(TAG + " error: getScannerType , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getScannerType Exception");
            ce.printStackTrace();
        }
        return 0;
    }

    // so rubbish , just compatable with open api
    public boolean setOutputParameter(int type, int value) {
        boolean result = false;
        try {
            switch (type) {
                case 1:
                    result = setPropertyInt(PropertyID.GOOD_READ_BEEP_ENABLE, value);
                    break;
                case 2:
                    result = setPropertyInt(PropertyID.GOOD_READ_VIBRATE_ENABLE, value);
                    break;
                case 3:
                    result = setPropertyInt(PropertyID.LABEL_APPEND_ENTER, value);
                    break;
                case 4:
                    if (mIUScanManager != null) {
                        mIUScanManager.writeConfig("SCANER_POWER", value);
                        result = true;
                    } else {
                        LogUtil.e(TAG + " error: setOutputParameter POWER , ScanManager:" + mIUScanManager);
                    }
                    break;
                case 5:
                    result = setPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE, value);
                    break;
                case 6:
                    result = setPropertyInt(PropertyID.TRIGGERING_LOCK, value);
                    break;
                case 7:
                    if (mIUScanManager != null) {
                        mIUScanManager.writeConfig("SCANER_TYPE", value);
                        result = true;
                    } else {
                        LogUtil.e(TAG + " error: setOutputParameter TYPE , ScanManager:" + mIUScanManager);
                    }
                    break;
                case 8:
                    result = resetScannerParameters();
                    break;
                case 9:
                    result = setPropertyInt(PropertyID.TRIGGERING_MODES, value);
                    break;
                default:
                    result = false;
                    LogUtil.i(TAG + " type:" + type + "not support");
                    break;
            }
        } catch (Exception e) {
            LogUtil.e(TAG + " setOutputParameter Exception");
            e.printStackTrace();
        }
        return result;
    }

    public int getOutputParameter(int type) {
        try {
            if (mIUScanManager != null) {
                switch (type) {
                    case 1:
                        return mIUScanManager.getPropertyInt(PropertyID.GOOD_READ_BEEP_ENABLE);
                    case 2:
                        return mIUScanManager.getPropertyInt(PropertyID.GOOD_READ_VIBRATE_ENABLE);
                    case 3:
                        return mIUScanManager.getPropertyInt(PropertyID.LABEL_APPEND_ENTER);
                    case 4:
                        return getScannerState() ? 1 : 0;
                    case 5:
                        return mIUScanManager.getPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE);
                    case 6:
                        return mIUScanManager.getPropertyInt(PropertyID.TRIGGERING_LOCK);
                    case 7:
                        return getScannerType();
                    case 9:
                        return mIUScanManager.getPropertyInt(PropertyID.TRIGGERING_MODES);
                    default:
                        LogUtil.i(TAG + " getOutputParameter type:" + type + "not support");
                        break;
                }
            } else {
                LogUtil.e(TAG + " error: getOutputParameter , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getOutputParameter Exception");
            ce.printStackTrace();
        }
        return 0;
    }

    // recommend api
    /**
     * @hide
     */
    public boolean setOutputParameter(String type, String value) {
        boolean result = false;
        try {
            if (mIUScanManager != null) {
                mIUScanManager.writeConfigs("SCANER_" + type, value);
                result = true;
            } else {
                LogUtil.e(TAG + " error: setOutputParameter , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " setOutputParameter Exception");
            ce.printStackTrace();
        }
        return result;
    }

    /**
     * @hide
     */
    public String getOutputParameter(String type, String value) {
        try {
            if (mIUScanManager != null) {
                return mIUScanManager.readConfigs("SCANER_" + type, value);
            } else {
                LogUtil.e(TAG + " error: getOutputParameter , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " getOutputParameter Exception");
            ce.printStackTrace();
        }
        return null;
    }

    public void writeConfig(String name,int value) {
        try {
            if (mIUScanManager != null) {
                mIUScanManager.writeConfig(name, value);
            } else {
                LogUtil.e(TAG + " error: writeConfig , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " writeConfig Exception");
            ce.printStackTrace();
        }
    }

    public int readConfig(String name) {
        try {
            if (mIUScanManager != null) {
                return mIUScanManager.readConfig(name);
            } else {    
                LogUtil.e(TAG + " error: readConfig , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " readConfig Exception");
            ce.printStackTrace();
        }
        return 0;
    }

    public void addScanCallBack(IScanCallBack cb) {
        try {
            if (mIUScanManager != null) {
                mIUScanManager.addScanCallBack(cb);
            } else {
                LogUtil.e(TAG + " error: addScanCallBack , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " addScanCallBack Exception");
            ce.printStackTrace();
        }
    }

    public void removeScanCallBack(IScanCallBack cb) {
        try {
            if (mIUScanManager != null) {
                mIUScanManager.removeScanCallBack(cb);
            } else {
                LogUtil.e(TAG + " error: addScanCallBack , ScanManager:" + mIUScanManager);
            }
        } catch (RemoteException ce) {
            LogUtil.e(TAG + " addScanCallBack Exception");
            ce.printStackTrace();
        }
    }

}
