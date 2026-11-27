package test;

import game.MainGame;
import map.Room;
import map.WorldMap;
import model.Enemy;

import javax.swing.SwingUtilities;

final class EnemyTestRoom {
    private EnemyTestRoom() {
    }

    static void launch(String title, String enemyClass, double cameraZoom) {
        SwingUtilities.invokeLater(() -> {
            MainGame game = new MainGame();
            game.setTitle(title);
            game.startGame(floor -> create(enemyClass), cameraZoom);
            game.setVisible(true);
            game.getContentPane().requestFocusInWindow();
        });
    }

    static WorldMap create(String enemyClass) {
        WorldMap map = new WorldMap(1);
        Room room = new Room(Room.RoomSize.BIG, Room.RoomType.SPAWN);
        int centerX = room.getWidth() * Room.TILE_SIZE / 2;
        int centerY = room.getHeight() * Room.TILE_SIZE / 2;

        Enemy enemy = instantiate(enemyClass, centerX + 128, centerY);
        enemy.setX(centerX + 128 - enemy.getWidth() / 2.0);
        enemy.setY(centerY - enemy.getHeight() / 2.0);
        room.addEnemy(enemy);
        room.setCleared(false);

        int gridCenter = WorldMap.GRID_SIZE / 2;
        map.addRoom(gridCenter, gridCenter, room);
        return map;
    }

    private static Enemy instantiate(String enemyClass, double x, double y) {
        try {
            Class<? extends Enemy> type = Class.forName(enemyClass).asSubclass(Enemy.class);
            try {
                return type.getConstructor().newInstance();
            } catch (NoSuchMethodException ignored) {
                return type.getConstructor(double.class, double.class).newInstance(x, y);
            }
        } catch (ReflectiveOperationException | ClassCastException exception) {
            throw new IllegalArgumentException(
                    "ENEMY_CLASS must name an Enemy with a public () or (double, double) constructor: " + enemyClass,
                    exception);
        }
    }
}
