package android.device;

public class RspCode {
    /**命令执行成功*/
    public static final int RSPOK = 0;
    /**命令执行失败*/
    public static final int RSPERR = -1;
    /**PEDAPI 错误码起始值*/
    public static final int PED_RET_ERR_START =( -300);
    /**密钥不存在*/
    public static final int PED_RET_ERR_NO_KEY =(PED_RET_ERR_START-1);
    /**密钥索引错,参数索引不在范围内*/
    public static final int PED_RET_ERR_KEYIDX_ERR =(PED_RET_ERR_START-2);
    /**密钥写入时,源密钥类型错或层次比目的密钥低*/
    public static final int PED_RET_ERR_DERIVE_ERR =(PED_RET_ERR_START-3);
    /**密钥验证失败*/
    public static final int PED_RET_ERR_CHECK_KEY_FAIL =(PED_RET_ERR_START-4);
    /**没输入 PIN*/
    public static final int PED_RET_ERR_NO_PIN_INPUT =(PED_RET_ERR_START-5);
    /**用户取消输入 PIN*/
    public static final int PED_RET_ERR_INPUT_CANCEL =(PED_RET_ERR_START-6);
    /**函数调用小于最小间隔时间*/
    public static final int PED_RET_ERR_WAIT_INTERVAL =(PED_RET_ERR_START-7);
    /**KCV 模式错,不支持*/
    public static final int PED_RET_ERR_CHECK_MODE_ERR =(PED_RET_ERR_START-8);
    /**无权使用该密钥,PED 当前密钥标签值和要使用的密钥标签值不相等*/
    public static final int PED_RET_ERR_NO_RIGHT_USE =(PED_RET_ERR_START-9);
    /**密钥类型错*/
    public static final int PED_RET_ERR_KEY_TYPE_ERR =(PED_RET_ERR_START-10);
    /**期望 PIN 的长度字符串错*/
    public static final int PED_RET_ERR_EXPLEN_ERR =(PED_RET_ERR_START-11);
    /**目的密钥索引错,不在范围内*/
    public static final int PED_RET_ERR_DSTKEY_IDX_ERR =(PED_RET_ERR_START-12);
    /**源密钥索引错,不在范围内或者写入密钥时,源密钥类型的值大于目的密钥类型,都会返回该密钥*/
    public static final int PED_RET_ERR_SRCKEY_IDX_ERR =(PED_RET_ERR_START-13);
    /**密钥长度错*/
    public static final int PED_RET_ERR_KEY_LEN_ERR =(PED_RET_ERR_START-14);
    /**输入 PIN 超时*/
    public static final int PED_RET_ERR_INPUT_TIMEOUT =(PED_RET_ERR_START-15);
    /**IC 卡不存在*/
    public static final int PED_RET_ERR_NO_ICC =(PED_RET_ERR_START-16);
    /**IC 卡未初始化*/
    public static final int PED_RET_ERR_ICC_NO_INIT =(PED_RET_ERR_START-17);
    /**DUKPT 组索引号错*/
    public static final int PED_RET_ERR_GROUP_IDX_ERR =(PED_RET_ERR_START-18);
    /**指针参数非法为空*/
    public static final int PED_RET_ERR_PARAM_PTR_NULL =(PED_RET_ERR_START-19);
    /**PED 已受攻击*/
    public static final int PED_RET_ERR_TAMPERED =(PED_RET_ERR_START-20);
    /**PED 通用错误*/
    public static final int PED_RET_ERROR =(PED_RET_ERR_START-21);
    /**没有空闲的缓冲*/
    public static final int PED_RET_ERR_NOMORE_BUF =(PED_RET_ERR_START-22);
    /**需要取得高级权限*/
    public static final int PED_RET_ERR_NEED_ADMIN =(PED_RET_ERR_START-23);
    /**DUKPT 已经溢出*/
    public static final int PED_RET_ERR_DUKPT_OVERFLOW =(PED_RET_ERR_START-24);
    /**KCV 校验失败*/
    public static final int PED_RET_ERR_KCV_CHECK_FAIL =(PED_RET_ERR_START-25);
    /**写入密钥时,源密钥 id 的密钥类型和源密钥类型不匹配*/
    public static final int PED_RET_ERR_SRCKEY_TYPE_ERR =(PED_RET_ERR_START-26);
    /**命令不支持*/
    public static final int PED_RET_ERR_UNSPT_CMD =(PED_RET_ERR_START-27);
    /**通讯错误*/
    public static final int PED_RET_ERR_COMM_ERR =(PED_RET_ERR_START-28);
    /**没有用户认证公钥*/
    public static final int PED_RET_ERR_NO_UAPUK =(PED_RET_ERR_START-29);
    /**取系统敏感服务失败*/
    public static final int PED_RET_ERR_ADMIN_ERR =(PED_RET_ERR_START-30);
    /**PED 处于下载非激活状态*/
    public static final int PED_RET_ERR_DOWNLOAD_INACTIVE =(PED_RET_ERR_START-31);
    /**KCV 奇校验失败*/
    public static final int PED_RET_ERR_KCV_ODD_CHECK_FAIL =(PED_RET_ERR_START-32);
    /**读取 PED 数据失败*/
    public static final int PED_RET_ERR_PED_DATA_RW_FAIL =(PED_RET_ERR_START-33);
    /**IC 卡操作错误=(脱机明文、密文密码验证);*/
    public static final int PED_RET_ERR_ICC_CMD_ERR =(PED_RET_ERR_START-34);
    /**写入的密钥全零或者,有组分相等,16/24 字节密钥存在两个组分相等的情况*/
    public static final int PED_RET_ERR_KEY_VALUE_INVALID =(PED_RET_ERR_START-35);
    /**已存在相同的密钥值的密钥*/
    public static final int PED_RET_ERR_KEY_VALUE_EXIST =(PED_RET_ERR_START-36);
    /** 串口参数不支持*/
    public static final int PED_RET_ERR_UART_PARAM_INVALID =(PED_RET_ERR_START-37);
    /**密钥索引没有选择或者和选择的密钥索引不相等*/
    public static final int PED_RET_ERR_KEY_INDEX_NOT_SELECT_OR_NOT_MATCH =(PED_RET_ERR_START-38);
    /**用户按 CLEAR 键退出输入 PIN*/
    public static final int PED_RET_ERR_INPUT_CLEAR =(PED_RET_ERR_START-39);
    /***/
    public static final int PED_RET_ERR_LOAD_TRK_FAIL =(PED_RET_ERR_START-40);
    /***/
    public static final int PED_RET_ERR_TRK_VERIFY_FAIL =(PED_RET_ERR_START-41);
    public static final int PED_RET_ERR_MSR_STATUS_INVALID =(PED_RET_ERR_START-42);
    public static final int PED_RET_ERR_NO_FREE_FLASH =(PED_RET_ERR_START-43);
    
    /**DUKPT KSN 需要先加 1*/
    public static final int PED_RET_ERR_DUKPT_NEED_INC_KSN =(PED_RET_ERR_START-44);
    /**KCV MODE 错误*/
    public static final int PED_RET_ERR_KCV_MODE_ERR =(PED_RET_ERR_START-45);
    /**NO KCV*/
    public static final int PED_RET_ERR_DUKPT_NO_KCV =(PED_RET_ERR_START-46);
    /**按 FN/ATM4 键取消 PIN 输入*/
    public static final int PED_RET_ERR_PIN_BYPASS_BYFUNKEY = (PED_RET_ERR_START-47);
    /**数据 MAC 校验错*/
    public static final int PED_RET_ERR_MAC_ERR =(PED_RET_ERR_START-48);
    /**数据 CRC 校验错*/
    public static final int PED_RET_ERR_CRC_ERR = (PED_RET_ERR_START-49);

    /**-352 密码错误*/
    public static final int PED_RET_ERR_PWD_ERR =(PED_RET_ERR_START-52);
    /**-353 密码需要重新设置*/
    public static final int PED_RET_ERR_PWD_NEEDRESET =(PED_RET_ERR_START-53);
    /**-354 密码错误次数超出*/
    public static final int PED_RET_ERR_PWD_ENTERTIMEOUT =(PED_RET_ERR_START-54);

    public static final int ERR_BASE =(-60000);
    /**
     * 串口发送失败
     */
    public static final int ERR_UARTSEND =(ERR_BASE-1);//
    /**串口接收 06 超时*/
    public static final int ERR_UARTSENDTIMEOUT =(ERR_BASE-2);//
    /**串口接收命令响应超时*/
    public static final int ERR_UARTRECVRSPTIMEOUT =(ERR_BASE-3);//
    /**响应 LRC 错误*/
    public static final int ERR_UARTRSPLRC=(ERR_BASE-4);//
    /**命令 LRC 错误*/
    public static final int ERR_UARTCMDLRC =(ERR_BASE-5);//
    /**命令格式错误 ETX!=0X03*/
    public static final int ERR_UARTRSPDATAFMT=(ERR_BASE-6);//
    /**用户中断接收*/
    public static final int ERR_USERINTERRUPTRECV=(ERR_BASE-7);//
    /**内存分配失败*/
    public static final int ERR_MEMFAILED=(ERR_BASE-8);//
    /**不是 BMP 图片*/
    public static final int ERR_BMPBMERR=(ERR_BASE-9);//
    /**宽大于 384 个像素点*/
    public static final int ERR_BMPWIDTHERR=(ERR_BASE-10);//
    /**不是单色位图*/
    public static final int ERR_BMPCLORERR =(ERR_BASE-11);//
    /**文件打开失败*/
    public static final int ERR_FILEOPENERR=(ERR_BASE-12);//
    /**参数错误*/
    public static final int ERR_PARA =(ERR_BASE-13);//
    /**文件不存在*/
    public static final int ERR_FILENOTEXIST =(ERR_BASE-14);//
    /**电压模式错误*/
    public static final int SC_VCCERR =(-2100);
    /**卡通道错误*/
    public static final int SC_SLOTERR =(-2101);
    /**奇偶错误*/
    public static final int SC_PARERR =(-2102);
    /**参数值为空*/
    public static final int SC_PARAERR =(-2103);
    /**协议错误*/
    public static final int SC_PROTOCALERR =(-2104);
    public static final int SC_DATALENERR =(-2105);
    /**卡拨出*/
    public static final int SC_CARDOUT =(-2106); //
    /**没有初始化*/
    public static final int SC_NORESET =(-2107); //
    /**卡通讯超时*/
    public static final int SC_TIMEOUT =(-2108); //
    public static final int SC_PPSERR =(-2109); //
    public static final int SC_ATRERR =(-2110);
    /**卡通讯失败*/
    public static final int SC_APDUERR =(-2111);
    
    public static final int RET_RF_START = (-3000);
    /**参数错误 */
    public static final int RET_RF_ERR_PARAM=(RET_RF_START-1);
    /**射频模块未开启*/
    public static final int RET_RF_ERR_NO_OPEN=(RET_RF_START-2);
    /**卡片未激活*/
    public static final int RET_RF_ERR_NOT_ACT =(RET_RF_START-3);
    /**多卡片冲突*/
    public static final int RET_RF_ERR_MULTI_CARD =(RET_RF_START-4);
    /**超时无响应*/
    public static final int RET_RF_ERR_TIMEOUT =(RET_RF_START-5);
    /**协议错误*/
    public static final int RET_RF_ERR_PROTOCOL =(RET_RF_START-6);
    /**通信传输错误*/
    public static final int RET_RF_ERR_TRANSMIT=(RET_RF_START-7);
    /**M1 卡认证失败*/
    public static final int RET_RF_ERR_AUTH=(RET_RF_START-8);
    /**扇区未认证*/
    public static final int RET_RF_ERR_NO_AUTH=(RET_RF_START-9);
    /**数值块数据格式有误,或 DesFire 卡片操作中文件大小错误*/
    public static final int RET_RF_ERR_VAL=(RET_RF_START-10);
    /**卡片仍在感应区内*/
    public static final int RET_RF_ERR_CARD_EXIST=(RET_RF_START-11);
    /**卡片状态错误=(如 A/B 卡调用 M1 卡接口, 或 M1 卡调用 PiccIsoCommand 接口);*/
    public static final int RET_RF_ERR_STATUS=(RET_RF_START-12);
    public static final int RET_RF_ERR_OVERFLOW =(RET_RF_START-13);
    /**DesFire 卡片的应答数据错误*/
    public static final int RET_RF_ERR_FAILED =(RET_RF_START-14);
    public static final int RET_RF_ERR_COLLERR =(RET_RF_START-15);
    /**DesFire 卡片操作中应用缓冲区空间不足*/
    public static final int RET_RF_ERR_FIFO =(RET_RF_START-16);
    public static final int RET_RF_ERR_CRC=(RET_RF_START-17);
    public static final int RET_RF_ERR_FRAMING=(RET_RF_START-18);
    public static final int RET_RF_ERR_PARITY=(RET_RF_START-19);
    public static final int RET_RF_ERR_DES_VAL =(RET_RF_START-20);
    /**操作不允许, 例如当前所选文件不是记录文件时,不能执行读记录操作*/
    public static final int RET_RF_ERR_NOT_ALLOWED =(RET_RF_START-21);
    /**接口芯片不存在或异常*/
    public static final int RET_RF_ERR_CHIP_ABNORMAL =(RET_RF_START-100);
    
    public static final int RET_RF_DET_START =(RET_RF_START-200);
    public static final int RET_RF_DET_ERR_INVALID_PARAM =(RET_RF_DET_START-1);
    public static final int RET_RF_DET_ERR_NO_POWER =(RET_RF_DET_START-2);
    public static final int RET_RF_DET_ERR_NO_CARD =(RET_RF_DET_START-3);
    public static final int RET_RF_DET_ERR_COLL =(RET_RF_DET_START-4);
    public static final int RET_RF_DET_ERR_ACT =(RET_RF_DET_START-5);
    public static final int RET_RF_DET_ERR_PROTOCOL =(RET_RF_DET_START-6);
    public static final int RET_RF_CMD_START =(RET_RF_START-300);
    public static final int RET_RF_CMD_ERR_INVALID_PARAM =(RET_RF_CMD_START-1);
    public static final int RET_RF_CMD_ERR_NO_POWER =(RET_RF_CMD_START-2);
    public static final int RET_RF_CMD_ERR_NO_CARD =(RET_RF_CMD_START-3);
    public static final int RET_RF_CMD_ERR_TX =(RET_RF_CMD_START-4);
    public static final int RET_RF_CMD_ERR_PROTOCOL =(RET_RF_CMD_START-5);
    public static final int KEY_OK = 60000;
    public static final int KEY_CANCEL = KEY_OK+1;
    public static final int KEY_OKDIRECT = KEY_OK+2;
    public static final int KEY_NOICCARD = KEY_OK+3;
    
}
