
package map;

import model.Enemy;
import model.boss.Boss1;
import model.boss.Boss2;
import model.boss.Boss2Minion;
import model.boss.Boss4;
import model.creep.Stalker;
import model.creep.AttackCreep;
import model.creep.Duck;
import java.awt.Color;
import java.util.Arrays;

/**
 * Bộ tạo bản đồ cố định cho chế độ test boss và creep.
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
     * Giữ sơ đồ boss cũ và nối hub phía Tây tới sáu phòng creep riêng biệt.
     */
    public WorldMap generateFloor(int floorLevel, int targetRooms) {
        WorldMap map = new WorldMap(floorLevel);
        int spawnX = 3;
        int spawnY = 3;

        // Giữ nguyên spawn và ba phòng boss cũ.
        Room spawnRoom = new Room(
                Room.DEFAULT_WIDTH,
                Room.DEFAULT_HEIGHT,
                Room.RoomType.SPAWN
        );
        map.addRoom(spawnX, spawnY, spawnRoom);

        map.addRoom(spawnX, spawnY - 1,
                new Room(Room.DEFAULT_WIDTH, Room.DEFAULT_HEIGHT, Room.RoomType.BOSS));
        map.addRoom(spawnX, spawnY + 1,
                new Room(Room.DEFAULT_WIDTH, Room.DEFAULT_HEIGHT, Room.RoomType.BOSS));
        map.addRoom(spawnX + 1, spawnY,
                new Room(Room.DEFAULT_WIDTH, Room.DEFAULT_HEIGHT, Room.RoomType.BOSS));

        // Hub phía Tây rộng hơn, có sáu cửa xếp dọc trên tường Tây.
        Room westHub = new Room(Room.RoomSize.BIG, Room.RoomType.NORMAL);
        map.addRoom(spawnX - 1, spawnY, westHub);

        // Sáu phòng đích được xếp trong cụm phía Tây.
        int[][] creepRoomGridPositions = {
                {0, 0}, {1, 1}, {0, 2}, {0, 4}, {1, 5}, {0, 6}
        };

        Room[] creepRooms = new Room[creepRoomGridPositions.length];

        for (int i = 0; i < creepRoomGridPositions.length; i++) {
            int[] position = creepRoomGridPositions[i];

            creepRooms[i] = new Room(
                    Room.DEFAULT_WIDTH,
                    Room.DEFAULT_HEIGHT,
                    Room.RoomType.NORMAL
            );

            map.addRoom(position[0], position[1], creepRooms[i]);
        }

        carveDoors(map);

        int[] westDoorOffsets = {5, 9, 13, 17, 21, 25};

        for (int i = 0; i < creepRooms.length; i++) {
            map.connectRooms(
                    westHub,
                    Direction.WEST,
                    westDoorOffsets[i],
                    creepRooms[i],
                    Room.DEFAULT_HEIGHT / 2
            );
        }

        populateEnemies(map, creepRooms);

        // Thêm đèn.
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
     * Sinh ba boss cũ, một Stalker ở phòng creep đầu tiên
     * và Boss2Minion ở năm phòng creep còn lại.
     */
    private void populateEnemies(WorldMap map, Room[] creepRooms) {
        int centerX = (Room.DEFAULT_WIDTH / 2) * Room.TILE_SIZE;
        int centerY = (Room.DEFAULT_HEIGHT / 2) * Room.TILE_SIZE;

        for (Room r : map.getRooms()) {
            if (r.getRoomType() == Room.RoomType.BOSS) {
                Enemy boss;

                if (r.getGridY() == 2) {
                    boss = new Boss1();
                } else if (r.getGridX() == 4) {
                    boss = new Boss2();
                } else {
                    boss = new Boss4(centerX, centerY);
                }

                boss.setX(centerX - boss.getWidth() / 2.0);
                boss.setY(centerY - boss.getHeight() / 2.0);

                r.addEnemy(boss);
                r.setCleared(false);

            } else {
                int creepRoomIndex = Arrays.asList(creepRooms).indexOf(r);

                if (creepRoomIndex >= 0) {
                    Enemy creep;

                    if (creepRoomIndex == 0) {
                        // Phòng creep số 1: AttackCreep.
                        creep = new AttackCreep();
                        creep.setX(centerX - creep.getWidth() / 2.0);
                        creep.setY(centerY - creep.getHeight() / 2.0);

                    } else if (creepRoomIndex == 1) {
                        // Phòng creep số 2: Entity 27 (Duck).
                        creep = new Duck(centerX, centerY);
                        creep.setX(centerX - creep.getWidth() / 2.0);
                        creep.setY(centerY - creep.getHeight() / 2.0);

                    } else {
                        // Bốn phòng còn lại giữ Boss2Minion.
                        creep = new Boss2Minion(
                                centerX - 35 / 2.0,
                                centerY - 35 / 2.0
                        );
                    }

                    r.addEnemy(creep);
                    r.setCleared(false);
                }
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
