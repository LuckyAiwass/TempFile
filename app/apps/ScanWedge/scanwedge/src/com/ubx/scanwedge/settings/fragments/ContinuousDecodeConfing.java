package com.ubx.scanwedge.settings.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.device.provider.Settings;
import android.device.scanner.configuration.PropertyID;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.database.helper.USettings;

public class ContinuousDecodeConfing extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private ListPreference mn6603_multiple_decode_mode;
    private EditTextPreference n6603_multiple_decode_timeout;
    private EditTextPreference n6603_multiple_decode_count;
    private EditTextPreference n6603_multiple_decode_interval;
    private PreferenceScreen root;
    private int scannerType;
    private int mProfileId;
    private ScanWedgeApplication mApplication;
    private ContentResolver mContentResolver;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        setHasOptionsMenu(true);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        // TODO Auto-generated method stub
        super.onAttach(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initPresenter() {

    }

    private void updateActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            //actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowCustomEnabled(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        scannerType = args != null ? args.getInt("type") : 0;
        mProfileId = args != null ? args.getInt("profileId") : USettings.Profile.DEFAULT_ID;
        mApplication = (ScanWedgeApplication) getActivity().getApplication();
        mContentResolver = getContentResolver();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        root = this.getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }
        addPreferencesFromResource(R.xml.scanner_settings);
        root = this.getPreferenceScreen();
        updateActionBar();
        updateState();
    }

    private void updateState() {
        getActivity().setTitle(R.string.n6603_multiple_decode_config);
        mn6603_multiple_decode_mode = new ListPreference(getActivity());
        mn6603_multiple_decode_mode.setTitle(R.string.n6603_multiple_decode_mode);
        mn6603_multiple_decode_mode.setKey(Settings.System.DEC_Multiple_Decode_MODE);
        mn6603_multiple_decode_mode.setEntries(R.array.n6603_multiple_decode_values_titles);
        mn6603_multiple_decode_mode.setEntryValues(R.array.n6603_multiple_decode_values);
        mn6603_multiple_decode_mode.setOnPreferenceChangeListener(this);
        root.addPreference(mn6603_multiple_decode_mode);

        n6603_multiple_decode_timeout = new EditTextPreference(getActivity());
        n6603_multiple_decode_timeout.setTitle(R.string.n6603_multiple_decode_timeout);
        n6603_multiple_decode_timeout.setSummary(R.string.n6603_multiple_decode_timeout_sum);
        n6603_multiple_decode_timeout.setDialogMessage(R.string.n6603_multiple_decode_timeout_sum);
        n6603_multiple_decode_timeout.setKey(Settings.System.DEC_Multiple_Decode_TIMEOUT);
        n6603_multiple_decode_timeout.setOnPreferenceChangeListener(this);
        root.addPreference(n6603_multiple_decode_timeout);

        n6603_multiple_decode_interval = new EditTextPreference(getActivity());
        n6603_multiple_decode_interval.setTitle(R.string.n6603_multiple_decode_interval);
        n6603_multiple_decode_interval.setSummary(R.string.n6603_multiple_decode_interval_sum);
        n6603_multiple_decode_interval.setKey(Settings.System.DEC_Multiple_Decode_INTERVAL);
        n6603_multiple_decode_interval.setOnPreferenceChangeListener(this);
        root.addPreference(n6603_multiple_decode_interval);

        n6603_multiple_decode_count = new EditTextPreference(getActivity());
        n6603_multiple_decode_count.setTitle(R.string.n6603_multiple_decode_count);
        n6603_multiple_decode_count.setSummary(R.string.n6603_multiple_decode_count_sum);
        n6603_multiple_decode_count.setKey(Settings.System.DEC_MaxMultiRead_COUNT);
        n6603_multiple_decode_count.setOnPreferenceChangeListener(this);
        //root.addPreference(n6603_multiple_decode_count);

        int mode = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_Multiple_Decode_MODE, 0);
        int timeout = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_Multiple_Decode_TIMEOUT, 5000);
        mn6603_multiple_decode_mode.setValue(String.valueOf(mode));
        mn6603_multiple_decode_mode.setSummary(mn6603_multiple_decode_mode.getEntry());
        n6603_multiple_decode_timeout.setSummary("" + String.valueOf(timeout));

        timeout = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_Multiple_Decode_INTERVAL, 50);
        n6603_multiple_decode_interval.setSummary("" + timeout);

        mode = mApplication.getPropertyInt(mProfileId, Settings.System.DEC_MaxMultiRead_COUNT, 1);
        n6603_multiple_decode_count.setSummary("" + mode);
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // TODO Auto-generated method stub
        try {
            String key = preference.getKey();
            //android.util.Log.i("debug", "onPreferenceTreeClick==================" + key);
            if (key == null) return false;
            if (key.equals(Settings.System.DEC_MaxMultiRead_COUNT)) {
                ((EditTextPreference) preference).getEditText().setText("" + n6603_multiple_decode_count.getSummary());
            } else if (key.equals(Settings.System.DEC_Multiple_Decode_TIMEOUT)) {
                ((EditTextPreference) preference).getEditText().setText("" + n6603_multiple_decode_timeout.getSummary());
            } else if (key.equals(Settings.System.DEC_Multiple_Decode_INTERVAL)) {
                ((EditTextPreference) preference).getEditText().setText("" + n6603_multiple_decode_interval.getSummary());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        String key = preference.getKey();
        //android.util.Log.i("debug", "onPreferenceChange==================" + key);
        if (key == null) return false;
        if (key.equals(Settings.System.DEC_Multiple_Decode_MODE)) {
            int value = Integer.parseInt((String) newValue);
            mApplication.setPropertyInt(mProfileId, PropertyID.DEC_Multiple_Decode_MODE, Settings.System.DEC_Multiple_Decode_MODE, value);
            mn6603_multiple_decode_mode.setSummary(mn6603_multiple_decode_mode.getEntries()[value]);
        } else if (key.equals(Settings.System.DEC_MaxMultiRead_COUNT)) {
            if (newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            int len = 0;
            try {
                len = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                len = 0;
                e.printStackTrace();
            }
            if (len >= 1 && len <= 10) {
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_MaxMultiRead_COUNT, Settings.System.DEC_MaxMultiRead_COUNT, len);
            } else {
                showAlertToast(1, 10);
            }
        } else if (key.equals(Settings.System.DEC_Multiple_Decode_TIMEOUT)) {
            if (newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            int len = 50;
            try {
                len = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                len = 0;
                e.printStackTrace();
            }
            if (len >= 50 && len <= 60000) {
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_Multiple_Decode_TIMEOUT, Settings.System.DEC_Multiple_Decode_TIMEOUT, len);
                n6603_multiple_decode_timeout.setSummary("" + value);
            } else {
                showAlertToast(50, 60000);
            }
        } else if (key.equals(Settings.System.DEC_Multiple_Decode_INTERVAL)) {
            if (newValue == null || newValue.equals("")) return false;
            String value = newValue.toString();
            int len = 50;
            try {
                len = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                len = 0;
                value = "" + len;
                e.printStackTrace();
            }
            if (len >= 0 && len <= 5000) {
                mApplication.setPropertyInt(mProfileId, PropertyID.DEC_Multiple_Decode_INTERVAL, Settings.System.DEC_Multiple_Decode_INTERVAL, len);
                n6603_multiple_decode_interval.setSummary("" + value);
            } else {
                showAlertToast(10, 5000);
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void showAlertToast(int min, int max) {
        Toast.makeText(getActivity(), String.format(getActivity().getString(R.string.scanner_symbology_length_range), min, max), Toast.LENGTH_LONG).show();
    }
}
