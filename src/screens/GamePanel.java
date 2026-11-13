package screens;

import game.MainGame;
import map.Room;
import model.Boss1;
import model.Enemy;
import model.Player;
import screens.input.*;

import java.awt.*;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {
    private MainGame mainFrame;
    private Thread gameThread;
    private volatile boolean isRunning;

    // Entities
    private Player player;
    private Enemy currentEnemy;
    private Room room;

    // Key Handling
    private KeyHandler keyH = new KeyHandler();

    // Renderer
    private GameRenderer renderer;

    public GamePanel(MainGame mainFrame) {
        setPreferredSize(new Dimension(MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT));
        this.mainFrame = mainFrame;
        this.renderer = new GameRenderer();
        this.addKeyListener(keyH);

        setFocusable(true);
        // Khởi tạo nhân vật, tải file map, load hội thoại cốt truyện đầu game ở đây
        initNewGame();
    }

    public void initNewGame() {
        // Khởi tạo Player (vị trí x=180, y=260, kích thước 50x50)
        this.player = new Player();
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

        this.room = new Room();
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

        //1. Cập nhật nhân vật
        if (player.isDead()) {
            endGame();
            return;
        }
        //Cần fix sau - Vị trí
        double moveX = 0;
        double moveY = 0;
        if (keyH.upPressed)    moveY -= player.getSpeed();
        if (keyH.downPressed)  moveY += player.getSpeed();
        if (keyH.leftPressed)  moveX -= player.getSpeed();
        if (keyH.rightPressed) moveX += player.getSpeed();
        movePlayer(moveX, moveY);
        player.clampPosition ( getWidth() , getHeight()) ;

        //2. Cập nhật Enemy
        if (currentEnemy != null && !currentEnemy.isDead()) {
            currentEnemy.chase(player);
            currentEnemy.clampPosition(getWidth(), getHeight());
            currentEnemy.tryContactAttack(player);
        }

    }

    private void movePlayer(double moveX, double moveY) {
        if (moveX != 0) {
            double nextX = player.getX() + moveX;
            if (currentEnemy == null || currentEnemy.isDead() || !player.overlapsAt(nextX, player.getY(), currentEnemy)) {
                player.setX(nextX); /// Kiểm tra điều kiện để đi , nếu bị chặn thì ko đi được 
            }
        }

        if (moveY != 0) {
            double nextY = player.getY() + moveY;
            if (currentEnemy == null || currentEnemy.isDead()|| !player.overlapsAt(player.getX(), nextY, currentEnemy)) {
                player.setY(nextY); /// Kiểm tra điều kiện để đi , nếu bị chặn thì ko đi được 
            }
        }
    }

    private void endGame() {
        isRunning = false;
        // Swing screen changes must run on the event dispatch thread.
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        renderer.render((Graphics2D) g, player, currentEnemy, room);

    }
}
