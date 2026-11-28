package model;

import map.Room;
import model.player.Player;

import java.awt.Rectangle;

/** A straight-moving projectile that is consumed by a target combatant or a wall. */
public class Projectile {
    public enum Kind {
        AK47_BULLET(1.0, 0),
        ACID(0.5, 2_000_000_000L);

        private final double slowMultiplier;
        private final long slowDurationNanos;

        Kind(double slowMultiplier, long slowDurationNanos) {
            this.slowMultiplier = slowMultiplier;
            this.slowDurationNanos = slowDurationNanos;
        }

        private void applyHitEffect(Player target) {
            if (slowDurationNanos > 0) {
                target.getMovementEffects().applySlow(this, slowMultiplier, slowDurationNanos);
            }
        }
    }

    private static final int DEFAULT_SIZE = 8;
    private static final double MAX_COLLISION_STEP = 2.0;

    private double x;
    private double y;
    private final double directionX;
    private final double directionY;
    private final double speed;
    private final int damage;
    private final int size;
    private final Kind kind;
    private boolean active = true;

    public Projectile(double x, double y, double directionX, double directionY, double speed, int damage) {
        this(x, y, directionX, directionY, speed, damage, DEFAULT_SIZE);
    }

    public Projectile(double x, double y, double directionX, double directionY, double speed, int damage, int size) {
        this(x, y, directionX, directionY, speed, damage, size, Kind.AK47_BULLET);
    }

    public Projectile(double x, double y, double directionX, double directionY, double speed,
                      int damage, int size, Kind kind) {
        double length = Math.hypot(directionX, directionY);
        if (!Double.isFinite(length) || length == 0) {
            throw new IllegalArgumentException("Projectile direction must be finite and non-zero.");
        }
        if (!Double.isFinite(speed) || speed <= 0 || damage < 0 || size <= 0) {
            throw new IllegalArgumentException("Projectile speed/size must be positive and damage non-negative.");
        }

        this.x = x;
        this.y = y;
        this.directionX = directionX / length;
        this.directionY = directionY / length;
        this.speed = speed;
        this.damage = damage;
        this.size = size;
        this.kind = kind;
    }

    /** Advances along a straight line and deactivates on wall contact. */
    public void update(Room room) {
        if (!active) {
            return;
        }
        if (room == null) {
            active = false;
            return;
        }

        double distanceX = directionX * speed;
        double distanceY = directionY * speed;
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(distanceX), Math.abs(distanceY))/ MAX_COLLISION_STEP));
        double stepX = distanceX / steps;
        double stepY = distanceY / steps;

        for (int i = 0; i < steps; i++) {
            x += stepX;
            y += stepY;
            if (x < 0 || y < 0 || x >= room.getWidth() * Room.TILE_SIZE
                    || y >= room.getHeight() * Room.TILE_SIZE
                    || room.checkCollision(getBounds())) {
                active = false;
                return;
            }
        }
    }

    public boolean hits(Combatant target) {
        return active && target != null && !target.isDead() && getBounds().intersects(
                new Rectangle((int) Math.floor(target.getX()), (int) Math.floor(target.getY()),
                        target.getWidth(), target.getHeight()));
    }

    public void hitPlayer(Player target) {
        target.takeDamage(damage);
        kind.applyHitEffect(target);
        deactivate();
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x - size / 2.0),
                (int) Math.round(y - size / 2.0), size, size);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}
