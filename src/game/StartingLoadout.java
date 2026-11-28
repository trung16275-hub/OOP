package game;

import java.util.ArrayList;
import java.util.List;
import model.player.Player;
import model.pickup.AmmoPickup;
import model.pickup.BandagePickup;
import model.pickup.KevlarArmorPickup;
import model.pickup.LiquidPain;
import model.pickup.Pickup;
import model.weapon.Ak47;
import model.weapon.Bat;
import model.weapon.Weapon;

public final class StartingLoadout {
    private StartingLoadout() {
    }

    /** The first weapon is equipped when a new game starts. */
    public static List<Weapon> createWeapons() {
        return new ArrayList<>(List.of(new Ak47(), new Bat()));
    }

    public static List<Pickup> createSpawnPickups(Player player) {
        int x = (int) player.getX();
        int y = (int) player.getY();
        int bottom = y + player.getHeight();
        return List.of(
                new AmmoPickup(x + player.getWidth() + 8, bottom - 30),
                new LiquidPain(x + 19, bottom + 32),
                new BandagePickup(x - 38, bottom - 30),
                new KevlarArmorPickup(x + 19, y - 32)
        );
    }
}
