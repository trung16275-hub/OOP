package screens;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import javax.imageio.ImageIO;

public class AssetLoader {
    private static final Map<String, BufferedImage[]> SPRITE_CACHE = new HashMap<>();
    private static boolean boss4Loaded = false;

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

        // Tự động cắt và đưa bộ sprite Boss 4 vào SPRITE_CACHE ở lần gọi đầu tiên
        if (path.startsWith("boss4_") && !boss4Loaded) {
            loadBoss4Sprites("/boss4/duller.png");
        }

        // Nếu đã load rồi thì trả về ngay từ cache
        if (SPRITE_CACHE.containsKey(path)) {
            return SPRITE_CACHE.get(path);
        }

        // Chưa có thì đọc file, cắt frame ngang rồi lưu vào cache
        BufferedImage sheet = loadImage(path);
        if (sheet == null) return null;

        int frameWidth = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }

        SPRITE_CACHE.put(path, frames);
        return frames;
    }

    public static void loadBoss4Sprites(String sheetPath) {
        boss4Loaded = true;
        BufferedImage raw = loadImage(sheetPath);
        if (raw == null) return;

        BufferedImage clean = stripOuterBackground(raw, 15);
        int w = clean.getWidth();
        int h = clean.getHeight();

        // Tách 5 hàng sprite theo vùng pixel không trong suốt
        List<int[]> rows = findSegments(h, y -> {
            for (int x = 0; x < w; x++) {
                if ((clean.getRGB(x, y) >>> 24) > 0) return true;
            }
            return false;
        }, 20);

        if (rows.size() >= 5) {
            SPRITE_CACHE.put("boss4_idle", extractRowFrames(clean, rows.get(0), 6, 12, false, 0.45));
            SPRITE_CACHE.put("boss4_run", extractRowFrames(clean, rows.get(2), 6, 12, false, 0.45));
            SPRITE_CACHE.put("boss4_attack", extractRowFrames(clean, rows.get(3), 6, 10, true, 0.45));
            // Hàng 5 (index 4): 9 frame gục chết (hạ minHeightRatio xuống 0.18 để lấy cả dáng nằm sát đất)
            SPRITE_CACHE.put("boss4_death", extractRowFrames(clean, rows.get(4), 0, 9, false, 0.18));
        }
    }

    private static BufferedImage[] extractRowFrames(BufferedImage clean, int[] rowY,
                                                    int startCol, int endCol,
                                                    boolean flipHorizontal, double minHeightRatio) {
        int y1 = rowY[0], y2 = rowY[1];
        int w = clean.getWidth();
        int rowH = y2 - y1;

        boolean[][] visited = new boolean[w][rowH];
        List<int[]> mainCharacters = new ArrayList<>(); // {minX, maxX, minY, maxY}
        List<int[]> smallParticles = new ArrayList<>(); // vệt máu / hiệu ứng tách rời

        for (int x = 0; x < w; x++) {
            for (int ry = 0; ry < rowH; ry++) {
                if (visited[x][ry] || (clean.getRGB(x, y1 + ry) >>> 24) == 0) continue;

                int minX = x, maxX = x, minY = ry, maxY = ry;
                ArrayDeque<int[]> queue = new ArrayDeque<>();
                queue.add(new int[]{x, ry});
                visited[x][ry] = true;

                while (!queue.isEmpty()) {
                    int[] p = queue.poll();
                    int px = p[0], py = p[1];
                    if (px < minX) minX = px;
                    if (px > maxX) maxX = px;
                    if (py < minY) minY = py;
                    if (py > maxY) maxY = py;

                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            int nx = px + dx, ny = py + dy;
                            if (nx >= 0 && nx < w && ny >= 0 && ny < rowH
                                    && !visited[nx][ny]
                                    && (clean.getRGB(nx, y1 + ny) >>> 24) > 0) {
                                visited[nx][ny] = true;
                                queue.add(new int[]{nx, ny});
                            }
                        }
                    }
                }

                int blobH = maxY - minY + 1;
                // maxY >= rowH * 0.65 giúp bỏ qua icon ngôi sao lơ lửng phía trên góc phải hàng 5
                if (blobH >= rowH * minHeightRatio && maxY >= rowH * 0.65) {
                    mainCharacters.add(new int[]{minX, maxX, minY, maxY});
                } else if ((maxX - minX + 1) * blobH >= 6) {
                    smallParticles.add(new int[]{minX, maxX, minY, maxY});
                }
            }
        }

        // Gắn các vệt máu/chuyển động vào nhân vật gần nhất (bỏ qua icon lơ lửng quá cao phía trên)
        for (int[] part : smallParticles) {
            int partCenterX = (part[0] + part[1]) / 2;
            int[] bestChar = null;
            int bestDist = 25;
            for (int[] ch : mainCharacters) {
                if (part[3] < ch[2] - 8) continue;
                int dist = partCenterX < ch[0] ? (ch[0] - partCenterX)
                         : partCenterX > ch[1] ? (partCenterX - ch[1]) : 0;
                if (dist < bestDist) {
                    bestDist = dist;
                    bestChar = ch;
                }
            }
            if (bestChar != null) {
                bestChar[0] = Math.min(bestChar[0], part[0]);
                bestChar[1] = Math.max(bestChar[1], part[1]);
                bestChar[2] = Math.min(bestChar[2], part[2]);
                bestChar[3] = Math.max(bestChar[3], part[3]);
            }
        }

        mainCharacters.sort((a, b) -> Integer.compare((a[0] + a[1]) / 2, (b[0] + b[1]) / 2));

        int maxW = 0;
        for (int i = startCol; i < endCol && i < mainCharacters.size(); i++) {
            int[] box = mainCharacters.get(i);
            maxW = Math.max(maxW, box[1] - box[0] + 1);
        }

        List<BufferedImage> frames = new ArrayList<>();
        for (int i = startCol; i < endCol && i < mainCharacters.size(); i++) {
            int[] box = mainCharacters.get(i);
            int bx = box[0], bw = box[1] - box[0] + 1;
            BufferedImage cell = new BufferedImage(maxW, rowH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D cg = cell.createGraphics();
            int drawX = (maxW - bw) / 2;
            BufferedImage sub = clean.getSubimage(bx, y1, bw, rowH);

            if (flipHorizontal) {
                cg.drawImage(sub, drawX + bw, 0, -bw, rowH, null);
            } else {
                cg.drawImage(sub, drawX, 0, null);
            }
            cg.dispose();
            frames.add(cell);
        }
        return frames.toArray(new BufferedImage[0]);
    }
    private static BufferedImage stripOuterBackground(BufferedImage src, int tol) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int bg = src.getRGB(0, 0);
        int bgR = (bg >> 16) & 0xFF, bgG = (bg >> 8) & 0xFF, bgB = bg & 0xFF;

        boolean[] visited = new boolean[w * h];
        ArrayDeque<Integer> q = new ArrayDeque<>();

        for (int x = 0; x < w; x++) {
            for (int y : new int[]{0, h - 1}) {
                int idx = y * w + x;
                if (!visited[idx] && isBgColor(src.getRGB(x, y), bgR, bgG, bgB, tol)) {
                    visited[idx] = true;
                    q.add(idx);
                }
            }
        }
        for (int y = 0; y < h; y++) {
            for (int x : new int[]{0, w - 1}) {
                int idx = y * w + x;
                if (!visited[idx] && isBgColor(src.getRGB(x, y), bgR, bgG, bgB, tol)) {
                    visited[idx] = true;
                    q.add(idx);
                }
            }
        }

        while (!q.isEmpty()) {
            int idx = q.poll();
            int x = idx % w, y = idx / w;
            int[] nx = {x - 1, x + 1, x, x};
            int[] ny = {y, y, y - 1, y + 1};
            for (int d = 0; d < 4; d++) {
                if (nx[d] >= 0 && nx[d] < w && ny[d] >= 0 && ny[d] < h) {
                    int nIdx = ny[d] * w + nx[d];
                    if (!visited[nIdx] && isBgColor(src.getRGB(nx[d], ny[d]), bgR, bgG, bgB, tol)) {
                        visited[nIdx] = true;
                        q.add(nIdx);
                    }
                }
            }
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int idx = y * w + x;
                dst.setRGB(x, y, visited[idx] ? 0x00000000 : (0xFF000000 | (src.getRGB(x, y) & 0x00FFFFFF)));
            }
        }
        return dst;
    }

    private static boolean isBgColor(int rgb, int bgR, int bgG, int bgB, int tol) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        return Math.abs(r - bgR) <= tol && Math.abs(g - bgG) <= tol && Math.abs(b - bgB) <= tol;
    }

    private static List<int[]> findSegments(int limit, IntPredicate hasPixel, int minSize) {
        List<int[]> segs = new ArrayList<>();
        int start = -1;
        for (int i = 0; i < limit; i++) {
            boolean active = hasPixel.test(i);
            if (active && start == -1) start = i;
            else if (!active && start != -1) {
                if (i - start >= minSize) segs.add(new int[]{start, i});
                start = -1;
            }
        }
        if (start != -1 && limit - start >= minSize) segs.add(new int[]{start, limit});
        return segs;
    }
}