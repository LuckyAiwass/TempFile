package com.ubx.keyremap.view;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ubx.keyremap.R;
import com.ubx.keyremap.Utils;

public abstract class ExtraAddDialog {

    private static final String TAG = Utils.TAG + "#" + ExtraAddDialog.class.getSimpleName();

    private Context mContext;
    private View mRootView;
    private EditText mKeyEdit;
    private EditText mValueEdit;
    private AlertDialog.Builder mDialogBuilder;
    private String mTitle;

    public ExtraAddDialog(Context context, String title) {
        mContext = context;
        mTitle = title;
        mDialogBuilder = new AlertDialog.Builder(mContext);
    }

    public ExtraAddDialog(Context context, int title) {
        mContext = context;
        mTitle = mContext.getString(title);
        mDialogBuilder = new AlertDialog.Builder(mContext);
    }

    public void show() {
        mRootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_intent_ext_add, null);
        mKeyEdit = (EditText) mRootView.findViewById(R.id.intent_ext_key_edit);
        mValueEdit = (EditText) mRootView.findViewById(R.id.intent_ext_value_edit);

        mDialogBuilder.setTitle(mTitle);
        mDialogBuilder.setView(mRootView);
        mDialogBuilder.setPositiveButton(android.R.string.ok, null);

        final AlertDialog dialog = mDialogBuilder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mKeyEdit.getText() == null || mKeyEdit.getText().toString().isEmpty()) {
                    Toast.makeText(mContext, "input extra key at least.", Toast.LENGTH_SHORT).show();
                    return;
                }
                onDialogOK(mKeyEdit.getText().toString(), mValueEdit.getText().toString());
                dialog.dismiss();
            }
        });

    }

    public abstract void onDialogOK(String key, String value);
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  