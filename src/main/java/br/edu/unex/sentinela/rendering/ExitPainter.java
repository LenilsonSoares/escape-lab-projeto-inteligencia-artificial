package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.entity.Player;
import br.edu.unex.sentinela.game.LaboratoryMap;
import br.edu.unex.sentinela.navigation.GridPosition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Desenha a sinalização e a abertura visual da saída do setor. */
final class ExitPainter {

    private static final Color BEACON = Color.web("#52e889");
    private static final Color BEACON_FILL = Color.web("#52e889", 0.10);
    private static final Color LABEL_BACKGROUND = Color.web("#03150f", 0.90);
    private static final Color OPENING = Color.web("#010907", 0.92);
    private static final Font LABEL_FONT = Font.font("Monospaced", FontWeight.BOLD, 8.0);
    private static final double OPENING_SPEED = 3.2;

    private final GraphicsContext graphics;
    private LaboratoryMap animatedMap;
    private long animationUpdatedAt;
    private double openProgress;

    ExitPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void draw(LaboratoryMap map, Player player, double tileSize, RenderLayout layout) {
        GridPosition exit = map.exitPosition();
        updateOpening(map, exit, player, tileSize);
        double scale = layout.scale();
        double displayedTileSize = tileSize * scale;
        double centerX = layout.screenX((exit.column() + 0.5) * tileSize);
        double centerY = layout.screenY((exit.row() + 0.5) * tileSize);
        double pulse = (Math.sin(visualSeconds() * 4.2) + 1.0) / 2.0;
        double radius = displayedTileSize * (0.42 + pulse * 0.13);

        graphics.save();
        graphics.setFill(BEACON_FILL);
        graphics.fillOval(centerX - displayedTileSize * 0.68,
                centerY - displayedTileSize * 0.68,
                displayedTileSize * 1.36, displayedTileSize * 1.36);
        drawOpeningDoor(centerX, centerY, displayedTileSize, scale);
        graphics.setStroke(Color.color(BEACON.getRed(), BEACON.getGreen(),
                BEACON.getBlue(), 0.42 + pulse * 0.45));
        graphics.setLineWidth(Math.max(1.0, 1.5 * scale));
        graphics.strokeOval(centerX - radius, centerY - radius, radius * 2.0, radius * 2.0);

        double arrowOffset = (3.0 + pulse * 3.0) * scale;
        double arrowSize = 5.0 * scale;
        graphics.setFill(BEACON);
        graphics.fillPolygon(
                new double[] {centerX - arrowOffset - arrowSize,
                    centerX - arrowOffset, centerX - arrowOffset - arrowSize},
                new double[] {centerY - arrowSize, centerY, centerY + arrowSize}, 3);
        drawLabel(centerX, centerY, displayedTileSize, scale);
        graphics.restore();
    }

    private void updateOpening(LaboratoryMap map, GridPosition exit,
            Player player, double tileSize) {
        long now = System.nanoTime();
        if (map != animatedMap) {
            animatedMap = map;
            openProgress = 0.0;
            animationUpdatedAt = now;
        }

        double elapsed = Math.min(0.05, (now - animationUpdatedAt) / 1_000_000_000.0);
        animationUpdatedAt = now;
        double exitCenterX = (exit.column() + 0.5) * tileSize;
        double exitCenterY = (exit.row() + 0.5) * tileSize;
        boolean playerIsNear = Math.hypot(player.centerX() - exitCenterX,
                player.centerY() - exitCenterY) <= tileSize * 1.6;
        double direction = playerIsNear ? 1.0 : -1.0;
        openProgress = Math.max(0.0,
                Math.min(1.0, openProgress + direction * elapsed * OPENING_SPEED));
    }

    private void drawOpeningDoor(double centerX, double centerY,
            double tileSize, double scale) {
        if (openProgress <= 0.0) {
            return;
        }

        double openingWidth = tileSize * 0.46 * openProgress;
        double openingHeight = tileSize * 0.62;
        double openingX = centerX - openingWidth / 2.0;
        double openingY = centerY - openingHeight / 2.0;
        graphics.setFill(OPENING);
        graphics.fillRect(openingX, openingY, openingWidth, openingHeight);
        graphics.setStroke(BEACON);
        graphics.setLineWidth(Math.max(1.0, scale));
        graphics.strokeLine(openingX, openingY, openingX, openingY + openingHeight);
        graphics.strokeLine(openingX + openingWidth, openingY,
                openingX + openingWidth, openingY + openingHeight);
    }

    private void drawLabel(double centerX, double centerY,
            double displayedTileSize, double scale) {
        double logicalWidth = 44.0;
        double logicalHeight = 15.0;
        double x = centerX - displayedTileSize / 2.0 - (logicalWidth + 4.0) * scale;
        double y = centerY - logicalHeight * scale / 2.0;

        graphics.save();
        graphics.translate(Math.rint(x), Math.rint(y));
        graphics.scale(scale, scale);
        graphics.setFill(LABEL_BACKGROUND);
        graphics.fillRect(0.0, 0.0, logicalWidth, logicalHeight);
        graphics.setStroke(BEACON);
        graphics.setLineWidth(1.0);
        graphics.strokeRect(0.5, 0.5, logicalWidth - 1.0, logicalHeight - 1.0);
        graphics.setFill(BEACON);
        graphics.setFont(LABEL_FONT);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.fillText(openProgress > 0.08 ? "ABRINDO" : "SAÍDA",
                logicalWidth / 2.0, 10.5, logicalWidth - 6.0);
        graphics.restore();
    }

    private static double visualSeconds() {
        return System.nanoTime() / 1_000_000_000.0;
    }
}
