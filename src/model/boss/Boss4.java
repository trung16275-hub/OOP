package model.boss;

import model.Combatant;
import model.Enemy;
import model.player.Player;
import map.Room;
import java.awt.Rectangle;
import map.TileType;
public class Boss4 extends Enemy {

    public enum State {
        SHADOW_AMBUSH,
        WALL_SNATCH,
        PANIC_SPRINT,
        IN_WALL,
        REPELLED,
        DEATH
    }
// Các pha của chiêu 1: Wall Snatch
    private enum SnatchPhase {
        EXTENDING,  // Thò tay xuyên tường vươn tới Player
        GRABBED,    // Đã chộp được và đang kéo về tường
        RETRACTING  // Thu tay lại
    }
    private static final int DEATH_FRAME_COUNT = 4;
private static final long DEATH_FRAME_DURATION_NANOS = 160_000_000L;
private static final long DEATH_CORPSE_HOLD_NANOS = 350_000_000L;
private long deathStartTime = 0L;
    private boolean submergedInWall = false;
private long sprintStartTime = 0L;
private long wallEnterTime = 0L;
private long wallHideEndTime = 0L;
private static final long MIN_SPRINT_BEFORE_DIVE_NANOS = 550_000_000L; // Chạy lộ diện ít nhất 0.55s rồi mới lặn
private static final long MIN_WALL_HIDE_BEFORE_SNATCH_NANOS = 900_000_000L; // Trượt ngầm ít nhất 0.9s để Player kịp thấy dấu vết
private static final long WALL_HIDE_DURATION_NANOS = 3_000_000_000L; // Tối đa 3 giây trong tường
private double wallPerimeterPos = 0.0;
    private State state = State.SHADOW_AMBUSH;
    private SnatchPhase snatchPhase = SnatchPhase.EXTENDING;

    // --- Chiêu 1: Wall Snatch ---
    private double armStartX, armStartY; // Điểm cánh tay nhô ra từ tường
    private double armEndX, armEndY;     // Vị trí bàn tay hiện tại
    private double targetX, targetY;     // Điểm nhắm tới lúc vươn tay
    private double armProgress = 0.0;    // 0.0 -> 1.0
    private boolean isArmActive = false;
    private boolean isGrabbingPlayer = false;
    private int struggleProgress = 0;
    private static final int STRUGGLE_TO_ESCAPE = 6;
    private static final int SNATCH_HEAVY_DAMAGE = 45;

    // --- Chiêu 2: Panic Sprint ---
    private static final double SPRINT_MULTIPLIER = 2.4;
    private static final long SPRINT_DURATION_NANOS = 3_000_000_000L; // 3 giây
    private long sprintEndTime = 0L;
    private int turnSign = 1; // Hướng bẻ góc (1: rẽ trái, -1: rẽ phải)

    // --- Chiêu 3: Shadow Ambush ---
    private static final double AMBUSH_DETECT_RANGE = 240.0;
    private static final double AMBUSH_LUNGE_RANGE = 95.0;
    private double ambushCharge = 0.0;

    // --- Chiêu 4: Almond Water Repulsion ---
    private double safeZoneX, safeZoneY, safeZoneRadius;
    private long safeZoneEndTime = 0L;
    public Boss4(double x, double y) {
    // 1. Gọi đúng constructor của Enemy: (name, maxHp, speed, baseDamage, detectionRange)
    super("Duller", 300, 3, 25, 450.0,28.0);

    // 2. Gán vị trí & kích thước (đổi sang setPosition(x, y) nếu Combatant dùng hàm đó)
    setX(x);
    setY(y);
    setWidth(60);
    setHeight(90);
}

    public void updateBoss4(Player player, Room room, long now) {
    if (player == null) return;

    if (state == State.DEATH) {
        releasePlayer(player);
        return;
    }

    // Nước Hạnh Nhân có thể ép Boss 4 bật ra khỏi tường nếu dùng sát chỗ nó đang trốn
    if (now < safeZoneEndTime && (isGrabbingPlayer || isInsideSafeZone(getCenterX(), getCenterY()))) {
        releasePlayer(player);
        submergedInWall = false;
        isArmActive = false;
        state = State.REPELLED;
    }
    

    switch (state) {
        case REPELLED:
            handleRepelled(player, room, now);
            break;
        case PANIC_SPRINT:
            handlePanicSprint(player, room, now);
            break;
        case IN_WALL:
            handleInWall(player, room, now);
            break;
        case WALL_SNATCH:
            handleWallSnatch(player, room, now);
            break;
        case SHADOW_AMBUSH:
            handleShadowAmbush(player, room, now);
            break;
    }
}
    

    // =========================================================
    // CHIÊU 1: WALL SNATCH (Thò tay xuyên tường tóm & kéo)
    // =========================================================
    
    /**
     * @param player
     * @param room
     * @param now
     */
    private void handleWallSnatch(Player player, Room room, long now) {
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;

        switch (snatchPhase) {
            case EXTENDING:
                armProgress += 0.06; // Tốc độ phóng tay
                if (armProgress > 1.0) armProgress = 1.0;

                armEndX = armStartX + (targetX - armStartX) * armProgress;
                armEndY = armStartY + (targetY - armStartY) * armProgress;

                // Kiểm tra bàn tay có chộp trúng Player không (trong vùng an toàn thì miễn nhiễm)
                double distToHand = Math.hypot(playerCenterX - armEndX, playerCenterY - armEndY);
                if (distToHand <= 32.0 && !(now < safeZoneEndTime && isInsideSafeZone(playerCenterX, playerCenterY))) {
                    isGrabbingPlayer = true;
                    struggleProgress = 0;
                    player.setGrabbed(true); // Khóa di chuyển của Player
                    snatchPhase = SnatchPhase.GRABBED;
                } else if (armProgress >= 1.0) {
                    // Chộp hụt -> thu tay về
                    snatchPhase = SnatchPhase.RETRACTING;
                }
                break;

            case GRABBED:
                // Kéo Player dần về phía bức tường (armStartX, armStartY)
                double dx = armStartX - playerCenterX;
                double dy = armStartY - playerCenterY;
                double dist = Math.hypot(dx, dy);

                if (dist > 25.0) {
                    double pullSpeed = 2.8;
                    player.setX(player.getX() + (dx / dist) * pullSpeed);
                    player.setY(player.getY() + (dy / dist) * pullSpeed);
                    armEndX = player.getX() + player.getWidth() / 2.0;
                    armEndY = player.getY() + player.getHeight() / 2.0;
                } else {
                    // Kéo hết quãng đường -> gây sát thương lớn và thả ra
                    player.takeDamage(SNATCH_HEAVY_DAMAGE);
                    releasePlayer(player);
                    snatchPhase = SnatchPhase.RETRACTING;
                }
                break;

            case RETRACTING:
                armProgress -= 0.08;
                if (armProgress <= 0.0) {
                    submergedInWall = false;
                    armProgress = 0.0;
                    isArmActive = false;
                    // Nếu Player đứng quá gần sau khi thu tay thì bỏ chạy
                    if (Math.hypot(playerCenterX - getCenterX(), playerCenterY - getCenterY()) < 130.0) {
                        triggerPanicSprint(player, now);
                    } else {
                        state = State.SHADOW_AMBUSH;
                    }
                } else {
                    armEndX = armStartX + (armEndX - armStartX) * armProgress;
                    armEndY = armStartY + (armEndY - armStartY) * armProgress;
                }
                break;
        }
    }

    // =========================================================
    // CHIÊU 2: PANIC SPRINT (Bỏ chạy tốc độ cao & bẻ góc)
    // =========================================================
    private void handlePanicSprint(Player player, Room room, long now) {
    if (now >= sprintEndTime) {
        if (isNearWall(room)) {
            enterWall(room, now);
        } else {
            ambushCharge = 0.0;
            state = State.SHADOW_AMBUSH;
        }
        return;
    }

    double awayX = getCenterX() - (player.getX() + player.getWidth() / 2.0);
    double awayY = getCenterY() - (player.getY() + player.getHeight() / 2.0);
    double dist = Math.hypot(awayX, awayY);
    if (dist == 0) {
        awayX = 1;
        dist = 1;
    }

    double dirX = awayX / dist;
    double dirY = awayY / dist;
    double sprintSpeed = Math.max(1.0, getSpeed()) * SPRINT_MULTIPLIER;

    boolean moved = tryMove(dirX * sprintSpeed, dirY * sprintSpeed, room);
    if (!moved) {
        // Đã chạy đủ lâu rồi đụng tường -> lặn thẳng vào trong tường
        if (now - sprintStartTime >= MIN_SPRINT_BEFORE_DIVE_NANOS) {
            enterWall(room, now);
            return;
        }

        // Chưa đủ thời gian chạy -> bẻ góc 90 độ trượt dọc tường
        double perpX = -dirY * turnSign;
        double perpY = dirX * turnSign;
        if (!tryMove(perpX * sprintSpeed, perpY * sprintSpeed, room)) {
            turnSign = -turnSign;
            if (!tryMove(-perpX * sprintSpeed, -perpY * sprintSpeed, room)) {
                // Bị dồn kẹt góc hoàn toàn -> lặn tường thoát thân ngay
                enterWall(room, now);
            }
        }
    }
}
private void enterWall(Room room, long now) {
    submergedInWall = true;
    isArmActive = false;
    state = State.IN_WALL;
    wallEnterTime = now;
    wallHideEndTime = now + WALL_HIDE_DURATION_NANOS;

    double[] bounds = getWalkableBounds(room);
    wallPerimeterPos = posToPerimeter(getX(), getY(), bounds);
    double[] snapped = perimeterToPos(wallPerimeterPos, bounds);
    setX(snapped[0]);
    setY(snapped[1]);
}
private void handleInWall(Player player, Room room, long now) {
    double[] bounds = getWalkableBounds(room);
    double boxW = bounds[1] - bounds[0];
    double boxH = bounds[3] - bounds[2];
    double perimeter = 2.0 * (boxW + boxH);
    if (perimeter <= 0) {
        submergedInWall = false;
        state = State.SHADOW_AMBUSH;
        return;
    }

    // Tìm điểm trên mép tường gần Player nhất để trượt ngầm tới đó
    double playerTargetX = player.getX();
    double playerTargetY = player.getY();
    double targetP = posToPerimeter(playerTargetX, playerTargetY, bounds);

    // Tính đường trượt ngắn nhất dọc theo chu vi 4 bức tường
    double diff = targetP - wallPerimeterPos;
    if (diff > perimeter / 2.0) diff -= perimeter;
    if (diff < -perimeter / 2.0) diff += perimeter;

    double slideSpeed = Math.max(1.0, getSpeed()) * 1.8;
    double step = Math.signum(diff) * Math.min(Math.abs(diff), slideSpeed);
    wallPerimeterPos = ((wallPerimeterPos + step) % perimeter + perimeter) % perimeter;

    double[] pos = perimeterToPos(wallPerimeterPos, bounds);
    setX(pos[0]);
    setY(pos[1]);

    double playerCenterX = player.getX() + player.getWidth() / 2.0;
    double playerCenterY = player.getY() + player.getHeight() / 2.0;
    double distToPlayer = Math.hypot(playerCenterX - getCenterX(), playerCenterY - getCenterY());

    // Đủ thời gian trượt ngầm & Player đứng gần tường -> thò tay Wall Snatch ngay từ trong tường
    boolean canStrike = (now - wallEnterTime) >= MIN_WALL_HIDE_BEFORE_SNATCH_NANOS;
    boolean playerProtected = (now < safeZoneEndTime) && isInsideSafeZone(playerCenterX, playerCenterY);

    if (canStrike && Math.abs(diff) <= 30.0 && distToPlayer <= 190.0 && !playerProtected) {
        startWallSnatch(player, room);
        return;
    }

    // Hết thời gian trốn (Player né tốt ở giữa phòng) -> chui ra khỏi tường
    if (now >= wallHideEndTime) {
        submergedInWall = false;
        ambushCharge = 0.0;
        state = State.SHADOW_AMBUSH;
    }
}
    // =========================================================
    // CHIÊU 3: SHADOW AMBUSH (Rình sát tường, bị phát hiện thì chạy)
    // =========================================================
    private void handleShadowAmbush(Player player, Room room, long now) {
    double playerCenterX = player.getX() + player.getWidth() / 2.0;
    double playerCenterY = player.getY() + player.getHeight() / 2.0;
    double toPlayerX = playerCenterX - getCenterX();
    double toPlayerY = playerCenterY - getCenterY();
    double dist = Math.hypot(toPlayerX, toPlayerY);

    if (!isNearWall(room)) {
        ambushCharge = 0.0;
        isArmActive = false;
        moveTowardNearestWall(room);
        return;
    }

    if (dist > AMBUSH_DETECT_RANGE) {
        ambushCharge = 0.0;
        isArmActive = false;
        return;
    }

    // Tình huống 1: Player ở phía bên kia bức tường -> thò tay xuyên tường (Chiêu 1)
    if (hasWallBetween(getCenterX(), getCenterY(), playerCenterX, playerCenterY, room)) {
        if (dist <= 180.0) {
            startWallSnatch(player, room);
        }
        return;
    }

    // Tình huống 2: Cùng một không gian tối -> phục kích trực diện (Chiêu 3)
    faceToward(player);
    ambushCharge = Math.min(1.0, ambushCharge + 0.025);
    isArmActive = true;
    armStartX = getCenterX();
    armStartY = getCenterY();
    armEndX = armStartX + (toPlayerX / dist) * (65.0 * ambushCharge);
    armEndY = armStartY + (toPlayerY / dist) * (65.0 * ambushCharge);

    double toBossX = -toPlayerX / dist;
    double toBossY = -toPlayerY / dist;
    double lookDot = player.getFacingX() * toBossX + player.getFacingY() * toBossY;

    if (lookDot > 0.75 && dist < 170.0) {
        isArmActive = false;
        ambushCharge = 0.0;
        triggerPanicSprint(player, now);
        return;
    }

    if (dist <= AMBUSH_LUNGE_RANGE || ambushCharge >= 1.0) {
        startWallSnatch(player, room);
    }
}

    // =========================================================
    // CHIÊU 4: ALMOND WATER REPULSION (Co rúm & lùi khỏi vùng an toàn)
    // =========================================================
    private void handleRepelled(Player player, Room room, long now) {
        double awayX = getCenterX() - safeZoneX;
        double awayY = getCenterY() - safeZoneY;
        double dist = Math.hypot(awayX, awayY);
        if (dist == 0) {
            awayX = 1;
            dist = 1;
        }

        // Hết thời gian nước hạnh nhân hoặc đã lùi ra xa an toàn -> quay lại phục kích
        if (now >= safeZoneEndTime || dist > safeZoneRadius + 45.0) {
            state = State.SHADOW_AMBUSH;
            return;
        }

        // Lùi lại trong tư thế co rúm che chắn
        double repelSpeed = Math.max(1.0, getSpeed()) * 1.3;
        boolean moved = tryMove((awayX / dist) * repelSpeed, (awayY / dist) * repelSpeed, room);

        // Nếu bị dồn vào góc tường không thể lùi tiếp -> kích hoạt Panic Sprint bỏ chạy
        if (!moved) {
            triggerPanicSprint(player, now);
        }
    }

    
    // =========================================================
    // CÁC HÀM HỖ TRỢ DI CHUYỂN & TƯỜNG
    // =========================================================
    @Override
public void takeDamage(int amount) {
    if (state == State.DEATH || submergedInWall) return;

    super.takeDamage(amount);
    isArmActive = false;
    isGrabbingPlayer = false;

    if (getHp() <= 0) {
        enterDeathState();
        return;
    }

    if (state != State.PANIC_SPRINT) {
        long now = System.nanoTime();
        state = State.PANIC_SPRINT;
        sprintStartTime = now;
        sprintEndTime = now + SPRINT_DURATION_NANOS;
    }
}

private void enterDeathState() {
    if (state == State.DEATH) return;
    state = State.DEATH;
    deathStartTime = System.nanoTime();
    submergedInWall = false;
    isArmActive = false;
    isGrabbingPlayer = false;
}

public boolean isDying() {
    return state == State.DEATH;
}

public int getDeathFrameIndex() {
    if (state != State.DEATH) return 0;
    long elapsed = Math.max(0L, System.nanoTime() - deathStartTime);
    int idx = (int) (elapsed / DEATH_FRAME_DURATION_NANOS);
    return Math.min(idx, DEATH_FRAME_COUNT - 1);
}

@Override
public boolean isRemovable() {
    if (!isDead()) {
        return false;
    }
    if (state != State.DEATH) {
        enterDeathState();
        return false;
    }
    long totalNanos = DEATH_FRAME_COUNT * DEATH_FRAME_DURATION_NANOS + DEATH_CORPSE_HOLD_NANOS;
    return (System.nanoTime() - deathStartTime) >= totalNanos;
}

    public void onAlmondWaterUsed(double x, double y, double radius, long durationNanos) {
    this.safeZoneX = x;
    this.safeZoneY = y;
    this.safeZoneRadius = radius;
    this.safeZoneEndTime = System.nanoTime() + durationNanos;

    if (isGrabbingPlayer || isInsideSafeZone(getCenterX(), getCenterY())) {
        isGrabbingPlayer = false;
        submergedInWall = false;
        isArmActive = false;
        this.state = State.REPELLED;
    }
}

    // Gọi hàm này khi Player bấm phím vùng thoát lúc đang bị tóm
    public void onPlayerStruggle(Player player) {
        if (!isGrabbingPlayer) return;
        struggleProgress++;
        if (struggleProgress >= STRUGGLE_TO_ESCAPE) {
            releasePlayer(player);
            snatchPhase = SnatchPhase.RETRACTING;
        }
    }

   private void triggerPanicSprint(Player player, long now) {
    releasePlayer(player);
    submergedInWall = false;
    isArmActive = false;
    state = State.PANIC_SPRINT;
    sprintStartTime = now;
    sprintEndTime = now + SPRINT_DURATION_NANOS;
}

    private void releasePlayer(Player player) {
        isGrabbingPlayer = false;
        struggleProgress = 0;
        if (player != null) {
            player.setGrabbed(false);
        }
    }

    private boolean isInsideSafeZone(double x, double y) {
        return Math.hypot(x - safeZoneX, y - safeZoneY) <= safeZoneRadius;
    }

    private boolean tryMove(double dx, double dy, Room room) {
        double nextX = getX() + dx;
        double nextY = getY() + dy;
        if (canMoveTo(nextX, nextY, room)) {
            setX(nextX);
            setY(nextY);
            if (dx != 0) setFacingX(dx); // Cập nhật hướng mặt khi chạy
            return true;
        }
        return false;
    }
    private boolean canMoveTo(double nextX, double nextY, Room room) {
    if (room == null) return true;
    Rectangle nextBounds = new Rectangle((int) nextX, (int) nextY, getWidth(), getHeight());
    return !room.checkCollision(nextBounds);
}
private boolean isNearWall(Room room) {
        if (room == null) return true;
        int margin = 12;
        return !canMoveTo(getX() - margin, getY(), room)
            || !canMoveTo(getX() + margin, getY(), room)
            || !canMoveTo(getX(), getY() - margin, room)
            || !canMoveTo(getX(), getY() + margin, room);
    }

// Tìm hướng có bức tường gần nhất (4 hướng) để Boss áp sát ẩn nấp
private void moveTowardNearestWall(Room room) {
    double speed = Math.max(1.0, getSpeed());
    int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    int bestDx = -1, bestDy = 0;
    int minSteps = Integer.MAX_VALUE;

    for (int[] d : dirs) {
        for (int step = 1; step <= 15; step++) {
            double testX = getX() + d[0] * step * 16;
            double testY = getY() + d[1] * step * 16;
            if (!canMoveTo(testX, testY, room)) {
                if (step < minSteps) {
                    minSteps = step;
                    bestDx = d[0];
                    bestDy = d[1];
                }
                break;
            }
        }
    }

    if (!tryMove(bestDx * speed, bestDy * speed, room)) {
        tryMove(bestDy * speed, bestDx * speed, room);
    }
}
   private void startWallSnatch(Player player, Room room) {
    state = State.WALL_SNATCH;
    snatchPhase = SnatchPhase.EXTENDING;
    isArmActive = true;
    armProgress = 0.0;

    double startX = getCenterX();
    double startY = getCenterY();
    targetX = player.getX() + player.getWidth() / 2.0;
    targetY = player.getY() + player.getHeight() / 2.0;

    armStartX = startX;
    armStartY = startY;

    if (!submergedInWall && room != null) {
        double dist = Math.hypot(targetX - startX, targetY - startY);
        int steps = (int) (dist / 8.0);
        boolean passedThroughWall = false;

        for (int i = 1; i <= steps; i++) {
            double t = (double) i / steps;
            double px = startX + (targetX - startX) * t;
            double py = startY + (targetY - startY) * t;
            Rectangle probe = new Rectangle((int) px - 4, (int) py - 4, 8, 8);

            if (room.checkCollision(probe)) {
                passedThroughWall = true;
                armStartX = px;
                armStartY = py;
            } else if (passedThroughWall) {
                armStartX = px;
                armStartY = py;
                break;
            }
        }
    }

    armEndX = armStartX;
    armEndY = armStartY;
    faceToward(player);
}

private boolean hasWallBetween(double x1, double y1, double x2, double y2, Room room) {
    if (room == null) return false;
    double dist = Math.hypot(x2 - x1, y2 - y1);
    int steps = (int) (dist / 8.0);
    for (int i = 1; i < steps; i++) {
        double t = (double) i / steps;
        double px = x1 + (x2 - x1) * t;
        double py = y1 + (y2 - y1) * t;
        if (room.checkCollision(new Rectangle((int) px - 4, (int) py - 4, 8, 8))) {
            return true;
        }
    }
    return false;
}
    
@Override
public String getSpriteKey() {
    return "boss4";
}

@Override
public String getAnimState() {
    return switch (state) {
        case PANIC_SPRINT -> "walk";
        case WALL_SNATCH -> "attack";
        case REPELLED -> "hurt";
        case DEATH -> "death";
        case SHADOW_AMBUSH, IN_WALL -> "idle";
    };
}

@Override
public int resolveAnimFrameIndex(String animState, int frameCount) {
    if (frameCount <= 1) {
        return 0;
    }
    if (state == State.DEATH) {
        return Math.min(getDeathFrameIndex(), frameCount - 1);
    }
    return super.resolveAnimFrameIndex(animState, frameCount);
}
    @Override
public void updateBehavior(Player player, Room room) {
    updateBoss4(player, room, System.nanoTime());

    // Đảm bảo mở khóa di chuyển nếu Boss vừa bị đánh bật sang trạng thái khác lúc đang tóm
    if (!isGrabbingPlayer && player != null && player.isGrabbed()) {
        player.setGrabbed(false);
    }
}
// Trả về {minX, maxX, minY, maxY} là khung sàn sát 4 bức tường bên trong phòng
private double[] getWalkableBounds(Room room) {
    if (room == null) {
        return new double[]{getX(), getX(), getY(), getY()};
    }

    int w = room.getWidth();
    int h = room.getHeight();
    int padX = room.getPadX();
    int padY = room.getPadY();

    int minTx = Integer.MAX_VALUE, maxTx = Integer.MIN_VALUE;
    int minTy = Integer.MAX_VALUE, maxTy = Integer.MIN_VALUE;

    for (int tx = padX; tx < w - padX; tx++) {
        for (int ty = padY; ty < h - padY; ty++) {
            if (room.getTile(tx, ty) == TileType.FLOOR) {
                if (tx < minTx) minTx = tx;
                if (tx > maxTx) maxTx = tx;
                if (ty < minTy) minTy = ty;
                if (ty > maxTy) maxTy = ty;
            }
        }
    }

    if (minTx > maxTx || minTy > maxTy) {
        return new double[]{getX(), getX(), getY(), getY()};
    }

    double minX = minTx * Room.TILE_SIZE;
    double maxX = Math.max(minX, (maxTx + 1) * Room.TILE_SIZE - getWidth());
    double minY = minTy * Room.TILE_SIZE;
    double maxY = Math.max(minY, (maxTy + 1) * Room.TILE_SIZE - getHeight());
    return new double[]{minX, maxX, minY, maxY};
}

private double posToPerimeter(double x, double y, double[] b) {
    double minX = b[0], maxX = b[1], minY = b[2], maxY = b[3];
    double boxW = maxX - minX;
    double boxH = maxY - minY;
    double cx = Math.max(minX, Math.min(maxX, x));
    double cy = Math.max(minY, Math.min(maxY, y));

    double dTop = cy - minY;
    double dBot = maxY - cy;
    double dLeft = cx - minX;
    double dRight = maxX - cx;

    double minDist = Math.min(Math.min(dTop, dBot), Math.min(dLeft, dRight));
    if (minDist == dTop)   return cx - minX;
    if (minDist == dRight) return boxW + (cy - minY);
    if (minDist == dBot)   return boxW + boxH + (maxX - cx);
    return 2.0 * boxW + boxH + (maxY - cy);
}

private double[] perimeterToPos(double p, double[] b) {
    double minX = b[0], maxX = b[1], minY = b[2], maxY = b[3];
    double boxW = maxX - minX;
    double boxH = maxY - minY;

    if (p <= boxW) {
        return new double[]{minX + p, minY};
    } else if (p <= boxW + boxH) {
        return new double[]{maxX, minY + (p - boxW)};
    } else if (p <= 2.0 * boxW + boxH) {
        return new double[]{maxX - (p - boxW - boxH), maxY};
    } else {
        return new double[]{minX, maxY - (p - 2.0 * boxW - boxH)};
    }
}

public boolean isSubmergedInWall() {
    return submergedInWall;
}

    // Getters phục vụ GameRenderer
    public boolean isArmActive() { return isArmActive; }
    public double getArmStartX() { return armStartX; }
    public double getArmStartY() { return armStartY; }
    public double getArmEndX() { return armEndX; }
    public double getArmEndY() { return armEndY; }
    public boolean isSafeZoneActive() { return System.nanoTime() < safeZoneEndTime; }
    public double getSafeZoneX() { return safeZoneX; }
    public double getSafeZoneY() { return safeZoneY; }
    public double getSafeZoneRadius() { return safeZoneRadius; }

}
