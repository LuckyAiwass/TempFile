package android.device;

import android.util.Log;
/**
 * 模块：    POS非接触式卡<br>
 * 版本号:   v2.2.3<br>
 * 更新时间: 2015/10/16<br>
 *           v2.2.3 2015/10/16 增加纯CPU卡(如Desfire Card)发送apdu指令的接口： apduTransmit(byte[] sent, int sentlen, byte[] rsp);<br>
 * 特别说明：<br>
 * 本类仅用于POS类设备。<br>
 */
public class PiccManager {
    private static final String TAG = "PiccReader";
    /**
     * 打开 PICC 模块。执行此命令后,PICC 模块开始正常工作.
     * @return RspCode.RSPOK 打开PICC模块成功, RspCode.RSPERR 打开PICC模块失败
     */
    public int open(){
        Log.i(TAG, "Open");
        return PiccReaderNative.picc_open();
    }
    /**
     * 使能RATS操作，
     * @return RspCode.RSPOK 设置成功, RspCode.RSPERR 设置失败
     */
       public int enableRATS(byte value){
               byte[] config= new byte[16];
               Log.i(TAG, "config");
               config[2]=value;
               return PiccReaderNative.picc_config(config);
       }
    
    /*public int init() {
        return PiccReaderNative.picc_init();
    }*/
    /**
     * 检测PICC卡片
     * @param mode 返回的卡类 A’ : A 卡,’B’ : B 卡
     * @param atq 成功则返回 ATQ
     * @return ATQ 长度, RspCode.RSPERR 未检测到 PICC
     */
    public int request(byte[] mode, byte[] atq){
        Log.i(TAG, "Request");
        return PiccReaderNative.picc_request(mode, atq);
    }

    /**
     * 检测PICC卡片,去掉rats步骤
     * @param mode 返回的卡类 A’ : A 卡,’B’ : B 卡
     * @param atq 成功则返回 ATQ
     * @return ATQ 长度, RspCode.RSPERR 未检测到 PICC
     */
    public int request_norats(byte[] mode, byte[] atq){
        Log.i(TAG, "Request");
        return PiccReaderNative.picc_request_norats(mode, atq);
    }

    /**
     * 检测指定类型PICC卡片
	 * @param pollType 指定卡片类型(bit0 A卡,bit1 B卡,bit2 Felica-212卡,bit3 Felica-424卡,bit4 15693卡,bit5 18000卡)
     * @param mode 返回的卡类 A’ : A 卡,’B’ : B 卡
     * @param atq 成功则返回 ATQ
     * @return ATQ 长度, RspCode.RSPERR 未检测到 PICC
     */
    public int request_type(byte pollType,byte[] mode, byte[] atq){
        Log.i(TAG, "Request type");
        return PiccReaderNative.picc_request_type(pollType,mode, atq);
    }
    /**
     * 选择卡,获取检测到的 PICC 卡片的序列号。
     * @param sn 卡片的序列号,bcd 编码
     * @param sak 卡片的SAK
     * @return 序列号的长度，RspCode.RSPERR 选择失败
     */
    public int antisel(byte[] sn, byte[] sak){
        Log.i(TAG, "Antisel");
        return PiccReaderNative.picc_Antisel(sn, sak);
    }
    
    /**
     * 激活 TYPEA/B 类型的 CPU 卡,开始 APDU 命令的传输。(M1 卡不用此步骤,适用于 CPU 卡)
     * @return RspCode.RSPOK 激活成功，RspCode.RSPERR 激活失败
     */
    public int reset(){
        Log.i(TAG, "Active");
        return PiccReaderNative.picc_Active();
    }
    public int activate(){
        Log.i(TAG, "Active");
        return PiccReaderNative.picc_Active();
    }
    public int activateEx(byte[] art){
        Log.i(TAG, "Active");
        return PiccReaderNative.picc_ActiveEx(art);
    }
    /**
     * 传输APDU指令
     * @param cmd 需要被发送到卡片的 apdu 指令的 bcd 码,命令格式符合 7816规范
     * @param cmdlen Apdu指令长度
     * @param rsp 卡片返回的响应数据，bcd码
     * @param sw 卡片执行apdu命令的状态字。2字节bcd码
     * @return 卡片返回的响应数据的长度,不包括 2 字节的 SW；RspCode.RSPERR命令执行错误
     */
    public int apduTransmit(byte[] cmd, int cmdlen, byte[] rsp, byte[] sw){
        Log.i(TAG, "ApduTransmit");
        int[] rspLen = new int[1];
        int ret = PiccReaderNative.picc_apdu(cmd, cmdlen, rsp, rspLen);
        if(ret < 0 || sw.length < 2) return RspCode.RSPERR;
        int swLen = rspLen[0] - 2;
        if(swLen  >= 0) {
            System.arraycopy(rsp, swLen, sw, 0, 2);
            return swLen;
        }
        return ret;
    }

	    /**
     * 传输APDU指令到Felica卡
     * @param cmd 需要被发送到卡片的 apdu 指令的 bcd 码,命令格式符合 7816规范
     * @param cmdlen Apdu指令长度
     * @param rsp 卡片返回的响应数据，bcd码
     * @return Felica卡片返回的响应数据的长度,RspCode.RSPERR命令执行错误
     */
    public int apduTransmitFelica(byte[] cmd, int cmdlen, byte[] rsp){
        Log.i(TAG, "ApduTransmit for Felica");
        int[] rspLen = new int[1];
        int ret = PiccReaderNative.picc_apduFelica(cmd, cmdlen, rsp, rspLen);
        if(ret != 0) return RspCode.RSPERR;

        return rspLen[0];
    }
    public void  SetTimeOutFelica(int timeout){
	Log.i(TAG, "SetTimeOutFelica");
	PiccReaderNative.picc_setTimeOutFelica(timeout);
    }

    /**
     * 传输exchange数据交换
     * @param sent 需要被发送到卡片的 data数据
     * @param sentlen Apdu指令长度
     * @param rsp 卡片返回的响应数据，
     * @return 卡片返回的响应数据的长度；RspCode.RSPERR命令执行错误
     */
    public int apduTransmit(byte[] sent, int sentlen, byte[] rsp){
        Log.i(TAG, "exchange");
        int[] rspLen = new int[1];
        int ret = PiccReaderNative.picc_exchange(sent, sentlen, rsp, rspLen);
        if(ret < 0 || rspLen[0] < 0) return RspCode.RSPERR;

        return rspLen[0];
    }
    
    /**
     * 传输Mifare SL-3交互命令(数据自带PCB，可设置CRC，传输速率)
     * @param cmd 需要被发送到卡片的指令的 bcd 码，不符合 7816 规范
     * @param cmdlen 指令长度
     * @param rsp 卡片返回的响应数据，bcd码
     * @param crc 数据是否进行CRC校验, 0 不校验，1 仅发送指令校验，2 仅响应数据校验，3发送指令和响应数据都校验
     * @param speed 传输速率, 0 默认速率，121 121kbps ,242 242kbps, 848 848kbps,其他值无效
     * @return 0 指令执行成功，其他执行出错
     */
    public int apduTransmit(byte[] cmd, int cmdlen, byte[] rsp, int crc,int speed){
        Log.i(TAG, "Exchange_EX");
        int[] rspLen = new int[1];
        int ret = PiccReaderNative.picc_exchangeEX(cmd, cmdlen, rsp, rspLen,crc,speed);
        if(ret != 0||rspLen[0]<0) {
            Log.i(TAG, "rsp error in exchange_EX");
            return ret;
        }
        return rspLen[0];
    }
    /**
     * 移除已连接的卡片
     * @param mode 0x00 为 HALT,仅向卡片发送停活指令后就退出;
     * 该过程不执行卡移开检测 0x01 为 REMOVE,向卡片发送停活指令,并执行卡移开检测;
     * 0x02 为符合 EMV 非接规范的移卡模式复位载波,并执行卡移开检测。
     * @return RspCode.RSPOK 移除卡片成功，RspCode.RSPERR 移除卡片失败
     */
    public int remove(byte mode){
        Log.i(TAG, "Remove");
        if(mode ==0 ) {
            mode = (byte)'h';
        } else if(mode == 1) {
            mode = (byte)'r';
        } else if(mode == 2) {
            mode = (byte)'e';
        }
        return PiccReaderNative.picc_remove(mode);
    }
    public int deactivate(byte mode){
        Log.i(TAG, "Remove");
        if(mode ==0 ) {
            mode = (byte)'h';
        } else if(mode == 1) {
            mode = (byte)'r';
        } else if(mode == 2) {
            mode = (byte)'e';
        }
        return PiccReaderNative.picc_remove(mode);
    }    
    /**
     * 关闭PICC模块
     * @return RspCode.RSPOK 关闭PICC模块成功，RspCode.RSPERR 关闭PICC模块失败
     */
    public int close(){
        Log.i(TAG, "Close");
        return PiccReaderNative.picc_close();
    }
    
    /**
     * M1卡密钥认证
     * @param keyType 密钥类型:0 : A 密码; 1 : B 密码
     * @param blnNo 需要读取的块号
     * @param keylen 密钥长度
     * @param keyBuf 密钥数据
     * @param iSeriNumlen 卡片序列号长度
     * @param seriNum 卡片序列号
     * @return RspCode.RSPOK 密钥认证成功，RspCode.RSPERR 密钥认证失败
     */
    public int m1_keyAuth(int keyType, int blnNo, int keylen, byte[] keyBuf, int iSeriNumlen, byte[] seriNum){
        Log.i(TAG, "M1_KeyAuth");
        return PiccReaderNative.picc_M1Auth(keyType, blnNo, keylen, keyBuf, iSeriNumlen, seriNum);
    }   
    
    /**
     * 读取M1卡块数据
     * @param blkNo 需要读取的块号
     * @param pReadBuf 读出的数据buf
     * @return 读出的数据长度；RspCode.RSPERR 读取数据失败
     */
    public int m1_readBlock(int blkNo, byte[] pReadBuf){
        Log.i(TAG, "M1_ReadBlock");
        return PiccReaderNative.picc_M1Read(blkNo, pReadBuf);
    }
    
    /**
     * 写M1卡快数据
     * @param blkNo 操作的块号
     * @param iLenWriteBuf 将写入数据的长度
     * @param pWriteBuf 写入的数据
     * @return RspCode.RSPOK 写数据成功，RspCode.RSPERR 写数据失败
     */
    public int m1_writeBlock(int blkNo, int iLenWriteBuf, byte[] pWriteBuf){
        Log.i(TAG, "M1_WriteBlock");
        return PiccReaderNative.picc_M1Write(blkNo, iLenWriteBuf, pWriteBuf);
    }
    
    /**
     * 对 M1 卡的指定钱包块加值操作,并自动将结果保存在卡内部临时数据寄存器
     * @param blkNo 操作的数据块
     * @param iMoney  金额
     * @return RspCode.RSPOK 数据操作成功，RspCode.RSPERR 数据操作失败
     */
    public int m1_increment(int blkNo, int iMoney){
        Log.i(TAG, "M1_Increment");
        return PiccReaderNative.picc_M1Increment(blkNo, iMoney);
    }
    
    /**
     * 对 M1 卡的指定钱包块减值操作,并自动将结果保存在卡内部临时数据寄存器
     * @param blkNO 操作的数据快
     * @param iMoney 金额
     * @return RspCode.RSPOK 数据操作成功，RspCode.RSPERR 数据操作失败
     */
    public int m1_decrement(int blkNO, int iMoney){
        Log.i(TAG, "M1_Decrement");
        return PiccReaderNative.picc_M1Decrement(blkNO, iMoney);
    }
    
    /**
     * 将钱包块的内容移入内部数据寄存器
     * @param blkNo 操作的数据块
     * @return RspCode.RSPOK 数据操作成功，RspCode.RSPERR 数据操作失败
     */
    public int m1_restore(int blkNo){
        Log.i(TAG, "M1_Restore");
        return PiccReaderNative.picc_M1Restore(blkNo);
    } 
    
    /**
     * 将内部数据寄存器的值移入钱包块
     * @param blkNo 操作的数据块
     * @return RspCode.RSPOK 数据操作成功，RspCode.RSPERR 数据操作失败
     */
    public int m1_transfer(int blkNo){
        Log.i(TAG, "M1_Transfer");
        return PiccReaderNative.picc_M1Transfer(blkNo);
    }
    
    public int m1_init(int blkNo,int value)
    {
        return PiccReaderNative.picc_M1Init(blkNo,value);
    }

    public int m1_amount(int blkNo)
    {
        return PiccReaderNative.picc_M1Revalue(blkNo);
    }
		//TYPE B for id card
	public int idcard_init(){
		return PiccReaderNative.picc_initB();

	}
    public  int idcard_apduTransmit(byte[] sent, int sentlen, byte[] rsp){
		Log.i(TAG, "idcard_apduTransmit");
		int[] rspLen = new int[1];
		int ret = PiccReaderNative.picc_exchangeB(sent, sentlen, rsp, rspLen);
		if(ret < 0 || rspLen[0] < 0) return RspCode.RSPERR;

		return rspLen[0];
	}
    public  int id_apduTransmit(byte[] sent, int sentlen, byte[] rsp){
        Log.i(TAG, "id_apduTransmit");
        int[] rspLen = new int[1];
        int ret = PiccReaderNative.picc_exchangeB(sent, sentlen, rsp, rspLen);
        if(ret < 0 || rspLen[0] < 0) return RspCode.RSPERR;

        return rspLen[0];
    }
	
	//TYPE B prime protocol
	public int primeCard_init(){
	           byte[] config= new byte[2];
             	  Log.i(TAG, "primeCard_init");
             	  config[0]=(byte)0x1;
		  config[1]=(byte)0x80;
               	 PiccReaderNative.picc_config(config);
		return PiccReaderNative.picc_initB();
	}
	
    public  int primeCard_apduTransmit(byte[] sent, int sentlen, byte[] rsp){
		Log.i(TAG, "primeCard_apduTransmit");
		int[] rspLen = new int[1];
		int ret = PiccReaderNative.picc_exchangeB(sent, sentlen, rsp, rspLen);
		if(ret < 0 || rspLen[0] < 0) return RspCode.RSPERR;

		return rspLen[0];
	}

    /* Type F */
    /**
     * 对F卡进行APDU操作；
     * @param sent 需要被发送到卡片的 data数据
     * @param sentlen Apdu指令长度
     * @param rsp 卡片返回的响应数据，
     * @return 卡片返回的响应数据的长度；RspCode.RSPERR命令执行错误
     */
    public int apduTransmit(int cmd, int num, byte[] sent, int sentlen, byte[] rsp){
        Log.i(TAG, "exchange");
        int[] rspLen = new int[1];
        int ret = PiccReaderNative.picc_exchangeF(cmd, num, sent, sentlen, rsp, rspLen);
        if(ret < 0 || rspLen[0] < 0) return RspCode.RSPERR;

        return rspLen[0];
    }

    /* Type Mifare UltraLight */
    /**
     * 对Mifare UltraLight 卡进行Read操作；
     * @param sectorNum 要读取的sector号
     * @param rsp 命令执行成功时返回4字节响应数据
     * @return 成功返回0，其他错误
     */
    public int mifareUlPageRead(int sectorNum, byte[] rsp){
        Log.i(TAG, "mifareUlPageRead");
        return PiccReaderNative.picc_MifareUlPageRead(sectorNum, rsp); 
    }

    /**
     * 对Mifare UltraLight 卡进行Write操作；
     * @param sectorNum 要写入的sector号
     * @param sent 要写入的4Byte数据
     * @return 成功返回0，其他错误
     */
    public int mifareUlPageWrite(int sectorNum, byte[] sent){
        Log.i(TAG, "mifareUlPageWrite");
        return PiccReaderNative.picc_MifareUlPageWrite(sectorNum, sent); 
    }

    /**
     * 获取Mifare UltraLight卡计数值操作；
     * @param bCntNum 计数器编号0-2
     * @param pCntValue 读取出来的计数值
     * @return 成功返回0，其他错误
     */
    public int mifareUlReadCnt(byte bCntNum, int[] pCntValue){
        return PiccReaderNative.picc_MifareUlReadCnt(bCntNum, pCntValue);
    }

    /**
     * Mifare UltraLight卡计数值自增操作；
     * @param bCntNum 计数器编号0-2
     * @param iCntValue 要写入的计数值;
     * @return 成功返回0，其他错误
     */
    public int mifareUlIncrCnt(byte bCntNum, int iCntValue){
        return PiccReaderNative.picc_MifareUlIncrCnt(bCntNum, iCntValue);
    }

    /**
     * 对卡片发送透传指令；
     * @param bProtocol 协议类型，0:14443A 1:14443B
     * @param pSendBuf 传输的数据
     * @param bSendLen 传输数据的长度
     * @param pRecvBuf 接收的数据
     * @param pRecvLen 接收的数据长度
     * @return 成功返回0，其他错误
     */
    public int transactionData(byte bProtocol, byte[] pSendBuf, int bSendLen, byte[] pRecvBuf, int[] pRecvLen){
        return PiccReaderNative.picc_TransactionData(bProtocol, pSendBuf, bSendLen, pRecvBuf, pRecvLen);
    }

// SRT512
	/**
     * SRT512 获取已取得的所有卡片的Chip ID
     * @param pValue 返回的Chip ID
     * @return 成功返回接收到的Chip ID长度。Chip ID长度为1个Byte，返回值亦可以表示已经寻得的卡片数量，支持最多返回4张卡片Chip ID。负值表示错误。
     */
    public int srt512ChipIDGet(byte pValue[]){
        return PiccReaderNative.picc_Srt512ChipIDGet(pValue);
    }
	
	/**
     * SRT512 获取卡片的UID，UID长度为8字节
     * @param bChipID 获取指定Chip ID的UID
     * @param pValue 返回的8字节UID
     * @return 成功返回接收到的UID长度。负值表示错误。
     */
    public int srt512UIDGet(byte bChipID, byte pValue[]){
        return PiccReaderNative.picc_Srt512UIDGet(bChipID, pValue);
    }

	/**
     * SRT512 使指定卡片进入select状态
     * @param bChipID 需要选择的卡片的Chip ID
     * @return 成功返回0。其他表示错误。
     */
    public int srt512Select(byte bChipID){
        return PiccReaderNative.picc_Srt512Select(bChipID);
    }

	/**
     * SRT512 对处于select状态的卡片读取指定地址的数据
     * @param bAddr 地址，有效值为0～15, 以及255
     * @param pValue 返回的4字节数据
     * @return 成功返回接收到的数据长度。负值表示错误。
     */
    public int srt512BlockRead(byte bAddr, byte pValue[]){
        return PiccReaderNative.picc_Srt512BlockRead(bAddr, pValue);
    }

	/**
     * SRT512 向处于select状态的卡片的指定地址写入数据
     * @param bAddr 地址，有效值为0～15, 以及255
     * @param pValue 要写入的4字节数据
     * @return 成功返回0。其他表示错误。
     */
    public int srt512BlockWrite(byte bAddr, byte pValue[], int iValueLen){
        return PiccReaderNative.picc_Srt512BlockWrite(bAddr, pValue, iValueLen);
    }

	/**
     * SRT512 向处于select状态的卡片发送Completion命令
     * @return 成功返回0。其他表示错误。
     */
    public int srt512Completion(){
        return PiccReaderNative.picc_Srt512Completion();
    }

	/**
     * SRT512 向处于select状态的卡片发送Reset to Inventory命令
     * @return 成功返回0。其他表示错误。
     */
    public int srt512ResettoInventory(){
        return PiccReaderNative.picc_Srt512Rst2Inventory();
    }
}
