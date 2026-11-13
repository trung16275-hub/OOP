package model;

public class Boss3 extends Enemy {
    // Chỉnh các chỉ số mặc định của Boss 3 tại đây.
    private static final String DEFAULT_NAME = "Boss 3";
    private static final int DEFAULT_MAX_HP = 200;
    private static final int DEFAULT_SPEED = 3;
    private static final int DEFAULT_BASE_DAMAGE = 2;
    private static final double DEFAULT_DETECTION_RANGE = Double.POSITIVE_INFINITY;
    private static final double DEFAULT_HITBOX_RADIUS = 30;
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;

    public Boss3() {
        this(DEFAULT_HITBOX_RADIUS);
    }

    public Boss3(double hitboxRadius) {
        super(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED, DEFAULT_BASE_DAMAGE,
                DEFAULT_DETECTION_RANGE, hitboxRadius);
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 3
        System.out.println(this.getName() + " đang tấn công!");
    }
}
