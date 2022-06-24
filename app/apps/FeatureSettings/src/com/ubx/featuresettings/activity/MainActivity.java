package com.ubx.featuresettings.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.preference.SwitchPreference;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.EditTextPreference;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.Button;
import android.widget.CheckBox;

import android.app.AlertDialog;
import android.provider.Settings;

import com.ubx.featuresettings.R;
import com.ubx.featuresettings.service.FloatingService;
import com.ubx.featuresettings.util.AppInfo;
import com.ubx.featuresettings.util.ChoiceAppDialog;
import com.ubx.featuresettings.util.SettingsUtils;
import com.ubx.featuresettings.adapter.StaggeredGridAdapter;
import com.ubx.featuresettings.util.ULog;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import android.device.admin.SettingProperty;
import android.os.Handler;
import android.device.DeviceManager;
import android.os.SystemProperties;
import com.ubx.featuresettings.util.SettingsContentObserver;
import android.net.Uri;

public class MainActivity extends PreferenceActivity {

    private static final String KEY_STATUS_BAR = "key_status";
    private static final String KEY_VBTN = "key_vbtn";
    private static final String KEY_NAVIGATION_BAR = "key_navigationbar";
    private static final String KEY_VBTN_LEFT = "key_vbtn_left";
    private static final String KEY_VBTN_MIDDLE = "key_vbtn_middle";
    private static final String KEY_VBTN_RIGHT = "key_vbtn_right";
    //	private static final String KEY_LOCK_SCREEN = "key_lock_screen";
    private static final String KEY_AUTO_INPUT_METHOD = "key_auto_input_method";
    private static final String KEY_STATUS_INPUT_METHOD = "key_status_input_method";
    private static final String KEY_DISABLE_SCANKEY_TO_APP = "key_disable_scankey_to_app";
    private static final String KEY_SHOW_SCANBTN = "key_show_scanbtn";
    private static final String KEY_LOCK_SCREEN = "key_lock_screen";
    private static final String KEY_HIND_LEFT_LOCKSCREEN = "key_hind_left_lockscreen";
    private static final String KEY_HIND_RIGHT_LOCKSCREEN = "key_hind_right_lockscreen";
    private static final String KEY_PTT_ACTION = "key_ptt_action";
    private static final String KEY_DEFAULT_LAUNCHER = "key_default_launcher";
    private static final String KEYCODE_KEYONE = "key_lock_first";
    private static final String KEYCODE_KEYTWO = "key_lock_second";
    private static final String KEYCODE_DISABLE_INPUT_METHOD = "key_disable_input_method";
    private static final String KEYCODE_WAKEUP_ENABLE = "key_wakeup_enable";
    private static final String KEY_THREE_FINGER_SCREENSHOTS = "key_three_finger_screenshots";
    private static final String KEYCODE_RFID_SCAN_ENABLE = "key_rfid_scan_enable";
    private static final String KEY_TELEPHONE_RECORD = "key_telephone_record";
    private static final String KEY_KEYBOARD_FLOATING_WINDOW = "key_soft_keyboard_floating_window";
    private static final String KEY_KEYBOARD_SETTINGS = "key_keyboard_settings";

    public final static String ATTRIBUTE_UROVO_STATUSBAR_ENABLE = "UROVO_STATUSBAR_ENABLE";
    public final static String ATTRIBUTE_AUTO_POP_SOFTINPUT = "auto_pop_softinput";
    public final static String ATTRIBUTE_UROVO_SCAN_PASS = "UROVO_SCAN_PASS";
    public final static String ATTRIBUTE_DISABLE_POP_SOFTINPUT = "disable_pop_softinput";
    public final static String ATTRIBUTE_MULTI_FINGER_SCREEN_SHOT_ENABLED = "multi_finger_screen_shot_enabled";
    public final static String ATTRIBUTE_UROVO_LOCK_SCREEN = "UROVO_LOCK_SCREEN";
    public final static String ATTRIBUTE_ENABLE_AUTORECORD = "ENABLE_AUTORECORD";
    public final static String ATTRIBUTE_AUTORECORD_PATH = "AUTORECORD_PATH";
    public final static String ATTRIBUTE_UROVO_DISALLOWED_KEYCODES = "UROVO_DISALLOWED_KEYCODES";

    private SwitchPreference statusBar;
    private Preference vbtn;
    private SwitchPreference navigationBar;
    private CheckBoxPreference vbtnLeft;
    private CheckBoxPreference vbtnMiddle;
    private CheckBoxPreference vbtnRight;
    //	private CheckBoxPreference lockScreen;
    private SwitchPreference autoInputMethod;
    private SwitchPreference disableInputMethod;
    private CheckBoxPreference statusInputMethod;
    private SwitchPreference disableScankeyToApp;
    private CheckBoxPreference showScanbtn;
    private SwitchPreference screenshots;
    private SwitchPreference lockScreen;
    private Preference wakeupEnable;
    private Preference functionEnable;
    private CheckBoxPreference hindLeftLockscreen;
    private CheckBoxPreference hindRightLockscreen;
    private Preference pttAction;
    private Preference defaultLauncher;
    private SwitchPreference autoTelephoneRecord;
    private SwitchPreference keyboardFloatingWindow;
    private Preference keyboardSettings;

    private DeviceManager deviceManager;
    private ChoiceAppDialog choiceAppDialog;
    private AlertDialog.Builder builder;

    private SettingsContentObserver settingsContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.featuresettings);
	deviceManager = new DeviceManager();
	settingsContentObserver = new SettingsContentObserver(this,new Handler());
	getContentResolver().registerContentObserver(android.provider.Settings.System.getUriFor("UROVO_STATUSBAR_ENABLE"), false , settingsContentObserver);
	initView();
    }

    private void initView() {
        statusBar = (SwitchPreference) findPreference(KEY_STATUS_BAR);
	if(Settings.System.getString(getContentResolver(), ATTRIBUTE_UROVO_STATUSBAR_ENABLE) == null || Settings.System.getString(getContentResolver(), ATTRIBUTE_UROVO_STATUSBAR_ENABLE).equals("0")){
	statusBar.setChecked(true);
	}else{
	statusBar.setChecked(false);
	}
        navigationBar = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR);
        navigationBar.setChecked("0".equals(SystemProperties.get("persist.sys.dynamickeys","1")));
        vbtn = (Preference) findPreference(KEY_VBTN);
	vbtnLeft = (CheckBoxPreference) findPreference(KEY_VBTN_LEFT);
        vbtnMiddle = (CheckBoxPreference) findPreference(KEY_VBTN_MIDDLE);
        vbtnRight = (CheckBoxPreference) findPreference(KEY_VBTN_RIGHT);
//		lockScreen = (CheckBoxPreference) findPreference(KEY_LOCK_SCREEN);
        autoInputMethod = (SwitchPreference) findPreference(KEY_AUTO_INPUT_METHOD);
        // statusInputMethod = (CheckBoxPreference)
        // findPreference(KEY_STATUS_INPUT_METHOD);
        disableScankeyToApp = (SwitchPreference) findPreference(KEY_DISABLE_SCANKEY_TO_APP);
	disableScankeyToApp.setChecked(deviceManager.getScanKeyPass());
        showScanbtn = (CheckBoxPreference) findPreference(KEY_SHOW_SCANBTN);
        lockScreen = (SwitchPreference) findPreference(KEY_LOCK_SCREEN);
        hindLeftLockscreen = (CheckBoxPreference) findPreference(KEY_HIND_LEFT_LOCKSCREEN);
        hindRightLockscreen = (CheckBoxPreference) findPreference(KEY_HIND_RIGHT_LOCKSCREEN);
        screenshots = (SwitchPreference) findPreference(KEY_THREE_FINGER_SCREENSHOTS);
        pttAction = (Preference) findPreference(KEY_PTT_ACTION);
        defaultLauncher = (Preference) findPreference(KEY_DEFAULT_LAUNCHER);
        wakeupEnable = (Preference) findPreference(KEYCODE_WAKEUP_ENABLE);
        functionEnable = (Preference) findPreference(KEYCODE_RFID_SCAN_ENABLE); 
        disableInputMethod = (SwitchPreference) findPreference(KEYCODE_DISABLE_INPUT_METHOD);
	autoTelephoneRecord = (SwitchPreference) findPreference(KEY_TELEPHONE_RECORD);
	autoTelephoneRecord.setChecked(deviceManager.getEnableAutoCallRecord());
        keyboardFloatingWindow = (SwitchPreference) findPreference(KEY_KEYBOARD_FLOATING_WINDOW);
        keyboardSettings = (Preference) findPreference(KEY_KEYBOARD_SETTINGS);
	if(Settings.Global.getInt(getContentResolver(), ATTRIBUTE_DISABLE_POP_SOFTINPUT , 0) == 1){
            disableInputMethod.setChecked(false);
        }else{
	    disableInputMethod.setChecked(true);
	}
	
	try{
	if(deviceManager.getAutoPopInputMethod()){
		autoInputMethod.setChecked(true);
	}else{
	    autoInputMethod.setChecked(false);
	}
	}catch(Exception e){
		autoInputMethod.setChecked(true);
	}

	if(Settings.System.getString(getContentResolver(), ATTRIBUTE_UROVO_LOCK_SCREEN) != null && Settings.System.getString(getContentResolver(), ATTRIBUTE_UROVO_LOCK_SCREEN).equals("true")){
	lockScreen.setChecked(true);
	}else{
	lockScreen.setChecked(false);
	}
	
	if(Settings.System.getInt(getContentResolver(), ATTRIBUTE_MULTI_FINGER_SCREEN_SHOT_ENABLED , 0)==1){
	screenshots.setChecked(true);
	}else if(Settings.System.getInt(getContentResolver(), ATTRIBUTE_MULTI_FINGER_SCREEN_SHOT_ENABLED , 0)==0){
	screenshots.setChecked(false);
	}

	//lockScreen.setChecked(deviceManager.getLockScreen());
        //urovo zhoubo add begin 20200516
        getPreferenceScreen().removePreference(hindLeftLockscreen);
        getPreferenceScreen().removePreference(hindRightLockscreen);
        //urovo zhoubo add end 20200516
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case KEY_STATUS_BAR:
                setStatusBar(statusBar.isChecked());
                break;
            case KEY_NAVIGATION_BAR:
                setNavigationBar(navigationBar.isChecked());
                break;
	    case KEY_VBTN:
		setVbtnEnable();
		break;
            case KEY_VBTN_LEFT:
                setVbtnLeft(vbtnLeft.isChecked());
                break;
            case KEY_VBTN_MIDDLE:
                setVbtnMiddle(vbtnMiddle.isChecked());
                break;
            case KEY_VBTN_RIGHT:
                setVbtnRight(vbtnRight.isChecked());

                break;
//		case KEY_LOCK_SCREEN:
//			setLockScreen(lockScreen.isChecked());
//			break;
            case KEY_AUTO_INPUT_METHOD:
                setAutoInputMethod(autoInputMethod.isChecked());
                break;
            case KEY_STATUS_INPUT_METHOD:
                // setStatusInputMethod(statusInputMethod.isChecked());
                break;
            case KEY_DISABLE_SCANKEY_TO_APP:
                setDisableScankeyToApp(disableScankeyToApp.isChecked());
                break;
            case KEY_SHOW_SCANBTN:
                setShowScanBtn(showScanbtn.isChecked());
                break;
		    case KEY_HIND_LEFT_LOCKSCREEN:
			    setHindLeftLockScreen(hindLeftLockscreen.isChecked());
			    break;
		    case KEY_HIND_RIGHT_LOCKSCREEN:
			    setHindRightLockScreen(hindRightLockscreen.isChecked());
			    break;
			case KEY_THREE_FINGER_SCREENSHOTS:
			    setDisableScreenshots(screenshots.isChecked());
			    break;
            case KEY_PTT_ACTION:
                setPttAction();
                break;
            case KEY_DEFAULT_LAUNCHER:
                setDefaultLauncher();
                break;
            case KEY_LOCK_SCREEN:
                setLockScreen();
                break;
            case KEYCODE_WAKEUP_ENABLE:
                setWakeupEnable();
                break;
            case KEYCODE_DISABLE_INPUT_METHOD:
                setDisableInputMethod(disableInputMethod.isChecked());
                break;
            case KEYCODE_RFID_SCAN_ENABLE:
                setRfidScanEnable();
                break;
	    case KEY_TELEPHONE_RECORD:
		setTeleRecord(autoTelephoneRecord.isChecked());
		break;
            case KEY_KEYBOARD_FLOATING_WINDOW:
                setKeyboardFloatingWindow(keyboardFloatingWindow.isChecked());
                break;
            case KEY_KEYBOARD_SETTINGS:
                setkeyboardSettingsEnable();
                break;

        }
        return true;
    }

    private void setStatusBar(boolean checked) {
	//deviceManager.enableStatusBar(checked);
	//Settings.System.putString(getContentResolver(), ATTRIBUTE_UROVO_STATUSBAR_ENABLE ,  checked ? "0" : "1");
	int what = 0;
        if (checked) {
            what &= ~StatusBarManager.DISABLE_EXPAND;
        } else {
            what |= StatusBarManager.DISABLE_EXPAND;
        }
        try {
            Settings.System.putString(getContentResolver() , "UROVO_STATUSBAR_ENABLE" , String.valueOf(what));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setNavigationBar(boolean checked) {
        Log.d("lincong","111");
        Intent intent = new Intent();
        intent.setAction("android.intent.action.HIDE_NAVIGATION");
        intent.putExtra("hide_navigation_bar",checked);
        this.sendBroadcast(intent);
    }


    private void setVbtnLeft(boolean checked) {
        //deviceManager.setLeftKeyEnabled(checked);
	String settingstr = "";
	 try {
            settingstr = Settings.System.getString(getContentResolver(), ATTRIBUTE_UROVO_DISALLOWED_KEYCODES);
            if (checked) {
                settingstr = SettingsUtils.removePackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_BACK));
            } else {
                settingstr = SettingsUtils.addPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_BACK));
            }
        Settings.System.putString(getContentResolver(), ATTRIBUTE_UROVO_DISALLOWED_KEYCODES , settingstr);
	 }catch(Exception e){
		 e.printStackTrace();	
		 }
    }

    private void setVbtnMiddle(boolean checked) {
        //deviceManager.setHomeKeyEnabled(checked);
	String settingstr = "";
         try {
            settingstr = Settings.System.getString(getContentResolver(), ATTRIBUTE_UROVO_DISALLOWED_KEYCODES);
            if (checked) {
                settingstr = SettingsUtils.removePackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_HOME));
            } else {
                settingstr = SettingsUtils.addPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_HOME));
            }
        Settings.System.putString(getContentResolver(), ATTRIBUTE_UROVO_DISALLOWED_KEYCODES , settingstr);
         }catch(Exception e){
                 e.printStackTrace();
                 }
    }

    private void setVbtnRight(boolean checked) {
        //deviceManager.setRightKeyEnabled(checked);
	String settingstr = "";
         try {
            settingstr = Settings.System.getString(getContentResolver(), ATTRIBUTE_UROVO_DISALLOWED_KEYCODES);
            if (checked) {
                settingstr = SettingsUtils.removePackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_APP_SWITCH));
            } else {
                settingstr = SettingsUtils.addPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_APP_SWITCH));
            }
        Settings.System.putString(getContentResolver(), ATTRIBUTE_UROVO_DISALLOWED_KEYCODES , settingstr);
         }catch(Exception e){
                 e.printStackTrace();
                 }
    }

    private boolean getVbtnLeft() {
	String settingstr = "";
        try {
            settingstr = Settings.System.getString(getContentResolver(), ATTRIBUTE_UROVO_DISALLOWED_KEYCODES);

            return SettingsUtils.containsPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_BACK)) ? false : true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean getVbtnMiddle() {
	String settingstr = "";
        try {
            settingstr = Settings.System.getString(getContentResolver(), ATTRIBUTE_UROVO_DISALLOWED_KEYCODES);

            return SettingsUtils.containsPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_HOME)) ? false : true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean getVbtnRight() {
	String settingstr = "";
        try {
            settingstr = Settings.System.getString(getContentResolver(), ATTRIBUTE_UROVO_DISALLOWED_KEYCODES);

            return SettingsUtils.containsPackageName(settingstr, String.valueOf(KeyEvent.KEYCODE_APP_SWITCH)) ? false : true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

//	private void setLockScreen(boolean checked) {
//		deviceManager.setKeyguardKeyEnabled(!checked);
//	}

    private void setAutoInputMethod(boolean checked) {
        //deviceManager.setAutoPopInputMethod(checked);
        Settings.Global.putInt(getContentResolver(), ATTRIBUTE_AUTO_POP_SOFTINPUT , checked ? 1 : 0);
    }

    private void setDisableScankeyToApp(boolean checked) {
        //deviceManager.setScanKeyPass(checked);
	Settings.System.putString(getContentResolver(), ATTRIBUTE_UROVO_SCAN_PASS ,   Boolean.toString(checked));
    }

    private void setShowScanBtn(boolean checked) {
        deviceManager.setShowScanButton(checked);
    }

    private void setWakeupEnable(){
        Intent intent = new Intent("android.intent.action.WAKE_KEY_SETTINGS");
        startActivity(intent);
    }

    private void setRfidScanEnable(){
        Intent intent = new Intent("android.intent.action.KeyFunction");
        startActivity(intent);
    }

    private void setDisableInputMethod(boolean checked){
        Settings.Global.putInt(getContentResolver(), ATTRIBUTE_DISABLE_POP_SOFTINPUT , checked ? 0 : 1);
	/*
	if(checked){
            deviceManager.setRestrictionPolicy("ime_enable", 1);
        } else {
            deviceManager.setRestrictionPolicy("ime_enable", 0);
        }*/

    }
    
    
    private void setDisableScreenshots(boolean checked){
        Settings.System.putInt(getContentResolver(), ATTRIBUTE_MULTI_FINGER_SCREEN_SHOT_ENABLED , checked ? 1 : 0);
    }

    private void setLockScreen() {

        final List<String> keyCode = new ArrayList<>();
        String first = Settings.System.getString(getContentResolver(), KEYCODE_KEYONE);
        String second = Settings.System.getString(getContentResolver(), KEYCODE_KEYTWO);
        keyCode.add(first == null ? "26" : first);
        keyCode.add(second == null ? "24" : second);
        Log.d("jinpu", first + second);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_keypad, null, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyc);
        final Switch mSwitch = (Switch) view.findViewById(R.id.st_lock);
        TextView ok = (TextView) view.findViewById(R.id.ok);

        String[] keyboard = getResources().getStringArray(R.array.keyboard);
        List<String> list = Arrays.asList(keyboard);
        final String[] keyboardCode = getResources().getStringArray(R.array.keyboardcode);
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < keyboard.length; i++)
            map.put(keyboardCode[i], keyboard[i]);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL));
        StaggeredGridAdapter adapter = new StaggeredGridAdapter(list, this, map.get(keyCode.get(0)), map.get(keyCode.get(1)));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
	//lockScreen.setChecked(deviceManager.getLockScreen());
        //mSwitch.setChecked(deviceManager.getLockScreen());
        //final Dialog mDidalog = new AlertDialog.Builder(this)
        //        .setTitle(getString(R.string.str_pinning_screen))
        //        .setView(view)
        //        .show();
        //ok.setText(android.R.string.yes);
        //ok.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        if (keyCode.size() < 2)
        //            Toast.makeText(MainActivity.this, getString(R.string.str_selec_error), Toast.LENGTH_SHORT).show();
        //        else {
        //            deviceManager.setLockScreen(mSwitch.isChecked());
        //            Settings.System.putString(getContentResolver(), KEYCODE_KEYONE, keyCode.get(0));
        //            Settings.System.putString(getContentResolver(), KEYCODE_KEYTWO, keyCode.get(1));
        //            mDidalog.dismiss();
        //        }
        //    }
        //});
	//deviceManager.setLockScreen(lockScreen.isChecked());
	Settings.System.putString(getContentResolver(), ATTRIBUTE_UROVO_LOCK_SCREEN ,  Boolean.toString(lockScreen.isChecked()));
	//deviceManager.setSettingProperty("System-UROVO_LOCK_SCREEN", Boolean.toString(lockScreen.isChecked()));
        Settings.System.putString(getContentResolver(), KEYCODE_KEYONE, keyCode.get(0));
        Settings.System.putString(getContentResolver(), KEYCODE_KEYTWO, keyCode.get(1));

        adapter.setOnViewItemClickListener(new StaggeredGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView textView = (TextView) view.findViewById(R.id.item_name);
                if (!textView.isSelected()) {
                    if (keyCode.size() < 2) {
                        textView.setSelected(!textView.isSelected());
                        keyCode.add(keyboardCode[position]);
                    }
                } else {
                    textView.setSelected(!textView.isSelected());
                    keyCode.remove(keyboardCode[position]);
                }
            }
        });
        //	deviceManager.setLockScreen(checked);
    }

    private void setHindLeftLockScreen(boolean checked) {
        //deviceManager.setLeftKeyguardEnabled(!checked);
	deviceManager.setSettingProperty("System-UROVO_LEFTKEYGUARD_ENABLE", Boolean.toString(!checked));
    }

    private void setHindRightLockScreen(boolean checked) {
        deviceManager.setRightKeyguardEnabled(!checked);
    }

    private void setDefaultLauncher() {
        choiceAppDialog = ChoiceAppDialog.newInstance(MainActivity.this, new ChoiceAppDialog.CallbackListener() {
            @Override
            public void getAppInfo(AppInfo info) {
                ULog.d(info.getAppName());
            }
        });
        choiceAppDialog.show();
    }

    private void setPttAction() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.title_ptt));
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_ptt_action, null, false);
        builder.setView(view);
        final EditText downaction = (EditText) view.findViewById(R.id.down_action);
        final EditText upaction = (EditText) view.findViewById(R.id.up_action);
        ULog.d(deviceManager.getPTTDownAction() + "           " + deviceManager.getPTTUpAction());
        downaction.setText(deviceManager.getPTTDownAction() == null ? "android.intent.action.PTT_KEYDOWN" : deviceManager.getPTTDownAction());
        upaction.setText(deviceManager.getPTTUpAction() == null ? "android.intent.action.PTT_KEYUP" : deviceManager.getPTTUpAction());
        builder.setNegativeButton(getString(R.string.str_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(getString(R.string.str_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String downStr = downaction.getText().toString().trim();
                String upStr = upaction.getText().toString().trim();
                ULog.i("downStr   =      " + downStr + "     upStr     =       " + upStr);
                deviceManager.setPTTDownAction(downStr);
                deviceManager.setPTTUpAction(upStr);
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

     private void setVbtnEnable() {
      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View view = LayoutInflater.from(this).inflate(R.layout.dialog_vbtn, null, false);
        	builder.setView(view);
                Dialog dialog=builder.create();
		dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		CheckBox cbRight= (CheckBox)view.findViewById(R.id.cb_vbtn_right);
                CheckBox cbLeft= (CheckBox)view.findViewById(R.id.cb_vbtn_left);
                CheckBox cbMiddle= (CheckBox)view.findViewById(R.id.cb_vbtn_middle);
		cbRight.setChecked(getVbtnRight());
                cbLeft.setChecked(getVbtnLeft());
                cbMiddle.setChecked(getVbtnMiddle());
		Button okButton=(Button)view.findViewById(R.id.key_vbtn_ok);
		okButton.setOnClickListener(new View.OnClickListener() {
            	@Override
            	public void onClick(View v) {
		if(cbLeft.isChecked() != getVbtnLeft()){
		setVbtnLeft(cbLeft.isChecked());
		}
		if(cbMiddle.isChecked() != getVbtnMiddle()){
		setVbtnMiddle(cbMiddle.isChecked());
		}
		if(cbRight.isChecked() != getVbtnRight()){
		setVbtnRight(cbRight.isChecked());
		}
		dialog.dismiss();
            }
        });
		Button cancelButton=(Button)view.findViewById(R.id.key_vbtn_cancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                dialog.dismiss();
            }
        });
		dialog.show();
     }

    private void setkeyboardSettingsEnable() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_vbtn, null, false);
        builder.setView(view);
        Dialog dialog=builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        CheckBox cbRight= (CheckBox)view.findViewById(R.id.cb_vbtn_right);
        CheckBox cbLeft= (CheckBox)view.findViewById(R.id.cb_vbtn_left);
        CheckBox cbMiddle= (CheckBox)view.findViewById(R.id.cb_vbtn_middle);
        cbRight.setChecked(getVbtnRight());
        cbLeft.setChecked(getVbtnLeft());
        cbMiddle.setChecked(getVbtnMiddle());
        Button okButton=(Button)view.findViewById(R.id.key_vbtn_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cbLeft.isChecked() != getVbtnLeft()){
                    setVbtnLeft(cbLeft.isChecked());
                }
                if(cbMiddle.isChecked() != getVbtnMiddle()){
                    setVbtnMiddle(cbMiddle.isChecked());
                }
                if(cbRight.isChecked() != getVbtnRight()){
                    setVbtnRight(cbRight.isChecked());
                }
                dialog.dismiss();
            }
        });
        Button cancelButton=(Button)view.findViewById(R.id.key_vbtn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }


     private void setTeleRecordDialog() {
	AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	View view = LayoutInflater.from(this).inflate(R.layout.dialog_telephone_record, null, false);
	builder.setView(view);
	Dialog dialog=builder.create();
	dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
	EditText recordFilePath = (EditText)view.findViewById(R.id.key_record_file_path);
	if(deviceManager.getAutoCallRecordPath()!=null){
        recordFilePath.setText(deviceManager.getAutoCallRecordPath());
	}
	Button okButton=(Button)view.findViewById(R.id.key_record_ok);
                okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
		if(!recordFilePath.getText().toString().isEmpty()){
	               deviceManager.setAutoCallRecordPath(recordFilePath.getText().toString());
		}
		Log.d("lincong",deviceManager.getAutoCallRecordPath());
                dialog.dismiss();
            }
        });
                Button cancelButton=(Button)view.findViewById(R.id.key_record_cancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                dialog.dismiss();
            }
        });
	dialog.show();
     } 
    
     private void setTeleRecord(boolean checked){
	Settings.System.putString(getContentResolver(), ATTRIBUTE_ENABLE_AUTORECORD ,  Boolean.toString(checked));
	if(checked){
		if(Settings.System.getString(getContentResolver(), ATTRIBUTE_AUTORECORD_PATH) == null){
		Settings.System.putString(getContentResolver(), ATTRIBUTE_AUTORECORD_PATH , getResources().getString(R.string.str_default_record_file_path));
		//deviceManager.setAutoCallRecordPath(getResources().getString(R.string.str_default_record_file_path));
		}
	}
     }

    private void setKeyboardFloatingWindow(boolean checked){
        if(checked){
            Settings.Global.putInt(getContentResolver(), "keyboard_floating_window", 1);
            startService(new Intent(MainActivity.this, FloatingService.class));
        } else {
            Settings.Global.putInt(getContentResolver(), "keyboard_floating_window", 0);
            stopService(new Intent(MainActivity.this, FloatingService.class));
        }
    }

}
