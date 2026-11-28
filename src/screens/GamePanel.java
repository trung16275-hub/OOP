package screens;

import game.MainGame;
import game.StartingLoadout;
import map.Direction;
import map.MapGenerator;
import map.Room;
import map.TileType;
import map.WorldMap;
import model.Enemy;
import model.Projectile;
import model.RoomEffect;
import model.weapon.Weapon;
import screens.input.KeyHandler;
import screens.lighting.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import model.player.Player;
public class GamePanel extends JPanel implements Runnable {
    private MainGame mainFrame;
    private Thread gameThread;
    private volatile boolean isRunning;
    private final Object stateLock = new Object();

    // Entities
    private Player player;
    private long playerDeathStartNanos;

    // Weapons
    private List<Weapon> weapons;
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Projectile> enemyProjectiles = new ArrayList<>();
    private final List<RoomEffect> roomEffects = new ArrayList<>();

    // Map System
    private MapGenerator mapGenerator;
    private WorldMap worldMap;
    private Room room;
    private int currentFloor;

    // Key Handling
    private KeyHandler keyH = new KeyHandler();

    // Renderer
    private GameRenderer renderer;
    private EnemyNote openNote;

    public GamePanel(MainGame mainFrame) {
        setPreferredSize(new Dimension(MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT));
        this.mainFrame = mainFrame;
        this.renderer = new GameRenderer();
        this.mapGenerator = new MapGenerator();
        this.addKeyListener(keyH);

        setFocusable(true);
        // Khởi tạo nhân vật, tải file map, load hội thoại cốt truyện đầu game ở đây
        initNewGame();
    }

    public void initNewGame() {
        clearRoomProjectilesAndEffects();
        this.currentFloor = 1;
        this.openNote = null;

        this.player = new Player();
        this.playerDeathStartNanos = 0;
        this.weapons = StartingLoadout.createWeapons();
        if (!weapons.isEmpty()) {
            this.player.equip(weapons.get(0));
        }
        createFloor();

        if (room != null) {
            room.getPickups().addAll(StartingLoadout.createSpawnPickups(player));
        }

    }

    private void toggleWeapon() {
        if (weapons.size() < 2) {
            return;
        }
        int currentIndex = weapons.indexOf(player.getCurrentWeapon());
        player.equip(weapons.get((currentIndex + 1) % weapons.size()));
        player.stopAttackAnimation();
    }

    public void startGameLoop() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // Vòng lặp game (Game Loop)
        while (isRunning) {
            update(); // Cập nhật logic (tọa độ, máu, va chạm)
            repaint(); // Vẽ lại mọi thứ ra màn hình

            try {
                Thread.sleep(16); // Giới hạn tốc độ khung hình (khoảng 60 FPS)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        synchronized (stateLock) {
            boolean closeNote = keyH.consumeCloseNotePress();
            if (openNote != null) {
                boolean toggleNote = keyH.consumePickupPress();
                keyH.consumeDashPress();
                keyH.consumeWeaponSwitchPress();
                if (closeNote || toggleNote) {
                    openNote = null;
                }
                return;
            }
            if (!player.isDead()) {
                updateState();
            }
            if (player.isDead()) {
                if (playerDeathStartNanos == 0) {
                    playerDeathStartNanos = System.nanoTime();
                }
                if (getPlayerDeathElapsedMs() >= renderer.getPlayerDeathDurationMs(player)) {
                    endGame();
                }
            }
        }
    }

    private long getPlayerDeathElapsedMs() {
        return playerDeathStartNanos == 0 ? 0
                : TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - playerDeathStartNanos);
    }

    private void updateState() {
        // Cập nhật trạng thái quái vật, nhân vật di chuyển

        //1. Cập nhật nhân vật
        if (keyH.consumeWeaponSwitchPress()) {
            toggleWeapon();
        }

        double moveX = 0;
        double moveY = 0;

        if (keyH.upPressed)    moveY -=1;
        if (keyH.downPressed)  moveY +=1;
        if (keyH.leftPressed)  moveX -=1;
        if (keyH.rightPressed) moveX +=1;

        if (moveX != 0 && moveY !=0) {
            moveX *= 0.7071;
            moveY *= 0.7071;
        }

        if (keyH.consumeDashPress()) {
            player.startDash(moveX, moveY);
        }

        double previousX = player.getX();
        double previousY = player.getY();
        player.updateMovement(moveX, moveY, room);
        if (keyH.consumePickupPress()) {
            String noteId = room.findNearbyEnemyNote(player.getMovementBounds());
            if (noteId != null) {
                openNote = EnemyNote.find(noteId);
                if (openNote != null) {
                    return;
                }
            }
            room.collectNearestPickup(player, weapons);
            if (player.isDead()) {
                return;
            }
        }
        double movedX = player.getX() - previousX;
        double movedY = player.getY() - previousY;
        player.setMoving(movedX != 0 || movedY != 0);
        if (movedX != 0) {
            // Chỉ đổi hướng ngắm khi nhân vật thực sự di chuyển ngang, không phải khi bị tường chặn.
            player.setAimDirection(movedX, 0);
            player.setFacingLeft(movedX < 0);
        }

        // Fire input is processed independently of movement collision, so touching a wall does not block shooting.
        if (player.isDashing() || player.isMovementLocked()) {
            player.stopAttackAnimation();
        } else if (keyH.spacePressed) {
            player.requestWeaponAttack();
        }

        player.updateAction(room, projectiles::add);
        updateProjectiles();

        // 2. Kiểm tra chuyển phòng
        Direction doorDir = room.checkDoorTrigger(player.getMovementBounds());
        if (doorDir != null) {
            Room nextRoom = worldMap.moveTo(doorDir);
            if (nextRoom != room) {
                this.room = nextRoom;
                this.room.setVisited(true);
                Point spawnPos = nextRoom.getDoorSpawnPosition(doorDir);
                player.setX(spawnPos.x);
                player.setY(spawnPos.y);
                clearRoomProjectilesAndEffects();
                room.getEnemies().forEach(Enemy::onRoomEntered);
            }
        }


        //3. Cập nhật Enemy
        if (room.getEnemies() != null) {
            List<Enemy> enemies = room.getEnemies();
            enemies.removeIf(Enemy::isRemovable);

            List<Enemy> enemiesToReplace = new ArrayList<>();
            for (Enemy e : enemies) {
                e.updateBehavior(player, room);
                if (player.isDead()) {
                    return;
                }
                e.updateAction(room, enemyProjectiles::add);
                if (e.shouldReplace()) {
                    enemiesToReplace.add(e);
                } else {
                    e.updateRoomEffects(roomEffects::add);
                }
            }

            for (Enemy enemy : enemiesToReplace) {
                enemies.remove(enemy);
                enemies.addAll(enemy.createReplacements(room));
            }
        }

        updateEnemyProjectiles();
        updateRoomEffects();

        //4. Cap nhat phong
        if (room != null) {
            if (room.getEnemies().isEmpty()) {
                room.setCleared(true);
                room.openDoors();
            }
            if (room.getRoomType() == Room.RoomType.BOSS &&
                    room.getEnemies().isEmpty() && !worldMap.isFloorCleared()) {
                room.spawnPortal();
                worldMap.setFloorCleared(true);
            }
        }

        //5. Cap nhat chuyen floor
        if (worldMap.isFloorCleared()) {
            Rectangle bound = player.getMovementBounds();
            if (room.getTile((bound.x + bound.width / 2) / Room.TILE_SIZE,
                    (bound.y + bound.height / 2) / Room.TILE_SIZE)  == TileType.PORTAL) {
                if (currentFloor < 3) {
                    currentFloor++;
                    createFloor();
                } else {
                    winGame();
                    return;
                }
            }
        }

        //update light system
        renderer.getLightSystem().update(room, 0.016);

    }

    private void createFloor() {
        clearRoomProjectilesAndEffects();
        worldMap = mapGenerator.generateFloor(currentFloor, 8);
        room = worldMap.getSpawnRoom();
        renderer.loadTheme(worldMap.getTheme());

        renderer.getLightSystem().setAmbientDarkness(
                worldMap.getTheme().getAmbientDarkness());

        if (room != null) {
            room.setVisited(true);
            Point spawnCenter = room.getCenterSpawnPosition(player.getWidth(),player.getHeight());
            player.setX(spawnCenter.x);
            player.setY(spawnCenter.y);

        }
    }

    private void clearRoomProjectilesAndEffects() {
        projectiles.clear();
        enemyProjectiles.clear();
        roomEffects.clear();
        if (player != null) {
            player.getMovementEffects().clear();
        }
    }

    private void updateEnemyProjectiles() {
        Iterator<Projectile> iterator = enemyProjectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.update(room);
            if (projectile.hits(player)) {
                projectile.hitPlayer(player);
            }
            if (!projectile.isActive()) {
                iterator.remove();
            }
        }
    }

    private void updateRoomEffects() {
        Iterator<RoomEffect> iterator = roomEffects.iterator();
        while (iterator.hasNext()) {
            RoomEffect effect = iterator.next();
            effect.update(player);
            if (effect.isExpired()) {
                iterator.remove();
            }
        }
    }

    private void updateProjectiles() {
        if (room == null || room.getEnemies() == null) {
            return;
        }
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.update(room);
            for (Enemy e : room.getEnemies()) {
                if (e != null && !e.isDead() && projectile.isActive() && projectile.hits(e)) {
                    e.takeDamage(projectile.getDamage());
                    projectile.deactivate();
                    break;
                }
            }
            if (!projectile.isActive()) {
                iterator.remove();
            }
        }
    }

    private void endGame() {
        isRunning = false;
        // Swing screen changes must run on the event dispatch thread.
        SwingUtilities.invokeLater(() -> {
            if (mainFrame.getContentPane() == this) {
                mainFrame.showGameOver();
            }
        });
    }

    private void winGame() {
        isRunning = false;
        // Swing screen changes must run on the event dispatch thread.
        SwingUtilities.invokeLater(() -> {
            if (mainFrame.getContentPane() == this) {
                mainFrame.showGameWin();
            }
        });
    }

    @Override
    public void removeNotify() {
        if (mainFrame == null || !mainFrame.isTogglingFullscreen()) {
            isRunning = false;
        }
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int panelW = getWidth();
        int panelH = getHeight();
        if (panelW <= 0 || panelH <= 0) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            if (panelW != MainGame.WINDOW_WIDTH || panelH != MainGame.WINDOW_HEIGHT) {
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, panelW, panelH);

                double scale = Math.min(
                        (double) panelW / MainGame.WINDOW_WIDTH,
                        (double) panelH / MainGame.WINDOW_HEIGHT
                );
                double offsetX = (panelW - MainGame.WINDOW_WIDTH * scale) / 2.0;
                double offsetY = (panelH - MainGame.WINDOW_HEIGHT * scale) / 2.0;

                g2.translate(offsetX, offsetY);
                g2.scale(scale, scale);
                g2.clipRect(0, 0, MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            }
            synchronized (stateLock) {
                renderer.render(g2, player, room, worldMap, projectiles, getPlayerDeathElapsedMs(),
                        enemyProjectiles, roomEffects);
                if (openNote != null) {
                    renderer.renderEnemyNote(g2, openNote);
                }
            }
        } finally {
            g2.dispose();
        }
    }
}
