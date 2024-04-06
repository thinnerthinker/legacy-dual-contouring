package com.yallo.dual_contouring.TerrainEditor;

import com.yallo.dual_contouring.TerrainGenerator.ScalarFieldSample;
import com.yallo.dual_contouring.Resources.SDFs;
import org.joml.Math;

public class SphereBrush extends Brush {
    private float radius;

    public SphereBrush(float length, int resolution, int maxRes, float radius) {
        super(length, resolution, maxRes, SDFs.getSphere(radius));
        this.radius = radius;
    }

    public void resample(int resDiff) {
        int newres = Math.clamp(resolution + resDiff, 1, maxRes);

        radius = newres * radius / resolution;
        sdf = SDFs.getSphere(radius);
        sfs = new ScalarFieldSample(newres * sfs.getLength() / resolution, newres, sdf);

        resolution = newres;
    }

    public float getRadius() {
        return radius;
    }
}
