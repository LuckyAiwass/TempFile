/*
 * Copyright (C) 2019, Urovo Ltd
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
 *
 * @Author: rocky
 * @Date: 19-12-30下午7:20
 */
package com.ubx.scanwedge.settings.activity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.service.ScanWedgeApplication;
import com.ubx.scanwedge.settings.dialog.AppDialog;
import com.ubx.database.helper.UConstants;
import com.ubx.database.helper.USettings;

import java.util.List;

import static com.ubx.database.helper.UConstants.PROFILE_ID;
import static com.ubx.database.helper.UConstants.PROFILE_NAME;

public class ScanWedgeProfileSubSettings extends PreferenceActivity implements View.OnClickListener {
    private static final String TAG = "WedgeProfileSub";
    private ListView headerList = null;
    boolean isMainPage;
    private static final String[] QUERY_PROJECTION = {
            UConstants.ID, PROFILE_NAME, UConstants.PROFILE_DELETABLE, UConstants.PROFILE_EDITABLE, UConstants.PROFILE_ENABLE
    };
    private static final int INDEX_PROFILE_ENABLE = 4;
    private static final String DELETE_SELECTION_WITH_ID = UConstants.ID + "=?";
    private static final String DELETE_SELECTION_WITH_PROFILE_ID = PROFILE_ID + "=?";
    private String mProfileName = USettings.Profile.DEFAULT;
    private int mProfileID = USettings.Profile.DEFAULT_ID;
    private ContentResolver mContentResolver;
    private String mFragmentClass;
    private Header mFirstHeader;
    private Header mCurrentHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
        //getFragmentManager().addOnBackStackChangedListener(this);
        mContentResolver = getContentResolver();
        initActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        invalidateHeaders();
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragment caller, Preference pref) {
        int titleRes = pref.getTitleRes();
        Log.v(TAG, "onPreferenceStartFragment Title:" + getResources().getString(titleRes));
        /*startPreferencePanel(pref.getFragment(), pref.getExtras(), titleRes, pref.getTitle(),
                null, 0);*/
        return true;
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        try {
            Cursor cursor = getContentResolver().query(USettings.Profile.CONTENT_URI_PROFILES, QUERY_PROJECTION, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Header header = new Header();
                    header.id = cursor.getInt(cursor.getColumnIndexOrThrow(UConstants.ID));
                    Log.v(TAG, "header.id = " + header.id);
                    header.title = cursor.getString(cursor.getColumnIndexOrThrow(PROFILE_NAME));

                    String enabled = cursor.getString(cursor.getColumnIndexOrThrow(UConstants.PROFILE_ENABLE));
                    //header.summary = "true".equals(enabled) ? "enabled" : "disabled";

                    Bundle bundle = new Bundle();
                    bundle.putInt(PROFILE_ID, (int) header.id);
                    bundle.putString(PROFILE_NAME, header.title.toString());
                    bundle.putString(UConstants.PROFILE_EDITABLE, cursor.getString(cursor.getColumnIndexOrThrow(UConstants.PROFILE_EDITABLE)));
                    bundle.putString(UConstants.PROFILE_DELETABLE, cursor.getString(cursor.getColumnIndexOrThrow(UConstants.PROFILE_DELETABLE)));
                    //bundle.putBoolean("is-main-page", false);
                    header.extras = bundle;

                    if (USettings.Profile.DEFAULT.equals(header.title)) {
                        mFirstHeader = header;
                    }

                    if (mFragmentClass != null) {
                        header.fragment = mFragmentClass;
                        header.fragmentArguments = bundle;
                    }

                    target.add(header);
                }
                cursor.close();
                cursor = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initActionBar() {
        View menuBtnView = LayoutInflater.from(this).inflate(R.layout.menu_button, null);
        ImageView menuButton = (ImageView) menuBtnView.findViewById(R.id.menu_button_view);
        menuButton.setOnClickListener(this);

        getActionBar().setDisplayShowCustomEnabled(true);
        getActionBar().setCustomView(menuBtnView, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.RIGHT));

        headerList = getListView();
        headerList.setDivider(new ColorDrawable(0xffcccccc));
        headerList.setDividerHeight(1);
        headerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Header current = getHeaders().get(position);
                // ULog.v(TAG, current.title.toString());

                if (current.extras != null) {
                    mProfileID = (int) current.id;
                    mProfileName = (String) current.title;
                    editProfileSettings(mProfileID, mProfileName);
                }
            }
        });
        headerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                showHeaderMenu(view, i);
                return true;
            }
        });
    }

    private void editProfileSettings(int profileID, String profileName) {
        try {
            Bundle bundle = new Bundle();
            bundle.putInt(PROFILE_ID, profileID);
            bundle.putString(PROFILE_NAME, profileName);
            Intent intent = new Intent("com.ubx.scanwedge.SCANNER_SETTINGS");
            intent.putExtras(bundle);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteProfile(final int profileId,
                                      final ContentResolver resolver) {
        if (profileId != 0) {
            resolver.delete(USettings.System.CONTENT_URI_PROPERTY_SETTINGS, DELETE_SELECTION_WITH_PROFILE_ID,
                    new String[]{String.valueOf(profileId)});
            resolver.delete(USettings.AppList.CONTENT_URI_APP_LIST, DELETE_SELECTION_WITH_PROFILE_ID,
                    new String[]{String.valueOf(profileId)});
            resolver.delete(USettings.Profile.CONTENT_URI_PROFILES, DELETE_SELECTION_WITH_ID,
                    new String[]{String.valueOf(profileId)});
        }
    }

    private void showHeaderMenu(View view, int pos) {
        final Header current = getHeaders().get(pos);
        // ULog.v(TAG, current.title.toString());

        PopupMenu popup = new PopupMenu(this, view);
        final Menu menu = popup.getMenu();

        menu.add(0, R.integer.id_profile_clone, 0, getString(R.string.clone_profile));
        if (current.extras != null) {
            String str = current.extras.getString(UConstants.PROFILE_EDITABLE);
            if (str != null && "true".equals(str) || "1".equals(str)) {
                menu.add(0, R.integer.id_profile_edit, 0, getString(R.string.edit_profile));
            }
            str = current.extras.getString(UConstants.PROFILE_DELETABLE);
            if (str != null && "true".equals(str)) {
                menu.add(0, R.integer.id_profile_rename, 0, getString(R.string.rename_profile));
                menu.add(0, R.integer.id_profile_delete, 0, getString(R.string.delete_profile));
            }
            mProfileID = (int) current.id;
            mProfileName = (String) current.title;
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                Log.v(TAG, "menu item " + id + " clicked");
                switch (id) {
                    case R.integer.id_profile_clone:
                        new AppDialog.CloneProfileDialog(ScanWedgeProfileSubSettings.this, (String) current.title, (int) current.id).show();
                        break;
                    case R.integer.id_profile_edit:
                        editProfileSettings(mProfileID, mProfileName);
                        break;
                    case R.integer.id_profile_rename:
                        new AppDialog.RenameProfileDialog(ScanWedgeProfileSubSettings.this, (String) current.title).show();
                        break;
                    case R.integer.id_profile_delete: {
                        new AppDialog.ConfirmDialog(ScanWedgeProfileSubSettings.this,
                                getString(R.string.dialog_delete_profile_title),
                                getString(R.string.dialog_delete_profile_message, current.title)) {
                            @Override
                            protected void confirmOk() {
                                showProcess(R.string.scanner_update_progress);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        deleteProfile((int) current.id, getContentResolver());
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                hideProcess();
                                                invalidateHeaders();
                                            }
                                        });
                                    }
                                }).start();
                            }
                        }.show();
                    }
                    break;
                }
                return true;
            }
        });

        popup.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_button_view: {
                // 创建PopupMenu对象
                PopupMenu popup = new PopupMenu(this, view);
                // 将R.menu.popup_menu菜单资源加载到popup菜单中
                getMenuInflater().inflate(R.menu.dw_popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        Log.v(TAG, "menu item " + id + " clicked");
                        switch (id) {
                            case R.id.new_profile:
                                new AppDialog.AddProfileDialog(ScanWedgeProfileSubSettings.this).show();
                                break;
                            case R.id.dw_settings:
                                Intent intent = new Intent("com.ubx.scanwedge.SCANWEDGE_SETTINGS");
                                intent.putExtra("reset", false);
                                startActivity(intent);
                                break;
                            case R.id.dw_about:
                                String version = String.format(ScanWedgeProfileSubSettings.this.getString(R.string.scanwedge_version), ((ScanWedgeApplication) ScanWedgeProfileSubSettings.this.getApplication()).getAPPVersion());
                                String scanFramework = ((ScanWedgeApplication) ScanWedgeProfileSubSettings.this.getApplication()).getScannerFrameworkVersion();
                                if (TextUtils.isEmpty(scanFramework) == false) {
                                    scanFramework = String.format(ScanWedgeProfileSubSettings.this.getString(R.string.scanning_framework), scanFramework);
                                    version = version + "\n" + scanFramework;
                                }
                                //String decoder_engine_info =  String.format(ScanWedgeProfileSettings.this.getString(R.string.decoder_library_engine),scanFramework);
                                /*Toast.makeText(ScanWedgeProfileSettings.this, version, Toast.LENGTH_LONG).show();*/
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                DialogFragment newFragment = AboutDialogFragment.newInstance(getResources().getString(R.string.about_scanwedge), version);
                                // Show the dialog.
                                newFragment.show(ft, "dialog");
                                break;
                        }
                        return true;
                    }
                });

                popup.show();
            }
            break;
        }
    }
    /*@Override
    public Intent onBuildStartFragmentIntent(String fragmentName, Bundle args, int titleRes, int shortTitleRes) {
        Log.v(TAG, "onBuildStartFragmentIntent "+ fragmentName);
        Intent intent = super.onBuildStartFragmentIntent(fragmentName, args, titleRes, shortTitleRes);
        //intent.setClass(this, SubSettings.class);
        intent.setAction("com.ubx.scanwedge.SCANNER_SETTINGS");

        return intent;
    }*/

    public static class AboutDialogFragment extends DialogFragment {

        public static AboutDialogFragment newInstance(String title, String message) {
            AboutDialogFragment frag = new AboutDialogFragment();
            Bundle args = new Bundle();
            args.putString("text", title);
            args.putString("message", message);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String text = getArguments().getString("text");
            String message = getArguments().getString("message");
            return new AlertDialog.Builder(getActivity())
                    .setIcon(R.drawable.launcher_icon)
                    .setTitle(text)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }
                    )
                    .create();
        }
    }
}
