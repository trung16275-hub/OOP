package test;

public class Room3MainTest {
    private static final String ENEMY_CLASS = "model.boss.Boss4";

    private static final double CAMERA_ZOOM = 0.75;

    public static void main(String[] args) {
        EnemyTestRoom.launch("Test Room 3", ENEMY_CLASS, CAMERA_ZOOM);
    }
}
