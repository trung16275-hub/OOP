package model.creep;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import map.Room;
import model.Enemy;
import model.Player;

public class Duck extends Enemy {

    public enum State {
        IDLE,
        WALK,
        ATTACK,
        DEATH
    }

    private static final int MAX_DUCKS_PER_ROOM = 10;
    private static final Map<Room, Integer> ROOM_SPAWN_COUNTS = new WeakHashMap<>();

    // Đánh thường (cận chiến)
    private static final double ATTACK_RANGE = 50.0;
    private static final long ATTACK_DURATION_NANOS = 400_000_000L; // 4 frame * 100ms
    private static final long ATTACK_COOLDOWN_NANOS = 850_000_000L;

    // Hoạt ảnh chết & nhân đôi
    private static final int DEATH_FRAME_COUNT = 4;
    private static final long DEATH_FRAME_DURATION_NANOS = 100_000_000L;

    private State state = State.IDLE;
    private long attackStartTime = 0L;
    private long lastAttackEndTime = 0L;
    private boolean hasDealtAttackDamage = false;

    private long deathStartTime = 0L;
    private boolean splitAnimationFinished = false;
    private boolean hasSplit = false;

    public Duck(double x, double y) {
        // maxHp = 10 -> bắn 1 viên đạn hoặc đập 1 gậy là chết ngay
        super("Duck", 10, 10, 12, 400.0, 20.0);
        setX(x);
        setY(y);
        setWidth(46);
        setHeight(46);
    }

    @Override
    public void takeDamage(int amount) {
        if (state == State.DEATH) return;
        super.takeDamage(amount);
        if (getHp() <= 0) {
            enterDeathState();
        }
    }

    private void enterDeathState() {
        if (state == State.DEATH) return;
        state = State.DEATH;
        deathStartTime = System.nanoTime();
    }

    @Override
    public void updateBehavior(Player player, Room room) {
        if (room != null && !ROOM_SPAWN_COUNTS.containsKey(room)) {
            int initialCount = 0;
            if (room.getEnemies() != null) {
                for (Enemy e : room.getEnemies()) {
                    if (e instanceof Duck) initialCount++;
                }
            }
            ROOM_SPAWN_COUNTS.put(room, Math.max(1, initialCount));
        }

        long now = System.nanoTime();

        // 1. Đang chết -> chạy hết 4 frame Death rồi báo hiệu nhân đôi
        if (state == State.DEATH) {
            if (now - deathStartTime >= DEATH_FRAME_COUNT * DEATH_FRAME_DURATION_NANOS) {
                splitAnimationFinished = true;
            }
            return;
        }

        if (player == null || player.isDead()) {
            state = State.IDLE;
            return;
        }

        double myCenterX = getX() + getWidth() / 2.0;
        double myCenterY = getY() + getHeight() / 2.0;
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;
        double dx = playerCenterX - myCenterX;
        double dy = playerCenterY - myCenterY;
        double dist = Math.hypot(dx, dy);

        // 2. Đang đánh thường
        if (state == State.ATTACK) {
            long elapsed = now - attackStartTime;
            if (!hasDealtAttackDamage && elapsed >= ATTACK_DURATION_NANOS / 2) {
                if (dist <= ATTACK_RANGE + 14.0) {
                    player.takeDamage(getBaseDamage());
                }
                hasDealtAttackDamage = true;
            }
            if (elapsed >= ATTACK_DURATION_NANOS) {
                lastAttackEndTime = now;
                state = State.IDLE;
            }
            return;
        }

        // 3. Ngoài tầm phát hiện -> đứng yên
        if (dist > getDetectionRange()) {
            state = State.IDLE;
            return;
        }

        if (dx != 0) setFacingX(dx);

        // 4. Đủ gần -> đánh thường
        if (dist <= ATTACK_RANGE) {
            if (now - lastAttackEndTime >= ATTACK_COOLDOWN_NANOS) {
                state = State.ATTACK;
                attackStartTime = now;
                hasDealtAttackDamage = false;
            } else {
                state = State.IDLE;
            }
            return;
        }

        // 5. Đuổi theo Player
        state = State.WALK;
        double speed = Math.max(1.0, getSpeed());
        double vx = (dx / dist) * speed;
        double vy = (dy / dist) * speed;

        if (!tryMove(vx, vy, room)) {
            if (!tryMove(vx, 0, room)) {
                tryMove(0, vy, room);
            }
        }
    }

    public boolean isSplitAnimationFinished() {
        return splitAnimationFinished && !hasSplit;
    }

    public List<Duck> createSplitClones(Room room) {
        hasSplit = true;
        List<Duck> clones = new ArrayList<>();
        if (room == null) return clones;

        int currentTotal = ROOM_SPAWN_COUNTS.getOrDefault(room, 1);
        int canSpawn = Math.min(2, MAX_DUCKS_PER_ROOM - currentTotal);
        if (canSpawn <= 0) return clones;

        ROOM_SPAWN_COUNTS.put(room, currentTotal + canSpawn);
        double[] offsetsX = {-18.0, 18.0};

        for (int i = 0; i < canSpawn; i++) {
            double spawnX = getX() + offsetsX[i % offsetsX.length];
            double spawnY = getY();
            if (!canMoveTo(spawnX, spawnY, room)) {
                spawnX = getX();
            }
            clones.add(new Duck(spawnX, spawnY));
        }
        return clones;
    }

    private boolean tryMove(double dx, double dy, Room room) {
        double nextX = getX() + dx;
        double nextY = getY() + dy;
        if (canMoveTo(nextX, nextY, room)) {
            setX(nextX);
            setY(nextY);
            return true;
        }
        return false;
    }

    private boolean canMoveTo(double nextX, double nextY, Room room) {
        if (room == null) return true;
        return !room.checkCollision(new Rectangle((int) nextX, (int) nextY, getWidth(), getHeight()));
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
    public boolean isDead() {
        if (state == State.DEATH) {
            return hasSplit;
        }
        if (getHp() <= 0) {
            enterDeathState();
            return false;
        }
        return false;
    }

    @Override
    public String getSpritePath() {
        switch (state) {
            case WALK:
                return "/creep/duck/walk";
            case ATTACK:
                return "/creep/duck/attack";
            case DEATH:
                return "/creep/duck/death";
            case IDLE:
            default:
                return "/creep/duck/idle";
        }
    }

    @Override
    public int getSpriteFrameCount() {
        return 4;
    }
}