package com.example.computergraphics.renderer;

import android.graphics.Color;
import android.nfc.Tag;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.content.Context;
import android.util.Log;
import android.graphics.Bitmap;

import androidx.compose.ui.graphics.GraphicsContext;

import com.example.computergraphics.Camera;
import com.example.computergraphics.PointLight;
import com.example.computergraphics.R;
import com.example.computergraphics.object.*;
import com.example.computergraphics.utils.*;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BasicRayTracingRenderer extends BaseRenderer {
    int width, height;
    float[] lookAt = {0f, 0f, 0f};
    float aspect;
    Program program;
    String TAG = "RAYTRACING";
    Sphere ground;
    Camera camera;
    List<GraphicObject> objectList = new ArrayList<>();
//    PointLight pointLight = new PointLight(new float[] {0f, 3f, 5f}, ArgbColor.white);
    List<PointLight> pointLights = new ArrayList<>();
    final float[] BACKGROUND_COLOR = ArgbColor.lightBlue;
    final int MAXIMUM_REFLECTION = 3;
    float AIR_REFRACTIVE_INDEX = 1f;
    float[] FULLY_REFLECTIVE = {0f, 1f, 0f};
    float[] FULLY_REFRACTIVE = {0f, 0f, 1f};
    float[] HALF_REFLECTIVE = {0.5f, 0.5f, 0f};
    public BasicRayTracingRenderer(Context context){
        this.context = context;
        this.eye = new float[]{0.0f, 0.0f, 3.0f};
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
        ground = new Sphere(
            new float[] {0f, -1000, 0},
            999.5f,
            context
        );
        ground.setColor(ArgbColor.white);
        objectList.add(ground);
        Sphere s1 = new Sphere(new float[] {0f, 0f, 0f}, 0.5f, context);
        Sphere s2 = new Sphere(new float[] {1f, 0f, 0f}, 0.5f, context);
        Sphere s3 = new Sphere(new float[] {0f, 0f, 1f}, 0.5f, context);
        Sphere s4 = new Sphere(new float[] {-1f, 0f, 0f}, 0.5f, context);
        s1.colorCoefficient = FULLY_REFLECTIVE;
        s3.colorCoefficient = FULLY_REFRACTIVE;
        s4.colorCoefficient = HALF_REFLECTIVE;
        objectList.add(s1);
        objectList.add(s2);
//        objectList.add(s3);
        objectList.add(s4);
        pointLights.add(new PointLight(new float[] {0f, 3f, 5f}, ArgbColor.white));
        pointLights.add(new PointLight(new float[] {0f, -3f, 5f}, ArgbColor.white));
        pointLights.add(new PointLight(new float[] {0f, 3f, 3f}, ArgbColor.white));
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        aspect = (float) width / height;

        float[] forward = MatrixUtils.normalize(MatrixUtils.sub(lookAt, eye));
        float[] up = new float[] {0f, 1f, 0f};
        float[] right = MatrixUtils.crossProduct(forward, up);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -aspect, aspect, -1, 1, 1, 10);
        program.useProgram();
        camera = new Camera(
            width,
            height,
            objectList,
            eye,
            forward,
            right,
            up,
            aspect,
            MAXIMUM_REFLECTION,
            BACKGROUND_COLOR,
            pointLights,
            context
        );
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        Matrix.orthoM(projectionMatrix, 0, -aspect, aspect, -1f, 1f, 0f, 1f);
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        program.draw(camera.screen);
    }
}
