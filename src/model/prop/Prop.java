package model.prop;

import java.awt.Rectangle;

/** An object placed at a pixel position within a room. */
public record Prop(String spriteKey, int x, int y,
                   int width, int height, boolean solid) {

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}
