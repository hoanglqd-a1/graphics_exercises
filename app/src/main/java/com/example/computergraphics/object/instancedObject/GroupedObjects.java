package com.example.computergraphics.object.instancedObject;

import android.content.Context;
import android.opengl.GLES30;
import android.util.Log;

import com.example.computergraphics.object.GraphicObject;
import com.example.computergraphics.renderer.CollisionDetectionRenderer;
import com.example.computergraphics.R;
import com.example.computergraphics.utils.Utils;

import java.nio.FloatBuffer;

public class GroupedObjects extends GraphicObject {
    private FloatBuffer instanceModelMatrixBuffer;
    private int instanceMatrixBufferId;
    public int copies;
    public int program;
    public GroupedObjects(float[] vertexData,
                          float[] normalData,
                          float[] textureCoordinateData,
                          int program,
                          Context context,
                          int copies,
                          float[] instancedModelMatrices){
        super(vertexData, normalData, textureCoordinateData, context);
        this.program = program;
        this.copies = copies;
        instanceModelMatrixBuffer = toBuffer(instancedModelMatrices);

        int[] buffers = new int[1];
        GLES30.glGenBuffers(1, buffers, 0);
        instanceMatrixBufferId = buffers[0];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, instanceMatrixBufferId);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, instanceModelMatrixBuffer.capacity() * 4,
                null, GLES30.GL_DYNAMIC_DRAW); // Use DYNAMIC_DRAW as data will change
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0); // Unbind
    }
    protected void initProgram() {
        String vertexShaderCode = Utils.readRawTextFile(context, R.raw.instanced_vertex_shader);
        String fragmentShaderCode = Utils.readRawTextFile(context, R.raw.fragment_shader);
        int vertexShader = CollisionDetectionRenderer.loadShader(GLES30.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = CollisionDetectionRenderer.loadShader(GLES30.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        // create empty OpenGL ES Program
        GLES30.glAttachShader(this.program, vertexShader);
        GLES30.glAttachShader(this.program, fragmentShader);
        GLES30.glLinkProgram(this.program);
    }
    public void draw(float[] viewMatrix,
                     float[] projectionMatrix,
                     float[] worldRotationMatrix,
                     float[] eye){
        GLES30.glUseProgram(program);

        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            // Linking failed
            Log.e("Shader", "Program linking failed.");
            Log.e("Shader", GLES30.glGetProgramInfoLog(program));
        }

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, instanceMatrixBufferId);
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0,
                instanceModelMatrixBuffer.limit() * 4,
                instanceModelMatrixBuffer);

        int viewMatrixHandle = GLES30.glGetUniformLocation(program, "uViewMatrix");
        int projectionMatrixHandle = GLES30.glGetUniformLocation(program, "uProjectionMatrix");
        int worldRotationMatrixHandle = GLES30.glGetUniformLocation(program, "uWorldRotationMatrix");
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
        int instancedModelMatrixRow1Handle = GLES30.glGetAttribLocation(program, "aInstancedModelMatrixRow1");
        int instancedModelMatrixRow2Handle = GLES30.glGetAttribLocation(program, "aInstancedModelMatrixRow2");
        int instancedModelMatrixRow3Handle = GLES30.glGetAttribLocation(program, "aInstancedModelMatrixRow3");
        int instancedModelMatrixRow4Handle = GLES30.glGetAttribLocation(program, "aInstancedModelMatrixRow4");

        // Set color for drawing the triangle
        GLES30.glUniform4fv(colorHandle, 1, color, 0);

        GLES30.glUniformMatrix4fv(viewMatrixHandle, 1, false, viewMatrix, 0);
        GLES30.glUniformMatrix4fv(projectionMatrixHandle, 1, false, projectionMatrix, 0);
        GLES30.glUniformMatrix4fv(worldRotationMatrixHandle, 1, false, worldRotationMatrix, 0);

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
        // Enable a handle to the triangle vertices
        GLES30.glEnableVertexAttribArray(positionHandle);
        // Prepare the triangle coordinate data
        GLES30.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        int stride = 16 * 4;
        GLES30.glEnableVertexAttribArray(instancedModelMatrixRow1Handle);
        GLES30.glEnableVertexAttribArray(instancedModelMatrixRow2Handle);
        GLES30.glEnableVertexAttribArray(instancedModelMatrixRow3Handle);
        GLES30.glEnableVertexAttribArray(instancedModelMatrixRow4Handle);
        GLES30.glVertexAttribPointer(
                instancedModelMatrixRow1Handle,
                4,
                GLES30.GL_FLOAT,
                false,
                stride,
                0);
        GLES30.glVertexAttribPointer(
                instancedModelMatrixRow2Handle,
                4,
                GLES30.GL_FLOAT,
                false,
                stride,
                4 * 4);
        GLES30.glVertexAttribPointer(
                instancedModelMatrixRow3Handle,
                4,
                GLES30.GL_FLOAT,
                false,
                stride,
                8 * 4);
        GLES30.glVertexAttribPointer(
                instancedModelMatrixRow4Handle,
                4,
                GLES30.GL_FLOAT,
                false,
                stride,
                12 * 4);
        GLES30.glVertexAttribDivisor(instancedModelMatrixRow1Handle, 1);
        GLES30.glVertexAttribDivisor(instancedModelMatrixRow2Handle, 1);
        GLES30.glVertexAttribDivisor(instancedModelMatrixRow3Handle, 1);
        GLES30.glVertexAttribDivisor(instancedModelMatrixRow4Handle, 1);

        GLES30.glDrawArrays(GLES30.GL_TRIANGLES,
                0,
                vertexData.length / COORDS_PER_VERTEX);
        // Disable vertex array
        GLES30.glDisableVertexAttribArray(positionHandle);
        GLES30.glDisableVertexAttribArray(instancedModelMatrixRow1Handle);
        GLES30.glDisableVertexAttribArray(instancedModelMatrixRow2Handle);
        GLES30.glDisableVertexAttribArray(instancedModelMatrixRow3Handle);
        GLES30.glDisableVertexAttribArray(instancedModelMatrixRow4Handle);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
    }
}
