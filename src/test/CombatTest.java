package test;

import model.Player;
import model.weapon.Weapon;
import model.weapon.Ak47;
import model.boss.Boss1;

public class CombatTest {
    public static void main(String[] args) {
        Player player = new Player();
        player.equip(new Weapon("testest", 25));
        Boss1 boss = new Boss1();
        System.out.println("Boss ban đầu HP: " + boss.getHp());
        player.attack(boss);
        System.out.println("Sau khi Player đánh: " + boss.getHp());

        Player shooter = new Player();
        shooter.equip(new Ak47());
        Ak47 rifle = (Ak47) shooter.getCurrentWeapon();
        if (rifle.getAmmo() != 30 || rifle.getMagazineSize() != 30) {
            throw new AssertionError("AK47 phải bắt đầu với 30/30 viên đạn");
        }
        Boss1 target = new Boss1();
        int hpBeforeShot = target.getHp();
        shooter.attack(target);
        if (target.getHp() != hpBeforeShot - 20) {
            throw new AssertionError("AK47 phải gây đúng 20 damage mỗi viên");
        }
        if (rifle.getAmmo() != 29) {
            throw new AssertionError("Mỗi lần bắn phải trừ một viên đạn");
        }
        for (int i = 0; i < 29; i++) {
            rifle.consumeAmmo();
        }
        if (rifle.hasAmmo() || rifle.consumeAmmo()) {
            throw new AssertionError("AK47 không được bắn khi đã hết đạn");
        }
        System.out.println("✓ AK47 gây 20 damage; đạn giảm từ 30/30 về 0/30 và không bắn khi hết.");
    }
}
