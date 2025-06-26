package com.example.computergraphics.object;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;
import android.util.Log;

import com.example.computergraphics.utils.Utils;

public class Point extends GraphicObject {
    public float [] color = new float [] {1f, 0f, 0f, 1f};
    public Point(float [] coordinate, int program, Context context){
        super(coordinate, null, null, program, context);
    }
    public void draw(float [] vMatrix, float[] pMatrix, float[] worldRotationMatrix, float [] eye) {
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

        int textureHandle = mtl.textureHandle;
        GLES30.glUniform1i(useTexture, 0);
        if (this.textureCoordinateBuffer != null && textureCoordinateHandle >= 0) {
            GLES30.glEnableVertexAttribArray(textureCoordinateHandle);
            GLES30.glUniform1i(useTexture, 1);
            GLES30.glVertexAttribPointer(
                    textureCoordinateHandle,
                    textureCoordinateDataSize,
                    GLES30.GL_FLOAT,
                    false,
                    0,
                    textureCoordinateBuffer
            );
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle);
            GLES30.glUniform1i(textureUniformHandle, 0);
        }

        if (this.lightPositionData != null && lightPositionHandle >= 0){
            GLES30.glUniform3fv(lightPositionHandle, 1, this.lightPositionData, 0);
        }

        GLES30.glUniform3fv(viewPositionHandle, 1, eye, 0);

        GLES30.glUniform3fv(ambientHandle, 1, mtl.Ka, 0);
        GLES30.glUniform3fv(diffuseHandle, 1, mtl.Kd, 0);
        GLES30.glUniform3fv(specularHandle, 1, mtl.Ks, 0);

        if (this.normalBuffer != null && normalHandle >= 0){
            GLES30.glUniform1i(useNormal, 1);
            GLES30.glEnableVertexAttribArray(normalHandle);
            GLES30.glVertexAttribPointer(normalHandle, COORDS_PER_NORMAL,
                    GLES30.GL_FLOAT, false,
                    normalStride, normalBuffer);
        }
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, vertexData.length / COORDS_PER_VERTEX);

        // Disable vertex array
        GLES30.glDisableVertexAttribArray(positionHandle);
    }
    public float[] getColor() {
        return color;
    }
}
