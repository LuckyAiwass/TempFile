package com.ubx.scanwedge.settings.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.ubx.scanwedge.R;
import com.ubx.scanwedge.settings.dialog.AppDialog.*;
import com.ubx.scanwedge.settings.fragments.FragmentPresenter.*;
import com.ubx.scanwedge.settings.fragments.IFragmentContract.IAssociatedAppsView;
import com.ubx.scanwedge.settings.utils.ULog;
import com.ubx.database.helper.USettings;

import java.util.List;
import java.util.Map;

public class AssociatedApps extends SettingsPreferenceFragment<AssociatedAppsPresenter> implements IAssociatedAppsView {
    private static final String TAG = ULog.TAG + AssociatedApps.class.getSimpleName();

    private int profileId;
    private PreferenceScreen root;
    private Activity mContext;
    private ListView mListView;

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void initPresenter() {
        mPresenter = new AssociatedAppsPresenter(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        initPresenter();
        initActionBar();

        Bundle args = getArguments();
        profileId = args != null ? args.getInt("profileId") : USettings.Profile.DEFAULT_ID;
    }
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        updatePreferences();
    }
    @Override
     public void initActionBar() {
        mContext.setTitle(R.string.scanner_associated_apps_title);
        mContext.getActionBar().setCustomView(null);

        View menuBtnView = LayoutInflater.from(mContext).inflate(R.layout.menu_button, null);
        ImageView menuButton = (ImageView) menuBtnView.findViewById(R.id.menu_button_view);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 创建PopupMenu对象
                PopupMenu popup = new PopupMenu(mContext, view);

                Menu menu = popup.getMenu();
                menu.add(0, R.integer.id_associated_add, 0, getString(R.string.associated_add));

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        ULog.v(TAG, "menu item " + id + " clicked");
                        if (id == R.integer.id_associated_add) {
                            new AttachPackageDialog(mContext,profileId,AssociatedApps.this).show();
                        }
                        return true;
                    }
                });

                popup.show();
            }
        });

        mContext.getActionBar().setDisplayShowCustomEnabled(true);
        mContext.getActionBar().setCustomView(menuBtnView, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.RIGHT));
    }

    @Override
    public void updatePreferences() {
        updatePreferences(mPresenter.getAssociatedApps(profileId));
    }

    @Override
    public void updatePreferences(List<Map<String, Object>> apps) {
        root = getPreferenceScreen();
        if (root != null) {
            root.removeAll();
        }

        addPreferencesFromResource(R.xml.scanner_settings);
        root = getPreferenceScreen();

        for (int i=0; i<apps.size(); ++i) {
            Preference preference = new Preference(mContext);
            preference.setIcon((Drawable) apps.get(i).get(AppAdapter.ICON));
            preference.setTitle((String) apps.get(i).get(AppAdapter.NAME));

            root.addPreference(preference);
        }

        setPreferenceScreen(root);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListView = getListView();
        if (mListView != null) {
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView) parent;
                    ListAdapter listAdapter = listView.getAdapter();
                    Preference obj = (Preference) listAdapter.getItem(position);
                    final String selectedPkg = obj.getTitle().toString();

                    String title = getString(R.string.dialog_delete_associate_app_title);
                    String message = getString(R.string.dialog_delete_associate_app_message, selectedPkg);
                    new ConfirmDialog(mContext, title, message) {
                        @Override
                        protected void confirmOk() {
                            AssociatedApps.this.mPresenter.deleteAssociatedApp(profileId, selectedPkg);
                        }
                    }.show();
                    return true;
                }
            });
        }
    }
}
