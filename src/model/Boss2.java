package model;

public class Boss2 extends Enemy {
    // Chỉnh các chỉ số mặc định của Boss 2 tại đây.
    private static final String DEFAULT_NAME = "Boss 2";
    private static final int DEFAULT_MAX_HP = 150;
    private static final int DEFAULT_SPEED = 2;
    private static final int DEFAULT_BASE_DAMAGE = 2;
    private static final double DEFAULT_DETECTION_RANGE = 10;
    private static final double DEFAULT_HITBOX_RADIUS = 30;
    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 60;

    public Boss2() {
        this(DEFAULT_HITBOX_RADIUS);
    }

    public Boss2(double hitboxRadius) {
        super(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED, DEFAULT_BASE_DAMAGE,DEFAULT_DETECTION_RANGE, hitboxRadius);
        setWidth(DEFAULT_WIDTH);
        setHeight(DEFAULT_HEIGHT);
    }

    @Override
    public void updateBehavior() {
        // Logic tấn công của Boss 2
        System.out.println(this.getName() + " đang tấn công!");
    }
}
