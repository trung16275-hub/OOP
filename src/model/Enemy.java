package model;

import map.Room;
import model.player.Player;
import java.util.List;
import java.util.function.Consumer;

// File: src/enemy/Enemy.java
public abstract class Enemy extends Combatant {
    private int baseDamage;
    private double detectionRange ;
    private static final long CONTACT_COOLDOWN_NANOS = 10_000_000_000L; /// Thời gian 2 skill liên tiếp là 1s
    private long lastContactAttackTime;
    private boolean hasContactAttacked = false;

    public void tryContactAttack(Player player) {
        if (player == null || player.isDead() || isDead()) {
            return;
        }

        if (!player.intersectsHurtBox(this)) {
            return;
        }

        long now = System.nanoTime();
        if (!hasContactAttacked|| now - lastContactAttackTime >= CONTACT_COOLDOWN_NANOS) {
            attack(player);
            lastContactAttackTime = now;
            hasContactAttacked = true;
        }
    }


    public Enemy (String name, int maxHp, int speed, int baseDamage, double detectionRange ) {
        super(name, maxHp,speed);
        this.baseDamage = baseDamage;
        this.detectionRange = detectionRange;
    }
    public Enemy(String name, int maxHp, int speed, int baseDamage,double detectionRange, double hitboxRadius) {
        super(name, maxHp, speed, hitboxRadius);
        this.baseDamage = baseDamage;
        this.detectionRange = detectionRange;
    }

    public int getBaseDamage() {
        return  baseDamage;
    }

    public void setBaseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
    }

    @Override
    public void attack(Combatant target){
        if(target!=null && !target.isDead()) {
            target.takeDamage(baseDamage);
        }

    }

    // Hành vi chung cho mọi Enemy.
    // Boss có thể override phương thức này và gọi super.updateBehavior(player)
    // để giữ lại hành vi đuổi theo và tấn công tiếp xúc.
    public void updateBehavior(Player player, Room room) {
        if (player == null || player.isDead() || isDead()) {
            return;
        }

        chase(player,room);
        tryContactAttack(player);
    }
    public double getDetectionRange ( )
    {
        return detectionRange ;
    }
    public boolean canDetect(Player player) {
        if (player == null || player.isDead() || this.isDead()) {
            return false;
        }
        return distanceTo(player) <= detectionRange;
    }
    public void chase(Player player, Room room) {
    if (!canDetect(player)) {
        return;
    }

    faceToward(player);

    // Khoảng cách giữa hai tâm nhân vật
    double dx = player.getCenterX() - getCenterX();
    double dy = player.getCenterY() - getCenterY();
    double distance = Math.hypot(dx, dy);

    // Tránh chia cho 0 và dừng khi đến gần Player
    if (distance <= 1) {
        return;
    }

    // Chuẩn hóa vector hướng và di chuyển

    double dirX = dx / distance;
    double dirY = dy / distance;

    int enemySpeed = getSpeed();
    if (distance < enemySpeed) {
        dirX *= (distance / enemySpeed);
        dirY *= (distance / enemySpeed);
    }

    move(dirX, dirY, room);
    }

    // Trả về tên bộ sprite trong assets/animations.properties (mặc định null nếu chưa có ảnh)
    public String getSpriteKey() {
        return null;
    }

    // Trả về trạng thái animation hiện tại: "idle", "run", "attack", "death"...
    public String getAnimState() {
        return "idle";
    }

    // Thời lượng mỗi frame (ms) cho trạng thái animation hiện tại.
    public int getAnimFrameDurationMs(String state) {
        return 100;
    }

    // Chỉ số frame cần vẽ cho trạng thái hiện tại (mặc định lặp theo thời gian).
    public int resolveAnimFrameIndex(String state, int frameCount) {
        if (frameCount <= 1) {
            return 0;
        }
        int duration = Math.max(1, getAnimFrameDurationMs(state));
        return (int) ((System.currentTimeMillis() / duration) % frameCount);
    }

    // Trả về true khi Enemy đã chết và hoàn tất mọi hoạt ảnh chết/phân thân để có thể xóa khỏi phòng.
    public boolean isRemovable() {
        return isDead();
    }

    public boolean shouldReplace() {
        return false;
    }

    public void onRoomEntered() {
    }

    public void updateRoomEffects(Consumer<RoomEffect> spawnEffect) {
    }

    public List<? extends Enemy> createReplacements(Room room) {
        return List.of();
    }
}
