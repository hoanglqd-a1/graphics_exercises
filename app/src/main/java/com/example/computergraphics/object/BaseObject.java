package com.example.computergraphics.object;

import android.content.Context;

import java.nio.FloatBuffer;
import java.util.List;

public abstract class BaseObject {
    protected int program;
    protected Context context;
    protected final int textureCoordinateDataSize = 2;
    protected float[] vertexData;
    protected FloatBuffer vertexDataBuffer;
    static public final int COORDS_PER_VERTEX = 3;
    protected final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    public abstract void draw(float[] vMatrix, float[] pMatrix, float[] worldRotationMatrix, float[] eye);
    public abstract List<float[][]> intersectWithLine(Ray ray);

}
