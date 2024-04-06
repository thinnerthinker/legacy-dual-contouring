package com.yallo.dual_contouring.TerrainEditor;

import com.yallo.dual_contouring.Resources.SDF;
import com.yallo.dual_contouring.TerrainGenerator.ScalarFieldSample;

public class Brush {
    protected SDF sdf;
    protected ScalarFieldSample sfs;

    protected int resolution, maxRes;

    public Brush(float length, int resolution, int maxRes, SDF sdf) {
        this.resolution = resolution;
        this.maxRes = maxRes;
        this.sdf = sdf;
        this.sfs = new ScalarFieldSample(length, resolution, sdf);
    }

    public SDF getSdf() {
        return sdf;
    }

    public ScalarFieldSample getSfs() {
        return sfs;
    }

    public void resample(int resDiff) {

    }

    public float length() {
        return sfs.getLength();
    }

    public int getResolution() {
        return resolution;
    }
}
