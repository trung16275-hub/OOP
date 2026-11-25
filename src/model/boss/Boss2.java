
package model.boss;

import model.Enemy;
import model.Player;
import map.Room;

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

    private long lastAcidAttack;
    private boolean acidAttackRequested;

    // Thời gian hiển thị animation tấn công
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
    // TRẠNG THÁI HÓA KÉN
    // =========================

    private static final double COCOON_HP_THRESHOLD = 0.30;
    private static final int COCOON_MAX_HP = 70;

    private int cocoonHP = COCOON_MAX_HP;
    private boolean cocooned;
    private boolean cocoonActivated;

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

        // Cập nhật trạng thái vụ nổ
        if (exploding && now - explosionStart >= EXPLOSION_DURATION) {
            exploding = false;
        }

        // Boss không thể hoạt động khi chết
        if (isDead()) {
            moving = false;
            return;
        }

        // Không có Player
        if (player == null) {
            moving = false;
            return;
        }

        // Khi hóa kén, Boss không di chuyển hoặc tấn công
        if (cocooned) {
            moving = false;
            return;
        }

        // Không phát hiện Player
        if (!canDetect(player)) {
            moving = false;
            return;
        }

        // Quay mặt về phía Player
        faceToward(player);

        double bossCenterX = getX() + getWidth() / 2.0;
        double bossCenterY = getY() + getHeight() / 2.0;

        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        double dx = playerCenterX - bossCenterX;
        double dy = playerCenterY - bossCenterY;

        double distance = Math.sqrt(dx * dx + dy * dy);

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

            // Giữ khoảng cách với Player
            if (distance > 140) {

                double oldX = getX();
                double oldY = getY();

                chase(player, room);

                // Kiểm tra Boss có thực sự di chuyển không
                moving = getX() != oldX || getY() != oldY;

            } else {
                moving = false;
            }

            // Yêu cầu GamePanel tạo đạn axit
            if (now - lastAcidAttack >= ACID_COOLDOWN) {

                acidAttackRequested = true;
                lastAcidAttack = now;

                // Kích hoạt animation phun axit
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

        if (damage <= 0 || isDead()) {
            return;
        }

        // Khi hóa kén, sát thương tác động lên kén trước
        if (cocooned) {

            cocoonHP -= damage;

            if (cocoonHP <= 0) {
                cocoonHP = 0;
                cocooned = false;
            }

            return;
        }

        // Boss nhận sát thương bình thường
        super.takeDamage(damage);

        // Kích hoạt animation bị thương
        if (!isDead()) {
            hurtAnimationEnd = System.nanoTime() + HURT_ANIMATION_DURATION;
        }

        // Hóa kén duy nhất một lần khi HP còn 30%
        if (!isDead()
                && !cocoonActivated
                && getHp() <= DEFAULT_MAX_HP * COCOON_HP_THRESHOLD) {

            cocooned = true;
            cocoonActivated = true;

            moving = false;
        }
    }

    // =========================
    // CÁC PHƯƠNG THỨC HỖ TRỢ GAMEPANEL
    // =========================

    public boolean consumeAcidAttackRequest() {

        if (!acidAttackRequested) {
            return false;
        }

        acidAttackRequested = false;
        return true;
    }

    public int getAcidDamage() {
        return ACID_DAMAGE;
    }

    public double getExplosionRadius() {
        return EXPLOSION_RADIUS;
    }

    public boolean isExploding() {
        return exploding;
    }

    public boolean isCocooned() {
        return cocooned;
    }

    public int getCocoonHP() {
        return cocoonHP;
    }

    public int getCocoonMaxHP() {
        return COCOON_MAX_HP;
    }

    public long getExplosionStart() {
        return explosionStart;
    }

    // =========================
    // HỖ TRỢ ANIMATION
    // =========================

    @Override
    public String getSpritePath() {
        return "boss2";
    }

    @Override
    public int getSpriteFrameCount() {
        return 4;
    }

    @Override
    public String getAnimState() {

        long now = System.nanoTime();

        // Ưu tiên trạng thái hóa kén
        if (cocooned) {
            return "cocoon";
        }

        // Animation nổ axit từ các túi axit phía sau
        if (exploding) {
            return "back_burst";
        }

        // Animation bị thương
        if (now < hurtAnimationEnd) {
            return "hurt";
        }

        // Animation phun axit
        if (now < attackAnimationEnd) {
            return "attack";
        }

        // Animation di chuyển
        if (moving) {
            return "run";
        }

        // Trạng thái mặc định
        return "idle";
    }
}
