
package screens;

import game.MainGame;
import map.Direction;
import map.MapGenerator;
import map.Room;
import map.WorldMap;

import model.Enemy;
import model.Player;
import model.Projectile;
import model.AcidProjectile;

import model.boss.Boss1;
import model.boss.Boss2;
import model.boss.Boss2Minion;
import model.boss.AcidTrail;

import model.weapon.Ak47;
import model.weapon.Bat;

import screens.input.KeyHandler;
import screens.lighting.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.awt.geom.Point2D;

import javax.swing.*;

import model.boss.Boss3;
import model.boss.Boss4;
import model.creep.Duck;

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
    private Enemy currentEnemy;

    // Weapons
    private Ak47 ak47;
    private Bat bat;
    private long nextShotTime;

    private final List<Projectile> projectiles = new ArrayList<>();

    private static final double PROJECTILE_SPEED = 12.0;

    // Đạn axit của Boss2
    private final List<AcidProjectile> acidProjectiles = new ArrayList<>();

    // Vệt axit do Boss2 Minion tạo ra
    private final List<AcidTrail> acidTrails = new ArrayList<>();

    // Lưu vị trí Minion tạo vệt axit gần nhất
    private final Map<Boss2Minion, Point2D.Double> lastTrailPositions =
            new HashMap<>();

    // Hiệu ứng làm chậm Player
    private static final long SLOW_DURATION_NANOS = 2_000_000_000L;
    private static final double SLOW_MULTIPLIER = 0.5;

    private long slowEffectEndTimeNanos;

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

    // =========================
    // KHỞI TẠO GAME
    // =========================

    public void initNewGame() {

        projectiles.clear();
        acidProjectiles.clear();
        acidTrails.clear();
        lastTrailPositions.clear();

        slowEffectEndTimeNanos = 0L;

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
        
    }

    // =========================
    // ĐỔI VŨ KHÍ
    // =========================

    private void toggleWeapon() {

        if (player.getCurrentWeapon() instanceof Ak47) {
            player.equip(bat);
        } else {
            player.equip(ak47);
        }

        player.stopAttackAnimation();
    }

    // =========================
    // GAME LOOP
    // =========================

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

        synchronized (stateLock) {
            updateState();
        }
    }

    // =========================
    // CẬP NHẬT GAME
    // =========================

    private void updateState() {

        if (player.isDead()) {
            endGame();
            return;
        }

        if (keyH.consumeWeaponSwitchPress()) {
            toggleWeapon();
        }

        // =========================
        // 1. DI CHUYỂN PLAYER
        // =========================

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

        if (moveX != 0 && moveY != 0) {
            moveX *= 0.7071;
            moveY *= 0.7071;
        }

        // Làm chậm Player khi dính axit
        if (System.nanoTime() < slowEffectEndTimeNanos) {
            moveX *= SLOW_MULTIPLIER;
            moveY *= SLOW_MULTIPLIER;
        }

        // Đạn chỉ bắn ngang
        if (moveX != 0) {
            player.setAimDirection(moveX, 0);
        }


        double previousX = player.getX();
        double previousY = player.getY();

// Stalker đánh trúng: Player không thể di chuyển trong thời gian bị choáng.
        if (player.isStunned()) {
            player.setMoving(false);
        } else {
            player.move(moveX, moveY, room);
        }

        double movedX = player.getX() - previousX;
        double movedY = player.getY() - previousY;

        player.setMoving(movedX != 0 || movedY != 0);

        if (movedX != 0) {
            player.setFacingLeft(movedX < 0);
        }


        // =========================
        // 2. TẤN CÔNG
        // =========================

        long now = System.currentTimeMillis();

        if (keyH.spacePressed && now >= nextShotTime) {

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

            } else if (player.getCurrentWeapon() instanceof Bat) {

                player.playAttackAnimation(PLAYER_ATTACK_ANIMATION_MS);

                for (Enemy e : room.getEnemies()) {

                    if (e != null
                            && !e.isDead()
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

        // =========================
        // 3. CHUYỂN PHÒNG
        // =========================

        Room.DoorTrigger doorTrigger = room.checkDoorTriggerDetails(
                player.getX(),
                player.getY(),
                player.getWidth(),
                player.getHeight()
        );

        if (doorTrigger != null) {

            WorldMap.RoomTransition transition = worldMap.moveTo(
                doorTrigger.direction(),
                doorTrigger.offset()
            );
            Room nextRoom = transition.room();

            if (nextRoom != room) {

                this.room = nextRoom;
                this.room.setVisited(true);

                Point spawnPos = nextRoom.getDoorSpawnPosition(
                    doorTrigger.direction(),
                    transition.arrivalOffset()
                );

                player.setX(spawnPos.x);
                player.setY(spawnPos.y);

                projectiles.clear();
                acidProjectiles.clear();
                acidTrails.clear();
                lastTrailPositions.clear();

                slowEffectEndTimeNanos = 0L;
            }
        }

        // =========================
        // 4. CẬP NHẬT ENEMY
        // =========================

        if (room.getEnemies() != null) {

            List<Enemy> enemies = room.getEnemies();

           enemies.removeIf(enemy -> {
    if (!enemy.isDead()) {
        return false;
    }

    if (enemy instanceof Boss1 boss) {
        return !boss.isPlayingDeathAnimation();
    }

    if (enemy instanceof model.creep.AttackCreep creep) {
        return !creep.isPlayingDeathAnimation();
    }

    return true;
});
            // Danh sách Boss2 đã hoàn thành phân thân
            List<Boss2> bossesToSplit = new ArrayList<>();
            // [Entity 27 - Duck] Danh sách Duck đã chạy xong animation chết để nhân đôi
            List<Duck> ducksToSplit = new ArrayList<>();
            for (Enemy e : enemies) {

                e.updateBehavior(player, room);

                // -------------------------
                // BOSS2 PHUN AXIT
                // -------------------------

                if (e instanceof Boss2 boss2
                        && boss2.consumeAcidAttackRequest()) {

                    double bossCenterX =
                            boss2.getX() + boss2.getWidth() / 2.0;

                    double bossCenterY =
                            boss2.getY() + boss2.getHeight() / 2.0;

                    double playerCenterX =
                            player.getX() + player.getWidth() / 2.0;

                    double playerCenterY =
                            player.getY() + player.getHeight() / 2.0;

                    double directionX = playerCenterX - bossCenterX;
                    double directionY = playerCenterY - bossCenterY;

                    AcidProjectile acid = new AcidProjectile(
                            bossCenterX - 6,
                            bossCenterY - 6,
                            directionX,
                            directionY,
                            boss2.getAcidDamage()
                    );

                    acidProjectiles.add(acid);
                }

                // -------------------------
                // KIỂM TRA BOSS2 PHÂN THÂN
                // -------------------------

                if (e instanceof Boss2 boss2
                        && boss2.isSplitAnimationFinished()) {

                    bossesToSplit.add(boss2);
                }
                if (e instanceof Duck duck
                        && duck.isSplitAnimationFinished()) {

                    ducksToSplit.add(duck);
                }
                // -------------------------
                // MINION ĐỂ LẠI VỆT AXIT
                // -------------------------

                if (e instanceof Boss2Minion minion
                        && !minion.isDead()) {

                    updateMinionTrail(minion);
                }
            }

            // -------------------------
            // THỰC HIỆN PHÂN THÂN
            // -------------------------

            for (Boss2 boss2 : bossesToSplit) {

                double bossX = boss2.getX();
                double bossY = boss2.getY();

                enemies.remove(boss2);

                Boss2Minion minion1 = new Boss2Minion(
                        bossX - 20,
                        bossY + 15
                );

                Boss2Minion minion2 = new Boss2Minion(
                        bossX + 55,
                        bossY + 15
                );

                enemies.add(minion1);
                enemies.add(minion2);

                lastTrailPositions.put(
                        minion1,
                        new Point2D.Double(minion1.getX(), minion1.getY())
                );

                lastTrailPositions.put(
                        minion2,
                        new Point2D.Double(minion2.getX(), minion2.getY())
                );
            }
            for (Duck duck : ducksToSplit) {
                enemies.remove(duck);
                enemies.addAll(duck.createSplitClones(room));
            }
            // Xóa dữ liệu của Minion không còn trong phòng
            lastTrailPositions.keySet().removeIf(
                    minion -> !enemies.contains(minion)
            );
        }

        // =========================
        // 5. CẬP NHẬT ĐẠN AXIT
        // =========================

        updateAcidProjectiles();

        // =========================
        // 6. CẬP NHẬT VỆT AXIT
        // =========================

        updateAcidTrails();

        // =========================
        // 7. KIỂM TRA PHÒNG
        // =========================

        if (room != null && room.getEnemies() != null) {

            if (room.getEnemies().isEmpty()) {

                room.setCleared(true);
                room.openDoors();
            }
        }

        // =========================
        // 8. CẬP NHẬT ÁNH SÁNG
        // =========================

        renderer.getLightSystem().update(room, 0.016);
    }

    // =========================
    // MINION TẠO VỆT AXIT
    // =========================

    private void updateMinionTrail(Boss2Minion minion) {

        Point2D.Double lastPosition = lastTrailPositions.get(minion);

        if (lastPosition == null) {

            lastTrailPositions.put(
                    minion,
                    new Point2D.Double(minion.getX(), minion.getY())
            );

            return;
        }

        double dx = minion.getX() - lastPosition.x;
        double dy = minion.getY() - lastPosition.y;

        double distance = Math.sqrt(dx * dx + dy * dy);

        // Cứ di chuyển đủ 20 pixel thì để lại một vệt axit
        if (distance >= 20) {

            acidTrails.add(new AcidTrail(
                    lastPosition.x + minion.getWidth() / 2.0,
                    lastPosition.y + minion.getHeight() / 2.0
            ));

            lastTrailPositions.put(
                    minion,
                    new Point2D.Double(minion.getX(), minion.getY())
            );
        }
    }

    // =========================
    // CẬP NHẬT ĐẠN PLAYER
    // =========================

    private void updateProjectiles() {

        if (room == null || room.getEnemies() == null) {
            return;
        }

        Iterator<Projectile> iterator = projectiles.iterator();

        while (iterator.hasNext()) {

            Projectile projectile = iterator.next();

            projectile.update(room);

            for (Enemy e : room.getEnemies()) {

                if (e != null
                        && !e.isDead()
                        && projectile.isActive()
                        && projectile.hits(e)) {

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

    // =========================
    // CẬP NHẬT ĐẠN AXIT BOSS2
    // =========================

    private void updateAcidProjectiles() {

        Iterator<AcidProjectile> iterator =
                acidProjectiles.iterator();

        while (iterator.hasNext()) {

            AcidProjectile acid = iterator.next();

            acid.update(room);

            if (acid.isActive() && acid.hits(player)) {

                player.takeDamage(acid.getDamage());

                // Làm chậm Player trong 2 giây
                applySlowEffect();

                acid.deactivate();
            }

            if (!acid.isActive()) {
                iterator.remove();
            }
        }
    }

    // =========================
    // CẬP NHẬT VỆT AXIT
    // =========================

    private void updateAcidTrails() {

        Iterator<AcidTrail> iterator = acidTrails.iterator();

        Rectangle playerBounds = new Rectangle(
                (int) Math.floor(player.getX()),
                (int) Math.floor(player.getY()),
                player.getWidth(),
                player.getHeight()
        );

        while (iterator.hasNext()) {

            AcidTrail trail = iterator.next();

            // Vệt axit gây sát thương khi Player đứng trên nó
            trail.update(player);

            // Kiểm tra Player có đang đứng trong vệt axit không
            if (trail.getBounds().intersects(playerBounds)) {

                applySlowEffect();
            }

            if (trail.isExpired()) {
                iterator.remove();
            }
        }
    }

    // =========================
    // LÀM CHẬM PLAYER
    // =========================

    private void applySlowEffect() {

        slowEffectEndTimeNanos =
                System.nanoTime() + SLOW_DURATION_NANOS;
    }

    // =========================
    // GAME OVER
    // =========================

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

    // =========================
    // RENDER
    // =========================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        synchronized (stateLock) {

            renderer.render(
                    (Graphics2D) g,
                    player,
                    room,
                    worldMap,
                    projectiles,
                    acidProjectiles,
                    acidTrails
            );
        }
    }
}
