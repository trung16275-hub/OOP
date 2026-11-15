package map;

/**
 * 4 hướng chính trong bản đồ hầm ngục và lưới phòng.
 */
public enum Direction {
    NORTH(0, -1),
    SOUTH(0, 1),
    WEST(-1, 0),
    EAST(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    /**
     * Hướng đối diện (dùng khi bước qua cửa và xuất hiện ở cửa đối diện của phòng mới).
     */
    public Direction opposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
        };
    }
}
