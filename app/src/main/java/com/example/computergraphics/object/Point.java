package com.example.computergraphics.object;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.example.computergraphics.utils.Utils;

public class Point extends GraphicObject {
    public float [] color = new float [] {1f, 0f, 0f, 1f};
    public Point(float [] coordinate, Context context){
        super(coordinate, null, null, context);
    }
    public float[] getColor() {
        return color;
    }
}
