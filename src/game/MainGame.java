package game;

import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import screens.GamePanel;
import screens.panels.GameOverPanel;
import screens.panels.GameWinPanel;
import screens.panels.IntroPanel;

public class MainGame extends JFrame {
    public static final int WINDOW_WIDTH = 1024;
    public static final int WINDOW_HEIGHT = 576;

    private boolean fullscreen;
    private boolean togglingFullscreen;

    public MainGame() {
        setTitle("Dungeon Crawler Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Căn giữa màn hình
        setResizable(false);
        registerFullscreenShortcuts();

        // Mặc định khi bật game lên sẽ nạp màn hình Intro
        showIntro();
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public boolean isTogglingFullscreen() {
        return togglingFullscreen;
    }


    public void toggleFullscreen() {
        boolean targetFullscreen = !fullscreen;
        Rectangle screenBounds = getGraphicsConfiguration().getBounds();

        togglingFullscreen = true;
        try {
            dispose();
            setUndecorated(targetFullscreen);
            setResizable(false);
            fullscreen = targetFullscreen;

            if (targetFullscreen) {
                setExtendedState(JFrame.NORMAL);
                setBounds(screenBounds);
                setVisible(true);
            } else {
                setExtendedState(JFrame.NORMAL);
                pack();
                setLocationRelativeTo(null);
                setVisible(true);
            }
        } finally {
            togglingFullscreen = false;
        }

        if (getContentPane() instanceof IntroPanel intro) {
            intro.updateFullscreenButtonText();
        }

        revalidate();
        repaint();
        SwingUtilities.invokeLater(() -> {
            Container contentPane = getContentPane();
            if (contentPane != null) {
                contentPane.requestFocusInWindow();
            }
        });
    }

    private void registerFullscreenShortcuts() {
        JRootPane root = getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();

        String actionKey = "toggleFullscreen";
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), actionKey);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK), actionKey);
        actionMap.put(actionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleFullscreen();
            }
        });
    }

    private void setScreen(JPanel panel, boolean requestFocus) {
        setContentPane(panel);
        if (!fullscreen) {
            pack();
            setLocationRelativeTo(null);
        }

        revalidate(); // Báo cho hệ thống cập nhật lại giao diện
        repaint();    // Vẽ lại màn hình
        if (requestFocus) {
            panel.requestFocusInWindow();
        }
    }

    // Hàm gọi màn hình Intro (Thành viên 1 & 2 dùng)
    public void showIntro() {
        setScreen(new IntroPanel(this), false);
    }

    // Hàm chuyển sang màn hình Game (Thành viên 2 gọi khi bấm nút Start)
    public void startGame() {
        GamePanel game = new GamePanel(this);
        setScreen(game, true);

        // Kích hoạt Game Loop
        game.startGameLoop();
    }

    public void showGameOver() {
        setScreen(new GameOverPanel(this), false);
    }

    public void showGameWin() {
        setScreen(new GameWinPanel(this), false);
    }

    public static void main(String[] args) {
        // Chạy GUI trên một luồng (thread) an toàn
        SwingUtilities.invokeLater(() -> {
            new MainGame().setVisible(true);
        });
    }
}
