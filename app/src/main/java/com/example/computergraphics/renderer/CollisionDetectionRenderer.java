package com.example.computergraphics.renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.res.AssetManager;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.computergraphics.CollisionDetection.Grid;
import com.example.computergraphics.CollisionDetection.KDTree;
import com.example.computergraphics.object.*;
import com.example.computergraphics.test_spatial_structure.*;
import com.example.computergraphics.utils.MatrixUtils;
import com.example.computergraphics.utils.Utils;
import com.example.computergraphics.R;

public class CollisionDetectionRenderer extends BaseRenderer {
    float[] userInputLineSource;
    float[] userInputLineDirection;
    private GraphicObject object;
    private Cube cube;
    private GraphicObject model;
    private Line line;
    private Cube [] cubes;
    private Line [] lines;
    private List<GraphicObject> triangles;
    private GroupedObjects groupedObjects;
    private Grid grid;
    private KDTree kdtree;

    public CollisionDetectionRenderer(float[] source, float[] direction, Context context) {
        // Constructor
        super.context = context;
        this.userInputLineSource = source;
        this.userInputLineDirection = direction;
    }
    private long naive_total = 0;
    private long grid_total = 0;
    private long kdtree_total = 0;
    private int count = 0;
    private int commonProgram;
    private int instancedProgram;
    private Program program;
    static int NUM_OF_LINES = 10;
    static int NUM_OF_TRIANGLES = 10;
    static int defaultLineLength = 40;
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
//		GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glLineWidth(10.0f);
//
        String vertexShaderCode = Utils.readRawTextFile(context, R.raw.vertex_shader);
        String fragmentShaderCode = Utils.readRawTextFile(context, R.raw.fragment_shader);
        program = new Program(vertexShaderCode, fragmentShaderCode);

        InputStream inputStream = null;
        try {
            AssetManager assetManager = context.getAssets();
            inputStream = assetManager.open("pokeball.obj");
            model = new GraphicObject(inputStream, context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Random random = new Random();
        cubes = new Cube[10];
        lines = new Line[NUM_OF_LINES];
        for(int i=0; i<cubes.length; i++){
            cubes[i] = new Cube(context);
            cubes[i].setTranslation(
                new float[] {
                    2 * random.nextFloat() - 1f,
                    2 * random.nextFloat() - 1f,
                    2 * random.nextFloat() - 1f,
                }
            );
        }
        for (int i=0; i<lines.length; i++){
            float[] source = new float[] {
                2 * random.nextFloat() - 1f,
                2 * random.nextFloat() - 1f,
                2 * random.nextFloat() - 1f,
            };
            float[] direction = new float[] {
                2 * random.nextFloat() - 1f,
                2 * random.nextFloat() - 1f,
                2 * random.nextFloat() - 1f,
            };
            lines[i] = new Line(source, direction, defaultLineLength, context);
        }
        triangles = new ArrayList<>();
        for(int i=0; i<NUM_OF_TRIANGLES; i++){
            Triangle triangle = new Triangle(context);
            float[] translation = MatrixUtils.add(
                MatrixUtils.mul(
                    MatrixUtils.randomVector(3),
                    4
                ), new float[] {-2f, -2f, -2f}
            );
            float[] scale = new float[] {1f, 1f, 1f};
            float[] rotation = MatrixUtils.mul(MatrixUtils.randomVector(3), 360f);
            triangle.setTranslation(translation);
            triangle.setRotation(rotation);
            triangle.setScale(scale);
            triangles.add(triangle);
        }
        grid = new Grid(triangles);
        kdtree = new KDTree(triangles);

//        testCollisionDetection();
    }
    private long lastTime = System.nanoTime();
    private int frames;
    public void onDrawFrame(GL10 unused) {
        frames ++;
        long currentTime = System.nanoTime();
        if (currentTime - lastTime > 1_000_000_000L){
            lastTime = currentTime;
            Log.d("FPS", "FPS " + frames);
            frames = 0;
        }

        // Redraw background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        Matrix.setLookAtM(viewMatrix, 0, eye[0], eye[1], eye[2], 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.setRotateM(rotationMatrix, 0, mAngle, 0f, 1.0f, 0.0f);
        GLES30.glUseProgram(program.program);

        for(Line l : lines){
            program.draw(l);
        }
        for(int i=0; i<triangles.size(); i++){
            program.draw(triangles.get(i));
        }
        long start = System.nanoTime();
        for (Line line : lines) {
            for (int i = 0; i < triangles.size(); i++) {
                List<GraphicObject.Intersection> intersectionList = triangles.get(i).getIntersectionsWithLine(line);
                for (GraphicObject.Intersection intersection : intersectionList) {
                    if (intersection.point != null) {
                        Point p = intersection.point;
                        program.draw(p);
                    } else if (intersection.line != null) {
                        Line l = intersection.line;
                        program.draw(l);
                    }
                }
            }
        }
        long end = System.nanoTime();
        naive_total += end - start;
        start = System.nanoTime();
        for(Line line : lines){
            List<GraphicObject.Intersection> intersectionList = grid.getIntersectionWithLine(line);
            for(GraphicObject.Intersection intersection : intersectionList){
                if(intersection.point != null){
                    Point p = intersection.point;
//                    program.draw(p);
                }
                else if(intersection.line != null){
                    Line l = intersection.line;
//                    program.draw(l);
                }
            }
        }
        end = System.nanoTime();
        grid_total += end - start;
        start = System.nanoTime();
        for(Line line: lines){
            List<GraphicObject.Intersection> intersectionList = kdtree.getIntersectionWithLine(line);
            for(GraphicObject.Intersection intersection : intersectionList){
                if(intersection.point != null){
                    Point p = intersection.point;
//                    program.draw(p);
                }
                else if(intersection.line != null){
                    Line l = intersection.line;
//                    program.draw(l);
                }
            }
        }
        end = System.nanoTime();
        kdtree_total += end - start;
        count ++;
        if (count == 10){
            Log.d("Experiment", "Naive approach's average execution time " + naive_total / 10_000_000.0 + " ms");
            Log.d("Experiment", "Grid-based approach execution time average " + grid_total / 10_000_000.0 + " ms");
            Log.d("Experiment", "kD-Tree approach's average execution time: " + kdtree_total / 10_000_000.0 + " ms");
            count = 0;
            naive_total = 0;
            grid_total = 0;
            kdtree_total = 0;
        }
    }
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
    }
    private void initProgram(int program, String vertexShaderCode, String fragmentShaderCode) {
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
    }
    private void testCollisionDetection(){
        try {
            Main.main(null, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}