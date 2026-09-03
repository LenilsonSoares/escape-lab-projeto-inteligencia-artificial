package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.TileMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

/**
 * Organiza o desenho do fundo, da moldura e do conteúdo visual do laboratório.
 */
final class LaboratoryPainter {

    private static final Color BACKGROUND_GRID = Color.web("#163145", 0.22);
    private static final Color BACKGROUND_GRID_MAJOR = Color.web("#246078", 0.18);
    private static final Color BACKGROUND_MARK = Color.web("#4ad9e6", 0.14);
    private static final Color MAP_SHADOW = Color.web("#000711", 0.74);
    private static final Color MAP_FRAME = Color.web("#102638");

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

    private final GraphicsContext graphics;
    private final LaboratoryTileCache tileCache = new LaboratoryTileCache();

    LaboratoryPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void drawBackground(double viewportWidth, double viewportHeight) {
        double backgroundScale = Math.max(
                1.0,
                Math.min(
                        viewportWidth / RenderLayout.BASE_VIEWPORT_WIDTH,
                        viewportHeight / RenderLayout.BASE_VIEWPORT_HEIGHT
                )
        );
        double gridStep = 64.0 * backgroundScale;
        double majorGridStep = 256.0 * backgroundScale;
        double markOffset = 128.0 * backgroundScale;

        graphics.setEffect(null);
        graphics.setFill(BACKGROUND);
        graphics.fillRect(0.0, 0.0, viewportWidth, viewportHeight);

        graphics.setFill(BACKGROUND_GRID);
        for (double x = 0.0; x < viewportWidth; x += gridStep) {
            graphics.fillRect(x, 0.0, backgroundScale, viewportHeight);
        }
        for (double y = 0.0; y < viewportHeight; y += gridStep) {
            graphics.fillRect(0.0, y, viewportWidth, backgroundScale);
        }

        graphics.setFill(BACKGROUND_GRID_MAJOR);
        for (double x = 0.0; x < viewportWidth; x += majorGridStep) {
            graphics.fillRect(x, 0.0, backgroundScale, viewportHeight);
        }
        for (double y = 0.0; y < viewportHeight; y += majorGridStep) {
            graphics.fillRect(0.0, y, viewportWidth, backgroundScale);
        }

        graphics.setFill(BACKGROUND_MARK);
        for (double x = markOffset; x < viewportWidth; x += majorGridStep) {
            for (double y = markOffset; y < viewportHeight; y += majorGridStep) {
                graphics.fillRect(
                        x - 3.0 * backgroundScale,
                        y,
                        7.0 * backgroundScale,
                        backgroundScale
                );
                graphics.fillRect(
                        x,
                        y - 3.0 * backgroundScale,
                        backgroundScale,
                        7.0 * backgroundScale
                );
            }
        }
    }

    void drawMap(TileMap tileMap, RenderLayout layout, LaboratoryTheme theme) {
        WritableImage tiles = tileCache.imageFor(tileMap);
        drawMapFrame(layout, theme);
        graphics.drawImage(
                tiles,
                layout.mapX(),
                layout.mapY(),
                layout.mapWidth(),
                layout.mapHeight()
        );
        drawMapBorder(layout, theme);
    }

    private void drawMapFrame(RenderLayout layout, LaboratoryTheme theme) {
        double unit = layout.scale();
        graphics.setFill(theme.accent(0.12));
        graphics.fillRoundRect(
                layout.mapX() - 17.0 * unit,
                layout.mapY() - 15.0 * unit,
                layout.mapWidth() + 34.0 * unit,
                layout.mapHeight() + 34.0 * unit,
                18.0 * unit,
                18.0 * unit
        );
        graphics.setFill(MAP_SHADOW);
        graphics.fillRoundRect(
                layout.mapX() - 12.0 * unit,
                layout.mapY() - 10.0 * unit,
                layout.mapWidth() + 24.0 * unit,
                layout.mapHeight() + 24.0 * unit,
                12.0 * unit,
                12.0 * unit
        );
        graphics.setFill(MAP_FRAME);
        graphics.fillRect(
                layout.mapX() - 6.0 * unit,
                layout.mapY() - 6.0 * unit,
                layout.mapWidth() + 12.0 * unit,
                layout.mapHeight() + 12.0 * unit
        );
    }

    private void drawMapBorder(RenderLayout layout, LaboratoryTheme theme) {
        double unit = layout.scale();
        double border = Math.max(1.0, 2.0 * unit);
        graphics.setFill(theme.accent(0.52));
        graphics.fillRect(
                layout.mapX() - border,
                layout.mapY() - border,
                layout.mapWidth() + border * 2.0,
                border
        );
        graphics.fillRect(
                layout.mapX() - border,
                layout.mapY() + layout.mapHeight(),
                layout.mapWidth() + border * 2.0,
                border
        );
        graphics.fillRect(layout.mapX() - border, layout.mapY(), border, layout.mapHeight());
        graphics.fillRect(
                layout.mapX() + layout.mapWidth(),
                layout.mapY(),
                border,
                layout.mapHeight()
        );

        graphics.setFill(theme.accent(0.20));
        graphics.fillRect(layout.mapX(), layout.mapY(), layout.mapWidth(), Math.max(1.0, unit));
        graphics.fillRect(
                layout.mapX(),
                layout.mapY() + layout.mapHeight() - Math.max(1.0, unit),
                layout.mapWidth(),
                Math.max(1.0, unit)
        );

        double cornerOffset = 5.0 * unit;
        double cornerLength = 14.0 * unit;
        double cornerThickness = Math.max(1.0, 2.0 * unit);
        graphics.setFill(theme.accent());
        drawCorner(
                layout.mapX() - cornerOffset,
                layout.mapY() - cornerOffset,
                1.0,
                1.0,
                cornerLength,
                cornerThickness
        );
        drawCorner(
                layout.mapX() + layout.mapWidth() + cornerOffset,
                layout.mapY() - cornerOffset,
                -1.0,
                1.0,
                cornerLength,
                cornerThickness
        );
        drawCorner(
                layout.mapX() - cornerOffset,
                layout.mapY() + layout.mapHeight() + cornerOffset,
                1.0,
                -1.0,
                cornerLength,
                cornerThickness
        );
        drawCorner(
                layout.mapX() + layout.mapWidth() + cornerOffset,
                layout.mapY() + layout.mapHeight() + cornerOffset,
                -1.0,
                -1.0,
                cornerLength,
                cornerThickness
        );
    }

    private void drawCorner(
            double x,
            double y,
            double horizontalDirection,
            double verticalDirection,
            double length,
            double thickness
    ) {
        double horizontalX = horizontalDirection > 0.0 ? x : x - length;
        double verticalY = verticalDirection > 0.0 ? y : y - length;
        graphics.fillRect(horizontalX, y - thickness / 2.0, length, thickness);
        graphics.fillRect(x - thickness / 2.0, verticalY, thickness, length);
    }
}
