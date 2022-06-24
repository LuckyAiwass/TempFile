package com.ubx.scanwedge.settings.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.settings.utils.ServiceMenuKeySequence;

import java.util.List;

/**
 * Created by xjf on 19-07-04.
 */
public class Settings extends PreferenceActivity {
    private static final String LOG_TAG = "Settings";
    ServiceMenuKeySequence mServiceInvoker = new ServiceMenuKeySequence();

    private static final String META_DATA_KEY_HEADER_ID =
            "com.ubx.scanwedge.TOP_LEVEL_HEADER_ID";
    private static final String META_DATA_KEY_FRAGMENT_CLASS =
            "com.ubx.scanwedge.FRAGMENT_CLASS";
    private static final String META_DATA_KEY_PARENT_TITLE =
            "com.ubx.scanwedge.PARENT_FRAGMENT_TITLE";
    private static final String META_DATA_KEY_PARENT_FRAGMENT_CLASS =
            "com.ubx.scanwedge.PARENT_FRAGMENT_CLASS";


    private static final String SAVE_KEY_CURRENT_HEADER = "com.ubx.scanwedge.CURRENT_HEADER";
    private static final String SAVE_KEY_PARENT_HEADER = "com.ubx.scanwedge.PARENT_HEADER";

    private String mFragmentClass;
    private int mTopLevelHeaderId;
    private Header mFirstHeader;
    private Header mCurrentHeader;
    private Header mParentHeader;
    private boolean mInLocalHeaderSwitch;

    private boolean checkPkgExist(String packageName) {
        try {
            PackageManager pm = this.getPackageManager();
            ApplicationInfo app = pm.getApplicationInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if (app != null) {
                return true;
            }
        } catch (NameNotFoundException e) {
            // Will never happen.
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getMetaData();
        mInLocalHeaderSwitch = true;
        super.onCreate(savedInstanceState);
        /*if(checkPkgExist("com.udroid.scanwedge")) {
            startActivity(new Intent("com.udroid.scanwedge.SETTINGS"));
            finish();
        }*/
        mInLocalHeaderSwitch = false;
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.settings_headers, target);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (mServiceInvoker.keyIn(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        //android.util.Log.d("SubSettings", "Launching fragment " + fragmentName);
        return true;
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (mServiceInvoker == null)
            mServiceInvoker = new ServiceMenuKeySequence();
        mServiceInvoker.setOnInvokeServiceMenuListener(new ServiceMenuKeySequence.OnInvokeServiceMenuListener() {
            public void onServiceMenu() {
                /*if(getTitle().toString().equals(getResources().getString(R.string.settings_label))) {
                    Intent virginPage = new Intent("android.intent.action.INTERNAL_TEST");
                    startActivity(virginPage);
                }*/
            }

        });
    }

    private void switchToHeaderLocal(Header header) {
        mInLocalHeaderSwitch = true;
        switchToHeader(header);
        mInLocalHeaderSwitch = false;
    }

    @Override
    public void switchToHeader(Header header) {
        if (!mInLocalHeaderSwitch) {
            mCurrentHeader = null;
            mParentHeader = null;
        }
        super.switchToHeader(header);
    }

    /**
     * Switch to parent fragment and store the grand parent's info
     *
     * @param className name of the activity wrapper for the parent fragment.
     */
    private void switchToParent(String className) {
        final ComponentName cn = new ComponentName(this, className);
        try {
            final PackageManager pm = getPackageManager();
            final ActivityInfo parentInfo = pm.getActivityInfo(cn, PackageManager.GET_META_DATA);

            if (parentInfo != null && parentInfo.metaData != null) {
                String fragmentClass = parentInfo.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);
                CharSequence fragmentTitle = parentInfo.loadLabel(pm);
                Header parentHeader = new Header();
                parentHeader.fragment = fragmentClass;
                parentHeader.title = fragmentTitle;
                mCurrentHeader = parentHeader;

                switchToHeaderLocal(parentHeader);
                //highlightHeader(mTopLevelHeaderId);

                mParentHeader = new Header();
                mParentHeader.fragment
                        = parentInfo.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
                mParentHeader.title = parentInfo.metaData.getString(META_DATA_KEY_PARENT_TITLE);
            }
        } catch (NameNotFoundException nnfe) {
            Log.w(LOG_TAG, "Could not find parent activity : " + className);
        }
    }

    @Override
    public Intent getIntent() {
        Intent superIntent = super.getIntent();
        String startingFragment = getStartingFragmentClass(superIntent);
        // This is called from super.onCreate, isMultiPane() is not yet reliable
        // Do not use onIsHidingHeaders either, which relies itself on this method
        if (startingFragment != null && !onIsMultiPane()) {
            Intent modIntent = new Intent(superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT, startingFragment);
            Bundle args = superIntent.getExtras();
            if (args != null) {
                args = new Bundle(args);
            } else {
                args = new Bundle();
            }
            args.putParcelable("intent", superIntent);
            modIntent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, superIntent.getExtras());
            return modIntent;
        }
        return superIntent;
    }

    /**
     * Checks if the component name in the intent is different from the Settings class and
     * returns the class name to load as a fragment.
     */
    protected String getStartingFragmentClass(Intent intent) {
        if (mFragmentClass != null) return mFragmentClass;

        String intentClass = intent.getComponent().getClassName();
        if (intentClass.equals(getClass().getName())) return null;

        return intentClass;
    }

    /**
     * Override initial header when an activity-alias is causing Settings to be launched
     * for a specific fragment encoded in the android:name parameter.
     */
    @Override
    public Header onGetInitialHeader() {
        String fragmentClass = getStartingFragmentClass(super.getIntent());
        if (fragmentClass != null) {
            Header header = new Header();
            header.fragment = fragmentClass;
            header.title = getTitle();
            header.fragmentArguments = getIntent().getExtras();
            mCurrentHeader = header;
            return header;
        }

        return mFirstHeader;
    }

    @Override
    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args,
                                             int titleRes, int shortTitleRes) {
        Intent intent = super.onBuildStartFragmentIntent(fragmentName, args,
                titleRes, shortTitleRes);

        intent.setClass(this, SubSettings.class);
        return intent;
    }

    private void getMetaData() {
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(getComponentName(),
                    PackageManager.GET_META_DATA);
            if (ai == null || ai.metaData == null) return;
            mTopLevelHeaderId = ai.metaData.getInt(META_DATA_KEY_HEADER_ID);
            mFragmentClass = ai.metaData.getString(META_DATA_KEY_FRAGMENT_CLASS);

            // Check if it has a parent specified and create a Header object
            final int parentHeaderTitleRes = ai.metaData.getInt(META_DATA_KEY_PARENT_TITLE);
            String parentFragmentClass = ai.metaData.getString(META_DATA_KEY_PARENT_FRAGMENT_CLASS);
            if (parentFragmentClass != null) {
                mParentHeader = new Header();
                mParentHeader.fragment = parentFragmentClass;
                if (parentHeaderTitleRes != 0) {
                    mParentHeader.title = getResources().getString(parentHeaderTitleRes);
                }
            }
        } catch (NameNotFoundException nnfe) {
            // No recovery
        }
    }

    public static class ScannerSettingsActivity extends Settings { /* empty */
    }
}
