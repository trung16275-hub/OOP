package map;

import java.awt.*;

public enum FloorTheme {
    LEVEL_0_OFFICE(1, "The Lobby", "/tiles/", new Color(45, 42, 24)),
    LEVEL_2_PIPEDREAM(2, "Pipe Dreams", "tiles", new Color(28, 16, 12)),
    LEVEL_52_SCHOOL(3, "School Halls", new Color(18, 26, 36));

    private final int floorLevel;
    private final String floorName;
    private final String assetsPath;
    private final Color ambientDarkness;

    FloorTheme(int floorLevel, String floorName, String assetsPath, Color ambientDarkness) {
        this.floorLevel = floorLevel;
        this.floorName = floorName;
        this.assetsPath = assetsPath;
        this.ambientDarkness = ambientDarkness;
    }

    public int getFloorLevel() { return floorLevel; }
    public String getFloorName() { return floorName; }
    public String getAssetsPath() { return assetsPath; }
    public Color getAmbientDarkness() { return ambientDarkness; }

}
