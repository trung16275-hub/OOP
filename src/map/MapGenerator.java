package map;

import model.Enemy;
import model.boss.Boss1;
import model.boss.Boss2;
import model.boss.Boss3;
import screens.lighting.*;

import java.awt.Point;
import java.util.*;
import java.awt.Color;

/**
 * Bộ tạo bản đồ ngẫu nhiên cho hầm ngục.
 * Tạo ra chuỗi phòng liên kết trên lưới 9x9, xác định phòng Boss, Shop và đặt cửa đối xứng.
 */
public class MapGenerator {
    private final Random rand;
    private final long seed;

    public MapGenerator() {
        this(System.currentTimeMillis());
    }

    public MapGenerator(long seed) {
        this.seed = seed;
        this.rand = new Random(seed);
    }

    public WorldMap generateFloor(int floorLevel) {
        return generateFloor(floorLevel, 4);
    }

    /**
     * Sinh tầng cố định: 1 phòng spawn ở giữa và 3 phòng boss kề bên.
     */
    public WorldMap generateFloor(int floorLevel, int targetRooms) {
        WorldMap map = new WorldMap(floorLevel);
        int spawnX = 3;
        int spawnY = 3;

        // Phòng spawn ở giữa.
        Room spawnRoom = new Room(Room.DEFAULT_WIDTH, Room.DEFAULT_HEIGHT, Room.RoomType.SPAWN);
        map.addRoom(spawnX, spawnY, spawnRoom);

        // 3 phòng boss kề trực tiếp với spawn: Bắc, Nam, Đông.
        map.addRoom(spawnX, spawnY - 1,
                new Room(Room.DEFAULT_WIDTH, Room.DEFAULT_HEIGHT, Room.RoomType.BOSS));
        map.addRoom(spawnX, spawnY + 1,
                new Room(Room.DEFAULT_WIDTH, Room.DEFAULT_HEIGHT, Room.RoomType.BOSS));
        map.addRoom(spawnX + 1, spawnY,
                new Room(Room.DEFAULT_WIDTH, Room.DEFAULT_HEIGHT, Room.RoomType.BOSS));

        carveDoors(map);
        populateEnemies(map, floorLevel);

        // them den
        populateLights(map);

        return map;
    }

    /**
     * Đục cửa kết nối giữa tất cả các phòng kề nhau trong lưới.
     */
    private void carveDoors(WorldMap map) {
        for (Room r : map.getRooms()) {
            for (Direction dir : Direction.values()) {
                int nx = r.getGridX() + dir.getDx();
                int ny = r.getGridY() + dir.getDy();

                if (map.hasRoom(nx, ny)) {
                    // Determine door position using the seed if not already set
                    if (!r.getDoors().containsKey(dir)) {
                        int offset;
                        if (dir == Direction.NORTH || dir == Direction.SOUTH) {
                            int minX = r.getPadX() + 3;
                            int maxX = r.getWidth() - r.getPadX() - 4;
                            offset = minX + rand.nextInt(Math.max(1, maxX - minX + 1));
                        } else {
                            int minY = r.getPadY() + 5;
                            int maxY = r.getHeight() - r.getPadY() - 4;
                            offset = minY + rand.nextInt(Math.max(1, maxY - minY + 1));
                        }
                        r.setDoorOffset(dir, offset);
                    }
                    r.carveDoor(dir, r.isCleared());
                }
            }
        }
    }

    /**
     * Sinh kẻ thù và Boss vào các phòng.
     */
    private void populateEnemies(WorldMap map, int floorLevel) {
        int midPxX = (Room.DEFAULT_WIDTH / 2) * Room.TILE_SIZE;
        int midPxY = (Room.DEFAULT_HEIGHT / 2) * Room.TILE_SIZE;

        for (Room r : map.getRooms()) {
            if (r.getRoomType() == Room.RoomType.BOSS) {
                Enemy boss;

                if (r.getGridY() == 2) {
                    boss = new Boss1(); // Phòng Bắc
                } else if (r.getGridX() == 4) {
                    boss = new Boss2(); // Phòng Đông
                } else {
                    boss = new Boss3(); // Phòng Nam
                }

                boss.setX(midPxX - boss.getWidth() / 2.0);
                boss.setY(midPxY - boss.getHeight() / 2.0);
                r.addEnemy(boss);
                r.setCleared(false);
            } else {
                // Spawn room và các phòng còn lại không có quái
                r.setCleared(true);
            }
        }
    }

    public long getSeed() {
        return seed;
    }
}
