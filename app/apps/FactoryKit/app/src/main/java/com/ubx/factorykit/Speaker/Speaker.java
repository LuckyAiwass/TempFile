/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.ubx.factorykit.Speaker;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ubx.factorykit.R;
import com.ubx.factorykit.Utilities;

public class Speaker extends Activity {

    String mAudiofilePath;
    static String TAG = "Speaker";
    MediaPlayer mMediaPlayer = new MediaPlayer();
    boolean isPlaying = false;
    Button playButton = null;
    Button stopButton = null;
    AudioManager mAudioManager;
    Context mContext;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.speaker);

        mContext = this;
        isPlaying = false;

        getService();
        bindView();

        if (mAudioManager.isWiredHeadsetOn())
            showWarningDialog(getString(R.string.remove_headset));

        setAudio();

    }

    @Override
    public void finish() {
        // AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_WIRED_HEADSET,
        // AudioSystem.DEVICE_STATE_UNAVAILABLE, "");
        AudioSystem.setForceUse(AudioSystem.FOR_MEDIA, AudioSystem.FORCE_NONE);
        stop();
        super.finish();
    }

    void play() throws IllegalArgumentException, IllegalStateException, IOException {

        final TextView mTextView = (TextView) findViewById(R.id.speaker_hint);
        mTextView.setText(getString(R.string.speaker_playing));
        // AudioSystem.setForceUse(AudioSystem.FOR_MEDIA,
        // AudioSystem.FORCE_SPEAKER);
        // mAudioManager.setMode(AudioManager.MODE_RINGTONE);
        try {
            if (mMediaPlayer == null) {
                mMediaPlayer = new MediaPlayer();
            }
            mMediaPlayer.reset();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mMediaPlayer = MediaPlayer.create(mContext, R.raw.qualsound);
            // Uri uri =
            // RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            // mMediaPlayer = MediaPlayer.create(mContext, uri);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
            isPlaying = true;
            showConfirmDialog();
        } catch (Exception e) {
            loge(e);
        }

    }

    void stop() {
        
        if (isPlaying == true && mMediaPlayer.isPlaying() == true) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            isPlaying = false;
        }
    }

    void showWarningDialog(String title) {
        
        new AlertDialog.Builder(mContext).setTitle(title)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setCancelable(false).show();

    }

    void showConfirmDialog() {

        new AlertDialog.Builder(mContext).setTitle(getString(R.string.speaker_confirm))
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

        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        AudioSystem.setForceUse(AudioSystem.FOR_MEDIA, AudioSystem.FORCE_SPEAKER);
        // AudioSystem.setDeviceConnectionState(AudioSystem.DEVICE_OUT_WIRED_HEADSET,
        // AudioSystem.DEVICE_STATE_AVAILABLE,
        // "");
        // AudioSystem.setForceUse(AudioSystem.FOR_MEDIA,
        // AudioSystem.FORCE_WIRED_ACCESSORY);
        float ratio = 1.0f;
        int mStreamVolume = 15; //urovo yuanwei ???????????? 2019-05-27 11 -> 15
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                mStreamVolume /*(int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC))*/, 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                mStreamVolume /*(int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL))*/, 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_RING,
                mStreamVolume /*(int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING))*/, 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
                mStreamVolume /*(int) (ratio * mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM))*/, 0);
    }

    void bindView() {

        playButton = (Button) findViewById(R.id.speaker_play);
        stopButton = (Button) findViewById(R.id.speaker_stop);
        final TextView mTextView = (TextView) findViewById(R.id.speaker_hint);
        mTextView.setText(getString(R.string.speaker_to_play));

        playButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                try {
                    play();
                } catch (Exception e) {
                    loge(e);
                }
            }
        });

        stopButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {

                if (isPlaying) {
                    stop();
                } else
                    showWarningDialog(getString(R.string.speaker_play_first));
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
