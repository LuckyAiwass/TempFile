/*
 * Copyright 2014 Urovo, Ltd.
 */

package com.ubx.factorykit.Print;

import com.ubx.factorykit.Util;
import com.ubx.factorykit.Utilities;

import android.device.PrinterManager;
import android.net.Uri;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.factorykit.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrinterManagerActivity extends Activity {
    private static final String TAG = "Printer";
    private static String resultString = Utilities.RESULT_FAIL;
    private Button mBtnPrnBill;
    private Button mBtnPrnPic;
    private Button mBtnPrnBarcode;
    private Button mBtnForWard;
    private Button mBtnBack;
    private EditText printInfo;

    private final static String PRNT_ACTION = "urovo.prnt.message";
    PrinterManager printer = new PrinterManager();

    private final static String STR_PRNT_BILL = "prn_bill";

    // Temperature
    private TextView mTvTemp;

    // Product Testing
    private boolean mIsFactoryTest = false;
    private LinearLayout mPrnPicture;
    private LinearLayout mPrnBill;

    private CheckBox mCbFactoryTest;

    private FontStylePanel mFontStylePanel;
    private Bundle mFontInfo;
    private Bitmap mBmpPicture;
    private final int DEF_TEMP_THROSHOLD = 50;
    private int mTempThresholdValue = DEF_TEMP_THROSHOLD;

    private int mVoltTempPair[][] = {
            {898, 80},
            {1008, 75},
            {1130, 70},
            {1263, 65},
            {1405, 60},
            {1555, 55},
            {1713, 50},
            {1871, 45},
            {2026, 40},
            {2185, 35},
            {2335, 30},
            {2475, 25},
            {2605, 20},
            {2722, 15},
            {2825, 10},
            {2915, 5},
            {2991, 0},
            {3054, -5},
            {3107, -10},
            {3149, -15},
            {3182, -20},
            {3209, -25},
            {3231, -30},
            {3247, -35},
            {3261, -40},
    };

    private static final String[] mTempThresholdTable = {
            "80", "75", "70", "65", "60",
            "55", "50", "45", "40", "35",
            "30", "25", "20", "15", "10",
            "5", "0", "-5", "-10", "-15",
            "-20", "-25", "-30", "-35", "-40",
    };

    private Spinner mSpinerThreshold;
    private ArrayAdapter<String> mThresholdAdapter;

    private final static String SPINNER_PREFERENCES_FILE = "SprinterPrefs";
    private final static String SPINNER_SELECT_POSITION_KEY = "spinnerPositions";
    private final static int DEF_SPINNER_SELECT_POSITION = 6;
    private final static String SPINNER_SELECT_VAULE_KEY = "spinnerValue";
    private final static String DEF_SPINNER_SELECT_VAULE = mTempThresholdTable[DEF_SPINNER_SELECT_POSITION];

    private int mSpinnerSelectPosition;
    private String mSpinnerSelectValue;

    private final static int DEF_PRINTER_HUE_VALUE = 0;
    private final static int MIN_PRINTER_HUE_VALUE = 0;
    private final static int MAX_PRINTER_HUE_VALUE = 150;

    private final static int DEF_PRINTER_SPEED_VALUE = 9;
    private final static int MIN_PRINTER_SPEED_VALUE = 0;
    private final static int MAX_PRINTER_SPEED_VALUE = 9;

    private Button mBtSetHue;
    private EditText mEtHue;
    private Button mBtSetSpeed;
    private EditText mEtSpeed;
    private int mPrinterHue = DEF_PRINTER_HUE_VALUE;
    private int mPrinterSpeed = 0;

    private final int FACTORYTEST_ON = 1;
    private final int FACTORYTEST_OFF = 0;
    private final String DEF_FACTORYTEST_CONTENT_VALUE = "1    2    3";

    private BroadcastReceiver mPrtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            int ret = intent.getIntExtra("ret", 0);
            if (ret == -1) {
                Toast.makeText(PrinterManagerActivity.this, R.string.tst_info_paper, Toast.LENGTH_SHORT).show();
            } else if (ret == -2) {
                Toast.makeText(PrinterManagerActivity.this, R.string.tst_info_temperature, Toast.LENGTH_SHORT).show();
            } else if (ret == -3) {
                Toast.makeText(PrinterManagerActivity.this, R.string.tst_info_voltage, Toast.LENGTH_SHORT).show();
            }
            mBtnPrnBill.setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            setContentView(R.layout.printer);
        mBtnPrnBill = (Button) findViewById(R.id.btn_prnBill);
        printInfo = (EditText) findViewById(R.id.printer_info);
        mPrnPicture = (LinearLayout) findViewById(R.id.ll_picture);
        mPrnBill = (LinearLayout) findViewById(R.id.ll_bill);
        /* Set hue: 0 ~ 4, default is 0 */
        mEtHue = (EditText) findViewById(R.id.et_hue);
        mBtSetHue = (Button) findViewById(R.id.bt_set_hue);
        mBtSetHue.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Convert.isNumeric(mEtHue.getText().toString(), PrinterManagerActivity.this)) {
                    try {
                        mPrinterHue = Integer.parseInt(mEtHue.getText().toString());
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    mPrinterHue = DEF_PRINTER_HUE_VALUE;
                }

                if (mPrinterHue < 0 || mPrinterHue < MIN_PRINTER_HUE_VALUE ||
                        mPrinterHue > MAX_PRINTER_HUE_VALUE) {
                    mPrinterHue = DEF_PRINTER_HUE_VALUE;
                }

                if (printer == null) {
                    printer = new PrinterManager();
                }
                Log.d("Hz", "---------set hue = " + mPrinterHue);
                printer.prn_setBlack(mPrinterHue);
                mEtHue.setText(String.valueOf(mPrinterHue));
            }
        });

        /* Set speed: 0 ~ 9, default is 9 */
        mEtSpeed = (EditText) findViewById(R.id.et_speed);
        mBtSetSpeed = (Button) findViewById(R.id.bt_set_speed);
        mBtSetSpeed.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Convert.isNumeric(mEtSpeed.getText().toString(), PrinterManagerActivity.this)) {
                    try {
                        mPrinterSpeed = Integer.parseInt(mEtSpeed.getText().toString());
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else {
                    mPrinterSpeed = DEF_PRINTER_SPEED_VALUE;
                }

                if (mPrinterSpeed < MIN_PRINTER_SPEED_VALUE || mPrinterSpeed > MAX_PRINTER_SPEED_VALUE) {
                    mPrinterSpeed = DEF_PRINTER_SPEED_VALUE;
                }

                if (printer == null) {
                    printer = new PrinterManager();
                }

                Log.d("Hz", "---------set PrinterSpeed = " + mPrinterSpeed);
                printer.prn_setSpeed(mPrinterSpeed);
                mEtSpeed.setText(String.valueOf(mPrinterSpeed));
            }
        });

        mBtnPrnBill.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String messgae = printInfo.getText().toString();
                mBtnPrnBill.setEnabled(false);
                int ret = printer.prn_getStatus();
                if (ret == 0) {
                    if (messgae.length() > 0) {
                        doprintwork(messgae + "\n\n\n\n\n\n");
                        printer.prn_paperForWard(150);

                    } else {
                        doprintwork(STR_PRNT_BILL);
                    }
                } else {
                    Intent intent = new Intent("urovo.prnt.message");
                    intent.putExtra("ret", ret);
                    sendBroadcast(intent);
                }
            }
        });

        mBtnForWard = (Button) findViewById(R.id.btn_FORWARD);
        mBtnForWard.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkTempThreshold())
                    return;
                printer.prn_paperForWard(50);
            }
        });

//        mBtnBack = (Button) findViewById(R.id.btn_BACK);
//        mBtnBack.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (checkTempThreshold())
//                    return;
//                printer.prn_paperBack(50);
//            }
//        });

        mBtnPrnPic = (Button) findViewById(R.id.btn_prnPicture);
        mBtnPrnPic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBmpPicture = null;
                new AlertDialog.Builder(PrinterManagerActivity.this)
                        .setTitle(R.string.tst_info_select_picture)
                        .setMessage(R.string.tst_info_select_picture_msg)
                        .setNegativeButton(R.string.mci_select_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBtnPrnPic.setEnabled(false);

                                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, PHOTO_REQUEST_CODE);
                            }
                        })
                        .setPositiveButton(R.string.mci_select_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mBtnPrnPic.setEnabled(false);

                                BitmapFactory.Options opts = new BitmapFactory.Options();
                                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                opts.inDensity = getResources().getDisplayMetrics().densityDpi;
                                opts.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
                                mBmpPicture = BitmapFactory.decodeResource(getResources(), R.drawable.hcp, opts);
                                doPrint(2);
                                printer.prn_paperForWard(200);
                                mBtnPrnPic.setEnabled(true);
                            }
                        })
                        .create()
                        .show();
            }
        });

        mBtnPrnBarcode = (Button) findViewById(R.id.btn_barcode);
        mBtnPrnBarcode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String messgae = printInfo.getText().toString();

                if (messgae.length() > 0) {
                    doPrint(1);
                    printer.prn_paperForWard(300);
                } else {
                    Toast.makeText(PrinterManagerActivity.this, R.string.tst_hint_content, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mCbFactoryTest = (CheckBox) findViewById(R.id.cb_factoryTest);
        mCbFactoryTest.setChecked(Util.SMT);
        updateStatu(Util.SMT);
        mCbFactoryTest.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                mIsFactoryTest = isChecked;
                updateStatu(mIsFactoryTest);
                printer.prn_setFactoryTest(mIsFactoryTest ? FACTORYTEST_ON : FACTORYTEST_OFF);
            }

        });

            Log.v("tao.he", "Fonts style setting");
        mFontStylePanel = new FontStylePanel(this);
        mSpinerThreshold = (Spinner) findViewById(R.id.spinner_threshold);
        mThresholdAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mTempThresholdTable);
            mSpinerThreshold.setAdapter(mThresholdAdapter);
            mSpinerThreshold.setOnItemSelectedListener(new SpinnerSelectedListener());
    }

    public void updateStatu(boolean flag) {
//        if (flag) {
//            mPrnPicture.setVisibility(View.GONE);
//            // mPrnBill.setVisibility(View.GONE);
//            mBtnPrnBarcode.setVisibility(View.GONE);
//            printInfo.setText(DEF_FACTORYTEST_CONTENT_VALUE);
//        } else {
//            mPrnPicture.setVisibility(View.VISIBLE);
//            // mPrnBill.setVisibility(View.VISIBLE);
//            mBtnPrnBarcode.setVisibility(View.VISIBLE);
//            printInfo.setText("");
//        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (printer == null) {
            printer = new PrinterManager();
        }

//		printer.prn_setSpeed(DEF_PRINTER_SPEED_VALUE);
//		printer.prn_setBlack(DEF_PRINTER_HUE_VALUE);

        printer.prn_setFactoryTest(FACTORYTEST_OFF);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mPrtReceiver);
        writeSpinnerPrefsState(this);
        printer.prn_setFactoryTest(FACTORYTEST_OFF);
    }

    private boolean hasChineseChar(String text) {
        boolean hasChar = false;
        int length = text.length();
        int byteSize = text.getBytes().length;
        hasChar = (length != byteSize);
        return hasChar;
    }

    void doprintwork(String msg) {
        if (checkTempThreshold())
            return;

        Intent intentService = new Intent(this, PrintBillService.class);
        intentService.putExtra("SPRT", msg);
        if (!msg.equals(STR_PRNT_BILL)) {
            intentService.putExtra("font-info", mFontStylePanel.getFontInfo());
        }
        startService(intentService);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        //printer.prn_setFactoryTest(mIsFactoryTest ? FACTORYTEST_ON : FACTORYTEST_OFF);
        printer.prn_setFactoryTest(Util.SMT ? FACTORYTEST_ON : FACTORYTEST_OFF);
        IntentFilter filter = new IntentFilter();
        filter.addAction(PRNT_ACTION);
        registerReceiver(mPrtReceiver, filter);
        readSpinnerPrefsState(this);
            mSpinerThreshold.setSelection(mSpinnerSelectPosition);
           // mTvTemp.setText(String.valueOf(getCurrentTemp()));
        mBtnPrnBill.setEnabled(true);
        mBtnPrnPic.setEnabled(true);
        mBtnPrnBarcode.setEnabled(true);
        mBtnForWard.setEnabled(true);
        //mBtnBack.setEnabled(true);
    }

    public void onPass(View view) {
        setResult(RESULT_OK);
        resultString = Utilities.RESULT_PASS;
        finish();
    }

    public void onFail(View view) {
        setResult(RESULT_CANCELED);
        resultString = Utilities.RESULT_FAIL;
        finish();
    }

    void doPrint(int type) {
        if (checkTempThreshold())
            return;
        int ret = printer.prn_getStatus();
        if (ret == 0) {
            printer.prn_setupPage(384, -1);
            switch (type) {
                case 1:
                    String text = printInfo.getText().toString();
                    if (hasChineseChar(text)) {
                        printer.prn_drawBarcode(text, 50, 10, 58, 8, 120, 0);
                        printer.prn_drawTextEx("\n\n\n\n", 0, 200, 300, -1, "arial", 24, 0, 0x0002 | 0x0004, 0);

                    } else {
                        printer.prn_drawBarcode(text, 196, 300, 20, 2, 70, 0);

                        printer.prn_drawBarcode(text, 196, 300, 20, 2, 70, 1);

                        printer.prn_drawBarcode(text, 196, 300, 20, 2, 70, 2);

                        printer.prn_drawBarcode(text, 196, 300, 20, 2, 70, 3);
                        doprintwork("\n");
                    }
                    break;

                case 2:
                    if (mBmpPicture != null) {
                        printer.prn_drawBitmap(mBmpPicture, 30, 0);
                    } else {
                        Toast.makeText(this, "mBmpPicture is null", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case 3:
                    printer.prn_drawLine(264, 50, 48, 50, 4);
                    printer.prn_drawLine(156, 0, 156, 120, 2);
                    printer.prn_drawLine(16, 0, 300, 100, 2);
                    printer.prn_drawLine(16, 100, 300, 0, 2);
                    break;
            }

            ret = printer.prn_printPage(0);
            if (type == 1 || type == 2) {
                printer.prn_paperForWard(13);
            }
        }
        Intent intent = new Intent("urovo.prnt.message");
        intent.putExtra("ret", ret);
        this.sendBroadcast(intent);
    }

    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    private static final int PHOTO_REQUEST_CODE = 200;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    //通过uri的方式返回，部分手机uri可能为空
                    Bitmap bitmap = null;
                    if (uri != null) {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        //部分手机可能直接存放在bundle中
                        Bundle bundleExtras = data.getExtras();
                        if (bundleExtras != null) {
                            bitmap = bundleExtras.getParcelable("data");
                        }
                    }
                    if (bitmap != null) {
                        mBmpPicture = Bitmap.createScaledBitmap(bitmap,
                                300, 300 * bitmap.getHeight() / bitmap.getWidth(), true);
                        doPrint(2);
                    }
                    printer.prn_paperForWard(4);
                    mBtnPrnPic.setEnabled(true);
                }
                break;
        }
    }

    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(
                AdapterView<?> arg0,
                View arg1, int arg2,
                long arg3) {

            mTempThresholdValue = Integer.parseInt(mTempThresholdTable[arg2]);
            // prepare prefs and write it to files
            mSpinnerSelectPosition = (int) arg3;
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }

    // read prefs to restore
    private boolean readSpinnerPrefsState(Context c) {
        SharedPreferences sharedPrefs = c.getSharedPreferences(SPINNER_PREFERENCES_FILE, MODE_PRIVATE);
        mSpinnerSelectPosition = sharedPrefs.getInt(SPINNER_SELECT_POSITION_KEY, DEF_SPINNER_SELECT_POSITION);
        mSpinnerSelectValue = sharedPrefs.getString(SPINNER_SELECT_VAULE_KEY, DEF_SPINNER_SELECT_VAULE);
        return (sharedPrefs.contains(SPINNER_SELECT_POSITION_KEY));
    }

    // write prefs to file for restroing
    private boolean writeSpinnerPrefsState(Context c) {
        SharedPreferences sharedPrefs = c.getSharedPreferences(SPINNER_PREFERENCES_FILE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(SPINNER_SELECT_POSITION_KEY, mSpinnerSelectPosition);
        editor.putString(SPINNER_SELECT_VAULE_KEY, mSpinnerSelectValue);
        return (editor.commit());
    }

    // return ture if printer's temperature is too high
    private boolean checkTempThreshold() {
		/* // used on platfrom 4.3
		if(Build.PROJECT.equals("SQ27T")||Build.PROJECT.equals("SQ27TC")||Build.PROJECT.equals("SQ27TE")||Build.PROJECT.equals("SQ27TD")){
			return false;  //去掉打印机温度检测
		}

		int currentTemp = getCurrentTemp();
		if(! Build.PROJECT.equals("SQ27T")&& !Build.PROJECT.equals("SQ27TC")&& !Build.PROJECT.equals("SQ27TE")&& !Build.PROJECT.equals("SQ27TD"))
			mTvTemp.setText(String.valueOf(currentTemp));

		if(currentTemp >= mTempThresholdValue){
			Log.e(TAG, "Printer temperature meets the Threshold: " + mTempThresholdValue);
			Toast.makeText(getApplicationContext(),
					R.string.printer_temp_overheating,
					Toast.LENGTH_SHORT).show();
			return true;
		}
		*/
        return false;
    }

    private int getCurrentTemp() {
        if (printer == null) {
            printer = new PrinterManager();
        }

        int currentTempVolt = printer.prn_getTemp();

//		Log.d("Hz", "---------currentTempVolt---------" + currentTempVolt);

        String tmp = String.valueOf(currentTempVolt);
        // get first 4# or first 3#
        if (tmp.length() >= 4) {
            if (tmp.length() == 4 || tmp.length() == 6) {        // when temperature equals 80
                currentTempVolt = Integer.parseInt(tmp.substring(0, 3));
            } else {
                currentTempVolt = Integer.parseInt(tmp.substring(0, 4));
            }
        } else {
            currentTempVolt = 0;
        }

//		Log.d("Hz", "getCurrentTemp =============== " + currentTempVolt);
        if (currentTempVolt < 0)
            currentTempVolt = 0;
        return voltToTemp(mVoltTempPair, currentTempVolt);
    }

    private int voltToTemp(int[][] table, int voltValue) {
        int left_side = 0;
        int right_side = table.length - 1;
        int mid;

        int realTemp = 0;

        while (left_side <= right_side) {
            mid = (left_side + right_side) / 2;

            if (mid == 0 || mid == table.length - 1 ||
                    (table[mid][0] <= voltValue && table[mid + 1][0] > voltValue)) {
                realTemp = table[mid][1];
                break;
            } else if (voltValue - table[mid][0] > 0)
                left_side = mid + 1;
            else
                right_side = mid - 1;
        }

        return realTemp;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.printer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            final boolean[] defaultSelectedStatus = {false};

            new AlertDialog.Builder(PrinterManagerActivity.this).setTitle(R.string.mci_select_title).setIcon(
                    android.R.drawable.ic_dialog_info).setMultiChoiceItems(
                    new String[]{PrinterManagerActivity.this.getResources().getString(R.string.mci_select_content)},
                    new boolean[]{mIsFactoryTest},
                    new DialogInterface.OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            // TODO Auto-generated method stub

                            defaultSelectedStatus[which] = isChecked;
                        }
                    }).setPositiveButton(
                    R.string.mci_select_ok,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            for (int i = 0; i < defaultSelectedStatus.length; i++) {
                                mIsFactoryTest = defaultSelectedStatus[i];
                            }
//									Log.i("Hz", "--------- mIsFactoryTest --------" + mIsFactoryTest);

                            if (mIsFactoryTest) {
                                mPrnPicture.setVisibility(View.GONE);
                                mPrnBill.setVisibility(View.GONE);
                                mCbFactoryTest.setChecked(true);
                                printInfo.setText(DEF_FACTORYTEST_CONTENT_VALUE);
                            } else {
                                mPrnPicture.setVisibility(View.VISIBLE);
                                mPrnBill.setVisibility(View.VISIBLE);
                                mCbFactoryTest.setChecked(false);
                                printInfo.setText("");
                            }

                            printer.prn_setFactoryTest(mIsFactoryTest ? FACTORYTEST_ON : FACTORYTEST_OFF);
                        }
                    }).setNegativeButton(R.string.mci_select_cancel, null).show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
