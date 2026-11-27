package model.prop;

public record KevlarArmorPickup(Prop prop, int amount) {
    public static final int ARMOR_AMOUNT = 30;
    public static final String DISPLAY_NAME = "Kevlar Armor";
    public static final String SPRITE_PATH = "/pickups/pickup_kevlar_armor.png";

    public KevlarArmorPickup {
        if (amount <= 0) {
            throw new IllegalArgumentException("Armor must be positive.");
        }
    }

    public KevlarArmorPickup(int x, int y) {
        this(new Prop(SPRITE_PATH, x, y, 32, 30, false), ARMOR_AMOUNT);
    }
}
