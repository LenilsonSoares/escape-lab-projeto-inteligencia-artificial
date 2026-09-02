package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.telemetry.FrameMetrics;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * Organiza as camadas visuais sem alterar o estado do jogo.
 */
public final class GameRenderer {

    private final Canvas canvas;
    private final GraphicsContext graphics;
    private final LaboratoryPainter laboratoryPainter;
    private final WorldPainter worldPainter;
    private final HudPainter hudPainter;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.graphics = canvas.getGraphicsContext2D();
        this.graphics.setImageSmoothing(false);
        this.laboratoryPainter = new LaboratoryPainter(graphics);
        this.worldPainter = new WorldPainter(graphics);
        this.hudPainter = new HudPainter(graphics);
    }

    public void render(GameWorld world, FrameMetrics metrics) {
        RenderLayout layout = RenderLayout.calculate(
                viewportWidth(),
                viewportHeight(),
                world.tileMap()
        );

        graphics.save();
        laboratoryPainter.drawBackground(viewportWidth(), viewportHeight());
        laboratoryPainter.drawMap(world.tileMap(), layout);
        graphics.restore();

        graphics.save();
        worldPainter.drawNavigation(
                world.navigationPath(),
                world.navigationDestination(),
                world.navigationStatus(),
                world.tileMap().tileSize(),
                layout
        );
        worldPainter.drawAgent(world.autonomousAgent(), layout);
        worldPainter.drawPlayer(world.player(), layout);
        graphics.restore();

        graphics.save();
        hudPainter.draw(world, metrics, layout);
        graphics.restore();
    }

    public double viewportWidth() {
        return canvas.getWidth();
    }

    public double viewportHeight() {
        return canvas.getHeight();
    }
}
