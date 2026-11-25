package screens.input;

import java.awt.event.*;

public class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, rightPressed, leftPressed;
    public boolean spacePressed;
    private boolean dashDown;
    private boolean dashPressed;
    private boolean weaponSwitchDown;
    private boolean weaponSwitchPressed;

    public boolean consumeWeaponSwitchPress() {
        boolean pressed = weaponSwitchPressed;
        weaponSwitchPressed = false;
        return pressed;
    }

    public boolean consumeDashPress() {
        boolean pressed = dashPressed;
        dashPressed = false;
        return pressed;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP)    upPressed = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN)  downPressed = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT)  leftPressed = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = true;
        if (code == KeyEvent.VK_SPACE) spacePressed = true;
        if (code == KeyEvent.VK_Q && !dashDown) {
            dashDown = true;
            dashPressed = true;
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
        if (code == KeyEvent.VK_Q) dashDown = false;
        if (code == KeyEvent.VK_T) weaponSwitchDown = false;
    }
    @Override
    public void keyTyped(KeyEvent e) {} // Not needed
}

