package screens;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public final class AssetLoader {
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
}
