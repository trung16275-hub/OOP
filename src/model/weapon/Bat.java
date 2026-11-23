package model.weapon;

public class Bat extends Weapon {
    private static final String DEFAULT_NAME = "Bat";
    private static final int DEFAULT_DAMAGE = 15;

    public Bat() {
        super(DEFAULT_NAME, DEFAULT_DAMAGE);
    }
}
