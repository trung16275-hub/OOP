package model.prop;

public record BandagePickup(Prop prop, int amount) {
    public static final int HEAL_AMOUNT = 20;
    public static final String DISPLAY_NAME = "Bandage";
    public static final String SPRITE_PATH = "/pickups/pickup_bandage.png";

    public BandagePickup {
        if (amount <= 0) {
            throw new IllegalArgumentException("Healing must be positive.");
        }
    }

    public BandagePickup(int x, int y) {
        this(new Prop(SPRITE_PATH, x, y, 32, 30, false), HEAL_AMOUNT);
    }
}
