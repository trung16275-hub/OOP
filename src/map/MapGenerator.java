package map;

import java.util.Random;

public class MapGenerator {
    private final Random rand;
    private final long seed;

    public MapGenerator() {
        this.seed = System.currentTimeMillis();
        this.rand = new Random();
    }

    public MapGenerator(long seed) {
        this.seed = seed;
        this.rand = new Random(seed);
    }


}
