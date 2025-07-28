//package com.example.computergraphics.CollisionDetection;
//
//import com.example.computergraphics.object.GraphicObject;
//import com.example.computergraphics.object.GraphicObject.Intersection;
//import com.example.computergraphics.utils.BBox;
//import com.example.computergraphics.utils.MatrixUtils;
//
//import java.util.ArrayList;
//import java.util.List;
//
//class ObjectKDNode {
//    BBox bbox;
//    ObjectKDTree leftChild, rightChild;
//    int splitAxis = -1;
//    float splitPosition;
//    List<float[]> meshList = null;
//
//    public ObjectKDNode() {};
//    public boolean isLeaf() {return splitAxis == -1;}
//}
//public class ObjectKDTree {
//    private final int MAX_DEPTH = 15;
//    private final int MINIMUM_MESHES_IN_LEAF;
//    public ObjectKDNode rootNode;
//    public ObjectKDTree(GraphicObject object){
//        List<BBox> meshBboxes =  new ArrayList<>();
//        float[] vertices = object.getWorldVertices();
//        assert (vertices.length % 9 == 0);
//        List<float[]> meshes = new ArrayList<>();
//        for(int i=0; i<vertices.length; i += 9){
//            float[] meshVertices = new float[9];
//            System.arraycopy(vertices, i, meshVertices, 0, 9);
//            meshBboxes.add(BBox.getObjectBBox(meshVertices));
//            meshes.add(meshVertices);
//        }
//        MINIMUM_MESHES_IN_LEAF = Math.max((int) (vertices.length * 1f / (9 * Math.pow(2f, MAX_DEPTH))), 1);
//        BBox rootBBox = new BBox();
//        for (BBox objBBox : meshBboxes){
//            rootBBox = BBox.getUnionBBoxes(rootBBox, objBBox);
//        }
////        Log.d("KDTree", "root's bounding box: " + Arrays.toString(rootBBox.lower) + " " + Arrays.toString(rootBBox.upper));
//        rootNode = build(meshes, meshBboxes, rootBBox, 0);
//    }
//    ObjectKDNode build(List<float[]> meshVertices, List<BBox> objBBoxes, BBox nodeBBox, int depth){
//        if (meshVertices.isEmpty()) return null;
//        ObjectKDNode node = new ObjectKDNode();
//        node.bbox = nodeBBox;
////        Log.d("KDTree", "Depth: " + depth);
////        Log.d("KDTree", "Number of objects: " + objects.size());
//        if (depth == MAX_DEPTH || meshVertices.size() <= MINIMUM_MESHES_IN_LEAF){
//            node.objectList = objects;
//            return node;
//        }
//        float[] bboxSize = MatrixUtils.sub(nodeBBox.upper, nodeBBox.lower);
//        int splitAxis = 0;
//        for(int axis=0; axis<3; axis++){
//            if(bboxSize[axis] > bboxSize[splitAxis]) splitAxis = axis;
//        }
//        float splitPosition = (nodeBBox.upper[splitAxis] + nodeBBox.lower[splitAxis]) / 2;
//        node.splitAxis = splitAxis;
//        List<GraphicObject> leftObjects = new ArrayList<>();
//        List<GraphicObject> rightObjects = new ArrayList<>();
//        List<BBox> leftObjBBoxes = new ArrayList<>();
//        List<BBox> rightObjBBoxes = new ArrayList<>();
//        for(int i=0; i<objects.size(); i++){
//            if(objBBoxes.get(i).lower[splitAxis] < splitPosition){
//                leftObjects.add(objects.get(i));
//                leftObjBBoxes.add(objBBoxes.get(i));
//            }
//            if(objBBoxes.get(i).upper[splitAxis] > splitPosition){
//                rightObjects.add(objects.get(i));
//                rightObjBBoxes.add(objBBoxes.get(i));
//            }
//        }
//        BBox[] childBBoxes = nodeBBox.split(splitPosition, splitAxis);
//        node.leftChild = build(leftObjects, leftObjBBoxes, childBBoxes[0], depth+1);
//        node.rightChild = build(rightObjects, rightObjBBoxes, childBBoxes[1], depth+1);
//        return node;
//    }
//}
