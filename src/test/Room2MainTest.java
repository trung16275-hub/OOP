package test;

public class Room2MainTest {
    private static final String ENEMY_CLASS = "model.boss.boss2.Boss2";

    private static final double CAMERA_ZOOM = 0.75;

    public static void main(String[] args) {
        EnemyTestRoom.launch("Test Room 2", ENEMY_CLASS, CAMERA_ZOOM);
    }
}
