package map;

public class Room {

    public static final int TYLE_SIZE = 32;

    public enum RoomType {
        SPAWN,
        SHOP,
        BOSS,
        NORMAL;
    }
    private int width;
    private int height;
    private RoomType roomType;
    private final TileType[][] tiles;

    public Room(int width, int height) {
        this.width = width;
        this.height = height;
        this.roomType = RoomType.NORMAL;
        this.tiles = new TileType[width][height];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public TileType getTile(int x, int y) {
        if (x<0||x>width||y<0||y>height) return TileType.WALL;
        return tiles[x][y];
    }
}
