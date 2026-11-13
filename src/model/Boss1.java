package model;

public class Boss1 extends Enemy {
    // Chỉnh các chỉ số mặc định của Boss 1 tại đây.
    private static final String DEFAULT_NAME = "Boss 1";
    private static final int DEFAULT_MAX_HP = 100;
    private static final int DEFAULT_SPEED = 0;
    private static final int DEFAULT_BASE_DAMAGE = 10;
    private static final double DEFAULT_DETECTION_RANGE = 500;
    private static final double DEFAULT_HITBOX_RADIUS = 200;
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;

    public Boss1() {
        this(DEFAULT_HITBOX_RADIUS);
    }

    public Boss1(double hitboxRadius) {
        super(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED, DEFAULT_BASE_DAMAGE,DEFAULT_DETECTION_RANGE, hitboxRadius);
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 1
        System.out.println(this.getName() + " đang tấn công!");
    }
}
