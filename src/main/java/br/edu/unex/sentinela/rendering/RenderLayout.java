package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.TileMap;
import java.util.Objects;

/**
 * Posições de tela usadas para manter mapa e painéis separados.
 */
record RenderLayout(
        double mapX,
        double mapY,
        double mapWidth,
        double mapHeight,
        double leftRailX,
        double rightRailX,
        double railWidth,
        double headerY,
        double footerY,
        boolean compact
) {

    static final double OUTER_MARGIN = 12.0;
    static final double GAP = 12.0;
    static final double BAR_HEIGHT = 36.0;
    private static final double MAX_RAIL_WIDTH = 236.0;
    private static final double COMPACT_WIDTH = 190.0;

    static RenderLayout calculate(double viewportWidth, double viewportHeight, TileMap tileMap) {
        requirePositive(viewportWidth, "A largura da área de desenho");
        requirePositive(viewportHeight, "A altura da área de desenho");
        Objects.requireNonNull(tileMap, "O tilemap não pode ser nulo");

        double mapWidth = tileMap.pixelWidth();
        double mapHeight = tileMap.pixelHeight();
        double mapX = Math.rint(Math.max(0.0, (viewportWidth - mapWidth) / 2.0));
        double mapY = Math.rint(Math.max(0.0, (viewportHeight - mapHeight) / 2.0));

        double sideSpace = Math.min(mapX, viewportWidth - mapX - mapWidth);
        double railWidth = Math.max(
                0.0,
                Math.min(MAX_RAIL_WIDTH, sideSpace - OUTER_MARGIN - GAP)
        );
        double leftRailX = Math.rint(mapX - GAP - railWidth);
        double rightRailX = Math.rint(mapX + mapWidth + GAP);
        double headerY = Math.max(OUTER_MARGIN, mapY - BAR_HEIGHT - GAP);
        double footerY = Math.min(
                viewportHeight - OUTER_MARGIN - BAR_HEIGHT,
                mapY + mapHeight + GAP
        );

        return new RenderLayout(
                mapX,
                mapY,
                mapWidth,
                mapHeight,
                leftRailX,
                rightRailX,
                railWidth,
                headerY,
                footerY,
                railWidth < COMPACT_WIDTH
        );
    }

    double screenX(double worldX) {
        return Math.rint(mapX + worldX);
    }

    double screenY(double worldY) {
        return Math.rint(mapY + worldY);
    }

    private static void requirePositive(double value, String description) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(description + " deve ser maior que zero");
        }
    }
}
