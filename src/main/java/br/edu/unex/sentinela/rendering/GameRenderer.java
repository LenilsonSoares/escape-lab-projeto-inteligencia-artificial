package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.entity.Player;
import br.edu.unex.sentinela.game.GameWorld;
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
    private static final Color GRID_LINE = Color.web("#17304a", 0.48);
    private static final Color BORDER = Color.web("#2d5577");
    private static final Color PRIMARY = Color.web("#38d9a9");
    private static final Color PRIMARY_DARK = Color.web("#126c60");
    private static final Color TEXT = Color.web("#e6f2ff");
    private static final Color MUTED_TEXT = Color.web("#91a9bd");
    private static final Color PANEL = Color.web("#0d1d2d", 0.92);

    private static final double GRID_SIZE = 48.0;
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
        drawGrid();
        drawArenaBorder();
        drawPlayer(world.player());
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

    private void drawGrid() {
        graphics.setStroke(GRID_LINE);
        graphics.setLineWidth(1.0);

        for (double x = 0.5; x < viewportWidth(); x += GRID_SIZE) {
            graphics.strokeLine(x, 0.0, x, viewportHeight());
        }
        for (double y = 0.5; y < viewportHeight(); y += GRID_SIZE) {
            graphics.strokeLine(0.0, y, viewportWidth(), y);
        }
    }

    private void drawArenaBorder() {
        graphics.setStroke(BORDER);
        graphics.setLineWidth(2.0);
        graphics.strokeRect(1.0, 1.0, viewportWidth() - 2.0, viewportHeight() - 2.0);
    }

    private void drawPlayer(Player player) {
        double x = player.x();
        double y = player.y();
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
        graphics.strokeLine(player.centerX(), y + 4.0, player.centerX(), antennaTop + 5.0);
        graphics.strokeOval(player.centerX() - 2.5, antennaTop, 5.0, 5.0);
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
