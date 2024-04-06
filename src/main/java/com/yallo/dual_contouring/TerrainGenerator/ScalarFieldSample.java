package com.yallo.dual_contouring.TerrainGenerator;

import com.yallo.dual_contouring.Resources.SDF;
import com.yallo.dual_contouring.Util.MathUtil;
import com.yallo.dual_contouring.TerrainEditor.Brush;
import org.joml.Vector3f;

import java.util.ArrayList;

public class ScalarFieldSample {
    private SampleValue[][][] vertices;
    private boolean[][][] edgesX;
    private boolean[][][] edgesY;
    private boolean[][][] edgesZ;

    private int size;
    private float scale, length;

    private Vector3f translation;

    public ScalarFieldSample(float length, int resolution, SDF sdf) {
        this.size = resolution + 1;
        this.length = length;
        this.scale = length / (float) resolution;

        translation = new Vector3f(1, 1, 1).mul((this.size - 1) / 2.0f);

        vertices = new SampleValue[this.size][this.size][this.size];
        edgesX = new boolean[this.size - 1][this.size][this.size];
        edgesY = new boolean[this.size][this.size - 1][this.size];
        edgesZ = new boolean[this.size][this.size][this.size - 1];

        int threads = 0;

        int d = this.size;
        if (d <= 15) {
            threads = 1;
        }
        if (d <= 25) {
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

        ArrayList<Thread> srs = new ArrayList<>();
        for (int i = 0; i < threads - 1; i++) {
            var sr = new Thread(new SamplingRoutine(vertices, this, sdf, i * d, 0, 0, (i + 1) * d, size, size));
            srs.add(sr);
            sr.start();
        }
        var sr = new Thread(new SamplingRoutine(vertices, this, sdf, (threads - 1) * d, 0, 0, size, size, size));
        srs.add(sr);
        sr.start();

        for (Thread t : srs) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        updateEdgeSurfaces();
    }

    //load from string
    public ScalarFieldSample(String[] lines) {
        String[] header = lines[0].split(" ");
        size = Integer.parseInt(header[0]);
        scale = Float.parseFloat(header[1]);
        length = Float.parseFloat(header[2]);

        translation = new Vector3f(1, 1, 1).mul((this.size - 1) / 2.0f);

        vertices = new SampleValue[this.size][this.size][this.size];
        edgesX = new boolean[this.size - 1][this.size][this.size];
        edgesY = new boolean[this.size][this.size - 1][this.size];
        edgesZ = new boolean[this.size][this.size][this.size - 1];

        int index = 1;
        for (int x = 0; x < this.size; x++) {
            for (int y = 0; y < this.size; y++) {
                for (int z = 0; z < this.size; z++) {
                    String[] data = lines[index++].split(" ");
                    vertices[x][y][z] = new SampleValue(Float.parseFloat(data[0]),
                            new Vector3f(Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3])));
                }
            }
        }

        updateEdgeSurfaces();
    }

    private void updateEdgeSurfaces() {
        updateEdgeSurfaces(0, 0, 0, size, size, size);
    }

    //update whether each edge is part of the surface or not(in a region)
    private void updateEdgeSurfaces(int x1, int y1, int z1, int x2, int y2, int z2) {
        for (int x = x1; x < x2 - 1; x++) {
            for (int y = y1; y < y2; y++) {
                for (int z = z1; z < z2; z++) {
                    edgesX[x][y][z] = vertices[x][y][z].distance * vertices[x + 1][y][z].distance < 0;
                }
            }
        }

        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2 - 1; y++) {
                for (int z = z1; z < z2; z++) {
                    edgesY[x][y][z] = vertices[x][y][z].distance * vertices[x][y + 1][z].distance < 0;
                }
            }
        }

        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2; y++) {
                for (int z = z1; z < z2 - 1; z++) {
                    edgesZ[x][y][z] = vertices[x][y][z].distance * vertices[x][y][z + 1].distance < 0;
                }
            }
        }
    }


    public Vector3f transform(Vector3f v) {
        return v.sub(translation).mul(scale);
    }

    public Vector3f transformInv(Vector3f v) {
        return v.mul(1.0f / scale).add(translation);
    }

    public float getVertex(int x, int y, int z) {
        return vertices[x][y][z].distance;
    }


    public SurfacePoint getSurfaceNormalX(int x, int y, int z) {
        //linearly interpolate between the points to get a more accurate result
        float t = vertices[x][y][z].distance / (vertices[x][y][z].distance - vertices[x + 1][y][z].distance);

        return new SurfacePoint(new Vector3f(x + t, y, z),
                MathUtil.normalize(new Vector3f(vertices[x][y][z].normal).mul(1 - t).add(new Vector3f(vertices[x + 1][y][z].normal).mul(t))));
    }

    public SurfacePoint getSurfaceNormalY(int x, int y, int z) {
        //linearly interpolate between the points to get a more accurate result
        float t = vertices[x][y][z].distance / (vertices[x][y][z].distance - vertices[x][y + 1][z].distance);

        return new SurfacePoint(new Vector3f(x, y + t, z),
                MathUtil.normalize(new Vector3f(vertices[x][y][z].normal).mul(1 - t).add(new Vector3f(vertices[x][y + 1][z].normal).mul(t))));
    }

    public SurfacePoint getSurfaceNormalZ(int x, int y, int z) {
        //linearly interpolate between the points to get a more accurate result
        float t = vertices[x][y][z].distance / (vertices[x][y][z].distance - vertices[x][y][z + 1].distance);

        return new SurfacePoint(new Vector3f(x, y, z + t),
                MathUtil.normalize(new Vector3f(vertices[x][y][z].normal).mul(1 - t).add(new Vector3f(vertices[x][y][z + 1].normal).mul(t))));
    }

    //add the value of the brush(centered at (cx, cy, cz)) to the scalar field
    public void add(Brush brush, int cx, int cy, int cz) {
        int hs = brush.getSfs().size / 2;

        //get bounds inside the sfs
        int x1 = Math.min(Math.max(cx - hs, 0), size - 1);
        int x2 = Math.min(Math.max(cx + hs, 0), size - 1);
        int y1 = Math.min(Math.max(cy - hs, 0), size - 1);
        int y2 = Math.min(Math.max(cy + hs, 0), size - 1);
        int z1 = Math.min(Math.max(cz - hs, 0), size - 1);
        int z2 = Math.min(Math.max(cz + hs, 0), size - 1);

        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2; y++) {
                for (int z = z1; z < z2; z++) {
                    var min = SampleValue.min(vertices[x][y][z], brush.getSfs().vertices[x - x1][y - y1][z - z1]);
                    vertices[x][y][z].distance = min.distance;
                    vertices[x][y][z].normal = new Vector3f(min.normal);
                }
            }
        }

        updateEdgeSurfaces(x1, y1, z1, x2, y2, z2);
    }

    public void subtract(Brush brush, int cx, int cy, int cz) {
        int hs = brush.getSfs().size / 2;

        //get bounds inside the sfs
        int x1 = Math.min(Math.max(cx - hs, 0), size - 1);
        int x2 = Math.min(Math.max(cx + hs, 0), size - 1);
        int y1 = Math.min(Math.max(cy - hs, 0), size - 1);
        int y2 = Math.min(Math.max(cy + hs, 0), size - 1);
        int z1 = Math.min(Math.max(cz - hs, 0), size - 1);
        int z2 = Math.min(Math.max(cz + hs, 0), size - 1);

        for (int x = x1; x < x2; x++) {
            for (int y = y1; y < y2; y++) {
                for (int z = z1; z < z2; z++) {
                    var maxn = SampleValue.maxn(vertices[x][y][z], brush.getSfs().vertices[x - x1][y - y1][z - z1]);
                    vertices[x][y][z].distance = maxn.distance;
                    vertices[x][y][z].normal = new Vector3f(maxn.normal);
                }
            }
        }

        updateEdgeSurfaces(x1, y1, z1, x2, y2, z2);
    }

    public boolean[][][] getEdgesX() {
        return edgesX;
    }

    public boolean[][][] getEdgesY() {
        return edgesY;
    }

    public boolean[][][] getEdgesZ() {
        return edgesZ;
    }

    public String sampleToString() {
        StringBuilder res = new StringBuilder();

        res.append(size); res.append(' ');
        res.append(scale); res.append(' ');
        res.append(length); res.append(' ');
        res.append('\n');

        for (int x = 0; x < this.size; x++) {
            for (int y = 0; y < this.size; y++) {
                for (int z = 0; z < this.size; z++) {
                    res.append(vertices[x][y][z].distance); res.append(' ');
                    res.append(vertices[x][y][z].normal.x); res.append(' ');
                    res.append(vertices[x][y][z].normal.y); res.append(' ');
                    res.append(vertices[x][y][z].normal.z); res.append(' ');
                    res.append('\n');
                }
            }
        }

        return res.toString();
    }

    public int getSize() {
        return size;
    }

    public float getScale() {
        return scale;
    }

    public float getLength() {
        return length;
    }
}
