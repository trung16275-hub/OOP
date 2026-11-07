package game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import screens.GamePanel;
import screens.IntroPanel;

public class MainGame extends JFrame {
    
    public MainGame() {
        setTitle("Dungeon Crawler Project");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Căn giữa màn hình
        setResizable(false);
        
        // Mặc định khi bật game lên sẽ nạp màn hình Intro
        showIntro();
    }

    // Hàm gọi màn hình Intro (Thành viên 1 & 2 dùng)
    public void showIntro() {
        IntroPanel intro = new IntroPanel(this);
        setContentPane(intro);
        revalidate(); // Báo cho hệ thống cập nhật lại giao diện
        repaint();    // Vẽ lại màn hình
    }

    // Hàm chuyển sang màn hình Game (Thành viên 2 gọi khi bấm nút Start)
    public void startGame() {
        GamePanel game = new GamePanel(this);
        setContentPane(game);
        revalidate();
        repaint();
        
        // Yêu cầu Focus để nhận sự kiện bàn phím
        game.requestFocusInWindow(); 
        
        // Kích hoạt Game Loop
        game.startGameLoop(); 
    }

    public static void main(String[] args) {
        // Chạy GUI trên một luồng (thread) an toàn
        SwingUtilities.invokeLater(() -> {
            new MainGame().setVisible(true);
        });
    }
}
