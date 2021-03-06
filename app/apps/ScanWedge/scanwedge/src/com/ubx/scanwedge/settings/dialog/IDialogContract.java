package com.ubx.scanwedge.settings.dialog;

import android.content.Context;

import java.util.List;
import java.util.Map;

/**
 * Contract for App Dialogs
 */
public interface IDialogContract {

    interface IBaseView {

    }

    /**
     * base dialog interfaces
     */
    interface IDialogView extends IBaseView {
        boolean show();
        boolean hide();
        void showProcess(int id);
        void hideProcess();
        void showToast(String msg);
        Context getContext();
    }

    /**
     * IAddProfileView
     */
    interface IAddProfileView extends IBaseView {
        String getPackageText(String text);
        String getProfileText();
        void updateEdit(String text);
        void updateHeaders(final boolean hide);
    }

    /**
     * IRenameProfileView
     */
    interface IRenameProfileView extends IBaseView {
        void initProfileText(String text);
        void updateHeaders(final boolean hide);
        String getProfileText();
    }

    /**
     * IRenameProfileView
     */
    interface IAttachPackageView extends IBaseView {
        void updateFragmentView(final boolean hide);
    }

    /**
     * base dialog presenter
     */
    interface IDialogPresenter {
        void dialogOk();
    }

    /**
     * AddProfile dialog presenter
     */
    interface IAddProfilePresenter extends IDialogPresenter {
        void doItemSelected(String text);
        void doNothingSelected();
        List<Map<String, Object>> getAppMapList();
    }

    /**
     * AddProfile dialog presenter
     */
    interface IRenameProfilePresenter extends IDialogPresenter {
        void initOldName(String text);
    }
    interface ICloneProfilePresenter extends IDialogPresenter {
        void initOldName(String text, int profileid);
    }
    /**
     * AttachPackage dialog presenter
     */
    interface IAttachPackagePresenter extends IDialogPresenter {
        List<Map<String, Object>> getAppMapList();
        void initProfileId(int profileId);
        void doItemClick(String text);
    }

}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 