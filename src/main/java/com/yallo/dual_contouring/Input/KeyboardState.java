package com.yallo.dual_contouring.Input;

import java.util.Arrays;
import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

public class KeyboardState {
    private final HashMap<Integer, Boolean> keyStates;
    private final long window;

    public KeyboardState(long window, int... keys) {
        this.keyStates = new HashMap<>();
        this.window = window;

        Arrays.stream(keys).forEach(k -> keyStates.put(k, false));
    }

    public void update() {
        keyStates.replaceAll((k, v) -> glfwGetKey(window, k) == GLFW_PRESS);
    }

    public boolean isKeyDown(int key) {
        return keyStates.get(key);
    }

    public void assign(KeyboardState keyboardState) {
        keyStates.putAll(keyboardState.keyStates);
    }
}
