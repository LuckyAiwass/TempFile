package com.android.server;

import android.device.MaxNative;
import android.device.SEManager;
import android.device.DukptAlgorithm;
import android.device.DukptNative;

import java.util.Arrays;
import android.util.Log;


public class PinPad {
    /****************************************************************************
     * des pin start
   ****************************************************************************/
    public static byte aasc_to_bcd(byte ucAsc) {
        byte ucBcd;

        if ((ucAsc >= '0') && (ucAsc <= '9'))
            ucBcd = (byte) (ucAsc - '0');
        else if ((ucAsc >= 'A') && (ucAsc <= 'F'))
            ucBcd = (byte) (ucAsc - 'A' + 10);
        else if ((ucAsc >= 'a') && (ucAsc <= 'f'))
            ucBcd = (byte) (ucAsc - 'a' + 10);
        else if ((ucAsc > 0x39) && (ucAsc <= 0x3f))
            ucBcd = (byte) (ucAsc - '0');
        else
            ucBcd = 0x0f;

        return ucBcd;
    }

    public static void AscToBcd(byte[] sBcdBuf, byte[] sAscBuf, int iAscLen) {
        int i, j = 0;

        for (i = 0; i < (iAscLen + 1) / 2; i++) {
            sBcdBuf[i] = (byte) (aasc_to_bcd(sAscBuf[j++]) << 4);
            if (j >= iAscLen) {
                sBcdBuf[i] |= 0x00;
            } else {
                sBcdBuf[i] |= aasc_to_bcd(sAscBuf[j++]);
            }
        }
    }

    public static void do_xor_urovo(byte[] src1, byte[] src2, int num) {
        int i;
        for (i = 0; i < num; i++) {
            src1[i] ^= src2[i];
        }
    }

    public static byte abcd_to_asc(byte ucBcd) {
        byte ucAsc;
        ucBcd &= 0x0f;
        if (ucBcd <= 9)
            ucAsc = (byte) (ucBcd + (byte) ('0'));
        else
            ucAsc = (byte) (ucBcd + (byte) ('A') - (byte) 10);
        return (ucAsc);
    }

    public static void BcdToAsc(byte[] sAscBuf, byte[] sBcdBuf, int iAscLen) {
        int i, j = 0;

        for (i = 0; i < iAscLen / 2; i++) {
            sAscBuf[j] = (byte) ((sBcdBuf[i] & 0xf0) >> 4);
            sAscBuf[j] = abcd_to_asc(sAscBuf[j]);
            j++;
            sAscBuf[j] = (byte) (sBcdBuf[i] & 0x0f);
            sAscBuf[j] = abcd_to_asc(sAscBuf[j]);
            j++;
        }
        if ((iAscLen % 2) != 0) {
            sAscBuf[j] = (byte) ((sBcdBuf[i] & 0xf0) >> 4);
            sAscBuf[j] = abcd_to_asc(sAscBuf[j]);
        }
    }
    public static byte[] ascii2Bcd(String ascii) {
        if (ascii == null)
            return null;
        if ((ascii.length() & 0x01) == 1)
            ascii = "0" + ascii;
        byte[] asc = ascii.getBytes();
        byte[] bcd = new byte[ascii.length() >> 1];
        for (int i = 0; i < bcd.length; i++) {
            bcd[i] = (byte)(hex2byte((char)asc[2 * i]) << 4 | hex2byte((char)asc[2 * i + 1]));
        }
        return bcd;
    }
    public static byte hex2byte(char hex) {
        if (hex <= 'f' && hex >= 'a') {
            return (byte) (hex - 'a' + 10);
        }

        if (hex <= 'F' && hex >= 'A') {
            return (byte) (hex - 'A' + 10);
        }

        if (hex <= '9' && hex >= '0') {
            return (byte) (hex - '0');
        }

        return 0;
    }
    public static int ExtractPAN(byte[] carno, byte[] pan) {
        if (carno == null)
            return SEManager.EUSERNAME_VALUE;
        int len;
        len = carno.length;
        if (len < 13 || len > 19)
            return SEManager.EUSERNAME_LENGTH; // 帐号取出错

        Arrays.fill(pan, (byte) '0');
        System.arraycopy(carno, len - 13, pan, 4, 12);
        return 0;
    }
    public static int dukptPinblock(int mode, int type, String PinD, byte[] BCarNo, byte[] pinBlock, byte[] outKsn) {
        int i, n;
        int j = 0;
        // 处理帐号
        byte[] PAN = new byte[17];
        // byte[] BCarNo = CarNo.getBytes();
        byte[] card_buf = new byte[16];

        int ret = ExtractPAN(BCarNo, PAN);
        if (ret != 0)
            return ret;
        AscToBcd(card_buf, PAN, 16);

        byte[] pin = PinD.getBytes();
        byte[] pin_buf = new byte[17];
        byte[] enpin_buf = new byte[8];
        byte[] buf = new byte[20];

        pin_buf[0] = (byte) pin.length;
        System.arraycopy(pin, 0, buf, 0, pin.length);
        n = pin.length;
        for (i = n; i < 17; i++) {
            buf[i] = 'F';
        }
        byte[] tempbuff = new byte[7];

        AscToBcd(tempbuff, buf, 14);
        System.arraycopy(tempbuff, 0, pin_buf, 1, 7);

        do_xor_urovo(card_buf, pin_buf, 8);
        Arrays.fill(enpin_buf, (byte) 0);
        System.arraycopy(card_buf, 0, enpin_buf, 0, 8);
        ret = DukptNative.DukptPinblock(mode, type, enpin_buf, pinBlock, outKsn);
		Log.i("PinPad","--------->DukptNative.DukptPinblock ret :" + ret);
        return ret;
    }
    
    public static int dukptPinblockBySP(int mode, int type, byte[] BCarNo, byte[] pinBlock, byte[] outKsn) {
        int ret = 0;
  
        ret = DukptNative.DukptPinblockBySP(mode, type, BCarNo, pinBlock, outKsn);
		Log.i("PinPad","--------->DukptNative.DukptPinblockBySP ret :" + ret);
        return ret;
    }    
    
    //Algorithm - 0x01(DES ECB) 0x02(DES CBC) 0x03(国密ECB) 0x04(国密CBC) */
    public static int pciDes(int KeyType, int key_no, int inlen, byte[] indata, byte[] desout,
            int mode, int AlgMode) {
        int iRet;
        byte[] reslen = new byte[1];
        byte[] dStartValue = new byte[8];
        if (mode == 0x01) {
            iRet = MaxNative.encryptData(KeyType, key_no, AlgMode, dStartValue, 8, 0x00, indata, inlen,
                    desout, reslen);
        } else {
            iRet = MaxNative.decryptData(KeyType, key_no, AlgMode, dStartValue, 8, 0x00, indata, inlen,
                    desout, reslen);
        }
        return iRet;
    }
    
    public static int PINDES(String PinD, byte[] BCarNo, int KeyUsage, int key_no, int pinAlgMode, byte[] Pin_OUT) {
        int iRet = -1;
        if(pinAlgMode == 0x03 || pinAlgMode == 0x04) {
            //SM4
            byte[] PAN = new byte[32];
            int length = BCarNo.length;
            if( length < 13) {
                System.arraycopy(BCarNo, 0, PAN, 32 - length, length);
            } else {
                System.arraycopy(BCarNo, BCarNo.length - 13, PAN, 20, 12);
            }
            byte[] PANBLOCK = ascii2Bcd(new String(PAN));
            byte[] pin = PinD.getBytes();
            
            byte[] pin_buf = new byte[16];
            Arrays.fill(pin_buf, (byte)0xFF);
            pin_buf[0] = (byte) pin.length;
            byte[] tempbuff = ascii2Bcd(PinD);
            System.arraycopy(tempbuff, 0, pin_buf, 1, tempbuff.length);

            byte[] enpin_buf = new byte[16];
            do_xor_urovo(PANBLOCK, pin_buf, 16);
            Arrays.fill(enpin_buf, (byte) 0);
            System.arraycopy(PANBLOCK, 0, enpin_buf, 0, 16);
            byte[] pin_out = new byte[16];
            for (int j = 0; j < 3; j++) {
                iRet = pciDes(KeyUsage, key_no, 16, enpin_buf, pin_out, 1, pinAlgMode);
                if (iRet == 0) {
                    break;
                }
            }
            if (iRet == 0) {
                byte[] byteArray = new byte[32];
                //byte[] bcdPinOut = new byte[8];
                //System.arraycopy(pin_out, 0, bcdPinOut, 0, 8);
                BcdToAsc(byteArray, pin_out, 32);
                if (Pin_OUT != null && Pin_OUT.length >= 32)
                    System.arraycopy(byteArray, 0, Pin_OUT, 0, 32);
            }
        } else {
            //DES/3DES
            int i, n;
            int j = 0;
            // 处理帐号
            byte[] PAN = new byte[17];
            // byte[] BCarNo = CarNo.getBytes();
            byte[] card_buf = new byte[16];

            iRet = ExtractPAN(BCarNo, PAN);
            if (iRet != 0)
                return iRet;
            AscToBcd(card_buf, PAN, 16);

            byte[] pin = PinD.getBytes();
            byte[] pin_buf = new byte[17];
            byte[] enpin_buf = new byte[8];
            byte[] buf = new byte[20];

            pin_buf[0] = (byte) pin.length;
            System.arraycopy(pin, 0, buf, 0, pin.length);
            n = pin.length;
            for (i = n; i < 17; i++) {
                buf[i] = 'F';
            }
            byte[] tempbuff = new byte[7];

            AscToBcd(tempbuff, buf, 14);
            System.arraycopy(tempbuff, 0, pin_buf, 1, 7);

            do_xor_urovo(card_buf, pin_buf, 8);
            Arrays.fill(enpin_buf, (byte) 0);
            System.arraycopy(card_buf, 0, enpin_buf, 0, 8);
            byte[] pin_out = new byte[8];
            for (j = 0; j < 3; j++) {
                iRet = pciDes(KeyUsage, key_no, 8, enpin_buf, pin_out, 1, pinAlgMode);
                if (iRet == 0) {
                    break;
                }
            }
            if (iRet == 0) {
                byte[] byteArray = new byte[16];
                BcdToAsc(byteArray, pin_out, 16);
                if (Pin_OUT != null && Pin_OUT.length >= 16)
                    System.arraycopy(byteArray, 0, Pin_OUT, 0, 16);
            }
        }
        return iRet;
    }
}
