/**
 * Copyright (c) 2007, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os;
import android.os.IInputActionListener;
import android.os.Bundle;
import android.os.ISignatureActionListener;

/** {@hide} */
interface IMaxqEncryptService
{
     int open();
     int close();
     int getFirmwareVersion(out byte[] responseData, out byte[] ResLen);
     int getDeviceStatus(out byte[] responseData, out byte[] ResLen);
     int queryTrigger(out byte[] trigger);
     int queryTriggeredState(out byte[] trigger);
     int queryTriggeredInfo(out byte[] trigger);
     int clearTriggeredState();
     int powerOff(int Time, out byte[] ResponseData, out byte[] ResLen);
     int clearKey(out byte[] ResponseData, out byte[] ResLen);
     int clearKeyInner(out byte[] ResponseData, out byte[] ResLen);
     int loadKey(int KeyUsage, int KeyNo, int ParentKeyNo, in byte[] KeyData,
            int KeyDataLen, out byte[] ResponseData, out byte[] ResLen);
     int loadKeyDuk(in byte[] Bdk, int BdkLen, in byte[] Ksn, int KsnLen);
     int loadKeyEx(int KeyUsage, int KeyNo,
			int ParentKeyType, int ParentKeyNo,
			int Alg, in byte[] KeyData, int KeyDataLen,
			int CheckMode, in byte[] CheckData, int CheckDataLen,
			out byte[] ResponseData, out byte[] ResLen);
     int readKey(int KeyUsage, int KeyNo, out byte[] ResponseData, out byte[] ResLen);
     int deleteKey(int KeyUsage, int KeyNo, out byte[] ResponseData, out byte[] ResLen);
     int encryptData(int KeyUsage, int KeyNo, int Algorithm, in byte[] StartValue,
            int StartValueLen, int PadChar, in byte[] EncryptData, int EncryptDataLen,
            out byte[] ResponseData, out byte[] ResLen);
     int encryptDataDuk(int Algorithm, int Mode/* 0 - GetPin, 1 - GetMac */,
			in byte[] EncryptData, int EncryptDataLen,
			out byte[] EncryptedData, out byte[] EncryptedDataLen,
			out byte[] OutKsn, out byte[] OutKsnLen);
     int decryptData(int KeyUsage, int KeyNo, int Algorithm, in byte[] StartValue,
            int StartValueLen, int PadChar, in byte[] DecryptData, int DecryptDataLen,
            out byte[] ResponseData, out byte[] ResLen);
     int calculateMAC(int KeyUsage, int KeyNo, int MacAlgorithmType, int DesAlgorithmType, in byte[] EncryptData,
            int EncryptDataLen, out byte[] ResponseData, out byte[] ResLen);
     int encryptMagData(int KeyUsage, int TDKeyNo, in byte[] EncryptData,
            int EncryptDataLen, out byte[] ResponseData, out byte[] ResLen);
     int getPinBlock(int KeyUsage, int PINKeyNo,
            in byte[] CustomerData, int CustomerDataLen, String message, long timeOut);
     int generateRandomData(out byte[] ResponseData, out byte[] ResLen);

     void addInputActionListener(in IInputActionListener listener);
     void removeInputActionListener(in IInputActionListener listener);
     void endPinInputEvent(int event);
     void configSupportPinLen(String supportPinLength);
     int inputOnlinePin(in Bundle param, in IInputActionListener listener);
     
     int startSignature(in Bundle bundle, in ISignatureActionListener listener);
     int stopSignature();
     void hsmOpen(int logicalID);
     void hsmClose(int logicalID);

     int psamOpen(byte slot, byte CardType, byte Volt);
     int psamClose();
     int psamDetect();
     int IccSetBaudrate(int FI, int DI);
     int psamActivate(out byte[] pAtr);
     int psamApduTransmit(in byte[] apdu, int apduLen, out byte[] rsp, out byte[] sw);
     int psamDeactivate();
     int psamSetParam(int type, in byte[] params);
     int psamGetParam(int type, out byte[] params);
     int psamSetVoltageClass(int voltageClass);
     int psamGetVoltageClass();
     int psamGetResponseEnable(byte autoFlag);
     int psamSetBaudrate(int FI, int DI);
     int psamGetCardType();

     int pciWriteMKeyse(int isPtk,byte app_no, byte key_type, byte key_no, byte key_len, in byte[] key_data,
		in byte[] appname, in byte[] mac);
     int pciWriteSKeyse(int isPtk,byte key_type, byte key_no, byte key_len, in byte[] key_data, in byte[] key_crc,
		byte mode, byte mkey_no);
     int pciGetMac(byte mackey_n, int inlen, in byte[] indata, out byte[] macout, byte mode);
     int encryptDataTDK(int KeyNo, int Algorithm, in byte[] StartValue, int StartValueLen,
		int PadChar, in byte[] EncryptData, int EncryptDataLen, out byte[] ResponseData, out int[] ResLen);
     int readCert(int type, int index, int coding,out byte[] key);
     int writeCert(int type, int index, int coding,in byte[] key, int len);
     int verifyCertSign(in byte[] msgDigest, int msglen, in byte[] sign, int signlen);
     int readCA(int format, int type, int index,out byte[] responseData,out int[] resLen);
     int writeCA(int format, int type, int index,in  byte[] responseData, int resLen);
     int deleteCA(int format, int type, int index);
    boolean isTampered();

    byte[] generateRandom(int length);
    void generateKeyPair(String aliasPrivateKey, int algorithm, int keySize);

    boolean injectPublicKeyCertificate(String alias,String aliasPrivateKey, in byte[] bufCert, int dataFormat);

    boolean injectRootCertificate(int certType, String alias,
		    in byte[] bufCert, int dataFormat);

    byte[] getCertificate(int certType, String alias, int dataFormat);

    boolean deleteCertificate(int certType, String alias);

    String[] queryCertificates(int certType);

    boolean deleteKeyPair(String aliasPrivateKey);

    byte[] generateCSR(String aliasPrivateKey, String commName);

    byte[] encrypt(int algorithm, String aliasPrivateKey, in byte[] bufPlain);

    byte[] decrypt(int algorithm, String aliasPrivateKey,
		    in byte[] bufCipher);
    long getFreeSpace();

    boolean injectPrivateKey(in byte[] keyBuffer, int dataFormat);
    String getEncryptedUniqueCode(String uniqueCode, String randomFactor);


    int magOpen(int magType);
    int magClose(int fd);
    int magCheckDev(int fd);
    int magGetAllStripInfo(int fd,out byte[] info);
    int magGetSingleStripInfo(int fd, int strip,out byte[] info);
    int DukptTrackData(int mode, int iKeyType,in byte[] inTrack1,in byte[] inTrack2,in byte[] inTrack3,in int[] plainTextlen,
								out byte[] outTrack1,out byte[] outTrack2,out byte[] outTrack3,out byte[] encPan,out byte[] Ksn,out int[] cipherlen);

	int downloadKeyDukpt(int keyType,in byte[] Bdk, int BdkLen,in byte[] Ksn, int KsnLen,in byte[] bsIpek, int bsIpeklength);
    int downloadKeyOfPINBdk(in byte[] Bdk,int BdkLen,in byte[] Ksn,int KsnLen, in byte[] bsIpek,int bsIpeklength);
    int downloadKeyOfTDKBdk(in byte[] Bdk, int BdkLen,in byte[] Ksn, int KsnLen,in byte[] bsIpek, int bsIpeklength);
    int calculateMACOfDUKPT(in byte[] rawData, int rawDataLen,out byte[] outData,out int[] outDataLen,out byte[] outKsn,out int[] KsnLen);
    int calculateMACOfDUKPTExtend(int keySetNum,in byte[] rawData, int rawDataLen,out byte[] outData,out int[] outDataLen,out byte[] outKsn,out int[] KsnLen);
    int encryptWithPEK(int keyType,int keySetNum,in byte[] rawData, int rawDataLen,out byte[] outData,out int[] outDataLen,out byte[] outKsn,out int[] KsnLen);
    int DukptEncryptDataIV(int keyType, int keySetNum, int encMode, 
								in byte[] iv, int ivLen,in byte[] dataIn, int inLen, 
								out byte[] dataOut,out int[] outLen,out byte[] outKsn,out int[] KsnLen);
    int DukptPinblockBySP(int mode, int iKeyType,in byte[] cardNo,out byte[] pinBlock,out byte[] outKsn);
    boolean generateRSAKey();
    boolean getRSAPublicKeyModel(out byte[] publickey,out int[] publickeyLen,out int[] exponent);
    int loadDukptBlob(int keySlot,in byte[] blob,int blobLen);
    int DukptGetKsn(int keySetNum,out byte[] outKsn);
}

