package screens;

import game.MainGame;
import map.Direction;
import map.MapGenerator;
import map.Room;
import map.TileType;
import map.WorldMap;
import model.Enemy;
import model.Projectile;
import model.prop.AmmoPickup;
import model.prop.LiquidPain;
import model.weapon.Ak47;
import model.weapon.Bat;
import screens.input.KeyHandler;
import screens.lighting.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import model.boss.Boss3;
import model.player.Player;
public class GamePanel extends JPanel implements Runnable {
    private static final long AK47_FIRE_INTERVAL_MS = 180;
    private static final long BAT_ATTACK_INTERVAL_MS = 450;
    private static final int PLAYER_ATTACK_ANIMATION_MS = 320;

    private MainGame mainFrame;
    private Thread gameThread;
    private volatile boolean isRunning;
    private final Object stateLock = new Object();

    // Entities
    private Player player;

    // Weapons
    private Ak47 ak47;
    private Bat bat;
    private long nextShotTime;
    private final List<Projectile> projectiles = new ArrayList<>();
    private static final double PROJECTILE_SPEED = 12.0;

    // Map System
    private MapGenerator mapGenerator;
    private WorldMap worldMap;
    private Room room;
    private int currentFloor;

    // Key Handling
    private KeyHandler keyH = new KeyHandler();

    // Renderer
    private GameRenderer renderer;

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
        this.currentFloor = 1;

        this.player = new Player();
        this.ak47 = new Ak47();
        this.bat = new Bat();
        this.player.equip(ak47);
        createFloor();

        if (room != null) {
            int x = (int) player.getX() + player.getWidth() + 8;
            int y = (int) player.getY() + player.getHeight() - 30;
            room.getAmmoPickups().add(new AmmoPickup(x, y));
            room.setLiquidPain(new LiquidPain((int) player.getX() + 19,
                    (int) player.getY() + player.getHeight() + 32));
        }

    }

    private void toggleWeapon() {
        if (player.getCurrentWeapon() instanceof Ak47) {
            player.equip(bat);
        } else {
            player.equip(ak47);
        }
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
            updateState();
        }
    }

    private void updateState() {
        // Cập nhật trạng thái quái vật, nhân vật di chuyển

        //1. Cập nhật nhân vật
        if (player.isDead()) {
            endGame();
            return;
        }

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

        boolean dashing = player.isDashing();
        if (dashing) {
            moveX = player.getDashDirectionX() * Player.DASH_SPEED_MULTIPLIER;
            moveY = player.getDashDirectionY() * Player.DASH_SPEED_MULTIPLIER;
        }
        double previousX = player.getX();
        double previousY = player.getY();
        player.move(moveX, moveY, room);
        if (keyH.consumePickupPress()) {
            interactWithPickup();
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
        long now = System.currentTimeMillis();
        if (player.isDashing()) {
            player.stopAttackAnimation();
        } else if (keyH.spacePressed && now >= nextShotTime) {
            if (player.getCurrentWeapon() instanceof Ak47 ak47 && ak47.consumeAmmo()) {
                player.playAttackAnimation(PLAYER_ATTACK_ANIMATION_MS);
                double bulletX = player.getX() + player.getWidth() / 2.0;
                double bulletY = player.getY() + player.getHeight() / 2.0;
                projectiles.add(new Projectile(bulletX, bulletY, player.getAimX(), player.getAimY()
                        , PROJECTILE_SPEED, ak47.getDamage()));
                nextShotTime = now + AK47_FIRE_INTERVAL_MS;
            } else if (player.getCurrentWeapon() instanceof Bat) {
                player.playAttackAnimation(PLAYER_ATTACK_ANIMATION_MS);
                for (Enemy e : room.getEnemies()) {
                    if (e != null && !e.isDead()
                            && player.isWithinHitbox(e)) {
                        player.attack(e);
                    }
                }
                nextShotTime = now + BAT_ATTACK_INTERVAL_MS;
            } else {
                player.stopAttackAnimation();
            }
        }

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
                projectiles.clear();
            }
        }


        //3. Cập nhật Enemy

        if (room.getEnemies()!=null) {
            room.getEnemies().removeIf(Enemy::isDead);
            for (Enemy e : room.getEnemies()) {
                e.updateBehavior(player, room);
                // player.faceToward(e); // Currently not used
                // player.resetFacing();
            }
        }

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
                if (currentFloor < 3){
                    currentFloor++ ;
                    createFloor();
                } else {/*show winning screen*/ }
            }
        }

        //update light system
        renderer.getLightSystem().update(room, 0.016);

    }

    private void interactWithPickup() {
        Rectangle playerBounds = player.getMovementBounds();
        int index = room.findNearestAmmoPickup(playerBounds);
        if (room.isLiquidPainNearest(playerBounds, index)) {
            player.takeDirectDamage(LiquidPain.HP_COST);
            room.setLiquidPain(null);
            return;
        }
        if (index < 0) {
            return;
        }

        List<AmmoPickup> pickups = room.getAmmoPickups();
        AmmoPickup pickup = pickups.get(index);
        int added = ak47.addAmmo(pickup.rounds());
        if (added == pickup.rounds()) {
            pickups.remove(index);
        } else if (added > 0) {
            pickups.set(index, new AmmoPickup(pickup.prop(), pickup.rounds() - added));
        }
    }

    private void createFloor() {
        projectiles.clear();
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

    private void updateProjectiles() {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.update(room);
            for (Enemy e : room.getEnemies()) {
                if (projectile.isActive() && projectile.hits(e)) {
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

    @Override
    public void removeNotify() {
        isRunning = false;
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized (stateLock) {
            renderer.render((Graphics2D) g, player, room, worldMap, projectiles);
        }
    }
}
