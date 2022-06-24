package android.device;

import android.content.Context;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.IMaxqEncryptService;

import java.util.HashMap;
import java.util.Map;

/**
 * <br>民生CPOS机具系统 HwSecurityManager服务 API说明</br>
 * <br> NetworkManager的核心服务对象由“cpossystem”</br>
 * <br>服务提供者通过getHwSecurityManager函数返回</br>
 * <br>功能：用于提供机具中与Hardware安全模块相关的功能</br>
 * * <br>应用程序必须申明“android.permission.ACCESS_HWSECURITYMANAGER"
 * 权限才可以获取HwSecurityManager对象
 */

public class HwSecurityManager {

    /**
     * PEM格式
     */
    public static final int CERT_FORMAT_PEM = 0;
    /**
     * DER格式
     */
    public static final int CERT_FORMAT_DER = 1;
    /**
     * 终端所有者根证书
     */
    public static final int CERT_TYPE_TERMINAL_OWNER = 1;
    /**
     * 终端自己的公钥证书
     */
    public static final int CERT_TYPE_PUBLIC_KEY = 2;
    /**
     * 终端应用根证书。该证书用于验证所有应用签名的合法性。
     */
    public static final int CERT_TYPE_APP_ROOT = 3;
    /**
     * 终端SSL通讯根证书。该证书用于验证服务器的合法性。
     */
    public static final int CERT_TYPE_COMM_ROOT = 4;
    
    // 证书类型，对应JNI中的HSM_OBJECT_TYPE类型
    public static final int KEY_TYPE_PRIVATE_KEY = 0;
    public static final int KEY_TYPE_PUBLIC_KEY = KEY_TYPE_PRIVATE_KEY + 1;
    public static final int KEY_TYPE_CERT = KEY_TYPE_PUBLIC_KEY + 1;
    
    /**
     * RSA最大加密明文大小
     */
//  private static final int MAX_ENCRYPT_BLOCK = 117; // KEY_SIZE = 1024;
    private static final int MAX_ENCRYPT_BLOCK = 245; // KEY_SIZE = 2048;

    /**
     * RSA最大解密密文大小
     */
//  private static final int MAX_DECRYPT_BLOCK = 128; // KEY_SIZE = 1024;
    private static final int MAX_DECRYPT_BLOCK = 256; // KEY_SIZE = 2048;
    
    private static final int KEY_SIZE = 2048;

    private IMaxqEncryptService mIHwSecurityManager;

    public HwSecurityManager() {
        IBinder b = ServiceManager.getService("maxqservice");
        mIHwSecurityManager = IMaxqEncryptService.Stub.asInterface(b);
    }

    /*
     * 权限：PERMISSION_READONLY_OLD,PERMISSION_READONLY_OLD2,PERMISSION_READONLY
     */
    public int open(int logicalID) {
        if (mIHwSecurityManager == null)
            return -1;
        try {
           mIHwSecurityManager.hsmOpen(logicalID);
           return 0;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /*
     * 权限PERMISSION_READONLY_OLD,PERMISSION_READONLY_OLD2,PERMISSION_READONLY
     */
    public int close(int logicalID) {
        if (mIHwSecurityManager == null)
            return -1;
        try {
            mIHwSecurityManager.hsmClose(logicalID);
            return 0;
        } catch (Exception e) {
        }
        return -1;
    }

    /*
     * 判断系统安全模块是否被破坏
     * 权限PERMISSION_READONLY_OLD,PERMISSION_READONLY_OLD2,PERMISSION_READONLY
     * */
    public boolean isTampered() {
        if (mIHwSecurityManager == null)
            return false;
        try {
            return mIHwSecurityManager.isTampered();
        } catch (Exception e) {
        }
        return true;
    }

    public void generateKeyPair(String aliasPrivateKey, int algorithm, int keySize){
        if (mIHwSecurityManager == null) return;
        try {
            mIHwSecurityManager.generateKeyPair(aliasPrivateKey, algorithm, keySize);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * <br>导入根证书到安全芯片</br>
     * 权限：PERMISSION_ACCESS_OLD,PERMISSION_ACCESS
     *
     * @param certType 证书类型
     * @param alias 证书别名
     * @param bufCert 证书
     * @param dataFormat    数据格式 只能为PEM或DER;
     * @return true 成功 false 失败
     */
    public boolean injectRootCertificate(int certType, String alias,
             byte[] bufCert, int dataFormat) {
        if (mIHwSecurityManager == null)
            return false;
        try {
            return mIHwSecurityManager.injectRootCertificate(certType,  alias,
                    bufCert, dataFormat);
        } catch (Exception e) {
        }
        return true;
    }

    /**
     * <br>Inject the certificate of the existing key pair
     * 权限：PERMISSION_ACCESS_OLD,PERMISSION_ACCESS
     ** @param alias           the alias of the certificate
     * @param aliasPrivateKey the alias of the key pair, usually it's the private key's
     *                        alias
     * @param bufCert         证书
     * @param dataFormat         数据格式 只能为PEM或DER;
     * @return true 成功 false 失败
     */
    public boolean  injectPublicKeyCertificate(String alias,
            String aliasPrivateKey, byte[] bufCert, int dataFormat){
        if (mIHwSecurityManager == null)
            return false;
        try {
            return mIHwSecurityManager.injectPublicKeyCertificate(
                    alias, aliasPrivateKey,bufCert, dataFormat);
        } catch (Exception e) {
        }
        return true;
    }

    /**
     * <br>获取证书
     * 权限：PERMISSION_READONLY_OLD,PERMISSION_READONLY_OLD2,PERMISSION_READONLY
     *
     * @param certType 证书类型 OWNER(1),PUBLIC_KEY(2),APP_ROOT(3),COMMUNICATE(4)
     * @param alias    证书别名
     * @param format  数据格式 只能为PEM或DER;
     * @return byte[] 成功：返回证书内容，长度为byte数组长度 失败：返回null
     */
    public byte[] getCertificate(int certType, String alias,
                                 int format) {
        if (mIHwSecurityManager == null)
            return null;
        try {
            return mIHwSecurityManager.getCertificate(certType,
                    alias, format);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * <br>删除证书
     * 权限：PERMISSION_ACCESS_OLD,PERMISSION_ACCESS
     *
     * @param certType 证书类型 OWNER(1),PUBLIC_KEY(2),APP_ROOT(3),COMMUNICATE(4)
     * @param alias    证书别名
     * @return true 成功 false 失败
     */
    public boolean deleteCertificate(int certType, String alias) {
        if (mIHwSecurityManager == null)
            return false;
        try {
            return mIHwSecurityManager.deleteCertificate(certType,
                    alias);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * <br>删除私钥对
     * 权限：PERMISSION_ACCESS_OLD,PERMISSION_ACCESS
     *
     * @param aliasPrivateKey the alias of the private key
     * @return true 成功 false 失败
     */
    public boolean deleteKeyPair(String aliasPrivateKey) {
        if (mIHwSecurityManager == null)
            return false;
        try {
            return mIHwSecurityManager.deleteKeyPair(aliasPrivateKey);
        } catch (Exception e) {
        }
        return false;
    }
    public String[] queryCertificates(int certType) {
        if (mIHwSecurityManager == null)
            return null;
        try {
            return mIHwSecurityManager.queryCertificates(certType);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * <br>Generate the CSR for given private key
     * 权限：PERMISSION_ACCESS_OLD,PERMISSION_ACCESS
     *
     * @param aliasPrivateKey the alias of the private key
     * @param commonName      the DN of the commonName
     * @return byte[] 成功：返回CSR内容，长度为byte数组长度 失败：返回null
     */
    public byte[] generateCSR(String aliasPrivateKey, String commonName) {
        if (mIHwSecurityManager == null)
            return null;
        try {
            return mIHwSecurityManager.generateCSR(aliasPrivateKey, commonName);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * <br>RSA加密
     * 权限：PERMISSION_ACCESS_OLD,PERMISSION_ACCESS
     * @param algorithm
     * @param aliasPrivateKey the alias of the private key
     * @param bufPlain        the buffer of the plain data
     * @return byte[] 成功：返回加密结果，长度为byte数组长度 失败：返回null
     */
    public byte[] doRSAEncrypt(int algorithm,String aliasPrivateKey, byte[] bufPlain) {
        if (mIHwSecurityManager == null)
            return null;
        try {
            return mIHwSecurityManager.encrypt(algorithm, aliasPrivateKey, bufPlain);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * <br>RSA解密
     * 权限：PERMISSION_ACCESS_OLD,PERMISSION_ACCESS
     * @param algorithm
     * @param aliasPrivateKey the alias of the private key
     * @param bufCipher        the buffer of the crypt data
     * @return byte[] 成功：返回解密结果，长度为byte数组长度 失败：返回null
     */
    public byte[] doRSADecrypt(int algorithm, String aliasPrivateKey, byte[] bufCipher) {
        if (mIHwSecurityManager == null)
            return null;
        try {
            return mIHwSecurityManager.decrypt(algorithm, aliasPrivateKey, bufCipher);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * <br>获取随机数
     * 权限：PERMISSION_ACCESS_OLD,PERMISSION_ACCESS
     *
     * @param length 随机数的长度
     * @return byte[] 成功：返回获取数据 失败：返回null
     */
    public byte[] generateRandom(int length) {
        if (mIHwSecurityManager == null)
            return null;
        try {
            return mIHwSecurityManager.generateRandom(length);
        } catch (Exception e) {
        }
        return null;
    }
    public long getFreeSpace(){
        if (mIHwSecurityManager == null)
            return -1;
        try {
            return mIHwSecurityManager.getFreeSpace();
        } catch (Exception e) {
        }
        return -1;
    }

    public boolean injectPrivateKey(byte[] keyBuffer,int dataFormat){
        if (mIHwSecurityManager == null)
            return false;
        try {
            return mIHwSecurityManager.injectPrivateKey(keyBuffer, dataFormat);
        } catch (Exception e) {
        }
        return false;
    }
    
    public String getEncryptedUniqueCode(String uniqueCode, String randomFactor) {
        if (mIHwSecurityManager == null)
            return null;
        try {
            return mIHwSecurityManager.getEncryptedUniqueCode(uniqueCode, randomFactor);
        } catch (Exception e) {
        }
        return null;
    }

}
