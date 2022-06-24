/*
 * instruction : New DUKPT Native for SQ29C, SQ27TG/TH,...
 * data        : Created on 2018/11/09
 * version     : V1.0.0
 * author      : RuiBo.Lin
 */

package android.device;

public class DukptNative {

    static {
        System.loadLibrary("DukptNew_jni");
    }

    /* keySetNum */
    public static int MCD_KETSETNUM = 1; // TDK Set.
    public static int EMV_KETSETNUM = 2; // reserve for emv.
    public static int PIN_KETSETNUM = 3; // PEK Set.
    public static int MAC_KETSETNUM = 4; // reserve for mac key set.

    /* keyType */
    public static int PIN_KEYTYPE = 0x01; // key for pin encryption.
    public static int MAC_KEYTYPE = 0x02; // key for calculate MAC.
    public static int DAT_KEYTYPE = 0x03; // key for track data encryption.

    /**
     * DukptInitial : Inital key loading of BDK or IPEK. <br/>
     *
     * @param keySetNum : index of dukpt keySet. <br/>
     * @param Bdk       : Bdk data if using BDK deliver.<br/>
     * @param BdkLen    : The length of Bdk. <br/>
     * @param Ipek      : Ipek data if using IPEK deliver. <br/>
     * @param Ksn       : KSN data. <br/>
     * @param KsnLen    : The length of Ksn. <br/>
     * @return : return result. <br/>
     */
    public native static int DukptInitial(int keySetNum, byte[] Bdk, int BdkLen,
                                          byte[] Ipek, int IpekLen, byte[] Ksn, int KsnLen);

    /**
     * DukptEncryptData : Base DUKPT Key encryption func. <br/>
     *
     * @param keyType   :<br/>
     *                  0x01 - Pin;
     *                  0x02 - Mac;
     *                  0x03 - TrackData. <br/>
     * @param keySetNum : index of which key sets.<br/>
     *                  0x01 - TDK Set;
     *                  0x02 - reserve for emv;
     *                  0x03 - PEK Set. <br/>
     *                  0x04 - reserve for mac key set. <br/>
     * @param dataIn    : input data. <br/>
     * @param inLen     : The length of inLen. <br/>
     * @param dataOut   : encrypted data. <br/>
     * @param outLen    : The length of dataOut. <br/>
     * @param outKsn    : outKsn data. <br/>
     * @param KsnLen    : The length of outKsn. <br/>
     * @return         : return result <br/>
     */
    public native static int DukptEncryptData(int keyType, int keySetNum, byte[] dataIn, int inLen,
                                              byte[] dataOut, int[] outLen, byte[] outKsn, int[] KsnLen);

    /**
     * DukptEncryptData : Base DUKPT Key encryption func. <br/>
     *
     * @param keyType   :<br/>
     *                  0x01 - Pin;
     *                  0x02 - Mac;
     *                  0x03 - TrackData. <br/>
     * @param keySetNum : index of which key sets.<br/>
     *                  0x01 - TDK Set;
     *                  0x02 - reserve for emv;
     *                  0x03 - PEK Set. <br/>
     *                  0x04 - reserve for mac key set. <br/>
     * @param encMode   : encMode. <br/>
     * @param iv        : iv data, inoput. <br/>
     * @param ivLen     : The length of iv, input. <br/>
     * @param dataIn    : input data. <br/>
     * @param inLen     : The length of inLen. <br/>
     * @param dataOut   : encrypted data. <br/>
     * @param outLen    : The length of dataOut. <br/>
     * @param outKsn    : outKsn data. <br/>
     * @param KsnLen    : The length of outKsn. <br/>
     * @return         : return result <br/>
     */
    public native static int DukptEncryptDataIV(int keyType, int keySetNum, int encMode,
                                                byte[] iv, int ivLen, byte[] dataIn, int inLen,
                                                byte[] dataOut, int[] outLen, byte[] outKsn, int[] KsnLen);

    public native static int DukptPinblockBySP(int mode, int iKeyType,
                                               byte[] cardNo, byte[] pinBlock, byte[] outKsn);

    /**
     * Encrypt Pin Block.
     */
    public native static int DukptPinblock(int mode, int iKeyType,
                                           byte[] inBlock, byte[] pinBlock, byte[] outKsn);


    /**
     * Encrypt Track Data.
     */
    public native static int DukptTrackData(int mode, int iKeyType,
                                            byte[] inTrack1, byte[] inTrack2, byte[] inTrack3,
                                            int[] plainTextlen,
                                            byte[] outTrack1, byte[] outTrack2, byte[] outTrack3,
                                            byte[] encPan, byte[] Ksn, int[] cipherlen);

	/*
 	 * DukptGetKsn: Get KSN Data.
 	 *
 	 * @param keySetNum : index of which key sets.<br/>
 	 *				0x01 - TDK Set;
 	 *				0x02 - reserve for emv;
 	 *				0x03 - PEK Set. <br/>
 	 *				0x04 - reserve for mac key set. <br/>
 	 * @param Ksn       : output KSN data. <br/>
 	 * @return          : return result. <br/>
 	 */	
	public native static int DukptGetKsn(int keySetNum, byte[] outKsn);

}

