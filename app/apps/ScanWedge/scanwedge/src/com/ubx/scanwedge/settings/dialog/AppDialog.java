package com.ubx.scanwedge.settings.dialog;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.preference.PreferenceActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.scanwedge.settings.BasePresenter;
import com.ubx.scanwedge.settings.dialog.DialogPresenter.*;
import com.ubx.scanwedge.R;
import com.ubx.scanwedge.settings.utils.ULog;
import com.ubx.scanwedge.settings.fragments.AppAdapter;
import com.ubx.scanwedge.settings.dialog.IDialogContract.*;
import com.ubx.scanwedge.settings.fragments.IFragmentContract;

/**
 * AppDialog
 *
 * @param <T>
 */
public abstract class AppDialog<T extends BasePresenter> implements IDialogView {

    protected T mPresenter;
    protected Context mContext;

    protected Handler mHandler;

    protected AlertDialog mDialog;
    protected AlertDialog.Builder mBuilder;

    protected ProgressDialog mProgressDialog;

    protected View mLayout;

    public AppDialog(Context context) {
        mContext = context;
        mHandler = new Handler(mContext.getMainLooper());
    }

    abstract void initPresenter();

    abstract void initDialog();

    abstract int getLayout();

    abstract void onOk();

    @Override
    public boolean show() {
        if (mDialog != null && !mDialog.isShowing()) {
            mDialog.show();
            Button positiveBtn = mDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            if (positiveBtn != null) {
                positiveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onOk();
                    }
                });
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hide() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            return true;
        }
        return false;
    }

    @Override
    public void showProcess(int id) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
        }
        mProgressDialog.setMessage(mContext.getResources().getString(id));
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void hideProcess() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    /**
     * AddProfileDialog
     */
    public static class AddProfileDialog extends AppDialog<AddProfilePresenter> implements AdapterView.OnItemSelectedListener, IAddProfileView {

        private EditText mNameEdit;
        private Spinner mAppSpinner;

        public AddProfileDialog(Context context) {
            super(context);
            initPresenter();
            initDialog();
        }

        @Override
        void initPresenter() {
            mPresenter = new AddProfilePresenter(this);
        }

        @Override
        void initDialog() {
            mLayout = View.inflate(mContext, getLayout(), null);

            mNameEdit = (EditText) mLayout.findViewById(R.id.edit_name);
            mAppSpinner = (Spinner) mLayout.findViewById(R.id.app_list);

            mAppSpinner.setAdapter(new AppAdapter(mContext, mPresenter.getAppMapList()));
            mAppSpinner.setSelection(0, true);
            mAppSpinner.setOnItemSelectedListener(this);

            mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setView(mLayout);
            mBuilder.setTitle(R.string.new_profile);
            mBuilder.setPositiveButton(android.R.string.yes, null);
            mBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int pos) {
                    hide();
                }
            });

            mDialog = mBuilder.create();
        }

        @Override
        int getLayout() {
            return R.layout.new_profile_dialog;
        }

        @Override
        void onOk() {
            mPresenter.dialogOk();
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            TextView text = (TextView) view.findViewById(R.id.name);
            if (text.getText() != null) {
                mPresenter.doItemSelected(text.getText().toString());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            mPresenter.doNothingSelected();
        }

        @Override
        public String getPackageText(String text) {
            return text;
        }

        @Override
        public String getProfileText() {
            if (mNameEdit.getText() != null) {
                return mNameEdit.getText().toString();
            }
            return null;
        }

        @Override
        public void updateEdit(String text) {
            if (mNameEdit.getText() == null || mNameEdit.getText().toString().isEmpty()) {
                mNameEdit.setText(text);
            }
        }

        @Override
        public void updateHeaders(final boolean hide) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    hideProcess();
                    if (hide) {
                        hide();
                    }
                    try {
                        ((PreferenceActivity) mContext).invalidateHeaders();
                    } catch (java.lang.ClassCastException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * RenameProfileDialog
     */
    public static class RenameProfileDialog extends AppDialog<RenameProfilePresenter> implements IRenameProfileView {
        private EditText mNameEdit;
        private LinearLayout mSelectAppLayout;

        public RenameProfileDialog(Context context, String oldName) {
            super(context);
            initPresenter();
            initDialog();
            mPresenter.initOldName(oldName);
        }

        @Override
        void initPresenter() {
            mPresenter = new RenameProfilePresenter(this);
        }

        @Override
        void initDialog() {
            mLayout = View.inflate(mContext, getLayout(), null);

            mNameEdit = (EditText) mLayout.findViewById(R.id.edit_name);

            mSelectAppLayout = (LinearLayout) mLayout.findViewById(R.id.select_app_layout);
            mSelectAppLayout.setVisibility(View.GONE);

            mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setView(mLayout);
            mBuilder.setTitle(R.string.rename_profile);
            mBuilder.setPositiveButton(android.R.string.yes, null);
            mBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int pos) {
                    hide();
                }
            });

            mDialog = mBuilder.create();
            mDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        int getLayout() {
            return R.layout.new_profile_dialog;
        }

        @Override
        void onOk() {
            mPresenter.dialogOk();
        }

        @Override
        public void initProfileText(String text) {
            mNameEdit.setText(text);
        }

        @Override
        public void updateHeaders(final boolean hide) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    hideProcess();
                    if (hide) {
                        hide();
                    }
                    try {
                        ((PreferenceActivity) mContext).invalidateHeaders();
                    } catch (java.lang.ClassCastException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public String getProfileText() {
            if (mNameEdit.getText() != null) {
                return mNameEdit.getText().toString();
            }
            return null;
        }
    }
    public static class CloneProfileDialog extends AppDialog<CloneProfilePresenter> implements IRenameProfileView {
        private EditText mNameEdit;
        private LinearLayout mSelectAppLayout;
        private String titleText;
        public CloneProfileDialog(Context context, String oldName, int profileid) {
            super(context);
            titleText = oldName;
            initPresenter();
            initDialog();
            mPresenter.initOldName(oldName, profileid);
        }

        @Override
        void initPresenter() {
            mPresenter = new CloneProfilePresenter(this);
        }

        @Override
        void initDialog() {
            mLayout = View.inflate(mContext, getLayout(), null);

            mNameEdit = (EditText) mLayout.findViewById(R.id.edit_name);

            mSelectAppLayout = (LinearLayout) mLayout.findViewById(R.id.select_app_layout);
            mSelectAppLayout.setVisibility(View.GONE);

            mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setView(mLayout);
            mBuilder.setTitle(titleText);
            mBuilder.setMessage(R.string.update_profile_message);
            mBuilder.setPositiveButton(android.R.string.yes, null);
            mBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int pos) {
                    hide();
                }
            });

            mDialog = mBuilder.create();
            mDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        int getLayout() {
            return R.layout.new_profile_dialog;
        }

        @Override
        void onOk() {
            mPresenter.dialogOk();
        }

        @Override
        public void initProfileText(String text) {
            mNameEdit.setText(text);
        }

        @Override
        public void updateHeaders(final boolean hide) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    hideProcess();
                    if (hide) {
                        hide();
                    }
                    try {
                        ((PreferenceActivity) mContext).invalidateHeaders();
                    } catch (java.lang.ClassCastException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public String getProfileText() {
            if (mNameEdit.getText() != null) {
                return mNameEdit.getText().toString();
            }
            return null;
        }
    }
    /**
     * AttachPackageDialog
     */
    public static class AttachPackageDialog extends AppDialog<AttachPackagePresenter> implements IAttachPackageView, AdapterView.OnItemClickListener {
        private static final String TAG = ULog.TAG + AttachPackageDialog.class.getSimpleName();

        private IFragmentContract.IAssociatedAppsView mFragmentView;

        public AttachPackageDialog(Context context, int profileId, IFragmentContract.IAssociatedAppsView fragment) {
            super(context);
            initPresenter();
            mPresenter.initProfileId(profileId);
            mFragmentView = fragment;
            initDialog();
        }

        @Override
        void initPresenter() {
            mPresenter = new AttachPackagePresenter(this);
        }

        @Override
        void initDialog() {
            mLayout = new ListView(mContext);
            mLayout.setLayoutParams(
                    new AbsListView.LayoutParams(
                            AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));

            ((ListView) mLayout).setAdapter(new AppAdapter(mContext, mPresenter.getAppMapList()));
            ((ListView) mLayout).setOnItemClickListener(this);

            mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setView(mLayout);
            mBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int pos) {
                    hide();
                }
            });

            mDialog = mBuilder.create();
            mDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        int getLayout() {
            return 0;
        }

        @Override
        void onOk() {
            mPresenter.dialogOk();
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            ULog.v(TAG, "item " + i + "clicked");
            TextView text = (TextView) view.findViewById(R.id.name);
            mPresenter.doItemClick((String) text.getText());
        }

        @Override
        public void updateFragmentView(final boolean hide) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (hide) {
                        hide();
                        mFragmentView.updatePreferences();
                    } else {
                        Toast.makeText(mContext, "associate app error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    /**
     * ConfirmDialog
     */
    public static abstract class ConfirmDialog extends AppDialog<ConfirmPresenter> {

        private String mTitle;
        private String mMessage;

        public ConfirmDialog(Context context, String title, String message) {
            super(context);
            this.mTitle = title;
            this.mMessage = message;
            initPresenter();
            initDialog();
        }

        @Override
        void initPresenter() {
            mPresenter = new ConfirmPresenter(this);
        }

        @Override
        void initDialog() {
            mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setTitle(mTitle);
            mBuilder.setMessage(mMessage);
            mBuilder.setPositiveButton(android.R.string.yes, null);
            mBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int pos) {
                    hide();
                }
            });

            mDialog = mBuilder.create();
            mDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        int getLayout() {
            return 0;
        }

        @Override
        void onOk() {
            confirmOk();
            hide();
        }

        protected abstract void confirmOk();
    }
}
