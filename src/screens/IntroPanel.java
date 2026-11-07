package screens;

import game.MainGame;
import java.awt.*;
import javax.swing.*;

public class IntroPanel extends JPanel {
    private MainGame mainFrame;

    public IntroPanel(MainGame mainFrame) {
        this.mainFrame = mainFrame;

        // Thiết lập layout và màu nền cơ bản
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        // --- GIAO DIỆN (Nhiệm vụ của TV 1 & 3) ---
        JLabel titleLabel = new JLabel("HẦM NGỤC VÔ TẬN");
        titleLabel.setForeground(Color.RED);
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 40));

        JButton startBtn = new JButton("Bắt Đầu");
        JButton exitBtn = new JButton("Thoát");

        // --- XỬ LÝ LOGIC CHUYỂN CẢNH (Nhiệm vụ của TV 2) ---
        startBtn.addActionListener(e -> {
            // Khi bấm nút Start, gọi hàm chuyển cảnh từ MainGame
            mainFrame.startGame(); 
        });

        exitBtn.addActionListener(e -> {
            System.exit(0); // Tắt chương trình
        });

        // --- SẮP XẾP BỐ CỤC ---
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        btnPanel.setOpaque(false); // Trong suốt để hiển thị nền đen
        btnPanel.add(startBtn);
        btnPanel.add(exitBtn);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 60, 0); // Khoảng cách
        add(titleLabel, gbc);

        gbc.gridy = 1;
        add(btnPanel, gbc);
    }
}
