package screens.panels;

import game.MainGame;
import screens.AssetLoader;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class IntroPanel extends JPanel {
    private static final String INTRO_IMAGE_PATH = "/introoutro/game_intro_panel.png";
    private static final double REF_IMAGE_WIDTH = 2754.0;
    private static final double REF_IMAGE_HEIGHT = 1536.0;

    private static final double[][] MENU_BUTTON_NORM_BOUNDS = {
            {327.0 / REF_IMAGE_WIDTH, 630.0 / REF_IMAGE_HEIGHT, 637.0 / REF_IMAGE_WIDTH, 177.0 / REF_IMAGE_HEIGHT},   // PLAY
            {327.0 / REF_IMAGE_WIDTH, 839.0 / REF_IMAGE_HEIGHT, 637.0 / REF_IMAGE_WIDTH, 177.0 / REF_IMAGE_HEIGHT},   // OPTIONS
            {326.0 / REF_IMAGE_WIDTH, 1048.0 / REF_IMAGE_HEIGHT, 638.0 / REF_IMAGE_WIDTH, 176.0 / REF_IMAGE_HEIGHT}   // QUIT
    };
    private static final String[] MENU_BUTTON_LABELS = {"PLAY", "OPTIONS", "QUIT"};

    private static final int BTN_PLAY = 0;
    private static final int BTN_OPTIONS = 1;
    private static final int BTN_QUIT = 2;

    // Colors sampled to match the Backrooms pixel-art palette in gameintro.png
    private static final Color BUTTON_FILL = new Color(188, 148, 104);
    private static final Color BUTTON_FILL_HOVER = new Color(214, 174, 122);
    private static final Color BUTTON_FILL_PRESSED = new Color(150, 114, 76);
    private static final Color BUTTON_BORDER_DARK = new Color(78, 54, 38);
    private static final Color BUTTON_BORDER_LIGHT = new Color(224, 186, 138);
    private static final Color TITLE_CRIMSON = new Color(196, 52, 46);
    private static final Color HOVER_WASH = new Color(255, 226, 135, 48);
    private static final Color HOVER_GLOW = new Color(255, 196, 72, 95);
    private static final Color HOVER_FRAME = new Color(255, 232, 146, 230);
    private static final Color PRESSED_WASH = new Color(0, 0, 0, 72);
    private static final Color PRESSED_FRAME = new Color(212, 152, 68, 220);

    private static final String[][] CONTROL_ROWS = {
            {"W A S D / ARROWS", "Move"},
            {"SPACE", "Shoot / Attack"},
            {"Q", "Dash"},
            {"T", "Switch Weapon"},
            {"F", "Pick Up / Interact"},
            {"F11 / ALT+ENTER", "Toggle Fullscreen"}
    };

    private static BufferedImage cachedBackgroundImage;
    private static boolean backgroundLoadAttempted;

    private final MainGame mainFrame;
    private final BufferedImage backgroundImage;
    private BufferedImage scaledBackground;
    private int scaledWidth = -1;
    private int scaledHeight = -1;

    private int hoveredMenuIndex = -1;
    private int pressedMenuIndex = -1;
    private int selectedMenuIndex = -1;

    private boolean optionsOpen;
    private int hoveredOptionIndex = -1;   // 0 = fullscreen toggle, 1 = back
    private int pressedOptionIndex = -1;

    private Rectangle imageBounds = new Rectangle();
    private final Rectangle[] menuButtonBounds = {new Rectangle(), new Rectangle(), new Rectangle()};
    private Rectangle optionsModalBounds = new Rectangle();
    private Rectangle fullscreenButtonBounds = new Rectangle();
    private Rectangle backButtonBounds = new Rectangle();

    public IntroPanel(MainGame mainFrame) {
        this.mainFrame = mainFrame;
        this.backgroundImage = loadBackgroundImage();

        setPreferredSize(new Dimension(MainGame.WINDOW_WIDTH, MainGame.WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);

        installMouseListeners();
        installKeyBindings();
    }

    private static synchronized BufferedImage loadBackgroundImage() {
        if (!backgroundLoadAttempted) {
            cachedBackgroundImage = AssetLoader.loadImage(INTRO_IMAGE_PATH);
            backgroundLoadAttempted = true;
        }
        return cachedBackgroundImage;
    }

    public void updateFullscreenButtonText() {
        repaint();
    }

    private void installMouseListeners() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoverState(e.getPoint());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredMenuIndex != -1 || hoveredOptionIndex != -1) {
                    hoveredMenuIndex = -1;
                    hoveredOptionIndex = -1;
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
                Point p = e.getPoint();

                if (optionsOpen) {
                    pressedOptionIndex = findOptionButtonAt(p);
                    if (pressedOptionIndex == -1 && !optionsModalBounds.contains(p)) {
                        closeOptions();
                        return;
                    }
                    repaint();
                } else {
                    pressedMenuIndex = findMenuButtonAt(p);
                    if (pressedMenuIndex != -1) {
                        selectedMenuIndex = pressedMenuIndex;
                    }
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                updateLayoutBounds(getWidth(), getHeight());
                Point p = e.getPoint();

                if (optionsOpen) {
                    int released = findOptionButtonAt(p);
                    int pressed = pressedOptionIndex;
                    pressedOptionIndex = -1;
                    if (pressed != -1 && pressed == released) {
                        activateOptionButton(pressed);
                    }
                    updateHoverState(p);
                    repaint();
                } else {
                    int released = findMenuButtonAt(p);
                    int pressed = pressedMenuIndex;
                    pressedMenuIndex = -1;
                    if (pressed != -1 && pressed == released) {
                        activateMenuButton(pressed);
                    }
                    updateHoverState(p);
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateHoverState(e.getPoint());
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private void installKeyBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        bindKey(inputMap, actionMap, KeyEvent.VK_UP, "menuUp", () -> moveSelection(-1));
        bindKey(inputMap, actionMap, KeyEvent.VK_W, "menuUpW", () -> moveSelection(-1));
        bindKey(inputMap, actionMap, KeyEvent.VK_DOWN, "menuDown", () -> moveSelection(1));
        bindKey(inputMap, actionMap, KeyEvent.VK_S, "menuDownS", () -> moveSelection(1));
        bindKey(inputMap, actionMap, KeyEvent.VK_ENTER, "menuConfirm", this::confirmSelection);
        bindKey(inputMap, actionMap, KeyEvent.VK_SPACE, "menuConfirmSpace", this::confirmSelection);
        bindKey(inputMap, actionMap, KeyEvent.VK_ESCAPE, "menuEscape", () -> {
            if (optionsOpen) {
                closeOptions();
            }
        });
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

    private void moveSelection(int delta) {
        if (optionsOpen) {
            hoveredOptionIndex = hoveredOptionIndex == 0 ? 1 : 0;
            repaint();
            return;
        }
        if (selectedMenuIndex == -1) {
            selectedMenuIndex = delta > 0 ? BTN_PLAY : BTN_QUIT;
        } else {
            selectedMenuIndex = (selectedMenuIndex + delta + MENU_BUTTON_LABELS.length) % MENU_BUTTON_LABELS.length;
        }
        hoveredMenuIndex = selectedMenuIndex;
        repaint();
    }

    private void confirmSelection() {
        if (optionsOpen) {
            int target = hoveredOptionIndex >= 0 ? hoveredOptionIndex : 1;
            activateOptionButton(target);
            return;
        }
        int target = selectedMenuIndex >= 0 ? selectedMenuIndex : BTN_PLAY;
        activateMenuButton(target);
    }

    private void activateMenuButton(int index) {
        switch (index) {
            case BTN_PLAY -> mainFrame.startGame();
            case BTN_OPTIONS -> openOptions();
            case BTN_QUIT -> System.exit(0);
            default -> {
            }
        }
    }

    private void activateOptionButton(int index) {
        if (index == 0) {
            mainFrame.toggleFullscreen();
            updateFullscreenButtonText();
        } else if (index == 1) {
            closeOptions();
        }
    }

    private void openOptions() {
        optionsOpen = true;
        hoveredMenuIndex = -1;
        pressedMenuIndex = -1;
        hoveredOptionIndex = -1;
        pressedOptionIndex = -1;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    private void closeOptions() {
        optionsOpen = false;
        hoveredOptionIndex = -1;
        pressedOptionIndex = -1;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    private void updateHoverState(Point p) {
        updateLayoutBounds(getWidth(), getHeight());
        if (optionsOpen) {
            int nextHover = findOptionButtonAt(p);
            if (nextHover != hoveredOptionIndex) {
                hoveredOptionIndex = nextHover;
                setCursor(nextHover != -1 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                repaint();
            }
        } else {
            int nextHover = findMenuButtonAt(p);
            if (nextHover != hoveredMenuIndex) {
                hoveredMenuIndex = nextHover;
                if (nextHover != -1) {
                    selectedMenuIndex = nextHover;
                }
                setCursor(nextHover != -1 ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                repaint();
            }
        }
    }

    private int findMenuButtonAt(Point p) {
        for (int i = 0; i < menuButtonBounds.length; i++) {
            if (menuButtonBounds[i].contains(p)) {
                return i;
            }
        }
        return -1;
    }

    private int findOptionButtonAt(Point p) {
        if (fullscreenButtonBounds.contains(p)) {
            return 0;
        }
        if (backButtonBounds.contains(p)) {
            return 1;
        }
        return -1;
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

        for (int i = 0; i < MENU_BUTTON_NORM_BOUNDS.length; i++) {
            double[] norm = MENU_BUTTON_NORM_BOUNDS[i];
            int bx = drawX + (int) Math.round(norm[0] * drawW);
            int by = drawY + (int) Math.round(norm[1] * drawH);
            int bw = (int) Math.round(norm[2] * drawW);
            int bh = (int) Math.round(norm[3] * drawH);
            menuButtonBounds[i].setBounds(bx, by, bw, bh);
        }

        double uiScale = Math.min(panelW / (double) MainGame.WINDOW_WIDTH, panelH / (double) MainGame.WINDOW_HEIGHT);
        int modalW = (int) Math.round(520 * uiScale);
        int modalH = (int) Math.round(410 * uiScale);
        int modalX = (panelW - modalW) / 2;
        int modalY = (panelH - modalH) / 2;
        optionsModalBounds.setBounds(modalX, modalY, modalW, modalH);

        int btnW = (int) Math.round(360 * uiScale);
        int btnH = (int) Math.round(46 * uiScale);
        int btnX = modalX + (modalW - btnW) / 2;
        fullscreenButtonBounds.setBounds(btnX, modalY + (int) Math.round(82 * uiScale), btnW, btnH);

        int backW = (int) Math.round(180 * uiScale);
        int backH = (int) Math.round(44 * uiScale);
        backButtonBounds.setBounds(
                modalX + (modalW - backW) / 2,
                modalY + modalH - backH - (int) Math.round(24 * uiScale),
                backW,
                backH
        );
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

        // Progressive halving downscale keeps pixel-art edges and lettering crisp without aliasing shimmer.
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
                drawArtworkButtonHighlights(g2);
            } else {
                drawFallbackMenu(g2, panelW, panelH);
            }

            if (optionsOpen) {
                drawOptionsOverlay(g2, panelW, panelH);
            }
        } finally {
            g2.dispose();
        }
    }

    private void drawArtworkButtonHighlights(Graphics2D g2) {
        if (optionsOpen) {
            return;
        }

        int activeIndex = hoveredMenuIndex != -1 ? hoveredMenuIndex : selectedMenuIndex;
        if (activeIndex < 0 || activeIndex >= menuButtonBounds.length) {
            return;
        }

        Rectangle r = menuButtonBounds[activeIndex];
        boolean isPressed = (pressedMenuIndex == activeIndex && hoveredMenuIndex == activeIndex);

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
        int caretGap = Math.max(10, Math.round(r.height * 0.20f));
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

    private void drawFallbackMenu(Graphics2D g2, int panelW, int panelH) {
        g2.setColor(new Color(22, 20, 12));
        g2.fillRect(0, 0, panelW, panelH);

        double uiScale = Math.min(panelW / (double) MainGame.WINDOW_WIDTH, panelH / (double) MainGame.WINDOW_HEIGHT);
        Font titleFont = new Font("Monospaced", Font.BOLD, (int) Math.round(48 * uiScale));
        g2.setFont(titleFont);
        String title = "BACKROOMS CRAWLER";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (panelW - fm.stringWidth(title)) / 2;
        int ty = (int) Math.round(150 * uiScale);
        g2.setColor(Color.BLACK);
        g2.drawString(title, tx + 3, ty + 3);
        g2.setColor(TITLE_CRIMSON);
        g2.drawString(title, tx, ty);

        for (int i = 0; i < menuButtonBounds.length; i++) {
            boolean hovered = (hoveredMenuIndex == i || selectedMenuIndex == i);
            boolean pressed = (pressedMenuIndex == i && hoveredMenuIndex == i);
            drawStyledButton(g2, menuButtonBounds[i], MENU_BUTTON_LABELS[i], hovered, pressed, uiScale);
        }
    }

    private void drawOptionsOverlay(Graphics2D g2, int panelW, int panelH) {
        g2.setColor(new Color(8, 7, 4, 192));
        g2.fillRect(0, 0, panelW, panelH);

        double uiScale = Math.min(panelW / (double) MainGame.WINDOW_WIDTH, panelH / (double) MainGame.WINDOW_HEIGHT);
        Rectangle m = optionsModalBounds;

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(m.x + 6, m.y + 8, m.width, m.height, 14, 14);

        g2.setColor(new Color(28, 24, 17, 246));
        g2.fillRoundRect(m.x, m.y, m.width, m.height, 12, 12);

        // Double pixel-inspired border matching the artwork buttons
        g2.setColor(BUTTON_BORDER_DARK);
        g2.setStroke(new BasicStroke(Math.max(3f, (float) (4.0 * uiScale))));
        g2.drawRoundRect(m.x, m.y, m.width, m.height, 12, 12);

        g2.setColor(BUTTON_FILL);
        g2.setStroke(new BasicStroke(Math.max(1.5f, (float) (2.0 * uiScale))));
        g2.drawRoundRect(m.x + 4, m.y + 4, m.width - 8, m.height - 8, 9, 9);

        Font titleFont = new Font("Monospaced", Font.BOLD, Math.max(20, (int) Math.round(30 * uiScale)));
        g2.setFont(titleFont);
        FontMetrics titleFm = g2.getFontMetrics();
        String title = "OPTIONS";
        int titleX = m.x + (m.width - titleFm.stringWidth(title)) / 2;
        int titleY = m.y + (int) Math.round(52 * uiScale);
        g2.setColor(Color.BLACK);
        g2.drawString(title, titleX + 2, titleY + 2);
        g2.setColor(TITLE_CRIMSON);
        g2.drawString(title, titleX, titleY);

        boolean isFullscreen = mainFrame != null && mainFrame.isFullscreen();
        String fsLabel = isFullscreen
                ? "DISPLAY: FULLSCREEN (F11)"
                : "DISPLAY: WINDOWED (F11)";
        drawStyledButton(
                g2,
                fullscreenButtonBounds,
                fsLabel,
                hoveredOptionIndex == 0,
                pressedOptionIndex == 0 && hoveredOptionIndex == 0,
                uiScale
        );

        // Divider + Controls header
        int dividerY = fullscreenButtonBounds.y + fullscreenButtonBounds.height + (int) Math.round(22 * uiScale);
        g2.setColor(new Color(120, 96, 66, 150));
        g2.fillRect(m.x + (int) Math.round(36 * uiScale), dividerY, m.width - (int) Math.round(72 * uiScale), 1);

        Font sectionFont = new Font("Monospaced", Font.BOLD, Math.max(13, (int) Math.round(16 * uiScale)));
        g2.setFont(sectionFont);
        g2.setColor(BUTTON_BORDER_LIGHT);
        String controlsTitle = "CONTROLS";
        int controlsTitleX = m.x + (m.width - g2.getFontMetrics().stringWidth(controlsTitle)) / 2;
        int controlsTitleY = dividerY + (int) Math.round(26 * uiScale);
        g2.drawString(controlsTitle, controlsTitleX, controlsTitleY);

        // Control rows
        Font keyFont = new Font("Monospaced", Font.BOLD, Math.max(11, (int) Math.round(14 * uiScale)));
        Font descFont = new Font("Monospaced", Font.PLAIN, Math.max(11, (int) Math.round(14 * uiScale)));
        int rowStartY = controlsTitleY + (int) Math.round(24 * uiScale);
        int rowStep = Math.max(18, (int) Math.round(24 * uiScale));
        int leftColX = m.x + (int) Math.round(54 * uiScale);
        int rightColX = m.x + m.width - (int) Math.round(54 * uiScale);

        for (int i = 0; i < CONTROL_ROWS.length; i++) {
            int ry = rowStartY + i * rowStep;
            g2.setFont(keyFont);
            g2.setColor(HOVER_FRAME);
            g2.drawString(CONTROL_ROWS[i][0], leftColX, ry);

            g2.setFont(descFont);
            g2.setColor(new Color(224, 214, 192));
            String desc = CONTROL_ROWS[i][1];
            int descW = g2.getFontMetrics().stringWidth(desc);
            g2.drawString(desc, rightColX - descW, ry);
        }

        drawStyledButton(
                g2,
                backButtonBounds,
                "BACK",
                hoveredOptionIndex == 1,
                pressedOptionIndex == 1 && hoveredOptionIndex == 1,
                uiScale
        );
    }

    private void drawStyledButton(Graphics2D g2, Rectangle r, String text,
                                  boolean hovered, boolean pressed, double uiScale) {
        int arc = Math.max(4, (int) Math.round(6 * uiScale));

        g2.setColor(BUTTON_BORDER_DARK);
        g2.fillRoundRect(r.x, r.y + 3, r.width, r.height, arc, arc);

        Color fill = pressed ? BUTTON_FILL_PRESSED : (hovered ? BUTTON_FILL_HOVER : BUTTON_FILL);
        int offsetY = pressed ? 2 : 0;
        g2.setColor(fill);
        g2.fillRoundRect(r.x, r.y + offsetY, r.width, r.height - 2, arc, arc);

        g2.setColor(hovered ? HOVER_FRAME : BUTTON_BORDER_LIGHT);
        g2.setStroke(new BasicStroke(Math.max(1.5f, (float) (2.0 * uiScale))));
        g2.drawRoundRect(r.x + 1, r.y + offsetY + 1, r.width - 2, r.height - 4, arc, arc);

        Font btnFont = new Font("Monospaced", Font.BOLD, Math.max(13, (int) Math.round(18 * uiScale)));
        g2.setFont(btnFont);
        FontMetrics fm = g2.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(text)) / 2;
        int ty = r.y + offsetY + (r.height - 2 - fm.getHeight()) / 2 + fm.getAscent();
        g2.setColor(new Color(20, 14, 10));
        g2.drawString(text, tx, ty);
    }
}
