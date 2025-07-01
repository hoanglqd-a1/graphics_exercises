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

import com.example.computergraphics.R;
import com.example.computergraphics.object.Cube;
import com.example.computergraphics.object.GraphicObject;
import com.example.computergraphics.object.Line;
import com.example.computergraphics.object.Point;
import com.example.computergraphics.object.Sphere;
import com.example.computergraphics.object.Triangle;
import com.example.computergraphics.utils.ArgbColor;
import com.example.computergraphics.utils.MaterialFileHandle;
import com.example.computergraphics.utils.MatrixUtils;
import com.example.computergraphics.utils.Utils;
import com.example.computergraphics.object.GraphicObject.Intersection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BasicRayTracingRenderer extends BaseRenderer {
    int width, height;
    float[] rotationMatrix;
    float[] lookAt = {0f, 0f, 0f};
    float[] upDirection = {0f, 1f, 0f};
    float aspect;
    Program program;
    String TAG = "RAYTRACING";
    GraphicObject object;
    int SPHERE_NUM = 2;
    Sphere ground;
    Camera camera;
    List<GraphicObject> objectList = new ArrayList<>();
    float[] tempLightsSource = {0f, 3f, 5f};
    float AIR_REFRACTIVE_INDEX = 1f;
    float[] fullyReflective = {0f, 1f, 0f};
    float[] fullyRefractive = {0f, 0f, 1f};
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
        Sphere s1 = new Sphere(new float[] {-0.5f, 0f, 0f}, 0.5f, context);
        Sphere s2 = new Sphere(new float[] {0.5f, 0f, 0f}, 0.5f, context);
        Sphere s3 = new Sphere(new float[] {0f, 0f, 1f}, 0.5f, context);
        s1.colorCoefficient = fullyReflective;
        s3.colorCoefficient = fullyRefractive;
        objectList.add(s1);
        objectList.add(s2);
//        objectList.add(s3);
        ground.id = 1;
//        s3.id = 2;
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
        program.useProgram();
        camera = new Camera(512, 1024, objectList);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);

//        eye = Utils.rotateVec3Yaxis(eye, mAngle);
//        Matrix.setLookAtM(viewMatrix, 0,
//                eye[0], eye[1], eye[2],
//                lookAt[0], lookAt[1], lookAt[2],
//                upDirection[0], upDirection[1], upDirection[2]);
//        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.orthoM(projectionMatrix, 0, -aspect, aspect, -1f, 1f, 0f, 1f);
        Matrix.setIdentityM(viewMatrix, 0);
        Matrix.multiplyMM(vpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        program.draw(camera.screen);
    }
    public class Camera {
        float FOV = 90;
        Bitmap bitmap;
        int[] pixels;
        GraphicObject screen;
        final int pixelX, pixelY;
        Line[] lines;
        float[] forward, right, up;
        List<GraphicObject> objectList;
        final float[] BACKGROUND_COLOR = ArgbColor.lightBlue;
        final int MAXIMUM_REFLECTION = 3;
        public Camera(int pixelX, int pixelY, List<GraphicObject> objectList){
            this.pixelX = pixelX;
            this.pixelY = pixelY;
            this.objectList = objectList;
            lines = new Line[pixelX * pixelY];
            forward = MatrixUtils.normalize(MatrixUtils.sub(lookAt, eye));
            right = MatrixUtils.crossProduct(forward, upDirection);
            up = new float[] {0f, 1f, 0f};
            pixels = new int[pixelX * pixelY];
            float tanFov = (float) Math.tan(Math.toRadians(FOV * 0.5));
            for(int i=0; i<pixelY; i++){
                for(int j=0; j<pixelX; j++){
                    float ndc_x = (float) (j + 0.5) / pixelX;
                    float ndc_y = (float) (i + 0.5) / pixelY ;
                    float screen_x = 2 * ndc_x - 1;
                    float screen_y = 2 * ndc_y - 1;
                    float[] rayDirecion = MatrixUtils.add(
                        forward,
                        MatrixUtils.add(
                            MatrixUtils.mul(right, screen_x * tanFov * aspect),
                            MatrixUtils.mul(up, -screen_y * tanFov)
                        )
                    );
                    rayDirecion = MatrixUtils.normalize(rayDirecion);
                    lines[i * pixelX + j] = new Line(eye, rayDirecion, -1, context);
                }
            }
            screen = new Screen(context);
            screen.setScale(new float[] {aspect * 2, 2f, 1f});
            for(int i=0; i<pixelY; i++){
                for(int j=0; j<pixelX; j++){
                    Line line = lines[i * pixelX + j];
                    float[] pixelColor = rayTracing(line, MAXIMUM_REFLECTION, 1f);
                    int a = Math.round(pixelColor[3] * 255);
                    int r = Math.round(pixelColor[0] * 255);
                    int g = Math.round(pixelColor[1] * 255);
                    int b = Math.round(pixelColor[2] * 255);
                    pixels[i * pixelX + j] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
            bitmap = Bitmap.createBitmap(pixels, pixelX, pixelY, Bitmap.Config.ARGB_8888);
            MaterialFileHandle mtl = screen.mtl;
            final int[] textureHandle = new int[1];
            GLES30.glGenTextures(1, textureHandle, 0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);
            // Set filtering
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
            mtl.textureHandle = textureHandle[0];
        }
        float[] rayTracing(Line line, int reflectCount, float refractiveIndex){
            final float epsilon = 1e-6f;
            Intersection nearest = getFirstIntersection(line);
            if(nearest == null) return BACKGROUND_COLOR;
            if(isShadow(nearest.coord, nearest.normal)) return ArgbColor.black;
            float[] position = nearest.coord;
            float[] normal = nearest.normal;
            float[] lightVector = MatrixUtils.normalize(
                MatrixUtils.sub(position, tempLightsSource)
            );
            float[] viewVector = MatrixUtils.normalize(
                MatrixUtils.sub(position, eye)
            );
            float[] reflectedVector = reflect(
                viewVector, normal
            );
            float diffuse = Math.max(- MatrixUtils.dot(normal, lightVector), 0.0f);
            float[] localColor = MatrixUtils.mul(nearest.object.getColor(), diffuse);
            if(reflectCount == 0) return localColor;
            float[] reflectColor = ArgbColor.black;
            if (nearest.object.colorCoefficient[1] > 0.1) {
                reflectColor = rayTracing(
                    new Line(position, reflectedVector, 100, context),
                    reflectCount - 1, refractiveIndex);
            }
//            float[] refractedVector = getRefractedVector(
//                viewVector,
//                normal,
//                refractiveIndex,
//                nearest.object.refractionIndex
//            );
//            if (nearest.object.id == 1){
//                Log.d(TAG, "reflect count " + reflectCount);
//            }
//            float[] refractedVector = line.getDirection();
            float[] refractColor = ArgbColor.black;
//            if (refractedVector != null && nearest.object.colorCoefficient[2] > 0.1f){
//                float[] shiftedPosition = MatrixUtils.add(position, MatrixUtils.mul(refractedVector, epsilon));
//                refractColor = rayTracing(
//                    new Line(shiftedPosition, refractedVector, 100, context),
//                    reflectCount - 1,
//                    nearest.object.refractionIndex
//                );
//            }
            float[] colorCoefficient = nearest.object.colorCoefficient;
            return MatrixUtils.add(
                MatrixUtils.mul(localColor,   colorCoefficient[0]),
                MatrixUtils.mul(reflectColor, colorCoefficient[1]),
                MatrixUtils.mul(refractColor, colorCoefficient[2])
            );
        }
        Intersection getFirstIntersection (Line line){
            float[] source = line.source;
            float[] direction = line.direction;
            List<Intersection> intersectionList = new ArrayList<>();
            for(GraphicObject object : objectList){
                List<Intersection> intersections =
                        object.getIntersectionsWithLine(line);
                for(Intersection intersection: intersections){
                    if(intersection.coord != null){
                        intersectionList.add(intersection);
                    }
                }
            }
            if (intersectionList.isEmpty()){
                return null;
            }
            intersectionList.sort((o1, o2) -> {
                for(int axis=0; axis<3; axis++){
                    if(direction[axis] == 0) continue;
                    float t1 = (o1.coord[axis] - source[axis]) / direction[axis];
                    float t2 = (o2.coord[axis] - source[axis]) / direction[axis];
                    return Float.compare(t1, t2);
                }
                return 0;
            });
            return intersectionList.get(0);
        }
        boolean isShadow(float[] position, float[] normal){
            final float epsilon = 1e-6f;
            float[] shiftedPosition = MatrixUtils.add(position, MatrixUtils.mul(normal, epsilon));
            Line reverseLightRay = new Line(shiftedPosition, tempLightsSource, context);
            float[] reverseDirection = reverseLightRay.direction;
            float dist2Light = distFromSource2Dest(position, reverseDirection, tempLightsSource);
            for (GraphicObject object : objectList){
                List<Intersection> intersections = object.getIntersectionsWithLine(reverseLightRay);
                for(Intersection intersection : intersections){
                    if(intersection.coord != null &&
                    MatrixUtils.norm(
                        MatrixUtils.sub(position, intersection.coord)
                    ) > 1e-2f &&
                    distFromSource2Dest(position, reverseDirection, intersection.coord) < dist2Light) return true;
                }
            }
            return false;
        }
        public float[] reflect(float[] v, float[] normal){
            float[] n = MatrixUtils.normalize(normal);
            float dot = MatrixUtils.dot(v, n);
            float[] scaledNormal = MatrixUtils.mul(n, 2 * dot);
            float[] reflected = MatrixUtils.sub(v, scaledNormal);
            return reflected;
        }
        public float distFromSource2Dest(float[] source, float[] direction, float[] dest){
            for(int axis=0; axis<3; axis++){
                if (direction[axis] != 0){
                    return (dest[axis] - source[axis]) / direction[axis];
                }
            }
            return 0.0f;
        }
        public float[] getRefractedVector(float[] incidenceVector, float[] normal, float leavingRefraction, float enteringRefraction){
            float n = leavingRefraction / enteringRefraction;
            float c1 = - MatrixUtils.dot(incidenceVector, normal);
            if (1 - n*n*(1 - c1*c1) < 0){
                return null;
            }
            float c2 = (float) Math.sqrt(1 - n*n*(1 - c1*c1));
            float[] T = MatrixUtils.add(
                MatrixUtils.mul(incidenceVector, n),
                MatrixUtils.mul(normal, n * c1 - c2)
            );
            return T;
        }
    }
}
class Screen extends GraphicObject{
    public float[] color = {1f, 1f, 1f, 1f};
    static float[] defaultVertexData = {
        -0.5f,  0.5f, 0f,   // top left front
        -0.5f, -0.5f, 0f,   // bottom left front
        0.5f, -0.5f, 0f,   // bottom right front
        -0.5f,  0.5f, 0f,   // top left front
        0.5f, -0.5f, 0f,   // bottom right front
        0.5f,  0.5f, 0f,   // top right front
    };
    static float[] defaultNormalData = {
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
        0.0f, 0.0f, 1.0f,
    };
    static float[] defaultTextureData = {
//        0.0f, 1.0f, // top left of quad (-0.5f,  0.5f, 0.5f) gets top-left of texture
//        0.0f, 0.0f, // bottom left of quad (-0.5f, -0.5f, 0.5f) gets bottom-left of texture
//        1.0f, 0.0f, // bottom right of quad (0.5f, -0.5f, 0.5f) gets bottom-right of texture
//        0.0f, 1.0f, // top left of quad (-0.5f,  0.5f, 0.5f) gets top-left of texture
//        1.0f, 0.0f, // bottom right of quad (0.5f, -0.5f, 0.5f) gets bottom-right of texture
//        1.0f, 1.0f, // top right of quad (0.5f,  0.5f, 0.5f) gets top-right of texture
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 1.0f,
        1.0f, 0.0f,
    };
    public Screen(Context context){
        super(defaultVertexData, defaultNormalData, defaultTextureData, context);
    }
    public float[] getColor() {
        return color;
    }
    public void setColor(float[] color){
        this.color = color;
    }
}
