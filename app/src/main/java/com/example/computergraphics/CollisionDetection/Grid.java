package com.example.computergraphics.CollisionDetection;

import static java.lang.Math.min;
import static java.lang.Math.max;

import com.example.computergraphics.object.GraphicObject;
import com.example.computergraphics.object.Line;
import com.example.computergraphics.utils.MatrixUtils;
import com.example.computergraphics.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Grid {
    float control = 1f;
    public float[][] gridBBox = new float[3][2];
    int[] resolution;
    float[] voxelSize;
    List<GraphicObject>[][][] objectsPerVoxel;
    public Grid(List<GraphicObject> objects) {
        List<float[][]> objectBBoxes = new ArrayList<>();
        for(GraphicObject obj: objects){
            float[][] objBBox = this.getObjectBBox(obj);
            objectBBoxes.add(objBBox);
        }
        gridBBox = new float[][]{
            new float[] {Float.MAX_VALUE, - Float.MAX_VALUE},
            new float[] {Float.MAX_VALUE, - Float.MAX_VALUE},
            new float[] {Float.MAX_VALUE, - Float.MAX_VALUE},
        };
        for(float[][] objBBox: objectBBoxes){
            gridBBox[0][0] = min(gridBBox[0][0], objBBox[0][0]);
            gridBBox[0][1] = max(gridBBox[0][1], objBBox[0][1]);
            gridBBox[1][0] = min(gridBBox[1][0], objBBox[1][0]);
            gridBBox[1][1] = max(gridBBox[1][1], objBBox[1][1]);
            gridBBox[2][0] = min(gridBBox[2][0], objBBox[2][0]);
            gridBBox[2][1] = max(gridBBox[2][1], objBBox[2][1]);
        }
        gridBBox[0][0] -= 0.01f;
        gridBBox[1][0] -= 0.01f;
        gridBBox[2][0] -= 0.01f;
        gridBBox[0][1] += 0.01f;
        gridBBox[1][1] += 0.01f;
        gridBBox[2][1] += 0.01f;

//        Log.d("Grid bounding box", Arrays.toString(gridBBox[0]));
//        Log.d("Grid bounding box", Arrays.toString(gridBBox[1]));
//        Log.d("Grid bounding box", Arrays.toString(gridBBox[2]));
        int voxel_per_axis = computeGridResolution(objects.size(), 3);
        resolution = new int[]{voxel_per_axis, voxel_per_axis, voxel_per_axis};
        voxelSize = new float[] {
            (gridBBox[0][1] - gridBBox[0][0]) / resolution[0],
            (gridBBox[1][1] - gridBBox[1][0]) / resolution[1],
            (gridBBox[2][1] - gridBBox[2][0]) / resolution[2],
        };
        objectsPerVoxel = new List[voxel_per_axis][voxel_per_axis][voxel_per_axis];
        assert(objectBBoxes.size() == objects.size());
        int objPerVoxelTotal = 0;
        for(int i=0; i<objects.size(); i++){
            float[][] objBBox = objectBBoxes.get(i);
            float[] minBounding = new float[] {objBBox[0][0], objBBox[1][0], objBBox[2][0]};
            float[] maxBounding = new float[] {objBBox[0][1], objBBox[1][1], objBBox[2][1]};
            List<Integer> minVoxelBounding = world2VoxelCoordinate(minBounding);
            List<Integer> maxVoxelBounding = world2VoxelCoordinate(maxBounding);
            for(int x=minVoxelBounding.get(0); x<=maxVoxelBounding.get(0); x++){
                for(int y=minVoxelBounding.get(1); y<=maxVoxelBounding.get(1); y++){
                    for(int z=minVoxelBounding.get(2); z<=maxVoxelBounding.get(2); z++){
                        if(objectsPerVoxel[x][y][z] == null) objectsPerVoxel[x][y][z] = new ArrayList<>();
                        objectsPerVoxel[x][y][z].add(objects.get(i));
                        objPerVoxelTotal++;
                    }
                }
            }
        }
    }
    public Set<List<Integer>> traverse(Line l){
        Set<List<Integer>> voxelIntersectionSet = new HashSet<>();
        List<Integer> start = world2VoxelCoordinate(l.source);
        if(start.get(0) >= 0 && start.get(1) >=0 && start.get(2) >= 0) voxelIntersectionSet.add(start);
        float[] source = l.getSource();
        float[] direction = l.getDirection();
        float[][] dist2VoxelIntersectsEachAxis = new float[3][2];
        // Calculate the range of t where the
        // world(lower_bound(voxel[axis])) <= (source + t * direction)[axis] <= world(upper_bound(voxel[axis]))
        for(int axis=0; axis<3; ++axis){
            if (direction[axis] == 0){
                if (source[axis] < gridBBox[axis][0] || source[axis] > gridBBox[axis][1]) return voxelIntersectionSet;
                dist2VoxelIntersectsEachAxis[axis][0] = 0f;
                dist2VoxelIntersectsEachAxis[axis][1] = Float.MAX_VALUE;
            }
            else if(direction[axis] > 0){
                float firstEncounter = min(0, (gridBBox[axis][0] - source[axis]) / direction[axis]);
                float lastEncounter = (gridBBox[axis][1] - source[axis]) / direction[axis];
                if (lastEncounter < 0) return voxelIntersectionSet;
                dist2VoxelIntersectsEachAxis[axis] = new float[] {firstEncounter, lastEncounter};
            }
            else {
                float firstEncounter = min(0, (gridBBox[axis][1] - source[axis]) / direction[axis]);
                float lastEncounter = (gridBBox[axis][0] - source[axis]) / direction[axis];
                if (lastEncounter < 0) return voxelIntersectionSet;
                dist2VoxelIntersectsEachAxis[axis] = new float[] {firstEncounter, lastEncounter};
            }
        }
        // calculate the union of range in 3 axis
        float[] union = Utils.Union(
                dist2VoxelIntersectsEachAxis[0],
                dist2VoxelIntersectsEachAxis[1]);
        if(union == null) return new HashSet<>();
        union = Utils.Union(union,
                dist2VoxelIntersectsEachAxis[2]);
        if(union == null) return new HashSet<>();
        float first = union[0];
        float last  = union[1];
        float[] firstBBoxEncounter = MatrixUtils.add(source, MatrixUtils.mul(direction, first));
        float[] lastBBoxEncounter = MatrixUtils.add(source, MatrixUtils.mul(direction, last));
        List<Integer> firstVoxelEncounter = world2VoxelCoordinate(firstBBoxEncounter);
        List<Integer> lastVoxelEncounter = world2VoxelCoordinate(lastBBoxEncounter);
//        Log.d("Voxels", "Start printing voxels");
        for (int axis=0; axis<3; ++axis){
            if(direction[axis] == 0) continue;
            if(direction[axis] >  0){
                for(int i=firstVoxelEncounter.get(axis); i<lastVoxelEncounter.get(axis); i++){
                    float t = (i * voxelSize[axis] + gridBBox[axis][0] - source[axis]) / direction[axis];
                    if (t < first || t > last) continue;
                    List<Integer> voxelIntersect = world2VoxelCoordinate(
                        MatrixUtils.add(source, MatrixUtils.mul(direction, t))
                    );
//                    Log.d("Voxels", Arrays.toString(voxelIntersect.toArray()));
                    voxelIntersectionSet.add(voxelIntersect);
                }
            }
            else {
                for(int i=firstVoxelEncounter.get(axis); i>lastVoxelEncounter.get(axis); i--){
                    float t = (i * voxelSize[axis] + gridBBox[axis][0] - source[axis]) / direction[axis];
                    if (t < first || t > last) continue;
                    List<Integer> voxelIntersect = world2VoxelCoordinate(
                        MatrixUtils.add(source, MatrixUtils.mul(direction, t))
                    );
                    voxelIntersect.set(axis, voxelIntersect.get(axis) - 1);
                    voxelIntersect = this.clamping(voxelIntersect);
//                    Log.d("Voxels", Arrays.toString(voxelIntersect.toArray()));
                    voxelIntersectionSet.add(voxelIntersect);
                }
            }
        }
        return voxelIntersectionSet;
    }
    public Set<GraphicObject> getIntersectedObjectSet(Set<List<Integer>> voxelIntersectionSet){
        Set<GraphicObject> objectsSet = new HashSet<>();
        for(List<Integer> voxel: voxelIntersectionSet){
            List<GraphicObject> objList = objectsPerVoxel[voxel.get(0)][voxel.get(1)][voxel.get(2)];
            if(objList != null) {
                objectsSet.addAll(objList);
            }
        }
        return objectsSet;
    }
    public List<GraphicObject.Intersection> getIntersectionWithLine(Line l){
        List<GraphicObject.Intersection> intersections = new ArrayList<>();
        Set<List<Integer>> intersectedVoxel = this.traverse(l);
//        Log.d("Object count", "Resolution " + Arrays.toString(resolution));
//        Log.d("Object count", "Number of intersected voxels: " + intersectedVoxel.size());
        Set<GraphicObject> intersectedObjects = this.getIntersectedObjectSet(intersectedVoxel);
//        Log.d("Object count", "Number of intersected objects: " + intersectedObjects.size());
        for(GraphicObject obj: intersectedObjects){
            List<GraphicObject.Intersection> intersectionsWithLinePerObject = obj.getIntersectionsWithLine(l);
            intersections.addAll(intersectionsWithLinePerObject);
        }
        return intersections;
    }
    public List<Integer> world2VoxelCoordinate(float[] coordinate){
        return this.clamping(
            Arrays.asList(
                (int) Math.floor((coordinate[0] - gridBBox[0][0]) / voxelSize[0]),
                (int) Math.floor((coordinate[1] - gridBBox[1][0]) / voxelSize[1]),
                (int) Math.floor((coordinate[2] - gridBBox[2][0]) / voxelSize[2])
            )
        );
    }
    public List<Float> voxel2WorldCoordinate(int[] coordinate){
        return Arrays.asList(
            coordinate[0] * voxelSize[0] + gridBBox[0][0],
            coordinate[1] * voxelSize[1] + gridBBox[1][0],
            coordinate[2] * voxelSize[2] + gridBBox[2][0]
        );
    }
    private List<Integer> clamping(List<Integer> coordinate){
        assert (coordinate.size() == 3);
        return Arrays.asList(
            min(max(coordinate.get(0), 0), resolution[0] - 1),
            min(max(coordinate.get(1), 0), resolution[1] - 1),
            min(max(coordinate.get(2), 0), resolution[2] - 1)
        );
    }
    private int computeGridResolution(int objectCount, float control){
        return min((int) Math.ceil(Math.cbrt((double) objectCount) * control), 16);
    }
    private float[][] getObjectBBox(GraphicObject obj){
        float[][] bbox = new float[][]{
            {Float.MAX_VALUE, - Float.MAX_VALUE},
            {Float.MAX_VALUE, - Float.MAX_VALUE},
            {Float.MAX_VALUE, - Float.MAX_VALUE},
        };
        float[] vertices = obj.getWorldVertices();
        for(int i=0; i<vertices.length; i+=3){
            bbox[0][0] = min(bbox[0][0], vertices[i]);
            bbox[1][0] = min(bbox[1][0], vertices[i+1]);
            bbox[2][0] = min(bbox[2][0], vertices[i+2]);
            bbox[0][1] = max(bbox[0][1], vertices[i]);
            bbox[1][1] = max(bbox[1][1], vertices[i+1]);
            bbox[2][1] = max(bbox[2][1], vertices[i+2]);
        }
//        Log.d("Object Bounding box", Arrays.toString(bbox[0]));
//        Log.d("Object Bounding box", Arrays.toString(bbox[1]));
//        Log.d("Object Bounding box", Arrays.toString(bbox[2]));

        return bbox;
    }
}
