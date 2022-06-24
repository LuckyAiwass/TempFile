package com.ubx.keyremap.component;

import android.content.Intent;
import android.device.KeyMapManager;
import android.os.Bundle;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.KeyEvent;
import android.util.Log;
import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;
import com.ubx.keyremap.dao.DataUpdateTask;
import android.os.Build;

import android.os.Build;

public class WakeKeySettings extends PreferenceActivity implements OnPreferenceChangeListener {

    private static final String TAG = Utils.TAG + "#" + WakeKeySettings.class.getSimpleName();
    private boolean isScan3 = Build.PROJECT.equals("SQ53"); // SQ53 右扫描键522 KEYCODE_SCAN_3
    private KeyMapManager mKeyMap = null;

    private SwitchPreference mVolumeUpPrf;
    private SwitchPreference mVolumeDownPrf;
    private SwitchPreference mLeftScanPrf;
    private SwitchPreference mRightScanPrf;
    private SwitchPreference mPttKeyPrf;

    private int KEYCODE_SCAN_LEFT = KeyEvent.KEYCODE_SCAN_1;
    private int KEYCODE_SCAN_RIGHT = KeyEvent.KEYCODE_SCAN_2;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if(Build.PROJECT.equals("SQ53X") || Build.PROJECT.equals("SQ45C")){
            KEYCODE_SCAN_LEFT = KeyEvent.KEYCODE_SCAN_2;
            KEYCODE_SCAN_RIGHT = KeyEvent.KEYCODE_SCAN_1;
        }
        
        addPreferencesFromResource(R.xml.wakekey_settings);

        mVolumeUpPrf = (SwitchPreference) getPreferenceScreen().findPreference("volume_up");
        mVolumeDownPrf = (SwitchPreference) getPreferenceScreen().findPreference("volume_down");
        mLeftScanPrf = (SwitchPreference) getPreferenceScreen().findPreference("left_scan");
        mRightScanPrf = (SwitchPreference) getPreferenceScreen().findPreference("right_scan");
        mPttKeyPrf = (SwitchPreference) getPreferenceScreen().findPreference("ptt_key");
        if(Build.PROJECT.equals("SQ53A")){
            getPreferenceScreen().removePreference(mPttKeyPrf);
        }

        if(Build.PROJECT.equals("SQ83")){
            getPreferenceScreen().removePreference(mLeftScanPrf);
            getPreferenceScreen().removePreference(mPttKeyPrf);
        }
        mVolumeUpPrf.setOnPreferenceChangeListener(this);
        mVolumeDownPrf.setOnPreferenceChangeListener(this);
        mLeftScanPrf.setOnPreferenceChangeListener(this);
        mRightScanPrf.setOnPreferenceChangeListener(this);
        mPttKeyPrf.setOnPreferenceChangeListener(this);

        getPreferenceScreen().removePreference(mVolumeUpPrf);
        getPreferenceScreen().removePreference(mVolumeDownPrf);

        mKeyMap = new KeyMapManager(getApplicationContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean b = (boolean )newValue;
        if(preference == mVolumeUpPrf) {
            mKeyMap.setWakeKey(KeyEvent.KEYCODE_VOLUME_UP, b);
        } else if(preference == mVolumeDownPrf){
            mKeyMap.setWakeKey(KeyEvent.KEYCODE_VOLUME_DOWN, b);
        } else if(preference == mLeftScanPrf){
            mKeyMap.setWakeKey(KEYCODE_SCAN_LEFT, b);
        } else if(preference == mRightScanPrf){
            if(!isScan3) {
                mKeyMap.setWakeKey(KEYCODE_SCAN_RIGHT, b);
            } else {
                mKeyMap.setWakeKey(KeyEvent.KEYCODE_SCAN_3, b);
            }
        } else if(preference == mPttKeyPrf){
            mKeyMap.setWakeKey(KeyEvent.KEYCODE_KEYBOARD_TALK, b);
            mKeyMap.setWakeKey(KeyEvent.KEYCODE_KEYBOARD_PTT, b);
        }
        return true;
    }

    // @Override
    // public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    //     return super.onPreferenceTreeClick(preferenceScreen, preference);
    // }

    public void updateView(){
        mVolumeUpPrf.setChecked(mKeyMap.isWakeKey(KeyEvent.KEYCODE_VOLUME_UP));

        mVolumeDownPrf.setChecked(mKeyMap.isWakeKey(KeyEvent.KEYCODE_VOLUME_DOWN));

        mLeftScanPrf.setChecked(mKeyMap.isWakeKey(KEYCODE_SCAN_LEFT));

        if(!isScan3) {
            mRightScanPrf.setChecked(mKeyMap.isWakeKey(KEYCODE_SCAN_RIGHT));
        } else {
            mRightScanPrf.setChecked(mKeyMap.isWakeKey(KeyEvent.KEYCODE_SCAN_3));
        }
        
        mPttKeyPrf.setChecked(mKeyMap.isWakeKey(KeyEvent.KEYCODE_KEYBOARD_PTT));

//        mPttKeyPrf.setChecked(mKeyMap.isWakeKey(KeyEvent.KEYCODE_KEYBOARD_TALK));
        
    }
}
