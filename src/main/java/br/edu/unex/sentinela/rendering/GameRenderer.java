package br.edu.unex.sentinela.rendering;

import br.edu.unex.sentinela.entity.Enemy;
import br.edu.unex.sentinela.game.AiMode;
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
    private final DoorPainter doorPainter;
    private final WorldPainter worldPainter;
    private final ExitPainter exitPainter;
    private final ActorPainter actorPainter;
    private final DebugOverlayPainter debugOverlayPainter;
    private final PerceptionPainter perceptionPainter;
    private final RobotLabelPainter robotLabelPainter;
    private final HudPainter hudPainter;
    private final MapPresentationPainter mapPresentationPainter;

    public GameRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.graphics = canvas.getGraphicsContext2D();
        this.graphics.setImageSmoothing(false);
        this.laboratoryPainter = new LaboratoryPainter(graphics);
        this.laboratoryEffectsPainter = new LaboratoryEffectsPainter(graphics);
        this.doorPainter = new DoorPainter(graphics);
        this.worldPainter = new WorldPainter(graphics);
        this.exitPainter = new ExitPainter(graphics);
        this.actorPainter = new ActorPainter(graphics);
        this.debugOverlayPainter = new DebugOverlayPainter(graphics);
        this.perceptionPainter = new PerceptionPainter(graphics);
        this.robotLabelPainter = new RobotLabelPainter(graphics);
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
        doorPainter.draw(world, layout);
        graphics.restore();

        if (debugVisible) {
            drawDebugGround(world, layout);
        }

        graphics.save();
        // No modo de IA as rotas só aparecem com TAB; na demonstração do A* elas são o assunto.
        boolean showRoutes = debugVisible || world.mode() != AiMode.FULL_AI;
        if (!world.escapeCompleted() && showRoutes) {
            for (Enemy enemy : world.enemies()) {
                if (enemy != world.enemies().getFirst()) {
                    worldPainter.drawEnemyNavigation(enemy, world.tileMap().tileSize(), layout);
                }
            }
            worldPainter.drawNavigation(
                    world.navigationPath(),
                    world.navigationDestination(),
                    world.navigationStatus(),
                    world.tileMap().tileSize(),
                    layout,
                    theme
            );
        }
        if (world.mode() == AiMode.FULL_AI) {
            drawPerception(world, layout, debugVisible);
        }
        exitPainter.draw(
                world.currentMap(),
                world.player(),
                world.tileMap().tileSize(),
                layout
        );
        for (Enemy enemy : world.enemies()) {
            actorPainter.drawEnemy(enemy, layout);
        }
        robotLabelPainter.draw(world, layout, debugVisible);
        // Piscando: o jogador acabou de levar um toque e está protegido.
        if (world.playerProtected() && (int) (world.time() * 8.0) % 2 == 0) {
            graphics.setGlobalAlpha(0.3);
        }
        actorPainter.drawPlayer(world.player(), layout);
        graphics.setGlobalAlpha(1.0);
        graphics.restore();

        graphics.save();
        hudPainter.draw(world, metrics, layout, debugVisible);
        graphics.restore();

        graphics.save();
        mapPresentationPainter.draw(world, layout, sessionStarted, paused);
        graphics.restore();
    }

    private void drawDebugGround(GameWorld world, RenderLayout layout) {
        graphics.save();
        debugOverlayPainter.drawGrid(world.tileMap(), layout);
        debugOverlayPainter.drawVisitedNodes(world, layout);
        graphics.restore();
    }

    // Cones, ondas e audição ficam acima do piso e abaixo dos robôs, recortados no mapa.
    private void drawPerception(GameWorld world, RenderLayout layout, boolean debugVisible) {
        graphics.save();
        graphics.beginPath();
        graphics.rect(layout.mapX(), layout.mapY(), layout.mapWidth(), layout.mapHeight());
        graphics.clip();
        perceptionPainter.drawVisionCones(world, layout);
        perceptionPainter.drawNoiseWaves(world, layout);
        if (debugVisible) {
            debugOverlayPainter.drawHearingRanges(world, layout);
        }
        graphics.restore();
    }

    public double viewportWidth() {
        return canvas.getWidth();
    }

    public double viewportHeight() {
        return canvas.getHeight();
    }
}
