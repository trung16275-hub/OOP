package map;

import model.Enemy;
import model.boss.Boss1;
import model.boss.boss2.Boss2;
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
        return generateFloor(floorLevel, 7 + rand.nextInt(3)); // 7 - 9 phòng mỗi tầng
    }

    /**
     * Sinh toàn bộ tầng hầm ngục với số lượng phòng mục tiêu.
     */
    public WorldMap generateFloor(int floorLevel, int targetRooms) {
        WorldMap map = new WorldMap(floorLevel);
        int spawnX = 3;
        int spawnY = 3;

        // 1. Khởi tạo phòng SPAWN tại trung tâm (3, 3)
        Room spawnRoom = new Room(Room.RoomSize.SCREEN, Room.RoomType.SPAWN);
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

            // Giới hạn trong phạm vi an toàn của lưới
            if (nx >= 1 && nx <= WorldMap.GRID_SIZE - 2 && ny >= 1 && ny <= WorldMap.GRID_SIZE - 2 && !map.hasRoom(nx, ny)) {
                // Kiểm tra số lượng phòng kề cận để tránh các cụm phòng hình vuông dày đặc
                if (countAdjacentRooms(map, nx, ny) <= 2 && !createsSquareCluster(map, nx, ny)) {
                    int roll = rand.nextInt(10);
                    Room.RoomSize chosenSize;
                    if (roll < 2) {
                        chosenSize = Room.RoomSize.SMALL;
                    } else if (roll < 8) {
                        chosenSize = Room.RoomSize.SCREEN;
                    } else chosenSize = Room.RoomSize.BIG;

                    Room newRoom = new Room(chosenSize, Room.RoomType.NORMAL);
                    map.addRoom(nx, ny, newRoom);
                    roomPositions.add(new Point(nx, ny));
                }
            }
        }

        // Chọn 1 phòng bình thường xa spawn nhất làm boss room
        setBossRoom(map, spawnRoom);

        // Đục cửa đối xứng giữa các phòng kề nhau
        carveDoors(map);

        // Áp dụng layout phòng định sẵn
        applyRoomLayouts(map, floorLevel);

        // Sinh quái vật vào các phòng
        populateEnemies(map, floorLevel);

        // them den
        populateLights(map);

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

                    boolean leadsToBoss = !map.isBossRoomUnlocked() && map.leadsToBoss(r, dir);
                    if (leadsToBoss) {
                        r.lockDoor(dir);
                        r.carveDoor(dir, false);
                    } else {
                        r.carveDoor(dir, r.isCleared());
                    }
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
                Boss1 boss = new Boss1();
                Point bossSpawnPoint = r.getCenterSpawnPosition(boss.getWidth(),boss.getHeight());
                boss.setX(bossSpawnPoint.x);
                boss.setY(bossSpawnPoint.y);
                r.addEnemy(boss);
                r.setCleared(false);
            } else {
                // Spawn room và các phòng còn lại không có quái
                r.setCleared(true);
            }
        }
    }

    private void applyRoomLayouts(WorldMap map, int floorLevel) {
        List<Point> candidatePickups = new ArrayList<>();
        List<Room> pickupRooms = new ArrayList<>();

        for (Room r : map.getRooms()) {
            List<Point> anchors = RoomTemplate.applyFloor1Layout(r, rand);
            if (r.getRoomType() == Room.RoomType.NORMAL && !anchors.isEmpty()) {
                for (Point p : anchors) {
                    candidatePickups.add(p);
                    pickupRooms.add(r);
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
                boolean flickers = (r.getRoomType() == Room.RoomType.BOSS) || (rand.nextInt(10) < 3);

                if (r.getTile((int) tubeX / Room.TILE_SIZE, (int) wallY / Room.TILE_SIZE ) == TileType.FLOOR ||
                        r.getTile((int) tubeX / Room.TILE_SIZE - 1, (int) wallY / Room.TILE_SIZE ) == TileType.FLOOR||
                        r.getTile((int) tubeX / Room.TILE_SIZE + 1, (int) wallY / Room.TILE_SIZE) == TileType.FLOOR
                ) continue;
                r.addLight(new LightSource(
                        LightSource.Type.FLUORESCENT_TUBE,
                        tubeX,
                        wallY,
                        170f,
                        new Color(220, 235, 190),
                        0.85f,
                        flickers
                ));
            }

            int innerTopPx = (r.getPadY() + 4) * Room.TILE_SIZE;
            int innerHeightPx = (r.getHeight() - (r.getPadY() + 4) - (r.getPadY() + 2)) * Room.TILE_SIZE;
            int innerWidthTiles = r.getWidth() - (r.getPadX() + 2) * 2;
            int innerHeightTiles = r.getHeight() - (r.getPadY() + 4) - (r.getPadY() + 2);

            int cols = innerWidthTiles >= 36 ? 3 : (innerWidthTiles >= 22 ? 2 : 1);
            int rows = innerHeightTiles >= 18 ? 2 : 1;
            float overheadRadius = innerWidthTiles >= 36 ? 190f : (innerWidthTiles >= 22 ? 170f : 155f);

            int maxLightCount = 3;
            int curLightCount = 0;

            for (int row = 0; row < rows; row++) {
                double yFrac = (rows == 1) ? 0.58 : (row == 0 ? 0.38 : 0.75);
                double poolY = innerTopPx + innerHeightPx * yFrac + rand.nextInt(-50, 51);

                for (int col = 0; col < cols; col++) {
                    if (curLightCount > maxLightCount) continue;
                    if (rand.nextInt(6) < 2) continue;
                    double poolX = innerLeftPx + innerWidthPx * ((col + 1.0) / (cols + 1.0)) + rand.nextInt(-50, 51);
                    boolean flickers = (r.getRoomType() == Room.RoomType.BOSS && rand.nextInt(10) < 4)
                            || (rand.nextInt(10) < 2);

                    r.addLight(new LightSource(
                            LightSource.Type.OVERHEAD_PANEL,
                            poolX,
                            poolY,
                            overheadRadius,
                            new Color(255, 242, 200),
                            (rand.nextInt(2) < 2) ? 0.82f : 0.35f,
                            flickers
                    ));
                }
            }
        }
    }

    public void setBossRoom(WorldMap map, Room spawnRoom) { //Now use BFS
        if (spawnRoom == null) return;

        Point start = new Point(spawnRoom.getGridX(), spawnRoom.getGridY());
        Queue<Point> queue = new LinkedList<>();
        Set<Point> visited = new HashSet<>();

        queue.offer(start);
        visited.add(start);

        Point furthestPoint = start;

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            furthestPoint = current;

            for (Direction dir : Direction.values()) {
                int nextX = current.x + dir.getDx();
                int nextY = current.y + dir.getDy();
                Point neighbor = new Point(nextX, nextY);

                // Check if a room exists at (nextX, nextY) and hasn't been visited
                if (!visited.contains(neighbor) && map.getRoom(nextX, nextY) != null) {
                    visited.add(neighbor);
                    queue.offer(neighbor);
                }
            }
        }

        if (!furthestPoint.equals(start)) {
            Room foundRoom = map.getRoom(furthestPoint.x, furthestPoint.y);
            if (foundRoom != null && foundRoom.getRoomType() != Room.RoomType.SPAWN) {
                Room bossRoom = new Room(Room.RoomSize.BIG, Room.RoomType.BOSS);
                map.addRoom(furthestPoint.x, furthestPoint.y, bossRoom);
            }
        }
    }

    private boolean createsSquareCluster(WorldMap map, int x, int y) {
        // Check all 4 quadrants where (x, y) would be a corner of a 2x2 square
        int[][] offsets = { {-1, -1}, {1, -1}, {-1, 1}, {1, 1} };
        for (int[] off : offsets) {
            int dx = off[0];
            int dy = off[1];
            if (map.hasRoom(x + dx, y) && map.hasRoom(x, y + dy) && map.hasRoom(x + dx, y + dy)) {
                return true;
            }
        }
        return false;
    }

    public long getSeed() {
        return seed;
    }
}
