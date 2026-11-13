
package model;

public class Boss1 extends Enemy {

    // Thông số riêng của Boss1
    private static final int MAX_HP = 300;
    private static final int SPEED = 2;
    private static final int DAMAGE = 20;
    private static final double DETECTION_RANGE = 250;

    // Lưu Player mà Boss đang truy đuổi
    private Player target;

    // Thời gian hồi chiêu kỹ năng
    private static final long SKILL_COOLDOWN = 3_000_000_000L;
    private long lastSkillTime = 0;

    // Constructor
    public Boss1() {
        super("Boss 1", MAX_HP, SPEED, DAMAGE, DETECTION_RANGE);
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
