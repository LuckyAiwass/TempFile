package com.ubx.usdk.profile;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.device.SettingsUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;
import android.os.BatteryManagerInternal;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.IPowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;

import com.android.server.LocalServices;
import com.ubx.usdk.profile.aidl.IDeviceControlPolicy;

import java.io.File;
import java.util.List;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.BatteryManager;
import java.io.IOException;
import android.telephony.TelephonyManager;
/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class DeviceControlImpl extends IDeviceControlPolicy.Stub {

    private Context mContext;
    private Bundle mBundle;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())){
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isBatteryCharging  =  (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
                int batteryLevel = intent.getIntExtra("level", 0);
                mBundle.putInt("plugged", isBatteryCharging ? 1 : 0);
                mBundle.putInt("level", batteryLevel);
            }
        }
    };

    public DeviceControlImpl(Context context) {
        mContext = context;
        IntentFilter filter = new IntentFilter();
        mBundle = new Bundle();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(receiver, filter);
    }

    @Override
    public String getDeviceId() throws RemoteException {
        return SystemProperties.get("persist.sys.product.serialno", SystemProperties.get("ro.serialno", Build.SERIAL));
    }

    @Override
    public void wipeData(int flags) throws RemoteException {
        Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);
        intent.setFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND);
        intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, (flags == 1) ? true : false);
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    @Override
    public void shutdown(boolean reboot) throws RemoteException {
        IPowerManager mPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
        try {
            if(reboot) {
                mPowerManager.reboot(false, "reboot", true);
            } else {
                mPowerManager.shutdown(false, "shutdown", true);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Bundle getBatteryInfo() throws RemoteException {
        return mBundle;
    }

    @Override
    public boolean copyFile(String srcFile, String destFile) throws RemoteException {
        return FileUtils.copyFile(new File(srcFile), new File(destFile));
    }

    @Override
    public boolean writeListStringToFile(List<String> list, String filePath) throws RemoteException {
        return SettingsUtils.writeListStringToFile(list, filePath);
    }

    @Override
    public List<String> readListStringFromFile(String filePath) throws RemoteException {
        return SettingsUtils.readStringListFormFile(filePath);
    }

    @Override
    public void setWallpaper(Bitmap bitmap, int which) throws RemoteException {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
        try{
            if(bitmap == null) {
                if(which == WallpaperManager.FLAG_SYSTEM) {
                    bitmap = BitmapFactory.decodeFile("/customize/res/wallpaper_system.jpg");
                    if(bitmap == null) wallpaperManager.clearWallpaper(which, UserHandle.USER_SYSTEM);
                } else if(which == WallpaperManager.FLAG_LOCK) {
                    bitmap = BitmapFactory.decodeFile("/customize/res/wallpaper_lock.jpg");
                    if(bitmap == null) wallpaperManager.clearWallpaper(which, UserHandle.USER_SYSTEM);
                }
            }
            if(bitmap != null)
                wallpaperManager.setBitmap(bitmap, null, true, which, UserHandle.USER_SYSTEM);
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public int changeUsbMode(int status) throws RemoteException {
        UsbManager usbManager = mContext.getSystemService(UsbManager.class);
       if(status == 1){
           SystemProperties.set("persist.sys.usb.config","mtp");
           SystemProperties.set("persist.sys.usb.config.udroid","mtp");
           usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MTP,false);
       } else if(status == 0){
           SystemProperties.set("persist.sys.usb.config","none");
           SystemProperties.set("persist.sys.usb.config.udroid","none");
           usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_NONE,false);
       } else if(status == 2){
           SystemProperties.set("persist.sys.usb.config","ptp");
           SystemProperties.set("persist.sys.usb.config.udroid","ptp");
           usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_PTP,false);
       } else if(status == 3){
           SystemProperties.set("persist.sys.usb.config","midi");
           SystemProperties.set("persist.sys.usb.config.udroid","midi");
           usbManager.setCurrentFunction(UsbManager.USB_FUNCTION_MIDI,false);
       } else {
           return -1;
       }
        return 0;
    }

    @Override
    public int getUsbMode() throws RemoteException {
        if (SystemProperties.get("sys.usb.config").contains("mtp")) {
            return 1;
       } else if (SystemProperties.get("sys.usb.config").contains("ptp")) {
            return 2;
       } else if (SystemProperties.get("sys.usb.config").contains("mipi")) {
            return 3;
       } else {
            return 0;
       }
    }

    @Override
    public void setCurrentTime(long millis) throws RemoteException {
        android.os.SystemClock.setCurrentTimeMillis(millis);
    }

    @Override
    public String getFlashId(int type) throws RemoteException {
        String flashid = "";
        File file = new File("/sys/block/mmcblk0/device/cid");
        if (!file.exists()) {
            return flashid;
        }
        try {
            flashid = FileUtils.readTextFile(file, 0, "");
        } catch (IOException e) {

        }
        return flashid;
    }

    @Override
    public void writeDatatoFlash(byte[] data,int offset) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(data == null) {
                    byte[] data = new byte[]{0x00,0x00,0x00,0x00};
                    int ret = android.device.UFSMaster.WriteRsvData(data, 0, 4);
                } else {
                    int ret = android.device.UFSMaster.WriteRsvData(data, offset, data.length);
                }
            }
        }).start();
    }

    @Override
    public void readDatatoFlash(byte[] data,int offset,int len) {
        try{
            android.device.UFSMaster.ReadRsvData(data, offset, len);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private String bytesToHexString(byte[] bytes) {
        if (bytes == null) return null;

        StringBuilder ret = new StringBuilder(2*bytes.length);

        for (int i = 0 ; i < bytes.length ; i++) {
            int b;

            b = 0x0f & (bytes[i] >> 4);

            ret.append("0123456789abcdef".charAt(b));

            b = 0x0f & bytes[i];

            ret.append("0123456789abcdef".charAt(b));
        }

        return ret.toString();
    }

    @Override
    public String getImei(int slotId) throws RemoteException {
        TelephonyManager telephonyManager = mContext.getSystemService(TelephonyManager.class);
        if(slotId == 1){
            return telephonyManager.getImei(0);
        }else if( slotId == 2) {
            return telephonyManager.getImei(1);
        }
        return "";
    }

}
