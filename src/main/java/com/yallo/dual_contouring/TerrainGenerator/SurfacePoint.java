package com.yallo.dual_contouring.TerrainGenerator;

import org.joml.Vector3f;

public class SurfacePoint {
    public Vector3f position;
    public Vector3f normal;

    public SurfacePoint(Vector3f position, Vector3f normal) {
        this.position = position;
        this.normal = normal;
    }
}
