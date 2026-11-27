
package model.boss;

import model.player.Player;

import java.awt.Rectangle;

public class AcidTrail {

    private static final int SIZE = 42;
    private static final long DURATION_NANOS = 5_000_000_000L;

    private static final int DAMAGE_PER_TICK = 2;
    private static final long DAMAGE_INTERVAL_NANOS = 500_000_000L;

    private final double x;
    private final double y;

    private final long createdAt;
    private long lastDamageTime;

    public AcidTrail(double x, double y) {
        this.x = x;
        this.y = y;

        this.createdAt = System.nanoTime();
        this.lastDamageTime = 0L;
    }

    public void update(Player player) {
        if (player == null || player.isDead() || isExpired()) {
            return;
        }

        if (!getBounds().intersects(getPlayerBounds(player))) {
            return;
        }

        long now = System.nanoTime();

        if (lastDamageTime == 0L ||
                now - lastDamageTime >= DAMAGE_INTERVAL_NANOS) {

            player.takeDamage(DAMAGE_PER_TICK);
            lastDamageTime = now;
        }
    }

    private Rectangle getPlayerBounds(Player player) {
        return new Rectangle(
                (int) Math.floor(player.getX()),
                (int) Math.floor(player.getY()),
                player.getWidth(),
                player.getHeight()
        );
    }

    public Rectangle getBounds() {
        return new Rectangle(
                (int) Math.round(x - SIZE / 2.0),
                (int) Math.round(y - SIZE / 2.0),
                SIZE,
                SIZE
        );
    }

    public boolean isExpired() {
        return System.nanoTime() - createdAt >= DURATION_NANOS;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
