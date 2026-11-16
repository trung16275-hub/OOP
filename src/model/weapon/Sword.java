package model.weapon;

import model.weapon.behavior.MeleeSlashBehavior;

public class Sword extends Weapon {

    public Sword() {
        super(
                "Kiếm",
                20,
                70,
                500_000_000L,
                new MeleeSlashBehavior()
        );
    }
}
