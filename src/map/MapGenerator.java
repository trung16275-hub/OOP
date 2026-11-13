package map;

import model.Boss1;
import model.Boss2;
import model.Boss3;
import model.Enemy;

import java.awt.Point;
import java.util.*;

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
        return generateFloor(floorLevel, 7 + rand.nextInt(3)); // 7 - 9 phòng mỗi tầng
    }

    /**
     * Sinh toàn bộ tầng hầm ngục với số lượng phòng mục tiêu.
     */
    public WorldMap generateFloor(int floorLevel, int targetRooms) {
        WorldMap map = new WorldMap(floorLevel);
        int spawnX = 4;
        int spawnY = 4;

        // 1. Khởi tạo phòng SPAWN tại trung tâm (4, 4)
        Room spawnRoom = new Room(Room.DEFAULT_WIDTH, Room.DEFAULT_HEIGHT, Room.RoomType.SPAWN);
        map.addRoom(spawnX, spawnY, spawnRoom);

        List<Point> roomPositions = new ArrayList<>();
        roomPositions.add(new Point(spawnX, spawnY));

        // 2. Thuật toán rẽ nhánh ngẫu nhiên (Random Walk / Branching)
        Direction[] directions = Direction.values();
        while (roomPositions.size() < targetRooms) {
            Point current = roomPositions.get(rand.nextInt(roomPositions.size()));
            Direction dir = directions[rand.nextInt(directions.length)];

            int nx = current.x + dir.getDx();
            int ny = current.y + dir.getDy();

            // Giới hạn trong phạm vi an toàn của lưới 9x9 (từ 1 đến 7)
            if (nx >= 1 && nx <= 7 && ny >= 1 && ny <= 7 && !map.hasRoom(nx, ny)) {
                // Kiểm tra số lượng phòng kề cận để tránh các cụm phòng hình vuông dày đặc
                if (countAdjacentRooms(map, nx, ny) <= 2) {
                    Room newRoom = new Room(Room.DEFAULT_WIDTH, Room.DEFAULT_HEIGHT, Room.RoomType.NORMAL);
                    map.addRoom(nx, ny, newRoom);
                    roomPositions.add(new Point(nx, ny));
                }
            }
        }


        // Đục cửa đối xứng giữa các phòng kề nhau
        carveDoors(map);

        // Sinh quái vật vào các phòng
        populateEnemies(map, floorLevel);

        return map;
    }

    private int countAdjacentRooms(WorldMap map, int gx, int gy) {
        int count = 0;
        for (Direction dir : Direction.values()) {
            if (map.hasRoom(gx + dir.getDx(), gy + dir.getDy())) {
                count++;
            }
        }
        return count;
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
            if (r.getRoomType() == Room.RoomType.NORMAL) {
                // Sinh 1 quái vật trong phòng bình thường
                Boss1 minion = new Boss1();
                minion.setX(midPxX - 50 + rand.nextInt(100));
                minion.setY(midPxY - 50 + rand.nextInt(100));
                r.addEnemy(minion);
                r.setCleared(false);
            } else {
                // SPAWN & SHOP an toàn không có quái
                r.setCleared(true);

            }
        }
    }


    public long getSeed() {
        return seed;
    }
}
