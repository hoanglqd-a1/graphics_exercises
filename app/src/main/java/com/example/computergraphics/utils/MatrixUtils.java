package com.example.computergraphics.utils;

import java.util.Random;

public class MatrixUtils {
    public static float MM(float [] v1, float [] v2){
        float ans = 0.0f;
        for (int i=0; i<v1.length; i++){
            ans += v1[i] * v2[i];
        }
        return ans;
    }
    public static float [] add(float [] v1, float [] v2){
        float [] ans = new float[v1.length];
        for (int i=0; i<v1.length; i++){
            ans[i] = v1[i] + v2[i];
        }
        return ans;
    }
    public static float [] sub(float [] v1, float [] v2){
        float [] ans = new float[v1.length];
        for (int i=0; i<v1.length; i++){
            ans[i] = v1[i] - v2[i];
        }
        return ans;
    }
    public static float [] mul(float [] v, float scalar) {
        float a [] = new float[v.length];
        for (int i=0; i<v.length; i++){
            a[i] = v[i] * scalar;
        }
        return a;
    }
    public static float [] crossProduct (float [] v1, float [] v2){
        return new float [] {v1[1]*v2[2] - v1[2]*v2[1], v1[2]*v2[0] - v1[0]*v2[2], v1[0]*v2[1] - v1[1]*v2[0]};
    }
    public static float[] normalize(float[] v) {
        float mag = (float)Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
        return new float[]{ v[0]/mag, v[1]/mag, v[2]/mag };
    }
    public static float norm(float[] v){
        return (float)Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
    }
    public static float[] getBarycentricCoordinate (float [][] ABC, float [] P){
        float [] AB = MatrixUtils.sub(ABC[1], ABC[0]);
        float [] AC = MatrixUtils.sub(ABC[2], ABC[0]);
        float [] AP = MatrixUtils.sub(P, ABC[0]);
        float SABC = 0.5f * MatrixUtils.norm(MatrixUtils.crossProduct(AB, AC));
        float SABP = 0.5f * MatrixUtils.norm(MatrixUtils.crossProduct(AB, AP));
        float SACP = 0.5f * MatrixUtils.norm(MatrixUtils.crossProduct(AC, AP));
        float SBCP = SABC - SABP - SACP;
        return new float [] {SBCP / SABC, SACP / SABC, SABP / SABC};
    }
    public static float[] generateRandomVector(int length){
        float[] vector = new float[length];
        Random random = new Random();
        for(int i=0; i<length; i++){
            vector[i] = random.nextFloat();
        }
        return vector;
    }
}
