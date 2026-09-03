package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.game.LaboratoryMap;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Desenha o avanço real do jogador pelos três setores do laboratório.
 */
final class MapProgressPainter {

    private static final Color COMPLETED = Color.web("#52e889");
    private static final Color PENDING = Color.web("#29485a");
    private static final Color TRACK = Color.web("#28556d");

    private final GraphicsContext graphics;
    private final Font labelFont = Font.font("Monospaced", FontWeight.BOLD, 8.0);

    MapProgressPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void draw(GameWorld world, RenderLayout layout, LaboratoryTheme theme) {
        int mapCount = LaboratoryMap.values().length;
        int currentMap = world.currentMap().number();
        int completedMaps = world.escapeCompleted() ? mapCount : currentMap - 1;
        double centerX = layout.mapX() + layout.mapWidth() / 2.0;
        double trackY = layout.headerY() + 23.0;
        double spacing = 34.0;
        double firstX = centerX - spacing;

        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setFont(labelFont);
        Color current = theme.accent();
        graphics.setFill(world.escapeCompleted() ? COMPLETED : current);
        graphics.fillText(
                world.escapeCompleted()
                        ? "3 / 3  CONCLUÍDO"
                        : "SETOR %d / %d".formatted(currentMap, mapCount),
                centerX,
                layout.headerY() + 11.0
        );

        graphics.setFill(TRACK);
        graphics.fillRect(firstX, trackY - 1.0, spacing * (mapCount - 1), 2.0);

        double pulse = (Math.sin(System.nanoTime() / 1_000_000_000.0 * 4.0) + 1.0) / 2.0;
        for (int mapNumber = 1; mapNumber <= mapCount; mapNumber++) {
            double x = firstX + (mapNumber - 1) * spacing;
            if (mapNumber <= completedMaps) {
                drawNode(x, trackY, 4.0, COMPLETED);
            } else if (mapNumber == currentMap && !world.escapeCompleted()) {
                graphics.setStroke(Color.color(
                        current.getRed(),
                        current.getGreen(),
                        current.getBlue(),
                        0.25 + pulse * 0.45
                ));
                graphics.setLineWidth(1.0);
                double ringRadius = 6.0 + pulse * 2.0;
                graphics.strokeOval(
                        x - ringRadius,
                        trackY - ringRadius,
                        ringRadius * 2.0,
                        ringRadius * 2.0
                );
                drawNode(x, trackY, 4.0, current);
            } else {
                drawNode(x, trackY, 3.0, PENDING);
            }
        }
    }

    private void drawNode(double x, double y, double radius, Color color) {
        graphics.setFill(Color.web("#04101b"));
        graphics.fillOval(x - radius - 2.0, y - radius - 2.0, radius * 2.0 + 4.0, radius * 2.0 + 4.0);
        graphics.setFill(color);
        graphics.fillOval(x - radius, y - radius, radius * 2.0, radius * 2.0);
    }
}
