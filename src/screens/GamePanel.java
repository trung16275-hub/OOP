package screens;

import game.MainGame;
import map.Direction;
import map.MapGenerator;
import map.Room;
import map.TileType;
import map.WorldMap;
import model.Enemy;
import model.Player;
import model.Projectile;
import model.weapon.Ak47;
import model.weapon.Bat;
import screens.input.KeyHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;
import model.boss.Boss3;
public class GamePanel extends JPanel implements Runnable {
    private static final long AK47_FIRE_INTERVAL_MS = 180;
    private static final long BAT_ATTACK_INTERVAL_MS = 450;
    private static final int PLAYER_ATTACK_ANIMATION_MS = 320;

    private MainGame mainFrame;
    private Thread gameThread;
    private volatile boolean isRunning;

    // Entities
    private Player player;
    private Enemy currentEnemy;
    private Ak47 ak47;
    private Bat bat;
    private long nextShotTime;
    private final List<Projectile> projectiles = new ArrayList<>();
    private static final double PROJECTILE_SPEED = 12.0;

    // Map System
    private MapGenerator mapGenerator;
    private WorldMap worldMap;
    private Room room;

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
        // 1. Tạo tầng hầm ngục ngẫu nhiên (Tầng 1, 8 phòng)
        this.worldMap = mapGenerator.generateFloor(1, 8);
        this.room = worldMap.getCurrentRoom();
        if (this.room != null) {
            this.room.setVisited(true);
        }

        this.player = new Player();
        this.ak47 = new Ak47();
        this.bat = new Bat();
        this.player.equip(ak47);
        this.player.setX(180);
        this.player.setY(260);

        // 3. Cập nhật kẻ địch trong phòng hiện tại
        updateRoomEntities();
        

    }
    
    

    private void updateRoomEntities() {
        if (room != null && !room.getEnemies().isEmpty()) {
            this.currentEnemy = room.getEnemies().get(0);
        } else {
            this.currentEnemy = null;
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
        // Đạn chỉ bắn ngang; phím lên/xuống không đổi hướng bắn.
        if (moveX != 0) {
            player.setAimDirection(moveX, 0);
        }

        double previousX = player.getX();
        double previousY = player.getY();
        player.move(moveX, moveY, room);
        double movedX = player.getX() - previousX;
        double movedY = player.getY() - previousY;
        player.setMoving(movedX != 0 || movedY != 0);
        if (movedX != 0) {
            player.setFacingLeft(movedX < 0);
        }

        long now = System.currentTimeMillis();
        if (keyH.spacePressed && now >= nextShotTime) {
            if (player.getCurrentWeapon() instanceof Ak47 ak47 && ak47.consumeAmmo()) {
                player.playAttackAnimation(PLAYER_ATTACK_ANIMATION_MS);
                double bulletX = player.getX() + player.getWidth() / 2.0;
                double bulletY = player.getY() + player.getHeight() / 2.0;
                projectiles.add(new Projectile(bulletX, bulletY, player.getAimX(), player.getAimY(),
                        PROJECTILE_SPEED, ak47.getDamage()));
                nextShotTime = now + AK47_FIRE_INTERVAL_MS;
            } else if (player.getCurrentWeapon() instanceof Bat) {
                player.playAttackAnimation(PLAYER_ATTACK_ANIMATION_MS);
                if (currentEnemy != null && !currentEnemy.isDead()
                        && player.isWithinHitbox(currentEnemy)) {
                    player.attack(currentEnemy);
                }
                nextShotTime = now + BAT_ATTACK_INTERVAL_MS;
            } else {
                player.stopAttackAnimation();
            }
        }

        updateProjectiles();

        // 2. Kiểm tra chuyển phòng
        Direction doorDir = room.checkDoorTrigger(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        if (doorDir != null) {
            Room nextRoom = worldMap.moveTo(doorDir);
            if (nextRoom != room) {
                this.room = nextRoom;
                this.room.setVisited(true);
                Point spawnPos = nextRoom.getDoorSpawnPosition(doorDir);
                player.setX(spawnPos.x);
                player.setY(spawnPos.y);
                projectiles.clear();
                updateRoomEntities();
            }
        }

        //3. Cap nhat cua
        if (room != null) {
            if (room.getEnemies().isEmpty()) {
                room.setCleared(true);
                room.openDoors();
            }
        }

        //4. Cập nhật Enemy
        if (currentEnemy != null && !currentEnemy.isDead()) {
            currentEnemy.updateBehavior(player, room);

            if (player.canDetect(currentEnemy)) {
                player.faceToward(currentEnemy);
            } else {
                player.resetFacing();
            }
        } else {
            player.resetFacing();
        }

    }

    private void updateProjectiles() {
        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile projectile = iterator.next();
            projectile.update(room);
            if (projectile.isActive() && projectile.hits(currentEnemy)) {
                currentEnemy.takeDamage(projectile.getDamage());
                projectile.deactivate();
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
        renderer.render((Graphics2D) g, player, currentEnemy, room, worldMap, projectiles);
    }
}
