package com.yallo.dual_contouring.Util;

import org.joml.Math;
import org.joml.Vector3f;

public class MathUtil {
    public static Vector3f normalize(Vector3f v) {
        float ms = v.dot(v);
        if (ms < 0.00001f) {
            return v;
        }

        return v.mul(Math.invsqrt(ms));
    }

    public static boolean insideCube(Vector3f v, Vector3f c1, Vector3f c2) {
        return v.x >= c1.x && v.y >= c1.y && v.z >= c1.z && v.x <= c2.x && v.y <= c2.y && v.z <= c2.z;
    }
}
