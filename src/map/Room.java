package map;

import model.Enemy;

import java.awt.*;
import java.util.ArrayList;
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
    private RoomType roomType;
    private final TileType[][] tiles;

    private boolean cleared;
    private boolean visited;

    private final List<Enemy> enemies = new ArrayList<>();
    private final Map<Direction, Boolean> doors = new EnumMap<>(Direction.class);
    // Stores the tile coordinate: X coordinate for NORTH/SOUTH, Y coordinate for WEST/EAST
    private final Map<Direction, Integer> doorOffsets = new EnumMap<>(Direction.class);


    // Constructor mặc định
    public Room() {
        this(RoomSize.SCREEN, RoomType.NORMAL);
    }

    public Room(RoomSize roomSize, RoomType type) {
        this.roomSize = roomSize;
        this.width = roomSize.width;
        this.height = roomSize.height;
        this.roomType = type;
        this.cleared = false;
        this.tiles = new TileType[width][height];

        generateDefaultRoom();
    }

    public void generateDefaultRoom() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x==0||y==0||x==(width-1)||y==(height-1)
                        ||x==1||y<=3||x==(width-2)||y==(height-2)) tiles[x][y] = TileType.WALL;
                else tiles[x][y] = TileType.FLOOR;
            }
        }
    }

    public void setDoorOffset(Direction dir, int offset) {
        doorOffsets.put(dir, offset);
    }

    public int getDoorOffset(Direction dir) {
        // Fallback to center if not set
        if (!doorOffsets.containsKey(dir)) {
            return (dir == Direction.NORTH || dir == Direction.SOUTH) ? width / 2 : height / 2;
        }
        return doorOffsets.get(dir);
    }

    public void carveDoor(Direction dir, boolean open) {
        TileType doorTile = open ? TileType.DOOR_OPEN : TileType.DOOR_CLOSED;
        doors.put(dir, open);
        int offset = getDoorOffset(dir);

        switch (dir) {
            case NORTH -> {
                for (int x = offset - 1; x <= offset; x++) {
                    for (int y = 0; y <= 3; y++) {
                        tiles[x][y] = doorTile;
                    }
                }
            }
            case SOUTH -> {
                for (int x = offset - 1; x <= offset; x++) {
                    for (int y = height - 2; y < height; y++) {
                        tiles[x][y] = doorTile;
                    }
                }
            }
            case WEST -> {
                for (int x = 0; x <= 1; x++) {
                    for (int y = offset - 1; y <= offset + 1; y++) {
                        tiles[x][y] = doorTile;
                    }
                }
            }
            case EAST -> {
                for (int x = width - 2; x < width; x++) {
                    for (int y = offset - 1; y <= offset + 1; y++) {
                        tiles[x][y] = doorTile;
                    }
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
        // North Door
        if (doors.getOrDefault(Direction.NORTH, false)) {
            int doorPxX = getDoorOffset(Direction.NORTH) * TILE_SIZE;
            if (py <= 2 * TILE_SIZE && px + pw > doorPxX - TILE_SIZE && px < doorPxX + TILE_SIZE) {
                return Direction.NORTH;
            }
        }
        // South Door
        if (doors.getOrDefault(Direction.SOUTH, false)) {
            int doorPxX = getDoorOffset(Direction.SOUTH) * TILE_SIZE;
            if (py + ph >= (height - 2) * TILE_SIZE && px + pw > doorPxX - TILE_SIZE && px < doorPxX + TILE_SIZE) {
                return Direction.SOUTH;
            }
        }
        // West Door
        if (doors.getOrDefault(Direction.WEST, false)) {
            int doorPxY = getDoorOffset(Direction.WEST) * TILE_SIZE;
            if (px <= 1.5 * TILE_SIZE && py + ph > doorPxY - TILE_SIZE && py < doorPxY + 2 * TILE_SIZE) {
                return Direction.WEST;
            }
        }
        // East Door
        if (doors.getOrDefault(Direction.EAST, false)) {
            int doorPxY = getDoorOffset(Direction.EAST) * TILE_SIZE;
            if (px + pw >= (width - 2) * TILE_SIZE && py + ph > doorPxY - TILE_SIZE && py < doorPxY + 2 * TILE_SIZE) {
                return Direction.EAST;
            }
        }

        return null;
    }

    /**
     * Vị trí xuất hiện an toàn của Player khi bước từ phòng khác vào (ở cửa đối diện).
     */
    public Point getDoorSpawnPosition(Direction fromDir) {
        return switch (fromDir) {
            case NORTH -> {
                int southDoorX = getDoorOffset(Direction.SOUTH) * TILE_SIZE;
                yield new Point(southDoorX - 16, (height - 3) * TILE_SIZE - 10);
            }
            case SOUTH -> {
                int northDoorX = getDoorOffset(Direction.NORTH) * TILE_SIZE;
                yield new Point(northDoorX - 16, 4 * TILE_SIZE);
            }
            case WEST -> {
                int eastDoorY = getDoorOffset(Direction.EAST) * TILE_SIZE;
                yield new Point((width - 3) * TILE_SIZE - 10, eastDoorY);
            }
            case EAST -> {
                int westDoorY = getDoorOffset(Direction.WEST) * TILE_SIZE;
                yield new Point(2 * TILE_SIZE, westDoorY);
            }
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
