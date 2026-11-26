package map;

import model.Enemy;
import screens.lighting.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Room {

    public static final int TILE_SIZE = 32;
    public static final int DEFAULT_WIDTH = 32;
    public static final int DEFAULT_HEIGHT = 18;

    public enum RoomType {
        SPAWN,
        SHOP,
        BOSS,
        NORMAL;
    }

    public enum RoomSize {
        SMALL(20, 14),
        SCREEN(32,18),
        BIG(48,28);

        public final int width;
        public final int height;
        RoomSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    private RoomSize roomSize;
    private final int width;
    private final int height;
    private int gridX;
    private int gridY;
    private final int innerWidth;
    private final int innerHeight;
    private final int padX;
    private final int padY;

    private RoomType roomType;
    private final TileType[][] tiles;

    private boolean cleared;
    private boolean visited;

    private final List<Enemy> enemies = new ArrayList<>();
    private final Map<Direction, Boolean> doors = new EnumMap<>(Direction.class);
    // Stores tile coordinates: X for NORTH/SOUTH, Y for WEST/EAST.
    private final Map<Direction, List<Integer>> doorOffsets = new EnumMap<>(Direction.class);

    private final List<LightSource> lights = new ArrayList<>();

    public record DoorTrigger(Direction direction, int offset) { }

    // Constructor mặc định
    public Room() {
        this(RoomSize.SCREEN, RoomType.NORMAL);
    }

    public Room(RoomSize roomSize, RoomType type) {
        this.roomSize = roomSize;
        this.innerWidth = roomSize.width;
        this.innerHeight = roomSize.height;

        // Force the tile grid to be at least screen size (32x18)
        this.width = Math.max(innerWidth, RoomSize.SCREEN.width);
        this.height = Math.max(innerHeight, RoomSize.SCREEN.height);
        this.padX = (this.width - innerWidth) / 2;
        this.padY = (this.height - innerHeight) / 2;

        this.roomType = type;
        this.cleared = false;
        this.tiles = new TileType[width][height];

        generateDefaultRoom();
    }

    public Room(int width, int height, RoomType type) {
        this(resolveRoomSize(width, height), type);
    }

    private static RoomSize resolveRoomSize(int width, int height) {
        if (width == RoomSize.SMALL.width && height == RoomSize.SMALL.height) {
            return RoomSize.SMALL;
        }
        if (width == RoomSize.BIG.width && height == RoomSize.BIG.height) {
            return RoomSize.BIG;
        }
        return RoomSize.SCREEN;
    }

    public void generateDefaultRoom() {
        int floorMinX = padX + 2;
        int floorMaxX = width - padX - 3;
        int floorMinY = padY + 4;
        int floorMaxY = height - padY - 3;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x >= floorMinX && x <= floorMaxX && y >= floorMinY && y <= floorMaxY) {
                    tiles[x][y] = TileType.FLOOR;
                } else {
                    tiles[x][y] = TileType.WALL;
                }
            }
        }
    }

    public void setDoorOffset(Direction dir, int offset) {
        List<Integer> offsets = new ArrayList<>();
        offsets.add(offset);
        doorOffsets.put(dir, offsets);
    }

    public void addDoorOffset(Direction dir, int offset) {
        List<Integer> offsets = doorOffsets.computeIfAbsent(dir, key -> new ArrayList<>());
        if (!offsets.contains(offset)) {
            offsets.add(offset);
        }
    }

    public int getDoorOffset(Direction dir) {
        List<Integer> offsets = doorOffsets.get(dir);
        if (offsets == null || offsets.isEmpty()) {
            return (dir == Direction.NORTH || dir == Direction.SOUTH) ? width / 2 : height / 2;
        }
        return offsets.get(0);
    }

    public List<Integer> getDoorOffsets(Direction dir) {
        List<Integer> offsets = doorOffsets.get(dir);
        if (offsets == null || offsets.isEmpty()) {
            return List.of((dir == Direction.NORTH || dir == Direction.SOUTH) ? width / 2 : height / 2);
        }
        return Collections.unmodifiableList(offsets);
    }

    public void carveDoor(Direction dir, boolean open) {
        TileType doorTile = open ? TileType.DOOR_OPEN : TileType.DOOR_CLOSED;
        doors.put(dir, open);
        for (int offset : getDoorOffsets(dir)) {
            carveDoorAtOffset(dir, offset, doorTile);
        }
    }

    private void carveDoorAtOffset(Direction dir, int offset, TileType doorTile) {
        switch (dir) {
            case NORTH -> {
                for (int x = offset - 1; x <= offset + 1; x++) {
                    tiles[x][0] = doorTile; // Door at top screen border
                    for (int y = 1; y <= padY + 3; y++) {
                        tiles[x][y] = TileType.FLOOR; // Hallway to inner room
                    }
                }
            }
            case SOUTH -> {
                for (int x = offset - 1; x <= offset + 1; x++) {
                    for (int y = height - padY - 2; y < height - 1; y++) {
                        tiles[x][y] = TileType.FLOOR; // Hallway to inner room
                    }
                    tiles[x][height - 1] = doorTile; // Door at bottom screen border
                }
            }
            case WEST -> {
                for (int y = offset - 1; y <= offset + 1; y++) {
                    tiles[0][y] = doorTile; // Door at left screen border
                    for (int x = 1; x <= padX + 1; x++) {
                        tiles[x][y] = TileType.FLOOR; // Hallway to inner room
                    }
                }
            }
            case EAST -> {
                for (int y = offset - 1; y <= offset + 1; y++) {
                    for (int x = width - padX - 2; x < width - 1; x++) {
                        tiles[x][y] = TileType.FLOOR; // Hallway to inner room
                    }
                    tiles[width - 1][y] = doorTile; // Door at right screen border
                }
            }
        }
    }

    public void openDoors() {
        for (Direction dir : doors.keySet()) {
            carveDoor(dir, true);
        }
    }

    public void closeDoors() {
        for (Direction dir : doors.keySet()) {
            carveDoor(dir, false);
        }
    }

    public Direction checkDoorTrigger(double px, double py, int pw, int ph) {
        DoorTrigger trigger = checkDoorTriggerDetails(px, py, pw, ph);
        return trigger == null ? null : trigger.direction();
    }

    public DoorTrigger checkDoorTriggerDetails(double px, double py, int pw, int ph) {
        // North Door
        if (doors.getOrDefault(Direction.NORTH, false)) {
            for (int offset : getDoorOffsets(Direction.NORTH)) {
                int doorPxX = offset * TILE_SIZE;
                if (py <= 0.5 * TILE_SIZE
                    && Math.abs(px + pw / 2.0 - doorPxX) <= TILE_SIZE * 1.5) {
                    return new DoorTrigger(Direction.NORTH, offset);
                }
            }
        }
        // South Door
        if (doors.getOrDefault(Direction.SOUTH, false)) {
            for (int offset : getDoorOffsets(Direction.SOUTH)) {
                int doorPxX = offset * TILE_SIZE;
                if (py + ph >= (height - 0.5) * TILE_SIZE
                    && Math.abs(px + pw / 2.0 - doorPxX) <= TILE_SIZE * 1.5) {
                    return new DoorTrigger(Direction.SOUTH, offset);
                }
            }
        }
        // West Door
        if (doors.getOrDefault(Direction.WEST, false)) {
            for (int offset : getDoorOffsets(Direction.WEST)) {
                int doorPxY = offset * TILE_SIZE;
                if (px <= 0.5 * TILE_SIZE
                    && Math.abs(py + ph / 2.0 - doorPxY) <= TILE_SIZE * 1.5) {
                    return new DoorTrigger(Direction.WEST, offset);
                }
            }
        }
        // East Door
        if (doors.getOrDefault(Direction.EAST, false)) {
            for (int offset : getDoorOffsets(Direction.EAST)) {
                int doorPxY = offset * TILE_SIZE;
                if (px + pw >= (width - 0.5) * TILE_SIZE
                    && Math.abs(py + ph / 2.0 - doorPxY) <= TILE_SIZE * 1.5) {
                    return new DoorTrigger(Direction.EAST, offset);
                }
            }
        }

        return null;
    }

    /**
     * Vị trí xuất hiện an toàn của Player khi bước từ phòng khác vào (ở cửa đối diện).
     */
    public Point getDoorSpawnPosition(Direction fromDir) {
        return getDoorSpawnPosition(fromDir, getDoorOffset(fromDir.opposite()));
    }

    public Point getDoorSpawnPosition(Direction fromDir, int offset) {
        return switch (fromDir) {
            // Came from North -> spawn at South entrance on connecting floor tile (height - 2)
            case NORTH -> new Point(offset * TILE_SIZE - 30, (height - 3) * TILE_SIZE);
            // Came from South -> spawn at North entrance on connecting floor tile (row 1)
            case SOUTH -> new Point(offset * TILE_SIZE - 30, 1 * TILE_SIZE + 25);
            // Came from West -> spawn at East entrance on connecting floor tile (width - 2)
            case WEST -> new Point((width - 2) * TILE_SIZE - 30, offset * TILE_SIZE - 30);
            // Came from East -> spawn at West entrance on connecting floor tile (column 1)
            case EAST -> new Point(1 * TILE_SIZE + 16, offset * TILE_SIZE - 30);
        };
    }
    public boolean checkCollision(Rectangle bounds) {
        int minTileX = Math.max(0, bounds.x / TILE_SIZE);
        int maxTileX = Math.min(width - 1, (bounds.x + bounds.width - 1) / TILE_SIZE);
        int minTileY = Math.max(0, bounds.y / TILE_SIZE);
        int maxTileY = Math.min(height - 1, (bounds.y + bounds.height - 1) / TILE_SIZE);

        for (int tx = minTileX; tx <= maxTileX; tx++) {
            for (int ty = minTileY; ty <= maxTileY; ty++) {
                if(isTileSolid(tx,ty)) {
                    Rectangle tileRec = new Rectangle(tx * TILE_SIZE, ty * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    if (bounds.intersects(tileRec)) return true;
                }
            }
        }

        return false;
    }

    public void carveOpenPath(Direction dir) {
        doors.put(dir, true);
        for (int offset : getDoorOffsets(dir)) {
            carveOpenPathAtOffset(dir, offset);
        }
    }

    private void carveOpenPathAtOffset(Direction dir, int offset) {
        switch (dir) {
            case NORTH -> {
                for (int x = offset - 1; x <= offset + 1; x++) {
                    tiles[x][0] = TileType.FLOOR;
                    for (int y = 1; y <= padY + 3; y++) {
                        tiles[x][y] = TileType.FLOOR;
                    }
                }
            }
            case SOUTH -> {
                for (int x = offset - 1; x <= offset + 1; x++) {
                    for (int y = height - padY - 2; y < height - 1; y++) {
                        tiles[x][y] = TileType.FLOOR;
                    }
                    tiles[x][height - 1] = TileType.FLOOR;
                }
            }
            case WEST -> {
                for (int y = offset - 1; y <= offset + 1; y++) {
                    tiles[0][y] = TileType.FLOOR;
                    for (int x = 1; x <= padX + 1; x++) {
                        tiles[x][y] = TileType.FLOOR;
                    }
                }
            }
            case EAST -> {
                for (int y = offset - 1; y <= offset + 1; y++) {
                    for (int x = width - padX - 2; x < width - 1; x++) {
                        tiles[x][y] = TileType.FLOOR;
                    }
                    tiles[width - 1][y] = TileType.FLOOR;
                }
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getGridX() { return gridX; }
    public void setGridX(int gridX) { this.gridX = gridX; }

    public int getGridY() { return gridY; }
    public void setGridY(int gridY) { this.gridY = gridY; }

    public int getPadX() { return padX; }
    public int getPadY() { return padY; }

    public boolean isVisited() {return visited;}
    public void setVisited(boolean visited) {this.visited = visited;}

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Map<Direction, Boolean> getDoors() { return doors; }
    public boolean hasDoor(Direction dir) { return doors.containsKey(dir); }

    public List<Enemy> getEnemies() { return enemies; }
    public void addEnemy(Enemy enemy) { enemies.add(enemy); }

    public List<LightSource> getLights() { return lights; }
    public void addLight(LightSource light) { lights.add(light); }

    public TileType getTile(int x, int y) {
        if (x<0||x>=width||y<0||y>=height) return TileType.WALL;
        return tiles[x][y];
    }

    public void setTile (int x, int y, TileType tile) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException(
                    String.format("Tile coordinates (%d, %d) out of bounds for grid size (%d, %d)", x, y, width, height));
            };
        tiles[x][y] = tile;
    }

    public boolean isTileSolid (int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return true;
        }
        return tiles[x][y].isSolid();
    }

    public boolean isCleared () {
        return cleared;
    }

    public void setCleared (boolean cleared) {
        this.cleared = cleared;
    }

}
