package com.example.computergraphics.object;

import android.content.Context;

public class Screen extends GraphicObject{
    public float[] color = {1f, 1f, 1f, 1f};
    static float[] defaultVertexData = {
            -0.5f,  0.5f, 0f,   // top left front
            -0.5f, -0.5f, 0f,   // bottom left front
            0.5f, -0.5f, 0f,   // bottom right front
            -0.5f,  0.5f, 0f,   // top left front
            0.5f, -0.5f, 0f,   // bottom right front
            0.5f,  0.5f, 0f,   // top right front
    };
    static float[] defaultNormalData = {
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
    };
    static float[] defaultTextureData = {
//        0.0f, 1.0f, // top left of quad (-0.5f,  0.5f, 0.5f) gets top-left of texture
//        0.0f, 0.0f, // bottom left of quad (-0.5f, -0.5f, 0.5f) gets bottom-left of texture
//        1.0f, 0.0f, // bottom right of quad (0.5f, -0.5f, 0.5f) gets bottom-right of texture
//        0.0f, 1.0f, // top left of quad (-0.5f,  0.5f, 0.5f) gets top-left of texture
//        1.0f, 0.0f, // bottom right of quad (0.5f, -0.5f, 0.5f) gets bottom-right of texture
//        1.0f, 1.0f, // top right of quad (0.5f,  0.5f, 0.5f) gets top-right of texture
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };
    public Screen(Context context){
        super(defaultVertexData, defaultNormalData, defaultTextureData, context);
    }
    public float[] getColor() {
        return color;
    }
    public void setColor(float[] color){
        this.color = color;
    }
}