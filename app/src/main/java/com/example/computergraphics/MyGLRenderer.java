package com.example.computergraphics;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.util.Log;

import com.example.computergraphics.CollisionDetection.Grid;
import com.example.computergraphics.CollisionDetection.KDTree;
import com.example.computergraphics.object.*;
import com.example.computergraphics.utils.MatrixUtils;
import com.example.computergraphics.utils.Utility;
import com.example.computergraphics.test_spatial_structure.*;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Context context;
    private float [] userInputLineSource;
    private float [] userInputLineDirection;
    private GraphicObject object;
    private Cube cube;
    private GraphicObject model;
    private Line line;
    private Cube [] cubes;
    private Line [] lines;
    private List<GraphicObject> triangles;
    private GroupedGraphicObjects groupedObjects;
    private Grid grid;
    private KDTree kdtree;
    private float[] vpMatrix = new float[16];
    private float[] projectionMatrix = new float[16];
    private float[] viewMatrix = new float [16];
    private float[] rotationMatrix = new float[16];
    private float[] lightModelMatrix = new float[16];
    public float[] eye = { 0.0f, 2.0f, 3.0f };
    MyGLRenderer(float [] source, float [] direction, Context context) {
        // Constructor
        this.context = context;
        this.userInputLineSource = source;
        this.userInputLineDirection = direction;
    }
    private long naive_total = 0;
    private long grid_total = 0;
    private long kdtree_total = 0;
    private int count = 0;
    private int commonProgram;
    private int instancedProgram;
    static int NUM_OF_LINES = 1;
    static int NUM_OF_TRIANGLES = 1;
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
//        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
//        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
////		GLES30.glEnable(GLES30.GL_CULL_FACE);
//        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
//        GLES30.glLineWidth(10.0f);
//
//        commonProgram = GLES30.glCreateProgram();
//        instancedProgram = GLES30.glCreateProgram();
//        String vertexShaderCode = Utility.readRawTextFile(context, R.raw.vertex_shader);
//        String fragmentShaderCode = Utility.readRawTextFile(context, R.raw.fragment_shader);
//        String instancedVertexShaderCode = Utility.readRawTextFile(context, R.raw.instanced_vertex_shader);
//        this.initProgram(commonProgram, vertexShaderCode, fragmentShaderCode);
//        this.initProgram(instancedProgram, instancedVertexShaderCode, fragmentShaderCode);

//        object = new GraphicObject(this.context);
//        cube = new Cube(context);
//        line = new Line(userInputLineSource, userInputLineDirection, -1, context);
//        try {
//            AssetManager assetManager = this.context.getAssets();
//            InputStream objInputStream = assetManager.open("capsule.obj");
//            model = new GraphicObject(objInputStream, this.context);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        Random random = new Random();
//        cubes = new Cube[10];
//        lines = new Line[NUM_OF_LINES];
//        for(int i=0; i<cubes.length; i++){
//            cubes[i] = new Cube(context);
//            cubes[i].setTranslation(
//                new float[] {
//                    2 * random.nextFloat() - 1f,
//                    2 * random.nextFloat() - 1f,
//                    2 * random.nextFloat() - 1f,
//                }
//            );
//        }
//        for (int i=0; i<lines.length; i++){
//            float[] source = new float[] {
//                2 * random.nextFloat() - 1f,
//                2 * random.nextFloat() - 1f,
//                2 * random.nextFloat() - 1f,
//            };
//            float[] direction = new float[] {
//                2 * random.nextFloat() - 1f,
//                2 * random.nextFloat() - 1f,
//                2 * random.nextFloat() - 1f,
//            };
//            lines[i] = new Line(source, direction, -1, context);
//        }
//        int copies = 100;
//        float[] instancedModelMatrices = new float[copies * 16];
//        for(int i=0; i<copies; i++){
//            float[] translation = MatrixUtils.add(
//                MatrixUtils.mul(
//                    MatrixUtils.generateRandomVector(3),
//                    2
//                ), new float[] {-1f, -1f, -1f}
//            );
//            float[] scale = new float[] {0.1f, 0.1f, 0.1f};
//            float[] rotation = MatrixUtils.mul(MatrixUtils.generateRandomVector(3), 360f);
//            float[] modelMatrix = Utility.getModelMatrix(translation, rotation, scale);
//            System.arraycopy(modelMatrix, 0, instancedModelMatrices, i * 16, 16);
//        }
//        groupedObjects = new GroupedGraphicObjects(context, copies, instancedModelMatrices);
//        triangles = new ArrayList<>();
//        long start = System.nanoTime();
//        for(int i=0; i<NUM_OF_TRIANGLES; i++){
//            GraphicObject triangle = new GraphicObject(commonProgram, context);
//            float[] translation = MatrixUtils.add(
//                MatrixUtils.mul(
//                    MatrixUtils.generateRandomVector(3),
//                    4
//                ), new float[] {-2f, -2f, -2f}
//            );
//            float[] scale = new float[] {0.01f, 0.01f, 0.01f};
//            float[] rotation = MatrixUtils.mul(MatrixUtils.generateRandomVector(3), 360f);
//            triangle.setTranslation(translation);
//            triangle.setRotation(rotation);
//            triangle.setScale(scale);
//            triangles.add(triangle);
//        }
//        long end = System.nanoTime();
//        Log.d("Initialization", "Triangle init exec time: " + (end - start) / 1_000_000.0 + " ms");
//        start = System.nanoTime();
//        grid = new Grid(triangles);
//        end = System.nanoTime();
//        Log.d("Initialization", "Grid init exec time: " + (end - start) / 1_000_000.0 + " ms");
//        start = System.nanoTime();
//        kdtree = new KDTree(triangles);
//        end = System.nanoTime();
//        Log.d("Initialization", "KDTree init exec time: " + (end - start) / 1_000_000.0 + " ms");
        try {
            Main.main(null, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private long lastTime = System.nanoTime();
    private int frames;
    public void onDrawFrame(GL10 unused) {
//        frames ++;
//        long currentTime = System.nanoTime();
//        if (currentTime - lastTime > 1_000_000_000L){
//            lastTime = currentTime;
//            Log.d("FPS", "FPS " + frames);
//            frames = 0;
//        }
//
//        // Redraw background color
//        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
//        Matrix.setLookAtM(viewMatrix, 0, eye[0], eye[1], eye[2], 0f, 0f, 0f, 0f, 1.0f, 0.0f);
//        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
//        Matrix.setRotateM(rotationMatrix, 0, mAngle, 0f, 1.0f, 0.0f);
//
//        float [] Identity = new float[16];
//        Matrix.setIdentityM(Identity, 0);
//
//
////        object.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////        cube.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////        model.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////        line.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////        List<float[][]> intersectionsList = cube.getIntersectionsWithLine(line);
////        for (float [][] intersections : intersectionsList){
////            if(intersections.length == 1){
////                Point p = new Point(intersections[0], context);
////                p.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////            }
////            else if(intersections.length == 2){
////                Line l = new Line(intersections[0], intersections[1], context);
////                l.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////            }
////        }
////        for(Cube c : cubes){
////            c.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////        }
////        for(Line l : lines){
////            l.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////        }
////        for(int i=0; i<cubes.length; i++){
////            for(int j=0; j<lines.length; j++){
////                List<float[][]> intersectionList = cubes[i].getIntersectionsWithLine(lines[j]);
////                for(float[][] intersections : intersectionList){
////                    if(intersections.length == 1){
////                        Point p = new Point(intersections[0], context);
////                        p.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////                    }
////                    else if(intersections.length == 2){
////                        Line l = new Line(intersections[0], intersections[1], context);
////                        l.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////                    }
////                }
////            }
////        }
////        groupedObjects.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
//
////        for(int i=0; i<triangles.size(); i++){
////            triangles.get(i).draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////        }
//        List<float[][]> groundTruth = new ArrayList<>();
//        long start = System.nanoTime();
//        for (Line line : lines) {
//            for (int i = 0; i < triangles.size(); i++) {
//                List<float[][]> intersectionList = triangles.get(i).getIntersectionsWithLine(line);
//                groundTruth.addAll(intersectionList);
////                for (float[][] intersections : intersectionList) {
////                    if (intersections.length == 1) {
////                        Point p = new Point(intersections[0], context);
////                        p.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
//////                        Log.d("collision detection", "Naive " + Arrays.toString(intersections[0]));
////                    } else if (intersections.length == 2) {
////                        Line l = new Line(intersections[0], intersections[1], context);
////                        l.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////                    }
////                }
//            }
//        }
////        Log.d("Ground truth", "Number of interstections: " + groundTruth.size());
//        long end = System.nanoTime();
////        Log.d("Experiment", "Naive approach execution time " + (end - start) / 1_000_000.0 + " ms");
//        naive_total += end - start;
////        groundTruth.sort((a, b) -> {
////            int cmpX = Float.compare(a[0][0], b[0][0]);
////            if (cmpX != 0) return cmpX;
////            int cmpY = Float.compare(a[0][1], b[0][1]);
////            if (cmpY != 0) return cmpY;
////            return Float.compare(a[0][2], b[0][2]);
////        });
////        List<float[][]> prediction = new ArrayList<>();
//        start = System.nanoTime();
//        for(Line line : lines){
//            List<float[][]> intersectionList = grid.getIntersectionWithLine(line);
////            prediction.addAll(intersectionList);
////            for(float[][] intersections : intersectionList){
////                if(intersections.length == 1){
////                    Point p = new Point(intersections[0], context);
//////                    p.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
//////                    Log.d("collision detection", "Grid-based " + Arrays.toString(intersections[0]));
////
////                }
////                else if(intersections.length == 2){
////                    Line l = new Line(intersections[0], intersections[1], context);
//////                    l.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////                }
////            }
//        }
//        end = System.nanoTime();
////        Log.d("Experiment", "Grid-based approach execution time " + (end - start) / 1_000_000.0 + " ms");
//        grid_total += end - start;
////        prediction.sort((a, b) -> {
////            int cmpX = Float.compare(a[0][0], b[0][0]);
////            if (cmpX != 0) return cmpX;
////            int cmpY = Float.compare(a[0][1], b[0][1]);
////            if (cmpY != 0) return cmpY;
////            return Float.compare(a[0][2], b[0][2]);
////        });
////        float loss = 0f;
////        for(int i=0; i<prediction.size(); i++){
////            if(i>=groundTruth.size())
////                loss += Math.abs(prediction.get(i)[0][0]) + Math.abs(prediction.get(i)[0][1] + Math.abs(prediction.get(i)[0][2]));
////            else{
////                loss += Math.abs(prediction.get(i)[0][0] - groundTruth.get(i)[0][0])
////                        + Math.abs(prediction.get(i)[0][1] - groundTruth.get(i)[0][1])
////                        + Math.abs(prediction.get(i)[0][2] - groundTruth.get(i)[0][2]);
////            }
////        }
////        Log.d("Experiment", "Line count=" + lines.length);
////        Log.d("Experiment", "Triangle count=" + triangles.size());
////        Log.d("Experiment", "Loss=" + loss);
//        start = System.nanoTime();
//        for(Line line: lines){
//            List<float[][]> intersectionList = kdtree.getIntersectionWithLine(line);
////            for(float[][] intersections : intersectionList){
////                if(intersections.length == 1){
////                    Point p = new Point(intersections[0], context);
////                    p.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////                    Log.d("collision detection", "KDTree " + Arrays.toString(intersections[0]));
////                }
////                else if(intersections.length == 2){
////                    Line l = new Line(intersections[0], intersections[1], context);
////                    l.draw(viewMatrix, projectionMatrix, rotationMatrix, eye);
////                }
////            }
//        }
//        end = System.nanoTime();
//        kdtree_total += end - start;
//        count ++;
//        if (count == 10){
//            Log.d("Experiment", "Naive approach's average execution time " + naive_total / 10_000_000.0 + " ms");
//            Log.d("Experiment", "Grid-based approach execution time average " + grid_total / 10_000_000.0 + " ms");
//            Log.d("Experiment", "kD-Tree approach's average execution time: " + kdtree_total / 10_000_000.0 + " ms");
//            count = 0;
//            naive_total = 0;
//            grid_total = 0;
//            kdtree_total = 0;
//        }
    }
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
    }
    public static int loadShader(int type, String shaderCode){
        int shader = GLES30.glCreateShader(type);
        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        return shader;
    }
    public volatile float mAngle;
    public float getAngle() {
        return this.mAngle;
    }
    public void setAngle(float angle) {
        this.mAngle = angle;
    }
    private void initProgram(int program, String vertexShaderCode, String fragmentShaderCode) {
        int vertexShader = MyGLRenderer.loadShader(GLES30.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES30.GL_FRAGMENT_SHADER,
                fragmentShaderCode);
        // create empty OpenGL ES Program
        GLES30.glAttachShader(program, vertexShader);
        GLES30.glAttachShader(program, fragmentShader);
        GLES30.glLinkProgram(program);
    }
}