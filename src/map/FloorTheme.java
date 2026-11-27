package map;

import java.awt.*;

public enum FloorTheme {
    LEVEL_0_OFFICE(1, "The Lobby", "/floor1/tiles/", new Color(45, 42, 24, 150)),
    LEVEL_2_PIPEDREAM(2, "Pipe Dreams", "/floor2/tiles/", new Color(28, 16, 12, 150)),
    LEVEL_52_SCHOOL(3, "School Halls", "/floor3/tiles/", new Color(5, 5, 10, 190));

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
