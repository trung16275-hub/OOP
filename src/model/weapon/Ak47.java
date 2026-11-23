package model.weapon;

public class Ak47 extends Weapon {
    private static final String DEFAULT_NAME = "AK47";
    private static final int DEFAULT_DAMAGE = 20;
    private static final int MAGAZINE_SIZE = 30;
    private int ammo = MAGAZINE_SIZE;

    public Ak47() {
        super(DEFAULT_NAME, DEFAULT_DAMAGE);
    }

    public int getAmmo() {
        return ammo;
    }

    public int getMagazineSize() {
        return MAGAZINE_SIZE;
    }

    public boolean hasAmmo() {
        return ammo > 0;
    }

    /** Consumes one round and returns false if the magazine is empty. */
    @Override
    public boolean consumeAmmo() {
        if (!hasAmmo()) {
            return false;
        }
        ammo--;
        return true;
    }
}
