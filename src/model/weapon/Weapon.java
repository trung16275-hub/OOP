package model.weapon;

public class Weapon {
    private final String name;
    private final int damage;

    public Weapon(String name, int damage) {
        this.name = name;
        this.damage = damage;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    /** Returns whether the weapon can make an attack; unlimited weapons use the default. */
    public boolean consumeAmmo() {
        return true;
    }
}
