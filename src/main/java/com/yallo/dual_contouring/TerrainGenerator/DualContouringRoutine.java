package com.yallo.dual_contouring.TerrainGenerator;

import com.yallo.dual_contouring.Util.MathUtil;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.stream.Stream;

public class DualContouringRoutine implements Runnable {
    private ScalarFieldSample sfs;
    private Vector3f[][][] vertices;
    private int x1, y1, z1, x2, y2, z2;

    public DualContouringRoutine(ScalarFieldSample sfs, Vector3f[][][] vertices, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.sfs = sfs;
        this.vertices = vertices;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    @Override
    public void run() {
        var edgesX = sfs.getEdgesX();
        var edgesY = sfs.getEdgesY();
        var edgesZ = sfs.getEdgesZ();

        //create a vertex inside each cube that is part of the surface
        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2; y++) {
                for (int z = z1; z < z2; z++) {
                    ArrayList<SurfacePoint> points = new ArrayList<>();

                    //get the surface point(and its normal) of each edge that is part of the surface
                    for (int c1 = 0; c1 <= 1; c1++) {
                        for (int c2 = 0; c2 <= 1; c2++) {
                            if (edgesX[x][y + c1][z + c2])
                                points.add(sfs.getSurfaceNormalX(x, y + c1, z + c2));
                            if (edgesY[x + c1][y][z + c2])
                                points.add(sfs.getSurfaceNormalY(x + c1, y, z + c2));
                            if (edgesZ[x + c1][y + c2][z])
                                points.add(sfs.getSurfaceNormalZ(x + c1, y + c2, z));
                        }
                    }

                    //if there are any surface points, the cube itself is part of the surface
                    //find the vertex that corresponds to the normals the most and add it to the cube
                    if (!points.isEmpty()) {
                        Vector3f vertPos = optimalVertex(points, new Vector3f(x, y, z), new Vector3f(x + 1, y + 1, z + 1));
                        vertices[x][y][z] = sfs.transform(vertPos);
                    }
                }
            }
        }
    }

    //calculate the optimal vertex in a cube defined by its 2 corners
    private Vector3f optimalVertex(ArrayList<SurfacePoint> points, Vector3f c1, Vector3f c2) {
        //make a qef based on the distance to the normals of the points
        QEF cube = new QEF(points);

        //solve in the cube
        Vector3f v = cube.solve();

        //if the solution is inside the cube, return it
        if (MathUtil.insideCube(v, c1, c2)) {
            return v;
        }

        //otherwise, solve on each face
        QEF x1 = cube.fixX(c1.x);
        QEF x2 = cube.fixX(c2.x);
        QEF y1 = cube.fixY(c1.y);
        QEF y2 = cube.fixY(c2.y);
        QEF z1 = cube.fixZ(c1.z);
        QEF z2 = cube.fixZ(c2.z);

        //choose the one with minimal error according to the qef
        v = Stream.of(x1, x2, y1, y2, z1, z2).map(QEF::solve).filter(x -> MathUtil.insideCube(x, c1, c2)).min((x, y) -> Float.compare(cube.apply(x), cube.apply(y))).orElse(null);

        //if none found, all of them was outside the cube
        if (v != null) {
            return v;
        }

        //solve on each edge
        QEF xy11 = x1.fixY(c1.y);
        QEF xy21 = x2.fixY(c1.y);
        QEF yz11 = y1.fixZ(c1.z);
        QEF yz21 = y2.fixZ(c1.z);
        QEF zx11 = z1.fixX(c1.x);
        QEF zx21 = z2.fixX(c1.x);
        QEF xy12 = x1.fixY(c2.y);
        QEF xy22 = x2.fixY(c2.y);
        QEF yz12 = y1.fixZ(c2.z);
        QEF yz22 = y2.fixZ(c2.z);
        QEF zx12 = z1.fixX(c2.x);
        QEF zx22 = z2.fixX(c2.x);

        v = Stream.of(xy11, xy21, yz11, yz21, zx11, zx21, xy12, xy22, yz12, yz22, zx12, zx22).map(QEF::solve).filter(x -> MathUtil.insideCube(x, c1, c2)).min((x, y) -> Float.compare(cube.apply(x), cube.apply(y))).orElse(null);

        if (v != null) {
            return v;
        }

        //otherwise, choose the corner with minimal error
        Vector3f c111 = new Vector3f(c1.x, c1.y, c1.z);
        Vector3f c112 = new Vector3f(c1.x, c1.y, c2.z);
        Vector3f c121 = new Vector3f(c1.x, c2.y, c1.z);
        Vector3f c122 = new Vector3f(c1.x, c2.y, c2.z);
        Vector3f c211 = new Vector3f(c2.x, c1.y, c1.z);
        Vector3f c212 = new Vector3f(c2.x, c1.y, c2.z);
        Vector3f c221 = new Vector3f(c2.x, c2.y, c1.z);
        Vector3f c222 = new Vector3f(c2.x, c2.y, c2.z);

        return Stream.of(c111, c112, c121, c122, c211, c212, c221, c222).min((x, y) -> Float.compare(cube.apply(x), cube.apply(y))).get();
    }
}
