package android.device;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.app.usage.UsageStats;
import android.content.ComponentName;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.device.admin.SettingProperty;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.ubx.usdk.profile.aidl.IApplicationPolicy;
import com.ubx.usdk.profile.aidl.IDeviceControlPolicy;
import com.ubx.usdk.profile.aidl.INetworkPolicy;
import com.ubx.usdk.profile.aidl.IProfileManager;
import com.ubx.usdk.profile.aidl.IRestrictionPolicy;
import com.ubx.usdk.profile.aidl.ISecurityPolicy;


import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.app.usage.UsageStatsManager.INTERVAL_BEST;

/**
 * The DeviceManager class provides support to get and set the device configurations.
 *
 * <br>
 * System interface extension:<br>
 * <p>
 * Startup the system to check whether the OTA server has any new updates:
 * <page>
 * </br><code> try {
 * </br>             &nbsp&nbsp&nbsp&nbsp// Get component info and create ComponentName
 * </br>             &nbsp&nbsp&nbsp&nbspString packageName = "com.ubx.update";</code>
 * </br>             &nbsp&nbsp&nbsp&nbspString className = "com.ubx.update.NotificationService";</code>
 * </br>             &nbsp&nbsp&nbsp&nbspComponentName component = new ComponentName(packageName, className);</code>
 * </br>             &nbsp&nbsp&nbsp&nbsp// Create a new intent. Use the old one for extras and such reuse
 * </br>             &nbsp&nbsp&nbsp&nbspIntent explicitIntent = new Intent();</code>
 * </br>             &nbsp&nbsp&nbsp&nbsp// Set the component to be explicit
 * </br>             &nbsp&nbsp&nbsp&nbsp// Set the Extra to be explicit
 * </br>             &nbsp&nbsp&nbsp&nbspexplicitIntent.putExtra("action", "com.ubx.update.BACKGROUND_CHECK");
 * </br>             &nbsp&nbsp&nbsp&nbspexplicitIntent.setComponent(component);
 * </br>              &nbsp&nbsp&nbsp&nbspstartService(explicitIntent);</code>
 * </br> &nbsp}catch (Exception e){</code>
 * </br> &nbsp}</code>
 * </page>
 * </p>
 * <p>
 * Update System FW from local OTA zip file:
 * <page>
 * </br><code> try {
 * </br>             &nbsp&nbsp&nbsp&nbsp// Create a new intent
 * </br>             &nbsp&nbsp&nbsp&nbspIntent explicitIntent = new Intent("com.osupdate.upgraderom");</code>
 * </br>             &nbsp&nbsp&nbsp&nbsp// Set the Extra to be explicit
 * </br>              &nbsp&nbsp&nbsp&nbspintent.putExtra("fullfilename", "sdcard/ota.zip");</code>
 * </br>              &nbsp&nbsp&nbsp&nbspsendBroadcast(intent);</code>
 * </br> &nbsp}catch (Exception e){</code>
 * </br> &nbsp}</code>
 * </page>
 * </p>
 */
public class DeviceManager {
    private static final String TAG = "DeviceManager";

    private IBinder mIProfileBinder;
    private IProfileManager mIProfileManager;

    private IApplicationPolicy mIApplicationPolicy;
    private IDeviceControlPolicy mIDeviceControlPolicy;
    private INetworkPolicy mINetworkPolicy;
    private IRestrictionPolicy mRestrictionPolicy;
    private ISecurityPolicy mSecurityPolicy;

    public DeviceManager() {
        checkServiceAlive();
    }

    private void checkServiceAlive() {
        if(mIProfileBinder == null || !mIProfileBinder.isBinderAlive()) {
            mIProfileBinder = ServiceManager.getService("USDKProfileManager");
            if(mIProfileBinder != null) {
                mIProfileManager = IProfileManager.Stub.asInterface(mIProfileBinder);
            } else {
                Log.e(TAG, "DeviceManager USDKProfileService is null");
                return;
            }
            if (mIProfileManager == null) {
                Log.e(TAG, "DeviceManager IProfileManager is null");
                return;
            }

            try {
                IBinder binderApp = mIProfileManager.getApplicationIBinder();
                if (binderApp != null) {
                    mIApplicationPolicy = IApplicationPolicy.Stub.asInterface(binderApp);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "IApplicationPolicy::init", e);
            }

            try {
                IBinder binderDevice = mIProfileManager.getDeviceControlIBinder();
                if (binderDevice != null) {
                    mIDeviceControlPolicy = IDeviceControlPolicy.Stub.asInterface(binderDevice);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "IDeviceControlPolicy::init", e);
            }

            try {
                IBinder binderNet = mIProfileManager.getNetworkPolicyIBinder();
                if (binderNet != null) {
                    mINetworkPolicy = INetworkPolicy.Stub.asInterface(binderNet);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "INetworkPolicy::init", e);
            }

            try {
                IBinder binderRes = mIProfileManager.getRestrictionIBinder();
                if (binderRes != null) {
                    mRestrictionPolicy = IRestrictionPolicy.Stub.asInterface(binderRes);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "RestrictionPolicy::init", e);
            }

            try {
                IBinder binderSec = mIProfileManager.getSecurityPolicyIBinder();
                if (binderSec != null) {
                    mSecurityPolicy = ISecurityPolicy.Stub.asInterface(binderSec);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "ISecurityPolicy::init", e);
            }
        }
    }

    /**
     * Get the device product serial number
     *
     * @return in the form of 14 alphanumeric characters.
     */
    public String getDeviceId() {
        checkServiceAlive();
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getDeviceId();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Build.SERIAL;
    }

    public String getTIDSN() {
        checkServiceAlive();
        return getDeviceId();
    }

    /**
     * Sets the current wall time, in milliseconds.
     *
     * @param when in milliseconds
     */
    public boolean setCurrentTime(long when) {
        checkServiceAlive();
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.setCurrentTime(when);
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Enable/Disable device HOME KEY
     *
     * @param enable value: false disable the Home KEY function. device reboot the state miss.
     *
     *               <br>
     *               Example in your project Activity:<br>
     *               <code>protected void onCreate(Bundle savedInstanceState) {</code>
     *               <br>
     *               <code> &nbsp&nbspsuper.onCreate(savedInstanceState);</code>
     *               <br>
     *               <code> &nbsp&nbspnew DeviceManager().enableHomeKey(false);</code>
     *               <br>
     *               <code>}</code>
     *               <br>
     *               You can revert the home key when exit your application .
     *               <code> protected void onDestroy() {</code>
     *               <br>
     *               <code> &nbsp&nbspnew DeviceManager().enableHomeKey(true);</code>
     *               <br>
     *               <code>}</code><br>
     */
    public void enableHomeKey(boolean enable) {
        checkServiceAlive();
        String settingstr = "";
        try {
            settingstr = mRestrictionPolicy.getSettingProperty(SettingProperty.System_DisallowedKeycodes);

            if (enable) {
                settingstr = SettingsUtils.removePackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_HOME));
            } else {
                settingstr = SettingsUtils.addPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_HOME));
            }
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_DisallowedKeycodes, settingstr);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Enable/Disable the device status bar
     *
     * @param enable Set to true to enable the status bar.  Set to false to disable the status bar expand.
     *               <p>
     *               Note This status is not persistent.  It will revert to the default condition (which is the enabled state) at th next reboot
     */
    public void enableStatusBar(boolean enable) {
        checkServiceAlive();
        int what = 0;
        if (enable) {
            what &= ~StatusBarManager.DISABLE_EXPAND;
        } else {
            what |= StatusBarManager.DISABLE_EXPAND;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_StatusBarEnable, String.valueOf(what));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the APN (access point name) configurations for mobile data network
     *
     * @param apn             APN name.  Cannot be null.
     * @param authtype        Authentication type. Value:0(none) 1(PAP) 2(CHAP) 3(PAP or CHAP)
     * @param bearer          BEARER  Radio Access Technology info.value:  0(none) 13(eHRPD) 14(LTE)
     * @param mcc             Mobile Country Code (MCC).
     * @param mmsc            MMSC URL.
     * @param mmsproxy        MMS proxy address.
     * @param mmsport         MMS proxy port.
     * @param mnc             MNC Mobile Network Code (MNC).  Cannot be null
     * @param name            Entry name.  Cannot be null
     * @param password        APN password.
     * @param port            Proxy port.
     * @param protocol        The protocol to use to connect to this APN.  value: IP IPv6 or IPv4v6
     * @param proxy           Proxy address.
     * @param roamingprotocol The protocol to use to connect to this APN when roaming.
     * @param server          Server address.
     * @param type            Comma-delimited list of APN types.
     * @param user            APN username.
     * @param current         enable current set this APN
     * @return Returns true if successful.  Returns false if failed.
     */
    public boolean setAPN(String name, String apn, String proxy, int port, String user,
                          String server, String password, String mmsc, String mcc, String mnc, String mmsproxy,
                          int mmsport, int authtype, String type, String protocol, int bearer, String roamingprotocol, boolean current) {
        checkServiceAlive();
        if (mINetworkPolicy != null) {
            if (name == null || name.length() < 1) {
                return false;
            }
            if (apn == null || apn.length() < 1) {
                return false;
            }
            if (mcc == null || mcc.length() != 3) {
                return false;
            }
            if (mnc == null || (mnc.length() & 0xFFFE) != 2) {
                return false;
            }
            proxy = checkNotSet(proxy);
            user = checkNotSet(user);
            password = checkNotSet(password);
            server = checkNotSet(server);
            mmsc = checkNotSet(mmsc);
            mmsproxy = checkNotSet(mmsproxy);
            type = checkNotSet(type);
            protocol = (protocol == null || protocol.equals("") ? "IP" : protocol);
            roamingprotocol = checkNotSet(roamingprotocol);

            String Mport = (port <= 0 ? "" : String.valueOf(port));
            String Mmmsport = (mmsport <= 0 ? "" : String.valueOf(mmsport));
            try {
                mINetworkPolicy.setAPN(name, apn, proxy, Mport, user,
                        server, password, mmsc, mcc, mnc, mmsproxy,
                        Mmmsport, authtype, type, protocol, bearer, roamingprotocol, current);
                return true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public String queryAPN(String selection, String[] selectionArgs) {
        checkServiceAlive();
        if (mINetworkPolicy != null) {
            try {
                return mINetworkPolicy.queryAPN(selection, selectionArgs);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public int deleteAPN(String where, String[] whereArgs) {
        checkServiceAlive();
        if (mINetworkPolicy != null) {

            try {
                return mINetworkPolicy.deleteAPN(where, whereArgs);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private String checkNotSet(String value) {
        if (value == null || value.equals("")) {
            return "";
        } else {
            return value;
        }
    }

    public void shutdown(boolean reboot) {
        checkServiceAlive();
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.shutdown(reboot);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void wipeData(int flags) {
        checkServiceAlive();
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.wipeData(flags);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Support i9000S(Android 4.3) only.
     * Return  the currently active docking station connected state . Default return false when runing on other devices.
     *
     * @return true The device is in Ethernet mode or USB Host mode
     */
    public boolean getDockerState() {
        checkServiceAlive();
        String ret = null;
        if (mRestrictionPolicy != null) {
            try {
                ret = mRestrictionPolicy.getSettingProperty("persist-persist.sys.docker.state");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return Boolean.parseBoolean(ret);
    }

    public void setSysProperties(String key, String value) {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setSettingProperty("persist-"+key, value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * for Android 7.1 i6300
     *
     * @param startTime
     * @param endTime
     * @return
     */
    public long getTrafficInfo(long startTime, long endTime, int type) {
        checkServiceAlive();
        if (mINetworkPolicy != null) {
            try {
                return mINetworkPolicy.getTrafficInfo(startTime, endTime, type);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    /**
     * for Android 7.1 i6300
     *
     * @return
     */
    public String getRamId() {
        checkServiceAlive();
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getFlashId(1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * for Android 7.1 i6300
     *
     * @return
     */
    public String getRomId() {
        checkServiceAlive();
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getFlashId(2);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * for Android 7.1 i6300
     *
     * @return
     */
    public String getFlashId() {
        checkServiceAlive();
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getFlashId(3);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public void wipeData() {
        checkServiceAlive();
        if (mIDeviceControlPolicy != null) {
            try {
                mIDeviceControlPolicy.wipeData(0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void controlBT(boolean status) {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_BLUETOOTH, status ? 0 : 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void controlWifi(boolean status) {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_WIFI, status ? 0 : 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void controlMobileDate(boolean status) {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_WWAN, status ? 0 : 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void controlGPS(boolean status) {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_GPS, status ? 0 : 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void controlAdb(boolean status) {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_ADB, status ? 0 : 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void controlScaner(int status) {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_SCANNER, (status == 0) ? 0 : 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public Bundle getAppsUseTimes() {
        checkServiceAlive();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -5);
        Bundle bundle = new Bundle();

        if(mIApplicationPolicy == null) {
            return bundle;
        }

        List<UsageStats> stats = null;
        try {
            ParceledListSlice<UsageStats> slice = mIApplicationPolicy.queryUsageStats(INTERVAL_BEST, cal.getTimeInMillis(), System.currentTimeMillis());
            if (slice != null) {
                stats = slice.getList();
            }
        } catch (RemoteException e) {
            // fallthrough and return the empty list.
        }
        if (stats == null || stats.isEmpty()) {
            return bundle;
        }

        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            UsageStats newStat = stats.get(i);
            if (newStat.getTotalTimeInForeground() != 0) {
                bundle.putLongArray(newStat.getPackageName(), new long[]{newStat.getTotalTimeInForeground(), newStat.getLastTimeUsed()});
            }
        }

        return bundle;
    }

    public Bundle getSingleAppUseTimes(String packageName) {
        checkServiceAlive();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -5);
        Bundle bundle = new Bundle();
        bundle.putLong("lasttime", 0);
        bundle.putLong("alltime", 0);

        if(mIApplicationPolicy == null) {
            return bundle;
        }

        List<UsageStats> stats = null;
        try {
            ParceledListSlice<UsageStats> slice = mIApplicationPolicy.queryUsageStats(INTERVAL_BEST, cal.getTimeInMillis(), System.currentTimeMillis());
            if (slice != null) {
                stats = slice.getList();
            }
        } catch (RemoteException e) {
            // fallthrough and return the empty list.
        }
        if (stats == null || stats.isEmpty()) {
            return bundle;
        }

        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            UsageStats newStat = stats.get(i);
            if (newStat.getPackageName().equals(packageName)) {
                bundle.putLong("lasttime", newStat.getLastTimeUsed());
                bundle.putLong("alltime", newStat.getTotalTimeInForeground());
                return bundle;
            }
        }

        return bundle;
    }

    public void switchUSB(boolean enable) {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_ADB, enable ? 0 : 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean getUSBState() {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                return mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_ADB) == 0 ? true : false;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void connectVPN(String name, int type, String server, String username
            , String password, String dnsServers, String searchDomains, String routes, boolean mppe
            , String l2tpSecret, String ipsecIdentifier, String ipsecSecret, String ipsecUserCert
            , String ipsecCaCert, String ipsecServerCert) {
        checkServiceAlive();
        if (mINetworkPolicy != null) {
            try {
                mINetworkPolicy.connectVPN(name, type, server, username
                        , password, dnsServers, searchDomains, routes, mppe
                        , l2tpSecret, ipsecIdentifier, ipsecSecret, ipsecUserCert
                        , ipsecCaCert, ipsecServerCert);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public int getVpnState() {
        checkServiceAlive();
        if (mINetworkPolicy != null) {
            try {
                return mINetworkPolicy.getVpnState();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public void disconnectVpn() {
        checkServiceAlive();
        if (mINetworkPolicy != null) {
            try {
                mINetworkPolicy.disconnectVpn();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean uninstallApplication(String packageName, boolean keepDataAndCache, IPackageDeleteObserver observer) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return false;
        }
        try {
            int flags = 0;
            if (keepDataAndCache) {
                flags = PackageManager.DELETE_KEEP_DATA;
            }
            int ret = mIApplicationPolicy.deletePackage(packageName, flags, observer);
            if (ret == 0) {
                return true;
            }
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean installApplication(String apkFilePath, boolean installOnSDCard, IPackageInstallObserver observer) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return false;
        }
        try {
            int flags = 0;
            if (installOnSDCard) {
               // flags = PackageManager.INSTALL_EXTERNAL;
            }
            int ret = mIApplicationPolicy.installPackage(apkFilePath, flags, observer);
            if (ret == 0) {
                return true;
            }
        } catch (RemoteException e) {
        }
        return false;

    }

    public void setProximityScanEnabled(boolean enabled) {
        checkServiceAlive();
        throw new RuntimeException("stub");
    }

    public int whiteListsAppInsert(String packageName) {
        checkServiceAlive();
        if(mIDeviceControlPolicy == null) {
            return -1;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            Set<String> toSet = new HashSet<String>(wihteList);
            toSet.add(packageName);
            mIDeviceControlPolicy.writeListStringToFile(new ArrayList<>(toSet), SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            return 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int whiteListAppRemove(String packageName) {
        checkServiceAlive();
        if(mIDeviceControlPolicy == null) {
            return -1;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            Set<String> toSet = new HashSet<String>(wihteList);
            toSet.remove(packageName);
            mIDeviceControlPolicy.writeListStringToFile(new ArrayList<>(toSet), SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            return 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getWhiteList() {
        checkServiceAlive();
        if(mIDeviceControlPolicy == null) {
            return "";
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            return SettingsUtils.join(wihteList, ",");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean hasPackageName(String packageName) {
        checkServiceAlive();
        if(mIDeviceControlPolicy == null) {
            return false;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            return wihteList.contains(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void resetPassword() {
        checkServiceAlive();
        ArrayList<LockPatternView.Cell> lockLists = new ArrayList<LockPatternView.Cell>();
        LockPatternView.Cell cell0 = LockPatternView.Cell.of(0, 0);
        LockPatternView.Cell cell1 = LockPatternView.Cell.of(0, 1);
        LockPatternView.Cell cell2 = LockPatternView.Cell.of(0, 2);
        LockPatternView.Cell cell3 = LockPatternView.Cell.of(1, 1);
        LockPatternView.Cell cell4 = LockPatternView.Cell.of(2, 0);
        LockPatternView.Cell cell5 = LockPatternView.Cell.of(2, 1);
        LockPatternView.Cell cell6 = LockPatternView.Cell.of(2, 2);
        lockLists.add(cell0);
        lockLists.add(cell1);
        lockLists.add(cell2);
        lockLists.add(cell3);
        lockLists.add(cell4);
        lockLists.add(cell5);
        lockLists.add(cell6);
        if(mSecurityPolicy != null) {
            try {
                mSecurityPolicy.saveLockPattern(LockPatternUtils.patternToString(lockLists));
            } catch (RemoteException e) {
            }
        }
    }

    public void enableGPS(boolean status) {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_GPS, status ? 0 : 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void enableMobileDate(boolean status) {
        checkServiceAlive();
        if (mRestrictionPolicy != null) {
            try {
                mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_WWAN, status ? 0 : 1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    //YDSD interface===end==========
    //YTO interface===start==========
    public long getAppMemUsage(String packagename) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return -1;
        }
        long memsize = 0;
        try {
            List<ActivityManager.RunningAppProcessInfo> processInfos = mIApplicationPolicy.getRunningAppProcesses();
            if (processInfos == null) {
                return memsize;
            }

            for (ActivityManager.RunningAppProcessInfo process : processInfos) {
                if (process.processName.equals(packagename)) {
                    Debug.MemoryInfo[] memoryInfos = mIApplicationPolicy.getProcessMemoryInfo(new int[]{process.pid});
                    memsize = memoryInfos[0].getTotalPrivateDirty() * 1024L;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return memsize;
    }

    public double getAppPowerUsage(String packagename) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return -1;
        }
        double ret = 0.0;
        try {
            ret = mIApplicationPolicy.getAppPowerUsage(packagename);
            return ret;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public long getAllAppsMemUsage() {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return -1;
        }
        long memsize = 0;
        try {
            List<ActivityManager.RunningAppProcessInfo> processInfos = mIApplicationPolicy.getRunningAppProcesses();
            if (processInfos == null) {
                return memsize;
            }

            for (ActivityManager.RunningAppProcessInfo process : processInfos) {
                Debug.MemoryInfo[] memoryInfos = mIApplicationPolicy.getProcessMemoryInfo(new int[]{process.pid});
                memsize += memoryInfos[0].getTotalPrivateDirty() * 1024L;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return memsize;
    }

    public double getAllAppsPowerUsage() {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return -1;
        }
        double ret = 0.0;
        try {
            ret = mIApplicationPolicy.getAllAppsPowerUsage();
            return ret;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public Bundle getMemInfo() {//totalMem, availMem, freeMem
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return null;
        }
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        try {
            mIApplicationPolicy.getMemoryInfo(memoryInfo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Bundle bundle = new Bundle();
        bundle.putLong("availMem", memoryInfo.availMem);
        bundle.putLong("totalMem", memoryInfo.totalMem);
        bundle.putLong("usedMem", memoryInfo.totalMem - memoryInfo.availMem);
        return bundle;
    }

    public Bundle getBatteryInfo() {//plugged,level
        checkServiceAlive();
        Bundle bundle = new Bundle();
        if (mIDeviceControlPolicy == null) {
            return bundle;
        }
        try {
            bundle = mIDeviceControlPolicy.getBatteryInfo();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return bundle;
    }

    public long getAppTrafficInfo(String packagename) {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return -1;
        }
        try {
            return mINetworkPolicy.getAppTrafficInfo(packagename, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void setLockSreenNon() {
        checkServiceAlive();
        if (mSecurityPolicy == null) {
            return;
        }
        try {
            mSecurityPolicy.clearLock();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public Bundle getPowerUsage() {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return null;
        }
        try {
            return mIApplicationPolicy.getPowerUsage();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void controlWifiAP(boolean status) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_HOTSPOT, status ? 0 : 1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void controlUSB(boolean status) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_ADB, status ? 0 : 1);
            mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_MTP, status ? 0 : 1);
            mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_PTP, status ? 0 : 1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void controlRecoverySystem(boolean status) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_RECOVERY, status ? 0 : 1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean getEnableAutoCallRecord() {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return false;
        }
        try {
            String res = mRestrictionPolicy.getSettingProperty(SettingProperty.System_EnableAutoRecord);
            return Boolean.parseBoolean(res);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getAutoCallRecordPath() {
        checkServiceAlive();
        String res = "";
        if (mRestrictionPolicy == null) {
            return res;
        }
        try {
            res = mRestrictionPolicy.getSettingProperty(SettingProperty.System_AutoRecordPath);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void setEnableAutoCallRecord(boolean enable) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_EnableAutoRecord, Boolean.toString(enable));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setAutoCallRecordPath(String path) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_AutoRecordPath, path);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    //YTO===end==========

    /**
     * Save a lock pattern.
     *
     * @param pattern The new pattern to save.
     */
    public void saveLockPattern(String pattern) {
        checkServiceAlive();
        if (mSecurityPolicy == null) {
            return;
        }
        try {
            mSecurityPolicy.saveLockPattern(pattern);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save a lock password.  Does not ensure that the password is as good
     * as the requested mode, but will adjust the mode to be as good as the
     * password.
     *
     * @param password The password to save
     * @param quality  {@see DevicePolicyManager#getPasswordQuality(android.content.ComponentName)}
     */
    public void saveLockPassword(String password, int quality) {
        checkServiceAlive();
        if (mSecurityPolicy == null) {
            return;
        }
        try {
            mSecurityPolicy.saveLockPassword(password, quality);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear any lock pattern or password.
     */
    public void clearLock() {
        checkServiceAlive();
        if (mSecurityPolicy == null) {
            return;
        }
        try {
            mSecurityPolicy.clearLock();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setDeviceOwner(ComponentName name) {
        checkServiceAlive();
        if (mSecurityPolicy == null) {
            return;
        }
        try {
            mSecurityPolicy.setDeviceOwner(name);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean isDeviceOwner(String name) {
        checkServiceAlive();
        if (mSecurityPolicy == null) {
            return false;
        }
        try {
            return mSecurityPolicy.isDeviceOwner(name);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void cleanDeviceOwner(String name) {
        checkServiceAlive();
        if (mSecurityPolicy == null) {
            return;
        }
        try {
            mSecurityPolicy.cleanDeviceOwner(name);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public String getDeviceOwner() {
        checkServiceAlive();
        if (mSecurityPolicy == null) {
            return "";
        }
        try {
            return mSecurityPolicy.getDeviceOwner();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    public void setLeftKeyEnabled(boolean enabled) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        String settingstr = "";
        try {
            settingstr = mRestrictionPolicy.getSettingProperty(SettingProperty.System_DisallowedKeycodes);

            if (enabled) {
                settingstr = SettingsUtils.removePackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_BACK));
            } else {
                settingstr = SettingsUtils.addPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_BACK));
            }
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_DisallowedKeycodes, settingstr);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean getLeftKeyEnabled() {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return true;
        }
        String settingstr = "";
        try {
            settingstr = mRestrictionPolicy.getSettingProperty(SettingProperty.System_DisallowedKeycodes);

            return SettingsUtils.containsPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_BACK)) ? false : true;

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void setHomeKeyEnabled(boolean enabled) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        String settingstr = "";
        try {
            settingstr = mRestrictionPolicy.getSettingProperty(SettingProperty.System_DisallowedKeycodes);

            if (enabled) {
                settingstr = SettingsUtils.removePackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_HOME));
            } else {
                settingstr = SettingsUtils.addPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_HOME));
            }
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_DisallowedKeycodes, settingstr);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean getHomeKeyEnabled() {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return true;
        }
        String settingstr = "";
        try {
            settingstr = mRestrictionPolicy.getSettingProperty(SettingProperty.System_DisallowedKeycodes);

            return SettingsUtils.containsPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_HOME)) ? false : true;

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void setRightKeyEnabled(boolean enabled) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        String settingstr = "";
        try {
            settingstr = mRestrictionPolicy.getSettingProperty(SettingProperty.System_DisallowedKeycodes);

            if (enabled) {
                settingstr = SettingsUtils.removePackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_APP_SWITCH));
            } else {
                settingstr = SettingsUtils.addPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_APP_SWITCH));
            }
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_DisallowedKeycodes, settingstr);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean getRightKeyEnabled() {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return true;
        }
        String settingstr = "";
        try {
            settingstr = mRestrictionPolicy.getSettingProperty(SettingProperty.System_DisallowedKeycodes);

            return SettingsUtils.containsPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_APP_SWITCH)) ? false : true;

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void setKeyguardKeyEnabled(boolean enabled) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_KeyguardKey, Boolean.toString(enabled));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean getKeyguardKeyEnabled() {
        checkServiceAlive();
        boolean b = true;
        if (mRestrictionPolicy == null) {
            return b;
        }
        try {
            b = Boolean.parseBoolean(mRestrictionPolicy.getSettingProperty(SettingProperty.System_KeyguardKey));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return b;
    }

    public void setAutoPopInputMethod(boolean enabled) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.GLOBAL_auto_pop_softinput, enabled ? "1" : "0");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean getAutoPopInputMethod() {
        checkServiceAlive();
        boolean b = true;
        if (mRestrictionPolicy == null) {
            return b;
        }
        try {
            b = Integer.parseInt(mRestrictionPolicy.getSettingProperty(SettingProperty.GLOBAL_auto_pop_softinput)) == 1;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return b;
    }

    public void setScanKeyPass(boolean enabled) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_ScanKeyPass, Boolean.toString(enabled));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean getScanKeyPass() {
        checkServiceAlive();
        boolean b = true;
        if (mRestrictionPolicy == null) {
            return b;
        }
        try {
            String enable = mRestrictionPolicy.getSettingProperty(SettingProperty.System_ScanKeyPass);
            if (TextUtils.isEmpty(enable)) {
                return true;
            } else {
                return Boolean.parseBoolean(enable);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return b;
    }

    public void setShowScanButton(boolean enabled) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_ScanSuspensionButton, enabled ? "1" : "0");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean getShowScanButton() {
        checkServiceAlive();
        boolean b = true;
        if (mRestrictionPolicy == null) {
            return b;
        }
        try {
            b = Integer.parseInt(mRestrictionPolicy.getSettingProperty(SettingProperty.System_ScanSuspensionButton)) == 1;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return b;
    }

    public boolean getLeftKeyguardEnabled() {
        checkServiceAlive();
        boolean b = true;
        if (mRestrictionPolicy == null) {
            return b;
        }
        try {
            b = Boolean.parseBoolean(mRestrictionPolicy.getSettingProperty(SettingProperty.System_KeyguardLeftKey));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return b;
    }

    public void setRightKeyguardEnabled(boolean enabled) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_KeyguardRightKey, Boolean.toString(enabled));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean getRightKeyguardEnabled() {
        checkServiceAlive();
        boolean b = true;
        if (mRestrictionPolicy == null) {
            return b;
        }
        try {
            b = Boolean.parseBoolean(mRestrictionPolicy.getSettingProperty(SettingProperty.System_KeyguardRightKey));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return b;
    }

    public void setPTTDownAction(String action) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_UROVO_PTT_Down_ACTION, action);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public String getPTTDownAction() {
        checkServiceAlive();
        String str = "";
        if (mRestrictionPolicy == null) {
            return str;
        }
        try {
            str = mRestrictionPolicy.getSettingProperty(SettingProperty.System_UROVO_PTT_Down_ACTION);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return str;
    }

    public void setPTTUpAction(String action) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_UROVO_PTT_Up_ACTION, action);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public String getPTTUpAction() {
        checkServiceAlive();
        String str = "";
        if (mRestrictionPolicy == null) {
            return str;
        }
        try {
            str = mRestrictionPolicy.getSettingProperty(SettingProperty.System_UROVO_PTT_Up_ACTION);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return str;
    }

    public List<String> getRunningAppProcesses() {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return null;
        }
        List<String> lists = new ArrayList<String>();
        try {
            List<ActivityManager.RunningAppProcessInfo> infos = mIApplicationPolicy.getRunningAppProcesses();
            if (infos != null) {
                for (ActivityManager.RunningAppProcessInfo i : infos) {
                    if (i.pkgList != null) {
                        for (String pkg : i.pkgList) {
                            lists.add(pkg);
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return lists;
    }

    public String getTopPackageName() {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return "";
        }
        try {
            List<ActivityManager.RunningTaskInfo> taskList = mIApplicationPolicy.getRunningTasks(1);
            if ((taskList != null)
                    && (taskList.get(0) != null)
                    && (taskList.get(0).topActivity != null)) {
                return taskList.get(0).topActivity.getPackageName();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return "";
    }

    public boolean setDefaultLauncher(ComponentName componentName) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return false;
        }
        try {
            return mIApplicationPolicy.setDefaultLauncher(componentName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeDefaultLauncher(String packageName) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return false;
        }
        try {
            return mIApplicationPolicy.removeDefaultLauncher(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Install a package. Since this may take a little while, the result
     * will be posted back to the given observer. An installation will
     * fail if the package named in the package file's manifest is already
     * installed INSTALL_REPLACE_EXISTING, or if there's no space available on the device.
     *
     * @param apkFilePath The location of the package file to install. This can
     *                    be a 'file:' or a 'content:' URI.
     */
    public boolean installApplication(String apkFilePath) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return false;
        }
        try {
            int ret = mIApplicationPolicy.installPackage(apkFilePath, 0, null);
            if (ret == 0) {
                return true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Attempts to delete a package.   A deletion will fail  if the
     * named package cannot be found, or if the named package is a "system package".
     * (TODO: include pointer to documentation on "system packages")
     *
     * @param packageName The name of the package to delete
     */
    public boolean uninstallApplication(String packageName) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return false;
        }
        try {
            int ret = mIApplicationPolicy.deletePackage(packageName, 0, new IPackageDeleteObserver.Stub() {
                @Override
                public void packageDeleted(String s, int i) throws RemoteException {
                    Log.i(TAG, "uninstallApplication packageDeleted:" + i + " " + s);
                }
            });
            if (ret == 0) {
                return true;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setSettingProperty(String keyName, String value) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return false;
        }
        if (mRestrictionPolicy != null) {
            try {
                return mRestrictionPolicy.setSettingProperty(keyName, value);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取当前系统设置项状态
     *
     * @param keyName 设置项名称
     *                return null if failed. other is successed.
     */
    public String getSettingProperty(String keyName) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return "";
        }
        if (mRestrictionPolicy != null) {
            try {
                return mRestrictionPolicy.getSettingProperty(keyName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getRestrictionPolicy(String function) {
        checkServiceAlive();
        if (mIDeviceControlPolicy == null) {
            return -1;
        }
        if (function.equals("changed_usb_mode")) {
            try {
                return mIDeviceControlPolicy.getUsbMode();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return 0;
        }
        int status = 0;
        try {
            if (function.equals("gps_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_GPS);
            } else if (function.equals("wwan_enable") || function.equals("wwan_data_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_WWAN);
            } else if (function.equals("wlan_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_WIFI);
            } else if (function.equals("scanner_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_SCANNER);
            } else if (function.equals("bluetooth_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_BLUETOOTH);
            } else if (function.equals("touch_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_TOUCH);
            } else if (function.equals("camera_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_CAMERA);
            } else if (function.equals("keyboard_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_KEYBOARD);
            } else if (function.equals("usb_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_ADB);
            } else if (function.equals("ime_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_IME);
            } else if (function.equals("torch_enable")) {
                status = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_TORCH);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return status == 0 ? 1 : 0;
    }

    public int setRestrictionPolicy(String function, int status) {
        checkServiceAlive();
        if (mIDeviceControlPolicy == null) {
            return -1;
        }
        if (!TextUtils.isEmpty(function)) {
            if (function.equals("changed_usb_mode")) {
                try {
                    return mIDeviceControlPolicy.changeUsbMode(status);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if (status == 0) {
                status = 1;
            } else if (status == 1) {
                status = 0;
            }
            try {
                if (function.equals("bluetooth_enable")) {
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_BLUETOOTH, status);
                } else if (function.equals("wlan_enable")) {
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_WIFI, status);
                } else if (function.equals("scanner_enable")) {
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_SCANNER, status);
                } else if (function.equals("gps_enable")) {
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_GPS, status);
                } else if (function.equals("wwan_enable")) {
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_WWAN, status);
                } else if (function.equals("touch_enable")) {
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_TOUCH, status);
                } else if (function.equals("camera_enable")) {
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_CAMERA, status);
                } else if (function.equals("keyboard_enable")) {
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_KEYBOARD, status);
                } else if (function.equals("usb_enable")) {
                    mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_MTP, status);
                    mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_PTP, status);
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_ADB, status);
                } else if (function.equals("ime_enable")) {
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_IME, status);
                } else if (function.equals("torch_enable")) {
                    return mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_TORCH, status);
                } else {
                    return -1;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return status;
        } else {
            return -1;
        }
    }

    public boolean getHideCallNumber() {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return false;
        }
        try {
            String str = mRestrictionPolicy.getSettingProperty(SettingProperty.System_HideCallNumber);
            return Boolean.parseBoolean(str);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setHideCallNumber(boolean enable) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_HideCallNumber, String.valueOf(enable));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public boolean writeCCZCPkgWhiteList(String pkg) {
        checkServiceAlive();
        if(mIDeviceControlPolicy == null) {
            return false;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            Set<String> toSet = new HashSet<String>(wihteList);
            toSet.add(pkg);
            mIDeviceControlPolicy.writeListStringToFile(new ArrayList<>(toSet), SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeCCZCPkgWhiteList(String pkg) {
        checkServiceAlive();
        if(mIDeviceControlPolicy == null) {
            return false;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            Set<String> toSet = new HashSet<String>(wihteList);
            toSet.remove(pkg);
            mIDeviceControlPolicy.writeListStringToFile(new ArrayList<>(toSet), SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getCCZCPkgWhiteList() {
        checkServiceAlive();
        if(mIDeviceControlPolicy == null) {
            return null;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
            return wihteList;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean writeCCZCBTWhiteList(String mac) {
        checkServiceAlive();
        if(mIDeviceControlPolicy == null) {
            return false;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.BT_WHITELIST_FILE);
            Set<String> toSet = new HashSet<String>(wihteList);
            toSet.add(mac);
            mIDeviceControlPolicy.writeListStringToFile(new ArrayList<>(toSet), SettingProperty.BT_WHITELIST_FILE);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeCCZCBTWhiteList(String mac) {
        checkServiceAlive();
        if(mIDeviceControlPolicy == null) {
            return false;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.BT_WHITELIST_FILE);
            Set<String> toSet = new HashSet<String>(wihteList);
            toSet.remove(mac);
            mIDeviceControlPolicy.writeListStringToFile(new ArrayList<>(toSet), SettingProperty.BT_WHITELIST_FILE);
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getCCZCBTWhiteList() {
        checkServiceAlive();
        if(mIDeviceControlPolicy == null) {
            return null;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.BT_WHITELIST_FILE);
            return wihteList;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setLockTaskMode(String packageName, boolean enable) {
        setLockTaskMode(packageName, enable, false);
    }

    public void setLockTaskMode(String packageName, boolean enable, boolean showStatusBar) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return;
        }
        setSettingProperty("System-SHOW_STATUSBAR_LOCKTASKMODE", String.valueOf(showStatusBar && enable));
        String passwd = null;
        try {
            mIApplicationPolicy.setLockTaskMode(packageName, enable);
            passwd = mRestrictionPolicy.getSettingProperty(SettingProperty.System_LockTaskModePassword);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (!(enable == false && passwd != null && !passwd.equals("")))
            enableStatusBar(!(showStatusBar && enable));
    }

    public void setLockTaskModePassword(String password) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setSettingProperty(SettingProperty.System_LockTaskModePassword, password);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void setPackageInstaller(String packageName, int action) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            if (action == 0) {
                mRestrictionPolicy.setSettingProperty(SettingProperty.System_InstallerPckNames, "");
            } else {
                String packageNames = mRestrictionPolicy.getSettingProperty(SettingProperty.System_InstallerPckNames);
                String newPackages = "";
                if (action == 1) {
                    newPackages = SettingsUtils.addPackageName(packageNames, packageName);
                } else {
                    newPackages = SettingsUtils.removePackageName(packageNames, packageName);
                }
                mRestrictionPolicy.setSettingProperty(SettingProperty.System_InstallerPckNames, newPackages);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<String> getPackageInstaller() {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return null;
        }
        try {
            String packageNames = mRestrictionPolicy.getSettingProperty(SettingProperty.System_InstallerPckNames);
            List<String> pckNames = null;
            if (!TextUtils.isEmpty(packageNames)) {
                String[] split = packageNames.split(",");
                pckNames = java.util.Arrays.asList(split);
            }
            return pckNames;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setAutoRunningApp(ComponentName componentName, int action) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            if (action == 0) {
                mRestrictionPolicy.setSettingProperty(SettingProperty.System_AutoRunningApp, "");
            } else if (componentName != null) {
                String packageNames = mRestrictionPolicy.getSettingProperty(SettingProperty.System_AutoRunningApp);
                String newPackages = "";
                if (action == 1) {
                    newPackages = SettingsUtils.addPackageName(packageNames, componentName.flattenToString());
                } else {
                    newPackages = SettingsUtils.removePackageName(packageNames, componentName.flattenToString());
                }
                mRestrictionPolicy.setSettingProperty(SettingProperty.System_AutoRunningApp, newPackages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getAutoRunningApp() {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return null;
        }
        try {
            String packageNames = mRestrictionPolicy.getSettingProperty(SettingProperty.System_AutoRunningApp);
            return SettingsUtils.stringToStringList(packageNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void enableAdb(boolean action) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            mRestrictionPolicy.setRestrictionPolicy(SettingProperty.ALLOW_ADB, action ? 0 : 1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public int getAdbStatus() {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return -1;
        }
        try {
            int ret = mRestrictionPolicy.getRestrictionPolicy(SettingProperty.ALLOW_ADB);
            return ret == 0 ? 1 : 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public boolean whiteListReset() {
        checkServiceAlive();
        if (mIDeviceControlPolicy == null) {
            return false;
        }
        try {
            return mIDeviceControlPolicy.writeListStringToFile(null, SettingProperty.DATA_PACKAGE_WHITELIST_FILE);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeFromWifiWhiteList(String ssid) {
        checkServiceAlive();
        if (mIDeviceControlPolicy == null) {
            return false;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.WIFI_WHITELIST_FILE);
            wihteList.remove(ssid);
            return mIDeviceControlPolicy.writeListStringToFile(wihteList, SettingProperty.WIFI_WHITELIST_FILE);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insertToWifiWhiteList(String ssid) {
        checkServiceAlive();
        if (mIDeviceControlPolicy == null) {
            return false;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.WIFI_WHITELIST_FILE);
            Set<String> toSet = new HashSet<String>(wihteList);
            toSet.add(ssid);
            return mIDeviceControlPolicy.writeListStringToFile(new ArrayList<>(toSet), SettingProperty.WIFI_WHITELIST_FILE);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setWifiWhiteList(List<String> ssids, int mode, int action)  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return;
        }
        try {
            if(ssids == null || ssids.size() == 0) {
                mINetworkPolicy.setWifiWhiteSsids(null, mode, action);
            } else {
                String[] strings = ssids.toArray(new String[ssids.size()]);
                mINetworkPolicy.setWifiWhiteSsids(strings, mode, action);
            }
        } catch (RemoteException e) {
        }
    }

    public void setWifiWhiteList(String ssids, int mode, int action)  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return;
        }
        String[] strings = {ssids};
        try {
            mINetworkPolicy.setWifiWhiteSsids(strings, mode, action);
        } catch (RemoteException e) {
        }
    }
	
	public void setWifiBand(int value)  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return;
        }
		 try {
       			mINetworkPolicy.setWifiBand(value);
	    } catch (RemoteException e) {
        }
    }
	
	public int getWifiBand()  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return 0;
        }
		try {
       			return mINetworkPolicy.getWifiBand();
	    } catch (RemoteException e) {
        }
		  return 0;
    }
	
	public void setWifiChannel(int band,String value)  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return;
        }
		 try {
       			mINetworkPolicy.setWifiChannel(band,value);
	    } catch (RemoteException e) {
        }
    }
	
	public String getWifiChannel(int band)  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return  null;
        }
		 try {
       			return mINetworkPolicy.getWifiChannel(band);
	    } catch (RemoteException e) {
        }
		 return  null;
    }
	
	public void setRoamingConfig(int threshold,int diff, int powerSave)  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return;
        }
		 try {
       			mINetworkPolicy.setRoamingConfig(threshold,diff,powerSave);
	    } catch (RemoteException e) {
        }
    }
	
	public int getRoamingThreshold()  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return 0;
        }
		 try {
       			return mINetworkPolicy.getRoamingThreshold();
	    } catch (RemoteException e) {
        }
		 return 0;
    }
	
	public int getRoamingThresholdDiff()  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return 0;
        }
		try {
       			return  mINetworkPolicy.getRoamingThresholdDiff();
	    } catch (RemoteException e) {
        }
		return 0;
    }
	
	public int getPowerSaveValue()  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return 1;
        } 
		try {
       			 return  mINetworkPolicy.getPowerSaveValue();
	    } catch (RemoteException e) {
        }
		return 1;
    }
	
	public void resetRoamingConfig()  {
        checkServiceAlive();
        if (mINetworkPolicy == null) {
            return;
        }
		try {
       			mINetworkPolicy.resetRoamingConfig();
	    } catch (RemoteException e) {
        }
    }

    public List<String> getWifiWhiteList() {
        checkServiceAlive();
        if (mIDeviceControlPolicy == null) {
            return null;
        }
        try {
            List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(SettingProperty.WIFI_WHITELIST_FILE);
            return wihteList;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param packageName
     * @param action
     */
    public void setHideApplicationIcon(String packageName, int action) {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return;
        }
        try {
            if (action == 0) {
                mRestrictionPolicy.setSettingProperty(SettingProperty.System_HideApplicationIcon, "");
            } else if (packageName != null) {
                String packageNames = mRestrictionPolicy.getSettingProperty(SettingProperty.System_HideApplicationIcon);
                String newPackages = "";
                if (action == 1) {
                    newPackages = SettingsUtils.addPackageName(packageNames, packageName);
                } else {
                    newPackages = SettingsUtils.removePackageName(packageNames, packageName);
                }
                mRestrictionPolicy.setSettingProperty(SettingProperty.System_HideApplicationIcon, newPackages);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getHideApplicationIcon() {
        checkServiceAlive();
        if (mRestrictionPolicy == null) {
            return null;
        }
        try {
            String packageNames = mRestrictionPolicy.getSettingProperty(SettingProperty.System_HideApplicationIcon);
            return SettingsUtils.stringToStringList(packageNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean getApplicationEnabledSetting(String packageName) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return false;
        }
        if (mIApplicationPolicy != null) {
            try {
                return mIApplicationPolicy.getApplicationEnabledSetting(packageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void setApplicationEnabledSetting(String packageName, boolean enable) {
        checkServiceAlive();
        if (mIApplicationPolicy == null) {
            return;
        }
        if (mIApplicationPolicy != null) {
            try {
                mIApplicationPolicy.setApplicationEnabledSetting(packageName, enable);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param packageName
     * @param mode
     * @param action
     */
    public void setAllowInstallApps(String packageName, int mode, int action) {
        checkServiceAlive();
        if (mIDeviceControlPolicy == null) {
            return;
        }
        try {
            String settingName = SettingProperty.DATA_PACKAGE_WHITELIST_FILE;
            if(mode == 0) {
                settingName = SettingProperty.DATA_PACKAGE_WHITELIST_FILE;
            } else if(mode == 1){
                settingName = SettingProperty.DATA_PACKAGE_REMOVE_FILE;
            }else if(mode == 5){
                settingName = SettingProperty.CUSTOMIZE_WHITELIST_FILE;
            }
            if(action == 0) {
                mIDeviceControlPolicy.writeListStringToFile(null, settingName);
            } else if(packageName != null){
                List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(settingName);
                Set<String> toSet = new HashSet<String>(wihteList);
                if(action == 1) {
                    toSet.add(packageName);
                } else {
                    toSet.remove(packageName);
                }
                mIDeviceControlPolicy.writeListStringToFile(new ArrayList<>(toSet), settingName);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllowInstallApps(int mode) {
        checkServiceAlive();
        if (mIDeviceControlPolicy == null) {
            return null;
        }
        List<String> list = null;
        String settingName = "";
        if (mode == 0) {
            settingName = SettingProperty.DATA_PACKAGE_WHITELIST_FILE;
        } else if (mode == 1) {
            settingName = SettingProperty.CUSTOMIZE_PACKAGE_WHITELIST_FILE;
        } else if (mode == 2) {
            settingName = SettingProperty.DATA_PACKAGE_REMOVE_FILE;
        } else if (mode == 3) {
            settingName = SettingProperty.CUSTOMIZE_PACKAGE_REMOVE_FILE;
        }

        try {
            list = mIDeviceControlPolicy.readListStringFromFile(settingName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * @returns the result that SDK supports specific feature or not.
     */
    public boolean isFeatureSupport(String methodName) {
        if (TextUtils.isEmpty(methodName))
            return false;
        Method[] keyMethods = DeviceManager.class.getDeclaredMethods();
        String tempMethodName = null;
        try {
            for (int i = 0; i < keyMethods.length; i++) {
                tempMethodName = keyMethods[i].getName();
                int modi = keyMethods[i].getModifiers();
                if (methodName.equals(tempMethodName)) {
                    if (Modifier.isPublic(modi)) {
                        return true;
                    }
                }
            }
        } catch (NullPointerException e) {
            Log.w(TAG, "Non-static field : " + methodName);
        } catch (IllegalArgumentException e1) {
            Log.w(TAG, "Type mismatch : " + methodName);
        }
        return false;
    }

    /**
     * @returns VersionCode of SDK.
     */
    public int getVersionCode() {
        return 200418;
    }

    /**
     * Get UMS SDK Version
     * @returns return the version of sdk that ums used
     */
    public String getUmsSdkVersion() {
        checkServiceAlive();
        String umsSdkVersion = "V0.0.0";
        try {
            umsSdkVersion = mRestrictionPolicy.getSettingProperty("persist-persist.sys.ums.sdkversion");
            return umsSdkVersion;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return umsSdkVersion;
    }

    /**
     * Get the Imei
     *
     * @return imei number
     */
    public String getImei(int slotId) {
        checkServiceAlive();
        if (mIDeviceControlPolicy != null) {
            try {
                return mIDeviceControlPolicy.getImei(slotId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return "";
      }

     /**
     * @param pkg 要保活的应用
     * @param mode 0:白名单　1:黑名单
     * @param action 0:清除　1:添加  2:删除
     * @return void
     */
    public void setAliveWhiteList(String packageName, int mode, int action) {
        checkServiceAlive();
        if (mIDeviceControlPolicy == null) {
            return;
        }
        try {
            String settingName = SettingProperty.ALIVE_WHITELIST_FILE_PATH;
            if(mode == 0) {
                settingName = SettingProperty.ALIVE_WHITELIST_FILE_PATH;
            } else if(mode == 1){
                settingName = SettingProperty.ALIVE_BLACKLIST_FILE_PATH;
            }
            if(action == 0) {
                mIDeviceControlPolicy.writeListStringToFile(null, settingName);
            } else if(packageName != null){
                List<String> wihteList = mIDeviceControlPolicy.readListStringFromFile(settingName);
                Set<String> toSet = new HashSet<String>(wihteList);
                if(action == 1) {
                    toSet.add(packageName);
                } else {
                    toSet.remove(packageName);
                }
                mIDeviceControlPolicy.writeListStringToFile(new ArrayList<>(toSet), settingName);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

     /**
     * @param mode 0:白名单　1:黑名单
     * @return List<String>
     */
    public List<String> getAliveWhiteList(int mode) {
        checkServiceAlive();
        if (mIDeviceControlPolicy == null) {
            return null;
        }
        List<String> list = null;
        String settingName = "";
        if (mode == 0) {
            settingName = SettingProperty.ALIVE_WHITELIST_FILE_PATH;
        } else if (mode == 1) {
            settingName = SettingProperty.ALIVE_BLACKLIST_FILE_PATH;
        } 
        try {
            list = mIDeviceControlPolicy.readListStringFromFile(settingName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return list;
    }

}
