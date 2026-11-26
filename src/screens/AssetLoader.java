package screens;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;
public class AssetLoader {
    private static final Map<String, BufferedImage[]> SPRITE_CACHE = new HashMap<>();

    public static BufferedImage loadImage(String path) {
        String cleanPath = path.startsWith("/") ? path : "/" + path;
        try (InputStream is = AssetLoader.class.getResourceAsStream(cleanPath)) {
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (IOException e) {
            System.err.println("Error reading classpath asset: " + path);
        }

        // Fallback for direct filesystem reading
        File file = new File("assets", path.startsWith("/") ? path.substring(1) : path);
        if (file.exists()) {
            try {
                return ImageIO.read(file);
            } catch (IOException e) {
                System.err.println("Error reading file asset: " + file.getPath());
            }
        }

        System.err.println("Asset not found: " + path);
        return null;
    }

    public static BufferedImage[] getSpriteSheet(String path, int frameCount) {
        if (path == null || frameCount <= 0) return null;

        String cacheKey = path + "#" + frameCount;
        if (SPRITE_CACHE.containsKey(cacheKey)) {
            return SPRITE_CACHE.get(cacheKey);
        }

        // Nạp bộ frame cắt rời: path_0.png, path_1.png...
        if (!path.endsWith(".png") && !path.endsWith(".jpg")) {
            BufferedImage[] frames = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = loadImage(path + "_" + i + ".png");
                if (frames[i] == null) {
                    return null;
                }
            }
            SPRITE_CACHE.put(cacheKey, frames);
            return frames;
        }

        // Nạp spritesheet 1 hàng ngang
        BufferedImage sheet = loadImage(path);
        if (sheet == null) return null;

        int frameWidth = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }

        SPRITE_CACHE.put(cacheKey, frames);
        return frames;
    }
}
