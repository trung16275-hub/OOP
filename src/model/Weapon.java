
package model;

public class Weapon {

    public enum Type {
        MELEE,
        RANGED
    }

    // Giữ public để tương thích với code cũ của nhóm.
    public String name;
    public int damage;

    private final Type type;
    private final double range;

    // Constructor cũ: mặc định là vũ khí cận chiến.
    public Weapon(String name, int damage) {
        this(name, damage, Type.MELEE, 70);
    }

    public Weapon(String name, int damage, Type type, double range) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Weapon name cannot be empty.");
        }

        if (damage <= 0) {
            throw new IllegalArgumentException("Damage must be positive.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Weapon type cannot be null.");
        }

        if (!Double.isFinite(range) || range <= 0) {
            throw new IllegalArgumentException(
                    "Range must be positive and finite."
            );
        }

        this.name = name;
        this.damage = damage;
        this.type = type;
        this.range = range;
    }

    public Type getType() {
        return type;
    }

    public double getRange() {
        return range;
    }
}


