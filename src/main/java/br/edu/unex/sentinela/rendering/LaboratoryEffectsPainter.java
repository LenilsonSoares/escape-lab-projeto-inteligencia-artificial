package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;

/**
 * Acrescenta animações ambientais leves sobre o laboratório já desenhado.
 */
final class LaboratoryEffectsPainter {

    private static final int PARTICLE_COUNT = 18;
    private static final Color AMBER_SIGNAL = Color.web("#ffb84d");
    private static final Paint TOP_SHADE = new LinearGradient(
            0.0,
            0.0,
            0.0,
            1.0,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#010611", 0.46)),
            new Stop(1.0, Color.TRANSPARENT)
    );
    private static final Paint BOTTOM_SHADE = new LinearGradient(
            0.0,
            1.0,
            0.0,
            0.0,
            true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#010611", 0.52)),
            new Stop(1.0, Color.TRANSPARENT)
    );

    private final GraphicsContext graphics;

    LaboratoryEffectsPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void draw(TileMap tileMap, RenderLayout layout, LaboratoryTheme theme) {
        double seconds = System.nanoTime() / 1_000_000_000.0;

        graphics.save();
        graphics.beginPath();
        graphics.rect(layout.mapX(), layout.mapY(), layout.mapWidth(), layout.mapHeight());
        graphics.clip();
        graphics.setFill(theme.ambientTint());
        graphics.fillRect(layout.mapX(), layout.mapY(), layout.mapWidth(), layout.mapHeight());
        drawScan(layout, theme, seconds);
        drawEquipmentSignals(tileMap, layout, theme, seconds);
        drawParticles(layout, theme, seconds);
        drawEdgeShade(layout);
        graphics.restore();
    }

    private void drawScan(RenderLayout layout, LaboratoryTheme theme, double seconds) {
        double cycle = (seconds * 0.095) % 1.0;
        double bandHeight = layout.mapHeight() * 0.22;
        double y = layout.mapY() - bandHeight + cycle * (layout.mapHeight() + bandHeight * 2.0);

        graphics.setFill(theme.scanBand());
        graphics.fillRect(layout.mapX(), y, layout.mapWidth(), bandHeight);
        graphics.setFill(theme.scanCore());
        graphics.fillRect(layout.mapX(), y + bandHeight / 2.0, layout.mapWidth(), layout.scale());
    }

    private void drawEquipmentSignals(
            TileMap tileMap,
            RenderLayout layout,
            LaboratoryTheme theme,
            double seconds
    ) {
        double tileSize = tileMap.tileSize() * layout.scale();
        double signalSize = Math.max(1.5, 2.0 * layout.scale());
        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                if (tileMap.tileAt(row, column) != TileType.EQUIPMENT) {
                    continue;
                }

                double phase = seconds * 3.2 + row * 0.73 + column * 0.41;
                double intensity = 0.38 + (Math.sin(phase) + 1.0) * 0.27;
                graphics.setGlobalAlpha(intensity);
                graphics.setFill(
                        (row + column) % 4 == 0
                                ? AMBER_SIGNAL
                                : theme.equipmentSignal()
                );
                graphics.fillRect(
                        layout.mapX() + column * tileSize + tileSize * 0.72,
                        layout.mapY() + row * tileSize + tileSize * 0.18,
                        signalSize,
                        signalSize
                );
            }
        }
        graphics.setGlobalAlpha(1.0);
    }

    private void drawParticles(RenderLayout layout, LaboratoryTheme theme, double seconds) {
        double travelHeight = layout.mapHeight() + 30.0 * layout.scale();
        for (int index = 0; index < PARTICLE_COUNT; index++) {
            double horizontal = fraction(index * 0.61803398875 + 0.17);
            double speed = 4.5 + (index % 5) * 1.15;
            double vertical = fraction(index * 0.38196601125 - seconds * speed / travelHeight);
            double blink = (Math.sin(seconds * 1.7 + index * 1.9) + 1.0) / 2.0;
            double size = (index % 3 == 0 ? 1.5 : 1.0) * layout.scale();

            graphics.setGlobalAlpha(0.10 + blink * 0.24);
            graphics.setFill(theme.particle());
            graphics.fillRect(
                    layout.mapX() + 8.0 * layout.scale() + horizontal * (layout.mapWidth() - 16.0 * layout.scale()),
                    layout.mapY() - 15.0 * layout.scale() + vertical * travelHeight,
                    size,
                    size
            );
        }
        graphics.setGlobalAlpha(1.0);
    }

    private void drawEdgeShade(RenderLayout layout) {
        double shadeHeight = Math.min(layout.mapHeight() * 0.16, 92.0 * layout.scale());
        graphics.setFill(TOP_SHADE);
        graphics.fillRect(layout.mapX(), layout.mapY(), layout.mapWidth(), shadeHeight);
        graphics.setFill(BOTTOM_SHADE);
        graphics.fillRect(
                layout.mapX(),
                layout.mapY() + layout.mapHeight() - shadeHeight,
                layout.mapWidth(),
                shadeHeight
        );
    }

    private static double fraction(double value) {
        return value - Math.floor(value);
    }
}
