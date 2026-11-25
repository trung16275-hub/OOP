package model.prop;

/** A rifle ammo box placed in a room. */
public record AmmoPickup(Prop prop, int rounds) {
    public static final int MAX_ROUNDS = 5;
    public static final int PICKUP_RANGE_PIXELS = 20;
    public static final String DISPLAY_NAME = "Hộp đạn súng Ak47";
    public static final String SPRITE_PATH = "/pickups/pickup_ammo_rifle.png";

    public AmmoPickup {
        if (rounds < 1 || rounds > MAX_ROUNDS) {
            throw new IllegalArgumentException("Ammo box must contain 1 to 5 rounds.");
        }
    }

    public AmmoPickup(int x, int y) {
        this(new Prop(SPRITE_PATH, x, y, 32, 30, false), MAX_ROUNDS);
    }
}
