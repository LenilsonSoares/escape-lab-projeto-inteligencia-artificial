package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.game.TileType;
import br.edu.unex.sentinela.navigation.GridPosition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/** Desenha as portas de segurança e o cartão de acesso, que mudam durante a partida. */
final class DoorPainter {

    private static final Color FRAME = Color.web("#8fa3b3");
    private static final Color FRAME_DARK = Color.web("#1a242e");
    private static final Color LEAF = Color.web("#3b4b58");
    private static final Color LEAF_EDGE = Color.web("#5b6f7e");
    private static final Color STRIPE_RED = Color.web("#d63c4a");
    private static final Color STRIPE_AMBER = Color.web("#f2a541");
    private static final Color LOCK_BODY = Color.web("#ffcf5a");
    private static final Color LOCK_DARK = Color.web("#3a2410");
    private static final Color OPEN_GREEN = Color.web("#52e889");
    private static final Color CARD_BODY = Color.web("#e9fbff");
    private static final Color CARD_STRIPE = Color.web("#22c7d8");
    private static final Color CARD_CHIP = Color.web("#f2c14e");
    private static final Color CARD_LABEL_BACKGROUND = Color.web("#03121a", 0.88);
    private static final Font CARD_FONT = Font.font("Monospaced", FontWeight.BOLD, 7.0);

    private final GraphicsContext graphics;

    DoorPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    void draw(GameWorld world, RenderLayout layout) {
        TileMap tileMap = world.tileMap();
        double seconds = System.nanoTime() / 1_000_000_000.0;
        for (int row = 0; row < tileMap.rows(); row++) {
            for (int column = 0; column < tileMap.columns(); column++) {
                if (tileMap.tileAt(row, column) == TileType.DOOR) {
                    drawDoor(tileMap, row, column, layout, seconds);
                }
            }
        }
        if (!world.cardCollected()) {
            world.cardPosition().ifPresent(card -> drawCard(card, tileMap.tileSize(), layout, seconds));
        }
    }

    private void drawDoor(TileMap tileMap, int row, int column, RenderLayout layout, double seconds) {
        double size = tileMap.tileSize();
        graphics.save();
        graphics.translate(layout.screenX(column * size), layout.screenY(row * size));
        graphics.scale(layout.scale() * size / 40.0, layout.scale() * size / 40.0);
        // O desenho base fecha uma passagem horizontal; com paredes dos lados, gira 90 graus.
        if (isWall(tileMap, row, column - 1) && isWall(tileMap, row, column + 1)) {
            graphics.translate(40.0, 0.0);
            graphics.rotate(90.0);
        }
        if (tileMap.doorsOpen()) {
            drawOpenDoor(seconds);
        } else {
            drawClosedDoor(seconds + row * 0.7 + column * 0.3);
        }
        graphics.restore();
    }

    private void drawClosedDoor(double seconds) {
        double pulse = (Math.sin(seconds * 3.5) + 1.0) / 2.0;
        graphics.setFill(Color.color(0.84, 0.24, 0.29, 0.10 + pulse * 0.16));
        graphics.fillRect(0.0, 0.0, 40.0, 40.0);

        graphics.setFill(FRAME_DARK);
        graphics.fillRect(10.0, 0.0, 20.0, 40.0);
        graphics.setFill(FRAME);
        graphics.fillRect(10.0, 0.0, 2.0, 40.0);
        graphics.fillRect(28.0, 0.0, 2.0, 40.0);

        graphics.setFill(LEAF);
        graphics.fillRect(13.0, 1.0, 14.0, 38.0);
        graphics.setFill(LEAF_EDGE);
        graphics.fillRect(13.0, 1.0, 1.0, 38.0);
        graphics.fillRect(19.5, 1.0, 1.0, 38.0);

        // Faixas de alerta em degraus, no estilo pixel art.
        for (int stripe = 0; stripe < 7; stripe++) {
            graphics.setFill(stripe % 2 == 0 ? STRIPE_RED : STRIPE_AMBER);
            double y = 2.0 + stripe * 5.0;
            graphics.fillRect(14.0, y, 5.0, 3.0);
            graphics.fillRect(21.0, y + 2.0, 5.0, 3.0);
        }

        graphics.setFill(LOCK_DARK);
        graphics.fillRect(14.0, 15.0, 12.0, 11.0);
        graphics.setStroke(LOCK_BODY);
        graphics.setLineWidth(1.5);
        graphics.strokeRect(17.0, 13.0, 6.0, 5.0);
        graphics.setFill(Color.color(1.0, 0.81, 0.35, 0.65 + pulse * 0.35));
        graphics.fillRect(15.0, 18.0, 10.0, 7.0);
        graphics.setFill(LOCK_DARK);
        graphics.fillRect(19.0, 20.0, 2.0, 3.0);
    }

    private void drawOpenDoor(double seconds) {
        double pulse = (Math.sin(seconds * 2.0) + 1.0) / 2.0;
        graphics.setFill(Color.color(0.32, 0.91, 0.54, 0.06 + pulse * 0.06));
        graphics.fillRect(0.0, 0.0, 40.0, 40.0);

        graphics.setFill(FRAME_DARK);
        graphics.fillRect(10.0, 0.0, 3.0, 40.0);
        graphics.fillRect(27.0, 0.0, 3.0, 40.0);
        graphics.setFill(LEAF);
        graphics.fillRect(10.0, 0.0, 2.0, 40.0);
        graphics.fillRect(28.0, 0.0, 2.0, 40.0);
        graphics.setFill(LEAF_EDGE);
        graphics.fillRect(12.0, 0.0, 1.0, 40.0);
        graphics.fillRect(27.0, 0.0, 1.0, 40.0);

        graphics.setFill(OPEN_GREEN);
        graphics.fillRect(10.5, 3.0, 1.0, 3.0);
        graphics.fillRect(28.5, 3.0, 1.0, 3.0);
        graphics.fillRect(10.5, 34.0, 1.0, 3.0);
        graphics.fillRect(28.5, 34.0, 1.0, 3.0);
    }

    private void drawCard(GridPosition card, double tileSize, RenderLayout layout, double seconds) {
        double pulse = (Math.sin(seconds * 3.0) + 1.0) / 2.0;
        double centerX = layout.screenX((card.column() + 0.5) * tileSize);
        double centerY = layout.screenY((card.row() + 0.5) * tileSize);

        graphics.save();
        graphics.translate(centerX, centerY);
        graphics.scale(layout.scale(), layout.scale());
        double hover = Math.sin(seconds * 2.4) * 2.0;

        graphics.setFill(Color.color(0.13, 0.78, 0.85, 0.12 + pulse * 0.18));
        double glow = 13.0 + pulse * 3.0;
        graphics.fillOval(-glow, -glow + hover, glow * 2.0, glow * 2.0);
        graphics.setFill(Color.color(0.0, 0.0, 0.0, 0.30));
        graphics.fillOval(-7.0, 9.0, 14.0, 3.0);

        graphics.setFill(CARD_BODY);
        graphics.fillRect(-8.0, -6.0 + hover, 16.0, 11.0);
        graphics.setFill(CARD_STRIPE);
        graphics.fillRect(-8.0, -6.0 + hover, 16.0, 3.0);
        graphics.fillRect(1.0, 0.0 + hover, 5.0, 1.0);
        graphics.fillRect(1.0, 2.0 + hover, 3.0, 1.0);
        graphics.setFill(CARD_CHIP);
        graphics.fillRect(-6.0, -1.0 + hover, 4.0, 4.0);
        graphics.setStroke(Color.color(0.13, 0.78, 0.85, 0.55 + pulse * 0.45));
        graphics.setLineWidth(1.0);
        graphics.strokeRect(-8.5, -6.5 + hover, 17.0, 12.0);

        graphics.setFill(CARD_LABEL_BACKGROUND);
        graphics.fillRect(-15.0, -20.0, 30.0, 9.0);
        graphics.setFill(CARD_STRIPE);
        graphics.setFont(CARD_FONT);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.fillText("CARTÃO", 0.0, -13.5, 28.0);
        graphics.restore();
    }

    private static boolean isWall(TileMap tileMap, int row, int column) {
        return row >= 0
                && row < tileMap.rows()
                && column >= 0
                && column < tileMap.columns()
                && tileMap.tileAt(row, column) == TileType.WALL;
    }
}
