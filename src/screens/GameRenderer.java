package screens;

import game.MainGame;
import map.Room;
import map.TileType;
import map.WorldMap;
import map.Direction;
import model.Combatant;
import model.Enemy;
import model.Player;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import model.boss.Boss3;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class GameRenderer {
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
    private BufferedImage armorImage;

    public GameRenderer() {
        // Preload tiles from assets/tiles/
        registerAnim("boss3", "idle", "assets/boss3/super-cool-robot-idle.png", 9);
    registerAnim("boss3", "run",  "assets/boss3/super-cool-robot-run.png", 9);
    registerAnim("boss3", "fire", "assets/boss3/super-cool-robot-fire.png", 4);
    registerAnim("boss3", "jump", "assets/boss3/super-cool-robot-jump.png", 7);

        basefloorImage = AssetLoader.loadImage("/tiles/tile_floor_clean.png");
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

    }
    // Hàm cắt 1 hàng ngang thành mảng các frame
private BufferedImage[] loadSpriteSheet(String path, int frameCount) {
    try {
        BufferedImage sheet = ImageIO.read(new File(path));
        int frameWidth = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();

        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }
        return frames;
    } catch (IOException e) {
        System.err.println("Không tìm thấy ảnh: " + path);
        return null;
    }
}
    // Hàm đăng ký animation cho 1 bộ sprite
    private void registerAnim(String spriteKey, String state, String path, int frameCount) {
    BufferedImage[] frames = loadSpriteSheet(path, frameCount);
    if (frames != null) {
        animations.put(spriteKey + "_" + state, frames);
    }
}
    public void render(Graphics2D g2, Player player, Enemy enemy, Room room) {
        render(g2, player, enemy, room, null);
    }

    public void render(Graphics2D g2, Player player, Enemy enemy, Room room, WorldMap worldMap) {
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

        if (enemy != null && !enemy.isDead()) {
            renderDetectionRange(g2, enemy, enemy.getDetectionRange(),
                    new Color(0, 255, 0, 22), new Color(0, 255, 0, 190));
                    // Vẽ 4 tia laze dưới thân Boss3
        if (enemy instanceof Boss3) {
            Boss3 boss3 = (Boss3) enemy;
            if (boss3.isFiringLaser()) {
                renderBoss3Lasers(g2, boss3);
            }
        }
            renderEnemy(g2, enemy);
            // Vẽ vòng sáng hồi máu đè lên thân Boss3
        if (enemy instanceof Boss3) {
            Boss3 boss3 = (Boss3) enemy;
            if (boss3.isHealing()) {
                renderBoss3Healing(g2, boss3);
            }
        }
        }
        if (player != null && !player.isDead()) {
            renderDetectionRange(g2, player, player.getDetectionRange(),
                    new Color(52, 152, 219, 22), new Color(52, 152, 219, 190));
            renderPlayer(g2, player);
        }
        if (enemy != null && !enemy.isDead()) {
            renderHitbox(g2, enemy);
        }
        if (player != null && !player.isDead()) {
            renderHitbox(g2, player);
        }

        g2.translate(camX, camY); // reset before rendering HUD/minimap

        if (worldMap != null) {
            renderMiniMap(g2, worldMap);
        }
        if (player != null) {
            renderPlayerStats(g2, player);
        }

    }

    private void renderPlayerStats(Graphics2D g2, Player player) {
        Graphics2D hud = (Graphics2D) g2.create();
        try {
            hud.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            int x = 10;
            int y = 10;
            int width = 200;
            int height = 72;

            // Khung HUD ngoài.
            hud.setColor(new Color(70, 58, 43));
            hud.fillRoundRect(x, y, width, height, 8, 8);
            hud.setColor(new Color(180, 145, 95));
            hud.fillRoundRect(x + 4, y + 4, width - 8, height - 8, 5, 5);
            hud.setColor(new Color(215, 196, 150));
            hud.drawRoundRect(x + 4, y + 4, width - 9, height - 9, 5, 5);

            hud.setFont(new Font("Minecraftia", Font.BOLD, 18));

            // Thanh HP.
            hud.setColor(Color.BLACK);
            hud.drawString("HP:", x + 12, y + 27);
            hud.drawString(player.getHp() + "/" + player.getMaxHp(), x + 45, y + 27);

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
            hud.fillRect(hpBarX + 2, hpBarY + 2,
                    (int) ((hpBarWidth - 4) * hpRatio), hpBarHeight - 4);

            // Thanh Armor.
            hud.setColor(Color.BLACK);
            hud.drawString("Armor:", x + 12, y + 57);
            hud.drawString(String.valueOf(player.getArmor()), x + 75, y + 57);


            // Icon giáp đơn giản ở góc phải.
            int iconX = x + 145;
            int iconY = y + 25;
            if (armorImage != null) {
                hud.drawImage(armorImage, iconX, iconY, 40, 50, null);
            }
        } finally {
            hud.dispose();
        }
    }

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
            mini.fillRoundRect(startX - 8, startY - 8, mapWidth + 16, mapHeight + 16, 12, 12);

            for (int gx = 0; gx < gridSize; gx++) {
                for (int gy = 0; gy < gridSize; gy++) {
                    Room room = worldMap.getRoom(gx, gy);
                    if (room == null) {
                        continue;
                    }

                    if (!room.isVisited() && !worldMap.hasVisitedNeighbor(gx,gy)) {
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
                }
            }
        } finally {
            mini.dispose();
        }
    }
    private void renderBackground(Graphics2D g2) {
        // Sau này thay bằng vẽ tile map; hiện tại là nền tối
        g2.setColor(new Color(25, 25, 30));
        g2.fillRect(0, 0, MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT);
    }
    private void renderPlayer(Graphics2D g2, Player player) {
        int x = (int) player.getX();
        int y = (int) player.getY();
        int w = player.getWidth();
        int h = player.getHeight();
        // Vẽ thân Player (khối xanh)
        g2.setColor(new Color(52, 152, 219));
        g2.fillOval(x, y, w, h);
        renderFacingMarker(g2, player);
    }
    private void renderEnemy(Graphics2D g2, Enemy enemy) {

        int x = (int) enemy.getX();
        int y = (int) enemy.getY();
        int w = enemy.getWidth();
        int h = enemy.getHeight();
        String key = enemy.getSpriteKey();
    if (key != null) {
        BufferedImage[] frames = animations.get(key + "_" + enemy.getAnimState());
        if (frames != null && frames.length > 0) {
            int frameIndex = (int) ((System.currentTimeMillis() / 100) % frames.length);
            if(enemy.getFacingX() >= 0) {
                g2.drawImage(frames[frameIndex], x, y, w, h, null);
            } else {
                // Lật ảnh theo chiều ngang
                g2.drawImage(frames[frameIndex], x + w, y, -w, h, null);
            }
            return;

        }
    }
        // Vẽ thân Enemy (khối đỏ)
        g2.setColor(new Color(231, 76, 60));
        g2.fillOval(x, y, w, h);
        renderFacingMarker(g2, enemy);
    }

    private void renderHitbox(Graphics2D g2, Combatant combatant) {
        double radius = combatant.getHitboxRadius();
        double diameter = radius * 2;
        double centerX = combatant.getX() + combatant.getWidth() / 2.0;
        double centerY = combatant.getY() + combatant.getHeight() / 2.0;
        Ellipse2D hitbox = new Ellipse2D.Double(
                centerX - radius, centerY - radius, diameter, diameter);

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

    private void renderDetectionRange(Graphics2D g2, Combatant combatant, double range,
                                      Color fillColor, Color outlineColor) {
        if (Double.isNaN(range) || range < 0) {
            return;
        }

        Graphics2D debug = (Graphics2D) g2.create();
        try {
            debug.setStroke(new BasicStroke(2f));
            if (Double.isInfinite(range)) {
                Rectangle viewport = new Rectangle(
                        0, 0, MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT);
                debug.setColor(fillColor);
                debug.fillRect(viewport.x, viewport.y, viewport.width, viewport.height);
                debug.setColor(outlineColor);
                debug.drawRect(viewport.x + 1, viewport.y + 1,
                        Math.max(0, viewport.width - 3), Math.max(0, viewport.height - 3));
                return;
            }

                double centerX = combatant.getX() + combatant.getWidth() / 2.0;
                double centerY = combatant.getY() + combatant.getHeight() / 2.0;
            double diameter = range * 2;
            Ellipse2D detectionArea = new Ellipse2D.Double(
                    centerX - range, centerY - range, diameter, diameter);
                debug.setColor(fillColor);
            debug.fill(detectionArea);
                debug.setColor(outlineColor);
            debug.draw(detectionArea);
        } finally {
            debug.dispose();
        }
    }

    private void renderFacingMarker(Graphics2D g2, Combatant combatant) {
        double centerX = combatant.getX() + combatant.getWidth() / 2.0;
        double centerY = combatant.getY() + combatant.getHeight() / 2.0;
        double markerOffset = Math.min(combatant.getWidth(), combatant.getHeight()) * 0.25;
        double markerCenterX = centerX + combatant.getFacingX() * markerOffset;
        double markerCenterY = centerY + combatant.getFacingY() * markerOffset;
        int eyeSize = Math.max(6, Math.min(combatant.getWidth(), combatant.getHeight()) / 5);
        int pupilSize = Math.max(3, eyeSize / 2);

        g2.setColor(Color.WHITE);
        g2.fillOval((int) Math.round(markerCenterX - eyeSize / 2.0),
                (int) Math.round(markerCenterY - eyeSize / 2.0), eyeSize, eyeSize);
        g2.setColor(Color.BLACK);
        g2.fillOval(
                (int) Math.round(markerCenterX + combatant.getFacingX() * eyeSize * 0.15 - pupilSize / 2.0),
                (int) Math.round(markerCenterY + combatant.getFacingY() * eyeSize * 0.15 - pupilSize / 2.0),
                pupilSize, pupilSize);
    }

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
                    case WALL -> renderWallTile(g2, room, x, y, px, py, tileSize);

                    case FLOOR, DOOR_OPEN -> {
                        if (basefloorImage != null) {
                            g2.drawImage(basefloorImage, px, py, tileSize, tileSize, null);
                        } else {
                            g2.setColor(FLOOR_COLOR);
                            g2.fillRect(px, py, tileSize, tileSize);
                        }
                    }
                    case DOOR_CLOSED -> {
                        if (doorClosedImage != null) {
                            g2.drawImage(doorClosedImage, px, py, tileSize, tileSize, null);
                        } else {
                            g2.setColor(DOOR_CLOSED_COLOR);
                            g2.fillRect(px, py, tileSize, tileSize);
                        }
                    }
                }
            }
        }
    }
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

            // Viền ngoài phát sáng (đỏ mờ)
            g2.setColor(new Color(255, 50, 50, 120));
            g2.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) centerX, (int) centerY, endX, endY);

            // Lõi tia laze (trắng sáng)
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine((int) centerX, (int) centerY, endX, endY);
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

        // Vòng hào quang xanh lá khi đang hồi máu
        g2.setColor(new Color(46, 204, 113, 70));
        g2.fillOval(x, y, w, h);
        g2.setColor(new Color(46, 204, 113, 220));
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(x, y, w, h);

        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
    }





    private boolean isWalkable(Room room, int x, int y) {
        if (room == null || x < 0 || x >= room.getWidth() || y < 0 || y >= room.getHeight()) {
            return false;
        }
        TileType type = room.getTile(x, y);
        return type == TileType.FLOOR || type == TileType.DOOR_OPEN || type == TileType.DOOR_CLOSED;
    }

    private void renderWallTile(Graphics2D g2, Room room, int x, int y, int px, int py, int tileSize) {
        int w = room.getWidth();
        int padX = room.getPadX();
        int padY = room.getPadY();

        BufferedImage img = null;
        Direction sideDir = (x <= padX + 1) ? Direction.WEST : Direction.EAST;
        int doorY = room.getDoorOffset(sideDir);

        // 1. North Wall Decorative Wallpaper Stack (shifted by padY and padX)
        if (y >= padY + 1 && y <= padY + 3 && x >= padX + 2 && x <= w - padX - 3) {
            int relY = y - padY;
            switch (relY) {
                case 1 -> img = wallTopRimImage != null ? wallTopRimImage : wallBorderTopImage;
                case 2 -> img = basewallImage;
                case 3 -> img = wallBotRimImage;
            }
        }
        // Extended West/East Hallway Wallpaper Rims (all the way to x = 0 and x = w - 1)
        else if (((x <= padX + 1) || (x >= w - padX - 2))
                && (y == doorY - 3 || y == doorY - 2)
                && isWalkable(room, x, doorY)) {
            if (y == doorY - 3) img = wallTopRimImage;
            else if (y == doorY - 2) img = wallBotRimImage;
        } else {
            // 2. Autotiling / Neighbor Checking
            boolean openNorth = isWalkable(room, x, y - 1);
            boolean openSouth = isWalkable(room, x, y + 1);
            boolean openWest  = isWalkable(room, x - 1, y) || (y >= padY + 1 && y <= padY + 3 && x == w - padX - 2);
            boolean openEast  = isWalkable(room, x + 1, y) || (y >= padY + 1 && y <= padY + 3 && x == padX + 1);

            if (openNorth && openWest)      img = wallCornerTopLeftImage;
            else if (openNorth && openEast) img = wallCornerTopRightImage;
            else if (openSouth && openWest) img = wallCornerBottomLeftImage;
            else if (openSouth && openEast) img = wallCornerBottomRightImage;
            else if (openWest)              img = wallBorderLeftImage;
            else if (openEast)              img = wallBorderRightImage;
            else if (openNorth)             img = wallBorderTopImage;
            else if (openSouth)             img = wallBorderBottomImage;
        }

        if (img != null) {
            g2.drawImage(img, px, py, tileSize, tileSize, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(px, py, tileSize, tileSize);
        }
    }


}



