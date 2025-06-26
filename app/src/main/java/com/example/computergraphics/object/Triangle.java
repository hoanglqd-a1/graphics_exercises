package com.example.computergraphics.object;

import android.content.Context;

public class Triangle extends GraphicObject{
    public static float[] defaultVertexData = {   // in counterclockwise order:
            0.0f,  0.622008459f, 0.0f, // top
            -0.5f, -0.311004243f, 0.0f, // bottom left
            0.5f, -0.311004243f, 0.0f,  // bottom right
    };
    public static float[] defaultNormalData = {
            0f, 0f, 1f,
            0f, 0f, 1f,
            0f, 0f, 1f,
    };
    public Triangle(float[] vertexData, float[] normalData, float[] textureCoordinateData, int program, Context context) {
        super(vertexData, normalData, textureCoordinateData, program, context);
    }
    public Triangle(int program, Context context){
        this(defaultVertexData, defaultNormalData, null, program, context);
    }
}
