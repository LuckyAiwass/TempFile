package com.unionpay.smartpos.ssl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.security.KeyFactory;
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
import java.io.ByteArrayOutputStream;

import android.security.KeyStoreParameter;
import android.util.Log;

//import com.android.org.bouncycastle.util.encoders.Base64;
import android.util.Base64;
import java.io.IOException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Copyright (C) 2019, Urovo Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @Author: rocky
 * @Date: 20-7-7下午3:33
 */
public class SSLSupport {
    public final static String TAG = "SSLSupport";
    public static TrustManagerFactory getTrustManagerFactory(String algorithm) {
        try {
            TrustManagerFactory tmf =
                    TrustManagerFactory.getInstance(algorithm);
            KeyStore trustKeyStore = KeyStore.getInstance("BKS");
            HwSecurityManager pkcs = new HwSecurityManager();
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            //InputStream caInput = new BufferedInputStream(new FileInputStream("/etc/trust01.crt"));
            byte[] trust = pkcs.getCertificate(0,"trust01",HwSecurityManager.CERT_FORMAT_PEM);
            ByteArrayInputStream bais = new ByteArrayInputStream(trust);
            final Certificate ca;
            try {
                //ca = cf.generateCertificate(caInput);
                ca = cf.generateCertificate(bais);
                Log.i(TAG, "ca=" + ((X509Certificate) ca).getSubjectDN());
                Log.i(TAG, "key=" + ((X509Certificate) ca).getPublicKey());
            } finally {
                //caInput.close();
                bais.close();
            }
            //String keyStoreType = KeyStore.getDefaultType();
            //KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            trustKeyStore.load(null, null);
            trustKeyStore.setCertificateEntry("trust01", ca);

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
            KeyStore keyStore = KeyStore.getInstance("BKS");

            HwSecurityManager pkcs = new HwSecurityManager();
            // String keyStoreType = KeyStore.getDefaultType();
            //KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
           // keyStore.setCertificateEntry("trust01", ca);
            byte[] publickey = pkcs.getCertificate(0,"client2048",HwSecurityManager.CERT_FORMAT_PEM);//readFileByBytes("/etc/client2048");
            Log.d(TAG,"publickey = "+new String(publickey));
            InputStream ceris = new ByteArrayInputStream(publickey);
            CertificateFactory myCertificateFactory;
            myCertificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cer = (X509Certificate)myCertificateFactory.generateCertificate(ceris);
            ceris.close();
            keyStore.setCertificateEntry("client2048", cer);
            Certificate[] chain = new Certificate[1];
            chain[0] = cer;
            byte[] privatekey =pkcs.getCertificate(0,"pk2048",HwSecurityManager.CERT_FORMAT_PEM);//readFileByBytes("/etc/pk2048");
            Log.d(TAG,"privatekey = "+new String(privatekey));
            /*String keyString = new String(privatekey);
            keyString = keyString.replaceAll("\n", "");
            Pattern pattern =Pattern.compile("-----BEGIN RSA PRIVATE KEY-----(.*?)-----END RSA PRIVATE KEY-----");
            Matcher matcher=pattern.matcher(keyString);
            while (matcher.find()) {
                privatekey = matcher.group(1).getBytes();
            }
            Log.d("lynn","privatekey 64 = "+bytesToHexString(privatekey));*/
            /*Certificate[] chain = new Certificate[1];
            chain[0] = keyStore.getCertificate("client2048");
                Certificate ca = keyStore.getCertificate("client2048");
                Log.i("lynn", "ca=" + ((X509Certificate) ca).getSubjectDN());
                Log.i("lynn", "key=" + ((X509Certificate) ca).getPublicKey());*/

            String keyString = new String(privatekey);
            keyString = keyString.replaceAll("\n", "");
            Pattern pattern =Pattern.compile("-----BEGIN RSA PRIVATE KEY-----(.*?)-----END RSA PRIVATE KEY-----");
            Matcher matcher=pattern.matcher(keyString);
            while (matcher.find()) {
                privatekey = matcher.group(1).getBytes();
            }
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decode(privatekey,Base64.DEFAULT));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA","BC");
            PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
            KeyStore.PrivateKeyEntry privateKeyEntry = new KeyStore.PrivateKeyEntry(privateK,chain);
            keyStore.setEntry("pk2048", privateKeyEntry, null);
            kmf.init(keyStore, "".toCharArray());
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
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    
    private static byte[] readFileByBytes(String fileName) {
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            in =  new BufferedInputStream(new FileInputStream(fileName));
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

