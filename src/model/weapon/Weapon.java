
package model.weapon;

import model.Combatant;
import model.weapon.behavior.WeaponBehavior;

public abstract class Weapon {

    private final String name;
    private final int damage;
    private final double range;
    private final long cooldownNanos;

    private WeaponBehavior behavior;

    public Weapon(String name, int damage, double range,
                  long cooldownNanos, WeaponBehavior behavior) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Weapon name cannot be empty.");
        }

        if (damage <= 0 || !Double.isFinite(range) || range <= 0
                || cooldownNanos < 0) {
            throw new IllegalArgumentException("Invalid weapon stats.");
        }

        this.name = name;
        this.damage = damage;
        this.range = range;
        this.cooldownNanos = cooldownNanos;
        this.behavior = behavior;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public double getRange() {
        return range;
    }

    public long getCooldownNanos() {
        return cooldownNanos;
    }

    public WeaponBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(WeaponBehavior behavior) {
        if (behavior == null) {
            throw new IllegalArgumentException("Behavior cannot be null.");
        }
        this.behavior = behavior;
    }

    public void attack(Combatant attacker, Combatant target) {
        if (attacker == null || target == null || behavior == null) {
            return;
        }

        behavior.attack(this, attacker, target);
    }
}

