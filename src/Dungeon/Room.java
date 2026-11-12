package Dungeon;

import java.awt.Rectangle;

public class Room {
    public int x;
    public int y;
    public int width;
    public int height;
    public boolean isBossRoom = false;
    public boolean isSpawnRoom = false;

    public Room(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getCenterX() {
        return x + width / 2;
    }

    public int getCenterY() {
        return y + height / 2;
    }

    public boolean intersects(Room other, int padding) {
        Rectangle r1 = new Rectangle(x - padding, y - padding, width + padding * 2, height + padding * 2);
        Rectangle r2 = new Rectangle(other.x, other.y, other.width, other.height);
        return r1.intersects(r2);
    }
}
