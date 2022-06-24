package com.andpos;

import android.util.Log;

import com.andpos.PosConstant;
//import com.andpos.CertMaster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by josin on 17-1-13.
 * 参考《智能POS终端设备证书签名体系以及资源权限管理草稿V1.3.pdf》
 */
public class PosApkFile {
	public static final String TAG = "PosApkFile";
	public static final String CA_PCI = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3eVLkszd6663WyPY1zx3y+cC9eJixC5+DRH5+Q5NmFDi3Z4x0n7t07P1OKmCJ7oKkXatXT1R/O3pt03w5ePrAiTg+f1SajRUH2Oyzgnigr7H5bThH77S9F2V3uOd9xP9KPRIgPVT0iw3qdnnQUV/M6keuVPhgWMVgqAPKbGTp0Xy7hp4WHnaudffIqzjAYdxJ3jWvxykEQRD6zfWqIM4PHJOnzjjrT2QlbfJ3Q+WnfaET9A/U+ls1gXqu+ld4aZzxpDT42RTaa1cv42cqHpPQccQVXbd3L6ToKw8ccz1VnZv6EFJHBEf1IKl8d0DL41eLkrM00wbPNbTJ799MgTcuQIDAQAB";
    private static final String SGN_NAME = "META-INF/UBXAPKSIGN.SGN";
    private static final String RSA_NAME = "META-INF/CERT.RSA";
    public static final String OID_PERMISSION = "2.5.29.99";
    private String mPath;
    private boolean isPosApk = false;
    private byte[] permissions;

    public PosApkFile(String apkPath, byte[] acquirer_root){
        mPath = apkPath;
        isPosApk = verify(acquirer_root);
    }

    public boolean isPosApk(){
	return isPosApk;
    }

    public byte[] getPerms(){
	return permissions;
    }
    public static byte[] readFileByBytes(String fileName) {
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
    private boolean verify(byte[] acquirer_root){
        try {
            if(acquirer_root == null) {
                android.util.Log.i(TAG, "pos apk acquirer_root= null");
                return false;
            }
            /*byte[] acquirer_root;
            int acquirerCertlen = CertMaster.open();
            //byte[] acqCert = readFileByBytes("/etc/acquirer_root.der");
            //if(acqCert != null && acqCert.length > 0) {
            //    int ret = CertMaster.writeCert(1, 1, 1, acqCert, acqCert.length);
           //     android.util.Log.i(TAG, "pos apk writeCert ret= " + ret);
            //}
            byte[] keyBuf = new byte[2048];
            acquirerCertlen = CertMaster.readCert(1, 1, 1, keyBuf);
            CertMaster.close();
            android.util.Log.i(TAG, "pos apk verfiy acquirerCertlen= " + acquirerCertlen);
            if(acquirerCertlen > 0) {
                acquirer_root = new byte[acquirerCertlen];
                System.arraycopy(keyBuf, 0, acquirer_root, 0, acquirerCertlen);
            } else {
                return false;
            }*/
            JarFile jarfile = new JarFile(mPath);
            JarEntry je = jarfile.getJarEntry(SGN_NAME);
            if (je == null) {
                jarfile.close();
                return false;
            }
            InputStream is = jarfile.getInputStream(je);
            DerPart dpCert = new DerPart(is);
            ByteArrayInputStream bis = new ByteArrayInputStream(dpCert.allBody);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate appCert = (X509Certificate)certificateFactory.generateCertificate(bis);
            byte[] perms = appCert.getExtensionValue(OID_PERMISSION);
            permissions = parseOIDPerm(perms);
            bis.close();

            //KeyFactory keyf = KeyFactory.getInstance("RSA");
            //PublicKey caKey = keyf.generatePublic(new X509EncodedKeySpec(com.android.org.bouncycastle.util.encoders.Base64.decode(CA_PCI.getBytes())));
            //appCert.verify(caKey);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            Certificate caCer = certFactory.generateCertificate(new ByteArrayInputStream(acquirer_root));
            PublicKey caKey = caCer.getPublicKey();
            appCert.verify(caKey);
            byte[] magicNum = read(is, 0, 4);
            DerPart dpHeader = new DerPart(is);

				DerPart algorithm = dpHeader.mParts.get(2);
				DerPart rsaHash = dpHeader.mParts.get(3);
				DerPart vendorIndex = dpHeader.mParts.get(4);
					DerPart vendorOffset = vendorIndex.mParts.get(1);
					DerPart vendorLength = vendorIndex.mParts.get(2);

				DerPart signatureIndex = dpHeader.mParts.get(5);
					DerPart signatureOffset = signatureIndex.mParts.get(1);
					DerPart signatrueLength = signatureIndex.mParts.get(2);

			is.close();
			is = jarfile.getInputStream(je);
			byte[] vendorInfo = read(is, bytesToInt(vendorOffset.mainBody), bytesToInt(vendorLength.mainBody));
			parseHeaderPerm(vendorInfo);
			is.close();

			is = jarfile.getInputStream(je);
			byte[] signature = read(is, bytesToInt(signatureOffset.mainBody), bytesToInt(signatrueLength.mainBody));
            is.close();

            JarEntry jeRsa = jarfile.getJarEntry(RSA_NAME);
            InputStream rsaIs = jarfile.getInputStream(jeRsa);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(inputStream2byte(rsaIs));
            byte[] digest = md.digest();
            rsaIs.close();
            if(digest.length != rsaHash.mainBody.length){
		return false;
            } else {
		for(int i = 0; i < digest.length; i ++){
			if(digest[i] != rsaHash.mainBody[i]){
				return false;
			}
		}
            }

            java.security.Signature signa = java.security.Signature.getInstance("SHA256WithRSA");
            signa.initVerify(appCert.getPublicKey());
            signa.update(magicNum );
            signa.update(dpHeader.allBody);
			if(vendorInfo != null)
		signa.update(vendorInfo);
            return signa.verify(signature);

        } catch (IOException e){
		    e.printStackTrace();
        } catch (CertificateException e) {
		    e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
		    e.printStackTrace();
		} /*catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}*/ catch (SignatureException e) {
			e.printStackTrace();
		} catch (NoSuchProviderException e){
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}

        return false;
    }

	public static byte[] parseOIDPerm(byte[] perms) {
		byte[] oidPerms = null;
		if (perms != null && perms.length > 2) {
			int length = (int) perms[1];
			oidPerms = new byte[length];
			for (int i = 0; i < length; i++) {
				oidPerms[i] = (byte) (perms[i + 2] >> 3);
			}
		}
		return oidPerms;
	}

	public static String[] getStrPermsByOidPers(byte[] perms){
		if(perms != null && perms.length > 0) {
			String[] permissions = new String[perms.length];
			for(int i = 0; i < perms.length; i++){
				permissions[i] = PosConstant.OID_TO_PERMISSION.get(Integer.valueOf(perms[i]));
			}
			return permissions;
		}
		return null;
	}

    private void parseHeaderPerm(byte[] perms){
	if(permissions != null && permissions.length > 0) return;
	// TODO 解析权限信息
	//permissions = new String[];
    }

    private static byte[] inputStream2byte(InputStream is)
    {
        byte[] buffer = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = is.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e)  {
            e.printStackTrace();
        }
        return buffer;
    }

    private byte[] read(InputStream is, int offset, int length) throws IOException{
        byte[] buffer = new byte[(int)(length & 0xffff)];
        if(offset > 0)
            is.skip(offset);
        if(is.read(buffer) != -1){
            return buffer;
        }
        return null;

    }

    private static int bytesToInt(byte[] ary) {
        int value;
        value = (int) ((ary[3] & 0xFF)
                | ((ary[2]<<8) & 0xFF00)
                | ((ary[1]<<16) & 0xFF0000)
                | ((ary[0]<<24) & 0xFF000000));
        return value;
    }

    private class DerPart{
	private InputStream mIs;
	public byte type;
	public int length;
	public byte[] lengthBody;
	public byte[] mainBody;
	public byte[] allBody;
	public List<DerPart> mParts = new ArrayList<DerPart>();

	public DerPart(InputStream is){
		mIs = is;
		getDerNextPart();
	}

	private void getDerNextPart(){
		try {
				type = (byte)mIs.read();
				length = mIs.read();
		        if((length & 0x80) == 0x00){
				lengthBody = new byte[]{(byte) length};
		        } else {
				int lenBytes = length & 0x7f;
				byte[] src = read(mIs, 0, lenBytes);
				int result = 0;
				for(int i = 0; i < lenBytes; i++){
					result = (src[i] & 0xff) << ((lenBytes-i-1)*8) | result;
				}
				lengthBody = new byte[lenBytes + 1];
				lengthBody[0] = (byte)length;
				System.arraycopy(src, 0, lengthBody, 1, lenBytes);
				length = result;
		        }
		        mainBody = read(mIs, 0, length);
		        if(mainBody != null){
				allBody = new byte[1+lengthBody.length+mainBody.length];
		        } else {
				allBody = new byte[1+lengthBody.length];
		        }
		        allBody[0] = type;
		        System.arraycopy(lengthBody, 0, allBody, 1, lengthBody.length);

		        if(mainBody != null){
				System.arraycopy(mainBody, 0, allBody, 1+lengthBody.length, mainBody.length);
		        }
		        if(isSequenceType()){
				ByteArrayInputStream bais = new ByteArrayInputStream(mainBody);
				int i = 0;
				while(bais.available() > 0){
					i++;
					mParts.add(new DerPart(bais));
				}
				bais.close();
		        }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private boolean isSequenceType(){
		return type==0x30;
	}
    }
}
