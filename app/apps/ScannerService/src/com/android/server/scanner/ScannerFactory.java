
package com.android.server.scanner;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import com.android.server.ScanServiceWrapper;

public class ScannerFactory {
    public static final int TYPE_0 = 0;

    public static final int TYPE_Opticon = 1;

    public static final int TYPE_SE955 = 2;

    public static final int TYPE_HONYWARE = 3;

    public static final int TYPE_SE4500 = 4;

    // urovo add shenpidong begin 2019-11-01
//    public static final int TYPE_DM30 = 5;
    public static final int TYPE_N6703 = 5;
    // urovo add shenpidong end 2019-11-01

    public static final int TYPE_N3680 = 6;

    public static final int TYPE_SE4850 = 7;

    public static final int TYPE_N6603 = 8;

    public static final int TYPE_SE2100 = 9;

    public static final int TYPE_IA100 = 10;

    // urovo add by shenpidong begin 2020-09-17
    // public static final int TYPE_EX30 = 11; // SQ73 7.1  7130
    // public static final int TYPE_N7130 = 11; // SQ73 7.1  7130
    public static final int TYPE_SE4770 = 11;
    // urovo add by shenpidong end 2020-09-17

    public static final int TYPE_N3601 = 12;

    public static final int TYPE_SE4710 = 13;

    public static final int TYPE_SE2707 = 14;
    public static final int TYPE_N4603 = 14;
    public static final int TYPE_SE2030 = 15;//N603
    public static final int TYPE_ZhDW = TYPE_0; // none

    public static final int TYPE_MIN = TYPE_Opticon;

    public static final int TYPE_MAX = TYPE_SE2030;

    private static final String TYPE_N603_NAME = "ov9281";

    private static final String TYPE_N6603_NAME = "n5600";

    private static final String TYPE_N6703_NAME = "n6700";

    private static final String TYPE_EX30_NAME = "HsmImager";

    private static final String TYPE_SE4710_NAME = "se4710";

    private static final String TYPE_SE2100_NAME = "se2100";

    private static final String TYPE_SE4750_NAME = "se4750";

    private static final String TYPE_SE4850_NAME = "se4850";
	
	private static final String TYPE_AR0144_NAME = "ar0144";

    private static final String TYPE_N4603_NAME = "n4603";

    private static final String TYPE_SE4770_NAME = "se4770";

    public static Scanner sScanner = null;
    private static boolean isCameraEngine = false;

    private static final String TAG = "ScannerFactory";

    private static HashMap<String, Integer> mScanerlist;

    public static Map<String, Integer> getScanerList() {
        return mScanerlist;
    }

    public static Map<String, Integer> scanerListInit() {
        mScanerlist = new HashMap<String, Integer>();
        mScanerlist.put("ME5600", 1);
        mScanerlist.put("955-2", 2);
        mScanerlist.put("honyware", 3);
        mScanerlist.put("se4750", 4);
    // urovo add shenpidong begin 2019-09-12
//        mScanerlist.put("DM30", 5);
        mScanerlist.put("N6703", 5);
    // urovo add shenpidong begin 2019-09-12
        mScanerlist.put("n3680", 6);
        mScanerlist.put("se4850", 7);
        mScanerlist.put("N6603", 8);
        mScanerlist.put("6602", 9);
        mScanerlist.put("6601", 10);
        // urovo add by shenpidong begin 2020-09-17
        //mScanerlist.put("EX30", 11);
        mScanerlist.put("se4770", 11);
        // urovo add by shenpidong end 2020-09-17
        mScanerlist.put("E2500SR", 12); // 3601
        mScanerlist.put("se4710", 13);
        mScanerlist.put("se2707", 14);
        mScanerlist.put("N603", 15);
        return mScanerlist;
    }

    public static Scanner createScanner(int type, ScanServiceWrapper scanService) {
        if (sScanner != null) {
            sScanner.release();
            sScanner = null;
        }
        isCameraEngine = false;
        String scanname = android.os.SystemProperties.get("persist.vendor.sys.scan.name", "");
        Log.i(TAG, type + ".##..... scanname:" + scanname);
        switch (type) {
            case TYPE_SE955:
                sScanner = new Se955Scanner(scanService);
                break;

            case TYPE_HONYWARE:
//                sScanner = new HonyWareScanner(scanService);
                break;

            case TYPE_N3680:
                sScanner = new N3680Scanner(scanService);
                break;
            case TYPE_Opticon:
//                sScanner = new OpticonScanner(scanService);
                break;

            case TYPE_N6703:
                if(TYPE_N6703_NAME.equals(scanname)) {
                    sScanner = new N6603Scanner(scanService , TYPE_N6703);
                    isCameraEngine = true;
                }
                break;
            // urovo add shenpidong end 2019-09-12
            // case TYPE_EX30:
            //     if(TYPE_EX30_NAME.equals(scanname)) {
            //         sScanner = new N6603Scanner(scanService , TYPE_EX30);
            //     }
            //     break;

            case TYPE_SE4500:
                if(TYPE_SE4750_NAME.equals(scanname)) {
                    sScanner = new Se4750Scanner(scanService);
                    isCameraEngine = true;
                }
                break;

            case TYPE_SE2100:
                if(TYPE_SE2100_NAME.equals(scanname)) {
                    sScanner = new Se2100Scanner(scanService);
                    isCameraEngine = true;
                }
                break;

            case TYPE_N6603:
                // urovo add shenpidong begin 2019-09-12
                if(TYPE_N6603_NAME.equals(scanname)) {
                    sScanner = new N6603Scanner(scanService , TYPE_N6603);
                    isCameraEngine = true;
                }
                // urovo add shenpidong end 2019-09-12
                break;
            case TYPE_IA100:
//                sScanner = new IA100Scanner(scanService);
                break;

            case TYPE_SE4770:
                if(TYPE_SE4770_NAME.equals(scanname)) {
                    if(android.os.Build.PROJECT.equals("SQ53H")){
                        sScanner = new ZebraMTKScanner(scanService , TYPE_SE4710);
                    } else {
                        sScanner = new Se4710Scanner(scanService , TYPE_SE4710);
                    }
                    isCameraEngine = true;
                }
                break;
            case TYPE_SE4710:
                if(TYPE_SE4710_NAME.equals(scanname)) {
                    sScanner = new Se4710Scanner(scanService , TYPE_SE4710);
                    isCameraEngine = true;
                }
                break;
            case TYPE_SE4850:
                if(TYPE_SE4850_NAME.equals(scanname)) {
                    sScanner = new Se4710Scanner(scanService , TYPE_SE4850);
                    isCameraEngine = true;
                }
                break;
		    case TYPE_SE2030:
                if(TYPE_N603_NAME.equals(scanname) || TYPE_AR0144_NAME.equals(scanname)) {
                    sScanner = new UN603Scanner(scanService);
                    isCameraEngine = true;
                }
                break;
            default:
                break;
        }
        return sScanner;
    }

    public static boolean isCameraEngine() {
        return isCameraEngine;
    }
}
