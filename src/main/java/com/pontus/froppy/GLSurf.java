package com.pontus.froppy;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GLSurf extends GLSurfaceView {

    private final FroppySTRenderer mRenderer;
    private final Context mContext;

    public GLSurf(Context context) {
        super(context);
        mContext= context;

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new FroppySTRenderer(context);
        setRenderer(mRenderer);

        // Render the view continuously
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }



    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mRenderer.processTouchEvent(e);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        //System.out.println("GL SURFACE ON RESUME!!");
        mRenderer.onResume();
    }

    public void onDestroy() {
        ((MainActivity)mContext).onDestroy();
    }


    public FroppySTRenderer getSpiderAttackRenderer(){
        return mRenderer==null?null:mRenderer;
    }

}
