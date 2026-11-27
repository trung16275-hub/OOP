
package model.boss.boss2;

import model.Enemy;
import model.player.Player;
import map.Room;

public class Boss2Minion extends Enemy {

    private static final int MAX_HP = 36;
    private static final int SPEED = 3;
    private static final int DAMAGE = 5;

    private static final double DETECTION_RANGE = 500;
    private static final double HITBOX_RADIUS = 17.5;

    private static final int WIDTH = 35;
    private static final int HEIGHT = 35;

    private boolean moving;

    public Boss2Minion(double x, double y) {
        super(
                "Boss 2 Minion",
                MAX_HP,
                SPEED,
                DAMAGE,
                DETECTION_RANGE,
                HITBOX_RADIUS
        );

        setX(x);
        setY(y);
        setWidth(WIDTH);
        setHeight(HEIGHT);
    }

    @Override
    public void updateBehavior(Player player, Room room) {

        if (isDead() || player == null || player.isDead()) {
            moving = false;
            return;
        }

        if (!canDetect(player)) {
            moving = false;
            return;
        }

        double oldX = getX();
        double oldY = getY();

        faceToward(player);
        chase(player, room);

        moving = getX() != oldX || getY() != oldY;

        // Tấn công khi tiếp xúc Player
        tryContactAttack(player);
    }

    @Override
    public String getSpriteKey() {
        return "boss2";
    }

    @Override
    public String getAnimState() {
        return moving ? "run" : "idle";
    }

    @Override
    public int getAnimFrameDurationMs(String state) {
        return "run".equals(state) ? 110 : 220;
    }
}
