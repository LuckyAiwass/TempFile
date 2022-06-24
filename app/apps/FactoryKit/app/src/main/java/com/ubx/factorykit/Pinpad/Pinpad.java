package com.ubx.factorykit.Pinpad;

import android.device.IccManager;
import android.os.Bundle;
import android.app.Activity;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import android.device.SEManager;

import java.util.List;

import android.os.ConditionVariable;

import com.ubx.factorykit.Framework.Framework;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class Pinpad extends Activity {

    String TAG = "Pinpad";
    SEManager mSEManager;
    private static int listen_result;
    private static ConditionVariable mCV = new ConditionVariable();
    private boolean flag = false;
    private byte[] data = {0x0, 0x4, 0xc, 0x10, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x0, (byte) 0x00, 0x3c};

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(Framework.orientation);
        setContentView(R.layout.pinpad);
        mSEManager = new SEManager();
        TextView pinpad_hint = (TextView) findViewById(R.id.pinpad_hint);
        pinpad_hint.setText("start pinpad dialog");
        Log.d(TAG,SystemProperties.get("persist.sys.check.tp.switch"));
        if (!SystemProperties.get("persist.sys.check.tp.switch", "false").equals("true")) {
            Log.d(TAG,"Does not test TP switch");
        } else {
            if (!android.os.SystemProperties.get("urv.hw.tpsw", "false").equals("true")) {
                fail(getString(R.string.pinpad_fail_message));
            }
        }


    }

    ;

    public int pinBlockGet(byte[] data) {
        int ret;
        byte key_n, money_num, mark, cardlen, min_len, max_len, mode, type;
        int uiWaitSec;
        byte[] money = new byte[50];
        byte[] out_ksn = new byte[10];
        byte[] indata = new byte[300];
        byte[] SW = new byte[2];
        byte[] OfflineEncPin = new byte[256];

        key_n = data[0];
        min_len = data[1];
        max_len = data[2];
        cardlen = data[3];

        byte[] card_no = new byte[cardlen];
        System.arraycopy(data, 4, card_no, 0, cardlen);
        mode = data[4 + cardlen];

        uiWaitSec = ((data[5 + cardlen] & 0xff) << 8) | (data[6 + cardlen] & 0xff);

        mSEManager.open();

        String len_limit = Byte.toString(min_len) + ",5,6,7,8,9,10,11," + Byte.toString(max_len);
        String cardNumStr = new String(card_no);

        logd("Lib_PinBlockGet key_n=" + key_n + " len_limit=" + len_limit + " cardno=" + cardNumStr);
        Bundle paramVar = new Bundle();

        int pinAlgMode = 0;
        paramVar.putInt("inputType", 0);
        paramVar.putInt("KeyUsage", 2);
        paramVar.putInt("PINKeyNo", 0);
        paramVar.putInt("pinAlgMode", pinAlgMode);
        paramVar.putString("cardNo", cardNumStr);
        paramVar.putBoolean("sound", true);
        paramVar.putBoolean("onlinePin", true);
        paramVar.putBoolean("FullScreen", true);
        paramVar.putBoolean("voicePrompt", true);
        if (!SystemProperties.get("persist.sys.check.tp.switch", "false").equals("true")) {
            paramVar.putBoolean("inputBySP", false);
        } else {
            paramVar.putBoolean("inputBySP", true);
        }
        paramVar.putLong("timeOutMS", 10 * 1000);
        paramVar.putString("title", "Security Keyboard");
        paramVar.putString("message", "Please input pin and cover by hand\n");
        paramVar.putString("supportPinLen", "0,4,5,6,7,8,9,10,11,12");
        AdminInputListen mPedAdminInput = new AdminInputListen();

        mSEManager.getPinBlockEx(paramVar, mPedAdminInput);
        mCV.block();
        mCV.close();
        ret = listen_result;
        mSEManager.close();
        showDialog();
        return ret;

    }

    @Override
    protected void onResume() {
        pinBlockGet(data);
        super.onResume();
    }


    private class AdminInputListen extends android.os.IInputActionListener.Stub {
        public void onInputChanged(int result, int len, Bundle bundle) {
            logd("CB_EVENT_ADMIN result: " + result + " len " + len);
            listen_result = result;

            if (null != mCV) ;
            mCV.open();
        }
    }

    private void showDialog() {

        new AlertDialog.Builder(Pinpad.this).setMessage(R.string.pinpad_dialog_message)
                .setPositiveButton(R.string.yes, passListener).setNegativeButton(R.string.no, failListener).show();
    }


    DialogInterface.OnClickListener passListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            pass();
        }
    };

    DialogInterface.OnClickListener failListener = new DialogInterface.OnClickListener() {

        public void onClick(DialogInterface dialog, int which) {
            fail(null);
        }
    };

    void fail(Object msg) {
        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG, "Failed");
        finish();
    }

    void pass() {
        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG, "Pass");
        finish();
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }

    private void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(TAG, e + "");
    }

    @SuppressWarnings("unused")
    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }
}
