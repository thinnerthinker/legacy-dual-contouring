package com.yallo.dual_contouring.Resources;

public class Resources {
    private static Shader terrainShader, brushPreviewShader;

    public static void init() {
        terrainShader = loadShader("src/main/Assets/terrain.vs", "src/main/Assets/terrain.fs");
        brushPreviewShader = loadShader("src/main/Assets/brushPreview.vs", "src/main/Assets/brushPreview.fs");
    }

    private static Shader loadShader(String vertex, String fragment) {
        try {
            return new Shader(vertex, fragment);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Shader getTerrainShader() {
        return terrainShader;
    }

    public static Shader getBrushPreviewShader() {
        return brushPreviewShader;
    }
}
