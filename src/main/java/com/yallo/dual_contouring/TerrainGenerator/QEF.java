package com.yallo.dual_contouring.TerrainGenerator;

import Jama.Matrix;
import org.joml.Vector3f;

import java.util.ArrayList;

//quadratic error function
public class QEF {
    private final double[] aTa;
    private final Vector3f aTb;
    private final float bTb;

    private final boolean fixedX, fixedY, fixedZ;
    private float fixedXv, fixedYv, fixedZv;

    public QEF(ArrayList<SurfacePoint> points) {
        float xx = 0, xy = 0, xz = 0, yy = 0, yz = 0, zz = 0;
        float npx = 0, npy = 0, npz = 0;
        float nps = 0;

        for (var point : points) {
            //discard malformed normals
            if (Float.valueOf(point.normal.x).isNaN() || Float.valueOf(point.normal.y).isNaN() || Float.valueOf(point.normal.z).isNaN()) {
                point.normal = new Vector3f(0, 0, 0);
            }

            xx += point.normal.x * point.normal.x;
            xy += point.normal.x * point.normal.y;
            xz += point.normal.x * point.normal.z;
            yy += point.normal.y * point.normal.y;
            yz += point.normal.y * point.normal.z;
            zz += point.normal.z * point.normal.z;

            float np = point.position.dot(point.normal);
            npx += point.normal.x * np;
            npy += point.normal.y * np;
            npz += point.normal.z * np;

            nps += np * np;
        }

        aTa = new double[] {
            xx, xy, xz,
            xy, yy, yz,
            xz, yz, zz
        };

        aTb = new Vector3f(npx, npy, npz);
        bTb = nps;

        fixedX = false;
        fixedY = false;
        fixedZ = false;
    }

    public QEF(double[] aTa, Vector3f aTb, float bTb, boolean fixedX, boolean fixedY, boolean fixedZ) {
        this.aTa = aTa;
        this.aTb = aTb;
        this.bTb = bTb;

        this.fixedX = fixedX;
        this.fixedY = fixedY;
        this.fixedZ = fixedZ;
    }

    public float apply(Vector3f v) {
        Vector3f a1 = new Vector3f((float)aTa[0], (float)aTa[1], (float)aTa[2]),
            a2 = new Vector3f((float)aTa[3], (float)aTa[4], (float)aTa[5]),
            a3 = new Vector3f((float)aTa[6], (float)aTa[7], (float)aTa[8]);

        return new Vector3f(v.dot(a1), v.dot(a2), v.dot(a3)).dot(v) - 2 * v.dot(aTb) + bTb;
    }

    public QEF fixX(float v) {
        fixedXv = v;

        int oldlen = 3;
        if (fixedY) {
            oldlen--;
        }
        if (fixedZ) {
            oldlen--;
        }

        double[] aTaf = new double[(oldlen - 1) * (oldlen - 1)];

        //exclude the row and column corresponding to X
        int ind = 0;
        for (int i = 1; i < oldlen; i++) {
            for (int j = 1; j < oldlen; j++) {
                aTaf[ind++] = aTa[i * oldlen + j];
            }
        }

        Vector3f aTbf = new Vector3f(aTb);

        //account for the dropped column in aTb
        //first get the rows for the remaining axes
        int yrow = oldlen - 1, zrow = oldlen - 1;
        if (!fixedZ) {
            yrow--;
        }

        //then subtract the values dropped from the column, multiplied by the new fixed value
        if (!fixedY) {
            aTbf.y -= (float) (v * aTa[yrow * oldlen]);
        }
        if (!fixedZ) {
            aTbf.z -= (float) (v * aTa[zrow * oldlen]);
        }

        return new QEF(aTaf, aTbf, bTb, true, fixedY, fixedZ);
    }

    public QEF fixY(float v) {
        fixedYv = v;

        int oldlen = 3;
        int exclude = 1;

        if (fixedX) {
            oldlen--;
            exclude--;
        }
        if (fixedZ) {
            oldlen--;
        }

        double[] aTaf = new double[(oldlen - 1) * (oldlen - 1)];

        //exclude the row and column corresponding to Y
        int ind = 0;
        for (int i = 0; i < oldlen; i++) {
            for (int j = 0; j < oldlen; j++) {
                if (i == exclude || j == exclude) continue;

                aTaf[ind++] = aTa[i * oldlen + j];
            }
        }

        Vector3f aTbf = new Vector3f(aTb);

        //account for the dropped column in aTb
        if (!fixedX) aTbf.x -= (float) (v * aTa[exclude]);
        if (!fixedZ) aTbf.z -= (float) (v * aTa[(oldlen - 1) * oldlen + exclude]);

        return new QEF(aTaf, aTbf, bTb, fixedX, true, fixedZ);
    }

    public QEF fixZ(float v) {
        fixedZv = v;

        int oldlen = 3;

        if (fixedX) {
            oldlen--;
        }
        if (fixedY) {
            oldlen--;
        }

        double[] aTaf = new double[(oldlen - 1) * (oldlen - 1)];

        //exclude the row and column corresponding to Z
        int ind = 0;
        for (int i = 0; i < oldlen - 1; i++) {
            for (int j = 0; j < oldlen - 1; j++) {
                aTaf[ind++] = aTa[i * oldlen + j];
            }
        }

        Vector3f aTbf = new Vector3f(aTb);

        //account for the dropped column in aTb
        //first get the rows for the remaining axes
        int yrow = 0;
        if (!fixedX) yrow++;

        //then subtract the values dropped from the column, multiplied by the new fixed value
        if (!fixedX) aTbf.x -= (float) (v * aTa[oldlen - 1]);
        if (!fixedY) aTbf.y -= (float) (v * aTa[yrow * oldlen + oldlen - 1]);

        return new QEF(aTaf, aTbf, bTb, fixedX, fixedY, true);
    }

    public Vector3f solve() {
        int len = 3;
        if (fixedX) len--;
        if (fixedY) len--;
        if (fixedZ) len--;

        var a = new Matrix(aTa, len);
        var svd = a.eig();

        //transform the eigenvalues to form the inverse
        var d = svd.getD();
        for (int i = 0; i < len; i++) {
            d.set(i, i, invert(d.get(i, i)));
        }

        //calculate pseudoinverse
        Matrix pinv = svd.getV().times(d).times(svd.getV().transpose());

        //transform aTb into column matrix
        double[][] rows = new double[][] { {aTb.x}, {aTb.y}, {aTb.z} };

        if (fixedY) {
            rows[1][0] = rows[2][0];
        }
        if (fixedX) {
            rows[0][0] = rows[1][0];
            rows[1][0] = rows[2][0];
        }

        //calculate result (pinv * aTb)
        Matrix aTbm = new Matrix(rows, len, 1);
        Matrix res = pinv.times(aTbm);

        //store the results into a vector
        int yind = 1, zind = 2;
        if (fixedX) {
            yind--;
            zind--;
        }
        if (fixedY) {
            zind--;
        }

        Vector3f resv = new Vector3f();
        if (!fixedX) resv.x = (float)res.get(0, 0);
        else resv.x = fixedXv;
        if (!fixedY) resv.y = (float)res.get(yind, 0);
        else resv.y = fixedYv;
        if (!fixedZ) resv.z = (float)res.get(zind, 0);
        else resv.z = fixedZv;

        return resv;
    }

    private double invert(double x) {
        //check for rank deficiency
        if (x < 0.00000001) {
            return 0;
        }

        return 1 / x;
    }
}
