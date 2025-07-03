package com.example.computergraphics.object;

import android.content.Context;

import com.example.computergraphics.utils.MatrixUtils;

import java.util.ArrayList;
import java.util.List;

public class Sphere extends GraphicObject{
    static float[] defaultCenter = {0f, 0f, 0f};
    static float defaultRadius = 1f;
    public Sphere(float[] center, float radius, Context context){
        super(context);
        float[][] sphereData = generateSphereVertices(1, 180, 180);
        vertexData = sphereData[0];
        normalData = sphereData[1];
        textureCoordinateData = sphereData[2];
        createBuffer();
        scale = new float[] {radius, radius, radius};
        translation = center;
    }
    public Sphere(Context context){
        this(defaultCenter, defaultRadius, context);
    }
    public static float[][] generateSphereVertices(float radius, int numStacks, int numSlices) {
        List<Float> positions = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();

        // Iterate through stacks (latitude bands)
        for (int i = 0; i <= numStacks; i++) {
            // Theta (latitude angle) from -PI/2 (south pole) to PI/2 (north pole)
            // Stacks go from bottom to top, so theta increases.
            float theta = (float) (i * Math.PI / numStacks - Math.PI / 2.0); // -PI/2 to PI/2

            float sinTheta = (float) Math.sin(theta);
            float cosTheta = (float) Math.cos(theta);

            // Iterate through slices (longitude segments)
            for (int j = 0; j <= numSlices; j++) {
                // Phi (longitude angle) from 0 to 2*PI
                float phi = (float) (j * 2 * Math.PI / numSlices); // 0 to 2*PI

                float sinPhi = (float) Math.sin(phi);
                float cosPhi = (float) Math.cos(phi);

                // Calculate vertex position (X, Y, Z)
                float x = radius * cosTheta * cosPhi;
                float y = radius * sinTheta;
                float z = radius * cosTheta * sinPhi;

                positions.add(x);
                positions.add(y);
                positions.add(z);

                // Normals: For a sphere centered at the origin, the normal is just the normalized position vector
                float normalX = x / radius;
                float normalY = y / radius;
                float normalZ = z / radius;
                normals.add(normalX);
                normals.add(normalY);
                normals.add(normalZ);

                // Texture Coordinates (U, V)
                // U: Maps longitude (phi) from 0 to 1 (left to right)
                // V: Maps latitude (theta) from 0 to 1 (bottom to top, then flipped for top-left origin)
                float u = (float) j / numSlices;
                float v = (float) i / numStacks; // V=0 at south pole, V=1 at north pole

                // Adjust V if you want 0 at the top of the texture (common for images)
                // float v = 1.0f - ((float) i / numStacks); // V=1 at south pole, V=0 at north pole
                texCoords.add(u);
                texCoords.add(v);
            }
        }

        // Convert lists to float arrays (for Positions, Normals, TexCoords)
        // Now, generate triangles from the grid of points.
        // This will create a non-indexed buffer (vertices are duplicated).
        List<Float> finalVertices = new ArrayList<>();
        List<Float> finalNormals = new ArrayList<>();
        List<Float> finalTexCoords = new ArrayList<>();

        for (int i = 0; i < numStacks; i++) {
            for (int j = 0; j < numSlices; j++) {
                // Get indices for the 4 corners of the quad (two triangles)
                // v1 --- v2
                // |      |
                // v3 --- v4
                // where indices are based on the full list of points generated above.
                int p1 = i * (numSlices + 1) + j;
                int p2 = i * (numSlices + 1) + j + 1;
                int p3 = (i + 1) * (numSlices + 1) + j;
                int p4 = (i + 1) * (numSlices + 1) + j + 1;

                // Triangle 1: p1, p3, p2 (top-left, bottom-left, top-right)
                addVertexData(finalVertices, finalNormals, finalTexCoords, positions, normals, texCoords, p1);
                addVertexData(finalVertices, finalNormals, finalTexCoords, positions, normals, texCoords, p3);
                addVertexData(finalVertices, finalNormals, finalTexCoords, positions, normals, texCoords, p2);

                // Triangle 2: p2, p3, p4 (top-right, bottom-left, bottom-right)
                addVertexData(finalVertices, finalNormals, finalTexCoords, positions, normals, texCoords, p2);
                addVertexData(finalVertices, finalNormals, finalTexCoords, positions, normals, texCoords, p3);
                addVertexData(finalVertices, finalNormals, finalTexCoords, positions, normals, texCoords, p4);
            }
        }

        // Convert ArrayLists to primitive float arrays
        float[] verticesArray = new float[finalVertices.size()];
        for (int i = 0; i < finalVertices.size(); i++) {
            verticesArray[i] = finalVertices.get(i);
        }

        float[] normalsArray = new float[finalNormals.size()];
        for (int i = 0; i < finalNormals.size(); i++) {
            normalsArray[i] = finalNormals.get(i);
        }

        float[] texCoordsArray = new float[finalTexCoords.size()];
        for (int i = 0; i < finalTexCoords.size(); i++) {
            texCoordsArray[i] = finalTexCoords.get(i);
        }

        return new float[][]{verticesArray, normalsArray, texCoordsArray};
    }
    private static void addVertexData(
            List<Float> finalVertices, List<Float> finalNormals, List<Float> finalTexCoords,
            List<Float> sourcePositions, List<Float> sourceNormals, List<Float> sourceTexCoords,
            int index) {
        // Position (XYZ)
        finalVertices.add(sourcePositions.get(index * 3));
        finalVertices.add(sourcePositions.get(index * 3 + 1));
        finalVertices.add(sourcePositions.get(index * 3 + 2));

        // Normal (XYZ)
        finalNormals.add(sourceNormals.get(index * 3));
        finalNormals.add(sourceNormals.get(index * 3 + 1));
        finalNormals.add(sourceNormals.get(index * 3 + 2));

        // Texture Coords (UV)
        finalTexCoords.add(sourceTexCoords.get(index * 2));
        finalTexCoords.add(sourceTexCoords.get(index * 2 + 1));
    }
    public List<Intersection> getIntersectionsWithRay(Ray ray){
        List<Intersection> intersections = new ArrayList<>();
        float[] center = getWorldCenter();
        float radius = getWorldRadius();
        float[] source = ray.source;
        float[] direction = ray.direction;
        float[] center2Source = MatrixUtils.sub(source, center);
        float b = MatrixUtils.dot(direction, center2Source);
        float c = MatrixUtils.dot(center2Source, center2Source) - radius * radius;
        float discriminant = b*b - c;
        if (discriminant > 0){
            float t1 = -b - (float) Math.sqrt(discriminant);
            float t2 = -b + (float) Math.sqrt(discriminant);
            if (t1 > 1e-6){
                float[] coordinate = MatrixUtils.add(source, MatrixUtils.mul(direction, t1));
                Intersection intersection = new Intersection(
                    this,
                    coordinate,
                    MatrixUtils.normalize(MatrixUtils.sub(coordinate, center))
                );
                intersections.add(intersection);
            }
            if (t2 > 1e-6){
                float[] coordinate = MatrixUtils.add(source, MatrixUtils.mul(direction, t2));
                Intersection intersection = new Intersection(
                    this,
                    coordinate,
                    MatrixUtils.normalize(MatrixUtils.sub(coordinate, center))
                );
                intersections.add(intersection);
            }
        }
        else if (discriminant == 0){
            float t = -b;
            if (t > 1e-6){
                float[] coordinate = MatrixUtils.add(source, MatrixUtils.mul(direction, t));
                Intersection intersection = new Intersection(
                    this,
                    coordinate,
                    MatrixUtils.sub(coordinate, center)
                );
                intersections.add(intersection);
            }
        }
        return intersections;
    }
    public float[] getWorldCenter() { return translation; }
    public float getWorldRadius() { return scale[0]; }
}
