package com.unicair.nv;

import android.util.Log;

 
public class NvWriter {
    private static final String TAG = "NvWriter";
    private static NvWriter sInstance = null;

    /*FLAG RESULT*/
    public static final char PASS = 'P';
    public static final char FAIL = 'F';
    public static final char NA = ' ';

    static {
      System.loadLibrary("nvwriter_jni");
    }
	/*
		guoyongcan 20180404
		flag: 0  ---read nv 
			  1 --- write nv with value
	*/
    private native String native_readflag_NV(int nv, int flag, int value);
   // private native void native_writeflag_NV(int nvId, int index,char newValue);// 把我们要操作的某一位重新赋值

    public static NvWriter getInstance() {
      if (sInstance == null) {
        sInstance = new NvWriter();
      }
      return sInstance;
    }
	 /* Convert byte[] to hex string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。   
 * @param src byte[] data   
 * @return hex string   
 */      
	public static String bytesToHexString(byte[] src){   
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
	Log.i(TAG, "--yongcan--bytesToHexString():" + stringBuilder.toString());

    return stringBuilder.toString();   
}   

    public String readFlagNV(int nv) {
		Log.i(TAG,"readFlagNV: nv  = " + nv);
      String mFlagNv = native_readflag_NV(nv,0,0);
      Log.i(TAG,"readFlagNV: mFlagNv = " + mFlagNv);
      return mFlagNv;
    }

    public void writeFlagNV(int nv,int newValue) {
     // native_writeflag_NV(nvId,index,newValue);
      String mFlagNv = native_readflag_NV(nv,1,newValue);
    }

    public char getFlag(int nvId, int index) {
      String mFlagNv = readFlagNV(nvId);
      char flag = NA;
      if (mFlagNv != null && mFlagNv.length() >= index) {
        flag = mFlagNv.charAt(index);
      }
      Log.i(TAG,index + ": flag = " + flag);
      return flag;
    }
}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             