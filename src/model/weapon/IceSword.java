package model.weapon;

import model.weapon.behavior.MeleeSlashBehavior;

public class IceSword extends Weapon {

    public IceSword() {
        super(
                "Kiếm băng",
                25,
                75,
                900_000_000L,
                new MeleeSlashBehavior()
        );
    }
}
