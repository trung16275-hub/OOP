package map;

import model.Enemy;
import model.prop.*;
import screens.lighting.*;

import java.awt.*;
import java.util.*;
import java.util.List;

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
        SCREEN(32, 18),
        BIG(48, 28);

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
    private List<Prop> props = new ArrayList<>();
    private final List<RoomDecal> floorDecals = new ArrayList<>();
    private final List<RoomDecal> wallDecals = new ArrayList<>();


    private boolean cleared;
    private boolean visited;
    private boolean bossLocked;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<AmmoPickup> ammoPickups = new ArrayList<>();
    private LiquidPain liquidPain;
    private final Map<Direction, Boolean> doors = new EnumMap<>(Direction.class);
    private final Set<Direction> lockedDoors = EnumSet.noneOf(Direction.class);

    // Stores the tile coordinate: X coordinate for NORTH/SOUTH, Y coordinate for WEST/EAST
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
        this.cleared = roomType == RoomType.SPAWN;
        this.bossLocked = roomType == RoomType.BOSS;

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
            if (!lockedDoors.contains(dir)) {
                carveDoor(dir, true);
            }
        }
    }

    public void closeDoors() {
        for (Direction dir : doors.keySet()) {
            carveDoor(dir, false);
        }
    }



    public Direction checkDoorTrigger(double px, double py, int pw, int ph) {
        return checkDoorTrigger(new Rectangle((int) Math.round(px), (int) Math.round(py), pw, ph));
    }

    public Direction checkDoorTrigger(Rectangle bounds) {
        DoorTrigger trigger = checkDoorTriggerDetails(bounds);
        return trigger == null ? null : trigger.direction();
    }

    public DoorTrigger checkDoorTriggerDetails(double px, double py, int pw, int ph) {
        return checkDoorTriggerDetails(new Rectangle((int) Math.round(px), (int) Math.round(py), pw, ph));
    }

    public DoorTrigger checkDoorTriggerDetails(Rectangle bounds) {
        for (Direction direction : Direction.values()) {
            if (!doors.getOrDefault(direction, false)) {
                continue;
            }
            boolean nearEdge = switch (direction) {
                case NORTH -> bounds.y <= 0.6 * TILE_SIZE;
                case SOUTH -> bounds.getMaxY() >= (height - 0.6) * TILE_SIZE;
                case WEST -> bounds.x <= 0.6 * TILE_SIZE;
                case EAST -> bounds.getMaxX() >= (width - 0.6) * TILE_SIZE;
            };
            if (!nearEdge) {
                continue;
            }
            boolean horizontal = direction == Direction.NORTH || direction == Direction.SOUTH;
            double start = horizontal ? bounds.getMinX() : bounds.getMinY();
            double end = horizontal ? bounds.getMaxX() : bounds.getMaxY();
            for (int offset : getDoorOffsets(direction)) {
                int door = offset * TILE_SIZE;
                if (end > door - TILE_SIZE && start < door + 2 * TILE_SIZE) {
                    return new DoorTrigger(direction, offset);
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
            // Came from North -> spawn at South entrance inside hallway
            case NORTH -> {
                int southDoorX = getDoorOffset(Direction.SOUTH) * TILE_SIZE;
                yield new Point(southDoorX - 14, (height - 3) * TILE_SIZE - 12);
            }
            // Came from South -> spawn at North entrance inside hallway
            case SOUTH -> {
                int northDoorX = getDoorOffset(Direction.NORTH) * TILE_SIZE;
                yield new Point(northDoorX - 14, TILE_SIZE + 8);
            }
            // Came from West -> spawn at East entrance inside hallway
            case WEST -> {
                int eastDoorY = getDoorOffset(Direction.EAST) * TILE_SIZE;
                yield new Point((width - 3) * TILE_SIZE + 4, eastDoorY - 25);
            }
            // Came from East -> spawn at West entrance inside hallway
            case EAST -> {
                int westDoorY = getDoorOffset(Direction.WEST) * TILE_SIZE;
                yield new Point(TILE_SIZE + 4, westDoorY - 25);
            }

        };
    }
    public Point getCenterSpawnPosition(int entityWidth, int entityHeight) {
        int cx = (width * TILE_SIZE - entityWidth) / 2;
        int cy = ((padY + 4 + (height - padY - 3)) * TILE_SIZE / 2) - entityHeight / 2;
        return new Point(cx, cy);
    }

    public boolean isInDoorCorridor(int tx, int ty) {
        int floorMinX = padX + 2;
        int floorMaxX = width - padX - 3;
        int floorMinY = padY + 4;
        int floorMaxY = height - padY - 3;

        // Keep 2-tile walkable buffer around the entire inner room perimeter
        if (tx < floorMinX + 2 || tx > floorMaxX - 2 || ty < floorMinY + 2 || ty > floorMaxY - 2) {
            return true;
        }
        return false;
    }

    public void addWallDecal(RoomDecal decal) {
        if (decal != null) {
            wallDecals.add(decal);
        }
    }


    public boolean checkCollision(Rectangle bounds) {
        int minTileX = Math.max(0, bounds.x / TILE_SIZE);
        int maxTileX = Math.min(width - 1, (bounds.x + bounds.width - 1) / TILE_SIZE);
        int minTileY = Math.max(0, bounds.y / TILE_SIZE);
        int maxTileY = Math.min(height - 1, (bounds.y + bounds.height - 1) / TILE_SIZE);

        for (int tx = minTileX; tx <= maxTileX; tx++) {
            for (int ty = minTileY; ty <= maxTileY; ty++) {
                if (isTileSolid(tx, ty)) {
                    Rectangle tileRec = new Rectangle(tx * TILE_SIZE, ty * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                    if (bounds.intersects(tileRec)) return true;
                }
            }
        }

        for (Prop prop : props) {
            if(prop.solid() && bounds.intersects(prop.getBounds())) {
                return true;
            }
        }

        return false;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public int getGridX() {
        return gridX;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public int getPadX() {
        return padX;
    }

    public int getPadY() {
        return padY;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Map<Direction, Boolean> getDoors() {
        return doors;
    }

    public boolean hasDoor(Direction dir) {
        return doors.containsKey(dir);
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public void addEnemy(Enemy enemy) {
        enemies.add(enemy);
    }

    public List<Prop> getProps() {
        return props;
    }

    public void addProp(Prop prop) {
        props.add(prop);
    }

    public RoomSize getRoomSize() {
        return roomSize;
    }

    public void addFloorDecal(RoomDecal decal) {
        if (decal != null) {
            floorDecals.add(decal);
        }
    }

    public List<RoomDecal> getWallDecals() {
        return wallDecals;
    }

    public List<RoomDecal> getFloorDecals() {
        return floorDecals;
    }


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

    public String findNearbyEnemyNote(Rectangle playerBounds) {
        String noteId = null;
        double nearestDistance = 2 * TILE_SIZE;
        for (RoomDecal decal : wallDecals) {
            if (decal.noteId() == null) {
                continue;
            }
            Rectangle bounds = new Rectangle(decal.x(), decal.y(), decal.width(), decal.height());
            double distance = pickupDistance(playerBounds, bounds);
            if (distance <= nearestDistance) {
                nearestDistance = distance;
                noteId = decal.noteId();
            }
        }
        return noteId;
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
        if (x < 0 || x >= width || y < 0 || y >= height) return TileType.WALL;
        return tiles[x][y];
    }

    public void setTile(int x, int y, TileType tile) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IndexOutOfBoundsException(
                    String.format("Tile coordinates (%d, %d) out of bounds for grid size (%d, %d)", x, y, width, height));
        }
        ;
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

    public void setCleared(boolean cleared) {
        this.cleared = cleared;
    }

    public void spawnPortal() {
        int midX = width / 2;
        int midY = height / 2;
        tiles[midX][midY] = TileType.PORTAL;
        tiles[midX][midY - 1] = TileType.PORTAL;
        tiles[midX - 1][midY] = TileType.PORTAL;
        tiles[midX - 1][midY - 1] = TileType.PORTAL;
    }

    public void lockDoor(Direction dir) {
        lockedDoors.add(dir);
        if (doors.containsKey(dir)) {
            carveDoor(dir, false);
        }
    }

    public void unlockDoor(Direction dir) {
        lockedDoors.remove(dir);
    }

}
