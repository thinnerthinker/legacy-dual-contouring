package com.yallo.dual_contouring.Game;

import com.yallo.dual_contouring.Input.Input;
import com.yallo.dual_contouring.Resources.SDF;
import com.yallo.dual_contouring.TerrainEditor.Brush;
import com.yallo.dual_contouring.TerrainEditor.BrushPreview;
import com.yallo.dual_contouring.TerrainEditor.RaycastInfo;
import com.yallo.dual_contouring.TerrainEditor.SphereBrush;
import com.yallo.dual_contouring.TerrainGenerator.ScalarFieldSample;
import com.yallo.dual_contouring.TerrainGenerator.Terrain;
import com.yallo.dual_contouring.Util.OptionsDialog;
import com.yallo.dual_contouring.TerrainGenerator.*;
import com.yallo.dual_contouring.TerrainEditor.*;
import com.yallo.dual_contouring.Util.*;
import com.yallo.dual_contouring.Resources.*;
import com.yallo.dual_contouring.Input.*;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.glfw.GLFW.*;

public class MyGame extends Game {
    private Camera camera;
    private ScalarFieldSample sfs;
    private Terrain terrain;

    private Brush sphereBrush;
    private BrushPreview brushPreview;
    private boolean fixedDistance;
    private float distance;

    private OptionsDialog options;

    @Override
    public void initialize() {
        options = new OptionsDialog();
        generateTerrain();

        camera = new Camera(0.25f, 0.15f);

        brushPreview = new BrushPreview(sphereBrush);
        brushPreview.update(new Vector3f(1, 0, 0));
    }

    boolean isMouseInWindow() {
        var pos = Input.getMousePosition();
        return pos.x >= 0 && pos.x < Window.getWidth() && pos.y >= 0 && pos.y < Window.getHeight();
    }

    @Override
    public void update(double dt) {
        camera.update((float) dt);

        //brush mode is only enabled while the camera is locked in place
        if (camera.isLocked() && isMouseInWindow()) {
            //get the position of the brush
            Vector3f p = null;

            Vector2f mp = new Vector2f(Input.getMousePosition()).mul(2.0f / Window.getWidth(), -2.0f / Window.getHeight()).sub(1, -1);
            Vector3f dir = camera.unproject(mp);

            if (!fixedDistance) {
                //cast a ray based on the cursor position
                RaycastInfo h = terrain.raycast(camera.getPosition(), dir);
                if (h.hit) p = sfs.transform(new Vector3f(h.px, h.py, h.pz));
            } else {
                //get the point a fixed distance away from the camera
                p = new Vector3f(camera.getPosition()).add(new Vector3f(dir.mul(distance)));
            }

            if (p != null) {
                brushPreview.update(p);

                //change the terrain if the ray hit it and mouse input is applied
                if (Input.isMouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                    Vector3f tp = sfs.transformInv(new Vector3f(p));
                    terrain.add(sphereBrush, (int) tp.x, (int) tp.y, (int) tp.z);
                }
                if (Input.isMouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT)) {
                    Vector3f tp = sfs.transformInv(new Vector3f(p));
                    terrain.subtract(sphereBrush, (int) tp.x, (int) tp.y, (int) tp.z);
                }

                //when scrolling with ctrl, set a fixed distance instead of using raycast(the distance starts from the last raycast)
                if (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) && Input.getScrollDelta() != 0 && !fixedDistance) {
                    distance = p.distance(camera.getPosition());
                    fixedDistance = true;
                }
            } else {
                brushPreview.setVisible(false);

                //when scrolling with ctrl, set a fixed distance instead of using raycast(the distance starts from 1 away from the camera)
                if (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) && Input.getScrollDelta() != 0 && !fixedDistance) {
                    distance = 1;
                    fixedDistance = true;
                }
            }

            if (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) && Input.getScrollDelta() != 0) {
                distance += Input.getScrollDelta() * dt;
            } else if (Input.getScrollDelta() != 0 && !Input.isKeyDown(GLFW_KEY_LEFT_CONTROL)) {
                sphereBrush.resample((int)Input.getScrollDelta());
                brushPreview.resample(sphereBrush);
            }
        } else {
            brushPreview.setVisible(false);
            fixedDistance = false;
        }

        if (Input.isKeyPressed(GLFW_KEY_F5)) {
            terrain.getMesh().dispose();
            generateTerrain();
        }

        if (Input.isKeyDown(GLFW_KEY_LEFT_SHIFT) && Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) && Input.isKeyPressed(GLFW_KEY_S)) {
            saveSfs();
        } else if (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) && Input.isKeyPressed(GLFW_KEY_S)) {
            saveTerrain();
        }
        if (Input.isKeyDown(GLFW_KEY_LEFT_CONTROL) && Input.isKeyPressed(GLFW_KEY_O)) {
            loadSfs();
        }
    }

    @Override
    public void draw() {
        terrain.draw(camera);
        brushPreview.draw(camera);
    }

    @Override
    public void dispose() {
        options.dispose();
    }

    private void generateTerrain() {
        options.setVisible(true);
        int res = options.getResolution();
        SDF sdf = options.getSdf();

        sphereBrush = new SphereBrush(0.1f, res / 10, res / 2, 0.04f);

        sfs = new ScalarFieldSample(1f, res, sdf);
        terrain = new Terrain(sfs);
    }

    private void saveTerrain() {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Save Resources.Mesh");
        int res = jfc.showSaveDialog(null);

        if (res == JFileChooser.APPROVE_OPTION) {
            String path = jfc.getSelectedFile().getAbsolutePath();
            try {
                Files.writeString(Path.of(path), terrain.meshToString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveSfs() {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Save Scalar Field Sample");
        int res = jfc.showSaveDialog(null);

        if (res == JFileChooser.APPROVE_OPTION) {
            String path = jfc.getSelectedFile().getAbsolutePath();
            try {
                Files.writeString(Path.of(path), sfs.sampleToString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadSfs() {
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle("Load Scalar Field Sample");
        int res = jfc.showSaveDialog(null);

        if (res == JFileChooser.APPROVE_OPTION) {
            String path = jfc.getSelectedFile().getAbsolutePath();
            try {
                sfs = new ScalarFieldSample(Files.readAllLines(Path.of(path)).toArray(String[]::new));
                terrain = new Terrain(sfs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
