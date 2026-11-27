
package screens;

import game.MainGame;
import map.Direction;
import map.Room;
import map.TileType;
import map.WorldMap;

import model.Combatant;
import model.Enemy;
import model.Player;
import model.Projectile;
import model.AcidProjectile;

import model.weapon.Ak47;

import model.boss.Boss1;
import model.boss.Boss2;
import model.boss.Boss3;
import model.boss.Boss4;
import model.boss.AcidTrail;

import screens.lighting.*;
import screens.lighting.LightSystem;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class GameRenderer {

    // Thời gian hiển thị mỗi frame
    private static final int PLAYER_IDLE_FRAME_DURATION_MS = 250;
    private static final int PLAYER_WALK_FRAME_DURATION_MS = 190;
    private static final int PLAYER_ATTACK_FRAME_DURATION_MS = 80;

    // Animation Boss 1
    private static final int BOSS1_LOOP_FRAME_DURATION_MS = 130;
    private static final int BOSS1_DASH_FRAME_DURATION_MS = 38;
    private static final int BOSS1_LUNGE_FRAME_DURATION_MS = 44;
    private static final int BOSS1_HURT_FRAME_DURATION_MS = 80;
    private static final int BOSS1_DEATH_FRAME_DURATION_MS = 220;

    // Animation Boss 2
    private static final int BOSS2_IDLE_FRAME_DURATION_MS = 220;
    private static final int BOSS2_RUN_FRAME_DURATION_MS = 110;
    private static final int BOSS2_ATTACK_FRAME_DURATION_MS = 90;
    private static final int BOSS2_BACK_BURST_FRAME_DURATION_MS = 90;
    private static final int BOSS2_HURT_FRAME_DURATION_MS = 80;
    // Animation Stalker
    private static final int STALKER_IDLE_FRAME_DURATION_MS = 220;
    private static final int STALKER_RUN_FRAME_DURATION_MS = 100;
    private static final int STALKER_ATTACK_FRAME_DURATION_MS = 90;
    private static final int STALKER_HURT_FRAME_DURATION_MS = 80;

    private BufferedImage basefloorImage;
    private BufferedImage basewallImage;
    private BufferedImage wallTopRimImage;
    private BufferedImage wallBotRimImage;
    private BufferedImage doorOpenedImage;
    private BufferedImage doorClosedImage;
    private BufferedImage wallGrimeImage;
    private BufferedImage wallBorderBottomImage;
    private BufferedImage wallBorderLeftImage;
    private BufferedImage wallBorderRightImage;
    private BufferedImage wallBorderTopImage;
    private BufferedImage wallCornerBottomLeftImage;
    private BufferedImage wallCornerBottomRightImage;
    private BufferedImage wallCornerTopLeftImage;
    private BufferedImage wallCornerTopRightImage;

    private Map<String, BufferedImage[]> animations = new HashMap<>();

    private BufferedImage heartImage;
    private BufferedImage bulletImage;
    private BufferedImage armorImage;

    private int lastAk47Ammo = 30;
    private int lastAk47MagazineSize = 30;

    // Light System
    private final LightSystem lightSystem = new LightSystem();

    public GameRenderer() {

        // Player
        registerPlayerAnim("player_ak47", "idle", "/player/ak47/idle", 4);
        registerPlayerAnim("player_ak47", "walk", "/player/ak47/walk", 4);
        registerPlayerAnim("player_ak47", "attack", "/player/ak47/attack", 4);

        registerPlayerAnim("player_bat", "idle", "/player/bat/idle", 4);
        registerPlayerAnim("player_bat", "walk", "/player/bat/walk", 4);
        registerPlayerAnim("player_bat", "attack", "/player/bat/attack", 4);

        // Boss 3
        registerAnim("boss3", "idle", "assets/boss3/super-cool-robot-idle.png", 9);
        registerAnim("boss3", "run", "assets/boss3/super-cool-robot-run.png", 9);
        registerAnim("boss3", "fire", "assets/boss3/super-cool-robot-fire.png", 4);
        registerAnim("boss3", "jump", "assets/boss3/super-cool-robot-jump.png", 7);

        // Boss 1
        for (String state : List.of("idle", "run", "dash", "lunge", "hurt", "death")) {
            registerGifAnim("boss1", state, "assets/boss1/" + state + ".gif");
        }

        // Boss 2
        registerAnim("boss2", "idle", "assets/boss2/idle.png", 4);
        registerAnim("boss2", "run", "assets/boss2/run.png", 4);
        registerAnim("boss2", "attack", "assets/boss2/attack.png", 4);
        registerAnim("boss2", "back_burst", "assets/boss2/back_burst.png", 4);
        registerAnim("boss2", "hurt", "assets/boss2/hurt.png", 4);

        // Stalker
        registerAnim("stalker", "idle",
                "assets/creep/stalker/stalker_idle.png", 4);

        registerAnim("stalker", "run",
                "assets/creep/stalker/stalker_run.png", 4);

        registerAnim("stalker", "attack",
                "assets/creep/stalker/stalker_attack.png", 4);

        registerAnim("stalker", "hurt",
                "assets/creep/stalker/stalker_hurt.png", 4);

        // AttackCreep
        registerAnim("attack_creep", "idle",
                "assets/creep/attack_creep/idle.png", 4);

        registerAnim("attack_creep", "run",
                "assets/creep/attack_creep/run.png", 4);

        registerAnim("attack_creep", "attack",
                "assets/creep/attack_creep/attack.png", 4);

        registerAnim("attack_creep", "hurt",
                "assets/creep/attack_creep/hurt.png", 4);

        registerAnim("attack_creep", "death",
                "assets/creep/attack_creep/death.png", 4);

        // Tiles
        basefloorImage = AssetLoader.loadImage("/tiles/tile_floor_backrooms.png");
        basewallImage = AssetLoader.loadImage("/tiles/wall_wallpaper_baseboard.png");
        wallTopRimImage = AssetLoader.loadImage("/tiles/wall_wallpaper_top_rim.png");
        wallBotRimImage = AssetLoader.loadImage("/tiles/wall_wallpaper_bottom_rim.png");
        doorOpenedImage = AssetLoader.loadImage("/tiles/wall_doorway_open.png");
        doorClosedImage = AssetLoader.loadImage("/tiles/wall_doorway_close.png");
        wallGrimeImage = AssetLoader.loadImage("/tiles/wall_wallpaper_grime.png");
        wallBorderBottomImage = AssetLoader.loadImage("/tiles/wall_border_bottom.png");
        wallBorderLeftImage = AssetLoader.loadImage("/tiles/wall_border_left.png");
        wallBorderRightImage = AssetLoader.loadImage("/tiles/wall_border_right.png");
        wallBorderTopImage = AssetLoader.loadImage("/tiles/wall_border_top.png");
        wallCornerBottomLeftImage = AssetLoader.loadImage("/tiles/wall_corner_bottom_left.png");
        wallCornerBottomRightImage = AssetLoader.loadImage("/tiles/wall_corner_bottom_right.png");
        wallCornerTopLeftImage = AssetLoader.loadImage("/tiles/wall_corner_top_left.png");
        wallCornerTopRightImage = AssetLoader.loadImage("/tiles/wall_corner_top_right.png");

        armorImage = AssetLoader.loadImage("/player/informationBar/armor.png");
        heartImage = AssetLoader.loadImage("/player/informationBar/heart.png");
        bulletImage = AssetLoader.loadImage("/player/informationBar/bullet.png");
    }

    // Cắt sprite sheet thành các frame
    private BufferedImage[] loadSpriteSheet(String path, int frameCount) {
        try {
            BufferedImage sheet = ImageIO.read(new File(path));

            int frameWidth = sheet.getWidth() / frameCount;
            int frameHeight = sheet.getHeight();

            BufferedImage[] frames = new BufferedImage[frameCount];

            for (int i = 0; i < frameCount; i++) {
                frames[i] = sheet.getSubimage(
                        i * frameWidth,
                        0,
                        frameWidth,
                        frameHeight
                );
            }

            return frames;

        } catch (IOException e) {
            System.err.println("Không tìm thấy ảnh: " + path);
            return null;
        }
    }

    private void registerGifAnim(String spriteKey, String state, String path) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new File(path))) {
            if (input == null) {
                throw new IOException("Không mở được file");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IOException("Không tìm thấy bộ đọc ảnh");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, false, true);
                BufferedImage[] frames = new BufferedImage[reader.getNumImages(true)];
                for (int i = 0; i < frames.length; i++) {
                    frames[i] = reader.read(i);
                }
                animations.put(spriteKey + "_" + state, frames);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            System.err.println("Không đọc được GIF " + path + " - " + e.getMessage());
        }
    }

    // Đăng ký animation
    private void registerAnim(String spriteKey, String state, String path, int frameCount) {
        BufferedImage[] frames = loadSpriteSheet(path, frameCount);

        if (frames != null) {
            animations.put(spriteKey + "_" + state, frames);
        }
    }

    private void registerPlayerAnim(String spriteKey, String state, String path, int frameCount) {

        BufferedImage[] frames = new BufferedImage[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = AssetLoader.loadImage(path + "_" + i + ".png");

            if (frames[i] == null) {
                return;
            }
        }

        animations.put(spriteKey + "_" + state, frames);
    }

    // =========================
    // RENDER OVERLOADS
    // =========================

    public void render(Graphics2D g2, Player player, Room room) {
        render(g2, player, room, null);
    }

    public void render(Graphics2D g2, Player player, Room room, WorldMap worldMap) {
        render(g2, player, room, worldMap, Collections.emptyList());
    }

    public void render(Graphics2D g2, Player player, Room room, WorldMap worldMap,
                       List<Projectile> projectiles) {

        render(g2, player, room, worldMap,
                projectiles, Collections.emptyList());
    }

    // Tương thích với cách gọi render cũ
    public void render(Graphics2D g2, Player player, Room room, WorldMap worldMap,
                       List<Projectile> projectiles,
                       List<AcidProjectile> acidProjectiles) {

        render(g2, player, room, worldMap,
                projectiles, acidProjectiles, Collections.emptyList());
    }

    // Render đầy đủ: đạn Player, đạn axit và vệt axit
    public void render(Graphics2D g2, Player player, Room room, WorldMap worldMap,
                       List<Projectile> projectiles,
                       List<AcidProjectile> acidProjectiles,
                       List<AcidTrail> acidTrails) {

        List<Enemy> enemies = (room != null && room.getEnemies() != null)
                ? room.getEnemies()
                : Collections.emptyList();

        renderScene(
                g2,
                player,
                enemies,
                room,
                worldMap,
                projectiles,
                acidProjectiles,
                acidTrails
        );
    }

    // =========================
    // PLAYER HUD
    // =========================

    private void renderPlayerStats(Graphics2D g2, Player player) {

        String msg = player.getStatusMessage();

        if (!msg.isEmpty()) {
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(new Color(135, 206, 250));
            g2.drawString(msg, (int) player.getX() - 50, (int) player.getY() - 15);
        }

        Graphics2D hud = (Graphics2D) g2.create();

        try {
            hud.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF
            );

            int x = 10;
            int y = 10;
            int width = 200;
            int height = 80;

            hud.setColor(new Color(70, 58, 43));
            hud.fillRoundRect(x, y, width, height, 8, 8);

            hud.setColor(new Color(180, 145, 95));
            hud.fillRoundRect(x + 4, y + 4, width - 8, height - 8, 5, 5);

            hud.setColor(new Color(215, 196, 150));
            hud.drawRoundRect(x + 4, y + 4, width - 9, height - 9, 5, 5);

            hud.setFont(new Font("Minecraftia", Font.BOLD, 18));

            // HP
            if (heartImage != null) {
                hud.drawImage(heartImage, x + 10, y + 10, 28, 28, null);
            }

            hud.setColor(Color.BLACK);
            hud.drawString(
                    player.getHp() + " / " + player.getMaxHp(),
                    x + 43,
                    y + 29
            );

            int hpBarX = x + 128;
            int hpBarY = y + 13;
            int hpBarWidth = 58;
            int hpBarHeight = 16;

            double hpRatio = player.getMaxHp() <= 0
                    ? 0
                    : (double) player.getHp() / player.getMaxHp();

            hud.setColor(new Color(45, 32, 25));
            hud.fillRect(hpBarX, hpBarY, hpBarWidth, hpBarHeight);

            hud.setColor(new Color(170, 35, 25));
            hud.fillRect(
                    hpBarX + 2,
                    hpBarY + 2,
                    (int) ((hpBarWidth - 4) * hpRatio),
                    hpBarHeight - 4
            );

            // Armor
            hud.setColor(Color.BLACK);

            if (armorImage != null) {
                hud.drawImage(armorImage, x + 10, y + 43, 28, 28, null);
            }

            hud.drawString(String.valueOf(player.getArmor()), x + 43, y + 64);

            // Ammo
            if (player.getCurrentWeapon() instanceof Ak47 ak47) {
                lastAk47Ammo = ak47.getAmmo();
                lastAk47MagazineSize = ak47.getMagazineSize();
            }

            if (bulletImage != null) {
                hud.drawImage(bulletImage, x + 100, y + 33, 50, 50, null);
            }

            hud.drawString(
                    lastAk47Ammo + " / " + lastAk47MagazineSize,
                    x + 140,
                    y + 64
            );

        } finally {
            hud.dispose();
        }
    }

    // =========================
    // MINIMAP
    // =========================

    private void renderMiniMap(Graphics2D g2, WorldMap worldMap) {

        int cellSize = 15;
        int gap = 3;
        int gridSize = WorldMap.GRID_SIZE;

        int mapWidth = gridSize * cellSize + (gridSize - 1) * gap;
        int mapHeight = gridSize * cellSize + (gridSize - 1) * gap;

        int startX = MainGame.WINDOW_WIDTH - mapWidth - 18;
        int startY = 18;

        Graphics2D mini = (Graphics2D) g2.create();

        try {
            mini.setColor(new Color(20, 20, 25, 220));
            mini.fillRoundRect(
                    startX - 8,
                    startY - 8,
                    mapWidth + 16,
                    mapHeight + 16,
                    12,
                    12
            );

            for (int gx = 0; gx < gridSize; gx++) {
                for (int gy = 0; gy < gridSize; gy++) {

                    Room room = worldMap.getRoom(gx, gy);

                    if (room == null) {
                        continue;
                    }

                    int x = startX + gx * (cellSize + gap);
                    int y = startY + gy * (cellSize + gap);

                    if (room == worldMap.getCurrentRoom()) {
                        mini.setColor(new Color(78, 180, 220, 210));
                    } else if (room.isVisited()) {
                        mini.setColor(new Color(145, 145, 145, 170));
                    } else {
                        mini.setColor(new Color(45, 45, 55, 170));
                    }

                    mini.fillRoundRect(x, y, cellSize, cellSize, 2, 2);

                    if (room.getRoomType() == Room.RoomType.BOSS) {
                        mini.setColor(Color.RED);
                        mini.fillOval(x + 3, y + 3, cellSize - 6, cellSize - 6);
                    }

                    if (room.getRoomType() == Room.RoomType.SPAWN) {
                        mini.setColor(Color.YELLOW);
                        mini.fillOval(x + 3, y + 3, cellSize - 6, cellSize - 6);
                    }

                    if (room.getRoomType() != Room.RoomType.BOSS
                            && !room.getEnemies().isEmpty()) {
                        mini.setColor(new Color(220, 80, 220));
                        mini.fillOval(x + 5, y + 5, cellSize - 10, cellSize - 10);
                    }
                }
            }

        } finally {
            mini.dispose();
        }
    }

    // =========================
    // BACKGROUND
    // =========================

    private void renderBackground(Graphics2D g2) {
        g2.setColor(new Color(25, 25, 30));
        g2.fillRect(0, 0, MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT);
    }

    // =========================
    // PLAYER
    // =========================

    private void renderPlayer(Graphics2D g2, Player player) {

        int x = (int) player.getX();
        int y = (int) player.getY();

        int w = player.getWidth();
        int h = player.getHeight();

        String weaponName = player.getCurrentWeapon() == null
                ? "AK47"
                : player.getCurrentWeapon().getName();

        String spriteKey = "Bat".equalsIgnoreCase(weaponName)
                ? "player_bat"
                : "player_ak47";

        String state = player.isAttacking()
                ? "attack"
                : player.isMoving() ? "walk" : "idle";

        BufferedImage[] frames = animations.get(spriteKey + "_" + state);

        int frameDuration = player.isAttacking()
                ? PLAYER_ATTACK_FRAME_DURATION_MS
                : player.isMoving()
                ? PLAYER_WALK_FRAME_DURATION_MS
                : PLAYER_IDLE_FRAME_DURATION_MS;

        if (frames != null && frames.length > 0) {

            long animationTime = player.isAttacking()
                    ? System.currentTimeMillis() - player.getAttackAnimationStartTime()
                    : System.currentTimeMillis();

            int frameIndex = (int) ((animationTime / frameDuration) % frames.length);

            BufferedImage frame = frames[frameIndex];

            int drawHeight = h;
            int drawWidth = Math.round(
                    (float) frame.getWidth() * drawHeight / frame.getHeight()
            );

            int drawX = x + (w - drawWidth) / 2;

            if (player.isFacingLeft()) {
                g2.drawImage(
                        frame,
                        drawX + drawWidth,
                        y,
                        -drawWidth,
                        drawHeight,
                        null
                );
            } else {
                g2.drawImage(frame, drawX, y, drawWidth, drawHeight, null);
            }

        } else {
            g2.setColor(new Color(52, 152, 219));
            g2.fillOval(x, y, w, h);
        }
    }

    // =========================
    // PLAYER PROJECTILES
    // =========================

    private void renderProjectiles(Graphics2D g2, List<Projectile> projectiles) {

        if (projectiles == null) return;

        g2.setColor(new Color(255, 220, 90));

        for (Projectile projectile : projectiles) {

            if (projectile != null && projectile.isActive()) {

                Rectangle bounds = projectile.getBounds();

                g2.fillOval(
                        bounds.x,
                        bounds.y,
                        bounds.width,
                        bounds.height
                );
            }
        }
    }

    // =========================
    // BOSS2 ACID PROJECTILES
    // =========================

    private void renderAcidProjectiles(
            Graphics2D g2,
            List<AcidProjectile> acidProjectiles) {

        if (acidProjectiles == null) return;

        for (AcidProjectile acid : acidProjectiles) {

            if (acid == null || !acid.isActive()) continue;

            Rectangle bounds = acid.getBounds();

            Graphics2D acidGraphics = (Graphics2D) g2.create();

            try {
                acidGraphics.setColor(new Color(50, 255, 50, 70));
                acidGraphics.fillOval(
                        bounds.x - 5,
                        bounds.y - 5,
                        bounds.width + 10,
                        bounds.height + 10
                );

                acidGraphics.setColor(new Color(80, 220, 30));
                acidGraphics.fillOval(
                        bounds.x,
                        bounds.y,
                        bounds.width,
                        bounds.height
                );

                acidGraphics.setColor(new Color(190, 255, 80));
                acidGraphics.fillOval(
                        bounds.x + 3,
                        bounds.y + 3,
                        Math.max(2, bounds.width - 6),
                        Math.max(2, bounds.height - 6)
                );

            } finally {
                acidGraphics.dispose();
            }
        }
    }

    // =========================
    // BOSS2 ACID TRAILS
    // =========================

    private void renderAcidTrails(Graphics2D g2, List<AcidTrail> acidTrails) {

        if (acidTrails == null) return;

        for (AcidTrail trail : acidTrails) {

            if (trail == null || trail.isExpired()) continue;

            Rectangle bounds = trail.getBounds();

            Graphics2D acidGraphics = (Graphics2D) g2.create();

            try {
                // Quầng sáng bên ngoài
                acidGraphics.setColor(new Color(50, 255, 30, 35));
                acidGraphics.fillOval(
                        bounds.x - 5,
                        bounds.y - 5,
                        bounds.width + 10,
                        bounds.height + 10
                );

                // Vệt axit chính
                acidGraphics.setColor(new Color(80, 220, 30, 150));
                acidGraphics.fillOval(
                        bounds.x,
                        bounds.y,
                        bounds.width,
                        bounds.height
                );

                // Phần lõi
                acidGraphics.setColor(new Color(170, 255, 60, 180));
                acidGraphics.fillOval(
                        bounds.x + 8,
                        bounds.y + 8,
                        bounds.width - 16,
                        bounds.height - 16
                );

            } finally {
                acidGraphics.dispose();
            }
        }
    }

    // =========================
    // ENEMY
    // =========================

    private int getBoss1FrameIndex(Boss1 boss, String state, int frameCount) {
        int frameDuration = switch (state) {
            case "dash" -> BOSS1_DASH_FRAME_DURATION_MS;
            case "lunge" -> BOSS1_LUNGE_FRAME_DURATION_MS;
            case "hurt" -> BOSS1_HURT_FRAME_DURATION_MS;
            case "death" -> BOSS1_DEATH_FRAME_DURATION_MS;
            default -> BOSS1_LOOP_FRAME_DURATION_MS;
        };
        if ("idle".equals(state) || "run".equals(state)) {
            return (int) ((System.currentTimeMillis() / frameDuration) % frameCount);
        }
        return (int) Math.min(boss.getAnimationElapsedMillis() / frameDuration, frameCount - 1);
    }

    private void renderEnemy(Graphics2D g2, Enemy enemy) {
        
        int x = (int) enemy.getX();
        int y = (int) enemy.getY();

        int w = enemy.getWidth();
        int h = enemy.getHeight();

        String key = enemy.getSpritePath();

        if (key != null) {

            String state = enemy.getAnimState();

            BufferedImage[] frames = animations.get(key + "_" + state);

            if (frames == null) {
                frames = animations.get(key + "_idle");
            }

            if (frames != null && frames.length > 0) {

                int frameDuration = 100;

                // Animation Boss 2 và Minion
                if ("boss2".equals(key)) {

                    switch (state) {
                        case "idle":
                            frameDuration = BOSS2_IDLE_FRAME_DURATION_MS;
                            break;

                        case "run":
                            frameDuration = BOSS2_RUN_FRAME_DURATION_MS;
                            break;

                        case "attack":
                            frameDuration = BOSS2_ATTACK_FRAME_DURATION_MS;
                            break;

                        case "back_burst":
                            frameDuration = BOSS2_BACK_BURST_FRAME_DURATION_MS;
                            break;

                        case "hurt":
                            frameDuration = BOSS2_HURT_FRAME_DURATION_MS;
                            break;

                        default:
                            frameDuration = BOSS2_IDLE_FRAME_DURATION_MS;
                            break;
                    }
                }

                // Animation Stalker
                else if ("stalker".equals(key)) {

                    switch (state) {
                        case "idle":
                            frameDuration = STALKER_IDLE_FRAME_DURATION_MS;
                            break;

                        case "run":
                            frameDuration = STALKER_RUN_FRAME_DURATION_MS;
                            break;

                        case "attack":
                            frameDuration = STALKER_ATTACK_FRAME_DURATION_MS;
                            break;

                        case "hurt":
                            frameDuration = STALKER_HURT_FRAME_DURATION_MS;
                            break;

                        default:
                            frameDuration = STALKER_IDLE_FRAME_DURATION_MS;
                            break;
                    }
                }

                int frameIndex = enemy instanceof Boss1 boss
                        ? getBoss1FrameIndex(boss, state, frames.length)
                        : (int) ((System.currentTimeMillis() / frameDuration) % frames.length);

                if (enemy.getFacingX() >= 0) {

                    g2.drawImage(
                            frames[frameIndex],
                            x,
                            y,
                            w,
                            h,
                            null
                    );

                } else {

                    g2.drawImage(
                            frames[frameIndex],
                            x + w,
                            y,
                            -w,
                            h,
                            null
                    );
                }

                return;
            }
        }

        BufferedImage[] frames = AssetLoader.getSpriteSheet(
                enemy.getSpritePath(),
                enemy.getSpriteFrameCount()
        );

        if (frames != null && frames.length > 0) {

            int frameIndex;
            if (enemy instanceof Boss4 b4 && b4.isDying()) {
                frameIndex = Math.min(b4.getDeathFrameIndex(), frames.length - 1);
            } else if (enemy instanceof model.creep.Duck duck && duck.isDying()) {
                frameIndex = Math.min(duck.getDeathFrameIndex(), frames.length - 1);
            } else {
                frameIndex = (int) ((System.currentTimeMillis() / 100) % frames.length);
            }

            BufferedImage frame = frames[frameIndex];
            int drawHeight = h;
            int drawWidth = Math.round((float) frame.getWidth() * drawHeight / frame.getHeight());
            int drawX = x + (w - drawWidth) / 2;

            if (enemy.getFacingX() < 0) {
                g2.drawImage(frame, drawX + drawWidth, y, -drawWidth, drawHeight, null);
            } else {
                g2.drawImage(frame, drawX, y, drawWidth, drawHeight, null);
            }

            return;
        }

        // Fallback khi không có sprite
        g2.setColor(new Color(231, 76, 60));
        g2.fillOval(x, y, w, h);

        renderFacingMarker(g2, enemy);
    }

    // =========================
    // HITBOX
    // =========================

    private void renderHitbox(Graphics2D g2, Combatant combatant) {

        double radius = combatant.getHitboxRadius();
        double diameter = radius * 2;

        double centerX = combatant.getX() + combatant.getWidth() / 2.0;
        double centerY = combatant.getY() + combatant.getHeight() / 2.0;

        Ellipse2D hitbox = new Ellipse2D.Double(
                centerX - radius,
                centerY - radius,
                diameter,
                diameter
        );

        Graphics2D debug = (Graphics2D) g2.create();

        try {
            debug.setColor(new Color(255, 230, 0, 28));
            debug.fill(hitbox);

            debug.setColor(Color.YELLOW);
            debug.setStroke(new BasicStroke(2f));
            debug.draw(hitbox);

        } finally {
            debug.dispose();
        }
    }

    // =========================
    // DETECTION RANGE
    // =========================

    private void renderDetectionRange(
            Graphics2D g2,
            Combatant combatant,
            double range,
            Color fillColor,
            Color outlineColor) {

        if (Double.isNaN(range) || range < 0) {
            return;
        }

        Graphics2D debug = (Graphics2D) g2.create();

        try {
            debug.setStroke(new BasicStroke(2f));

            if (Double.isInfinite(range)) {

                Rectangle viewport = new Rectangle(
                        0,
                        0,
                        MainGame.WINDOW_WIDTH,
                        MainGame.WINDOW_HEIGHT
                );

                debug.setColor(fillColor);
                debug.fillRect(
                        viewport.x,
                        viewport.y,
                        viewport.width,
                        viewport.height
                );

                debug.setColor(outlineColor);
                debug.drawRect(
                        viewport.x + 1,
                        viewport.y + 1,
                        Math.max(0, viewport.width - 3),
                        Math.max(0, viewport.height - 3)
                );

                return;
            }

            double centerX = combatant.getX() + combatant.getWidth() / 2.0;
            double centerY = combatant.getY() + combatant.getHeight() / 2.0;

            double diameter = range * 2;

            Ellipse2D detectionArea = new Ellipse2D.Double(
                    centerX - range,
                    centerY - range,
                    diameter,
                    diameter
            );

            debug.setColor(fillColor);
            debug.fill(detectionArea);

            debug.setColor(outlineColor);
            debug.draw(detectionArea);

        } finally {
            debug.dispose();
        }
    }

    // =========================
    // ENEMY FACING MARKER
    // =========================

    private void renderFacingMarker(Graphics2D g2, Combatant combatant) {

        double centerX = combatant.getX() + combatant.getWidth() / 2.0;
        double centerY = combatant.getY() + combatant.getHeight() / 2.0;

        double markerOffset = Math.min(
                combatant.getWidth(),
                combatant.getHeight()
        ) * 0.25;

        double markerCenterX = centerX + combatant.getFacingX() * markerOffset;
        double markerCenterY = centerY + combatant.getFacingY() * markerOffset;

        int eyeSize = Math.max(
                6,
                Math.min(combatant.getWidth(), combatant.getHeight()) / 5
        );

        int pupilSize = Math.max(3, eyeSize / 2);

        g2.setColor(Color.WHITE);

        g2.fillOval(
                (int) Math.round(markerCenterX - eyeSize / 2.0),
                (int) Math.round(markerCenterY - eyeSize / 2.0),
                eyeSize,
                eyeSize
        );

        g2.setColor(Color.BLACK);

        g2.fillOval(
                (int) Math.round(
                        markerCenterX
                                + combatant.getFacingX() * eyeSize * 0.15
                                - pupilSize / 2.0
                ),
                (int) Math.round(
                        markerCenterY
                                + combatant.getFacingY() * eyeSize * 0.15
                                - pupilSize / 2.0
                ),
                pupilSize,
                pupilSize
        );
    }

    // =========================
    // ROOM
    // =========================

    private static final Color WALL_COLOR = new Color(37, 37, 37);
    private static final Color FLOOR_COLOR = new Color(82, 82, 82);
    private static final Color DOOR_OPEN_COLOR = new Color(20, 20, 25);
    private static final Color DOOR_CLOSED_COLOR = new Color(139, 128, 0);

    private void renderRoom(Graphics2D g2, Room room) {

        int tileSize = Room.TILE_SIZE;

        for (int x = 0; x < room.getWidth(); x++) {

            for (int y = 0; y < room.getHeight(); y++) {

                TileType type = room.getTile(x, y);

                int px = x * tileSize;
                int py = y * tileSize;

                switch (type) {

                    case WALL:
                        renderWallTile(g2, room, x, y, px, py, tileSize);
                        break;

                    case FLOOR, DOOR_OPEN:

                        if (basefloorImage != null) {
                            g2.drawImage(
                                    basefloorImage,
                                    px,
                                    py,
                                    tileSize,
                                    tileSize,
                                    null
                            );
                        } else {
                            g2.setColor(FLOOR_COLOR);
                            g2.fillRect(px, py, tileSize, tileSize);
                        }

                        break;

                    case DOOR_CLOSED:

                        if (doorClosedImage != null) {
                            g2.drawImage(
                                    doorClosedImage,
                                    px,
                                    py,
                                    tileSize,
                                    tileSize,
                                    null
                            );
                        } else {
                            g2.setColor(DOOR_CLOSED_COLOR);
                            g2.fillRect(px, py, tileSize, tileSize);
                        }

                        break;

                    case PORTAL:
                        g2.setColor(new Color(125, 80, 180));
                        g2.fillRect(px, py, tileSize, tileSize);
                        break;

                    case VOID:
                        g2.setColor(Color.BLACK);
                        g2.fillRect(px, py, tileSize, tileSize);
                        break;
                }
            }
        }
    }

    // =========================
    // BOSS 3
    // =========================

    private void renderBoss3Lasers(Graphics2D g2, Boss3 boss) {

        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();

        double centerX = boss.getX() + boss.getWidth() / 2.0;
        double centerY = boss.getY() + boss.getHeight() / 2.0;

        double baseAngle = boss.getLaserBaseAngle();
        double length = boss.getLaserLength();

        int count = boss.getLaserCount();

        for (int i = 0; i < count; i++) {

            double angle = baseAngle + i * (Math.PI / 2.0);

            int endX = (int) (centerX + Math.cos(angle) * length);
            int endY = (int) (centerY + Math.sin(angle) * length);

            g2.setColor(new Color(255, 50, 50, 120));
            g2.setStroke(
                    new BasicStroke(
                            12f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            g2.drawLine(
                    (int) centerX,
                    (int) centerY,
                    endX,
                    endY
            );

            g2.setColor(Color.WHITE);
            g2.setStroke(
                    new BasicStroke(
                            4f,
                            BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND
                    )
            );

            g2.drawLine(
                    (int) centerX,
                    (int) centerY,
                    endX,
                    endY
            );
        }

        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }

    private void renderBoss3Healing(Graphics2D g2, Boss3 boss) {

        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();

        int padding = 10;

        int x = (int) boss.getX() - padding;
        int y = (int) boss.getY() - padding;

        int w = boss.getWidth() + padding * 2;
        int h = boss.getHeight() + padding * 2;

        g2.setColor(new Color(46, 204, 113, 70));
        g2.fillOval(x, y, w, h);

        g2.setColor(new Color(46, 204, 113, 220));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(x, y, w, h);

        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }

    // =========================
    // WALL HELPERS
    // =========================

    private boolean isWalkable(Room room, int x, int y) {

        if (room == null
                || x < 0
                || x >= room.getWidth()
                || y < 0
                || y >= room.getHeight()) {

            return false;
        }

        TileType type = room.getTile(x, y);

        return type == TileType.FLOOR
                || type == TileType.DOOR_OPEN
                || type == TileType.DOOR_CLOSED;
    }

    private void renderWallTile(
            Graphics2D g2,
            Room room,
            int x,
            int y,
            int px,
            int py,
            int tileSize) {

        int w = room.getWidth();
        int padX = room.getPadX();
        int padY = room.getPadY();

        BufferedImage img = null;

        Direction sideDir = (x <= padX + 1)
                ? Direction.WEST
                : Direction.EAST;

        BufferedImage sideDoorTrimImage = null;
        for (int doorOffset : room.getDoorOffsets(sideDir)) {
            if (!isWalkable(room, x, doorOffset)) {
                continue;
            }
            if (y == doorOffset - 3) {
                sideDoorTrimImage = wallTopRimImage;
                break;
            }
            if (y == doorOffset - 2) {
                sideDoorTrimImage = wallBotRimImage;
                break;
            }
        }

        // North Wall Decorative Wallpaper
        if (y >= padY + 1
                && y <= padY + 3
                && x >= padX + 2
                && x <= w - padX - 3) {

            int relY = y - padY;

            switch (relY) {
                case 1:
                    img = wallTopRimImage != null
                            ? wallTopRimImage
                            : wallBorderTopImage;
                    break;

                case 2:
                    img = basewallImage;
                    break;

                case 3:
                    img = wallBotRimImage;
                    break;
            }

        } else if (((x <= padX + 1) || (x >= w - padX - 2))
                && sideDoorTrimImage != null) {
            img = sideDoorTrimImage;

        } else {

            boolean openNorth = isWalkable(room, x, y - 1);
            boolean openSouth = isWalkable(room, x, y + 1);

            boolean openWest = isWalkable(room, x - 1, y)
                    || (y >= padY + 1
                    && y <= padY + 3
                    && x == w - padX - 2);

            boolean openEast = isWalkable(room, x + 1, y)
                    || (y >= padY + 1
                    && y <= padY + 3
                    && x == padX + 1);

            if (openNorth && openWest) {
                img = wallCornerTopLeftImage;
            } else if (openNorth && openEast) {
                img = wallCornerTopRightImage;
            } else if (openSouth && openWest) {
                img = wallCornerBottomLeftImage;
            } else if (openSouth && openEast) {
                img = wallCornerBottomRightImage;
            } else if (openWest) {
                img = wallBorderLeftImage;
            } else if (openEast) {
                img = wallBorderRightImage;
            } else if (openNorth) {
                img = wallBorderTopImage;
            } else if (openSouth) {
                img = wallBorderBottomImage;
            }
        }

        if (img != null) {
            g2.drawImage(img, px, py, tileSize, tileSize, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(px, py, tileSize, tileSize);
        }
    }

    // =========================
    // LIGHT SYSTEM
    // =========================

    public LightSystem getLightSystem() {
        return lightSystem;
    }

    // =========================
    // RENDER SINGLE ENEMY
    // =========================

    public void render(
            Graphics2D g2,
            Player player,
            Enemy enemy,
            Room room,
            WorldMap worldMap) {

        List<Enemy> enemies = (enemy != null)
                ? Collections.singletonList(enemy)
                : Collections.emptyList();

        renderScene(
                g2,
                player,
                enemies,
                room,
                worldMap,
                null,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    // =========================
    // MAIN RENDER SCENE
    // =========================

    private void renderScene(
            Graphics2D g2,
            Player player,
            List<Enemy> enemies,
            Room room,
            WorldMap worldMap,
            List<Projectile> projectiles,
            List<AcidProjectile> acidProjectiles,
            List<AcidTrail> acidTrails) {

        // 1. Nền tối ngoài bản đồ
        g2.setColor(Color.BLACK);
        g2.fillRect(
                0,
                0,
                MainGame.WINDOW_WIDTH,
                MainGame.WINDOW_HEIGHT
        );

        if (room == null || player == null) {
            return;
        }

        int roomPxWidth = room.getWidth() * Room.TILE_SIZE;
        int roomPxHeight = room.getHeight() * Room.TILE_SIZE;

        int camX = roomPxWidth <= MainGame.WINDOW_WIDTH
                ? -(MainGame.WINDOW_WIDTH - roomPxWidth) / 2
                : (int) Math.max(
                0,
                Math.min(
                        player.getX() - MainGame.WINDOW_WIDTH / 2,
                        roomPxWidth - MainGame.WINDOW_WIDTH
                )
        );

        int camY = roomPxHeight <= MainGame.WINDOW_HEIGHT
                ? -(MainGame.WINDOW_HEIGHT - roomPxHeight) / 2
                : (int) Math.max(
                0,
                Math.min(
                        player.getY() - MainGame.WINDOW_HEIGHT / 2,
                        roomPxHeight - MainGame.WINDOW_HEIGHT
                )
        );

        // 2. Dịch chuyển camera
        g2.translate(-camX, -camY);

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // 3. Vẽ phòng
        renderRoom(g2, room);

        // 4. Vẽ vệt axit trên mặt đất
        renderAcidTrails(g2, acidTrails);

        // 5. Vẽ toàn bộ Enemy và Boss
        for (Enemy enemy : enemies) {

            if (enemy == null) {
                continue;
            }
           if (enemy.isDead()) {
    if (enemy instanceof Boss1 boss && boss.isPlayingDeathAnimation()) {
        renderEnemy(g2, boss);
    } else if (enemy instanceof model.creep.AttackCreep creep
            && creep.isPlayingDeathAnimation()) {
        renderEnemy(g2, creep);
    }

    continue;
}

            boolean boss4Hidden = enemy instanceof Boss4
                    && ((Boss4) enemy).isSubmergedInWall();

            if (!boss4Hidden) {
                renderDetectionRange(
                        g2,
                        enemy,
                        enemy.getDetectionRange(),
                        new Color(0, 255, 0, 22),
                        new Color(0, 255, 0, 190)
                );
            }

            if (enemy instanceof Boss3) {

                Boss3 boss3 = (Boss3) enemy;

                if (boss3.isFiringLaser()) {
                    renderBoss3Lasers(g2, boss3);
                }
            }

            if (enemy instanceof Boss4) {
                renderBoss4SafeZone(g2, (Boss4) enemy);
            }

            if (boss4Hidden) {
                renderBoss4WallShadow(g2, (Boss4) enemy);
            } else {
                renderEnemy(g2, enemy);
            }

            if (enemy instanceof Boss3) {

                Boss3 boss3 = (Boss3) enemy;

                if (boss3.isHealing()) {
                    renderBoss3Healing(g2, boss3);
                }
            }

            if (enemy instanceof Boss4) {
                renderBoss4Arm(g2, (Boss4) enemy);
            }
        }

        // 6. Vẽ Player
        if (!player.isDead()) {

            renderDetectionRange(
                    g2,
                    player,
                    player.getDetectionRange(),
                    new Color(52, 152, 219, 22),
                    new Color(52, 152, 219, 190)
            );

            renderPlayer(g2, player);
        }

        // 7. Vẽ đạn
        renderProjectiles(g2, projectiles);
        renderAcidProjectiles(g2, acidProjectiles);

        // 8. Vẽ hitbox
        for (Enemy enemy : enemies) {

            if (enemy == null || enemy.isDead()) {
                continue;
            }

            boolean boss4Hidden = enemy instanceof Boss4
                    && ((Boss4) enemy).isSubmergedInWall();

            if (!boss4Hidden) {
                renderHitbox(g2, enemy);
            }
        }

        if (!player.isDead()) {
            renderHitbox(g2, player);
        }

        // 9. Reset camera trước khi vẽ HUD và minimap
        g2.translate(camX, camY);

        // 10. Ánh sáng
        lightSystem.render(g2, player, room, camX, camY);

        // 11. Minimap và HUD
        if (worldMap != null) {
            renderMiniMap(g2, worldMap);
        }

        renderPlayerStats(g2, player);
    }

    // =========================
    // BOSS 4
    // =========================

    private void renderBoss4SafeZone(Graphics2D g2, Boss4 b4) {

        if (!b4.isSafeZoneActive()) {
            return;
        }

        Color oldColor = g2.getColor();

        int r = (int) b4.getSafeZoneRadius();

        g2.setColor(new Color(100, 200, 255, 55));

        g2.fillOval(
                (int) b4.getSafeZoneX() - r,
                (int) b4.getSafeZoneY() - r,
                r * 2,
                r * 2
        );

        g2.setColor(oldColor);
    }

    private void renderBoss4WallShadow(Graphics2D g2, Boss4 b4) {

        Color oldColor = g2.getColor();

        int cx = (int) (b4.getX() + b4.getWidth() / 2.0);
        int cy = (int) (b4.getY() + b4.getHeight() / 2.0);

        int pulse = (int) (Math.sin(System.currentTimeMillis() / 120.0) * 5);
        int r = 24 + pulse;

        g2.setColor(new Color(15, 10, 25, 150));
        g2.fillOval(cx - r, cy - r, r * 2, r * 2);

        g2.setColor(new Color(90, 20, 40, 180));
        g2.drawOval(cx - r, cy - r, r * 2, r * 2);

        g2.setColor(oldColor);
    }

    private void renderBoss4Arm(Graphics2D g2, Boss4 b4) {

        if (!b4.isArmActive()) {
            return;
        }

        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();

        int sx = (int) b4.getArmStartX();
        int sy = (int) b4.getArmStartY();

        int ex = (int) b4.getArmEndX();
        int ey = (int) b4.getArmEndY();

        g2.setColor(new Color(140, 40, 60, 180));

        g2.setStroke(
                new BasicStroke(
                        12f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        g2.drawLine(sx, sy, ex, ey);

        g2.setColor(new Color(25, 20, 35));

        g2.setStroke(
                new BasicStroke(
                        8f,
                        BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND
                )
        );

        g2.drawLine(sx, sy, ex, ey);
        g2.fillOval(ex - 10, ey - 10, 20, 20);

        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }
}
