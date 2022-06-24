/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.usettings;

import android.content.ComponentName;
import android.content.Context;
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

import java.util.List;

public class Settings extends PreferenceActivity {
    private static final String LOG_TAG = "Settings";
    ServiceMenuKeySequence mServiceInvoker = new ServiceMenuKeySequence();
    
    private static final String META_DATA_KEY_HEADER_ID =
            "com.android.usettings.TOP_LEVEL_HEADER_ID";
        private static final String META_DATA_KEY_FRAGMENT_CLASS =
            "com.android.usettings.FRAGMENT_CLASS";
        private static final String META_DATA_KEY_PARENT_TITLE =
            "com.android.usettings.PARENT_FRAGMENT_TITLE";
        private static final String META_DATA_KEY_PARENT_FRAGMENT_CLASS =
            "com.android.usettings.PARENT_FRAGMENT_CLASS";


        private static final String SAVE_KEY_CURRENT_HEADER = "com.android.usettings.CURRENT_HEADER";
        private static final String SAVE_KEY_PARENT_HEADER = "com.android.usettings.PARENT_HEADER";

        private String mFragmentClass;
        private int mTopLevelHeaderId;
        private Header mFirstHeader;
        private Header mCurrentHeader;
        private Header mParentHeader;
        private boolean mInLocalHeaderSwitch;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getMetaData();
        mInLocalHeaderSwitch = true;
        super.onCreate(savedInstanceState);
        //urovo add jinpu.lin begin 2019.06.24
        if(checkPkgExist("com.udroid.scanwedge")) {
            try{
                startActivity(new Intent("com.udroid.scanwedge.SETTINGS"));
                finish();
            } catch(Exception nnfe) {
                Log.w(LOG_TAG, "Could not find parent com.udroid.scanwedge : ");
            }
        }
        //urovo add end 2019.06.24
        if(Build.PROJECT.equals("SQ46"))
            if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
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
        if(mServiceInvoker.keyIn(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
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
        if(mServiceInvoker == null)
            mServiceInvoker = new ServiceMenuKeySequence();
        mServiceInvoker.setOnInvokeServiceMenuListener(new ServiceMenuKeySequence.OnInvokeServiceMenuListener(){
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

    @Override
    protected boolean isValidFragment(String className) {
		return true;//super.isValidFragment(className);
    }
    
    //urovo add jinpu.lin 2019.06.24
    private boolean checkPkgExist(String packageName) {
        try {
            PackageManager pm=this.getPackageManager();
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
    //urovo add end 2019.06.24

    public static class ScannerSettingsActivity extends Settings { /* empty */ }
}
