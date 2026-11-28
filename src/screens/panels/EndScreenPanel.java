package screens.panels;

import game.MainGame;
import screens.AssetLoader;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

abstract class EndScreenPanel extends JPanel {
    private static final double REF_IMAGE_WIDTH = 2752.0;
    private static final double REF_IMAGE_HEIGHT = 1536.0;

    // Normalized bounding box aligned to the centered MAIN MENU button in game_over_panel.png / game_win_panel.png
    private static final double MAIN_MENU_NORM_X = 1011.0 / REF_IMAGE_WIDTH;
    private static final double MAIN_MENU_NORM_Y = 1128.0 / REF_IMAGE_HEIGHT;
    private static final double MAIN_MENU_NORM_W = 730.0 / REF_IMAGE_WIDTH;
    private static final double MAIN_MENU_NORM_H = 196.0 / REF_IMAGE_HEIGHT;

    private static final Color BUTTON_FILL = new Color(188, 148, 104);
    private static final Color BUTTON_FILL_HOVER = new Color(214, 174, 122);
    private static final Color BUTTON_FILL_PRESSED = new Color(150, 114, 76);
    private static final Color BUTTON_BORDER_DARK = new Color(78, 54, 38);
    private static final Color BUTTON_BORDER_LIGHT = new Color(224, 186, 138);
    private static final Color HOVER_WASH = new Color(255, 226, 135, 48);
    private static final Color HOVER_GLOW = new Color(255, 196, 72, 95);
    private static final Color HOVER_FRAME = new Color(255, 232, 146, 230);
    private static final Color PRESSED_WASH = new Color(0, 0, 0, 72);
    private static final Color PRESSED_FRAME = new Color(212, 152, 68, 220);

    private static final Map<String, BufferedImage> IMAGE_CACHE = new HashMap<>();

    private final MainGame mainFrame;
    private final String fallbackTitle;
    private final Color fallbackTitleColor;
    private final BufferedImage backgroundImage;

    private BufferedImage scaledBackground;
    private int scaledWidth = -1;
    private int scaledHeight = -1;

    private boolean buttonHovered;
    private boolean buttonPressed;

    private final Rectangle imageBounds = new Rectangle();
    private final Rectangle mainMenuButtonBounds = new Rectangle();

    protected EndScreenPanel(MainGame mainFrame, String imagePath, String fallbackTitle, Color fallbackTitleColor) {
        this.mainFrame = mainFrame;
        this.fallbackTitle = fallbackTitle;
        this.fallbackTitleColor = fallbackTitleColor;
        this.backgroundImage = loadCachedImage(imagePath);

        setPreferredSize(new Dimension(MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);

        installMouseListeners();
        installKeyBindings();
    }

    private static synchronized BufferedImage loadCachedImage(String path) {
        if (!IMAGE_CACHE.containsKey(path)) {
            IMAGE_CACHE.put(path, AssetLoader.loadImage(path));
        }
        return IMAGE_CACHE.get(path);
    }

    private void installMouseListeners() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(e.getPoint());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateHover(e.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (buttonHovered) {
                    buttonHovered = false;
                    setCursor(Cursor.getDefaultCursor());
                    repaint();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                updateLayoutBounds(getWidth(), getHeight());
                if (mainMenuButtonBounds.contains(e.getPoint())) {
                    buttonPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                updateLayoutBounds(getWidth(), getHeight());
                boolean wasPressed = buttonPressed;
                buttonPressed = false;
                boolean inside = mainMenuButtonBounds.contains(e.getPoint());
                updateHover(e.getPoint());
                repaint();
                if (wasPressed && inside) {
                    returnToMainMenu();
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void installKeyBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        bindKey(inputMap, actionMap, KeyEvent.VK_ENTER, "returnMenuEnter", this::returnToMainMenu);
        bindKey(inputMap, actionMap, KeyEvent.VK_SPACE, "returnMenuSpace", this::returnToMainMenu);
        bindKey(inputMap, actionMap, KeyEvent.VK_ESCAPE, "returnMenuEscape", this::returnToMainMenu);
    }

    private static void bindKey(InputMap inputMap, ActionMap actionMap, int keyCode, String key, Runnable action) {
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), key);
        actionMap.put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                action.run();
            }
        });
    }

    private void returnToMainMenu() {
        if (mainFrame != null) {
            mainFrame.showIntro();
        }
    }

    private void updateHover(Point p) {
        updateLayoutBounds(getWidth(), getHeight());
        boolean nextHover = mainMenuButtonBounds.contains(p);
        if (nextHover != buttonHovered) {
            buttonHovered = nextHover;
            setCursor(nextHover ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }
    }

    private void updateLayoutBounds(int panelW, int panelH) {
        if (panelW <= 0 || panelH <= 0) {
            return;
        }

        double imgW = backgroundImage != null ? backgroundImage.getWidth() : REF_IMAGE_WIDTH;
        double imgH = backgroundImage != null ? backgroundImage.getHeight() : REF_IMAGE_HEIGHT;
        double panelAspect = (double) panelW / panelH;

        double scale = (panelAspect >= 1.5 && panelAspect <= 2.1)
                ? Math.max(panelW / imgW, panelH / imgH)
                : Math.min(panelW / imgW, panelH / imgH);

        int drawW = (int) Math.round(imgW * scale);
        int drawH = (int) Math.round(imgH * scale);
        int drawX = (panelW - drawW) / 2;
        int drawY = (panelH - drawH) / 2;
        imageBounds.setBounds(drawX, drawY, drawW, drawH);

        int bx = drawX + (int) Math.round(MAIN_MENU_NORM_X * drawW);
        int by = drawY + (int) Math.round(MAIN_MENU_NORM_Y * drawH);
        int bw = (int) Math.round(MAIN_MENU_NORM_W * drawW);
        int bh = (int) Math.round(MAIN_MENU_NORM_H * drawH);
        mainMenuButtonBounds.setBounds(bx, by, bw, bh);
    }

    private BufferedImage getScaledBackground(int targetW, int targetH) {
        if (backgroundImage == null || targetW <= 0 || targetH <= 0) {
            return null;
        }
        if (scaledBackground != null && scaledWidth == targetW && scaledHeight == targetH) {
            return scaledBackground;
        }

        BufferedImage current = backgroundImage;
        int w = current.getWidth();
        int h = current.getHeight();

        while (w / 2 >= targetW && h / 2 >= targetH) {
            w = Math.max(targetW, w / 2);
            h = Math.max(targetH, h / 2);
            current = scaleStep(current, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }

        if (w != targetW || h != targetH) {
            current = scaleStep(current, targetW, targetH, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }

        scaledBackground = current;
        scaledWidth = targetW;
        scaledHeight = targetH;
        return scaledBackground;
    }

    private static BufferedImage scaleStep(BufferedImage src, int w, int h, Object interpolationHint) {
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationHint);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }
        return dst;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int panelW = getWidth();
        int panelH = getHeight();
        if (panelW <= 0 || panelH <= 0) {
            return;
        }

        updateLayoutBounds(panelW, panelH);

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, panelW, panelH);

            BufferedImage bg = getScaledBackground(imageBounds.width, imageBounds.height);
            if (bg != null) {
                g2.drawImage(bg, imageBounds.x, imageBounds.y, null);
                drawArtworkButtonHighlight(g2);
            } else {
                drawFallbackScreen(g2, panelW, panelH);
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawArtworkButtonHighlight(Graphics2D g2) {
        if (!buttonHovered && !buttonPressed) {
            return;
        }

        Rectangle r = mainMenuButtonBounds;
        boolean isPressed = buttonPressed && buttonHovered;

        int inset = Math.max(1, Math.round(r.height * 0.03f));
        int x = r.x + inset;
        int y = r.y + inset;
        int w = r.width - inset * 2;
        int h = r.height - inset * 2;
        int arc = Math.max(4, Math.round(r.height * 0.12f));

        g2.setColor(HOVER_GLOW);
        g2.setStroke(new BasicStroke(Math.max(3f, r.height * 0.08f)));
        g2.drawRoundRect(x - 1, y - 1, w + 2, h + 2, arc + 2, arc + 2);

        g2.setColor(isPressed ? PRESSED_WASH : HOVER_WASH);
        g2.fillRoundRect(x, y, w, h, arc, arc);

        g2.setColor(isPressed ? PRESSED_FRAME : HOVER_FRAME);
        g2.setStroke(new BasicStroke(Math.max(2f, r.height * 0.045f)));
        g2.drawRoundRect(x, y, w, h, arc, arc);

        int caretSize = Math.max(8, Math.round(r.height * 0.24f));
        int caretGap = Math.max(10, Math.round(r.height * 0.18f));
        int tipX = r.x - caretGap + (isPressed ? 2 : 0);
        int centerY = r.y + r.height / 2;
        Polygon caret = new Polygon(
                new int[]{tipX - caretSize, tipX, tipX - caretSize},
                new int[]{centerY - caretSize / 2, centerY, centerY + caretSize / 2},
                3
        );
        g2.setColor(new Color(0, 0, 0, 160));
        g2.translate(2, 2);
        g2.fillPolygon(caret);
        g2.translate(-2, -2);
        g2.setColor(isPressed ? PRESSED_FRAME : HOVER_FRAME);
        g2.fillPolygon(caret);
    }

    private void drawFallbackScreen(Graphics2D g2, int panelW, int panelH) {
        g2.setColor(new Color(22, 20, 14));
        g2.fillRect(0, 0, panelW, panelH);

        double uiScale = Math.min(panelW / (double) MainGame.WINDOW_WIDTH, panelH / (double) MainGame.WINDOW_HEIGHT);
        Font titleFont = new Font("Monospaced", Font.BOLD, Math.max(28, (int) Math.round(56 * uiScale)));
        g2.setFont(titleFont);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (panelW - fm.stringWidth(fallbackTitle)) / 2;
        int ty = (int) Math.round(210 * uiScale);
        g2.setColor(Color.BLACK);
        g2.drawString(fallbackTitle, tx + 3, ty + 3);
        g2.setColor(fallbackTitleColor);
        g2.drawString(fallbackTitle, tx, ty);

        Rectangle r = mainMenuButtonBounds;
        int arc = Math.max(4, (int) Math.round(6 * uiScale));
        boolean pressed = buttonPressed && buttonHovered;

        g2.setColor(BUTTON_BORDER_DARK);
        g2.fillRoundRect(r.x, r.y + 3, r.width, r.height, arc, arc);

        Color fill = pressed ? BUTTON_FILL_PRESSED : (buttonHovered ? BUTTON_FILL_HOVER : BUTTON_FILL);
        int offsetY = pressed ? 2 : 0;
        g2.setColor(fill);
        g2.fillRoundRect(r.x, r.y + offsetY, r.width, r.height - 2, arc, arc);

        g2.setColor(buttonHovered ? HOVER_FRAME : BUTTON_BORDER_LIGHT);
        g2.setStroke(new BasicStroke(Math.max(1.5f, (float) (2.0 * uiScale))));
        g2.drawRoundRect(r.x + 1, r.y + offsetY + 1, r.width - 2, r.height - 4, arc, arc);

        Font btnFont = new Font("Monospaced", Font.BOLD, Math.max(14, (int) Math.round(22 * uiScale)));
        g2.setFont(btnFont);
        FontMetrics bfm = g2.getFontMetrics();
        String label = "MAIN MENU";
        int bx = r.x + (r.width - bfm.stringWidth(label)) / 2;
        int by = r.y + offsetY + (r.height - 2 - bfm.getHeight()) / 2 + bfm.getAscent();
        g2.setColor(new Color(20, 14, 10));
        g2.drawString(label, bx, by);
    }
}
