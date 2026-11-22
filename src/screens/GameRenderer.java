package screens;

import game.MainGame;
import map.Room;
import map.TileType;
import map.WorldMap;
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
        // 1. Khử răng cưa cho hình vẽ mượt mà
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 2. Vẽ nền / bản đồ
        renderBackground(g2);
        renderRoom(g2, room);
        // 3. Vẽ các thực thể trong game (Entities)
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
            // A light background keeps dark gray armor readable on the game map.
            hud.setColor(new Color(235, 235, 240, 230));
            hud.fillRoundRect(10, 10, 220, 70, 12, 12);
            hud.setFont(new Font("SansSerif", Font.BOLD, 22));

            hud.setColor(Color.RED);
            hud.drawString("HP: " + player.getHp() + "/" + player.getMaxHp(), 20, 37);

            hud.setColor(Color.DARK_GRAY);
            hud.drawString("Armor: " + player.getArmor(), 20, 66);
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
        return type == TileType.FLOOR || type == TileType.DOOR_OPEN;
    }

    private void renderWallTile(Graphics2D g2, Room room, int x, int y, int px, int py, int tileSize) {
        int w = room.getWidth();
        int h = room.getHeight();
        int midY = h/2;

        BufferedImage img = null;

        // 1. North Wall Decorative Wallpaper Stack (rows 1 to 3, between side walls)
        if (y >= 1 && y <= 3 && x >= 2 && x <= w - 3) {
            switch (y) {
                case 1 -> img = wallTopRimImage != null ? wallTopRimImage : wallBorderTopImage;
                case 2 -> img = basewallImage;
                case 3 -> img = wallBotRimImage;
            }
        } else if (((x <= 1) || (x >= w - 2)) && (y == midY - 3||y == midY - 2) && (isWalkable(room,x , midY))) {
            if (y == midY - 3) img = wallTopRimImage;
            else if (y == midY - 2) img = wallBotRimImage;
        } else {
            // 2. Autotiling / Context-Aware Neighbor Checking for side walls, bottom wall, and doorways
            boolean openNorth = isWalkable(room, x, y - 1);
            boolean openSouth = isWalkable(room, x, y + 1);
            boolean openWest  = isWalkable(room, x - 1, y) || (1<=y && y<=3) && (x == w - 2);
            boolean openEast  = isWalkable(room, x + 1, y) || (1<=y && y<=3) && (x == 1);

            // Doorway and perimeter corner caps
            if (openNorth && openWest) {
                img = wallCornerTopLeftImage;
            } else if (openNorth && openEast) {
                img = wallCornerTopRightImage;
            } else if (openSouth && openWest) {
                img = wallCornerBottomLeftImage;
            } else if (openSouth && openEast) {
                img = wallCornerBottomRightImage;
            }
            // Straight borders facing walkable floor or doorway
            else if (openWest) {
                img = wallBorderLeftImage;
            } else if (openEast) {
                img = wallBorderRightImage;
            } else if (openNorth) {
                img = wallBorderTopImage;
            } else if (openSouth) {
                img = wallBorderBottomImage;
            }
        }

        // Render texture or fallback color
        if (img != null) {
            g2.drawImage(img, px, py, tileSize, tileSize, null);
        } else {
            g2.setColor(Color.BLACK);
            g2.fillRect(px, py, tileSize, tileSize);
        }
    }


}
    


