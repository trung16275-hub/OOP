package screens;

import game.MainGame;
import model.Boss1;
import model.Enemy;
import model.Player;

import java.awt.*;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {
    private MainGame mainFrame;
    private Thread gameThread;
    private boolean isRunning;

    // Entities
    private Player player;
    private Enemy currentEnemy;

    public GamePanel(MainGame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Color.DARK_GRAY);
        setFocusable(true);

        // Khởi tạo nhân vật, tải file map, load hội thoại cốt truyện đầu game ở đây
        initNewGame();
    }

    public void initNewGame() {
        // Khởi tạo Player (vị trí x=180, y=260, kích thước 50x50)
        this.player = new Player("Hero", 100);
        this.player.setX(180);
        this.player.setY(260);
        this.player.setWidth(50);
        this.player.setHeight(50);

        // Khởi tạo Boss (vị trí x=680, y=250, kích thước 60x60)
        this.currentEnemy = new Boss1();
        this.currentEnemy.setX(680);
        this.currentEnemy.setY(250);
        this.currentEnemy.setWidth(60);
        this.currentEnemy.setHeight(60);
    }

    public void startGameLoop() {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // Vòng lặp game (Game Loop)
        while (isRunning) {
            update(); // Cập nhật logic (tọa độ, máu, va chạm)
            repaint(); // Vẽ lại mọi thứ ra màn hình

            try {
                Thread.sleep(16); // Giới hạn tốc độ khung hình (khoảng 60 FPS)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void update() {
        // Cập nhật trạng thái quái vật, nhân vật di chuyển
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        //Vẽ Player - xanh
        if (player != null) {
            g2.setColor(new Color(52, 152, 219));
            g2.fillOval((int) player.getX(), (int) player.getY(), player.getWidth(), player.getHeight());

        }

        //Vẽ Boss - đỏ
        if (currentEnemy != null && !currentEnemy.isDead()) {
            g2.setColor(new Color(231, 76, 60));
            g2.fillOval((int) currentEnemy.getX(), (int) currentEnemy.getY(), currentEnemy.getWidth(), currentEnemy.getHeight());

        }

    }
}
