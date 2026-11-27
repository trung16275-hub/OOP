
package model.creep;

import map.Room;
import model.Combatant;
import model.Enemy;
import model.player.Player;
import model.weapon.Ak47;

public class Stalker extends Enemy {

    private static final int MAX_HP = 60;
    private static final int SPEED = 3;
    private static final int DAMAGE = 8;

    private static final double DETECTION_RANGE = 280.0;
    private static final double ATTACK_RANGE = 45.0;

    private static final long ATTACK_COOLDOWN_NANOS = 1_500_000_000L;
    private static final long STUN_DURATION_MS = 300L;

    private static final long ATTACK_ANIMATION_NANOS = 350_000_000L;
    private static final long HURT_ANIMATION_NANOS = 250_000_000L;

    private long nextAttackTime;
    private long attackAnimationEndTime;
    private long hurtAnimationEndTime;

    private boolean aggressive;
    private boolean moving;

    public Stalker() {
        super("Stalker", MAX_HP, SPEED, DAMAGE,
                DETECTION_RANGE, 24);

        setWidth(48);
        setHeight(48);
    }

    public boolean isAggressive() {
        return aggressive;
    }

    /**
     * Khi nhận sát thương, Stalker chuyển sang trạng thái hung hãn.
     * Áp dụng cả khi bị AK47 bắn trúng bằng projectile.
     */
    @Override
    public void takeDamage(int damage) {
        if (damage <= 0 || isDead()) {
            return;
        }

        super.takeDamage(damage);

        if (!isDead()) {
            aggressive = true;
            hurtAnimationEndTime =
                    System.nanoTime() + HURT_ANIMATION_NANOS;
        }
    }

    @Override
    public void updateBehavior(Player player, Room room) {
        if (player == null || player.isDead() || isDead()) {
            moving = false;
            return;
        }

        moving = false;

        double dx = (player.getX() + player.getWidth() / 2.0)
                - (getX() + getWidth() / 2.0);

        double dy = (player.getY() + player.getHeight() / 2.0)
                - (getY() + getHeight() / 2.0);

        double distance = Math.hypot(dx, dy);

        // Chưa bị kích động: chỉ phát hiện Player khi Player cầm AK47.
        boolean playerHoldingAk47 =
                player.getCurrentWeapon() instanceof Ak47;

        if (!aggressive && !playerHoldingAk47) {
            return;
        }

        if (distance > getDetectionRange()) {
            return;
        }

        faceToward(player);

        // Đến đủ gần thì tấn công, không tiếp tục lao vào Player.
        if (distance <= ATTACK_RANGE) {
            tryStalkerAttack(player);
            return;
        }

        // Đuổi theo Player.
        double dirX = dx / distance;
        double dirY = dy / distance;

        double oldX = getX();
        double oldY = getY();

        move(dirX, dirY, room);

        moving = Math.abs(getX() - oldX) > 0.01
                || Math.abs(getY() - oldY) > 0.01;
    }

    private void tryStalkerAttack(Player player) {
        long now = System.nanoTime();

        if (now < nextAttackTime) {
            return;
        }

        attack(player);

        nextAttackTime = now + ATTACK_COOLDOWN_NANOS;
        attackAnimationEndTime = now + ATTACK_ANIMATION_NANOS;
    }

    @Override
    public void attack(Combatant target) {
        if (!(target instanceof Player) || target.isDead()) {
            return;
        }

        Player player = (Player) target;

        player.takeDamage(getBaseDamage());
        player.applyStun(STUN_DURATION_MS);
    }

    /**
     * Khóa animation được GameRenderer sử dụng.
     */
    @Override
    public String getSpritePath() {
        return "stalker";
    }

    @Override
    public String getAnimState() {
        long now = System.nanoTime();

        if (now < hurtAnimationEndTime) {
            return "hurt";
        }

        if (now < attackAnimationEndTime) {
            return "attack";
        }

        if (moving) {
            return "run";
        }

        return "idle";
    }

    @Override
    public int getSpriteFrameCount() {
        return 4;
    }
}
