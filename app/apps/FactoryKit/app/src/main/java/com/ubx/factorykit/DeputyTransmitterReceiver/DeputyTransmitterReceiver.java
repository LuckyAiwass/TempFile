/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */
package com.ubx.factorykit.DeputyTransmitterReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Environment;
import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class DeputyTransmitterReceiver extends Activity {

    String mAudiofilePath;
    static String TAG = "DeputyTransmitterReceiver";
    MediaRecorder mMediaRecorder = new MediaRecorder();
    boolean isRecording = false;
    Button recordButton = null;
    Button stopButton = null;
    AudioManager mAudioManager;
    Context mContext;
	private static final String CIT_MIC_MODE = "persist.sys.cit.mic.mode";
	private String mic_mode = "";
	private static final String CURRENT_MIC_MODE = "2";
	private final String OUTPUT_PATH = new File(Environment.getExternalStorageDirectory(),
                "record.wav").getAbsolutePath();

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.transmitter_receiver);

        mContext = this;
        isRecording = false;
        try{
		mic_mode = SystemProperties.get(CIT_MIC_MODE, "");
		SystemProperties.set(CIT_MIC_MODE, CURRENT_MIC_MODE);  //set main mic mode
             }catch (Exception e){
            e.printStackTrace();
        }
        
        getService();
        bindView();
        setAudio();
    }

    @Override
    public void finish() {
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        // urovo yuanwei 如果是扬声器模式则关闭 BEGIN 2019.07.11
        if (mAudioManager.isSpeakerphoneOn()) mAudioManager.setSpeakerphoneOn(false);
        // urovo yuanwei 如果是扬声器模式则关闭 END 2019.07.11
        super.finish();
    }
 	@Override
 	protected void onDestroy() {
		if( mic_mode != "" )
			SystemProperties.set(CIT_MIC_MODE, mic_mode);
 		super.onDestroy();
 	}

    void record() throws IllegalStateException, IOException, InterruptedException {

        // urovo GaoGe 2019.07.19 拆除主MIC 辅MIC录音无声音 begin
        //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // urovo GaoGe 2019.07.19 拆除主MIC 辅MIC录音无声音 begin
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        //mMediaRecorder.setOutputFile(this.getCacheDir().getAbsolutePath() + "/test.amr");
        //mAudiofilePath = this.getCacheDir().getAbsolutePath() + "/test.amr";
        mMediaRecorder.setOutputFile(OUTPUT_PATH);
		mMediaRecorder.prepare();
        mMediaRecorder.start();
    }

    void replay() throws IllegalArgumentException, IllegalStateException, IOException {
        final TextView mTextView = (TextView) findViewById(R.id.transmitter_receiver_hint);
        // mTextView.setText(getString(R.string.transmitter_receiver_playing));
        mTextView.setText(getString(R.string.deputy_transmitter_receiver_playing)); //urovo yuanwei 2019.07.11
        stopButton.setClickable(false);
        File file = new File(OUTPUT_PATH);
        FileInputStream mFileInputStream = new FileInputStream(file);
        final MediaPlayer mMediaPlayer = new MediaPlayer();

        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(mFileInputStream.getFD());
        mMediaPlayer.prepare();
        mMediaPlayer.start();

        mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            public void onCompletion(MediaPlayer mPlayer) {

                mPlayer.stop();
                mPlayer.reset();
                mPlayer.release();
                File file = new File(OUTPUT_PATH);
                file.delete();
                
                final TextView mTextView = (TextView) findViewById(R.id.transmitter_receiver_hint);
                mTextView.setText(getString(R.string.transmitter_receiver_replay_end));
                showConfirmDialog();

            }
        });

    }

    void showWarningDialog(String title) {
        
        new AlertDialog.Builder(mContext).setTitle(title)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setCancelable(false).show();

    }

    void showConfirmDialog() {
        
        new AlertDialog.Builder(mContext).setTitle(getString(R.string.transmitter_receiver_confirm))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    
                    public void onClick(DialogInterface dialog, int which) {
                        
                        pass();
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    
                    public void onClick(DialogInterface dialog, int which) {
                        
                        fail(null);
                    }
                }).setCancelable(false).show();
    }

    public void setAudio() {
        // mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        mAudioManager.setSpeakerphoneOn(true);
        // urovo GaoGe 2019.07.19 拆除主MIC 辅MIC录音无声音 begin
        mAudioManager.setParameters("mic_factory_aux=true");
        // urovo GaoGe 2019.07.19 拆除主MIC 辅MIC录音无声音 begin
        float ratio = 1f;
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING)), 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
                (int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)), 0);

    }

    void bindView() {

        recordButton = (Button) findViewById(R.id.transmitter_receiver_start);
        stopButton = (Button) findViewById(R.id.transmitter_receiver_stop);
        final TextView mTextView = (TextView) findViewById(R.id.transmitter_receiver_hint);
        mTextView.setText(getString(R.string.transmitter_receiver_to_record));

        recordButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                if (!mAudioManager.isWiredHeadsetOn()) {

                    mTextView.setText(getString(R.string.transmitter_receiver_recording));
                    try {
                        recordButton.setClickable(false);
                        record();
                        isRecording = true;

                    } catch (Exception e) {
                        loge(e);
                    }
                } else
                    showWarningDialog(getString(R.string.remove_headset));

            }
        });

        stopButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                if (isRecording) {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                    mMediaRecorder.release();

                    try {
                        replay();
                    } catch (Exception e) {
                        loge(e);
                    }
                } else {
                    showWarningDialog(getString(R.string.transmitter_receiver_record_first));
                }
            }
        });
    }

    void getService() {

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    void fail(Object msg) {

        loge(msg);
        toast(msg);
        setResult(RESULT_CANCELED);
        Utilities.writeCurMessage(this, TAG, "Failed");
        finish();
    }

    void pass() {

        setResult(RESULT_OK);
        Utilities.writeCurMessage(this, TAG, "Pass");
        finish();
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(this, s + "", Toast.LENGTH_SHORT).show();
    }

    private void loge(Object e) {

        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(TAG, e + "");
    }

    @SuppressWarnings("unused")
    private void logd(Object s) {

        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();

        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }

}
