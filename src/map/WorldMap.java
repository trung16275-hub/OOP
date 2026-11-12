package map;


import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WorldMap {
    private static final int GRID_SIZE = 9;

    private final int floorLevel;
    private final Room[][] grid;
    private final List<Room> rooms;

    private Room currentRoom;

    public WorldMap(int floorLevel) {
        this.floorLevel = floorLevel;
        this.grid = new Room[GRID_SIZE][GRID_SIZE];
        this.rooms = new ArrayList<>();
        this.currentRoom = grid[4][4];
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public List<Room> getRooms () {
        return rooms;
    }

    public Room getRoom(int x, int y) {
        return grid[x][y];
    }

    public void addRoom(int x, int y, Room room) {
        if (x<0||x>=GRID_SIZE||y<0||y>=GRID_SIZE) {
            throw new IndexOutOfBoundsException(
                    String.format("Room coordinates (%d, %d) out of bounds for grid size (%d, %d)", x, y, GRID_SIZE, GRID_SIZE));
        };
        grid[x][y] = room;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        currentRoom = room;
    }
}
