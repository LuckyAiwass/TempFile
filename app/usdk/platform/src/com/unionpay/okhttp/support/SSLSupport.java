package com.udroid.uvotest.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import android.device.HwSecurityManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLSupport {

    public static TrustManagerFactory getTrustManagerFactory(String algorithm) {
        try {
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(algorithm);
            KeyStore trustKeyStore = KeyStore.getInstance("");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(new FileInputStream(""));
            final Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                //Log.i(TAG, "ca=" + ((X509Certificate) ca).getSubjectDN());
                //Log.i(TAG, "key=" + ((X509Certificate) ca).getPublicKey());
            } finally {
                caInput.close();
            }
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("trust01", ca);

//如下部分读取银联云POS已经注入的证书,各厂商自行实现并保证安全
//此处省略................
            tmf.init((KeyStore) trustKeyStore);
            return tmf;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
// TODO Auto-generated catch block
// TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static KeyManagerFactory getkeyManagerFactory(String algorithm) {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            KeyStore keys = KeyStore.getInstance("");
//如下部分读取银联云POS已经注入的证书和私钥,各厂商自行实现并保证安全
//此处省略................
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new BufferedInputStream(new FileInputStream("unionpay/trust01.crt"));
            final Certificate ca;
            try {
                ca = cf.generateCertificate(caInput);
                //Log.i(TAG, "ca=" + ((X509Certificate) ca).getSubjectDN());
                //Log.i(TAG, "key=" + ((X509Certificate) ca).getPublicKey());
            } finally {
                caInput.close();
            }
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("trust01", ca);
            HwSecurityManager pkcs = new HwSecurityManager();
            byte[] privatekey = pkcs.getCertificate(0,"pk2048",HwSecurityManager.CERT_FORMAT_PEM);
            keyStore.setKeyEntry("pk2048",privatekey,null);

            kmf.init(keys, "".toCharArray());
            return kmf;
        } catch (NoSuchAlgorithmException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        } catch (KeyStoreException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

