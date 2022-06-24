package com.ubx.usdk.profile;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.IConnectivityManager;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.Network;
import android.net.NetworkTemplate;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Telephony;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.util.Log;

import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnProfile;
import com.ubx.usdk.profile.aidl.INetworkPolicy;

import java.util.List;

import org.json.JSONObject;

import android.device.SettingsUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import android.device.admin.SettingProperty;

/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class NetworkImpl extends INetworkPolicy.Stub {
	private final static String TAG = NetworkImpl.class.getSimpleName();
	
	private final static Uri ALL_APN_URI = Uri.parse("content://telephony/carriers");
    private final static Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
	
	private Context mContext;
	private ContentResolver mSystemCR; 
	
	public NetworkImpl(Context context) {
		mContext = context;
		mSystemCR = mContext.getContentResolver();
	}

    @Override
    public boolean setHotpotInfo(String name, String password, int mode) throws RemoteException {
        WifiConfiguration apConfig = new WifiConfiguration();
        apConfig.SSID = name;
        apConfig.preSharedKey=password;
        apConfig.allowedKeyManagement.set(mode);
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiApConfiguration(apConfig);
        return true;
    }

    @Override
    public void setAPN(String name, String apn, String proxy, String port, String user, String server, String password, String mmsc, String mcc, String mnc, String mmsproxy, String mmsport, int authtype, String type, String protocol, int bearer, String roamingprotocol, boolean current) throws RemoteException {
        ContentValues values = new ContentValues();
        values.put(Telephony.Carriers.NAME, name);
        values.put(Telephony.Carriers.APN, apn);
        values.put(Telephony.Carriers.PROXY, proxy);
        values.put(Telephony.Carriers.PORT, port);
        values.put(Telephony.Carriers.MMSPROXY, mmsproxy);
        values.put(Telephony.Carriers.MMSPORT, mmsport);

        values.put(Telephony.Carriers.USER, user);
        values.put(Telephony.Carriers.SERVER, server);
        values.put(Telephony.Carriers.PASSWORD, password);
        values.put(Telephony.Carriers.MMSC, mmsc);
        values.put(Telephony.Carriers.AUTH_TYPE, authtype);

        values.put(Telephony.Carriers.PROTOCOL, protocol);
        values.put(Telephony.Carriers.ROAMING_PROTOCOL, roamingprotocol);

        values.put(Telephony.Carriers.TYPE, type);

        values.put(Telephony.Carriers.MCC, mcc);
        values.put(Telephony.Carriers.MNC, mnc);

        values.put(Telephony.Carriers.NUMERIC, mcc + mnc);
        if (current)
            values.put(Telephony.Carriers.CURRENT, 1);
        values.put(Telephony.Carriers.BEARER, bearer);

        long id = Binder.clearCallingIdentity();

        mSystemCR.delete(getUri(ALL_APN_URI), " name = ?", new String[]{name});

        Uri newUri = mSystemCR.insert(getUri(ALL_APN_URI), values);
        if (current) {
            try {
                long apn_id = ContentUris.parseId(newUri);
                values.clear();
                values.put("apn_id", String.valueOf(apn_id));
                mContext.getContentResolver().update(getUri(PREFERRED_APN_URI), values, null, null);
            } catch (UnsupportedOperationException e) {
                //System.out.println(e);
            } catch (NumberFormatException ex) {
                // TODO: handle exception
            }

        }
    }
    
    private Uri getUri(Uri uri) {
        return Uri.withAppendedPath(uri, "/subId/" + SubscriptionManager.getDefaultDataSubscriptionId());
    }

    @Override
    public int deleteAPN(String where, String[] whereArgs) throws RemoteException {
        int result = mSystemCR.delete(getUri(ALL_APN_URI), where, whereArgs);

        return result;
    }

    @Override
    public String queryAPN(String selection, String[] selectionArgs) throws RemoteException {
        final String[] projection = new String[]{
                Telephony.Carriers._ID,
                Telephony.Carriers.NAME,
                Telephony.Carriers.APN,
                Telephony.Carriers.MCC,
                Telephony.Carriers.MNC,
                Telephony.Carriers.NUMERIC,
                Telephony.Carriers.PROXY,
                Telephony.Carriers.PORT,
                Telephony.Carriers.MMSPROXY,
                Telephony.Carriers.MMSPORT,
                Telephony.Carriers.MMSC,
                Telephony.Carriers.SERVER,
                Telephony.Carriers.PASSWORD,
                Telephony.Carriers.AUTH_TYPE,
                Telephony.Carriers.TYPE,
                Telephony.Carriers.USER,
                Telephony.Carriers.CURRENT
        };
        Cursor cursor = mSystemCR.query(getUri(ALL_APN_URI), projection, selection, selectionArgs, Telephony.Carriers.DEFAULT_SORT_ORDER);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put(Telephony.Carriers._ID, cursor.getString(0));
                jsonObj.put(Telephony.Carriers.NAME, cursor.getString(1));
                jsonObj.put(Telephony.Carriers.APN, cursor.getString(2));
                jsonObj.put(Telephony.Carriers.MCC, cursor.getString(3));
                jsonObj.put(Telephony.Carriers.MNC, cursor.getString(4));
                jsonObj.put(Telephony.Carriers.NUMERIC, cursor.getString(5));
                jsonObj.put(Telephony.Carriers.PROXY, cursor.getString(6));
                jsonObj.put(Telephony.Carriers.PORT, cursor.getString(7));
                jsonObj.put(Telephony.Carriers.MMSPROXY, cursor.getString(8));
                jsonObj.put(Telephony.Carriers.MMSPORT, cursor.getString(9));
                jsonObj.put(Telephony.Carriers.MMSC, cursor.getString(10));
                jsonObj.put(Telephony.Carriers.SERVER, cursor.getString(11));
                jsonObj.put(Telephony.Carriers.PASSWORD, cursor.getString(12));
                jsonObj.put(Telephony.Carriers.AUTH_TYPE, cursor.getString(13));
                jsonObj.put(Telephony.Carriers.TYPE, cursor.getString(14));
                jsonObj.put(Telephony.Carriers.USER, cursor.getString(15));
                jsonObj.put(Telephony.Carriers.CURRENT, cursor.getString(16));
                return jsonObj.toString();
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public int getCurrentApn() throws RemoteException {
        int apnid = -1;
        String str = "";
        ContentResolver resolver = mContext.getContentResolver();
        Cursor c = resolver.query(getUri(PREFERRED_APN_URI), null, null, null, null);
        while (c != null && c.moveToNext()) {
            str = c.getString(c.getColumnIndex("_id"));
            apnid = Integer.valueOf(str);
        }
        return apnid;
    }

    @Override
    public boolean setCurrentApn(int appid) throws RemoteException {
        boolean res = false;
        ContentValues values = new ContentValues();
        values.put("apn_id", appid);

        try {
            mSystemCR.update(getUri(PREFERRED_APN_URI), values, null, null);
            Cursor c = mSystemCR.query(getUri(PREFERRED_APN_URI), new String[]{"name",
                    "apn"}, "_id=" + appid, null, null);
            if (c != null) {
                res = true;
                c.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public void connectVPN(String name, int type, String server, String username, String password, String dnsServers, String searchDomains, String routes, boolean mppe, String l2tpSecret, String ipsecIdentifier, String ipsecSecret, String ipsecUserCert, String ipsecCaCert, String ipsecServerCert) throws RemoteException {
        VpnProfile profile = new VpnProfile("defaultvpn");
        profile.name = name;
        profile.type = type;
        profile.server = server;
        profile.username = username;
        profile.password = password;
        profile.dnsServers = dnsServers;
        profile.searchDomains = searchDomains;
        profile.routes = routes;
        profile.mppe = mppe;
        profile.l2tpSecret = l2tpSecret;
        profile.ipsecIdentifier = ipsecIdentifier;
        profile.ipsecSecret = ipsecSecret;
        profile.ipsecUserCert = ipsecUserCert;
        profile.ipsecCaCert = ipsecCaCert;
        profile.ipsecServerCert = ipsecServerCert;

        IConnectivityManager mConnectivityService = IConnectivityManager.Stub.asInterface(
            ServiceManager.getService(Context.CONNECTIVITY_SERVICE));
        try {
            mConnectivityService.startLegacyVpn(profile);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to connect", e);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to connect", e);
        }
    }

    @Override
    public int getVpnState() throws RemoteException {
        int ret = 0;

        IConnectivityManager mConnectivityService = IConnectivityManager.Stub.asInterface(
            ServiceManager.getService(Context.CONNECTIVITY_SERVICE));
        try {
            LegacyVpnInfo connected = mConnectivityService.getLegacyVpnInfo(UserHandle.USER_SYSTEM);
            if(connected == null) {
                ret = 0;
            } else if("defaultvpn".equals(connected.key)) {
                ret = 1;
            } else {
                ret = 2;
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to prepareVpn", e);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to prepareVpn", e);
        }

        return ret;
    }

    @Override
    public void disconnectVpn() throws RemoteException {
        IConnectivityManager mConnectivityService = IConnectivityManager.Stub.asInterface(
            ServiceManager.getService(Context.CONNECTIVITY_SERVICE));
        try {
            mConnectivityService.prepareVpn(null, VpnConfig.LEGACY_VPN, UserHandle.USER_SYSTEM);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Failed to prepareVpn", e);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to prepareVpn", e);
        }
    }

    @Override
    public long getTrafficInfo(long startTime, long endTime, int type) throws RemoteException {
        String[] trafficInfo = new String[4];
        try {
            INetworkStatsService statsService = INetworkStatsService.Stub.asInterface(
                    ServiceManager.getService(Context.NETWORK_STATS_SERVICE));
            statsService.forceUpdate();
            //statsService.forceUpdateIfaces(new Network[]{new Network(100), new Network(101)});
            INetworkStatsSession session = statsService.openSession();

            long wifiBytes = session.getSummaryForNetwork(NetworkTemplate.buildTemplateWifiWildcard(), startTime, endTime).getTotalBytes();
            long mobileBytes = session.getSummaryForNetwork(NetworkTemplate.buildTemplateMobileWildcard(), startTime, endTime).getTotalBytes();
            if (type == 0) {
                return wifiBytes + mobileBytes;
            } else if (type == 1) {
                return wifiBytes;
            } else if (type == 2) {
                return mobileBytes;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long getAppTrafficInfo(String packagename, int type) throws RemoteException {
        long totalBytes = -1;
        PackageManager packageManager = mContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packagename, PackageManager.GET_META_DATA);
            int uid = packageInfo.applicationInfo.uid;

            totalBytes = android.net.TrafficStats.getUidRxBytes(uid) + android.net.TrafficStats.getUidTxBytes(uid);
        } catch (PackageManager.NameNotFoundException e) {
            return totalBytes;
        }
        
        return totalBytes;
    }

    @Override
    public void setFirewallUidChainRule(int uid, int networkType, boolean allow) throws RemoteException {
        INetworkManagementService mNetworkService = INetworkManagementService.Stub.asInterface(
                    ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
        try {
            mNetworkService.setFirewallUidChainRule(uid, networkType, allow);
        } catch(RemoteException e) {
            e.printStackTrace();
        };
    }

    @Override
    public void clearFirewallChain(String chain) throws RemoteException {
        INetworkManagementService mNetworkService = INetworkManagementService.Stub.asInterface(
                    ServiceManager.getService(Context.NETWORKMANAGEMENT_SERVICE));
        try {
            mNetworkService.setFirewallUidChainRule(10000, -1, true);
        } catch(RemoteException e) {
            e.printStackTrace();
        };
    }

    @Override
    public void setWifiWhiteSsids(String[] ssids, int mode, int action) throws RemoteException{
        String filepath = SettingProperty.WIFI_WHITELIST_FILE;
        if(mode == 0) {
            filepath = SettingProperty.WIFI_WHITELIST_FILE;
        } else if(mode == 1) {
            filepath = SettingProperty.WIFI_BLACKLIST_FILE;
        }

        ArrayList<String> currentList = SettingsUtils.readStringListFormFile(filepath);
        Set<String> stringSet = new HashSet<>(currentList);

        if(action == 0) {
            File file = new File(filepath);
            if (file.exists() && file.isFile()){
                if (file.delete()) {
                    Log.i(TAG,"Successful deletion of files:"+filepath);
                }
            }
        } else if(action == 1) {
            if(ssids != null && ssids.length != 0) {
                List<String> ssidList = Arrays.asList(ssids);
                stringSet.addAll(ssidList);
                SettingsUtils.writeListStringToFile(new ArrayList<>(stringSet), filepath);
            }
        } else if(action == 2) {
            if(ssids != null && ssids.length != 0) {
                List<String> ssidList = Arrays.asList(ssids);
                stringSet.removeAll(ssidList);
                SettingsUtils.writeListStringToFile(new ArrayList<>(stringSet), filepath);
            }
        }

        ArrayList<String> list = SettingsUtils.readStringListFormFile(SettingProperty.WIFI_WHITELIST_FILE);
        ArrayList<String> blacklist = SettingsUtils.readStringListFormFile(SettingProperty.WIFI_BLACKLIST_FILE);
        //获取已保存wifi信息networksList，白名单list
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        List<WifiConfiguration> networksList =  mWifiManager.getConfiguredNetworks();
        if (list!=null && networksList!=null && list.size()>0 && networksList.size()>0 ){
            //Log.d(TAG,"networksList :"+networksList.size()+"  list:"+list.size());
            for (WifiConfiguration wifiConfiguration:networksList){
                boolean wifienable = false;//是否在白名单里面,
                if(!list.contains(wifiConfiguration.SSID.replace("\"", ""))){
                    mWifiManager.removeNetwork(wifiConfiguration.networkId);
                }
                if(blacklist.contains(wifiConfiguration.SSID.replace("\"", ""))){
                    mWifiManager.removeNetwork(wifiConfiguration.networkId);
                }
            }
        }
    }
	
	private static final String PERSIST_5G = "persist.sys.is_5g";
    private static final String PERSIST_24G = "persist.sys.is_24g";
    private static final String PERSIST_VENDOR_5G = "persist.vendor.sys.channel_5g_wifi";
    private static final String PERSIST_VENDOR_24G = "persist.vendor.sys.channel_24g_wifi";
    private static final String PERSIST_VENDOR_all = "persist.vendor.sys.channel_all_wifi";
    private static final String SYSTEM_CHANNEL_24G = "wifi_24.config";
    private static final String SYSTEM_CHANNEL_5G = "wifi_5.config";
    private static final String PERSIST_CHANNEL_5G = "persist.sys.wifi_5.config";
    private static final String PERSIST_CHANNEL_24G = "persist.sys.wifi_24.config";
    private static final String WIFI_BAND = "ap_frequency_band";
    private static final String hardware = android.os.Build.HARDWARE;
    private static final String iniPath_mtk = "/data/system/wifi.cfg";
    private static final String iniPath_diff_mtk = "/data/system/wifi_diff.cfg";
    private static final String ROAM_DIFF_MTK0 = "RoamingRCPIDelta0";
    private static final String ROAM_DIFF_MTK1 = "RoamingRCPIDelta1";
    private static final String ROAM_THRESHOLD_MTK0 = "RoamingRCPIGoodValue";
    private static final String ROAM_THRESHOLD_MTK1 = "RoamingRCPIPoorValue";
    private static final String POWER_SAVE_MTK = "gParamPowerMode";
    private static final String iniPath_qcom = "/vendor/etc/wifi/WCNSS_qcom_cfg.ini";
    private static final String ROAM_DIFF_QCOM = "RoamRssiDiff";
    private static final String ROAM_THRESHOLD_QCOM = "gNeighborLookupThreshold";
    private static final String POWER_SAVE_QCOM = "gEnableBmps";
    private static final String ROAM_DIFF_DEFAULT = "3";
    private static final String ROAM_THRESHOLD_DEFAULT = "65";
    private static final String POWER_SAVE_DEFAULT = "1";

	public void resetWifi(){
        Log.d(TAG, "resetWifi: ");
		WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        mWifiManager.setWifiEnabled(false);
        Handler handler = new Handler(mContext.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWifiManager.setWifiEnabled(true);
            }
        }, 1000);

    }

	/**
     *??wifi??
     * value:0 ->???1 -> 5GHZ, 2 -> 2.4GHZ
     */
    public void setWifiBand(int value) {
        switch (value)
        {
            case 0:
                SystemProperties.set(PERSIST_5G, "0");
                SystemProperties.set(PERSIST_24G, "0");
                SystemProperties.set(PERSIST_VENDOR_5G, "0");
                SystemProperties.set(PERSIST_VENDOR_24G, "0");
                SystemProperties.set(PERSIST_VENDOR_all, "1");
                Settings.Global.putString(mSystemCR,WIFI_BAND, "7");
                resetWifi();
                break;
            case 1:
                SystemProperties.set(PERSIST_5G, "1");
                SystemProperties.set(PERSIST_24G, "0");
                SystemProperties.set(PERSIST_VENDOR_5G, "1");
                SystemProperties.set(PERSIST_VENDOR_24G, "0");
                SystemProperties.set(PERSIST_VENDOR_all, "0");
                Settings.Global.putString(mSystemCR,WIFI_BAND, "6");
                resetWifi();
                break;
            case 2:
                SystemProperties.set(PERSIST_5G, "0");
                SystemProperties.set(PERSIST_24G, "1");
                SystemProperties.set(PERSIST_VENDOR_5G, "0");
                SystemProperties.set(PERSIST_VENDOR_24G, "1");
                SystemProperties.set(PERSIST_VENDOR_all, "0");
                Settings.Global.putString(mSystemCR,WIFI_BAND, "1");
                resetWifi();
                break;
            default:
                SystemProperties.set(PERSIST_5G, "0");
                SystemProperties.set(PERSIST_24G, "0");
                SystemProperties.set(PERSIST_VENDOR_5G, "0");
                SystemProperties.set(PERSIST_VENDOR_24G, "0");
                SystemProperties.set(PERSIST_VENDOR_all, "1");
                Settings.Global.putString(mSystemCR,WIFI_BAND, "7");
                resetWifi();
                break;
        }
    }
	 
	 /**
     * ??wifi??
     * return 0 ->???1 -> 5GHZ, 2 -> 2.4GHZ
     * */
    public int getWifiBand(){
        String wBandStr = "0";
        int wBand = 0;
        wBandStr = Settings.Global.getString(mSystemCR,WIFI_BAND);
        if (wBandStr != null) {
            wBand = Integer.parseInt(wBandStr);
            switch (wBand)
            {
                case 1:
                    return 2;
                case 6:
                    return 1;
                case 7:
                    return 0;
            }
        }
        return wBand;
    }
	
	/**
     * ??wifi??
     * band(??): 1?> 5g , 2 -->2.4g
     * value: ???
     * */
    public void setWifiChannel(int band,String value) {
        if (band == 1) {
            SystemProperties.set(PERSIST_CHANNEL_5G, value);
            Settings.System.putString(mSystemCR,SYSTEM_CHANNEL_5G, value);
        } else if (band == 2){
            SystemProperties.set(PERSIST_CHANNEL_24G, value);
            Settings.System.putString(mSystemCR,SYSTEM_CHANNEL_24G, value);
        } else {
            Log.e(TAG, "setWifiChannel error because band is wrong");
        }
        resetWifi();
    }
	
	/**
     * ??wifi??
     * band(??): 1?> 5g , 2 -->2.4g
     * return: 0,??  ??  ?????
     * */
    public String getWifiChannel(int band) {
        String mChannel = "0";
        if (band == 1) {
            mChannel = Settings.System.getString(mSystemCR,SYSTEM_CHANNEL_5G);
        } else if (band == 2){
            mChannel = Settings.System.getString(mSystemCR,SYSTEM_CHANNEL_24G);
        }
        return mChannel != null ? mChannel : "0";
    }
	
	private static void shellurovo(String cmd) {
        try {
            java.lang.Process p = Runtime.getRuntime().exec(cmd);
            int test = p.waitFor();
            //Log.i(TAG, "test=:" + test);
        } catch (Exception e) {
            //Log.i(TAG, "e:" + e);
        }
    }
	
	public static boolean writeCfgFile(String path, String content) {
		Object mLock = new Object();
        synchronized (mLock) {
            if (content == null || content.isEmpty() || path == null || path.isEmpty()) {
                Log.e(TAG, "content is null!");
                return false;
            }

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(path));
                bw.write(content);
                bw.close();

                // set file mode 664
                String command = "chmod 664 " + path;
                Runtime runtime = Runtime.getRuntime();
                java.lang.Process prc = runtime.exec(command);
            } catch (IOException e) {
                Log.d(TAG, e.toString());
            }
            return true;
        }
    }
	
	public  int locationBlanks(String s) {
        int location = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ' ' || s.charAt(i) == '\t') {
                location = i;
                break;
            }
        }
        return location;
    }
	
	 /**
     * ??????
     * threshold:??  diff??????  powerSave?power save?????0???1???53H(MTK) 0???2???
     * */
    public void setRoamingConfig(int threshold,int diff, int powerSave) {
        if (hardware.contains("mt")) {
            StringBuffer sb = new StringBuffer()
                    .append("RoamingCustomization 1")
                    .append("\n")
                    .append("RoamingRCPIGoodValue " + threshold)
                    .append("\n")
                    .append("RoamingRCPIPoorValue " + threshold)
                    .append("\n")
                    .append("RoamingRCPIDelta0 " + diff)
                    .append("\n")
                    .append("RoamingRCPIDelta1 " + diff)
                    .append("\n");
            writeCfgFile("/data/system/wifi.cfg", sb.toString());
            writeCfgFile("/data/system/wifi_diff.cfg", "gParamPowerMode " + powerSave);
        } else {
            shellurovo("/system/xbin/iwpriv wlan0 setConfig RoamRssiDiff=" + diff);
            shellurovo("/system/xbin/iwpriv wlan0 setConfig gNeighborLookupThreshold=" + threshold);
            shellurovo("/system/xbin/iwpriv wlan0 setConfig gEnableBmps=" + powerSave);
            shellurovo("/system/xbin/iwpriv    wlan0  getConfig");
        }
        Settings.Global.putString(mSystemCR, "wifi_signal_delta", diff + "");
        Settings.Global.putString(mSystemCR, "wifi_signal_trigger", threshold + "");
        Settings.Global.putString(mSystemCR, "wifi_power_save", powerSave + "");
    }
	
	/**
     * ??????
     * */
    public int getRoamingThreshold() {
        String value = Settings.Global.getString(mSystemCR, "wifi_signal_trigger");
        if (value != null && value.length() > 0) return Integer.parseInt(value);
        BufferedReader br = null;
        String iniPath = (hardware.contains("mt")) ? iniPath_mtk : iniPath_qcom;
        String type = (hardware.contains("mt")) ? ROAM_THRESHOLD_MTK0 : ROAM_THRESHOLD_QCOM;
        try {
            br = new BufferedReader(new FileReader(new File(iniPath)));
            for (String str = br.readLine(); str != null; str = br.readLine()) {
                if (str.contains(type)) {
                    value = str;
                    break;
                }
            }

            if (value.contains("#")) {
                //#BandCapability=0
                if (value.startsWith("#"))
                    value = "";
                else {
                    //BandCapability=0 #5G  2.4G
                    value = value.substring(value.indexOf("=") + 1, locationBlanks(value));
                }
            } else if (value.contains(" ")) {
                //for ini config like
                //BandCapability 0
                value = value.substring(value.indexOf(" ") + 1, value.length());
            } else {
                //for ini config like
                //BandCapability=0
                value = value.substring(value.indexOf("=") + 1, value.length());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        if (value == null || value.length() <= 0) {
            if (type.equals(ROAM_THRESHOLD_MTK0) || type.equals(ROAM_THRESHOLD_MTK1) || type.equals(ROAM_THRESHOLD_QCOM)) {
                value = ROAM_THRESHOLD_DEFAULT;
            }
        } else {
            Settings.Global.putString(mSystemCR, "wifi_signal_trigger_default", value);
        }

        return Integer.parseInt(value);
    }
	
	/**
     * ???????
     * */
    public int getRoamingThresholdDiff() {
        String value = Settings.Global.getString(mSystemCR, "wifi_signal_delta");
        if (value != null && value.length() > 0) return Integer.parseInt(value);
        BufferedReader br = null;
        String iniPath = (hardware.contains("mt")) ? iniPath_mtk : iniPath_qcom;
        String type = (hardware.contains("mt")) ? ROAM_DIFF_MTK0 : ROAM_DIFF_QCOM;
        try {
            br = new BufferedReader(new FileReader(new File(iniPath)));
            for (String str = br.readLine(); str != null; str = br.readLine()) {
                if (str.contains(type)) {
                    value = str;
                    break;
                }
            }

            if (value.contains("#")) {
                //#BandCapability=0
                if (value.startsWith("#"))
                    value = "";
                else {
                    //BandCapability=0 #5G  2.4G
                    value = value.substring(value.indexOf("=") + 1, locationBlanks(value));
                }
            } else if (value.contains(" ")) {
                //for ini config like
                //BandCapability 0
                value = value.substring(value.indexOf(" ") + 1, value.length());
            } else {
                //for ini config like
                //BandCapability=0
                value = value.substring(value.indexOf("=") + 1, value.length());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        if (value == null || value.length() <= 0) {
            if (type.equals(ROAM_DIFF_MTK0) || type.equals(ROAM_DIFF_MTK1) || type.equals(ROAM_DIFF_QCOM)) {
                value = ROAM_DIFF_DEFAULT;
            }
        } else {
            Settings.Global.putString(mSystemCR, "wifi_signal_delta_default", value);
        }

        return Integer.parseInt(value);
    }
	
	 /**
     * ??power save??
     * */
    public int getPowerSaveValue() {
        String value = Settings.Global.getString(mSystemCR, "wifi_power_save");
        if (value != null && value.length() > 0) return Integer.parseInt(value);
        BufferedReader br = null;
        String iniPath = (hardware.contains("mt")) ? iniPath_diff_mtk : iniPath_qcom;
        String type = (hardware.contains("mt")) ? POWER_SAVE_MTK : POWER_SAVE_QCOM;
        try {
            br = new BufferedReader(new FileReader(new File(iniPath)));
            for (String str = br.readLine(); str != null; str = br.readLine()) {
                if (str.contains(type)) {
                    value = str;
                    break;
                }
            }

            if (value.contains("#")) {
                if (value.startsWith("#"))
                    value = "";
                else {
                    value = value.substring(value.indexOf("=") + 1, locationBlanks(value));
                }
            } else if (value.contains(" ")) {
                //for ini config like
                //BandCapability 0
                value = value.substring(value.indexOf(" ") + 1, value.length());
            } else {
                //for ini config like
                //BandCapability=0
                value = value.substring(value.indexOf("=") + 1, value.length());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        if (value == null || value.length() <= 0) {
            value = POWER_SAVE_DEFAULT;
        } else {
            Settings.Global.putString(mSystemCR, "wifi_power_save_default", value);
        }
        return Integer.parseInt(value);
    }

	 /**
     * ????(???????????Power Save ??)
     * */
    public void resetRoamingConfig() {
        getRoamingThreshold();
        getRoamingThresholdDiff();
        getPowerSaveValue();
        String roamDefalut = Settings.Global.getString(mSystemCR, "wifi_signal_trigger_default");
        if (roamDefalut == null) roamDefalut = ROAM_THRESHOLD_DEFAULT;
        String roamDiffDefault = Settings.Global.getString(mSystemCR, "wifi_signal_delta_default");
        if (roamDiffDefault == null) roamDiffDefault = ROAM_DIFF_DEFAULT;
        String powerSaveDefault = Settings.Global.getString(mSystemCR, "wifi_power_save_default");
        if (powerSaveDefault == null) powerSaveDefault = POWER_SAVE_DEFAULT;
        setRoamingConfig(
                Integer.parseInt(roamDefalut),
                Integer.parseInt(roamDiffDefault),
                Integer.parseInt(powerSaveDefault));
    }
	
}
