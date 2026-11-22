
package model.boss;

import map.Room;
import model.Enemy;
import model.Player;

public class Boss2 extends Enemy {

    // Thong so mac dinh
    private static final String DEFAULT_NAME = "Skeleton King";
    private static final int DEFAULT_MAX_HP = 500;
    private static final int DEFAULT_SPEED = 2;
    private static final int DEFAULT_BASE_DAMAGE = 25;
    private static final double DEFAULT_DETECTION_RANGE = 350;
    private static final double DEFAULT_HITBOX_RADIUS = 50;
    private static final int DEFAULT_WIDTH = 80;
    private static final int DEFAULT_HEIGHT = 80;

    // Thoi gian hoi chieu
    private static final long NORMAL_COOLDOWN = 2_500_000_000L;
    private static final long RAGE_COOLDOWN = 1_500_000_000L;

    private long lastAttackTime = 0;

    // Constructor
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

    // Kiem tra giai doan cuong no
    public boolean isEnraged() {
        return getHp() <= getMaxHp() / 2;
    }

    // Hanh vi cua Boss2
    @Override
    public void updateBehavior(Player player, Room room) {

        if (player == null || player.isDead() || isDead()) {
            return;
        }

        // Duoi theo nguoi choi
        chase(player, room);

        // Tan cong khi nguoi choi o trong hitbox
        if (isWithinHitbox(player)) {
            specialAttack(player);
        }
    }

    // Ky nang tan cong cua Boss2
    public void specialAttack(Player player) {

        if (player == null || player.isDead() || isDead()) {
            return;
        }

        long currentTime = System.nanoTime();

        // Kiem tra giai doan cua Boss
        long cooldown = isEnraged()
                ? RAGE_COOLDOWN
                : NORMAL_COOLDOWN;

        if (currentTime - lastAttackTime >= cooldown) {

            int damage = isEnraged()
                    ? getBaseDamage() * 2
                    : getBaseDamage();

            player.takeDamage(damage);

            lastAttackTime = currentTime;
        }
    }
}
