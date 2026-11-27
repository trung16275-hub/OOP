package model.boss;

import map.Room;
import model.Enemy;
import model.player.Player;

public class Boss1 extends Enemy {
    // Thông số riêng của Boss1
    private static final String DEFAULT_NAME = "Boss 1";
    private static final int DEFAULT_MAX_HP = 300;
    private static final int DEFAULT_SPEED = 2;
    private static final int DEFAULT_BASE_DAMAGE = 1;
    private static final double DEFAULT_DETECTION_RANGE = 250;
    private static final double DEFAULT_HITBOX_RADIUS = 50;
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;

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

    // Hành vi của Boss: dùng hành vi chung rồi thêm kỹ năng riêng
    @Override
    public void updateBehavior(Player player, Room room) {
        super.updateBehavior(player,room);

        if (player != null && player.intersectsHurtBox(this)) {
            specialAttack(player);
        }
    }

    // Kỹ năng đặc biệt: gây sát thương gấp đôi
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