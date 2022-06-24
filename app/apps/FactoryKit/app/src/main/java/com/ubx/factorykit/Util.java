package com.ubx.factorykit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;

import com.qualcomm.qcrilhook.QcRilHookCallback;
import com.qualcomm.qcrilhook.QcRilHook;

public class Util {
	public static int[] Status = new int[20];
	public static List<Map<String, Integer>> AllList = new ArrayList<>();
	public static Map<String, Integer>  map = new HashMap<>();
	/*Map<String, Integer> ledMap = new HashMap<String, Integer>();
   Map<String, Integer> flashMap = new HashMap<String, Integer>();
   Map<String, Integer> touchMap = new HashMap<String, Integer>();
   Map<String, Integer> keyMap = new HashMap<String, Integer>();
   Map<String, Integer> DisplayMap = new HashMap<String, Integer>();
   Map<String, Integer> camerabMap = new HashMap<String, Integer>();
   Map<String, Integer> camerafMap = new HashMap<String, Integer>(); */
	//TriColorLed           Status[0]
	//Flashlight                          1
	//TouchPanelEdge                2
	//TouchPanelKey                  3
	//Keypad                              4
	//CameraFront                     5
	//CameraBack                      6
	//
	//
	//
       public static boolean SMT = false; 
       public static void addMap(String key, int value){
	   map.put(key, value);
       }
	
       public static boolean getAllTestStatus1(){
	   boolean statu = true;
	   if (map.values().size() < 22 ) {
	       return false;
	   }
	   for (Integer v : map.values()) {
	       Log.e("liqb", "v----------------->>>"+v);
	       if (v != 1) {
		   statu = false;
	       }
	   }
	   return statu;
	}

       public static boolean getAllTestStatus(){
           boolean statu = true;
	   if (map.values().size() < 22 ) {
	       return false;
	   }
           for (Map.Entry<String, Integer> entry : map.entrySet()) {
               Log.e("liqb","key------->>> " + entry.getKey() + " and value---------->>> "+ entry.getValue());
               if (entry.getValue() != 1) {
		   statu = false;
	       }
           }
           return statu;
       }

	public static void qcRilSetCitStatus(QcRilHook mQcRilHook){
           if (getAllTestStatus()) {
               Log.e("liqb", "QcRilHook setNV  value ----------------->>>"+1);
               //set cit value
    	       //mQcRilHook.qcRilSetNvValue(6859, 1);
           }else{
               Log.e("liqb", "QcRilHook setNV  value ----------------->>>"+0);
               //mQcRilHook.qcRilSetNvValue(6859, 0);
           }
           mQcRilHook.dispose();
	}

	public static int pixelToDp(Context context, int pixel) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return pixel < 0 ? pixel : Math.round(pixel / displayMetrics.density);
	}
	public static int dpToPixel(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return dp < 0 ? dp : Math.round(dp * displayMetrics.density);
	}
	
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         