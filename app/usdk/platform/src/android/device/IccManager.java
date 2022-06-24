/*
 * Copyright (C) 2008 The Android Open Source Project
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
 */
package android.device;
import android.os.Build;
import android.util.Log;
import android.device.IccReaderNative;
import android.device.RspCode;
import android.icc.IccNative;
import android.os.ServiceManager;
import android.os.IBinder;
import android.os.IMaxqEncryptService;
import android.os.RemoteException;
import android.device.SEManager;
/**
 * 模块：    POS接触式卡<br>
 * 版本号:   v2.2.4<br>
 * 更新时间: 2015/11/27<br>
 *           v2.2.4 2015/11/27 接触式IC卡添加ISO4436卡片协议操作接口：<br>
 *           添加减金额接口 
 *          int sle4436_decValue(int pValue)<br>
 * 更新时间: 2015/11/24<br>
 *           v2.2.4 2015/11/24 接触式IC卡添加ISO4436卡片协议操作接口：<br>
 *           int Sle4436_reset(int fd, byte[] pAtr);<br>
 *           byte[] Sle4436_readMemory(int fd,int addr, int length);<br>
 *           int Sle4436_writeMemory(int fd, int addr, byte[] data, int length);<br>
 *           int Sle4436_writeCarry(int fd, int mode, int addr, byte[] data, int length);<br>
 *           int Sle4436_verifyPassword(int fd, byte[] passwd);<br>
 *           byte[] Sle4436_authenticate(int fd, int key, byte[] clkCnt, byte[] challengeData);<br>
 *           int Sle4436_regIncrease(int fd, int shiftBits);<br>
 *           int Sle4436_readBit(int fd, byte[] pData);<br>
 *           int Sle4436_writeBit(int fd);<br>
 *           int Sle4436_reloadByte(int fd);<br>
 * 特别说明：<br>
 * 本类仅用于POS类设备。<br>
 */
public class IccManager {
    private static final String TAG="IccManager";
    private int slot = 0;
    private int  fd = -1;
    private int volt = -1;
	private double voltSelect = 3;

    private IMaxqEncryptService mService;
	private SEManager mSEManager;
    private  boolean card1_detected = false;
    private  boolean card2_detected = false;
    public IccManager() {
        IBinder b = ServiceManager.getService("maxqservice");
        mService = IMaxqEncryptService.Stub.asInterface(b);
		mSEManager = new SEManager();
    }

    /**
     * @hide
    * Return true if open succeed. 
    * @see #openIcc()
    */
    public boolean IccOpen()
    {   
        
	    return true;//IccNative.IccOpen();
    }
    /**
     * @hide
    * Return true if close succeed 
    * @see #closeIcc()
    */
    public boolean IccClose()
    {   
	    return true;//IccNative.IccClose();
    }
    /**
    * Set PPS Card Baudrate
    * @param FI The follow values are allowed for baudrate a range:1~16
    * @param DI The follow values are allowed for baudrate a range:1~16
    */
    public boolean IccSetBaudrate(int FI, int DI)
    {   
        return true;//IccNative.IccSetBaudrate((char)FI, (char)DI);
    }

     /**
      * @hide
    * Return true if  succeed 
    * @see #closeIcc()
    */
	public byte[] IccFound() {
		return null;//IccNative.IccFound();
	}
      /**
       * @hide
    * Return true if  succeed 
    * @see #closeIcc()
    */
    public int IccSelect(char solt)
    {   
        return 0;//IccNative.IccSelect(solt);
    }
       /**
        * @hide
    * Return true if  succeed 
    * @see #doAck()
    */
	public byte[] IccAct(char vol) 
	{
		return null;//IccNative.IccAct(vol);
	}
       /**
        * @hide
    * Return true if  succeed 
    * @see #doDefaultSet()
    */
	public byte[] IccExapdu(byte[] apdu_utf, char apdu_count) 
	{
		return null;//IccNative.IccExapdu(apdu_utf, apdu_count);
	}
	
    /**
     * 初始化 IC 卡
     * @param slot 被选取的卡槽：0 用户卡槽； 1 PSAM卡槽
     * @param  CardType     0x01 - IC card;
     *                                    0x02 - SLE4442;
     *
     * @param  Volt    初始化电压值:
     *                          0x01 - 3V;
     *                          0x02 - 5V;
     *                          0x03 - 1.8V;
     * @return              [0 - success, -1 - failed]
     */


    public int open(byte slot, byte CardType, byte Volt) {
        if(slot == 0) {
            if(fd > 0)  {
                return 0;
            }
            fd = IccReaderNative.open();
            if(fd > 0 ) {
                this.slot = slot;
                return IccReaderNative.IccOpen(fd, CardType, Volt);
            }
            return (fd != 0 ? 0 : -1);
        } else {
            this.slot = slot;
            volt = Volt;
            /*IccNative.IccSelect(slot == 2 ? (char)1 : (char)0);
            return IccNative.IccOpen() ? 0 : -1;*/
			try {
			    int ret = mService.psamOpen(slot, CardType, Volt);
                if((slot==1 && !card1_detected) || (slot==2 && !card2_detected)){
                    Log.e(TAG,"psam open delay ");
                    Thread.sleep(300);
                }
                return ret;
			} catch(RemoteException e) {
                e.printStackTrace();
            } catch(Exception e) {
                e.printStackTrace();
            }
			return -1;
        }
    }
    public int close() {
        if(slot == 0) {
            if(fd > 0) {
                IccReaderNative.IccClose(fd);
                IccReaderNative.close(fd);
                fd = -1;
            }
        } else {
            //return IccNative.IccClose() ? 0 : -1;
            try {
			    return mService.psamClose();
			} catch(RemoteException e) {
                e.printStackTrace();
            }
			return -1;
        }
        return 0;
   }
    /**
     * 检测IC卡是否已插入
     * 
     * @return {@link RspCode#RSPOK} 检测到IC卡插入；{@link RspCode#RSPERR} IC卡插入检测失败
     */
    public int detect() {
        byte[] rps = new byte[4];
        if(slot == 0) {
            if(fd == -1) return fd;
            int res = IccReaderNative.Detect(fd, rps);
            if(res != 0) return res;
        } else {
            //rps = IccFound();
            try {
                Log.e(TAG,"card1_detected >>>>>> "+card1_detected);
                Log.e(TAG,"card2_detected >>>>>> "+card2_detected);
			    int ret = mService.psamDetect();
                if ((ret != 0) && ((slot==1 && card1_detected) || (slot==2 && card2_detected))) {
                    for(int i=0; i<3; i++) {
                        byte[] pAtr = new byte[124];
                        if(slot==2){
                            mSEManager.selectPsam2PowerSource(i);
                        }
                        int res = mService.psamActivate(pAtr);
                        if (res > 0) {
                            ret =  mService.psamDetect();
                            if (ret == 0) {
                                break;
                            }
                        }
                    }
                }

                if (ret == 0) {
                    if(slot == 1){
                        card1_detected = true;
                    }else{
                        card2_detected = true;
                    }
                } else {
                    if(slot == 1){
                        card1_detected = false;
                    }else{
                        card2_detected = false;
                    }
                }
                return ret;
			} catch(RemoteException e) {
                e.printStackTrace();
            }
			return -1;
        }
        int status = -1;
        for(int i = 0; i < 4; i++) {
            if(rps[i] == 1) {
                status = 0;
                break;
            }
        }
        return status;
    }
    /**
     * 对卡片进行上电复位。
     * @param pAtr 返回的 ATR,以 ascii 码存储
     * @return 卡片 ATR 长度 (int 型)；否则{@link RspCode#RSPERR} 对卡片上电失败
     */
    public int reset(byte[] pAtr) {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Reset(fd, pAtr);
        } else {
            /*double vol = 5;
            switch (volt) {
                case 3:
                    vol =  1.8;
                    break;
                case 1:
                    vol =  3;
                    break;
                case 2:
                    vol =  5;
                    break;
                }
            byte[] res = IccNative.IccAct((char)vol);
            if(res == null) return -1;
            int min = pAtr.length > res.length ? res.length : pAtr.length;
            System.arraycopy(res, 0, pAtr, 0, min);
            return res.length;*/
            return -1;
        }
    }

    public int activate(byte[] pAtr) {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Reset(fd, pAtr);
        } else {
            double vol = 5;
			int ret;
            switch (volt) {
                case 3:
                    vol =  1.8;
                    break;
                case 1:
                    vol =  3;
                    break;
                case 2:
                    vol =  5;
                    break;
            }
            /*byte[] res = IccNative.IccAct((char)vol);
            if(res == null) return -1;
            int min = pAtr.length > res.length ? res.length : pAtr.length;
            System.arraycopy(res, 0, pAtr, 0, min);
            return res.length;*/
            try {
                ret = mService.psamActivate(pAtr);
                Log.e(TAG,"activate ret >>>>>> "+ret);
                if(ret <= 0){
                    for (int i = 0; i < 3; i++) {
                        if(slot==2){
                            mSEManager.selectPsam2PowerSource(i);
                        }
                        ret = mService.psamActivate(pAtr);
                        if (ret > 0) {
                            if(i == 0){
                                voltSelect = 1.8;
                            }else if(i == 1){
                                voltSelect = 3;
                            }else if(i == 2){
                                voltSelect = 5;
                            }
                            break;
                        }
                    }
                }
				return ret;
			} catch(RemoteException e) {
                e.printStackTrace();
            }
			return -1;
        }
    }
    
    public int activateEx(byte[] pAtr) {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.ResetEx(fd, pAtr);
        } else {
            /*double vol = 5;
            switch (volt) {
                case 3:
                    vol =  1.8;
                    break;
                case 1:
                    vol =  3;
                    break;
                case 2:
                    vol =  5;
                    break;
            }
            byte[] res = IccNative.IccAct((char)vol);
            if(res == null) return -1;
            int min = pAtr.length > res.length ? res.length : pAtr.length;
            System.arraycopy(res, 0, pAtr, 0, min);
            return res.length;*/
            return -1;
        }
    }
    /**
     *传输 APDU 命令给 IC 卡。
     * @param apdu 需要被发送到卡片的 apdu 指令的 bcd 码,命令格式符合 7816规范
     * @param apduLen Apdu 命令的长度
     * @param rsp 卡片对 apdu 命令的响应数据,bcd 码
     * @param sw 卡片返回的状态字,bcd 码
     * @return 卡片响应数据的长度（int 型）；否则{@link RspCode#RSPERR} 传输APDU命令失败
     */
    public int apduTransmit(byte[] apdu, int apduLen, byte[] rsp, byte[] sw) {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.ApduTransmit(fd, apdu, apduLen, rsp, sw);
        } else {
            /*byte[] res = IccNative.IccExapdu(apdu, (char)apduLen);
            if(res == null) return -1;
            int min = rsp.length > res.length ? res.length : rsp.length;
            System.arraycopy(res, 0, rsp, 0, min);
            min = sw.length >=2 ? 2 : 1;
            System.arraycopy(res, res.length-2, sw, 0, min);
            return res.length;*/
            try {
			    return mService.psamApduTransmit(apdu, apduLen, rsp, sw);
			} catch(RemoteException e) {
                e.printStackTrace();
            }
			return -1;
        }
        
    }
    /**
     *给IC卡下电
     *
     * @return {@link RspCode#RSPOK} 给卡片下电成功；{@link RspCode#RSPERR} 给卡片下电失败
     */
    public int remove() {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Eject(fd);
        }
        return 0;
    }
    public int deactivate() {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Eject(fd);
        }
        //return 0;
        try {
			    return mService.psamDeactivate();
			} catch(RemoteException e) {
                e.printStackTrace();
            }
			return -1;
    }

    public int setParam(int type, byte[] params) {
        try {
	        return mService.psamSetParam(type, params);
		} catch(RemoteException e) {
            e.printStackTrace();
        }
	    return -1;
    }

    public int getParam(int type, byte[] params) {
        try {
	        return mService.psamGetParam(type, params);
		} catch(RemoteException e) {
            e.printStackTrace();
        }
	    return -1;
    }

	public int setBaudrate(int FI, int DI) {
        try {
	        return mService.psamSetBaudrate(FI, DI);
		} catch(RemoteException e) {
            e.printStackTrace();
        }
	    return -1;
    }

	public int setVoltageClass(int voltageClass) {
        try {
	        return mService.psamSetVoltageClass(voltageClass);
		} catch(RemoteException e) {
            e.printStackTrace();
        }
	    return -1;
    }

	public int getVoltageClass() {
        try {
	        return mService.psamGetVoltageClass();
		} catch(RemoteException e) {
            e.printStackTrace();
        }
	    return -1;
    }

	public int getCardType() {
        try {
	        return mService.psamGetCardType();
		} catch(RemoteException e) {
            e.printStackTrace();
        }
	    return -1;
    }
    
    /**
     *设置 APDU 接收响应超时时间,默认时间为 10 秒钟。对于一些卡片耗时超过此时间的命令,可调用此函数延长接收超时时间。
     *@param 超时时间,单位为毫秒(ms)
     */
    /*public void SetApduTimeOut(int ms) {
        IccReaderNative.SetApduTimeOut(ms);
    }*/
    /**
     * 设置是否由驱动层来对 61xx,6Cxx 等命令进行自动数据获取,默认为自动获取。
     * @param autoFlag 0:表示由应用层来获取;1:表示由驱动层进行自动获取
     * @return {@link RspCode#RSPOK} 设置成功；{@link RspCode#RSPERR} 设置成功
     */
    public int getResponseEnable(byte autoFlag) {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.GetResponseEnable(fd, autoFlag);
        } else {
            try {
	            return mService.psamGetResponseEnable(autoFlag);
		    } catch(RemoteException e) {
                e.printStackTrace();
            }
	        return -1;
        }
    }
    /**
     * 设置 ETU 时间,默认为 372 个 clock。
     * @param etuTime ETU时间
     * @return {@link RspCode#RSPOK} 设置成功；{@link RspCode#RSPERR} 设置成功
     */
    public int setETU(int etuTime) {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.SetETU(fd, etuTime);
        }
        return 0;
    }
    /**
     *应用层终止 APDU 响应的接收,在等待超时时间段内,若是应用需要立即停止接收,则调用此函数。
     *一般用于应用程序程序终止时,由于正在接收 APDU 响应,为避免应用退出不干净,而调用此函数。
     */
    /*public void stopApduRspRecv() {
        IccReaderNative.StopApduRspRecv(fd);
    }*/
    
    /**
     * 对4442卡片进行上电复位。
     * @param pAtr 返回的 ATR,以 ascii 码存储
     * @return 卡片 ATR 长度 (int 型)；否则{@link RspCode#RSPERR} 对卡片上电失败
     */
    public int sle4442_reset(byte[] pAtr) {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Sle4442_Reset(fd, pAtr);
        }
        return 0;
    }

    public int sle4442_activate(byte[] pAtr) {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Sle4442_Reset(fd, pAtr);
        }
        return 0;
    }

    /**
     * Activate  IC4442 from the selected slot.
     * @param volt The operating voltage.  Could be 5V, 3V etc
     * @param atr_len for the byte array indicating the ATR length.
     * @return Returns byte array indicating the ATR if successful.  Returns null if failed.
      */
     /*public byte[] sle4442_activate(char volt,byte[] atr_len) 
     {   
         //byte[] ret = IccNative.Sle4442Act(volt,atr_len);
         //return ret;
         return null;
     }*/
     /**
      * Read the data stored in main memory for SLE4442
     * @param addr The starting address of operation data, the range of the parameters of the SLE4442 card: 0---255.
     * @param length To read the data length, the range of the parameters of the SLE4442 card: 1---256. 
     * In addition to ByteAddr and Length and cannot be greater than the actual capacity of the card, 
     * otherwise the reader will refuse to execute the command, and returns an error.
     * @return Returns byte array indicating the store data if successful. Returns null if failed.
      */
     public byte[] sle4442_readMainMemory(int addr,int length) 
     {   
         if(slot == 0) {
             if(fd == -1) return null;
             byte[] ret = IccReaderNative.Sle4442_ReadMainMemory(fd, addr, length);
             return ret;
         }
         return null;
     }
     /**
      * Write data to the main storage area SLE4442
     * @param addr The starting address of operation data, the range of the parameters of the SLE4442 card: 0---255.
     * @param data The data to be written
     * @return Returns 0 if successful. Returns -1 if failed.
      */
     public int sle4442_writeMainMemory(int addr,byte[] data, int dataLen) 
     {   
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.Sle4442_WriteMainMemory(fd, addr, data, dataLen);
         }
         return -1;
     }   
     /**
      * Read SLE4442 card protection bit storage data (4 BYTE).
      * 4 bytes of data read is to save the SLE4442 bits data in storage area. 
      * Sequential data bit: low in front, high in the post. 
      * Such as: read data: 30 FF 1F F8 from the first byte of 30H 
      * can be analyzed as follows: the binary 30H 
      * corresponding to the lower four bits: 00110000 to 0 represent the four byte SLE4442 of the card (answer to reset) cannot be changed
     * @param address 0x00- 0xff
     * @param len   Length to read, 4 Bytes:
     *                          bit31 - read out 32Bytes;
     *                          bit0  - read out 1Byte;
     *                          bit*  - etc.
     * @return Returns byte array indicating protected storage data if successful. Returns null if failed. 
      */
     public byte[] sle4442_readProtectionMemory(int address, int len) 
     {
         if(slot == 0) {
             if(fd == -1) return null;
             return IccReaderNative.Sle4442_ReadProtectionMemory(fd, address, len);
         }
         return null;
     }
     /**
      * The write protection bit storage area.
      * Write protection on the 4442 card data (disposable, write protection will not be able to restore). 
      * Before the 32BYTE data only for SLE4442 write protect (corresponding to protect a storage area of 32 BIT). 
      * This function can be one of a plurality of consecutive bytes protection (up to 32 bytes). 
      * According to the write protection properties of SLE4442 card, 
      * to a byte write protection must provide the bytes of data at the same time sends the write protection command,
      * consistent data only provides the data and the actual store when really on the specified storage area is write protected.
     * @param addr The starting address of operation data, the range of the parameters of the SLE4442 card: 0---31.
     * @param data Write protect bit data
     * @return  Returns 0 if successful. Returns -1 if failed.
      */
     public int sle4442_writeProtectionMemory(int addr, byte[] data, int dataLen) 
     {
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.Sle4442_WriteProtectionMemory(fd, addr, data, dataLen);
         }
         return -1;
     }
     /**
      * Comparison of SLE4442 cards, each card after power compared to the card password, 
      * otherwise the card data is read-only, you will not be on the card of any write operation. 
      * Secure storage area (except the error counter outside) will not be able to read and write
      * 
     * @param passwd The data buffer pointer password, password here to store data migration and card in the password.
     * The first byte of 
     * pt corresponding to protected storage
     * (pt+1) corresponding to second byte protected storage
     * (pt+2) corresponding to third byte protected storage
     * @return Returns 0 if successful. Returns -1 if failed.
      */
     public int sle4442_verifyPassword(byte[] passwd) 
     {   
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.Sle4442_VerifyPassword(fd, passwd);
         }
         return -1;
     }

     /**
      * Change password.
      * @param  passwd The data buffer point to the new password.
      * @return        Returns 0 if successful. Returns -1 if failed.
      */
     public int sle4442_changePassword(byte[] passwd) 
     {   
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.Sle4442_ChangePassword(fd, passwd);
         }
         return -1;
     }

    /**
      * Change password.
      * @param  errorCount The data buffer point to error count, 1Byte.
      * @return        Returns 0 if successful. Returns -1 if failed.
      */
     public int sle4442_readErrorCounter(byte[] errorCount) 
     {   
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.Sle4442_ReadErrorCounter(fd, errorCount);
         }
         return -1;
     }
     // 暂时不导出
	public int sle4436_reset(byte[] pAtr) {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Sle4436_Reset(fd, pAtr);
        }
        return 0;
    }
    
    /**
     * Activate  IC4442 from the selected slot.
     * @param volt The operating voltage.  Could be 5V, 3V etc
     * @param atr_len for the byte array indicating the ATR length.
     * @return Returns byte array indicating the ATR if successful.  Returns null if failed.
      */
     /*public byte[] sle4442_activate(char volt,byte[] atr_len) 
     {   
         //byte[] ret = IccNative.Sle4442Act(volt,atr_len);
         //return ret;
         return null;
     }*/
     /**
      * Read the data stored in main memory for SLE4436
     * @param addr The starting address of operation data, the range of the parameters of the SLE4436 card: 0---112.
     * @param length To read the data length, the range of the parameters of the SLE4436 card: 1---112. 
     * In addition to ByteAddr and Length and cannot be greater than the actual capacity of the card, 
     * otherwise the reader will refuse to execute the command, and returns an error.
     * @return Returns byte array indicating the store data if successful. Returns null if failed.
      */
     public byte[] sle4436_readMemory(int addr,int length) 
     {   
         if(slot == 0) {
             if(fd == -1) return null;
             byte[] ret = IccReaderNative.Sle4436_ReadMemory(fd, addr, length);
             return ret;
         }
         return null;
     }
     /**
      * Write data to the main storage area SLE4436
     * @param addr The starting address of operation data, the range of the parameters of the SLE4436 card: 0---112.
     * @param data The data to be written
     * @param dataLen The data length
     * @return Returns 0 if successful. Returns -1 if failed.
      */
     public int sle4436_writeMemory(int addr,byte[] data, int dataLen) 
     {
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.Sle4436_WriteMemory(fd, addr, data, dataLen);
         }
         return -1;
     }   
     /**
      * Write Carry data
      * @param mode
      * @param addr The starting address of operation data
      * @param data The data to be written
      * @param dataLen
      * @return Returns 0 if successful. Returns -1 if failed.
      */
     public int sle4436_writeCarry(int mode, int addr, byte[] data, int dataLen) 
     {
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.Sle4436_WriteCarry(fd, mode, addr, data, dataLen);
         }
         return -1;
     }
     /**
      * Submit the transfer data, enter the personalization mode
      * 
     * @param passwd The data buffer pointer password, password here to store data migration and card in the password.
     * The first byte of 
     * pt corresponding to protected storage
     * (pt+1) corresponding to second byte protected storage
     * (pt+2) corresponding to third byte protected storage
     * @return Returns 0 if successful. Returns -1 if failed.
      */
     public int sle4436_verifyPassword(byte[] passwd) 
     {   
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.Sle4436_VerifyPassword(fd, passwd);
         }
         return -1;
     }
     /**
      *  Get the Card verification certificate
      * @param key
      * @param clkCnt Number of clock
      * @param challengeData
      * @return
      */
     public byte[] sle4436_authenticate(int key, byte[] clkCnt, byte[] challengeData) 
     {   
         if(slot == 0) {
             if(fd == -1) return null;
             byte[] ret = IccReaderNative.Sle4436_Authenticate(fd, key, clkCnt, challengeData);
			 return ret;
         }
         return null;
     }
     /**
      * Move the register
      * @param shiftBits move Bits
      * @return Returns 0 if successful. Returns -1 if failed.
      */
	public int sle4436_regIncrease(int shiftBits)
	{   if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Sle4436_RegIncrease(fd, shiftBits);
        }
        return -1;
    }
	/**
     * Read values from the Card bit 
     * @param pData
     * @return Returns 0 if successful. Returns -1 if failed.
     */
	public int sle4436_readBit(byte[] pData)
    {    if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Sle4436_ReadBit(fd, pData);
        }
        return -1;
    }
	/**
	 * Write a byte for Card
	 * @return Returns 0 if successful. Returns -1 if failed.
	 */
	public int sle4436_writeBit()
    {   
	    if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Sle4436_WriteBit(fd);
        }
        return -1;
    }
	/**
     * Reload Byte
     * @return Returns 0 if successful. Returns -1 if failed.
     */
	public int sle4436_reloadByte()
    {   
	    if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Sle4436_ReloadByte(fd);
        }
        return -1;
    }
	/**
     * Reduction balance
     * @param pValue The Amount of money to be eduction
     * @return Returns the card balance if successful. Returns -1 if failed.
     */
	public int sle4436_decValue(int pValue)
    {   
		if(slot == 0) {
            if(fd == -1) return -1;
            return IccReaderNative.Sle4436_DecValue(fd, pValue);
        }
        return -1;
    }

	// ****************** sle4428 ******************************//
	public int sle4428_reset(byte[] pAtr) {
        if(slot == 0) {
            if(fd == -1) return fd;
            return IccReaderNative.Sle4428_Reset(fd, pAtr);
        }
        return 0;
    }
    
     /**
      * Read the data stored in main memory for SLE4428
     * @param addr The starting address of operation data, the range of the parameters of the SLE4428 card: 0---0x3FF.
     * @param length To read the data length, the range of the parameters of the SLE4428 card: 1---0x400. 
     * In addition to ByteAddr and Length and cannot be greater than the actual capacity of the card, 
     * otherwise the reader will refuse to execute the command, and returns an error.
     * @return Returns byte array indicating the store data if successful. Returns null if failed.
      */
     public byte[] sle4428_readMemory(int addr,int length) 
     {   
         if(slot == 0) {
             if(fd == -1) return null;
             byte[] ret = IccReaderNative.Sle4428_ReadMemory(fd, addr, length);
             return ret;
         }
         return null;
     }
     /**
      * Write data to the main storage area SLE4428
     * @param addr The starting address of operation data, the range of the parameters of the SLE4428 card: 0---0x3FF.
     * @param data The data to be written
     * @param dataLen The data length, 0x400.
     * @return Returns 0 if successful. Returns -1 if failed.
      */
     public int sle4428_writeMemory(int addr,byte[] data, int dataLen) 
     {
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.Sle4428_WriteMemory(fd, addr, data, dataLen);
         }
         return -1;
     }

	  public int sle4428_password(int mode, byte[] data) 
     {
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.Sle4428_Password(fd, mode, data);
         }
         return -1;
     }

	// ****************** At88sc102 ******************************//
	/**
      * Read the data stored in main memory for At88sc102
     * @param addr The starting address of operation data, the range of the parameters of the At88sc102 card: 0---0xFF.
     * @param length To read the data length, the range of the parameters of the At88sc102 card: 1---0x100. 
     * In addition to ByteAddr and Length and cannot be greater than the actual capacity of the card, 
     * otherwise the reader will refuse to execute the command, and returns an error.
     * @return Returns byte array indicating the store data if successful. Returns null if failed.
      */
     public byte[] at88sc102_read(int addr,int length) 
     {   
         if(slot == 0) {
             if(fd == -1) return null;
             byte[] ret = IccReaderNative.At88sc102_Read(fd, addr, length);
             return ret;
         }
         return null;
     }
     /**
      * Write data to the main storage area At88sc102
     * @param addr The starting address of operation data, the range of the parameters of the At88sc102 card: 0---0xFF.
     * @param data The data to be written
     * @param dataLen The data length, 0x100.
     * @return Returns 0 if successful. Returns -1 if failed.
      */
     public int at88sc102_write(int addr,byte[] data, int dataLen) 
     {
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.At88sc102_Write(fd, addr, data, dataLen);
         }
         return -1;
     }

	/**
	  * Check Verify password for At88sc102.
	  * @param type type of password. 0: SC, 1: erase EZ1 password, 2: erase EZ2 password.
	  * @param data password to verify. If type is SC, the len should be 2Bytes. EZ1, the len should be 6Bytes. EZ2, the len should be 4Bytes.
      * @param length The data length, 0x100.
	  * @return Returns 0 if successful. -1 if failed.
	  */
	public int at88sc102_VerifyPassword(int type, byte[] data, int length) 
    {
         if(slot == 0) {
             if(fd == -1) return fd;
             return IccReaderNative.At88sc102_VerifyPassword(fd, type, data, length);
         }
         return -1;
     }
}
