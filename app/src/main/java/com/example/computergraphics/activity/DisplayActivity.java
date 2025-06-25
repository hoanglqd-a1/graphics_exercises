package com.example.computergraphics.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.computergraphics.MyGLSurfaceView;

public class DisplayActivity extends Activity {
    private MyGLSurfaceView glView;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        float [] source = intent.getFloatArrayExtra(MainActivity.EXTRA_RAY_SOURCE);
        float [] direction = intent.getFloatArrayExtra(MainActivity.EXTRA_RAY_DIRECTION);
        glView = new MyGLSurfaceView(source, direction, this);
        setContentView(glView);
    }
}