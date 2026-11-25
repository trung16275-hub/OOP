package model.player;

import java.awt.Rectangle;

/** Defines the player area that can receive attacks, relative to the sprite origin. */
public final class HurtBox {
    private final int offsetX;
    private final int offsetY;
    private final int width;
    private final int height;

    public HurtBox(int offsetX, int offsetY, int width, int height) {
        if (offsetX < 0 || offsetY < 0 || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Hurt box offsets must be non-negative and dimensions positive.");
        }
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
    }

    public Rectangle getBounds(double playerX, double playerY) {
        return new Rectangle((int) Math.round(playerX + offsetX),
                (int) Math.round(playerY + offsetY), width, height);
    }
}