package com.ubx.scanwedge.settings.dialog;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;

import com.ubx.scanwedge.settings.utils.ULog;
import com.ubx.scanwedge.settings.fragments.AppAdapter;
import com.ubx.database.helper.USettings;

import java.util.List;
import java.util.Map;

/**
 * DialogModel
 */
public class DialogModel implements IDialogModel {
    public static final String ADD_PROFILE = "AddProfile";
    public static final String RENAME_PROFILE = "RenameProfile";
    public static final String ATTACH_PACKAGE = "AttachPackage";
    public static final String CLONE_PROFILE = "CloneProfile";
    public static IDialogModel createModel(String model) {
        switch (model) {
            case ADD_PROFILE:
                return new AddProfileModel();
            case RENAME_PROFILE:
                return new RenameProfileModel();
            case ATTACH_PACKAGE:
                return new AttachPackageModel();
            case CLONE_PROFILE:
                return new CloneProfileModel();
        }
        return null;
    }

    /**
     * AddProfileModel
     */
    public static class AddProfileModel implements IDialogModel {
        private static final String TAG = ULog.TAG + AddProfileModel.class.getSimpleName();

        private String mProfileName;
        private String mPackageName;

        private List<Map<String, Object>> mAppMapList;

        public String getProfileName() {
            return mProfileName;
        }

        public void setProfileName(String profileName) {
            this.mProfileName = profileName;
        }

        public String getPackageName() {
            return mPackageName;
        }

        public void setPackageName(String packageName) {
            this.mPackageName = packageName;
        }

        public List<Map<String, Object>> getAppMapList(Context context) {
            mAppMapList = getAppMapList(context.getPackageManager(), context.getContentResolver());

            if (mAppMapList != null && mAppMapList.size() > 0 && mPackageName == null) {
                mPackageName = mAppMapList.get(0).get(AppAdapter.NAME).toString();
            }
            return mAppMapList;
        }

        public List<Map<String, Object>> getAppMapList(PackageManager pm, ContentResolver helper) {
            return AppAdapter.getAppMapList(pm, helper);
        }

        public boolean addNewSettings(Context context) {
            int profileId = (int) USettings.Profile.createProfile(context.getContentResolver(), mProfileName, true, true);
            if (profileId == -1) {
                ULog.e(TAG, "add TABLE_PROFILES error, profileName already exist");
                return false;
            }
            if(mPackageName != null) {
                int listId = (int) USettings.AppList.refreshList(context.getContentResolver(), profileId, mPackageName);
                if (listId == -1) {
                    ULog.e(TAG, "add TABLE_APP_LIST error, packageName already added");
                    return false;
                }
            }

            USettings.System.initSettings(context, profileId);
            return true;
        }
    }

    /**
     * RenameProfileModel
     */
    public static class RenameProfileModel implements IDialogModel {
        private String mOldProfileName;
        private String mNewProfileName;

        public void setNewProfileName(String newProfileName) {
            this.mNewProfileName = newProfileName;
        }

        public void setOldProfileName(String oldProfileName) {
            this.mOldProfileName = oldProfileName;
        }

        public boolean isProfileNameChanged() {
            if (mOldProfileName == null || mNewProfileName == null) {
                return false;
            }
            return !mOldProfileName.equals(mNewProfileName);
        }

        public boolean updateProfileName(Context context) {
            return USettings.Profile.renameProfile(context.getContentResolver(), mNewProfileName, mOldProfileName) != -1;
        }
    }
    /**
     * CloneProfileModel
     */
    public static class CloneProfileModel implements IDialogModel {
        private String mOldProfileName;
        private String mNewProfileName;
        private int oldProfileId;
        public void setNewProfileName(String newProfileName) {
            this.mNewProfileName = newProfileName;
        }

        public void setOldProfileName(String oldProfileName) {
            this.mOldProfileName = oldProfileName;
        }
        public void setOldProfileId(int profileid) {
            this.oldProfileId = profileid;
        }
        public boolean isProfileNameChanged() {
            if (mOldProfileName == null || mNewProfileName == null) {
                return false;
            }
            return !mOldProfileName.equals(mNewProfileName);
        }

        public boolean cloneProfileName(final Context context) {
            final long newProfile = USettings.Profile.createProfile(context.getContentResolver(), mNewProfileName, true, true);
            if (newProfile == -1) {
                ULog.e("WedgeCloneProfile", "add TABLE_PROFILES error, profileName already exist");
                return false;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    USettings.System.cloneSettings(context.getContentResolver(), oldProfileId, newProfile);
                }
            }).start();
            return newProfile != -1;
        }
    }
    /**
     * AttachPackageModel
     */
    public static class AttachPackageModel implements IDialogModel {
        private static final String TAG = ULog.TAG + AttachPackageModel.class.getSimpleName();

        private int profileId;
        private String mPackageName;

        public int getProfileId() {
            return profileId;
        }

        public void setProfileId(int profileId) {
            this.profileId = profileId;
        }

        public void setPackageName(String packageName) {
            this.mPackageName = packageName;
        }

        private List<Map<String, Object>> mAppMapList;

        public List<Map<String, Object>> getAppMapList(Context context) {
            mAppMapList = getAppMapList(context.getPackageManager(), context.getContentResolver());

            return mAppMapList;
        }

        public List<Map<String, Object>> getAppMapList(PackageManager pm, ContentResolver helper) {
            return AppAdapter.getAppMapList(pm, helper);
        }

        public boolean refreshAppList(Context context) {
            if (mPackageName != null && !mPackageName.isEmpty()) {
                int listId = (int) USettings.AppList.refreshList(context.getContentResolver(), profileId, mPackageName);
                if (listId == -1) {
                    ULog.e(TAG, "refresh TABLE_APP_LIST error, packageName already added");
                    return false;
                }
                return true;
            }
            return false;
        }
    }
}

