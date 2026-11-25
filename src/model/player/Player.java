package model.player;

import model.Combatant;
import model.Enemy;
import model.weapon.Weapon;

import java.awt.Rectangle;

public class Player extends Combatant {
    private static final String DEFAULT_NAME = "Hero";
    private static final int DEFAULT_MAX_HP = 100;
    private static final int DEFAULT_SPEED = 4;
    private static final int DEFAULT_ARMOR = 30;
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;
    private static final double DEFAULT_HITBOX_RADIUS = 60;
    private static final double DEFAULT_DETECTION_RANGE = 250.0;

    private final HurtBox hurtBox = new HurtBox(0, 0, 35, 60);
    private final MovementHitbox movementHitbox = new MovementHitbox(14, 20, 30, 42);
    private int armor = DEFAULT_ARMOR;
    private double detectionRange = DEFAULT_DETECTION_RANGE;
    private Weapon currentWeapon;
    private boolean moving;
    private boolean facingLeft;
    private long attackAnimationStartTime;
    private long attackAnimationEndTime;
    private double aimX = 1;
    private double aimY;

    public Player() {
        this(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED, DEFAULT_HITBOX_RADIUS);
    }

    public Player(String name, int maxHp, int speed) {
        this(name, maxHp, speed, DEFAULT_HITBOX_RADIUS);
    }

    public Player(String name, int maxHp, int speed, double hitboxRadius) {
        super(name, maxHp, speed, hitboxRadius);
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }

    public HurtBox getHurtBox() {
        return hurtBox;
    }

    public MovementHitbox getMovementHitbox() {
        return movementHitbox;
    }

    public boolean intersectsHurtBox(Combatant attacker) {
        if (attacker == null || attacker == this) {
            return false;
        }
        return hurtBox.getBounds(getX(), getY()).intersects(
            new Rectangle(
                        (int) Math.floor(attacker.getX()),
                        (int) Math.floor(attacker.getY()),
                        attacker.getWidth(),
                        attacker.getHeight()));
    }

    @Override
    protected Rectangle getMovementBounds(double candidateX, double candidateY) {
        return movementHitbox.getBounds(candidateX, candidateY);
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }

    public void setFacingLeft(boolean facingLeft) {
        this.facingLeft = facingLeft;
    }

    public double getAimX() {
        return aimX;
    }

    public double getAimY() {
        return aimY;
    }

    public void setAimDirection(double directionX, double directionY) {
        double length = Math.hypot(directionX, directionY);
        if (length == 0 || !Double.isFinite(length)) {
            return;
        }
        aimX = directionX / length;
        aimY = directionY / length;
        if (directionX != 0) {
            facingLeft = directionX < 0;
        }
    }

    public void playAttackAnimation(int durationMs) {
        attackAnimationStartTime = System.currentTimeMillis();
        attackAnimationEndTime = attackAnimationStartTime + durationMs;
    }

    public long getAttackAnimationStartTime() {
        return attackAnimationStartTime;
    }

    public boolean isAttacking() {
        return System.currentTimeMillis() < attackAnimationEndTime;
    }

    public void stopAttackAnimation() {
        attackAnimationEndTime = 0;
        attackAnimationStartTime = 0;
    }

    public int getArmor() {
        return armor;
    }

    public double getDetectionRange() {
        return detectionRange;
    }

    public void setDetectionRange(double detectionRange) {
        if (!Double.isFinite(detectionRange) || detectionRange < 0) {
            throw new IllegalArgumentException("Detection range must be a finite, non-negative value.");
        }
        this.detectionRange = detectionRange;
    }

    public boolean canDetect(Enemy enemy) {
        if (enemy == null || enemy.isDead() || isDead()) {
            return false;
        }

        double dx = (enemy.getX() + enemy.getWidth() / 2.0)
                  - (getX() + getWidth() / 2.0);
        double dy = (enemy.getY() + enemy.getHeight() / 2.0)
                  - (getY() + getHeight() / 2.0);

        return dx * dx + dy * dy <= detectionRange * detectionRange;
    }

    public void equip(Weapon weapon) {
        this.currentWeapon = weapon;
        System.out.println("Đã trang bị: " + weapon.getName());
    }

    @Override
    public void attack(Combatant target) {
        if (currentWeapon != null && target != null && currentWeapon.consumeAmmo()) {
            target.takeDamage(currentWeapon.getDamage());
        }
    }

    @Override
    public void takeDamage(int damage) {
        if (damage <= 0 || isDead()) {
            return;
        }
        int absorbed = Math.min(armor, damage);
        armor -= absorbed;
        super.takeDamage(damage - absorbed);
    }
}