package com.yallo.dual_contouring.TerrainEditor;

import org.joml.Vector3f;

public class RayPoint {
    public float t;
    public Vector3f p;

    public RayPoint(float t, Vector3f p) {
        this.t = t;
        this.p = p;
    }
}
