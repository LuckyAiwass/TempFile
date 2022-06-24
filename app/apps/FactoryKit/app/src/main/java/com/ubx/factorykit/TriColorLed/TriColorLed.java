/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.TriColorLed;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

//import android.device.ScanManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;

import android.os.SystemProperties;
import android.util.Log;
import android.widget.TextView;

import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;
import com.ubx.factorykit.Values;
import com.ubx.factorykit.Framework.MainApp;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import static com.ubx.factorykit.Framework.FactoryKitPro.PRODUCT_SQ53H;
import static com.ubx.factorykit.Framework.FactoryKitPro.ledNotyBlue;
import static com.ubx.factorykit.Framework.FactoryKitPro.ledNotyGreen;
import static com.ubx.factorykit.Framework.FactoryKitPro.ledNotyRed;
import static com.ubx.factorykit.Framework.FactoryKitPro.ledScanBlue;
import static com.ubx.factorykit.Framework.FactoryKitPro.ledScanGreen;
import static com.ubx.factorykit.Framework.FactoryKitPro.ledScanRed;

public class TriColorLed extends Activity {

    private static final String TAG = "TriColorLed";
    private static Context mContext;
    private final int INIT_COLOR_NUM = 4;
    private int color = 0;
	private static String doubleLightNode1 =  "/sys/class/leds/red/blink";//"/sys/devices/soc.0/gpio-leds.72/leds/green/brightness";//
	private static String doubleLightNode2 =  "/sys/class/doubleled/led/doubleled";//"/sys/devices/soc.0/78b9000.i2c/i2c-5/5-0045/leds/red/brightness";//
	private static String LENS_BRIGHTNESS = "/sys/class/leds/led:torch_1/brightness";// #设置亮度（电流大小）
	private static String LENS_SWITCH =  "/sys/class/leds/led:switch/brightness";//   #点亮led灯
    private static String POWER_ON =  "/sys/class/pwv-gpio-intf/vcc_power/enable";//SQ47 上电节点
    private static String SCAN_ON = "/sys/class/pwv-gpio-intf/scan_power/enable";//SQ47扫描指示灯上电节点
    private static String SCAN_RED =  "/sys/class/leds/scan-red/brightness";//  SQ47扫描指示红灯 开关节点
	final byte[] DOUBLE1_RED_ON = { '5' };
	final byte[] DOUBLE1_RED_OFF = { '6' };
	final byte[] DOUBLE1_GREEN_ON = { '7' };
	final byte[] DOUBLE1_GREEN_OFF = { '8' };
	final byte[] DOUBLE2_RED_ON = { '1' };
	final byte[] DOUBLE2_RED_OFF = { '2' };
	final byte[] DOUBLE2_BLUE_ON = { '3' };
	final byte[] DOUBLE2_BLUE_OFF = { '4' };
    private ColorTest mTest;
    String ledString[]={ledScanRed,ledScanGreen,ledScanBlue,ledNotyRed,ledNotyGreen,ledNotyBlue,};
    private int colorNum = INIT_COLOR_NUM;
    CountDownTimer mCountDownTimer;
    private  String ledNotiString =" ";
    private static List ledNode= new ArrayList();
    final byte[] LED_ON = new byte[]{'2','5','5'};
    final byte[] LED_OFF = new byte[]{'0'};



    @Override
    public void finish() {
    	 mTest.setColor(-1);
        super.finish();
    }

    private void init(Context context) {
        setResult(RESULT_CANCELED);
        mContext = context;
        color = 0;
        setContentView(R.layout.tricolor_led);
        TextView textView = (TextView) findViewById(R.id.led_hint);
        if(FactoryKitPro.PRODUCT_SQ52TG){
            textView.setText(R.string.led_tri_text_SQ52);
            mTest = new SQ52LedTest();
        } else if(FactoryKitPro.PRODUCT_SQ53||FactoryKitPro.PRODUCT_SQ53Q){
            textView.setText(R.string.led_tri_text_SQ53);
            mTest = new SQ53LedTest();
        } else if(FactoryKitPro.PRODUCT_SQ45){
            textView.setText(R.string.led_tri_text_SQ45);
            mTest = new SQ45LedTest();
            colorNum = 3;// urovo yuanwei 2->3
        } else if(FactoryKitPro.PRODUCT_SQ45S){
            textView.setText(R.string.led_tri_text_SQ45S);
            mTest = new SQ45SLedTest();
            colorNum = 5;
        } else if(FactoryKitPro.PRODUCT_SQ38){
            textView.setText(R.string.led_tri_text_SQ38);
            mTest = new SQ38LedTest();
            colorNum = 6;
        // urovo yuanwei add SQ53C 2019-05-17 begin
        } else if(FactoryKitPro.PRODUCT_SQ53C){
            textView.setText(R.string.led_tri_text_SQ53c);
            mTest = new SQ53CLedTest();
        // urovo yuanwei add SQ53C 2019-05-17 end
        }else if(FactoryKitPro.PRODUCT_SQ51FW){
            textView.setText(R.string.led_tri_text_SQ51FW);
            mTest = new SQ51FWLedTest();
        }else if (FactoryKitPro.PRODUCT_SQ51S){
            textView.setText(R.string.led_tri_text_SQ51s);
            mTest = new SQ51SLedTest();
        }else if (FactoryKitPro.PRODUCT_SQ47){
            textView.setText(R.string.led_tri_text_SQ47);
            mTest = new SQ47LedTest();
        }else if (PRODUCT_SQ53H){
            textView.setText(R.string.led_tri_text_SQ53);
            mTest = new SQ53HLedTest();
        }else{
            getLedConfig();
            textView.setText(ledNotiString);
            mTest = new NormalLedTest();
        }

        //根据不同项目适配不同colorNum
        if(colorNum > 0){
        mCountDownTimer = new CountDownTimer(colorNum * 1000, 1000) {

            public void onTick(long arg0) {
                logd("");
                mTest.setColor(color++);
            }

            public void onFinish() {
                mTest.setColor(-1);
                logd("");
                showDialog();
            }
        };
        mCountDownTimer.start();}
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        	 init(getApplicationContext());
    }

    @Override
    protected void onDestroy() {

        mCountDownTimer.cancel();
        super.onDestroy();
    }

    private void showDialog() {

        new AlertDialog.Builder(TriColorLed.this).setMessage(R.string.led_confirm)
                .setPositiveButton(R.string.yes, passListener).setNegativeButton(R.string.no, failListener).show();
    }

    OnClickListener passListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {

            setResult(RESULT_OK);
            Utilities.writeCurMessage(mContext, TAG, "Pass");
            finish();
        }
    };

    OnClickListener failListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {

            setResult(RESULT_CANCELED);
            Utilities.writeCurMessage(mContext, TAG, "Failed");
            finish();
        }
    };
	void enableDevice(String fileNode, byte[] ledData) {
		try {

			FileOutputStream fileOutputStream = new FileOutputStream(fileNode);
			fileOutputStream.write(ledData);
			fileOutputStream.close();
		} catch (FileNotFoundException e) {
                        android.util.Log.d("LedsTest","FileNotFoundException" + e.toString());
		} catch (IOException e) {
                         android.util.Log.d("LedsTest","IOException" + e.toString());
		}catch (Exception e) {
            android.util.Log.d("LedsTest","Exception" + e.toString());
        }

	}



    void logd(Object d) {

        Log.d(TAG, "" + d);
    }

    void loge(Object e) {

        Log.e(TAG, "" + e);
    }
    private interface ColorTest {
    	public void setColor(int color);
    }
    private class SQ52LedTest implements ColorTest{

		@Override
	    public void setColor(int color) {

	        logd("set:" + color);
	        if(color == 0){
	        	enableDevice(doubleLightNode2, DOUBLE2_BLUE_ON);
	        	enableDevice(doubleLightNode2, DOUBLE2_RED_OFF);
				enableDevice(LENS_SWITCH, new byte[]{'0'});
				enableDevice(LENS_BRIGHTNESS, new byte[]{'0'});
	        }else if(color == 1){
	        	enableDevice(doubleLightNode2, DOUBLE2_RED_ON);
	        	enableDevice(doubleLightNode2, DOUBLE2_BLUE_OFF);
				enableDevice(LENS_SWITCH, new byte[]{'0'});
				enableDevice(LENS_BRIGHTNESS, new byte[]{'0'});
	        }else if(color == 2){
				enableDevice(LENS_SWITCH, new byte[]{'1'});
				enableDevice(LENS_BRIGHTNESS, new byte[]{'2','5'});
	        	enableDevice(doubleLightNode2, DOUBLE2_BLUE_OFF);
	        	enableDevice(doubleLightNode2, DOUBLE2_RED_OFF);
	        }else if(color == -1){
	        	enableDevice(doubleLightNode2, DOUBLE2_BLUE_OFF);
	        	enableDevice(doubleLightNode2, DOUBLE2_RED_OFF);
				enableDevice(LENS_SWITCH, new byte[]{'0'});
				enableDevice(LENS_BRIGHTNESS, new byte[]{'0'});
	        }

	    }
    	
    }


    private static String SQ53_led_green = "/sys/class/leds/green/brightness";
    private static String SQ53_led_red = "/sys/class/leds/red/brightness";
    private static String SQ53_led_blue = "/sys/class/leds/blue/brightness";
    private static String SQ53_led_left_red = "/sys/class/leds/left-red/brightness";
    private class SQ53LedTest implements ColorTest{

        @Override
        public void setColor(int color) {

            logd("set:" + color);
            if(color == 0){
                enableDevice(SQ53_led_green, new byte[]{'2','5','5'});
            }else if(color == 1){
                enableDevice(SQ53_led_green, new byte[]{'0'});
                enableDevice(SQ53_led_red, new byte[]{'2','5','5'});
            }else if(color == 2){
                enableDevice(SQ53_led_red, new byte[]{'0'});
                enableDevice(SQ53_led_blue, new byte[]{'2','5','5'});
            }else if(color == 3){
                enableDevice(SQ53_led_blue, new byte[]{'0'});
                enableDevice(SQ53_led_left_red, new byte[]{'2','5','5'});
            }else if(color == -1){
                enableDevice(SQ53_led_green, new byte[]{'0'});
                enableDevice(SQ53_led_red, new byte[]{'0'});
                enableDevice(SQ53_led_blue, new byte[]{'0'});
                enableDevice(SQ53_led_left_red, new byte[]{'0'});
            }

        }
        
    }

    private static String SQ53H_led_green = "/sys/class/leds/notification_led_green/brightness";
    private static String SQ53H_led_red = "/sys/class/leds/notification_led_red/brightness";
    private static String SQ53H_led_blue = "/sys/class/leds/custom_led_blue/brightness";
    private static String SQ53H_led_left_red = "/sys/class/leds/scanner_led_red/brightness";
    private class SQ53HLedTest implements ColorTest{

        @Override
        public void setColor(int color) {

            logd("set:" + color);
            if(color == 0){
                enableDevice(SQ53H_led_green, new byte[]{'2','5','5'});
            }else if(color == 1){
                enableDevice(SQ53H_led_green, new byte[]{'0'});
                enableDevice(SQ53H_led_red, new byte[]{'2','5','5'});
            }else if(color == 2){
                enableDevice(SQ53H_led_red, new byte[]{'0'});
                enableDevice(SQ53H_led_blue, new byte[]{'2','5','5'});
            }else if(color == 3){
                enableDevice(SQ53H_led_blue, new byte[]{'0'});
                enableDevice(SQ53H_led_left_red, new byte[]{'2','5','5'});
            }else if(color == -1){
                enableDevice(SQ53H_led_green, new byte[]{'0'});
                enableDevice(SQ53H_led_red, new byte[]{'0'});
                enableDevice(SQ53H_led_blue, new byte[]{'0'});
                enableDevice(SQ53H_led_left_red, new byte[]{'0'});
            }

        }

    }

    private static String SQ47_led_green = "/sys/class/leds/green/brightness";
    private static String SQ47_led_red = "/sys/class/leds/red/brightness";
    private static String SQ47_led_scan = "/sys/class/leds/scan/brightness";
    private static String SQ47_led_blue = "/sys/class/leds/blue/brightness";

    private class SQ47LedTest implements ColorTest{

        @Override
        public void setColor(int color) {

            logd("set:" + color);
            if(color == 0){
                enableDevice(SQ47_led_green, new byte[]{'2','5','5'});
            }else if(color == 1){
                enableDevice(SQ47_led_green, new byte[]{'0'});
                enableDevice(SQ47_led_red, new byte[]{'2','5','5'});
            }else if(color == 2){
                enableDevice(SQ47_led_red, new byte[]{'0'});
                enableDevice(SQ47_led_blue, new byte[]{'2','5','5'});
            }else if(color == 3){
                enableDevice(SQ47_led_blue, new byte[]{'0'});
                //enableDevice(SQ47_led_scan, new byte[]{'2','5','5'});
                //scan
                enableDevice(POWER_ON, new byte[]{'1'});
                enableDevice(SCAN_ON, new byte[]{'1'});
                enableDevice(SCAN_RED, new byte[]{'2','5','5'});
            }else if(color == -1){
                enableDevice(SQ47_led_green, new byte[]{'0'});
                enableDevice(SQ47_led_red, new byte[]{'0'});
                //enableDevice(SQ47_led_scan, new byte[]{'0'});
                enableDevice(SQ47_led_blue, new byte[]{'0'});
                //scan
                enableDevice(POWER_ON, new byte[]{'0'});
                enableDevice(SCAN_ON, new byte[]{'0'});
                enableDevice(SCAN_RED, new byte[]{'0'});
            }

        }

    }


    //urovo luoqi SQ51S led 2019.11.01
    private static String SQ51S_left_red_pwr = "/sys/class/leds/left-red-pwr/brightness"; // power ctrl for left red led
    private static String SQ51S_led_green = "/sys/class/leds/green/brightness";
    private static String SQ51S_led_red = "/sys/class/leds/red/brightness";
    private static String SQ51S_led_blue = "/sys/class/leds/blue/brightness";
    private static String SQ51S_led_left_red = "/sys/class/leds/left-red/brightness";
    private class SQ51SLedTest implements ColorTest{
    
        @Override
        public void setColor(int color) {
			enableDevice(SQ51S_left_red_pwr, new byte[]{'1'});	// Power enable
			
            logd("set:" + color);
            if(color == 0){
                enableDevice(SQ51S_led_left_red, new byte[]{'2','5','5'});
            }else if(color == 1){
                enableDevice(SQ51S_led_left_red, new byte[]{'0'});
                enableDevice(SQ51S_led_blue, new byte[]{'2','5','5'});
            }else if(color == 2){
                enableDevice(SQ51S_led_blue, new byte[]{'0'});
                enableDevice(SQ51S_led_green, new byte[]{'2','5','5'});
            }else if(color == 3){
                enableDevice(SQ51S_led_green, new byte[]{'0'});
                enableDevice(SQ51S_led_red, new byte[]{'2','5','5'});
            }else if(color == -1){
                enableDevice(SQ51S_led_green, new byte[]{'2','5','5'});
                enableDevice(SQ51S_led_red, new byte[]{'0'});
                enableDevice(SQ51S_led_blue, new byte[]{'0'});
                enableDevice(SQ51S_led_left_red, new byte[]{'0'});
                enableDevice(SQ51S_left_red_pwr, new byte[]{'0'});	// Power Disable
            }
			
        }
    }

    private static String SQ45_led_green = "/sys/class/leds/green/brightness";
    private static String SQ45_led_red = "/sys/class/leds/red/brightness";
    // urovo yuanwei 添加蓝灯测试 2019-05-07
    private static String SQ45_led_blue = "/sys/goodix/led_test/led_test";
    private class SQ45LedTest implements ColorTest{

        @Override
        public void setColor(int color) {

            logd("set:" + color);
            if(color == 0){
                enableDevice(SQ45_led_green, new byte[]{'2','5','5'});
            }else if(color == 1){
                enableDevice(SQ45_led_green, new byte[]{'0'});
                enableDevice(SQ45_led_red, new byte[]{'2','5','5'});
            }else if(color == 2){
                enableDevice(SQ45_led_red, new byte[]{'0'});
                enableDevice(SQ45_led_blue, new byte[]{'2','5','5'});
            }else if(color == -1){
                enableDevice(SQ45_led_green, new byte[]{'1'});
                enableDevice(SQ45_led_red, new byte[]{'0'});
                enableDevice(SQ45_led_blue, new byte[]{'0'});
            }
        }

    }

    private class SQ45SLedTest implements ColorTest{

        @Override
        public void setColor(int color) {

            logd("set:" + color);
            if(color == 0){
                enableDevice(SQ45_led_green, new byte[]{'2','5','5'});
            }else if(color == 1){
                enableDevice(SQ45_led_green, new byte[]{'0'});
                enableDevice(SQ45_led_red, new byte[]{'2','5','5'});
            }else if(color == 2){
                enableDevice(SQ45_led_red, new byte[]{'0'});
                enableDevice(SQ45_led_blue, new byte[]{'2'});//扫描蓝灯亮
            }else if(color == 3){
                enableDevice(SQ45_led_blue, new byte[]{'3'});//扫描蓝灯灭
                enableDevice(SQ45_led_blue, new byte[]{'6'});//扫描红灯上电
                enableDevice(SQ45_led_blue, new byte[]{'4'});//扫描红灯亮
            }else if(color == 4){
                enableDevice(SQ45_led_blue, new byte[]{'5'});//扫描红灯灭
                enableDevice(SQ45_led_blue, new byte[]{'0'});//键盘蓝灯亮
            }else if(color == -1){
                enableDevice(SQ45_led_green, new byte[]{'2','5','5'});//测试完工作绿灯亮
                enableDevice(SQ45_led_red, new byte[]{'0'});
                enableDevice(SQ45_led_blue, new byte[]{'1'});//键盘蓝灯灭
            }
        }

    }

    private static String SQ38_led_green = "/sys/class/leds/green/brightness";
    private static String SQ38_led_red = "/sys/class/leds/red/brightness";
    private static String SQ38_led_doubleled = "/sys/class/doubleled/led/doubleled";
    //ScanManager mScanManager;
    private CharSequence entries[];
    private CharSequence entriesvaule[];

    private class SQ38LedTest implements ColorTest {

        @Override
        public void setColor(int color) {

            logd("set:" + color);
            if (color == 0) {
                enableDevice(SQ38_led_green, new byte[]{'1'});
            } else if (color == 1) {
                enableDevice(SQ38_led_green, new byte[]{'0'});
                enableDevice(SQ38_led_red, new byte[]{'1'});
            } else if (color == 2) {
                //左侧第二组红灯亮
                enableDevice(SQ38_led_red, new byte[]{'0'});
                enableDevice(SQ38_led_doubleled, new byte[]{'1'});
            } else if (color == 3) {
                //左侧第二组红灯灭,绿灯亮
                enableDevice(SQ38_led_doubleled, new byte[]{'2'});
                enableDevice(SQ38_led_doubleled, new byte[]{'3'});
            } else if (color == 4) {
                //中间红灯亮
                //initScan();
                enableDevice(SQ38_led_doubleled, new byte[]{'4'});
                enableDevice(SQ38_led_doubleled, new byte[]{'0'});//开启供电
                enableDevice(SQ38_led_doubleled, new byte[]{'5'});
            } else if (color == 5) {
                //中间红灯灭,绿灯亮
                enableDevice(SQ38_led_doubleled, new byte[]{'6'});
                enableDevice(SQ38_led_doubleled, new byte[]{'7'});
            } else if (color == -1) {
                enableDevice(SQ38_led_green, new byte[]{'1'});
                enableDevice(SQ38_led_red, new byte[]{'0'});
                enableDevice(SQ38_led_doubleled, new byte[]{'2'});
                enableDevice(SQ38_led_doubleled, new byte[]{'4'});
                enableDevice(SQ38_led_doubleled, new byte[]{'6'});
                enableDevice(SQ38_led_doubleled, new byte[]{'8'});
                //mScanManager.closeScanner();// 关闭扫描头服务
		// urovo add shenpidong begin 2019-12-17 , for only SQ38
//                enableDevice(SQ38_led_doubleled, new byte[]{'9'});//关闭供电
		// urovo add shenpidong end 2019-12-17
            }
        }
    }

    // urovo yuanwei SQ53C led 2019-05-22
    private class SQ53CLedTest implements ColorTest {

        @Override
        public void setColor(int color) {

            logd("set:" + color);
            if (color == 0) {
                enableDevice(SQ38_led_green, new byte[]{'1'});
            } else if (color == 1) {
                enableDevice(SQ38_led_green, new byte[]{'0'});
                enableDevice(SQ38_led_red, new byte[]{'1'});
            } else if (color == 2) {
                //左侧第二组红灯亮
                enableDevice(SQ38_led_red, new byte[]{'0'});
                enableDevice(SQ38_led_doubleled, new byte[]{'1'});
            } else if (color == 3) {
                //左侧第二组红灯灭,蓝灯亮
                enableDevice(SQ38_led_doubleled, new byte[]{'2'});
                enableDevice(SQ38_led_doubleled, new byte[]{'3'});
            } else if (color == -1) {
                enableDevice(SQ38_led_green, new byte[]{'1'});
                enableDevice(SQ38_led_red, new byte[]{'0'});
                enableDevice(SQ38_led_doubleled, new byte[]{'2'});
                enableDevice(SQ38_led_doubleled, new byte[]{'4'});
            }
        }
    }

    private static String SQ51FW_led_red = "/sys/class/leds/red/brightness";
    private static String SQ51FW_led_bus = "/sys/bus/i2c/drivers/AW2013_LED/2-0045/led";
    private class SQ51FWLedTest implements ColorTest{

        @Override
        public void setColor(int color) {

            logd("set:" + color);
            if(color == 0){
                enableDevice(SQ51FW_led_red, new byte[]{'1'});
            }else if(color == 1){
                //红灯灭，绿灯亮
                enableDevice(SQ51FW_led_red, new byte[]{'0'});
                enableDevice(SQ51FW_led_bus, new byte[]{'1'});
            }else if(color == 2){
                //绿灯灭，红灯亮
                enableDevice(SQ51FW_led_bus, new byte[]{'0'});
                enableDevice(SQ51FW_led_bus, new byte[]{'3'});
            }else if(color == 3){
                //红灯灭，蓝灯亮
                enableDevice(SQ51FW_led_bus, new byte[]{'2'});
                enableDevice(SQ51FW_led_bus, new byte[]{'5'});
            }else if(color == -1){
                enableDevice(SQ51FW_led_red, new byte[]{'0'});
                enableDevice(SQ51FW_led_bus, new byte[]{'0'});
                enableDevice(SQ51FW_led_bus, new byte[]{'2'});
                enableDevice(SQ51FW_led_bus, new byte[]{'4'});
            }

        }
        
    }

    private void getLedConfig() {
        String configFile = null;
        String configFileExternalSD = Utilities.getExternalSDPath(mContext);
        ledNode.clear();
        ledNotiString =getString(R.string.led_tri_text_nomal);
        //priority read External SD
        if (configFileExternalSD != null) {
            String tempPath = configFileExternalSD + "/led_config_factory.xml";
            File temp = new File(tempPath);
            if (temp.exists() && temp.canRead())
                configFile = tempPath;
        }

        if (configFile == null) {
            for (String tmpConfigFile : Values.CONFIG_LED_FILE_SEARCH_LIST) {
                File tmp = new File(tmpConfigFile);
                if (tmp.exists() && tmp.canRead()) {
                    configFile = tmpConfigFile;
                    logd("Found config file: " + tmpConfigFile);
                    break;
                }
            }
        }

        XmlPullParser xmlPullParser = null;
        if (configFile != null) {
            XmlPullParserFactory xmlPullParserFactory;
            try {
                xmlPullParserFactory = XmlPullParserFactory.newInstance();
                xmlPullParserFactory.setNamespaceAware(true);
                xmlPullParser = xmlPullParserFactory.newPullParser();
                FileInputStream fileInputStream = new FileInputStream(configFile);
                xmlPullParser.setInput(fileInputStream, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            int mEventType = xmlPullParser.getEventType();
            /** Parse the xml */
            while (mEventType != XmlPullParser.END_DOCUMENT) {
                if (mEventType == XmlPullParser.START_TAG) {
                    String name = xmlPullParser.getName();

                    if (name.equals("led_test")) {
                        ledNotiString +=  xmlPullParser.getAttributeValue(null, "ledColor");
                        ledNotiString +=" ,";
                        ledNode.add(xmlPullParser.getAttributeValue(null, "ledPath"));
                    }
                }
                mEventType = xmlPullParser.next();
            }
            if(ledNode.size() > 0)
                colorNum = ledNode.size();
        } catch (Exception e) {
            loge(e);
        }
    }


    private class NormalLedTest implements ColorTest{

        @Override
        public void setColor(int color) {

            logd("set:" + color);
            if(color >= 0){
                if(color>0)
                enableDevice((String)ledNode.get(color-1),LED_OFF);
                enableDevice((String)ledNode.get(color),LED_ON);
            }else if(color == -1){
                for (Object node : ledNode) {
                    enableDevice((String) node,LED_OFF);
                }
            }

        }

    }
}
