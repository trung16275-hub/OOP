package model.weapon;

import map.Room;
import model.Enemy;
import model.Projectile;
import model.player.Player;
import java.util.function.Consumer;

public class Bat extends Weapon {
    private static final String DEFAULT_NAME = "Bat";
    private static final int DEFAULT_DAMAGE = 15;

    public Bat() {
        super(DEFAULT_NAME, DEFAULT_DAMAGE);
    }

    @Override
    public long getAttackCooldownNanos() {
        return 450_000_000L;
    }

    @Override
    public boolean tryAttack(Player attacker, Room room, Consumer<Projectile> spawnProjectile) {
        for (Enemy enemy : room.getEnemies()) {
            if (enemy != null && !enemy.isDead() && attacker.isWithinHitbox(enemy)) {
                attacker.attack(enemy);
            }
        }
        return true;
    }
}
