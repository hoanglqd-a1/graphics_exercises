package com.example.computergraphics.renderer;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import com.example.computergraphics.R;
import com.example.computergraphics.object.Cube;
import com.example.computergraphics.utils.MatrixUtils;
import com.example.computergraphics.utils.Utils;
import com.example.computergraphics.object.*;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.util.List;

public class TestRenderer extends BaseRenderer{
    int width, height;
    float[] rotationMatrix;
    float[] lookAt = {0f, 0f, 0f};
    float[] upDirection = {0f, 1f, 0f};
    float aspect;
    Program program;
    Sphere sphere;
    Cube cube;
    Ray ray;
    public TestRenderer(Context context){
        this.context = context;
        this.lightSource = new float[] {1f, 0f, 3f};
        this.eye = new float[] {0f, 0f, 3f};
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
		GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glLineWidth(10.0f);

        String vertexShaderCode = Utils.readRawTextFile(context, R.raw.vertex_shader);
        String fragmentShaderCode = Utils.readRawTextFile(context, R.raw.fragment_shader);
        program = new Program(vertexShaderCode, fragmentShaderCode);
        sphere = new Sphere(context);
        cube = new Cube(context);
        ray = new Ray(MatrixUtils.mul(MatrixUtils.randomVector(3), 0.5f),
                MatrixUtils.randomVector(3), context);
    }
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        aspect = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -aspect, aspect, -1, 1, 1, 10);

        program.useProgram();
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        float radius = 5.0f;
        float rad = (float)Math.toRadians(mAngle);
        eye = new float[] {
            (float)(radius * Math.sin(rad)),
            0f,
            (float)(radius * Math.cos(rad))
        };
        Matrix.setLookAtM(viewMatrix, 0, eye[0], eye[1], eye[2],
                0f, 0f, 0f,
                0f, 1.0f, 0.0f);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        program.draw(sphere);
//        program.draw(cube);
        program.draw(ray);
        List<GraphicObject.Intersection> intersections = sphere.getIntersectionsWithRay(ray);
        for(GraphicObject.Intersection intersection : intersections){
            if (intersection.position!= null){
                program.draw(new Point(intersection.position, context));
            }
        }
    }
}
