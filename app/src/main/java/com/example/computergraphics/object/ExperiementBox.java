package com.example.computergraphics.object;

import android.content.Context;
import android.util.Log;

import com.example.computergraphics.CollisionDetection.BBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 3D box (cuboid) defined by its minimum and maximum coordinates.
 * Provides methods to generate its vertices, colors, and draw itself using OpenGL ES 1.0.
 */
public class ExperiementBox extends GraphicObject {
    BBox bbox;
    public ExperiementBox(float[] min, float[] max, Context context){
        super(-1, context);
        bbox = new BBox(min, max);
        vertexData = new float[] {min[0], min[1], min[2], max[0], max[1], max[2]};
    }
    public List<float[][]> getIntersectionsWithLine(Line line){
        float[] source = line.getWorldSource();
        float[] direction = line.getWorldDirection();
        isIntersected = bbox.canIntersectWithLine(source, direction);
        return new ArrayList<>();
    }
}
