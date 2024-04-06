package com.yallo.dual_contouring.TerrainGenerator;

import org.joml.Vector3f;

public class SampleValue {
    public float distance;
    public Vector3f normal;

    public SampleValue(float distance, Vector3f normal) {
        this.distance = distance;
        this.normal = normal;
    }

    public static SampleValue min(SampleValue s1, SampleValue s2) {
        return s1.distance < s2.distance ? s1 : s2;
    }
    public static SampleValue maxn(SampleValue s1, SampleValue s2) {
        return s1.distance > -s2.distance ? s1 : new SampleValue(-s2.distance, new Vector3f(s2.normal).negate());
    }
}
