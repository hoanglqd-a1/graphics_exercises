package com.example.computergraphics.utils;


import android.content.Context;
import android.opengl.Matrix;

import java.io.*;

public class Utils {
    public static String readRawTextFile(Context context, int resId) {
        InputStream inputStream = context.getResources().openRawResource(resId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
    public static float[] getModelMatrix(float[] translation, float[] rotation, float[] scale) {
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);

        // Apply translation FIRST
        Matrix.translateM(modelMatrix, 0, translation[0], translation[1], translation[2]);

        // Apply rotations
        Matrix.rotateM(modelMatrix, 0, rotation[2], 0.0f, 0.0f, 1.0f); // Z
        Matrix.rotateM(modelMatrix, 0, rotation[1], 0.0f, 1.0f, 0.0f); // Y
        Matrix.rotateM(modelMatrix, 0, rotation[0], 1.0f, 0.0f, 0.0f); // X

        // Apply scaling LAST
        Matrix.scaleM(modelMatrix, 0, scale[0], scale[1], scale[2]);

        return modelMatrix;
    }

    public static float[] Union(float[] range1, float[] range2){
        float[] union = {Math.max(range1[0], range2[0]), Math.min(range1[1], range2[1])};
        if (union[0] > union[1]) return null;
        return union;
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
}
