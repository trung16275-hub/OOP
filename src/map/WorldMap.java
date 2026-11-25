package map;

import java.util.ArrayList;
import java.util.List;

public class WorldMap {
    public static final int GRID_SIZE = 7;
    private final int keysRequired = 3;

    private FloorTheme theme;
    private int keysCollected;

    private final Room[][] grid;
    private final List<Room> rooms;

    private Room currentRoom;
    private Room spawnRoom;

    private boolean bossRoomUnlocked;

    public WorldMap(int floorLevel) {
        switch (floorLevel) {
            case 1 -> theme = FloorTheme.LEVEL_0_OFFICE;
            case 2 -> theme = FloorTheme.LEVEL_2_PIPEDREAM;
            case 3 -> theme = FloorTheme.LEVEL_52_SCHOOL;
        }
        this.keysCollected = 0;
        this.bossRoomUnlocked = false;

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
        return theme.getFloorLevel();
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

    public Room getBossRoom() {
        for (Room r : rooms) {
            if (r.getRoomType() == Room.RoomType.BOSS) return r;
        }
        return null;
    }

    public void collectKey () {
        keysCollected ++;
    }

    public boolean isBossRoomUnlocked() {
        return bossRoomUnlocked;
    }

    public boolean leadsToBoss(Room room, Direction dir) {
        if (room == null || dir == null) return false;
        Room neighbor = getRoom(room.getGridX() + dir.getDx(), room.getGridY() + dir.getDy());
        return neighbor != null && neighbor.getRoomType() == Room.RoomType.BOSS;
    }

    public boolean isConnectedToBoss (Room room) {
        if (room == null) return false;
        for (Direction dir : room.getDoors().keySet()) {
            if (leadsToBoss(room, dir)) {
                return true;
            }
        }
        return false;
    }

    public void unlockBossRoom() {
        this.bossRoomUnlocked = true;
        Room bossRoom = getBossRoom();
        if (bossRoom == null) return;
        for (Direction dir : Direction.values()) {
            Room neighbor = getRoom(bossRoom.getGridX() + dir.getDx(), bossRoom.getGridY() + dir.getDy());
            if (neighbor != null) {
                Direction doorToBoss = dir.opposite();
                neighbor.unlockDoor(doorToBoss);
                if (neighbor.isCleared()) {
                    neighbor.carveDoor(doorToBoss, true);
                }
            }
        }
    }

    public void setSpawnRoom(Room spawnRoom) {
        this.spawnRoom = spawnRoom;
    }

    public boolean hasVisitedNeighbor (int gx, int gy) {
        for (Direction dir : Direction.values()) {
            int nx = gx + dir.getDx();
            int ny = gy + dir.getDy();

            if (nx >= 0 && nx < WorldMap.GRID_SIZE && ny >= 0 && ny < WorldMap.GRID_SIZE) {
                Room neighbor = getRoom(nx, ny);
                if (neighbor != null && neighbor.isVisited()) {
                    return true;
                }
            }

        }

        return false;
    }
}
