package model.weapon;

import model.weapon.behavior.MeleeSlashBehavior;

public class FireSword extends Weapon {

    public FireSword() {
        super(
                "Kiếm lửa",
                30,
                80,
                800_000_000L,
                new MeleeSlashBehavior()
        );
    }
}
