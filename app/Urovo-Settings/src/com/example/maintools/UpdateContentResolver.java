package com.example.maintools;

import android.text.TextUtils;
import android.util.Log;

import com.example.saxparsexml.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;

import android.content.Context;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings;
import com.android.internal.view.RotationPolicy;
import android.device.scanner.configuration.PropertyID;
import android.device.scanner.configuration.Triggering;
import android.device.ScanManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.bluetooth.BluetoothAdapter;
import java.util.Locale;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.media.AudioManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.LocaleList;
import java.util.Locale;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.res.Configuration;
import android.os.RemoteException;
import android.app.backup.BackupManager;
import android.app.NotificationManager;
import com.android.settingslib.display.DisplayDensityUtils;
import android.view.Display;


public class UpdateContentResolver {
    String  TAG="urovo";
    Context  mContext;
    private static final int TYPE_SOUNDS = 1;
    private static final int TYPE_VIBRATE = 2;
    private static final int TYPE_ENTER = 3;
    private static final int TYPE_POWER = 4;
    private static final int TYPE_MODE = 5;
    private static final int TYPE_KEY = 6;
    private final Configuration mCurConfig = new Configuration();
    public UpdateContentResolver(Context context) {
        // TODO Auto-generated constructor stub
        mContext=context;
    }

    public boolean UpdateSystem(String  key,String data){
        boolean state=false;
        ScanManager mScanManager= new ScanManager();
        AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int[] update_id_buffer = new int[1];
        int[] update_value_buffer = new int[1];
        int value=0;
        float Fvalue= (float)1.00;
        Log.i("wujinquan","key:"+key+"  data:"+data);
        if((!TextUtils.isEmpty(data)) && (!TextUtils.isEmpty(key))){
            try {
                if(key.equals("font_scale"))
                    Fvalue=Float.parseFloat(data);
                else
                    value = Integer.parseInt(data);
            } catch (Exception e) {
                Log.i("urovo","Exception e:"+e);
            }
            switch(key){
                case "wifi_on":
                    WifiManager mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
                    mWifiManager.setWifiEnabled(data.equals("1"));
                    break;
                case "bluetooth_on":
                    BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
                    if(data.equals("1"))bluetoothAdapter.enable();
                    else bluetoothAdapter.disable();
                    break;
		case "location_providers_allowed":
                    String location_mode = data.equals("gps") ? "1":data.equals("network")? "2":"3";
                    Settings.Secure.putString(mContext.getContentResolver(), "location_mode" , location_mode);
                    break;
		case "zen_mode":
		    NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		    int zen_mode = Integer.valueOf(data);
	            mNotificationManager.setZenMode(zen_mode, null, TAG);
		    break;
		case "display_density_forced":
		    int densityDpi = Integer.valueOf(data);
		    DisplayDensityUtils.setForcedDisplayDensity(Display.DEFAULT_DISPLAY, densityDpi);
		    break;
                case "airplane_mode_on"://arrayzh.add("飞行模式"); break;// ok
                    Intent mintent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                    mintent.putExtra("state", data.equals("1"));
                    mContext.sendBroadcast(mintent);
                    break;
                case "boot_count":break;
                case "screen_brightness":break;//arrayzh.add("亮度"); break;//ok
                case "screen_brightness_mode":break;//arrayzh.add("自动调节亮度"); break;  //ok
                case "screen_off_timeout":break;//arrayzh.add("休眠"); break; //ok
                case "accelerometer_rotation":break;//arrayzh.add("设备旋转时");  break;//ok
                case "install_non_market_apps":break;//arrayzh.add("未知来源"); break;  //ok
                case "default_input_method":break;//arrayzh.add("输入法"); break;      //ok
                case "enabled_input_method":break;//arrayzh.add("勾选输入法"); break;  //ok
                case "selected_input_method_subtype":break;//arrayzh.add("当前输入法"); break;  //ok
                case "input_methods_subtype_history":break;//arrayzh.add("当前输入法"); break;  //ok
                case "auto_time":break;//arrayzh.add("自动确定时间和日期"); break; //ok
                case "auto_time_zone":break;//arrayzh.add("自动确定时区"); break;  //ok
                case "auto_pop_softinput":break;//arrayzh.add("自动弹出软键盘"); break;  //ok
                case "adb_enabled":break;//arrayzh.add("USB调试"); break;            //ok
                case "wifi_ssid_password":break;//arrayzh.add("wifi账号、密码"); break;  //ok
                //case "location_providers_allowed"://arrayzh.add("位置"); break;//********************on
                //case "device_nfc"://arrayzh.add("NFC"); break;       //***********************no
                case "SEND_GOOD_READ_VIBRATE_ENABLE"://arrayzh.add("震动（广播模式）"); break;
                    update_id_buffer[0] = PropertyID.SEND_GOOD_READ_VIBRATE_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "SEND_GOOD_READ_BEEP_ENABLE"://arrayzh.add("提示音(广播模式)"); break;
                    update_id_buffer[0] =PropertyID.SEND_GOOD_READ_BEEP_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "volume_music_speaker"://arrayzh.add("媒体音量"); break;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,value,AudioManager.FLAG_PLAY_SOUND);
                    break;
                case "volume_alarm_speaker"://arrayzh.add("闹钟音量"); break;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,value,AudioManager.FLAG_PLAY_SOUND);
                    break;
                case "volume_ring_speaker"://arrayzh.add("铃声音量"); break;
                    mAudioManager.setStreamVolume(AudioManager.STREAM_RING,value,AudioManager.FLAG_PLAY_SOUND);
                    break;
                case "font_scale"://arrayzh.add("字体大小"); break;
                    try {
                        mCurConfig.fontScale = Fvalue;
                        ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
                    } catch (RemoteException re) {
                        Log.i("urovo","Exception e:"+re);
                    }
                case "time_12_24":break;//arrayzh.add("使用24小时格式"); break;   //ok
                case "accessibility_display_inversion_enabled":break;//arrayzh.add("颜色反转"); break;  //ok
                // case "persist.sys.country"://break;//arrayzh.add("地区（语言）"); break;
                case "persist.sys.locale"://arrayzh.add("语言");
                    String mlanguage=android.os.SystemProperties.get("persist.sys.locale","zh-CN");
                    setlanguage(mlanguage);
                    break;
                case "SUSPENSION_BUTTON":
                    Intent intent=new Intent("android.intent.action.SUSPENSION_BUTTON");
                    mContext.sendBroadcast(intent);
                    break;
                case "TRIGGERING_MODES"://arrayzh.add("触发模式");
                    if(data.equals("4"))
                        mScanManager.setTriggerMode(Triggering.CONTINUOUS);
                    if(data.equals("2"))
                        mScanManager.setTriggerMode(Triggering.PULSE);
                    if(data.equals("8"))
                        mScanManager.setTriggerMode(Triggering.HOST);
                    break;
                case "WEDGE_KEYBOARD_ENABLE"://arrayzh.add("键盘方式输出");

                    mScanManager.setOutputParameter(TYPE_MODE, value);
                    break;
                case "WEDGE_KEYBOARD_TYPE"://arrayzh.add("键盘类型");
                    int[] id = new int[]{PropertyID.WEDGE_KEYBOARD_TYPE};

                    update_value_buffer[0] =  value;
                    mScanManager.setPropertyInts(id, update_value_buffer);
                    break;
                case "GOOD_READ_BEEP_ENABLE"://arrayzh.add("提示音");
                    update_id_buffer[0] = PropertyID.GOOD_READ_BEEP_ENABLE;

                    update_value_buffer[0] =   Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "GOOD_READ_VIBRATE_ENABLE"://arrayzh.add("震动");
                    update_id_buffer[0] =  PropertyID.GOOD_READ_VIBRATE_ENABLE;

                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "LABEL_APPEND_ENTER"://arrayzh.add("附加回车键");
                    mScanManager.setOutputParameter(TYPE_ENTER, Integer.parseInt(data));
                    break;
                case "WEDGE_INTENT_ACTION_NAME"://arrayzh.add("广播动作");
                    mScanManager.setPropertyString(PropertyID.WEDGE_INTENT_ACTION_NAME, data);
                    break;
                case "INTENT_DATA_STRING_TAG"://arrayzh.add("广播数据标签");
                    mScanManager.setPropertyString(PropertyID.WEDGE_INTENT_DATA_STRING_TAG, data);
                    break;
                case "CODING_FORMAT"://arrayzh.add("中文编码类型");
                    update_id_buffer[0] = PropertyID.CODING_FORMAT;
                    int[] value_buf = new int[1];
                    value_buf[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, value_buf);
                    break;
                case "SEND_LABEL_PREFIX_SUFFIX"://arrayzh.add("附加格式化");
                    update_id_buffer[0] = PropertyID.SEND_LABEL_PREFIX_SUFFIX;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "REMOVE_NONPRINT_CHAR"://arrayzh.add("删除非打印控制字符");
                    update_id_buffer[0] = PropertyID.REMOVE_NONPRINT_CHAR;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "LABEL_FORMAT_SEPARATOR_CHAR"://arrayzh.add("应用标识分隔符");
                    mScanManager.setPropertyString(PropertyID.LABEL_FORMAT_SEPARATOR_CHAR, data);//str
                    break;
                case "LABEL_SEPARATOR_ENABLE"://arrayzh.add("应用标识符");
                    update_id_buffer[0] = PropertyID.LABEL_SEPARATOR_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "LABEL_PREFIX"://arrayzh.add("前缀");
                    mScanManager.setPropertyString(PropertyID.LABEL_PREFIX, data);
                    break;
                case "LABEL_SUFFIX"://arrayzh.add("后缀");
                    mScanManager.setPropertyString(PropertyID.LABEL_SUFFIX, data);
                    break;
                case "LABEL_MATCHER_PATTERN"://arrayzh.add("格式化");
                    mScanManager.setPropertyString(PropertyID.LABEL_MATCHER_PATTERN, data);
                    break;
                case "MULTI_DECODE_MODE"://arrayzh.add("启用多次解码");
                    update_id_buffer[0] = PropertyID.MULTI_DECODE_MODE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "BAR_CODES_TO_READ"://arrayzh.add("解码次数");
                    update_id_buffer[0] = PropertyID.BAR_CODES_TO_READ;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "FULL_READ_MODE"://arrayzh.add("完整解码");
                    update_id_buffer[0] = PropertyID.FULL_READ_MODE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "AZTEC_ENABLE":
                    update_id_buffer[0] = PropertyID.AZTEC_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "AZTEC_INVERSE":
                    update_id_buffer[0] = PropertyID.AZTEC_INVERSE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "AZTEC_SYMBOL_SIZE":
                    update_id_buffer[0] = PropertyID.AZTEC_SYMBOL_SIZE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODABAR_ENABLE":
                    update_id_buffer[0] = PropertyID.CODABAR_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODABAR_CLSI":
                    update_id_buffer[0] = PropertyID.CODABAR_CLSI;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODABAR_NOTIS":
                    update_id_buffer[0] = PropertyID.CODABAR_NOTIS;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODABAR_LENGTH1":
                    update_id_buffer[0] = PropertyID.CODABAR_LENGTH1;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODABAR_LENGTH2":
                    update_id_buffer[0] = PropertyID.CODABAR_LENGTH2;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE128_ENABLE":
                    update_id_buffer[0] = PropertyID.CODE128_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE128_CHECK_ISBT_TABLE":
                    update_id_buffer[0] = PropertyID.CODE128_CHECK_ISBT_TABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE128_LENGTH1":
                    update_id_buffer[0] = PropertyID.CODE128_LENGTH1;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE128_LENGTH2":
                    update_id_buffer[0] = PropertyID.CODE128_LENGTH2;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE128_GS1_ENABLE":
                    update_id_buffer[0] = PropertyID.CODE128_GS1_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE39_ENABLE":
                    update_id_buffer[0] = PropertyID.CODE39_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE39_ENABLE_CHECK":
                    update_id_buffer[0] = PropertyID.CODE39_ENABLE_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE39_SEND_CHECK":
                    update_id_buffer[0] = PropertyID.CODE39_SEND_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE39_FULL_ASCII":
                    update_id_buffer[0] = PropertyID.CODE39_FULL_ASCII;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE39_LENGTH1":
                    update_id_buffer[0] = PropertyID.CODE39_LENGTH1;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE39_LENGTH2":
                    update_id_buffer[0] = PropertyID.CODE39_LENGTH2;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE93_ENABLE":
                    update_id_buffer[0] = PropertyID.CODE93_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE93_LENGTH1":
                    update_id_buffer[0] = PropertyID.CODE93_LENGTH1;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE93_LENGTH2":
                    update_id_buffer[0] = PropertyID.CODE93_LENGTH2;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE11_ENABLE":
                    update_id_buffer[0] = PropertyID.CODE11_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE11_LENGTH1":
                    update_id_buffer[0] = PropertyID.CODE11_LENGTH1;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE11_LENGTH2":
                    update_id_buffer[0] = PropertyID.CODE11_LENGTH2;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE11_SEND_CHECK":
                    update_id_buffer[0] = PropertyID.CODE11_SEND_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE11_ENABLE_CHECK":
                    update_id_buffer[0] = PropertyID.CODE11_ENABLE_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "COMPOSITE_CC_AB_ENABLE":
                    update_id_buffer[0] = PropertyID.COMPOSITE_CC_AB_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "COMPOSITE_CC_C_ENABLE":
                    update_id_buffer[0] = PropertyID.COMPOSITE_CC_C_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "COMPOSITE_TLC39_ENABLE":
                    update_id_buffer[0] = PropertyID.COMPOSITE_TLC39_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "HANXIN_ENABLE":
                    update_id_buffer[0] = PropertyID.HANXIN_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "HANXIN_INVERSE":
                    update_id_buffer[0] = PropertyID.HANXIN_INVERSE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "DATAMATRIX_ENABLE":
                    update_id_buffer[0] = PropertyID.DATAMATRIX_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "DATAMATRIX_INVERSE":
                    update_id_buffer[0] = PropertyID.DATAMATRIX_INVERSE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "DATAMATRIX_SYMBOL_SIZE":
                    update_id_buffer[0] = PropertyID.DATAMATRIX_SYMBOL_SIZE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "EAN13_ENABLE":
                    update_id_buffer[0] = PropertyID.EAN13_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "EAN13_SEND_CHECK":
                    update_id_buffer[0] = PropertyID.EAN13_SEND_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "EAN13_BOOKLANDEAN":
                    update_id_buffer[0] = PropertyID.EAN13_BOOKLANDEAN;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "EAN13_BOOKLAND_FORMAT":
                    update_id_buffer[0] = PropertyID.EAN13_BOOKLAND_FORMAT;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "EAN8_ENABLE":
                    update_id_buffer[0] = PropertyID.EAN8_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "EAN8_SEND_CHECK":
                    update_id_buffer[0] = PropertyID.EAN8_SEND_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "EAN8_TO_EAN13":
                    update_id_buffer[0] = PropertyID.EAN8_TO_EAN13;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "GS1_14_ENABLE":
                    update_id_buffer[0] = PropertyID.GS1_14_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "GS1_EXP_ENABLE":
                    update_id_buffer[0] = PropertyID.GS1_EXP_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "GS1_EXP_LENGTH1":
                    update_id_buffer[0] = PropertyID.GS1_EXP_LENGTH1;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "GS1_EXP_LENGTH2":
                    update_id_buffer[0] = PropertyID.GS1_EXP_LENGTH2;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "GS1_LIMIT_ENABLE":
                    update_id_buffer[0] = PropertyID.GS1_LIMIT_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "I25_ENABLE":
                    update_id_buffer[0] = PropertyID.I25_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "I25_LENGTH1":
                    update_id_buffer[0] = PropertyID.I25_LENGTH1;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "I25_LENGTH2":
                    update_id_buffer[0] = PropertyID.I25_LENGTH2;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "I25_ENABLE_CHECK":
                    update_id_buffer[0] = PropertyID.I25_ENABLE_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "I25_SEND_CHECK":
                    update_id_buffer[0] = PropertyID.I25_SEND_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "I25_TO_EAN13":
                    update_id_buffer[0] = PropertyID.I25_TO_EAN13;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "M25_ENABLE":
                    update_id_buffer[0] = PropertyID.M25_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "C25_ENABLE":
                    update_id_buffer[0] = PropertyID.C25_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "MAXICODE_ENABLE":
                    update_id_buffer[0] = PropertyID.MAXICODE_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "MAXICODE_SYMBOL_SIZE":
                    update_id_buffer[0] = PropertyID.MAXICODE_SYMBOL_SIZE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "MICROPDF417_ENABLE":
                    update_id_buffer[0] = PropertyID.MICROPDF417_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "MSI_ENABLE":
                    update_id_buffer[0] = PropertyID.MSI_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "MSI_LENGTH1":
                    update_id_buffer[0] = PropertyID.MSI_LENGTH1;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "MSI_LENGTH2":
                    update_id_buffer[0] = PropertyID.MSI_LENGTH2;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "MSI_REQUIRE_2_CHECK":
                    update_id_buffer[0] = PropertyID.MSI_REQUIRE_2_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "MSI_CHECK_2_MOD_11":
                    update_id_buffer[0] = PropertyID.MSI_CHECK_2_MOD_11;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "MSI_SEND_CHECK":
                    update_id_buffer[0] = PropertyID.MSI_SEND_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE32_ENABLE":
                    update_id_buffer[0] = PropertyID.CODE32_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "CODE32_SEND_START":
                    update_id_buffer[0] = PropertyID.CODE32_SEND_START;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "PDF417_ENABLE":
                    update_id_buffer[0] = PropertyID.PDF417_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "AUSTRALIAN_POST_ENABLE":
                    update_id_buffer[0] = PropertyID.AUSTRALIAN_POST_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "JAPANESE_POST_ENABLE":
                    update_id_buffer[0] = PropertyID.JAPANESE_POST_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "KIX_CODE_ENABLE":
                    update_id_buffer[0] = PropertyID.KIX_CODE_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "ROYAL_MAIL_ENABLE":
                    update_id_buffer[0] = PropertyID.ROYAL_MAIL_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "US_PLANET_ENABLE":
                    update_id_buffer[0] = PropertyID.US_PLANET_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "US_POSTAL_SEND_CHECK":
                    update_id_buffer[0] = PropertyID.US_POSTAL_SEND_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "US_POSTNET_ENABLE":
                    update_id_buffer[0] = PropertyID.US_POSTNET_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "USPS_4STATE_ENABLE":
                    update_id_buffer[0] = PropertyID.USPS_4STATE_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPU_FICS_ENABLE":
                    update_id_buffer[0] = PropertyID.UPU_FICS_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "QRCODE_ENABLE":
                    update_id_buffer[0] = PropertyID.QRCODE_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "QRCODE_INVERSE":
                    update_id_buffer[0] = PropertyID.QRCODE_INVERSE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "QRCODE_SYMBOL_SIZE":
                    update_id_buffer[0] = PropertyID.QRCODE_SYMBOL_SIZE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "MICROQRCODE_ENABLE":
                    update_id_buffer[0] = PropertyID.MICROQRCODE_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "D25_ENABLE":
                    update_id_buffer[0] = PropertyID.D25_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "D25_LENGTH1":
                    update_id_buffer[0] = PropertyID.D25_LENGTH1;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "D25_LENGTH2":
                    update_id_buffer[0] = PropertyID.D25_LENGTH2;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "TRIOPTIC_ENABLE":
                    update_id_buffer[0] = PropertyID.TRIOPTIC_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCA_ENABLE":
                    update_id_buffer[0] = PropertyID.UPCA_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCA_SEND_CHECK":
                    update_id_buffer[0] = PropertyID.UPCA_SEND_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCA_SEND_SYS":
                    update_id_buffer[0] = PropertyID.UPCA_SEND_SYS;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCA_TO_EAN13":
                    update_id_buffer[0] = PropertyID.UPCA_TO_EAN13;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCE_ENABLE":
                    update_id_buffer[0] = PropertyID.UPCE_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCE_SEND_CHECK":
                    update_id_buffer[0] = PropertyID.UPCE_SEND_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCE_SEND_SYS":
                    update_id_buffer[0] = PropertyID.UPCE_SEND_SYS;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCE_TO_UPCA":
                    update_id_buffer[0] = PropertyID.UPCE_TO_UPCA;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCE1_ENABLE":
                    update_id_buffer[0] = PropertyID.UPCE1_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCE1_SEND_CHECK":
                    update_id_buffer[0] = PropertyID.UPCE1_SEND_CHECK;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCE1_SEND_SYS":
                    update_id_buffer[0] = PropertyID.UPCE1_SEND_SYS;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPCE1_TO_UPCA":
                    update_id_buffer[0] = PropertyID.UPCE1_TO_UPCA;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "EAN_EXT_ENABLE_2_5_DIGIT":
                    update_id_buffer[0] = PropertyID.EAN_EXT_ENABLE_2_5_DIGIT;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UPC_EAN_SECURITY_LEVEL":
                    update_id_buffer[0] = PropertyID.UPC_EAN_SECURITY_LEVEL;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "UCC_COUPON_EXT_CODE":
                    update_id_buffer[0] = PropertyID.UCC_COUPON_EXT_CODE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "DOTCODE_ENABLE":
                    update_id_buffer[0] = PropertyID.DOTCODE_ENABLE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "DPM_DECODE_MODE":
                    update_id_buffer[0] = PropertyID.DPM_DECODE_MODE;
                    update_value_buffer[0] = Integer.parseInt(data);
                    mScanManager.setPropertyInts(update_id_buffer, update_value_buffer);
                    break;
                case "lockscreen_sounds_enabled":break;//arrayzh.add("屏幕锁定提示音"); break;
                case "charging_sounds_enabled":break;//arrayzh.add("充电提示音"); break;
                case "sound_effects_enabled":break;//arrayzh.add("触摸提示音"); break;
                case "haptic_feedback_enabled":break;//arrayzh.add("触摸时震动"); break;
                case "dtmf_tone":break;//arrayzh.add("拨号键盘提示音"); break;
                case "SCANNER_ENABLE"://arrayzh.add("扫描设置"); break;
                    if (data.equals("1")) {
                        mScanManager.openScanner();
                    } else {
                        mScanManager.closeScanner();
                    }
                case "Glove_mode"://arrayzh.add("手套模式"); break;
                    setNodeEnable(data.equals("1"),"/sys/goodix/smartcover/smart_cover");
                case "SCAN_HANDLE"://arrayzh.add("扫描手柄"); break;
                    setNodeEnable(data.equals("1"),"/sys/devices/soc/qpnp-smbcharger-17/usbid_scankey");
                default:   state=true;Log.i("urovo-1","default");

            }
        }
        return state;

    }

    public void setlanguage(String mlanguage){
        try{
            String mfont_scale=Settings.System.getString(mContext.getContentResolver(), "font_scale").trim();
            Log.i(TAG,"setlanguage font_scale:"+mfont_scale);
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            float mfscale = Float.parseFloat(mfont_scale);
            Log.i(TAG,"setlanguage mfscale:"+mfont_scale);
            config.fontScale=mfscale;
            Log.i(TAG,"setlanguage mlanguage:"+mlanguage);
            //String mlanguage=android.os.SystemProperties.get("persist.sys.locale","zh-CN");
            //String mcountry=android.os.SystemProperties.get("persist.sys.country","CN");
            String mcountry=mlanguage.substring(mlanguage.lastIndexOf("-")+1);
            Log.i(TAG,"setlanguage mcountry:"+mcountry);
            //android.os.SystemProperties.set("persist.sys.country", mcountry);
            mlanguage=mlanguage.substring(0, mlanguage.indexOf("-"));
            Log.i(TAG,"setlanguage mlanguage:"+mlanguage);
            //android.os.SystemProperties.set("persist.sys.language", mlanguage);
            Locale mlocale = new Locale(mlanguage,mcountry);
            LocaleList locales=new LocaleList(mlocale);
            config.userSetLocale = true;
            config.setLocales(locales);
            am.updatePersistentConfiguration(config);
            //BackupManager.dataChanged("com.android.providers.settings");
        } catch (RemoteException e) {
            Log.i(TAG,"RemoteException e:"+e);
        }

    }



    public void setNodeEnable(boolean enabled, String node) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(node);
            outputStream.write(Integer.toString(enabled ? 1 : 0).getBytes());
            outputStream.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


}
