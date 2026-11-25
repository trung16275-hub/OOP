package screens.lighting;

import game.MainGame;
import map.Room;
import model.player.Player;
import screens.AssetLoader;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class LightSystem {
    private final BufferedImage lightMap;
    private BufferedImage lampFixtureImage;
    private final BufferedImage tubeLightMask;
    private final Graphics2D lightG;

    // Darkness level: 0 = bright, 255 = pitch black
    private Color ambientDarkness = new Color(5, 5, 10, 160);
    private double elapsedTime = 0;

    public LightSystem() {
        this.lightMap = new BufferedImage(
                MainGame.WINDOW_WIDTH,
                MainGame.WINDOW_HEIGHT,
                BufferedImage.TYPE_INT_ARGB
        );
        this.lightG = lightMap.createGraphics();
        this.lightG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        lampFixtureImage = AssetLoader.loadImage("/props/lamp_fixture.png");
        this.tubeLightMask = createTubeLightMask(440, 120, 36.0, 205.0);
    }

    public void update(Room room, double deltaSeconds) {
        elapsedTime += deltaSeconds;
        if (room != null) {
            for (LightSource light : room.getLights()) {
                light.update(elapsedTime);
            }
        }
    }

    public void render(Graphics2D screenG2, Player player, Room room, int camX, int camY) {
        // 1. Reset lightMap with full ambient darkness
        lightG.setComposite(AlphaComposite.Src);
        lightG.setColor(ambientDarkness);
        lightG.fillRect(0, 0, lightMap.getWidth(), lightMap.getHeight());

        // 2. Switch to DstOut so lights erase darkness proportionally to their alpha
        lightG.setComposite(AlphaComposite.DstOut);

        // Carve static room lights (ceiling tubes, etc.)
        if (room != null) {
            for (LightSource light : room.getLights()) {
                drawRoomLightCutout(light, camX, camY);
            }
        }

        // 3. Composite the carved darkness mask onto the main screen
        screenG2.drawImage(lightMap, 0, 0, null);

        // 4. Optional subtle additive/translucent color tint for atmosphere
        if (room != null) {
            drawColorTints(screenG2, player, room, camX, camY);
        }
    }

    private void drawRadialCutout(float sx, float sy, float radius, float intensity) {
        if (radius <= 0 || intensity <= 0) return;
        int maxAlpha = (int) (255 * Math.min(1f, intensity));

        RadialGradientPaint paint = new RadialGradientPaint(
                sx, sy, radius,
                new float[]{0f, 0.55f, 1f},
                new Color[]{
                        new Color(0, 0, 0, maxAlpha),
                        new Color(0, 0, 0, (int) (maxAlpha * 0.5f)),
                        new Color(0, 0, 0, 0)
                }
        );
        lightG.setPaint(paint);
        lightG.fillOval((int) (sx - radius), (int) (sy - radius), (int) (radius * 2), (int) (radius * 2));
    }

    private void drawRoomLightCutout(LightSource light, int camX, int camY) {
        float sx = (float) (light.getWorldX() - camX);
        float sy = (float) (light.getWorldY() - camY);
        float intensity = light.getCurrentIntensity();

        if (light.getType() == LightSource.Type.FLUORESCENT_TUBE) {
            Composite oldComp = lightG.getComposite();
            lightG.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT, intensity));

            int drawX = Math.round(sx - tubeLightMask.getWidth() / 2.0f);
            int drawY = Math.round(sy - 10); // Bottom edge of lampFixtureImage (sy - 20 + 12)
            lightG.drawImage(tubeLightMask, drawX, drawY, null);

            lightG.setComposite(oldComp);
        } else {
            drawRadialCutout(sx, sy, light.getRadius(), intensity);
        }
    }

    private void drawColorTints(Graphics2D screenG2, Player player, Room room, int camX, int camY) {
        Composite oldComp = screenG2.getComposite();

        for (LightSource light : room.getLights()) {
            if (light.getType() == LightSource.Type.FLUORESCENT_TUBE) {
                int sx = (int) (light.getWorldX() - camX);
                int sy = (int) (light.getWorldY() - camY);
                float intensity = light.getCurrentIntensity();

                // 1. Subtle warm tint across the flared cone
                int maskX = Math.round(sx - tubeLightMask.getWidth() / 2.0f);
                int maskY = Math.round(sy - 10);
                screenG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.14f * intensity));
                screenG2.drawImage(tubeLightMask, maskX, maskY, null);

                // 2. Draw the fixture sprite at full opacity, then dim it if flickering
                screenG2.setComposite(oldComp);
                if (lampFixtureImage != null) {
                    int fx = sx - 36;
                    int fy = sy - 20;
                    screenG2.drawImage(lampFixtureImage, fx, fy, 72, 12, null);

                    if (intensity < 0.99f) {
                        int dimAlpha = Math.min(200, Math.round((1f - intensity) * 200));
                        screenG2.setColor(new Color(5, 5, 10, dimAlpha));
                        screenG2.fillRect(fx, fy, 72, 12);
                    }
                }
            }
        }

        screenG2.setComposite(oldComp);
    }

    private BufferedImage createTubeLightMask(int w, int h, double topHalfW, double botHalfW) {
        BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        double cx = w / 2.0;

        for (int y = 0; y < h; y++) {
            double t = (double) y / (h - 1);
            double halfW = topHalfW + (botHalfW - topHalfW) * Math.pow(t, 0.85);
            double vFade = Math.pow(1.0 - t, 1.25);

            for (int x = 0; x < w; x++) {
                double normX = Math.abs(x - cx) / halfW;
                if (normX >= 1.0) continue;

                double edge = Math.min(1.0, (1.0 - normX) / 0.60);
                double hFade = edge * edge * (3.0 - 2.0 * edge);

                int alpha = (int) Math.round(255.0 * vFade * hFade);
                mask.setRGB(x, y, (alpha << 24) | 0x00FFF2C8);
            }
        }
        return mask;
    }

    public void dispose() {
        lightG.dispose();
    }
}

