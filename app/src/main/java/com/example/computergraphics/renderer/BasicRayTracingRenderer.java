package com.example.computergraphics.renderer;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.content.Context;
import android.util.Log;

import com.example.computergraphics.R;
import com.example.computergraphics.object.Line;
import com.example.computergraphics.utils.MatrixUtils;
import com.example.computergraphics.utils.Utils;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BasicRayTracingRenderer extends BaseRenderer {
    int width, height;
    float[] eye = {0.0f, 0.0f, 3.0f};
    float[] lookAt = {0f, 0f, 0f};
    float[] upDirection = {0f, 1f, 0f};
    float aspect;
    Line[] lines;
    Program program;
    String TAG = "RAYTRACING";
    public BasicRayTracingRenderer(Context context){
        this.context = context;
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
//		GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glLineWidth(10.0f);

        String vertexShaderCode = Utils.readRawTextFile(context, R.raw.vertex_shader);
        String fragmentShaderCode = Utils.readRawTextFile(context, R.raw.fragment_shader);
        program = new Program(vertexShaderCode, fragmentShaderCode);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        aspect = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -aspect, aspect, -1, 1, 1, 10);
        createRayTracing();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        Matrix.setLookAtM(viewMatrix, 0,
                eye[0], eye[1], eye[2],
                lookAt[0], lookAt[1], lookAt[2],
                upDirection[0], upDirection[1], upDirection[2]);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.setRotateM(rotationMatrix, 0, mAngle, 0f, 1.0f, 0.0f);

        program.useProgram();
    }
    public void createRayTracing(){
        final int downScaledWidth = this.width / 8;
        final int downScaledHeight = this.height / 8;
        int numRay = downScaledWidth * downScaledHeight;
        lines = new Line[numRay];
        float[] forward = MatrixUtils.normalize(MatrixUtils.sub(lookAt, eye));
        float[] right = MatrixUtils.crossProduct(forward, upDirection);
        float fov = 90;
        float tanFov = (float) Math.tan(Math.toRadians(fov * 0.5));
        for(int i=0; i<downScaledWidth; i++){
            for(int j=0; j<downScaledHeight; j++){
                float ndc_i = (float) (i + 0.5) / downScaledWidth;
                float ndc_j = (float) (j + 0.5) / downScaledHeight;
                float screen_i = 2 * ndc_i - 1;
                float screen_j = 2 * ndc_j - 1;
                float[] rayDirecion = MatrixUtils.add(
                    forward,
                    MatrixUtils.add(
                        MatrixUtils.mul(right, screen_i * tanFov * aspect),
                        MatrixUtils.mul(upDirection, -screen_j * tanFov)
                    )
                );
                lines[i * downScaledHeight + j] = new Line(eye, rayDirecion, -1, context);
            }
        }
    }
}
