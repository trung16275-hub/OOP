package model.weapon.behavior;

import model.Combatant;
import model.weapon.Weapon;

public interface WeaponBehavior {
    void attack(Weapon weapon, Combatant attacker, Combatant target);
}
