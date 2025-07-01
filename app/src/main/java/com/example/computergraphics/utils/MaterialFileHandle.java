package com.example.computergraphics.utils;

public class MaterialFileHandle {
    public String name;
    public float [] Ka = { 0.5f, 0.5f, 0.5f };
    public float [] Kd = { 0.5f, 0.5f, 0.5f };
    public float [] Ks = { 0.5f, 0.5f, 0.5f };
    public float Ns = 0.0f;
    public int resourceId = -1;
    public int textureHandle = -1;
    public MaterialFileHandle(String name) {
        this.name = name;
    }
}
