package com.example.computergraphics.renderer;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.computergraphics.R;
import com.example.computergraphics.object.GroupedObjects;
import com.example.computergraphics.object.Triangle;
import com.example.computergraphics.utils.MatrixUtils;
import com.example.computergraphics.utils.Utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class InstancedObjectRenderer implements GLSurfaceView.Renderer {
    private Context context;
    private int program;
    private GroupedObjects groupedObjects;
    private float[] vpMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float [16];
    private float[] rotationMatrix = new float[16];
    public float[] eye = { 0.0f, 2.0f, 3.0f };
    private int COPIES = 100;
    public InstancedObjectRenderer(Context context) {
        // Constructor
        this.context = context;
    }
    public volatile float mAngle;
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
//		GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glLineWidth(10.0f);

        program = GLES30.glCreateProgram();
        String instancedVertexShaderCode =
                Utils.readRawTextFile(context, R.raw.instanced_vertex_shader);
        String fragmentShaderCode =
                Utils.readRawTextFile(context, R.raw.fragment_shader);
        initProgram(program, instancedVertexShaderCode, fragmentShaderCode);

        float[] instancedModelMatrices = new float[COPIES * 16];
        for(int i=0; i<COPIES; i++){
            float[] translation = MatrixUtils.add(
                MatrixUtils.mul(
                    MatrixUtils.generateRandomVector(3),
                    2
                ), new float[] {-1f, -1f, -1f}
            );
            float[] scale = new float[] {0.1f, 0.1f, 0.1f};
            float[] rotation = MatrixUtils.mul(MatrixUtils.generateRandomVector(3), 360f);
            float[] modelMatrix = Utils.getModelMatrix(translation, rotation, scale);
            System.arraycopy(modelMatrix, 0, instancedModelMatrices, i * 16, 16);
        }
        groupedObjects = new GroupedObjects(
            Triangle.defaultVertexData,
            Triangle.defaultNormalData,
            null,
            program,
            context,
            COPIES,
            instancedModelMatrices
        );
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        Matrix.setLookAtM(viewMatrix, 0, eye[0], eye[1], eye[2], 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.setRotateM(rotationMatrix, 0, mAngle, 0f, 1.0f, 0.0f);

        groupedObjects.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
    }

    private void initProgram(int program, String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = CollisionDetectionRenderer.loadShader(GLES30.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = CollisionDetectionRenderer.loadShader(GLES30.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        // create empty OpenGL ES Program
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
    }
    public float getAngle() {
        return this.mAngle;
    }
    public void setAngle(float angle) {
        this.mAngle = angle;
    }
}
