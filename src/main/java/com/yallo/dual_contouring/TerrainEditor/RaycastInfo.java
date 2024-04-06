package com.yallo.dual_contouring.TerrainEditor;

import org.joml.Vector3f;

public class RaycastInfo {
    public int px, py, pz;
    public boolean hit;

    public RaycastInfo() {
        hit = false;
    }

    public RaycastInfo(int px, int py, int pz) {
        this.px = px;
        this.py = py;
        this.pz = pz;
        this.hit = true;
    }
}
