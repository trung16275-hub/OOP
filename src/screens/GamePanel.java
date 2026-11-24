
package screens;

import game.MainGame;
import map.Direction;
import map.MapGenerator;
import map.Room;
import map.WorldMap;
import model.Enemy;
import model.Player;
import model.Projectile;
import model.weapon.Ak47;
import model.weapon.Bat;
import model.boss.Boss2;
import screens.input.KeyHandler;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.*;

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

    // Đạn của người chơi
    private final List<Projectile> projectiles = new ArrayList<>();

    // Đạn axit của Boss2
    private final List<Projectile> enemyProjectiles = new ArrayList<>();

    private static final double PROJECTILE_SPEED = 12.0;
    private static final double ACID_PROJECTILE_SPEED = 6.0;

    // Map System
    private MapGenerator mapGenerator;
    private WorldMap worldMap;
    private Room room;

    // Key Handling
    private KeyHandler keyH = new KeyHandler();

    // Renderer
    private GameRenderer renderer;

    public GamePanel(MainGame mainFrame) {
        setPreferredSize(new Dimension(
                MainGame.WINDOW_WIDTH,
                MainGame.WINDOW_HEIGHT
        ));

        this.mainFrame = mainFrame;
        this.renderer = new GameRenderer();
        this.mapGenerator = new MapGenerator();

        this.addKeyListener(keyH);
        setFocusable(true);

        initNewGame();
    }

    public void initNewGame() {

        // 1. Tạo tầng hầm ngục
        this.worldMap = mapGenerator.generateFloor(1, 8);
        this.room = worldMap.getCurrentRoom();

        if (this.room != null) {
            this.room.setVisited(true);
        }

        // 2. Khởi tạo người chơi và vũ khí
        this.player = new Player();
        this.ak47 = new Ak47();
        this.bat = new Bat();

        this.player.equip(ak47);
        this.player.setX(180);
        this.player.setY(260);

        // Xóa đạn cũ nếu bắt đầu game mới
        projectiles.clear();
        enemyProjectiles.clear();

        nextShotTime = 0;

        // 3. Cập nhật kẻ địch
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

        while (isRunning) {
            update();
            repaint();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {

        // 1. Kiểm tra người chơi đã chết chưa
        if (player.isDead()) {
            endGame();
            return;
        }

        // Đổi vũ khí
        if (keyH.consumeWeaponSwitchPress()) {
            toggleWeapon();
        }

        // 2. Điều khiển di chuyển
        double moveX = 0;
        double moveY = 0;

        if (keyH.upPressed) {
            moveY -= 1;
        }

        if (keyH.downPressed) {
            moveY += 1;
        }

        if (keyH.leftPressed) {
            moveX -= 1;
        }

        if (keyH.rightPressed) {
            moveX += 1;
        }

        // Chuẩn hóa tốc độ khi di chuyển chéo
        if (moveX != 0 && moveY != 0) {
            moveX *= 0.7071;
            moveY *= 0.7071;
        }

        // Đạn người chơi chỉ bắn ngang
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

        // 3. Tấn công bằng vũ khí
        long now = System.currentTimeMillis();

        if (keyH.spacePressed && now >= nextShotTime) {

            // AK47
            if (player.getCurrentWeapon() instanceof Ak47 ak47
                    && ak47.consumeAmmo()) {

                player.playAttackAnimation(PLAYER_ATTACK_ANIMATION_MS);

                double bulletX =
                        player.getX() + player.getWidth() / 2.0;

                double bulletY =
                        player.getY() + player.getHeight() / 2.0;

                projectiles.add(new Projectile(
                        bulletX,
                        bulletY,
                        player.getAimX(),
                        player.getAimY(),
                        PROJECTILE_SPEED,
                        ak47.getDamage()
                ));

                nextShotTime = now + AK47_FIRE_INTERVAL_MS;
            }

            // Bat
            else if (player.getCurrentWeapon() instanceof Bat) {

                player.playAttackAnimation(PLAYER_ATTACK_ANIMATION_MS);

                if (currentEnemy != null
                        && !currentEnemy.isDead()
                        && player.isWithinHitbox(currentEnemy)) {

                    player.attack(currentEnemy);
                }

                nextShotTime = now + BAT_ATTACK_INTERVAL_MS;
            }

            else {
                player.stopAttackAnimation();
            }
        }

        // 4. Cập nhật đạn của người chơi
        updateProjectiles();

        // 5. Kiểm tra chuyển phòng
        Direction doorDir = room.checkDoorTrigger(
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight()
        );

        if (doorDir != null) {

            Room nextRoom = worldMap.moveTo(doorDir);

            if (nextRoom != room) {

                this.room = nextRoom;
                this.room.setVisited(true);

                Point spawnPos = nextRoom.getDoorSpawnPosition(doorDir);

                player.setX(spawnPos.x);
                player.setY(spawnPos.y);

                // Xóa đạn khi chuyển phòng
                projectiles.clear();
                enemyProjectiles.clear();

                updateRoomEntities();
            }
        }

        // 6. Cập nhật cửa phòng
        // Mở cửa khi tất cả Enemy trong phòng đã chết
        if (room != null && !room.isCleared()) {

            boolean allEnemiesDead = true;

            for (Enemy enemy : room.getEnemies()) {
                if (!enemy.isDead()) {
                    allEnemiesDead = false;
                    break;
                }
            }

            if (allEnemiesDead) {
                room.setCleared(true);
                room.openDoors();
            }
        }

        // 7. Cập nhật Enemy
        if (currentEnemy != null && !currentEnemy.isDead()) {

            currentEnemy.updateBehavior(player, room);

            // Boss2 tạo yêu cầu bắn axit
            if (currentEnemy instanceof Boss2 boss2
                    && boss2.consumeAcidAttackRequest()) {

                double bossX =
                        boss2.getX() + boss2.getWidth() / 2.0;

                double bossY =
                        boss2.getY() + boss2.getHeight() / 2.0;

                double playerX =
                        player.getX() + player.getWidth() / 2.0;

                double playerY =
                        player.getY() + player.getHeight() / 2.0;

                double dx = playerX - bossX;
                double dy = playerY - bossY;

                enemyProjectiles.add(new Projectile(
                        bossX,
                        bossY,
                        dx,
                        dy,
                        ACID_PROJECTILE_SPEED,
                        boss2.getAcidDamage()
                ));
            }

            if (player.canDetect(currentEnemy)) {
                player.faceToward(currentEnemy);
            } else {
                player.resetFacing();
            }

        } else {
            player.resetFacing();
        }

        // 8. Cập nhật đạn axit
        updateEnemyProjectiles();

        // 9. Cập nhật hệ thống ánh sáng
        renderer.getLightSystem().update(room, 0.016);
    }

    // Cập nhật đạn của người chơi
    private void updateProjectiles() {

        Iterator<Projectile> iterator = projectiles.iterator();

        while (iterator.hasNext()) {

            Projectile projectile = iterator.next();

            projectile.update(room);

            if (projectile.isActive()
                    && currentEnemy != null
                    && !currentEnemy.isDead()
                    && projectile.hits(currentEnemy)) {

                currentEnemy.takeDamage(projectile.getDamage());
                projectile.deactivate();
            }

            if (!projectile.isActive()) {
                iterator.remove();
            }
        }
    }

    // Cập nhật đạn axit của Boss2
    private void updateEnemyProjectiles() {

        Iterator<Projectile> iterator = enemyProjectiles.iterator();

        while (iterator.hasNext()) {

            Projectile projectile = iterator.next();

            projectile.update(room);

            // Đạn axit va chạm với người chơi
            if (projectile.isActive() && projectile.hits(player)) {

                player.takeDamage(projectile.getDamage());
                projectile.deactivate();
            }

            if (!projectile.isActive()) {
                iterator.remove();
            }
        }
    }

    private void endGame() {

        isRunning = false;

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

        // Gộp hai loại đạn để dùng Renderer hiện tại
        // Chưa thay đổi GameRenderer
        List<Projectile> allProjectiles = new ArrayList<>(projectiles);
        allProjectiles.addAll(enemyProjectiles);

        renderer.render(
                (Graphics2D) g,
                player,
                currentEnemy,
                room,
                worldMap,
                allProjectiles
        );
    }
}
