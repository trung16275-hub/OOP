package model;

import java.util.ArrayList;
import java.util.List;

// File: src/enemy/Enemy.java
public abstract class Enemy extends Combatant {
    private int baseDamage;
    private double detectionRange ; 


    public Enemy (String name, int maxHp, int speed, int baseDamage, double detectionRange ) {
        super(name, maxHp,speed);
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

    // Mỗi người sẽ tự viết logic tấn công riêng cho Boss của mình
    public abstract void updateBehavior();
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
    public void chase(Player player) {
    if (!canDetect(player)) {
        return;
    }

    // Khoảng cách giữa hai tâm nhân vật
    double dx = (player.getX() + player.getWidth() / 2.0)
              - (getX() + getWidth() / 2.0);

    double dy = (player.getY() + player.getHeight() / 2.0)
              - (getY() + getHeight() / 2.0);

    double distance = Math.sqrt(dx * dx + dy * dy);

    // Tránh chia cho 0 và dừng khi đến gần Player
    if (distance <= 1) {
        return;
    }

    // Chuẩn hóa vector hướng và di chuyển
    double step = Math.min(getSpeed(), distance);

    setX(getX() + dx / distance * step);
    setY(getY() + dy / distance * step);
}

}
