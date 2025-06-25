package com.example.computergraphics.CollisionDetection;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class BBox {
    float[] lower;
    float[] upper;
    public BBox(){
        this.lower = new float[] {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE};
        this.upper = new float[] {- Float.MAX_VALUE, - Float.MAX_VALUE, - Float.MAX_VALUE};
    }
    public BBox(float[] lower, float[] upper){
        this.lower = lower;
        this.upper = upper;
    }
    public void insert(float[] point){
        lower = BBox.lowerBound(lower, point);
        upper = BBox.upperBound(upper, point);
    }
    public BBox[] split(float splitPosition, int axis){
        BBox[] splitBBoxes = new BBox[] {
            new BBox(lower.clone(), upper.clone()), new BBox(lower.clone(), upper.clone())
        };
        splitBBoxes[0].upper[axis] = splitPosition;
        splitBBoxes[1].lower[axis] = splitPosition;
        return splitBBoxes;
    }
    public boolean canIntersectWithLine(float[] source, float[] direction){
        float [][] intersectedRange = new float[3][2];
        for(int axis=0; axis<3; axis++){
            if (direction[axis] == 0){
                if (source[axis] > upper[axis] || source[axis] < lower[axis]) return false;
                intersectedRange[axis] = new float[] {0f, Float.MAX_VALUE};
            } else if (direction[axis] > 0){
                intersectedRange[axis] = new float[] {
                    (lower[axis] - source[axis]) / direction[axis],
                    (upper[axis] - source[axis]) / direction[axis],
                };
            } else {
                intersectedRange[axis] = new float[] {
                    (upper[axis] - source[axis]) / direction[axis],
                    (lower[axis] - source[axis]) / direction[axis],
                };
            }
        }
        // Check valid range
        float[] range = new float[]{
            max(max(intersectedRange[0][0], intersectedRange[1][0]), intersectedRange[2][0]),
            min(min(intersectedRange[0][1], intersectedRange[1][1]), intersectedRange[2][1]),
        };
        return (range[0] <= range[1]) && (range[1] >= 0);
    }
    static public BBox getObjectBBox(float[] vertices){
        BBox bbox = new BBox();
        for(int i=0; i<vertices.length; i+=3){
            float[] vertex = new float[] {vertices[i], vertices[i+1], vertices[i+2]};
            bbox.insert(vertex);
        }
        return bbox;
    }
    static public BBox getUnionBBoxes(BBox b1, BBox b2){
        float[] lower = BBox.lowerBound(b1.lower, b2.lower);
        float[] upper = BBox.upperBound(b1.upper, b2.upper);
        return new BBox(lower, upper);
    }
    static public float[] lowerBound(float[] l1, float[] l2){
        float[] lower = new float[l1.length];
        for(int i=0; i<l1.length; i++){
            lower[i] = min(l1[i], l2[i]);
        }
        return lower;
    }
    static public float[] upperBound(float[] l1, float[] l2){
        float[] upper = new float[l1.length];
        for(int i=0; i<l1.length; i++){
            upper[i] = max(l1[i], l2[i]);
        }
        return upper;
    }
}
