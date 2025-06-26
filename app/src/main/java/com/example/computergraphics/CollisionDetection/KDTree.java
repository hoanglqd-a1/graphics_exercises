package com.example.computergraphics.CollisionDetection;

import com.example.computergraphics.object.GraphicObject;
import com.example.computergraphics.object.Line;
import com.example.computergraphics.utils.BBox;
import com.example.computergraphics.utils.MatrixUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class KDNode {
    BBox bbox;
    KDNode leftChild, rightChild;
    int splitAxis = -1;
    float splitPosition;
    List<GraphicObject> objectList = null;
    public KDNode() {};
    public boolean isLeaf() {return splitAxis == -1;}
}
public class KDTree {
    private final int MAX_DEPTH = 15;
    private int MINIMUM_OBJ_IN_LEAF = 1;
    public KDNode rootNode;
    public int count = 0;
    public KDTree(List<GraphicObject> objects){
        List<BBox> objBboxes =  new ArrayList<>();
        for(GraphicObject object: objects){
            float[] vertices = object.getWorldVertices();
            objBboxes.add(BBox.getObjectBBox(vertices));
        }
        MINIMUM_OBJ_IN_LEAF = Math.max((int) (objects.size() * 1f / Math.pow(2f, MAX_DEPTH)), 1);
        BBox rootBBox = new BBox();
        for (BBox objBBox : objBboxes){
            rootBBox = BBox.getUnionBBoxes(rootBBox, objBBox);
        }
//        Log.d("KDTree", "root's bounding box: " + Arrays.toString(rootBBox.lower) + " " + Arrays.toString(rootBBox.upper));
        rootNode = build(objects, objBboxes, rootBBox, 0);
    }
    KDNode build(List<GraphicObject> objects, List<BBox> objBBoxes, BBox nodeBBox, int depth){
        if (objects.isEmpty()) return null;
//        Log.d("KDTree", "node's bounding box: " + Arrays.toString(nodeBBox.lower) + " " + Arrays.toString(nodeBBox.upper));
        KDNode node = new KDNode();
        node.bbox = nodeBBox;
//        Log.d("KDTree", "Depth: " + depth);
//        Log.d("KDTree", "Number of objects: " + objects.size());
        if (depth == MAX_DEPTH || objects.size() <= MINIMUM_OBJ_IN_LEAF){
            node.objectList = objects;
            return node;
        }
        float[] bboxSize = MatrixUtils.sub(nodeBBox.upper, nodeBBox.lower);
        int splitAxis = 0;
        for(int axis=0; axis<3; axis++){
            if(bboxSize[axis] > bboxSize[splitAxis]) splitAxis = axis;
        }
        float splitPosition = (nodeBBox.upper[splitAxis] + nodeBBox.lower[splitAxis]) / 2;
        node.splitAxis = splitAxis;
        List<GraphicObject> leftObjects = new ArrayList<>();
        List<GraphicObject> rightObjects = new ArrayList<>();
        List<BBox> leftObjBBoxes = new ArrayList<>();
        List<BBox> rightObjBBoxes = new ArrayList<>();
        for(int i=0; i<objects.size(); i++){
            if(objBBoxes.get(i).lower[splitAxis] < splitPosition){
                leftObjects.add(objects.get(i));
                leftObjBBoxes.add(objBBoxes.get(i));
            }
            if(objBBoxes.get(i).upper[splitAxis] > splitPosition){
                rightObjects.add(objects.get(i));
                rightObjBBoxes.add(objBBoxes.get(i));
            }
        }
        BBox[] childBBoxes = nodeBBox.split(splitPosition, splitAxis);
        node.leftChild = build(leftObjects, leftObjBBoxes, childBBoxes[0], depth+1);
        node.rightChild = build(rightObjects, rightObjBBoxes, childBBoxes[1], depth+1);
        return node;
    }
    public Set<GraphicObject> traverse(KDNode node, float[] source, float[] direction){
        if (node == null) return new HashSet<>();
        if (!node.bbox.canIntersectWithLine(source, direction)) return new HashSet<>();
        if (node.isLeaf()){
//            Log.d("KDTree", "Number of intersected Objects: " + node.objectList.size());
            return new HashSet<>(node.objectList);
        }
        Set<GraphicObject> intersectedObjects = new HashSet<>();
        intersectedObjects.addAll(traverse(node.leftChild, source, direction));
        intersectedObjects.addAll(traverse(node.rightChild, source, direction));
//        Log.d("KDTree", "Number of intersected Objects: " + intersectedObjects.size());
        return intersectedObjects;
    }
    public List<float[][]> getIntersectionWithLine(Line l){
        List<float[][]> intersections = new ArrayList<>();
        float[] source = l.getWorldSource();
        float[] direction = l.getWorldDirection();
        Set<GraphicObject> intersectedObjects = traverse(rootNode, source, direction);
        for(GraphicObject obj: intersectedObjects){
            intersections.addAll(
                obj.getIntersectionsWithLine(l)
            );
        }
        return intersections;
    }
    public void insert(KDNode node, GraphicObject object, BBox objectBBox){
        if (node.isLeaf()){
            node.objectList.add(object);
            node.bbox = BBox.getUnionBBoxes(node.bbox, objectBBox);
            return;
        }
        if (node.leftChild != null && objectBBox.lower[node.splitAxis] < node.splitPosition){
            insert(node.leftChild, object, objectBBox);
            node.bbox = BBox.getUnionBBoxes(node.bbox, node.leftChild.bbox);
        }
        if (node.rightChild != null && objectBBox.upper[node.splitAxis] >= node.splitPosition){
            insert(node.rightChild, object, objectBBox);
            node.bbox = BBox.getUnionBBoxes(node.bbox, node.rightChild.bbox);
        }
    }
    public void insertObject(GraphicObject object){
        float[] vertices = object.getWorldVertices();
        BBox objectBBox = BBox.getObjectBBox(vertices);
        insert(rootNode, object, objectBBox);
    }
}
