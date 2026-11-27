
package model;

import model.player.Player;

import map.Room;

import java.awt.Rectangle;

public class AcidProjectile {

    private static final int SIZE = 12;
    private static final double SPEED = 5.5;
    private static final double MAX_COLLISION_STEP = 2.0;

    private double x;
    private double y;

    private final double directionX;
    private final double directionY;

    private final int damage;

    private boolean active = true;

    public AcidProjectile(
            double x,
            double y,
            double directionX,
            double directionY,
            int damage
    ) {

        double length = Math.hypot(directionX, directionY);

        if (!Double.isFinite(length) || length == 0) {
            throw new IllegalArgumentException(
                    "Invalid projectile direction"
            );
        }

        this.x = x;
        this.y = y;

        this.directionX = directionX / length;
        this.directionY = directionY / length;

        this.damage = damage;
    }

    // Cập nhật vị trí viên đạn
    public void update(Room room) {

        if (!active || room == null) {
            return;
        }

        double moveX = directionX * SPEED;
        double moveY = directionY * SPEED;

        double distance = Math.max(
                Math.abs(moveX),
                Math.abs(moveY)
        );

        int steps = Math.max(
                1,
                (int) Math.ceil(distance / MAX_COLLISION_STEP)
        );

        double stepX = moveX / steps;
        double stepY = moveY / steps;

        for (int i = 0; i < steps; i++) {

            x += stepX;
            y += stepY;

            // Kiểm tra va chạm bằng toàn bộ kích thước viên đạn
            if (room.checkCollision(getBounds())) {
                active = false;
                return;
            }
        }
    }

    // Kiểm tra va chạm với Player
    public boolean hits(Player player) {

        if (!active || player == null) {
            return false;
        }

        Rectangle projectileBounds = getBounds();

        Rectangle playerBounds = new Rectangle(
                (int) Math.floor(player.getX()),
                (int) Math.floor(player.getY()),
                player.getWidth(),
                player.getHeight()
        );

        return projectileBounds.intersects(playerBounds);
    }

    // Lấy vùng va chạm của viên đạn
    public Rectangle getBounds() {

        return new Rectangle(
                (int) x,
                (int) y,
                SIZE,
                SIZE
        );
    }

    public int getDamage() {
        return damage;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
