package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

/**
 * Desenha pisos, paredes, equipamentos e detalhes internos do laboratório.
 */
final class LaboratoryTilePainter {

    private static final Color FLOOR_SEAM = Color.web("#07131d");
    private static final Color FLOOR_A = Color.web("#102431");
    private static final Color FLOOR_B = Color.web("#132a39");
    private static final Color FLOOR_PANEL = Color.web("#1b3a49", 0.72);
    private static final Color FLOOR_DETAIL = Color.web("#2b6070", 0.52);

    private static final Color WALL_SHADOW = Color.web("#101b27");
    private static final Color WALL_BODY = Color.web("#344c5e");
    private static final Color WALL_TOP = Color.web("#526b7d");
    private static final Color WALL_HIGHLIGHT = Color.web("#7890a1");
    private static final Color WALL_BASE = Color.web("#1b2a38");
    private static final Color WALL_BOLT = Color.web("#91a7b5");
    private static final Color WALL_EDGE_LIGHT = Color.web("#8bddec", 0.34);
    private static final Color WALL_EDGE_SHADOW = Color.web("#050b12", 0.68);
    private static final Color WALL_LAMP_MOUNT = Color.web("#07151e", 0.92);

    private static final Color EQUIPMENT_SHADOW = Color.web("#160d14");
    private static final Color EQUIPMENT_BODY = Color.web("#5d3040");
    private static final Color EQUIPMENT_PANEL = Color.web("#85485a");
    private static final Color EQUIPMENT_EDGE = Color.web("#b46770");
    private static final Color SCREEN_DARK = Color.web("#061820");
    private static final Color SCREEN_LIGHT = Color.web("#39d9c5");
    private static final Color EQUIPMENT_AMBER = Color.web("#f29e4c");
    private static final Color EXIT_DARK = Color.web("#052218");
    private static final Color EXIT_FRAME = Color.web("#20a860");
    private static final Color EXIT_LIGHT = Color.web("#69f59e");

    private static final Paint CYAN_LIGHT = new RadialGradient(
            0.0,
            0.0,
            0.5,
            0.5,
            0.5,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#15d4dd", 0.17)),
            new Stop(1.0, Color.TRANSPARENT)
    );
    private static final Paint MAGENTA_LIGHT = new RadialGradient(
            0.0,
            0.0,
            0.5,
            0.5,
            0.5,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#d94d88", 0.11)),
            new Stop(1.0, Color.TRANSPARENT)
    );
    private static final Paint BLUE_LIGHT = new RadialGradient(
            0.0,
            0.0,
            0.5,
            0.5,
            0.5,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#2688d9", 0.12)),
            new Stop(1.0, Color.TRANSPARENT)
    );
    private static final Paint GREEN_LIGHT = new RadialGradient(
            0.0,
            0.0,
            0.5,
            0.5,
            0.5,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#34c783", 0.10)),
            new Stop(1.0, Color.TRANSPARENT)
    );
    private static final Paint AMBER_LIGHT = new RadialGradient(
            0.0,
            0.0,
            0.5,
            0.5,
            0.5,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#e2993e", 0.10)),
            new Stop(1.0, Color.TRANSPARENT)
    );
    private final GraphicsContext graphics;
    private final LaboratoryTheme theme;

    LaboratoryTilePainter(GraphicsContext graphics, LaboratoryTheme theme) {
        this.graphics = graphics;
        this.theme = theme;
    }

    void draw(TileMap tileMap, double mapX, double mapY) {
        double tileSize = tileMap.tileSize();
        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                double x = mapX + column * tileSize;
                double y = mapY + row * tileSize;
                if (tileMap.tileAt(row, column) == TileType.WALL) {
                    drawWall(tileMap, row, column, x, y, tileSize);
                } else {
                    drawFloor(row, column, x, y, tileSize);
                }
            }
        }

        drawLaboratoryLighting(mapX, mapY, tileMap.pixelWidth(), tileMap.pixelHeight());
        drawSurfaceDetails(tileMap, mapX, mapY);

        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                if (tileMap.tileAt(row, column) == TileType.EQUIPMENT) {
                    drawEquipment(
                            row,
                            column,
                            mapX + column * tileSize,
                            mapY + row * tileSize,
                            tileSize
                    );
                } else if (tileMap.tileAt(row, column) == TileType.EXIT) {
                    drawExit(
                            mapX + column * tileSize,
                            mapY + row * tileSize,
                            tileSize
                    );
                }
            }
        }

    }

    private void drawFloor(int row, int column, double x, double y, double size) {
        Image floor = tileVariant(row, column) == 3 && VisualAssets.FLOOR_ALT != null
                ? VisualAssets.FLOOR_ALT
                : VisualAssets.FLOOR;
        if (floor != null) {
            drawTileImage(floor, row, column, x, y, size);
            drawFloorAccent(row, column, x, y, size);
            return;
        }

        graphics.setFill(FLOOR_SEAM);
        graphics.fillRect(x, y, size, size);

        graphics.setFill((row + column) % 2 == 0 ? FLOOR_A : FLOOR_B);
        graphics.fillRect(x + 1.0, y + 1.0, size - 2.0, size - 2.0);

        graphics.setFill(FLOOR_PANEL);
        graphics.fillRect(x + 4.0, y + 4.0, size - 8.0, 1.0);
        graphics.fillRect(x + 4.0, y + 4.0, 1.0, size - 8.0);

        graphics.setFill(FLOOR_DETAIL);
        graphics.fillRect(x + 5.0, y + 5.0, 2.0, 2.0);
        graphics.fillRect(x + size - 7.0, y + size - 7.0, 2.0, 2.0);

        drawFloorAccent(row, column, x, y, size);
    }

    private void drawFloorAccent(int row, int column, double x, double y, double size) {
        if (detailVariant(row, column) % 3 != 0) {
            return;
        }

        double unit = size / 40.0;
        int variant = tileVariant(row, column);
        if (variant == 1) {
            graphics.setFill(FLOOR_DETAIL);
            graphics.fillRect(x + 12.0 * unit, y + 30.0 * unit, 10.0 * unit, unit);
        } else if (variant == 2) {
            graphics.setFill(FLOOR_DETAIL);
            graphics.fillRect(x + 30.0 * unit, y + 12.0 * unit, unit, 9.0 * unit);
        } else if (variant == 3) {
            graphics.setFill(sectorAccent(row, column));
            graphics.fillRect(x + 18.0 * unit, y + 18.0 * unit, 4.0 * unit, 2.0 * unit);
        } else {
            graphics.setFill(FLOOR_DETAIL);
            graphics.fillRect(x + 7.0 * unit, y + 7.0 * unit, 2.0 * unit, 2.0 * unit);
            graphics.fillRect(x + 31.0 * unit, y + 31.0 * unit, 2.0 * unit, 2.0 * unit);
        }
    }

    private void drawWall(
            TileMap tileMap,
            int row,
            int column,
            double x,
            double y,
            double size
    ) {
        Image wall = tileVariant(row, column) == 2 && VisualAssets.WALL_ALT != null
                ? VisualAssets.WALL_ALT
                : VisualAssets.WALL;
        if (wall != null) {
            drawTileImage(wall, row, column, x, y, size);
            drawExposedWallEdge(tileMap, row, column, x, y, size);
            drawWallStatusLight(tileMap, row, column, x, y, size);
            return;
        }

        graphics.setFill(WALL_SHADOW);
        graphics.fillRect(x, y, size, size);

        graphics.setFill(WALL_BODY);
        graphics.fillRect(x + 2.0, y + 2.0, size - 4.0, size - 6.0);
        graphics.setFill(WALL_TOP);
        graphics.fillRect(x + 3.0, y + 3.0, size - 6.0, 9.0);
        graphics.setFill(WALL_HIGHLIGHT);
        graphics.fillRect(x + 5.0, y + 5.0, size - 10.0, 2.0);
        graphics.setFill(WALL_BASE);
        graphics.fillRect(x + 2.0, y + size - 8.0, size - 4.0, 6.0);

        graphics.setFill(WALL_BOLT);
        graphics.fillRect(x + 5.0, y + 16.0, 2.0, 2.0);
        graphics.fillRect(x + size - 7.0, y + 16.0, 2.0, 2.0);

        drawExposedWallEdge(tileMap, row, column, x, y, size);
        drawWallStatusLight(tileMap, row, column, x, y, size);
    }

    private void drawEquipment(int row, int column, double x, double y, double size) {
        Image equipment = equipmentFor(tileVariant(row, column));
        if (equipment != null) {
            drawEquipmentMount(x, y, size);
            graphics.drawImage(equipment, x, y, size, size);
            drawEquipmentIndicator(row, column, x, y, size);
            return;
        }

        graphics.setFill(EQUIPMENT_SHADOW);
        graphics.fillRect(x + 6.0, y + 6.0, size - 8.0, size - 7.0);
        graphics.setFill(EQUIPMENT_BODY);
        graphics.fillRect(x + 4.0, y + 3.0, size - 8.0, size - 8.0);
        graphics.setFill(EQUIPMENT_PANEL);
        graphics.fillRect(x + 6.0, y + 5.0, size - 12.0, size - 12.0);
        graphics.setFill(EQUIPMENT_EDGE);
        graphics.fillRect(x + 7.0, y + 6.0, size - 14.0, 2.0);

        if (tileVariant(row, column) % 2 == 0) {
            drawTerminal(x, y, size);
        } else {
            drawServerRack(x, y, size);
        }
        drawEquipmentIndicator(row, column, x, y, size);
    }

    private void drawExit(double x, double y, double size) {
        double unit = size / 40.0;
        graphics.setFill(EXIT_DARK);
        graphics.fillRect(x + 3.0 * unit, y + 3.0 * unit, size - 6.0 * unit, size - 6.0 * unit);
        graphics.setFill(EXIT_FRAME);
        graphics.fillRect(x + 6.0 * unit, y + 5.0 * unit, size - 12.0 * unit, 4.0 * unit);
        graphics.fillRect(x + 6.0 * unit, y + size - 9.0 * unit, size - 12.0 * unit, 4.0 * unit);
        graphics.fillRect(x + 6.0 * unit, y + 5.0 * unit, 4.0 * unit, size - 10.0 * unit);
        graphics.fillRect(x + size - 10.0 * unit, y + 5.0 * unit, 4.0 * unit, size - 10.0 * unit);
        graphics.setFill(EXIT_LIGHT);
        graphics.fillRect(x + 12.0 * unit, y + 18.0 * unit, 13.0 * unit, 4.0 * unit);
        graphics.fillRect(x + 21.0 * unit, y + 14.0 * unit, 4.0 * unit, 12.0 * unit);
        graphics.fillRect(x + 25.0 * unit, y + 17.0 * unit, 4.0 * unit, 6.0 * unit);
    }

    private void drawLaboratoryLighting(
            double mapX,
            double mapY,
            double mapWidth,
            double mapHeight
    ) {
        graphics.setFill(CYAN_LIGHT);
        graphics.fillOval(
                mapX + mapWidth * 0.025,
                mapY + mapHeight * 0.045,
                mapWidth * 0.29,
                mapHeight * 0.32
        );

        graphics.setFill(BLUE_LIGHT);
        graphics.fillOval(
                mapX + mapWidth * 0.35,
                mapY + mapHeight * 0.035,
                mapWidth * 0.30,
                mapHeight * 0.34
        );

        graphics.setFill(MAGENTA_LIGHT);
        graphics.fillOval(
                mapX + mapWidth * 0.69,
                mapY + mapHeight * 0.035,
                mapWidth * 0.28,
                mapHeight * 0.34
        );

        graphics.setFill(GREEN_LIGHT);
        graphics.fillOval(
                mapX + mapWidth * 0.20,
                mapY + mapHeight * 0.63,
                mapWidth * 0.35,
                mapHeight * 0.32
        );

        graphics.setFill(AMBER_LIGHT);
        graphics.fillOval(
                mapX + mapWidth * 0.68,
                mapY + mapHeight * 0.62,
                mapWidth * 0.29,
                mapHeight * 0.32
        );
    }

    private void drawSurfaceDetails(TileMap tileMap, double mapX, double mapY) {
        double size = tileMap.tileSize();
        double unit = size / 40.0;
        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                if (tileMap.tileAt(row, column) != TileType.LAB_FLOOR) {
                    continue;
                }

                int detail = detailVariant(row, column);
                double x = mapX + column * size;
                double y = mapY + row * size;
                graphics.setFill(sectorAccent(row, column));

                if (detail == 0) {
                    graphics.fillRect(x + 5.0 * unit, y + 5.0 * unit, 8.0 * unit, unit);
                    graphics.fillRect(x + 5.0 * unit, y + 5.0 * unit, unit, 5.0 * unit);
                } else if (detail == 7) {
                    graphics.fillRect(x + 27.0 * unit, y + 34.0 * unit, 7.0 * unit, unit);
                    graphics.fillRect(x + 33.0 * unit, y + 30.0 * unit, unit, 5.0 * unit);
                } else if (detail == 13) {
                    graphics.fillRect(x + 8.0 * unit, y + 20.0 * unit, 3.0 * unit, unit);
                    graphics.fillRect(x + 14.0 * unit, y + 20.0 * unit, 3.0 * unit, unit);
                    graphics.fillRect(x + 20.0 * unit, y + 20.0 * unit, 3.0 * unit, unit);
                }
            }
        }
    }

    private void drawEquipmentMount(double x, double y, double size) {
        graphics.setFill(EQUIPMENT_SHADOW);
        graphics.fillRoundRect(x + 3.0, y + 5.0, size - 6.0, size - 6.0, 6.0, 6.0);
        graphics.setFill(EQUIPMENT_EDGE);
        graphics.fillRect(x + 4.0, y + 4.0, 7.0, 2.0);
        graphics.fillRect(x + size - 11.0, y + 4.0, 7.0, 2.0);
        graphics.fillRect(x + 4.0, y + size - 5.0, 7.0, 2.0);
        graphics.fillRect(x + size - 11.0, y + size - 5.0, 7.0, 2.0);
    }

    private void drawTileImage(
            Image image,
            int row,
            int column,
            double x,
            double y,
            double size
    ) {
        int variant = tileVariant(row, column);
        boolean mirrorHorizontally = (variant & 1) != 0;
        boolean mirrorVertically = (variant & 2) != 0;

        graphics.save();
        graphics.translate(
                x + (mirrorHorizontally ? size : 0.0),
                y + (mirrorVertically ? size : 0.0)
        );
        graphics.scale(
                mirrorHorizontally ? -1.0 : 1.0,
                mirrorVertically ? -1.0 : 1.0
        );
        graphics.drawImage(image, 0.0, 0.0, size, size);
        graphics.restore();
    }

    private void drawExposedWallEdge(
            TileMap tileMap,
            int row,
            int column,
            double x,
            double y,
            double size
    ) {
        double unit = size / 40.0;
        if (!isWall(tileMap, row + 1, column)) {
            graphics.setFill(WALL_EDGE_SHADOW);
            graphics.fillRect(x + 2.0 * unit, y + size - 4.0 * unit, size - 4.0 * unit, 4.0 * unit);
            graphics.setFill(WALL_EDGE_LIGHT);
            graphics.fillRect(x + 4.0 * unit, y + size - 4.0 * unit, size - 8.0 * unit, 1.0 * unit);
        }
        if (!isWall(tileMap, row - 1, column)) {
            graphics.setFill(WALL_EDGE_LIGHT);
            graphics.fillRect(x + 4.0 * unit, y + 1.0 * unit, size - 8.0 * unit, 1.0 * unit);
        }
        if (!isWall(tileMap, row, column - 1)) {
            graphics.setFill(WALL_EDGE_SHADOW);
            graphics.fillRect(x, y + 3.0 * unit, 2.0 * unit, size - 6.0 * unit);
        }
        if (!isWall(tileMap, row, column + 1)) {
            graphics.setFill(WALL_EDGE_LIGHT);
            graphics.fillRect(x + size - 2.0 * unit, y + 4.0 * unit, 1.0 * unit, size - 8.0 * unit);
        }
    }

    private void drawWallStatusLight(
            TileMap tileMap,
            int row,
            int column,
            double x,
            double y,
            double size
    ) {
        if (detailVariant(row, column) % 7 != 0 || !hasOpenNeighbor(tileMap, row, column)) {
            return;
        }

        double unit = size / 40.0;
        Color light = sectorAccent(row, column);
        graphics.setFill(WALL_LAMP_MOUNT);
        graphics.fillRoundRect(
                x + 14.0 * unit,
                y + size - 7.0 * unit,
                12.0 * unit,
                5.0 * unit,
                2.0 * unit,
                2.0 * unit
        );
        graphics.setFill(light);
        graphics.fillRect(
                x + 17.0 * unit,
                y + size - 6.0 * unit,
                6.0 * unit,
                2.0 * unit
        );
    }

    private void drawEquipmentIndicator(
            int row,
            int column,
            double x,
            double y,
            double size
    ) {
        double unit = size / 40.0;
        Color light = sectorAccent(row, column);
        graphics.setFill(SCREEN_DARK);
        graphics.fillRoundRect(
                x + size - 12.0 * unit,
                y + 6.0 * unit,
                7.0 * unit,
                5.0 * unit,
                2.0 * unit,
                2.0 * unit
        );
        graphics.setFill(light);
        graphics.fillRect(
                x + size - 10.0 * unit,
                y + 8.0 * unit,
                3.0 * unit,
                1.0 * unit
        );

        if (detailVariant(row, column) % 2 == 0) {
            graphics.fillRect(x + 7.0 * unit, y + size - 7.0 * unit, 5.0 * unit, 1.0 * unit);
            graphics.fillRect(x + 14.0 * unit, y + size - 7.0 * unit, 3.0 * unit, 1.0 * unit);
        }
    }

    private void drawTerminal(double x, double y, double size) {
        graphics.setFill(SCREEN_DARK);
        graphics.fillRect(x + 9.0, y + 10.0, size - 18.0, 11.0);
        graphics.setFill(SCREEN_LIGHT);
        graphics.fillRect(x + 12.0, y + 13.0, size - 24.0, 2.0);
        graphics.fillRect(x + 12.0, y + 17.0, 7.0, 1.0);
        graphics.setFill(EQUIPMENT_AMBER);
        graphics.fillRect(x + size - 14.0, y + 17.0, 3.0, 3.0);
        graphics.setFill(EQUIPMENT_SHADOW);
        graphics.fillRect(x + 10.0, y + 25.0, size - 20.0, 4.0);
    }

    private void drawServerRack(double x, double y, double size) {
        graphics.setFill(SCREEN_DARK);
        for (int slot = 0; slot < 3; slot++) {
            double slotY = y + 10.0 + slot * 7.0;
            graphics.fillRect(x + 9.0, slotY, size - 18.0, 5.0);
            graphics.setFill(slot == 1 ? EQUIPMENT_AMBER : SCREEN_LIGHT);
            graphics.fillRect(x + size - 14.0, slotY + 1.0, 2.0, 2.0);
            graphics.setFill(SCREEN_DARK);
        }
    }

    private static boolean isWall(TileMap tileMap, int row, int column) {
        return row >= 0
                && row < tileMap.rows()
                && column >= 0
                && column < tileMap.columns()
                && tileMap.tileAt(row, column) == TileType.WALL;
    }

    private static boolean hasOpenNeighbor(TileMap tileMap, int row, int column) {
        return isOpenTile(tileMap, row - 1, column)
                || isOpenTile(tileMap, row + 1, column)
                || isOpenTile(tileMap, row, column - 1)
                || isOpenTile(tileMap, row, column + 1);
    }

    private static boolean isOpenTile(TileMap tileMap, int row, int column) {
        return row >= 0
                && row < tileMap.rows()
                && column >= 0
                && column < tileMap.columns()
                && tileMap.tileAt(row, column) != TileType.WALL;
    }

    private Image equipmentFor(int variant) {
        return switch (theme) {
            case ESCAPE_ROUTE -> switch (variant) {
                case 0, 3 -> VisualAssets.TERMINAL;
                case 1 -> VisualAssets.BIO_POD;
                default -> VisualAssets.CONSOLE;
            };
            case DATA_CORE -> variant % 2 == 0
                    ? VisualAssets.TERMINAL
                    : VisualAssets.CONSOLE;
            case CONTAINMENT -> variant % 2 == 0
                    ? VisualAssets.REACTOR
                    : VisualAssets.BIO_POD;
        };
    }

    private Color sectorAccent(int row, int column) {
        boolean secondaryArea = (row <= 4 && column >= 14)
                || (row >= 10 && column >= 11);
        return secondaryArea
                ? theme.equipmentSignal(0.40)
                : theme.accent(0.35);
    }

    private static int detailVariant(int row, int column) {
        return Math.floorMod(row * 97 + column * 53, 29);
    }

    private static int tileVariant(int row, int column) {
        return Math.floorMod(row * 31 + column * 17, 4);
    }
}
