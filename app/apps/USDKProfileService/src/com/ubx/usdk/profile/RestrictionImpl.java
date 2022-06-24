package com.ubx.usdk.profile;

import java.io.File;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.FileUtils;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.device.admin.SettingProperty;

import com.ubx.usdk.profile.aidl.IRestrictionPolicy;

import android.os.IScanService;
import android.os.ServiceManager;

/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class RestrictionImpl extends IRestrictionPolicy.Stub {
	private final static String TAG = RestrictionImpl.class.getSimpleName();
	
	private Context mContext;
	private ContentResolver mSystemCR; 
	private UserManager mUserManager;
	
	public RestrictionImpl(Context context) {
		mContext = context;
		mSystemCR = mContext.getContentResolver();
		mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
                // UMS sdk version
                android.os.SystemProperties.set("persist.sys.ums.sdkversion","V1.0.0");
	}
	
    @Override
    public boolean setSettingProperty(String keyName, String value) throws RemoteException {
        if (TextUtils.isEmpty(keyName) || value == null) {
            return false;
        }
        String[] tableProperty = keyName.split("-");
        //persist.sys.locale
        if (tableProperty.length < 2) {
            return false;
        }
        long id = Binder.clearCallingIdentity();
        boolean result = false;
        try {
            if (tableProperty[0].equals("Secure")) {
                Settings.Secure.putString(mSystemCR, tableProperty[1], value);
                result = true;
            } else if (tableProperty[0].equals("Global")) {
                Settings.Global.putString(mSystemCR, tableProperty[1], value);
                result = true;
            } else if (tableProperty[0].equals("System")) {
                Settings.System.putString(mSystemCR, tableProperty[1], value);
                result = true;
            } else if (tableProperty[0].equals("Udroid")) {
                android.device.provider.Settings.System.putString(mSystemCR, tableProperty[1], value);
                result = true;
            } else if (tableProperty[0].equals("persist")) {
                android.os.SystemProperties.set(tableProperty[1], value);
                result = true;
            } else if (tableProperty[0].equals("File")) {
                FileUtils.stringToFile(tableProperty[1], value);
                result = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "setSettingProperty ", e);
        }
        return result;
    }

    @Override
    public String getSettingProperty(String keyName) throws RemoteException {
        if (TextUtils.isEmpty(keyName)) {
            return null;
        }
        String[] tableProperty = keyName.split("-");
        //persist.sys.locale
        /*if (tableProperty.length < 2) {
            return null;
        }*/
        String result = null;

        try {
            if (tableProperty[0].equals("Secure")) {
                result = Settings.Secure.getString(mSystemCR, tableProperty[1]);
            } else if (tableProperty[0].equals("Global")) {
                result = Settings.Global.getString(mSystemCR, tableProperty[1]);
            } else if (tableProperty[0].equals("System")) {
                result = Settings.System.getString(mSystemCR, tableProperty[1]);
            } else if (tableProperty[0].equals("Udroid")) {
                result = android.device.provider.Settings.System.getString(mSystemCR, tableProperty[1]);
            } else if (tableProperty[0].equals("persist")) {
                result = android.os.SystemProperties.get(tableProperty[1], "");
            } else if (tableProperty[0].equals("File")) {
                result = FileUtils.readTextFile(new File(tableProperty[1]), 0, null);
            } else {
                result = android.os.SystemProperties.get(keyName, "");
            }
        } catch (Exception e) {
            Log.e(TAG, "getSettingProperty ", e);
        }

        return result;
    }

    @Override
    public int getRestrictionPolicy(int faction) throws RemoteException {
        return SystemProperties.getInt(SettingProperty.ALLOW_PROPS[faction], 0);
    }

    @Override
    public int setRestrictionPolicy(int faction, int status) throws RemoteException {
            UsbManager usbManager = mContext.getSystemService(UsbManager.class);
            SystemProperties.set(SettingProperty.ALLOW_PROPS[faction], "0");
            switch (faction) {
                case SettingProperty.ALLOW_NONE:
                    break;
                case SettingProperty.ALLOW_BLUETOOTH:
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (status == 0) {
                        mUserManager.setUserRestriction(UserManager.DISALLOW_BLUETOOTH, false);
                        //bluetoothAdapter.enable();
                    } else {
                        //bluetoothAdapter.disable();
                        mUserManager.setUserRestriction(UserManager.DISALLOW_BLUETOOTH, true);
                    }
                    break;
                case SettingProperty.ALLOW_WIFI:
                    WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(status == 0 ? true : false);
                    break;
                case SettingProperty.ALLOW_SCANNER:
                    IScanService mScanService = IScanService.Stub.asInterface(ServiceManager.getService(Context.SCAN_SERVICE));
                    try {
                        if (status == 0) {
                            mScanService.open();
                        } else {
                            mScanService.close();
                        }
                    } catch (Exception e) {
                    }

                    break;
                case SettingProperty.ALLOW_GPS:
                    if (status == 0) {
                        mUserManager.setUserRestriction(UserManager.DISALLOW_CONFIG_LOCATION, false);
                        Settings.Secure.setLocationProviderEnabled(mSystemCR, "network", true);
                        Settings.Secure.setLocationProviderEnabled(mSystemCR, "gps", true);
                        Settings.Global.putInt(mSystemCR, Settings.Global.ASSISTED_GPS_ENABLED, 1);
                        Settings.Secure.putInt(mSystemCR, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
                    } else {
                        Settings.Secure.setLocationProviderEnabled(mSystemCR, "network", false);
                        Settings.Secure.setLocationProviderEnabled(mSystemCR, "gps", false);
                        Settings.Global.putInt(mSystemCR, Settings.Global.ASSISTED_GPS_ENABLED, 0);
                        Settings.Secure.putInt(mSystemCR, Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
                        mUserManager.setUserRestriction(UserManager.DISALLOW_CONFIG_LOCATION, true);
                    }
                    break;
                case SettingProperty.ALLOW_WWAN:
                    TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                    SubscriptionManager subscriptionManager = SubscriptionManager.from(mContext);
                    telephonyManager.setDataEnabled(0, status == 0 ? true : false);
                    for(SubscriptionInfo info:subscriptionManager.getAllSubscriptionInfoList()){
                        telephonyManager.setDataEnabled(info.getSubscriptionId(), status == 0 ? true : false);
                        Log.d(TAG, "setDataEnabled:"+info.getSubscriptionId() + "   " + status);
                    }

                    if (status == 0) {
                        Settings.Global.putInt(mSystemCR, Settings.Global.MOBILE_DATA+"0", 1);
                        Settings.Global.putInt(mSystemCR, Settings.Global.MOBILE_DATA+"1", 1);
                        Settings.Global.putInt(mSystemCR, Settings.Global.MOBILE_DATA+"2", 1);
                    } else {
                        Settings.Global.putInt(mSystemCR, Settings.Global.MOBILE_DATA+"0", 0);
                        Settings.Global.putInt(mSystemCR, Settings.Global.MOBILE_DATA+"1", 0);
                        Settings.Global.putInt(mSystemCR, Settings.Global.MOBILE_DATA+"2", 0);
                    }
                    break;
                case SettingProperty.ALLOW_EDITAPN:
                    mUserManager.setUserRestriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, status == 0 ? false : true);
                    break;
                case SettingProperty.ALLOW_SDCARD:
                    if(status == 0) {
                        usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, false);
                    } else {
                        usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_NONE, false);
                    }
                    break;
                case SettingProperty.ALLOW_MTP:
                    if(status == 0) {
                        usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP, false);
                    } else {
                        usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_NONE, false);
                    }
                    break;
                case SettingProperty.ALLOW_PTP:
                    if(status == 0) {
                        usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP, false);
                    } else {
                        usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_NONE, false);
                    }
                    break;
                case SettingProperty.ALLOW_MASSSTORAGE:

                    break;
                case SettingProperty.ALLOW_EDITVPN:
                    mUserManager.setUserRestriction(UserManager.DISALLOW_CONFIG_VPN, status == 0 ? false : true);
                    break;
                case SettingProperty.ALLOW_HOTSPOT:
                    ConnectivityManager mConnectivityManager= (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (status == 0) {
                        /*
                        mConnectivityManager.startTethering(ConnectivityManager.TETHERING_WIFI, false, new ConnectivityManager.OnStartTetheringCallback() {
                            @Override
                            public void onTetheringStarted() {
                            }

                            @Override
                            public void onTetheringFailed() {
                            }
                        });
                        */
                    } else {
                        mConnectivityManager.stopTethering(ConnectivityManager.TETHERING_WIFI);
                    }

                    break;
                case SettingProperty.ALLOW_TOUCH:

                    break;
                case SettingProperty.ALLOW_CAMERA:

                    break;
                case SettingProperty.ALLOW_KEYBOARD:
//                    mDisallowedKeyboard = status == 0 ? false : true;
                    break;
                case SettingProperty.ALLOW_IME:

                    break;
                case SettingProperty.ALLOW_TORCH:

                    break;
                case SettingProperty.ALLOW_ADB:
                    if(status == 0) {
                        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 1);
                        usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_NONE, false);
                    } else {
                        Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0);
                        usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_NONE, false);
                    }
                    break;
                case SettingProperty.ALLOW_RECOVERY:
                    mUserManager.setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, status == 0 ? false : true);
                    break;
            }
            SystemProperties.set(SettingProperty.ALLOW_PROPS[faction], String.valueOf(status));

        //}
        return 0;
    }

    @Override
    public void setUserRestriction(String key, boolean value) throws RemoteException {
        mUserManager.setUserRestriction(key, value);
    }

    @Override
    public boolean hasUserRestriction(String restrictionKey) throws RemoteException {
        boolean b = false;
        b = mUserManager.hasUserRestriction(restrictionKey);
        return b;
    }
}
