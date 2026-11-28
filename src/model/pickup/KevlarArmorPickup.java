package model.pickup;

import model.prop.RoomProp;

import java.util.List;
import model.player.Player;
import model.weapon.Weapon;

public record KevlarArmorPickup(RoomProp prop, int amount) implements Pickup {
    public static final int ARMOR_AMOUNT = 30;
    public static final String DISPLAY_NAME = "Kevlar Armor";
    public static final String SPRITE_PATH = "/pickups/pickup_kevlar_armor.png";

    public KevlarArmorPickup {
        if (amount <= 0) {
            throw new IllegalArgumentException("Armor must be positive.");
        }
    }

    public KevlarArmorPickup(int x, int y) {
        this(new RoomProp(SPRITE_PATH, x, y, 32, 30, false), ARMOR_AMOUNT);
    }

    @Override
    public String prompt() {
        return DISPLAY_NAME + " · +" + amount + " giáp";
    }

    @Override
    public Pickup collect(Player player, List<Weapon> weapons) {
        player.addArmor(amount);
        return null;
    }
}
