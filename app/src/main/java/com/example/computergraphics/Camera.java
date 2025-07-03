package com.example.computergraphics;

import android.content.Context;
import android.graphics.Bitmap;
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
    List<PointLight> lightSources;
    Ray[] rays;
    List<GraphicObject> objectList;
    final float[] BACKGROUND_COLOR;
    final float[] SCREEN_SCALE;
    final int MAXIMUM_REFLECTION;
    Context context;
    float EPSILON = 1e-4f;
    final String TAG = "Ray Tracing";
    public Camera(int pixelX, int pixelY,
                  List<GraphicObject> objectList,
                  float[] eye,
                  float[] forward,
                  float[] right,
                  float[] up,
                  float aspect,
                  int MAXIMUM_REFLECTION,
                  float[] BACKGROUND_COLOR,
                  List<PointLight> lightSources,
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
        this.lightSources = lightSources;
        this.SCREEN_SCALE = new float[] {aspect * 2, 2f, 1f};
        this.context = context;
        this.screen = new Screen(context);
        this.screen.setScale(SCREEN_SCALE);
        pixels = new int[pixelX * pixelY];
        initScreen();
    }
    void initScreen(){
        float tanFov = (float) Math.tan(Math.toRadians(FOV * 0.5));
        for(int i=0; i<pixelY; i++){
            for(int j=0; j<pixelX; j++){
                float ndc_x = (float) (j + 0.5) / pixelX;
                float ndc_y = (float) (i + 0.5) / pixelY;
                float screen_x = 2 * ndc_x - 1;
                float screen_y = 2 * ndc_y - 1;
                float[] rayDirection = MatrixUtils.add(
                    forward,
                    MatrixUtils.add(
                        MatrixUtils.mul(right, screen_x * tanFov * aspect),
                        MatrixUtils.mul(up, -screen_y * tanFov)
                    )
                );
                rayDirection = MatrixUtils.normalize(rayDirection);
                float[] pixelColor = rayTracing(eye, rayDirection, 0, 1f);
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
    float[] rayTracing(float[] raySource, float[] rayDirection, int rayReflectCount, float refractiveIndex){
        Ray ray = new Ray(
            shiftPosition(raySource, rayDirection, EPSILON),
            rayDirection,
            context
        );
        Intersection nearest = getFirstIntersection(ray);
        if(nearest == null) {
            return BACKGROUND_COLOR;
        }
        float[] colorCoefficient = nearest.object.colorCoefficient;
        float[] hitPosition = nearest.position;
        float[] hitNormal = nearest.normal;

        float[] reflectedDirection = reflect(
                rayDirection, hitNormal
        );
        float[] localColor = computeLocalColor(hitPosition, hitNormal, lightSources, nearest.object);
        if(rayReflectCount == MAXIMUM_REFLECTION) return localColor;
        float[] reflectColor = ArgbColor.black;
        if (colorCoefficient[1] > 0.1) {
            reflectColor = rayTracing(
                hitPosition, reflectedDirection,
                rayReflectCount + 1, refractiveIndex);
        }
//        float[] refractedVector = getRefractedVector(rayDirection, hitNormal, refractiveIndex, nearest.object.refractionIndex);
        float[] refractColor = ArgbColor.black;
//        if (refractedVector != null && colorCoefficient[2] > 0.1f){
//            refractColor = rayTracing(
//                hitPosition, refractedVector,
//                rayReflectCount - 1,
//                nearest.object.refractionIndex
//            );
//        }
        float[] finalColor = MatrixUtils.add(
                MatrixUtils.mul(localColor,   colorCoefficient[0]),
                MatrixUtils.mul(reflectColor, colorCoefficient[1]),
                MatrixUtils.mul(refractColor, colorCoefficient[2])
        );
        return finalColor;
    }
    Intersection getFirstIntersection (Ray ray){
        float[] source = ray.source;
        float[] direction = ray.direction;
        List<Intersection> intersectionList = new ArrayList<>();
        for(GraphicObject object : objectList){
            List<Intersection> intersections =
                object.getIntersectionsWithRay(ray);
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
    boolean isShadow(float[] position, float[] normal, PointLight lightSource){
        float[] shiftedPosition = MatrixUtils.add(position, MatrixUtils.mul(normal, EPSILON));
        Ray reverseLightRay = Ray.fromPoints(shiftedPosition, lightSource.position, context);
        float[] reverseDirection = reverseLightRay.direction;
        float dist2Light = distFromSource2Dest(position, reverseDirection, lightSource.position);
        for (GraphicObject object : objectList){
            List<Intersection> intersections = object.getIntersectionsWithRay(reverseLightRay);
            for(Intersection intersection : intersections){
                if(intersection.position != null &&
                    distFromSource2Dest(position, reverseDirection, intersection.position) < dist2Light) return true;
            }
        }
        return false;
    }
    public float[] computeLocalColor(float[] position, float[] normal, List<PointLight> lightSources, GraphicObject object){
        float[] localColor = new float[] {0f, 0f, 0f, 0f};
        for (PointLight lightSource: lightSources){
            if (isShadow(position, normal, lightSource)) continue;
            float[] lightVector = MatrixUtils.normalize(
                    MatrixUtils.sub(position, lightSource.position)
            );
            float cosine = Math.max(- MatrixUtils.dot(normal, lightVector), 0.0f);
            float[] fr = MatrixUtils.mul(object.getColor(), (float) (1/Math.PI));
//        float[] fr = object.getColor();
            localColor = MatrixUtils.add(
                localColor,
                MatrixUtils.mul(
                    MatrixUtils.mul(fr, lightSource.color),
                    cosine
                )
            );
        }
        return localColor;
    }
    public static float[] reflect(float[] v, float[] normal){
        float[] n = MatrixUtils.normalize(normal);
        float dot = MatrixUtils.dot(v, n);
        float[] scaledNormal = MatrixUtils.mul(n, 2 * dot);
        return MatrixUtils.sub(v, scaledNormal);
    }
    public static float distFromSource2Dest(float[] source, float[] direction, float[] dest){
        for(int axis=0; axis<3; axis++){
            if (direction[axis] != 0){
                return (dest[axis] - source[axis]) / direction[axis];
            }
        }
        return 0.0f;
    }
    public static float[] getRefractedVector(float[] incidenceVector, float[] normal, float leavingRefraction, float enteringRefraction){
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
    public static float[] shiftPosition(float[] position, float[] direction, float epsilon){
        return MatrixUtils.add(position, MatrixUtils.mul(direction, epsilon));
    }
}
