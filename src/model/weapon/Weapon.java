package model.weapon;

import model.Combatant;
import model.Player;
import java.util.concurrent.TimeUnit;

/**
 * Lớp cơ sở cho các loại vũ khí.
 * Các lớp con sẽ định nghĩa cách tấn công cụ thể, ví dụ cận chiến hoặc tầm xa.
 */
public abstract class Weapon {
    private final String name;
    private final int damage;
    private final double hitboxRange;
    private final long cooldownMillis;
    private long lastAttackNanos;
    private boolean hasAttacked;

    protected Weapon(String name, int damage, double hitboxRange, long cooldownMillis) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Weapon name cannot be blank.");
        }
        if (damage < 0) {
            throw new IllegalArgumentException("Weapon damage cannot be negative.");
        }
        if (!Double.isFinite(hitboxRange) || hitboxRange < 0) {
            throw new IllegalArgumentException("Weapon hitbox range must be a finite, non-negative value.");
        }
        if (cooldownMillis < 0) {
            throw new IllegalArgumentException("Weapon cooldown cannot be negative.");
        }

        this.name = name;
        this.damage = damage;
        this.hitboxRange = hitboxRange;
        this.cooldownMillis = cooldownMillis;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public double getHitboxRange(Player attacker) {
        return hitboxRange;
    }

    protected boolean isTargetInHitbox(Player attacker, Combatant target) {
        if (attacker == null || target == null || attacker == target
                || attacker.isDead() || target.isDead()) {
            return false;
        }

        double dx = (target.getX() + target.getWidth() / 2.0)
                - (attacker.getX() + attacker.getWidth() / 2.0);
        double dy = (target.getY() + target.getHeight() / 2.0)
                - (attacker.getY() + attacker.getHeight() / 2.0);
        double range = getHitboxRange(attacker);

        return dx * dx + dy * dy <= range * range;
    }

    public long getCooldownMillis() {
        return cooldownMillis;
    }

    public final boolean tryAttack(Player attacker, Combatant target) {
        if (attacker == null || attacker == target || attacker.isDead()) {
            return false;
        }

        long now = System.nanoTime();
        long cooldownNanos = TimeUnit.MILLISECONDS.toNanos(cooldownMillis);
        if (hasAttacked && now - lastAttackNanos < cooldownNanos) {
            return false;
        }

        lastAttackNanos = now;
        hasAttacked = true;
        attack(attacker, target);
        return true;
    }

    public abstract void attack(Player attacker, Combatant target);
}
