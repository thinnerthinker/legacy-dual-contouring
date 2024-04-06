package com.yallo.dual_contouring.TerrainEditor;

import com.yallo.dual_contouring.Game.Camera;
import com.yallo.dual_contouring.Resources.Mesh;
import com.yallo.dual_contouring.Resources.Resources;
import com.yallo.dual_contouring.Resources.Shader;
import com.yallo.dual_contouring.TerrainGenerator.Terrain;
import com.yallo.dual_contouring.Resources.*;
import org.joml.Vector3f;

public class BrushPreview {
    private Vector3f position;
    private Mesh mesh;

    private float gridScale;

    private boolean visible;

    public BrushPreview(Brush brush) {
        position = new Vector3f(0, 0, 0);
        resample(brush);
    }

    public void resample(Brush brush) {
        if (mesh != null) mesh.dispose();
        mesh = new Terrain(brush.getSfs()).getMesh();
        gridScale = 1 / brush.getSfs().getScale();
    }

    public void update(Vector3f position) {
        visible = true;
        this.position = new Vector3f(position);
    }

    public void draw(Camera camera) {
        if (!visible) return;

        Shader shader = Resources.getBrushPreviewShader();
        shader.bind();

        shader.setUniform("viewProj", camera.getViewProjection());
        shader.setUniform("wPosition", position);
        mesh.draw();

        shader.unbind();
    }

    public Vector3f getPosition() {
        return position;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
