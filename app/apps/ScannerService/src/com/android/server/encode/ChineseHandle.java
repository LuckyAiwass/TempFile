
package com.android.server.encode;

import android.util.Log;

public class ChineseHandle {
    /*int[] temp_value = new int[byteBarcodeData.length];
    for(int i = 0; i < .byteBarcodeData.length;i++){
        temp_value[i] = .byteBarcodeData[i]&0xff;
    }

ResultsView.setText("Data(GB2312): " + ChineseHandle(temp_value) +*/
    /**
     * Function:ChineseHandle
     * 
     * @param arraydata
     * @return
     */
    public static String chineseBarcode(int[] arraydata) {
        String str01 = "";
        if (!Isutf8orgb2312(arraydata)) {
            str01 = Utf8toString(arraydata);
        } else {
            str01 = DecodetoString(1, arraydata);
        }
        return str01;
    }

    private static boolean Isutf8orgb2312(int[] value) {
        boolean bool = true;// GB2312
        int len = value.length;
        boolean flag = false;

        for (int i = 0; i < len; i++) {
            if (value[i] >= 128) {
                if ((i + 2) < len) {
                    if ((value[i] >= 0xE0) && (value[i + 1] >= 0x80) && (value[i + 2] >= 0x80)) {// Judge
                                                                                                 // TF-8
                        i = i + 2;
                    } else {
                        flag = true;
                        bool = true;
                        break;
                    }
                } else {
                    flag = true;
                    break;
                }
            }
        }
        if (!flag) {
            bool = false;
        }
        return bool;

    }

    /**
     * function:DecodetoString
     * 
     * @param value[] id:=0,reservd,=1,Chinese ,=2,Japanese
     * @return
     */
    private static String DecodetoString(int id, int[] value) {
        int len = value.length;
        String str = null;
        byte[] bt = new byte[len];
        for (int i = 0; i < len; i++) {
            bt[i] = (byte) value[i];
        }
        try {
            switch (id) {
                case 1:
                    Log.d("GB2312", "GB2312 in Java dectected");
                    str = new String(bt, "gb2312");
                    break;
                case 2:
                    Log.d("SHIFT-JIS", "SHIFT-JIS in Java dectected");
                    str = new String(bt, "SHIFT-JIS");
                    break;
                default:
                    str = new String(bt, "gb2312");
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * function:UTF8 to String
     * 
     * @param value
     * @return
     */
    private static String Utf8toString(int[] value) {
        int len = value.length;
        String str = null;
        byte[] bt = new byte[len];
        for (int i = 0; i < len; i++) {
            bt[i] = (byte) value[i];
        }
        try {
            str = new String(bt, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return str;
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   