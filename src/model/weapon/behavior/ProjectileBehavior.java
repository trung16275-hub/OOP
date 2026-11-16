package model.weapon.behavior;

import model.Combatant;
import model.weapon.Weapon;

public class ProjectileBehavior implements WeaponBehavior {

    @Override
    public void attack(Weapon weapon, Combatant attacker, Combatant target) {
        if (attacker == null || target == null) {
            return;
        }

        if (attacker == target || target.isDead()) {
            return;
        }

        double dx = (target.getX() + target.getWidth() / 2.0)
                - (attacker.getX() + attacker.getWidth() / 2.0);

        double dy = (target.getY() + target.getHeight() / 2.0)
                - (attacker.getY() + attacker.getHeight() / 2.0);

        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= weapon.getRange()) {
            target.takeDamage(weapon.getDamage());
        }
    }
}
