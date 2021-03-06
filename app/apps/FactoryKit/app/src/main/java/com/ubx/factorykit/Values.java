/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit;

import com.ubx.factorykit.Framework.FactoryKitPro;

public class Values {

    public static final boolean LOG = true;
    public static final boolean WIFI_ENABLE_PASS = false;
    public static final boolean HasFlashlightFile = true;
    public static final boolean BLUETOOTH_SCAN_TO_SUCESS = true;
    // public static final String PROP_HW_PLATFORM = "ro.hw_platform";
    public static final String PROP_HW_PLATFORM = "ro.product.device";
    public static final String PRODUCT_MSM7627A_SKU1 = "QRD_SKU1";
    public static final String PRODUCT_MSM7627A_SKU3 = "QRD_SKU3";
    public static final String PRODUCT_MSM8X25_SKU5 = "msm8x25_sku5";
    public static final String PRODUCT_MSM8X25Q_SKUD = "msm8x25q_skud";
    public static final String PRODUCT_MSM7627A_SKU7 = "msm7627a_sku7";
    public static final String PRODUCT_MSM7X27_SKU5A = "msm7x27_sku5a";
    public static final String PRODUCT_MSM8226 = "msm8226";
    public static final String PRODUCT_MSM8610 = "msm8610";
    public static final String PRODUCT_MSMx30 = "msm8960";

    public static final String PRODUCT_DEFAULT = "msm7627a";
    public static final boolean ENABLE_NV = false;

    public static final String CONFIG_FILE_IN_SD = "/data/factorykit_config.xml";
    //测试项配置文件
    public static final String CONFIG_FILE_SEARCH_LIST[] = { "/sdcard/item_config_factory.xml",
            "/data/item_config_factory.xml" , FactoryKitPro.testItemConfigFile};
    //按键测试配置文件
    public static final String CONFIG_KEYPAD_FILE_SEARCH_LIST[] = { "/sdcard/keypad_config_factory.xml",
            "/data/keypad_config_factory.xml" , FactoryKitPro.keypadConfigFile};
    //led测试配置文件
    public static final String CONFIG_LED_FILE_SEARCH_LIST[] = { "/sdcard/led_config_factory.xml",
            "/data/led_config_factory.xml" , FactoryKitPro.ledpadConfigFile};
    public static final String CHARGING_ENABLE_SYS_FILE = "/sys/class/power_supply/battery/charging_enabled";

    /** Property */
    public static final String PROP_CHARGE_DISABLE = "persist.usb.chgdisabled";
    public static final String PROP_MULTISIM = "persist.radio.multisim.config";

    /** Config */
    public static final boolean ENABLE_BACKGROUND_SERVICE = true;
    public static final boolean FACTORY_MODE = false;

    public static final String BROADCAST_UPDATE_MAINVIEW = "com.qualcomm.factory.updateview";
    public static final String KEY_SERVICE_INDEX = "key";
    public static final String KEY_SERVICE_DELAY = "ServiceDelay";
    public static final String KEY_OUTPUT = "output";

    public static final boolean SERVICE_LOG = true;
    public static final String BROADCAST_FACTORY_SINGLETEST = "com.ubx.factorykit.singletest";

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         