package com.ubx.keyremap;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;

import com.ubx.keyremap.data.ListItem;

import java.util.List;

public interface IContract {
    interface IView<P> {
        void setPresenter(P presenter);
        void showHomeButton(boolean show);
        void updateTitle(String title);
    }

    interface IPresenter<V> {
        void register(V view);
        void unRegister();
        void initViews();
        void refreshUI();
    }

    interface IModel {

    }

    interface IMainView {
        void showListView(boolean show);
        void showResetMapView(boolean show);
        void setKeyCodeName(String code, String name);
        void setRemapTypeText(String text);
    }

    interface IMainPresenter {
        List getRemapTypes();
        void doListViewClick(int position);
        boolean interceptionKeyEvent(KeyEvent event);
        void updateListViewState();
        void resetCurrentKeyMap();
    }

    interface IFragmentView {
        void setFragment(Fragment fragment);
        void setFragmentView(View view);
    }

    interface IRemapDetailView extends IFragmentView {
        void setMapCodeName(int code, String name);
        void setAppContent(Drawable icon, String label, String pkg);
        void setBroadcastSummary(String down, String up);
        void notifyIntentExtList();
        int wakeupEnable();
        void setWakeUpVisibility(boolean visible);
        boolean isKeyDownBroadcastEnable();
        boolean isKeyUpBroadcastEnable();
    }

    interface IIntentExtraPresenter {
        List getIntentExtras();
        void addIntentExtra(ListItem.IntentExtra extra);
        void removeIntentExtra(int position);
    }

    interface IRemapDetailPresenter extends IIntentExtraPresenter {
        void setFragmentView(View view);
        void startPickActivity();
        void startEditBroadcastActivity(int requestCode);
        void setMapCodeName(int code, String name);
        void setAppSelectedResult(Intent data);
        void setBroadcastEditResult(Intent data, int requestCode);
        List getAllKeyNames();
        String findKeyName(int position);
        String getKeyDownAction();
        String getKeyUpAction();
        int findKeyCode(String name);
        void doRemap();
    }

    interface IEditBroadcastView extends IFragmentView {
        void setBroadcastContent(String action);
        void notifyIntentExtList();
    }

    interface IEditBroadcastPresenter extends IIntentExtraPresenter {
        void setFragmentView(View view);
        void setBroadcastAction(String action);
        void confirmEdit();
    }

    interface IRemapResultView {
        void notifyMappedKeyList(Cursor cursor);
    }

    interface IRemapResultPresenter {
        void initListData();
        void updateFilesData();
        void deleteAll(String selection);
        void importConfig();
        void exportConfig();
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          