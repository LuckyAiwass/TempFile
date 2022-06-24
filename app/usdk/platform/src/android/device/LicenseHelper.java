
package android.device;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LicenseHelper {
    private static final String TAG = "DECODERLicenseHelper";
    public static final String Activation_DIR = "sdcard/Decode/";
    static String anchoring_filePath = String.format("%s/anchoring-0", Activation_DIR/*Environment.getExternalStorageDirectory()*/);
    static String storage_filePath = String.format("%s/storage-0", Activation_DIR/*Environment.getExternalStorageDirectory()*/);
    //static String dirFilePath = String.format("%s/decodeEngine/", Environment.getExternalStorageDirectory());
    static final int BLOCK_OFFSET  = 1000 * 1020;
    static final int RW_BUFFER_MAX_LENGTH = 20 * 1024;
    static final int RW_BUFFER_MIN_LENGTH = 1024;
    private static final String deviceResponse_filePath = String.format("%s/deviceResponse.bin", Activation_DIR/*Environment.getExternalStorageDirectory()*/);
    public static boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
    public static boolean setWifiWnable(Context mContext, boolean enable) {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if(enable) {
            if(!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(enable);
                return false;
            }
        } else {
            wifiManager.setWifiEnabled(enable);
        }
        return true;
    }
    public static boolean checkActivationResponseFileExists()  {
        File file = new File(deviceResponse_filePath);
        boolean exists = file.exists();
        if(exists && file.length() < 1024) {
            Log.d(TAG, file.exists() + "Licen checkActivationResponseFileExists length=" + file.length());
            return false;
        }
        return exists;
    }
    public static void clearResponseExistsFiles() {
        try {
            File file = new File(deviceResponse_filePath);
            boolean exists = file.exists();
            if(exists) {
                file.delete();
                file = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean checkFileExists() {
        File file = new File(anchoring_filePath);
        boolean exists = file.exists();
        Log.d(TAG, "checkFileanchoringExists=" + exists);
        if(exists && file.length() < 151) {
            Log.d(TAG, "Licen checkFileanchoringExists length=" + file.length());
            exists = false;
        }
        Log.d(TAG, "Licen checkFileanchoringExists length=" + file.length());
        file = new File(storage_filePath);
        boolean storageExists = file.exists();
        Log.d(TAG, "Licen checkFilestorageExists=" + storageExists);
        if(storageExists && file.length() < 10752) {
            Log.d(TAG, "Licen checkFilestorageExists length=" + file.length());
            storageExists = false;
        }
        Log.d(TAG, "Licen checkFilestorageExists length=" + file.length());
        return exists&&storageExists;
    }
    public static void clearExistsFiles() {
        try {
            File file = new File(anchoring_filePath);
            boolean exists = file.exists();
            if(exists) {
                file.delete();
                file = null;
            }

            file = new File(storage_filePath);
            boolean storageExists = file.exists();
            if(storageExists) {
                file.delete();
                file = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void deleteDir() {
        try {
            File file = new File(Activation_DIR);
            boolean exists = file.exists();
            if(exists) {
                file.delete();
                file = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean syncLicenseStrore() {
        try {
            byte[] anchoringBuffer = new byte[RW_BUFFER_MIN_LENGTH];
            byte[] storageBuffer = new byte[RW_BUFFER_MAX_LENGTH];
            FileInputStream fileStream = new FileInputStream(anchoring_filePath);
            int readAnchoringBytes = fileStream.read(anchoringBuffer, 0, RW_BUFFER_MIN_LENGTH);
            Log.d(TAG, "sync anchoring License length=" + readAnchoringBytes);
            fileStream.close();
            fileStream = new FileInputStream(storage_filePath);
            int readStorageBytes = fileStream.read(storageBuffer, 0, RW_BUFFER_MAX_LENGTH);
            fileStream.close();
            Log.d(TAG, "sync License length=" + readStorageBytes);
            if (readAnchoringBytes > 0 && readStorageBytes > 0) {
                byte[] bufferKey = new byte[RW_BUFFER_MAX_LENGTH];
                int ret = LicenseKeystore.initStore();
                ret = LicenseKeystore.readStore(BLOCK_OFFSET, bufferKey);
                ret = LicenseKeystore.releaseStore();
                byte[] syncanchoringBuffer = null;

                int syncreadAnchoringBytes = ((int)(bufferKey[1] & 0xff)) *256 + ((int)(bufferKey[2] & 0xff));
                Log.d(TAG, "sync Licens syncreadAnchoringBytes=" + syncreadAnchoringBytes);
                int syncreadStorageBytes = ((int)(bufferKey[syncreadAnchoringBytes + 3] & 0xff)) *256 + ((int)(bufferKey[syncreadAnchoringBytes + 4] & 0xff));
                Log.d(TAG, "sync Licens syncreadStorageBytes=" + syncreadStorageBytes);

                if(0 < syncreadStorageBytes && syncreadAnchoringBytes > 0) {
                    int STX = bufferKey[0];
                    int ETX = bufferKey[syncreadAnchoringBytes + syncreadStorageBytes + 3 + 2];
                    if( STX == 0x02 && ETX == 0x03) {
                        syncanchoringBuffer = new byte[syncreadAnchoringBytes];
                        System.arraycopy(bufferKey, 3, syncanchoringBuffer, 0, syncreadAnchoringBytes);
                        String newLicese = bytesToHexString(anchoringBuffer, 0, readAnchoringBytes);
                        String StoreLicese = bytesToHexString(syncanchoringBuffer, 0, syncreadAnchoringBytes);
                        if(!newLicese.equals(StoreLicese)) {
                            Log.d(TAG, "Licens up");
                            syncanchoringBuffer = null;
                        } else {
                            return true;
                        }
                    }
                    Log.d(TAG, "sync Licens STX=" + STX + " ETX = " + ETX);
                }
                if(syncanchoringBuffer == null) {
                    Log.d(TAG, "sync new Licens ");
                    byte[] caKEY = new byte[readAnchoringBytes + readStorageBytes + 6];
                    caKEY[0] = 0x02;
                    caKEY[1] = (byte)((readAnchoringBytes) >> 8);
                    caKEY[2] =(byte)((readAnchoringBytes) & 0xff) ;
                    System.arraycopy(anchoringBuffer, 0, caKEY, 3, readAnchoringBytes);
                    caKEY[readAnchoringBytes + 3] = (byte)((readStorageBytes) >> 8);
                    caKEY[readAnchoringBytes + 4] =(byte)((readStorageBytes) & 0xff) ;
                    System.arraycopy(storageBuffer, 0, caKEY, 3 + readAnchoringBytes + 2, readStorageBytes);
                    caKEY[readAnchoringBytes + readStorageBytes + 3 + 2] = 0x03;
                    ret = LicenseKeystore.initStore();
                    ret = LicenseKeystore.writeStore(BLOCK_OFFSET, caKEY, caKEY.length);
                    LicenseKeystore.releaseStore();
                    return true;
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
        }
        return false;
    }
    public static boolean updateLicenseToCAStrore() {
        boolean result = false;
        byte[] anchoringBuffer = new byte[RW_BUFFER_MIN_LENGTH];
        byte[] storageBuffer = new byte[RW_BUFFER_MAX_LENGTH];
        try {
            FileInputStream fileStream = new FileInputStream(anchoring_filePath);
            int readAnchoringBytes = fileStream.read(anchoringBuffer, 0, RW_BUFFER_MIN_LENGTH);
            Log.d(TAG, "update anchoring License length=" + readAnchoringBytes);
            fileStream.close();
            fileStream = new FileInputStream(storage_filePath);
            int readStorageBytes = fileStream.read(storageBuffer, 0, RW_BUFFER_MAX_LENGTH);
            fileStream.close();
            Log.d(TAG, "update storage License length=" + readStorageBytes);
            if (readAnchoringBytes > 0 && readStorageBytes > 0) {
                byte[] caKEY = new byte[readAnchoringBytes + readStorageBytes + 6];
                caKEY[0] = 0x02;
                caKEY[1] = (byte)((readAnchoringBytes) >> 8);
                caKEY[2] =(byte)((readAnchoringBytes) & 0xff) ;
                System.arraycopy(anchoringBuffer, 0, caKEY, 3, readAnchoringBytes);

                caKEY[readAnchoringBytes + 3] = (byte)((readStorageBytes) >> 8);
                caKEY[readAnchoringBytes + 4] =(byte)((readStorageBytes) & 0xff) ;
                //Log.d(TAG, "update  Licens readAnchoringBytes H " + caKEY[readAnchoringBytes + 3] + " L " +caKEY[readAnchoringBytes + 4]);
                System.arraycopy(storageBuffer, 0, caKEY, 3 + readAnchoringBytes + 2, readStorageBytes);
                caKEY[readAnchoringBytes + readStorageBytes + 3 + 2] = 0x03;
                int ret = LicenseKeystore.initStore();
                ret = LicenseKeystore.writeStore(BLOCK_OFFSET, caKEY, caKEY.length);
                if(ret == 0) {
                    Log.d(TAG, "update  Licens to Store file OK");
                    result = true;
                }
                ret = LicenseKeystore.releaseStore();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
            result = false;
        }
        return result;
    }
    public static boolean updateLicensFromCAStrore() {
        byte[] bufferKey = new byte[RW_BUFFER_MAX_LENGTH];
        int ret = LicenseKeystore.initStore();
        ret = LicenseKeystore.readStore(BLOCK_OFFSET, bufferKey);
        ret = LicenseKeystore.releaseStore();
        int readAnchoringBytes = ((int)(bufferKey[1] & 0xff)) *256 + ((int)(bufferKey[2] & 0xff));
        Log.d(TAG, "update From Licens readAnchoringBytes=" + readAnchoringBytes);
        int readStorageBytes = ((int)(bufferKey[readAnchoringBytes + 3] & 0xff)) *256 + ((int)(bufferKey[readAnchoringBytes + 4] & 0xff));
        Log.d(TAG, "update From Licens readStorageBytes=" + readStorageBytes);

        if(readStorageBytes > 0 && readAnchoringBytes > 0) {
            Log.d(TAG, "update  From Licens to file");
            int STX = bufferKey[0];
            int ETX = bufferKey[readAnchoringBytes + readStorageBytes + 3 + 2];
            if( STX == 0x02 && ETX == 0x03) {
                byte[] anchoringBuffer = new byte[readAnchoringBytes];
                byte[] storageBuffer = new byte[readStorageBytes];

                System.arraycopy(bufferKey, 3, anchoringBuffer, 0, readAnchoringBytes);
                System.arraycopy(bufferKey, 3 + readAnchoringBytes + 2, storageBuffer, 0, readStorageBytes);
                boolean result = writeFile(anchoring_filePath, anchoringBuffer);
                result = writeFile(storage_filePath, storageBuffer);
                Log.d(TAG, "update From Licens OK STX=" + STX + " ETX = " + ETX);
                return result;

            }
            Log.d(TAG, "update From Licens STX=" + STX + " ETX = " + ETX);
        }
        return false;
    }
    public static boolean syncResponseBinStrore() {
        try {
            byte[] storageBuffer = new byte[RW_BUFFER_MAX_LENGTH];
            FileInputStream fileStream = new FileInputStream(deviceResponse_filePath);
            int readStorageBytes = fileStream.read(storageBuffer, 0, RW_BUFFER_MAX_LENGTH);
            fileStream.close();
            Log.d(TAG, "sync bin License length=" + readStorageBytes);
            if (readStorageBytes > 0) {
                byte[] bufferKey = new byte[RW_BUFFER_MAX_LENGTH];
                int ret = LicenseKeystore.initStore();
                ret = LicenseKeystore.readStore(BLOCK_OFFSET, bufferKey);
                ret = LicenseKeystore.releaseStore();
                byte[] syncanchoringBuffer = null;

                int syncreadAnchoringBytes = ((int)(bufferKey[1] & 0xff)) *256 + ((int)(bufferKey[2] & 0xff));
                Log.d(TAG, "sync bin Licens syncreadAnchoringBytes=" + syncreadAnchoringBytes);

                if(syncreadAnchoringBytes > 0) {
                    int SOH = bufferKey[0];
                    int EOT = bufferKey[syncreadAnchoringBytes + 3];
                    if( SOH == 0x01 && EOT == 0x04) {
                        syncanchoringBuffer = new byte[syncreadAnchoringBytes];
                        System.arraycopy(bufferKey, 3, syncanchoringBuffer, 0, syncreadAnchoringBytes);
                        String newLicese = bytesToHexString(storageBuffer, 0, readStorageBytes);
                        String StoreLicese = bytesToHexString(syncanchoringBuffer, 0, syncreadAnchoringBytes);
                        if(!newLicese.equals(StoreLicese)) {
                            Log.d(TAG, "bin Licens up");
                            syncanchoringBuffer = null;
                        } else {
                            return true;
                        }
                    }
                    Log.d(TAG, "sync Licens SOH=" + SOH + " EOT = " + EOT);
                }
                if(syncanchoringBuffer == null) {
                    Log.d(TAG, "sync new bin Licens ");
                    byte[] caKEY = new byte[readStorageBytes + 4];
                    caKEY[0] = 0x01;
                    caKEY[1] = (byte)((readStorageBytes) >> 8);
                    caKEY[2] =(byte)((readStorageBytes) & 0xff) ;
                    System.arraycopy(storageBuffer, 0, caKEY, 3, readStorageBytes);
                    caKEY[readStorageBytes + 3] = 0x04;
                    ret = LicenseKeystore.initStore();
                    ret = LicenseKeystore.writeStore(BLOCK_OFFSET, caKEY, caKEY.length);
                    LicenseKeystore.releaseStore();
                    return true;
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e(TAG, e.getLocalizedMessage());
        }
        return false;
    }
    public static boolean updateResponseBinFromCAStrore() {
        byte[] bufferKey = new byte[RW_BUFFER_MAX_LENGTH];
        int ret = LicenseKeystore.initStore();
        ret = LicenseKeystore.readStore(BLOCK_OFFSET, bufferKey);
        ret = LicenseKeystore.releaseStore();
        int responseBin = ((int)(bufferKey[1] & 0xff)) *256 + ((int)(bufferKey[2] & 0xff));
        Log.d(TAG, "update From Licens readAnchoringBytes=" + responseBin);

        if(responseBin > 0) {
            Log.d(TAG, "update  From Licens to file");
            int SOH = bufferKey[0];
            int EOT = bufferKey[responseBin + 3];
            if( SOH == 0x01 && EOT == 0x04) {
                byte[] responseBinBuffer = new byte[responseBin];

                System.arraycopy(bufferKey, 3, responseBinBuffer, 0, responseBin);
                boolean result = writeFile(deviceResponse_filePath, responseBinBuffer);
                Log.d(TAG, "update From Licens OK SOH=" + SOH + " EOT = " + EOT);
                return result;

            }
            Log.d(TAG, "update From Licens SOH=" + SOH + " EOT = " + EOT);
        }
        return false;
    }
    private static boolean writeFile(String fileName, byte[] data) {
        boolean ret = false;
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File fileDir = new File(Activation_DIR);
            /*if(!fileDir.exists())
                fileDir.mkdirs();*/
            fileDir.mkdirs();
            file = new File(fileName);
            if(file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(data);
            bos.flush();
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return ret;
    }
    public static String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = offset; i < length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}