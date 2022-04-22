package com.pontus.froppy;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Iterator;
import java.util.Vector;

public abstract class ImageTileManager {

    private final  float RI_TEXT_UV_BOX_WIDTH ;
    private final  float RI_TEXT_WIDTH ;

    private final  int IMAGE_MATRIX_SIZE;
    private final  int IMAGE_MATRIX_LINE_SIZE;

    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer drawListBuffer;

    private float[] vecs;
    private float[] uvs;
    private short[] indices;
    private float[] colors;

    private int index_vecs;
    private int index_indices;
    private int index_uvs;
    private int index_colors;

    private int texturenr;

    private float uniformscale;

    public static int[] l_size;

    public Vector<TileObject> tileObjectCollection;

    public ImageTileManager(int[] h_size_matrix, float ri_text_uv_box_width, float ri_text_width, int image_matrix_size, int image_matrix_line_size) {

        RI_TEXT_UV_BOX_WIDTH = ri_text_uv_box_width;
        RI_TEXT_WIDTH = ri_text_width;
        IMAGE_MATRIX_SIZE = image_matrix_size;
        IMAGE_MATRIX_LINE_SIZE = image_matrix_line_size;

        //set matrix;
        setL_size(h_size_matrix);
        // Create our container
        tileObjectCollection = new Vector<TileObject>();

        // Create the arrays
        vecs = new float[3 * IMAGE_MATRIX_SIZE];
        colors = new float[4 * IMAGE_MATRIX_SIZE];
        uvs = new float[2 * IMAGE_MATRIX_SIZE];
        indices = new short[IMAGE_MATRIX_SIZE];

        // init as 0 as default
        texturenr = 0;
    }

    private void setL_size(int[] h_size_matrix) {

        this.l_size=h_size_matrix;
    }


    public void addTileObject(TileObject obj) {
        // Add text object to our collection
        tileObjectCollection.add(obj);
    }

    public void removeTileObject(TileObject obj) {
        // Add text object to our collection
        tileObjectCollection.remove(obj);
    }

    public void removeAll() {
        // Add text object to our collection
        tileObjectCollection.removeAllElements();
    }


    public void setTextureID(int val) {
        texturenr = val;
    }


    public void AddRenderInformation(float[] vec, float[] cs, float[] uv, short[] indi) {
        // We need a base value because the object has indices related to
        // that object and not to this collection so basicly we need to
        // translate the indices to align with the vertexlocation in our
        // vecs array of vectors.

        short base = (short) (index_vecs / 3);

        // We should add the vec, translating the indices to our saved vector
        for (int i = 0; i < vec.length; i++) {
            vecs[index_vecs] = vec[i];
            index_vecs++;
        }

        // We should add the colors, so we can use the same texture for multiple effects.
        for (int i = 0; i < cs.length; i++) {
            colors[index_colors] = cs[i];
            index_colors++;
        }

        // We should add the uvs
        for (int i = 0; i < uv.length; i++) {
            uvs[index_uvs] = uv[i];
            index_uvs++;
        }

        // We handle the indices
        for (int j = 0; j < indi.length; j++) {
            indices[index_indices] = (short) (base + indi[j]);
            index_indices++;
        }
    }

    public void PrepareDrawInfo() {
        // Reset the indices.
        index_vecs = 0;
        index_indices = 0;
        index_uvs = 0;
        index_colors = 0;

        // Get the total amount of characters
        int charcount = 0;

        for (TileObject tobj : tileObjectCollection) {
            if (tobj != null) {
                charcount += 1;

            }
        }

        // Create the arrays we need with the correct size.
        vecs = null;
        colors = null;
        uvs = null;
        indices = null;

        vecs = new float[charcount * 12];
        colors = new float[charcount * 16];
        uvs = new float[charcount * 8];
        indices = new short[charcount * 6];

    }

    public void PrepareDraw() {
        // Setup all the arrays
        PrepareDrawInfo();

        // Using the iterator protects for problems with concurrency
        for (Iterator<TileObject> it = tileObjectCollection.iterator(); it.hasNext(); ) {
            TileObject txt = it.next();
            if (txt != null) {
                convertTextToTriangleInfo(txt);
            }
        }
    }

    public void Draw(float[] m) {
        // Set the correct shader for our grid object.
        GLES20.glUseProgram(riGraphicTools.sp_Text);

        // Enable blending
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND_DST_ALPHA);

        // The vertex buffer.
        ByteBuffer bb = ByteBuffer.allocateDirect(vecs.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vecs);
        vertexBuffer.position(0);

        // The vertex buffer.
        ByteBuffer bb3 = ByteBuffer.allocateDirect(colors.length * 4);
        bb3.order(ByteOrder.nativeOrder());
        colorBuffer = bb3.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        // The texture buffer
        ByteBuffer bb2 = ByteBuffer.allocateDirect(uvs.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        textureBuffer = bb2.asFloatBuffer();
        textureBuffer.put(uvs);
        textureBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(indices);
        drawListBuffer.position(0);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Text, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the background coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.sp_Text, "a_texCoord");

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, textureBuffer);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);

        int mColorHandle = GLES20.glGetAttribLocation(riGraphicTools.sp_Text, "a_Color");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mColorHandle);

        // Prepare the background coordinate data
        GLES20.glVertexAttribPointer(mColorHandle, 4,
                GLES20.GL_FLOAT, false,
                0, colorBuffer);

        // get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(riGraphicTools.sp_Text, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0);

        int mSamplerLoc = GLES20.glGetUniformLocation(riGraphicTools.sp_Text, "s_texture");

        // Set the sampler texture unit to our selected id
        GLES20.glUniform1i(mSamplerLoc, texturenr);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
        GLES20.glDisableVertexAttribArray(mColorHandle);

    }

    private void convertTextToTriangleInfo(TileObject val) {
        // Get attributes from text object
        float x = val.x;
        float y = val.y;

        // Create

        int indx = val.getIndex();


        // Calculate the uv parts
        int row = indx / IMAGE_MATRIX_LINE_SIZE;
        int col = indx % IMAGE_MATRIX_LINE_SIZE;

        float v = row * RI_TEXT_UV_BOX_WIDTH;
        float v2 = v + RI_TEXT_UV_BOX_WIDTH;
        float u = col * RI_TEXT_UV_BOX_WIDTH;
        float u2 = u + RI_TEXT_UV_BOX_WIDTH;

        // Creating the triangle information
        float[] vec = new float[12];
        float[] uv = new float[8];
        float[] colors = new float[16];

        vec[0] = x;                                     // 500
        vec[1] = y + (RI_TEXT_WIDTH * uniformscale);    // 500 + 250 * 1 = 750
        vec[2] = 0.99f;

        vec[3] = x;                                     // 500
        vec[4] = y;                                     // 500
        vec[5] = 0.99f;

        vec[6] = x + (RI_TEXT_WIDTH * uniformscale);    // 500 + 250 * 1 = 750
        vec[7] = y;                                     // 500
        vec[8] = 0.99f;

        vec[9] = x + (RI_TEXT_WIDTH * uniformscale);    // 500 + 250 * 1 = 750
        vec[10] = y + (RI_TEXT_WIDTH * uniformscale);   // 500 + 250 * 1 = 750
        vec[11] = 0.99f;

        colors = new float[]
                {val.color[0], val.color[1], val.color[2], val.color[3],
                        val.color[0], val.color[1], val.color[2], val.color[3],
                        val.color[0], val.color[1], val.color[2], val.color[3],
                        val.color[0], val.color[1], val.color[2], val.color[3]
                };
        // 0.001f = texture bleeding hack/fix
        uv[0] = u + 0.001f; // 0.333
        uv[1] = v + 0.001f; // 0
        uv[2] = u + 0.001f;  // 0.333
        uv[3] = v2 - 0.001f; // 0.333
        uv[4] = u2 - 0.001f; // 0.666
        uv[5] = v2 - 0.001f; // 0.333
        uv[6] = u2 - 0.001f; // 0.666
        uv[7] = v + 0.001f;  // 0

        short[] inds = {0, 1, 2, 0, 2, 3};

        // Add our triangle information to our collection for 1 render call.
        AddRenderInformation(vec, colors, uv, inds);

        // Calculate the new position
        x += ((l_size[indx] / 2) * uniformscale);

    }

    public float getUniformscale() {
        return uniformscale;
    }

    public void setUniformscale(float uniformscale) {
        this.uniformscale = uniformscale;
    }
}
