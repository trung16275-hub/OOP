package screens;

import game.MainGame;
import map.Direction;
import map.MapGenerator;
import map.Room;
import map.TileType;
import map.WorldMap;
import model.Enemy;
import model.Player;
import screens.input.KeyHandler;

import java.awt.*;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {
    private MainGame mainFrame;
    private Thread gameThread;
    private volatile boolean isRunning;

    // Entities
    private Player player;
    private Enemy currentEnemy;

    // Map System
    private MapGenerator mapGenerator;
    private WorldMap worldMap;
    private Room room;

    // Key Handling
    private KeyHandler keyH = new KeyHandler();

    // Renderer
    private GameRenderer renderer;

    public GamePanel(MainGame mainFrame) {
        setPreferredSize(new Dimension(MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT));
        this.mainFrame = mainFrame;
        this.renderer = new GameRenderer();
        this.mapGenerator = new MapGenerator();
        this.addKeyListener(keyH);

        setFocusable(true);
        // Khởi tạo nhân vật, tải file map, load hội thoại cốt truyện đầu game ở đây
        initNewGame();
    }

    public void initNewGame() {
        // 1. Tạo tầng hầm ngục ngẫu nhiên (Tầng 1, 8 phòng)
        this.worldMap = mapGenerator.generateFloor(1, 8);
        this.room = worldMap.getCurrentRoom();

        this.player = new Player();
        this.player.setX(180);
        this.player.setY(260);

        // 3. Cập nhật kẻ địch trong phòng hiện tại
        updateRoomEntities();
    }

    private void updateRoomEntities() {
        if (room != null && !room.getEnemies().isEmpty()) {
            this.currentEnemy = room.getEnemies().get(0);
        } else {
            this.currentEnemy = null;
        }
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

        if (keyH.upPressed)    player.setY(player.getY() - player.getSpeed());
        if (keyH.downPressed)  player.setY(player.getY() + player.getSpeed());
        if (keyH.leftPressed)  player.setX(player.getX() - player.getSpeed());
        if (keyH.rightPressed) player.setX(player.getX() + player.getSpeed());

        // 2. Kiểm tra chuyển phòng
        Direction doorDir = room.checkDoorTrigger(player.getX(), player.getY(), player.getWidth(), player.getHeight());
        if (doorDir != null) {
            Room nextRoom = worldMap.moveTo(doorDir);
            if (nextRoom != room) {
                this.room = nextRoom;
                Point spawnPos = nextRoom.getDoorSpawnPosition(doorDir);
                player.setX(spawnPos.x);
                player.setY(spawnPos.y);
                updateRoomEntities();
            }
        } else {
            player.clampPosition(getWidth(), getHeight());
        }

        //3. Cập nhật Enemy
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

        room.openDoors();
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
        renderer.render((Graphics2D) g, player, currentEnemy, room, worldMap);
    }
}
