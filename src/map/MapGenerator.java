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

    public Room createRoom(Roomtype type, int width, int height) {

    }

    public WorldMap generate() {

    }


}
