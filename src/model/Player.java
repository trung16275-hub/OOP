
package model;

import java.util.ArrayList;
import java.util.List;

import model.weapon.Weapon;

public class Player extends Combatant {

    // ================= DEFAULT STATS =================

    private static final String DEFAULT_NAME = "Hero";
    private static final int DEFAULT_MAX_HP = 100;
    private static final int DEFAULT_SPEED = 10;
    private static final int DEFAULT_ARMOR = 30;

    private static final int DEFAULT_WIDTH = 50;
    private static final int DEFAULT_HEIGHT = 50;

    private static final double DEFAULT_HITBOX_RADIUS = 25.0;
    private static final double DEFAULT_DETECTION_RANGE = 250.0;

    // ================= PLAYER STATS =================

    private int armor = DEFAULT_ARMOR;
    private double detectionRange = DEFAULT_DETECTION_RANGE;

    // ================= WEAPON SYSTEM =================

    private final List<Weapon> weapons = new ArrayList<>();

    private int currentWeaponIndex = -1;

    private long lastAttackTime = 0;
    private boolean hasAttacked = false;

    // ================= CONSTRUCTORS =================

    public Player() {
        this(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED,
                DEFAULT_HITBOX_RADIUS);
    }

    public Player(String name, int maxHp, int speed) {
        this(name, maxHp, speed, DEFAULT_HITBOX_RADIUS);
    }

    public Player(String name, int maxHp, int speed,
                  double hitboxRadius) {

        super(name, maxHp, speed, hitboxRadius);

        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }

    // ================= ARMOR =================

    public int getArmor() {
        return armor;
    }

    @Override
    public void takeDamage(int dmg) {

        if (dmg <= 0 || isDead()) {
            return;
        }

        int absorbed = Math.min(armor, dmg);

        armor -= absorbed;

        int remainingDamage = dmg - absorbed;

        super.takeDamage(remainingDamage);
    }

    // ================= DETECTION =================

    public double getDetectionRange() {
        return detectionRange;
    }

    public void setDetectionRange(double detectionRange) {

        if (!Double.isFinite(detectionRange)
                || detectionRange < 0) {

            throw new IllegalArgumentException(
                    "Detection range must be finite and non-negative."
            );
        }

        this.detectionRange = detectionRange;
    }

    public boolean canDetect(Enemy enemy) {

        if (enemy == null || enemy.isDead() || isDead()) {
            return false;
        }

        double dx =
                (enemy.getX() + enemy.getWidth() / 2.0)
                        - (getX() + getWidth() / 2.0);

        double dy =
                (enemy.getY() + enemy.getHeight() / 2.0)
                        - (getY() + getHeight() / 2.0);

        return dx * dx + dy * dy
                <= detectionRange * detectionRange;
    }

    // ================= WEAPON EQUIPMENT =================

    public List<Weapon> getWeapons() {
        return new ArrayList<>(weapons);
    }

    public Weapon getCurrentWeapon() {

        if (currentWeaponIndex < 0
                || currentWeaponIndex >= weapons.size()) {
            return null;
        }

        return weapons.get(currentWeaponIndex);
    }

    public void equip(Weapon weapon) {

        if (weapon == null) {
            throw new IllegalArgumentException(
                    "Weapon cannot be null."
            );
        }

        weapons.add(weapon);

        currentWeaponIndex = weapons.size() - 1;

        System.out.println(
                "Đã trang bị: " + weapon.getName()
        );
    }

    public void switchWeapon() {

        if (weapons.size() <= 1) {
            return;
        }

        currentWeaponIndex =
                (currentWeaponIndex + 1) % weapons.size();

        System.out.println(
                "Đã đổi sang: " + getCurrentWeapon().getName()
        );
    }

    public void unequip() {

        weapons.clear();
        currentWeaponIndex = -1;

        lastAttackTime = 0;
        hasAttacked = false;
    }

    // ================= ATTACK SYSTEM =================

    @Override
    public void attack(Combatant target) {

        Weapon currentWeapon = getCurrentWeapon();

        if (currentWeapon == null
                || target == null
                || target == this
                || isDead()
                || target.isDead()) {

            return;
        }

        long now = System.nanoTime();

        // Cooldown riêng của vũ khí hiện tại.
        if (hasAttacked
                && now - lastAttackTime
                < currentWeapon.getCooldownNanos()) {

            return;
        }

        // Vũ khí tự quyết định cách tấn công thông qua Strategy.
        currentWeapon.attack(this, target);

        lastAttackTime = now;
        hasAttacked = true;
    }
}


