/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.device;

import java.util.Map;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.content.Context;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Symbology;
import android.device.scanner.configuration.Triggering;
import android.os.RemoteException;
import android.os.IScanService;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.scanner.IScanCallBack;

//import com.android.server.ScanService;

/**
 * The ScanManager class provides developers access to barcode reader related in the device
 */
public class ScanManager {

	private static final String TAG = "ScanManager";
	private int scanType;
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
     * String contains the output data as a byte array. In the case of concatenated bar codes, the decode data is
     *  concatenated and sent out as a single array
     *  
     */
    public static final String DECODE_DATA_TAG = "barcode";
	private IScanService mScanService;
    
    //urovo add jinpu.lin 2019.04.26
    public static final String CACHE_LIMIT_ENABLE = "CACHE_LIMIT_ENABLE";
    public static final String CACHE_LIMIT_TIME = "CACHE_LIMIT_TIME";
    public static final String CACHE_ENABLE = "CACHE_ENABLE";
    //urovo add end 2019.04.26
	public ScanManager() {

		IScanService mService = IScanService.Stub.asInterface(ServiceManager
				.getService(Context.SCAN_SERVICE));
		mScanService = mService;
        try {
            mScanService.init();
        } catch (RemoteException e){
            e.printStackTrace();
        }
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
		Log.i(TAG, "switchInputState......[" + mode + "]");
		setPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE, mode);
		return true;
	}
    /**
	 * get current the scan result output mode
	 * 
	 * @return Returns zero if the barcode is sent as intent.  Returns 1 if barcode is sent to the text box in focus
	 */
	 public int getOutputMode(){
			try {
			    if (mScanService != null)
				return mScanService.getPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE);
			} catch (Exception e) {
				// TODO: handle exception
			}
			return 0;
	 }
	 
	/**
	 *Turn on the power for the bar code reader.
	 *
     *@return false if failed. true if success, 
	 */
	public boolean openScanner() {
        Log.i(TAG, "openScanner: mScanService=" + mScanService);
	    if (mScanService == null)  return false;
		try {
			mScanService.open();
			unlockTriggler();
		} catch (android.os.RemoteException e) {
		}
		//ScanNative.unlockTriggle();
		/* initOutputMode(); */
		return true;
	}

	/**
     *Turn off the power for the bar code reader.
     *
     *@return false if failed. true if success, 
     */
	public boolean closeScanner() {
        Log.i(TAG, "closeScanner: mScanService=" + mScanService);
	    if (mScanService == null)  return false;
		try {
			mScanService.close();
		} catch (android.os.RemoteException e) {
		}
		return true;
	}

	public boolean closeScannerByCamera() {
        Log.i(TAG, "closeScanner: mScanService=" + mScanService);
	    if (mScanService == null)  return false;
		try {
			mScanService.closeScannerByCamera();
		} catch (android.os.RemoteException e) {
		}
		return true;
	}

	/**
	 *get the scanner power states
	 *
	 *@return true if the scanner power on.
	 */
	public boolean getScannerState() {
		try {
		    if (mScanService != null){
    			int state = mScanService.readConfig("SCANER_POWER");
    			if (state == 0)
    				return false;
    			else
    				return true;
		    }
		} catch (RemoteException e) {
			Log.i(TAG, "getScannerState..... exception");
		}
		return false;
	}

	/**
     * This stops any data acquisition currently in progress.
     * 
     * @return true if stop successed.
	 */
	public boolean stopDecode() {
		Log.i(TAG, "stopDecode......");
        Log.i(TAG, "mScanServcie ==== " + mScanService);
		if (mScanService == null)  return false;
		try {
            mScanService.softTrigger(0);
        } catch (RemoteException e) {
        }
		return true;

	}

	/**
     * Call this method to start decoding. <br>
     * 
     * @return true if the sanner and the trigger is already active
	 */
	public boolean startDecode() {
		Log.i(TAG, "startDecode  scantyp......[" + scanType + "]");
		if (mScanService == null)  return false;
		try {
            mScanService.softTrigger(1);
        } catch (RemoteException e) {
        }
		return true;
	}
	
	/**
	 * Set the scan trigger inactive (disable the scan button)
	 * 
	 * @return Returns true if successful.  Returns false if failed.  
	 */
	public boolean lockTrigger() {
        Log.i(TAG, "lockTrigger......");
        if (mScanService == null)  return false;
        try {
            return mScanService.lockHwTriggler(true);
        } catch (RemoteException e) {
        }
        return false;
    }

	/**
     * Set the scan trigger active (enable the scan button)
     * 
     * @return Returns true if successful.  Returns false if failed.  
     */
	public boolean unlockTrigger() {
	    Log.i(TAG, "unlockTriggle......");
	    if (mScanService == null)  return false;
        try {
            return mScanService.lockHwTriggler(false);
        } catch (RemoteException e) {
        }
        return false;
	}

	/**
     * get the scan trigger status
     * 
     * @return true if the scan trigger is already active
     */
	public boolean getTriggerLockState() {
	    if (mScanService == null)  return false;
		int state = 0;
		try {
			state = mScanService.getPropertyInt(PropertyID.TRIGGERING_LOCK);
		} catch (RemoteException e) {
		}
		if (state == 1)
			return true;
		else
			return false;
	}

	/**
     * Set factory defaults for all barcode symbology types.
     *
	 * @return true if succeed
	 * 
	 */
	public boolean resetScannerParameters() {
	    if (mScanService == null)  return false;
		Log.i(TAG, "resetScannerParameters......[" + scanType + "]");
		/*int scantyp = getScannerType();
		if (scantyp == 1)
			ScanNative.doopticonset();
		if (scantyp == 2)
			ScanNative.doDefaultSet();
		if (scantyp == 3 || scantyp == 6)
			ScanNative.dohonywareset();
		if (scantyp == 0)
			ScanNative.doopticonset();
		if (scantyp == 7)
			ScanNative.dominjiedefaultset();
		*/
        try {
            mScanService.setDefaults();
        } catch (RemoteException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
		return true;
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
	    setPropertyInt(PropertyID.TRIGGERING_MODES, mode.toInt());
    }
	
	/**
     * 
     * Returns current configure triggering decode mode.
     * @return mode info {@link android.device.scanner.configuration.Triggering}
     */
    public Triggering getTriggerMode() {
        try {
            if (mScanService != null) {
                int mode = mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES);
                switch (mode) {
                    case 4:
                        return Triggering.CONTINUOUS;
                    case 8:
                        return Triggering.HOST;
                    case 2:
                        return Triggering.PULSE;
                }
            }
        } catch (RemoteException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return Triggering.HOST;
    }
	
	private void setPropertyInt(int index, int value) {
	    int[] id_buffer = new int[1];
        int[] value_buffer = new int[1];
        id_buffer[0] = index;
        int[] id_bad_buffer = new int[1];
        value_buffer[0] = value;
        try{
            if(mScanService != null) {
                int error = mScanService.setPropertyInts(id_buffer, 1, value_buffer, 1,
                        id_bad_buffer);
                
                if (error > 0) {
                    Log.e(TAG,"Config error ids: in setPropertyInt: " + id_bad_buffer[0]);
                }
            }
        } catch(RemoteException ce) {
            Log.e(TAG,"Decoder error: in setPropertyInt");
        }
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

        try {
            if(mScanService != null) {
                int error = mScanService.setPropertyInts(id_buffer, idBuffLen, value_buffer, valueBuffLen,
                        id_bad_buffer);
                if (error > 0) {
                    Log.e(TAG,"Decoder error: in setParameterInts");
                    return error;
                }
            }
        } catch (RemoteException ex) {
            Log.e(TAG,"Decoder error: in setParameterInts");
            return -1;
        }
        return 0;
	}
   
	/**
	 * Gets one or more programming parameters of type Integer from the scan engine 
	 * @param id_buffer  The indexes to the programming parameteres.    {@link android.device.scanner.configuration.PropertyID}
	 * @return int arrary of the parameters. 
	 */
   public int[] getParameterInts(int[] id_buffer) {
       // TODO Auto-generated method stub
       int idBuffLen = id_buffer.length;
       int[] value_buffer = new int[idBuffLen];
       //int valueBuffLen = value_buffer.length;
       int[] id_bad_buffer = new int[idBuffLen];// no support id
       try {
           if(mScanService != null) {
               int error = mScanService.getPropertyInts(id_buffer, idBuffLen, value_buffer, idBuffLen,
                       id_bad_buffer);
               if (error > 0) {
                   Log.e(TAG,"Decoder error: in getPropertyInts");
                   return value_buffer;
               }
           }
       } catch (RemoteException e) {
           Log.e(TAG,"Decoder error: in getPropertyInts");
           return null;
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
       int minLen = id_buffer.length > value_buffer.length ? value_buffer.length : id_buffer.length;
       try {
           if(mScanService != null) {
               for(int i = 0; i < minLen; i++) {
                   mScanService.setPropertyString(id_buffer[i], value_buffer[i]);
               }
               return true;
           }
        } catch (RemoteException e) {
            Log.e(TAG,"Decoder error: in setPropertyString");
        }
       return false;
   }

	/**
     * Gets label programming parameters of type String at the specified indexes.  
     * @param id_buffer The indexes to the programming parameteres.  {@link android.device.scanner.configuration.PropertyID}
     * @return string arrary of the parameters. 
     */
    public String[] getParameterString(int[] id_buffer) {
        String[] value_buf = new String[id_buffer.length];
        try {
            if(mScanService != null) {
                for(int i = 0; i < id_buffer.length; i++) {
                    value_buf[i] = mScanService.getPropertyString(id_buffer[i]); 
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG,"Decoder error: in setPropertyString");
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
            if(mScanService != null) {
                isSupport = mScanService.isSymbologySupported(barcodeType.toInt());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
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
        try {
            if(mScanService != null) {
                return mScanService.isSymbologyEnabled(barcodeType.toInt());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
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
            if(mScanService != null) {
                mScanService.enableAllSymbologies(enable);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
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
        int type = barcodeType.toInt();
        Log.d(TAG, "barcodeType  =" + type);
        try {
            if(mScanService != null) {
                mScanService.enableSymbology(type, enable);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
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
        // TODO Auto-generated method stub
        int idBuffLen = id_buffer.length;
        int valueBuffLen = value_buffer.length;
        int[] id_bad_buffer = new int[idBuffLen];// no support id
        try {
            if(mScanService != null) {
                int error = mScanService.getPropertyInts(id_buffer, idBuffLen, value_buffer, valueBuffLen,
                        id_bad_buffer);
                if (error > 0) {
                    Log.e(TAG,"Decoder error: in getPropertyInts");
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG,"Decoder error: in getPropertyInts");
        }
    }
    /**
     * @hide
     */
    public boolean setPropertyString(int index, String value) {
        try {
            if(mScanService != null) {
                return mScanService.setPropertyString(index, value);
            }
        } catch (RemoteException e) {
            Log.e(TAG,"Decoder error: in setPropertyString");
        }
       return false;
    }
    /**
     * @hide
     */
     public String getPropertyString(int index) {
         try {
             if(mScanService != null) {
                 return mScanService.getPropertyString(index);
             }
         } catch (RemoteException e) {
             Log.e(TAG,"Decoder error: in setPropertyString");
         }
         return null;
     }
     public boolean getCameraStatus() {
         try {
             if(mScanService != null) {
                 return mScanService.getCameraStatus();
             }
         } catch (RemoteException e) {
             Log.e(TAG,"Decoder error: getCameraStatus");
         }
         return false;
     }
     public void setCameraStatus(boolean status) {
         try {
             if(mScanService != null) {
                 mScanService.setCameraStatus(status);
             }
         } catch (RemoteException e) {
             Log.e(TAG,"Decoder error: in setCameraStatus");
         }
     }
    /**
     * 
     * @hide
     */
    public boolean initOutputMode(){
        return true;
    }

    public int getScannerType(){
           try {
               if(mScanService != null) {
                   return mScanService.readConfig("SCANER_TYPE");
               }
           } catch (Exception e) {
               // TODO: handle exception
               e.printStackTrace();
           }
           return 0;        
    }
 // so rubbish , just compatable with open api
    public boolean setOutputParameter(int type, int value) {
        try {
            switch (type) {
            case 1:
                setPropertyInt(PropertyID.GOOD_READ_BEEP_ENABLE, value);
                break;
            case 2:
                setPropertyInt(PropertyID.GOOD_READ_VIBRATE_ENABLE, value);
                break;
            case 3:
                setPropertyInt(PropertyID.LABEL_APPEND_ENTER, value);
                break;
            case 4:
                if(mScanService != null) {
                    mScanService.writeConfig("SCANER_POWER", value);
                }
                break;
            case 5:
                setPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE, value);
                break;
            case 6:
                setPropertyInt(PropertyID.TRIGGERING_LOCK, value);
                break;
            case 7:
                if(mScanService != null) {
                    mScanService.writeConfig("SCANER_TYPE", value);
                }
                break;
            case 8:
                resetScannerParameters();
                break;
            case 9:
                setPropertyInt(PropertyID.TRIGGERING_MODES, value);
                break;
            default:
                Log.i(TAG, "type" + "" + type + "not support");
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "setOutputParameter..... exception");
        }
        return true;
    }

    public int getOutputParameter(int type) {
        try {
            if(mScanService != null) {
                switch (type) {
                case 1:
                    return mScanService.getPropertyInt(PropertyID.GOOD_READ_BEEP_ENABLE);//mScanService.readConfig("SCANER_SOUND");
                case 2:
                    return mScanService.getPropertyInt(PropertyID.GOOD_READ_VIBRATE_ENABLE);//mScanService.readConfig("SCANER_VIB");
                case 3:
                    return mScanService.getPropertyInt(PropertyID.LABEL_APPEND_ENTER);//mScanService.readConfig("SCANER_ENTER");
                case 4:
                    return getScannerState() ? 1 : 0;//mScanService.readConfig("SCANER_POWER");
                case 5:
                    return mScanService.getPropertyInt(PropertyID.WEDGE_KEYBOARD_ENABLE);//mScanService.readConfig("SCANER_MODE");
                case 6:
                    return mScanService.getPropertyInt(PropertyID.TRIGGERING_LOCK);//mScanService.readConfig("SCANER_KEY");
                case 7:
                    return getScannerType();//mScanService.readConfig("SCANER_TYPE");
                case 9:
                    return mScanService.getPropertyInt(PropertyID.TRIGGERING_MODES);
                default:
                    Log.i(TAG, "type" + "" + type + "not support");
                    break;
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "getOutputParameter..... exception");
        }
        return 0;
    }

    // recommend api
    /**
     * @hide
     */
    public boolean setOutputParameter(String type, String value) {
        try {
            if (mScanService != null && type != null)
                mScanService.writeConfigs("SCANER_" + type, value);
            else
                Log.i(TAG, "type" + "" + type + "not support");
        } catch (Exception e) {
            Log.i(TAG, "setOutputParameters..... exception");
        }
        return true;
    }

    /**
     * @hide
     */
    public String getOutputParameter(String type, String value) {
        try {
            if (mScanService != null && type != null)
                return mScanService.readConfigs("SCANER_" + type, value);
            else if( type != null && type.equals("SN"))
                return SystemProperties.get("persist.sys.product.serialno");
            else
                Log.i(TAG, "type" + "" + type + "not support");
        } catch (Exception e) {
            Log.i(TAG, "getOutputParameters..... exception");
        }
        return null;
    }
    
    /**
     * @hide
     */
    public boolean doAck() {
        boolean ret = ScanNative.doAck();
        return ret;
    }

    /**
     * @hide
     */
    public boolean doNack(int reason) {
        boolean ret = ScanNative.doNack(reason);
        return ret;
    }
    /**
     * @hide
     */
    public boolean lockTriggler() {
        return lockTrigger();
    }
    /**
     * @hide
     */
    public boolean unlockTriggler() {//error name
        return unlockTrigger();
    }
    /**
     * @hide
     */
    public Map<String, Integer> getScanerList() {
        try {
            if (mScanService != null)
            return mScanService.getScanerList();
        } catch (RemoteException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return true if succeed if the mathing have not set misc,use this
     * mothed,this method is used in misc apk
     */
    public boolean resetScannerParameters(int scantyp) {
        if (mScanService == null)
            return false;
        if (scantyp == 1)
            ScanNative.doopticonset();
        if (scantyp == 2)
            ScanNative.doDefaultSet();
        if (scantyp == 3 || scantyp == 6)
            ScanNative.dohonywareset();
        if (scantyp == 0)
            ScanNative.doopticonset();
        if (scantyp == 5)
            ScanNative.resetDM30Scanner();
        if (scantyp == 7)
            ScanNative.dominjiedefaultset();
        Log.i(TAG, "resetScannerParameters(int)...[" + scantyp + "]");
        try {
            mScanService.setDefaults();
        } catch (RemoteException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return true;
    }
    /**
     * @hide
     */
    public boolean setIndicatorBlue(int onoff){
        if (mScanService == null) return false;
        ScanNative.setIndicatorBlue(onoff); 
        return true;
    }
    
    //urovo add jinpu.lin 2019.04.26
    public void writeConfig(String name,int value) {
        try {
            mScanService.writeConfig(name, value);
        } catch (RemoteException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
    public int readConfig(String name) {
        try {
            return mScanService.readConfig(name);
        } catch (RemoteException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return 0;
    }
    //urovo add end 2019.04.26s

    public void addScanCallBack(IScanCallBack cb) {
        try {
            mScanService.addScanCallBack(cb);
        } catch (RemoteException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public void removeScanCallBack(IScanCallBack cb) {
        try {
            mScanService.removeScanCallBack(cb);
        } catch (RemoteException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
