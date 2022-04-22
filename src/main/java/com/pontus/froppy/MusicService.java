package com.pontus.froppy;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class MusicService extends Service implements OnErrorListener {

    private final IBinder mBinder = new ServiceBinder();
    MediaPlayer mPlayer;
    private int length = 0;
    private float rightVol = 0.0f;
    private float leftVol = 0.0f;

    public void setVolumeLevel(float volume) {
        if (volume > 1 || volume < 0) {
            volume = .5f;
            try {
                throw new Exception("volume value:" + volume + ". And Should be between 0-1. Set to 0.3 my default");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            rightVol = leftVol = volume;
            mPlayer.setVolume(leftVol, rightVol);
        }

    }

    public class ServiceBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPlayer = MediaPlayer.create(this, R.raw.spider_attack);
        mPlayer.setOnErrorListener(this);

        if (mPlayer != null) {
            mPlayer.setLooping(true);
            mPlayer.setVolume(leftVol, rightVol);
        }


        mPlayer.setOnErrorListener(new OnErrorListener() {

            public boolean onError(MediaPlayer mp, int what, int
                    extra) {
                return true;
            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mPlayer == null) return 0;
        mPlayer.start();
        return START_NOT_STICKY;
    }


    public void playMusic() {
        if (mPlayer != null){
            mPlayer.start();
        }else{
            mPlayer = MediaPlayer.create(this, R.raw.spider_attack);
            mPlayer.setOnErrorListener(this);

            if (mPlayer != null) {
                mPlayer.setLooping(true);
                mPlayer.setVolume(leftVol, rightVol);
                mPlayer.start();
            }

        }
    }

    public void pauseMusic() {
        if (mPlayer == null) return;
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            length = mPlayer.getCurrentPosition();

        }
    }

    public void resumeMusic() {

        if (mPlayer == null) return;
        if (!mPlayer.isPlaying()) {
            mPlayer.seekTo(length);
            mPlayer.start();
        }
    }

    public void stopMusic() {

        if (mPlayer == null) return;
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
    }


    @Override
    public void onDestroy()

    {
        super.onDestroy();
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }

    }

    public boolean onError(MediaPlayer mp, int what, int extra) {

        //System.out.println("MusicService.class: ON ERROR: Music player failed");
        //System.out.println("MP is:" + ((mPlayer == null) ? "Null" : "Not Null"));

        if (mPlayer != null) {
            try {
                //System.out.println("Attempting to stop and release");
                mPlayer.stop();
                mPlayer.release();
            } finally {
                //System.out.println("Finally set MP to null");
                mPlayer = null;
            }
        }
        return false;
    }
}


