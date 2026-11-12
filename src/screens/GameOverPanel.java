package screens;

import game.MainGame;
import java.awt.*;
import javax.swing.*;

public class GameOverPanel extends JPanel {
    public GameOverPanel(MainGame mainFrame) {
        setLayout(new GridBagLayout());
        setBackground(new Color(25, 25, 30));

        JLabel title = new JLabel("GAME OVER");
        title.setFont(new Font("Monospaced", Font.BOLD, 56));
        title.setForeground(Color.RED);

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        exitButton.setToolTipText("Return to main menu");
        exitButton.addActionListener(e -> mainFrame.showIntro());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 40, 0);
        add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.ipadx = 60;
        gbc.ipady = 10;
        add(exitButton, gbc);
    }
}
