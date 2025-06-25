package com.example.computergraphics.object;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.example.computergraphics.utils.MatrixUtils;
import com.example.computergraphics.utils.Utils;

public class Line extends GraphicObject {
    static float []  defaultSource = {
            -2.0f, 0.0f, -2.0f
    };
    static float [] defaultDirection = {
            0.1f, 0.0f, 0.1f
    };
    static int defaultMaxLength = 40;
    public final int typ = 1;
    public float [] source;
    public float [] direction;
    public float length;
    public float [] color = {0f, 0f, 1f, 1f};
    public Line(float [] source, float [] direction, float length, int program, Context context){
        this(source,
            MatrixUtils.add(source,
                MatrixUtils.mul(direction, length > 0f ? length : defaultMaxLength)),
            program,
            context);
    }
    public Line(Context context){
        this(defaultSource, defaultDirection, defaultMaxLength, context);
    }
    public Line(float [] start, float [] end, int program, Context context){
        super(
            new float[] {start[0], start[1], start[2], end[0], end[1], end[2]},
            null,
            null,
            program,
            context
        );
        source = start;
        float [] ray = MatrixUtils.sub(end, start);
        length = MatrixUtils.norm(ray);
        direction = new float[] {ray[0] / length, ray[1] / length, ray[2] / length};
    }
    public float[] getIntersectionWithLine(Line line){
        float [] s1 = source;
        float [] s2 = line.source;
        float [] d1 = direction;
        float [] d2 = line.direction;
        float [] r = MatrixUtils.sub(s1, s2);
        float a = MatrixUtils.MM(d1, d1);
        float b = MatrixUtils.MM(d1, d2);
        float c = MatrixUtils.MM(d2, d2);
        float d = MatrixUtils.MM(d1, r);
        float e = MatrixUtils.MM(d2, r);
        float denom = a*c - b*b;
        if (Math.abs(denom) < 1e-6){
            return null;
        }
        float t = (b*e - c*d)/denom;
        float s = (a*e - b*d)/denom;

        float [] p1 = new float [] {
                s1[0] + t*d1[0], s1[1] + t*d1[1], s1[2] + t*d1[2],
        };
        float [] p2 = new float [] {
                s2[0] + s*d2[0], s2[1] + s*d2[1], s2[2] + s*d2[2],
        };
        float [] dis = MatrixUtils.sub(p1, p2);
        if (dis[0] * dis[0] + dis[1] * dis[1] + dis[2] * dis[2] > 1e-6){
            return null;
        }
        return p1;
    }
    public void draw(float [] vMatrix, float[] pMatrix, float[] worldRotationMatrix, float [] eye) {
        // Add program to OpenGL ES environment
        GLES30.glUseProgram(program);

        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            // Linking failed
            Log.e("Shader", "Program linking failed.");
            Log.e("Shader", GLES30.glGetProgramInfoLog(program));
        }

        int mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
        int mvMatrixHandle = GLES30.glGetUniformLocation(program, "uMVMatrix");
        int colorHandle = GLES30.glGetUniformLocation(program, "uColor");
        int viewPositionHandle = GLES30.glGetUniformLocation(program, "uViewPosition");

        int positionHandle = GLES30.glGetAttribLocation(program, "aPosition");

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(positionHandle);

        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // Set color for drawing the triangle
        GLES30.glUniform4fv(colorHandle, 1, color, 0);

        modelMatrix = Utils.getModelMatrix(translation, rotation, scale);
        Matrix.multiplyMM(modelMatrix, 0, worldRotationMatrix, 0, modelMatrix, 0);
        float [] vpMatrix = new float[16];
        Matrix.multiplyMM(vpMatrix, 0, pMatrix, 0, vMatrix, 0);
        float [] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, modelMatrix, 0);
        // Pass the projection and view transformation to the shader
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

        float [] mvMatrix = new float[16];
        Matrix.multiplyMM(mvMatrix, 0, vMatrix, 0, modelMatrix, 0);
        GLES30.glUniformMatrix4fv(mvMatrixHandle, 1, false, mvMatrix, 0);

        GLES30.glUniform3fv(viewPositionHandle, 1, eye, 0);

        GLES30.glDrawArrays(GLES30.GL_LINES, 0, vertexData.length / COORDS_PER_VERTEX);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(positionHandle);
    }
    public float[] getWorldSource(){
        float[] modelMatrix = Utils.getModelMatrix(translation, rotation, scale);
        float[] worldSource = new float[] {source[0], source[1], source[2], 1};
        Matrix.multiplyMV(worldSource, 0, modelMatrix, 0, worldSource, 0);
        return new float[] {worldSource[0], worldSource[1], worldSource[2]};
    }
    public float[] getWorldDirection(){
        float[] modelMatrix = Utils.getModelMatrix(translation, rotation, scale);
        float[] worldDirection = new float[] {direction[0], direction[1], direction[2], 1};
        Matrix.multiplyMV(worldDirection, 0, modelMatrix, 0, worldDirection, 0);
        return new float[] {worldDirection[0], worldDirection[1], worldDirection[2]};
    }
}

