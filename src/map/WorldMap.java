package map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldMap {
    public static final int GRID_SIZE = 7;

    private final int floorLevel;
    private final Room[][] grid;
    private final List<Room> rooms;
    private final Map<Room, Map<DoorKey, RoomLink>> roomLinks = new HashMap<>();

    private Room currentRoom;
    private Room spawnRoom;

    private record DoorKey(Direction direction, int offset) { }
    private record RoomLink(Room room, int arrivalOffset) { }
    public record RoomTransition(Room room, int arrivalOffset) { }

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

    /** Connects a specific doorway to a room, with a matching return doorway. */
    public void connectRooms(Room from, Direction direction, int fromOffset,
            Room to, int toOffset) {
        from.addDoorOffset(direction, fromOffset);
        to.addDoorOffset(direction.opposite(), toOffset);
        from.carveOpenPath(direction);
        to.carveOpenPath(direction.opposite());

        roomLinks.computeIfAbsent(from, key -> new HashMap<>())
                .put(new DoorKey(direction, fromOffset), new RoomLink(to, toOffset));
        roomLinks.computeIfAbsent(to, key -> new HashMap<>())
                .put(new DoorKey(direction.opposite(), toOffset), new RoomLink(from, fromOffset));
    }

    /** Transitions through the exact doorway that the player entered. */
    public RoomTransition moveTo(Direction dir, int doorOffset) {
        if (currentRoom == null || !currentRoom.hasDoor(dir)) {
            return new RoomTransition(currentRoom, doorOffset);
        }

        Map<DoorKey, RoomLink> links = roomLinks.get(currentRoom);
        RoomLink link = links == null ? null : links.get(new DoorKey(dir, doorOffset));
        if (link != null) {
            currentRoom = link.room();
            return new RoomTransition(currentRoom, link.arrivalOffset());
        }

        int targetX = currentRoom.getGridX() + dir.getDx();
        int targetY = currentRoom.getGridY() + dir.getDy();
        if (hasRoom(targetX, targetY)) {
            currentRoom = grid[targetX][targetY];
            return new RoomTransition(currentRoom, currentRoom.getDoorOffset(dir.opposite()));
        }

        return new RoomTransition(currentRoom, doorOffset);
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
