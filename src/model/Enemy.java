package model;

import java.util.ArrayList;
import java.util.List;

// File: src/enemy/Enemy.java
public abstract class Enemy extends Combatant {
    private int baseDamage;

    public Enemy (String name, int maxHp, int baseDamage) {
        super(name, maxHp);
        this.baseDamage = baseDamage;
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
}
