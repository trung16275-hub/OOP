package map;

import java.util.ArrayList;
import java.util.List;

public class Map {
    private static final int GRID_SIZE = 9;

    private final int floorLevel;
    private final List<Room> rooms;

    public DungeonMap(int floorLevel) {
        this.floorLevel = floorLevel;
        this.rooms = new ArrayList<>();
        
    }

    public int getFloorLevel() {
        return floorLevel;
    }


}
