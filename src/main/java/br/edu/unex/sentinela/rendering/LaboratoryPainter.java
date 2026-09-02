package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Desenha o laboratório e seus tiles lógicos.
 */
final class LaboratoryPainter {

    private static final Color BACKGROUND_GRID = Color.web("#163145", 0.28);
    private static final Color MAP_SHADOW = Color.web("#000711", 0.74);
    private static final Color MAP_FRAME = Color.web("#102638");
    private static final Color MAP_BORDER = Color.web("#2c6a83");
    private static final Color MAP_CORNER = Color.web("#45d7f2");

    private static final Color FLOOR_SEAM = Color.web("#07131d");
    private static final Color FLOOR_A = Color.web("#102431");
    private static final Color FLOOR_B = Color.web("#132a39");
    private static final Color FLOOR_PANEL = Color.web("#1b3a49", 0.72);
    private static final Color FLOOR_DETAIL = Color.web("#2b6070", 0.52);
    private static final Color FLOOR_LIGHT = Color.web("#39aab5", 0.35);

    private static final Color WALL_SHADOW = Color.web("#101b27");
    private static final Color WALL_BODY = Color.web("#344c5e");
    private static final Color WALL_TOP = Color.web("#526b7d");
    private static final Color WALL_HIGHLIGHT = Color.web("#7890a1");
    private static final Color WALL_BASE = Color.web("#1b2a38");
    private static final Color WALL_BOLT = Color.web("#91a7b5");

    private static final Color EQUIPMENT_SHADOW = Color.web("#160d14");
    private static final Color EQUIPMENT_BODY = Color.web("#5d3040");
    private static final Color EQUIPMENT_PANEL = Color.web("#85485a");
    private static final Color EQUIPMENT_EDGE = Color.web("#b46770");
    private static final Color SCREEN_DARK = Color.web("#061820");
    private static final Color SCREEN_LIGHT = Color.web("#39d9c5");
    private static final Color EQUIPMENT_AMBER = Color.web("#f29e4c");
    private static final Color SECTOR_TEXT = Color.web("#72e9f4", 0.58);

    private static final Paint BACKGROUND = new LinearGradient(
            0.0,
            0.0,
            0.0,
            1.0,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#07111f")),
            new Stop(1.0, Color.web("#030914"))
    );
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
    private static final Font SECTOR_FONT = Font.font(
            "Monospaced",
            FontWeight.BOLD,
            8.0
    );

    private final GraphicsContext graphics;
    private TileMap cachedTileMap;
    private WritableImage cachedTiles;

    LaboratoryPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void drawBackground(double viewportWidth, double viewportHeight) {
        graphics.setEffect(null);
        graphics.setFill(BACKGROUND);
        graphics.fillRect(0.0, 0.0, viewportWidth, viewportHeight);

        graphics.setFill(BACKGROUND_GRID);
        for (double x = 0.0; x < viewportWidth; x += 64.0) {
            graphics.fillRect(x, 0.0, 1.0, viewportHeight);
        }
        for (double y = 0.0; y < viewportHeight; y += 64.0) {
            graphics.fillRect(0.0, y, viewportWidth, 1.0);
        }
    }

    void drawMap(TileMap tileMap, RenderLayout layout) {
        ensureTileCache(tileMap);
        drawMapFrame(layout);
        graphics.drawImage(cachedTiles, layout.mapX(), layout.mapY());
        drawMapBorder(layout);
    }

    private void ensureTileCache(TileMap tileMap) {
        if (tileMap == cachedTileMap && cachedTiles != null) {
            return;
        }

        Canvas cacheCanvas = new Canvas(tileMap.pixelWidth(), tileMap.pixelHeight());
        GraphicsContext cacheGraphics = cacheCanvas.getGraphicsContext2D();
        cacheGraphics.setImageSmoothing(false);
        LaboratoryPainter cachePainter = new LaboratoryPainter(cacheGraphics);
        cachePainter.drawTiles(tileMap, 0.0, 0.0);

        SnapshotParameters snapshotParameters = new SnapshotParameters();
        snapshotParameters.setFill(Color.TRANSPARENT);
        cachedTiles = new WritableImage(tileMap.pixelWidth(), tileMap.pixelHeight());
        cacheCanvas.snapshot(snapshotParameters, cachedTiles);
        cachedTileMap = tileMap;
    }

    private void drawTiles(TileMap tileMap, double mapX, double mapY) {
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
                }
            }
        }

        drawSectorLabels(mapX, mapY);
    }

    private void drawMapFrame(RenderLayout layout) {
        graphics.setFill(MAP_SHADOW);
        graphics.fillRoundRect(
                layout.mapX() - 12.0,
                layout.mapY() - 10.0,
                layout.mapWidth() + 24.0,
                layout.mapHeight() + 24.0,
                12.0,
                12.0
        );
        graphics.setFill(MAP_FRAME);
        graphics.fillRect(
                layout.mapX() - 6.0,
                layout.mapY() - 6.0,
                layout.mapWidth() + 12.0,
                layout.mapHeight() + 12.0
        );
    }

    private void drawFloor(int row, int column, double x, double y, double size) {
        if (VisualAssets.FLOOR != null) {
            drawTileImage(VisualAssets.FLOOR, row, column, x, y, size);
            drawFloorAccent(row, column, x, y);
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

        drawFloorAccent(row, column, x, y);
    }

    private void drawFloorAccent(int row, int column, double x, double y) {
        int variant = tileVariant(row, column);
        if (variant == 1) {
            graphics.setFill(FLOOR_DETAIL);
            graphics.fillRect(x + 12.0, y + 30.0, 10.0, 1.0);
        } else if (variant == 2) {
            graphics.setFill(FLOOR_DETAIL);
            graphics.fillRect(x + 30.0, y + 12.0, 1.0, 9.0);
        } else if (variant == 3) {
            graphics.setFill(FLOOR_LIGHT);
            graphics.fillRect(x + 18.0, y + 18.0, 4.0, 2.0);
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
        if (VisualAssets.WALL != null) {
            drawTileImage(VisualAssets.WALL, row, column, x, y, size);
            drawExposedWallEdge(tileMap, row, column, x, y, size);
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
    }

    private void drawEquipment(int row, int column, double x, double y, double size) {
        Image equipment = tileVariant(row, column) % 2 == 0
                ? VisualAssets.TERMINAL
                : VisualAssets.BIO_POD;
        if (equipment != null) {
            drawEquipmentMount(x, y, size);
            graphics.drawImage(equipment, x + 1.0, y + 1.0, size - 2.0, size - 2.0);
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
    }

    private void drawLaboratoryLighting(
            double mapX,
            double mapY,
            double mapWidth,
            double mapHeight
    ) {
        graphics.setFill(CYAN_LIGHT);
        graphics.fillOval(mapX + 22.0, mapY + 28.0, 230.0, 190.0);
        graphics.fillOval(mapX + mapWidth - 252.0, mapY + 28.0, 230.0, 190.0);
        graphics.fillOval(mapX + 170.0, mapY + mapHeight - 218.0, 270.0, 190.0);

        graphics.setFill(MAGENTA_LIGHT);
        graphics.fillOval(mapX + mapWidth - 206.0, mapY + mapHeight - 210.0, 185.0, 170.0);
    }

    private void drawSectorLabels(double mapX, double mapY) {
        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFont(SECTOR_FONT);
        graphics.setFill(SECTOR_TEXT);
        graphics.fillText("BIO LAB", mapX + 48.0, mapY + 66.0);
        graphics.fillText("DADOS", mapX + 366.0, mapY + 66.0);
        graphics.fillText("CONTROLE", mapX + 646.0, mapY + 66.0);
        graphics.fillText("SETOR 07", mapX + 252.0, mapY + 267.0);
        graphics.fillText("PESQUISA", mapX + 244.0, mapY + 427.0);
        graphics.fillText("SERVIDORES", mapX + 644.0, mapY + 427.0);
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
        if (!isWall(tileMap, row + 1, column)) {
            graphics.setFill(MAP_BORDER);
            graphics.fillRect(x + 3.0, y + size - 3.0, size - 6.0, 2.0);
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

    private void drawMapBorder(RenderLayout layout) {
        graphics.setFill(MAP_BORDER);
        graphics.fillRect(layout.mapX() - 2.0, layout.mapY() - 2.0, layout.mapWidth() + 4.0, 2.0);
        graphics.fillRect(
                layout.mapX() - 2.0,
                layout.mapY() + layout.mapHeight(),
                layout.mapWidth() + 4.0,
                2.0
        );
        graphics.fillRect(layout.mapX() - 2.0, layout.mapY(), 2.0, layout.mapHeight());
        graphics.fillRect(
                layout.mapX() + layout.mapWidth(),
                layout.mapY(),
                2.0,
                layout.mapHeight()
        );

        graphics.setFill(MAP_CORNER);
        drawCorner(layout.mapX() - 5.0, layout.mapY() - 5.0, 1.0, 1.0);
        drawCorner(layout.mapX() + layout.mapWidth() + 5.0, layout.mapY() - 5.0, -1.0, 1.0);
        drawCorner(layout.mapX() - 5.0, layout.mapY() + layout.mapHeight() + 5.0, 1.0, -1.0);
        drawCorner(
                layout.mapX() + layout.mapWidth() + 5.0,
                layout.mapY() + layout.mapHeight() + 5.0,
                -1.0,
                -1.0
        );
    }

    private void drawCorner(double x, double y, double horizontalDirection, double verticalDirection) {
        double horizontalX = horizontalDirection > 0.0 ? x : x - 14.0;
        double verticalY = verticalDirection > 0.0 ? y : y - 14.0;
        graphics.fillRect(horizontalX, y - 1.0, 14.0, 2.0);
        graphics.fillRect(x - 1.0, verticalY, 2.0, 14.0);
    }

    private static boolean isWall(TileMap tileMap, int row, int column) {
        return row >= 0
                && row < tileMap.rows()
                && column >= 0
                && column < tileMap.columns()
                && tileMap.tileAt(row, column) == TileType.WALL;
    }

    private static int tileVariant(int row, int column) {
        return Math.floorMod(row * 31 + column * 17, 4);
    }
}
