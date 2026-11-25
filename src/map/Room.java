package map;

import model.Enemy;
import model.prop.AmmoPickup;
import model.prop.LiquidPain;
import screens.lighting.*;

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
    private final int innerWidth;
    private final int innerHeight;
    private final int padX;
    private final int padY;

    private RoomType roomType;
    private final TileType[][] tiles;

    private boolean cleared;
    private boolean visited;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<AmmoPickup> ammoPickups = new ArrayList<>();
    private LiquidPain liquidPain;
    private final Map<Direction, Boolean> doors = new EnumMap<>(Direction.class);
    // Stores the tile coordinate: X coordinate for NORTH/SOUTH, Y coordinate for WEST/EAST
    private final Map<Direction, Integer> doorOffsets = new EnumMap<>(Direction.class);

    private final List<LightSource> lights = new ArrayList<>();

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
        // North Door
        if (doors.getOrDefault(Direction.NORTH, false)) {
            int doorPxX = getDoorOffset(Direction.NORTH) * TILE_SIZE;
            if (py <= 0.5 * TILE_SIZE && px + pw > doorPxX - TILE_SIZE && px < doorPxX + TILE_SIZE) {
                return Direction.NORTH;
            }
        }
        // South Door
        if (doors.getOrDefault(Direction.SOUTH, false)) {
            int doorPxX = getDoorOffset(Direction.SOUTH) * TILE_SIZE;
            if (py + ph >= (height - 0.5) * TILE_SIZE && px + pw > doorPxX - TILE_SIZE && px < doorPxX + TILE_SIZE) {
                return Direction.SOUTH;
            }
        }
        // West Door
        if (doors.getOrDefault(Direction.WEST, false)) {
            int doorPxY = getDoorOffset(Direction.WEST) * TILE_SIZE;
            if (px <= 0.5 * TILE_SIZE && py + ph > doorPxY - TILE_SIZE && py < doorPxY + 2 * TILE_SIZE) {
                return Direction.WEST;
            }
        }
        // East Door
        if (doors.getOrDefault(Direction.EAST, false)) {
            int doorPxY = getDoorOffset(Direction.EAST) * TILE_SIZE;
            if (px + pw >= (width - 0.5) * TILE_SIZE && py + ph > doorPxY - TILE_SIZE && py < doorPxY + 2 * TILE_SIZE) {
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
            // Came from North -> spawn at South entrance on connecting floor tile (height - 2)
            case NORTH -> {
                int southDoorX = getDoorOffset(Direction.SOUTH) * TILE_SIZE;
                yield new Point(southDoorX - 16, (height - 3) * TILE_SIZE);
            }
            // Came from South -> spawn at North entrance on connecting floor tile (row 1)
            case SOUTH -> {
                int northDoorX = getDoorOffset(Direction.NORTH) * TILE_SIZE;
                yield new Point(northDoorX - 16, 1 * TILE_SIZE + 25);
            }
            // Came from West -> spawn at East entrance on connecting floor tile (width - 2)
            case WEST -> {
                int eastDoorY = getDoorOffset(Direction.EAST) * TILE_SIZE;
                yield new Point((width - 2) * TILE_SIZE - 30, eastDoorY);
            }
            // Came from East -> spawn at West entrance on connecting floor tile (column 1)
            case EAST -> {
                int westDoorY = getDoorOffset(Direction.WEST) * TILE_SIZE;
                yield new Point(1 * TILE_SIZE + 16, westDoorY);
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

    public List<AmmoPickup> getAmmoPickups() { return ammoPickups; }

    public LiquidPain getLiquidPain() { return liquidPain; }
    public void setLiquidPain(LiquidPain liquidPain) { this.liquidPain = liquidPain; }

    public boolean isLiquidPainNearest(Rectangle playerBounds, int nearestAmmoIndex) {
        if (liquidPain == null) {
            return false;
        }
        double distance = pickupDistance(playerBounds, liquidPain.prop().getBounds());
        return distance <= AmmoPickup.PICKUP_RANGE_PIXELS
                && (nearestAmmoIndex < 0 || distance < pickupDistance(
                        playerBounds, ammoPickups.get(nearestAmmoIndex).prop().getBounds()));
    }

    public int findNearestAmmoPickup(Rectangle playerBounds) {
        int nearest = -1;
        double nearestDistance = AmmoPickup.PICKUP_RANGE_PIXELS;
        for (int i = 0; i < ammoPickups.size(); i++) {
            double distance = pickupDistance(playerBounds, ammoPickups.get(i).prop().getBounds());
            if (distance <= nearestDistance && (nearest == -1 || distance < nearestDistance)) {
                nearestDistance = distance;
                nearest = i;
            }
        }
        return nearest;
    }

    private double pickupDistance(Rectangle playerBounds, Rectangle bounds) {
        double dx = Math.max(0, Math.max(bounds.getMinX() - playerBounds.getMaxX(),
                playerBounds.getMinX() - bounds.getMaxX()));
        double dy = Math.max(0, Math.max(bounds.getMinY() - playerBounds.getMaxY(),
                playerBounds.getMinY() - bounds.getMaxY()));
        return Math.hypot(dx, dy);
    }

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
