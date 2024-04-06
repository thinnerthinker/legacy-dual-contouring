package com.yallo.dual_contouring.Resources;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.memFree;

public class Mesh {
    protected int vbo, normalVbo, vao, ebo;

    protected int triangleCount;

    protected boolean indexed;

    public Mesh() {
        indexed = false;
        triangleCount = 0;
    }

    public Mesh(float[] vertices, float[] normals, int[] indices) {
        indexed = true;
        triangleCount = indices.length / 3;

        //make vao
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        //store the vertices and normals into buffers
        vbo = makeVbo(vertices, 0);
        normalVbo = makeVbo(normals, 1);

        //store indices into buffer
        ebo = makeEbo(indices);

        //unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    public Mesh(float[] vertices, float[] normals) {
        indexed = false;
        triangleCount = vertices.length / 3;

        //make vao
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        //store the vertices and normals into buffers
        vbo = makeVbo(vertices, 0);
        normalVbo = makeVbo(normals, 1);

        ebo = 0;

        //unbind
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void draw() {
        glBindVertexArray(vao);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        if (indexed) {
            glDrawElements(GL_TRIANGLES, triangleCount * 3, GL_UNSIGNED_INT, 0);
        }
        else {
            glDrawArrays(GL_TRIANGLES, 0, triangleCount * 3);
        }

        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    public void dispose() {
        glDisableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(vbo);

        if (indexed) {
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glDeleteBuffers(ebo);
        }

        glBindVertexArray(0);
        glDeleteVertexArrays(vao);
    }

    protected int makeVbo(float[] array, int attribInd) {
        //store into vram
        int buf = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, buf);
        glBufferData(GL_ARRAY_BUFFER, array, GL_STATIC_DRAW);

        //set the attributes
        glVertexAttribPointer(attribInd, 3, GL_FLOAT, false, 0, 0);

        return buf;
    }
    protected int makeEbo(int[] array) {
        int buf = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buf);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, array, GL_STATIC_DRAW);

        return buf;
    }
}
