package screens;

import game.MainGame;
import java.awt.Color;

public class GameWinPanel extends EndScreenPanel {
    private static final String GAME_WIN_IMAGE_PATH = "/introoutro/game_win_panel.png";
    private static final Color TITLE_CRIMSON = new Color(196, 52, 46);

    public GameWinPanel(MainGame mainFrame) {
        super(mainFrame, GAME_WIN_IMAGE_PATH, "VICTORY", TITLE_CRIMSON);
    }
}
