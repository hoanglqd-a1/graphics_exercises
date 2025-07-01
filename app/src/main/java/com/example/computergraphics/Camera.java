package com.example.computergraphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.nfc.Tag;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.example.computergraphics.object.*;
import com.example.computergraphics.object.GraphicObject.Intersection;
import com.example.computergraphics.utils.ArgbColor;
import com.example.computergraphics.utils.MaterialFileHandle;
import com.example.computergraphics.utils.MatrixUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Camera {
    final float FOV = 90;
    public GraphicObject screen;
    Bitmap bitmap;
    int[] pixels;
    final int pixelX, pixelY;
    float aspect;
    float[] eye;
    float[] forward, right, up;
    float[] lightSource;
    Line[] lines;
    List<GraphicObject> objectList;
    final float[] BACKGROUND_COLOR;
    final float[] SCREEN_SCALE;
    final int MAXIMUM_REFLECTION;
    Context context;
    public Camera(int pixelX, int pixelY,
                  List<GraphicObject> objectList,
                  float[] eye,
                  float[] forward,
                  float[] right,
                  float[] up,
                  float aspect,
                  int MAXIMUM_REFLECTION,
                  float[] BACKGROUND_COLOR,
                  float[] lightSource,
                  Context context
    ){
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        this.objectList = objectList;
        this.eye = eye;
        this.forward = forward;
        this.right = right;
        this.up = up;
        this.aspect = aspect;
        this.BACKGROUND_COLOR = BACKGROUND_COLOR;
        this.MAXIMUM_REFLECTION = MAXIMUM_REFLECTION;
        this.lightSource = lightSource;
        this.SCREEN_SCALE = new float[] {aspect * 2, 2f, 1f};
        this.context = context;
        lines = new Line[pixelX * pixelY];
        pixels = new int[pixelX * pixelY];
        initScreen();
    }
    void initScreen(){
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
        screen.setScale(SCREEN_SCALE);
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
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
        mtl.textureHandle = textureHandle[0];
    }
    float[] rayTracing(Line line, int reflectCount, float refractiveIndex){
        Intersection nearest = getFirstIntersection(line);
        if(nearest == null) return BACKGROUND_COLOR;
        if(isShadow(nearest.position, nearest.normal)) return ArgbColor.black;
        float[] position = nearest.position;
        float[] normal = nearest.normal;
        float[] lightVector = MatrixUtils.normalize(
                MatrixUtils.sub(position, lightSource)
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
                if(intersection.position != null){
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
                float t1 = (o1.position[axis] - source[axis]) / direction[axis];
                float t2 = (o2.position[axis] - source[axis]) / direction[axis];
                return Float.compare(t1, t2);
            }
            return 0;
        });
        return intersectionList.get(0);
    }
    boolean isShadow(float[] position, float[] normal){
        final float epsilon = 1e-6f;
        float[] shiftedPosition = MatrixUtils.add(position, MatrixUtils.mul(normal, epsilon));
        Line reverseLightRay = new Line(shiftedPosition, lightSource, context);
        float[] reverseDirection = reverseLightRay.direction;
        float dist2Light = distFromSource2Dest(position, reverseDirection, lightSource);
        for (GraphicObject object : objectList){
            List<Intersection> intersections = object.getIntersectionsWithLine(reverseLightRay);
            for(Intersection intersection : intersections){
                if(intersection.position != null &&
                    MatrixUtils.norm(
                        MatrixUtils.sub(position, intersection.position)
                    ) > 1e-2f &&
                    distFromSource2Dest(position, reverseDirection, intersection.position) < dist2Light) return true;
            }
        }
        return false;
    }
    public static float[] reflect(float[] v, float[] normal){
        float[] n = MatrixUtils.normalize(normal);
        float dot = MatrixUtils.dot(v, n);
        float[] scaledNormal = MatrixUtils.mul(n, 2 * dot);
        float[] reflected = MatrixUtils.sub(v, scaledNormal);
        return reflected;
    }
    public static float distFromSource2Dest(float[] source, float[] direction, float[] dest){
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
