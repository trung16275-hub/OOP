
package model.boss.boss2;

import model.Enemy;
import model.Projectile;
import model.player.Player;
import map.Room;
import java.util.List;

public class Boss2 extends Enemy {

    // =========================
    // CHỈ SỐ CƠ BẢN
    // =========================

    private static final String DEFAULT_NAME = "Boss 2";
    private static final int DEFAULT_MAX_HP = 240;
    private static final int DEFAULT_SPEED = 2;
    private static final int DEFAULT_BASE_DAMAGE = 10;
    private static final double DEFAULT_DETECTION_RANGE = 300;
    private static final double DEFAULT_HITBOX_RADIUS = 35;
    private static final int DEFAULT_WIDTH = 70;
    private static final int DEFAULT_HEIGHT = 70;

    // =========================
    // KỸ NĂNG PHUN AXIT
    // =========================

    private static final int ACID_DAMAGE = 12;
    private static final long ACID_COOLDOWN = 2_800_000_000L;

    private static final long ATTACK_ANIMATION_DURATION = 450_000_000L;
    private long attackAnimationEnd;

    // =========================
    // KỸ NĂNG NỔ AXIT
    // =========================

    private static final int EXPLOSION_DAMAGE = 20;
    private static final double EXPLOSION_TRIGGER_RANGE = 90;
    private static final double EXPLOSION_RADIUS = 110;

    private static final long EXPLOSION_COOLDOWN = 5_000_000_000L;
    private static final long EXPLOSION_DURATION = 500_000_000L;

    private long lastExplosion;
    private long explosionStart;
    private boolean exploding;

    // =========================
    // KỸ NĂNG TÁCH ĐÔI
    // =========================

    private static final double SPLIT_HP_THRESHOLD = 0.30;
    private static final long SPLIT_ANIMATION_DURATION = 700_000_000L;

    private boolean splitTriggered;
    private boolean splitRequested;
    private long splitAnimationEnd;

    // =========================
    // TRẠNG THÁI ANIMATION
    // =========================

    private boolean moving;

    private static final long HURT_ANIMATION_DURATION = 350_000_000L;
    private long hurtAnimationEnd;

    // =========================
    // CONSTRUCTOR
    // =========================

    public Boss2() {
        this(DEFAULT_HITBOX_RADIUS);
    }

    public Boss2(double hitboxRadius) {
        super(
                DEFAULT_NAME,
                DEFAULT_MAX_HP,
                DEFAULT_SPEED,
                DEFAULT_BASE_DAMAGE,
                DEFAULT_DETECTION_RANGE,
                hitboxRadius
        );

        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }

    // =========================
    // CẬP NHẬT HÀNH VI
    // =========================

    @Override
    public void updateBehavior(Player player, Room room) {

        long now = System.nanoTime();

        if (exploding && now - explosionStart >= EXPLOSION_DURATION) {
            exploding = false;
        }

        if (isDead()) {
            moving = false;
            return;
        }

        if (player == null) {
            moving = false;
            return;
        }

        // Đang thực hiện tách đôi
        if (splitTriggered) {
            moving = false;
            return;
        }

        if (!canDetect(player)) {
            moving = false;
            return;
        }

        faceToward(player);

        double distance = distanceTo(player);

        // =========================
        // KỸ NĂNG NỔ AXIT
        // =========================

        if (distance <= EXPLOSION_TRIGGER_RANGE
                && now - lastExplosion >= EXPLOSION_COOLDOWN) {

            exploding = true;
            explosionStart = now;
            lastExplosion = now;

            moving = false;

            if (distance <= EXPLOSION_RADIUS) {
                player.takeDamage(EXPLOSION_DAMAGE);
            }

            return;
        }

        // =========================
        // DI CHUYỂN VÀ PHUN AXIT
        // =========================

        if (distance > EXPLOSION_TRIGGER_RANGE) {

            if (distance > 140) {

                double oldX = getX();
                double oldY = getY();

                chase(player, room);

                moving = getX() != oldX || getY() != oldY;

            } else {
                moving = false;
            }

            if (requestAction(ACID_COOLDOWN, (currentRoom, spawnProjectile) -> {
                double dx = player.getCenterX() - getCenterX();
                double dy = player.getCenterY() - getCenterY();
                if (dx != 0 || dy != 0) {
                    spawnProjectile.accept(new Projectile(getCenterX(), getCenterY(), dx, dy,
                            ACID_PROJECTILE_SPEED, getAcidDamage(), ACID_PROJECTILE_SIZE, Projectile.Kind.ACID));
                }
                return true;
            })) {
                attackAnimationEnd = now + ATTACK_ANIMATION_DURATION;
            }

        } else {
            moving = false;
        }
    }

    // =========================
    // NHẬN SÁT THƯƠNG
    // =========================

    @Override
    public void takeDamage(int damage) {

        if (damage <= 0 || isDead() || splitTriggered) {
            return;
        }

        super.takeDamage(damage);

        if (isDead()) {
            moving = false;
            return;
        }

        hurtAnimationEnd = System.nanoTime() + HURT_ANIMATION_DURATION;

        // Kích hoạt tách đôi khi HP còn 30%
        if (getHp() <= DEFAULT_MAX_HP * SPLIT_HP_THRESHOLD) {

            splitTriggered = true;
            splitRequested = true;

            moving = false;

            splitAnimationEnd =
                    System.nanoTime() + SPLIT_ANIMATION_DURATION;
        }
    }

    // =========================
    // HỖ TRỢ GAMEPANEL
    // =========================

    public int getAcidDamage() {
        return ACID_DAMAGE;
    }

    public double getExplosionRadius() {
        return EXPLOSION_RADIUS;
    }

    public boolean isExploding() {
        return exploding;
    }

    public boolean consumeSplitRequest() {

        if (!splitRequested) {
            return false;
        }

        splitRequested = false;
        return true;
    }

    public boolean isSplitTriggered() {
        return splitTriggered;
    }

    // Kiểm tra hoạt ảnh phân thân đã kết thúc
    public boolean isSplitAnimationFinished() {
        return splitTriggered
                && System.nanoTime() >= splitAnimationEnd;
    }

    @Override
    public boolean shouldReplace() {
        return isSplitAnimationFinished();
    }

    @Override
    public List<Boss2Minion> createReplacements(Room room) {
        return List.of(new Boss2Minion(getX() - 20, getY() + 15),
                new Boss2Minion(getX() + 55, getY() + 15));
    }

    // =========================
    // HỖ TRỢ ANIMATION
    // =========================

    public static final double ACID_PROJECTILE_SPEED = 5.5;
    public static final int ACID_PROJECTILE_SIZE = 12;

    @Override
    public String getSpriteKey() {
        return "boss2";
    }

    @Override
    public int getAnimFrameDurationMs(String state) {
        return switch (state) {
            case "run" -> 110;
            case "attack", "back_burst" -> 90;
            case "hurt" -> 80;
            default -> 220;
        };
    }

    @Override
    public String getAnimState() {

        long now = System.nanoTime();

        if (splitTriggered && now < splitAnimationEnd) {
            return "split";
        }

        if (exploding) {
            return "back_burst";
        }

        if (now < hurtAnimationEnd) {
            return "hurt";
        }

        if (now < attackAnimationEnd) {
            return "attack";
        }

        if (moving) {
            return "run";
        }

        return "idle";
    }
}
