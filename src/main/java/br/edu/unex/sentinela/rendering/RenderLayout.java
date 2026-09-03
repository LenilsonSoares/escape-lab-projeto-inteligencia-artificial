package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.TileMap;
import java.util.Objects;

/**
 * Posições de tela usadas para manter mapa e painéis separados.
 */
record RenderLayout(
        double scale,
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

    static final double BASE_VIEWPORT_WIDTH = 1_280.0;
    static final double BASE_VIEWPORT_HEIGHT = 720.0;
    static final double OUTER_MARGIN = 12.0;
    static final double GAP = 12.0;
    static final double BAR_HEIGHT = 36.0;
    private static final double SCALE_STEP = 0.25;
    private static final double MAX_RAIL_WIDTH = 236.0;
    private static final double COMPACT_WIDTH = 190.0;

    static RenderLayout calculate(double viewportWidth, double viewportHeight, TileMap tileMap) {
        requirePositive(viewportWidth, "A largura da área de desenho");
        requirePositive(viewportHeight, "A altura da área de desenho");
        Objects.requireNonNull(tileMap, "O tilemap não pode ser nulo");

        double scale = calculateScale(viewportWidth, viewportHeight);
        double outerMargin = OUTER_MARGIN * scale;
        double gap = GAP * scale;
        double barHeight = BAR_HEIGHT * scale;
        double mapWidth = tileMap.pixelWidth() * scale;
        double mapHeight = tileMap.pixelHeight() * scale;
        double mapX = Math.rint(Math.max(0.0, (viewportWidth - mapWidth) / 2.0));
        double mapY = Math.rint(Math.max(0.0, (viewportHeight - mapHeight) / 2.0));

        double sideSpace = Math.min(mapX, viewportWidth - mapX - mapWidth);
        double railWidth = Math.max(
                0.0,
                Math.min(MAX_RAIL_WIDTH * scale, sideSpace - outerMargin - gap)
        );
        double leftRailX = Math.rint(mapX - gap - railWidth);
        double rightRailX = Math.rint(mapX + mapWidth + gap);
        double headerY = Math.max(outerMargin, mapY - barHeight - gap);
        double footerY = Math.min(
                viewportHeight - outerMargin - barHeight,
                mapY + mapHeight + gap
        );

        return new RenderLayout(
                scale,
                mapX,
                mapY,
                mapWidth,
                mapHeight,
                leftRailX,
                rightRailX,
                railWidth,
                headerY,
                footerY,
                railWidth < COMPACT_WIDTH * scale
        );
    }

    double screenX(double worldX) {
        return Math.rint(mapX + worldX * scale);
    }

    double screenY(double worldY) {
        return Math.rint(mapY + worldY * scale);
    }

    double scaled(double logicalSize) {
        return logicalSize * scale;
    }

    double gap() {
        return scaled(GAP);
    }

    double outerMargin() {
        return scaled(OUTER_MARGIN);
    }

    double barHeight() {
        return scaled(BAR_HEIGHT);
    }

    /**
     * Devolve as mesmas posições em unidades lógicas para desenhar o HUD sob
     * uma transformação de escala única.
     */
    RenderLayout logicalCoordinates() {
        if (scale == 1.0) {
            return this;
        }

        return new RenderLayout(
                1.0,
                mapX / scale,
                mapY / scale,
                mapWidth / scale,
                mapHeight / scale,
                leftRailX / scale,
                rightRailX / scale,
                railWidth / scale,
                headerY / scale,
                footerY / scale,
                compact
        );
    }

    private static double calculateScale(double viewportWidth, double viewportHeight) {
        double availableScale = Math.min(
                viewportWidth / BASE_VIEWPORT_WIDTH,
                viewportHeight / BASE_VIEWPORT_HEIGHT
        );

        // Passos de um quarto deixam o redimensionamento gradual; Full HD,
        // 2K e 4K continuam usando escalas previsíveis para o pixel art.
        double steppedScale = Math.floor(availableScale / SCALE_STEP) * SCALE_STEP;
        return Math.max(1.0, steppedScale);
    }

    private static void requirePositive(double value, String description) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(description + " deve ser maior que zero");
        }
    }
}
