package br.edu.unex.sentinela.core;

import br.edu.unex.sentinela.game.GameWorld;
import br.edu.unex.sentinela.input.InputManager;
import br.edu.unex.sentinela.input.MovementInput;
import br.edu.unex.sentinela.rendering.GameRenderer;
import br.edu.unex.sentinela.telemetry.FrameMetrics;
import javafx.animation.AnimationTimer;

/**
 * Coordena o ciclo input -> update -> render da aplicação.
 */
public final class GameEngine extends AnimationTimer {

    static final double MAX_DELTA_SECONDS = 0.05;
    private static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0;

    private final InputManager inputManager;
    private final GameWorld world;
    private final GameRenderer renderer;
    private final FrameMetrics metrics;

    private long previousFrameTime;
    private MovementInput currentInput = MovementInput.NONE;

    public GameEngine(
            InputManager inputManager,
            GameWorld world,
            GameRenderer renderer,
            FrameMetrics metrics
    ) {
        this.inputManager = inputManager;
        this.world = world;
        this.renderer = renderer;
        this.metrics = metrics;
    }

    @Override
    public void handle(long now) {
        if (previousFrameTime == 0L) {
            previousFrameTime = now;
            resizeWorldToViewport();
            render();
            return;
        }

        double frameTime = calculateFrameTime(now);
        double simulationDeltaTime = Math.min(frameTime, MAX_DELTA_SECONDS);
        previousFrameTime = now;

        processInput();
        update(simulationDeltaTime, frameTime);
        render();
    }

    private double calculateFrameTime(long now) {
        double elapsedSeconds = (now - previousFrameTime) / NANOSECONDS_PER_SECOND;
        return Math.max(0.0, elapsedSeconds);
    }

    private void processInput() {
        currentInput = inputManager.movementInput();
    }

    private void update(double simulationDeltaTime, double frameTime) {
        resizeWorldToViewport();
        world.update(simulationDeltaTime, currentInput);
        metrics.recordFrame(frameTime);
    }

    private void render() {
        renderer.render(world, metrics);
    }

    private void resizeWorldToViewport() {
        world.resize(renderer.viewportWidth(), renderer.viewportHeight());
    }

    public void shutdown() {
        stop();
        inputManager.clear();
        previousFrameTime = 0L;
    }
}
