package model.creep;

import model.Enemy;

/** A small, red dungeon creep. */
public class Slime extends Enemy {
    private static final String DEFAULT_NAME = "Slime";
    private static final int DEFAULT_MAX_HP = 50;
    private static final int DEFAULT_SPEED = 0;
    private static final int DEFAULT_BASE_DAMAGE = 8;
    private static final double DEFAULT_DETECTION_RANGE = 220;
    private static final double DEFAULT_HITBOX_RADIUS = 18;
    private static final int DEFAULT_SIZE = 36;

    public Slime() {
        super(DEFAULT_NAME, DEFAULT_MAX_HP, DEFAULT_SPEED, DEFAULT_BASE_DAMAGE,
                DEFAULT_DETECTION_RANGE, DEFAULT_HITBOX_RADIUS);
        setWidth(DEFAULT_SIZE);
        setHeight(DEFAULT_SIZE);
    }
}
