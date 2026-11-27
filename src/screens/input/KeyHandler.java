package screens.input;

import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, rightPressed, leftPressed;
    public boolean spacePressed;
    private boolean dashDown;
    private boolean dashPressed;
    private boolean weaponSwitchDown;
    private boolean weaponSwitchPressed;
    private boolean pickupDown;
    private final AtomicBoolean pickupPressed = new AtomicBoolean();
    private final AtomicBoolean closeNotePressed = new AtomicBoolean();

    public boolean consumeCloseNotePress() {
        return closeNotePressed.getAndSet(false);
    }

    public boolean consumePickupPress() {
        return pickupPressed.getAndSet(false);
    }

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
        if (code == KeyEvent.VK_ESCAPE) closeNotePressed.set(true);
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
        if (code == KeyEvent.VK_F && !pickupDown) {
            pickupDown = true;
            pickupPressed.set(true);
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
        if (code == KeyEvent.VK_F) pickupDown = false;
    }
    @Override
    public void keyTyped(KeyEvent e) {} // Not needed
}

