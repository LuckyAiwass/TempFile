
package com.ubx.scanwedge.aidl;
import java.util.Map;

/** {@hide} */
interface IScanWedgeEngine
{
    int open() ;
    int close() ;
    Map getScanerList();
    
    boolean setDefaults();
    boolean softTrigger(int on);
    boolean hardwareTrigger(int on);
    boolean lockHwTriggler(boolean lock);
    int getPropertyInt(int index);
    int setPropertyInts(in int[] id_buffer, int id_buffer_length, in int[] value_buffer, int value_buffer_length, out int[] id_bad_buffer);
    int getPropertyInts(in int[] id_buffer, int id_buffer_length, out int[] value_buffer, int value_buffer_length, out int[] id_bad_buffer);
    boolean setPropertyString(int index, String value);
    String getPropertyString(int index);
    void enableAllSymbologies(boolean enable);
    boolean isSymbologyEnabled(int barcodeType);
    boolean isSymbologySupported(int barcodeType);
    boolean isPropertySupported(int idProperty);
    void enableSymbology(int barcodeType, boolean enable);
    /*int setNumParameter(int paramNum, int paramVal);
    int setStrParameter(int paramNum, String paramVal);
    int getNumParameter(int paramNum);
    String getStrParameter(int paramNum);*/
}

