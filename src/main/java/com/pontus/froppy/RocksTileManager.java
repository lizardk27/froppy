package com.pontus.froppy;

/**
 * Created by usuario on 5/4/2016.
 */
public class RocksTileManager extends ImageTileManager {

    private static final float RI_TEXT_UV_BOX_WIDTH = 0.333f;
    private static final float RI_TEXT_WIDTH = 250f;

    public RocksTileManager() {
        super(new int[]{
                130, 148, 0,
                80, 95, 0,
                40, 67, 0}, RI_TEXT_UV_BOX_WIDTH, RI_TEXT_WIDTH, 10, 3);

    }
}
