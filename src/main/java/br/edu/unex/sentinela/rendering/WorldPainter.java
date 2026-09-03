package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.NavigationStatus;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;

/**
 * Desenha a rota e o destino nas coordenadas do mapa.
 */
final class WorldPainter {

    private static final Color PATH_NODE = Color.web("#061923");
    private static final Color DESTINATION = Color.web("#ffd166");
    private static final Color DESTINATION_FILL = Color.web("#ffd166", 0.12);
    private static final Color UNREACHABLE = Color.web("#ef476f");
    private static final Color UNREACHABLE_FILL = Color.web("#ef476f", 0.12);
    private final GraphicsContext graphics;
    private final DropShadow destinationGlow;
    private final DropShadow unreachableGlow;

    WorldPainter(GraphicsContext graphics) {
        this.graphics = graphics;
        this.destinationGlow = new DropShadow(9.0, Color.web("#ffd166", 0.62));
        this.unreachableGlow = new DropShadow(9.0, Color.web("#ef476f", 0.62));
    }

    void drawNavigation(
            List<GridPosition> path,
            GridPosition destination,
            NavigationStatus navigationStatus,
            double tileSize,
            RenderLayout layout,
            LaboratoryTheme theme
    ) {
        drawPath(path, tileSize, layout, theme);
        drawDestination(destination, navigationStatus, tileSize, layout);
    }

    private void drawPath(
            List<GridPosition> path,
            double tileSize,
            RenderLayout layout,
            LaboratoryTheme theme
    ) {
        if (path.isEmpty()) {
            return;
        }

        double scale = visualScale(layout);
        drawPathSegments(path, tileSize, layout, theme.routeHalo(), 9.0 * scale);
        drawPathSegments(path, tileSize, layout, theme.routeOuter(), 4.0 * scale);
        drawPathSegments(path, tileSize, layout, theme.routeCore(), 2.0 * scale);
        drawMovingPathPulse(path, tileSize, layout, scale, theme.routePulse());

        for (GridPosition position : path) {
            drawPathNode(position, tileSize, layout, scale, theme.routeCore());
        }
    }

    /**
     * Cria um fluxo luminoso sobre a rota já calculada. É apenas uma animação
     * visual: os pontos e a ordem do caminho continuam vindo do A*.
     */
    private void drawMovingPathPulse(
            List<GridPosition> path,
            double tileSize,
            RenderLayout layout,
            double scale,
            Color pulseColor
    ) {
        graphics.save();
        graphics.setStroke(pulseColor);
        graphics.setLineWidth(1.5 * scale);
        graphics.setLineCap(StrokeLineCap.BUTT);
        graphics.setLineDashes(5.0 * scale, 10.0 * scale);
        double dashCycle = 15.0 * scale;
        double dashOffset = (visualSeconds() * 28.0 * scale) % dashCycle;
        graphics.setLineDashOffset(-dashOffset);

        for (int index = 1; index < path.size(); index++) {
            GridPosition start = path.get(index - 1);
            GridPosition end = path.get(index);
            graphics.strokeLine(
                    tileCenterX(start, tileSize, layout),
                    tileCenterY(start, tileSize, layout),
                    tileCenterX(end, tileSize, layout),
                    tileCenterY(end, tileSize, layout)
            );
        }
        graphics.restore();
    }

    private void drawPathSegments(
            List<GridPosition> path,
            double tileSize,
            RenderLayout layout,
            Color color,
            double thickness
    ) {
        graphics.setFill(color);
        for (int index = 1; index < path.size(); index++) {
            drawPathSegment(
                    path.get(index - 1),
                    path.get(index),
                    tileSize,
                    layout,
                    thickness
            );
        }
    }

    private void drawPathSegment(
            GridPosition start,
            GridPosition end,
            double tileSize,
            RenderLayout layout,
            double thickness
    ) {
        double startX = tileCenterX(start, tileSize, layout);
        double startY = tileCenterY(start, tileSize, layout);
        double endX = tileCenterX(end, tileSize, layout);
        double endY = tileCenterY(end, tileSize, layout);

        if (start.row() == end.row()) {
            double left = Math.min(startX, endX);
            graphics.fillRect(
                    left,
                    startY - thickness / 2.0,
                    Math.abs(endX - startX),
                    thickness
            );
        } else {
            double top = Math.min(startY, endY);
            graphics.fillRect(
                    startX - thickness / 2.0,
                    top,
                    thickness,
                    Math.abs(endY - startY)
            );
        }
    }

    private void drawPathNode(
            GridPosition position,
            double tileSize,
            RenderLayout layout,
            double scale,
            Color routeCore
    ) {
        double centerX = tileCenterX(position, tileSize, layout);
        double centerY = tileCenterY(position, tileSize, layout);
        double outerSize = Math.max(4.0, 6.0 * scale);
        double coreSize = Math.max(2.0, 3.0 * scale);
        graphics.setFill(PATH_NODE);
        graphics.fillRect(
                centerX - outerSize / 2.0,
                centerY - outerSize / 2.0,
                outerSize,
                outerSize
        );
        graphics.setFill(routeCore);
        graphics.fillRect(
                centerX - coreSize / 2.0,
                centerY - coreSize / 2.0,
                coreSize,
                coreSize
        );
    }

    private void drawDestination(
            GridPosition destination,
            NavigationStatus navigationStatus,
            double tileSize,
            RenderLayout layout
    ) {
        double scale = visualScale(layout);
        destinationGlow.setRadius(9.0 * scale);
        unreachableGlow.setRadius(9.0 * scale);
        double displayedTileSize = tileSize * scale;
        double pulse = (Math.sin(visualSeconds() * 4.0) + 1.0) / 2.0;
        double inset = (5.5 - pulse * 1.5) * scale;
        double x = tileX(destination, tileSize, layout) + inset;
        double y = tileY(destination, tileSize, layout) + inset;
        double markerSize = displayedTileSize - inset * 2.0;
        boolean unreachable = navigationStatus == NavigationStatus.NO_PATH;
        Color marker = unreachable ? UNREACHABLE : DESTINATION;

        graphics.setFill(unreachable ? UNREACHABLE_FILL : DESTINATION_FILL);
        graphics.fillRect(x, y, markerSize, markerSize);
        graphics.setEffect(unreachable ? unreachableGlow : destinationGlow);
        graphics.setFill(marker);
        drawTargetCorners(x, y, markerSize, scale);
        double centerMark = 5.0 * scale;
        graphics.fillRect(
                x + markerSize / 2.0 - centerMark / 2.0,
                y + markerSize / 2.0 - centerMark / 2.0,
                centerMark,
                centerMark
        );
        graphics.setEffect(null);

        graphics.setStroke(Color.color(
                marker.getRed(),
                marker.getGreen(),
                marker.getBlue(),
                0.2 + pulse * 0.28
        ));
        graphics.setLineWidth(Math.max(1.0, scale));
        double ringInset = (2.0 + pulse * 3.0) * scale;
        graphics.strokeOval(
                x - ringInset,
                y - ringInset,
                markerSize + ringInset * 2.0,
                markerSize + ringInset * 2.0
        );
    }

    private void drawTargetCorners(double x, double y, double size, double scale) {
        double corner = 9.0 * scale;
        double thickness = Math.max(2.0, 3.0 * scale);
        graphics.fillRect(x, y, corner, thickness);
        graphics.fillRect(x, y, thickness, corner);
        graphics.fillRect(x + size - corner, y, corner, thickness);
        graphics.fillRect(x + size - thickness, y, thickness, corner);
        graphics.fillRect(x, y + size - thickness, corner, thickness);
        graphics.fillRect(x, y + size - corner, thickness, corner);
        graphics.fillRect(x + size - corner, y + size - thickness, corner, thickness);
        graphics.fillRect(x + size - thickness, y + size - corner, thickness, corner);
    }

    private static double tileX(
            GridPosition position,
            double tileSize,
            RenderLayout layout
    ) {
        return layout.screenX(position.column() * tileSize);
    }

    private static double tileY(
            GridPosition position,
            double tileSize,
            RenderLayout layout
    ) {
        return layout.screenY(position.row() * tileSize);
    }

    private static double tileCenterX(
            GridPosition position,
            double tileSize,
            RenderLayout layout
    ) {
        return layout.screenX((position.column() + 0.5) * tileSize);
    }

    private static double tileCenterY(
            GridPosition position,
            double tileSize,
            RenderLayout layout
    ) {
        return layout.screenY((position.row() + 0.5) * tileSize);
    }

    /**
     * Aplica a mesma escala inteira ou de meio passo usada pelo mapa. Assim a
     * pintura continua correta tanto no tamanho base quanto em telas 4K.
     */
    private static double visualScale(RenderLayout layout) {
        return layout.scale();
    }

    private static double visualSeconds() {
        return System.nanoTime() / 1_000_000_000.0;
    }
}
