package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.entity.EnemyType;
import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.game.TileMap;
import br.edu.unex.sentinela.navigation.GridPosition;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/** Camada do TAB: grade lógica, nós visitados pelo A* e alcance da audição. */
final class DebugOverlayPainter {

    private final GraphicsContext graphics;

    DebugOverlayPainter(GraphicsContext graphics) {
        this.graphics = graphics;
    }

    /**
     * Robô cujo A* aparece no debug: o primeiro (guarda). Enquanto ele está parado
     * no posto, mostra o primeiro robô que calculou uma rota de verdade.
     */
    static Enemy aStarFocus(List<Enemy> enemies) {
        return enemies.stream()
                .filter(DebugOverlayPainter::hasRealRoute)
                .findFirst()
                .orElse(enemies.getFirst());
    }

    private static boolean hasRealRoute(Enemy enemy) {
        return enemy.lastSearch().found() && enemy.lastSearch().pathCost() > 0;
    }

    static double hearingRadius(EnemyType type, double tileSize) {
        return GameWorld.STEP_NOISE_TILES * type.hearingSensitivity() * tileSize;
    }

    void drawGrid(TileMap map, RenderLayout layout) {
        double tile = map.tileSize() * layout.scale();
        graphics.setFill(AiPalette.GRID_LINE);
        graphics.setGlobalAlpha(0.07);
        for (int column = 1; column < map.columns(); column++) {
            graphics.fillRect(Math.rint(layout.mapX() + column * tile), layout.mapY(), 1.0, layout.mapHeight());
        }
        for (int row = 1; row < map.rows(); row++) {
            graphics.fillRect(layout.mapX(), Math.rint(layout.mapY() + row * tile), layout.mapWidth(), 1.0);
        }
        graphics.setGlobalAlpha(1.0);
    }

    void drawVisitedNodes(GameWorld world, RenderLayout layout) {
        Enemy focus = aStarFocus(world.enemies());
        double tileSize = world.tileMap().tileSize();
        double size = tileSize * layout.scale();
        double dot = Math.max(2.0, 3.0 * layout.scale());
        graphics.setFill(AiPalette.VISITED_NODE);
        for (GridPosition node : focus.lastSearch().visited()) {
            double x = layout.screenX(node.column() * tileSize);
            double y = layout.screenY(node.row() * tileSize);
            graphics.setGlobalAlpha(0.10);
            graphics.fillRect(x + 1.0, y + 1.0, size - 2.0, size - 2.0);
            graphics.setGlobalAlpha(0.45);
            graphics.fillRect(x + size / 2.0 - dot / 2.0, y + size / 2.0 - dot / 2.0, dot, dot);
        }
        graphics.setGlobalAlpha(1.0);
    }

    void drawHearingRanges(GameWorld world, RenderLayout layout) {
        double scale = layout.scale();
        graphics.setLineWidth(Math.max(1.0, scale));
        graphics.setLineDashes(4.0 * scale, 5.0 * scale);
        graphics.setGlobalAlpha(0.30);
        for (Enemy enemy : world.enemies()) {
            if (!enemy.hasAi()) {
                continue;
            }
            double radius = hearingRadius(enemy.type(), world.tileMap().tileSize()) * scale;
            double centerX = layout.screenX(enemy.agent().centerX());
            double centerY = layout.screenY(enemy.agent().centerY());
            Color color = AiPalette.identity(enemy.type());
            graphics.setStroke(color);
            graphics.strokeOval(centerX - radius, centerY - radius, radius * 2.0, radius * 2.0);
        }
        graphics.setLineDashes();
        graphics.setGlobalAlpha(1.0);
    }
}
