package model.weapon;

import model.Combatant;
import model.Player;

/** Vũ khí cận chiến gây sát thương cho mục tiêu trong tầm kiếm. */
public class Sword extends Weapon {
    private static final String DEFAULT_NAME = "Kiếm";
    private static final int DEFAULT_DAMAGE = 25;
    private static final double HITBOX_RANGE = 300;
    private static final long DEFAULT_COOLDOWN_MILLIS = 100;
    private static final long SWING_DURATION_NANOS = 100_000_000L;
    private volatile long swingStartedAtNanos = -1;

    public Sword() {
        this(DEFAULT_NAME, DEFAULT_DAMAGE, HITBOX_RANGE, DEFAULT_COOLDOWN_MILLIS);
    }

    protected Sword(String name, int damage, double hitboxRange, long cooldownMillis) {
        super(name, damage, hitboxRange, cooldownMillis);
    }

    @Override
    public void attack(Player attacker, Combatant target) {
        if (attacker == null || attacker.isDead() || target == attacker) {
            return;
        }

        swingStartedAtNanos = System.nanoTime();
        if (isTargetInSwordReach(attacker, target)) {
            target.takeDamage(getDamage());
        }
    }

    private boolean isTargetInSwordReach(Player attacker, Combatant target) {
        if (attacker == null || target == null || attacker == target
                || attacker.isDead() || target.isDead()) {
            return false;
        }

        double dx = (target.getX() + target.getWidth() / 2.0)
                - (attacker.getX() + attacker.getWidth() / 2.0);
        double dy = (target.getY() + target.getHeight() / 2.0)
                - (attacker.getY() + attacker.getHeight() / 2.0);
        double reach = getHitboxRange(attacker) + target.getHitboxRadius();

        return dx * dx + dy * dy <= reach * reach;
    }

    /** Returns the current swing progress from 0 to 1, or -1 when idle. */
    public double getSwingProgress() {
        if (swingStartedAtNanos < 0) {
            return -1;
        }

        long elapsed = System.nanoTime() - swingStartedAtNanos;
        if (elapsed < 0 || elapsed >= SWING_DURATION_NANOS) {
            return -1;
        }
        return elapsed / (double) SWING_DURATION_NANOS;
    }
}
