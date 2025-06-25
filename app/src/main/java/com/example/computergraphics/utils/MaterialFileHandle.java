package com.example.computergraphics.utils;

public class MaterialFileHandle {
    public String name;
    public float [] Ka = { 1.0f, 1.0f, 1.0f };
    public float [] Kd = { 1.0f, 1.0f, 1.0f };
    public float [] Ks = { 1.0f, 1.0f, 1.0f };
    public float Ns = 0.0f;
    public int resourceId = -1;
    public int textureHandle = -1;
    public MaterialFileHandle(String name) {
        this.name = name;
    }
}
