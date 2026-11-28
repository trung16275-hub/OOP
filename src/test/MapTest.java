package test;

import map.Direction;
import map.MapGenerator;
import map.Room;
import map.TileType;
import map.WorldMap;

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
        assert room.isCleared() : "Phòng SPAWN phải được cleared mặc định";
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
        WorldMap map = new MapGenerator(12345L).generateFloor(1, 8);
        assert map.getRooms().size() == 8 : "Procedural floor should contain eight rooms";
        assert map.getSpawnRoom() != null : "Procedural floor should contain a spawn room";
        assert map.getBossRoom() != null : "Procedural floor should contain a boss room";
        System.out.println("✓ testProceduralFloorGeneration: OK");
    }

    private static void testDoorSymmetry() {
        MapGenerator generator = new MapGenerator(999L);
        WorldMap map = generator.generateFloor(1, 8);

        for (Room r : map.getRooms()) {
            for (Direction dir : Direction.values()) {
                if (r.hasDoor(dir)) {
                    Room neighbor = map.getRoom(r.getGridX() + dir.getDx(), r.getGridY() + dir.getDy());
                    assert neighbor != null : "Phải tồn tại phòng lân cận ở hướng có cửa";
                    assert neighbor.hasDoor(dir.opposite()) : "Phòng lân cận phải có cửa đối xứng ngược lại";
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
