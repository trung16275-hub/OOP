package map;

import java.util.ArrayList;
import java.util.List;

public class Room {

    public static final int TILE_SIZE = 32;

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

    private boolean cleared;

    //default constructor for fast testing
    public Room() {
        this.width = 30;
        this.height = 20;
        this.roomType = RoomType.NORMAL;
        this.cleared = true;
        this.tiles = new TileType[30][20];

        generateDefaultRoom();
    }

    public Room(int width, int height, RoomType type) {
        this.width = width;
        this.height = height;
        this.roomType = type;
        this.cleared = false;
        this.tiles = new TileType[width][height];
    }

    public void generateDefaultRoom() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x==0||y==0||x==(width-1)||y==(height-1)) tiles[x][y] = TileType.WALL;
                else tiles[x][y] = TileType.FLOOR;
            }
        }
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
