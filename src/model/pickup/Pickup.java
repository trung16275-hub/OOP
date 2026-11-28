package model.pickup;

import model.prop.RoomProp;

import java.util.List;
import model.player.Player;
import model.weapon.Weapon;

public interface Pickup {
    int PICKUP_RANGE_PIXELS = 20;

    RoomProp prop();

    String prompt();

    /** Returns the pickup left on the floor, or null if fully consumed. */
    Pickup collect(Player player, List<Weapon> weapons);
}
