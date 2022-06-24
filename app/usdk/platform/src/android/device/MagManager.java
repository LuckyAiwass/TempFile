package android.device;
import android.util.Log;
import android.os.IMaxqEncryptService;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.ServiceManager;

public class MagManager {
    private static final String TAG = "MagManager";
    private SEManager mSEManager;
    private IMaxqEncryptService mService;
    private int fd = -1;
    public MagManager() {
        mSEManager = new SEManager();
        IBinder b = ServiceManager.getService("maxqservice");
        mService = IMaxqEncryptService.Stub.asInterface(b);
    }
    /**
     * 初始化磁条卡读写器,执行此命令后,磁条卡读写器处于等待用户刷卡状态
     * @return RspCode.RSPOK 初始化磁条卡模块成功, RspCode.RSPERR 初始化磁条卡模块失败
     */
    public int open() {
        try{
            fd = mService.magOpen(0);
            return (fd != 0 ? 0 : -1);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
        return -1;
        
    }
    /**
     * 初始化磁条卡读写器,执行此命令后,磁条卡读写器处于等待用户刷卡状态
     * @return RspCode.RSPOK 初始化磁条卡模块成功, RspCode.RSPERR 初始化磁条卡模块失败
     */
    public int open(int magType) {
        try{
            fd = mService.magOpen(magType);
            return (fd != 0 ? 0 : -1);
        } catch(RemoteException e) {
            e.printStackTrace();
        }
        return -1;
        
    }
    /**
     * 关闭磁条读写器模块
     * @return RspCode.RSPOK 关闭磁条卡模块成功, RspCode.RSPERR 关闭磁条卡模块失败
     */
    public int close() {
        try{
            if(fd == -1) return -1;
            return mService.magClose(fd);//RspCode.RSPERR;
        } catch(RemoteException e) {
            e.printStackTrace();
        }
        return -1;
        
    }
    /**
     * 查看是否磁条卡检测到有用户刷卡动作
     * @return RspCode.RSPOK 检测到有用户刷卡动作, RspCode.RSPERR 未检测到有用户刷卡动作
     */
    public int checkCard() {
        try{
            if(fd == -1) return -1;
            return mService.magCheckDev(fd);//RspCode.RSPERR;
        } catch(RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }
    /**
     * 获取磁条卡的所有磁道(1、2、3) 的信息
     * @param info 数据缓冲区.用于存储获取到的磁条卡 3 个磁道的信息
                         格式为:
                            第一字节表示磁道序号,
                            第二个字节是磁道数据字节数 N,
                            第三个字节到 N+3 字节是磁道数据;
                            磁道数据是 ASCII 码串;
                            三个磁道信息按顺序组合(TLV 格式)
                            TAG 定义:01 = 磁道 1, 02 = 磁道 2, 03 = 磁道 3
                            00 = 有刷卡动作,但读磁道失败
     *@return 获取到的磁条卡信息字节数
     * */
    public int getAllStripInfo(byte[] info ) {
        try{
            if(fd == -1) return -1;
            return mService.magGetAllStripInfo(fd, info);//RspCode.RSPOK;
        } catch(RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }
    /**
     * 获取磁条卡指定磁道 (1/2/3) 的信息
     * @param strip 指定需要获取此磁道上的信息
     * @param info 数据缓冲区.用于存储获取到的磁条卡指定磁道上的信息,采用 ascii 编码
     * @return 获取到的磁条卡信息字节数
     **/
    public int getSingleStripInfo(int strip, byte[] info ) {
        try{
            if(fd == -1) return -1;
            return mService.magGetSingleStripInfo(fd, strip, info);//RspCode.RSPOK;
        } catch(RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }


	public int getEncryptStripInfo(int AlgMode, int keyIndex, byte[] info, byte[] cardNo, byte[] KSN) {
        if(fd == -1) return -1;
        int allLen = -1;
        if(info == null || cardNo == null || KSN == null) return -2;
        if(AlgMode == 0) {
        } else if(AlgMode == 1) {//dupkt key
            byte[] stripInfo = new byte[1024];
            try{
                allLen = mService.magGetAllStripInfo(fd, stripInfo);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
            
            if (allLen > 0) {
                int len = stripInfo[1];
                byte[] CARD_TRACK_1 = null;
                byte[] CARD_TRACK_2 = null;
                byte[] CARD_TRACK_3 = null;
                int[] plainTextlen = new int[3];
                byte[] encPan = new byte[50];
                //byte[] Ksn = new byte[10];
                int[] cipherlenbuf = new int[4];
                if (len != 0) {
                    CARD_TRACK_1 = new byte[len];
                    System.arraycopy(stripInfo, 2, CARD_TRACK_1, 0, len);
                }
                int len2 = stripInfo[3 + len];
                if (len2 != 0) {
                    CARD_TRACK_2 = new byte[len2];
                    System.arraycopy(stripInfo, 4 + len, CARD_TRACK_2, 0, len2);
                    
                    String pan = new String(CARD_TRACK_2);
                    int index = pan.indexOf("=");
                    if (index != -1) {
                        System.arraycopy(CARD_TRACK_2, 0, cardNo, 0, cardNo.length > index ? index : cardNo.length);
                    }
                }
                int len3 = stripInfo[5 + len+len2];
                if (len3 != 0 && len3 < 1024) {
                    CARD_TRACK_3 = new byte[len3];
                    System.arraycopy(stripInfo, 6 + len + len2, CARD_TRACK_3, 0, len3);
                }
                plainTextlen[0] = len;
                plainTextlen[1] = len2;
                plainTextlen[2] = len3;
                byte[] outTrack1 = new byte[100];
                byte[] outTrack2 = new byte[50];
                byte[] outTrack3 = new byte[120];
                int ret = -1;
                try{
                    ret = mService.DukptTrackData(3, 1, len > 0 ? CARD_TRACK_1 : "".getBytes(), len2 > 0 ? CARD_TRACK_2 : "".getBytes(), len3 > 0 ? CARD_TRACK_3 : "".getBytes(),plainTextlen,outTrack1, outTrack2, outTrack3, encPan, KSN, cipherlenbuf);
                } catch(RemoteException e) {
                    e.printStackTrace();
                }
                
                //Log.d("MagManager"," DukptNative.DukptTrackData ret =  " + ret);
                if(ret == 0) {
                    info[0] = 1;
                    if(cipherlenbuf[0] > 0) {
                        len = cipherlenbuf[0];
                        info[1] = (byte)len;
                        System.arraycopy(outTrack1, 0, info, 2, len);
                    }
                    info[2 + len] = 2;
                    if(cipherlenbuf[1] > 0) {
                        len2 = cipherlenbuf[1];
                        info[3 + len] = (byte)len2;
                        System.arraycopy(outTrack2, 0, info, 4 + len, len2);
                    }
                    info[4 + len+len2] = 3;
                    if(cipherlenbuf[2] > 0) {
                        len3 = cipherlenbuf[2];
                        info[5 + len+len2] = (byte)len3;
                        System.arraycopy(outTrack3, 0, info, 6 + len + len2, len3);
                    }
                    allLen = 6 + len + len2 + len3;
                } else {
                    allLen = -1;
                }
            }
        }
        return allLen;
    }
	public int getEncryptStripInfo(int AlgMode, int keyIndex, int mode, byte[] info, byte[] cardNo, byte[] KSN, byte[] chipTag) {
        if(fd == -1) return -1;
        int allLen = -1;
        if(info == null || cardNo == null || KSN == null || chipTag == null) return -2;
        if(AlgMode == 0) {
        } else if(AlgMode == 1) {//dupkt key
            byte[] stripInfo = new byte[1024];
            try{
                allLen = mService.magGetAllStripInfo(fd, stripInfo);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
            if (allLen > 0) {
                ////Log.d("magManager", DecodeConvert.bytesToHexString(stripInfo, 0, allLen));
                int len = stripInfo[1];
                byte[] CARD_TRACK_1 = null;
                byte[] CARD_TRACK_2 = null;
                byte[] CARD_TRACK_3 = null;
                int[] plainTextlen = new int[3];
                byte[] encPan = new byte[50];
                //byte[] Ksn = new byte[10];
                int[] cipherlenbuf = new int[4];
                if (len != 0) {
                    CARD_TRACK_1 = new byte[len];
                    System.arraycopy(stripInfo, 2, CARD_TRACK_1, 0, len);
                }
                int len2 = stripInfo[3 + len];
                if (len2 != 0) {
                    CARD_TRACK_2 = new byte[len2];
                    System.arraycopy(stripInfo, 4 + len, CARD_TRACK_2, 0, len2);

                    String pan = new String(CARD_TRACK_2);
                    int index = pan.indexOf("=");
                    if (index != -1) {
                        System.arraycopy(CARD_TRACK_2, 0, cardNo, 0, cardNo.length > index ? index : cardNo.length);
                        if(index + 5 < len2)
                            chipTag[0] = CARD_TRACK_2[index + 5];
                    }
                }
                int len3 = stripInfo[5 + len+len2];
                if (len3 != 0 && len3 < 1024) {
                    CARD_TRACK_3 = new byte[len3];
                    System.arraycopy(stripInfo, 6 + len + len2, CARD_TRACK_3, 0, len3);
                }
                plainTextlen[0] = len;
                plainTextlen[1] = len2;
                plainTextlen[2] = len3;
                byte[] outTrack1 = new byte[250];
                byte[] outTrack2 = new byte[50];
                byte[] outTrack3 = new byte[120];
                int ret = -1;
                byte[] inTrack = new byte[250];
                int inLength =0;
                inTrack[inLength++] = 0x56;
                if(len > 0) {
                    inTrack[inLength++] = (byte)(len + 2);
                    inTrack[inLength++] = 0x25;
                    System.arraycopy(CARD_TRACK_1, 0, inTrack, inLength, len);
                    inLength = len + inLength;
                    inTrack[inLength++] = 0x3F;
                } else {
                    inTrack[inLength++] = 0x00;
                }
                inTrack[inLength++] = 0x57;
                if(len2 > 0) {
                    inTrack[inLength++] = (byte)(len2 + 2);
                    inTrack[inLength++] = 0x3B;
                    System.arraycopy(CARD_TRACK_2, 0, inTrack, inLength, len2);
                    inLength = len2 + inLength;
                    inTrack[inLength++] = 0x3F;
                }
                inTrack[inLength++] = 0x58;
                inTrack[inLength++] = 0x00;
                plainTextlen[0] = inLength;
                plainTextlen[1] = 0;//罗大鹏的接口做了改动，只能调用一次加密接口，印度数据格式
                plainTextlen[2] = 0;
                ////Log.d("magManager", " len " + len + " len2 " + len2 + " len3 " + len3 + " inLength " + inLength);
                try{
                    ret = mService.DukptTrackData(3, 1, inTrack, len2 > 0 ? CARD_TRACK_2 : "".getBytes(), len3 > 0 ? CARD_TRACK_3 : "".getBytes(),plainTextlen,
                        outTrack1, outTrack2, outTrack3, encPan, KSN, cipherlenbuf);
                } catch(RemoteException e) {
                    e.printStackTrace();
                }
                ////Log.d("magManager", " ret " + ret);
                if(ret == 0) {
                    if(cipherlenbuf[0] > 0) {
                        len = cipherlenbuf[0];
                        System.arraycopy(outTrack1, 0, info, 0, len);
                        allLen = len;
                        ////Log.d("magManager", DecodeConvert.bytesToHexString(outTrack1, 0, len));
                    }
                } else {
                    allLen = -1;
                }
            }
        }
        return allLen;
    }

    public int getEncryptStripInfoUP(int keyIndex, byte[] cardNo,byte[] encryptTrackSecond, byte[] encryptThird, int[] tracksLen) {
        int retCode = 0;
        if(fd == -1) return -1;
        int allLen = -1;
        if(cardNo == null ||tracksLen.length < 2) return -2;

        byte[] stripInfo = new byte[1024];
        try{
            allLen =  mService.magGetAllStripInfo(fd, stripInfo);
        } catch(RemoteException e) {
                    e.printStackTrace();
        }
        
        if (allLen > 0) {
            //Log.d(TAG,"stripInfo = "+bytesToHexString(stripInfo,0,stripInfo.length));
            int len = stripInfo[1];
            byte[] CARD_TRACK_1 = null;
            byte[] CARD_TRACK_2 = null;
            byte[] CARD_TRACK_3 = null;
            int[] plainTextlen = new int[3];
            byte[] encPan = new byte[50];
            //byte[] Ksn = new byte[10];
            int[] cipherlenbuf = new int[4];
            //Log.d(TAG,"len = "+len);
            if (len != 0) {
                CARD_TRACK_1 = new byte[len];
                System.arraycopy(stripInfo, 2, CARD_TRACK_1, 0, len);
            }
            int len2 = stripInfo[3 + len];
            //Log.d(TAG,"len2 = "+len2);
            if (len2 != 0) {
                CARD_TRACK_2 = new byte[len2];
                System.arraycopy(stripInfo, 4 + len, CARD_TRACK_2, 0, len2);

                String pan = new String(CARD_TRACK_2);
                //Log.d(TAG,"pan = " + pan);
                int index = pan.indexOf("=");
                if (index != -1) {
                    System.arraycopy(CARD_TRACK_2, 0, cardNo, 0, cardNo.length > index ? index : cardNo.length);
                }
            }
            int len3 = stripInfo[5 + len+len2];
            //Log.d(TAG,"len3 = "+len3);
            if (len3 != 0 && len3 < 1024) {
                CARD_TRACK_3 = new byte[len3];
                System.arraycopy(stripInfo, 6 + len + len2, CARD_TRACK_3, 0, len3);
            }
            
            // extract the data of track to encrypt according to the UnionPay Spec.
            byte[] tdb2 =  new byte[8];
            byte[] tdb3 =  new byte[8];
            
            byte[] EStartValue = new byte[] { 0x00 };
            byte[] pchar = new byte[1];

            byte[] response = new byte[256];
            int[] reslen = new int[1];
            
            if(CARD_TRACK_2 != null){
                String track2 = new String(CARD_TRACK_2);

                int track2Length = track2.length();
                int startIndex;
                int endIndex;
                //Log.d(TAG, "track2 = " + track2 + " track2Length :" + track2Length);

                if(track2Length % 2 != 0){
                    startIndex = track2Length - 17;
                    endIndex = track2Length - 1;
                }else{
                    startIndex = track2Length - 18;
                    endIndex = track2Length - 2;
                }
                String tdb2Str = track2.substring(startIndex, endIndex);
                tdb2 = hexStr2BytesStripSpec(tdb2Str);
                if(tdb2 == null) return -1;
                //Log.d("magManager", "tdb2Str = " + tdb2Str);

                byte[] encrypTdb2 = new byte[8];
                int ret = mSEManager.encryptDataTDK(keyIndex, 1, EStartValue, EStartValue.length,		
					pchar[0], tdb2, tdb2.length, response, reslen);
                if (ret != 0) {
                    return -10;
                }
                //Log.d("magManager", "encryptDataTDK ret = " + ret + "  reslen =" + reslen[0] + "response = " + bytesToHexString(response,0,reslen[0]));
            
                System.arraycopy(response, 0, encrypTdb2, 0, reslen[0]);
 
                String encrypTdb2Str = bytesToHexString(encrypTdb2, 0, encrypTdb2.length);
                //Log.d("magManager", "encrypTdb2Str = " + encrypTdb2Str);
            
                String outTracktemp2 = new String(CARD_TRACK_2);
                //Log.d("magManager", "outTracktemp2 = " + outTracktemp2);
                outTracktemp2 = outTracktemp2.replace(tdb2Str, encrypTdb2Str);
                //Log.d("magManager", "replace outTracktemp2 = " + outTracktemp2);

                //System.arraycopy(encrypTdb2, 0, encrypTdb2Str, startIndex, encrypTdb2Str.length());
                //encryptTrackSecond = outTracktemp2.getBytes();
                System.arraycopy(outTracktemp2.getBytes(), 0, encryptTrackSecond, 0, len2);
                //Log.d("magManager", "  encryptTrackSecond = " + bytesToHexString(encryptTrackSecond,0,len2));
                tracksLen[0] = len2; // Track2
            }
            if(CARD_TRACK_3 != null){
                String track3 = new String(CARD_TRACK_3);
                int track3Length = track3.length();
                int startIndex;
                int endIndex;
                 //Log.d(TAG, "track3 = " + track3 + " track3Length :" + track3Length);
                if(track3Length % 2 != 0){
                    startIndex = track3Length - 17;
                    endIndex = track3Length - 1;
                }else{
                    startIndex = track3Length - 18;
                    endIndex = track3Length - 2;
                }
                String tdb3Str = track3.substring(startIndex, endIndex);
                tdb3 = hexStr2BytesStripSpec(tdb3Str);
                if(tdb3 == null) return -1;
                //Log.d("magManager", "tdb3 = " + tdb3Str);

                byte[] encrypTdb3 = new byte[8]; 
                int ret = mSEManager.encryptDataTDK(keyIndex, 1, EStartValue, EStartValue.length,		
					pchar[0], tdb3, tdb3.length, response, reslen);
                if (ret != 0) {
                    return -11;
                }
                //Log.d("magManager", "encryptDataTDK ret = " + ret + "  reslen =" + reslen[0] + "response = " + bytesToHexString(response,0,reslen[0]));            
                System.arraycopy(response, 0, encrypTdb3, 0, reslen[0]);

                String encrypTdb3Str = bytesToHexString(encrypTdb3, 0, encrypTdb3.length); 

                //Log.d("magManager", " encrypTdb3Str = " + encrypTdb3Str);
                String outTracktemp3 = new String(CARD_TRACK_3);
                //Log.d("magManager", " outTracktemp3 = " + outTracktemp3);
                outTracktemp3 = outTracktemp3.replace(tdb3Str, encrypTdb3Str);  
                //Log.d("magManager", "replace outTracktemp3 = " + outTracktemp3);        
                //System.arraycopy(encrypTdb3, 0, encrypTdb3Str, startIndex, encrypTdb3Str.length());
                //encryptThird = outTracktemp3.getBytes();
                System.arraycopy(outTracktemp3.getBytes(), 0, encryptThird, 0, len3);
                //Log.d("magManager", "  encryptThird = " + bytesToHexString(encryptThird,0,len3));
                tracksLen[1] = len3; // Track3
            }
        
        }
    
        return retCode;
    }
    public byte[] hexStr2BytesStripSpec(String str) {
        if (str == null)
            return null;
        else if (str.length() < 2)
            return null;
        else {
            int len = str.length() / 2;
            byte[] temp = str.getBytes();
            for (int i = 0; i < temp.length; i++) {
                byte ch = temp[i];
                if (ch == '=')
                    temp[i] = 'D';
            }
            try{
                String hex = new String(temp);
                byte[] buffer = new byte[len];
                for (int i = 0; i < len; i++) {
                    buffer[i] = (byte) Integer.parseInt(
                        hex.substring(i * 2, i * 2 + 2), 16);
                }
                return buffer;
            }catch (Exception e){
                return null;
            }
        }
    
   }

    public  String bytesToHexString(byte[] src, int offset, int length) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = offset; i < length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

}
