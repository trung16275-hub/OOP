
package screens;

import game.MainGame;
import map.Room;
import model.Boss1;
import model.Enemy;
import model.Player;
import model.weapon.Sword;
import model.weapon.Bow;
import screens.input.*;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

    // Trạng thái phím tấn công và đổi vũ khí
    private boolean attackKeyDown = false;
    private boolean switchKeyDown = false;

    // Renderer
    private GameRenderer renderer;

    public GamePanel(MainGame mainFrame) {
        setPreferredSize(new Dimension(
                MainGame.WINDOW_WIDTH,
                MainGame.WINDOW_HEIGHT
        ));

        this.mainFrame = mainFrame;
        this.renderer = new GameRenderer();
        this.addKeyListener(keyH);

        // Điều khiển J/K
        this.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                if (key == KeyEvent.VK_J && !attackKeyDown) {
                    attackKeyDown = true;

                    if (player != null && currentEnemy != null) {
                        player.attack(currentEnemy);
                    }
                }

                if (key == KeyEvent.VK_K && !switchKeyDown) {
                    switchKeyDown = true;

                    if (player != null) {
                        player.switchWeapon();
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();

                if (key == KeyEvent.VK_J) {
                    attackKeyDown = false;
                }

                if (key == KeyEvent.VK_K) {
                    switchKeyDown = false;
                }
            }
        });

        setFocusable(true);

        initNewGame();
    }

    public void initNewGame() {

        // Khởi tạo Player
        this.player = new Player();
        this.player.setX(180);
        this.player.setY(260);

        // Trang bị vũ khí theo hệ thống mới
        player.equip(new Sword());
        player.equip(new Bow());

        // Bắt đầu bằng kiếm
        player.switchWeapon();

        // Khởi tạo Boss
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

        while (isRunning) {
            update();
            repaint();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void update() {

        if (player.isDead()) {
            endGame();
            return;
        }

        // Di chuyển Player
        if (keyH.upPressed) {
            player.setY(player.getY() - player.getSpeed());
        }

        if (keyH.downPressed) {
            player.setY(player.getY() + player.getSpeed());
        }

        if (keyH.leftPressed) {
            player.setX(player.getX() - player.getSpeed());
        }

        if (keyH.rightPressed) {
            player.setX(player.getX() + player.getSpeed());
        }

        player.clampPosition(getWidth(), getHeight());

        // Cập nhật Enemy
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
        renderer.render(g2, player, currentEnemy, room);
    }
}

