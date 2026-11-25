package model.prop;

public record LiquidPain(Prop prop) {
    public static final String DISPLAY_NAME = "Liquid Pain";
    public static final String SPRITE_PATH = "/pickups/buff_liquid_pain.png";
    public static final int HP_COST = 200;

    public LiquidPain(int x, int y) {
        this(new Prop(SPRITE_PATH, x, y, 22, 34, false));
    }
}
