package model;

// File: src/player/Player.java
public class Player {
    public int hp = 100;
    public Weapon currentWeapon;

    public void equip(Weapon w) {
        this.currentWeapon = w;
        System.out.println("Đã trang bị: " + w.name);
    }

    public void attack(Boss target) {
        if (currentWeapon != null) {
            target.takeDamage(currentWeapon.damage);
        }
    }
}
