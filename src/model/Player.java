package model;

import model.weapon.Weapon;
import model.weapon.AlmondWater;
import java.util.Collections;
import java.util.List;
import model.boss.Boss4;
// File: src/player/Player.java
public class Player extends Combatant {
    // Chỉnh các chỉ số mặc định của Player tại đây.
    private static final String DEFAULT_NAME = "Hero";
    private static final int DEFAULT_MAX_HP = 100;
    private static final int DEFAULT_SPEED = 4;
    private static final int DEFAULT_ARMOR = 30;
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;
    private static final double DEFAULT_HITBOX_RADIUS = 60;
    private static final double DEFAULT_DETECTION_RANGE = 250.0;
    private String statusMessage = "";
    private long statusMessageEndTime = 0L;
    private int armor = DEFAULT_ARMOR;
    private double detectionRange = DEFAULT_DETECTION_RANGE;
    private Weapon currentWeapon;
    private boolean moving;
    private boolean facingLeft;
    private long attackAnimationStartTime;
    private long attackAnimationEndTime;
    private double aimX = 1;
    private double aimY;

    //default constructor for testing
    public Player() {
        this(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED, DEFAULT_HITBOX_RADIUS);
    }

    public Player (String name, int maxHp, int speed) {
        this(name, maxHp, speed, DEFAULT_HITBOX_RADIUS);
    }

    public Player(String name, int maxHp, int speed, double hitboxRadius) {
        super(name, maxHp, speed, hitboxRadius);
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
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

    public int getArmor ( )
    {
        return armor ;
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

    public void equip(Weapon w) {
        this.currentWeapon = w;
        System.out.println("Đã trang bị: " + w.getName());
    }

    @Override
    public void attack(Combatant target) {
        if (currentWeapon != null && target != null && currentWeapon.consumeAmmo()) {
            target.takeDamage(currentWeapon.getDamage());
    attack(target, null);
}
    }

// Hàm đầy đủ hỗ trợ vật phẩm diện rộng như Nước Hạnh Nhân
public void attack(Combatant target, List<Enemy> enemies) {
    if (currentWeapon == null) {
        return;
    }

    // 1. Luồng dùng Nước Hạnh Nhân (không cần target != null)
    if (currentWeapon instanceof AlmondWater) {
        AlmondWater water = (AlmondWater) currentWeapon;

        // Phòng trường hợp nơi gọi chưa truyền list enemies nhưng có target là Boss4
        if (enemies == null && target instanceof Boss4) {
            enemies = Collections.singletonList((Enemy) target);
        }

        if (water.use(this, enemies)) {
            showStatusMessage("Đã dùng Nước Hạnh Nhân! Còn lại: " + water.getUsesLeft() + " chai");
        } else {
            showStatusMessage("Đã hết Nước Hạnh Nhân!");
        }
        return;
    }

    // 2. Luồng dùng vũ khí gây sát thương bình thường
    if (target != null) {
        target.takeDamage(currentWeapon.getDamage());
    }
}

public void showStatusMessage(String msg) {
    this.statusMessage = msg;
    this.statusMessageEndTime = System.nanoTime() + 2_000_000_000L; // Hiện trong 2 giây
    System.out.println(msg); // In thêm ra terminal để dễ kiểm tra
}

public String getStatusMessage() {
    if (System.nanoTime() < statusMessageEndTime) {
        return statusMessage;
    }
    return "";
}
    @Override
    public void takeDamage ( int dmg )
    {
        if ( dmg <= 0 || isDead ( ) )
        {
            return ;
        }
        int absorbed = Math.min ( armor , dmg ) ;
        armor -= absorbed ;
        super.takeDamage ( dmg - absorbed ) ;
    }
    private boolean grabbed = false;

public boolean isGrabbed() { return grabbed; }
public void setGrabbed(boolean grabbed) { this.grabbed = grabbed; }
}
