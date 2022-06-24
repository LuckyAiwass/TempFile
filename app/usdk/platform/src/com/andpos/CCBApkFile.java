package com.andpos;

import android.util.Slog;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * Created by luolin on 17-09-07.
 */
public class CCBApkFile {

    private static final String TAG = "CCBApkFile";
    private static final boolean DEBUG = true;


    public static final String CCB_CA = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq/EETL96rrpLTnoX4Y4yVogsoH9DpsPhH0u2xqKCGFetZQLO+1FdObk+EN4ke3S3h/0WA+uLjjP+D46vkgxSx8dUn6hnt8EX3F2W63kp/UqD57x6x4dvn69K9EXTqczFKyDd01YKail3EqeGMuoC+5C7PJM8AoOb8DBOoR79HWxz20IwtulFDk/PXvAeuo+Vfjn/PPTMLtM7FSLj2oG4f0EkGHn4dh/w0bEmmGtJ0O74zmL01b+NhAgcYbnKcK7Z7lQt4DuFcKA9nP/Pnr3FvAdMRnRDjhYoXLBV8nl1P7kbUtXq461hEO5RfcWRgCJVX0DKhpCFaH+HNZwBSbZruQIDAQAB";
    private static String rootCertPath = "/etc/CCB_CA.crt";
    private static String[] rootCertPath_old = new String[]{ "/etc/CCB_CA_old.crt", "/etc/CCB_CA.crt"};
    private final String SGN_NAME = "META-INF/APKSIGNV1.SGN";
    private byte[] certBody;
    private byte[] headerSignature;
    private X509Certificate structureCert;
    private String apkPath;
    private PermsFile permsFile;

    public CCBApkFile(String apkPath) {
        this.apkPath = apkPath;
    }
    
    public HashSet<String> getPerms(){
    	if(permsFile != null)
    		return permsFile.getPermissions();
    	 
    	return null;
    }

    public boolean verifyCCBApkSign(){

        if(!verifyStructKey()){
            return false;
        }
        try {
            RandomAccessFile raf = new RandomAccessFile(apkPath, "r");
            long scanOffset = raf.length() - ZipFile.ENDHDR;
            if (scanOffset < 0) {
                throw new ZipException("File too short to be a zip file: " + raf.length());
            }

            // 目录结束标识
            final int ENDHEADERMAGIC = 0x06054b50;
            long endheardOffset = scanOffset = ApkUtils.searchIdentify(raf, ENDHEADERMAGIC, scanOffset, false);
            logI("0x06054b50 offset: " + endheardOffset);

            // 核心目录总数
            scanOffset += 4 + 2 + 2; // 核心目录结束标记长度+当前磁盘编号长度+核心目录开始位置的磁盘编号
            raf.seek(scanOffset);
            int centDirNums = ApkUtils.get16(raf);
            
            logI("central dir nums: " + centDirNums);
            // 核心目录结构总数
            scanOffset += 2;
            raf.seek(scanOffset);
            int centStruNums = ApkUtils.get16(raf);
            logI("central structure dir nums: " + centStruNums);
            // 核心目录大小
            scanOffset += 2;
            raf.seek(scanOffset);
            int centDirSize = (int)(ApkUtils.get32(raf) & 0xffffffff);
            logI("central dir size: " + centDirSize);
            // 核心目录位移
            scanOffset += 4;
            raf.seek(scanOffset);
            int centDirOffset = (int)(ApkUtils.get32(raf) & 0xffffffff);
            logI("central dir offset: " + centDirOffset);

            scanOffset = centDirOffset;
            raf.seek(scanOffset);

            scanOffset = endheardOffset;
            raf.seek(scanOffset);
            // 最后一个压缩的目录源数据
            final int DIRECTORYHEADERMAGIC = 0x02014b50;
            long drctHeaderOffset;
            int fileHeaderOffset, drctNameLen, drcextraFildLen, fileCommentLen;
			while (true) {
				drctHeaderOffset = scanOffset = ApkUtils.searchIdentify(raf, DIRECTORYHEADERMAGIC, scanOffset, true);
				scanOffset += 28; // 文件名长度
				raf.seek(scanOffset);
				drctNameLen = ApkUtils.get16(raf);
				scanOffset += 2; //扩展域长度
				raf.seek(scanOffset);
				drcextraFildLen = ApkUtils.get16(raf);
				scanOffset += 2; // 文件注释长度
				raf.seek(scanOffset);
				fileCommentLen = ApkUtils.get16(raf);
				scanOffset += 10; // 本地文件头的相对位移
				raf.seek(scanOffset);
				fileHeaderOffset = (int)(ApkUtils.get32(raf) & 0xffffffff);
				scanOffset += 4;
				raf.seek(scanOffset);
				byte[] drctName = new byte[drctNameLen];
				raf.readFully(drctName);
				if (!SGN_NAME.equals(new String(drctName))) {
					scanOffset = drctHeaderOffset - 1;
					if(scanOffset <= 0)
						return false;
//					throw new ZipException(
//							"APKSIGNV1.SGN not found in file header!");
				} else {
					break;
				}
			}
			
			final long sgnDirectoryOffset = drctHeaderOffset;
			final int sgnDirectoryLen = 46 + drctNameLen + drcextraFildLen + fileCommentLen;
			logI("sgnDirectoryOffset === " + drctHeaderOffset + "; sgnDirectoryLen === " + sgnDirectoryLen);
			
	        // 压缩的文件内容源数据
	        final int FILEHEADERMAGIC = 0x04034b50;
	        scanOffset = fileHeaderOffset;
	        raf.seek(scanOffset);
	        if ((int)(ApkUtils.get32(raf) & 0xffffffff) != FILEHEADERMAGIC){
	        	throw new ZipException("Apk data error!");
	        }
	        scanOffset += 6; //通用比特标志位
	        raf.seek(scanOffset);
	        boolean hasDataDescriptor = ((raf.readShort() & 0x0800) == 0x0800);
	        scanOffset += 12; // 压缩后的大小
	        raf.seek(scanOffset);
	        int compressedSize = (int)(ApkUtils.get32(raf) & 0xffffffff);
	        scanOffset += 8; // 文件名长度
	        raf.seek(scanOffset);
	        int fileNameLen = ApkUtils.get16(raf);
	        scanOffset += 2; // 扩展区长度
	        raf.seek(scanOffset);
	        int extraFieldLen = ApkUtils.get16(raf);
	        scanOffset += 2;
	        raf.seek(scanOffset);
	        byte[] fileName = new byte[fileNameLen];
	        raf.readFully(fileName);
	        if(!SGN_NAME.equals(new String(fileName))){
	        	throw new ZipException("APKSIGNV1.SGN not found in file header!");
	        }
	        
	        final long sgnFileOffset = fileHeaderOffset;
	        long sgnFileLen = 30 + fileNameLen + extraFieldLen + compressedSize;
	        sgnFileLen = hasDataDescriptor ? sgnFileLen + 12 : sgnFileLen;
	        logI("sgnFileOffset === " + sgnFileOffset + "; sgnFileLen === " + sgnFileLen);

            java.security.Signature signa = java.security.Signature.getInstance("SHA256WithRSA");
            signa.initVerify(structureCert.getPublicKey());
            scanOffset = 0;
            raf.seek(scanOffset);
            byte[] apkSrcBuff = new byte[1024];
            int buffLen = apkSrcBuff.length;

            /** 
             *  |---------------|--1024--|=====sgnFileLen=====|--------------------|--1024--|=====sgnDirectoryLen=====|----|-1024-|-----------------------|
             *  0                            sgnFileOffset                                                        sgnDirectoryOffset                                      endheardOffset
             */
            while (scanOffset < raf.length()) {
                if(scanOffset  + apkSrcBuff.length <= sgnFileOffset){
                    buffLen = apkSrcBuff.length;
                    raf.readFully(apkSrcBuff);
                    scanOffset += 1024;
                } else if(scanOffset <= sgnFileOffset && scanOffset + apkSrcBuff.length > sgnFileOffset){
                	buffLen = (int)(sgnFileOffset - scanOffset);
                	raf.readFully(apkSrcBuff, 0, buffLen);
                	scanOffset = sgnFileOffset + sgnFileLen;
                } else if(scanOffset >= sgnFileOffset + sgnFileLen && scanOffset + apkSrcBuff.length <= sgnDirectoryOffset){
                	buffLen = apkSrcBuff.length;
                    raf.readFully(apkSrcBuff);
                    scanOffset += 1024;
                } else if(scanOffset < sgnDirectoryOffset && scanOffset + apkSrcBuff.length > sgnDirectoryOffset){
                	buffLen = (int)(sgnDirectoryOffset - scanOffset);
                	raf.readFully(apkSrcBuff, 0, buffLen);
                	scanOffset = sgnDirectoryOffset + sgnDirectoryLen;
                } else if(scanOffset >= sgnDirectoryOffset + sgnDirectoryLen && scanOffset + apkSrcBuff.length <= endheardOffset){
                	buffLen = apkSrcBuff.length;
                    raf.readFully(apkSrcBuff);
                    scanOffset += 1024;
                } else if(scanOffset < endheardOffset && scanOffset + apkSrcBuff.length > endheardOffset){
                	buffLen = (int)(endheardOffset - scanOffset);
                	raf.readFully(apkSrcBuff, 0, buffLen);
                	scanOffset = endheardOffset;
                } else if(scanOffset == endheardOffset){
                    buffLen = (int) (raf.length() - scanOffset > apkSrcBuff.length? apkSrcBuff.length : raf.length() -scanOffset);
                    raf.readFully(apkSrcBuff, 0, buffLen);
                    byte[] dirNums = ApkUtils.shortToBytesReverse((short)(centDirNums-1));
                    byte[] dirStruNums = ApkUtils.shortToBytesReverse((short)(centStruNums - 1));
                    byte[] dirSize = ApkUtils.intToBytesReverse(centDirSize - sgnDirectoryLen);
                    byte[] dirOffset = ApkUtils.intToBytesReverse((int)(centDirOffset - sgnFileLen));

                    System.arraycopy(dirNums, 0, apkSrcBuff, 8, dirNums.length);
                    System.arraycopy(dirStruNums, 0, apkSrcBuff, 10, dirStruNums.length);
                    System.arraycopy(dirSize, 0, apkSrcBuff, 12, dirSize.length);
                    System.arraycopy(dirOffset, 0, apkSrcBuff, 16, dirOffset.length);
                    scanOffset += buffLen;
                }
                signa.update(apkSrcBuff, 0, buffLen);
                raf.seek(scanOffset);
            }
            raf.close();
            if(certBody != null && certBody.length > 0){
                signa.update(certBody, 0, certBody.length);
            }
            boolean result = signa.verify(headerSignature);
            logI("Signature verify result : "+result);
            return result;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch (InvalidKeyException e){
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (Exception e){
            logE("Verify apk signature error!");
            e.printStackTrace();
        }
        return false;

    }

    private boolean verifyStructKey(){
        try {
            JarFile jarfile = new JarFile(apkPath);
            JarEntry je = jarfile.getJarEntry(SGN_NAME);
            if (je == null) {
                jarfile.close();
                return false;
            }
            InputStream is = jarfile.getInputStream(je);
            ApkUtils.DerFile derFile = new ApkUtils.DerFile(is);
            ApkUtils.DerObject objName = derFile.getNextDerObject();
            if(!"ACQUIRER-SGN-INFO".equals(new String(objName.mainBody))){
            	logI("string ACQUIRER-SGN-INFO can not found in SGN.");
            	return false;
            }
            ApkUtils.DerObject objHeader = derFile.getNextDerObject();
            if(objHeader.sonObjs.size() >= 3){
            	ApkUtils.DerObject objInfo = objHeader.sonObjs.get(0);
            	logI("objInfo.length ================ " + objInfo.sonObjs.size());
            		ApkUtils.DerObject exInfo = objInfo.sonObjs.get(5);
            			ApkUtils.DerObject permInfo = exInfo.sonObjs.get(0).sonObjs.get(0).sonObjs.get(1);
            			permsFile = new PermsFile(permInfo.mainBody);
            	ApkUtils.DerObject objSign = objHeader.sonObjs.get(1);
            	ApkUtils.DerObject objCert = objHeader.sonObjs.get(2);
            	
            	certBody = objInfo.mainBody;
            	headerSignature = objSign.mainBody;
            	
            	CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
                ByteArrayInputStream bis = new ByteArrayInputStream(objCert.mainBody, 1, objCert.mainBody.length-1);
                structureCert = (X509Certificate)certificateFactory.generateCertificate(bis);
                bis.close();

                structureCert.checkValidity();

                //PublicKey caKey = ApkUtils.loadPublicKey(CCB_CA);
                 // 获取根证书
                Certificate caCer = certificateFactory.generateCertificate(new FileInputStream(rootCertPath));
                PublicKey caKey = caCer.getPublicKey();

                structureCert.verify(caKey);
                return true;
                /*for(int i = 0; i < rootCertPath_old.length; i++) {
                    try{
                        Certificate caCer = certificateFactory.generateCertificate(new FileInputStream(rootCertPath_old[i]));
                        PublicKey caKey = caCer.getPublicKey();
                        
                        structureCert.verify(caKey);
                        return true;
                    } catch (Exception e){
                        logE("Verify structure certificate error!");
                    }
                }*/
            } 
           
        } catch (Exception e){
            logE("Verify structure certificate error!");
            e.printStackTrace();
        }

        return false;
    }
    
    private void logI(String msg){
        if(DEBUG)
            Slog.i(TAG, msg);
    }

    private void logE(String msg){
        Slog.e(TAG, msg);
    }

   private static class PermsFile{
	   private final HashSet<String> permissions;
	   
	   public PermsFile(byte[] perms){
		   String permStr = new String(perms);
		   Pattern pattern = Pattern.compile("\\[Uses\\-permission\\-\\d+\\][\r\n]*Name\\=([\\w\\.]*)[\r\n]*");
		   Matcher matcher = pattern.matcher(permStr);
		   permissions = new HashSet<String>();
		   while(matcher.find()){
			   if(matcher.group().length() >1)
				   permissions.add(matcher.group(1));
		   }
	   }
	   
	   public HashSet<String> getPermissions(){
		   return permissions;
	   }
   }
}
