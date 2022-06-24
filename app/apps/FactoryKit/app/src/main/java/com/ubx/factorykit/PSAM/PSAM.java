package com.ubx.factorykit.PSAM;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.ubx.factorykit.Framework.FactoryKitPro;
import com.ubx.factorykit.R;
import com.qualcomm.qti.sam.manager.PsamInterfaceManger;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.device.IccManager;


public class PSAM extends Activity {
    private EditText mNo;
    private boolean fflag = false;
    private static PsamInterfaceManger mPsamManger;

    private static final String TAG = "PSAM";
    private Button read1;
    private Button read2;
    private boolean isReading = false;
    private RadioGroup radioGroup;
    private RadioButton radio1;
    private RadioButton radio2;
    private RadioButton radio3;
    private byte mVoltage = 3;
    private byte cardType = 1;
    private byte id = 1;
    private int ret = -1;
    private int slot_id = 1;
    private double volt_select = 3;
    public static byte[] sw;
    public static byte[] pAtr;
    public static byte[] apdu_resp;
    public static byte[] apdu_data;
    private EditText mFI = null;
    private EditText mDI = null;

    private IccManager mIccManager;
    private int mCardType = 0;
    private static final int CARD_TYPE_NONE = 0;
    private static final int CARD_TYPE_SIM = 1;
    private static final int CARD_TYPE_PSAM = 2;
    class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == radio1.getId()) {
                mVoltage = 3;
                volt_select = 3;
            } else if (checkedId == radio2.getId()) {
                mVoltage = 5;
                volt_select = 5;
            } else if (checkedId == radio3.getId()) {
                mVoltage = (byte) 1.8;
                volt_select = 1.8;
            }
            Log.i("onCheckedChanged", "-------onCheckedChanged--------------- mVoltage:" + volt_select);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ic);
		if(FactoryKitPro.PRODUCT_SQ47)
        mPsamManger = new PsamInterfaceManger(this);
		else
		mIccManager = new IccManager();
        String apduStr = "0084000004";
        apdu_data = hexStringtoBytes(apduStr);
        initView();

    }

    private void initView() {
        OnClickListener ocl = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == read1) {
                    id = 1;
                    if(FactoryKitPro.PRODUCT_SQ47) {
                        id = 0;
                    }
                    readSam(id);
                } else if (view == read2) {
                    id = 2;
                    slot_id = 2;
                    readSam(id);
                }

            }
        };

        LinearLayout readLayout = (LinearLayout) findViewById(R.id.read_layout);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1);
        read1 = new Button(this);
        read1.setLayoutParams(lp);
        read1.setOnClickListener(ocl);
//        String doubleSim = android.os.SystemProperties.get("persist.radio.multisim.config", "");
//        if (doubleSim.equals("dsds")) {
//            read1.setVisibility(View.GONE);
//        }

        readLayout.addView(read1);
        if (FactoryKitPro.isDoublePsam) {
            read1.setText("read SAM1");
            read2 = new Button(this);
            read2.setLayoutParams(lp);
            read2.setText("read SAM2");
            read2.setOnClickListener(ocl);
            readLayout.addView(read2);
            if (FactoryKitPro.PRODUCT_SQ29Z){
                read1.setVisibility(View.GONE);
            }
        } else {
            read1.setText("read SAM");
        }

        // SQ38 只有一张卡
        if(FactoryKitPro.PRODUCT_SQ38) read2.setVisibility(View.GONE);
        //SQ28 自适应
        if(FactoryKitPro.PRODUCT_SQ28) {
            mIccManager.open((byte) 2,(byte) 1,(byte) 3);
            mCardType = mIccManager.getCardType();
            Log.d(TAG, "mCardType = " + mCardType);
            if (mCardType != CARD_TYPE_PSAM) {
                read2.setVisibility(View.GONE);
            }
            mIccManager.close();
        }
        mNo = (EditText) findViewById(R.id.icc);
        mNo.setText("");
        radioGroup = (RadioGroup) findViewById(R.id.radioGroupID);
        radio1 = (RadioButton) findViewById(R.id.radio1);
        radio2 = (RadioButton) findViewById(R.id.radio2);
        radio3 = (RadioButton) findViewById(R.id.radio3);
        radioGroup.setOnCheckedChangeListener(new RadioGroupListener());
        mFI = (EditText) findViewById(R.id.urovo_fi);
        mDI = (EditText) findViewById(R.id.urovo_di);

        Button pass = (Button) findViewById(R.id.pass);
        pass.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                pass();
            }
        });

        Button fail = (Button) findViewById(R.id.fail);
        fail.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                fail(null);
            }
        });
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        fflag = true;
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    public void pass() {
        setResult(RESULT_OK);
        finish();
    }

    public void fail(Object msg) {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        fflag = false;
    }

    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] hexStringtoBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

    /*
    * Convert byte[] to hex
    * string.这里我们可以将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
    *
    * @param src byte[] data
    *
    * @return hex string
    */
    public String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private boolean onDataReceivedF(final byte[] content, final String type) {
        Log.i("mIccManager", "-------onDataReceivedF---------------");
        if (fflag == true)
            return false;

        runOnUiThread(new Runnable() {
            public void run() {
                if (content != null) {
                    String contenta = bytesToHexString(content);
                    if (contenta == null) {

                    } else {
                        int len = contenta.length();
                        mNo.append(type);
                        mNo.append(contenta);
                        mNo.append("\n");
                    }
                }
            }
        });
        return true;
    }

    private boolean onDataReceivedF(final String content) {

        if (fflag)
            return false;

        runOnUiThread(new Runnable() {
            public void run() {
                if (content == null) {

                } else {
                    mNo.append(content);
                    mNo.append("\n");
                }
            }
        });
        return true;
    }

    public void readSam(byte id) {
        if (isReading) return;
        final byte slot = id;
        int urovo_fi_num = 12;
        int urovo_di_num = 8;
        apdu_resp = new byte[1024];
        sw = new byte[2];

        final int fi = urovo_fi_num;
        final int di = urovo_di_num;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                isReading = true;
                Log.i(TAG, "-------run---------------");
                byte vol = mVoltage;
				if(FactoryKitPro.PRODUCT_SQ47)
        		ret = mPsamManger.open(slot, cardType, vol);
				else
                ret = mIccManager.open(slot, cardType, vol);
                Log.i(TAG, "------open-ret-------->" + ret);
                if (ret != 0) {
                    isReading = false;
                    return;
                }

                pAtr = new byte[124];
                String activate_str;
                byte[] test_atr;
				if(FactoryKitPro.PRODUCT_SQ47)
        		ret = mPsamManger.activate(pAtr);
				else
                ret = mIccManager.activate(pAtr);
                Log.i(TAG, "-------activate, ret--------->" + ret);
                if (slot == 2 && ret <= 0) {
                    for (int i = 0; i < 3; i++) {
						if(FactoryKitPro.PRODUCT_SQ47)
        				ret = mPsamManger.activate(pAtr);
						else
                        ret = mIccManager.activate(pAtr);
                        if (ret > 0) {
                            test_atr = new byte[ret];
                            System.arraycopy(pAtr, 0, test_atr, 0, ret);
                            activate_str = "ATR:" +bytesToHexString(test_atr) ;
                            if (i == 0) {
                                volt_select = 1.8;
                            } else if (i == 1) {
                                volt_select = 3;
                            } else if (i == 2) {
                                volt_select = 5;
                            }
                            Log.i(TAG, "-------readSam--------------mVoltage:" + volt_select + ",fi:" + fi + ",di:" + di);
                            break;
                        }
                    }
                } else {
                    Log.i(TAG, "-------readSam--------------- mVoltage:" + volt_select + ",fi:" + fi + ",di:" + di);
                }

                if (ret > 0) {
                    test_atr = new byte[ret];
                    System.arraycopy(pAtr, 0, test_atr, 0, ret);
                    activate_str = "ATR:" +bytesToHexString(test_atr) ;
                    Log.i(TAG, "activate_str ------------> " + activate_str);
                } else {
                    activate_str = " getCardAtr failed!";
                    onDataReceivedF(activate_str);
                    isReading = false;
					if(FactoryKitPro.PRODUCT_SQ47)
        			mPsamManger.close();
					else
                    mIccManager.close();
                    return;
                }

                Log.i(TAG, "activate ------------> " + activate_str);
				if(FactoryKitPro.PRODUCT_SQ47)
                ret = mPsamManger.detect();
                else
                ret = mIccManager.detect();
                Log.i(TAG, "detect ------------> " + ret);
                if (ret != 0) {
                    Log.d(TAG, "readSam , detect failed!!!! , ret:");
                    onDataReceivedF("detect failed!!!!");
                    isReading = false;
					if(FactoryKitPro.PRODUCT_SQ47)
                    mPsamManger.close();
                    else
                    mIccManager.close();
                    return;
                }

                for (int i = 0; i < pAtr.length && pAtr.length < 33; i++) {
                    android.util.Log.i("mIccManager", "----------------pAtr[" + i + "]=" + pAtr[i]);
                }

                if (FactoryKitPro.isDoublePsam) {
                    onDataReceivedF(test_atr, "SAM" + (slot_id) + "-ATR(" + volt_select + "): ");
                } else {
                    onDataReceivedF(test_atr, "SAM" + "-ATR: ");
                }

//                int param_type = 0;
//                byte[] test_psam_data;
//                byte[] get_param_data = new byte[8];
//                String get_psam_params;
//                if(!FactoryKitPro.PRODUCT_SQ47) {
//                    ret = mIccManager.Psam_GetParam(param_type, get_param_data);
//                    Log.i(TAG, "Baudrate ------------ " + ret);
//                    if (ret > 0) {
//                        test_psam_data = new byte[ret];
//                        System.arraycopy(get_param_data, 0, test_psam_data, 0, ret);
//                        get_psam_params = "Baudrate:" + bytesToHexString(test_psam_data);
//                    } else {
//                        get_psam_params = "get Baudrate failed!";
//                        onDataReceivedF(get_psam_params);
//                        isReading = false;
//                        mIccManager.close();
//                    }
//                }
				if(FactoryKitPro.PRODUCT_SQ47)
                ret = mPsamManger.apduTransmit(apdu_data, apdu_data.length, apdu_resp, sw);
                else
                ret = mIccManager.apduTransmit(apdu_data, apdu_data.length, apdu_resp, sw);
                Log.d(TAG,"apduTransmit==" + ret);

                byte[] test_apdu_data;
                String stream_apdu;
                if (ret > 0) {
                    test_apdu_data = new byte[ret];
                    System.arraycopy(apdu_resp, 0, test_apdu_data, 0, test_apdu_data.length);
                    StringBuilder activate = new StringBuilder();
                    activate.append("activate successful, and CardATR = ");
                    activate.append(bytesToHexString(test_apdu_data));

                    //stream_apdu = "Apdu : " +bytesToHexString(test_apdu_data) + "\nSW = " + bytesToHexString(sw)+"\n\n";
					stream_apdu = "Apdu: response = " + activate.toString() + ", SW = " + bytesToHexString(sw) +"\n" + getString(R.string.pass_psam);
                } else {
                    stream_apdu = "Apdu: response faild, ret = " + ret;
                }
                onDataReceivedF(stream_apdu);
				if(FactoryKitPro.PRODUCT_SQ47)
                mPsamManger.close();
                else
                mIccManager.close();
                isReading = false;
            }
        }).start();
    }

}
