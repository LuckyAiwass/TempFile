/*
 * Copyright (c) 2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.Framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

//tom_chang_20180822 add start, for get modem info using QcRilHook
import com.qualcomm.qcrilhook.QcRilHook;
//tom_chang_20180822 add end, for get modem info using QcRilHook
public class MainApp extends Application {

    private static MainApp mMainApp;
    private static Context mContext;
    private static String TAG = "MainApp";
    private int servcieCounter;
    private static QcRilHook mQcRILHook; //tom_chang_20180822 add, for get modem info using QcRilHook

    public static class ServiceInfo {
        ServiceInfo(Intent intent, Object object) {
            intentService = intent;
            this.paraMap = (HashMap<String, String>) object;
        }

        Intent intentService;
        HashMap<String, String> paraMap;
    }

    ArrayList<ServiceInfo> intentArrayList = new ArrayList<ServiceInfo>();

    void clearAllService() {
        if (intentArrayList.size() > 0) {
            for (int i = 0; i < intentArrayList.size(); i++) {
                Intent intent = intentArrayList.get(i).intentService;
                logd("===" + intent);
                if (intent.getComponent() != null ||
                        intent.getPackage() != null) {
                    mContext.stopService(intent);
                }
            }
            intentArrayList.clear();
        }
    }

    void addServiceList(ServiceInfo serviceInfo) {
        intentArrayList.add(serviceInfo);
    }

    public static class FunctionItem {

        String name;
        String packageName;// the key for get test name
        String auto;
        HashMap<String, String> parameter = new HashMap<String, String>();

        public String getName() {
            return name;
        }

        public void setName(String nm) {
            name = nm;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String pname) {
            packageName = pname;
        }

        public String getAuto() {
            return auto;
        }

        public void setAuto(String at) {
            auto = at;
        }

        public HashMap<String, String> getParameter() {
            return parameter;
        }

        public void setParameter(HashMap<String, String> pa) {
            parameter = pa;
        }
    }

    private void init(Context context) {
        mContext = context;
    }

    public List<? extends Map<String, ?>> mItemList;

    @Override
    public void onCreate() {
        logd("");
        init(getApplicationContext());
        super.onCreate();
        //tom_chang_20180822 add start, for get modem info using QcRilHook
        if (FactoryKitPro.PRODUCT_SQ53 || FactoryKitPro.PRODUCT_SQ51S || FactoryKitPro.PRODUCT_SQ53Q)
        mQcRILHook = new QcRilHook(getApplicationContext());
        //tom_chang_20180822 add end, for get modem info using QcRilHook
    }

    @Override
    public void onLowMemory() {
        logd("");
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        logd("");
        super.onTerminate();
    }

    public static MainApp getInstance() {
        if (mMainApp == null)
            mMainApp = new MainApp();
        return mMainApp;
    }

    public MainApp() {
    }

    public Context getContext() {
        return mContext;
    }

    private static void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }

    public int getServiceCounter() {
        return servcieCounter;
    }

    public void voteServcieCounter() {
        this.servcieCounter++;
    }

    public void unvoteServcieCounter() {
        this.servcieCounter--;
    }

    //tom_chang_20180822 add start, for get modem info using QcRilHook
    public QcRilHook getQcRilHook() {
        if(mQcRILHook ==null){
            logd("mQcRILHook is null, create a new instance");
            if (FactoryKitPro.PRODUCT_SQ53 || FactoryKitPro.PRODUCT_SQ51S || FactoryKitPro.PRODUCT_SQ53Q)
            mQcRILHook = new QcRilHook(getApplicationContext());
        }
        return mQcRILHook;
    }
    // private GpsStatus mGpsStatus;
    // private Iterable<GpsSatellite> mSatellites;
    // List<String> satelliteList = new ArrayList<String>();
    // GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener() {
    //
    // public void onGpsStatusChanged(int arg0) {
    //
    // switch (arg0) {
    // case GpsStatus.GPS_EVENT_STARTED:
    // break;
    // case GpsStatus.GPS_EVENT_STOPPED:
    // break;
    // case GpsStatus.GPS_EVENT_FIRST_FIX:
    // pass();
    // break;
    // case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
    // logd("GPS_EVENT_SATELLITE_STATUS");
    // mGpsStatus = mLocationManager.getGpsStatus(null);
    // mSatellites = mGpsStatus.getSatellites();
    // Iterator<GpsSatellite> iterator = mSatellites.iterator();
    // int count = 0;
    // satelliteList.clear();
    // while (iterator.hasNext()) {
    // GpsSatellite gpsS = (GpsSatellite) iterator.next();
    // satelliteList.add(count++, "Prn" + gpsS.getPrn() + " Snr:" +
    // gpsS.getSnr());
    // }
    // if (count >= MIN_SAT_NUM)
    // pass();
    // break;
    // default:
    // break;
    // }
    //
    // }
    //
    // };

}
