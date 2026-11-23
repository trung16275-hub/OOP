package model.weapon;

public class Ak47 extends Weapon {
    private static final String DEFAULT_NAME = "AK47";
    private static final int DEFAULT_DAMAGE = 25;

    public Ak47() {
        super(DEFAULT_NAME, DEFAULT_DAMAGE);
    }
}
