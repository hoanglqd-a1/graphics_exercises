package com.example.computergraphics.test_spatial_structure;

import android.content.Context;
import android.util.Log;

import com.example.computergraphics.CollisionDetection.*;
import com.example.computergraphics.object.ExperiementBox;
import com.example.computergraphics.object.GraphicObject;
import com.example.computergraphics.object.Line;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Unit {
    public static class UnitInput{
        public int n_rays;
        public int n_aabbs;
        public float[] origins;
        public float[] directions;
        public float[] min;
        public float[] max;

        public UnitInput(int n_rays, int n_aabbs, float[] origins, float[] directions, float[] min, float[] max){
            this.n_rays = n_rays;
            this.n_aabbs = n_aabbs;
            this.origins = origins;
            this.directions = directions;
            this.min = min;
            this.max = max;
        }
    }
    public static class UnitOutput{
        Map<Integer, Integer[]> outputs;
        Long exec_time;

        public UnitOutput(Map<Integer, Integer[]> outputs){
            this.outputs = outputs;
        }
    }

    public static UnitOutput functionTest(UnitInput input, Context context){
        // enter code
        List<GraphicObject> experimentBoxes = new ArrayList<>();
        for(int i=0; i<input.n_aabbs; i++){
            float[] min = new float[] {
                input.min[i*3], input.min[i*3+1], input.min[i*3+2]
            };
            float[] max = new float[] {
                input.max[i*3], input.max[i*3+1], input.max[i*3+2]
            };
            ExperiementBox box = new ExperiementBox(min, max, context);
            box.id = i;
            experimentBoxes.add(box);
        }
        List<Line> lines = new ArrayList<>();
        for(int i=0; i<input.n_rays; i++){
            float[] origin = new float[] {
                input.origins[i*3], input.origins[i*3+1], input.origins[i*3+2]
            };
            float[] direction = new float[] {
                input.directions[i*3], input.directions[i*3+1], input.directions[i*3+2]
            };
            Line tmp = new Line(origin, direction, 0, 0, context);
            lines.add(tmp);
        }
        List<Integer>[] intersectedObjectsList = new ArrayList[input.n_rays];
//        Grid grid = new Grid(experimentBoxes);
        KDTree kdTree = new KDTree(experimentBoxes);

        long start = System.currentTimeMillis();
        for(int i=0; i<lines.size(); i++){
            intersectedObjectsList[i] = new ArrayList<>();
            Set<GraphicObject> intersectedObjects = kdTree.traverse(kdTree.rootNode, lines.get(i).getWorldSource(), lines.get(i).getWorldDirection());
            for(GraphicObject obj : intersectedObjects){
                obj.getIntersectionsWithLine(lines.get(i));
                if(obj.isIntersected){
                    intersectedObjectsList[i].add(obj.id);
                    obj.isIntersected = false;
                }
            }
        }
//        for(int i=0; i<lines.size(); i++){
//            intersectedObjectsList[i] = new ArrayList<>();
//            Set<List<Integer>> voxels = grid.traverse(lines.get(i));
//            Set<GraphicObject> intersectedObjects = grid.getIntersectedObjectSet(voxels);
//            for(GraphicObject obj : intersectedObjects){
//                obj.getIntersectionsWithLine(lines.get(i));
//                if(obj.isIntersected){
//                    intersectedObjectsList[i].add(obj.id);
//                    obj.isIntersected = false;
//                }
//            }
//        }
//        for(int i=0; i<lines.size(); i++){
//            intersectedObjectsList[i] = new ArrayList<>();
//            for (int j=0; j<experimentBoxes.size(); j++){
//                List<float[][]> tmp = experimentBoxes.get(j).getIntersectionsWithLine(lines.get(i));
//                if(experimentBoxes.get(j).isIntersected){
//                    intersectedObjectsList[i].add(j);
//                    experimentBoxes.get(j).isIntersected = false;
//                }
//            }
//        }

        long end = System.currentTimeMillis();
        Log.d("ExperimentLog", "Traverse time: " + (end - start) + " ms");

        HashMap<Integer, Integer[]> map = new HashMap<>();
        for(int i=0; i<input.n_rays; i++){
            map.put(i, intersectedObjectsList[i].toArray(new Integer[0]));
        }
        UnitOutput unitOutput = new UnitOutput(map);
        unitOutput.exec_time = end - start;
        return unitOutput;
    }
}
