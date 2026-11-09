package screens;

import model.Combatant;
import model.Enemy;
import model.Player;
import java.awt.*;

public class GameRenderer {
    public GameRenderer() {
        // Tải trước sprite ở đây

    }

    public void render(Graphics2D g2, Player player, Enemy enemy) {
        // 1. Khử răng cưa cho hình vẽ mượt mà
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 2. Vẽ nền / bản đồ
        renderBackground(g2);
        // 3. Vẽ các thực thể trong game (Entities)
        if (enemy != null && !enemy.isDead()) {
            renderEnemy(g2, enemy);
        }
        if (player != null && !player.isDead()) {
            renderPlayer(g2, player);
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
    }
    private void renderEnemy(Graphics2D g2, Enemy enemy) {
        int x = (int) enemy.getX();
        int y = (int) enemy.getY();
        int w = enemy.getWidth();
        int h = enemy.getHeight();
        // Vẽ thân Enemy (khối đỏ)
        g2.setColor(new Color(231, 76, 60));
        g2.fillOval(x, y, w, h);
    }

}

