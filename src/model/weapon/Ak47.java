package model.weapon;

import map.Room;
import model.Projectile;
import model.player.Player;
import model.pickup.AmmoPickup;
import java.util.function.Consumer;

public class Ak47 extends Weapon {
    private static final String DEFAULT_NAME = "AK47";
    private static final int DEFAULT_DAMAGE = 20;
    private static final int MAGAZINE_SIZE = 30;
    private int ammo = MAGAZINE_SIZE;

    public Ak47() {
        super(DEFAULT_NAME, DEFAULT_DAMAGE);
    }

    public int getAmmo() {
        return ammo;
    }

    public int getMagazineSize() {
        return MAGAZINE_SIZE;
    }

    public boolean hasAmmo() {
        return ammo > 0;
    }

    public int addAmmo(int rounds) {
        int added = Math.min(Math.max(0, rounds), MAGAZINE_SIZE - ammo);
        ammo += added;
        return added;
    }

    @Override
    public int addAmmo(AmmoPickup.Kind kind, int rounds) {
        return kind == AmmoPickup.Kind.AK47 ? addAmmo(rounds) : 0;
    }

    /** Consumes one round and returns false if the magazine is empty. */
    @Override
    public boolean consumeAmmo() {
        if (!hasAmmo()) {
            return false;
        }
        ammo--;
        return true;
    }

    @Override
    public long getAttackCooldownNanos() {
        return 180_000_000L;
    }

    @Override
    public boolean tryAttack(Player attacker, Room room, Consumer<Projectile> spawnProjectile) {
        if (!consumeAmmo()) {
            return false;
        }
        spawnProjectile.accept(new Projectile(attacker.getCenterX(), attacker.getCenterY(),
                attacker.getAimX(), attacker.getAimY(), 12.0, getDamage()));
        return true;
    }
}
