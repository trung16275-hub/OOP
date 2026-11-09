package test;

import model.Boss1;
import model.Player;
import model.Weapon;

public class CombatTest {
    public static void main(String[] args) {
        Player player = new Player();
        player.equip(new Weapon("testest", 25));
        Boss1 boss = new Boss1();
        System.out.println("Boss ban đầu HP: " + boss.getHp());
        player.attack(boss);
        System.out.println("Sau khi Player đánh: " + boss.getHp());
    }
}
