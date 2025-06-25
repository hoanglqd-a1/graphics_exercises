package com.example.computergraphics;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.example.computergraphics.renderer.MyGLRenderer;

public class MyGLSurfaceView extends GLSurfaceView {
    private final MyGLRenderer renderer;
    private float previousX;
    private float previousY;

    public MyGLSurfaceView(float [] source, float [] direction, Context context){
        super(context);

        // Create an OpenGL ES 3.0 context
        setEGLContextClientVersion(3);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        renderer = new MyGLRenderer(source, direction, context);

        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }
   @Override
   public boolean onTouchEvent(MotionEvent e) {
       // MotionEvent reports input details from the touch screen
       // and other input controls. In this case, you are only
       // interested in events where the touch position changed.

       float x = e.getX();
       float y = e.getY();

       float TOUCH_SCALE_FACTOR = 180.0f / 320;
       switch (e.getAction()) {
           case MotionEvent.ACTION_MOVE:

               float dx = x - previousX;
               float dy = y - previousY;

               // reverse direction of rotation above the mid-line
               if (y > (float) getHeight() / 2) {
                   dx = dx * -1 ;
               }

               // reverse direction of rotation to left of the mid-line
               if (x < (float) getWidth() / 2) {
                   dy = dy * -1 ;
               }

               renderer.setAngle(
                       renderer.getAngle() +
                               ((dx + dy) * TOUCH_SCALE_FACTOR));
               requestRender();
       }

       previousX = x;
       previousY = y;
       return true;
   }
}
