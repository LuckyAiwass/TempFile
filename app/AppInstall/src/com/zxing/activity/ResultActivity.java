package com.zxing.activity;

import java.io.UnsupportedEncodingException;  

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.ubx.appinstall.R;

import com.ubx.appinstall.util.AutoInstallConfig;
import com.zxing.decode.DecodeThread;

public class ResultActivity extends Activity {

	private ImageView mResultImage;
	private TextView mResultText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);

		Bundle extras = getIntent().getExtras();

		mResultImage = (ImageView) findViewById(R.id.result_image);
		mResultText = (TextView) findViewById(R.id.result_text);

		if (null != extras) {
			int width = extras.getInt("width");
			int height = extras.getInt("height");

			LayoutParams lps = new LayoutParams(width, height);
			lps.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, getResources().getDisplayMetrics());
			lps.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
			lps.rightMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());

			mResultImage.setLayoutParams(lps);

			String result = extras.getString("result");

			String url = extras.getString("result");
			DownloadManager mDownloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			String downloadName = url.split("/")[url.split("/").length - 1];
			String downloadDir = AutoInstallConfig.AUTOPATH;
			request.setDestinationInExternalPublicDir(downloadDir,downloadName);
			mDownloadManager.enqueue(request);

			try {
				mResultText.setText(new String(result.getBytes("ISO-8859-1"), "GB2312").toString());
				//mResultText.setText(new String(result.getBytes("GB2312"), "GB2312").toString());
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			//???????????????
			Bitmap barcode = null;
			byte[] compressedBitmap = extras.getByteArray(DecodeThread.BARCODE_BITMAP);
			if (compressedBitmap != null) {
				barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
				// Mutable copy:
				barcode = barcode.copy(Bitmap.Config.RGB_565, true);
			}

			mResultImage.setImageBitmap(barcode);
		}
	}
}
