package com.pontus.froppy;

/**
 * Created by gabriel yibirin on 2/13/2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


public class Sprite {


    private float angle;
    private float scale;
    private RectF base;
    private PointF translation;
    private boolean isTouched = false;
    private int texture_position;
    // Misc
    private Context mContext;
    // Geometric variables
    private static float vertices[];
    private static short indices[];
    private static float uvs[];
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer uvBuffer;


    public Sprite(Context context, float scale, float angle, PointF initial_pos, RectF base) {

        mContext = context;
        this.base = base;
        this.translation = initial_pos;
        this.scale = scale;
        this.angle = angle;
    }


    public float getWidth() {
        return base.width();
    }

    public float getHeight() {
        return base.height();
    }

    public void setWidth(float width) {
        float curr_width = base.width();
        float txl = base.left;
        float txr = base.right;
        float txc = (txr - txl) / 2;

        base.left = txc - (width / 2);
        base.right = txc + (width / 2);
    }

    public void setHeight(float height){
        float curr_height= base.height();
        float txt = base.top;
        float txb = base.bottom;
        float txc = (txt - txb) / 2;

        base.top = txc - (height / 2);
        base.bottom = txc + (height / 2);
    }

    public void growFromBase(float height){
        System.out.println("base: " + base);
        System.out.println("height: " + height );
        base.top =  height;
        System.out.println("base: " + base);
    }
    public void growFromLeft(float height){
        System.out.println("base: " + base);
        System.out.println("height: " + height );
        base.right =  height;
        System.out.println("base: " + base);
    }

    public Sprite(Context context, PointF initial_pos, RectF base_rect ){

        mContext = context;
        this.base = base_rect;
        this.translation = initial_pos;
        this.scale = 1f;
        this.angle = 0f;
    }

    public void moveTo(float xpos, float ypos) {
        // Update our location.
        translation.x = xpos;
        translation.y = ypos;
    }
    public void move(float deltax, float deltay){
        translation.x += deltax;
        translation.y += deltay;
    }

    public int getTexturePos(){
        return texture_position;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setAngle(float angle){
        this.angle = angle;
    }

    public float getAngle(){
        return angle;
    }

    public float getScale(){
        return scale;
    }

    public float getX(){
        return translation.x;
    }

    public float getY(){
        return translation.y;
    }

    public void setX(float xpos){
        translation.x = xpos;
    }

    public void setY(float ypos){
        translation.y = ypos;
    }

    public RectF getBase() {
        return base;
    }

    public ShortBuffer getDrawListBuffer() {
        return drawListBuffer;
    }

    public static short[] getIndices() {
        return indices;
    }

    public int getTexture_position() {
        return texture_position;
    }

    public FloatBuffer getUvBuffer() {
        return uvBuffer;
    }

    public static float[] getUvs() {
        return uvs;
    }

    public FloatBuffer getVertexBuffer() {
        return vertexBuffer;
    }

    public static float[] getVertices() {
        return vertices;
    }

    public float[] getTransformedVertices() {
        // Start with scaling
        float x1 = base.left * scale;
        float x2 = base.right * scale;
        float y1 = base.bottom * scale;
        float y2 = base.top * scale;

        // We now detach from our Rect because when rotating,
        // we need the seperate points, so we do so in opengl order
        PointF one = new PointF(x1, y2);
        PointF two = new PointF(x1, y1);
        PointF three = new PointF(x2, y1);
        PointF four = new PointF(x2, y2);

        // We create the sin and cos function once,
        // so we do not have calculate them each time.
        float s = (float) Math.sin(angle);
        float c = (float) Math.cos(angle);

        // Then we rotate each point
        one.x = x1 * c - y2 * s;
        one.y = x1 * s + y2 * c;
        two.x = x1 * c - y1 * s;
        two.y = x1 * s + y1 * c;
        three.x = x2 * c - y1 * s;
        three.y = x2 * s + y1 * c;
        four.x = x2 * c - y2 * s;
        four.y = x2 * s + y2 * c;

        // Finally we translate the sprite to its correct position.
        one.x += translation.x;
        one.y += translation.y;
        two.x += translation.x;
        two.y += translation.y;
        three.x += translation.x;
        three.y += translation.y;
        four.x += translation.x;
        four.y += translation.y;

        // We now return our float array of vertices.
        return new float[]
                {
                        one.x, one.y, 0.0f,
                        two.x, two.y, 0.0f,
                        three.x, three.y, 0.0f,
                        four.x, four.y, 0.0f,
        };
    }

    public void SetupImage(String image_resource_name, int pos, int[] texturenames) {
        
        SetupTriangle();
        
        // Create our UV coordinates.
        uvs = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        texture_position = pos;

        // The texture buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        uvBuffer = bb.asFloatBuffer();
        uvBuffer.put(uvs);
        uvBuffer.position(0);

        // Generate Textures
        GLES20.glGenTextures(texturenames.length, texturenames, 0);

        // Retrieve our image from resources.
        int id = mContext.getResources().getIdentifier(image_resource_name, null, mContext.getPackageName());

        // Temporary create a bitmap
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), id);

        int textureIntVal = GLES20.GL_TEXTURE0 + pos ;


        GLES20.glActiveTexture(textureIntVal);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[pos]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bmp, 0);

        // We are done using the bitmap so we should recycle it.
        bmp.recycle();

    }



    private void SetupTriangle()   {
        // Get information of sprite.
        vertices = this.getTransformedVertices();

        // The order of vertexrendering for a quad
        indices = new short[] {0, 1, 2, 0, 2, 3};

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);
    }

    public void UpdateSprite() {
        // Get new transformed vertices
        vertices = this.getTransformedVertices();

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertices);
        vertexBuffer.position(0);
    }

    public void handleActionDown(float eventX, float eventY) {


        float x1 = translation.x + base.left;
        float x2 = translation.x + base.right;
        float y1 = translation.y + base.bottom;
        float y2 = translation.y + base.top ;


        if (eventX >= x1 && eventX <= x2) {
            if (eventY >= y1 && eventY <= y2) {
                //System.out.println("Sprite TOUCHED");
                 setTouched(true);
            } else {
                //System.out.println("Sprite NOT TOUCHED");
                setTouched(false);
            }
        } else {
            setTouched(false);
        }
    }

    public void setNotTouched(){
        isTouched=false;
    }

    private void setTouched(boolean touch){
        isTouched=touch;
    }

    public boolean isTouched(){
        return isTouched;
    }
}