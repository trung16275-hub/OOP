package model;

import map.Room;

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

        if (!isWithinHitbox(player)) {
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

        double dx = (player.getX() + player.getWidth() / 2.0)
                  - (getX() + getWidth() / 2.0);

        double dy = (player.getY() + player.getHeight() / 2.0)
                  - (getY() + getHeight() / 2.0);

        double distance = Math.sqrt(dx * dx + dy * dy);

        return distance <= detectionRange;
    }
    public void chase(Player player, Room room) {
    if (!canDetect(player)) {
        return;
    }

    faceToward(player);

    // Khoảng cách giữa hai tâm nhân vật
    double dx = (player.getX() + player.getWidth() / 2.0)
              - (getX() + getWidth() / 2.0);

    double dy = (player.getY() + player.getHeight() / 2.0)
              - (getY() + getHeight() / 2.0);

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
<<<<<<< HEAD
=======
    // Trả về tên bộ sprite (mặc định null nếu quái thường chưa có ảnh)
>>>>>>> 45849b59d5baf0c5d133f97e632b41c71e4e5fbf
public String getSpritePath() {
    return null;
}

<<<<<<< HEAD
=======
// Trả về khóa animation tương ứng trong GameRenderer.
public String getAnimState() {
    return "idle";
}

// Trả về số frame của sprite; Boss có thể override nếu dùng sprite sheet.
>>>>>>> 45849b59d5baf0c5d133f97e632b41c71e4e5fbf
public int getSpriteFrameCount() {
    return 1;
}
}
