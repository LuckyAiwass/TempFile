package com.ubx.scanwedge.settings.fragments;

import com.ubx.scanwedge.settings.BasePresenter;
import com.ubx.scanwedge.settings.fragments.IFragmentContract.*;
import com.ubx.database.helper.USettings;

import java.util.List;
import java.util.Map;

public class FragmentPresenter<T extends SettingsPreferenceFragment> extends BasePresenter<SettingsPreferenceFragment> {

    public static class AssociatedAppsPresenter extends FragmentPresenter<AssociatedApps> implements IAssociatedAppsPresenter {
        IAssociatedAppsView mView;

        public AssociatedAppsPresenter(IBaseView view) {
            this.mView = (IAssociatedAppsView) view;
        }

        @Override
        public List<Map<String, Object>> getAssociatedApps(int profileId) {
            return AppAdapter.getAssociatedApps(mView.getContext().getPackageManager(), mView.getContext().getContentResolver(), profileId);
        }

        @Override
        public void deleteAssociatedApp(int profileId, final String pkg) {
            USettings.AppList.deleteAssociatedApp(mView.getContext().getContentResolver(), profileId, pkg);
            mView.updatePreferences(AppAdapter.getAssociatedApps(mView.getContext().getPackageManager(), mView.getContext().getContentResolver(), profileId));
        }

    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              