package com.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Tianchaoxiong on 2017/1/16.
 */

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 初始化监测模型路径
     *
     * @return
     */
    public static boolean makeModelDirExist() {
        //1:检测路径不存在创建  初始化路径
        //2:路径存在检测文件 文件不存在立刻创建
        if (FileUtils.makeFolder("FaceDetection")
                && FileUtils.makeFolder("FaceDetection/useso")
                && FileUtils.makeFolder("FaceDetection/useso/Model")) {
            return true;
        } else return false;
    }

    public static boolean makeFolder(String folder){
        File filefolder = new File(Environment.getExternalStorageDirectory().toString() + "/" + folder);
        if(!filefolder.exists()){
            filefolder.mkdir();
            if(filefolder.exists()){
                Log.d(TAG,folder+"创建成功");
            }
            else {
                Log.d(TAG,folder+"创建失败");
            }

        }
        return true;
    }
    public static void Assets2Sd(Context context, String fileAssetPath, String fileSdPath){
        //测试把文件直接复制到sd卡中 fileSdPath完整路径
        File file = new File(fileSdPath);
        if (!file.exists()) {
            Log.d(TAG,"************文件不存在,文件创建");
            try {
                copyBigDataToSD(context, fileAssetPath, fileSdPath);
                Log.d(TAG, "************拷贝成功");
            } catch (IOException e) {
                Log.d(TAG, "************拷贝失败");
                e.printStackTrace();
            }
        } else {
            Log.d(TAG,"************文件夹存在,文件存在");
        }

    }
    public static void copyBigDataToSD(Context context, String fileAssetPath, String strOutFileName) throws IOException
    {
        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(strOutFileName);
        myInput = context.getAssets().open(fileAssetPath);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while(length > 0)
        {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }

    /**
     * 保存一个bitmap 到执行的目录
     * @param b
     */
    public static void saveBitmap(Bitmap b){
        String FolderPath = "/FaceDetection/useso/Pictures/result";
        String  path = Environment.getExternalStorageDirectory()+FolderPath;
        if (FileUtils.makeFolder("FaceDetection")
                && FileUtils.makeFolder("FaceDetection/useso")
                && FileUtils.makeFolder("FaceDetection/useso/Pictures")
                && FileUtils.makeFolder("FaceDetection/useso/Pictures/result" )) {
            long dataTake = System.currentTimeMillis();
            String jpegName = path + "/" + dataTake +".jpg";
            try {
                FileOutputStream fout = new FileOutputStream(jpegName);
                BufferedOutputStream bos = new BufferedOutputStream(fout);
                b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                Log.i(TAG, "saveBitmap成功");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.i(TAG, "saveBitmap:失败");
                e.printStackTrace();
            }
        }
    }
    /**
     * 保存一个bitmap 到执行的目录
     * @param b
     */
    public static boolean savePahtBitmap(Bitmap b,String Name){
        String FolderPath = "/FaceDetection/useso/Pictures/FINAL";
        String  path = Environment.getExternalStorageDirectory()+FolderPath;
        if (FileUtils.makeFolder("FaceDetection")
                && FileUtils.makeFolder("FaceDetection/useso")
                && FileUtils.makeFolder("FaceDetection/useso/Pictures")
                && FileUtils.makeFolder("FaceDetection/useso/Pictures/FINAL" )) {
            long dataTake = System.currentTimeMillis();
            String jpegName = path + "/" +"match"+Name +".jpg";
            try {
                FileOutputStream fout = new FileOutputStream(jpegName);
                BufferedOutputStream bos = new BufferedOutputStream(fout);
                b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                Log.i(TAG, "Name********   "+Name+"saveBitmap成功");
                return true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.i(TAG,"Name********   "+Name+ "saveBitmap:失败");
                e.printStackTrace();
                return false;
            }
        }else{
            return false;
        }
    }
    /**
     * 保存一个bitmap 到执行的目录
     * String FolderPath = "/FaceDetection/useso/Pictures/Tmp/tmp.png";
     * @param b
     */
    public static void savePaiZhaoBitmap(Bitmap b){
//        /FaceDetection/useso/Pictures/Tmp/tmp.png
        String FolderPath = "/FaceDetection/useso/Pictures/Tmp";
        String  path = Environment.getExternalStorageDirectory()+FolderPath;
        if (FileUtils.makeFolder("FaceDetection")
                && FileUtils.makeFolder("FaceDetection/useso")
                && FileUtils.makeFolder("FaceDetection/useso/Pictures")
                && FileUtils.makeFolder("FaceDetection/useso/Pictures/Tmp" )) {
            String jpegName = path + "/" + "tmp"+".png";
            try {
                FileOutputStream fout = new FileOutputStream(jpegName);
                BufferedOutputStream bos = new BufferedOutputStream(fout);
                b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                Log.i(TAG, "PaiZhao  saveBitmap成功");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.i(TAG, "PaiZhao  saveBitmap:失败");
                e.printStackTrace();
            }
        }
    }
    /**
     * 保存一个bitmap 到执行的目录
     * @param b
     */
    public static void saveSmallBitmap(Bitmap b){
        String FolderPath = "/FaceDetection/useso/Pictures/Smallresult";
        String  path = Environment.getExternalStorageDirectory()+FolderPath;
        if (FileUtils.makeFolder("FaceDetection")
                && FileUtils.makeFolder("FaceDetection/useso")
                && FileUtils.makeFolder("FaceDetection/useso/Pictures")
                && FileUtils.makeFolder("FaceDetection/useso/Pictures/Smallresult" )) {
            long dataTake = System.currentTimeMillis();
            String jpegName = path + "/" + dataTake +".jpg";
            try {
                FileOutputStream fout = new FileOutputStream(jpegName);
                BufferedOutputStream bos = new BufferedOutputStream(fout);
                b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                Log.i(TAG, "Smallresult成功");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.i(TAG, "Smallresult失败");
                e.printStackTrace();
            }
        }
    }
    /**
     * 保存一个暂时的bitmap图片
     *
     * 保存目录在 “/FaceDetection/useso/Pictures/Tmp”
     * @param b
     */
    public static String saveTmpBitmap(Bitmap b){
        String result= "";
        String FolderPath = "/FaceDetection/useso/Pictures/Tmp";
        String  path = Environment.getExternalStorageDirectory()+FolderPath;
        if (FileUtils.makeFolder("FaceDetection")
                && FileUtils.makeFolder("FaceDetection/useso")
                && FileUtils.makeFolder("FaceDetection/useso/Pictures")
                && FileUtils.makeFolder("FaceDetection/useso/Pictures/Tmp" )) {
            //目录创建成功复制文件
            long dataTake = System.currentTimeMillis();
            String jpegName = path + "/" + dataTake +".jpg";
            Log.d("FileUtils",jpegName);
            result = jpegName;
            try {
                FileOutputStream fout = new FileOutputStream(jpegName);
                BufferedOutputStream bos = new BufferedOutputStream(fout);
                b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                bos.flush();
                bos.close();
                Log.i(TAG, "暂存的 saveBitmap成功");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.i(TAG, "暂存的saveBitmap失败");
                e.printStackTrace();
            }
        }
        return result;
    }

    public static Bitmap decodeBitmap(String path){
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            bitmap = BitmapFactory.decodeStream(fis);
            Log.d("FileUtils","图片解析成功");
            return bitmap;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("FileUtils","图片解析失败");
            return null;
        }
    }
}
