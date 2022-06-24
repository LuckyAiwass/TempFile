package com.android.server;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.device.MaxNative;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gouwei on 17-6-28.
 */

public class DigestUtils {
    public static String LOG_TAG = "DigestUtils";
    public static MaxNative mSEManager = new MaxNative();
    public static String byte2hex(byte[] data, int length) {
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < length; i++) {
		String temp = Integer.toHexString(((int) data[i]) & 0xFF);
		for (int t = temp.length(); t < 2; t++) {
			sb.append("0");
		}
		sb.append(temp);
	}
	return sb.toString();
    }
    private static int parse(char c) {
			if (c >= 'a')
				return (c - 'a' + 10) & 0x0f;
			if (c >= 'A')
				return (c - 'A' + 10) & 0x0f;
			return (c - '0') & 0x0f;
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
    public static byte[] HexString2Bytes(String hexstr) {
		byte[] b = new byte[hexstr.length() / 2];
		int j = 0;
		for (int i = 0; i < b.length; i++) {
			char c0 = hexstr.charAt(j++);
			char c1 = hexstr.charAt(j++);
			b[i] = (byte) ((parse(c0) << 4) | parse(c1));
		}
		return b;
    }

    public static byte[] String2HexBytes(String str) {
	return HexString2Bytes(byte2hex(str.getBytes()));
    }
    public static String encryptData(String data){
        byte[] encryptData = new byte[1024];
        int len = mSEManager.pciEncryptForAppDat(HexString2Bytes(data),HexString2Bytes(data).length,encryptData);
        android.util.Log.d(LOG_TAG,"encryptData = " + len);
        if(len < 0) {
            return null;
        }
        String result =  byte2hex(encryptData,len);
        //android.util.Log.d("gouwei","encryptData = " + data);
        //android.util.Log.d("gouwei","encryptData result = " + result);
        return result;

    }
    public static String decryptData(String data){
        byte[] decryptData = new byte[1024];
        int len = mSEManager.pciDecryptForAppDat(HexString2Bytes(data),HexString2Bytes(data).length,decryptData);
        String result =  byte2hex(decryptData,len);
        //android.util.Log.d("gouwei","decryptData = " + data);
        //android.util.Log.d("gouwei","decryptData result = " + result);
        return result;

    }
    public static boolean writeDigest(Context context, String apkPath, String packageName, String digest){
        if(apkPath.startsWith("/system/")){
            File file = new File("/data/data/" + packageName + ".txt");
            FileOutputStream outputStream = null;
                try {
                    if(!file.exists()){
                        file.createNewFile();
                    }
                    outputStream = new FileOutputStream(file);
                    outputStream.write(digest.getBytes());
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    if(outputStream != null){
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;

        }else{
            return Settings.System.putString(context.getContentResolver(),packageName,digest);
        }
    }
    public static String readDigest(Context context, String apkPath, String packageName){
        if(apkPath.startsWith("/system/")){
            if(apkPath.equals("/system/framework/framework-res.apk")){
               return getDigestFromFile(apkPath);
            }
            File file = new File("/data/data/" + packageName + ".txt");
            FileInputStream inputStream = null;
            if(!file.exists()){
                throw new RuntimeException("error not found file for " + packageName);
            }
            try{
                byte[] digestByte = new byte[1024];
                inputStream = new FileInputStream(file);
                int lenth = inputStream.read(digestByte);
                return new String(digestByte,0,lenth);
            }catch (Exception e){
                e.printStackTrace();
                throw new RuntimeException("error not read file for " + packageName);
            }finally {
                if(inputStream != null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }else{

            String digest =  Settings.System.getString(context.getContentResolver(),packageName);
            if(TextUtils.isEmpty(digest)){
                throw new RuntimeException("read null for " + packageName);
            }
            return digest;
        }
    }
    public static String  getSystemAPKDigest(String apkCodePath , String isa){
        if(apkCodePath.startsWith("/system/")){
            if(apkCodePath.equals("/system/framework/framework-res.apk")){
               return getDigestFromFile(apkCodePath);
            }
            String[] paths = apkCodePath.split("/");
            int lenth = paths.length;
            StringBuilder builder = new StringBuilder();
            builder.append("/data/dalvik-cache")
                    .append("/")
                    .append(isa)
                    .append("/");

            for(int i = 1 ; i < lenth ; i++){
                builder.append(paths[i]);
                builder.append("@");
            }
            builder.append("classes.dex");
            return getDigestFromFile(builder.toString());

        }else{
            throw new RuntimeException("error path");
        }
    }

    public static String  getDigest(String apkPath){
        byte[] sum = getDigestFromFilePath(apkPath).getBytes();
        MessageDigest alg;
        try {
            alg = MessageDigest.getInstance("SHA-256");
            alg.update(sum);
            return byte2hex(alg.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String  getDigestFromFilePath(String apkPath){
        StringBuilder builder = new StringBuilder();
        File floder = new File(apkPath);
        if(!floder.isDirectory()){
            throw new RuntimeException("path error");
        }
        List<File> files = Arrays.asList(floder.listFiles());
        Collections.sort(files, new Comparator< File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        for(int i = 0 ; i < files.size() ; i++ ){
            if(files.get(i).isDirectory())
                builder.append(getDigestFromFilePath(files.get(i).getAbsolutePath()));
            else {
				//android.util.Log.d(LOG_TAG,"apk path = " + files.get(i).getAbsolutePath());
                builder.append(getDigestFromFile(files.get(i).getAbsolutePath()));
            }
        }
        return builder.toString();

    }
    public static String  getDigestFromFile(String apkPath){
        MessageDigest alg;
        try {
            alg = MessageDigest.getInstance("SHA-256");
            byte[] apkSrc = new byte[1024*16];
            FileInputStream inputStream = new FileInputStream(apkPath);
            int lenth = inputStream.read(apkSrc);
            while(lenth != -1){
                alg.update(apkSrc,0,lenth);
                lenth = inputStream.read(apkSrc);
            }
            return byte2hex(alg.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
    public static boolean queryTrigger() {
        boolean triggerSec = false;
        if(android.os.SystemProperties.get("urv.se.support", "true").equals("true")) {
            byte[] sestatue = new byte[8];
            MaxNative.open();
            MaxNative.picQueryTriggerSec(sestatue);
            log(String.format("sestatue = 0x%02x%02x%02x%02x", sestatue[0], sestatue[1], sestatue[2], sestatue[3]));
            MaxNative.close();
            if ((sestatue[0] != 0 || sestatue[1] != 0|| sestatue[2] != 0)|| sestatue[3] != 0) {
                triggerSec = true;
            }
        }
        return triggerSec;
    }
    public static String bytes2Hex(byte[] src){
        char[] res = new char[src.length*2];
        final char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        for(int i=0,j=0; i<src.length; i++){
            res[j++] = hexDigits[src[i] >>>4 & 0x0f];
            res[j++] = hexDigits[src[i] & 0x0f];
        }

        return new String(res);
    }
    public static void log(String message){
        android.util.Log.d(LOG_TAG,message);
    }
}
