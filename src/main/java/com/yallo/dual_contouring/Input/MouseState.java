package com.yallo.dual_contouring.Input;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.system.MemoryUtil;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;

public class MouseState {
    private final long window;

    private final DoubleBuffer bx;
    private final DoubleBuffer by;
    private double x, y;

    private boolean left, right, middle;

    private double scrollAmount;

    public MouseState(long window, boolean updateScroll) {
        this.window = window;

        bx = BufferUtils.createDoubleBuffer(1);
        by = BufferUtils.createDoubleBuffer(1);

        scrollAmount = 0;

        if (updateScroll) {
            glfwSetScrollCallback(window, GLFWScrollCallback.create((win, xOffset, yOffset) -> scrollAmount += yOffset));
        }
    }

    public void update() {
        glfwGetCursorPos(window, bx, by);
        x = bx.get(0);
        y = by.get(0);

        left = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS;
        right = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS;
        middle = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_MIDDLE) == GLFW_PRESS;
    }

    public boolean isButtonDown(int button) {
        return switch (button) {
            case GLFW_MOUSE_BUTTON_LEFT -> left;
            case GLFW_MOUSE_BUTTON_RIGHT -> right;
            case GLFW_MOUSE_BUTTON_MIDDLE -> middle;
            default -> false;
        };
    }

    public Vector2f getMousePosition() {
        return new Vector2f((float) x, (float) y);
    }

    public double getScrollAmount() {
        return scrollAmount;
    }

    public void assign(MouseState mouseState) {
        left = mouseState.left;
        right = mouseState.right;
        middle = mouseState.middle;

        x = mouseState.x;
        y = mouseState.y;

        scrollAmount = mouseState.scrollAmount;
    }

    public void dispose() {
        MemoryUtil.memFree(bx);
        MemoryUtil.memFree(by);
    }
}
