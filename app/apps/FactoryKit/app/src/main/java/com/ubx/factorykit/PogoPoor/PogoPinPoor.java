package com.ubx.factorykit.PogoPoor;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/*
* Urovo huangjiezhou add for SQ57
*
*/

public class PogoPinPoor extends Activity {
    private static final String TAG = "PogoPinPoor";

    private static final String ETHERNET_CHANGED = "ethernet_status_changed";
    private EthernetManager mEthernetManager;
    private StringBuilder mStringBuilder;
    private TextView mTextChargeTitle;
    private TextView mTextCharge;
    private TextView mTextEthernetTitle;
    private TextView mTextEthernet;
    private TextView mTextEthernetIpAddress;
    private TextView mTextUsbPluggedState;
    private Button chargeTest;
    private Button ethernetTest;
    private Context mContext;
    private Toast mToast;
    private static Handler mHandler = new Handler();
    private boolean isReceiverRegister;// Record all Receiver Registers state

    // Node PATH --> PATH_HOST
    // use prop value first --> persist.sys.pogopin.otgdata.switch <--
    // add pogopin poor first for SQ57, so def PATH_HOST for SQ57
    private final String PATH_HOST        = SystemProperties.get("persist.sys.pogopin.otgdata.switch",
                                            "/sys/kernel/kobject_pogo_otg_status/pogo_otg_status");

    // default node in os, no need to change basically
    private final String PATH_VOLTAGE_NOW = "/sys/class/power_supply/battery/voltage_now";
    private final String PATH_STATUS      = "/sys/class/power_supply/battery/status";

    private final EthernetManager.Listener mEthernetListener = new EthernetManager.Listener() {
        @Override
        public void onAvailabilityChanged(String iface, boolean isAvailable) {
            mHandler.post(mUpdateEthernetMacAvailableRunnable);
        }
    };

    private final Runnable mUpdateEthernetMacAvailableRunnable = new Runnable() {
        @Override
        public void run() {
            syscEthInfo();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.pogo_poor);
        mContext = getApplicationContext();
        mEthernetManager = (EthernetManager) getSystemService(Context.ETHERNET_SERVICE);

        int renameNumIndex = -1;
        mTextChargeTitle = (TextView) findViewById(R.id.pogo_pin_t4);
        mStringBuilder = new StringBuilder(mTextChargeTitle.getText().toString());
        renameNumIndex = mStringBuilder.indexOf("4");
        if (renameNumIndex != -1) {
            mStringBuilder.setCharAt(renameNumIndex, '1');
        }
        mTextChargeTitle.setText(mStringBuilder.toString());

        mTextEthernetTitle = (TextView) findViewById(R.id.pogo_pin_t7);
        mStringBuilder = new StringBuilder(mTextEthernetTitle.getText().toString());
        renameNumIndex = mStringBuilder.indexOf("7");
        if (renameNumIndex != -1) {
            mStringBuilder.setCharAt(renameNumIndex, '2');
        }
        mTextEthernetTitle.setText(mStringBuilder.toString());

        mTextEthernet = (TextView) findViewById(R.id.pogo_dock_hint);
        mTextCharge = (TextView) findViewById(R.id.pogo_charge_result);
        mTextEthernetIpAddress = (TextView) findViewById(R.id.pogo_ethernet_ipconfig);
        mTextUsbPluggedState = (TextView) findViewById(R.id.pogo_usb_plugged_state);

        chargeTest = (Button) findViewById(R.id.pogo_charge_btn);
        chargeTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEnabled(false);//Host Disable
                ethControl(false, mContext);
                batteryCharing();
            }
        });

        ethernetTest = (Button) findViewById(R.id.pogo_ethernet_btn);
        ethernetTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEnabled(true);
                ethControl(true, mContext);
                syscEthInfo();
            }
        });

        Button pass = (Button) findViewById(R.id.pass);
        pass.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                pass();
            }
        });
        Button fail = (Button) findViewById(R.id.fail);
        fail.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                fail(null);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mEthernetManager.addListener(mEthernetListener);
        syscEthInfo();
        registerReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver();
        // TODO —— SQ57 not to stop host when onPause(),cause ethernet state change will trigger onPause()
        if (FactoryKitPro.PRODUCT_SQ57) {

        } else {
            setEnabled(false);//Host Disable
            ethControl(false, mContext);
        }

        mEthernetManager.removeListener(mEthernetListener);
    }

    @Override
    public void finish() {
        setEnabled(false);//Host Disable
        ethControl(false, mContext);
        unregisterReceiver();
        mEthernetManager.removeListener(mEthernetListener);
        super.finish();
    }

    /**
     * 获取节点的状态值
     * @param path 节点路径
     * @return String 返回节点内容
     */
    private String getBatteryInfo(String path) {
        File mFile;
        FileReader mFileReader;
        mFile = new File(path);

        try {
            mFileReader = new FileReader(mFile);
            char data[] = new char[128];
            int charCount;
            String status[] = null;
            try {
                charCount = mFileReader.read(data);
                status = new String(data, 0, charCount).trim().split("\n");// Maybe data has "\n"
                logd(status[0]);
                return status[0];
            } catch (IOException e) {
                loge(e);
            }
        } catch (FileNotFoundException e) {
            loge(e);
        }
        return null;
    }

    void fail(Object msg) {
        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG, "Failed");
        finish();
    }

    void pass() {
        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG, "Pass");
        finish();
    }


    public void toast(Object s) {
        if (s == null)  return;
        if (mToast == null) {
            mToast = Toast.makeText(this, s + "", Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(String.valueOf(s));
        }
    }

    private static void loge(Object e) {
        if (e == null)  return;

        Log.e(TAG, e + "");
    }

    @SuppressWarnings("unused")
    private static void logd(Object s) {
        if (s == null)  return;

        Log.d(TAG, s + "");
    }

    /**
     * 获取以太网eth0 MAC地址 IP地址
     * @return
     * @TextView mTextEthernet          显示获取到的Mac地址
     * @TextView mTextEthernetIpAddress 显示获取到的IP地址
     */
    public void syscEthInfo() {
        logd("PogoPinPoor > syscEthInfo");

        try {
            // 获得以太网eth0 MAC地址
            mStringBuilder = new StringBuilder(getResources().getString(R.string.pogo_ethernet_result_txt));
            String ethernetMacAddr = getMacAddress("eth0");
            if (!TextUtils.isEmpty(ethernetMacAddr)) {
                mTextEthernet.setTextColor(Color.GREEN);
                mStringBuilder.append(" " + ethernetMacAddr);
            } else {
                mTextEthernet.setTextColor(Color.RED);
                mStringBuilder.append(" reading ...");
            }
            mTextEthernet.setText(mStringBuilder.toString());

            // 获得以太网eth0 IP地址
            mStringBuilder = new StringBuilder(getResources().getString(R.string.pogo_ethernet_ip_address));
            String ethernetIpAddress = getIpAddress("eth0");
            if (!TextUtils.isEmpty(ethernetIpAddress)) {
                mTextEthernetIpAddress.setTextColor(Color.GREEN);
                mStringBuilder.append(" " + ethernetIpAddress);
            } else {
                mTextEthernetIpAddress.setTextColor(Color.RED);
                mStringBuilder.append(" " + "reading ...");
            }
            mTextEthernetIpAddress.setText(mStringBuilder.toString());
        } catch (Exception e) {
            loge("Maybe not plug in Netting Twine ");
        }
    }

    /**
     * 使能USB Host功能
     * @param enabled 开关
     * 注: USB Host节点 不同项目生效的值可能不同
     *    有的项目写 1 生效， 写 2 不生效(如SQ57)，有的项目写 2 生效
     *    后续有问题需要加以区分判断，或者做一个自适应的优化判断
     */
    public void setEnabled(boolean enabled) {
        FileOutputStream outputStream = null;
        try {
            String flagHost = loadFileAsString(PATH_HOST);
            if (!TextUtils.isEmpty(flagHost)) {
                flagHost = flagHost.trim();
                if (enabled == "1".equals(flagHost)) {
                    // not to reset host again!!!
                    return;
                }
            }

            outputStream = new FileOutputStream(PATH_HOST);
            outputStream.write(Integer.toString(enabled ? 1 : 0).getBytes());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            loge(e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    loge(e.getMessage());
                }
            }
        }
        Settings.System.putInt(getApplicationContext().getContentResolver(), "sys.docker.function", enabled ? 1 : 0);
    }

    private void batteryCharing() {
        boolean ret = false;
        String tmp = null;
        float voltage = 0;
        String result = "";

        tmp = getBatteryInfo(PATH_STATUS);
        if (tmp != null) {
            result += tmp + "\n";
            if ("Charging".equals(tmp) || "Full".equals(tmp)) {
                ret = true;
            }
            else
                ret = false;
        }
        tmp = getBatteryInfo(PATH_VOLTAGE_NOW);
        if (tmp != null) {
            voltage = Float.valueOf(tmp);
            if (voltage > 1000000)
                voltage = voltage / 1000000;
            else if (voltage > 1000)
                voltage = voltage / 1000;
            result += (getString(R.string.battery_voltage)) + voltage + "V";

        }
        mTextCharge.setText(result);
        mTextCharge.setTextColor(ret ? Color.GREEN : Color.RED);
    }

    private void ethControl(boolean ethToggleEnabled, Context context) {
        // avoid to restart ethernet
        boolean ethernetEnabled = mEthernetManager.isAvailable("eth0");
        if (ethernetEnabled == ethToggleEnabled) {
            return;
        }

        SystemProperties.set("persist.sys.ethernet.mode", ethToggleEnabled ? "true" : "false");
        try {
            mEthernetManager.updateIface("eth0", ethToggleEnabled);
        } catch (NoSuchMethodError ex) {
            loge(ex.getMessage());
            loge("NoSuchMethodError occured, maybe this os doesn't have this method indeed");
            loge("NoSuchMethodError -------> not need to resolve it if function is ok!!!");
        } catch (Exception e) {
            loge(e.getMessage());
        }

        Settings.System.putInt(context.getContentResolver(), "sys.ethernet.switch", ethToggleEnabled ? 1 : 0);
        Intent intent = new Intent(ETHERNET_CHANGED);
        context.sendBroadcast(intent);
    }

    public static String loadFileAsString(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while ((numRead=reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }

    /**
     * 获取指定网卡ip
     * @param netInterface 网卡名
     * @return String 返回IP地址
     * @permission android.permission.INTERNET
     */
    public static String getIpAddress(String netInterface) {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                if (ni.getName().equals(netInterface)) {
                    Enumeration<InetAddress> ias = ni.getInetAddresses();
                    while (ias.hasMoreElements()) {
                        ia = ias.nextElement();
                        if (ia instanceof Inet6Address) {
                            continue;// skip ipv6
                        }
                        String ip = ia.getHostAddress();
                        // 过滤掉127段的ip地址
                        if (!"127.0.0.1".equals(ip)) {
                            hostIp = ia.getHostAddress();
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            loge(e.getMessage());
        }
        return hostIp;
    }

    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_BATTERY_CHANGED: // 检测电源状态改变
                    int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
                    boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
                    if (usbCharge) {
                        mTextUsbPluggedState.setText(getResources().getString(R.string.pogo_usb_plugged_state)
                                + " " + getResources().getString(R.string.plugged_usb));
                    } else if (acCharge) {
                        mTextUsbPluggedState.setText(getResources().getString(R.string.pogo_usb_plugged_state)
                                + " " + getResources().getString(R.string.plugged_ac));
                    } else {
                        mTextUsbPluggedState.setText(getResources().getString(R.string.pogo_usb_plugged_state)
                                + " " + getResources().getString(R.string.unknown_state));
                        loge("this state is unknown or not need to show");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    BroadcastReceiver mNetChangeReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            syscEthInfo();
        }
    };

    public void registerReceiver() {
        if (isReceiverRegister) return;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        IntentFilter networkfilter = new IntentFilter();
        networkfilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        isReceiverRegister = true;
        this.registerReceiver(mUsbReceiver, filter);
        this.registerReceiver(mNetChangeReceiver, networkfilter);
    }

    public void unregisterReceiver() {
        if (!isReceiverRegister) return;

        this.unregisterReceiver(mUsbReceiver);
        this.unregisterReceiver(mNetChangeReceiver);
        isReceiverRegister = false;
    }

    /**
     * 获取指定网卡Mac地址
     * @param netInterface 网卡名
     * @return String 返回Mac地址
     * @permission android.permission.INTERNET
     */
    public static String getMacAddress(String netInterface) {
        String ethernetMacAddr = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                if (ni.getName().equals(netInterface)) {
                    byte[] macAddrBuf = ni.getHardwareAddress();
                    ethernetMacAddr = byteHexStringUpper(macAddrBuf);
                    // format ethernetMacAddr
                    ethernetMacAddr = formatMac(ethernetMacAddr, ":");
                }
            }
        } catch (Exception e) {
            // maybe throws SocketException IllegalArgumentException
            loge(e.getMessage());
        }

        return ethernetMacAddr;
    }

    /**
     * 字节数组转16进制字符串
     * @param array   待转换的字节数组
     * @return String 返回转换后大写的16进制字符串
     */
    public static String byteHexStringUpper(byte[] array) {
        StringBuilder builder = new StringBuilder();

        for (byte b : array) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            builder.append(hex);
        }

        return builder.toString().toUpperCase();
    }

    /**
     * 格式化MAC地址
     * @param mac   待转换的Mac地址
     * @param split 分隔符号
     * @return String 返回格式化后的Mac地址
     */
    public static String formatMac(String mac, String split) throws IllegalArgumentException {
        String regex = "[0-9a-fA-F]{12}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(mac);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("mac format is error");
        }

        StringBuilder ans = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            char c = mac.charAt(i);
            ans.append(c);

            if ((i & 1) == 1 && i <= 9) {
                ans.append(split);
            }
        }

        return ans.toString();
    }
}