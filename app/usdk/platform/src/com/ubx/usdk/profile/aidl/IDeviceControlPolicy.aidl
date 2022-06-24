// IDeviceControlPolicy.aidl
package com.ubx.usdk.profile.aidl;

import android.os.Bundle;
import android.graphics.Bitmap;

// Declare any non-default types here with import statements

interface IDeviceControlPolicy {

        String getDeviceId();
        void wipeData(int flags);
        void shutdown(boolean reboot);
        Bundle getBatteryInfo();
	
        boolean copyFile(String srcFile, String destFile);
        boolean writeListStringToFile(in List<String> list, String filePath);
        List<String> readListStringFromFile(String filePath);

        void setWallpaper(in Bitmap bitmap, int which);
        int changeUsbMode(int status);
        int getUsbMode();

        void setCurrentTime(long when);

        String getFlashId(int type);
        void writeDatatoFlash(inout byte[] data,int offset);
        void readDatatoFlash(inout byte[] data,int offset,int len);
        String getImei(int slotId);
}
