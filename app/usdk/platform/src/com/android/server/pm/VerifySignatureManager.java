package com.android.server.pm;


import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.admin.IDevicePolicyManager;
import android.app.backup.IBackupManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.IOnPermissionsChangeListener;
import android.content.pm.IPackageDataObserver;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageDeleteObserver2;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageInstaller;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.InstrumentationInfo;
import android.content.pm.IntentFilterVerificationInfo;
import android.content.pm.KeySet;
//import android.content.pm.PackageCleanItem;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInfoLite;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.LegacyPackageDeleteObserver;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.ActivityIntentInfo;
import android.content.pm.PackageParser.PackageLite;
import android.content.pm.PackageParser.PackageParserException;
import android.content.pm.PackageStats;
import android.content.pm.PackageUserState;
import android.content.pm.ParceledListSlice;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.Signature;
import android.content.pm.UserInfo;
import android.content.pm.VerificationParams;
import android.content.pm.VerifierDeviceIdentity;
import android.content.pm.VerifierInfo;
import android.content.res.Resources;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Debug;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Environment.UserEnvironment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.KeyStore;
import android.security.SystemKeyStore;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.EventLog;
import android.util.ExceptionUtils;
import android.util.Log;
import android.util.LogPrinter;
import android.util.MathUtils;
import android.util.PrintStreamPrinter;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.Xml;
import android.view.Display;

import dalvik.system.DexFile;
import dalvik.system.VMRuntime;

import libcore.io.IoUtils;
import libcore.util.EmptyArray;

import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.IMediaContainerService;
import com.android.internal.app.ResolverActivity;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.content.PackageHelper;
import com.android.internal.os.IParcelFileDescriptorFactory;
import com.android.internal.os.SomeArgs;
import com.android.internal.os.Zygote;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.server.LocalServices;
import com.android.server.SystemConfig;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.security.cert.Certificate;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import android.util.apk.ApkSignatureSchemeV2VerifierEx;
import com.andpos.CertMaster;
import com.andpos.PosConstant;
import com.andpos.AuthSign;
import com.andpos.AllinPayZipFile;
import com.andpos.PosConstant;
import com.andpos.AuthSignCommon;
import com.andpos.CCBApkFile;
//import android.device.MaxNative;
import com.android.server.sign.UrovoSecurityPermission;
import com.andpos.ICBCApkFile;
/**
 *这个类有PackageManagerService服务调用，统一管理所有Apk签名验证
 * Created by rocky on 18-11-7.
 */

public class VerifySignatureManager {
    public static final int INSTALL_FAILED_AUTH_CERTIFICATE = -117;
    private static final String TAG = "VerifySignatureManager";
    private byte[] acquirer_root;
    private boolean isPosApk = false;
    private UrovoSecurityPermission mUrovoSecurityPermission;
    //add by jinpu.lin distinguish POS or PDA 2019.01.09
    private boolean isPosEquitment = false;
    public VerifySignatureManager() {
        //add by xjf read CA from SE.ro.perm.enforce
        mUrovoSecurityPermission = new UrovoSecurityPermission();
        //add by jinpu.lin 
        isPosEquitment = SystemProperties.get("persist.uvo.equipment", "PDA").equals("POS") ? true : false;
        if(false) {
            int acquirerCertlen = CertMaster.open();
            //byte[] acqCert = readFileByBytes("/etc/acquirer_root.cer");
            //if(acqCert != null && acqCert.length > 0) {
            //    int ret = CertMaster.writeCert(1, 1, 1, acqCert, acqCert.length);
            //    android.util.Log.i(TAG, "pos apk writeCert ret= " + ret);
            //}
            byte[] keyBuf = new byte[2048];
            acquirerCertlen = CertMaster.readCert(1, 1, 1, keyBuf);
            android.util.Log.i(TAG, "pos apk verfiy acquirerCertlen= " + acquirerCertlen);
            if(acquirerCertlen > 0) {
                acquirer_root = new byte[acquirerCertlen];
                System.arraycopy(keyBuf, 0, acquirer_root, 0, acquirerCertlen);
                try{
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    Certificate caCer = certFactory.generateCertificate(new ByteArrayInputStream(acquirer_root));
                    X509Certificate certificateWorkCert=(X509Certificate) caCer;
                    byte[] tbsCert = certificateWorkCert.getTBSCertificate();
                    MessageDigest mDigest = MessageDigest.getInstance("SHA256");
                    mDigest.update(tbsCert);
                    byte[] msg = mDigest.digest();
                    byte[] signData = certificateWorkCert.getSignature();
                    if(msg != null && signData != null) {
                        acquirerCertlen = CertMaster.verifyCertSign(msg, msg.length, signData, signData.length);
                        android.util.Log.i(TAG, "pos apk verfiy msg " + msg.length + " signData " + signData.length+ " verifyCertSign= " + acquirerCertlen);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            CertMaster.close();
        } else {
            try {
                if (isPosEquitment) {
                    acquirer_root = readFileByBytes("/system/etc/uAPPComRoot.crt");
                } else {
                    acquirer_root = readFileByBytes("/system/etc/uAPPYundaRoot.crt");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /*根据签发给客户的机构根证书验证，签发app的应用证书是否合法，证书扩展部分包含应用访问ic卡，picc printer pinpad等硬件权限
    *V2签名验证，是根据Android V2签名方式扩展了一个区域存放签名证书信息
    */
    public static final String OID_PERMISSION = "2.5.29.99";
    private boolean verifyAPKDoubleV2Sign(PackageParser.Package pkg) {
        if (pkg != null) {
            String filePath = pkg.baseCodePath;
            try {
                X509Certificate[][] isV2Cert = ApkSignatureSchemeV2VerifierEx.verify(filePath);
                if(isV2Cert != null && isV2Cert.length > 0){
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    Certificate caCer = certificateFactory.generateCertificate(new ByteArrayInputStream(acquirer_root));
                    PublicKey caKey = caCer.getPublicKey();
                    for(int i = 0; i < isV2Cert.length; i++) {
                        try {
                            isV2Cert[i][0].verify(caKey);
                            Log.i(TAG, "Signature V2: pass");
                            /*boolean checkPerm = true;SystemProperties.getBoolean("ro.perm.enforce", false);
                            if(checkPerm) {
                                try{
                                    byte[] perms = isV2Cert[i][0].getExtensionValue(OID_PERMISSION);
                                    if (perms != null && perms.length > 2) {
                                        int length = (int) perms[1];
                                        for (int k = 0; k < length; k++) {
                                            int indexPerm = (byte) (perms[k + 2] >> 3);
                                            if(indexPerm >= 0 && indexPerm < PosConstant.definitionPerms.length) {
                                                String permString = PosConstant.definitionPerms[indexPerm];
                                                if (pkg.requestedPermissions.indexOf(permString) == -1) {
                                                    pkg.requestedPermissions.add(permString.intern());
                                                    //pkg.requestedPermissionsRequired.add(Boolean.TRUE);
                                                }
                                            }
                                        }
                                    }
                                }catch(Exception e){
                                    //e.printStackTrace();
                                    Log.i(TAG, "parse Permissions V2: error");
                                }
                            }*/
                            return true;
                        } catch (Exception e) {
                            Log.e(TAG, "java.security.SignatureException V2: error");
                        }
                    }
                }
                Slog.e(TAG, "SignatureSchemeV2 faile, return false");
            } catch (Exception e) {
                //e.printStackTrace();
                 Log.e(TAG, "SignatureSchemeV2: error");
            }
            return false;
        }
        return false;
    }
    //META-INF 单证书 V2结构，POS通用app证书
    /**
    192.168.8.173:8082
    I9000 单证书，直接替换上传apk证书
    NEW_I9000 双证书，POS终端三级应用签名证书。在apk中添加urovo签名的证书
    NEW_V2_POS POS终端三级应用签名证书，V2方式签名apk
    YD 韵达定制的三级证书，V2方式签名apk

    http://192.168.8.111:8081/

    I9000 单证书，直接替换上传apk证书
    I9100 双证书，POS终端三级应用签名证书。在apk中添加urovo签名的证书
    NEW_V2_POS POS终端三级应用签名证书，V2方式签名apk
    BHW(博韩伟业部分终端)，HSY(合生元), YD(韵达) 定制的证书，单证书，直接替换上传apk证书
    */
    private boolean verifyAPKDoubleV2Sign(PackageParser.Package pkg, String crtFile) {
        if (pkg != null) {
            String filePath = pkg.baseCodePath;
            try {
                X509Certificate[][] isV2Cert = ApkSignatureSchemeV2VerifierEx.verify(filePath);
                if(isV2Cert != null && isV2Cert.length > 0){
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    //byte[] byteCert = Base64.decode(mCert, Base64.DEFAULT);
                    Certificate caCer = certificateFactory.generateCertificate(new FileInputStream(crtFile));
                    PublicKey caKey = caCer.getPublicKey();
                    for(int i = 0; i < isV2Cert.length; i++) {
                        try {
                            isV2Cert[i][0].verify(caKey);
                            Log.i(TAG, "Signature V2: pass");
                            /*boolean checkPerm = true;SystemProperties.getBoolean("ro.perm.enforce", false);
                            if(checkPerm) {
                                try{
                                    byte[] perms = isV2Cert[i][0].getExtensionValue(OID_PERMISSION);
                                    if (perms != null && perms.length > 2) {
                                        int length = (int) perms[1];
                                        for (int k = 0; k < length; k++) {
                                            int indexPerm = (byte) (perms[k + 2] >> 3);
                                            if(indexPerm >= 0 && indexPerm < PosConstant.definitionPerms.length) {
                                                String permString = PosConstant.definitionPerms[indexPerm];
                                                if (pkg.requestedPermissions.indexOf(permString) == -1) {
                                                    pkg.requestedPermissions.add(permString.intern());
                                                    //pkg.requestedPermissionsRequired.add(Boolean.TRUE);
                                                }
                                            }
                                        }
                                    }
                                }catch(Exception e){
                                    //e.printStackTrace();
                                    Log.i(TAG, "parse Permissions V2: error");
                                }
                            }*/
                            return true;
                        } catch (Exception e) {
                            Log.e(TAG, "java.security.SignatureException V2: error");
                        }
                    }
                }
                Slog.e(TAG, "SignatureSchemeV2 faile, return false");
            } catch (Exception e) {
                //e.printStackTrace();
                 Log.e(TAG, "SignatureSchemeV2: error");
            }
            return false;
        }
        return false;
    }
    //META-INF 双证书
    private boolean verifyAPKDoubleSign(PackageParser.Package pkg, String crtFile) {
        PackageInfo packageInfo = PackageParser.generatePackageInfo(
                pkg, null, PackageManager.GET_SIGNATURES, 0, 0,null, new PackageUserState());

        if (packageInfo != null) {
            android.content.pm.Signature[] signs = packageInfo.signatures;
            for(android.content.pm.Signature sign : signs){
                try{
                    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                    Certificate caCer = certificateFactory.generateCertificate(new FileInputStream(crtFile));
                    PublicKey caKey = caCer.getPublicKey();
                    X509Certificate cert =  (X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(sign.toByteArray()));
                    cert.verify(caKey);
                    Slog.e(TAG, "verifyAPKDoubleSign sucess, return ture");
                    try{
                        byte[] perms = cert.getExtensionValue(OID_PERMISSION);
                        if (perms != null && perms.length > 2) {
                            int length = (int) perms[1];
                            for (int i = 0; i < length; i++) {
                                int indexPerm = (byte) (perms[i + 2] >> 3);
                                if(indexPerm >= 0 && indexPerm < PosConstant.definitionPerms.length) {
                                    String permString = PosConstant.definitionPerms[indexPerm];
                                    if (pkg.requestedPermissions.indexOf(permString) == -1) {
                                        pkg.requestedPermissions.add(permString.intern());
                                        //pkg.requestedPermissionsRequired.add(Boolean.TRUE);
                                    }
                                }
                            }
                        }
                    }catch(Exception e){
                        //e.printStackTrace();
                        Log.i(TAG, "parse Permissions: error");
                    }
                    return true;
                }catch(Exception e){
                    //e.printStackTrace();
                    Log.i(TAG, "java.security.SignatureException: error");
                }
            }
            Slog.e(TAG, "verifyAPKDoubleSign faile, return false");
            return false;
        }
        return false;
    }

    public boolean VerifySignature(PackageParser.Package pkg) {
        //if(Build.PWV_CUSTOM_CUSTOM.equals("PCIAQ")) {
            isPosApk = false;
            if(!pkg.baseCodePath.startsWith("/system/")) {
                /*if(acquirer_root == null) {
                    Log.e(TAG, "pos device no load cert!");
                    return false;
                }*/
                //使用内置根证书验签
                if(acquirer_root != null && verifyAPKDoubleV2Sign(pkg)){
                    return true;
                }
                //银商验签
                if (Build.PWV_CUSTOM_CUSTOM.equals("UMSPOS")) {
                    try {
                        isPosApk = false;
                        AuthSign authSign = new AuthSign();
                        isPosApk = authSign.authSignApk(pkg.baseCodePath);
                        Log.i(TAG, "ums apk verify success!");
                    } catch (Exception e) {
                        Log.i(TAG, "ums apk verify failed! details:" + e.getMessage());
                    }
                } else if (Build.PWV_CUSTOM_CUSTOM.equals("SYB")) {
                    try {
                        isPosApk = false;
                        AllinPayZipFile allinFile = new AllinPayZipFile(pkg.baseCodePath);
                        isPosApk = allinFile.verifyAllinPaySign();
                        Log.i(TAG, "SYB apk verify success!");
                    } catch (Exception e) {
                        Log.i(TAG, "SYB apk verify failed! details:" + e.getMessage());
                    }
                } else if (Build.PWV_CUSTOM_CUSTOM.equals("UMFPAY")) {
                    try {
                        isPosApk = false;
                        AuthSignCommon authSign = new AuthSignCommon();
                        isPosApk = authSign.authSignApk(pkg.baseCodePath);
                        Log.i(TAG, "UMFPAY apk verify success!");
                    } catch (Exception e) {
                        Log.i(TAG, "isUMFPAYApk apk verify failed! details:" + e.getMessage());
                    }
                } else if(Build.PWV_CUSTOM_CUSTOM.equals("CCB")){
                    isPosApk = false;
                    try {
                        CCBApkFile ccbApkFile = new CCBApkFile(pkg.baseCodePath);
                        isPosApk = ccbApkFile.verifyCCBApkSign();
                        if(isPosApk && ccbApkFile.getPerms() != null && ccbApkFile.getPerms().size() > 0){
                            for(String perm : ccbApkFile.getPerms()){
                                Log.i(TAG, "add Perms:" + perm);
                                if (pkg.requestedPermissions.indexOf(perm) == -1) {
                                    pkg.requestedPermissions.add(perm.intern());
                                    //pkg.requestedPermissionsRequired.add(Boolean.TRUE);
                                }

                            }
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "CCB apk verify failed! details:" + e.getMessage());
                    }
                } else if(Build.PWV_CUSTOM_CUSTOM.equals("ICBC")){
                    isPosApk = false;
                    try {
                        ICBCApkFile icbcApkFile = new ICBCApkFile(pkg.baseCodePath);
                        isPosApk = icbcApkFile.verifyICBCApkSign();
                        if(isPosApk && icbcApkFile.getPerms() != null && icbcApkFile.getPerms().size() > 0){
                            for(String perm : icbcApkFile.getPerms()){
                                Log.i(TAG, "add Perms:" + perm);
                                if (pkg.requestedPermissions.indexOf(perm) == -1) {
                                    pkg.requestedPermissions.add(perm.intern());
                                    //pkg.requestedPermissionsRequired.add(Boolean.TRUE);
                                }

                            }
                        }
                    } catch (Exception e) {
                        Log.i(TAG, "ICBC apk verify failed! details:" + e.getMessage());
                    }
                } else if(Build.PWV_CUSTOM_CUSTOM.equals("WEPOY")) {
                    isPosApk = false;
                    isPosApk = verifyAPKDoubleSign(pkg, "/etc/phoenix.crt");
                }else if(Build.PWV_CUSTOM_CUSTOM.equals("ACS")) {
                    isPosApk = false;
                    isPosApk = verifyAPKDoubleSign(pkg, "/etc/uruvo_certificate.pem");
                }
                if(!isPosApk) {
                    try {
                        if(Build.PWV_CUSTOM_CUSTOM.equals("Reliance")) {
                            if(verifyAPKDoubleSign(pkg, "/etc/RelianceOrganRoot.crt"))
                                return true;
                        } else if(Build.PWV_CUSTOM_CUSTOM.equals("TOTAN")) {
                            if(verifyAPKDoubleSign(pkg, "/etc/TotanOrganRoot.crt"))
                                return true;
                        } else if(Build.PWV_CUSTOM_CUSTOM.equals("GHB")) {//泰国GHB银行定制
                            if(verifyAPKDoubleSign(pkg, "/etc/uAPPGHBRoot.crt"))
                                return true;
                        } else if(Build.PWV_CUSTOM_CUSTOM.equals("GANA")) {//哥伦比亚定制
                            if(verifyAPKDoubleSign(pkg, "/etc/uAPPColombiaRoot.crt"))
                                return true;
                        } else if(Build.PWV_CUSTOM_CUSTOM.equals("BAXI")) {//香港汇普达科技有限公司APP签名证书签发
                            if(verifyAPKDoubleSign(pkg, "/etc/uAPPBAXIRoot.crt"))
                                return true;
                        } else if(!isPosEquitment&&verifyAPKDoubleV2Sign(pkg, "/system/etc/uAPPYundaRoot.crt")) {
                            return true;
                        }else if(isPosEquitment&&verifyAPKDoubleV2Sign(pkg, "/system/etc/uAPPComRoot.crt")) {
                            return true;
                        }
                        //对比公钥证书
                        String signInfoPubKey = null;
                        String[] signInfoPubKeyArray = null;

                        PackageInfo packageInfo = PackageParser.generatePackageInfo(
                                pkg, null, PackageManager.GET_SIGNATURES, 0, 0,null, new PackageUserState());
                        if (packageInfo != null) {
                            String packageName = packageInfo.packageName;

                            android.content.pm.Signature[] signs = packageInfo.signatures;
                            if (signs != null) {
                                android.content.pm.Signature sign = signs[0];
                                if (sign != null) {
                                    signInfoPubKey = mUrovoSecurityPermission
                                            .parseSignature(sign.toByteArray());
                                } else {
                                    Slog.w(TAG, "Device Security- "
                                            + "sign info is null");
                                }
                            } else {
                                Slog.w(TAG, "Device Security- " + "packageInfo is null");
                            }
                            if (signInfoPubKey != null) {
                                isPosApk = mUrovoSecurityPermission.cmpSign(signInfoPubKey, packageName);
                            } else {
                                Slog.w(TAG, "Device Security- "
                                        + "get packageInfo faild : " + packageName);
                                return false;
                            }
                        } else {
                            Slog.w(TAG, "Device Security- " + "packageInfo is null");
                            return false;
                        }
                    } catch (Exception e) {
                        Log.i(TAG, " apk verify failed! details:" + e.getMessage());
                    }
                }
            } else {
                if(pkg.requestedPOSPermissions.size() > 0) {
                    for (int i = 0 ; i < pkg.requestedPOSPermissions.size(); i++) {
                        String perm = pkg.requestedPOSPermissions.get(i);
                        Log.i(TAG, "sys add Perms:" + perm);
                        if (pkg.requestedPermissions.indexOf(perm) == -1) {
                            pkg.requestedPermissions.add(perm.intern());
                        }
                    }
                }
                isPosApk = true;
            }
        //}
        return isPosApk;
    }

    private static byte[] readFileByBytes(String fileName) {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            in = new java.io.FileInputStream(fileName);
            byte[] buf = new byte[1024];
            int length = 0;
            while ((length = in.read(buf)) != -1) {
                out.write(buf, 0, length);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return out.toByteArray();
    }
}
