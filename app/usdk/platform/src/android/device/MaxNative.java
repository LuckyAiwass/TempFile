/*
 * Copyright (C) 2015, The Urovo. Ltd, co
 * {@hide}
 */

package android.device;
/**
 *{@hide}
 */
public class MaxNative {
    static{
        System.loadLibrary("se_chip_jni");
    }
    /**
     * open: open Max3250. <br/>
     * 
     * @return 0: success; 1: faild
     */
    public static native int open();

    /**
     * close:close Max3250. <br/>
     * 
     * @return 0: success; 1: faild
     */
    public static native int close();

    /**
     * queryTrigger: Figure out the trigger. <br/>
     * 
     * @param CardSlot input data, which trigger to query <br/>
     * @param trigger output data, 6B, if it triggered out it will be 1, otherwise 0 <br/>
     * @return return errorCode <br/>
     */
    public static native int queryTrigger(int CardSlot, byte[] trigger);

    /**
     * queryTriggeredState: Figure out the triggered history. <br/>
     * 
     * @param CardSlot input data, which trigger to query <br/>
     * @param trigger output data, 6B, if it triggered out it will be 1, otherwise 0 <br/>
     * @return return errorCode <br/>
     */
    public static native int queryTriggeredState(int CardSlot, byte[] trigger);

    /**
     * queryTriggeredInfo: Figure out the triggered history. <br/>
     * struct tri_history{
     *   unsigned int timestamp;
     *   unsigned int info; // triggered info
     *   unsigned int back[6];
     * };
     * @param trigger output data: 5*sizeof(struct tri_history)<br/>
     * @return return len if success, else negative value <br/>
     */
    public static native int queryTriggeredInfo(byte[] trigger);

    /**
     * clearTriggeredState: Figure out the triggered history. <br/>
     * 
     * @param CardSlot input data, which trigger to query <br/>
     * @return return errorCode <br/>
     */
    public static native int clearTriggeredState(int CardSlot);

    /**
     * getFirmwareVersion:get max3250 software version. <br/>
     * 
     * @param ResponseData output data, max3250 response data <br/>
     * @param ResLen output data, max3250 response data length <br/>
     * @return return errorCode <br/>
     */
    public static native int getFirmwareVersion(byte[] ResponseData, byte[] ResLen);

    /**
     * getDeviceStatus:get max3250 hardware status <br/>
     * 
     * @param ResponseData output data, max3250 response data <br/>
     * @param ResLen output data, max3250 response data length <br/>
     * @return return errorCode <br/>
     */
    public static native int getDeviceStatus(byte[] ResponseData, byte[] ResLen);

    /**
     * powerOff: Close max3250 device. <br/>
     * 
     * @param Time input data, Time (second) the machine will power off <br/>
     * @param ResponseData output data, max3250 response data, this function
     *            return data is NULL. <br/>
     * @param ResLen output data, max3250 response data length, this function
     *            return data length is 0. <br/>
     * @return return errorCode <br/>
     */
    public static native int powerOff(int Time, byte[] ResponseData, byte[] ResLen);

    /**
     * clearKey:Clear All Keys. <br/>
     * 
     * @param ResponseData output data, max3250 response data, this function
     *            return data is NULL. <br/>
     * @param ResLen output data, max3250 response data length, this function
     *            return data length is 0. <br/>
     * @return return errorCode <br/>
     */
    public static native int clearKey(byte[] ResponseData, byte[] ResLen);

    /**
     * [clearKeyInner Dont Export it out!!! Only For Inner Test!!!]
     */
    public static native int clearKeyInner(byte[] ResponseData, byte[] ResLen);

    /**
     * loadKey:Download Keys in max3250 RAM. Keys length is 8 bytes (DES) or 16
     * bytes (3DES) <br/>
     * Notice: Parent keys or encryption keys unsupported 8 bytes length, that
     * must be 16 bytes. <br/>
     * 
     * @param KeyUsage input data, Key Usage
     * @param KeyNo input data, Key number
     * @param ParentKeyNo input data, Parent Key Number. when LoadKey is used to
     *            load parent key, this parameter can be ignore. Parent Key have
     *            not parent key. <br/>
     * @param KeyData input data, key data, this parameter have 8 bytes or 16
     *            bytes (3DES) <br/>
     * @param KeyDataLen input data, Key Data length <br/>
     * @param ResponseData output data, max3250 response data, output KCV (with
     *            clear key encryption 8 0x00 return value, get first 4 bytes) <br/>
     * @param ResLen output data, max3250 response data length <br/>
     * @return return errorCode <br/>
     */
    public static native int loadKey(int KeyUsage, int KeyNo, int ParentKeyNo, byte[] KeyData,
            int KeyDataLen, byte[] ResponseData, byte[] ResLen);

	public static native int loadKeyDuk(byte[] Bdk, int BdkLen,
        	byte[] Ksn, int KsnLen);
			
	public static native int loadKeyEx(
			int KeyUsage, int KeyNo,
			int ParentKeyType, int ParentKeyNo,
			int Alg, byte[] KeyData, int KeyDataLen,
			int CheckMode, byte[] CheckData, int CheckDataLen,
			byte[] ResponseData, byte[] ResLen);
    
    /**
     * readKey:read Keys. <br/>
     * 
     * @param KeyUsage input data, Key Usage
     * @param KeyNo input data, Key number
     * @param ResponseData output data, max3250 response keys data <br/>
     * @param ResLen output data, max3250 response keys data length <br/>
     * @return return errorCode <br/>
     */
    public static native int readKey(int KeyUsage, int KeyNo, byte[] ResponseData, byte[] ResLen);

    /**
     * deleteKey:Delete Keys. <br/>
     * 
     * @param KeyUsage input data, Key Usage
     * @param KeyNo input data, Key number
     * @param ResponseData output data, max3250 response data, this function
     *            return data is NULL. <br/>
     * @param ResLen output data, max3250 response data length, this function
     *            return data length is 0. <br/>
     * @return return errorCode <br/>
     */
    public static native int deleteKey(int KeyUsage, int KeyNo, byte[] ResponseData, byte[] ResLen);

    /**
     * encryptData: Encryption Data. <br/>
     * 
     * @param KeyUsage input data, Key Usage
     * @param KeyNo input data, Key number
     * @param Algorithm input data, Algorithm define: 0x01(ECB) 0x02(CBC).<br/>
     * @param StartValue input data, initialization vector, this value is
     *            NULL(Algorithm = 0x01) or 8 bytes (Algorithm = 0x02).<br/>
     * @param StartValueLen input data, initialization vector length.<br/>
     * @param PadChar input data, fill character, value is 0x0 ~ 0xF.<br/>
     * @param EncryptData input data, encryption data <br/>
     * @param EncryptDataLen input data, encryption data length <br/>
     * @param ResponseData output data, max3250 response data <br/>
     * @param ResLen output data, max3250 response data length <br/>
     * @return return errorCode <br/>
     */
    public static native int encryptData(int KeyUsage, int KeyNo, int Algorithm, byte[] StartValue,
            int StartValueLen, int PadChar, byte[] EncryptData, int EncryptDataLen,
            byte[] ResponseData, byte[] ResLen);
			
	public static native int encryptDataDuk(int Algorithm, int Mode/* 0 - GetPin, 1 - GetMac */,
			byte[] EncryptData, int EncryptDataLen,
			byte[] EncryptedData, byte[] EncryptedDataLen,
			byte[] OutKsn, byte[] OutKsnLen);

    /**
     * decryptData: Decryption Data<br/>
     * 
     * @param KeyUsage input data, Key Usage
     * @param KeyNo input data, Key number
     * @param Algorithm input data, Algorithm define: 0x01(ECB) 0x02(CBC).<br/>
     * @param StartValue input data, initialization vector, this value is
     *            NULL(Algorithm = 0x01) or 8 bytes (Algorithm = 0x02).<br/>
     * @param StartValueLen input data, initialization vector length.<br/>
     * @param PadChar input data, fill character, value is 0x0 ~ 0xF.<br/>
     * @param DecryptData input data, dencryption data.<br/>
     * @param DecryptDataLen input data, dencryption data length.<br/>
     * @param ResponseData output data, max3250 response data <br/>
     * @param ResLen output data, max3250 response data length <br/>
     * @return return errorCode <br/>
     */
    public static native int decryptData(int KeyUsage, int KeyNo, int Algorithm, byte[] StartValue,
            int StartValueLen, int PadChar, byte[] DecryptData, int DecryptDataLen,
            byte[] ResponseData, byte[] ResLen);

    /**
     * calculateMAC:MAC Algorithm calculate MAC value. <br/>
     * 
     * @param KeyUsage input data, Key Usage
     * @param KeyNo input data, Key number
     * @param EncryptData input data, mac encryption data. <br/>
     * @param EncryptDataLen input data, mac encryption data length.<br/>
     * @param ResponseData output data, max3250 response data <br/>
     * @param ResLen output data, max3250 response data length <br/>
     * @return return errorCode <br/>
     */
    public static native int calculateMAC(int KeyUsage, int KeyNo,
    		byte[] EncryptData, int EncryptDataLen, byte[] ResponseData, byte[] ResLen);
    public static native int calculateMAC(int KeyUsage, int KeyNo,
    		int MacAlgorithmType, int DesAlgorithmType,
    		byte[] EncryptData, int EncryptDataLen, byte[] ResponseData, byte[] ResLen);

    /**
     * encryptMagData:encryption Mag data. <br/>
     * 
     * @param KeyUsage input data, Key Usage
     * @param TDKeyNo input data, TD Key number
     * @param EncryptData input data, encryption data.<br/>
     * @param EncryptDataLen input data, encryption data length.<br/>
     * @param ResponseData output data, max3250 response data <br/>
     * @param ResLen output data, max3250 response data length <br/>
     * @return return errorCode <br/>
     */
    public static native int encryptMagData(int KeyUsage, int TDKeyNo, byte[] EncryptData,
            int EncryptDataLen, byte[] ResponseData, byte[] ResLen);

    public static native int getPinBlock(int KeyUsage, int PINKeyNo, int PadChar,
            byte[] CustomerData, int CustomerDataLen, byte[] ResponseData, byte[] ResLen);

    public static native int generateRandomData(byte[] ResponseData, byte[] ResLen);
    
    public static native int setLed(int id, int onoff, byte[] ResponseData, byte[] ResLen);
    public static native int setRTC(int id, int onoff, byte[] ResponseData, byte[] ResLen);
    public static native int getRTC(byte[] ResponseData, byte[] ResLen);
    public static native int enableSuspend(int enable, byte[] ResponseData, byte[] ResLen);
    public static native int setSuspendTimeout(int seconds, byte[] ResponseData, byte[] ResLen);
    public static native int setBeeper(int cnts, int freq, int time, byte[] ResponseData, byte[] ResLen);
    public static native int selectPsam2PowerSource(int cit);
    public static native int picQueryTriggerSec(byte[] status);

    public static native int pciClearTriggeredStateSec();
    public static native int pciEncryptForAppDat(byte[] a, int i, byte[] b);
    public static native int pciDecryptForAppDat(byte[] a, int i, byte[] b);

// if isPtk == 1, select Lib_PciWrite_MKeyse；if isPtk == 0, select Lib_PciWrite_MKeyseNotPtk.
    public static native int pciWriteMKeyse(int isPtk,byte app_no, byte key_type, byte key_no, byte key_len, byte[] key_data,
		byte[] appname, byte[] mac);
	
	public static native int pciWriteSKeyse(int isPtk,byte key_type, byte key_no, byte key_len, byte[] key_data, byte[] key_crc,
		byte mode, byte mkey_no);

	public static native int pciGetMac(byte mackey_n, int inlen, byte[] indata, byte[] macout, byte mode);

	public static native int encryptDataTDK(int KeyNo, int Algorithm, byte[] StartValue, int StartValueLen,
		int PadChar, byte[] EncryptData, int EncryptDataLen, byte[] ResponseData, int[] ResLen);

    public static native int readCert(int type, int index, int coding,byte[] key);
    public static native int writeCert(int type, int index, int coding,byte[] key, int len);
    public static native int verifyCertSign(byte[] msgDigest, int msglen, byte[] sign, int signlen);

	public static native int pciSecLogicJudgAdminA(byte[] data_in, int len_in, byte[] data_out, int[] len_out);
	public static native int pciSecLogicJudgAdminB(byte[] data_in, int len_in, byte[] data_out, int[] len_out);
	public static native int pciSecLogicChangeAdminA(byte[] data_in, int len_in, byte[] data_out, int[] len_out);
	public static native int pciSecLogicChangeAdminB(byte[] data_in, int len_in, byte[] data_out, int[] len_out);

	public static native int getFirstrunFlag(int[] flag, int[] flagLen);
	
    public static native int readRecordSck(byte[] ResponseData, int[] ResLen, byte[] rhData, int[] rhDataLen);
    public static native int readRecordTri(byte[] ResponseData, int[] ResLen, byte[] rhData, int[] rhDataLen);
    public static native int clearRecordSck();
    public static native int clearRecordTri();

    public static native int readCA(int format, int type, int index, byte[] responseData, int[] resLen);
    public static native int writeCA(int format, int type, int index, byte[] responseData, int resLen);
    public static native int deleteCA(int format, int type, int index);
    public static native int getCAStatus(byte[] st, int[] stLen);
    public static native int pciGetPin(byte pinkey_n,byte min_len, byte max_len,byte cardlen, byte[] card_no,byte mode, int[] pin_block_len ,byte[] pin_block,
            short waittime_sec, byte mark,byte money_num, byte[] money,byte pin_len, byte[] pin_data);
    public static native int pciGetPin_other(byte type, byte pinkey_n,byte min_len, byte max_len,byte cardlen, byte[] card_no,byte mode,int[] pin_block_len,
            byte[] pin_block,int waittime_sec, byte pin_len, byte[] pin_data);

    public static native int pciOfflinePlainPin(byte CardSlot,byte min_len, byte max_len,short waittime_sec,byte pin_len, byte[] pin_data);

    public static native int pciOfflineEncPin(byte CardSlot,byte min_len, byte max_len,short waittime_sec,byte modLen,byte[] moduleStr,byte expLen,byte[] expStr, byte pin_len, byte[] pin_data);
	
	/* BCM remote download key */
    public static native int eppkeyGen(byte algo, byte[] ResponseData, byte[] ResLen);
    public static native int eppRandGen(byte algo, byte[] ResponseData, byte[] ResLen);
    public static native int pkEppRead(byte algo, byte[] ResponseData, byte[] ResLen);
    public static native int pkEppsPKVndWrite(byte[] PKEpps, int PKEppsLen, byte[] PKVnd, int PKVndLen, byte algo, byte[] ResponseData, byte[] ResLen);
    public static native int pkEppsRead(byte algo, byte[] ResponseData, byte[] ResLen);
    public static native int pkBanksPKBankVerity(byte[] PKBanks, int PKBanksLen, byte[] PKBank, int PKBankLen, byte algo, byte[] ResponseData, byte[] ResLen);

    public static native int calculateMAC2(int KeyUsage, int KeyNo,int PadMode,int AlgorMode, int CalcMod,
                         byte[] EncryptData, int EncryptDataLen,byte[] ResponseData, byte[] ResLen); 
}
