package com.example.computergraphics.object;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.example.computergraphics.R;
import com.example.computergraphics.utils.MatrixUtils;

import java.util.Arrays;

public class Cube extends GraphicObject {
    static float defaultCubeCoords[] = {
            -0.5f,  0.5f, 0.5f,   // top left front
            -0.5f, -0.5f, 0.5f,   // bottom left front
            0.5f, -0.5f, 0.5f,   // bottom right front
            0.5f,  0.5f, 0.5f,   // top right front
            -0.5f,  0.5f,  -0.5f,   // top left back
            -0.5f, -0.5f,  -0.5f,   // bottom left back
            0.5f, -0.5f,  -0.5f,   // bottom right back
            0.5f,  0.5f,  -0.5f }; // top right back
    static int defaultDrawOrder[] = {
            0, 1, 2, 0, 2, 3, // front face
            4, 7, 6, 4, 6, 5, // back face
            0, 4, 5, 0, 5, 1, // left face
            2, 6, 7, 2, 7, 3, // right face
            0, 3, 7, 0, 7, 4, // top face
            1, 5, 6, 1, 6, 2, // bottom face
    };
    static float defaultNormalData [] = {
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // Back face
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,

            // Left face
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,
            -1.0f, 0.0f, 0.0f,

            // Right face
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            // Top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f,

            // Bottom face
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
            0.0f, -1.0f, 0.0f,
    };
    static float defaultTextureCoordinateData[] = {
            // Front face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Back face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Left face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Right face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Top face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,

            // Bottom face
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };
    public Cube(Context context) {
        this(createData(defaultCubeCoords, defaultDrawOrder, 3),
            defaultNormalData, defaultTextureCoordinateData,
            context);
        mtl.textureHandle = loadTexture(context, R.drawable.bumpy_bricks_public_domain);
    }
    public Cube(float [] vertexData, float [] normalData, float [] textureData, Context context){
        super(vertexData, normalData, textureData, context);
    }
}
