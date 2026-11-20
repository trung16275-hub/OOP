package screens;

import game.MainGame;
import map.Direction;
import map.MapGenerator;
import map.Room;
import map.TileType;
import map.WorldMap;
import model.Enemy;
import model.Player;
import model.weapon.Sword;
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
    private boolean attackWasPressed;

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
        installAttackKeyBinding();

        setFocusable(true);
        // Khởi tạo nhân vật, tải file map, load hội thoại cốt truyện đầu game ở đây
        initNewGame();
    }

    public void initNewGame() {
        // 1. Tạo tầng hầm ngục ngẫu nhiên (Tầng 1, 8 phòng)
        this.worldMap = mapGenerator.generateFloor(1, 8);
        this.room = worldMap.getCurrentRoom();
        if (this.room != null) {
            this.room.setVisited(true);
        }

        this.player = new Player();
        this.player.setX(180);
        this.player.setY(260);
        this.player.equipWeapon(new Sword());
        this.attackWasPressed = false;

        // 3. Cập nhật kẻ địch trong phòng hiện tại
        updateRoomEntities();
    }

    private void updateRoomEntities() {
        this.currentEnemy = null;
        if (room == null || player == null) {
            return;
        }
        double playerCenterX = player.getX() + player.getWidth() / 2.0;
        double playerCenterY = player.getY() + player.getHeight() / 2.0;
        double nearestDistanceSquared = Double.POSITIVE_INFINITY;
        for (Enemy enemy : room.getEnemies()) {
            if (enemy.isDead()) {
                continue;
            }

            double dx = enemy.getX() + enemy.getWidth() / 2.0 - playerCenterX;
            double dy = enemy.getY() + enemy.getHeight() / 2.0 - playerCenterY;
            double distanceSquared = dx * dx + dy * dy;
            if (distanceSquared < nearestDistanceSquared) {
                this.currentEnemy = enemy;
                nearestDistanceSquared = distanceSquared;
            }
        }
    }

    private void installAttackKeyBinding() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("pressed SPACE"), "attack-pressed");
        inputMap.put(KeyStroke.getKeyStroke("released SPACE"), "attack-released");

        getActionMap().put("attack-pressed", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                keyH.attackPressed = true;
            }
        });
        getActionMap().put("attack-released", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                keyH.attackPressed = false;
            }
        });
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

        double facingX = (keyH.rightPressed ? 1 : 0) - (keyH.leftPressed ? 1 : 0);
        double facingY = (keyH.downPressed ? 1 : 0) - (keyH.upPressed ? 1 : 0);
        player.setFacingDirection(facingX, facingY);

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
                this.room.setVisited(true);
                Point spawnPos = nextRoom.getDoorSpawnPosition(doorDir);
                player.setX(spawnPos.x);
                player.setY(spawnPos.y);
                updateRoomEntities();
            }
        } else {
            player.clampPosition(getWidth(), getHeight());
        }

        // 3. Cập nhật tất cả Enemy trong phòng.
        if (room != null) {
            for (Enemy enemy : room.getEnemies()) {
                if (!enemy.isDead()) {
                    enemy.updateBehavior(player);
                    enemy.clampPosition(getWidth(), getHeight());
                }
            }
        }
        updateRoomEntities();
        if (currentEnemy != null && !currentEnemy.isDead() && player.canDetect(currentEnemy)) {
            player.faceToward(currentEnemy);
        }

        // Space tạo một lần chém khi vừa nhấn; đứng trong hitbox không tự gây sát thương.
        if (keyH.attackPressed && !attackWasPressed) {
            player.attack(currentEnemy);
            if (currentEnemy != null && currentEnemy.isDead()) {
                updateRoomEntities();
            }
        }
        attackWasPressed = keyH.attackPressed;

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
