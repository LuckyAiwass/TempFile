package com.ubx.scanwedge.settings.dialog;

import com.ubx.scanwedge.settings.BasePresenter;
import com.ubx.scanwedge.settings.dialog.AppDialog.*;
import com.ubx.scanwedge.settings.dialog.IDialogContract.*;
import com.ubx.scanwedge.settings.dialog.DialogModel.*;
import com.ubx.scanwedge.R;

import java.util.List;
import java.util.Map;

/**
 * DialogPresenter
 * @param <T>
 */
public abstract class DialogPresenter<T extends AppDialog> extends BasePresenter<AppDialog> {

    protected IDialogView mView;

    public DialogPresenter(IBaseView view) {
        this.mView = (IDialogView) view;
    }

    /**
     * AddProfilePresenter
     */
    public static class AddProfilePresenter extends DialogPresenter<AddProfileDialog> implements IDialogContract.IAddProfilePresenter {
        private IAddProfileView mExtView;
        private AddProfileModel mModel;

        public AddProfilePresenter(IBaseView view) {
            super(view);
            this.mExtView = (IAddProfileView) view;
            this.mModel = (AddProfileModel) DialogModel.createModel("AddProfile");
        }

        @Override
        public void dialogOk() {
            mModel.setProfileName(mExtView.getProfileText());
            if (mModel.getProfileName() == null || mModel.getProfileName().isEmpty()) {
                mView.showToast(mView.getContext().getString(R.string.update_profile_message));
                return;
            }
            if (mModel.getPackageName() == null || mModel.getPackageName().isEmpty()) {
                mView.showToast("no available app");
                return;
            }

            mView.showProcess(R.string.scanner_update_progress);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    mExtView.updateHeaders(mModel.addNewSettings(mView.getContext()));
                }
            }).start();
        }


        @Override
        public void doItemSelected(String text) {
            mModel.setPackageName(mExtView.getPackageText(text));
            mExtView.updateEdit(mModel.getPackageName().substring(mModel.getPackageName().lastIndexOf(".") + 1));
        }

        @Override
        public void doNothingSelected() {
            mModel.setPackageName(null);
        }

        @Override
        public List<Map<String, Object>> getAppMapList() {
            return mModel.getAppMapList(mView.getContext());
        }
    }

    /**
     * RenameProfilePresenter
     */
    public static class RenameProfilePresenter extends DialogPresenter<RenameProfileDialog> implements IRenameProfilePresenter{
        private IRenameProfileView mExtView;
        private RenameProfileModel mModel;

        public RenameProfilePresenter(IBaseView view) {
            super(view);
            this.mExtView = (IRenameProfileView) view;
            this.mModel = (RenameProfileModel) DialogModel.createModel("RenameProfile");
        }

        @Override
        public void dialogOk() {
            mModel.setNewProfileName(mExtView.getProfileText());
            if (mModel.isProfileNameChanged()) {
                mView.showProcess(R.string.scanner_update_progress);
                mExtView.updateHeaders(mModel.updateProfileName(mView.getContext()));
            } else {
                mView.showToast(mView.getContext().getString(R.string.update_profile_message));
            }
        }

        @Override
        public void initOldName(String text) {
            mModel.setOldProfileName(text);
            mExtView.initProfileText(text);
        }
    }
    /**
     * CloneProfilePresenter
     */
    public static class CloneProfilePresenter extends DialogPresenter<CloneProfileDialog> implements ICloneProfilePresenter{
        private IRenameProfileView mExtView;
        private CloneProfileModel mModel;

        public CloneProfilePresenter(IBaseView view) {
            super(view);
            this.mExtView = (IRenameProfileView) view;
            this.mModel = (CloneProfileModel) DialogModel.createModel("CloneProfile");
        }

        @Override
        public void dialogOk() {
            mModel.setNewProfileName(mExtView.getProfileText());
            if (mModel.isProfileNameChanged()) {
                mView.showProcess(R.string.scanner_update_progress);
                mExtView.updateHeaders(mModel.cloneProfileName(mView.getContext()));
            } else {
                mView.showToast(mView.getContext().getString(R.string.update_profile_message));
            }
        }

        @Override
        public void initOldName(String text, int profileid) {
            mModel.setOldProfileName(text);
            mModel.setOldProfileId(profileid);
            mExtView.initProfileText(text);
        }
    }
    /**
     * AttachPackagePresenter
     */
    public static class AttachPackagePresenter extends DialogPresenter<AttachPackageDialog> implements IAttachPackagePresenter {
        IAttachPackageView mExtView;
        AttachPackageModel mModel;

        public AttachPackagePresenter(IBaseView view) {
            super(view);
            this.mExtView = (IAttachPackageView) view;
            this.mModel = (AttachPackageModel) DialogModel.createModel("AttachPackage");
        }

        @Override
        public void initProfileId(int profileId) {
            mModel.setProfileId(profileId);
        }

        @Override
        public void doItemClick(String text) {
            mModel.setPackageName(text);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final boolean ans = mModel.refreshAppList(mView.getContext());
                    mExtView.updateFragmentView(ans);
                }
            }).start();
        }

        @Override
        public void dialogOk() {
            // do nothing
        }

        @Override
        public List<Map<String, Object>> getAppMapList() {
            return mModel.getAppMapList(mView.getContext());
        }
    }

    public static class ConfirmPresenter extends DialogPresenter<ConfirmDialog> {

        public ConfirmPresenter(IBaseView view) {
            super(view);
        }
    }
}
