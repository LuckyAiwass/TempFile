package android.device;

public class PiccReaderNative {
    static{
        System.loadLibrary("picc_jni");
    }
    public static native int picc_open();
    public static native int picc_close();
    public static native int picc_remove(byte mode);
    public static native int picc_init();
    public static native int picc_request(byte repose[],byte Atq[]);
    public static native int picc_request_norats(byte repose[],byte Atq[]);
	public static native int picc_request_type(byte pollType,byte repose[],byte Atq[]);
    //public static native int picc_requestM1(byte repose[],byte Atq[]);
    public static native int picc_exchange(byte write_buf[],int write_lenth,byte read_buf[],int read_lenth[]);
    public static native int picc_exchangeEX(byte write_buf[],int write_lenth,byte read_buf[],int read_lenth[], int CRC,int speed);
    public static native int picc_apdu(byte write_buf[],int write_lenth,byte read_buf[],int read_lenth[]);
    public static native int picc_Antisel(byte SN[],byte SAK[]);
    //public static native int picc_AntiselM1(byte SN[],byte SAK[]);
    public static native int picc_Active();
    public static native int picc_ActiveEx(byte[] atr);
    public static native int picc_M1Auth(int keyType, int blkNo, int keyLen, byte keyBuf[], int iSeriNumLen, byte SeriNum[]);
    public static native int picc_M1Read(int blkNo, byte pReadBuf[]);
    public static native int picc_M1Write(int blkNo, int iLenWriteBuf, byte pWriteBuf[]);
    public static native int picc_M1Increment(int blkNo, int iMoney);
    public static native int picc_M1Decrement(int blkNo, int iMoney);
    public static native int picc_M1Restore(int blkNo);
    public static native int picc_M1Transfer(int blkNo);
    public static native int picc_M1Init(int blkNo,int value);
	public static native int picc_M1Revalue(int blkNo);

    /* Type B */
	public static native int picc_initB();
	public static native int picc_exchangeB(byte write_buf[],int write_lenth,byte read_buf[],int read_lenth[]);

    /* Type F */
    public static native int picc_exchangeF(
            int cmd, int num,
            byte write_buf[], int write_lenth,
            byte read_buf[], int read_lenth[]);
	public static native int picc_config(byte config[]);

    public static native int picc_apduFelica(byte write_buf[], int write_lenth, byte read_buf[], int read_lenth[]);
    public static native int picc_setTimeOutFelica(int time_out);

	public static native int picc_M1IncrementTransfer(int blkNoSrc, int iMoney,int blkNoDst);
	public static native int picc_M1DecrementTransfer(int blkNoSrc, int iMoney,int blkNoDst);
	public static native int picc_M1RestoreTransfer(int blkNoSrc,int blkNoDst);
	
	/* Type Mifare UltraLight */
    public static native int picc_MifareUlPageRead(int bSectorNum, byte pReadBuf[]);
    public static native int picc_MifareUlPageWrite(int bSectorNum, byte pWriteBuf[]);

    public static native int picc_MifareUlReadCnt(byte bCntNum, int pCntValue[]);
	public static native int picc_MifareUlIncrCnt(byte bCntNum, int iCntValue);
	public static native int picc_TransactionData(byte bProtocol,
									byte pSendBuf[], int bSendLen,
									byte pRecvBuf[], int pRecvLen[]);

	/* Type S(SRT512) */
	public static native int picc_Srt512ChipIDGet(byte pValue[]);
	public static native int picc_Srt512UIDGet(byte bChipID, byte pValue[]);
	public static native int picc_Srt512Select(byte bChipID);
	public static native int picc_Srt512BlockRead(byte bAddr, byte pValue[]);
	public static native int picc_Srt512BlockWrite(byte bAddr, byte pValue[], int iVauleLen);
	public static native int picc_Srt512Completion();
	public static native int picc_Srt512Rst2Inventory();
	
}
                                                                                                                                                                                                                                                                                                                                                                                                                   