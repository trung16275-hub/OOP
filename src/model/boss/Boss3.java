package model.boss;

import model.Enemy;

import model.Player;

public class Boss3 extends Enemy {
    // Chỉnh các chỉ số mặc định của Boss 3 tại đây.
    private static final String DEFAULT_NAME = "Boss 3";
    private static final int DEFAULT_MAX_HP = 200;
    private static final int DEFAULT_SPEED = 3;
    private static final int DEFAULT_BASE_DAMAGE = 20;
    private static final double DEFAULT_DETECTION_RANGE = Double.POSITIVE_INFINITY;
    private static final double DEFAULT_HITBOX_RADIUS = 30;
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;

    // Cấu hình kỹ năng hồi máu
    private static final double HEAL_TRIGGER_RATIO = 0.25;           // <= 25% máu
    private static final long HEAL_DURATION_NANOS = 10_000_000_000L; // 10 giây
    private static final long HEAL_TICK_NANOS = 1_000_000_000L;      // Mỗi 1 giây
    private static final int HEAL_PER_SECOND = 10;                   // +10 máu/giây

    private boolean isHealing = false;
    private boolean hasUsedHeal = false; // Chỉ kích hoạt 1 lần
    private long healStartTime = 0L;
    private long lastHealTickTime = 0L;

    public Boss3() {
        this(DEFAULT_HITBOX_RADIUS);
    }

    public Boss3(double hitboxRadius) {
        super(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED, DEFAULT_BASE_DAMAGE,
                DEFAULT_DETECTION_RANGE, hitboxRadius);
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }

    @Override
    public void updateBehavior(Player player) {
        if (isDead()) {
            isHealing = false;
            return;
        }

        long now = System.nanoTime();
    // 1. Kích hoạt hồi máu khi máu tụt xuống <= 25%
    if (!hasUsedHeal && !isHealing && getHp() <= DEFAULT_MAX_HP * HEAL_TRIGGER_RATIO) {
        isHealing = true;
        hasUsedHeal = true;
        healStartTime = now;
        lastHealTickTime = now;
    }

    // 2. Đang hồi máu -> đứng yên, không đuổi theo hay tấn công
    if (isHealing) {
        if (now - lastHealTickTime >= HEAL_TICK_NANOS) {
            heal(HEAL_PER_SECOND);
            lastHealTickTime += HEAL_TICK_NANOS;
        }

        if (now - healStartTime >= HEAL_DURATION_NANOS) {
            isHealing = false;
        }

        return;
    }
        // 3. Hoạt động bình thường (đuổi theo + tấn công tiếp xúc)
        super.updateBehavior(player);
    }

    public boolean isHealing() {
        return isHealing;
    }
}
