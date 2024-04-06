package com.yallo.dual_contouring.TerrainGenerator;

import org.joml.Vector3f;

public class Triangle {
    public Vector3f v1, v2, v3;
    public Vector3f normal;

    public Triangle(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f normal) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.normal = normal;
    }
    public Triangle(Vector3f v1, Vector3f v2, Vector3f v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.normal = new Vector3f(v1).sub(v2).cross(new Vector3f(v1).sub(v3)).normalize();
    }
}
