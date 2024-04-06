package com.yallo.dual_contouring.Game;

import com.yallo.dual_contouring.Input.Input;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private float yaw, pitch;
    private float rotationSpeed, moveSpeed;
    private final Vector3f position;

    private Vector3f up, right, forward;

    private Matrix4f view;
    private final Matrix4f projection;

    private final float aspect;
    private final float nearPlane;

    private boolean locked;

    public Camera(float moveSpeed, float rotationSpeed) {
        yaw = 0;
        pitch = 0;

        this.moveSpeed = moveSpeed;
        this.rotationSpeed = rotationSpeed;

        position = new Vector3f(0.0f, 0.0f, 1.f);

        aspect = 16.0f / 9.0f;
        nearPlane = 0.001f;

        projection = new Matrix4f().perspective((float) Math.PI / 4, aspect, nearPlane, 300.f);
        updateView();
    }

    public void update(float dt) {
        if (Input.isKeyPressed(GLFW_KEY_L)) {
            locked = !locked;
        }

        if (locked) return;

        move(dt);
        if (Input.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
            turn(dt);
        }
    }

    private void turn(float dt)
    {
        Vector2f dPos = Input.getMouseMovement();
        yaw -= rotationSpeed * dPos.x * dt;
        if ((pitch < Math.PI / 2 && rotationSpeed * dPos.y < 0) || (pitch > -Math.PI / 2 && rotationSpeed * dPos.y > 0))
            pitch -= rotationSpeed * dPos.y * dt;

        updateView();
    }

    private void move(float dt) {
        Vector3f moveVector = new Vector3f(0, 0, 0);
        if (Input.isKeyDown(GLFW_KEY_A)) {
            moveVector.add(-1, 0, 0);
        }
        if (Input.isKeyDown(GLFW_KEY_D)) {
            moveVector.add(1, 0, 0);
        }
        if (Input.isKeyDown(GLFW_KEY_W)) {
            moveVector.add(0, 0, -1);
        }
        if (Input.isKeyDown(GLFW_KEY_S)) {
            moveVector.add(0, 0, 1);
        }

        addToPosition(moveVector.mul(moveSpeed * dt));
    }

    private void updateView() {
        Matrix4f rotation = new Matrix4f().rotationYXZ(yaw, pitch, 0);

        Vector3f originalTarget = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f rotatedTarget = rotation.transformDirection(originalTarget);
        Vector3f finalTarget = new Vector3f(position).add(rotatedTarget);

        forward = new Matrix4f().rotationYXZ(yaw, pitch, 0).transformDirection(new Vector3f(0, 0, -1));

        Vector3f originalUp = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f rotatedUp = rotation.transformDirection(originalUp);

        up = rotatedUp;
        right = new Vector3f(forward).cross(up);

        view = new Matrix4f().lookAt(position, finalTarget, rotatedUp);
    }

    private void addToPosition(Vector3f v) {
        Matrix4f rotation = new Matrix4f().rotationYXZ(yaw, pitch, 0);
        Vector3f rotatedVector = rotation.transformDirection(v);

        position.add(rotatedVector);
        updateView();
    }

    public Vector3f unproject(Vector2f screen) {
        return getViewProjection().invert().transformDirection(new Vector3f(screen, 2*nearPlane)).normalize();
    }

    public Vector3f getUp() {
        return up;
    }

    public Vector3f getRight() {
        return right;
    }

    public Vector3f getForward() {
        return forward;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Matrix4f getViewProjection() {
        return new Matrix4f().mul(projection).mul(view);
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public boolean isLocked() {
        return locked;
    }

    public float getAspect() {
        return aspect;
    }
}
