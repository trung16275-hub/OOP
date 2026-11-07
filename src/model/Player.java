package model;

// File: src/player/Player.java
public class Player extends Combatant {
    private Weapon currentWeapon;
    //default constructor for testing
    public Player() {
        this("Hero", 100);
    }

    public Player (String name, int maxHp) {
        super(name, maxHp);
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public void equip(Weapon w) {
        this.currentWeapon = w;
        System.out.println("Đã trang bị: " + w.name);
    }

    @Override
    public void attack(Combatant target) {
        if (currentWeapon != null && target !=null) {
            target.takeDamage(currentWeapon.damage);
        }
    }
}
