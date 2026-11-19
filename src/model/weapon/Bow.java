package model.weapon;

import model.Combatant;
import model.Player;

/** Vũ khí tầm xa có tầm đánh bằng detection range của người chơi. */
public class Bow extends Weapon {
    public Bow(String name, int damage, long cooldownMillis) {
        // Tầm hitbox được lấy động từ Player, nên giá trị cơ sở không được sử dụng.
        super(name, damage, 0, cooldownMillis);
    }

    @Override
    public double getHitboxRange(Player attacker) {
        return attacker == null ? 0 : attacker.getDetectionRange();
    }

    @Override
    public void attack(Player attacker, Combatant target) {
        if (isTargetInHitbox(attacker, target)) {
            target.takeDamage(getDamage());
        }
    }
}
