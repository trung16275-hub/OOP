package model.player;

import java.util.HashMap;
import java.util.Map;

public final class MovementEffects {
    private record Slow(double multiplier, long expiresAtNanos) {
    }

    private final Map<Object, Slow> slows = new HashMap<>();

    public void applySlow(Object source, double multiplier, long durationNanos) {
        if (source == null || !(multiplier >= 0 && multiplier < 1) || durationNanos <= 0) {
            throw new IllegalArgumentException("Slow requires a source, a multiplier in [0, 1), and a positive duration");
        }
        slows.put(source, new Slow(multiplier, System.nanoTime() + durationNanos));
    }

    public double getSpeedMultiplier() {
        long now = System.nanoTime();
        slows.values().removeIf(slow -> now - slow.expiresAtNanos() >= 0);
        double multiplier = 1.0;
        for (Slow slow : slows.values()) {
            multiplier = Math.min(multiplier, slow.multiplier());
        }
        return multiplier;
    }

    public void clear() {
        slows.clear();
    }
}
