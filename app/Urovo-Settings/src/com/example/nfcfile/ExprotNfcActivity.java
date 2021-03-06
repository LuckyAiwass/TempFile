package com.example.nfcfile;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.widget.TextView;
import com.urovo.bluetooth.scanner.R;
import android.os.Environment;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.provider.Settings;

public class ExprotNfcActivity extends Activity implements NfcAdapter.CreateBeamUrisCallback {
    private NfcAdapter mNfcAdapter;
    private  String targetFilename = "/sdcard/Custom_Local/default_Settings_property.xml";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        TextView textView = (TextView)findViewById(R.id.nfctext);
        textView.setText(getString(R.string.text_export_nfc_contact));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNfcAdapter = mNfcAdapter.getDefaultAdapter(this);
        Log.i("urovo","mNfcAdapter:"+mNfcAdapter);
        Log.i("urovo","mNfcAdapter.isEnabled():"+mNfcAdapter.isEnabled());
        if (mNfcAdapter != null && mNfcAdapter.isEnabled()) {

        }else{
            IsToSet(this);
            // Toast.makeText(this, "NFC 未打开，请打开NFC!!", Toast.LENGTH_SHORT).show();
        }
        mNfcAdapter.setBeamPushUrisCallback(this, this);
    }

    @Override
    public Uri[] createBeamUris(NfcEvent event) {
        File sdCardDir = Environment.getExternalStorageDirectory();
        targetFilename=sdCardDir+"/Custom_Local/default_Settings_property.xml";
        File file = new File(targetFilename);
        if (file.exists()){
            android.os.SystemProperties.set("persist.sys.nfc.u-setting","true");
            Uri[] uris = new Uri[1];
            Uri uri = Uri.parse("file://" + targetFilename);
            uris[0] = uri;
            return uris;
        }else {
            android.os.SystemProperties.set("persist.sys.nfc.u-setting","false");
            Toast.makeText(this, getString(R.string.text_nfc_exprot_fail), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private  void IsToSet(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(getString(R.string.text_nfc_no_open));
        // builder.setTitle("提示");
        builder.setPositiveButton(getString(R.string.main_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent("android.settings.NFC_SETTINGS");
                activity.startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.main_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }); builder.create().show();
    }


}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       