package model.weapon; // Đổi lại theo đúng package chứa Weapon của bạn

import java.util.List;

import model.Enemy;
import model.Player;
import model.boss.Boss4;

public class AlmondWater extends Weapon {
    private int usesLeft;
    private final double repelRadius;
    private final long durationNanos;

    public AlmondWater(int initialUses) {
        super("Almond Water", 0); // Không gây sát thương trực tiếp
        this.usesLeft = initialUses;
        this.repelRadius = 180.0;            // Bán kính vùng an toàn (pixel)
        this.durationNanos = 4_000_000_000L; // Hiệu lực trong 4 giây
    }

    public boolean use(Player player, List<Enemy> enemies) {
        if (usesLeft <= 0 || player == null) {
            return false; // Hết nước hạnh nhân
        }

        usesLeft--;

        double centerX = player.getX() + player.getWidth() / 2.0;
        double centerY = player.getY() + player.getHeight() / 2.0;

        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy instanceof Boss4) {
                    ((Boss4) enemy).onAlmondWaterUsed(centerX, centerY, repelRadius, durationNanos);
                }
            }
        }
        return true;
    }

    public void addUses(int amount) {
        if (amount > 0) {
            this.usesLeft += amount;
        }
    }

    public int getUsesLeft() {
        return usesLeft;
    }

    public double getRepelRadius() {
        return repelRadius;
    }
}