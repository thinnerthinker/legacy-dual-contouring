package com.yallo.dual_contouring.Input;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class Input {
    private static KeyboardState keyboardCurrent;
    private static KeyboardState keyboardPrevious;

    private static MouseState mouseCurrent;
    private static MouseState mousePrevious;

    private static double mouseScroll, mouseScrollPrevious;

    public static void init(long window) {
        keyboardCurrent = new KeyboardState(window, GLFW_KEY_W, GLFW_KEY_A, GLFW_KEY_S, GLFW_KEY_D, GLFW_KEY_L, GLFW_KEY_LEFT_CONTROL, GLFW_KEY_LEFT_SHIFT, GLFW_KEY_O, GLFW_KEY_F5);
        keyboardPrevious = new KeyboardState(window, GLFW_KEY_W, GLFW_KEY_A, GLFW_KEY_S, GLFW_KEY_D, GLFW_KEY_L, GLFW_KEY_LEFT_CONTROL, GLFW_KEY_LEFT_SHIFT, GLFW_KEY_O, GLFW_KEY_F5);

        mouseCurrent = new MouseState(window, true);
        mousePrevious = new MouseState(window, false);

        mouseScroll = 0;
        mouseScrollPrevious = 0;
    }

    public static void update() {
        keyboardPrevious.assign(keyboardCurrent);
        keyboardCurrent.update();

        mousePrevious.assign(mouseCurrent);
        mouseCurrent.update();

        mouseScrollPrevious = mouseScroll;
        mouseScroll = mouseCurrent.getScrollAmount();
    }

    public static boolean isKeyDown(int key) {
        return keyboardCurrent.isKeyDown(key);
    }

    public static boolean isKeyPressed(int key) {
        return keyboardCurrent.isKeyDown(key) && !keyboardPrevious.isKeyDown(key);
    }

    public static boolean isMouseButtonDown(int button) {
        return mouseCurrent.isButtonDown(button);
    }

    public static boolean isMouseButtonPressed(int button) {
        return mouseCurrent.isButtonDown(button) && !mousePrevious.isButtonDown(button);
    }

    public static Vector2f getMousePosition() {
        return mouseCurrent.getMousePosition();
    }

    public static  Vector2f getMouseMovement() {
        return mouseCurrent.getMousePosition().sub(mousePrevious.getMousePosition());
    }

    public static float getScrollAmount() {
        return (float)mouseScroll;
    }

    public static float getScrollDelta() {
        return (float)(mouseScroll - mouseScrollPrevious);
    }

    public static void dispose() {
        mouseCurrent.dispose();
        mousePrevious.dispose();
    }
}
