
package model.boss.boss2;

import model.player.Player;
import model.RoomEffect;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class AcidTrail implements RoomEffect {

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

    @Override
    public void update(Player player) {
        if (player == null || !getBounds().intersects(getPlayerBounds(player))) {
            return;
        }
        player.getMovementEffects().applySlow(AcidTrail.class, 0.5, 2_000_000_000L);
        if (player.isDead() || isExpired()) {
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

    @Override
    public boolean isExpired() {
        return System.nanoTime() - createdAt >= DURATION_NANOS;
    }

    @Override
    public void render(Graphics2D graphics) {
        Rectangle bounds = getBounds();
        graphics.setColor(new Color(50, 255, 30, 35));
        graphics.fillOval(bounds.x - 5, bounds.y - 5, bounds.width + 10, bounds.height + 10);
        graphics.setColor(new Color(80, 220, 30, 150));
        graphics.fillOval(bounds.x, bounds.y, bounds.width, bounds.height);
        graphics.setColor(new Color(170, 255, 60, 180));
        graphics.fillOval(bounds.x + 8, bounds.y + 8, bounds.width - 16, bounds.height - 16);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
