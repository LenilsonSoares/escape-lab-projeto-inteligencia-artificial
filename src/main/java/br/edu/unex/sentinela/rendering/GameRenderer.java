package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.entity.Player;
import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import br.edu.unex.sentinela.telemetry.FrameMetrics;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Representa o estado do jogo no Canvas sem alterar suas regras.
 */
public final class GameRenderer {

    private static final Color BACKGROUND = Color.web("#07111f");
    private static final Color LAB_FLOOR_COLOR = Color.web("#172b3b");
    private static final Color WALL_COLOR = Color.web("#3c5266");
    private static final Color WALL_DETAIL = Color.web("#62788b");
    private static final Color EQUIPMENT_COLOR = Color.web("#713d4b");
    private static final Color EQUIPMENT_LIGHT = Color.web("#e36d5b");
    private static final Color TILE_LINE = Color.web("#24435a", 0.72);
    private static final Color BORDER = Color.web("#2d5577");
    private static final Color PRIMARY = Color.web("#38d9a9");
    private static final Color PRIMARY_DARK = Color.web("#126c60");
    private static final Color TEXT = Color.web("#e6f2ff");
    private static final Color MUTED_TEXT = Color.web("#91a9bd");
    private static final Color PANEL = Color.web("#0d1d2d", 0.92);

    private static final double PANEL_X = 20.0;
    private static final double PANEL_Y = 20.0;
    private static final double PANEL_WIDTH = 224.0;
    private static final double PANEL_HEIGHT = 136.0;

    private final Canvas canvas;
    private final GraphicsContext graphics;
    private final DropShadow playerGlow;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.graphics = canvas.getGraphicsContext2D();
        this.playerGlow = new DropShadow(16.0, Color.web("#38d9a9", 0.55));
    }

    public void render(GameWorld world, FrameMetrics metrics) {
        clear();
        double mapX = Math.max(0.0, (viewportWidth() - world.tileMap().pixelWidth()) / 2.0);
        double mapY = Math.max(0.0, (viewportHeight() - world.tileMap().pixelHeight()) / 2.0);
        drawTileMap(world.tileMap(), mapX, mapY);
        drawPlayer(world.player(), mapX, mapY);
        drawDebugPanel(world.player(), metrics);
        drawInstructions();
    }

    public double viewportWidth() {
        return canvas.getWidth();
    }

    public double viewportHeight() {
        return canvas.getHeight();
    }

    private void clear() {
        graphics.setEffect(null);
        graphics.setFill(BACKGROUND);
        graphics.fillRect(0.0, 0.0, viewportWidth(), viewportHeight());
    }

    private void drawTileMap(TileMap tileMap, double mapX, double mapY) {
        double tileSize = tileMap.tileSize();
        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                double x = mapX + column * tileSize;
                double y = mapY + row * tileSize;
                drawTile(tileMap.tileAt(row, column), x, y, tileSize);
            }
        }

        graphics.setStroke(BORDER);
        graphics.setLineWidth(2.0);
        graphics.strokeRect(mapX + 1.0, mapY + 1.0, tileMap.pixelWidth() - 2.0, tileMap.pixelHeight() - 2.0);
    }

    private void drawTile(TileType tileType, double x, double y, double size) {
        Color tileColor = switch (tileType) {
            case LAB_FLOOR -> LAB_FLOOR_COLOR;
            case WALL -> WALL_COLOR;
            case EQUIPMENT -> EQUIPMENT_COLOR;
        };

        graphics.setFill(tileColor);
        graphics.fillRect(x, y, size, size);
        graphics.setStroke(TILE_LINE);
        graphics.setLineWidth(1.0);
        graphics.strokeRect(x + 0.5, y + 0.5, size - 1.0, size - 1.0);

        if (tileType == TileType.WALL) {
            graphics.setFill(WALL_DETAIL);
            graphics.fillRect(x + 5.0, y + 5.0, size - 10.0, 5.0);
        } else if (tileType == TileType.EQUIPMENT) {
            graphics.setFill(EQUIPMENT_LIGHT);
            graphics.fillRoundRect(x + 8.0, y + 8.0, size - 16.0, size - 16.0, 5.0, 5.0);
        }
    }

    private void drawPlayer(Player player, double mapX, double mapY) {
        double x = mapX + player.x();
        double y = mapY + player.y();
        double size = player.size();
        double antennaTop = Math.max(1.0, y - 10.0);

        graphics.setEffect(playerGlow);
        graphics.setFill(PRIMARY_DARK);
        graphics.fillRoundRect(x, y, size, size, 10.0, 10.0);
        graphics.setFill(PRIMARY);
        graphics.fillOval(x + 7.0, y + 7.0, size - 14.0, size - 14.0);
        graphics.setEffect(null);

        graphics.setStroke(TEXT);
        graphics.setLineWidth(2.0);
        graphics.strokeLine(x + size / 2.0, y + 4.0, x + size / 2.0, antennaTop + 5.0);
        graphics.strokeOval(x + size / 2.0 - 2.5, antennaTop, 5.0, 5.0);
    }

    private void drawDebugPanel(Player player, FrameMetrics metrics) {
        graphics.setFill(PANEL);
        graphics.fillRoundRect(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT, 14.0, 14.0);
        graphics.setStroke(BORDER);
        graphics.setLineWidth(1.0);
        graphics.strokeRoundRect(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT, 14.0, 14.0);

        graphics.setTextAlign(TextAlignment.LEFT);
        graphics.setFill(PRIMARY);
        graphics.setFont(Font.font("System", FontWeight.BOLD, 15.0));
        graphics.fillText("ESCAPE LAB", PANEL_X + 16.0, PANEL_Y + 26.0);

        graphics.setFill(TEXT);
        graphics.setFont(Font.font("Monospaced", FontWeight.NORMAL, 13.0));
        graphics.fillText(
                "POS  x:%6.1f  y:%6.1f".formatted(player.x(), player.y()),
                PANEL_X + 16.0,
                PANEL_Y + 57.0
        );
        graphics.fillText(
                "FPS  %6.1f".formatted(metrics.framesPerSecond()),
                PANEL_X + 16.0,
                PANEL_Y + 82.0
        );
        graphics.fillText(
                "DT   %6.2f ms".formatted(metrics.deltaTime() * 1_000.0),
                PANEL_X + 16.0,
                PANEL_Y + 107.0
        );

        graphics.setFill(MUTED_TEXT);
        graphics.setFont(Font.font("System", 11.0));
        graphics.fillText("STATUS  LABORATÓRIO EM ALERTA", PANEL_X + 16.0, PANEL_Y + 126.0);
    }

    private void drawInstructions() {
        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setFill(MUTED_TEXT);
        graphics.setFont(Font.font("System", FontWeight.NORMAL, 13.0));
        graphics.fillText(
                "MOVIMENTO  •  WASD ou SETAS",
                viewportWidth() - 20.0,
                viewportHeight() - 22.0
        );
    }
}
