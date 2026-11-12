package map;

import java.util.ArrayList;
import java.util.List;

public class WorldMap {
    private static final int GRID_SIZE = 9;

    private final int floorLevel;
    private final Room[][] grid;

    private Room currentRoom;
    private int currentX, currentY;

    public WorldMap(int floorLevel) {
        this.floorLevel = floorLevel;
        this.grid = new Room[GRID_SIZE][GRID_SIZE];
        this.currentX = 4; this.currentY = 4;
        this.currentRoom = grid[currentX][currentY];

    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public Room getRoom(int x, int y) {
        return grid[x][y];
    }

    public void setRoom(int x, int y, Room room) {
        grid[x][y] = room;
    }


}
