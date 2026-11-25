package screens.input;

import java.awt.event.*;

public class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, rightPressed, leftPressed;
    public boolean spacePressed;

    private boolean weaponSwitchDown;
    private boolean weaponSwitchPressed;
<<<<<<< HEAD

    // Thêm 2 biến bắt sự kiện bấm 1 lần (dùng cho Nước Hạnh Nhân & vùng thoát)
    private boolean spaceJustPressed;
    private int struggleTapCount;

=======
    // Thêm 2 biến bắt sự kiện bấm 1 lần (dùng cho Nước Hạnh Nhân & vùng thoát)
    private boolean spaceJustPressed;
    private int struggleTapCount;
>>>>>>> 45849b59d5baf0c5d133f97e632b41c71e4e5fbf
    public boolean consumeWeaponSwitchPress() {
        boolean pressed = weaponSwitchPressed;
        weaponSwitchPressed = false;
        return pressed;
    }
    public boolean consumeSpacePress() {
        boolean pressed = spaceJustPressed;
        spaceJustPressed = false;
        return pressed;
    }

    public int consumeStruggleTaps() {
        int taps = struggleTapCount;
        struggleTapCount = 0;
        return taps;
    }

    public boolean consumeSpacePress() {
        boolean pressed = spaceJustPressed;
        spaceJustPressed = false;
        return pressed;
    }

    public int consumeStruggleTaps() {
        int taps = struggleTapCount;
        struggleTapCount = 0;
        return taps;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
<<<<<<< HEAD
=======
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)    upPressed = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  downPressed = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)  leftPressed = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = true;
        if (code == KeyEvent.VK_SPACE) spacePressed = true;
>>>>>>> 45849b59d5baf0c5d133f97e632b41c71e4e5fbf
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            if (!upPressed) struggleTapCount++;
            upPressed = true;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            if (!downPressed) struggleTapCount++;
            downPressed = true;
        }
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            if (!leftPressed) struggleTapCount++;
            leftPressed = true;
        }
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            if (!rightPressed) struggleTapCount++;
            rightPressed = true;
        }
        if (code == KeyEvent.VK_SPACE) {
            if (!spacePressed) {
                spaceJustPressed = true;
                struggleTapCount++;
            }
            spacePressed = true;
        }
        if (code == KeyEvent.VK_T && !weaponSwitchDown) {
            weaponSwitchDown = true;
            weaponSwitchPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)    upPressed = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  downPressed = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)  leftPressed = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = false;
        if (code == KeyEvent.VK_SPACE) spacePressed = false;
        if (code == KeyEvent.VK_T) weaponSwitchDown = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}

