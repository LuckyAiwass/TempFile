package com.ubx.factorykit.AttesttationKey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;
import com.qti.factory.Framework.ShellUtils;
import com.qti.factory.Framework.ShellUtils.CommandResult;
import com.ubx.factorykit.Values;
import com.unicair.googlekey.GoogleKey;
import android.os.SystemProperties;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class AttesttationKey extends Activity {

    String TAG = "AttesttationKey";
    String resultString = Utilities.RESULT_FAIL;
    String googleKeyDeviceID = "58231930300001";
    final private String googleKeyFilePath = "/vendor/bin/test_keybox.xml";

   
    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void finish() {

        Utilities.writeCurMessage(getApplicationContext(), TAG, resultString);
        super.finish();
    }

    private void init(Context context)
    { 
        resultString = Utilities.RESULT_FAIL;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.battery);
        init(getApplicationContext());

        int status = SystemProperties.getInt("persist.sys.googlekey", 0);
        File keyfile = new File(googleKeyFilePath);
        if(status > 0) {
            pass();
        } else if(!keyfile.exists()) {
            Log.d(TAG,"device not have googlekey file !!!");
            pass();
        } else {

    		boolean ret = false;
            getGoogkeyNameString();
    		//ret = GoogleKey.getInstance().verifyKey();
    		//Log.d("zxj", "GoogleKey check ret = " + ret);
    		if(!ret){
                        String[] commands = {"",""};
                        /*if(android.os.Build.PWV_CUSTOM_CUSTOM.equals("UTE")) {
                            commands = new String[]{"cd vendor/bin", "LD_LIBRARY_PATH=/vendor/lib64/hw KmInstallKeybox test_keybox.xml "+ "58231930301001" +" false"};
                        } else */if(Utilities.getBuildProject().equals("SQ51S")) {
                            commands = new String[]{"cd vendor/bin", "LD_LIBRARY_PATH=/vendor/lib64/hw KmInstallKeybox test_keybox.xml "+ "68241936000001" +" false"};
                        } else if(Utilities.getBuildProject().equals("SQ45")) {
                            commands = new String[]{"cd vendor/bin", "LD_LIBRARY_PATH=/vendor/lib64/hw KmInstallKeybox test_keybox.xml "+ "Urovo_DT40_00000001" +" false"};
                        } else if(FactoryKitPro.PRODUCT_SQ83) {
                            commands = new String[]{"cd vendor/bin", "LD_LIBRARY_PATH=/vendor/lib64/hw KmInstallKeybox test_keybox.xml "+ "866123043782385" +" false"};
                        } else {
                            commands = new String[]{"cd vendor/bin", "LD_LIBRARY_PATH=/vendor/lib64/hw KmInstallKeybox test_keybox.xml "+ googleKeyDeviceID +" false"};
                        }
    			CommandResult mCommandResult = ShellUtils.execCommand(commands, false , true);
                        Log.d(TAG,"commands = "+commands[1]);
    			Log.d(TAG,"mCommandResult.result="+mCommandResult.result+", successMsg="+mCommandResult.successMsg+", errorMsg="+mCommandResult.errorMsg);
    			if(mCommandResult.successMsg != null){
    				//toast(mCommandResult.successMsg.toString());
    			}
    			if(mCommandResult.errorMsg != null){
    				//toast(mCommandResult.errorMsg.toString());
    			}
    			if(mCommandResult.result == 0 ){
    				ret = true;
    				//toast(getString(R.string.key_success));
    			}else{
    				ret = false;
                    Log.d(TAG,"google key write fail!!!please check selinux right !!!");
    				//toast(getString(R.string.key_failed));
    			}
    		}
    					        
            if (ret) {
                SystemProperties.set("persist.sys.googlekey", "1");
                pass();
            }
            else {
                fail(null);
            }
        }
    }

    void fail(Object msg) {

        loge(msg);
        setResult(RESULT_CANCELED);
        resultString=Utilities.RESULT_FAIL;
        finish();
    }

    void pass() {
        setResult(RESULT_OK);
        resultString=Utilities.RESULT_PASS;
        finish();
    }

    /*public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(getApplicationContext(), s + "", Toast.LENGTH_SHORT).show();
    }*/

    private void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(TAG, e + "");
    }

    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }

    private void getGoogkeyNameString() {
        String configFile = null;

        File temp = new File(googleKeyFilePath);
        if (temp.exists() && temp.canRead())
            configFile = googleKeyFilePath;


        if (configFile == null) {
            return;
        }

        XmlPullParser xmlPullParser = null;
        if (configFile != null) {
            XmlPullParserFactory xmlPullParserFactory;
            try {
                xmlPullParserFactory = XmlPullParserFactory.newInstance();
                xmlPullParserFactory.setNamespaceAware(true);
                xmlPullParser = xmlPullParserFactory.newPullParser();
                FileInputStream fileInputStream = new FileInputStream(configFile);
                xmlPullParser.setInput(fileInputStream, "utf-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            int mEventType = xmlPullParser.getEventType();
            /** Parse the xml */
            while (mEventType != XmlPullParser.END_DOCUMENT) {
                if (mEventType == XmlPullParser.START_TAG) {
                    String name = xmlPullParser.getName();

                    if (name.equals("Keybox")) {
                        googleKeyDeviceID =  xmlPullParser.getAttributeValue(null, "DeviceID");
                        Log.d(TAG,"googleKeyDeviceID =" +googleKeyDeviceID);
                    }
                }
                mEventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            loge(e);
        }
    }

}
