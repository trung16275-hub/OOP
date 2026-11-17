
package model.boss;

import model.Enemy;
import model.Player;

public class Boss1 extends Enemy {
    // Thông số riêng của Boss1
    private static final String DEFAULT_NAME = "Boss 1";
    private static final int DEFAULT_MAX_HP = 300;
    private static final int DEFAULT_SPEED = 2;
    private static final int DEFAULT_BASE_DAMAGE = 20;
    private static final double DEFAULT_DETECTION_RANGE = 250;
    private static final double DEFAULT_HITBOX_RADIUS = 50;
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;

    // Lưu Player mà Boss đang truy đuổi
    private Player target;

    // Thời gian hồi chiêu kỹ năng
    private static final long SKILL_COOLDOWN = 3_000_000_000L;
    private long lastSkillTime = 0;

    // Constructor
    public Boss1() {
        this(DEFAULT_HITBOX_RADIUS);
    }

    public Boss1(double hitboxRadius) {
        super(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED, DEFAULT_BASE_DAMAGE,DEFAULT_DETECTION_RANGE, hitboxRadius);
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }

    // Gán mục tiêu cho Boss
    public void setTarget(Player target) {
        this.target = target;
    }

    // Lấy mục tiêu hiện tại
    public Player getTarget() {
        return target;
    }

    // Hành vi của Boss
    @Override
    public void updateBehavior() {
        if (target == null || target.isDead() || isDead()) {
            return;
        }

        // Đuổi theo Player nếu phát hiện
        if (canDetect(target)) {
            chase(target);

            // Tấn công khi tiếp xúc
            tryContactAttack(target);
        }
    }

    // Cho phép cập nhật mục tiêu trực tiếp
    public void updateBehavior(Player player) {
        setTarget(player);
        updateBehavior();
    }

    // Kỹ năng đặc biệt: Gây sát thương gấp đôi
    public void specialAttack(Player player) {
        if (player == null || player.isDead() || isDead()) {
            return;
        }

        long currentTime = System.nanoTime();

        if (currentTime - lastSkillTime >= SKILL_COOLDOWN) {
            player.takeDamage(getBaseDamage() * 2);
            lastSkillTime = currentTime;
        }
    }
}
