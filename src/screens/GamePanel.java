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
        // GamePanel chỉ quản lý vị trí spawn; chỉ số và kích thước nằm trong class nhân vật.
        this.player = new Player();
        this.player.setX(180);
        this.player.setY(260);

        // Khởi tạo Boss tại vị trí spawn.
        this.currentEnemy = new Boss1();
        this.currentEnemy.setX(680);
        this.currentEnemy.setY(250);

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
        if (keyH.upPressed)    player.setY(player.getY() - player.getSpeed());
        if (keyH.downPressed)  player.setY(player.getY() + player.getSpeed());
        if (keyH.leftPressed)  player.setX(player.getX() - player.getSpeed());
        if (keyH.rightPressed) player.setX(player.getX() + player.getSpeed());
        player.clampPosition ( getWidth() , getHeight()) ;

        //2. Cập nhật Enemy
        if (currentEnemy != null && !currentEnemy.isDead()) {
            currentEnemy.chase(player);
            currentEnemy.clampPosition(getWidth(), getHeight());
            if (player.canDetect(currentEnemy)) {
                player.faceToward(currentEnemy);
            } else {
                player.resetFacing();
            }
            currentEnemy.tryContactAttack(player);
        } else {
            player.resetFacing();
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
