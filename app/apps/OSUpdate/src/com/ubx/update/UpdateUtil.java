/**
 * Copyright (c) 2011-2012, Qualcomm Technologies, Inc.
 * All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 * Developed by QRD Engineering team.
 */

package com.ubx.update;

import com.ubx.update.https.Cer;
import com.ubx.update.https.HttpsManager;
import com.ubx.update.misc.Constants;
import com.ubx.update.service.UpdateCheckService;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
//import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//import junit.framework.Test;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
//import android.device.PrinterManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import android.os.SystemProperties;
//import android.device.MaxqManager;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.device.DeviceManager;
import android.os.Bundle;

public class UpdateUtil {

    public static final String KEY_UPDATE_FILE_PATH = "update_path";

    public static final String KEY_UPDATE_FILE_SIZE = "update_size";

    public static final String KEY_UPDATE_FILE_DATE = "update_date";
    public static final String KEY_UPDATE_FILE_VERSION = "update_version";
    public static final String KEY_UPDATE_DOWNLOADED = "update_downloaded";

    // used for push update results to server
    public static final String TAG_UPGRADE_FAILED_REASON = "UpgradeFailedReason";
    public static final String TAG_BASE_OS_VERSION = "BaseOsVersion";
    public static final String TAG_IS_UPGRADE = "IsUpgrade";
    public static final int KEEP_FAILED_REASON = -1;
    public static final int VERIFICATION_FAILED = 0;
    public static final int LOW_BATTERY_LEVEL = 1;
    public static final int CHECK_FAILED_IN_RECOVERY = 2;
    public static final int OTHER = 3;
    public static final int NULL_REASON = 4;

    public static final String SN_PROPERTY = Build.PROJECT.equals("SQ21") ? "ro.serialno" : Build.PROJECT.equals("SQ46M") ? "persist.sys.device.sn" : "persist.sys.product.serialno";

    public static final String[] UPGRADE_FAILED_REASONS = {
        "Verification-failed-in-the-system",
        "Low-battery-level-in-the-system",
        "Check-failed-in-the-recovery",
        "Other",
        "null"
    };

    private static final boolean isRUYDE = SystemProperties.get("persist.sys.ru.yde", "false").equals("true");
    // urovo huangjiezhou add begin 2021/09/14
    private static final String CUSTOM_SERVER_URL = SystemProperties.get("persist.sys.setotaserver.url", "false");
    private static final String CUSTOM_TRUST_FILE =  SystemProperties.get("persist.sys.settrustcert.file", "none");
    // urovo huangjiezhou add end

    /**
     * 0: normal 1:delta
     */
    public static final String KEY_UPDATE_MODE = "update_mode";

    public static final String TAG = "OSUpdateQRDUpdate";

    private static final boolean DEBUG = true;
    private static void test(Context context, String address) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(context.getAssets().open("gd_bundle-g2-g1.crt"));
            final Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                Log.i(TAG, "ca=" + ((X509Certificate) ca).getSubjectDN());
                Log.i(TAG, "key=" + ((X509Certificate) ca).getPublicKey());
            } finally {
                caInput.close();
            }
            int cas = 1;
            
            // Create an SSLContext that uses our TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS","AndroidOpenSSL");
            
            if(cas == 1) {
                sslContext.init(null, new TrustManager[]{
                        new X509TrustManager() {
                            @Override
                            public void checkClientTrusted(X509Certificate[] chain,
                                    String authType)
                                    throws CertificateException {

                            }

                            @Override
                            public void checkServerTrusted(X509Certificate[] chain,
                                    String authType)
                                    throws CertificateException {
                                Log.i(TAG, "checkServerTrusted " + chain.length );
                                int length = 0;
                                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                                for (X509Certificate cert : chain) {
                                    Log.i(TAG, "checkServerTrusted length= " + length++ );
                                    // Make sure that it hasn't expired.
                                    cert.checkValidity();
                                    /*ByteArrayInputStream bais;
                                    bais = new ByteArrayInputStream(chain[0].getEncoded());
                                    cert = (X509Certificate) factory.generateCertificate(bais);
                                    try {
                                        bais.close();
                                    } catch (IOException e1) {
                                        // TODO Auto-generated catch block
                                        e1.printStackTrace();
                                    }*/
                                    // Verify the certificate's public key chain.
                                    Log.i(TAG, "checkServerTrusted getSubjectDN= " + cert.getSubjectDN() );
                                    Log.i(TAG, "checkServerTrusted getEncoded= " + cert.getEncoded() );
                                    Log.i(TAG, "checkServerTrusted getPublicKey= " + cert.getPublicKey() );
                                    try {
                                        cert.verify(((X509Certificate) ca).getPublicKey());
                                        Log.i(TAG, "==================================================checkServerTrusted verify==================================================pass");
                                    } catch (NoSuchAlgorithmException e) {
                                        e.printStackTrace();
                                    } catch (InvalidKeyException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchProviderException e) {
                                        e.printStackTrace();
                                    } catch (SignatureException e) {
                                        e.printStackTrace();
                                    } catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                            @Override
                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[0];
                            }
                        }
                }, null);
            } else {
                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);
                sslContext.init(null, tmf.getTrustManagers(), null);
            }

            URL url = new URL(address);
            HttpsURLConnection urlConnection =
                    (HttpsURLConnection)url.openConnection();
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            urlConnection.setConnectTimeout(30000);
            urlConnection.setReadTimeout(20000);
            urlConnection.connect();
            InputStream in = urlConnection.getInputStream();
           final String result = parseSendMessageResponse(in);
           Log.i(TAG, "result==================================================" +result);
          } catch (CertificateException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
          } catch (KeyManagementException e) {
            e.printStackTrace();
          } catch (NoSuchProviderException e) {
            e.printStackTrace();
          }  catch (Exception e) {
              e.printStackTrace();
          }
    }
    private  static String parseSendMessageResponse(InputStream in) throws Exception
    {
        // Read from the input stream and convert into a String.
        InputStreamReader inputStream = new InputStreamReader(in);
        BufferedReader buff = new BufferedReader(inputStream);

        StringBuilder sb = new StringBuilder();
        String line = buff.readLine();
        while(line != null)
        {
            sb.append(line);
            line = buff.readLine();
        }

        return sb.toString();
    }
    public static List<UpdateInfo> getUpdateInfo(String address,Context mContext) {
        //log("get updates from: " + address);
        UpdateInfoHandler updateInfoHandler = new UpdateInfoHandler();
        HttpsURLConnection connection = null;
        InputStream in = null;
        try {
            //test(mContext, address);
            URL url = new URL(address);
            SSLContext sslContext;
            if(Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
                HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open("gd_bundle-g2-g1.crt"));
                sslContext = HttpsManager.getInstance().getSSLContext(mContext);
                } else if (isRUYDE) {
                               HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open("mdm_urovo_com_2021_06_27.crt"));
                               sslContext = HttpsManager.getInstance().getSSLContext(mContext);
            // urovo huangjiezhou add begin 2021/09/14
            } else if(!CUSTOM_SERVER_URL.equals("false") && CUSTOM_SERVER_URL.startsWith("https")){
                HttpsManager.getInstance().setTrustrCertificates(mContext.getAssets().open(CUSTOM_TRUST_FILE));
                sslContext = HttpsManager.getInstance().getSSLContext(mContext);
            // urovo huangjiezhou add end
                       } else {
                sslContext = Cer.getSSLContext(mContext);
            }
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "PacificHttpClient");
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    // TODO Auto-generated method stub
                    return true;
                }
            });
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(20000);
            connection.connect();
            if (connection.getResponseCode() == 404) {
                log("get updates from 404 error! ");
                throw new Exception("404 error!");
            }
            in = connection.getInputStream();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp;
            sp = spf.newSAXParser();
            XMLReader reader = sp.getXMLReader();
            reader.setContentHandler(updateInfoHandler);
            reader.parse(new InputSource(new InputStreamReader(in, "GBK")));
        } catch (Exception e) {
            Log.e(TAG,"get updates error:" + e);
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        log("get updates end! ");
        return updateInfoHandler.getUpdates();
    }
    public static List<UpdateInfo> getUpdateInfo(String address) {
        // log("get updates from: " + address);
        Log.d("OSUpdate","getUpdateInfo:address---------->"+address);
        UpdateInfoHandler updateInfoHandler = new UpdateInfoHandler();
        HttpURLConnection connection = null;
        InputStream in = null;
        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "PacificHttpClient");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(20000);
            if (connection.getResponseCode() == 404) {
                throw new Exception("404 error!");
            }
            in = connection.getInputStream();
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp;
            sp = spf.newSAXParser();
            XMLReader reader = sp.getXMLReader();
            reader.setContentHandler(updateInfoHandler);
            reader.parse(new InputSource(new InputStreamReader(in, "GBK")));
        } catch (Exception e) {
            log("get updates error:" + e);
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return updateInfoHandler.getUpdates();
    }

    public static void updateFileSize(Context context, List<UpdateInfo> updates) {
        if (updates == null || updates.size() == 0)
            return;
        for (UpdateInfo update : updates) {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(update.getDownloadURL());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "PacificHttpClient");
                connection.setConnectTimeout(30000);
                connection.setReadTimeout(20000);
                update.setSize(connection.getContentLength());
            } catch (Exception e) {
                log("update updates error:" + e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    public static boolean rebootInstallDelta(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction("android.system.agent");
            intent.putExtra("para", "reboot,recovery");
            context.startService(intent);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "failed to reboot and install delta:" + e);
            return false;
        }
    }

     public static boolean isNetworkAvailable(Context context) {
         ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
         boolean wifi=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
         boolean internet=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
         return activeNetworkInfo != null;
     }

    /*
     *home key and statubar control
     */
    public static void handleForceupgradingView(boolean tag){
        Log.d(TAG,"handleForceupgradingView---------------------tag:"+tag);
        DeviceManager deviceManager = new DeviceManager();
        //deviceManager.enableHomeKey(tag);
        deviceManager.setSettingProperty("System-force_upgrading_view",String.valueOf(tag));
        deviceManager.enableStatusBar(tag);
        deviceManager.setRightKeyEnabled(tag);
        deviceManager.setLeftKeyEnabled(tag);
        deviceManager.setHomeKeyEnabled(tag);
    }

    public static Bundle getSystemCurrentTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");
        Date mDate = new Date(System.currentTimeMillis());
        String currentDate = simpleDateFormat.format(mDate).substring(0,8);
        String currentTime = simpleDateFormat.format(mDate).substring(9,17);
        Log.d(TAG,"currentDate--->"+currentDate+" currentTime--->"+currentTime);
        Bundle bundle = new Bundle();
        bundle.putString("currentDate",currentDate);
        bundle.putString("currentTime",currentTime);
        return bundle;
    }

    public static void forceExit() {
        handleForceupgradingView(true);
        ForceUpdateManager.getInstance().setforceUpdate(false);
        ForceUpdateManager.getInstance().exit();
    }
    /**
     * call uid system to create system directory
     */
    public static boolean mkDeltaDir(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction("android.system.fullagent");
            String extra = "mkdir,/cache/fota";
            intent.putExtra("para", extra);
            context.startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "make delta dir failed:" + e);
            return false;
        }
        return true;
    }

    public static boolean writeDeltaCommand() {
        String filePath = "/cache/fota/ipth_config_dfs.txt";
        String command = "IP_PREVIOUS_UPDATE_IN_PROGRESS";
        boolean res = true;
        FileWriter mFileWriter = null;
        try {
            mFileWriter = new FileWriter(new File(filePath));
            mFileWriter.write(command);
        } catch (IOException e) {
            Log.e(TAG, "write delta command failed:" + e);
            res = false;
        } finally {
            try {
                mFileWriter.close();
            } catch (IOException e) {
            }
        }
        return res;
    }

    public static boolean copyToDeltaFile(File srcFile) {
        File dstFile = new File("/cache/fota/ipth_package.bin");
        if (dstFile.exists()) {
            dstFile.delete();
        }
        InputStream in = null;
        OutputStream out = null;
        int cnt;
        byte[] buf = new byte[4096];
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(dstFile);
            while ((cnt = in.read(buf)) >= 0) {
                out.write(buf, 0, cnt);
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "failed to copy delta file:" + e);
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static String formatSize(float size) {
        long kb = 1024;
        long mb = (kb * 1024);
        long gb = (mb * 1024);
        if (size < kb) {
            return String.format("%d bytes", (int) size);
        } else if (size < mb) {
            return String.format("%.1f kB", size / kb);
        } else if (size < gb) {
            return String.format("%.1f MB", size / mb);
        } else {
            return String.format("%.1f GB", size / gb);
        }
    }

    private static void log(String msg) {
        if (DEBUG) {
            Log.d(TAG, msg);
        }
    }

    /**
     * @param context
     * @param path
     * @param isDelta 0:normai 1:delta
     */
    public static void saveUpdate(Context context, String path, boolean isDelta ,String version, boolean downloadSuccess) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.putString(KEY_UPDATE_FILE_PATH, path);
        edit.putLong(KEY_UPDATE_FILE_SIZE, new File(path).length());
        edit.putLong(KEY_UPDATE_FILE_DATE, new File(path).lastModified());
        edit.putString(KEY_UPDATE_FILE_VERSION, version);
        edit.putInt(KEY_UPDATE_MODE, isDelta ? 1 : 0);
        edit.putBoolean(KEY_UPDATE_DOWNLOADED, downloadSuccess);
        edit.commit();
    }

    public static void deteteUpdate(Context context) {
        Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.remove(KEY_UPDATE_FILE_PATH);
        edit.remove(KEY_UPDATE_FILE_SIZE);
        edit.remove(KEY_UPDATE_FILE_DATE);
        edit.remove(KEY_UPDATE_FILE_VERSION);
        edit.remove(KEY_UPDATE_MODE);
        edit.commit();
    }
    public static final String KEY_TAG_REMOTE_URL = "tag_remote_url";
    public static final String KEY_TAG_UPDATE_SIZE = "tag_update_size";
    public static final String KEY_TAG_LOCAL_PATH = "tag_local_path";
    public static final String KEY_TAG_LOCAL_SIZE = "tag_local_size";
    public static final String KEY_TAG_LOCAL_TIME = "tag_local_time";
    public static boolean clearTag(Context context) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.remove(KEY_TAG_REMOTE_URL);
        editor.remove(KEY_TAG_UPDATE_SIZE);
        editor.remove(KEY_TAG_LOCAL_PATH);
        editor.remove(KEY_TAG_LOCAL_SIZE);
        editor.remove(KEY_TAG_LOCAL_TIME);
        return editor.commit();
    }

    public static String getLastUpdate(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String filepath = sp.getString(KEY_UPDATE_FILE_PATH, null);
        if (filepath != null) {
            File f = new File(filepath);
            if (f.length() == sp.getLong(KEY_UPDATE_FILE_SIZE, -1)
                    && f.lastModified() == sp.getLong(KEY_UPDATE_FILE_DATE, -1))
                return filepath;
        }
        return null;
    }

    /**
     * 0:normal 1:delta
     *
     * @param context
     * @return
     */
    public static boolean getLastIsDelta(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(KEY_UPDATE_MODE, 0) == 1;
    }
    
    //M806_P4_06_YBXX_AU88_407_R_0_150327_01
    public static String[] getCurrentVersionInfo() {
        String buildId = Build.DISPLAY;
        //android.util.Log.i("===================", buildId);
        if(buildId != null && !buildId.equals(""))
            return buildId.split("_");
        return null;
    }
    private static int covertInt(String info) {
        try{
            return Integer.parseInt(info);
        } catch(NumberFormatException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public static boolean hasNewUpdate(String updateInfo) {
        String[] curVersion = getCurrentVersionInfo();
        int curDate = covertInt(curVersion[curVersion.length -2]);
        
        int curCount = covertInt(curVersion[curVersion.length -1]);
        //android.util.Log.i("===================", "upDate=== " + curDate + " ==curCount==" + curCount);
        if(updateInfo != null && !updateInfo.equals("")) {
            String[] updateBuildId = updateInfo.split("_");
            int upDate = covertInt(updateBuildId[updateBuildId.length-2]);
            int upCount = covertInt(updateBuildId[updateBuildId.length-1]);
            //android.util.Log.i("===================", "upDate=== " + upDate + " ==upCount==" + upCount);
            if(curDate < upDate || curCount < upCount) {
                return true;
            }
        }
        return false;
    }
    
    public static String getCurrentVersion() {
        String buildId = SystemProperties.get("ro.vendor.build.id", Build.ID);
        //android.util.Log.i("===================", buildId);
        Matcher mMatcher = Pattern.compile(".*(\\d{6}_\\d{2}).*").matcher(buildId);
        if(mMatcher.find())
            buildId = mMatcher.group(1);
        System.out.println( "   getCurrentVersion ============================    " +buildId.toString());
        if(buildId != null && !buildId.equals("")){
            String[] display = buildId.split("_");
	    String androidVersion = Build.VERSION.RELEASE;
            StringBuilder sb = new StringBuilder(androidVersion.indexOf(".") != -1 ? androidVersion.substring(0, androidVersion.indexOf(".")) : androidVersion);
            if(display.length < 2){
                return "0";
            }
            int date = covertInt(display[display.length - 2]);
            sb.append(".");
            sb.append(date/10000);
            int mm = date%10000;
            if(mm >= 1000 &&  mm < 10000)
                sb.append("." +mm );
            else if( 100 <= mm && mm < 1000)
                sb.append(".0" +mm );
            else if( 10 <= mm && mm < 100)
                sb.append(".00" +mm );
            else if( 0 <= mm && mm < 10)
                sb.append(".000" +mm );
                
            int count = covertInt(display[display.length - 1]);
            if( 0 <= count && count < 10)
            sb.append(".0" +count );
            else sb.append("." +count );
            Log.d(TAG,"currentVersion========>"+sb.toString());
            return sb.toString();
        } else {
            return "0";
        }
    }
    
    private static String getSEVersion(){
        //String version = get32550Versino();
        String version = android.os.SystemProperties.get("urv.se.version","");
        if(version == null || version.equals("")) return Build.VERSION.RELEASE + ".0.0.0";
        int number =version.indexOf("V");
        if(number == -1)  return Build.VERSION.RELEASE + ".0.0.0";
        String seVersion = Build.VERSION.RELEASE+"."+ version.substring(number+1, number+6);
        return seVersion;
    }

    private static String getSCVersion(){
        String version = getScVersinon();
        if(version == null || version.equals("")) return Build.VERSION.RELEASE + ".0.0.0";
        int number =version.indexOf("V");
        if(number == -1)  return Build.VERSION.RELEASE + ".0.0.0";
        String scVersion = Build.VERSION.RELEASE+"."+ version.substring(number+1, number+6);
        return scVersion;
    }

    public static boolean isDivision(){
         if(Build.PROJECT.equals("SQ27") || Build.PROJECT.equals("SQ27D") || Build.PROJECT.equals("SQ27C") || Build.PROJECT.equals("SQ27T") 
          || Build.PROJECT.equals("SQ27TC") || Build.PROJECT.equals("SQ27TD") || Build.PROJECT.equals("SQ27TE") || Build.PROJECT.equals("SQ26B") || Build.PROJECT.equals("SQ26TB")){
              return true;
         }
         return false;
    }

    public static boolean isverifysign(){
        if(Build.PROJECT.equals("SQ46") && Build.PWV_CUSTOM_CUSTOM.equals("XX")){
            return false;
        }
        return true;
    }

    private static String get32550Versino(){
    	/*MaxqManager mMaxNative = new MaxqManager();
    	int ret = mMaxNative.open();
        if(ret != 0)
        return null;
        byte[] response = new byte[256];
        byte[] reslen = new byte[1];
        mMaxNative.getFirmwareVersion(response, reslen);
        String version = new String(response, 0, (int) reslen[0]);
        mMaxNative.close();
    	return version;*/
        return null;
    }

    private static String getScVersinon(){
    	/*PrinterManager pm = new PrinterManager();
        String version;
        byte[] response = new byte[256];
        byte[] reslen = new byte[1];
        int ret = pm.getFirmwareVersion(response, reslen);
        if(ret == 0){
            version = new String(response, 0, (int) reslen[0]);
            return version;
        }*/
    	return null;
    }

    //HHT5FC_V4.15.0609.01
    private static String getSFCurrentVersion() {
        String buildId = Build.DISPLAY;
        if(buildId != null && !buildId.equals("")){
            try{
                int index = buildId.indexOf("_V");
                return buildId.substring(index+2);
            } catch(IndexOutOfBoundsException e) {
                return "0";
            }
        } else {
            return "0";
        }
    }
    
    private static String getCustomName() {
        if(Build.PWV_CUSTOM_CUSTOM.equals("XX")) {
            boolean sign = android.os.SystemProperties.getBoolean("pwv.custom.sign", false);
            return sign ? "StandardOS-S" : "StandardOS-N";
        } else if (Build.PWV_CUSTOM_CUSTOM_ATTACH.equals("XX") || Build.PWV_CUSTOM_CUSTOM_ATTACH.equals("xx")) {
            return Build.PWV_CUSTOM_CUSTOM;
        } else {
            return Build.PWV_CUSTOM_CUSTOM_ATTACH;
        }
    }

    private static String getSECustomName() {
         boolean sign = android.os.SystemProperties.getBoolean("pwv.custom.sign", false);
         return sign ? "SEFW-S" : "SEFW-N";
    }

    private static String getSCCustomName() {
         boolean sign = android.os.SystemProperties.getBoolean("pwv.custom.sign", false);
         return sign ? "PPKG-S" : "PPKG-N";
    }

    public static String buildUrl(Context ctx, String address) {
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        String language = android.os.SystemProperties.getBoolean("pwv.custom.enbuild", false) ? "english" : "chinese";
        String RFType = android.os.SystemProperties.get("persist.radio.multisim.config", "");
        if(RFType.equals("dsds")){
            RFType = "DS";
        }else{
            RFType = "WE";
        }
        if (ni == null)
            return null;

        int connType = ni.getType();
        String conn;
        if (connType == ConnectivityManager.TYPE_WIFI)
            conn = "Wifi";
        else
            conn = "4G";

        StringBuilder sb = new StringBuilder();
        sb.append("&dvcId=").append(SystemProperties.get(SN_PROPERTY, ""));
        sb.append("&osType=Android");
        sb.append("&osLanguage=").append(language);
        sb.append("&osVersion=").append(Build.PWV_CUSTOM_CUSTOM.equals("SF") ? getSFCurrentVersion() : getCurrentVersion());
        sb.append("&networkType=").append(conn);
        sb.append("&devType=")
        .append(Build.PROJECT).append("_")
        .append(RFType).append("_");
        sb.append(getCustomName());
        sb.append("&isload=true")
        .append("&protocolVer=02");
        android.util.Log.i(TAG, sb.toString());
        String protocol = "?protocol=http";
        if(address.startsWith("https")) {
            protocol = "?protocol=https";
        }
        String url = address + protocol + sb.toString();
        //Log.d(TAG,"OS url ------> "+url);
         //return "https://yun.urovo.com:4443/Update/osUpdate" + "?protocol=http" +"&dvcId=98211813108213&osType=Android&osLanguage=chinese&osVersion=6.18.0601.02&networkType=Wifi&devType=SQ29_WE_UMSPOS&isload=true&protocolVer=02";
        return url;
        //return "http://update.urovo.com:8000/Update/osUpdate?protocol=http&dvcId=97061646271009&osType=Android&osLanguage=chinese&osVersion=5.18.0512.01&networkType=Wifi&devType=SQ27T_AX_StandardOS-N&isload=true&protocolVer=02";
    }
    /*private static String[] getUFSVersion() {
        String[] ufsInfo = new String[]{"0.0.0.0", "PKG-XX"};
        android.device.UfsManager manager = new android.device.UfsManager();
        if(manager.init() != 0) {//没有分区返回非0
            manager.release();
            return ufsInfo;
        }
        
        byte[] usfVersion = new byte[64];
        int ret = manager.getPkgVersion(usfVersion);
        Log.i(TAG,"getPkgVersion: usfModel ret= " + ret);
        if(ret >=6 &&  ret < usfVersion.length) {
            String buildId = new String(usfVersion, 0 , ret);
            String[] mods = buildId.trim().split("_");
            buildId = "";
            if(mods.length >= 2) {
                ufsInfo[1] = mods[0];
                buildId = mods[1];
            }
            if(!buildId.equals("")){
                StringBuilder sb = new StringBuilder("5");
                int date = covertInt(buildId);
                sb.append(".");
                sb.append(date/10000);
                int mm = date%10000;
                if(mm >= 1000 &&  mm < 10000)
                    sb.append("." +mm );
                else if( 100 <= mm && mm < 1000)
                    sb.append(".0" +mm );
                else if( 10 <= mm && mm < 100)
                    sb.append(".00" +mm );
                else if( 0 <= mm && mm < 10)
                    sb.append(".000" +mm );
                    
                int count = 1;
                if(mods.length >= 3) {
                    count = covertInt(mods[2]);
                }
                if( 0 <= count && count < 10)
                sb.append(".0" +count );
                else sb.append("." +count );
                
                ufsInfo[0] = sb.toString();
            }
        }
        manager.release();
        return ufsInfo;
    }*/

    private static String[] getUFSVersion() {
        String[] ufsInfo = new String[]{"0.0.0.0", "PKG-XX"};
        ufsInfo[0] = android.os.SystemProperties.get("ro.ufs.build.version","0.0.0.0");
        ufsInfo[1] = "PKG-" + android.os.SystemProperties.get("ro.ufs.custom","XX");
        return ufsInfo;
    }

    public static String buildUFSUpdatePKGUrl(Context ctx, String address) {
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        String language = android.os.SystemProperties.getBoolean("pwv.custom.enbuild", false) ? "english" : "chinese";
        //String RFType = android.os.SystemProperties.get("pwv.rf.type", "WE");
        String RFType = android.os.SystemProperties.get("persist.radio.multisim.config", "");
        if(RFType.equals("dsds")){
            RFType = "DS";
        }else{
            RFType = "WE";
        }
        if (ni == null || address == null)
            return null;

        int connType = ni.getType();
        String conn;
        String[] ufsVerInfo;
        if (connType == ConnectivityManager.TYPE_WIFI)
            conn = "Wifi";
        else
            conn = "4G";

        ufsVerInfo = getUFSVersion();
        StringBuilder sb = new StringBuilder();
        sb.append("&dvcId=").append(SystemProperties.get(SN_PROPERTY, ""));
        sb.append("&osType=Android");
        sb.append("&osLanguage=").append(language);
        sb.append("&osVersion=").append(ufsVerInfo[0]);
        sb.append("&networkType=").append(conn);
        sb.append("&devType=")
        .append(Build.PROJECT).append("_")
        .append(RFType).append("_")
        .append(ufsVerInfo[1]);
        sb.append("&isload=true")
        .append("&protocolVer=02");
        android.util.Log.i(TAG, sb.toString());
        String url = address + "?protocol=http" + sb.toString();
        //Log.e(TAG,"UFS url ----> "+url);
        return url;
    }

    public static String buildSEUpdatePKGUrl(Context ctx, String address) {
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        String language = android.os.SystemProperties.getBoolean("pwv.custom.enbuild", false) ? "english" : "chinese";
        String RFType = android.os.SystemProperties.get("persist.radio.multisim.config", "");
        if(RFType.equals("dsds")){
            RFType = "DS";
        }else{
            RFType = "WE";
        }
        if (ni == null || address == null)
            return null;

        int connType = ni.getType();
        String conn;

        if (connType == ConnectivityManager.TYPE_WIFI)
            conn = "Wifi";
        else
            conn = "4G";

        StringBuilder sb = new StringBuilder();
        sb.append("&dvcId=").append(SystemProperties.get(SN_PROPERTY, ""));
        sb.append("&osType=Android");
        sb.append("&osLanguage=").append(language);
        sb.append("&osVersion=").append(getSEVersion());
        sb.append("&networkType=").append(conn);
        sb.append("&devType=")
        .append(Build.PROJECT).append("_")
        .append(RFType).append("_")
        .append(getSECustomName());
        sb.append("&isload=true")
        .append("&protocolVer=02");
        android.util.Log.i(TAG, sb.toString());
        String url = address + "?protocol=http" + sb.toString();
        //Log.e(TAG,"SE url ------> "+url);
        return url;
    }

    public static String buildSCUpdatePKGUrl(Context ctx, String address) {
        ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        String language = android.os.SystemProperties.getBoolean("pwv.custom.enbuild", false) ? "english" : "chinese";
        String RFType = android.os.SystemProperties.get("persist.radio.multisim.config", "");
        if(RFType.equals("dsds")){
            RFType = "DS";
        }else{
            RFType = "WE";
        }
        if (ni == null || address == null)
            return null;

        int connType = ni.getType();
        String conn;

        if (connType == ConnectivityManager.TYPE_WIFI)
            conn = "Wifi";
        else
            conn = "4G";

        StringBuilder sb = new StringBuilder();
        sb.append("&dvcId=").append(SystemProperties.get(SN_PROPERTY, ""));
        sb.append("&osType=Android");
        sb.append("&osLanguage=").append(language);
        sb.append("&osVersion=").append(getSCVersion());
        sb.append("&networkType=").append(conn);
        sb.append("&devType=")
        .append(Build.PROJECT).append("_")
        .append(RFType).append("_")
        .append(getSCCustomName());
        sb.append("&isload=true")
        .append("&protocolVer=02");
        android.util.Log.i(TAG, sb.toString());
        String url = address + "?protocol=http" + sb.toString();
        //Log.e(TAG,"SC url ------> "+url);
        return url;
    }

    public static void pushUpgradeResultToServer(Context context, int reason) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                pushUpdateInfoToServer(context, reason);
            }
        }).start();
    }

    public static void pushUpdateInfoToServer(Context context, int reason) {
        if (reason != KEEP_FAILED_REASON)
            setUpgradeFailedReason(reason);
        String address = "http://update.urovo.com:8000/Update/osUpdate";
        List<UpdateInfo> resultInfos;
        if(address.startsWith("https")){
            resultInfos = getUpdateInfo(resultToServerUrl(address), context);
        }else{
            resultInfos = getUpdateInfo(resultToServerUrl(address));
        }
        if (resultInfos != null && resultInfos.size() > 0) {
            UpdateInfo info = resultInfos.get(0);
            Log.e(TAG,"info >>>>> "+info.getIsSuccess()+", msg >>>>>> "+info.getFailedMeg());
            if (!info.getIsSuccess()) {
                //push result to server failed, push it 5min later
                Log.d(TAG, "push update result to serve failed, try it again five minutes later!!!");
                schedulePushResultToServer(context, 300000);
                return;
            }
            Log.d(TAG, "the upgrade result has been pushed to server, reset baseOsVersion to currentOsVersion!!!!");
            setBaseOsVersion();
            writeOemPartition(TAG_IS_UPGRADE, "0");
            return;
        }
        schedulePushResultToServer(context, 300000);
    }

    public static void schedulePushResultToServer(Context context, int updateFrequency) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(UpdateSettings.PUSH_RESULT_TO_SERVER);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Clear any old alarms and schedule the new alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        if (updateFrequency != Constants.UPDATE_FREQ_NONE) {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + updateFrequency, pi);
        }
    }

    // success: return true   failed: return false
    public static boolean isUpgradeSuccess() {
        String baseOsVersion = readOemPartition(TAG_BASE_OS_VERSION);
        String currentOsVersion = getCurrentVersion().substring(getCurrentVersion().length() - 10);
        Log.d(TAG,"baseOsVersion=======" + baseOsVersion + "  currentOsVersion=======" + currentOsVersion);
        if (baseOsVersion != null && currentOsVersion != null) {
            return !baseOsVersion.equals(currentOsVersion);
        }
        return false;
    }

    public static String resultToServerUrl(String address) {
        String RFType = android.os.SystemProperties.get("persist.radio.multisim.config", "");
        if(RFType.equals("dsds")){
            RFType = "DS";
        }else{
            RFType = "WE";
        }

        String updateSucess = String.valueOf(isUpgradeSuccess());
        String failedReason = UPGRADE_FAILED_REASONS[Integer.parseInt(readOemPartition(TAG_UPGRADE_FAILED_REASON))];

        String dvcId = SystemProperties.get(SN_PROPERTY, "");
        Log.d(TAG, "dvcId=======" + dvcId);

        StringBuilder sb = new StringBuilder();
        sb.append("Update/finishedNotice");
        sb.append("?dvcId=").append(dvcId);
        sb.append("&osVersion=").append(getCurrentVersion());
        sb.append("&devType=")
        .append(Build.PROJECT).append("_")
        .append(RFType).append("_");
        sb.append(getCustomName());
        sb.append("&finishedTime=");
        sb.append(getCurrentDate());
        sb.append("&upgFlag=");
        sb.append(updateSucess);
        sb.append("&failedRes=");
        sb.append(failedReason);
        sb.append("&baseOsVersion=");
        sb.append(Build.VERSION.RELEASE + "." + readOemPartition(TAG_BASE_OS_VERSION));
        int index = address.indexOf("Update");
        if(index > 0){
            address = address.substring(0, index);
        }
        String url = address + sb.toString();
        Log.e(TAG,"resultToServerUrl  ====> " + url);
        return url;
    }

    public static String getCurrentDate(){
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now=new Date();
        String date = mSimpleDateFormat.format(now);
        return date;
    }

    public static void setUpgradeFailedReason(int reason) {
        writeOemPartition(TAG_UPGRADE_FAILED_REASON, String.valueOf(reason));
    }

    public static void setBaseOsVersion() {
        String currentVer = getCurrentVersion();
        writeOemPartition(TAG_BASE_OS_VERSION, currentVer.substring(currentVer.length() - 10));
    }

    public static void writeNode(String node,String value) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(node);
            outputStream.write(value.getBytes());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public static String getNode(String node) {
        FileInputStream inputStream = null;
        byte[] buffer = null;
        try {
            inputStream = new FileInputStream(node);
            buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // urovo huangjiezhou add begin on 20210916
        if (buffer == null) {
            Log.d(TAG, "The buffer get from node is null==>the node is not supported for this device");
            return null;
        } else { 
        // urovo huangjiezhou add end on 20210916
            Log.i(TAG, "getNode =" + new String(buffer).trim());
            return new String(buffer).trim();
        }
    }

    public static void writeOemPartition(String where, String value) {
        writeNode("/sys/kernel/oem/name", where);
        writeNode("/sys/kernel/oem/value_str", value);
    }

    public static String readOemPartition(String where) {
        writeNode("/sys/kernel/oem/name", where);
        return getNode("/sys/kernel/oem/value_str");
    }

    public static String getUTEServerUrl () {
        // urovo modify huangjiezhou 20210826
        String deviceMode = Build.PROJECT.equals("SQ46M") ? "WD100" : Build.MODEL.toLowerCase();
        String CUSTOM = Build.PWV_CUSTOM_CUSTOM.equals("UTEWO") ? "Wifi" : "";
        String address = DownloadManager.UTE_DEFAULT_SERVER_PREFIX + String.format(DownloadManager.UTE_DEFAULT_SERVER_URL_FORMAT, deviceMode, CUSTOM, Build.PROJECT);
        return address;
    }
    public static String getCurrentBuildVersion() {
        String buildId = Build.DISPLAY;
        Matcher mMatcher = Pattern.compile(".*(\\d{6}_\\d{2}).*").matcher(buildId);
        if(mMatcher.find())
            buildId = mMatcher.group(1);
        if(buildId != null && !buildId.equals("")){
            System.out.println( " getCurrentVersion ============================" +buildId.toString());
            return buildId.replace("_", "");
        }
        return "0";
    }
    public static String getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni.getType() == ConnectivityManager.TYPE_WIFI)
            return  "Wifi";
        return "";
    }
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }
    /**
     * 
     * @param context
     * @param updateFrequency 更新频率　毫秒
     */
    public static void scheduleUpdateService(Context context, int updateFrequency) {
        // Load the required settings from preferences
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //long lastCheck = prefs.getLong(Constants.LAST_UPDATE_CHECK_PREF, 0);
        //Log.d(TAG, "LAST_UPDATE_CHECK_PREF" + lastCheck);
        Log.d(TAG,"updateFrequency------------->"+updateFrequency+" ms");
        // Get the intent ready
      /*  Intent i = new Intent(context, UpdateCheckService.class);
        i.setAction(UpdateCheckService.ACTION_CHECK);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);*/

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(UpdateSettings.NOTIFICATION);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        
        // Clear any old alarms and schedule the new alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        if (updateFrequency != Constants.UPDATE_FREQ_NONE) {
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + updateFrequency, updateFrequency, pi);
        }
    }

    // urovo weiyu add on 2020-07-02 start
    /**
     *
     * @param context
     * @param hour
     * @param min
     * @param second
     */
    public static void scheduleUpdateService(Context context, int hour, int min, int second) {

	Log.d("TAG","scheduleUpdateService-------------------------------");
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setAction(UpdateSettings.TIMINGUPDATECHECK);
        PendingIntent pi = PendingIntent.getBroadcast(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Clear any old alarms and schedule the new alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
	am.setRepeating(AlarmManager.RTC_WAKEUP, getTimeDiff(hour,min,second), AlarmManager.INTERVAL_DAY, pi);
    }

    public static long getTimeDiff(int hour, int min, int second){
        Calendar ca=Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY,hour);
        ca.set(Calendar.MINUTE,min);
        ca.set(Calendar.SECOND,second);
        return ca.getTimeInMillis();
    }
    // urovo weiyu add on 2020-07-02 end

    public static final String EXTRA_UPDATE_NON_INTERACTIVE = "update_non_interactive";
    public static void scheduleUpdateCheck(Context context, String action, int interval, long lastCheck, boolean oneshot) {

        Intent updateCheck = new Intent(context, UpdateCheckService.class);
        updateCheck.setAction(action);
        //updateCheck.putExtra(EXTRA_UPDATE_NON_INTERACTIVE, true);
        PendingIntent pi = PendingIntent.getService(context, 0, updateCheck,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);

        long intervalMillis = ((long)interval) * 1000;
        long now = System.currentTimeMillis();
        if (!oneshot) {
            if (interval > Constants.UPDATE_FREQ_AT_BOOT) {
                am.setRepeating(AlarmManager.RTC, lastCheck + intervalMillis, intervalMillis, pi);
                Log.i(TAG, "Scheduled update check for "
                        + (((lastCheck + intervalMillis) - now) / 1000) + " seconds from now"
                        + " repeating every " + interval + " seconds");
            }
        } else {
            am.set(AlarmManager.RTC, lastCheck + intervalMillis, pi);
            Log.i(TAG, "Scheduled update check for "
                    + (((lastCheck + intervalMillis) - now) / 1000) + " seconds from now");
        }
    }

    public static String secondToTime(int second,Context ctx) {
        int h = 0;
        int d = 0;
        int s = 0;
        int temp = second % 3600;
        if (second > 3600) {
            h = second / 3600;
            if (temp != 0) {
                if (temp > 60) {
                    d = temp / 60;
                    if (temp % 60 != 0) {
                        s = temp % 60;
                    }
                } else {
                    s = temp;
                }
            }
        } else {
            d = second / 60;
            if (second % 60 != 0) {
                s = second % 60;
            }
        }
        if (h==0){
            return ctx.getResources().getQuantityString(R.plurals.minute, d, d) + ctx.getResources().getQuantityString(R.plurals.second, s, s);
        }else {
            return ctx.getResources().getQuantityString(R.plurals.hour, h, h) + ctx.getResources().getQuantityString(R.plurals.minute, d, d) + ctx.getResources().getQuantityString(R.plurals.second, s, s);
        }
    }
}
