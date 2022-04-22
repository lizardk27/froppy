package com.pontus.froppy;

/**
 * Created by usuario on 5/4/2016.
 */

public class SignsTileManager extends ImageTileManager {

    private static float RI_TEXT_UV_BOX_WIDTH = 0.333f;
    private static float RI_TEXT_WIDTH = 250f;

    public SignsTileManager() {
        super(new int[]{
                512, 512, 512,
                512, 512, 512,
                512, 512, 512}, RI_TEXT_UV_BOX_WIDTH, RI_TEXT_WIDTH, 10, 3);

    }

}
