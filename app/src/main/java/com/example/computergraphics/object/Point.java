package com.example.computergraphics.object;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.example.computergraphics.utils.Utility;

public class Point extends GraphicObject {
    public final int typ = 0;
    public float [] color = new float [] {1f, 0f, 0f, 1f};
    public Point(float [] coordinate, Context context){
        super(coordinate, null, null, GLES30.glCreateProgram(), context);
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
        int useTexture = GLES30.glGetUniformLocation(program, "uUseTexture");
        int useNormal = GLES30.glGetUniformLocation(program, "uUseNormal");
        int textureUniformHandle = GLES30.glGetUniformLocation(program, "uTexture");
        int colorHandle = GLES30.glGetUniformLocation(program, "uColor");
        int lightPositionHandle = GLES30.glGetUniformLocation(program, "uLightPosition");
        int viewPositionHandle = GLES30.glGetUniformLocation(program, "uViewPosition");
        int ambientHandle = GLES30.glGetUniformLocation(program, "Ka");
        int diffuseHandle = GLES30.glGetUniformLocation(program, "Kd");
        int specularHandle = GLES30.glGetUniformLocation(program, "Ks");

        int positionHandle = GLES30.glGetAttribLocation(program, "aPosition");
        int normalHandle = GLES30.glGetAttribLocation(program, "aNormal");
        int textureCoordinateHandle = GLES30.glGetAttribLocation(program, "aTextureCoordinate");

        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(positionHandle);
        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        GLES30.glUniform4fv(colorHandle, 1, color, 0);

        modelMatrix = Utility.getModelMatrix(translation, rotation, scale);
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
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, vertexData.length / COORDS_PER_VERTEX);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(positionHandle);
    }
}
