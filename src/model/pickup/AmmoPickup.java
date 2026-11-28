package model.pickup;

import model.prop.RoomProp;

import java.util.List;
import model.player.Player;
import model.weapon.Weapon;

/** A rifle ammo box placed in a room. */
public record AmmoPickup(RoomProp prop, int rounds, Kind kind) implements Pickup {
    public enum Kind {
        AK47
    }

    public static final int MAX_ROUNDS = 5;
    public static final String DISPLAY_NAME = "Hộp đạn súng Ak47";
    public static final String SPRITE_PATH = "/pickups/pickup_ammo_rifle.png";

    public AmmoPickup {
        if (rounds < 1 || rounds > MAX_ROUNDS) {
            throw new IllegalArgumentException("Ammo box must contain 1 to 5 rounds.");
        }
    }

    public AmmoPickup(int x, int y) {
        this(new RoomProp(SPRITE_PATH, x, y, 32, 30, false), MAX_ROUNDS);
    }

    public AmmoPickup(RoomProp prop, int rounds) {
        this(prop, rounds, Kind.AK47);
    }

    @Override
    public String prompt() {
        return DISPLAY_NAME + " · " + rounds + " viên";
    }

    @Override
    public Pickup collect(Player player, List<Weapon> weapons) {
        for (var weapon : weapons) {
            int added = weapon.addAmmo(kind, rounds);
            if (added > 0) {
                return added == rounds ? null : new AmmoPickup(prop, rounds - added, kind);
            }
        }
        return this;
    }
}
