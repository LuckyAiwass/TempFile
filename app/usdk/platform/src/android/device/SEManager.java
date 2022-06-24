/*
 * Copyright (C) 2015, The Urovo Co.,Ltd.
 */

package android.device;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.os.IMaxqEncryptService;
import android.os.IInputActionListener;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.device.MaxNative;
import android.util.Log;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * 模块：    POS安全加密芯片<br>
 * 版本号:   v2.2.2<br>
 * 更新时间: 2015/10/08<br>
 * v2.2.2 2015/10/08 增加控制安全加密芯片休眠接口： enableSuspend(...)是否可进入休眠；setSuspendTimeout(.....)设置多久时间后进入休眠;
 * 特别说明：<br>
 * 本类仅用于POS类设备。<br>
 * 除了输密操作是异步接口，其它是同步接口，请勿block住调用同步接口的APP调用层。<br>
 */
public class SEManager {
    private static final String TAG = "SEManager";

    public static final int ALGORITHM_ECB = 0x01;

    public static final int ALGORITHM_CBC = 0x02;

    /** return error code defined */
    /**
     * command execute Success
     */
    public static final int S_OK = 0x00;

    /**
     * unsupported command
     */
    public static final int ENOTSUP_CMD = 0x01;

    /**
     * command length error
     */
    public static final int ELENGTH_CMD = 0x02;

    /**
     * command separator error
     */
    public static final int ESEPARATOR_CMD = 0x03;

    /**
     * command separator length error
     */
    public static final int ESEPARATORLEN_CMD = 0x04;

    /**
     * command head (CB) error
     */
    public static final int EHEAD_CMD = 0x05;

    /**
     * data crc16 error
     */
    public static final int ECRC16_DATA = 0x06;

    /**
     * message mac error
     */
    public static final int EMSG_MAC = 0x07;

    /**
     * message format error
     */
    public static final int EMSG_FORMAT = 0x09;

    /**
     * unsupported algorithm
     */
    public static final int ENOTSUP_ALGORITHM = 0x0C;

    /**
     * unsupported format
     */
    public static final int ENOTSUP_FORMAT = 0x0D;

    /**
     * unsupported mode
     */
    public static final int ENOTSUP_MODE = 0x0E;

    /**
     * keys not ready or init
     */
    public static final int EKEYS_RDORINIT = 0x12;

    /**
     * keys number not found
     */
    public static final int EKEYSNO_NOTFOUND = 0x14;

    /**
     * keys number out of range
     */
    public static final int EKEYSNO_OVERRANGE = 0x15;

    /**
     * keys download
     */
    public static final int EKEYS_DOWNLOAD = 0x16;

    /**
     * keys not download
     */
    public static final int EKEYS_NOTDOWNLOAD = 0x17;

    /**
     * keys out of space
     */
    public static final int EKEYS_OUTOFSPACE = 0x18;

    /**
     * keys exists or cant't be overwrite
     */
    public static final int EKEYS_EXISTS = 0x19;

    /**
     * keys encrypt keys not download
     */
    public static final int EENCRYPTKEYS_NOTDOWLOAD = 0x1A;

    /**
     * keys unsupported specified use
     */
    public static final int ENOTSUP_SEPUSE = 0x1B;

    /**
     * unsupported keys length
     */
    public static final int ENOTSUP_KEYSLENGTH = 0x1C;

    /**
     * Reserved
     */
    public static final int ERESERVED_0X1D = 0x1D;

    /**
     * unsupported pinblock format
     */
    public static final int ENOTSUP_PINBLOCK_FORMAT = 0x28;

    /**
     * username length error
     */
    public static final int EUSERNAME_LENGTH = 0x29;

    /**
     * username value error
     */
    public static final int EUSERNAME_VALUE = 0x2A;

    /**
     * user pinblock length error
     */
    public static final int EUSER_PINBLOCK_LENGTH = 0x2B;

    /**
     * user pinblock value error
     */
    public static final int EUSER_PINBLOCK_VALUE = 0x2C;

    /**
     * pinblock char error
     */
    public static final int EPINBLOCK_CHAR = 0x2D;

    public static final int PB_ALGOMODE_DUKPT = 10;
    private IMaxqEncryptService mService;

    public SEManager() {
        IBinder b = ServiceManager.getService("maxqservice");
        mService = IMaxqEncryptService.Stub.asInterface(b);
	Log.d(TAG,"mService = "+mService);
    }

    public SEManager(IMaxqEncryptService service) {
        mService = service;
    }

    /**
     * open security processor <br/>
     *
     * @return 0: success; negative number: faild <br/>
     */
    public int open() {
        //Log.i(TAG, "SEManager.open()");
        //return MaxNative.open();
        try {
            return mService.open();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * close security processor <br/>
     *
     * @return 0: success; negative number: faild <br/>
     */
    public int close() {
        //Log.i(TAG, "SEManager.close()");
        //return MaxNative.close();
        try {
            return mService.close();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        release();
        return -1;
    }

    /**
     * get security processor firmware version.  <br/>
     *
     * @param ResponseData output data, processor response data <br/>
     * @param ResLen       output data, processor response data length <br/>
     * @return return errorCode <br/>
     */
    public int getFirmwareVersion(byte[] ResponseData, byte[] ResLen) {
        //return MaxNative.getFirmwareVersion(ResponseData, ResLen);
        try {
            return mService.getFirmwareVersion(ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * get security processor hardware status <br/>
     *
     * @param ResponseData output data, 5Bytes device status:
     *                     Byte[0] – maxium storage num of keys we support(The sum of all keys)
     *                     Byte[1] – used for mag strip info encryption(TDK)
     *                     Byte[2] – used for counting PIN keys
     *                     Byte[3] – num of MAC keys
     *                     Byte[4] – num of Master keys
     *                     #：
     *                     all num of keys is not greater than the sum of all keys(Byte[0]).<br/>
     * @param ResLen       output data, length of ResponseData, 5Bytes. <br/>
     * @return return errorCode <br/>
     */
    public int getStatus(byte[] ResponseData, byte[] ResLen) {
        //return MaxNative.getDeviceStatus(ResponseData, ResLen);
        try {
            return mService.getDeviceStatus(ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * clearKey:Clear All Keys. <br/>
     *
     * @param ResponseData output data, processor response data, this function
     *                     return data is NULL. <br/>
     * @param ResLen       output data, processor response data length, this function
     *                     return data length is 0. <br/>
     * @return return errorCode <br/>
     */
    public int clearKey(byte[] ResponseData, byte[] ResLen) {
        //return MaxNative.clearKey(ResponseData, ResLen);
        try {
            return mService.clearKey(ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * [clearKeyInner Dont Export it out!!! Only For Inner Test!!!]
     */
    public int clearKeyInner(byte[] ResponseData, byte[] ResLen) {
        //return MaxNative.clearKey(ResponseData, ResLen);
        try {
            return mService.clearKeyInner(ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * loadKey:Download Keys in security processor RAM. Keys length is 8 bytes (DES), 16
     * bytes or 24Bytes(3DES) <br/>
     * Notice: Parent keys or encryption keys unsupported 8 bytes length, that
     * must be 16 bytes. <br/>
     *
     * @param KeyUsage     input data, Key Usage:
     *                     0x01 - used for Mag strip info encryption(TDK)
     *                     0x02 - used for PIN encryption
     *                     0x03 - used for MAC calculation
     *                     0x04 - Parent(Master) key
     *                     0x05 - Reserved
     *                     0x06 - Reserved
     *                     0x07 – used for message transportation encryption <br/>
     * @param KeyNo        input data, Key number, 0 - 20.<br/>
     * @param ParentKeyNo  input data, Parent Key Number. when DownloadKey is used to
     *                     load parent key, this parameter can be ignore. Parent(Master) Key have
     *                     no parent key. <br/>
     * @param KeyData      input data, key data, this parameter have 8 bytes, 16 bytes or
     *                     24 bytes(3DES) <br/>
     * @param KeyDataLen   input data, Key Data length <br/>
     * @param ResponseData output data, security processor response data, output KCV (with
     *                     clear key encryption 8 0x00 return value, get first 4 bytes) <br/>
     * @param ResLen       output data, security processor response data length <br/>
     * @return return errorCode <br/>
     */
    public int downloadKey(int KeyUsage, int KeyNo, int ParentKeyNo, byte[] KeyData, int KeyDataLen,
                           byte[] ResponseData, byte[] ResLen) {
        /*return MaxNative.loadKey(KeyUsage, KeyNo, ParentKeyNo, KeyData, KeyDataLen, ResponseData,
                ResLen);*/
        try {
            return mService.loadKey(KeyUsage, KeyNo, ParentKeyNo, KeyData, KeyDataLen, ResponseData,
                    ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int downloadKeyDuk(byte[] Bdk, int BdkLen,
                              byte[] Ksn, int KsnLen) {
		/*return MaxNative.loadKey(KeyUsage, KeyNo, ParentKeyNo, KeyData, KeyDataLen, ResponseData,
				ResLen);*/
        try {
            return mService.loadKeyDuk(Bdk, BdkLen, Ksn, KsnLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int downloadKeyEx(
            int KeyUsage, int KeyNo,
            int ParentKeyType, int ParentKeyNo,
            int Alg, byte[] KeyData, int KeyDataLen,
            int CheckMode, byte[] CheckData, int CheckDataLen,
            byte[] ResponseData, byte[] ResLen) {
        try {
            return mService.loadKeyEx(
                    KeyUsage, KeyNo,
                    ParentKeyType, ParentKeyNo,
                    Alg, KeyData, KeyDataLen,
                    CheckMode, CheckData, CheckDataLen,
                    ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * deleteKey:Delete Keys. <br/>
     *
     * @param KeyUsage     input data, Key Usage
     *                     0x01 - used for Mag strip info encryption(TDK)
     *                     0x02 - used for PIN encryption
     *                     0x03 - used for MAC calculation
     *                     0x04 - Parent(Master) key
     *                     0x05 - Reserved
     *                     0x06 - Reserved
     *                     0x07 – used for message transportation encryption <br/>
     * @param KeyNo        input data, Key index, 0 - 20.<br/>
     * @param ResponseData output data, security processor response data, this function
     *                     return data is NULL. <br/>
     * @param ResLen       output data, security processor response data length, this function
     *                     return data length is 0. <br/>
     * @return return errorCode <br/>
     */
    public int deleteKey(int KeyUsage, int KeyNo, byte[] ResponseData, byte[] ResLen) {
        //return MaxNative.deleteKey(KeyUsage, KeyNo, ResponseData, ResLen);
        try {
            return mService.deleteKey(KeyUsage, KeyNo, ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * encryptData: Encryption Data. <br/>
     *
     * @param KeyUsage       input data, Key Usage
     *                       0x01 - used for Mag strip info encryption(TDK)
     *                       0x02 - used for PIN encryption
     *                       0x03 - used for MAC calculation
     *                       0x04 - Parent(Master) key
     *                       0x05 - Reserved
     *                       0x06 - Reserved
     *                       0x07 – used for message transportation encryption <br/>
     * @param KeyNo          input data, Key index, 0 - 20.<br/>
     * @param Algorithm      input data, Algorithm define: 0x01(ECB) 0x02(CBC).<br/>
     * @param StartValue     input data, initialization vector, this value is
     *                       NULL(Algorithm = 0x01) or 8 bytes (Algorithm = 0x02).<br/>
     * @param StartValueLen  input data, initialization vector length.<br/>
     * @param PaddingChar    input data, padding character, value is 0x0 ~ 0xF.<br/>
     * @param EncryptData    input data, encryption data, length is 0 - 128 bytes. <br/>
     * @param EncryptDataLen input data, encryption data length <br/>
     * @param ResponseData   output data, encrypted data return form security processor. <br/>
     * @param ResLen         output data, security processor response data length <br/>
     * @return return errorCode <br/>
     */
    public int encryptData(int KeyUsage, int KeyNo, int Algorithm, byte[] StartValue,
                           int StartValueLen, int PaddingChar, byte[] EncryptData, int EncryptDataLen,
                           byte[] ResponseData, byte[] ResLen) {
        //return MaxNative.encryptData(KeyUsage, KeyNo, Algorithm, StartValue, StartValueLen,
        //        PadChar, EncryptData, EncryptDataLen, ResponseData, ResLen);
        try {
            return mService.encryptData(KeyUsage, KeyNo, Algorithm, StartValue, StartValueLen,
                    PaddingChar, EncryptData, EncryptDataLen, ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int encryptDataDuk(int Algorithm, int Mode/* 0 - GetPin, 1 - GetMac */,
                              byte[] EncryptData, int EncryptDataLen,
                              byte[] EncryptedData, byte[] EncryptedDataLen,
                              byte[] OutKsn, byte[] OutKsnLen) {
        //return MaxNative.encryptData(KeyUsage, KeyNo, Algorithm, StartValue, StartValueLen,
        //        PadChar, EncryptData, EncryptDataLen, ResponseData, ResLen);
        try {
            return mService.encryptDataDuk(Algorithm, Mode/* 0 - GetPin, 1 - GetMac */,
                    EncryptData, EncryptDataLen,
                    EncryptedData, EncryptedDataLen,
                    OutKsn, OutKsnLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * decryptData: Decryption Data<br/>
     *
     * @param KeyUsage       input data, Key Usage
     *                       0x01 - used for Mag strip info encryption(TDK)
     *                       0x02 - used for PIN encryption
     *                       0x03 - used for MAC calculation
     *                       0x04 - Parent(Master) key
     *                       0x05 - Reserved
     *                       0x06 - Reserved
     *                       0x07 – used for message transportation encryption <br/>
     * @param KeyNo          input data, Key index: 0 - 20.<br/>
     * @param Algorithm      input data, Algorithm define: 0x01(ECB) 0x02(CBC).<br/>
     * @param StartValue     input data, initialization vector, this value is
     *                       NULL(Algorithm = 0x01) or 8 bytes (Algorithm = 0x02).<br/>
     * @param StartValueLen  input data, initialization vector length.<br/>
     * @param PaddingChar    input data, padding character, value is 0x0 ~ 0xF.<br/>
     * @param DecryptData    input data, dencryption data, 0～128Bytes.<br/>
     * @param DecryptDataLen input data, dencryption data length.<br/>
     * @param ResponseData   output data, dencrypted data returned from security processor <br/>
     * @param ResLen         output data, security processor response data length <br/>
     * @return return errorCode <br/>
     */
    public int decryptData(int KeyUsage, int KeyNo, int Algorithm, byte[] StartValue,
                           int StartValueLen, int PaddingChar, byte[] DecryptData, int DecryptDataLen,
                           byte[] ResponseData, byte[] ResLen) {
        try {
            return mService.decryptData(KeyUsage, KeyNo, Algorithm, StartValue, StartValueLen,
                    PaddingChar, DecryptData, DecryptDataLen, ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * calculateMAC:MAC Algorithm calculate MAC value. <br/>
     *
     * @param KeyUsage       input data, Key Usage
     *                       0x01 - used for Mag strip info encryption(TDK)
     *                       0x02 - used for PIN encryption
     *                       0x03 - used for MAC calculation
     *                       0x04 - Parent(Master) key
     *                       0x05 - Reserved
     *                       0x06 - Reserved
     *                       0x07 – used for message transportation encryption <br/>
     * @param KeyNo          input data, Key index: 0 - 20. <br/>
     * @param EncryptData    input data, mac encryption data(data fragmented into piece to
     *                       perform XOR to get the 8 bytes according to UnionPay standard). <br/>
     * @param EncryptDataLen input data, mac encryption data length.<br/>
     * @param ResponseData   output data, security processor response data <br/>
     * @param ResLen         output data, security processor response data length <br/>
     * @return return errorCode <br/>
     */
    public int calculateMAC(int KeyUsage, int KeyNo,
                            int MacAlgorithmType, int DesAlgorithmType,
                            byte[] EncryptData, int EncryptDataLen,
                            byte[] ResponseData, byte[] ResLen) {
        try {
            return mService.calculateMAC(KeyUsage, KeyNo,
                    MacAlgorithmType, DesAlgorithmType,
                    EncryptData, EncryptDataLen,
                    ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get the pin block.<br/>
     *
     * @param KeyUsage        input data, Key Usage
     *                        0x01 - used for Mag strip info encryption(TDK)
     *                        0x02 - used for PIN encryption
     *                        0x03 - used for MAC calculation
     *                        0x04 - Parent(Master) key
     *                        0x05 - Reserved
     *                        0x06 - Reserved
     *                        0x07 – used for message transportation encryption<br/>
     * @param PINKeyNo        input data, Key index: 0 - 20.<br/>
     * @param CustomerData    input data, ASCII format Customer account, 0 - 24Bytes.<br/>
     * @param CustomerDataLen input data, the length of CustomerData<br/>
     * @param message         <br/>
     * @param timeOut         set the input key timeout<br/>
     * @param listener        The <code>PedInputListener</code> that will be called when
     *                        a input key event is fired.<br/>
     * @return 0 if successful, negative number if fail <br/>
     */
    public int getPinBlock(int KeyUsage, int PINKeyNo, byte[] CustomerData,
                           int CustomerDataLen, String message, long timeOut, PedInputListener listener) {
        try {
            addPedInputListener(listener);
            return mService.getPinBlock(KeyUsage, PINKeyNo, CustomerData, CustomerDataLen, message, timeOut);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void endPinInputEvent(int event) {
        try {
            mService.endPinInputEvent(event);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the pin block.<br/>
     *
     * @param KeyUsage        input data, Key Usage
     *                        0x01 - used for Mag strip info encryption(TDK)
     *                        0x02 - used for PIN encryption
     *                        0x03 - used for MAC calculation
     *                        0x04 - Parent(Master) key
     *                        0x05 - Reserved
     *                        0x06 - Reserved
     *                        0x07 – used for message transportation encryption<br/>
     * @param PINKeyNo        input data, Key index: 0 - 20.<br/>
     * @param CustomerData    input data, ASCII format Customer account, 0 - 24Bytes.<br/>
     * @param CustomerDataLen input data, the length of CustomerData<br/>
     * @param message         <br/>
     * @param timeOut         set the input key timeout<br/>
     * @param supportPinLen   set the input key support length, default format: "0,4,6,8,10,12"<br/>
     * @param listener        The <code>PedInputListener</code> that will be called when
     *                        a input key event is fired.<br/>
     * @return 0 if successful, negative number if fail <br/>
     */
    public int getPinBlockEx(int KeyUsage, int PINKeyNo, byte[] CustomerData,
                             int CustomerDataLen, String message, long timeOut, String supportPinLen, PedInputListener listener) {
        try {
            addPedInputListener(listener);
            mService.configSupportPinLen(supportPinLen);
            return mService.getPinBlock(KeyUsage, PINKeyNo, CustomerData, CustomerDataLen, message, timeOut);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get the pin block.<br/>
     *
     * @param bundle   is a Bundle obejct<br/>
     *                 eg.
     *                 Bundle param = new Bundle();<br/>
     *                 param.putInt("KeyUsage", 1);<br/>
     *                 param.putInt("PINKeyNo", 2);<br/>
     *                 param.putInt("pinAlgMode", 1);<br/>
     *                 param.putString("cardNo", "6225887855370299");<br/>
     *                 param.putBoolean("sound", false);<br/>
     *                 param.putLong("timeOutMS", 60000);<br/>
     *                 param.putString("supportPinLen", "0,4,6,8,10,12");<br/>
     *                 param.putString("title", "Security Keyboard");<br/>
     *                 param.putString("message", "please input password \n 6225****0299");<br/><br/>
     *                 KeyUsage input data, Key Usage
     *                 0x01 - used for Mag strip info encryption(TDK)
     *                 0x02 - used for PIN encryption
     *                 0x03 - used for MAC calculation
     *                 0x04 - Parent(Master) key
     *                 0x05 - Reserved
     *                 0x06 - Reserved
     *                 0x07 – used for message transportation encryption<br/>
     *                 PINKeyNo input data, Key index: 0 - 20.<br/>
     *                 pinAlgMode pinblock Algorithm Mode: 0x01(DES ECB) 0x02(DES CBC) 0x03(SM4 ECB) 0x04(SM4 CBC)(Note: need update FW version to 20170610)
     *                 CustomerData  input data, ASCII format Customer account, 0 - 24Bytes.<br/>
     *                 CustomerDataLen  input data, the length of CustomerData<br/>
     *                 message <br/>
     *                 sound set the input key sound feedback<br/>
     *                 timeOutMS set the input key timeout<br/>
     *                 supportPinLen set the input key support length, default format: "0,4,6,8,10,12"<br/>
     *                 title set the Keyboard UI title <br/>
     *                 message set the message displayed in the middle of the UI<br/>
     * @param listener The <code>PedInputListener</code> that will be called when
     *                 a input key event is fired.<br/>
     * @return 0 if successful, negative number if fail <br/>
     */
    public int getPinBlockEx(android.os.Bundle bundle, PedInputListener listener) {
        if (listener != null) {
            try {
                Log.i(TAG, "addPedInputListener");
                synchronized (mInputListeners) {
                    PedInputTransport transport = mInputListeners.get(listener);
                    if (transport == null) {
                        transport = new PedInputTransport(listener);
                    }
                    mInputListeners.put(listener, transport);
                    return mService.inputOnlinePin(bundle, transport);
                }
            } catch (RemoteException ex) {
                Log.e(TAG, "addPedInputListener: DeadObjectException", ex);
            }
        }
        return -1;
    }

    public int getPinBlockEx(android.os.Bundle bundle, IInputActionListener listener) {
        if (listener != null) {
            try {
                return mService.inputOnlinePin(bundle, listener);
            } catch (RemoteException ex) {
                Log.e(TAG, "addPedInputListener: DeadObjectException", ex);
            }
        }
        return -1;
    }

    /**
     * Generate a 8 bytes random number.<br/>
     *
     * @param ResponseData output data, to store the generated 8 bytes random number <br/>
     * @param ResLen       bytes indicating the length of response data <br/>
     * @return 0 if successful, negative number if fail <br/>
     */
    public int generateRandomData(byte[] ResponseData, byte[] ResLen) {
        try {
            return mService.generateRandomData(ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Enable the security processor timeout before automatic
     * suspension.
     *
     * @param enable       indicates to enable or to disable the security processor suspend.
     * @param ResponseData output data, security processor response data <br/>
     * @param ResLen       output data, security processor response data length <br/>
     */
    public int enableSuspend(int enable, byte[] ResponseData, byte[] ResLen) {
        return MaxNative.enableSuspend(enable, ResponseData, ResLen);
    }

    /**
     * This function allows to set the security processor timeout before automatic
     * suspension.
     *
     * @param timeout      The value of the timeout (in seconds) or 0 for infinite.
     * @param ResponseData output data, security processor response data <br/>
     * @param ResLen       output data, security processor response data length <br/>
     */
    public int setSuspendTimeout(int timeout, byte[] ResponseData, byte[] ResLen) {
        return MaxNative.setSuspendTimeout(timeout, ResponseData, ResLen);
    }

    /*
     * The application has to implement the appropriate listener
     * */
    public interface PedInputListener {
        public void onChanged(int result, int keylen, byte[] key);
    }

    private HashMap<PedInputListener, PedInputTransport> mInputListeners = new HashMap<PedInputListener, PedInputTransport>();

    public void addPedInputListener(PedInputListener listener) {
        if (listener != null) {
            try {
                Log.i(TAG, "addPedInputListener");
                synchronized (mInputListeners) {
                    PedInputTransport transport = mInputListeners.get(listener);
                    if (transport == null) {
                        transport = new PedInputTransport(listener);
                    }
                    mInputListeners.put(listener, transport);
                    mService.addInputActionListener(transport);
                }
            } catch (RemoteException ex) {
                Log.e(TAG, "addPedInputListener: DeadObjectException", ex);
            }
        }
    }

    public void removePedInputListener(PedInputListener listener) {
        if (listener != null) {
            try {
                PedInputTransport transport = mInputListeners.remove(listener);
                if (transport != null) {
                    this.mService.removeInputActionListener(transport);
                }
                transport = null;
            } catch (RemoteException ex) {
                Log.e(TAG, "removePedInputListener: DeadObjectException", ex);
            }
        }
    }

    private class PedInputTransport extends IInputActionListener.Stub {
        private PedInputListener mListener;
        private static final int MSG_INPUT_DATA = 1;
        private final Handler mListenerHandler;

        public PedInputTransport(PedInputListener listener) {
            this.mListener = listener;
            mListenerHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case MSG_INPUT_DATA:
                            int result = msg.arg1;
                            int keylen = msg.arg2;
                            Bundle bundle = (Bundle) msg.obj;
                            byte[] pinBlock = bundle.getByteArray("pinBlock");
                            byte[] ksn = bundle.getByteArray("ksn");
                            mListener.onChanged(result, keylen, pinBlock);
                            break;
                        default:
                            break;
                    }
                }
            };
        }

        public void onInputChanged(int result, int keylen, Bundle bundle) {
            Log.d(TAG, "onInputDataReceived");
            Message msg = Message.obtain();
            msg.what = MSG_INPUT_DATA;
            //msg.obj = key;
            msg.obj = bundle;
            msg.arg1 = result;
            msg.arg2 = keylen;
            mListenerHandler.removeMessages(MSG_INPUT_DATA);
            mListenerHandler.sendMessage(msg);
        }
    }

    private void release() {
        if (mInputListeners.size() != 0) {
            Set s = mInputListeners.entrySet();
            if (s != null) {
                Iterator iter = s.iterator();
                while (iter.hasNext()) {
                    Map.Entry e = (Map.Entry) iter.next();
                    PedInputListener listener = (PedInputListener) e.getKey();
                    if (listener != null) {
                        removePedInputListener(listener);
                    }
                }
            }
            mInputListeners.clear();
        }
    }

    /**
     * queryTrigger:queryTrigger max32550. <br/>
     *
     * @return 0: success; -1: faild
     */
    public int queryTrigger(byte[] trigger) {
        //Log.i(TAG, "SEManager.queryTrigger()");
        //return MaxNative.queryTrigger(0x3f, trigger);
        try {
            return mService.queryTrigger(trigger);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * queryTriggeredState:queryTriggeredState max32550. <br/>
     *
     * @return 0: success; -1: faild
     */
    public int queryTriggeredState(byte[] trigger) {
        //return MaxNative.queryTriggeredState(0x3f, trigger);
        try {
            return mService.queryTriggeredState(trigger);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * queryTriggeredInfo:queryTriggeredInfo MH1902. <br/>
     *
     * @return 0: success; -1: faild
     */
    public int queryTriggeredInfo(byte[] trigger) {
        try {
            return mService.queryTriggeredInfo(trigger);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * clearTriggeredState:clearTriggeredState max32550. <br/>
     *
     * @return 0: success; -1: faild
     */
    public int clearTriggeredState() {
        //return MaxNative.clearTriggeredState(0x3f);
        try {
            return mService.clearTriggeredState();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param Time         input data, Time (second) the machine will power off <br/>
     * @param ResponseData output data, max32550 response data, this function
     *                     return data is NULL. <br/>
     * @param ResLen       output data, max32550 response data length, this function
     *                     return data length is 0. <br/>
     * @return return errorCode <br/>
     * @hide powerOff: Close max32550 device. <br/>
     */
    public int powerOff(int Time, byte[] ResponseData, byte[] ResLen) {
        //return MaxNative.powerOff(Time, ResponseData, ResLen);
        try {
            return mService.powerOff(Time, ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * @param KeyUsage     input data, Key Usage
     * @param KeyNo        input data, Key number
     * @param ResponseData output data, max32550 response keys data <br/>
     * @param ResLen       output data, max32550 response keys data length <br/>
     * @return return errorCode <br/>
     * @hide readKey:read Keys. <br/>
     */
    public int readKey(int KeyUsage, int KeyNo, byte[] ResponseData, byte[] ResLen) {
        //return MaxNative.readKey(KeyUsage, KeyNo, ResponseData, ResLen);
        try {
            return mService.readKey(KeyUsage, KeyNo, ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getDeviceStatus(byte[] ResponseData, byte[] ResLen) {
        return getStatus(ResponseData, ResLen);
    }

    public int loadKey(int KeyUsage, int KeyNo, int ParentKeyNo, byte[] KeyData, int KeyDataLen,
                       byte[] ResponseData, byte[] ResLen) {

        return downloadKey(KeyUsage, KeyNo, ParentKeyNo, KeyData, KeyDataLen, ResponseData,
                ResLen);
    }

    public int loadKeyDuk(byte[] Bdk, int BdkLen,
                          byte[] Ksn, int KsnLen) {

        return downloadKeyDuk(Bdk, BdkLen, Ksn, KsnLen);
    }

    /**
     * encryptMagData:encryption Mag data. <br/>
     *
     * @param KeyUsage       input data, Key Usage
     * @param TDKeyNo        input data, TD Key number
     * @param EncryptData    input data, encryption data.<br/>
     * @param EncryptDataLen input data, encryption data length.<br/>
     * @param ResponseData   output data, max32550 response data <br/>
     * @param ResLen         output data, max32550 response data length <br/>
     * @return return errorCode <br/>
     */
    public int encryptMagData(int KeyUsage, int TDKeyNo, byte[] EncryptData, int EncryptDataLen,
                              byte[] ResponseData, byte[] ResLen) {
        try {
            return mService.encryptMagData(KeyUsage, TDKeyNo, EncryptData, EncryptDataLen,
                    ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int setIndicatorLED(int id, int onoff) {
        byte[] ResponseData = new byte[32];
        byte[] ResLen = new byte[1];
        return MaxNative.setLed(id, onoff, ResponseData, ResLen);
    }

    public int setRTC(int id, int onoff, byte[] ResponseData, byte[] ResLen) {
        return MaxNative.setRTC(id, onoff, ResponseData, ResLen);
    }

    public int getRTC(byte[] ResponseData, byte[] ResLen) {
        return MaxNative.getRTC(ResponseData, ResLen);
    }

    public int setBeeper(int cnts, int freq, int time) {
        byte[] ResponseData = new byte[32];
        byte[] ResLen = new byte[1];
        return MaxNative.setBeeper(cnts, freq, time, ResponseData, ResLen);
    }

    public int readRecordSck(byte[] ResponseData, int[] ResLen, byte[] rhData, int[] rhDataLen) {
        return MaxNative.readRecordSck(ResponseData, ResLen, rhData, rhDataLen);
    }

    public int readRecordTri(byte[] ResponseData, int[] ResLen, byte[] rhData, int[] rhDataLen) {
        return MaxNative.readRecordTri(ResponseData, ResLen, rhData, rhDataLen);
    }

    public int clearRecordSck() {
        return MaxNative.clearRecordSck();
    }

    public int clearRecordTri() {
        return MaxNative.clearRecordTri();
    }

    /* CA */
    /* Format: ignored;
     * Type:enum CA_TYPE{
            CA_TYPE_ROOT_UNI = 0,   // Universal, download to SE
            CA_TYPE_ROOT_OTH,   // Other, reserved by SE
            CA_TYPE_L1, // 2    // download to QCom's side, Level-1
            CA_TYPE_L2,         // downlad to QCom's side, Level-2

            CA_TYPE_END = CA_TYPE_L2 + 1,
        };
     * Index: 0~63;
     */

    /*4101004000ff0000000000000001ff0000000000000000ffffffffffffffffffff0000000000000000*/
    /*  struct ca_status{
            unsigned char cnts; // 0x41
            unsigned char st_cnt[CA_TYPE_END]; // 01 00 40 00, cnts for 4 specific kind of Types
            unsigned char st[CA_TYPE_END][(CA_MAX_NUM >> 3) + 1]; // '1' for TAG 'ff'
            unsigned char rsv[3];
        };
    */
    public int getCAStatus(byte[] st, int[] stLen) {
        return MaxNative.getCAStatus(st, stLen);
    }

    public int selectPsam2PowerSource(int cit) {
        return MaxNative.selectPsam2PowerSource(cit);
    }


    public int pciWriteMKeyse(int isPtk, byte app_no, byte key_type, byte key_no, byte key_len, byte[] key_data,
                              byte[] appname, byte[] mac) {
        //Log.i(TAG, "SEManager.pciWriteMKeyse()");
        try {
            return mService.pciWriteMKeyse(isPtk, app_no, key_type, key_no, key_len, key_data, appname, mac);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public int pciWriteSKeyse(int isPtk, byte key_type, byte key_no, byte key_len, byte[] key_data, byte[] key_crc,
                              byte mode, byte mkey_no) {
        //Log.i(TAG, "SEManager.pciWriteSKeyse()");
        try {
            return mService.pciWriteSKeyse(isPtk, key_type, key_no, key_len, key_data, key_crc, mode, mkey_no);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public int pciGetMac(byte mackey_n, int inlen, byte[] indata, byte[] macout, byte mode) {
        //Log.i(TAG, "SEManager.pciGetMac()");
        try {
            return mService.pciGetMac(mackey_n, inlen, indata, macout, mode);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int encryptDataTDK(int KeyNo, int Algorithm, byte[] StartValue, int StartValueLen,
                              int PadChar, byte[] EncryptData, int EncryptDataLen, byte[] ResponseData, int[] ResLen) {
        //Log.i(TAG, "SEManager.encryptDataTDK()");
        try {
            return mService.encryptDataTDK(KeyNo, Algorithm, StartValue, StartValueLen, PadChar, EncryptData,
                    EncryptDataLen, ResponseData, ResLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int readCert(int type, int index, int coding, byte[] key) {
        try {
            return mService.readCert(type, index, coding, key);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int writeCert(int type, int index, int coding, byte[] key, int len) {
        try {
            return mService.writeCert(type, index, coding, key, len);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int writeCA(int format, int type, int index, byte[] responseData, int resLen) {
        //Log.i(TAG,"SEManager.writeCA");
        try {
            return mService.writeCA(format, type, index, responseData, resLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int readCA(int format, int type, int index, byte[] responseData, int[] resLen) {
        //Log.i(TAG,"SEManager.readCA");
        try {
            return mService.readCA(format, type, index, responseData, resLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int deleteCA(int format, int type, int index) {
        //Log.i(TAG,"SEManager.deleteCA");
        try {
            return mService.deleteCA(format, type, index);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int verifyCertSign(byte[] msgDigest, int msglen, byte[] sign, int signlen) {
        //Log.i(TAG, "SEManager.verifyCertSign()");
        try {
            return mService.verifyCertSign(msgDigest, msglen, sign, signlen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public int downloadKeyDukpt(int keyType, byte[] Bdk, int BdkLen, byte[] Ksn, int KsnLen, byte[] bsIpek, int bsIpeklength) {
        try {
            return mService.downloadKeyDukpt(keyType, Bdk, BdkLen, Ksn, KsnLen, bsIpek, bsIpeklength);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * download bdk or ipek for PIN encryption. <br/>
     *
     * @param Bdk          : BDK data <br/>
     * @param BdkLen       : The length of Bdk.<br/>
     * @param Ksn          : KSN data. <br/>
     * @param KsnLen       : The length of KSN data. <br/>
     * @param bsIpek       : IPEK data. <br/>
     * @param bsIpeklength : The length of IPEK data. <br/>
     * @return return errorCode <br/>
     */
    public int downloadKeyOfPINBdk(byte[] Bdk, int BdkLen, byte[] Ksn, int KsnLen, byte[] bsIpek, int bsIpeklength) {
        try {
            return mService.downloadKeyOfPINBdk(Bdk, BdkLen, Ksn, KsnLen, bsIpek, bsIpeklength);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * download bdk or ipek for Magcard data encryption. <br/>
     *
     * @param Bdk          : BDK data <br/>
     * @param BdkLen       : The length of Bdk.<br/>
     * @param Ksn          : KSN data. <br/>* @param KsnLen : The length of KSN data. <br/>
     * @param bsIpek       : IPEK data. <br/>
     * @param bsIpeklength : The length of IPEK data. <br/>
     * @return return errorCode <br/>
     */
    public int downloadKeyOfTDKBdk(byte[] Bdk, int BdkLen, byte[] Ksn, int KsnLen, byte[] bsIpek, int bsIpeklength) {
        try {
            return mService.downloadKeyOfTDKBdk(Bdk, BdkLen, Ksn, KsnLen, bsIpek, bsIpeklength);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Get the pin block.<br/>
     *
     * @param bundle is a Bundle obejct<br/>
     *  eg.
     *  Bundle param = new Bundle();<br/>
     *  param.putInt("KeyUsage", 1);<br/>
     *  param.putInt("PINKeyNo", 2);<br/>
     *  param.putInt("pinAlgMode", 1);<br/>
     *  param.putString("cardNo", "6225887855370299");<br/>
     *  param.putBoolean("sound", false);<br/>
     *  param.putLong("timeOutMS", 60000);<br/>
     *  param.putString("supportPinLen", "0,4,6,8,10,12");<br/>
     *  param.putString("title", "Security Keyboard");<br/>
     *  param.putString("message", "please input password \n 6225****0299");<br/><br/>
     * KeyUsage input data, Key Usage
     *                 0x01 - used for Mag strip info encryption(TDK)
     *                 0x02 - used for PIN encryption
     *                 0x03 - used for MAC calculation
     *                 0x04 - Parent(Master) key
     *                 0x05 - Reserved
     *                 0x06 - Reserved
     *                 0x07 – used for message transportation encryption<br/>
     * PINKeyNo input data, Key index: 0 - 20.<br/>
     * pinAlgMode pinblock Algorithm Mode: 0x01(DES ECB) 0x02(DES CBC) 0x03(SM4 ECB) 0x04(SM4 CBC)(Note: need update FW version to 20170610)
     * CustomerData  input data, ASCII format Customer account, 0 - 24Bytes.<br/>
     * CustomerDataLen  input data, the length of CustomerData<br/>
     * message <br/>
     * sound set the input key sound feedback<br/>
     * timeOutMS set the input key timeout<br/>
     * supportPinLen set the input key support length, default format: "0,4,6,8,10,12"<br/>
     * title set the Keyboard UI title <br/>
     * message set the message displayed in the middle of the UI<br/>
     * @param listener  The <code>PedInputListener</code> that will be called when
     *            a input key event is fired.<br/>
     * @return 0 if successful, negative number if fail <br/>
     */
/*    public int getPinBlockEx(android.os.Bundle bundle, PedInputListener listener) {
        if (listener != null) {
           try {
                Log.i(TAG, "addPedInputListener");
                synchronized (mInputListeners) {
                    PedInputTransport transport = mInputListeners.get(listener);
                    if (transport == null) {
                        transport = new PedInputTransport(listener);
                    }
                    mInputListeners.put(listener, transport);
                    return mService.inputOnlinePin(bundle, transport);
                }
            } catch (RemoteException ex) {
                Log.e(TAG, "addPedInputListener: DeadObjectException", ex);
            }

        }
        return -1;
    }
*/

    /**
     * calculateMACOfDUKPT : calculate the MAC under DUKPT
     * Key-Manager. <br/>
     *
     * @param rawData    : input data. <br/>
     * @param rawDataLen : The length of rawData.<br/>
     * @param outData    : The MAC result of rawData. <br/>
     * @param outDataLen : The length of outData. <br/>
     * @return return errorCode <br/>
     */
    public int calculateMACOfDUKPT(byte[] rawData, int rawDataLen, byte[] outData, int[] outDataLen, byte[] outKsn, int[] KsnLen) {
        try {
            return mService.calculateMACOfDUKPT(rawData, rawDataLen, outData, outDataLen, outKsn, KsnLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int calculateMACOfDUKPTExtend(int keySetNum, byte[] rawData, int rawDataLen, byte[] outData, int[] outDataLen, byte[] outKsn, int[] KsnLen) {
        try {
            return mService.calculateMACOfDUKPTExtend(keySetNum, rawData, rawDataLen, outData, outDataLen, outKsn, KsnLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int DukptPinblockBySP(int mode, int iKeyType, byte[] cardNo, byte[] pinBlock, byte[] outKsn) {
        try {
            return mService.DukptPinblockBySP(mode, iKeyType, cardNo, pinBlock, outKsn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }


    /**
     * DukptEncryptData : Base DUKPT Key encryption func. <br/>
     *
     * @param keyType    :<br/>
     *                   0x01 - Pin;
     *                   0x02 - Mac;
     *                   0x03 - TrackData. <br/>
     * @param keySetNum  : index of which key sets.<br/>
     *                   0x01 - TDK Set;
     *                   0x02 - reserve for emv;
     *                   0x03 - PEK Set. <br/>
     *                   0x04 - reserve for mac key set. <br/>
     * @param rawData    : input data. <br/>
     * @param rawDataLen : The length of inLen. <br/>
     * @param outData    : encrypted data. <br/>
     * @param outDataLen : The length of dataOut. <br/>
     * @param outKsn     : outKsn data. <br/>
     * @param KsnLen     : The length of outKsn. <br/>
     * @return : return result <br/>
     */
    public int encryptWithPEK(int keyType, int keySetNum, byte[] rawData, int rawDataLen, byte[] outData, int[] outDataLen, byte[] outKsn, int[] KsnLen) {
        try {
            return mService.encryptWithPEK(keyType, keySetNum, rawData, rawDataLen, outData, outDataLen, outKsn, KsnLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public int DukptEncryptDataIV(int keyType, int keySetNum, int encMode,
                                  byte[] iv, int ivLen, byte[] dataIn, int inLen,
                                  byte[] dataOut, int[] outLen, byte[] outKsn, int[] KsnLen) {
        try {
            return mService.DukptEncryptDataIV(keyType, keySetNum, encMode, iv, ivLen, dataIn, inLen, dataOut, outLen, outKsn, KsnLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void GetDukptPinBlock(byte keyIndex, byte min_len, byte max_len, byte cardlen,
                                 byte[] card_no, int waitTimeoutMs, IInputActionListener listener) {
        byte[] money = new byte[50];
        byte[] out_ksn = new byte[10];
        byte[] indata = new byte[300];
        byte[] SW = new byte[2];
        byte[] OfflineEncPin = new byte[256];

        SEManager mp = new SEManager();

        open();

        String len_limit = rangeSpec((int) min_len, (int) max_len);
        String cardNumStr = new String(card_no);

        Log.i(TAG, "Lib_PinBlockGetDukpt key_n=" + keyIndex + " len_limit=" + len_limit + " cardno=" + cardNumStr);
        Bundle paramVar = new Bundle();

        paramVar.putInt("inputType", 0);
        paramVar.putInt("KeyUsage", 2);
        paramVar.putInt("PINKeyNo", keyIndex);
        paramVar.putBoolean("bypass", false);
        paramVar.putInt("pinAlgMode", PB_ALGOMODE_DUKPT);
        paramVar.putString("cardNo", cardNumStr);
        paramVar.putBoolean("sound", true);
        paramVar.putBoolean("onlinePin", true);
        paramVar.putBoolean("FullScreen", true);
        paramVar.putLong("timeOutMS", waitTimeoutMs);
        paramVar.putString("supportPinLen", len_limit);
        paramVar.putString("title", "Security Keyboard");
        paramVar.putString("message", "Please input pin and cover by hand\n");
        //paramVar.putInt("checkAdminMode", 1);//Check Admin Password.
        //mCV = new ConditionVariable();

        //AdminInputListen mPedAdminInput = new AdminInputListen();


        //mp.getPinBlockEx(param, mPedInputListener);
        getPinBlockEx(paramVar, listener);
//Log.d(TAG,">>>>>>>>>>> block block");
        //mCV.block();
//Log.d(TAG,">>>>>>>>>>> block close");
        //mCV.close();
        Log.d(TAG, ">>>>>>>>>>> se close");
        close();

    }

    public void GetDukptPinBlock(byte keyIndex, byte min_len, byte max_len, byte cardlen,
                                 byte[] card_no,boolean bypass, int waitTimeoutMs, IInputActionListener listener) {
        byte[] money = new byte[50];
        byte[] out_ksn = new byte[10];
        byte[] indata = new byte[300];
        byte[] SW = new byte[2];
        byte[] OfflineEncPin = new byte[256];

        SEManager mp = new SEManager();

        open();

        String len_limit = rangeSpec((int) min_len, (int) max_len);
        String cardNumStr = new String(card_no);

        Log.i(TAG, "Lib_PinBlockGetDukpt key_n=" + keyIndex + " len_limit=" + len_limit + " cardno=" + cardNumStr);
        Bundle paramVar = new Bundle();

        paramVar.putInt("inputType", 0);
        paramVar.putInt("KeyUsage", 2);
        paramVar.putInt("PINKeyNo", keyIndex);
        paramVar.putBoolean("bypass", bypass);
        paramVar.putInt("pinAlgMode", PB_ALGOMODE_DUKPT);
        paramVar.putString("cardNo", cardNumStr);
        paramVar.putBoolean("sound", true);
        paramVar.putBoolean("onlinePin", true);
        paramVar.putBoolean("FullScreen", true);
        paramVar.putLong("timeOutMS", waitTimeoutMs);
        paramVar.putString("supportPinLen", len_limit);
        paramVar.putString("title", "Security Keyboard");
        paramVar.putString("message", "Please input pin and cover by hand\n");
        //paramVar.putInt("checkAdminMode", 1);//Check Admin Password.
        //mCV = new ConditionVariable();

        //AdminInputListen mPedAdminInput = new AdminInputListen();


        //mp.getPinBlockEx(param, mPedInputListener);
        getPinBlockEx(paramVar, listener);
//Log.d(TAG,">>>>>>>>>>> block block");
        //mCV.block();
//Log.d(TAG,">>>>>>>>>>> block close");
        //mCV.close();
        Log.d(TAG, ">>>>>>>>>>> se close");
        close();

    }

    private static String rangeSpec(int min, int max) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = min; i <= max; i++) {
            if (i != max) {
                stringBuilder.append(i + ",");
            } else {
                stringBuilder.append(i);
            }
        }

        return stringBuilder.toString();
    }

    public boolean generateRSAKey(){
        try {
            return mService.generateRSAKey();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }


    public boolean getRSAPublicKeyModel(byte[] publickey,int[] publickeyLen,int[] exponent){
        try {
            return mService.getRSAPublicKeyModel(publickey,publickeyLen,exponent);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int loadDukptBlob(int keySlot,byte[] blob,int blobLen){
        try {
            return mService.loadDukptBlob(keySlot,blob,blobLen);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public int DukptGetKsn(int keySetNum, byte[] outKsn){
        try {
            return mService.DukptGetKsn(keySetNum,outKsn);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return -1;
    }
	
	// BCM
	public int eppkeyGen(byte algo,byte[] ResponseData, byte[] ResLen){
        return MaxNative.eppkeyGen(algo,ResponseData,ResLen);
    }

    public int eppRandGen(byte algo,byte[] ResponseData, byte[] ResLen){
        return MaxNative.eppRandGen(algo,ResponseData,ResLen);
    }

    public int pkEppRead(byte algo,byte[] ResponseData, byte[] ResLen){
        return MaxNative.pkEppRead(algo,ResponseData,ResLen);
    }

    public int pkEppsPKVndWrite(byte[] PKEpps, int PKEppsLen, byte[] PKVnd, int PKVndLen, byte algo, byte[] ResponseData, byte[] ResLen){
        return MaxNative.pkEppsPKVndWrite(PKEpps, PKEppsLen, PKVnd, PKVndLen, algo, ResponseData, ResLen);
    }

    public int pkEppsRead(byte algo,byte[] ResponseData, byte[] ResLen){
        return MaxNative.pkEppsRead(algo,ResponseData,ResLen);
    }

    public int pkBanksPKBankVerity(byte[] PKBanks, int PKBanksLen, byte[] PKBank, int PKBankLen, byte algo, byte[] ResponseData, byte[] ResLen){
        return MaxNative.pkBanksPKBankVerity(PKBanks, PKBanksLen, PKBank, PKBankLen, algo, ResponseData, ResLen);
    }
}
