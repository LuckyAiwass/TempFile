package com.ubx.factorykit.DeviceInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.device.DeviceManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

//import com.qualcomm.qcnvitems.QcNvItemIds;
//import com.qualcomm.qcrilhook.QcRilHook;
//import com.qualcomm.qcrilhook.*;
//import com.qualcomm.qcrilhook.IQcRilHook;
//import com.qualcomm.qcnvitems.QcNvItems;
//import com.qualcomm.qcrilhook.QcRilHookCallback;
//import android.telephony.MSimTelephonyManager;
import android.telephony.TelephonyManager;


import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHook;
import com.qualcomm.qcrilhook.QcRilHookCallback;
import com.qualcomm.qcrilhook.OemHookCallback;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.AsyncResult;

import static com.ubx.factorykit.Framework.FactoryKitPro.PRODUCT_SQ53H;


public class DeviceInfo extends Activity {
    private static final String  TAG  = "DeviceInfo333";
    private static final int  SN_LENGTH  = 14;
    private boolean mIsQcRilHookReady = false;
    TextView               mSoftwareVersion;
    TextView               mMacAddress;
    TextView               mCpuSize;
    TextView               mMemoryInfo;
    TextView               mCalibrationFlag;
    TextView               mQcnVersion;
    TextView               mModemVersion;
    TextView               mBaseBandVersion;
    TextView               m52C;
    TextView               mLcdInfo;
    TextView               mTpInfo;
	TextView               mbatteryTypeInfo; // battery type
    TextView               mbatteryIdInfo;  // battery id
    TextView               mBoardSN;  // mBoardSN
    Button                 mNoProblem;
    Button                 mHasProblem;
    protected boolean      mSuccess;
    private static int     mTestId;
    private static Boolean mCanAutoTest;
    String                 mSnFilePath  = "data/data/com.ubx.factorykit/sn";
    private static String  mHwnFilePath = "data/data/com.ubx.factorykit/hwn";
    private boolean mDisplayInfoFor316 = false;

    private static final String LCD_SYS_NODE = SystemProperties.get("persist.sys.lcd.info", PRODUCT_SQ53H ? "/sys/devices/platform/mtkfb@0/lcd_info" : "/sys/kernel/lcd_kobj/lcd_info");
    private static final String TP_SYS_NODE =SystemProperties.get("persist.sys.tp.info",PRODUCT_SQ53H ? "/sys/devices/platform/touch/tp_info" :  "/sys/kernel/gt1x_kobj/gt1x_info");
		// Urovo-add for battery type and battery id
    private static final String BATTERY_TYPE_NODE =SystemProperties.get("persist.sys.battery.type",PRODUCT_SQ53H ? "/sys/class/power_supply/battery/uevent" :  "/sys/class/power_supply/bms/battery_type");
    private static final String BATTERY_ID_NODE = SystemProperties.get("persist.sys.battery.id","/sys/class/power_supply/bms/resistance_id");
    private static  String MAINBOARD_SN = SystemProperties.get("persist.sys.mainboard.sn","");
    private static  String WIFI_MAC_SN = "";

    private DeviceManager mDeviceManager;

    private QcRilHook mQcRilOemHook = null;
    //private QmiOemHook mQcRilOemHook = null;
    private QcNvItems mQcNvItems = null;

    String str2499 = "";
    String str2499_01 = "";
    String str2499_02 = "";
	String strSN = "";
	String strQCN = "";
	TextView urovo_sn;
    private static String urovoSN ="unknow";
	private static final int EVENT_SHOW_STRING = 123;
	private static final int EVENT_SHOW_STRING_CAL = 124;
	private static final int EVENT_SHOW_QCN_STRING = 125;
	private List<String> NvItems_string = new ArrayList<String>(0);
	private List<String> NvItems_qcn_string = new ArrayList<String>(0);
    private QcRilHookCallback mQcrilHookCb = new QcRilHookCallback() {
        public void onQcRilHookReady() {
            try {
                // SQ53 友恺获取NV(2499)
                if((FactoryKitPro.PRODUCT_SQ53 ||FactoryKitPro.PRODUCT_SQ53Q||FactoryKitPro.PRODUCT_SQ51S) && mQcRilOemHook != null) {  //urovo add 51S by luoqi 2019.11.06
                    mQcRilOemHook.qcRilGetModemInfoStringAsync(2499, new OemHookCallback(null){//2499
                        @Override
                        public void onOemHookResponse(byte[] response, int phoneId) {
                            // 获取nv2499的第一位和第二位
                            str2499_01 = new String(response).charAt(0) + "";
                            str2499_02 = new String(response).charAt(4) + "";
                            Log.d(TAG, "sq53 str2499_01 = " + str2499_01 + ",str2499_02 = " + str2499_02);
                            //updateCalibrationFlag();
							//try {
								//strSN = new String(response,"ISO-8859-1");
							    //Log.d(TAG,"wangyinghua strSN=="+strSN);
								//NvItems_string.add(strSN);
								Message msg3 = mHandler.obtainMessage(EVENT_SHOW_STRING_CAL);
								msg3.obj = new AsyncResult(null, 0, null);
								mHandler.sendMessageDelayed(msg3, 800);

							//}catch (UnsupportedEncodingException  e) {
								//Log.e(LOG_TAG,"unsupport ISO-8859-1");
							//}
                        }
				});}
				if(FactoryKitPro.PRODUCT_SQ53Q&& mQcRilOemHook != null) {  //urovo add 51S by luoqi 2019.11.06
                    Log.d(TAG,"wangyinghua");
					mQcRilOemHook.qcRilGetModemInfoStringAsync(6860, new OemHookCallback(null){//2499
                        @Override
                        public void onOemHookResponse(byte[] response, int phoneId) {


							try {
								strSN = new String(response,"ISO-8859-1");
							    Log.d(TAG,"wangyinghua strSN=="+strSN);
								NvItems_string.add(strSN);
								Message msg4 = mHandler.obtainMessage(EVENT_SHOW_STRING);
								msg4.obj = new AsyncResult(null, 0, null);
								mHandler.sendMessageDelayed(msg4, 800);

							}catch (UnsupportedEncodingException  e) {
								//Log.e(LOG_TAG,"unsupport ISO-8859-1");
							}

                        }
				});}

				if(FactoryKitPro.PRODUCT_SQ53Q&& mQcRilOemHook != null) {  //urovo add 51S by luoqi 2019.11.06
                    Log.d(TAG,"wangyinghua");
					mQcRilOemHook.qcRilGetModemInfoStringAsync(568, new OemHookCallback(null){//2499
                        @Override
                        public void onOemHookResponse(byte[] response, int phoneId) {


							try {
								strQCN = new String(response,"ISO-8859-1");
							    Log.d(TAG,"wangyinghua strQCN=="+strQCN);
								String strqcn_01 = new String(response).charAt(0)+"";
								String strqcn_02 = new String(response).charAt(4)+"";
								String strqcn_03 = new String(response).charAt(8) + "";
								String strqcn_04 = new String(response).charAt(12) + "";
								String strqcn_05 = new String(response).charAt(16) + "";
								String strqcn_06 = new String(response).charAt(20) + "";
								String strqcn_07 = new String(response).charAt(24) + "";
								String strqcn_08 = new String(response).charAt(28) + "";
								String strqcn_09 = new String(response).charAt(32) + "";
								String strqcn_10 = new String(response).charAt(36) + "";
								String str_01 = strqcn_01+strqcn_02+"";
								String str_02 = strqcn_03+strqcn_04+"_";
								String str_03 = strqcn_05+strqcn_06+"";
								String str_04 = strqcn_07+strqcn_08+"";
								String str_05 = strqcn_09+strqcn_10+"";
								//Log.d(TAG,"wangyinghua strqcn_01=="+str_01+"--"+strqcn_02+"--"+strqcn_03+"--"+strqcn_04+"--"+strqcn_05);
								if(str_01.equals("12")){
									str_01 = "SQ53Q_";
								}else if(str_01.equals("0a")){
									str_01 = "SQ53_";
								}
								if(str_03.equals("03")){
									str_03 = "7DS_";
								}
								if(str_04.equals("01")){
									str_04 = "V01_";
								}else if(str_04.equals("02")){
									str_04 = "V02_";
								}
								if(str_05.equals("06")){
									str_05 = "H06";
								}
								strQCN = str_01+str_02+str_03+str_04+str_05+"";
								Log.d(TAG,"wangyinghua strQCN strQCN=="+strQCN);
								NvItems_qcn_string.add(strQCN);
								Message msg4 = mHandler.obtainMessage(EVENT_SHOW_QCN_STRING);
								msg4.obj = new AsyncResult(null, 0, null);
								mHandler.sendMessageDelayed(msg4, 800);

							}catch (UnsupportedEncodingException  e) {
								//Log.e(LOG_TAG,"unsupport ISO-8859-1");
							}

                        }
				});} else{
                    // 高格获取NV
                    try{
                    String mString2499 = mQcNvItems.getNvFactoryData3I();
                    str2499_01 = mString2499.charAt(0) + "";
                    str2499_02 = mString2499.charAt(1) + "";
                    Log.d("zml", "str2499_01 = " + str2499_01 + ",str2499_02 = " + str2499_02);}
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    try{
                    String qcn = mQcNvItems.getQcnVersion();
                    NvItems_qcn_string.add(Utilities.parserQcn(qcn));
                    Message msg_qcn = mHandler.obtainMessage(EVENT_SHOW_QCN_STRING);
                    msg_qcn.obj = new AsyncResult(null, 0, null);
                    mHandler.sendMessageDelayed(msg_qcn, 800);
                    Log.d("zml", "qcn == " + qcn);}catch (Exception e){
                        e.printStackTrace();
                    }
                    try{
                    byte[] mStringNvSn = mQcNvItems.getUbrovoSN();
                    String urovoSNTemp = null;
                    if (mStringNvSn != null)
                    urovoSNTemp = Utilities.convertHexToString(Utilities.bytesToHexString(mStringNvSn));//last two is SACN MODEL
                    if(urovoSNTemp !=null && urovoSNTemp.length()>0)
                    {
                        urovoSN =getUrovoSN(urovoSNTemp);
                        Log.d("zml","urovoSN" + urovoSN);
                        //if(!TextUtils.isEmpty(urovoSN))
                        urovo_sn.setText(urovoSN);
                    }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(!FactoryKitPro.PRODUCT_SQ45S) {
                        try {
                            if (mQcNvItems.getWIFIMacAddr() != null)
                                WIFI_MAC_SN = getMacAddress(mQcNvItems.getWIFIMacAddr());
                        } catch (IOException e) {
                            e.getStackTrace();
                            Log.d(TAG, "getWIFIMacAddr faild.");
                        }
                        try {
                            if (mQcNvItems.getMainBordSNNumber() != null)
                                MAINBOARD_SN = mQcNvItems.getMainBordSNNumber();
                        } catch (IOException e) {
                            Log.d(TAG, "getMainBordSNNumber faild.");
                        }
                    }
                    if (!FactoryKitPro.PRODUCT_SQ53A) {
                        // urovo huangjiezhou add begin on 20211225
                        if ("00".equals(SystemProperties.get("persist.sys.calibration_info", "00"))) {
                            Log.d(TAG, "prop-persist.sys.calibration_info is not right, try to set from nv!");
                            updateCalibrationFlag();
                        }
                        // urovo huangjiezhou add end
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onQcRilHookDisconnected() {
        }
    };

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			final AsyncResult ar;
			switch (msg.what) {
			case EVENT_SHOW_STRING:
                //snAddress.setText(NvItems_string.get(0));
                urovo_sn.setText( NvItems_string.get(0));
				break;

            case EVENT_SHOW_QCN_STRING:                //snAddress.setText(NvItems_string.get(0));
                mQcnVersion.setText(NvItems_qcn_string.get(0));
				break;

			case EVENT_SHOW_STRING_CAL:
                //snAddress.setText(NvItems_string.get(0));
                //Log.d(TAG, "updateCalibrationFlag " );
				if (!str2499_01.equals("P") && !str2499_01.equals("F")) str2499_01 = "0";
				if (!str2499_02.equals("P") && !str2499_02.equals("F")) str2499_02 = "0";
				str2499 = str2499_01 + str2499_02;
				Log.d(TAG, "wangyinghua str2499 = " + str2499);
				if (str2499 != null && !str2499.equals("")) {
					mCalibrationFlag.setText(str2499);
				} else {
					mCalibrationFlag.setText("unknown");
				}
				break;
				default :
					break;
			}

		}
	};

    private void updateCalibrationFlag() {

        Log.d(TAG, "updateCalibrationFlag " );
        if (!str2499_01.equals("P") && !str2499_01.equals("F")) str2499_01 = "0";
        if (!str2499_02.equals("P") && !str2499_02.equals("F")) str2499_02 = "0";
        Log.d(TAG, "str2499_01   "  + str2499_01 +  "str2499_01   "  + str2499_02 );
        str2499 = str2499_01 + str2499_02;
        Log.d(TAG, "str2499 = " + str2499);
        if (str2499 != null && !str2499.equals("")) {
            mCalibrationFlag.setText(str2499);
        } else {
            mCalibrationFlag.setText("unknown");
        }
        if(FactoryKitPro.PRODUCT_SQ47 || FactoryKitPro.PRODUCT_SQ83) {
            Log.d("zml"," urovoSN " + urovoSN);
            urovo_sn.setText(urovoSN);
            mBoardSN.setText(MAINBOARD_SN);
            mMacAddress.setText(WIFI_MAC_SN);
        }
    }

    /**
     * 2499 取前两位
     * @param group
     * @return
     */
    private String nvToStr(String group) {
        StringBuffer nv = new StringBuffer();
        if (group != null && !group.equals("")) {
            for (int i = 0; i < group.length(); i += 4) {
                if (nv.length() < 2) {
                    nv.append(group.charAt(i));
                }
            }
        }
        return nv.toString();
    }

    public/*private*/ static String HARDWARE_DEV = "/sys/devices/soc/800f000.qcom,spmi/spmi-0/spmi0-00/800f000.qcom,spmi:qcom,pm660@0:vadc@3100/unc_board_id";
    /**
     * SQ53 获取硬件版本号
     * @param path
     * @return
     */
    public/*private*/ static String getVersionString(String path) {
        String prop = "";
        String versionProp = "";
        File mVersionFile = new File(path);
        FileReader fileReader;
        BufferedReader br;
        try {
            fileReader = new FileReader(mVersionFile);
            br = new BufferedReader(fileReader);
            prop = br.readLine();
            versionProp = "Rev 1."+prop.substring(0, prop.indexOf("&&"));
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionProp;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if("com.urovo.deviceinfo".equals(getIntent().getAction())){
        	mDisplayInfoFor316 = true;
        }else{
                setRequestedOrientation(Framework.orientation);
        }
        mQcNvItems = new QcNvItems(this);
        mQcRilOemHook = new QcRilHook(getApplicationContext(),mQcrilHookCb);
        setContentView(R.layout.version_port);
        mSoftwareVersion = (TextView) findViewById(R.id.versionId);
        mMacAddress = (TextView) findViewById(R.id.versionMacAddress);
        if(mDisplayInfoFor316){
        	mMacAddress.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.version_2)).setVisibility(View.GONE);
        }

        mNoProblem = (Button) findViewById(R.id.versionNoProblem);
        if(mDisplayInfoFor316){
        	mNoProblem.setVisibility(View.GONE);
        }
        mHasProblem = (Button) findViewById(R.id.versionHasProblem);
        if(mDisplayInfoFor316){
        	mHasProblem.setVisibility(View.GONE);
        }
        mCpuSize = (TextView) findViewById(R.id.versionCpusize);
        if(mDisplayInfoFor316){
        	mCpuSize.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.version_9)).setVisibility(View.GONE);
        }
        mMemoryInfo = (TextView) findViewById(R.id.versionMemoryInfo);
        if(mDisplayInfoFor316){
        	mMemoryInfo.setVisibility(View.GONE);
                ((TextView) findViewById(R.id.version_10)).setVisibility(View.GONE);
        }
        mCalibrationFlag = (TextView) findViewById(R.id.calibration_info);
        mCalibrationFlag.setText(SystemProperties.get("persist.sys.calibration_info", "00"));
        /*if(mDisplayInfoFor316){
        	mCalibrationFlag.setVisibility(View.GONE);
        }*/
        //m52C = (TextView)findViewById(R.id.sq52_version);
        //if(!Utilities.getBuildProject().startsWith("SQ52")){
        //	m52C.setVisibility(View.GONE);
        //}
        mQcnVersion = (TextView) findViewById(R.id.qcnversion_info);
        // mModemVersion = (TextView) findViewById(R.id.modem_version);
        // mModemVersion.setVisibility(View.GONE);//urovo yuanwei gone
        mBaseBandVersion = (TextView) findViewById(R.id.baseband_version_info);
        TextView urovo_hw = (TextView) findViewById(R.id.urovo_hw_info);
        urovo_sn = (TextView) findViewById(R.id.urovo_sn_info);
        mLcdInfo = (TextView) findViewById(R.id.lcd_info);
        mTpInfo = (TextView) findViewById(R.id.tp_info);

        //mLcdInfo.setVisibility(View.GONE);
        //mTpInfo.setVisibility(View.GONE);


				// Urovo-add for battery type and battery id
        mbatteryTypeInfo = (TextView) findViewById(R.id.battery_info);
        mbatteryIdInfo = (TextView) findViewById(R.id.battery_id_info);
        mBoardSN = (TextView) findViewById(R.id.mainbord_sn_info);

        if(FactoryKitPro.PRODUCT_SQ45S) {
            TextView mainbord_sn = (TextView) findViewById(R.id.mainbord_sn);
            mainbord_sn.setVisibility(View.GONE);
            mLcdInfo.setVisibility(View.GONE);
            TextView tv_lcd_info = (TextView) findViewById(R.id.tv_lcd_info);
            tv_lcd_info.setVisibility(View.GONE);
            mBoardSN.setVisibility(View.GONE);
        }

        //mbatteryTypeInfo.setVisibility(View.GONE);
        // mbatteryIdInfo.setVisibility(View.GONE);
				// Urovo-add end
        //mDeviceManager = new DeviceManager();
        String qcn = SystemProperties.get("ro.build.qcn.version",SystemProperties.get("persist.sys.qcn.version",
                SystemProperties.get("persist.radio.qcn.version")));

        if(qcn != null && !qcn.equals("")){
            /*if(mDeviceManager != null){
                mQcnVersion.setText(getResources().getString(R.string.qcn_info) + mDeviceManager.parserQcn(qcn));
            }else*/

			{
                mQcnVersion.setText(qcn);
            }
        }else{
            mQcnVersion.setText("unknown");
        }

        //hw information  ro.build.hw.version
        String uhwn = SystemProperties.get(SystemProperties.get("ro.build.hw.version"),SystemProperties.get("ro.boot.hardwareversion",
                SystemProperties.get("ro.boot.hardware.revision", "")));
        if(uhwn == null || uhwn.equals("")) {
            uhwn = SystemProperties.get("persist.sys.hw.version", SystemProperties.get("ro.boot.hardware.revision", ""));
        }
        // SQ53 硬件版本号是写死的，后面看方案商是否会调整
        if(FactoryKitPro.PRODUCT_SQ53||FactoryKitPro.PRODUCT_SQ53Q){
            urovo_hw.setText( getVersionString(HARDWARE_DEV)); //urovo yuanwei 2019-05-22
        }else{
            urovo_hw.setText(uhwn);
        }
       //sn
        //String urovoSN = android.device.provider.Settings.System.getString(getContentResolver(),"device_sn");
         urovoSN =  SystemProperties.get("persist.sys.device.sn",SystemProperties.get("persist.sys.product.serialno", "Unknown"));
        urovo_sn.setText(urovoSN);
        if(!TextUtils.isEmpty(MAINBOARD_SN))
            mBoardSN.setText(MAINBOARD_SN);

        // Cpu size
        String size = "0";
        // urovo huangjiezhou add begin for check all cpuinfo 20220512
        String freq = "";
        for (int i = FactoryKitPro.CPU_HAVE_EIGHT_CORES ? 4 : 0; i < 8; i++) {
            if (!new File("/sys/devices/system/cpu/cpu"+i+"/cpufreq/cpuinfo_max_freq").exists()) {
                break;
            }

            if (TextUtils.isEmpty(freq)) {
                freq = getCpuInfo(i);
                if (!TextUtils.isEmpty(freq)) {
                    freq = freq.trim().split("\n")[0];//maybe getCpuInfo(i) has "\n"
                }
            }

            String curfreq = getCpuInfo(i);
            if (!TextUtils.isEmpty(curfreq)) {
                curfreq = curfreq.trim().split("\n")[0];//maybe getCpuInfo(i) has "\n"
            }

        	freq = Integer.parseInt(curfreq) >= Integer.parseInt(freq) ? curfreq : freq;
            Log.d(TAG, "get cpu" + i + " freq: " + freq);
        }
        if(!TextUtils.isEmpty(freq)){
            size = freq;
        }
        // urovo huangjiezhou add end
        float max = Float.parseFloat(size) / 1000;
        mCpuSize.setText(String.valueOf((int) max) + "MHz");

        // Memory Info
        mMemoryInfo.setText(getTotalMemory());

        // software version
        String mfirmwareBuildNumber = SystemProperties.get("persist.sys.sw.version",SystemProperties.get("ro.vendor.build.id", "")); // urovo yuanwei 2019.09.11
        if((FactoryKitPro.PRODUCT_SQ47|| FactoryKitPro.PRODUCT_SQ83) && !FactoryKitPro.IS_UROVO_VERSION)
            mfirmwareBuildNumber  = SystemProperties.get("persist.sys.sw.version",SystemProperties.get("ro.build.urovo.id", ""));
        //String mfirmwareBuildNumber = SystemProperties.get("ro.build.display.id", "");

        Log.d(TAG,"wangyinghua mfirmwareBuildNumber ==+"+mfirmwareBuildNumber);

		mSoftwareVersion.setText(mfirmwareBuildNumber != "" ? mfirmwareBuildNumber : Build.ID);

        // hardware version --end
        mTestId = getIntent().getIntExtra("TESTID", -1);
        mCanAutoTest = getIntent().getBooleanExtra("AUTOTEST", false);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
        String str_macAddress = SystemProperties.get("persist.sys.device.wifimac","NA");
        if (macAddress == null) {
            mMacAddress.setText(getResources().getString(R.string.mac_addr_no_exist));
        } else if(str_macAddress.equals("NA")){
           mMacAddress.setText(macAddress);
        } else {
            mMacAddress.setText(str_macAddress);
        }
        mBaseBandVersion.setText(getBaseBandVersion());
        Log.d("zml","versio   " + SystemProperties.get("gsm.version.baseband", ""));
        /** LCD & TP info */
        String lcdInfo = readSysNode(LCD_SYS_NODE);
        String tpInfo = readSysNode(TP_SYS_NODE);
        Log.d("zml","wangyinghua lcdInfo=="+lcdInfo);
        if(!"".equals(lcdInfo)) {
            mLcdInfo.setVisibility(View.VISIBLE);
            String lcdInfoArray[] ;
            if(lcdInfo.contains("\n")){
                lcdInfoArray= lcdInfo.split("\n");
                lcdInfo = lcdInfoArray[0];
                for(int i=1;i<lcdInfoArray.length;i++) {
                    lcdInfo += "\n" + lcdInfoArray[i];
                }
            }
            mLcdInfo.setText(lcdInfo);
        }
        Log.d("zml","wangyinghua tpInfo=="+tpInfo);
        if(!"".equals(tpInfo)) {
            mTpInfo.setVisibility(View.VISIBLE);
            //tpInfo = tpInfo;
            mTpInfo.setText(tpInfo);
        }
			  // Urovo-add fot battery type and battery id
        String batteryTypeInfo = readSysNode(BATTERY_TYPE_NODE);
        String batteryIdInfo = readSysNode(BATTERY_ID_NODE);

        if(!"".equals(batteryTypeInfo)) {
            mbatteryTypeInfo.setVisibility(View.VISIBLE);
            //batteryTypeInfo = getResources().getString(R.string.battery_type_info) + batteryTypeInfo;
            mbatteryTypeInfo.setText(batteryTypeInfo);
        }
        if(!"".equals(batteryIdInfo)) {
            mbatteryIdInfo.setVisibility(View.VISIBLE);
            //batteryIdInfo = getResources().getString(R.string.battery_id_info) + batteryIdInfo;
            mbatteryIdInfo.setText(batteryIdInfo);
        }

				// urovo-add end

        mNoProblem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mSuccess = true;
                returnResult();
            }
        });

        mHasProblem.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mSuccess = false;
                returnResult();
            }
        });
    }
    //SQ47 version have , and repetition,like "MPSS.AT.3.1-005_200728,MPSS.AT.3.1-005_200728"
    private String getBaseBandVersion() {
        String baseVersion = SystemProperties.get("persist.sys.bb.version",SystemProperties.get("gsm.version.baseband", "UNKNOWN"));
        String[] versions = baseVersion.split(",");
        if (versions != null && versions.length == 2 && versions[0].equals(versions[1])) {
                return versions[0];
            }
         return baseVersion;
    }



    private void returnResult() {
        setResult(mSuccess?RESULT_OK:RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG,mSuccess?"Pass":"Failed");
        mQcNvItems.dispose();
        if (mQcRilOemHook != null)mQcRilOemHook.dispose();
        finish();
    }

    public String getCpuInfo(int i) {
        String result = null;
        try {
            String[] args = {"/system/bin/cat", "/sys/devices/system/cpu/cpu"+i+"/cpufreq/cpuinfo_max_freq" };
            ProcessBuilder cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[1024];
            result = "";
            while (in.read(re) != -1) {
                System.out.println(new String(re));
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }
    public String is52C() {
        String result = "";
        try {
            String[] args = {"/system/bin/cat", "/sys/devices/soc/soc:meig-hwversion/hwversion" };
            ProcessBuilder cmd = new ProcessBuilder(args);

            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[1024];
            while (in.read(re) != -1) {
                Log.d(TAG, new String(re));
                result = result + new String(re);
            }
            in.close();

            // hwversion: 000 is in result now, and we want to remove hwversion: here
            if (result.indexOf(':') >= 0)
                result = result.substring(result.lastIndexOf(":") + 1 );
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public String getTotalMemory() {
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();

            arrayOfString = str2.split("\\s+");

            initial_memory =((long)Integer.valueOf(arrayOfString[1]).intValue()) * 1024;
            localBufferedReader.close();

        } catch (IOException e) {
        }
        return Formatter.formatFileSize(this, initial_memory);
    }

    public static String readSysNode(String nodePath){

        String message = "";
        String line = null;

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("cat " + nodePath);
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            //br = new BufferedReader(new FileReader(nodePath));
            while((line = br.readLine()) != null) {
                message += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    private String getUrovoSN(String group) {

        String product_model = android.os.SystemProperties.get("ro.product.model");
        String product_project = android.os.SystemProperties.get("pwv.project");
        String product_custom = android.os.SystemProperties.get("pwv.custom.custom");
        boolean isJANAMXT30 = ("SQ51FW".equals(product_project) || "SQ51S".equals(product_project)) && "JANAM".equals(product_custom) && "XT30".equals(product_model);
        Log.d(TAG, "model:" + product_model + ",project:" + product_project + ",custom:" + product_custom + ",is XT30:" + isJANAMXT30);
        StringBuffer sn = new StringBuffer();
        StringBuffer nv = new StringBuffer();
        StringBuffer model = new StringBuffer();
        if (group != null && !group.equals("")) {
            for (int i = 0; i < group.length(); i += 4) {
                //if(number.contains(String.valueOf(group.charAt(i)).toLowerCase())){
                // urovo add by shenpidong begin 2019-03-25
                if (isJANAMXT30) { // SQ51S/SQ51FW JANAM XT30 SN length value is 10 , date:20200325
                    if (sn.length() < 10) {
                        sn.append(group.charAt(i));
                    } else if (nv.length() < 2) {
                        if ("SQ51FW".equals(product_project) && i < 56) {
                            Log.d(TAG, "ignore i:" + i + ",At:" + group.charAt(i));
                            continue;
                        }
                        nv.append(group.charAt(i));
                    } else {
                        model.append(group.charAt(i));
                    }
                } else {
                    if (sn.length() < 14) {
                        sn.append(group.charAt(i));
                    } else if (nv.length() < 2) {
                        nv.append(group.charAt(i));
                    } else {
                        model.append(group.charAt(i));
                    }
                }
                // urovo add by shenpidong end 2019-03-25
                //}
            }
            String newDeviceSN = sn.toString().trim();
            if (android.os.SystemProperties.get("pwv.custom.custom", "urovo").equals("MYNT")) {

                newDeviceSN = "M" + newDeviceSN;
            }

            return newDeviceSN;

        }
        return "";
    }

    public String getMacAddress(String mac) {
        Log.d(TAG, "getMacAddress for mac: " + mac);
        StringBuffer macBuf = new StringBuffer();
        int macLen = mac.length() - 1;
        for (int i = 0; i < macLen; i += 2) {
            if ((i + 2) < macLen) {
                macBuf.append(mac.substring(i, i + 2));
                macBuf.append(":");
            } else {
                macBuf.append(mac.substring(i));
            }
        }
        return macBuf.toString();
    }

}
