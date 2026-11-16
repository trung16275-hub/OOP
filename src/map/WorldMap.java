package map;

import java.util.ArrayList;
import java.util.List;

public class WorldMap {
    public static final int GRID_SIZE = 9;

    private final int floorLevel;
    private final Room[][] grid;
    private final List<Room> rooms;

    private Room currentRoom;
    private Room spawnRoom;

    public WorldMap(int floorLevel) {
        this.floorLevel = floorLevel;
        this.grid = new Room[GRID_SIZE][GRID_SIZE];
        this.rooms = new ArrayList<>();
    }
    /**
     * Thêm một phòng vào vị trí tọa độ (gx, gy) trên lưới 9x9.
     */
    public void addRoom(int gx, int gy, Room room) {
        if (gx < 0 || gx >= GRID_SIZE || gy < 0 || gy >= GRID_SIZE) {
            throw new IndexOutOfBoundsException(
                    String.format("Room coordinates (%d, %d) out of bounds for grid size (%d, %d)", gx, gy, GRID_SIZE, GRID_SIZE));
        }

        grid[gx][gy] = room;
        room.setGridX(gx);
        room.setGridY(gy);

        if (!rooms.contains(room)) {
            rooms.add(room);
        }

        if (room.getRoomType() == Room.RoomType.SPAWN) {
            this.spawnRoom = room;
            this.currentRoom = room;
        }
    }

    /**
     * Di chuyển nhân vật sang phòng kế tiếp theo hướng cửa bước vào.
     */
    public Room moveTo(Direction dir) {
        if (currentRoom == null || !currentRoom.hasDoor(dir)) {
            return currentRoom;
        }

        int targetX = currentRoom.getGridX() + dir.getDx();
        int targetY = currentRoom.getGridY() + dir.getDy();

        if (hasRoom(targetX, targetY)) {
            Room nextRoom = grid[targetX][targetY];
            this.currentRoom = nextRoom;

            return nextRoom;
        }

        return currentRoom;
    }

    public boolean hasRoom(int gx, int gy) {
        return gx >= 0 && gx < GRID_SIZE && gy >= 0 && gy < GRID_SIZE && grid[gx][gy] != null;
    }

    public Room getRoom(int gx, int gy) {
        if (gx < 0 || gx >= GRID_SIZE || gy < 0 || gy >= GRID_SIZE) {
            return null;
        }
        return grid[gx][gy];
    }

    public int getFloorLevel() {
        return floorLevel;
    }

    public List<Room> getRooms () {
        return rooms;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public Room getSpawnRoom() {
        if (spawnRoom == null) {
            for (Room r : rooms) {
                if (r.getRoomType() == Room.RoomType.SPAWN) {
                    spawnRoom = r;
                    break;
                }
            }
        }
        return spawnRoom;
    }

    public void setSpawnRoom(Room spawnRoom) {
        this.spawnRoom = spawnRoom;
    }
}
