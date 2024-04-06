package com.yallo.dual_contouring.TerrainEditor;

import com.yallo.dual_contouring.Resources.Mesh;

import static org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray;
import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class DynMesh extends Mesh {
    private int capacity;

    public DynMesh(float[] vertices, float[] normals, int capacity) {
        super();

        this.capacity = capacity;
        triangleCount = vertices.length / 3;

        //make vao
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        //store the vertices and normals into buffers
        vbo = makeVbo(capacity, 0);
        normalVbo = makeVbo(capacity, 1);
        update(vertices, normals);

        //unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void update(float[] vertices, float[] normals) {
        if (vertices.length > capacity) {
            resize(vertices.length);
        }

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        glBindBuffer(GL_ARRAY_BUFFER, normalVbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, normals);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    protected int makeVbo(int length, int attribInd) {
        int buf = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, buf);
        glBufferData(GL_ARRAY_BUFFER, length * 4, GL_STREAM_DRAW);

        //set the attributes
        glVertexAttribPointer(attribInd, 3, GL_FLOAT, false, 0, 0);

        return buf;
    }

    private void resize(int newlen) {
        while (capacity < newlen) {
            capacity *= 2;
        }

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, capacity, GL_STREAM_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, normalVbo);
        glBufferData(GL_ARRAY_BUFFER, capacity, GL_STREAM_DRAW);
    }
}
