package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import br.edu.unex.sentinela.navigation.GridPosition;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.Objects;

/**
 * Resume no HUD o mesmo mapa lógico usado pelo A* e pelas colisões.
 */
final class MiniMapPainter {

    private static final Color BACKGROUND = Color.web("#06111b");
    private static final Color FLOOR = Color.web("#173245");
    private static final Color WALL = Color.web("#4a6275");
    private static final Color EQUIPMENT = Color.web("#88465a");
    private static final Color EXIT = Color.web("#52e889");
    private static final Color BORDER = Color.web("#2c6a83");
    private static final Color PATH = Color.web("#66ecff");
    private static final Color PLAYER = Color.web("#38d9a9");
    private static final Color AGENT = Color.web("#ffb703");
    private static final Color DESTINATION = Color.web("#ffd166");

    private final GraphicsContext graphics;
    private TileMap cachedTileMap;
    private int cachedWidth;
    private int cachedHeight;
    private WritableImage cachedBase;

    MiniMapPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void draw(GameWorld world, double x, double y, double width, double height) {
        int imageWidth = Math.max(1, (int) Math.rint(width));
        int imageHeight = Math.max(1, (int) Math.rint(height));
        ensureBase(world.tileMap(), imageWidth, imageHeight);

        graphics.drawImage(cachedBase, x, y, width, height);
        if (!world.escapeCompleted()) {
            drawPath(world, x, y, width, height);
            drawDestination(world, x, y, width, height);
        }
        drawMarker(
                playerPosition(world),
                PLAYER,
                world.tileMap(),
                x,
                y,
                width,
                height,
                5.0
        );
        drawMarker(
                world.autonomousAgent().currentGridPosition(),
                AGENT,
                world.tileMap(),
                x,
                y,
                width,
                height,
                4.0
        );
    }

    static double proportionalHeight(TileMap tileMap, double width) {
        Objects.requireNonNull(tileMap, "tileMap");
        if (!Double.isFinite(width) || width <= 0.0) {
            throw new IllegalArgumentException("A largura do minimapa deve ser positiva");
        }
        return width * tileMap.rows() / tileMap.columns();
    }

    /**
     * Ajusta a largura para que cada tile ocupe um número inteiro de pixels.
     */
    static double fittedWidth(TileMap tileMap, double maximumWidth) {
        Objects.requireNonNull(tileMap, "tileMap");
        if (!Double.isFinite(maximumWidth) || maximumWidth <= 0.0) {
            throw new IllegalArgumentException("A largura máxima deve ser positiva");
        }
        int cellSize = Math.max(1, (int) Math.floor(maximumWidth / tileMap.columns()));
        return cellSize * tileMap.columns();
    }

    private void ensureBase(TileMap tileMap, int width, int height) {
        if (cachedTileMap == tileMap
                && cachedWidth == width
                && cachedHeight == height
                && cachedBase != null) {
            return;
        }

        Canvas canvas = new Canvas(width, height);
        GraphicsContext cacheGraphics = canvas.getGraphicsContext2D();
        cacheGraphics.setImageSmoothing(false);
        cacheGraphics.setFill(BACKGROUND);
        cacheGraphics.fillRect(0.0, 0.0, width, height);

        double cellWidth = (double) width / tileMap.columns();
        double cellHeight = (double) height / tileMap.rows();
        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                cacheGraphics.setFill(tileColor(tileMap.tileAt(row, column)));
                cacheGraphics.fillRect(
                        column * cellWidth + 0.5,
                        row * cellHeight + 0.5,
                        Math.max(1.0, cellWidth - 1.0),
                        Math.max(1.0, cellHeight - 1.0)
                );
            }
        }

        cacheGraphics.setStroke(BORDER);
        cacheGraphics.setLineWidth(1.0);
        cacheGraphics.strokeRect(0.5, 0.5, width - 1.0, height - 1.0);

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        cachedBase = new WritableImage(width, height);
        canvas.snapshot(parameters, cachedBase);
        cachedTileMap = tileMap;
        cachedWidth = width;
        cachedHeight = height;
    }

    private void drawPath(GameWorld world, double x, double y, double width, double height) {
        if (world.navigationPath().size() < 2) {
            return;
        }

        TileMap tileMap = world.tileMap();
        double cellWidth = width / tileMap.columns();
        double cellHeight = height / tileMap.rows();
        graphics.setStroke(PATH);
        graphics.setLineWidth(1.5);
        for (int index = 1; index < world.navigationPath().size(); index++) {
            GridPosition previous = world.navigationPath().get(index - 1);
            GridPosition current = world.navigationPath().get(index);
            graphics.strokeLine(
                    markerX(previous, x, cellWidth),
                    markerY(previous, y, cellHeight),
                    markerX(current, x, cellWidth),
                    markerY(current, y, cellHeight)
            );
        }
    }

    private void drawDestination(GameWorld world, double x, double y, double width, double height) {
        TileMap tileMap = world.tileMap();
        double cellWidth = width / tileMap.columns();
        double cellHeight = height / tileMap.rows();
        GridPosition destination = world.navigationDestination();
        double markerSize = Math.max(4.0, Math.min(cellWidth, cellHeight) - 3.0);

        graphics.setStroke(DESTINATION);
        graphics.setLineWidth(1.0);
        graphics.strokeRect(
                markerX(destination, x, cellWidth) - markerSize / 2.0,
                markerY(destination, y, cellHeight) - markerSize / 2.0,
                markerSize,
                markerSize
        );
    }

    private void drawMarker(
            GridPosition position,
            Color color,
            TileMap tileMap,
            double x,
            double y,
            double width,
            double height,
            double markerSize
    ) {
        double cellWidth = width / tileMap.columns();
        double cellHeight = height / tileMap.rows();
        graphics.setFill(color);
        graphics.fillRect(
                markerX(position, x, cellWidth) - markerSize / 2.0,
                markerY(position, y, cellHeight) - markerSize / 2.0,
                markerSize,
                markerSize
        );
    }

    private static GridPosition playerPosition(GameWorld world) {
        int row = (int) Math.floor(world.player().centerY() / world.tileMap().tileSize());
        int column = (int) Math.floor(world.player().centerX() / world.tileMap().tileSize());
        return new GridPosition(row, column);
    }

    private static Color tileColor(TileType tileType) {
        return switch (tileType) {
            case LAB_FLOOR -> FLOOR;
            case WALL -> WALL;
            case EQUIPMENT -> EQUIPMENT;
            case EXIT -> EXIT;
        };
    }

    private static double markerX(GridPosition position, double x, double cellWidth) {
        return x + (position.column() + 0.5) * cellWidth;
    }

    private static double markerY(GridPosition position, double y, double cellHeight) {
        return y + (position.row() + 0.5) * cellHeight;
    }
}
