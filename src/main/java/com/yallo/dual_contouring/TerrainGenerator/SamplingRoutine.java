package com.yallo.dual_contouring.TerrainGenerator;

import com.yallo.dual_contouring.Resources.SDF;
import org.joml.Vector3f;

public class SamplingRoutine implements Runnable {
    private SampleValue[][][] vertices;
    private ScalarFieldSample sfs;
    private SDF sdf;
    private int x1, y1, z1, x2, y2, z2;

    public SamplingRoutine(SampleValue[][][] vertices, ScalarFieldSample sfs, SDF sdf, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.vertices = vertices;
        this.sfs = sfs;
        this.sdf = sdf;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    @Override
    public void run() {
        //sample the sdf at each vertex
        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2; y++) {
                for (int z = y1; z < y2; z++) {
                    vertices[x][y][z] = sdf.getSample(sfs.transform(new Vector3f(x, y, z)));
                    if (vertices[x][y][z].distance == 0) vertices[x][y][z].distance = 0.000001f;
                }
            }
        }
    }
}
