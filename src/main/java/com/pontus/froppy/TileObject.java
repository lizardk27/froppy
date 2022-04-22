package com.pontus.froppy;
/**
 * Created by gabriel yibirin on 2/13/2016.
 */

public class TileObject {

    private int index;
    public float x;
    public float y;
    public float[] color;
    private float width;
    private float height;
    private boolean isTouched = false;
    private float angle;


    public TileObject(int index, float x, float y, float width, float height) {
        if (index < 0) {
            index = 0;
        }

        this.index = index;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = new float[]{1f, 1f, 1f, 1.0f};
        isTouched = false;

    }

    public int getIndex(){
        return index;
    }
    public void setIndex(int index){
        this.index = index;
    }


    public void handleActionDown(float eventX, float eventY) {


        float x1 = x ;
        float x2 = x + width;
        float y1 = y ;
        float y2 = y + height;


        if (eventX >= x1 && eventX <= x2) {
            if (eventY >= y1 && eventY <= y2) {
                setTouched(true);
            } else {
                setTouched(false);
            }
        } else {
            setTouched(false);
        }
    }

    public void setNotTouched() {
        isTouched = false;
    }

    private void setTouched(boolean touch) {
        isTouched = touch;
    }

    public boolean isTouched() {
        return isTouched;
    }


    public void moveTo(float x, float y) {
        // Update our location.
        this.x = x;
        this.y = y;
    }

    public void move(float deltax, float deltay) {
        this.x += deltax;
        this.y += deltay;
    }

    public float getWidth(){
        return width;
    }
    public float getHeight(){
        return height;
    }
    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }
}
