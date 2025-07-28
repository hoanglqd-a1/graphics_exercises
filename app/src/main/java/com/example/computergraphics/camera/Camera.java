package com.example.computergraphics.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.example.computergraphics.PointLight;
import com.example.computergraphics.object.*;
import com.example.computergraphics.object.GraphicObject.Intersection;
import com.example.computergraphics.utils.ArgbColor;
import com.example.computergraphics.utils.MaterialFileHandle;
import com.example.computergraphics.utils.MatrixUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Camera {
    final float FOVdegree = 90;
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
    final float AIR_REFRACTIVE_INDEX;
    final float MIN_TRANSMITTANCE;
    Context context;
    float EPSILON = 1e-4f;
    final String TAG = "Ray Tracing";
    final float fixStepSize = 0.1f;
    public Camera(int pixelX, int pixelY,
          List<GraphicObject> objectList,
          float[] eye,
          float[] forward,
          float[] right,
          float[] up,
          float aspect,
          int MAXIMUM_REFLECTION,
          float[] BACKGROUND_COLOR,
          float AIR_REFRACTIVE_INDEX,
          float MIN_TRANSMITTANCE,
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
        this.AIR_REFRACTIVE_INDEX = AIR_REFRACTIVE_INDEX;
        this.MIN_TRANSMITTANCE = MIN_TRANSMITTANCE;
        this.lightSources = lightSources;
        this.SCREEN_SCALE = new float[] {aspect * 2, 2f, 1f};
        this.context = context;
        this.screen = new Screen(context);
        this.screen.setScale(SCREEN_SCALE);
        pixels = new int[pixelX * pixelY];
        initScreen();
    }
    void initScreen(){
        int initRayReflectCount = 0;
        float initTransmission = 1f;

        float tanFov = (float) Math.tan(Math.toRadians(FOVdegree * 0.5));
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
                float[] pixelColor = rayTracing(
                    eye,
                    rayDirection,
                    initRayReflectCount,
                    AIR_REFRACTIVE_INDEX,
                    initTransmission
                );
                pixelColor = clampingColor(pixelColor);
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
    float[] rayTracing(
        float[] raySource,
        float[] rayDirection,
        int rayReflectCount,
        float refractiveIndex,
        float transmission
    ){
        if (rayReflectCount == MAXIMUM_REFLECTION){
            return ArgbColor.black;
        }
        Ray ray = new Ray(
            shiftPosition(raySource, rayDirection, EPSILON),
            rayDirection,
            context
        );
        Intersection nearest = getFirstIntersection(ray);
        if(nearest == null) {
            return BACKGROUND_COLOR;
        }
        GraphicObject hitObject = nearest.object;
        float[] colorCoefficient = hitObject.colorCoefficient;
        float[] hitPosition = nearest.position;
        float[] hitNormal = nearest.normal;
        if (hitObject.isVolume){
            Intersection secondHit = getFirstIntersection(
                new Ray(
                    shiftPosition(hitPosition, rayDirection, EPSILON),
                    rayDirection,
                    context
                )
            );
            float[] secondHitPosition = secondHit.position;
            float intervalLength= MatrixUtils.norm(
                MatrixUtils.sub(
                    secondHitPosition,
                    hitPosition
                )
            );
            return rayMarching(
                hitPosition,
                rayDirection,
                hitObject.getColor(),
                transmission,
                intervalLength,
                hitObject.volumeDensity,
                rayReflectCount,
                refractiveIndex
            );
        }
        float[] localColor = computeLocalColor(hitPosition, hitNormal, lightSources, nearest.object, rayReflectCount, refractiveIndex);
        float[] reflectedDirection = reflect(
                rayDirection, hitNormal
        );
        float[] reflectColor = ArgbColor.black;
        if (colorCoefficient[1] > 0.1) {
            reflectColor = rayTracing(
                hitPosition, reflectedDirection,
                rayReflectCount + 1, refractiveIndex, transmission);
        }
        boolean[] isRefracted = new boolean[] {true};
        float enteringRefractiveIndex;
        if (MatrixUtils.dot(rayDirection, hitNormal) < 0) {
            enteringRefractiveIndex = nearest.object.refractiveIndex;
        } else {
            enteringRefractiveIndex = AIR_REFRACTIVE_INDEX;
        }
        float[] refractedVector =
            getRefractedVector(rayDirection, hitNormal, refractiveIndex, enteringRefractiveIndex, isRefracted);
        if (!isRefracted[0]){
            enteringRefractiveIndex = refractiveIndex;
        }
        float[] refractColor = ArgbColor.black;
        if (refractedVector != null && colorCoefficient[2] > 0.1f){
            refractColor = rayTracing(
                hitPosition, refractedVector,
                rayReflectCount + 1,
                enteringRefractiveIndex, transmission
            );
        }
        float[] finalColor = MatrixUtils.add(
            MatrixUtils.mul(localColor,   colorCoefficient[0]),
            MatrixUtils.mul(reflectColor, colorCoefficient[1]),
            MatrixUtils.mul(refractColor, colorCoefficient[2])
        );
        finalColor = clampingColor(finalColor);
        return finalColor;
    }
    public float[] rayMarching(
        float[] rayOrigin,
        float[] rayDirection,
        float[] mediumColor,
        float accumulatedTransmittance,
        float distanceToExit,
        float density,
        int rayReflectCount,
        float refractiveIndex
    ){
        // If the accumulatedTransmittance is too low, stop ray marching
        if (accumulatedTransmittance <= MIN_TRANSMITTANCE){
            return ArgbColor.transparentBlack;
        }
        // If the ray exit the object, return to ray tracing
        if (distanceToExit <= 0){
            return rayTracing(rayOrigin, rayDirection, rayReflectCount, refractiveIndex, accumulatedTransmittance);
        }
        float stepSize = Math.min(distanceToExit, fixStepSize);
        float[] currentSamplePoint = MatrixUtils.add(
            rayOrigin,
            MatrixUtils.mul(rayDirection, 0.5f * stepSize)
        );
        float[] nextRayOrigin = MatrixUtils.add(
            rayOrigin,
            MatrixUtils.mul(rayDirection, stepSize)
        );
        float transmittanceOfCurrentSegment = (float)Math.exp(-stepSize * density);
        float opacityOfCurrentSegment = 1 - transmittanceOfCurrentSegment;
        float[] lightIn = ArgbColor.transparentBlack;
        for (PointLight lightSource: lightSources){
            lightIn = MatrixUtils.add(
                lightIn,
                getLightIn(
                    currentSamplePoint,
                    lightSource,
                    density,
                    mediumColor
                )
            );
        }
//        float[] lightIn = color;
        float[] lightOut = MatrixUtils.mul(
            lightIn, opacityOfCurrentSegment
        );
        accumulatedTransmittance *= transmittanceOfCurrentSegment;
        float[] lightFromBehind = rayMarching(
            nextRayOrigin,
            rayDirection,
            mediumColor,
            accumulatedTransmittance,
            distanceToExit - stepSize,
            density,
            rayReflectCount,
            refractiveIndex
        );

        return MatrixUtils.add(
            lightOut,
            MatrixUtils.mul(lightFromBehind, transmittanceOfCurrentSegment)
        );
//        return lightOut;
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
    public float[] computeLocalColor(float[] position, float[] normal, List<PointLight> lightSources, GraphicObject object, int rayReflectCount, float refractiveIndex){
        float[] localColor = Arrays.copyOf(ArgbColor.transparentBlack, 4);
        for (PointLight lightSource: lightSources){
//            float[][] reflectRays = LambertianRaySimulationGeneration(normal);
//            for (float[] rayDirection: reflectRays){
//                float _cosine = Math.max(-MatrixUtils.dot(normal, rayDirection), 0f);
//                float[] _fr = MatrixUtils.mul(object.getColor(), (float) (1/Math.PI));
//                localColor = MatrixUtils.add(
//                    localColor,
//                    MatrixUtils.mul(
//                        MatrixUtils.mul(
//                            rayTracing(position,
//                                rayDirection,
//                                rayReflectCount + 1,
//                                refractiveIndex),
//                            _fr
//                        ),
//                        _cosine
//                    )
//                );
//            }
            if (isShadow(position, normal, lightSource)) continue;
            float[] lightVector = MatrixUtils.normalize(
                    MatrixUtils.sub(position, lightSource.position)
            );
            float cosine = Math.max(- MatrixUtils.dot(normal, lightVector), 0.0f);
            float[] fr = MatrixUtils.mul(object.getColor(), (float) (1/Math.PI));
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
    public float[] getLightIn(
        float[] targetPosition,
        PointLight lightSource,
        float density,
        float[] color
    ){
        // Get the ray from target point to the light source
        float[] rayTargetPosToLightDirection = MatrixUtils.normalize(
            MatrixUtils.sub(lightSource.position, targetPosition)
        );
        Ray rayTargetPosToLight = new Ray(
            targetPosition,
            rayTargetPosToLightDirection,
            context
        );
        Intersection exitIntersect = getFirstIntersection(rayTargetPosToLight);
        // If the target point is not inside the object, maybe it's on the object's surface
        // Hence we return the origin color
        if (exitIntersect == null){
            return color;
        }
        // Assume that the target point is inside the object
        // Hence the first intersection of the ray will be object that the target point
        // is inside
        float intervalLength = MatrixUtils.norm(
            MatrixUtils.sub(exitIntersect.position, targetPosition)
        );
        float alpha = (float) Math.exp(-intervalLength * density);
        return MatrixUtils.mul(
            MatrixUtils.mul(color, alpha),
            lightSource.color
        );
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
    public static float[] getRefractedVector(float[] incidenceVector, float[] normal, float leavingRefraction, float enteringRefraction, boolean[] isFracted){
        float n = leavingRefraction / enteringRefraction;
        float c1 = - MatrixUtils.dot(incidenceVector, normal);
        if (1 - n*n*(1 - c1*c1) < 0){
            isFracted[0] = false;
            return reflect(incidenceVector, normal);
        }
        float c2 = (float) Math.sqrt(1 - n*n*(1 - c1 * c1));
        if (c1 < 0) c2 = - c2;
        float[] T = MatrixUtils.add(
            MatrixUtils.mul(incidenceVector, n),
            MatrixUtils.mul(normal, n * c1 - c2)
        );
        return T;
    }
    public static float[] shiftPosition(float[] position, float[] direction, float epsilon){
        return MatrixUtils.add(position, MatrixUtils.mul(direction, epsilon));
    }
    public static float[][] LambertianRaySimulationGeneration(float[] normal){
        float[] tangent;
        float[][] generatedRay = new float[8][];
        if (Math.abs(normal[0]) > 1e-6){
            tangent = MatrixUtils.normalize(
                MatrixUtils.crossProduct(
                    new float[]{0f, 1f, 0f}, normal
                )
            );
        } else {
            tangent = MatrixUtils.normalize(
                MatrixUtils.crossProduct(
                    new float[]{1f, 0f, 0f}, normal
                )
            );
        }
        float[] bitangent = MatrixUtils.crossProduct(normal, tangent);
        for (int i=0; i<8; i++){
            float angle = (float)(2.0 * Math.PI * i / 8);
            float x = (float)Math.cos(angle);
            float y = (float)Math.sin(angle);
            float z = (float)Math.sqrt(0.5);
            float[] localDir = new float[] {x * (float)Math.sqrt(0.5),
                y * (float)Math.sqrt(0.5),
                z};
            float[] worldDir = MatrixUtils.normalize(
                MatrixUtils.add(
                    MatrixUtils.mul(tangent, localDir[0]),
                    MatrixUtils.mul(bitangent, localDir[1]),
                    MatrixUtils.mul(normal, localDir[2])
                )
            );
            generatedRay[i] = worldDir;
        }
        return generatedRay;
    }
    public static float[] clampingColor(float[] color){
        float[] upperBound = ArgbColor.white;
        float[] lowerBound = ArgbColor.transparentBlack;
        return MatrixUtils.lower(
            MatrixUtils.upper(
                color, lowerBound
            ), upperBound
        );
    }
}
