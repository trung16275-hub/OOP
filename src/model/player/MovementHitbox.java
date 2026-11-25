package model.player;

import java.awt.Rectangle;

/** Defines the smaller area used to keep the player clear of solid map tiles. */
public final class MovementHitbox {
    private final int offsetX;
    private final int offsetY;
    private final int width;
    private final int height;

    public MovementHitbox(int offsetX, int offsetY, int width, int height) {
        if (offsetX < 0 || offsetY < 0 || width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Movement hitbox offsets must be non-negative and dimensions positive.");
        }
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.width = width;
        this.height = height;
    }

    public Rectangle getBounds(double playerX, double playerY, int spriteWidth, boolean facingLeft) {
        int horizontalOffset = facingLeft ? spriteWidth - offsetX - width : offsetX;
        return new Rectangle((int) Math.round(playerX + horizontalOffset),
                (int) Math.round(playerY + offsetY), width, height);
    }
}
