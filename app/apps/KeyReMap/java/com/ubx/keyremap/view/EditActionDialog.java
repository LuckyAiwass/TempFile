package com.ubx.keyremap.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;

public abstract class EditActionDialog {

    private static final String TAG = Utils.TAG + "#" + EditActionDialog.class.getSimpleName();

    private Context mContext;
    private View mRootView;
    private EditText mActionEdit;
    private AlertDialog.Builder mDialogBuilder;

    public EditActionDialog(Context context) {
        mContext = context;
        mDialogBuilder = new AlertDialog.Builder(mContext);
    }

    public void show() {
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_action_edit, null);
        mActionEdit = (EditText) mRootView.findViewById(R.id.action_edit);

        mDialogBuilder.setTitle(R.string.dialog_title_action_edit);
        mDialogBuilder.setView(mRootView);
        mDialogBuilder.setPositiveButton(android.R.string.ok, null);

        final AlertDialog dialog = mDialogBuilder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionEdit.getText() == null || mActionEdit.getText().toString().isEmpty()) {
                    Toast.makeText(mContext, "input illegal, check again please!", Toast.LENGTH_SHORT).show();
                    return;
                }
                onDialogOK(mActionEdit.getText().toString());
                dialog.dismiss();
            }
        });

    }

    public abstract void onDialogOK(String action);
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         