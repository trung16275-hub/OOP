package map;

import java.util.Random;

public class MapGenerator {
    private final Random rand;
    private final long seed;

    public MapGenerator() {
        this.seed = System.currentTimeMillis();
        this.rand = new Random(seed);
    }

    public MapGenerator(long seed) {
        this.seed = seed;
        this.rand = new Random(seed);
    }

    public Room createRoom(Room.RoomType type, int width, int height) {
        //fix sau
        Room room = new Room();
        room.generateDefaultRoom();
        return room;
    }

    public WorldMap generate() {
        //fix sau
        WorldMap map = new WorldMap(1);
        map.addRoom(4,4, createRoom(Room.RoomType.NORMAL,30,20));

        return map;
    }


}
