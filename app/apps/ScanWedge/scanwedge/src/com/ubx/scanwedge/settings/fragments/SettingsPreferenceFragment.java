package com.ubx.scanwedge.settings.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import com.ubx.scanwedge.settings.BasePresenter;
import com.ubx.scanwedge.R;
import com.ubx.scanwedge.settings.utils.ULog;

/**
 * Base class for Settings fragments, with some helper functions and dialog management.
 */
public abstract class SettingsPreferenceFragment<T extends BasePresenter>
        extends PreferenceFragment implements IFragmentContract.IBaseView {

    private static final String TAG = ULog.TAG + SettingsPreferenceFragment.class.getSimpleName();

    private String mHelpUrl;

    protected T mPresenter;

    public abstract void initPresenter();

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Prepare help url and enable menu if necessary
        int helpResource = getHelpResource();
        if (helpResource != 0) {
            mHelpUrl = getResources().getString(helpResource);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!TextUtils.isEmpty(mHelpUrl)) {
            setHasOptionsMenu(true);
        }
    }

    /**
     * Override this if you want to show a help item in the menu, by returning the resource id.
     * @return the resource id for the help url
     */
    protected int getHelpResource() {
        return 0;
    }

    /*
     * The name is intentionally made different from Activity#finish(), so that
     * users won't misunderstand its meaning.
     */
    public final void finishFragment() {
        getActivity().onBackPressed();
    }

    // Some helpers for functions used by the settings fragments when they were activities

    /**
     * Returns the ContentResolver from the owning Activity.
     */
    protected ContentResolver getContentResolver() {
        return getActivity().getContentResolver();
    }

    /**
     * Returns the specified system service from the owning Activity.
     */
    protected Object getSystemService(final String name) {
        return getActivity().getSystemService(name);
    }

    /**
     * Returns the PackageManager from the owning Activity.
     */
    protected PackageManager getPackageManager() {
        return getActivity().getPackageManager();
    }

    @Override
    public void onDetach() {

        super.onDetach();
    }

    @SuppressLint("LongLogTag")
    public boolean startFragment(
            Fragment caller, String fragmentClass, int requestCode, Bundle extras) {
        if (getActivity() instanceof PreferenceActivity) {
            PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
            preferenceActivity.startPreferencePanel(fragmentClass, extras, R.string.app_name, null, caller, requestCode);
            return true;
        } else {
            ULog.w(TAG, "Parent isn't PreferenceActivity, thus there's no way to launch the "
                    + "given Fragment (name: " + fragmentClass + ", requestCode: " + requestCode
                    + ")");
            return false;
        }
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 