
package screens;

import game.MainGame;
import map.Direction;
import map.MapGenerator;
import map.Room;
import map.TileType;
import map.WorldMap;

import model.Enemy;
import model.Projectile;

import model.boss.Boss1;
import model.boss.boss2.AcidProjectile;
import model.boss.boss2.AcidTrail;
import model.boss.boss2.Boss2;
import model.boss.boss2.Boss2Minion;

import model.prop.AmmoPickup;
import model.prop.BandagePickup;
import model.prop.KevlarArmorPickup;
import model.prop.LiquidPain;
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

import java.util.concurrent.TimeUnit;
import javax.swing.*;

import model.boss.Boss3;
import model.player.Player;
import java.util.function.IntFunction;
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
    private long playerDeathStartNanos;
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
    private final IntFunction<WorldMap> floorFactory;
    private final boolean spawnStarterPickups;
    private WorldMap worldMap;
    private Room room;
    private int currentFloor;

    // Key Handling
    private KeyHandler keyH = new KeyHandler();

    // Renderer
    private GameRenderer renderer;
    private final double cameraZoom;
    private EnemyNote openNote;

    public GamePanel(MainGame mainFrame) {
        this(mainFrame, floor -> new MapGenerator().generateFloor(floor, 8), true, 1.0);
    }

    public GamePanel(MainGame mainFrame, IntFunction<WorldMap> floorFactory) {
        this(mainFrame, floorFactory, 1.0);
    }

    public GamePanel(MainGame mainFrame, IntFunction<WorldMap> floorFactory, double cameraZoom) {
        this(mainFrame, floorFactory, false, cameraZoom);
    }

    private GamePanel(MainGame mainFrame, IntFunction<WorldMap> floorFactory, boolean spawnStarterPickups,
                      double cameraZoom) {

        setPreferredSize(new Dimension(
                MainGame.WINDOW_WIDTH,
                MainGame.WINDOW_HEIGHT
        ));

        this.mainFrame = mainFrame;
        this.renderer = new GameRenderer();
        this.floorFactory = floorFactory;
        this.spawnStarterPickups = spawnStarterPickups;
        this.cameraZoom = clampCameraZoom(cameraZoom);

        this.addKeyListener(keyH);

        setFocusable(true);

        initNewGame();
    }

    private static double clampCameraZoom(double zoom) {
        return Double.isFinite(zoom) ? Math.max(0.6, Math.min(1.5, zoom)) : 1.0;
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

        this.currentFloor = 1;
        this.openNote = null;

        this.player = new Player();
        this.playerDeathStartNanos = 0;
        this.ak47 = new Ak47();
        this.bat = new Bat();

        this.player.equip(ak47);
        createFloor();

        if (spawnStarterPickups && room != null) {
            int x = (int) player.getX() + player.getWidth() + 8;
            int y = (int) player.getY() + player.getHeight() - 30;
            room.getAmmoPickups().add(new AmmoPickup(x, y));
            room.setLiquidPain(new LiquidPain((int) player.getX() + 19,
                    (int) player.getY() + player.getHeight() + 32));
            room.getBandages().add(new BandagePickup(
                    (int) player.getX() - 38, y));
            room.getKevlarArmorPickups().add(new KevlarArmorPickup(
                    (int) player.getX() + 19, (int) player.getY() - 32));
        }

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

        //1. Cập nhật nhân vật
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

        if (keyH.consumeDashPress()) {
            player.startDash(moveX, moveY);
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



        boolean dashing = player.isDashing();
        if (dashing) {
            moveX = player.getDashDirectionX() * Player.DASH_SPEED_MULTIPLIER;
            moveY = player.getDashDirectionY() * Player.DASH_SPEED_MULTIPLIER;
        }
        if (player.isMovementLocked()) {
            moveX = 0;
            moveY = 0;
        }
        double previousX = player.getX();
        double previousY = player.getY();
        player.move(moveX, moveY, room);
        if (keyH.consumePickupPress()) {
            String noteId = room.findNearbyEnemyNote(player.getMovementBounds());
            if (noteId != null) {
                openNote = EnemyNote.find(noteId);
                if (openNote != null) {
                    return;
                }
            }
            interactWithPickup();
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

        // =========================
        // 2. TẤN CÔNG
        // =========================

        long now = System.currentTimeMillis();
        if (player.isDashing() || player.isMovementLocked()) {
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

        // 2. Kiểm tra chuyển phòng
        Room.DoorTrigger doorTrigger = room.checkDoorTriggerDetails(player.getMovementBounds());
        if (doorTrigger != null) {
            WorldMap.RoomTransition transition = worldMap.moveTo(doorTrigger.direction(), doorTrigger.offset());
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

            enemies.removeIf(enemy -> enemy.isDead()
                    && !(enemy instanceof Boss1 boss && boss.isPlayingDeathAnimation()));

            // Danh sách Boss2 đã hoàn thành phân thân
            List<Boss2> bossesToSplit = new ArrayList<>();
            // [Entity 27 - Duck] Danh sách Duck đã chạy xong animation chết để nhân đôi
            List<Duck> ducksToSplit = new ArrayList<>();
            for (Enemy e : enemies) {

                e.updateBehavior(player, room);
                if (player.isDead()) {
                    return;
                }

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

    private void interactWithPickup() {
        Rectangle playerBounds = player.getMovementBounds();
        int index = room.findNearestAmmoPickup(playerBounds);
        BandagePickup bandage = room.findNearestBandage(playerBounds, index);
        KevlarArmorPickup kevlar = room.findNearestKevlarArmor(playerBounds, index, bandage);
        if (kevlar != null) {
            player.addArmor(kevlar.amount());
            room.getKevlarArmorPickups().remove(kevlar);
            return;
        }
        if (bandage != null) {
            int amount = Math.min(bandage.amount(), player.getMaxHp() - player.getHp());
            if (amount > 0) {
                player.heal(amount);
                room.getBandages().remove(bandage);
            }
            return;
        }
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
        worldMap = floorFactory.apply(currentFloor);
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

    // =========================
    // RENDER
    // =========================

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
                        acidProjectiles, acidTrails, cameraZoom);
                if (openNote != null) {
                    renderer.renderEnemyNote(g2, openNote);
                }
            }
        } finally {
            g2.dispose();
        }
    }
}
