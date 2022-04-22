package com.pontus.froppy;

/**
 * Created by gabriel yibirin on 2/13/2016.
 */

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.Builder;
import android.provider.MediaStore;

import java.util.HashMap;

public class SoundManager {

    private  SoundPool mSoundPool;
    private  AudioManager  mAudioManager;

    private int[] sm;
    Context context;
    public static final int NUM_SOUNDS = 16;

    public static final int SOUND_WON = 1;
    public static final int GAME_OVER = 2;
    public static final int DEAD_BUG = 3;
    public static final int DEFEND_NEST = 4;
    public static final int EXTRA_SHOTS = 5;
    public static final int FIRE_SHOT = 6;
    public static final int GREAT_JOB = 7;
    public static final int LEVEL_UP = 8;
    public static final int LIFE_LOST = 9;
    public static final int SPIDER_STEP = 10;
    public static final int EXPERT = 11;
    public static final int BETTER = 12;
    public static final int EXCELLENT= 13;

    private final float streamVolumeMax = 100f;
    private float volumeLevel = 50f;
    public boolean soundON = true;

    public SoundManager(Context theContext) {
        context = theContext;
        mSoundPool = new SoundPool(NUM_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        sm = new int[NUM_SOUNDS];

        sm[SOUND_WON] = mSoundPool.load(context, R.raw.you_won, 1);
        sm[GAME_OVER] = mSoundPool.load(context, R.raw.game_over, 1);
        sm[DEAD_BUG] = mSoundPool.load(context, R.raw.dead_bug, 1);
        sm[DEFEND_NEST] = mSoundPool.load(context, R.raw.defend_nest, 1);
        sm[EXTRA_SHOTS] = mSoundPool.load(context, R.raw.extra_shots, 1);
        sm[LEVEL_UP] = mSoundPool.load(context, R.raw.level_up, 1);
        sm[GREAT_JOB] = mSoundPool.load(context, R.raw.great_job, 1);
        sm[LIFE_LOST] = mSoundPool.load(context, R.raw.life_lost, 1);
        sm[SPIDER_STEP] = mSoundPool.load(context, R.raw.step, 1);
        sm[FIRE_SHOT] = mSoundPool.load(context, R.raw.fire_shot, 1);
        sm[EXPERT] = mSoundPool.load(context, R.raw.expert, 1);
        sm[BETTER] = mSoundPool.load(context, R.raw.getting_better, 1);
        sm[EXCELLENT] = mSoundPool.load(context, R.raw.excellent, 1);

        volumeLevel = 50f;
    }

    public void setVolumeLevel(float volume) {
        if (volume > 100 || volume < 0) {
            volume = 50;
            try {
                throw new Exception("volume value:" + volume + ". And Should be between 0-100. Set to 50 my default");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            volumeLevel = volume;
        }
    }

    public final void playSound(int sound) {

        if (soundON) {
            float volume = volumeLevel / streamVolumeMax;
            mSoundPool.play(sm[sound], volume, volume, 1, 0, 1f);
        }
    }

    public final void cleanUp() {
        sm = null;
        context = null;
        mSoundPool.release();
        mSoundPool = null;
    }

    public void turnOnSound() {
        soundON = true;

    }

    public void turnOffSound() {
        soundON = false;

    }



}
