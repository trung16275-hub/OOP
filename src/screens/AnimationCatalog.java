package screens;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/** Loads sprite animations from assets/animations.properties. */
public final class AnimationCatalog {
    private static final String MANIFEST_NAME = "animations.properties";
    private static final AnimationCatalog INSTANCE = new AnimationCatalog();

    private final Map<String, BufferedImage[]> animations = new HashMap<>();

    private AnimationCatalog() {
        Properties manifest = loadManifest();
        for (String property : manifest.stringPropertyNames()) {
            if (property.startsWith("animation.")) {
                String key = property.substring("animation.".length());
                loadAnimation(key, manifest.getProperty(property).trim());
            }
        }
    }

    public static AnimationCatalog getInstance() {
        return INSTANCE;
    }

    /** Keys use the format <sprite key>.<state>, for example faceling.walk. */
    public BufferedImage[] getFrames(String key) {
        return animations.get(key);
    }

    private Properties loadManifest() {
        Properties properties = new Properties();
        try (InputStream stream = AnimationCatalog.class.getResourceAsStream("/" + MANIFEST_NAME)) {
            if (stream != null) {
                properties.load(stream);
                return properties;
            }
        } catch (IOException e) {
            System.err.println("Cannot read animation manifest from classpath: " + e.getMessage());
        }

        File file = new File("assets", MANIFEST_NAME);
        if (!file.isFile()) {
            System.err.println("Animation manifest not found: " + file.getPath());
            return properties;
        }
        try (InputStream stream = new java.io.FileInputStream(file)) {
            properties.load(stream);
        } catch (IOException e) {
            System.err.println("Cannot read animation manifest " + file.getPath() + ": " + e.getMessage());
        }
        return properties;
    }

    private void loadAnimation(String key, String definition) {
        String[] parts = definition.split("\\|", 3);
        if (parts.length < 2) {
            System.err.println("Invalid animation definition for " + key + ": " + definition);
            return;
        }

        String type = parts[0].trim();
        try {
            BufferedImage[] frames = switch (type) {
                case "gif" -> loadGif(parts[1].trim());
                case "sequence" -> {
                    if (parts.length != 3) {
                        throw new IllegalArgumentException("sequence requires frame count and path prefix");
                    }
                    yield loadSequence(parts[2].trim(), Integer.parseInt(parts[1].trim()));
                }
                case "sheet" -> {
                    if (parts.length != 3) {
                        throw new IllegalArgumentException("sheet requires frame count and file path");
                    }
                    yield loadSpriteSheet(parts[2].trim(), Integer.parseInt(parts[1].trim()));
                }
                default -> {
                    System.err.println("Unknown animation type for " + key + ": " + type);
                    yield null;
                }
            };
            if (frames != null && frames.length > 0) {
                animations.put(key, frames);
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Cannot load animation " + key + " (" + definition + "): " + e.getMessage());
        }
    }

    private BufferedImage[] loadSequence(String pathPrefix, int frameCount) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("frame count must be positive");
        }
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = AssetLoader.loadImage(pathPrefix + "_" + i + ".png");
            if (frames[i] == null) {
                return null;
            }
        }
        return frames;
    }

    private BufferedImage[] loadSpriteSheet(String path, int frameCount) {
        if (frameCount <= 0) {
            throw new IllegalArgumentException("frame count must be positive");
        }
        BufferedImage sheet = AssetLoader.loadImage(path);
        if (sheet == null || sheet.getWidth() < frameCount) {
            return null;
        }
        int frameWidth = sheet.getWidth() / frameCount;
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, sheet.getHeight());
        }
        return frames;
    }

    private BufferedImage[] loadGif(String path) throws IOException {
        try (InputStream stream = openAsset(path);
             ImageInputStream imageStream = ImageIO.createImageInputStream(stream)) {
            if (imageStream == null) {
                throw new IOException("Unsupported image stream: " + path);
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
            if (!readers.hasNext()) {
                throw new IOException("No image reader found: " + path);
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageStream, false, false);
                int count = reader.getNumImages(true);
                ArrayList<BufferedImage> frames = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    frames.add(reader.read(i));
                }
                return frames.toArray(new BufferedImage[0]);
            } finally {
                reader.dispose();
            }
        }
    }

    private InputStream openAsset(String path) throws IOException {
        String resourcePath = path.startsWith("/") ? path : "/" + path;
        InputStream resource = AnimationCatalog.class.getResourceAsStream(resourcePath);
        if (resource != null) {
            return resource;
        }

        File file = new File("assets", path.startsWith("/") ? path.substring(1) : path);
        if (file.isFile()) {
            return new java.io.FileInputStream(file);
        }
        throw new IOException("Asset not found: " + path);
    }
}
