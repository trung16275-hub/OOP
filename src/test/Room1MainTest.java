package test;

public class Room1MainTest {
    private static final String ENEMY_CLASS = "model.boss.Boss1";

    private static final double CAMERA_ZOOM = 1 ;

    public static void main(String[] args) {
        EnemyTestRoom.launch("Test Room 1", ENEMY_CLASS, CAMERA_ZOOM);
    }
}
