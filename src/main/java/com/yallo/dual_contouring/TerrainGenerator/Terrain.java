package com.yallo.dual_contouring.TerrainGenerator;

import com.yallo.dual_contouring.Game.Camera;
import com.yallo.dual_contouring.Resources.Mesh;
import com.yallo.dual_contouring.Resources.Resources;
import com.yallo.dual_contouring.Resources.Shader;
import com.yallo.dual_contouring.TerrainEditor.Brush;
import com.yallo.dual_contouring.TerrainEditor.DynMesh;
import com.yallo.dual_contouring.TerrainEditor.RayPoint;
import com.yallo.dual_contouring.TerrainEditor.RaycastInfo;
import com.yallo.dual_contouring.Util.MathUtil;
import com.yallo.dual_contouring.Resources.*;
import com.yallo.dual_contouring.TerrainEditor.*;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.stream.Stream;

public class Terrain {
    private final ScalarFieldSample sfs;
    private final Vector3f[][][] vertices;
    private final ArrayList<Triangle> triangles;

    private DynMesh mesh;

    private final int size;
    private final float scale;

    public Terrain(ScalarFieldSample scalarFieldSample) {
        sfs = scalarFieldSample;
        size = scalarFieldSample.getSize();
        scale = scalarFieldSample.getScale();
        vertices = new Vector3f[size - 1][size - 1][size - 1];
        triangles = new ArrayList<>();

        dualContouring();
        calculateMesh();
    }

    public void draw(Camera camera) {
        Shader shader = Resources.getTerrainShader();
        shader.bind();

        shader.setUniform("viewProj", camera.getViewProjection());
        mesh.draw();

        shader.unbind();
    }

    private void dualContouring() {
        dualContouring(0, 0, 0, size - 1, size - 1, size - 1);
    }

    //perform dual contouring on a subsection specified by its 2 corners
    private void dualContouring(int x1, int y1, int z1, int x2, int y2, int z2) {
        int threads;

        int d = x2 - x1;
        if (d <= 15) {
            threads = 1;
        }
        else if (d <= 25) {
            threads = 2;
        }
        else if (d <= 50) {
            threads = 4;
        }
        else {
            threads = 8;
        }

        //split the work between the threads
        d /= threads;

        ArrayList<Thread> dcrs = new ArrayList<>();
        for (int i = 0; i < threads - 1; i++) {
            var dcr = new Thread(new DualContouringRoutine(sfs, vertices, x1 + i * d, y1, z1, x1 + (i + 1) * d, y2, z2));
            dcrs.add(dcr);
            dcr.start();
        }
        var dcr = new Thread(new DualContouringRoutine(sfs, vertices, x1 + (threads - 1) * d, y1, z1, x2, y2, z2));
        dcrs.add(dcr);
        dcr.start();

        for (Thread t : dcrs) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        var edgesX = sfs.getEdgesX();
        var edgesY = sfs.getEdgesY();
        var edgesZ = sfs.getEdgesZ();

        //add a quad for each edge that is part of the surface(the quad's vertices come from the 4 neighboring cubes of the edge)
        triangles.clear();
        for (int x = 0; x < size - 1; x++) {
            for (int y = 1; y < size - 2; y++) {
                for (int z = 1; z < size - 2; z++) {
                    if (edgesX[x][y][z]) {
                        AddQuadX(x, y, z);
                    }
                }
            }
        }
        for (int x = 1; x < size - 2; x++) {
            for (int y = 0; y < size - 1; y++) {
                for (int z = 1; z < size - 2; z++) {
                    if (edgesY[x][y][z]) {
                        AddQuadY(x, y, z);
                    }
                }
            }
        }
        for (int x = 1; x < size - 2; x++) {
            for (int y = 1; y < size - 2; y++) {
                for (int z = 0; z < size - 1; z++) {
                    if (edgesZ[x][y][z]) {
                        AddQuadZ(x, y, z);
                    }
                }
            }
        }
    }

    private void AddQuadX(int x, int y, int z) {
        if (sfs.getVertex(x, y, z) < 0) {
            triangles.add(new Triangle(vertices[x][y][z], vertices[x][y - 1][z], vertices[x][y][z - 1]));
            triangles.add(new Triangle(vertices[x][y][z - 1], vertices[x][y - 1][z], vertices[x][y - 1][z - 1]));
        } else {
            triangles.add(new Triangle(vertices[x][y][z - 1], vertices[x][y - 1][z], vertices[x][y][z]));
            triangles.add(new Triangle(vertices[x][y - 1][z - 1], vertices[x][y - 1][z], vertices[x][y][z - 1]));
        }
    }

    private void AddQuadY(int x, int y, int z) {
        if (sfs.getVertex(x, y, z) > 0) {
            triangles.add(new Triangle(vertices[x][y][z], vertices[x - 1][y][z], vertices[x][y][z - 1]));
            triangles.add(new Triangle(vertices[x][y][z - 1], vertices[x - 1][y][z], vertices[x - 1][y][z - 1]));
        } else {
            triangles.add(new Triangle(vertices[x][y][z - 1], vertices[x - 1][y][z], vertices[x][y][z]));
            triangles.add(new Triangle(vertices[x - 1][y][z - 1], vertices[x - 1][y][z], vertices[x][y][z - 1]));
        }
    }

    private void AddQuadZ(int x, int y, int z) {
        if (sfs.getVertex(x, y, z) < 0) {
            triangles.add(new Triangle(vertices[x][y][z], vertices[x - 1][y][z], vertices[x][y - 1][z]));
            triangles.add(new Triangle(vertices[x][y - 1][z], vertices[x - 1][y][z], vertices[x - 1][y - 1][z]));
        } else {
            triangles.add(new Triangle(vertices[x][y - 1][z], vertices[x - 1][y][z], vertices[x][y][z]));
            triangles.add(new Triangle(vertices[x - 1][y - 1][z], vertices[x - 1][y][z], vertices[x][y - 1][z]));
        }
    }


    private void calculateMesh() {
        if (mesh != null) mesh.update(verticesToArray(), normalsToArray());
        else mesh = new DynMesh(verticesToArray(), normalsToArray(), 6 * size * size * size);
    }

    private float[] verticesToArray() {
        float[] mVertices = new float[9 * triangles.size()];

        int index = 0;
        for (Triangle t : triangles) {
            mVertices[index++] = t.v1.x;
            mVertices[index++] = t.v1.y;
            mVertices[index++] = t.v1.z;

            mVertices[index++] = t.v2.x;
            mVertices[index++] = t.v2.y;
            mVertices[index++] = t.v2.z;

            mVertices[index++] = t.v3.x;
            mVertices[index++] = t.v3.y;
            mVertices[index++] = t.v3.z;
        }

        return mVertices;
    }

    private float[] normalsToArray() {
        float[] mNormals = new float[9 * triangles.size()];

        int index = 0;
        for (Triangle t : triangles) {
            for (int i = 0; i < 3; i++) {
                mNormals[index++] = t.normal.x;
                mNormals[index++] = t.normal.y;
                mNormals[index++] = t.normal.z;
            }
        }

        return mNormals;
    }


    public String meshToString() {
        StringBuilder result = new StringBuilder();

        float[] mVertices = verticesToArray();
        float[] mNormals = normalsToArray();

        for (int i = 0; i < triangles.size(); i++) {
            for (int j = 0; j < 3; j++) {
                result.append(mVertices[i]);
                result.append(' ');
            }
            result.append('\n');

            for (int j = 0; j < 3; j++) {
                result.append(mNormals[i]);
                result.append(' ');
            }
            result.append('\n');
        }

        return result.toString();
    }


    public Mesh getMesh() {
        return mesh;
    }


    public void add(Brush brush, int cx, int cy, int cz) {
        sfs.add(brush, cx, cy, cz);

        int hs = brush.getSfs().getSize() / 2;

        int x1 = Math.min(Math.max(cx - hs, 0), size - 1);
        int x2 = Math.min(Math.max(cx + hs, 0), size - 1);
        int y1 = Math.min(Math.max(cy - hs, 0), size - 1);
        int y2 = Math.min(Math.max(cy + hs, 0), size - 1);
        int z1 = Math.min(Math.max(cz - hs, 0), size - 1);
        int z2 = Math.min(Math.max(cz + hs, 0), size - 1);

        dualContouring(x1, y1, z1, x2, y2, z2);
        calculateMesh();
    }

    public void subtract(Brush brush, int cx, int cy, int cz) {
        sfs.subtract(brush, cx, cy, cz);

        int hs = brush.getSfs().getSize() / 2;

        int x1 = Math.min(Math.max(cx - hs, 0), size - 1);
        int x2 = Math.min(Math.max(cx + hs, 0), size - 1);
        int y1 = Math.min(Math.max(cy - hs, 0), size - 1);
        int y2 = Math.min(Math.max(cy + hs, 0), size - 1);
        int z1 = Math.min(Math.max(cz - hs, 0), size - 1);
        int z2 = Math.min(Math.max(cz + hs, 0), size - 1);

        dualContouring(x1, y1, z1, x2, y2, z2);
        calculateMesh();
    }

    //find the vertex a given ray intersects with
    public RaycastInfo raycast(Vector3f start, Vector3f dir) {
        float len = sfs.getLength() / 2;
        Vector3f c1 = new Vector3f(-len, -len, -len);
        Vector3f c2 = new Vector3f(len, len, len);

        Vector3f p;
        if (!MathUtil.insideCube(start, c1, c2)) {
            //get the cell the ray hits when it enters the sf
            RayPoint rp = Stream.of((c1.x - start.x) / dir.x, (c1.y - start.y) / dir.y, (c1.z - start.z) / dir.z, (c2.x - start.x) / dir.x, (c2.y - start.y) / dir.y, (c2.z - start.z) / dir.z)
                    .filter(t -> t > 0).map(t -> new RayPoint(t, new Vector3f(start).add(new Vector3f(dir).mul(t)))).filter(v -> MathUtil.insideCube(v.p, c1, c2)).
                            min((x, y) -> Float.compare(x.t, y.t)).orElse(null);

            //if not hitting the sf
            if (rp == null) {
                return new RaycastInfo();
            }

            p = rp.p;
        } else {
            p = new Vector3f(start);
        }

        Vector3f cellp = sfs.transformInv(p);
        int x = (int) cellp.x, y = (int) cellp.y, z = (int) cellp.z;

        var edgesX = sfs.getEdgesX();
        var edgesY = sfs.getEdgesY();
        var edgesZ = sfs.getEdgesZ();

        int stepx = dir.x < 0 ? -1 : 1, stepy = dir.y < 0 ? -1 : 1, stepz = dir.z < 0 ? -1 : 1;
        Vector3f nextp = sfs.transform(new Vector3f(x + stepx, y + stepy, z + stepz));

        float tmaxx = (nextp.x - start.x) / dir.x, tmaxy = (nextp.y - start.y) / dir.y, tmaxz = (nextp.z - start.z) / dir.z;
        float tdeltax = scale / dir.x * stepx, tdeltay = scale / dir.y * stepy, tdeltaz = scale / dir.z * stepz;

        //march through the cubes one by one(always to the closest one)
        while (true) {
            if (tmaxx < tmaxy) {
                if (tmaxx < tmaxz) {
                    x += stepx;
                    tmaxx += tdeltax;
                } else {
                    z += stepz;
                    tmaxz += tdeltaz;
                }
            } else {
                if (tmaxy < tmaxz) {
                    y += stepy;
                    tmaxy += tdeltay;
                } else {
                    z += stepz;
                    tmaxz += tdeltaz;
                }
            }

            //if exited the sf, no hit
            if (x < 0 || y < 0 || z < 0 || x >= size || y >= size || z >= size) {
                return new RaycastInfo();
            }

            //if the cube is part of the surface, hit
            if (x > 0 && y > 0 && z > 0 && x < size - 1 && y < size - 1 && z < size - 1) {
                for (int e1 = 0; e1 <= 1; e1++) {
                    for (int e2 = 0; e2 <= 1; e2++) {
                        if (edgesX[x][y + e1][z + e2] || edgesY[x + e1][y][z + e2] || edgesZ[x + e1][y + e2][z]) {
                            return new RaycastInfo(x, y, z);
                        }
                    }
                }
            }
        }
    }
}
