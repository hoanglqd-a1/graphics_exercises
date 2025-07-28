package com.example.computergraphics.renderer;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.computergraphics.PointLight;
import com.example.computergraphics.R;
import com.example.computergraphics.camera.Camera;
import com.example.computergraphics.object.GraphicObject;
import com.example.computergraphics.object.Sphere;
import com.example.computergraphics.utils.ArgbColor;
import com.example.computergraphics.utils.MatrixUtils;
import com.example.computergraphics.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VolumeRenderer extends BaseRenderer{
    int width, height;
    float[] cameraLookAt = {0f, 0f, 0f};
    float cameraAspect;
    Program program;
    String TAG = "RAYTRACING";
    Sphere ground;
    Camera camera;
    List<GraphicObject> objectList = new ArrayList<>();
    //    PointLight pointLight = new PointLight(new float[] {0f, 3f, 5f}, ArgbColor.white);
    List<PointLight> worldPointLights = new ArrayList<>();
    final float[] BACKGROUND_COLOR = ArgbColor.lightBlue;
    final int MAXIMUM_REFLECTION = 3;
    float AIR_REFRACTIVE_INDEX = 1f;
    float[] FULLY_REFLECTIVE = {0f, 1f, 0f};
    float[] FULLY_REFRACTIVE = {0f, 0f, 1f};
    float[] HALF_REFLECTIVE = {0.5f, 0.5f, 0f};
    float MIN_TRANSMITTANCE = 0.01f;
    Sphere volumeSphere;
    float[] sphereCenter = {0f, 0f, 0f};
    float sphereRadius = 2f;
    public VolumeRenderer(Context context){
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
        volumeSphere = new Sphere(
            sphereCenter,
            sphereRadius,
            context
        );
        volumeSphere.isVolume = true;
        volumeSphere.volumeDensity = 0.3f;
        volumeSphere.setColor(ArgbColor.green);
        objectList.add(volumeSphere);

//        worldPointLights.add(new PointLight(new float[] {3f, 3f, 3f}, ArgbColor.white));
        worldPointLights.add(new PointLight(new float[] {0f, 5f, 0f}, ArgbColor.white));
        worldPointLights.add(new PointLight(new float[] {0f, 5f, 0f}, ArgbColor.white));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        cameraAspect = (float) width / height;

        float[] forward = MatrixUtils.normalize(MatrixUtils.sub(cameraLookAt, eye));
        float[] right = MatrixUtils.normalize(
            MatrixUtils.crossProduct(forward, new float[] {0f, 1f, 0f})
        );
        float[] up = MatrixUtils.normalize(
            MatrixUtils.crossProduct(right, forward)
        );

        Matrix.frustumM(projectionMatrix, 0, -cameraAspect, cameraAspect, -1, 1, 1, 10);
        program.useProgram();

        camera = new Camera(
            width / 4,
            height / 4,
            objectList,
            eye,
            forward,
            right,
            up,
            cameraAspect,
            MAXIMUM_REFLECTION,
            BACKGROUND_COLOR,
            AIR_REFRACTIVE_INDEX,
            MIN_TRANSMITTANCE,
            worldPointLights,
            context
        );
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

        Matrix.orthoM(projectionMatrix, 0, -cameraAspect, cameraAspect, -1f, 1f, 0f, 1f);
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        program.draw(camera.screen);
    }
}
