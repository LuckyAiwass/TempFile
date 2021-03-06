/*
 * Copyright (C) 2015, Urovo Corp., Ltd.
 *
  * {@hide}
 */

package android.device;

/**
 *{@hide}
 */
public class IccReaderNative {
    static{
        System.loadLibrary("iccreader_jni");
    }
    // Open & Close Max3250 Serial
    public static native int open();
    public static native int close(int fd);

    // IccReader
    public static native int IccOpen(int fd, byte CardType, byte Volt);
    public static native int IccClose(int fd);

    public static native int SelectSlot(int fd, byte Slot);
    public static native int Detect(int fd, byte[] Slots);
    public static native int Reset(int fd, byte[] pAtr);
    public static native int ResetEx(int fd, byte[] pAtr);
    public static native int ApduTransmit(int fd, byte[] Apdu, int apdulen, byte[] Rsp, byte[] Sw);
    public static native int Eject(int fd);
    public static native void SetApduTimeOut(int fd, int ms);
    public static native int GetResponseEnable(int fd, byte autoflag);
    public static native int SetETU(int fd, int etuTime);
    public static native void StopApduRspRecv(int fd);

    // Sle4442 - temp
    public static native int Sle4442_Reset(int fd, byte[] pAtr);
    public static native byte[] Sle4442_ReadMainMemory(int fd,int addr, int length);
    public static native int Sle4442_WriteMainMemory(int fd, int addr, byte[] data, int length);
    public static native byte[] Sle4442_ReadProtectionMemory(int fd, int address, int length);
    public static native int Sle4442_WriteProtectionMemory(int fd, int addr, byte[] data, int length);
    public static native int Sle4442_VerifyPassword(int fd, byte[] passwd);
    public static native int Sle4442_ChangePassword(int fd, byte[] passwd);
    public static native int Sle4442_ReadErrorCounter(int fd, byte[] errorCount);

	// Sle4436
    public static native int Sle4436_Reset(int fd, byte[] pAtr);
    public static native byte[] Sle4436_ReadMemory(int fd,int addr, int length);
    public static native int Sle4436_WriteMemory(int fd, int addr, byte[] data, int length);
    public static native int Sle4436_WriteCarry(int fd, int mode, int addr, byte[] data, int length);
    public static native int Sle4436_VerifyPassword(int fd, byte[] passwd);
    public static native byte[] Sle4436_Authenticate(int fd, int key, byte[] clkCnt, byte[] challengeData);
    public static native int Sle4436_RegIncrease(int fd, int shiftBits);
    public static native int Sle4436_ReadBit(int fd, byte[] pData);
    public static native int Sle4436_WriteBit(int fd);
    public static native int Sle4436_ReloadByte(int fd);
    public static native int Sle4436_DecValue(int fd, int value);

	// Sle4428
    public static native int Sle4428_Reset(int fd, byte[] pAtr);
    public static native byte[] Sle4428_ReadMemory(int fd,int addr, int length);
    public static native int Sle4428_WriteMemory(int fd, int addr, byte[] data, int length);
    public static native int Sle4428_Password(int fd, int mode, byte[] passwd);

	// At88sc102
    public static native byte[] At88sc102_Read(int fd,int addr, int length);
    public static native int At88sc102_Write(int fd, int addr, byte[] data, int length);
    public static native int At88sc102_VerifyPassword(int fd, int type, byte[] passwd, int length);
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          