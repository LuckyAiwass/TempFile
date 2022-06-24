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
import android.os.ServiceManager;
import android.util.Log;
import android.hardware.light.V2_0.Type;
import android.hardware.light.V2_0.Brightness;
import android.os.Build;
import android.os.IULightsManager;
import android.os.RemoteException;

public class ULightsManager {

	public static final String TAG = "ULightsManager";
    static final boolean DEBUG = false;
	private static IULightsManager sService;

    public static final int LIGHT_ID_BACKLIGHT = Type.BACKLIGHT;
    public static final int LIGHT_ID_KEYBOARD = Type.KEYBOARD;
    public static final int LIGHT_ID_BUTTONS = Type.BUTTONS;
    public static final int LIGHT_ID_BATTERY = Type.BATTERY;
    public static final int LIGHT_ID_NOTIFICATIONS = Type.NOTIFICATIONS;
    public static final int LIGHT_ID_ATTENTION = Type.ATTENTION;
    public static final int LIGHT_ID_BLUETOOTH = Type.BLUETOOTH;
    public static final int LIGHT_ID_WIFI = Type.WIFI;
    public static final int LIGHT_ID_COUNT = Type.COUNT;
    //urovo custom led
    public static final int POWER_RED = LIGHT_ID_COUNT + 1;
    public static final int POWER_GREEN = LIGHT_ID_COUNT + 2;
    public static final int SCAN_RED = LIGHT_ID_COUNT + 3;
    public static final int SCAN_BLUE = LIGHT_ID_COUNT + 4;
    public static final int EXT_RED = LIGHT_ID_COUNT + 5;
    public static final int EXT_GREEN = LIGHT_ID_COUNT + 6;
    public static final int EXT_BLUE = LIGHT_ID_COUNT + 7;
	public static final int LED_ID_COUNT = EXT_BLUE - LIGHT_ID_COUNT;


	private LedControl POWER_RED_SQ53C = 
	new LedControl("/sys/class/leds/red/brightness", 0, 255);
	private LedControl POWER_GREEN_SQ53C = 
	new LedControl("/sys/class/leds/green/brightness", 0, 255);
	private LedControl SCAN_RED_SQ53C = 
	new LedControl("/sys/class/doubleled/led/doubleled", 2, 1);// 1 红灯亮 2 红灯灭 3 蓝灯亮
	private LedControl SCAN_BLUE_SQ53C = 
	new LedControl("/sys/class/doubleled/led/doubleled", 4, 3);// 1 红灯亮 2 红灯灭 3 蓝灯亮

    private LedControl POWER_RED_SQ38 = 
    new LedControl("/sys/class/leds/red/brightness", 0, 255);
    private LedControl POWER_GREEN_SQ38 = 
    new LedControl("/sys/class/leds/green/brightness", 0, 255);
    private LedControl SCAN_RED_SQ38 = 
    new LedControl("/sys/class/doubleled/led/doubleled", 2, 1);// 1 红灯亮 2 红灯灭 3 蓝灯亮
    private LedControl SCAN_BLUE_SQ38 = 
    new LedControl("/sys/class/doubleled/led/doubleled", 4, 3);// 1 红灯亮 2 红灯灭 3 蓝灯亮
    private LedControl EXT_RED_SQ38 = 
    new LedControl("/sys/class/doubleled/led/doubleled", 6, 5);// 1 红灯亮 2 红灯灭 3 蓝灯亮
    private LedControl EXT_GREEN_SQ38 = 
    new LedControl("/sys/class/doubleled/led/doubleled", 8, 7);// 7

    private LedControl POWER_RED_SQ53 = 
	new LedControl("/sys/class/leds/red/brightness", 0, 255);
	private LedControl POWER_GREEN_SQ53 = 
	new LedControl("/sys/class/leds/green/brightness", 0, 255);
	private LedControl SCAN_RED_SQ53 = 
	new LedControl("/sys/class/leds/left-red/brightness", 0, 255);// 1 红灯亮 2 红灯灭 3 蓝灯亮
	private LedControl SCAN_BLUE_SQ53 = 
	new LedControl("/sys/class/leds/blue/brightness", 0, 255);// 1 红灯亮 2 红灯灭 3 蓝灯亮

    private LedControl POWER_RED_SQ45 = 
    new LedControl("/sys/class/leds/red/brightness", 0, 255);
    private LedControl POWER_GREEN_SQ45 = 
    new LedControl("/sys/class/leds/green/brightness", 0, 255);
    private LedControl SCAN_BLUE_SQ45 = 
    new LedControl("/sys/goodix/led_test/led_test", 0, 255);

    private LedControl POWER_RED_SQ51FW = 
    new LedControl("/sys/class/leds/red/brightness", 0, 255);
    private LedControl SCAN_RED_SQ51FW = 
    new LedControl("/sys/bus/i2c/drivers/AW2013_LED/2-0045/led", 2, 3);
    private LedControl POWER_GREEN_SQ51FW = 
    new LedControl("/sys/bus/i2c/drivers/AW2013_LED/2-0045/led", 0, 1);
    private LedControl SCAN_BLUE_SQ51FW = 
    new LedControl("/sys/bus/i2c/drivers/AW2013_LED/2-0045/led", 4, 5);


	public ULightsManager(){
		IBinder b = ServiceManager.getService(TAG);
        sService = IULightsManager.Stub.asInterface(b);
	}

    public LedControl getLedControl(int id) {
    	if(Build.PROJECT.equals("SQ53C")) {
    		switch(id) {
    			case POWER_RED:
    				return POWER_RED_SQ53C;
    			case POWER_GREEN:
                    return POWER_GREEN_SQ53C;
    			case SCAN_RED:
    				return SCAN_RED_SQ53C;
    			case SCAN_BLUE:
    				return SCAN_BLUE_SQ53C;
    			default:
    				return SCAN_BLUE_SQ53C;
            }
        } else if(Build.PROJECT.equals("SQ53") || Build.PROJECT.equals("SQ51S") || Build.PROJECT.equals("SQ53Q")) {
            switch(id) {
                case POWER_RED:
                    return POWER_RED_SQ53;
                case POWER_GREEN:
                    return POWER_GREEN_SQ53;
                case SCAN_RED:
                    return SCAN_RED_SQ53;
                case SCAN_BLUE:
                    return SCAN_BLUE_SQ53;
                default:
                    return SCAN_BLUE_SQ53;
            }
        } else if(Build.PROJECT.equals("SQ45")) {
            switch(id) {
                case POWER_RED:
                    return POWER_RED_SQ45;
                case POWER_GREEN:
                    return POWER_GREEN_SQ45;
                case SCAN_RED:
                case SCAN_BLUE:
                    return SCAN_BLUE_SQ45;
                default:
                    return SCAN_BLUE_SQ45;
            }
        } else if(Build.PROJECT.equals("SQ51FW")) {
            switch(id) {
                case POWER_RED:
                    return POWER_RED_SQ51FW;
                case POWER_GREEN:
                    return POWER_GREEN_SQ51FW;
                case SCAN_RED:
                    return SCAN_RED_SQ51FW;
                case SCAN_BLUE:
                    return SCAN_BLUE_SQ51FW;
                default:
                    return SCAN_BLUE_SQ51FW;
            }
        } else if(Build.PROJECT.equals("SQ38")) {
            switch(id) {
                case POWER_RED:
                    return POWER_RED_SQ38;
                case POWER_GREEN:
                    return POWER_GREEN_SQ38;
                case SCAN_RED:
                    return SCAN_RED_SQ38;
                case SCAN_BLUE:
                    return SCAN_BLUE_SQ38;
                case EXT_RED:
                    return EXT_RED_SQ38;
                case EXT_GREEN:
                    return EXT_GREEN_SQ38;
                default:
                    return SCAN_BLUE_SQ38;
            }
        }
        return new LedControl("/sys/class/leds/red/brightness", 0, 255);
    }

    public class LedControl {
    	public String PATH;
    	public byte[] ON = {'1'};
    	public byte[] OFF = {'0'};
    	public int Min = 0;
    	public int Max = 0;
    	public LedControl(String path, int min, int max){
    		PATH = path;
            Min = min;
            Max = max;
  		    ON = String.valueOf(Max).getBytes();
            OFF = String.valueOf(Min).getBytes();
    	}
    }

    public void setBrightness(int id, int brightness){
    	this.setBrightness(id, brightness, Brightness.USER);
	}

	public void setBrightness(int id, int brightness, int brightnessMode){
		try {
            sService.setBrightness(id, brightness, brightnessMode);
        } catch (RemoteException e) {
            LOGD("setBrightness RemoteException");
        }
	}

	public void setColor(int id, int color){
		try {
            sService.setColor(id, color);
        } catch (RemoteException e) {
            LOGD("setColor RemoteException");
        }
	}

	public void setFlashing(int id, int color, int mode, int onMS, int offMS){
		try {
            sService.setFlashing(id, color, mode, onMS, offMS);
        } catch (RemoteException e) {
            LOGD("setFlashing RemoteException");
        }
	}

	public void pulse(int id){
		try {
            sService.pulse(id, 0x00ffffff, 1000);
        } catch (RemoteException e) {
            LOGD("pulse RemoteException");
        }
	}

	public void pulse(int id, int color, int onMS){
		try {
            sService.pulse(id, color, onMS);
        } catch (RemoteException e) {
            LOGD("turnOff RemoteException");
        }
	}
	    
	public void turnOff(int id){
		try {
            sService.turnOff(id);
        } catch (RemoteException e) {
            LOGD("turnOff RemoteException");
        }
	}

	public void setVrMode(int id, boolean enabled){
		try {
            sService.setVrMode(id, enabled);
        } catch (RemoteException e) {
            LOGD("setVrMode RemoteException");
        }
	}

	public int getBrightness(int id){
		int brightness = 0;
		try {
            brightness = sService.getBrightness(id);
        } catch (RemoteException e) {
            LOGD("getBrightness RemoteException");
        }
        return brightness;
	}    

    public void LOGD(String msg){
        if(DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
