
package model;

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

    // Cooldown giữa hai lần tấn công: 0.5 giây.
    private static final long ATTACK_COOLDOWN_NANOS = 500_000_000L;

    // ================= PLAYER STATS =================

    private int armor = DEFAULT_ARMOR;
    private double detectionRange = DEFAULT_DETECTION_RANGE;

    // ================= WEAPON SYSTEM =================

    private Weapon currentWeapon;
    private Weapon meleeWeapon;
    private Weapon rangedWeapon;

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

        // Armor hấp thụ damage trước.
        int absorbed = Math.min(armor, dmg);

        armor -= absorbed;

        // Phần damage còn lại mới trừ vào HP.
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

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public Weapon getMeleeWeapon() {
        return meleeWeapon;
    }

    public Weapon getRangedWeapon() {
        return rangedWeapon;
    }

    public void equip(Weapon weapon) {

        if (weapon == null) {
            throw new IllegalArgumentException(
                    "Weapon cannot be null."
            );
        }

        if (weapon.getType() == Weapon.Type.MELEE) {

            meleeWeapon = weapon;

        } else {

            rangedWeapon = weapon;
        }

        // Vũ khí vừa trang bị sẽ được chọn làm vũ khí hiện tại.
        currentWeapon = weapon;

        System.out.println("Đã trang bị: " + weapon.name);
    }

    // Chuyển đổi giữa vũ khí cận chiến và tầm xa.
    public void switchWeapon() {

        if (currentWeapon == null) {

            if (meleeWeapon != null) {
                currentWeapon = meleeWeapon;
            } else {
                currentWeapon = rangedWeapon;
            }

            return;
        }

        if (currentWeapon.getType() == Weapon.Type.MELEE
                && rangedWeapon != null) {

            currentWeapon = rangedWeapon;

        } else if (currentWeapon.getType() == Weapon.Type.RANGED
                && meleeWeapon != null) {

            currentWeapon = meleeWeapon;
        }

        if (currentWeapon != null) {
            System.out.println(
                    "Đã đổi sang: " + currentWeapon.name
            );
        }
    }

    // Tháo toàn bộ vũ khí.
    public void unequip() {

        currentWeapon = null;
        meleeWeapon = null;
        rangedWeapon = null;
    }

    // ================= ATTACK SYSTEM =================

    @Override
    public void attack(Combatant target) {

        if (currentWeapon == null
                || target == null
                || target == this
                || isDead()
                || target.isDead()) {

            return;
        }

        long now = System.nanoTime();

        // Kiểm tra cooldown.
        if (hasAttacked
                && now - lastAttackTime < ATTACK_COOLDOWN_NANOS) {

            return;
        }

        // Tính khoảng cách giữa tâm hai nhân vật.
        double dx =
                (target.getX() + target.getWidth() / 2.0)
                        - (getX() + getWidth() / 2.0);

        double dy =
                (target.getY() + target.getHeight() / 2.0)
                        - (getY() + getHeight() / 2.0);

        double distanceSquared = dx * dx + dy * dy;

        double attackRange = currentWeapon.getRange();

        // Cận chiến tính thêm hitbox của hai nhân vật.
        if (currentWeapon.getType() == Weapon.Type.MELEE) {

            attackRange += getHitboxRadius()
                    + target.getHitboxRadius();
        }

        // Ngoài tầm đánh thì không gây damage.
        if (distanceSquared > attackRange * attackRange) {
            return;
        }

        // Gây sát thương cho mục tiêu.
        target.takeDamage(currentWeapon.damage);

        lastAttackTime = now;
        hasAttacked = true;

        System.out.println(
                getName() + " tấn công "
                        + target.getName()
                        + " bằng " + currentWeapon.name
                        + ", gây " + currentWeapon.damage
                        + " damage."
        );
    }
}

