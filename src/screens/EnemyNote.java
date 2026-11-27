package screens;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

record EnemyNote(String title, String body, String imagePath) {
    private static final String FILE_NAME = "enemy_notes.properties";
    private static final Properties NOTES = loadNotes();

    static EnemyNote find(String id) {
        String title = NOTES.getProperty(id + ".title");
        String body = NOTES.getProperty(id + ".body");
        String imagePath = NOTES.getProperty(id + ".image", "").trim();
        return title == null || body == null ? null
                : new EnemyNote(title, body, imagePath.isEmpty() ? null : imagePath);
    }

    private static Properties loadNotes() {
        Properties notes = new Properties();
        InputStream resource = EnemyNote.class.getResourceAsStream("/" + FILE_NAME);
        try (InputStream input = resource != null ? resource
                : Files.newInputStream(Path.of("assets", FILE_NAME))) {
            notes.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Cannot read " + FILE_NAME + " - " + e.getMessage());
        }
        return notes;
    }
}
