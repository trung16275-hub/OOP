
package model.boss;

import model.Enemy;
import model.Player;
import map.Room;

public class Boss2 extends Enemy {

    // Chỉ số cơ bản
    private static final String DEFAULT_NAME = "Boss 2";
    private static final int DEFAULT_MAX_HP = 240;
    private static final int DEFAULT_SPEED = 2;
    private static final int DEFAULT_BASE_DAMAGE = 10;
    private static final double DEFAULT_DETECTION_RANGE = 300;
    private static final double DEFAULT_HITBOX_RADIUS = 35;
    private static final int DEFAULT_WIDTH = 70;
    private static final int DEFAULT_HEIGHT = 70;

    // Kỹ năng phun axit
    private static final int ACID_DAMAGE = 12;
    private static final long ACID_COOLDOWN = 2_800_000_000L;
    private long lastAcidAttack;
    private boolean acidAttackRequested;

    // Kỹ năng nổ axit
    private static final int EXPLOSION_DAMAGE = 20;
    private static final double EXPLOSION_TRIGGER_RANGE = 90;
    private static final double EXPLOSION_RADIUS = 110;
    private static final long EXPLOSION_COOLDOWN = 5_000_000_000L;
    private static final long EXPLOSION_DURATION = 500_000_000L;
    private long lastExplosion;
    private long explosionStart;
    private boolean exploding;

    // Trạng thái hóa kén
    private static final double COCOON_HP_THRESHOLD = 0.30;
    private static final int COCOON_MAX_HP = 70;

    private int cocoonHP = COCOON_MAX_HP;
    private boolean cocooned;
    private boolean cocoonActivated;

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

    @Override
    public void updateBehavior(Player player, Room room) {
        if (isDead() || player == null || cocooned) {
            return;
        }

        long now = System.nanoTime();

        // Cập nhật hiệu ứng vụ nổ
        if (exploding && now - explosionStart >= EXPLOSION_DURATION) {
            exploding = false;
        }

        if (!canDetect(player)) {
            return;
        }

        faceToward(player);

        double bossCenterX = getX() + getWidth() / 2.0;
        double bossCenterY = getY() + getHeight() / 2.0;
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        double dx = playerCenterX - bossCenterX;
        double dy = playerCenterY - bossCenterY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Nổ axit khi Player đến quá gần
        if (distance <= EXPLOSION_TRIGGER_RANGE
                && now - lastExplosion >= EXPLOSION_COOLDOWN) {

            exploding = true;
            explosionStart = now;
            lastExplosion = now;

            if (distance <= EXPLOSION_RADIUS) {
                player.takeDamage(EXPLOSION_DAMAGE);
            }

            return;
        }

        // Giữ khoảng cách và phun axit
        if (distance > EXPLOSION_TRIGGER_RANGE) {

            if (distance > 140) {
                chase(player, room);
            }

            if (now - lastAcidAttack >= ACID_COOLDOWN) {
                acidAttackRequested = true;
                lastAcidAttack = now;
            }
        }
    }

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

        super.takeDamage(damage);

        // Hóa kén duy nhất một lần khi HP còn 30%
        if (!isDead()
                && !cocoonActivated
                && getHp() <= DEFAULT_MAX_HP * COCOON_HP_THRESHOLD) {

            cocooned = true;
            cocoonActivated = true;
        }
    }

    // Các phương thức hỗ trợ GamePanel

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
}
