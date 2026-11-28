package screens.panels;

import game.MainGame;
import java.awt.Color;

public class GameOverPanel extends EndScreenPanel {
    private static final String GAME_OVER_IMAGE_PATH = "/introoutro/game_over_panel.png";
    private static final Color TITLE_CRIMSON = new Color(196, 52, 46);

    public GameOverPanel(MainGame mainFrame) {
        super(mainFrame, GAME_OVER_IMAGE_PATH, "GAME OVER", TITLE_CRIMSON);
    }
}
