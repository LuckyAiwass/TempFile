package com.android.server.scanner;

import java.io.IOException;
import java.io.FileOutputStream;
// urovo add shenpidong begin 2019-04-17
import android.hardware.Camera.CameraInfo;
import android.util.Log;
// urovo add shenpidong end 2019-04-17

public class ScanUtil {

    public static final String TAG = "ScanUtil";
    public static final int AIMLED_SE2100_STATE = 1;
    public static final int FLASHLED_SE2100_STATE = 2;
    public static final String AIMLED_SE2100 = "/sys/class/misc/moto_sdl/enable_aimer_led";
    public static final String AIMLED_FLASH_SE2100 = "/sys/class/misc/moto_sdl/enable_flash_aim_led";
    public static final String FLASHLED_SE2100 = "/sys/class/misc/moto_sdl/enable_illum_led";
    public static final byte[] LIGHT_ON = {'1'};
    public static final byte[] LIGHT_OFF = {'0'};
//    public static boolean isFlash = false;
    public static final int OFF = 0x00000000;
    public static final int AIMER_ONLY = 0x00000001;
    public static final int ILLUM_ONLY = 0x00000010;
    public static final int ALTERNATING = 0x00000100;
    public static final int CONCURRENT = 0x00001000;
    public static final int PICKLIST = 0x000100000;

    private static final byte[] getAIMLEDState(int value) {
	if(value >= 1) {
	    return LIGHT_ON;
	}
	return LIGHT_OFF;
    }

    public static int getMode(int mode) {
	if(mode == 0) {
	    return OFF;
	} else if(mode == 1) {
	    return AIMER_ONLY;
	} else if(mode == 2) {
	    return ILLUM_ONLY;
	} else if(mode == 3) {
	    return ALTERNATING;
	} else if(mode == 4) {
	    return CONCURRENT;
	} else if(mode == PICKLIST) {
	    return PICKLIST;
	}
	return ALTERNATING;
    }

    private static final String getLEDPath(int led) {
	if(led == AIMLED_SE2100_STATE) {
	    return AIMLED_SE2100;
	} else if(led == FLASHLED_SE2100_STATE) {
	    return FLASHLED_SE2100;
	}
	return null;
    }

/*
    static class Flash_AIM_LED extends Thread {
	
	public void run() {
	    FileOutputStream fLed = null;
	    try {
		fLed = new FileOutputStream(AIMLED_FLASH_SE2100);
		boolean flash = true;
		while(isFlash) {
		    fLed.write(getAIMLEDState(flash ? 1 : 0));
		    fLed.flush();
		    flash = !flash;
		    try {
			Thread.sleep(50);
		    } catch(Exception e) {
		    }
		}
	    } catch(IOException e) {
		e.printStackTrace();
	    } finally {
		isFlash = false;
		if(fLed!=null) {
		    try {
			fLed.close();
		    } catch(IOException ee) {
			ee.printStackTrace();
		    }
		}
	    }
	}
    }
*/

    public static boolean update_LED_state(int type , int led , int value , int mode) {
	String path = getLEDPath(led);
	android.util.Log.d(TAG , "update_LED_state , type:" + type + ",led:" + led + ",value:" + value);
	if(path !=null && type == ScannerFactory.TYPE_SE2100) {
	    FileOutputStream fLed = null;
	    try {
		fLed = new FileOutputStream(path);
		fLed.write(getAIMLEDState(value==1?1:0));
		fLed.flush();
		if((OFF | mode) == OFF || (AIMER_ONLY & mode) == AIMER_ONLY || (ILLUM_ONLY & mode) == ILLUM_ONLY || (PICKLIST & mode) == PICKLIST) {
//	android.util.Log.d(TAG , "update_LED_state , type:" + type + ",led:" + led + ",----------value:" + value);
		    value = 0;
		}
		if(led == AIMLED_SE2100_STATE) {
	android.util.Log.d(TAG , "update_LED_state , ----------value:" + value);
/*
		    isFlash = value == 1;
		    if(false && isFlash) {
			new Flash_AIM_LED().start();
		    } else {
			fLed = new FileOutputStream(AIMLED_FLASH_SE2100);
			fLed.write(getAIMLEDState(value));
			fLed.flush();
		    }
*/
		    fLed = new FileOutputStream(AIMLED_FLASH_SE2100);
		    fLed.write(getAIMLEDState(value==1?1:0));
		    fLed.flush();
		}
		return true;
	    } catch(IOException e) {
		e.printStackTrace();
	    } finally {
		if(fLed!=null) {
		    try {
			fLed.close();
		    } catch(IOException ee) {
			ee.printStackTrace();
		    }
		}
	    }
	}
	return false;
    }

    // urovo add shenpidong begin 2019-04-17
    public static int scannerID() {
	int num = android.hardware.Camera.getNumberOfCameras();
	Log.i(TAG, "scannerID , num:" + num);
	int scanID = 0;
	/*
	CameraInfo info = new CameraInfo();
	for(int i=0 ; i < num ; i++ ) {
	    android.hardware.Camera.getCameraInfo(i, info);
	    if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
		Log.i(TAG, "scannerID , back camera found:" + i);
		scanID++;
	    } else if(info.facing == CameraInfo.CAMERA_FACING_FRONT) {
		Log.i(TAG, "scannerID , front camera found:" + i);
		scanID++;
	    } else {
		Log.i(TAG, "scannerID , other camera found:" + i);
//		scanID = i;
	    }
	}
	*/
	scanID=num; //juzhitao add
	Log.e(TAG, "scannerID , num:" + num + ",scanID:" + scanID);
	return scanID;
    }
    // urovo add shenpidong end 2019-04-17

    // urovo add shenpidong begin 2019-05-06
    public static void searchLoopGSAndReplase(byte[] arr) {
	if(arr == null || arr.length <= 0) {
	    Log.i(TAG, "searchLoopGSAndReplase ignore. array is " + (arr==null) + ", array length is " + (arr!=null?arr.length:-1));
	    return;
	}
	for(int i=0;i<arr.length;i++) {
	    // GS replace space
	    if(arr[i] == 0x1D) { // 1D ASCII is GS
		arr[i] = 0x20; // 0x20 ASCII is space
	    }
	}
    }
    // urovo add shenpidong end 2019-05-06

    // urovo add shenpidong begin 2019-07-24
    /** 
     * 6603 UPC-E1
     * aimID is 0x58 , Capital X
     * aimModifier is 0x30 , number 0
     * codeID is 0x45 , Capital E
     */
    public static boolean isBarCodeUPC_E1(byte aimID , byte aimModifier , byte codeID) {
	if(aimID == 0x58 && aimModifier == 0x30 && codeID == 0x45) {
	    return true;
	}
	return false;
    }
    // urovo add shenpidong end 2019-07-24

    // urovo add shenpidong begin 2019-07-24
    public static byte barCodeUPC_E1_Checksum(byte[] data) {
	byte checksum = -1;
	if(data == null || data.length == 0) {
	    Log.i(TAG, "UPC_E1_Checksum , data:" + data);
	    return checksum;
	}
	int oddSum = 0 , evenSum = 0;
	boolean isChecksum = false;
	for(int i=0; i<data.length; i++) {
	    if(data[i] >= 0x30 && data[i] <= 0x39) { // ASCII 0x30 ~ 0x39 , number is 0 1 2 ... 9
		if((i & 1) != 1) { // odd number sum
		    oddSum += data[i] - 0x30;
//		    Log.i(TAG, "UPC_E1_Checksum , oddSum:" + oddSum + ",data[" + i + "]:" + data[i] + ",s:" + (data[i] - 0x30));
		} else { // even number sum
		    evenSum += data[i] - 0x30;
//		    Log.i(TAG, "UPC_E1_Checksum ,2 oddSum:" + oddSum + ",data[" + i + "]:" + data[i] + ",s:" + (data[i] - 0x30));
		}
		isChecksum = true;
	    } else {
		Log.i(TAG, "UPC-E1 Checksum igore.");
		checksum = -1;
		isChecksum = false;
		break;
	    }
	}
	if(isChecksum) {
	    // oddSum * 3 = ((oddSum << 1) + oddSum)
	    int sum = ((oddSum << 1) + oddSum) + evenSum;
	    checksum = (byte)(((10 - sum % 10)%10) + 0x30);
//	    Log.i(TAG, "UPC_E1_Checksum , sum:" + sum + ",dd:" + ((oddSum << 1) + oddSum));
	}
//	Log.i(TAG, "UPC_E1_Checksum , isChecksum:" + isChecksum + ",checksum:" + checksum + ",oddSum:" + oddSum + ",evenSum:" + evenSum + ",odd:" + ((oddSum << 1) + oddSum));
	return checksum;
    }
    // urovo add shenpidong end 2019-07-24

}
