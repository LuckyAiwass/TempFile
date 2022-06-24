package com.unicair.googlekey;

import android.util.Log;

 
public class GoogleKey {
    private static final String TAG = "GoogleKey";
    private static GoogleKey sInstance = null;

    static {
      System.loadLibrary("googlekey_jni");
    }

    private native int native_test_GoogleKey();

    public static GoogleKey getInstance() {
      if (sInstance == null) {
        sInstance = new GoogleKey();
      }
      return sInstance;
    }
	
    public boolean verifyKey() {
      int ret = native_test_GoogleKey();
	  Log.d(TAG, "verifyKey ret = " + ret);
	  if(ret == 0) //KM_ERROR_OK
	  	return true;
	  else
      	return false;
    }
}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          