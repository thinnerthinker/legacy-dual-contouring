package com.yallo.dual_contouring.Resources;

import com.yallo.dual_contouring.Util.MathUtil;
import com.yallo.dual_contouring.TerrainGenerator.SampleValue;
import org.joml.Vector3f;

import java.util.function.Function;

//signed distance function
public class SDF {
    private Function<Vector3f, Float> sdf;
    private Function<Vector3f, Vector3f> grad;

    public SDF(Function<Vector3f, Float> sdf) {
        this.sdf = sdf;

        grad = new Function<Vector3f, Vector3f>() {
            @Override
            public Vector3f apply(Vector3f v) {
                float eps = 0.001f;
                var x = new Vector3f((sdf.apply(new Vector3f(v.x + eps, v.y, v.z)) - sdf.apply(new Vector3f(v.x - eps, v.y, v.z))) / (2 * eps),
                        (sdf.apply(new Vector3f(v.x, v.y + eps, v.z)) - sdf.apply(new Vector3f(v.x, v.y - eps, v.z))) / (2 * eps),
                        (sdf.apply(new Vector3f(v.x, v.y, v.z + eps)) - sdf.apply(new Vector3f(v.x, v.y, v.z - eps))) / (2 * eps));

                return x;
            }
        };
    }

    public float apply(Vector3f v) {
        return sdf.apply(v);
    }

    public Vector3f applyGrad(Vector3f v) {
        return grad.apply(v);
    }

    public Vector3f getNormal(Vector3f v) {
        return MathUtil.normalize(grad.apply(v));
    }

    public SampleValue getSample(Vector3f v) {
        return new SampleValue(sdf.apply(v), getNormal(v));
    }
}
