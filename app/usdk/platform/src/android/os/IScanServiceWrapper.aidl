
package android.os;
import java.util.Map;
import android.os.scanner.IScanCallBack;

/** {@hide} */
interface IScanServiceWrapper
{
    boolean open() ;
    void close() ;
    void closeScannerByCamera() ;
    void writeConfig(String name,int value) ;
    int readConfig(String name) ;
    String writeConfigs(String name,String value) ;
    String readConfigs(String name,String value) ;
    Map getScanerList();
    
    boolean setDefaults();
    void softTrigger(int on);
    boolean lockHwTriggler(boolean lock);
    int getPropertyInt(int index);
    int setPropertyInts(in int[] id_buffer, int id_buffer_length, in int[] value_buffer, int value_buffer_length, out int[] id_bad_buffer);
    int getPropertyInts(in int[] id_buffer, int id_buffer_length, out int[] value_buffer, int value_buffer_length, out int[] id_bad_buffer);
    boolean setPropertyString(int index, String value);
    String getPropertyString(int index);
    void enableAllSymbologies(boolean enable);
    boolean isSymbologyEnabled(int barcodeType);
    boolean isSymbologySupported(int barcodeType);
    void enableSymbology(int barcodeType, boolean enable);
    boolean getCameraStatus();
    void setCameraStatus(boolean status) ;

    void addScanCallBack(in IScanCallBack sr);
    void removeScanCallBack(in IScanCallBack sr);
    // urovo add shenpidong begin 2019-07-20
    boolean screenTurnedOn(boolean on);
    // urovo add shenpidong end 2019-07-20
}

