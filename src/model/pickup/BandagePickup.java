package model.pickup;

import model.prop.RoomProp;

import java.util.List;
import model.player.Player;
import model.weapon.Weapon;

public record BandagePickup(RoomProp prop, int amount) implements Pickup {
    public static final int HEAL_AMOUNT = 20;
    public static final String DISPLAY_NAME = "Bandage";
    public static final String SPRITE_PATH = "/pickups/pickup_bandage.png";

    public BandagePickup {
        if (amount <= 0) {
            throw new IllegalArgumentException("Healing must be positive.");
        }
    }

    public BandagePickup(int x, int y) {
        this(new RoomProp(SPRITE_PATH, x, y, 32, 30, false), HEAL_AMOUNT);
    }

    @Override
    public String prompt() {
        return DISPLAY_NAME + " · +" + amount + " HP";
    }

    @Override
    public Pickup collect(Player player, List<Weapon> weapons) {
        if (player.getHp() >= player.getMaxHp()) {
            return this;
        }
        player.heal(amount);
        return null;
    }
}
