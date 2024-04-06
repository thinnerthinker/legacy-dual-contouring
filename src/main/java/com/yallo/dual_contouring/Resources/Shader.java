package com.yallo.dual_contouring.Resources;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.lwjgl.opengl.GL30.*;


public class Shader {
    private final int program;

    private final HashMap<String, Integer> uniformLocations;

    public Shader(String vertexPath, String fragmentPath) throws Exception {
        program = glCreateProgram();
        if (program == 0) {
            throw new Exception("Could not create shader");
        }

        int vertexShader = createShader(Files.readString(Path.of(vertexPath)), GL_VERTEX_SHADER);
        int fragmentShader = createShader(Files.readString(Path.of(fragmentPath)), GL_FRAGMENT_SHADER);

        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);
        glLinkProgram(program);

        glDetachShader(program, vertexShader);
        glDetachShader(program, fragmentShader);
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        uniformLocations = new HashMap<>();
    }

    private int createShader(String shaderCode, int shaderType) throws Exception {
        int shader = glCreateShader(shaderType);
        if (shader == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        glShaderSource(shader, shaderCode);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Resources.Shader code: " + glGetShaderInfoLog(shader, 1024));
        }

        return shader;
    }

    public void bind() {
        glUseProgram(program);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void dispose() {
        unbind();
        if (program != 0) {
            glDeleteProgram(program);
        }
    }

    private int getUniformLocation(String name) {
        //cache
        if (!uniformLocations.containsKey(name)) {
            int loc = glGetUniformLocation(program, name);
            uniformLocations.put(name, loc);

            return loc;
        }

        return uniformLocations.get(name);
    }

    public void setUniform(String name, Matrix4f value) {
        //store into stack
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            value.get(buffer);

            glUniformMatrix4fv(getUniformLocation(name), false, buffer);
        }
    }

    public void setUniform(String name, Vector3f value) {
        //store into stack
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(3);
            value.get(buffer);

            glUniform3fv(getUniformLocation(name), buffer);
        }
    }
}