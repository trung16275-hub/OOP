package model.boss;

import model.Enemy;
import model.Combatant;
import model.Player;
import map.Room;
public class Boss3 extends Enemy {
    // Chỉnh các chỉ số mặc định của Boss 3 tại đây.
    private static final String DEFAULT_NAME = "ChatGPT";
    private static final int DEFAULT_MAX_HP = 1000;
    private static final int DEFAULT_SPEED = 3;
    private static final int DEFAULT_BASE_DAMAGE = 20;
    private static final double DEFAULT_DETECTION_RANGE = 275;
    private static final double DEFAULT_HITBOX_RADIUS = 50;
    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 200;
    private static final long JUMP_ANIM_DURATION_NANOS = 700_000_000L; // 0.7 giây (khớp 7 frame x 100ms)
    private long jumpAnimEndTime = 0L;
    // Cấu hình kỹ năng hồi máu
    private static final double HEAL_TRIGGER_RATIO = 0.25;           // <= 25% máu
    private static final long HEAL_DURATION_NANOS = 10_000_000_000L; // 10 giây
    private static final long HEAL_TICK_NANOS = 1_000_000_000L;      // Mỗi 1 giây
    private static final int HEAL_PER_SECOND = 10;                   // +10 máu/giây

    private boolean isHealing = false;
    private boolean hasUsedHeal = false; // Chỉ kích hoạt 1 lần
    private long healStartTime = 0L;
    private long lastHealTickTime = 0L;
        // Cấu hình kỹ năng 4 tia laze xoay 90 độ
    private static final int LASER_COUNT = 4;
    private static final double LASER_ROTATIONS_PER_SECOND = 0.25; // 4 vòng/giây (giảm xuống 0.25 nếu muốn xoay 90 độ trong 1s)
    private static final double LASER_SWEEP_RAD = Math.PI / 2.0;  // Xoay 90 độ rồi kết thúc
    private static final double LASER_LENGTH = 300.0;             // Chiều dài mỗi tia (pixel)
    private static final int LASER_DAMAGE = 25;                   // Sát thương tia laze
    private static final long LASER_COOLDOWN_NANOS = 8_000_000_000L; // Hồi chiêu 8 giây

    private boolean isFiringLaser = false;
    private boolean hasHitPlayerWithLaser = false; // Tránh trừ máu liên tục mỗi frame
    private double laserBaseAngle = 0.0;           // Góc hiện tại của tia đầu tiên (radian)
    private double laserSweptAngle = 0.0;          // Tổng góc đã xoay được
    private long lastLaserUpdateTime = 0L;
    private long lastLaserEndTime = 0L;
    public Boss3() {
        this(DEFAULT_HITBOX_RADIUS);
    }

    public Boss3(double hitboxRadius) {
        super(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED, DEFAULT_BASE_DAMAGE,
                DEFAULT_DETECTION_RANGE, hitboxRadius);
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }
        
<<<<<<< HEAD

=======
>>>>>>> 45849b59d5baf0c5d133f97e632b41c71e4e5fbf
@Override 
public void attack(Combatant target) {
    if (isDead() || target == null || target.isDead()) {
        return;
    }
    // Tấn công tiếp xúc
    if (!isFiringLaser && !isHealing) {
        super.attack(target);
        jumpAnimEndTime = System.nanoTime() + JUMP_ANIM_DURATION_NANOS;
    }
}
@Override
public String getSpritePath() {
    if (isFiringLaser) return "boss3/super-cool-robot-fire.png";
    if (isHealing)     return "boss3/super-cool-robot-idle.png";
    if (System.nanoTime() < jumpAnimEndTime) return "boss3/super-cool-robot-jump.png";
    return "boss3/super-cool-robot-run.png";
}

@Override
public int getSpriteFrameCount() {
    if (isFiringLaser) return 4;
    if (isHealing)     return 9;
    if (System.nanoTime() < jumpAnimEndTime) return 7;
    return 9;
}

@Override
public String getSpritePath() {
    return "boss3";
}

@Override
public int getSpriteFrameCount() {
    return 9;
}

    @Override
    public void updateBehavior(Player player,Room room) {
        if (isDead()) {
            isHealing = false;
            isFiringLaser = false;
            return;
        }

        long now = System.nanoTime();
    // 1. Kích hoạt hồi máu khi máu tụt xuống <= 25%
    if (!hasUsedHeal && !isHealing && getHp() <= DEFAULT_MAX_HP * HEAL_TRIGGER_RATIO) {
        isHealing = true;
        isFiringLaser = false; // Ngắt laze nếu đang bắn
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
        // --- 2. KỸ NĂNG 4 TIA LAZE XOAY 90 ĐỘ ---
        if (!isFiringLaser && player != null && !player.isDead()
                && (lastLaserEndTime == 0L || now - lastLaserEndTime >= LASER_COOLDOWN_NANOS)) {
            startLaserSkill(now);
        }

        if (isFiringLaser) {
            updateLaserSkill(player, now);
            return; // Đứng yên khi xoay laze (xóa dòng này nếu muốn Boss vừa đi vừa bắn)
        }

        
        // 3. Hoạt động bình thường (đuổi theo + tấn công tiếp xúc)
        super.updateBehavior(player,room);
    }

    public boolean isHealing() {
        return isHealing;
    }
    private void startLaserSkill(long now) {
    isFiringLaser = true;
    hasHitPlayerWithLaser = false;
    laserBaseAngle = 0.0; // Bắt đầu từ hướng ngang/dọc (hình dấu +)
    laserSweptAngle = 0.0;
    lastLaserUpdateTime = now;
    }

    private void updateLaserSkill(Player player, long now) {
        double deltaSeconds = (now - lastLaserUpdateTime) / 1_000_000_000.0;
        lastLaserUpdateTime = now;

        // Tính góc xoay trong frame này: 4 vòng/giây = 4 * 2 * PI rad/giây
        double angularSpeed = LASER_ROTATIONS_PER_SECOND * 2.0 * Math.PI;
        double deltaAngle = angularSpeed * deltaSeconds;

        // Đảm bảo không xoay quá 90 độ
        if (laserSweptAngle + deltaAngle >= LASER_SWEEP_RAD) {
            deltaAngle = LASER_SWEEP_RAD - laserSweptAngle;
            laserBaseAngle += deltaAngle;
            laserSweptAngle = LASER_SWEEP_RAD;

            checkLaserCollision(player);
            isFiringLaser = false;
            lastLaserEndTime = now;
            return;
        }

        laserBaseAngle += deltaAngle;
        laserSweptAngle += deltaAngle;
        checkLaserCollision(player);
    }

    private void checkLaserCollision(Player player) {
        if (hasHitPlayerWithLaser || player == null || player.isDead()) {
            return;
        }

        double bossCenterX = getX() + getWidth() / 2.0;
        double bossCenterY = getY() + getHeight() / 2.0;
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;
        double playerRadius = Math.max(player.getWidth(), player.getHeight()) / 2.0;

        // Kiểm tra từng tia trong 4 tia (cách nhau 90 độ = PI/2)
        for (int i = 0; i < LASER_COUNT; i++) {
            double angle = laserBaseAngle + i * (Math.PI / 2.0);
            double endX = bossCenterX + Math.cos(angle) * LASER_LENGTH;
            double endY = bossCenterY + Math.sin(angle) * LASER_LENGTH;

            if (distancePointToSegment(playerCenterX, playerCenterY, bossCenterX, bossCenterY, endX, endY) <= playerRadius) {
                player.takeDamage(LASER_DAMAGE);
                hasHitPlayerWithLaser = true;
                break;
            }
        }
    }

    // Tính khoảng cách ngắn nhất từ tâm Player đến đoạn thẳng tia laze
    private double distancePointToSegment(double px, double py, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        if (lenSq == 0) {
            return Math.hypot(px - x1, py - y1);
        }
        double t = ((px - x1) * dx + (py - y1) * dy) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));
        double projX = x1 + t * dx;
        double projY = y1 + t * dy;
        return Math.hypot(px - projX, py - projY);
    }

    // Getter để lớp Renderer/View vẽ 4 tia laze lên màn hình
    public boolean isFiringLaser() { return isFiringLaser; }
    public double getLaserBaseAngle() { return laserBaseAngle; }
    public double getLaserLength() { return LASER_LENGTH; }
    public int getLaserCount() { return LASER_COUNT; }
    }
