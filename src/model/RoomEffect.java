package model;

import java.awt.Graphics2D;
import model.player.Player;

public interface RoomEffect {
    void update(Player player);
    boolean isExpired();
    void render(Graphics2D graphics);
}
