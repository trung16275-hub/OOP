package model.weapon;

import map.Room;
import model.Projectile;
import model.player.Player;
import model.pickup.AmmoPickup;
import java.util.function.Consumer;

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

    public long getAttackCooldownNanos() {
        return 0;
    }

    public int addAmmo(AmmoPickup.Kind kind, int rounds) {
        return 0;
    }

    public boolean tryAttack(Player attacker, Room room, Consumer<Projectile> spawnProjectile) {
        return false;
    }
}
