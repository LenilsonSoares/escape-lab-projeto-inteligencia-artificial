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
    private final LaboratoryEffectsPainter laboratoryEffectsPainter;
    private final WorldPainter worldPainter;
    private final ExitPainter exitPainter;
    private final ActorPainter actorPainter;
    private final HudPainter hudPainter;
    private final MapPresentationPainter mapPresentationPainter;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.graphics = canvas.getGraphicsContext2D();
        this.graphics.setImageSmoothing(false);
        this.laboratoryPainter = new LaboratoryPainter(graphics);
        this.laboratoryEffectsPainter = new LaboratoryEffectsPainter(graphics);
        this.worldPainter = new WorldPainter(graphics);
        this.exitPainter = new ExitPainter(graphics);
        this.actorPainter = new ActorPainter(graphics);
        this.hudPainter = new HudPainter(graphics);
        this.mapPresentationPainter = new MapPresentationPainter(graphics);
    }

    public void render(
            GameWorld world,
            FrameMetrics metrics,
            boolean debugVisible,
            boolean sessionStarted,
            boolean paused
    ) {
        RenderLayout layout = RenderLayout.calculate(
                viewportWidth(),
                viewportHeight(),
                world.tileMap()
        );
        LaboratoryTheme theme = LaboratoryTheme.forMap(world.currentMap());

        graphics.save();
        laboratoryPainter.drawBackground(viewportWidth(), viewportHeight());
        laboratoryPainter.drawMap(world.tileMap(), layout, theme);
        laboratoryEffectsPainter.draw(world.tileMap(), layout, theme);
        graphics.restore();

        graphics.save();
        if (!world.escapeCompleted()) {
            worldPainter.drawNavigation(
                    world.navigationPath(),
                    world.navigationDestination(),
                    world.navigationStatus(),
                    world.tileMap().tileSize(),
                    layout,
                    theme
            );
        }
        exitPainter.draw(
                world.currentMap(),
                world.player(),
                world.tileMap().tileSize(),
                layout
        );
        actorPainter.drawAgent(world.autonomousAgent(), layout);
        actorPainter.drawPlayer(world.player(), layout);
        graphics.restore();

        graphics.save();
        hudPainter.draw(world, metrics, layout, debugVisible, sessionStarted);
        graphics.restore();

        graphics.save();
        mapPresentationPainter.draw(world, layout, sessionStarted, paused);
        graphics.restore();
    }

    public double viewportWidth() {
        return canvas.getWidth();
    }

    public double viewportHeight() {
        return canvas.getHeight();
    }
}
