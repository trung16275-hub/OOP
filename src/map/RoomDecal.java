package map;

public record RoomDecal(
        String spritePath,
        int x,
        int y,
        int width,
        int height,
        int rotationSteps,
        boolean flipX,
        float alpha,
        String noteId
) {
    public RoomDecal(String spritePath, int x, int y, int width, int height,
                     int rotationSteps, boolean flipX, float alpha) {
        this(spritePath, x, y, width, height, rotationSteps, flipX, alpha, null);
    }

    public RoomDecal(String spritePath, int x, int y, int width, int height) {
        this(spritePath, x, y, width, height, 0, false, 1.0f);
    }
}
