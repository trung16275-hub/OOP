package map;

import model.Enemy;
import java.awt.Point;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Room {

    public static final int TILE_SIZE = 32;
    public static final int DEFAULT_WIDTH = 30;
    public static final int DEFAULT_HEIGHT = 20;

    public enum RoomType {
        SPAWN,
        SHOP,
        BOSS,
        NORMAL;
    }

    private final int width;
    private final int height;
    private int gridX;
    private int gridY;
    private RoomType roomType;
    private final TileType[][] tiles;

    private boolean cleared;
    private boolean visited;

    private final List<Enemy> enemies = new ArrayList<>();
    private final Map<Direction, Boolean> doors = new EnumMap<>(Direction.class);

    // Constructor mặc định
    public Room() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT, RoomType.NORMAL);
    }

    public Room(int width, int height, RoomType type) {
        this.width = width;
        this.height = height;
        this.roomType = type;
        this.cleared = false;
        this.tiles = new TileType[width][height];

        generateDefaultRoom();
    }

    public void generateDefaultRoom() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x==0||y==0||x==(width-1)||y==(height-1)) tiles[x][y] = TileType.WALL;
                else tiles[x][y] = TileType.FLOOR;
            }
        }
    }

    public void carveDoor(Direction dir, boolean open) {
        TileType doorTile = open ? TileType.DOOR_OPEN : TileType.DOOR_CLOSED;
        doors.put(dir, open);

        switch (dir) {
            case NORTH -> {
                int midX = width / 2;
                tiles[midX - 1][0] = doorTile;
                tiles[midX][0] = doorTile;
            }
            case SOUTH -> {
                int midX = width / 2;
                tiles[midX - 1][height - 1] = doorTile;
                tiles[midX][height - 1] = doorTile;
            }
            case WEST -> {
                int midY = height / 2;
                tiles[0][midY - 1] = doorTile;
                tiles[0][midY] = doorTile;
            }
            case EAST -> {
                int midY = height / 2;
                tiles[width - 1][midY - 1] = doorTile;
                tiles[width - 1][midY] = doorTile;
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
        int midX = (width / 2) * TILE_SIZE;
        int midY = (height / 2) * TILE_SIZE;

        // Cửa Bắc
        if (doors.getOrDefault(Direction.NORTH, false)) {
            if (py <= TILE_SIZE && px + pw > midX - TILE_SIZE && px < midX + TILE_SIZE) {
                return Direction.NORTH;
            }
        }
        // Cửa Nam
        if (doors.getOrDefault(Direction.SOUTH, false)) {
            if (py + ph >= (height - 1) * TILE_SIZE && px + pw > midX - TILE_SIZE && px < midX + TILE_SIZE) {
                return Direction.SOUTH;
            }
        }
        // Cửa Tây
        if (doors.getOrDefault(Direction.WEST, false)) {
            if (px <= TILE_SIZE && py + ph > midY - TILE_SIZE && py < midY + TILE_SIZE) {
                return Direction.WEST;
            }
        }
        // Cửa Đông
        if (doors.getOrDefault(Direction.EAST, false)) {
            if (px + pw >= (width - 1) * TILE_SIZE && py + ph > midY - TILE_SIZE && py < midY + TILE_SIZE) {
                return Direction.EAST;
            }
        }

        return null;
    }

    /**
     * Vị trí xuất hiện an toàn của Player khi bước từ phòng khác vào (ở cửa đối diện).
     */
    public Point getDoorSpawnPosition(Direction fromDir) {
        int midX = (width / 2) * TILE_SIZE;
        int midY = (height / 2) * TILE_SIZE;

        return switch (fromDir) {
            case NORTH -> new Point(midX - 25, (height - 3) * TILE_SIZE); // Đi từ Bắc xuống -> xuất hiện ở cửa Nam
            case SOUTH -> new Point(midX - 25, 2 * TILE_SIZE);            // Đi từ Nam lên -> xuất hiện ở cửa Bắc
            case WEST -> new Point((width - 3) * TILE_SIZE, midY - 25);   // Đi từ Tây sang -> xuất hiện ở cửa Đông
            case EAST -> new Point(2 * TILE_SIZE, midY - 25);            // Đi từ Đông sang -> xuất hiện ở cửa Tây
        };
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getGridX() { return gridX; }
    public void setGridX(int gridX) { this.gridX = gridX; }

    public int getGridY() { return gridY; }
    public void setGridY(int gridY) { this.gridY = gridY; }

    public boolean isVisited() {return visited;}
    public void setVisited(boolean visited) {this.visited = visited;}

    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }

    public Map<Direction, Boolean> getDoors() { return doors; }
    public boolean hasDoor(Direction dir) { return doors.containsKey(dir); }

    public List<Enemy> getEnemies() { return enemies; }
    public void addEnemy(Enemy enemy) { enemies.add(enemy); }

    public TileType getTile(int x, int y) {
        if (x<0||x>=width||y<0||y>=height) return TileType.WALL;
        return tiles[x][y];
    }

    public void setTile (int x, int y, TileType tile) {
        if (x<0||x>=width||y<0||y>=height) {
            throw new IndexOutOfBoundsException(
                    String.format("Tile coordinates (%d, %d) out of bounds for grid size (%d, %d)", x, y, width, height));
            };
        tiles[x][y] = tile;
    }

    public boolean isCleared () {
        return cleared;
    }

    public void setCleared (boolean cleared) {
        this.cleared = cleared;
    }
}
