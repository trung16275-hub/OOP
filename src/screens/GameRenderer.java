
package screens;

import game.MainGame;
import map.*;
import model.Combatant;
import model.Enemy;
import model.Projectile;
import model.AcidProjectile;

import model.prop.AmmoPickup;
import model.prop.LiquidPain;
import model.prop.Prop;
import model.weapon.Ak47;

import model.boss.Boss1;
import model.boss.Boss2;
import model.boss.Boss3;
import model.boss.Boss4;
import model.boss.AcidTrail;

import model.player.Player;
import screens.lighting.*;
import screens.lighting.LightSystem;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

public class GameRenderer {

    // Thời gian hiển thị mỗi frame
    private static final int PLAYER_IDLE_FRAME_DURATION_MS = 250;
    private static final int PLAYER_WALK_FRAME_DURATION_MS = 190;
    private static final int PLAYER_ATTACK_FRAME_DURATION_MS = 80;
    private static final int PLAYER_HURT_FRAME_DURATION_MS = 80;
    private static final int PLAYER_DEATH_FRAME_DURATION_MS = 200;

    private BufferedImage contactShadowImage;
    private BufferedImage aoNorthImage;
    private BufferedImage aoWestImage;
    private BufferedImage aoEastImage;
    private BufferedImage aoSouthImage;

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
    private BufferedImage portalDoorImage;
    private BufferedImage doorClosedImage;
    private BufferedImage wallBorderBottomImage;
    private BufferedImage wallBorderLeftImage;
    private BufferedImage wallBorderRightImage;
    private BufferedImage wallBorderTopImage;
    private BufferedImage wallCornerBottomLeftImage;
    private BufferedImage wallCornerBottomRightImage;
    private BufferedImage wallCornerTopLeftImage;
    private BufferedImage wallCornerTopRightImage;

    private final AnimationCatalog animations = AnimationCatalog.getInstance();

    private BufferedImage heartImage;
    private BufferedImage bulletImage;
    private BufferedImage armorImage;
    private final BufferedImage rifleAmmoImage = AssetLoader.loadImage(AmmoPickup.SPRITE_PATH);
    private final BufferedImage liquidPainImage = AssetLoader.loadImage(LiquidPain.SPRITE_PATH);
    private BufferedImage floorWearPatch;

    private int lastAk47Ammo = 30;
    private int lastAk47MagazineSize = 30;

    private final Map<String, BufferedImage> decalCache = new HashMap<>();


    //Light System
    private final LightSystem lightSystem = new LightSystem();

    public GameRenderer() {
        armorImage = AssetLoader.loadImage("/player/informationBar/armor.png");
        heartImage = AssetLoader.loadImage("/player/informationBar/heart.png");
        bulletImage = AssetLoader.loadImage("/player/informationBar/bullet.png");

        initShadowSprites();
        floorWearPatch = createFloorWearPatch(48);
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
        render(g2, player, room, worldMap, projectiles, 0);
    }

    void render(Graphics2D g2, Player player, Room room, WorldMap worldMap,
                List<Projectile> projectiles, long playerDeathElapsedMs) {
        render(g2, player, room, worldMap, projectiles, playerDeathElapsedMs, List.of(), List.of());
    }

    void render(Graphics2D g2, Player player, Room room, WorldMap worldMap,
                List<Projectile> projectiles, long playerDeathElapsedMs,
                List<AcidProjectile> acidProjectiles, List<AcidTrail> acidTrails) {
        // 1. Fill entire screen with the abyss/void color BEFORE camera translation
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT);

        int roomPxWidth = room.getWidth() * Room.TILE_SIZE;
        int roomPxHeight = room.getHeight() * Room.TILE_SIZE;

        int camX = roomPxWidth <= MainGame.WINDOW_WIDTH
                ? -(MainGame.WINDOW_WIDTH - roomPxWidth) / 2
                : (int) Math.max(0, Math.min(player.getX() - MainGame.WINDOW_WIDTH / 2, roomPxWidth - MainGame.WINDOW_WIDTH));
        int camY = roomPxHeight <= MainGame.WINDOW_HEIGHT
                ? -(MainGame.WINDOW_HEIGHT - roomPxHeight) / 2
                : (int) Math.max(0, Math.min(player.getY() - MainGame.WINDOW_HEIGHT / 2, roomPxHeight - MainGame.WINDOW_HEIGHT));
        // 2. Translate world
        g2.translate(-camX, -camY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 3. Render room & entities
        renderRoom(g2, room);
        renderDecals(g2, room.getFloorDecals());
        renderAcidTrails(g2, acidTrails);

        if (room.getLiquidPain() != null && liquidPainImage != null) {
            Prop prop = room.getLiquidPain().prop();
            g2.drawImage(liquidPainImage, prop.x(), prop.y(), prop.width(), prop.height(), null);
        }
        if (rifleAmmoImage != null) {
            for (AmmoPickup pickup : room.getAmmoPickups()) {
                Prop prop = pickup.prop();
                g2.drawImage(rifleAmmoImage, prop.x(), prop.y(), prop.width(), prop.height(), null);
            }
        }

        renderWallAmbientOcclusion(g2, room);

        renderEntitiesAndPropsYSorted(g2, room, player, playerDeathElapsedMs);
        renderProjectiles(g2, projectiles);
        renderAcidProjectiles(g2, acidProjectiles);

        if (player != null && !player.isDead()) {
            renderPlayerHitboxes(g2, player);
        }

        g2.translate(camX, camY); // reset before rendering HUD/minimap

        lightSystem.render(g2, player, room, camX, camY);
        g2.translate(-camX, -camY);
        for (Enemy enemy : room.getEnemies()) {
            if (enemy != null && !enemy.isDead()) {
                renderDetectionRange(g2, enemy, enemy.getDetectionRange(),
                        new Color(0, 255, 0, 22), new Color(0, 255, 0, 190), camX, camY);
                renderHitbox(g2, enemy);
            }
        }
        g2.translate(camX, camY);
        if (player != null && !player.isDead()) {
            Rectangle playerBounds = player.getMovementBounds();
            int index = room.findNearestAmmoPickup(playerBounds);
            String noteId = room.findNearbyEnemyNote(playerBounds);
            if (noteId != null && EnemyNote.find(noteId) != null) {
                renderPickupPrompt(g2, "Đọc hồ sơ");
            } else if (room.isLiquidPainNearest(playerBounds, index)) {
                renderPickupPrompt(g2, LiquidPain.DISPLAY_NAME + " · -" + LiquidPain.HP_COST + " HP");
            } else if (index >= 0) {
                AmmoPickup pickup = room.getAmmoPickups().get(index);
                renderPickupPrompt(g2, AmmoPickup.DISPLAY_NAME + " · " + pickup.rounds() + " viên");
            }
        }

        if (worldMap != null) {
            renderMiniMap(g2, worldMap);
        }
        if (player != null) {
            renderPlayerStats(g2, player);
        }

    }

    //Load theme refer to floor
    public void loadTheme(FloorTheme theme) {
        String path = theme.getAssetsPath();
        basefloorImage = AssetLoader.loadImage(path + "tile_floor_backrooms.png");
        basewallImage = AssetLoader.loadImage(path + "wall_baseboard.png");
        wallTopRimImage = AssetLoader.loadImage(path + "wall_top_rim.png");
        wallBotRimImage = AssetLoader.loadImage(path + "wall_bottom_rim.png");

        wallBorderBottomImage = AssetLoader.loadImage(path + "wall_border.png");
        wallBorderLeftImage = rotate90Clockwise(wallBorderBottomImage);
        wallBorderTopImage = rotate90Clockwise(wallBorderLeftImage);
        wallBorderRightImage = rotate90Clockwise(wallBorderTopImage);

        wallCornerBottomLeftImage = AssetLoader.loadImage(path + "wall_corner.png");
        wallCornerTopLeftImage = rotate90Clockwise(wallCornerBottomLeftImage);
        wallCornerTopRightImage = rotate90Clockwise(wallCornerTopLeftImage);
        wallCornerBottomRightImage = rotate90Clockwise(wallCornerTopRightImage);

        portalDoorImage = AssetLoader.loadImage(switch (theme) {
            case LEVEL_0_OFFICE -> "/floor1/tiles_addon/wall_elevator_door_open.png";
            case LEVEL_2_PIPEDREAM -> path + "wall_doorway_opened.png";
            case LEVEL_52_SCHOOL -> path + "wall_blast_door_open.png";
        });
        doorClosedImage = AssetLoader.loadImage(path + "wall_doorway_closed.png");

    }

    void renderEnemyNote(Graphics2D g2, EnemyNote note) {
        Graphics2D overlay = (Graphics2D) g2.create();
        try {
            overlay.setColor(new Color(0, 0, 0, 188));
            overlay.fillRect(0, 0, MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT);
            overlay.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            BufferedImage portrait = getCachedImage(note.imagePath());
            int width = portrait == null ? 700 : 900;
            int height = 270;
            int x = (MainGame.WINDOW_WIDTH - width) / 2;
            int y = (MainGame.WINDOW_HEIGHT - height) / 2;
            Color accent = new Color(46, 211, 111);
            overlay.setPaint(new GradientPaint(x, y, new Color(9, 28, 19),
                    x, y + height, new Color(3, 10, 7)));
            overlay.fillRect(x, y, width, height);
            overlay.setColor(new Color(24, 87, 53));
            overlay.drawRect(x, y, width - 1, height - 1);
            overlay.drawLine(x + 28, y + 72, x + width - 28, y + 72);
            overlay.drawLine(x + 28, y + height - 56, x + width - 28, y + height - 56);
            overlay.setPaint(new GradientPaint(x, y, new Color(46, 211, 111, 80),
                    x, y + 9, new Color(46, 211, 111, 0)));
            overlay.fillRect(x + 1, y + 1, 71, 9);
            overlay.setColor(accent);
            overlay.fillRect(x, y, 72, 3);
            overlay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 27));
            overlay.drawString(note.title(), x + 28, y + 49);

            if (portrait != null) {
                int portraitSize = 124;
                int portraitX = x + width - portraitSize - 28;
                int portraitY = y + 81;
                overlay.setPaint(new GradientPaint(portraitX, portraitY, new Color(13, 39, 25),
                        portraitX, portraitY + portraitSize, new Color(3, 9, 6)));
                overlay.fillRect(portraitX, portraitY, portraitSize, portraitSize);
                overlay.setColor(new Color(27, 104, 62));
                overlay.drawRect(portraitX, portraitY, portraitSize - 1, portraitSize - 1);
                double scale = Math.min((portraitSize - 16.0) / portrait.getWidth(),
                        (portraitSize - 16.0) / portrait.getHeight());
                int imageWidth = (int) Math.round(portrait.getWidth() * scale);
                int imageHeight = (int) Math.round(portrait.getHeight() * scale);
                overlay.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                overlay.drawImage(portrait,
                        portraitX + (portraitSize - imageWidth) / 2,
                        portraitY + (portraitSize - imageHeight) / 2,
                        imageWidth, imageHeight, null);
            }

            overlay.setColor(new Color(209, 228, 216));
            overlay.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 19));
            int lineY = y + 110;
            for (String line : note.body().split("\\R")) {
                overlay.drawString(line, x + 28, lineY);
                lineY += 28;
            }

            overlay.setColor(accent);
            overlay.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
            overlay.drawString("[F] Đóng", x + 28, y + height - 24);
        } finally {
            overlay.dispose();
        }
    }

    private void renderPickupPrompt(Graphics2D g2, String text) {
        Graphics2D prompt = (Graphics2D) g2.create();
        try {
            prompt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
            String key = "[F]";
            FontMetrics metrics = prompt.getFontMetrics();
            int infoOffset = 16 + metrics.stringWidth(key) + 14;
            int width = infoOffset + metrics.stringWidth(text) + 16;
            int height = metrics.getHeight() + 12;
            int x = (MainGame.WINDOW_WIDTH - width) / 2;
            int y = MainGame.WINDOW_HEIGHT - height - 24;
            int baseline = y + 6 + metrics.getAscent();

            prompt.setColor(new Color(0, 0, 0, 185));
            prompt.fillRect(x, y, width, height);
            prompt.setColor(new Color(95, 222, 129));
            prompt.fillRect(x, y, width, 1);
            prompt.drawString(key, x + 16, baseline);
            prompt.setColor(Color.WHITE);
            prompt.drawString(text, x + infoOffset, baseline);
        } finally {
            prompt.dispose();
        }
    }

    private void renderPlayerStats(Graphics2D g2, Player player) {
        Graphics2D hud = (Graphics2D) g2.create();

        try {
            hud.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_OFF
            );

            int x = 10;
            int y = 10;
            int width = 200;
            int height = 104;

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
            hud.drawString(lastAk47Ammo + " / " + lastAk47MagazineSize, x + 140, y + 64);

                // Dash cooldown indicator: empty while recharging, full when Q is ready.
                int dashBarX = x + 48;
                int dashBarY = y + 83;
                int dashBarWidth = 138;
                int dashBarHeight = 12;
                double dashProgress = player.getDashCooldownProgress();
                hud.setFont(new Font("Minecraftia", Font.BOLD, 12));
                hud.setColor(Color.BLACK);
                hud.setColor(new Color(45, 32, 25));
                hud.fillRoundRect(dashBarX, dashBarY, dashBarWidth, dashBarHeight, 6, 6);
                hud.setColor(dashProgress >= 1.0
                    ? new Color(65, 190, 105)
                    : new Color(70, 145, 190));
                hud.fillRoundRect(dashBarX + 2, dashBarY + 2,
                    (int) ((dashBarWidth - 4) * dashProgress), dashBarHeight - 4, 5, 5);


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

                    if (room == null || !room.isVisited()) {
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
    private String getPlayerSpriteKey(Player player) {
        String weaponName = player.getCurrentWeapon() == null ? "AK47" : player.getCurrentWeapon().getName();
        return "Bat".equalsIgnoreCase(weaponName) ? "player_bat" : "player_ak47";
    }

    int getPlayerDeathDurationMs(Player player) {
        BufferedImage[] frames = animations.getFrames(getPlayerSpriteKey(player) + ".death");
        return frames == null ? 0 : frames.length * PLAYER_DEATH_FRAME_DURATION_MS;
    }

    private void renderPlayer(Graphics2D g2, Player player, long deathElapsedMs) {
        int x = (int) player.getX();
        int y = (int) player.getY();

        int w = player.getWidth();
        int h = player.getHeight();
        String spriteKey = getPlayerSpriteKey(player);
        long now = System.currentTimeMillis();
        long hurtStartTime = player.getHurtAnimationStartTime();
        long hurtElapsed = now - hurtStartTime;
        BufferedImage[] hurtFrames = animations.getFrames(spriteKey + ".hurt");
        boolean isHurt = hurtStartTime != 0 && hurtElapsed >= 0 && hurtFrames != null
                && hurtElapsed / PLAYER_HURT_FRAME_DURATION_MS < hurtFrames.length;
        String state = player.isDead() ? "death"
                : player.isHurt() ? "hurt"
                : player.isDashing() ? "dash"
                : player.isAttacking() ? "attack"
                : player.isMoving() ? "walk" : "idle";
        String animationKey = state.equals("dash")
            ? "player.dash"
            : spriteKey + "." + state;
        BufferedImage[] frames = isHurt && !player.isDead() ? hurtFrames : animations.getFrames(animationKey);
        int frameDuration = switch (state) {
            case "attack" -> PLAYER_ATTACK_FRAME_DURATION_MS;
            case "walk" -> PLAYER_WALK_FRAME_DURATION_MS;
            default -> PLAYER_IDLE_FRAME_DURATION_MS;
        };

        if (frames != null && frames.length > 0) {
            int lastFrame = frames.length - 1;
            int frameIndex = switch (state) {
                case "death" -> (int) Math.min(deathElapsedMs / PLAYER_DEATH_FRAME_DURATION_MS, lastFrame);
                case "hurt" -> Math.min((int) ((now - player.getHurtAnimationStartTime())
                        * frames.length / Player.HURT_ANIMATION_DURATION_MS), lastFrame);
                case "dash" -> Math.min((int) ((now - player.getDashStartTime())
                        * frames.length / Player.DASH_DURATION_MS), lastFrame);
                case "attack" -> Math.min((int) ((now - player.getAttackAnimationStartTime())
                        / frameDuration), lastFrame);
                default -> (int) ((now / frameDuration) % frames.length);
            };
            BufferedImage frame = frames[frameIndex];
            int referenceHeight = player.isDead() ? frames[0].getHeight() : frame.getHeight();
            float scale = (float) h / referenceHeight;
            int drawHeight = Math.round(frame.getHeight() * scale);
            int drawWidth = Math.round(frame.getWidth() * scale);
            int drawX = x + (w - drawWidth) / 2;
            int drawY = y + h - drawHeight;

            Rectangle feet = player.getHurtBounds();
            int shadowW = Math.round(feet.width * 1.15f);
            int shadowH = Math.max(8, Math.round(shadowW * 0.35f));
            renderContactShadow(g2, feet.x + feet.width / 2, feet.y + feet.height + 2, shadowW, shadowH);


            if (player.isFacingLeft()) {
                g2.drawImage(
                        frame,
                        drawX + drawWidth,
                        drawY,
                        -drawWidth,
                        drawHeight,
                        null
                );
            } else {
                g2.drawImage(frame, drawX, drawY, drawWidth, drawHeight, null);
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

        for (Projectile projectile : projectiles) {
            if (projectile.isActive()) {
                Rectangle b = projectile.getBounds();
                renderContactShadow(g2, b.x + b.width / 2, b.y + b.height + 16, b.width + 4, Math.max(4, b.height / 2));
                g2.setColor(new Color(255, 220, 90));
                g2.fillOval(b.x, b.y, b.width, b.height);
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

        String key = enemy.getSpriteKey();
        if (key == null) {
            key = enemy.getSpritePath();
        }

        int shadowW = Math.round(w * 0.65f);
        int shadowH = Math.max(8, Math.round(shadowW * 0.35f));
        renderContactShadow(g2, x + w / 2, y + h - 3, shadowW, shadowH);


        if (key != null) {

            String state = enemy.getAnimState();

            BufferedImage[] frames = animations.getFrames(key + "." + state);

            if (frames == null) {
                frames = animations.getFrames(key + ".idle");
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

    private void renderPlayerHitboxes(Graphics2D g2, Player player) {
        Graphics2D debug = (Graphics2D) g2.create();
        try {
            Rectangle movementBounds = player.getMovementBounds();
            Rectangle hurtBounds = player.getHurtBounds();

            debug.setStroke(new BasicStroke(2f));
            debug.setColor(new Color(52, 220, 255, 210));
            debug.draw(movementBounds);
            debug.setColor(new Color(255, 80, 80, 220));
            debug.draw(hurtBounds);
        } finally {
            debug.dispose();
        }
    }

    private void renderDetectionRange(Graphics2D g2, Combatant combatant, double range,
                                      Color fillColor, Color outlineColor, int camX, int camY) {
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

                    case FLOOR, DOOR_OPEN, PORTAL:

                        if (type == TileType.PORTAL && portalDoorImage == null) {
                            g2.setColor(new Color(125, 80, 180));
                            g2.fillRect(px, py, tileSize, tileSize);
                            break;
                        }

                        if (basefloorImage != null) {
                            g2.drawImage(basefloorImage, px, py, tileSize, tileSize, null);
                            renderFloorVariation(g2, room, x, y, px, py, tileSize);
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

                    case VOID:
                        g2.setColor(Color.BLACK);
                        g2.fillRect(px, py, tileSize, tileSize);
                        break;
                }
            }
        }
        renderDecals(g2, room.getWallDecals());

        // The portal spans the four center tiles set by Room.spawnPortal().
        int centerX = room.getWidth() / 2;
        int centerY = room.getHeight() / 2;
        if (portalDoorImage != null && room.getTile(centerX, centerY) == TileType.PORTAL) {
            g2.drawImage(portalDoorImage, (centerX - 1) * tileSize, (centerY - 1) * tileSize,
                    2 * tileSize, 2 * tileSize, null);
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
        return type == TileType.FLOOR || type == TileType.DOOR_OPEN || type == TileType.DOOR_CLOSED || type == TileType.PORTAL;
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

    private void initShadowSprites() {
        // 1. Soft oval contact shadow (for entities and bullet)
        contactShadowImage = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D sg = contactShadowImage.createGraphics();
        sg.setPaint(new RadialGradientPaint(
                32f, 16f, 32f,
                new float[]{0f, 0.55f, 1f},
                new Color[]{
                        new Color(0, 0, 0, 140),
                        new Color(0, 0, 0, 75),
                        new Color(0, 0, 0, 0)
                }
        ));

        sg.scale(1.0, 0.5);
        sg.fillOval(0, 0, 64, 64);
        sg.dispose();
        // 2. Wall-to-floor ambient occlusion
        int ts = Room.TILE_SIZE;
        aoNorthImage = createGradientStrip(ts, 18, 0, 0, 0, 18, 125);   // strongest
        aoWestImage  = createGradientStrip(12, ts, 0, 0, 12, 0, 85);    // medium
        aoEastImage  = createGradientStrip(12, ts, 12, 0, 0, 0, 85);    // medium
        aoSouthImage = createGradientStrip(ts, 8,  0, 8, 0, 0, 60);     // subtle
    }
    private BufferedImage createGradientStrip(int w, int h, int x1, int y1, int x2, int y2, int maxAlpha) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setPaint(new GradientPaint(x1, y1, new Color(0, 0, 0, maxAlpha), x2, y2, new Color(0, 0, 0, 0)));
        g.fillRect(0, 0, w, h);
        g.dispose();
        return img;
    }

    private void renderContactShadow(Graphics2D g2, int centerX, int footY, int width, int height) {
        g2.drawImage(contactShadowImage, centerX - width / 2, footY - height / 2, width, height, null);
    }

    private void renderWallAmbientOcclusion(Graphics2D g2, Room room) {
        int ts = Room.TILE_SIZE;

        for (int x = 0; x < room.getWidth(); x++) {
            for (int y = 0; y < room.getHeight(); y++) {
                if (!isWalkable(room, x, y)) continue;

                int px = x * ts;
                int py = y * ts;

                if (!isWalkable(room, x, y - 1)) {
                    g2.drawImage(aoNorthImage, px, py, null);
                }
                if (!isWalkable(room, x - 1, y)) {
                    g2.drawImage(aoWestImage, px, py, null);
                }
                if (!isWalkable(room, x + 1, y)) {
                    g2.drawImage(aoEastImage, px + ts - aoEastImage.getWidth(), py, null);
                }
                if (!isWalkable(room, x, y + 1)) {
                    g2.drawImage(aoSouthImage, px, py + ts - aoSouthImage.getHeight(), null);
                }
            }
        }
    }

    private void renderDecals(Graphics2D g2, List<RoomDecal> decals) {
        if (decals == null || decals.isEmpty()) {
            return;
        }
        Composite oldComp = g2.getComposite();
        for (RoomDecal decal : decals) {
            BufferedImage img = getCachedImage(decal.spritePath());
            if (img == null) {
                continue;
            }
            if (decal.alpha() < 0.99f) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, decal.alpha()));
            } else {
                g2.setComposite(oldComp);
            }
            drawDecalImage(g2, img, decal);
        }
        g2.setComposite(oldComp);
    }

    private BufferedImage getCachedImage(String path) {
        if (path == null) {
            return null;
        }
        return decalCache.computeIfAbsent(path, AssetLoader::loadImage);
    }

    private void renderEntitiesAndPropsYSorted(Graphics2D g2, Room room, Player player, long playerDeathElapsedMs) {
        record RenderEntry(int sortY, Runnable draw) {}
        List<RenderEntry> entries = new ArrayList<>();

        for (Prop prop : room.getProps()) {
            BufferedImage sprite = getCachedImage(prop.spriteKey());
            entries.add(new RenderEntry(prop.y() + prop.height(), () -> {
                int shadowW = Math.round(prop.width() * 0.9f);
                int shadowH = Math.max(8, Math.round(shadowW * 0.30f));
                renderContactShadow(g2, prop.x() + prop.width() / 2, prop.y() + prop.height() - 2, shadowW, shadowH);
                if (sprite != null) {
                    g2.drawImage(sprite, prop.x(), prop.y(), prop.width(), prop.height(), null);
                } else {
                    g2.setColor(new Color(95, 80, 65));
                    g2.fillRect(prop.x(), prop.y(), prop.width(), prop.height());
                }
            }));
        }

        for (Enemy enemy : room.getEnemies()) {
            if (enemy == null || enemy.isDead()) {
                continue;
            }
            int sortY = (int) Math.round(enemy.getY() + enemy.getHeight());
            entries.add(new RenderEntry(sortY, () -> {
                if (enemy instanceof Boss3 boss3 && boss3.isFiringLaser()) {
                    renderBoss3Lasers(g2, boss3);
                }
                renderEnemy(g2, enemy);
                if (enemy instanceof Boss3 boss3 && boss3.isHealing()) {
                    renderBoss3Healing(g2, boss3);
                }
            }));
        }

        if (player != null) {
            int sortY = (int) Math.round(player.getY() + player.getHeight());
            entries.add(new RenderEntry(sortY, () -> renderPlayer(g2, player, playerDeathElapsedMs)));
        }

        entries.sort(Comparator.comparingInt(RenderEntry::sortY));
        for (RenderEntry entry : entries) {
            entry.draw().run();
        }
    }

    private void drawDecalImage(Graphics2D g2, BufferedImage img, RoomDecal decal) {
        int x = decal.x();
        int y = decal.y();
        int w = decal.width();
        int h = decal.height();
        int rot = decal.rotationSteps() & 3;
        if (rot == 0 && !decal.flipX()) {
            g2.drawImage(img, x, y, w, h, null);
            return;
        }
        AffineTransform oldTx = g2.getTransform();
        g2.translate(x + w / 2.0, y + h / 2.0);
        if (rot != 0) {
            g2.rotate(rot * (Math.PI / 2.0));
        }
        if (decal.flipX()) {
            g2.scale(-1.0, 1.0);
        }
        g2.drawImage(img, -w / 2, -h / 2, w, h, null);
        g2.setTransform(oldTx);
    }




    /** Pre-bakes a soft organic carpet wear smudge to break up repeating floor tiles. */
    private BufferedImage createFloorWearPatch(int size) {
        BufferedImage patch = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = patch.createGraphics();
        float center = size / 2.0f;
        g.setPaint(new RadialGradientPaint(
                center, center, center,
                new float[]{0f, 0.55f, 1f},
                new Color[]{
                        new Color(18, 14, 5, 34),
                        new Color(18, 14, 5, 16),
                        new Color(18, 14, 5, 0)
                }
        ));
        g.fillOval(0, 0, size, size);
        g.dispose();
        return patch;
    }

    /** Adds deterministic per-tile shade variation and cross-seam wear patches to break the flat grid. */
    private void renderFloorVariation(Graphics2D g2, Room room, int x, int y, int px, int py, int tileSize) {
        int h = (x + room.getGridX() * 31) * 73856093 ^ (y + room.getGridY() * 31) * 19349663;
        int mod = Math.floorMod(h, 100);

        if (mod < 30) {
            int alpha = 8 + (mod % 12);
            g2.setColor(new Color(18, 14, 6, alpha));
            g2.fillRect(px, py, tileSize, tileSize);
        }

        if (floorWearPatch != null && mod >= 88) {
            int ox = ((h >>> 8) & 15) - 8;
            int oy = ((h >>> 12) & 15) - 8;
            g2.drawImage(floorWearPatch, px - 8 + ox, py - 8 + oy, null);
        }
    }

    private static BufferedImage rotate90Clockwise(BufferedImage src) {
        int width = src.getWidth();
        int height = src.getHeight();

        int type = (src.getType() == 0) ? BufferedImage.TYPE_INT_ARGB : src.getType();
        BufferedImage dest = new BufferedImage(height, width, type);

        AffineTransform transform = new AffineTransform();
        transform.translate(height, 0);
        transform.rotate(Math.PI / 2);

        AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
        return op.filter(src, dest);
    }

    public LightSystem getLightSystem() { return lightSystem; }

}



