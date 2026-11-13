package screens;

import game.MainGame;
import map.Room;
import map.TileType;
import model.Combatant;
import model.Enemy;
import model.Player;
import java.awt.*;
import java.awt.geom.Ellipse2D;

public class GameRenderer {
    public GameRenderer() {
        // Tải trước sprite ở đây

    }

    public void render(Graphics2D g2, Player player, Enemy enemy, Room room) {
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

    private void renderRoom(Graphics2D g2, Room room) {
        int tileSize = Room.TILE_SIZE;

        for (int x = 0; x < room.getWidth(); x++) {
            for (int y = 0; y < room.getHeight(); y++) {
                g2.setColor(room.getTile(x, y) == TileType.WALL ? WALL_COLOR : FLOOR_COLOR);
                g2.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }
    }

}

