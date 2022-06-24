package com.ubx.factorykit.Framework;

import android.os.Build;
import android.os.SystemProperties;

/**
 * Created by yuanwei on 19-3-18.
 */

public class FactoryKitPro {
    public static final String PROJECT = SystemProperties.get("pwv.project", SystemProperties.get("ro.build.product"));
    // Android 11.0 project
    public static final boolean PRODUCT_SQ52M = PROJECT.equals("SQ52M") || PROJECT.equals("k62_t2151eir_v1_ga_ybx_sq52");
    public static final boolean PRODUCT_SQ57 = PROJECT.equals("SQ57") || PROJECT.equals("k62_t2157eir_v1_ta_sq57_ybx");
    public static final boolean PRODUCT_SQ45C = PROJECT.equals("SQ45C") || PROJECT.equals("k62_t2145eir_v1_ga_ybx_sq45c");
    public static final boolean PRODUCT_SQ53X = PROJECT.equals("SQ53X") || PROJECT.equals("k62_t2153eir_v1_ta_ybx_sq53_r");
    public static final boolean PRODUCT_SQ57S = PROJECT.equals("SQ57S");
    public static final boolean PRODUCT_SQ53S = PROJECT.equals("SQ53S") || PROJECT.equals("DT50S");
    // Android 10.0 project
    public static final boolean PRODUCT_SQ47 = PROJECT.equals("SQ47");
    public static final boolean PRODUCT_SQ83 = PROJECT.equals("SQ83") || PROJECT.equals("P8100P");
    public static final boolean PRODUCT_SQ53A = PROJECT.equals("SQ53A") || PROJECT.equals("lito");
    public static final boolean PRODUCT_SQ53Z = PROJECT.equals("SQ53Z") || PROJECT.equals("DT50");
    //47C&47使用同一套代码
    public static final boolean IS_SQ47C = SystemProperties.get("ro.boot.hardware.revision", "V01").contains("C");
    // Android 9.0 project
	public static final boolean PRODUCT_SQ29Z = PROJECT.equals("SQ29Z");
	public static final boolean PRODUCT_SQ28 = PROJECT.contains("SQ28");
    public static final boolean PRODUCT_SQ53 = PROJECT.equals("SQ53");
    public static final boolean PRODUCT_SQ53Q = PROJECT.equals("SQ53Q"); //|| Utilities.getBuildProject().equals("SQ53Q");
	public static final boolean PRODUCT_SQ53C = PROJECT.equals("SQ53C");
    public static final boolean PRODUCT_SQ51FW = PROJECT.equals("SQ51FW");
    public static final boolean PRODUCT_SQ38 = PROJECT.equals("SQ38");
    public static final boolean PRODUCT_SQ45 = PROJECT.equals("SQ45");
    public static final boolean PRODUCT_SQ45S = PROJECT.equals("SQ45S");
    public static final boolean PRODUCT_SQ51S = PROJECT.equals("SQ51S");
    public static final boolean PRODUCT_SQ53H = PROJECT.equals("SQ53H");
    // Android 8.1 project
    public static final boolean PRODUCT_SQ27TG = PROJECT.equals("SQ27TG");
    public static final boolean PRODUCT_SQ27TH = PROJECT.equals("SQ27TH");
    public static final boolean PRODUCT_SQ29C = PROJECT.equals("SQ29C");
    public static final boolean PRODUCT_SQ52TG = PROJECT.equals("SQ52TG");
    public static final boolean PRODUCT_SQ52TGW = PROJECT.equals("SQ52TGW");
    public static final boolean PRODUCT_SQ52UT = PROJECT.equals("SQ52UT");

    // 定制
    public static final boolean CUSTOM_UTE = false;//Build.PWV_CUSTOM_CUSTOM.startsWith("UTE");

    public static final boolean mHaveNavbar = PROJECT.equals("SQ53") || PROJECT.equals("SQ53Q") || PROJECT.equals("SQ53C") || FactoryKitPro.PRODUCT_SQ38 || FactoryKitPro.PRODUCT_SQ47;
    //SQ53 SQ53Q  SQ47 SQ53Z have eight cores, cpu size read need start with the forth.(前面4个核为低功耗核)
    public static final boolean CPU_HAVE_EIGHT_CORES = PROJECT.equals("SQ53") || PROJECT.equals("SQ53Q") || PROJECT.equals("SQ47") || PRODUCT_SQ53Z;
    //judge project is urovo version
    public static final boolean IS_UROVO_VERSION = SystemProperties.get("pwv.custom.custom").length() >0;
	// urovo huangjiezhou modify on 20210901
	public static final boolean FULL_SCREEN_HAVE_NAVBAR = !PROJECT.contains("SQ28") && !PROJECT.equals("SQ29Z") && !PROJECT.contains("SQ46") && !PROJECT.contains("SQ42")
            && !PROJECT.contains("SQ52") && !PROJECT.contains("SQ51") && !PROJECT.contains("SQ45") && !PROJECT.contains("k62_t2151eir_v1_ga_ybx_sq52") && !PRODUCT_SQ57
            && !PROJECT.contains("k62_t2145eir_v1_ga_ybx_sq45c") && !PRODUCT_SQ57S;
	public static final boolean isDoublePsam = !PROJECT.contains("SQ47") && !PROJECT.equals("SQ53C") && !PROJECT.contains("SQ42") && !PROJECT.contains("SQ83");
    public static final String testItemConfigFile = "/system/etc/item_config_factory.xml";
    public static final String keypadConfigFile = "/system/etc/keypad_config_factory.xml";
    public static final String ledpadConfigFile = "/system/etc/led_config_factory.xml";

    public static final String ledScanRed = SystemProperties.get("persist.sys.led.scan");
    public static final String ledScanGreen = SystemProperties.get("persist.sys.led.scan.green");
    public static final String ledScanBlue = SystemProperties.get("persist.sys.led.scan.blue");
    public static final String ledNotyRed = SystemProperties.get("persist.sys.led.red");
    public static final String ledNotyGreen = SystemProperties.get("persist.sys.led.green");
    public static final String ledNotyBlue = SystemProperties.get("persist.sys.led.blue");
    //串口节点
    public static final String POGO_KEY_SERIAL = SystemProperties.get("persist.sys.pogopin.serial", "/dev/ttyHSL0");
    public static final boolean GOOGLE_KEY_MENU_DISPLAY = SystemProperties.get("pwv.custom.enbuild", "false").equals("true") && (PROJECT.contains("SQ47") || PRODUCT_SQ83 || PRODUCT_SQ53Z);
}
