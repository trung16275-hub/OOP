package map;

import model.prop.RoomProp;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Premade tactical room templates for Floor 1 ("The Lobby") with mirroring,
 * door-corridor protection, and non-blocking office clutter decals.
 * . — Open floor
 * # — Solid interior pillar / cubicle block (TileType.WALL or solid RoomProp depending on style)
 * D — Solid Office Desk (desk_wood_front.png, 64×36 solid cover)
 * C — Solid Filing Cabinet (cabinet_tall_4drawer.png / cabinet_short_2drawer.png)
 * H / V — Cubicle Partition (prop_cubicle_partition_h.png / _v.png)
 * B — Cardboard Box cluster (prop_cardboard_boxes_stacked.png)
 * P — Pickup anchor (candidate position for AmmoPickup / LiquidPain)
 * E — Enemy spawn anchor (ready for future enemy waves or enabled in Phase 1 if desired)
 * door-corridor protection, and non-blocking office clutter decals.
 */
public final class RoomTemplate {

    private static final String[][] SCREEN_TEMPLATES = {
            // 0: Four-Corner Cubicles — classic 4-quadrant cover with wide center crosslanes
            {
                    "........................",
                    "..HD................DH..",
                    "..V..................V..",
                    "........................",
                    "........................",
                    "..V..................V..",
                    "..HC................CH..",
                    "........................"
            },
            // 1: Staggered Office Lanes — horizontal cubicle rows forcing flank routes
            {
                    "........................",
                    "....HHH..........DDD....",
                    "........................",
                    ".........B....C.........",
                    ".........C....B.........",
                    "........................",
                    "....DDD..........HHH....",
                    "........................"
            },
            // 2: Central Workstation Island — loop around the center desk/printer hub
            {
                    "........................",
                    "..B..................B..",
                    "........HH..HH..........",
                    "........D....D..........",
                    "........C....P..........",
                    "........HH..HH..........",
                    "..C..................C..",
                    "........................"
            },
            // 3: Filing Maze — vertical partitions & filing cabinets for peek-shooting
            {
                    "........................",
                    "....V......C.......V....",
                    "....V......D.......V....",
                    "........................",
                    "........................",
                    "....V......D.......V....",
                    "....V......C.......V....",
                    "........................"
            },
            // 4: Break Lounge — perimeter vending/cooler + light staggered boxes
            {
                    ".W....................M.",
                    "........................",
                    "......B..........B......",
                    "........................",
                    "........................",
                    "......D..........D......",
                    "........................",
                    ".P....................B."
            }
    };

    private static final String[][] SMALL_TEMPLATES = {
            // 0: Corner Cover Pods
            {
                    ".C........C.",
                    "............",
                    "............",
                    ".B........B."
            },
            // 1: Center Desk Split
            {
                    "............",
                    "....D..B....",
                    "....C..D....",
                    "............"
            },
            // 2: Side Cubicles
            {
                    "..V......V..",
                    "..H......H..",
                    "............",
                    ".....P......"
            }
    };

    private static final String[] BOSS_1_ARENA = {
            "........................................",
            "..C..................................C..",
            "..B..................................B..",
            "........................................",
            ".......HH......................HH.......",
            ".......D........................D.......",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            "........................................",
            ".......D........................D.......",
            ".......HH......................HH.......",
            "........................................",
            "..B..................................B..",
            "..C..................................C..",
            "........................................"
    };

    private RoomTemplate() {}

    /**
     * Applies a premade Floor 1 layout and office clutter to the given room.
     * Returns candidate pickup points inside the room (guaranteed unobstructed).
     */
    public static List<Point> applyFloor1Layout(Room room, Random rand) {
        List<Point> pickupAnchors = new ArrayList<>();
        if (room == null) {
            return pickupAnchors;
        }

        Room.RoomType type = room.getRoomType();
        if (type == Room.RoomType.SPAWN) {
            addSpawnRoomProps(room, rand);
            addOfficeClutter(room, rand);
            int boardX = (room.getWidth() / 2 - 3) * Room.TILE_SIZE;
            int boardY = (room.getPadY() + 2) * Room.TILE_SIZE;
            room.addWallDecal(new RoomDecal("/floor1/props_addon/prop_whiteboard_notes.png",
                    boardX, boardY, 64, 44, 0, false, 1.0f, "boss1"));
            return pickupAnchors;
        }

        String[] pattern;
        boolean allowMirror = true;
        if (type == Room.RoomType.BOSS) {
            pattern = BOSS_1_ARENA;
            allowMirror = false;
        } else if (room.getRoomSize() == Room.RoomSize.SMALL) {
            pattern = SMALL_TEMPLATES[rand.nextInt(SMALL_TEMPLATES.length)];
        } else {
            pattern = SCREEN_TEMPLATES[rand.nextInt(SCREEN_TEMPLATES.length)];
        }

        boolean flipX = allowMirror && rand.nextBoolean();
        boolean flipY = allowMirror && rand.nextBoolean();

        int innerMinX = room.getPadX() + 4; // 2-tile walkable buffer inside inner walls
        int innerMaxX = room.getWidth() - room.getPadX() - 5;
        int innerMinY = room.getPadY() + 6;
        int innerMaxY = room.getHeight() - room.getPadY() - 5;

        int availW = Math.max(1, innerMaxX - innerMinX + 1);
        int availH = Math.max(1, innerMaxY - innerMinY + 1);
        int patH = pattern.length;
        int patW = pattern[0].length();

        for (int py = 0; py < patH; py++) {
            int srcY = flipY ? (patH - 1 - py) : py;
            String row = pattern[srcY];
            int tx = 0;
            for (int px = 0; px < patW; px++) {
                int srcX = flipX ? (patW - 1 - px) : px;
                char ch = row.charAt(srcX);
                if (ch == '.') {
                    continue;
                }

                // Map pattern cell cleanly into the available safe interior
                tx = innerMinX + (int) Math.round((px + 0.5) * availW / (double) patW - 0.5);
                int ty = innerMinY + (int) Math.round((py + 0.5) * availH / (double) patH - 0.5);
                tx = Math.max(innerMinX, Math.min(innerMaxX, tx));
                ty = Math.max(innerMinY, Math.min(innerMaxY, ty));

                if (room.isInDoorCorridor(tx, ty)) {
                    continue;
                }
                if (type == Room.RoomType.BOSS && isNearRoomCenter(room, tx, ty, 3)) {
                    continue;
                }

                if (ch == 'P') {
                    pickupAnchors.add(new Point(tx * Room.TILE_SIZE, ty * Room.TILE_SIZE));
                    continue;
                }

                RoomProp prop = createFloor1Prop(ch, tx, ty, rand);
                if (prop != null && canPlaceSolidProp(room, prop)) {
                    room.addProp(prop);
                }
            }
        }

        addOfficeClutter(room, rand);
        return pickupAnchors;
    }

    private static void addSpawnRoomProps(Room room, Random rand) {
        int floorMinX = room.getPadX() + 2;
        int floorMaxX = room.getWidth() - room.getPadX() - 3;
        int floorMinY = room.getPadY() + 4;

        // Place corner potted plant / water cooler safely clear of door corridors
        RoomProp plant = new RoomProp("/floor1/props_addon/prop_potted_dead_plant.png",
                (floorMinX + 2) * Room.TILE_SIZE,
                (floorMinY + 2) * Room.TILE_SIZE,
                28, 38, true);
        if (canPlaceSolidProp(room, plant)) {
            room.addProp(plant);
        }

        if (rand.nextBoolean()) {
            RoomProp cooler = new RoomProp("/floor1/props_addon/prop_water_cooler_intact.png",
                    (floorMaxX - 2) * Room.TILE_SIZE,
                    (floorMinY + 2) * Room.TILE_SIZE,
                    24, 48, true);
            if (canPlaceSolidProp(room, cooler)) {
                room.addProp(cooler);
            }
        }
    }

    private static RoomProp createFloor1Prop(char token, int tx, int ty, Random rand) {
        int px = tx * Room.TILE_SIZE;
        int py = ty * Room.TILE_SIZE;
        return switch (token) {
            case 'D' -> new RoomProp("/floor1/props/desk_wood_front.png", px - 10, py - 4, 86, 60, true);
            case 'C' -> rand.nextBoolean()
                    ? new RoomProp("/floor1/props/cabinet_tall_4drawer.png", px + 2, py - 18, 36, 62, true)
                    : new RoomProp("/floor1/props/cabinet_short_2drawer.png", px + 1, py - 4, 28, 36, true);
            case 'H' -> new RoomProp("/floor1/props_addon/prop_cubicle_partition_h.png", px - 10, py - 4, 96, 60, true);
            case 'V' -> new RoomProp("/floor1/props_addon/prop_cubicle_partition_v.png", px + 2, py - 18, 48, 80, true);
            case 'B' -> new RoomProp("/floor1/props_addon/prop_cardboard_boxes_stacked.png", px - 4, py - 4, 48, 44, true);
            case 'W' -> new RoomProp("/floor1/props_addon/prop_water_cooler_intact.png", px + 4, py - 16, 36, 60, true);
            case 'M' -> new RoomProp("/floor1/props_addon/prop_vending_machine_on.png", px - 3, py - 20, 60, 80, true);
            default -> null;
        };
    }

    private static boolean canPlaceSolidProp(Room room, RoomProp candidate) {
        Rectangle bounds = candidate.getBounds();
        if (room.checkCollision(bounds)) {
            return false;
        }
        // Keep a safety margin around every carved door opening
        int minTx = Math.max(0, bounds.x / Room.TILE_SIZE);
        int maxTx = Math.min(room.getWidth() - 1, (bounds.x + bounds.width - 1) / Room.TILE_SIZE);
        int minTy = Math.max(0, bounds.y / Room.TILE_SIZE);
        int maxTy = Math.min(room.getHeight() - 1, (bounds.y + bounds.height - 1) / Room.TILE_SIZE);
        for (int tx = minTx; tx <= maxTx; tx++) {
            for (int ty = minTy; ty <= maxTy; ty++) {
                if (room.isInDoorCorridor(tx, ty)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isNearRoomCenter(Room room, int tx, int ty, int radiusTiles) {
        int midX = room.getWidth() / 2;
        int midY = (room.getPadY() + 4 + (room.getHeight() - room.getPadY() - 3)) / 2;
        return Math.abs(tx - midX) <= radiusTiles && Math.abs(ty - midY) <= radiusTiles;
    }

    private static void addOfficeClutter(Room room, Random rand) {
        int floorMinX = room.getPadX() + 2;
        int floorMaxX = room.getWidth() - room.getPadX() - 3;
        int floorMinY = room.getPadY() + 4;
        int floorMaxY = room.getHeight() - room.getPadY() - 3;
        int ts = Room.TILE_SIZE;

        // 1. Floor stains, damp carpet, vents, scattered documents (non-blocking)
        String[] floorSprites = {
                "/floor1/props/clutter_scattered_documents.png",
                "/floor1/props_addon/prop_coffee_mug_spill.png",
                "/floor1/props_addon/prop_wet_floor_sign.png",
                "/floor1/tiles_addon/tile_floor_damp_carpet.png",
                "/floor1/tiles_addon/tile_floor_scuff_marks.png",
                "/floor1/tiles_addon/tile_floor_air_vent_clean.png"
        };

        int decalCount = room.getRoomSize() == Room.RoomSize.BIG ? 8 : 5;
        for (int i = 0; i < decalCount; i++) {
            int tx = floorMinX + rand.nextInt(Math.max(1, floorMaxX - floorMinX + 1));
            int ty = floorMinY + rand.nextInt(Math.max(1, floorMaxY - floorMinY + 1));
            if (room.isTileSolid(tx, ty)) {
                continue;
            }
            String sprite = floorSprites[rand.nextInt(floorSprites.length)];
            int w = sprite.contains("documents") ? 36 : (sprite.contains("wet_floor") ? 24 : 32);
            int h = sprite.contains("documents") ? 24 : (sprite.contains("wet_floor") ? 32 : 32);
            float alpha = sprite.contains("tiles_addon") ? 0.65f : 0.90f;
            room.addFloorDecal(new RoomDecal(sprite, tx * ts, ty * ts, w, h, 0, rand.nextBoolean(), alpha));
        }

        // 2. North-wall accents (whiteboard, clock, exit sign, peeling wallpaper) away from doorways
        int northWallMidY = (room.getPadY() + 2) * ts;
        int northWallBotY = (room.getPadY() + 3) * ts;
        String[] wallAccents = {
                "/floor1/props_addon/prop_whiteboard_notes.png",
                "/floor1/props_addon/prop_wall_clock_frozen.png",
                "/floor1/props_addon/prop_exit_sign_green.png",
                "/floor1/tiles_addon/wall_wallpaper_water_stain.png",
                "/floor1/tiles_addon/wall_wallpaper_torn_a.png"
        };

        int accentCount = room.getRoomSize() == Room.RoomSize.BIG ? 4 : 2;
        for (int i = 0; i < accentCount; i++) {
            int tx = floorMinX + 1 + rand.nextInt(Math.max(1, floorMaxX - floorMinX - 1));
            if (room.getTile(tx, room.getPadY() + 2) != TileType.WALL) {
                continue; // skip north door cutout
            }
            String sprite = wallAccents[rand.nextInt(wallAccents.length)];
            if (sprite.contains("whiteboard")) {
                room.addWallDecal(new RoomDecal(sprite, tx * ts, northWallMidY + 2, 42, 30));
            } else if (sprite.contains("clock")) {
                room.addWallDecal(new RoomDecal(sprite, tx * ts + 4, northWallMidY - 4, 24, 24));
            } else if (sprite.contains("exit_sign")) {
                room.addWallDecal(new RoomDecal(sprite, tx * ts + 2, (room.getPadY() + 1) * ts + 6, 28, 16));
            } else {
                room.addWallDecal(new RoomDecal(sprite, tx * ts, northWallBotY, ts, ts, 0, false, 0.75f));
            }
        }
    }
}
