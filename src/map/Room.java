package map;

public class Room {
    public enum RoomType {
        SPAWN,
        SHOP,
        BOSS,
        NORMAL;
    }
    private int width;
    private int height;
    private RoomType roomType;

    public Room(int width, int height) {
        this.width = width;
        this.height = height;
        this.roomType = RoomType.NORMAL;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }
}
