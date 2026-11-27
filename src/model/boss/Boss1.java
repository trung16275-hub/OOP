package model.boss;

import map.Room;
import model.Enemy;
import model.player.Player;

public class Boss1 extends Enemy {

    private static final String DEFAULT_NAME = "Boss 1";
    private static final int DEFAULT_MAX_HP = 300;
    private static final int DEFAULT_SPEED = 1;
    private static final int DEFAULT_BASE_DAMAGE = 30;
    private static final double DEFAULT_DETECTION_RANGE = 300;
    private static final double DEFAULT_HITBOX_RADIUS = 30;
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;

    // Skill 1: Lao tới
    private static final double DASH_MIN_RANGE = 150;
    private static final double DASH_MAX_RANGE = 250;
    private static final long DASH_DURATION = 350_000_000L;
    private static final long DASH_COOLDOWN = 5_000_000_000L;
    private static final double DASH_SPEED_MULTIPLIER = 5.5;
    private static final int DASH_DAMAGE = 10;

    // Skill 2: Vồ áp sát
    private static final long LUNGE_DURATION = 400_000_000L;
    private static final long LUNGE_COOLDOWN = 2_500_000_000L;
    private static final double LUNGE_SPEED_MULTIPLIER = 3.0;
    private static final int LUNGE_DAMAGE = 20;
    private static final double LUNGE_TRIGGER_DISTANCE = 100;

    private enum BossState {
        IDLE,
        CHASE,
        DASH_STRIKE,
        LUNGE_BITE,
        HURT,
        DEATH
    }

    private BossState state = BossState.IDLE;

    private long stateStartTime;
    private static final long DEATH_ANIMATION_DURATION_MS = 1100;
    private long lastDashTime;
    private long lastLungeTime;

    private boolean hasUsedDash;
    private boolean hasUsedLunge;

    private double lockedDirX;
    private double lockedDirY;

    private boolean dashHit;
    private boolean lungeHit;

private static final long HURT_DURATION = 320_000_000L;

    private long hurtStartTime;
    

    public Boss1() {
        this(DEFAULT_HITBOX_RADIUS);
    }

    public Boss1(double hitboxRadius) {
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
protected void onDamaged() {
    if (isDead()) {
        state = BossState.DEATH;
        stateStartTime = System.nanoTime();
        return;
    }

    state = BossState.HURT;
    hurtStartTime = System.nanoTime();
    stateStartTime = hurtStartTime;
}


  
  
@Override
public void updateBehavior(Player player, Room room) {

    
long now = System.nanoTime();

// Boss đã chết: không cập nhật AI nữa
if (isDead()) {
    if (state != BossState.DEATH) {
        state = BossState.DEATH;
        stateStartTime = now;
    }
    return;
}

// Boss đang bị thương
if (state == BossState.HURT) {
    if (now - hurtStartTime < HURT_DURATION) {
        return;
    }

    // Hết animation Hurt, quay lại AI
    if (player == null || player.isDead()) {
        state = BossState.IDLE;
    } else if (canDetect(player)) {
        state = BossState.CHASE;
    } else {
        state = BossState.IDLE;
    }
}


    // 3. Player không hợp lệ
    if (player == null || player.isDead()) {
        state = BossState.IDLE;
        return;
    }

    double distance = distanceTo(player);

    // 4. Ưu tiên hoàn thành skill đang chạy
    if (state == BossState.DASH_STRIKE) {
        updateDash(player, room, now);
        return;
    }

    if (state == BossState.LUNGE_BITE) {
        updateLunge(player, room, now);
        return;
    }

    // 5. Boss không phát hiện Player
    if (!canDetect(player)) {
        state = BossState.IDLE;
        return;
    }

    // 6. Chỉ quay hướng Player khi không thực hiện skill
    faceToward(player);

    // 7. Skill 2: Vồ áp sát
    if (distance <= LUNGE_TRIGGER_DISTANCE
            && canUseLunge(now)) {

        startLunge(player, now);
        updateLunge(player, room, now);
        return;
    }

    // 8. Skill 1: Lao tới
    if (distance >= DASH_MIN_RANGE
            && distance <= DASH_MAX_RANGE
            && canUseDash(now)) {

        startDash(player, now);
        updateDash(player, room, now);
        return;
    }

    // 9. Truy đuổi thông thường
    state = BossState.CHASE;
    chase(player, room);
}


    // =========================
    // SKILL 1: LAO TỚI
    // =========================

    private boolean canUseDash(long now) {
        return !hasUsedDash
                || now - lastDashTime >= DASH_COOLDOWN;
    }

    private void startDash(Player player, long now) {

        faceToward(player);

        lockedDirX = getFacingX();
        lockedDirY = getFacingY();

        state = BossState.DASH_STRIKE;
        stateStartTime = now;
        lastDashTime = now;
        hasUsedDash = true;

        dashHit = false;
    }

    private void updateDash(Player player, Room room, long now) {

        if (now - stateStartTime >= DASH_DURATION) {
            state = BossState.CHASE;
            return;
        }

        // Lao theo hướng đã khóa
        move(
                lockedDirX * DASH_SPEED_MULTIPLIER,
                lockedDirY * DASH_SPEED_MULTIPLIER,
                room
        );

        // Chỉ gây sát thương một lần
        if (!dashHit && isWithinHitbox(player)) {
            player.takeDamage(DASH_DAMAGE);
            dashHit = true;
        }
    }

    // =========================
    // SKILL 2: VỒ ÁP SÁT
    // =========================

    private boolean canUseLunge(long now) {
        return !hasUsedLunge
                || now - lastLungeTime >= LUNGE_COOLDOWN;
    }

    private void startLunge(Player player, long now) {

        faceToward(player);

        lockedDirX = getFacingX();
        lockedDirY = getFacingY();

        state = BossState.LUNGE_BITE;
        stateStartTime = now;
        lastLungeTime = now;
        hasUsedLunge = true;

        lungeHit = false;
    }

    private void updateLunge(Player player, Room room, long now) {

        if (now - stateStartTime >= LUNGE_DURATION) {
            state = BossState.CHASE;
            return;
        }

        // Vồ theo hướng đã khóa
        move(
                lockedDirX * LUNGE_SPEED_MULTIPLIER,
                lockedDirY * LUNGE_SPEED_MULTIPLIER,
                room
        );

        // Chỉ gây sát thương một lần
        if (!lungeHit && isWithinHitbox(player)) {
            player.takeDamage(LUNGE_DAMAGE);
            lungeHit = true;
        }
    }

    // =========================
    // ANIMATION & LIFECYCLE
    // =========================

    private static final int LOOP_FRAME_DURATION_MS = 130;
    private static final int DASH_FRAME_DURATION_MS = 38;
    private static final int LUNGE_FRAME_DURATION_MS = 44;
    private static final int HURT_FRAME_DURATION_MS = 80;
    private static final int DEATH_FRAME_DURATION_MS = 220;

    @Override
    public String getSpriteKey() {
        return "boss1";
    }

    @Override
    public String getAnimState() {
        return switch (state) {
            case DASH_STRIKE -> "dash";
            case LUNGE_BITE -> "lunge";
            case CHASE -> "run";
            case HURT -> "hurt";
            case DEATH -> "death";
            case IDLE -> "idle";
        };
    }

    @Override
    public int getAnimFrameDurationMs(String animState) {
        return switch (animState) {
            case "dash" -> DASH_FRAME_DURATION_MS;
            case "lunge" -> LUNGE_FRAME_DURATION_MS;
            case "hurt" -> HURT_FRAME_DURATION_MS;
            case "death" -> DEATH_FRAME_DURATION_MS;
            default -> LOOP_FRAME_DURATION_MS;
        };
    }

    @Override
    public int resolveAnimFrameIndex(String animState, int frameCount) {
        if (frameCount <= 1) {
            return 0;
        }
        if ("idle".equals(animState) || "run".equals(animState)) {
            return super.resolveAnimFrameIndex(animState, frameCount);
        }
        int frameDuration = getAnimFrameDurationMs(animState);
        return (int) Math.min(getAnimationElapsedMillis() / frameDuration, frameCount - 1);
    }

    @Override
    public boolean isRemovable() {
        return isDead() && !isPlayingDeathAnimation();
    }

    public boolean isDashing() {
        return state == BossState.DASH_STRIKE;
    }

    public long getAnimationElapsedMillis() {
        return (System.nanoTime() - stateStartTime) / 1_000_000L;
    }

    public boolean isPlayingDeathAnimation() {
        return state == BossState.DEATH && getAnimationElapsedMillis() < DEATH_ANIMATION_DURATION_MS;
    }

    public boolean isLunging() {
        return state == BossState.LUNGE_BITE;
    }
}
