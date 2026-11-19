package screens;

import game.MainGame;
import map.Direction;
import map.Room;
import map.TileType;
import map.WorldMap;
import model.Combatant;
import model.Enemy;
import model.Player;
import model.weapon.Sword;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

public class GameRenderer {
    public GameRenderer() {
        // Tải trước sprite ở đây

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
            renderEnemy(g2, enemy);
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
        int cellSize = 12;
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
                }
            }
        } finally {
            mini.dispose();
        }
    }
    private void renderBackground(Graphics2D g2) {
        // Sau này thay bằng vẽ tile map; hiện tại là nền tối
        g2.setColor(new Color(25, 25, 30));
        g2.fillRect(0, 0, 960, 640);
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
        renderSword(g2, player);
    }

    private void renderSword(Graphics2D g2, Player player) {
        if (!(player.getEquippedWeapon() instanceof Sword sword)) {
            return;
        }

        double centerX = player.getX() + player.getWidth() / 2.0;
        double centerY = player.getY() + player.getHeight() / 2.0;
        double facingX = player.getFacingX();
        double facingY = player.getFacingY();
        if (facingX == 0 && facingY == 0) {
            facingX = 1;
        }

        double facingAngle = Math.atan2(facingY, facingX);
        double swingProgress = sword.getSwingProgress();
        boolean swinging = swingProgress >= 0;
        double swingAngle = swinging
                ? Math.toRadians(-75 + 150 * swingProgress)
                : 0;

        Graphics2D swordGraphics = (Graphics2D) g2.create();
        try {
            swordGraphics.translate(centerX, centerY);
            swordGraphics.rotate(facingAngle + swingAngle);

            if (swinging) {
                int alpha = (int) (190 * (1 - swingProgress));
                Path2D slashTrail = new Path2D.Double();
                slashTrail.moveTo(24, -31);
                slashTrail.curveTo(43, -49, 77, -34, 79, -4);
                slashTrail.curveTo(81, 19, 57, 34, 32, 29);
                swordGraphics.setColor(new Color(170, 230, 255, Math.max(0, alpha)));
                swordGraphics.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                swordGraphics.draw(slashTrail);
            }

            // Grip and pommel.
            swordGraphics.setColor(new Color(102, 62, 35));
            swordGraphics.fillRoundRect(14, -4, 20, 8, 4, 4);
            swordGraphics.setColor(new Color(220, 180, 90));
            swordGraphics.fillOval(10, -5, 8, 10);

            // Crossguard.
            swordGraphics.setColor(new Color(218, 172, 75));
            swordGraphics.fillRoundRect(31, -13, 6, 26, 4, 4);

            // Blade, with a bright center line.
            Polygon blade = new Polygon(
                    new int[] {35, 68, 82, 68, 35},
                    new int[] {-5, -5, 0, 5, 5},
                    5);
            swordGraphics.setColor(new Color(195, 211, 222));
            swordGraphics.fillPolygon(blade);
            swordGraphics.setColor(new Color(70, 88, 100));
            swordGraphics.setStroke(new BasicStroke(1.5f));
            swordGraphics.drawPolygon(blade);
            swordGraphics.setColor(new Color(245, 250, 255));
            swordGraphics.setStroke(new BasicStroke(1.5f));
            swordGraphics.drawLine(37, -2, 70, -2);
        } finally {
            swordGraphics.dispose();
        }
    }

    private void renderEnemy(Graphics2D g2, Enemy enemy) {
        int x = (int) enemy.getX();
        int y = (int) enemy.getY();
        int w = enemy.getWidth();
        int h = enemy.getHeight();
        // Vẽ thân Enemy (khối đỏ)
        g2.setColor(new Color(231, 76, 60));
        g2.fillOval(x, y, w, h);
        renderFacingMarker(g2, enemy);
        renderEnemyHealthBar(g2, enemy);
    }

    private void renderEnemyHealthBar(Graphics2D g2, Enemy enemy) {
        int barWidth = Math.max(52, enemy.getWidth());
        int barHeight = 7;
        int barX = (int) Math.round(enemy.getX() + (enemy.getWidth() - barWidth) / 2.0);
        int barY = (int) enemy.getY() - 13;
        double healthRatio = enemy.getMaxHp() == 0
                ? 0
                : Math.max(0, enemy.getHp()) / (double) enemy.getMaxHp();

        g2.setColor(new Color(25, 25, 25, 220));
        g2.fillRoundRect(barX, barY, barWidth, barHeight, 5, 5);
        g2.setColor(healthRatio > 0.5 ? new Color(46, 204, 113)
                : healthRatio > 0.25 ? new Color(243, 156, 18)
                : new Color(231, 76, 60));
        g2.fillRoundRect(barX, barY,
                (int) Math.round(barWidth * healthRatio), barHeight, 5, 5);

        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        String hpText = enemy.getHp() + "/" + enemy.getMaxHp();
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(hpText,
            barX + (barWidth - metrics.stringWidth(hpText)) / 2,
            barY - 3);
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

    private void renderRoom(Graphics2D g2, Room room) {
        int tileSize = Room.TILE_SIZE;

        for (int x = 0; x < room.getWidth(); x++) {
            for (int y = 0; y < room.getHeight(); y++) {
                TileType type = room.getTile(x, y);
                int px = x * tileSize;
                int py = y * tileSize;

                switch (type) {
                    case WALL -> {
                        g2.setColor(WALL_COLOR);
                        g2.fillRect(px, py, tileSize, tileSize);
                    }
                    case FLOOR -> {
                        g2.setColor(FLOOR_COLOR);
                        g2.fillRect(px, py, tileSize, tileSize);
                    }
                    case DOOR_OPEN -> {
                        g2.setColor(DOOR_OPEN_COLOR);
                        g2.fillRect(px, py, tileSize, tileSize);
                    }
                }
            }
        }
    }

}

