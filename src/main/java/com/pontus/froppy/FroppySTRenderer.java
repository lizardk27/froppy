package com.pontus.froppy;
/**
 * Created by gabriel yibirin on 2/13/2016.
 */

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.view.MotionEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class FroppySTRenderer implements Renderer {

    // VARIABLE DEFINITION START
    //--------------------------------------------------------------------------------

    // Our screenresolution
    final static float optimalScreenHeight = 1920;
    // Our view, projection and p&v matrices
    private final float[] mtrxProjection = new float[16];
    private final float[] mtrxView = new float[16];
    private final float[] mtrxProjectionAndView = new float[16];
    // textures array - the size is defined by
    // GLES20.GL_TEXTURE31 -- predefined texture position
    // which is the highest number of texture predefined pos
    // CHANGED -- TESTING 99 as limit
    // int[] texturenames = new int[32];
    int[] texturenames = new int[30];
    long mLastTime;
    long msLastTimeBugsCreated = 0;
    int fly_speed = 10;
    int fly_creation_counter = 5000;

    //ARRAY LISTS TO HOLD DIFFRENT SPRITES
    private ArrayList<Sprite> permanent_images = new ArrayList<Sprite>();
    private ArrayList<Sprite> menu_images = new ArrayList<Sprite>();
    private float currScreenHeight = 1920;
    private float currScreenWidth = 1080;
    private float scale;
    private PointF center_stage;
    private long last_flies_created = 0;
    private long last_flies_updated = 0;
    private int flyCounter = 0;
    private int activeBug = 0;

    private ArrayList<Fly> flies = new ArrayList<Fly>();


    //Sprites

    private Sprite the_tongue_sprite;
    private float tongueX;
    private float tongueY;
    private float tonguePivotX;
    private float tonguePivotY;

    private float scoreYpos;
    private float scoreXpos;
    private float levelYpos;
    private float levelXpos;
    private float timerYpos;
    private float timerXpos;


    float angle = 0;
    float height = 0;


    // Menu Sprites
    private Sprite about_sprite;
    private Sprite menu_back_sprite;
    private Sprite soundVol_sprite;
    private Sprite musicVol_sprite;
    private Sprite play_sprite;
    private Sprite exit_sprite;
    private Sprite musicControl_sprite;
    private Sprite soundControl_sprite;
    private Sprite pause_button;
    private Sprite connect_button_sprite;


    private Sprite confirm_text_sprite;
    private Sprite confirm_yes_sprite;
    private Sprite confirm_no_sprite;


    private Sprite background_sprite;


    //BANNERS

    private TileObject level_up_sprite;
    private TileObject extra_shots_sprite;
    private TileObject life_lost_sprite;
    private TileObject start_sprite;
    private TileObject lost_sprite;
    private TileObject won_sprite;

    //Banners timers
    private int sign_lost_timer = -2;
    private int sign_won_timer = -2;
    private int sign_level_timer = -2;
    private int sign_shots_timer = -2;
    private int sign_start_timer = -2;
    private int sign_lifelost_timer = -2;
    // Misc
    private Context mContext;
    private float minXpos;
    private float maxXpos;
    private float middleXpos;

    //private float angle;
    private RectF base;
    private int talking_timer = 0;
    private float minXVCpos;
    private float maxXVCpos;
    private float middleXVCpos;

    private int score = 0;
    private int level = 0;
    private int level_change_score = 30;
    private int timer = 0;
    private long last_timer_change = 0;
    private int timer_change_period = 1500;

    private boolean isPaused = true;
    private boolean isSurfaceCreated = false;
    private boolean levelChangeLocked = true;
    private boolean isFirstRun = true;
    private boolean gameIsWon = false;
    private boolean gameIsLost = false;
    private boolean showPlayButton = true;
    private boolean returningFromPause = false;


    private TextManager tm;

    private SignsTileManager banners_manager;
    private FliesTileManager flies_manager;

    private TextObject timer_text;
    private TextObject score_text;
    private TextObject level_text;

    private SoundManager froppySoundManager;

    //--------------------------------------------------------------------------------
    // VARIABLE DEFINITION END

    // Constructor
    //    FroppySTRenderer(Context c)
    public FroppySTRenderer(Context c) {


        mContext = c;
        mLastTime = System.currentTimeMillis() + 100;

        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        currScreenHeight = metrics.heightPixels;
        currScreenWidth = metrics.widthPixels;

        System.out.println("SCREEN HEIGHT: " + currScreenHeight);
        System.out.println("SCREEN WIDTH: " + currScreenWidth);

        // SET BASE SQUARE FOR OUR IMAGE, INITIAL POSITION (translation)
        base = new RectF(
                -(((float) (currScreenHeight * 1.7777777777777777777777777) * 1.25f) / 2),                                    // left,
                ((((float) (currScreenHeight * 1.7777777777777777777777777) * 1.25f) / 2) / 1.48148148148148148148148148f),   // top,
                (((float) (currScreenHeight * 1.7777777777777777777777777) * 1.25f) / 2),                                   // right,
                -((((float) (currScreenHeight * 1.7777777777777777777777777) * 1.25f) / 2) / 1.48148148148148148148148148f));     // bottom

        center_stage = new PointF(currScreenWidth / 2, currScreenHeight / 2);

        System.out.println("SCREEN CENTER : " + center_stage);
        System.out.println("SCREEN BASE : " + base);

        scale = 1f;
        angle = 0f;

        // ADDING BACKGROUND
        background_sprite = new Sprite(c, scale, angle, center_stage, base);
        permanent_images.add(background_sprite);

        // calculate scale factor for all images
        // 1920 x 1080 is the "normal" resolution
        // so, since all images are set to that resolution
        // we change the scale in accordance
        // to the difference between the optimalScreenHeight and currScreenHeight

        scale = currScreenHeight / optimalScreenHeight;
        ////System.out.println("SCALE=" + scale);

        // ADDING controlCase
        // SIZE: 200x200      left,    top,     right,     bottom
        base = new RectF(-100f, 100f, 100f, -100f);

        // Position from center
        float xpos = center_stage.x - 220 * scale;
        float ypos = center_stage.y - 800 * scale;

        PointF translation = new PointF(xpos, ypos);

        // same size (base)
        // Position from center (same y)
        xpos = center_stage.x + 320 * scale;

        translation = new PointF(xpos, ypos);

        // SIZE: 50x100      left,    top,     right,     bottom
        base = new RectF(-25f, 50f, 25f, -50f);

        // Position from center

        xpos = -100 * scale;
        ypos = center_stage.y + 900 * scale;

        translation = new PointF(xpos, ypos);

        xpos = -450 * scale;
        ypos = center_stage.y + 9000 * scale;


        // SIZE: 120x100      left,    top,     right,     bottom
        base = new RectF(-60f, 50f, 60f, -50f);

        // Position from center
        xpos = center_stage.x - 385 * scale;
        ypos = center_stage.y + 755 * scale;

        froppySoundManager = new SoundManager(mContext);

        // SIZE: 220x90     left,    top,     right,     bottom
        base = new RectF(0f, -45f, 220f, 45f);

        // Position from center
        tongueX = center_stage.x - 32 * scale;
        tongueY = center_stage.y + 500 * scale;

        tonguePivotX = 870;
        tonguePivotY = 250;

        translation = new PointF(tongueX, tongueY);
        the_tongue_sprite = new Sprite(c, scale, angle, translation, base);


        // SIZE: 112x112      left,    top,     right,     bottom
        base = new RectF(-56f, 56f, 56f, -56f);
        // Position from center
        xpos = center_stage.x + 460 * scale;
        ypos = center_stage.y + 890 * scale;
        translation = new PointF(xpos, ypos);
        pause_button = new Sprite(c, scale, angle, translation, base);


        //PAUSE MENU AND START MENU OVER ALL RENDERED LAST !!!!

        // SIZE: 430x650      left,    top,     right,     bottom
        base = new RectF(-215f, 325f, 215f, -325f);
        // Position from center
        xpos = center_stage.x + 10 * scale;
        ypos = center_stage.y + 10 * scale;
        translation = new PointF(xpos, ypos);

        menu_back_sprite = new Sprite(c, scale, angle, translation, base);
        menu_back_sprite.setScale(1.5f * scale);
        menu_images.add(menu_back_sprite);


        // SIZE: 300x150      left,    top,     right,     bottom
        base = new RectF(-150f, 75f, 150f, -75f);
        // Position from center
        ypos = center_stage.y + 170 * scale;
        translation = new PointF(xpos, ypos);
        soundVol_sprite = new Sprite(c, scale, angle, translation, base);
        soundVol_sprite.setScale(scale * 1.2f);
        menu_images.add(soundVol_sprite);
        base = new RectF(-30f, 30f, 30f, -30f);
        ypos = center_stage.y + 140 * scale;
        translation = new PointF(xpos, ypos);
        soundControl_sprite = new Sprite(c, scale, angle, translation, base);
        soundControl_sprite.setScale(scale * 1.2f);
        menu_images.add(soundControl_sprite);


        // SIZE: 300x150      left,    top,     right,     bottom
        base = new RectF(-150f, 75f, 150f, -75f);
        // Position from center
        ypos = center_stage.y + 10 * scale;
        translation = new PointF(xpos, ypos);
        musicVol_sprite = new Sprite(c, scale, angle, translation, base);
        musicVol_sprite.setScale(scale * 1.2f);
        menu_images.add(musicVol_sprite);


        base = new RectF(-30f, 30f, 30f, -30f);
        ypos = center_stage.y - 30 * scale;
        translation = new PointF(xpos, ypos);
        musicControl_sprite = new Sprite(c, scale, angle, translation, base);
        musicControl_sprite.setScale(scale * 1.3f);
        menu_images.add(musicControl_sprite);


        // SIZE: 420x112       left,    top,     right,     bottom
        base = new RectF(-210f, 66f, 210f, -66f);
        // Position from center
        ypos = center_stage.y + 370 * scale;
        translation = new PointF(xpos, ypos);
        about_sprite = new Sprite(c, scale, angle, translation, base);
        about_sprite.setScale(scale * 1.2f);
        menu_images.add(about_sprite);


        // SIZE: 250x60      left,    top,     right,     bottom
        base = new RectF(-125f, 30f, 125f, -30f);
        // Position from center
        ypos = center_stage.y - 250 * scale;
        translation = new PointF(xpos, ypos);
        play_sprite = new Sprite(c, scale, angle, translation, base);
        play_sprite.setScale(scale * 1.5f);
        menu_images.add(play_sprite);

        // SIZE: 428x117      left,    top,     right,     bottom
        base = new RectF(-214, 58.5f, 214f, -58.5f);
        // Position from center
        ypos = center_stage.y - 250 * scale;
        translation = new PointF(xpos, ypos);
        connect_button_sprite = new Sprite(c, scale, angle, translation, base);
        connect_button_sprite.setScale(scale * 0f);
        menu_images.add(connect_button_sprite);


        // SIZE: 220x50      left,    top,     right,     bottom
        base = new RectF(-110f, 25f, 110f, -25f);
        // Position from center
        ypos = center_stage.y - 400 * scale;
        translation = new PointF(xpos, ypos);
        exit_sprite = new Sprite(c, scale, angle, translation, base);
        exit_sprite.setScale(scale * 1.5f);
        menu_images.add(exit_sprite);


        // SIZE: 364x34     left,    top,     right,     bottom
        base = new RectF(-182f, 17f, 182f, -17f);
        // Position from center
        ypos = center_stage.y - 250 * scale;
        translation = new PointF(xpos, ypos);
        confirm_text_sprite = new Sprite(c, scale, angle, translation, base);
        confirm_text_sprite.setScale(scale * 0f);
        menu_images.add(confirm_text_sprite);

        // SIZE: 103x48     left,    top,     right,     bottom
        base = new RectF(-51.5f, 24f, 51.5f, -24f);
        // Position from center
        ypos = center_stage.y - 400 * scale;
        xpos = center_stage.x - 100 * scale;
        translation = new PointF(xpos, ypos);
        confirm_yes_sprite = new Sprite(c, scale, angle, translation, base);
        confirm_yes_sprite.setScale(scale * 0f);
        menu_images.add(confirm_yes_sprite);


        // SIZE: 103x48     left,    top,     right,     bottom
        base = new RectF(-51.5f, 24f, 51.5f, -24f);
        // Position from center
        ypos = center_stage.y - 400 * scale;
        xpos = center_stage.x + 100 * scale;
        translation = new PointF(xpos, ypos);
        confirm_no_sprite = new Sprite(c, scale, angle, translation, base);
        confirm_no_sprite.setScale(scale * 0f);
        menu_images.add(confirm_no_sprite);


        // GAME ANNOUNCE SIGNS / BANNERS


        ypos = center_stage.y + 10 * scale;
        xpos = center_stage.x + 10 * scale;


        //LISTO
        start_sprite = new TileObject(0, xpos - 500 * scale, ypos - 500 * scale, 170, 170);
        extra_shots_sprite = new TileObject(1, xpos - 500 * scale, ypos - 500 * scale, 300, 300);
        won_sprite = new TileObject(2, xpos - 470 * scale, ypos - 500 * scale, 300, 300);
        lost_sprite = new TileObject(3, xpos - 500 * scale, ypos - 500 * scale, 300, 300);
        life_lost_sprite = new TileObject(4, xpos - 500 * scale, ypos - 500 * scale, 300, 300);
        level_up_sprite = new TileObject(5, xpos - 500 * scale, ypos - 500 * scale, 300, 300);


        System.out.println("Sign_ level_timer pos(x,y):(" + level_up_sprite.x + "," + level_up_sprite.y + ")");


    }

    public void onDestroy() {
        ((MainActivity) mContext).onDestroy();
        System.exit(0);
    }


    public void onPause() {
        if (gameIsWon | gameIsLost) {
            gameIsLost = false;
            gameIsWon = false;
        }
        returningFromPause = true;
        ((MainActivity) mContext).onPause();
        if (!showPlayButton) {
            play_sprite.setScale(scale * 0f);
            connect_button_sprite.setScale(scale * 1f);
        } else {
            play_sprite.setScale(scale * 1.5f);
            connect_button_sprite.setScale(scale * 0f);
        }
        play_sprite.UpdateSprite();
        connect_button_sprite.UpdateSprite();
    }

    public void showPlayButton(boolean state) {
        showPlayButton = state;
    }


    private void setLevel(int level_value) throws IOException {
        if (level_value < 0 || level_value > 11) {
            throw new IOException("level value should be between 0 and 11");
        }
        // Create our new textobject
        String tmp = String.valueOf(level);
        while (tmp.length() < 2) {
            tmp = "0" + tmp;
        }
        // Prepare the text for rendering
        tm.removeText(level_text);
        level_text = new TextObject(tmp, levelXpos, levelYpos);
        tm.addText(level_text);
        tm.PrepareDraw();
        if (level == 11) {
            playWonAnimation();
        }
    }

    private void setTimer(int timer_value) throws IOException {

        String tmp = String.valueOf(timer_value);
        while (tmp.length() < 2) {
            tmp = "0" + tmp;
        }
        // Prepare the text for rendering
        tm.removeText(timer_text);
        timer_text = new TextObject(tmp, timerXpos, timerYpos);
        tm.addText(timer_text);
        tm.PrepareDraw();

    }


    public void onResume() {
        //System.out.println("SPIDER ATTACK-RENDERER IS RESUMING...");
        //System.out.println("CACHE FILE EXISTS???: " + CacheFileHandler.fileExists(mContext, "spiderCacheFile.txt"));

        if (!isPaused) {
            ((MainActivity) (mContext)).hideBanner();
            ((MainActivity) (mContext)).closeContextMenu();
        }


        /*
        ((MainActivity) (mContext)).hideBanner();
        ((MainActivity) (mContext)).closeContextMenu();
        ((Ma binActivity) (mContext)).closeOptionsMenu();
        /*

        if (CacheFileHandler.fileExists(mContext, "spiderCacheFile.txt")) {
            String readText = CacheFileHandler.readAllCachedText(mContext, "spiderCacheFile.txt");
            System.out.println("READ FROM FILE: " + readText);
            String[] vals = readText.split("-");
            score = Integer.valueOf(vals[0]);
            level = Integer.valueOf(vals[1]);
            number_flies_reached_spider = Integer.valueOf(vals[2]);
            number_lives_left = Integer.valueOf(vals[3]);
        }
*/
    }


    @Override
    public synchronized void onDrawFrame(GL10 unused) {


        isSurfaceCreated = true;

        // Get the current time
        long now = System.currentTimeMillis();

        // We should make sure we are valid and sane
        if (mLastTime > now) return;


        float ypos = center_stage.y + 1 * scale;
        float xpos = center_stage.x + 1 * scale;


        // BLOCK  RUNS IF NOT PAUSED
        if (!isPaused) {
            try {

                setScore(score);

                if (now - last_timer_change > timer_change_period) {

                    timer--;

                    if (timer <= 0) {
                        timer = 99;
                    }

                    setTimer(timer);

                    last_timer_change = now;
                }

                if (talking_timer > 0) {
                    talking_timer--;
                }

                if (talking_timer == 0 && score > 0 && score % 9 == 0) {

                    int pos = (int) Math.floor(Math.random() * 4);
                    switch (pos) {
                        case 0:
                            froppySoundManager.playSound(SoundManager.GREAT_JOB);
                            talking_timer = 2000;
                            break;
                        case 1:
                            froppySoundManager.playSound(SoundManager.EXCELLENT);
                            talking_timer = 2000;
                            break;
                        case 2:
                            froppySoundManager.playSound(SoundManager.EXPERT);
                            talking_timer = 2000;
                            break;
                        case 3:
                            froppySoundManager.playSound(SoundManager.BETTER);
                            talking_timer = 2000;
                            break;
                    }
                }


                if (returningFromPause || isFirstRun || ((score > 0) && (score % level_change_score == 0) && !levelChangeLocked)) {


                    levelChangeLocked = true;

                    if (!returningFromPause) {

                        if (isFirstRun) {
                            level++;
                            sign_start_timer = 500;
                        } else if (level < 11) {
                            froppySoundManager.playSound(SoundManager.LEVEL_UP);
                            sign_level_timer = 500;
                            level++;
                        }
                    }
                    isFirstRun = false;
                    returningFromPause = false;


                    setLevel(level);

                    switch (level) {
                        case 1:
                            fly_creation_counter = 3000;
                            fly_speed = 10;
                            break;
                        case 2:
                            fly_creation_counter = 2600;
                            fly_speed = 12;
                            break;
                        case 3:
                            fly_creation_counter = 2200;
                            fly_speed = 14;
                            break;
                        case 4:
                            fly_creation_counter = 1800;
                            fly_speed = 16;
                            break;
                        case 5:
                            fly_creation_counter = 1600;
                            fly_speed = 18;
                            break;
                        case 6:
                            fly_creation_counter = 1200;
                            fly_speed = 20;
                            break;
                        case 7:
                            fly_creation_counter = 1000;
                            fly_speed = 22;
                            break;
                        case 8:
                            fly_creation_counter = 800;
                            fly_speed = 24;
                            break;
                        case 9:
                            fly_creation_counter = 800;
                            fly_speed = 26;
                            break;
                        case 10:
                            fly_creation_counter = 600;
                            fly_speed = 28;
                            break;


                    }//end switch

                }
                // END OF TRY IN NOT PAUSED LOOP
            } catch (IOException e) {
                e.printStackTrace();
            }

            // STILL IN NOT PAUSED

            pause_button.setScale(scale * 1f);
            pause_button.UpdateSprite();
            pause_button.setNotTouched();


            //  UPDATE FLIES if game not won or lost a
            //  AND
            //  TIME CONDITION: (last_flies_updated == 0 || (now - last_flies_updated)

            if ((last_flies_updated == 0 || (now - last_flies_updated) > 100)
                    && (!gameIsLost && !gameIsWon)) {
                try {
                    for (int fly_count = 0; fly_count < flies.size(); fly_count++) {
                        Fly tmp = flies.get(fly_count);
                        int heading = (int) Math.floor(Math.random() * 15) + 1;
                        if (heading < 6) {
                            tmp.setHeading(heading);
                        }
                        tmp.move();
                        if (tmp.x < 0) {
                            tmp.x = currScreenWidth;
                            if (tmp.getHeading()==4) {
                                tmp.setHeading(3);
                            }else if (tmp.getHeading()==6) {
                                tmp.setHeading(1);
                            }else{
                                tmp.setHeading(2);
                            }
                        }
                        if (tmp.x > currScreenWidth) {
                            tmp.x = 0;
                            if (tmp.getHeading()==1) {
                                tmp.setHeading(6);
                            }else if (tmp.getHeading()==3) {
                                tmp.setHeading(4);
                            }else{
                                tmp.setHeading(5);
                            }
                        }
                        if (tmp.y < 0) {
                            tmp.y = currScreenHeight;
                            if (tmp.getHeading()==3) {
                                tmp.setHeading(1);
                            }else{
                                tmp.setHeading(6);
                            }
                        }
                        if (tmp.y > currScreenHeight) {
                            tmp.y = 0;
                            if (tmp.getHeading()==1) {
                                tmp.setHeading(3);
                            }else{
                                tmp.setHeading(4);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                 }


                last_flies_updated = now;

            }// END LAST FLIES UPDATED


            //  CREATE Flies if game not won or lost a
            //  AND
            //  TIME CONDITION: (now - last_flies_created) > fly_creation_counter

            if ((last_flies_created == 0 || (now - last_flies_created) > fly_creation_counter)
                    && (!gameIsLost && !gameIsWon)) {

                Fly aFly = new Fly(3, xpos, ypos, 85, 85);
                try {
                    aFly.setHeading(1);
                    aFly.setStep(fly_speed);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                flies.add(aFly);

                last_flies_created = now;
            }// END CRATE FLIES

            banners_manager.removeAll();
            flies_manager.removeAll();

            for (int fly_count = 0; fly_count < flies.size(); fly_count++) {
                flies_manager.addTileObject(flies.get(fly_count));
            }


            flies_manager.PrepareDraw();

            // IF THERE IS A SIGN TO SHOW
            if (sign_level_timer > -2 | sign_start_timer > -2 | sign_lifelost_timer > -2 |
                    sign_shots_timer > -2 | sign_won_timer > -2 | sign_lost_timer > -2) {

                banners_manager.removeAll();

                //System.out.println(" -IN LOOP - --- - - - - -  --  - - - - - - ");

                if (sign_level_timer >= 0) {
                    //System.out.println("Sign_ level_up_sprite pos(x,y):(" + level_up_sprite.x + "," + level_up_sprite.y + ")");
                    level_up_sprite.move(0, 4 * scale);
                    banners_manager.addTileObject(level_up_sprite);
                    sign_level_timer--;
                }
                if (sign_level_timer == -1) {
                    sign_level_timer = -2;
                    level_up_sprite.moveTo(xpos - 500 * scale, ypos - 500 * scale);
                    banners_manager.removeTileObject(level_up_sprite);

                }
                if (sign_shots_timer >= 0) {
                    extra_shots_sprite.move(0, 4 * scale);
                    banners_manager.addTileObject(extra_shots_sprite);
                    sign_shots_timer--;
                }
                if (sign_shots_timer == -1) {
                    extra_shots_sprite.moveTo(xpos - 500 * scale, ypos - 500 * scale);
                    banners_manager.removeTileObject(extra_shots_sprite);
                    sign_shots_timer = -2;

                }

                if (sign_lifelost_timer >= 0) {
                    life_lost_sprite.move(0, -4 * scale);
                    banners_manager.addTileObject(life_lost_sprite);
                    sign_lifelost_timer--;
                }
                if (sign_lifelost_timer == -1) {
                    life_lost_sprite.moveTo(xpos - 500 * scale, ypos - 500 * scale);
                    banners_manager.removeTileObject(life_lost_sprite);
                    sign_lifelost_timer = -2;
                }
                if (sign_start_timer >= 0) {
                    start_sprite.move(0, -8 * scale);
                    banners_manager.addTileObject(start_sprite);
                    sign_start_timer--;
                }
                if (sign_start_timer == -1) {
                    start_sprite.moveTo(xpos - 500 * scale, ypos - 500 * scale);
                    banners_manager.removeTileObject(start_sprite);
                    sign_start_timer = -2;
                }
                if (sign_won_timer >= 0) {
                    won_sprite.move(0, 3f * scale);
                    banners_manager.addTileObject(won_sprite);
                    sign_won_timer--;
                }
                if (sign_won_timer == -1) {
                    sign_won_timer = -2;
                    won_sprite.moveTo(xpos - 500 * scale, ypos - 500 * scale);
                    banners_manager.removeTileObject(won_sprite);

                    score = 0;
                    level = 1;
                    try {
                        setLevel(level);
                        setScore(score);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    returnToPauseMenu();
                    isPaused = true;
                    //System.out.println("Ended showing WON sign. isPaused = true");
                }

                if (sign_lost_timer >= 0) {
                    lost_sprite.move(0, -2f * scale);
                    banners_manager.addTileObject(lost_sprite);
                    sign_lost_timer--;
                }
                if (sign_lost_timer == -1) {
                    sign_lost_timer = -2;
                    lost_sprite.moveTo(xpos - 500 * scale, ypos - 500 * scale);
                    banners_manager.removeTileObject(lost_sprite);
                    level = 1;
                    score = 0;
                    try {
                        setLevel(level);
                        setScore(score);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    returnToPauseMenu();
                    isPaused = true;
                    //System.out.println("Ended showing LOST sign. isPaused = true");

                }
                banners_manager.PrepareDraw();

            }// END OF SIGNS SHOW BLOCK
        }// END OF BLOCK THAT RUNS WHEN NOT PAUSED


        if (permanent_images != null && permanent_images.size() > 0) {
            // Update our permanent sprites

            for (Sprite tmp : permanent_images) {
                tmp.UpdateSprite();
            }
        }
        if (menu_images != null && menu_images.size() > 0) {
            // Update our permanent sprites
            for (Sprite tmp : menu_images) {
                tmp.UpdateSprite();
            }
        }

        the_tongue_sprite.UpdateSprite();

        // Render all
        Render(mtrxProjectionAndView);
        // Save the current time to see how long it took :).


        // Render the text
        if (tm != null && !isPaused) {
            tm.Draw(mtrxProjectionAndView);
        }

        // Render the signs when needed from image tile
        if (banners_manager != null && !isPaused) {
            banners_manager.Draw(mtrxProjectionAndView);
        }

        // Render the signs when needed from image tile
        if (flies_manager != null && !isPaused) {
            flies_manager.Draw(mtrxProjectionAndView);
        }


        mLastTime = now;
    }

    private synchronized void Render(float[] matrix) {


        // Set our shaderprogram to image shader
        GLES20.glUseProgram(riGraphicTools.sp_Image);


        // clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        //GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // No culling of back faces
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // No depth testing
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_ALWAYS);

        // Enable blending
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "vPosition");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        int mTexCoordLoc = 0;

        if (permanent_images != null && permanent_images.size() > 0) {
            renderSpriteArray(permanent_images, mPositionHandle, mTexCoordLoc, matrix);
        }


        the_tongue_sprite.UpdateSprite();
        renderSprite(the_tongue_sprite, mPositionHandle, mTexCoordLoc, matrix);

        if (isPaused) {
            if (menu_images != null && menu_images.size() > 0) {
                renderSpriteArray(menu_images, mPositionHandle, mTexCoordLoc, matrix);
            }
        }
        renderSprite(pause_button, mPositionHandle, mTexCoordLoc, matrix);


        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);

    }

    private synchronized void renderSpriteArray(ArrayList<Sprite> theSpriteArray, int mPositionHandle, int mTexCoordLoc, float[] matrix) {

        for (Iterator<Sprite> sprite_iterator = theSpriteArray.iterator(); sprite_iterator.hasNext(); ) {

            Sprite tmp = sprite_iterator.next();

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, tmp.getVertexBuffer());

            // Get handle to texture coordinates location
            mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord");

            // Enable generic vertex attribute array
            GLES20.glEnableVertexAttribArray(mTexCoordLoc);

            // Prepare the texturecoordinates
            GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, tmp.getUvBuffer());

            // Get handle to shape's transformation matrix
            int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");

            // Apply the projection and view transformation
            GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, matrix, 0);

            // Get handle to textures locations
            int mSamplerLoc = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "s_texture");

            // Set the sampler texture unit to sprite.texture_position, where we have saved the texture.
            GLES20.glUniform1i(mSamplerLoc, tmp.getTexturePos());

            // Draw the triangles
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, tmp.getIndices().length, GLES20.GL_UNSIGNED_SHORT, tmp.getDrawListBuffer());

        }
    }

    private synchronized void renderSprite(Sprite theSprite, int mPositionHandle, int mTexCoordLoc, float[] matrix) {


        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, theSprite.getVertexBuffer());

        // Get handle to texture coordinates location
        mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Image, "a_texCoord");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, theSprite.getUvBuffer());

        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, matrix, 0);

        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation(riGraphicTools.sp_Image, "s_texture");

        // Set the sampler texture unit to sprite.texture_position, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, theSprite.getTexturePos());

        // Draw the triangles
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, theSprite.getIndices().length, GLES20.GL_UNSIGNED_SHORT, theSprite.getDrawListBuffer());

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {


        // We need to know the current width and height.
        currScreenWidth = width;
        currScreenHeight = height;

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, (int) currScreenWidth, (int) currScreenHeight);

        // Clear our matrices
        for (int i = 0; i < 16; i++) {
            mtrxProjection[i] = 0.0f;
            mtrxView[i] = 0.0f;
            mtrxProjectionAndView[i] = 0.0f;
        }

        // Setup our screen width and height for normal sprite translation.
        Matrix.orthoM(mtrxProjection, 0, 0f, currScreenWidth, 0.0f, currScreenHeight, 0, 50);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0);


    }

    private void goToPontus() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://pontusdd.com"));
        ((MainActivity) (mContext)).startActivity(browserIntent);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {


        // Load image as textures
        // texture position marker
        background_sprite.SetupImage("drawable/background", 0, texturenames);
        the_tongue_sprite.SetupImage("drawable/tongue", 3, texturenames);

        menu_back_sprite.SetupImage("drawable/message_board", 14, texturenames);
        musicVol_sprite.SetupImage("drawable/music_vol_control", 15, texturenames);
        musicControl_sprite.SetupImage("drawable/volume_control_stick", 16, texturenames);
        soundVol_sprite.SetupImage("drawable/sound_vol_control", 17, texturenames);
        soundControl_sprite.SetupImage("drawable/volume_control_stick", 18, texturenames);
        play_sprite.SetupImage("drawable/play_button", 19, texturenames);
        exit_sprite.SetupImage("drawable/exit_button", 20, texturenames);
        pause_button.SetupImage("drawable/pause_button", 21, texturenames);
        about_sprite.SetupImage("drawable/about_pontus", 22, texturenames);
        confirm_yes_sprite.SetupImage("drawable/confirm_yes", 23, texturenames);
        confirm_no_sprite.SetupImage("drawable/confirm_no", 24, texturenames);
        confirm_text_sprite.SetupImage("drawable/confirm_text", 25, texturenames);
        connect_button_sprite.SetupImage("drawable/connect_button", 29, texturenames);

        SetupTextAndImageTiles();


        isSurfaceCreated = true;

        // the middle of the control stick (position 0)
        middleXpos = currScreenWidth / 2;
        minXpos = middleXpos - 150 * scale;
        maxXpos = middleXpos + 150 * scale;


        // the middle of the control stick of the volume control menus (position 0)
        middleXVCpos = musicVol_sprite.getX();
        minXVCpos = middleXVCpos - 150 * scale;
        maxXVCpos = middleXVCpos + 150 * scale;


        // Set the clear color to black
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1);


        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);


        // Create the shaders, solid color
        int vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_SolidColor);
        int fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_SolidColor);
        // create empty OpenGL ES Program
        riGraphicTools.sp_SolidColor = GLES20.glCreateProgram();
        // add the vertex shader to program
        GLES20.glAttachShader(riGraphicTools.sp_SolidColor, vertexShader);
        // add the fragment shader to program
        GLES20.glAttachShader(riGraphicTools.sp_SolidColor, fragmentShader);
        // creates OpenGL ES program executables
        GLES20.glLinkProgram(riGraphicTools.sp_SolidColor);


        // Create the shaders, images
        vertexShader = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_Image);
        fragmentShader = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Image);
        // create empty OpenGL ES Program
        riGraphicTools.sp_Image = GLES20.glCreateProgram();
        // add the vertex shader to program
        GLES20.glAttachShader(riGraphicTools.sp_Image, vertexShader);
        // add the fragment shader to program
        GLES20.glAttachShader(riGraphicTools.sp_Image, fragmentShader);
        // creates OpenGL ES program executables
        GLES20.glLinkProgram(riGraphicTools.sp_Image);

        // Text shader
        int vshadert = riGraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, riGraphicTools.vs_Text);
        int fshadert = riGraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, riGraphicTools.fs_Text);

        riGraphicTools.sp_Text = GLES20.glCreateProgram();
        GLES20.glAttachShader(riGraphicTools.sp_Text, vshadert);
        GLES20.glAttachShader(riGraphicTools.sp_Text, fshadert);        // add the fragment shader to program
        GLES20.glLinkProgram(riGraphicTools.sp_Text);                  // creates OpenGL ES program executables


        // Set our shader programm
        GLES20.glUseProgram(riGraphicTools.sp_Image);


        try {
            setLevel(level);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public boolean isSurfaceCreated() {
        return isSurfaceCreated;
    }

    public void processTouchEvent(MotionEvent event) {

        if (isPaused) {
            int pointerCount = event.getPointerCount();

            for (int i = 0; i < pointerCount; i++) {
                final float x = event.getX(i);
                final float y = event.getY(i);
                int id = event.getPointerId(i);
                int action = event.getActionMasked();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        exit_sprite.handleActionDown(x, (currScreenHeight - y));
                        if (showPlayButton) {
                            play_sprite.handleActionDown(x, (currScreenHeight - y));
                        } else {
                            connect_button_sprite.handleActionDown(x, (currScreenHeight - y));
                        }
                        musicControl_sprite.handleActionDown(x, (currScreenHeight - y));
                        soundControl_sprite.handleActionDown(x, (currScreenHeight - y));
                        confirm_no_sprite.handleActionDown(x, (currScreenHeight - y));
                        confirm_yes_sprite.handleActionDown(x, (currScreenHeight - y));
                        about_sprite.handleActionDown(x, (currScreenHeight - y));

                        if (about_sprite.isTouched()) {
                            goToPontus();
                        }

                        if (play_sprite.isTouched()) {

                            froppySoundManager.playSound(SoundManager.DEFEND_NEST);
                            isPaused = false;
                            sign_start_timer = 500;
                            play_sprite.setNotTouched();
                            ((MainActivity) mContext).hideBanner();
                        }
                        if (connect_button_sprite.isTouched()) {
                            connect_button_sprite.setNotTouched();
                            ((MainActivity) mContext).checkInternetConnection();
                        }
                        if (exit_sprite.isTouched()) {
                            exit_sprite.setNotTouched();
                            confirmExit();
                        }
                        if (musicControl_sprite.isTouched()) {
                            musicControl_sprite.setScale(scale * 1.3F);
                            musicControl_sprite.UpdateSprite();
                        }
                        if (soundControl_sprite.isTouched()) {
                            soundControl_sprite.setScale(scale * 1.3F);
                            soundControl_sprite.UpdateSprite();
                        }
                        if (confirm_no_sprite.isTouched()) {
                            returnToPauseMenu();
                        }
                        if (confirm_yes_sprite.isTouched()) {
                            onDestroy();
                        }
                        break;


                    case MotionEvent.ACTION_POINTER_DOWN:

                        exit_sprite.handleActionDown(x, (currScreenHeight - y));
                        if (showPlayButton) {
                            play_sprite.handleActionDown(x, (currScreenHeight - y));
                        } else {
                            connect_button_sprite.handleActionDown(x, (currScreenHeight - y));
                        }
                        musicControl_sprite.handleActionDown(x, (currScreenHeight - y));
                        soundControl_sprite.handleActionDown(x, (currScreenHeight - y));
                        confirm_no_sprite.handleActionDown(x, (currScreenHeight - y));
                        confirm_yes_sprite.handleActionDown(x, (currScreenHeight - y));
                        about_sprite.handleActionDown(x, (currScreenHeight - y));

                        if (about_sprite.isTouched()) {
                            goToPontus();
                        }

                        if (play_sprite.isTouched()) {
                            isPaused = false;
                            sign_start_timer = 500;
                            play_sprite.setNotTouched();
                            froppySoundManager.playSound(SoundManager.DEFEND_NEST);
                            ((MainActivity) mContext).hideBanner();
                        }
                        if (connect_button_sprite.isTouched()) {
                            connect_button_sprite.setNotTouched();
                            ((MainActivity) mContext).checkInternetConnection();
                        }
                        if (exit_sprite.isTouched()) {
                            exit_sprite.setNotTouched();
                            confirmExit();
                        }
                        if (musicControl_sprite.isTouched()) {
                            musicControl_sprite.setScale(scale * 1.3F);
                            musicControl_sprite.UpdateSprite();
                        }
                        if (soundControl_sprite.isTouched()) {
                            soundControl_sprite.setScale(scale * 1.3F);
                            soundControl_sprite.UpdateSprite();
                        }
                        if (confirm_no_sprite.isTouched()) {
                            returnToPauseMenu();
                        }
                        if (confirm_yes_sprite.isTouched()) {
                            onDestroy();
                        }
                        break;


                    case MotionEvent.ACTION_MOVE:

                        if (musicControl_sprite.isTouched()) {
                            // the stick is being dragged
                            float yPos = musicControl_sprite.getY();
                            float xPos = event.getX();
                            if (xPos >= minXVCpos && xPos <= maxXVCpos) {
                                musicControl_sprite.moveTo(event.getX(), yPos);
                                musicControl_sprite.UpdateSprite();
                                //is control is near center (40 - center to center + 40)
                                if (xPos < (middleXVCpos + 10 * scale) && xPos > (middleXVCpos - 10 * scale)) {
                                    ((MainActivity) mContext).setMusicVolume(0.5f);
                                } else {
                                    if (xPos > middleXVCpos) {
                                        ((MainActivity) mContext).setMusicVolume(0.75f);
                                    } else {
                                        ((MainActivity) mContext).setMusicVolume(0.25f);
                                    }
                                }
                            } else if (xPos > maxXVCpos) {
                                ((MainActivity) mContext).setMusicVolume(1f);
                                musicControl_sprite.moveTo(maxXVCpos, yPos);
                                musicControl_sprite.UpdateSprite();
                            } else if (xPos < minXVCpos) {
                                ((MainActivity) mContext).setMusicVolume(0f);
                                musicControl_sprite.moveTo(minXVCpos, yPos);
                                musicControl_sprite.UpdateSprite();
                            }
                        }

                        if (soundControl_sprite.isTouched()) {
                            // the stick is being dragged
                            float yPos = soundControl_sprite.getY();
                            float xPos = event.getX();
                            if (xPos >= minXVCpos && xPos <= maxXVCpos) {
                                soundControl_sprite.moveTo(event.getX(), yPos);
                                soundControl_sprite.UpdateSprite();
                                //is control is near center (40 - center to center + 40)
                                if (xPos < (middleXVCpos + 10 * scale) && xPos > (middleXVCpos - 10 * scale)) {
                                    froppySoundManager.setVolumeLevel(0);
                                } else {
                                    if (xPos > middleXVCpos) {
                                        froppySoundManager.setVolumeLevel(0);
                                    } else {
                                        froppySoundManager.setVolumeLevel(0);
                                    }
                                }
                            } else if (xPos > maxXVCpos) {
                                soundControl_sprite.moveTo(maxXVCpos, yPos);
                                froppySoundManager.setVolumeLevel(000);
                                soundControl_sprite.UpdateSprite();
                            } else if (xPos < minXVCpos) {
                                soundControl_sprite.moveTo(minXVCpos, yPos);
                                froppySoundManager.setVolumeLevel(00);
                                soundControl_sprite.UpdateSprite();
                            }
                        }
                        break;
                    default:
                        //System.out.println("SWITCH(" + action + ") has no case. On default (do nothing).");
                        break;

                }//CLOSE SWITCH

                ////System.out.println("ACTION: " + actionString);
            }//CLOSE FOR LOOP
        } // END IF ON PAUSE

        // RUN WHEN NOT PAUSED
        else {


            float orientation = event.getOrientation();
            int pointerCount = event.getPointerCount();

            for (int i = 0; i < pointerCount; i++) {
                final float x = event.getX(i);
                final float y = event.getY(i);
                int id = event.getPointerId(i);
                int action = event.getActionMasked();
                int actionIndex = event.getActionIndex();

                switch (action) {

                    case MotionEvent.ACTION_DOWN:


                        PointF origin = new PointF(tonguePivotX, tonguePivotY);
                        PointF target = new PointF(x, y);

                        System.out.println("base. Target: " + target);
                        System.out.println("base. Origin: " + origin);

                        double height = Math.sqrt((Math.pow((x - tonguePivotX), 2) + (Math.pow((y - (tonguePivotY)), 2))));
                        height += (height * 3 / 4);
                        double angle = Math.atan2(y - tonguePivotY, x - tonguePivotX);
                        angle = -angle;

                        the_tongue_sprite.growFromLeft((float) height);
                        the_tongue_sprite.setAngle((float) angle);

                        pause_button.handleActionDown(x, (currScreenHeight - y));

                        if (pause_button.isTouched() && !isPaused) {
                            pause_button.setScale(scale * .6f);
                            pause_button.UpdateSprite();
                            //onPause();
                            returnToPauseMenu();
                            isPaused = true;
                            ((MainActivity) mContext).onPause();
                        }


                        break;

                    case MotionEvent.ACTION_UP:
                        // TO-DO
                        the_tongue_sprite.growFromLeft((float) 30);
                        the_tongue_sprite.setAngle((float) Math.PI / 2);
                        break;


                }//CLOSE SWITCH
            }//CLOSE FOR LOOP
        }// CLOSE ELSE: RUN WHEN NOT PAUSED
    }//CLOSE METHOD

    private void returnToPauseMenu() {
        play_sprite.setScale(scale * 1.5f);
        exit_sprite.setScale(scale * 1.5f);
        confirm_no_sprite.setScale(0);
        confirm_text_sprite.setScale(0);
        confirm_yes_sprite.setScale(0);
        returningFromPause = true;
    }

    private void confirmExit() {
        play_sprite.setScale(0);
        exit_sprite.setScale(0);
        confirm_no_sprite.setScale(scale * 1);
        confirm_text_sprite.setScale(scale * 1.3f);
        confirm_yes_sprite.setScale(scale * 1);
    }


    private synchronized void setScore(int score_value) throws IOException {


        // Create our new textobject
        String tmp = String.valueOf(score);
        while (tmp.length() < 3) {
            tmp = "0" + tmp;
        }
        tm.removeText(score_text);
        score_text = new TextObject(tmp, scoreXpos, scoreYpos);
        tm.addText(score_text);
        // Prepare the text for rendering
        tm.PrepareDraw();
    }


    private void playWonAnimation() {

        froppySoundManager.playSound(SoundManager.SOUND_WON);
        gameIsWon = true;
        sign_won_timer = 500;

    }

    private void playDeathAnimation() {


        gameIsLost = true;
        froppySoundManager.playSound(SoundManager.GAME_OVER);
        sign_lost_timer = 500;

    }


    public void SetupTextAndImageTiles() {


        //  for the text texture
        int id = mContext.getResources().getIdentifier("drawable/font", null, mContext.getPackageName());
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 26);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[26]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);
        bmp.recycle();


        // Create our text manager
        tm = new TextManager();

        // Tell our text manager to use index 35 of textures loaded
        tm.setTextureID(26);

        // Pass the uniform scale
        tm.setUniformscale(scale * 4);

        // Position from center
        scoreYpos = center_stage.y - 650 * scale;
        levelYpos = scoreYpos;

        scoreXpos = center_stage.x - 690 * scale;


        // Create our new textobject

        score_text = new TextObject("000", scoreXpos, scoreYpos);

        levelXpos = center_stage.x + 470 * scale;

        level_text = new TextObject("000", levelXpos, levelYpos);

        timerXpos = center_stage.x + 1145 * scale;
        timerYpos = center_stage.y + 555 * scale;

        timer_text = new TextObject("00", timerXpos, timerYpos);

        // Add it to our manager
        tm.addText(timer_text);
        tm.addText(score_text);
        tm.addText(level_text);

        // Prepare the text for rendering
        tm.PrepareDraw();


        //-----------------------------------------------------------------


        //  for the images tile texture manager
        int id2 = mContext.getResources().getIdentifier("drawable/image_tile", null, mContext.getPackageName());
        Bitmap bmp2 = BitmapFactory.decodeResource(mContext.getResources(), id2);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 27);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[27]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp2, 0);
        bmp2.recycle();


        //-----------------------------------------------------------------

        //  for the BANNERS-SIGNS images tile texture manager
        int fliesTile = mContext.getResources().getIdentifier("drawable/tile6x6", null, mContext.getPackageName());
        Bitmap bmp1 = BitmapFactory.decodeResource(mContext.getResources(), fliesTile);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 29);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[29]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp1, 0);
        bmp1.recycle();

        // Create our image tile managers
        flies_manager = new FliesTileManager();

        // Tell our manager to use index of textures loaded
        flies_manager.setTextureID(29);

        // Pass the uniform scale
        flies_manager.setUniformscale(scale / 2);


        //-----------------------------------------------------------------


        //  for the BANNERS-SIGNS images tile texture manager
        int id3 = mContext.getResources().getIdentifier("drawable/signs_tile_images", null, mContext.getPackageName());
        Bitmap bmp3 = BitmapFactory.decodeResource(mContext.getResources(), id3);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + 28);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[28]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp3, 0);
        bmp3.recycle();


        // Create our image tile managers
        banners_manager = new SignsTileManager();

        // Tell our manager to use index of textures loaded
        banners_manager.setTextureID(28);

        // Pass the uniform scale
        banners_manager.setUniformscale(4 * scale);
    }

    public void showConnectioMessagge() {


        if (!showPlayButton) {
            play_sprite.setScale(0f);
            connect_button_sprite.setScale(scale * 1f);
        } else {
            play_sprite.setScale(scale * 1.5f);
            connect_button_sprite.setScale(0f);
        }
        play_sprite.UpdateSprite();
        connect_button_sprite.UpdateSprite();
    }
}