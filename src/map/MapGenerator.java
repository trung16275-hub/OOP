package map;

import model.Enemy;
import model.boss.Boss1;
import model.boss.Boss2;
import model.boss.Boss3;
import screens.lighting.LightSource;

import java.awt.Color;

/**
 * Bộ tạo bản đồ cố định cho chế độ test Boss.
 * Sinh 1 phòng spawn ở giữa và 3 phòng boss kề bên.
 */
public class MapGenerator {

    public MapGenerator() {
    }

    public MapGenerator(long seed) {
        // Seed được giữ để tương thích API cũ; map test hiện tại không sử dụng seed.
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
                    // Fixed test layout: keep doors centered.
                    if (!r.getDoors().containsKey(dir)) {
                        int offset = (dir == Direction.NORTH || dir == Direction.SOUTH)
                                ? r.getWidth() / 2
                                : r.getHeight() / 2;
                        r.setDoorOffset(dir, offset);
                    }
                    r.carveOpenPath(dir);
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

    private void populateLights(WorldMap map) {
        for (Room r : map.getRooms()) {
            int innerLeftPx = (r.getPadX() + 2) * Room.TILE_SIZE;
            int innerWidthPx = (r.getWidth() - (r.getPadX() + 2) * 2) * Room.TILE_SIZE;
            int wallY = (r.getPadY() + 2) * Room.TILE_SIZE;

            int tubeCount = (r.getWidth() - r.getPadX() * 2 <= 22) ? 1 : 2;

            for (int i = 0; i < tubeCount; i++) {
                double tubeX = innerLeftPx + innerWidthPx * ((i + 1.0) / (tubeCount + 1.0));
                boolean flickers = false;

                int tileX = (int) tubeX / Room.TILE_SIZE;
                int tileY = wallY / Room.TILE_SIZE;
                if (r.getTile(tileX, tileY) == TileType.FLOOR
                        || r.getTile(tileX - 1, tileY) == TileType.FLOOR
                        || r.getTile(tileX + 1, tileY) == TileType.FLOOR) {
                    continue;
                }

                r.addLight(new screens.lighting.LightSource(
                        screens.lighting.LightSource.Type.FLUORESCENT_TUBE,
                        tubeX,
                        wallY,
                        170f,
                        new Color(220, 235, 190),
                        0.85f,
                        flickers
                ));
            }
        }
    }

    public long getSeed() {
        return 0L;
    }
}
