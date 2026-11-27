
package model.creep;

import map.Room;
import model.Combatant;
import model.Enemy;
import model.Player;

public class AttackCreep extends Enemy {

    private static final int MAX_HP = 30;
    private static final int SPEED = 3;
    private static final int DAMAGE = 5;

    private static final double DETECTION_RANGE = 250.0;
    private static final double ATTACK_RANGE = 40.0;

    private static final long ATTACK_COOLDOWN_NANOS = 1_000_000_000L;
    private static final long ATTACK_ANIMATION_NANOS = 350_000_000L;
    private static final long HURT_ANIMATION_NANOS = 250_000_000L;
    private static final long DEATH_ANIMATION_NANOS = 600_000_000L;

    private long nextAttackTime = 0L;
    private long attackAnimationEndTime = 0L;
    private long hurtAnimationEndTime = 0L;
    private long deathAnimationEndTime = 0L;

    private boolean moving = false;

    public AttackCreep() {
        super("AttackCreep", MAX_HP, SPEED, DAMAGE, DETECTION_RANGE, 18);
        setWidth(48);
        setHeight(48);
    }

    @Override
    public void updateBehavior(Player player, Room room) {
        if (player == null || player.isDead() || isDead() || room == null) {
            moving = false;
            return;
        }

        long now = System.nanoTime();

        if (now < hurtAnimationEndTime) {
            moving = false;
            return;
        }

        double dx = (player.getX() + player.getWidth() / 2.0)
                - (getX() + getWidth() / 2.0);
        double dy = (player.getY() + player.getHeight() / 2.0)
                - (getY() + getHeight() / 2.0);

        double distance = Math.hypot(dx, dy);

        if (distance > DETECTION_RANGE) {
            moving = false;
            return;
        }

        faceToward(player);

        if (distance <= ATTACK_RANGE) {
            moving = false;
            tryBite(player, now);
            return;
        }

        if (distance > 0) {
            double dirX = dx / distance;
            double dirY = dy / distance;

            double oldX = getX();
            double oldY = getY();

            move(dirX, dirY, room);

            moving = Math.abs(getX() - oldX) > 0.01
                    || Math.abs(getY() - oldY) > 0.01;
        } else {
            moving = false;
        }
    }

    private void tryBite(Player player, long now) {
        if (now < nextAttackTime) {
            return;
        }

        attack(player);

        nextAttackTime = now + ATTACK_COOLDOWN_NANOS;
        attackAnimationEndTime = now + ATTACK_ANIMATION_NANOS;
    }

    @Override
    public void attack(Combatant target) {
        if (!(target instanceof Player)) {
            return;
        }

        Player player = (Player) target;

        if (player.isDead() || isDead()) {
            return;
        }

        double dx = (player.getX() + player.getWidth() / 2.0)
                - (getX() + getWidth() / 2.0);
        double dy = (player.getY() + player.getHeight() / 2.0)
                - (getY() + getHeight() / 2.0);

        if (Math.hypot(dx, dy) <= ATTACK_RANGE) {
            player.takeDamage(DAMAGE);
        }
    }

   @Override
public void takeDamage(int damage) {
    if (damage <= 0 || isDead()) {
        return;
    }

    super.takeDamage(damage);

    long now = System.nanoTime();

    if (isDead()) {
        deathAnimationEndTime = now + DEATH_ANIMATION_NANOS;

        moving = false;
        attackAnimationEndTime = 0L;
        hurtAnimationEndTime = 0L;
    } else {
        hurtAnimationEndTime = now + HURT_ANIMATION_NANOS;
    }
}

    @Override
    public String getSpritePath() {
        return "attack_creep";
    }

    @Override
    public String getAnimState() {
        if (isDead()) {
            return "death";
        }

        long now = System.nanoTime();

        if (now < hurtAnimationEndTime) {
            return "hurt";
        }

        if (now < attackAnimationEndTime) {
            return "attack";
        }

        return moving ? "run" : "idle";
    }

    @Override
    public int getSpriteFrameCount() {
        return 4;
    }
    // Kiểm tra hoạt ảnh chết còn đang phát hay không
    public boolean isPlayingDeathAnimation() {
    return isDead()
            && deathAnimationEndTime > 0L
            && System.nanoTime() < deathAnimationEndTime;
    }
}
