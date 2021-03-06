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
 * {@hide}
 */

package android.device;

import java.io.FileDescriptor;

public class ScanNative {
    static{
        System.loadLibrary("uscanner");
    }
    /**
     * Return true if open succeed.
     * 
     * @see #openscan()
     */
    /*
     * public boolean openScan(){ return true; }
     */
    /**
     * Return true if close succeed
     * 
     * @see #closescan()
     */
    /*
     * public boolean closeScan(){ return true; }
     */
    public native static boolean openScan();

    public native static boolean closeScan();

    public native static boolean scanUp();

    public native static boolean scanDown();

    public native static boolean lockTriggle();

    public native static boolean unlockTriggle();

    public native static boolean doAck();

    public native static boolean doNack(int reason);

    public native static boolean doDefaultSet();
    
    public native static boolean dohonywareset();
    public native static boolean resetHoneywellRS232();
    public native static boolean doopticonset();
    
    public native static boolean dominjiedefaultset();

    public native static boolean setIndicatorRed(int onoff);

    public native static boolean setIndicatorBlue(int onoff);

    /*public native static FileDescriptor open(String path, int baudrate,
            int scantype);*/
    public native static int serialRead(int fd, byte[] buffer, int length);
    public native static int openSerialFd(String path, int baudrate,
            int scantype);
    public native static int closeSerialFd(int fd);
    // zdw
    public native static boolean doGetZdwVersion();

    public native static boolean doAckzdw();
    public native static boolean setTriggerMode(int mode);
    public native static boolean setProperties(byte[] szByte);
    public native static boolean getProperties(byte[] szByte);
	public native static boolean setHWProperties(byte[] szByte);
    public native static boolean setOptionParams(byte[] command);
    public native static boolean setDM30Properties(byte[] szByte);
    public native static boolean resetDM30Scanner();
    public native static boolean triggerCmd(int action, int engineType);
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          