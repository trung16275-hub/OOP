package screens.lighting;

import java.awt.Color;

public class LightSource {
    public enum Type {
        POINT,              // Circular glow (lanterns, orbs, player halo)
        FLUORESCENT_TUBE    // Wide downward pool on North wall + floor
    }

    private final Type type;
    private final double worldX;
    private final double worldY;
    private final float radius;
    private final Color tint;
    private final float baseIntensity; // 0.0f to 1.0f

    private boolean flickering;
    private int flickerTick;
    private float currentIntensity;

    public LightSource(Type type, double worldX, double worldY, float radius,
                       Color tint, float baseIntensity, boolean flickering) {
        this.type = type;
        this.worldX = worldX;
        this.worldY = worldY;
        this.radius = radius;
        this.tint = tint;
        this.baseIntensity = Math.max(0f, Math.min(1f, baseIntensity));
        this.currentIntensity = this.baseIntensity;
        this.flickering = flickering;
    }

    public void update(double timeSeconds) {
        if (!flickering) {
            currentIntensity = baseIntensity;
            return;
        }
        // Deterministic-feeling fluorescent buzz/flicker wave
        flickerTick++;
        double wave = Math.sin(timeSeconds * 19.0 + worldX) * Math.cos(timeSeconds * 31.0 + worldY);
        if (wave > 0.88) {
            currentIntensity = baseIntensity * 0.25f; // brief dip
        } else if (wave > 0.72) {
            currentIntensity = baseIntensity * 0.7f;
        } else {
            currentIntensity = baseIntensity;
        }
    }

    public Type getType() { return type; }
    public double getWorldX() { return worldX; }
    public double getWorldY() { return worldY; }
    public float getRadius() { return radius; }
    public Color getTint() { return tint; }
    public float getCurrentIntensity() { return currentIntensity; }
}
