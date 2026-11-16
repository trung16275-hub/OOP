package model.weapon;

import model.weapon.behavior.ProjectileBehavior;

public class Bow extends Weapon {

    public Bow() {
        super(
                "Cung",
                15,
                250,
                700_000_000L,
                new ProjectileBehavior()
        );
    }
}