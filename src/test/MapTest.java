package test;

import map.Direction;
import map.MapGenerator;
import map.Room;
import map.TileType;
import map.WorldMap;
import model.boss.Boss2Minion;

public class MapTest {
    public static void main(String[] args) {
        System.out.println("--- Bắt đầu kiểm thử Map System (TBOI) ---");
        testRoomCreation();
        testDoorCarving();
        testProceduralFloorGeneration();
        testDoorSymmetry();
        testRoomTransitions();
        System.out.println("✅ TẤT CẢ CÁC BÀI TEST MAP ĐÃ VƯỢT QUA THÀNH CÔNG!");
    }

    private static void testRoomCreation() {
        Room room = new Room(Room.RoomSize.SCREEN, Room.RoomType.SPAWN);
        assert room.getWidth() == 32 : "Chiều rộng phòng phải là 32";
        assert room.getHeight() == 18 : "Chiều cao phòng phải là 18";
        assert room.getTile(0, 0) == TileType.WALL : "Góc (0, 0) phải là tường";
        assert room.getTile(16, 9) == TileType.FLOOR : "Tâm phòng phải là sàn";
        assert !room.isCleared() : "Phòng mới chưa được đánh dấu cleared";
        System.out.println("✓ testRoomCreation: OK");
    }

    private static void testDoorCarving() {
        Room room = new Room(Room.RoomSize.SCREEN, Room.RoomType.NORMAL);
        room.carveDoor(Direction.NORTH, false);
        room.carveDoor(Direction.SOUTH, true);

        assert room.hasDoor(Direction.NORTH) : "Phải có cửa Bắc";
        assert room.hasDoor(Direction.SOUTH) : "Phải có cửa Nam";
        assert room.getTile(16, 0) == TileType.DOOR_CLOSED : "Cửa Bắc đóng phải là DOOR_CLOSED";
        assert room.getTile(16, 17) == TileType.DOOR_OPEN : "Cửa Nam mở phải là DOOR_OPEN";

        room.openDoors();
        assert room.getTile(16, 0) == TileType.DOOR_OPEN : "Sau openDoors(), cửa Bắc phải mở";
        System.out.println("✓ testDoorCarving: OK");
    }

    private static void testProceduralFloorGeneration() {
        MapGenerator generator = new MapGenerator(12345L);
        WorldMap map = generator.generateFloor(1, 8);

        assert map.getRooms().size() == 11 : "Bản đồ phải có spawn, 3 boss, hub Tây và 6 phòng creep";
        assert map.getSpawnRoom() != null : "Phải có phòng SPAWN";
        assert map.getSpawnRoom().getGridX() == 3 && map.getSpawnRoom().getGridY() == 3 : "SPAWN phải giữ ở (3, 3)";
        Room westHub = map.getRoom(2, 3);
        assert westHub != null : "Phải có hub phía Tây";
        assert westHub.getWidth() == Room.RoomSize.BIG.width : "Hub phía Tây phải dài hơn phòng thường";
        assert westHub.getEnemies().isEmpty() : "Hub phía Tây không được có quái";
        assert westHub.getDoorOffsets(Direction.WEST).size() == 6 : "Hub phải có đúng 6 cửa phía Tây";

        int bossCount = 0;
        int creepRoomCount = 0;
        for (Room room : map.getRooms()) {
            if (room.getRoomType() == Room.RoomType.BOSS) {
                bossCount++;
            }
            if (!room.getEnemies().isEmpty() && room.getEnemies().get(0) instanceof Boss2Minion) {
                assert room.getEnemies().size() == 1 : "Mỗi phòng creep phải có đúng một creep";
                assert room.getEnemies().get(0) instanceof Boss2Minion : "Creep phòng đáy phải là Boss2Minion";
                creepRoomCount++;
            }
        }
        assert bossCount == 3 : "Ba phòng boss cũ phải được giữ nguyên";
        assert creepRoomCount == 6 : "Phải có đúng 6 phòng creep phía Tây";

        java.util.Set<Room> reachedRooms = new java.util.HashSet<>();
        for (int offset : westHub.getDoorOffsets(Direction.WEST)) {
            Room.DoorTrigger trigger = westHub.checkDoorTriggerDetails(
                0,
                offset * Room.TILE_SIZE - 30,
                60,
                60
            );
            assert trigger != null && trigger.direction() == Direction.WEST && trigger.offset() == offset
                : "Mỗi lối phải được nhận diện riêng theo vị trí cửa";

            map.setCurrentRoom(westHub);
            WorldMap.RoomTransition transition = map.moveTo(Direction.WEST, offset);
            assert transition.room() != westHub : "Mỗi cửa Tây phải dẫn sang phòng khác";
            assert transition.room().getEnemies().get(0) instanceof Boss2Minion
                    : "Mỗi cửa Tây phải dẫn tới một phòng creep";
            assert transition.room().getGridX() < map.getSpawnRoom().getGridX()
                : "Cả sáu phòng đích phải nằm phía Tây spawn";
            reachedRooms.add(transition.room());
        }
        assert reachedRooms.size() == 6 : "Sáu cửa Tây phải dẫn tới sáu phòng riêng biệt";
        System.out.println("✓ testProceduralFloorGeneration: OK (hub Tây có 6 cửa tới 6 phòng creep)");
    }

    private static void testDoorSymmetry() {
        MapGenerator generator = new MapGenerator(999L);
        WorldMap map = generator.generateFloor(1, 4);

        for (Room r : map.getRooms()) {
            for (Direction dir : Direction.values()) {
                if (r.hasDoor(dir)) {
                    Room neighbor = map.getRoom(r.getGridX() + dir.getDx(), r.getGridY() + dir.getDy());
                    if (neighbor != null) {
                        assert neighbor.hasDoor(dir.opposite()) : "Phòng lân cận phải có cửa đối xứng ngược lại";
                    } else {
                        for (int offset : r.getDoorOffsets(dir)) {
                            map.setCurrentRoom(r);
                            WorldMap.RoomTransition transition = map.moveTo(dir, offset);
                            assert transition.room() != r : "Cửa ngoài lưới phải nối tới phòng đích";
                            assert transition.room().hasDoor(dir.opposite())
                                    : "Phòng đích phải có cửa quay lại";
                        }
                    }
                }
            }
        }
        System.out.println("✓ testDoorSymmetry: OK (Tất cả cửa đều đối xứng hai chiều)");
    }

    private static void testRoomTransitions() {
        WorldMap map = new WorldMap(1);
        Room r1 = new Room(Room.RoomSize.SCREEN, Room.RoomType.SPAWN);
        Room r2 = new Room(Room.RoomSize.SCREEN, Room.RoomType.NORMAL);
        map.addRoom(4, 4, r1);
        map.addRoom(4, 3, r2); // r2 ở phía Bắc của r1

        r1.carveDoor(Direction.NORTH, true);
        r2.carveDoor(Direction.SOUTH, true);

        assert map.getCurrentRoom() == r1 : "Phòng hiện tại ban đầu phải là r1";
        Room moved = map.moveTo(Direction.NORTH);
        assert moved == r2 : "Di chuyển lên Bắc phải sang r2";
        assert map.getCurrentRoom() == r2 : "Phòng hiện tại phải cập nhật thành r2";
        System.out.println("✓ testRoomTransitions: OK");
    }
}
