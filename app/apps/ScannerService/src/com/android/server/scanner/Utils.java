package com.android.server.scanner;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.android.usettings.R;
/**
 * Created by rocky on 17-5-8.
 */

public class Utils {
    public static File getAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
       /* File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), albumName);*/
        //File file = new File(Environment.getExternalStorageDirectory(), albumName);

        File file = new File(Environment.getExternalStorageDirectory(), albumName);
        if (!file.mkdirs()) {
            Log.e("CameraConfiguration", "Directory not created");
        }
        return file;
    }
	public static void saveImageAsPNG(Bitmap signature, String dirName, String fileName) {
        try {
            File fileDir = new File(dirName);
            if(!fileDir.exists())
                fileDir.mkdirs();
            File photo = new File(String.format("%s/%s.png", dirName, fileName));
            if (photo.exists()) {
                photo.delete();
            }
            photo.createNewFile();
            OutputStream stream = new FileOutputStream(photo);
            signature.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CameraConfiguration", "Directory IOException");
        }
    }

    public static void saveImageAsJPG(Bitmap signature, String dirName, String fileName) {
        try {
            File fileDir = new File(dirName);
            if(!fileDir.exists())
                fileDir.mkdirs();
            File photo = new File(String.format("%s/%s.jpg", dirName, fileName));
            if (photo.exists()) {
                photo.delete();
            }
            photo.createNewFile();
            OutputStream stream = new FileOutputStream(photo);
            signature.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CameraConfiguration", "Directory IOException");
        }
    }

    public static String addSignatureToGallery(Bitmap signature, long time) {
        String result = null;
        try {
            File photo = new File(getAlbumStorageDir("Decode"), String.format("decode_W%d_H%d_%d.jpg", signature.getWidth(), signature.getHeight(), time));
            OutputStream stream = new FileOutputStream(photo);
            signature.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.close();
            result = photo.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("CameraConfiguration", "Directory IOException");
            result = null;
        }
        return result;
    }
    public static byte[] readFileHead(InputStream paramInputStream) {
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
        byte[] arrayOfByte = new byte[2048];
        try {
            int size = 0;
            while ((size = paramInputStream.read(arrayOfByte, 0, arrayOfByte.length)) != -1) {
                localByteArrayOutputStream.write(arrayOfByte, 0, size);
            }
            localByteArrayOutputStream.flush();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return localByteArrayOutputStream.toByteArray();
    }
    private static File mkdirSaveDir() {
        //File file = new File(Environment.getExternalStorageDirectory(), "Decode");
        File file = new File("sdcard/decodeImg");
        if (!file.mkdirs()) {
            Log.e("CameraConfiguration", "Directory not created");
        }
        return file;
    }
    public static void saveYUVImage(final Context paramContext, final byte[] paramArrayOfByte, final int width, final int heigth, final long time) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = String.format("decode_W%d_H%d_%d.bmp", width, heigth, time);
                try {
                    File dir = mkdirSaveDir();
                    if ((dir == null) || (TextUtils.isEmpty(dir.getAbsolutePath()))) {
                        Log.e("CameraConfiguration", "Directory isEmpty");
                        return;
                    }
                    int imgRawId = -1;
                    if ((width == 1280) && (heigth == 800)) {
                        imgRawId = R.raw.bh_1280_800;
                    } else if ((width == 640) && (heigth == 480)) {
                        imgRawId = R.raw.bh_640_480;
                    } else if ((width == 1280) && (heigth == 720)) {
                        imgRawId = R.raw.bh_1280_720;
                    } else {
                        Log.e("CameraConfiguration", String.format("No bmp head template found for preview data[%dX%d]", new Object[] { Integer.valueOf(width), Integer.valueOf(heigth) }));
                    }
                    if(imgRawId != -1) {
                        InputStream localInputStream = paramContext.getResources().openRawResource(imgRawId);
                        //YuvImage localYuvImage = new YuvImage(paramArrayOfByte, 17, width, heigth, null);
                        if(localInputStream != null) {
                            byte[] headOfByte = readFileHead(localInputStream);
                            if ((headOfByte != null)){
                                File photo = new File(dir, fileName);
                                FileOutputStream localFileOutputStream = new FileOutputStream(photo);
                                localFileOutputStream.write(headOfByte);
                                localFileOutputStream.write(paramArrayOfByte, 0, width * heigth);
                                localFileOutputStream.flush();
                                localFileOutputStream.close();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    public static String byte2hex(byte[] data) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            String temp = Integer.toHexString(((int) data[i]) & 0xFF);
            for (int t = temp.length(); t < 2; t++) {
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }
    public static void appendLog(int width, int height, byte[] src) {
        FileWriter writer = null;
        Date date = new Date();
        int size = src.length;
        try {
            String content = byte2hex(src);
            writer = new FileWriter("/sdcard/"+ size +"_" + width + "X" + height +".txt", true);
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write(content);
            bw.flush();
            bw.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
