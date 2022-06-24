
/*
 * Copyright (c) 2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.Framework;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.device.IccManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.LocaleData;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.factorykit.CameraFront.CameraFront;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;
import com.ubx.factorykit.Values;
import com.ubx.factorykit.Framework.MainApp.FunctionItem;

import org.codeaurora.telephony.utils.AsyncResult;//this is for 10.0
// 9.0 is import  android.os.AsyncResult
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
//import com.qualcomm.qcnvitems.QcNvItemIds;
import com.qualcomm.qcrilhook.QcRilHook;
import com.qualcomm.qcrilhook.QmiOemHook;
import com.qualcomm.qcrilhook.IQcRilHook;
import com.qualcomm.qti.sam.manager.PsamInterfaceManger;

import android.graphics.Color;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import static com.ubx.factorykit.Framework.FactoryKitPro.GOOGLE_KEY_MENU_DISPLAY;
import static com.ubx.factorykit.Framework.FactoryKitPro.IS_SQ47C;
import static com.ubx.factorykit.Framework.FactoryKitPro.IS_UROVO_VERSION;

public class Framework extends ListActivity {
    
    private final static String TAG = "Framework";
    private boolean mExitFlag = false;
    private Handler mHandler = new Handler();
    private LayoutInflater mInflater;
    Context mContext;
    String TempFile;
    private long curBackButtonTime = 0;
    private long lastBackButtonTime = 0;
    private int positionClicked = 1;
    final static int[] flagList = new int[99];
    final static int[] resultCodeList = new int[99];
    private static final int MENU_CLEAN_STATE = Menu.FIRST;
    private static final int MENU_UNINSTALL = Menu.FIRST + 1;
    private static final int MENU_SENSOR_CAL = Menu.FIRST + 2;
    private static final int MENU_EXIT = Menu.FIRST + 3;
    private List<FunctionItem> mFunctionItems = new ArrayList();

    private static final int CARD_TYPE_NONE = 0;
	private static final int CARD_TYPE_SIM = 1;
	private static final int CARD_TYPE_PSAM = 2;
    private Bitmap passBitmap;
    private Bitmap failBitmap;
    private boolean toStartAutoTest = false;
    private final int AUTO_TEST_TIME_INTERVAL = 900;
    private boolean originChargingStatus = true;
    TextView nvTextView;
    private static int initScreenTime;
    public static int orientation = 0;
    private static QcRilHook mQcRilOemHook;
    private static final String factoryResetTitle = "恢复出厂设置";

	private byte card = 1;
	private byte mVoltage = 3;
	private int cardType = 1;
    private String oldLanguageFlag;
    private AlertDialog.Builder alertD;
    private static final boolean DEBUG = true;
    String doubleSim = android.os.SystemProperties.get("persist.radio.multisim.config", "");
    // urovo yuanwei 测试结果 2019-05-27 begin
    private QcRilHook mQcRILHook;
    // urovo yuanwei 测试结果 2019-05-27 end
    //s hanzengqin 20181009
    private boolean isPassTestForFlag = true;
    private int nvCIT = 7239;
    //e
    private static PsamInterfaceManger mPsamManger;
    static {
        logd("Loading libqcomfm_jni.so");
        //System.loadLibrary("qcomfm_jni");
    }
    
    void init(Context context) {
        
        mContext = context;
        initScreenTime = Utilities.getScreenTimeout(mContext);
        mQcRilOemHook = new QcRilHook(context);
        lastBackButtonTime = 0;
        curBackButtonTime = 0;
        
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        orientation = getScreenOrientation();
		setRequestedOrientation(orientation);

        mInflater = LayoutInflater.from(context);
        
        passBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.test_pass);
        failBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.test_fail);
        
        /** nv factory_data_3 */
        if (Values.ENABLE_NV) {
        }
        
        // Write TestLog Start message
        Utilities.writeTestLog("\n=========Start=========", null);
        
        originChargingStatus = getChargingStatus();
        logd("originChargingStatus=" + originChargingStatus);

        // To save test time, enable some devices first
        // Utilities.enableWifi(mContext, true);
        // Utilities.enableBluetooth(true);
        // Utilities.enableGps(mContext, true);
        // Utilities.configScreenTimeout(mContext, 1800000); // 1 min
        // Utilities.configMultiSim(mContext);
        // configSoundEffects(false);
        // createShortcut(context);// add shortcut
        //mPsamManger = new PsamInterfaceManger(this);
        //cardType = getCardType((byte)0);
        //高通PSAM、SIM自适应
        if (FactoryKitPro.PRODUCT_SQ28){
            cardType = getCardType((byte)2);
            Log.d(TAG, "cardType = " + cardType);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mQcRILHook = ((MainApp)getApplicationContext()).getQcRilHook();
        alertD = new AlertDialog.Builder(this);
        if (ActivityManager.isUserAMonkey())
            finish();
        else {
            init(getApplicationContext());
            String hwPlatform = Utilities.getPlatform();
            if (FactoryKitPro.PRODUCT_SQ47)
            setTitle(getString(R.string.app_name) + " " + "RT40");
            else
            setTitle(getString(R.string.app_name) + " " + hwPlatform);
            logd(hwPlatform);
            // 加载测试项目
            loadItemList();
        }
        Boolean auto = getIntent().getBooleanExtra("state",false);
        if(auto) {
            toStartAutoTest = true;
            mHandler.postDelayed(mRunnable, AUTO_TEST_TIME_INTERVAL);
        }
    }

    private void loadItemList() {
		
        /** Get Test Items */
        FunctionItem functionItems = null;
        String configFile = null;
        String configFileExternalSD = Utilities.getExternalSDPath(mContext);
        //priority read External SD
        if(configFileExternalSD !=null){
            String tempPath = configFileExternalSD + "/item_config_factory.xml";
            File temp = new File(tempPath);
            if (temp.exists() && temp.canRead())
                configFile = tempPath;
        }
        if(configFile ==null) {
            for (String tmpConfigFile : Values.CONFIG_FILE_SEARCH_LIST) {
                File tmp = new File(tmpConfigFile);
                if (tmp.exists() && tmp.canRead()) {
                    configFile = tmpConfigFile;
                    logd("Found config file: " + tmpConfigFile);
                    break;
                }
            }
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

        } else {
            oldLanguageFlag = SystemProperties.get("persist.sys.locale", "");
            if(DEBUG) Log.d(TAG, "oldLanguageFlag---->" + oldLanguageFlag);
            
			//根据persist.sys.locale 切换配置文件
           /*if (oldLanguageFlag.equals("zh-TW") || oldLanguageFlag.equals("zh-CN") || oldLanguageFlag.equals("zh-HK") || oldLanguageFlag.equals("zh-Hans-CN")) {
                // 中文
				Log.d(TAG,"FactoryKitPro.PRODUCT_SQ53Q=="+FactoryKitPro.PRODUCT_SQ53Q);
                if (FactoryKitPro.PRODUCT_SQ53) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq53);
                } else if (FactoryKitPro.PRODUCT_SQ53Q) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq53q);
                } else if (FactoryKitPro.PRODUCT_SQ53C) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq53c);
                } else if (FactoryKitPro.PRODUCT_SQ51FW) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq51fw);
                } else if (FactoryKitPro.PRODUCT_SQ38) {
                    // SQ38 单双卡区分psam
                    if (doubleSim.equals("dsds")) {
                        xmlPullParser = getResources().getXml(R.xml.item_config_sq38);
                    }else if(doubleSim.equals("ssss")){
                        xmlPullParser = getResources().getXml(R.xml.item_config_sq38_sam);
                    }
                } else if (FactoryKitPro.PRODUCT_SQ45) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq45);
                } else if (FactoryKitPro.PRODUCT_SQ51S) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq51s);
                } else {
                    //中文默认配置文件
                    xmlPullParser = getResources().getXml(R.xml.item_config_default);
                }
            //}/* else {*/
                // 英文
                if (FactoryKitPro.PRODUCT_SQ53) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq53en);
                } else if (FactoryKitPro.PRODUCT_SQ53Q) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq53qen);
                } else if (FactoryKitPro.PRODUCT_SQ53C) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq53cen);
                } else if (FactoryKitPro.PRODUCT_SQ51FW) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq51fwen);
                } else if (FactoryKitPro.PRODUCT_SQ38) {
                    if (doubleSim.equals("dsds")) {
                        xmlPullParser = getResources().getXml(R.xml.item_config_sq38en);
                    }else if(doubleSim.equals("ssss")){
                        xmlPullParser = getResources().getXml(R.xml.item_config_sq38_sam_en);
                    }
                } else if (FactoryKitPro.PRODUCT_SQ45) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq45);
                } else if (FactoryKitPro.PRODUCT_SQ45S) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq45s);
                } else if (FactoryKitPro.PRODUCT_SQ51S) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq51sen);
                } else if (FactoryKitPro.PRODUCT_SQ47) {
                    if(IS_SQ47C)
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq47c);
                    else
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq47);
                }  else if (FactoryKitPro.PRODUCT_SQ29Z) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq29z);
                } else if (FactoryKitPro.PRODUCT_SQ28) {
                    if (oldLanguageFlag.equals("zh-TW") || oldLanguageFlag.equals("zh-CN") || oldLanguageFlag.equals("zh-HK") || oldLanguageFlag.equals("zh-Hans-CN")) {
                        xmlPullParser = getResources().getXml(R.xml.item_config_sq28);
                    }
                    else
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq28en);
                } else if (FactoryKitPro.PRODUCT_SQ53H) {
                    xmlPullParser = getResources().getXml(R.xml.item_config_sq53hen);
                }else {

                    //英文默认配置文件
                    xmlPullParser = getResources().getXml(R.xml.item_config_defaulten);
                }
            }

        //}
        try {
            int mEventType = xmlPullParser.getEventType();
            /** Parse the xml */
            while (mEventType != XmlPullParser.END_DOCUMENT) {
                if (mEventType == XmlPullParser.START_TAG) {
                    String name = xmlPullParser.getName();

                    if (name.equals("FunctionItem")) {
                        functionItems = null;
                        HashMap<String, String> out=null;
                        String enable = xmlPullParser.getAttributeValue(null, "enable");

                        if (enable != null && enable.equals("true")) {
                            functionItems = new FunctionItem();
                            functionItems.name = xmlPullParser.getAttributeValue(null, "name");
                            functionItems.auto = xmlPullParser.getAttributeValue(null, "auto");
                            functionItems.packageName = xmlPullParser.getAttributeValue(null, "packageName");
                            Utilities.parseParameter(xmlPullParser.getAttributeValue(null, "parameter"),
                                    functionItems.parameter);
                        }
                    }
                } else if (mEventType == XmlPullParser.END_TAG) {
                    String tagName = xmlPullParser.getName();

                    if (functionItems != null && tagName.equals("FunctionItem")) {
                        // add by urovo
                        if(functionItems.packageName.equals("com.ubx.factorykit.AttesttationKey") && SystemProperties.get("pwv.custom.enbuild", "false").equals("false")){
                            mEventType = xmlPullParser.next();
                            continue;
                        }
                        if(functionItems.packageName.equals("com.ubx.factorykit.SIM1") && cardType != CARD_TYPE_SIM) {
                            //mEventType = xmlPullParser.next();
                            //continue;
                        }
                        if(functionItems.packageName.equals("com.ubx.factorykit.PSAM") && cardType != CARD_TYPE_PSAM ) {
                           //mEventType = xmlPullParser.next();
                          // continue;
                        }
                        if (FactoryKitPro.PRODUCT_SQ28 && functionItems.packageName.equals("com.ubx.factorykit.Scan") && TextUtils.isEmpty(SystemProperties.get("persist.vendor.sys.scan.name",""))){
                            Log.d(TAG,"persist.vendor.sys.scan.name is empty remove scan test");
                            mEventType = xmlPullParser.next();
                            continue;
                        }
                        if (functionItems.packageName.equals("com.ubx.factorykit.SIM2") && cardType != CARD_TYPE_SIM && FactoryKitPro.PRODUCT_SQ28){
                            Log.d(TAG,"cardType != CARD_TYPE_SIM hide SIM2");
                            mEventType = xmlPullParser.next();
                            continue;
                        }
                        if (functionItems.packageName.equals("com.ubx.factorykit.CameraFront") && !CameraFront.checkCameraFront()) {
                            // urovo huangjiezhou add begin on 20211206
                            Log.d(TAG,"CameraFront is empty, remove CameraFront test");
                            mEventType = xmlPullParser.next();
                            continue;
                            // urovo huangjiezhou add end
                        }

                        // urovo huangjiezhou add begin for multi locale to functionItems.name on 20220512
                        if (configFile != null) {
                            functionItems.setName(Utilities.getLocaleString(mContext,
                                    R.array.item_test_packagename_entries, R.array.item_test_description_values,
                                    functionItems));
                        }
                        // urovo huangjiezhou add end

                         mFunctionItems.add(functionItems);

                    }
                }
                mEventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            loge(e);
        }


        // put ItemList into MainApp.getInstance().mItemList
        MainApp.getInstance().mItemList = getItemList(mFunctionItems);
        if (Values.ENABLE_BACKGROUND_SERVICE)
            startService(new Intent(mContext, AutoService.class));
        else {
            // To save test time, enable some devices first
            Utilities.enableWifi(mContext, true);
            Utilities.enableBluetooth(true);
            Utilities.enableGps(mContext, true);
            Utilities.configScreenTimeout(mContext, 1800000);
            Utilities.configMultiSim(mContext);
            Utilities.enableCharging(true);
        }
        setListAdapter(mBaseAdapter);
    }

    @Override
    protected void onDestroy() {
        logd("onDestroy");
        if (!ActivityManager.isUserAMonkey()) {
            
            // Utilities.enableWifi(mContext, false);
            // Utilities.enableBluetooth(false);
            // Utilities.enableGps(mContext, false);
            Utilities.configScreenTimeout(mContext, initScreenTime); // 1 min
            // enableCharging(false);
            if (Values.ENABLE_BACKGROUND_SERVICE) {
                logd("MainApp.getInstance().clearAllService()");
                MainApp.getInstance().clearAllService();
                stopService(new Intent(mContext, AutoService.class));
            } else {
                // Utilities.enableWifi(mContext, false);
                // Utilities.enableBluetooth(false);
                // Utilities.enableGps(mContext, false);
                Utilities.configScreenTimeout(mContext, initScreenTime); // 1 min
            }
        }
        super.onDestroy();
    }



    private boolean getChargingStatus() {
        String value = Utilities.getSystemProperties(Values.PROP_CHARGE_DISABLE, null);
        if ("1".equals(value))
            return false;
        else
            return true;
        
    }

    private void configSoundEffects(boolean enable) {
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (enable)
            mAudioManager.loadSoundEffects();
        else
            mAudioManager.unloadSoundEffects();
    }
    
    public void createShortcut(Context context) {
        Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.app_name));
        intent.putExtra("duplicate", false);
        Intent appIntent = new Intent();
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.setClass(context, getClass());
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, appIntent);
        ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context, R.drawable.icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        context.sendBroadcast(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        if (!ActivityManager.isUserAMonkey()) {
            int groupId = 0;

            SubMenu addMenu = menu.addSubMenu(groupId, MENU_CLEAN_STATE, Menu.NONE,
                    R.string.clean_state);
            addMenu.setIcon(android.R.drawable.ic_menu_revert);

            SubMenu resetMenu = menu.addSubMenu(groupId, MENU_UNINSTALL, Menu.NONE,
                    R.string.uninstall);
            resetMenu.setIcon(android.R.drawable.ic_menu_delete);
            if(!FactoryKitPro.PRODUCT_SQ47 && !FactoryKitPro.PRODUCT_SQ53H&& !FactoryKitPro.PRODUCT_SQ29Z&& !FactoryKitPro.PRODUCT_SQ28){
            SubMenu sensorCalMenu = menu.addSubMenu(groupId, MENU_SENSOR_CAL, Menu.NONE,
                    R.string.sensor_calibration_menu); // urovo yuanwei add sensor cal 2019-06-20
            sensorCalMenu.setIcon(android.R.drawable.ic_menu_delete);}
			SubMenu exittMenu = menu.addSubMenu(groupId, MENU_EXIT, Menu.NONE,
                    R.string.exit_menu); // urovo yuanwei exit

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.action_bar, menu);
        }
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        logd(item.getItemId());
        switch (item.getItemId()) {
        case (MENU_CLEAN_STATE):
            cleanTestState();
            break;
        case (MENU_UNINSTALL):
            Uri uri = Uri.fromParts("package", "com.ubx.factorykit", null);
            startActivity(new Intent(Intent.ACTION_DELETE, uri));
            break;
        case (MENU_SENSOR_CAL):// urovo yuanwei add sensor cal 2019-06-20
            Intent sensorCal = new Intent("com.android.intent.action.HIDDEN_SENSOR_CAL");
            if(Utilities.checkApk("com.qualcomm.qti.sensors.ui.selftest",mContext))
            {
                sensorCal.setComponent(new ComponentName("com.qualcomm.qti.sensors.ui.selftest",
                        "com.qualcomm.qti.sensors.ui.selftest.SensorCalListActivity"));
            }
			try {
                startActivity(sensorCal);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
            break;
        case (MENU_EXIT):
            mExitFlag = true;
            finish();
            break;
        case R.id.run_auto_items:
            toStartAutoTest = true;
            positionClicked = getNextUntestedItem(MainApp.getInstance().mItemList);
            loge("pos=" + positionClicked);
            if (positionClicked < 0) {
                toStartAutoTest = false;
            } else {
                Intent intent = (Intent) MainApp.getInstance().mItemList.get(positionClicked).get("intent");
                intent.putExtra(Values.KEY_SERVICE_INDEX, positionClicked);
                startActivityForResult(intent, positionClicked);
            }
            break;
        case R.id.pause_auto_items:
            toStartAutoTest = false;
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        if (!ActivityManager.isUserAMonkey()) {
            /*String nowLanguageFlag = SystemProperties.get("persist.sys.locale", "");;
            Log.d(TAG, "nowLanguageFlag---->" + nowLanguageFlag);
            if (!oldLanguageFlag.equals(nowLanguageFlag)) {
                Log.d(TAG, "oldLanguageFlag---->loadItemList()");
                startActivity(new Intent(this,Framework.class));
                finish();//关闭自己
            }*/
            IntentFilter filter = new IntentFilter(Values.BROADCAST_UPDATE_MAINVIEW);
            registerReceiver(mViewBroadcastReceiver, filter);
            IntentFilter filterSingle = new IntentFilter(Values.BROADCAST_FACTORY_SINGLETEST);
            registerReceiver(mSingleBroadcastReceiver, filterSingle);

            Utilities.configScreenTimeout(mContext, 1800000);// urovo yuanwei 2019-06-04
        }
        //urovo modify begin
        if(FactoryKitPro.PRODUCT_SQ53 || FactoryKitPro.PRODUCT_SQ51S ||FactoryKitPro.PRODUCT_SQ53Q /*|| FactoryKitPro.PRODUCT_SQ47*/)
        	refreshFunctionValue();//hanzengqin 20181009 add for flag "*#6789#"
        //urovo modify end
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (!ActivityManager.isUserAMonkey()) {
            unregisterReceiver(mViewBroadcastReceiver);
            unregisterReceiver(mSingleBroadcastReceiver);
            Utilities.configScreenTimeout(mContext, initScreenTime); // 1 min urovo yuanwei 2019-06-04
        }
        super.onPause();
    }


    private List getItemList(List<FunctionItem> functionItems) {
        
        List<Map> mList = new ArrayList<Map>();
        //for factorykit activity intent
        Intent mIntent = new Intent(Intent.ACTION_MAIN, null);
        mIntent.addCategory("android.category.factory.kit");
        PackageManager packageManager = getPackageManager();
        /** Retrieve all activities that can be performed for the given intent */
        List<ResolveInfo> list = packageManager.queryIntentActivities(mIntent, 0);

        //for third-app intent

        
        if (list == null)
            super.finish();
        if (functionItems == null)
            super.finish();
        
        int len = list.size();

        for(int n = 0; n < functionItems.size(); n ++) {
            //带GMS版本添加GoogleKey测试
            if((n==(functionItems.size()-1)) && GOOGLE_KEY_MENU_DISPLAY && IS_UROVO_VERSION)//将googlekey添加至恢复出厂设置项之前
            {
                FunctionItem itemGoogleKey = new FunctionItem();
                itemGoogleKey.name ="GoogleKey";
                itemGoogleKey.auto ="true";
                itemGoogleKey.packageName ="com.ubx.factorykit.AttesttationKey";
                Intent intentGoogleKey = new Intent();
                intentGoogleKey.setClassName("com.ubx.factorykit", "com.ubx.factorykit.AttesttationKey.AttesttationKey");
                addItem(mList, intentGoogleKey, itemGoogleKey);
            }

            FunctionItem functionItem = functionItems.get(n);
            if (functionItem == null) {
                continue;
            }
            if(functionItem.packageName.contains("com.ubx.factorykit")) {
                for (int i = 0; i < len; i++) {
                    ResolveInfo resolveInfo = list.get(i);

                    String className = resolveInfo.activityInfo.name.substring(0,
                            resolveInfo.activityInfo.name.lastIndexOf('.'));

                    if (!functionItem.packageName.equals(className)) {
                        continue;
                    }

                    Intent intent = new Intent();
                    intent.setClassName(resolveInfo.activityInfo.applicationInfo.packageName, resolveInfo.activityInfo.name);
                    addItem(mList, intent, functionItem);
                }
            }else if(functionItem.packageName.contains("/")){
                String[] sourceArray = functionItem.packageName.split("/");
                String pkg = sourceArray[0];
                String className = sourceArray[1];
                if(!className.contains(pkg)){
                    className = pkg+className;
                }
                Intent intent = new Intent();
                intent.setClassName(pkg, className);
                addItem(mList, intent, functionItem);
            }else{
                Intent tIntent = new Intent(Intent.ACTION_MAIN, null);
                tIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                tIntent.setPackage(functionItem.packageName);
                /** Retrieve all activities that can be performed for the given intent */
                try {
                    List<ResolveInfo> thirdList = packageManager.queryIntentActivities(tIntent, 0);
                    if(thirdList == null || thirdList.size() <= 0) {
                        //for category have <category android:name="android.category.factory.kit"/>  but no launcher icon
                        for (int i = 0; i < len; i++) {
                            ResolveInfo resolveInfo = list.get(i);
                            String className = resolveInfo.activityInfo.name.substring(0,
                                    resolveInfo.activityInfo.name.lastIndexOf('.'));

                            if (!functionItem.packageName.equals(className)) {
                                continue;
                            }
                            Intent intent = new Intent();
                            intent.setClassName(resolveInfo.activityInfo.applicationInfo.packageName, resolveInfo.activityInfo.name);
                            addItem(mList, intent, functionItem);
                        }

                    } else{
                        ResolveInfo ri = thirdList.iterator().next();

                        if (ri != null) {
                            Intent intent = new Intent();
                            intent.setClassName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name);
                            addItem(mList, intent, functionItem);
                        }
                    }
                }catch (Exception e){

                }
            }
        }
        return mList;
    }
    
    private void addItem(List<Map> list, Intent intent, FunctionItem functionItem) {

        Map<String, Object> temp = new HashMap<String, Object>();
        temp.put("intent", intent);
        temp.put("title", functionItem.name);
        temp.put("auto", functionItem.auto);
        temp.put("parameter", functionItem.parameter);
        temp.put("result_key", functionItem.packageName);
        temp.put("result", Utilities.getStringValueSaved(mContext, functionItem.packageName /**functionItem.name*/, "NULL"));
        if(DEBUG) Log.d(TAG, "addItem---->" + "packageName=" + functionItem.packageName +  " ,result= "+ Utilities.getStringValueSaved(mContext, functionItem.packageName, "NULL"));
        list.add(temp);
    }
    
    int itemClickPosition;
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        
        // Gets the data associated with the specified position in the list.
        logd("click pos=" + position);
        itemClickPosition = position;
        if (!toStartAutoTest) {
            Map map = (Map) l.getItemAtPosition(position);
            positionClicked = position;
            Intent intent = (Intent) map.get("intent");
            intent.putExtra(Values.KEY_SERVICE_INDEX, position);
            /*if(intent.getComponent().getClassName().equals("com.ubx.factorykit.Serial.Serial")){
            	intent = new Intent("android.intent.action.TEST_SERIAL");
            	startActivityForResult(intent, position);
            } else */
            if(intent.getComponent().getClassName().equals("com.ubx.factorykit.MasterClear.MasterClear")){
                getResult();
                if(totalSize == passSize){
                    startActivityForResult(intent, position);
                    Log.d(TAG, "onListItemClick ----> all pass don't show dialog");
                }else{
                    View view = mInflater.inflate(R.layout.alert_report, null, false);
                    TextView alertAllTv = view.findViewById(R.id.alert_all);
                    alertAllTv.setText(String.format(getString(R.string.report_all), totalSize));
                    alertAllTv.setVisibility(View.VISIBLE);
                    TextView alertPassTv = view.findViewById(R.id.alert_pass);
                    alertPassTv.setText(String.format(getString(R.string.report_pass), passSize));
                    alertPassTv.setTextColor(Color.GREEN);
                    alertPassTv.setVisibility(View.VISIBLE);
                    TextView alertFailTv = view.findViewById(R.id.alert_fail);
                    alertFailTv.setText(String.format(getString(R.string.report_fail), failSize, failItem));
                    alertFailTv.setTextColor(Color.RED);
                    TextView alertNoTestTv = view.findViewById(R.id.alert_not_test);
                    alertNoTestTv.setText(String.format(getString(R.string.report_notest), notTestSize, notTestItem));
                    alertNoTestTv.setTextColor(Color.GRAY);
                    View divider_01 = view.findViewById(R.id.alert_view_01);
                    divider_01.setVisibility(View.GONE);
                    View divider_02 = view.findViewById(R.id.alert_view_02);
                    divider_02.setVisibility(View.GONE);
                    alertD.setTitle(getString(R.string.report_title));
                    alertD.setView(view)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = (Intent) MainApp.getInstance().mItemList.get(itemClickPosition).get("intent");
                                intent.putExtra(Values.KEY_SERVICE_INDEX, itemClickPosition);
                                startActivityForResult(intent, itemClickPosition);
                            }
                        }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                    alertD.show();
                }
            } else {
                logd("onListItemClick ----> position" + position);
                startActivityForResult(intent, position);
            }
        }
    }

    protected void updateView(int requestCode, int resultCode) {
        
        resultCodeList[requestCode] = resultCode;
        Map map = (Map) getListView().getItemAtPosition(requestCode);
        // urovo yuanwei change "title" -> "result_key"
        String name = (String) map.get("result_key");
        String result = (resultCode == RESULT_OK ? "OK" : "FAIL");
        map.put("result", result);
        
        mBaseAdapter.notifyDataSetChanged();
    }
    
    protected void cleanTestState() {

        if (MainApp.getInstance().mItemList ==  null)
            return;

        int size = MainApp.getInstance().mItemList.size();
        
        for (int i = 0; i < size; i++) {
            
            Map map = (Map) this.getListView().getItemAtPosition(i);
            map.put("result", "NULL");
            // urovo yuanwei change "title" -> "result_key"
            Utilities.saveStringValue(mContext, (String) map.get("result_key"), null);
            // urovo yuanwei 2019.06.21 begin
                if (FactoryKitPro.PRODUCT_SQ53 || FactoryKitPro.PRODUCT_SQ51S || FactoryKitPro.PRODUCT_SQ53Q) {
                    Log.d(TAG, "BindQcrilMsgTunnelService ok, cleanTestState 0");
                    if(mQcRILHook.getBindQcrilMsgTunnelServiceState())
                    mQcRILHook.qcRilSetModemInfo(nvCIT, 0);
                }
			
            // urovo yuanwei 2019.06.21 end
        }
        
        mBaseAdapter.notifyDataSetChanged();
    }

    //s hanzengqin 20181009 add for flag "*#6789#"
    protected void refreshFunctionValue() {
        if (MainApp.getInstance().mItemList == null)
            return;

        boolean isPassInitForFlag = true;

        int size = MainApp.getInstance().mItemList.size();
        Log.i(TAG, " refreshFunctionValue size=" + size);
        //最后一项是恢复出厂设置 size -> size -1
        for (int i = 0; i < size - 1; i++) {
            Map map = (Map) this.getListView().getItemAtPosition(i);
            if (!"Pass".equals(map.get("result"))) {
                isPassInitForFlag = false;
                break; //有失败项时结束for循环，优化处理
            }
        }
        Log.d(TAG, " isPassInitForFlag===" + isPassInitForFlag);
        if (FactoryKitPro.PRODUCT_SQ53 || FactoryKitPro.PRODUCT_SQ51S || FactoryKitPro.PRODUCT_SQ53Q){
        if (isPassInitForFlag) {
            if (mQcRILHook.getBindQcrilMsgTunnelServiceState()) {
                Log.d(TAG, "BindQcrilMsgTunnelService ok refreshFunctionValue 1");
                mQcRILHook.qcRilSetModemInfo(nvCIT, 1);
            }

        } else {
            if (mQcRILHook.getBindQcrilMsgTunnelServiceState()) {
                Log.d(TAG, "BindQcrilMsgTunnelService ok refreshFunctionValue 0");
                mQcRILHook.qcRilSetModemInfo(nvCIT, 0);
            }
        }
     }
    }
    //e

    // urovo yuanwei 2020-04-28
    private int totalSize, passSize, failSize, notTestSize;
    private String failItem, notTestItem;
    private void getResult() {
        //重置一次变量
        totalSize = 0;
        passSize = 0;
        failSize = 0;
        notTestSize = 0;
        failItem = "";
        notTestItem = "";
        if (MainApp.getInstance().mItemList == null) return;
        //最后一项是恢复出厂设置 totalSize -> totalSize -1
        totalSize = MainApp.getInstance().mItemList.size() -1;
        StringBuffer failStr = new StringBuffer();
        StringBuffer notTestStr = new StringBuffer();
        for (int i = 0; i < totalSize; i++) {
            Map map = (Map) this.getListView().getItemAtPosition(i);
            if ("Failed".equals(map.get("result"))) {
                failStr.append(map.get("title") + ",");
                failSize ++;
            }else if("Pass".equals(map.get("result"))){
                passSize ++;
            }else{
                notTestStr.append(map.get("title") + ",");
                notTestSize ++;
            }
        }
        failItem = failStr.toString();
        notTestItem = notTestStr.toString();
        //Log.d(TAG, "getResult---->" + "totalSize=" + totalSize + " passSize=" + passSize + " failSize=" + failSize + " notTestSize=" + notTestSize);
        //Log.d(TAG, "getResult---->" + "failItem=" + failItem + " notTestItem=" + notTestItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (positionClicked == requestCode) {
            flagList[requestCode] = 1;
            resultCodeList[requestCode] = resultCode;
            Map map = (Map) this.getListView().getItemAtPosition(requestCode);
            // urovo yuanwei change "title" -> "result_key"
            String name = (String) map.get("result_key");
            String result = (resultCode == RESULT_OK ? "Pass" : "Failed");
            map.put("result", result);
            logd("Test:" + name + "result=" + result);
            Utilities.saveStringValue(mContext, name, result);
            // urovo yuanwei 2019.06.21 begin
            // Log.i(TAG," onActivityResult Test:" + name + ";result=" + result+";positionClicked="+positionClicked+";toStartAutoTest="+toStartAutoTest);
            //s hanzengqin 20181009 add for flag "*#6789#"
            if (result.equals("Failed") && (FactoryKitPro.PRODUCT_SQ53 || FactoryKitPro.PRODUCT_SQ51S
                || FactoryKitPro.PRODUCT_SQ53Q /*|| FactoryKitPro.PRODUCT_SQ47*/)) {
                if (mQcRILHook.getBindQcrilMsgTunnelServiceState()) {
                    Log.d(TAG, "BindQcrilMsgTunnelService ok onActivityResult 0");
                    mQcRILHook.qcRilSetModemInfo(nvCIT, 0);
                }
                if (toStartAutoTest) isPassTestForFlag = false;
            }
            if (toStartAutoTest && (FactoryKitPro.PRODUCT_SQ53 || FactoryKitPro.PRODUCT_SQ51S
                || FactoryKitPro.PRODUCT_SQ53Q /*|| FactoryKitPro.PRODUCT_SQ47*/)) {
                int maxIndex = MainApp.getInstance().mItemList.size() - 2 /* 最后一项是恢复出厂设置,倒数第二项完成后开始写NV*/;
                if (positionClicked == maxIndex) {//size-2
                    Log.i(TAG, " onActivityResult auto test end, isPassTestForFlag=" + isPassTestForFlag);
                    if (isPassTestForFlag)
                        if (mQcRILHook.getBindQcrilMsgTunnelServiceState()) {
                            Log.d(TAG, "BindQcrilMsgTunnelService ok toStartAutoTest 1");
                            mQcRILHook.qcRilSetModemInfo(nvCIT, 1);
                        } else if (mQcRILHook.getBindQcrilMsgTunnelServiceState()) {
                            Log.d(TAG, "BindQcrilMsgTunnelService ok toStartAutoTest 3333333333");
                            Log.d(TAG, "BindQcrilMsgTunnelService ok toStartAutoTest 0");
                            mQcRILHook.qcRilSetModemInfo(nvCIT, 0);
                        }

                }
            }
            // urovo yuanwei 2019.06.21 end

            mBaseAdapter.notifyDataSetChanged();
            Utilities.writeTestLog(name, result);
            if (toStartAutoTest) {
                mHandler.postDelayed(mRunnable, AUTO_TEST_TIME_INTERVAL);
                int nexPos = getNextUntestedItem(MainApp.getInstance().mItemList);
                if (nexPos > 4)
                    setSelection(nexPos - 4);
            }

            // urovo yuanwei add 2019-05-27 begin
            //guoyongcan this is only works for PDC550 ,because it have 26 items to be tested
            //when it comes to the last record ,then check all the results again for nv7239
            //if (positionClicked == (MainApp.getInstance().mItemList.size() - 2 /* 最后一项是恢复出厂设置,倒数第二项完成后开始写NV default 1*/)) {
            /*    boolean allpass = true;
                for (int i = 0; i <= positionClicked; i++) {
                    logd("[nv569Check] Test[" + i + "] result=" + resultCodeList[i]);
                    if (resultCodeList[i] != RESULT_OK) {
                        allpass = false;
                    }
                }
                if (FactoryKitPro.PRODUCT_SQ53) {
                    logd("[nv569Check] PRODUCT_SQ53");
                    if (allpass) {
                        Log.d(TAG, "items allpass");
                        if (mQcRILHook.getBindQcrilMsgTunnelServiceState()) {
                            Log.d(TAG, "BindQcrilMsgTunnelService allpass ok");
                            mQcRILHook.qcRilSetModemInfo(569, 1);
                        } else {
                            Log.d(TAG, "BindQcrilMsgTunnelService failed, need retry later.");
                        }
                    } else {
                        Log.d(TAG, "items not allpass");
                        if (mQcRILHook.getBindQcrilMsgTunnelServiceState()) {
                            Log.d(TAG, "BindQcrilMsgTunnelService not allpass ok");
                            mQcRILHook.qcRilSetModemInfo(569, 0);
                        } else {
                            Log.d(TAG, "BindQcrilMsgTunnelService failed, need retry later.");
                        }

                    }
                }

            }*/
            // urovo yuanwei add 2019-05-27 end

            // /** auto test */
            // if (toStartAutoTest) {
            // positionClicked =
            // getNextAutoItem(MainApp.getInstance().mItemList);
            // loge("pos=" + positionClicked);
            // if (positionClicked < 0) {
            // toStartAutoTest = false;
            // } else {
            // Intent intent = (Intent)
            // MainApp.getInstance().mItemList.get(positionClicked).get("intent");
            // startActivityForResult(intent, positionClicked);
            // }
            // }
        }
        
    }
    
    private int getNextUntestedItem(List<? extends Map<String, ?>> list) {
        int pos = -1;
        for (int i = 0; i < list.size(); i++) {
            Map<String, ?> item = list.get(i);
            if ("NULL".equals(item.get("result"))) {
                // if ("true".equals(item.get("auto")) &&
                // "NULL".equals(item.get("result"))) {
                pos = i;
                break;
            }
        }
        return pos;
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            curBackButtonTime = System.currentTimeMillis();
            if (curBackButtonTime - lastBackButtonTime < 2000)
                mExitFlag = true;
            lastBackButtonTime = curBackButtonTime;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_HOME) return true;
        return super.dispatchKeyEvent(event);
    }

    AlertDialog alert;
    long alertCurBackButtonTime = 0;
    long alertLastBackButtonTime = 0;
    public void finish() {
        
        /*
         * if (mExitFlag == false) { new
         * AlertDialog.Builder(this).setTitle(getString
         * (R.string.control_center_quit_confirm))
         * .setPositiveButton(getString(R.string.yes), new
         * DialogInterface.OnClickListener() {
         * 
         * public void onClick(DialogInterface dialog, int which) {
         * 
         * mExitFlag = true; finish(); }
         * }).setNegativeButton(getString(R.string.no), new
         * DialogInterface.OnClickListener() {
         * 
         * public void onClick(DialogInterface dialog, int which) {
         * 
         * } }).setCancelable(false).show(); return; }
         */
        
        /*if (mExitFlag == false) {
            new AlertDialog.Builder(this).setTitle(getString(R.string.control_center_quit_confirm))
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            mExitFlag = true;
                            int i;
                            int length = MainApp.getInstance().mItemList.size();
                            for(i = 0; i < length; i++) {
                                if (!(((String)MainApp.getInstance().mItemList.get(i).get("result")).equals("Pass"))) break;
                            }
                            if (i == length) {
				                write_NV_2499_Succ();
                            } else {
                                write_NV_2499_Fail();
                            }
                            finish();
                        }
                    }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
            return;
        }

        if (!mExitFlag) {
            toast("Press BACK again to quit");
            return;
        }*/

        if (!mExitFlag) {
            getResult();
            if(totalSize == passSize){
                toast(getString(R.string.exit_all_pass));
                Log.d(TAG, "finish ----> all pass don't show dialog");
            }else{
                View view = mInflater.inflate(R.layout.alert_report, null, false);
                TextView alertFailTv = view.findViewById(R.id.alert_fail);
                alertFailTv.setText(String.format(getString(R.string.report_fail), failSize, failItem));
                alertFailTv.setTextColor(Color.BLACK);
                TextView alertNoTestTv = view.findViewById(R.id.alert_not_test);
                alertNoTestTv.setText(String.format(getString(R.string.report_notest), notTestSize, notTestItem));
                alertNoTestTv.setTextColor(Color.BLACK);
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.4f, 0.6f);
                alphaAnimation.setDuration(1000);
                alphaAnimation.setRepeatCount(Animation.INFINITE);
                alphaAnimation.setRepeatMode(Animation.RESTART);
                alert =  new AlertDialog.Builder(this).create();
                alert.setTitle(getString(R.string.report_title));
                alert.show();
                alert.getWindow().setContentView(view);
                alert.setCanceledOnTouchOutside(false);
                alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
                            alertCurBackButtonTime = System.currentTimeMillis();
                            Log.d(TAG, "finish ----> alertCurBackButtonTime - alertLastBackButtonTime=" + (alertCurBackButtonTime - alertLastBackButtonTime));
                            if (alertCurBackButtonTime - alertLastBackButtonTime < 800) mExitFlag = true;
                            alertLastBackButtonTime = alertCurBackButtonTime;
                            if (mExitFlag) {
                                alert.dismiss();
                                Framework.this.finish();
                                mExitFlag = false;
                            }else{
                                // 利用延迟给双击做准备
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (alert.isShowing()) alert.dismiss();
                                    }
                                }, 200);
                            }
                            return true;
                        }
                        return false;
                    }
                });
                if (alert.isShowing()) {
                    curBackButtonTime = 0;
                    lastBackButtonTime = 0;
                    view.setAnimation(alphaAnimation);
                    view.setBackgroundColor(Color.RED);
                    alphaAnimation.start();
                }else {
                    view.setBackgroundColor(Color.WHITE);
                    alphaAnimation.cancel();
                    view.clearAnimation();
                }
            }
            return;
        }
        /** write NV_FACTORY_DATA_3_I result */
        if (Values.ENABLE_NV) {
        }
        super.finish();
    }

    public int getCardType(byte slot) {
        IccManager mIccManager = new IccManager();
        int ret = mIccManager.open(slot, card, mVoltage);
        if(ret != 0){
            Log.d(TAG, "open failed");
            return -1;
        }

        ret = mIccManager.getCardType();
        if(ret == -1){
            Log.d(TAG, "get card type failed");
        }
        mIccManager.close();
        return ret;
    }

    private void write_NV_2499_Succ(){
        try{
            ByteBuffer buf = ByteBuffer.allocate(26);
            buf.order(ByteOrder.LITTLE_ENDIAN);
            buf.putInt(2499);
            buf.putInt(1);
            buf.putShort((short)(26));
            AsyncResult result = mQcRilOemHook.sendQcRilHookMsg(IQcRilHook.QCRILHOOK_NV_WRITE, buf.array());
            if (result.exception != null) {
                result.exception.printStackTrace();
                throw new IOException();
            }
		} catch(IOException e) {
			e.printStackTrace();
		}
    }
    private void write_NV_2499_Fail(){
        try{
            ByteBuffer buf1 = ByteBuffer.allocate(26);
            buf1.order(ByteOrder.LITTLE_ENDIAN);
            buf1.putInt(2499);
            buf1.putInt(0);
            buf1.putShort((short)(26));
            AsyncResult result1 = mQcRilOemHook.sendQcRilHookMsg(IQcRilHook.QCRILHOOK_NV_WRITE, buf1.array());
            if (result1.exception != null) {
                result1.exception.printStackTrace();
                throw new IOException();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    private BaseAdapter mBaseAdapter = new BaseAdapter() {
        
        public View getView(int position, View convertView, ViewGroup parent) {
            
            if (convertView == null)
                convertView = mInflater.inflate(R.layout.list_item, null);
            
            ImageView image = (ImageView) convertView.findViewById(R.id.icon_center);
            TextView text = (TextView) convertView.findViewById(R.id.text_center);
            text.setText((String) (MainApp.getInstance().mItemList.get(position).get("title")));
            
            String result = (String) (String) (MainApp.getInstance().mItemList.get(position).get("result"));
            if (result.equals("NULL") == false){
                image.setImageBitmap(result.equals("Pass") ? passBitmap : failBitmap);
                // urovo add yuanwei anim
                AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 0.8f);
                alphaAnimation.setDuration(1000);
                alphaAnimation.setRepeatCount(Animation.INFINITE);
                alphaAnimation.setRepeatMode(Animation.RESTART);
                if(result.equals("Pass")){
                    convertView.setBackgroundColor(Color.WHITE);
                    alphaAnimation.cancel();
                    convertView.clearAnimation();
                }else{
                    convertView.setAnimation(alphaAnimation);
                    convertView.setBackgroundColor(Color.RED);
                    alphaAnimation.start();
                }
            }else{
                image.setImageBitmap(null);
                convertView.setBackgroundColor(Color.WHITE);
                convertView.clearAnimation();
            }
            return convertView;
        }
        
        public int getCount() {
            
            return MainApp.getInstance().mItemList.size();
        }
        
        public Object getItem(int position) {
            
            return MainApp.getInstance().mItemList.get(position);
        }
        
        public long getItemId(int arg0) {
            
            return 0;
        }
        
    };
    
    private void autoTestItems(List<? extends Map<String, ?>> list) {
        int testNum = list.size();
        for (int pos = 0; pos < testNum; pos++) {
        }
    }
    
    private Runnable mRunnable = new Runnable() {
        
        @Override
        public void run() {
            /** auto test */
            if (toStartAutoTest) {
                positionClicked = getNextUntestedItem(MainApp.getInstance().mItemList);
                loge("pos=" + positionClicked);
                if (positionClicked < 0) {
                    toStartAutoTest = false;
                } else {
                    Intent intent = (Intent) MainApp.getInstance().mItemList.get(positionClicked).get("intent");
                    intent.putExtra(Values.KEY_SERVICE_INDEX, positionClicked);
                    startActivityForResult(intent, positionClicked);
                }
            }
        }
    };
    
    BroadcastReceiver mViewBroadcastReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            mBaseAdapter.notifyDataSetChanged();
        }
    };

    BroadcastReceiver mSingleBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String activity = arg1.getStringExtra(Values.KEY_SERVICE_INDEX);
            startSingleTest(activity);
        }
    };

    public void startSingleTest(String activity) {
        if(TextUtils.isEmpty(activity))
            return;
        positionClicked = getItemPositionFromName(activity);
        if(positionClicked == -1)
            return;
        Intent intent = (Intent) MainApp.getInstance().mItemList.get(positionClicked).get("intent");
        intent.putExtra(Values.KEY_SERVICE_INDEX, positionClicked);
        startActivityForResult(intent, positionClicked);
    }

    public int getItemPositionFromName(String activity) {
        int itemSize = MainApp.getInstance().mItemList.size() -1 ;//master clear no need
        for (int i = 0; i < itemSize; i++) {
            Map map = (Map) this.getListView().getItemAtPosition(i);
            if (map.get("result_key").equals(activity)) {
                return i;
            }
        }
        return -1;
    }


    /** Fixed */
    
    public void toast(Object s) {
        
        if (s == null)
            return;
        Toast.makeText(getApplicationContext(), s + "", Toast.LENGTH_SHORT).show();
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
    
    private static void logd(Object s) {
        
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        
        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }

    private int getScreenOrientation() {
	    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }
}
