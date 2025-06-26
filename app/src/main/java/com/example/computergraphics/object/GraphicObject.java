package com.example.computergraphics.object;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.example.computergraphics.utils.MaterialFileHandle;
import com.example.computergraphics.utils.MatrixUtils;
import com.example.computergraphics.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class GraphicObject {
    public Context context;
    static final public int COORDS_PER_VERTEX = 3;
    static final public int COORDS_PER_NORMAL = 3;
    public float[] color = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
    public float vertexData [];
    public float normalData [];
    public float lightPositionData [] = {0.0f, 0.0f, 0.75f, 1.0f};
    public float textureCoordinateData [];
    public FloatBuffer vertexBuffer;
    public FloatBuffer normalBuffer;
    public FloatBuffer textureCoordinateBuffer;
    public boolean isIntersected = false;
    public int id = -1;
    public float translation [] = { 0.0f, 0.0f, 0.0f};
    public float rotation [] = { 0.0f, 0.0f, 0.0f};
    public float scale [] = { 1.0f, 1.0f, 1.0f };
    public float modelMatrix [] = new float[16];

    public MaterialFileHandle mtl;
    public final int textureCoordinateDataSize = 2;
    public final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
    public final int normalStride = COORDS_PER_VERTEX * 4;
    public GraphicObject(float [] vertexData, float [] normalData, float [] textureCoordinateData, Context context){
        this.vertexData = vertexData;
        this.normalData = normalData;
        this.textureCoordinateData = textureCoordinateData;
        this.context = context;
        this.mtl = new MaterialFileHandle("default");
        this.createBuffer();
    }
    public GraphicObject(InputStream objInputStream, Context context) {
        this.context = context;
        BufferedReader reader = new BufferedReader(new InputStreamReader(objInputStream));
        List<Float> vertices = new ArrayList<>();
        List<Float> textures = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Short> vfaces = new ArrayList<>();
        List<Short> tfaces = new ArrayList<>();
        List<Short> nfaces = new ArrayList<>();
        String line;
        while (true){
            try {
                if ((line = reader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String[] tokens = line.split(" ");
            if (tokens[0].equals("mtllib")){
                AssetManager assetManager = this.context.getAssets();
                InputStream mtlfile;
                try {
                    mtlfile = assetManager.open(tokens[1]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.mtl = this.readMaterialFile(mtlfile);
            }
            if (tokens[0].equals("v")){
                vertices.add(Float.parseFloat(tokens[1]));
                vertices.add(Float.parseFloat(tokens[2]));
                vertices.add(Float.parseFloat(tokens[3]));
            }
            else if (tokens[0].equals("vt")){
                textures.add(Float.parseFloat(tokens[1]));
                textures.add(Float.parseFloat(tokens[2]));
            }
            else if (tokens[0].equals("vn")){
                normals.add(Float.parseFloat(tokens[1]));
                normals.add(Float.parseFloat(tokens[2]));
                normals.add(Float.parseFloat(tokens[3]));
            }
            else if (tokens[0].equals("f")){
                int verticesCount = tokens.length;
                String [] token1 = tokens[1].split("/");
                for (int i = 2; i < verticesCount - 1; i++){
                    String [] token2 = tokens[i].split("/");
                    String [] token3 = tokens[i+1].split("/");
                    vfaces.add((short) (Integer.parseInt(token1[0]) - 1));
                    vfaces.add((short) (Integer.parseInt(token2[0]) - 1));
                    vfaces.add((short) (Integer.parseInt(token3[0]) - 1));
                    if (token1.length >= 2 && token1[1] != ""){
                        tfaces.add((short) (Integer.parseInt(token1[1]) - 1));
                        tfaces.add((short) (Integer.parseInt(token2[1]) - 1));
                        tfaces.add((short) (Integer.parseInt(token3[1]) - 1));
                    }
                    if (token1.length == 3 && token1[1] != ""){
                        nfaces.add((short) (Integer.parseInt(token1[2]) - 1));
                        nfaces.add((short) (Integer.parseInt(token2[2]) - 1));
                        nfaces.add((short) (Integer.parseInt(token3[2]) - 1));
                    }
                }
            }
            else if (tokens[0].equals("#")){
                // ignore comments
                continue;
            }
            else {
                // ignore other lines
                continue;
            }
        }
        if (this.mtl == null){
            this.mtl = new MaterialFileHandle("default");
        }
        float [] modelCoords = new float[vertices.size()];
        short [] drawOrder = new short[vfaces.size()];
        float textureCoords [] = new float[textures.size()];
        short textureOrder [] = new short[tfaces.size()];
        float normalCoords [] = new float [normals.size()];
        short normalOrder [] = new short[nfaces.size()];
        for (int i=0; i<vertices.size(); ++i){
            modelCoords[i] = vertices.get(i);
        }
        for (int i=0; i<vfaces.size(); ++i){
            drawOrder[i] = vfaces.get(i);
        }
        for (int i=0; i<textures.size(); ++i){
            textureCoords[i] = textures.get(i);
        }
        for (int i=0; i<tfaces.size(); ++i){
            textureOrder[i] = tfaces.get(i);
        }
        for (int i=0; i<normals.size(); ++i){
            normalCoords[i] = normals.get(i);
        }
        for (int i=0; i<nfaces.size(); ++i){
            normalOrder[i] = nfaces.get(i);
        }

        this.vertexData = createData(modelCoords, drawOrder, COORDS_PER_VERTEX);
        this.textureCoordinateData = createData(textureCoords, textureOrder, 2);
        this.normalData = createData(normalCoords, normalOrder, 3);
        this.createBuffer();
    }
    public void createBuffer() {
        if (this.vertexData != null) {
            this.vertexBuffer = toBuffer(this.vertexData);
        }
        if (this.textureCoordinateData != null) {
            this.textureCoordinateBuffer = toBuffer(this.textureCoordinateData);
        }
        if (this.normalData != null) {
            this.normalBuffer = toBuffer(this.normalData);
        }
    }
    public static FloatBuffer toBuffer(float[] data){
        // Initialize a ByteBuffer for the data
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(data.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        // Create a FloatBuffer from the ByteBuffer
        FloatBuffer buffer = byteBuffer.asFloatBuffer();
        // Put the data into the FloatBuffer
        buffer.put(data);
        // Set the position to the beginning
        buffer.position(0);
        return buffer;
    }
    static float [] createData (float[] coords, short[] order, int stride) {
        float [] data = new float[order.length * stride];
        for (int i = 0; i < order.length; i++){
            short index = order[i];
            for (int j = 0; j < stride; j++){
                data[i * stride + j] = coords[index * stride + j];
            }
        }
        return data;
    }
    public static int loadTexture(final Context context, final int resourceId) {
        final int[] textureHandle = new int[1];
        GLES30.glGenTextures(1, textureHandle, 0);
        if (textureHandle[0] != 0) {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false; // No pre-scaling
            // Load the bitmap from resources
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
            if (bitmap == null) {
                throw new RuntimeException("Error loading texture.");
            }
            // Bind to the texture in OpenGL
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureHandle[0]);
            // Set filtering
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0);
            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }
        else {
            throw new RuntimeException("Error loading texture.");
        }
        return textureHandle[0];
    }
    public MaterialFileHandle readMaterialFile (InputStream mtlFile){
        BufferedReader reader = new BufferedReader(new InputStreamReader(mtlFile));
        // HashMap<String, Material> mtls = new HashMap<>();
        MaterialFileHandle curr = null;
        String line;
        while (true) {
            try {
                if((line = reader.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String [] tokens = line.split(" ");
            if (tokens[0].equals("newmtl")){
                curr = new MaterialFileHandle(tokens[1]);
            }
            else if (tokens[0].equals("Ka")){
                curr.Ka = new float [] {
                        Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])
                };
            }
            else if (tokens[0].equals("Kd")){
                curr.Kd = new float [] {
                        Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])
                };
            }
            else if (tokens[0].equals("Ks")){
                curr.Ks = new float [] {
                        Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3])
                };
            }
            else if (tokens[0].equals("Ns")){
                curr.Ns = Float.parseFloat(tokens[1]);
            }
            else if (tokens[0].equals("map_Kd")){
                String texName = tokens[1];
                int dotIndex = texName.lastIndexOf('.');
                if (dotIndex > 0){
                    texName = texName.substring(0, dotIndex);
                }
                curr.resourceId = this.context.getResources().getIdentifier(texName, "drawable", "com.example.computergraphics");
                curr.textureHandle = this.loadTexture(this.context, curr.resourceId);
            }
            else if (tokens[0].equals("#")){
                continue;
            }
            else{
                continue;
            }
        }
        return curr;
    }
    public List<float[][]> getIntersectionsWithLine(Line line){
        List<float[][]> intersections = new ArrayList<>();
        float [] worldVertices = getWorldVertices();
        for (int i=0; i<worldVertices.length; i += 3 * COORDS_PER_VERTEX){
            float [] v0 = {worldVertices[i  ], worldVertices[i+1], worldVertices[i+2]};
            float [] v1 = {worldVertices[i+3], worldVertices[i+4], worldVertices[i+5]};
            float [] v2 = {worldVertices[i+6], worldVertices[i+7], worldVertices[i+8]};
            float [] v0v2temp = MatrixUtils.sub(v2, v0);
            float [] v0v1temp = MatrixUtils.sub(v1, v0);
            float [] normal = MatrixUtils.normalize(MatrixUtils.crossProduct(v0v1temp, v0v2temp));
            // Log.d("tag", "v "   + v0[0] + " " + v0[1] + " " + v0[2] + " "
            //                     + v1[0] + " " + v1[1] + " " + v1[2] + " "
            //                     + v2[0] + " " + v2[1] + " " + v2[2] + " ");
            float D = - MatrixUtils.MM(normal, v0);
            float NR = MatrixUtils.MM(normal, line.direction);
            if (Math.abs(NR) < 1e-6){
                Line l0 = new Line(v1, v2, context);
                Line l1 = new Line(v2, v0, context);
                Line l2 = new Line(v0, v1, context);
                float [] p0 = line.getIntersectionWithLine(l0);
                float [] p1 = line.getIntersectionWithLine(l1);
                float [] p2 = line.getIntersectionWithLine(l2);
                List<float[]> points = new ArrayList<>();
                if (p0 != null && (p0[0] - v1[0]) * (p0[0] - v2[0]) - 1e-6 < 0){
                    points.add(p0);
                }
                if (p1 != null && (p1[0] - v2[0]) * (p1[0] - v0[0]) - 1e-6 < 0){
                    points.add(p1);
                }
                if (p2 != null && (p2[0] - v0[0]) * (p2[0] - v1[0]) - 1e-6 < 0){
                    points.add(p2);
                }
                if (points.size() != 2) continue;
                intersections.add(new float[][]{
                    points.get(0), points.get(1)
                });
            }
            else {
                float t = - (MatrixUtils.MM(normal, line.source) + D) / NR;
                if (t < 0){
                    continue;
                }
                float [] p = MatrixUtils.add(line.source, MatrixUtils.mul(line.direction, t));
                float [] v0v1 = MatrixUtils.sub(v1, v0);
                float [] v1v2 = MatrixUtils.sub(v2, v1);
                float [] v2v0 = MatrixUtils.sub(v0, v2);
                float [] v0p = MatrixUtils.sub(p, v0);
                float [] v1p = MatrixUtils.sub(p, v1);
                float [] v2p = MatrixUtils.sub(p, v2);
                float n1 = MatrixUtils.MM(normal, MatrixUtils.crossProduct(v0v1, v0p));
                float n2 = MatrixUtils.MM(normal, MatrixUtils.crossProduct(v1v2, v1p));
                float n3 = MatrixUtils.MM(normal, MatrixUtils.crossProduct(v2v0, v2p));
                if (n1 * n2 >= 0 && n1 * n3 >= 0){
                    isIntersected = true;
                    intersections.add(new float[][] {p});
                }
            }
        }
        return intersections;
    }
    public void setTranslation(float [] translation){
        this.translation = translation;
    }
    public void setRotation(float[] rotation){
        this.rotation = rotation;
    }
    public void setScale(float[] scale){
        this.scale = scale;
    }
    public float [] getWorldVertices(){
        float [] modelMatrix = Utils.getModelMatrix(translation, rotation, scale);
        float [] worldVertices = new float[vertexData.length];
        for(int i=0; i<vertexData.length; i+=3){
            float [] vertex = new float[] {vertexData[i], vertexData[i+1], vertexData[i+2], 1};
            Matrix.multiplyMV(vertex, 0, modelMatrix, 0, vertex, 0);
            System.arraycopy(vertex, 0, worldVertices, i, 3);
        }
        return worldVertices;
    }
    public float[] getColor() {
        return color;
    }
}
