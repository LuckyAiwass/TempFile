// INetworkPolicy.aidl
package com.ubx.usdk.profile.aidl;

// Declare any non-default types here with import statements

interface INetworkPolicy {

        boolean setHotpotInfo(String name,String password,int mode);
        void setAPN(String name, String apn, String proxy, String port, String user,
                String server, String password, String mmsc, String mcc, String mnc, String mmsproxy,
                String mmsport, int authtype, String type, String protocol, int bearer, String roamingprotocol, boolean current);
        int deleteAPN(String where, in String[] whereArgs);
        String queryAPN(String selection, in String[] selectionArgs);
        int getCurrentApn();
        boolean setCurrentApn(int appid);

        void connectVPN(String name, int type, String server, String username
                , String password, String dnsServers, String searchDomains, String routes, boolean mppe
                , String l2tpSecret, String ipsecIdentifier, String ipsecSecret, String ipsecUserCert
                , String ipsecCaCert, String ipsecServerCert);

        int getVpnState();
        void disconnectVpn();

        long getTrafficInfo(long startTime, long endTime, int type);
        long getAppTrafficInfo(String packagename, int type);

        void setFirewallUidChainRule(int uid, int networkType, boolean allow);
        void clearFirewallChain(String chain);
        void setWifiWhiteSsids(in String[] ssids, int mode, int action);
		
	void setWifiBand(int value);
    int getWifiBand();
    void setWifiChannel(int band,String value);
    String getWifiChannel(int band);
    void setRoamingConfig(int threshold,int diff, int powerSave);
    int getRoamingThreshold();
    int getRoamingThresholdDiff();
    int getPowerSaveValue();
    void resetRoamingConfig();
}
