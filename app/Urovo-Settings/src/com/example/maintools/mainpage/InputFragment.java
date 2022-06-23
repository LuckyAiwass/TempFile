package com.example.maintools.mainpage;

import com.urovo.bluetooth.scanner.R;

import android.view.View;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.urovo.bluetooth.scanner.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import com.example.maintools.SettingsProperty;
import android.content.Context;
import android.widget.Toast;
import android.os.Environment;

public class InputFragment extends BaseFragment implements OnClickListener{
    Context mContext;
    @Override
    protected int getContentLayoutRes() {
        return R.layout.fragment_input;
    }

    @Override
    protected void initView(View childView) {

        childView.findViewById(R.id.iv_input_nfc).setOnClickListener(this);
        childView.findViewById(R.id.iv_input_scan).setOnClickListener(this);
        childView.findViewById(R.id.iv_input_local).setOnClickListener(this);
        mContext=this.getActivity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_input_nfc://进入NFC
            {
                SharedPreferences preferences = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
                SharedPreferences.Editor  editor = preferences.edit();
                editor.putBoolean("NFCRead", true);
                editor.commit();
                Intent intent =new Intent(mContext,com.example.nfcfile.ImprotNfcActivity.class);
                mContext.startActivity(intent);


            }
            break;
            case R.id.iv_input_scan://进入扫描
            {
                SharedPreferences preferences = mContext.getSharedPreferences("user", Context.MODE_PRIVATE);
                SharedPreferences.Editor  editor = preferences.edit();
                new Thread(){
                    public void run(){
                        File sdCardDir = Environment.getExternalStorageDirectory();
                        String path = "/Custom_BT/default_Settings_property.xml";
                        File fileName = new File(sdCardDir+path);
                        fileName.delete();//(sdCardDir+path);
                    }
                }.start();

                editor.putBoolean("BTexport", false);
                editor.putBoolean("BTRead", true);
                editor.commit();
                Intent intent =new Intent(mContext,com.bluetoothscan.qrcode.BtScanActivity.class);
                mContext.startActivity(intent);


            }
            break;
            case R.id.iv_input_local ://进入本地
                chooseFile();
                break;

            default:
                break;
        }
    }

    private static final int FILE_SELECT_CODE = 0;

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        File sdCardDir = Environment.getExternalStorageDirectory();
        intent.setType("storage/emulated/0/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            getActivity().startActivityForResult(Intent.createChooser(intent, "Select  file"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(mContext, mContext.getString(R.string.no_file_manager), Toast.LENGTH_SHORT).show();
        }
    }

}
