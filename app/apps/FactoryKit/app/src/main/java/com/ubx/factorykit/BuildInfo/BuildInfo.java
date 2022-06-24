package com.ubx.factorykit.BuildInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.device.MaxNative;
import android.os.SystemProperties;
import android.device.DeviceManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.util.Log;

import com.qualcomm.qcrilhook.*;
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;
//import android.telephony.MSimTelephonyManager;


import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import static com.ubx.factorykit.ICC.Convert.bytesToHexString;

public class BuildInfo extends Activity {
    private static final String TAG = "BuildInfo";
    private Button mHasProblem;
    private Button mNoProblem;
    private TextView ap;
    private TextView mp;
    private TextView se;
    private TextView qcn;
    private TextView flag;
    private TextView urovo_sn;
    private TextView hw;
    private TextView mac;
    private TextView mCpuSize;
    private TextView mMemoryInfo;
    private TextView trigger_sec;
    private TextView tv_lcd_info;
    private TextView tv_tp_info;
    private TextView mCalibrationFlag;
    private LinearLayout layout_sec;
    private String urovoSN = "";
    private boolean mSuccess = false;
    byte[] sestatue = new byte[8];
    private DeviceManager mDeviceManager;

    private static final String LCD_SYS_NODE = SystemProperties.get("persist.sys.lcd.info", "/sys/kernel/lcd_kobj/lcd_info");
    private static final String TP_SYS_NODE =SystemProperties.get("persist.sys.tp.info","/sys/kernel/gt1x_kobj/gt1x_info");
    // Urovo-add for battery type and battery id
    private static final String BATTERY_TYPE_NODE =SystemProperties.get("persist.sys.battery.type", "/sys/class/power_supply/bms/battery_type");
    private static final String BATTERY_ID_NODE = SystemProperties.get("persist.sys.battery.id","/sys/class/power_supply/bms/resistance_id");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.build);
        init();
        //ap
        ap.setText(android.os.SystemProperties.get("ro.vendor.build.id", Build.ID));
        //mp
        String mpversion = SystemProperties.get("persist.sys.modem.version",SystemProperties.get("gsm.version.baseband"));
        if (mpversion != null) {
            mp.setText(mpversion);
        }
        //qcn
        String qcnversion = SystemProperties.get("persist.sys.qcn.version");
        if (qcnversion != null) {
            qcn.setText(getResources().getString(R.string.qcn_version)+qcnversion);
			if (FactoryKitPro.PRODUCT_SQ28){
                String[] devicname = qcnversion.split("_");
                flag.setText(getResources().getString(R.string.device_name)+devicname[0]);
			}
        }else {
            qcn.setText("QCN不存在!");
        }
        if (FactoryKitPro.PRODUCT_SQ29Z) qcn.setVisibility(View.GONE); //sq29z 暂时不显示qcn
        String calibration_info = SystemProperties.get("persist.sys.calibration_info","00");
        calibration_info = calibration_info.replace("0","U");
        calibration_info = calibration_info.replace("1","P");
        mCalibrationFlag.setText(getResources().getString(R.string.calibration_info) + calibration_info);
        //se
        //String seversion = get32550Versino();
        String seversion = android.os.SystemProperties.get("urv.se.version");
        if (TextUtils.isEmpty(seversion)) {
            byte[] ResponseData = new byte[128];
            byte[] ResLen = new byte[1];
            int firmwareVersion = MaxNative.getFirmwareVersion(ResponseData, ResLen);
            Log.d(TAG,"firmwareVersion = "+firmwareVersion);
            seversion = new String(ResponseData,0,ResLen[0]);
            if (!TextUtils.isEmpty(seversion))
                se.setText(seversion);
            else
                se.setText("SE不存在");
        }else {
            se.setText(seversion);
        }

        //sn:
        urovo_sn.setText("SN: " + "Unknown");
        //sn
        //String urovoSN = android.device.provider.Settings.System.getString(getContentResolver(),"device_sn");
        urovoSN = android.os.SystemProperties.get("persist.sys.product.serialno", "Unknown");
        urovo_sn.setText(urovoSN);

        //flag
        //hw
        String hwversion = android.os.SystemProperties.get("persist.sys.hw.version", "Unknown");
        if (hw != null) {
            hw.setText(getResources().getString(R.string.version_hardware_title)+hwversion);
        } else {
            hw.setText("硬件版本号不存在!");
        }
        //mac
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();

        if (macAddress == null) {
            mac.setText("Mac 地址不存在!");
        } else {
            mac.setText(getResources().getString(R.string.version_2)+macAddress);
        }

        // lcd
        String lcdInfo = readSysNode(LCD_SYS_NODE);
        String tpInfo = readSysNode(TP_SYS_NODE);
        if (!TextUtils.isEmpty(lcdInfo)){
            tv_lcd_info.setVisibility(View.VISIBLE);
            tv_lcd_info.setText(getResources().getString(R.string.lcd_info)+lcdInfo);
        }else {
            tv_lcd_info.setVisibility(View.GONE);
        }
        // tp
        if (!TextUtils.isEmpty(tpInfo)){
            tv_lcd_info.setVisibility(View.VISIBLE);
            tv_tp_info.setText(getResources().getString(R.string.tp_info)+tpInfo);
        }else {
            tv_tp_info.setVisibility(View.GONE);
        }

        // Cpu size
        String size = "0";
        for (int i = FactoryKitPro.CPU_HAVE_EIGHT_CORES ? 4 : 0; i < 8; i++) {
            String freq = getCpuInfo(i);
            if(!TextUtils.isEmpty(freq)){
                Log.d(TAG, "cpu: " + i);
                size = freq;
                break;
            }
        }

        float max = Float.parseFloat(size) / 1000;
        mCpuSize.setText(getString(R.string.version_9) + String.valueOf((int) max) + "MHz");

        // Memory Info
        mMemoryInfo.setText(getString(R.string.version_10) + getTotalMemory());


        //trigger_sec
        String sec = null;
        try {
            sec = getTriggerSecInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (sec != null) {
            trigger_sec.setText(sec);
        } else {
            trigger_sec.setText(getResources().getString(R.string.query_trigger_info_fail));
        }

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

    public void init() {
        ap = (TextView) findViewById(R.id.ap);
        mp = (TextView) findViewById(R.id.mp);
        se = (TextView) findViewById(R.id.se);
        qcn = (TextView) findViewById(R.id.qcn);
        flag = (TextView) findViewById(R.id.flag);
        hw = (TextView) findViewById(R.id.hwversion);
        urovo_sn = (TextView) findViewById(R.id.sn);
        mac = (TextView) findViewById(R.id.mac);
        mCalibrationFlag = (TextView) findViewById(R.id.calibration);
        mHasProblem = (Button) findViewById(R.id.versionHasProblem);
        mNoProblem = (Button) findViewById(R.id.versionNoProblem);
        trigger_sec = (TextView) findViewById(R.id.trigger_sec);
        layout_sec = (LinearLayout) findViewById(R.id.layout_sec);
        tv_tp_info = (TextView) findViewById(R.id.tv_tp_info);
        tv_lcd_info = (TextView) findViewById(R.id.tv_lcd_info);
        mCpuSize = (TextView) findViewById(R.id.versionCpusize);
        mMemoryInfo = (TextView) findViewById(R.id.versionMemoryInfo);
        if (SystemProperties.get("urv.se.support", "true").equals("true") /*&& SystemProperties.get("ro.build.type", "user").equals("userdebug")*/) {
            //nothing to do
        } else {
            layout_sec.setVisibility(View.GONE);
        }
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
            br = new BufferedReader(new FileReader(nodePath));
            while((line = br.readLine()) != null) {
                message += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return message;
    }

    private void returnResult() {
        setResult(mSuccess ? RESULT_OK : RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG, mSuccess ? "Pass" : "Failed");
        //mQcNvItems.dispose();
        //mQcRilOemHook.dispose();
        finish();
    }

    public String getTriggerSecInfo() {
        MaxNative.open();
        int ret = MaxNative.picQueryTriggerSec(sestatue);
        Log.d(TAG, "ret = " + ret);
        if (ret != 0) {
            sestatue[0] = (byte) 0xFF;
        }
        Log.v(TAG, "MESSAGE_QUERY_TRIGGER:" + sestatue[0] + "    " + sestatue[1] + "  " + sestatue[2] + "  " + sestatue[3]);
        if (sestatue[0] == (byte) 0x73 || sestatue[0] == (byte) 0x74) {
            trigger_sec.setBackgroundColor(Color.YELLOW);
        } else if (sestatue[0] == (byte) 0x00 && sestatue[0] == (byte) 0x00) {
            trigger_sec.setBackgroundColor(Color.GREEN);
        } else {
            trigger_sec.setBackgroundColor(Color.RED);
        }
        String secInfo = String.format("0x%02x%02x%02x%02x", sestatue[0], sestatue[1], sestatue[2], sestatue[3]);
        return secInfo;
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
}
