package model.pickup;

import model.prop.RoomProp;

import java.util.List;
import model.player.Player;
import model.weapon.Weapon;

public record LiquidPain(RoomProp prop) implements Pickup {
    public static final String DISPLAY_NAME = "Liquid Pain";
    public static final String SPRITE_PATH = "/pickups/buff_liquid_pain.png";
    public static final int HP_COST = 200;

    public LiquidPain(int x, int y) {
        this(new RoomProp(SPRITE_PATH, x, y, 22, 34, false));
    }

    @Override
    public String prompt() {
        return DISPLAY_NAME + " · -" + HP_COST + " HP";
    }

    @Override
    public Pickup collect(Player player, List<Weapon> weapons) {
        player.takeDirectDamage(HP_COST);
        return null;
    }
}
