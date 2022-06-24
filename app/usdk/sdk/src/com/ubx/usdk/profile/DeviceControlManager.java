package com.ubx.usdk.profile;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.ubx.usdk.LogUtil;
import com.ubx.usdk.profile.aidl.IDeviceControlPolicy;

import java.util.List;

/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class DeviceControlManager {
    private ProfileManager mProfileManager;
    private IDeviceControlPolicy mIDeviceControlPolicy;

    protected DeviceControlManager(ProfileManager profileManager) {
        this.mProfileManager = profileManager;
    }

    /**
     * @hide
     */
    protected void init() {
        if (mIDeviceControlPolicy == null && mProfileManager != null && mProfileManager.getIProfileManager() != null) {
            try {
                IBinder binder = mProfileManager.getIProfileManager().getDeviceControlIBinder();
                if (binder != null) {
                    mIDeviceControlPolicy = IDeviceControlPolicy.Stub.asInterface(binder);
                }
            } catch (RemoteException e) {
                LogUtil.e("NetworkManager::init", e);
            }
        }
    }

    /**
     * @hide
     */
    protected void release() {
        mIDeviceControlPolicy = null;
    }

    public String getDeviceId() {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getDeviceId();
            } catch (RemoteException e) {
                LogUtil.e("getDeviceId", e);
            }
        }
        return Build.SERIAL;
    }

    public void wipeData(int flags) {
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.wipeData(flags);
            } catch (RemoteException e) {
                LogUtil.e("wipeData", e);
            }
        }

    }

    public void shutdown(boolean reboot) {
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.shutdown(reboot);
            } catch (RemoteException e) {
                LogUtil.e("shutdown", e);
            }
        }
    }

    public Bundle getBatteryInfo() {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getBatteryInfo();
            } catch (RemoteException e) {
                LogUtil.e("getBatteryInfo", e);
            }
        }
        return null;
    }

    public boolean copyFile(String srcFile, String destFile) {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.copyFile(srcFile, destFile);
            } catch (RemoteException e) {
                LogUtil.e("copyFile", e);
            }
        }
        return false;
    }

    public boolean writeListStringToFile(List<String> list, String filePath) {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.writeListStringToFile(list, filePath);
            } catch (RemoteException e) {
                LogUtil.e("writeListStringToFile", e);
            }
        }
        return false;
    }

    public List<String> readListStringFromFile(String filePath) {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.readListStringFromFile(filePath);
            } catch (RemoteException e) {
                LogUtil.e("readListStringFromFile", e);
            }
        }
        return null;
    }

    public void setWallpaper(Bitmap bitmap, int which) {
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.setWallpaper(bitmap, which);
            } catch (RemoteException e) {
                LogUtil.e("setWallpaper", e);
            }
        }
    }

    public int changeUsbMode(int status) {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.changeUsbMode(status);
            } catch (RemoteException e) {
                LogUtil.e("changeUsbMode", e);
            }
        }
        return 0;
    }

    public int getUsbMode() {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getUsbMode();
            } catch (RemoteException e) {
                LogUtil.e("getUsbMode", e);
            }
        }
        return 0;
    }

    public void setCurrentTime(long when) {
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.setCurrentTime(when);
            } catch (RemoteException e) {
                LogUtil.e("setCurrentTime", e);
            }
        }
    }

    public String getRamId() {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getFlashId(1);
            } catch (RemoteException e) {
                LogUtil.e("getRamId", e);
            }
        }
        return "";
    }

    public String getRomId() {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getFlashId(2);
            } catch (RemoteException e) {
                LogUtil.e("getRomId", e);
            }
        }
        return "";
    }

    public String getFlashId() {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getFlashId(3);
            } catch (RemoteException e) {
                LogUtil.e("getFlashId", e);
            }
        }
        return "";
    }

    public void writeDatatoFlash(byte[] data,int offset) {
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.writeDatatoFlash(data,offset);
            } catch (RemoteException e) {
                LogUtil.e("writeDatatoFlash", e);
            }
        }
    }

    public void readDatatoFlash(byte[] data,int offset,int len) {
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.readDatatoFlash(data,offset,len);
            } catch (RemoteException e) {
                LogUtil.e("readDatatoFlash", e);
            }
        }
    }
    public String getImei(int slotId) {
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getImei(slotId);
            } catch (RemoteException e) {
                LogUtil.e("getImei", e);
            }
        }
        return "";
    }
}
