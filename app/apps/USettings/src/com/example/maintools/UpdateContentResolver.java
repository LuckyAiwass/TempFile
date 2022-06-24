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
