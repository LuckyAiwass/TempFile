package com.ubx.usdk.profile;

import android.os.IBinder;
import android.os.RemoteException;

import com.ubx.usdk.LogUtil;
import com.ubx.usdk.profile.aidl.INetworkPolicy;

/**
 * @author lin.luo
 * @ClassName:
 * @Description: TODO
 * @date 2019.12.26
 * @Copyright ubx
 */
public class NetworkManager {

    private ProfileManager mProfileManager;
    private INetworkPolicy mINetworkPolicy;

    protected NetworkManager(ProfileManager profileManager) {
        this.mProfileManager = profileManager;
    }

    /**
     * @hide
     */
    protected void init() {
        if (mINetworkPolicy == null && mProfileManager != null && mProfileManager.getIProfileManager() != null) {
            try {
                IBinder binder = mProfileManager.getIProfileManager().getNetworkPolicyIBinder();
                if (binder != null) {
                    mINetworkPolicy = INetworkPolicy.Stub.asInterface(binder);
                }
            } catch (RemoteException e) {
                LogUtil.e("NetworkManager::init", e);
            }
        }
    }

    /**
     * @hide
     */
    protected void release() {
        mINetworkPolicy = null;
    }

    public boolean setHotpotInfo(String name, String password, int mode) {
        if (mINetworkPolicy != null) {
            try {
                return mINetworkPolicy.setHotpotInfo(name, password, mode);
            } catch (RemoteException e) {
                LogUtil.e("setHotpotInfo", e);
            }
        }
        return false;
    }

    public void setAPN(String name, String apn, String proxy, String port, String user,
                       String server, String password, String mmsc, String mcc, String mnc, String mmsproxy,
                       String mmsport, int authtype, String type, String protocol, int bearer, String roamingprotocol, boolean current) {
        if (mINetworkPolicy != null) {
            if (name == null || name.length() < 1) {
                return;
            }
            if (apn == null || apn.length() < 1) {
                return;
            }
            if (mcc == null || mcc.length() != 3) {
                return;
            }
            if (mnc == null || (mnc.length() & 0xFFFE) != 2) {
                return;
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
            port = checkNotSet(port);
            mmsport = checkNotSet(mmsport);
            try {
                mINetworkPolicy.setAPN(name, apn, proxy, port, user,
                        server, password, mmsc, mcc, mnc, mmsproxy,
                        mmsport, authtype, type, protocol, bearer, roamingprotocol, current);
            } catch (RemoteException e) {
                LogUtil.e("setAPN", e);
            }
        }

    }

    private String checkNotSet(String value) {
        if (value == null || value.equals("")) {
            return "";
        } else {
            return value;
        }
    }

    public int deleteAPN(String where, String[] whereArgs) {
        if (mINetworkPolicy != null) {

            try {
                return mINetworkPolicy.deleteAPN(where, whereArgs);
            } catch (RemoteException e) {
                LogUtil.e("deleteAPN", e);
            }
        }
        return 0;
    }

    public String queryAPN(String selection, String[] selectionArgs) {
        if (mINetworkPolicy != null) {
            try {
                return mINetworkPolicy.queryAPN(selection, selectionArgs);
            } catch (RemoteException e) {
                LogUtil.e("queryAPN", e);
            }
        }
        return null;
    }

    public int getCurrentApn() {
        if (mINetworkPolicy != null) {
            try {
                return mINetworkPolicy.getCurrentApn();
            } catch (RemoteException e) {
                LogUtil.e("getCurrentApn", e);
            }
        }
        return 0;
    }

    public boolean setCurrentApn(int appid) {
        if (mINetworkPolicy != null) {
            try {
                return mINetworkPolicy.setCurrentApn(appid);
            } catch (RemoteException e) {
                LogUtil.e("setCurrentApn", e);
            }
        }
        return false;
    }

    public void connectVPN(String name, int type, String server, String username
            , String password, String dnsServers, String searchDomains, String routes, boolean mppe
            , String l2tpSecret, String ipsecIdentifier, String ipsecSecret, String ipsecUserCert
            , String ipsecCaCert, String ipsecServerCert) {
        if (mINetworkPolicy != null) {
            try {
                mINetworkPolicy.connectVPN(name, type, server, username
                        , password, dnsServers, searchDomains, routes, mppe
                        , l2tpSecret, ipsecIdentifier, ipsecSecret, ipsecUserCert
                        , ipsecCaCert, ipsecServerCert);
            } catch (RemoteException e) {
                LogUtil.e("connectVPN", e);
            }
        }

    }

    public int getVpnState() {
        if (mINetworkPolicy != null) {
            try {
                return mINetworkPolicy.getVpnState();
            } catch (RemoteException e) {
                LogUtil.e("getVpnState", e);
            }
        }
        return -1;
    }

    public void disconnectVpn() {
        if (mINetworkPolicy != null) {
            try {
                mINetworkPolicy.disconnectVpn();
            } catch (RemoteException e) {
                LogUtil.e("disconnectVpn", e);
            }
        }
    }

    public long getTrafficInfo(long startTime, long endTime, int type) {
        if (mINetworkPolicy != null) {
            try {
                return mINetworkPolicy.getTrafficInfo(startTime, endTime, type);
            } catch (RemoteException e) {
                LogUtil.e("getTrafficInfo", e);
            }
        }
        return -1;
    }

    public long getAppTrafficInfo(String packagename, int type) {
        if (mINetworkPolicy != null) {
            try {
                return mINetworkPolicy.getAppTrafficInfo(packagename, type);
            } catch (RemoteException e) {
                LogUtil.e("getTrafficInfo", e);
            }
        }
        return -1;
    }

    public void setFirewallUidChainRule(int uid, int networkType, boolean allow) {
        if (mINetworkPolicy != null) {
            try {
                mINetworkPolicy.setFirewallUidChainRule(uid, networkType, allow);
            } catch (RemoteException e) {
                LogUtil.e("getTrafficInfo", e);
            }
        }
    }

    public void clearFirewallChain(String chain) {
        if (mINetworkPolicy != null) {
            try {
                mINetworkPolicy.clearFirewallChain(chain);
            } catch (RemoteException e) {
                LogUtil.e("clearFirewallChain", e);
            }
        }
    }
	
	public void setWifiBand(int value) {
        if (mINetworkPolicy != null) {
			try {
                mINetworkPolicy.setWifiBand(value);
            } catch (RemoteException e) {
                LogUtil.e("setWifiBand", e);
            }
        }
    }
	
	public int getWifiBand() {
        if (mINetworkPolicy != null) {
			try {
                return mINetworkPolicy.getWifiBand();
            } catch (RemoteException e) {
                LogUtil.e("getWifiBand", e);
            }
        }
		return 0;
    }

	public void setWifiChannel(int band,String value) {
        if (mINetworkPolicy != null) {
			try {
                mINetworkPolicy.setWifiChannel(band,value);
            } catch (RemoteException e) {
                LogUtil.e("setWifiChannel", e);
            }
        }
    }
	
	public String getWifiChannel(int band) {
        if (mINetworkPolicy != null) {
			try {
                return   mINetworkPolicy.getWifiChannel(band);
            } catch (RemoteException e) {
                LogUtil.e("getWifiChannel", e);
            }
        }
		return null;
    }
								 
	public void setRoamingConfig(int threshold,int diff, int powerSave){
        if (mINetworkPolicy != null) {
			try {
                mINetworkPolicy.setRoamingConfig(threshold,diff,powerSave);
            } catch (RemoteException e) {
                LogUtil.e("setRoamingConfig", e);
            }
        }
    }
								 
	public  int getRoamingThreshold(){
        if (mINetworkPolicy != null) {
			try {
               return   mINetworkPolicy.getRoamingThreshold();
            } catch (RemoteException e) {
                LogUtil.e("getRoamingThreshold", e);
            }
        }
		 return 0;
    }
								 
	public  int getRoamingThresholdDiff(){
        if (mINetworkPolicy != null) {
			try {
               return  mINetworkPolicy.getRoamingThresholdDiff();
            } catch (RemoteException e) {
                LogUtil.e("getRoamingThresholdDiff", e);
            }
        }
		 return 0;
    }
	
	public  int getPowerSaveValue(){
        if (mINetworkPolicy != null) { 
			try {
               return  mINetworkPolicy.getPowerSaveValue();
            } catch (RemoteException e) {
                LogUtil.e("getRoamingThresholdDiff", e);
            }
        }
		return 1;
    }
								 
	public void resetRoamingConfig(){
        if (mINetworkPolicy != null) {
			try {
               mINetworkPolicy.resetRoamingConfig();
            } catch (RemoteException e) {
                LogUtil.e("resetRoamingConfig", e);
            }
        }
    }

}
