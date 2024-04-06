package com.yallo.dual_contouring.Resources;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.function.Function;

public class SDFs {
    public static SDF getSphere(float radius) {
        return new SDF(new Function<Vector3f, Float>() {
            @Override
            public Float apply(Vector3f v) {
                return v.length() - radius;
            }
        });
    }

    public static SDF getTorus(Vector2f t) {
        return new SDF(new Function<Vector3f, Float>() {
            @Override
            public Float apply(Vector3f v) {
                Vector2f q = new Vector2f(new Vector3f(v.x, 0, v.z).length() - t.x, v.y);
                return q.length() - t.y;
            }
        });
    }

    public static SDF getBox(Vector3f b) {
        return new SDF(new Function<Vector3f, Float>() {
            @Override
            public Float apply(Vector3f v) {
                Vector3f q = v.absolute().sub(b);
                return (q.max(new Vector3f(0, 0, 0))).length() + Math.min(Math.max(q.x, Math.max(q.y, q.z)), -0.00001f);
            }
        });
    }

    public static SDF getPlane(float h) {
        return new SDF(new Function<Vector3f, Float>() {
            @Override
            public Float apply(Vector3f v) {
                return v.y - h;
            }
        });
    }
}
