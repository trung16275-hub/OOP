package map;

public enum TileType {
    WALL(true),
    FLOOR(false),
    DOOR_CLOSED(false),
    DOOR_OPEN(false),
    PORTAL(false);

    private final boolean solid;

    TileType(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid () {
        return solid;
    }


}
