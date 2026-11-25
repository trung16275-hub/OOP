package model.player;

import model.Combatant;
import model.Enemy;
import model.weapon.Weapon;

import java.awt.Rectangle;

public class Player extends Combatant {
    private static final String DEFAULT_NAME = "Hero";
    private static final int DEFAULT_MAX_HP = 100;
    private static final int DEFAULT_SPEED = 3;
    private static final int DEFAULT_ARMOR = 30;
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;
    private static final double DEFAULT_HITBOX_RADIUS = 60;
    public static final double DASH_SPEED_MULTIPLIER = 1.7;
    public static final long DASH_DURATION_MS = 600;
    private static final long DASH_COOLDOWN_MS = 800;

    private final HurtBox hurtBox = new HurtBox(0, 0, 35, 60);
    private final MovementHitbox movementHitbox = new MovementHitbox(14, 20, 30, 42);
    private int armor = DEFAULT_ARMOR;
    private Weapon currentWeapon;
    private boolean moving;
    private boolean facingLeft;
    private long attackAnimationStartTime;
    private long hurtAnimationStartTime;
    private long attackAnimationEndTime;
    private long dashStartTime;
    private long dashEndTime;
    private long nextDashAvailableTime;
    private double dashDirectionX;
    private double dashDirectionY;
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

    public Rectangle getHurtBounds() {
        return hurtBox.getBounds(getX(), getY(), getWidth(), facingLeft);
    }

    public Rectangle getMovementBounds() {
        return getMovementBounds(getX(), getY());
    }

    public boolean intersectsHurtBox(Combatant attacker) {
        if (attacker == null || attacker == this) {
            return false;
        }
        return getHurtBounds().intersects(
            new Rectangle(
                        (int) Math.floor(attacker.getX()),
                        (int) Math.floor(attacker.getY()),
                        attacker.getWidth(),
                        attacker.getHeight()));
    }

    @Override
    protected Rectangle getMovementBounds(double candidateX, double candidateY) {
        return movementHitbox.getBounds(candidateX, candidateY, getWidth(), facingLeft);
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

    public boolean startDash(double directionX, double directionY) {
        long now = System.currentTimeMillis();
        if (isDead() || now < nextDashAvailableTime) {
            return false;
        }

        if (directionX == 0 && directionY == 0) {
            directionX = aimX;
            directionY = aimY;
        }
        double length = Math.hypot(directionX, directionY);
        if (length == 0 || !Double.isFinite(length)) {
            return false;
        }

        dashDirectionX = directionX / length;
        dashDirectionY = directionY / length;
        dashStartTime = now;
        dashEndTime = now + DASH_DURATION_MS;
        nextDashAvailableTime = now + DASH_COOLDOWN_MS;
        stopAttackAnimation();
        if (dashDirectionX != 0) {
            facingLeft = dashDirectionX < 0;
        }
        return true;
    }

    public boolean isDashing() {
        return System.currentTimeMillis() < dashEndTime;
    }

    public double getDashCooldownProgress() {
        long remainingMs = Math.max(0, nextDashAvailableTime - System.currentTimeMillis());
        return Math.max(0.0, Math.min(1.0, 1.0 - remainingMs / (double) DASH_COOLDOWN_MS));
    }

    public long getDashStartTime() {
        return dashStartTime;
    }

    public double getDashDirectionX() {
        return dashDirectionX;
    }

    public double getDashDirectionY() {
        return dashDirectionY;
    }

    public void playAttackAnimation(int durationMs) {
        attackAnimationStartTime = System.currentTimeMillis();
        attackAnimationEndTime = attackAnimationStartTime + durationMs;
    }

    public long getAttackAnimationStartTime() {
        return attackAnimationStartTime;
    }

    public long getHurtAnimationStartTime() {
        return hurtAnimationStartTime;
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
        hurtAnimationStartTime = System.currentTimeMillis();
        int absorbed = Math.min(armor, damage);
        armor -= absorbed;
        super.takeDamage(damage - absorbed);
    }
}
