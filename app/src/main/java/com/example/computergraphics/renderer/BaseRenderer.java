package com.example.computergraphics.renderer;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.computergraphics.object.GraphicObject;
import com.example.computergraphics.object.Line;
import com.example.computergraphics.object.Point;
import com.example.computergraphics.utils.MaterialFileHandle;
import com.example.computergraphics.utils.Utils;

public abstract class BaseRenderer implements GLSurfaceView.Renderer {
    protected Context context;
    protected float[] vpMatrix = new float[16];
    protected float[] projectionMatrix = new float[16];
    protected float[] viewMatrix = new float [16];
    protected float[] eye = { 0.0f, 2.0f, 3.0f };
    protected float[] lightSource;
    public volatile float mAngle;
    public float getAngle() {
        return this.mAngle;
    }
    public void setAngle(float angle) {
        this.mAngle = angle;
    }
    public static int loadShader(int type, String shaderCode){
        int shader = GLES30.glCreateShader(type);
        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        return shader;
    }
    public class Program {
        public int program;
        int mvpMatrixHandle;
        int mvMatrixHandle;
        int useTexture;
        int useNormal;
        int useLight;
        int textureUniformHandle;
        int colorHandle;
        int lightPositionHandle;
        int viewPositionHandle;
        int ambientHandle;
        int diffuseHandle;
        int specularHandle;
        int positionHandle;
        int normalHandle;
        int textureCoordinateHandle;
        public Program(String vertexShaderCode, String fragmentShaderCode){
            this.program = GLES30.glCreateProgram();

            int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER,
                    vertexShaderCode);
            int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER,
                    fragmentShaderCode);
            // create empty OpenGL ES Program
            GLES30.glAttachShader(program, vertexShader);
            GLES30.glAttachShader(program, fragmentShader);
            GLES30.glLinkProgram(program);

            int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                // Linking failed
                Log.e("Shader", "Program linking failed.");
                Log.e("Shader", GLES30.glGetProgramInfoLog(program));
            }

            mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix");
            mvMatrixHandle = GLES30.glGetUniformLocation(program, "uMVMatrix");
            useTexture = GLES30.glGetUniformLocation(program, "uUseTexture");
            useNormal = GLES30.glGetUniformLocation(program, "uUseNormal");
            useLight = GLES30.glGetUniformLocation(program, "uUseLight");
            textureUniformHandle = GLES30.glGetUniformLocation(program, "uTexture");
            colorHandle = GLES30.glGetUniformLocation(program, "uColor");
            lightPositionHandle = GLES30.glGetUniformLocation(program, "uLightPosition");
            viewPositionHandle = GLES30.glGetUniformLocation(program, "uViewPosition");
            ambientHandle = GLES30.glGetUniformLocation(program, "Ka");
            diffuseHandle = GLES30.glGetUniformLocation(program, "Kd");
            specularHandle = GLES30.glGetUniformLocation(program, "Ks");

            positionHandle = GLES30.glGetAttribLocation(program, "aPosition");
            normalHandle = GLES30.glGetAttribLocation(program, "aNormal");
            textureCoordinateHandle = GLES30.glGetAttribLocation(program, "aTextureCoordinate");
        }
        public void draw(GraphicObject o){
            // Enable a handle to the triangle vertices
            GLES30.glEnableVertexAttribArray(positionHandle);

            // Prepare the triangle coordinate data
            GLES30.glVertexAttribPointer(positionHandle, GraphicObject.COORDS_PER_VERTEX,
                    GLES30.GL_FLOAT, false,
                    o.vertexStride, o.vertexBuffer);

            // Set color for drawing the triangle
            float[] color = o.getColor();
            GLES30.glUniform4fv(colorHandle, 1, color, 0);
            o.modelMatrix = Utils.getModelMatrix(o.translation, o.rotation, o.scale);
            float [] vpMatrix = new float[16];
            Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
            float [] mvpMatrix = new float[16];
            Matrix.multiplyMM(mvpMatrix, 0, vpMatrix, 0, o.modelMatrix, 0);

            // Pass the projection and view transformation to the shader
            GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0);

            float [] mvMatrix = new float[16];
            Matrix.multiplyMM(mvMatrix, 0, viewMatrix, 0, o.modelMatrix, 0);
            GLES30.glUniformMatrix4fv(mvMatrixHandle, 1, false, mvMatrix, 0);

            MaterialFileHandle mtl = o.mtl;
            int textureHandle = mtl.textureHandle;
            GLES30.glUniform1i(useTexture, 0);
            if (o.textureCoordinateBuffer != null && textureCoordinateHandle >= 0 && textureHandle >= 0) {
                GLES30.glEnableVertexAttribArray(textureCoordinateHandle);
                GLES30.glUniform1i(useTexture, 1);
                GLES30.glVertexAttribPointer(
                        textureCoordinateHandle,
                        o.textureCoordinateDataSize,
                        GLES30.GL_FLOAT,
                        false,
                        0,
                        o.textureCoordinateBuffer
                );
                GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle);
                GLES30.glUniform1i(textureUniformHandle, 0);
            }

            GLES30.glUniform1i(useLight, 0);
            if (lightSource != null && lightPositionHandle >= 0){
                GLES30.glUniform1i(useLight, 1);
                GLES30.glUniform3fv(lightPositionHandle, 1, lightSource, 0);
            }

            GLES30.glUniform3fv(viewPositionHandle, 1, eye, 0);

            GLES30.glUniform3fv(ambientHandle, 1, mtl.Ka, 0);
            GLES30.glUniform3fv(diffuseHandle, 1, mtl.Kd, 0);
            GLES30.glUniform3fv(specularHandle, 1, mtl.Ks, 0);

            if (o.normalBuffer != null && normalHandle >= 0){
                GLES30.glUniform1i(useNormal, 1);
                GLES30.glEnableVertexAttribArray(normalHandle);
                GLES30.glVertexAttribPointer(normalHandle, GraphicObject.COORDS_PER_NORMAL,
                        GLES30.GL_FLOAT, false,
                        o.normalStride, o.normalBuffer);
            }
            if (o instanceof Point){
                GLES30.glDrawArrays(GLES30.GL_POINTS, 0, o.vertexData.length / GraphicObject.COORDS_PER_VERTEX);
            } else if (o instanceof Line) {
                GLES30.glDrawArrays(GLES30.GL_LINES, 0, o.vertexData.length / GraphicObject.COORDS_PER_VERTEX);
            } else {
                GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, o.vertexData.length / GraphicObject.COORDS_PER_VERTEX);
            }
            // Disable vertex array
            GLES30.glDisableVertexAttribArray(positionHandle);
            if (textureCoordinateHandle >= 0){
                GLES30.glDisableVertexAttribArray(textureCoordinateHandle);
            }
        }
        public void useProgram(){
            GLES30.glUseProgram(program);
        }
    }
    public class RayTracingProgram{
        int program;

        int positionHandle;
        int textureCoordinateHandle;
        int useTexture;
        int useNormal;

        public RayTracingProgram(String vertexShaderCode, String fragmentShaderCode){
            this.program = GLES30.glCreateProgram();

            int vertexShader = loadShader(GLES30.GL_VERTEX_SHADER,
                    vertexShaderCode);
            int fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER,
                    fragmentShaderCode);
            // create empty OpenGL ES Program
            GLES30.glAttachShader(program, vertexShader);
            GLES30.glAttachShader(program, fragmentShader);
            GLES30.glLinkProgram(program);

            int[] linkStatus = new int[1];
            GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                // Linking failed
                Log.e("Shader", "Program linking failed.");
                Log.e("Shader", GLES30.glGetProgramInfoLog(program));
            }

            positionHandle = GLES30.glGetAttribLocation(program, "aPosition");
            textureCoordinateHandle = GLES30.glGetAttribLocation(program, "aTexCoordinate");
            useTexture = GLES30.glGetUniformLocation(program, "uUseTexture");
            useNormal = GLES30.glGetUniformLocation(program, "uUseNormal");
        }
        public void draw(GraphicObject o){
            GLES30.glEnableVertexAttribArray(positionHandle);
            GLES30.glVertexAttribPointer(positionHandle,
                    2, GLES30.GL_FLOAT,
                    false, 0, o.vertexBuffer);

            MaterialFileHandle mtl = o.mtl;
            int textureHandle = mtl.textureHandle;
            GLES30.glUniform1i(useTexture, 0);
        }
    }
}
